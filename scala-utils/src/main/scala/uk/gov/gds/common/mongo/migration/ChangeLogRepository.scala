package uk.gov.gds.common.mongo.migration

import uk.gov.gds.common.mongo.repository.IdentityBasedMongoRepository
import uk.gov.gds.common.logging.Logging
import uk.gov.gds.common.mongo.MongoDatabaseManager

abstract class ChangeLogRepository(databaseManager: MongoDatabaseManager)
  extends IdentityBasedMongoRepository[ChangeScriptAudit]
  with Logging {

  def databaseChangeScripts: List[ChangeScript]

  protected lazy val collection = databaseManager("changelog")
  protected val databaseIdProperty = "name"

  startup()

  override def startup() {
    super.startup()
    databaseChangeScripts.foreach(applyChangeScript(_))
  }

  override def deleteAll() {
    logger.warn("Deleting ALL changescripts from repository. I hope you knew what you were doing!")
    super.deleteAll()
  }

  def appliedChangeScripts = all

  def changeScriptAuditFor(changeScriptName: String) = load(changeScriptName)

  def changeScriptAuditFor(changeScript: ChangeScript) = load(changeScript.name)

  private def applyChangeScript(changeScript: ChangeScript) {
    changeScriptAuditFor(changeScript) match {
      case Some(audit) if (ChangeScriptStatus.ok.equals(audit.status)) =>
        logger.debug("Change script " + changeScript.name + " has already been applied")

      case _ => {
        logger.info("Applying change script " + changeScript.name)

        try {
          changeScript.applyToDatabase()
          safeInsert(SuccessfulChangeScriptAudit(changeScript))
        }
        catch {
          case e: Exception =>
            safeInsert(FailedChangeScriptAudit(changeScript))
            logger.error("Change script failed to apply " + changeScript.shortName, e)

            throw new ChangeScriptFailedException(
              "Change script failed to apply " + changeScript.shortName + " [" + e.getMessage + "]", e)
        }
      }
    }
  }
}