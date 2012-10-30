package uk.gov.gds.common.repository

import uk.gov.gds.common.logging.Logging

trait Cursor[A] {
  def pageOfData: List[A]

  def pages: Int

  def currentPage: Long

  def total: Long

  def gotoNextPage(): Unit

  def hasNextPage: Boolean
}

abstract class CursorBase[A](var pageSize: Int,
                             var currentPage: Long)
  extends Cursor[A] with Logging {

  def pages = math.ceil(total.toDouble / pageSize.toDouble).toInt

  def gotoNextPage() = if (!hasNextPage) throw new EndOfCursorException else currentPage += 1

  def hasNextPage = (currentPage + 1) <= pages

  def foreach[B](f: (A) => B) {
    1.to(pages).foreach {
      _ =>
        pageOfData.foreach(f)
        advanceToNextPage()
    }
  }

  def map[B](f: (A) => B) = 1.to(pages).map {
    _ =>
      val results = pageOfData.map(f)
      advanceToNextPage()
      results
  }.flatten

  def toList: List[A] = map(x => x).toList

  protected def skipSize = ((currentPage - 1) * pageSize).toInt

  protected def logAndTimeQuery[B](logMessage: String, query: => B) = {
    logger.trace(logMessage)
    val startTimeInMillis = System.currentTimeMillis()
    val queryResult = query
    logger.debug(logMessage + " completed in " + (System.currentTimeMillis() - startTimeInMillis) + "ms")
    queryResult
  }

  private def advanceToNextPage() = if (hasNextPage) gotoNextPage()
}

class EndOfCursorException extends RuntimeException