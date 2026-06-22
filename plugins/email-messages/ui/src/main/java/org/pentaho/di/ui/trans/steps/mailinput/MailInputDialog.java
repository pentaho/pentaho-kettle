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


package org.pentaho.di.ui.trans.steps.mailinput;

import jakarta.mail.Folder;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.annotations.PluginDialog;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.entries.getpop.MailConnection;
import org.pentaho.di.job.entries.getpop.MailConnectionMeta;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.TransPreviewFactory;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.steps.mailinput.MailInputField;
import org.pentaho.di.trans.steps.mailinput.MailInputMeta;
import org.pentaho.di.ui.core.dialog.EnterNumberDialog;
import org.pentaho.di.ui.core.dialog.EnterTextDialog;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.dialog.PreviewRowsDialog;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.gui.WindowProperty;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.LabelTextVar;
import org.pentaho.di.ui.core.widget.PasswordTextVar;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.job.entries.getpop.SelectFolderDialog;
import org.pentaho.di.ui.trans.dialog.TransPreviewProgressDialog;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

import java.text.SimpleDateFormat;
import java.util.Calendar;

@PluginDialog( id = "MailInput", image = "GETPOP.svg", pluginType = PluginDialog.PluginType.STEP,
        documentationUrl = "http://wiki.pentaho.com/display/EAI/Email+Messages+Input" )
public class MailInputDialog extends BaseStepDialog implements StepDialogInterface {
  private static Class<?> PKG = MailInputMeta.class; // for i18n purposes, needed by Translator2!!

  private MailInputMeta input;
  private TextVar wServerName;

  private TextVar wSender;

  private TextVar wRecipient;

  private TextVar wSubject;

  private Combo wUseAuth;

  private Label wlGrantType;

  private Combo grantType;

  private LabelTextVar wAuthClientId;

  private LabelTextVar wAuthSecretKey;

  private LabelTextVar wAuthScope;

  private LabelTextVar wAuthTokenUrl;

  private LabelTextVar wAuthorizationCode;

  private LabelTextVar wRedirectUri;

  private LabelTextVar wAuthRefreshToken;

  private Label wlUserName;
  private TextVar wUserName;

  private Label wlIMAPFolder;
  private TextVar wIMAPFolder;

  private Label wlPassword;
  private TextVar wPassword;

  private Label wlUseProxy;
  private Button wUseProxy;

  private Label wlProxyUsername;
  private TextVar wProxyUsername;

  private Label wlListMails;
  private CCombo wListMails;

  private Label wlIMAPListMails;
  private CCombo wIMAPListMails;

  private Label wlFirstMails;
  private TextVar wFirstMails;

  private Label wlIMAPFirstMails;
  private TextVar wIMAPFirstMails;

  private Label wlPort;
  private TextVar wPort;

  private Label wlUseSSL;
  private Button wUseSSL;

  private Label wlIncludeSubFolders;
  private Button wIncludeSubFolders;

  private Button wNegateSender;

  private Button wNegateRecipient;

  private Button wNegateSubject;

  private Button wNegateReceivedDate;

  private Label wlPOP3Message;

  private Label wlLimit;
  private Text wLimit;

  private Label wlReadFrom;
  private TextVar wReadFrom;
  private Button open;

  private Label wlConditionOnReceivedDate;
  private CCombo wConditionOnReceivedDate;

  private Label wlReadTo;
  private TextVar wReadTo;
  private Button openTo;

  private CCombo wProtocol;

  private Button wTestIMAPFolder;

  private Button wSelectFolder;

  private Label wlFolderField;
  private Label wlDynamicFolder;
  private CCombo wFolderField;
  private Button wDynamicFolder;

  private TableView wFields;

  private MailConnection mailConn = null;

  private boolean gotPreviousFields = false;

  private Button wUseBatch;
  private Text wBatchSize;
  private TextVar wStartMessage;
  private TextVar wEndMessage;
  private Button wIgnoreFieldErrors;

  public MailInputDialog( Shell parent, Object in, TransMeta tr, String sName ) {
    super( parent, (BaseStepMeta) in, tr, sName );
    input = (MailInputMeta) in;
  }

  public String open() {
    Shell parent = getParent();
    Display display = parent.getDisplay();

    shell = new Shell( parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MIN | SWT.MAX );
    props.setLook( shell );
    setShellImage( shell, input );

    ModifyListener lsMod = new ModifyListener() {
      public void modifyText( ModifyEvent e ) {
        closeMailConnection();
        input.setChanged();
      }
    };

    changed = input.hasChanged();

    FormLayout formLayout = new FormLayout();
    formLayout.marginWidth = Const.FORM_MARGIN;
    formLayout.marginHeight = Const.FORM_MARGIN;

    shell.setLayout( formLayout );
    shell.setText( BaseMessages.getString( PKG, "MailInputdialog.Shell.Title" ) );

    int middle = props.getMiddlePct();
    int margin = Const.MARGIN;

    // Stepname line
    wlStepname = new Label( shell, SWT.RIGHT );
    wlStepname.setText( BaseMessages.getString( PKG, "MailInputdialog.Stepname.Label" ) );
    props.setLook( wlStepname );
    fdlStepname = new FormData();
    fdlStepname.left = new FormAttachment( 0, 0 );
    fdlStepname.right = new FormAttachment( middle, -margin );
    fdlStepname.top = new FormAttachment( 0, margin );
    wlStepname.setLayoutData( fdlStepname );
    wStepname = new Text( shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    wStepname.setText( stepname );
    props.setLook( wStepname );
    wStepname.addModifyListener( lsMod );
    fdStepname = new FormData();
    fdStepname.left = new FormAttachment( middle, 0 );
    fdStepname.top = new FormAttachment( 0, margin );
    fdStepname.right = new FormAttachment( 100, 0 );
    wStepname.setLayoutData( fdStepname );

    CTabFolder wTabFolder = new CTabFolder( shell, SWT.BORDER );
    props.setLook( wTabFolder, Props.WIDGET_STYLE_TAB );

    // ////////////////////////
    // START OF GENERAL TAB ///
    // ////////////////////////

    CTabItem wGeneralTab = new CTabItem( wTabFolder, SWT.NONE );
    wGeneralTab.setText( BaseMessages.getString( PKG, "MailInput.Tab.General.Label" ) );
    Composite wGeneralComp = new Composite( wTabFolder, SWT.NONE );
    props.setLook( wGeneralComp );
    FormLayout generalLayout = new FormLayout();
    generalLayout.marginWidth = 3;
    generalLayout.marginHeight = 3;
    wGeneralComp.setLayout( generalLayout );

    // ////////////////////////
    // START OF SERVER SETTINGS GROUP///
    // /
    Group wServerSettings = new Group( wGeneralComp, SWT.SHADOW_NONE );
    props.setLook( wServerSettings );
    wServerSettings.setText( BaseMessages.getString( PKG, "MailInput.ServerSettings.Group.Label" ) );

    FormLayout serverSettingsGroupLayout = new FormLayout();
    serverSettingsGroupLayout.marginWidth = 10;
    serverSettingsGroupLayout.marginHeight = 10;
    wServerSettings.setLayout( serverSettingsGroupLayout );

    // ServerName line
    Label wlServerName = new Label( wServerSettings, SWT.RIGHT );
    wlServerName.setText( BaseMessages.getString( PKG, "MailInput.Server.Label" ) );
    props.setLook( wlServerName );
    FormData fdlServerName = new FormData();
    fdlServerName.left = new FormAttachment( 0, 0 );
    fdlServerName.top = new FormAttachment( 0, 2 * margin );
    fdlServerName.right = new FormAttachment( middle, -margin );
    wlServerName.setLayoutData( fdlServerName );
    wServerName = new TextVar( transMeta, wServerSettings, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wServerName );
    wServerName.addModifyListener( lsMod );
    FormData fdServerName = new FormData();
    fdServerName.left = new FormAttachment( middle, 0 );
    fdServerName.top = new FormAttachment( 0, 2 * margin );
    fdServerName.right = new FormAttachment( 100, 0 );
    wServerName.setLayoutData( fdServerName );

    // USE connection with SSL
    wlUseSSL = new Label( wServerSettings, SWT.RIGHT );
    wlUseSSL.setText( BaseMessages.getString( PKG, "MailInput.UseSSLMails.Label" ) );
    props.setLook( wlUseSSL );
    FormData fdlUseSSL = new FormData();
    fdlUseSSL.left = new FormAttachment( 0, 0 );
    fdlUseSSL.top = new FormAttachment( wServerName, margin );
    fdlUseSSL.right = new FormAttachment( middle, -margin );
    wlUseSSL.setLayoutData( fdlUseSSL );
    wUseSSL = new Button( wServerSettings, SWT.CHECK );
    props.setLook( wUseSSL );
    FormData fdUseSSL = new FormData();
    wUseSSL.setToolTipText( BaseMessages.getString( PKG, "MailInput.UseSSLMails.Tooltip" ) );
    fdUseSSL.left = new FormAttachment( middle, 0 );
    fdUseSSL.top = new FormAttachment( wServerName, margin );
    fdUseSSL.right = new FormAttachment( 100, 0 );
    wUseSSL.setLayoutData( fdUseSSL );

    wUseSSL.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e ) {
        closeMailConnection();
        refreshPort( true );
      }
    } );

    // port
    wlPort = new Label( wServerSettings, SWT.RIGHT );
    wlPort.setText( BaseMessages.getString( PKG, "MailInput.SSLPort.Label" ) );
    props.setLook( wlPort );
    FormData fdlPort = new FormData();
    fdlPort.left = new FormAttachment( 0, 0 );
    fdlPort.top = new FormAttachment( wUseSSL, margin );
    fdlPort.right = new FormAttachment( middle, -margin );
    wlPort.setLayoutData( fdlPort );
    wPort = new TextVar( transMeta, wServerSettings, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wPort );
    wPort.setToolTipText( BaseMessages.getString( PKG, "MailInput.SSLPort.Tooltip" ) );
    wPort.addModifyListener( lsMod );
    FormData fdPort = new FormData();
    fdPort.left = new FormAttachment( middle, 0 );
    fdPort.top = new FormAttachment( wUseSSL, margin );
    fdPort.right = new FormAttachment( 100, 0 );
    wPort.setLayoutData( fdPort );

    FormData fdServerSettings = new FormData();
    fdServerSettings.left = new FormAttachment( 0, margin );
    fdServerSettings.top = new FormAttachment( wProtocol, margin );
    fdServerSettings.right = new FormAttachment( 100, -margin );
    wServerSettings.setLayoutData( fdServerSettings );

    // ////////////////////////////////////
    // START OF AUTHENTIFICATION GROUP
    // ////////////////////////////////////

    Group wAuthentificationGroup = new Group( wGeneralComp, SWT.SHADOW_NONE );
    props.setLook( wAuthentificationGroup );
    wAuthentificationGroup.setText( BaseMessages.getString( PKG, "MailInput.Group.Authentification.Label" ) );

    FormLayout authentificationGroupLayout = new FormLayout();
    authentificationGroupLayout.marginWidth = 10;
    authentificationGroupLayout.marginHeight = 10;
    wAuthentificationGroup.setLayout( authentificationGroupLayout );

    // Authentication?
    Label wlUseAuth = new Label( wAuthentificationGroup, SWT.RIGHT );
    wlUseAuth.setText( BaseMessages.getString( PKG, "MailInput.UseAuthentication.Label" ) );
    props.setLook( wlUseAuth );
    FormData fdlUseAuth = new FormData();
    fdlUseAuth.left = new FormAttachment( 0, 0 );
    fdlUseAuth.top = new FormAttachment( wServerSettings, margin );
    fdlUseAuth.right = new FormAttachment( middle, -2 * margin );
    wlUseAuth.setLayoutData( fdlUseAuth );
    wUseAuth = new Combo( wAuthentificationGroup, SWT.DROP_DOWN | SWT.READ_ONLY );
    wUseAuth.add( MailInputMeta.AUTENTICATION_BASIC );
    wUseAuth.add( MailInputMeta.AUTENTICATION_OAUTH );
    wUseAuth.select( wUseAuth.indexOf( MailInputMeta.AUTENTICATION_BASIC ) );
    props.setLook( wUseAuth );
    wUseAuth.addModifyListener( lsMod );
    FormData fdUseAuth = new FormData();
    fdUseAuth.left = new FormAttachment( middle, -margin );
    fdUseAuth.top = new FormAttachment( wServerSettings, margin );
    fdUseAuth.right = new FormAttachment( 100, 0 );
    wUseAuth.setLayoutData( fdUseAuth );
    wUseAuth.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e ) {
        setUseAuth();
      }
    } );

    // UserName line
    wlUserName = new Label( wAuthentificationGroup, SWT.RIGHT );
    wlUserName.setText( BaseMessages.getString( PKG, "MailInput.Username.Label" ) );
    props.setLook( wlUserName );
    FormData fdlUserName = new FormData();
    fdlUserName.left = new FormAttachment( 0, 0 );
    fdlUserName.top = new FormAttachment( wUseAuth, margin );
    fdlUserName.right = new FormAttachment( middle, -margin );
    wlUserName.setLayoutData( fdlUserName );
    wUserName = new TextVar( transMeta, wAuthentificationGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wUserName );
    wUserName.setToolTipText( BaseMessages.getString( PKG, "MailInput.Username.Tooltip" ) );
    wUserName.addModifyListener( lsMod );
    FormData fdUserName = new FormData();
    fdUserName.left = new FormAttachment( middle, 0 );
    fdUserName.top = new FormAttachment( wUseAuth, margin );
    fdUserName.right = new FormAttachment( 100, 0 );
    wUserName.setLayoutData( fdUserName );

    // Password line
    wlPassword = new Label( wAuthentificationGroup, SWT.RIGHT );
    wlPassword.setText( BaseMessages.getString( PKG, "MailInput.Password.Label" ) );
    props.setLook( wlPassword );
    FormData fdlPassword = new FormData();
    fdlPassword.left = new FormAttachment( 0, 0 );
    fdlPassword.top = new FormAttachment( wUserName, margin );
    fdlPassword.right = new FormAttachment( middle, -margin );
    wlPassword.setLayoutData( fdlPassword );
    wPassword = new PasswordTextVar( transMeta, wAuthentificationGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wPassword );
    wPassword.addModifyListener( lsMod );
    FormData fdPassword = new FormData();
    fdPassword.left = new FormAttachment( middle, 0 );
    fdPassword.top = new FormAttachment( wUserName, margin );
    fdPassword.right = new FormAttachment( 100, 0 );
    wPassword.setLayoutData( fdPassword );
    // AuthSecretKey line
    wAuthSecretKey = new LabelTextVar( transMeta, wAuthentificationGroup,
            BaseMessages.getString( PKG, "MailInput.AuthenticationSecretKey.Label" ),
            BaseMessages.getString( PKG, "MailInput.AuthenticationSecretKey.Tooltip" ), true );
    wAuthSecretKey.addModifyListener( lsMod );
    FormData fdAuthSecretKey = new FormData();
    fdAuthSecretKey.left = new FormAttachment( 0, 0 );
    fdAuthSecretKey.top = new FormAttachment( wPassword, margin );
    fdAuthSecretKey.right = new FormAttachment( 100, 0 );
    wAuthSecretKey.setLayoutData( fdAuthSecretKey );

    // AuthClientId line
    wAuthClientId = new LabelTextVar( transMeta, wAuthentificationGroup,
            BaseMessages.getString( PKG, "MailInput.AuthenticationClientId.Label" ),
            BaseMessages.getString( PKG, "MailInput.AuthenticationClientId.Tooltip" ));
    wAuthClientId.addModifyListener( lsMod );
    FormData fdAuthClientId = new FormData();
    fdAuthClientId.left = new FormAttachment( 0, 0 );
    fdAuthClientId.top = new FormAttachment( wAuthSecretKey, margin );
    fdAuthClientId.right = new FormAttachment( 100, 0 );
    wAuthClientId.setLayoutData( fdAuthClientId );

    //Scope line
    wAuthScope = new LabelTextVar( transMeta, wAuthentificationGroup,
            BaseMessages.getString( PKG, "MailInput.AuthenticationScope.Label" ),
            BaseMessages.getString( PKG, "MailInput.AuthenticationScope.Tooltip" ));
    wAuthScope.addModifyListener( lsMod );
    FormData fdAuthScope = new FormData();
    fdAuthScope.left = new FormAttachment( 0, 0 );
    fdAuthScope.top = new FormAttachment( wAuthClientId, margin );
    fdAuthScope.right = new FormAttachment( 100, 0 );
    wAuthScope.setLayoutData( fdAuthScope );
// Grant Type
    wlGrantType = new Label( wAuthentificationGroup, SWT.RIGHT );
    wlGrantType.setText(BaseMessages.getString( PKG, "MailInput.GrantType.Label" ) );
    props.setLook( wlGrantType );
    FormData fdlGrantType = new FormData();
    fdlGrantType.left = new FormAttachment( 0, 0 );
    fdlGrantType.top = new FormAttachment( wAuthScope, 2*margin );
    fdlGrantType.right = new FormAttachment( middle, -margin );
    wlGrantType.setLayoutData( fdlGrantType );
    grantType = new Combo( wAuthentificationGroup, SWT.DROP_DOWN );
    grantType.add( MailInputMeta.GRANTTYPE_CLIENTCREDENTIALS );
    grantType.add( MailInputMeta.GRANTTYPE_AUTHORIZATION_CODE );
    grantType.add( MailInputMeta.GRANTTYPE_REFRESH_TOKEN );
    props.setLook( grantType );
    grantType.addModifyListener( lsMod );
    FormData fdGrantType = new FormData();
    fdGrantType.left = new FormAttachment( middle, margin );
    fdGrantType.top = new FormAttachment( wAuthScope, 2*margin );
    fdGrantType.right = new FormAttachment( 100, 0 );
    grantType.setLayoutData( fdGrantType );
    grantType.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e ) {
        setUseGrantType();
      }
    } );
    //Token Url
    wAuthTokenUrl = new LabelTextVar( transMeta, wAuthentificationGroup,
            BaseMessages.getString( PKG, "MailInput.AuthenticationTokenUrl.Label" ),
            BaseMessages.getString( PKG, "MailInput.AuthenticationTokenUrl.Tooltip" ));
    wAuthTokenUrl.addModifyListener( lsMod );
    FormData fdAuthTokenUrl = new FormData();
    fdAuthTokenUrl.left = new FormAttachment( 0, 0 );
    fdAuthTokenUrl.top = new FormAttachment( grantType, margin );
    fdAuthTokenUrl.right = new FormAttachment( 100, 0 );
    wAuthTokenUrl.setLayoutData( fdAuthTokenUrl );
    //AuthorizationCode
    wAuthorizationCode= new LabelTextVar( transMeta, wAuthentificationGroup,
            BaseMessages.getString( PKG, "MailInput.AuthorizationCode.Label" ),
            BaseMessages.getString( PKG, "MailInput.AuthorizationCode.Tooltip" ));
    wAuthorizationCode.addModifyListener( lsMod );
    FormData fdAuthorizationCode = new FormData();
    fdAuthorizationCode.left = new FormAttachment( 0, 0 );
    fdAuthorizationCode.top = new FormAttachment( wAuthTokenUrl, margin );
    fdAuthorizationCode.right = new FormAttachment( 100, 0 );
    wAuthorizationCode.setLayoutData( fdAuthorizationCode );
    //Redirect Uri
    wRedirectUri= new LabelTextVar( transMeta, wAuthentificationGroup,
            BaseMessages.getString( PKG, "MailInput.RedirectURI.Label" ),
            BaseMessages.getString( PKG, "MailInput.RedirectURI.Tooltip" ));
    wRedirectUri.addModifyListener( lsMod );
    FormData fdRedirectUri = new FormData();
    fdRedirectUri.left = new FormAttachment( 0, 0 );
    fdRedirectUri.top = new FormAttachment(wAuthorizationCode, margin );
    fdRedirectUri.right = new FormAttachment( 100, 0 );
    wRedirectUri.setLayoutData( fdRedirectUri );
    //Refresh Token
    wAuthRefreshToken= new LabelTextVar( transMeta, wAuthentificationGroup,
            BaseMessages.getString( PKG, "MailInput.RefreshToken.Label" ),
            BaseMessages.getString( PKG, "MailInput.RefreshToken.Tooltip" ));
    wAuthRefreshToken.addModifyListener( lsMod );
    FormData fdAuthRefreshToken = new FormData();
    fdAuthRefreshToken.left = new FormAttachment( 0, 0 );
    fdAuthRefreshToken.top = new FormAttachment(wRedirectUri, margin );
    fdAuthRefreshToken.right = new FormAttachment( 100, 0 );
    wAuthRefreshToken.setLayoutData( fdAuthRefreshToken );



    // USE proxy
    wlUseProxy = new Label( wAuthentificationGroup, SWT.RIGHT );
    wlUseProxy.setText( BaseMessages.getString( PKG, "MailInput.UseProxyMails.Label" ) );
    props.setLook( wlUseProxy );
    FormData fdlUseProxy = new FormData();
    fdlUseProxy.left = new FormAttachment( 0, 0 );
    fdlUseProxy.top = new FormAttachment( wAuthRefreshToken, 2 * margin );
    fdlUseProxy.right = new FormAttachment( middle, -margin );
    wlUseProxy.setLayoutData( fdlUseProxy );
    wUseProxy = new Button( wAuthentificationGroup, SWT.CHECK );
    props.setLook( wUseProxy );
    FormData fdUseProxy = new FormData();
    wUseProxy.setToolTipText( BaseMessages.getString( PKG, "MailInput.UseProxyMails.Tooltip" ) );
    fdUseProxy.left = new FormAttachment( middle, 0 );
    fdUseProxy.top = new FormAttachment( wAuthRefreshToken, 2 * margin );
    fdUseProxy.right = new FormAttachment( 100, 0 );
    wUseProxy.setLayoutData( fdUseProxy );

    wUseProxy.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e ) {
        setUserProxy();
        input.setChanged();
      }
    } );

    // ProxyUsername line
    wlProxyUsername = new Label( wAuthentificationGroup, SWT.RIGHT );
    wlProxyUsername.setText( BaseMessages.getString( PKG, "MailInput.ProxyUsername.Label" ) );
    wProxyUsername = new TextVar( transMeta, wAuthentificationGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    wProxyUsername.setToolTipText( BaseMessages.getString( PKG, "MailInput.ProxyUsername.Tooltip" ) );
    wProxyUsername.addModifyListener( lsMod );
    addLabelInputPairBelow( wlProxyUsername, wProxyUsername, wUseProxy );

    // Use Batch label/checkbox
    Label wlUseBatch = new Label( wAuthentificationGroup, SWT.RIGHT );
    wlUseBatch.setText( BaseMessages.getString( PKG, "MailInputDialog.UseBatch.Label" ) );
    wUseBatch = new Button( wAuthentificationGroup, SWT.CHECK );
    wUseBatch.setToolTipText( BaseMessages.getString( PKG, "MailInputDialog.UseBatch.Tooltip" ) );
    wUseBatch.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e ) {
        setBatchSettingsEnabled();
      }
    } );
    addLabelInputPairBelow( wlUseBatch, wUseBatch, wProxyUsername );
    // ignore field errors
    Label wlIgnoreFieldErrors = new Label( wAuthentificationGroup, SWT.RIGHT );
    wlIgnoreFieldErrors.setText( BaseMessages.getString( PKG, "MailInput.IgnoreFieldErrors.Label" ) );
    wIgnoreFieldErrors = new Button( wAuthentificationGroup, SWT.CHECK );
    wIgnoreFieldErrors.setToolTipText( BaseMessages.getString( PKG, "MailInput.IgnoreFieldErrors.Tooltip" ) );
    addLabelInputPairBelow( wlIgnoreFieldErrors, wIgnoreFieldErrors, wUseBatch );

    // Protocol
    Label wlProtocol = new Label( wAuthentificationGroup, SWT.RIGHT );
    wlProtocol.setText( BaseMessages.getString( PKG, "MailInput.Protocol.Label" ) );
    wProtocol = new CCombo( wAuthentificationGroup, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER );
    wProtocol.setItems( MailConnectionMeta.protocolCodes );
    wProtocol.select( 0 );
    wProtocol.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e ) {
        refreshProtocol( true );

      }
    } );
    addLabelInputPairBelow( wlProtocol, wProtocol, wIgnoreFieldErrors );

    // Test connection button
    Button wTest = new Button( wAuthentificationGroup, SWT.PUSH );
    wTest.setText( BaseMessages.getString( PKG, "MailInput.TestConnection.Label" ) );
    props.setLook( wTest );
    FormData fdTest = new FormData();
    wTest.setToolTipText( BaseMessages.getString( PKG, "MailInput.TestConnection.Tooltip" ) );
    fdTest.top = new FormAttachment( wProtocol, margin );
    fdTest.right = new FormAttachment( 100, 0 );
    wTest.setLayoutData( fdTest );

    FormData fdAuthentificationGroup = new FormData();
    fdAuthentificationGroup.left = new FormAttachment( 0, margin );
    fdAuthentificationGroup.top = new FormAttachment( wServerSettings, margin );
    fdAuthentificationGroup.right = new FormAttachment( 100, -margin );
    fdAuthentificationGroup.bottom = new FormAttachment( 100, -margin );
    wAuthentificationGroup.setLayoutData( fdAuthentificationGroup );

    // //////////////////////////////////////
    // END OF AUTHENTIFICATION GROUP
    // //////////////////////////////////////
    FormData fdGeneralComp = new FormData();
    fdGeneralComp.left = new FormAttachment( 0, 0 );
    fdGeneralComp.top = new FormAttachment( wStepname, 0 );
    fdGeneralComp.right = new FormAttachment( 100, 0 );
    fdGeneralComp.bottom = new FormAttachment( 100, 0 );
    wGeneralComp.setLayoutData( fdGeneralComp );

    wGeneralComp.layout();
    wGeneralTab.setControl( wGeneralComp );
    props.setLook( wGeneralComp );

    // ///////////////////////////////////////////////////////////
    // END OF GENERAL TAB
    // ///////////////////////////////////////////////////////////

    // ////////////////////////
    // START OF SETTINGS TAB
    // ////////////////////////

    CTabItem wSettingsTab = new CTabItem( wTabFolder, SWT.NONE );
    wSettingsTab.setText( BaseMessages.getString( PKG, "MailInput.Tab.Pop.Label" ) );
    Composite wSettingsComp = new Composite( wTabFolder, SWT.NONE );
    props.setLook( wSettingsComp );
    FormLayout PopLayout = new FormLayout();
    PopLayout.marginWidth = 3;
    PopLayout.marginHeight = 3;
    wSettingsComp.setLayout( PopLayout );

    // Message: for POP3, only INBOX folder is available!
    wlPOP3Message = new Label( wSettingsComp, SWT.RIGHT );
    wlPOP3Message.setText( BaseMessages.getString( PKG, "MailInput.POP3Message.Label" ) );
    props.setLook( wlPOP3Message );
    FormData fdlPOP3Message = new FormData();
    fdlPOP3Message.left = new FormAttachment( 0, margin );
    fdlPOP3Message.top = new FormAttachment( 0, 3 * margin );
    wlPOP3Message.setLayoutData( fdlPOP3Message );
    wlPOP3Message.setForeground( GUIResource.getInstance().getColorOrange() );

    // ////////////////////////
    // START OF POP3 Settings GROUP///
    // /
    Group wPOP3Settings = new Group( wSettingsComp, SWT.SHADOW_NONE );
    props.setLook( wPOP3Settings );
    wPOP3Settings.setText( BaseMessages.getString( PKG, "MailInput.POP3Settings.Group.Label" ) );

    FormLayout pop3SettingsGroupLayout = new FormLayout();
    pop3SettingsGroupLayout.marginWidth = 10;
    pop3SettingsGroupLayout.marginHeight = 10;
    wPOP3Settings.setLayout( pop3SettingsGroupLayout );

    // List of mails of retrieve
    wlListMails = new Label( wPOP3Settings, SWT.RIGHT );
    wlListMails.setText( BaseMessages.getString( PKG, "MailInput.Listmails.Label" ) );
    props.setLook( wlListMails );
    FormData fdlListMails = new FormData();
    fdlListMails.left = new FormAttachment( 0, 0 );
    fdlListMails.right = new FormAttachment( middle, 0 );
    fdlListMails.top = new FormAttachment( wlPOP3Message, 2 * margin );
    wlListMails.setLayoutData( fdlListMails );
    wListMails = new CCombo( wPOP3Settings, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER );
    wListMails.add( BaseMessages.getString( PKG, "MailInput.RetrieveAllMails.Label" ) );
    // [PDI-7241] pop3 does not support retrieve unread option
    wListMails.add( BaseMessages.getString( PKG, "MailInput.RetrieveFirstMails.Label" ) );
    wListMails.select( 0 ); // +1: starts at -1

    props.setLook( wListMails );
    FormData fdListMails = new FormData();
    fdListMails.left = new FormAttachment( middle, 0 );
    fdListMails.top = new FormAttachment( wlPOP3Message, 2 * margin );
    fdListMails.right = new FormAttachment( 100, 0 );
    wListMails.setLayoutData( fdListMails );

    wListMails.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e ) {
        input.setChanged();
        chooseListMails();
      }
    } );

    // Retrieve the first ... mails
    wlFirstMails = new Label( wPOP3Settings, SWT.RIGHT );
    wlFirstMails.setText( BaseMessages.getString( PKG, "MailInput.Firstmails.Label" ) );
    props.setLook( wlFirstMails );
    FormData fdlFirstMails = new FormData();
    fdlFirstMails.left = new FormAttachment( 0, 0 );
    fdlFirstMails.right = new FormAttachment( middle, -margin );
    fdlFirstMails.top = new FormAttachment( wListMails, margin );
    wlFirstMails.setLayoutData( fdlFirstMails );

    wFirstMails = new TextVar( transMeta, wPOP3Settings, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wFirstMails );
    wFirstMails.addModifyListener( lsMod );
    FormData fdFirstMails = new FormData();
    fdFirstMails.left = new FormAttachment( middle, 0 );
    fdFirstMails.top = new FormAttachment( wListMails, margin );
    fdFirstMails.right = new FormAttachment( 100, 0 );
    wFirstMails.setLayoutData( fdFirstMails );

    FormData fdPOP3Settings = new FormData();
    fdPOP3Settings.left = new FormAttachment( 0, margin );
    fdPOP3Settings.top = new FormAttachment( wlPOP3Message, 2 * margin );
    fdPOP3Settings.right = new FormAttachment( 100, -margin );
    wPOP3Settings.setLayoutData( fdPOP3Settings );
    // ///////////////////////////////////////////////////////////
    // END OF POP3 SETTINGS GROUP
    // ///////////////////////////////////////////////////////////

    // ////////////////////////
    // START OF IMAP Settings GROUP
    // ////////////////////////
    Group wIMAPSettings = new Group( wSettingsComp, SWT.SHADOW_NONE );
    props.setLook( wIMAPSettings );
    wIMAPSettings.setText( BaseMessages.getString( PKG, "MailInput.IMAPSettings.Groupp.Label" ) );

    FormLayout imapSettingsGroupLayout = new FormLayout();
    imapSettingsGroupLayout.marginWidth = 10;
    imapSettingsGroupLayout.marginHeight = 10;
    wIMAPSettings.setLayout( imapSettingsGroupLayout );

    // Is folder name defined in a Field
    wlDynamicFolder = new Label( wIMAPSettings, SWT.RIGHT );
    wlDynamicFolder.setText( BaseMessages.getString( PKG, "MailInput.dynamicFolder.Label" ) );
    props.setLook( wlDynamicFolder );
    FormData fdlDynamicFolder = new FormData();
    fdlDynamicFolder.left = new FormAttachment( 0, 0 );
    fdlDynamicFolder.top = new FormAttachment( 0, margin );
    fdlDynamicFolder.right = new FormAttachment( middle, -margin );
    wlDynamicFolder.setLayoutData( fdlDynamicFolder );

    wDynamicFolder = new Button( wIMAPSettings, SWT.CHECK );
    props.setLook( wDynamicFolder );
    wDynamicFolder.setToolTipText( BaseMessages.getString( PKG, "MailInput.dynamicFolder.Tooltip" ) );
    FormData fdDynamicFolder = new FormData();
    fdDynamicFolder.left = new FormAttachment( middle, 0 );
    fdDynamicFolder.top = new FormAttachment( 0, margin );
    wDynamicFolder.setLayoutData( fdDynamicFolder );
    SelectionAdapter lsXmlStream = new SelectionAdapter() {
      public void widgetSelected( SelectionEvent arg0 ) {
        activeDynamicFolder();
        input.setChanged();
      }
    };
    wDynamicFolder.addSelectionListener( lsXmlStream );

    // Folder field
    wlFolderField = new Label( wIMAPSettings, SWT.RIGHT );
    wlFolderField.setText( BaseMessages.getString( PKG, "MailInput.wlFolderField.Label" ) );
    props.setLook( wlFolderField );
    FormData fdlFolderField = new FormData();
    fdlFolderField.left = new FormAttachment( 0, 0 );
    fdlFolderField.top = new FormAttachment( wDynamicFolder, margin );
    fdlFolderField.right = new FormAttachment( middle, -margin );
    wlFolderField.setLayoutData( fdlFolderField );

    wFolderField = new CCombo( wIMAPSettings, SWT.BORDER | SWT.READ_ONLY );
    wFolderField.setEditable( true );
    props.setLook( wFolderField );
    wFolderField.addModifyListener( lsMod );
    FormData fdFolderField = new FormData();
    fdFolderField.left = new FormAttachment( middle, 0 );
    fdFolderField.top = new FormAttachment( wDynamicFolder, margin );
    fdFolderField.right = new FormAttachment( 100, -margin );
    wFolderField.setLayoutData( fdFolderField );
    wFolderField.addFocusListener( new FocusListener() {
      public void focusLost( org.eclipse.swt.events.FocusEvent e ) {
      }

      public void focusGained( org.eclipse.swt.events.FocusEvent e ) {
        setFolderField();
      }
    } );

    // SelectFolder button
    wSelectFolder = new Button( wIMAPSettings, SWT.PUSH );
    wSelectFolder.setImage( GUIResource.getInstance().getImageBol() );
    wSelectFolder.setToolTipText( BaseMessages.getString( PKG, "MailInput.SelectFolderConnection.Label" ) );
    props.setLook( wSelectFolder );
    FormData fdSelectFolder = new FormData();
    wSelectFolder.setToolTipText( BaseMessages.getString( PKG, "MailInput.SelectFolderConnection.Tooltip" ) );
    fdSelectFolder.top = new FormAttachment( wFolderField, margin );
    fdSelectFolder.right = new FormAttachment( 100, 0 );
    wSelectFolder.setLayoutData( fdSelectFolder );

    // TestIMAPFolder button
    wTestIMAPFolder = new Button( wIMAPSettings, SWT.PUSH );
    wTestIMAPFolder.setText( BaseMessages.getString( PKG, "MailInput.TestIMAPFolderConnection.Label" ) );
    props.setLook( wTestIMAPFolder );
    FormData fdTestIMAPFolder = new FormData();
    wTestIMAPFolder.setToolTipText( BaseMessages.getString( PKG, "MailInput.TestIMAPFolderConnection.Tooltip" ) );
    fdTestIMAPFolder.top = new FormAttachment( wFolderField, margin );
    fdTestIMAPFolder.right = new FormAttachment( wSelectFolder, -margin );
    wTestIMAPFolder.setLayoutData( fdTestIMAPFolder );

    // IMAPFolder line
    wlIMAPFolder = new Label( wIMAPSettings, SWT.RIGHT );
    wlIMAPFolder.setText( BaseMessages.getString( PKG, "MailInput.IMAPFolder.Label" ) );
    props.setLook( wlIMAPFolder );
    FormData fdlIMAPFolder = new FormData();
    fdlIMAPFolder.left = new FormAttachment( 0, 0 );
    fdlIMAPFolder.top = new FormAttachment( wFolderField, margin );
    fdlIMAPFolder.right = new FormAttachment( middle, -margin );
    wlIMAPFolder.setLayoutData( fdlIMAPFolder );
    wIMAPFolder = new TextVar( transMeta, wIMAPSettings, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wIMAPFolder );
    wIMAPFolder.setToolTipText( BaseMessages.getString( PKG, "MailInput.IMAPFolder.Tooltip" ) );
    wIMAPFolder.addModifyListener( lsMod );
    FormData fdIMAPFolder = new FormData();
    fdIMAPFolder.left = new FormAttachment( middle, 0 );
    fdIMAPFolder.top = new FormAttachment( wFolderField, margin );
    fdIMAPFolder.right = new FormAttachment( wTestIMAPFolder, -margin );
    wIMAPFolder.setLayoutData( fdIMAPFolder );

    // Include subfolders?
    wlIncludeSubFolders = new Label( wIMAPSettings, SWT.RIGHT );
    wlIncludeSubFolders.setText( BaseMessages.getString( PKG, "MailInput.IncludeSubFoldersMails.Label" ) );
    props.setLook( wlIncludeSubFolders );
    FormData fdlIncludeSubFolders = new FormData();
    fdlIncludeSubFolders.left = new FormAttachment( 0, 0 );
    fdlIncludeSubFolders.top = new FormAttachment( wIMAPFolder, margin );
    fdlIncludeSubFolders.right = new FormAttachment( middle, -margin );
    wlIncludeSubFolders.setLayoutData( fdlIncludeSubFolders );
    wIncludeSubFolders = new Button( wIMAPSettings, SWT.CHECK );
    props.setLook( wIncludeSubFolders );
    FormData fdIncludeSubFolders = new FormData();
    wIncludeSubFolders.setToolTipText( BaseMessages.getString( PKG, "MailInput.IncludeSubFoldersMails.Tooltip" ) );
    fdIncludeSubFolders.left = new FormAttachment( middle, 0 );
    fdIncludeSubFolders.top = new FormAttachment( wIMAPFolder, margin );
    fdIncludeSubFolders.right = new FormAttachment( 100, 0 );
    wIncludeSubFolders.setLayoutData( fdIncludeSubFolders );
    wIncludeSubFolders.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e1 ) {
        input.setChanged();
        closeMailConnection();
      }
    } );

    // List of mails of retrieve
    wlIMAPListMails = new Label( wIMAPSettings, SWT.RIGHT );
    wlIMAPListMails.setText( BaseMessages.getString( PKG, "MailInput.IMAPListmails.Label" ) );
    props.setLook( wlIMAPListMails );
    FormData fdlIMAPListMails = new FormData();
    fdlIMAPListMails.left = new FormAttachment( 0, 0 );
    fdlIMAPListMails.right = new FormAttachment( middle, -margin );
    fdlIMAPListMails.top = new FormAttachment( wIncludeSubFolders, margin );
    wlIMAPListMails.setLayoutData( fdlIMAPListMails );
    wIMAPListMails = new CCombo( wIMAPSettings, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER );
    wIMAPListMails.setItems( MailConnectionMeta.valueIMAPListDesc );
    wIMAPListMails.select( 0 ); // +1: starts at -1

    props.setLook( wIMAPListMails );
    FormData fdIMAPListMails = new FormData();
    fdIMAPListMails.left = new FormAttachment( middle, 0 );
    fdIMAPListMails.top = new FormAttachment( wIncludeSubFolders, margin );
    fdIMAPListMails.right = new FormAttachment( 100, 0 );
    wIMAPListMails.setLayoutData( fdIMAPListMails );

    wIMAPListMails.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e ) {
        // ChooseIMAPListmails();

      }
    } );

    // Retrieve the first ... mails
    wlIMAPFirstMails = new Label( wIMAPSettings, SWT.RIGHT );
    wlIMAPFirstMails.setText( BaseMessages.getString( PKG, "MailInput.IMAPFirstmails.Label" ) );
    props.setLook( wlIMAPFirstMails );
    FormData fdlIMAPFirstMails = new FormData();
    fdlIMAPFirstMails.left = new FormAttachment( 0, 0 );
    fdlIMAPFirstMails.right = new FormAttachment( middle, -margin );
    fdlIMAPFirstMails.top = new FormAttachment( wIMAPListMails, margin );
    wlIMAPFirstMails.setLayoutData( fdlIMAPFirstMails );

    wIMAPFirstMails = new TextVar( transMeta, wIMAPSettings, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wIMAPFirstMails );
    wIMAPFirstMails.addModifyListener( lsMod );
    FormData fdIMAPFirstMails = new FormData();
    fdIMAPFirstMails.left = new FormAttachment( middle, 0 );
    fdIMAPFirstMails.top = new FormAttachment( wIMAPListMails, margin );
    fdIMAPFirstMails.right = new FormAttachment( 100, 0 );
    wIMAPFirstMails.setLayoutData( fdIMAPFirstMails );

    FormData fdIMAPSettings = new FormData();
    fdIMAPSettings.left = new FormAttachment( 0, margin );
    fdIMAPSettings.top = new FormAttachment( wPOP3Settings, 2 * margin );
    fdIMAPSettings.right = new FormAttachment( 100, -margin );
    wIMAPSettings.setLayoutData( fdIMAPSettings );
    // ///////////////////////////////////////////////////////////
    // END OF IMAP SETTINGS GROUP
    // ///////////////////////////////////////////////////////////

    // ////////////////////////////////
    // START OF Batch Settings GROUP
    // ////////////////////////////////
    Group wBatchSettingsGroup = createGroup( wSettingsComp, wIMAPSettings, BaseMessages.getString(
      PKG, "MailInputDialog.BatchSettingsGroup.Label" ) );

    // Batch size
    Label wlBatchSize = new Label( wBatchSettingsGroup, SWT.RIGHT );
    wlBatchSize.setText( BaseMessages.getString( PKG, "MailInputDialog.BatchSize.Label" ) );
    wBatchSize = new Text( wBatchSettingsGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    addLabelInputPairBelow( wlBatchSize, wBatchSize, wBatchSettingsGroup );

    // Starting message
    Label wlStartMessage = new Label( wBatchSettingsGroup, SWT.RIGHT );
    wlStartMessage.setText( BaseMessages.getString( PKG, "MailInputDialog.StartMessage.Label" ) );
    wStartMessage = new TextVar( transMeta, wBatchSettingsGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    addLabelInputPairBelow( wlStartMessage, wStartMessage, wBatchSize );

    // Last message
    Label wlEndMessage = new Label( wBatchSettingsGroup, SWT.RIGHT );
    wlEndMessage.setText( BaseMessages.getString( PKG, "MailInputDialog.EndMessage.Label" ) );
    wEndMessage = new TextVar( transMeta, wBatchSettingsGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    addLabelInputPairBelow( wlEndMessage, wEndMessage, wStartMessage );

    // ///////////////////////////////
    // END OF Batch Settings GROUP
    // ///////////////////////////////

    FormData fdSettingsComp = new FormData();
    fdSettingsComp.left = new FormAttachment( 0, 0 );
    fdSettingsComp.top = new FormAttachment( wStepname, 0 );
    fdSettingsComp.right = new FormAttachment( 100, 0 );
    fdSettingsComp.bottom = new FormAttachment( 100, 0 );
    wSettingsComp.setLayoutData( fdSettingsComp );

    wSettingsComp.layout();
    wSettingsTab.setControl( wSettingsComp );
    props.setLook( wSettingsComp );

    // ///////////////////////////////////////////////////////////
    // END OF Pop TAB
    // ///////////////////////////////////////////////////////////

    // ////////////////////////
    // START OF SEARCH TAB
    // ////////////////////////

    CTabItem wSearchTab = new CTabItem( wTabFolder, SWT.NONE );
    wSearchTab.setText( BaseMessages.getString( PKG, "MailInput.Tab.Search.Label" ) );
    Composite wSearchComp = new Composite( wTabFolder, SWT.NONE );
    props.setLook( wSearchComp );
    FormLayout searchLayout = new FormLayout();
    searchLayout.marginWidth = 3;
    searchLayout.marginHeight = 3;
    wSearchComp.setLayout( searchLayout );

    // ////////////////////////
    // START OF HEADER GROUP
    // ////////////////////////
    Group wHeader = new Group( wSearchComp, SWT.SHADOW_NONE );
    props.setLook( wHeader );
    wHeader.setText( BaseMessages.getString( PKG, "MailInput.Header.Group.Label" ) );

    FormLayout headerGroupLayout = new FormLayout();
    headerGroupLayout.marginWidth = 10;
    headerGroupLayout.marginHeight = 10;
    wHeader.setLayout( headerGroupLayout );

    wNegateSender = new Button( wHeader, SWT.CHECK );
    props.setLook( wNegateSender );
    FormData fdNegateSender = new FormData();
    wNegateSender.setToolTipText( BaseMessages.getString( PKG, "MailInput.NegateSender.Tooltip" ) );
    fdNegateSender.top = new FormAttachment( 0, margin );
    fdNegateSender.right = new FormAttachment( 100, -margin );
    wNegateSender.setLayoutData( fdNegateSender );

    // From line
    Label wlSender = new Label( wHeader, SWT.RIGHT );
    wlSender.setText( BaseMessages.getString( PKG, "MailInput.wSender.Label" ) );
    props.setLook( wlSender );
    FormData fdlSender = new FormData();
    fdlSender.left = new FormAttachment( 0, 0 );
    fdlSender.top = new FormAttachment( 0, margin );
    fdlSender.right = new FormAttachment( middle, -margin );
    wlSender.setLayoutData( fdlSender );
    wSender = new TextVar( transMeta, wHeader, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wSender );
    wSender.addModifyListener( lsMod );
    FormData fdSender = new FormData();
    fdSender.left = new FormAttachment( middle, 0 );
    fdSender.top = new FormAttachment( 0, margin );
    fdSender.right = new FormAttachment( wNegateSender, -margin );
    wSender.setLayoutData( fdSender );

    wNegateRecipient = new Button( wHeader, SWT.CHECK );
    props.setLook( wNegateRecipient );
    FormData fdNegateRecipient = new FormData();
    wNegateRecipient.setToolTipText( BaseMessages.getString( PKG, "MailInput.NegateReceipient.Tooltip" ) );
    fdNegateRecipient.top = new FormAttachment( wSender, margin );
    fdNegateRecipient.right = new FormAttachment( 100, -margin );
    wNegateRecipient.setLayoutData( fdNegateRecipient );

    // Recipient line
    Label wlRecipient = new Label( wHeader, SWT.RIGHT );
    wlRecipient.setText( BaseMessages.getString( PKG, "MailInput.Receipient.Label" ) );
    props.setLook( wlRecipient );
    FormData fdlRecipient = new FormData();
    fdlRecipient.left = new FormAttachment( 0, 0 );
    fdlRecipient.top = new FormAttachment( wSender, margin );
    fdlRecipient.right = new FormAttachment( middle, -margin );
    wlRecipient.setLayoutData( fdlRecipient );
    wRecipient = new TextVar( transMeta, wHeader, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wRecipient );
    wRecipient.addModifyListener( lsMod );
    FormData fdRecipient = new FormData();
    fdRecipient.left = new FormAttachment( middle, 0 );
    fdRecipient.top = new FormAttachment( wSender, margin );
    fdRecipient.right = new FormAttachment( wNegateRecipient, -margin );
    wRecipient.setLayoutData( fdRecipient );

    wNegateSubject = new Button( wHeader, SWT.CHECK );
    props.setLook( wNegateSubject );
    FormData fdNegateSubject = new FormData();
    wNegateSubject.setToolTipText( BaseMessages.getString( PKG, "MailInput.NegateSubject.Tooltip" ) );
    fdNegateSubject.top = new FormAttachment( wRecipient, margin );
    fdNegateSubject.right = new FormAttachment( 100, -margin );
    wNegateSubject.setLayoutData( fdNegateSubject );

    // Subject line
    Label wlSubject = new Label( wHeader, SWT.RIGHT );
    wlSubject.setText( BaseMessages.getString( PKG, "MailInput.Subject.Label" ) );
    props.setLook( wlSubject );
    FormData fdlSubject = new FormData();
    fdlSubject.left = new FormAttachment( 0, 0 );
    fdlSubject.top = new FormAttachment( wRecipient, margin );
    fdlSubject.right = new FormAttachment( middle, -margin );
    wlSubject.setLayoutData( fdlSubject );
    wSubject = new TextVar( transMeta, wHeader, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wSubject );
    wSubject.addModifyListener( lsMod );
    FormData fdSubject = new FormData();
    fdSubject.left = new FormAttachment( middle, 0 );
    fdSubject.top = new FormAttachment( wRecipient, margin );
    fdSubject.right = new FormAttachment( wNegateSubject, -margin );
    wSubject.setLayoutData( fdSubject );

    FormData fdHeader = new FormData();
    fdHeader.left = new FormAttachment( 0, margin );
    fdHeader.top = new FormAttachment( wRecipient, 2 * margin );
    fdHeader.right = new FormAttachment( 100, -margin );
    wHeader.setLayoutData( fdHeader );
    // ///////////////////////////////////////////////////////////
    // END OF HEADER GROUP
    // ///////////////////////////////////////////////////////////

    // ////////////////////////
    // START OF RECEIVED DATE GROUP
    // ////////////////////////
    Group wReceivedDate = new Group( wSearchComp, SWT.SHADOW_NONE );
    props.setLook( wReceivedDate );
    wReceivedDate.setText( BaseMessages.getString( PKG, "MailInput.ReceivedDate.Group.Label" ) );

    FormLayout receivedDateGroupLayout = new FormLayout();
    receivedDateGroupLayout.marginWidth = 10;
    receivedDateGroupLayout.marginHeight = 10;
    wReceivedDate.setLayout( receivedDateGroupLayout );

    wNegateReceivedDate = new Button( wReceivedDate, SWT.CHECK );
    props.setLook( wNegateReceivedDate );
    FormData fdNegateReceivedDate = new FormData();
    wNegateReceivedDate.setToolTipText( BaseMessages.getString( PKG, "MailInput.NegateReceivedDate.Tooltip" ) );
    fdNegateReceivedDate.top = new FormAttachment( wHeader, margin );
    fdNegateReceivedDate.right = new FormAttachment( 100, -margin );
    wNegateReceivedDate.setLayoutData( fdNegateReceivedDate );

    // Received Date Condition
    wlConditionOnReceivedDate = new Label( wReceivedDate, SWT.RIGHT );
    wlConditionOnReceivedDate.setText( BaseMessages.getString( PKG, "MailInput.ConditionOnReceivedDate.Label" ) );
    props.setLook( wlConditionOnReceivedDate );
    FormData fdlConditionOnReceivedDate = new FormData();
    fdlConditionOnReceivedDate.left = new FormAttachment( 0, 0 );
    fdlConditionOnReceivedDate.right = new FormAttachment( middle, -margin );
    fdlConditionOnReceivedDate.top = new FormAttachment( wHeader, margin );
    wlConditionOnReceivedDate.setLayoutData( fdlConditionOnReceivedDate );

    wConditionOnReceivedDate = new CCombo( wReceivedDate, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER );
    wConditionOnReceivedDate.setItems( MailConnectionMeta.conditionDateDesc );
    wConditionOnReceivedDate.select( 0 ); // +1: starts at -1

    props.setLook( wConditionOnReceivedDate );
    FormData fdConditionOnReceivedDate = new FormData();
    fdConditionOnReceivedDate.left = new FormAttachment( middle, 0 );
    fdConditionOnReceivedDate.top = new FormAttachment( wHeader, margin );
    fdConditionOnReceivedDate.right = new FormAttachment( wNegateReceivedDate, -margin );
    wConditionOnReceivedDate.setLayoutData( fdConditionOnReceivedDate );
    wConditionOnReceivedDate.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e ) {
        conditionReceivedDate();
        input.setChanged();
      }
    } );

    open = new Button( wReceivedDate, SWT.PUSH );
    open.setImage( GUIResource.getInstance().getImageCalendar() );
    open.setToolTipText( BaseMessages.getString( PKG, "MailInput.OpenCalendar" ) );
    FormData fdlButton = new FormData();
    fdlButton.top = new FormAttachment( wConditionOnReceivedDate, margin );
    fdlButton.right = new FormAttachment( 100, 0 );
    open.setLayoutData( fdlButton );
    open.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e ) {
        final Shell dialog = new Shell( shell, SWT.DIALOG_TRIM );
        dialog.setText( BaseMessages.getString( PKG, "MailInput.SelectDate" ) );
        dialog.setImage( GUIResource.getInstance().getImageSpoon() );
        dialog.setLayout( new GridLayout( 3, false ) );

        final DateTime calendar = new DateTime( dialog, SWT.CALENDAR );
        final DateTime time = new DateTime( dialog, SWT.TIME );
        new Label( dialog, SWT.NONE );
        new Label( dialog, SWT.NONE );

        Button ok = new Button( dialog, SWT.PUSH );
        ok.setText( BaseMessages.getString( PKG, "System.Button.OK" ) );
        ok.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, false, false ) );
        ok.addSelectionListener( new SelectionAdapter() {
          public void widgetSelected( SelectionEvent e ) {
            Calendar cal = Calendar.getInstance();
            cal.set( Calendar.YEAR, calendar.getYear() );
            cal.set( Calendar.MONTH, calendar.getMonth() );
            cal.set( Calendar.DAY_OF_MONTH, calendar.getDay() );

            cal.set( Calendar.HOUR_OF_DAY, time.getHours() );
            cal.set( Calendar.MINUTE, time.getMinutes() );
            cal.set( Calendar.SECOND, time.getSeconds() );

            wReadFrom.setText( new SimpleDateFormat( MailInputMeta.DATE_PATTERN ).format( cal.getTime() ) );

            dialog.close();
          }
        } );
        dialog.setDefaultButton( ok );
        dialog.pack();
        dialog.open();
      }
    } );

    wlReadFrom = new Label( wReceivedDate, SWT.RIGHT );
    wlReadFrom.setText( BaseMessages.getString( PKG, "MailInput.ReadFrom.Label" ) );
    props.setLook( wlReadFrom );
    FormData fdlReadFrom = new FormData();
    fdlReadFrom.left = new FormAttachment( 0, 0 );
    fdlReadFrom.top = new FormAttachment( wConditionOnReceivedDate, margin );
    fdlReadFrom.right = new FormAttachment( middle, -margin );
    wlReadFrom.setLayoutData( fdlReadFrom );
    wReadFrom = new TextVar( transMeta, wReceivedDate, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    wReadFrom.setToolTipText( BaseMessages.getString( PKG, "MailInput.ReadFrom.Tooltip" ) );
    props.setLook( wReadFrom );
    wReadFrom.addModifyListener( lsMod );
    FormData fdReadFrom = new FormData();
    fdReadFrom.left = new FormAttachment( middle, 0 );
    fdReadFrom.top = new FormAttachment( wConditionOnReceivedDate, margin );
    fdReadFrom.right = new FormAttachment( open, -margin );
    wReadFrom.setLayoutData( fdReadFrom );

    openTo = new Button( wReceivedDate, SWT.PUSH );
    openTo.setImage( GUIResource.getInstance().getImageCalendar() );
    openTo.setToolTipText( BaseMessages.getString( PKG, "MailInput.OpenCalendar" ) );
    FormData fdlButtonTo = new FormData();
    fdlButtonTo.top = new FormAttachment( wReadFrom, 2 * margin );
    fdlButtonTo.right = new FormAttachment( 100, 0 );
    openTo.setLayoutData( fdlButtonTo );
    openTo.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e ) {
        final Shell dialogTo = new Shell( shell, SWT.DIALOG_TRIM );
        dialogTo.setText( BaseMessages.getString( PKG, "MailInput.SelectDate" ) );
        dialogTo.setImage( GUIResource.getInstance().getImageSpoon() );
        dialogTo.setLayout( new GridLayout( 3, false ) );

        final DateTime calendarTo = new DateTime( dialogTo, SWT.CALENDAR | SWT.BORDER );
        final DateTime timeTo = new DateTime( dialogTo, SWT.TIME );
        new Label( dialogTo, SWT.NONE );
        new Label( dialogTo, SWT.NONE );
        Button okTo = new Button( dialogTo, SWT.PUSH );
        okTo.setText( BaseMessages.getString( PKG, "System.Button.OK" ) );
        okTo.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, false, false ) );
        okTo.addSelectionListener( new SelectionAdapter() {
          public void widgetSelected( SelectionEvent e ) {
            Calendar cal = Calendar.getInstance();
            cal.set( Calendar.YEAR, calendarTo.getYear() );
            cal.set( Calendar.MONTH, calendarTo.getMonth() );
            cal.set( Calendar.DAY_OF_MONTH, calendarTo.getDay() );

            cal.set( Calendar.HOUR_OF_DAY, timeTo.getHours() );
            cal.set( Calendar.MINUTE, timeTo.getMinutes() );
            cal.set( Calendar.SECOND, timeTo.getSeconds() );

            wReadTo.setText( new SimpleDateFormat( MailInputMeta.DATE_PATTERN ).format( cal.getTime() ) );

            dialogTo.close();
          }
        } );
        dialogTo.setDefaultButton( okTo );
        dialogTo.pack();
        dialogTo.open();
      }
    } );

    wlReadTo = new Label( wReceivedDate, SWT.RIGHT );
    wlReadTo.setText( BaseMessages.getString( PKG, "MailInput.ReadTo.Label" ) );
    props.setLook( wlReadTo );
    FormData fdlReadTo = new FormData();
    fdlReadTo.left = new FormAttachment( 0, 0 );
    fdlReadTo.top = new FormAttachment( wReadFrom, 2 * margin );
    fdlReadTo.right = new FormAttachment( middle, -margin );
    wlReadTo.setLayoutData( fdlReadTo );
    wReadTo = new TextVar( transMeta, wReceivedDate, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    wReadTo.setToolTipText( BaseMessages.getString( PKG, "MailInput.ReadTo.Tooltip" ) );
    props.setLook( wReadTo );
    wReadTo.addModifyListener( lsMod );
    FormData fdReadTo = new FormData();
    fdReadTo.left = new FormAttachment( middle, 0 );
    fdReadTo.top = new FormAttachment( wReadFrom, 2 * margin );
    fdReadTo.right = new FormAttachment( openTo, -margin );
    wReadTo.setLayoutData( fdReadTo );

    FormData fdReceivedDate = new FormData();
    fdReceivedDate.left = new FormAttachment( 0, margin );
    fdReceivedDate.top = new FormAttachment( wHeader, margin );
    fdReceivedDate.right = new FormAttachment( 100, -margin );
    wReceivedDate.setLayoutData( fdReceivedDate );
    // ///////////////////////////////////////////////////////////
    // END OF RECEIVED DATE GROUP
    // ///////////////////////////////////////////////////////////

    wlLimit = new Label( wSearchComp, SWT.RIGHT );
    wlLimit.setText( BaseMessages.getString( PKG, "MailInput.Limit.Label" ) );
    props.setLook( wlLimit );
    FormData fdlLimit = new FormData();
    fdlLimit.left = new FormAttachment( 0, 0 );
    fdlLimit.top = new FormAttachment( wReceivedDate, 2 * margin );
    fdlLimit.right = new FormAttachment( middle, -margin );
    wlLimit.setLayoutData( fdlLimit );
    wLimit = new Text( wSearchComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wLimit );
    wLimit.addModifyListener( lsMod );
    FormData fdLimit = new FormData();
    fdLimit.left = new FormAttachment( middle, 0 );
    fdLimit.top = new FormAttachment( wReceivedDate, 2 * margin );
    fdLimit.right = new FormAttachment( 100, 0 );
    wLimit.setLayoutData( fdLimit );

    FormData fdSearchComp = new FormData();
    fdSearchComp.left = new FormAttachment( 0, 0 );
    fdSearchComp.top = new FormAttachment( wStepname, 0 );
    fdSearchComp.right = new FormAttachment( 100, 0 );
    fdSearchComp.bottom = new FormAttachment( 100, 0 );
    wSearchComp.setLayoutData( fdSearchComp );

    wSearchComp.layout();
    wSearchTab.setControl( wSearchComp );
    props.setLook( wSearchComp );

    // ////////////////////////////////
    // END OF SEARCH TAB
    // ////////////////////////////////

    // Fields tab...
    //
    CTabItem wFieldsTab = new CTabItem( wTabFolder, SWT.NONE );
    wFieldsTab.setText( BaseMessages.getString( PKG, "MailInputdialog.Fields.Tab" ) );

    FormLayout fieldsLayout = new FormLayout();
    fieldsLayout.marginWidth = Const.FORM_MARGIN;
    fieldsLayout.marginHeight = Const.FORM_MARGIN;

    Composite wFieldsComp = new Composite( wTabFolder, SWT.NONE );
    wFieldsComp.setLayout( fieldsLayout );
    props.setLook( wFieldsComp );

    wGet = new Button( wFieldsComp, SWT.PUSH );
    wGet.setText( BaseMessages.getString( PKG, "MailInputdialog.GetFields.Button" ) );
    fdGet = new FormData();
    fdGet.left = new FormAttachment( 50, 0 );
    fdGet.bottom = new FormAttachment( 100, 0 );
    wGet.setLayoutData( fdGet );

    final int FieldsRows = input.getInputFields().length;

    ColumnInfo[] colInf =
      new ColumnInfo[] {
        new ColumnInfo(
          BaseMessages.getString( PKG, "MailInputdialog.FieldsTable.Name.Column" ),
          ColumnInfo.COLUMN_TYPE_TEXT, false ),
        new ColumnInfo(
          BaseMessages.getString( PKG, "MailInputdialog.FieldsTable.Column.Column" ),
          ColumnInfo.COLUMN_TYPE_CCOMBO, MailInputField.ColumnDesc, true ), };

    colInf[ 0 ].setUsingVariables( true );
    colInf[ 0 ].setToolTip( BaseMessages.getString( PKG, "MailInputdialog.FieldsTable.Name.Column.Tooltip" ) );
    colInf[ 1 ].setToolTip( BaseMessages.getString( PKG, "MailInputdialog.FieldsTable.Column.Column.Tooltip" ) );

    wFields =
      new TableView( transMeta, wFieldsComp, SWT.FULL_SELECTION | SWT.MULTI, colInf, FieldsRows, lsMod, props );

    FormData fdFields = new FormData();
    fdFields.left = new FormAttachment( 0, 0 );
    fdFields.top = new FormAttachment( 0, 0 );
    fdFields.right = new FormAttachment( 100, 0 );
    fdFields.bottom = new FormAttachment( wGet, -margin );
    wFields.setLayoutData( fdFields );

    FormData fdFieldsComp = new FormData();
    fdFieldsComp.left = new FormAttachment( 0, 0 );
    fdFieldsComp.top = new FormAttachment( 0, 0 );
    fdFieldsComp.right = new FormAttachment( 100, 0 );
    fdFieldsComp.bottom = new FormAttachment( 100, 0 );
    wFieldsComp.setLayoutData( fdFieldsComp );

    wFieldsComp.layout();
    wFieldsTab.setControl( wFieldsComp );

    FormData fdTabFolder = new FormData();
    fdTabFolder.left = new FormAttachment( 0, 0 );
    fdTabFolder.top = new FormAttachment( wStepname, margin );
    fdTabFolder.right = new FormAttachment( 100, 0 );
    fdTabFolder.bottom = new FormAttachment( 100, -50 );
    wTabFolder.setLayoutData( fdTabFolder );

    wOK = new Button( shell, SWT.PUSH );
    wOK.setText( BaseMessages.getString( PKG, "System.Button.OK" ) );
    wCancel = new Button( shell, SWT.PUSH );
    wPreview = new Button( shell, SWT.PUSH );
    wPreview.setText( BaseMessages.getString( PKG, "MailInputDialog.Preview" ) );

    wCancel.setText( BaseMessages.getString( PKG, "System.Button.Cancel" ) );

    BaseStepDialog.positionBottomButtons( shell, new Button[] { wOK, wPreview, wCancel }, margin, wTabFolder );
    // Add listeners
    lsGet = e -> getFields();
    lsCancel = e -> cancel();
    lsOK = e -> ok();
    lsPreview = e -> preview();
    wCancel.addListener( SWT.Selection, lsCancel );
    wOK.addListener( SWT.Selection, lsOK );

    lsDef = new SelectionAdapter() {
      public void widgetDefaultSelected( SelectionEvent e ) {
        ok();
      }
    };
    Listener lsTest = e -> test();
    wTest.addListener( SWT.Selection, lsTest );
    wPreview.addListener( SWT.Selection, lsPreview );
    wGet.addListener( SWT.Selection, lsGet );
    wTestIMAPFolder.addListener( SWT.Selection,
      e1 -> checkFolder( transMeta.environmentSubstitute( wIMAPFolder.getText() ) ) );

    wSelectFolder.addListener( SWT.Selection, e -> selectFolder( wIMAPFolder ) );

    wStepname.addSelectionListener( lsDef );
    wServerName.addSelectionListener( lsDef );

    // Detect X or ALT-F4 or something that kills this window...
    shell.addShellListener( new ShellAdapter() {
      public void shellClosed( ShellEvent e ) {
        cancel();
      }
    } );

    getData();
    setUseAuth();
    setUseGrantType();
    setUserProxy();
    chooseListMails();
    refreshProtocol( false );
    conditionReceivedDate();
    wTabFolder.setSelection( 0 );
    BaseStepDialog.setSize( shell );

    shell.open();

    shell.open();
    while ( !shell.isDisposed() ) {
      if ( !display.readAndDispatch() ) {
        display.sleep();
      }
    }
    return stepname;
  }

  private void activeDynamicFolder() {
    wlFolderField.setEnabled( wDynamicFolder.getSelection() );
    wFolderField.setEnabled( wDynamicFolder.getSelection() );
    wlIMAPFolder.setEnabled( !wDynamicFolder.getSelection() );
    wIMAPFolder.setEnabled( !wDynamicFolder.getSelection() );
    wPreview.setEnabled( !wDynamicFolder.getSelection() );
    if ( wDynamicFolder.getSelection() ) {
      wLimit.setText( "0" );
    }
    wlLimit.setEnabled( !wDynamicFolder.getSelection() );
    wLimit.setEnabled( !wDynamicFolder.getSelection() );
    boolean activePOP3 = wProtocol.getText().equals( MailConnectionMeta.PROTOCOL_STRING_POP3 );
    wlIMAPFolder.setEnabled( !wDynamicFolder.getSelection() && !activePOP3 );
    wIMAPFolder.setEnabled( !wDynamicFolder.getSelection() && !activePOP3 );
    wTestIMAPFolder.setEnabled( !wDynamicFolder.getSelection() && !activePOP3 );
    wSelectFolder.setEnabled( !wDynamicFolder.getSelection() && !activePOP3 );
  }

  /**
   * Copy information from the meta-data input to the dialog fields.
   */
  public void getData() {
    if ( input.getServerName() != null ) {
      wServerName.setText( input.getServerName() );
    }
    if ( input.getUserName() != null ) {
      wUserName.setText( input.getUserName() );
    }
    if ( input.getPassword() != null ) {
      wPassword.setText( input.getPassword() );
    }

    wUseSSL.setSelection( input.isUseSSL() );

    if ( input.getPort() != null ) {
      wPort.setText( input.getPort() );
    }
    if ( input.isUsingAuthentication() != null ) {
      wUseAuth.setText( input.isUsingAuthentication() );
    }

    if ( input.getClientId() != null ) {
      wAuthClientId.setText( input.getClientId() );
    }
    if ( input.getSecretKey() != null ) {
      wAuthSecretKey.setText( input.getSecretKey() );
    }
    if ( input.getScope() != null ) {
      wAuthScope.setText( input.getScope() );
    }
    if ( input.getTokenUrl() != null ) {
      wAuthTokenUrl.setText( input.getTokenUrl() );
    }
    if ( input.getAuthorization_code() != null ) {
      wAuthorizationCode.setText( input.getAuthorization_code() );
    }
    if ( input.getRedirectUri() != null ) {
      wRedirectUri.setText( input.getRedirectUri() );
    }
    if ( input.getRefresh_token() != null ) {
      wAuthRefreshToken.setText( input.getRefresh_token() );
    }
    if ( input.getGrant_type() != null ) {
      grantType.setText( input.getGrant_type() );
    }

    String protocol = input.getProtocol();

    boolean isPop3 = MailConnectionMeta.PROTOCOL_STRING_POP3.equals( protocol );
    wProtocol.setText( protocol );
    int iRet = input.getRetrievemails();

    // [PDI-7241] POP3 does not support retrieve email flags.
    // if anyone already used 'unread' for POP3 in transformation or 'retrieve... first'
    // now they realize that all this time it was 'retrieve all mails'.
    if ( iRet > 0 ) {
      if ( isPop3 ) {
        wListMails.select( iRet - 1 );
      } else {
        wListMails.select( iRet );
      }
    } else {
      wListMails.select( 0 ); // Retrieve All Mails
    }

    if ( input.getFirstMails() != null ) {
      wFirstMails.setText( input.getFirstMails() );
    }

    wIMAPListMails.setText( MailConnectionMeta.getValueImapListDesc( input.getValueImapList() ) );
    if ( input.getFirstIMAPMails() != null ) {
      wIMAPFirstMails.setText( input.getFirstIMAPMails() );
    }
    if ( input.getIMAPFolder() != null ) {
      wIMAPFolder.setText( input.getIMAPFolder() );
    }
    // search term
    if ( input.getSenderSearchTerm() != null ) {
      wSender.setText( input.getSenderSearchTerm() );
    }
    wNegateSender.setSelection( input.isNotTermSenderSearch() );
    if ( input.getRecipientSearch() != null ) {
      wRecipient.setText( input.getRecipientSearch() );
    }
    wNegateRecipient.setSelection( input.isNotTermRecipientSearch() );
    if ( input.getSubjectSearch() != null ) {
      wSubject.setText( input.getSubjectSearch() );
    }
    wNegateSubject.setSelection( input.isNotTermSubjectSearch() );
    wConditionOnReceivedDate
      .setText( MailConnectionMeta.getConditionDateDesc( input.getConditionOnReceivedDate() ) );
    wNegateReceivedDate.setSelection( input.isNotTermReceivedDateSearch() );
    if ( input.getReceivedDate1() != null ) {
      wReadFrom.setText( input.getReceivedDate1() );
    }
    if ( input.getReceivedDate2() != null ) {
      wReadTo.setText( input.getReceivedDate2() );
    }
    wIncludeSubFolders.setSelection( input.isIncludeSubFolders() );
    wUseProxy.setSelection( input.isUseProxy() );
    if ( input.getProxyUsername() != null ) {
      wProxyUsername.setText( input.getProxyUsername() );
    }
    wDynamicFolder.setSelection( input.isDynamicFolder() );
    if ( input.getFolderField() != null ) {
      wFolderField.setText( input.getFolderField() );
    }
    wLimit.setText( Const.NVL( input.getRowLimit(), "0" ) );
    for ( int i = 0; i < input.getInputFields().length; i++ ) {
      MailInputField field = input.getInputFields()[ i ];

      if ( field != null ) {
        TableItem item = wFields.table.getItem( i );
        String name = field.getName();
        String column = field.getColumnDesc();
        if ( name != null ) {
          item.setText( 1, name );
        }
        if ( column != null ) {
          item.setText( 2, column );
        }
      }
    }

    wFields.removeEmptyRows();
    wFields.setRowNums();
    wFields.optWidth( true );

    wUseBatch.setSelection( input.isUseBatch() );
    wBatchSize.setText(
      input.getBatchSize() == null
        ? String.valueOf( MailInputMeta.DEFAULT_BATCH_SIZE )
        : input.getBatchSize().toString() );
    wStartMessage.setText( Const.NVL( input.getStart(), "" ) );
    wEndMessage.setText( Const.NVL( input.getEnd(), "" ) );
    wIgnoreFieldErrors.setSelection( input.isStopOnError() );
    setBatchSettingsEnabled();

    wStepname.selectAll();
    wStepname.setFocus();
  }

  private void cancel() {
    stepname = null;
    input.setChanged( changed );
    dispose();
  }

  private void ok() {
    if ( Utils.isEmpty( wStepname.getText() ) ) {
      return;
    }

    try {
      getInfo( input );
    } catch ( KettleException e ) {
      new ErrorDialog(
        shell, BaseMessages.getString( PKG, "MailInputDialog.ErrorParsingData.DialogTitle" ), BaseMessages
          .getString( PKG, "MailInputDialog.ErrorParsingData.DialogMessage" ), e );
    }
    dispose();
  }

  private void getInfo( MailInputMeta in ) throws KettleException {
    stepname = wStepname.getText(); // return value

    in.setServerName( wServerName.getText() );
    in.setUserName( wUserName.getText() );
    in.setPassword( wPassword.getText() );
    in.setUseSSL( wUseSSL.getSelection() );
    in.setPort( wPort.getText() );
    in.setClientId( wAuthClientId.getText() );
    in.setSecretKey( wAuthSecretKey.getText() );
    in.setScope( wAuthScope.getText() );
    in.setTokenUrl( wAuthTokenUrl.getText() );
    in.setAuthorization_code( wAuthorizationCode.getText() );
    in.setRedirectUri( wRedirectUri.getText() );
    in.setRefresh_token( wAuthRefreshToken.getText() );
    in.setUsingAuthentication( wUseAuth.getText() );
    in.setGrant_type( grantType.getText() );

    // [PDI-7241] Option 'retrieve unread' is removed and there is only 2 options.
    // for backward compatibility: 0 is 'retrieve all', 2 is 'retrieve first...'
    int actualIndex = wListMails.getSelectionIndex();
    in.setRetrievemails( actualIndex > 0 ? 2 : 0 );

    //Set first... emails for POP3
    in.setFirstMails( wFirstMails.getText() );
    in.setProtocol( wProtocol.getText() );
    in.setValueImapList( MailConnectionMeta.getValueImapListByDesc( wIMAPListMails.getText() ) );
    //Set first... emails for IMAP
    in.setFirstIMAPMails( wIMAPFirstMails.getText() );
    in.setIMAPFolder( wIMAPFolder.getText() );
    // search term
    in.setSenderSearchTerm( wSender.getText() );
    in.setNotTermSenderSearch( wNegateSender.getSelection() );

    in.setRecipientSearch( wRecipient.getText() );
    in.setNotTermRecipientSearch( wNegateRecipient.getSelection() );
    in.setSubjectSearch( wSubject.getText() );
    in.setNotTermSubjectSearch( wNegateSubject.getSelection() );
    in.setConditionOnReceivedDate( MailConnectionMeta.getConditionDateByDesc( wConditionOnReceivedDate.getText() ) );
    in.setNotTermReceivedDateSearch( wNegateReceivedDate.getSelection() );
    in.setReceivedDate1( wReadFrom.getText() );
    in.setReceivedDate2( wReadTo.getText() );
    in.setIncludeSubFolders( wIncludeSubFolders.getSelection() );
    in.setUseProxy( wUseProxy.getSelection() );
    in.setProxyUsername( wProxyUsername.getText() );
    in.setDynamicFolder( wDynamicFolder.getSelection() );
    in.setFolderField( wFolderField.getText() );
    in.setRowLimit( wLimit.getText() );
    int nrFields = wFields.nrNonEmpty();
    in.allocate( nrFields );
    for ( int i = 0; i < nrFields; i++ ) {
      MailInputField field = new MailInputField();

      TableItem item = wFields.getNonEmpty( i );

      field.setName( item.getText( 1 ) );
      field.setColumn( MailInputField.getColumnByDesc( item.getText( 2 ) ) );
      in.getInputFields()[ i ] = field;
    }

    in.setUseBatch( wUseBatch.getSelection() );
    Integer batchSize = getInteger( wBatchSize.getText() );
    in.setBatchSize( Const.NVL( batchSize, MailInputMeta.DEFAULT_BATCH_SIZE ) );
    in.setStart( wStartMessage.getText() );
    in.setEnd( wEndMessage.getText() );
    in.setStopOnError( wIgnoreFieldErrors.getSelection() );
  }

  private void setFolderField() {
    if ( !gotPreviousFields ) {
      try {
        String field = wFolderField.getText();
        wFolderField.removeAll();

        RowMetaInterface r = transMeta.getPrevStepFields( stepname );
        if ( r != null ) {
          wFolderField.setItems( r.getFieldNames() );
        }
        if ( field != null ) {
          wFolderField.setText( field );
        }
      } catch ( KettleException ke ) {
        new ErrorDialog(
          shell, BaseMessages.getString( PKG, "MailInput.FailedToGetFields.DialogTitle" ), BaseMessages
            .getString( PKG, "MailInput.FailedToGetFields.DialogMessage" ), ke );
      }
      gotPreviousFields = true;
    }
  }

  private void closeMailConnection() {
    try {
      if ( mailConn != null ) {
        mailConn.disconnect();
        mailConn = null;
      }
    } catch ( Exception e ) { /* Ignore */
    }
  }

  private void conditionReceivedDate() {
    boolean activeReceivedDate =
      !( MailConnectionMeta.getConditionDateByDesc( wConditionOnReceivedDate.getText() )
      == MailConnectionMeta.CONDITION_DATE_IGNORE );
    boolean useBetween =
      ( MailConnectionMeta.getConditionDateByDesc( wConditionOnReceivedDate.getText() )
      == MailConnectionMeta.CONDITION_DATE_BETWEEN );
    wlReadFrom.setVisible( activeReceivedDate );
    wReadFrom.setVisible( activeReceivedDate );
    open.setVisible( activeReceivedDate );
    wlReadTo.setVisible( activeReceivedDate && useBetween );
    wReadTo.setVisible( activeReceivedDate && useBetween );
    openTo.setVisible( activeReceivedDate && useBetween );
    if ( !activeReceivedDate ) {
      wReadFrom.setText( "" );
      wReadTo.setText( "" );
      wNegateReceivedDate.setSelection( false );
    }
  }

  private void refreshPort( boolean refreshPort ) {
    if ( refreshPort ) {
      if ( wProtocol.getText().equals( MailConnectionMeta.PROTOCOL_STRING_POP3 ) ) {
        if ( wUseSSL.getSelection() ) {
          if ( Utils.isEmpty( wPort.getText() )
            || wPort.getText().equals( "" + MailConnectionMeta.DEFAULT_SSL_IMAP_PORT ) ) {
            wPort.setText( "" + MailConnectionMeta.DEFAULT_SSL_POP3_PORT );
          }
        } else {
          if ( Utils.isEmpty( wPort.getText() ) || wPort.getText()
            .equals( "" + MailConnectionMeta.DEFAULT_IMAP_PORT ) ) {
            wPort.setText( "" + MailConnectionMeta.DEFAULT_POP3_PORT );
          }
        }
      } else if ( wProtocol.getText().equals( MailConnectionMeta.PROTOCOL_STRING_IMAP ) ) {
        if ( wUseSSL.getSelection() ) {
          if ( Utils.isEmpty( wPort.getText() )
            || wPort.getText().equals( "" + MailConnectionMeta.DEFAULT_SSL_POP3_PORT ) ) {
            wPort.setText( "" + MailConnectionMeta.DEFAULT_SSL_IMAP_PORT );
          }
        } else {
          if ( Utils.isEmpty( wPort.getText() ) || wPort.getText()
            .equals( "" + MailConnectionMeta.DEFAULT_POP3_PORT ) ) {
            wPort.setText( "" + MailConnectionMeta.DEFAULT_IMAP_PORT );
          }
        }
      } else {
        wPort.setText( "" );
      }
    }
  }

  private void refreshProtocol( boolean refreshPort ) {
    boolean activePOP3 = MailConnectionMeta.PROTOCOL_STRING_POP3.equals( wProtocol.getText() );
    boolean activeIMAP = MailConnectionMeta.PROTOCOL_STRING_IMAP.equals( wProtocol.getText() );

    wlPOP3Message.setEnabled( activePOP3 );
    wlListMails.setEnabled( activePOP3 );
    wListMails.setEnabled( activePOP3 );
    wlFirstMails.setEnabled( activePOP3 );

    wlIMAPFirstMails.setEnabled( activeIMAP );
    wIMAPFirstMails.setEnabled( activeIMAP );

    wlIncludeSubFolders.setEnabled( !activePOP3 );
    wIncludeSubFolders.setEnabled( !activePOP3 );
    wlIMAPListMails.setEnabled( activeIMAP );
    wIMAPListMails.setEnabled( activeIMAP );
    if ( activePOP3 && wDynamicFolder.getSelection() ) {
      wDynamicFolder.setSelection( false );
    }
    wlDynamicFolder.setEnabled( !activePOP3 );
    wDynamicFolder.setEnabled( !activePOP3 );

    if ( activePOP3 ) {
      // clear out selections
      wConditionOnReceivedDate.select( 0 );
      conditionReceivedDate();
    }
    // POP3/MBOX protocols do not provide information about when a message was received
    wConditionOnReceivedDate.setEnabled( activeIMAP );
    wNegateReceivedDate.setEnabled( activeIMAP );
    wlConditionOnReceivedDate.setEnabled( activeIMAP );

    setRemoteOptionsEnabled( activePOP3 || activeIMAP );

    activeDynamicFolder();
    chooseListMails();
    refreshPort( refreshPort );
  }

  private void setRemoteOptionsEnabled( boolean enableRemoteOpts ) {
    wlUserName.setEnabled( enableRemoteOpts );
    wUserName.setEnabled( enableRemoteOpts );
    wPort.setEnabled( enableRemoteOpts );
    wlPort.setEnabled( enableRemoteOpts );
    wPassword.setEnabled( enableRemoteOpts );
    wlPassword.setEnabled( enableRemoteOpts );
    if ( !enableRemoteOpts && wUseProxy.getSelection() ) {
      wUseProxy.setSelection( false );
      setUserProxy();
    }
    wUseProxy.setEnabled( enableRemoteOpts );
    wlUseProxy.setEnabled( enableRemoteOpts );
    wUseSSL.setEnabled( enableRemoteOpts );
    wlUseSSL.setEnabled( enableRemoteOpts );
  }

  public void dispose() {
    closeMailConnection();
    props.setScreen( new WindowProperty( shell ) );
    shell.dispose();
  }

  public void chooseListMails() {
    boolean ok =
      ( wProtocol.getText().equals( MailConnectionMeta.PROTOCOL_STRING_POP3 ) && wListMails.getSelectionIndex() == 1 );
    wlFirstMails.setEnabled( ok );
    wFirstMails.setEnabled( ok );
  }

  private void selectFolder( TextVar input ) {
    if ( connect() ) {
      try {
        Folder folder = mailConn.getStore().getDefaultFolder();
        SelectFolderDialog s = new SelectFolderDialog( shell, SWT.NONE, folder );
        String folderName = s.open();
        if ( folderName != null ) {
          input.setText( folderName );
        }
      } catch ( Exception e ) {
        // Ignore errors
      }
    }
  }

  private boolean connect() {
    String errorDescription = null;
    boolean retval = false;
    if ( mailConn != null && mailConn.isConnected() ) {
      retval = mailConn.isConnected();
    }

    if ( !retval ) {
      String realServer = transMeta.environmentSubstitute( wServerName.getText() );
      String realUser = transMeta.environmentSubstitute( wUserName.getText() );
      String realPass = transMeta.environmentSubstitute( wPassword.getText() );
      String realProxyUsername = transMeta.environmentSubstitute( wProxyUsername.getText() );
      int realPort = Const.toInt( transMeta.environmentSubstitute( wPort.getText() ), -1 );

      String tokenUrl = transMeta.environmentSubstitute( wAuthTokenUrl.getText() );
      String scope = transMeta.environmentSubstitute( wAuthScope.getText() );
      String clientId = transMeta.environmentSubstitute( wAuthClientId.getText() );
      String secretKey = transMeta.environmentSubstitute( wAuthSecretKey.getText() );
      String grantType = transMeta.environmentSubstitute( input.getGrant_type() );
      String refreshToken = transMeta.environmentSubstitute( wAuthRefreshToken.getText() );
      String authorizationCode = transMeta.environmentSubstitute( wAuthorizationCode.getText() );
      String redirectUri = transMeta.environmentSubstitute( wRedirectUri.getText() );

      if( wUseAuth.getText().equals( MailInputMeta.AUTENTICATION_OAUTH ) ){
        realPass = "Bearer "+ input.getOauthToken(tokenUrl,scope,clientId,secretKey,
                grantType, refreshToken, authorizationCode, redirectUri).getAccessToken();
      }

      try {
        mailConn =
          new MailConnection( transMeta.getBowl(),
            LogChannel.UI, MailConnectionMeta.getProtocolFromString(
              wProtocol.getText(), MailConnectionMeta.PROTOCOL_IMAP ), realServer, realPort, realUser,
            realPass, wUseSSL.getSelection(), wUseProxy.getSelection(), realProxyUsername );
        mailConn.connect();

        retval = true;
      } catch ( Exception e ) {
        errorDescription = e.getMessage();
      }
    }

    if ( !retval ) {
      MessageBox mb = new MessageBox( shell, SWT.OK | SWT.ICON_ERROR );
      mb.setMessage( BaseMessages.getString( PKG, "MailInput.Connected.NOK.ConnectionBad", wServerName.getText() )
        + Const.CR + Const.NVL( errorDescription, "" ) );
      mb.setText( BaseMessages.getString( PKG, "MailInput.Connected.Title.Bad" ) );
      mb.open();
    }

    return ( mailConn.isConnected() );
  }

  private void test() {
    if ( connect() ) {
      MessageBox mb = new MessageBox( shell, SWT.OK | SWT.ICON_INFORMATION );
      mb.setMessage( BaseMessages.getString( PKG, "MailInput.Connected.OK", transMeta.environmentSubstitute( wServerName.getText() ) )+ Const.CR );
      mb.setText( BaseMessages.getString( PKG, "MailInput.Connected.Title.Ok" ) );
      mb.open();
    }
  }

  private void checkFolder( String folderName ) {
    if ( !Utils.isEmpty( folderName ) ) {
      if ( connect() ) {
        // check folder
        if ( mailConn.folderExists( folderName ) ) {
          MessageBox mb = new MessageBox( shell, SWT.OK | SWT.ICON_INFORMATION );
          mb.setMessage( BaseMessages.getString( PKG, "MailInput.IMAPFolderExists.OK", folderName ) + Const.CR );
          mb.setText( BaseMessages.getString( PKG, "MailInput.IMAPFolderExists.Title.Ok" ) );
          mb.open();
        } else {
          MessageBox mb = new MessageBox( shell, SWT.OK | SWT.ICON_ERROR );
          mb.setMessage( BaseMessages.getString( PKG, "MailInput.Connected.NOK.IMAPFolderExists", folderName )
            + Const.CR );
          mb.setText( BaseMessages.getString( PKG, "MailInput.IMAPFolderExists.Title.Bad" ) );
          mb.open();
        }
      }
    }
  }

  // Preview the data
  private void preview() {
    try {
      MailInputMeta oneMeta = new MailInputMeta();
      getInfo( oneMeta );

      TransMeta previewMeta =
        TransPreviewFactory.generatePreviewTransformation( transMeta, oneMeta, wStepname.getText() );

      EnterNumberDialog numberDialog = new EnterNumberDialog( shell, props.getDefaultPreviewSize(),
        BaseMessages.getString( PKG, "MailInputDialog.NumberRows.DialogTitle" ),
        BaseMessages.getString( PKG, "MailInputDialog.NumberRows.DialogMessage" ) );

      int previewSize = numberDialog.open();
      if ( previewSize > 0 ) {
        TransPreviewProgressDialog progressDialog =
          new TransPreviewProgressDialog(
            shell, previewMeta, new String[] { wStepname.getText() }, new int[] { previewSize } );
        progressDialog.open();

        if ( !progressDialog.isCancelled() ) {
          Trans trans = progressDialog.getTrans();
          String loggingText = progressDialog.getLoggingText();

          if ( trans.getResult() != null && trans.getResult().getNrErrors() > 0 ) {
            EnterTextDialog etd = new EnterTextDialog( shell,
              BaseMessages.getString( PKG, "System.Dialog.PreviewError.Title" ),
              BaseMessages.getString( PKG, "System.Dialog.PreviewError.Message" ), loggingText, true );
            etd.setReadOnly();
            etd.open();
          }
          PreviewRowsDialog prd =
            new PreviewRowsDialog(
              shell, transMeta, SWT.NONE, wStepname.getText(), progressDialog.getPreviewRowsMeta( wStepname
                .getText() ), progressDialog.getPreviewRows( wStepname.getText() ), loggingText );
          prd.open();

        }
      }
    } catch ( KettleException e ) {
      new ErrorDialog(
        shell, BaseMessages.getString( PKG, "MailInputDialog.ErrorPreviewingData.DialogTitle" ), BaseMessages
          .getString( PKG, "MailInputDialog.ErrorPreviewingData.DialogMessage" ), e );
    }
  }

  private void setUserProxy() {
    wlProxyUsername.setEnabled( wUseProxy.getSelection() );
    wProxyUsername.setEnabled( wUseProxy.getSelection() );
  }

  private void getFields() {

    // Clear Fields Grid
    wFields.removeAll();
    for ( int i = 0; i < MailInputField.ColumnDesc.length; i++ ) {
      wFields.add( MailInputField.ColumnDesc[ i ], MailInputField.ColumnDesc[ i ] );
    }

    wFields.removeEmptyRows();
    wFields.setRowNums();
    wFields.optWidth( true );
  }

  private void setBatchSettingsEnabled() {
    boolean enabled = wUseBatch.getSelection();
    wBatchSize.setEnabled( enabled );
    wStartMessage.setEnabled( enabled );
    wEndMessage.setEnabled( enabled );
  }

  private Group createGroup( Composite parentTab, Control top, String label ) {
    Group group = new Group( parentTab, SWT.SHADOW_NONE );
    props.setLook( group );
    group.setText( label );

    FormLayout groupLayout = new FormLayout();
    groupLayout.marginWidth = 10;
    groupLayout.marginHeight = 10;
    group.setLayout( groupLayout );

    FormData fdGroup = new FormData();
    fdGroup.left = new FormAttachment( 0, Const.MARGIN );
    fdGroup.top = new FormAttachment( top, Const.MARGIN );
    fdGroup.right = new FormAttachment( 100, -Const.MARGIN );
    group.setLayoutData( fdGroup );

    return group;
  }

  private void addLabelInputPairBelow( Control label, Control input, Control widgetAbove ) {
    addLabelBelow( label, widgetAbove );
    addControlBelow( input, widgetAbove );
  }

  private void addLabelBelow( Control label, Control widgetAbove ) {
    props.setLook( label );
    FormData fData = new FormData();
    fData.top = new FormAttachment( widgetAbove, Const.MARGIN );
    fData.right = new FormAttachment( Const.MIDDLE_PCT, -Const.MARGIN );
    label.setLayoutData( fData );
  }

  protected void setUseAuth() {
    String selectedAuth = wUseAuth.getText();

    if ( MailInputMeta.AUTENTICATION_OAUTH.equals( selectedAuth ) ) {
      wAuthClientId.setEnabled( true );
      wAuthSecretKey.setEnabled( true );
      wUserName.setEnabled( true );
      wPassword.setEnabled( false );
      wAuthScope.setEnabled( true );
      wlGrantType.setEnabled( true );
      grantType.setEnabled( true );
    } else {
      // MailInputMeta.AUTENTICATION_BASIC
      wAuthClientId.setEnabled( false );
      wAuthSecretKey.setEnabled( false );
      wUserName.setEnabled( true );
      wPassword.setEnabled( true );
      wAuthScope.setEnabled( false );
      wlGrantType.setEnabled( false );
      grantType.setEnabled( false );
      wAuthTokenUrl.setEnabled( false );
      wAuthorizationCode.setEnabled( false );
      wRedirectUri.setEnabled( false );
      wAuthRefreshToken.setEnabled( false );
    }
  }

  protected void setUseGrantType() {
    String selectedAuth = grantType.getText();

    if ( selectedAuth.equals( MailInputMeta.GRANTTYPE_CLIENTCREDENTIALS ) ) {
      wAuthTokenUrl.setEnabled( true );
      wAuthorizationCode.setEnabled( false );
      wRedirectUri.setEnabled( false );
      wAuthRefreshToken.setEnabled( false );
    } else if ( selectedAuth.equals( MailInputMeta.GRANTTYPE_REFRESH_TOKEN ) ) {
      wAuthTokenUrl.setEnabled( true );
      wAuthorizationCode.setEnabled( false );
      wRedirectUri.setEnabled( false );
      wAuthRefreshToken.setEnabled( true );
    } else if ( selectedAuth.equals( MailInputMeta.GRANTTYPE_AUTHORIZATION_CODE ) ) {
      wAuthTokenUrl.setEnabled( true );
      wAuthorizationCode.setEnabled( true );
      wRedirectUri.setEnabled( true );
      wAuthRefreshToken.setEnabled( false );
    }
  }

  private void addControlBelow( Control control, Control widgetAbove ) {
    props.setLook( control );
    FormData fData = new FormData();
    fData.top = new FormAttachment( widgetAbove, Const.MARGIN );
    fData.left = new FormAttachment( Const.MIDDLE_PCT, Const.MARGIN );
    fData.right = new FormAttachment( 100, -Const.MARGIN );
    control.setLayoutData( fData );
  }

  private Integer getInteger( String toParse ) {
    if ( Utils.isEmpty( toParse ) ) {
      return null;
    }

    try {
      return new Integer( toParse );
    } catch ( NumberFormatException e ) {
      return null;
    }
  }
}
