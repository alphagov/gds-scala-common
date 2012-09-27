package uk.gov.gds.common.mongo

import uk.gov.gds.common.config.Config
import uk.gov.gds.common.logging.Logging
import util.control.Exception

object MongoConfig extends Logging {

  def authenticated = {
    try {
      Config("mongo.database.auth.username")
      true
    } catch { case _ => false }
  }

  def slaveOk = Config("mongo.database.slaveok", "true").toBoolean
}