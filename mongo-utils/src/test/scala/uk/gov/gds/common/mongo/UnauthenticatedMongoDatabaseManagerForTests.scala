package uk.gov.gds.common.mongo

import com.mongodb.WriteConcern
import uk.gov.gds.common.audit.TestAuditEventRepository
import com.mongodb.casbah.MongoDB
import com.mongodb.Bytes

object UnauthenticatedMongoDatabaseManagerForTests extends MongoDatabaseManager {

  database.setWriteConcern(WriteConcern.MAJORITY)
  database.underlying.setOptions(database.getOptions & (~Bytes.QUERYOPTION_SLAVEOK))

  protected val repositoriesToInitialiseOnStartup = List(TestAuditEventRepository)

  override def shouldAuthenticate = {
    false
  }
}
