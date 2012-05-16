package uk.gov.gds.common.mongo.migration

import uk.gov.gds.common.mongo.repository.IdentityBasedMongoRepository
import uk.gov.gds.common.logging.Logging

abstract class ChangeLogRepository
  extends IdentityBasedMongoRepository[ChangeScriptAudit]
  with ChangeScripts
  with Logging {

  def applyChangeScripts() {
    changeScripts.foreach(s => load(s).getOrElse(commitChangeScript(s)))
  }

  def load(changeScript: ChangeScript): Option[ChangeScriptAudit] = load(changeScript.getClass.getName)

  override def deleteAll() {
    logger.warn("Deleting ALL changescripts from repository. I hope you knew what you were doing!")
    super.deleteAll()
  }

  private def commitChangeScript(changeScript: ChangeScript) {
    logger.info("Applying change script " + changeScript.getClass.getSimpleName)

    try {
      changeScript.applyToDatabase()
      store(ChangeScriptAudit(changeScript))
    }
    catch {
      case e: Exception =>
        logger.error("Change script failed to apply " + changeScript.getClass.getName, e)
        throw new ChangeScriptFailedException
    }
  }
}