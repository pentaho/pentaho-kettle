// CHECKSTYLE:FileLength:OFF
/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2023 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.ui.job.entries.getpop;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import javax.mail.Folder;
import org.apache.commons.lang.StringUtils;
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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Props;
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
import org.pentaho.di.ui.core.widget.PasswordTextVar;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.job.dialog.JobDialog;
import org.pentaho.di.ui.job.entry.JobEntryDialog;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.core.annotations.PluginDialog;

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

  private Label wlName;

  private Text wName;

  private FormData fdlName, fdName;

  private Label wlServerName;

  private TextVar wServerName;

  private FormData fdlServerName, fdServerName;

  private Label wlSender;

  private TextVar wSender;

  private FormData fdlSender, fdSender;

  private Label wlReceipient;

  private TextVar wReceipient;

  private FormData fdlReceipient, fdReceipient;

  private Label wlSubject;

  private TextVar wSubject;

  private FormData fdlSubject, fdSubject;

  private Label wlBody;

  private TextVar wBody;

  private FormData fdlBody, fdBody;

  private Label wlAttachmentFolder;

  private TextVar wAttachmentFolder;

  private FormData fdlAttachmentFolder, fdAttachmentFolder;

  private Button wbAttachmentFolder;

  private FormData fdbAttachmentFolder;

  private Label wlAttachmentWildcard;

  private TextVar wAttachmentWildcard;

  private FormData fdlAttachmentWildcard, fdAttachmentWildcard;

  private Label wlUserName;

  private TextVar wUserName;

  private FormData fdlUserName, fdUserName;

  private Label wlIMAPFolder;

  private TextVar wIMAPFolder;

  private FormData fdlIMAPFolder, fdIMAPFolder;

  private Label wlMoveToFolder;

  private TextVar wMoveToFolder;

  private FormData fdlMoveToFolder, fdMoveToFolder;

  private Button wSelectMoveToFolder;

  private FormData fdSelectMoveToFolder;

  private Button wTestMoveToFolder;

  private FormData fdTestMoveToFolder;

  private Label wlPassword;

  private TextVar wPassword;

  private FormData fdlPassword, fdPassword;

  private Label wlOutputDirectory;

  private TextVar wOutputDirectory;

  private FormData fdlOutputDirectory, fdOutputDirectory;

  private Label wlFilenamePattern;

  private TextVar wFilenamePattern;

  private FormData fdlFilenamePattern, fdFilenamePattern;

  private Button wbDirectory;

  private FormData fdbDirectory;

  private Label wlListmails;

  private CCombo wListmails;

  private FormData fdlListmails, fdListmails;

  private Label wlIMAPListmails;

  private CCombo wIMAPListmails;

  private FormData fdlIMAPListmails, fdIMAPListmails;

  private Label wlAfterGetIMAP;

  private CCombo wAfterGetIMAP;

  private FormData fdlAfterGetIMAP, fdAfterGetIMAP;

  private Label wlFirstmails;

  private TextVar wFirstmails;

  private FormData fdlFirstmails, fdFirstmails;

  private Label wlIMAPFirstmails;

  private TextVar wIMAPFirstmails;

  private FormData fdlIMAPFirstmails, fdIMAPFirstmails;

  private Label wlPort;

  private TextVar wPort;

  private FormData fdlPort, fdPort;

  private Label wlUseSSL;

  private Button wUseSSL;

  private FormData fdlUseSSL, fdUseSSL;

  private Label wlUseProxy;

  private Button wUseProxy;

  private FormData fdlUseProxy, fdUseProxy;

  private Label wlProxyUsername;

  private TextVar wProxyUsername;

  private FormData fdlProxyUsername, fdProxyUsername;

  private Label wlIncludeSubFolders;

  private Button wIncludeSubFolders;

  private FormData fdlIncludeSubFolders, fdIncludeSubFolders;

  private Label wlcreateMoveToFolder;

  private Button wcreateMoveToFolder;

  private FormData fdlcreateMoveToFolder, fdcreateMoveToFolder;

  private Label wlcreateLocalFolder;

  private Button wcreateLocalFolder;

  private FormData fdlcreateLocalFolder, fdcreateLocalFolder;

  private Button wNegateSender;

  private FormData fdNegateSender;

  private Button wNegateReceipient;

  private FormData fdNegateReceipient;

  private Button wNegateSubject;

  private FormData fdNegateSubject;

  private Button wNegateBody;

  private FormData fdNegateBody;

  private Button wNegateReceivedDate;

  private FormData fdNegateReceivedDate;

  private Label wlGetAttachment;

  private Button wGetAttachment;

  private FormData fdlGetAttachment, fdGetAttachment;

  private Label wlGetMessage;

  private Button wGetMessage;

  private FormData fdlGetMessage, fdGetMessage;

  private Label wlDifferentFolderForAttachment;

  private Button wDifferentFolderForAttachment;

  private FormData fdlDifferentFolderForAttachment, fdDifferentFolderForAttachment;

  private Label wlPOP3Message;

  private FormData fdlPOP3Message;

  private Label wlDelete;

  private Button wDelete;

  private FormData fdlDelete, fdDelete;

  private Button wOK, wCancel;

  private Listener lsOK, lsCancel;

  private JobEntryGetPOP jobEntry;

  private Shell shell;

  private SelectionAdapter lsDef;

  private boolean changed;

  private CTabFolder wTabFolder;

  private Composite wGeneralComp, wSettingsComp, wSearchComp;

  private CTabItem wGeneralTab, wSettingsTab, wSearchTab;

  private FormData fdGeneralComp, fdSettingsComp, fdSearchComp;

  private FormData fdTabFolder;

  private Group wServerSettings, wPOP3Settings, wIMAPSettings, wReceivedDate, wHeader, wContent;

  private FormData fdServerSettings, fdPOP3Settings, fdIMAPSettings, fdReceivedDate, fdHeader, fdContent;

  private Label wlReadFrom;

  private TextVar wReadFrom;

  private FormData fdlReadFrom, fdReadFrom;

  private Button open;

  private Label wlConditionOnReceivedDate;

  private CCombo wConditionOnReceivedDate;

  private FormData fdlConditionOnReceivedDate, fdConditionOnReceivedDate;

  private Label wlActionType;

  private CCombo wActionType;

  private FormData fdlActionType, fdActionType;

  private Label wlReadTo;

  private TextVar wReadTo;

  private FormData fdlReadTo, fdReadTo;

  private Button opento;

  private Group wTargetFolder;

  private FormData fdTargetFolder;

  private Label wlProtocol;

  private CCombo wProtocol;

  private FormData fdlProtocol, fdProtocol;

  private Button wTest;

  private FormData fdTest;

  private Listener lsTest;

  private Button wTestIMAPFolder;

  private FormData fdTestIMAPFolder;

  private Listener lsTestIMAPFolder;

  private Button wSelectFolder;

  private FormData fdSelectFolder;

  private Listener lsSelectFolder;

  private Listener lsTestMoveToFolder;

  private Listener lsSelectMoveToFolder;

  private MailConnection mailConn = null;

  public JobEntryGetPOPDialog( Shell parent, JobEntryInterface jobEntryInt, Repository rep, JobMeta jobMeta ) {
    super( parent, jobEntryInt, rep, jobMeta );
    jobEntry = (JobEntryGetPOP) jobEntryInt;
    if ( this.jobEntry.getName() == null ) {
      this.jobEntry.setName( BaseMessages.getString( PKG, "JobGetPOP.Name.Default" ) );
    }
  }

  public JobEntryInterface open() {
    Shell parent = getParent();
    Display display = parent.getDisplay();

    shell = new Shell( parent, props.getJobsDialogStyle() );
    props.setLook( shell );
    JobDialog.setShellImage( shell, jobEntry );

    ModifyListener lsMod = new ModifyListener() {
      public void modifyText( ModifyEvent e ) {
        closeMailConnection();
        jobEntry.setChanged();
      }
    };

    SelectionListener lsSelection = new SelectionAdapter() {
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
    wlName = new Label( shell, SWT.RIGHT );
    wlName.setText( BaseMessages.getString( PKG, "JobGetPOP.Name.Label" ) );
    props.setLook( wlName );
    fdlName = new FormData();
    fdlName.left = new FormAttachment( 0, 0 );
    fdlName.right = new FormAttachment( middle, -margin );
    fdlName.top = new FormAttachment( 0, margin );
    wlName.setLayoutData( fdlName );
    wName = new Text( shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wName );
    wName.addModifyListener( lsMod );
    fdName = new FormData();
    fdName.left = new FormAttachment( middle, 0 );
    fdName.top = new FormAttachment( 0, margin );
    fdName.right = new FormAttachment( 100, 0 );
    wName.setLayoutData( fdName );

    wTabFolder = new CTabFolder( shell, SWT.BORDER );
    props.setLook( wTabFolder, Props.WIDGET_STYLE_TAB );

    // ////////////////////////
    // START OF GENERAL TAB ///
    // ////////////////////////

    wGeneralTab = new CTabItem( wTabFolder, SWT.NONE );
    wGeneralTab.setText( BaseMessages.getString( PKG, "JobGetPOP.Tab.General.Label" ) );
    wGeneralComp = new Composite( wTabFolder, SWT.NONE );
    props.setLook( wGeneralComp );
    FormLayout generalLayout = new FormLayout();
    generalLayout.marginWidth = 3;
    generalLayout.marginHeight = 3;
    wGeneralComp.setLayout( generalLayout );

    // ////////////////////////
    // START OF SERVER SETTINGS GROUP///
    // /
    wServerSettings = new Group( wGeneralComp, SWT.SHADOW_NONE );
    props.setLook( wServerSettings );
    wServerSettings.setText( BaseMessages.getString( PKG, "JobGetPOP.ServerSettings.Group.Label" ) );

    FormLayout ServerSettingsgroupLayout = new FormLayout();
    ServerSettingsgroupLayout.marginWidth = 10;
    ServerSettingsgroupLayout.marginHeight = 10;
    wServerSettings.setLayout( ServerSettingsgroupLayout );

    // ServerName line
    wlServerName = new Label( wServerSettings, SWT.RIGHT );
    wlServerName.setText( BaseMessages.getString( PKG, "JobGetPOP.Server.Label" ) );
    props.setLook( wlServerName );
    fdlServerName = new FormData();
    fdlServerName.left = new FormAttachment( 0, 0 );
    fdlServerName.top = new FormAttachment( 0, 2 * margin );
    fdlServerName.right = new FormAttachment( middle, -margin );
    wlServerName.setLayoutData( fdlServerName );
    wServerName = new TextVar( jobMeta, wServerSettings, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wServerName );
    wServerName.addModifyListener( lsMod );
    fdServerName = new FormData();
    fdServerName.left = new FormAttachment( middle, 0 );
    fdServerName.top = new FormAttachment( 0, 2 * margin );
    fdServerName.right = new FormAttachment( 100, 0 );
    wServerName.setLayoutData( fdServerName );

    // USE connection with SSL
    wlUseSSL = new Label( wServerSettings, SWT.RIGHT );
    wlUseSSL.setText( BaseMessages.getString( PKG, "JobGetPOP.UseSSLMails.Label" ) );
    props.setLook( wlUseSSL );
    fdlUseSSL = new FormData();
    fdlUseSSL.left = new FormAttachment( 0, 0 );
    fdlUseSSL.top = new FormAttachment( wServerName, margin );
    fdlUseSSL.right = new FormAttachment( middle, -margin );
    wlUseSSL.setLayoutData( fdlUseSSL );
    wUseSSL = new Button( wServerSettings, SWT.CHECK );
    props.setLook( wUseSSL );
    fdUseSSL = new FormData();
    wUseSSL.setToolTipText( BaseMessages.getString( PKG, "JobGetPOP.UseSSLMails.Tooltip" ) );
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
    wlPort.setText( BaseMessages.getString( PKG, "JobGetPOP.SSLPort.Label" ) );
    props.setLook( wlPort );
    fdlPort = new FormData();
    fdlPort.left = new FormAttachment( 0, 0 );
    fdlPort.top = new FormAttachment( wUseSSL, margin );
    fdlPort.right = new FormAttachment( middle, -margin );
    wlPort.setLayoutData( fdlPort );
    wPort = new TextVar( jobMeta, wServerSettings, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wPort );
    wPort.setToolTipText( BaseMessages.getString( PKG, "JobGetPOP.SSLPort.Tooltip" ) );
    wPort.addModifyListener( lsMod );
    fdPort = new FormData();
    fdPort.left = new FormAttachment( middle, 0 );
    fdPort.top = new FormAttachment( wUseSSL, margin );
    fdPort.right = new FormAttachment( 100, 0 );
    wPort.setLayoutData( fdPort );

    // UserName line
    wlUserName = new Label( wServerSettings, SWT.RIGHT );
    wlUserName.setText( BaseMessages.getString( PKG, "JobGetPOP.Username.Label" ) );
    props.setLook( wlUserName );
    fdlUserName = new FormData();
    fdlUserName.left = new FormAttachment( 0, 0 );
    fdlUserName.top = new FormAttachment( wPort, margin );
    fdlUserName.right = new FormAttachment( middle, -margin );
    wlUserName.setLayoutData( fdlUserName );
    wUserName = new TextVar( jobMeta, wServerSettings, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wUserName );
    wUserName.setToolTipText( BaseMessages.getString( PKG, "JobGetPOP.Username.Tooltip" ) );
    wUserName.addModifyListener( lsMod );
    fdUserName = new FormData();
    fdUserName.left = new FormAttachment( middle, 0 );
    fdUserName.top = new FormAttachment( wPort, margin );
    fdUserName.right = new FormAttachment( 100, 0 );
    wUserName.setLayoutData( fdUserName );

    // Password line
    wlPassword = new Label( wServerSettings, SWT.RIGHT );
    wlPassword.setText( BaseMessages.getString( PKG, "JobGetPOP.Password.Label" ) );
    props.setLook( wlPassword );
    fdlPassword = new FormData();
    fdlPassword.left = new FormAttachment( 0, 0 );
    fdlPassword.top = new FormAttachment( wUserName, margin );
    fdlPassword.right = new FormAttachment( middle, -margin );
    wlPassword.setLayoutData( fdlPassword );
    wPassword = new PasswordTextVar( jobMeta, wServerSettings, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wPassword );
    wPassword.addModifyListener( lsMod );
    fdPassword = new FormData();
    fdPassword.left = new FormAttachment( middle, 0 );
    fdPassword.top = new FormAttachment( wUserName, margin );
    fdPassword.right = new FormAttachment( 100, 0 );
    wPassword.setLayoutData( fdPassword );

    // USE proxy
    wlUseProxy = new Label( wServerSettings, SWT.RIGHT );
    wlUseProxy.setText( BaseMessages.getString( PKG, "JobGetPOP.UseProxyMails.Label" ) );
    props.setLook( wlUseProxy );
    fdlUseProxy = new FormData();
    fdlUseProxy.left = new FormAttachment( 0, 0 );
    fdlUseProxy.top = new FormAttachment( wPassword, 2 * margin );
    fdlUseProxy.right = new FormAttachment( middle, -margin );
    wlUseProxy.setLayoutData( fdlUseProxy );
    wUseProxy = new Button( wServerSettings, SWT.CHECK );
    props.setLook( wUseProxy );
    fdUseProxy = new FormData();
    wUseProxy.setToolTipText( BaseMessages.getString( PKG, "JobGetPOP.UseProxyMails.Tooltip" ) );
    fdUseProxy.left = new FormAttachment( middle, 0 );
    fdUseProxy.top = new FormAttachment( wPassword, 2 * margin );
    fdUseProxy.right = new FormAttachment( 100, 0 );
    wUseProxy.setLayoutData( fdUseProxy );

    wUseProxy.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e ) {
        setUserProxy();
        jobEntry.setChanged();
      }
    } );

    // ProxyUsername line
    wlProxyUsername = new Label( wServerSettings, SWT.RIGHT );
    wlProxyUsername.setText( BaseMessages.getString( PKG, "JobGetPOP.ProxyUsername.Label" ) );
    props.setLook( wlProxyUsername );
    fdlProxyUsername = new FormData();
    fdlProxyUsername.left = new FormAttachment( 0, 0 );
    fdlProxyUsername.top = new FormAttachment( wUseProxy, margin );
    fdlProxyUsername.right = new FormAttachment( middle, -margin );
    wlProxyUsername.setLayoutData( fdlProxyUsername );
    wProxyUsername = new TextVar( jobMeta, wServerSettings, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wProxyUsername );
    wProxyUsername.setToolTipText( BaseMessages.getString( PKG, "JobGetPOP.ProxyUsername.Tooltip" ) );
    wProxyUsername.addModifyListener( lsMod );
    fdProxyUsername = new FormData();
    fdProxyUsername.left = new FormAttachment( middle, 0 );
    fdProxyUsername.top = new FormAttachment( wUseProxy, margin );
    fdProxyUsername.right = new FormAttachment( 100, 0 );
    wProxyUsername.setLayoutData( fdProxyUsername );

    // Protocol
    wlProtocol = new Label( wServerSettings, SWT.RIGHT );
    wlProtocol.setText( BaseMessages.getString( PKG, "JobGetPOP.Protocol.Label" ) );
    props.setLook( wlProtocol );
    fdlProtocol = new FormData();
    fdlProtocol.left = new FormAttachment( 0, 0 );
    fdlProtocol.right = new FormAttachment( middle, -margin );
    fdlProtocol.top = new FormAttachment( wProxyUsername, margin );
    wlProtocol.setLayoutData( fdlProtocol );
    wProtocol = new CCombo( wServerSettings, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER );
    wProtocol.setItems( MailConnectionMeta.protocolCodes );
    wProtocol.select( 0 );
    props.setLook( wProtocol );
    fdProtocol = new FormData();
    fdProtocol.left = new FormAttachment( middle, 0 );
    fdProtocol.top = new FormAttachment( wProxyUsername, margin );
    fdProtocol.right = new FormAttachment( 100, 0 );
    wProtocol.setLayoutData( fdProtocol );
    wProtocol.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e ) {
        refreshProtocol( true );

      }
    } );

    // Test connection button
    wTest = new Button( wServerSettings, SWT.PUSH );
    wTest.setText( BaseMessages.getString( PKG, "JobGetPOP.TestConnection.Label" ) );
    props.setLook( wTest );
    fdTest = new FormData();
    wTest.setToolTipText( BaseMessages.getString( PKG, "JobGetPOP.TestConnection.Tooltip" ) );
    // fdTest.left = new FormAttachment(middle, 0);
    fdTest.top = new FormAttachment( wProtocol, margin );
    fdTest.right = new FormAttachment( 100, 0 );
    wTest.setLayoutData( fdTest );

    fdServerSettings = new FormData();
    fdServerSettings.left = new FormAttachment( 0, margin );
    fdServerSettings.top = new FormAttachment( wProtocol, margin );
    fdServerSettings.right = new FormAttachment( 100, -margin );
    wServerSettings.setLayoutData( fdServerSettings );
    // ///////////////////////////////////////////////////////////
    // / END OF SERVER SETTINGS GROUP
    // ///////////////////////////////////////////////////////////

    // ////////////////////////
    // START OF Target Folder GROUP///
    // /
    wTargetFolder = new Group( wGeneralComp, SWT.SHADOW_NONE );
    props.setLook( wTargetFolder );
    wTargetFolder.setText( BaseMessages.getString( PKG, "JobGetPOP.TargetFolder.Group.Label" ) );

    FormLayout TargetFoldergroupLayout = new FormLayout();
    TargetFoldergroupLayout.marginWidth = 10;
    TargetFoldergroupLayout.marginHeight = 10;
    wTargetFolder.setLayout( TargetFoldergroupLayout );

    // OutputDirectory line
    wlOutputDirectory = new Label( wTargetFolder, SWT.RIGHT );
    wlOutputDirectory.setText( BaseMessages.getString( PKG, "JobGetPOP.OutputDirectory.Label" ) );
    props.setLook( wlOutputDirectory );
    fdlOutputDirectory = new FormData();
    fdlOutputDirectory.left = new FormAttachment( 0, 0 );
    fdlOutputDirectory.top = new FormAttachment( wServerSettings, margin );
    fdlOutputDirectory.right = new FormAttachment( middle, -margin );
    wlOutputDirectory.setLayoutData( fdlOutputDirectory );

    // Browse Source folders button ...
    wbDirectory = new Button( wTargetFolder, SWT.PUSH | SWT.CENTER );
    props.setLook( wbDirectory );
    wbDirectory.setText( BaseMessages.getString( PKG, "JobGetPOP.BrowseFolders.Label" ) );
    fdbDirectory = new FormData();
    fdbDirectory.right = new FormAttachment( 100, -margin );
    fdbDirectory.top = new FormAttachment( wServerSettings, margin );
    wbDirectory.setLayoutData( fdbDirectory );

    wOutputDirectory = new TextVar( jobMeta, wTargetFolder, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wOutputDirectory );
    wOutputDirectory.setToolTipText( BaseMessages.getString( PKG, "JobGetPOP.OutputDirectory.Tooltip" ) );
    wOutputDirectory.addModifyListener( lsMod );
    fdOutputDirectory = new FormData();
    fdOutputDirectory.left = new FormAttachment( middle, 0 );
    fdOutputDirectory.top = new FormAttachment( wServerSettings, margin );
    fdOutputDirectory.right = new FormAttachment( wbDirectory, -margin );
    wOutputDirectory.setLayoutData( fdOutputDirectory );

    wbDirectory.addSelectionListener( new SelectionAdapterFileDialogTextVar( jobMeta.getLogChannel(), wOutputDirectory, jobMeta,
      new SelectionAdapterOptions( SelectionOperation.FOLDER ) ) );

    // Create local folder
    wlcreateLocalFolder = new Label( wTargetFolder, SWT.RIGHT );
    wlcreateLocalFolder.setText( BaseMessages.getString( PKG, "JobGetPOP.createLocalFolder.Label" ) );
    props.setLook( wlcreateLocalFolder );
    fdlcreateLocalFolder = new FormData();
    fdlcreateLocalFolder.left = new FormAttachment( 0, 0 );
    fdlcreateLocalFolder.top = new FormAttachment( wOutputDirectory, margin );
    fdlcreateLocalFolder.right = new FormAttachment( middle, -margin );
    wlcreateLocalFolder.setLayoutData( fdlcreateLocalFolder );
    wcreateLocalFolder = new Button( wTargetFolder, SWT.CHECK );
    props.setLook( wcreateLocalFolder );
    fdcreateLocalFolder = new FormData();
    wcreateLocalFolder.setToolTipText( BaseMessages.getString( PKG, "JobGetPOP.createLocalFolder.Tooltip" ) );
    fdcreateLocalFolder.left = new FormAttachment( middle, 0 );
    fdcreateLocalFolder.top = new FormAttachment( wOutputDirectory, margin );
    fdcreateLocalFolder.right = new FormAttachment( 100, 0 );
    wcreateLocalFolder.setLayoutData( fdcreateLocalFolder );

    // Filename pattern line
    wlFilenamePattern = new Label( wTargetFolder, SWT.RIGHT );
    wlFilenamePattern.setText( BaseMessages.getString( PKG, "JobGetPOP.FilenamePattern.Label" ) );
    props.setLook( wlFilenamePattern );
    fdlFilenamePattern = new FormData();
    fdlFilenamePattern.left = new FormAttachment( 0, 0 );
    fdlFilenamePattern.top = new FormAttachment( wcreateLocalFolder, margin );
    fdlFilenamePattern.right = new FormAttachment( middle, -margin );
    wlFilenamePattern.setLayoutData( fdlFilenamePattern );
    wFilenamePattern = new TextVar( jobMeta, wTargetFolder, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wFilenamePattern );
    wFilenamePattern.setToolTipText( BaseMessages.getString( PKG, "JobGetPOP.FilenamePattern.Tooltip" ) );
    wFilenamePattern.addModifyListener( lsMod );
    fdFilenamePattern = new FormData();
    fdFilenamePattern.left = new FormAttachment( middle, 0 );
    fdFilenamePattern.top = new FormAttachment( wcreateLocalFolder, margin );
    fdFilenamePattern.right = new FormAttachment( 100, 0 );
    wFilenamePattern.setLayoutData( fdFilenamePattern );

    // Whenever something changes, set the tooltip to the expanded version:
    wFilenamePattern.addModifyListener( new ModifyListener() {
      public void modifyText( ModifyEvent e ) {
        wFilenamePattern.setToolTipText( jobMeta.environmentSubstitute( wFilenamePattern.getText() ) );
      }
    } );

    // Get message?
    wlGetMessage = new Label( wTargetFolder, SWT.RIGHT );
    wlGetMessage.setText( BaseMessages.getString( PKG, "JobGetPOP.GetMessageMails.Label" ) );
    props.setLook( wlGetMessage );
    fdlGetMessage = new FormData();
    fdlGetMessage.left = new FormAttachment( 0, 0 );
    fdlGetMessage.top = new FormAttachment( wFilenamePattern, margin );
    fdlGetMessage.right = new FormAttachment( middle, -margin );
    wlGetMessage.setLayoutData( fdlGetMessage );
    wGetMessage = new Button( wTargetFolder, SWT.CHECK );
    props.setLook( wGetMessage );
    fdGetMessage = new FormData();
    wGetMessage.setToolTipText( BaseMessages.getString( PKG, "JobGetPOP.GetMessageMails.Tooltip" ) );
    fdGetMessage.left = new FormAttachment( middle, 0 );
    fdGetMessage.top = new FormAttachment( wFilenamePattern, margin );
    fdGetMessage.right = new FormAttachment( 100, 0 );
    wGetMessage.setLayoutData( fdGetMessage );

    wGetMessage.addSelectionListener( new SelectionAdapter() {
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
    fdlGetAttachment = new FormData();
    fdlGetAttachment.left = new FormAttachment( 0, 0 );
    fdlGetAttachment.top = new FormAttachment( wGetMessage, margin );
    fdlGetAttachment.right = new FormAttachment( middle, -margin );
    wlGetAttachment.setLayoutData( fdlGetAttachment );
    wGetAttachment = new Button( wTargetFolder, SWT.CHECK );
    props.setLook( wGetAttachment );
    fdGetAttachment = new FormData();
    wGetAttachment.setToolTipText( BaseMessages.getString( PKG, "JobGetPOP.GetAttachmentMails.Tooltip" ) );
    fdGetAttachment.left = new FormAttachment( middle, 0 );
    fdGetAttachment.top = new FormAttachment( wGetMessage, margin );
    fdGetAttachment.right = new FormAttachment( 100, 0 );
    wGetAttachment.setLayoutData( fdGetAttachment );

    wGetAttachment.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e ) {
        activeAttachmentFolder();
      }
    } );

    // different folder for attachment?
    wlDifferentFolderForAttachment = new Label( wTargetFolder, SWT.RIGHT );
    wlDifferentFolderForAttachment.setText( BaseMessages.getString(
      PKG, "JobGetPOP.DifferentFolderForAttachmentMails.Label" ) );
    props.setLook( wlDifferentFolderForAttachment );
    fdlDifferentFolderForAttachment = new FormData();
    fdlDifferentFolderForAttachment.left = new FormAttachment( 0, 0 );
    fdlDifferentFolderForAttachment.top = new FormAttachment( wGetAttachment, margin );
    fdlDifferentFolderForAttachment.right = new FormAttachment( middle, -margin );
    wlDifferentFolderForAttachment.setLayoutData( fdlDifferentFolderForAttachment );
    wDifferentFolderForAttachment = new Button( wTargetFolder, SWT.CHECK );
    props.setLook( wDifferentFolderForAttachment );
    fdDifferentFolderForAttachment = new FormData();
    wDifferentFolderForAttachment.setToolTipText( BaseMessages.getString(
      PKG, "JobGetPOP.DifferentFolderForAttachmentMails.Tooltip" ) );
    fdDifferentFolderForAttachment.left = new FormAttachment( middle, 0 );
    fdDifferentFolderForAttachment.top = new FormAttachment( wGetAttachment, margin );
    fdDifferentFolderForAttachment.right = new FormAttachment( 100, 0 );
    wDifferentFolderForAttachment.setLayoutData( fdDifferentFolderForAttachment );

    wDifferentFolderForAttachment.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e ) {
        activeAttachmentFolder();
      }
    } );

    // AttachmentFolder line
    wlAttachmentFolder = new Label( wTargetFolder, SWT.RIGHT );
    wlAttachmentFolder.setText( BaseMessages.getString( PKG, "JobGetPOP.AttachmentFolder.Label" ) );
    props.setLook( wlAttachmentFolder );
    fdlAttachmentFolder = new FormData();
    fdlAttachmentFolder.left = new FormAttachment( 0, 0 );
    fdlAttachmentFolder.top = new FormAttachment( wDifferentFolderForAttachment, margin );
    fdlAttachmentFolder.right = new FormAttachment( middle, -margin );
    wlAttachmentFolder.setLayoutData( fdlAttachmentFolder );

    // Browse Source folders button ...
    wbAttachmentFolder = new Button( wTargetFolder, SWT.PUSH | SWT.CENTER );
    props.setLook( wbAttachmentFolder );
    wbAttachmentFolder.setText( BaseMessages.getString( PKG, "JobGetPOP.BrowseFolders.Label" ) );
    fdbAttachmentFolder = new FormData();
    fdbAttachmentFolder.right = new FormAttachment( 100, -margin );
    fdbAttachmentFolder.top = new FormAttachment( wDifferentFolderForAttachment, margin );
    wbAttachmentFolder.setLayoutData( fdbAttachmentFolder );

    wAttachmentFolder = new TextVar( jobMeta, wTargetFolder, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wAttachmentFolder );
    wAttachmentFolder.setToolTipText( BaseMessages.getString( PKG, "JobGetPOP.AttachmentFolder.Tooltip" ) );
    wAttachmentFolder.addModifyListener( lsMod );
    fdAttachmentFolder = new FormData();
    fdAttachmentFolder.left = new FormAttachment( middle, 0 );
    fdAttachmentFolder.top = new FormAttachment( wDifferentFolderForAttachment, margin );
    fdAttachmentFolder.right = new FormAttachment( wbAttachmentFolder, -margin );
    wAttachmentFolder.setLayoutData( fdAttachmentFolder );

    wbAttachmentFolder.addSelectionListener( new SelectionAdapterFileDialogTextVar( jobMeta.getLogChannel(), wAttachmentFolder, jobMeta,
      new SelectionAdapterOptions( SelectionOperation.FOLDER ) ) );

    // Limit attached files
    wlAttachmentWildcard = new Label( wTargetFolder, SWT.RIGHT );
    wlAttachmentWildcard.setText( BaseMessages.getString( PKG, "JobGetPOP.AttachmentWildcard.Label" ) );
    props.setLook( wlAttachmentWildcard );
    fdlAttachmentWildcard = new FormData();
    fdlAttachmentWildcard.left = new FormAttachment( 0, 0 );
    fdlAttachmentWildcard.top = new FormAttachment( wbAttachmentFolder, margin );
    fdlAttachmentWildcard.right = new FormAttachment( middle, -margin );
    wlAttachmentWildcard.setLayoutData( fdlAttachmentWildcard );
    wAttachmentWildcard = new TextVar( jobMeta, wTargetFolder, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wAttachmentWildcard );
    wAttachmentWildcard.setToolTipText( BaseMessages.getString( PKG, "JobGetPOP.AttachmentWildcard.Tooltip" ) );
    wAttachmentWildcard.addModifyListener( lsMod );
    fdAttachmentWildcard = new FormData();
    fdAttachmentWildcard.left = new FormAttachment( middle, 0 );
    fdAttachmentWildcard.top = new FormAttachment( wbAttachmentFolder, margin );
    fdAttachmentWildcard.right = new FormAttachment( 100, 0 );
    wAttachmentWildcard.setLayoutData( fdAttachmentWildcard );

    // Whenever something changes, set the tooltip to the expanded version:
    wAttachmentWildcard.addModifyListener( new ModifyListener() {
      public void modifyText( ModifyEvent e ) {
        wAttachmentWildcard.setToolTipText( jobMeta.environmentSubstitute( wAttachmentWildcard.getText() ) );
      }
    } );

    fdTargetFolder = new FormData();
    fdTargetFolder.left = new FormAttachment( 0, margin );
    fdTargetFolder.top = new FormAttachment( wServerSettings, margin );
    fdTargetFolder.right = new FormAttachment( 100, -margin );
    wTargetFolder.setLayoutData( fdTargetFolder );
    // ///////////////////////////////////////////////////////////
    // / END OF SERVER SETTINGS GROUP
    // ///////////////////////////////////////////////////////////

    fdGeneralComp = new FormData();
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

    wSettingsTab = new CTabItem( wTabFolder, SWT.NONE );
    wSettingsTab.setText( BaseMessages.getString( PKG, "JobGetPOP.Tab.Pop.Label" ) );
    wSettingsComp = new Composite( wTabFolder, SWT.NONE );
    props.setLook( wSettingsComp );
    FormLayout PopLayout = new FormLayout();
    PopLayout.marginWidth = 3;
    PopLayout.marginHeight = 3;
    wSettingsComp.setLayout( PopLayout );

    // Action type
    wlActionType = new Label( wSettingsComp, SWT.RIGHT );
    wlActionType.setText( BaseMessages.getString( PKG, "JobGetPOP.ActionType.Label" ) );
    props.setLook( wlActionType );
    fdlActionType = new FormData();
    fdlActionType.left = new FormAttachment( 0, 0 );
    fdlActionType.right = new FormAttachment( middle, -margin );
    fdlActionType.top = new FormAttachment( 0, 3 * margin );
    wlActionType.setLayoutData( fdlActionType );

    wActionType = new CCombo( wSettingsComp, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER );
    wActionType.setItems( MailConnectionMeta.actionTypeDesc );
    wActionType.select( 0 ); // +1: starts at -1

    props.setLook( wActionType );
    fdActionType = new FormData();
    fdActionType.left = new FormAttachment( middle, 0 );
    fdActionType.top = new FormAttachment( 0, 3 * margin );
    fdActionType.right = new FormAttachment( 100, 0 );
    wActionType.setLayoutData( fdActionType );
    wActionType.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e ) {
        setActionType();
        jobEntry.setChanged();
      }
    } );

    // Message: for POP3, only INBOX folder is available!
    wlPOP3Message = new Label( wSettingsComp, SWT.RIGHT );
    wlPOP3Message.setText( BaseMessages.getString( PKG, "JobGetPOP.POP3Message.Label" ) );
    props.setLook( wlPOP3Message );
    fdlPOP3Message = new FormData();
    fdlPOP3Message.left = new FormAttachment( 0, margin );
    fdlPOP3Message.top = new FormAttachment( wActionType, 3 * margin );
    wlPOP3Message.setLayoutData( fdlPOP3Message );
    wlPOP3Message.setForeground( GUIResource.getInstance().getColorOrange() );

    // ////////////////////////
    // START OF POP3 Settings GROUP///
    // /
    wPOP3Settings = new Group( wSettingsComp, SWT.SHADOW_NONE );
    props.setLook( wPOP3Settings );
    wPOP3Settings.setText( BaseMessages.getString( PKG, "JobGetPOP.POP3Settings.Group.Label" ) );

    FormLayout POP3SettingsgroupLayout = new FormLayout();
    POP3SettingsgroupLayout.marginWidth = 10;
    POP3SettingsgroupLayout.marginHeight = 10;
    wPOP3Settings.setLayout( POP3SettingsgroupLayout );

    // List of mails of retrieve
    wlListmails = new Label( wPOP3Settings, SWT.RIGHT );
    wlListmails.setText( BaseMessages.getString( PKG, "JobGetPOP.Listmails.Label" ) );
    props.setLook( wlListmails );
    fdlListmails = new FormData();
    fdlListmails.left = new FormAttachment( 0, 0 );
    fdlListmails.right = new FormAttachment( middle, 0 );
    fdlListmails.top = new FormAttachment( wlPOP3Message, 2 * margin );
    wlListmails.setLayoutData( fdlListmails );
    wListmails = new CCombo( wPOP3Settings, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER );
    wListmails.add( BaseMessages.getString( PKG, "JobGetPOP.RetrieveAllMails.Label" ) );
    // PDI-7241 POP3 does not support retrive unread
    // wListmails.add( BaseMessages.getString( PKG, "JobGetPOP.RetrieveUnreadMails.Label" ) );
    wListmails.add( BaseMessages.getString( PKG, "JobGetPOP.RetrieveFirstMails.Label" ) );
    wListmails.select( 0 ); // +1: starts at -1

    props.setLook( wListmails );
    fdListmails = new FormData();
    fdListmails.left = new FormAttachment( middle, 0 );
    fdListmails.top = new FormAttachment( wlPOP3Message, 2 * margin );
    fdListmails.right = new FormAttachment( 100, 0 );
    wListmails.setLayoutData( fdListmails );

    wListmails.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e ) {
        jobEntry.setChanged();
        chooseListMails();

      }
    } );

    // Retrieve the first ... mails
    wlFirstmails = new Label( wPOP3Settings, SWT.RIGHT );
    wlFirstmails.setText( BaseMessages.getString( PKG, "JobGetPOP.Firstmails.Label" ) );
    props.setLook( wlFirstmails );
    fdlFirstmails = new FormData();
    fdlFirstmails.left = new FormAttachment( 0, 0 );
    fdlFirstmails.right = new FormAttachment( middle, -margin );
    fdlFirstmails.top = new FormAttachment( wListmails, margin );
    wlFirstmails.setLayoutData( fdlFirstmails );

    wFirstmails = new TextVar( jobMeta, wPOP3Settings, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wFirstmails );
    wFirstmails.addModifyListener( lsMod );
    fdFirstmails = new FormData();
    fdFirstmails.left = new FormAttachment( middle, 0 );
    fdFirstmails.top = new FormAttachment( wListmails, margin );
    fdFirstmails.right = new FormAttachment( 100, 0 );
    wFirstmails.setLayoutData( fdFirstmails );

    // Delete mails after retrieval...
    wlDelete = new Label( wPOP3Settings, SWT.RIGHT );
    wlDelete.setText( BaseMessages.getString( PKG, "JobGetPOP.DeleteMails.Label" ) );
    props.setLook( wlDelete );
    fdlDelete = new FormData();
    fdlDelete.left = new FormAttachment( 0, 0 );
    fdlDelete.top = new FormAttachment( wFirstmails, margin );
    fdlDelete.right = new FormAttachment( middle, -margin );
    wlDelete.setLayoutData( fdlDelete );
    wDelete = new Button( wPOP3Settings, SWT.CHECK );
    props.setLook( wDelete );
    fdDelete = new FormData();
    wDelete.setToolTipText( BaseMessages.getString( PKG, "JobGetPOP.DeleteMails.Tooltip" ) );
    fdDelete.left = new FormAttachment( middle, 0 );
    fdDelete.top = new FormAttachment( wFirstmails, margin );
    fdDelete.right = new FormAttachment( 100, 0 );
    wDelete.setLayoutData( fdDelete );

    fdPOP3Settings = new FormData();
    fdPOP3Settings.left = new FormAttachment( 0, margin );
    fdPOP3Settings.top = new FormAttachment( wlPOP3Message, 2 * margin );
    fdPOP3Settings.right = new FormAttachment( 100, -margin );
    wPOP3Settings.setLayoutData( fdPOP3Settings );
    // ///////////////////////////////////////////////////////////
    // / END OF POP3 SETTINGS GROUP
    // ///////////////////////////////////////////////////////////

    // ////////////////////////
    // START OF IMAP Settings GROUP///
    // /
    wIMAPSettings = new Group( wSettingsComp, SWT.SHADOW_NONE );
    props.setLook( wIMAPSettings );
    wIMAPSettings.setText( BaseMessages.getString( PKG, "JobGetPOP.IMAPSettings.Groupp.Label" ) );

    FormLayout IMAPSettingsgroupLayout = new FormLayout();
    IMAPSettingsgroupLayout.marginWidth = 10;
    IMAPSettingsgroupLayout.marginHeight = 10;
    wIMAPSettings.setLayout( IMAPSettingsgroupLayout );

    // SelectFolder button
    wSelectFolder = new Button( wIMAPSettings, SWT.PUSH );
    wSelectFolder.setText( BaseMessages.getString( PKG, "JobGetPOP.SelectFolderConnection.Label" ) );
    props.setLook( wSelectFolder );
    fdSelectFolder = new FormData();
    wSelectFolder.setToolTipText( BaseMessages.getString( PKG, "JobGetPOP.SelectFolderConnection.Tooltip" ) );
    fdSelectFolder.top = new FormAttachment( wPOP3Settings, margin );
    fdSelectFolder.right = new FormAttachment( 100, 0 );
    wSelectFolder.setLayoutData( fdSelectFolder );

    // TestIMAPFolder button
    wTestIMAPFolder = new Button( wIMAPSettings, SWT.PUSH );
    wTestIMAPFolder.setText( BaseMessages.getString( PKG, "JobGetPOP.TestIMAPFolderConnection.Label" ) );
    props.setLook( wTestIMAPFolder );
    fdTestIMAPFolder = new FormData();
    wTestIMAPFolder.setToolTipText( BaseMessages.getString( PKG, "JobGetPOP.TestIMAPFolderConnection.Tooltip" ) );
    fdTestIMAPFolder.top = new FormAttachment( wPOP3Settings, margin );
    fdTestIMAPFolder.right = new FormAttachment( wSelectFolder, -margin );
    wTestIMAPFolder.setLayoutData( fdTestIMAPFolder );

    // IMAPFolder line
    wlIMAPFolder = new Label( wIMAPSettings, SWT.RIGHT );
    wlIMAPFolder.setText( BaseMessages.getString( PKG, "JobGetPOP.IMAPFolder.Label" ) );
    props.setLook( wlIMAPFolder );
    fdlIMAPFolder = new FormData();
    fdlIMAPFolder.left = new FormAttachment( 0, 0 );
    fdlIMAPFolder.top = new FormAttachment( wPOP3Settings, margin );
    fdlIMAPFolder.right = new FormAttachment( middle, -margin );
    wlIMAPFolder.setLayoutData( fdlIMAPFolder );
    wIMAPFolder = new TextVar( jobMeta, wIMAPSettings, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wIMAPFolder );
    wIMAPFolder.setToolTipText( BaseMessages.getString( PKG, "JobGetPOP.IMAPFolder.Tooltip" ) );
    wIMAPFolder.addModifyListener( lsMod );
    fdIMAPFolder = new FormData();
    fdIMAPFolder.left = new FormAttachment( middle, 0 );
    fdIMAPFolder.top = new FormAttachment( wPOP3Settings, margin );
    fdIMAPFolder.right = new FormAttachment( wTestIMAPFolder, -margin );
    wIMAPFolder.setLayoutData( fdIMAPFolder );

    // Include subfolders?
    wlIncludeSubFolders = new Label( wIMAPSettings, SWT.RIGHT );
    wlIncludeSubFolders.setText( BaseMessages.getString( PKG, "JobGetPOP.IncludeSubFoldersMails.Label" ) );
    props.setLook( wlIncludeSubFolders );
    fdlIncludeSubFolders = new FormData();
    fdlIncludeSubFolders.left = new FormAttachment( 0, 0 );
    fdlIncludeSubFolders.top = new FormAttachment( wIMAPFolder, margin );
    fdlIncludeSubFolders.right = new FormAttachment( middle, -margin );
    wlIncludeSubFolders.setLayoutData( fdlIncludeSubFolders );
    wIncludeSubFolders = new Button( wIMAPSettings, SWT.CHECK );
    props.setLook( wIncludeSubFolders );
    fdIncludeSubFolders = new FormData();
    wIncludeSubFolders.setToolTipText( BaseMessages.getString( PKG, "JobGetPOP.IncludeSubFoldersMails.Tooltip" ) );
    fdIncludeSubFolders.left = new FormAttachment( middle, 0 );
    fdIncludeSubFolders.top = new FormAttachment( wIMAPFolder, margin );
    fdIncludeSubFolders.right = new FormAttachment( 100, 0 );
    wIncludeSubFolders.setLayoutData( fdIncludeSubFolders );
    wIncludeSubFolders.addSelectionListener( lsSelection );

    // List of mails of retrieve
    wlIMAPListmails = new Label( wIMAPSettings, SWT.RIGHT );
    wlIMAPListmails.setText( BaseMessages.getString( PKG, "JobGetPOP.IMAPListmails.Label" ) );
    props.setLook( wlIMAPListmails );
    fdlIMAPListmails = new FormData();
    fdlIMAPListmails.left = new FormAttachment( 0, 0 );
    fdlIMAPListmails.right = new FormAttachment( middle, -margin );
    fdlIMAPListmails.top = new FormAttachment( wIncludeSubFolders, margin );
    wlIMAPListmails.setLayoutData( fdlIMAPListmails );
    wIMAPListmails = new CCombo( wIMAPSettings, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER );
    wIMAPListmails.setItems( MailConnectionMeta.valueIMAPListDesc );
    wIMAPListmails.select( 0 ); // +1: starts at -1

    props.setLook( wIMAPListmails );
    fdIMAPListmails = new FormData();
    fdIMAPListmails.left = new FormAttachment( middle, 0 );
    fdIMAPListmails.top = new FormAttachment( wIncludeSubFolders, margin );
    fdIMAPListmails.right = new FormAttachment( 100, 0 );
    wIMAPListmails.setLayoutData( fdIMAPListmails );

    wIMAPListmails.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e ) {
        // ChooseIMAPListmails();

      }
    } );

    // Retrieve the first ... mails
    wlIMAPFirstmails = new Label( wIMAPSettings, SWT.RIGHT );
    wlIMAPFirstmails.setText( BaseMessages.getString( PKG, "JobGetPOP.IMAPFirstmails.Label" ) );
    props.setLook( wlIMAPFirstmails );
    fdlIMAPFirstmails = new FormData();
    fdlIMAPFirstmails.left = new FormAttachment( 0, 0 );
    fdlIMAPFirstmails.right = new FormAttachment( middle, -margin );
    fdlIMAPFirstmails.top = new FormAttachment( wIMAPListmails, margin );
    wlIMAPFirstmails.setLayoutData( fdlIMAPFirstmails );

    wIMAPFirstmails = new TextVar( jobMeta, wIMAPSettings, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wIMAPFirstmails );
    wIMAPFirstmails.addModifyListener( lsMod );
    fdIMAPFirstmails = new FormData();
    fdIMAPFirstmails.left = new FormAttachment( middle, 0 );
    fdIMAPFirstmails.top = new FormAttachment( wIMAPListmails, margin );
    fdIMAPFirstmails.right = new FormAttachment( 100, 0 );
    wIMAPFirstmails.setLayoutData( fdIMAPFirstmails );

    // After get IMAP
    wlAfterGetIMAP = new Label( wIMAPSettings, SWT.RIGHT );
    wlAfterGetIMAP.setText( BaseMessages.getString( PKG, "JobGetPOP.AfterGetIMAP.Label" ) );
    props.setLook( wlAfterGetIMAP );
    fdlAfterGetIMAP = new FormData();
    fdlAfterGetIMAP.left = new FormAttachment( 0, 0 );
    fdlAfterGetIMAP.right = new FormAttachment( middle, -margin );
    fdlAfterGetIMAP.top = new FormAttachment( wIMAPFirstmails, 2 * margin );
    wlAfterGetIMAP.setLayoutData( fdlAfterGetIMAP );
    wAfterGetIMAP = new CCombo( wIMAPSettings, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER );
    wAfterGetIMAP.setItems( MailConnectionMeta.afterGetIMAPDesc );
    wAfterGetIMAP.select( 0 ); // +1: starts at -1

    props.setLook( wAfterGetIMAP );
    fdAfterGetIMAP = new FormData();
    fdAfterGetIMAP.left = new FormAttachment( middle, 0 );
    fdAfterGetIMAP.top = new FormAttachment( wIMAPFirstmails, 2 * margin );
    fdAfterGetIMAP.right = new FormAttachment( 100, 0 );
    wAfterGetIMAP.setLayoutData( fdAfterGetIMAP );

    wAfterGetIMAP.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e ) {
        setAfterIMAPRetrived();
        jobEntry.setChanged();
      }
    } );

    // MoveToFolder line
    wlMoveToFolder = new Label( wIMAPSettings, SWT.RIGHT );
    wlMoveToFolder.setText( BaseMessages.getString( PKG, "JobGetPOP.MoveToFolder.Label" ) );
    props.setLook( wlMoveToFolder );
    fdlMoveToFolder = new FormData();
    fdlMoveToFolder.left = new FormAttachment( 0, 0 );
    fdlMoveToFolder.top = new FormAttachment( wAfterGetIMAP, margin );
    fdlMoveToFolder.right = new FormAttachment( middle, -margin );
    wlMoveToFolder.setLayoutData( fdlMoveToFolder );

    // SelectMoveToFolder button
    wSelectMoveToFolder = new Button( wIMAPSettings, SWT.PUSH );
    wSelectMoveToFolder.setText( BaseMessages.getString( PKG, "JobGetPOP.SelectMoveToFolderConnection.Label" ) );
    props.setLook( wSelectMoveToFolder );
    fdSelectMoveToFolder = new FormData();
    wSelectMoveToFolder.setToolTipText( BaseMessages.getString(
      PKG, "JobGetPOP.SelectMoveToFolderConnection.Tooltip" ) );
    fdSelectMoveToFolder.top = new FormAttachment( wAfterGetIMAP, margin );
    fdSelectMoveToFolder.right = new FormAttachment( 100, 0 );
    wSelectMoveToFolder.setLayoutData( fdSelectMoveToFolder );

    // TestMoveToFolder button
    wTestMoveToFolder = new Button( wIMAPSettings, SWT.PUSH );
    wTestMoveToFolder.setText( BaseMessages.getString( PKG, "JobGetPOP.TestMoveToFolderConnection.Label" ) );
    props.setLook( wTestMoveToFolder );
    fdTestMoveToFolder = new FormData();
    wTestMoveToFolder
      .setToolTipText( BaseMessages.getString( PKG, "JobGetPOP.TestMoveToFolderConnection.Tooltip" ) );
    fdTestMoveToFolder.top = new FormAttachment( wAfterGetIMAP, margin );
    fdTestMoveToFolder.right = new FormAttachment( wSelectMoveToFolder, -margin );
    wTestMoveToFolder.setLayoutData( fdTestMoveToFolder );

    wMoveToFolder = new TextVar( jobMeta, wIMAPSettings, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wMoveToFolder );
    wMoveToFolder.setToolTipText( BaseMessages.getString( PKG, "JobGetPOP.MoveToFolder.Tooltip" ) );
    wMoveToFolder.addModifyListener( lsMod );
    fdMoveToFolder = new FormData();
    fdMoveToFolder.left = new FormAttachment( middle, 0 );
    fdMoveToFolder.top = new FormAttachment( wAfterGetIMAP, margin );
    fdMoveToFolder.right = new FormAttachment( wTestMoveToFolder, -margin );
    wMoveToFolder.setLayoutData( fdMoveToFolder );

    // Create move to folder
    wlcreateMoveToFolder = new Label( wIMAPSettings, SWT.RIGHT );
    wlcreateMoveToFolder.setText( BaseMessages.getString( PKG, "JobGetPOP.createMoveToFolderMails.Label" ) );
    props.setLook( wlcreateMoveToFolder );
    fdlcreateMoveToFolder = new FormData();
    fdlcreateMoveToFolder.left = new FormAttachment( 0, 0 );
    fdlcreateMoveToFolder.top = new FormAttachment( wMoveToFolder, margin );
    fdlcreateMoveToFolder.right = new FormAttachment( middle, -margin );
    wlcreateMoveToFolder.setLayoutData( fdlcreateMoveToFolder );
    wcreateMoveToFolder = new Button( wIMAPSettings, SWT.CHECK );
    props.setLook( wcreateMoveToFolder );
    fdcreateMoveToFolder = new FormData();
    wcreateMoveToFolder
      .setToolTipText( BaseMessages.getString( PKG, "JobGetPOP.createMoveToFolderMails.Tooltip" ) );
    fdcreateMoveToFolder.left = new FormAttachment( middle, 0 );
    fdcreateMoveToFolder.top = new FormAttachment( wMoveToFolder, margin );
    fdcreateMoveToFolder.right = new FormAttachment( 100, 0 );
    wcreateMoveToFolder.setLayoutData( fdcreateMoveToFolder );

    fdIMAPSettings = new FormData();
    fdIMAPSettings.left = new FormAttachment( 0, margin );
    fdIMAPSettings.top = new FormAttachment( wPOP3Settings, 2 * margin );
    fdIMAPSettings.right = new FormAttachment( 100, -margin );
    wIMAPSettings.setLayoutData( fdIMAPSettings );
    // ///////////////////////////////////////////////////////////
    // / END OF IMAP SETTINGS GROUP
    // ///////////////////////////////////////////////////////////

    fdSettingsComp = new FormData();
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

    wSearchTab = new CTabItem( wTabFolder, SWT.NONE );
    wSearchTab.setText( BaseMessages.getString( PKG, "JobGetPOP.Tab.Search.Label" ) );
    wSearchComp = new Composite( wTabFolder, SWT.NONE );
    props.setLook( wSearchComp );
    FormLayout searchLayout = new FormLayout();
    searchLayout.marginWidth = 3;
    searchLayout.marginHeight = 3;
    wSearchComp.setLayout( searchLayout );

    // ////////////////////////
    // START OF HEADER ROUP///
    // /
    wHeader = new Group( wSearchComp, SWT.SHADOW_NONE );
    props.setLook( wHeader );
    wHeader.setText( BaseMessages.getString( PKG, "JobGetPOP.Header.Group.Label" ) );

    FormLayout HeadergroupLayout = new FormLayout();
    HeadergroupLayout.marginWidth = 10;
    HeadergroupLayout.marginHeight = 10;
    wHeader.setLayout( HeadergroupLayout );

    wNegateSender = new Button( wHeader, SWT.CHECK );
    props.setLook( wNegateSender );
    fdNegateSender = new FormData();
    wNegateSender.setToolTipText( BaseMessages.getString( PKG, "JobGetPOP.NegateSender.Tooltip" ) );
    fdNegateSender.top = new FormAttachment( 0, margin );
    fdNegateSender.right = new FormAttachment( 100, -margin );
    wNegateSender.setLayoutData( fdNegateSender );

    // From line
    wlSender = new Label( wHeader, SWT.RIGHT );
    wlSender.setText( BaseMessages.getString( PKG, "JobGetPOP.wSender.Label" ) );
    props.setLook( wlSender );
    fdlSender = new FormData();
    fdlSender.left = new FormAttachment( 0, 0 );
    fdlSender.top = new FormAttachment( 0, margin );
    fdlSender.right = new FormAttachment( middle, -margin );
    wlSender.setLayoutData( fdlSender );
    wSender = new TextVar( jobMeta, wHeader, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wSender );
    wSender.addModifyListener( lsMod );
    fdSender = new FormData();
    fdSender.left = new FormAttachment( middle, 0 );
    fdSender.top = new FormAttachment( 0, margin );
    fdSender.right = new FormAttachment( wNegateSender, -margin );
    wSender.setLayoutData( fdSender );

    wNegateReceipient = new Button( wHeader, SWT.CHECK );
    props.setLook( wNegateReceipient );
    fdNegateReceipient = new FormData();
    wNegateReceipient.setToolTipText( BaseMessages.getString( PKG, "JobGetPOP.NegateReceipient.Tooltip" ) );
    fdNegateReceipient.top = new FormAttachment( wSender, margin );
    fdNegateReceipient.right = new FormAttachment( 100, -margin );
    wNegateReceipient.setLayoutData( fdNegateReceipient );

    // Receipient line
    wlReceipient = new Label( wHeader, SWT.RIGHT );
    wlReceipient.setText( BaseMessages.getString( PKG, "JobGetPOP.Receipient.Label" ) );
    props.setLook( wlReceipient );
    fdlReceipient = new FormData();
    fdlReceipient.left = new FormAttachment( 0, 0 );
    fdlReceipient.top = new FormAttachment( wSender, margin );
    fdlReceipient.right = new FormAttachment( middle, -margin );
    wlReceipient.setLayoutData( fdlReceipient );
    wReceipient = new TextVar( jobMeta, wHeader, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wReceipient );
    wReceipient.addModifyListener( lsMod );
    fdReceipient = new FormData();
    fdReceipient.left = new FormAttachment( middle, 0 );
    fdReceipient.top = new FormAttachment( wSender, margin );
    fdReceipient.right = new FormAttachment( wNegateReceipient, -margin );
    wReceipient.setLayoutData( fdReceipient );

    wNegateSubject = new Button( wHeader, SWT.CHECK );
    props.setLook( wNegateSubject );
    fdNegateSubject = new FormData();
    wNegateSubject.setToolTipText( BaseMessages.getString( PKG, "JobGetPOP.NegateSubject.Tooltip" ) );
    fdNegateSubject.top = new FormAttachment( wReceipient, margin );
    fdNegateSubject.right = new FormAttachment( 100, -margin );
    wNegateSubject.setLayoutData( fdNegateSubject );

    // Subject line
    wlSubject = new Label( wHeader, SWT.RIGHT );
    wlSubject.setText( BaseMessages.getString( PKG, "JobGetPOP.Subject.Label" ) );
    props.setLook( wlSubject );
    fdlSubject = new FormData();
    fdlSubject.left = new FormAttachment( 0, 0 );
    fdlSubject.top = new FormAttachment( wReceipient, margin );
    fdlSubject.right = new FormAttachment( middle, -margin );
    wlSubject.setLayoutData( fdlSubject );
    wSubject = new TextVar( jobMeta, wHeader, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wSubject );
    wSubject.addModifyListener( lsMod );
    fdSubject = new FormData();
    fdSubject.left = new FormAttachment( middle, 0 );
    fdSubject.top = new FormAttachment( wReceipient, margin );
    fdSubject.right = new FormAttachment( wNegateSubject, -margin );
    wSubject.setLayoutData( fdSubject );

    fdHeader = new FormData();
    fdHeader.left = new FormAttachment( 0, margin );
    fdHeader.top = new FormAttachment( wReceipient, 2 * margin );
    fdHeader.right = new FormAttachment( 100, -margin );
    wHeader.setLayoutData( fdHeader );
    // ///////////////////////////////////////////////////////////
    // / END OF HEADER GROUP
    // ///////////////////////////////////////////////////////////

    // ////////////////////////
    // START OF CONTENT GROUP///
    // /
    wContent = new Group( wSearchComp, SWT.SHADOW_NONE );
    props.setLook( wContent );
    wContent.setText( BaseMessages.getString( PKG, "JobGetPOP.Content.Group.Label" ) );

    FormLayout ContentgroupLayout = new FormLayout();
    ContentgroupLayout.marginWidth = 10;
    ContentgroupLayout.marginHeight = 10;
    wContent.setLayout( ContentgroupLayout );

    wNegateBody = new Button( wContent, SWT.CHECK );
    props.setLook( wNegateBody );
    fdNegateBody = new FormData();
    wNegateBody.setToolTipText( BaseMessages.getString( PKG, "JobGetPOP.NegateBody.Tooltip" ) );
    fdNegateBody.top = new FormAttachment( wHeader, margin );
    fdNegateBody.right = new FormAttachment( 100, -margin );
    wNegateBody.setLayoutData( fdNegateBody );

    // Body line
    wlBody = new Label( wContent, SWT.RIGHT );
    wlBody.setText( BaseMessages.getString( PKG, "JobGetPOP.Body.Label" ) );
    props.setLook( wlBody );
    fdlBody = new FormData();
    fdlBody.left = new FormAttachment( 0, 0 );
    fdlBody.top = new FormAttachment( wHeader, margin );
    fdlBody.right = new FormAttachment( middle, -margin );
    wlBody.setLayoutData( fdlBody );
    wBody = new TextVar( jobMeta, wContent, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wBody );
    wBody.addModifyListener( lsMod );
    fdBody = new FormData();
    fdBody.left = new FormAttachment( middle, 0 );
    fdBody.top = new FormAttachment( wHeader, margin );
    fdBody.right = new FormAttachment( wNegateBody, -margin );
    wBody.setLayoutData( fdBody );

    fdContent = new FormData();
    fdContent.left = new FormAttachment( 0, margin );
    fdContent.top = new FormAttachment( wHeader, margin );
    fdContent.right = new FormAttachment( 100, -margin );
    wContent.setLayoutData( fdContent );
    // ///////////////////////////////////////////////////////////
    // / END OF CONTENT GROUP
    // ///////////////////////////////////////////////////////////

    // ////////////////////////
    // START OF RECEIVED DATE ROUP///
    // /
    wReceivedDate = new Group( wSearchComp, SWT.SHADOW_NONE );
    props.setLook( wReceivedDate );
    wReceivedDate.setText( BaseMessages.getString( PKG, "JobGetPOP.ReceivedDate.Group.Label" ) );

    FormLayout ReceivedDategroupLayout = new FormLayout();
    ReceivedDategroupLayout.marginWidth = 10;
    ReceivedDategroupLayout.marginHeight = 10;
    wReceivedDate.setLayout( ReceivedDategroupLayout );

    wNegateReceivedDate = new Button( wReceivedDate, SWT.CHECK );
    props.setLook( wNegateReceivedDate );
    fdNegateReceivedDate = new FormData();
    wNegateReceivedDate.setToolTipText( BaseMessages.getString( PKG, "JobGetPOP.NegateReceivedDate.Tooltip" ) );
    fdNegateReceivedDate.top = new FormAttachment( wContent, margin );
    fdNegateReceivedDate.right = new FormAttachment( 100, -margin );
    wNegateReceivedDate.setLayoutData( fdNegateReceivedDate );

    // Received Date Condition
    wlConditionOnReceivedDate = new Label( wReceivedDate, SWT.RIGHT );
    wlConditionOnReceivedDate.setText( BaseMessages.getString( PKG, "JobGetPOP.ConditionOnReceivedDate.Label" ) );
    props.setLook( wlConditionOnReceivedDate );
    fdlConditionOnReceivedDate = new FormData();
    fdlConditionOnReceivedDate.left = new FormAttachment( 0, 0 );
    fdlConditionOnReceivedDate.right = new FormAttachment( middle, -margin );
    fdlConditionOnReceivedDate.top = new FormAttachment( wContent, margin );
    wlConditionOnReceivedDate.setLayoutData( fdlConditionOnReceivedDate );

    wConditionOnReceivedDate = new CCombo( wReceivedDate, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER );
    wConditionOnReceivedDate.setItems( MailConnectionMeta.conditionDateDesc );
    wConditionOnReceivedDate.select( 0 ); // +1: starts at -1

    props.setLook( wConditionOnReceivedDate );
    fdConditionOnReceivedDate = new FormData();
    fdConditionOnReceivedDate.left = new FormAttachment( middle, 0 );
    fdConditionOnReceivedDate.top = new FormAttachment( wContent, margin );
    fdConditionOnReceivedDate.right = new FormAttachment( wNegateReceivedDate, -margin );
    wConditionOnReceivedDate.setLayoutData( fdConditionOnReceivedDate );
    wConditionOnReceivedDate.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e ) {
        conditionReceivedDate();
        jobEntry.setChanged();
      }
    } );

    open = new Button( wReceivedDate, SWT.PUSH );
    open.setImage( GUIResource.getInstance().getImageCalendar() );
    open.setToolTipText( BaseMessages.getString( PKG, "JobGetPOP.OpenCalendar" ) );
    FormData fdlButton = new FormData();
    fdlButton.top = new FormAttachment( wConditionOnReceivedDate, margin );
    fdlButton.right = new FormAttachment( 100, 0 );
    open.setLayoutData( fdlButton );
    open.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e ) {
        final Shell dialog = new Shell( shell, SWT.DIALOG_TRIM );
        dialog.setText( BaseMessages.getString( PKG, "JobGetPOP.SelectDate" ) );
        dialog.setImage( GUIResource.getInstance().getImageSpoon() );
        dialog.setLayout( new GridLayout( 3, false ) );

        final DateTime calendar = new DateTime( dialog, SWT.CALENDAR );
        final DateTime time = new DateTime( dialog, SWT.TIME | SWT.TIME );
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
    fdlReadFrom = new FormData();
    fdlReadFrom.left = new FormAttachment( 0, 0 );
    fdlReadFrom.top = new FormAttachment( wConditionOnReceivedDate, margin );
    fdlReadFrom.right = new FormAttachment( middle, -margin );
    wlReadFrom.setLayoutData( fdlReadFrom );
    wReadFrom = new TextVar( jobMeta, wReceivedDate, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    wReadFrom.setToolTipText( BaseMessages.getString( PKG, "JobGetPOP.ReadFrom.Tooltip" ) );
    props.setLook( wReadFrom );
    wReadFrom.addModifyListener( lsMod );
    fdReadFrom = new FormData();
    fdReadFrom.left = new FormAttachment( middle, 0 );
    fdReadFrom.top = new FormAttachment( wConditionOnReceivedDate, margin );
    fdReadFrom.right = new FormAttachment( open, -margin );
    wReadFrom.setLayoutData( fdReadFrom );

    opento = new Button( wReceivedDate, SWT.PUSH );
    opento.setImage( GUIResource.getInstance().getImageCalendar() );
    opento.setToolTipText( BaseMessages.getString( PKG, "JobGetPOP.OpenCalendar" ) );
    FormData fdlButtonto = new FormData();
    fdlButtonto.top = new FormAttachment( wReadFrom, 2 * margin );
    fdlButtonto.right = new FormAttachment( 100, 0 );
    opento.setLayoutData( fdlButtonto );
    opento.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e ) {
        final Shell dialogto = new Shell( shell, SWT.DIALOG_TRIM );
        dialogto.setText( BaseMessages.getString( PKG, "JobGetPOP.SelectDate" ) );
        dialogto.setImage( GUIResource.getInstance().getImageSpoon() );
        dialogto.setLayout( new GridLayout( 3, false ) );

        final DateTime calendarto = new DateTime( dialogto, SWT.CALENDAR | SWT.BORDER );
        final DateTime timeto = new DateTime( dialogto, SWT.TIME | SWT.TIME );
        new Label( dialogto, SWT.NONE );
        new Label( dialogto, SWT.NONE );
        Button okto = new Button( dialogto, SWT.PUSH );
        okto.setText( BaseMessages.getString( PKG, "System.Button.OK" ) );
        okto.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, false, false ) );
        okto.addSelectionListener( new SelectionAdapter() {
          public void widgetSelected( SelectionEvent e ) {
            Calendar cal = Calendar.getInstance();
            cal.set( Calendar.YEAR, calendarto.getYear() );
            cal.set( Calendar.MONTH, calendarto.getMonth() );
            cal.set( Calendar.DAY_OF_MONTH, calendarto.getDay() );

            cal.set( Calendar.HOUR_OF_DAY, timeto.getHours() );
            cal.set( Calendar.MINUTE, timeto.getMinutes() );
            cal.set( Calendar.SECOND, timeto.getSeconds() );

            wReadTo.setText( new SimpleDateFormat( JobEntryGetPOP.DATE_PATTERN ).format( cal.getTime() ) );
            dialogto.close();
          }
        } );
        dialogto.setDefaultButton( okto );
        dialogto.pack();
        dialogto.open();
      }
    } );

    wlReadTo = new Label( wReceivedDate, SWT.RIGHT );
    wlReadTo.setText( BaseMessages.getString( PKG, "JobGetPOP.ReadTo.Label" ) );
    props.setLook( wlReadTo );
    fdlReadTo = new FormData();
    fdlReadTo.left = new FormAttachment( 0, 0 );
    fdlReadTo.top = new FormAttachment( wReadFrom, 2 * margin );
    fdlReadTo.right = new FormAttachment( middle, -margin );
    wlReadTo.setLayoutData( fdlReadTo );
    wReadTo = new TextVar( jobMeta, wReceivedDate, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    wReadTo.setToolTipText( BaseMessages.getString( PKG, "JobGetPOP.ReadTo.Tooltip" ) );
    props.setLook( wReadTo );
    wReadTo.addModifyListener( lsMod );
    fdReadTo = new FormData();
    fdReadTo.left = new FormAttachment( middle, 0 );
    fdReadTo.top = new FormAttachment( wReadFrom, 2 * margin );
    fdReadTo.right = new FormAttachment( opento, -margin );
    wReadTo.setLayoutData( fdReadTo );

    fdReceivedDate = new FormData();
    fdReceivedDate.left = new FormAttachment( 0, margin );
    fdReceivedDate.top = new FormAttachment( wContent, margin );
    fdReceivedDate.right = new FormAttachment( 100, -margin );
    wReceivedDate.setLayoutData( fdReceivedDate );
    // ///////////////////////////////////////////////////////////
    // / END OF RECEIVED DATE GROUP
    // ///////////////////////////////////////////////////////////

    fdSearchComp = new FormData();
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

    fdTabFolder = new FormData();
    fdTabFolder.left = new FormAttachment( 0, 0 );
    fdTabFolder.top = new FormAttachment( wName, margin );
    fdTabFolder.right = new FormAttachment( 100, 0 );
    fdTabFolder.bottom = new FormAttachment( 100, -50 );
    wTabFolder.setLayoutData( fdTabFolder );

    wOK = new Button( shell, SWT.PUSH );
    wOK.setText( BaseMessages.getString( PKG, "System.Button.OK" ) );
    wCancel = new Button( shell, SWT.PUSH );
    wCancel.setText( BaseMessages.getString( PKG, "System.Button.Cancel" ) );

    BaseStepDialog.positionBottomButtons( shell, new Button[] { wOK, wCancel }, margin, wTabFolder );

    // Add listeners
    lsCancel = new Listener() {
      public void handleEvent( Event e ) {
        cancel();
      }
    };
    lsOK = new Listener() {
      public void handleEvent( Event e ) {
        ok();
      }
    };

    wCancel.addListener( SWT.Selection, lsCancel );
    wOK.addListener( SWT.Selection, lsOK );

    lsDef = new SelectionAdapter() {
      public void widgetDefaultSelected( SelectionEvent e ) {
        ok();
      }
    };
    lsTest = new Listener() {
      public void handleEvent( Event e ) {
        test();
      }
    };
    wTest.addListener( SWT.Selection, lsTest );

    lsTestIMAPFolder = new Listener() {
      public void handleEvent( Event e ) {
        checkFolder( jobMeta.environmentSubstitute( wIMAPFolder.getText() ) );
      }
    };
    wTestIMAPFolder.addListener( SWT.Selection, lsTestIMAPFolder );

    lsTestMoveToFolder = new Listener() {
      public void handleEvent( Event e ) {
        checkFolder( jobMeta.environmentSubstitute( wMoveToFolder.getText() ) );
      }
    };
    wTestMoveToFolder.addListener( SWT.Selection, lsTestMoveToFolder );

    lsSelectFolder = new Listener() {
      public void handleEvent( Event e ) {
        selectFolder( wIMAPFolder );
      }
    };
    wSelectFolder.addListener( SWT.Selection, lsSelectFolder );

    lsSelectMoveToFolder = new Listener() {
      public void handleEvent( Event e ) {
        selectFolder( wMoveToFolder );
      }
    };
    wSelectMoveToFolder.addListener( SWT.Selection, lsSelectMoveToFolder );

    wName.addSelectionListener( lsDef );
    wServerName.addSelectionListener( lsDef );

    // Detect X or ALT-F4 or something that kills this window...
    shell.addShellListener( new ShellAdapter() {
      public void shellClosed( ShellEvent e ) {
        cancel();
      }
    } );

    getData();
    setUserProxy();
    chooseListMails();
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
    String errordescription = null;
    boolean retval = false;
    if ( mailConn != null && mailConn.isConnected() ) {
      retval = mailConn.isConnected();
    }

    if ( !retval ) {
      String realserver = jobMeta.environmentSubstitute( wServerName.getText() );
      String realuser = jobMeta.environmentSubstitute( wUserName.getText() );
      String realpass = jobEntry.getRealPassword( jobMeta.environmentSubstitute( wPassword.getText() ) );
      int realport = Const.toInt( jobMeta.environmentSubstitute( wPort.getText() ), -1 );
      String realproxyuser = jobMeta.environmentSubstitute( wProxyUsername.getText() );
      try {
        mailConn =
          new MailConnection(
            LogChannel.UI, MailConnectionMeta.getProtocolFromString(
              wProtocol.getText(), MailConnectionMeta.PROTOCOL_IMAP ), realserver, realport, realuser,
            realpass, wUseSSL.getSelection(), wUseProxy.getSelection(), realproxyuser );
        mailConn.connect();

        retval = true;
      } catch ( Exception e ) {
        errordescription = e.getMessage();
      }
    }

    if ( !retval ) {
      MessageBox mb = new MessageBox( shell, SWT.OK | SWT.ICON_ERROR );
      mb.setMessage( BaseMessages.getString( PKG, "JobGetPOP.Connected.NOK.ConnectionBad", wServerName.getText() )
        + Const.CR + Const.NVL( errordescription, "" ) );
      mb.setText( BaseMessages.getString( PKG, "JobGetPOP.Connected.Title.Bad" ) );
      mb.open();
    }

    return ( mailConn.isConnected() );

  }

  private void test() {
    if ( connect() ) {
      MessageBox mb = new MessageBox( shell, SWT.OK | SWT.ICON_INFORMATION );
      mb.setMessage( BaseMessages.getString( PKG, "JobGetPOP.Connected.OK", wServerName.getText() ) + Const.CR );
      mb.setText( BaseMessages.getString( PKG, "JobGetPOP.Connected.Title.Ok" ) );
      mb.open();
    }
  }

  private void selectFolder( TextVar input ) {
    if ( connect() ) {
      try {
        Folder folder = mailConn.getStore().getDefaultFolder();
        SelectFolderDialog s = new SelectFolderDialog( shell, SWT.NONE, folder );
        String foldername = s.open();
        if ( foldername != null ) {
          input.setText( foldername );
        }
      } catch ( Exception e ) {
        // Ignore errors
      }
    }
  }

  private void checkFolder( String foldername ) {
    if ( !Utils.isEmpty( foldername ) ) {
      if ( connect() ) {
        // check folder
        if ( mailConn.folderExists( foldername ) ) {
          MessageBox mb = new MessageBox( shell, SWT.OK | SWT.ICON_INFORMATION );
          mb.setMessage( BaseMessages.getString( PKG, "JobGetPOP.IMAPFolderExists.OK", foldername ) + Const.CR );
          mb.setText( BaseMessages.getString( PKG, "JobGetPOP.IMAPFolderExists.Title.Ok" ) );
          mb.open();
        } else {
          MessageBox mb = new MessageBox( shell, SWT.OK | SWT.ICON_ERROR );
          mb.setMessage( BaseMessages.getString( PKG, "JobGetPOP.Connected.NOK.IMAPFolderExists", foldername )
            + Const.CR );
          mb.setText( BaseMessages.getString( PKG, "JobGetPOP.IMAPFolderExists.Title.Bad" ) );
          mb.open();
        }
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
    opento.setVisible( activeReceivedDate && useBetween );
    if ( !activeReceivedDate ) {
      wReadFrom.setText( "" );
      wReadTo.setText( "" );
      wNegateReceivedDate.setSelection( false );
    }
  }

  private void activeAttachmentFolder() {
    boolean getmessages =
      MailConnectionMeta.getActionTypeByDesc( wActionType.getText() ) == MailConnectionMeta.ACTION_TYPE_GET;
    wlDifferentFolderForAttachment.setEnabled( getmessages && wGetAttachment.getSelection() );
    wDifferentFolderForAttachment.setEnabled( getmessages && wGetAttachment.getSelection() );
    boolean activeattachmentfolder =
      ( wGetAttachment.getSelection() && wDifferentFolderForAttachment.getSelection() );
    wlAttachmentFolder.setEnabled( getmessages && activeattachmentfolder );
    wAttachmentFolder.setEnabled( getmessages && activeattachmentfolder );
    wbAttachmentFolder.setEnabled( getmessages && activeattachmentfolder );
    if ( !wGetAttachment.getSelection() && !wGetMessage.getSelection() ) {
      wGetMessage.setSelection( true );
    }
  }

  private void refreshPort( boolean refreshport ) {
    if ( refreshport ) {
      if ( wProtocol.getText().equals( MailConnectionMeta.PROTOCOL_STRING_POP3 ) ) {
        if ( wUseSSL.getSelection() ) {
          if ( Utils.isEmpty( wPort.getText() )
            || wPort.getText().equals( "" + MailConnectionMeta.DEFAULT_SSL_IMAP_PORT ) ) {
            wPort.setText( "" + MailConnectionMeta.DEFAULT_SSL_POP3_PORT );
          }
        } else {
          if ( Utils.isEmpty( wPort.getText() ) || wPort.getText().equals( MailConnectionMeta.DEFAULT_IMAP_PORT ) ) {
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
          if ( Utils.isEmpty( wPort.getText() ) || wPort.getText().equals( MailConnectionMeta.DEFAULT_POP3_PORT ) ) {
            wPort.setText( "" + MailConnectionMeta.DEFAULT_IMAP_PORT );
          }
        }
      }

    }
  }

  private void refreshProtocol( boolean refreshport ) {
    checkUnavailableMode();
    boolean activePOP3 = wProtocol.getText().equals( MailConnectionMeta.PROTOCOL_STRING_POP3 );
    wlPOP3Message.setEnabled( activePOP3 );
    wlListmails.setEnabled( activePOP3 );
    wListmails.setEnabled( activePOP3 );
    wlFirstmails.setEnabled( activePOP3 );
    wlDelete.setEnabled( activePOP3 );
    wDelete.setEnabled( activePOP3 );

    wlIMAPFirstmails.setEnabled( !activePOP3 );
    wIMAPFirstmails.setEnabled( !activePOP3 );
    wlIMAPFolder.setEnabled( !activePOP3 );
    wIMAPFolder.setEnabled( !activePOP3 );
    wlIncludeSubFolders.setEnabled( !activePOP3 );
    wIncludeSubFolders.setEnabled( !activePOP3 );
    wlIMAPListmails.setEnabled( !activePOP3 );
    wIMAPListmails.setEnabled( !activePOP3 );
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
    refreshPort( refreshport );
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

    boolean getmessages =
      MailConnectionMeta.getActionTypeByDesc( wActionType.getText() ) == MailConnectionMeta.ACTION_TYPE_GET;

    wlOutputDirectory.setEnabled( getmessages );
    wOutputDirectory.setEnabled( getmessages );
    wbDirectory.setEnabled( getmessages );
    wlcreateLocalFolder.setEnabled( getmessages );
    wcreateLocalFolder.setEnabled( getmessages );
    wFilenamePattern.setEnabled( getmessages );
    wlFilenamePattern.setEnabled( getmessages );
    wlAttachmentWildcard.setEnabled( getmessages );
    wAttachmentWildcard.setEnabled( getmessages );
    wlDifferentFolderForAttachment.setEnabled( getmessages );
    wDifferentFolderForAttachment.setEnabled( getmessages );
    wlGetAttachment.setEnabled( getmessages );
    wGetAttachment.setEnabled( getmessages );
    wlGetMessage.setEnabled( getmessages );
    wGetMessage.setEnabled( getmessages );

    wlAfterGetIMAP
      .setEnabled( getmessages && wProtocol.getText().equals( MailConnectionMeta.PROTOCOL_STRING_IMAP ) );
    wAfterGetIMAP
      .setEnabled( getmessages && wProtocol.getText().equals( MailConnectionMeta.PROTOCOL_STRING_IMAP ) );

    setAfterIMAPRetrived();
  }

  private void setAfterIMAPRetrived() {
    boolean activeMoveToFolfer =
      ( ( ( wProtocol.getText().equals( MailConnectionMeta.PROTOCOL_STRING_IMAP ) ) && ( MailConnectionMeta
        .getActionTypeByDesc( wActionType.getText() ) == MailConnectionMeta.ACTION_TYPE_MOVE ) ) || ( MailConnectionMeta
        .getAfterGetIMAPByDesc( wAfterGetIMAP.getText() ) == MailConnectionMeta.AFTER_GET_IMAP_MOVE ) );
    wlMoveToFolder.setEnabled( activeMoveToFolfer );
    wMoveToFolder.setEnabled( activeMoveToFolfer );
    wTestMoveToFolder.setEnabled( activeMoveToFolfer );
    wSelectMoveToFolder.setEnabled( activeMoveToFolfer );
    wlcreateMoveToFolder.setEnabled( activeMoveToFolfer );
    wcreateMoveToFolder.setEnabled( activeMoveToFolfer );
  }

  public void chooseListMails() {
    boolean ok =
      ( wProtocol.getText().equals( MailConnectionMeta.PROTOCOL_STRING_POP3 ) && wListmails.getSelectionIndex() == 1 );
    wlFirstmails.setEnabled( ok );
    wFirstmails.setEnabled( ok );
  }

  public void dispose() {
    closeMailConnection();
    WindowProperty winprop = new WindowProperty( shell );
    props.setScreen( winprop );
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
    boolean isPop3 = StringUtils.equals( protocol, MailConnectionMeta.PROTOCOL_STRING_POP3 );
    wProtocol.setText( protocol );
    int i = jobEntry.getRetrievemails();

    if ( i > 0 ) {
      if ( isPop3 ) {
        wListmails.select( i - 1 );
      } else {
        wListmails.select( i );
      }
    } else {
      wListmails.select( 0 ); // Retrieve All Mails
    }

    if ( jobEntry.getFirstMails() != null ) {
      wFirstmails.setText( jobEntry.getFirstMails() );
    }

    wDelete.setSelection( jobEntry.getDelete() );
    wIMAPListmails.setText( MailConnectionMeta.getValueImapListDesc( jobEntry.getValueImapList() ) );
    if ( jobEntry.getIMAPFolder() != null ) {
      wIMAPFolder.setText( jobEntry.getIMAPFolder() );
    }
    // search term
    if ( jobEntry.getSenderSearchTerm() != null ) {
      wSender.setText( jobEntry.getSenderSearchTerm() );
    }
    wNegateSender.setSelection( jobEntry.isNotTermSenderSearch() );
    if ( jobEntry.getReceipientSearch() != null ) {
      wReceipient.setText( jobEntry.getReceipientSearch() );
    }
    wNegateReceipient.setSelection( jobEntry.isNotTermReceipientSearch() );
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
    wcreateMoveToFolder.setSelection( jobEntry.isCreateMoveToFolder() );
    wcreateLocalFolder.setSelection( jobEntry.isCreateLocalFolder() );
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
      wIMAPFirstmails.setText( jobEntry.getFirstIMAPMails() );
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
    int actualIndex = wListmails.getSelectionIndex();
    jobEntry.setRetrievemails( actualIndex > 0 ? 2 : 0 );

    jobEntry.setFirstMails( wFirstmails.getText() );
    jobEntry.setDelete( wDelete.getSelection() );
    jobEntry.setProtocol( wProtocol.getText() );
    jobEntry.setAttachmentWildcard( wAttachmentWildcard.getText() );
    jobEntry.setValueImapList( MailConnectionMeta.getValueImapListByDesc( wIMAPListmails.getText() ) );
    jobEntry.setFirstIMAPMails( wIMAPFirstmails.getText() );
    jobEntry.setIMAPFolder( wIMAPFolder.getText() );
    // search term
    jobEntry.setSenderSearchTerm( wSender.getText() );
    jobEntry.setNotTermSenderSearch( wNegateSender.getSelection() );

    jobEntry.setReceipientSearch( wReceipient.getText() );
    jobEntry.setNotTermReceipientSearch( wNegateReceipient.getSelection() );
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
    jobEntry.setCreateMoveToFolder( wcreateMoveToFolder.getSelection() );
    jobEntry.setCreateLocalFolder( wcreateLocalFolder.getSelection() );
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
