package models.activate

import net.fwbrasil.activate.ActivateContext
import net.fwbrasil.activate.storage.Storage
import net.fwbrasil.activate.storage.memory.TransientMemoryStorage
import net.fwbrasil.activate.storage.relational.PooledJdbcRelationalStorage
import net.fwbrasil.activate.storage.relational.idiom.postgresqlDialect
import net.fwbrasil.activate.storage.relational.idiom.h2Dialect
import play.api.Play
import play.api.Mode

object computerPersistenceContext extends ActivateContext {

  import Play.current

  // val storage = new TransientMemoryStorage
  ///*
  val storage = Play.application.mode match {
    case Mode.Dev | Mode.Test => new TransientMemoryStorage
    case Mode.Prod => {
      Play.current.configuration.getConfig("db.default").map{ config =>
        val driverConfig = config.getString("driver").getOrElse("org.h2.Driver")
        val urlConfig = config.getString("url").getOrElse("jdbc:h2:mem:play-webshop")
        val userConfig = config.getString("user").getOrElse("sa")
        val passwordConfig = config.getString("password").orNull;
        if ("org.postgresql.Driver".equals(driverConfig)) {
	      new PooledJdbcRelationalStorage {
	        val jdbcDriver = driverConfig
	        val user = userConfig
	        val password = passwordConfig
	        val url = urlConfig
	        val dialect = postgresqlDialect
	      }
        }
        else if ("org.h2.Driver".equals(driverConfig)) {
          new PooledJdbcRelationalStorage {
	        val jdbcDriver = driverConfig
	        val user = userConfig
	        val password = passwordConfig
	        val url = urlConfig
	        val dialect = h2Dialect
          }
        }
        else {
          throw new IllegalArgumentException("Unsupported db configuration (currently only postgresql and h2 are supported): " + config);
        }
      }.getOrElse(throw new IllegalArgumentException("No configuration for db.default found"))
    }
  }
  // */
}

