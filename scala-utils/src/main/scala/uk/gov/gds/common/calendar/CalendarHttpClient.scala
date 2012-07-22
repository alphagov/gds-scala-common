package uk.gov.gds.common.calendar

import uk.gov.gds.common.http.ApacheHttpClient
import uk.gov.gds.common.config.Config

object CalendarHttpClient extends ApacheHttpClient {
  @inline protected def targetUrl(path: String) = Config("calendar.api.url", "https://www.gov.uk/bank-holidays.json") + path
}
