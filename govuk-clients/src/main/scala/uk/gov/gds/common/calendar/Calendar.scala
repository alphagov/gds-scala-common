package uk.gov.gds.common.calendar

import org.joda.time.{ LocalDate, DateTime }

object Calendar {

  def plusDays(division: String, date: DateTime, daysAhead: Int, onlyWorkingDays: Boolean) =
    if (onlyWorkingDays)
      plusDaysWithHolidays(division, date, daysAhead)
    else
      date.plusDays(daysAhead)

  def plusDaysWithHolidays(division: String, date: DateTime, daysAhead: Int) = {
    val localDate = date.toLocalDate
    val weeksAhead = daysAhead / 5
    var finalDate = getNextMondayIfWeekend(localDate).plusWeeks(weeksAhead)
    var bankHolidays = CalendarApiClient.getBankHolidays(division, localDate.getYear).filter(_.compareTo(localDate) >= 0) ++
      (localDate.getYear + 1 to finalDate.getYear).flatMap(year => CalendarApiClient.getBankHolidays(division, year))

    var daysToCount = (daysAhead % 5) + bankHolidays.count(_.compareTo(finalDate) <= 0)

    while (daysToCount > 0) {
      finalDate = finalDate.plusDays(1)
      if (finalDate.getDayOfYear == 1) bankHolidays ++= CalendarApiClient.getBankHolidays(division, finalDate.getYear)
      if (!bankHolidays.contains(finalDate) && finalDate.getDayOfWeek < 6) daysToCount -= 1
    }

    finalDate.toDateTime(date.toLocalTime)
  }

  private def getNextMondayIfWeekend(date: LocalDate) =
    date.plusDays(date.getDayOfWeek match {
      case 6 => 2
      case 7 => 1
      case _ => 0
    })

}
