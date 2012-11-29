package uk.gov.gds.common.audit

import uk.gov.gds.common.mongo.repository._
import scala.Some
import uk.gov.gds.common.repository.Cursor
import com.mongodb.casbah.commons.MongoDBObject

abstract class AuditEventRepositoryBase extends SimpleMongoRepository[AuditEvent] {

  private lazy val applicationName = Option(System.getProperty("gds.application.name")).getOrElse("not-configured")

  def audit(auditType: String, tags: Map[String, String] = Map.empty, detail: Map[String, String] = Map.empty) {
    audit(AuditEvent(
      auditType = auditType,
      tags = tags,
      detail = detail))
  }

  protected def audit(event: AuditEvent) {
    logger.info(event.toString)
    unsafeInsert(event.copy(tags = event.tags + ("applicationName" -> applicationName)))
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

  def findOne(auditType: String, tags: Map[String, String]): Option[AuditEvent] =
    findOne(filter = buildQuery(auditType = Some(auditType), tags = tags))

  def find(auditType: String): Cursor[AuditEvent] = find(Some(auditType))

  def find(auditType: String, tags: Map[String, String]): Cursor[AuditEvent] = find(Some(auditType), tags)

  def find(tags: Map[String, String]): Cursor[AuditEvent] = find(None, tags)

  private def find(auditType: Option[String] = None, tags: Map[String, String] = Map.empty): Cursor[AuditEvent] =
    SimpleMongoCursor(
      order = order("timestamp" -> -1),
      query = buildQuery(auditType, tags))

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