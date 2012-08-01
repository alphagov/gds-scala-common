package uk.gov.gds.common.mongo.repository

import com.novus.salat._
import com.mongodb.casbah.Imports._
import play.api.Logger

abstract class SimpleMongoRepository[A <: CaseClass](implicit m: Manifest[A]) extends MongoRepositoryBase[A] {

  def store(obj: A) = {
    val query = domainObj2mongoObj(obj)
    collection += query
    grater[A].asObject(query)
  }

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