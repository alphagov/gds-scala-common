package uk.gov.gds.common.mongo

import uk.gov.gds.common.config.Config

object MongoConfig {

  def slaveOk = Config("mongo.database.slaveok", "true").toBoolean
}