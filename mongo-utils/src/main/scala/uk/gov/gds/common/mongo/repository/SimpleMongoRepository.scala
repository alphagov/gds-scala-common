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

  def insertWith(writeConcern: WriteConcern, obj: A) = try {
    insert(obj, writeConcern)
  } catch {
    case e: Exception => {
      logger.error("insert with concern " + writeConcern.toString + " failed for " + obj, e)
      throw e
    }
  }

  def bulkInsertWith(writeConcern: WriteConcern, obj: List[A]) = try {
    bulkInsert(obj, writeConcern)
  } catch {
    case e: Exception => {
      logger.error("insert with concern " + writeConcern.toString + " failed for " + obj, e)
      throw e
    }
  }

  def updateWith(writeConcern: WriteConcern, query: DBObject, obj: DBObject, upsert: Boolean, multi: Boolean) = {
    try {
      collection.update(query, obj, upsert, multi, writeConcern)
    } catch {
      case e: Exception => {
        logger.error(e.getMessage)
        throw e
      }
    }
  }

  def safeInsert(obj: A) = insertWith(WriteConcern.MAJORITY, obj)

  def unsafeInsert(obj: A) = insertWith(WriteConcern.NORMAL, obj)

  def safeUpdate(query: DBObject, obj: DBObject, upsert: Boolean = true, multi: Boolean = false) =
    updateWith(WriteConcern.MAJORITY, query, obj, upsert, multi)

  def unSafeUpdate(query: DBObject, obj: DBObject, upsert: Boolean = true, multi: Boolean = false) =
    updateWith(WriteConcern.NORMAL, query, obj, upsert, multi)

  def unsafeDelete(id: String) = unsafeDelete(where("_id" -> oid(id)))

  def safeDelete(id: String) = safeDelete(where("_id" -> oid(id)))

  def unsafeDelete(query: DBObject) = try {
    collection.remove(query, WriteConcern.NORMAL)
  } catch {
    case e =>
      logger.error("unsafeDelete failed for %s".format(query.toString), e)
      throw e
  }

  def safeDelete(query: DBObject) = try {
    collection.remove(query, WriteConcern.MAJORITY)
  } catch {
    case e =>
      logger.error("unsafeDelete failed for %s".format(query.toString), e)
      throw e
  }

  def deleteAll() {
    collection.remove(query())
  }

  def findAndModify(query: DBObject, update: DBObject) = try {
    collection.findAndModify(query = query, update = update, sort = null, fields = null, remove = false, returnNew = true, upsert = false)
  } catch {
    case e =>
      logger.error("findAndModify failed for %s %s".format(query.toString, update.toString), e)
      throw e
  }
  
  def all = SimpleMongoCursor()

  def allFields = MongoDBObject.empty

  def addFieldWith(writeConcern: WriteConcern, fieldName: String, defaultValue: String = "") = updateWith(writeConcern,
    allFields, update("$set" -> field(fieldName, defaultValue)), false, true)

  def removeFieldWith(writeConcern: WriteConcern, fieldName: String, defaultValue: String = "") = updateWith(writeConcern,
    allFields, update("$unset" -> field(fieldName, defaultValue)), false, true)

  def field(fieldName: String, defaultValue: String = "") = values(fieldName -> defaultValue)

  def addField(fieldName: String, defaultValue: String = "") = safeUpdate(allFields,
    update("$set" -> field(fieldName, defaultValue)), false, true)

  def removeField(fieldName: String, defaultValue: String = "") = safeUpdate(allFields,
    update("$unset" -> field(fieldName, defaultValue)), false, true)

  def findOne(filter: DBObject) = collection.findOne(filter)

  protected def findAll(filter: DBObject): List[A] = collection.find(filter)

  @inline private final def update(query: DBObject, obj: DBObject, upsert: Boolean = true, multi: Boolean = false, writeConcern: WriteConcern) =
    collection.update(query, obj, upsert, multi, writeConcern)

  @inline private final def insert(obj: A, writeConcern: WriteConcern) = {
    val query = domainObj2mongoObj(obj)
    collection.insert(query, writeConcern)
    grater[A].asObject(query)
  }

  @inline private final def bulkInsert(obj: List[A], writeConcern: WriteConcern) = {
    val query = domainList2MongoObj(obj)
    collection.insert(query, writeConcern)
    query.map(grater[A].asObject(_))
  }
}
