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

import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.repository.KettleRepositoryLostException;
import org.pentaho.di.repository.RepositorySecurityManager;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RepositoryServiceSessionTimeoutHandlerTest {

  private RepositorySecurityManager repositoryService;

  private SessionTimeoutHandler sessionTimeoutHandler;

  private RepositoryServiceSessionTimeoutHandler metaStoresessionTimeoutHandler;

  @Before
  public void before() {
    repositoryService = mock( RepositorySecurityManager.class );
    sessionTimeoutHandler = mock( SessionTimeoutHandler.class );
    metaStoresessionTimeoutHandler =
        new RepositoryServiceSessionTimeoutHandler( repositoryService, sessionTimeoutHandler );
  }

  @SuppressWarnings( "unchecked" )
  @Test
  public void testHandlerCallOnException() throws Throwable {
    when( repositoryService.getUsers() ).thenThrow( KettleRepositoryLostException.class );
    Method method = RepositorySecurityManager.class.getMethod( "getUsers" );

    metaStoresessionTimeoutHandler.invoke( mock( Proxy.class ), method, new Object[0] );
    verify( sessionTimeoutHandler ).handle( any(), any(), any(), any() );
  }

}
