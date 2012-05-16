package uk.gov.gds.common.mongo.migration

import uk.gov.gds.common.mongo.repository.IdentityBasedMongoRepository
import uk.gov.gds.common.logging.Logging

abstract class ChangeLogRepository extends IdentityBasedMongoRepository[ChangeScriptAudit] with ChangeScripts with Logging {

  def applyChangeScripts() {
    changeScripts.foreach {
      changeScript =>
        val name = changeScript.getClass.getName

        logger.info("Applying change script " + name)
        changeScript.applyToDatabase()

        store(ChangeScriptAudit(changeScript))
    }
  }

  override def deleteAll() {
    logger.warn("Deleting ALL changescripts from repository. I hope you knew what you were doing!")
    super.deleteAll()
  }
}