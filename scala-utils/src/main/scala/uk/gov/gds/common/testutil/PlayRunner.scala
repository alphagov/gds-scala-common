package uk.gov.gds.common.testutil

import uk.gov.gds.common.logging.Logging
import java.lang.ProcessBuilder
import collection.JavaConversions._
import sys.process.Process

trait PlayRunner extends LazyStop with Logging {

  override protected def definitionOfSoonInSeconds = 10

  val playAppName: String
  val playAppPort: String

  def projectRoot = "./"

  def startCommand = List(projectRoot + "playctl.sh", playAppName, "start", playAppPort)

  def stopCommand = List(projectRoot + "playctl.sh", playAppName, "stop")

  override def doStart = {
    val builder = new ProcessBuilder

    logger.info("Running " + startCommand)

    builder.command(startCommand)
    val p = builder.start()
    p.waitFor()
  }

  override def doStop = {
    logger.info("Running " + projectRoot + "playctl.sh " + playAppName + " stop ")

    Process(stopCommand).!
  }
}