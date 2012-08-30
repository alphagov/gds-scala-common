package uk.gov.gds.common.clamav

import java.net.{InetSocketAddress, Socket}
import java.io._
import play.api.Logger

object ClamAntiVirus extends ClamAvConfig {

  def pingClamServer = "PONG".equals(cmd(ping))

  def clamdStatus = cmd(status)

  def checkStreamForVirus(inputStream: InputStream,
                          streamCopyFunction: (InputStream) => Unit = devNull(_),
                          virusDetectedFunction: => Unit = ()) {
    val pipedInputStream = new PipedInputStream()
    val pipedOutputStream = new PipedOutputStream(pipedInputStream)

    try {
      val streamCopyThread = runStreamCopyThread(pipedInputStream, streamCopyFunction)
      val virusInformation = onClamAvServer(_.scan(inputStream, pipedOutputStream))

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
      inputStream.close()
      pipedInputStream.close()
      pipedOutputStream.close()
    }
  }

  private def runStreamCopyThread(inputStream: PipedInputStream, block: (InputStream) => Unit) = {
    val thread = new Thread(new Runnable() {
      def run() {
        block(inputStream)
      }
    })

    thread.start()
    thread
  }

  private def devNull(inputStream: InputStream) = Unit

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

      try {
        streamFileToClamd(toScan, copy)
      }
      finally {
        copy.close()
      }

      responseFromClamd()
    }

    private def streamFileToClamd(toScan: InputStream, copy: OutputStream) {
      Iterator.continually(toScan.read)
        .takeWhile(_ != -1)
        .grouped(chunkSize)
        .foreach {
        group =>
          val bytes = group.map(_.toByte).toArray

          toClam.writeInt(bytes.length)
          toClam.write(bytes)
          copy.write(bytes)
          toClam.flush()
          copy.flush()
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

