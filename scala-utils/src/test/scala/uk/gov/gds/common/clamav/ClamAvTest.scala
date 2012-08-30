package uk.gov.gds.common.clamav

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import java.io.{ByteArrayOutputStream, ByteArrayInputStream}
import play.api.Logger

class ClamAvTest extends FunSuite with ShouldMatchers {

  private val virusSig = "X5O!P%@AP[4\\PZX54(P^)7CC)7}$EICAR-STANDARD-ANTIVIRUS-TEST-FILE!$H+H*\0"

  test("Can ping clamd") {
    ClamAntiVirus.pingClamServer should be(true)
  }

  test("Can get clamd status") {
    ClamAntiVirus.clamdStatus.contains("POOLS") should be(true)
  }

  test("Can scan stream without virus") {
    ClamAntiVirus.checkStreamForVirus(inputStream = getBytes(payloadSize = 1000))
  }

  test("Can detect a small stream with a virus at the beginning") {
    intercept[VirusDetectedException](ClamAntiVirus.checkStreamForVirus(inputStream = getBytes(shouldInsertVirusAtPosition = Some(0))))
  }

  test("Calls cleanup function when a virus is detected") {
    var cleanupCalled = false

    def cleanup() {
      cleanupCalled = true
    }

    intercept[VirusDetectedException] {
      ClamAntiVirus.checkStreamForVirus(
        inputStream = getBytes(shouldInsertVirusAtPosition = Some(0)),
        virusDetectedFunction = cleanup)
    }

    cleanupCalled should be(true)
  }

  test("Can pass in a function which copies input stream to output stream") {
    val outputStream = new ByteArrayOutputStream()

    try {
      val payload = getPayload(1000)
      val inputStream = getBytes(payload)

      ClamAntiVirus.checkStreamForVirus(
        inputStream = inputStream,
        streamCopyFunction = {
          inputStream =>

            Logger.info("Running thread")
            Iterator.continually(inputStream.read())
              .takeWhile(_ != -1)
              .foreach {
              byte =>
                if (Thread.interrupted())
                  throw new InterruptedException()

                Logger.info("read: " + byte)

                outputStream.write(byte)
                outputStream.flush()
            }
        })

      new String(outputStream.toByteArray) should be(payload)
    }
    finally {
      outputStream.close()
    }
  }

  private def getPayload(payloadSize: Int = 0, shouldInsertVirusAtPosition: Option[Int] = None) = {
    val payloadData = shouldInsertVirusAtPosition match {
      case Some(position) =>
        val virusStartPosition = math.min(position, payloadSize - virusSig.length)
        val virusEndPosition = virusStartPosition + virusSig.length

        0.until(virusStartPosition).map(_ => "a") ++ virusSig ++ virusEndPosition.until(payloadSize).map(_ => "a")

      case _ =>
        0.until(payloadSize).map(_ => "a")
    }

    val payload = payloadData.mkString

    shouldInsertVirusAtPosition match {
      case Some(position) =>
        payload.contains(virusSig) should be(true)
        payload.length should be(math.max(virusSig.length, payloadSize))
      case _ =>
        payload.length should be(payloadSize)
    }

    payload
  }

  private def getBytes(payload: String) = new ByteArrayInputStream(payload.getBytes())

  private def getBytes(payloadSize: Int = 0, shouldInsertVirusAtPosition: Option[Int] = None) =
    new ByteArrayInputStream(getPayload(payloadSize, shouldInsertVirusAtPosition).getBytes())
}