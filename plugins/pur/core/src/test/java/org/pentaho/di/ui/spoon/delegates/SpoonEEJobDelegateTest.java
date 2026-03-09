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

package org.pentaho.di.ui.spoon.delegates;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.ui.repository.pur.services.ILockService;
import org.pentaho.di.ui.spoon.Spoon;

public class SpoonEEJobDelegateTest {

  private Spoon spoon;
  private Repository repository;
  private ILockService lockService;
  private SpoonEEJobDelegate delegate;

  @Before
  public void setUp() {
    spoon = mock( Spoon.class );
    when( spoon.getLog() ).thenReturn( mock( LogChannelInterface.class ) );
    repository = mock( Repository.class );
    lockService = mock( ILockService.class );
    delegate = new SpoonEEJobDelegate( spoon );
  }

  /**
   * Helper that invokes the private getLockService() via reflection.
   */
  private ILockService invokeLockService() throws Exception {
    Method m = SpoonEEJobDelegate.class.getDeclaredMethod( "getLockService" );
    m.setAccessible( true );
    return (ILockService) m.invoke( delegate );
  }

  @Test
  public void testGetLockServiceReturnsNullWhenRepositoryIsNull() throws Exception {
    when( spoon.getRepository() ).thenReturn( null );

    ILockService result = invokeLockService();

    assertNull( result );
  }

  @Test
  public void testGetLockServiceReturnsServiceWhenAvailable() throws Exception {
    when( spoon.getRepository() ).thenReturn( repository );
    when( repository.hasService( ILockService.class ) ).thenReturn( true );
    when( repository.getService( ILockService.class ) ).thenReturn( lockService );

    ILockService result = invokeLockService();

    assertNotNull( result );
    verify( repository ).hasService( ILockService.class );
    verify( repository ).getService( ILockService.class );
  }

  @Test
  public void testGetLockServiceReturnsNullWhenServiceNotAvailable() throws Exception {
    when( spoon.getRepository() ).thenReturn( repository );
    when( repository.hasService( ILockService.class ) ).thenReturn( false );

    ILockService result = invokeLockService();

    assertNull( result );
    verify( repository, never() ).getService( any() );
  }

  @Test
  public void testGetLockServiceFetchesFromRepositoryEachTime() throws Exception {
    when( spoon.getRepository() ).thenReturn( repository );
    when( repository.hasService( ILockService.class ) ).thenReturn( true );
    when( repository.getService( ILockService.class ) ).thenReturn( lockService );

    // Call twice to confirm no caching
    invokeLockService();
    invokeLockService();

    verify( repository, org.mockito.Mockito.times( 2 ) ).hasService( ILockService.class );
    verify( repository, org.mockito.Mockito.times( 2 ) ).getService( ILockService.class );
  }

  @Test
  public void testGetLockServiceFetchesFreshAfterRepositoryChange() throws Exception {
    Repository repository2 = mock( Repository.class );
    ILockService lockService2 = mock( ILockService.class );

    // First call returns original repository
    when( spoon.getRepository() ).thenReturn( repository );
    when( repository.hasService( ILockService.class ) ).thenReturn( true );
    when( repository.getService( ILockService.class ) ).thenReturn( lockService );

    ILockService first = invokeLockService();
    assertNotNull( first );

    // Simulate reconnection - new repository instance
    when( spoon.getRepository() ).thenReturn( repository2 );
    when( repository2.hasService( ILockService.class ) ).thenReturn( true );
    when( repository2.getService( ILockService.class ) ).thenReturn( lockService2 );

    ILockService second = invokeLockService();
    assertNotNull( second );

    // Verify the second call used repository2, not the original
    verify( repository2 ).hasService( ILockService.class );
    verify( repository2 ).getService( ILockService.class );
  }
}

