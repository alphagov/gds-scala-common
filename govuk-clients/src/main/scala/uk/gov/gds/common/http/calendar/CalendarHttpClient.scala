package uk.gov.gds.common.http.calendar

import uk.gov.gds.common.http.ApacheHttpClient
import uk.gov.gds.common.config.Config

object CalendarHttpClient extends ApacheHttpClient {
  private[http] def targetUrl(path: String) = Config("calendar.api.url", "https://www.gov.uk/bank-holidays.json") + path
}
