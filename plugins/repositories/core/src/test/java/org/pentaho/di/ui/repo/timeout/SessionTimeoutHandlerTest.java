/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.di.ui.repo.timeout;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.repository.KettleRepositoryLostException;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.ui.repo.controller.RepositoryConnectController;

import java.lang.reflect.Method;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
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
    when( repository.getDatabaseIDs( anyBoolean() ) ).thenReturn( new ObjectId[0] );
    Method method = Repository.class.getMethod( "getDatabaseIDs", boolean.class );

    sessionTimeoutHandler.handle( repository, mock( Exception.class ), method, new Object[] { Boolean.FALSE } );

    verify( sessionTimeoutHandler, never() ).showLoginScreen( any() );
  }

  @Test
  public void handleSecondExecutionFailed() throws Throwable {
    when( repository.getDatabaseIDs( anyBoolean() ) ).thenThrow( KettleRepositoryLostException.class ).thenReturn( new ObjectId[0] );
    Method method = Repository.class.getMethod( "getDatabaseIDs", boolean.class );

    sessionTimeoutHandler.handle( repository, mock( Exception.class ), method, new Object[] { Boolean.FALSE } );
    
    verify( sessionTimeoutHandler ).showLoginScreen( any() );
  }

}
