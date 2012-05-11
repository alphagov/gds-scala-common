package uk.gov.gds.common.mongo.migration

import org.joda.time.DateTime
import uk.gov.gds.common.repository.HasIdentity

case class ChangeScript(name: String, runAt: DateTime) extends HasIdentity {
  def id = name
}