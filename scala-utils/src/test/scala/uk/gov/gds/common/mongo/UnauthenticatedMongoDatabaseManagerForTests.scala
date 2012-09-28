package uk.gov.gds.common.mongo

import com.mongodb.WriteConcern
import uk.gov.gds.common.audit.TestAuditEventRepository
import com.mongodb.casbah.MongoDB

object UnauthenticatedMongoDatabaseManagerForTests extends MongoDatabaseManager {

  database.setWriteConcern(WriteConcern.MAJORITY)

  protected val repositoriesToInitialiseOnStartup = List(TestAuditEventRepository)

  protected override def authenticateToDatabaseIfRequired(connection: MongoDB) {
    // no-op. We're disabling authentication
  }
}
