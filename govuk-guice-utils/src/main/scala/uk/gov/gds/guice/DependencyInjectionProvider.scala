package uk.gov.gds.guice

import com.google.inject.Guice
import com.google.inject.Module
import com.google.inject.Injector

/**
 * Mix this trait into any play controllers that require access to the dependency injection framework.
 *
 * If you wish to specify your own
 */
trait DependencyInjectionProvider {

  import GuiceContainer.injector

  /**
   * This method will return a dependency from the dependency injection framework. It is designed to be used from
   * objects that are not managed by the dependency injection framework, such as play controllers. It only allows for
   * dependency lookup by class, which should be enough for all uses in controllers.
   *
   * @tparam A The class of the dependency to lookup
   * @return instance of class resolved from dependency injection framework
   */

  @inline protected final def dependency[A <: AnyRef](implicit m: Manifest[A]) =
    injector.getInstance(m.erasure.asInstanceOf[Class[A]])

  @inline protected final def initalizeDependencyInjector() {
    injector.getAllBindings
  }
}

object GuiceContainer {

  private var di: Injector = null;

  @inline private[guice] final def injector =
    if (di == null)
      throw new IllegalStateException("Guice is not initialise. You must call initialize first!")
    else
      di

  def initialize(module: Module = DefaultModuleProvider.defaultModule) = performInit(List(module))

  def initialize(modules: List[Module]) = performInit(modules)

  @inline protected final def dependency[A <: AnyRef](implicit m: Manifest[A]) =
    di.getInstance(m.erasure.asInstanceOf[Class[A]])

  protected def initalizeDependencyInjector() {
    di.getAllBindings
  }

  @inline private final def performInit(modules: List[Module]) {
    synchronized {
      if (injector != null)
        throw new IllegalStateException("Guice is already initialized")

      di = Guice.createInjector(modules.toSeq: _*)
    }
  }
}

private[guice] object DefaultModuleProvider {

  import com.google.inject.AbstractModule

  private[guice] class EmptyModule extends AbstractModule {
    def configure {
      // no-op
    }
  }

  lazy val defaultModule = new EmptyModule
}
