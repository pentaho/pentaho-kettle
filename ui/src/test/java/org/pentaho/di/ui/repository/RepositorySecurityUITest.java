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

package org.pentaho.di.ui.repository;

import org.eclipse.swt.widgets.Shell;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.repository.KettleAuthenticationException;
import org.pentaho.di.repository.KettleRepositoryLostException;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryOperation;
import org.pentaho.di.repository.RepositorySecurityProvider;
import org.pentaho.di.ui.spoon.Spoon;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RepositorySecurityUITest {

  private Shell mockShell;
  private Repository mockRepository;
  private RepositorySecurityProvider mockSecurityProvider;
  private Spoon mockSpoon;
  private MockedStatic<Spoon> spoonMockedStatic;

  @Before
  public void setUp() {
    mockShell = mock( Shell.class );
    mockRepository = mock( Repository.class );
    mockSecurityProvider = mock( RepositorySecurityProvider.class );
    mockSpoon = mock( Spoon.class );

    when( mockRepository.getSecurityProvider() ).thenReturn( mockSecurityProvider );

    spoonMockedStatic = mockStatic( Spoon.class );
    when( Spoon.getInstance() ).thenReturn( mockSpoon );
  }

  @After
  public void tearDown() {
    spoonMockedStatic.close();
  }

  @Test
  public void returnsFalseWhenOperationIsAllowed() throws KettleException {
    doNothing().when( mockSecurityProvider ).validateAction( any( RepositoryOperation[].class ) );

    boolean result = RepositorySecurityUI.verifyOperations( mockShell, mockRepository, true,
      RepositoryOperation.MODIFY_TRANSFORMATION );

    assertFalse( result );
  }

  @Test
  public void returnsFalseWhenRepositoryIsNull() {
    boolean result = RepositorySecurityUI.verifyOperations( mockShell, null, true,
      RepositoryOperation.MODIFY_TRANSFORMATION );

    assertFalse( result );
  }


  @Test
  public void returnsTrueWhenSecurityExceptionThrownWithoutDisplayError() throws KettleException {
    doThrow( new KettleException( "Not allowed" ) )
      .when( mockSecurityProvider ).validateAction( any( RepositoryOperation[].class ) );

    boolean result = RepositorySecurityUI.verifyOperations( mockShell, mockRepository, false,
      RepositoryOperation.MODIFY_TRANSFORMATION );

    assertTrue( result );
  }

  @Test
  public void handlesRepositoryLostExceptionDirectly() throws KettleException {
    KettleRepositoryLostException krle = new KettleRepositoryLostException( "connection lost" );
    doThrow( krle ).when( mockSecurityProvider ).validateAction( any( RepositoryOperation[].class ) );

    boolean result = RepositorySecurityUI.verifyOperations( mockShell, mockRepository, true,
      RepositoryOperation.MODIFY_TRANSFORMATION );

    assertTrue( result );
    verify( mockSpoon ).handleRepositoryLost( krle );
  }

  @Test
  public void handlesRepositoryLostExceptionNestedInKettleException() throws KettleException {
    KettleRepositoryLostException krle = new KettleRepositoryLostException( "connection lost" );
    doThrow( new KettleException( "wrapper", krle ) )
      .when( mockSecurityProvider ).validateAction( any( RepositoryOperation[].class ) );

    boolean result = RepositorySecurityUI.verifyOperations( mockShell, mockRepository, true,
      RepositoryOperation.MODIFY_TRANSFORMATION );

    assertTrue( result );
    verify( mockSpoon ).handleRepositoryLost( krle );
  }

  @Test
  public void returnsTrueWithoutDialogWhenSessionExpired() throws KettleException {
    KettleAuthenticationException authEx = new KettleAuthenticationException( "session expired" );
    doThrow( new KettleException( authEx ) )
      .when( mockSecurityProvider ).validateAction( any( RepositoryOperation[].class ) );

    boolean result = RepositorySecurityUI.verifyOperations( mockShell, mockRepository, true,
      RepositoryOperation.MODIFY_TRANSFORMATION );

    assertTrue( result );
    verify( mockSpoon, never() ).handleRepositoryLost( any() );
  }

  @Test
  public void defaultOverloadPassesDisplayErrorTrue() throws KettleException {
    doNothing().when( mockSecurityProvider ).validateAction( any( RepositoryOperation[].class ) );

    boolean result = RepositorySecurityUI.verifyOperations( mockShell, mockRepository,
      RepositoryOperation.MODIFY_TRANSFORMATION );

    assertFalse( result );
  }

  @Test
  public void returnsFalseWithMultipleAllowedOperations() throws KettleException {
    doNothing().when( mockSecurityProvider ).validateAction( any( RepositoryOperation[].class ) );

    boolean result = RepositorySecurityUI.verifyOperations( mockShell, mockRepository, true,
      RepositoryOperation.MODIFY_TRANSFORMATION, RepositoryOperation.MODIFY_JOB,
      RepositoryOperation.DELETE_TRANSFORMATION );

    assertFalse( result );
  }

  @Test
  public void sessionExpiredExceptionTakesPriorityOverRepositoryLostLookup() throws KettleException {
    KettleAuthenticationException authEx = new KettleAuthenticationException( "auth failed" );
    KettleException wrapper = new KettleException( authEx );
    doThrow( wrapper ).when( mockSecurityProvider ).validateAction( any( RepositoryOperation[].class ) );

    boolean result = RepositorySecurityUI.verifyOperations( mockShell, mockRepository, true,
      RepositoryOperation.MODIFY_TRANSFORMATION );

    assertTrue( result );
    verify( mockSpoon, never() ).handleRepositoryLost( any() );
  }

  @Test
  public void returnsFalseWhenNullRepositoryAndNoOperations() {
    boolean result = RepositorySecurityUI.verifyOperations( mockShell, null, false );

    assertFalse( result );
  }
}

