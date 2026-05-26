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
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.json.simple.JSONObject;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.BaseRepositoryMeta;
import org.pentaho.di.ui.core.FormDataBuilder;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.repo.util.SsoProviderService;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;

import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.core.logging.LogChannelInterface;

public class PentahoEnterpriseRepoFormComposite extends BaseRepoFormComposite {

  private static final Class<?> PKG = PentahoEnterpriseRepoFormComposite.class;

  private static final LogChannelInterface log =
      KettleLogStore.getLogChannelInterfaceFactory().create( PentahoEnterpriseRepoFormComposite.class );

  private static final String AUTH_METHOD_SSO = "SSO";
  private static final String AUTH_METHOD_USERNAME_PASSWORD = "USERNAME_PASSWORD";
  private static final String SSO_PROVIDER_NAME = "ssoProviderName";
  private static final String SSO_AUTHORIZATION_URI = "ssoAuthorizationUri";
  private static final String SSO_REGISTRATION_ID = "ssoRegistrationId";

  private Text txtUrl;
  private Composite authSection;
  private Composite ssoSection;
  private Button radioUsernamePassword;
  private Button radioSSO;
  private Combo comboSsoProvider;
  private Label lblSsoProvider;
  private Label lblSsoStatus;
  private boolean oAuthEnabled = false;
  private List<SsoProviderService.SsoProvider> ssoProviders = Collections.emptyList();
  private final SsoProviderService ssoProviderService = new SsoProviderService();
  private String selectedAuthorizationUri;
  private String selectedRegistrationId;
  private String lastLoadedUrl;
  private Runnable pendingOAuthCheck = null;
  private final ExecutorService providerLoadExecutor =
    Executors.newSingleThreadExecutor( r -> {
      Thread t = new Thread( r, "SSO-Provider-Loader" );
      t.setDaemon( true );
      return t;
    } );
  private final AtomicReference<Future<?>> providerLoadFuture = new AtomicReference<>();


  public PentahoEnterpriseRepoFormComposite( Composite parent, int style ) {
    super( parent, style );
  }

  @Override
  protected Control uiAfterDisplayName() {
    this.props = PropsUI.getInstance();

    Label lUrl = new Label( this, SWT.NONE );
    lUrl.setText( BaseMessages.getString( PKG, "PentahoEnterpriseRepoForm.label.url" ) );
    lUrl.setLayoutData(
      new FormDataBuilder().left( 0, 0 ).right( 100, 0 ).top( txtDisplayName, CONTROL_MARGIN ).result() );
    props.setLook( lUrl );

    txtUrl = new Text( this, SWT.BORDER );
    txtUrl.setLayoutData(
      new FormDataBuilder().left( 0, 0 ).top( lUrl, LABEL_CONTROL_MARGIN ).width( MEDIUM_WIDTH ).result() );
    txtUrl.addModifyListener( lsMod );
    txtUrl.addModifyListener( event -> {
      lastLoadedUrl = null;
      // Cancel any previously scheduled debounced check
      if ( pendingOAuthCheck != null ) {
        getDisplay().timerExec( -1, pendingOAuthCheck );
        pendingOAuthCheck = null;
      }
      if ( Utils.isEmpty( txtUrl.getText() ) ) {
        setOAuthEnabled( false );
        setSaveButtonEnabled();
        return;
      }

      pendingOAuthCheck = () -> {
        if ( !isDisposed() ) {
          checkOAuthAndLoadProviders( false );
        }
        pendingOAuthCheck = null;
      };
      getDisplay().timerExec( 2000, pendingOAuthCheck );
    } );
    txtUrl.addFocusListener( new FocusAdapter() {
      @Override
      public void focusLost( FocusEvent e ) {
        onUrlFocusLost();
      }
    } );
    props.setLook( txtUrl );

    // Auth section composite - visible only when OAuth is enabled on the server
    authSection = new Composite( this, SWT.NONE );
    authSection.setLayout( new FormLayout() );
    authSection.setLayoutData(
      new FormDataBuilder().left( 0, 0 ).right( 100, 0 ).top( txtUrl, CONTROL_MARGIN ).result() );
    props.setLook( authSection );

    // Authentication method radio buttons
    Label lAuthMethod = new Label( authSection, SWT.NONE );
    lAuthMethod.setText( BaseMessages.getString( PKG, "PentahoEnterpriseRepoForm.label.authMethod" ) );
    lAuthMethod.setLayoutData(
      new FormDataBuilder().left( 0, 0 ).right( 100, 0 ).top( 0, 0 ).result() );
    props.setLook( lAuthMethod );

    radioUsernamePassword = new Button( authSection, SWT.RADIO );
    radioUsernamePassword.setText( BaseMessages.getString( PKG, "PentahoEnterpriseRepoForm.radio.usernamePassword" ) );
    radioUsernamePassword.setLayoutData(
      new FormDataBuilder().left( 0, 0 ).top( lAuthMethod, LABEL_CONTROL_MARGIN ).result() );
    radioUsernamePassword.setSelection( true );
    radioUsernamePassword.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent e ) {
        updateSsoControlsVisibility();
        lsMod.modifyText( null );
      }
    } );
    props.setLook( radioUsernamePassword );

    radioSSO = new Button( authSection, SWT.RADIO );
    radioSSO.setText( BaseMessages.getString( PKG, "PentahoEnterpriseRepoForm.radio.sso" ) );
    radioSSO.setLayoutData(
      new FormDataBuilder().left( 0, 0 ).top( radioUsernamePassword, LABEL_CONTROL_MARGIN ).result() );
    radioSSO.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent e ) {
        updateSsoControlsVisibility();
        if ( radioSSO.getSelection() ) {
          loadSsoProviders( false );
        }
        lsMod.modifyText( null );
      }
    } );
    props.setLook( radioSSO );

    // SSO section composite - visible only when SSO radio is selected
    ssoSection = new Composite( authSection, SWT.NONE );
    ssoSection.setLayout( new FormLayout() );
    ssoSection.setLayoutData(
      new FormDataBuilder().left( 0, 0 ).right( 100, 0 ).top( radioSSO, CONTROL_MARGIN ).result() );
    props.setLook( ssoSection );

    lblSsoProvider = new Label( ssoSection, SWT.NONE );
    lblSsoProvider.setText( BaseMessages.getString( PKG, "PentahoEnterpriseRepoForm.label.identityProvider" ) );
    lblSsoProvider.setLayoutData(
      new FormDataBuilder().left( 0, 0 ).right( 100, 0 ).top( 0, 0 ).result() );
    props.setLook( lblSsoProvider );

    comboSsoProvider = new Combo( ssoSection, SWT.DROP_DOWN | SWT.READ_ONLY );
    comboSsoProvider.setLayoutData(
      new FormDataBuilder().left( 0, 0 ).top( lblSsoProvider, LABEL_CONTROL_MARGIN ).width( MEDIUM_WIDTH ).result() );
    comboSsoProvider.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent e ) {
        applySelectedProvider();
        lsMod.modifyText( null );
      }
    } );
    props.setLook( comboSsoProvider );

    lblSsoStatus = new Label( ssoSection, SWT.WRAP );
    lblSsoStatus.setLayoutData(
      new FormDataBuilder().left( 0, 0 ).right( 100, 0 ).top( comboSsoProvider, LABEL_CONTROL_MARGIN ).result() );
    props.setLook( lblSsoStatus );

    // Initially hide both sections
    setAuthSectionVisible( false );
    setSsoSectionVisible( false );

    return authSection;
  }

  @Override
  public Map<String, Object> toMap() {
    Map<String, Object> ret = super.toMap();

    ret.put( BaseRepositoryMeta.ID, "PentahoEnterpriseRepository" );

    ret.put( "url", txtUrl.getText() );
    ret.put( "authMethod", ( oAuthEnabled && radioSSO.getSelection() ) ? AUTH_METHOD_SSO : AUTH_METHOD_USERNAME_PASSWORD );
    String providerName = comboSsoProvider.getSelectionIndex() >= 0 ? comboSsoProvider.getText() : null;
    ret.put( SSO_PROVIDER_NAME, providerName );
    String authUri = ( oAuthEnabled && radioSSO.getSelection() ) ? selectedAuthorizationUri : null;
    ret.put( SSO_AUTHORIZATION_URI, authUri );
    String regId = ( oAuthEnabled && radioSSO.getSelection() ) ? selectedRegistrationId : null;
    ret.put( SSO_REGISTRATION_ID, regId );
    
    if ( oAuthEnabled && radioSSO.getSelection() ) {
      log.logDebug( "toMap() - SSO is selected, saving provider: " + providerName );
      log.logDebug( "toMap() - Saving authorizationUri: " + authUri );
      log.logDebug( "toMap() - Saving registrationId: " + regId );
    }

    return ret;
  }


  @SuppressWarnings( "unchecked" )
  @Override
  public void populate( JSONObject source ) {
    super.populate( source );
    txtUrl.setText( (String) source.getOrDefault( "url", "http://localhost:8080/pentaho" ) );
    props.setLook( txtUrl );
    
    // Set authentication method
    String authMethod = (String) source.getOrDefault( "authMethod", AUTH_METHOD_USERNAME_PASSWORD );
    if ( AUTH_METHOD_SSO.equals( authMethod ) ) {
      radioSSO.setSelection( true );
      radioUsernamePassword.setSelection( false );
    } else {
      radioUsernamePassword.setSelection( true );
      radioSSO.setSelection( false );
    }

    selectedAuthorizationUri = stringValue( source.get( SSO_AUTHORIZATION_URI ) );
    selectedRegistrationId = stringValue( source.get( SSO_REGISTRATION_ID ) );
    String providerName = stringValue( source.get( SSO_PROVIDER_NAME ) );
    
    log.logDebug( "populate() - Loaded authorizationUri: " + selectedAuthorizationUri );
    log.logDebug( "populate() - Loaded registrationId: " + selectedRegistrationId );
    log.logDebug( "populate() - Loaded providerName: " + providerName );
    
    if ( !Utils.isEmpty( providerName ) ) {
      comboSsoProvider.setItems( providerName );
      comboSsoProvider.select( 0 );
    }

    // Check OAuth status for the URL (will show/hide auth section accordingly)
    checkOAuthAndLoadProviders( true );
  }

  @Override
  protected boolean validateSaveAllowed() {
    return super.validateSaveAllowed()
      && !Utils.isEmpty( txtUrl.getText() )
      && ( !oAuthEnabled || !radioSSO.getSelection() || !Utils.isEmpty( selectedAuthorizationUri ) );
  }

  private void onUrlFocusLost() {
    if ( pendingOAuthCheck != null ) {
      getDisplay().timerExec( -1, pendingOAuthCheck );
      pendingOAuthCheck = null;
    }
    checkOAuthAndLoadProviders( false );
  }

  private void setOAuthEnabled( boolean enabled ) {
    this.oAuthEnabled = enabled;
    setAuthSectionVisible( enabled );
    if ( !enabled ) {
      radioUsernamePassword.setSelection( true );
      radioSSO.setSelection( false );
      clearProviderSelection( true );
      lblSsoStatus.setText( "" );
      setSsoSectionVisible( false );
    } else {
      updateSsoControlsVisibility();
    }
    layout( true, true );
  }

  private void setAuthSectionVisible( boolean visible ) {
    authSection.setVisible( visible );
    FormData fd = (FormData) authSection.getLayoutData();
    if ( visible ) {
      fd.height = SWT.DEFAULT;
    } else {
      fd.height = 0;
    }
  }

  private void setSsoSectionVisible( boolean visible ) {
    ssoSection.setVisible( visible );
    FormData fd = (FormData) ssoSection.getLayoutData();
    if ( visible ) {
      fd.height = SWT.DEFAULT;
    } else {
      fd.height = 0;
    }
    authSection.layout( true, true );
  }

  private void updateSsoControlsVisibility() {
    boolean ssoSelected = radioSSO.getSelection();
    setSsoSectionVisible( ssoSelected );
    if ( !ssoSelected ) {
      lblSsoStatus.setText( "" );
    }
    layout( true, true );
  }

  private void checkOAuthAndLoadProviders( boolean preserveCurrentSelection ) {
    String serverUrl = txtUrl.getText();
    if ( Utils.isEmpty( serverUrl ) ) {
      setOAuthEnabled( false );
      setSaveButtonEnabled();
      return;
    }

    // Show loading state
    cancelInFlightLoad();
    final String savedAuthorizationUri = selectedAuthorizationUri;
    final String savedRegistrationId = selectedRegistrationId;
    final String urlToCheck = serverUrl;
    final boolean ssoSelected = radioSSO.getSelection();

    providerLoadFuture.set( providerLoadExecutor.submit(
      () -> fetchOAuthStatusAndProviders( urlToCheck, preserveCurrentSelection,
        savedAuthorizationUri, savedRegistrationId, ssoSelected ) ) );
  }

  private void fetchOAuthStatusAndProviders( String urlToCheck, boolean preserveCurrentSelection,
                                             String savedAuthorizationUri, String savedRegistrationId,
                                             boolean ssoSelected ) {
    boolean isOAuth;
    try {
      isOAuth = ssoProviderService.isOAuthEnabled( urlToCheck );
    } catch ( Exception e ) {
      log.logDebug( "checkOAuthEnabled() - error checking OAuth status for " + urlToCheck + ": " + e.getMessage() );
      isOAuth = false;
    }

    final boolean finalIsOAuth = isOAuth;
    List<SsoProviderService.SsoProvider> fetchedProviders = Collections.emptyList();
    Exception fetchException = null;

    // Only fetch providers when SSO is actually needed: either the user already has SSO selected
    // or we are restoring a previously saved SSO selection (preserveCurrentSelection).
    if ( isOAuth && ( ssoSelected || preserveCurrentSelection ) ) {
      try {
        fetchedProviders = ssoProviderService.fetchProviders( urlToCheck );
      } catch ( Exception e ) {
        fetchException = e;
      }
    }

    final List<SsoProviderService.SsoProvider> finalProviders = fetchedProviders;
    final Exception finalException = fetchException;

    getDisplay().asyncExec( () -> applyOAuthStatusAndProviders( urlToCheck, finalIsOAuth, finalProviders,
      finalException, preserveCurrentSelection, savedAuthorizationUri, savedRegistrationId ) );
  }

  private void applyOAuthStatusAndProviders( String urlToCheck, boolean isOAuth,
                                             List<SsoProviderService.SsoProvider> finalProviders,
                                             Exception finalException, boolean preserveCurrentSelection,
                                             String savedAuthorizationUri, String savedRegistrationId ) {
    if ( isDisposed() ) {
      return;
    }

    // Discard stale results if the URL changed while loading
    if ( !urlToCheck.equals( txtUrl.getText() ) ) {
      return;
    }

    setOAuthEnabled( isOAuth );

    if ( !isOAuth ) {
      setSaveButtonEnabled();
      return;
    }

    // Restore the saved selection state so that restoreProviderSelection can find the right provider
    if ( preserveCurrentSelection ) {
      selectedAuthorizationUri = savedAuthorizationUri;
      selectedRegistrationId = savedRegistrationId;
    }

    if ( !radioSSO.getSelection() ) {
      setSaveButtonEnabled();
      return;
    }

    // Apply provider results
    comboSsoProvider.setEnabled( true );

    if ( finalException != null ) {
      log.logDebug( "loadSsoProviders() - error fetching from " + urlToCheck + ": " + finalException.getMessage() );
      if ( Utils.isEmpty( comboSsoProvider.getText() ) ) {
        clearProviderSelection( true );
      }
      setSsoStatusError( BaseMessages.getString( PKG, "PentahoEnterpriseRepoForm.status.loadError" ) );
    } else {
      ssoProviders = finalProviders;
      lastLoadedUrl = urlToCheck;
      log.logDebug( "loadSsoProviders() - fetched " + ssoProviders.size() + " providers from " + urlToCheck );

      comboSsoProvider.removeAll();
      for ( SsoProviderService.SsoProvider provider : ssoProviders ) {
        log.logDebug( "loadSsoProviders() - Adding provider: " + provider.clientName()
          + " with authUri: " + provider.authorizationUri() );
        comboSsoProvider.add( provider.clientName() );
      }

      if ( ssoProviders.isEmpty() ) {
        clearProviderSelection( true );
        setSsoStatusInfo( BaseMessages.getString( PKG, "PentahoEnterpriseRepoForm.status.noProviders" ) );
      } else {
        restoreProviderSelection( preserveCurrentSelection );
        setSsoStatusInfo( "" );
      }
    }

    layout( true, true );
    setSaveButtonEnabled();
  }

  private void loadSsoProviders( boolean preserveCurrentSelection ) {
    if ( !oAuthEnabled || !radioSSO.getSelection() ) {
      return;
    }

    String serverUrl = txtUrl.getText();
    if ( Utils.isEmpty( serverUrl ) ) {
      clearProviderSelection( true );
      setSsoStatusInfo( BaseMessages.getString( PKG, "PentahoEnterpriseRepoForm.status.enterUrl" ) );
      layout( true, true );
      setSaveButtonEnabled();
      return;
    }

    if ( serverUrl.equals( lastLoadedUrl ) && !ssoProviders.isEmpty() ) {
      restoreProviderSelection( preserveCurrentSelection );
      return;
    }

    // Preserve the current selection state before clearing
    final String savedAuthorizationUri = selectedAuthorizationUri;
    final String savedRegistrationId = selectedRegistrationId;

    clearProviderSelection( true );
    comboSsoProvider.setEnabled( false );
    setSsoStatusInfo( BaseMessages.getString( PKG, "PentahoEnterpriseRepoForm.status.loading" ) );
    layout( true, true );
    setSaveButtonEnabled();

    final String urlToLoad = serverUrl;
    cancelInFlightLoad();
    providerLoadFuture.set( providerLoadExecutor.submit(
      () -> fetchAndApplyProviders( urlToLoad, preserveCurrentSelection, savedAuthorizationUri,
        savedRegistrationId ) ) );
  }

  private void fetchAndApplyProviders( String urlToLoad, boolean preserveCurrentSelection,
                                       String savedAuthorizationUri, String savedRegistrationId ) {
    List<SsoProviderService.SsoProvider> fetchedProviders;
    Exception fetchException = null;
    try {
      fetchedProviders = ssoProviderService.fetchProviders( urlToLoad );
    } catch ( Exception e ) {
      fetchedProviders = Collections.emptyList();
      fetchException = e;
    }

    final List<SsoProviderService.SsoProvider> finalProviders = fetchedProviders;
    final Exception finalException = fetchException;

    getDisplay().asyncExec( () -> applyFetchedProviders( urlToLoad, finalProviders, finalException,
      preserveCurrentSelection, savedAuthorizationUri, savedRegistrationId ) );
  }

  private void applyFetchedProviders( String urlToLoad, List<SsoProviderService.SsoProvider> finalProviders,
                                      Exception finalException, boolean preserveCurrentSelection,
                                      String savedAuthorizationUri, String savedRegistrationId ) {
    if ( isDisposed() || !radioSSO.getSelection() ) {
      return;
    }

    // Discard stale results if the URL changed while loading
    if ( !urlToLoad.equals( txtUrl.getText() ) ) {
      return;
    }

    // Restore the saved selection state so that restoreProviderSelection can find the right provider
    if ( preserveCurrentSelection ) {
      selectedAuthorizationUri = savedAuthorizationUri;
      selectedRegistrationId = savedRegistrationId;
    }

    comboSsoProvider.setEnabled( true );

    if ( finalException != null ) {
      log.logDebug( "loadSsoProviders() - error fetching from " + urlToLoad + ": " + finalException.getMessage() );
      if ( Utils.isEmpty( comboSsoProvider.getText() ) ) {
        clearProviderSelection( true );
      }
      setSsoStatusError( BaseMessages.getString( PKG, "PentahoEnterpriseRepoForm.status.loadError" ) );
    } else {
      ssoProviders = finalProviders;
      lastLoadedUrl = urlToLoad;
      log.logDebug( "loadSsoProviders() - fetched " + ssoProviders.size() + " providers from " + urlToLoad );

      comboSsoProvider.removeAll();
      for ( SsoProviderService.SsoProvider provider : ssoProviders ) {
        log.logDebug( "loadSsoProviders() - Adding provider: " + provider.clientName()
          + " with authUri: " + provider.authorizationUri() );
        comboSsoProvider.add( provider.clientName() );
      }

      if ( ssoProviders.isEmpty() ) {
        clearProviderSelection( true );
        setSsoStatusInfo( BaseMessages.getString( PKG, "PentahoEnterpriseRepoForm.status.noProviders" ) );
      } else {
        restoreProviderSelection( preserveCurrentSelection );
        setSsoStatusInfo( "" );
      }
    }

    layout( true, true );
    setSaveButtonEnabled();
  }

  private void restoreProviderSelection( boolean preserveCurrentSelection ) {
    if ( comboSsoProvider.getItemCount() == 0 ) {
      clearProviderSelection( true );
      return;
    }

    int selectedIndex = -1;
    if ( preserveCurrentSelection && !Utils.isEmpty( selectedAuthorizationUri ) ) {
      selectedIndex = findProviderIndexByAuthorizationUri( selectedAuthorizationUri );
    }

    if ( selectedIndex < 0 && !Utils.isEmpty( comboSsoProvider.getText() ) ) {
      selectedIndex = comboSsoProvider.indexOf( comboSsoProvider.getText() );
    }

    if ( selectedIndex < 0 ) {
      selectedIndex = 0;
    }

    comboSsoProvider.select( selectedIndex );
    applySelectedProvider();
  }

  private int findProviderIndexByAuthorizationUri( String authorizationUri ) {
    for ( int index = 0; index < ssoProviders.size(); index++ ) {
      if ( authorizationUri.equals( ssoProviders.get( index ).authorizationUri() ) ) {
        return index;
      }
    }
    return -1;
  }

  private void applySelectedProvider() {
    int selectedIndex = comboSsoProvider.getSelectionIndex();
    log.logDebug( "applySelectedProvider() - selectedIndex: " + selectedIndex );
    
    if ( selectedIndex < 0 || selectedIndex >= ssoProviders.size() ) {
      log.logDebug( "applySelectedProvider() - invalid index, clearing selection" );
      clearProviderSelection( false );
      return;
    }

    SsoProviderService.SsoProvider provider = ssoProviders.get( selectedIndex );
    selectedAuthorizationUri = provider.authorizationUri();
    selectedRegistrationId = provider.registrationId();
    log.logDebug( "applySelectedProvider() - set authorizationUri: " + selectedAuthorizationUri );
    log.logDebug( "applySelectedProvider() - set registrationId: " + selectedRegistrationId );
  }

  private void clearProviderSelection( boolean clearCombo ) {
    selectedAuthorizationUri = null;
    selectedRegistrationId = null;
    if ( clearCombo ) {
      comboSsoProvider.removeAll();
    }
  }

  private void setSsoStatusError( String message ) {
    lblSsoStatus.setForeground( Display.getCurrent().getSystemColor( SWT.COLOR_RED ) );
    lblSsoStatus.setText( message );
  }

  private void setSsoStatusInfo( String message ) {
    Color defaultColor = Display.getCurrent().getSystemColor( SWT.COLOR_WIDGET_FOREGROUND );
    lblSsoStatus.setForeground( defaultColor );
    lblSsoStatus.setText( message );
  }

  private String stringValue( Object value ) {
    return value == null ? null : value.toString();
  }

  /**
   * Cancels any in-flight provider load so that stale results are never applied.
   * Uses {@link AtomicReference#getAndSet} to atomically read and clear the reference,
   * preventing race conditions between concurrent callers.
   */
  private void cancelInFlightLoad() {
    Future<?> f = providerLoadFuture.getAndSet( null );
    if ( f != null && !f.isDone() ) {
      f.cancel( true );
    }
  }

  @Override
  public void dispose() {
    cancelInFlightLoad();
    providerLoadExecutor.shutdownNow();
    super.dispose();
  }

}
