/*******************************************************************************
 * Copyright (c) 2011, 2012 Frank Appel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Frank Appel - initial API and implementation
 *    EclipseSource - ongoing developement
 ******************************************************************************/
package org.eclipse.rap.rwt.application;


/**
 * An <code>ApplicationConfiguration</code> describes an RWT application, including
 * the entrypoints, URL mappings, themes, etc. that constitute the application.
 * <p>
 * The <code>configure</code> method will be called by the framework in order
 * to configure an application instance before it is started. An implementation
 * must at least register an entrypoint that provides the user interface for the
 * application. A simple implementation of this interface looks like this:
 * </p>
 * <pre>
 * public class ExampleConfiguration implements ApplicationConfiguration {
 *
 *   public void configure( Application application ) {
 *     configuration.addEntryPoint( &quot;/example&quot;, ExampleEntryPoint.class, null );
 *   }
 * }
 * </pre>
 * <p>
 * The <code>configure</code> method is called only once during the lifetime of
 * an application. The configuration of the application takes place before the
 * system is activated. Therefore, manipulation of the configuration instance at
 * a later point in time is unsupported.
 * </p>
 * <p>
 * There can be more than one application instance at runtime, running on
 * different network ports or in different contexts. In most cases, developers
 * do not have to create an application instance explicitly. The
 * <code>ApplicationConfiguration</code> can be registered with the
 * the surrounding container instead. For example, in a servlet container, the
 * application can be registered as <code>context-param</code> in the
 * <code>web.xml</code> (see <code>CONFIGURATION_PARAM</code>), in
 * <code>OSGi</code> it can be registered as a service, and when using the
 * workbench with RAP, the application is registered with an extension-point.
 * </p>
 * <p>
 * Apart from this, an <code>{@link ApplicationRunner ApplicationRunner}</code>
 * can be used to run an application with this configuration.
 * </p>
 *
 * @see Application
 * @see ApplicationRunner
 * @since 2.0
 */
public interface ApplicationConfiguration {

  /**
   * This constant contains the context parameter name to register an
   * ApplicationConfiguration in a servlet container environment when running
   * RAP without OSGi. To do so, the fully qualified class name of the
   * implementation has to be registered as a <code>context-param</code> in the
   * <code>web.xml</code>. Example:
   *
   * <pre>
   * &lt;context-param&gt;
   *   &lt;param-name&gt;org.eclipse.rap.applicationConfiguration&lt;/param-name&gt;
   *   &lt;param-value&gt;com.example.ExampleConfiguration&lt;/param-value&gt;
   * &lt;/context-param&gt
   * </pre>
   */
  public static final String CONFIGURATION_PARAM = "org.eclipse.rap.applicationConfiguration";

  /**
   * This constant contains the context parameter name to configure the web
   * application's context directory on disk.
   */
  public static final String RESOURCE_ROOT_LOCATION = "resource_root_location";

  /**
   * Implementations must use this method to configure an application. The
   * method is called by the framework once before the application is started.
   *
   * @param application the application to configure
   */
  void configure( Application application );
}
