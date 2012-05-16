package uk.gov.gds.common.mongo

import migration.MigrationTestRepostiory

object MongoDatabaseManagerForTests extends MongoDatabaseManager {

  protected val repositoriesToInitialiseOnStartup = List(MigrationTestRepostiory())
}
