package uk.gov.gds.common.audit

import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

case class AuditEvent(
    auditType: String,
    uniqueTagIds: List[String],
    tags: Map[String, String],
    detail: Map[String, String],
    hostname: String,
    timestamp: DateTime
) {

  override def toString = {
    val nicelyFormattedTags = tags.map(item => item._1 + ": " + item._2).mkString(", ")
    val nicelyFormattedDetail = detail.map(item => item._1 + ": " + item._2).mkString(", ")

    val output = "[AuditEvent | Type: " +
      auditType + " | Tags: " +
      nicelyFormattedTags + " | Detail: " +
      nicelyFormattedDetail + " | Hostname: " +
      hostname + " | Timestamp: " +
      timestamp.toString(DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")) + "]"

    output
  }
}

object AuditEvent {

  def apply(
    auditType: String,
    tags: Map[String, String] = Map.empty,
    detail: Map[String, String] = Map.empty,
    hostname: String = java.net.InetAddress.getLocalHost.getHostName,
    timestamp: DateTime = DateTime.now
  ) =
    new AuditEvent(
      auditType = auditType,
      detail = detail,
      hostname = hostname,
      timestamp = timestamp,
      tags = tags,
      uniqueTagIds = tags.map {
        case (key, value) => key + ":" + value
      }.toList
    )
}
