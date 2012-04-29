package uk.gov.gds.common.pagination

trait PaginationSupport {

  def maxPage = 100

  def defaultPage = 1

  def minPage = defaultPage

  def maxPageSize = 500

  def defaultPageSize = 100

  def minPageSize = 0
}
