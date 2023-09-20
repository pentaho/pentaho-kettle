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

package org.pentaho.di.ui.job.entries.ftpdelete;

import java.net.InetAddress;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.annotations.PluginDialog;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.Props;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entries.ftpdelete.JobEntryFTPDelete;
import org.pentaho.di.job.entries.ftpsget.FTPSConnection;
import org.pentaho.di.job.entries.sftp.SFTPClient;
import org.pentaho.di.job.entry.JobEntryDialogInterface;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.ui.core.events.dialog.FilterType;
import org.pentaho.di.ui.core.events.dialog.SelectionAdapterFileDialogTextVar;
import org.pentaho.di.ui.core.events.dialog.SelectionAdapterOptions;
import org.pentaho.di.ui.core.events.dialog.SelectionOperation;
import org.pentaho.di.ui.core.gui.WindowProperty;
import org.pentaho.di.ui.core.widget.LabelText;
import org.pentaho.di.ui.core.widget.LabelTextVar;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.job.dialog.JobDialog;
import org.pentaho.di.ui.job.entry.JobEntryDialog;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

import com.enterprisedt.net.ftp.FTPClient;
import com.trilead.ssh2.Connection;
import com.trilead.ssh2.HTTPProxyData;
import com.trilead.ssh2.SFTPv3Client;
import com.trilead.ssh2.SFTPv3FileAttributes;

/**
 * This dialog allows you to edit the FTP Delete job entry settings.
 *
 * @author Samatar
 * @since 27-04-2008
 */
@PluginDialog( id = "FTP_DELETE", image = "FTPD.svg", pluginType = PluginDialog.PluginType.JOBENTRY,
        documentationUrl = "http://wiki.pentaho.com/display/EAI/FTP+Delete" )
public class JobEntryFTPDeleteDialog extends JobEntryDialog implements JobEntryDialogInterface {
  private static Class<?> PKG = JobEntryFTPDelete.class; // for i18n purposes, needed by Translator2!!

  private LabelText wName;

  private FormData fdName;

  private LabelTextVar wServerName;

  private FormData fdServerName;

  private LabelTextVar wUserName;

  private FormData fdUserName;

  private LabelTextVar wPassword;

  private FormData fdPassword;

  private TextVar wFtpDirectory;
  private Label wlFtpDirectory;

  private FormData fdFtpDirectory;
  private FormData fdlFtpDirectory;

  private LabelTextVar wWildcard;

  private FormData fdWildcard;

  private Label wluseProxy;

  private Button wuseProxy;

  private FormData fdluseProxy, fduseProxy;

  private LabelTextVar wTimeout;

  private FormData fdTimeout;

  private Label wlActive;

  private Button wActive;

  private FormData fdlActive, fdActive;

  private Label wlConnectionType;
  private FormData fdlConnectionType;
  private CCombo wConnectionType;
  private FormData fdConnectionType;

  private Button wOK, wCancel;

  private Listener lsOK, lsCancel;

  private JobEntryFTPDelete jobEntry;

  private Shell shell;

  private SelectionAdapter lsDef;

  private Label wlProtocol;

  private Combo wProtocol;

  private FormData fdlProtocol, fdProtocol;

  private Label wlusePublicKey;

  private Button wusePublicKey;

  private FormData fdlusePublicKey, fdusePublicKey;

  private boolean changed;

  private Group wServerSettings;
  private FormData fdServerSettings;

  private CTabFolder wTabFolder;
  private Composite wGeneralComp, wFilesComp, wSocksProxyComp;
  private CTabItem wGeneralTab, wFilesTab, wSocksProxyTab;
  private FormData fdGeneralComp, fdFilesComp;
  private FormData fdTabFolder;
  private Group wSocksProxy;
  private LabelTextVar wSocksProxyHost, wSocksProxyPort, wSocksProxyUsername, wSocksProxyPassword;
  private FormData fdSocksProxyComp, fdSocksProxyHost, fdSocksProxyPort, fdSocksProxyUsername, fdSocksProxyPassword;

  private LabelTextVar wPort;

  private FormData fdPort;

  private LabelTextVar wProxyHost;

  private FormData fdProxyHost;

  private LabelTextVar wProxyPort;

  private FormData fdProxyPort;

  private LabelTextVar wProxyUsername;

  private FormData fdProxyUsername;

  private LabelTextVar wProxyPassword;

  private FormData fdProxyPasswd;

  private Button wTest;

  private FormData fdTest;

  private Listener lsTest;

  private Listener lsCheckFolder;

  private Group wAdvancedSettings;
  private FormData fdAdvancedSettings;

  private Group wRemoteSettings;
  private FormData fdRemoteSettings;

  private Button wbTestChangeFolderExists;
  private FormData fdbTestChangeFolderExists;

  private Group wSuccessOn;
  private FormData fdSuccessOn;

  private Label wlNrErrorsLessThan;
  private TextVar wNrErrorsLessThan;
  private FormData fdlNrErrorsLessThan, fdNrErrorsLessThan;

  private Label wlSuccessCondition;
  private CCombo wSuccessCondition;
  private FormData fdlSuccessCondition, fdSuccessCondition;

  private LabelTextVar wkeyfilePass;

  private FormData fdkeyfilePass;

  private Label wlKeyFilename;

  private Button wbKeyFilename;

  private TextVar wKeyFilename;

  private FormData fdlKeyFilename, fdbKeyFilename, fdKeyFilename;

  private Label wlgetPrevious;

  private Button wgetPrevious;

  private FormData fdlgetPrevious, fdgetPrevious;

  private FTPSConnection ftpsclient = null;
  private FTPClient ftpclient = null;
  private SFTPClient sftpclient = null;
  private Connection conn = null;
  private String pwdFolder = null;

  private static final String[] FILETYPES = new String[] {
    BaseMessages.getString( PKG, "JobFTPDelete.Filetype.Pem" ),
    BaseMessages.getString( PKG, "JobFTPDelete.Filetype.All" ) };

  public JobEntryFTPDeleteDialog( Shell parent, JobEntryInterface jobEntryInt, Repository rep, JobMeta jobMeta ) {
    super( parent, jobEntryInt, rep, jobMeta );
    jobEntry = (JobEntryFTPDelete) jobEntryInt;
    if ( this.jobEntry.getName() == null ) {
      this.jobEntry.setName( BaseMessages.getString( PKG, "JobFTPDelete.Name.Default" ) );
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
        pwdFolder = null;
        ftpclient = null;
        ftpsclient = null;
        sftpclient = null;
        conn = null;
        jobEntry.setChanged();
      }
    };
    changed = jobEntry.hasChanged();

    FormLayout formLayout = new FormLayout();
    formLayout.marginWidth = Const.FORM_MARGIN;
    formLayout.marginHeight = Const.FORM_MARGIN;

    shell.setLayout( formLayout );
    shell.setText( BaseMessages.getString( PKG, "JobFTPDelete.Title" ) );

    int middle = props.getMiddlePct();
    int margin = Const.MARGIN;

    // Job entry name line
    wName =
      new LabelText( shell, BaseMessages.getString( PKG, "JobFTPDelete.Name.Label" ), BaseMessages.getString(
        PKG, "JobFTPDelete.Name.Tooltip" ) );
    wName.addModifyListener( lsMod );
    fdName = new FormData();
    fdName.top = new FormAttachment( 0, 0 );
    fdName.left = new FormAttachment( 0, 0 );
    fdName.right = new FormAttachment( 100, 0 );
    wName.setLayoutData( fdName );

    wTabFolder = new CTabFolder( shell, SWT.BORDER );
    props.setLook( wTabFolder, Props.WIDGET_STYLE_TAB );

    // ////////////////////////
    // START OF GENERAL TAB ///
    // ////////////////////////

    wGeneralTab = new CTabItem( wTabFolder, SWT.NONE );
    wGeneralTab.setText( BaseMessages.getString( PKG, "JobFTPDelete.Tab.General.Label" ) );

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
    wServerSettings.setText( BaseMessages.getString( PKG, "JobFTPDelete.ServerSettings.Group.Label" ) );

    FormLayout ServerSettingsgroupLayout = new FormLayout();
    ServerSettingsgroupLayout.marginWidth = 10;
    ServerSettingsgroupLayout.marginHeight = 10;

    wServerSettings.setLayout( ServerSettingsgroupLayout );

    // Protocol
    wlProtocol = new Label( wServerSettings, SWT.RIGHT );
    wlProtocol.setText( BaseMessages.getString( PKG, "JobFTPDelete.Protocol.Label" ) );
    props.setLook( wlProtocol );
    fdlProtocol = new FormData();
    fdlProtocol.left = new FormAttachment( 0, 0 );
    fdlProtocol.top = new FormAttachment( wName, margin );
    fdlProtocol.right = new FormAttachment( middle, 0 );
    wlProtocol.setLayoutData( fdlProtocol );
    wProtocol = new Combo( wServerSettings, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    wProtocol.setToolTipText( BaseMessages.getString( PKG, "JobFTPDelete.Protocol.Tooltip" ) );
    wProtocol.add( JobEntryFTPDelete.PROTOCOL_FTP );
    wProtocol.add( JobEntryFTPDelete.PROTOCOL_FTPS );
    wProtocol.add( JobEntryFTPDelete.PROTOCOL_SFTP );
    wProtocol.add( JobEntryFTPDelete.PROTOCOL_SSH );
    props.setLook( wProtocol );
    fdProtocol = new FormData();
    fdProtocol.left = new FormAttachment( middle, margin );
    fdProtocol.top = new FormAttachment( wName, margin );
    fdProtocol.right = new FormAttachment( 100, 0 );
    wProtocol.setLayoutData( fdProtocol );
    wProtocol.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e ) {
        activeFTPProtocol();
        jobEntry.setChanged();
      }
    } );

    // ServerName line
    wServerName =
      new LabelTextVar(
        jobMeta, wServerSettings, BaseMessages.getString( PKG, "JobFTPDelete.Server.Label" ), BaseMessages
          .getString( PKG, "JobFTPDelete.Server.Tooltip" ) );
    props.setLook( wServerName );
    wServerName.addModifyListener( lsMod );
    fdServerName = new FormData();
    fdServerName.left = new FormAttachment( 0, 0 );
    fdServerName.top = new FormAttachment( wProtocol, margin );
    fdServerName.right = new FormAttachment( 100, 0 );
    wServerName.setLayoutData( fdServerName );

    // Proxy port line
    wPort =
      new LabelTextVar(
        jobMeta, wServerSettings, BaseMessages.getString( PKG, "JobFTPDelete.Port.Label" ), BaseMessages
          .getString( PKG, "JobFTPDelete.Port.Tooltip" ) );
    props.setLook( wPort );
    wPort.addModifyListener( lsMod );
    fdPort = new FormData();
    fdPort.left = new FormAttachment( 0, 0 );
    fdPort.top = new FormAttachment( wServerName, margin );
    fdPort.right = new FormAttachment( 100, 0 );
    wPort.setLayoutData( fdPort );

    // UserName line
    wUserName =
      new LabelTextVar(
        jobMeta, wServerSettings, BaseMessages.getString( PKG, "JobFTPDelete.User.Label" ), BaseMessages
          .getString( PKG, "JobFTPDelete.User.Tooltip" ) );
    props.setLook( wUserName );
    wUserName.addModifyListener( lsMod );
    fdUserName = new FormData();
    fdUserName.left = new FormAttachment( 0, 0 );
    fdUserName.top = new FormAttachment( wPort, margin );
    fdUserName.right = new FormAttachment( 100, 0 );
    wUserName.setLayoutData( fdUserName );

    // Password line
    wPassword =
      new LabelTextVar(
        jobMeta, wServerSettings, BaseMessages.getString( PKG, "JobFTPDelete.Password.Label" ), BaseMessages
          .getString( PKG, "JobFTPDelete.Password.Tooltip" ), true );
    props.setLook( wPassword );
    wPassword.addModifyListener( lsMod );
    fdPassword = new FormData();
    fdPassword.left = new FormAttachment( 0, 0 );
    fdPassword.top = new FormAttachment( wUserName, margin );
    fdPassword.right = new FormAttachment( 100, 0 );
    wPassword.setLayoutData( fdPassword );

    wlConnectionType = new Label( wServerSettings, SWT.RIGHT );
    wlConnectionType.setText( BaseMessages.getString( PKG, "JobFTPDelete.ConnectionType.Label" ) );
    props.setLook( wlConnectionType );
    fdlConnectionType = new FormData();
    fdlConnectionType.left = new FormAttachment( 0, 0 );
    fdlConnectionType.right = new FormAttachment( middle, 0 );
    fdlConnectionType.top = new FormAttachment( wPassword, 2 * margin );
    wlConnectionType.setLayoutData( fdlConnectionType );
    wConnectionType = new CCombo( wServerSettings, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER );
    wConnectionType.setItems( FTPSConnection.connection_type_Desc );
    props.setLook( wConnectionType );
    fdConnectionType = new FormData();
    fdConnectionType.left = new FormAttachment( middle, margin );
    fdConnectionType.top = new FormAttachment( wPassword, 2 * margin );
    fdConnectionType.right = new FormAttachment( 100, 0 );
    wConnectionType.setLayoutData( fdConnectionType );
    wConnectionType.addModifyListener( lsMod );

    // Use proxy...
    wluseProxy = new Label( wServerSettings, SWT.RIGHT );
    wluseProxy.setText( BaseMessages.getString( PKG, "JobFTPDelete.useProxy.Label" ) );
    props.setLook( wluseProxy );
    fdluseProxy = new FormData();
    fdluseProxy.left = new FormAttachment( 0, 0 );
    fdluseProxy.top = new FormAttachment( wConnectionType, margin );
    fdluseProxy.right = new FormAttachment( middle, 0 );
    wluseProxy.setLayoutData( fdluseProxy );
    wuseProxy = new Button( wServerSettings, SWT.CHECK );
    props.setLook( wuseProxy );
    wuseProxy.setToolTipText( BaseMessages.getString( PKG, "JobFTPDelete.useProxy.Tooltip" ) );
    fduseProxy = new FormData();
    fduseProxy.left = new FormAttachment( middle, margin );
    fduseProxy.top = new FormAttachment( wConnectionType, margin );
    fduseProxy.right = new FormAttachment( 100, 0 );
    wuseProxy.setLayoutData( fduseProxy );
    wuseProxy.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e ) {
        activeProxy();
        jobEntry.setChanged();
      }
    } );

    // Proxy host line
    wProxyHost =
      new LabelTextVar(
        jobMeta, wServerSettings, BaseMessages.getString( PKG, "JobFTPDelete.ProxyHost.Label" ), BaseMessages
          .getString( PKG, "JobFTPDelete.ProxyHost.Tooltip" ) );
    props.setLook( wProxyHost );
    wProxyHost.addModifyListener( lsMod );
    fdProxyHost = new FormData();
    fdProxyHost.left = new FormAttachment( 0, 0 );
    fdProxyHost.top = new FormAttachment( wuseProxy, margin );
    fdProxyHost.right = new FormAttachment( 100, 0 );
    wProxyHost.setLayoutData( fdProxyHost );

    // Proxy port line
    wProxyPort =
      new LabelTextVar(
        jobMeta, wServerSettings, BaseMessages.getString( PKG, "JobFTPDelete.ProxyPort.Label" ), BaseMessages
          .getString( PKG, "JobFTPDelete.ProxyPort.Tooltip" ) );
    props.setLook( wProxyPort );
    wProxyPort.addModifyListener( lsMod );
    fdProxyPort = new FormData();
    fdProxyPort.left = new FormAttachment( 0, 0 );
    fdProxyPort.top = new FormAttachment( wProxyHost, margin );
    fdProxyPort.right = new FormAttachment( 100, 0 );
    wProxyPort.setLayoutData( fdProxyPort );

    // Proxy username line
    wProxyUsername =
      new LabelTextVar( jobMeta, wServerSettings,
        BaseMessages.getString( PKG, "JobFTPDelete.ProxyUsername.Label" ),
        BaseMessages.getString( PKG, "JobFTPDelete.ProxyUsername.Tooltip" ) );
    props.setLook( wProxyUsername );
    wProxyUsername.addModifyListener( lsMod );
    fdProxyUsername = new FormData();
    fdProxyUsername.left = new FormAttachment( 0, 0 );
    fdProxyUsername.top = new FormAttachment( wProxyPort, margin );
    fdProxyUsername.right = new FormAttachment( 100, 0 );
    wProxyUsername.setLayoutData( fdProxyUsername );

    // Proxy password line
    wProxyPassword =
      new LabelTextVar( jobMeta, wServerSettings,
        BaseMessages.getString( PKG, "JobFTPDelete.ProxyPassword.Label" ),
        BaseMessages.getString( PKG, "JobFTPDelete.ProxyPassword.Tooltip" ), true );
    props.setLook( wProxyPassword );
    wProxyPassword.addModifyListener( lsMod );
    fdProxyPasswd = new FormData();
    fdProxyPasswd.left = new FormAttachment( 0, 0 );
    fdProxyPasswd.top = new FormAttachment( wProxyUsername, margin );
    fdProxyPasswd.right = new FormAttachment( 100, 0 );
    wProxyPassword.setLayoutData( fdProxyPasswd );

    // usePublicKey
    wlusePublicKey = new Label( wServerSettings, SWT.RIGHT );
    wlusePublicKey.setText( BaseMessages.getString( PKG, "JobFTPDelete.usePublicKeyFiles.Label" ) );
    props.setLook( wlusePublicKey );
    fdlusePublicKey = new FormData();
    fdlusePublicKey.left = new FormAttachment( 0, 0 );
    fdlusePublicKey.top = new FormAttachment( wProxyPassword, margin );
    fdlusePublicKey.right = new FormAttachment( middle, 0 );
    wlusePublicKey.setLayoutData( fdlusePublicKey );
    wusePublicKey = new Button( wServerSettings, SWT.CHECK );
    wusePublicKey.setToolTipText( BaseMessages.getString( PKG, "JobFTPDelete.usePublicKeyFiles.Tooltip" ) );
    props.setLook( wusePublicKey );
    fdusePublicKey = new FormData();
    fdusePublicKey.left = new FormAttachment( middle, margin );
    fdusePublicKey.top = new FormAttachment( wProxyPassword, margin );
    fdusePublicKey.right = new FormAttachment( 100, 0 );
    wusePublicKey.setLayoutData( fdusePublicKey );
    wusePublicKey.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e ) {
        activeUsePublicKey();
        jobEntry.setChanged();
      }
    } );

    // Key File
    wlKeyFilename = new Label( wServerSettings, SWT.RIGHT );
    wlKeyFilename.setText( BaseMessages.getString( PKG, "JobFTPDelete.KeyFilename.Label" ) );
    props.setLook( wlKeyFilename );
    fdlKeyFilename = new FormData();
    fdlKeyFilename.left = new FormAttachment( 0, 0 );
    fdlKeyFilename.top = new FormAttachment( wusePublicKey, margin );
    fdlKeyFilename.right = new FormAttachment( middle, -margin );
    wlKeyFilename.setLayoutData( fdlKeyFilename );

    wbKeyFilename = new Button( wServerSettings, SWT.PUSH | SWT.CENTER );
    props.setLook( wbKeyFilename );
    wbKeyFilename.setText( BaseMessages.getString( PKG, "System.Button.Browse" ) );
    fdbKeyFilename = new FormData();
    fdbKeyFilename.right = new FormAttachment( 100, 0 );
    fdbKeyFilename.top = new FormAttachment( wusePublicKey, 0 );
    // fdbKeyFilename.height = 22;
    wbKeyFilename.setLayoutData( fdbKeyFilename );

    wKeyFilename = new TextVar( jobMeta, wServerSettings, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    wKeyFilename.setToolTipText( BaseMessages.getString( PKG, "JobFTPDelete.KeyFilename.Tooltip" ) );
    props.setLook( wKeyFilename );
    wKeyFilename.addModifyListener( lsMod );
    fdKeyFilename = new FormData();
    fdKeyFilename.left = new FormAttachment( middle, margin );
    fdKeyFilename.top = new FormAttachment( wusePublicKey, margin );
    fdKeyFilename.right = new FormAttachment( wbKeyFilename, -margin );
    wKeyFilename.setLayoutData( fdKeyFilename );

    // Whenever something changes, set the tooltip to the expanded version:
    wKeyFilename.addModifyListener( new ModifyListener() {
      public void modifyText( ModifyEvent e ) {
        wKeyFilename.setToolTipText( jobMeta.environmentSubstitute( wKeyFilename.getText() ) );
      }
    } );

    wbKeyFilename.addSelectionListener( new SelectionAdapterFileDialogTextVar( jobMeta.getLogChannel(), wKeyFilename, jobMeta,
            new SelectionAdapterOptions( SelectionOperation.FILE,
                    new FilterType[] { FilterType.ALL, FilterType.PEM }, FilterType.PEM  ) ) );

    // keyfilePass line
    wkeyfilePass =
      new LabelTextVar(
        jobMeta, wServerSettings, BaseMessages.getString( PKG, "JobFTPDelete.keyfilePass.Label" ),
        BaseMessages.getString( PKG, "JobFTPDelete.keyfilePass.Tooltip" ), true );
    props.setLook( wkeyfilePass );
    wkeyfilePass.addModifyListener( lsMod );
    fdkeyfilePass = new FormData();
    fdkeyfilePass.left = new FormAttachment( 0, 0 );
    fdkeyfilePass.top = new FormAttachment( wKeyFilename, margin );
    fdkeyfilePass.right = new FormAttachment( 100, 0 );
    wkeyfilePass.setLayoutData( fdkeyfilePass );

    // Test connection button
    wTest = new Button( wServerSettings, SWT.PUSH );
    wTest.setText( BaseMessages.getString( PKG, "JobFTPDelete.TestConnection.Label" ) );
    props.setLook( wTest );
    fdTest = new FormData();
    wTest.setToolTipText( BaseMessages.getString( PKG, "JobFTPDelete.TestConnection.Tooltip" ) );
    // fdTest.left = new FormAttachment(middle, 0);
    fdTest.top = new FormAttachment( wkeyfilePass, margin );
    fdTest.right = new FormAttachment( 100, 0 );
    wTest.setLayoutData( fdTest );

    fdServerSettings = new FormData();
    fdServerSettings.left = new FormAttachment( 0, margin );
    fdServerSettings.top = new FormAttachment( wName, margin );
    fdServerSettings.right = new FormAttachment( 100, -margin );
    wServerSettings.setLayoutData( fdServerSettings );
    // ///////////////////////////////////////////////////////////
    // / END OF SERVER SETTINGS GROUP
    // ///////////////////////////////////////////////////////////

    fdGeneralComp = new FormData();
    fdGeneralComp.left = new FormAttachment( 0, 0 );
    fdGeneralComp.top = new FormAttachment( 0, 0 );
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
    // START OF Advanced TAB ///
    // ////////////////////////

    wFilesTab = new CTabItem( wTabFolder, SWT.NONE );
    wFilesTab.setText( BaseMessages.getString( PKG, "JobFTPDelete.Tab.Files.Label" ) );

    wFilesComp = new Composite( wTabFolder, SWT.NONE );
    props.setLook( wFilesComp );

    FormLayout AdvancedLayout = new FormLayout();
    AdvancedLayout.marginWidth = 3;
    AdvancedLayout.marginHeight = 3;
    wFilesComp.setLayout( AdvancedLayout );

    // ////////////////////////
    // START OF Advanced SETTINGS GROUP///
    // /
    wAdvancedSettings = new Group( wFilesComp, SWT.SHADOW_NONE );
    props.setLook( wAdvancedSettings );
    wAdvancedSettings.setText( BaseMessages.getString( PKG, "JobFTPDelete.AdvancedSettings.Group.Label" ) );

    FormLayout AdvancedSettingsgroupLayout = new FormLayout();
    AdvancedSettingsgroupLayout.marginWidth = 10;
    AdvancedSettingsgroupLayout.marginHeight = 10;

    wAdvancedSettings.setLayout( AdvancedSettingsgroupLayout );

    // Timeout line
    wTimeout =
      new LabelTextVar(
        jobMeta, wAdvancedSettings, BaseMessages.getString( PKG, "JobFTPDelete.Timeout.Label" ), BaseMessages
          .getString( PKG, "JobFTPDelete.Timeout.Tooltip" ) );
    props.setLook( wTimeout );
    wTimeout.addModifyListener( lsMod );
    fdTimeout = new FormData();
    fdTimeout.left = new FormAttachment( 0, 0 );
    fdTimeout.top = new FormAttachment( wActive, margin );
    fdTimeout.right = new FormAttachment( 100, 0 );
    wTimeout.setLayoutData( fdTimeout );

    // active connection?
    wlActive = new Label( wAdvancedSettings, SWT.RIGHT );
    wlActive.setText( BaseMessages.getString( PKG, "JobFTPDelete.ActiveConns.Label" ) );
    props.setLook( wlActive );
    fdlActive = new FormData();
    fdlActive.left = new FormAttachment( 0, 0 );
    fdlActive.top = new FormAttachment( wTimeout, margin );
    fdlActive.right = new FormAttachment( middle, 0 );
    wlActive.setLayoutData( fdlActive );
    wActive = new Button( wAdvancedSettings, SWT.CHECK );
    wActive.setToolTipText( BaseMessages.getString( PKG, "JobFTPDelete.ActiveConns.Tooltip" ) );
    props.setLook( wActive );
    fdActive = new FormData();
    fdActive.left = new FormAttachment( middle, margin );
    fdActive.top = new FormAttachment( wTimeout, margin );
    fdActive.right = new FormAttachment( 100, 0 );
    wActive.setLayoutData( fdActive );

    fdAdvancedSettings = new FormData();
    fdAdvancedSettings.left = new FormAttachment( 0, margin );
    fdAdvancedSettings.top = new FormAttachment( 0, margin );
    fdAdvancedSettings.right = new FormAttachment( 100, -margin );
    wAdvancedSettings.setLayoutData( fdAdvancedSettings );
    // ///////////////////////////////////////////////////////////
    // / END OF Advanced SETTINGS GROUP
    // ///////////////////////////////////////////////////////////

    // ////////////////////////
    // START OF Remote SETTINGS GROUP///
    // /
    wRemoteSettings = new Group( wFilesComp, SWT.SHADOW_NONE );
    props.setLook( wRemoteSettings );
    wRemoteSettings.setText( BaseMessages.getString( PKG, "JobFTPDelete.RemoteSettings.Group.Label" ) );

    FormLayout RemoteSettinsgroupLayout = new FormLayout();
    RemoteSettinsgroupLayout.marginWidth = 10;
    RemoteSettinsgroupLayout.marginHeight = 10;

    wRemoteSettings.setLayout( RemoteSettinsgroupLayout );

    // Get arguments from previous result...
    wlgetPrevious = new Label( wRemoteSettings, SWT.RIGHT );
    wlgetPrevious.setText( BaseMessages.getString( PKG, "JobFTPDelete.getPrevious.Label" ) );
    props.setLook( wlgetPrevious );
    fdlgetPrevious = new FormData();
    fdlgetPrevious.left = new FormAttachment( 0, 0 );
    fdlgetPrevious.top = new FormAttachment( wAdvancedSettings, margin );
    fdlgetPrevious.right = new FormAttachment( middle, 0 );
    wlgetPrevious.setLayoutData( fdlgetPrevious );
    wgetPrevious = new Button( wRemoteSettings, SWT.CHECK );
    props.setLook( wgetPrevious );
    wgetPrevious.setToolTipText( BaseMessages.getString( PKG, "JobFTPDelete.getPrevious.Tooltip" ) );
    fdgetPrevious = new FormData();
    fdgetPrevious.left = new FormAttachment( middle, margin );
    fdgetPrevious.top = new FormAttachment( wAdvancedSettings, margin );
    fdgetPrevious.right = new FormAttachment( 100, 0 );
    wgetPrevious.setLayoutData( fdgetPrevious );
    wgetPrevious.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e ) {
        activeCopyFromPrevious();
        jobEntry.setChanged();
      }
    } );

    // FTP directory
    wlFtpDirectory = new Label( wRemoteSettings, SWT.RIGHT );
    wlFtpDirectory.setText( BaseMessages.getString( PKG, "JobFTPDelete.RemoteDir.Label" ) );
    props.setLook( wlFtpDirectory );
    fdlFtpDirectory = new FormData();
    fdlFtpDirectory.left = new FormAttachment( 0, 0 );
    fdlFtpDirectory.top = new FormAttachment( wgetPrevious, margin );
    fdlFtpDirectory.right = new FormAttachment( middle, 0 );
    wlFtpDirectory.setLayoutData( fdlFtpDirectory );

    // Test remote folder button ...
    wbTestChangeFolderExists = new Button( wRemoteSettings, SWT.PUSH | SWT.CENTER );
    props.setLook( wbTestChangeFolderExists );
    wbTestChangeFolderExists.setText( BaseMessages.getString( PKG, "JobFTPDelete.TestFolderExists.Label" ) );
    fdbTestChangeFolderExists = new FormData();
    fdbTestChangeFolderExists.right = new FormAttachment( 100, 0 );
    fdbTestChangeFolderExists.top = new FormAttachment( wgetPrevious, margin );
    wbTestChangeFolderExists.setLayoutData( fdbTestChangeFolderExists );

    wFtpDirectory =
      new TextVar( jobMeta, wRemoteSettings, SWT.SINGLE | SWT.LEFT | SWT.BORDER, BaseMessages.getString(
        PKG, "JobFTPDelete.RemoteDir.Tooltip" ) );
    props.setLook( wFtpDirectory );
    wFtpDirectory.addModifyListener( lsMod );
    fdFtpDirectory = new FormData();
    fdFtpDirectory.left = new FormAttachment( middle, margin );
    fdFtpDirectory.top = new FormAttachment( wgetPrevious, margin );
    fdFtpDirectory.right = new FormAttachment( wbTestChangeFolderExists, -margin );
    wFtpDirectory.setLayoutData( fdFtpDirectory );

    // Wildcard line
    wWildcard =
      new LabelTextVar(
        jobMeta, wRemoteSettings, BaseMessages.getString( PKG, "JobFTPDelete.Wildcard.Label" ), BaseMessages
          .getString( PKG, "JobFTPDelete.Wildcard.Tooltip" ) );
    props.setLook( wWildcard );
    wWildcard.addModifyListener( lsMod );
    fdWildcard = new FormData();
    fdWildcard.left = new FormAttachment( 0, 0 );
    fdWildcard.top = new FormAttachment( wFtpDirectory, margin );
    fdWildcard.right = new FormAttachment( 100, 0 );
    wWildcard.setLayoutData( fdWildcard );

    fdRemoteSettings = new FormData();
    fdRemoteSettings.left = new FormAttachment( 0, margin );
    fdRemoteSettings.top = new FormAttachment( wAdvancedSettings, margin );
    fdRemoteSettings.right = new FormAttachment( 100, -margin );
    wRemoteSettings.setLayoutData( fdRemoteSettings );
    // ///////////////////////////////////////////////////////////
    // / END OF Remote SETTINGSGROUP
    // ///////////////////////////////////////////////////////////

    // SuccessOngrouping?
    // ////////////////////////
    // START OF SUCCESS ON GROUP///
    // /
    wSuccessOn = new Group( wFilesComp, SWT.SHADOW_NONE );
    props.setLook( wSuccessOn );
    wSuccessOn.setText( BaseMessages.getString( PKG, "JobFTPDelete.SuccessOn.Group.Label" ) );

    FormLayout successongroupLayout = new FormLayout();
    successongroupLayout.marginWidth = 10;
    successongroupLayout.marginHeight = 10;

    wSuccessOn.setLayout( successongroupLayout );

    // Success Condition
    wlSuccessCondition = new Label( wSuccessOn, SWT.RIGHT );
    wlSuccessCondition.setText( BaseMessages.getString( PKG, "JobFTPDelete.SuccessCondition.Label" ) + " " );
    props.setLook( wlSuccessCondition );
    fdlSuccessCondition = new FormData();
    fdlSuccessCondition.left = new FormAttachment( 0, 0 );
    fdlSuccessCondition.right = new FormAttachment( middle, 0 );
    fdlSuccessCondition.top = new FormAttachment( wRemoteSettings, margin );
    wlSuccessCondition.setLayoutData( fdlSuccessCondition );
    wSuccessCondition = new CCombo( wSuccessOn, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER );
    wSuccessCondition.add( BaseMessages.getString( PKG, "JobFTPDelete.SuccessWhenAllWorksFine.Label" ) );
    wSuccessCondition.add( BaseMessages.getString( PKG, "JobFTPDelete.SuccessWhenAtLeat.Label" ) );
    wSuccessCondition.add( BaseMessages.getString( PKG, "JobFTPDelete.SuccessWhenNrErrorsLessThan.Label" ) );
    wSuccessCondition.select( 0 ); // +1: starts at -1

    props.setLook( wSuccessCondition );
    fdSuccessCondition = new FormData();
    fdSuccessCondition.left = new FormAttachment( middle, 0 );
    fdSuccessCondition.top = new FormAttachment( wRemoteSettings, margin );
    fdSuccessCondition.right = new FormAttachment( 100, 0 );
    wSuccessCondition.setLayoutData( fdSuccessCondition );
    wSuccessCondition.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e ) {
        activeSuccessCondition();

      }
    } );

    // Success when number of errors less than
    wlNrErrorsLessThan = new Label( wSuccessOn, SWT.RIGHT );
    wlNrErrorsLessThan.setText( BaseMessages.getString( PKG, "JobFTPDelete.NrBadFormedLessThan.Label" ) + " " );
    props.setLook( wlNrErrorsLessThan );
    fdlNrErrorsLessThan = new FormData();
    fdlNrErrorsLessThan.left = new FormAttachment( 0, 0 );
    fdlNrErrorsLessThan.top = new FormAttachment( wSuccessCondition, margin );
    fdlNrErrorsLessThan.right = new FormAttachment( middle, -margin );
    wlNrErrorsLessThan.setLayoutData( fdlNrErrorsLessThan );

    wNrErrorsLessThan =
      new TextVar( jobMeta, wSuccessOn, SWT.SINGLE | SWT.LEFT | SWT.BORDER, BaseMessages.getString(
        PKG, "JobFTPDelete.NrBadFormedLessThan.Tooltip" ) );
    props.setLook( wNrErrorsLessThan );
    wNrErrorsLessThan.addModifyListener( lsMod );
    fdNrErrorsLessThan = new FormData();
    fdNrErrorsLessThan.left = new FormAttachment( middle, 0 );
    fdNrErrorsLessThan.top = new FormAttachment( wSuccessCondition, margin );
    fdNrErrorsLessThan.right = new FormAttachment( 100, -margin );
    wNrErrorsLessThan.setLayoutData( fdNrErrorsLessThan );

    fdSuccessOn = new FormData();
    fdSuccessOn.left = new FormAttachment( 0, margin );
    fdSuccessOn.top = new FormAttachment( wRemoteSettings, margin );
    fdSuccessOn.right = new FormAttachment( 100, -margin );
    wSuccessOn.setLayoutData( fdSuccessOn );
    // ///////////////////////////////////////////////////////////
    // / END OF Success ON GROUP
    // ///////////////////////////////////////////////////////////

    fdFilesComp = new FormData();
    fdFilesComp.left = new FormAttachment( 0, 0 );
    fdFilesComp.top = new FormAttachment( 0, 0 );
    fdFilesComp.right = new FormAttachment( 100, 0 );
    fdFilesComp.bottom = new FormAttachment( 100, 0 );
    wFilesComp.setLayoutData( fdFilesComp );

    wFilesComp.layout();
    wFilesTab.setControl( wFilesComp );
    props.setLook( wFilesComp );

    // ///////////////////////////////////////////////////////////
    // / END OF Advanced TAB
    // ///////////////////////////////////////////////////////////

    // ///////////////////////////////////////////////////////////
    // Start of Socks Proxy Tab
    // ///////////////////////////////////////////////////////////
    wSocksProxyTab = new CTabItem( wTabFolder, SWT.NONE );
    wSocksProxyTab.setText( BaseMessages.getString( PKG, "JobFTPDelete.Tab.Socks.Label" ) );

    wSocksProxyComp = new Composite( wTabFolder, SWT.NONE );
    props.setLook( wSocksProxyComp );

    FormLayout SoxProxyLayout = new FormLayout();
    SoxProxyLayout.marginWidth = 3;
    SoxProxyLayout.marginHeight = 3;
    wSocksProxyComp.setLayout( SoxProxyLayout );

    // ////////////////////////////////////////////////////////
    // Start of Proxy Group
    // ////////////////////////////////////////////////////////
    wSocksProxy = new Group( wSocksProxyComp, SWT.SHADOW_NONE );
    props.setLook( wSocksProxy );
    wSocksProxy.setText( BaseMessages.getString( PKG, "JobFTPDelete.SocksProxy.Group.Label" ) );

    FormLayout SocksProxyGroupLayout = new FormLayout();
    SocksProxyGroupLayout.marginWidth = 10;
    SocksProxyGroupLayout.marginHeight = 10;
    wSocksProxy.setLayout( SocksProxyGroupLayout );

    // host line
    wSocksProxyHost =
      new LabelTextVar(
        jobMeta, wSocksProxy, BaseMessages.getString( PKG, "JobFTPDelete.SocksProxyHost.Label" ), BaseMessages
          .getString( PKG, "JobFTPDelete.SocksProxyHost.Tooltip" ) );
    props.setLook( wSocksProxyHost );
    wSocksProxyHost.addModifyListener( lsMod );
    fdSocksProxyHost = new FormData();
    fdSocksProxyHost.left = new FormAttachment( 0, 0 );
    fdSocksProxyHost.top = new FormAttachment( wName, margin );
    fdSocksProxyHost.right = new FormAttachment( 100, margin );
    wSocksProxyHost.setLayoutData( fdSocksProxyHost );

    // port line
    wSocksProxyPort =
      new LabelTextVar(
        jobMeta, wSocksProxy, BaseMessages.getString( PKG, "JobFTPDelete.SocksProxyPort.Label" ), BaseMessages
          .getString( PKG, "JobFTPDelete.SocksProxyPort.Tooltip" ) );
    props.setLook( wSocksProxyPort );
    wSocksProxyPort.addModifyListener( lsMod );
    fdSocksProxyPort = new FormData();
    fdSocksProxyPort.left = new FormAttachment( 0, 0 );
    fdSocksProxyPort.top = new FormAttachment( wSocksProxyHost, margin );
    fdSocksProxyPort.right = new FormAttachment( 100, margin );
    wSocksProxyPort.setLayoutData( fdSocksProxyPort );

    // username line
    wSocksProxyUsername =
      new LabelTextVar( jobMeta, wSocksProxy,
        BaseMessages.getString( PKG, "JobFTPDelete.SocksProxyUsername.Label" ),
        BaseMessages.getString( PKG, "JobFTPDelete.SocksProxyPassword.Tooltip" ) );
    props.setLook( wSocksProxyUsername );
    wSocksProxyUsername.addModifyListener( lsMod );
    fdSocksProxyUsername = new FormData();
    fdSocksProxyUsername.left = new FormAttachment( 0, 0 );
    fdSocksProxyUsername.top = new FormAttachment( wSocksProxyPort, margin );
    fdSocksProxyUsername.right = new FormAttachment( 100, margin );
    wSocksProxyUsername.setLayoutData( fdSocksProxyUsername );

    // password line
    wSocksProxyPassword =
      new LabelTextVar( jobMeta, wSocksProxy,
        BaseMessages.getString( PKG, "JobFTPDelete.SocksProxyPassword.Label" ),
        BaseMessages.getString( PKG, "JobFTPDelete.SocksProxyPassword.Tooltip" ), true );
    props.setLook( wSocksProxyPort );
    wSocksProxyPassword.addModifyListener( lsMod );
    fdSocksProxyPassword = new FormData();
    fdSocksProxyPassword.left = new FormAttachment( 0, 0 );
    fdSocksProxyPassword.top = new FormAttachment( wSocksProxyUsername, margin );
    fdSocksProxyPassword.right = new FormAttachment( 100, margin );
    wSocksProxyPassword.setLayoutData( fdSocksProxyPassword );

    // ///////////////////////////////////////////////////////////////
    // End of socks proxy group
    // ///////////////////////////////////////////////////////////////

    fdSocksProxyComp = new FormData();
    fdSocksProxyComp.left = new FormAttachment( 0, margin );
    fdSocksProxyComp.top = new FormAttachment( 0, margin );
    fdSocksProxyComp.right = new FormAttachment( 100, -margin );
    wSocksProxy.setLayoutData( fdSocksProxyComp );

    wSocksProxyComp.layout();
    wSocksProxyTab.setControl( wSocksProxyComp );
    props.setLook( wSocksProxyComp );

    // ////////////////////////////////////////////////////////
    // End of Socks Proxy Tab
    // ////////////////////////////////////////////////////////

    fdTabFolder = new FormData();
    fdTabFolder.left = new FormAttachment( 0, 0 );
    fdTabFolder.top = new FormAttachment( wName, margin );
    fdTabFolder.right = new FormAttachment( 100, 0 );
    fdTabFolder.bottom = new FormAttachment( 100, -50 );
    wTabFolder.setLayoutData( fdTabFolder );

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
    lsTest = new Listener() {
      public void handleEvent( Event e ) {
        test();
      }
    };
    lsCheckFolder = new Listener() {
      public void handleEvent( Event e ) {
        checkFTPFolder();
      }
    };

    wCancel.addListener( SWT.Selection, lsCancel );
    wOK.addListener( SWT.Selection, lsOK );
    wTest.addListener( SWT.Selection, lsTest );
    wbTestChangeFolderExists.addListener( SWT.Selection, lsCheckFolder );

    lsDef = new SelectionAdapter() {
      public void widgetDefaultSelected( SelectionEvent e ) {
        ok();
      }
    };

    wName.addSelectionListener( lsDef );
    wServerName.addSelectionListener( lsDef );
    wUserName.addSelectionListener( lsDef );
    wPassword.addSelectionListener( lsDef );
    wFtpDirectory.addSelectionListener( lsDef );
    wFtpDirectory.addSelectionListener( lsDef );
    wWildcard.addSelectionListener( lsDef );
    wTimeout.addSelectionListener( lsDef );

    // Detect X or ALT-F4 or something that kills this window...
    shell.addShellListener( new ShellAdapter() {
      public void shellClosed( ShellEvent e ) {
        cancel();
      }
    } );

    getData();
    activeSuccessCondition();
    activeUsePublicKey();
    activeProxy();
    activeFTPProtocol();
    activeCopyFromPrevious();

    wTabFolder.setSelection( 0 );
    BaseStepDialog.setSize( shell );

    shell.open();
    props.setDialogSize( shell, "JobFTPDeleteDialogSize" );
    while ( !shell.isDisposed() ) {
      if ( !display.readAndDispatch() ) {
        display.sleep();
      }
    }
    return jobEntry;
  }

  private void activeCopyFromPrevious() {
    wFtpDirectory.setEnabled( !wgetPrevious.getSelection() );
    wlFtpDirectory.setEnabled( !wgetPrevious.getSelection() );
    wWildcard.setEnabled( !wgetPrevious.getSelection() );
    wbTestChangeFolderExists.setEnabled( !wgetPrevious.getSelection() );
  }

  private void activeUsePublicKey() {
    wlKeyFilename.setEnabled( wusePublicKey.getSelection() );
    wKeyFilename.setEnabled( wusePublicKey.getSelection() );
    wbKeyFilename.setEnabled( wusePublicKey.getSelection() );
    wkeyfilePass.setEnabled( wusePublicKey.getSelection() );
  }

  private void activeProxy() {
    wProxyHost.setEnabled( wuseProxy.getSelection() );
    wProxyPassword.setEnabled( wuseProxy.getSelection() );
    wProxyPort.setEnabled( wuseProxy.getSelection() );
    wProxyUsername.setEnabled( wuseProxy.getSelection() );
  }

  private void activeFTPProtocol() {
    wlConnectionType.setEnabled( wProtocol.getText().equals( JobEntryFTPDelete.PROTOCOL_FTPS ) );
    wConnectionType.setEnabled( wProtocol.getText().equals( JobEntryFTPDelete.PROTOCOL_FTPS ) );
    if ( wProtocol.getText().equals( JobEntryFTPDelete.PROTOCOL_SSH ) ) {
      wlusePublicKey.setEnabled( true );
      wusePublicKey.setEnabled( true );
      wSocksProxyHost.setEnabled( false );
    } else if ( wProtocol.getText().equals( JobEntryFTPDelete.PROTOCOL_FTP ) ) {
      wSocksProxy.setEnabled( true );
    } else {
      wusePublicKey.setSelection( false );
      activeUsePublicKey();
      wlusePublicKey.setEnabled( false );
      wusePublicKey.setEnabled( false );
      wSocksProxy.setEnabled( false );
    }
  }

  /**
   * Checks if a directory exists
   *
   * @param sftpClient
   * @param directory
   * @return true, if directory exists
   */
  public boolean sshDirectoryExists( SFTPv3Client sftpClient, String directory ) {
    try {
      SFTPv3FileAttributes attributes = sftpClient.stat( directory );

      if ( attributes != null ) {
        return ( attributes.isDirectory() );
      } else {
        return false;
      }

    } catch ( Exception e ) {
      return false;
    }
  }

  private void checkFTPFolder() {
    boolean folderexists = false;
    String errmsg = "";
    try {
      String realfoldername = jobMeta.environmentSubstitute( wFtpDirectory.getText() );
      if ( !Utils.isEmpty( realfoldername ) ) {
        if ( connect() ) {
          if ( wProtocol.getText().equals( JobEntryFTPDelete.PROTOCOL_FTP ) ) {
            ftpclient.chdir( pwdFolder );
            ftpclient.chdir( realfoldername );
            folderexists = true;
          }
          if ( wProtocol.getText().equals( JobEntryFTPDelete.PROTOCOL_FTPS ) ) {
            ftpsclient.changeDirectory( pwdFolder );
            ftpsclient.changeDirectory( realfoldername );
            folderexists = true;
          } else if ( wProtocol.getText().equals( JobEntryFTPDelete.PROTOCOL_SFTP ) ) {
            sftpclient.chdir( pwdFolder );
            sftpclient.chdir( realfoldername );
            folderexists = true;
          } else if ( wProtocol.getText().equals( JobEntryFTPDelete.PROTOCOL_SSH ) ) {
            SFTPv3Client client = new SFTPv3Client( conn );
            boolean folderexist = sshDirectoryExists( client, realfoldername );
            client.close();
            if ( folderexist ) {
              // Folder exists
              folderexists = true;
            } else {
              // we can not find folder
              folderexists = false;
            }
          }

        }
      }
    } catch ( Exception e ) {
      errmsg = e.getMessage();
    }
    if ( folderexists ) {
      MessageBox mb = new MessageBox( shell, SWT.OK | SWT.ICON_INFORMATION );
      mb.setMessage( BaseMessages.getString( PKG, "JobFTPDelete.FolderExists.OK", wFtpDirectory.getText() )
        + Const.CR );
      mb.setText( BaseMessages.getString( PKG, "JobFTPDelete.FolderExists.Title.Ok" ) );
      mb.open();
    } else {
      MessageBox mb = new MessageBox( shell, SWT.OK | SWT.ICON_ERROR );
      mb.setMessage( BaseMessages.getString( PKG, "JobFTPDelete.FolderExists.NOK", wFtpDirectory.getText() )
        + Const.CR + errmsg );
      mb.setText( BaseMessages.getString( PKG, "JobFTPDelete.FolderExists.Title.Bad" ) );
      mb.open();
    }
  }

  private boolean connect() {
    boolean connexion = false;
    if ( wProtocol.getText().equals( JobEntryFTPDelete.PROTOCOL_FTP ) ) {
      connexion = connectToFTP();
    } else if ( wProtocol.getText().equals( JobEntryFTPDelete.PROTOCOL_FTPS ) ) {
      connexion = connectToFTPS();
    } else if ( wProtocol.getText().equals( JobEntryFTPDelete.PROTOCOL_SFTP ) ) {
      connexion = connectToSFTP();
    } else if ( wProtocol.getText().equals( JobEntryFTPDelete.PROTOCOL_SSH ) ) {
      connexion = connectToSSH();
    }
    return connexion;
  }

  private void test() {

    if ( connect() ) {
      MessageBox mb = new MessageBox( shell, SWT.OK | SWT.ICON_INFORMATION );
      mb.setMessage( BaseMessages.getString( PKG, "JobFTPDelete.Connected.OK", wServerName.getText() ) + Const.CR );
      mb.setText( BaseMessages.getString( PKG, "JobFTPDelete.Connected.Title.Ok" ) );
      mb.open();
    }
  }

  private boolean connectToFTP() {
    boolean retval = false;
    String realServername = null;
    try {
      if ( ftpclient == null || !ftpclient.connected() ) {
        // Create ftp client to host:port ...
        ftpclient = new FTPClient();
        realServername = jobMeta.environmentSubstitute( wServerName.getText() );
        int realPort = Const.toInt( jobMeta.environmentSubstitute( wPort.getText() ), 21 );
        ftpclient.setRemoteAddr( InetAddress.getByName( realServername ) );
        ftpclient.setRemotePort( realPort );

        if ( !Utils.isEmpty( wProxyHost.getText() ) ) {
          String realProxy_host = jobMeta.environmentSubstitute( wProxyHost.getText() );
          ftpclient.setRemoteAddr( InetAddress.getByName( realProxy_host ) );

          int port = Const.toInt( jobMeta.environmentSubstitute( wProxyPort.getText() ), 21 );
          if ( port != 0 ) {
            ftpclient.setRemotePort( port );
          }
        }

        // login to ftp host ...
        ftpclient.connect();
        String realUsername =
          jobMeta.environmentSubstitute( wUserName.getText() )
            + ( !Utils.isEmpty( wProxyHost.getText() ) ? "@" + realServername : "" )
            + ( !Utils.isEmpty( wProxyUsername.getText() ) ? " "
              + jobMeta.environmentSubstitute( wProxyUsername.getText() ) : "" );

        String realPassword =
          Utils.resolvePassword( jobMeta, wPassword.getText() )
            + ( !Utils.isEmpty( wProxyPassword.getText() ) ? " "
              + Utils.resolvePassword( jobMeta, wProxyPassword.getText() ) : "" );
        // login now ...
        ftpclient.login( realUsername, realPassword );
        pwdFolder = ftpclient.pwd();
      }
      retval = true;
    } catch ( Exception e ) {
      if ( ftpclient != null ) {
        try {
          ftpclient.quit();
        } catch ( Exception ignored ) {
          // We've tried quitting the FTP Client exception
          // nothing else to be done if the FTP Client was already disconnected
        }
        ftpclient = null;
      }
      MessageBox mb = new MessageBox( shell, SWT.OK | SWT.ICON_ERROR );
      mb.setMessage( BaseMessages.getString( PKG, "JobFTPDelete.ErrorConnect.NOK", realServername,
          e.getMessage() ) + Const.CR );
      mb.setText( BaseMessages.getString( PKG, "JobFTPDelete.ErrorConnect.Title.Bad" ) );
      mb.open();
    }
    return retval;
  }

  private boolean connectToFTPS() {
    boolean retval = false;
    try {
      if ( ftpsclient == null ) {
        String realServername = jobMeta.environmentSubstitute( wServerName.getText() );
        String realUsername = jobMeta.environmentSubstitute( wUserName.getText() );
        String realPassword = Utils.resolvePassword( jobMeta, wPassword.getText() );
        int port = Const.toInt( jobMeta.environmentSubstitute( wPort.getText() ), 0 );

        // Create ftp client to host:port ...
        ftpsclient =
          new FTPSConnection(
            FTPSConnection.getConnectionTypeByDesc( wConnectionType.getText() ), realServername, port,
            realUsername, realPassword );

        if ( !Utils.isEmpty( wProxyHost.getText() ) ) {
          // Set proxy
          String realProxy_host = jobMeta.environmentSubstitute( wProxyHost.getText() );
          String realProxy_user = jobMeta.environmentSubstitute( wProxyUsername.getText() );
          String realProxy_pass = Utils.resolvePassword( jobMeta, wProxyPassword.getText() );
          ftpsclient.setProxyHost( realProxy_host );
          int proxyport = Const.toInt( jobMeta.environmentSubstitute( wProxyPort.getText() ), 990 );
          if ( proxyport != 0 ) {
            ftpsclient.setProxyPort( proxyport );
          }
          if ( !Utils.isEmpty( realProxy_user ) ) {
            ftpsclient.setProxyUser( realProxy_user );
          }
          if ( !Utils.isEmpty( realProxy_pass ) ) {
            ftpsclient.setProxyPassword( realProxy_pass );
          }
        }

        // login to FTPS host ...
        ftpsclient.connect();
        pwdFolder = ftpsclient.getWorkingDirectory();
      }
      retval = true;
    } catch ( Exception e ) {
      if ( ftpsclient != null ) {
        try {
          ftpsclient.disconnect();
        } catch ( Exception ignored ) {
          // We've tried quitting the FTPS Client exception
          // nothing else to be done if the FTPS Client was already disconnected
        }
        ftpsclient = null;
      }
      MessageBox mb = new MessageBox( shell, SWT.OK | SWT.ICON_ERROR );
      mb.setMessage( BaseMessages.getString( PKG, "JobFTPDelete.ErrorConnect.NOK", e.getMessage() ) + Const.CR );
      mb.setText( BaseMessages.getString( PKG, "JobFTPDelete.ErrorConnect.Title.Bad" ) );
      mb.open();
    }
    return retval;
  }

  private boolean connectToSFTP() {
    boolean retval = false;
    try {
      if ( sftpclient == null ) {
        // Create sftp client to host ...
        sftpclient =
          new SFTPClient( InetAddress.getByName( jobMeta.environmentSubstitute( wServerName.getText() ) ), Const
            .toInt( jobMeta.environmentSubstitute( wPort.getText() ), 22 ), jobMeta
            .environmentSubstitute( wUserName.getText() ) );

        // login to ftp host ...
        sftpclient.login( Utils.resolvePassword( jobMeta, wPassword.getText() ) );
        pwdFolder = sftpclient.pwd();
      }

      retval = true;
    } catch ( Exception e ) {
      if ( sftpclient != null ) {
        try {
          sftpclient.disconnect();
        } catch ( Exception ignored ) {
          // We've tried quitting the SFTP Client exception
          // nothing else to be done if the SFTP Client was already disconnected
        }
        sftpclient = null;
      }
      MessageBox mb = new MessageBox( shell, SWT.OK | SWT.ICON_ERROR );
      mb.setMessage( BaseMessages.getString( PKG, "JobFTPDelete.ErrorConnect.NOK", e.getMessage() ) + Const.CR );
      mb.setText( BaseMessages.getString( PKG, "JobFTPDelete.ErrorConnect.Title.Bad" ) );
      mb.open();
    }
    return retval;
  }

  private boolean connectToSSH() {
    boolean retval = false;
    try {
      if ( conn == null ) { // Create a connection instance
        conn =
          new Connection( jobMeta.environmentSubstitute( wServerName.getText() ), Const.toInt( jobMeta
            .environmentSubstitute( wPort.getText() ), 22 ) );

        /* We want to connect through a HTTP proxy */
        if ( wuseProxy.getSelection() ) {
          /* Now connect */
          // if the proxy requires basic authentication:
          if ( !Utils.isEmpty( wProxyUsername.getText() ) ) {
            conn.setProxyData( new HTTPProxyData(
              jobMeta.environmentSubstitute( wProxyHost.getText() ), Const.toInt( wProxyPort.getText(), 22 ),
              jobMeta.environmentSubstitute( wProxyUsername.getText() ),
                    Utils.resolvePassword( jobMeta, wProxyPassword.getText() ) ) );
          } else {
            conn.setProxyData( new HTTPProxyData( jobMeta.environmentSubstitute( wProxyHost.getText() ), Const
              .toInt( wProxyPort.getText(), 22 ) ) );
          }
        }

        conn.connect();

        // Authenticate
        if ( wusePublicKey.getSelection() ) {
          retval =
            conn.authenticateWithPublicKey(
              jobMeta.environmentSubstitute( wUserName.getText() ), new java.io.File( jobMeta
                .environmentSubstitute( wKeyFilename.getText() ) ), jobMeta
                .environmentSubstitute( wkeyfilePass.getText() ) );
        } else {
          retval =
            conn.authenticateWithPassword( jobMeta.environmentSubstitute( wUserName.getText() ),
                    Utils.resolvePassword( jobMeta, wPassword.getText() ) );
        }
      }

      retval = true;
    } catch ( Exception e ) {
      if ( conn != null ) {
        try {
          conn.close();
        } catch ( Exception ignored ) {
          // We've tried quitting the SSH Client exception
          // nothing else to be done if the SSH Client was already disconnected
        }
        conn = null;
      }
      MessageBox mb = new MessageBox( shell, SWT.OK | SWT.ICON_ERROR );
      mb.setMessage( BaseMessages.getString( PKG, "JobFTPDelete.ErrorConnect.NOK", e.getMessage() ) + Const.CR );
      mb.setText( BaseMessages.getString( PKG, "JobFTPDelete.ErrorConnect.Title.Bad" ) );
      mb.open();
    }
    return retval;
  }

  private void activeSuccessCondition() {
    wlNrErrorsLessThan.setEnabled( wSuccessCondition.getSelectionIndex() != 0 );
    wNrErrorsLessThan.setEnabled( wSuccessCondition.getSelectionIndex() != 0 );
  }

  public void dispose() {
    closeFTPConnections();
    WindowProperty winprop = new WindowProperty( shell );
    props.setScreen( winprop );
    shell.dispose();
  }

  /**
   * Copy information from the meta-data input to the dialog fields.
   */
  public void getData() {
    wName.setText( Const.nullToEmpty( jobEntry.getName() ) );

    wProtocol.setText( Const.NVL( jobEntry.getProtocol(), "FTP" ) );
    wPort.setText( Const.NVL( jobEntry.getPort(), "" ) );
    wServerName.setText( Const.NVL( jobEntry.getServerName(), "" ) );
    wUserName.setText( Const.NVL( jobEntry.getUserName(), "" ) );
    wPassword.setText( Const.NVL( jobEntry.getPassword(), "" ) );
    wFtpDirectory.setText( Const.NVL( jobEntry.getFtpDirectory(), "" ) );
    wWildcard.setText( Const.NVL( jobEntry.getWildcard(), "" ) );
    wTimeout.setText( "" + jobEntry.getTimeout() );
    wActive.setSelection( jobEntry.isActiveConnection() );

    wuseProxy.setSelection( jobEntry.isUseProxy() );
    wProxyHost.setText( Const.NVL( jobEntry.getProxyHost(), "" ) );
    wProxyPort.setText( Const.NVL( jobEntry.getProxyPort(), "" ) );
    wProxyUsername.setText( Const.NVL( jobEntry.getProxyUsername(), "" ) );
    wProxyPassword.setText( Const.NVL( jobEntry.getProxyPassword(), "" ) );
    wSocksProxyHost.setText( Const.NVL( jobEntry.getSocksProxyHost(), "" ) );
    wSocksProxyPort.setText( Const.NVL( jobEntry.getSocksProxyPort(), "" ) );
    wSocksProxyUsername.setText( Const.NVL( jobEntry.getSocksProxyUsername(), "" ) );
    wSocksProxyPassword.setText( Const.NVL( jobEntry.getSocksProxyPassword(), "" ) );

    wNrErrorsLessThan.setText( Const.NVL( jobEntry.getLimitSuccess(), "10" ) );

    if ( jobEntry.getSuccessCondition() != null ) {
      if ( jobEntry.getSuccessCondition().equals( jobEntry.SUCCESS_IF_AT_LEAST_X_FILES_DOWNLOADED ) ) {
        wSuccessCondition.select( 1 );
      } else if ( jobEntry.getSuccessCondition().equals( jobEntry.SUCCESS_IF_ERRORS_LESS ) ) {
        wSuccessCondition.select( 2 );
      } else {
        wSuccessCondition.select( 0 );
      }
    } else {
      wSuccessCondition.select( 0 );
    }

    wusePublicKey.setSelection( jobEntry.isUsePublicKey() );
    wKeyFilename.setText( Const.nullToEmpty( jobEntry.getKeyFilename() ) );
    wkeyfilePass.setText( Const.nullToEmpty( jobEntry.getKeyFilePass() ) );

    wgetPrevious.setSelection( jobEntry.isCopyPrevious() );
    wConnectionType.setText( FTPSConnection.getConnectionTypeDesc( jobEntry.getFTPSConnectionType() ) );

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
      mb.setText( BaseMessages.getString( PKG, "System.StepJobEntryNameMissing.Title" ) );
      mb.setMessage( BaseMessages.getString( PKG, "System.JobEntryNameMissing.Msg" ) );
      mb.open();
      return;
    }
    jobEntry.setName( wName.getText() );
    jobEntry.setProtocol( wProtocol.getText() );
    jobEntry.setPort( wPort.getText() );
    jobEntry.setServerName( wServerName.getText() );
    jobEntry.setUserName( wUserName.getText() );
    jobEntry.setPassword( wPassword.getText() );
    jobEntry.setFtpDirectory( wFtpDirectory.getText() );
    jobEntry.setWildcard( wWildcard.getText() );
    jobEntry.setTimeout( Const.toInt( wTimeout.getText(), 10000 ) );
    jobEntry.setActiveConnection( wActive.getSelection() );

    jobEntry.setUseProxy( wuseProxy.getSelection() );
    jobEntry.setProxyHost( wProxyHost.getText() );
    jobEntry.setProxyPort( wProxyPort.getText() );
    jobEntry.setProxyUsername( wProxyUsername.getText() );
    jobEntry.setProxyPassword( wProxyPassword.getText() );
    jobEntry.setSocksProxyHost( wSocksProxyHost.getText() );
    jobEntry.setSocksProxyPort( wSocksProxyPort.getText() );
    jobEntry.setSocksProxyUsername( wSocksProxyUsername.getText() );
    jobEntry.setSocksProxyPassword( wSocksProxyPassword.getText() );

    jobEntry.setLimitSuccess( wNrErrorsLessThan.getText() );

    if ( wSuccessCondition.getSelectionIndex() == 1 ) {
      jobEntry.setSuccessCondition( jobEntry.SUCCESS_IF_AT_LEAST_X_FILES_DOWNLOADED );
    } else if ( wSuccessCondition.getSelectionIndex() == 2 ) {
      jobEntry.setSuccessCondition( jobEntry.SUCCESS_IF_ERRORS_LESS );
    } else {
      jobEntry.setSuccessCondition( jobEntry.SUCCESS_IF_ALL_FILES_DOWNLOADED );
    }

    jobEntry.setUsePublicKey( wusePublicKey.getSelection() );
    jobEntry.setKeyFilename( wKeyFilename.getText() );
    jobEntry.setKeyFilePass( wkeyfilePass.getText() );

    jobEntry.setCopyPrevious( wgetPrevious.getSelection() );
    jobEntry.setFTPSConnectionType( FTPSConnection.getConnectionTypeByDesc( wConnectionType.getText() ) );
    dispose();
  }

  private void closeFTPConnections() {
    // Close FTP connection if necessary
    if ( ftpclient != null && ftpclient.connected() ) {
      try {
        ftpclient.quit();
        ftpclient = null;
      } catch ( Exception e ) {
        // Ignore close errors
      }
    }

    // Close SecureFTP connection if necessary
    if ( sftpclient != null ) {
      try {
        sftpclient.disconnect();
        sftpclient = null;
      } catch ( Exception e ) {
        // Ignore close errors
      }
    }
    // Close SSH connection if necessary
    if ( conn != null ) {
      try {
        conn.close();
        conn = null;

      } catch ( Exception e ) {
        // Ignore close errors
      }
    }
  }

  public boolean evaluates() {
    return true;
  }

  public boolean isUnconditional() {
    return false;
  }
}
