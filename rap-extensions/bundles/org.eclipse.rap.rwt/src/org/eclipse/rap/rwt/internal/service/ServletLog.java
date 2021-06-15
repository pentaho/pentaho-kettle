/*******************************************************************************
 * Copyright (c) 2009, 2015 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.rap.rwt.internal.service;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

import org.eclipse.rap.rwt.RWT;


public final class ServletLog {

  public static void log( String message, Throwable throwable ) {
    ServletContext servletContext = getServletContext();
    if( servletContext == null ) {
      System.err.println( message );
      if( throwable != null ) {
        throwable.printStackTrace( System.err );
      }
    } else if( throwable == null ) {
      servletContext.log( message );
    } else {
      servletContext.log( message, throwable );
    }
  }

  private static ServletContext getServletContext() {
    try {
      HttpSession session = RWT.getUISession().getHttpSession();
      return session.getServletContext();
    } catch( @SuppressWarnings( "unused" ) Exception e ) {
      // Obtaining the servlet context is not critical, if it fails, log to system error
      return null;
    }
  }

}
