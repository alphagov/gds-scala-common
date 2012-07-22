package uk.gov.gds.common.calendar

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import uk.gov.gds.common.logging.Logging
import org.joda.time.LocalDate

class CalendarTests extends FunSuite
with ShouldMatchers
with Logging {

  test("2012-07-04 shouldn't be a holiday in England, but 2013-01-01 should be one in N.I.") {
    CalendarApiClient.isBankHoliday("england", LocalDate.parse("2012-07-04")) should be(false)
    CalendarApiClient.isBankHoliday("ni", LocalDate.parse("2013-01-01")) should be(true)
  }

  test("2012-01-02 shouldn be a holiday in Wales, but 2012-07-12 shouldn't be one in Scotland") {
    CalendarApiClient.getBankHolidays("wales", 2012) should contain(LocalDate.parse("2012-01-02"))
    CalendarApiClient.getBankHolidays("Scotland", 2012) should not contain (LocalDate.parse("2012-07-12"))
    CalendarApiClient.getBankHolidays("ni", 2012) should contain(LocalDate.parse("2012-07-12"))
  }

}
