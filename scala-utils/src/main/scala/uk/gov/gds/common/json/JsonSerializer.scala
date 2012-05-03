package uk.gov.gds.common.json

import com.codahale.jerkson.Json._

object JsonSerializer {

  def toJson[A](obj: A)(implicit m: Manifest[A]): String = generate[A](obj)

  def fromJson[A](json: String)(implicit m: Manifest[A]) = parse[A](json)
}
