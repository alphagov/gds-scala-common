package uk.gov.gds.common.http

import java.net.{URLEncoder, URLDecoder}

trait UrlEncoding {

  def urlDecode(in: String) = URLDecoder.decode(in, "UTF-8")

  def urlEncode(in: String) = URLEncoder.encode(in, "UTF-8")
}