package uk.gov.gds.common.mongo

import uk.gov.gds.common.config.Config
import uk.gov.gds.common.logging.Logging
import util.control.Exception

object MongoConfig extends Logging {

  def slaveOk = Config("mongo.database.slaveok", "true").toBoolean
}