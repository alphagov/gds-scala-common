package uk.gov.gds.common.mongo

import repository.MongoRepositoryBase
import uk.gov.gds.common.logging.Logging
import com.mongodb.ServerAddress
import uk.gov.gds.common.j2ee.ContainerEventListener
import uk.gov.gds.common.config.Config
import com.mongodb.casbah.{MongoDB, WriteConcern, MongoConnection}

abstract class MongoDatabaseManager extends ContainerEventListener with Logging {

  protected val repositoriesToInitialiseOnStartup: List[MongoRepositoryBase[_]]

  private lazy val databaseHosts = Config("mongo.database.hosts").split(",").toList
  private lazy val mongoConnection = MongoConnection(databaseHosts.map(new ServerAddress(_)))

  lazy val database: MongoDB = mongoConnection(databaseName)

  override def startup() {
    logger.info("Mongo startup")
    emptyDatabase() // TODO: remove when we have db loader / changelog / migrations
  }

  def apply(collectionName: String) = collection(collectionName)

  def collection(collectionName: String) = {
    val collection = database(collectionName)
    collection.slaveOk()
    collection
  }

  def emptyDatabase() {
    try {
      database.setWriteConcern(WriteConcern.Normal)
      database.dropDatabase()
    }
    finally {
      initialize()
    }
  }

  protected def databaseName = Config("mongo.database.name")

  private def initialize() {
    repositoriesToInitialiseOnStartup.foreach(_.startup())
    database.setWriteConcern(WriteConcern.Safe)
  }
}

