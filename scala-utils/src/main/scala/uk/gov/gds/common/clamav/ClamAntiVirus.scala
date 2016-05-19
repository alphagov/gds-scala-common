package uk.gov.gds.common.clamav

import java.io._
import uk.gov.gds.common.logging.Logging
import net.sf.jmimemagic.Magic
import java.net.{ InetSocketAddress, Socket }

class ClamAntiVirus(
  streamCopyFunction: (InputStream) => Unit = DevNull.nullStream(_),
  virusDetectedFunction: => Unit = (),
  allowedMimeTypes: Set[String]
)
    extends ClamAvConfig with Logging {

  private val copyInputStream = new PipedInputStream()
  private val copyOutputStream = new PipedOutputStream(copyInputStream)
  private val socket = configureSocket()
  private val toClam = new DataOutputStream(socket.getOutputStream)
  private val fromClam = socket.getInputStream
  private val streamCopyThread = runStreamCopyThread()

  @volatile private var mimeTypeDetected: String = null

  toClam.write(instream.getBytes())

  def sendBytesToClamd(bytes: Array[Byte]) {
    if (mimeTypeDetected == null)
      mimeTypeDetected = detectMimeType(bytes)

    toClam.writeInt(bytes.length)
    toClam.write(bytes)
    copyOutputStream.write(bytes)
    toClam.flush()
    copyOutputStream.flush()
  }

  def checkForVirus() {
    try {
      toClam.writeInt(0)
      toClam.flush()
      copyOutputStream.flush()
      copyOutputStream.close()

      val virusInformation = responseFromClamd()

      if ((!okResponse.equals(virusInformation)) || !isValidMimeType) {
        streamCopyThread.interrupt()
        virusDetectedFunction

        logger.error("Virus detected " + virusInformation)
        raiseError(virusInformation)
      } else {
        streamCopyThread.join()
      }
    } finally {
      terminate
    }
  }

  def terminate() {
    try {
      copyInputStream.close()
      copyOutputStream.close()
      socket.close()
      toClam.close()
    } catch {
      case e: IOException =>
        logger.warn("Error closing socket to clamd", e)
    }
  }

  private def raiseError(responseFromClamd: String): Nothing =
    if (!isValidMimeType)
      throw new InvalidMimeTypeException(mimeTypeDetected)
    else
      throw new VirusDetectedException(responseFromClamd)

  private def isValidMimeType =
    if (allowedMimeTypes.contains(mimeTypeDetected)) {
      true
    } else {
      false
    }

  private def detectMimeType(bytes: Array[Byte]) = {
    val mimeType = Magic.getMagicMatch(bytes).getMimeType

    if (mimeType == null)
      "[unknown mime type]"
    else
      mimeType
  }

  private def responseFromClamd() = {
    val response = new String(
      Iterator.continually(fromClam.read)
      .takeWhile(_ != -1)
      .map(_.toByte)
      .toArray
    )

    logger.info("Response from clamd: " + response)
    response.trim()
  }

  private def configureSocket() = {
    val sock = new Socket
    sock.setSoTimeout(timeout)
    sock.connect(new InetSocketAddress(host, port))
    sock
  }

  private def runStreamCopyThread() = {
    val thread = new Thread(new Runnable() {
      def run() {
        streamCopyFunction(copyInputStream)
      }
    })

    thread.start()
    thread
  }
}

private object DevNull {
  def nullStream(inputStream: InputStream) =
    Iterator.continually(inputStream.read())
      .takeWhile(_ != -1)
      .foreach {
        b => // no-op. We just throw the bytes away
      }
}
