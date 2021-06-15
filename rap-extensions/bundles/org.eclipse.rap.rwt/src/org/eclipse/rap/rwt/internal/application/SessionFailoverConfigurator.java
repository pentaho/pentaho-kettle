/*******************************************************************************
 * Copyright (c) 2011, 2012 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.rap.rwt.internal.application;

import java.util.Collection;
import java.util.EnumSet;

import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.FilterRegistration;
import javax.servlet.FilterRegistration.Dynamic;
import javax.servlet.ServletContext;
import javax.servlet.ServletRegistration;

import org.eclipse.rap.rwt.engine.RWTServlet;
import org.eclipse.rap.rwt.internal.engine.RWTClusterSupport;
import org.eclipse.rap.rwt.internal.lifecycle.SimpleLifeCycle;

/*
 * This class requires the servlet 3.0 API to be on the class path.
 */
class SessionFailoverConfigurator {
  private final ApplicationContextImpl applicationContext;
  private final ServletContext servletContext;

  SessionFailoverConfigurator( ApplicationContextImpl applicationContext ) {
    this.applicationContext = applicationContext;
    servletContext = applicationContext.getServletContext();
  }

  void configure() {
    checkServletVersion();
    ServletRegistration servletRegistration = findRWTServletRegistration();
    checkRWTServletRegistration( servletRegistration );
    configureJEECompatibility();
    configureSessionFailoverFilter( servletRegistration );
  }

  private void configureJEECompatibility() {
    applicationContext.getLifeCycleFactory().configure( SimpleLifeCycle.class );
  }

  private void configureSessionFailoverFilter( ServletRegistration servletRegistration ) {
    Dynamic filterRegistration = registerSessionFailoverFilter();
    mapFilterToServlet( filterRegistration, servletRegistration );
  }

  private Dynamic registerSessionFailoverFilter() {
    Filter filter = new RWTClusterSupport();
    String filterName = filter.getClass().getName();
    return servletContext.addFilter( filterName, filter );
  }

  private static void mapFilterToServlet( FilterRegistration filterRegistration,
                                          ServletRegistration servletRegistration )
  {
    EnumSet<DispatcherType> dispatcherType = EnumSet.of( DispatcherType.REQUEST );
    String servletName = servletRegistration.getName();
    filterRegistration.addMappingForServletNames( dispatcherType, false, servletName );
  }

  private void checkServletVersion() {
    if( servletContext.getMajorVersion() < 3 ) {
      throw new IllegalStateException( "Session failover support requires Servlet 3.0 or later." );
    }
  }

  private void checkRWTServletRegistration( ServletRegistration servletRegistration ) {
    if( servletRegistration == null ) {
      throw new IllegalStateException( "The RWT servlet registration could not be found." );
    }
  }

  private ServletRegistration findRWTServletRegistration() {
    ServletRegistration result = null;
    ServletRegistration[] servletRegistrations = getSerlvetRegistrations();
    for( int i = 0; result == null && i < servletRegistrations.length; i++ ) {
      if( RWTServlet.class.getName().equals( servletRegistrations[ i ].getClassName() ) ) {
        result = servletRegistrations[ i ];
      }
    }
    return result;
  }

  private ServletRegistration[] getSerlvetRegistrations() {
    Collection<?> servletRegistrations = servletContext.getServletRegistrations().values();
    return servletRegistrations.toArray( new ServletRegistration[ servletRegistrations.size() ] );
  }
}
