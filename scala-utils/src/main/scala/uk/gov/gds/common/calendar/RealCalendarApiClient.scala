package uk.gov.gds.common.calendar

import com.google.common.cache.CacheBuilder
import java.util.concurrent.TimeUnit
import com.google.common.cache.CacheLoader

object RealCalendarApiClient extends CalendarApiClient {

  private val cache = CacheBuilder
    .newBuilder()
    .expireAfterWrite(1, TimeUnit.DAYS)
    .build(new CacheLoader[String, String] {
      def load(key: String) = CalendarHttpClient.get("")
    })

  def getBankHolidays = cache.get("")
}