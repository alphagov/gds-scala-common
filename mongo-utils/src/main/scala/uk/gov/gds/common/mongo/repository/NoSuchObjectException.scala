package uk.gov.gds.common.mongo.repository

import org.bson.types.ObjectId

class NoSuchObjectException(val id: String) extends Exception {

  def this(id: ObjectId) = this(id.toString)

  override def toString = "NoSuchObjectException(" + id + ")"
}