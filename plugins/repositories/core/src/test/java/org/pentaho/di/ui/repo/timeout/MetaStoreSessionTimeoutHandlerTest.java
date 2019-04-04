/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
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
