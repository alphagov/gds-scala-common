package uk.gov.gds.common.mongo.migration

import org.joda.time.DateTime
import uk.gov.gds.common.repository.HasIdentity

object ChangeScriptStatus {
  val ok = "ok"
  val failed = "failed"
}

case class ChangeScriptAudit(name: String, runAt: DateTime, status: String = ChangeScriptStatus.ok) extends HasIdentity {
  def id = name
}

object SuccessfulChangeScriptAudit {
  def apply(changeScript: ChangeScript) = new ChangeScriptAudit(changeScript.name, DateTime.now)
}

object FailedChangeScriptAudit {
  def apply(changeScript: ChangeScript) = new ChangeScriptAudit(changeScript.name, DateTime.now(), ChangeScriptStatus.failed)
}

trait ChangeScript {

  final def name = getClass.getName

  final def shortName = getClass.getSimpleName

  def applyToDatabase(): Unit

  // Override in implementors if you want the migration to fail based on custom checks that you have written.
  // The framework will run applyToDatabase() first and then run checkHasBeenAppliedToDatabase
  // The default case assumes that if applyToDatabase does not throw an exception the script has been applied
  // and no further checks are required. Override if you want to change this behaviour
  def checkHasBeenAppliedToDatabase(): Boolean = true
}

class ChangeScriptFailedException extends RuntimeException