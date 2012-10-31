package uk.gov.gds.common.mongo.repository

import com.novus.salat.CaseClass
import uk.gov.gds.common.repository.HasTimestamp
import org.joda.time.DateTime
import com.mongodb.casbah.Imports._

abstract class TimestampBasedMongoRepository[A <: CaseClass with HasTimestamp](implicit m: Manifest[A])
  extends SimpleMongoRepository[A] {

  protected val databaseTimeStampProperty: String

  def load(filter: DBObject, timeQuery: DBObject, sort: Order = Descending, pageSize: Int = 100) = {
    filter.putAll(timeQuery)
    SimpleMongoCursor(filter, order((databaseTimeStampProperty, sort.order)), pageSize)
  }

  def gt(time: DateTime) = where(databaseTimeStampProperty -> query("$gt" -> time))

  def lt(time: DateTime) = where(databaseTimeStampProperty -> query("$lt" -> time))

  @inline override def startup() {
    super.startup()
    createIndexes()
  }

  @inline override protected def createIndexes() {
    super.createIndexes()
    addIndex(index(databaseTimeStampProperty -> Ascending), unique = Unenforced)
  }

}