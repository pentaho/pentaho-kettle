/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2016 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.ui.trans.steps.monetdbbulkloader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.DBCache;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.SQLStatement;
import org.pentaho.di.core.SourceToTargetMapping;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseInterface;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.monetdbbulkloader.MonetDBBulkLoaderMeta;
import org.pentaho.di.ui.core.database.dialog.DatabaseDialog;
import org.pentaho.di.ui.core.database.dialog.DatabaseExplorerDialog;
import org.pentaho.di.ui.core.database.dialog.SQLEditor;
import org.pentaho.di.ui.core.dialog.EnterMappingDialog;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.ComboVar;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.ui.trans.step.TableItemInsertListener;

/**
 * Dialog class for the MonetDB bulk loader step.
 */
public class MonetDBBulkLoaderDialog extends BaseStepDialog implements StepDialogInterface {
  private static Class<?> PKG = MonetDBBulkLoaderMeta.class; // for i18n purposes, needed by Translator2!!

  private CTabFolder wTabFolder;
  private CTabItem wGeneralSettingsTab, wMonetDBmclientSettingsTab, wOutputFieldsTab;
  private Composite wGeneralSettingsComp, wMonetDBmclientSettingsComp, wOutputFieldsComp;
  private Group wMonetDBmclientParamGroup;
  private FormData fdgMonetDBmclientParamGroup;

  //
  // General Settings tab - Widgets and FormData
  private ComboVar wConnection;

  private Label wlSchema;
  private TextVar wSchema;
  private FormData fdlSchema, fdSchema;

  private Label wlTable;
  private Button wbTable;
  private TextVar wTable;
  private FormData fdlTable, fdbTable, fdTable;

  private Label wlBufferSize;
  private TextVar wBufferSize;
  private FormData fdlBufferSize, fdBufferSize;

  private Label wlLogFile;
  private Button wbLogFile;
  private TextVar wLogFile;
  private FormData fdlLogFile, fdbLogFile, fdLogFile;

  private Label wlTruncate;
  private Button wTruncate;
  private FormData fdlTruncate, fdTruncate;

  private Label wlFullyQuoteSQL;
  private Button wFullyQuoteSQL;
  private FormData fdlFullyQuoteSQL, fdFullyQuoteSQL;

  //
  // MonetDB API Settings tab - Widgets and FormData
  private Label wlFieldSeparator;
  private Combo wFieldSeparator;
  private FormData fdlFieldSeparator, fdFieldSeparator;

  private Label wlFieldEnclosure;
  private Combo wFieldEnclosure;
  private FormData fdlFieldEnclosure, fdFieldEnclosure;

  private Label wlNULLrepresentation;
  private Combo wNULLrepresentation;
  private FormData fdlNULLrepresentation, fdNULLrepresentation;

  private Label wlEncoding;
  private Combo wEncoding;
  private FormData fdlEncoding, fdEncoding;

  //
  // Output Fields tab - Widgets and FormData
  private Label wlReturn;
  private TableView wReturn;
  private FormData fdlReturn, fdReturn;

  private Button wGetLU;
  private FormData fdGetLU;
  private Listener lsGetLU;

  private Button wClearDBCache;
  private FormData fdClearDBCache;
  private Listener lsClearDBCache;

  private Button wDoMapping;
  private FormData fdDoMapping;

  private MonetDBBulkLoaderMeta input;

  private ColumnInfo[] ciReturn;

  private Map<String, Integer> inputFields;

  /**
   * List of ColumnInfo that should have the field names of the selected database table
   */
  private List<ColumnInfo> tableFieldColumns = new ArrayList<ColumnInfo>();

  // Commonly used field delimiters (separators)
  private static String[] fieldSeparators = { "", "|", "," };
  // Commonly used enclosure characters
  private static String[] fieldEnclosures = { "", "\"" };

  // In MonetDB when streaming fields over, you can specify how to alert the database of a truly empty field i.e. NULL
  // The user can put anything they want or leave it blank.
  private static String[] nullRepresentations = { "", "null" };

  // These should not be translated, they are required to exist on all
  // platforms according to the documentation of "Charset".
  private static String[] encodings = { "", "US-ASCII", "ISO-8859-1", "UTF-8", "UTF-16BE", "UTF-16LE", "UTF-16" };

  private static final String[] ALL_FILETYPES = new String[] { BaseMessages.getString(
    PKG, "MonetDBBulkLoaderDialog.Filetype.All" ) };

  public MonetDBBulkLoaderDialog( Shell parent, Object in, TransMeta transMeta, String sname ) {
    super( parent, (BaseStepMeta) in, transMeta, sname );
    input = (MonetDBBulkLoaderMeta) in;
    inputFields = new HashMap<String, Integer>();
  }

  public String open() {
    Shell parent = getParent();
    Display display = parent.getDisplay();

    shell = new Shell( parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN );
    props.setLook( shell );
    setShellImage( shell, input );

    ModifyListener lsMod = new ModifyListener() {
      public void modifyText( ModifyEvent e ) {
        input.setChanged();
      }
    };
    FocusListener lsFocusLost = new FocusAdapter() {
      public void focusLost( FocusEvent arg0 ) {
        setTableFieldCombo();
      }
    };
    changed = input.hasChanged();

    FormLayout formLayout = new FormLayout();
    formLayout.marginWidth = Const.FORM_MARGIN;
    formLayout.marginHeight = Const.FORM_MARGIN;

    shell.setLayout( formLayout );
    shell.setText( BaseMessages.getString( PKG, "MonetDBBulkLoaderDialog.Shell.Title" ) );

    // The right side of all the labels is available as a user-defined percentage: props.getMiddlePct()
    // Page 610 - Pentaho Kettle Solutions
    int middle = props.getMiddlePct();
    int margin = Const.MARGIN; // Default 4 pixel margin around components.

    //
    // Dialog Box Contents (Organized from dialog top to bottom, dialog left to right.)
    // Label - Step name
    wlStepname = new Label( shell, SWT.LEFT );
    wlStepname.setText( BaseMessages.getString( PKG, "MonetDBBulkLoaderDialog.Stepname.Label" ) );
    props.setLook( wlStepname ); // Puts the user-selected background color and font on the widget.

    // Text box for editing the step name
    wStepname = new Text( shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    wStepname.setText( stepname );
    props.setLook( wStepname );
    wStepname.addModifyListener( lsMod );

    // //////////////////////////////////////////////
    // Prepare the Folder that will contain tabs. //
    // //////////////////////////////////////////////
    wTabFolder = new CTabFolder( shell, SWT.BORDER );
    props.setLook( wTabFolder, Props.WIDGET_STYLE_TAB );

    // ////////////////////////
    // General Settings tab //
    // ////////////////////////

    wGeneralSettingsTab = new CTabItem( wTabFolder, SWT.NONE );
    wGeneralSettingsTab
      .setText( BaseMessages.getString( PKG, "MonetDBBulkLoaderDialog.Tab.GeneralSettings.Label" ) );

    wGeneralSettingsComp = new Composite( wTabFolder, SWT.NONE );
    props.setLook( wGeneralSettingsComp );

    FormLayout tabLayout = new FormLayout();
    tabLayout.marginWidth = 3;
    tabLayout.marginHeight = 3;
    wGeneralSettingsComp.setLayout( tabLayout );

    wGeneralSettingsComp.layout();
    wGeneralSettingsTab.setControl( wGeneralSettingsComp );

    // ////////////////////////////////
    // MonetDB Settings tab //
    // ////////////////////////////////

    wMonetDBmclientSettingsTab = new CTabItem( wTabFolder, SWT.NONE );
    wMonetDBmclientSettingsTab.setText( BaseMessages.getString(
      PKG, "MonetDBBulkLoaderDialog.Tab.MonetDBmclientSettings.Label" ) );

    wMonetDBmclientSettingsComp = new Composite( wTabFolder, SWT.NONE );
    props.setLook( wMonetDBmclientSettingsComp );
    wMonetDBmclientSettingsComp.setLayout( tabLayout );
    wMonetDBmclientSettingsComp.layout();
    wMonetDBmclientSettingsTab.setControl( wMonetDBmclientSettingsComp );

    wMonetDBmclientParamGroup = new Group( wMonetDBmclientSettingsComp, SWT.SHADOW_IN );
    wMonetDBmclientParamGroup.setText( BaseMessages.getString(
      PKG, "MonetDBBulkLoaderDialog.Tab.MonetDBmclientSettings.ParameterGroup" ) );
    props.setLook( wMonetDBmclientParamGroup );
    wMonetDBmclientParamGroup.setLayout( tabLayout );
    wMonetDBmclientParamGroup.layout();

    // /////////////////////
    // Output Fields tab //
    // /////////////////////

    wOutputFieldsTab = new CTabItem( wTabFolder, SWT.NONE );
    wOutputFieldsTab.setText( BaseMessages.getString( PKG, "MonetDBBulkLoaderDialog.Tab.OutputFields" ) );

    wOutputFieldsComp = new Composite( wTabFolder, SWT.NONE );
    props.setLook( wOutputFieldsComp );

    wOutputFieldsComp.setLayout( tabLayout );

    wOutputFieldsComp.layout();
    wOutputFieldsTab.setControl( wOutputFieldsComp );

    // Activate the "General Settings" tab
    wTabFolder.setSelection( 0 );

    wTabFolder.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent arg0 ) {
        wTabFolder.layout( true, true );
      }
    } );

    //
    // Connection line (General Settings tab)
    //
    wConnection = addConnectionLine( wGeneralSettingsComp, wTabFolder, middle, margin, null, transMeta );
    if ( input.getDatabaseMeta() == null && transMeta.nrDatabases() == 1 ) {
      wConnection.select( 0 );
    }
    wConnection.addModifyListener( lsMod );

    //
    // Schema line (General Settings tab)
    //
    wlSchema = new Label( wGeneralSettingsComp, SWT.LEFT );
    wlSchema.setText( BaseMessages.getString( PKG, "MonetDBBulkLoaderDialog.TargetSchema.Label" ) );
    props.setLook( wlSchema );
    wSchema = new TextVar( transMeta, wGeneralSettingsComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wSchema );
    wSchema.addModifyListener( lsMod );
    wSchema.addFocusListener( lsFocusLost );

    //
    // Table line (General Settings tab)
    //
    wlTable = new Label( wGeneralSettingsComp, SWT.LEFT );
    wlTable.setText( BaseMessages.getString( PKG, "MonetDBBulkLoaderDialog.TargetTable.Label" ) );
    props.setLook( wlTable );

    wbTable = new Button( wGeneralSettingsComp, SWT.PUSH | SWT.CENTER );
    props.setLook( wbTable );
    wbTable.setText( BaseMessages.getString( PKG, "MonetDBBulkLoaderDialog.Browse.Button" ) );

    wTable = new TextVar( transMeta, wGeneralSettingsComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wTable );
    wTable.addModifyListener( lsMod );
    wTable.addFocusListener( lsFocusLost );

    //
    // Buffer size line (General Settings tab)
    //
    wlBufferSize = new Label( wGeneralSettingsComp, SWT.RIGHT );
    wlBufferSize.setText( BaseMessages.getString( PKG, "MonetDBBulkLoaderDialog.BufferSize.Label" ) );
    props.setLook( wlBufferSize );

    wBufferSize = new TextVar( transMeta, wGeneralSettingsComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wBufferSize );
    wBufferSize.addModifyListener( lsMod );

    //
    // Log file line (General Settings tab)
    //
    wlLogFile = new Label( wGeneralSettingsComp, SWT.LEFT );
    wlLogFile.setText( BaseMessages.getString( PKG, "MonetDBBulkLoaderDialog.LogFile.Label" ) );
    props.setLook( wlLogFile );

    wbLogFile = new Button( wGeneralSettingsComp, SWT.PUSH | SWT.CENTER );
    props.setLook( wbLogFile );
    wbLogFile.setText( BaseMessages.getString( PKG, "MonetDBBulkLoaderDialog.Browse.Button" ) );

    wLogFile = new TextVar( transMeta, wGeneralSettingsComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wLogFile );
    wLogFile.addModifyListener( lsMod );

    //
    // Truncate before loading check box (General Settings tab)
    //
    wlTruncate = new Label( wGeneralSettingsComp, SWT.LEFT );
    wlTruncate.setText( BaseMessages.getString( PKG, "MonetDBBulkLoaderDialog.Truncate.Label" ) );
    props.setLook( wlTruncate );
    wTruncate = new Button( wGeneralSettingsComp, SWT.CHECK );
    props.setLook( wTruncate );
    SelectionAdapter lsSelMod = new SelectionAdapter() {
      public void widgetSelected( SelectionEvent arg0 ) {
        input.setChanged();
        input.setTruncate( wTruncate.getSelection() );
      }
    };
    wTruncate.addSelectionListener( lsSelMod );

    //
    // Fully Quote SQL during the run. (This setting will persist into the database connection definition.)
    //
    wlFullyQuoteSQL = new Label( wGeneralSettingsComp, SWT.LEFT );
    wlFullyQuoteSQL.setText( BaseMessages.getString( PKG, "MonetDBBulkLoaderDialog.FullyQuoteSQL.Label" ) );
    props.setLook( wlFullyQuoteSQL );

    wFullyQuoteSQL = new Button( wGeneralSettingsComp, SWT.CHECK );
    props.setLook( wFullyQuoteSQL );
    SelectionAdapter lsFullyQuoteSQL = new SelectionAdapter() {
      public void widgetSelected( SelectionEvent arg0 ) {
        input.setChanged();
        input.getDatabaseMeta().setQuoteAllFields( wFullyQuoteSQL.getSelection() );
      }
    };
    wFullyQuoteSQL.addSelectionListener( lsFullyQuoteSQL );

    // /////////////////////////////////////////////////////////
    // MonetDB API Settings tab widget declarations follow
    // /////////////////////////////////////////////////////////

    // (Sub-group within the "MonetDB mclient Settings" tab)
    // Widgets for setting the parameters that are sent to the mclient software when the step executes
    wlFieldSeparator = new Label( wMonetDBmclientParamGroup, SWT.LEFT );
    wlFieldSeparator.setText( BaseMessages.getString(
      PKG, "MonetDBBulkLoaderDialog.Tab.MonetDBmclientSettings.ParameterGroup.FieldSeparator.Label" ) );
    props.setLook( wlFieldSeparator );

    wFieldSeparator = new Combo( wMonetDBmclientParamGroup, SWT.SINGLE | SWT.CENTER | SWT.BORDER );
    wFieldSeparator.setItems( fieldSeparators );
    props.setLook( wFieldSeparator );
    wFieldSeparator.addModifyListener( lsMod );

    wlFieldEnclosure = new Label( wMonetDBmclientParamGroup, SWT.LEFT );
    wlFieldEnclosure.setText( BaseMessages.getString(
      PKG, "MonetDBBulkLoaderDialog.Tab.MonetDBmclientSettings.ParameterGroup.FieldEnclosure.Label" ) );
    props.setLook( wlFieldEnclosure );

    wFieldEnclosure = new Combo( wMonetDBmclientParamGroup, SWT.SINGLE | SWT.CENTER | SWT.BORDER );
    wFieldEnclosure.setItems( fieldEnclosures );
    wFieldEnclosure.addModifyListener( lsMod );

    wlNULLrepresentation = new Label( wMonetDBmclientParamGroup, SWT.LEFT );
    wlNULLrepresentation.setText( BaseMessages.getString(
      PKG, "MonetDBBulkLoaderDialog.Tab.MonetDBmclientSettings.ParameterGroup.NULLrepresentation.Label" ) );
    props.setLook( wlNULLrepresentation );

    wNULLrepresentation = new Combo( wMonetDBmclientParamGroup, SWT.SINGLE | SWT.CENTER | SWT.BORDER );
    wNULLrepresentation.setItems( nullRepresentations );
    wNULLrepresentation.addModifyListener( lsMod );

    //
    // Control encoding line (MonetDB API Settings tab -> Parameter Group)
    //
    // The drop down is editable as it may happen an encoding may not be present
    // on one machine, but you may want to use it on your execution server
    //
    wlEncoding = new Label( wMonetDBmclientParamGroup, SWT.RIGHT );
    wlEncoding.setText( BaseMessages.getString( PKG, "MonetDBBulkLoaderDialog.Encoding.Label" ) );
    props.setLook( wlEncoding );

    wEncoding = new Combo( wMonetDBmclientParamGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    wEncoding.setToolTipText( BaseMessages.getString( PKG, "MonetDBBulkLoaderDialog.Encoding.Tooltip" ) );
    wEncoding.setItems( encodings );
    props.setLook( wEncoding );
    wEncoding.addModifyListener( lsMod );

    //
    // OK (Button), Cancel (Button) and SQL (Button)
    // - these appear at the bottom of the dialog window.
    wOK = new Button( shell, SWT.PUSH );
    wOK.setText( BaseMessages.getString( PKG, "System.Button.OK" ) );
    wSQL = new Button( shell, SWT.PUSH );
    wSQL.setText( BaseMessages.getString( PKG, "MonetDBBulkLoaderDialog.SQL.Button" ) );
    wCancel = new Button( shell, SWT.PUSH );
    wCancel.setText( BaseMessages.getString( PKG, "System.Button.Cancel" ) );

    setButtonPositions( new Button[] { wOK, wCancel, wSQL }, margin, null );

    // The field Table
    wlReturn = new Label( wOutputFieldsComp, SWT.NONE );
    wlReturn.setText( BaseMessages.getString( PKG, "MonetDBBulkLoaderDialog.Fields.Label" ) );
    props.setLook( wlReturn );

    int UpInsCols = 3;
    int UpInsRows = ( input.getFieldTable() != null ? input.getFieldTable().length : 1 );

    ciReturn = new ColumnInfo[UpInsCols];
    ciReturn[0] =
      new ColumnInfo(
        BaseMessages.getString( PKG, "MonetDBBulkLoaderDialog.ColumnInfo.TableField" ),
        ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] { "" }, false );
    ciReturn[1] =
      new ColumnInfo(
        BaseMessages.getString( PKG, "MonetDBBulkLoaderDialog.ColumnInfo.StreamField" ),
        ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] { "" }, false );
    ciReturn[2] =
      new ColumnInfo(
        BaseMessages.getString( PKG, "MonetDBBulkLoaderDialog.ColumnInfo.FormatOK" ),
        ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] { "Y", "N", }, true );
    tableFieldColumns.add( ciReturn[0] );
    wReturn =
      new TableView( transMeta, wOutputFieldsComp, SWT.BORDER
        | SWT.FULL_SELECTION | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL, ciReturn, UpInsRows, lsMod, props );
    wReturn.optWidth( true );

    // wReturn.table.pack(); // Force columns to take up the size they need. Make it easy for the user to see what
    // values are in the field.

    wGetLU = new Button( wOutputFieldsComp, SWT.PUSH );
    wGetLU.setText( BaseMessages.getString( PKG, "MonetDBBulkLoaderDialog.GetFields.Label" ) );

    wDoMapping = new Button( wOutputFieldsComp, SWT.PUSH );
    wDoMapping.setText( BaseMessages.getString( PKG, "MonetDBBulkLoaderDialog.EditMapping.Label" ) );

    wDoMapping.addListener( SWT.Selection, new Listener() {
      public void handleEvent( Event arg0 ) {
        generateMappings();
      }
    } );

    wClearDBCache = new Button( wOutputFieldsComp, SWT.PUSH );
    wClearDBCache.setText( BaseMessages.getString( PKG, "MonetDBBulkLoaderDialog.Tab.ClearDbCache" ) );

    lsClearDBCache = new Listener() {
      public void handleEvent( Event e ) {
        DBCache.getInstance().clear( input.getDbConnectionName() );
        MessageBox mb = new MessageBox( shell, SWT.OK | SWT.ICON_INFORMATION );
        mb.setMessage( BaseMessages.getString( PKG, "MonetDBBulkLoaderDialog.Tab.ClearedDbCacheMsg" ) );
        mb.setText( BaseMessages.getString( PKG, "MonetDBBulkLoaderDialog.Tab.ClearedDbCacheTitle" ) );
        mb.open();
      }
    };
    wClearDBCache.addListener( SWT.Selection, lsClearDBCache );

    //
    // Visual Layout Definition
    //
    // FormLayout (org.eclipse.swt.layout.FormLayout) is being used to compose the dialog box.
    // The layout works by creating FormAttachments for each side of the widget and storing them in the layout data.
    // An attachment 'attaches' a specific side of the widget either to a position in the parent Composite or to another
    // widget within the layout.

    //
    // Step name (Label and Edit Box)
    // - Location: top of the dialog box
    //
    fdlStepname = new FormData();
    fdlStepname.top = new FormAttachment( 0, 15 );
    fdlStepname.left = new FormAttachment( 0, margin );
    fdlStepname.right = new FormAttachment( 20, 0 );
    wlStepname.setLayoutData( fdlStepname );

    fdStepname = new FormData();
    fdStepname.top = new FormAttachment( 0, 15 );
    fdStepname.left = new FormAttachment( wlStepname, margin ); // FormAttachment(middle, 0);
    fdStepname.right = new FormAttachment( 100, -margin ); // 100% of the form component (length of edit box)
    wStepname.setLayoutData( fdStepname );

    //
    // Tabs will appear below the "Step Name" area and above the buttons at the bottom of the dialog.
    //
    FormData fdTabFolder = new FormData();
    fdTabFolder.left = new FormAttachment( 0, 0 );
    fdTabFolder.top = new FormAttachment( wStepname, margin + 20 );
    fdTabFolder.right = new FormAttachment( 100, 0 );
    fdTabFolder.bottom = new FormAttachment( 100, -50 );
    wTabFolder.setLayoutData( fdTabFolder );

    //
    // Positioning for the Database Connection line happened above in the function call
    // - wConnection = addConnectionLine(wGeneralSettingsComp, wTabFolder, middle, margin, null, transMeta);
    //

    // Database Schema Line - (General Settings Tab)
    //
    // Database Schema (Label layout)
    fdlSchema = new FormData();
    fdlSchema.top = new FormAttachment( wConnection, margin * 2 );
    fdlSchema.left = new FormAttachment( wGeneralSettingsComp, margin );
    wlSchema.setLayoutData( fdlSchema );

    // Database schema (Edit box layout)
    fdSchema = new FormData();
    fdSchema.top = new FormAttachment( wConnection, margin * 2 );
    fdSchema.left = new FormAttachment( middle, margin );
    fdSchema.right = new FormAttachment( 100, -margin );
    wSchema.setLayoutData( fdSchema );

    // Target Table Line - (General Settings Tab)
    //
    // Target table (Label layout)
    // - tied to the left wall of the general settings tab (composite)
    fdlTable = new FormData();
    fdlTable.top = new FormAttachment( wSchema, margin );
    fdlTable.left = new FormAttachment( 0, margin );
    wlTable.setLayoutData( fdlTable );

    // Target table browse (Button layout)
    // - tied to the right wall of the general settings tab (composite)
    fdbTable = new FormData();
    fdbTable.top = new FormAttachment( wSchema, 0 );
    fdbTable.right = new FormAttachment( 100, -margin );
    wbTable.setLayoutData( fdbTable );

    // Target table (Edit box layout)
    // Between the label and button.
    // - tied to the right edge of the general Browse tables button
    fdTable = new FormData();
    fdTable.top = new FormAttachment( wSchema, margin );
    fdTable.left = new FormAttachment( middle, margin );
    fdTable.right = new FormAttachment( wbTable, -margin );
    wTable.setLayoutData( fdTable );

    // Buffer size (Label layout)
    fdlBufferSize = new FormData();
    fdlBufferSize.top = new FormAttachment( wTable, margin );
    fdlBufferSize.left = new FormAttachment( 0, margin );
    wlBufferSize.setLayoutData( fdlBufferSize );

    fdBufferSize = new FormData();
    fdBufferSize.top = new FormAttachment( wTable, margin );
    fdBufferSize.left = new FormAttachment( middle, margin );
    fdBufferSize.right = new FormAttachment( 100, -margin );
    wBufferSize.setLayoutData( fdBufferSize );

    fdlLogFile = new FormData();
    fdlLogFile.top = new FormAttachment( wBufferSize, margin );
    fdlLogFile.left = new FormAttachment( 0, margin );
    wlLogFile.setLayoutData( fdlLogFile );

    // Log file Browse (button)
    fdbLogFile = new FormData();
    fdbLogFile.top = new FormAttachment( wBufferSize, 0 );
    fdbLogFile.right = new FormAttachment( 100, -margin );
    wbLogFile.setLayoutData( fdbLogFile );

    fdLogFile = new FormData();
    fdLogFile.left = new FormAttachment( middle, margin );
    fdLogFile.top = new FormAttachment( wBufferSize, margin );
    fdLogFile.right = new FormAttachment( wbLogFile, -margin );
    wLogFile.setLayoutData( fdLogFile );

    fdlTruncate = new FormData();
    fdlTruncate.top = new FormAttachment( wLogFile, margin * 2 );
    fdlTruncate.left = new FormAttachment( 0, margin );
    wlTruncate.setLayoutData( fdlTruncate );

    fdTruncate = new FormData();
    fdTruncate.top = new FormAttachment( wLogFile, margin * 2 );
    fdTruncate.left = new FormAttachment( wlTruncate, margin );
    fdTruncate.right = new FormAttachment( 100, -margin );
    wTruncate.setLayoutData( fdTruncate );

    fdlFullyQuoteSQL = new FormData();
    fdlFullyQuoteSQL.top = new FormAttachment( wlTruncate, margin * 2 );
    fdlFullyQuoteSQL.left = new FormAttachment( 0, margin );
    wlFullyQuoteSQL.setLayoutData( fdlFullyQuoteSQL );

    fdFullyQuoteSQL = new FormData();
    fdFullyQuoteSQL.top = new FormAttachment( wTruncate, margin * 2 );
    fdFullyQuoteSQL.left = new FormAttachment( wlFullyQuoteSQL, margin );
    fdFullyQuoteSQL.right = new FormAttachment( 100, -margin );
    wFullyQuoteSQL.setLayoutData( fdFullyQuoteSQL );
    //
    // MonetDB Settings tab layout
    //

    //
    // mclient parameter grouping (Group composite)
    // - Visually we make it clear what is being fed to mclient as parameters.
    fdgMonetDBmclientParamGroup = new FormData();
    fdgMonetDBmclientParamGroup.top = new FormAttachment( wMonetDBmclientSettingsComp, margin * 3 );
    fdgMonetDBmclientParamGroup.left = new FormAttachment( 0, margin );
    fdgMonetDBmclientParamGroup.right = new FormAttachment( 100, -margin );
    wMonetDBmclientParamGroup.setLayoutData( fdgMonetDBmclientParamGroup );

    // Figure out font width in pixels, then set the combo boxes to a standard width of 20 characters.
    Text text = new Text( shell, SWT.NONE );
    GC gc = new GC( text );
    FontMetrics fm = gc.getFontMetrics();
    int charWidth = fm.getAverageCharWidth();
    int fieldWidth = text.computeSize( charWidth * 20, SWT.DEFAULT ).x;
    gc.dispose();

    fdlFieldSeparator = new FormData();
    fdlFieldSeparator.top = new FormAttachment( wMonetDBmclientSettingsComp, 3 * margin );
    fdlFieldSeparator.left = new FormAttachment( 0, 3 * margin );
    wlFieldSeparator.setLayoutData( fdlFieldSeparator );

    fdFieldSeparator = new FormData();
    fdFieldSeparator.top = new FormAttachment( wMonetDBmclientSettingsComp, 3 * margin );
    fdFieldSeparator.left = new FormAttachment( middle, margin );
    fdFieldSeparator.width = fieldWidth;
    wFieldSeparator.setLayoutData( fdFieldSeparator );

    fdlFieldEnclosure = new FormData();
    fdlFieldEnclosure.top = new FormAttachment( wFieldSeparator, 2 * margin );
    fdlFieldEnclosure.left = new FormAttachment( 0, 3 * margin );
    wlFieldEnclosure.setLayoutData( fdlFieldEnclosure );

    fdFieldEnclosure = new FormData();
    fdFieldEnclosure.top = new FormAttachment( wFieldSeparator, 2 * margin );
    fdFieldEnclosure.left = new FormAttachment( middle, margin );
    fdFieldEnclosure.width = fieldWidth;
    wFieldEnclosure.setLayoutData( fdFieldEnclosure );

    fdlNULLrepresentation = new FormData();
    fdlNULLrepresentation.top = new FormAttachment( wFieldEnclosure, 2 * margin );
    fdlNULLrepresentation.left = new FormAttachment( 0, 3 * margin );
    wlNULLrepresentation.setLayoutData( fdlNULLrepresentation );

    fdNULLrepresentation = new FormData();
    fdNULLrepresentation.top = new FormAttachment( wFieldEnclosure, 2 * margin );
    fdNULLrepresentation.left = new FormAttachment( middle, margin );
    fdNULLrepresentation.width = fieldWidth;
    wNULLrepresentation.setLayoutData( fdNULLrepresentation );

    // Stream encoding parameter sent to mclient (Label layout)
    fdlEncoding = new FormData();
    fdlEncoding.top = new FormAttachment( wNULLrepresentation, 2 * margin );
    fdlEncoding.left = new FormAttachment( 0, 3 * margin );
    wlEncoding.setLayoutData( fdlEncoding );

    fdEncoding = new FormData();
    fdEncoding.top = new FormAttachment( wNULLrepresentation, 2 * margin );
    fdEncoding.left = new FormAttachment( middle, margin );
    fdEncoding.width = fieldWidth;
    wEncoding.setLayoutData( fdEncoding );

    //
    // Output Fields tab layout
    //
    // Label at the top left of the tab
    fdlReturn = new FormData();
    fdlReturn.top = new FormAttachment( wOutputFieldsComp, 2 * margin );
    fdlReturn.left = new FormAttachment( 0, margin );
    wlReturn.setLayoutData( fdlReturn );

    // button right, top of the tab
    fdDoMapping = new FormData();
    fdDoMapping.top = new FormAttachment( wOutputFieldsComp, 2 * margin );
    fdDoMapping.right = new FormAttachment( 100, -margin );
    wDoMapping.setLayoutData( fdDoMapping );

    // to the left of the button above
    fdGetLU = new FormData();
    fdGetLU.top = new FormAttachment( wOutputFieldsComp, 2 * margin );
    fdGetLU.right = new FormAttachment( wDoMapping, -margin );
    wGetLU.setLayoutData( fdGetLU );

    // to the left of the button above
    fdClearDBCache = new FormData();
    fdClearDBCache.top = new FormAttachment( wOutputFieldsComp, 2 * margin );
    fdClearDBCache.right = new FormAttachment( wGetLU, -margin );
    wClearDBCache.setLayoutData( fdClearDBCache );

    // Table of results
    fdReturn = new FormData();
    fdReturn.top = new FormAttachment( wGetLU, 3 * margin );
    fdReturn.left = new FormAttachment( 0, margin );
    fdReturn.right = new FormAttachment( 100, -margin );
    fdReturn.bottom = new FormAttachment( 100, -2 * margin );
    wReturn.setLayoutData( fdReturn );

    //
    // Layout section ends

    //
    // Search the fields in the background
    //

    final Runnable runnable = new Runnable() {
      public void run() {
        StepMeta stepMeta = transMeta.findStep( stepname );
        if ( stepMeta != null ) {
          try {
            RowMetaInterface row = transMeta.getPrevStepFields( stepMeta );

            // Remember these fields...
            for ( int i = 0; i < row.size(); i++ ) {
              inputFields.put( row.getValueMeta( i ).getName(), i );
            }

            setComboBoxes();
          } catch ( KettleException e ) {
            logError( BaseMessages.getString( PKG, "System.Dialog.GetFieldsFailed.Message" ) );
          }
        }
      }
    };
    new Thread( runnable ).start();

    wbLogFile.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e ) {
        FileDialog dialog = new FileDialog( shell, SWT.OPEN );
        dialog.setFilterExtensions( new String[] { "*" } );
        if ( wLogFile.getText() != null ) {
          dialog.setFileName( wLogFile.getText() );
        }
        dialog.setFilterNames( ALL_FILETYPES );
        if ( dialog.open() != null ) {
          wLogFile.setText( dialog.getFilterPath() + Const.FILE_SEPARATOR + dialog.getFileName() );
        }
      }
    } );

    // Add listeners
    lsOK = new Listener() {
      public void handleEvent( Event e ) {
        ok();
      }
    };
    lsGetLU = new Listener() {
      public void handleEvent( Event e ) {
        getUpdate();
      }
    };

    lsSQL = new Listener() {
      public void handleEvent( Event e ) {
        create();
      }
    };
    lsCancel = new Listener() {
      public void handleEvent( Event e ) {
        cancel();
      }
    };

    wOK.addListener( SWT.Selection, lsOK );
    wGetLU.addListener( SWT.Selection, lsGetLU );
    wSQL.addListener( SWT.Selection, lsSQL );
    wCancel.addListener( SWT.Selection, lsCancel );

    lsDef = new SelectionAdapter() {
      public void widgetDefaultSelected( SelectionEvent e ) {
        ok();
      }
    };

    wStepname.addSelectionListener( lsDef );
    wSchema.addSelectionListener( lsDef );
    wTable.addSelectionListener( lsDef );
    wBufferSize.addSelectionListener( lsDef );
    wLogFile.addSelectionListener( lsDef );

    wFieldSeparator.addSelectionListener( lsDef );

    // Detect X or ALT-F4 or something that kills this window...
    shell.addShellListener( new ShellAdapter() {
      public void shellClosed( ShellEvent e ) {
        cancel();
      }
    } );

    wbTable.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e ) {
        getTableName();
      }
    } );

    // Set the shell size, based upon previous time...
    setSize();

    getData();
    setTableFieldCombo();
    input.setChanged( changed );

    shell.open();
    while ( !shell.isDisposed() ) {
      if ( !display.readAndDispatch() ) {
        display.sleep();
      }
    }
    return stepname;
  }

  protected void setTableFieldCombo() {
    Runnable fieldLoader = new Runnable() {
      public void run() {
        if ( !wTable.isDisposed() && !wConnection.isDisposed() && !wSchema.isDisposed() ) {
          final String tableName = wTable.getText(), connectionName = wConnection.getText(), schemaName =
            wSchema.getText();

          // clear
          for ( ColumnInfo colInfo : tableFieldColumns ) {
            colInfo.setComboValues( new String[] {} );
          }
          if ( !Utils.isEmpty( tableName ) ) {
            DatabaseMeta ci = transMeta.findDatabase( connectionName );
            if ( ci != null ) {
              Database db = new Database( loggingObject, ci );
              try {
                db.connect();

                String schemaTable =
                  ci.getQuotedSchemaTableCombination( transMeta.environmentSubstitute( schemaName ), transMeta
                    .environmentSubstitute( tableName ) );
                RowMetaInterface r = db.getTableFields( schemaTable );
                if ( null != r ) {
                  String[] fieldNames = r.getFieldNames();
                  if ( null != fieldNames ) {
                    for ( ColumnInfo colInfo : tableFieldColumns ) {
                      colInfo.setComboValues( fieldNames );
                    }
                  }
                }
              } catch ( Exception e ) {
                for ( ColumnInfo colInfo : tableFieldColumns ) {
                  colInfo.setComboValues( new String[] {} );
                }
                // ignore any errors here. drop downs will not be
                // filled, but no problem for the user
              } finally {
                try {
                  if ( db != null ) {
                    db.disconnect();
                  }
                } catch ( Exception ignored ) {
                  // ignore any errors here.
                  db = null;
                }
              }
            }
          }
        }
      }
    };
    shell.getDisplay().asyncExec( fieldLoader );
  }

  /**
   * Copy information from the meta-data input to the dialog fields.
   * <p/>
   * This method is called each time the dialog is opened.
   */
  public void getData() {
    if ( log.isDebug() ) {
      logDebug( BaseMessages.getString( PKG, "MonetDBBulkLoaderDialog.Log.GettingKeyInfo" ) );
    }

    if ( input.getFieldTable() != null ) {
      for ( int i = 0; i < input.getFieldTable().length; i++ ) {
        TableItem item = wReturn.table.getItem( i );
        if ( input.getFieldTable()[i] != null ) {
          item.setText( 1, input.getFieldTable()[i] );
        }
        if ( input.getFieldStream()[i] != null ) {
          item.setText( 2, input.getFieldStream()[i] );
        }
        item.setText( 3, input.getFieldFormatOk()[i] ? "Y" : "N" );
      }
    }

    if ( input.getDatabaseMeta() != null ) {
      wConnection.setText( input.getDatabaseMeta().getName() );
    } else {
      if ( transMeta.nrDatabases() == 1 ) {
        wConnection.setText( transMeta.getDatabase( 0 ).getName() );
      }
    }
    // General Settings Tab values from step meta-data configuration.
    if ( input.getSchemaName() != null ) {
      wSchema.setText( input.getSchemaName() );
    }
    if ( input.getTableName() != null ) {
      wTable.setText( input.getTableName() );
    }
    wBufferSize.setText( "" + input.getBufferSize() );
    if ( input.getLogFile() != null ) {
      wLogFile.setText( input.getLogFile() );
    }
    wTruncate.setSelection( input.isTruncate() );
    wFullyQuoteSQL.setSelection( input.isFullyQuoteSQL() );

    // MonetDB mclient Settings tab
    if ( input.getFieldSeparator() != null ) {
      wFieldSeparator.setText( input.getFieldSeparator() );
    }
    if ( input.getFieldEnclosure() != null ) {
      wFieldEnclosure.setText( input.getFieldEnclosure() );
    }
    if ( input.getNULLrepresentation() != null ) {
      wNULLrepresentation.setText( input.getNULLrepresentation() );
    }
    if ( input.getEncoding() != null ) {
      wEncoding.setText( input.getEncoding() );
    }

    wReturn.setRowNums();
    wReturn.optWidth( true );

    wStepname.selectAll();
    wStepname.setFocus();
  }

  protected void cancel() {
    stepname = null;
    input.setChanged( changed );
    dispose();
  }

  protected void setComboBoxes() {
    // Something was changed in the row.
    //
    final Map<String, Integer> fields = new HashMap<String, Integer>();

    // Add the currentMeta fields...
    fields.putAll( inputFields );

    Set<String> keySet = fields.keySet();
    List<String> entries = new ArrayList<String>( keySet );

    String[] fieldNames = entries.toArray( new String[entries.size()] );
    Const.sortStrings( fieldNames );
    // return fields
    if ( ciReturn != null ) {
      ciReturn[1].setComboValues( fieldNames );
    }
  }

  /*
   * When the OK button is pressed, this method is called to take all values from the dialog and save them in the step
   * meta data.
   */
  protected void getInfo( MonetDBBulkLoaderMeta inf ) {
    int nrfields = wReturn.nrNonEmpty();

    inf.allocate( nrfields );

    if ( log.isDebug() ) {
      logDebug( BaseMessages.getString( PKG, "MonetDBBulkLoaderDialog.Log.FoundFields", "" + nrfields ) );
    }
    //CHECKSTYLE:Indentation:OFF
    for ( int i = 0; i < nrfields; i++ ) {
      TableItem item = wReturn.getNonEmpty( i );
      inf.getFieldTable()[i] = item.getText( 1 );
      inf.getFieldStream()[i] = item.getText( 2 );
      inf.getFieldFormatOk()[i] = "Y".equalsIgnoreCase( item.getText( 3 ) );
    }
    // General Settings Tab values from step meta-data configuration.
    inf.setDbConnectionName( wConnection.getText() );
    inf.setSchemaName( wSchema.getText() );
    inf.setTableName( wTable.getText() );
    inf.setDatabaseMeta( transMeta.findDatabase( wConnection.getText() ) );
    inf.setBufferSize( wBufferSize.getText() );
    inf.setLogFile( wLogFile.getText() );
    inf.setTruncate( wTruncate.getSelection() );
    inf.setFullyQuoteSQL( wFullyQuoteSQL.getSelection() );

    // MonetDB API Settings tab
    inf.setFieldSeparator( wFieldSeparator.getText() );
    inf.setFieldEnclosure( wFieldEnclosure.getText() );
    inf.setNULLrepresentation( wNULLrepresentation.getText() );
    inf.setEncoding( wEncoding.getText() );

    stepname = wStepname.getText(); // return value
  }

  protected void ok() {
    if ( Utils.isEmpty( wStepname.getText() ) ) {
      return;
    }

    // Get the information for the dialog into the input structure.
    getInfo( input );

    /*
     * if (input.getDatabaseMeta() == null) { MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR);
     * mb.setMessage(BaseMessages.getString(PKG, "MonetDBBulkLoaderDialog.InvalidConnection.DialogMessage"));
     * mb.setText(BaseMessages.getString(PKG, "MonetDBBulkLoaderDialog.InvalidConnection.DialogTitle")); mb.open(); }
     */
    dispose();
  }

  protected void getTableName() {
    DatabaseMeta inf = null;
    // New class: SelectTableDialog
    int connr = -1;
    if ( wConnection != null && wConnection.getCComboWidget() != null ) {
      connr = wConnection.getCComboWidget().getSelectionIndex();
    }
    if ( connr >= 0 ) {
      inf = transMeta.getDatabase( connr );
    }
    if ( inf != null ) {
      if ( log.isDebug() ) {
        logDebug( BaseMessages.getString( PKG, "MonetDBBulkLoaderDialog.Log.LookingAtConnection" )
          + inf.toString() );
      }

      DatabaseExplorerDialog std = new DatabaseExplorerDialog( shell, SWT.NONE, inf, transMeta.getDatabases() );
      std.setSelectedSchemaAndTable( wSchema.getText(), wTable.getText() );
      if ( std.open() ) {
        wSchema.setText( Const.NVL( std.getSchemaName(), "" ) );
        wTable.setText( Const.NVL( std.getTableName(), "" ) );
      }
    } else {
      MessageBox mb = new MessageBox( shell, SWT.OK | SWT.ICON_ERROR );
      mb.setMessage( BaseMessages.getString( PKG, "MonetDBBulkLoaderDialog.InvalidConnection.DialogMessage" ) );
      mb.setText( BaseMessages.getString( PKG, "MonetDBBulkLoaderDialog.InvalidConnection.DialogTitle" ) );
      mb.open();
    }
  }

  /**
   * Reads in the fields from the previous steps and from the ONE next step and opens an EnterMappingDialog with this
   * information. After the user did the mapping, those information is put into the Select/Rename table.
   */
  private void generateMappings() {

    // Determine the source and target fields...
    //
    RowMetaInterface sourceFields;
    RowMetaInterface targetFields;

    try {
      sourceFields = transMeta.getPrevStepFields( stepMeta );
    } catch ( KettleException e ) {
      new ErrorDialog( shell,
        BaseMessages.getString( PKG, "MonetDBBulkLoaderDialog.DoMapping.UnableToFindSourceFields.Title" ),
        BaseMessages.getString( PKG, "MonetDBBulkLoaderDialog.DoMapping.UnableToFindSourceFields.Message" ), e );
      return;
    }
    // refresh data
    input.setDatabaseMeta( transMeta.findDatabase( wConnection.getText() ) );
    input.setTableName( transMeta.environmentSubstitute( wTable.getText() ) );
    StepMetaInterface stepMetaInterface = stepMeta.getStepMetaInterface();
    try {
      targetFields = stepMetaInterface.getRequiredFields( transMeta );
    } catch ( KettleException e ) {
      new ErrorDialog( shell,
        BaseMessages.getString( PKG, "MonetDBBulkLoaderDialog.DoMapping.UnableToFindTargetFields.Title" ),
        BaseMessages.getString( PKG, "MonetDBBulkLoaderDialog.DoMapping.UnableToFindTargetFields.Message" ), e );
      return;
    }

    String[] inputNames = new String[sourceFields.size()];
    for ( int i = 0; i < sourceFields.size(); i++ ) {
      ValueMetaInterface value = sourceFields.getValueMeta( i );
      inputNames[i] = value.getName() + EnterMappingDialog.STRING_ORIGIN_SEPARATOR + value.getOrigin() + ")";
    }

    // Create the existing mapping list...
    //
    List<SourceToTargetMapping> mappings = new ArrayList<SourceToTargetMapping>();
    StringBuilder missingSourceFields = new StringBuilder();
    StringBuilder missingTargetFields = new StringBuilder();

    int nrFields = wReturn.nrNonEmpty();
    for ( int i = 0; i < nrFields; i++ ) {
      TableItem item = wReturn.getNonEmpty( i );
      String source = item.getText( 2 );
      String target = item.getText( 1 );

      int sourceIndex = sourceFields.indexOfValue( source );
      if ( sourceIndex < 0 ) {
        missingSourceFields.append( Const.CR ).append( "   " ).append( source ).append( " --> " ).append( target );
      }
      int targetIndex = targetFields.indexOfValue( target );
      if ( targetIndex < 0 ) {
        missingTargetFields.append( Const.CR ).append( "   " ).append( source ).append( " --> " ).append( target );
      }
      if ( sourceIndex < 0 || targetIndex < 0 ) {
        continue;
      }

      SourceToTargetMapping mapping = new SourceToTargetMapping( sourceIndex, targetIndex );
      mappings.add( mapping );
    }

    // show a confirm dialog if some missing field was found
    //
    if ( missingSourceFields.length() > 0 || missingTargetFields.length() > 0 ) {

      String message = "";
      if ( missingSourceFields.length() > 0 ) {
        message +=
          BaseMessages.getString(
            PKG, "MonetDBBulkLoaderDialog.DoMapping.SomeSourceFieldsNotFound", missingSourceFields.toString() )
            + Const.CR;
      }
      if ( missingTargetFields.length() > 0 ) {
        message +=
          BaseMessages.getString(
            PKG, "MonetDBBulkLoaderDialog.DoMapping.SomeTargetFieldsNotFound", missingSourceFields.toString() )
            + Const.CR;
      }
      message += Const.CR;
      message +=
        BaseMessages.getString( PKG, "MonetDBBulkLoaderDialog.DoMapping.SomeFieldsNotFoundContinue" ) + Const.CR;
      MessageDialog.setDefaultImage( GUIResource.getInstance().getImageSpoon() );
      boolean goOn =
        MessageDialog.openConfirm( shell, BaseMessages.getString(
          PKG, "MonetDBBulkLoaderDialog.DoMapping.SomeFieldsNotFoundTitle" ), message );
      if ( !goOn ) {
        return;
      }
    }
    EnterMappingDialog d =
      new EnterMappingDialog( MonetDBBulkLoaderDialog.this.shell, sourceFields.getFieldNames(), targetFields
        .getFieldNames(), mappings );
    mappings = d.open();

    // mappings == null if the user pressed cancel
    //
    if ( mappings != null ) {
      // Clear and re-populate!
      //
      wReturn.table.removeAll();
      wReturn.table.setItemCount( mappings.size() );
      for ( int i = 0; i < mappings.size(); i++ ) {
        SourceToTargetMapping mapping = mappings.get( i );
        TableItem item = wReturn.table.getItem( i );
        item.setText( 2, sourceFields.getValueMeta( mapping.getSourcePosition() ).getName() );
        item.setText( 1, targetFields.getValueMeta( mapping.getTargetPosition() ).getName() );
      }
      wReturn.setRowNums();
      wReturn.optWidth( true );
    }
  }

  /*
   * Runs when the "Get Fields" button is pressed on the Output Fields dialog tab.
   */
  private void getUpdate() {
    try {

      RowMetaInterface r = transMeta.getPrevStepFields( stepname );
      if ( r != null ) {
        TableItemInsertListener listener = new TableItemInsertListener() {
          public boolean tableItemInserted( TableItem tableItem, ValueMetaInterface v ) {
            if ( v.getType() == ValueMetaInterface.TYPE_DATE ) {
              // The default is : format is OK for dates, see if this sticks later on...
              //
              tableItem.setText( 3, "Y" );
            } else {
              tableItem.setText( 3, "Y" ); // default is OK too...
            }
            return true;
          }
        };
        BaseStepDialog.getFieldsFromPrevious( r, wReturn, 1, new int[] { 1, 2 }, new int[] {}, -1, -1, listener );
      }
    } catch ( KettleException ke ) {
      new ErrorDialog( shell, BaseMessages
        .getString( PKG, "MonetDBBulkLoaderDialog.FailedToGetFields.DialogTitle" ), BaseMessages.getString(
        PKG, "MonetDBBulkLoaderDialog.FailedToGetFields.DialogMessage" ), ke );
    }
  }

  // Generate code for create table...
  // Conversions done by Database
  private void create() {
    try {
      MonetDBBulkLoaderMeta info = new MonetDBBulkLoaderMeta();
      getInfo( info );

      String name = stepname; // new name might not yet be linked to other steps!

      SQLStatement sql = info.getTableDdl( transMeta, name, false, null, false );
      if ( !sql.hasError() ) {
        if ( sql.hasSQL() ) {
          SQLEditor sqledit =
            new SQLEditor( transMeta, shell, SWT.NONE, info.getDatabaseMeta(), transMeta.getDbCache(), sql
              .getSQL() );
          sqledit.open();
        } else {
          MessageBox mb = new MessageBox( shell, SWT.OK | SWT.ICON_INFORMATION );
          mb.setMessage( BaseMessages.getString( PKG, "MonetDBBulkLoaderDialog.NoSQLNeeds.DialogMessage" ) );
          mb.setText( BaseMessages.getString( PKG, "MonetDBBulkLoaderDialog.NoSQLNeeds.DialogTitle" ) );
          mb.open();
        }
      } else {
        MessageBox mb = new MessageBox( shell, SWT.OK | SWT.ICON_ERROR );
        mb.setMessage( sql.getError() );
        mb.setText( BaseMessages.getString( PKG, "MonetDBBulkLoaderDialog.SQLError.DialogTitle" ) );
        mb.open();
      }
    } catch ( KettleException ke ) {
      new ErrorDialog(
        shell, BaseMessages.getString( PKG, "MonetDBBulkLoaderDialog.CouldNotBuildSQL.DialogTitle" ),
        BaseMessages.getString( PKG, "MonetDBBulkLoaderDialog.CouldNotBuildSQL.DialogMessage" ), ke );
    }
  }

  public ComboVar addConnectionLine( Composite parent, Control previous, int middle, int margin,
    final Class<? extends DatabaseInterface> databaseType, final TransMeta transMeta ) {
    final ComboVar wConnection;
    final FormData fdlConnection, fdbConnection, fdeConnection, fdConnection;

    wConnection = new ComboVar( transMeta, parent, SWT.BORDER );
    props.setLook( wConnection );

    addDatabases( wConnection, null );

    //
    // Database connection (Label)
    //
    Label wlConnection = new Label( parent, SWT.LEFT );
    wlConnection.setText( BaseMessages.getString( PKG, "MonetDBBulkLoaderDialog.Connection.Label" ) );
    props.setLook( wlConnection );

    //
    // New (Button)
    //
    Button wbnConnection = new Button( parent, SWT.RIGHT );
    wbnConnection.setText( BaseMessages.getString( PKG, "MonetDBBulkLoaderDialog.NewConnectionButton.Label" ) );
    wbnConnection.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e ) {
        DatabaseMeta databaseMeta = new DatabaseMeta();
        databaseMeta.shareVariablesWith( transMeta );
        DatabaseDialog cid = getDatabaseDialog( shell );
        cid.setDatabaseMeta( databaseMeta );
        cid.setModalDialog( true );
        if ( cid.open() != null ) {
          transMeta.addDatabase( databaseMeta );
          wConnection.removeAll();
          addDatabases( wConnection, databaseType );
          selectDatabase( wConnection, databaseMeta.getName() );
        }
      }
    } );

    //
    // Edit (Button)
    //
    Button wbeConnection = new Button( parent, SWT.RIGHT );
    wbeConnection.setText( BaseMessages.getString( PKG, "BaseStepDialog.EditConnectionButton.Label" ) );
    wbeConnection.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e ) {
        DatabaseMeta databaseMeta = transMeta.findDatabase( wConnection.getText() );
        if ( databaseMeta != null ) {
          databaseMeta.shareVariablesWith( transMeta );

          DatabaseDialog cid = getDatabaseDialog( shell );
          cid.setDatabaseMeta( databaseMeta );
          cid.setModalDialog( true );
          if ( cid.open() != null ) {
            wConnection.removeAll();
            addDatabases( wConnection, null );
            selectDatabase( wConnection, databaseMeta.getName() );
          }
        }
      }
    } );

    //
    // Database connection (Label layout)
    //
    fdlConnection = new FormData();
    fdlConnection.left = new FormAttachment( 0, margin ); // attaches to the left of the bounding container
    if ( previous != null ) {
      fdlConnection.top = new FormAttachment( previous, margin + 10 );
    } else {
      fdlConnection.top = new FormAttachment( 0, 0 );
    }
    wlConnection.setLayoutData( fdlConnection );

    //
    // New (Button layout)
    //
    fdbConnection = new FormData();
    fdbConnection.right = new FormAttachment( 100, -margin );
    if ( previous != null ) {
      fdbConnection.top = new FormAttachment( previous, margin );
    } else {
      fdbConnection.top = new FormAttachment( 0, 0 );
    }
    wbnConnection.setLayoutData( fdbConnection );

    //
    // Edit (Button layout)
    //
    fdeConnection = new FormData();
    fdeConnection.right = new FormAttachment( wbnConnection, -margin );
    if ( previous != null ) {
      fdeConnection.top = new FormAttachment( previous, margin );
    } else {
      fdeConnection.top = new FormAttachment( 0, 0 );
    }
    wbeConnection.setLayoutData( fdeConnection );

    //
    // Connection (Combo Box layout)
    //
    // The right side of the Combo Box is attached to the Edit button
    // The left side of the Combo Box is attached to the side of the database connection label
    // Effectively, it resizes the combo box between the two components.
    fdConnection = new FormData();
    // fdConnection.height = fieldHeight;
    fdConnection.left = new FormAttachment( middle, margin );
    fdConnection.right = new FormAttachment( wbeConnection, -margin );
    if ( previous != null ) {
      fdConnection.top = new FormAttachment( previous, margin + 10 );
    } else {
      fdConnection.top = new FormAttachment( wlConnection, margin );
    }

    wConnection.setLayoutData( fdConnection );

    return wConnection;
  }

  public void addDatabases( ComboVar wConnection, Class<? extends DatabaseInterface> databaseType ) {
    for ( int i = 0; i < transMeta.nrDatabases(); i++ ) {
      DatabaseMeta ci = transMeta.getDatabase( i );
      if ( databaseType == null || ci.getDatabaseInterface().getClass().equals( databaseType ) ) {
        wConnection.add( ci.getName() );
      }
    }
    // Add the metaDBConnectionName if we have it
    // and it is already not added to the list in wConnection.
    if ( !Utils.isEmpty( input.getDbConnectionName() ) ) {
      String[] arrayDatabaseList = wConnection.getItems();
      if ( arrayDatabaseList == null ) {
        List<String> databaseNameList = Arrays.asList();
        if ( !databaseNameList.contains( input.getDbConnectionName() ) ) {
          wConnection.add( input.getDbConnectionName() );
        }
      }
    }
  }

  public void selectDatabase( ComboVar wConnection, String name ) {
    int idx = wConnection.getCComboWidget().indexOf( name );
    if ( idx >= 0 ) {
      wConnection.select( idx );
    }
  }

}
