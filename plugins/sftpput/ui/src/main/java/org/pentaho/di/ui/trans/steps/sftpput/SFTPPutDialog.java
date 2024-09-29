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

package org.pentaho.di.ui.trans.steps.sftpput;

import com.google.common.annotations.VisibleForTesting;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Cursor;
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
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.annotations.PluginDialog;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleJobException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.entries.sftp.SFTPClient;
import org.pentaho.di.job.entries.sftpput.JobEntrySFTPPUT;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.steps.sftpput.SFTPPutMeta;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.events.dialog.FilterType;
import org.pentaho.di.ui.core.events.dialog.SelectionAdapterFileDialogTextVar;
import org.pentaho.di.ui.core.events.dialog.SelectionAdapterOptions;
import org.pentaho.di.ui.core.events.dialog.SelectionOperation;
import org.pentaho.di.ui.core.widget.LabelTextVar;
import org.pentaho.di.ui.core.widget.PasswordTextVar;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Send file to SFTP host.
 *
 * @author Samatar Hassan
 * @since 30-April-2012
 */

@PluginDialog( id = "SFTPPut", image = "SFP.svg", pluginType = PluginDialog.PluginType.STEP,
        documentationUrl = "http://wiki.pentaho.com/display/EAI/SFTP+Put" )
public class SFTPPutDialog extends BaseStepDialog implements StepDialogInterface {
  //for i18n purposes, needed by Translator2!!
  private static Class<?> PKG = org.pentaho.di.trans.steps.sftpput.SFTPPutMeta.class;

  private boolean gotPreviousFields = false;

  private SFTPPutMeta input;

  private CTabFolder wTabFolder;
  private Composite wGeneralComp, wFilesComp;
  private CTabItem wGeneralTab, wFilesTab;
  private FormData fdGeneralComp, fdFilesComp;
  private FormData fdTabFolder;
  private SelectionAdapter lsDef;

  private Group wServerSettings;

  private FormData fdServerSettings;

  private Group wSourceFiles;

  private FormData fdSourceFiles;

  private Group wTargetFiles;

  private FormData fdTargetFiles;

  private Label wlSourceFileNameField;
  private CCombo wSourceFileNameField;
  private FormData fdlSourceFileNameField, fdSourceFileNameField;

  private boolean changed;

  private Button wTest;

  private FormData fdTest;

  private Listener lsTest;

  private Label wlAddFilenameToResult;
  private Button wAddFilenameToResult;
  private FormData fdlAddFilenameToResult, fdAddFilenameToResult;

  private Label wlInputIsStream;
  private Button wInputIsStream;
  private FormData fdlInputIsStream, fdInputIsStream;

  private LabelTextVar wkeyfilePass;

  private FormData fdkeyfilePass;

  private Label wlusePublicKey;

  private Button wusePublicKey;

  private FormData fdlusePublicKey, fdusePublicKey;

  private Label wlKeyFilename;

  private Button wbKeyFilename;

  private TextVar wKeyFilename;

  private FormData fdlKeyFilename, fdbKeyFilename, fdKeyFilename;

  private Label wlCreateRemoteFolder;
  private Button wCreateRemoteFolder;
  private FormData fdlCreateRemoteFolder, fdCreateRemoteFolder;

  private Label wlServerName;
  private TextVar wServerName;
  private FormData fdlServerName, fdServerName;

  private Label wlServerPort;
  private TextVar wServerPort;
  private FormData fdlServerPort, fdServerPort;

  private Label wlUserName;
  private TextVar wUserName;
  private FormData fdlUserName, fdUserName;

  private Label wlPassword;
  private TextVar wPassword;
  private FormData fdlPassword, fdPassword;

  private Label wlRemoteDirectory;
  private CCombo wRemoteDirectory;
  private FormData fdlRemoteDirectory, fdRemoteDirectory;

  private Label wlProxyType;
  private FormData fdlProxyType;
  private CCombo wProxyType;
  private FormData fdProxyType;

  private LabelTextVar wProxyHost;
  private FormData fdProxyHost;
  private LabelTextVar wProxyPort;
  private FormData fdProxyPort;
  private LabelTextVar wProxyUsername;
  private FormData fdProxyUsername;
  private LabelTextVar wProxyPassword;
  private FormData fdProxyPasswd;

  private Label wlCompression;
  private FormData fdlCompression;
  private CCombo wCompression;
  private FormData fdCompression;

  private Label wlAfterFTPPut;
  private CCombo wAfterFTPPut;
  private FormData fdlAfterFTPPut, fdAfterFTPPut;

  private Label wlCreateDestinationFolder;
  private Button wCreateDestinationFolder;
  private FormData fdlCreateDestinationFolder, fdCreateDestinationFolder;

  private Label wlDestinationFolderFieldName;
  private CCombo wDestinationFolderFieldName;
  private FormData fdlDestinationFolderFieldName, fdDestinationFolderFieldName;

  private Label wlRemoteFileName;
  private CCombo wRemoteFileName;
  private FormData fdlRemoteFileName, fdRemoteFileName;

  private SFTPClient sftpclient = null;

  public SFTPPutDialog( Shell parent, Object in, TransMeta tr, String sname ) {
    super( parent, (BaseStepMeta) in, tr, sname );
    input = (SFTPPutMeta) in;
  }

  public String open() {
    Shell parent = getParent();
    Display display = parent.getDisplay();

    shell = new Shell( parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MIN | SWT.MAX );
    props.setLook( shell );
    setShellImage( shell, input );

    ModifyListener lsMod = new ModifyListener() {
      public void modifyText( ModifyEvent e ) {
        input.setChanged();
      }
    };
    changed = input.hasChanged();

    FormLayout formLayout = new FormLayout();
    formLayout.marginWidth = Const.FORM_MARGIN;
    formLayout.marginHeight = Const.FORM_MARGIN;

    shell.setLayout( formLayout );
    shell.setText( BaseMessages.getString( PKG, "SFTPPutDialog.Shell.Title" ) );

    int middle = props.getMiddlePct();
    int margin = Const.MARGIN;

    // Stepname line
    wlStepname = new Label( shell, SWT.RIGHT );
    wlStepname.setText( BaseMessages.getString( PKG, "SFTPPutDialog.Stepname.Label" ) );
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

    wTabFolder = new CTabFolder( shell, SWT.BORDER );
    props.setLook( wTabFolder, Props.WIDGET_STYLE_TAB );

    // ////////////////////////
    // START OF GENERAL TAB ///
    // ////////////////////////

    wGeneralTab = new CTabItem( wTabFolder, SWT.NONE );
    wGeneralTab.setText( BaseMessages.getString( PKG, "SFTPPutDialog.Tab.General.Label" ) );

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
    wServerSettings.setText( BaseMessages.getString( PKG, "SFTPPUT.ServerSettings.Group.Label" ) );
    FormLayout ServerSettingsgroupLayout = new FormLayout();
    ServerSettingsgroupLayout.marginWidth = 10;
    ServerSettingsgroupLayout.marginHeight = 10;
    wServerSettings.setLayout( ServerSettingsgroupLayout );

    // ServerName line
    wlServerName = new Label( wServerSettings, SWT.RIGHT );
    wlServerName.setText( BaseMessages.getString( PKG, "SFTPPUT.Server.Label" ) );
    props.setLook( wlServerName );
    fdlServerName = new FormData();
    fdlServerName.left = new FormAttachment( 0, 0 );
    fdlServerName.top = new FormAttachment( wStepname, margin );
    fdlServerName.right = new FormAttachment( middle, -margin );
    wlServerName.setLayoutData( fdlServerName );
    wServerName = new TextVar( transMeta, wServerSettings, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wServerName );
    wServerName.addModifyListener( lsMod );
    fdServerName = new FormData();
    fdServerName.left = new FormAttachment( middle, 0 );
    fdServerName.top = new FormAttachment( wStepname, margin );
    fdServerName.right = new FormAttachment( 100, 0 );
    wServerName.setLayoutData( fdServerName );

    // ServerPort line
    wlServerPort = new Label( wServerSettings, SWT.RIGHT );
    wlServerPort.setText( BaseMessages.getString( PKG, "SFTPPUT.Port.Label" ) );
    props.setLook( wlServerPort );
    fdlServerPort = new FormData();
    fdlServerPort.left = new FormAttachment( 0, 0 );
    fdlServerPort.top = new FormAttachment( wServerName, margin );
    fdlServerPort.right = new FormAttachment( middle, -margin );
    wlServerPort.setLayoutData( fdlServerPort );
    wServerPort = new TextVar( transMeta, wServerSettings, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wServerPort );
    wServerPort.setToolTipText( BaseMessages.getString( PKG, "SFTPPUT.Port.Tooltip" ) );
    wServerPort.addModifyListener( lsMod );
    fdServerPort = new FormData();
    fdServerPort.left = new FormAttachment( middle, 0 );
    fdServerPort.top = new FormAttachment( wServerName, margin );
    fdServerPort.right = new FormAttachment( 100, 0 );
    wServerPort.setLayoutData( fdServerPort );

    // UserName line
    wlUserName = new Label( wServerSettings, SWT.RIGHT );
    wlUserName.setText( BaseMessages.getString( PKG, "SFTPPUT.Username.Label" ) );
    props.setLook( wlUserName );
    fdlUserName = new FormData();
    fdlUserName.left = new FormAttachment( 0, 0 );
    fdlUserName.top = new FormAttachment( wServerPort, margin );
    fdlUserName.right = new FormAttachment( middle, -margin );
    wlUserName.setLayoutData( fdlUserName );
    wUserName = new TextVar( transMeta, wServerSettings, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wUserName );
    wUserName.setToolTipText( BaseMessages.getString( PKG, "SFTPPUT.Username.Tooltip" ) );
    wUserName.addModifyListener( lsMod );
    fdUserName = new FormData();
    fdUserName.left = new FormAttachment( middle, 0 );
    fdUserName.top = new FormAttachment( wServerPort, margin );
    fdUserName.right = new FormAttachment( 100, 0 );
    wUserName.setLayoutData( fdUserName );

    // Password line
    wlPassword = new Label( wServerSettings, SWT.RIGHT );
    wlPassword.setText( BaseMessages.getString( PKG, "SFTPPUT.Password.Label" ) );
    props.setLook( wlPassword );
    fdlPassword = new FormData();
    fdlPassword.left = new FormAttachment( 0, 0 );
    fdlPassword.top = new FormAttachment( wUserName, margin );
    fdlPassword.right = new FormAttachment( middle, -margin );
    wlPassword.setLayoutData( fdlPassword );
    wPassword = new PasswordTextVar( transMeta, wServerSettings, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wPassword );
    wPassword.addModifyListener( lsMod );
    fdPassword = new FormData();
    fdPassword.left = new FormAttachment( middle, 0 );
    fdPassword.top = new FormAttachment( wUserName, margin );
    fdPassword.right = new FormAttachment( 100, 0 );
    wPassword.setLayoutData( fdPassword );

    // usePublicKey
    wlusePublicKey = new Label( wServerSettings, SWT.RIGHT );
    wlusePublicKey.setText( BaseMessages.getString( PKG, "SFTPPUT.useKeyFile.Label" ) );
    props.setLook( wlusePublicKey );
    fdlusePublicKey = new FormData();
    fdlusePublicKey.left = new FormAttachment( 0, 0 );
    fdlusePublicKey.top = new FormAttachment( wPassword, margin );
    fdlusePublicKey.right = new FormAttachment( middle, -margin );
    wlusePublicKey.setLayoutData( fdlusePublicKey );
    wusePublicKey = new Button( wServerSettings, SWT.CHECK );
    wusePublicKey.setToolTipText( BaseMessages.getString( PKG, "SFTPPUT.useKeyFile.Tooltip" ) );
    props.setLook( wusePublicKey );
    fdusePublicKey = new FormData();
    fdusePublicKey.left = new FormAttachment( middle, 0 );
    fdusePublicKey.top = new FormAttachment( wPassword, margin );
    fdusePublicKey.right = new FormAttachment( 100, 0 );
    wusePublicKey.setLayoutData( fdusePublicKey );
    wusePublicKey.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e ) {
        activeUseKey();
        transMeta.setChanged();
      }
    } );

    // Key File
    wlKeyFilename = new Label( wServerSettings, SWT.RIGHT );
    wlKeyFilename.setText( BaseMessages.getString( PKG, "SFTPPUT.KeyFilename.Label" ) );
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

    wKeyFilename = new TextVar( transMeta, wServerSettings, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    wKeyFilename.setToolTipText( BaseMessages.getString( PKG, "SFTPPUT.KeyFilename.Tooltip" ) );
    props.setLook( wKeyFilename );
    wKeyFilename.addModifyListener( lsMod );
    fdKeyFilename = new FormData();
    fdKeyFilename.left = new FormAttachment( middle, 0 );
    fdKeyFilename.top = new FormAttachment( wusePublicKey, margin );
    fdKeyFilename.right = new FormAttachment( wbKeyFilename, -margin );
    wKeyFilename.setLayoutData( fdKeyFilename );

    wbKeyFilename.addSelectionListener( new SelectionAdapterFileDialogTextVar( log, wKeyFilename, transMeta,
      new SelectionAdapterOptions( SelectionOperation.FILE,
        new FilterType[] { FilterType.PEM, FilterType.ALL }, FilterType.PEM  ) ) );

    // keyfilePass line
    wkeyfilePass =
      new LabelTextVar(
        transMeta, wServerSettings, BaseMessages.getString( PKG, "SFTPPUT.keyfilePass.Label" ), BaseMessages
          .getString( PKG, "SFTPPUT.keyfilePass.Tooltip" ), true );
    props.setLook( wkeyfilePass );
    wkeyfilePass.addModifyListener( lsMod );
    fdkeyfilePass = new FormData();
    fdkeyfilePass.left = new FormAttachment( 0, -margin );
    fdkeyfilePass.top = new FormAttachment( wKeyFilename, margin );
    fdkeyfilePass.right = new FormAttachment( 100, 0 );
    wkeyfilePass.setLayoutData( fdkeyfilePass );

    wlProxyType = new Label( wServerSettings, SWT.RIGHT );
    wlProxyType.setText( BaseMessages.getString( PKG, "SFTPPUT.ProxyType.Label" ) );
    props.setLook( wlProxyType );
    fdlProxyType = new FormData();
    fdlProxyType.left = new FormAttachment( 0, 0 );
    fdlProxyType.right = new FormAttachment( middle, -margin );
    fdlProxyType.top = new FormAttachment( wkeyfilePass, 2 * margin );
    wlProxyType.setLayoutData( fdlProxyType );

    wProxyType = new CCombo( wServerSettings, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER );
    wProxyType.add( SFTPClient.PROXY_TYPE_HTTP );
    wProxyType.add( SFTPClient.PROXY_TYPE_SOCKS5 );
    wProxyType.select( 0 ); // +1: starts at -1
    props.setLook( wProxyType );
    fdProxyType = new FormData();
    fdProxyType.left = new FormAttachment( middle, 0 );
    fdProxyType.top = new FormAttachment( wkeyfilePass, 2 * margin );
    fdProxyType.right = new FormAttachment( 100, 0 );
    wProxyType.setLayoutData( fdProxyType );
    wProxyType.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e ) {
        setDefaulProxyPort();
      }
    } );

    // Proxy host line
    wProxyHost =
      new LabelTextVar(
        transMeta, wServerSettings, BaseMessages.getString( PKG, "SFTPPUT.ProxyHost.Label" ), BaseMessages
          .getString( PKG, "SFTPPUT.ProxyHost.Tooltip" ) );
    props.setLook( wProxyHost );
    wProxyHost.addModifyListener( lsMod );
    fdProxyHost = new FormData();
    fdProxyHost.left = new FormAttachment( 0, -2 * margin );
    fdProxyHost.top = new FormAttachment( wProxyType, margin );
    fdProxyHost.right = new FormAttachment( 100, 0 );
    wProxyHost.setLayoutData( fdProxyHost );

    // Proxy port line
    wProxyPort =
      new LabelTextVar(
        transMeta, wServerSettings, BaseMessages.getString( PKG, "SFTPPUT.ProxyPort.Label" ), BaseMessages
          .getString( PKG, "SFTPPUT.ProxyPort.Tooltip" ) );
    props.setLook( wProxyPort );
    wProxyPort.addModifyListener( lsMod );
    fdProxyPort = new FormData();
    fdProxyPort.left = new FormAttachment( 0, -2 * margin );
    fdProxyPort.top = new FormAttachment( wProxyHost, margin );
    fdProxyPort.right = new FormAttachment( 100, 0 );
    wProxyPort.setLayoutData( fdProxyPort );

    // Proxy username line
    wProxyUsername =
      new LabelTextVar(
        transMeta, wServerSettings, BaseMessages.getString( PKG, "SFTPPUT.ProxyUsername.Label" ), BaseMessages
          .getString( PKG, "SFTPPUT.ProxyUsername.Tooltip" ) );
    props.setLook( wProxyUsername );
    wProxyUsername.addModifyListener( lsMod );
    fdProxyUsername = new FormData();
    fdProxyUsername.left = new FormAttachment( 0, -2 * margin );
    fdProxyUsername.top = new FormAttachment( wProxyPort, margin );
    fdProxyUsername.right = new FormAttachment( 100, 0 );
    wProxyUsername.setLayoutData( fdProxyUsername );

    // Proxy password line
    wProxyPassword =
      new LabelTextVar(
        transMeta, wServerSettings, BaseMessages.getString( PKG, "SFTPPUT.ProxyPassword.Label" ), BaseMessages
          .getString( PKG, "SFTPPUT.ProxyPassword.Tooltip" ), true );
    props.setLook( wProxyPassword );
    wProxyPassword.addModifyListener( lsMod );
    fdProxyPasswd = new FormData();
    fdProxyPasswd.left = new FormAttachment( 0, -2 * margin );
    fdProxyPasswd.top = new FormAttachment( wProxyUsername, margin );
    fdProxyPasswd.right = new FormAttachment( 100, 0 );
    wProxyPassword.setLayoutData( fdProxyPasswd );

    // Test connection button
    wTest = new Button( wServerSettings, SWT.PUSH );
    wTest.setText( BaseMessages.getString( PKG, "SFTPPUT.TestConnection.Label" ) );
    props.setLook( wTest );
    fdTest = new FormData();
    wTest.setToolTipText( BaseMessages.getString( PKG, "SFTPPUT.TestConnection.Tooltip" ) );
    fdTest.top = new FormAttachment( wProxyPassword, margin );
    fdTest.right = new FormAttachment( 100, 0 );
    wTest.setLayoutData( fdTest );

    fdServerSettings = new FormData();
    fdServerSettings.left = new FormAttachment( 0, margin );
    fdServerSettings.top = new FormAttachment( wStepname, margin );
    fdServerSettings.right = new FormAttachment( 100, -margin );
    wServerSettings.setLayoutData( fdServerSettings );
    // ///////////////////////////////////////////////////////////
    // / END OF SERVER SETTINGS GROUP
    // ///////////////////////////////////////////////////////////

    wlCompression = new Label( wGeneralComp, SWT.RIGHT );
    wlCompression.setText( BaseMessages.getString( PKG, "SFTPPUT.Compression.Label" ) );
    props.setLook( wlCompression );
    fdlCompression = new FormData();
    fdlCompression.left = new FormAttachment( 0, -margin );
    fdlCompression.right = new FormAttachment( middle, 0 );
    fdlCompression.top = new FormAttachment( wServerSettings, margin );
    wlCompression.setLayoutData( fdlCompression );

    wCompression = new CCombo( wGeneralComp, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER );
    wCompression.add( "none" );
    wCompression.add( "zlib" );
    wCompression.select( 0 ); // +1: starts at -1
    wCompression.addModifyListener( lsMod );

    props.setLook( wCompression );
    fdCompression = new FormData();
    fdCompression.left = new FormAttachment( middle, margin );
    fdCompression.top = new FormAttachment( wServerSettings, margin );
    fdCompression.right = new FormAttachment( 100, 0 );
    wCompression.setLayoutData( fdCompression );

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
    wFilesTab.setText( BaseMessages.getString( PKG, "SFTPPUT.Tab.Files.Label" ) );

    wFilesComp = new Composite( wTabFolder, SWT.NONE );
    props.setLook( wFilesComp );

    FormLayout FilesLayout = new FormLayout();
    FilesLayout.marginWidth = 3;
    FilesLayout.marginHeight = 3;
    wFilesComp.setLayout( FilesLayout );

    // ////////////////////////
    // START OF Source files GROUP///
    // /
    wSourceFiles = new Group( wFilesComp, SWT.SHADOW_NONE );
    props.setLook( wSourceFiles );
    wSourceFiles.setText( BaseMessages.getString( PKG, "SFTPPUT.SourceFiles.Group.Label" ) );
    FormLayout SourceFilesgroupLayout = new FormLayout();
    SourceFilesgroupLayout.marginWidth = 10;
    SourceFilesgroupLayout.marginHeight = 10;
    wSourceFiles.setLayout( SourceFilesgroupLayout );

    // Add filenames to result filenames...
    wlInputIsStream = new Label( wSourceFiles, SWT.RIGHT );
    wlInputIsStream.setText( BaseMessages.getString( PKG, "SFTPPUT.InputIsStream.Label" ) );
    props.setLook( wlInputIsStream );
    fdlInputIsStream = new FormData();
    fdlInputIsStream.left = new FormAttachment( 0, 0 );
    fdlInputIsStream.top = new FormAttachment( wStepname, margin );
    fdlInputIsStream.right = new FormAttachment( middle, -margin );
    wlInputIsStream.setLayoutData( fdlInputIsStream );
    wInputIsStream = new Button( wSourceFiles, SWT.CHECK );
    wInputIsStream.setToolTipText( BaseMessages.getString( PKG, "SFTPPUT.InputIsStream.Tooltip" ) );
    props.setLook( wInputIsStream );
    fdInputIsStream = new FormData();
    fdInputIsStream.left = new FormAttachment( middle, 0 );
    fdInputIsStream.top = new FormAttachment( wStepname, margin );
    fdInputIsStream.right = new FormAttachment( 100, 0 );
    wInputIsStream.setLayoutData( fdInputIsStream );
    wInputIsStream.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e ) {
        setInputStream();
        transMeta.setChanged();
      }
    } );

    // SourceFileNameField field
    wlSourceFileNameField = new Label( wSourceFiles, SWT.RIGHT );
    wlSourceFileNameField.setText( BaseMessages.getString( PKG, "SFTPPUTDialog.SourceFileNameField.Label" ) );
    props.setLook( wlSourceFileNameField );
    fdlSourceFileNameField = new FormData();
    fdlSourceFileNameField.left = new FormAttachment( 0, 0 );
    fdlSourceFileNameField.right = new FormAttachment( middle, -margin );
    fdlSourceFileNameField.top = new FormAttachment( wInputIsStream, margin );
    wlSourceFileNameField.setLayoutData( fdlSourceFileNameField );

    wSourceFileNameField = new CCombo( wSourceFiles, SWT.BORDER | SWT.READ_ONLY );
    props.setLook( wSourceFileNameField );
    wSourceFileNameField.setEditable( true );
    wSourceFileNameField.addModifyListener( lsMod );
    fdSourceFileNameField = new FormData();
    fdSourceFileNameField.left = new FormAttachment( middle, 0 );
    fdSourceFileNameField.top = new FormAttachment( wInputIsStream, margin );
    fdSourceFileNameField.right = new FormAttachment( 100, -margin );
    wSourceFileNameField.setLayoutData( fdSourceFileNameField );
    wSourceFileNameField.addFocusListener( new FocusListener() {
      public void focusLost( org.eclipse.swt.events.FocusEvent e ) {
      }

      public void focusGained( org.eclipse.swt.events.FocusEvent e ) {
        Cursor busy = new Cursor( shell.getDisplay(), SWT.CURSOR_WAIT );
        shell.setCursor( busy );
        getFields();
        shell.setCursor( null );
        busy.dispose();
      }
    } );

    // Add filenames to result filenames...
    wlAddFilenameToResult = new Label( wSourceFiles, SWT.RIGHT );
    wlAddFilenameToResult.setText( BaseMessages.getString( PKG, "SFTPPUT.AddfilenametoResult.Label" ) );
    props.setLook( wlAddFilenameToResult );
    fdlAddFilenameToResult = new FormData();
    fdlAddFilenameToResult.left = new FormAttachment( 0, 0 );
    fdlAddFilenameToResult.top = new FormAttachment( wSourceFileNameField, margin );
    fdlAddFilenameToResult.right = new FormAttachment( middle, -margin );
    wlAddFilenameToResult.setLayoutData( fdlAddFilenameToResult );
    wAddFilenameToResult = new Button( wSourceFiles, SWT.CHECK );
    wAddFilenameToResult.setToolTipText( BaseMessages.getString( PKG, "SFTPPUT.AddfilenametoResult.Tooltip" ) );
    props.setLook( wAddFilenameToResult );
    fdAddFilenameToResult = new FormData();
    fdAddFilenameToResult.left = new FormAttachment( middle, 0 );
    fdAddFilenameToResult.top = new FormAttachment( wSourceFileNameField, margin );
    fdAddFilenameToResult.right = new FormAttachment( 100, 0 );
    wAddFilenameToResult.setLayoutData( fdAddFilenameToResult );

    // After FTP Put
    wlAfterFTPPut = new Label( wSourceFiles, SWT.RIGHT );
    wlAfterFTPPut.setText( BaseMessages.getString( PKG, "SFTPPUT.AfterFTPPut.Label" ) );
    props.setLook( wlAfterFTPPut );
    fdlAfterFTPPut = new FormData();
    fdlAfterFTPPut.left = new FormAttachment( 0, 0 );
    fdlAfterFTPPut.right = new FormAttachment( middle, -margin );
    fdlAfterFTPPut.top = new FormAttachment( wAddFilenameToResult, 2 * margin );
    wlAfterFTPPut.setLayoutData( fdlAfterFTPPut );
    wAfterFTPPut = new CCombo( wSourceFiles, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER );
    wAfterFTPPut.add( BaseMessages.getString( PKG, "SFTPPUT.AfterSFTP.DoNothing.Label" ) );
    wAfterFTPPut.add( BaseMessages.getString( PKG, "SFTPPUT.AfterSFTP.Delete.Label" ) );
    wAfterFTPPut.add( BaseMessages.getString( PKG, "SFTPPUT.AfterSFTP.Move.Label" ) );
    wAfterFTPPut.select( 0 ); // +1: starts at -1
    props.setLook( wAfterFTPPut );
    fdAfterFTPPut = new FormData();
    fdAfterFTPPut.left = new FormAttachment( middle, 0 );
    fdAfterFTPPut.top = new FormAttachment( wAddFilenameToResult, 2 * margin );
    fdAfterFTPPut.right = new FormAttachment( 100, -margin );
    wAfterFTPPut.setLayoutData( fdAfterFTPPut );
    wAfterFTPPut.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e ) {
        AfterFTPPutActivate();

      }
    } );

    // moveTo Directory
    wlDestinationFolderFieldName = new Label( wSourceFiles, SWT.RIGHT );
    wlDestinationFolderFieldName.setText( BaseMessages.getString( PKG, "SFTPPUT.DestinationFolder.Label" ) );
    props.setLook( wlDestinationFolderFieldName );
    fdlDestinationFolderFieldName = new FormData();
    fdlDestinationFolderFieldName.left = new FormAttachment( 0, 0 );
    fdlDestinationFolderFieldName.top = new FormAttachment( wAfterFTPPut, margin );
    fdlDestinationFolderFieldName.right = new FormAttachment( middle, -margin );
    wlDestinationFolderFieldName.setLayoutData( fdlDestinationFolderFieldName );

    wDestinationFolderFieldName = new CCombo( wSourceFiles, SWT.BORDER | SWT.READ_ONLY );
    wDestinationFolderFieldName
      .setToolTipText( BaseMessages.getString( PKG, "SFTPPUT.DestinationFolder.Tooltip" ) );
    props.setLook( wDestinationFolderFieldName );
    wDestinationFolderFieldName.addModifyListener( lsMod );
    fdDestinationFolderFieldName = new FormData();
    fdDestinationFolderFieldName.left = new FormAttachment( middle, 0 );
    fdDestinationFolderFieldName.top = new FormAttachment( wAfterFTPPut, margin );
    fdDestinationFolderFieldName.right = new FormAttachment( 100, -margin );
    wDestinationFolderFieldName.setLayoutData( fdDestinationFolderFieldName );

    // Whenever something changes, set the tooltip to the expanded version:
    wDestinationFolderFieldName.addModifyListener( new ModifyListener() {
      public void modifyText( ModifyEvent e ) {
        wDestinationFolderFieldName.setToolTipText( transMeta.environmentSubstitute( wDestinationFolderFieldName
          .getText() ) );
      }
    } );

    wDestinationFolderFieldName.addFocusListener( new FocusListener() {
      public void focusLost( org.eclipse.swt.events.FocusEvent e ) {
      }

      public void focusGained( org.eclipse.swt.events.FocusEvent e ) {
        Cursor busy = new Cursor( shell.getDisplay(), SWT.CURSOR_WAIT );
        shell.setCursor( busy );
        getFields();
        shell.setCursor( null );
        busy.dispose();
      }
    } );

    // Create destination folder if necessary ...
    wlCreateDestinationFolder = new Label( wSourceFiles, SWT.RIGHT );
    wlCreateDestinationFolder.setText( BaseMessages.getString( PKG, "SFTPPUT.CreateDestinationFolder.Label" ) );
    props.setLook( wlCreateDestinationFolder );
    fdlCreateDestinationFolder = new FormData();
    fdlCreateDestinationFolder.left = new FormAttachment( 0, 0 );
    fdlCreateDestinationFolder.top = new FormAttachment( wDestinationFolderFieldName, margin );
    fdlCreateDestinationFolder.right = new FormAttachment( middle, -margin );
    wlCreateDestinationFolder.setLayoutData( fdlCreateDestinationFolder );
    wCreateDestinationFolder = new Button( wSourceFiles, SWT.CHECK );
    wCreateDestinationFolder.setToolTipText( BaseMessages.getString(
      PKG, "SFTPPUT.CreateDestinationFolder.Tooltip" ) );
    props.setLook( wCreateDestinationFolder );
    fdCreateDestinationFolder = new FormData();
    fdCreateDestinationFolder.left = new FormAttachment( middle, 0 );
    fdCreateDestinationFolder.top = new FormAttachment( wDestinationFolderFieldName, margin );
    fdCreateDestinationFolder.right = new FormAttachment( 100, 0 );
    wCreateDestinationFolder.setLayoutData( fdCreateDestinationFolder );

    fdSourceFiles = new FormData();
    fdSourceFiles.left = new FormAttachment( 0, margin );
    fdSourceFiles.top = new FormAttachment( wServerSettings, 2 * margin );
    fdSourceFiles.right = new FormAttachment( 100, -margin );
    wSourceFiles.setLayoutData( fdSourceFiles );
    // ///////////////////////////////////////////////////////////
    // / END OF Source files GROUP
    // ///////////////////////////////////////////////////////////

    // ////////////////////////
    // START OF Target files GROUP///
    // /
    wTargetFiles = new Group( wFilesComp, SWT.SHADOW_NONE );
    props.setLook( wTargetFiles );
    wTargetFiles.setText( BaseMessages.getString( PKG, "SFTPPUT.TargetFiles.Group.Label" ) );
    FormLayout TargetFilesgroupLayout = new FormLayout();
    TargetFilesgroupLayout.marginWidth = 10;
    TargetFilesgroupLayout.marginHeight = 10;
    wTargetFiles.setLayout( TargetFilesgroupLayout );

    // FtpDirectory line
    wlRemoteDirectory = new Label( wTargetFiles, SWT.RIGHT );
    wlRemoteDirectory.setText( BaseMessages.getString( PKG, "SFTPPUT.RemoteDir.Label" ) );
    props.setLook( wlRemoteDirectory );
    fdlRemoteDirectory = new FormData();
    fdlRemoteDirectory.left = new FormAttachment( 0, 0 );
    fdlRemoteDirectory.top = new FormAttachment( wSourceFiles, margin );
    fdlRemoteDirectory.right = new FormAttachment( middle, -margin );
    wlRemoteDirectory.setLayoutData( fdlRemoteDirectory );

    // Target (remote) folder
    wRemoteDirectory = new CCombo( wTargetFiles, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER );
    props.setLook( wRemoteDirectory );
    wRemoteDirectory.setToolTipText( BaseMessages.getString( PKG, "SFTPPUT.RemoteDir.Tooltip" ) );
    wRemoteDirectory.addModifyListener( lsMod );
    fdRemoteDirectory = new FormData();
    fdRemoteDirectory.left = new FormAttachment( middle, 0 );
    fdRemoteDirectory.top = new FormAttachment( wSourceFiles, margin );
    fdRemoteDirectory.right = new FormAttachment( 100, -margin );
    wRemoteDirectory.setLayoutData( fdRemoteDirectory );

    wRemoteDirectory.addFocusListener( new FocusListener() {
      public void focusLost( org.eclipse.swt.events.FocusEvent e ) {
      }

      public void focusGained( org.eclipse.swt.events.FocusEvent e ) {
        Cursor busy = new Cursor( shell.getDisplay(), SWT.CURSOR_WAIT );
        shell.setCursor( busy );
        getFields();
        shell.setCursor( null );
        busy.dispose();
      }
    } );

    // CreateRemoteFolder files after retrieval...
    wlCreateRemoteFolder = new Label( wTargetFiles, SWT.RIGHT );
    wlCreateRemoteFolder.setText( BaseMessages.getString( PKG, "SFTPPUT.CreateRemoteFolderFiles.Label" ) );
    props.setLook( wlCreateRemoteFolder );
    fdlCreateRemoteFolder = new FormData();
    fdlCreateRemoteFolder.left = new FormAttachment( 0, 0 );
    fdlCreateRemoteFolder.top = new FormAttachment( wRemoteDirectory, margin );
    fdlCreateRemoteFolder.right = new FormAttachment( middle, -margin );
    wlCreateRemoteFolder.setLayoutData( fdlCreateRemoteFolder );
    wCreateRemoteFolder = new Button( wTargetFiles, SWT.CHECK );
    props.setLook( wCreateRemoteFolder );
    fdCreateRemoteFolder = new FormData();
    wCreateRemoteFolder.setToolTipText( BaseMessages.getString( PKG, "SFTPPUT.CreateRemoteFolderFiles.Tooltip" ) );
    fdCreateRemoteFolder.left = new FormAttachment( middle, 0 );
    fdCreateRemoteFolder.top = new FormAttachment( wRemoteDirectory, margin );
    fdCreateRemoteFolder.right = new FormAttachment( 100, 0 );
    wCreateRemoteFolder.setLayoutData( fdCreateRemoteFolder );
    wCreateRemoteFolder.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e ) {
        transMeta.setChanged();
      }
    } );

    // Remote filename
    wlRemoteFileName = new Label( wTargetFiles, SWT.RIGHT );
    wlRemoteFileName.setText( BaseMessages.getString( PKG, "SFTPPUT.RemoteFilename.Label" ) );
    props.setLook( wlRemoteFileName );
    fdlRemoteFileName = new FormData();
    fdlRemoteFileName.left = new FormAttachment( 0, 0 );
    fdlRemoteFileName.top = new FormAttachment( wCreateRemoteFolder, margin );
    fdlRemoteFileName.right = new FormAttachment( middle, -margin );
    wlRemoteFileName.setLayoutData( fdlRemoteFileName );

    // Target (remote) folder
    wRemoteFileName = new CCombo( wTargetFiles, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER );
    props.setLook( wRemoteFileName );
    wRemoteFileName.setToolTipText( BaseMessages.getString( PKG, "SFTPPUT.RemoteFilename.Tooltip" ) );
    wRemoteFileName.addModifyListener( lsMod );
    fdRemoteFileName = new FormData();
    fdRemoteFileName.left = new FormAttachment( middle, 0 );
    fdRemoteFileName.top = new FormAttachment( wCreateRemoteFolder, margin );
    fdRemoteFileName.right = new FormAttachment( 100, -margin );
    wRemoteFileName.setLayoutData( fdRemoteFileName );

    wRemoteFileName.addFocusListener( new FocusListener() {
      public void focusLost( org.eclipse.swt.events.FocusEvent e ) {
      }

      public void focusGained( org.eclipse.swt.events.FocusEvent e ) {
        Cursor busy = new Cursor( shell.getDisplay(), SWT.CURSOR_WAIT );
        shell.setCursor( busy );
        getFields();
        shell.setCursor( null );
        busy.dispose();
      }
    } );

    fdTargetFiles = new FormData();
    fdTargetFiles.left = new FormAttachment( 0, margin );
    fdTargetFiles.top = new FormAttachment( wSourceFiles, margin );
    fdTargetFiles.right = new FormAttachment( 100, -margin );
    wTargetFiles.setLayoutData( fdTargetFiles );
    // ///////////////////////////////////////////////////////////
    // / END OF Target files GROUP
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

    fdTabFolder = new FormData();
    fdTabFolder.left = new FormAttachment( 0, 0 );
    fdTabFolder.top = new FormAttachment( wStepname, margin );
    fdTabFolder.right = new FormAttachment( 100, 0 );
    fdTabFolder.bottom = new FormAttachment( 100, -50 );
    wTabFolder.setLayoutData( fdTabFolder );

    // Some buttons
    wOK = new Button( shell, SWT.PUSH );
    wOK.setText( BaseMessages.getString( PKG, "System.Button.OK" ) );
    wCancel = new Button( shell, SWT.PUSH );
    wCancel.setText( BaseMessages.getString( PKG, "System.Button.Cancel" ) );

    setButtonPositions( new Button[] { wOK, wCancel }, margin, wTabFolder );

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
    lsTest = new Listener() {
      public void handleEvent( Event e ) {
        test();
      }
    };

    lsDef = new SelectionAdapter() {
      public void widgetDefaultSelected( SelectionEvent e ) {
        ok();
      }
    };
    wTest.addListener( SWT.Selection, lsTest );

    lsDef = new SelectionAdapter() {
      public void widgetDefaultSelected( SelectionEvent e ) {
        ok();
      }
    };

    lsDef = new SelectionAdapter() {
      public void widgetDefaultSelected( SelectionEvent e ) {
        ok();
      }
    };

    wStepname.addSelectionListener( lsDef );
    wServerName.addSelectionListener( lsDef );
    wUserName.addSelectionListener( lsDef );
    wPassword.addSelectionListener( lsDef );
    wRemoteDirectory.addSelectionListener( lsDef );
    wRemoteFileName.addSelectionListener( lsDef );

    // Detect X or ALT-F4 or something that kills this window...
    shell.addShellListener( new ShellAdapter() {
      public void shellClosed( ShellEvent e ) {
        cancel();
      }
    } );
    wTabFolder.setSelection( 0 );

    // Set the shell size, based upon previous time...
    setSize();

    getData();
    activeUseKey();
    AfterFTPPutActivate();
    setInputStream();
    input.setChanged( changed );

    shell.open();
    while ( !shell.isDisposed() ) {
      if ( !display.readAndDispatch() ) {
        display.sleep();
      }
    }
    return stepname;
  }

  /**
   * Copy information from the meta-data input to the dialog fields.
   */
  public void getData() {
    wServerName.setText( Const.NVL( input.getServerName(), "" ) );
    wServerPort.setText( Const.NVL( input.getServerPort(), "" ) );
    wUserName.setText( Const.NVL( input.getUserName(), "" ) );
    wPassword.setText( Const.NVL( input.getPassword(), "" ) );
    wRemoteDirectory.setText( Const.NVL( input.getRemoteDirectoryFieldName(), "" ) );
    wSourceFileNameField.setText( Const.NVL( input.getSourceFileFieldName(), "" ) );
    wInputIsStream.setSelection( input.isInputStream() );
    wAddFilenameToResult.setSelection( input.isAddFilenameResut() );
    wusePublicKey.setSelection( input.isUseKeyFile() );
    wKeyFilename.setText( Const.NVL( input.getKeyFilename(), "" ) );
    wkeyfilePass.setText( Const.NVL( input.getKeyPassPhrase(), "" ) );
    wCompression.setText( Const.NVL( input.getCompression(), "none" ) );

    wProxyType.setText( Const.NVL( input.getProxyType(), "" ) );
    wProxyHost.setText( Const.NVL( input.getProxyHost(), "" ) );
    wProxyPort.setText( Const.NVL( input.getProxyPort(), "" ) );
    wProxyUsername.setText( Const.NVL( input.getProxyUsername(), "" ) );
    wProxyPassword.setText( Const.NVL( input.getProxyPassword(), "" ) );
    wCreateRemoteFolder.setSelection( input.isCreateRemoteFolder() );

    wAfterFTPPut.setText( JobEntrySFTPPUT.getAfterSFTPPutDesc( input.getAfterFTPS() ) );
    wDestinationFolderFieldName.setText( Const.NVL( input.getDestinationFolderFieldName(), "" ) );
    wCreateDestinationFolder.setSelection( input.isCreateDestinationFolder() );
    wRemoteFileName.setText( Const.NVL( input.getRemoteFilenameFieldName(), "" ) );

    wStepname.selectAll();
    wStepname.setFocus();
  }

  private void cancel() {
    // Close open connections
    closeFTPConnections();
    stepname = null;
    input.setChanged( changed );
    dispose();
  }

  private void ok() {
    if ( Utils.isEmpty( wStepname.getText() ) ) {
      return;
    }

    stepname = wStepname.getText(); // return value

    input.setServerName( wServerName.getText() );
    input.setServerPort( wServerPort.getText() );
    input.setUserName( wUserName.getText() );
    input.setPassword( wPassword.getText() );
    input.setRemoteDirectoryFieldName( wRemoteDirectory.getText() );
    input.setSourceFileFieldName( wSourceFileNameField.getText() );
    input.setAddFilenameResut( wAddFilenameToResult.getSelection() );
    input.setUseKeyFile( wusePublicKey.getSelection() );
    input.setKeyFilename( wKeyFilename.getText() );
    input.setKeyPassPhrase( wkeyfilePass.getText() );
    input.setCompression( wCompression.getText() );

    input.setProxyType( wProxyType.getText() );
    input.setProxyHost( wProxyHost.getText() );
    input.setProxyPort( wProxyPort.getText() );
    input.setProxyUsername( wProxyUsername.getText() );
    input.setProxyPassword( wProxyPassword.getText() );
    input.setCreateRemoteFolder( wCreateRemoteFolder.getSelection() );
    input.setAfterFTPS( JobEntrySFTPPUT.getAfterSFTPPutByDesc( wAfterFTPPut.getText() ) );
    input.setCreateDestinationFolder( wCreateDestinationFolder.getSelection() );
    input.setDestinationFolderFieldName( wDestinationFolderFieldName.getText() );
    input.setInputStream( wInputIsStream.getSelection() );
    input.setRemoteFilenameFieldName( wRemoteFileName.getText() );

    dispose();
  }

  private void setDefaulProxyPort() {
    if ( wProxyType.getText().equals( SFTPClient.PROXY_TYPE_HTTP ) ) {
      if ( Utils.isEmpty( wProxyPort.getText() )
        || ( !Utils.isEmpty( wProxyPort.getText() ) && wProxyPort.getText().equals(
          SFTPClient.SOCKS5_DEFAULT_PORT ) ) ) {
        wProxyPort.setText( SFTPClient.HTTP_DEFAULT_PORT );
      }
    } else {
      if ( Utils.isEmpty( wProxyPort.getText() )
        || ( !Utils.isEmpty( wProxyPort.getText() ) && wProxyPort
          .getText().equals( SFTPClient.HTTP_DEFAULT_PORT ) ) ) {
        wProxyPort.setText( SFTPClient.SOCKS5_DEFAULT_PORT );
      }
    }
  }

  private void AfterFTPPutActivate() {
    boolean moveFile =
      JobEntrySFTPPUT.getAfterSFTPPutByDesc( wAfterFTPPut.getText() ) == JobEntrySFTPPUT.AFTER_FTPSPUT_MOVE;
    boolean doNothing =
      JobEntrySFTPPUT.getAfterSFTPPutByDesc( wAfterFTPPut.getText() ) == JobEntrySFTPPUT.AFTER_FTPSPUT_NOTHING;

    wlDestinationFolderFieldName.setEnabled( moveFile );
    wDestinationFolderFieldName.setEnabled( moveFile );
    wlCreateDestinationFolder.setEnabled( moveFile );
    wCreateDestinationFolder.setEnabled( moveFile );
    wlAddFilenameToResult.setEnabled( doNothing );
    wAddFilenameToResult.setEnabled( doNothing );

  }

  private void activeUseKey() {
    wlKeyFilename.setEnabled( wusePublicKey.getSelection() );
    wKeyFilename.setEnabled( wusePublicKey.getSelection() );
    wbKeyFilename.setEnabled( wusePublicKey.getSelection() );
    wkeyfilePass.setEnabled( wusePublicKey.getSelection() );
  }

  private void test() {

    if ( connectToSFTP( false, null ) ) {
      MessageBox mb = new MessageBox( shell, SWT.OK | SWT.ICON_INFORMATION );
      mb.setMessage( BaseMessages.getString( PKG, "SFTPPUT.Connected.OK", wServerName.getText() ) + Const.CR );
      mb.setText( BaseMessages.getString( PKG, "SFTPPUT.Connected.Title.Ok" ) );
      mb.open();
    } else {
      MessageBox mb = new MessageBox( shell, SWT.OK | SWT.ICON_ERROR );
      mb.setMessage( BaseMessages.getString( PKG, "SFTPPUT.Connected.NOK.ConnectionBad", wServerName.getText() )
        + Const.CR );
      mb.setText( BaseMessages.getString( PKG, "SFTPPUT.Connected.Title.Bad" ) );
      mb.open();
    }

  }

  @VisibleForTesting
  boolean connectToSFTP( boolean checkFolder, String Remotefoldername ) {
    boolean retval = true;
    try {
      if ( sftpclient == null || input.hasChanged() ) {
        sftpclient = createSFTPClient();
      }
      if ( checkFolder ) {
        retval = sftpclient.folderExists( Remotefoldername );
      }

    } catch ( Exception e ) {
      MessageBox mb = new MessageBox( shell, SWT.OK | SWT.ICON_ERROR );
      mb.setMessage( BaseMessages.getString( PKG, "SFTPPUT.ErrorConnect.NOK", wServerName.getText(), e
        .getMessage() )
        + Const.CR );
      mb.setText( BaseMessages.getString( PKG, "SFTPPUT.ErrorConnect.Title.Bad" ) );
      mb.open();
      retval = false;
    }
    return retval;
  }

  SFTPClient createSFTPClient() throws UnknownHostException, KettleJobException {
    // Create sftp client to host ...
    sftpclient =
      new SFTPClient(
        InetAddress.getByName(
          transMeta.environmentSubstitute( wServerName.getText() ) ),
        Const.toInt( transMeta.environmentSubstitute( wServerPort.getText() ), 22 ),
        transMeta.environmentSubstitute( wUserName.getText() ),
        transMeta.environmentSubstitute( wKeyFilename.getText() ),
        transMeta.environmentSubstitute( wkeyfilePass.getText() ) );
    // Set proxy?
    String realProxyHost = transMeta.environmentSubstitute( wProxyHost.getText() );
    if ( !Utils.isEmpty( realProxyHost ) ) {
      // Set proxy
      sftpclient.setProxy(
        realProxyHost,
        transMeta.environmentSubstitute( wProxyPort.getText() ),
        transMeta.environmentSubstitute( wProxyUsername.getText() ),
        Utils.resolvePassword( transMeta,  wProxyPassword.getText() ),
        wProxyType.getText() );
    }
    // login to ftp host ...
    sftpclient.login( Utils.resolvePassword( transMeta, wPassword.getText() ) );

    return sftpclient;
  }

  private void getFields() {
    if ( !gotPreviousFields ) {
      gotPreviousFields = true;
      try {
        String source = wSourceFileNameField.getText();
        String rep = wRemoteDirectory.getText();
        String after = wDestinationFolderFieldName.getText();
        String remote = wRemoteFileName.getText();

        wSourceFileNameField.removeAll();
        wRemoteDirectory.removeAll();
        wDestinationFolderFieldName.removeAll();
        wRemoteFileName.removeAll();
        RowMetaInterface r = transMeta.getPrevStepFields( stepname );
        if ( r != null ) {
          String[] fields = r.getFieldNames();
          wSourceFileNameField.setItems( fields );
          wRemoteDirectory.setItems( fields );
          wDestinationFolderFieldName.setItems( fields );
          wRemoteFileName.setItems( fields );

          if ( source != null ) {
            wSourceFileNameField.setText( source );
          }
          if ( rep != null ) {
            wRemoteDirectory.setText( rep );
          }
          if ( after != null ) {
            wDestinationFolderFieldName.setText( after );
          }
          if ( remote != null ) {
            wRemoteFileName.setText( remote );
          }
        }
      } catch ( KettleException ke ) {
        new ErrorDialog(
          shell, BaseMessages.getString( PKG, "SFTPPUTDialog.FailedToGetFields.DialogTitle" ), BaseMessages
            .getString( PKG, "SFTPPUTDialog.FailedToGetFields.DialogMessage" ), ke );
      }
    }
  }

  private void closeFTPConnections() {
    // Close SecureFTP connection if necessary
    if ( sftpclient != null ) {
      try {
        sftpclient.disconnect();
        sftpclient = null;
      } catch ( Exception e ) {
        // Ignore errors
      }
    }
  }

  private void setInputStream() {
    wAddFilenameToResult.setEnabled( !wInputIsStream.getSelection() );
    wlAddFilenameToResult.setEnabled( !wInputIsStream.getSelection() );
    if ( wInputIsStream.getSelection() ) {
      wAddFilenameToResult.setSelection( false );
    }
    wlAfterFTPPut.setEnabled( !wInputIsStream.getSelection() );
    wAfterFTPPut.setEnabled( !wInputIsStream.getSelection() );
    wDestinationFolderFieldName.setEnabled( !wInputIsStream.getSelection() );
    wlDestinationFolderFieldName.setEnabled( !wInputIsStream.getSelection() );
    wlCreateDestinationFolder.setEnabled( !wInputIsStream.getSelection() );
    wCreateDestinationFolder.setEnabled( !wInputIsStream.getSelection() );
  }
}
