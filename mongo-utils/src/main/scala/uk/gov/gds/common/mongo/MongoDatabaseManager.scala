package uk.gov.gds.common.mongo

import migration._
import repository.{ IdentityBasedMongoRepository, MongoRepositoryBase }
import uk.gov.gds.common.logging.Logging
import uk.gov.gds.common.config.Config
import com.mongodb.casbah.{ MongoDB, MongoConnection }
import com.mongodb.{ Bytes, WriteConcern, ServerAddress }
import com.mongodb.WriteConcern.{ NORMAL, SAFE }
import util.DynamicVariable

abstract class MongoDatabaseManager extends Logging {

  lazy val database: MongoDB = {
    logger.info("Connecting to database: " + databaseName)

    try {
      val conn = mongoConnection(databaseName)
      authenticateToDatabaseIfRequired(conn)
      conn
    } catch {
      case e: Exception =>
        logger.error("Failure initialising & authenticating to mongoDB: " + e.getMessage(), e)
        throw e
    }
  }

  val changeLogRepository = new ChangeLogRepository(this)

  protected def databaseUsername = Config("mongo.database.auth.username")

  protected def databasePasssword = Config("mongo.database.auth.password")

  def shouldAuthenticate = {
    try {
      databaseUsername != null
    } catch {
      case _ => false
    }
  }

  protected def authenticateToDatabaseIfRequired(connection: MongoDB) {
    /* authenticate if a username is set in the config file */
    if (shouldAuthenticate) {
      logger.info("Attempting to authenticate as user:" + databaseUsername)

      connection.authenticate(databaseUsername, databasePasssword)
    } else {
      logger.info("No database authentication configured")
    }
  }

  protected lazy val databaseHosts = {
    val databaseHostString = Config("mongo.database.hosts")
    logger.info("Mongo Database Hosts: " + databaseHostString)
    databaseHostString.split(",").toList
  }

  private lazy val mongoConnection = MongoConnection(databaseHosts.map(new ServerAddress(_)))

  if (MongoConfig.slaveOk) {
    logger.info("Setting database to slaveOk mode. Will read from slaves")
    database.slaveOk()
  } else {
    logger.info("Not Setting database to slaveOk mode. Will only read & write from master")
    database.underlying.setOptions(database.getOptions() & (~Bytes.QUERYOPTION_SLAVEOK))
  }

  protected val repositoriesToInitialiseOnStartup: List[MongoRepositoryBase[_]]

  def databaseChangeScripts: List[ChangeScript] = Nil

  protected def databaseName = Config("mongo.database.name")

  def apply(collectionName: String) = collection(collectionName)

  def collection(collectionName: String) = database(collectionName)

  def initializeDatabase(writeConcern: WriteConcern = SAFE) {
    synchronized {
      withWriteConcern(writeConcern) {
        initialiseRepositories()
        databaseChangeScripts.foreach(applyChangeScript(_))
      }
    }
  }

  def emptyDatabase(writeConcern: WriteConcern = NORMAL) {
    synchronized {
      withWriteConcern(writeConcern) {
        repositoriesToInitialiseOnStartup.foreach(_.deleteAll())
        changeLogRepository.deleteAll()
        initializeDatabase()
      }
    }
  }

  def appliedChangeScripts = changeLogRepository.all

  def changeScriptAuditFor(changeScriptName: String) = changeLogRepository.load(changeScriptName)

  def changeScriptAuditFor(changeScript: ChangeScript) = changeLogRepository.load(changeScript.name)

  private def initialiseRepositories() {
    changeLogRepository.startup()

    repositoriesToInitialiseOnStartup.foreach {
      repository =>
        logger.info("Initialising repository " + repository.getClass.getSimpleName)
        repository.startup()
    }
  }

  private def withWriteConcern(writeConcern: WriteConcern)(block: => Unit) = {
    val currentWriteConcern = database.getWriteConcern

    try {
      database.setWriteConcern(writeConcern)
      block
    } finally {
      database.setWriteConcern(currentWriteConcern)
    }
  }

  private def applyChangeScript(changeScript: ChangeScript) {
    changeScriptAuditFor(changeScript) match {
      case Some(audit) if (ChangeScriptStatus.ok.equals(audit.status)) =>
        logger.debug("Change script " + changeScript.name + " has already been applied")

      case _ => {
        logger.info("Applying change script " + changeScript.name)

        try {
          changeScript.applyToDatabase()
          changeLogRepository.safeInsert(SuccessfulChangeScriptAudit(changeScript))
        } catch {
          case e: Exception =>
            changeLogRepository.safeInsert(FailedChangeScriptAudit(changeScript))
            logger.error("Change script failed to apply " + changeScript.shortName, e)

            throw new ChangeScriptFailedException(
              "Change script failed to apply " + changeScript.shortName + " [" + e.getMessage + "]", e
            )
        }
      }
    }
  }

  class ChangeLogRepository(databaseManager: MongoDatabaseManager)
      extends IdentityBasedMongoRepository[ChangeScriptAudit]
      with Logging {

    protected val collection = databaseManager("changelog")
    protected val databaseIdProperty = "name"

    override def deleteAll() {
      logger.warn("Deleting ALL changescripts from repository. I hope you knew what you were doing!")
      super.deleteAll()
    }
  }

}

