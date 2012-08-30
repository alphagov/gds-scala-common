package uk.gov.gds.common.clamav

import uk.gov.gds.common.config.Config

trait ClamAvConfig {

  /*
   * Should virus checking be enabled
   */

  val antivirusActive = Config("clam.antivirus.enabled", "true").toBoolean

  /*
  * Size of chunks in bytes to read in / out of clamd
  */

  val chunkSize = Config("clam.antivirus.chunksize", "32768").toInt

  /*
  * IP address of clamd daemon
  */

  val host = Config("clam.antivirus.host", "127.0.0.1")

  /**
   * port of clamd daemon
   */

  val port = Config("clam.antivirus.port", "3310").toInt

  /**
   * Socket timeout for clam
   */

  val timeout = Config("clam.antivirus.timeout", "5000").toInt

  /**
   * The number of threads to have in the pool to run antivirus conversion / copying
   */

  val threadPoolSize = Config("clam.antivirus.threadpool" , "20").toInt

  /**
   * Clam socket commands
   */
  val instream = "zINSTREAM\0"
  val ping = "zPING\0"
  val status = "nSTATS\n"


  // OK response from clam
  val okResponse = "stream: OK"
}