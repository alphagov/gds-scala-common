package uk.gov.gds.common.mongo

import com.mongodb.WriteConcern

object MongoDatabaseManagerForTests extends MongoDatabaseManager {

  database.setWriteConcern(WriteConcern.MAJORITY)

  protected val repositoriesToInitialiseOnStartup = Nil
}
