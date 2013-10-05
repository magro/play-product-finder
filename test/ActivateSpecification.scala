import net.fwbrasil.activate.storage.memory.TransientMemoryStorage
import org.specs2.mutable.Specification
import play.api.test._
import play.api.test.Helpers._
import play.api.GlobalSettings
import models.activate.SeedData

/**
 * Inspired by https://github.com/kouphax/try-things
 */
trait ActivateSpecification { self: Specification  =>
  
  def withNewDatabase[T](block: => T): T = {
    import models.activate.computerPersistenceContext._

    // This cleans all data, which is not desired in our case...
//    storage.asInstanceOf[TransientMemoryStorage].directAccess.clear()

    // This does also not work...
//    val seed = new SeedData
//    seed.down
//    seed.up

    reinitializeContext

    block
  }

  def withSeededDatabase[T](seed: => Unit)(block: => T) = withNewDatabase {
    import models.activate.computerPersistenceContext._
    transactional(seed)
    block
  }

  def fakeApp = FakeApplication(
    withGlobal = Some(new GlobalSettings {}),
    additionalPlugins = Seq("net.fwbrasil.activate.play.ActivatePlayPlugin"))

  implicit class DSL(thing: String) {
    def inRunningApp[T](b: => T)( implicit evidence$1 : org.specs2.execute.AsResult[T]): org.specs2.specification.Example = {
      self.inExample(thing).in(withRunningApp(b))
    }
    def inBrowser[T](b: TestBrowser => T)( implicit evidence$1 : org.specs2.execute.AsResult[T]): org.specs2.specification.Example = {
      self.inExample(thing).in {
        running(TestServer(3333), HTMLUNIT) { browser =>
          withNewDatabase(b(browser))
        }
      }
    }
  }

  def withRunningApp[T](b: => T): T = {
    running(fakeApp)(withNewDatabase(b))
  }

}