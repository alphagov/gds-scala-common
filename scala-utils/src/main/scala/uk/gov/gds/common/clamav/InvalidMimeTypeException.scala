package uk.gov.gds.common.clamav

class InvalidMimeTypeException(mimeType: String)
  extends Exception("Invalid mime type detected: " + mimeType)
