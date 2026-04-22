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

package org.pentaho.amazon.s3;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

import org.pentaho.amazon.s3.provider.S3Provider;
import org.pentaho.di.connections.vfs.VFSDetailsComposite;
import org.pentaho.di.core.bowl.Bowl;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.StringUtil;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.vfs.connections.ui.dialog.VFSDetailsCompositeHelper;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.ui.core.FormDataBuilder;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.widget.CheckBoxVar;
import org.pentaho.di.ui.core.widget.ComboVar;
import org.pentaho.di.ui.core.widget.FileChooserVar;
import org.pentaho.di.ui.core.widget.PasswordVisibleTextVar;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.spoon.Spoon;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.amazonaws.regions.Regions;

public class S3DetailComposite implements VFSDetailsComposite {
  private static final Class<?> PKG = S3DetailComposite.class;
  private final Composite wComposite; //The content for the parentComposite
  private final S3Details details;
  private final VFSDetailsCompositeHelper helper;
  private final PropsUI props;
  private final Bowl bowl;

  // what has been saved is the ordinal, keep that in mind if changing this
  enum S3ConnectionType {
    AMAZON,
    MINIO
  }

  enum AuthType {
    KEYS,
    CREDENTIALS_FILE;

    public String toDetailsValue() {
      return switch ( this ) {
        case KEYS -> S3Provider.ACCESS_KEY_SECRET_KEY;
        case CREDENTIALS_FILE -> S3Provider.CREDENTIALS_FILE;
      };
    }

    public static Optional<AuthType> fromDetailsValue( String val ) {
      if ( val == null ) {
        return Optional.empty();
      }
      return switch ( val ) {
        case S3Provider.ACCESS_KEY_SECRET_KEY -> Optional.of( KEYS );
        case S3Provider.CREDENTIALS_FILE -> Optional.of( CREDENTIALS_FILE );
        default -> Optional.empty();
      };
    }
  }

  private static final int TEXT_VAR_FLAGS = SWT.SINGLE | SWT.LEFT | SWT.BORDER;
  private final String[] regionChoices;
  private boolean initializingUiForFirstTime = true;
  private CCombo wS3ConnectionType;
  private CCombo wAuthType;

  private Composite wBottomHalf;
  private Composite wWidgetHolder;

  private ComboVar wRegion;
  private TextVar wMinioRegion;
  private PasswordVisibleTextVar wAccessKey;
  private PasswordVisibleTextVar wSecretKey;
  private PasswordVisibleTextVar wSessionToken;
  private CheckBoxVar wDefaultS3Config;
  private TextVar wProfileName;
  private FileChooserVar wCredentialsFilePath;
  private TextVar wEndpoint;
  private TextVar wSignatureVersion;
  private CheckBoxVar wPathStyleAccess;

  private VariableSpace variableSpace;
  private HashSet<Control> skipControls = new HashSet<>();

  private Group gSslTrustStore;
  private FileChooserVar wTrustStoreFilePath;
  private PasswordVisibleTextVar wTrustStorePassword;
  private CheckBoxVar wTrustAll;

  private Group gKeyStore;
  private FileChooserVar wKeyStoreFilePath;
  private PasswordVisibleTextVar wKeyStorePassword;

  public S3DetailComposite( Bowl bowl, Composite composite, S3Details details, PropsUI props ) {
    helper = new VFSDetailsCompositeHelper( PKG, props );
    this.bowl = bowl;
    this.props = props;
    this.wComposite = composite;
    this.details = details;
    regionChoices = details.getRegions().toArray( new String[ 0 ] );
    variableSpace = Spoon.getInstance().getADefaultVariableSpace();
  }

  public Object open() {
    FormLayout genLayout = new FormLayout();
    genLayout.marginWidth = Const.FORM_MARGIN;
    genLayout.marginHeight = Const.FORM_MARGIN;
    wComposite.setLayout( genLayout );

    //Title
    Label wlTitle = helper.createTitleLabel( wComposite, "ConnectionDialog.s3.Details.Title", skipControls );

    // S3 Connection Type
    Label wlConnectionType = createLabel( "ConnectionDialog.s3.ConnectionType.Label", wlTitle, wComposite );
    wS3ConnectionType = createCCombo( wlConnectionType, 200, wComposite );
    wS3ConnectionType.setItems( getConnectionTypeChoices() );
    wS3ConnectionType.select( Integer.parseInt( Const.NVL( details.getConnectionType(), "0" ) ) );


    // This composite will fill dynamically based on connection type and auth type
    wBottomHalf = new Composite( wComposite, SWT.NONE );
    FormLayout bottomHalfLayout = new FormLayout();
    wBottomHalf.setLayout( bottomHalfLayout );

    FormData wfdBottomHalf = new FormData();
    wfdBottomHalf.top = new FormAttachment( wS3ConnectionType, 0 );
    wfdBottomHalf.left = new FormAttachment( 0, 0 );
    wfdBottomHalf.right = new FormAttachment( 100, 0 );
    wBottomHalf.setLayoutData( wfdBottomHalf );
    props.setLook( wBottomHalf );

    //This composite holds controls that are not currently in use.  No layout needed here
    wWidgetHolder = new Composite( wComposite, SWT.NONE );
    wWidgetHolder.setVisible( false );

    wAuthType = createStandbyCombo();
    wAuthType.setItems( getAuthTypeChoices() );
    wAuthType.select( AuthType.fromDetailsValue( details.getAuthType() ).orElse( AuthType.KEYS ).ordinal() );
    wRegion = createStandbyComboVar();
    wRegion.setItems( regionChoices );
    wRegion.select( 0 );
    wAccessKey = createStandbyPasswordVisibleTextVar();
    wSecretKey = createStandbyPasswordVisibleTextVar();
    wSessionToken = createStandbyPasswordVisibleTextVar();
    wDefaultS3Config = createStandByCheckBoxVar();
    wProfileName = createStandByTextVar();
    wCredentialsFilePath = new FileChooserVar( bowl, variableSpace, wWidgetHolder, TEXT_VAR_FLAGS, "Browse File" );
    wEndpoint = createStandByTextVar();
    wSignatureVersion = createStandByTextVar();

    createTrustStoreGroup( wWidgetHolder );

    createKeyStoreGroup( wWidgetHolder );

    wPathStyleAccess = createStandByCheckBoxVar();
    wMinioRegion = createStandByTextVar();

    skipControls.add( wBottomHalf );
    skipControls.add( wWidgetHolder );

    setupBottomHalf();

    // Set the data
    populateWidgets();

    //add Listeners
    for ( CCombo cCombo : Arrays.asList( wS3ConnectionType, wAuthType ) ) {
      cCombo.addSelectionListener( new SelectionAdapter() {
        @Override public void widgetSelected( SelectionEvent selectionEvent ) {
          setupBottomHalf();
        }
      } );
    }
    wRegion.addSelectionListener( new SelectionAdapter() {
      @Override public void widgetSelected( SelectionEvent selectionEvent ) {
        details.setRegion( wRegion.getText() );
      }
    } );
    wRegion.addModifyListener( modifyEvent -> details.setRegion( wRegion.getText() ) );
    wMinioRegion.addModifyListener( e -> {
      String minioRegion = wMinioRegion.getText();
      details.setRegion( StringUtils.isBlank( minioRegion ) ? null : minioRegion );
    } );
    wAccessKey.addModifyListener( modifyEvent -> details.setAccessKey( wAccessKey.getText() ) );
    wSecretKey.addModifyListener( modifyEvent -> details.setSecretKey( wSecretKey.getText() ) );
    wSessionToken.addModifyListener(
      modifyEvent -> details.setSessionToken( wSessionToken.getText() ) );
    wDefaultS3Config.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent e ) {
        details.setDefaultS3Config( Boolean.toString( wDefaultS3Config.getSelection() ) );
      }
    } );
    wDefaultS3Config.getTextVar().addModifyListener(
      modifyEvent -> details.setDefaultS3ConfigVariable( wDefaultS3Config.getVariableName() ) );
    wCredentialsFilePath.addModifyListener(
      modifyEvent -> details.setCredentialsFilePath( wCredentialsFilePath.getText() ) );
    wProfileName.addModifyListener( modifyEvent -> details.setProfileName( wProfileName.getText() ) );
    wEndpoint.addModifyListener( modifyEvent -> details.setEndpoint( wEndpoint.getText() ) );
    wSignatureVersion.addModifyListener( modifyEvent -> details.setSignatureVersion( wSignatureVersion.getText() ) );
    wPathStyleAccess.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent e ) {
        details.setPathStyleAccess( Boolean.toString( wPathStyleAccess.getSelection() ) );
      }
    } );
    wPathStyleAccess.getTextVar().addModifyListener(
      modifyEvent -> details.setPathStyleAccessVariable( wPathStyleAccess.getVariableName() ) );

    setTrustStoreListeners();
    setKeyStoreListeners();

    VFSDetailsCompositeHelper.setupCompositeResizeListener( wComposite );
    initializingUiForFirstTime = false;
    return wComposite;
  }

  private void setTrustStoreListeners() {
    wTrustStoreFilePath.addModifyListener(
      modifyEvent -> details.setTrustStoreFilePath( wTrustStoreFilePath.getText() ) );
    wTrustStorePassword.addModifyListener(
      modifyEvent -> details.setTrustStorePassword( wTrustStorePassword.getText() ) );
    wTrustAll.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent e ) {
        details.setTrustAll( Boolean.toString( wTrustAll.getSelection() ) );
        onTrustAllChanged();
      }

    } );
  }

  private void setKeyStoreListeners() {
    wKeyStoreFilePath.addModifyListener(
      modifyEvent -> details.setKeyStoreFilePath( wKeyStoreFilePath.getText() ) );
    wKeyStorePassword.addModifyListener(
      modifyEvent -> details.setKeyStorePassword( wKeyStorePassword.getText() ) );
  }

  private void onTrustAllChanged() {
    // checked is mutually exclusive with file+pass
    wTrustStoreFilePath.setEnabled( !wTrustAll.getSelection() );
    wTrustStorePassword.setEnabled( !wTrustAll.getSelection() );
  }

  private void createTrustStoreGroup( Composite parent ) {
    gSslTrustStore = new Group( parent, TEXT_VAR_FLAGS );
    gSslTrustStore.setLayout( new GridLayout( 1, false ) );
    gSslTrustStore.setText( BaseMessages.getString( PKG, "ConnectionDialog.s3.TrustStoreGroup.Label" ) );

    Label lblFilePath = new Label( gSslTrustStore, SWT.LEFT );
    lblFilePath.setText( BaseMessages.getString( PKG, "ConnectionDialog.s3.TrustStoreFile.Label" ) );

    wTrustStoreFilePath = new FileChooserVar( bowl, variableSpace, gSslTrustStore, TEXT_VAR_FLAGS, BaseMessages.getString( "System.Button.Browse" ) );
    wTrustStoreFilePath.setLayoutData( new GridData( GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL ) );

    Label lblFilePass = new Label( gSslTrustStore, SWT.LEFT );
    lblFilePass.setText( BaseMessages.getString( PKG, "ConnectionDialog.s3.TrustStorePassword.Label" ) );

    wTrustStorePassword = createStandbyPasswordVisibleTextVar( gSslTrustStore );
    wTrustStorePassword.setLayoutData( new GridData( GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL ) );

    wTrustAll = new CheckBoxVar( variableSpace, gSslTrustStore, SWT.CHECK );
    wTrustAll.setText( BaseMessages.getString( PKG, "ConnectionDialog.s3.TrustAll.Label" ) );
    wTrustAll.setLayoutData( new GridData( GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL ) );
  }

  private void createKeyStoreGroup( Composite parent ) {
    gKeyStore = new Group( parent, TEXT_VAR_FLAGS );
    gKeyStore.setLayout( new GridLayout( 1, false ) );
    gKeyStore.setText( BaseMessages.getString( PKG, "ConnectionDialog.s3.KeyStoreGroup.Label" ) );

    Label lblFilePath = new Label( gKeyStore, SWT.LEFT );
    lblFilePath.setText( BaseMessages.getString( PKG, "ConnectionDialog.s3.KeyStoreFile.Label" ) );

    wKeyStoreFilePath = new FileChooserVar( bowl, variableSpace, gKeyStore, TEXT_VAR_FLAGS, BaseMessages.getString( "System.Button.Browse" ) );
    wKeyStoreFilePath.setLayoutData( new GridData( GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL ) );

    Label lblFilePass = new Label( gKeyStore, SWT.LEFT );
    lblFilePass.setText( BaseMessages.getString( PKG, "ConnectionDialog.s3.KeyStorePassword.Label" ) );

    wKeyStorePassword = createStandbyPasswordVisibleTextVar( gKeyStore );
    wKeyStorePassword.setLayoutData( new GridData( GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL ) );
  }

  private String getAuthTypeLabel( AuthType authType ) {
    //TODO: i18n
    return switch( authType ) {
      case KEYS -> "Access Key/Secret Key";
      case CREDENTIALS_FILE -> "Credentials File";
    };
  }

  private String getS3ConnectionTypeLabel( S3ConnectionType authType ) {
    return switch( authType ) {
      case AMAZON -> "Amazon";
      case MINIO -> "Minio/HCP";
    };
  }

  private S3ConnectionType getConnectionType() {
    return computeComboValue( wS3ConnectionType, S3ConnectionType.class ).orElse( S3ConnectionType.AMAZON );
  }

  private void setupBottomHalf() {
    var s3ConnectionType = getConnectionType();
    var authType = computeComboValue( wAuthType, AuthType.class ).orElse( AuthType.KEYS );

    if ( initializingUiForFirstTime || s3ConnectionType.ordinal() != stringToInteger( details.getConnectionType() ) ) {
      details.setConnectionType( String.valueOf( s3ConnectionType.ordinal() ) );
    }

    if ( initializingUiForFirstTime || !authType.toDetailsValue().equals( details.getAuthType() ) ) {
      details.setAuthType( authType.toDetailsValue() );
    }

    if ( !initializingUiForFirstTime ) {
      syncRegionFromWidget( s3ConnectionType );
    }

    moveWidgetsToBottom( s3ConnectionType, authType );

    wBottomHalf.layout();
    wComposite.pack();
    VFSDetailsCompositeHelper.updateScrollableRegion( wComposite );
  }

  /** Ensure region in details is what is seen */
  private void syncRegionFromWidget( S3ConnectionType s3ConnectionType ) {
    if ( s3ConnectionType == S3ConnectionType.MINIO ) {
      String minioRegion = wMinioRegion.getText();
      details.setRegion( StringUtils.isBlank( minioRegion ) ? null : minioRegion );
    } else {
      details.setRegion( wRegion.getText() );
    }
  }

  private void moveWidgetsToBottom( S3ConnectionType s3ConnectionType, AuthType authType ) {
    for ( Control c : wBottomHalf.getChildren() ) {
      if ( c instanceof Label ) {
        c.dispose(); //Dispose any labels
      } else {
        c.setParent( wWidgetHolder ); //Other widgets go back to the wWidgetHolder (they hold the current value)
      }
    }

    Control above = null; // just to help with rearranging
    if ( s3ConnectionType == S3ConnectionType.AMAZON ) {
      switch ( authType ) {
        case KEYS -> {
          // Amazon with Access Key
          moveWidgetToBottomHalf( wAuthType, "ConnectionDialog.s3.AuthType.Label", null, 200 );
          moveWidgetToBottomHalf( wRegion, "ConnectionDialog.s3.Region.Label", wAuthType, 200 );
          moveWidgetToBottomHalf( wAccessKey, "ConnectionDialog.s3.AccessKey.Label", wRegion );
          moveWidgetToBottomHalf( wSecretKey, "ConnectionDialog.s3.SecretKey.Label", wAccessKey );
          moveWidgetToBottomHalf( wSessionToken, "ConnectionDialog.s3.sessionToken.Label", wSecretKey );
          moveWidgetToBottomHalf( wDefaultS3Config, "ConnectionDialog.s3.DefaultS3Config.Label", wSessionToken );
        }
        case CREDENTIALS_FILE -> {
          // Amazon with Credentials File
          moveWidgetToBottomHalf( wAuthType, "ConnectionDialog.s3.AuthType.Label", null, 200 );
          moveWidgetToBottomHalf( wRegion, "ConnectionDialog.s3.Region.Label", wAuthType, 200 );
          moveWidgetToBottomHalf( wProfileName, "ConnectionDialog.s3.ProfileName.Label", wRegion );
          moveWidgetToBottomHalf( wCredentialsFilePath, "ConnectionDialog.s3.CredentialsFilePath.Label", wProfileName );
        }
      }
    } else {
      // Minio
      above = moveWidgetToBottomHalf( wMinioRegion, "ConnectionDialog.s3.Region.Label", above );
      above = moveWidgetToBottomHalf( wAccessKey, "ConnectionDialog.s3.AccessKey.Label", above );
      above = moveWidgetToBottomHalf( wSecretKey, "ConnectionDialog.s3.SecretKey.Label", above );
      above = moveWidgetToBottomHalf( wEndpoint, "ConnectionDialog.s3.Endpoint.Label", above );
      above = moveWidgetToBottomHalf( wSignatureVersion, "ConnectionDialog.s3.SignatureVersion.Label", above );
      above = moveGroupToBottomHalf( gSslTrustStore, above );
      above = moveGroupToBottomHalf( gKeyStore, above );
      above = moveWidgetToBottomHalf( wPathStyleAccess, "ConnectionDialog.s3.PathStyleAccess.Label", above );
      above = moveWidgetToBottomHalf( wDefaultS3Config, "ConnectionDialog.s3.DefaultS3Config.Label", above );
    }
  }

  private void populateWidgets() {
    wS3ConnectionType.select( stringToInteger( details.getConnectionType() ) );
    wAuthType.select( stringToInteger( details.getAuthType() ) );
    wAccessKey.setText( Const.NVL( details.getAccessKey(), "" ) );
    wSecretKey.setText( Const.NVL( details.getSecretKey(), "" ) );
    wSessionToken.setText( Const.NVL( details.getSessionToken(), "" ) );
    wDefaultS3Config.setSelection( Boolean.parseBoolean( Const.NVL( details.getDefaultS3Config(), "false" ) ) );
    wDefaultS3Config.setVariableName( Const.NVL( details.getDefaultS3ConfigVariable(), "" ) );
    populateRegion();
    wProfileName.setText( Const.NVL( details.getProfileName(), "" ) );
    wCredentialsFilePath.setText( Const.NVL( details.getCredentialsFilePath(), "" ) );
    wEndpoint.setText( Const.NVL( details.getEndpoint(), "" ) );
    wSignatureVersion.setText( Const.NVL( details.getSignatureVersion(), "" ) );
    wPathStyleAccess.setSelection( Boolean.parseBoolean( Const.NVL( details.getPathStyleAccess(), "false" ) ) );
    wPathStyleAccess.setVariableName( Const.NVL( details.getPathStyleAccessVariable(), "" ) );

    wTrustStoreFilePath.setText( Const.NVL( details.getTrustStoreFilePath(), "" ) );
    wTrustStorePassword.setText( Const.NVL( details.getTrustStorePassword(), "" ) );
    wTrustAll.setSelection( Boolean.parseBoolean( Const.NVL( details.getTrustAll(), "false" ) ) );
    onTrustAllChanged();

    wKeyStoreFilePath.setText( Const.NVL( details.getKeyStoreFilePath(), "" ) );
    wKeyStorePassword.setText( Const.NVL( details.getKeyStorePassword(), "" ) );
  }

  private void populateRegion() {
    switch ( getConnectionType() ) {
      case AMAZON -> {
        int regionIndex = computeComboIndex( Const.NVL( details.getRegion(), Regions.DEFAULT_REGION.getName() ),
          regionChoices, -1 );
        if ( regionIndex != -1 ) {
          wRegion.select( regionIndex );
        } else {
          wRegion.setText( details.getRegion() );
        }
      }
      case MINIO -> {
        wMinioRegion.setText( Const.NVL( details.getRegion(), "" ) );
        int regionIndex = computeComboIndex( Regions.DEFAULT_REGION.getName(), regionChoices, 0 );
        wRegion.select( regionIndex );
      }
    }
  }

  private Label createLabel( String key, Control topWidget, Composite composite ) {
    return helper.createLabel( composite, SWT.LEFT | SWT.WRAP, key, topWidget );
  }

  private PasswordVisibleTextVar createStandbyPasswordVisibleTextVar( Composite container ) {
    return new PasswordVisibleTextVar( variableSpace, container, SWT.LEFT | SWT.BORDER );
  }

  private PasswordVisibleTextVar createStandbyPasswordVisibleTextVar() {
    return new PasswordVisibleTextVar( variableSpace, wWidgetHolder, SWT.LEFT | SWT.BORDER );
  }

  private TextVar createStandByTextVar() {
    return new TextVar( variableSpace, wWidgetHolder, TEXT_VAR_FLAGS );
  }

  private CCombo createCCombo( Control topWidget, int width, Composite composite ) {
    return helper.createCCombo( composite, TEXT_VAR_FLAGS, topWidget, width );
  }

  private CCombo createStandbyCombo() {
    return new CCombo( wWidgetHolder, TEXT_VAR_FLAGS );
  }

  private ComboVar createStandbyComboVar() {
    return new ComboVar( variableSpace, wWidgetHolder, TEXT_VAR_FLAGS );
  }

  private CheckBoxVar createStandByCheckBoxVar() {
    return new CheckBoxVar( variableSpace, wWidgetHolder, SWT.CHECK );
  }

  private Control moveWidgetToBottomHalf( Control targetWidget, String labelKey, Control topWidget ) {
    return moveWidgetToBottomHalf( targetWidget, labelKey, topWidget, 0 );
  }

  private Control moveWidgetToBottomHalf( Control targetWidget, String labelKey, Control topWidget, int width ) {
    Label lbl = createLabel( labelKey, topWidget, wBottomHalf );
    targetWidget.setParent( wBottomHalf );
    props.setLook( targetWidget );
    targetWidget.setLayoutData( helper.getFormDataField( lbl, width ) );
    return targetWidget;
  }

  private Control moveGroupToBottomHalf( Control target, Control above ) {
    target.setParent( wBottomHalf );
    props.setLook( target );
    target.setLayoutData( new FormDataBuilder().top( above ).left().right().result() );
    return target;
  }

  @Override
  public void close() {
    wComposite.dispose();
    if ( wBottomHalf != null ) {
      wBottomHalf.dispose();
    }

  }

  private String[] getAuthTypeChoices() {
    return Stream.of( AuthType.values() ).map( this::getAuthTypeLabel ).toArray( String[]::new );
  }

  private String[] getConnectionTypeChoices() {
    return Stream.of( S3ConnectionType.values() ).map( this::getS3ConnectionTypeLabel ).toArray( String[]::new );
  }

  private int stringToInteger( String value ) {
    return Integer.parseInt( Const.NVL( value, "0" ) );
  }

  private <E extends Enum<E>> Optional<E> computeComboValue( CCombo combo, Class<E> choicesClass ) {
    int idx = combo.getSelectionIndex();
    var choices = choicesClass.getEnumConstants();
    if ( idx < 0 || idx >= choices.length ) {
      return Optional.empty();
    }
    return Optional.of( choices[idx] );
  }

  private int computeComboIndex( String targetValue, String[] choices, int notFoundReturnValue ) {
    return IntStream.range( 0, choices.length )
      .filter( i -> choices[ i ].equals( targetValue ) )
      .findFirst().orElse( notFoundReturnValue );
  }

  public String validate() {
    if ( getConnectionType() == S3ConnectionType.AMAZON ) {
      int regionIndex = computeComboIndex( Const.NVL( details.getRegion(), regionChoices[ 0 ] ), regionChoices, -1 );
      if ( regionIndex == -1 && !StringUtil.isVariable( details.getRegion() ) ) {
        return BaseMessages.getString( PKG, "ConnectionDialog.s3.Validate.badRegionText" );
      }
    }
    return null;
  }

}
