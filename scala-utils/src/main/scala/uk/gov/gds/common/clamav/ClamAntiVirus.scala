package uk.gov.gds.common.clamav

import java.net.{InetSocketAddress, Socket}
import java.io._
import play.api.Logger
import uk.gov.gds.common.logging.Logging


class ClamAntiVirus(streamCopyFunction: (InputStream) => Unit = DevNull.nullStream(_),
                    virusDetectedFunction: => Unit = ())
  extends ClamAvConfig
  with Logging {

  private val copyInputStream = new PipedInputStream()
  private val copyOutputStream = new PipedOutputStream(copyInputStream)
  private val streamCopyThread = configureStreamCopyThread()
  private val socket = configureSocket()
  private val toClam = new DataOutputStream(socket.getOutputStream)
  private val fromClam = socket.getInputStream

  streamCopyThread.start()
  toClam.write(instream.getBytes())

  def sendBytesToClamd(bytes: Array[Byte]) {
    toClam.writeInt(bytes.length)
    toClam.write(bytes)
    toClam.flush()

    copyOutputStream.write(bytes)
    copyOutputStream.flush()
  }

  def checkForVirus() {
    try {
      toClam.writeInt(0)
      toClam.flush()
      copyOutputStream.flush()
      copyOutputStream.close()

      val virusInformation = responseFromClamd()

      if (!okResponse.equals(virusInformation)) {
        streamCopyThread.interrupt()
        virusDetectedFunction

        Logger.error("Virus detected " + virusInformation)
        throw new VirusDetectedException(virusInformation)
      } else {
        streamCopyThread.join()
      }
    }
    finally {
      terminate
    }
  }

  def terminate {
    try {
      copyInputStream.close()
      copyOutputStream.close()
      socket.close()
      toClam.close()
    }
    catch {
      case e: IOException =>
        Logger.warn("Error closing socket to clamd", e)
    }
  }

  private def responseFromClamd() = {
    val response = new String(
      Iterator.continually(fromClam.read)
        .takeWhile(_ != -1)
        .map(_.toByte)
        .toArray)

    Logger.debug("Response from clamd: " + response)
    response.trim()
  }

  private def configureSocket() = {
    val sock = new Socket
    sock.setSoTimeout(timeout)
    sock.connect(new InetSocketAddress(host, port))
    sock
  }

  private def configureStreamCopyThread() = new Thread(new Runnable() {
    def run() {
      streamCopyFunction(copyInputStream)
    }
  })
}

private object DevNull {
  def nullStream(inputStream: InputStream) = Unit
}
