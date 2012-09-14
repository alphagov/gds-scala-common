package uk.gov.gds.common.placesclient.model

import uk.gov.gds.common.repository.HasIdentity
import uk.gov.gds.common.localauth.Authority

// TODO: flatten model together. There should not be an ELMS model and a default model
// TODO: Standardise on one type of address. There should not be an Address and a GovUkAddress
// TODO: kill the ERO class, we're not using it


case class OpcsId(value: String)

object OpcsId {

  val opcsIdRegex = "[0-9]{2}[A-Z][A-Z]?".r

  def isValid(value: String) = value.matches(opcsIdRegex.toString())
}

case class Ero(address: Option[GovUkAddress] = None, telephoneNumber: String = "")

case class LocalAuthority(name: String = "",
                          ero: Ero = Ero(),
                          opcsId: String,
                          gssId: String = "") extends HasIdentity {
  def id = opcsId
}

object LocalAuthority {
  def apply(a: Authority) = new LocalAuthority(name = a.name, ero = Ero(), opcsId = a.snacCode.get, gssId = "")
}

case class GovUkAddress(lineOne: String = "",
                        lineTwo: String = "",
                        lineThree: String = "",
                        lineFour: String = "",
                        lineFive: String = "",
                        city: String = "",
                        postcode: String = "",
                        county: String = "",
                        uprn: Option[String] = None) {

  def shortString = if (lineOne.length() > 0)
    lineOne + " " + postcode
  else
    ""

  def asString = List(lineOne, lineTwo, lineThree, lineFour, lineFive, city, postcode)
    .filterNot(_.isEmpty).mkString(", ")

  def asShortString = List(lineOne, lineTwo, lineThree, lineFour, lineFive)
    .filterNot(_.isEmpty).reduceLeft(_ + ", " + _)
}

case class Address(lineOne: String = "",
                   lineTwo: String = "",
                   lineThree: String = "",
                   lineFour: String = "",
                   lineFive: String = "",
                   city: String = "",
                   postcode: String = "",
                   county: String = "",
                   uprn: Option[String] = None) {

  def shortString = if (lineOne.length() > 0)
    lineOne + " " + postcode
  else
    ""

  def asString = List(lineOne, lineTwo, lineThree, lineFour, lineFive, city, postcode)
    .filterNot(_.isEmpty).mkString(", ")

  def asShortString = List(lineOne, lineTwo, lineThree, lineFour, lineFive)
    .filterNot(_.isEmpty).reduceLeft(_ + ", " + _)
}

