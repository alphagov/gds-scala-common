package uk.gov.gds.common.mongo.migration

trait ChangeScripts {

  protected def changeScripts: List[ChangeScript]
}
