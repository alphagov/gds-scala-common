package uk.gov.gds.common.clamav

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FunSuite
import java.io.{ByteArrayOutputStream, InputStream}

class ClamAvTest extends FunSuite with ShouldMatchers {

  private val virusSig = "X5O!P%@AP[4\\PZX54(P^)7CC)7}$EICAR-STANDARD-ANTIVIRUS-TEST-FILE!$H+H*\0"

  test("Can scan stream without virus") {
    val clamAv = new ClamAntiVirus()
    clamAv.sendBytesToClamd(getBytes(payloadSize = 1000))
    clamAv.checkForVirus()
  }

  test("Can stream multiple clean blocks to clam") {
    val clamAv = new ClamAntiVirus()
    clamAv.sendBytesToClamd(getBytes(payloadSize = 1000))
    clamAv.sendBytesToClamd(getBytes(payloadSize = 1000))
    clamAv.checkForVirus()
  }

  test("Can detect a small stream with a virus at the beginning") {
    val clamAv = new ClamAntiVirus()

    intercept[VirusDetectedException] {
      clamAv.sendBytesToClamd(getBytes(shouldInsertVirusAtPosition = Some(0)))
      clamAv.checkForVirus()
    }
  }

  test("Calls cleanup function when a virus is detected") {
    var cleanupCalled = false

    def cleanup() {
      cleanupCalled = true
    }

    val clamAv = new ClamAntiVirus(virusDetectedFunction = cleanup())

    intercept[VirusDetectedException] {
      clamAv.sendBytesToClamd(getBytes(shouldInsertVirusAtPosition = Some(0)))
      clamAv.checkForVirus()
    }

    cleanupCalled should be(true)
  }

  test("Can pass in a function which copies input stream to output stream") {
    val outputStream = new ByteArrayOutputStream()

    val clamav = new ClamAntiVirus(streamCopyFunction = {
      inputStream: InputStream =>
        Iterator.continually(inputStream.read())
          .takeWhile(_ != -1)
          .foreach {
          byte =>
            if (Thread.interrupted())
              throw new InterruptedException()

            outputStream.write(byte)
            outputStream.flush()
        }
    })

    try {
      val payload = getBytes(1000)
      clamav.sendBytesToClamd(payload)

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

  private def getBytes(payloadSize: Int = 0,
                       shouldInsertVirusAtPosition: Option[Int] = None) =
    getPayload(payloadSize, shouldInsertVirusAtPosition).getBytes()
}