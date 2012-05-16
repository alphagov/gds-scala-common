package uk.gov.gds.common.mongo.migration

trait ChangeScript {
  /**
   * Apply this change script to the database. This must be implemented in subclasses of ChangeScript
   */

  def applyToDatabase(): Unit
}
