/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2019 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.ui.trans.steps.getsubfolders;

import org.eclipse.swt.SWT;
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
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.Props;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.TransPreviewFactory;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.steps.getsubfolders.GetSubFoldersMeta;
import org.pentaho.di.ui.core.dialog.EnterNumberDialog;
import org.pentaho.di.ui.core.dialog.EnterTextDialog;
import org.pentaho.di.ui.core.dialog.PreviewRowsDialog;
import org.pentaho.di.ui.core.events.dialog.SelectionAdapterFileDialogTextVar;
import org.pentaho.di.ui.core.events.dialog.SelectionAdapterOptions;
import org.pentaho.di.ui.core.events.dialog.SelectionOperation;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.ComboVar;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.trans.dialog.TransPreviewProgressDialog;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

public class GetSubFoldersDialog extends BaseStepDialog implements StepDialogInterface {
  private static Class<?> PKG = GetSubFoldersMeta.class; // for i18n purposes, needed by Translator2!!

  private CTabFolder wTabFolder;

  private FormData fdTabFolder;

  private CTabItem wFolderTab, wSettingsTab;

  private Composite wFolderComp, wSettingsComp;

  private FormData fdFolderComp, fdSettingsComp;

  private Label wlFoldername;

  private Button wbbFoldername; // Browse: add directory

  private Button wbdFoldername; // Delete

  private Button wbeFoldername; // Edit

  private Button wbaFoldername; // Add or change

  private TextVar wFoldername;

  private FormData fdlFoldername, fdbFoldername, fdbdFoldername, fdbeFoldername, fdbaFoldername, fdFoldername;

  private Label wlFoldernameList;

  private TableView wFoldernameList;

  private FormData fdlFoldernameList, fdFoldernameList;

  private GetSubFoldersMeta input;

  private int middle, margin;

  private ModifyListener lsMod;

  private Group wOriginFolders;

  private FormData fdOriginFolders, fdFoldernameField, fdlFoldernameField;
  private Button wFolderField;

  private Label wlFileField, wlFilenameField;
  private ComboVar wFoldernameField;
  private FormData fdlFileField, fdFileField;

  private Group wAdditionalGroup;
  private FormData fdAdditionalGroup;

  private Label wlLimit;
  private Text wLimit;
  private FormData fdlLimit, fdLimit;

  private Label wlInclRownum;
  private Button wInclRownum;
  private FormData fdlInclRownum, fdRownum;

  private Label wlInclRownumField;
  private TextVar wInclRownumField;
  private FormData fdlInclRownumField, fdInclRownumField;

  public GetSubFoldersDialog( Shell parent, Object in, TransMeta transMeta, String sname ) {
    super( parent, (BaseStepMeta) in, transMeta, sname );
    input = (GetSubFoldersMeta) in;
  }

  public String open() {
    Shell parent = getParent();
    Display display = parent.getDisplay();

    shell = new Shell( parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN );
    props.setLook( shell );
    setShellImage( shell, input );

    lsMod = new ModifyListener() {
      public void modifyText( ModifyEvent e ) {
        input.setChanged();
      }
    };
    changed = input.hasChanged();

    FormLayout formLayout = new FormLayout();
    formLayout.marginWidth = Const.FORM_MARGIN;
    formLayout.marginHeight = Const.FORM_MARGIN;

    shell.setLayout( formLayout );
    shell.setText( BaseMessages.getString( PKG, "GetSubFoldersDialog.DialogTitle" ) );

    middle = props.getMiddlePct();
    margin = Const.MARGIN;

    // Stepname line
    wlStepname = new Label( shell, SWT.RIGHT );
    wlStepname.setText( BaseMessages.getString( PKG, "System.Label.StepName" ) );
    props.setLook( wlStepname );
    fdlStepname = new FormData();
    fdlStepname.left = new FormAttachment( 0, 0 );
    fdlStepname.top = new FormAttachment( 0, margin );
    fdlStepname.right = new FormAttachment( middle, -margin );
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
    // START OF FILE TAB ///
    // ////////////////////////
    wFolderTab = new CTabItem( wTabFolder, SWT.NONE );
    wFolderTab.setText( BaseMessages.getString( PKG, "GetSubFoldersDialog.FolderTab.TabTitle" ) );

    wFolderComp = new Composite( wTabFolder, SWT.NONE );
    props.setLook( wFolderComp );

    FormLayout fileLayout = new FormLayout();
    fileLayout.marginWidth = 3;
    fileLayout.marginHeight = 3;
    wFolderComp.setLayout( fileLayout );

    // ///////////////////////////////
    // START OF Origin files GROUP //
    // ///////////////////////////////

    wOriginFolders = new Group( wFolderComp, SWT.SHADOW_NONE );
    props.setLook( wOriginFolders );
    wOriginFolders.setText( BaseMessages.getString( PKG, "GetSubFoldersDialog.wOriginFiles.Label" ) );

    FormLayout OriginFilesgroupLayout = new FormLayout();
    OriginFilesgroupLayout.marginWidth = 10;
    OriginFilesgroupLayout.marginHeight = 10;
    wOriginFolders.setLayout( OriginFilesgroupLayout );

    // Is Filename defined in a Field
    wlFileField = new Label( wOriginFolders, SWT.RIGHT );
    wlFileField.setText( BaseMessages.getString( PKG, "GetSubFoldersDialog.FolderField.Label" ) );
    props.setLook( wlFileField );
    fdlFileField = new FormData();
    fdlFileField.left = new FormAttachment( 0, -margin );
    fdlFileField.top = new FormAttachment( 0, margin );
    fdlFileField.right = new FormAttachment( middle, -2 * margin );
    wlFileField.setLayoutData( fdlFileField );

    wFolderField = new Button( wOriginFolders, SWT.CHECK );
    props.setLook( wFolderField );
    wFolderField.setToolTipText( BaseMessages.getString( PKG, "GetSubFoldersDialog.FileField.Tooltip" ) );
    fdFileField = new FormData();
    fdFileField.left = new FormAttachment( middle, -margin );
    fdFileField.top = new FormAttachment( 0, margin );
    wFolderField.setLayoutData( fdFileField );
    SelectionAdapter lfilefield = new SelectionAdapter() {
      public void widgetSelected( SelectionEvent arg0 ) {
        ActiveFileField();
        input.setChanged();
      }
    };
    wFolderField.addSelectionListener( lfilefield );

    // Filename field
    wlFilenameField = new Label( wOriginFolders, SWT.RIGHT );
    wlFilenameField.setText( BaseMessages.getString( PKG, "GetSubFoldersDialog.wlFilenameField.Label" ) );
    props.setLook( wlFilenameField );
    fdlFoldernameField = new FormData();
    fdlFoldernameField.left = new FormAttachment( 0, -margin );
    fdlFoldernameField.top = new FormAttachment( wFolderField, margin );
    fdlFoldernameField.right = new FormAttachment( middle, -2 * margin );
    wlFilenameField.setLayoutData( fdlFoldernameField );

    wFoldernameField = new ComboVar( transMeta, wOriginFolders, SWT.BORDER | SWT.READ_ONLY );
    wFoldernameField.setEditable( true );
    props.setLook( wFoldernameField );
    wFoldernameField.addModifyListener( lsMod );
    fdFoldernameField = new FormData();
    fdFoldernameField.left = new FormAttachment( middle, -margin );
    fdFoldernameField.top = new FormAttachment( wFolderField, margin );
    fdFoldernameField.right = new FormAttachment( 100, -margin );
    wFoldernameField.setLayoutData( fdFoldernameField );
    wFoldernameField.setEnabled( false );
    wFoldernameField.addFocusListener( new FocusListener() {
      public void focusLost( org.eclipse.swt.events.FocusEvent e ) {
      }

      public void focusGained( org.eclipse.swt.events.FocusEvent e ) {
        Cursor busy = new Cursor( shell.getDisplay(), SWT.CURSOR_WAIT );
        shell.setCursor( busy );
        BaseStepDialog.getFieldsFromPrevious( wFoldernameField, transMeta, stepMeta );
        shell.setCursor( null );
        busy.dispose();
      }
    } );

    fdOriginFolders = new FormData();
    fdOriginFolders.left = new FormAttachment( 0, margin );
    fdOriginFolders.top = new FormAttachment( wFoldernameList, margin );
    fdOriginFolders.right = new FormAttachment( 100, -margin );
    wOriginFolders.setLayoutData( fdOriginFolders );

    // ///////////////////////////////////////////////////////////
    // / END OF Origin files GROUP
    // ///////////////////////////////////////////////////////////

    // Foldername line
    wlFoldername = new Label( wFolderComp, SWT.RIGHT );
    wlFoldername.setText( BaseMessages.getString( PKG, "GetSubFoldersDialog.Filename.Label" ) );
    props.setLook( wlFoldername );
    fdlFoldername = new FormData();
    fdlFoldername.left = new FormAttachment( 0, 0 );
    fdlFoldername.top = new FormAttachment( wOriginFolders, margin );
    fdlFoldername.right = new FormAttachment( middle, -margin );
    wlFoldername.setLayoutData( fdlFoldername );

    wbbFoldername = new Button( wFolderComp, SWT.PUSH | SWT.CENTER );
    props.setLook( wbbFoldername );
    wbbFoldername.setText( BaseMessages.getString( PKG, "System.Button.Browse" ) );
    wbbFoldername.setToolTipText( BaseMessages.getString( PKG, "System.Tooltip.BrowseForFileOrDirAndAdd" ) );
    fdbFoldername = new FormData();
    fdbFoldername.right = new FormAttachment( 100, 0 );
    fdbFoldername.top = new FormAttachment( wOriginFolders, margin );
    wbbFoldername.setLayoutData( fdbFoldername );

    wbaFoldername = new Button( wFolderComp, SWT.PUSH | SWT.CENTER );
    props.setLook( wbaFoldername );
    wbaFoldername.setText( BaseMessages.getString( PKG, "GetSubFoldersDialog.FoldernameAdd.Button" ) );
    wbaFoldername.setToolTipText( BaseMessages.getString( PKG, "GetSubFoldersDialog.FoldernameAdd.Tooltip" ) );
    fdbaFoldername = new FormData();
    fdbaFoldername.right = new FormAttachment( wbbFoldername, -margin );
    fdbaFoldername.top = new FormAttachment( wOriginFolders, margin );
    wbaFoldername.setLayoutData( fdbaFoldername );

    wFoldername = new TextVar( transMeta, wFolderComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wFoldername );
    wFoldername.addModifyListener( lsMod );
    fdFoldername = new FormData();
    fdFoldername.left = new FormAttachment( middle, 0 );
    fdFoldername.right = new FormAttachment( wbaFoldername, -margin );
    fdFoldername.top = new FormAttachment( wOriginFolders, margin );
    wFoldername.setLayoutData( fdFoldername );

    // Filename list line
    wlFoldernameList = new Label( wFolderComp, SWT.RIGHT );
    wlFoldernameList.setText( BaseMessages.getString( PKG, "GetSubFoldersDialog.FoldernameList.Label" ) );
    props.setLook( wlFoldernameList );
    fdlFoldernameList = new FormData();
    fdlFoldernameList.left = new FormAttachment( 0, 0 );
    fdlFoldernameList.top = new FormAttachment( wFoldername, margin );
    fdlFoldernameList.right = new FormAttachment( middle, -margin );
    wlFoldernameList.setLayoutData( fdlFoldernameList );

    // Buttons to the right of the screen...
    wbdFoldername = new Button( wFolderComp, SWT.PUSH | SWT.CENTER );
    props.setLook( wbdFoldername );
    wbdFoldername.setText( BaseMessages.getString( PKG, "GetSubFoldersDialog.FoldernameDelete.Button" ) );
    wbdFoldername.setToolTipText( BaseMessages.getString( PKG, "GetSubFoldersDialog.FoldernameDelete.Tooltip" ) );
    fdbdFoldername = new FormData();
    fdbdFoldername.right = new FormAttachment( 100, 0 );
    fdbdFoldername.top = new FormAttachment( wFoldername, 40 );
    wbdFoldername.setLayoutData( fdbdFoldername );

    wbeFoldername = new Button( wFolderComp, SWT.PUSH | SWT.CENTER );
    props.setLook( wbeFoldername );
    wbeFoldername.setText( BaseMessages.getString( PKG, "GetSubFoldersDialog.FilenameEdit.Button" ) );
    wbeFoldername.setToolTipText( BaseMessages.getString( PKG, "GetSubFoldersDialog.FilenameEdit.Tooltip" ) );
    fdbeFoldername = new FormData();
    fdbeFoldername.right = new FormAttachment( 100, 0 );
    fdbeFoldername.left = new FormAttachment( wbdFoldername, 0, SWT.LEFT );
    fdbeFoldername.top = new FormAttachment( wbdFoldername, margin );
    wbeFoldername.setLayoutData( fdbeFoldername );

    ColumnInfo[] colinfo = new ColumnInfo[2];
    colinfo[0] =
      new ColumnInfo(
        BaseMessages.getString( PKG, "GetSubFoldersDialog.FileDirColumn.Column" ),
        ColumnInfo.COLUMN_TYPE_TEXT, false );
    colinfo[0].setUsingVariables( true );
    colinfo[1] =
      new ColumnInfo(
        BaseMessages.getString( PKG, "GetSubFoldersDialog.Required.Column" ), ColumnInfo.COLUMN_TYPE_CCOMBO,
        GetSubFoldersMeta.RequiredFoldersDesc );
    colinfo[1].setToolTip( BaseMessages.getString( PKG, "GetSubFoldersDialog.Required.Tooltip" ) );

    wFoldernameList =
      new TableView(
        transMeta, wFolderComp, SWT.FULL_SELECTION | SWT.SINGLE | SWT.BORDER, colinfo, colinfo.length, lsMod,
        props );
    props.setLook( wFoldernameList );
    fdFoldernameList = new FormData();
    fdFoldernameList.left = new FormAttachment( middle, 0 );
    fdFoldernameList.right = new FormAttachment( wbdFoldername, -margin );
    fdFoldernameList.top = new FormAttachment( wFoldername, margin );
    fdFoldernameList.bottom = new FormAttachment( 100, -margin );
    wFoldernameList.setLayoutData( fdFoldernameList );

    fdFolderComp = new FormData();
    fdFolderComp.left = new FormAttachment( 0, 0 );
    fdFolderComp.top = new FormAttachment( 0, 0 );
    fdFolderComp.right = new FormAttachment( 100, 0 );
    fdFolderComp.bottom = new FormAttachment( 100, 0 );
    wFolderComp.setLayoutData( fdFolderComp );

    wFolderComp.layout();
    wFolderTab.setControl( wFolderComp );

    // ///////////////////////////////////////////////////////////
    // / END OF FILE TAB
    // ///////////////////////////////////////////////////////////

    fdTabFolder = new FormData();
    fdTabFolder.left = new FormAttachment( 0, 0 );
    fdTabFolder.top = new FormAttachment( wStepname, margin );
    fdTabFolder.right = new FormAttachment( 100, 0 );
    fdTabFolder.bottom = new FormAttachment( 100, -50 );
    wTabFolder.setLayoutData( fdTabFolder );

    // ////////////////////////
    // START OF Filter TAB ///
    // ////////////////////////
    wSettingsTab = new CTabItem( wTabFolder, SWT.NONE );
    wSettingsTab.setText( BaseMessages.getString( PKG, "GetSubFoldersDialog.SettingsTab.TabTitle" ) );

    wSettingsComp = new Composite( wTabFolder, SWT.NONE );
    props.setLook( wSettingsComp );

    FormLayout filesettingLayout = new FormLayout();
    filesettingLayout.marginWidth = 3;
    filesettingLayout.marginHeight = 3;
    wSettingsComp.setLayout( fileLayout );

    // /////////////////////////////////
    // START OF Additional Fields GROUP
    // /////////////////////////////////

    wAdditionalGroup = new Group( wSettingsComp, SWT.SHADOW_NONE );
    props.setLook( wAdditionalGroup );
    wAdditionalGroup.setText( BaseMessages.getString( PKG, "GetSubFoldersDialog.Group.AdditionalGroup.Label" ) );

    FormLayout additionalgroupLayout = new FormLayout();
    additionalgroupLayout.marginWidth = 10;
    additionalgroupLayout.marginHeight = 10;
    wAdditionalGroup.setLayout( additionalgroupLayout );

    wlInclRownum = new Label( wAdditionalGroup, SWT.RIGHT );
    wlInclRownum.setText( BaseMessages.getString( PKG, "GetSubFoldersDialog.InclRownum.Label" ) );
    props.setLook( wlInclRownum );
    fdlInclRownum = new FormData();
    fdlInclRownum.left = new FormAttachment( 0, 0 );
    fdlInclRownum.top = new FormAttachment( 0, 2 * margin );
    fdlInclRownum.right = new FormAttachment( middle, -margin );
    wlInclRownum.setLayoutData( fdlInclRownum );
    wInclRownum = new Button( wAdditionalGroup, SWT.CHECK );
    props.setLook( wInclRownum );
    wInclRownum.setToolTipText( BaseMessages.getString( PKG, "GetSubFoldersDialog.InclRownum.Tooltip" ) );
    fdRownum = new FormData();
    fdRownum.left = new FormAttachment( middle, 0 );
    fdRownum.top = new FormAttachment( 0, 2 * margin );
    wInclRownum.setLayoutData( fdRownum );
    SelectionAdapter linclRownum = new SelectionAdapter() {
      public void widgetSelected( SelectionEvent arg0 ) {
        ActiveIncludeRowNum();
        input.setChanged();
      }
    };
    wInclRownum.addSelectionListener( linclRownum );

    wlInclRownumField = new Label( wAdditionalGroup, SWT.RIGHT );
    wlInclRownumField.setText( BaseMessages.getString( PKG, "GetSubFoldersDialog.InclRownumField.Label" ) );
    props.setLook( wlInclRownumField );
    fdlInclRownumField = new FormData();
    fdlInclRownumField.left = new FormAttachment( wInclRownum, margin );
    fdlInclRownumField.top = new FormAttachment( 0, 2 * margin );
    wlInclRownumField.setLayoutData( fdlInclRownumField );
    wInclRownumField = new TextVar( transMeta, wAdditionalGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wInclRownumField );
    wInclRownumField.addModifyListener( lsMod );
    fdInclRownumField = new FormData();
    fdInclRownumField.left = new FormAttachment( wlInclRownumField, margin );
    fdInclRownumField.top = new FormAttachment( 0, 2 * margin );
    fdInclRownumField.right = new FormAttachment( 100, 0 );
    wInclRownumField.setLayoutData( fdInclRownumField );

    fdAdditionalGroup = new FormData();
    fdAdditionalGroup.left = new FormAttachment( 0, margin );
    fdAdditionalGroup.top = new FormAttachment( 0, margin );
    fdAdditionalGroup.right = new FormAttachment( 100, -margin );
    wAdditionalGroup.setLayoutData( fdAdditionalGroup );

    // ///////////////////////////////////////////////////////////
    // / END OF DESTINATION ADDRESS GROUP
    // ///////////////////////////////////////////////////////////

    wlLimit = new Label( wSettingsComp, SWT.RIGHT );
    wlLimit.setText( BaseMessages.getString( PKG, "GetSubFoldersDialog.Limit.Label" ) );
    props.setLook( wlLimit );
    fdlLimit = new FormData();
    fdlLimit.left = new FormAttachment( 0, 0 );
    fdlLimit.top = new FormAttachment( wAdditionalGroup, 2 * margin );
    fdlLimit.right = new FormAttachment( middle, -margin );
    wlLimit.setLayoutData( fdlLimit );
    wLimit = new Text( wSettingsComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wLimit );
    wLimit.addModifyListener( lsMod );
    fdLimit = new FormData();
    fdLimit.left = new FormAttachment( middle, 0 );
    fdLimit.top = new FormAttachment( wAdditionalGroup, 2 * margin );
    fdLimit.right = new FormAttachment( 100, 0 );
    wLimit.setLayoutData( fdLimit );

    fdSettingsComp = new FormData();
    fdSettingsComp.left = new FormAttachment( 0, 0 );
    fdSettingsComp.top = new FormAttachment( 0, 0 );
    fdSettingsComp.right = new FormAttachment( 100, 0 );
    fdSettingsComp.bottom = new FormAttachment( 100, 0 );
    wSettingsComp.setLayoutData( fdSettingsComp );

    wSettingsComp.layout();
    wSettingsTab.setControl( wSettingsComp );

    // ///////////////////////////////////////////////////////////
    // / END OF FILE Filter TAB
    // ///////////////////////////////////////////////////////////

    wOK = new Button( shell, SWT.PUSH );
    wOK.setText( BaseMessages.getString( PKG, "System.Button.OK" ) );

    wPreview = new Button( shell, SWT.PUSH );
    wPreview.setText( BaseMessages.getString( PKG, "GetSubFoldersDialog.Preview.Button" ) );

    wCancel = new Button( shell, SWT.PUSH );
    wCancel.setText( BaseMessages.getString( PKG, "System.Button.Cancel" ) );

    setButtonPositions( new Button[] { wOK, wPreview, wCancel }, margin, wTabFolder );

    // Add listeners
    lsOK = new Listener() {
      public void handleEvent( Event e ) {
        ok();
      }
    };
    lsPreview = new Listener() {
      public void handleEvent( Event e ) {
        preview();
      }
    };
    lsCancel = new Listener() {
      public void handleEvent( Event e ) {
        cancel();
      }
    };

    wOK.addListener( SWT.Selection, lsOK );
    wPreview.addListener( SWT.Selection, lsPreview );
    wCancel.addListener( SWT.Selection, lsCancel );

    lsDef = new SelectionAdapter() {
      public void widgetDefaultSelected( SelectionEvent e ) {
        ok();
      }
    };

    wStepname.addSelectionListener( lsDef );

    // Add the file to the list of files...
    SelectionAdapter selA = new SelectionAdapter() {
      public void widgetSelected( SelectionEvent arg0 ) {
        wFoldernameList.add( new String[] { wFoldername.getText() } );
        wFoldername.setText( "" );
        wFoldernameList.removeEmptyRows();
        wFoldernameList.setRowNums();
        wFoldernameList.optWidth( true );
      }
    };
    wbaFoldername.addSelectionListener( selA );
    wFoldername.addSelectionListener( selA );

    // Delete files from the list of files...
    wbdFoldername.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent arg0 ) {
        int[] idx = wFoldernameList.getSelectionIndices();
        wFoldernameList.remove( idx );
        wFoldernameList.removeEmptyRows();
        wFoldernameList.setRowNums();
      }
    } );

    // Edit the selected file & remove from the list...
    wbeFoldername.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent arg0 ) {
        int idx = wFoldernameList.getSelectionIndex();
        if ( idx >= 0 ) {
          String[] string = wFoldernameList.getItem( idx );
          wFoldername.setText( string[0] );
          wFoldernameList.remove( idx );
        }
        wFoldernameList.removeEmptyRows();
        wFoldernameList.setRowNums();
      }
    } );

    // Listen to the Browse... button
    wbbFoldername.addSelectionListener( new SelectionAdapterFileDialogTextVar( log, wFoldername, transMeta,
      new SelectionAdapterOptions( SelectionOperation.FOLDER ) ) );

    // Detect X or ALT-F4 or something that kills this window...
    shell.addShellListener( new ShellAdapter() {
      public void shellClosed( ShellEvent e ) {
        cancel();
      }
    } );

    wTabFolder.setSelection( 0 );

    // Set the shell size, based upon previous time...
    getData( input );
    ActiveFileField();
    ActiveIncludeRowNum();
    setSize();

    shell.open();
    while ( !shell.isDisposed() ) {
      if ( !display.readAndDispatch() ) {
        display.sleep();
      }
    }
    return stepname;
  }

  private void ActiveIncludeRowNum() {
    wlInclRownumField.setEnabled( wInclRownum.getSelection() );
    wInclRownumField.setEnabled( wInclRownum.getSelection() );
  }

  private void ActiveFileField() {
    if ( wFolderField.getSelection() ) {
      wLimit.setText( "0" );
    }
    wlFilenameField.setEnabled( wFolderField.getSelection() );
    wFoldernameField.setEnabled( wFolderField.getSelection() );

    wlFoldername.setEnabled( !wFolderField.getSelection() );
    wbbFoldername.setEnabled( !wFolderField.getSelection() );
    wbaFoldername.setEnabled( !wFolderField.getSelection() );
    wFoldername.setEnabled( !wFolderField.getSelection() );
    wlFoldernameList.setEnabled( !wFolderField.getSelection() );
    wbdFoldername.setEnabled( !wFolderField.getSelection() );
    wbeFoldername.setEnabled( !wFolderField.getSelection() );
    wlFoldernameList.setEnabled( !wFolderField.getSelection() );
    wFoldernameList.setEnabled( !wFolderField.getSelection() );
    wPreview.setEnabled( !wFolderField.getSelection() );
    wlLimit.setEnabled( !wFolderField.getSelection() );
    wLimit.setEnabled( !wFolderField.getSelection() );

  }

  /**
   * Read the data from the TextFileInputMeta object and show it in this dialog.
   *
   * @param meta
   *          The TextFileInputMeta object to obtain the data from.
   */
  public void getData( GetSubFoldersMeta meta ) {
    final GetSubFoldersMeta in = meta;

    if ( in.getFolderName() != null ) {
      wFoldernameList.removeAll();
      for ( int i = 0; i < in.getFolderName().length; i++ ) {
        wFoldernameList.add( new String[] {
          in.getFolderName()[i], in.getRequiredFilesDesc( in.getFolderRequired()[i] ) } );

      }
      wFoldernameList.removeEmptyRows();
      wFoldernameList.setRowNums();
      wFoldernameList.optWidth( true );

      wInclRownum.setSelection( in.includeRowNumber() );
      wFolderField.setSelection( in.isFoldernameDynamic() );
      if ( in.getRowNumberField() != null ) {
        wInclRownumField.setText( in.getRowNumberField() );
      }
      if ( in.getDynamicFoldernameField() != null ) {
        wFoldernameField.setText( in.getDynamicFoldernameField() );
      }
      wLimit.setText( "" + in.getRowLimit() );

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
    getInfo( input );
    dispose();
  }

  private void getInfo( GetSubFoldersMeta in ) {
    stepname = wStepname.getText(); // return value

    int nrfiles = wFoldernameList.getItemCount();
    in.allocate( nrfiles );

    in.setFolderName( wFoldernameList.getItems( 0 ) );
    in.setFolderRequired( wFoldernameList.getItems( 1 ) );

    in.setIncludeRowNumber( wInclRownum.getSelection() );
    in.setDynamicFoldernameField( wFoldernameField.getText() );
    in.setFolderField( wFolderField.getSelection() );
    in.setRowNumberField( wInclRownumField.getText() );
    in.setRowLimit( Const.toLong( wLimit.getText(), 0L ) );

  }

  // Preview the data
  private void preview() {
    // Create the XML input step
    GetSubFoldersMeta oneMeta = new GetSubFoldersMeta();
    getInfo( oneMeta );

    TransMeta previewMeta =
      TransPreviewFactory.generatePreviewTransformation( transMeta, oneMeta, wStepname.getText() );

    EnterNumberDialog numberDialog = new EnterNumberDialog( shell, props.getDefaultPreviewSize(),
      BaseMessages.getString( PKG, "GetSubFoldersDialog.PreviewSize.DialogTitle" ),
      BaseMessages.getString( PKG, "GetSubFoldersDialog.PreviewSize.DialogMessage" ) );
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
          EnterTextDialog etd =
            new EnterTextDialog( shell, BaseMessages.getString( PKG, "System.Dialog.Error.Title" ), BaseMessages
              .getString( PKG, "GetSubFoldersDialog.ErrorInPreview.DialogMessage" ), loggingText, true );
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
  }
}
