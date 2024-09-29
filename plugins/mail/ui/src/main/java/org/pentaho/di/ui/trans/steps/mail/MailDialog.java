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

package org.pentaho.di.ui.trans.steps.mail;

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
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.annotations.PluginDialog;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
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
import java.util.ArrayList;
import java.util.Random;

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

  private static final String[] FILETYPES =
    new String[] { BaseMessages.getString( PKG, "MailDialog.Filetype.All" ) };

  private static final String[] IMAGES_FILE_TYPES = new String[] {
    BaseMessages.getString( PKG, "MailDialog.Filetype.Png" ),
    BaseMessages.getString( PKG, "MailDialog.Filetype.Jpeg" ),
    BaseMessages.getString( PKG, "MailDialog.Filetype.Gif" ),
    BaseMessages.getString( PKG, "MailDialog.Filetype.All" ) };

  private boolean gotEncodings = false;

  private Group wOriginFiles, wZipGroup, wAttachedContent;

  private Button wisFileDynamic, wisattachContentField;

  private Label wlisFileDynamic, wlDynamicFilenameField, wlisattachContentField;
  private Label wlattachContentField, wlattachContentFileNameField;
  private CCombo wDynamicFilenameField, wattachContentField, wattachContentFileNameField;

  private Label wlDynamicWildcardField;
  private CCombo wDynamicWildcardField;

  private CTabFolder wTabFolder;
  private Composite wGeneralComp, wContentComp, wAttachedComp, wMessageComp, wembeddedComp;
  private CTabItem wGeneralTab, wContentTab, wAttachedTab, wMessageTab, wembeddedTab;
  private FormData fdGeneralComp, fdContentComp, fdAttachedComp, fdMessageComp, fdembeddedComp;
  private FormData fdTabFolder;

  private Label wlisZipFileDynamic;

  private Label wlReplyToAddresses;
  private FormData fdReplyToAddresses;
  private CCombo wReplyToAddresses;
  private FormData fdlReplyToAddresses;

  private Group wDestinationGroup, wReplyGroup, wServerGroup, wAuthentificationGroup;
  private Group wMessageSettingsGroup, wMessageGroup;

  private LabelText wName;

  private FormData fdlDestination, fdlDestinationBCc;

  private CCombo wDestination;

  private Label wlDestination;

  private CCombo wDestinationCc, wDestinationBCc;

  private Label wlDestinationCc, wlDestinationBCc;

  private FormData fdlDestinationCc;

  private FormData fdDestination;

  private FormData fdDestinationCc;

  private FormData fdDestinationBCc;

  private CCombo wServer;

  private Label wlServer;

  private FormData fdlServer;

  private FormData fdServer;

  private CCombo wPort;

  private Label wlPort;

  private FormData fdlPort;

  private FormData fdPort;

  private Label wlUseAuth;

  private Button wUseAuth;

  private FormData fdlUseAuth, fdUseAuth;

  private Label wlUseSecAuth;

  private Button wUseSecAuth;

  private FormData fdlUseSecAuth, fdUseSecAuth;

  private CCombo wAuthUser;

  private Label wlAuthUser;

  private FormData fdAuthUser;

  private CCombo wAuthPass;

  private Label wlAuthPass;

  private FormData fdlAuthPass;

  private FormData fdAuthPass;

  private CCombo wReply, wReplyName;

  private FormData fdReply, fdReplyName;

  private CCombo wSubject;

  private Label wlSubject;

  private FormData fdlSubject;

  private FormData fdSubject;

  private Label wlAddDate;

  private Button wAddDate;

  private FormData fdlAddDate, fdAddDate;

  private Label wlReply, wlReplyName;

  private FormData fdlReply, fdlReplyName;

  private FormData fdlAuthUser;

  private CCombo wPerson;

  private Label wlWildcard;

  private TextVar wWildcard;

  private FormData fdlWildcard;

  private FormData fdWildcard;

  private Label wlPerson, wlPhone;

  private FormData fdlPerson, fdlPhone;

  private FormData fdPerson;

  private CCombo wPhone;

  private FormData fdPhone;

  private CCombo wComment;

  private Label wlComment;

  private Label wlSourceFileFoldername;
  private Button wbFileFoldername, wbSourceFolder;
  private TextVar wSourceFileFoldername;
  private FormData fdlSourceFileFoldername, fdbSourceFileFoldername, fdSourceFileFoldername, fdbSourceFolder;

  private Label wlincludeSubFolders;
  private Button wincludeSubFolders;
  private FormData fdlincludeSubFolders, fdincludeSubFolders;

  private FormData fdlComment, fdComment;

  private Label wlOnlyComment, wlUseHTML, wlUsePriority;

  private Button wOnlyComment, wUseHTML, wUsePriority;

  private FormData fdlOnlyComment, fdOnlyComment, fdlUseHTML, fdUseHTML, fdUsePriority;

  private Label wlEncoding;
  private CCombo wEncoding;
  private FormData fdlEncoding, fdEncoding;

  private Label wlSecureConnectionType;
  private CCombo wSecureConnectionType;
  private FormData fdlSecureConnectionType, fdSecureConnectionType;

  private Label wlPriority;
  private CCombo wPriority;
  private FormData fdlPriority, fdPriority;

  private Label wlImportance;
  private CCombo wImportance;
  private FormData fdlImportance, fdImportance;

  private Label wlSensitivity;
  private CCombo wSensitivity;
  private FormData fdlSensitivity, fdSensitivity;

  private Label wlZipFiles;

  private FormData fdlisZipFileDynamic;

  private Label wlDynamicZipFileField;

  private CCombo wDynamicZipFileField;

  private FormData fdlDynamicZipFileField;

  private FormData fdDynamicZipFileField;

  private FormData fdisZipFileDynamic;

  private Button wisZipFileDynamic;

  private Button wZipFiles;

  private FormData fdlZipFiles, fdZipFiles;

  private LabelTextVar wZipFilename;

  private LabelTextVar wZipSizeCondition;

  private FormData fdZipFilename;

  private FormData fdZipSizeCondition;

  private Label wlImageFilename, wlContentID, wlFields;
  private Button wbImageFilename, wbaImageFilename, wbdImageFilename, wbeImageFilename;
  private TextVar wImageFilename, wContentID;
  private TableView wFields;

  private boolean getpreviousFields = false;

  private MailMeta input;

  public MailDialog( Shell parent, Object in, TransMeta tr, String sname ) {
    super( parent, (BaseStepMeta) in, tr, sname );
    input = (MailMeta) in;
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

    wTabFolder = new CTabFolder( shell, SWT.BORDER );
    props.setLook( wTabFolder, Props.WIDGET_STYLE_TAB );

    // ////////////////////////
    // START OF GENERAL TAB ///
    // ////////////////////////

    wGeneralTab = new CTabItem( wTabFolder, SWT.NONE );
    wGeneralTab.setText( BaseMessages.getString( PKG, "Mail.Tab.General.Label" ) );

    wGeneralComp = new Composite( wTabFolder, SWT.NONE );
    props.setLook( wGeneralComp );

    FormLayout generalLayout = new FormLayout();
    generalLayout.marginWidth = 3;
    generalLayout.marginHeight = 3;
    wGeneralComp.setLayout( generalLayout );

    // ////////////////////////
    // START OF Destination Settings GROUP
    // ////////////////////////

    wDestinationGroup = new Group( wGeneralComp, SWT.SHADOW_NONE );
    props.setLook( wDestinationGroup );
    wDestinationGroup.setText( BaseMessages.getString( PKG, "Mail.Group.DestinationAddress.Label" ) );

    FormLayout destinationgroupLayout = new FormLayout();
    destinationgroupLayout.marginWidth = 10;
    destinationgroupLayout.marginHeight = 10;
    wDestinationGroup.setLayout( destinationgroupLayout );

    // Destination
    wlDestination = new Label( wDestinationGroup, SWT.RIGHT );
    wlDestination.setText( BaseMessages.getString( PKG, "Mail.DestinationAddress.Label" ) );
    props.setLook( wlDestination );
    fdlDestination = new FormData();
    fdlDestination.left = new FormAttachment( 0, -margin );
    fdlDestination.top = new FormAttachment( wStepname, margin );
    fdlDestination.right = new FormAttachment( middle, -2 * margin );
    wlDestination.setLayoutData( fdlDestination );

    wDestination = new CCombo( wDestinationGroup, SWT.BORDER | SWT.READ_ONLY );
    wDestination.setEditable( true );
    props.setLook( wDestination );
    wDestination.addModifyListener( lsMod );
    fdDestination = new FormData();
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
    wlDestinationCc = new Label( wDestinationGroup, SWT.RIGHT );
    wlDestinationCc.setText( BaseMessages.getString( PKG, "Mail.DestinationAddressCc.Label" ) );
    props.setLook( wlDestinationCc );
    fdlDestinationCc = new FormData();
    fdlDestinationCc.left = new FormAttachment( 0, -margin );
    fdlDestinationCc.top = new FormAttachment( wDestination, margin );
    fdlDestinationCc.right = new FormAttachment( middle, -2 * margin );
    wlDestinationCc.setLayoutData( fdlDestinationCc );

    wDestinationCc = new CCombo( wDestinationGroup, SWT.BORDER | SWT.READ_ONLY );
    wDestinationCc.setEditable( true );
    props.setLook( wDestinationCc );
    wDestinationCc.addModifyListener( lsMod );
    fdDestinationCc = new FormData();
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
    wlDestinationBCc = new Label( wDestinationGroup, SWT.RIGHT );
    wlDestinationBCc.setText( BaseMessages.getString( PKG, "Mail.DestinationAddressBCc.Label" ) );
    props.setLook( wlDestinationBCc );
    fdlDestinationBCc = new FormData();
    fdlDestinationBCc.left = new FormAttachment( 0, -margin );
    fdlDestinationBCc.top = new FormAttachment( wDestinationCc, margin );
    fdlDestinationBCc.right = new FormAttachment( middle, -2 * margin );
    wlDestinationBCc.setLayoutData( fdlDestinationBCc );

    wDestinationBCc = new CCombo( wDestinationGroup, SWT.BORDER | SWT.READ_ONLY );
    wDestinationBCc.setEditable( true );
    props.setLook( wDestinationBCc );
    wDestinationBCc.addModifyListener( lsMod );
    fdDestinationBCc = new FormData();
    fdDestinationBCc.left = new FormAttachment( middle, -margin );
    fdDestinationBCc.top = new FormAttachment( wDestinationCc, margin );
    fdDestinationBCc.right = new FormAttachment( 100, -margin );
    wDestinationBCc.setLayoutData( fdDestinationBCc );
    wDestinationBCc.addFocusListener( new FocusListener() {
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

    wReplyGroup = new Group( wGeneralComp, SWT.SHADOW_NONE );
    props.setLook( wReplyGroup );
    wReplyGroup.setText( BaseMessages.getString( PKG, "MailDialog.Group.Reply.Label" ) );

    FormLayout replygroupLayout = new FormLayout();
    replygroupLayout.marginWidth = 10;
    replygroupLayout.marginHeight = 10;
    wReplyGroup.setLayout( replygroupLayout );

    // ReplyName
    wlReplyName = new Label( wReplyGroup, SWT.RIGHT );
    wlReplyName.setText( BaseMessages.getString( PKG, "Mail.ReplyName.Label" ) );
    props.setLook( wlReplyName );
    fdlReplyName = new FormData();
    fdlReplyName.left = new FormAttachment( 0, -margin );
    fdlReplyName.top = new FormAttachment( wDestinationGroup, margin );
    fdlReplyName.right = new FormAttachment( middle, -2 * margin );
    wlReplyName.setLayoutData( fdlReplyName );

    wReplyName = new CCombo( wReplyGroup, SWT.BORDER | SWT.READ_ONLY );
    wReplyName.setEditable( true );
    props.setLook( wReplyName );
    wReplyName.addModifyListener( lsMod );
    fdReplyName = new FormData();
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
    wlReply = new Label( wReplyGroup, SWT.RIGHT );
    wlReply.setText( BaseMessages.getString( PKG, "Mail.ReplyAddress.Label" ) );
    props.setLook( wlReply );
    fdlReply = new FormData();
    fdlReply.left = new FormAttachment( 0, -margin );
    fdlReply.top = new FormAttachment( wReplyName, margin );
    fdlReply.right = new FormAttachment( middle, -2 * margin );
    wlReply.setLayoutData( fdlReply );

    wReply = new CCombo( wReplyGroup, SWT.BORDER | SWT.READ_ONLY );
    wReply.setEditable( true );
    props.setLook( wReply );
    wReply.addModifyListener( lsMod );
    fdReply = new FormData();
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
    wlReplyToAddresses = new Label( wGeneralComp, SWT.RIGHT );
    wlReplyToAddresses.setText( BaseMessages.getString( PKG, "MailDialog.ReplyToAddresses.Label" ) );
    props.setLook( wlReplyToAddresses );
    fdlReplyToAddresses = new FormData();
    fdlReplyToAddresses.left = new FormAttachment( 0, -margin );
    fdlReplyToAddresses.top = new FormAttachment( wReplyGroup, 2 * margin );
    fdlReplyToAddresses.right = new FormAttachment( middle, -2 * margin );
    wlReplyToAddresses.setLayoutData( fdlReplyToAddresses );

    wReplyToAddresses = new CCombo( wGeneralComp, SWT.BORDER | SWT.READ_ONLY );
    wReplyToAddresses.setEditable( true );
    props.setLook( wReplyToAddresses );
    wReplyToAddresses.addModifyListener( lsMod );
    fdReplyToAddresses = new FormData();
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
    wlPerson = new Label( wGeneralComp, SWT.RIGHT );
    wlPerson.setText( BaseMessages.getString( PKG, "Mail.Contact.Label" ) );
    props.setLook( wlPerson );
    fdlPerson = new FormData();
    fdlPerson.left = new FormAttachment( 0, -margin );
    fdlPerson.top = new FormAttachment( wReplyToAddresses, 2 * margin );
    fdlPerson.right = new FormAttachment( middle, -2 * margin );
    wlPerson.setLayoutData( fdlPerson );

    wPerson = new CCombo( wGeneralComp, SWT.BORDER | SWT.READ_ONLY );
    wPerson.setEditable( true );
    props.setLook( wPerson );
    wPerson.addModifyListener( lsMod );
    fdPerson = new FormData();
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
    wlPhone = new Label( wGeneralComp, SWT.RIGHT );
    wlPhone.setText( BaseMessages.getString( PKG, "Mail.ContactPhone.Label" ) );
    props.setLook( wlPhone );
    fdlPhone = new FormData();
    fdlPhone.left = new FormAttachment( 0, -margin );
    fdlPhone.top = new FormAttachment( wPerson, margin );
    fdlPhone.right = new FormAttachment( middle, -2 * margin );
    wlPhone.setLayoutData( fdlPhone );

    wPhone = new CCombo( wGeneralComp, SWT.BORDER | SWT.READ_ONLY );
    wPhone.setEditable( true );
    props.setLook( wPhone );
    wPhone.addModifyListener( lsMod );
    fdPhone = new FormData();
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

    fdGeneralComp = new FormData();
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

    wContentTab = new CTabItem( wTabFolder, SWT.NONE );
    wContentTab.setText( BaseMessages.getString( PKG, "MailDialog.Server.Label" ) );

    FormLayout contentLayout = new FormLayout();
    contentLayout.marginWidth = 3;
    contentLayout.marginHeight = 3;

    wContentComp = new Composite( wTabFolder, SWT.NONE );
    props.setLook( wContentComp );
    wContentComp.setLayout( contentLayout );

    // ////////////////////////
    // START OF SERVER GROUP
    // /////////////////////////

    wServerGroup = new Group( wContentComp, SWT.SHADOW_NONE );
    props.setLook( wServerGroup );
    wServerGroup.setText( BaseMessages.getString( PKG, "Mail.Group.SMTPServer.Label" ) );

    FormLayout servergroupLayout = new FormLayout();
    servergroupLayout.marginWidth = 10;
    servergroupLayout.marginHeight = 10;
    wServerGroup.setLayout( servergroupLayout );

    // Server
    wlServer = new Label( wServerGroup, SWT.RIGHT );
    wlServer.setText( BaseMessages.getString( PKG, "Mail.SMTPServer.Label" ) );
    props.setLook( wlServer );
    fdlServer = new FormData();
    fdlServer.left = new FormAttachment( 0, -margin );
    fdlServer.top = new FormAttachment( 0, margin );
    fdlServer.right = new FormAttachment( middle, -2 * margin );
    wlServer.setLayoutData( fdlServer );

    wServer = new CCombo( wServerGroup, SWT.BORDER | SWT.READ_ONLY );
    wServer.setEditable( true );
    props.setLook( wServer );
    wServer.addModifyListener( lsMod );
    fdServer = new FormData();
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
    wlPort = new Label( wServerGroup, SWT.RIGHT );
    wlPort.setText( BaseMessages.getString( PKG, "Mail.Port.Label" ) );
    props.setLook( wlPort );
    fdlPort = new FormData();
    fdlPort.left = new FormAttachment( 0, -margin );
    fdlPort.top = new FormAttachment( wServer, margin );
    fdlPort.right = new FormAttachment( middle, -2 * margin );
    wlPort.setLayoutData( fdlPort );

    wPort = new CCombo( wServerGroup, SWT.BORDER | SWT.READ_ONLY );
    wPort.setEditable( true );
    props.setLook( wPort );
    wPort.addModifyListener( lsMod );
    fdPort = new FormData();
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

    wAuthentificationGroup = new Group( wContentComp, SWT.SHADOW_NONE );
    props.setLook( wAuthentificationGroup );
    wAuthentificationGroup.setText( BaseMessages.getString( PKG, "Mail.Group.Authentification.Label" ) );

    FormLayout authentificationgroupLayout = new FormLayout();
    authentificationgroupLayout.marginWidth = 10;
    authentificationgroupLayout.marginHeight = 10;
    wAuthentificationGroup.setLayout( authentificationgroupLayout );

    // Authentication?
    wlUseAuth = new Label( wAuthentificationGroup, SWT.RIGHT );
    wlUseAuth.setText( BaseMessages.getString( PKG, "Mail.UseAuthentication.Label" ) );
    props.setLook( wlUseAuth );
    fdlUseAuth = new FormData();
    fdlUseAuth.left = new FormAttachment( 0, 0 );
    fdlUseAuth.top = new FormAttachment( wServerGroup, margin );
    fdlUseAuth.right = new FormAttachment( middle, -2 * margin );
    wlUseAuth.setLayoutData( fdlUseAuth );
    wUseAuth = new Button( wAuthentificationGroup, SWT.CHECK );
    props.setLook( wUseAuth );
    fdUseAuth = new FormData();
    fdUseAuth.left = new FormAttachment( middle, -margin );
    fdUseAuth.top = new FormAttachment( wServerGroup, margin );
    fdUseAuth.right = new FormAttachment( 100, 0 );
    wUseAuth.setLayoutData( fdUseAuth );
    wUseAuth.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e ) {
        setUseAuth();
        input.setChanged();
      }
    } );

    // AuthUser line
    wlAuthUser = new Label( wAuthentificationGroup, SWT.RIGHT );
    wlAuthUser.setText( BaseMessages.getString( PKG, "Mail.AuthenticationUser.Label" ) );
    props.setLook( wlAuthUser );
    fdlAuthUser = new FormData();
    fdlAuthUser.left = new FormAttachment( 0, -margin );
    fdlAuthUser.top = new FormAttachment( wUseAuth, margin );
    fdlAuthUser.right = new FormAttachment( middle, -2 * margin );
    wlAuthUser.setLayoutData( fdlAuthUser );

    wAuthUser = new CCombo( wAuthentificationGroup, SWT.BORDER | SWT.READ_ONLY );
    wAuthUser.setEditable( true );
    props.setLook( wAuthUser );
    wAuthUser.addModifyListener( lsMod );
    fdAuthUser = new FormData();
    fdAuthUser.left = new FormAttachment( middle, -margin );
    fdAuthUser.top = new FormAttachment( wUseAuth, margin );
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
    fdlAuthPass = new FormData();
    fdlAuthPass.left = new FormAttachment( 0, -margin );
    fdlAuthPass.top = new FormAttachment( wAuthUser, margin );
    fdlAuthPass.right = new FormAttachment( middle, -2 * margin );
    wlAuthPass.setLayoutData( fdlAuthPass );

    wAuthPass = new CCombo( wAuthentificationGroup, SWT.BORDER | SWT.READ_ONLY );
    wAuthPass.setEditable( true );
    props.setLook( wAuthPass );
    wAuthPass.addModifyListener( lsMod );
    fdAuthPass = new FormData();
    fdAuthPass.left = new FormAttachment( middle, -margin );
    fdAuthPass.top = new FormAttachment( wAuthUser, margin );
    fdAuthPass.right = new FormAttachment( 100, -margin );
    wAuthPass.setLayoutData( fdAuthPass );
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
    fdlUseSecAuth = new FormData();
    fdlUseSecAuth.left = new FormAttachment( 0, 0 );
    fdlUseSecAuth.top = new FormAttachment( wAuthPass, margin );
    fdlUseSecAuth.right = new FormAttachment( middle, -2 * margin );
    wlUseSecAuth.setLayoutData( fdlUseSecAuth );
    wUseSecAuth = new Button( wAuthentificationGroup, SWT.CHECK );
    props.setLook( wUseSecAuth );
    fdUseSecAuth = new FormData();
    fdUseSecAuth.left = new FormAttachment( middle, -margin );
    fdUseSecAuth.top = new FormAttachment( wAuthPass, margin );
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
    fdlSecureConnectionType = new FormData();
    fdlSecureConnectionType.left = new FormAttachment( 0, 0 );
    fdlSecureConnectionType.top = new FormAttachment( wUseSecAuth, margin );
    fdlSecureConnectionType.right = new FormAttachment( middle, -2 * margin );
    wlSecureConnectionType.setLayoutData( fdlSecureConnectionType );
    wSecureConnectionType = new CCombo( wAuthentificationGroup, SWT.BORDER | SWT.READ_ONLY );
    wSecureConnectionType.setEditable( true );
    props.setLook( wSecureConnectionType );
    wSecureConnectionType.addModifyListener( lsMod );
    fdSecureConnectionType = new FormData();
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

    fdContentComp = new FormData();
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

    wMessageTab = new CTabItem( wTabFolder, SWT.NONE );
    wMessageTab.setText( BaseMessages.getString( PKG, "Mail.Tab.Message.Label" ) );

    FormLayout messageLayout = new FormLayout();
    messageLayout.marginWidth = 3;
    messageLayout.marginHeight = 3;

    wMessageComp = new Composite( wTabFolder, SWT.NONE );
    props.setLook( wMessageComp );
    wMessageComp.setLayout( contentLayout );

    // ////////////////////////////////////
    // START OF MESSAGE SETTINGS GROUP
    // ////////////////////////////////////

    wMessageSettingsGroup = new Group( wMessageComp, SWT.SHADOW_NONE );
    props.setLook( wMessageSettingsGroup );
    wMessageSettingsGroup.setText( BaseMessages.getString( PKG, "Mail.Group.MessageSettings.Label" ) );

    FormLayout messagesettingsgroupLayout = new FormLayout();
    messagesettingsgroupLayout.marginWidth = 10;
    messagesettingsgroupLayout.marginHeight = 10;
    wMessageSettingsGroup.setLayout( messagesettingsgroupLayout );

    // Add date to logfile name?
    wlAddDate = new Label( wMessageSettingsGroup, SWT.RIGHT );
    wlAddDate.setText( BaseMessages.getString( PKG, "Mail.IncludeDate.Label" ) );
    props.setLook( wlAddDate );
    fdlAddDate = new FormData();
    fdlAddDate.left = new FormAttachment( 0, 0 );
    fdlAddDate.top = new FormAttachment( 0, margin );
    fdlAddDate.right = new FormAttachment( middle, -2 * margin );
    wlAddDate.setLayoutData( fdlAddDate );
    wAddDate = new Button( wMessageSettingsGroup, SWT.CHECK );
    props.setLook( wAddDate );
    fdAddDate = new FormData();
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
    wlOnlyComment = new Label( wMessageSettingsGroup, SWT.RIGHT );
    wlOnlyComment.setText( BaseMessages.getString( PKG, "Mail.OnlyCommentInBody.Label" ) );
    props.setLook( wlOnlyComment );
    fdlOnlyComment = new FormData();
    fdlOnlyComment.left = new FormAttachment( 0, 0 );
    fdlOnlyComment.top = new FormAttachment( wAddDate, margin );
    fdlOnlyComment.right = new FormAttachment( middle, -2 * margin );
    wlOnlyComment.setLayoutData( fdlOnlyComment );
    wOnlyComment = new Button( wMessageSettingsGroup, SWT.CHECK );
    props.setLook( wOnlyComment );
    fdOnlyComment = new FormData();
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
    wlUseHTML = new Label( wMessageSettingsGroup, SWT.RIGHT );
    wlUseHTML.setText( BaseMessages.getString( PKG, "Mail.UseHTMLInBody.Label" ) );
    props.setLook( wlUseHTML );
    fdlUseHTML = new FormData();
    fdlUseHTML.left = new FormAttachment( 0, 0 );
    fdlUseHTML.top = new FormAttachment( wOnlyComment, margin );
    fdlUseHTML.right = new FormAttachment( middle, -2 * margin );
    wlUseHTML.setLayoutData( fdlUseHTML );
    wUseHTML = new Button( wMessageSettingsGroup, SWT.CHECK );
    props.setLook( wUseHTML );
    fdUseHTML = new FormData();
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
    fdlEncoding = new FormData();
    fdlEncoding.left = new FormAttachment( 0, 0 );
    fdlEncoding.top = new FormAttachment( wUseHTML, margin );
    fdlEncoding.right = new FormAttachment( middle, -2 * margin );
    wlEncoding.setLayoutData( fdlEncoding );
    wEncoding = new CCombo( wMessageSettingsGroup, SWT.BORDER | SWT.READ_ONLY );
    wEncoding.setEditable( true );
    props.setLook( wEncoding );
    wEncoding.addModifyListener( lsMod );
    fdEncoding = new FormData();
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
    wlUsePriority = new Label( wMessageSettingsGroup, SWT.RIGHT );
    wlUsePriority.setText( BaseMessages.getString( PKG, "Mail.UsePriority.Label" ) );
    props.setLook( wlUsePriority );
    fdlPriority = new FormData();
    fdlPriority.left = new FormAttachment( 0, 0 );
    fdlPriority.top = new FormAttachment( wEncoding, margin );
    fdlPriority.right = new FormAttachment( middle, -2 * margin );
    wlUsePriority.setLayoutData( fdlPriority );
    wUsePriority = new Button( wMessageSettingsGroup, SWT.CHECK );
    wUsePriority.setToolTipText( BaseMessages.getString( PKG, "Mail.UsePriority.Tooltip" ) );
    props.setLook( wUsePriority );
    fdUsePriority = new FormData();
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
    fdPriority = new FormData();
    fdPriority.left = new FormAttachment( middle, -margin );
    fdPriority.top = new FormAttachment( wUsePriority, margin );
    fdPriority.right = new FormAttachment( 100, 0 );
    wPriority.setLayoutData( fdPriority );

    // Importance
    wlImportance = new Label( wMessageSettingsGroup, SWT.RIGHT );
    wlImportance.setText( BaseMessages.getString( PKG, "Mail.Importance.Label" ) );
    props.setLook( wlImportance );
    fdlImportance = new FormData();
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
    fdImportance = new FormData();
    fdImportance.left = new FormAttachment( middle, -margin );
    fdImportance.top = new FormAttachment( wPriority, margin );
    fdImportance.right = new FormAttachment( 100, 0 );
    wImportance.setLayoutData( fdImportance );

    // Sensitivity
    wlSensitivity = new Label( wMessageSettingsGroup, SWT.RIGHT );
    wlSensitivity.setText( BaseMessages.getString( PKG, "Mail.Sensitivity.Label" ) );
    props.setLook( wlSensitivity );
    fdlSensitivity = new FormData();
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
    fdSensitivity = new FormData();
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

    wMessageGroup = new Group( wMessageComp, SWT.SHADOW_NONE );
    props.setLook( wMessageGroup );
    wMessageGroup.setText( BaseMessages.getString( PKG, "Mail.Group.Message.Label" ) );

    FormLayout messagegroupLayout = new FormLayout();
    messagegroupLayout.marginWidth = 10;
    messagegroupLayout.marginHeight = 10;
    wMessageGroup.setLayout( messagegroupLayout );

    // Subject line
    wlSubject = new Label( wMessageGroup, SWT.RIGHT );
    wlSubject.setText( BaseMessages.getString( PKG, "Mail.Subject.Label" ) );
    props.setLook( wlSubject );
    fdlSubject = new FormData();
    fdlSubject.left = new FormAttachment( 0, -margin );
    fdlSubject.top = new FormAttachment( wMessageSettingsGroup, margin );
    fdlSubject.right = new FormAttachment( middle, -2 * margin );
    wlSubject.setLayoutData( fdlSubject );

    wSubject = new CCombo( wMessageGroup, SWT.BORDER | SWT.READ_ONLY );
    wSubject.setEditable( true );
    props.setLook( wSubject );
    wSubject.addModifyListener( lsMod );
    fdSubject = new FormData();
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
    wlComment = new Label( wMessageGroup, SWT.RIGHT );
    wlComment.setText( BaseMessages.getString( PKG, "Mail.Comment.Label" ) );
    props.setLook( wlComment );
    fdlComment = new FormData();
    fdlComment.left = new FormAttachment( 0, -margin );
    fdlComment.top = new FormAttachment( wSubject, margin );
    fdlComment.right = new FormAttachment( middle, -2 * margin );
    wlComment.setLayoutData( fdlComment );

    wComment = new CCombo( wMessageGroup, SWT.BORDER | SWT.READ_ONLY );
    wComment.setEditable( true );
    props.setLook( wComment );
    wComment.addModifyListener( lsMod );
    fdComment = new FormData();
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

    fdMessageComp = new FormData();
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

    wAttachedTab = new CTabItem( wTabFolder, SWT.NONE );
    wAttachedTab.setText( BaseMessages.getString( PKG, "Mail.Tab.AttachedFiles.Label" ) );

    FormLayout attachedLayout = new FormLayout();
    attachedLayout.marginWidth = 3;
    attachedLayout.marginHeight = 3;

    wAttachedComp = new Composite( wTabFolder, SWT.NONE );
    props.setLook( wAttachedComp );
    wAttachedComp.setLayout( attachedLayout );

    // ///////////////////////////////
    // START OF Attached files GROUP //
    // ///////////////////////////////

    wAttachedContent = new Group( wAttachedComp, SWT.SHADOW_NONE );
    props.setLook( wAttachedContent );
    wAttachedContent.setText( BaseMessages.getString( PKG, "MailDialog.AttachedContent.Label" ) );

    FormLayout AttachedContentgroupLayout = new FormLayout();
    AttachedContentgroupLayout.marginWidth = 10;
    AttachedContentgroupLayout.marginHeight = 10;
    wAttachedContent.setLayout( AttachedContentgroupLayout );

    // Is Filename defined in a Field
    wlisattachContentField = new Label( wAttachedContent, SWT.RIGHT );
    wlisattachContentField.setText( BaseMessages.getString( PKG, "MailDialog.isattachContentField.Label" ) );
    props.setLook( wlisattachContentField );
    FormData fdlisattachContentField = new FormData();
    fdlisattachContentField.left = new FormAttachment( 0, -margin );
    fdlisattachContentField.top = new FormAttachment( 0, margin );
    fdlisattachContentField.right = new FormAttachment( middle, -2 * margin );
    wlisattachContentField.setLayoutData( fdlisattachContentField );

    wisattachContentField = new Button( wAttachedContent, SWT.CHECK );
    props.setLook( wisattachContentField );
    wisattachContentField.setToolTipText( BaseMessages.getString( PKG, "MailDialog.isattachContentField.Tooltip" ) );
    FormData fdisattachContentField = new FormData();
    fdisattachContentField.left = new FormAttachment( middle, -margin );
    fdisattachContentField.top = new FormAttachment( 0, margin );
    wisattachContentField.setLayoutData( fdisattachContentField );
    SelectionAdapter lisattachContentField = new SelectionAdapter() {
      public void widgetSelected( SelectionEvent arg0 ) {
        activeISAttachContentField();
        input.setChanged();
      }
    };
    wisattachContentField.addSelectionListener( lisattachContentField );

    // attache file content field
    wlattachContentField = new Label( wAttachedContent, SWT.RIGHT );
    wlattachContentField.setText( BaseMessages.getString( PKG, "MailDialog.attachContentField.Label" ) );
    props.setLook( wlattachContentField );
    FormData fdlattachContentField = new FormData();
    fdlattachContentField.left = new FormAttachment( 0, -margin );
    fdlattachContentField.top = new FormAttachment( wisattachContentField, margin );
    fdlattachContentField.right = new FormAttachment( middle, -2 * margin );
    wlattachContentField.setLayoutData( fdlattachContentField );

    wattachContentField = new CCombo( wAttachedContent, SWT.BORDER | SWT.READ_ONLY );
    wattachContentField.setEditable( true );
    props.setLook( wattachContentField );
    wattachContentField.addModifyListener( lsMod );
    FormData fdattachContentField = new FormData();
    fdattachContentField.left = new FormAttachment( middle, -margin );
    fdattachContentField.top = new FormAttachment( wisattachContentField, margin );
    fdattachContentField.right = new FormAttachment( 100, -margin );
    wattachContentField.setLayoutData( fdattachContentField );
    wattachContentField.addFocusListener( new FocusListener() {
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
    wlattachContentFileNameField = new Label( wAttachedContent, SWT.RIGHT );
    wlattachContentFileNameField.setText( BaseMessages.getString(
      PKG, "MailDialog.attachContentFileNameField.Label" ) );
    props.setLook( wlattachContentFileNameField );
    FormData fdlattachContentFileNameField = new FormData();
    fdlattachContentFileNameField.left = new FormAttachment( 0, -margin );
    fdlattachContentFileNameField.top = new FormAttachment( wattachContentField, margin );
    fdlattachContentFileNameField.right = new FormAttachment( middle, -2 * margin );
    wlattachContentFileNameField.setLayoutData( fdlattachContentFileNameField );

    wattachContentFileNameField = new CCombo( wAttachedContent, SWT.BORDER | SWT.READ_ONLY );
    wattachContentFileNameField.setEditable( true );
    props.setLook( wattachContentFileNameField );
    wattachContentFileNameField.addModifyListener( lsMod );
    FormData fdattachContentFileNameField = new FormData();
    fdattachContentFileNameField.left = new FormAttachment( middle, -margin );
    fdattachContentFileNameField.top = new FormAttachment( wattachContentField, margin );
    fdattachContentFileNameField.right = new FormAttachment( 100, -margin );
    wattachContentFileNameField.setLayoutData( fdattachContentFileNameField );
    wattachContentFileNameField.addFocusListener( new FocusListener() {
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

    FormLayout OriginFilesgroupLayout = new FormLayout();
    OriginFilesgroupLayout.marginWidth = 10;
    OriginFilesgroupLayout.marginHeight = 10;
    wOriginFiles.setLayout( OriginFilesgroupLayout );

    // Is Filename defined in a Field
    wlisFileDynamic = new Label( wOriginFiles, SWT.RIGHT );
    wlisFileDynamic.setText( BaseMessages.getString( PKG, "MailDialog.isFileDynamic.Label" ) );
    props.setLook( wlisFileDynamic );
    FormData fdlisFileDynamic = new FormData();
    fdlisFileDynamic.left = new FormAttachment( 0, -margin );
    fdlisFileDynamic.top = new FormAttachment( wAttachedContent, margin );
    fdlisFileDynamic.right = new FormAttachment( middle, -2 * margin );
    wlisFileDynamic.setLayoutData( fdlisFileDynamic );

    wisFileDynamic = new Button( wOriginFiles, SWT.CHECK );
    props.setLook( wisFileDynamic );
    wisFileDynamic.setToolTipText( BaseMessages.getString( PKG, "MailDialog.isFileDynamic.Tooltip" ) );
    FormData fdisFileDynamic = new FormData();
    fdisFileDynamic.left = new FormAttachment( middle, -margin );
    fdisFileDynamic.top = new FormAttachment( wAttachedContent, margin );
    wisFileDynamic.setLayoutData( fdisFileDynamic );
    SelectionAdapter lisFileDynamic = new SelectionAdapter() {
      public void widgetSelected( SelectionEvent arg0 ) {
        ActiveisFileDynamic();
        input.setChanged();
      }
    };
    wisFileDynamic.addSelectionListener( lisFileDynamic );

    // Filename field
    wlDynamicFilenameField = new Label( wOriginFiles, SWT.RIGHT );
    wlDynamicFilenameField.setText( BaseMessages.getString( PKG, "MailDialog.DynamicFilenameField.Label" ) );
    props.setLook( wlDynamicFilenameField );
    FormData fdlFilenameField = new FormData();
    fdlFilenameField.left = new FormAttachment( 0, -margin );
    fdlFilenameField.top = new FormAttachment( wisFileDynamic, margin );
    fdlFilenameField.right = new FormAttachment( middle, -2 * margin );
    wlDynamicFilenameField.setLayoutData( fdlFilenameField );

    wDynamicFilenameField = new CCombo( wOriginFiles, SWT.BORDER | SWT.READ_ONLY );
    wDynamicFilenameField.setEditable( true );
    props.setLook( wDynamicFilenameField );
    wDynamicFilenameField.addModifyListener( lsMod );
    FormData fdFilenameField = new FormData();
    fdFilenameField.left = new FormAttachment( middle, -margin );
    fdFilenameField.top = new FormAttachment( wisFileDynamic, margin );
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

    // FileFoldername line
    wlSourceFileFoldername = new Label( wOriginFiles, SWT.RIGHT );
    wlSourceFileFoldername.setText( BaseMessages.getString( PKG, "MailDialog.FileFoldername.Label" ) );
    props.setLook( wlSourceFileFoldername );
    fdlSourceFileFoldername = new FormData();
    fdlSourceFileFoldername.left = new FormAttachment( 0, 0 );
    fdlSourceFileFoldername.top = new FormAttachment( wDynamicWildcardField, 2 * margin );
    fdlSourceFileFoldername.right = new FormAttachment( middle, -margin );
    wlSourceFileFoldername.setLayoutData( fdlSourceFileFoldername );

    // Browse Destination folders button ...
    wbSourceFolder = new Button( wOriginFiles, SWT.PUSH | SWT.CENTER );
    props.setLook( wbSourceFolder );
    wbSourceFolder.setText( BaseMessages.getString( PKG, "MailDialog.BrowseFolders.Label" ) );
    fdbSourceFolder = new FormData();
    fdbSourceFolder.right = new FormAttachment( 100, 0 );
    fdbSourceFolder.top = new FormAttachment( wDynamicWildcardField, 2 * margin );
    wbSourceFolder.setLayoutData( fdbSourceFolder );

    // Browse source file button ...
    wbFileFoldername = new Button( wOriginFiles, SWT.PUSH | SWT.CENTER );
    props.setLook( wbFileFoldername );
    wbFileFoldername.setText( BaseMessages.getString( PKG, "MailDialog.BrowseFiles.Label" ) );
    fdbSourceFileFoldername = new FormData();
    fdbSourceFileFoldername.right = new FormAttachment( wbSourceFolder, -margin );
    fdbSourceFileFoldername.top = new FormAttachment( wDynamicWildcardField, 2 * margin );
    wbFileFoldername.setLayoutData( fdbSourceFileFoldername );

    wSourceFileFoldername = new TextVar( transMeta, wOriginFiles, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wSourceFileFoldername );
    wSourceFileFoldername.addModifyListener( lsMod );
    fdSourceFileFoldername = new FormData();
    fdSourceFileFoldername.left = new FormAttachment( middle, 0 );
    fdSourceFileFoldername.top = new FormAttachment( wDynamicWildcardField, 2 * margin );
    fdSourceFileFoldername.right = new FormAttachment( wbFileFoldername, -margin );
    wSourceFileFoldername.setLayoutData( fdSourceFileFoldername );

    // Whenever something changes, set the tooltip to the expanded version:
    wSourceFileFoldername.addModifyListener( new ModifyListener() {
      public void modifyText( ModifyEvent e ) {
        wSourceFileFoldername.setToolTipText( transMeta.environmentSubstitute( wSourceFileFoldername.getText() ) );
      }
    } );

    wbSourceFolder.addSelectionListener( new SelectionAdapterFileDialogTextVar( log, wSourceFileFoldername, transMeta,
      new SelectionAdapterOptions( SelectionOperation.FOLDER ) ) );

    wbFileFoldername.addSelectionListener( new SelectionAdapterFileDialogTextVar( log, wSourceFileFoldername, transMeta,
      new SelectionAdapterOptions( SelectionOperation.FILE,
        new FilterType[] { FilterType.ALL }, FilterType.ALL  ) ) );


    // Include sub folders
    wlincludeSubFolders = new Label( wOriginFiles, SWT.RIGHT );
    wlincludeSubFolders.setText( BaseMessages.getString( PKG, "MailDialog.includeSubFolders.Label" ) );
    props.setLook( wlincludeSubFolders );
    fdlincludeSubFolders = new FormData();
    fdlincludeSubFolders.left = new FormAttachment( 0, 0 );
    fdlincludeSubFolders.top = new FormAttachment( wSourceFileFoldername, margin );
    fdlincludeSubFolders.right = new FormAttachment( middle, -margin );
    wlincludeSubFolders.setLayoutData( fdlincludeSubFolders );
    wincludeSubFolders = new Button( wOriginFiles, SWT.CHECK );
    props.setLook( wincludeSubFolders );
    wincludeSubFolders.setToolTipText( BaseMessages.getString( PKG, "MailDialog.includeSubFolders.Tooltip" ) );
    fdincludeSubFolders = new FormData();
    fdincludeSubFolders.left = new FormAttachment( middle, 0 );
    fdincludeSubFolders.top = new FormAttachment( wSourceFileFoldername, margin );
    fdincludeSubFolders.right = new FormAttachment( 100, 0 );
    wincludeSubFolders.setLayoutData( fdincludeSubFolders );
    wincludeSubFolders.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e ) {
        input.setChanged();
      }
    } );

    // Wildcard
    wlWildcard = new Label( wOriginFiles, SWT.RIGHT );
    wlWildcard.setText( BaseMessages.getString( PKG, "MailDialog.Wildcard.Label" ) );
    props.setLook( wlWildcard );
    fdlWildcard = new FormData();
    fdlWildcard.left = new FormAttachment( 0, 0 );
    fdlWildcard.top = new FormAttachment( wincludeSubFolders, margin );
    fdlWildcard.right = new FormAttachment( middle, -margin );
    wlWildcard.setLayoutData( fdlWildcard );
    wWildcard = new TextVar( transMeta, wOriginFiles, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wWildcard );
    wWildcard.setToolTipText( BaseMessages.getString( PKG, "MailDialog.Wildcard.Tooltip" ) );
    wWildcard.addModifyListener( lsMod );
    fdWildcard = new FormData();
    fdWildcard.left = new FormAttachment( middle, 0 );
    fdWildcard.top = new FormAttachment( wincludeSubFolders, margin );
    fdWildcard.right = new FormAttachment( wbFileFoldername, -margin );
    wWildcard.setLayoutData( fdWildcard );

    // Whenever something changes, set the tooltip to the expanded version:
    wWildcard.addModifyListener( new ModifyListener() {
      public void modifyText( ModifyEvent e ) {
        wWildcard.setToolTipText( transMeta.environmentSubstitute( wWildcard.getText() ) );
      }
    } );
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

    FormLayout ZipGroupgroupLayout = new FormLayout();
    ZipGroupgroupLayout.marginWidth = 10;
    ZipGroupgroupLayout.marginHeight = 10;
    wZipGroup.setLayout( ZipGroupgroupLayout );

    // Zip Files?
    wlZipFiles = new Label( wZipGroup, SWT.RIGHT );
    wlZipFiles.setText( BaseMessages.getString( PKG, "MailDialog.ZipFiles.Label" ) );
    props.setLook( wlZipFiles );
    fdlZipFiles = new FormData();
    fdlZipFiles.left = new FormAttachment( 0, -margin );
    fdlZipFiles.top = new FormAttachment( wOriginFiles, margin );
    fdlZipFiles.right = new FormAttachment( middle, -2 * margin );
    wlZipFiles.setLayoutData( fdlZipFiles );
    wZipFiles = new Button( wZipGroup, SWT.CHECK );
    props.setLook( wZipFiles );
    fdZipFiles = new FormData();
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
    wlisZipFileDynamic = new Label( wZipGroup, SWT.RIGHT );
    wlisZipFileDynamic.setText( BaseMessages.getString( PKG, "MailDialog.isZipFileDynamic.Label" ) );
    props.setLook( wlisZipFileDynamic );
    fdlisZipFileDynamic = new FormData();
    fdlisZipFileDynamic.left = new FormAttachment( 0, -margin );
    fdlisZipFileDynamic.top = new FormAttachment( wZipFiles, margin );
    fdlisZipFileDynamic.right = new FormAttachment( middle, -2 * margin );
    wlisZipFileDynamic.setLayoutData( fdlisZipFileDynamic );
    wisZipFileDynamic = new Button( wZipGroup, SWT.CHECK );
    props.setLook( wisZipFileDynamic );
    fdisZipFileDynamic = new FormData();
    fdisZipFileDynamic.left = new FormAttachment( middle, -margin );
    fdisZipFileDynamic.top = new FormAttachment( wZipFiles, margin );
    fdisZipFileDynamic.right = new FormAttachment( 100, -margin );
    wisZipFileDynamic.setLayoutData( fdisZipFileDynamic );
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
    fdlDynamicZipFileField = new FormData();
    fdlDynamicZipFileField.left = new FormAttachment( 0, -margin );
    fdlDynamicZipFileField.top = new FormAttachment( wisZipFileDynamic, margin );
    fdlDynamicZipFileField.right = new FormAttachment( middle, -2 * margin );
    wlDynamicZipFileField.setLayoutData( fdlDynamicZipFileField );

    wDynamicZipFileField = new CCombo( wZipGroup, SWT.BORDER | SWT.READ_ONLY );
    wDynamicZipFileField.setEditable( true );
    props.setLook( wDynamicZipFileField );
    wDynamicZipFileField.addModifyListener( lsMod );
    fdDynamicZipFileField = new FormData();
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
    fdZipFilename = new FormData();
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
    fdZipSizeCondition = new FormData();
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

    fdAttachedComp = new FormData();
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

    wembeddedTab = new CTabItem( wTabFolder, SWT.NONE );
    wembeddedTab.setText( BaseMessages.getString( PKG, "Mail.Tab.embeddedImages.Label" ) );

    FormLayout embeddedLayout = new FormLayout();
    embeddedLayout.marginWidth = 3;
    embeddedLayout.marginHeight = 3;

    wembeddedComp = new Composite( wTabFolder, SWT.NONE );
    props.setLook( wembeddedComp );
    wembeddedComp.setLayout( embeddedLayout );

    // ImageFilename line
    wlImageFilename = new Label( wembeddedComp, SWT.RIGHT );
    wlImageFilename.setText( BaseMessages.getString( PKG, "MailDialog.ImageFilename.Label" ) );
    props.setLook( wlImageFilename );
    FormData fdlImageFilename = new FormData();
    fdlImageFilename.left = new FormAttachment( 0, 0 );
    fdlImageFilename.top = new FormAttachment( wStepname, margin );
    fdlImageFilename.right = new FormAttachment( middle, -margin );
    wlImageFilename.setLayoutData( fdlImageFilename );

    wbImageFilename = new Button( wembeddedComp, SWT.PUSH | SWT.CENTER );
    props.setLook( wbImageFilename );
    wbImageFilename.setText( BaseMessages.getString( PKG, "MailDialog.BrowseFiles.Label" ) );
    wbImageFilename.setToolTipText( BaseMessages.getString( PKG, "MailDialog.BrowseFiles.Tooltip" ) );
    FormData fdbImageFilename = new FormData();
    fdbImageFilename.right = new FormAttachment( 100, 0 );
    fdbImageFilename.top = new FormAttachment( wStepname, margin );
    fdbImageFilename.right = new FormAttachment( 100, -margin );
    wbImageFilename.setLayoutData( fdbImageFilename );

    wbaImageFilename = new Button( wembeddedComp, SWT.PUSH | SWT.CENTER );
    props.setLook( wbaImageFilename );
    wbaImageFilename.setText( BaseMessages.getString( PKG, "MailDialog.ImageFilenameAdd.Button" ) );
    wbaImageFilename.setToolTipText( BaseMessages.getString( PKG, "MailDialog.ImageFilenameAdd.Tooltip" ) );
    FormData fdbaImageFilename = new FormData();
    fdbaImageFilename.right = new FormAttachment( wbImageFilename, -margin );
    fdbaImageFilename.top = new FormAttachment( wStepname, margin );
    wbaImageFilename.setLayoutData( fdbaImageFilename );

    wImageFilename = new TextVar( transMeta, wembeddedComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wImageFilename );
    wImageFilename.addModifyListener( lsMod );
    FormData fdImageFilename = new FormData();
    fdImageFilename.left = new FormAttachment( middle, 0 );
    fdImageFilename.top = new FormAttachment( wStepname, margin );
    fdImageFilename.right = new FormAttachment( wbaImageFilename, -margin );
    wImageFilename.setLayoutData( fdImageFilename );

    // Whenever something changes, set the tooltip to the expanded version:
    wImageFilename.addModifyListener( new ModifyListener() {
      public void modifyText( ModifyEvent e ) {
        wImageFilename.setToolTipText( transMeta.environmentSubstitute( wImageFilename.getText() ) );
      }
    } );

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
          Random randomgen = new Random();
          wContentID.setText( Long.toString( Math.abs( randomgen.nextLong() ), 32 ) );
        }
      }
    } );

    // ContentID
    wlContentID = new Label( wembeddedComp, SWT.RIGHT );
    wlContentID.setText( BaseMessages.getString( PKG, "MailDialog.ContentID.Label" ) );
    props.setLook( wlContentID );
    FormData fdlContentID = new FormData();
    fdlContentID.left = new FormAttachment( 0, 0 );
    fdlContentID.top = new FormAttachment( wImageFilename, margin );
    fdlContentID.right = new FormAttachment( middle, -margin );
    wlContentID.setLayoutData( fdlContentID );
    wContentID =
      new TextVar( transMeta, wembeddedComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER, BaseMessages.getString(
        PKG, "MailDialog.ContentID.Tooltip" ) );
    props.setLook( wContentID );
    wContentID.addModifyListener( lsMod );
    FormData fdContentID = new FormData();
    fdContentID.left = new FormAttachment( middle, 0 );
    fdContentID.top = new FormAttachment( wImageFilename, margin );
    fdContentID.right = new FormAttachment( wbaImageFilename, -margin );
    wContentID.setLayoutData( fdContentID );

    // Buttons to the right of the screen...
    wbdImageFilename = new Button( wembeddedComp, SWT.PUSH | SWT.CENTER );
    props.setLook( wbdImageFilename );
    wbdImageFilename.setText( BaseMessages.getString( PKG, "MailDialog.ImageFilenameDelete.Button" ) );
    wbdImageFilename.setToolTipText( BaseMessages.getString( PKG, "MailDialog.ImageFilenameDelete.Tooltip" ) );
    FormData fdbdImageFilename = new FormData();
    fdbdImageFilename.right = new FormAttachment( 100, 0 );
    fdbdImageFilename.top = new FormAttachment( wContentID, 40 );
    wbdImageFilename.setLayoutData( fdbdImageFilename );

    wbeImageFilename = new Button( wembeddedComp, SWT.PUSH | SWT.CENTER );
    props.setLook( wbeImageFilename );
    wbeImageFilename.setText( BaseMessages.getString( PKG, "MailDialog.ImageFilenameEdit.Button" ) );
    wbeImageFilename.setToolTipText( BaseMessages.getString( PKG, "MailDialog.ImageFilenameEdit.Tooltip" ) );
    FormData fdbeImageFilename = new FormData();
    fdbeImageFilename.right = new FormAttachment( 100, 0 );
    fdbeImageFilename.left = new FormAttachment( wbdImageFilename, 0, SWT.LEFT );
    fdbeImageFilename.top = new FormAttachment( wbdImageFilename, margin );
    wbeImageFilename.setLayoutData( fdbeImageFilename );

    wlFields = new Label( wembeddedComp, SWT.NONE );
    wlFields.setText( BaseMessages.getString( PKG, "MailDialog.Fields.Label" ) );
    props.setLook( wlFields );
    FormData fdlFields = new FormData();
    fdlFields.left = new FormAttachment( 0, 0 );
    fdlFields.right = new FormAttachment( middle, -margin );
    fdlFields.top = new FormAttachment( wContentID, margin );
    wlFields.setLayoutData( fdlFields );

    int rows =
      input.getEmbeddedImages() == null ? 1 : ( input.getEmbeddedImages().length == 0 ? 0 : input
        .getEmbeddedImages().length );
    final int FieldsRows = rows;

    ColumnInfo[] colinf =
      new ColumnInfo[] {
        new ColumnInfo(
          BaseMessages.getString( PKG, "MailDialog.Fields.Image.Label" ), ColumnInfo.COLUMN_TYPE_TEXT, false ),
        new ColumnInfo(
          BaseMessages.getString( PKG, "MailDialog.Fields.ContentID.Label" ), ColumnInfo.COLUMN_TYPE_TEXT,
          false ), };

    colinf[0].setUsingVariables( true );
    colinf[0].setToolTip( BaseMessages.getString( PKG, "MailDialog.Fields.Image.Tooltip" ) );
    colinf[1].setUsingVariables( true );
    colinf[1].setToolTip( BaseMessages.getString( PKG, "MailDialog.Fields.ContentID.Tooltip" ) );

    wFields =
      new TableView(
        transMeta, wembeddedComp, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI, colinf, FieldsRows, lsMod,
        props );

    FormData fdFields = new FormData();
    fdFields.left = new FormAttachment( 0, 0 );
    fdFields.top = new FormAttachment( wlFields, margin );
    fdFields.right = new FormAttachment( wbeImageFilename, -margin );
    fdFields.bottom = new FormAttachment( 100, -margin );
    wFields.setLayoutData( fdFields );

    // Add the file to the list of files...
    SelectionAdapter selA = new SelectionAdapter() {
      public void widgetSelected( SelectionEvent arg0 ) {
        wFields.add( new String[] { wImageFilename.getText(), wContentID.getText() } );
        wImageFilename.setText( "" );
        wContentID.setText( "" );
        wFields.removeEmptyRows();
        wFields.setRowNums();
        wFields.optWidth( true );
      }
    };
    wbaImageFilename.addSelectionListener( selA );
    wImageFilename.addSelectionListener( selA );

    // Delete files from the list of files...
    wbdImageFilename.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent arg0 ) {
        int[] idx = wFields.getSelectionIndices();
        wFields.remove( idx );
        wFields.removeEmptyRows();
        wFields.setRowNums();
      }
    } );

    // Edit the selected file & remove from the list...
    wbeImageFilename.addSelectionListener( new SelectionAdapter() {
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

    fdembeddedComp = new FormData();
    fdembeddedComp.left = new FormAttachment( 0, 0 );
    fdembeddedComp.top = new FormAttachment( 0, 0 );
    fdembeddedComp.right = new FormAttachment( 100, 0 );
    fdembeddedComp.bottom = new FormAttachment( 100, 0 );
    wembeddedComp.setLayoutData( wembeddedComp );

    wembeddedComp.layout();
    wembeddedTab.setControl( wembeddedComp );

    // ///////////////////////////////////////////////////////////
    // / END OF embedded images TAB
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
    ActiveisFileDynamic();
    SetEnabledEncoding();
    activeUsePriority();
    setDynamicZip();
    setZip();
    setUseAuth();
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
    wlisZipFileDynamic.setEnabled( wZipFiles.getSelection() );
    wisZipFileDynamic.setEnabled( wZipFiles.getSelection() );
    setDynamicZip();
  }

  private void ActiveisFileDynamic() {
    wlDynamicFilenameField.setEnabled( wisFileDynamic.getSelection() );
    wDynamicFilenameField.setEnabled( wisFileDynamic.getSelection() );
    wlDynamicWildcardField.setEnabled( wisFileDynamic.getSelection() );
    wDynamicWildcardField.setEnabled( wisFileDynamic.getSelection() );
    wWildcard.setEnabled( !wisFileDynamic.getSelection() );
    wlWildcard.setEnabled( !wisFileDynamic.getSelection() );
    wSourceFileFoldername.setEnabled( !wisFileDynamic.getSelection() );
    wlSourceFileFoldername.setEnabled( !wisFileDynamic.getSelection() );
    wbFileFoldername.setEnabled( !wisFileDynamic.getSelection() );
    wbSourceFolder.setEnabled( !wisFileDynamic.getSelection() );
  }

  private void getPreviousFields() {
    try {
      if ( !getpreviousFields ) {
        getpreviousFields = true;
        String destination = null;
        if ( wDestination != null ) {
          destination = wDestination.getText();
        }
        wDestination.removeAll();

        String destinationcc = null;
        if ( wDestinationCc != null ) {
          destinationcc = wDestinationCc.getText();
        }
        wDestinationCc.removeAll();

        String destinationbcc = null;
        if ( wDestinationBCc != null ) {
          destinationbcc = wDestinationBCc.getText();
        }
        wDestinationBCc.removeAll();

        String replyToaddress = null;
        if ( wReplyToAddresses != null ) {
          replyToaddress = wReplyToAddresses.getText();
        }
        wReplyToAddresses.removeAll();

        String replyname = null;
        if ( wReplyName != null ) {
          replyname = wReplyName.getText();
        }
        wReplyName.removeAll();

        String replyaddress = null;
        if ( wReply != null ) {
          replyaddress = wReply.getText();
        }
        wReply.removeAll();

        String person = null;
        if ( wPerson != null ) {
          person = wPerson.getText();
        }
        wPerson.removeAll();

        String phone = null;
        if ( wPhone != null ) {
          phone = wPhone.getText();
        }
        wPhone.removeAll();

        String servername = null;
        if ( wServer != null ) {
          servername = wServer.getText();
        }
        wServer.removeAll();

        String port = null;
        if ( wPort != null ) {
          port = wPort.getText();
        }
        wPort.removeAll();

        String authuser = null;
        String authpass = null;

        if ( wAuthUser != null ) {
          authuser = wAuthUser.getText();
        }
        wAuthUser.removeAll();
        if ( wAuthPass != null ) {
          authpass = wAuthPass.getText();
        }
        wAuthPass.removeAll();

        String subject = null;
        if ( wSubject != null ) {
          subject = wSubject.getText();
        }
        wSubject.removeAll();

        String comment = null;
        if ( wComment != null ) {
          comment = wComment.getText();
        }
        wComment.removeAll();

        String dynamFile = null;
        String dynamWildcard = null;

        if ( wDynamicFilenameField != null ) {
          dynamFile = wDynamicFilenameField.getText();
        }
        wDynamicFilenameField.removeAll();
        if ( wDynamicWildcardField != null ) {
          dynamWildcard = wDynamicWildcardField.getText();
        }
        wDynamicWildcardField.removeAll();

        String dynamZipFile = null;

        if ( wDynamicZipFileField != null ) {
          dynamZipFile = wDynamicZipFileField.getText();
        }
        wDynamicZipFileField.removeAll();

        String attachcontent = null;
        if ( wattachContentField != null ) {
          attachcontent = wattachContentField.getText();
        }
        wattachContentField.removeAll();

        String attachcontentfilename = null;
        if ( wattachContentFileNameField != null ) {
          attachcontentfilename = wattachContentFileNameField.getText();
        }
        wattachContentFileNameField.removeAll();

        RowMetaInterface r = transMeta.getPrevStepFields( stepname );
        if ( r != null ) {
          String[] fieldnames = r.getFieldNames();
          wDestination.setItems( fieldnames );
          wDestinationCc.setItems( fieldnames );
          wDestinationBCc.setItems( fieldnames );
          wReplyName.setItems( fieldnames );
          wReply.setItems( fieldnames );
          wPerson.setItems( fieldnames );
          wPhone.setItems( fieldnames );
          wServer.setItems( fieldnames );
          wPort.setItems( fieldnames );
          wAuthUser.setItems( fieldnames );
          wAuthPass.setItems( fieldnames );
          wSubject.setItems( fieldnames );
          wComment.setItems( fieldnames );
          wDynamicFilenameField.setItems( fieldnames );
          wDynamicWildcardField.setItems( fieldnames );
          wDynamicZipFileField.setItems( fieldnames );
          wReplyToAddresses.setItems( fieldnames );
          wattachContentField.setItems( fieldnames );
          wattachContentFileNameField.setItems( fieldnames );

        }
        if ( destination != null ) {
          wDestination.setText( destination );
        }
        if ( destinationcc != null ) {
          wDestinationCc.setText( destinationcc );
        }
        if ( destinationbcc != null ) {
          wDestinationBCc.setText( destinationbcc );
        }
        if ( replyname != null ) {
          wReplyName.setText( replyname );
        }
        if ( replyaddress != null ) {
          wReply.setText( replyaddress );
        }
        if ( person != null ) {
          wPerson.setText( person );
        }
        if ( phone != null ) {
          wPhone.setText( phone );
        }
        if ( servername != null ) {
          wServer.setText( servername );
        }
        if ( port != null ) {
          wPort.setText( port );
        }
        if ( authuser != null ) {
          wAuthUser.setText( authuser );
        }
        if ( authpass != null ) {
          wAuthPass.setText( authpass );
        }
        if ( subject != null ) {
          wSubject.setText( subject );
        }
        if ( comment != null ) {
          wComment.setText( comment );
        }
        if ( dynamFile != null ) {
          wDynamicFilenameField.setText( dynamFile );
        }
        if ( dynamWildcard != null ) {
          wDynamicWildcardField.setText( dynamWildcard );
        }
        if ( dynamZipFile != null ) {
          wDynamicZipFileField.setText( dynamZipFile );
        }
        if ( replyToaddress != null ) {
          wReplyToAddresses.setText( replyToaddress );
        }
        if ( attachcontent != null ) {
          wattachContentField.setText( attachcontent );
        }
        if ( attachcontentfilename != null ) {
          wattachContentFileNameField.setText( attachcontentfilename );
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
    wbaImageFilename.setEnabled( wUseHTML.getSelection() );
    wImageFilename.setEnabled( wUseHTML.getSelection() );
    wlContentID.setEnabled( wUseHTML.getSelection() );
    wContentID.setEnabled( wUseHTML.getSelection() );
    wbdImageFilename.setEnabled( wUseHTML.getSelection() );
    wbeImageFilename.setEnabled( wUseHTML.getSelection() );
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
      ArrayList<Charset> values = new ArrayList<Charset>( Charset.availableCharsets().values() );
      for ( int i = 0; i < values.size(); i++ ) {
        Charset charSet = values.get( i );
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
    wlAuthUser.setEnabled( wUseAuth.getSelection() );
    wAuthUser.setEnabled( wUseAuth.getSelection() );
    wlAuthPass.setEnabled( wUseAuth.getSelection() );
    wAuthPass.setEnabled( wUseAuth.getSelection() );
    wUseSecAuth.setEnabled( wUseAuth.getSelection() );
    wlUseSecAuth.setEnabled( wUseAuth.getSelection() );
    if ( !wUseAuth.getSelection() ) {
      wSecureConnectionType.setEnabled( false );
      wlSecureConnectionType.setEnabled( false );
    } else {
      setSecureConnectiontype();
    }

  }

  /**
   * Copy information from the meta-data input to the dialog fields.
   */
  public void getData() {
    wisattachContentField.setSelection( input.isAttachContentFromField() );
    if ( input.getAttachContentField() != null ) {
      wattachContentField.setText( input.getAttachContentField() );
    }
    if ( input.getAttachContentFileNameField() != null ) {
      wattachContentFileNameField.setText( input.getAttachContentFileNameField() );
    }
    if ( input.getDestination() != null ) {
      wDestination.setText( input.getDestination() );
    }
    if ( input.getDestinationCc() != null ) {
      wDestinationCc.setText( input.getDestinationCc() );
    }
    if ( input.getDestinationBCc() != null ) {
      wDestinationBCc.setText( input.getDestinationBCc() );
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
    wisFileDynamic.setSelection( input.isDynamicFilename() );
    if ( input.getDynamicFieldname() != null ) {
      wDynamicFilenameField.setText( input.getDynamicFieldname() );
    }
    if ( input.getDynamicWildcard() != null ) {
      wDynamicWildcardField.setText( input.getDynamicWildcard() );
    }

    if ( input.getSourceFileFoldername() != null ) {
      wSourceFileFoldername.setText( input.getSourceFileFoldername() );
    }

    if ( input.getSourceWildcard() != null ) {
      wWildcard.setText( input.getSourceWildcard() );
    }

    wincludeSubFolders.setSelection( input.isIncludeSubFolders() );

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

    wUseAuth.setSelection( input.isUsingAuthentication() );
    wUseSecAuth.setSelection( input.isUsingSecureAuthentication() );
    if ( input.getAuthenticationUser() != null ) {
      wAuthUser.setText( input.getAuthenticationUser() );
    }
    if ( input.getAuthenticationPassword() != null ) {
      wAuthPass.setText( input.getAuthenticationPassword() );
    }

    wOnlyComment.setSelection( input.isOnlySendComment() );

    wUseHTML.setSelection( input.isUseHTML() );

    if ( input.getEncoding() != null ) {
      wEncoding.setText( "" + input.getEncoding() );
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
    input.setAttachContentFromField( wisattachContentField.getSelection() );
    input.setAttachContentField( wattachContentField.getText() );
    input.setAttachContentFileNameField( wattachContentFileNameField.getText() );
    input.setDestination( wDestination.getText() );
    input.setDestinationCc( wDestinationCc.getText() );
    input.setDestinationBCc( wDestinationBCc.getText() );
    input.setServer( wServer.getText() );
    input.setPort( wPort.getText() );
    input.setReplyAddress( wReply.getText() );
    input.setReplyName( wReplyName.getText() );
    input.setSubject( wSubject.getText() );
    input.setContactPerson( wPerson.getText() );
    input.setContactPhone( wPhone.getText() );
    input.setComment( wComment.getText() );

    input.setIncludeSubFolders( wincludeSubFolders.getSelection() );
    input.setIncludeDate( wAddDate.getSelection() );
    input.setisDynamicFilename( wisFileDynamic.getSelection() );
    input.setDynamicFieldname( wDynamicFilenameField.getText() );
    input.setDynamicWildcard( wDynamicWildcardField.getText() );

    input.setDynamicZipFilenameField( wDynamicZipFileField.getText() );

    input.setSourceFileFoldername( wSourceFileFoldername.getText() );
    input.setSourceWildcard( wWildcard.getText() );

    input.setZipLimitSize( wZipSizeCondition.getText() );

    input.setZipFilenameDynamic( wisZipFileDynamic.getSelection() );

    input.setZipFilename( wZipFilename.getText() );
    input.setZipFiles( wZipFiles.getSelection() );
    input.setAuthenticationUser( wAuthUser.getText() );
    input.setAuthenticationPassword( wAuthPass.getText() );
    input.setUsingAuthentication( wUseAuth.getSelection() );
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

    int nritems = wFields.nrNonEmpty();
    int nr = 0;
    for ( int i = 0; i < nritems; i++ ) {
      String arg = wFields.getNonEmpty( i ).getText( 1 );
      if ( arg != null && arg.length() != 0 ) {
        nr++;
      }
    }
    input.allocate( nr );

    nr = 0;
    for ( int i = 0; i < nritems; i++ ) {
      String image = wFields.getNonEmpty( i ).getText( 1 );
      String id = wFields.getNonEmpty( i ).getText( 2 );
      input.setEmbeddedImage( i, image );
      input.setContentIds( i, id );
      nr++;
    }

    dispose();
  }

  private void activeISAttachContentField() {
    wOriginFiles.setEnabled( !wisattachContentField.getSelection() );
    wZipGroup.setEnabled( !wisattachContentField.getSelection() );
    wlattachContentField.setEnabled( wisattachContentField.getSelection() );
    wattachContentField.setEnabled( wisattachContentField.getSelection() );
    wlattachContentFileNameField.setEnabled( wisattachContentField.getSelection() );
    wattachContentFileNameField.setEnabled( wisattachContentField.getSelection() );
  }
}
