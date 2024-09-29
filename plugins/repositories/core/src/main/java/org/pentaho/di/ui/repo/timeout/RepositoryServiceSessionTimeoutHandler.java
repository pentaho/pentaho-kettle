/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/


package org.pentaho.di.ui.repo.timeout;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.pentaho.di.repository.IRepositoryService;

public class RepositoryServiceSessionTimeoutHandler implements InvocationHandler {

  private final IRepositoryService repositoryService;

  private final SessionTimeoutHandler sessionTimeoutHandler;

  public RepositoryServiceSessionTimeoutHandler( IRepositoryService repositoryService,
      SessionTimeoutHandler sessionTimeoutHandler ) {
    this.repositoryService = repositoryService;
    this.sessionTimeoutHandler = sessionTimeoutHandler;
  }

  @Override
  public Object invoke( Object proxy, Method method, Object[] args ) throws Throwable {
    try {
      return method.invoke( repositoryService, args );
    } catch ( InvocationTargetException ex ) {
      return sessionTimeoutHandler.handle( repositoryService, ex.getCause(), method, args );
    }
  }
}
