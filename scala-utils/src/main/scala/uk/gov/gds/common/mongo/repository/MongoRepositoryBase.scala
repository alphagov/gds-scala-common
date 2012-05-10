package uk.gov.gds.common.mongo.repository

import com.novus.salat._
import com.novus.salat.global.NoTypeHints
import com.mongodb.casbah.Imports._
import com.mongodb.casbah.commons.conversions.scala.RegisterJodaTimeConversionHelpers
import com.mongodb.casbah.MongoCollection
import uk.gov.gds.common.logging.Logging
import uk.gov.gds.common.j2ee.ContainerEventListener
import uk.gov.gds.common.pagination.PaginationSupport
import uk.gov.gds.common.repository.{CursorBase, Repository}

abstract class MongoRepositoryBase[A <: CaseClass](implicit m: Manifest[A])
  extends Repository[A]
  with Logging
  with ContainerEventListener
  with SyntacticSugarForMongoQueries
  with PaginationSupport {

  RegisterJodaTimeConversionHelpers()

  protected val collection: MongoCollection
  protected implicit val ctx = NoTypeHints
  protected lazy val emptyQuery = MongoDBObject()

  protected implicit def domainObj2mongoObj(o: A) = grater[A].asDBObject(o)

  protected implicit def mongoOptObj2DomainObj(o: Option[collection.T]) = o.map(grater[A].asObject(_))

  protected implicit def mongoObj2DomianObj(o: collection.T) = grater[A].asObject(o)

  protected implicit def cursorOfMongoObject2listOfDomainObjects(c: collection.CursorType) = c.map(grater[A].asObject(_)).toList

  protected def createIndexes() {}

  protected def createReferenceData() {}

  override def startup() {
    createIndexes()
    createReferenceData()
  }

  protected def addIndex(index: DBObject,
                         unique: Boolean = Enforced,
                         sparse: Boolean = Sparse) {
    logger.info("Adding index " + index)

    try {
      collection.underlying.ensureIndex(
        index,
        query(
          "unique" -> unique,
          "background" -> false,
          "sparse" -> sparse))
    }
    catch {
      case e: Exception =>
        logger.error("Could not create index " + index, e)
        throw e
    }
  }

  protected class SimpleMongoCursor(query: DBObject,
                                    pageSize: Int,
                                    currentPage: Long,
                                    order: Option[MongoDBObject] = None)
    extends CursorBase[A](pageSize, currentPage) {

    def pageOfData = logAndTimeQuery[List[A]](
      logMessage = "Mongo query: " + query + " with page:" + currentPage + " skip:" + skipSize + " page-size:" + pageSize + " sort order " + order,
      query = order match {
        case Some(direction) => collection.find(query).sort(direction).skip(skipSize).limit(pageSize)
        case _ => collection.find(query).skip(skipSize).limit(pageSize)
      }
    )

    def total = logAndTimeQuery(
      logMessage = "Mongo count: " + collection.name,
      query = collection.count(query)
    )
  }

  protected object SimpleMongoCursor {

    def apply(query: DBObject) = buildCursor(query)

    def apply(query: DBObject, order: DBObject) = buildCursor(query = query, order = Some(order))

    def apply(query: DBObject, page: Int, pageSize: Int) = buildCursor(
      query = query,
      pageSize = pageSize
    )

    def apply(query: DBObject, page: Int, pageSize: Int, order: MongoDBObject) = buildCursor(
      query = query,
      pageSize = pageSize,
      order = Some(order)
    )

    def apply(pageSize: Int = defaultPageSize) = buildCursor(
      query = emptyQuery,
      pageSize = pageSize
    )

    private def buildCursor(query: DBObject,
                            pageSize: Int = defaultPageSize,
                            currentPage: Int = 1,
                            order: Option[MongoDBObject] = None) =
      new SimpleMongoCursor(
        query = query,
        pageSize = pageSize,
        currentPage = currentPage,
        order = order)
  }

}