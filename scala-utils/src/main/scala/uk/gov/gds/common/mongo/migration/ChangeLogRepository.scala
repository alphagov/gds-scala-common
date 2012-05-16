package uk.gov.gds.common.mongo.migration

import uk.gov.gds.common.mongo.repository.IdentityBasedMongoRepository
import uk.gov.gds.common.logging.Logging

abstract class ChangeLogRepository
  extends IdentityBasedMongoRepository[ChangeScriptAudit]
  with ChangeScripts
  with Logging {

  def applyChangeScripts() {
    changeScripts.foreach(cs => load(cs).getOrElse(commitChangeScript(cs)))
  }

  def load(changeScript: ChangeScript): Option[ChangeScriptAudit] = load(changeScript.getClass.getName)

  override def deleteAll() {
    logger.warn("Deleting ALL changescripts from repository. I hope you knew what you were doing!")
    super.deleteAll()
  }

  private def commitChangeScript(changeScript: ChangeScript) {
    logger.info("Applying change script " + changeScript.getClass.getSimpleName)
    changeScript.applyToDatabase()
    store(ChangeScriptAudit(changeScript))
  }
}