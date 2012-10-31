package uk.gov.gds.common.mongo.repository

import com.mongodb.casbah.commons.MongoDBObject.{ newBuilder => build }
import org.bson.types.ObjectId

sealed abstract class Order(val order: Int)
case object Ascending extends Order(1)
case object Descending extends Order(-1)

sealed abstract class IndexType(val index: Boolean)
case object Sparse extends IndexType(true)
case object Complete extends IndexType(false)

sealed abstract class Uniqueness(val uniqueness: Boolean)
case object Enforced extends Uniqueness(true)
case object Unenforced extends Uniqueness(false)

sealed abstract class Duplicate(val duplicate: Boolean)
case object Drop extends Duplicate(true)
case object Keep extends Duplicate(false)

trait SyntacticSugarForMongoQueries {

  protected implicit def order2int[A <: Order](o: A) = o.order

  protected implicit def indexType2bool(i: IndexType) = i.index

  protected implicit def uniqueness2bool(u: Uniqueness) = u.uniqueness

  protected implicit def duplicates2bool(d: Duplicate) = d.duplicate

  protected def where[A <: String, B <: Any](t: (A, B)*) = (build[A, B] ++= t).result

  protected def update[A <: String, B <: Any](t: (A, B)*) = (build[A, B] ++= t).result

  protected def values[A <: String, B <: Any](t: (A, B)*) = (build[A, B] ++= t).result

  protected def query[A <: String, B <: Any](t: (A, B)*) = (build[A, B] ++= t).result

  protected def order[A <: String, B <: Int](t: (A, B)*) = (build[A, B] ++= t).result

  protected def neq(a: String) = query("$ne" -> a)

  protected def in(ids: List[String]) = query("$in" -> ids)

  protected def inOids(ids: List[String]) = query("$in" -> ids.map(oid(_)))

  protected def oid(id: String) = new ObjectId(id)

  protected def index(t: (String, Int)*) = (build[String, Int] ++= t).result

}