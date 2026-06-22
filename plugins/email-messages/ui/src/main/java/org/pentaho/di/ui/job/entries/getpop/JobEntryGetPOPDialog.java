// CHECKSTYLE:FileLength:OFF
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


package org.pentaho.di.ui.job.entries.getpop;

import jakarta.mail.Folder;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
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
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.annotations.PluginDialog;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entries.getpop.JobEntryGetPOP;
import org.pentaho.di.job.entries.getpop.MailConnection;
import org.pentaho.di.job.entries.getpop.MailConnectionMeta;
import org.pentaho.di.job.entry.JobEntryDialogInterface;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.ui.core.events.dialog.SelectionAdapterFileDialogTextVar;
import org.pentaho.di.ui.core.events.dialog.SelectionAdapterOptions;
import org.pentaho.di.ui.core.events.dialog.SelectionOperation;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.gui.WindowProperty;
import org.pentaho.di.ui.core.widget.LabelTextVar;
import org.pentaho.di.ui.core.widget.PasswordTextVar;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.job.dialog.JobDialog;
import org.pentaho.di.ui.job.entry.JobEntryDialog;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * This dialog allows you to edit the Get POP job entry settings.
 *
 * @author Matt
 * @since 19-06-2003
 */
@PluginDialog( id = "GET_POP", image = "GETPOP.svg", pluginType = PluginDialog.PluginType.JOBENTRY,
        documentationUrl = "http://wiki.pentaho.com/display/EAI/Get+Mails+from+POP" )
public class JobEntryGetPOPDialog extends JobEntryDialog implements JobEntryDialogInterface {
  private static Class<?> PKG = JobEntryGetPOP.class; // for i18n purposes, needed by Translator2!!

  private Text wName;

  private TextVar wServerName;

  private TextVar wSender;

  private TextVar wRecipient;

  private TextVar wSubject;

  private TextVar wBody;

  private Label wlAttachmentFolder;

  private TextVar wAttachmentFolder;

  private Button wbAttachmentFolder;

  private Label wlAttachmentWildcard;

  private TextVar wAttachmentWildcard;

  private TextVar wUserName;

  private Label wlIMAPFolder;

  private TextVar wIMAPFolder;

  private Label wlMoveToFolder;

  private TextVar wMoveToFolder;

  private Button wSelectMoveToFolder;

  private Button wTestMoveToFolder;

  private TextVar wPassword;

  private Label wlOutputDirectory;

  private TextVar wOutputDirectory;

  private Label wlFilenamePattern;

  private TextVar wFilenamePattern;

  private Button wbDirectory;

  private Label wlListMails;

  private CCombo wListMails;

  private Label wlIMAPListMails;

  private CCombo wIMAPListMails;

  private Label wlAfterGetIMAP;

  private CCombo wAfterGetIMAP;

  private Label wlFirstMails;

  private TextVar wFirstMails;

  private Label wlIMAPFirstMails;

  private TextVar wIMAPFirstMails;

  private TextVar wPort;

  private Button wUseSSL;

  private Button wUseProxy;

  private Label wlProxyUsername;

  private TextVar wProxyUsername;

  private Label wlIncludeSubFolders;

  private Button wIncludeSubFolders;

  private Label wlCreateMoveToFolder;

  private Button wCreateMoveToFolder;

  private Label wlCreateLocalFolder;

  private Button wCreateLocalFolder;

  private Button wNegateSender;

  private Button wNegateRecipient;

  private Button wNegateSubject;

  private Button wNegateBody;

  private Button wNegateReceivedDate;

  private Label wlGetAttachment;

  private Button wGetAttachment;

  private Label wlGetMessage;

  private Button wGetMessage;

  private Label wlDifferentFolderForAttachment;

  private Button wDifferentFolderForAttachment;

  private Label wlPOP3Message;

  private Label wlDelete;

  private Button wDelete;

  private JobEntryGetPOP jobEntry;

  private Shell shell;

  private boolean changed;

  private Label wlReadFrom;

  private TextVar wReadFrom;

  private Button open;

  private Label wlConditionOnReceivedDate;

  private CCombo wConditionOnReceivedDate;

  private CCombo wActionType;

  private Label wlReadTo;

  private TextVar wReadTo;

  private Button openTo;

  private CCombo wProtocol;

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

  private Button wTestIMAPFolder;

  private Button wSelectFolder;

  private MailConnection mailConn = null;

  public JobEntryGetPOPDialog( Shell parent, JobEntryInterface jobEntryInt, Repository rep, JobMeta jobMeta ) {
    super( parent, jobEntryInt, rep, jobMeta );
    jobEntry = (JobEntryGetPOP) jobEntryInt;
    if ( jobEntry.getName() == null ) {
      jobEntry.setName( BaseMessages.getString( PKG, "JobGetPOP.Name.Default" ) );
    }
  }

  protected void setUseAuth() {
    String selectedAuth = wUseAuth.getText();

    if ( selectedAuth.equals( JobEntryGetPOP.AUTENTICATION_OAUTH ) ) {
      wAuthClientId.setEnabled( true );
      wAuthSecretKey.setEnabled( true );
      wUserName.setEnabled( true );
      wPassword.setEnabled( false );
      wAuthScope.setEnabled( true );
      wlGrantType.setEnabled( true );
      grantType.setEnabled( true );
    } else {
      // JobEntryGetPOP.AUTENTICATION_BASIC
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

    if ( selectedAuth.equals( JobEntryGetPOP.GRANTTYPE_CLIENTCREDENTIALS ) ) {
      wAuthTokenUrl.setEnabled( true );
      wAuthorizationCode.setEnabled( false );
      wRedirectUri.setEnabled( false );
      wAuthRefreshToken.setEnabled( false );
    } else if ( selectedAuth.equals( JobEntryGetPOP.GRANTTYPE_REFRESH_TOKEN ) ) {
      wAuthTokenUrl.setEnabled( true );
      wAuthorizationCode.setEnabled( false );
      wRedirectUri.setEnabled( false );
      wAuthRefreshToken.setEnabled( true );
    } else if ( selectedAuth.equals( JobEntryGetPOP.GRANTTYPE_AUTHORIZATION_CODE ) ) {
      wAuthTokenUrl.setEnabled( true );
      wAuthorizationCode.setEnabled( true );
      wRedirectUri.setEnabled( true );
      wAuthRefreshToken.setEnabled( false );
    }
  }

  @Override
  public JobEntryInterface open() {
    Shell parent = getParent();
    Display display = parent.getDisplay();

    shell = new Shell( parent, props.getJobsDialogStyle() );
    props.setLook( shell );
    JobDialog.setShellImage( shell, jobEntry );

    ModifyListener lsMod = new ModifyListener() {
      @Override
      public void modifyText( ModifyEvent e ) {
        closeMailConnection();
        jobEntry.setChanged();
      }
    };

    SelectionListener lsSelection = new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent e ) {
        jobEntry.setChanged();
        closeMailConnection();
      }
    };
    changed = jobEntry.hasChanged();

    FormLayout formLayout = new FormLayout();
    formLayout.marginWidth = Const.FORM_MARGIN;
    formLayout.marginHeight = Const.FORM_MARGIN;

    shell.setLayout( formLayout );
    shell.setText( BaseMessages.getString( PKG, "JobGetPOP.Title" ) );

    int middle = props.getMiddlePct();
    int margin = Const.MARGIN;

    // Filename line
    Label wlName = new Label( shell, SWT.RIGHT );
    wlName.setText( BaseMessages.getString( PKG, "JobGetPOP.Name.Label" ) );
    props.setLook( wlName );
    FormData fdlName = new FormData();
    fdlName.left = new FormAttachment( 0, 0 );
    fdlName.right = new FormAttachment( middle, -margin );
    fdlName.top = new FormAttachment( 0, margin );
    wlName.setLayoutData( fdlName );
    wName = new Text( shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wName );
    wName.addModifyListener( lsMod );
    FormData fdName = new FormData();
    fdName.left = new FormAttachment( middle, 0 );
    fdName.top = new FormAttachment( 0, margin );
    fdName.right = new FormAttachment( 100, 0 );
    wName.setLayoutData( fdName );

    CTabFolder wTabFolder = new CTabFolder( shell, SWT.BORDER );
    props.setLook( wTabFolder, Props.WIDGET_STYLE_TAB );

    // ////////////////////////
    // START OF GENERAL TAB ///
    // ////////////////////////

    CTabItem wGeneralTab = new CTabItem( wTabFolder, SWT.NONE );
    wGeneralTab.setText( BaseMessages.getString( PKG, "JobGetPOP.Tab.General.Label" ) );
    Composite wGeneralComp = new Composite( wTabFolder, SWT.NONE );
    props.setLook( wGeneralComp );
    FormLayout generalLayout = new FormLayout();
    generalLayout.marginWidth = 3;
    generalLayout.marginHeight = 3;
    wGeneralComp.setLayout( generalLayout );

    // ////////////////////////
    // START OF SERVER SETTINGS GROUP
    // ////////////////////////
    Group wServerSettings = new Group( wGeneralComp, SWT.SHADOW_NONE );
    props.setLook( wServerSettings );
    wServerSettings.setText( BaseMessages.getString( PKG, "JobGetPOP.ServerSettings.Group.Label" ) );

    FormLayout serverSettingsGroupLayout = new FormLayout();
    serverSettingsGroupLayout.marginWidth = 10;
    serverSettingsGroupLayout.marginHeight = 10;
    wServerSettings.setLayout( serverSettingsGroupLayout );

    // ServerName line
    Label wlServerName = new Label( wServerSettings, SWT.RIGHT );
    wlServerName.setText( BaseMessages.getString( PKG, "JobGetPOP.Server.Label" ) );
    props.setLook( wlServerName );
    FormData fdlServerName = new FormData();
    fdlServerName.left = new FormAttachment( 0, 0 );
    fdlServerName.top = new FormAttachment( 0, 2 * margin );
    fdlServerName.right = new FormAttachment( middle, -margin );
    wlServerName.setLayoutData( fdlServerName );
    wServerName = new TextVar( jobMeta, wServerSettings, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wServerName );
    wServerName.addModifyListener( lsMod );
    FormData fdServerName = new FormData();
    fdServerName.left = new FormAttachment( middle, 0 );
    fdServerName.top = new FormAttachment( 0, 2 * margin );
    fdServerName.right = new FormAttachment( 100, 0 );
    wServerName.setLayoutData( fdServerName );

    // USE connection with SSL
    Label wlUseSSL = new Label( wServerSettings, SWT.RIGHT );
    wlUseSSL.setText( BaseMessages.getString( PKG, "JobGetPOP.UseSSLMails.Label" ) );
    props.setLook( wlUseSSL );
    FormData fdlUseSSL = new FormData();
    fdlUseSSL.left = new FormAttachment( 0, 0 );
    fdlUseSSL.top = new FormAttachment( wServerName, margin );
    fdlUseSSL.right = new FormAttachment( middle, -margin );
    wlUseSSL.setLayoutData( fdlUseSSL );
    wUseSSL = new Button( wServerSettings, SWT.CHECK );
    props.setLook( wUseSSL );
    FormData fdUseSSL = new FormData();
    wUseSSL.setToolTipText( BaseMessages.getString( PKG, "JobGetPOP.UseSSLMails.Tooltip" ) );
    fdUseSSL.left = new FormAttachment( middle, 0 );
    fdUseSSL.top = new FormAttachment( wServerName, margin );
    fdUseSSL.right = new FormAttachment( 100, 0 );
    wUseSSL.setLayoutData( fdUseSSL );

    wUseSSL.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent e ) {
        closeMailConnection();
        refreshPort( true );
      }
    } );

    // port
    Label wlPort = new Label( wServerSettings, SWT.RIGHT );
    wlPort.setText( BaseMessages.getString( PKG, "JobGetPOP.SSLPort.Label" ) );
    props.setLook( wlPort );
    FormData fdlPort = new FormData();
    fdlPort.left = new FormAttachment( 0, 0 );
    fdlPort.top = new FormAttachment( wUseSSL, margin );
    fdlPort.right = new FormAttachment( middle, -margin );
    wlPort.setLayoutData( fdlPort );
    wPort = new TextVar( jobMeta, wServerSettings, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wPort );
    wPort.setToolTipText( BaseMessages.getString( PKG, "JobGetPOP.SSLPort.Tooltip" ) );
    wPort.addModifyListener( lsMod );
    FormData fdPort = new FormData();
    fdPort.left = new FormAttachment( middle, 0 );
    fdPort.top = new FormAttachment( wUseSSL, margin );
    fdPort.right = new FormAttachment( 100, 0 );
    wPort.setLayoutData( fdPort );

    // Authentication?
    Label wlUseAuth = new Label( wServerSettings, SWT.RIGHT );
    wlUseAuth.setText( BaseMessages.getString( PKG, "JobGetPOP.UseAuthentication.Label" ) );
    props.setLook( wlUseAuth );
    FormData fdlUseAuth = new FormData();
    fdlUseAuth.left = new FormAttachment( 0, 0 );
    fdlUseAuth.top = new FormAttachment( wlPort, 2 * margin );
    fdlUseAuth.right = new FormAttachment( middle, -margin );
    wlUseAuth.setLayoutData( fdlUseAuth );
    wUseAuth = new Combo( wServerSettings, SWT.DROP_DOWN | SWT.READ_ONLY);
    wUseAuth.add( JobEntryGetPOP.AUTENTICATION_BASIC );
    wUseAuth.add( JobEntryGetPOP.AUTENTICATION_OAUTH );
    wUseAuth.select( wUseAuth.indexOf( JobEntryGetPOP.AUTENTICATION_BASIC ) );
    props.setLook( wUseAuth );
    wUseAuth.addModifyListener( lsMod );
    FormData fdUseAuth = new FormData();
    fdUseAuth.left = new FormAttachment( middle, margin );
    fdUseAuth.top = new FormAttachment( wlPort, 2 * margin );
    fdUseAuth.right = new FormAttachment( 100, 0 );
    wUseAuth.setLayoutData( fdUseAuth );
    wUseAuth.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent e ) {
        setUseAuth();
      }
    } );

    // UserName line
    Label wlUserName = new Label( wServerSettings, SWT.RIGHT );
    wlUserName.setText( BaseMessages.getString( PKG, "JobGetPOP.Username.Label" ) );
    props.setLook( wlUserName );
    FormData fdlUserName = new FormData();
    fdlUserName.left = new FormAttachment( 0, 0 );
    fdlUserName.top = new FormAttachment( wUseAuth, margin );
    fdlUserName.right = new FormAttachment( middle, -margin );
    wlUserName.setLayoutData( fdlUserName );
    wUserName = new TextVar( jobMeta, wServerSettings, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wUserName );
    wUserName.setToolTipText( BaseMessages.getString( PKG, "JobGetPOP.Username.Tooltip" ) );
    wUserName.addModifyListener( lsMod );
    FormData fdUserName = new FormData();
    fdUserName.left = new FormAttachment( middle, 0 );
    fdUserName.top = new FormAttachment( wUseAuth, margin );
    fdUserName.right = new FormAttachment( 100, 0 );
    wUserName.setLayoutData( fdUserName );

    // Password line
    Label wlPassword = new Label( wServerSettings, SWT.RIGHT );
    wlPassword.setText( BaseMessages.getString( PKG, "JobGetPOP.Password.Label" ) );
    props.setLook( wlPassword );
    FormData fdlPassword = new FormData();
    fdlPassword.left = new FormAttachment( 0, 0 );
    fdlPassword.top = new FormAttachment( wUserName, margin );
    fdlPassword.right = new FormAttachment( middle, -margin );
    wlPassword.setLayoutData( fdlPassword );
    wPassword = new PasswordTextVar( jobMeta, wServerSettings, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wPassword );
    wPassword.addModifyListener( lsMod );
    FormData fdPassword = new FormData();
    fdPassword.left = new FormAttachment( middle, 0 );
    fdPassword.top = new FormAttachment( wUserName, margin );
    fdPassword.right = new FormAttachment( 100, 0 );
    wPassword.setLayoutData( fdPassword );

    // AuthClientId line
    wAuthClientId = new LabelTextVar( jobMeta, wServerSettings,
            BaseMessages.getString( PKG, "JobGetPOP.AuthenticationClientId.Label" ),
            BaseMessages.getString( PKG, "JobGetPOP.AuthenticationClientId.Tooltip" ) );
    wAuthClientId.addModifyListener( lsMod );
    FormData fdAuthClientId = new FormData();
    fdAuthClientId.left = new FormAttachment( 0, 0 );
    fdAuthClientId.top = new FormAttachment( wPassword, margin );
    fdAuthClientId.right = new FormAttachment( 100, 0 );
    wAuthClientId.setLayoutData( fdAuthClientId );

    // AuthSecretKey line
    wAuthSecretKey = new LabelTextVar( jobMeta, wServerSettings,
            BaseMessages.getString( PKG, "JobGetPOP.AuthenticationSecretKey.Label" ),
            BaseMessages.getString( PKG, "JobGetPOP.AuthenticationSecretKey.Tooltip" ), true );
    wAuthSecretKey.addModifyListener( lsMod );
    FormData fdAuthSecretKey = new FormData();
    fdAuthSecretKey.left = new FormAttachment( 0, 0 );
    fdAuthSecretKey.top = new FormAttachment( wAuthClientId, margin );
    fdAuthSecretKey.right = new FormAttachment( 100, 0 );
    wAuthSecretKey.setLayoutData( fdAuthSecretKey );

    //scope line
    wAuthScope = new LabelTextVar( jobMeta, wServerSettings,
            BaseMessages.getString( PKG, "JobGetPOP.AuthenticationScope.Label" ),
            BaseMessages.getString( PKG, "JobGetPOP.AuthenticationScope.Tooltip" ) );
    wAuthScope.addModifyListener( lsMod );
    FormData fdAuthScope = new FormData();
    fdAuthScope.left = new FormAttachment( 0, 0 );
    fdAuthScope.top = new FormAttachment( wAuthSecretKey, margin );
    fdAuthScope.right = new FormAttachment( 100, 0 );
    wAuthScope.setLayoutData( fdAuthScope );

    // Grant Type
    wlGrantType = new Label( wServerSettings, SWT.RIGHT );
    wlGrantType.setText( BaseMessages.getString( PKG, "JobGetPOP.GrantType.Label" ) );
    props.setLook( wlGrantType );
    FormData fdlGrantType = new FormData();
    fdlGrantType.left = new FormAttachment( 0, 0 );
    fdlGrantType.top = new FormAttachment( wAuthScope, 2*margin );
    fdlGrantType.right = new FormAttachment( middle, -margin );
    wlGrantType.setLayoutData( fdlGrantType );
    grantType = new Combo( wServerSettings, SWT.DROP_DOWN );
    grantType.add( JobEntryGetPOP.GRANTTYPE_CLIENTCREDENTIALS );
    grantType.add( JobEntryGetPOP.GRANTTYPE_AUTHORIZATION_CODE );
    grantType.add( JobEntryGetPOP.GRANTTYPE_REFRESH_TOKEN );
    props.setLook( grantType );
    grantType.addModifyListener( lsMod );
    FormData fdGrantType = new FormData();
    fdGrantType.left = new FormAttachment( middle, margin );
    fdGrantType.top = new FormAttachment( wAuthScope, 2*margin );
    fdGrantType.right = new FormAttachment( 100, 0 );
    grantType.setLayoutData( fdGrantType );
    grantType.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent e ) {
        setUseGrantType();
      }
    } );

    //Token Url
    wAuthTokenUrl = new LabelTextVar( jobMeta, wServerSettings,
            BaseMessages.getString( PKG, "JobGetPOP.AuthenticationTokenUrl.Label" ),
            BaseMessages.getString( PKG, "JobGetPOP.AuthenticationTokenUrl.Tooltip" ) );
    wAuthTokenUrl.addModifyListener( lsMod );
    FormData fdAuthTokenUrl = new FormData();
    fdAuthTokenUrl.left = new FormAttachment( 0, 0 );
    fdAuthTokenUrl.top = new FormAttachment( grantType, margin );
    fdAuthTokenUrl.right = new FormAttachment( 100, 0 );
    wAuthTokenUrl.setLayoutData( fdAuthTokenUrl );

    //AuthorizationCode
    wAuthorizationCode= new LabelTextVar( jobMeta, wServerSettings,
            BaseMessages.getString( PKG, "JobGetPOP.AuthorizationCode.Label" ),
            BaseMessages.getString( PKG, "JobGetPOP.AuthorizationCode.Tooltip" ));
    wAuthorizationCode.addModifyListener( lsMod );
    FormData fdAuthorizationCode = new FormData();
    fdAuthorizationCode.left = new FormAttachment( 0, 0 );
    fdAuthorizationCode.top = new FormAttachment( wAuthTokenUrl, margin );
    fdAuthorizationCode.right = new FormAttachment( 100, 0 );
    wAuthorizationCode.setLayoutData( fdAuthorizationCode );

    //Redirect Uri
    wRedirectUri= new LabelTextVar( jobMeta, wServerSettings,
            BaseMessages.getString( PKG, "JobGetPOP.RedirectURI.Label" ),
            BaseMessages.getString( PKG, "JobGetPOP.RedirectURI.Tooltip" ));
    wRedirectUri.addModifyListener( lsMod );
    FormData fdRedirectUri = new FormData();
    fdRedirectUri.left = new FormAttachment( 0, 0 );
    fdRedirectUri.top = new FormAttachment( wAuthorizationCode, margin );
    fdRedirectUri.right = new FormAttachment( 100, 0 );
    wRedirectUri.setLayoutData( fdRedirectUri );

    //Refresh Token
    wAuthRefreshToken= new LabelTextVar( jobMeta, wServerSettings,
            BaseMessages.getString( PKG, "JobGetPOP.RefreshToken.Label" ),
            BaseMessages.getString( PKG, "JobGetPOP.RefreshToken.Tooltip" ));
    wAuthRefreshToken.addModifyListener( lsMod );
    FormData fdAuthRefreshToken = new FormData();
    fdAuthRefreshToken.left = new FormAttachment( 0, 0 );
    fdAuthRefreshToken.top = new FormAttachment(wRedirectUri, margin );
    fdAuthRefreshToken.right = new FormAttachment( 100, 0 );
    wAuthRefreshToken.setLayoutData( fdAuthRefreshToken );

    // USE proxy
    Label wlUseProxy = new Label( wServerSettings, SWT.RIGHT );
    wlUseProxy.setText( BaseMessages.getString( PKG, "JobGetPOP.UseProxyMails.Label" ) );
    props.setLook( wlUseProxy );
    FormData fdlUseProxy = new FormData();
    fdlUseProxy.left = new FormAttachment( 0, 0 );
    fdlUseProxy.top = new FormAttachment( wAuthRefreshToken, 2 * margin );
    fdlUseProxy.right = new FormAttachment( middle, -margin );
    wlUseProxy.setLayoutData( fdlUseProxy );
    wUseProxy = new Button( wServerSettings, SWT.CHECK );
    props.setLook( wUseProxy );
    FormData fdUseProxy = new FormData();
    wUseProxy.setToolTipText( BaseMessages.getString( PKG, "JobGetPOP.UseProxyMails.Tooltip" ) );
    fdUseProxy.left = new FormAttachment( middle, 0 );
    fdUseProxy.top = new FormAttachment( wAuthRefreshToken, 2 * margin );
    fdUseProxy.right = new FormAttachment( 100, 0 );
    wUseProxy.setLayoutData( fdUseProxy );

    wUseProxy.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent e ) {
        setUserProxy();
        jobEntry.setChanged();
      }
    } );

    // ProxyUsername line
    wlProxyUsername = new Label( wServerSettings, SWT.RIGHT );
    wlProxyUsername.setText( BaseMessages.getString( PKG, "JobGetPOP.ProxyUsername.Label" ) );
    props.setLook( wlProxyUsername );
    FormData fdlProxyUsername = new FormData();
    fdlProxyUsername.left = new FormAttachment( 0, 0 );
    fdlProxyUsername.top = new FormAttachment( wUseProxy, margin );
    fdlProxyUsername.right = new FormAttachment( middle, -margin );
    wlProxyUsername.setLayoutData( fdlProxyUsername );
    wProxyUsername = new TextVar( jobMeta, wServerSettings, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wProxyUsername );
    wProxyUsername.setToolTipText( BaseMessages.getString( PKG, "JobGetPOP.ProxyUsername.Tooltip" ) );
    wProxyUsername.addModifyListener( lsMod );
    FormData fdProxyUsername = new FormData();
    fdProxyUsername.left = new FormAttachment( middle, 0 );
    fdProxyUsername.top = new FormAttachment( wUseProxy, margin );
    fdProxyUsername.right = new FormAttachment( 100, 0 );
    wProxyUsername.setLayoutData( fdProxyUsername );

    // Protocol
    Label wlProtocol = new Label( wServerSettings, SWT.RIGHT );
    wlProtocol.setText( BaseMessages.getString( PKG, "JobGetPOP.Protocol.Label" ) );
    props.setLook( wlProtocol );
    FormData fdlProtocol = new FormData();
    fdlProtocol.left = new FormAttachment( 0, 0 );
    fdlProtocol.right = new FormAttachment( middle, -margin );
    fdlProtocol.top = new FormAttachment( wProxyUsername, margin );
    wlProtocol.setLayoutData( fdlProtocol );
    wProtocol = new CCombo( wServerSettings, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER );
    wProtocol.setItems( MailConnectionMeta.protocolCodes );
    wProtocol.select( 0 );
    props.setLook( wProtocol );
    FormData fdProtocol = new FormData();
    fdProtocol.left = new FormAttachment( middle, 0 );
    fdProtocol.top = new FormAttachment( wProxyUsername, margin );
    fdProtocol.right = new FormAttachment( 100, 0 );
    wProtocol.setLayoutData( fdProtocol );
    wProtocol.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent e ) {
        refreshProtocol( true );
      }
    } );

    // Test connection button
    Button wTest = new Button( wServerSettings, SWT.PUSH );
    wTest.setText( BaseMessages.getString( PKG, "JobGetPOP.TestConnection.Label" ) );
    props.setLook( wTest );
    FormData fdTest = new FormData();
    wTest.setToolTipText( BaseMessages.getString( PKG, "JobGetPOP.TestConnection.Tooltip" ) );
    // fdTest.left = new FormAttachment(middle, 0);
    fdTest.top = new FormAttachment( wProtocol, margin );
    fdTest.right = new FormAttachment( 100, 0 );
    wTest.setLayoutData( fdTest );

    FormData fdServerSettings = new FormData();
    fdServerSettings.left = new FormAttachment( 0, margin );
    fdServerSettings.top = new FormAttachment( wProtocol, margin );
    fdServerSettings.right = new FormAttachment( 100, -margin );
    wServerSettings.setLayoutData( fdServerSettings );
    // ///////////////////////////////////////////////////////////
    // / END OF SERVER SETTINGS GROUP
    // ///////////////////////////////////////////////////////////

    // ////////////////////////
    // START OF Target Folder GROUP
    // ////////////////////////
    Group wTargetFolder = new Group( wGeneralComp, SWT.SHADOW_NONE );
    props.setLook( wTargetFolder );
    wTargetFolder.setText( BaseMessages.getString( PKG, "JobGetPOP.TargetFolder.Group.Label" ) );

    FormLayout targetFolderGroupLayout = new FormLayout();
    targetFolderGroupLayout.marginWidth = 10;
    targetFolderGroupLayout.marginHeight = 10;
    wTargetFolder.setLayout( targetFolderGroupLayout );

    // OutputDirectory line
    wlOutputDirectory = new Label( wTargetFolder, SWT.RIGHT );
    wlOutputDirectory.setText( BaseMessages.getString( PKG, "JobGetPOP.OutputDirectory.Label" ) );
    props.setLook( wlOutputDirectory );
    FormData fdlOutputDirectory = new FormData();
    fdlOutputDirectory.left = new FormAttachment( 0, 0 );
    fdlOutputDirectory.top = new FormAttachment( wServerSettings, margin );
    fdlOutputDirectory.right = new FormAttachment( middle, -margin );
    wlOutputDirectory.setLayoutData( fdlOutputDirectory );

    // Browse Source folders button ...
    wbDirectory = new Button( wTargetFolder, SWT.PUSH | SWT.CENTER );
    props.setLook( wbDirectory );
    wbDirectory.setText( BaseMessages.getString( PKG, "JobGetPOP.BrowseFolders.Label" ) );
    FormData fdbDirectory = new FormData();
    fdbDirectory.right = new FormAttachment( 100, -margin );
    fdbDirectory.top = new FormAttachment( wServerSettings, margin );
    wbDirectory.setLayoutData( fdbDirectory );

    wOutputDirectory = new TextVar( jobMeta, wTargetFolder, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wOutputDirectory );
    wOutputDirectory.setToolTipText( BaseMessages.getString( PKG, "JobGetPOP.OutputDirectory.Tooltip" ) );
    wOutputDirectory.addModifyListener( lsMod );
    FormData fdOutputDirectory = new FormData();
    fdOutputDirectory.left = new FormAttachment( middle, 0 );
    fdOutputDirectory.top = new FormAttachment( wServerSettings, margin );
    fdOutputDirectory.right = new FormAttachment( wbDirectory, -margin );
    wOutputDirectory.setLayoutData( fdOutputDirectory );

    wbDirectory.addSelectionListener( new SelectionAdapterFileDialogTextVar( jobMeta.getLogChannel(), wOutputDirectory, jobMeta,
      new SelectionAdapterOptions( jobMeta.getBowl(), SelectionOperation.FOLDER ) ) );

    // Create local folder
    wlCreateLocalFolder = new Label( wTargetFolder, SWT.RIGHT );
    wlCreateLocalFolder.setText( BaseMessages.getString( PKG, "JobGetPOP.createLocalFolder.Label" ) );
    props.setLook( wlCreateLocalFolder );
    FormData fdlCreateLocalFolder = new FormData();
    fdlCreateLocalFolder.left = new FormAttachment( 0, 0 );
    fdlCreateLocalFolder.top = new FormAttachment( wOutputDirectory, margin );
    fdlCreateLocalFolder.right = new FormAttachment( middle, -margin );
    wlCreateLocalFolder.setLayoutData( fdlCreateLocalFolder );
    wCreateLocalFolder = new Button( wTargetFolder, SWT.CHECK );
    props.setLook( wCreateLocalFolder );
    FormData fdCreateLocalFolder = new FormData();
    wCreateLocalFolder.setToolTipText( BaseMessages.getString( PKG, "JobGetPOP.createLocalFolder.Tooltip" ) );
    fdCreateLocalFolder.left = new FormAttachment( middle, 0 );
    fdCreateLocalFolder.top = new FormAttachment( wOutputDirectory, margin );
    fdCreateLocalFolder.right = new FormAttachment( 100, 0 );
    wCreateLocalFolder.setLayoutData( fdCreateLocalFolder );

    // Filename pattern line
    wlFilenamePattern = new Label( wTargetFolder, SWT.RIGHT );
    wlFilenamePattern.setText( BaseMessages.getString( PKG, "JobGetPOP.FilenamePattern.Label" ) );
    props.setLook( wlFilenamePattern );
    FormData fdlFilenamePattern = new FormData();
    fdlFilenamePattern.left = new FormAttachment( 0, 0 );
    fdlFilenamePattern.top = new FormAttachment( wCreateLocalFolder, margin );
    fdlFilenamePattern.right = new FormAttachment( middle, -margin );
    wlFilenamePattern.setLayoutData( fdlFilenamePattern );
    wFilenamePattern = new TextVar( jobMeta, wTargetFolder, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wFilenamePattern );
    wFilenamePattern.setToolTipText( BaseMessages.getString( PKG, "JobGetPOP.FilenamePattern.Tooltip" ) );
    wFilenamePattern.addModifyListener( lsMod );
    FormData fdFilenamePattern = new FormData();
    fdFilenamePattern.left = new FormAttachment( middle, 0 );
    fdFilenamePattern.top = new FormAttachment( wCreateLocalFolder, margin );
    fdFilenamePattern.right = new FormAttachment( 100, 0 );
    wFilenamePattern.setLayoutData( fdFilenamePattern );

    // Whenever something changes, set the tooltip to the expanded version:
    wFilenamePattern.addModifyListener(
      e -> wFilenamePattern.setToolTipText( jobMeta.environmentSubstitute( wFilenamePattern.getText() ) ) );

    // Get message?
    wlGetMessage = new Label( wTargetFolder, SWT.RIGHT );
    wlGetMessage.setText( BaseMessages.getString( PKG, "JobGetPOP.GetMessageMails.Label" ) );
    props.setLook( wlGetMessage );
    FormData fdlGetMessage = new FormData();
    fdlGetMessage.left = new FormAttachment( 0, 0 );
    fdlGetMessage.top = new FormAttachment( wFilenamePattern, margin );
    fdlGetMessage.right = new FormAttachment( middle, -margin );
    wlGetMessage.setLayoutData( fdlGetMessage );
    wGetMessage = new Button( wTargetFolder, SWT.CHECK );
    props.setLook( wGetMessage );
    FormData fdGetMessage = new FormData();
    wGetMessage.setToolTipText( BaseMessages.getString( PKG, "JobGetPOP.GetMessageMails.Tooltip" ) );
    fdGetMessage.left = new FormAttachment( middle, 0 );
    fdGetMessage.top = new FormAttachment( wFilenamePattern, margin );
    fdGetMessage.right = new FormAttachment( 100, 0 );
    wGetMessage.setLayoutData( fdGetMessage );

    wGetMessage.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent e ) {
        if ( !wGetAttachment.getSelection() && !wGetMessage.getSelection() ) {
          wGetAttachment.setSelection( true );
        }
      }
    } );

    // Get attachment?
    wlGetAttachment = new Label( wTargetFolder, SWT.RIGHT );
    wlGetAttachment.setText( BaseMessages.getString( PKG, "JobGetPOP.GetAttachmentMails.Label" ) );
    props.setLook( wlGetAttachment );
    FormData fdlGetAttachment = new FormData();
    fdlGetAttachment.left = new FormAttachment( 0, 0 );
    fdlGetAttachment.top = new FormAttachment( wGetMessage, margin );
    fdlGetAttachment.right = new FormAttachment( middle, -margin );
    wlGetAttachment.setLayoutData( fdlGetAttachment );
    wGetAttachment = new Button( wTargetFolder, SWT.CHECK );
    props.setLook( wGetAttachment );
    FormData fdGetAttachment = new FormData();
    wGetAttachment.setToolTipText( BaseMessages.getString( PKG, "JobGetPOP.GetAttachmentMails.Tooltip" ) );
    fdGetAttachment.left = new FormAttachment( middle, 0 );
    fdGetAttachment.top = new FormAttachment( wGetMessage, margin );
    fdGetAttachment.right = new FormAttachment( 100, 0 );
    wGetAttachment.setLayoutData( fdGetAttachment );

    wGetAttachment.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent e ) {
        activeAttachmentFolder();
      }
    } );

    // different folder for attachment?
    wlDifferentFolderForAttachment = new Label( wTargetFolder, SWT.RIGHT );
    wlDifferentFolderForAttachment.setText( BaseMessages.getString(
      PKG, "JobGetPOP.DifferentFolderForAttachmentMails.Label" ) );
    props.setLook( wlDifferentFolderForAttachment );
    FormData fdlDifferentFolderForAttachment = new FormData();
    fdlDifferentFolderForAttachment.left = new FormAttachment( 0, 0 );
    fdlDifferentFolderForAttachment.top = new FormAttachment( wGetAttachment, margin );
    fdlDifferentFolderForAttachment.right = new FormAttachment( middle, -margin );
    wlDifferentFolderForAttachment.setLayoutData( fdlDifferentFolderForAttachment );
    wDifferentFolderForAttachment = new Button( wTargetFolder, SWT.CHECK );
    props.setLook( wDifferentFolderForAttachment );
    FormData fdDifferentFolderForAttachment = new FormData();
    wDifferentFolderForAttachment.setToolTipText( BaseMessages.getString(
      PKG, "JobGetPOP.DifferentFolderForAttachmentMails.Tooltip" ) );
    fdDifferentFolderForAttachment.left = new FormAttachment( middle, 0 );
    fdDifferentFolderForAttachment.top = new FormAttachment( wGetAttachment, margin );
    fdDifferentFolderForAttachment.right = new FormAttachment( 100, 0 );
    wDifferentFolderForAttachment.setLayoutData( fdDifferentFolderForAttachment );

    wDifferentFolderForAttachment.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent e ) {
        activeAttachmentFolder();
      }
    } );

    // AttachmentFolder line
    wlAttachmentFolder = new Label( wTargetFolder, SWT.RIGHT );
    wlAttachmentFolder.setText( BaseMessages.getString( PKG, "JobGetPOP.AttachmentFolder.Label" ) );
    props.setLook( wlAttachmentFolder );
    FormData fdlAttachmentFolder = new FormData();
    fdlAttachmentFolder.left = new FormAttachment( 0, 0 );
    fdlAttachmentFolder.top = new FormAttachment( wDifferentFolderForAttachment, margin );
    fdlAttachmentFolder.right = new FormAttachment( middle, -margin );
    wlAttachmentFolder.setLayoutData( fdlAttachmentFolder );

    // Browse Source folders button ...
    wbAttachmentFolder = new Button( wTargetFolder, SWT.PUSH | SWT.CENTER );
    props.setLook( wbAttachmentFolder );
    wbAttachmentFolder.setText( BaseMessages.getString( PKG, "JobGetPOP.BrowseFolders.Label" ) );
    FormData fdbAttachmentFolder = new FormData();
    fdbAttachmentFolder.right = new FormAttachment( 100, -margin );
    fdbAttachmentFolder.top = new FormAttachment( wDifferentFolderForAttachment, margin );
    wbAttachmentFolder.setLayoutData( fdbAttachmentFolder );

    wAttachmentFolder = new TextVar( jobMeta, wTargetFolder, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wAttachmentFolder );
    wAttachmentFolder.setToolTipText( BaseMessages.getString( PKG, "JobGetPOP.AttachmentFolder.Tooltip" ) );
    wAttachmentFolder.addModifyListener( lsMod );
    FormData fdAttachmentFolder = new FormData();
    fdAttachmentFolder.left = new FormAttachment( middle, 0 );
    fdAttachmentFolder.top = new FormAttachment( wDifferentFolderForAttachment, margin );
    fdAttachmentFolder.right = new FormAttachment( wbAttachmentFolder, -margin );
    wAttachmentFolder.setLayoutData( fdAttachmentFolder );

    wbAttachmentFolder.addSelectionListener( new SelectionAdapterFileDialogTextVar( jobMeta.getLogChannel(), wAttachmentFolder, jobMeta,
      new SelectionAdapterOptions( jobMeta.getBowl(), SelectionOperation.FOLDER ) ) );

    // Limit attached files
    wlAttachmentWildcard = new Label( wTargetFolder, SWT.RIGHT );
    wlAttachmentWildcard.setText( BaseMessages.getString( PKG, "JobGetPOP.AttachmentWildcard.Label" ) );
    props.setLook( wlAttachmentWildcard );
    FormData fdlAttachmentWildcard = new FormData();
    fdlAttachmentWildcard.left = new FormAttachment( 0, 0 );
    fdlAttachmentWildcard.top = new FormAttachment( wbAttachmentFolder, margin );
    fdlAttachmentWildcard.right = new FormAttachment( middle, -margin );
    wlAttachmentWildcard.setLayoutData( fdlAttachmentWildcard );
    wAttachmentWildcard = new TextVar( jobMeta, wTargetFolder, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wAttachmentWildcard );
    wAttachmentWildcard.setToolTipText( BaseMessages.getString( PKG, "JobGetPOP.AttachmentWildcard.Tooltip" ) );
    wAttachmentWildcard.addModifyListener( lsMod );
    FormData fdAttachmentWildcard = new FormData();
    fdAttachmentWildcard.left = new FormAttachment( middle, 0 );
    fdAttachmentWildcard.top = new FormAttachment( wbAttachmentFolder, margin );
    fdAttachmentWildcard.right = new FormAttachment( 100, 0 );
    wAttachmentWildcard.setLayoutData( fdAttachmentWildcard );

    // Whenever something changes, set the tooltip to the expanded version:
    wAttachmentWildcard.addModifyListener(
      e -> wAttachmentWildcard.setToolTipText( jobMeta.environmentSubstitute( wAttachmentWildcard.getText() ) ) );

    FormData fdTargetFolder = new FormData();
    fdTargetFolder.left = new FormAttachment( 0, margin );
    fdTargetFolder.top = new FormAttachment( wServerSettings, margin );
    fdTargetFolder.right = new FormAttachment( 100, -margin );
    wTargetFolder.setLayoutData( fdTargetFolder );
    // ///////////////////////////////////////////////////////////
    // / END OF SERVER SETTINGS GROUP
    // ///////////////////////////////////////////////////////////

    FormData fdGeneralComp = new FormData();
    fdGeneralComp.left = new FormAttachment( 0, 0 );
    fdGeneralComp.top = new FormAttachment( wName, 0 );
    fdGeneralComp.right = new FormAttachment( 100, 0 );
    fdGeneralComp.bottom = new FormAttachment( 100, 0 );
    wGeneralComp.setLayoutData( fdGeneralComp );

    wGeneralComp.layout();
    wGeneralTab.setControl( wGeneralComp );
    props.setLook( wGeneralComp );

    // ///////////////////////////////////////////////////////////
    // / END OF GENERAL TAB
    // ///////////////////////////////////////////////////////////

    // ////////////////////////
    // START OF SETTINGS TAB ///
    // ////////////////////////

    CTabItem wSettingsTab = new CTabItem( wTabFolder, SWT.NONE );
    wSettingsTab.setText( BaseMessages.getString( PKG, "JobGetPOP.Tab.Pop.Label" ) );
    Composite wSettingsComp = new Composite( wTabFolder, SWT.NONE );
    props.setLook( wSettingsComp );
    FormLayout PopLayout = new FormLayout();
    PopLayout.marginWidth = 3;
    PopLayout.marginHeight = 3;
    wSettingsComp.setLayout( PopLayout );

    // Action type
    Label wlActionType = new Label( wSettingsComp, SWT.RIGHT );
    wlActionType.setText( BaseMessages.getString( PKG, "JobGetPOP.ActionType.Label" ) );
    props.setLook( wlActionType );
    FormData fdlActionType = new FormData();
    fdlActionType.left = new FormAttachment( 0, 0 );
    fdlActionType.right = new FormAttachment( middle, -margin );
    fdlActionType.top = new FormAttachment( 0, 3 * margin );
    wlActionType.setLayoutData( fdlActionType );

    wActionType = new CCombo( wSettingsComp, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER );
    wActionType.setItems( MailConnectionMeta.actionTypeDesc );
    wActionType.select( 0 ); // +1: starts at -1

    props.setLook( wActionType );
    FormData fdActionType = new FormData();
    fdActionType.left = new FormAttachment( middle, 0 );
    fdActionType.top = new FormAttachment( 0, 3 * margin );
    fdActionType.right = new FormAttachment( 100, 0 );
    wActionType.setLayoutData( fdActionType );
    wActionType.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent e ) {
        setActionType();
        jobEntry.setChanged();
      }
    } );

    // Message: for POP3, only INBOX folder is available!
    wlPOP3Message = new Label( wSettingsComp, SWT.RIGHT );
    wlPOP3Message.setText( BaseMessages.getString( PKG, "JobGetPOP.POP3Message.Label" ) );
    props.setLook( wlPOP3Message );
    FormData fdlPOP3Message = new FormData();
    fdlPOP3Message.left = new FormAttachment( 0, margin );
    fdlPOP3Message.top = new FormAttachment( wActionType, 3 * margin );
    wlPOP3Message.setLayoutData( fdlPOP3Message );
    wlPOP3Message.setForeground( GUIResource.getInstance().getColorOrange() );

    // ////////////////////////
    // START OF POP3 Settings GROUP
    // ////////////////////////
    Group wPOP3Settings = new Group( wSettingsComp, SWT.SHADOW_NONE );
    props.setLook( wPOP3Settings );
    wPOP3Settings.setText( BaseMessages.getString( PKG, "JobGetPOP.POP3Settings.Group.Label" ) );

    FormLayout pop3SettingsGroupLayout = new FormLayout();
    pop3SettingsGroupLayout.marginWidth = 10;
    pop3SettingsGroupLayout.marginHeight = 10;
    wPOP3Settings.setLayout( pop3SettingsGroupLayout );

    // List of mails of retrieve
    wlListMails = new Label( wPOP3Settings, SWT.RIGHT );
    wlListMails.setText( BaseMessages.getString( PKG, "JobGetPOP.Listmails.Label" ) );
    props.setLook( wlListMails );
    FormData fdlListMails = new FormData();
    fdlListMails.left = new FormAttachment( 0, 0 );
    fdlListMails.right = new FormAttachment( middle, 0 );
    fdlListMails.top = new FormAttachment( wlPOP3Message, 2 * margin );
    wlListMails.setLayoutData( fdlListMails );
    wListMails = new CCombo( wPOP3Settings, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER );
    wListMails.add( BaseMessages.getString( PKG, "JobGetPOP.RetrieveAllMails.Label" ) );
    // PDI-7241 POP3 does not support retrieve unread
    wListMails.add( BaseMessages.getString( PKG, "JobGetPOP.RetrieveFirstMails.Label" ) );
    wListMails.select( 0 ); // +1: starts at -1

    props.setLook( wListMails );
    FormData fdListMails = new FormData();
    fdListMails.left = new FormAttachment( middle, 0 );
    fdListMails.top = new FormAttachment( wlPOP3Message, 2 * margin );
    fdListMails.right = new FormAttachment( 100, 0 );
    wListMails.setLayoutData( fdListMails );

    wListMails.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent e ) {
        jobEntry.setChanged();
        chooseListMails();
      }
    } );

    // Retrieve the first ... mails
    wlFirstMails = new Label( wPOP3Settings, SWT.RIGHT );
    wlFirstMails.setText( BaseMessages.getString( PKG, "JobGetPOP.Firstmails.Label" ) );
    props.setLook( wlFirstMails );
    FormData fdlFirstMails = new FormData();
    fdlFirstMails.left = new FormAttachment( 0, 0 );
    fdlFirstMails.right = new FormAttachment( middle, -margin );
    fdlFirstMails.top = new FormAttachment( wListMails, margin );
    wlFirstMails.setLayoutData( fdlFirstMails );

    wFirstMails = new TextVar( jobMeta, wPOP3Settings, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wFirstMails );
    wFirstMails.addModifyListener( lsMod );
    FormData fdFirstMails = new FormData();
    fdFirstMails.left = new FormAttachment( middle, 0 );
    fdFirstMails.top = new FormAttachment( wListMails, margin );
    fdFirstMails.right = new FormAttachment( 100, 0 );
    wFirstMails.setLayoutData( fdFirstMails );

    // Delete mails after retrieval...
    wlDelete = new Label( wPOP3Settings, SWT.RIGHT );
    wlDelete.setText( BaseMessages.getString( PKG, "JobGetPOP.DeleteMails.Label" ) );
    props.setLook( wlDelete );
    FormData fdlDelete = new FormData();
    fdlDelete.left = new FormAttachment( 0, 0 );
    fdlDelete.top = new FormAttachment( wFirstMails, margin );
    fdlDelete.right = new FormAttachment( middle, -margin );
    wlDelete.setLayoutData( fdlDelete );
    wDelete = new Button( wPOP3Settings, SWT.CHECK );
    props.setLook( wDelete );
    FormData fdDelete = new FormData();
    wDelete.setToolTipText( BaseMessages.getString( PKG, "JobGetPOP.DeleteMails.Tooltip" ) );
    fdDelete.left = new FormAttachment( middle, 0 );
    fdDelete.top = new FormAttachment( wFirstMails, margin );
    fdDelete.right = new FormAttachment( 100, 0 );
    wDelete.setLayoutData( fdDelete );

    FormData fdPOP3Settings = new FormData();
    fdPOP3Settings.left = new FormAttachment( 0, margin );
    fdPOP3Settings.top = new FormAttachment( wlPOP3Message, 2 * margin );
    fdPOP3Settings.right = new FormAttachment( 100, -margin );
    wPOP3Settings.setLayoutData( fdPOP3Settings );
    // ///////////////////////////////////////////////////////////
    // / END OF POP3 SETTINGS GROUP
    // ///////////////////////////////////////////////////////////

    // ////////////////////////
    // START OF IMAP Settings GROUP
    // ////////////////////////
    Group wIMAPSettings = new Group( wSettingsComp, SWT.SHADOW_NONE );
    props.setLook( wIMAPSettings );
    wIMAPSettings.setText( BaseMessages.getString( PKG, "JobGetPOP.IMAPSettings.Groupp.Label" ) );

    FormLayout imapSettingsGroupLayout = new FormLayout();
    imapSettingsGroupLayout.marginWidth = 10;
    imapSettingsGroupLayout.marginHeight = 10;
    wIMAPSettings.setLayout( imapSettingsGroupLayout );

    // SelectFolder button
    wSelectFolder = new Button( wIMAPSettings, SWT.PUSH );
    wSelectFolder.setText( BaseMessages.getString( PKG, "JobGetPOP.SelectFolderConnection.Label" ) );
    props.setLook( wSelectFolder );
    FormData fdSelectFolder = new FormData();
    wSelectFolder.setToolTipText( BaseMessages.getString( PKG, "JobGetPOP.SelectFolderConnection.Tooltip" ) );
    fdSelectFolder.top = new FormAttachment( wPOP3Settings, margin );
    fdSelectFolder.right = new FormAttachment( 100, 0 );
    wSelectFolder.setLayoutData( fdSelectFolder );

    // TestIMAPFolder button
    wTestIMAPFolder = new Button( wIMAPSettings, SWT.PUSH );
    wTestIMAPFolder.setText( BaseMessages.getString( PKG, "JobGetPOP.TestIMAPFolderConnection.Label" ) );
    props.setLook( wTestIMAPFolder );
    FormData fdTestIMAPFolder = new FormData();
    wTestIMAPFolder.setToolTipText( BaseMessages.getString( PKG, "JobGetPOP.TestIMAPFolderConnection.Tooltip" ) );
    fdTestIMAPFolder.top = new FormAttachment( wPOP3Settings, margin );
    fdTestIMAPFolder.right = new FormAttachment( wSelectFolder, -margin );
    wTestIMAPFolder.setLayoutData( fdTestIMAPFolder );

    // IMAPFolder line
    wlIMAPFolder = new Label( wIMAPSettings, SWT.RIGHT );
    wlIMAPFolder.setText( BaseMessages.getString( PKG, "JobGetPOP.IMAPFolder.Label" ) );
    props.setLook( wlIMAPFolder );
    FormData fdlIMAPFolder = new FormData();
    fdlIMAPFolder.left = new FormAttachment( 0, 0 );
    fdlIMAPFolder.top = new FormAttachment( wPOP3Settings, margin );
    fdlIMAPFolder.right = new FormAttachment( middle, -margin );
    wlIMAPFolder.setLayoutData( fdlIMAPFolder );
    wIMAPFolder = new TextVar( jobMeta, wIMAPSettings, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wIMAPFolder );
    wIMAPFolder.setToolTipText( BaseMessages.getString( PKG, "JobGetPOP.IMAPFolder.Tooltip" ) );
    wIMAPFolder.addModifyListener( lsMod );
    FormData fdIMAPFolder = new FormData();
    fdIMAPFolder.left = new FormAttachment( middle, 0 );
    fdIMAPFolder.top = new FormAttachment( wPOP3Settings, margin );
    fdIMAPFolder.right = new FormAttachment( wTestIMAPFolder, -margin );
    wIMAPFolder.setLayoutData( fdIMAPFolder );

    // Include subfolders?
    wlIncludeSubFolders = new Label( wIMAPSettings, SWT.RIGHT );
    wlIncludeSubFolders.setText( BaseMessages.getString( PKG, "JobGetPOP.IncludeSubFoldersMails.Label" ) );
    props.setLook( wlIncludeSubFolders );
    FormData fdlIncludeSubFolders = new FormData();
    fdlIncludeSubFolders.left = new FormAttachment( 0, 0 );
    fdlIncludeSubFolders.top = new FormAttachment( wIMAPFolder, margin );
    fdlIncludeSubFolders.right = new FormAttachment( middle, -margin );
    wlIncludeSubFolders.setLayoutData( fdlIncludeSubFolders );
    wIncludeSubFolders = new Button( wIMAPSettings, SWT.CHECK );
    props.setLook( wIncludeSubFolders );
    FormData fdIncludeSubFolders = new FormData();
    wIncludeSubFolders.setToolTipText( BaseMessages.getString( PKG, "JobGetPOP.IncludeSubFoldersMails.Tooltip" ) );
    fdIncludeSubFolders.left = new FormAttachment( middle, 0 );
    fdIncludeSubFolders.top = new FormAttachment( wIMAPFolder, margin );
    fdIncludeSubFolders.right = new FormAttachment( 100, 0 );
    wIncludeSubFolders.setLayoutData( fdIncludeSubFolders );
    wIncludeSubFolders.addSelectionListener( lsSelection );

    // List of mails of retrieve
    wlIMAPListMails = new Label( wIMAPSettings, SWT.RIGHT );
    wlIMAPListMails.setText( BaseMessages.getString( PKG, "JobGetPOP.IMAPListmails.Label" ) );
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
      @Override
      public void widgetSelected( SelectionEvent e ) {
        // ChooseIMAPListmails();
      }
    } );

    // Retrieve the first ... mails
    wlIMAPFirstMails = new Label( wIMAPSettings, SWT.RIGHT );
    wlIMAPFirstMails.setText( BaseMessages.getString( PKG, "JobGetPOP.IMAPFirstmails.Label" ) );
    props.setLook( wlIMAPFirstMails );
    FormData fdlIMAPFirstMails = new FormData();
    fdlIMAPFirstMails.left = new FormAttachment( 0, 0 );
    fdlIMAPFirstMails.right = new FormAttachment( middle, -margin );
    fdlIMAPFirstMails.top = new FormAttachment( wIMAPListMails, margin );
    wlIMAPFirstMails.setLayoutData( fdlIMAPFirstMails );

    wIMAPFirstMails = new TextVar( jobMeta, wIMAPSettings, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wIMAPFirstMails );
    wIMAPFirstMails.addModifyListener( lsMod );
    FormData fdIMAPFirstMails = new FormData();
    fdIMAPFirstMails.left = new FormAttachment( middle, 0 );
    fdIMAPFirstMails.top = new FormAttachment( wIMAPListMails, margin );
    fdIMAPFirstMails.right = new FormAttachment( 100, 0 );
    wIMAPFirstMails.setLayoutData( fdIMAPFirstMails );

    // After get IMAP
    wlAfterGetIMAP = new Label( wIMAPSettings, SWT.RIGHT );
    wlAfterGetIMAP.setText( BaseMessages.getString( PKG, "JobGetPOP.AfterGetIMAP.Label" ) );
    props.setLook( wlAfterGetIMAP );
    FormData fdlAfterGetIMAP = new FormData();
    fdlAfterGetIMAP.left = new FormAttachment( 0, 0 );
    fdlAfterGetIMAP.right = new FormAttachment( middle, -margin );
    fdlAfterGetIMAP.top = new FormAttachment( wIMAPFirstMails, 2 * margin );
    wlAfterGetIMAP.setLayoutData( fdlAfterGetIMAP );
    wAfterGetIMAP = new CCombo( wIMAPSettings, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER );
    wAfterGetIMAP.setItems( MailConnectionMeta.afterGetIMAPDesc );
    wAfterGetIMAP.select( 0 ); // +1: starts at -1

    props.setLook( wAfterGetIMAP );
    FormData fdAfterGetIMAP = new FormData();
    fdAfterGetIMAP.left = new FormAttachment( middle, 0 );
    fdAfterGetIMAP.top = new FormAttachment( wIMAPFirstMails, 2 * margin );
    fdAfterGetIMAP.right = new FormAttachment( 100, 0 );
    wAfterGetIMAP.setLayoutData( fdAfterGetIMAP );

    wAfterGetIMAP.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent e ) {
        setAfterIMAPRetrieved();
        jobEntry.setChanged();
      }
    } );

    // MoveToFolder line
    wlMoveToFolder = new Label( wIMAPSettings, SWT.RIGHT );
    wlMoveToFolder.setText( BaseMessages.getString( PKG, "JobGetPOP.MoveToFolder.Label" ) );
    props.setLook( wlMoveToFolder );
    FormData fdlMoveToFolder = new FormData();
    fdlMoveToFolder.left = new FormAttachment( 0, 0 );
    fdlMoveToFolder.top = new FormAttachment( wAfterGetIMAP, margin );
    fdlMoveToFolder.right = new FormAttachment( middle, -margin );
    wlMoveToFolder.setLayoutData( fdlMoveToFolder );

    // SelectMoveToFolder button
    wSelectMoveToFolder = new Button( wIMAPSettings, SWT.PUSH );
    wSelectMoveToFolder.setText( BaseMessages.getString( PKG, "JobGetPOP.SelectMoveToFolderConnection.Label" ) );
    props.setLook( wSelectMoveToFolder );
    FormData fdSelectMoveToFolder = new FormData();
    wSelectMoveToFolder.setToolTipText( BaseMessages.getString(
      PKG, "JobGetPOP.SelectMoveToFolderConnection.Tooltip" ) );
    fdSelectMoveToFolder.top = new FormAttachment( wAfterGetIMAP, margin );
    fdSelectMoveToFolder.right = new FormAttachment( 100, 0 );
    wSelectMoveToFolder.setLayoutData( fdSelectMoveToFolder );

    // TestMoveToFolder button
    wTestMoveToFolder = new Button( wIMAPSettings, SWT.PUSH );
    wTestMoveToFolder.setText( BaseMessages.getString( PKG, "JobGetPOP.TestMoveToFolderConnection.Label" ) );
    props.setLook( wTestMoveToFolder );
    FormData fdTestMoveToFolder = new FormData();
    wTestMoveToFolder
      .setToolTipText( BaseMessages.getString( PKG, "JobGetPOP.TestMoveToFolderConnection.Tooltip" ) );
    fdTestMoveToFolder.top = new FormAttachment( wAfterGetIMAP, margin );
    fdTestMoveToFolder.right = new FormAttachment( wSelectMoveToFolder, -margin );
    wTestMoveToFolder.setLayoutData( fdTestMoveToFolder );

    wMoveToFolder = new TextVar( jobMeta, wIMAPSettings, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wMoveToFolder );
    wMoveToFolder.setToolTipText( BaseMessages.getString( PKG, "JobGetPOP.MoveToFolder.Tooltip" ) );
    wMoveToFolder.addModifyListener( lsMod );
    FormData fdMoveToFolder = new FormData();
    fdMoveToFolder.left = new FormAttachment( middle, 0 );
    fdMoveToFolder.top = new FormAttachment( wAfterGetIMAP, margin );
    fdMoveToFolder.right = new FormAttachment( wTestMoveToFolder, -margin );
    wMoveToFolder.setLayoutData( fdMoveToFolder );

    // Create move to folder
    wlCreateMoveToFolder = new Label( wIMAPSettings, SWT.RIGHT );
    wlCreateMoveToFolder.setText( BaseMessages.getString( PKG, "JobGetPOP.createMoveToFolderMails.Label" ) );
    props.setLook( wlCreateMoveToFolder );
    FormData fdlCreateMoveToFolder = new FormData();
    fdlCreateMoveToFolder.left = new FormAttachment( 0, 0 );
    fdlCreateMoveToFolder.top = new FormAttachment( wMoveToFolder, margin );
    fdlCreateMoveToFolder.right = new FormAttachment( middle, -margin );
    wlCreateMoveToFolder.setLayoutData( fdlCreateMoveToFolder );
    wCreateMoveToFolder = new Button( wIMAPSettings, SWT.CHECK );
    props.setLook( wCreateMoveToFolder );
    FormData fdCreateMoveToFolder = new FormData();
    wCreateMoveToFolder
      .setToolTipText( BaseMessages.getString( PKG, "JobGetPOP.createMoveToFolderMails.Tooltip" ) );
    fdCreateMoveToFolder.left = new FormAttachment( middle, 0 );
    fdCreateMoveToFolder.top = new FormAttachment( wMoveToFolder, margin );
    fdCreateMoveToFolder.right = new FormAttachment( 100, 0 );
    wCreateMoveToFolder.setLayoutData( fdCreateMoveToFolder );

    FormData fdIMAPSettings = new FormData();
    fdIMAPSettings.left = new FormAttachment( 0, margin );
    fdIMAPSettings.top = new FormAttachment( wPOP3Settings, 2 * margin );
    fdIMAPSettings.right = new FormAttachment( 100, -margin );
    wIMAPSettings.setLayoutData( fdIMAPSettings );
    // ///////////////////////////////////////////////////////////
    // / END OF IMAP SETTINGS GROUP
    // ///////////////////////////////////////////////////////////

    FormData fdSettingsComp = new FormData();
    fdSettingsComp.left = new FormAttachment( 0, 0 );
    fdSettingsComp.top = new FormAttachment( wName, 0 );
    fdSettingsComp.right = new FormAttachment( 100, 0 );
    fdSettingsComp.bottom = new FormAttachment( 100, 0 );
    wSettingsComp.setLayoutData( fdSettingsComp );

    wSettingsComp.layout();
    wSettingsTab.setControl( wSettingsComp );
    props.setLook( wSettingsComp );

    // ///////////////////////////////////////////////////////////
    // / END OF Pop TAB
    // ///////////////////////////////////////////////////////////

    // ////////////////////////
    // START OF SEARCH TAB ///
    // ////////////////////////

    CTabItem wSearchTab = new CTabItem( wTabFolder, SWT.NONE );
    wSearchTab.setText( BaseMessages.getString( PKG, "JobGetPOP.Tab.Search.Label" ) );
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
    wHeader.setText( BaseMessages.getString( PKG, "JobGetPOP.Header.Group.Label" ) );

    FormLayout headerGroupLayout = new FormLayout();
    headerGroupLayout.marginWidth = 10;
    headerGroupLayout.marginHeight = 10;
    wHeader.setLayout( headerGroupLayout );

    wNegateSender = new Button( wHeader, SWT.CHECK );
    props.setLook( wNegateSender );
    FormData fdNegateSender = new FormData();
    wNegateSender.setToolTipText( BaseMessages.getString( PKG, "JobGetPOP.NegateSender.Tooltip" ) );
    fdNegateSender.top = new FormAttachment( 0, margin );
    fdNegateSender.right = new FormAttachment( 100, -margin );
    wNegateSender.setLayoutData( fdNegateSender );

    // From line
    Label wlSender = new Label( wHeader, SWT.RIGHT );
    wlSender.setText( BaseMessages.getString( PKG, "JobGetPOP.wSender.Label" ) );
    props.setLook( wlSender );
    FormData fdlSender = new FormData();
    fdlSender.left = new FormAttachment( 0, 0 );
    fdlSender.top = new FormAttachment( 0, margin );
    fdlSender.right = new FormAttachment( middle, -margin );
    wlSender.setLayoutData( fdlSender );
    wSender = new TextVar( jobMeta, wHeader, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
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
    wNegateRecipient.setToolTipText( BaseMessages.getString( PKG, "JobGetPOP.NegateReceipient.Tooltip" ) );
    fdNegateRecipient.top = new FormAttachment( wSender, margin );
    fdNegateRecipient.right = new FormAttachment( 100, -margin );
    wNegateRecipient.setLayoutData( fdNegateRecipient );

    // Recipient line
    Label wlRecipient = new Label( wHeader, SWT.RIGHT );
    wlRecipient.setText( BaseMessages.getString( PKG, "JobGetPOP.Receipient.Label" ) );
    props.setLook( wlRecipient );
    FormData fdlRecipient = new FormData();
    fdlRecipient.left = new FormAttachment( 0, 0 );
    fdlRecipient.top = new FormAttachment( wSender, margin );
    fdlRecipient.right = new FormAttachment( middle, -margin );
    wlRecipient.setLayoutData( fdlRecipient );
    wRecipient = new TextVar( jobMeta, wHeader, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
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
    wNegateSubject.setToolTipText( BaseMessages.getString( PKG, "JobGetPOP.NegateSubject.Tooltip" ) );
    fdNegateSubject.top = new FormAttachment( wRecipient, margin );
    fdNegateSubject.right = new FormAttachment( 100, -margin );
    wNegateSubject.setLayoutData( fdNegateSubject );

    // Subject line
    Label wlSubject = new Label( wHeader, SWT.RIGHT );
    wlSubject.setText( BaseMessages.getString( PKG, "JobGetPOP.Subject.Label" ) );
    props.setLook( wlSubject );
    FormData fdlSubject = new FormData();
    fdlSubject.left = new FormAttachment( 0, 0 );
    fdlSubject.top = new FormAttachment( wRecipient, margin );
    fdlSubject.right = new FormAttachment( middle, -margin );
    wlSubject.setLayoutData( fdlSubject );
    wSubject = new TextVar( jobMeta, wHeader, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
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
    // / END OF HEADER GROUP
    // ///////////////////////////////////////////////////////////

    // ////////////////////////
    // START OF CONTENT GROUP
    // ////////////////////////
    Group wContent = new Group( wSearchComp, SWT.SHADOW_NONE );
    props.setLook( wContent );
    wContent.setText( BaseMessages.getString( PKG, "JobGetPOP.Content.Group.Label" ) );

    FormLayout contentGroupLayout = new FormLayout();
    contentGroupLayout.marginWidth = 10;
    contentGroupLayout.marginHeight = 10;
    wContent.setLayout( contentGroupLayout );

    wNegateBody = new Button( wContent, SWT.CHECK );
    props.setLook( wNegateBody );
    FormData fdNegateBody = new FormData();
    wNegateBody.setToolTipText( BaseMessages.getString( PKG, "JobGetPOP.NegateBody.Tooltip" ) );
    fdNegateBody.top = new FormAttachment( wHeader, margin );
    fdNegateBody.right = new FormAttachment( 100, -margin );
    wNegateBody.setLayoutData( fdNegateBody );

    // Body line
    Label wlBody = new Label( wContent, SWT.RIGHT );
    wlBody.setText( BaseMessages.getString( PKG, "JobGetPOP.Body.Label" ) );
    props.setLook( wlBody );
    FormData fdlBody = new FormData();
    fdlBody.left = new FormAttachment( 0, 0 );
    fdlBody.top = new FormAttachment( wHeader, margin );
    fdlBody.right = new FormAttachment( middle, -margin );
    wlBody.setLayoutData( fdlBody );
    wBody = new TextVar( jobMeta, wContent, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wBody );
    wBody.addModifyListener( lsMod );
    FormData fdBody = new FormData();
    fdBody.left = new FormAttachment( middle, 0 );
    fdBody.top = new FormAttachment( wHeader, margin );
    fdBody.right = new FormAttachment( wNegateBody, -margin );
    wBody.setLayoutData( fdBody );

    FormData fdContent = new FormData();
    fdContent.left = new FormAttachment( 0, margin );
    fdContent.top = new FormAttachment( wHeader, margin );
    fdContent.right = new FormAttachment( 100, -margin );
    wContent.setLayoutData( fdContent );
    // ///////////////////////////////////////////////////////////
    // / END OF CONTENT GROUP
    // ///////////////////////////////////////////////////////////

    // ////////////////////////
    // START OF RECEIVED DATE GROUP
    // ////////////////////////
    Group wReceivedDate = new Group( wSearchComp, SWT.SHADOW_NONE );
    props.setLook( wReceivedDate );
    wReceivedDate.setText( BaseMessages.getString( PKG, "JobGetPOP.ReceivedDate.Group.Label" ) );

    FormLayout receivedDateGroupLayout = new FormLayout();
    receivedDateGroupLayout.marginWidth = 10;
    receivedDateGroupLayout.marginHeight = 10;
    wReceivedDate.setLayout( receivedDateGroupLayout );

    wNegateReceivedDate = new Button( wReceivedDate, SWT.CHECK );
    props.setLook( wNegateReceivedDate );
    FormData fdNegateReceivedDate = new FormData();
    wNegateReceivedDate.setToolTipText( BaseMessages.getString( PKG, "JobGetPOP.NegateReceivedDate.Tooltip" ) );
    fdNegateReceivedDate.top = new FormAttachment( wContent, margin );
    fdNegateReceivedDate.right = new FormAttachment( 100, -margin );
    wNegateReceivedDate.setLayoutData( fdNegateReceivedDate );

    // Received Date Condition
    wlConditionOnReceivedDate = new Label( wReceivedDate, SWT.RIGHT );
    wlConditionOnReceivedDate.setText( BaseMessages.getString( PKG, "JobGetPOP.ConditionOnReceivedDate.Label" ) );
    props.setLook( wlConditionOnReceivedDate );
    FormData fdlConditionOnReceivedDate = new FormData();
    fdlConditionOnReceivedDate.left = new FormAttachment( 0, 0 );
    fdlConditionOnReceivedDate.right = new FormAttachment( middle, -margin );
    fdlConditionOnReceivedDate.top = new FormAttachment( wContent, margin );
    wlConditionOnReceivedDate.setLayoutData( fdlConditionOnReceivedDate );

    wConditionOnReceivedDate = new CCombo( wReceivedDate, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER );
    wConditionOnReceivedDate.setItems( MailConnectionMeta.conditionDateDesc );
    wConditionOnReceivedDate.select( 0 ); // +1: starts at -1

    props.setLook( wConditionOnReceivedDate );
    FormData fdConditionOnReceivedDate = new FormData();
    fdConditionOnReceivedDate.left = new FormAttachment( middle, 0 );
    fdConditionOnReceivedDate.top = new FormAttachment( wContent, margin );
    fdConditionOnReceivedDate.right = new FormAttachment( wNegateReceivedDate, -margin );
    wConditionOnReceivedDate.setLayoutData( fdConditionOnReceivedDate );
    wConditionOnReceivedDate.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent e ) {
        conditionReceivedDate();
        jobEntry.setChanged();
      }
    } );

    // This string is used multiple times
    String systemButtonOkText = BaseMessages.getString( PKG, "System.Button.OK" );

    open = new Button( wReceivedDate, SWT.PUSH );
    open.setImage( GUIResource.getInstance().getImageCalendar() );
    open.setToolTipText( BaseMessages.getString( PKG, "JobGetPOP.OpenCalendar" ) );
    FormData fdlButton = new FormData();
    fdlButton.top = new FormAttachment( wConditionOnReceivedDate, margin );
    fdlButton.right = new FormAttachment( 100, 0 );
    open.setLayoutData( fdlButton );
    open.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent e ) {
        final Shell dialog = new Shell( shell, SWT.DIALOG_TRIM );
        dialog.setText( BaseMessages.getString( PKG, "JobGetPOP.SelectDate" ) );
        dialog.setImage( GUIResource.getInstance().getImageSpoon() );
        dialog.setLayout( new GridLayout( 3, false ) );

        final DateTime calendar = new DateTime( dialog, SWT.CALENDAR );
        final DateTime time = new DateTime( dialog, SWT.TIME );
        new Label( dialog, SWT.NONE );
        new Label( dialog, SWT.NONE );

        Button ok = new Button( dialog, SWT.PUSH );
        ok.setText( systemButtonOkText );
        ok.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, false, false ) );
        ok.addSelectionListener( new SelectionAdapter() {
          @Override
          public void widgetSelected( SelectionEvent e ) {
            Calendar cal = Calendar.getInstance();
            cal.set( Calendar.YEAR, calendar.getYear() );
            cal.set( Calendar.MONTH, calendar.getMonth() );
            cal.set( Calendar.DAY_OF_MONTH, calendar.getDay() );
            cal.set( Calendar.HOUR_OF_DAY, time.getHours() );
            cal.set( Calendar.MINUTE, time.getMinutes() );
            cal.set( Calendar.SECOND, time.getSeconds() );

            wReadFrom.setText( new SimpleDateFormat( JobEntryGetPOP.DATE_PATTERN ).format( cal.getTime() ) );

            dialog.close();
          }
        } );
        dialog.setDefaultButton( ok );
        dialog.pack();
        dialog.open();
      }
    } );

    wlReadFrom = new Label( wReceivedDate, SWT.RIGHT );
    wlReadFrom.setText( BaseMessages.getString( PKG, "JobGetPOP.ReadFrom.Label" ) );
    props.setLook( wlReadFrom );
    FormData fdlReadFrom = new FormData();
    fdlReadFrom.left = new FormAttachment( 0, 0 );
    fdlReadFrom.top = new FormAttachment( wConditionOnReceivedDate, margin );
    fdlReadFrom.right = new FormAttachment( middle, -margin );
    wlReadFrom.setLayoutData( fdlReadFrom );
    wReadFrom = new TextVar( jobMeta, wReceivedDate, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    wReadFrom.setToolTipText( BaseMessages.getString( PKG, "JobGetPOP.ReadFrom.Tooltip" ) );
    props.setLook( wReadFrom );
    wReadFrom.addModifyListener( lsMod );
    FormData fdReadFrom = new FormData();
    fdReadFrom.left = new FormAttachment( middle, 0 );
    fdReadFrom.top = new FormAttachment( wConditionOnReceivedDate, margin );
    fdReadFrom.right = new FormAttachment( open, -margin );
    wReadFrom.setLayoutData( fdReadFrom );

    openTo = new Button( wReceivedDate, SWT.PUSH );
    openTo.setImage( GUIResource.getInstance().getImageCalendar() );
    openTo.setToolTipText( BaseMessages.getString( PKG, "JobGetPOP.OpenCalendar" ) );
    FormData fdlButtonTo = new FormData();
    fdlButtonTo.top = new FormAttachment( wReadFrom, 2 * margin );
    fdlButtonTo.right = new FormAttachment( 100, 0 );
    openTo.setLayoutData( fdlButtonTo );
    openTo.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent e ) {
        final Shell dialogTo = new Shell( shell, SWT.DIALOG_TRIM );
        dialogTo.setText( BaseMessages.getString( PKG, "JobGetPOP.SelectDate" ) );
        dialogTo.setImage( GUIResource.getInstance().getImageSpoon() );
        dialogTo.setLayout( new GridLayout( 3, false ) );

        final DateTime calendarTo = new DateTime( dialogTo, SWT.CALENDAR | SWT.BORDER );
        final DateTime timeTo = new DateTime( dialogTo, SWT.TIME );
        new Label( dialogTo, SWT.NONE );
        new Label( dialogTo, SWT.NONE );
        Button okTo = new Button( dialogTo, SWT.PUSH );
        okTo.setText( systemButtonOkText );
        okTo.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, false, false ) );
        okTo.addSelectionListener( new SelectionAdapter() {
          @Override
          public void widgetSelected( SelectionEvent e ) {
            Calendar cal = Calendar.getInstance();
            cal.set( Calendar.YEAR, calendarTo.getYear() );
            cal.set( Calendar.MONTH, calendarTo.getMonth() );
            cal.set( Calendar.DAY_OF_MONTH, calendarTo.getDay() );
            cal.set( Calendar.HOUR_OF_DAY, timeTo.getHours() );
            cal.set( Calendar.MINUTE, timeTo.getMinutes() );
            cal.set( Calendar.SECOND, timeTo.getSeconds() );

            wReadTo.setText( new SimpleDateFormat( JobEntryGetPOP.DATE_PATTERN ).format( cal.getTime() ) );
            dialogTo.close();
          }
        } );
        dialogTo.setDefaultButton( okTo );
        dialogTo.pack();
        dialogTo.open();
      }
    } );

    wlReadTo = new Label( wReceivedDate, SWT.RIGHT );
    wlReadTo.setText( BaseMessages.getString( PKG, "JobGetPOP.ReadTo.Label" ) );
    props.setLook( wlReadTo );
    FormData fdlReadTo = new FormData();
    fdlReadTo.left = new FormAttachment( 0, 0 );
    fdlReadTo.top = new FormAttachment( wReadFrom, 2 * margin );
    fdlReadTo.right = new FormAttachment( middle, -margin );
    wlReadTo.setLayoutData( fdlReadTo );
    wReadTo = new TextVar( jobMeta, wReceivedDate, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    wReadTo.setToolTipText( BaseMessages.getString( PKG, "JobGetPOP.ReadTo.Tooltip" ) );
    props.setLook( wReadTo );
    wReadTo.addModifyListener( lsMod );
    FormData fdReadTo = new FormData();
    fdReadTo.left = new FormAttachment( middle, 0 );
    fdReadTo.top = new FormAttachment( wReadFrom, 2 * margin );
    fdReadTo.right = new FormAttachment( openTo, -margin );
    wReadTo.setLayoutData( fdReadTo );

    FormData fdReceivedDate = new FormData();
    fdReceivedDate.left = new FormAttachment( 0, margin );
    fdReceivedDate.top = new FormAttachment( wContent, margin );
    fdReceivedDate.right = new FormAttachment( 100, -margin );
    wReceivedDate.setLayoutData( fdReceivedDate );
    // ///////////////////////////////////////////////////////////
    // / END OF RECEIVED DATE GROUP
    // ///////////////////////////////////////////////////////////

    FormData fdSearchComp = new FormData();
    fdSearchComp.left = new FormAttachment( 0, 0 );
    fdSearchComp.top = new FormAttachment( wName, 0 );
    fdSearchComp.right = new FormAttachment( 100, 0 );
    fdSearchComp.bottom = new FormAttachment( 100, 0 );
    wSearchComp.setLayoutData( fdSearchComp );

    wSearchComp.layout();
    wSearchTab.setControl( wSearchComp );
    props.setLook( wSearchComp );

    // ////////////////////////////////
    // / END OF SEARCH TAB
    // ////////////////////////////////

    FormData fdTabFolder = new FormData();
    fdTabFolder.left = new FormAttachment( 0, 0 );
    fdTabFolder.top = new FormAttachment( wName, margin );
    fdTabFolder.right = new FormAttachment( 100, 0 );
    fdTabFolder.bottom = new FormAttachment( 100, -50 );
    wTabFolder.setLayoutData( fdTabFolder );

    Button wOK = new Button( shell, SWT.PUSH );
    wOK.setText( systemButtonOkText );
    Button wCancel = new Button( shell, SWT.PUSH );
    wCancel.setText( BaseMessages.getString( PKG, "System.Button.Cancel" ) );

    BaseStepDialog.positionBottomButtons( shell, new Button[] { wOK, wCancel }, margin, wTabFolder );

    // Add listeners

    wCancel.addListener( SWT.Selection, e -> cancel() );
    wOK.addListener( SWT.Selection, e -> ok() );

    SelectionAdapter lsDef = new SelectionAdapter() {
      @Override
      public void widgetDefaultSelected( SelectionEvent e ) {
        ok();
      }
    };
    wTest.addListener( SWT.Selection, e -> test() );

    wTestIMAPFolder.addListener( SWT.Selection,
      e -> checkFolder( jobMeta.environmentSubstitute( wIMAPFolder.getText() ) ) );

    wTestMoveToFolder.addListener( SWT.Selection,
      e -> checkFolder( jobMeta.environmentSubstitute( wMoveToFolder.getText() ) ) );

    wSelectFolder.addListener( SWT.Selection, e1 -> selectFolder( wIMAPFolder ) );

    wSelectMoveToFolder.addListener( SWT.Selection, e -> selectFolder( wMoveToFolder ) );

    wName.addSelectionListener( lsDef );
    wServerName.addSelectionListener( lsDef );

    // Detect X or ALT-F4 or something that kills this window...
    shell.addShellListener( new ShellAdapter() {
      @Override
      public void shellClosed( ShellEvent e ) {
        cancel();
      }
    } );

    getData();
    setUserProxy();
    chooseListMails();
    setUseAuth();
    setUseGrantType();
    activeAttachmentFolder();
    refreshProtocol( false );
    conditionReceivedDate();
    wTabFolder.setSelection( 0 );
    BaseStepDialog.setSize( shell );

    shell.open();
    props.setDialogSize( shell, "JobEntryGetPOPDialogSize" );
    while ( !shell.isDisposed() ) {
      if ( !display.readAndDispatch() ) {
        display.sleep();
      }
    }
    return jobEntry;
  }

  private void setUserProxy() {
    wlProxyUsername.setEnabled( wUseProxy.getSelection() );
    wProxyUsername.setEnabled( wUseProxy.getSelection() );
  }

  private boolean connect() {
    String errorDescription = null;
    boolean retval = false;
    if ( mailConn != null && mailConn.isConnected() ) {
      retval = mailConn.isConnected();
    }

    if ( !retval ) {
      String realServer = jobMeta.environmentSubstitute( wServerName.getText() );
      String realUser = jobMeta.environmentSubstitute( wUserName.getText() );
      String realPass = jobEntry.getRealPassword( jobMeta.environmentSubstitute( wPassword.getText() ) );
      int realPort = Const.toInt( jobMeta.environmentSubstitute( wPort.getText() ), -1 );
      String realProxyUser = jobMeta.environmentSubstitute( wProxyUsername.getText() );
      if ( JobEntryGetPOP.AUTENTICATION_OAUTH.equals( wUseAuth.getText() ) ) {
        realPass = "Bearer " + jobEntry.getOauthToken( jobMeta.environmentSubstitute( wAuthTokenUrl.getText() ) )
          .getAccessToken();
      }

      try {
        mailConn = new MailConnection( jobMeta.getBowl(),
            LogChannel.UI, MailConnectionMeta.getProtocolFromString(
              wProtocol.getText(), MailConnectionMeta.PROTOCOL_IMAP ), realServer, realPort, realUser,
            realPass, wUseSSL.getSelection(), wUseProxy.getSelection(), realProxyUser );
        mailConn.connect();

        retval = true;
      } catch ( Exception e ) {
        errorDescription = e.getMessage();
      }
    }

    if ( !retval ) {
      MessageBox mb = new MessageBox( shell, SWT.OK | SWT.ICON_ERROR );
      mb.setMessage( BaseMessages.getString( PKG, "JobGetPOP.Connected.NOK.ConnectionBad", wServerName.getText() )
        + Const.CR + Const.NVL( errorDescription, "" ) );
      mb.setText( BaseMessages.getString( PKG, "JobGetPOP.Connected.Title.Bad" ) );
      mb.open();
    }

    return ( mailConn.isConnected() );
  }

  private void test() {
    if ( connect() ) {
      MessageBox mb = new MessageBox( shell, SWT.OK | SWT.ICON_INFORMATION );
      mb.setMessage( BaseMessages.getString( PKG, "JobGetPOP.Connected.OK", jobMeta.environmentSubstitute(wServerName.getText()) ) + Const.CR );
      mb.setText( BaseMessages.getString( PKG, "JobGetPOP.Connected.Title.Ok" ) );
      mb.open();
    }
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

  private void checkFolder( String folderName ) {
    if ( !Utils.isEmpty( folderName ) && connect() ) {
      // check folder
      if ( mailConn.folderExists( folderName ) ) {
        MessageBox mb = new MessageBox( shell, SWT.OK | SWT.ICON_INFORMATION );
        mb.setMessage( BaseMessages.getString( PKG, "JobGetPOP.IMAPFolderExists.OK", folderName ) + Const.CR );
        mb.setText( BaseMessages.getString( PKG, "JobGetPOP.IMAPFolderExists.Title.Ok" ) );
        mb.open();
      } else {
        MessageBox mb = new MessageBox( shell, SWT.OK | SWT.ICON_ERROR );
        mb.setMessage( BaseMessages.getString( PKG, "JobGetPOP.Connected.NOK.IMAPFolderExists", folderName )
          + Const.CR );
        mb.setText( BaseMessages.getString( PKG, "JobGetPOP.IMAPFolderExists.Title.Bad" ) );
        mb.open();
      }
    }
  }

  private void closeMailConnection() {
    try {
      if ( mailConn != null ) {
        mailConn.disconnect();
        mailConn = null;
      }
    } catch ( Exception e ) {
      // Ignore
    }
  }

  private void conditionReceivedDate() {
    boolean activeReceivedDate = MailConnectionMeta.getConditionDateByDesc( wConditionOnReceivedDate.getText() )
        != MailConnectionMeta.CONDITION_DATE_IGNORE;
    boolean useBetween = MailConnectionMeta.getConditionDateByDesc( wConditionOnReceivedDate.getText() )
        == MailConnectionMeta.CONDITION_DATE_BETWEEN;
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

  private void activeAttachmentFolder() {
    boolean getMessages =
      MailConnectionMeta.getActionTypeByDesc( wActionType.getText() ) == MailConnectionMeta.ACTION_TYPE_GET;
    wlDifferentFolderForAttachment.setEnabled( getMessages && wGetAttachment.getSelection() );
    wDifferentFolderForAttachment.setEnabled( getMessages && wGetAttachment.getSelection() );
    boolean activeAttachmentFolder = ( wGetAttachment.getSelection() && wDifferentFolderForAttachment.getSelection() );
    wlAttachmentFolder.setEnabled( getMessages && activeAttachmentFolder );
    wAttachmentFolder.setEnabled( getMessages && activeAttachmentFolder );
    wbAttachmentFolder.setEnabled( getMessages && activeAttachmentFolder );
    if ( !wGetAttachment.getSelection() && !wGetMessage.getSelection() ) {
      wGetMessage.setSelection( true );
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
      } else {
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
      }
    }
  }

  private void refreshProtocol( boolean refreshPort ) {
    checkUnavailableMode();
    boolean activePOP3 = wProtocol.getText().equals( MailConnectionMeta.PROTOCOL_STRING_POP3 );
    wlPOP3Message.setEnabled( activePOP3 );
    wlListMails.setEnabled( activePOP3 );
    wListMails.setEnabled( activePOP3 );
    wlFirstMails.setEnabled( activePOP3 );
    wlDelete.setEnabled( activePOP3 );
    wDelete.setEnabled( activePOP3 );

    wlIMAPFirstMails.setEnabled( !activePOP3 );
    wIMAPFirstMails.setEnabled( !activePOP3 );
    wlIMAPFolder.setEnabled( !activePOP3 );
    wIMAPFolder.setEnabled( !activePOP3 );
    wlIncludeSubFolders.setEnabled( !activePOP3 );
    wIncludeSubFolders.setEnabled( !activePOP3 );
    wlIMAPListMails.setEnabled( !activePOP3 );
    wIMAPListMails.setEnabled( !activePOP3 );
    wTestIMAPFolder.setEnabled( !activePOP3 );
    wSelectFolder.setEnabled( !activePOP3 );
    wlAfterGetIMAP.setEnabled( !activePOP3 );
    wAfterGetIMAP.setEnabled( !activePOP3 );

    if ( activePOP3 ) {
      // clear out selections
      wConditionOnReceivedDate.select( 0 );
      conditionReceivedDate();
    }
    // POP3 protocol does not provide information about when a message was received
    wConditionOnReceivedDate.setEnabled( !activePOP3 );
    wNegateReceivedDate.setEnabled( !activePOP3 );
    wlConditionOnReceivedDate.setEnabled( !activePOP3 );

    chooseListMails();
    refreshPort( refreshPort );
    setActionType();
  }

  private void checkUnavailableMode() {
    if ( wProtocol.getText().equals( MailConnectionMeta.PROTOCOL_STRING_POP3 )
      && MailConnectionMeta.getActionTypeByDesc( wActionType.getText() ) == MailConnectionMeta.ACTION_TYPE_MOVE ) {
      MessageBox mb = new MessageBox( shell, SWT.OK | SWT.ICON_ERROR );
      mb.setMessage( "This action is not available for POP3!"
        + Const.CR + "Only one Folder (INBOX) is available in POP3." + Const.CR
        + "If you want to move messages to another folder," + Const.CR + "please use IMAP protocol." );
      mb.setText( "ERROR" );
      mb.open();
      wActionType.setText( MailConnectionMeta.getActionTypeDesc( MailConnectionMeta.ACTION_TYPE_GET ) );
    }
  }

  private void setActionType() {
    checkUnavailableMode();
    if ( MailConnectionMeta.getActionTypeByDesc( wActionType.getText() ) != MailConnectionMeta.ACTION_TYPE_GET ) {
      wAfterGetIMAP.setText( MailConnectionMeta.getAfterGetIMAPDesc( MailConnectionMeta.AFTER_GET_IMAP_NOTHING ) );
    }

    boolean getMessages =
      MailConnectionMeta.getActionTypeByDesc( wActionType.getText() ) == MailConnectionMeta.ACTION_TYPE_GET;

    wlOutputDirectory.setEnabled( getMessages );
    wOutputDirectory.setEnabled( getMessages );
    wbDirectory.setEnabled( getMessages );
    wlCreateLocalFolder.setEnabled( getMessages );
    wCreateLocalFolder.setEnabled( getMessages );
    wFilenamePattern.setEnabled( getMessages );
    wlFilenamePattern.setEnabled( getMessages );
    wlAttachmentWildcard.setEnabled( getMessages );
    wAttachmentWildcard.setEnabled( getMessages );
    wlDifferentFolderForAttachment.setEnabled( getMessages );
    wDifferentFolderForAttachment.setEnabled( getMessages );
    wlGetAttachment.setEnabled( getMessages );
    wGetAttachment.setEnabled( getMessages );
    wlGetMessage.setEnabled( getMessages );
    wGetMessage.setEnabled( getMessages );

    wlAfterGetIMAP.setEnabled( getMessages && wProtocol.getText().equals( MailConnectionMeta.PROTOCOL_STRING_IMAP ) );
    wAfterGetIMAP.setEnabled( getMessages && wProtocol.getText().equals( MailConnectionMeta.PROTOCOL_STRING_IMAP ) );

    setAfterIMAPRetrieved();
  }

  private void setAfterIMAPRetrieved() {
    boolean activeMoveToFolfer =
      ( ( ( wProtocol.getText().equals( MailConnectionMeta.PROTOCOL_STRING_IMAP ) ) && ( MailConnectionMeta
        .getActionTypeByDesc( wActionType.getText() ) == MailConnectionMeta.ACTION_TYPE_MOVE ) ) || ( MailConnectionMeta
        .getAfterGetIMAPByDesc( wAfterGetIMAP.getText() ) == MailConnectionMeta.AFTER_GET_IMAP_MOVE ) );
    wlMoveToFolder.setEnabled( activeMoveToFolfer );
    wMoveToFolder.setEnabled( activeMoveToFolfer );
    wTestMoveToFolder.setEnabled( activeMoveToFolfer );
    wSelectMoveToFolder.setEnabled( activeMoveToFolfer );
    wlCreateMoveToFolder.setEnabled( activeMoveToFolfer );
    wCreateMoveToFolder.setEnabled( activeMoveToFolfer );
  }

  public void chooseListMails() {
    boolean ok =
      ( wProtocol.getText().equals( MailConnectionMeta.PROTOCOL_STRING_POP3 ) && wListMails.getSelectionIndex() == 1 );
    wlFirstMails.setEnabled( ok );
    wFirstMails.setEnabled( ok );
  }

  public void dispose() {
    closeMailConnection();
    props.setScreen( new WindowProperty( shell ) );
    shell.dispose();
  }

  /**
   * Copy information from the meta-data input to the dialog fields.
   */
  public void getData() {
    if ( jobEntry.getName() != null ) {
      wName.setText( jobEntry.getName() );
    }
    if ( jobEntry.getServerName() != null ) {
      wServerName.setText( jobEntry.getServerName() );
    }
    if ( jobEntry.getUserName() != null ) {
      wUserName.setText( jobEntry.getUserName() );
    }
    if ( jobEntry.getPassword() != null ) {
      wPassword.setText( jobEntry.getPassword() );
    }

    wUseSSL.setSelection( jobEntry.isUseSSL() );
    wGetMessage.setSelection( jobEntry.isSaveMessage() );
    wGetAttachment.setSelection( jobEntry.isSaveAttachment() );
    wDifferentFolderForAttachment.setSelection( jobEntry.isDifferentFolderForAttachment() );
    wAuthClientId.setText( Const.nullToEmpty( jobEntry.getClientId() ) );
    wAuthSecretKey.setText( Const.nullToEmpty( jobEntry.getSecretKey() ) );
    wAuthScope.setText( Const.nullToEmpty( jobEntry.getScope() ) );
    wAuthTokenUrl.setText( Const.nullToEmpty( jobEntry.getTokenUrl() ) );
    wAuthorizationCode.setText( Const.nullToEmpty( jobEntry.getAuthorization_code() ) );
    wRedirectUri.setText( Const.nullToEmpty( jobEntry.getRedirectUri() ) );
    wAuthRefreshToken.setText( Const.nullToEmpty( jobEntry.getRefresh_token() ) );
    grantType.setText( Const.nullToEmpty( jobEntry.getGrant_type() ) );
    wUseAuth.setText( Const.nullToEmpty( jobEntry.isUsingAuthentication() ) );
    if ( jobEntry.getAttachmentFolder() != null ) {
      wAttachmentFolder.setText( jobEntry.getAttachmentFolder() );
    }

    if ( jobEntry.getPort() != null ) {
      wPort.setText( jobEntry.getPort() );
    }

    if ( jobEntry.getOutputDirectory() != null ) {
      wOutputDirectory.setText( jobEntry.getOutputDirectory() );
    }
    if ( jobEntry.getFilenamePattern() != null ) {
      wFilenamePattern.setText( jobEntry.getFilenamePattern() );
    }
    if ( jobEntry.getAttachmentWildcard() != null ) {
      wAttachmentWildcard.setText( jobEntry.getAttachmentWildcard() );
    }

    String protocol = jobEntry.getProtocol();
    boolean isPop3 = MailConnectionMeta.PROTOCOL_STRING_POP3.equals( protocol );
    wProtocol.setText( protocol );
    int i = jobEntry.getRetrievemails();

    if ( i > 0 ) {
      if ( isPop3 ) {
        wListMails.select( i - 1 );
      } else {
        wListMails.select( i );
      }
    } else {
      wListMails.select( 0 ); // Retrieve All Mails
    }

    if ( jobEntry.getFirstMails() != null ) {
      wFirstMails.setText( jobEntry.getFirstMails() );
    }

    wDelete.setSelection( jobEntry.getDelete() );
    wIMAPListMails.setText( MailConnectionMeta.getValueImapListDesc( jobEntry.getValueImapList() ) );
    if ( jobEntry.getIMAPFolder() != null ) {
      wIMAPFolder.setText( jobEntry.getIMAPFolder() );
    }
    // search term
    if ( jobEntry.getSenderSearchTerm() != null ) {
      wSender.setText( jobEntry.getSenderSearchTerm() );
    }
    wNegateSender.setSelection( jobEntry.isNotTermSenderSearch() );
    if ( jobEntry.getReceipientSearch() != null ) {
      wRecipient.setText( jobEntry.getReceipientSearch() );
    }
    wNegateRecipient.setSelection( jobEntry.isNotTermReceipientSearch() );
    if ( jobEntry.getSubjectSearch() != null ) {
      wSubject.setText( jobEntry.getSubjectSearch() );
    }
    wNegateSubject.setSelection( jobEntry.isNotTermSubjectSearch() );
    if ( jobEntry.getBodySearch() != null ) {
      wBody.setText( jobEntry.getBodySearch() );
    }
    wNegateBody.setSelection( jobEntry.isNotTermBodySearch() );
    wConditionOnReceivedDate.setText( MailConnectionMeta.getConditionDateDesc( jobEntry
      .getConditionOnReceivedDate() ) );
    wNegateReceivedDate.setSelection( jobEntry.isNotTermReceivedDateSearch() );
    if ( jobEntry.getReceivedDate1() != null ) {
      wReadFrom.setText( jobEntry.getReceivedDate1() );
    }
    if ( jobEntry.getReceivedDate2() != null ) {
      wReadTo.setText( jobEntry.getReceivedDate2() );
    }
    wActionType.setText( MailConnectionMeta.getActionTypeDesc( jobEntry.getActionType() ) );
    wCreateMoveToFolder.setSelection( jobEntry.isCreateMoveToFolder() );
    wCreateLocalFolder.setSelection( jobEntry.isCreateLocalFolder() );
    if ( jobEntry.getMoveToIMAPFolder() != null ) {
      wMoveToFolder.setText( jobEntry.getMoveToIMAPFolder() );
    }
    wAfterGetIMAP.setText( MailConnectionMeta.getAfterGetIMAPDesc( jobEntry.getAfterGetIMAP() ) );
    wIncludeSubFolders.setSelection( jobEntry.isIncludeSubFolders() );
    wUseProxy.setSelection( jobEntry.isUseProxy() );
    if ( jobEntry.getProxyUsername() != null ) {
      wProxyUsername.setText( jobEntry.getProxyUsername() );
    }
    if ( jobEntry.getFirstIMAPMails() != null ) {
      wIMAPFirstMails.setText( jobEntry.getFirstIMAPMails() );
    }

    wName.selectAll();
    wName.setFocus();
  }

  private void cancel() {
    jobEntry.setChanged( changed );
    jobEntry = null;
    dispose();
  }

  private void ok() {
    if ( Utils.isEmpty( wName.getText() ) ) {
      MessageBox mb = new MessageBox( shell, SWT.OK | SWT.ICON_ERROR );
      mb.setMessage( BaseMessages.getString( PKG, "JobGetPOP.NoNameMessageBox.Message" ) );
      mb.setText( BaseMessages.getString( PKG, "JobGetPOP.NoNameMessageBox.Text" ) );
      mb.open();
      return;
    }
    jobEntry.setName( wName.getText() );
    jobEntry.setClientId( wAuthClientId.getText() );
    jobEntry.setSecretKey( wAuthSecretKey.getText() );
    jobEntry.setScope( wAuthScope.getText() );
    jobEntry.setTokenUrl( wAuthTokenUrl.getText() );
    jobEntry.setAuthorization_code( wAuthorizationCode.getText() );
    jobEntry.setRedirectUri( wRedirectUri.getText() );
    jobEntry.setRefresh_token( wAuthRefreshToken.getText() );
    jobEntry.setUsingAuthentication( wUseAuth.getText() );
    jobEntry.setGrant_type( grantType.getText() );
    jobEntry.setServerName( wServerName.getText() );
    jobEntry.setUserName( wUserName.getText() );
    jobEntry.setPassword( wPassword.getText() );
    jobEntry.setUseSSL( wUseSSL.getSelection() );
    jobEntry.setSaveAttachment( wGetAttachment.getSelection() );
    jobEntry.setSaveMessage( wGetMessage.getSelection() );
    jobEntry.setDifferentFolderForAttachment( wDifferentFolderForAttachment.getSelection() );
    jobEntry.setAttachmentFolder( wAttachmentFolder.getText() );
    jobEntry.setPort( wPort.getText() );
    jobEntry.setOutputDirectory( wOutputDirectory.getText() );
    jobEntry.setFilenamePattern( wFilenamePattern.getText() );

    // [PDI-7241] Option 'retrieve unread' is removed and there is only 2 options.
    // for backward compatibility: 0 is 'retrieve all', 1 is 'retrieve first...'
    int actualIndex = wListMails.getSelectionIndex();
    jobEntry.setRetrievemails( actualIndex > 0 ? 2 : 0 );

    jobEntry.setFirstMails( wFirstMails.getText() );
    jobEntry.setDelete( wDelete.getSelection() );
    jobEntry.setProtocol( wProtocol.getText() );
    jobEntry.setAttachmentWildcard( wAttachmentWildcard.getText() );
    jobEntry.setValueImapList( MailConnectionMeta.getValueImapListByDesc( wIMAPListMails.getText() ) );
    jobEntry.setFirstIMAPMails( wIMAPFirstMails.getText() );
    jobEntry.setIMAPFolder( wIMAPFolder.getText() );
    // search term
    jobEntry.setSenderSearchTerm( wSender.getText() );
    jobEntry.setNotTermSenderSearch( wNegateSender.getSelection() );

    jobEntry.setReceipientSearch( wRecipient.getText() );
    jobEntry.setNotTermReceipientSearch( wNegateRecipient.getSelection() );
    jobEntry.setSubjectSearch( wSubject.getText() );
    jobEntry.setNotTermSubjectSearch( wNegateSubject.getSelection() );
    jobEntry.setBodySearch( wBody.getText() );
    jobEntry.setNotTermBodySearch( wNegateBody.getSelection() );
    jobEntry.setConditionOnReceivedDate( MailConnectionMeta.getConditionDateByDesc( wConditionOnReceivedDate
      .getText() ) );
    jobEntry.setNotTermReceivedDateSearch( wNegateReceivedDate.getSelection() );
    jobEntry.setReceivedDate1( wReadFrom.getText() );
    jobEntry.setReceivedDate2( wReadTo.getText() );
    jobEntry.setActionType( MailConnectionMeta.getActionTypeByDesc( wActionType.getText() ) );
    jobEntry.setMoveToIMAPFolder( wMoveToFolder.getText() );
    jobEntry.setCreateMoveToFolder( wCreateMoveToFolder.getSelection() );
    jobEntry.setCreateLocalFolder( wCreateLocalFolder.getSelection() );
    jobEntry.setAfterGetIMAP( MailConnectionMeta.getAfterGetIMAPByDesc( wAfterGetIMAP.getText() ) );
    jobEntry.setIncludeSubFolders( wIncludeSubFolders.getSelection() );
    jobEntry.setUseProxy( wUseProxy.getSelection() );
    jobEntry.setProxyUsername( wProxyUsername.getText() );
    dispose();
  }

  public boolean evaluates() {
    return true;
  }

  public boolean isUnconditional() {
    return false;
  }
}
