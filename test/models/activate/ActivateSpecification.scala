package models.activate

import org.specs2.mutable.Specification

import play.api.test._
import play.api.test.Helpers._
import play.api.GlobalSettings

import models.activate.computerPersistenceContext._

import net.fwbrasil.activate.migration.StorageVersion
import net.fwbrasil.activate.storage.memory.TransientMemoryStorage
import net.fwbrasil.activate.storage.relational.PooledJdbcRelationalStorage

/**
 * Inspired by https://github.com/kouphax/try-things
 */
trait ActivateSpecification { self: Specification  =>
  
  def withNewDatabase[T](clearData: Boolean, block: => T): T = {
    import models.activate.computerPersistenceContext._

    if (clearData) {
      if (storage.isInstanceOf[PooledJdbcRelationalStorage]) {
        transactional {
          all[Computer].foreach(_.delete)
          all[Company].foreach(_.delete)
          all[Product].foreach(_.delete)
          all[Category].foreach(_.delete)
          all[Brand].foreach(_.delete)
        }
      } else if (storage.isInstanceOf[TransientMemoryStorage])
        storage.asInstanceOf[TransientMemoryStorage].directAccess.clear()
      else
        throw new IllegalStateException("Unsupported storage type " + storage.getClass())
    }

    reinitializeContext

    block
  }

  def withSeededDatabase[T](seed: => Unit)(block: => T) = withNewDatabase(true, {
    import models.activate.computerPersistenceContext._
    transactional(seed)
    block
  })
  
  def category(categoryName: String): Category = {
    import models.activate.computerPersistenceContext._
    val items = select[Category].where(_.name :== categoryName)
    items.headOption.getOrElse(new Category(categoryName))
  }
  
  def brand(brandName: String): Brand = {
    import models.activate.computerPersistenceContext._
    val items = select[Brand].where(_.name :== brandName)
    items.headOption.getOrElse(new Brand(brandName))
  }

  def fakeApp = FakeApplication(
    // withGlobal = Some(new GlobalSettings {}),
    additionalPlugins = Seq("net.fwbrasil.activate.play.ActivatePlayPlugin"))

  implicit class DSL(thing: String) {
    def inRunningApp[T](b: => T)( implicit evidence$1 : org.specs2.execute.AsResult[T]): org.specs2.specification.Example = {
      self.inExample(thing).in(withRunningApp(true, b))
    }
    def inRunningAppWithSeedData[T](b: => T)( implicit evidence$1 : org.specs2.execute.AsResult[T]): org.specs2.specification.Example = {
      self.inExample(thing).in(withRunningApp(false, b))
    }
    def inRunningAppWithSeed[T](seed: => Unit, b: => T)( implicit evidence$1 : org.specs2.execute.AsResult[T]): org.specs2.specification.Example = {
      self.inExample(thing).in(running(fakeApp)(withSeededDatabase(seed)(b)))
    }
    def inBrowserWithSeedData[T](b: TestBrowser => T)( implicit evidence$1 : org.specs2.execute.AsResult[T]): org.specs2.specification.Example = {
      self.inExample(thing).in {
        running(TestServer(3333), HTMLUNIT) { browser =>
          withNewDatabase(false, b(browser))
        }
      }
    }
  }

  def withRunningApp[T](clearData: Boolean, b: => T): T = {
    running(fakeApp)(withNewDatabase(clearData, b))
  }

}