package uk.gov.gds.common.j2ee

import javax.servlet.{ServletContextListener, ServletContextEvent}

trait ContainerEventListener {
  def startup() {}

  def shutdown() {}
}

abstract class ContextEventBroadcaster extends ServletContextListener {

  protected def listeners: List[ContainerEventListener]

  override def contextInitialized(event: ServletContextEvent) {
    listeners.foreach(_.startup())
  }

  override def contextDestroyed(event: ServletContextEvent) {
    listeners.foreach(_.shutdown())
  }
}

