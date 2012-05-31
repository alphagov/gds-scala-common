package uk.gov.gds.common.testutil

import java.util.concurrent.locks.ReentrantLock

object IntegrationTestMutex {

  private val mutex = new ReentrantLock()

  def sequentially[A](block: => A) = try {
    lock()
    block
  }
  finally {
    unlock()
  }

  def lock() = mutex.lock()

  def unlock() = mutex.unlock()
}