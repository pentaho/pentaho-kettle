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
import org.pentaho.di.repository.Repository;
import org.pentaho.di.shared.SharedObjects;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.ui.repo.controller.RepositoryConnectController;

import java.lang.reflect.Method;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SessionTimeoutHandlerTest {

  private RepositoryConnectController repositoryConnectController;

  private Repository repository;

  private SessionTimeoutHandler sessionTimeoutHandler;

  @Before
  public void before() {
    repositoryConnectController = mock( RepositoryConnectController.class );
    repository = mock( Repository.class );
    sessionTimeoutHandler = spy( new SessionTimeoutHandler( repositoryConnectController ) );

    doReturn( true ).when( sessionTimeoutHandler ).lookupForConnectTimeoutError( any() );
    doReturn( false ).when( sessionTimeoutHandler ).calledFromThisHandler();
    doReturn( true ).when( sessionTimeoutHandler ).showLoginScreen( repositoryConnectController );
  }

  @Test
  public void handle() throws Throwable {
    when( repository.readTransSharedObjects( any() ) ).thenReturn( mock( SharedObjects.class ) );
    Method method = Repository.class.getMethod( "readTransSharedObjects", TransMeta.class );

    sessionTimeoutHandler.handle( repository, mock( Exception.class ), method, new Object[] { mock(
        TransMeta.class ) } );

    verify( sessionTimeoutHandler, never() ).showLoginScreen( any() );
  }

  @SuppressWarnings( "unchecked" )
  @Test
  public void handleSecondExecutionFailed() throws Throwable {
    when( repository.readTransSharedObjects( any() ) ).thenThrow( KettleRepositoryLostException.class ).thenReturn(
        mock( SharedObjects.class ) );
    Method method = Repository.class.getMethod( "readTransSharedObjects", TransMeta.class );

    sessionTimeoutHandler.handle( repository, mock( Exception.class ), method, new Object[] { mock(
        TransMeta.class ) } );
    
    verify( sessionTimeoutHandler ).showLoginScreen( any() );
  }

}
