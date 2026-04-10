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

package org.pentaho.di.ui.repo.dialog;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.repository.RepositoriesMeta;
import org.pentaho.di.repository.RepositoryMeta;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.repo.controller.RepositoryConnectController;
import org.pentaho.di.ui.repo.service.BrowserAuthenticationService;
import org.pentaho.di.ui.repo.service.BrowserAuthenticationService.SessionInfo;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.ui.spoon.session.AuthenticationContext;
import org.pentaho.di.ui.spoon.session.SpoonSessionManager;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RepositoryManagerDialogTest {

  private static MockedStatic<GUIResource> guiResourceMock;
  private static MockedStatic<PropsUI> propsUIMock;

  private RepositoryManagerDialog dialogInstance;
  private Shell mockShell;
  private Shell mockDialogShell;
  private LogChannelInterface mockLog;
  private Display mockDisplay;

  @BeforeClass
  public static void setUpClass() throws Exception {
    if ( !KettleLogStore.isInitialized() ) {
      KettleLogStore.init();
    }
    if ( !KettleClientEnvironment.isInitialized() ) {
      KettleClientEnvironment.init();
    }

    GUIResource mockGui = mock( GUIResource.class );
    Image mockImage = mock( Image.class );
    when( mockGui.getImageLogoSmall() ).thenReturn( mockImage );

    guiResourceMock = mockStatic( GUIResource.class );
    guiResourceMock.when( GUIResource::getInstance ).thenReturn( mockGui );

    propsUIMock = mockStatic( PropsUI.class );
    propsUIMock.when( PropsUI::getInstance ).thenReturn( mock( PropsUI.class ) );
  }

  @AfterClass
  public static void tearDownClass() {
    if ( guiResourceMock != null ) {
      guiResourceMock.close();
    }
    if ( propsUIMock != null ) {
      propsUIMock.close();
    }
  }

  @Before
  public void setUp() throws Exception {
    mockShell = mock( Shell.class );
    mockDialogShell = mock( Shell.class );
    mockLog = mock( LogChannelInterface.class );
    mockDisplay = mock( Display.class );

    // Make Display.asyncExec run the Runnable immediately for synchronous testing
    doAnswer( inv -> {
      ( (Runnable) inv.getArgument( 0 ) ).run();
      return null;
    } ).when( mockDisplay ).asyncExec( any( Runnable.class ) );

    // Use Objenesis to create instance without calling the constructor
    dialogInstance = org.objenesis.ObjenesisHelper.newInstance( RepositoryManagerDialog.class );

    // Set internal fields via reflection
    setField( dialogInstance, "dialog", mockDialogShell );
    setField( dialogInstance, "log", mockLog );

    when( mockDialogShell.isDisposed() ).thenReturn( false );
  }

  @Test
  public void openBrowserLogin_Success_StoresSessionAndConnects() throws Exception {
    SessionInfo sessionInfo = new SessionInfo( "JSESS123", "adminUser" );
    CompletableFuture<SessionInfo> completedFuture = CompletableFuture.completedFuture( sessionInfo );

    AuthenticationContext mockAuthContext = mock( AuthenticationContext.class );
    SpoonSessionManager mockSessionMgr = mock( SpoonSessionManager.class );
    when( mockSessionMgr.getAuthenticationContext( "http://localhost:8080/pentaho" ) )
      .thenReturn( mockAuthContext );

    RepositoryConnectController mockController = mock( RepositoryConnectController.class );

    try ( MockedStatic<SpoonSessionManager> sessionMock = mockStatic( SpoonSessionManager.class );
          MockedStatic<Display> displayMock = mockStatic( Display.class );
          MockedStatic<RepositoryConnectController> ctrlMock = mockStatic( RepositoryConnectController.class );
          MockedConstruction<BrowserAuthenticationService> authMock =
            mockConstruction( BrowserAuthenticationService.class, ( mock, ctx ) ->
              when( mock.authenticate( anyString(), any() ) ).thenReturn( completedFuture ) ) ) {

      sessionMock.when( SpoonSessionManager::getInstance ).thenReturn( mockSessionMgr );
      displayMock.when( Display::getDefault ).thenReturn( mockDisplay );
      ctrlMock.when( RepositoryConnectController::getInstance ).thenReturn( mockController );

      dialogInstance.openBrowserLogin( "myRepo", "http://localhost:8080/pentaho",
        new RepositoriesMeta(), null );

      // Verify dialog was closed
      verify( mockDialogShell ).close();

      // Verify session was stored
      verify( mockAuthContext ).storeJSessionId( "JSESS123" );

      // Verify connectToRepository was called with correct arguments
      verify( mockController ).connectToRepository(
        "myRepo",
        "adminUser",
        AuthenticationContext.SESSION_AUTH_TOKEN
      );
    }
  }

  @Test
  public void openBrowserLogin_AuthFails_ShowsAuthenticationFailedError() {
    CompletableFuture<SessionInfo> failedFuture = new CompletableFuture<>();
    failedFuture.completeExceptionally( new RuntimeException( "Server unreachable" ) );

    Spoon mockSpoon = mock( Spoon.class );
    when( mockSpoon.getShell() ).thenReturn( mockShell );

    try ( MockedStatic<Display> displayMock = mockStatic( Display.class );
          MockedStatic<Spoon> spoonMock = mockStatic( Spoon.class );
          MockedConstruction<BrowserAuthenticationService> authMock =
            mockConstruction( BrowserAuthenticationService.class, ( mock, ctx ) ->
              when( mock.authenticate( anyString(), any() ) ).thenReturn( failedFuture ) );
          MockedConstruction<org.eclipse.swt.widgets.MessageBox> mbMock =
            mockConstruction( org.eclipse.swt.widgets.MessageBox.class ) ) {

      displayMock.when( Display::getDefault ).thenReturn( mockDisplay );
      spoonMock.when( Spoon::getInstance ).thenReturn( mockSpoon );

      dialogInstance.openBrowserLogin( "myRepo", "http://localhost:8080/pentaho",
        new RepositoriesMeta(), null );

      // Verify error was logged
      verify( mockLog ).logError( eq( "Browser authentication failed" ), any( Throwable.class ) );

      // Verify error dialog was shown
      org.eclipse.swt.widgets.MessageBox msgBox = mbMock.constructed().get( 0 );
      verify( msgBox ).setText( anyString() );
      verify( msgBox ).open();
    }
  }

  @Test
  public void openBrowserLogin_Timeout_ShowsTimeoutMessage() {
    CompletableFuture<SessionInfo> timedOutFuture = new CompletableFuture<>();
    timedOutFuture.completeExceptionally( new TimeoutException( "Timed out" ) );

    Spoon mockSpoon = mock( Spoon.class );
    when( mockSpoon.getShell() ).thenReturn( mockShell );

    try ( MockedStatic<Display> displayMock = mockStatic( Display.class );
          MockedStatic<Spoon> spoonMock = mockStatic( Spoon.class );
          MockedConstruction<BrowserAuthenticationService> authMock =
            mockConstruction( BrowserAuthenticationService.class, ( mock, ctx ) ->
              when( mock.authenticate( anyString(), any() ) ).thenReturn( timedOutFuture ) );
          MockedConstruction<org.eclipse.swt.widgets.MessageBox> mbMock =
            mockConstruction( org.eclipse.swt.widgets.MessageBox.class ) ) {

      displayMock.when( Display::getDefault ).thenReturn( mockDisplay );
      spoonMock.when( Spoon::getInstance ).thenReturn( mockSpoon );

      dialogInstance.openBrowserLogin( "myRepo", "http://localhost:8080/pentaho",
        new RepositoriesMeta(), null );

      // Verify error dialog was shown
      org.eclipse.swt.widgets.MessageBox msgBox = mbMock.constructed().get( 0 );
      verify( msgBox ).setText( anyString() );
      verify( msgBox ).open();
    }
  }

  @Test
  public void openBrowserLogin_AuthenticateThrows_ShowsError() {
    Spoon mockSpoon = mock( Spoon.class );
    when( mockSpoon.getShell() ).thenReturn( mockShell );

    try ( MockedStatic<Spoon> spoonMock = mockStatic( Spoon.class );
          MockedConstruction<BrowserAuthenticationService> authMock =
            mockConstruction( BrowserAuthenticationService.class, ( mock, ctx ) ->
              when( mock.authenticate( anyString(), any() ) ).thenThrow(
                new RuntimeException( "Port already in use" ) ) );
          MockedConstruction<org.eclipse.swt.widgets.MessageBox> mbMock =
            mockConstruction( org.eclipse.swt.widgets.MessageBox.class ) ) {

      spoonMock.when( Spoon::getInstance ).thenReturn( mockSpoon );

      dialogInstance.openBrowserLogin( "myRepo", "http://localhost:8080/pentaho",
        new RepositoriesMeta(), null );

      // Verify error dialog was shown
      org.eclipse.swt.widgets.MessageBox msgBox = mbMock.constructed().get( 0 );
      verify( msgBox ).setText( anyString() );
      verify( msgBox ).open();
    }
  }

  @Test
  public void openBrowserLogin_DialogAlreadyDisposed_SkipsClose() {
    when( mockDialogShell.isDisposed() ).thenReturn( true );

    CompletableFuture<SessionInfo> pending = new CompletableFuture<>();

    try ( MockedConstruction<BrowserAuthenticationService> authMock =
            mockConstruction( BrowserAuthenticationService.class, ( mock, ctx ) ->
              when( mock.authenticate( anyString(), any() ) ).thenReturn( pending ) ) ) {

      dialogInstance.openBrowserLogin( "myRepo", "http://localhost:8080/pentaho",
        new RepositoriesMeta(), null );

      // dialog.close() should NOT have been called since it's already disposed
      verify( mockDialogShell, never() ).close();
    }
  }

  @Test
  public void openBrowserLogin_PassesCorrectServerUrl() {
    String serverUrl = "https://secure-server:443/pentaho";
    CompletableFuture<SessionInfo> pending = new CompletableFuture<>();

    try ( MockedConstruction<BrowserAuthenticationService> authMock =
            mockConstruction( BrowserAuthenticationService.class, ( mock, ctx ) ->
              when( mock.authenticate( anyString(), any() ) ).thenReturn( pending ) ) ) {

      dialogInstance.openBrowserLogin( "myRepo", serverUrl, new RepositoriesMeta(), null );

      BrowserAuthenticationService constructed = authMock.constructed().get( 0 );
      verify( constructed ).authenticate( "https://secure-server:443/pentaho", null );
    }
  }

  @Test
  public void openBrowserLogin_PassesAuthorizationUri() {
    String serverUrl = "http://server:8080/pentaho";
    String authUri = "https://idp.example.com/authorize";
    CompletableFuture<SessionInfo> pending = new CompletableFuture<>();

    try ( MockedConstruction<BrowserAuthenticationService> authMock =
            mockConstruction( BrowserAuthenticationService.class, ( mock, ctx ) ->
              when( mock.authenticate( anyString(), any() ) ).thenReturn( pending ) ) ) {

      dialogInstance.openBrowserLogin( "myRepo", serverUrl, new RepositoriesMeta(), authUri );

      BrowserAuthenticationService constructed = authMock.constructed().get( 0 );
      verify( constructed ).authenticate( serverUrl, authUri );
    }
  }

  @Test
  public void reloadRepositoriesMetadata_ReturnsFoundRepository() throws Exception {
    RepositoryMeta expectedMeta = mock( RepositoryMeta.class );

    try ( MockedConstruction<RepositoriesMeta> repoMetasMock =
            mockConstruction( RepositoriesMeta.class, ( mock, ctx ) ->
              when( mock.findRepository( "myRepo" ) ).thenReturn( expectedMeta ) ) ) {

      RepositoryMeta result = invokeReloadRepositoriesMetadata( "myRepo" );

      assertEquals( expectedMeta, result );
      RepositoriesMeta constructed = repoMetasMock.constructed().get( 0 );
      verify( constructed ).readData();
      verify( constructed ).findRepository( "myRepo" );
    }
  }

  @Test
  public void reloadRepositoriesMetadata_ReturnsNullWhenRepoNotFound() throws Exception {
    try ( MockedConstruction<RepositoriesMeta> repoMetasMock =
            mockConstruction( RepositoriesMeta.class, ( mock, ctx ) ->
              when( mock.findRepository( "unknownRepo" ) ).thenReturn( null ) ) ) {

      RepositoryMeta result = invokeReloadRepositoriesMetadata( "unknownRepo" );

      assertNull( result );
      RepositoriesMeta constructed = repoMetasMock.constructed().get( 0 );
      verify( constructed ).readData();
      verify( constructed ).findRepository( "unknownRepo" );
    }
  }

  @Test
  public void reloadRepositoriesMetadata_ReturnsNullAndLogsDebugWhenReadDataFails() throws Exception {
    KettleException expectedException = new KettleException( "disk read error" );

    try ( MockedConstruction<RepositoriesMeta> repoMetasMock =
            mockConstruction( RepositoriesMeta.class, ( mock, ctx ) ->
              doThrow( expectedException ).when( mock ).readData() ) ) {

      RepositoryMeta result = invokeReloadRepositoriesMetadata( "myRepo" );

      assertNull( result );
      verify( mockLog ).logDebug( "Error reloading repositories metadata", expectedException );
      RepositoriesMeta constructed = repoMetasMock.constructed().get( 0 );
      verify( constructed, never() ).findRepository( anyString() );
    }
  }

  private RepositoryMeta invokeReloadRepositoriesMetadata( String repoName ) throws Exception {
    Method method = RepositoryManagerDialog.class.getDeclaredMethod( "reloadRepositoriesMetadata", String.class );
    method.setAccessible( true );
    try {
      return (RepositoryMeta) method.invoke( dialogInstance, repoName );
    } catch ( InvocationTargetException e ) {
      if ( e.getCause() instanceof Exception ) {
        throw (Exception) e.getCause();
      }
      throw e;
    }
  }

  @Test
  public void formatBrowserAuthErrorMessage_TimeoutException_ReturnsTimeoutMessage() throws Exception {
    String result = invokeFormatBrowserAuthErrorMessage( new TimeoutException( "timed out" ) );

    assertTrue( result.contains( "Sign" ) );
    assertTrue( result.contains( "did not complete in time" ) );
  }

  @Test
  public void formatBrowserAuthErrorMessage_WrappedTimeoutException_ReturnsTimeoutMessage() throws Exception {
    CompletionException wrapped = new CompletionException( new TimeoutException( "timed out" ) );

    String result = invokeFormatBrowserAuthErrorMessage( wrapped );

    assertTrue( result.contains( "did not complete in time" ) );
  }

  @Test
  public void formatBrowserAuthErrorMessage_GenericErrorWithMessage_ReturnsDetailsMessage() throws Exception {
    String result = invokeFormatBrowserAuthErrorMessage( new RuntimeException( "Connection refused" ) );

    assertTrue( result.contains( "Unable to complete browser sign-in" ) );
    assertTrue( result.contains( "Connection refused" ) );
  }

  @Test
  public void formatBrowserAuthErrorMessage_NullMessage_ReturnsFallbackMessage() throws Exception {
    String result = invokeFormatBrowserAuthErrorMessage( new RuntimeException( (String) null ) );

    assertEquals( "Unable to complete browser sign-in. Please try again.", result );
  }

  @Test
  public void formatBrowserAuthErrorMessage_EmptyMessage_ReturnsFallbackMessage() throws Exception {
    String result = invokeFormatBrowserAuthErrorMessage( new RuntimeException( "  " ) );

    assertEquals( "Unable to complete browser sign-in. Please try again.", result );
  }

  @Test
  public void unwrapRootCause_SingleCompletionException_ReturnsWrappedCause() throws Exception {
    RuntimeException root = new RuntimeException( "root cause" );
    CompletionException wrapper = new CompletionException( root );

    Throwable result = invokeUnwrapRootCause( wrapper );

    assertSame( root, result );
  }

  @Test
  public void unwrapRootCause_NestedCompletionExceptions_UnwrapsToDeepestNonCompletion() throws Exception {
    IllegalStateException root = new IllegalStateException( "deep root" );
    CompletionException inner = new CompletionException( root );
    CompletionException outer = new CompletionException( inner );

    Throwable result = invokeUnwrapRootCause( outer );

    assertSame( root, result );
  }

  @Test
  public void unwrapRootCause_NonCompletionException_ReturnsSameThrowable() throws Exception {
    RuntimeException original = new RuntimeException( "plain error" );

    Throwable result = invokeUnwrapRootCause( original );

    assertSame( original, result );
  }

  @Test
  public void unwrapRootCause_CompletionExceptionWithNullCause_ReturnsSameException() throws Exception {
    CompletionException ce = new CompletionException( null );

    Throwable result = invokeUnwrapRootCause( ce );

    assertSame( ce, result );
  }

  @Test
  public void openBrowserLogin_ConnectionErrorAfterAuth_LogsErrorAndShowsDialog() throws Exception {
    SessionInfo sessionInfo = new SessionInfo( "JSESS456", "user1" );
    CompletableFuture<SessionInfo> completedFuture = CompletableFuture.completedFuture( sessionInfo );

    AuthenticationContext mockAuthContext = mock( AuthenticationContext.class );
    SpoonSessionManager mockSessionMgr = mock( SpoonSessionManager.class );
    when( mockSessionMgr.getAuthenticationContext( "http://server:8080/pentaho" ) )
      .thenReturn( mockAuthContext );

    RepositoryConnectController mockController = mock( RepositoryConnectController.class );
    doThrow( new KettleException( "login failed" ) ).when( mockController )
      .connectToRepository( anyString(), anyString(), anyString() );

    Spoon mockSpoon = mock( Spoon.class );
    when( mockSpoon.getShell() ).thenReturn( mockShell );

    try ( MockedStatic<SpoonSessionManager> sessionMock = mockStatic( SpoonSessionManager.class );
          MockedStatic<Display> displayMock = mockStatic( Display.class );
          MockedStatic<RepositoryConnectController> ctrlMock = mockStatic( RepositoryConnectController.class );
          MockedStatic<Spoon> spoonMock = mockStatic( Spoon.class );
          MockedConstruction<BrowserAuthenticationService> authMock =
            mockConstruction( BrowserAuthenticationService.class, ( mock, ctx ) ->
              when( mock.authenticate( anyString(), any() ) ).thenReturn( completedFuture ) );
          MockedConstruction<org.eclipse.swt.widgets.MessageBox> mbMock =
            mockConstruction( org.eclipse.swt.widgets.MessageBox.class ) ) {

      sessionMock.when( SpoonSessionManager::getInstance ).thenReturn( mockSessionMgr );
      displayMock.when( Display::getDefault ).thenReturn( mockDisplay );
      ctrlMock.when( RepositoryConnectController::getInstance ).thenReturn( mockController );
      spoonMock.when( Spoon::getInstance ).thenReturn( mockSpoon );

      dialogInstance.openBrowserLogin( "myRepo", "http://server:8080/pentaho",
        new RepositoriesMeta(), null );

      verify( mockLog ).logError( eq( "Error connecting to repository after browser authentication" ),
        any( Throwable.class ) );

      org.eclipse.swt.widgets.MessageBox msgBox = mbMock.constructed().get( 0 );
      verify( msgBox ).setText( anyString() );
      verify( msgBox ).open();
    }
  }

  @Test
  public void openBrowserLogin_CompletionExceptionWrappedError_ShowsUnwrappedErrorMessage() {
    CompletableFuture<SessionInfo> failedFuture = new CompletableFuture<>();
    failedFuture.completeExceptionally(
      new CompletionException( new RuntimeException( "wrapped failure" ) ) );

    Spoon mockSpoon = mock( Spoon.class );
    when( mockSpoon.getShell() ).thenReturn( mockShell );

    try ( MockedStatic<Display> displayMock = mockStatic( Display.class );
          MockedStatic<Spoon> spoonMock = mockStatic( Spoon.class );
          MockedConstruction<BrowserAuthenticationService> authMock =
            mockConstruction( BrowserAuthenticationService.class, ( mock, ctx ) ->
              when( mock.authenticate( anyString(), any() ) ).thenReturn( failedFuture ) );
          MockedConstruction<org.eclipse.swt.widgets.MessageBox> mbMock =
            mockConstruction( org.eclipse.swt.widgets.MessageBox.class ) ) {

      displayMock.when( Display::getDefault ).thenReturn( mockDisplay );
      spoonMock.when( Spoon::getInstance ).thenReturn( mockSpoon );

      dialogInstance.openBrowserLogin( "myRepo", "http://localhost:8080/pentaho",
        new RepositoriesMeta(), null );

      verify( mockLog ).logError( eq( "Browser authentication failed" ), any( Throwable.class ) );

      org.eclipse.swt.widgets.MessageBox msgBox = mbMock.constructed().get( 0 );
      verify( msgBox ).setMessage( org.mockito.ArgumentMatchers.contains( "wrapped failure" ) );
      verify( msgBox ).open();
    }
  }

  @Test
  public void openBrowserLogin_MalformedUrl_ShowsConfigErrorAndDoesNotLaunchBrowser() {
    Spoon mockSpoon = mock( Spoon.class );
    when( mockSpoon.getShell() ).thenReturn( mockShell );

    try ( MockedStatic<Spoon> spoonMock = mockStatic( Spoon.class );
          MockedConstruction<BrowserAuthenticationService> authMock =
            mockConstruction( BrowserAuthenticationService.class );
          MockedConstruction<org.eclipse.swt.widgets.MessageBox> mbMock =
            mockConstruction( org.eclipse.swt.widgets.MessageBox.class ) ) {

      spoonMock.when( Spoon::getInstance ).thenReturn( mockSpoon );

      dialogInstance.openBrowserLogin( "myRepo", "not a valid url :// %%",
        new RepositoriesMeta(), null );

      assertTrue( "No BrowserAuthenticationService should have been created",
        authMock.constructed().isEmpty() );
      verify( mockDialogShell, never() ).close();
      verify( mockLog ).logError( eq( "Invalid server URL for browser login: not a valid url :// %%" ),
        any( java.net.URISyntaxException.class ) );

      org.eclipse.swt.widgets.MessageBox msgBox = mbMock.constructed().get( 0 );
      verify( msgBox ).setText( "Configuration Error" );
      verify( msgBox ).open();
    }
  }

  @Test
  public void openBrowserLogin_UrlWithNoHost_ShowsConfigErrorAndDoesNotLaunchBrowser() {
    Spoon mockSpoon = mock( Spoon.class );
    when( mockSpoon.getShell() ).thenReturn( mockShell );

    try ( MockedStatic<Spoon> spoonMock = mockStatic( Spoon.class );
          MockedConstruction<BrowserAuthenticationService> authMock =
            mockConstruction( BrowserAuthenticationService.class );
          MockedConstruction<org.eclipse.swt.widgets.MessageBox> mbMock =
            mockConstruction( org.eclipse.swt.widgets.MessageBox.class ) ) {

      spoonMock.when( Spoon::getInstance ).thenReturn( mockSpoon );

      dialogInstance.openBrowserLogin( "myRepo", "file:///local/path",
        new RepositoriesMeta(), null );

      assertTrue( "No BrowserAuthenticationService should have been created",
        authMock.constructed().isEmpty() );
      verify( mockDialogShell, never() ).close();
      verify( mockLog ).logError( eq( "Invalid server URL for browser login: file:///local/path" ),
        any( java.net.URISyntaxException.class ) );

      org.eclipse.swt.widgets.MessageBox msgBox = mbMock.constructed().get( 0 );
      verify( msgBox ).setText( "Configuration Error" );
      verify( msgBox ).open();
    }
  }

  @Test
  public void openBrowserLogin_OpaqueUriWithNoHost_ShowsConfigErrorAndDoesNotLaunchBrowser() {
    Spoon mockSpoon = mock( Spoon.class );
    when( mockSpoon.getShell() ).thenReturn( mockShell );

    try ( MockedStatic<Spoon> spoonMock = mockStatic( Spoon.class );
          MockedConstruction<BrowserAuthenticationService> authMock =
            mockConstruction( BrowserAuthenticationService.class );
          MockedConstruction<org.eclipse.swt.widgets.MessageBox> mbMock =
            mockConstruction( org.eclipse.swt.widgets.MessageBox.class ) ) {

      spoonMock.when( Spoon::getInstance ).thenReturn( mockSpoon );

      dialogInstance.openBrowserLogin( "myRepo", "mailto:user@example.com",
        new RepositoriesMeta(), null );

      assertTrue( authMock.constructed().isEmpty() );
      verify( mockDialogShell, never() ).close();

      org.eclipse.swt.widgets.MessageBox msgBox = mbMock.constructed().get( 0 );
      verify( msgBox ).setText( "Configuration Error" );
      verify( msgBox ).open();
    }
  }

  @Test
  public void openBrowserLogin_NullAuthContext_ShowsErrorDialogAndDoesNotNPE() throws Exception {
    SessionInfo sessionInfo = new SessionInfo( "JSESS789", "someUser" );
    CompletableFuture<SessionInfo> completedFuture = CompletableFuture.completedFuture( sessionInfo );

    SpoonSessionManager mockSessionMgr = mock( SpoonSessionManager.class );
    when( mockSessionMgr.getAuthenticationContext( "http://localhost:8080/pentaho" ) )
      .thenReturn( null );

    Spoon mockSpoon = mock( Spoon.class );
    when( mockSpoon.getShell() ).thenReturn( mockShell );

    RepositoryConnectController mockController = mock( RepositoryConnectController.class );

    try ( MockedStatic<SpoonSessionManager> sessionMock = mockStatic( SpoonSessionManager.class );
          MockedStatic<Display> displayMock = mockStatic( Display.class );
          MockedStatic<RepositoryConnectController> ctrlMock = mockStatic( RepositoryConnectController.class );
          MockedStatic<Spoon> spoonMock = mockStatic( Spoon.class );
          MockedConstruction<BrowserAuthenticationService> authMock =
            mockConstruction( BrowserAuthenticationService.class, ( mock, ctx ) ->
              when( mock.authenticate( anyString(), any() ) ).thenReturn( completedFuture ) );
          MockedConstruction<org.eclipse.swt.widgets.MessageBox> mbMock =
            mockConstruction( org.eclipse.swt.widgets.MessageBox.class ) ) {

      sessionMock.when( SpoonSessionManager::getInstance ).thenReturn( mockSessionMgr );
      displayMock.when( Display::getDefault ).thenReturn( mockDisplay );
      ctrlMock.when( RepositoryConnectController::getInstance ).thenReturn( mockController );
      spoonMock.when( Spoon::getInstance ).thenReturn( mockSpoon );

      dialogInstance.openBrowserLogin( "myRepo", "http://localhost:8080/pentaho",
        new RepositoriesMeta(), null );

      verify( mockLog ).logError( "Failed to create authentication context for server URL: http://localhost:8080/pentaho" );

      org.eclipse.swt.widgets.MessageBox msgBox = mbMock.constructed().get( 0 );
      verify( msgBox ).setText( "Connection Failed" );
      verify( msgBox ).setMessage( org.mockito.ArgumentMatchers.contains( "http://localhost:8080/pentaho" ) );
      verify( msgBox ).open();

      verify( mockController, never() ).connectToRepository( anyString(), anyString(), anyString() );
    }
  }

  @Test
  public void openBrowserLogin_ValidHttpsUrl_ProceedsToBrowserAuth() {
    CompletableFuture<SessionInfo> pending = new CompletableFuture<>();

    try ( MockedConstruction<BrowserAuthenticationService> authMock =
            mockConstruction( BrowserAuthenticationService.class, ( mock, ctx ) ->
              when( mock.authenticate( anyString(), any() ) ).thenReturn( pending ) ) ) {

      dialogInstance.openBrowserLogin( "myRepo", "https://secure.example.com:443/pentaho",
        new RepositoriesMeta(), "https://idp.example.com/auth" );

      assertEquals( 1, authMock.constructed().size() );
      BrowserAuthenticationService constructed = authMock.constructed().get( 0 );
      verify( constructed ).authenticate( "https://secure.example.com:443/pentaho",
        "https://idp.example.com/auth" );
    }
  }

  @Test
  public void openBrowserLogin_UrlWithOnlyWhitespace_ShowsConfigError() {
    Spoon mockSpoon = mock( Spoon.class );
    when( mockSpoon.getShell() ).thenReturn( mockShell );

    try ( MockedStatic<Spoon> spoonMock = mockStatic( Spoon.class );
          MockedConstruction<BrowserAuthenticationService> authMock =
            mockConstruction( BrowserAuthenticationService.class );
          MockedConstruction<org.eclipse.swt.widgets.MessageBox> mbMock =
            mockConstruction( org.eclipse.swt.widgets.MessageBox.class ) ) {

      spoonMock.when( Spoon::getInstance ).thenReturn( mockSpoon );

      dialogInstance.openBrowserLogin( "myRepo", "   ",
        new RepositoriesMeta(), null );

      assertTrue( authMock.constructed().isEmpty() );
      verify( mockDialogShell, never() ).close();

      org.eclipse.swt.widgets.MessageBox msgBox = mbMock.constructed().get( 0 );
      verify( msgBox ).setText( "Configuration Error" );
      verify( msgBox ).open();
    }
  }

  @Test
  public void openBrowserLogin_ValidUrlWithPortNoPath_ProceedsToBrowserAuth() {
    CompletableFuture<SessionInfo> pending = new CompletableFuture<>();

    try ( MockedConstruction<BrowserAuthenticationService> authMock =
            mockConstruction( BrowserAuthenticationService.class, ( mock, ctx ) ->
              when( mock.authenticate( anyString(), any() ) ).thenReturn( pending ) ) ) {

      dialogInstance.openBrowserLogin( "myRepo", "http://myserver:9090",
        new RepositoriesMeta(), null );

      assertEquals( 1, authMock.constructed().size() );
      verify( mockDialogShell ).close();
      verify( authMock.constructed().get( 0 ) ).authenticate( "http://myserver:9090", null );
    }
  }

  @Test
  public void openBrowserLogin_NullDialogField_ValidUrl_ProceedsWithoutClose() throws Exception {
    setField( dialogInstance, "dialog", null );
    CompletableFuture<SessionInfo> pending = new CompletableFuture<>();

    try ( MockedConstruction<BrowserAuthenticationService> authMock =
            mockConstruction( BrowserAuthenticationService.class, ( mock, ctx ) ->
              when( mock.authenticate( anyString(), any() ) ).thenReturn( pending ) ) ) {

      dialogInstance.openBrowserLogin( "myRepo", "http://valid-host:8080/pentaho",
        new RepositoriesMeta(), null );

      assertEquals( 1, authMock.constructed().size() );
    }
  }

  @Test
  public void openBrowserLogin_NullAuthContext_StoreJSessionIdNeverCalled() throws Exception {
    SessionInfo sessionInfo = new SessionInfo( "JSESS999", "testUser" );
    CompletableFuture<SessionInfo> completedFuture = CompletableFuture.completedFuture( sessionInfo );

    SpoonSessionManager mockSessionMgr = mock( SpoonSessionManager.class );
    when( mockSessionMgr.getAuthenticationContext( "http://localhost:8080/pentaho" ) )
      .thenReturn( null );

    AuthenticationContext neverUsedContext = mock( AuthenticationContext.class );

    Spoon mockSpoon = mock( Spoon.class );
    when( mockSpoon.getShell() ).thenReturn( mockShell );

    RepositoryConnectController mockController = mock( RepositoryConnectController.class );

    try ( MockedStatic<SpoonSessionManager> sessionMock = mockStatic( SpoonSessionManager.class );
          MockedStatic<Display> displayMock = mockStatic( Display.class );
          MockedStatic<RepositoryConnectController> ctrlMock = mockStatic( RepositoryConnectController.class );
          MockedStatic<Spoon> spoonMock = mockStatic( Spoon.class );
          MockedConstruction<BrowserAuthenticationService> authMock =
            mockConstruction( BrowserAuthenticationService.class, ( mock, ctx ) ->
              when( mock.authenticate( anyString(), any() ) ).thenReturn( completedFuture ) );
          MockedConstruction<org.eclipse.swt.widgets.MessageBox> mbMock =
            mockConstruction( org.eclipse.swt.widgets.MessageBox.class ) ) {

      sessionMock.when( SpoonSessionManager::getInstance ).thenReturn( mockSessionMgr );
      displayMock.when( Display::getDefault ).thenReturn( mockDisplay );
      ctrlMock.when( RepositoryConnectController::getInstance ).thenReturn( mockController );
      spoonMock.when( Spoon::getInstance ).thenReturn( mockSpoon );

      dialogInstance.openBrowserLogin( "myRepo", "http://localhost:8080/pentaho",
        new RepositoriesMeta(), null );

      verify( neverUsedContext, never() ).storeJSessionId( anyString() );
      verify( mockController, never() ).connectToRepository( anyString(), anyString(), anyString() );

      org.eclipse.swt.widgets.MessageBox msgBox = mbMock.constructed().get( 0 );
      verify( msgBox ).setText( "Connection Failed" );
      verify( msgBox ).open();
    }
  }

  @Test
  public void openBrowserLogin_InvalidUrlConfigError_LogsWithCorrectUrl() {
    String invalidUrl = "file:///local/path";
    Spoon mockSpoon = mock( Spoon.class );
    when( mockSpoon.getShell() ).thenReturn( mockShell );

    try ( MockedStatic<Spoon> spoonMock = mockStatic( Spoon.class );
          MockedConstruction<BrowserAuthenticationService> authMock =
            mockConstruction( BrowserAuthenticationService.class );
          MockedConstruction<org.eclipse.swt.widgets.MessageBox> mbMock =
            mockConstruction( org.eclipse.swt.widgets.MessageBox.class ) ) {

      spoonMock.when( Spoon::getInstance ).thenReturn( mockSpoon );

      dialogInstance.openBrowserLogin( "myRepo", invalidUrl,
        new RepositoriesMeta(), null );

      verify( mockLog ).logError(
        eq( "Invalid server URL for browser login: " + invalidUrl ),
        any( java.net.URISyntaxException.class ) );

      org.eclipse.swt.widgets.MessageBox msgBox = mbMock.constructed().get( 0 );
      verify( msgBox ).setMessage( org.mockito.ArgumentMatchers.contains( invalidUrl ) );
    }
  }

  private String invokeFormatBrowserAuthErrorMessage( Throwable error ) throws Exception {
    Method method = RepositoryManagerDialog.class.getDeclaredMethod( "formatBrowserAuthErrorMessage", Throwable.class );
    method.setAccessible( true );
    try {
      return (String) method.invoke( dialogInstance, error );
    } catch ( InvocationTargetException e ) {
      if ( e.getCause() instanceof Exception ) {
        throw (Exception) e.getCause();
      }
      throw e;
    }
  }

  private Throwable invokeUnwrapRootCause( Throwable throwable ) throws Exception {
    Method method = RepositoryManagerDialog.class.getDeclaredMethod( "unwrapRootCause", Throwable.class );
    method.setAccessible( true );
    try {
      return (Throwable) method.invoke( dialogInstance, throwable );
    } catch ( InvocationTargetException e ) {
      if ( e.getCause() instanceof Exception ) {
        throw (Exception) e.getCause();
      }
      throw e;
    }
  }

  private static void setField( Object target, String fieldName, Object value ) throws Exception {
    Class<?> clazz = target.getClass();
    while ( clazz != null ) {
      try {
        Field f = clazz.getDeclaredField( fieldName );
        f.setAccessible( true );
        f.set( target, value );
        return;
      } catch ( NoSuchFieldException e ) {
        clazz = clazz.getSuperclass();
      }
    }
    throw new NoSuchFieldException( fieldName + " not found in " + target.getClass().getName() + " hierarchy" );
  }
}

