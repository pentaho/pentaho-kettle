/*******************************************************************************
 * Copyright (c) 2011, 2015 Frank Appel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Frank Appel - initial API and implementation
 *    EclipseSource - ongoing development
 ******************************************************************************/
package org.eclipse.rap.rwt.application;

import javax.servlet.ServletContext;

import org.eclipse.rap.rwt.internal.SingletonManager;
import org.eclipse.rap.rwt.internal.application.ApplicationContextImpl;
import org.eclipse.rap.rwt.internal.resources.ResourceDirectory;
import org.eclipse.rap.rwt.internal.util.ParamCheck;
import org.eclipse.rap.rwt.service.ApplicationContext;


/**
 * An <code>ApplicationRunner</code> is used to start an RWT application with
 * the given <code>ApplicationConfiguration</code> in the given
 * <code>ServletContext</code>.
 * <p>
 * In most cases, application developers don't have to use this class directly.
 * Instead of this, the class <code>RWTServletContextListener</code> can be
 * registered as a listener in the deployment descriptor (web.xml). In this
 * case, the <code>ApplicationConfiguration</code> defined in the init-parameter
 * <code>org.eclipse.rap.applicationConfiguration</code> will be started by the
 * framework.
 * </p>
 * <p>
 * When a custom <code>ServletContextListener</code> is used, the
 * <code>ApplicationRunner</code> is usually constructed and started in the
 * <code>contextInitialized()</code> method and stopped in the
 * <code>contextDestroyed()</code> method.
 * </p>
 *
 * @since 2.0
 * @see ApplicationConfiguration
 * @see org.eclipse.rap.rwt.engine.RWTServletContextListener
 * @see javax.servlet.ServletContext
 * @see javax.servlet.ServletContextListener
 * @noextend This class is not intended to be subclassed by clients.
 */
public class ApplicationRunner {

  public final static String RESOURCES = ResourceDirectory.DIRNAME;

  private final ApplicationContextImpl applicationContext;

  /**
   * Constructs a new instance of this class given an application configuration and
   * the servlet context it is bound to.
   *
   * @param configuration the configuration for the application to start. Must not be
   *          <code>null</code>.
   * @param servletContext the servlet context this application is bound to.
   *          Must not be <code>null</code>.
   */
  public ApplicationRunner( ApplicationConfiguration configuration, ServletContext servletContext )
  {
    ParamCheck.notNull( configuration, "configuration" );
    ParamCheck.notNull( servletContext, "servletContext" );
    applicationContext = new ApplicationContextImpl( configuration, servletContext );
  }

  /**
   * Starts the application if it is not running. If the application is already running, this method
   * does nothing.
   */
  public void start() {
    applicationContext.attachToServletContext();
    SingletonManager.install( applicationContext );
    try {
      applicationContext.activate();
    } catch( RuntimeException rte ) {
      applicationContext.removeFromServletContext();
      throw rte;
    }
  }

  /**
   * Stops the application if it is running. If the application is not running, this method does
   * nothing.
   */
  public void stop() {
    try {
      applicationContext.deactivate();
    } finally {
      applicationContext.removeFromServletContext();
    }
  }

  /**
   * Returns the <code>ApplicationContext</code> of the running application that is controlled by
   * this application runner. If the application is not running, this method will return
   * <code>null</code>.
   *
   * @return the {@link ApplicationContext} of the running application or <code>null</code> if the
   *         application is not running
   * @since 3.0
   */
  public ApplicationContext getApplicationContext() {
    return applicationContext.isActive() ? applicationContext : null;
  }

}
