// CHECKSTYLE:FileLength:OFF
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



package org.pentaho.di.ui.trans.steps.mail;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.FocusListener;
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
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.annotations.PluginDialog;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.steps.mail.MailMeta;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.events.dialog.FilterType;
import org.pentaho.di.ui.core.events.dialog.SelectionAdapterFileDialogTextVar;
import org.pentaho.di.ui.core.events.dialog.SelectionAdapterOptions;
import org.pentaho.di.ui.core.events.dialog.SelectionOperation;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.LabelText;
import org.pentaho.di.ui.core.widget.LabelTextVar;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

import java.nio.charset.Charset;
import java.security.SecureRandom;
import java.util.ArrayList;

/**
 * Send mail step. based on Mail job entry
 *
 * @author Samatar
 * @since 28-07-2008
 */

@PluginDialog( id = "Mail", image = "MAIL.svg", pluginType = PluginDialog.PluginType.STEP,
        documentationUrl = "http://wiki.pentaho.com/display/EAI/Mail+%28step%29" )
public class MailDialog extends BaseStepDialog implements StepDialogInterface {
  private static Class<?> PKG = MailMeta.class; // for i18n purposes, needed by Translator2!!

  private static final String[] IMAGES_FILE_TYPES = new String[] {
    BaseMessages.getString( PKG, "MailDialog.Filetype.Png" ),
    BaseMessages.getString( PKG, "MailDialog.Filetype.Jpeg" ),
    BaseMessages.getString( PKG, "MailDialog.Filetype.Gif" ),
    BaseMessages.getString( PKG, "MailDialog.Filetype.All" ) };

  private boolean gotEncodings = false;

  private Group wOriginFiles;
  private Group wZipGroup;

  private Button wIsFileDynamic;
  private Button wIsAttachContentField;

  private Label wlDynamicFilenameField;
  private Label wlAttachContentField;
  private Label wlAttachContentFileNameField;
  private CCombo wDynamicFilenameField;
  private CCombo wAttachContentField;
  private CCombo wAttachContentFileNameField;

  private Label wlDynamicWildcardField;
  private CCombo wDynamicWildcardField;

  private Label wlIsZipFileDynamic;

  private CCombo wReplyToAddresses;

  private LabelText wName;

  private CCombo wDestination;

  private CCombo wDestinationCc;
  private CCombo wDestinationBcc;

  private CCombo wServer;

  private CCombo wPort;

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

  private Label wlUseSecAuth;

  private Button wUseSecAuth;

  private CCombo wAuthUser;

  private Label wlAuthUser;

  private CCombo wAuthPass;

  private Label wlAuthPass;

  private CCombo wReply;
  private CCombo wReplyName;

  private CCombo wSubject;

  private Button wAddDate;

  private CCombo wPerson;

  private Label wlWildcard;

  private TextVar wWildcard;

  private CCombo wPhone;

  private CCombo wComment;

  private Label wlSourceFileFolderName;
  private Button wbFileFolderName;
  private Button wbSourceFolder;
  private TextVar wSourceFileFolderName;

  private Button wIncludeSubFolders;

  private Button wOnlyComment;
  private Button wUseHTML;
  private Button wUsePriority;

  private Label wlEncoding;
  private CCombo wEncoding;

  private Label wlSecureConnectionType;
  private CCombo wSecureConnectionType;

  private Label wlPriority;
  private CCombo wPriority;

  private Label wlImportance;
  private CCombo wImportance;

  private Label wlSensitivity;
  private CCombo wSensitivity;

  private Label wlDynamicZipFileField;

  private CCombo wDynamicZipFileField;

  private Button wisZipFileDynamic;

  private Button wZipFiles;

  private LabelTextVar wZipFilename;

  private LabelTextVar wZipSizeCondition;

  private Label wlImageFilename;
  private Label wlContentID;
  private Label wlFields;
  private Button wbImageFilename;
  private Button wbAddImageFilename;
  private Button wbDeleteImageFilename;
  private Button wbEditImageFilename;
  private TextVar wImageFilename;
  private TextVar wContentID;
  private TableView wFields;

  private boolean getPreviousFields = false;

  private MailMeta input;

  private final SecureRandom secureRandom = new SecureRandom();

  public MailDialog( Shell parent, Object in, TransMeta tr, String sName ) {
    super( parent, (BaseStepMeta) in, tr, sName );
    input = (MailMeta) in;
  }

  public String open() {
    Shell parent = getParent();
    Display display = parent.getDisplay();

    shell = new Shell( parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MIN | SWT.MAX );
    props.setLook( shell );
    setShellImage( shell, input );

    ModifyListener lsMod = e -> input.setChanged();
    changed = input.hasChanged();

    FormLayout formLayout = new FormLayout();
    formLayout.marginWidth = Const.FORM_MARGIN;
    formLayout.marginHeight = Const.FORM_MARGIN;

    shell.setLayout( formLayout );
    shell.setText( BaseMessages.getString( PKG, "MailDialog.Shell.Title" ) );

    int middle = props.getMiddlePct();
    int margin = Const.MARGIN;

    // Stepname line
    wlStepname = new Label( shell, SWT.RIGHT );
    wlStepname.setText( BaseMessages.getString( PKG, "MailDialog.Stepname.Label" ) );
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
    wGeneralTab.setText( BaseMessages.getString( PKG, "Mail.Tab.General.Label" ) );

    Composite wGeneralComp = new Composite( wTabFolder, SWT.NONE );
    props.setLook( wGeneralComp );

    FormLayout generalLayout = new FormLayout();
    generalLayout.marginWidth = 3;
    generalLayout.marginHeight = 3;
    wGeneralComp.setLayout( generalLayout );

    // ////////////////////////
    // START OF Destination Settings GROUP
    // ////////////////////////

    Group wDestinationGroup = new Group( wGeneralComp, SWT.SHADOW_NONE );
    props.setLook( wDestinationGroup );
    wDestinationGroup.setText( BaseMessages.getString( PKG, "Mail.Group.DestinationAddress.Label" ) );

    FormLayout destinationGroupLayout = new FormLayout();
    destinationGroupLayout.marginWidth = 10;
    destinationGroupLayout.marginHeight = 10;
    wDestinationGroup.setLayout( destinationGroupLayout );

    // Destination
    Label wlDestination = new Label( wDestinationGroup, SWT.RIGHT );
    wlDestination.setText( BaseMessages.getString( PKG, "Mail.DestinationAddress.Label" ) );
    props.setLook( wlDestination );
    FormData fdlDestination = new FormData();
    fdlDestination.left = new FormAttachment( 0, -margin );
    fdlDestination.top = new FormAttachment( wStepname, margin );
    fdlDestination.right = new FormAttachment( middle, -2 * margin );
    wlDestination.setLayoutData( fdlDestination );

    wDestination = new CCombo( wDestinationGroup, SWT.BORDER | SWT.READ_ONLY );
    wDestination.setEditable( true );
    props.setLook( wDestination );
    wDestination.addModifyListener( lsMod );
    FormData fdDestination = new FormData();
    fdDestination.left = new FormAttachment( middle, -margin );
    fdDestination.top = new FormAttachment( wStepname, margin );
    fdDestination.right = new FormAttachment( 100, -margin );
    wDestination.setLayoutData( fdDestination );
    wDestination.addFocusListener( new FocusListener() {
      public void focusLost( org.eclipse.swt.events.FocusEvent e ) {
      }

      public void focusGained( org.eclipse.swt.events.FocusEvent e ) {
        Cursor busy = new Cursor( shell.getDisplay(), SWT.CURSOR_WAIT );
        shell.setCursor( busy );
        getPreviousFields();
        shell.setCursor( null );
        busy.dispose();
      }
    } );

    // DestinationCcCc
    Label wlDestinationCc = new Label( wDestinationGroup, SWT.RIGHT );
    wlDestinationCc.setText( BaseMessages.getString( PKG, "Mail.DestinationAddressCc.Label" ) );
    props.setLook( wlDestinationCc );
    FormData fdlDestinationCc = new FormData();
    fdlDestinationCc.left = new FormAttachment( 0, -margin );
    fdlDestinationCc.top = new FormAttachment( wDestination, margin );
    fdlDestinationCc.right = new FormAttachment( middle, -2 * margin );
    wlDestinationCc.setLayoutData( fdlDestinationCc );

    wDestinationCc = new CCombo( wDestinationGroup, SWT.BORDER | SWT.READ_ONLY );
    wDestinationCc.setEditable( true );
    props.setLook( wDestinationCc );
    wDestinationCc.addModifyListener( lsMod );
    FormData fdDestinationCc = new FormData();
    fdDestinationCc.left = new FormAttachment( middle, -margin );
    fdDestinationCc.top = new FormAttachment( wDestination, margin );
    fdDestinationCc.right = new FormAttachment( 100, -margin );
    wDestinationCc.setLayoutData( fdDestinationCc );
    wDestinationCc.addFocusListener( new FocusListener() {
      public void focusLost( org.eclipse.swt.events.FocusEvent e ) {
      }

      public void focusGained( org.eclipse.swt.events.FocusEvent e ) {
        Cursor busy = new Cursor( shell.getDisplay(), SWT.CURSOR_WAIT );
        shell.setCursor( busy );
        getPreviousFields();
        shell.setCursor( null );
        busy.dispose();
      }
    } );
    // DestinationBCc
    Label wlDestinationBCc = new Label( wDestinationGroup, SWT.RIGHT );
    wlDestinationBCc.setText( BaseMessages.getString( PKG, "Mail.DestinationAddressBCc.Label" ) );
    props.setLook( wlDestinationBCc );
    FormData fdlDestinationBCc = new FormData();
    fdlDestinationBCc.left = new FormAttachment( 0, -margin );
    fdlDestinationBCc.top = new FormAttachment( wDestinationCc, margin );
    fdlDestinationBCc.right = new FormAttachment( middle, -2 * margin );
    wlDestinationBCc.setLayoutData( fdlDestinationBCc );

    wDestinationBcc = new CCombo( wDestinationGroup, SWT.BORDER | SWT.READ_ONLY );
    wDestinationBcc.setEditable( true );
    props.setLook( wDestinationBcc );
    wDestinationBcc.addModifyListener( lsMod );
    FormData fdDestinationBCc = new FormData();
    fdDestinationBCc.left = new FormAttachment( middle, -margin );
    fdDestinationBCc.top = new FormAttachment( wDestinationCc, margin );
    fdDestinationBCc.right = new FormAttachment( 100, -margin );
    wDestinationBcc.setLayoutData( fdDestinationBCc );
    wDestinationBcc.addFocusListener( new FocusListener() {
      public void focusLost( org.eclipse.swt.events.FocusEvent e ) {
      }

      public void focusGained( org.eclipse.swt.events.FocusEvent e ) {
        Cursor busy = new Cursor( shell.getDisplay(), SWT.CURSOR_WAIT );
        shell.setCursor( busy );
        getPreviousFields();
        shell.setCursor( null );
        busy.dispose();
      }
    } );

    FormData fdDestinationGroup = new FormData();
    fdDestinationGroup.left = new FormAttachment( 0, margin );
    fdDestinationGroup.top = new FormAttachment( wName, margin );
    fdDestinationGroup.right = new FormAttachment( 100, -margin );
    wDestinationGroup.setLayoutData( fdDestinationGroup );

    // ///////////////////////////////////////////////////////////
    // / END OF DESTINATION ADDRESS GROUP
    // ///////////////////////////////////////////////////////////

    // ////////////////////////
    // START OF Reply Settings GROUP
    // ////////////////////////

    Group wReplyGroup = new Group( wGeneralComp, SWT.SHADOW_NONE );
    props.setLook( wReplyGroup );
    wReplyGroup.setText( BaseMessages.getString( PKG, "MailDialog.Group.Reply.Label" ) );

    FormLayout replyGroupLayout = new FormLayout();
    replyGroupLayout.marginWidth = 10;
    replyGroupLayout.marginHeight = 10;
    wReplyGroup.setLayout( replyGroupLayout );

    // ReplyName
    Label wlReplyName = new Label( wReplyGroup, SWT.RIGHT );
    wlReplyName.setText( BaseMessages.getString( PKG, "Mail.ReplyName.Label" ) );
    props.setLook( wlReplyName );
    FormData fdlReplyName = new FormData();
    fdlReplyName.left = new FormAttachment( 0, -margin );
    fdlReplyName.top = new FormAttachment( wDestinationGroup, margin );
    fdlReplyName.right = new FormAttachment( middle, -2 * margin );
    wlReplyName.setLayoutData( fdlReplyName );

    wReplyName = new CCombo( wReplyGroup, SWT.BORDER | SWT.READ_ONLY );
    wReplyName.setEditable( true );
    props.setLook( wReplyName );
    wReplyName.addModifyListener( lsMod );
    FormData fdReplyName = new FormData();
    fdReplyName.left = new FormAttachment( middle, -margin );
    fdReplyName.top = new FormAttachment( wDestinationGroup, margin );
    fdReplyName.right = new FormAttachment( 100, -margin );
    wReplyName.setLayoutData( fdReplyName );
    wReplyName.addFocusListener( new FocusListener() {
      public void focusLost( org.eclipse.swt.events.FocusEvent e ) {
      }

      public void focusGained( org.eclipse.swt.events.FocusEvent e ) {
        Cursor busy = new Cursor( shell.getDisplay(), SWT.CURSOR_WAIT );
        shell.setCursor( busy );
        getPreviousFields();
        shell.setCursor( null );
        busy.dispose();
      }
    } );

    // Reply
    Label wlReply = new Label( wReplyGroup, SWT.RIGHT );
    wlReply.setText( BaseMessages.getString( PKG, "Mail.ReplyAddress.Label" ) );
    props.setLook( wlReply );
    FormData fdlReply = new FormData();
    fdlReply.left = new FormAttachment( 0, -margin );
    fdlReply.top = new FormAttachment( wReplyName, margin );
    fdlReply.right = new FormAttachment( middle, -2 * margin );
    wlReply.setLayoutData( fdlReply );

    wReply = new CCombo( wReplyGroup, SWT.BORDER | SWT.READ_ONLY );
    wReply.setEditable( true );
    props.setLook( wReply );
    wReply.addModifyListener( lsMod );
    FormData fdReply = new FormData();
    fdReply.left = new FormAttachment( middle, -margin );
    fdReply.top = new FormAttachment( wReplyName, margin );
    fdReply.right = new FormAttachment( 100, -margin );
    wReply.setLayoutData( fdReply );
    wReply.addFocusListener( new FocusListener() {
      public void focusLost( org.eclipse.swt.events.FocusEvent e ) {
      }

      public void focusGained( org.eclipse.swt.events.FocusEvent e ) {
        Cursor busy = new Cursor( shell.getDisplay(), SWT.CURSOR_WAIT );
        shell.setCursor( busy );
        getPreviousFields();
        shell.setCursor( null );
        busy.dispose();
      }
    } );
    FormData fdReplyGroup = new FormData();
    fdReplyGroup.left = new FormAttachment( 0, margin );
    fdReplyGroup.top = new FormAttachment( wDestinationGroup, margin );
    fdReplyGroup.right = new FormAttachment( 100, -margin );
    wReplyGroup.setLayoutData( fdReplyGroup );

    // ///////////////////////////////////////////////////////////
    // / END OF Reply GROUP
    // ///////////////////////////////////////////////////////////

    // Reply to addresses
    Label wlReplyToAddresses = new Label( wGeneralComp, SWT.RIGHT );
    wlReplyToAddresses.setText( BaseMessages.getString( PKG, "MailDialog.ReplyToAddresses.Label" ) );
    props.setLook( wlReplyToAddresses );
    FormData fdlReplyToAddresses = new FormData();
    fdlReplyToAddresses.left = new FormAttachment( 0, -margin );
    fdlReplyToAddresses.top = new FormAttachment( wReplyGroup, 2 * margin );
    fdlReplyToAddresses.right = new FormAttachment( middle, -2 * margin );
    wlReplyToAddresses.setLayoutData( fdlReplyToAddresses );

    wReplyToAddresses = new CCombo( wGeneralComp, SWT.BORDER | SWT.READ_ONLY );
    wReplyToAddresses.setEditable( true );
    props.setLook( wReplyToAddresses );
    wReplyToAddresses.addModifyListener( lsMod );
    FormData fdReplyToAddresses = new FormData();
    fdReplyToAddresses.left = new FormAttachment( middle, -margin );
    fdReplyToAddresses.top = new FormAttachment( wReplyGroup, 2 * margin );
    fdReplyToAddresses.right = new FormAttachment( 100, -margin );
    wReplyToAddresses.setLayoutData( fdReplyToAddresses );
    wReplyToAddresses.addFocusListener( new FocusListener() {
      public void focusLost( org.eclipse.swt.events.FocusEvent e ) {
      }

      public void focusGained( org.eclipse.swt.events.FocusEvent e ) {
        Cursor busy = new Cursor( shell.getDisplay(), SWT.CURSOR_WAIT );
        shell.setCursor( busy );
        getPreviousFields();
        shell.setCursor( null );
        busy.dispose();
      }
    } );

    // Person
    Label wlPerson = new Label( wGeneralComp, SWT.RIGHT );
    wlPerson.setText( BaseMessages.getString( PKG, "Mail.Contact.Label" ) );
    props.setLook( wlPerson );
    FormData fdlPerson = new FormData();
    fdlPerson.left = new FormAttachment( 0, -margin );
    fdlPerson.top = new FormAttachment( wReplyToAddresses, 2 * margin );
    fdlPerson.right = new FormAttachment( middle, -2 * margin );
    wlPerson.setLayoutData( fdlPerson );

    wPerson = new CCombo( wGeneralComp, SWT.BORDER | SWT.READ_ONLY );
    wPerson.setEditable( true );
    props.setLook( wPerson );
    wPerson.addModifyListener( lsMod );
    FormData fdPerson = new FormData();
    fdPerson.left = new FormAttachment( middle, -margin );
    fdPerson.top = new FormAttachment( wReplyToAddresses, 2 * margin );
    fdPerson.right = new FormAttachment( 100, -margin );
    wPerson.setLayoutData( fdPerson );
    wPerson.addFocusListener( new FocusListener() {
      public void focusLost( org.eclipse.swt.events.FocusEvent e ) {
      }

      public void focusGained( org.eclipse.swt.events.FocusEvent e ) {
        Cursor busy = new Cursor( shell.getDisplay(), SWT.CURSOR_WAIT );
        shell.setCursor( busy );
        getPreviousFields();
        shell.setCursor( null );
        busy.dispose();
      }
    } );

    // Phone line
    Label wlPhone = new Label( wGeneralComp, SWT.RIGHT );
    wlPhone.setText( BaseMessages.getString( PKG, "Mail.ContactPhone.Label" ) );
    props.setLook( wlPhone );
    FormData fdlPhone = new FormData();
    fdlPhone.left = new FormAttachment( 0, -margin );
    fdlPhone.top = new FormAttachment( wPerson, margin );
    fdlPhone.right = new FormAttachment( middle, -2 * margin );
    wlPhone.setLayoutData( fdlPhone );

    wPhone = new CCombo( wGeneralComp, SWT.BORDER | SWT.READ_ONLY );
    wPhone.setEditable( true );
    props.setLook( wPhone );
    wPhone.addModifyListener( lsMod );
    FormData fdPhone = new FormData();
    fdPhone.left = new FormAttachment( middle, -margin );
    fdPhone.top = new FormAttachment( wPerson, margin );
    fdPhone.right = new FormAttachment( 100, -margin );
    wPhone.setLayoutData( fdPhone );
    wPhone.addFocusListener( new FocusListener() {
      public void focusLost( org.eclipse.swt.events.FocusEvent e ) {
      }

      public void focusGained( org.eclipse.swt.events.FocusEvent e ) {
        Cursor busy = new Cursor( shell.getDisplay(), SWT.CURSOR_WAIT );
        shell.setCursor( busy );
        getPreviousFields();
        shell.setCursor( null );
        busy.dispose();
      }
    } );

    FormData fdGeneralComp = new FormData();
    fdGeneralComp.left = new FormAttachment( 0, 0 );
    fdGeneralComp.top = new FormAttachment( 0, 0 );
    fdGeneralComp.right = new FormAttachment( 100, 0 );
    fdGeneralComp.bottom = new FormAttachment( 500, -margin );
    wGeneralComp.setLayoutData( fdGeneralComp );

    wGeneralComp.layout();
    wGeneralTab.setControl( wGeneralComp );
    props.setLook( wGeneralComp );

    // ///////////////////////////////////////////////////////////
    // / END OF GENERAL TAB
    // ///////////////////////////////////////////////////////////

    // ////////////////////////////////////
    // START OF SERVER TAB ///
    // ///////////////////////////////////

    CTabItem wContentTab = new CTabItem( wTabFolder, SWT.NONE );
    wContentTab.setText( BaseMessages.getString( PKG, "MailDialog.Server.Label" ) );

    FormLayout contentLayout = new FormLayout();
    contentLayout.marginWidth = 3;
    contentLayout.marginHeight = 3;

    Composite wContentComp = new Composite( wTabFolder, SWT.NONE );
    props.setLook( wContentComp );
    wContentComp.setLayout( contentLayout );

    // ////////////////////////
    // START OF SERVER GROUP
    // /////////////////////////

    Group wServerGroup = new Group( wContentComp, SWT.SHADOW_NONE );
    props.setLook( wServerGroup );
    wServerGroup.setText( BaseMessages.getString( PKG, "Mail.Group.SMTPServer.Label" ) );

    FormLayout serverGroupLayout = new FormLayout();
    serverGroupLayout.marginWidth = 10;
    serverGroupLayout.marginHeight = 10;
    wServerGroup.setLayout( serverGroupLayout );

    // Server
    Label wlServer = new Label( wServerGroup, SWT.RIGHT );
    wlServer.setText( BaseMessages.getString( PKG, "Mail.SMTPServer.Label" ) );
    props.setLook( wlServer );
    FormData fdlServer = new FormData();
    fdlServer.left = new FormAttachment( 0, -margin );
    fdlServer.top = new FormAttachment( 0, margin );
    fdlServer.right = new FormAttachment( middle, -2 * margin );
    wlServer.setLayoutData( fdlServer );

    wServer = new CCombo( wServerGroup, SWT.BORDER | SWT.READ_ONLY );
    wServer.setEditable( true );
    props.setLook( wServer );
    wServer.addModifyListener( lsMod );
    FormData fdServer = new FormData();
    fdServer.left = new FormAttachment( middle, -margin );
    fdServer.top = new FormAttachment( 0, margin );
    fdServer.right = new FormAttachment( 100, -margin );
    wServer.setLayoutData( fdServer );
    wServer.addFocusListener( new FocusListener() {
      public void focusLost( org.eclipse.swt.events.FocusEvent e ) {
      }

      public void focusGained( org.eclipse.swt.events.FocusEvent e ) {
        Cursor busy = new Cursor( shell.getDisplay(), SWT.CURSOR_WAIT );
        shell.setCursor( busy );
        getPreviousFields();
        shell.setCursor( null );
        busy.dispose();
      }
    } );

    // Port
    Label wlPort = new Label( wServerGroup, SWT.RIGHT );
    wlPort.setText( BaseMessages.getString( PKG, "Mail.Port.Label" ) );
    props.setLook( wlPort );
    FormData fdlPort = new FormData();
    fdlPort.left = new FormAttachment( 0, -margin );
    fdlPort.top = new FormAttachment( wServer, margin );
    fdlPort.right = new FormAttachment( middle, -2 * margin );
    wlPort.setLayoutData( fdlPort );

    wPort = new CCombo( wServerGroup, SWT.BORDER | SWT.READ_ONLY );
    wPort.setEditable( true );
    props.setLook( wPort );
    wPort.addModifyListener( lsMod );
    FormData fdPort = new FormData();
    fdPort.left = new FormAttachment( middle, -margin );
    fdPort.top = new FormAttachment( wServer, margin );
    fdPort.right = new FormAttachment( 100, -margin );
    wPort.setLayoutData( fdPort );
    wPort.addFocusListener( new FocusListener() {
      public void focusLost( org.eclipse.swt.events.FocusEvent e ) {
      }

      public void focusGained( org.eclipse.swt.events.FocusEvent e ) {
        Cursor busy = new Cursor( shell.getDisplay(), SWT.CURSOR_WAIT );
        shell.setCursor( busy );
        getPreviousFields();
        shell.setCursor( null );
        busy.dispose();
      }
    } );

    FormData fdServerGroup = new FormData();
    fdServerGroup.left = new FormAttachment( 0, margin );
    fdServerGroup.top = new FormAttachment( wName, margin );
    fdServerGroup.right = new FormAttachment( 100, -margin );
    wServerGroup.setLayoutData( fdServerGroup );

    // //////////////////////////////////////
    // / END OF SERVER ADDRESS GROUP
    // ///////////////////////////////////////

    // ////////////////////////////////////
    // START OF AUTHENTIFICATION GROUP
    // ////////////////////////////////////

    Group wAuthentificationGroup = new Group( wContentComp, SWT.SHADOW_NONE );
    props.setLook( wAuthentificationGroup );
    wAuthentificationGroup.setText( BaseMessages.getString( PKG, "Mail.Group.Authentification.Label" ) );

    FormLayout authentificationGroupLayout = new FormLayout();
    authentificationGroupLayout.marginWidth = 10;
    authentificationGroupLayout.marginHeight = 10;
    wAuthentificationGroup.setLayout( authentificationGroupLayout );

    // Authentication?
    Label wlUseAuth = new Label( wAuthentificationGroup, SWT.RIGHT );
    wlUseAuth.setText( BaseMessages.getString( PKG, "Mail.UseAuthentication.Label" ) );
    props.setLook( wlUseAuth );
    FormData fdlUseAuth = new FormData();
    fdlUseAuth.left = new FormAttachment( 0, 0 );
    fdlUseAuth.top = new FormAttachment( wServerGroup, margin );
    fdlUseAuth.right = new FormAttachment( middle, -2 * margin );
    wlUseAuth.setLayoutData( fdlUseAuth );
    wUseAuth = new Combo( wAuthentificationGroup, SWT.DROP_DOWN | SWT.READ_ONLY );
    wUseAuth.add( MailMeta.AUTENTICATION_NONE );
    wUseAuth.add( MailMeta.AUTENTICATION_BASIC );
    wUseAuth.add( MailMeta.AUTENTICATION_OAUTH );
    wUseAuth.select( wUseAuth.indexOf( MailMeta.AUTENTICATION_NONE ) );
    props.setLook( wUseAuth );
    wUseAuth.addModifyListener( lsMod );
    FormData fdUseAuth = new FormData();
    fdUseAuth.left = new FormAttachment( middle, -margin );
    fdUseAuth.top = new FormAttachment( wServerGroup, margin );
    fdUseAuth.right = new FormAttachment( 100, 0 );
    wUseAuth.setLayoutData( fdUseAuth );
    wUseAuth.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e ) {
        setUseAuth();
      }
    } );

    // AuthSecretKey line
    wAuthSecretKey = new LabelTextVar( transMeta, wAuthentificationGroup,
            BaseMessages.getString( PKG, "Mail.AuthenticationSecretKey.Label" ),
            BaseMessages.getString( PKG, "Mail.AuthenticationSecretKey.Tooltip" ), true );
    wAuthSecretKey.addModifyListener( lsMod );
    FormData fdAuthSecretKey = new FormData();
    fdAuthSecretKey.left = new FormAttachment( 0, 0 );
    fdAuthSecretKey.top = new FormAttachment( wUseAuth, margin );
    fdAuthSecretKey.right = new FormAttachment( 100, 0 );
    wAuthSecretKey.setLayoutData( fdAuthSecretKey );

    // AuthClientId line
    wAuthClientId = new LabelTextVar( transMeta, wAuthentificationGroup,
            BaseMessages.getString( PKG, "Mail.AuthenticationClientId.Label" ),
            BaseMessages.getString( PKG, "Mail.AuthenticationClientId.Tooltip" ));
    wAuthClientId.addModifyListener( lsMod );
    FormData fdAuthClientId = new FormData();
    fdAuthClientId.left = new FormAttachment( 0, 0 );
    fdAuthClientId.top = new FormAttachment( wAuthSecretKey, margin );
    fdAuthClientId.right = new FormAttachment( 100, 0 );
    wAuthClientId.setLayoutData( fdAuthClientId );

    // AuthUser line
    wlAuthUser = new Label( wAuthentificationGroup, SWT.RIGHT );
    wlAuthUser.setText( BaseMessages.getString( PKG, "Mail.AuthenticationUser.Label" ) );
    props.setLook( wlAuthUser );
    FormData fdlAuthUser = new FormData();
    fdlAuthUser.left = new FormAttachment( 0, -margin );
    fdlAuthUser.top = new FormAttachment( wAuthClientId, margin );
    fdlAuthUser.right = new FormAttachment( middle, -2 * margin );
    wlAuthUser.setLayoutData( fdlAuthUser );

    wAuthUser = new CCombo( wAuthentificationGroup, SWT.BORDER | SWT.READ_ONLY );
    wAuthUser.setEditable( true );
    props.setLook( wAuthUser );
    wAuthUser.addModifyListener( lsMod );
    FormData fdAuthUser = new FormData();
    fdAuthUser.left = new FormAttachment( middle, -margin );
    fdAuthUser.top = new FormAttachment( wAuthClientId, margin );
    fdAuthUser.right = new FormAttachment( 100, -margin );
    wAuthUser.setLayoutData( fdAuthUser );
    wAuthUser.addFocusListener( new FocusListener() {
      public void focusLost( org.eclipse.swt.events.FocusEvent e ) {
      }

      public void focusGained( org.eclipse.swt.events.FocusEvent e ) {
        Cursor busy = new Cursor( shell.getDisplay(), SWT.CURSOR_WAIT );
        shell.setCursor( busy );
        getPreviousFields();
        shell.setCursor( null );
        busy.dispose();
      }
    } );

    // AuthPass line
    wlAuthPass = new Label( wAuthentificationGroup, SWT.RIGHT );
    wlAuthPass.setText( BaseMessages.getString( PKG, "Mail.AuthenticationPassword.Label" ) );
    props.setLook( wlAuthPass );
    FormData fdlAuthPass = new FormData();
    fdlAuthPass.left = new FormAttachment( 0, -margin );
    fdlAuthPass.top = new FormAttachment( wAuthUser, margin );
    fdlAuthPass.right = new FormAttachment( middle, -2 * margin );
    wlAuthPass.setLayoutData( fdlAuthPass );

    wAuthPass = new CCombo( wAuthentificationGroup, SWT.BORDER | SWT.READ_ONLY );
    wAuthPass.setEditable( true );
    props.setLook( wAuthPass );
    wAuthPass.addModifyListener( lsMod );
    FormData fdAuthPass = new FormData();
    fdAuthPass.left = new FormAttachment( middle, -margin );
    fdAuthPass.top = new FormAttachment( wAuthUser, margin );
    fdAuthPass.right = new FormAttachment( 100, -margin );
    wAuthPass.setLayoutData( fdAuthPass );

    //Scope line
    wAuthScope = new LabelTextVar( transMeta, wAuthentificationGroup,
            BaseMessages.getString( PKG, "Mail.AuthenticationScope.Label" ),
            BaseMessages.getString( PKG, "Mail.AuthenticationScope.Tooltip" ));
    wAuthScope.addModifyListener( lsMod );
    FormData fdAuthScope = new FormData();
    fdAuthScope.left = new FormAttachment( 0, 0 );
    fdAuthScope.top = new FormAttachment( wAuthPass, margin );
    fdAuthScope.right = new FormAttachment( 100, 0 );
    wAuthScope.setLayoutData( fdAuthScope );
    // Grant Type
    wlGrantType = new Label( wAuthentificationGroup, SWT.RIGHT );
    wlGrantType.setText(BaseMessages.getString( PKG, "Mail.GrantType.Label" ) );
    props.setLook( wlGrantType );
    FormData fdlGrantType = new FormData();
    fdlGrantType.left = new FormAttachment( 0, 0 );
    fdlGrantType.top = new FormAttachment( wAuthScope, 2*margin );
    fdlGrantType.right = new FormAttachment( middle, -margin );
    wlGrantType.setLayoutData( fdlGrantType );
    grantType = new Combo( wAuthentificationGroup, SWT.DROP_DOWN );
    grantType.add( MailMeta.GRANTTYPE_CLIENTCREDENTIALS );
    grantType.add( MailMeta.GRANTTYPE_AUTHORIZATION_CODE );
    grantType.add( MailMeta.GRANTTYPE_REFRESH_TOKEN );
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
            BaseMessages.getString( PKG, "Mail.AuthenticationTokenUrl.Label" ),
            BaseMessages.getString( PKG, "Mail.AuthenticationTokenUrl.Tooltip" ));
    wAuthTokenUrl.addModifyListener( lsMod );
    FormData fdAuthTokenUrl = new FormData();
    fdAuthTokenUrl.left = new FormAttachment( 0, 0 );
    fdAuthTokenUrl.top = new FormAttachment( grantType, margin );
    fdAuthTokenUrl.right = new FormAttachment( 100, 0 );
    wAuthTokenUrl.setLayoutData( fdAuthTokenUrl );
    //AuthorizationCode
    wAuthorizationCode= new LabelTextVar( transMeta, wAuthentificationGroup,
            BaseMessages.getString( PKG, "Mail.AuthorizationCode.Label" ),
            BaseMessages.getString( PKG, "Mail.AuthorizationCode.Tooltip" ));
    wAuthorizationCode.addModifyListener( lsMod );
    FormData fdAuthorizationCode = new FormData();
    fdAuthorizationCode.left = new FormAttachment( 0, 0 );
    fdAuthorizationCode.top = new FormAttachment( wAuthTokenUrl, margin );
    fdAuthorizationCode.right = new FormAttachment( 100, 0 );
    wAuthorizationCode.setLayoutData( fdAuthorizationCode );
    //Redirect Uri
    wRedirectUri= new LabelTextVar( transMeta, wAuthentificationGroup,
            BaseMessages.getString( PKG, "Mail.RedirectURI.Label" ),
            BaseMessages.getString( PKG, "Mail.RedirectURI.Tooltip" ));
    wRedirectUri.addModifyListener( lsMod );
    FormData fdRedirectUri = new FormData();
    fdRedirectUri.left = new FormAttachment( 0, 0 );
    fdRedirectUri.top = new FormAttachment(wAuthorizationCode, margin );
    fdRedirectUri.right = new FormAttachment( 100, 0 );
    wRedirectUri.setLayoutData( fdRedirectUri );
    //Refresh Token
    wAuthRefreshToken= new LabelTextVar( transMeta, wAuthentificationGroup,
            BaseMessages.getString( PKG, "Mail.RefreshToken.Label" ),
            BaseMessages.getString( PKG, "Mail.RefreshToken.Tooltip" ));
    wAuthRefreshToken.addModifyListener( lsMod );
    FormData fdAuthRefreshToken = new FormData();
    fdAuthRefreshToken.left = new FormAttachment( 0, 0 );
    fdAuthRefreshToken.top = new FormAttachment(wRedirectUri, margin );
    fdAuthRefreshToken.right = new FormAttachment( 100, 0 );
    wAuthRefreshToken.setLayoutData( fdAuthRefreshToken );

    wAuthPass.addFocusListener( new FocusListener() {
      public void focusLost( org.eclipse.swt.events.FocusEvent e ) {
      }

      public void focusGained( org.eclipse.swt.events.FocusEvent e ) {
        Cursor busy = new Cursor( shell.getDisplay(), SWT.CURSOR_WAIT );
        shell.setCursor( busy );
        getPreviousFields();
        shell.setCursor( null );
        busy.dispose();
      }
    } );

    // Use secure authentication?
    wlUseSecAuth = new Label( wAuthentificationGroup, SWT.RIGHT );
    wlUseSecAuth.setText( BaseMessages.getString( PKG, "Mail.UseSecAuthentication.Label" ) );
    props.setLook( wlUseSecAuth );
    FormData fdlUseSecAuth = new FormData();
    fdlUseSecAuth.left = new FormAttachment( 0, 0 );
    fdlUseSecAuth.top = new FormAttachment( wAuthRefreshToken, margin );
    fdlUseSecAuth.right = new FormAttachment( middle, -2 * margin );
    wlUseSecAuth.setLayoutData( fdlUseSecAuth );
    wUseSecAuth = new Button( wAuthentificationGroup, SWT.CHECK );
    props.setLook( wUseSecAuth );
    FormData fdUseSecAuth = new FormData();
    fdUseSecAuth.left = new FormAttachment( middle, -margin );
    fdUseSecAuth.top = new FormAttachment( wAuthRefreshToken, margin );
    fdUseSecAuth.right = new FormAttachment( 100, 0 );
    wUseSecAuth.setLayoutData( fdUseSecAuth );
    wUseSecAuth.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e ) {
        setSecureConnectiontype();
        input.setChanged();
      }
    } );

    // SecureConnectionType
    wlSecureConnectionType = new Label( wAuthentificationGroup, SWT.RIGHT );
    wlSecureConnectionType.setText( BaseMessages.getString( PKG, "Mail.SecureConnectionType.Label" ) );
    props.setLook( wlSecureConnectionType );
    FormData fdlSecureConnectionType = new FormData();
    fdlSecureConnectionType.left = new FormAttachment( 0, 0 );
    fdlSecureConnectionType.top = new FormAttachment( wUseSecAuth, margin );
    fdlSecureConnectionType.right = new FormAttachment( middle, -2 * margin );
    wlSecureConnectionType.setLayoutData( fdlSecureConnectionType );
    wSecureConnectionType = new CCombo( wAuthentificationGroup, SWT.BORDER | SWT.READ_ONLY );
    wSecureConnectionType.setEditable( true );
    props.setLook( wSecureConnectionType );
    wSecureConnectionType.addModifyListener( lsMod );
    FormData fdSecureConnectionType = new FormData();
    fdSecureConnectionType.left = new FormAttachment( middle, -margin );
    fdSecureConnectionType.top = new FormAttachment( wUseSecAuth, margin );
    fdSecureConnectionType.right = new FormAttachment( 100, 0 );
    wSecureConnectionType.setLayoutData( fdSecureConnectionType );
    wSecureConnectionType.add( "SSL" );
    wSecureConnectionType.add( "TLS" );
    wSecureConnectionType.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e ) {
        setSecureConnectiontype();
        input.setChanged();
      }
    } );

    FormData fdAuthentificationGroup = new FormData();
    fdAuthentificationGroup.left = new FormAttachment( 0, margin );
    fdAuthentificationGroup.top = new FormAttachment( wServerGroup, margin );
    fdAuthentificationGroup.right = new FormAttachment( 100, -margin );
    fdAuthentificationGroup.bottom = new FormAttachment( 100, -margin );
    wAuthentificationGroup.setLayoutData( fdAuthentificationGroup );

    // //////////////////////////////////////
    // / END OF AUTHENTIFICATION GROUP
    // ///////////////////////////////////////

    FormData fdContentComp = new FormData();
    fdContentComp.left = new FormAttachment( 0, 0 );
    fdContentComp.top = new FormAttachment( 0, 0 );
    fdContentComp.right = new FormAttachment( 100, 0 );
    fdContentComp.bottom = new FormAttachment( 100, 0 );
    wContentComp.setLayoutData( wContentComp );

    wContentComp.layout();
    wContentTab.setControl( wContentComp );

    // ///////////////////////////////////////////////////////////
    // / END OF SERVER TAB
    // ///////////////////////////////////////////////////////////

    // ////////////////////////////////////
    // START OF MESSAGE TAB ///
    // ///////////////////////////////////

    CTabItem wMessageTab = new CTabItem( wTabFolder, SWT.NONE );
    wMessageTab.setText( BaseMessages.getString( PKG, "Mail.Tab.Message.Label" ) );

    FormLayout messageLayout = new FormLayout();
    messageLayout.marginWidth = 3;
    messageLayout.marginHeight = 3;

    Composite wMessageComp = new Composite( wTabFolder, SWT.NONE );
    props.setLook( wMessageComp );
    wMessageComp.setLayout( contentLayout );

    // ////////////////////////////////////
    // START OF MESSAGE SETTINGS GROUP
    // ////////////////////////////////////

    Group wMessageSettingsGroup = new Group( wMessageComp, SWT.SHADOW_NONE );
    props.setLook( wMessageSettingsGroup );
    wMessageSettingsGroup.setText( BaseMessages.getString( PKG, "Mail.Group.MessageSettings.Label" ) );

    FormLayout messageSettingsGroupLayout = new FormLayout();
    messageSettingsGroupLayout.marginWidth = 10;
    messageSettingsGroupLayout.marginHeight = 10;
    wMessageSettingsGroup.setLayout( messageSettingsGroupLayout );

    // Add date to logfile name?
    Label wlAddDate = new Label( wMessageSettingsGroup, SWT.RIGHT );
    wlAddDate.setText( BaseMessages.getString( PKG, "Mail.IncludeDate.Label" ) );
    props.setLook( wlAddDate );
    FormData fdlAddDate = new FormData();
    fdlAddDate.left = new FormAttachment( 0, 0 );
    fdlAddDate.top = new FormAttachment( 0, margin );
    fdlAddDate.right = new FormAttachment( middle, -2 * margin );
    wlAddDate.setLayoutData( fdlAddDate );
    wAddDate = new Button( wMessageSettingsGroup, SWT.CHECK );
    props.setLook( wAddDate );
    FormData fdAddDate = new FormData();
    fdAddDate.left = new FormAttachment( middle, -margin );
    fdAddDate.top = new FormAttachment( 0, margin );
    fdAddDate.right = new FormAttachment( 100, 0 );
    wAddDate.setLayoutData( fdAddDate );
    wAddDate.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e ) {
        input.setChanged();
      }
    } );

    // Only send the comment in the mail body
    Label wlOnlyComment = new Label( wMessageSettingsGroup, SWT.RIGHT );
    wlOnlyComment.setText( BaseMessages.getString( PKG, "Mail.OnlyCommentInBody.Label" ) );
    props.setLook( wlOnlyComment );
    FormData fdlOnlyComment = new FormData();
    fdlOnlyComment.left = new FormAttachment( 0, 0 );
    fdlOnlyComment.top = new FormAttachment( wAddDate, margin );
    fdlOnlyComment.right = new FormAttachment( middle, -2 * margin );
    wlOnlyComment.setLayoutData( fdlOnlyComment );
    wOnlyComment = new Button( wMessageSettingsGroup, SWT.CHECK );
    props.setLook( wOnlyComment );
    FormData fdOnlyComment = new FormData();
    fdOnlyComment.left = new FormAttachment( middle, -margin );
    fdOnlyComment.top = new FormAttachment( wAddDate, margin );
    fdOnlyComment.right = new FormAttachment( 100, 0 );
    wOnlyComment.setLayoutData( fdOnlyComment );
    wOnlyComment.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e ) {
          input.setChanged();
      }
    } );

    // HTML format ?
    Label wlUseHTML = new Label( wMessageSettingsGroup, SWT.RIGHT );
    wlUseHTML.setText( BaseMessages.getString( PKG, "Mail.UseHTMLInBody.Label" ) );
    props.setLook( wlUseHTML );
    FormData fdlUseHTML = new FormData();
    fdlUseHTML.left = new FormAttachment( 0, 0 );
    fdlUseHTML.top = new FormAttachment( wOnlyComment, margin );
    fdlUseHTML.right = new FormAttachment( middle, -2 * margin );
    wlUseHTML.setLayoutData( fdlUseHTML );
    wUseHTML = new Button( wMessageSettingsGroup, SWT.CHECK );
    props.setLook( wUseHTML );
    FormData fdUseHTML = new FormData();
    fdUseHTML.left = new FormAttachment( middle, -margin );
    fdUseHTML.top = new FormAttachment( wOnlyComment, margin );
    fdUseHTML.right = new FormAttachment( 100, 0 );
    wUseHTML.setLayoutData( fdUseHTML );
    wUseHTML.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e ) {
        SetEnabledEncoding();
        input.setChanged();
      }
    } );

    // Encoding
    wlEncoding = new Label( wMessageSettingsGroup, SWT.RIGHT );
    wlEncoding.setText( BaseMessages.getString( PKG, "Mail.Encoding.Label" ) );
    props.setLook( wlEncoding );
    FormData fdlEncoding = new FormData();
    fdlEncoding.left = new FormAttachment( 0, 0 );
    fdlEncoding.top = new FormAttachment( wUseHTML, margin );
    fdlEncoding.right = new FormAttachment( middle, -2 * margin );
    wlEncoding.setLayoutData( fdlEncoding );
    wEncoding = new CCombo( wMessageSettingsGroup, SWT.BORDER | SWT.READ_ONLY );
    wEncoding.setEditable( true );
    props.setLook( wEncoding );
    wEncoding.addModifyListener( lsMod );
    FormData fdEncoding = new FormData();
    fdEncoding.left = new FormAttachment( middle, -margin );
    fdEncoding.top = new FormAttachment( wUseHTML, margin );
    fdEncoding.right = new FormAttachment( 100, 0 );
    wEncoding.setLayoutData( fdEncoding );
    wEncoding.addFocusListener( new FocusListener() {
      public void focusLost( org.eclipse.swt.events.FocusEvent e ) {
      }

      public void focusGained( org.eclipse.swt.events.FocusEvent e ) {
        Cursor busy = new Cursor( shell.getDisplay(), SWT.CURSOR_WAIT );
        shell.setCursor( busy );
        setEncodings();
        shell.setCursor( null );
        busy.dispose();
      }
    } );

    // Use Priority ?
    Label wlUsePriority = new Label( wMessageSettingsGroup, SWT.RIGHT );
    wlUsePriority.setText( BaseMessages.getString( PKG, "Mail.UsePriority.Label" ) );
    props.setLook( wlUsePriority );
    FormData fdlPriority = new FormData();
    fdlPriority.left = new FormAttachment( 0, 0 );
    fdlPriority.top = new FormAttachment( wEncoding, margin );
    fdlPriority.right = new FormAttachment( middle, -2 * margin );
    wlUsePriority.setLayoutData( fdlPriority );
    wUsePriority = new Button( wMessageSettingsGroup, SWT.CHECK );
    wUsePriority.setToolTipText( BaseMessages.getString( PKG, "Mail.UsePriority.Tooltip" ) );
    props.setLook( wUsePriority );
    FormData fdUsePriority = new FormData();
    fdUsePriority.left = new FormAttachment( middle, -margin );
    fdUsePriority.top = new FormAttachment( wEncoding, margin );
    fdUsePriority.right = new FormAttachment( 100, 0 );
    wUsePriority.setLayoutData( fdUsePriority );
    wUsePriority.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e ) {
        activeUsePriority();
        input.setChanged();
      }
    } );

    SelectionAdapter selChanged = new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e ) {
        input.setChanged();
      }
    };

    // Priority
    wlPriority = new Label( wMessageSettingsGroup, SWT.RIGHT );
    wlPriority.setText( BaseMessages.getString( PKG, "Mail.Priority.Label" ) );
    props.setLook( wlPriority );
    fdlPriority = new FormData();
    fdlPriority.left = new FormAttachment( 0, 0 );
    fdlPriority.right = new FormAttachment( middle, -2 * margin );
    fdlPriority.top = new FormAttachment( wUsePriority, margin );
    wlPriority.setLayoutData( fdlPriority );
    wPriority = new CCombo( wMessageSettingsGroup, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER );
    wPriority.add( BaseMessages.getString( PKG, "Mail.Priority.Low.Label" ) );
    wPriority.add( BaseMessages.getString( PKG, "Mail.Priority.Normal.Label" ) );
    wPriority.add( BaseMessages.getString( PKG, "Mail.Priority.High.Label" ) );
    wPriority.select( 1 ); // +1: starts at -1
    wPriority.addSelectionListener( selChanged );
    props.setLook( wPriority );
    FormData fdPriority = new FormData();
    fdPriority.left = new FormAttachment( middle, -margin );
    fdPriority.top = new FormAttachment( wUsePriority, margin );
    fdPriority.right = new FormAttachment( 100, 0 );
    wPriority.setLayoutData( fdPriority );

    // Importance
    wlImportance = new Label( wMessageSettingsGroup, SWT.RIGHT );
    wlImportance.setText( BaseMessages.getString( PKG, "Mail.Importance.Label" ) );
    props.setLook( wlImportance );
    FormData fdlImportance = new FormData();
    fdlImportance.left = new FormAttachment( 0, 0 );
    fdlImportance.right = new FormAttachment( middle, -2 * margin );
    fdlImportance.top = new FormAttachment( wPriority, margin );
    wlImportance.setLayoutData( fdlImportance );
    wImportance = new CCombo( wMessageSettingsGroup, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER );
    wImportance.add( BaseMessages.getString( PKG, "Mail.Priority.Low.Label" ) );
    wImportance.add( BaseMessages.getString( PKG, "Mail.Priority.Normal.Label" ) );
    wImportance.add( BaseMessages.getString( PKG, "Mail.Priority.High.Label" ) );

    wImportance.select( 1 ); // +1: starts at -1
    wImportance.addSelectionListener( selChanged );

    props.setLook( wImportance );
    FormData fdImportance = new FormData();
    fdImportance.left = new FormAttachment( middle, -margin );
    fdImportance.top = new FormAttachment( wPriority, margin );
    fdImportance.right = new FormAttachment( 100, 0 );
    wImportance.setLayoutData( fdImportance );

    // Sensitivity
    wlSensitivity = new Label( wMessageSettingsGroup, SWT.RIGHT );
    wlSensitivity.setText( BaseMessages.getString( PKG, "Mail.Sensitivity.Label" ) );
    props.setLook( wlSensitivity );
    FormData fdlSensitivity = new FormData();
    fdlSensitivity.left = new FormAttachment( 0, 0 );
    fdlSensitivity.right = new FormAttachment( middle, -2 * margin );
    fdlSensitivity.top = new FormAttachment( wImportance, margin );
    wlSensitivity.setLayoutData( fdlSensitivity );
    wSensitivity = new CCombo( wMessageSettingsGroup, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER );
    wSensitivity.add( BaseMessages.getString( PKG, "Mail.Sensitivity.normal.Label" ) );
    wSensitivity.add( BaseMessages.getString( PKG, "Mail.Sensitivity.personal.Label" ) );
    wSensitivity.add( BaseMessages.getString( PKG, "Mail.Sensitivity.private.Label" ) );
    wSensitivity.add( BaseMessages.getString( PKG, "Mail.Sensitivity.confidential.Label" ) );
    wSensitivity.select( 0 );
    wSensitivity.addSelectionListener( selChanged );

    props.setLook( wSensitivity );
    FormData fdSensitivity = new FormData();
    fdSensitivity.left = new FormAttachment( middle, -margin );
    fdSensitivity.top = new FormAttachment( wImportance, margin );
    fdSensitivity.right = new FormAttachment( 100, 0 );
    wSensitivity.setLayoutData( fdSensitivity );
    FormData fdMessageSettingsGroup = new FormData();
    fdMessageSettingsGroup.left = new FormAttachment( 0, margin );
    fdMessageSettingsGroup.top = new FormAttachment( wName, margin );
    fdMessageSettingsGroup.right = new FormAttachment( 100, -margin );
    wMessageSettingsGroup.setLayoutData( fdMessageSettingsGroup );

    // //////////////////////////////////////
    // / END OF MESSAGE SETTINGS GROUP
    // ///////////////////////////////////////

    // ////////////////////////////////////
    // START OF MESSAGE GROUP
    // ////////////////////////////////////

    Group wMessageGroup = new Group( wMessageComp, SWT.SHADOW_NONE );
    props.setLook( wMessageGroup );
    wMessageGroup.setText( BaseMessages.getString( PKG, "Mail.Group.Message.Label" ) );

    FormLayout messageGroupLayout = new FormLayout();
    messageGroupLayout.marginWidth = 10;
    messageGroupLayout.marginHeight = 10;
    wMessageGroup.setLayout( messageGroupLayout );

    // Subject line
    Label wlSubject = new Label( wMessageGroup, SWT.RIGHT );
    wlSubject.setText( BaseMessages.getString( PKG, "Mail.Subject.Label" ) );
    props.setLook( wlSubject );
    FormData fdlSubject = new FormData();
    fdlSubject.left = new FormAttachment( 0, -margin );
    fdlSubject.top = new FormAttachment( wMessageSettingsGroup, margin );
    fdlSubject.right = new FormAttachment( middle, -2 * margin );
    wlSubject.setLayoutData( fdlSubject );

    wSubject = new CCombo( wMessageGroup, SWT.BORDER | SWT.READ_ONLY );
    wSubject.setEditable( true );
    props.setLook( wSubject );
    wSubject.addModifyListener( lsMod );
    FormData fdSubject = new FormData();
    fdSubject.left = new FormAttachment( middle, -margin );
    fdSubject.top = new FormAttachment( wMessageSettingsGroup, margin );
    fdSubject.right = new FormAttachment( 100, -margin );
    wSubject.setLayoutData( fdSubject );
    wSubject.addFocusListener( new FocusListener() {
      public void focusLost( org.eclipse.swt.events.FocusEvent e ) {
      }

      public void focusGained( org.eclipse.swt.events.FocusEvent e ) {
        Cursor busy = new Cursor( shell.getDisplay(), SWT.CURSOR_WAIT );
        shell.setCursor( busy );
        getPreviousFields();
        shell.setCursor( null );
        busy.dispose();
      }
    } );
    // Comment line
    Label wlComment = new Label( wMessageGroup, SWT.RIGHT );
    wlComment.setText( BaseMessages.getString( PKG, "Mail.Comment.Label" ) );
    props.setLook( wlComment );
    FormData fdlComment = new FormData();
    fdlComment.left = new FormAttachment( 0, -margin );
    fdlComment.top = new FormAttachment( wSubject, margin );
    fdlComment.right = new FormAttachment( middle, -2 * margin );
    wlComment.setLayoutData( fdlComment );

    wComment = new CCombo( wMessageGroup, SWT.BORDER | SWT.READ_ONLY );
    wComment.setEditable( true );
    props.setLook( wComment );
    wComment.addModifyListener( lsMod );
    FormData fdComment = new FormData();
    fdComment.left = new FormAttachment( middle, -margin );
    fdComment.top = new FormAttachment( wSubject, margin );
    fdComment.right = new FormAttachment( 100, -margin );
    wComment.setLayoutData( fdComment );
    wComment.addFocusListener( new FocusListener() {
      public void focusLost( org.eclipse.swt.events.FocusEvent e ) {
      }

      public void focusGained( org.eclipse.swt.events.FocusEvent e ) {
        Cursor busy = new Cursor( shell.getDisplay(), SWT.CURSOR_WAIT );
        shell.setCursor( busy );
        getPreviousFields();
        shell.setCursor( null );
        busy.dispose();
      }
    } );
    FormData fdMessageGroup = new FormData();
    fdMessageGroup.left = new FormAttachment( 0, margin );
    fdMessageGroup.top = new FormAttachment( wMessageSettingsGroup, margin );
    fdMessageGroup.bottom = new FormAttachment( 100, -margin );
    fdMessageGroup.right = new FormAttachment( 100, -margin );
    wMessageGroup.setLayoutData( fdMessageGroup );

    // //////////////////////////////////////
    // / END OF MESSAGE GROUP
    // ///////////////////////////////////////

    FormData fdMessageComp = new FormData();
    fdMessageComp.left = new FormAttachment( 0, 0 );
    fdMessageComp.top = new FormAttachment( 0, 0 );
    fdMessageComp.right = new FormAttachment( 100, 0 );
    fdMessageComp.bottom = new FormAttachment( 100, 0 );
    wMessageComp.setLayoutData( wMessageComp );

    wMessageComp.layout();
    wMessageTab.setControl( wMessageComp );

    // ///////////////////////////////////////////////////////////
    // / END OF MESSAGE TAB
    // ///////////////////////////////////////////////////////////

    // ////////////////////////////////////
    // START OF ATTACHED FILES TAB ///
    // ///////////////////////////////////

    CTabItem wAttachedTab = new CTabItem( wTabFolder, SWT.NONE );
    wAttachedTab.setText( BaseMessages.getString( PKG, "Mail.Tab.AttachedFiles.Label" ) );

    FormLayout attachedLayout = new FormLayout();
    attachedLayout.marginWidth = 3;
    attachedLayout.marginHeight = 3;

    Composite wAttachedComp = new Composite( wTabFolder, SWT.NONE );
    props.setLook( wAttachedComp );
    wAttachedComp.setLayout( attachedLayout );

    // ///////////////////////////////
    // START OF Attached files GROUP //
    // ///////////////////////////////

    Group wAttachedContent = new Group( wAttachedComp, SWT.SHADOW_NONE );
    props.setLook( wAttachedContent );
    wAttachedContent.setText( BaseMessages.getString( PKG, "MailDialog.AttachedContent.Label" ) );

    FormLayout attachedContentGroupLayout = new FormLayout();
    attachedContentGroupLayout.marginWidth = 10;
    attachedContentGroupLayout.marginHeight = 10;
    wAttachedContent.setLayout( attachedContentGroupLayout );

    // Is Filename defined in a Field
    Label wlIsAttachContentField = new Label( wAttachedContent, SWT.RIGHT );
    wlIsAttachContentField.setText( BaseMessages.getString( PKG, "MailDialog.isattachContentField.Label" ) );
    props.setLook( wlIsAttachContentField );
    FormData fdlIsAttachContentField = new FormData();
    fdlIsAttachContentField.left = new FormAttachment( 0, -margin );
    fdlIsAttachContentField.top = new FormAttachment( 0, margin );
    fdlIsAttachContentField.right = new FormAttachment( middle, -2 * margin );
    wlIsAttachContentField.setLayoutData( fdlIsAttachContentField );

    wIsAttachContentField = new Button( wAttachedContent, SWT.CHECK );
    props.setLook( wIsAttachContentField );
    wIsAttachContentField.setToolTipText( BaseMessages.getString( PKG, "MailDialog.isattachContentField.Tooltip" ) );
    FormData fdIsAttachContentField = new FormData();
    fdIsAttachContentField.left = new FormAttachment( middle, -margin );
    fdIsAttachContentField.top = new FormAttachment( 0, margin );
    wIsAttachContentField.setLayoutData( fdIsAttachContentField );
    SelectionAdapter lIsAttachContentField = new SelectionAdapter() {
      public void widgetSelected( SelectionEvent arg0 ) {
        activeISAttachContentField();
        input.setChanged();
      }
    };
    wIsAttachContentField.addSelectionListener( lIsAttachContentField );

    // attach file content field
    wlAttachContentField = new Label( wAttachedContent, SWT.RIGHT );
    wlAttachContentField.setText( BaseMessages.getString( PKG, "MailDialog.attachContentField.Label" ) );
    props.setLook( wlAttachContentField );
    FormData fdlAttachContentField = new FormData();
    fdlAttachContentField.left = new FormAttachment( 0, -margin );
    fdlAttachContentField.top = new FormAttachment( wIsAttachContentField, margin );
    fdlAttachContentField.right = new FormAttachment( middle, -2 * margin );
    wlAttachContentField.setLayoutData( fdlAttachContentField );

    wAttachContentField = new CCombo( wAttachedContent, SWT.BORDER | SWT.READ_ONLY );
    wAttachContentField.setEditable( true );
    props.setLook( wAttachContentField );
    wAttachContentField.addModifyListener( lsMod );
    FormData fdAttachContentField = new FormData();
    fdAttachContentField.left = new FormAttachment( middle, -margin );
    fdAttachContentField.top = new FormAttachment( wIsAttachContentField, margin );
    fdAttachContentField.right = new FormAttachment( 100, -margin );
    wAttachContentField.setLayoutData( fdAttachContentField );
    wAttachContentField.addFocusListener( new FocusListener() {
      public void focusLost( org.eclipse.swt.events.FocusEvent e ) {
      }

      public void focusGained( org.eclipse.swt.events.FocusEvent e ) {
        Cursor busy = new Cursor( shell.getDisplay(), SWT.CURSOR_WAIT );
        shell.setCursor( busy );
        getPreviousFields();
        shell.setCursor( null );
        busy.dispose();
      }
    } );

    // attached content filename field
    wlAttachContentFileNameField = new Label( wAttachedContent, SWT.RIGHT );
    wlAttachContentFileNameField.setText( BaseMessages.getString(
      PKG, "MailDialog.attachContentFileNameField.Label" ) );
    props.setLook( wlAttachContentFileNameField );
    FormData fdlAttachContentFileNameField = new FormData();
    fdlAttachContentFileNameField.left = new FormAttachment( 0, -margin );
    fdlAttachContentFileNameField.top = new FormAttachment( wAttachContentField, margin );
    fdlAttachContentFileNameField.right = new FormAttachment( middle, -2 * margin );
    wlAttachContentFileNameField.setLayoutData( fdlAttachContentFileNameField );

    wAttachContentFileNameField = new CCombo( wAttachedContent, SWT.BORDER | SWT.READ_ONLY );
    wAttachContentFileNameField.setEditable( true );
    props.setLook( wAttachContentFileNameField );
    wAttachContentFileNameField.addModifyListener( lsMod );
    FormData fdAttachContentFileNameField = new FormData();
    fdAttachContentFileNameField.left = new FormAttachment( middle, -margin );
    fdAttachContentFileNameField.top = new FormAttachment( wAttachContentField, margin );
    fdAttachContentFileNameField.right = new FormAttachment( 100, -margin );
    wAttachContentFileNameField.setLayoutData( fdAttachContentFileNameField );
    wAttachContentFileNameField.addFocusListener( new FocusListener() {
      public void focusLost( org.eclipse.swt.events.FocusEvent e ) {
      }

      public void focusGained( org.eclipse.swt.events.FocusEvent e ) {
        Cursor busy = new Cursor( shell.getDisplay(), SWT.CURSOR_WAIT );
        shell.setCursor( busy );
        getPreviousFields();
        shell.setCursor( null );
        busy.dispose();
      }
    } );

    FormData fdAttachedContent = new FormData();
    fdAttachedContent.left = new FormAttachment( 0, margin );
    fdAttachedContent.top = new FormAttachment( 0, 2 * margin );
    fdAttachedContent.right = new FormAttachment( 100, -margin );
    wAttachedContent.setLayoutData( fdAttachedContent );

    // ///////////////////////////////////////////////////////////
    // / END OF Attached files GROUP
    // ///////////////////////////////////////////////////////////

    // ///////////////////////////////
    // START OF Origin files GROUP //
    // ///////////////////////////////

    wOriginFiles = new Group( wAttachedComp, SWT.SHADOW_NONE );
    props.setLook( wOriginFiles );
    wOriginFiles.setText( BaseMessages.getString( PKG, "MailDialog.OriginAttachedFiles.Label" ) );

    FormLayout originFilesGroupLayout = new FormLayout();
    originFilesGroupLayout.marginWidth = 10;
    originFilesGroupLayout.marginHeight = 10;
    wOriginFiles.setLayout( originFilesGroupLayout );

    // Is Filename defined in a Field
    Label wlIsFileDynamic = new Label( wOriginFiles, SWT.RIGHT );
    wlIsFileDynamic.setText( BaseMessages.getString( PKG, "MailDialog.isFileDynamic.Label" ) );
    props.setLook( wlIsFileDynamic );
    FormData fdlIsFileDynamic = new FormData();
    fdlIsFileDynamic.left = new FormAttachment( 0, -margin );
    fdlIsFileDynamic.top = new FormAttachment( wAttachedContent, margin );
    fdlIsFileDynamic.right = new FormAttachment( middle, -2 * margin );
    wlIsFileDynamic.setLayoutData( fdlIsFileDynamic );

    wIsFileDynamic = new Button( wOriginFiles, SWT.CHECK );
    props.setLook( wIsFileDynamic );
    wIsFileDynamic.setToolTipText( BaseMessages.getString( PKG, "MailDialog.isFileDynamic.Tooltip" ) );
    FormData fdIsFileDynamic = new FormData();
    fdIsFileDynamic.left = new FormAttachment( middle, -margin );
    fdIsFileDynamic.top = new FormAttachment( wAttachedContent, margin );
    wIsFileDynamic.setLayoutData( fdIsFileDynamic );
    SelectionAdapter lIsFileDynamic = new SelectionAdapter() {
      public void widgetSelected( SelectionEvent arg0 ) {
        activeIsFileDynamic();
        input.setChanged();
      }
    };
    wIsFileDynamic.addSelectionListener( lIsFileDynamic );

    // Filename field
    wlDynamicFilenameField = new Label( wOriginFiles, SWT.RIGHT );
    wlDynamicFilenameField.setText( BaseMessages.getString( PKG, "MailDialog.DynamicFilenameField.Label" ) );
    props.setLook( wlDynamicFilenameField );
    FormData fdlFilenameField = new FormData();
    fdlFilenameField.left = new FormAttachment( 0, -margin );
    fdlFilenameField.top = new FormAttachment( wIsFileDynamic, margin );
    fdlFilenameField.right = new FormAttachment( middle, -2 * margin );
    wlDynamicFilenameField.setLayoutData( fdlFilenameField );

    wDynamicFilenameField = new CCombo( wOriginFiles, SWT.BORDER | SWT.READ_ONLY );
    wDynamicFilenameField.setEditable( true );
    props.setLook( wDynamicFilenameField );
    wDynamicFilenameField.addModifyListener( lsMod );
    FormData fdFilenameField = new FormData();
    fdFilenameField.left = new FormAttachment( middle, -margin );
    fdFilenameField.top = new FormAttachment( wIsFileDynamic, margin );
    fdFilenameField.right = new FormAttachment( 100, -margin );
    wDynamicFilenameField.setLayoutData( fdFilenameField );
    wDynamicFilenameField.addFocusListener( new FocusListener() {
      public void focusLost( org.eclipse.swt.events.FocusEvent e ) {
      }

      public void focusGained( org.eclipse.swt.events.FocusEvent e ) {
        Cursor busy = new Cursor( shell.getDisplay(), SWT.CURSOR_WAIT );
        shell.setCursor( busy );
        getPreviousFields();
        shell.setCursor( null );
        busy.dispose();
      }
    } );

    // Wildcard field
    wlDynamicWildcardField = new Label( wOriginFiles, SWT.RIGHT );
    wlDynamicWildcardField.setText( BaseMessages.getString( PKG, "MailDialog.DynamicWildcardField.Label" ) );
    props.setLook( wlDynamicWildcardField );
    FormData fdlDynamicWildcardField = new FormData();
    fdlDynamicWildcardField.left = new FormAttachment( 0, -margin );
    fdlDynamicWildcardField.top = new FormAttachment( wDynamicFilenameField, margin );
    fdlDynamicWildcardField.right = new FormAttachment( middle, -2 * margin );
    wlDynamicWildcardField.setLayoutData( fdlDynamicWildcardField );

    wDynamicWildcardField = new CCombo( wOriginFiles, SWT.BORDER | SWT.READ_ONLY );
    wDynamicWildcardField.setEditable( true );
    props.setLook( wDynamicWildcardField );
    wDynamicWildcardField.addModifyListener( lsMod );
    FormData fdDynamicWildcardField = new FormData();
    fdDynamicWildcardField.left = new FormAttachment( middle, -margin );
    fdDynamicWildcardField.top = new FormAttachment( wDynamicFilenameField, margin );
    fdDynamicWildcardField.right = new FormAttachment( 100, -margin );
    wDynamicWildcardField.setLayoutData( fdDynamicWildcardField );
    wDynamicWildcardField.addFocusListener( new FocusListener() {
      public void focusLost( org.eclipse.swt.events.FocusEvent e ) {
      }

      public void focusGained( org.eclipse.swt.events.FocusEvent e ) {
        Cursor busy = new Cursor( shell.getDisplay(), SWT.CURSOR_WAIT );
        shell.setCursor( busy );
        getPreviousFields();
        shell.setCursor( null );
        busy.dispose();
      }
    } );

    // FileFolderName line
    wlSourceFileFolderName = new Label( wOriginFiles, SWT.RIGHT );
    wlSourceFileFolderName.setText( BaseMessages.getString( PKG, "MailDialog.FileFoldername.Label" ) );
    props.setLook( wlSourceFileFolderName );
    FormData fdlSourceFileFolderName = new FormData();
    fdlSourceFileFolderName.left = new FormAttachment( 0, 0 );
    fdlSourceFileFolderName.top = new FormAttachment( wDynamicWildcardField, 2 * margin );
    fdlSourceFileFolderName.right = new FormAttachment( middle, -margin );
    wlSourceFileFolderName.setLayoutData( fdlSourceFileFolderName );

    // Browse Destination folders button ...
    wbSourceFolder = new Button( wOriginFiles, SWT.PUSH | SWT.CENTER );
    props.setLook( wbSourceFolder );
    wbSourceFolder.setText( BaseMessages.getString( PKG, "MailDialog.BrowseFolders.Label" ) );
    FormData fdbSourceFolder = new FormData();
    fdbSourceFolder.right = new FormAttachment( 100, 0 );
    fdbSourceFolder.top = new FormAttachment( wDynamicWildcardField, 2 * margin );
    wbSourceFolder.setLayoutData( fdbSourceFolder );

    // Browse source file button ...
    wbFileFolderName = new Button( wOriginFiles, SWT.PUSH | SWT.CENTER );
    props.setLook( wbFileFolderName );
    wbFileFolderName.setText( BaseMessages.getString( PKG, "MailDialog.BrowseFiles.Label" ) );
    FormData fdbSourceFileFolderName = new FormData();
    fdbSourceFileFolderName.right = new FormAttachment( wbSourceFolder, -margin );
    fdbSourceFileFolderName.top = new FormAttachment( wDynamicWildcardField, 2 * margin );
    wbFileFolderName.setLayoutData( fdbSourceFileFolderName );

    wSourceFileFolderName = new TextVar( transMeta, wOriginFiles, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wSourceFileFolderName );
    wSourceFileFolderName.addModifyListener( lsMod );
    FormData fdSourceFileFolderName = new FormData();
    fdSourceFileFolderName.left = new FormAttachment( middle, 0 );
    fdSourceFileFolderName.top = new FormAttachment( wDynamicWildcardField, 2 * margin );
    fdSourceFileFolderName.right = new FormAttachment( wbFileFolderName, -margin );
    wSourceFileFolderName.setLayoutData( fdSourceFileFolderName );

    // Whenever something changes, set the tooltip to the expanded version:
    wSourceFileFolderName.addModifyListener(
      e -> wSourceFileFolderName.setToolTipText( transMeta.environmentSubstitute( wSourceFileFolderName.getText() ) ) );

    wbSourceFolder.addSelectionListener( new SelectionAdapterFileDialogTextVar( log, wSourceFileFolderName, transMeta,
      new SelectionAdapterOptions( transMeta.getBowl(), SelectionOperation.FOLDER ) ) );

    wbFileFolderName.addSelectionListener( new SelectionAdapterFileDialogTextVar( log, wSourceFileFolderName, transMeta,
      new SelectionAdapterOptions( transMeta.getBowl(), SelectionOperation.FILE,
        new FilterType[] { FilterType.ALL }, FilterType.ALL  ) ) );


    // Include sub folders
    Label wlIncludeSubFolders = new Label( wOriginFiles, SWT.RIGHT );
    wlIncludeSubFolders.setText( BaseMessages.getString( PKG, "MailDialog.includeSubFolders.Label" ) );
    props.setLook( wlIncludeSubFolders );
    FormData fdlIncludeSubFolders = new FormData();
    fdlIncludeSubFolders.left = new FormAttachment( 0, 0 );
    fdlIncludeSubFolders.top = new FormAttachment( wSourceFileFolderName, margin );
    fdlIncludeSubFolders.right = new FormAttachment( middle, -margin );
    wlIncludeSubFolders.setLayoutData( fdlIncludeSubFolders );
    wIncludeSubFolders = new Button( wOriginFiles, SWT.CHECK );
    props.setLook( wIncludeSubFolders );
    wIncludeSubFolders.setToolTipText( BaseMessages.getString( PKG, "MailDialog.includeSubFolders.Tooltip" ) );
    FormData fdIncludeSubFolders = new FormData();
    fdIncludeSubFolders.left = new FormAttachment( middle, 0 );
    fdIncludeSubFolders.top = new FormAttachment( wSourceFileFolderName, margin );
    fdIncludeSubFolders.right = new FormAttachment( 100, 0 );
    wIncludeSubFolders.setLayoutData( fdIncludeSubFolders );
    wIncludeSubFolders.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e ) {
        input.setChanged();
      }
    } );

    // Wildcard
    wlWildcard = new Label( wOriginFiles, SWT.RIGHT );
    wlWildcard.setText( BaseMessages.getString( PKG, "MailDialog.Wildcard.Label" ) );
    props.setLook( wlWildcard );
    FormData fdlWildcard = new FormData();
    fdlWildcard.left = new FormAttachment( 0, 0 );
    fdlWildcard.top = new FormAttachment( wIncludeSubFolders, margin );
    fdlWildcard.right = new FormAttachment( middle, -margin );
    wlWildcard.setLayoutData( fdlWildcard );
    wWildcard = new TextVar( transMeta, wOriginFiles, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wWildcard );
    wWildcard.setToolTipText( BaseMessages.getString( PKG, "MailDialog.Wildcard.Tooltip" ) );
    wWildcard.addModifyListener( lsMod );
    FormData fdWildcard = new FormData();
    fdWildcard.left = new FormAttachment( middle, 0 );
    fdWildcard.top = new FormAttachment( wIncludeSubFolders, margin );
    fdWildcard.right = new FormAttachment( wbFileFolderName, -margin );
    wWildcard.setLayoutData( fdWildcard );

    // Whenever something changes, set the tooltip to the expanded version:
    wWildcard.addModifyListener(
      e -> wWildcard.setToolTipText( transMeta.environmentSubstitute( wWildcard.getText() ) ) );
    FormData fdOriginFiles = new FormData();
    fdOriginFiles.left = new FormAttachment( 0, margin );
    fdOriginFiles.top = new FormAttachment( wAttachedContent, 2 * margin );
    fdOriginFiles.right = new FormAttachment( 100, -margin );
    wOriginFiles.setLayoutData( fdOriginFiles );

    // ///////////////////////////////////////////////////////////
    // / END OF Origin files GROUP
    // ///////////////////////////////////////////////////////////

    // ///////////////////////////////
    // START OF Zip Group files GROUP //
    // ///////////////////////////////

    wZipGroup = new Group( wAttachedComp, SWT.SHADOW_NONE );
    props.setLook( wZipGroup );
    wZipGroup.setText( BaseMessages.getString( PKG, "MailDialog.ZipGroup.Label" ) );

    FormLayout zipGroupGroupLayout = new FormLayout();
    zipGroupGroupLayout.marginWidth = 10;
    zipGroupGroupLayout.marginHeight = 10;
    wZipGroup.setLayout( zipGroupGroupLayout );

    // Zip Files?
    Label wlZipFiles = new Label( wZipGroup, SWT.RIGHT );
    wlZipFiles.setText( BaseMessages.getString( PKG, "MailDialog.ZipFiles.Label" ) );
    props.setLook( wlZipFiles );
    FormData fdlZipFiles = new FormData();
    fdlZipFiles.left = new FormAttachment( 0, -margin );
    fdlZipFiles.top = new FormAttachment( wOriginFiles, margin );
    fdlZipFiles.right = new FormAttachment( middle, -2 * margin );
    wlZipFiles.setLayoutData( fdlZipFiles );
    wZipFiles = new Button( wZipGroup, SWT.CHECK );
    props.setLook( wZipFiles );
    FormData fdZipFiles = new FormData();
    fdZipFiles.left = new FormAttachment( middle, -margin );
    fdZipFiles.top = new FormAttachment( wOriginFiles, margin );
    fdZipFiles.right = new FormAttachment( 100, -margin );
    wZipFiles.setLayoutData( fdZipFiles );
    wZipFiles.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e ) {
        input.setChanged();
        setZip();
      }
    } );

    // is zipfilename is dynamic?
    wlIsZipFileDynamic = new Label( wZipGroup, SWT.RIGHT );
    wlIsZipFileDynamic.setText( BaseMessages.getString( PKG, "MailDialog.isZipFileDynamic.Label" ) );
    props.setLook( wlIsZipFileDynamic );
    FormData fdlIsZipFileDynamic = new FormData();
    fdlIsZipFileDynamic.left = new FormAttachment( 0, -margin );
    fdlIsZipFileDynamic.top = new FormAttachment( wZipFiles, margin );
    fdlIsZipFileDynamic.right = new FormAttachment( middle, -2 * margin );
    wlIsZipFileDynamic.setLayoutData( fdlIsZipFileDynamic );
    wisZipFileDynamic = new Button( wZipGroup, SWT.CHECK );
    props.setLook( wisZipFileDynamic );
    FormData fdIsZipFileDynamic = new FormData();
    fdIsZipFileDynamic.left = new FormAttachment( middle, -margin );
    fdIsZipFileDynamic.top = new FormAttachment( wZipFiles, margin );
    fdIsZipFileDynamic.right = new FormAttachment( 100, -margin );
    wisZipFileDynamic.setLayoutData( fdIsZipFileDynamic );
    wisZipFileDynamic.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e ) {
        input.setChanged();
        setDynamicZip();
      }
    } );

    // ZipFile field
    wlDynamicZipFileField = new Label( wZipGroup, SWT.RIGHT );
    wlDynamicZipFileField.setText( BaseMessages.getString( PKG, "MailDialog.DynamicZipFileField.Label" ) );
    props.setLook( wlDynamicZipFileField );
    FormData fdlDynamicZipFileField = new FormData();
    fdlDynamicZipFileField.left = new FormAttachment( 0, -margin );
    fdlDynamicZipFileField.top = new FormAttachment( wisZipFileDynamic, margin );
    fdlDynamicZipFileField.right = new FormAttachment( middle, -2 * margin );
    wlDynamicZipFileField.setLayoutData( fdlDynamicZipFileField );

    wDynamicZipFileField = new CCombo( wZipGroup, SWT.BORDER | SWT.READ_ONLY );
    wDynamicZipFileField.setEditable( true );
    props.setLook( wDynamicZipFileField );
    wDynamicZipFileField.addModifyListener( lsMod );
    FormData fdDynamicZipFileField = new FormData();
    fdDynamicZipFileField.left = new FormAttachment( middle, -margin );
    fdDynamicZipFileField.top = new FormAttachment( wisZipFileDynamic, margin );
    fdDynamicZipFileField.right = new FormAttachment( 100, -margin );
    wDynamicZipFileField.setLayoutData( fdDynamicZipFileField );
    wDynamicZipFileField.addFocusListener( new FocusListener() {
      public void focusLost( org.eclipse.swt.events.FocusEvent e ) {
      }

      public void focusGained( org.eclipse.swt.events.FocusEvent e ) {
        Cursor busy = new Cursor( shell.getDisplay(), SWT.CURSOR_WAIT );
        shell.setCursor( busy );
        getPreviousFields();
        shell.setCursor( null );
        busy.dispose();
      }
    } );

    // ZipFilename line
    wZipFilename =
      new LabelTextVar(
        transMeta, wZipGroup, BaseMessages.getString( PKG, "MailDialog.ZipFilename.Label" ), BaseMessages
          .getString( PKG, "MailDialog.ZipFilename.Tooltip" ) );
    wZipFilename.addModifyListener( lsMod );
    FormData fdZipFilename = new FormData();
    fdZipFilename.left = new FormAttachment( 0, -margin );
    fdZipFilename.top = new FormAttachment( wDynamicZipFileField, margin );
    fdZipFilename.right = new FormAttachment( 100, -4 * margin );
    wZipFilename.setLayoutData( fdZipFilename );

    // Zip files on condition?
    wZipSizeCondition =
      new LabelTextVar(
        transMeta, wZipGroup, BaseMessages.getString( PKG, "MailDialog.ZipSizeCondition.Label" ), BaseMessages
          .getString( PKG, "MailDialog.ZipSizeCondition.Tooltip" ) );
    wZipSizeCondition.addModifyListener( lsMod );
    FormData fdZipSizeCondition = new FormData();
    fdZipSizeCondition.left = new FormAttachment( 0, -margin );
    fdZipSizeCondition.top = new FormAttachment( wZipFilename, margin );
    fdZipSizeCondition.right = new FormAttachment( 100, -4 * margin );
    wZipSizeCondition.setLayoutData( fdZipSizeCondition );

    FormData fdZipGroup = new FormData();
    fdZipGroup.left = new FormAttachment( 0, margin );
    fdZipGroup.top = new FormAttachment( wOriginFiles, margin );
    fdZipGroup.right = new FormAttachment( 100, -margin );
    wZipGroup.setLayoutData( fdZipGroup );

    // ///////////////////////////////////////////////////////////
    // / END OF Zip Group GROUP
    // ///////////////////////////////////////////////////////////

    FormData fdAttachedComp = new FormData();
    fdAttachedComp.left = new FormAttachment( 0, 0 );
    fdAttachedComp.top = new FormAttachment( 0, 0 );
    fdAttachedComp.right = new FormAttachment( 100, 0 );
    fdAttachedComp.bottom = new FormAttachment( 100, 0 );
    wAttachedComp.setLayoutData( wAttachedComp );

    wAttachedComp.layout();
    wAttachedTab.setControl( wAttachedComp );

    // ///////////////////////////////////////////////////////////
    // / END OF FILES TAB
    // ///////////////////////////////////////////////////////////

    // ////////////////////////////////////
    // START OF embedded images TAB ///
    // ///////////////////////////////////

    CTabItem wEmbeddedTab = new CTabItem( wTabFolder, SWT.NONE );
    wEmbeddedTab.setText( BaseMessages.getString( PKG, "Mail.Tab.embeddedImages.Label" ) );

    FormLayout embeddedLayout = new FormLayout();
    embeddedLayout.marginWidth = 3;
    embeddedLayout.marginHeight = 3;

    Composite wEmbeddedComp = new Composite( wTabFolder, SWT.NONE );
    props.setLook( wEmbeddedComp );
    wEmbeddedComp.setLayout( embeddedLayout );

    // ImageFilename line
    wlImageFilename = new Label( wEmbeddedComp, SWT.RIGHT );
    wlImageFilename.setText( BaseMessages.getString( PKG, "MailDialog.ImageFilename.Label" ) );
    props.setLook( wlImageFilename );
    FormData fdlImageFilename = new FormData();
    fdlImageFilename.left = new FormAttachment( 0, 0 );
    fdlImageFilename.top = new FormAttachment( wStepname, margin );
    fdlImageFilename.right = new FormAttachment( middle, -margin );
    wlImageFilename.setLayoutData( fdlImageFilename );

    wbImageFilename = new Button( wEmbeddedComp, SWT.PUSH | SWT.CENTER );
    props.setLook( wbImageFilename );
    wbImageFilename.setText( BaseMessages.getString( PKG, "MailDialog.BrowseFiles.Label" ) );
    wbImageFilename.setToolTipText( BaseMessages.getString( PKG, "MailDialog.BrowseFiles.Tooltip" ) );
    FormData fdbImageFilename = new FormData();
    fdbImageFilename.right = new FormAttachment( 100, 0 );
    fdbImageFilename.top = new FormAttachment( wStepname, margin );
    fdbImageFilename.right = new FormAttachment( 100, -margin );
    wbImageFilename.setLayoutData( fdbImageFilename );

    wbAddImageFilename = new Button( wEmbeddedComp, SWT.PUSH | SWT.CENTER );
    props.setLook( wbAddImageFilename );
    wbAddImageFilename.setText( BaseMessages.getString( PKG, "MailDialog.ImageFilenameAdd.Button" ) );
    wbAddImageFilename.setToolTipText( BaseMessages.getString( PKG, "MailDialog.ImageFilenameAdd.Tooltip" ) );
    FormData fdbAddImageFilename = new FormData();
    fdbAddImageFilename.right = new FormAttachment( wbImageFilename, -margin );
    fdbAddImageFilename.top = new FormAttachment( wStepname, margin );
    wbAddImageFilename.setLayoutData( fdbAddImageFilename );

    wImageFilename = new TextVar( transMeta, wEmbeddedComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wImageFilename );
    wImageFilename.addModifyListener( lsMod );
    FormData fdImageFilename = new FormData();
    fdImageFilename.left = new FormAttachment( middle, 0 );
    fdImageFilename.top = new FormAttachment( wStepname, margin );
    fdImageFilename.right = new FormAttachment( wbAddImageFilename, -margin );
    wImageFilename.setLayoutData( fdImageFilename );

    // Whenever something changes, set the tooltip to the expanded version:
    wImageFilename.addModifyListener(
      e -> wImageFilename.setToolTipText( transMeta.environmentSubstitute( wImageFilename.getText() ) ) );

    wbImageFilename.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e ) {
        FileDialog dialog = new FileDialog( shell, SWT.OPEN );
        dialog.setFilterExtensions( new String[] { "*png;*PNG", "*jpeg;*jpg;*JPEG;*JPG", "*gif;*GIF", "*" } );
        if ( wImageFilename.getText() != null ) {
          dialog.setFileName( transMeta.environmentSubstitute( wImageFilename.getText() ) );
        }
        dialog.setFilterNames( IMAGES_FILE_TYPES );
        if ( dialog.open() != null ) {
          wImageFilename.setText( dialog.getFilterPath() + Const.FILE_SEPARATOR + dialog.getFileName() );
          wContentID.setText( Long.toString( Math.abs( secureRandom.nextLong() ), 32 ) );
        }
      }
    } );

    // ContentID
    wlContentID = new Label( wEmbeddedComp, SWT.RIGHT );
    wlContentID.setText( BaseMessages.getString( PKG, "MailDialog.ContentID.Label" ) );
    props.setLook( wlContentID );
    FormData fdlContentID = new FormData();
    fdlContentID.left = new FormAttachment( 0, 0 );
    fdlContentID.top = new FormAttachment( wImageFilename, margin );
    fdlContentID.right = new FormAttachment( middle, -margin );
    wlContentID.setLayoutData( fdlContentID );
    wContentID =
      new TextVar( transMeta, wEmbeddedComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER, BaseMessages.getString(
        PKG, "MailDialog.ContentID.Tooltip" ) );
    props.setLook( wContentID );
    wContentID.addModifyListener( lsMod );
    FormData fdContentID = new FormData();
    fdContentID.left = new FormAttachment( middle, 0 );
    fdContentID.top = new FormAttachment( wImageFilename, margin );
    fdContentID.right = new FormAttachment( wbAddImageFilename, -margin );
    wContentID.setLayoutData( fdContentID );

    // Buttons to the right of the screen...
    wbDeleteImageFilename = new Button( wEmbeddedComp, SWT.PUSH | SWT.CENTER );
    props.setLook( wbDeleteImageFilename );
    wbDeleteImageFilename.setText( BaseMessages.getString( PKG, "MailDialog.ImageFilenameDelete.Button" ) );
    wbDeleteImageFilename.setToolTipText( BaseMessages.getString( PKG, "MailDialog.ImageFilenameDelete.Tooltip" ) );
    FormData fdbDeleteImageFilename = new FormData();
    fdbDeleteImageFilename.right = new FormAttachment( 100, 0 );
    fdbDeleteImageFilename.top = new FormAttachment( wContentID, 40 );
    wbDeleteImageFilename.setLayoutData( fdbDeleteImageFilename );

    wbEditImageFilename = new Button( wEmbeddedComp, SWT.PUSH | SWT.CENTER );
    props.setLook( wbEditImageFilename );
    wbEditImageFilename.setText( BaseMessages.getString( PKG, "MailDialog.ImageFilenameEdit.Button" ) );
    wbEditImageFilename.setToolTipText( BaseMessages.getString( PKG, "MailDialog.ImageFilenameEdit.Tooltip" ) );
    FormData fdbEditImageFilename = new FormData();
    fdbEditImageFilename.right = new FormAttachment( 100, 0 );
    fdbEditImageFilename.left = new FormAttachment( wbDeleteImageFilename, 0, SWT.LEFT );
    fdbEditImageFilename.top = new FormAttachment( wbDeleteImageFilename, margin );
    wbEditImageFilename.setLayoutData( fdbEditImageFilename );

    wlFields = new Label( wEmbeddedComp, SWT.NONE );
    wlFields.setText( BaseMessages.getString( PKG, "MailDialog.Fields.Label" ) );
    props.setLook( wlFields );
    FormData fdlFields = new FormData();
    fdlFields.left = new FormAttachment( 0, 0 );
    fdlFields.right = new FormAttachment( middle, -margin );
    fdlFields.top = new FormAttachment( wContentID, margin );
    wlFields.setLayoutData( fdlFields );

    final int fieldsRows =
      input.getEmbeddedImages() == null ? 1 : ( input.getEmbeddedImages().length == 0 ? 0 : input
        .getEmbeddedImages().length );

    ColumnInfo[] colInf =
      new ColumnInfo[] {
        new ColumnInfo(
          BaseMessages.getString( PKG, "MailDialog.Fields.Image.Label" ), ColumnInfo.COLUMN_TYPE_TEXT, false ),
        new ColumnInfo(
          BaseMessages.getString( PKG, "MailDialog.Fields.ContentID.Label" ), ColumnInfo.COLUMN_TYPE_TEXT,
          false ), };

    colInf[ 0 ].setUsingVariables( true );
    colInf[ 0 ].setToolTip( BaseMessages.getString( PKG, "MailDialog.Fields.Image.Tooltip" ) );
    colInf[ 1 ].setUsingVariables( true );
    colInf[ 1 ].setToolTip( BaseMessages.getString( PKG, "MailDialog.Fields.ContentID.Tooltip" ) );

    wFields =
      new TableView(
        transMeta, wEmbeddedComp, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI, colInf, fieldsRows, lsMod,
        props );

    FormData fdFields = new FormData();
    fdFields.left = new FormAttachment( 0, 0 );
    fdFields.top = new FormAttachment( wlFields, margin );
    fdFields.right = new FormAttachment( wbEditImageFilename, -margin );
    fdFields.bottom = new FormAttachment( 100, -margin );
    wFields.setLayoutData( fdFields );

    // Add the file to the list of files...
    SelectionAdapter selA = new SelectionAdapter() {
      public void widgetSelected( SelectionEvent arg0 ) {
        wFields.add( wImageFilename.getText(), wContentID.getText() );
        wImageFilename.setText( "" );
        wContentID.setText( "" );
        wFields.removeEmptyRows();
        wFields.setRowNums();
        wFields.optWidth( true );
      }
    };
    wbAddImageFilename.addSelectionListener( selA );
    wImageFilename.addSelectionListener( selA );

    // Delete files from the list of files...
    wbDeleteImageFilename.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent arg0 ) {
        int[] idx = wFields.getSelectionIndices();
        wFields.remove( idx );
        wFields.removeEmptyRows();
        wFields.setRowNums();
      }
    } );

    // Edit the selected file & remove from the list...
    wbEditImageFilename.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent arg0 ) {
        int idx = wFields.getSelectionIndex();
        if ( idx >= 0 ) {
          String[] string = wFields.getItem( idx );
          wImageFilename.setText( string[0] );
          wContentID.setText( string[1] );
          wFields.remove( idx );
        }
        wFields.removeEmptyRows();
        wFields.setRowNums();
      }
    } );

    FormData fdEmbeddedComp = new FormData();
    fdEmbeddedComp.left = new FormAttachment( 0, 0 );
    fdEmbeddedComp.top = new FormAttachment( 0, 0 );
    fdEmbeddedComp.right = new FormAttachment( 100, 0 );
    fdEmbeddedComp.bottom = new FormAttachment( 100, 0 );
    wEmbeddedComp.setLayoutData( wEmbeddedComp );

    wEmbeddedComp.layout();
    wEmbeddedTab.setControl( wEmbeddedComp );

    // ///////////////////////////////////////////////////////////
    // / END OF embedded images TAB
    // ///////////////////////////////////////////////////////////

    FormData fdTabFolder = new FormData();
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
    lsCancel = e -> cancel();
    lsOK = e -> ok();

    wCancel.addListener( SWT.Selection, lsCancel );
    wOK.addListener( SWT.Selection, lsOK );

    lsDef = new SelectionAdapter() {
      public void widgetDefaultSelected( SelectionEvent e ) {
        ok();
      }
    };

    wStepname.addSelectionListener( lsDef );

    // Detect X or ALT-F4 or something that kills this window...
    shell.addShellListener( new ShellAdapter() {
      public void shellClosed( ShellEvent e ) {
        cancel();
      }
    } );

    // Set the shell size, based upon previous time...
    setSize();

    getData();
    activeIsFileDynamic();
    SetEnabledEncoding();
    activeUsePriority();
    setDynamicZip();
    setZip();
    setUseAuth();
    setUseGrantType();
    activeISAttachContentField();
    input.setChanged( changed );
    wTabFolder.setSelection( 0 );
    shell.open();
    while ( !shell.isDisposed() ) {
      if ( !display.readAndDispatch() ) {
        display.sleep();
      }
    }
    return stepname;
  }

  private void setDynamicZip() {
    wDynamicZipFileField.setEnabled( wZipFiles.getSelection() && wisZipFileDynamic.getSelection() );
    wlDynamicZipFileField.setEnabled( wZipFiles.getSelection() && wisZipFileDynamic.getSelection() );
  }

  private void setZip() {
    wZipFilename.setEnabled( wZipFiles.getSelection() );
    wZipSizeCondition.setEnabled( wZipFiles.getSelection() );
    wlIsZipFileDynamic.setEnabled( wZipFiles.getSelection() );
    wisZipFileDynamic.setEnabled( wZipFiles.getSelection() );
    setDynamicZip();
  }

  private void activeIsFileDynamic() {
    wlDynamicFilenameField.setEnabled( wIsFileDynamic.getSelection() );
    wDynamicFilenameField.setEnabled( wIsFileDynamic.getSelection() );
    wlDynamicWildcardField.setEnabled( wIsFileDynamic.getSelection() );
    wDynamicWildcardField.setEnabled( wIsFileDynamic.getSelection() );
    wWildcard.setEnabled( !wIsFileDynamic.getSelection() );
    wlWildcard.setEnabled( !wIsFileDynamic.getSelection() );
    wSourceFileFolderName.setEnabled( !wIsFileDynamic.getSelection() );
    wlSourceFileFolderName.setEnabled( !wIsFileDynamic.getSelection() );
    wbFileFolderName.setEnabled( !wIsFileDynamic.getSelection() );
    wbSourceFolder.setEnabled( !wIsFileDynamic.getSelection() );
  }

  private void getPreviousFields() {
    try {
      if ( !getPreviousFields ) {
        getPreviousFields = true;

        String destination = null;
        if ( wDestination != null ) {
          destination = wDestination.getText();
          wDestination.removeAll();
        }

        String destinationCc = null;
        if ( wDestinationCc != null ) {
          destinationCc = wDestinationCc.getText();
          wDestinationCc.removeAll();
        }

        String destinationBcc = null;
        if ( wDestinationBcc != null ) {
          destinationBcc = wDestinationBcc.getText();
          wDestinationBcc.removeAll();
        }

        String replyToAddress = null;
        if ( wReplyToAddresses != null ) {
          replyToAddress = wReplyToAddresses.getText();
          wReplyToAddresses.removeAll();
        }

        String replyName = null;
        if ( wReplyName != null ) {
          replyName = wReplyName.getText();
          wReplyName.removeAll();
        }

        String replyAddress = null;
        if ( wReply != null ) {
          replyAddress = wReply.getText();
          wReply.removeAll();
        }

        String person = null;
        if ( wPerson != null ) {
          person = wPerson.getText();
          wPerson.removeAll();
        }

        String phone = null;
        if ( wPhone != null ) {
          phone = wPhone.getText();
          wPhone.removeAll();
        }

        String serverName = null;
        if ( wServer != null ) {
          serverName = wServer.getText();
          wServer.removeAll();
        }

        String port = null;
        if ( wPort != null ) {
          port = wPort.getText();
          wPort.removeAll();
        }

        String authUser = null;
        if ( wAuthUser != null ) {
          authUser = wAuthUser.getText();
          wAuthUser.removeAll();
        }

        String authPass = null;
        if ( wAuthPass != null ) {
          authPass = wAuthPass.getText();
          wAuthPass.removeAll();
        }

        String subject = null;
        if ( wSubject != null ) {
          subject = wSubject.getText();
          wSubject.removeAll();
        }

        String comment = null;
        if ( wComment != null ) {
          comment = wComment.getText();
          wComment.removeAll();
        }

        String dynamicFile = null;
        if ( wDynamicFilenameField != null ) {
          dynamicFile = wDynamicFilenameField.getText();
          wDynamicFilenameField.removeAll();
        }

        String dynamicWildcard = null;
        if ( wDynamicWildcardField != null ) {
          dynamicWildcard = wDynamicWildcardField.getText();
          wDynamicWildcardField.removeAll();
        }

        String dynamicZipFile = null;
        if ( wDynamicZipFileField != null ) {
          dynamicZipFile = wDynamicZipFileField.getText();
          wDynamicZipFileField.removeAll();
        }

        String attachContent = null;
        if ( wAttachContentField != null ) {
          attachContent = wAttachContentField.getText();
          wAttachContentField.removeAll();
        }

        String attachContentFileName = null;
        if ( wAttachContentFileNameField != null ) {
          attachContentFileName = wAttachContentFileNameField.getText();
          wAttachContentFileNameField.removeAll();
        }

        RowMetaInterface r = transMeta.getPrevStepFields( stepname );
        if ( r != null ) {
          String[] fieldNames = r.getFieldNames();
          wDestination.setItems( fieldNames );
          wDestinationCc.setItems( fieldNames );
          wDestinationBcc.setItems( fieldNames );
          wReplyName.setItems( fieldNames );
          wReply.setItems( fieldNames );
          wPerson.setItems( fieldNames );
          wPhone.setItems( fieldNames );
          wServer.setItems( fieldNames );
          wPort.setItems( fieldNames );
          wAuthUser.setItems( fieldNames );
          wAuthPass.setItems( fieldNames );
          wSubject.setItems( fieldNames );
          wComment.setItems( fieldNames );
          wDynamicFilenameField.setItems( fieldNames );
          wDynamicWildcardField.setItems( fieldNames );
          wDynamicZipFileField.setItems( fieldNames );
          wReplyToAddresses.setItems( fieldNames );
          wAttachContentField.setItems( fieldNames );
          wAttachContentFileNameField.setItems( fieldNames );
        }

        if ( destination != null ) {
          wDestination.setText( destination );
        }
        if ( destinationCc != null ) {
          wDestinationCc.setText( destinationCc );
        }
        if ( destinationBcc != null ) {
          wDestinationBcc.setText( destinationBcc );
        }
        if ( replyName != null ) {
          wReplyName.setText( replyName );
        }
        if ( replyAddress != null ) {
          wReply.setText( replyAddress );
        }
        if ( person != null ) {
          wPerson.setText( person );
        }
        if ( phone != null ) {
          wPhone.setText( phone );
        }
        if ( serverName != null ) {
          wServer.setText( serverName );
        }
        if ( port != null ) {
          wPort.setText( port );
        }
        if ( authUser != null ) {
          wAuthUser.setText( authUser );
        }
        if ( authPass != null ) {
          wAuthPass.setText( authPass );
        }
        if ( subject != null ) {
          wSubject.setText( subject );
        }
        if ( comment != null ) {
          wComment.setText( comment );
        }
        if ( dynamicFile != null ) {
          wDynamicFilenameField.setText( dynamicFile );
        }
        if ( dynamicWildcard != null ) {
          wDynamicWildcardField.setText( dynamicWildcard );
        }
        if ( dynamicZipFile != null ) {
          wDynamicZipFileField.setText( dynamicZipFile );
        }
        if ( replyToAddress != null ) {
          wReplyToAddresses.setText( replyToAddress );
        }
        if ( attachContent != null ) {
          wAttachContentField.setText( attachContent );
        }
        if ( attachContentFileName != null ) {
          wAttachContentFileNameField.setText( attachContentFileName );
        }
      }
    } catch ( KettleException ke ) {
      new ErrorDialog(
        shell, BaseMessages.getString( PKG, "MailDialog.FailedToGetFields.DialogTitle" ), BaseMessages
          .getString( PKG, "MailDialog.FailedToGetFields.DialogMessage" ), ke );
    }
  }

  private void activeUsePriority() {
    wlPriority.setEnabled( wUsePriority.getSelection() );
    wPriority.setEnabled( wUsePriority.getSelection() );
    wlImportance.setEnabled( wUsePriority.getSelection() );
    wImportance.setEnabled( wUsePriority.getSelection() );
    wlSensitivity.setEnabled( wUsePriority.getSelection() );
    wSensitivity.setEnabled( wUsePriority.getSelection() );
  }

  private void SetEnabledEncoding() {
    wEncoding.setEnabled( wUseHTML.getSelection() );
    wlEncoding.setEnabled( wUseHTML.getSelection() );
    wlImageFilename.setEnabled( wUseHTML.getSelection() );
    wlImageFilename.setEnabled( wUseHTML.getSelection() );
    wbImageFilename.setEnabled( wUseHTML.getSelection() );
    wbAddImageFilename.setEnabled( wUseHTML.getSelection() );
    wImageFilename.setEnabled( wUseHTML.getSelection() );
    wlContentID.setEnabled( wUseHTML.getSelection() );
    wContentID.setEnabled( wUseHTML.getSelection() );
    wbDeleteImageFilename.setEnabled( wUseHTML.getSelection() );
    wbEditImageFilename.setEnabled( wUseHTML.getSelection() );
    wlFields.setEnabled( wUseHTML.getSelection() );
    wFields.setEnabled( wUseHTML.getSelection() );
  }

  protected void setSecureConnectiontype() {
    wSecureConnectionType.setEnabled( wUseSecAuth.getSelection() );
    wlSecureConnectionType.setEnabled( wUseSecAuth.getSelection() );
  }

  private void setEncodings() {
    // Encoding of the text file:
    if ( !gotEncodings ) {
      gotEncodings = true;

      wEncoding.removeAll();
      ArrayList<Charset> values = new ArrayList<>( Charset.availableCharsets().values() );
      for ( Charset charSet : values ) {
        wEncoding.add( charSet.displayName() );
      }

      // Now select the default!
      String defEncoding = Const.getEnvironmentVariable( "file.encoding", "UTF-8" );
      int idx = Const.indexOfString( defEncoding, wEncoding.getItems() );
      if ( idx >= 0 ) {
        wEncoding.select( idx );
      }
    }
  }

  protected void setUseAuth() {
    String selectedAuth = wUseAuth.getText();

    if ( MailMeta.AUTENTICATION_NONE.equals( selectedAuth ) ) {
      wAuthClientId.setEnabled( false );
      wAuthSecretKey.setEnabled( false );
      wlAuthUser.setEnabled( false );
      wAuthUser.setEnabled( false );
      wlAuthPass.setEnabled( false );
      wAuthPass.setEnabled( false );
      wAuthScope.setEnabled( false );
      wlGrantType.setEnabled( false );
      grantType.setEnabled( false );
      wAuthTokenUrl.setEnabled( false );
      wAuthorizationCode.setEnabled( false );
      wRedirectUri.setEnabled( false );
      wAuthRefreshToken.setEnabled( false );
      wlUseSecAuth.setEnabled( false );
      wUseSecAuth.setEnabled( false );
      wlSecureConnectionType.setEnabled( false );
      wSecureConnectionType.setEnabled( false );
    } else if ( MailMeta.AUTENTICATION_BASIC.equals( selectedAuth ) ) {
      wAuthClientId.setEnabled( false );
      wAuthSecretKey.setEnabled( false );
      wlAuthUser.setEnabled( true );
      wAuthUser.setEnabled( true );
      wlAuthPass.setEnabled( true );
      wAuthPass.setEnabled( true );
      wAuthScope.setEnabled( false );
      wlGrantType.setEnabled( false );
      grantType.setEnabled( false );
      wAuthTokenUrl.setEnabled( false );
      wAuthorizationCode.setEnabled( false );
      wRedirectUri.setEnabled( false );
      wAuthRefreshToken.setEnabled( false );
      wlUseSecAuth.setEnabled( true );
      wUseSecAuth.setEnabled( true );
      setSecureConnectiontype();
    } else if ( MailMeta.AUTENTICATION_OAUTH.equals( selectedAuth ) ) {
      wAuthClientId.setEnabled( true );
      wAuthSecretKey.setEnabled( true );
      wlAuthUser.setEnabled( true );
      wAuthUser.setEnabled( true );
      wlAuthPass.setEnabled( false );
      wAuthPass.setEnabled( false );
      wAuthScope.setEnabled( true );
      wlGrantType.setEnabled( true );
      grantType.setEnabled( true );
      wlUseSecAuth.setEnabled( true );
      wUseSecAuth.setEnabled( true );
      setUseGrantType();
    } else {
      setSecureConnectiontype();
    }
  }

  protected void setUseGrantType() {
    String selectedAuth = grantType.getText();

    if ( selectedAuth.equals( MailMeta.GRANTTYPE_CLIENTCREDENTIALS ) ) {
      wAuthTokenUrl.setEnabled( true );
      wAuthorizationCode.setEnabled( false );
      wRedirectUri.setEnabled( false );
      wAuthRefreshToken.setEnabled( false );
    } else if ( selectedAuth.equals( MailMeta.GRANTTYPE_REFRESH_TOKEN ) ) {
      wAuthTokenUrl.setEnabled( true );
      wAuthorizationCode.setEnabled( false );
      wRedirectUri.setEnabled( false );
      wAuthRefreshToken.setEnabled( true );
    } else if ( selectedAuth.equals( MailMeta.GRANTTYPE_AUTHORIZATION_CODE ) ) {
      wAuthTokenUrl.setEnabled( true );
      wAuthorizationCode.setEnabled( true );
      wRedirectUri.setEnabled( true );
      wAuthRefreshToken.setEnabled( false );
    } else {
      setSecureConnectiontype();
    }
  }

  /**
   * Copy information from the meta-data input to the dialog fields.
   */
  public void getData() {
    wIsAttachContentField.setSelection( input.isAttachContentFromField() );
    if ( input.getAttachContentField() != null ) {
      wAttachContentField.setText( input.getAttachContentField() );
    }
    if ( input.getAttachContentFileNameField() != null ) {
      wAttachContentFileNameField.setText( input.getAttachContentFileNameField() );
    }
    if ( input.getDestination() != null ) {
      wDestination.setText( input.getDestination() );
    }
    if ( input.getDestinationCc() != null ) {
      wDestinationCc.setText( input.getDestinationCc() );
    }
    if ( input.getDestinationBCc() != null ) {
      wDestinationBcc.setText( input.getDestinationBCc() );
    }
    if ( input.getServer() != null ) {
      wServer.setText( input.getServer() );
    }
    if ( input.getPort() != null ) {
      wPort.setText( input.getPort() );
    }
    if ( input.getReplyAddress() != null ) {
      wReply.setText( input.getReplyAddress() );
    }
    if ( input.getReplyName() != null ) {
      wReplyName.setText( input.getReplyName() );
    }
    if ( input.getSubject() != null ) {
      wSubject.setText( input.getSubject() );
    }
    if ( input.getContactPerson() != null ) {
      wPerson.setText( input.getContactPerson() );
    }
    if ( input.getContactPhone() != null ) {
      wPhone.setText( input.getContactPhone() );
    }
    if ( input.getComment() != null ) {
      wComment.setText( input.getComment() );
    }

    wAddDate.setSelection( input.getIncludeDate() );
    wIsFileDynamic.setSelection( input.isDynamicFilename() );
    if ( input.getDynamicFieldname() != null ) {
      wDynamicFilenameField.setText( input.getDynamicFieldname() );
    }
    if ( input.getDynamicWildcard() != null ) {
      wDynamicWildcardField.setText( input.getDynamicWildcard() );
    }

    if ( input.getSourceFileFoldername() != null ) {
      wSourceFileFolderName.setText( input.getSourceFileFoldername() );
    }

    if ( input.getSourceWildcard() != null ) {
      wWildcard.setText( input.getSourceWildcard() );
    }

    wIncludeSubFolders.setSelection( input.isIncludeSubFolders() );

    wZipFiles.setSelection( input.isZipFiles() );
    if ( input.getZipFilename() != null ) {
      wZipFilename.setText( input.getZipFilename() );
    }

    if ( input.getZipLimitSize() != null ) {
      wZipSizeCondition.setText( input.getZipLimitSize() );
    } else {
      wZipSizeCondition.setText( "0" );
    }

    wisZipFileDynamic.setSelection( input.isZipFilenameDynamic() );
    if ( input.getDynamicZipFilenameField() != null ) {
      wDynamicZipFileField.setText( input.getDynamicZipFilenameField() );
    }
    if( input.isUsingAuthentication() !=null) {
      wUseAuth.setText(input.isUsingAuthentication());
    }
    wUseSecAuth.setSelection( input.isUsingSecureAuthentication() );
    if ( input.getAuthenticationUser() != null ) {
      wAuthUser.setText( input.getAuthenticationUser() );
    }
    if ( input.getAuthenticationPassword() != null ) {
      wAuthPass.setText( input.getAuthenticationPassword() );
    }
    if( input.getClientId()!= null ) {
      wAuthClientId.setText( input.getClientId() );
    }
    if( input.getSecretKey()!= null ) {
      wAuthSecretKey.setText( input.getSecretKey() );
    }
    if( input.getScope()!= null ) {
      wAuthScope.setText( input.getScope() );
    }
    if( input.getTokenUrl()!= null ) {
      wAuthTokenUrl.setText( input.getTokenUrl() );
    }
    if( input.getAuthorization_code()!= null ) {
      wAuthorizationCode.setText( input.getAuthorization_code() );
    }
    if( input.getRedirectUri()!= null ) {
      wRedirectUri.setText( input.getRedirectUri() );
    }
    if( input.getRefresh_token()!= null ) {
      wAuthRefreshToken.setText( input.getRefresh_token() );
    }
    if( input.getGrant_type()!= null ) {
      grantType.setText( input.getGrant_type() );
    }

    wOnlyComment.setSelection( input.isOnlySendComment() );

    wUseHTML.setSelection( input.isUseHTML() );

    if ( input.getEncoding() != null ) {
      wEncoding.setText( input.getEncoding() );
    } else {
      wEncoding.setText( "UTF-8" );
    }

    // Secure connection type
    if ( input.getSecureConnectionType() != null ) {
      wSecureConnectionType.setText( input.getSecureConnectionType() );
    } else {
      wSecureConnectionType.setText( "SSL" );
    }

    wUsePriority.setSelection( input.isUsePriority() );

    // Priority

    if ( input.getPriority() != null ) {
      if ( input.getPriority().equals( "low" ) ) {
        wPriority.select( 0 ); // Low
      } else if ( input.getPriority().equals( "normal" ) ) {
        wPriority.select( 1 ); // Normal
      } else {
        wPriority.select( 2 ); // Default High
      }
    } else {
      wPriority.select( 3 ); // Default High
    }

    // Importance
    if ( input.getImportance() != null ) {
      if ( input.getImportance().equals( "low" ) ) {
        wImportance.select( 0 ); // Low
      } else if ( input.getImportance().equals( "normal" ) ) {
        wImportance.select( 1 ); // Normal
      } else {
        wImportance.select( 2 ); // Default High
      }
    } else {
      wImportance.select( 3 ); // Default High
    }

    if ( input.getReplyToAddresses() != null ) {
      wReplyToAddresses.setText( input.getReplyToAddresses() );
    }

    // Sensitivity
    if ( input.getSensitivity() != null ) {
      if ( input.getSensitivity().equals( "personal" ) ) {
        wSensitivity.select( 1 );
      } else if ( input.getSensitivity().equals( "private" ) ) {
        wSensitivity.select( 2 );
      } else if ( input.getSensitivity().equals( "company-confidential" ) ) {
        wSensitivity.select( 3 );
      } else {
        wSensitivity.select( 0 );
      }
    } else {
      wSensitivity.select( 0 ); // Default normal
    }

    if ( input.getEmbeddedImages() != null ) {
      for ( int i = 0; i < input.getEmbeddedImages().length; i++ ) {
        TableItem ti = wFields.table.getItem( i );
        if ( input.getEmbeddedImages()[i] != null ) {
          ti.setText( 1, input.getEmbeddedImages()[i] );
        }
        if ( input.getContentIds()[i] != null ) {
          ti.setText( 2, input.getContentIds()[i] );
        }
      }
      wFields.setRowNums();
      wFields.optWidth( true );
    }

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

    stepname = wStepname.getText(); // return value
    input.setAttachContentFromField( wIsAttachContentField.getSelection() );
    input.setAttachContentField( wAttachContentField.getText() );
    input.setAttachContentFileNameField( wAttachContentFileNameField.getText() );
    input.setDestination( wDestination.getText() );
    input.setDestinationCc( wDestinationCc.getText() );
    input.setDestinationBCc( wDestinationBcc.getText() );
    input.setServer( wServer.getText() );
    input.setPort( wPort.getText() );
    input.setReplyAddress( wReply.getText() );
    input.setReplyName( wReplyName.getText() );
    input.setSubject( wSubject.getText() );
    input.setContactPerson( wPerson.getText() );
    input.setContactPhone( wPhone.getText() );
    input.setComment( wComment.getText() );

    input.setIncludeSubFolders( wIncludeSubFolders.getSelection() );
    input.setIncludeDate( wAddDate.getSelection() );
    input.setisDynamicFilename( wIsFileDynamic.getSelection() );
    input.setDynamicFieldname( wDynamicFilenameField.getText() );
    input.setDynamicWildcard( wDynamicWildcardField.getText() );

    input.setDynamicZipFilenameField( wDynamicZipFileField.getText() );

    input.setSourceFileFoldername( wSourceFileFolderName.getText() );
    input.setSourceWildcard( wWildcard.getText() );

    input.setZipLimitSize( wZipSizeCondition.getText() );

    input.setZipFilenameDynamic( wisZipFileDynamic.getSelection() );

    input.setZipFilename( wZipFilename.getText() );
    input.setZipFiles( wZipFiles.getSelection() );
    input.setAuthenticationUser( wAuthUser.getText() );
    input.setAuthenticationPassword( wAuthPass.getText() );
    input.setClientId( wAuthClientId.getText() );
    input.setSecretKey( wAuthSecretKey.getText() );
    input.setScope( wAuthScope.getText() );
    input.setTokenUrl( wAuthTokenUrl.getText() );
    input.setAuthorization_code( wAuthorizationCode.getText() );
    input.setRedirectUri( wRedirectUri.getText() );
    input.setRefresh_token( wAuthRefreshToken.getText() );
    input.setUsingAuthentication( wUseAuth.getText() );
    input.setGrant_type( grantType.getText() );
    input.setUsingSecureAuthentication( wUseSecAuth.getSelection() );
    input.setOnlySendComment( wOnlyComment.getSelection() );
    input.setUseHTML( wUseHTML.getSelection() );
    input.setUsePriority( wUsePriority.getSelection() );

    input.setEncoding( wEncoding.getText() );
    input.setPriority( wPriority.getText() );

    // Priority
    if ( wPriority.getSelectionIndex() == 0 ) {
      input.setPriority( "low" );
    } else if ( wPriority.getSelectionIndex() == 1 ) {
      input.setPriority( "normal" );
    } else {
      input.setPriority( "high" );
    }

    // Importance
    if ( wImportance.getSelectionIndex() == 0 ) {
      input.setImportance( "low" );
    } else if ( wImportance.getSelectionIndex() == 1 ) {
      input.setImportance( "normal" );
    } else {
      input.setImportance( "high" );
    }

    // Sensitivity
    if ( wSensitivity.getSelectionIndex() == 1 ) {
      input.setSensitivity( "personal" );
    } else if ( wSensitivity.getSelectionIndex() == 2 ) {
      input.setSensitivity( "private" );
    } else if ( wSensitivity.getSelectionIndex() == 3 ) {
      input.setSensitivity( "company-confidential" );
    } else {
      input.setSensitivity( "normal" ); // default is normal
    }

    // Secure Connection type
    input.setSecureConnectionType( wSecureConnectionType.getText() );
    input.setReplyToAddresses( wReplyToAddresses.getText() );

    int nrItems = wFields.nrNonEmpty();
    int nr = 0;
    for ( int i = 0; i < nrItems; i++ ) {
      String arg = wFields.getNonEmpty( i ).getText( 1 );
      if ( arg != null && !arg.isEmpty() ) {
        nr++;
      }
    }

    input.allocate( nr );

    nr = 0;
    for ( int i = 0; i < nrItems; i++ ) {
      String image = wFields.getNonEmpty( i ).getText( 1 );
      String id = wFields.getNonEmpty( i ).getText( 2 );
      input.setEmbeddedImage( i, image );
      input.setContentIds( i, id );
      nr++;
    }

    dispose();
  }

  private void activeISAttachContentField() {
    wOriginFiles.setEnabled( !wIsAttachContentField.getSelection() );
    wZipGroup.setEnabled( !wIsAttachContentField.getSelection() );
    wlAttachContentField.setEnabled( wIsAttachContentField.getSelection() );
    wAttachContentField.setEnabled( wIsAttachContentField.getSelection() );
    wlAttachContentFileNameField.setEnabled( wIsAttachContentField.getSelection() );
    wAttachContentFileNameField.setEnabled( wIsAttachContentField.getSelection() );
  }
}
