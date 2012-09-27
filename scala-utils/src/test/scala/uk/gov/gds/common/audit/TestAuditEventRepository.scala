package uk.gov.gds.common.audit

import uk.gov.gds.common.mongo.UnauthenticatedMongoDatabaseManagerForTests

object TestAuditEventRepository extends AuditEventRepositoryBase {
  protected val collection = UnauthenticatedMongoDatabaseManagerForTests("auditEvents")
}
