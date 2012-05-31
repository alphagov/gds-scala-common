package uk.gov.gds.common.mongo

object MongoDatabaseManagerForTests extends MongoDatabaseManager {

  protected val repositoriesToInitialiseOnStartup = Nil
}
