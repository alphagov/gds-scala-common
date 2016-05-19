package uk.gov.gds.common.testutil

import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.{ ScheduledFuture, TimeUnit, Executors }

trait LazyStop {
  private val counter = new AtomicInteger
  private var isStarted = false

  protected def definitionOfSoonInSeconds = 2

  def doStart

  def doStop

  def start() {
    synchronized {
      timer.cancel()
      counter.incrementAndGet
      if (!isStarted) {
        doStart
        isStarted = true
      }
    }
  }

  def stopUnlessSomeoneCallsStartAgainSoon() {
    if (counter.decrementAndGet <= 0) timer.set()
  }

  def stop() {
    synchronized {
      if (isStarted) {
        doStop
        isStarted = false
      }
    }
  }

  def throwIfNotStarted() {
    synchronized {
      if (counter.get <= 0 || !isStarted) {
        sys.error("Opps. Webapp under test not started. Ensure you call .start() on the appserver!")
      }
    }
  }

  private def timerPing() {
    synchronized {
      if (counter.get <= 0) {
        stop()
      }
    }
  }

  private object timer {
    val executor = Executors.newSingleThreadScheduledExecutor()
    var running: Option[ScheduledFuture[_]] = None

    def cancel() {
      running.foreach(_.cancel(false))
      running = None
    }

    def set() {
      cancel()
      running = Some(executor.schedule(
        new Runnable() {
          def run() {
            timerPing()
          }
        },
        definitionOfSoonInSeconds, TimeUnit.SECONDS
      ))
    }
  }

}
