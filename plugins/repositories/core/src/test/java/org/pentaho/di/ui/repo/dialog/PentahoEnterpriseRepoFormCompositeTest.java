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

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.json.simple.JSONObject;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.repo.util.SsoProviderService;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import org.junit.Assume;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class PentahoEnterpriseRepoFormCompositeTest {

  private static final Class<?> PKG = PentahoEnterpriseRepoFormComposite.class;

  private static Display display;
  private static MockedStatic<PropsUI> propsUIMock;

  @BeforeClass
  public static void setUpClass() throws Exception {
    if ( !KettleLogStore.isInitialized() ) {
      KettleLogStore.init();
    }
    if ( !KettleClientEnvironment.isInitialized() ) {
      KettleClientEnvironment.init();
    }

    PropsUI mockPropsUI = mock( PropsUI.class );
    propsUIMock = mockStatic( PropsUI.class );
    propsUIMock.when( PropsUI::getInstance ).thenReturn( mockPropsUI );

    try {
      display = new Display();
    } catch ( SWTError e ) {
      Assume.assumeNoException( "No display available; skipping SWT UI tests in this environment.", e );
    }
  }

  @AfterClass
  public static void tearDownClass() {
    if ( propsUIMock != null ) {
      propsUIMock.close();
    }
    if ( display != null && !display.isDisposed() ) {
      display.dispose();
    }
  }

  private Shell shell;
  private PentahoEnterpriseRepoFormComposite composite;
  private MockedConstruction<SsoProviderService> ssoServiceMock;
  private SsoProviderService mockSsoService;
  private Button btnSave;

  @Before
  public void setUp() {
    shell = new Shell( display );

    ssoServiceMock = mockConstruction( SsoProviderService.class, ( mock, ctx ) -> {
      // Default: return empty list so no NPEs during async callbacks
      when( mock.fetchProviders( anyString() ) ).thenReturn( Collections.emptyList() );
    } );

    composite = new PentahoEnterpriseRepoFormComposite( shell, SWT.NONE );
    mockSsoService = ssoServiceMock.constructed().get( 0 );

    btnSave = new Button( shell, SWT.PUSH );
    composite.updateSaveButton( btnSave );
  }

  @After
  public void tearDown() {
    ssoServiceMock.close();
    if ( shell != null && !shell.isDisposed() ) {
      shell.dispose();
    }
  }

  private void waitForAsync( BooleanCondition condition, long timeoutMs ) throws Exception {
    long deadline = System.currentTimeMillis() + timeoutMs;
    while ( !condition.check() && System.currentTimeMillis() < deadline ) {
      while ( display.readAndDispatch() ) {
        // process pending UI events
      }
    }
    // Flush one more time to make sure all events are processed
    while ( display.readAndDispatch() ) {
      // flush
    }
  }

  @FunctionalInterface
  interface BooleanCondition {
    boolean check() throws Exception;
  }

  private Object getField( String name ) throws Exception {
    Class<?> clazz = composite.getClass();
    while ( clazz != null ) {
      try {
        Field f = clazz.getDeclaredField( name );
        f.setAccessible( true );
        return f.get( composite );
      } catch ( NoSuchFieldException e ) {
        clazz = clazz.getSuperclass();
      }
    }
    throw new NoSuchFieldException( name );
  }

  private void setField( String name, Object value ) throws Exception {
    Class<?> clazz = composite.getClass();
    while ( clazz != null ) {
      try {
        Field f = clazz.getDeclaredField( name );
        f.setAccessible( true );
        f.set( composite, value );
        return;
      } catch ( NoSuchFieldException e ) {
        clazz = clazz.getSuperclass();
      }
    }
    throw new NoSuchFieldException( name );
  }

  private Object invoke( String method, Class<?>[] types, Object... args ) throws Exception {
    Method m = PentahoEnterpriseRepoFormComposite.class.getDeclaredMethod( method, types );
    m.setAccessible( true );
    return m.invoke( composite, args );
  }

  private Text txtUrl() throws Exception {
    return (Text) getField( "txtUrl" );
  }

  private Text txtDisplayName() throws Exception {
    return (Text) getField( "txtDisplayName" );
  }

  private Button radioSSO() throws Exception {
    return (Button) getField( "radioSSO" );
  }

  private Button radioUsernamePassword() throws Exception {
    return (Button) getField( "radioUsernamePassword" );
  }

  private Combo combo() throws Exception {
    return (Combo) getField( "comboSsoProvider" );
  }

  private Label lblSsoStatus() throws Exception {
    return (Label) getField( "lblSsoStatus" );
  }

  private Label lblSsoProvider() throws Exception {
    return (Label) getField( "lblSsoProvider" );
  }

  private void setUrlAndSelectSSO( String url ) throws Exception {
    // With SSO not selected, the modify listener returns early after setting lastLoadedUrl = null
    radioSSO().setSelection( false );
    radioUsernamePassword().setSelection( true );
    txtUrl().setText( url );
    // Now select SSO programmatically (no listener fires)
    radioSSO().setSelection( true );
    radioUsernamePassword().setSelection( false );
  }

  private boolean validateSaveAllowed() throws Exception {
    Method m = BaseRepoFormComposite.class.getDeclaredMethod( "validateSaveAllowed" );
    m.setAccessible( true );
    return (Boolean) m.invoke( composite );
  }

  private void updateSsoControls() throws Exception {
    invoke( "updateSsoControls", new Class[ 0 ] );
  }

  private void loadSsoProviders( boolean preserve ) throws Exception {
    invoke( "loadSsoProviders", new Class[] { boolean.class }, preserve );
  }

  private void onUrlFocusLost() throws Exception {
    invoke( "onUrlFocusLost", new Class[ 0 ] );
  }

  private void restoreProviderSelection( boolean preserve ) throws Exception {
    invoke( "restoreProviderSelection", new Class[] { boolean.class }, preserve );
  }

  private void applySelectedProvider() throws Exception {
    invoke( "applySelectedProvider", new Class[ 0 ] );
  }

  private void clearProviderSelection( boolean clearCombo ) throws Exception {
    invoke( "clearProviderSelection", new Class[] { boolean.class }, clearCombo );
  }

  private void setSsoStatusError( String msg ) throws Exception {
    invoke( "setSsoStatusError", new Class[] { String.class }, msg );
  }

  private void setSsoStatusInfo( String msg ) throws Exception {
    invoke( "setSsoStatusInfo", new Class[] { String.class }, msg );
  }

  private String stringValue( Object arg ) throws Exception {
    return (String) invoke( "stringValue", new Class[] { Object.class }, arg );
  }

  @SuppressWarnings( "unchecked" )
  private java.util.concurrent.Future<?> getProviderLoadFuture() throws Exception {
    java.util.concurrent.atomic.AtomicReference<java.util.concurrent.Future<?>> ref =
      (java.util.concurrent.atomic.AtomicReference<java.util.concurrent.Future<?>>) getField( "providerLoadFuture" );
    return ref.get();
  }

  private void applyFetchedProviders( String urlToLoad, List<SsoProviderService.SsoProvider> providers,
                                      Exception exception, boolean preserve ) throws Exception {
    Method m = PentahoEnterpriseRepoFormComposite.class.getDeclaredMethod(
      "applyFetchedProviders", String.class, List.class, Exception.class, boolean.class,
      String.class, String.class );
    m.setAccessible( true );
    m.invoke( composite, urlToLoad, providers, exception, preserve, null, null );
  }

  private int findProviderIndexByAuthorizationUri( String uri ) throws Exception {
    Method m = PentahoEnterpriseRepoFormComposite.class.getDeclaredMethod(
      "findProviderIndexByAuthorizationUri", String.class );
    m.setAccessible( true );
    return (int) m.invoke( composite, uri );
  }

  @Test
  public void testPopulate_defaults_usernamePassword() throws Exception {
    composite.populate( new JSONObject() );

    assertEquals( "http://localhost:8080/pentaho", txtUrl().getText() );
    assertFalse( radioSSO().getSelection() );
    assertTrue( radioUsernamePassword().getSelection() );
  }

  @Test
  public void testPopulate_usernamePassword_explicitAuthMethod() throws Exception {
    JSONObject src = new JSONObject();
    src.put( "url", "http://myserver:9090/pentaho" );
    src.put( "authMethod", "USERNAME_PASSWORD" );
    composite.populate( src );

    assertEquals( "http://myserver:9090/pentaho", txtUrl().getText() );
    assertTrue( radioUsernamePassword().getSelection() );
    assertFalse( radioSSO().getSelection() );
  }

  @Test
  public void testPopulate_sso_noProviderName() throws Exception {
    JSONObject src = new JSONObject();
    src.put( "url", "http://server/pentaho" );
    src.put( "authMethod", "SSO" );

    composite.populate( src );

    assertTrue( radioSSO().getSelection() );
    assertNull( getField( "selectedAuthorizationUri" ) );
  }


  @Test
  public void testPopulate_sso_triggersProviderLoad() throws Exception {
    when( mockSsoService.fetchProviders( anyString() ) ).thenReturn( Collections.emptyList() );

    JSONObject src = new JSONObject();
    src.put( "url", "http://server/pentaho" );
    src.put( "authMethod", "SSO" );
    composite.populate( src );

    // After load completes, lastLoadedUrl should be set
    waitForAsync( () -> "http://server/pentaho".equals( getField( "lastLoadedUrl" ) ), 3000 );
    assertEquals( "http://server/pentaho", getField( "lastLoadedUrl" ) );
  }

  @Test
  public void testToMap_usernamePassword() {
    JSONObject src = new JSONObject();
    src.put( "url", "http://server/pentaho" );
    src.put( "authMethod", "USERNAME_PASSWORD" );
    composite.populate( src );

    Map<String, Object> result = composite.toMap();

    assertEquals( "PentahoEnterpriseRepository", result.get( "id" ) );
    assertEquals( "http://server/pentaho", result.get( "url" ) );
    assertEquals( "USERNAME_PASSWORD", result.get( "authMethod" ) );
    assertNull( result.get( "ssoAuthorizationUri" ) );
    assertNull( result.get( "ssoRegistrationId" ) );
  }

  @Test
  public void testToMap_sso_withProvider() throws Exception {
    setUrlAndSelectSSO( "http://server/pentaho" );
    setField( "selectedAuthorizationUri", "https://auth.example.com/oauth2" );
    setField( "selectedRegistrationId", "myProvider" );
    combo().setItems( "My Provider" );
    combo().select( 0 );

    Map<String, Object> result = composite.toMap();

    assertEquals( "SSO", result.get( "authMethod" ) );
    assertEquals( "My Provider", result.get( "ssoProviderName" ) );
    assertEquals( "https://auth.example.com/oauth2", result.get( "ssoAuthorizationUri" ) );
    assertEquals( "myProvider", result.get( "ssoRegistrationId" ) );
  }

  @Test
  public void testToMap_sso_noComboSelection() throws Exception {
    setUrlAndSelectSSO( "http://server/pentaho" );
    combo().removeAll(); // no items, selectionIndex = -1

    Map<String, Object> result = composite.toMap();

    assertEquals( "SSO", result.get( "authMethod" ) );
    assertNull( result.get( "ssoProviderName" ) );
    // SSO fields are still read from field values, even with no combo selection
    assertNull( result.get( "ssoAuthorizationUri" ) );
  }

  @Test
  public void testValidateSaveAllowed_emptyUrl_returnsFalse() throws Exception {
    txtDisplayName().setText( "My Repo" );
    txtUrl().setText( "" );

    assertFalse( validateSaveAllowed() );
  }

  @Test
  public void testValidateSaveAllowed_usernamePassword_valid_returnsTrue() throws Exception {
    txtDisplayName().setText( "My Repo" );
    // txtUrl is not empty â€” set it without triggering SSO paths
    txtUrl().setText( "http://server/pentaho" );

    assertTrue( validateSaveAllowed() );
  }

  @Test
  public void testValidateSaveAllowed_sso_noAuthUri_returnsFalse() throws Exception {
    txtDisplayName().setText( "My Repo" );
    setUrlAndSelectSSO( "http://server/pentaho" );
    setField( "selectedAuthorizationUri", null );

    assertFalse( validateSaveAllowed() );
  }

  @Test
  public void testValidateSaveAllowed_sso_emptyAuthUri_returnsFalse() throws Exception {
    txtDisplayName().setText( "My Repo" );
    setUrlAndSelectSSO( "http://server/pentaho" );
    setField( "selectedAuthorizationUri", "" );

    assertFalse( validateSaveAllowed() );
  }

  @Test
  public void testValidateSaveAllowed_sso_withAuthUri_returnsTrue() throws Exception {
    txtDisplayName().setText( "My Repo" );
    setUrlAndSelectSSO( "http://server/pentaho" );
    setField( "selectedAuthorizationUri", "https://auth.example.com" );

    assertTrue( validateSaveAllowed() );
  }

  @Test
  public void testUpdateSsoControls_ssoSelected_enablesControls() throws Exception {
    radioSSO().setSelection( true );
    radioUsernamePassword().setSelection( false );
    lblSsoStatus().setText( "some previous text" );

    updateSsoControls();

    assertTrue( lblSsoProvider().isEnabled() );
    assertTrue( combo().isEnabled() );
    assertTrue( lblSsoStatus().isEnabled() );
    // Status text not cleared when SSO is selected
    assertEquals( "some previous text", lblSsoStatus().getText() );
  }

  @Test
  public void testUpdateSsoControls_usernamePasswordSelected_disablesAndClearsStatus() throws Exception {
    radioSSO().setSelection( false );
    radioUsernamePassword().setSelection( true );
    lblSsoStatus().setText( "some status" );

    updateSsoControls();

    assertFalse( lblSsoProvider().isEnabled() );
    assertFalse( combo().isEnabled() );
    assertFalse( lblSsoStatus().isEnabled() );
    assertEquals( "", lblSsoStatus().getText() );
  }

  @Test
  public void testLoadSsoProviders_ssoNotSelected_returnsEarlyWithoutFetch() throws Exception {
    radioSSO().setSelection( false );
    radioUsernamePassword().setSelection( true );

    loadSsoProviders( false );

    verify( mockSsoService, never() ).fetchProviders( anyString() );
  }

  @Test
  public void testLoadSsoProviders_emptyUrl_showsEnterUrlMessage() throws Exception {
    radioSSO().setSelection( true );
    radioUsernamePassword().setSelection( false );
    txtUrl().setText( "" );

    loadSsoProviders( false );

    assertEquals( BaseMessages.getString( PKG, "PentahoEnterpriseRepoForm.status.enterUrl" ),
      lblSsoStatus().getText() );
    verify( mockSsoService, never() ).fetchProviders( anyString() );
  }

  @Test
  public void testLoadSsoProviders_sameUrlCached_restoresFromCache() throws Exception {
    String url = "http://cached-server/pentaho";
    SsoProviderService.SsoProvider p =
      new SsoProviderService.SsoProvider( "Google", "https://auth.google.com", "google" );

    setField( "ssoProviders", Collections.singletonList( p ) );
    setField( "lastLoadedUrl", url );
    setField( "selectedAuthorizationUri", "https://auth.google.com" );

    radioSSO().setSelection( true );
    radioUsernamePassword().setSelection( false );
    txtUrl().setText( url );
    // Restore fields overwritten by setText's modify listener (SSO not selected during setText, so no side effects)
    setField( "ssoProviders", Collections.singletonList( p ) );
    setField( "lastLoadedUrl", url );
    setField( "selectedAuthorizationUri", "https://auth.google.com" );
    // Populate combo to represent an already-loaded state
    combo().setItems( "Google" );

    loadSsoProviders( true );

    // No network request should have been made
    verify( mockSsoService, never() ).fetchProviders( anyString() );
    assertEquals( 1, combo().getItemCount() );
  }

  @Test
  public void testLoadSsoProviders_freshFetch_success_populatesCombo() throws Exception {
    String url = "http://newserver/pentaho";
    SsoProviderService.SsoProvider p =
      new SsoProviderService.SsoProvider( "Google", "https://auth.google.com", "google" );
    when( mockSsoService.fetchProviders( url ) ).thenReturn( Collections.singletonList( p ) );

    setUrlAndSelectSSO( url );
    loadSsoProviders( false );

    waitForAsync( () -> combo().getItemCount() > 0, 3000 );

    assertEquals( 1, combo().getItemCount() );
    assertEquals( "Google", combo().getItem( 0 ) );
    assertEquals( "", lblSsoStatus().getText() );
  }

  @Test
  public void testLoadSsoProviders_freshFetch_noProviders_showsInfoMessage() throws Exception {
    String url = "http://newserver/pentaho";
    when( mockSsoService.fetchProviders( url ) ).thenReturn( Collections.emptyList() );

    setUrlAndSelectSSO( url );
    loadSsoProviders( false );

    waitForAsync( () -> BaseMessages.getString( PKG, "PentahoEnterpriseRepoForm.status.noProviders" )
      .equals( lblSsoStatus().getText() ), 3000 );

    assertEquals( BaseMessages.getString( PKG, "PentahoEnterpriseRepoForm.status.noProviders" ),
      lblSsoStatus().getText() );
    assertEquals( 0, combo().getItemCount() );
  }

  @Test
  public void testLoadSsoProviders_freshFetch_ioException_showsErrorMessage() throws Exception {
    String url = "http://newserver/pentaho";
    when( mockSsoService.fetchProviders( url ) ).thenThrow( new IOException( "Connection refused" ) );

    setUrlAndSelectSSO( url );
    loadSsoProviders( false );

    waitForAsync( () -> BaseMessages.getString( PKG, "PentahoEnterpriseRepoForm.status.loadError" )
      .equals( lblSsoStatus().getText() ), 3000 );

    assertEquals( BaseMessages.getString( PKG, "PentahoEnterpriseRepoForm.status.loadError" ),
      lblSsoStatus().getText() );
  }

  @Test
  public void testLoadSsoProviders_freshFetch_multipleProviders() throws Exception {
    String url = "http://server/pentaho";
    List<SsoProviderService.SsoProvider> providers = Arrays.asList(
      new SsoProviderService.SsoProvider( "Google", "https://auth.google.com", "google" ),
      new SsoProviderService.SsoProvider( "Okta", "https://auth.okta.com", "okta" )
    );
    when( mockSsoService.fetchProviders( url ) ).thenReturn( providers );

    setUrlAndSelectSSO( url );
    loadSsoProviders( false );

    waitForAsync( () -> combo().getItemCount() == 2, 3000 );

    assertEquals( 2, combo().getItemCount() );
    assertEquals( "Google", combo().getItem( 0 ) );
    assertEquals( "Okta", combo().getItem( 1 ) );
  }

  @Test
  public void testOnUrlFocusLost_ssoSelected_withPendingLoad_cancelsAndLoads() throws Exception {
    String url = "http://server/pentaho";
    when( mockSsoService.fetchProviders( url ) ).thenReturn( Collections.emptyList() );

    setUrlAndSelectSSO( url );

    Runnable pendingLoad = mock( Runnable.class );
    setField( "pendingProviderLoad", pendingLoad );

    onUrlFocusLost();

    // Pending load should have been cancelled (set to null)
    assertNull( getField( "pendingProviderLoad" ) );
    // And a fresh load should have started
    waitForAsync( () -> BaseMessages.getString( PKG, "PentahoEnterpriseRepoForm.status.noProviders" )
      .equals( lblSsoStatus().getText() ), 3000 );
  }

  @Test
  public void testOnUrlFocusLost_ssoSelected_noPendingLoad_loadsProviders() throws Exception {
    String url = "http://server/pentaho";
    when( mockSsoService.fetchProviders( url ) ).thenReturn( Collections.emptyList() );

    setUrlAndSelectSSO( url );
    setField( "pendingProviderLoad", null );

    onUrlFocusLost();

    waitForAsync( () -> BaseMessages.getString( PKG, "PentahoEnterpriseRepoForm.status.noProviders" )
      .equals( lblSsoStatus().getText() ), 3000 );
    assertEquals( BaseMessages.getString( PKG, "PentahoEnterpriseRepoForm.status.noProviders" ),
      lblSsoStatus().getText() );
  }

  @Test
  public void testOnUrlFocusLost_usernamePasswordSelected_doesNothing() throws Exception {
    radioSSO().setSelection( false );
    radioUsernamePassword().setSelection( true );

    onUrlFocusLost();

    verify( mockSsoService, never() ).fetchProviders( anyString() );
  }

  @Test
  public void testUrlModifyListener_ssoNotSelected_clearsLastLoadedUrl_andReturnsEarly() throws Exception {
    radioSSO().setSelection( false );
    radioUsernamePassword().setSelection( true );
    setField( "lastLoadedUrl", "http://prev" );

    // setText fires the modify listener
    txtUrl().setText( "http://changed/pentaho" );

    assertNull( getField( "lastLoadedUrl" ) );
    assertNull( getField( "pendingProviderLoad" ) );
  }

  @Test
  public void testUrlModifyListener_ssoSelected_emptyUrl_disablesComboAndShowsMessage() throws Exception {
    radioSSO().setSelection( true );
    radioUsernamePassword().setSelection( false );
    // Ensure the listener path for empty URL is taken
    txtUrl().setText( "http://previous" ); // set first so clearing triggers the right branch
    radioSSO().setSelection( true ); // keep SSO selected
    // Cancel any pending load scheduled by the previous setText
    Runnable pending = (Runnable) getField( "pendingProviderLoad" );
    if ( pending != null ) {
      display.timerExec( -1, pending );
      setField( "pendingProviderLoad", null );
    }

    txtUrl().setText( "" ); // empty â†’ triggers "Enter a URL" branch

    assertFalse( combo().isEnabled() );
    assertEquals( BaseMessages.getString( PKG, "PentahoEnterpriseRepoForm.status.enterUrl" ),
      lblSsoStatus().getText() );
    assertNull( getField( "pendingProviderLoad" ) );
  }

  @Test
  public void testUrlModifyListener_ssoSelected_nonEmptyUrl_schedulesDebounce() throws Exception {
    radioSSO().setSelection( true );
    radioUsernamePassword().setSelection( false );

    txtUrl().setText( "http://server/pentaho" );

    assertFalse( combo().isEnabled() );
    assertNotNull( getField( "pendingProviderLoad" ) );
    // Clean up the scheduled timer
    Runnable pending = (Runnable) getField( "pendingProviderLoad" );
    display.timerExec( -1, pending );
    setField( "pendingProviderLoad", null );
  }

  @Test
  public void testUrlModifyListener_ssoSelected_secondChange_cancelsPreviousPending() throws Exception {
    radioSSO().setSelection( true );
    radioUsernamePassword().setSelection( false );

    txtUrl().setText( "http://server1/pentaho" );
    Runnable firstPending = (Runnable) getField( "pendingProviderLoad" );
    assertNotNull( firstPending );

    txtUrl().setText( "http://server2/pentaho" );
    Runnable secondPending = (Runnable) getField( "pendingProviderLoad" );
    assertNotNull( secondPending );
    assertNotSame( firstPending, secondPending );

    // Clean up
    display.timerExec( -1, secondPending );
    setField( "pendingProviderLoad", null );
  }

  @Test
  public void testPendingProviderLoad_lambda_whenSsoStillSelected_triggersLoad() throws Exception {
    String url = "http://server/pentaho";
    when( mockSsoService.fetchProviders( url ) ).thenReturn( Collections.emptyList() );

    // Schedule the debounced load
    radioSSO().setSelection( true );
    radioUsernamePassword().setSelection( false );
    txtUrl().setText( url );

    Runnable pending = (Runnable) getField( "pendingProviderLoad" );
    assertNotNull( pending );

    // Simulate the timer firing by running the lambda directly
    pending.run();

    assertNull( getField( "pendingProviderLoad" ) );

    waitForAsync( () -> BaseMessages.getString( PKG, "PentahoEnterpriseRepoForm.status.noProviders" )
      .equals( lblSsoStatus().getText() ), 3000 );
  }

  @Test
  public void testPendingProviderLoad_lambda_whenSsoDeselected_skipsLoad() throws Exception {
    radioSSO().setSelection( true );
    radioUsernamePassword().setSelection( false );
    txtUrl().setText( "http://server/pentaho" );

    Runnable pending = (Runnable) getField( "pendingProviderLoad" );
    assertNotNull( pending );

    // Deselect SSO before the timer fires
    radioSSO().setSelection( false );
    radioUsernamePassword().setSelection( true );

    pending.run();

    assertNull( getField( "pendingProviderLoad" ) );
    verify( mockSsoService, never() ).fetchProviders( anyString() );
  }

  @Test
  public void testApplyFetchedProviders_ssoDeselected_returnsEarly() throws Exception {
    radioSSO().setSelection( false );
    radioUsernamePassword().setSelection( true );
    txtUrl().setText( "http://server/pentaho" );

    applyFetchedProviders( "http://server/pentaho", Collections.emptyList(), null, false );

    // Combo should be unaffected (method returned early)
    assertEquals( 0, combo().getItemCount() );
  }

  @Test
  public void testApplyFetchedProviders_staleUrl_discards() throws Exception {
    setUrlAndSelectSSO( "http://server2/pentaho" );

    applyFetchedProviders( "http://server1/pentaho", Collections.emptyList(), null, false );

    // Results from server1 should be discarded
    assertEquals( 0, combo().getItemCount() );
  }

  @Test
  public void testApplyFetchedProviders_exception_comboHasNoText_clearsAndShowsError() throws Exception {
    setUrlAndSelectSSO( "http://server/pentaho" );
    // Combo has no selection (getText returns "")

    applyFetchedProviders( "http://server/pentaho", Collections.emptyList(),
      new IOException( "Network error" ), false );

    assertEquals( BaseMessages.getString( PKG, "PentahoEnterpriseRepoForm.status.loadError" ),
      lblSsoStatus().getText() );
    assertEquals( 0, combo().getItemCount() );
    assertNull( getField( "selectedAuthorizationUri" ) );
  }

  @Test
  public void testApplyFetchedProviders_exception_comboHasText_keepsItemsAndShowsError() throws Exception {
    // Set up: SSO selected, URL set, combo has an existing selection
    setUrlAndSelectSSO( "http://server/pentaho" );
    combo().setItems( "Google" );
    combo().select( 0 ); // getText() now returns "Google"

    applyFetchedProviders( "http://server/pentaho", Collections.emptyList(),
      new IOException( "Network error" ), false );

    // Combo items should be preserved (not cleared) when there's an existing selection
    assertEquals( 1, combo().getItemCount() );
    assertEquals( BaseMessages.getString( PKG, "PentahoEnterpriseRepoForm.status.loadError" ),
      lblSsoStatus().getText() );
  }

  @Test
  public void testRestoreProviderSelection_emptyCombo_clearsSelection() throws Exception {
    combo().removeAll();
    setField( "selectedAuthorizationUri", "https://auth.example.com" );
    setField( "selectedRegistrationId", "reg" );

    restoreProviderSelection( true );

    assertNull( getField( "selectedAuthorizationUri" ) );
    assertNull( getField( "selectedRegistrationId" ) );
  }

  @Test
  public void testRestoreProviderSelection_byAuthUri_selectsMatchingProvider() throws Exception {
    SsoProviderService.SsoProvider p0 =
      new SsoProviderService.SsoProvider( "Google", "https://auth.google.com", "google" );
    SsoProviderService.SsoProvider p1 = new SsoProviderService.SsoProvider( "Okta", "https://auth.okta.com", "okta" );
    setField( "ssoProviders", Arrays.asList( p0, p1 ) );
    setField( "selectedAuthorizationUri", "https://auth.okta.com" );

    combo().setItems( "Google", "Okta" );

    restoreProviderSelection( true );

    assertEquals( 1, combo().getSelectionIndex() );
    assertEquals( "https://auth.okta.com", getField( "selectedAuthorizationUri" ) );
    assertEquals( "okta", getField( "selectedRegistrationId" ) );
  }

  @Test
  public void testRestoreProviderSelection_authUriFallsThrough_selectsByExistingText() throws Exception {
    SsoProviderService.SsoProvider p =
      new SsoProviderService.SsoProvider( "Google", "https://auth.google.com", "google" );
    setField( "ssoProviders", Collections.singletonList( p ) );
    setField( "selectedAuthorizationUri", null );

    combo().setItems( "Google" );
    combo().select( 0 ); // getText() = "Google"

    restoreProviderSelection( false ); // preserveCurrentSelection=false â†’ skip authUri lookup

    assertEquals( 0, combo().getSelectionIndex() );
    assertEquals( "https://auth.google.com", getField( "selectedAuthorizationUri" ) );
  }

  @Test
  public void testRestoreProviderSelection_notFoundAnywhere_defaultsToFirstItem() throws Exception {
    SsoProviderService.SsoProvider p =
      new SsoProviderService.SsoProvider( "Google", "https://auth.google.com", "google" );
    setField( "ssoProviders", Collections.singletonList( p ) );
    setField( "selectedAuthorizationUri", null );

    combo().setItems( "Google" );
    combo().deselectAll(); // getText() = ""

    restoreProviderSelection( false );

    assertEquals( 0, combo().getSelectionIndex() );
    assertEquals( "https://auth.google.com", getField( "selectedAuthorizationUri" ) );
  }

  @Test
  public void testRestoreProviderSelection_preserveTrue_authUriNotFound_usesText() throws Exception {
    SsoProviderService.SsoProvider p =
      new SsoProviderService.SsoProvider( "Google", "https://auth.google.com", "google" );
    setField( "ssoProviders", Collections.singletonList( p ) );
    // AuthUri that doesn't match anything
    setField( "selectedAuthorizationUri", "https://unknown.example.com" );

    combo().setItems( "Google" );
    combo().select( 0 ); // getText() = "Google"

    restoreProviderSelection( true );

    // AuthUri lookup returned -1. Falls through to text-based lookup ("Google" â†’ index 0)
    assertEquals( 0, combo().getSelectionIndex() );
  }

  @Test
  public void testApplySelectedProvider_validIndex_setsFields() throws Exception {
    SsoProviderService.SsoProvider p =
      new SsoProviderService.SsoProvider( "Google", "https://auth.google.com", "google" );
    setField( "ssoProviders", Collections.singletonList( p ) );
    combo().setItems( "Google" );
    combo().select( 0 );

    applySelectedProvider();

    assertEquals( "https://auth.google.com", getField( "selectedAuthorizationUri" ) );
    assertEquals( "google", getField( "selectedRegistrationId" ) );
  }

  @Test
  public void testApplySelectedProvider_negativeIndex_clearsFields() throws Exception {
    setField( "ssoProviders", Collections.singletonList(
      new SsoProviderService.SsoProvider( "Google", "https://auth.google.com", "google" ) ) );
    setField( "selectedAuthorizationUri", "previously-set" );
    setField( "selectedRegistrationId", "previously-set" );
    combo().removeAll(); // selectionIndex = -1

    applySelectedProvider();

    assertNull( getField( "selectedAuthorizationUri" ) );
    assertNull( getField( "selectedRegistrationId" ) );
  }

  @Test
  public void testApplySelectedProvider_outOfBounds_clearsFields() throws Exception {
    // providers list is empty but combo has an item selected at index 0
    setField( "ssoProviders", Collections.emptyList() );
    setField( "selectedAuthorizationUri", "previously-set" );
    combo().setItems( "something" );
    combo().select( 0 ); // index 0 >= ssoProviders.size() (0)

    applySelectedProvider();

    assertNull( getField( "selectedAuthorizationUri" ) );
    assertNull( getField( "selectedRegistrationId" ) );
  }

  @Test
  public void testClearProviderSelection_clearComboTrue_removesItemsAndClearsFields() throws Exception {
    combo().setItems( "Google", "Okta" );
    setField( "selectedAuthorizationUri", "https://auth.google.com" );
    setField( "selectedRegistrationId", "google" );

    clearProviderSelection( true );

    assertNull( getField( "selectedAuthorizationUri" ) );
    assertNull( getField( "selectedRegistrationId" ) );
    assertEquals( 0, combo().getItemCount() );
  }

  @Test
  public void testClearProviderSelection_clearComboFalse_keepsItemsButClearsFields() throws Exception {
    combo().setItems( "Google", "Okta" );
    setField( "selectedAuthorizationUri", "https://auth.google.com" );
    setField( "selectedRegistrationId", "google" );

    clearProviderSelection( false );

    assertNull( getField( "selectedAuthorizationUri" ) );
    assertNull( getField( "selectedRegistrationId" ) );
    assertEquals( 2, combo().getItemCount() ); // items preserved
  }

  @Test
  public void testSetSsoStatusError_setsRedForegroundAndText() throws Exception {
    setSsoStatusError( "An error occurred" );

    assertEquals( "An error occurred", lblSsoStatus().getText() );
    assertEquals( display.getSystemColor( SWT.COLOR_RED ), lblSsoStatus().getForeground() );
  }

  @Test
  public void testSetSsoStatusInfo_emptyText_setsDefaultColor() throws Exception {
    // First set to red to verify color resets
    setSsoStatusError( "error" );
    setSsoStatusInfo( "" );

    assertEquals( "", lblSsoStatus().getText() );
    assertEquals( display.getSystemColor( SWT.COLOR_WIDGET_FOREGROUND ), lblSsoStatus().getForeground() );
  }

  @Test
  public void testSetSsoStatusInfo_withText_setsDefaultColorAndText() throws Exception {
    String loadingMsg = BaseMessages.getString( PKG, "PentahoEnterpriseRepoForm.status.loading" );
    setSsoStatusInfo( loadingMsg );

    assertEquals( loadingMsg, lblSsoStatus().getText() );
    assertEquals( display.getSystemColor( SWT.COLOR_WIDGET_FOREGROUND ), lblSsoStatus().getForeground() );
  }

  @Test
  public void testStringValue_null_returnsNull() throws Exception {
    assertNull( stringValue( null ) );
  }

  @Test
  public void testStringValue_string_returnsSameString() throws Exception {
    assertEquals( "hello", stringValue( "hello" ) );
  }

  @Test
  public void testStringValue_integer_returnsStringRepresentation() throws Exception {
    assertEquals( "42", stringValue( 42 ) );
  }

  @Test
  public void testFindProviderIndexByAuthorizationUri_found_returnsCorrectIndex() throws Exception {
    SsoProviderService.SsoProvider p0 = new SsoProviderService.SsoProvider( "A", "https://a.com", "a" );
    SsoProviderService.SsoProvider p1 = new SsoProviderService.SsoProvider( "B", "https://b.com", "b" );
    setField( "ssoProviders", Arrays.asList( p0, p1 ) );

    assertEquals( 1, findProviderIndexByAuthorizationUri( "https://b.com" ) );
  }

  @Test
  public void testFindProviderIndexByAuthorizationUri_notFound_returnsMinusOne() throws Exception {
    SsoProviderService.SsoProvider p = new SsoProviderService.SsoProvider( "A", "https://a.com", "a" );
    setField( "ssoProviders", Collections.singletonList( p ) );

    assertEquals( -1, findProviderIndexByAuthorizationUri( "https://notfound.com" ) );
  }

  @Test
  public void testFindProviderIndexByAuthorizationUri_emptyList_returnsMinusOne() throws Exception {
    setField( "ssoProviders", Collections.emptyList() );

    assertEquals( -1, findProviderIndexByAuthorizationUri( "https://any.com" ) );
  }

  @Test
  public void testPopulate_sso_withProviderName_restoresSelectionAfterFetch() throws Exception {
    String url = "http://server/pentaho";
    List<SsoProviderService.SsoProvider> providers = Arrays.asList(
      new SsoProviderService.SsoProvider( "Other IdP", "https://auth.other.com", "other" ),
      new SsoProviderService.SsoProvider( "My IdP", "https://auth.myidp.com", "myidp-reg" )
    );
    when( mockSsoService.fetchProviders( url ) ).thenReturn( providers );

    JSONObject src = new JSONObject();
    src.put( "url", url );
    src.put( "authMethod", "SSO" );
    src.put( "ssoProviderName", "My IdP" );
    src.put( "ssoAuthorizationUri", "https://auth.myidp.com" );
    src.put( "ssoRegistrationId", "myidp-reg" );

    composite.populate( src );

    // Wait for async fetch to complete and restore selection
    waitForAsync( () -> combo().getItemCount() == 2, 3000 );

    assertEquals( 2, combo().getItemCount() );
    // Should have restored the saved provider by matching authorizationUri
    assertEquals( 1, combo().getSelectionIndex() );
    assertEquals( "https://auth.myidp.com", getField( "selectedAuthorizationUri" ) );
    assertEquals( "myidp-reg", getField( "selectedRegistrationId" ) );
  }

  @Test
  public void testPopulate_usernamePassword_doesNotTriggerProviderLoad() throws Exception {
    JSONObject src = new JSONObject();
    src.put( "url", "http://server/pentaho" );
    src.put( "authMethod", "USERNAME_PASSWORD" );

    composite.populate( src );

    while ( display.readAndDispatch() ) {
      // flush
    }

    verify( mockSsoService, never() ).fetchProviders( anyString() );
  }

  @Test
  public void testApplyFetchedProviders_preserveTrue_restoresSavedAuthUriAndRegId() throws Exception {
    String url = "http://server/pentaho";
    SsoProviderService.SsoProvider p0 =
      new SsoProviderService.SsoProvider( "Google", "https://auth.google.com", "google" );
    SsoProviderService.SsoProvider p1 =
      new SsoProviderService.SsoProvider( "Okta", "https://auth.okta.com", "okta" );
    List<SsoProviderService.SsoProvider> providers = Arrays.asList( p0, p1 );

    setUrlAndSelectSSO( url );

    // Call applyFetchedProviders with preserveCurrentSelection=true and saved values
    Method m = PentahoEnterpriseRepoFormComposite.class.getDeclaredMethod(
      "applyFetchedProviders", String.class, List.class, Exception.class, boolean.class,
      String.class, String.class );
    m.setAccessible( true );
    m.invoke( composite, url, providers, null, true,
      "https://auth.okta.com", "okta" );

    // Should restore saved selection and match the Okta provider (index 1)
    assertEquals( 1, combo().getSelectionIndex() );
    assertEquals( "https://auth.okta.com", getField( "selectedAuthorizationUri" ) );
    assertEquals( "okta", getField( "selectedRegistrationId" ) );
  }

  @Test
  public void testApplyFetchedProviders_preserveFalse_doesNotRestoreSavedValues() throws Exception {
    String url = "http://server/pentaho";
    SsoProviderService.SsoProvider p0 =
      new SsoProviderService.SsoProvider( "Google", "https://auth.google.com", "google" );
    SsoProviderService.SsoProvider p1 =
      new SsoProviderService.SsoProvider( "Okta", "https://auth.okta.com", "okta" );
    List<SsoProviderService.SsoProvider> providers = Arrays.asList( p0, p1 );

    setUrlAndSelectSSO( url );

    // Call applyFetchedProviders with preserveCurrentSelection=false
    Method m = PentahoEnterpriseRepoFormComposite.class.getDeclaredMethod(
      "applyFetchedProviders", String.class, List.class, Exception.class, boolean.class,
      String.class, String.class );
    m.setAccessible( true );
    m.invoke( composite, url, providers, null, false,
      "https://auth.okta.com", "okta" );

    // Should default to first provider (index 0) since preserve=false and no prior selection
    assertEquals( 0, combo().getSelectionIndex() );
    assertEquals( "https://auth.google.com", getField( "selectedAuthorizationUri" ) );
    assertEquals( "google", getField( "selectedRegistrationId" ) );
  }

  @Test
  public void testLoadSsoProviders_preserve_savesAndRestoresSelection() throws Exception {
    String url = "http://server/pentaho";
    SsoProviderService.SsoProvider p0 =
      new SsoProviderService.SsoProvider( "Google", "https://auth.google.com", "google" );
    SsoProviderService.SsoProvider p1 =
      new SsoProviderService.SsoProvider( "Okta", "https://auth.okta.com", "okta" );
    when( mockSsoService.fetchProviders( url ) ).thenReturn( Arrays.asList( p0, p1 ) );

    setUrlAndSelectSSO( url );
    setField( "selectedAuthorizationUri", "https://auth.okta.com" );
    setField( "selectedRegistrationId", "okta" );

    loadSsoProviders( true );

    waitForAsync( () -> combo().getItemCount() == 2, 3000 );

    // Should restore Okta as the selected provider
    assertEquals( 1, combo().getSelectionIndex() );
    assertEquals( "https://auth.okta.com", getField( "selectedAuthorizationUri" ) );
    assertEquals( "okta", getField( "selectedRegistrationId" ) );
  }

  @Test
  public void testLoadSsoProviders_showsLoadingIndicatorDuringFetch() throws Exception {
    String url = "http://server/pentaho";
    // Use a latch to control the timing of the fetch
    final Object fetchLatch = new Object();
    when( mockSsoService.fetchProviders( url ) ).thenAnswer( invocation -> {
      synchronized ( fetchLatch ) {
        fetchLatch.wait( 500 );
      }
      return Collections.singletonList(
        new SsoProviderService.SsoProvider( "Google", "https://auth.google.com", "google" ) );
    } );

    setUrlAndSelectSSO( url );
    loadSsoProviders( false );

    // Immediately after calling loadSsoProviders, should show loading indicator
    assertEquals( BaseMessages.getString( PKG, "PentahoEnterpriseRepoForm.status.loading" ),
      lblSsoStatus().getText() );
    assertFalse( combo().isEnabled() );

    // Release the fetch
    synchronized ( fetchLatch ) {
      fetchLatch.notifyAll();
    }

    waitForAsync( () -> combo().getItemCount() > 0, 3000 );
    assertTrue( combo().isEnabled() );
  }

  @Test
  public void testToMap_sso_noProviderSelected_nullAuthUri() throws Exception {
    setUrlAndSelectSSO( "http://server/pentaho" );
    setField( "selectedAuthorizationUri", null );
    setField( "selectedRegistrationId", null );
    combo().removeAll();

    Map<String, Object> result = composite.toMap();

    assertEquals( "SSO", result.get( "authMethod" ) );
    assertNull( result.get( "ssoProviderName" ) );
    assertNull( result.get( "ssoAuthorizationUri" ) );
    assertNull( result.get( "ssoRegistrationId" ) );
  }

  @Test
  public void testRestoreProviderSelection_preserveTrue_matchesFirstProvider() throws Exception {
    SsoProviderService.SsoProvider p0 =
      new SsoProviderService.SsoProvider( "Google", "https://auth.google.com", "google" );
    SsoProviderService.SsoProvider p1 =
      new SsoProviderService.SsoProvider( "Okta", "https://auth.okta.com", "okta" );
    setField( "ssoProviders", Arrays.asList( p0, p1 ) );
    setField( "selectedAuthorizationUri", "https://auth.google.com" );

    combo().setItems( "Google", "Okta" );

    restoreProviderSelection( true );

    assertEquals( 0, combo().getSelectionIndex() );
    assertEquals( "https://auth.google.com", getField( "selectedAuthorizationUri" ) );
    assertEquals( "google", getField( "selectedRegistrationId" ) );
  }

  @Test
  public void testRestoreProviderSelection_preserveFalse_ignoresAuthUri_usesTextFallback() throws Exception {
    SsoProviderService.SsoProvider p0 =
      new SsoProviderService.SsoProvider( "Google", "https://auth.google.com", "google" );
    SsoProviderService.SsoProvider p1 =
      new SsoProviderService.SsoProvider( "Okta", "https://auth.okta.com", "okta" );
    setField( "ssoProviders", Arrays.asList( p0, p1 ) );
    // Even with a valid authorizationUri, preserve=false should skip the authUri match
    setField( "selectedAuthorizationUri", "https://auth.okta.com" );

    combo().setItems( "Google", "Okta" );
    combo().select( 1 ); // getText() = "Okta"

    restoreProviderSelection( false );

    // Since preserve=false, it skips authUri lookup and falls to text-based match ("Okta" â†’ index 1)
    assertEquals( 1, combo().getSelectionIndex() );
    assertEquals( "https://auth.okta.com", getField( "selectedAuthorizationUri" ) );
  }

  @Test
  public void testLoadSsoProviders_cachedWithProviders_noPreserve_restoresSelection() throws Exception {
    String url = "http://cached-server/pentaho";
    SsoProviderService.SsoProvider p0 =
      new SsoProviderService.SsoProvider( "Google", "https://auth.google.com", "google" );
    SsoProviderService.SsoProvider p1 =
      new SsoProviderService.SsoProvider( "Okta", "https://auth.okta.com", "okta" );

    setField( "ssoProviders", Arrays.asList( p0, p1 ) );
    setField( "lastLoadedUrl", url );

    radioSSO().setSelection( true );
    radioUsernamePassword().setSelection( false );
    txtUrl().setText( url );
    // Restore fields overwritten by setText's modify listener
    setField( "ssoProviders", Arrays.asList( p0, p1 ) );
    setField( "lastLoadedUrl", url );

    combo().setItems( "Google", "Okta" );
    combo().select( 1 );

    loadSsoProviders( false );

    // No network request should have been made
    verify( mockSsoService, never() ).fetchProviders( anyString() );
    // Should restore selection (no preserve, so text-based fallback selects "Okta" at index 1)
    assertEquals( 1, combo().getSelectionIndex() );
  }

  @Test
  public void testPopulate_sso_nullProviderName_doesNotPrePopulateCombo() throws Exception {
    when( mockSsoService.fetchProviders( anyString() ) ).thenReturn( Collections.emptyList() );

    JSONObject src = new JSONObject();
    src.put( "url", "http://server/pentaho" );
    src.put( "authMethod", "SSO" );
    // ssoProviderName not set â†’ will be null

    composite.populate( src );

    // Combo should not be pre-populated
    waitForAsync( () -> getField( "lastLoadedUrl" ) != null, 3000 );
    assertEquals( 0, combo().getItemCount() );
  }

  @Test
  public void testPopulate_sso_emptyProviderName_doesNotPrePopulateCombo() throws Exception {
    when( mockSsoService.fetchProviders( anyString() ) ).thenReturn( Collections.emptyList() );

    JSONObject src = new JSONObject();
    src.put( "url", "http://server/pentaho" );
    src.put( "authMethod", "SSO" );
    src.put( "ssoProviderName", "" );

    composite.populate( src );

    waitForAsync( () -> getField( "lastLoadedUrl" ) != null, 3000 );
    // Empty provider name should not add items to combo
    assertEquals( 0, combo().getItemCount() );
  }

  @Test
  public void testValidateSaveAllowed_emptyDisplayName_returnsFalse() throws Exception {
    txtDisplayName().setText( "" );
    txtUrl().setText( "http://server/pentaho" );

    assertFalse( validateSaveAllowed() );
  }

  @Test
  public void testFetchAndApplyProviders_endToEnd_preserveTrue() throws Exception {
    String url = "http://server/pentaho";
    SsoProviderService.SsoProvider p0 =
      new SsoProviderService.SsoProvider( "IdP A", "https://auth.a.com", "a" );
    SsoProviderService.SsoProvider p1 =
      new SsoProviderService.SsoProvider( "IdP B", "https://auth.b.com", "b" );
    when( mockSsoService.fetchProviders( url ) ).thenReturn( Arrays.asList( p0, p1 ) );

    setUrlAndSelectSSO( url );
    setField( "selectedAuthorizationUri", "https://auth.b.com" );
    setField( "selectedRegistrationId", "b" );

    // Invoke fetchAndApplyProviders directly (simulates the thread)
    Method m = PentahoEnterpriseRepoFormComposite.class.getDeclaredMethod(
      "fetchAndApplyProviders", String.class, boolean.class, String.class, String.class );
    m.setAccessible( true );
    m.invoke( composite, url, true, "https://auth.b.com", "b" );

    // Process the asyncExec callback
    waitForAsync( () -> combo().getItemCount() == 2, 3000 );

    assertEquals( 2, combo().getItemCount() );
    assertEquals( 1, combo().getSelectionIndex() );
    assertEquals( "https://auth.b.com", getField( "selectedAuthorizationUri" ) );
    assertEquals( "b", getField( "selectedRegistrationId" ) );
  }

  @Test
  public void testFetchAndApplyProviders_endToEnd_fetchException() throws Exception {
    String url = "http://server/pentaho";
    when( mockSsoService.fetchProviders( url ) ).thenThrow( new RuntimeException( "Server down" ) );

    setUrlAndSelectSSO( url );

    Method m = PentahoEnterpriseRepoFormComposite.class.getDeclaredMethod(
      "fetchAndApplyProviders", String.class, boolean.class, String.class, String.class );
    m.setAccessible( true );
    m.invoke( composite, url, false, null, null );

    waitForAsync( () -> BaseMessages.getString( PKG, "PentahoEnterpriseRepoForm.status.loadError" )
      .equals( lblSsoStatus().getText() ), 3000 );

    assertEquals( BaseMessages.getString( PKG, "PentahoEnterpriseRepoForm.status.loadError" ),
      lblSsoStatus().getText() );
  }

  @Test
  public void testSaveButtonState_changesWithValidation() throws Exception {
    // Initially, nothing changed yet, so save button should be disabled
    assertFalse( btnSave.getEnabled() );

    // Set valid display name and URL â€” triggers lsMod which sets changed=true
    txtDisplayName().setText( "My Repo" );
    txtUrl().setText( "http://server/pentaho" );

    // Save should be enabled now (changed=true, validateSaveAllowed()=true)
    assertTrue( btnSave.getEnabled() );
  }

  @Test
  public void testSaveButtonState_ssoWithoutAuthUri_disablesSave() throws Exception {
    txtDisplayName().setText( "My Repo" );
    setUrlAndSelectSSO( "http://server/pentaho" );
    setField( "selectedAuthorizationUri", null );
    // Trigger changed flag
    setField( "changed", true );
    composite.toMap(); // just to exercise the path

    // Force re-evaluation
    Method setSaveButtonEnabled = BaseRepoFormComposite.class.getDeclaredMethod( "setSaveButtonEnabled" );
    setSaveButtonEnabled.setAccessible( true );
    setSaveButtonEnabled.invoke( composite );

    assertFalse( btnSave.getEnabled() );
  }

  @Test
  public void testSaveButtonState_ssoWithAuthUri_enablesSave() throws Exception {
    txtDisplayName().setText( "My Repo" );
    setUrlAndSelectSSO( "http://server/pentaho" );
    setField( "selectedAuthorizationUri", "https://auth.example.com" );
    setField( "changed", true );

    Method setSaveButtonEnabled = BaseRepoFormComposite.class.getDeclaredMethod( "setSaveButtonEnabled" );
    setSaveButtonEnabled.setAccessible( true );
    setSaveButtonEnabled.invoke( composite );

    assertTrue( btnSave.getEnabled() );
  }

  @Test
  public void testFindProviderIndexByAuthorizationUri_firstElement() throws Exception {
    SsoProviderService.SsoProvider p0 = new SsoProviderService.SsoProvider( "A", "https://a.com", "a" );
    SsoProviderService.SsoProvider p1 = new SsoProviderService.SsoProvider( "B", "https://b.com", "b" );
    SsoProviderService.SsoProvider p2 = new SsoProviderService.SsoProvider( "C", "https://c.com", "c" );
    setField( "ssoProviders", Arrays.asList( p0, p1, p2 ) );

    assertEquals( 0, findProviderIndexByAuthorizationUri( "https://a.com" ) );
  }

  @Test
  public void testFindProviderIndexByAuthorizationUri_lastElement() throws Exception {
    SsoProviderService.SsoProvider p0 = new SsoProviderService.SsoProvider( "A", "https://a.com", "a" );
    SsoProviderService.SsoProvider p1 = new SsoProviderService.SsoProvider( "B", "https://b.com", "b" );
    SsoProviderService.SsoProvider p2 = new SsoProviderService.SsoProvider( "C", "https://c.com", "c" );
    setField( "ssoProviders", Arrays.asList( p0, p1, p2 ) );

    assertEquals( 2, findProviderIndexByAuthorizationUri( "https://c.com" ) );
  }

  @Test
  public void testToMap_includesBaseFields() {
    JSONObject src = new JSONObject();
    src.put( "url", "http://server/pentaho" );
    src.put( "displayName", "My Test Repo" );
    src.put( "description", "A test repository" );
    src.put( "isDefault", true );
    composite.populate( src );

    Map<String, Object> result = composite.toMap();

    assertEquals( "My Test Repo", result.get( "displayName" ) );
    assertEquals( "A test repository", result.get( "description" ) );
    assertTrue( (Boolean) result.get( "isDefault" ) );
    assertEquals( "PentahoEnterpriseRepository", result.get( "id" ) );
  }

  @Test
  public void testPopulate_preservesOriginalName() {
    JSONObject src = new JSONObject();
    src.put( "url", "http://server/pentaho" );
    src.put( "displayName", "Existing Repo" );
    composite.populate( src );

    Map<String, Object> result = composite.toMap();

    assertEquals( "Existing Repo", result.get( "originalName" ) );
  }

  @Test
  public void testUpdateSsoControls_ssoSelected_keepsExistingStatusText() throws Exception {
    String loadingMsg = BaseMessages.getString( PKG, "PentahoEnterpriseRepoForm.status.loading" );
    radioSSO().setSelection( true );
    radioUsernamePassword().setSelection( false );
    lblSsoStatus().setText( loadingMsg );

    updateSsoControls();

    // SSO selected should NOT clear the status text
    assertEquals( loadingMsg, lblSsoStatus().getText() );
  }

  @Test
  public void testApplyFetchedProviders_success_setsLastLoadedUrl() throws Exception {
    String url = "http://server/pentaho";
    SsoProviderService.SsoProvider p =
      new SsoProviderService.SsoProvider( "Google", "https://auth.google.com", "google" );

    setUrlAndSelectSSO( url );

    Method m = PentahoEnterpriseRepoFormComposite.class.getDeclaredMethod(
      "applyFetchedProviders", String.class, List.class, Exception.class, boolean.class,
      String.class, String.class );
    m.setAccessible( true );
    m.invoke( composite, url, Collections.singletonList( p ), null, false, null, null );

    assertEquals( url, getField( "lastLoadedUrl" ) );
  }

  @Test
  public void testApplyFetchedProviders_exception_doesNotSetLastLoadedUrl() throws Exception {
    String url = "http://server/pentaho";

    setUrlAndSelectSSO( url );
    setField( "lastLoadedUrl", null );

    Method m = PentahoEnterpriseRepoFormComposite.class.getDeclaredMethod(
      "applyFetchedProviders", String.class, List.class, Exception.class, boolean.class,
      String.class, String.class );
    m.setAccessible( true );
    m.invoke( composite, url, Collections.emptyList(),
      new IOException( "error" ), false, null, null );

    assertNull( getField( "lastLoadedUrl" ) );
  }

  @Test
  public void loadSsoProvidersAssignsProviderLoadFuture() throws Exception {
    String url = "http://server/pentaho";
    when( mockSsoService.fetchProviders( url ) ).thenReturn(
      Collections.singletonList(
        new SsoProviderService.SsoProvider( "Google", "https://auth.google.com", "google" ) ) );

    setUrlAndSelectSSO( url );
    loadSsoProviders( false );

    assertNotNull( getProviderLoadFuture() );

    waitForAsync( () -> combo().getItemCount() > 0, 3000 );
  }

  @Test
  public void providerLoadFutureIsDoneAfterFetchCompletes() throws Exception {
    String url = "http://server/pentaho";
    when( mockSsoService.fetchProviders( url ) ).thenReturn(
      Collections.singletonList(
        new SsoProviderService.SsoProvider( "Google", "https://auth.google.com", "google" ) ) );

    setUrlAndSelectSSO( url );
    loadSsoProviders( false );

    waitForAsync( () -> combo().getItemCount() > 0, 3000 );

    java.util.concurrent.Future<?> future =
      getProviderLoadFuture();
    assertNotNull( future );
    assertTrue( future.isDone() );
  }

  @Test
  public void cancelInFlightLoadCancelsRunningFuture() throws Exception {
    java.util.concurrent.CountDownLatch fetchStarted = new java.util.concurrent.CountDownLatch( 1 );
    java.util.concurrent.CountDownLatch fetchGate = new java.util.concurrent.CountDownLatch( 1 );
    String url = "http://server/pentaho";
    when( mockSsoService.fetchProviders( url ) ).thenAnswer( invocation -> {
      fetchStarted.countDown();
      fetchGate.await( 5, java.util.concurrent.TimeUnit.SECONDS );
      return Collections.singletonList(
        new SsoProviderService.SsoProvider( "Google", "https://auth.google.com", "google" ) );
    } );

    setUrlAndSelectSSO( url );
    loadSsoProviders( false );
    fetchStarted.await( 3, java.util.concurrent.TimeUnit.SECONDS );

    java.util.concurrent.Future<?> firstFuture =
      getProviderLoadFuture();
    assertNotNull( firstFuture );
    assertFalse( firstFuture.isDone() );

    invoke( "cancelInFlightLoad", new Class[ 0 ] );

    assertTrue( firstFuture.isCancelled() );
    assertNull( getProviderLoadFuture() );

    fetchGate.countDown();
  }

  @Test
  public void cancelInFlightLoadIsNoOpWhenNoFutureIsSet() throws Exception {
    assertNull( getProviderLoadFuture() );

    invoke( "cancelInFlightLoad", new Class[ 0 ] );

    assertNull( getProviderLoadFuture() );
  }

  @Test
  public void cancelInFlightLoadClearsReferenceForAlreadyCompletedFuture() throws Exception {
    String url = "http://server/pentaho";
    when( mockSsoService.fetchProviders( url ) ).thenReturn( Collections.emptyList() );

    setUrlAndSelectSSO( url );
    loadSsoProviders( false );

    waitForAsync( () -> {
      java.util.concurrent.Future<?> f =
        getProviderLoadFuture();
      return f != null && f.isDone();
    }, 3000 );

    java.util.concurrent.Future<?> doneFuture =
      getProviderLoadFuture();
    assertTrue( doneFuture.isDone() );

    invoke( "cancelInFlightLoad", new Class[ 0 ] );

    assertNull( getProviderLoadFuture() );
    assertFalse( doneFuture.isCancelled() );
  }

  @Test
  public void secondLoadCancelsFirstInFlightLoad() throws Exception {
    java.util.concurrent.CountDownLatch firstFetchStarted = new java.util.concurrent.CountDownLatch( 1 );
    java.util.concurrent.CountDownLatch firstFetchGate = new java.util.concurrent.CountDownLatch( 1 );
    String url1 = "http://server1/pentaho";
    String url2 = "http://server2/pentaho";

    when( mockSsoService.fetchProviders( url1 ) ).thenAnswer( invocation -> {
      firstFetchStarted.countDown();
      firstFetchGate.await( 5, java.util.concurrent.TimeUnit.SECONDS );
      return Collections.singletonList(
        new SsoProviderService.SsoProvider( "Stale", "https://stale.com", "stale" ) );
    } );
    when( mockSsoService.fetchProviders( url2 ) ).thenReturn(
      Collections.singletonList(
        new SsoProviderService.SsoProvider( "Fresh", "https://fresh.com", "fresh" ) ) );

    setUrlAndSelectSSO( url1 );
    loadSsoProviders( false );
    firstFetchStarted.await( 3, java.util.concurrent.TimeUnit.SECONDS );

    java.util.concurrent.Future<?> firstFuture =
      getProviderLoadFuture();

    setField( "lastLoadedUrl", null );
    setUrlAndSelectSSO( url2 );
    loadSsoProviders( false );

    assertTrue( firstFuture.isCancelled() );
    java.util.concurrent.Future<?> secondFuture =
      getProviderLoadFuture();
    assertNotSame( firstFuture, secondFuture );

    firstFetchGate.countDown();

    waitForAsync( () -> combo().getItemCount() == 1 && "Fresh".equals( combo().getItem( 0 ) ), 3000 );
    assertEquals( "Fresh", combo().getItem( 0 ) );
  }

  @Test
  public void disposeShutDownsExecutorAndCancelsInFlightLoad() throws Exception {
    java.util.concurrent.CountDownLatch fetchGate = new java.util.concurrent.CountDownLatch( 1 );
    String url = "http://server/pentaho";
    when( mockSsoService.fetchProviders( url ) ).thenAnswer( invocation -> {
      fetchGate.await( 5, java.util.concurrent.TimeUnit.SECONDS );
      return Collections.emptyList();
    } );

    setUrlAndSelectSSO( url );
    loadSsoProviders( false );

    java.util.concurrent.Future<?> future =
      getProviderLoadFuture();
    java.util.concurrent.ExecutorService executor =
      (java.util.concurrent.ExecutorService) getField( "providerLoadExecutor" );

    assertNotNull( future );
    assertFalse( executor.isShutdown() );

    composite.dispose();

    assertTrue( executor.isShutdown() );
    assertTrue( future.isCancelled() );

    fetchGate.countDown();
  }

  @Test
  public void disposeSafeWhenNoLoadIsInFlight() throws Exception {
    assertNull( getProviderLoadFuture() );

    java.util.concurrent.ExecutorService executor =
      (java.util.concurrent.ExecutorService) getField( "providerLoadExecutor" );
    assertFalse( executor.isShutdown() );

    composite.dispose();

    assertTrue( executor.isShutdown() );
  }

  @Test
  public void providerLoadExecutorUsesDaemonThread() throws Exception {
    java.util.concurrent.ExecutorService executor =
      (java.util.concurrent.ExecutorService) getField( "providerLoadExecutor" );

    java.util.concurrent.CompletableFuture<Boolean> isDaemon = new java.util.concurrent.CompletableFuture<>();
    executor.submit( () -> isDaemon.complete( Thread.currentThread().isDaemon() ) );

    assertTrue( isDaemon.get( 3, java.util.concurrent.TimeUnit.SECONDS ) );
  }

  @Test
  public void providerLoadExecutorThreadIsNamedSsoProviderLoader() throws Exception {
    java.util.concurrent.ExecutorService executor =
      (java.util.concurrent.ExecutorService) getField( "providerLoadExecutor" );

    java.util.concurrent.CompletableFuture<String> threadName = new java.util.concurrent.CompletableFuture<>();
    executor.submit( () -> threadName.complete( Thread.currentThread().getName() ) );

    assertEquals( "SSO-Provider-Loader",
      threadName.get( 3, java.util.concurrent.TimeUnit.SECONDS ) );
  }

  @Test
  public void executorSerializesConsecutiveLoads() throws Exception {
    String url1 = "http://server1/pentaho";
    String url2 = "http://server2/pentaho";

    java.util.concurrent.atomic.AtomicInteger concurrency = new java.util.concurrent.atomic.AtomicInteger( 0 );
    java.util.concurrent.atomic.AtomicInteger maxConcurrency = new java.util.concurrent.atomic.AtomicInteger( 0 );

    when( mockSsoService.fetchProviders( anyString() ) ).thenAnswer( invocation -> {
      int cur = concurrency.incrementAndGet();
      maxConcurrency.updateAndGet( max -> Math.max( max, cur ) );
      concurrency.decrementAndGet();
      return Collections.singletonList(
        new SsoProviderService.SsoProvider( "IdP", "https://auth.com", "idp" ) );
    } );

    setUrlAndSelectSSO( url1 );
    loadSsoProviders( false );

    setField( "lastLoadedUrl", null );
    setUrlAndSelectSSO( url2 );
    loadSsoProviders( false );

    waitForAsync( () -> {
      java.util.concurrent.Future<?> f =
        getProviderLoadFuture();
      return f != null && f.isDone();
    }, 5000 );

    assertTrue( maxConcurrency.get() <= 1 );
  }

  @Test
  public void staleFetchResultIsDiscardedWhenUrlChangedDuringLoad() throws Exception {
    java.util.concurrent.CountDownLatch fetchStarted = new java.util.concurrent.CountDownLatch( 1 );
    java.util.concurrent.CountDownLatch fetchGate = new java.util.concurrent.CountDownLatch( 1 );
    String url1 = "http://server1/pentaho";

    when( mockSsoService.fetchProviders( url1 ) ).thenAnswer( invocation -> {
      fetchStarted.countDown();
      fetchGate.await( 5, java.util.concurrent.TimeUnit.SECONDS );
      return Collections.singletonList(
        new SsoProviderService.SsoProvider( "Stale", "https://stale.com", "stale" ) );
    } );

    setUrlAndSelectSSO( url1 );
    loadSsoProviders( false );
    fetchStarted.await( 3, java.util.concurrent.TimeUnit.SECONDS );

    radioSSO().setSelection( false );
    radioUsernamePassword().setSelection( true );
    txtUrl().setText( "http://different-server/pentaho" );
    radioSSO().setSelection( true );
    radioUsernamePassword().setSelection( false );

    fetchGate.countDown();

    waitForAsync( () -> {
      java.util.concurrent.Future<?> f =
        getProviderLoadFuture();
      return f != null && f.isDone();
    }, 3000 );
    while ( display.readAndDispatch() ) { /* flush */ }

    assertEquals( 0, combo().getItemCount() );
  }

  @Test
  public void loadSsoProvidersDoesNotStartFetchWhenSsoNotSelected() throws Exception {
    radioSSO().setSelection( false );
    radioUsernamePassword().setSelection( true );
    txtUrl().setText( "http://server/pentaho" );

    loadSsoProviders( false );

    assertNull( getProviderLoadFuture() );
    verify( mockSsoService, never() ).fetchProviders( anyString() );
  }

  @Test
  public void loadSsoProvidersDoesNotStartFetchForEmptyUrl() throws Exception {
    radioSSO().setSelection( true );
    radioUsernamePassword().setSelection( false );
    txtUrl().setText( "" );

    loadSsoProviders( false );

    assertNull( getProviderLoadFuture() );
  }

  @Test
  public void providerLoadFutureIsNullOnFreshComposite() throws Exception {
    assertNull( getProviderLoadFuture() );
  }

  @Test
  public void providerLoadExecutorIsNotShutdownOnFreshComposite() throws Exception {
    java.util.concurrent.ExecutorService executor =
      (java.util.concurrent.ExecutorService) getField( "providerLoadExecutor" );
    assertFalse( executor.isShutdown() );
    assertFalse( executor.isTerminated() );
  }

  @Test
  public void cachedUrlSkipsExecutorAndDoesNotReplaceFuture() throws Exception {
    String url = "http://cached/pentaho";
    SsoProviderService.SsoProvider p =
      new SsoProviderService.SsoProvider( "Google", "https://auth.google.com", "google" );
    when( mockSsoService.fetchProviders( url ) ).thenReturn( Collections.singletonList( p ) );

    setUrlAndSelectSSO( url );
    loadSsoProviders( false );

    waitForAsync( () -> combo().getItemCount() > 0, 3000 );

    java.util.concurrent.Future<?> firstFuture =
      getProviderLoadFuture();
    assertNotNull( firstFuture );

    loadSsoProviders( true );

    java.util.concurrent.Future<?> sameFuture =
      getProviderLoadFuture();
    assertSame( firstFuture, sameFuture );
    verify( mockSsoService ).fetchProviders( url );
  }

  @Test
  public void cancelInFlightLoadCalledTwiceDoesNotThrow() throws Exception {
    java.util.concurrent.CountDownLatch fetchGate = new java.util.concurrent.CountDownLatch( 1 );
    String url = "http://server/pentaho";
    when( mockSsoService.fetchProviders( url ) ).thenAnswer( invocation -> {
      fetchGate.await( 5, java.util.concurrent.TimeUnit.SECONDS );
      return Collections.emptyList();
    } );

    setUrlAndSelectSSO( url );
    loadSsoProviders( false );

    invoke( "cancelInFlightLoad", new Class[ 0 ] );
    assertNull( getProviderLoadFuture() );

    invoke( "cancelInFlightLoad", new Class[ 0 ] );
    assertNull( getProviderLoadFuture() );

    fetchGate.countDown();
  }

  @Test
  public void threeRapidUrlChangesOnlyAppliesLastResult() throws Exception {
    java.util.concurrent.CountDownLatch gate1 = new java.util.concurrent.CountDownLatch( 1 );
    java.util.concurrent.CountDownLatch gate2 = new java.util.concurrent.CountDownLatch( 1 );
    String url1 = "http://server1/pentaho";
    String url2 = "http://server2/pentaho";
    String url3 = "http://server3/pentaho";

    when( mockSsoService.fetchProviders( url1 ) ).thenAnswer( invocation -> {
      gate1.await( 5, java.util.concurrent.TimeUnit.SECONDS );
      return Collections.singletonList(
        new SsoProviderService.SsoProvider( "First", "https://first.com", "first" ) );
    } );
    when( mockSsoService.fetchProviders( url2 ) ).thenAnswer( invocation -> {
      gate2.await( 5, java.util.concurrent.TimeUnit.SECONDS );
      return Collections.singletonList(
        new SsoProviderService.SsoProvider( "Second", "https://second.com", "second" ) );
    } );
    when( mockSsoService.fetchProviders( url3 ) ).thenReturn(
      Collections.singletonList(
        new SsoProviderService.SsoProvider( "Third", "https://third.com", "third" ) ) );

    setUrlAndSelectSSO( url1 );
    loadSsoProviders( false );
    java.util.concurrent.Future<?> f1 =
      getProviderLoadFuture();

    setField( "lastLoadedUrl", null );
    setUrlAndSelectSSO( url2 );
    loadSsoProviders( false );
    java.util.concurrent.Future<?> f2 =
      getProviderLoadFuture();
    assertTrue( f1.isCancelled() );

    setField( "lastLoadedUrl", null );
    setUrlAndSelectSSO( url3 );
    loadSsoProviders( false );
    assertTrue( f2.isCancelled() );

    gate1.countDown();
    gate2.countDown();

    waitForAsync( () -> combo().getItemCount() == 1 && "Third".equals( combo().getItem( 0 ) ), 3000 );
    assertEquals( "Third", combo().getItem( 0 ) );
    assertEquals( url3, getField( "lastLoadedUrl" ) );
  }

  @Test
  public void executorRejectsNewTasksAfterDispose() throws Exception {
    composite.dispose();

    java.util.concurrent.ExecutorService executor =
      (java.util.concurrent.ExecutorService) getField( "providerLoadExecutor" );
    assertTrue( executor.isShutdown() );

    try {
      executor.submit( () -> {} );
      assertTrue( "Expected rejection after shutdown", false );
    } catch ( java.util.concurrent.RejectedExecutionException e ) {
      // expected
    }
  }

  @Test
  public void endToEndLoadPopulatesComboViaExecutorAndAsyncExec() throws Exception {
    String url = "http://e2e-server/pentaho";
    SsoProviderService.SsoProvider p0 =
      new SsoProviderService.SsoProvider( "Okta", "https://okta.example.com", "okta" );
    SsoProviderService.SsoProvider p1 =
      new SsoProviderService.SsoProvider( "Azure", "https://azure.example.com", "azure" );
    when( mockSsoService.fetchProviders( url ) ).thenReturn( Arrays.asList( p0, p1 ) );

    setUrlAndSelectSSO( url );
    loadSsoProviders( false );

    waitForAsync( () -> combo().getItemCount() == 2, 3000 );

    assertEquals( "Okta", combo().getItem( 0 ) );
    assertEquals( "Azure", combo().getItem( 1 ) );
    assertEquals( 0, combo().getSelectionIndex() );
    assertEquals( "https://okta.example.com", getField( "selectedAuthorizationUri" ) );
    assertEquals( "okta", getField( "selectedRegistrationId" ) );
    assertEquals( url, getField( "lastLoadedUrl" ) );
    assertEquals( "", lblSsoStatus().getText() );
    assertTrue( combo().isEnabled() );
  }

  @Test
  public void loadAfterCancelStillWorksCorrectly() throws Exception {
    java.util.concurrent.CountDownLatch fetchGate = new java.util.concurrent.CountDownLatch( 1 );
    String url1 = "http://server1/pentaho";
    String url2 = "http://server2/pentaho";

    when( mockSsoService.fetchProviders( url1 ) ).thenAnswer( invocation -> {
      fetchGate.await( 5, java.util.concurrent.TimeUnit.SECONDS );
      return Collections.emptyList();
    } );
    when( mockSsoService.fetchProviders( url2 ) ).thenReturn(
      Collections.singletonList(
        new SsoProviderService.SsoProvider( "Google", "https://google.com", "google" ) ) );

    setUrlAndSelectSSO( url1 );
    loadSsoProviders( false );

    invoke( "cancelInFlightLoad", new Class[ 0 ] );
    fetchGate.countDown();

    setField( "lastLoadedUrl", null );
    setUrlAndSelectSSO( url2 );
    loadSsoProviders( false );

    waitForAsync( () -> combo().getItemCount() == 1, 3000 );
    assertEquals( "Google", combo().getItem( 0 ) );
  }

  @Test
  public void cancelInFlightLoadSafeWhenFutureAlreadyCancelled() throws Exception {
    java.util.concurrent.CountDownLatch fetchGate = new java.util.concurrent.CountDownLatch( 1 );
    String url = "http://server/pentaho";
    when( mockSsoService.fetchProviders( url ) ).thenAnswer( invocation -> {
      fetchGate.await( 5, java.util.concurrent.TimeUnit.SECONDS );
      return Collections.emptyList();
    } );

    setUrlAndSelectSSO( url );
    loadSsoProviders( false );

    java.util.concurrent.Future<?> future =
      getProviderLoadFuture();
    future.cancel( true );
    assertTrue( future.isCancelled() );
    assertTrue( future.isDone() );

    invoke( "cancelInFlightLoad", new Class[ 0 ] );

    assertNull( getProviderLoadFuture() );

    fetchGate.countDown();
  }

  @Test
  public void disposeCalledTwiceDoesNotThrow() throws Exception {
    java.util.concurrent.ExecutorService executor =
      (java.util.concurrent.ExecutorService) getField( "providerLoadExecutor" );

    composite.dispose();
    assertTrue( executor.isShutdown() );

    composite.dispose();
    assertTrue( executor.isShutdown() );
  }

  @Test
  public void disposeMarksWidgetAsDisposed() {
    assertFalse( composite.isDisposed() );

    composite.dispose();

    assertTrue( composite.isDisposed() );
  }

  @Test
  public void cancelledLoadDoesNotApplyResultsToCombo() throws Exception {
    java.util.concurrent.CountDownLatch fetchStarted = new java.util.concurrent.CountDownLatch( 1 );
    java.util.concurrent.CountDownLatch fetchGate = new java.util.concurrent.CountDownLatch( 1 );
    String url = "http://server/pentaho";
    when( mockSsoService.fetchProviders( url ) ).thenAnswer( invocation -> {
      fetchStarted.countDown();
      fetchGate.await( 5, java.util.concurrent.TimeUnit.SECONDS );
      return Collections.singletonList(
        new SsoProviderService.SsoProvider( "ShouldNotAppear", "https://gone.com", "gone" ) );
    } );

    setUrlAndSelectSSO( url );
    loadSsoProviders( false );
    fetchStarted.await( 3, java.util.concurrent.TimeUnit.SECONDS );

    invoke( "cancelInFlightLoad", new Class[ 0 ] );

    radioSSO().setSelection( false );
    radioUsernamePassword().setSelection( true );
    txtUrl().setText( "http://other/pentaho" );
    radioSSO().setSelection( true );
    radioUsernamePassword().setSelection( false );

    fetchGate.countDown();

    waitForAsync( () -> {
      java.util.concurrent.Future<?> f =
        getProviderLoadFuture();
      return f == null;
    }, 3000 );
    while ( display.readAndDispatch() ) { /* flush */ }

    assertEquals( 0, combo().getItemCount() );
    assertNull( getField( "selectedAuthorizationUri" ) );
  }

  @Test
  public void executorStillUsableAfterCancelInFlightLoad() throws Exception {
    java.util.concurrent.CountDownLatch fetchGate = new java.util.concurrent.CountDownLatch( 1 );
    String url1 = "http://server1/pentaho";
    String url2 = "http://server2/pentaho";

    when( mockSsoService.fetchProviders( url1 ) ).thenAnswer( invocation -> {
      fetchGate.await( 5, java.util.concurrent.TimeUnit.SECONDS );
      return Collections.emptyList();
    } );
    when( mockSsoService.fetchProviders( url2 ) ).thenReturn(
      Collections.singletonList(
        new SsoProviderService.SsoProvider( "Okta", "https://okta.com", "okta" ) ) );

    setUrlAndSelectSSO( url1 );
    loadSsoProviders( false );
    invoke( "cancelInFlightLoad", new Class[ 0 ] );
    fetchGate.countDown();

    java.util.concurrent.ExecutorService executor =
      (java.util.concurrent.ExecutorService) getField( "providerLoadExecutor" );
    assertFalse( executor.isShutdown() );

    setField( "lastLoadedUrl", null );
    setUrlAndSelectSSO( url2 );
    loadSsoProviders( false );

    assertNotNull( getProviderLoadFuture() );
    waitForAsync( () -> combo().getItemCount() == 1, 3000 );
    assertEquals( "Okta", combo().getItem( 0 ) );
  }

  @Test
  public void cancelInFlightLoadSafeWhenFutureCompletesJustBeforeCancel() throws Exception {
    String url = "http://server/pentaho";
    when( mockSsoService.fetchProviders( url ) ).thenReturn(
      Collections.singletonList(
        new SsoProviderService.SsoProvider( "Google", "https://auth.google.com", "google" ) ) );

    setUrlAndSelectSSO( url );
    loadSsoProviders( false );

    waitForAsync( () -> {
      java.util.concurrent.Future<?> f =
        getProviderLoadFuture();
      return f != null && f.isDone();
    }, 3000 );

    java.util.concurrent.Future<?> completedFuture =
      getProviderLoadFuture();
    assertTrue( completedFuture.isDone() );
    assertFalse( completedFuture.isCancelled() );

    invoke( "cancelInFlightLoad", new Class[ 0 ] );

    assertNull( getProviderLoadFuture() );
    assertFalse( completedFuture.isCancelled() );
  }

  @Test
  public void disposeWithCompletedFutureDoesNotFailOnCancel() throws Exception {
    String url = "http://server/pentaho";
    when( mockSsoService.fetchProviders( url ) ).thenReturn( Collections.emptyList() );

    setUrlAndSelectSSO( url );
    loadSsoProviders( false );

    waitForAsync( () -> {
      java.util.concurrent.Future<?> f =
        getProviderLoadFuture();
      return f != null && f.isDone();
    }, 3000 );

    java.util.concurrent.Future<?> completedFuture =
      getProviderLoadFuture();
    assertTrue( completedFuture.isDone() );

    composite.dispose();

    java.util.concurrent.ExecutorService executor =
      (java.util.concurrent.ExecutorService) getField( "providerLoadExecutor" );
    assertTrue( executor.isShutdown() );
    assertFalse( completedFuture.isCancelled() );
  }
}
