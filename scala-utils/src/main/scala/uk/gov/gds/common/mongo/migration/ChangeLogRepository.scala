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

  def load(changeScript: ChangeScript): Option[ChangeScriptAudit] = load(changeScript.name)

  override def deleteAll() {
    logger.warn("Deleting ALL changescripts from repository. I hope you knew what you were doing!")
    super.deleteAll()
  }

  private def commitChangeScript(changeScript: ChangeScript) {
    try {
      logger.info("Applying change script " + changeScript.shortName)
      changeScript.applyToDatabase()
      store(SuccessfulChangeScriptAudit(changeScript))
    }
    catch {
      case e: Exception =>
        store(FailedChangeScriptAudit(changeScript))
        logger.error("Change script failed to apply " + changeScript.shortName, e)

        throw new ChangeScriptFailedException
    }
  }
}