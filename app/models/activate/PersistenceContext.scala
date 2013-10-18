package models.activate

//import net.fwbrasil.activate.ActivateContext
//import net.fwbrasil.activate.storage.Storage
//import net.fwbrasil.activate.storage.memory.TransientMemoryStorage
//import net.fwbrasil.activate.storage.relational.PooledJdbcRelationalStorage
//import net.fwbrasil.activate.storage.relational.idiom.postgresqlDialect
//import net.fwbrasil.activate.storage.relational.idiom.h2Dialect
//import net.fwbrasil.activate.storage.relational.async.AsyncPostgreSQLStorage
//import com.github.mauricio.async.db.postgresql.pool.PostgreSQLConnectionFactory
//import com.github.mauricio.async.db.Configuration
//import play.api._
//import com.github.mauricio.async.db.postgresql.util.URLParser



import net.fwbrasil.activate.ActivateContext
import net.fwbrasil.activate.storage.memory.TransientMemoryStorage
import net.fwbrasil.activate.storage.relational.async.AsyncPostgreSQLStorage
import com.github.mauricio.async.db.postgresql.pool.PostgreSQLConnectionFactory
import com.github.mauricio.async.db.Configuration

object shopPersistenceContext extends ActivateContext {

  
  val storage = new AsyncPostgreSQLStorage {

    def configuration =
      new Configuration(
        username = "play",
        host = "localhost",
        password = Some("play"),
        database = Some("play-webshop"))

    lazy val objectFactory = new PostgreSQLConnectionFactory(configuration)
  }
//  import Play.current
//
//  val storage = new AsyncPostgreSQLStorage {
//
//    private def config(key: String)(implicit app: Application): Option[play.api.Configuration] = {
//      app.configuration.getConfig(key)
//    }
//
//    private def playConfig = Play.application.mode match {
//      case Mode.Prod | Mode.Dev => config("db.default").get
//      case Mode.Test => config("db.test").getOrElse(config("db.default").get)
//    }
//
//    private def configuration: Configuration = {
//      System.getenv("DATABASE_URL") match {
//        case url: String => URLParser.parse(url)
//        case _ => {
//          val config = playConfig
//          // maybe the url already has set the username and password or not.
//          var dbConfig = URLParser.parse(config.getString("url").getOrElse("jdbc:postgresql://localhost:5432/play-webshop"))
//          config.getString("user").foreach { user =>
//            dbConfig = dbConfig.copy(username = user)
//          }
//          config.getString("password").foreach { pwd =>
//            dbConfig = dbConfig.copy(password = Some(pwd))
//          }
//          dbConfig
//        }
//      }
//    }
//
//    lazy val objectFactory = new PostgreSQLConnectionFactory(configuration)
//  }

}
