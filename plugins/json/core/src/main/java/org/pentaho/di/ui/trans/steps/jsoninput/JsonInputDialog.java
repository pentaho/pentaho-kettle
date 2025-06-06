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


package org.pentaho.di.ui.trans.steps.jsoninput;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
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
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.fileinput.FileInputList;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaBase;
import org.pentaho.di.core.row.value.ValueMetaFactory;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.TransPreviewFactory;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.steps.jsoninput.JsonInputField;
import org.pentaho.di.trans.steps.jsoninput.JsonInputMeta;
import org.pentaho.di.ui.core.dialog.EnterNumberDialog;
import org.pentaho.di.ui.core.dialog.EnterSelectionDialog;
import org.pentaho.di.ui.core.dialog.EnterTextDialog;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.dialog.PreviewRowsDialog;
import org.pentaho.di.ui.core.events.dialog.FilterType;
import org.pentaho.di.ui.core.events.dialog.SelectionAdapterFileDialogTextVar;
import org.pentaho.di.ui.core.events.dialog.SelectionAdapterOptions;
import org.pentaho.di.ui.core.events.dialog.SelectionOperation;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.trans.dialog.TransPreviewProgressDialog;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.ui.trans.steps.jsoninput.getfields.GetFieldsDialog;

import java.util.ArrayList;
import java.util.List;

public class JsonInputDialog extends BaseStepDialog implements StepDialogInterface {

  private static final Class<?> PKG = JsonInputMeta.class;
  private static final String YES = BaseMessages.getString( PKG, "System.Combo.Yes" );
  private static final String NO = BaseMessages.getString( PKG, "System.Combo.No" );

  private CTabFolder wTabFolder;

  private Label wlFilename;
  private Label wlSourceIsAFile;
  private Button wbbFilename; // Browse: add file or directory
  private Button wbdFilename; // Delete
  private Button wbeFilename; // Edit
  private Button wbaFilename; // Add or change
  private TextVar wFilename;

  private Label wlFilenameList;
  private TableView wFilenameList;

  private Label wlFilemask;
  private TextVar wFilemask;

  private Button wbShowFiles;

  private Label wlSourceField;
  private CCombo wFieldValue;
  private Button wSourceStreamField;
  private Button wSourceIsAFile;

  private Label wlInclFilename;
  private Button wInclFilename;
  private Button wAddResult;

  private Label wlreadUrl;
  private Button wreadUrl;

  private Label wlremoveSourceField;
  private Button wremoveSourceField;
  private Label wlInclFilenameField;
  private TextVar wInclFilenameField;

  private Label wlAddResult;
  private Button wInclRownum;

  private Label wlInclRownumField;
  private TextVar wInclRownumField;

  private Label wlLimit;
  private Text wLimit;

  private TableView wFields;

  private Label wlExcludeFilemask;
  private TextVar wExcludeFilemask;

  // ignore empty files flag
  private Button wIgnoreEmptyFile;

  // ignore missing path
  private Button wIgnoreMissingPath;

  // default path leaf to null
  private Button wDefaultPathLeafToNull;

  // include null values
  private Button wIncludeNulls;

  // do not fail if no files?
  private Button wdoNotFailIfNoFile;

  private TextVar wShortFileFieldName;
  private TextVar wPathFieldName;

  private TextVar wIsHiddenName;
  private TextVar wLastModificationTimeName;
  private TextVar wUriName;
  private TextVar wRootUriName;
  private TextVar wExtensionFieldName;
  private TextVar wSizeFieldName;

  private JsonInputMeta input;

  private int middle;
  private int margin;
  private ModifyListener lsMod;

  public JsonInputDialog( Shell parent, Object in, TransMeta transMeta, String sname ) {
    super( parent, (BaseStepMeta) in, transMeta, sname );
    input = (JsonInputMeta) in;
  }

  public String open() {
    Shell parent = getParent();
    Display display = parent.getDisplay();

    shell = new Shell( parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN );
    props.setLook( shell );
    setShellImage( shell, input );

    lsMod = e -> input.setChanged();
    changed = input.hasChanged();

    FormLayout formLayout = new FormLayout();
    formLayout.marginWidth = Const.FORM_MARGIN;
    formLayout.marginHeight = Const.FORM_MARGIN;

    shell.setLayout( formLayout );
    shell.setText( BaseMessages.getString( PKG, "JsonInputDialog.DialogTitle" ) );

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

    addFileTab();

    addContentTab();

    addFieldsTab();

    addAdditionalFieldsTab();

    FormData fdTabFolder = new FormData();
    fdTabFolder.left = new FormAttachment( 0, 0 );
    fdTabFolder.top = new FormAttachment( wStepname, margin );
    fdTabFolder.right = new FormAttachment( 100, 0 );
    fdTabFolder.bottom = new FormAttachment( 100, -50 );
    wTabFolder.setLayoutData( fdTabFolder );

    wOK = new Button( shell, SWT.PUSH );
    wOK.setText( BaseMessages.getString( PKG, "System.Button.OK" ) );

    wPreview = new Button( shell, SWT.PUSH );
    wPreview.setText( BaseMessages.getString( PKG, "JsonInputDialog.Button.PreviewRows" ) );

    wCancel = new Button( shell, SWT.PUSH );
    wCancel.setText( BaseMessages.getString( PKG, "System.Button.Cancel" ) );

    setButtonPositions( new Button[] { wOK, wPreview, wCancel }, margin, wTabFolder );

    // Add listeners
    lsOK = e -> ok();
    lsGet = e -> get();
    lsPreview = e -> preview();
    lsCancel = e -> cancel();

    wOK.addListener( SWT.Selection, lsOK );
    wGet.addListener( SWT.Selection, lsGet );
    wPreview.addListener( SWT.Selection, lsPreview );
    wCancel.addListener( SWT.Selection, lsCancel );

    lsDef = new SelectionAdapter() {
      @Override
      public void widgetDefaultSelected( SelectionEvent e ) {
        ok();
      }
    };

    wStepname.addSelectionListener( lsDef );
    wLimit.addSelectionListener( lsDef );
    wInclRownumField.addSelectionListener( lsDef );
    wInclFilenameField.addSelectionListener( lsDef );

    // Add the file to the list of files...
    SelectionAdapter selA = new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent arg0 ) {
        String[] items = new String[] {
          wFilename.getText(), wFilemask.getText(), wExcludeFilemask.getText(),
          JsonInputMeta.RequiredFilesCode[0], JsonInputMeta.RequiredFilesCode[0] };
        wFilenameList.add( items );
        wFilename.setText( "" );
        wFilemask.setText( "" );
        wExcludeFilemask.setText( "" );
        wFilenameList.removeEmptyRows();
        wFilenameList.setRowNums();
        wFilenameList.optWidth( true );
      }
    };
    wbaFilename.addSelectionListener( selA );
    wFilename.addSelectionListener( selA );

    // Delete files from the list of files...
    wbdFilename.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent arg0 ) {
        int[] idx = wFilenameList.getSelectionIndices();
        wFilenameList.remove( idx );
        wFilenameList.removeEmptyRows();
        wFilenameList.setRowNums();
      }
    } );

    // Edit the selected file & remove from the list...
    wbeFilename.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent arg0 ) {
        int idx = wFilenameList.getSelectionIndex();
        if ( idx >= 0 ) {
          String[] string = wFilenameList.getItem( idx );
          wFilename.setText( string[0] );
          wFilemask.setText( string[1] );
          wExcludeFilemask.setText( string[2] );
          wFilenameList.remove( idx );
        }
        wFilenameList.removeEmptyRows();
        wFilenameList.setRowNums();
      }
    } );

    // Show the files that are selected at this time...
    wbShowFiles.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent e ) {
        try {
          JsonInputMeta tfii = new JsonInputMeta();
          getInfo( tfii );
          FileInputList fileInputList = tfii.getFiles( transMeta );
          String[] files = fileInputList.getFileStrings();
          if ( files != null && files.length > 0 ) {
            EnterSelectionDialog esd = new EnterSelectionDialog( shell, files,
              BaseMessages.getString( PKG, "JsonInputDialog.FilesReadSelection.DialogTitle" ),
              BaseMessages.getString( PKG, "JsonInputDialog.FilesReadSelection.DialogMessage" ) );
            esd.setViewOnly();
            esd.open();
          } else {
            MessageBox mb = new MessageBox( shell, SWT.OK | SWT.ICON_ERROR );
            mb.setMessage( BaseMessages.getString( PKG, "JsonInputDialog.NoFileFound.DialogMessage" ) );
            mb.setText( BaseMessages.getString( PKG, "System.Dialog.Error.Title" ) );
            mb.open();
          }
        } catch ( KettleException ex ) {
          new ErrorDialog( shell, BaseMessages.getString( PKG, "JsonInputDialog.ErrorParsingData.DialogTitle" ),
            BaseMessages.getString( PKG, "JsonInputDialog.ErrorParsingData.DialogMessage" ), ex );
        }
      }
    } );
    // Enable/disable the right fields to allow a filename to be added to each row...
    wInclFilename.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent e ) {
        setIncludeFilename();
      }
    } );

    // Enable/disable the right fields to allow a row number to be added to each row...
    wInclRownum.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent e ) {
        setIncludeRownum();
      }
    } );

    // Whenever something changes, set the tooltip to the expanded version of the filename:
    wFilename.addModifyListener( e -> wFilename.setToolTipText( wFilename.getText() ) );

    // Listen to the Browse... button
    wbbFilename.addSelectionListener( new SelectionAdapterFileDialogTextVar( log, wFilename, transMeta,
      new SelectionAdapterOptions( SelectionOperation.FILE_OR_FOLDER,
        new FilterType[] { FilterType.JSON, FilterType.JS, FilterType.ALL }, FilterType.JSON  ) ) );

    // Detect X or ALT-F4 or something that kills this window...
    shell.addShellListener( new ShellAdapter() {
      @Override
      public void shellClosed( ShellEvent e ) {
        cancel();
      }
    } );

    wTabFolder.setSelection( 0 );

    // Set the shell size, based upon previous time...
    setSize();
    getData( input );
    activeStreamField();
    setIncludeFilename();
    setIncludeRownum();
    input.setChanged( changed );
    wFields.optWidth( true );

    shell.open();
    while ( !shell.isDisposed() ) {
      if ( !display.readAndDispatch() ) {
        display.sleep();
      }
    }
    return stepname;
  }

  private void addFieldsTab() {
    // Fields tab...
    CTabItem wFieldsTab = new CTabItem( wTabFolder, SWT.NONE );
    wFieldsTab.setText( BaseMessages.getString( PKG, "JsonInputDialog.Fields.Tab" ) );

    FormLayout fieldsLayout = new FormLayout();
    fieldsLayout.marginWidth = Const.FORM_MARGIN;
    fieldsLayout.marginHeight = Const.FORM_MARGIN;

    Composite wFieldsComp = new Composite( wTabFolder, SWT.NONE );
    wFieldsComp.setLayout( fieldsLayout );
    props.setLook( wFieldsComp );

    wGet = new Button( wFieldsComp, SWT.PUSH );
    wGet.setText( BaseMessages.getString( PKG, "JsonInputDialog.Button.SelectFields" ) );
    fdGet = new FormData();
    fdGet.left = new FormAttachment( 50, 0 );
    fdGet.bottom = new FormAttachment( 100, 0 );
    wGet.setLayoutData( fdGet );

    setButtonPositions( new Button[] { wGet }, margin, null );

    final int FieldsRows = input.getInputFields().length;

    ColumnInfo[] colinf =
      new ColumnInfo[] {
        new ColumnInfo(
          BaseMessages.getString( PKG, "JsonInputDialog.FieldsTable.Name.Column" ),
          ColumnInfo.COLUMN_TYPE_TEXT, false ),
        new ColumnInfo(
          BaseMessages.getString( PKG, "JsonInputDialog.FieldsTable.Path.Column" ),
          ColumnInfo.COLUMN_TYPE_TEXT, false ),
        new ColumnInfo(
          BaseMessages.getString( PKG, "JsonInputDialog.FieldsTable.Type.Column" ),
          ColumnInfo.COLUMN_TYPE_CCOMBO, ValueMetaFactory.getValueMetaNames(), true ),
        new ColumnInfo(
          BaseMessages.getString( PKG, "JsonInputDialog.FieldsTable.Format.Column" ),
          ColumnInfo.COLUMN_TYPE_CCOMBO, Const.getConversionFormats() ),
        new ColumnInfo(
          BaseMessages.getString( PKG, "JsonInputDialog.FieldsTable.Length.Column" ),
          ColumnInfo.COLUMN_TYPE_TEXT, false ),
        new ColumnInfo(
          BaseMessages.getString( PKG, "JsonInputDialog.FieldsTable.Precision.Column" ),
          ColumnInfo.COLUMN_TYPE_TEXT, false ),
        new ColumnInfo(
          BaseMessages.getString( PKG, "JsonInputDialog.FieldsTable.Currency.Column" ),
          ColumnInfo.COLUMN_TYPE_TEXT, false ),
        new ColumnInfo(
          BaseMessages.getString( PKG, "JsonInputDialog.FieldsTable.Decimal.Column" ),
          ColumnInfo.COLUMN_TYPE_TEXT, false ),
        new ColumnInfo(
          BaseMessages.getString( PKG, "JsonInputDialog.FieldsTable.Group.Column" ),
          ColumnInfo.COLUMN_TYPE_TEXT, false ),
        new ColumnInfo(
          BaseMessages.getString( PKG, "JsonInputDialog.FieldsTable.TrimType.Column" ),
          ColumnInfo.COLUMN_TYPE_CCOMBO, ValueMetaBase.trimTypeDesc, true ),
        new ColumnInfo(
          BaseMessages.getString( PKG, "JsonInputDialog.FieldsTable.Repeat.Column" ),
          ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] {YES, NO }, true ),
      };

    colinf[0].setUsingVariables( true );
    colinf[0].setToolTip( BaseMessages.getString( PKG, "JsonInputDialog.FieldsTable.Name.Column.Tooltip" ) );
    colinf[1].setUsingVariables( true );
    colinf[1].setToolTip( BaseMessages.getString( PKG, "JsonInputDialog.FieldsTable.Path.Column.Tooltip" ) );

    wFields =
      new TableView( transMeta, wFieldsComp, SWT.FULL_SELECTION | SWT.MULTI, colinf, FieldsRows, lsMod, props );

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
  }

  private void addContentTab() {
    // ////////////////////////
    // START OF CONTENT TAB///
    // /
    CTabItem wContentTab = new CTabItem( wTabFolder, SWT.NONE );
    wContentTab.setText( BaseMessages.getString( PKG, "JsonInputDialog.Content.Tab" ) );

    FormLayout contentLayout = new FormLayout();
    contentLayout.marginWidth = 3;
    contentLayout.marginHeight = 3;

    Composite wContentComp = new Composite( wTabFolder, SWT.NONE );
    props.setLook( wContentComp );
    wContentComp.setLayout( contentLayout );

    // ///////////////////////////////
    // START OF Conf Field GROUP //
    // ///////////////////////////////

    Group wConf = new Group( wContentComp, SWT.SHADOW_NONE );
    props.setLook( wConf );
    wConf.setText( BaseMessages.getString( PKG, "JsonInputDialog.wConf.Label" ) );

    FormLayout confgroupLayout = new FormLayout();
    confgroupLayout.marginWidth = 10;
    confgroupLayout.marginHeight = 10;
    wConf.setLayout( confgroupLayout );

    // Ignore Empty File
    Label wlIgnoreEmptyFile = new Label( wConf, SWT.RIGHT );
    wlIgnoreEmptyFile.setText( BaseMessages.getString( PKG, "JsonInputDialog.IgnoreEmptyFile.Label" ) );
    props.setLook( wlIgnoreEmptyFile );

    FormData fdlIgnoreEmptyFile = new FormData();
    fdlIgnoreEmptyFile.left = new FormAttachment( 0, 0 );
    fdlIgnoreEmptyFile.top = new FormAttachment( 0, margin );
    fdlIgnoreEmptyFile.right = new FormAttachment( middle, -margin );
    wlIgnoreEmptyFile.setLayoutData( fdlIgnoreEmptyFile );
    wIgnoreEmptyFile = new Button( wConf, SWT.CHECK );
    props.setLook( wIgnoreEmptyFile );
    wIgnoreEmptyFile.setToolTipText( BaseMessages.getString( PKG, "JsonInputDialog.IgnoreEmptyFile.Tooltip" ) );

    FormData fdIgnoreEmptyFile = new FormData();
    fdIgnoreEmptyFile.left = new FormAttachment( middle, 0 );
    fdIgnoreEmptyFile.top = new FormAttachment( 0, margin );
    wIgnoreEmptyFile.setLayoutData( fdIgnoreEmptyFile );

    // do not fail if no files?
    Label wldoNotFailIfNoFile = new Label( wConf, SWT.RIGHT );
    wldoNotFailIfNoFile.setText( BaseMessages.getString( PKG, "JsonInputDialog.doNotFailIfNoFile.Label" ) );
    props.setLook( wldoNotFailIfNoFile );

    FormData fdldoNotFailIfNoFile = new FormData();
    fdldoNotFailIfNoFile.left = new FormAttachment( 0, 0 );
    fdldoNotFailIfNoFile.top = new FormAttachment( wIgnoreEmptyFile, margin );
    fdldoNotFailIfNoFile.right = new FormAttachment( middle, -margin );
    wldoNotFailIfNoFile.setLayoutData( fdldoNotFailIfNoFile );
    wdoNotFailIfNoFile = new Button( wConf, SWT.CHECK );
    props.setLook( wdoNotFailIfNoFile );
    wdoNotFailIfNoFile.setToolTipText( BaseMessages.getString( PKG, "JsonInputDialog.doNotFailIfNoFile.Tooltip" ) );

    FormData fddoNotFailIfNoFile = new FormData();
    fddoNotFailIfNoFile.left = new FormAttachment( middle, 0 );
    fddoNotFailIfNoFile.top = new FormAttachment( wIgnoreEmptyFile, margin );
    wdoNotFailIfNoFile.setLayoutData( fddoNotFailIfNoFile );

    // Ignore missing path
    Label wlIgnoreMissingPath = new Label( wConf, SWT.RIGHT );
    wlIgnoreMissingPath.setText( BaseMessages.getString( PKG, "JsonInputDialog.IgnoreMissingPath.Label" ) );
    props.setLook( wlIgnoreMissingPath );

    FormData fdlIgnoreMissingPath = new FormData();
    fdlIgnoreMissingPath.left = new FormAttachment( 0, 0 );
    fdlIgnoreMissingPath.top = new FormAttachment( wdoNotFailIfNoFile, margin );
    fdlIgnoreMissingPath.right = new FormAttachment( middle, -margin );
    wlIgnoreMissingPath.setLayoutData( fdlIgnoreMissingPath );
    wIgnoreMissingPath = new Button( wConf, SWT.CHECK );
    props.setLook( wIgnoreMissingPath );
    wIgnoreMissingPath.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent e ) {
        input.setChanged();
      }
    } );
    wIgnoreMissingPath.setToolTipText( BaseMessages.getString( PKG, "JsonInputDialog.IgnoreMissingPath.Tooltip" ) );

    FormData fdIgnoreMissingPath = new FormData();
    fdIgnoreMissingPath.left = new FormAttachment( middle, 0 );
    fdIgnoreMissingPath.top = new FormAttachment( wdoNotFailIfNoFile, margin );
    wIgnoreMissingPath.setLayoutData( fdIgnoreMissingPath );

    // default path leaf to null
    Label wlDefaultPathLeafToNull = new Label( wConf, SWT.RIGHT );
    wlDefaultPathLeafToNull.setText( BaseMessages.getString( PKG, "JsonInputDialog.DefaultPathLeafToNull.Label" ) );
    props.setLook( wlDefaultPathLeafToNull );

    FormData fdlDefaultPathLeafToNull = new FormData();
    fdlDefaultPathLeafToNull.left = new FormAttachment( 0, 0 );
    fdlDefaultPathLeafToNull.top = new FormAttachment( wIgnoreMissingPath, margin );
    fdlDefaultPathLeafToNull.right = new FormAttachment( middle, -margin );
    wlDefaultPathLeafToNull.setLayoutData( fdlDefaultPathLeafToNull );
    wDefaultPathLeafToNull = new Button( wConf, SWT.CHECK );
    props.setLook( wDefaultPathLeafToNull );
    wDefaultPathLeafToNull.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent e ) {
        input.setChanged();
      }
    } );
    wDefaultPathLeafToNull.setToolTipText( BaseMessages.getString( PKG, "JsonInputDialog.DefaultPathLeafToNull.Tooltip" ) );

    FormData fdDefaultPathLeafToNull = new FormData();
    fdDefaultPathLeafToNull.left = new FormAttachment( middle, 0 );
    fdDefaultPathLeafToNull.top = new FormAttachment( wIgnoreMissingPath, margin );
    wDefaultPathLeafToNull.setLayoutData( fdDefaultPathLeafToNull );
    // default path leaf to null - end

    Label wlIncludeNulls = new Label( wConf, SWT.RIGHT );
    wlIncludeNulls.setText( BaseMessages.getString( PKG, "JsonInputDialog.IncludeNulls.Label" ) );
    props.setLook( wlIncludeNulls );

    FormData fdlIncludeNulls = new FormData();
    fdlIncludeNulls.left = new FormAttachment( 0, 0 );
    fdlIncludeNulls.top = new FormAttachment( wDefaultPathLeafToNull, margin );
    fdlIncludeNulls.right = new FormAttachment( middle, -margin );
    wlIncludeNulls.setLayoutData( fdlIncludeNulls );
    wIncludeNulls = new Button( wConf, SWT.CHECK );
    props.setLook( wIncludeNulls );
    wIncludeNulls.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent e ) {
        input.setChanged();
      }
    } );
    wIncludeNulls.setToolTipText( BaseMessages.getString( PKG, "JsonInputDialog.IncludeNulls.Tooltip" ) );

    FormData fdIncludeNulls = new FormData();
    fdIncludeNulls.left = new FormAttachment( middle, 0 );
    fdIncludeNulls.top = new FormAttachment( wDefaultPathLeafToNull, margin );
    wIncludeNulls.setLayoutData( fdIncludeNulls );
    // Include nulls - end

    wlLimit = new Label( wConf, SWT.RIGHT );
    wlLimit.setText( BaseMessages.getString( PKG, "JsonInputDialog.Limit.Label" ) );
    props.setLook( wlLimit );

    FormData fdlLimit = new FormData();
    fdlLimit.left = new FormAttachment( 0, 0 );
    fdlLimit.top = new FormAttachment( wIncludeNulls, margin );
    fdlLimit.right = new FormAttachment( middle, -margin );
    wlLimit.setLayoutData( fdlLimit );
    wLimit = new Text( wConf, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wLimit );
    wLimit.addModifyListener( lsMod );

    FormData fdLimit = new FormData();
    fdLimit.left = new FormAttachment( middle, 0 );
    fdLimit.top = new FormAttachment( wIncludeNulls, margin );
    fdLimit.right = new FormAttachment( 100, 0 );
    wLimit.setLayoutData( fdLimit );

    FormData fdConf = new FormData();
    fdConf.left = new FormAttachment( 0, margin );
    fdConf.top = new FormAttachment( 0, margin );
    fdConf.right = new FormAttachment( 100, -margin );
    wConf.setLayoutData( fdConf );

    // ///////////////////////////////////////////////////////////
    // / END OF Conf Field GROUP
    // ///////////////////////////////////////////////////////////

    // ///////////////////////////////
    // START OF Additional Fields GROUP //
    // ///////////////////////////////

    Group wAdditionalFields = new Group( wContentComp, SWT.SHADOW_NONE );
    props.setLook( wAdditionalFields );
    wAdditionalFields.setText( BaseMessages.getString( PKG, "JsonInputDialog.wAdditionalFields.Label" ) );

    FormLayout additionalFieldsgroupLayout = new FormLayout();
    additionalFieldsgroupLayout.marginWidth = 10;
    additionalFieldsgroupLayout.marginHeight = 10;
    wAdditionalFields.setLayout( additionalFieldsgroupLayout );

    wlInclFilename = new Label( wAdditionalFields, SWT.RIGHT );
    wlInclFilename.setText( BaseMessages.getString( PKG, "JsonInputDialog.InclFilename.Label" ) );
    props.setLook( wlInclFilename );

    FormData fdlInclFilename = new FormData();
    fdlInclFilename.left = new FormAttachment( 0, 0 );
    fdlInclFilename.top = new FormAttachment( wConf, 4 * margin );
    fdlInclFilename.right = new FormAttachment( middle, -margin );
    wlInclFilename.setLayoutData( fdlInclFilename );
    wInclFilename = new Button( wAdditionalFields, SWT.CHECK );
    props.setLook( wInclFilename );
    wInclFilename.setToolTipText( BaseMessages.getString( PKG, "JsonInputDialog.InclFilename.Tooltip" ) );

    FormData fdInclFilename = new FormData();
    fdInclFilename.left = new FormAttachment( middle, 0 );
    fdInclFilename.top = new FormAttachment( wConf, 4 * margin );
    wInclFilename.setLayoutData( fdInclFilename );

    wlInclFilenameField = new Label( wAdditionalFields, SWT.LEFT );
    wlInclFilenameField.setText( BaseMessages.getString( PKG, "JsonInputDialog.InclFilenameField.Label" ) );
    props.setLook( wlInclFilenameField );

    FormData fdlInclFilenameField = new FormData();
    fdlInclFilenameField.left = new FormAttachment( wInclFilename, margin );
    fdlInclFilenameField.top = new FormAttachment( wLimit, 4 * margin );
    wlInclFilenameField.setLayoutData( fdlInclFilenameField );
    wInclFilenameField = new TextVar( transMeta, wAdditionalFields, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wInclFilenameField );
    wInclFilenameField.addModifyListener( lsMod );

    FormData fdInclFilenameField = new FormData();
    fdInclFilenameField.left = new FormAttachment( wlInclFilenameField, margin );
    fdInclFilenameField.top = new FormAttachment( wLimit, 4 * margin );
    fdInclFilenameField.right = new FormAttachment( 100, 0 );
    wInclFilenameField.setLayoutData( fdInclFilenameField );

    Label wlInclRownum = new Label( wAdditionalFields, SWT.RIGHT );
    wlInclRownum.setText( BaseMessages.getString( PKG, "JsonInputDialog.InclRownum.Label" ) );
    props.setLook( wlInclRownum );

    FormData fdlInclRownum = new FormData();
    fdlInclRownum.left = new FormAttachment( 0, 0 );
    fdlInclRownum.top = new FormAttachment( wInclFilenameField, margin );
    fdlInclRownum.right = new FormAttachment( middle, -margin );
    wlInclRownum.setLayoutData( fdlInclRownum );
    wInclRownum = new Button( wAdditionalFields, SWT.CHECK );
    props.setLook( wInclRownum );
    wInclRownum.setToolTipText( BaseMessages.getString( PKG, "JsonInputDialog.InclRownum.Tooltip" ) );

    FormData fdRownum = new FormData();
    fdRownum.left = new FormAttachment( middle, 0 );
    fdRownum.top = new FormAttachment( wInclFilenameField, margin );
    wInclRownum.setLayoutData( fdRownum );

    wlInclRownumField = new Label( wAdditionalFields, SWT.RIGHT );
    wlInclRownumField.setText( BaseMessages.getString( PKG, "JsonInputDialog.InclRownumField.Label" ) );
    props.setLook( wlInclRownumField );

    FormData fdlInclRownumField = new FormData();
    fdlInclRownumField.left = new FormAttachment( wInclRownum, margin );
    fdlInclRownumField.top = new FormAttachment( wInclFilenameField, margin );
    wlInclRownumField.setLayoutData( fdlInclRownumField );
    wInclRownumField = new TextVar( transMeta, wAdditionalFields, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wInclRownumField );
    wInclRownumField.addModifyListener( lsMod );

    FormData fdInclRownumField = new FormData();
    fdInclRownumField.left = new FormAttachment( wlInclRownumField, margin );
    fdInclRownumField.top = new FormAttachment( wInclFilenameField, margin );
    fdInclRownumField.right = new FormAttachment( 100, 0 );
    wInclRownumField.setLayoutData( fdInclRownumField );

    FormData fdAdditionalFields = new FormData();
    fdAdditionalFields.left = new FormAttachment( 0, margin );
    fdAdditionalFields.top = new FormAttachment( wConf, margin );
    fdAdditionalFields.right = new FormAttachment( 100, -margin );
    wAdditionalFields.setLayoutData( fdAdditionalFields );

    // ///////////////////////////////////////////////////////////
    // / END OF Additional Fields GROUP
    // ///////////////////////////////////////////////////////////

    // ///////////////////////////////
    // START OF AddFileResult GROUP //
    // ///////////////////////////////

    Group wAddFileResult = new Group( wContentComp, SWT.SHADOW_NONE );
    props.setLook( wAddFileResult );
    wAddFileResult.setText( BaseMessages.getString( PKG, "JsonInputDialog.wAddFileResult.Label" ) );

    FormLayout addFileResultgroupLayout = new FormLayout();
    addFileResultgroupLayout.marginWidth = 10;
    addFileResultgroupLayout.marginHeight = 10;
    wAddFileResult.setLayout( addFileResultgroupLayout );

    wlAddResult = new Label( wAddFileResult, SWT.RIGHT );
    wlAddResult.setText( BaseMessages.getString( PKG, "JsonInputDialog.AddResult.Label" ) );
    props.setLook( wlAddResult );

    FormData fdlAddResult = new FormData();
    fdlAddResult.left = new FormAttachment( 0, 0 );
    fdlAddResult.top = new FormAttachment( wAdditionalFields, margin );
    fdlAddResult.right = new FormAttachment( middle, -margin );
    wlAddResult.setLayoutData( fdlAddResult );
    wAddResult = new Button( wAddFileResult, SWT.CHECK );
    props.setLook( wAddResult );
    wAddResult.setToolTipText( BaseMessages.getString( PKG, "JsonInputDialog.AddResult.Tooltip" ) );

    FormData fdAddResult = new FormData();
    fdAddResult.left = new FormAttachment( middle, 0 );
    fdAddResult.top = new FormAttachment( wAdditionalFields, margin );
    wAddResult.setLayoutData( fdAddResult );

    FormData fdAddFileResult = new FormData();
    fdAddFileResult.left = new FormAttachment( 0, margin );
    fdAddFileResult.top = new FormAttachment( wAdditionalFields, margin );
    fdAddFileResult.right = new FormAttachment( 100, -margin );
    wAddFileResult.setLayoutData( fdAddFileResult );

    // ///////////////////////////////////////////////////////////
    // / END OF AddFileResult GROUP
    // ///////////////////////////////////////////////////////////

    FormData fdContentComp = new FormData();
    fdContentComp.left = new FormAttachment( 0, 0 );
    fdContentComp.top = new FormAttachment( 0, 0 );
    fdContentComp.right = new FormAttachment( 100, 0 );
    fdContentComp.bottom = new FormAttachment( 100, 0 );
    wContentComp.setLayoutData( fdContentComp );

    wContentComp.layout();
    wContentTab.setControl( wContentComp );

    // ///////////////////////////////////////////////////////////
    // / END OF CONTENT TAB
    // ///////////////////////////////////////////////////////////
  }

  private void addFileTab() {
    // ////////////////////////
    // START OF FILE TAB ///
    // ////////////////////////
    CTabItem wFileTab = new CTabItem( wTabFolder, SWT.NONE );
    wFileTab.setText( BaseMessages.getString( PKG, "JsonInputDialog.File.Tab" ) );

    Composite wFileComp = new Composite( wTabFolder, SWT.NONE );
    props.setLook( wFileComp );

    FormLayout fileLayout = new FormLayout();
    fileLayout.marginWidth = 3;
    fileLayout.marginHeight = 3;
    wFileComp.setLayout( fileLayout );

    // ///////////////////////////////
    // START OF Output Field GROUP //
    // ///////////////////////////////

    Group wOutputField = new Group( wFileComp, SWT.SHADOW_NONE );
    props.setLook( wOutputField );
    wOutputField.setText( BaseMessages.getString( PKG, "JsonInputDialog.wOutputField.Label" ) );

    FormLayout outputfieldgroupLayout = new FormLayout();
    outputfieldgroupLayout.marginWidth = 10;
    outputfieldgroupLayout.marginHeight = 10;
    wOutputField.setLayout( outputfieldgroupLayout );

    // Is source string defined in a Field
    Label wlSourceStreamField = new Label( wOutputField, SWT.RIGHT );
    wlSourceStreamField.setText( BaseMessages.getString( PKG, "JsonInputDialog.wlSourceStreamField.Label" ) );
    props.setLook( wlSourceStreamField );

    FormData fdlSourceStreamField = new FormData();
    fdlSourceStreamField.left = new FormAttachment( 0, -margin );
    fdlSourceStreamField.top = new FormAttachment( 0, margin );
    fdlSourceStreamField.right = new FormAttachment( middle, -2 * margin );
    wlSourceStreamField.setLayoutData( fdlSourceStreamField );

    wSourceStreamField = new Button( wOutputField, SWT.CHECK );
    props.setLook( wSourceStreamField );
    wSourceStreamField
      .setToolTipText( BaseMessages.getString( PKG, "JsonInputDialog.wSourceStreamField.Tooltip" ) );

    FormData fdSourceStreamField = new FormData();
    fdSourceStreamField.left = new FormAttachment( middle, -margin );
    fdSourceStreamField.top = new FormAttachment( 0, margin );
    wSourceStreamField.setLayoutData( fdSourceStreamField );
    SelectionAdapter lsstream = new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent arg0 ) {
        activeStreamField();
        input.setChanged();
      }
    };
    wSourceStreamField.addSelectionListener( lsstream );

    // If source string defined in a Field
    wlSourceField = new Label( wOutputField, SWT.RIGHT );
    wlSourceField.setText( BaseMessages.getString( PKG, "JsonInputDialog.wlSourceField.Label" ) );
    props.setLook( wlSourceField );

    FormData fdlFieldValue = new FormData();
    fdlFieldValue.left = new FormAttachment( 0, -margin );
    fdlFieldValue.top = new FormAttachment( wSourceStreamField, margin );
    fdlFieldValue.right = new FormAttachment( middle, -2 * margin );
    wlSourceField.setLayoutData( fdlFieldValue );

    wFieldValue = new CCombo( wOutputField, SWT.BORDER | SWT.READ_ONLY );
    wFieldValue.setEditable( true );
    props.setLook( wFieldValue );
    wFieldValue.addModifyListener( lsMod );

    FormData fdFieldValue = new FormData();
    fdFieldValue.left = new FormAttachment( middle, -margin );
    fdFieldValue.top = new FormAttachment( wSourceStreamField, margin );
    fdFieldValue.right = new FormAttachment( 100, -margin );
    wFieldValue.setLayoutData( fdFieldValue );
    setSourceStreamField();

    FormData fdOutputField = new FormData();
    fdOutputField.left = new FormAttachment( 0, margin );
    fdOutputField.top = new FormAttachment( wFilenameList, margin );
    fdOutputField.right = new FormAttachment( 100, -margin );
    wOutputField.setLayoutData( fdOutputField );

    // Is source is a file?
    wlSourceIsAFile = new Label( wOutputField, SWT.RIGHT );
    wlSourceIsAFile.setText( BaseMessages.getString( PKG, "JsonInputDialog.SourceIsAFile.Label" ) );
    props.setLook( wlSourceIsAFile );

    FormData fdlSourceIsAFile = new FormData();
    fdlSourceIsAFile.left = new FormAttachment( 0, -margin );
    fdlSourceIsAFile.top = new FormAttachment( wFieldValue, margin );
    fdlSourceIsAFile.right = new FormAttachment( middle, -2 * margin );
    wlSourceIsAFile.setLayoutData( fdlSourceIsAFile );

    wSourceIsAFile = new Button( wOutputField, SWT.CHECK );
    props.setLook( wSourceIsAFile );
    wSourceIsAFile.setToolTipText( BaseMessages.getString( PKG, "JsonInputDialog.SourceIsAFile.Tooltip" ) );

    FormData fdSourceIsAFile = new FormData();
    fdSourceIsAFile.left = new FormAttachment( middle, -margin );
    fdSourceIsAFile.top = new FormAttachment( wFieldValue, margin );
    wSourceIsAFile.setLayoutData( fdSourceIsAFile );
    SelectionAdapter lssourceisafile = new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent arg0 ) {
        if ( wSourceIsAFile.getSelection() ) {
          wreadUrl.setSelection( false );
        }
        input.setChanged();
      }
    };
    wSourceIsAFile.addSelectionListener( lssourceisafile );

    // read url as source ?
    wlreadUrl = new Label( wOutputField, SWT.RIGHT );
    wlreadUrl.setText( BaseMessages.getString( PKG, "JsonInputDialog.readUrl.Label" ) );
    props.setLook( wlreadUrl );

    FormData fdlreadUrl = new FormData();
    fdlreadUrl.left = new FormAttachment( 0, -margin );
    fdlreadUrl.top = new FormAttachment( wlSourceIsAFile, margin );
    fdlreadUrl.right = new FormAttachment( middle, -2 * margin );
    wlreadUrl.setLayoutData( fdlreadUrl );
    wreadUrl = new Button( wOutputField, SWT.CHECK );
    props.setLook( wreadUrl );
    wreadUrl.setToolTipText( BaseMessages.getString( PKG, "JsonInputDialog.readUrl.Tooltip" ) );

    FormData fdreadUrl = new FormData();
    fdreadUrl.left = new FormAttachment( middle, -margin );
    fdreadUrl.top = new FormAttachment( wlSourceIsAFile, margin );
    wreadUrl.setLayoutData( fdreadUrl );
    SelectionAdapter lsreadurl = new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent arg0 ) {
        if ( wreadUrl.getSelection() ) {
          wSourceIsAFile.setSelection( false );
        }
        input.setChanged();
      }
    };
    wreadUrl.addSelectionListener( lsreadurl );

    // Remove source field from output stream?
    wlremoveSourceField = new Label( wOutputField, SWT.RIGHT );
    wlremoveSourceField.setText( BaseMessages.getString( PKG, "JsonInputDialog.removeSourceField.Label" ) );
    props.setLook( wlremoveSourceField );

    FormData fdlremoveSourceField = new FormData();
    fdlremoveSourceField.left = new FormAttachment( 0, -margin );
    fdlremoveSourceField.top = new FormAttachment( wlreadUrl, margin );
    fdlremoveSourceField.right = new FormAttachment( middle, -2 * margin );
    wlremoveSourceField.setLayoutData( fdlremoveSourceField );
    wremoveSourceField = new Button( wOutputField, SWT.CHECK );
    props.setLook( wremoveSourceField );

    FormData fdremoveSourceField = new FormData();
    fdremoveSourceField.left = new FormAttachment( middle, -margin );
    fdremoveSourceField.top = new FormAttachment( wlreadUrl, margin );
    wremoveSourceField.setLayoutData( fdremoveSourceField );
    SelectionAdapter removeSourceFieldAdapter = new SelectionAdapter() {
      @Override public void widgetSelected( SelectionEvent selectionEvent ) {
        input.setChanged();
      }
    };
    wremoveSourceField.addSelectionListener( removeSourceFieldAdapter );

    // ///////////////////////////////////////////////////////////
    // / END OF Output Field GROUP
    // ///////////////////////////////////////////////////////////

    // Filename line
    wlFilename = new Label( wFileComp, SWT.RIGHT );
    wlFilename.setText( BaseMessages.getString( PKG, "JsonInputDialog.Filename.Label" ) );
    props.setLook( wlFilename );

    FormData fdlFilename = new FormData();
    fdlFilename.left = new FormAttachment( 0, 0 );
    fdlFilename.top = new FormAttachment( wOutputField, margin );
    fdlFilename.right = new FormAttachment( middle, -margin );
    wlFilename.setLayoutData( fdlFilename );

    wbbFilename = new Button( wFileComp, SWT.PUSH | SWT.CENTER );
    props.setLook( wbbFilename );
    wbbFilename.setText( BaseMessages.getString( PKG, "JsonInputDialog.FilenameBrowse.Button" ) );
    wbbFilename.setToolTipText( BaseMessages.getString( PKG, "System.Tooltip.BrowseForFileOrDirAndAdd" ) );

    FormData fdbFilename = new FormData();
    fdbFilename.right = new FormAttachment( 100, 0 );
    fdbFilename.top = new FormAttachment( wOutputField, margin );
    wbbFilename.setLayoutData( fdbFilename );

    wbaFilename = new Button( wFileComp, SWT.PUSH | SWT.CENTER );
    props.setLook( wbaFilename );
    wbaFilename.setText( BaseMessages.getString( PKG, "JsonInputDialog.FilenameAdd.Button" ) );
    wbaFilename.setToolTipText( BaseMessages.getString( PKG, "JsonInputDialog.FilenameAdd.Tooltip" ) );

    FormData fdbaFilename = new FormData();
    fdbaFilename.right = new FormAttachment( wbbFilename, -margin );
    fdbaFilename.top = new FormAttachment( wOutputField, margin );
    wbaFilename.setLayoutData( fdbaFilename );

    wFilename = new TextVar( transMeta, wFileComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wFilename );
    wFilename.addModifyListener( lsMod );

    FormData fdFilename = new FormData();
    fdFilename.left = new FormAttachment( middle, 0 );
    fdFilename.right = new FormAttachment( wbaFilename, -margin );
    fdFilename.top = new FormAttachment( wOutputField, margin );
    wFilename.setLayoutData( fdFilename );

    wlFilemask = new Label( wFileComp, SWT.RIGHT );
    wlFilemask.setText( BaseMessages.getString( PKG, "JsonInputDialog.RegExp.Label" ) );
    props.setLook( wlFilemask );

    FormData fdlFilemask = new FormData();
    fdlFilemask.left = new FormAttachment( 0, 0 );
    fdlFilemask.top = new FormAttachment( wFilename, margin );
    fdlFilemask.right = new FormAttachment( middle, -margin );
    wlFilemask.setLayoutData( fdlFilemask );
    wFilemask = new TextVar( transMeta, wFileComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wFilemask );
    wFilemask.addModifyListener( lsMod );

    FormData fdFilemask = new FormData();
    fdFilemask.left = new FormAttachment( middle, 0 );
    fdFilemask.top = new FormAttachment( wFilename, margin );
    fdFilemask.right = new FormAttachment( 100, 0 );
    wFilemask.setLayoutData( fdFilemask );

    wlExcludeFilemask = new Label( wFileComp, SWT.RIGHT );
    wlExcludeFilemask.setText( BaseMessages.getString( PKG, "JsonInputDialog.ExcludeFilemask.Label" ) );
    props.setLook( wlExcludeFilemask );

    FormData fdlExcludeFilemask = new FormData();
    fdlExcludeFilemask.left = new FormAttachment( 0, 0 );
    fdlExcludeFilemask.top = new FormAttachment( wFilemask, margin );
    fdlExcludeFilemask.right = new FormAttachment( middle, -margin );
    wlExcludeFilemask.setLayoutData( fdlExcludeFilemask );
    wExcludeFilemask = new TextVar( transMeta, wFileComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wExcludeFilemask );
    wExcludeFilemask.addModifyListener( lsMod );

    FormData fdExcludeFilemask = new FormData();
    fdExcludeFilemask.left = new FormAttachment( middle, 0 );
    fdExcludeFilemask.top = new FormAttachment( wFilemask, margin );
    fdExcludeFilemask.right = new FormAttachment( wFilename, 0, SWT.RIGHT );
    wExcludeFilemask.setLayoutData( fdExcludeFilemask );

    // Filename list line
    wlFilenameList = new Label( wFileComp, SWT.RIGHT );
    wlFilenameList.setText( BaseMessages.getString( PKG, "JsonInputDialog.FilenameList.Label" ) );
    props.setLook( wlFilenameList );

    FormData fdlFilenameList = new FormData();
    fdlFilenameList.left = new FormAttachment( 0, 0 );
    fdlFilenameList.top = new FormAttachment( wExcludeFilemask, margin );
    fdlFilenameList.right = new FormAttachment( middle, -margin );
    wlFilenameList.setLayoutData( fdlFilenameList );

    // Buttons to the right of the screen...
    wbdFilename = new Button( wFileComp, SWT.PUSH | SWT.CENTER );
    props.setLook( wbdFilename );
    wbdFilename.setText( BaseMessages.getString( PKG, "JsonInputDialog.FilenameRemove.Button" ) );
    wbdFilename.setToolTipText( BaseMessages.getString( PKG, "JsonInputDialog.FilenameRemove.Tooltip" ) );

    FormData fdbdFilename = new FormData();
    fdbdFilename.right = new FormAttachment( 100, 0 );
    fdbdFilename.top = new FormAttachment( wExcludeFilemask, 40 );
    wbdFilename.setLayoutData( fdbdFilename );

    wbeFilename = new Button( wFileComp, SWT.PUSH | SWT.CENTER );
    props.setLook( wbeFilename );
    wbeFilename.setText( BaseMessages.getString( PKG, "JsonInputDialog.FilenameEdit.Button" ) );
    wbeFilename.setToolTipText( BaseMessages.getString( PKG, "JsonInputDialog.FilenameEdit.Tooltip" ) );

    FormData fdbeFilename = new FormData();
    fdbeFilename.right = new FormAttachment( 100, 0 );
    fdbeFilename.left = new FormAttachment( wbdFilename, 0, SWT.LEFT );
    fdbeFilename.top = new FormAttachment( wbdFilename, margin );
    wbeFilename.setLayoutData( fdbeFilename );

    wbShowFiles = new Button( wFileComp, SWT.PUSH | SWT.CENTER );
    props.setLook( wbShowFiles );
    wbShowFiles.setText( BaseMessages.getString( PKG, "JsonInputDialog.ShowFiles.Button" ) );

    FormData fdbShowFiles = new FormData();
    fdbShowFiles.left = new FormAttachment( middle, 0 );
    fdbShowFiles.bottom = new FormAttachment( 100, 0 );
    wbShowFiles.setLayoutData( fdbShowFiles );

    ColumnInfo[] colinfo = new ColumnInfo[5];
    colinfo[0] =
      new ColumnInfo(
        BaseMessages.getString( PKG, "JsonInputDialog.Files.Filename.Column" ), ColumnInfo.COLUMN_TYPE_TEXT,
        false );
    colinfo[1] =
      new ColumnInfo(
        BaseMessages.getString( PKG, "JsonInputDialog.Files.Wildcard.Column" ), ColumnInfo.COLUMN_TYPE_TEXT,
        false );
    colinfo[2] =
      new ColumnInfo(
        BaseMessages.getString( PKG, "JsonInputDialog.Files.ExcludeWildcard.Column" ),
        ColumnInfo.COLUMN_TYPE_TEXT, false );

    colinfo[0].setUsingVariables( true );
    colinfo[1].setUsingVariables( true );
    colinfo[1].setToolTip( BaseMessages.getString( PKG, "JsonInputDialog.Files.Wildcard.Tooltip" ) );
    colinfo[2].setUsingVariables( true );
    colinfo[2].setToolTip( BaseMessages.getString( PKG, "JsonInputDialog.Files.ExcludeWildcard.Tooltip" ) );
    colinfo[3] =
      new ColumnInfo(
        BaseMessages.getString( PKG, "JsonInputDialog.Required.Column" ), ColumnInfo.COLUMN_TYPE_CCOMBO,
        JsonInputMeta.RequiredFilesDesc );
    colinfo[3].setToolTip( BaseMessages.getString( PKG, "JsonInputDialog.Required.Tooltip" ) );
    colinfo[4] =
      new ColumnInfo(
        BaseMessages.getString( PKG, "JsonInputDialog.IncludeSubDirs.Column" ), ColumnInfo.COLUMN_TYPE_CCOMBO,
        JsonInputMeta.RequiredFilesDesc );
    colinfo[4].setToolTip( BaseMessages.getString( PKG, "JsonInputDialog.IncludeSubDirs.Tooltip" ) );

    wFilenameList =
      new TableView(
        transMeta, wFileComp, SWT.FULL_SELECTION | SWT.SINGLE | SWT.BORDER, colinfo, 2, lsMod, props );
    props.setLook( wFilenameList );

    FormData fdFilenameList = new FormData();
    fdFilenameList.left = new FormAttachment( middle, 0 );
    fdFilenameList.right = new FormAttachment( wbdFilename, -margin );
    fdFilenameList.top = new FormAttachment( wExcludeFilemask, margin );
    fdFilenameList.bottom = new FormAttachment( wbShowFiles, -margin );
    wFilenameList.setLayoutData( fdFilenameList );

    FormData fdFileComp = new FormData();
    fdFileComp.left = new FormAttachment( 0, 0 );
    fdFileComp.top = new FormAttachment( 0, 0 );
    fdFileComp.right = new FormAttachment( 100, 0 );
    fdFileComp.bottom = new FormAttachment( 100, 0 );
    wFileComp.setLayoutData( fdFileComp );

    wFileComp.layout();
    wFileTab.setControl( wFileComp );

    // ///////////////////////////////////////////////////////////
    // / END OF FILE TAB
    // ///////////////////////////////////////////////////////////
  }

  private void setSourceStreamField() {
    try {
      String value = wFieldValue.getText();
      wFieldValue.removeAll();

      RowMetaInterface r = transMeta.getPrevStepFields( stepname );
      if ( r != null ) {
        wFieldValue.setItems( r.getFieldNames() );
      }
      if ( value != null ) {
        wFieldValue.setText( value );
      }
    } catch ( KettleException ke ) {
      new ErrorDialog( shell, BaseMessages.getString( PKG, "JsonInputDialog.FailedToGetFields.DialogTitle" ),
          BaseMessages.getString( PKG, "JsonInputDialog.FailedToGetFields.DialogMessage" ), ke );
    }
  }

  private void activeStreamField() {
    wlSourceField.setEnabled( wSourceStreamField.getSelection() );
    wFieldValue.setEnabled( wSourceStreamField.getSelection() );
    wlSourceIsAFile.setEnabled( wSourceStreamField.getSelection() );
    wSourceIsAFile.setEnabled( wSourceStreamField.getSelection() );
    wlreadUrl.setEnabled( wSourceStreamField.getSelection() );
    wreadUrl.setEnabled( wSourceStreamField.getSelection() );
    wlremoveSourceField.setEnabled( wSourceStreamField.getSelection() );
    wremoveSourceField.setEnabled( wSourceStreamField.getSelection() );

    wlFilename.setEnabled( !wSourceStreamField.getSelection() );
    wbbFilename.setEnabled( !wSourceStreamField.getSelection() );
    wbaFilename.setEnabled( !wSourceStreamField.getSelection() );
    wFilename.setEnabled( !wSourceStreamField.getSelection() );
    wlExcludeFilemask.setEnabled( !wSourceStreamField.getSelection() );
    wExcludeFilemask.setEnabled( !wSourceStreamField.getSelection() );
    wlFilemask.setEnabled( !wSourceStreamField.getSelection() );
    wFilemask.setEnabled( !wSourceStreamField.getSelection() );
    wlFilenameList.setEnabled( !wSourceStreamField.getSelection() );
    wbdFilename.setEnabled( !wSourceStreamField.getSelection() );
    wbeFilename.setEnabled( !wSourceStreamField.getSelection() );
    wbShowFiles.setEnabled( !wSourceStreamField.getSelection() );
    wlFilenameList.setEnabled( !wSourceStreamField.getSelection() );

    wFilenameList.setEnabled( !wSourceStreamField.getSelection() );
    setCompositeEnabled( wFilenameList, !wSourceStreamField.getSelection() );

    wInclFilename.setEnabled( !wSourceStreamField.getSelection() );
    wlInclFilename.setEnabled( !wSourceStreamField.getSelection() );

    if ( wSourceStreamField.getSelection() ) {
      wInclFilename.setSelection( false );
      wlInclFilenameField.setEnabled( false );
      wInclFilenameField.setEnabled( false );
    } else {
      wlInclFilenameField.setEnabled( wInclFilename.getSelection() );
      wInclFilenameField.setEnabled( wInclFilename.getSelection() );
    }

    wAddResult.setEnabled( !wSourceStreamField.getSelection() );
    wlAddResult.setEnabled( !wSourceStreamField.getSelection() );
    wLimit.setEnabled( !wSourceStreamField.getSelection() );
    wlLimit.setEnabled( !wSourceStreamField.getSelection() );
    wPreview.setEnabled( !wSourceStreamField.getSelection() );
  }

  private void setCompositeEnabled( Composite comp, boolean enabled ) {
    comp.setEnabled( enabled );
    for ( Control child : comp.getChildren() ) {
      child.setEnabled( enabled );
    }
  }

  public void setIncludeFilename() {
    wlInclFilenameField.setEnabled( wInclFilename.getSelection() );
    wInclFilenameField.setEnabled( wInclFilename.getSelection() );
  }

  public void setIncludeRownum() {
    wlInclRownumField.setEnabled( wInclRownum.getSelection() );
    wInclRownumField.setEnabled( wInclRownum.getSelection() );
  }

  /**
   * Read the data from the TextFileInputMeta object and show it in this dialog.
   *
   * @param in
   *          The TextFileInputMeta object to obtain the data from.
   */
  public void getData( JsonInputMeta in ) {
    if ( in.getFileName() != null ) {
      wFilenameList.removeAll();

      for ( int i = 0; i < in.getFileName().length; i++ ) {
        String[] items = new String[] {
          in.getFileName()[i], in.getFileMask()[i], in.getExcludeFileMask()[i],
          in.getRequiredFilesDesc( in.getFileRequired()[i] ),
          in.getRequiredFilesDesc( in.getIncludeSubFolders()[i] ) };
        wFilenameList.add( items );
      }

      wFilenameList.removeEmptyRows();
      wFilenameList.setRowNums();
      wFilenameList.optWidth( true );
    }
    wInclFilename.setSelection( in.includeFilename() );
    wInclRownum.setSelection( in.includeRowNumber() );
    wAddResult.setSelection( in.addResultFile() );
    wreadUrl.setSelection( in.isReadUrl() );
    wIgnoreEmptyFile.setSelection( in.isIgnoreEmptyFile() );
    wdoNotFailIfNoFile.setSelection( in.isDoNotFailIfNoFile() );
    wIgnoreMissingPath.setSelection( in.isIgnoreMissingPath() );
    wDefaultPathLeafToNull.setSelection( in.isDefaultPathLeafToNull() );
    wIncludeNulls.setSelection( in.isIncludeNulls() || JsonInputMeta.getIncludeNullsProperty() );
    wremoveSourceField.setSelection( in.isRemoveSourceField() );
    wSourceStreamField.setSelection( in.isInFields() );
    wSourceIsAFile.setSelection( in.getIsAFile() );

    if ( in.getFieldValue() != null ) {
      wFieldValue.setText( in.getFieldValue() );
    }

    if ( in.getFilenameField() != null ) {
      wInclFilenameField.setText( in.getFilenameField() );
    }
    if ( in.getRowNumberField() != null ) {
      wInclRownumField.setText( in.getRowNumberField() );
    }
    wLimit.setText( "" + in.getRowLimit() );

    if ( isDebug() ) {
      logDebug( BaseMessages.getString( PKG, "JsonInputDialog.Log.GettingFieldsInfo" ) );
    }
    for ( int i = 0; i < in.getInputFields().length; i++ ) {
      JsonInputField field = in.getInputFields()[i];

      if ( field != null ) {
        TableItem item = wFields.table.getItem( i );
        String name = field.getName();
        String xpath = field.getPath();
        String type = field.getTypeDesc();
        String format = field.getFormat();
        String length = "" + field.getLength();
        String prec = "" + field.getPrecision();
        String curr = field.getCurrencySymbol();
        String group = field.getGroupSymbol();
        String decim = field.getDecimalSymbol();
        String trim = field.getTrimTypeDesc();
        String rep =
          field.isRepeated() ? YES : NO;

        if ( name != null ) {
          item.setText( 1, name );
        }
        if ( xpath != null ) {
          item.setText( 2, xpath );
        }
        if ( type != null ) {
          item.setText( 3, type );
        }
        if ( format != null ) {
          item.setText( 4, format );
        }
        if ( length != null && !"-1".equals( length ) ) {
          item.setText( 5, length );
        }
        if ( prec != null && !"-1".equals( prec ) ) {
          item.setText( 6, prec );
        }
        if ( curr != null ) {
          item.setText( 7, curr );
        }
        if ( decim != null ) {
          item.setText( 8, decim );
        }
        if ( group != null ) {
          item.setText( 9, group );
        }
        if ( trim != null ) {
          item.setText( 10, trim );
        }
        if ( rep != null ) {
          item.setText( 11, rep );
        }

      }
    }
    setSourceStreamField();

    wFields.removeEmptyRows();
    wFields.setRowNums();
    wFields.optWidth( true );

    if ( in.getShortFileNameField() != null ) {
      wShortFileFieldName.setText( in.getShortFileNameField() );
    }
    if ( in.getPathField() != null ) {
      wPathFieldName.setText( in.getPathField() );
    }
    if ( in.isHiddenField() != null ) {
      wIsHiddenName.setText( in.isHiddenField() );
    }
    if ( in.getLastModificationDateField() != null ) {
      wLastModificationTimeName.setText( in.getLastModificationDateField() );
    }
    if ( in.getUriField() != null ) {
      wUriName.setText( in.getUriField() );
    }
    if ( in.getRootUriField() != null ) {
      wRootUriName.setText( in.getRootUriField() );
    }
    if ( in.getExtensionField() != null ) {
      wExtensionFieldName.setText( in.getExtensionField() );
    }
    if ( in.getSizeField() != null ) {
      wSizeFieldName.setText( in.getSizeField() );
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
    try {
      getInfo( input );
    } catch ( KettleException e ) {
      new ErrorDialog( shell, BaseMessages.getString( PKG, "JsonInputDialog.ErrorParsingData.DialogTitle" ),
          BaseMessages.getString( PKG, "JsonInputDialog.ErrorParsingData.DialogMessage" ), e );
    }
    dispose();
  }

  /**
   * dialog -&gt; meta
   */
  private void getInfo( JsonInputMeta in ) throws KettleException {
    stepname = wStepname.getText(); // return value

    in.setRowLimit( Const.toLong( wLimit.getText(), 0L ) );
    in.setFilenameField( wInclFilenameField.getText() );
    in.setRowNumberField( wInclRownumField.getText() );
    in.setAddResultFile( wAddResult.getSelection() );
    in.setIncludeFilename( wInclFilename.getSelection() );
    in.setIncludeRowNumber( wInclRownum.getSelection() );
    in.setReadUrl( wreadUrl.getSelection() );
    in.setIgnoreEmptyFile( wIgnoreEmptyFile.getSelection() );
    in.setDoNotFailIfNoFile( wdoNotFailIfNoFile.getSelection() );
    in.setIgnoreMissingPath( wIgnoreMissingPath.getSelection() );
    in.setDefaultPathLeafToNull( wDefaultPathLeafToNull.getSelection() );
    in.setIncludeNulls( wIncludeNulls.getSelection() );
    in.setRemoveSourceField( wremoveSourceField.getSelection() );
    in.setInFields( wSourceStreamField.getSelection() );
    in.setIsAFile( wSourceIsAFile.getSelection() );
    in.setFieldValue( wFieldValue.getText() );

    int nrFiles = wFilenameList.getItemCount();
    int nrFields = wFields.nrNonEmpty();

    in.allocate( nrFiles, nrFields );
    in.setFileName( wFilenameList.getItems( 0 ) );
    in.setFileMask( wFilenameList.getItems( 1 ) );
    in.setExcludeFileMask( wFilenameList.getItems( 2 ) );
    in.setFileRequired( wFilenameList.getItems( 3 ) );
    in.setIncludeSubFolders( wFilenameList.getItems( 4 ) );

    for ( int i = 0; i < nrFields; i++ ) {
      JsonInputField field = new JsonInputField();

      TableItem item = wFields.getNonEmpty( i );

      field.setName( item.getText( 1 ) );
      field.setPath( item.getText( 2 ) );
      field.setType( ValueMetaFactory.getIdForValueMeta( item.getText( 3 ) ) );
      field.setFormat( item.getText( 4 ) );
      field.setLength( Const.toInt( item.getText( 5 ), -1 ) );
      field.setPrecision( Const.toInt( item.getText( 6 ), -1 ) );
      field.setCurrencySymbol( item.getText( 7 ) );
      field.setDecimalSymbol( item.getText( 8 ) );
      field.setGroupSymbol( item.getText( 9 ) );
      field.setTrimType( ValueMetaBase.getTrimTypeByDesc( item.getText( 10 ) ) );
      field.setRepeated( YES.equalsIgnoreCase( item.getText( 11 ) ) );

      in.getInputFields()[i] = field;
    }
    in.setShortFileNameField( wShortFileFieldName.getText() );
    in.setPathField( wPathFieldName.getText() );
    in.setIsHiddenField( wIsHiddenName.getText() );
    in.setLastModificationDateField( wLastModificationTimeName.getText() );
    in.setUriField( wUriName.getText() );
    in.setRootUriField( wRootUriName.getText() );
    in.setExtensionField( wExtensionFieldName.getText() );
    in.setSizeField( wSizeFieldName.getText() );
  }

  private void get() {
    try {
      // Call new get fields dialog
      if ( wFilenameList.getItemCount() > 0 ) {
        String filename = transMeta.environmentSubstitute( wFilenameList.getItem( 0 )[ 0 ] );
        List<String> paths = new ArrayList<>();
        for ( int i = 0; i < wFields.table.getItems().length; i++ ) {
          TableItem item = wFields.table.getItem( i );
          String value = item.getText( 2 );
          if ( !value.trim().isEmpty() ) {
            paths.add( value );
          }
        }

        GetFieldsDialog getFieldsDialog = new GetFieldsDialog( shell, input );
        getFieldsDialog.open( filename, paths, wFields );

      }
    } catch ( Exception e ) {
      log.logError( e.getMessage() );
    }
  }

  // Preview the data
  private void preview() {
    try {
      JsonInputMeta oneMeta = new JsonInputMeta();
      getInfo( oneMeta );

      TransMeta previewMeta =
          TransPreviewFactory.generatePreviewTransformation( transMeta, oneMeta, wStepname.getText() );

      EnterNumberDialog numberDialog =
          new EnterNumberDialog( shell, props.getDefaultPreviewSize(), BaseMessages.getString( PKG,
              "JsonInputDialog.NumberRows.DialogTitle" ), BaseMessages.getString( PKG,
              "JsonInputDialog.NumberRows.DialogMessage" ) );

      int previewSize = numberDialog.open();
      if ( previewSize > 0 ) {
        TransPreviewProgressDialog progressDialog =
            new TransPreviewProgressDialog( shell, previewMeta, new String[] { wStepname.getText() },
                new int[] { previewSize } );
        progressDialog.open();

        if ( !progressDialog.isCancelled() ) {
          Trans trans = progressDialog.getTrans();
          String loggingText = progressDialog.getLoggingText();

          if ( trans.getResult() != null && trans.getResult().getNrErrors() > 0 ) {
            EnterTextDialog etd =
                new EnterTextDialog( shell, BaseMessages.getString( PKG, "System.Dialog.PreviewError.Title" ),
                    BaseMessages.getString( PKG, "System.Dialog.PreviewError.Message" ), loggingText, true );
            etd.setReadOnly();
            etd.open();
          }
          PreviewRowsDialog prd =
              new PreviewRowsDialog( shell, transMeta, SWT.NONE, wStepname.getText(), progressDialog
                  .getPreviewRowsMeta( wStepname.getText() ), progressDialog.getPreviewRows( wStepname.getText() ),
                  loggingText );
          prd.open();
        }
      }
    } catch ( KettleException e ) {
      new ErrorDialog( shell, BaseMessages.getString( PKG, "JsonInputDialog.ErrorPreviewingData.DialogTitle" ),
          BaseMessages.getString( PKG, "JsonInputDialog.ErrorPreviewingData.DialogMessage" ), e );
    }
  }

  private void addAdditionalFieldsTab() {
    // ////////////////////////
    // START OF ADDITIONAL FIELDS TAB ///
    // ////////////////////////
    CTabItem wAdditionalFieldsTab = new CTabItem( wTabFolder, SWT.NONE );
    wAdditionalFieldsTab.setText( BaseMessages.getString( PKG, "JsonInputDialog.AdditionalFieldsTab.TabTitle" ) );

    Composite wAdditionalFieldsComp = new Composite( wTabFolder, SWT.NONE );
    props.setLook( wAdditionalFieldsComp );

    FormLayout fieldsLayout = new FormLayout();
    fieldsLayout.marginWidth = 3;
    fieldsLayout.marginHeight = 3;
    wAdditionalFieldsComp.setLayout( fieldsLayout );
    // ShortFileFieldName line

    Label wlShortFileFieldName = new Label( wAdditionalFieldsComp, SWT.RIGHT );
    wlShortFileFieldName.setText( BaseMessages.getString( PKG, "JsonInputDialog.ShortFileFieldName.Label" ) );
    props.setLook( wlShortFileFieldName );

    FormData fdlShortFileFieldName = new FormData();
    fdlShortFileFieldName.left = new FormAttachment( 0, 0 );
    fdlShortFileFieldName.top = new FormAttachment( wInclRownumField, margin );
    fdlShortFileFieldName.right = new FormAttachment( middle, -margin );
    wlShortFileFieldName.setLayoutData( fdlShortFileFieldName );

    wShortFileFieldName = new TextVar( transMeta, wAdditionalFieldsComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wShortFileFieldName );
    wShortFileFieldName.addModifyListener( lsMod );

    FormData fdShortFileFieldName = new FormData();
    fdShortFileFieldName.left = new FormAttachment( middle, 0 );
    fdShortFileFieldName.right = new FormAttachment( 100, -margin );
    fdShortFileFieldName.top = new FormAttachment( wInclRownumField, margin );
    wShortFileFieldName.setLayoutData( fdShortFileFieldName );

    // ExtensionFieldName line
    Label wlExtensionFieldName = new Label( wAdditionalFieldsComp, SWT.RIGHT );
    wlExtensionFieldName.setText( BaseMessages.getString( PKG, "JsonInputDialog.ExtensionFieldName.Label" ) );
    props.setLook( wlExtensionFieldName );

    FormData fdlExtensionFieldName = new FormData();
    fdlExtensionFieldName.left = new FormAttachment( 0, 0 );
    fdlExtensionFieldName.top = new FormAttachment( wShortFileFieldName, margin );
    fdlExtensionFieldName.right = new FormAttachment( middle, -margin );
    wlExtensionFieldName.setLayoutData( fdlExtensionFieldName );

    wExtensionFieldName = new TextVar( transMeta, wAdditionalFieldsComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wExtensionFieldName );
    wExtensionFieldName.addModifyListener( lsMod );

    FormData fdExtensionFieldName = new FormData();
    fdExtensionFieldName.left = new FormAttachment( middle, 0 );
    fdExtensionFieldName.right = new FormAttachment( 100, -margin );
    fdExtensionFieldName.top = new FormAttachment( wShortFileFieldName, margin );
    wExtensionFieldName.setLayoutData( fdExtensionFieldName );

    // PathFieldName line
    Label wlPathFieldName = new Label( wAdditionalFieldsComp, SWT.RIGHT );
    wlPathFieldName.setText( BaseMessages.getString( PKG, "JsonInputDialog.PathFieldName.Label" ) );
    props.setLook( wlPathFieldName );

    FormData fdlPathFieldName = new FormData();
    fdlPathFieldName.left = new FormAttachment( 0, 0 );
    fdlPathFieldName.top = new FormAttachment( wExtensionFieldName, margin );
    fdlPathFieldName.right = new FormAttachment( middle, -margin );
    wlPathFieldName.setLayoutData( fdlPathFieldName );

    wPathFieldName = new TextVar( transMeta, wAdditionalFieldsComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wPathFieldName );
    wPathFieldName.addModifyListener( lsMod );

    FormData fdPathFieldName = new FormData();
    fdPathFieldName.left = new FormAttachment( middle, 0 );
    fdPathFieldName.right = new FormAttachment( 100, -margin );
    fdPathFieldName.top = new FormAttachment( wExtensionFieldName, margin );
    wPathFieldName.setLayoutData( fdPathFieldName );

    // SizeFieldName line
    Label wlSizeFieldName = new Label( wAdditionalFieldsComp, SWT.RIGHT );
    wlSizeFieldName.setText( BaseMessages.getString( PKG, "JsonInputDialog.SizeFieldName.Label" ) );
    props.setLook( wlSizeFieldName );

    FormData fdlSizeFieldName = new FormData();
    fdlSizeFieldName.left = new FormAttachment( 0, 0 );
    fdlSizeFieldName.top = new FormAttachment( wPathFieldName, margin );
    fdlSizeFieldName.right = new FormAttachment( middle, -margin );
    wlSizeFieldName.setLayoutData( fdlSizeFieldName );

    wSizeFieldName = new TextVar( transMeta, wAdditionalFieldsComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wSizeFieldName );
    wSizeFieldName.addModifyListener( lsMod );

    FormData fdSizeFieldName = new FormData();
    fdSizeFieldName.left = new FormAttachment( middle, 0 );
    fdSizeFieldName.right = new FormAttachment( 100, -margin );
    fdSizeFieldName.top = new FormAttachment( wPathFieldName, margin );
    wSizeFieldName.setLayoutData( fdSizeFieldName );

    // IsHiddenName line
    Label wlIsHiddenName = new Label( wAdditionalFieldsComp, SWT.RIGHT );
    wlIsHiddenName.setText( BaseMessages.getString( PKG, "JsonInputDialog.IsHiddenName.Label" ) );
    props.setLook( wlIsHiddenName );

    FormData fdlIsHiddenName = new FormData();
    fdlIsHiddenName.left = new FormAttachment( 0, 0 );
    fdlIsHiddenName.top = new FormAttachment( wSizeFieldName, margin );
    fdlIsHiddenName.right = new FormAttachment( middle, -margin );
    wlIsHiddenName.setLayoutData( fdlIsHiddenName );

    wIsHiddenName = new TextVar( transMeta, wAdditionalFieldsComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wIsHiddenName );
    wIsHiddenName.addModifyListener( lsMod );

    FormData fdIsHiddenName = new FormData();
    fdIsHiddenName.left = new FormAttachment( middle, 0 );
    fdIsHiddenName.right = new FormAttachment( 100, -margin );
    fdIsHiddenName.top = new FormAttachment( wSizeFieldName, margin );
    wIsHiddenName.setLayoutData( fdIsHiddenName );

    // LastModificationTimeName line
    Label wlLastModificationTimeName = new Label( wAdditionalFieldsComp, SWT.RIGHT );
    wlLastModificationTimeName.setText( BaseMessages.getString(
      PKG, "JsonInputDialog.LastModificationTimeName.Label" ) );
    props.setLook( wlLastModificationTimeName );

    FormData fdlLastModificationTimeName = new FormData();
    fdlLastModificationTimeName.left = new FormAttachment( 0, 0 );
    fdlLastModificationTimeName.top = new FormAttachment( wIsHiddenName, margin );
    fdlLastModificationTimeName.right = new FormAttachment( middle, -margin );
    wlLastModificationTimeName.setLayoutData( fdlLastModificationTimeName );

    wLastModificationTimeName = new TextVar( transMeta, wAdditionalFieldsComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wLastModificationTimeName );
    wLastModificationTimeName.addModifyListener( lsMod );

    FormData fdLastModificationTimeName = new FormData();
    fdLastModificationTimeName.left = new FormAttachment( middle, 0 );
    fdLastModificationTimeName.right = new FormAttachment( 100, -margin );
    fdLastModificationTimeName.top = new FormAttachment( wIsHiddenName, margin );
    wLastModificationTimeName.setLayoutData( fdLastModificationTimeName );

    // UriName line
    Label wlUriName = new Label( wAdditionalFieldsComp, SWT.RIGHT );
    wlUriName.setText( BaseMessages.getString( PKG, "JsonInputDialog.UriName.Label" ) );
    props.setLook( wlUriName );

    FormData fdlUriName = new FormData();
    fdlUriName.left = new FormAttachment( 0, 0 );
    fdlUriName.top = new FormAttachment( wLastModificationTimeName, margin );
    fdlUriName.right = new FormAttachment( middle, -margin );
    wlUriName.setLayoutData( fdlUriName );

    wUriName = new TextVar( transMeta, wAdditionalFieldsComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wUriName );
    wUriName.addModifyListener( lsMod );

    FormData fdUriName = new FormData();
    fdUriName.left = new FormAttachment( middle, 0 );
    fdUriName.right = new FormAttachment( 100, -margin );
    fdUriName.top = new FormAttachment( wLastModificationTimeName, margin );
    wUriName.setLayoutData( fdUriName );

    // RootUriName line
    Label wlRootUriName = new Label( wAdditionalFieldsComp, SWT.RIGHT );
    wlRootUriName.setText( BaseMessages.getString( PKG, "JsonInputDialog.RootUriName.Label" ) );
    props.setLook( wlRootUriName );

    FormData fdlRootUriName = new FormData();
    fdlRootUriName.left = new FormAttachment( 0, 0 );
    fdlRootUriName.top = new FormAttachment( wUriName, margin );
    fdlRootUriName.right = new FormAttachment( middle, -margin );
    wlRootUriName.setLayoutData( fdlRootUriName );

    wRootUriName = new TextVar( transMeta, wAdditionalFieldsComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wRootUriName );
    wRootUriName.addModifyListener( lsMod );

    FormData fdRootUriName = new FormData();
    fdRootUriName.left = new FormAttachment( middle, 0 );
    fdRootUriName.right = new FormAttachment( 100, -margin );
    fdRootUriName.top = new FormAttachment( wUriName, margin );
    wRootUriName.setLayoutData( fdRootUriName );

    FormData fdAdditionalFieldsComp = new FormData();
    fdAdditionalFieldsComp.left = new FormAttachment( 0, 0 );
    fdAdditionalFieldsComp.top = new FormAttachment( wStepname, margin );
    fdAdditionalFieldsComp.right = new FormAttachment( 100, 0 );
    fdAdditionalFieldsComp.bottom = new FormAttachment( 100, 0 );
    wAdditionalFieldsComp.setLayoutData( fdAdditionalFieldsComp );

    wAdditionalFieldsComp.layout();
    wAdditionalFieldsTab.setControl( wAdditionalFieldsComp );

    // ///////////////////////////////////////////////////////////
    // / END OF ADDITIONAL FIELDS TAB
    // ///////////////////////////////////////////////////////////

  }
}
