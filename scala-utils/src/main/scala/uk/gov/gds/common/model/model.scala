package uk.gov.gds.common.model

// TODO: flatten model together. There should not be an ELMS model and a default model
// TODO: Standardise on one type of address. There should not be an Address and a GovUkAddress
// TODO: kill the ERO class, we're not using it

case class OpcsId(value: String)

object OpcsId {

  val opcsIdRegex = "[0-9]{2}[A-Z][A-Z]?".r

  def isValid(value: String) = value.matches(opcsIdRegex.toString())
}

case class Address(
  lineOne: String = "",
    lineTwo: String = "",
    lineThree: String = "",
    lineFour: String = "",
    lineFive: String = "",
    city: String = "",
    postcode: String = "",
    county: String = "",
    uprn: Option[String] = None
) {

  def shortString = if (lineOne.length() > 0)
    lineOne + " " + postcode
  else
    ""

  def asString = List(lineOne, lineTwo, lineThree, lineFour, lineFive, city, postcode)
    .filterNot(_.isEmpty).mkString(", ")

  def asShortString = List(lineOne, lineTwo, lineThree, lineFour, lineFive)
    .filterNot(_.isEmpty).reduceLeft(_ + ", " + _)
}

