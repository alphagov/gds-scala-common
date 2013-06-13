package uk.gov.gds.common.mongo

import uk.gov.gds.common.config.Config
import uk.gov.gds.common.logging.Logging
import util.control.Exception
import com.mongodb.ReadPreference

object MongoConfig extends Logging {

  def slaveOk = Config("mongo.database.slaveok", "true").toBoolean

  def readPreference =
    Config("mongo.database.read.preference", "default") match {
      case "primary" => Some(ReadPreference.PRIMARY)
      case "secondary" => Some(ReadPreference.SECONDARY)
      case "default" => None
      case unknown => throw new Exception("Unknown read preference value: %s".format(unknown))
  }
}