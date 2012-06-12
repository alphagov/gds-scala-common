package uk.gov.gds.placesclient.model

case class PAList(Items: List[PAAddress])

case class PAAddress(Udprn: Int,
                     Company: String,
                     Department: String,
                     Line1: String,
                     Line2: String,
                     Line3: String,
                     Line4: String,
                     Line5: String,
                     PostTown: String,
                     County: String,
                     Postcode: String,
                     Mailsort: Int,
                     Barcode: String,
                     Type: String,
                     DeliveryPointSuffix: String,
                     SubBuilding: String,
                     PoBox: String,
                     CountryName: String) {

  def generateAddress() = GovUkAddress(
    lineOne = Line1,
    lineTwo = Line2,
    lineThree = Line3,
    lineFour = Line4,
    lineFive = Line5,
    city = PostTown,
    postcode = Postcode,
    county = County,
    uprn = Some(Udprn.toString))
}

