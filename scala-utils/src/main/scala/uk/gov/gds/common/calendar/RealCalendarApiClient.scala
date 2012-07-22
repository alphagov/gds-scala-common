package uk.gov.gds.common.calendar

object RealCalendarApiClient extends CalendarApiClient {
  def getBankHolidays = CalendarHttpClient.get("")
}