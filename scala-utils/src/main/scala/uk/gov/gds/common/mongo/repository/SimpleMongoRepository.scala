package uk.gov.gds.common.mongo.repository

import com.novus.salat._
import com.mongodb.casbah.Imports._
import com.mongodb.WriteConcern

abstract class SimpleMongoRepository[A <: CaseClass](implicit m: Manifest[A]) extends MongoRepositoryBase[A] {

  private def insert(obj: A, writeConcern: WriteConcern) = {
    val query = domainObj2mongoObj(obj)
    collection.insert(obj, writeConcern)
    grater[A].asObject(query)
  }

  def safeInsert(obj: A) = {
    try {
      insert(obj, WriteConcern.MAJORITY)
    } catch {
      case e: Exception => {
        logger.error(e.getMessage)
        throw e
      }
    }
  }

  def unsafeInsert(obj: A) = insert(obj, WriteConcern.NORMAL)

  def findOne(filter: DBObject) = collection.findOne(filter)

  def load(id: String) = collection.findOne(where("_id" -> oid(id)))

  def load(ids: List[String]) = SimpleMongoCursor(where("_id" -> inOids(ids)))

  def delete(id: String) {
    collection -= where("_id" -> oid(id))
  }

  def deleteAll() {
    collection.remove(query())
  }

  def all = SimpleMongoCursor()
}