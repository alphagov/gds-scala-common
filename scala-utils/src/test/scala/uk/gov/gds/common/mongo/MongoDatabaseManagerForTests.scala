package uk.gov.gds.common.mongo

import com.mongodb.WriteConcern
import uk.gov.gds.common.audit.TestAuditEventRepository

object MongoDatabaseManagerForTests extends MongoDatabaseManager {

  database.setWriteConcern(WriteConcern.MAJORITY)

  protected val repositoriesToInitialiseOnStartup = List(TestAuditEventRepository)
}
