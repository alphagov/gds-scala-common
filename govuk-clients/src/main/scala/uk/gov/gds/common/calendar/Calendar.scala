package uk.gov.gds.common.calendar

import org.joda.time.DateTime
import org.joda.time.LocalDateTime

object Calendar {

  def plusDays(division: String, date: DateTime, daysAhead: Int, onlyWorkingDays: Boolean): DateTime =
    if (onlyWorkingDays)
      plusDaysWithHolidays(division, date, daysAhead).toDateTime
    else
      date.plusDays(daysAhead)

  def plusDaysWithHolidays(division: String, date: DateTime, daysAhead: Int): LocalDateTime = {
    val localDate = date.toLocalDateTime
    plusDaysWithHolidays(division, localDate, daysAhead)
  }

  def plusDaysWithHolidays(division: String, date: LocalDateTime, daysAhead: Int, first: Boolean = true): LocalDateTime = {
    if (daysAhead == 0) {
      date
    } else {
      val rollDays = daysAhead % 5
      val weeksAhead = (daysAhead - rollDays) / 5

      val noHolidayFinalDate = rollDateIncludingWeekend(date, rollDays).plusWeeks(weeksAhead)

      val holidays = for {
        year <- date.getYear to noHolidayFinalDate.getYear
        holiday <- CalendarApiClient.getBankHolidays(division, year)
        if ((first && holiday.compareTo(date.toLocalDate) >= 0) || holiday.compareTo(date.toLocalDate) > 0)
        if (holiday.compareTo(noHolidayFinalDate.toLocalDate) <= 0)
      } yield holiday
      plusDaysWithHolidays(division, noHolidayFinalDate, holidays.size, false)
    }
  }

  private def rollDateIncludingWeekend(date: LocalDateTime, rollDays: Int): LocalDateTime = {
    if (rollDays == 0) {
      date.plusDays(date.getDayOfWeek match {
        case 6 => 2
        case 7 => 1
        case _ => 0
      })
    } else {
      val rolled = date.plusDays(date.getDayOfWeek match {
        case 5 => 3
        case 6 => 3
        case 7 => 2
        case _ => 1
      })

      rollDateIncludingWeekend(rolled, rollDays - 1)
    }
  }

}
