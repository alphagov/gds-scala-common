package uk.gov.gds.common.mongo.repository

import com.mongodb.casbah.commons.MongoDBObject.{ newBuilder => build }
import org.bson.types.ObjectId

import scala.language.implicitConversions

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
  self =>

  protected implicit def order2int[A <: Order](o: A) = o.order

  protected implicit def indexType2bool(i: IndexType) = i.index

  protected implicit def uniqueness2bool(u: Uniqueness) = u.uniqueness

  protected implicit def duplicates2bool(d: Duplicate) = d.duplicate

  @inline protected final def where[A <: String, B <: Any](t: (A, B)*) = (build[A, B] ++= t).result

  @inline protected final def update[A <: String, B <: Any](t: (A, B)*) = (build[A, B] ++= t).result

  @inline protected final def values[A <: String, B <: Any](t: (A, B)*) = (build[A, B] ++= t).result

  @inline protected final def query[A <: String, B <: Any](t: (A, B)*) = (build[A, B] ++= t).result

  @inline protected final def order[A <: String, B <: Int](t: (A, B)*) = (build[A, B] ++= t).result

  @inline protected final def neq(a: String) = query("$ne" -> a)

  @inline protected final def in(ids: List[String]) = query("$in" -> ids)

  @inline protected final def inOids(ids: List[String]) = query("$in" -> ids.map(oid(_)))

  @inline protected final def oid(id: String) = new ObjectId(id)

  @inline protected final def index(t: (String, Int)*) = (build[String, Int] ++= t).result

}