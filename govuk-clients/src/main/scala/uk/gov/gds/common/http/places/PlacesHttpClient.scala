package uk.gov.gds.common.http.places

import uk.gov.gds.common.config.Config
import uk.gov.gds.common.http.ApacheHttpClient

object PlacesHttpClient extends ApacheHttpClient {
  private[http] def targetUrl(path: String) = Config("places.api.url") + path
}
