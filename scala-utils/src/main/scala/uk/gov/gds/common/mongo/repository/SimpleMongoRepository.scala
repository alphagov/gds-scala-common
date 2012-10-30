package uk.gov.gds.common.mongo.repository

import com.novus.salat._
import com.mongodb.casbah.Imports._
import com.mongodb.WriteConcern

abstract class SimpleMongoRepository[A <: CaseClass](implicit m: Manifest[A]) extends MongoRepositoryBase[A] {

  def load(id: String) = load(oid(id))

  def load(id: ObjectId) = findOne(where("_id" -> id))

  def load(ids: List[String]) = SimpleMongoCursor(where("_id" -> inOids(ids)))

  def get(id: String): A = get(oid(id))

  def get(id: ObjectId): A = load(id).getOrElse(throw new NoSuchObjectException(id))

  def safeInsert(obj: A) = try {
    insert(obj, WriteConcern.MAJORITY)
  } catch {
    case e: Exception =>
      logger.error("safeInsert failed for " + obj, e)
      throw e
  }

  def unsafeInsert(obj: A) = try {
    insert(obj, WriteConcern.NORMAL)
  }
  catch {
    case e: Exception =>
      logger.warn("unsafeInsert failed for " + obj, e)
      obj
  }

  def safeUpdate(query: DBObject, obj: DBObject, upsert: Boolean = true, multi: Boolean = false) = {
    try {
      collection.update(query, obj, upsert, multi, WriteConcern.MAJORITY)
    } catch {
      case e: Exception => {
        logger.error(e.getMessage)
        throw e
      }
    }
  }

  def unSafeUpdate(query: DBObject, obj: DBObject, upsert: Boolean = true, multi: Boolean = false) =
    collection.update(query, obj, upsert, multi, WriteConcern.NORMAL)

  def delete(id: String) {
    collection -= where("_id" -> oid(id))
  }

  def deleteAll() {
    collection.remove(query())
  }

  def all = SimpleMongoCursor()

  @inline def findOne(filter: DBObject) = collection.findOne(filter)

  @inline protected def findAll(filter: DBObject): List[A] = collection.find(filter)

  @inline private def update(query: DBObject, obj: DBObject, upsert: Boolean = true, multi: Boolean = false, writeConcern: WriteConcern) =
    collection.update(query, obj, upsert, multi, writeConcern)

  @inline private def insert(obj: A, writeConcern: WriteConcern) = {
    val query = domainObj2mongoObj(obj)
    collection.insert(query, writeConcern)
    grater[A].asObject(query)
  }
}
