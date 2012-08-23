package uk.gov.gds.common.testutil

import org.scalatest._
import uk.gov.gds.common.mongo.MongoDatabaseManager
import play.api.Logger

trait MongoDatabaseBackedTest extends BeforeAndAfterEach {
  self: BeforeAndAfterEach with Suite =>

  protected def databaseManager: MongoDatabaseManager

  override protected def beforeEach() {
    IntegrationTestMutex.lock()
    Logger.warn("Locking for Test " + super.toString)
    super.beforeEach()
    databaseManager.emptyDatabase()
  }

  override protected def afterEach() {
    try {
      super.afterEach()
    }
    finally {
      Logger.warn("Unlocking after Test " + super.toString)
      IntegrationTestMutex.unlock()
    }
  }
}