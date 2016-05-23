package uk.gov.gds.common.audit

import com.mongodb.casbah.commons.MongoDBObject
import org.joda.time.DateTime
import uk.gov.gds.common.mongo.repository._
import uk.gov.gds.common.repository.Cursor

abstract class AuditEventRepositoryBase extends SimpleMongoRepository[AuditEvent] {

  private lazy val applicationName = Option(System.getProperty("gds.application.name")).getOrElse("not-configured")

  def audit(auditType: String, tags: Map[String, String] = Map.empty, detail: Map[String, String] = Map.empty) {
    audit(AuditEvent(
      auditType = auditType,
      tags = tags,
      detail = detail
    ))
  }

  protected def audit(event: AuditEvent) {
    logger.info(event.toString)
    unsafeInsert(event.copy(tags = event.tags + ("applicationName" -> applicationName)))
  }

  override protected def createIndexes() {
    super.createIndexes()

    addIndex(
      index(
        "auditType" -> Ascending,
        "uniqueTagIds" -> Ascending,
        "timestamp" -> Descending
      ),

      unique = Unenforced,
      sparse = Complete
    )

    addIndex(index("uniqueTagIds" -> Ascending), unique = Unenforced, sparse = Complete)
    addIndex(index("auditType" -> Ascending), unique = Unenforced, sparse = Complete)
    addIndex(index("timestamp" -> Descending), unique = Unenforced, sparse = Complete)
  }

  def findOne(auditType: String, tags: Map[String, String]): Option[AuditEvent] =
    findOne(filter = buildQuery(auditType = Some(auditType), tags = tags))

  def find(auditType: String): Cursor[AuditEvent] = find(Some(auditType))

  def find(
    auditType: String,
    tags: Map[String, String]
  ): Cursor[AuditEvent] =
    find(Some(auditType), tags, None, None)

  def find(
    auditType: String,
    tags: Map[String, String],
    after: Option[DateTime],
    before: Option[DateTime]
  ): Cursor[AuditEvent] =
    find(Some(auditType), tags, after, before)

  def find(tags: Map[String, String]): Cursor[AuditEvent] = find(None, tags)

  private def find(
    auditType: Option[String] = None,
    tags: Map[String, String] = Map.empty,
    after: Option[DateTime] = None,
    before: Option[DateTime] = None
  ): Cursor[AuditEvent] =
    SimpleMongoCursor(
      order = order("timestamp" -> -1),
      query = buildQuery(auditType, tags, after, before)
    )

  private def buildQuery(
    auditType: Option[String],
    tags: Map[String, String],
    after: Option[DateTime] = None,
    before: Option[DateTime] = None
  ) = {
    val builder = MongoDBObject.newBuilder

    if (tags.nonEmpty)
      builder += "uniqueTagIds" -> query("$all" -> buildTagList(tags))

    if (auditType.isDefined)
      builder += "auditType" -> auditType.get

    val timeRange = List(after.map("$gte" -> _), before.map("$lte" -> _)).flatten
    if (timeRange.nonEmpty)
      builder += ("timestamp" -> query(timeRange: _*))

    builder.result
  }

  private def buildTagList(tags: Map[String, String]) = tags.map {
    case (key, value) => key + ":" + value
  }
}
