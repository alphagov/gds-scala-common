package uk.gov.gds.common.repository

trait Repository[A] {

  def store(obj: A): A

  def load(id: String): Option[A]

  def load(ids: List[String]): Cursor[A]

  def delete(id: String): Unit

  def deleteAll(): Unit

  def all: Cursor[A]
}

