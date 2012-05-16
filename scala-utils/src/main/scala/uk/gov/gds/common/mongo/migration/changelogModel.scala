package uk.gov.gds.common.mongo.migration

import org.joda.time.DateTime
import uk.gov.gds.common.repository.HasIdentity

case class ChangeScriptAudit(name: String, runAt: DateTime) extends HasIdentity {
  def id = name
}

object ChangeScriptAudit {
  def apply(changeScript: ChangeScript) = new ChangeScriptAudit(changeScript.getClass.getName, DateTime.now)
}
