package models.activate

import net.fwbrasil.activate.ActivateContext
import net.fwbrasil.activate.storage.Storage
import net.fwbrasil.activate.storage.memory.TransientMemoryStorage
import net.fwbrasil.activate.storage.relational.PooledJdbcRelationalStorage
import net.fwbrasil.activate.storage.relational.idiom.postgresqlDialect
import net.fwbrasil.activate.storage.relational.idiom.h2Dialect
import net.fwbrasil.activate.storage.relational.async.AsyncPostgreSQLStorage
import com.github.mauricio.async.db.postgresql.pool.PostgreSQLConnectionFactory
import com.github.mauricio.async.db.Configuration
import play.api._
import com.github.mauricio.async.db.postgresql.util.URLParser

object shopPersistenceContext extends ActivateContext {

  
  val storage = new AsyncPostgreSQLStorage {

    def configuration =
      new Configuration(
        username = "play",
        host = "localhost",
        password = Some("play"),
        database = Some("play-webshop"))

    val objectFactory = new PostgreSQLConnectionFactory(configuration)
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

object computerPersistenceContext extends ActivateContext {

  import Play.current

  // val storage = new TransientMemoryStorage
  ///*
  val storage = Play.application.mode match {
    case Mode.Prod => new TransientMemoryStorage
    // case Mode.Dev | Mode.Test => new TransientMemoryStorage
    case Mode.Dev | Mode.Test => {
      try {
      Play.current.configuration.getConfig("db.default").map { config =>
        val driverConfig = config.getString("driver").getOrElse("org.h2.Driver")
        if ("org.postgresql.Driver".equals(driverConfig)) {
	      new PooledJdbcRelationalStorage {
	        val jdbcDriver = "org.postgresql.Driver"
	        val config = Play.current.configuration.getConfig("db.default").get
	        val url = config.getString("url").getOrElse("jdbc:postgresql://localhost:5432/play-webshop")
	        val user = config.getString("user").getOrElse("play")
	        val password = config.getString("user").getOrElse("play")
	        val dialect = postgresqlDialect
	      }
        }
        else if ("org.h2.Driver".equals(driverConfig)) {
          new PooledJdbcRelationalStorage {
	        val jdbcDriver = driverConfig
	        val config = Play.current.configuration.getConfig("db.default").get
            val url = config.getString("url").getOrElse("jdbc:h2:mem:play-webshop")
            val user = config.getString("user").getOrElse("sa")
            val password = config.getString("password").orNull
	        val dialect = h2Dialect
          }
        }
        else {
          throw new IllegalArgumentException("Unsupported db configuration (currently only postgresql and h2 are supported): " + config);
        }
      }.getOrElse(throw new IllegalArgumentException("No configuration for db.default found"))
    } catch {
      case e: Throwable => {
        println("Caught " + e);
        e.printStackTrace()
        new TransientMemoryStorage
      }
    }
    }
  }
  // */
}

