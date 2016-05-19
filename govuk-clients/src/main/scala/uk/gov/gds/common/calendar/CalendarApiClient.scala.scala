package uk.gov.gds.common.calendar

import org.joda.time.LocalDate
import uk.gov.gds.common.logging.Logging
import uk.gov.gds.common.json.JsonSerializer._
import com.fasterxml.jackson.annotation.JsonIgnoreProperties

trait CalendarApiClient {
  def getBankHolidays: String
}

case class EventMap(division: String, calendars: Map[String, Year])

case class Year(year: Int, division: String, events: List[Event])

case class Event(title: String, date: String, notes: String)

object CalendarApiClient extends Logging {

  private lazy val client = {
    logger.info("Initialising CalendarApiClient MODE= " + System.getProperty("MODE", "PROD_MODE") +
      " Should be one of (CACHED_MODE or PROD_MODE)")
    System.getProperty("MODE", "PROD_MODE") match {
      case "CACHED_MODE" => MockCalendarApiClient
      //  case "PROD_MODE" => RealCalendarApiClient
      case "PROD_MODE" => MockCalendarApiClient
      case _ => MockCalendarApiClient
    }
  }

  def isBankHoliday(division: String, date: LocalDate): Boolean =
    getEvents(division.toLowerCase, date.getYear).find(_.date == date.toString).isDefined

  def getBankHolidays(division: String, year: Int) =
    getEvents(division.toLowerCase, year).map(event => LocalDate.parse(event.date))

  private def getEvents(division: String, year: Int) = {
    val divisionCalendars = fromJson[Map[String, EventMap]](client.getBankHolidays)
    val divisionUpdated =
      if ((division == "england" || division == "wales") && !divisionCalendars.contains(division))
        "england-and-wales"
      else
        division.toLowerCase

    val eventsOption = for {
      divisionObj <- divisionCalendars.get(divisionUpdated);
      yearObj <- divisionObj.calendars.get(year.toString)
    } yield {
      yearObj.events
    }
    eventsOption.getOrElse(Nil)
  }
}
