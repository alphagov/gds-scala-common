package uk.gov.gds.common.audit

import uk.gov.gds.common.mongo.MongoDatabaseManagerForTests

object TestAuditEventRepository extends AuditEventRepositoryBase {
  protected val collection = MongoDatabaseManagerForTests("auditEvents")
}
