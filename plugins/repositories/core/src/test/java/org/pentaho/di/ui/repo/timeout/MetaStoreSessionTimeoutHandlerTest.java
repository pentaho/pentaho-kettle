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

import static org.mockito.Mockito.*;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.repository.KettleRepositoryLostException;
import org.pentaho.di.ui.repo.timeout.MetaStoreSessionTimeoutHandler;
import org.pentaho.di.ui.repo.timeout.SessionTimeoutHandler;
import org.pentaho.metastore.api.IMetaStore;

public class MetaStoreSessionTimeoutHandlerTest {

  private IMetaStore metaStore;

  private SessionTimeoutHandler sessionTimeoutHandler;

  private MetaStoreSessionTimeoutHandler metaStoresessionTimeoutHandler;

  @Before
  public void before() {
    metaStore = mock( IMetaStore.class );
    sessionTimeoutHandler = mock( SessionTimeoutHandler.class );
    metaStoresessionTimeoutHandler = new MetaStoreSessionTimeoutHandler( metaStore, sessionTimeoutHandler );
  }

  @SuppressWarnings( "unchecked" )
  @Test
  public void testHandlerCallOnException() throws Throwable {
    when( metaStore.getName() ).thenThrow( KettleRepositoryLostException.class );
    Method method = IMetaStore.class.getMethod( "getName" );

    metaStoresessionTimeoutHandler.invoke( mock( Proxy.class ), method, new Object[0] );
    verify( sessionTimeoutHandler ).handle( any(), any(), any(), any() );
  }

}
