package uk.gov.gds.common.clamav

class VirusDetectedException(val virusInformation: String)
  extends Exception("Virus detected: " + virusInformation)
