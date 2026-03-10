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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import org.pentaho.amazon.s3.provider.S3Provider;
import org.pentaho.di.connections.vfs.VFSDetailsComposite;
import org.pentaho.di.core.bowl.Bowl;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.StringUtil;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.core.vfs.connections.ui.dialog.VFSDetailsCompositeHelper;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.widget.CheckBoxVar;
import org.pentaho.di.ui.core.widget.ComboVar;
import org.pentaho.di.ui.core.widget.FileChooserVar;
import org.pentaho.di.ui.core.widget.PasswordVisibleTextVar;
import org.pentaho.di.ui.core.widget.TextVar;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

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

  private VariableSpace variableSpace = Variables.getADefaultVariableSpace();
  private HashSet<Control> skipControls = new HashSet<>();

  public S3DetailComposite( Bowl bowl, Composite composite, S3Details details, PropsUI props ) {
    helper = new VFSDetailsCompositeHelper( PKG, props );
    this.bowl = bowl;
    this.props = props;
    this.wComposite = composite;
    this.details = details;
    regionChoices = details.getRegions().toArray( new String[ 0 ] );
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
      details.setMinioRegion( StringUtils.isBlank( minioRegion ) ? null : minioRegion );
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

    VFSDetailsCompositeHelper.setupCompositeResizeListener( wComposite );
    initializingUiForFirstTime = false;
    return wComposite;
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

  private void setupBottomHalf() {
    var s3ConnectionType = computeComboValue( wS3ConnectionType, S3ConnectionType.class ).orElse( S3ConnectionType.AMAZON );
    var authType = computeComboValue( wAuthType, AuthType.class ).orElse( AuthType.KEYS );

    if ( initializingUiForFirstTime || s3ConnectionType.ordinal() != stringToInteger( details.getConnectionType() ) ) {
      details.setConnectionType( String.valueOf( s3ConnectionType.ordinal() ) );
    }

    if ( initializingUiForFirstTime || !authType.toDetailsValue().equals( details.getAuthType() ) ) {
      details.setAuthType( authType.toDetailsValue() );
    }

    if ( details.getRegion() == null ) {
      details.setRegion( wRegion.getText() );
    }

    if ( !initializingUiForFirstTime ) {
      if ( s3ConnectionType == S3ConnectionType.AMAZON ) {
        // ensure leftover minio region cannot take precedence
        details.setMinioRegion( null );
      } else {
        // set back from stored in widget in case we switched back
        String minioRegion = wMinioRegion.getText();
        details.setMinioRegion( StringUtils.isBlank( minioRegion ) ? null : minioRegion );
      }
    }

    moveWidgetsToBottom( s3ConnectionType, authType );

    wBottomHalf.layout();
    wComposite.pack();
    VFSDetailsCompositeHelper.updateScrollableRegion( wComposite );
  }

  private void moveWidgetsToBottom( S3ConnectionType s3ConnectionType, AuthType authType ) {
    for ( Control c : wBottomHalf.getChildren() ) {
      if ( c instanceof Label ) {
        c.dispose(); //Dispose any labels
      } else {
        c.setParent( wWidgetHolder ); //Other widgets go back to the wWidgetHolder (they hold the current value)
      }
    }

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
      moveWidgetToBottomHalf( wMinioRegion, "ConnectionDialog.s3.Region.Label", null );
      moveWidgetToBottomHalf( wAccessKey, "ConnectionDialog.s3.AccessKey.Label", wMinioRegion );
      moveWidgetToBottomHalf( wSecretKey, "ConnectionDialog.s3.SecretKey.Label", wAccessKey );
      moveWidgetToBottomHalf( wEndpoint, "ConnectionDialog.s3.Endpoint.Label", wSecretKey );
      moveWidgetToBottomHalf( wSignatureVersion, "ConnectionDialog.s3.SignatureVersion.Label", wEndpoint );
      moveWidgetToBottomHalf( wPathStyleAccess, "ConnectionDialog.s3.PathStyleAccess.Label", wSignatureVersion );
      moveWidgetToBottomHalf( wDefaultS3Config, "ConnectionDialog.s3.DefaultS3Config.Label", wPathStyleAccess );
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
    int regionIndex = computeComboIndex( Const.NVL( details.getRegion(), regionChoices[ 0 ] ), regionChoices, -1 );
    if ( regionIndex != -1 ) {
      wRegion.select( regionIndex );
    } else {
      wRegion.setText( details.getRegion() );
    }
    wMinioRegion.setText( Const.NVL( details.getMinioRegion(), "" ) );
    wProfileName.setText( Const.NVL( details.getProfileName(), "" ) );
    wCredentialsFilePath.setText( Const.NVL( details.getCredentialsFilePath(), "" ) );
    wEndpoint.setText( Const.NVL( details.getEndpoint(), "" ) );
    wSignatureVersion.setText( Const.NVL( details.getSignatureVersion(), "" ) );
    wPathStyleAccess.setSelection( Boolean.parseBoolean( Const.NVL( details.getPathStyleAccess(), "false" ) ) );
    wPathStyleAccess.setVariableName( Const.NVL( details.getPathStyleAccessVariable(), "" ) );
  }

  private Label createLabel( String key, Control topWidget, Composite composite ) {
    return helper.createLabel( composite, SWT.LEFT | SWT.WRAP, key, topWidget );
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

  private void moveWidgetToBottomHalf( Control targetWidget, String labelKey, Control topWidget ) {
    moveWidgetToBottomHalf( targetWidget, labelKey, topWidget, 0 );
  }

  private void moveWidgetToBottomHalf( Control targetWidget, String labelKey, Control topWidget, int width ) {
    Label lbl = createLabel( labelKey, topWidget, wBottomHalf );
    targetWidget.setParent( wBottomHalf );
    props.setLook( targetWidget );
    targetWidget.setLayoutData( helper.getFormDataField( lbl, width ) );
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
    int regionIndex = computeComboIndex( Const.NVL( details.getRegion(), regionChoices[ 0 ] ), regionChoices, -1 );
    if ( regionIndex == -1 && !StringUtil.isVariable( details.getRegion() ) ) {
      return BaseMessages.getString( PKG, "ConnectionDialog.s3.Validate.badRegionText" );
    }
    return null;
  }

}
