package models.activate

import net.fwbrasil.activate.ActivateContext
import net.fwbrasil.activate.storage.Storage
import net.fwbrasil.activate.storage.memory.TransientMemoryStorage
import net.fwbrasil.activate.storage.relational.PooledJdbcRelationalStorage
import net.fwbrasil.activate.storage.relational.idiom.postgresqlDialect
import net.fwbrasil.activate.storage.relational.idiom.h2Dialect
import play.api.Play
import play.api.Mode

class ComputerPersistenceContext(app: play.api.Application) extends ActivateContext {

  // val storage = new TransientMemoryStorage
  ///*
  val storage = app.mode match {
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

