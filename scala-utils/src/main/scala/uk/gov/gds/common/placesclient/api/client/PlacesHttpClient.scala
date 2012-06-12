package uk.gov.gds.placesclient.api.client

import uk.gov.gds.common.config.Config
import uk.gov.gds.common.http.ApacheHttpClient

object PlacesHttpClient extends ApacheHttpClient {
  @inline protected def targetUrl(path: String) = Config("places.api.url") + path
}
