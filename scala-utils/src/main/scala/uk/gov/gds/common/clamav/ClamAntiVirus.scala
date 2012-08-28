package uk.gov.gds.common.clamav

import java.net.{InetSocketAddress, Socket}
import java.io.{OutputStream, InputStream, DataOutputStream, IOException}
import play.api.Logger
import com.google.common.io.NullOutputStream

object ClamAntiVirus extends ClamAvConfig {

  private val devNull = new NullOutputStream

  def pingClamServer = "PONG".equals(cmd(ping))

  def clamdStatus = cmd(status)

  def checkStreamForVirus(inputStream: InputStream,
                          outputStream: OutputStream = devNull,
                          virusDetectedFunction: => Unit = ()) {
    val virusInformation = onClamAvServer(_.scan(inputStream, outputStream))

    if (!okResponse.equals(virusInformation)) {
      virusDetectedFunction
      Logger.error("Virus detected " + virusInformation)
      throw new VirusDetectedException(virusInformation)
    }
  }

  private def cmd(cmd: String) = onClamAvServer(_.execute(cmd))

  private def onClamAvServer(block: (ClamServer) => String) = {
    if (!antivirusActive) {
      Logger.warn("Anti-virus checking disabled")
      okResponse
    }
    else {
      val socket: Socket = new Socket
      var dataOutputStream: DataOutputStream = null

      try {
        dataOutputStream = configureSocket(socket)
        block(ClamServer(dataOutputStream, socket.getInputStream))
      }
      catch {
        case e: Exception =>
          Logger.error("Exception communicating with clamd", e)
          throw e
      }
      finally {
        try {
          if (dataOutputStream != null) dataOutputStream.close()
          if (!socket.isClosed) socket.close()
        }
        catch {
          case e: IOException => Logger.warn("Error closing socket to clamd", e)
        }
      }
    }
  }

  private def configureSocket(socket: Socket) = {
    socket.setSoTimeout(timeout)
    socket.connect(new InetSocketAddress(host, port))
    new DataOutputStream(socket.getOutputStream())
  }

  private case class ClamServer(toClam: DataOutputStream, fromClam: InputStream) {

    def execute(command: String) = {
      begin(command)
      toClam.flush()
      responseFromClamd()
    }

    def scan(toScan: InputStream, copy: OutputStream) = {
      begin(instream)
      streamFileToClamd(toScan, copy)
      responseFromClamd()
    }

    private def streamFileToClamd(toScan: InputStream, copy: OutputStream) {
      Iterator.continually(toScan.read)
        .takeWhile(_ != -1)
        .grouped(chunkSize)
        .foreach {
        group =>
          toClam.writeInt(group.length)

          group.foreach {
            byte =>
              toClam.write(byte)
              copy.write(byte)
          }
      }
      toClam.writeInt(0)
      toClam.flush()
    }

    private def begin(command: String) {
      Logger.debug("Request to clamd: " + command)
      toClam.write(command.getBytes())
    }

    private def responseFromClamd() = {
      val response = new String(
        Iterator.continually(fromClam.read)
          .takeWhile(_ != -1)
          .map(_.toByte)
          .toArray)

      Logger.info("Response from clamd: " + response)
      response.trim()
    }
  }

}

