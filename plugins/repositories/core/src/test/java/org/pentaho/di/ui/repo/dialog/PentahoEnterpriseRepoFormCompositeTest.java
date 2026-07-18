/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/


package org.pentaho.di.ui.repo.dialog;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
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
    } catch ( SWTError | SWTException | UnsatisfiedLinkError | NoClassDefFoundError e ) {
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
      // Default: return empty list and OAuth disabled so no NPEs during async callbacks
      when( mock.fetchProviders( anyString() ) ).thenReturn( Collections.emptyList() );
      when( mock.isOAuthEnabled( anyString() ) ).thenReturn( false );
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

  private Composite authSection() throws Exception {
    return (Composite) getField( "authSection" );
  }

  private Composite ssoSection() throws Exception {
    return (Composite) getField( "ssoSection" );
  }

  private void enableOAuth() throws Exception {
    setField( "oAuthEnabled", true );
    invoke( "setAuthSectionVisible", new Class[] { boolean.class }, true );
  }

  private void setUrlAndSelectSSO( String url ) throws Exception {
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

  private void updateSsoControlsVisibility() throws Exception {
    invoke( "updateSsoControlsVisibility", new Class[ 0 ] );
  }

  private void checkOAuthAndLoadProviders( boolean preserve ) throws Exception {
    invoke( "checkOAuthAndLoadProviders", new Class[] { boolean.class }, preserve );
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

  // ============================== Auth Section Visibility Tests ==============================

  @Test
  public void testAuthSection_initiallyHidden() throws Exception {
    assertFalse( authSection().getVisible() );
    assertFalse( ssoSection().getVisible() );
  }

  @Test
  public void testAuthSection_hiddenWhenOAuthDisabled() throws Exception {
    enableOAuth();
    assertTrue( authSection().getVisible() );

    invoke( "setOAuthEnabled", new Class[] { boolean.class }, false );

    assertFalse( authSection().getVisible() );
    assertFalse( (boolean) getField( "oAuthEnabled" ) );
  }

  @Test
  public void testSsoSection_hiddenWhenUsernamePasswordSelected() throws Exception {
    enableOAuth();
    radioUsernamePassword().setSelection( true );
    radioSSO().setSelection( false );

    updateSsoControlsVisibility();

    assertFalse( ssoSection().getVisible() );
  }

  @Test
  public void testSsoSection_visibleWhenSsoSelected() throws Exception {
    enableOAuth();
    radioSSO().setSelection( true );
    radioUsernamePassword().setSelection( false );

    updateSsoControlsVisibility();

    assertTrue( ssoSection().getVisible() );
  }

  @Test
  public void testSetOAuthEnabled_false_forcesUsernamePassword() throws Exception {
    enableOAuth();
    radioSSO().setSelection( true );
    radioUsernamePassword().setSelection( false );

    invoke( "setOAuthEnabled", new Class[] { boolean.class }, false );

    assertTrue( radioUsernamePassword().getSelection() );
    assertFalse( radioSSO().getSelection() );
    assertFalse( authSection().getVisible() );
  }

  @Test
  public void testCheckOAuthAndLoadProviders_oauthEnabled_showsAuthSection() throws Exception {
    String url = "http://server/pentaho";
    when( mockSsoService.isOAuthEnabled( url ) ).thenReturn( true );
    when( mockSsoService.fetchProviders( url ) ).thenReturn( Collections.emptyList() );

    txtUrl().setText( url );
    Runnable pending = (Runnable) getField( "pendingOAuthCheck" );
    if ( pending != null ) {
      display.timerExec( -1, pending );
      setField( "pendingOAuthCheck", null );
    }

    checkOAuthAndLoadProviders( false );

    waitForAsync( () -> getField( "lastOAuthCheckUrl" ) != null, 3000 );

    assertTrue( (boolean) getField( "oAuthEnabled" ) );
    assertTrue( authSection().getVisible() );
  }

  @Test
  public void testCheckOAuthAndLoadProviders_oauthDisabled_hidesAuthSection() throws Exception {
    enableOAuth();
    String url = "http://server/pentaho";
    when( mockSsoService.isOAuthEnabled( url ) ).thenReturn( false );

    txtUrl().setText( url );
    Runnable pending = (Runnable) getField( "pendingOAuthCheck" );
    if ( pending != null ) {
      display.timerExec( -1, pending );
      setField( "pendingOAuthCheck", null );
    }

    checkOAuthAndLoadProviders( false );

    waitForAsync( () -> getField( "lastOAuthCheckUrl" ) != null, 3000 );

    assertFalse( (boolean) getField( "oAuthEnabled" ) );
    assertFalse( authSection().getVisible() );
  }

  @Test
  public void testCheckOAuthAndLoadProviders_oauthEnabled_ssoSelected_loadsProviders() throws Exception {
    String url = "http://server/pentaho";
    SsoProviderService.SsoProvider p =
      new SsoProviderService.SsoProvider( "Google", "https://auth.google.com", "google" );
    when( mockSsoService.isOAuthEnabled( url ) ).thenReturn( true );
    when( mockSsoService.fetchProviders( url ) ).thenReturn( Collections.singletonList( p ) );

    setUrlAndSelectSSO( url );
    checkOAuthAndLoadProviders( false );

    waitForAsync( () -> combo().getItemCount() > 0, 3000 );

    assertTrue( (boolean) getField( "oAuthEnabled" ) );
    assertEquals( 1, combo().getItemCount() );
    assertEquals( "Google", combo().getItem( 0 ) );
  }

  @Test
  public void testCheckOAuthAndLoadProviders_oauthEnabled_usernamePasswordSelected_noProviderApplied() throws Exception {
    String url = "http://server/pentaho";
    when( mockSsoService.isOAuthEnabled( url ) ).thenReturn( true );
    when( mockSsoService.fetchProviders( url ) ).thenReturn(
      Collections.singletonList( new SsoProviderService.SsoProvider( "G", "https://g.com", "g" ) ) );

    radioSSO().setSelection( false );
    radioUsernamePassword().setSelection( true );
    txtUrl().setText( url );
    Runnable pending = (Runnable) getField( "pendingOAuthCheck" );
    if ( pending != null ) {
      display.timerExec( -1, pending );
      setField( "pendingOAuthCheck", null );
    }

    checkOAuthAndLoadProviders( false );

    waitForAsync( () -> getField( "lastOAuthCheckUrl" ) != null, 3000 );

    assertTrue( (boolean) getField( "oAuthEnabled" ) );
    assertTrue( authSection().getVisible() );
    assertEquals( 0, combo().getItemCount() );
  }

  // ============================== Populate Tests ==============================

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
  public void testPopulate_sso_triggersOAuthCheck() throws Exception {
    when( mockSsoService.isOAuthEnabled( anyString() ) ).thenReturn( true );
    when( mockSsoService.fetchProviders( anyString() ) ).thenReturn( Collections.emptyList() );

    JSONObject src = new JSONObject();
    src.put( "url", "http://server/pentaho" );
    src.put( "authMethod", "SSO" );
    composite.populate( src );

    waitForAsync( () -> "http://server/pentaho".equals( getField( "lastOAuthCheckUrl" ) ), 3000 );
    assertEquals( "http://server/pentaho", getField( "lastOAuthCheckUrl" ) );
    assertTrue( (boolean) getField( "oAuthEnabled" ) );
  }

  @Test
  public void testPopulate_sso_withProviderName_restoresSelectionAfterFetch() throws Exception {
    String url = "http://server/pentaho";
    List<SsoProviderService.SsoProvider> providers = Arrays.asList(
      new SsoProviderService.SsoProvider( "Other IdP", "https://auth.other.com", "other" ),
      new SsoProviderService.SsoProvider( "My IdP", "https://auth.myidp.com", "myidp-reg" )
    );
    when( mockSsoService.isOAuthEnabled( url ) ).thenReturn( true );
    when( mockSsoService.fetchProviders( url ) ).thenReturn( providers );

    JSONObject src = new JSONObject();
    src.put( "url", url );
    src.put( "authMethod", "SSO" );
    src.put( "ssoProviderName", "My IdP" );
    src.put( "ssoAuthorizationUri", "https://auth.myidp.com" );
    src.put( "ssoRegistrationId", "myidp-reg" );

    composite.populate( src );

    waitForAsync( () -> combo().getItemCount() == 2, 3000 );

    assertEquals( 2, combo().getItemCount() );
    assertEquals( 1, combo().getSelectionIndex() );
    assertEquals( "https://auth.myidp.com", getField( "selectedAuthorizationUri" ) );
    assertEquals( "myidp-reg", getField( "selectedRegistrationId" ) );
  }

  @Test
  public void testPopulate_usernamePassword_doesNotShowAuthSectionIfOAuthDisabled() throws Exception {
    when( mockSsoService.isOAuthEnabled( anyString() ) ).thenReturn( false );

    JSONObject src = new JSONObject();
    src.put( "url", "http://server/pentaho" );
    src.put( "authMethod", "USERNAME_PASSWORD" );

    composite.populate( src );

    waitForAsync( () -> getField( "lastOAuthCheckUrl" ) != null, 3000 );

    verify( mockSsoService, never() ).fetchProviders( anyString() );
    assertFalse( authSection().getVisible() );
  }

  // ============================== toMap Tests ==============================

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
  public void testToMap_sso_withProvider_oauthEnabled() throws Exception {
    enableOAuth();
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
  public void testToMap_sso_selected_but_oauthDisabled_reportsUsernamePassword() throws Exception {
    setField( "oAuthEnabled", false );
    setUrlAndSelectSSO( "http://server/pentaho" );
    setField( "selectedAuthorizationUri", "https://auth.example.com" );

    Map<String, Object> result = composite.toMap();

    assertEquals( "USERNAME_PASSWORD", result.get( "authMethod" ) );
    assertNull( result.get( "ssoAuthorizationUri" ) );
  }

  @Test
  public void testToMap_sso_noComboSelection() throws Exception {
    enableOAuth();
    setUrlAndSelectSSO( "http://server/pentaho" );
    combo().removeAll();

    Map<String, Object> result = composite.toMap();

    assertEquals( "SSO", result.get( "authMethod" ) );
    assertNull( result.get( "ssoProviderName" ) );
    assertNull( result.get( "ssoAuthorizationUri" ) );
  }

  // ============================== Validate Tests ==============================

  @Test
  public void testValidateSaveAllowed_emptyUrl_returnsFalse() throws Exception {
    txtDisplayName().setText( "My Repo" );
    txtUrl().setText( "" );

    assertFalse( validateSaveAllowed() );
  }

  @Test
  public void testValidateSaveAllowed_usernamePassword_valid_returnsTrue() throws Exception {
    txtDisplayName().setText( "My Repo" );
    txtUrl().setText( "http://server/pentaho" );

    assertTrue( validateSaveAllowed() );
  }

  @Test
  public void testValidateSaveAllowed_oauthDisabled_alwaysValid() throws Exception {
    txtDisplayName().setText( "My Repo" );
    txtUrl().setText( "http://server/pentaho" );
    setField( "oAuthEnabled", false );
    setField( "selectedAuthorizationUri", null );

    assertTrue( validateSaveAllowed() );
  }

  @Test
  public void testValidateSaveAllowed_sso_noAuthUri_returnsFalse() throws Exception {
    txtDisplayName().setText( "My Repo" );
    enableOAuth();
    setUrlAndSelectSSO( "http://server/pentaho" );
    setField( "selectedAuthorizationUri", null );

    assertFalse( validateSaveAllowed() );
  }

  @Test
  public void testValidateSaveAllowed_sso_emptyAuthUri_returnsFalse() throws Exception {
    txtDisplayName().setText( "My Repo" );
    enableOAuth();
    setUrlAndSelectSSO( "http://server/pentaho" );
    setField( "selectedAuthorizationUri", "" );

    assertFalse( validateSaveAllowed() );
  }

  @Test
  public void testValidateSaveAllowed_sso_withAuthUri_returnsTrue() throws Exception {
    txtDisplayName().setText( "My Repo" );
    enableOAuth();
    setUrlAndSelectSSO( "http://server/pentaho" );
    setField( "selectedAuthorizationUri", "https://auth.example.com" );

    assertTrue( validateSaveAllowed() );
  }

  // ============================== SSO Controls Visibility Tests ==============================

  @Test
  public void testUpdateSsoControlsVisibility_usernamePasswordSelected_hidesSsoSection() throws Exception {
    enableOAuth();
    radioSSO().setSelection( false );
    radioUsernamePassword().setSelection( true );
    lblSsoStatus().setText( "some status" );

    updateSsoControlsVisibility();

    assertFalse( ssoSection().getVisible() );
    assertEquals( "", lblSsoStatus().getText() );
  }

  // ============================== Load SSO Providers Tests ==============================

  @Test
  public void testLoadSsoProviders_oauthNotEnabled_returnsEarlyWithoutFetch() throws Exception {
    setField( "oAuthEnabled", false );
    radioSSO().setSelection( true );
    radioUsernamePassword().setSelection( false );

    loadSsoProviders( false );

    verify( mockSsoService, never() ).fetchProviders( anyString() );
  }

  @Test
  public void testLoadSsoProviders_ssoNotSelected_returnsEarlyWithoutFetch() throws Exception {
    enableOAuth();
    radioSSO().setSelection( false );
    radioUsernamePassword().setSelection( true );

    loadSsoProviders( false );

    verify( mockSsoService, never() ).fetchProviders( anyString() );
  }

  @Test
  public void testLoadSsoProviders_sameUrlCached_restoresFromCache() throws Exception {
    enableOAuth();
    String url = "http://cached-server/pentaho";
    SsoProviderService.SsoProvider p =
      new SsoProviderService.SsoProvider( "Google", "https://auth.google.com", "google" );

    setField( "ssoProviders", Collections.singletonList( p ) );
    setField( "lastLoadedUrl", url );
    setField( "selectedAuthorizationUri", "https://auth.google.com" );

    radioSSO().setSelection( true );
    radioUsernamePassword().setSelection( false );
    txtUrl().setText( url );
    // Restore fields overwritten by setText's modify listener
    setField( "ssoProviders", Collections.singletonList( p ) );
    setField( "lastLoadedUrl", url );
    setField( "selectedAuthorizationUri", "https://auth.google.com" );
    combo().setItems( "Google" );

    loadSsoProviders( true );

    verify( mockSsoService, never() ).fetchProviders( anyString() );
    assertEquals( 1, combo().getItemCount() );
  }

  @Test
  public void testLoadSsoProviders_freshFetch_success_populatesCombo() throws Exception {
    enableOAuth();
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
    enableOAuth();
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
    enableOAuth();
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
    enableOAuth();
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

  // ============================== URL Focus Lost Tests ==============================

  @Test
  public void testOnUrlFocusLost_withPendingCheck_cancelsAndChecksOAuth() throws Exception {
    String url = "http://server/pentaho";
    when( mockSsoService.isOAuthEnabled( url ) ).thenReturn( true );
    when( mockSsoService.fetchProviders( url ) ).thenReturn( Collections.emptyList() );

    setUrlAndSelectSSO( url );

    Runnable pendingCheck = mock( Runnable.class );
    setField( "pendingOAuthCheck", pendingCheck );

    onUrlFocusLost();

    assertNull( getField( "pendingOAuthCheck" ) );
    waitForAsync( () -> getField( "lastOAuthCheckUrl" ) != null, 3000 );
  }

  @Test
  public void testOnUrlFocusLost_noPendingCheck_checksOAuth() throws Exception {
    String url = "http://server/pentaho";
    when( mockSsoService.isOAuthEnabled( url ) ).thenReturn( false );

    setUrlAndSelectSSO( url );
    setField( "pendingOAuthCheck", null );

    onUrlFocusLost();

    waitForAsync( () -> getField( "lastOAuthCheckUrl" ) != null, 3000 );
    assertEquals( url, getField( "lastOAuthCheckUrl" ) );
  }

  @Test
  public void testOnUrlFocusLost_emptyUrl_disablesOAuth() throws Exception {
    txtUrl().setText( "" );

    onUrlFocusLost();

    assertFalse( (boolean) getField( "oAuthEnabled" ) );
    verify( mockSsoService, never() ).isOAuthEnabled( anyString() );
  }

  // ============================== URL Modify Listener Tests ==============================

  @Test
  public void testUrlModifyListener_clearsLastLoadedUrlAndLastOAuthCheckUrl() throws Exception {
    setField( "lastLoadedUrl", "http://prev" );
    setField( "lastOAuthCheckUrl", "http://prev" );

    txtUrl().setText( "http://changed/pentaho" );

    assertNull( getField( "lastLoadedUrl" ) );
    assertNull( getField( "lastOAuthCheckUrl" ) );
    // Clean up pending
    Runnable pending = (Runnable) getField( "pendingOAuthCheck" );
    if ( pending != null ) {
      display.timerExec( -1, pending );
      setField( "pendingOAuthCheck", null );
    }
  }

  @Test
  public void testUrlModifyListener_emptyUrl_disablesOAuth() throws Exception {
    enableOAuth();
    txtUrl().setText( "http://previous" );
    Runnable pending = (Runnable) getField( "pendingOAuthCheck" );
    if ( pending != null ) {
      display.timerExec( -1, pending );
      setField( "pendingOAuthCheck", null );
    }

    txtUrl().setText( "" );

    assertFalse( (boolean) getField( "oAuthEnabled" ) );
    assertNull( getField( "pendingOAuthCheck" ) );
  }

  @Test
  public void testUrlModifyListener_nonEmptyUrl_schedulesDebounce() throws Exception {
    txtUrl().setText( "http://server/pentaho" );

    assertNotNull( getField( "pendingOAuthCheck" ) );
    // Clean up
    Runnable pending = (Runnable) getField( "pendingOAuthCheck" );
    display.timerExec( -1, pending );
    setField( "pendingOAuthCheck", null );
  }

  @Test
  public void testUrlModifyListener_secondChange_cancelsPreviousPending() throws Exception {
    txtUrl().setText( "http://server1/pentaho" );
    Runnable firstPending = (Runnable) getField( "pendingOAuthCheck" );
    assertNotNull( firstPending );

    txtUrl().setText( "http://server2/pentaho" );
    Runnable secondPending = (Runnable) getField( "pendingOAuthCheck" );
    assertNotNull( secondPending );
    assertNotSame( firstPending, secondPending );

    // Clean up
    display.timerExec( -1, secondPending );
    setField( "pendingOAuthCheck", null );
  }

  @Test
  public void testPendingOAuthCheck_lambda_triggersCheck() throws Exception {
    String url = "http://server/pentaho";
    when( mockSsoService.isOAuthEnabled( url ) ).thenReturn( false );

    txtUrl().setText( url );

    Runnable pending = (Runnable) getField( "pendingOAuthCheck" );
    assertNotNull( pending );

    pending.run();

    assertNull( getField( "pendingOAuthCheck" ) );
    waitForAsync( () -> getField( "lastOAuthCheckUrl" ) != null, 3000 );
  }

  // ============================== ApplyFetchedProviders Tests ==============================

  @Test
  public void testApplyFetchedProviders_ssoDeselected_returnsEarly() throws Exception {
    enableOAuth();
    radioSSO().setSelection( false );
    radioUsernamePassword().setSelection( true );
    txtUrl().setText( "http://server/pentaho" );

    applyFetchedProviders( "http://server/pentaho", Collections.emptyList(), null, false );

    assertEquals( 0, combo().getItemCount() );
  }

  @Test
  public void testApplyFetchedProviders_staleUrl_discards() throws Exception {
    enableOAuth();
    setUrlAndSelectSSO( "http://server2/pentaho" );

    applyFetchedProviders( "http://server1/pentaho", Collections.emptyList(), null, false );

    assertEquals( 0, combo().getItemCount() );
  }

  @Test
  public void testApplyFetchedProviders_exception_comboHasNoText_clearsAndShowsError() throws Exception {
    enableOAuth();
    setUrlAndSelectSSO( "http://server/pentaho" );

    applyFetchedProviders( "http://server/pentaho", Collections.emptyList(),
      new IOException( "Network error" ), false );

    assertEquals( BaseMessages.getString( PKG, "PentahoEnterpriseRepoForm.status.loadError" ),
      lblSsoStatus().getText() );
    assertEquals( 0, combo().getItemCount() );
    assertNull( getField( "selectedAuthorizationUri" ) );
  }

  @Test
  public void testApplyFetchedProviders_exception_comboHasText_keepsItemsAndShowsError() throws Exception {
    enableOAuth();
    setUrlAndSelectSSO( "http://server/pentaho" );
    combo().setItems( "Google" );
    combo().select( 0 );

    applyFetchedProviders( "http://server/pentaho", Collections.emptyList(),
      new IOException( "Network error" ), false );

    assertEquals( 1, combo().getItemCount() );
    assertEquals( BaseMessages.getString( PKG, "PentahoEnterpriseRepoForm.status.loadError" ),
      lblSsoStatus().getText() );
  }

  // ============================== RestoreProviderSelection Tests ==============================

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
    combo().select( 0 );

    restoreProviderSelection( false );

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
    combo().deselectAll();

    restoreProviderSelection( false );

    assertEquals( 0, combo().getSelectionIndex() );
    assertEquals( "https://auth.google.com", getField( "selectedAuthorizationUri" ) );
  }

  // ============================== ApplySelectedProvider Tests ==============================

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
    combo().removeAll();

    applySelectedProvider();

    assertNull( getField( "selectedAuthorizationUri" ) );
    assertNull( getField( "selectedRegistrationId" ) );
  }

  @Test
  public void testApplySelectedProvider_outOfBounds_clearsFields() throws Exception {
    setField( "ssoProviders", Collections.emptyList() );
    setField( "selectedAuthorizationUri", "previously-set" );
    combo().setItems( "something" );
    combo().select( 0 );

    applySelectedProvider();

    assertNull( getField( "selectedAuthorizationUri" ) );
    assertNull( getField( "selectedRegistrationId" ) );
  }

  // ============================== ClearProviderSelection Tests ==============================

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
    assertEquals( 2, combo().getItemCount() );
  }

  // ============================== Status Label Tests ==============================

  @Test
  public void testSetSsoStatusError_setsRedForegroundAndText() throws Exception {
    setSsoStatusError( "An error occurred" );

    assertEquals( "An error occurred", lblSsoStatus().getText() );
    assertEquals( display.getSystemColor( SWT.COLOR_RED ), lblSsoStatus().getForeground() );
  }

  @Test
  public void testSetSsoStatusInfo_emptyText_setsDefaultColor() throws Exception {
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

  // ============================== StringValue Tests ==============================

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

  // ============================== FindProviderIndex Tests ==============================

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

  // ============================== Save Button State Tests ==============================

  @Test
  public void testSaveButtonState_changesWithValidation() throws Exception {
    assertFalse( btnSave.getEnabled() );

    txtDisplayName().setText( "My Repo" );
    txtUrl().setText( "http://server/pentaho" );

    assertTrue( btnSave.getEnabled() );
    // Clean up pending
    Runnable pending = (Runnable) getField( "pendingOAuthCheck" );
    if ( pending != null ) {
      display.timerExec( -1, pending );
      setField( "pendingOAuthCheck", null );
    }
  }

  @Test
  public void testSaveButtonState_ssoWithoutAuthUri_disablesSave() throws Exception {
    txtDisplayName().setText( "My Repo" );
    enableOAuth();
    setUrlAndSelectSSO( "http://server/pentaho" );
    setField( "selectedAuthorizationUri", null );
    setField( "changed", true );

    Method setSaveButtonEnabled = BaseRepoFormComposite.class.getDeclaredMethod( "setSaveButtonEnabled" );
    setSaveButtonEnabled.setAccessible( true );
    setSaveButtonEnabled.invoke( composite );

    assertFalse( btnSave.getEnabled() );
  }

  @Test
  public void testSaveButtonState_ssoWithAuthUri_enablesSave() throws Exception {
    txtDisplayName().setText( "My Repo" );
    enableOAuth();
    setUrlAndSelectSSO( "http://server/pentaho" );
    setField( "selectedAuthorizationUri", "https://auth.example.com" );
    setField( "changed", true );

    Method setSaveButtonEnabled = BaseRepoFormComposite.class.getDeclaredMethod( "setSaveButtonEnabled" );
    setSaveButtonEnabled.setAccessible( true );
    setSaveButtonEnabled.invoke( composite );

    assertTrue( btnSave.getEnabled() );
  }

  // ============================== Executor and Dispose Tests ==============================

  @Test
  public void testProviderLoadFutureIsNullOnFreshComposite() throws Exception {
    assertNull( getProviderLoadFuture() );
  }

  @Test
  public void testProviderLoadExecutorIsNotShutdownOnFreshComposite() throws Exception {
    java.util.concurrent.ExecutorService executor =
      (java.util.concurrent.ExecutorService) getField( "providerLoadExecutor" );
    assertFalse( executor.isShutdown() );
    assertFalse( executor.isTerminated() );
  }

  @Test
  public void loadSsoProvidersAssignsProviderLoadFuture() throws Exception {
    enableOAuth();
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
    enableOAuth();
    String url = "http://server/pentaho";
    when( mockSsoService.fetchProviders( url ) ).thenReturn(
      Collections.singletonList(
        new SsoProviderService.SsoProvider( "Google", "https://auth.google.com", "google" ) ) );

    setUrlAndSelectSSO( url );
    loadSsoProviders( false );

    waitForAsync( () -> combo().getItemCount() > 0, 3000 );

    java.util.concurrent.Future<?> future = getProviderLoadFuture();
    assertNotNull( future );
    assertTrue( future.isDone() );
  }

  @Test
  public void cancelInFlightLoadCancelsRunningFuture() throws Exception {
    enableOAuth();
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

    java.util.concurrent.Future<?> firstFuture = getProviderLoadFuture();
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
  public void disposeShutDownsExecutorAndCancelsInFlightLoad() throws Exception {
    enableOAuth();
    java.util.concurrent.CountDownLatch fetchGate = new java.util.concurrent.CountDownLatch( 1 );
    String url = "http://server/pentaho";
    when( mockSsoService.fetchProviders( url ) ).thenAnswer( invocation -> {
      fetchGate.await( 5, java.util.concurrent.TimeUnit.SECONDS );
      return Collections.emptyList();
    } );

    setUrlAndSelectSSO( url );
    loadSsoProviders( false );

    java.util.concurrent.Future<?> future = getProviderLoadFuture();
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

  // ============================== End-to-End Tests ==============================

  @Test
  public void endToEndLoadPopulatesComboViaExecutorAndAsyncExec() throws Exception {
    enableOAuth();
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
  public void testLoadSsoProviders_preserve_savesAndRestoresSelection() throws Exception {
    enableOAuth();
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

    assertEquals( 1, combo().getSelectionIndex() );
    assertEquals( "https://auth.okta.com", getField( "selectedAuthorizationUri" ) );
    assertEquals( "okta", getField( "selectedRegistrationId" ) );
  }

  @Test
  public void testLoadSsoProviders_showsLoadingIndicatorDuringFetch() throws Exception {
    enableOAuth();
    String url = "http://server/pentaho";
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

    assertEquals( BaseMessages.getString( PKG, "PentahoEnterpriseRepoForm.status.loading" ),
      lblSsoStatus().getText() );
    assertFalse( combo().isEnabled() );

    synchronized ( fetchLatch ) {
      fetchLatch.notifyAll();
    }

    waitForAsync( () -> combo().getItemCount() > 0, 3000 );
    assertTrue( combo().isEnabled() );
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
  public void testValidateSaveAllowed_emptyDisplayName_returnsFalse() throws Exception {
    txtDisplayName().setText( "" );
    txtUrl().setText( "http://server/pentaho" );

    assertFalse( validateSaveAllowed() );
  }

  @Test
  public void secondLoadCancelsFirstInFlightLoad() throws Exception {
    enableOAuth();
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

    java.util.concurrent.Future<?> firstFuture = getProviderLoadFuture();

    setField( "lastLoadedUrl", null );
    setUrlAndSelectSSO( url2 );
    loadSsoProviders( false );

    assertTrue( firstFuture.isCancelled() );
    java.util.concurrent.Future<?> secondFuture = getProviderLoadFuture();
    assertNotSame( firstFuture, secondFuture );

    firstFetchGate.countDown();

    waitForAsync( () -> combo().getItemCount() == 1 && "Fresh".equals( combo().getItem( 0 ) ), 3000 );
    assertEquals( "Fresh", combo().getItem( 0 ) );
  }

  @Test
  public void loadAfterCancelStillWorksCorrectly() throws Exception {
    enableOAuth();
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
}
