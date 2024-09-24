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

package org.pentaho.di.ui.job.entries.ftpsget;

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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.annotations.PluginDialog;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entries.ftpsget.JobEntryFTPSGet;
import org.pentaho.di.job.entries.ftpsget.FTPSConnection;
import org.pentaho.di.job.entry.JobEntryDialogInterface;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.Repository;
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

/**
 * This dialog allows you to edit the FTPS job entry settings.
 *
 * @author Samatar
 * @since 19-03-2010
 */

@PluginDialog( id = "FTPS_GET", image = "GFTPS.svg", pluginType = PluginDialog.PluginType.JOBENTRY,
        documentationUrl = "http://wiki.pentaho.com/display/EAI/Get+a+file+with+FTPS" )
public class JobEntryFTPSGetDialog extends JobEntryDialog implements JobEntryDialogInterface {
  private static Class<?> PKG = JobEntryFTPSGet.class; // for i18n purposes, needed by Translator2!!

  private LabelText wName;

  private FormData fdName;

  private LabelTextVar wServerName;

  private FormData fdServerName;

  private LabelTextVar wUserName;

  private FormData fdUserName;

  private LabelTextVar wPassword;

  private FormData fdPassword;

  private TextVar wFTPSDirectory;
  private Label wlFTPSDirectory;

  private FormData fdFTPSDirectory;
  private FormData fdlFTPSDirectory;

  private LabelTextVar wWildcard;

  private FormData fdWildcard;

  private Label wlBinaryMode;

  private Button wBinaryMode;

  private FormData fdlBinaryMode, fdBinaryMode;

  private LabelTextVar wTimeout;

  private FormData fdTimeout;

  private Label wlRemove;

  private Button wRemove;

  private FormData fdlRemove, fdRemove;

  private Label wlOnlyNew;

  private Button wOnlyNew;

  private FormData fdlOnlyNew, fdOnlyNew;

  private Label wlActive;

  private Button wActive;

  private FormData fdlActive, fdActive;

  private Button wOK, wCancel;

  private Listener lsOK, lsCancel;

  private JobEntryFTPSGet jobEntry;

  private Shell shell;

  private SelectionAdapter lsDef;

  private boolean changed;

  private Label wlMove;

  private Button wMove;

  private Label wlMoveToDirectory;

  private FormData fdMove, fdlMove;

  private FormData fdMoveToDirectory, fdlMoveToDirectory;

  private TextVar wMoveToDirectory;

  private Label wlSpecifyFormat;
  private Button wSpecifyFormat;
  private FormData fdlSpecifyFormat, fdSpecifyFormat;

  private Label wlDateTimeFormat;
  private CCombo wDateTimeFormat;
  private FormData fdlDateTimeFormat, fdDateTimeFormat;

  private Label wlAddDate;
  private Button wAddDate;
  private FormData fdlAddDate, fdAddDate;

  private Label wlAddTime;
  private Button wAddTime;
  private FormData fdlAddTime, fdAddTime;

  private Label wlAddDateBeforeExtension;
  private Button wAddDateBeforeExtension;
  private FormData fdlAddDateBeforeExtension, fdAddDateBeforeExtension;

  private Group wServerSettings;
  private FormData fdServerSettings;

  private Group wLocalSettings;
  private FormData fdLocalSettings;

  private CTabFolder wTabFolder;
  private Composite wGeneralComp, wFilesComp, wAdvancedComp;
  private CTabItem wGeneralTab, wFilesTab, wAdvancedTab;
  private FormData fdGeneralComp, fdFilesComp, fdAdvancedComp;
  private FormData fdTabFolder;

  private LabelTextVar wProxyHost;

  private FormData fdPort;

  private LabelTextVar wPort;

  private FormData fdProxyHost;

  private LabelTextVar wProxyPort;

  private FormData fdProxyPort;

  private LabelTextVar wProxyUsername;

  private FormData fdProxyUsername;

  private LabelTextVar wProxyPassword;

  private FormData fdProxyPasswd;

  private Label wlConnectionType;
  private FormData fdlConnectionType;
  private CCombo wConnectionType;
  private FormData fdConnectionType;

  private Button wTest;

  private FormData fdTest;

  private Listener lsTest;

  private Listener lsCheckFolder;

  private Listener lsCheckChangeFolder;

  private Group wAdvancedSettings;
  private FormData fdAdvancedSettings;

  private Group wRemoteSettings;
  private FormData fdRemoteSettings;

  private Label wlAddFilenameToResult;

  private Button wAddFilenameToResult;

  private FormData fdlAddFilenameToResult, fdAddFilenameToResult;

  private Label wlTargetDirectory;
  private TextVar wTargetDirectory;
  private FormData fdlTargetDirectory, fdTargetDirectory;

  private Button wbTargetDirectory;
  private FormData fdbTargetDirectory;

  private Label wlIfFileExists;
  private CCombo wIfFileExists;
  private FormData fdlIfFileExists, fdIfFileExists;

  private Button wbTestFolderExists;
  private FormData fdbTestFolderExists;

  private Button wCreateMoveFolder;
  private Label wlCreateMoveFolder;
  private FormData fdCreateMoveFolder;
  private FormData fdlCreateMoveFolder;

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

  private FTPSConnection connection = null;

  public JobEntryFTPSGetDialog( Shell parent, JobEntryInterface jobEntryInt, Repository rep, JobMeta jobMeta ) {
    super( parent, jobEntryInt, rep, jobMeta );
    jobEntry = (JobEntryFTPSGet) jobEntryInt;
    if ( this.jobEntry.getName() == null ) {
      this.jobEntry.setName( BaseMessages.getString( PKG, "JobFTPS.Name.Default" ) );
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
        // pwdFolder=null;
        connection = null;
        jobEntry.setChanged();
      }
    };
    changed = jobEntry.hasChanged();

    FormLayout formLayout = new FormLayout();
    formLayout.marginWidth = Const.FORM_MARGIN;
    formLayout.marginHeight = Const.FORM_MARGIN;

    shell.setLayout( formLayout );
    shell.setText( BaseMessages.getString( PKG, "JobFTPS.Title" ) );

    int middle = props.getMiddlePct();
    int margin = Const.MARGIN;

    // Job entry name line
    wName =
      new LabelText( shell, BaseMessages.getString( PKG, "JobFTPS.Name.Label" ), BaseMessages.getString(
        PKG, "JobFTPS.Name.Tooltip" ) );
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
    wGeneralTab.setText( BaseMessages.getString( PKG, "JobFTPS.Tab.General.Label" ) );

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
    wServerSettings.setText( BaseMessages.getString( PKG, "JobFTPS.ServerSettings.Group.Label" ) );

    FormLayout ServerSettingsgroupLayout = new FormLayout();
    ServerSettingsgroupLayout.marginWidth = 10;
    ServerSettingsgroupLayout.marginHeight = 10;

    wServerSettings.setLayout( ServerSettingsgroupLayout );

    // ServerName line
    wServerName =
      new LabelTextVar(
        jobMeta, wServerSettings, BaseMessages.getString( PKG, "JobFTPS.Server.Label" ), BaseMessages
          .getString( PKG, "JobFTPS.Server.Tooltip" ) );
    props.setLook( wServerName );
    wServerName.addModifyListener( lsMod );
    fdServerName = new FormData();
    fdServerName.left = new FormAttachment( 0, 0 );
    fdServerName.top = new FormAttachment( wName, margin );
    fdServerName.right = new FormAttachment( 100, 0 );
    wServerName.setLayoutData( fdServerName );

    // Server port line
    wPort =
      new LabelTextVar(
        jobMeta, wServerSettings, BaseMessages.getString( PKG, "JobFTPS.Port.Label" ), BaseMessages.getString(
          PKG, "JobFTPS.Port.Tooltip" ) );
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
        jobMeta, wServerSettings, BaseMessages.getString( PKG, "JobFTPS.User.Label" ), BaseMessages.getString(
          PKG, "JobFTPS.User.Tooltip" ) );
    props.setLook( wUserName );
    wUserName.addModifyListener( lsMod );
    fdUserName = new FormData();
    fdUserName.left = new FormAttachment( 0, 0 );
    fdUserName.top = new FormAttachment( wPort, margin );
    fdUserName.right = new FormAttachment( 100, 0 );
    wUserName.setLayoutData( fdUserName );

    // Password line
    wPassword =
      new LabelTextVar( jobMeta, wServerSettings,
        BaseMessages.getString( PKG, "JobFTPS.Password.Label" ),
        BaseMessages.getString( PKG, "JobFTPS.Password.Tooltip" ), true );
    props.setLook( wPassword );
    wPassword.addModifyListener( lsMod );
    fdPassword = new FormData();
    fdPassword.left = new FormAttachment( 0, 0 );
    fdPassword.top = new FormAttachment( wUserName, margin );
    fdPassword.right = new FormAttachment( 100, 0 );
    wPassword.setLayoutData( fdPassword );

    // Proxy host line
    wProxyHost =
      new LabelTextVar(
        jobMeta, wServerSettings, BaseMessages.getString( PKG, "JobFTPS.ProxyHost.Label" ), BaseMessages
          .getString( PKG, "JobFTPS.ProxyHost.Tooltip" ) );
    props.setLook( wProxyHost );
    wProxyHost.addModifyListener( lsMod );
    fdProxyHost = new FormData();
    fdProxyHost.left = new FormAttachment( 0, 0 );
    fdProxyHost.top = new FormAttachment( wPassword, 2 * margin );
    fdProxyHost.right = new FormAttachment( 100, 0 );
    wProxyHost.setLayoutData( fdProxyHost );

    // Proxy port line
    wProxyPort =
      new LabelTextVar(
        jobMeta, wServerSettings, BaseMessages.getString( PKG, "JobFTPS.ProxyPort.Label" ), BaseMessages
          .getString( PKG, "JobFTPS.ProxyPort.Tooltip" ) );
    props.setLook( wProxyPort );
    wProxyPort.addModifyListener( lsMod );
    fdProxyPort = new FormData();
    fdProxyPort.left = new FormAttachment( 0, 0 );
    fdProxyPort.top = new FormAttachment( wProxyHost, margin );
    fdProxyPort.right = new FormAttachment( 100, 0 );
    wProxyPort.setLayoutData( fdProxyPort );

    // Proxy username line
    wProxyUsername =
      new LabelTextVar(
        jobMeta, wServerSettings, BaseMessages.getString( PKG, "JobFTPS.ProxyUsername.Label" ), BaseMessages
          .getString( PKG, "JobFTPS.ProxyUsername.Tooltip" ) );
    props.setLook( wProxyUsername );
    wProxyUsername.addModifyListener( lsMod );
    fdProxyUsername = new FormData();
    fdProxyUsername.left = new FormAttachment( 0, 0 );
    fdProxyUsername.top = new FormAttachment( wProxyPort, margin );
    fdProxyUsername.right = new FormAttachment( 100, 0 );
    wProxyUsername.setLayoutData( fdProxyUsername );

    // Proxy password line
    wProxyPassword =
      new LabelTextVar(
        jobMeta, wServerSettings, BaseMessages.getString( PKG, "JobFTPS.ProxyPassword.Label" ), BaseMessages
          .getString( PKG, "JobFTPS.ProxyPassword.Tooltip" ), true );
    props.setLook( wProxyPassword );
    wProxyPassword.addModifyListener( lsMod );
    fdProxyPasswd = new FormData();
    fdProxyPasswd.left = new FormAttachment( 0, 0 );
    fdProxyPasswd.top = new FormAttachment( wProxyUsername, margin );
    fdProxyPasswd.right = new FormAttachment( 100, 0 );
    wProxyPassword.setLayoutData( fdProxyPasswd );

    wlConnectionType = new Label( wServerSettings, SWT.RIGHT );
    wlConnectionType.setText( BaseMessages.getString( PKG, "JobFTPS.ConnectionType.Label" ) );
    props.setLook( wlConnectionType );
    fdlConnectionType = new FormData();
    fdlConnectionType.left = new FormAttachment( 0, 0 );
    fdlConnectionType.right = new FormAttachment( middle, 0 );
    fdlConnectionType.top = new FormAttachment( wProxyPassword, 2 * margin );
    wlConnectionType.setLayoutData( fdlConnectionType );
    wConnectionType = new CCombo( wServerSettings, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER );
    wConnectionType.setItems( FTPSConnection.connection_type_Desc );
    props.setLook( wConnectionType );
    fdConnectionType = new FormData();
    fdConnectionType.left = new FormAttachment( middle, margin );
    fdConnectionType.top = new FormAttachment( wProxyPassword, 2 * margin );
    fdConnectionType.right = new FormAttachment( 100, 0 );
    wConnectionType.setLayoutData( fdConnectionType );

    wConnectionType.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e ) {
      }
    } );

    // Test connection button
    wTest = new Button( wServerSettings, SWT.PUSH );
    wTest.setText( BaseMessages.getString( PKG, "JobFTPS.TestConnection.Label" ) );
    props.setLook( wTest );
    fdTest = new FormData();
    wTest.setToolTipText( BaseMessages.getString( PKG, "JobFTPS.TestConnection.Tooltip" ) );
    // fdTest.left = new FormAttachment(middle, 0);
    fdTest.top = new FormAttachment( wConnectionType, margin );
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

    // ////////////////////////
    // START OF Advanced SETTINGS GROUP///
    // /
    wAdvancedSettings = new Group( wGeneralComp, SWT.SHADOW_NONE );
    props.setLook( wAdvancedSettings );
    wAdvancedSettings.setText( BaseMessages.getString( PKG, "JobFTPS.AdvancedSettings.Group.Label" ) );
    FormLayout AdvancedSettingsgroupLayout = new FormLayout();
    AdvancedSettingsgroupLayout.marginWidth = 10;
    AdvancedSettingsgroupLayout.marginHeight = 10;
    wAdvancedSettings.setLayout( AdvancedSettingsgroupLayout );

    // Binary mode selection...
    wlBinaryMode = new Label( wAdvancedSettings, SWT.RIGHT );
    wlBinaryMode.setText( BaseMessages.getString( PKG, "JobFTPS.BinaryMode.Label" ) );
    props.setLook( wlBinaryMode );
    fdlBinaryMode = new FormData();
    fdlBinaryMode.left = new FormAttachment( 0, 0 );
    fdlBinaryMode.top = new FormAttachment( wServerSettings, margin );
    fdlBinaryMode.right = new FormAttachment( middle, 0 );
    wlBinaryMode.setLayoutData( fdlBinaryMode );
    wBinaryMode = new Button( wAdvancedSettings, SWT.CHECK );
    props.setLook( wBinaryMode );
    wBinaryMode.setToolTipText( BaseMessages.getString( PKG, "JobFTPS.BinaryMode.Tooltip" ) );
    fdBinaryMode = new FormData();
    fdBinaryMode.left = new FormAttachment( middle, margin );
    fdBinaryMode.top = new FormAttachment( wServerSettings, margin );
    fdBinaryMode.right = new FormAttachment( 100, 0 );
    wBinaryMode.setLayoutData( fdBinaryMode );

    // Timeout line
    wTimeout =
      new LabelTextVar(
        jobMeta, wAdvancedSettings, BaseMessages.getString( PKG, "JobFTPS.Timeout.Label" ), BaseMessages
          .getString( PKG, "JobFTPS.Timeout.Tooltip" ) );
    props.setLook( wTimeout );
    wTimeout.addModifyListener( lsMod );
    fdTimeout = new FormData();
    fdTimeout.left = new FormAttachment( 0, 0 );
    fdTimeout.top = new FormAttachment( wlBinaryMode, margin );
    fdTimeout.right = new FormAttachment( 100, 0 );
    wTimeout.setLayoutData( fdTimeout );

    // active connection?
    wlActive = new Label( wAdvancedSettings, SWT.RIGHT );
    wlActive.setText( BaseMessages.getString( PKG, "JobFTPS.ActiveConns.Label" ) );
    props.setLook( wlActive );
    fdlActive = new FormData();
    fdlActive.left = new FormAttachment( 0, 0 );
    fdlActive.top = new FormAttachment( wTimeout, margin );
    fdlActive.right = new FormAttachment( middle, 0 );
    wlActive.setLayoutData( fdlActive );
    wActive = new Button( wAdvancedSettings, SWT.CHECK );
    wActive.setToolTipText( BaseMessages.getString( PKG, "JobFTPS.ActiveConns.Tooltip" ) );
    props.setLook( wActive );
    fdActive = new FormData();
    fdActive.left = new FormAttachment( middle, margin );
    fdActive.top = new FormAttachment( wTimeout, margin );
    fdActive.right = new FormAttachment( 100, 0 );
    wActive.setLayoutData( fdActive );

    fdAdvancedSettings = new FormData();
    fdAdvancedSettings.left = new FormAttachment( 0, margin );
    fdAdvancedSettings.top = new FormAttachment( wServerSettings, margin );
    fdAdvancedSettings.right = new FormAttachment( 100, -margin );
    wAdvancedSettings.setLayoutData( fdAdvancedSettings );
    // ///////////////////////////////////////////////////////////
    // / END OF Advanced SETTINGS GROUP
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
    // START OF Files TAB ///
    // ////////////////////////

    wFilesTab = new CTabItem( wTabFolder, SWT.NONE );
    wFilesTab.setText( BaseMessages.getString( PKG, "JobFTPS.Tab.Files.Label" ) );

    wFilesComp = new Composite( wTabFolder, SWT.NONE );
    props.setLook( wFilesComp );

    FormLayout FilesLayout = new FormLayout();
    FilesLayout.marginWidth = 3;
    FilesLayout.marginHeight = 3;
    wFilesComp.setLayout( FilesLayout );

    // ////////////////////////
    // START OF Remote SETTINGS GROUP///
    // /
    wRemoteSettings = new Group( wFilesComp, SWT.SHADOW_NONE );
    props.setLook( wRemoteSettings );
    wRemoteSettings.setText( BaseMessages.getString( PKG, "JobFTPS.RemoteSettings.Group.Label" ) );

    FormLayout RemoteSettinsgroupLayout = new FormLayout();
    RemoteSettinsgroupLayout.marginWidth = 10;
    RemoteSettinsgroupLayout.marginHeight = 10;

    wRemoteSettings.setLayout( RemoteSettinsgroupLayout );

    // Move to directory
    wlFTPSDirectory = new Label( wRemoteSettings, SWT.RIGHT );
    wlFTPSDirectory.setText( BaseMessages.getString( PKG, "JobFTPS.RemoteDir.Label" ) );
    props.setLook( wlFTPSDirectory );
    fdlFTPSDirectory = new FormData();
    fdlFTPSDirectory.left = new FormAttachment( 0, 0 );
    fdlFTPSDirectory.top = new FormAttachment( 0, margin );
    fdlFTPSDirectory.right = new FormAttachment( middle, 0 );
    wlFTPSDirectory.setLayoutData( fdlFTPSDirectory );

    // Test remote folder button ...
    wbTestChangeFolderExists = new Button( wRemoteSettings, SWT.PUSH | SWT.CENTER );
    props.setLook( wbTestChangeFolderExists );
    wbTestChangeFolderExists.setText( BaseMessages.getString( PKG, "JobFTPS.TestFolderExists.Label" ) );
    fdbTestChangeFolderExists = new FormData();
    fdbTestChangeFolderExists.right = new FormAttachment( 100, 0 );
    fdbTestChangeFolderExists.top = new FormAttachment( 0, margin );
    wbTestChangeFolderExists.setLayoutData( fdbTestChangeFolderExists );

    wFTPSDirectory = new TextVar( jobMeta, wRemoteSettings, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wFTPSDirectory );
    wFTPSDirectory.setToolTipText( BaseMessages.getString( PKG, "JobFTPS.RemoteDir.Tooltip" ) );
    wFTPSDirectory.addModifyListener( lsMod );
    fdFTPSDirectory = new FormData();
    fdFTPSDirectory.left = new FormAttachment( middle, margin );
    fdFTPSDirectory.top = new FormAttachment( 0, margin );
    fdFTPSDirectory.right = new FormAttachment( wbTestChangeFolderExists, -margin );
    wFTPSDirectory.setLayoutData( fdFTPSDirectory );

    // Wildcard line
    wWildcard =
      new LabelTextVar(
        jobMeta, wRemoteSettings, BaseMessages.getString( PKG, "JobFTPS.Wildcard.Label" ), BaseMessages
          .getString( PKG, "JobFTPS.Wildcard.Tooltip" ) );
    props.setLook( wWildcard );
    wWildcard.addModifyListener( lsMod );
    fdWildcard = new FormData();
    fdWildcard.left = new FormAttachment( 0, 0 );
    fdWildcard.top = new FormAttachment( wFTPSDirectory, 2 * margin );
    fdWildcard.right = new FormAttachment( 100, 0 );
    wWildcard.setLayoutData( fdWildcard );

    // Remove files after retrieval...
    wlRemove = new Label( wRemoteSettings, SWT.RIGHT );
    wlRemove.setText( BaseMessages.getString( PKG, "JobFTPS.RemoveFiles.Label" ) );
    props.setLook( wlRemove );
    fdlRemove = new FormData();
    fdlRemove.left = new FormAttachment( 0, 0 );
    fdlRemove.top = new FormAttachment( wWildcard, margin );
    fdlRemove.right = new FormAttachment( middle, 0 );
    wlRemove.setLayoutData( fdlRemove );
    wRemove = new Button( wRemoteSettings, SWT.CHECK );
    wRemove.setToolTipText( BaseMessages.getString( PKG, "JobFTPS.RemoveFiles.Tooltip" ) );
    props.setLook( wRemove );
    fdRemove = new FormData();
    fdRemove.left = new FormAttachment( middle, margin );
    fdRemove.top = new FormAttachment( wWildcard, margin );
    fdRemove.right = new FormAttachment( 100, 0 );
    wRemove.setLayoutData( fdRemove );

    wRemove.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e ) {
        if ( wRemove.getSelection() ) {
          wMove.setSelection( false );
          activateMoveTo();
        }

      }
    } );

    // Move files after the transfert?...
    wlMove = new Label( wRemoteSettings, SWT.RIGHT );
    wlMove.setText( BaseMessages.getString( PKG, "JobFTPS.MoveFiles.Label" ) );
    props.setLook( wlMove );
    fdlMove = new FormData();
    fdlMove.left = new FormAttachment( 0, 0 );
    fdlMove.top = new FormAttachment( wRemove, margin );
    fdlMove.right = new FormAttachment( middle, -margin );
    wlMove.setLayoutData( fdlMove );
    wMove = new Button( wRemoteSettings, SWT.CHECK );
    props.setLook( wMove );
    wMove.setToolTipText( BaseMessages.getString( PKG, "JobFTPS.MoveFiles.Tooltip" ) );
    fdMove = new FormData();
    fdMove.left = new FormAttachment( middle, margin );
    fdMove.top = new FormAttachment( wRemove, margin );
    fdMove.right = new FormAttachment( 100, 0 );
    wMove.setLayoutData( fdMove );
    wMove.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e ) {
        activateMoveTo();
        if ( wMove.getSelection() ) {
          wRemove.setSelection( false );
        }

      }
    } );

    // Move to directory
    wlMoveToDirectory = new Label( wRemoteSettings, SWT.RIGHT );
    wlMoveToDirectory.setText( BaseMessages.getString( PKG, "JobFTPS.MoveFolder.Label" ) );
    props.setLook( wlMoveToDirectory );
    fdlMoveToDirectory = new FormData();
    fdlMoveToDirectory.left = new FormAttachment( 0, 0 );
    fdlMoveToDirectory.top = new FormAttachment( wMove, margin );
    fdlMoveToDirectory.right = new FormAttachment( middle, 0 );
    wlMoveToDirectory.setLayoutData( fdlMoveToDirectory );

    // Test remote folder button ...
    wbTestFolderExists = new Button( wRemoteSettings, SWT.PUSH | SWT.CENTER );
    props.setLook( wbTestFolderExists );
    wbTestFolderExists.setText( BaseMessages.getString( PKG, "JobFTPS.TestFolderExists.Label" ) );
    fdbTestFolderExists = new FormData();
    fdbTestFolderExists.right = new FormAttachment( 100, 0 );
    fdbTestFolderExists.top = new FormAttachment( wMove, margin );
    wbTestFolderExists.setLayoutData( fdbTestFolderExists );

    wMoveToDirectory = new TextVar( jobMeta, wRemoteSettings, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    wMoveToDirectory.setToolTipText( BaseMessages.getString( PKG, "JobFTPS.MoveToDirectory.Tooltip" ) );
    props.setLook( wMoveToDirectory );
    wMoveToDirectory.addModifyListener( lsMod );
    fdMoveToDirectory = new FormData();
    fdMoveToDirectory.left = new FormAttachment( middle, margin );
    fdMoveToDirectory.top = new FormAttachment( wMove, margin );
    fdMoveToDirectory.right = new FormAttachment( wbTestFolderExists, -margin );
    wMoveToDirectory.setLayoutData( fdMoveToDirectory );

    // create destination folder?...
    wlCreateMoveFolder = new Label( wRemoteSettings, SWT.RIGHT );
    wlCreateMoveFolder.setText( BaseMessages.getString( PKG, "JobFTPS.CreateMoveFolder.Label" ) );
    props.setLook( wlCreateMoveFolder );
    fdlCreateMoveFolder = new FormData();
    fdlCreateMoveFolder.left = new FormAttachment( 0, 0 );
    fdlCreateMoveFolder.top = new FormAttachment( wMoveToDirectory, margin );
    fdlCreateMoveFolder.right = new FormAttachment( middle, 0 );
    wlCreateMoveFolder.setLayoutData( fdlCreateMoveFolder );
    wCreateMoveFolder = new Button( wRemoteSettings, SWT.CHECK );
    wCreateMoveFolder.setToolTipText( BaseMessages.getString( PKG, "JobFTPS.CreateMoveFolder.Tooltip" ) );
    props.setLook( wCreateMoveFolder );
    fdCreateMoveFolder = new FormData();
    fdCreateMoveFolder.left = new FormAttachment( middle, margin );
    fdCreateMoveFolder.top = new FormAttachment( wMoveToDirectory, margin );
    fdCreateMoveFolder.right = new FormAttachment( 100, 0 );
    wCreateMoveFolder.setLayoutData( fdCreateMoveFolder );

    fdRemoteSettings = new FormData();
    fdRemoteSettings.left = new FormAttachment( 0, margin );
    fdRemoteSettings.top = new FormAttachment( 0, 2 * margin );
    fdRemoteSettings.right = new FormAttachment( 100, -margin );
    wRemoteSettings.setLayoutData( fdRemoteSettings );
    // ///////////////////////////////////////////////////////////
    // / END OF Remote SETTINGSGROUP
    // ///////////////////////////////////////////////////////////

    // ////////////////////////
    // START OF LOCAL SETTINGS GROUP///
    // /
    wLocalSettings = new Group( wFilesComp, SWT.SHADOW_NONE );
    props.setLook( wLocalSettings );
    wLocalSettings.setText( BaseMessages.getString( PKG, "JobFTPS.LocalSettings.Group.Label" ) );

    FormLayout LocalSettinsgroupLayout = new FormLayout();
    LocalSettinsgroupLayout.marginWidth = 10;
    LocalSettinsgroupLayout.marginHeight = 10;

    wLocalSettings.setLayout( LocalSettinsgroupLayout );

    // TargetDirectory
    wlTargetDirectory = new Label( wLocalSettings, SWT.RIGHT );
    wlTargetDirectory.setText( BaseMessages.getString( PKG, "JobFTPS.TargetDir.Label" ) );
    props.setLook( wlTargetDirectory );
    fdlTargetDirectory = new FormData();
    fdlTargetDirectory.left = new FormAttachment( 0, 0 );
    fdlTargetDirectory.top = new FormAttachment( wRemoteSettings, margin );
    fdlTargetDirectory.right = new FormAttachment( middle, -margin );
    wlTargetDirectory.setLayoutData( fdlTargetDirectory );

    // Browse folders button ...
    wbTargetDirectory = new Button( wLocalSettings, SWT.PUSH | SWT.CENTER );
    props.setLook( wbTargetDirectory );
    wbTargetDirectory.setText( BaseMessages.getString( PKG, "JobFTPS.BrowseFolders.Label" ) );
    fdbTargetDirectory = new FormData();
    fdbTargetDirectory.right = new FormAttachment( 100, 0 );
    fdbTargetDirectory.top = new FormAttachment( wRemoteSettings, margin );
    wbTargetDirectory.setLayoutData( fdbTargetDirectory );

    wTargetDirectory = new TextVar( jobMeta, wLocalSettings, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wTargetDirectory );
    wTargetDirectory.setToolTipText( BaseMessages.getString( PKG, "JobFTPS.TargetDir.Tooltip" ) );
    wTargetDirectory.addModifyListener( lsMod );
    fdTargetDirectory = new FormData();
    fdTargetDirectory.left = new FormAttachment( middle, margin );
    fdTargetDirectory.top = new FormAttachment( wRemoteSettings, margin );
    fdTargetDirectory.right = new FormAttachment( wbTargetDirectory, -margin );
    wTargetDirectory.setLayoutData( fdTargetDirectory );

    wbTargetDirectory.addSelectionListener( new SelectionAdapterFileDialogTextVar( jobMeta.getLogChannel(), wTargetDirectory, jobMeta,
            new SelectionAdapterOptions( SelectionOperation.FOLDER ) ) );
    // Create multi-part file?
    wlAddDate = new Label( wLocalSettings, SWT.RIGHT );
    wlAddDate.setText( BaseMessages.getString( PKG, "JobFTPS.AddDate.Label" ) );
    props.setLook( wlAddDate );
    fdlAddDate = new FormData();
    fdlAddDate.left = new FormAttachment( 0, 0 );
    fdlAddDate.top = new FormAttachment( wTargetDirectory, margin );
    fdlAddDate.right = new FormAttachment( middle, -margin );
    wlAddDate.setLayoutData( fdlAddDate );
    wAddDate = new Button( wLocalSettings, SWT.CHECK );
    props.setLook( wAddDate );
    wAddDate.setToolTipText( BaseMessages.getString( PKG, "JobFTPS.AddDate.Tooltip" ) );
    fdAddDate = new FormData();
    fdAddDate.left = new FormAttachment( middle, margin );
    fdAddDate.top = new FormAttachment( wTargetDirectory, margin );
    fdAddDate.right = new FormAttachment( 100, 0 );
    wAddDate.setLayoutData( fdAddDate );
    wAddDate.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e ) {
        jobEntry.setChanged();
      }
    } );
    // Create multi-part file?
    wlAddTime = new Label( wLocalSettings, SWT.RIGHT );
    wlAddTime.setText( BaseMessages.getString( PKG, "JobFTPS.AddTime.Label" ) );
    props.setLook( wlAddTime );
    fdlAddTime = new FormData();
    fdlAddTime.left = new FormAttachment( 0, 0 );
    fdlAddTime.top = new FormAttachment( wAddDate, margin );
    fdlAddTime.right = new FormAttachment( middle, -margin );
    wlAddTime.setLayoutData( fdlAddTime );
    wAddTime = new Button( wLocalSettings, SWT.CHECK );
    props.setLook( wAddTime );
    wAddTime.setToolTipText( BaseMessages.getString( PKG, "JobFTPS.AddTime.Tooltip" ) );
    fdAddTime = new FormData();
    fdAddTime.left = new FormAttachment( middle, margin );
    fdAddTime.top = new FormAttachment( wAddDate, margin );
    fdAddTime.right = new FormAttachment( 100, 0 );
    wAddTime.setLayoutData( fdAddTime );
    wAddTime.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e ) {
        jobEntry.setChanged();
      }
    } );

    // Specify date time format?
    wlSpecifyFormat = new Label( wLocalSettings, SWT.RIGHT );
    wlSpecifyFormat.setText( BaseMessages.getString( PKG, "JobFTPS.SpecifyFormat.Label" ) );
    props.setLook( wlSpecifyFormat );
    fdlSpecifyFormat = new FormData();
    fdlSpecifyFormat.left = new FormAttachment( 0, 0 );
    fdlSpecifyFormat.top = new FormAttachment( wAddTime, margin );
    fdlSpecifyFormat.right = new FormAttachment( middle, -margin );
    wlSpecifyFormat.setLayoutData( fdlSpecifyFormat );
    wSpecifyFormat = new Button( wLocalSettings, SWT.CHECK );
    props.setLook( wSpecifyFormat );
    wSpecifyFormat.setToolTipText( BaseMessages.getString( PKG, "JobFTPS.SpecifyFormat.Tooltip" ) );
    fdSpecifyFormat = new FormData();
    fdSpecifyFormat.left = new FormAttachment( middle, margin );
    fdSpecifyFormat.top = new FormAttachment( wAddTime, margin );
    fdSpecifyFormat.right = new FormAttachment( 100, 0 );
    wSpecifyFormat.setLayoutData( fdSpecifyFormat );
    wSpecifyFormat.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e ) {
        jobEntry.setChanged();
        setDateTimeFormat();
        setAddDateBeforeExtension();
      }
    } );

    // Prepare a list of possible DateTimeFormats...
    String[] dats = Const.getDateFormats();

    // DateTimeFormat
    wlDateTimeFormat = new Label( wLocalSettings, SWT.RIGHT );
    wlDateTimeFormat.setText( BaseMessages.getString( PKG, "JobFTPS.DateTimeFormat.Label" ) );
    props.setLook( wlDateTimeFormat );
    fdlDateTimeFormat = new FormData();
    fdlDateTimeFormat.left = new FormAttachment( 0, 0 );
    fdlDateTimeFormat.top = new FormAttachment( wSpecifyFormat, margin );
    fdlDateTimeFormat.right = new FormAttachment( middle, -margin );
    wlDateTimeFormat.setLayoutData( fdlDateTimeFormat );
    wDateTimeFormat = new CCombo( wLocalSettings, SWT.BORDER | SWT.READ_ONLY );
    wDateTimeFormat.setEditable( true );
    props.setLook( wDateTimeFormat );
    wDateTimeFormat.addModifyListener( lsMod );
    fdDateTimeFormat = new FormData();
    fdDateTimeFormat.left = new FormAttachment( middle, margin );
    fdDateTimeFormat.top = new FormAttachment( wSpecifyFormat, margin );
    fdDateTimeFormat.right = new FormAttachment( 100, 0 );
    wDateTimeFormat.setLayoutData( fdDateTimeFormat );
    for ( int x = 0; x < dats.length; x++ ) {
      wDateTimeFormat.add( dats[x] );
    }

    // Add Date before extension?
    wlAddDateBeforeExtension = new Label( wLocalSettings, SWT.RIGHT );
    wlAddDateBeforeExtension.setText( BaseMessages.getString( PKG, "JobFTPS.AddDateBeforeExtension.Label" ) );
    props.setLook( wlAddDateBeforeExtension );
    fdlAddDateBeforeExtension = new FormData();
    fdlAddDateBeforeExtension.left = new FormAttachment( 0, 0 );
    fdlAddDateBeforeExtension.top = new FormAttachment( wDateTimeFormat, margin );
    fdlAddDateBeforeExtension.right = new FormAttachment( middle, -margin );
    wlAddDateBeforeExtension.setLayoutData( fdlAddDateBeforeExtension );
    wAddDateBeforeExtension = new Button( wLocalSettings, SWT.CHECK );
    props.setLook( wAddDateBeforeExtension );
    wAddDateBeforeExtension
      .setToolTipText( BaseMessages.getString( PKG, "JobFTPS.AddDateBeforeExtension.Tooltip" ) );
    fdAddDateBeforeExtension = new FormData();
    fdAddDateBeforeExtension.left = new FormAttachment( middle, margin );
    fdAddDateBeforeExtension.top = new FormAttachment( wDateTimeFormat, margin );
    fdAddDateBeforeExtension.right = new FormAttachment( 100, 0 );
    wAddDateBeforeExtension.setLayoutData( fdAddDateBeforeExtension );
    wAddDateBeforeExtension.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e ) {
        jobEntry.setChanged();
      }
    } );

    // OnlyNew files after retrieval...
    wlOnlyNew = new Label( wLocalSettings, SWT.RIGHT );
    wlOnlyNew.setText( BaseMessages.getString( PKG, "JobFTPS.DontOverwrite.Label" ) );
    props.setLook( wlOnlyNew );
    fdlOnlyNew = new FormData();
    fdlOnlyNew.left = new FormAttachment( 0, 0 );
    fdlOnlyNew.top = new FormAttachment( wAddDateBeforeExtension, margin );
    fdlOnlyNew.right = new FormAttachment( middle, 0 );
    wlOnlyNew.setLayoutData( fdlOnlyNew );
    wOnlyNew = new Button( wLocalSettings, SWT.CHECK );
    wOnlyNew.setToolTipText( BaseMessages.getString( PKG, "JobFTPS.DontOverwrite.Tooltip" ) );
    props.setLook( wOnlyNew );
    fdOnlyNew = new FormData();
    fdOnlyNew.left = new FormAttachment( middle, margin );
    fdOnlyNew.top = new FormAttachment( wAddDateBeforeExtension, margin );
    fdOnlyNew.right = new FormAttachment( 100, 0 );
    wOnlyNew.setLayoutData( fdOnlyNew );
    wOnlyNew.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e ) {
        activeIfExists();
        jobEntry.setChanged();
      }
    } );

    // Add filenames to result filenames...
    wlAddFilenameToResult = new Label( wLocalSettings, SWT.RIGHT );
    wlAddFilenameToResult.setText( BaseMessages.getString( PKG, "JobFTPS.AddFilenameToResult.Label" ) );
    props.setLook( wlAddFilenameToResult );
    fdlAddFilenameToResult = new FormData();
    fdlAddFilenameToResult.left = new FormAttachment( 0, 0 );
    fdlAddFilenameToResult.top = new FormAttachment( wAddDateBeforeExtension, margin );
    fdlAddFilenameToResult.right = new FormAttachment( middle, 0 );
    wlAddFilenameToResult.setLayoutData( fdlAddFilenameToResult );
    wAddFilenameToResult = new Button( wLocalSettings, SWT.CHECK );
    wAddFilenameToResult.setToolTipText( BaseMessages.getString( PKG, "JobFTPS.AddFilenameToResult.Tooltip" ) );
    props.setLook( wAddFilenameToResult );
    fdAddFilenameToResult = new FormData();
    fdAddFilenameToResult.left = new FormAttachment( middle, margin );
    fdAddFilenameToResult.top = new FormAttachment( wAddDateBeforeExtension, margin );
    fdAddFilenameToResult.right = new FormAttachment( 100, 0 );
    wAddFilenameToResult.setLayoutData( fdAddFilenameToResult );

    // If File Exists
    wlIfFileExists = new Label( wLocalSettings, SWT.RIGHT );
    wlIfFileExists.setText( BaseMessages.getString( PKG, "JobFTPS.IfFileExists.Label" ) );
    props.setLook( wlIfFileExists );
    fdlIfFileExists = new FormData();
    fdlIfFileExists.left = new FormAttachment( 0, 0 );
    fdlIfFileExists.right = new FormAttachment( middle, 0 );
    fdlIfFileExists.top = new FormAttachment( wOnlyNew, margin );
    wlIfFileExists.setLayoutData( fdlIfFileExists );
    wIfFileExists = new CCombo( wLocalSettings, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER );
    wIfFileExists.add( BaseMessages.getString( PKG, "JobFTPS.Skip.Label" ) );
    wIfFileExists.add( BaseMessages.getString( PKG, "JobFTPS.Give_Unique_Name.Label" ) );
    wIfFileExists.add( BaseMessages.getString( PKG, "JobFTPS.Fail.Label" ) );
    wIfFileExists.select( 0 ); // +1: starts at -1

    props.setLook( wIfFileExists );
    fdIfFileExists = new FormData();
    fdIfFileExists.left = new FormAttachment( middle, 0 );
    fdIfFileExists.top = new FormAttachment( wOnlyNew, margin );
    fdIfFileExists.right = new FormAttachment( 100, 0 );
    wIfFileExists.setLayoutData( fdIfFileExists );

    fdIfFileExists = new FormData();
    fdIfFileExists.left = new FormAttachment( middle, margin );
    fdIfFileExists.top = new FormAttachment( wOnlyNew, margin );
    fdIfFileExists.right = new FormAttachment( 100, 0 );
    wIfFileExists.setLayoutData( fdIfFileExists );

    wIfFileExists.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e ) {

      }
    } );

    // Add filenames to result filenames...
    wlAddFilenameToResult = new Label( wLocalSettings, SWT.RIGHT );
    wlAddFilenameToResult.setText( BaseMessages.getString( PKG, "JobFTPS.AddFilenameToResult.Label" ) );
    props.setLook( wlAddFilenameToResult );
    fdlAddFilenameToResult = new FormData();
    fdlAddFilenameToResult.left = new FormAttachment( 0, 0 );
    fdlAddFilenameToResult.top = new FormAttachment( wIfFileExists, 2 * margin );
    fdlAddFilenameToResult.right = new FormAttachment( middle, 0 );
    wlAddFilenameToResult.setLayoutData( fdlAddFilenameToResult );
    wAddFilenameToResult = new Button( wLocalSettings, SWT.CHECK );
    wAddFilenameToResult.setToolTipText( BaseMessages.getString( PKG, "JobFTPS.AddFilenameToResult.Tooltip" ) );
    props.setLook( wAddFilenameToResult );
    fdAddFilenameToResult = new FormData();
    fdAddFilenameToResult.left = new FormAttachment( middle, margin );
    fdAddFilenameToResult.top = new FormAttachment( wIfFileExists, 2 * margin );
    fdAddFilenameToResult.right = new FormAttachment( 100, 0 );
    wAddFilenameToResult.setLayoutData( fdAddFilenameToResult );

    fdLocalSettings = new FormData();
    fdLocalSettings.left = new FormAttachment( 0, margin );
    fdLocalSettings.top = new FormAttachment( wRemoteSettings, margin );
    fdLocalSettings.right = new FormAttachment( 100, -margin );
    wLocalSettings.setLayoutData( fdLocalSettings );
    // ///////////////////////////////////////////////////////////
    // / END OF LOCAL SETTINGSGROUP
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
    // / END OF Files TAB
    // ///////////////////////////////////////////////////////////

    // ////////////////////////
    // START OF Advanced TAB ///
    // ////////////////////////

    wAdvancedTab = new CTabItem( wTabFolder, SWT.NONE );
    wAdvancedTab.setText( BaseMessages.getString( PKG, "JobFTPS.Tab.Advanced.Label" ) );

    wAdvancedComp = new Composite( wTabFolder, SWT.NONE );
    props.setLook( wAdvancedComp );

    FormLayout AdvancedLayout = new FormLayout();
    AdvancedLayout.marginWidth = 3;
    AdvancedLayout.marginHeight = 3;
    wAdvancedComp.setLayout( AdvancedLayout );

    // SuccessOngrouping?
    // ////////////////////////
    // START OF SUCCESS ON GROUP///
    // /
    wSuccessOn = new Group( wAdvancedComp, SWT.SHADOW_NONE );
    props.setLook( wSuccessOn );
    wSuccessOn.setText( BaseMessages.getString( PKG, "JobFTPS.SuccessOn.Group.Label" ) );

    FormLayout successongroupLayout = new FormLayout();
    successongroupLayout.marginWidth = 10;
    successongroupLayout.marginHeight = 10;

    wSuccessOn.setLayout( successongroupLayout );

    // Success Condition
    wlSuccessCondition = new Label( wSuccessOn, SWT.RIGHT );
    wlSuccessCondition.setText( BaseMessages.getString( PKG, "JobFTPS.SuccessCondition.Label" ) + " " );
    props.setLook( wlSuccessCondition );
    fdlSuccessCondition = new FormData();
    fdlSuccessCondition.left = new FormAttachment( 0, 0 );
    fdlSuccessCondition.right = new FormAttachment( middle, 0 );
    fdlSuccessCondition.top = new FormAttachment( 0, margin );
    wlSuccessCondition.setLayoutData( fdlSuccessCondition );
    wSuccessCondition = new CCombo( wSuccessOn, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER );
    wSuccessCondition.add( BaseMessages.getString( PKG, "JobFTPS.SuccessWhenAllWorksFine.Label" ) );
    wSuccessCondition.add( BaseMessages.getString( PKG, "JobFTPS.SuccessWhenAtLeat.Label" ) );
    wSuccessCondition.add( BaseMessages.getString( PKG, "JobFTPS.SuccessWhenNrErrorsLessThan.Label" ) );
    wSuccessCondition.select( 0 ); // +1: starts at -1

    props.setLook( wSuccessCondition );
    fdSuccessCondition = new FormData();
    fdSuccessCondition.left = new FormAttachment( middle, 0 );
    fdSuccessCondition.top = new FormAttachment( 0, margin );
    fdSuccessCondition.right = new FormAttachment( 100, 0 );
    wSuccessCondition.setLayoutData( fdSuccessCondition );
    wSuccessCondition.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e ) {
        activeSuccessCondition();

      }
    } );

    // Success when number of errors less than
    wlNrErrorsLessThan = new Label( wSuccessOn, SWT.RIGHT );
    wlNrErrorsLessThan.setText( BaseMessages.getString( PKG, "JobFTPS.NrBadFormedLessThan.Label" ) + " " );
    props.setLook( wlNrErrorsLessThan );
    fdlNrErrorsLessThan = new FormData();
    fdlNrErrorsLessThan.left = new FormAttachment( 0, 0 );
    fdlNrErrorsLessThan.top = new FormAttachment( wSuccessCondition, margin );
    fdlNrErrorsLessThan.right = new FormAttachment( middle, -margin );
    wlNrErrorsLessThan.setLayoutData( fdlNrErrorsLessThan );

    wNrErrorsLessThan = new TextVar( jobMeta, wSuccessOn, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wNrErrorsLessThan );
    wNrErrorsLessThan.setToolTipText( BaseMessages.getString( PKG, "JobFTPS.NrBadFormedLessThan.Tooltip" ) );
    wNrErrorsLessThan.addModifyListener( lsMod );
    fdNrErrorsLessThan = new FormData();
    fdNrErrorsLessThan.left = new FormAttachment( middle, 0 );
    fdNrErrorsLessThan.top = new FormAttachment( wSuccessCondition, margin );
    fdNrErrorsLessThan.right = new FormAttachment( 100, -margin );
    wNrErrorsLessThan.setLayoutData( fdNrErrorsLessThan );

    fdSuccessOn = new FormData();
    fdSuccessOn.left = new FormAttachment( 0, margin );
    fdSuccessOn.top = new FormAttachment( 0, margin );
    fdSuccessOn.right = new FormAttachment( 100, -margin );
    wSuccessOn.setLayoutData( fdSuccessOn );
    // ///////////////////////////////////////////////////////////
    // / END OF Success ON GROUP
    // ///////////////////////////////////////////////////////////

    fdAdvancedComp = new FormData();
    fdAdvancedComp.left = new FormAttachment( 0, 0 );
    fdAdvancedComp.top = new FormAttachment( 0, 0 );
    fdAdvancedComp.right = new FormAttachment( 100, 0 );
    fdAdvancedComp.bottom = new FormAttachment( 100, 0 );
    wAdvancedComp.setLayoutData( fdAdvancedComp );

    wAdvancedComp.layout();
    wAdvancedTab.setControl( wAdvancedComp );
    props.setLook( wAdvancedComp );

    // ///////////////////////////////////////////////////////////
    // / END OF Advanced TAB
    // ///////////////////////////////////////////////////////////

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
        checkRemoteFolder( false, true, wMoveToDirectory.getText() );
      }
    };
    lsCheckChangeFolder = new Listener() {
      public void handleEvent( Event e ) {
        checkRemoteFolder( true, false, wFTPSDirectory.getText() );
      }
    };

    wCancel.addListener( SWT.Selection, lsCancel );
    wOK.addListener( SWT.Selection, lsOK );
    wTest.addListener( SWT.Selection, lsTest );
    wbTestFolderExists.addListener( SWT.Selection, lsCheckFolder );
    wbTestChangeFolderExists.addListener( SWT.Selection, lsCheckChangeFolder );

    lsDef = new SelectionAdapter() {
      public void widgetDefaultSelected( SelectionEvent e ) {
        ok();
      }
    };

    wName.addSelectionListener( lsDef );
    wServerName.addSelectionListener( lsDef );
    wUserName.addSelectionListener( lsDef );
    wPassword.addSelectionListener( lsDef );
    wFTPSDirectory.addSelectionListener( lsDef );
    wTargetDirectory.addSelectionListener( lsDef );
    wFTPSDirectory.addSelectionListener( lsDef );
    wWildcard.addSelectionListener( lsDef );
    wTimeout.addSelectionListener( lsDef );

    // Detect X or ALT-F4 or something that kills this window...
    shell.addShellListener( new ShellAdapter() {
      public void shellClosed( ShellEvent e ) {
        cancel();
      }
    } );

    getData();
    activateMoveTo();
    setDateTimeFormat();
    setAddDateBeforeExtension();
    activeSuccessCondition();
    activeIfExists();

    wTabFolder.setSelection( 0 );
    BaseStepDialog.setSize( shell );

    shell.open();
    props.setDialogSize( shell, "JobFTPSDialogSize" );
    while ( !shell.isDisposed() ) {
      if ( !display.readAndDispatch() ) {
        display.sleep();
      }
    }
    return jobEntry;
  }

  private void activeIfExists() {
    wIfFileExists.setEnabled( wOnlyNew.getSelection() );
    wlIfFileExists.setEnabled( wOnlyNew.getSelection() );
  }

  private void test() {

    if ( connectToFTPS( false, false ) ) {
      MessageBox mb = new MessageBox( shell, SWT.OK | SWT.ICON_INFORMATION );
      mb.setMessage( BaseMessages.getString( PKG, "JobFTPS.Connected.OK", wServerName.getText() ) + Const.CR );
      mb.setText( BaseMessages.getString( PKG, "JobFTPS.Connected.Title.Ok" ) );
      mb.open();
    }
  }

  private void checkRemoteFolder( boolean FTPSFolfer, boolean checkMoveFolder, String foldername ) {
    if ( !Utils.isEmpty( foldername ) ) {
      if ( connectToFTPS( FTPSFolfer, checkMoveFolder ) ) {
        MessageBox mb = new MessageBox( shell, SWT.OK | SWT.ICON_INFORMATION );
        mb.setMessage( BaseMessages.getString( PKG, "JobFTPS.FolderExists.OK", foldername ) + Const.CR );
        mb.setText( BaseMessages.getString( PKG, "JobFTPS.FolderExists.Title.Ok" ) );
        mb.open();
      }
    }
  }

  private boolean connectToFTPS( boolean checkfolder, boolean checkmoveToFolder ) {
    boolean retval = true;
    String realServername = null;
    try {
      if ( connection == null ) {
        // Create FTPS client to host:port ...
        realServername = jobMeta.environmentSubstitute( wServerName.getText() );
        int port = Const.toInt( jobMeta.environmentSubstitute( wPort.getText() ), 0 );
        String realUsername = jobMeta.environmentSubstitute( wUserName.getText() );
        String realPassword = Utils.resolvePassword( jobMeta, wPassword.getText() );

        connection =
          new FTPSConnection(
            FTPSConnection.getConnectionTypeByDesc( wConnectionType.getText() ), realServername, port,
            realUsername, realPassword );

        if ( !Utils.isEmpty( wProxyHost.getText() ) ) {
          // Set proxy
          String realProxy_host = jobMeta.environmentSubstitute( wProxyHost.getText() );
          String realProxy_user = jobMeta.environmentSubstitute( wProxyUsername.getText() );
          String realProxy_pass = Utils.resolvePassword( jobMeta, wProxyPassword.getText() );
          connection.setProxyHost( realProxy_host );
          int proxyport = Const.toInt( jobMeta.environmentSubstitute( wProxyPort.getText() ), 990 );
          if ( proxyport != 0 ) {
            connection.setProxyPort( proxyport );
          }
          if ( !Utils.isEmpty( realProxy_user ) ) {
            connection.setProxyUser( realProxy_user );
          }
          if ( !Utils.isEmpty( realProxy_pass ) ) {
            connection.setProxyPassword( realProxy_pass );
          }
        }

        // login to FTPS host ...
        connection.connect();
        // pwdFolder=connection.getWorkingDirectory();
      }

      String realFTPSDirectory = null;
      if ( !Utils.isEmpty( wFTPSDirectory.getText() ) ) {
        realFTPSDirectory = jobMeta.environmentSubstitute( wFTPSDirectory.getText() );
      }

      if ( checkfolder ) {
        // if(pwdFolder!=null) connection.changeDirectory(pwdFolder);
        // move to spool dir ...
        if ( !Utils.isEmpty( realFTPSDirectory ) ) {
          // connection.changeDirectory(realFTPSDirectory);
          retval = connection.isDirectoryExists( realFTPSDirectory );
        }
      }
      if ( checkmoveToFolder ) {
        // if(pwdFolder!=null) connection.changeDirectory(pwdFolder);

        // move to folder ...

        if ( !Utils.isEmpty( wMoveToDirectory.getText() ) ) {
          String realMoveDirectory = jobMeta.environmentSubstitute( wMoveToDirectory.getText() );
          // realMoveDirectory=realFTPSDirectory+"/"+realMoveDirectory;
          // connection.changeDirectory(realMoveDirectory);
          retval = connection.isDirectoryExists( realMoveDirectory );
        }
      }

    } catch ( Exception e ) {
      retval = false;
      if ( connection != null ) {
        try {
          connection.disconnect();
        } catch ( Exception ignored ) {
          // We've tried quitting the FTPS Client exception
          // nothing else to be done if the FTPS Client was already disconnected
        }
        connection = null;
      }
      MessageBox mb = new MessageBox( shell, SWT.OK | SWT.ICON_ERROR );
      mb.setMessage( BaseMessages.getString( PKG, "JobFTPS.ErrorConnect.NOK", realServername,
          e.getMessage() ) + Const.CR );
      mb.setText( BaseMessages.getString( PKG, "JobFTPS.ErrorConnect.Title.Bad" ) );
      mb.open();
    }
    return retval;
  }

  private void activeSuccessCondition() {
    wlNrErrorsLessThan.setEnabled( wSuccessCondition.getSelectionIndex() != 0 );
    wNrErrorsLessThan.setEnabled( wSuccessCondition.getSelectionIndex() != 0 );
  }

  private void setAddDateBeforeExtension() {
    wlAddDateBeforeExtension.setEnabled( wAddDate.getSelection()
      || wAddTime.getSelection() || wSpecifyFormat.getSelection() );
    wAddDateBeforeExtension.setEnabled( wAddDate.getSelection()
      || wAddTime.getSelection() || wSpecifyFormat.getSelection() );
    if ( !wAddDate.getSelection() && !wAddTime.getSelection() && !wSpecifyFormat.getSelection() ) {
      wAddDateBeforeExtension.setSelection( false );
    }
  }

  public void activateMoveTo() {
    wMoveToDirectory.setEnabled( wMove.getSelection() );
    wlMoveToDirectory.setEnabled( wMove.getSelection() );
    wCreateMoveFolder.setEnabled( wMove.getSelection() );
    wlCreateMoveFolder.setEnabled( wMove.getSelection() );
    wbTestFolderExists.setEnabled( wMove.getSelection() );
  }

  private void setDateTimeFormat() {
    if ( wSpecifyFormat.getSelection() ) {
      wAddDate.setSelection( false );
      wAddTime.setSelection( false );
    }

    wDateTimeFormat.setEnabled( wSpecifyFormat.getSelection() );
    wlDateTimeFormat.setEnabled( wSpecifyFormat.getSelection() );
    wAddDate.setEnabled( !wSpecifyFormat.getSelection() );
    wlAddDate.setEnabled( !wSpecifyFormat.getSelection() );
    wAddTime.setEnabled( !wSpecifyFormat.getSelection() );
    wlAddTime.setEnabled( !wSpecifyFormat.getSelection() );

  }

  public void dispose() {
    closeFTPSConnection();
    WindowProperty winprop = new WindowProperty( shell );
    props.setScreen( winprop );
    shell.dispose();
  }

  /**
   * Copy information from the meta-data input to the dialog fields.
   */
  public void getData() {
    wName.setText( Const.nullToEmpty( jobEntry.getName() ) );

    wServerName.setText( Const.NVL( jobEntry.getServerName(), "" ) );
    wPort.setText( Const.NVL( jobEntry.getPort(), "21" ) );
    wUserName.setText( Const.NVL( jobEntry.getUserName(), "" ) );
    wPassword.setText( Const.NVL( jobEntry.getPassword(), "" ) );
    wFTPSDirectory.setText( Const.NVL( jobEntry.getFTPSDirectory(), "" ) );
    wTargetDirectory.setText( Const.NVL( jobEntry.getTargetDirectory(), "" ) );
    wWildcard.setText( Const.NVL( jobEntry.getWildcard(), "" ) );
    wBinaryMode.setSelection( jobEntry.isBinaryMode() );
    wTimeout.setText( "" + jobEntry.getTimeout() );
    wRemove.setSelection( jobEntry.getRemove() );
    wOnlyNew.setSelection( jobEntry.isOnlyGettingNewFiles() );
    wActive.setSelection( jobEntry.isActiveConnection() );
    wMove.setSelection( jobEntry.isMoveFiles() );
    wMoveToDirectory.setText( Const.NVL( jobEntry.getMoveToDirectory(), "" ) );

    wAddDate.setSelection( jobEntry.isDateInFilename() );
    wAddTime.setSelection( jobEntry.isTimeInFilename() );

    wDateTimeFormat.setText( Const.nullToEmpty( jobEntry.getDateTimeFormat() ) );
    wSpecifyFormat.setSelection( jobEntry.isSpecifyFormat() );

    wAddDateBeforeExtension.setSelection( jobEntry.isAddDateBeforeExtension() );
    wAddFilenameToResult.setSelection( jobEntry.isAddToResult() );
    wCreateMoveFolder.setSelection( jobEntry.isCreateMoveFolder() );

    wProxyHost.setText( Const.NVL( jobEntry.getProxyHost(), "" ) );
    wProxyPort.setText( Const.NVL( jobEntry.getProxyPort(), "" ) );
    wProxyUsername.setText( Const.NVL( jobEntry.getProxyUsername(), "" ) );
    wProxyPassword.setText( Const.NVL( jobEntry.getProxyPassword(), "" ) );

    wIfFileExists.select( jobEntry.getIfFileExists() );

    wNrErrorsLessThan.setText( Const.NVL( jobEntry.getLimit(), "10" ) );

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

    wConnectionType.setText( FTPSConnection.getConnectionTypeDesc( jobEntry.getConnectionType() ) );

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
    jobEntry.setPort( wPort.getText() );
    jobEntry.setServerName( wServerName.getText() );
    jobEntry.setUserName( wUserName.getText() );
    jobEntry.setPassword( wPassword.getText() );
    jobEntry.setFTPSDirectory( wFTPSDirectory.getText() );
    jobEntry.setTargetDirectory( wTargetDirectory.getText() );
    jobEntry.setWildcard( wWildcard.getText() );
    jobEntry.setBinaryMode( wBinaryMode.getSelection() );
    jobEntry.setTimeout( Const.toInt( wTimeout.getText(), 10000 ) );
    jobEntry.setRemove( wRemove.getSelection() );
    jobEntry.setOnlyGettingNewFiles( wOnlyNew.getSelection() );
    jobEntry.setActiveConnection( wActive.getSelection() );
    jobEntry.setMoveFiles( wMove.getSelection() );
    jobEntry.setMoveToDirectory( wMoveToDirectory.getText() );

    jobEntry.setDateInFilename( wAddDate.getSelection() );
    jobEntry.setTimeInFilename( wAddTime.getSelection() );
    jobEntry.setSpecifyFormat( wSpecifyFormat.getSelection() );
    jobEntry.setDateTimeFormat( wDateTimeFormat.getText() );

    jobEntry.setAddDateBeforeExtension( wAddDateBeforeExtension.getSelection() );
    jobEntry.setAddToResult( wAddFilenameToResult.getSelection() );
    jobEntry.setCreateMoveFolder( wCreateMoveFolder.getSelection() );

    jobEntry.setProxyHost( wProxyHost.getText() );
    jobEntry.setProxyPort( wProxyPort.getText() );
    jobEntry.setProxyUsername( wProxyUsername.getText() );
    jobEntry.setProxyPassword( wProxyPassword.getText() );

    int index = wIfFileExists.getSelectionIndex();
    jobEntry.setIfFileExists( index );

    jobEntry.setLimit( wNrErrorsLessThan.getText() );

    if ( wSuccessCondition.getSelectionIndex() == 1 ) {
      jobEntry.setSuccessCondition( jobEntry.SUCCESS_IF_AT_LEAST_X_FILES_DOWNLOADED );
    } else if ( wSuccessCondition.getSelectionIndex() == 2 ) {
      jobEntry.setSuccessCondition( jobEntry.SUCCESS_IF_ERRORS_LESS );
    } else {
      jobEntry.setSuccessCondition( jobEntry.SUCCESS_IF_NO_ERRORS );
    }

    jobEntry.setConnectionType( FTPSConnection.getConnectionTypeByDesc( wConnectionType.getText() ) );

    dispose();
  }

  private void closeFTPSConnection() {
    // Close FTPS connection if necessary
    if ( connection != null ) {
      try {
        connection.disconnect();
      } catch ( Exception e ) {
        // Ignore errors
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
