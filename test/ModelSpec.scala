import org.specs2.mutable._
import org.specs2.runner._
import org.junit.runner._
import play.api.test._
import play.api.test.Helpers._
import models.activate.computerPersistenceContext._
import net.fwbrasil.activate.statement.Criteria
import net.fwbrasil.activate.entity.map.MutableEntityMap

@RunWith(classOf[JUnitRunner])
class ModelSpec extends Specification {
  
  import models.activate._

  // -- Date helpers
  
  def dateIs(date: java.util.Date, str: String) = new java.text.SimpleDateFormat("yyyy-MM-dd").format(date) == str
  
  // --
  
  "Computer model" should {
    
    "be retrieved by id" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        transactional {
          val id = select[Computer].where(_.name :== "Macintosh").map(_.id).head
          
          val Some(macintosh) = byId[Computer](id)
          macintosh.name must equalTo("Macintosh")
          macintosh.introduced must beSome.which(dateIs(_, "1984-01-24"))

        }
      }
    }
    
    "be listed along its companies" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        
        val computers = Computer.list()

        computers.total must equalTo(565)
        computers.items must have length(10)

      }
    }
    
    "be updated if needed" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        val c = transactional {
          val c = select[Computer].where(_.name :== "Macintosh", _.introduced isNotNull).head
          // update { (comp: Computer) => where(comp.id :== c.id) set(comp.name := "The Macintosh", comp.introduced := None) }
          new MutableEntityMap[Computer]()
          .put(_.name)("The Macintosh")
          .put(_.introduced)(None)
          .updateEntity(c)
          c
        }
        transactional {
	      // Computer.update(21, Computer(name="The Macintosh", introduced=None, discontinued=None, companyId=Some(1)))
          val Some(macintosh) = byId[Computer](c.id)
          macintosh.name must equalTo("The Macintosh")
          macintosh.introduced must beNone
        }
      }
    }

  }

}