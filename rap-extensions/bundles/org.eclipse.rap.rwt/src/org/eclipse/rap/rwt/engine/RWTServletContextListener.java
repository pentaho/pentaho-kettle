/*******************************************************************************
 * Copyright (c) 2002, 2015 Innoopract Informationssysteme GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Innoopract Informationssysteme GmbH - initial API and implementation
 *   Frank Appel - replaced singletons and static fields (Bug 337787)
 *   EclipseSource - ongoing development
 ******************************************************************************/
package org.eclipse.rap.rwt.engine;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletRegistration;

import org.eclipse.rap.rwt.application.Application;
import org.eclipse.rap.rwt.application.ApplicationConfiguration;
import org.eclipse.rap.rwt.application.ApplicationRunner;
import org.eclipse.rap.rwt.application.EntryPoint;
import org.eclipse.rap.rwt.internal.util.ClassUtil;


/**
 * A ServletContextListener that creates and starts an RWT application on
 * initialization and stops it on shutdown. The application to start is read
 * from the init parameter <code>org.eclipse.rap.applicationConfiguration</code>.
 *
 * @since 2.0
 * @noextend This class is not intended to be subclassed by clients.
 */
public class RWTServletContextListener implements ServletContextListener {

  /*
   * These parameters have been used prior to RAP 1.5 to register entrypoints.
   * They are still used by the RWTLauncher.
   */
  static final String ENTRY_POINTS_PARAM = "org.eclipse.rwt.entryPoints";
  static final String RWT_SERVLET_NAME = "rwtServlet";

  private ApplicationRunner applicationRunner;

  @Override
  public void contextInitialized( ServletContextEvent event ) {
    ServletContext servletContext = event.getServletContext();
    ApplicationConfiguration configuration = readConfiguration( servletContext );
    applicationRunner = new ApplicationRunner( configuration, servletContext );
    applicationRunner.start();
  }

  @Override
  public void contextDestroyed( ServletContextEvent event ) {
    applicationRunner.stop();
    applicationRunner = null;
  }

  private ApplicationConfiguration readConfiguration( ServletContext servletContext ) {
    String name = servletContext.getInitParameter( ApplicationConfiguration.CONFIGURATION_PARAM );
    if( name != null ) {
      return createConfiguration( name );
    }
    return readEntryPointRunnerConfiguration( servletContext );
  }

  private ApplicationConfiguration createConfiguration( String className ) {
    return ( ApplicationConfiguration )ClassUtil.newInstance( getClassLoader(), className );
  }

  private ApplicationConfiguration readEntryPointRunnerConfiguration( ServletContext context ) {
    try {
      return doReadEntryPointRunnerConfiguration( context );
    } catch( ClassNotFoundException cnfe ) {
      throw new IllegalArgumentException( cnfe );
    }
  }

  @SuppressWarnings("unchecked")
  private ApplicationConfiguration doReadEntryPointRunnerConfiguration( ServletContext context )
    throws ClassNotFoundException
  {
    String servletPath = "/rap";
    ServletRegistration servletRegistration = context.getServletRegistration( RWT_SERVLET_NAME );
    if( servletRegistration != null ) {
      String[] mappings = servletRegistration.getMappings().toArray( new String[ 0 ] );
      if( mappings.length > 0 ) {
        servletPath = mappings[ 0 ].equals( "" ) ? "/" : mappings[ 0 ];
      }
    }
    String className = context.getInitParameter( ENTRY_POINTS_PARAM );
    Class<?> entryPointClass = getClassLoader().loadClass( className );
    return new EntryPointRunnerConfiguration( servletPath,
                                              ( Class<? extends EntryPoint> )entryPointClass );
  }

  private ClassLoader getClassLoader() {
    ClassLoader loader = Thread.currentThread().getContextClassLoader();
    if( loader == null ) {
      loader = getClass().getClassLoader();
    }
    return loader;
  }

  private static class EntryPointRunnerConfiguration implements ApplicationConfiguration {

    private final String servletPath;
    private final Class<? extends EntryPoint> entryPointClass;

    private EntryPointRunnerConfiguration( String servletPath,
                                           Class<? extends EntryPoint> entryPointClass )
    {
      this.servletPath = servletPath;
      this.entryPointClass = entryPointClass;
    }

    @Override
    public void configure( Application application ) {
      application.addEntryPoint( servletPath, entryPointClass, null );
    }
  }

}
