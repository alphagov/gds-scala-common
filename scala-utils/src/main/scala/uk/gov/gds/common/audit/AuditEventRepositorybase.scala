package uk.gov.gds.common.audit

import uk.gov.gds.common.mongo.repository._
import scala.Some
import uk.gov.gds.common.repository.Cursor
import com.mongodb.casbah.commons.MongoDBObject

abstract class AuditEventRepositoryBase extends SimpleMongoRepository[AuditEvent] {

  def apply(event: AuditEvent) {
    logger.info(event.toString)
    super.unsafeInsert(event)
  }

  override protected def createIndexes() {
    super.createIndexes()

    addIndex(index(
      "auditType" -> Ascending,
      "uniqueTagIds" -> Ascending,
      "timestamp" -> Descending),

      unique = Unenforced,
      sparse = Complete)

    addIndex(index("uniqueTagIds" -> Ascending), unique = Unenforced, sparse = Complete)
    addIndex(index("auditType" -> Ascending), unique = Unenforced, sparse = Complete)
    addIndex(index("timestamp" -> Descending), unique = Unenforced, sparse = Complete)
  }

  def find(auditType: String): Cursor[AuditEvent] = find(Some(auditType))

  def find(auditType: String, tags: Map[String, String]): Cursor[AuditEvent] = find(Some(auditType), tags)

  def find(tags: Map[String, String]): Cursor[AuditEvent] = find(None, tags)

  private def find(auditType: Option[String] = None, tags: Map[String, String] = Map.empty): Cursor[AuditEvent] =
    SimpleMongoCursor(
      order = order("timestamp" -> -1),
      query = buildQuery(auditType, tags)
    )

  private def buildQuery(auditType: Option[String], tags: Map[String, String]) = {
    val builder = MongoDBObject.newBuilder

    if (tags.size > 0)
      builder += "uniqueTagIds" -> query("$all" -> buildTagList(tags))

    if (!auditType.isEmpty)
      builder += "auditType" -> auditType.get

    builder.result
  }

  private def buildTagList(tags: Map[String, String]) = tags.map {
    case (key, value) => key + ":" + value
  }
}
