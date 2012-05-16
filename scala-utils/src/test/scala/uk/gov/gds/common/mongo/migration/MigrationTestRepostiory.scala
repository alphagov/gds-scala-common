package uk.gov.gds.common.mongo.migration

import uk.gov.gds.common.mongo.MongoDatabaseManagerForTests

class MigrationTestRepostiory(val changeScripts: List[ChangeScript]) extends ChangeLogRepository {

  protected val databaseIdProperty = "scala-common-test-changelog"

  protected val collection = MongoDatabaseManagerForTests.collection("changelog")

  override def startup() {
    super.startup()
    deleteAll()
  }
}

object MigrationTestRepostiory {
  def apply(changeScripts: List[ChangeScript]) = new MigrationTestRepostiory(changeScripts)

  def apply(changeScript: ChangeScript) = new MigrationTestRepostiory(List(changeScript))

  def apply() = new MigrationTestRepostiory(Nil)
}

