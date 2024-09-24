/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2024 by Hitachi Vantara : http://www.pentaho.com
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
