package net.fwbrasil.activate.test

import org.specs2.mutable.Specification

import play.api.Play
import play.api.test._
import play.api.test.Helpers._

import net.fwbrasil.activate.migration.Migration
import net.fwbrasil.activate.migration.ManualMigration
import net.fwbrasil.activate.ActivateContext
import net.fwbrasil.activate.migration.StorageVersion
import net.fwbrasil.activate.storage.memory.TransientMemoryStorage
import net.fwbrasil.activate.storage.relational.PooledJdbcRelationalStorage
import net.fwbrasil.activate.storage.relational.async.AsyncPostgreSQLStorage


trait Strategy {
  def runTest[R](seed: => Option[() => Unit], f: => R)(implicit context: ActivateContext): R
}

object transactionRollbackStrategy extends Strategy {
  def runTest[R](seed: => Option[() => Unit] = None, f: => R)(implicit ctx: ActivateContext) = {
    import ctx._
    val transaction = new Transaction
    try {
      seed.foreach(s => transactional(transaction)(s()))
      transactional(transaction)(f)
    }
    finally transaction.rollback
  }
}

object cleanDatabaseStrategy extends Strategy {
  
  def runTest[R](seed: => Option[() => Unit] = None, f: => R)(implicit ctx: ActivateContext) = {
    import ctx._
    cleanDatabase
    ctx.reinitializeContext
    seed.foreach(s => transactional(s()))
    transactional(f)
  }

  def cleanDatabase(implicit ctx: ActivateContext) {
    import ctx._
    if (storage.isInstanceOf[PooledJdbcRelationalStorage]
        || storage.isInstanceOf[AsyncPostgreSQLStorage]) {
      transactional {
        // TODO: find a better way to dynamically delete all entities
        import models.activate._
        all[Shop].foreach(_.delete)
      }
    } else if (storage.isInstanceOf[TransientMemoryStorage])
      storage.asInstanceOf[TransientMemoryStorage].directAccess.clear()
    else
      throw new IllegalStateException("Unsupported storage type " + storage.getClass())
  }
}

object recreateDatabaseStrategy extends Strategy {

  def runTest[R](seed: => Option[() => Unit] = None, f: => R)(implicit ctx: ActivateContext) = {
    import ctx._
    cleanDatabase
    resetStorageVersion
    ctx.reinitializeContext
    ctx.runMigration
    seed.foreach(s => transactional(s()))
    transactional(f)
  }

  def resetStorageVersion(implicit ctx: ActivateContext) =
    ctx.transactional {
      val storageVersion = Migration.storageVersionCache(ctx.name)
      storageVersion.lastScript = -1
      storageVersion.lastAction = -1
    }

  def cleanDatabase(implicit ctx: ActivateContext) =
    new ManualMigration {
      def up = {
        removeReferencesForAllEntities.ifExists
        removeAllEntitiesTables.ifExists
      }
    }.execute
}

trait ActivateTest { self: Specification =>

  def strategy: Strategy = recreateDatabaseStrategy

  // No implicit here, to have the dependencies more explicit
  def context(app: play.api.Application): ActivateContext

  def fakeApp = FakeApplication(
    // withGlobal = Some(new GlobalSettings {}),
    additionalPlugins = Seq("net.fwbrasil.activate.play.ActivatePlayPlugin"))

  implicit class DSL(thing: String) {

    def inActivate[R](f: => R)(implicit evidence$1: org.specs2.execute.AsResult[R]): org.specs2.specification.Example = {
      self.inExample(thing).in {
        implicit val app = fakeApp
        running(app)(strategy.runTest(None, f)(context(app)))
      }
    }

    // TODO: it would be nice to be able to take the seed data just lazily (seed: => Unit), without forcing it to be a function
    def inActivateWithSeed[R](seed: () => Unit, f: => R)(implicit evidence$1: org.specs2.execute.AsResult[R]): org.specs2.specification.Example = {
      self.inExample(thing).in {
        implicit val app = fakeApp
        running(app)(strategy.runTest(Some(seed), f)(context(app)))
      }
    }

    def inBrowserWithActivate[R](f: TestBrowser => R)( implicit evidence$1 : org.specs2.execute.AsResult[R]): org.specs2.specification.Example = {
      self.inExample(thing).in {
        running(TestServer(3333))(withBrowser { browser =>
          val app = play.api.Play.current
          strategy.runTest(None, f(browser))(context(app))
        })
      }
    }

    /**
     * Executes a block of code with a test browser.
     */
    private def withBrowser[T](block: TestBrowser => T): T = {
      var browser: TestBrowser = null
      synchronized {
        try {
          // Use HtmlUnitDriver with Firefix 17, as with Chrome and IE (default) tests fail with e.g.
          // WebDriverException: com.gargoylesoftware.htmlunit.ScriptException: TypeError: Cannot find
          // function addEventListener in object [object HTMLDocument]. (http://code.jquery.com/jquery-1.10.1.min.js#5)
          import org.openqa.selenium.htmlunit._
          import com.gargoylesoftware.htmlunit._
          val driver = new HtmlUnitDriver(BrowserVersion.FIREFOX_17)
          driver.setJavascriptEnabled(true)
          browser = TestBrowser(driver, None)
          block(browser)
        } finally {
          if (browser != null) {
            browser.quit()
          }
        }
      }
    }

  }

}