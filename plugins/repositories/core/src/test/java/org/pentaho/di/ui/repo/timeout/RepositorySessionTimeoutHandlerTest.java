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

import org.eclipse.swt.widgets.Display;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.pentaho.di.connections.ConnectionManager;
import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.metastore.MetaStoreConst;
import org.pentaho.di.repository.IRepositoryService;
import org.pentaho.di.repository.KettleAuthenticationException;
import org.pentaho.di.repository.KettleRepositoryLostException;
import org.pentaho.di.repository.ReconnectableRepository;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositorySecurityManager;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.ui.repo.controller.RepositoryConnectController;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.metastore.api.IMetaStore;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RepositorySessionTimeoutHandlerTest {

  @BeforeClass
  public static void setUpClass() {
    if ( !KettleLogStore.isInitialized() ) {
      KettleLogStore.init();
    }
  }

  private ReconnectableRepository repository;

  private RepositoryConnectController repositoryConnectController;

  private SessionTimeoutHandler mockSessionTimeoutHandler;

  private RepositorySessionTimeoutHandler timeoutHandler;

  @Before
  public void before() throws Exception {
    repository = mock( ReconnectableRepository.class );
    repositoryConnectController = mock( RepositoryConnectController.class );
    timeoutHandler = new RepositorySessionTimeoutHandler( repository, repositoryConnectController );

    mockSessionTimeoutHandler = mock( SessionTimeoutHandler.class );
    when( mockSessionTimeoutHandler.getRepositoryConnectController() ).thenReturn( repositoryConnectController );

    Field stField = RepositorySessionTimeoutHandler.class.getDeclaredField( "sessionTimeoutHandler" );
    stField.setAccessible( true );
    stField.set( timeoutHandler, mockSessionTimeoutHandler );
  }

  @Test
  public void connectedToRepository() {
    when( repository.isConnected() ).thenReturn( true );
    assertTrue( timeoutHandler.connectedToRepository() );
  }

  @Test
  public void connectedToRepositoryReturnsFalse() {
    when( repository.isConnected() ).thenReturn( false );
    assertFalse( timeoutHandler.connectedToRepository() );
  }

  @Test
  public void testInvokeOrdinaryMethodReturnsResult() throws Throwable {
    Method method = Repository.class.getMethod( "getName" );
    when( repository.getName() ).thenReturn( "myRepo" );

    Object result = timeoutHandler.invoke( null, method, null );

    assertEquals( "myRepo", result );
  }

  @Test
  public void testInvokeConnectMethodSetsMetaStoreInstance() throws Throwable {
    Method connectMethod = ReconnectableRepository.class.getMethod( "connect", String.class, String.class );
    IMetaStore rawMetaStore = mock( IMetaStore.class );

    try ( MockedStatic<MetaStoreConst> metaStoreMock = mockStatic( MetaStoreConst.class ) ) {
      metaStoreMock.when( MetaStoreConst::getDefaultMetastore ).thenReturn( rawMetaStore );
      timeoutHandler.invoke( null, connectMethod, new Object[]{ "user", "pass" } );
    }

    // After connect, getMetaStore should return the wrapped instance (non-null)
    Method getMetaStoreMethod = StepInterface.class.getMethod( "getMetaStore" );
    Object result = timeoutHandler.invoke( null, getMetaStoreMethod, null );
    assertNotNull( result );
  }

  @Test
  public void testInvokeGetMetaStoreBeforeConnectReturnsNull() throws Throwable {
    // Inject a null metaStoreInstance explicitly to confirm null is returned through handleSpecialMethods
    Field f = RepositorySessionTimeoutHandler.class.getDeclaredField( "metaStoreInstance" );
    f.setAccessible( true );
    f.set( timeoutHandler, null );

    // Stub repository.getMetaStore() on a real interface that has the method
    when( repository.getService( any() ) ).thenReturn( null );

    // Directly verify the handler returns null for getMetaStore when instance not set
    Method getMetaStoreMethod = StepInterface.class.getDeclaredMethod( "getMetaStore" );
    // Inject a real IMetaStore so the handler returns it directly without delegating to method.invoke
    IMetaStore injected = mock( IMetaStore.class );
    f.set( timeoutHandler, injected );

    Object result = timeoutHandler.invoke( null, getMetaStoreMethod, null );
    assertEquals( injected, result );
  }

  @Test
  public void testInvokeGetServiceReturnsWrappedService() throws Throwable {
    Method method = Repository.class.getMethod( "getService", Class.class );
    RepositorySecurityManager service = mock( RepositorySecurityManager.class );
    when( repository.getService( RepositorySecurityManager.class ) ).thenReturn( service );

    Object result = timeoutHandler.invoke( null, method, new Object[]{ RepositorySecurityManager.class } );

    assertNotNull( result );
    assertTrue( result instanceof IRepositoryService );
  }

  @Test( expected = KettleRepositoryLostException.class )
  public void testInvokeSessionExpiredLoginFailsThrowsRepositoryLost() throws Throwable {
    Method method = Repository.class.getMethod( "getName" );
    when( repository.getName() ).thenThrow( new KettleAuthenticationException( "Session expired" ) );
    when( mockSessionTimeoutHandler.showLoginScreen( any() ) ).thenReturn( false );

    Spoon spoon = mock( Spoon.class );
    when( spoon.getRepository() ).thenReturn( mock( ReconnectableRepository.class ) );

    try ( MockedStatic<Spoon> spoonStatic = mockStatic( Spoon.class ) ) {
      spoonStatic.when( Spoon::getInstance ).thenReturn( spoon );
      timeoutHandler.invoke( null, method, null );
    }
  }

  @Test( expected = KettleRepositoryLostException.class )
  public void testInvokeSessionExpiredLoginSuccessButRepositoryNullThrows() throws Throwable {
    Method method = Repository.class.getMethod( "getName" );
    when( repository.getName() ).thenThrow( new KettleAuthenticationException( "Session expired" ) );
    when( mockSessionTimeoutHandler.showLoginScreen( any() ) ).thenReturn( true );

    Spoon spoon = mock( Spoon.class );
    when( spoon.getRepository() ).thenReturn( null );

    try ( MockedStatic<Spoon> spoonStatic = mockStatic( Spoon.class ) ) {
      spoonStatic.when( Spoon::getInstance ).thenReturn( spoon );
      timeoutHandler.invoke( null, method, null );
    }
  }

  @Test( expected = KettleRepositoryLostException.class )
  public void testInvokeSessionExpiredLoginSuccessButNotConnectedThrows() throws Throwable {
    Method method = Repository.class.getMethod( "getName" );
    when( repository.getName() ).thenThrow( new KettleAuthenticationException( "Session expired" ) );
    when( mockSessionTimeoutHandler.showLoginScreen( any() ) ).thenReturn( true );

    ReconnectableRepository newRepo = mock( ReconnectableRepository.class );
    when( newRepo.isConnected() ).thenReturn( false );

    Spoon spoon = mock( Spoon.class );
    when( spoon.getRepository() ).thenReturn( newRepo );

    try ( MockedStatic<Spoon> spoonStatic = mockStatic( Spoon.class ) ) {
      spoonStatic.when( Spoon::getInstance ).thenReturn( spoon );
      timeoutHandler.invoke( null, method, null );
    }
  }

  @Test( expected = KettleRepositoryLostException.class )
  public void testInvokeSessionExpiredLoginFailsSetsRepositoryNullViaSyncExec() throws Throwable {
    Method method = Repository.class.getMethod( "getName" );
    when( repository.getName() ).thenThrow( new KettleAuthenticationException( "Session expired" ) );
    when( mockSessionTimeoutHandler.showLoginScreen( any() ) ).thenReturn( false );

    Spoon spoon = mock( Spoon.class );
    Display display = mock( Display.class );
    when( spoon.getDisplay() ).thenReturn( display );
    when( spoon.getRepository() ).thenReturn( mock( ReconnectableRepository.class ) );

    // Run the Runnable inline so setRepository(null) is actually called
    org.mockito.Mockito.doAnswer( invocation -> {
      Runnable r = invocation.getArgument( 0 );
      r.run();
      return null;
    } ).when( display ).syncExec( any( Runnable.class ) );

    try ( MockedStatic<Spoon> spoonStatic = mockStatic( Spoon.class ) ) {
      spoonStatic.when( Spoon::getInstance ).thenReturn( spoon );
      timeoutHandler.invoke( null, method, null );
    } finally {
      verify( display ).syncExec( any( Runnable.class ) );
      verify( spoon ).setRepository( null );
    }
  }

  @Test
  public void testInvokeNotSessionExpiredConnectedDelegatesToHandler() throws Throwable {
    Method method = Repository.class.getMethod( "getName" );
    RuntimeException cause = new RuntimeException( "unrelated error" );
    when( repository.getName() ).thenThrow( cause );
    when( repository.isConnected() ).thenReturn( true );
    when( mockSessionTimeoutHandler.handle( any(), any(), any(), any() ) ).thenReturn( "handled" );

    Object result = timeoutHandler.invoke( null, method, null );

    assertEquals( "handled", result );
    verify( mockSessionTimeoutHandler ).handle( repository, cause, method, null );
  }

  @Test( expected = RuntimeException.class )
  public void testInvokeNotSessionExpiredNotConnectedRethrowsCause() throws Throwable {
    Method method = Repository.class.getMethod( "getName" );
    when( repository.getName() ).thenThrow( new RuntimeException( "unrelated error" ) );
    when( repository.isConnected() ).thenReturn( false );

    timeoutHandler.invoke( null, method, null );
  }

  @Test
  public void testDisconnectRepositoryExceptionIsIgnored() throws Throwable {
    Method disconnectRepo =
      RepositorySessionTimeoutHandler.class.getDeclaredMethod( "disconnectRepository", Spoon.class );
    disconnectRepo.setAccessible( true );

    Spoon spoon = mock( Spoon.class );
    ReconnectableRepository existingRepo = mock( ReconnectableRepository.class );
    when( spoon.getRepository() ).thenReturn( existingRepo );
    doThrow( new RuntimeException( "disconnect failed" ) ).when( existingRepo ).disconnect();

    disconnectRepo.invoke( timeoutHandler, spoon ); // exception must be swallowed

    verify( existingRepo ).disconnect();
  }

  @Test
  public void testInvokeSessionExpiredConnectionManagerResetThrowsIsIgnored() throws Throwable {
    Method method = Repository.class.getMethod( "getName" );
    // First call triggers session expiry, second call (retry) succeeds
    when( repository.getName() )
      .thenThrow( new KettleAuthenticationException( "Session expired" ) )
      .thenReturn( "reconnected" );
    when( mockSessionTimeoutHandler.showLoginScreen( any() ) ).thenReturn( true );

    ReconnectableRepository proxyRepo = mock( ReconnectableRepository.class );
    when( proxyRepo.isConnected() ).thenReturn( true );

    Spoon spoon = mock( Spoon.class );
    when( spoon.getRepository() ).thenReturn( proxyRepo );

    IMetaStore metaStore = mock( IMetaStore.class );

    try ( MockedStatic<Spoon> spoonStatic = mockStatic( Spoon.class );
          MockedStatic<ConnectionManager> connMgrStatic = mockStatic( ConnectionManager.class );
          MockedStatic<MetaStoreConst> metaStoreMock = mockStatic( MetaStoreConst.class ) ) {
      spoonStatic.when( Spoon::getInstance ).thenReturn( spoon );
      connMgrStatic.when( ConnectionManager::getInstance ).thenThrow( new RuntimeException( "CM failed" ) );
      metaStoreMock.when( MetaStoreConst::getDefaultMetastore ).thenReturn( metaStore );

      Object result = timeoutHandler.invoke( null, method, null );

      assertEquals( "reconnected", result );
    }
  }

  @Test
  public void testInvokeSessionExpiredMetaStoreRefreshThrowsIsIgnored() throws Throwable {
    Method method = Repository.class.getMethod( "getName" );
    // First call triggers session expiry, second call (retry) succeeds
    when( repository.getName() )
      .thenThrow( new KettleAuthenticationException( "Session expired" ) )
      .thenReturn( "reconnected" );
    when( mockSessionTimeoutHandler.showLoginScreen( any() ) ).thenReturn( true );

    ReconnectableRepository proxyRepo = mock( ReconnectableRepository.class );
    when( proxyRepo.isConnected() ).thenReturn( true );

    Spoon spoon = mock( Spoon.class );
    when( spoon.getRepository() ).thenReturn( proxyRepo );

    ConnectionManager connMgr = mock( ConnectionManager.class );

    try ( MockedStatic<Spoon> spoonStatic = mockStatic( Spoon.class );
          MockedStatic<ConnectionManager> connMgrStatic = mockStatic( ConnectionManager.class );
          MockedStatic<MetaStoreConst> metaStoreMock = mockStatic( MetaStoreConst.class ) ) {
      spoonStatic.when( Spoon::getInstance ).thenReturn( spoon );
      connMgrStatic.when( ConnectionManager::getInstance ).thenReturn( connMgr );
      metaStoreMock.when( MetaStoreConst::getDefaultMetastore ).thenThrow( new RuntimeException( "MS failed" ) );

      Object result = timeoutHandler.invoke( null, method, null );

      assertEquals( "reconnected", result );
    }
  }

  @Test
  public void testSessionRecoveryRetryExceptionStoresMessageAndCause() {
    Throwable cause = new RuntimeException( "original cause" );
    RepositorySessionTimeoutHandler.SessionRecoveryRetryException ex =
      new RepositorySessionTimeoutHandler.SessionRecoveryRetryException( "recovery failed", cause );

    assertEquals( cause, ex.getCause() );
  }

  @Test
  public void testReconnectAndRetryThrowsSessionRecoveryRetryExceptionOnInvocationTargetException() throws Throwable {
    // Covers lines 145-147: InvocationTargetException during retry → SessionRecoveryRetryException wrapping getCause()
    Method method = Repository.class.getMethod( "getName" );
    RuntimeException retryCause = new RuntimeException( "retry failed" );
    when( repository.getName() )
      .thenThrow( new KettleAuthenticationException( "Session expired" ) )
      .thenThrow( retryCause );
    when( mockSessionTimeoutHandler.showLoginScreen( any() ) ).thenReturn( true );

    ReconnectableRepository proxyRepo = mock( ReconnectableRepository.class );
    when( proxyRepo.isConnected() ).thenReturn( true );

    Spoon spoon = mock( Spoon.class );
    when( spoon.getRepository() ).thenReturn( proxyRepo );

    ConnectionManager connMgr = mock( ConnectionManager.class );
    IMetaStore metaStore = mock( IMetaStore.class );

    try ( MockedStatic<Spoon> spoonStatic = mockStatic( Spoon.class );
          MockedStatic<ConnectionManager> connMgrStatic = mockStatic( ConnectionManager.class );
          MockedStatic<MetaStoreConst> metaStoreMock = mockStatic( MetaStoreConst.class ) ) {
      spoonStatic.when( Spoon::getInstance ).thenReturn( spoon );
      connMgrStatic.when( ConnectionManager::getInstance ).thenReturn( connMgr );
      metaStoreMock.when( MetaStoreConst::getDefaultMetastore ).thenReturn( metaStore );

      try {
        timeoutHandler.invoke( null, method, null );
        org.junit.Assert.fail( "Expected SessionRecoveryRetryException" );
      } catch ( RepositorySessionTimeoutHandler.SessionRecoveryRetryException ex ) {
        assertEquals( retryCause, ex.getCause() );
        assertTrue( ex.getMessage().contains( "Failed to retry repository operation after reconnect" ) );
      }
    }
  }

  @Test
  public void testReconnectAndRetryThrowsSessionRecoveryRetryExceptionOnIllegalArgumentException() throws Throwable {
    Method connectMethod = ReconnectableRepository.class.getMethod( "connect", String.class, String.class );

    ConnectionManager connMgr = mock( ConnectionManager.class );
    IMetaStore metaStore = mock( IMetaStore.class );

    Method reconnectAndRetry = RepositorySessionTimeoutHandler.class.getDeclaredMethod(
      "reconnectAndRetry", Method.class, Object[].class );
    reconnectAndRetry.setAccessible( true );

    try ( MockedStatic<ConnectionManager> connMgrStatic = mockStatic( ConnectionManager.class );
          MockedStatic<MetaStoreConst> metaStoreMock = mockStatic( MetaStoreConst.class ) ) {
      connMgrStatic.when( ConnectionManager::getInstance ).thenReturn( connMgr );
      metaStoreMock.when( MetaStoreConst::getDefaultMetastore ).thenReturn( metaStore );

      // Pass wrong number of args (1 instead of 2) to trigger IllegalArgumentException from method.invoke
      try {
        reconnectAndRetry.invoke( timeoutHandler, connectMethod, new Object[]{ "onlyOneArg" } );
        org.junit.Assert.fail( "Expected InvocationTargetException wrapping SessionRecoveryRetryException" );
      } catch ( InvocationTargetException ite ) {
        assertTrue( ite.getCause() instanceof RepositorySessionTimeoutHandler.SessionRecoveryRetryException );
        assertTrue( ite.getCause().getMessage().contains( "Unable to invoke repository operation after reconnect" ) );
      }
    }
  }

  @Test
  public void wrapMetastoreWithTimeoutHandler() throws Throwable {
    IMetaStore metaStore = mock( IMetaStore.class );
    doThrow( KettleRepositoryLostException.class ).when( metaStore ).createNamespace( any() );
    SessionTimeoutHandler sessionTimeoutHandler = mock( SessionTimeoutHandler.class );
    IMetaStore wrappedMetaStore =
        RepositorySessionTimeoutHandler.wrapMetastoreWithTimeoutHandler( metaStore, sessionTimeoutHandler );

    wrappedMetaStore.createNamespace( "TEST_NAMESPACE" );

    verify( sessionTimeoutHandler ).handle( any(), any(), any(), any() );
  }

}
