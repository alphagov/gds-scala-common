package uk.gov.gds.common.mongo.repository

import com.novus.salat.CaseClass
import uk.gov.gds.common.repository.HasTimestamp
import org.joda.time.DateTime

object DirectedQuery extends SyntacticSugarForMongoQueries {
  def gt(time: DateTime) = ("$gt" -> time)

  def lt(time: DateTime) = ("$lt" -> time)
}

abstract class TimestampBasedMongoRepository[A <: CaseClass with HasTimestamp](implicit m: Manifest[A])
  extends SimpleMongoRepository[A] {

  protected val databaseTimeStampProperty: String

  def load(filter: Seq[(String, Any)], timeQuery: (String, DateTime), sort: Order = Descending, pageSize: Int = 100) = {
    val q = filter ++ Seq((databaseTimeStampProperty, where(timeQuery)))
    SimpleMongoCursor(
      query(q:_*), order((databaseTimeStampProperty, sort.order)), pageSize
    )
  }

  @inline override def startup() {
    super.startup()
    createIndexes()
  }

  @inline override protected def createIndexes() {
    super.createIndexes()
    addIndex(index(databaseTimeStampProperty -> Ascending))
  }

}