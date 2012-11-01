package uk.gov.gds.common.repository

import com.mongodb.DBObject

trait Repository[A] {

  def safeInsert(obj: A): A

  def unsafeInsert(obj: A): A

  def findOne(filter: DBObject): Option[A]

  def load(id: String): Option[A]

  def load(ids: List[String]): Cursor[A]

  def delete(id: String): Unit

  def deleteAll(): Unit

  def all: Cursor[A]
}

