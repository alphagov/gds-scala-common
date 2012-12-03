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

  /**
   * This method will return a dependency from the dependency injection framework. It is designed to be used from
   * objects that are not managed by the dependency injection framework, such as play controllers. It only allows for
   * dependency lookup by class, which should be enough for all uses in controllers.
   *
   * @tparam A The class of the dependency to lookup
   * @return instance of class resolved from dependency injection framework
   */

  @inline protected final def dependency[A <: AnyRef](implicit m: Manifest[A]) = GuiceContainer.dependency[A]
}

/**
 * The GuiceContainer sets up & provides access to the Guice DI containter within the app. Note: This object is not
 * threadsafe, as it needs to provide access to Guice in a static context also, so integration tests that require Guice
 * objects (which include play framework FakeApplication tests) MUST be run in series, as the DI container will be re-initialized
 * on each test.
 */

object GuiceContainer {

  private var di: Injector = null;

  /**
   * Call this method in a static context when your app starts up, within Global in play for example,
   * to initialize guice. You can pass in a module to configure the DI container if required, but this is optional.
   * If you don't do this then a default empty module will be used, which may be sufficient for most simple
   * applications.
   */

  def initialize(module: Module = DefaultModuleProvider.defaultModule) = performInit(List(module))

  /**
   * Call this method in a static context when your app starts up, within Global in play for example,
   * to initialize guice. You can pass in an array of modules to configure the DI container. This method is provided
   * for the case when you want to initialise the DI container with an array of configuration modules.
   */

  def initialize(modules: List[Module]) = performInit(modules)

  @inline private[guice] final def dependency[A <: AnyRef](implicit m: Manifest[A]) =
    injector.getInstance(m.erasure.asInstanceOf[Class[A]])

  @inline private final def performInit(modules: List[Module]) {
    synchronized {
      di = Guice.createInjector(modules.toSeq: _*)
    }
  }

  @inline private final def injector =
    if (di == null)
      throw new IllegalStateException("Guice is not initialised. You must call initialize first!")
    else
      di
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
