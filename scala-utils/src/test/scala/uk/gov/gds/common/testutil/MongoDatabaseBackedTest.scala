package uk.gov.gds.common.testutil

import org.scalatest._
import uk.gov.gds.common.logging.Logging

trait MongoDatabaseBackedTest extends BeforeAndAfterEach with Logging {
  self: BeforeAndAfterEach with Suite =>

  override protected def beforeEach() {
    IntegrationTestMutex.lock()
    super.beforeEach()
    MongoDatabaseManager.emptyDatabase()
  }

  override protected def afterEach() {
    super.afterEach()
    IntegrationTestMutex.unlock()
  }
}