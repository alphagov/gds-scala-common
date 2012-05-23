package uk.gov.gds.common.mongo.repository

import com.novus.salat.CaseClass
import uk.gov.gds.common.repository.HasTimestamp
import org.joda.time.DateTime
import com.mongodb.casbah.commons.MongoDBObject
import com.mongodb.DBObject

object DirectedQuery extends SyntacticSugarForMongoQueries {
  def gt(time: DateTime) = ("$gt" -> time)

  def lt(time: DateTime) = ("$lt" -> time)
}

abstract class TimestampBasedMongoRepository[A <: CaseClass with HasTimestamp](implicit m: Manifest[A])
  extends SimpleMongoRepository[A] {

  protected val databaseTimeStampProperty: String

  def load(q: (String, DateTime), sort: Order = Descending) = SimpleMongoCursor(
    query((databaseTimeStampProperty, where(q))), order((databaseTimeStampProperty, sort.order))
  )

  def load(filter:(String, Any), timeQuery: (String, DateTime)) = SimpleMongoCursor(
    query(filter, (databaseTimeStampProperty, where(timeQuery)))
  )

  @inline override def startup() {
    super.startup()
    createIndexes()
  }

  @inline override protected def createIndexes() {
    super.createIndexes()
    addIndex(index(databaseTimeStampProperty -> Ascending))
  }

}