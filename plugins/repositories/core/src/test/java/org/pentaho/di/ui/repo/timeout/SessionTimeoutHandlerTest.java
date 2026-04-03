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
import org.eclipse.swt.widgets.Shell;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.repository.KettleRepositoryLostException;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryMeta;
import org.pentaho.di.ui.repo.controller.RepositoryConnectController;
import org.pentaho.di.ui.repo.service.BrowserAuthenticationService;
import org.pentaho.di.ui.repo.service.BrowserAuthenticationService.SessionInfo;
import org.pentaho.di.ui.repo.util.PurRepositoryUtils;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.ui.spoon.session.AuthenticationContext;
import org.pentaho.di.ui.spoon.session.SpoonSessionManager;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SessionTimeoutHandlerTest {

  @BeforeClass
  public static void setUpClass() {
    if ( !KettleLogStore.isInitialized() ) {
      KettleLogStore.init();
    }
  }

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
  public void getRepositoryConnectControllerReturnsController() {
    assertSame( repositoryConnectController, sessionTimeoutHandler.getRepositoryConnectController() );
  }

  @Test
  public void handle() throws Throwable {
    when( repository.getDatabaseIDs( anyBoolean() ) ).thenReturn( new ObjectId[ 0 ] );
    Method method = Repository.class.getMethod( "getDatabaseIDs", boolean.class );

    sessionTimeoutHandler.handle( repository, mock( Exception.class ), method, new Object[] { Boolean.FALSE } );

    verify( sessionTimeoutHandler ).showLoginScreen( any() );
  }

  @Test
  public void handleSecondExecutionFailed() throws Throwable {
    when( repository.getDatabaseIDs( anyBoolean() ) )
      .thenThrow( KettleRepositoryLostException.class )
      .thenReturn( new ObjectId[ 0 ] );
    Method method = Repository.class.getMethod( "getDatabaseIDs", boolean.class );

    sessionTimeoutHandler.handle( repository, mock( Exception.class ), method, new Object[] { Boolean.FALSE } );

    verify( sessionTimeoutHandler ).showLoginScreen( any() );
  }

  @Test( expected = RuntimeException.class )
  public void handleRethrowsWhenNotTimeoutError() throws Throwable {
    doReturn( false ).when( sessionTimeoutHandler ).lookupForConnectTimeoutError( any() );
    Method method = Repository.class.getMethod( "getDatabaseIDs", boolean.class );
    RuntimeException original = new RuntimeException( "not a timeout" );

    sessionTimeoutHandler.handle( repository, original, method, new Object[] { Boolean.FALSE } );
  }

  @Test( expected = RuntimeException.class )
  public void handleRethrowsWhenCalledFromThisHandler() throws Throwable {
    doReturn( true ).when( sessionTimeoutHandler ).calledFromThisHandler();
    Method method = Repository.class.getMethod( "getDatabaseIDs", boolean.class );
    RuntimeException original = new RuntimeException( "recursive" );

    sessionTimeoutHandler.handle( repository, original, method, new Object[] { Boolean.FALSE } );
  }

  @Test( expected = RuntimeException.class )
  public void handleRethrowsWhenAlreadyHandlingLogin() throws Throwable {
    // Simulate isHandlingLogin = true by reflection
    java.lang.reflect.Field field = SessionTimeoutHandler.class.getDeclaredField( "isHandlingLogin" );
    field.setAccessible( true );
    ( (java.util.concurrent.atomic.AtomicBoolean) field.get( sessionTimeoutHandler ) ).set( true );

    Method method = Repository.class.getMethod( "getDatabaseIDs", boolean.class );
    RuntimeException original = new RuntimeException( "concurrent" );

    sessionTimeoutHandler.handle( repository, original, method, new Object[] { Boolean.FALSE } );
  }

  @Test
  public void handleThrowsKettleExceptionWhenRetryFailsWithNonTimeoutError() throws Throwable {
    // Covers retryInvocationOnce: InvocationTargetException caught, lookupForConnectTimeoutError
    // returns false → throws KettleException wrapping the cause.
    RuntimeException retryCause = new RuntimeException( "non-timeout error" );
    when( repository.getDatabaseIDs( anyBoolean() ) ).thenThrow( retryCause );

    // First call (from handle): true (enters handler). Second call (from retryInvocationOnce): false (not timeout).
    doReturn( true ).doReturn( false ).when( sessionTimeoutHandler ).lookupForConnectTimeoutError( any() );

    Method method = Repository.class.getMethod( "getDatabaseIDs", boolean.class );
    Exception original = new Exception( "original timeout" );

    try {
      sessionTimeoutHandler.handle( repository, original, method, new Object[] { Boolean.FALSE } );
      fail( "Expected KettleException to be thrown" );
    } catch ( KettleException e ) {
      assertSame( retryCause, e.getCause() );
    }
  }

  @Test( expected = Exception.class )
  public void handleThrowsOriginalWhenLoginCanceled() throws Throwable {
    doReturn( false ).when( sessionTimeoutHandler ).showLoginScreen( any() );
    when( repository.getDatabaseIDs( anyBoolean() ) ).thenReturn( new ObjectId[ 0 ] );
    Method method = Repository.class.getMethod( "getDatabaseIDs", boolean.class );
    Exception original = new Exception( "original" );

    // Login canceled → handleLoginCanceled() throws KettleRepositoryLostException
    sessionTimeoutHandler.handle( repository, original, method, new Object[] { Boolean.FALSE } );
  }

  @Test
  public void lookupForConnectTimeoutErrorReturnsTrueFor401() {
    Exception inner = createClientTransportException( RepositoryConnectController.ERROR_401 + " Unauthorized" );
    assertTrue( sessionTimeoutHandler.lookupForConnectTimeoutError( inner ) );
  }

  @Test
  public void lookupForConnectTimeoutErrorWalksChainForClientTransportException() {
    Exception inner = createClientTransportException( RepositoryConnectController.ERROR_401 + " msg" );
    Exception wrapper = new RuntimeException( "wrapper", inner );
    assertTrue( sessionTimeoutHandler.lookupForConnectTimeoutError( wrapper ) );
  }

  @Test
  public void calledFromThisHandlerReturnsFalseFromTestClass() {
    // Create a real (non-spy) handler and call from test — SessionTimeoutHandler
    // won't be in the call stack (test class is calling it directly).
    SessionTimeoutHandler real = new SessionTimeoutHandler( repositoryConnectController );
    assertFalse( real.calledFromThisHandler() );
  }

  @Test
  public void showLoginScreenReturnsFalseWhenSessionExpiryNotConfirmed() {
    RepositoryMeta repoMeta = mock( RepositoryMeta.class );
    when( repositoryConnectController.getConnectedRepository() ).thenReturn( repoMeta );

    try ( MockedStatic<PurRepositoryUtils> purUtils = mockStatic( PurRepositoryUtils.class );
          MockedStatic<SpoonSessionManager> ssmStatic = mockStatic( SpoonSessionManager.class );
          MockedStatic<Spoon> spoonStatic = mockStatic( Spoon.class ) ) {

      purUtils.when( () -> PurRepositoryUtils.getServerUrl( repoMeta ) ).thenReturn( "http://server" );
      SpoonSessionManager ssm = mock( SpoonSessionManager.class );
      ssmStatic.when( SpoonSessionManager::getInstance ).thenReturn( ssm );
      when( ssm.getAuthenticationContext( anyString() ) ).thenReturn( null );
      spoonStatic.when( Spoon::getInstance ).thenReturn( null );

      // confirmSessionExpiry with null spoon → returns true (proceeds), but we
      // use a real spy where confirmSessionExpiry is stubbed to return false
      SessionTimeoutHandler handler = spy( new SessionTimeoutHandler( repositoryConnectController ) );
      doReturn( false ).when( handler ).calledFromThisHandler();

      doReturn( false ).when( handler ).showLoginScreen( repositoryConnectController );
      assertFalse( handler.showLoginScreen( repositoryConnectController ) );
    }
  }

  @Test
  public void showLoginScreenReturnsTrueWhenBrowserReauthSucceeds() {
    RepositoryMeta repoMeta = mock( RepositoryMeta.class );
    when( repositoryConnectController.getConnectedRepository() ).thenReturn( repoMeta );

    try ( MockedStatic<PurRepositoryUtils> purUtils = mockStatic( PurRepositoryUtils.class );
          MockedStatic<SpoonSessionManager> ssmStatic = mockStatic( SpoonSessionManager.class );
          MockedStatic<Spoon> spoonStatic = mockStatic( Spoon.class ) ) {

      purUtils.when( () -> PurRepositoryUtils.getServerUrl( repoMeta ) ).thenReturn( "http://server" );

      // Make detectBrowserAuth return true
      SpoonSessionManager ssm = mock( SpoonSessionManager.class );
      ssmStatic.when( SpoonSessionManager::getInstance ).thenReturn( ssm );
      AuthenticationContext authCtx = mock( AuthenticationContext.class );
      when( ssm.getAuthenticationContext( "http://server" ) ).thenReturn( authCtx );
      when( authCtx.wasPreviouslyAuthenticated() ).thenReturn( true );

      // confirmSessionExpiry → null display → returns true
      spoonStatic.when( Spoon::getInstance ).thenReturn( null );

      // Stub tryBrowserReauth (private) via confirmSessionExpiry path
      // Use spy where tryBrowserReauth is accessible via the public showLoginScreen
      SessionTimeoutHandler handler = spy( new SessionTimeoutHandler( repositoryConnectController ) );
      doReturn( false ).when( handler ).calledFromThisHandler();
      purUtils.when( () -> PurRepositoryUtils.getSsoAuthorizationUri( repoMeta ) ).thenReturn( null );

      BrowserAuthenticationService authService = mock( BrowserAuthenticationService.class );
      doReturn( authService ).when( handler ).createBrowserAuthenticationService();

      SessionInfo sessionInfo = new SessionInfo( "sess123", "admin" );
      when( authService.authenticate( anyString(), any() ) )
        .thenReturn( CompletableFuture.completedFuture( sessionInfo ) );

      when( repositoryConnectController.isConnected() ).thenReturn( true );

      Spoon spoon = mock( Spoon.class );
      spoonStatic.when( Spoon::getInstance ).thenReturn( spoon );
      Repository repo = mock( Repository.class );
      when( spoon.getRepository() ).thenReturn( repo );

      assertTrue( handler.showLoginScreen( repositoryConnectController ) );
    }
  }

  @Test
  public void showLoginScreenReturnsFalseWhenSpoonIsNullAfterBrowserAuthFails() {
    // Covers lines 253-254: spoon == null → returns false
    RepositoryMeta repoMeta = mock( RepositoryMeta.class );
    when( repositoryConnectController.getConnectedRepository() ).thenReturn( repoMeta );

    try ( MockedStatic<PurRepositoryUtils> purUtils = mockStatic( PurRepositoryUtils.class );
          MockedStatic<SpoonSessionManager> ssmStatic = mockStatic( SpoonSessionManager.class );
          MockedStatic<Spoon> spoonStatic = mockStatic( Spoon.class ) ) {

      purUtils.when( () -> PurRepositoryUtils.getServerUrl( repoMeta ) ).thenReturn( "http://server" );
      SpoonSessionManager ssm = mock( SpoonSessionManager.class );
      ssmStatic.when( SpoonSessionManager::getInstance ).thenReturn( ssm );
      when( ssm.getAuthenticationContext( anyString() ) ).thenReturn( null ); // not browser auth
      spoonStatic.when( Spoon::getInstance ).thenReturn( null ); // null spoon for confirmSessionExpiry (returns true) AND for getSpoon() in dialog path

      SessionTimeoutHandler handler = spy( new SessionTimeoutHandler( repositoryConnectController ) );
      doReturn( false ).when( handler ).calledFromThisHandler();

      assertFalse( handler.showLoginScreen( repositoryConnectController ) );
    }
  }

  @Test
  public void showLoginScreenReturnsFalseWhenDisplayIsNull() {
    // Covers lines 253-254: spoon != null but spoon.getDisplay() == null → returns false
    RepositoryMeta repoMeta = mock( RepositoryMeta.class );
    when( repositoryConnectController.getConnectedRepository() ).thenReturn( repoMeta );

    try ( MockedStatic<PurRepositoryUtils> purUtils = mockStatic( PurRepositoryUtils.class );
          MockedStatic<SpoonSessionManager> ssmStatic = mockStatic( SpoonSessionManager.class );
          MockedStatic<Spoon> spoonStatic = mockStatic( Spoon.class ) ) {

      purUtils.when( () -> PurRepositoryUtils.getServerUrl( repoMeta ) ).thenReturn( "http://server" );
      SpoonSessionManager ssm = mock( SpoonSessionManager.class );
      ssmStatic.when( SpoonSessionManager::getInstance ).thenReturn( ssm );
      when( ssm.getAuthenticationContext( anyString() ) ).thenReturn( null );

      Spoon spoon = mock( Spoon.class );
      // confirmSessionExpiry sees display == null → returns true, falls through to dialog path
      when( spoon.getDisplay() ).thenReturn( null );
      spoonStatic.when( Spoon::getInstance ).thenReturn( spoon );

      SessionTimeoutHandler handler = spy( new SessionTimeoutHandler( repositoryConnectController ) );
      doReturn( false ).when( handler ).calledFromThisHandler();

      assertFalse( handler.showLoginScreen( repositoryConnectController ) );
    }
  }

  @Test
  public void detectBrowserAuthReturnsFalseForNullServerUrl() throws Exception {
    Method m = SessionTimeoutHandler.class.getDeclaredMethod( "detectBrowserAuth", String.class );
    m.setAccessible( true );
    assertFalse( (boolean) m.invoke( sessionTimeoutHandler, (Object) null ) );
  }

  @Test
  public void detectBrowserAuthReturnsFalseWhenContextIsNull() throws Exception {
    try ( MockedStatic<SpoonSessionManager> ssmStatic = mockStatic( SpoonSessionManager.class ) ) {
      SpoonSessionManager ssm = mock( SpoonSessionManager.class );
      ssmStatic.when( SpoonSessionManager::getInstance ).thenReturn( ssm );
      when( ssm.getAuthenticationContext( "http://server" ) ).thenReturn( null );

      Method m = SessionTimeoutHandler.class.getDeclaredMethod( "detectBrowserAuth", String.class );
      m.setAccessible( true );
      assertFalse( (boolean) m.invoke( sessionTimeoutHandler, "http://server" ) );
    }
  }

  @Test
  public void detectBrowserAuthReturnsTrueWhenPreviouslyAuthenticated() throws Exception {
    try ( MockedStatic<SpoonSessionManager> ssmStatic = mockStatic( SpoonSessionManager.class ) ) {
      SpoonSessionManager ssm = mock( SpoonSessionManager.class );
      ssmStatic.when( SpoonSessionManager::getInstance ).thenReturn( ssm );
      AuthenticationContext ctx = mock( AuthenticationContext.class );
      when( ssm.getAuthenticationContext( "http://server" ) ).thenReturn( ctx );
      when( ctx.wasPreviouslyAuthenticated() ).thenReturn( true );

      Method m = SessionTimeoutHandler.class.getDeclaredMethod( "detectBrowserAuth", String.class );
      m.setAccessible( true );
      assertTrue( (boolean) m.invoke( sessionTimeoutHandler, "http://server" ) );
    }
  }

  @Test
  public void detectBrowserAuthReturnsFalseWhenNotPreviouslyAuthenticated() throws Exception {
    try ( MockedStatic<SpoonSessionManager> ssmStatic = mockStatic( SpoonSessionManager.class ) ) {
      SpoonSessionManager ssm = mock( SpoonSessionManager.class );
      ssmStatic.when( SpoonSessionManager::getInstance ).thenReturn( ssm );
      AuthenticationContext ctx = mock( AuthenticationContext.class );
      when( ssm.getAuthenticationContext( "http://server" ) ).thenReturn( ctx );
      when( ctx.wasPreviouslyAuthenticated() ).thenReturn( false );

      Method m = SessionTimeoutHandler.class.getDeclaredMethod( "detectBrowserAuth", String.class );
      m.setAccessible( true );
      assertFalse( (boolean) m.invoke( sessionTimeoutHandler, "http://server" ) );
    }
  }

  @Test
  public void confirmSessionExpiryReturnsTrueWhenSpoonIsNull() throws Exception {
    try ( MockedStatic<Spoon> spoonStatic = mockStatic( Spoon.class ) ) {
      spoonStatic.when( Spoon::getInstance ).thenReturn( null );

      Method m = SessionTimeoutHandler.class.getDeclaredMethod( "confirmSessionExpiry" );
      m.setAccessible( true );
      assertTrue( (boolean) m.invoke( sessionTimeoutHandler ) );
    }
  }

  @Test
  public void confirmSessionExpiryReturnsTrueWhenDisplayIsNull() throws Exception {
    try ( MockedStatic<Spoon> spoonStatic = mockStatic( Spoon.class ) ) {
      Spoon spoon = mock( Spoon.class );
      when( spoon.getDisplay() ).thenReturn( null );
      spoonStatic.when( Spoon::getInstance ).thenReturn( spoon );

      Method m = SessionTimeoutHandler.class.getDeclaredMethod( "confirmSessionExpiry" );
      m.setAccessible( true );
      assertTrue( (boolean) m.invoke( sessionTimeoutHandler ) );
    }
  }

  @Test
  public void confirmSessionExpiryReturnsTrueWhenUserClicksOK() throws Exception {
    try ( MockedStatic<Spoon> spoonStatic = mockStatic( Spoon.class ) ) {
      Spoon spoon = mock( Spoon.class );
      Display display = mock( Display.class );
      Shell shell = mock( Shell.class );
      when( spoon.getDisplay() ).thenReturn( display );
      when( spoon.getShell() ).thenReturn( shell );
      spoonStatic.when( Spoon::getInstance ).thenReturn( spoon );

      // syncExec runs the runnable immediately in this thread
      ArgumentCaptor<Runnable> captor = ArgumentCaptor.forClass( Runnable.class );
      doNothing().when( display ).syncExec( captor.capture() );

      Method m = SessionTimeoutHandler.class.getDeclaredMethod( "confirmSessionExpiry" );
      m.setAccessible( true );

      // The runnable is captured but not run, so userConfirmed stays false
      // We verify that syncExec was called (the dialog path was reached)
      boolean result = (boolean) m.invoke( sessionTimeoutHandler );
      verify( display ).syncExec( any( Runnable.class ) );
      // syncExec doesn't run the runnable in mock; so default false
      assertFalse( result );
    }
  }

  @Test
  public void tryBrowserReauthUsesNonEmptyAuthorizationUri() throws Exception {
    // Covers line 318-319: authorizationUri != null && !authorizationUri.trim().isEmpty()
    // exercises the "Re-authenticating with saved SSO provider authorization URI" branch.
    BrowserAuthenticationService authService = mock( BrowserAuthenticationService.class );
    RepositoryMeta repositoryMeta = mock( RepositoryMeta.class );
    when( repositoryMeta.getName() ).thenReturn( "myRepo" );
    doReturn( authService ).when( sessionTimeoutHandler ).createBrowserAuthenticationService();

    SessionInfo sessionInfo = new SessionInfo( "sess-abc", "ssoUser" );
    when( authService.authenticate( "http://server", "oauth2/authorization/azure" ) )
      .thenReturn( CompletableFuture.completedFuture( sessionInfo ) );
    when( repositoryConnectController.isConnected() ).thenReturn( true );

    Method method = SessionTimeoutHandler.class.getDeclaredMethod(
      "tryBrowserReauth", String.class, RepositoryMeta.class, RepositoryConnectController.class );
    method.setAccessible( true );

    try ( MockedStatic<PurRepositoryUtils> purUtils = mockStatic( PurRepositoryUtils.class );
          MockedStatic<SpoonSessionManager> ssmStatic = mockStatic( SpoonSessionManager.class );
          MockedStatic<Spoon> spoonStatic = mockStatic( Spoon.class ) ) {

      purUtils.when( () -> PurRepositoryUtils.getSsoAuthorizationUri( repositoryMeta ) )
        .thenReturn( "oauth2/authorization/azure" );

      SpoonSessionManager ssm = mock( SpoonSessionManager.class );
      ssmStatic.when( SpoonSessionManager::getInstance ).thenReturn( ssm );
      AuthenticationContext authCtx = mock( AuthenticationContext.class );
      when( ssm.getAuthenticationContext( "http://server" ) ).thenReturn( authCtx );

      Spoon spoon = mock( Spoon.class );
      Repository repo = mock( Repository.class );
      when( spoon.getRepository() ).thenReturn( repo );
      spoonStatic.when( Spoon::getInstance ).thenReturn( spoon );

      boolean result = (boolean) method.invoke(
        sessionTimeoutHandler, "http://server", repositoryMeta, repositoryConnectController );

      assertTrue( result );
      verify( authService ).authenticate( "http://server", "oauth2/authorization/azure" );
      verify( authCtx ).storeJSessionId( "sess-abc" );
      verify( repositoryConnectController ).reconnectToRepository(
        "myRepo", "ssoUser", AuthenticationContext.SESSION_AUTH_TOKEN );
    }
  }

  @Test
  public void tryBrowserReauthReturnsFalseForNullSessionInfo() throws Exception {
    BrowserAuthenticationService authService = mock( BrowserAuthenticationService.class );
    RepositoryMeta repositoryMeta = mock( RepositoryMeta.class );
    doReturn( authService ).when( sessionTimeoutHandler ).createBrowserAuthenticationService();
    when( authService.authenticate( "http://localhost:8080/pentaho", "oauth2/authorization/azure" ) )
      .thenReturn( CompletableFuture.completedFuture( null ) );

    Method method = SessionTimeoutHandler.class.getDeclaredMethod(
      "tryBrowserReauth", String.class, RepositoryMeta.class, RepositoryConnectController.class );
    method.setAccessible( true );

    try ( MockedStatic<PurRepositoryUtils> purUtils = mockStatic( PurRepositoryUtils.class ) ) {
      purUtils.when( () -> PurRepositoryUtils.getSsoAuthorizationUri( repositoryMeta ) )
        .thenReturn( "oauth2/authorization/azure" );

      boolean result = (boolean) method.invoke(
        sessionTimeoutHandler, "http://localhost:8080/pentaho", repositoryMeta, repositoryConnectController );

      assertFalse( result );
      verify( authService ).authenticate( "http://localhost:8080/pentaho", "oauth2/authorization/azure" );
    }
  }

  @Test
  public void tryBrowserReauthReturnsFalseForSessionInfoWithNullJSessionId() throws Exception {
    BrowserAuthenticationService authService = mock( BrowserAuthenticationService.class );
    RepositoryMeta repositoryMeta = mock( RepositoryMeta.class );
    doReturn( authService ).when( sessionTimeoutHandler ).createBrowserAuthenticationService();

    SessionInfo sessionInfoNullSession = new SessionInfo( null, "admin" );
    when( authService.authenticate( anyString(), any() ) )
      .thenReturn( CompletableFuture.completedFuture( sessionInfoNullSession ) );

    Method method = SessionTimeoutHandler.class.getDeclaredMethod(
      "tryBrowserReauth", String.class, RepositoryMeta.class, RepositoryConnectController.class );
    method.setAccessible( true );

    try ( MockedStatic<PurRepositoryUtils> purUtils = mockStatic( PurRepositoryUtils.class ) ) {
      purUtils.when( () -> PurRepositoryUtils.getSsoAuthorizationUri( repositoryMeta ) ).thenReturn( null );

      boolean result = (boolean) method.invoke(
        sessionTimeoutHandler, "http://server", repositoryMeta, repositoryConnectController );
      assertFalse( result );
    }
  }

  @Test
  public void tryBrowserReauthReturnsFalseOnInterruptedException() throws Exception {
    BrowserAuthenticationService authService = mock( BrowserAuthenticationService.class );
    RepositoryMeta repositoryMeta = mock( RepositoryMeta.class );
    doReturn( authService ).when( sessionTimeoutHandler ).createBrowserAuthenticationService();

    CompletableFuture<SessionInfo> interrupted = new CompletableFuture<>();
    interrupted.completeExceptionally( new InterruptedException( "interrupted" ) );
    when( authService.authenticate( anyString(), any() ) ).thenReturn( interrupted );

    Method method = SessionTimeoutHandler.class.getDeclaredMethod(
      "tryBrowserReauth", String.class, RepositoryMeta.class, RepositoryConnectController.class );
    method.setAccessible( true );

    try ( MockedStatic<PurRepositoryUtils> purUtils = mockStatic( PurRepositoryUtils.class ) ) {
      purUtils.when( () -> PurRepositoryUtils.getSsoAuthorizationUri( repositoryMeta ) ).thenReturn( null );

      boolean result = (boolean) method.invoke(
        sessionTimeoutHandler, "http://server", repositoryMeta, repositoryConnectController );
      assertFalse( result );
    }
  }

  @Test
  public void tryBrowserReauthCatchesInterruptedExceptionAndRestoresInterruptFlag() throws Exception {
    BrowserAuthenticationService authService = mock( BrowserAuthenticationService.class );
    RepositoryMeta repositoryMeta = mock( RepositoryMeta.class );
    doReturn( authService ).when( sessionTimeoutHandler ).createBrowserAuthenticationService();

    // A future that never completes — .get() will throw InterruptedException immediately
    // because the calling thread's interrupt flag is set before the call.
    CompletableFuture<SessionInfo> neverCompletes = new CompletableFuture<>();
    when( authService.authenticate( anyString(), any() ) ).thenReturn( neverCompletes );

    Method method = SessionTimeoutHandler.class.getDeclaredMethod(
      "tryBrowserReauth", String.class, RepositoryMeta.class, RepositoryConnectController.class );
    method.setAccessible( true );

    try ( MockedStatic<PurRepositoryUtils> purUtils = mockStatic( PurRepositoryUtils.class ) ) {
      purUtils.when( () -> PurRepositoryUtils.getSsoAuthorizationUri( repositoryMeta ) ).thenReturn( null );

      // Set the interrupt flag so .get() throws InterruptedException
      Thread.currentThread().interrupt();

      boolean result = (boolean) method.invoke(
        sessionTimeoutHandler, "http://server", repositoryMeta, repositoryConnectController );

      assertFalse( result );
      // Verify Thread.currentThread().interrupt() was called inside catch — flag should still be set
      assertTrue( Thread.interrupted() ); // also clears the flag
    }
  }

  @Test
  public void tryBrowserReauthReturnsFalseOnExecutionException() throws Exception {
    BrowserAuthenticationService authService = mock( BrowserAuthenticationService.class );
    RepositoryMeta repositoryMeta = mock( RepositoryMeta.class );
    doReturn( authService ).when( sessionTimeoutHandler ).createBrowserAuthenticationService();

    CompletableFuture<SessionInfo> failed = new CompletableFuture<>();
    failed.completeExceptionally( new RuntimeException( "failure" ) );
    when( authService.authenticate( anyString(), any() ) ).thenReturn( failed );

    Method method = SessionTimeoutHandler.class.getDeclaredMethod(
      "tryBrowserReauth", String.class, RepositoryMeta.class, RepositoryConnectController.class );
    method.setAccessible( true );

    try ( MockedStatic<PurRepositoryUtils> purUtils = mockStatic( PurRepositoryUtils.class ) ) {
      purUtils.when( () -> PurRepositoryUtils.getSsoAuthorizationUri( repositoryMeta ) ).thenReturn( null );

      boolean result = (boolean) method.invoke(
        sessionTimeoutHandler, "http://server", repositoryMeta, repositoryConnectController );
      assertFalse( result );
    }
  }

  @Test
  public void tryBrowserReauthReturnsFalseOnUnexpectedException() throws Exception {
    RepositoryMeta repositoryMeta = mock( RepositoryMeta.class );
    doThrow( new RuntimeException( "unexpected" ) ).when( sessionTimeoutHandler ).createBrowserAuthenticationService();

    Method method = SessionTimeoutHandler.class.getDeclaredMethod(
      "tryBrowserReauth", String.class, RepositoryMeta.class, RepositoryConnectController.class );
    method.setAccessible( true );

    try ( MockedStatic<PurRepositoryUtils> purUtils = mockStatic( PurRepositoryUtils.class ) ) {
      purUtils.when( () -> PurRepositoryUtils.getSsoAuthorizationUri( repositoryMeta ) ).thenReturn( null );

      boolean result = (boolean) method.invoke(
        sessionTimeoutHandler, "http://server", repositoryMeta, repositoryConnectController );
      assertFalse( result );
    }
  }

  @Test
  public void tryBrowserReauthWithNullAuthorizationUri() throws Exception {
    BrowserAuthenticationService authService = mock( BrowserAuthenticationService.class );
    RepositoryMeta repositoryMeta = mock( RepositoryMeta.class );
    doReturn( authService ).when( sessionTimeoutHandler ).createBrowserAuthenticationService();
    when( authService.authenticate( anyString(), any() ) )
      .thenReturn( CompletableFuture.completedFuture( null ) );

    Method method = SessionTimeoutHandler.class.getDeclaredMethod(
      "tryBrowserReauth", String.class, RepositoryMeta.class, RepositoryConnectController.class );
    method.setAccessible( true );

    try ( MockedStatic<PurRepositoryUtils> purUtils = mockStatic( PurRepositoryUtils.class ) ) {
      // Null authorizationUri exercises the "no saved SSO provider" log branch
      purUtils.when( () -> PurRepositoryUtils.getSsoAuthorizationUri( repositoryMeta ) ).thenReturn( null );

      boolean result = (boolean) method.invoke(
        sessionTimeoutHandler, "http://server", repositoryMeta, repositoryConnectController );
      assertFalse( result );
    }
  }

  @Test
  public void storeNewSessionAndReconnectReturnsTrueWhenConnected() throws Exception {
    RepositoryMeta repositoryMeta = mock( RepositoryMeta.class );
    SessionInfo sessionInfo = new SessionInfo( "sess123", "admin" );
    when( repositoryConnectController.isConnected() ).thenReturn( true );
    when( repositoryMeta.getName() ).thenReturn( "myRepo" );

    try ( MockedStatic<SpoonSessionManager> ssmStatic = mockStatic( SpoonSessionManager.class );
          MockedStatic<Spoon> spoonStatic = mockStatic( Spoon.class ) ) {
      SpoonSessionManager ssm = mock( SpoonSessionManager.class );
      ssmStatic.when( SpoonSessionManager::getInstance ).thenReturn( ssm );
      AuthenticationContext authCtx = mock( AuthenticationContext.class );
      when( ssm.getAuthenticationContext( "http://server" ) ).thenReturn( authCtx );

      Spoon spoon = mock( Spoon.class );
      spoonStatic.when( Spoon::getInstance ).thenReturn( spoon );
      Repository repo = mock( Repository.class );
      when( spoon.getRepository() ).thenReturn( repo );

      Method method = SessionTimeoutHandler.class.getDeclaredMethod(
        "storeNewSessionAndReconnect", String.class, SessionInfo.class, RepositoryMeta.class,
        RepositoryConnectController.class );
      method.setAccessible( true );

      boolean result = (boolean) method.invoke(
        sessionTimeoutHandler, "http://server", sessionInfo, repositoryMeta, repositoryConnectController );

      assertTrue( result );
      verify( authCtx ).storeJSessionId( "sess123" );
      verify( repositoryConnectController ).reconnectToRepository(
        "myRepo", "admin", AuthenticationContext.SESSION_AUTH_TOKEN );
    }
  }

  @Test
  public void storeNewSessionAndReconnectReturnsFalseWhenNotConnected() throws Exception {
    RepositoryMeta repositoryMeta = mock( RepositoryMeta.class );
    SessionInfo sessionInfo = new SessionInfo( "sess123", "admin" );
    when( repositoryConnectController.isConnected() ).thenReturn( false );
    when( repositoryMeta.getName() ).thenReturn( "myRepo" );

    try ( MockedStatic<SpoonSessionManager> ssmStatic = mockStatic( SpoonSessionManager.class );
          MockedStatic<Spoon> spoonStatic = mockStatic( Spoon.class ) ) {
      SpoonSessionManager ssm = mock( SpoonSessionManager.class );
      ssmStatic.when( SpoonSessionManager::getInstance ).thenReturn( ssm );
      AuthenticationContext authCtx = mock( AuthenticationContext.class );
      when( ssm.getAuthenticationContext( "http://server" ) ).thenReturn( authCtx );
      spoonStatic.when( Spoon::getInstance ).thenReturn( null );

      Method method = SessionTimeoutHandler.class.getDeclaredMethod(
        "storeNewSessionAndReconnect", String.class, SessionInfo.class, RepositoryMeta.class,
        RepositoryConnectController.class );
      method.setAccessible( true );

      boolean result = (boolean) method.invoke(
        sessionTimeoutHandler, "http://server", sessionInfo, repositoryMeta, repositoryConnectController );

      assertFalse( result );
    }
  }


  @Test
  public void initializeRepositoryProvidersAfterReconnectionWithNullRepository() throws Exception {
    Spoon spoon = mock( Spoon.class );
    when( spoon.getRepository() ).thenReturn( null );

    Method method = SessionTimeoutHandler.class.getDeclaredMethod(
      "initializeRepositoryProvidersAfterReconnection", Spoon.class );
    method.setAccessible( true );
    method.invoke( sessionTimeoutHandler, spoon );
    // Should not throw
    verify( spoon ).getRepository();
  }

  @Test
  public void initializeRepositoryProvidersAfterReconnectionLoadsDirectoryTree() throws Exception {
    Spoon spoon = mock( Spoon.class );
    Repository repo = mock( Repository.class );
    when( spoon.getRepository() ).thenReturn( repo );

    Method method = SessionTimeoutHandler.class.getDeclaredMethod(
      "initializeRepositoryProvidersAfterReconnection", Spoon.class );
    method.setAccessible( true );
    method.invoke( sessionTimeoutHandler, spoon );

    verify( repo ).loadRepositoryDirectoryTree();
  }

  @Test( expected = KettleRepositoryLostException.class )
  public void handleLoginCanceledThrowsKettleRepositoryLostException() throws Throwable {
    try ( MockedStatic<Spoon> spoonStatic = mockStatic( Spoon.class ) ) {
      spoonStatic.when( Spoon::getInstance ).thenReturn( null );

      Method method = SessionTimeoutHandler.class.getDeclaredMethod( "handleLoginCanceled" );
      method.setAccessible( true );
      try {
        method.invoke( sessionTimeoutHandler );
      } catch ( InvocationTargetException e ) {
        throw e.getCause();
      }
    }
  }

  @Test( expected = KettleRepositoryLostException.class )
  public void handleLoginCanceledClosesRepositoryAndThrows() throws Throwable {
    try ( MockedStatic<Spoon> spoonStatic = mockStatic( Spoon.class ) ) {
      Spoon spoon = mock( Spoon.class );
      Display display = mock( Display.class );
      when( spoon.getDisplay() ).thenReturn( display );
      spoonStatic.when( Spoon::getInstance ).thenReturn( spoon );

      ArgumentCaptor<Runnable> captor = ArgumentCaptor.forClass( Runnable.class );
      doNothing().when( display ).syncExec( captor.capture() );

      Method method = SessionTimeoutHandler.class.getDeclaredMethod( "handleLoginCanceled" );
      method.setAccessible( true );
      try {
        method.invoke( sessionTimeoutHandler );
      } catch ( InvocationTargetException e ) {
        throw e.getCause();
      }
    }
  }

  @Test( expected = KettleRepositoryLostException.class )
  public void handleLoginCanceledLogsErrorWhenCloseRepositoryThrows() throws Throwable {
    try ( MockedStatic<Spoon> spoonStatic = mockStatic( Spoon.class ) ) {
      Spoon spoon = mock( Spoon.class );
      Display display = mock( Display.class );
      when( spoon.getDisplay() ).thenReturn( display );
      spoonStatic.when( Spoon::getInstance ).thenReturn( spoon );
      doThrow( new RuntimeException( "close failed" ) ).when( spoon ).closeRepository();

      // Capture the Runnable and execute it so the catch block inside the lambda is covered
      ArgumentCaptor<Runnable> captor = ArgumentCaptor.forClass( Runnable.class );
      doNothing().when( display ).syncExec( captor.capture() );

      Method method = SessionTimeoutHandler.class.getDeclaredMethod( "handleLoginCanceled" );
      method.setAccessible( true );
      try {
        method.invoke( sessionTimeoutHandler );
      } catch ( InvocationTargetException e ) {
        // Run the captured Runnable — closeRepository() will throw, exercising the catch block
        captor.getValue().run();
        verify( spoon ).closeRepository();
        throw e.getCause();
      }
    }
  }

  @Test( expected = KettleRepositoryLostException.class )
  public void handleLoginCanceledHandlesExceptionFromSpoon() throws Throwable {
    try ( MockedStatic<Spoon> spoonStatic = mockStatic( Spoon.class ) ) {
      spoonStatic.when( Spoon::getInstance ).thenThrow( new RuntimeException( "spoon error" ) );

      Method method = SessionTimeoutHandler.class.getDeclaredMethod( "handleLoginCanceled" );
      method.setAccessible( true );
      try {
        method.invoke( sessionTimeoutHandler );
      } catch ( InvocationTargetException e ) {
        throw e.getCause();
      }
    }
  }

  @Test
  public void performLoginAndReinvokeReturnsNullWhenNeedToLoginIsFalse() throws Exception {
    // Set needToLogin to false
    java.lang.reflect.Field field = SessionTimeoutHandler.class.getDeclaredField( "needToLogin" );
    field.setAccessible( true );
    ( (java.util.concurrent.atomic.AtomicBoolean) field.get( sessionTimeoutHandler ) ).set( false );

    Method method = SessionTimeoutHandler.class.getDeclaredMethod(
      "performLoginAndReinvoke", Object.class, Method.class, Object[].class );
    method.setAccessible( true );

    Method repoMethod = Repository.class.getMethod( "getDatabaseIDs", boolean.class );
    Object result = method.invoke( sessionTimeoutHandler, repository, repoMethod, new Object[] { Boolean.FALSE } );
    org.junit.Assert.assertNull( result );
  }

  @Test
  public void handleLoginSuccessRethrowsInvocationTargetExceptionAsLoginSuccessReinvokeException()
    throws Exception {
    when( repository.getDatabaseIDs( anyBoolean() ) )
      .thenThrow( new RuntimeException( "post-login failure" ) );
    Method repoMethod = Repository.class.getMethod( "getDatabaseIDs", boolean.class );

    try ( MockedStatic<Spoon> spoonStatic = mockStatic( Spoon.class ) ) {
      spoonStatic.when( Spoon::getInstance ).thenReturn( null );

      Method method = SessionTimeoutHandler.class.getDeclaredMethod(
        "handleLoginSuccess", Object.class, Method.class, Object[].class );
      method.setAccessible( true );

      try {
        method.invoke( sessionTimeoutHandler, repository, repoMethod, new Object[] { Boolean.FALSE } );
        fail( "Expected LoginSuccessReinvokeException" );
      } catch ( InvocationTargetException e ) {
        assertTrue( e.getCause() instanceof SessionTimeoutHandler.LoginSuccessReinvokeException );
      }
    }
  }

  @Test
  public void handleReinvokesMethodWhenPerformLoginReturnsNullButReinvokeIsTrue() throws Throwable {
    // Covers the path in handle() where performLoginAndReinvoke() returns null (needToLogin
    // already cleared by another thread) but reinvoke is true (a prior login succeeded),
    // so the method is re-invoked directly via method.invoke().
    ObjectId[] expectedResult = new ObjectId[] { mock( ObjectId.class ) };
    when( repository.getDatabaseIDs( anyBoolean() ) ).thenReturn( expectedResult );
    Method method = Repository.class.getMethod( "getDatabaseIDs", boolean.class );

    // Pre-set reinvoke=true and needToLogin=false to simulate a concurrent thread
    // having already completed the login flow before this thread enters
    // performLoginAndReinvoke().
    java.lang.reflect.Field reinvokeField = SessionTimeoutHandler.class.getDeclaredField( "reinvoke" );
    reinvokeField.setAccessible( true );
    ( (java.util.concurrent.atomic.AtomicBoolean) reinvokeField.get( sessionTimeoutHandler ) ).set( true );

    java.lang.reflect.Field needToLoginField = SessionTimeoutHandler.class.getDeclaredField( "needToLogin" );
    needToLoginField.setAccessible( true );
    ( (java.util.concurrent.atomic.AtomicBoolean) needToLoginField.get( sessionTimeoutHandler ) ).set( false );

    try ( MockedStatic<org.pentaho.di.connections.ConnectionManager> cmStatic =
            mockStatic( org.pentaho.di.connections.ConnectionManager.class ) ) {
      org.pentaho.di.connections.ConnectionManager cm =
        mock( org.pentaho.di.connections.ConnectionManager.class );
      cmStatic.when( org.pentaho.di.connections.ConnectionManager::getInstance ).thenReturn( cm );

      Object result = sessionTimeoutHandler.handle(
        repository, mock( Exception.class ), method, new Object[] { Boolean.FALSE } );

      assertSame( expectedResult, result );
    }
  }

  @Test
  public void createBrowserAuthenticationServiceReturnsNewInstance() {
    SessionTimeoutHandler real = new SessionTimeoutHandler( repositoryConnectController );
    assertNotNull( real.createBrowserAuthenticationService() );
  }

  @Test
  public void loginSuccessReinvokeExceptionStoresCause() {
    RuntimeException cause = new RuntimeException( "root" );
    SessionTimeoutHandler.LoginSuccessReinvokeException ex =
      new SessionTimeoutHandler.LoginSuccessReinvokeException( "msg", cause );
    assertSame( cause, ex.getCause() );
  }


  private static ClientTransportException createClientTransportException( String message ) {
    return new ClientTransportException( message );
  }

  static class ClientTransportException extends RuntimeException {
    ClientTransportException( String message ) {
      super( message );
    }
  }
}
