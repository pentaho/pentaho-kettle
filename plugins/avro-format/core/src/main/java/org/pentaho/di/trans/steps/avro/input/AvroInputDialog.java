/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2022 by Hitachi Vantara : http://www.pentaho.com
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
package org.pentaho.di.trans.steps.avro.input;

import org.pentaho.di.trans.steps.avro.BaseAvroStepDialog;
import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.annotations.PluginDialog;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaFactory;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.TransPreviewFactory;
import org.pentaho.di.ui.core.FormDataBuilder;
import org.pentaho.di.ui.core.dialog.EnterNumberDialog;
import org.pentaho.di.ui.core.dialog.EnterTextDialog;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.dialog.PreviewRowsDialog;
import org.pentaho.di.ui.core.events.dialog.SelectionAdapterFileDialogTextVar;
import org.pentaho.di.ui.core.events.dialog.SelectionAdapterOptions;
import org.pentaho.di.ui.core.events.dialog.SelectionOperation;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.ColumnsResizer;
import org.pentaho.di.ui.core.widget.ComboVar;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.trans.dialog.TransPreviewProgressDialog;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@PluginDialog( id = "AvroInputNew", image = "AI.svg", pluginType = PluginDialog.PluginType.STEP,
        documentationUrl = "mk-95pdia003/pdi-transformation-steps/avro-input" )
public class AvroInputDialog extends BaseAvroStepDialog {
  private static final Class<?> PKG = AvroInputMeta.class;

  private static final int SHELL_WIDTH = 698;
  private static final int SHELL_HEIGHT = 554;
  private static final int RADIO_BUTTON_WIDTH = 150;

  private static final int AVRO_ORIGINAL_PATH_COLUMN_INDEX = 1;
  private static final int AVRO_DISPLAY_PATH_COLUMN_INDEX = 2;
  private static final int AVRO_TYPE_COLUMN_INDEX = 3;
  private static final int AVRO_INDEXED_VALUES_COLUMN_INDEX = 4;
  private static final int FIELD_NAME_COLUMN_INDEX = 5;
  private static final int FIELD_TYPE_COLUMN_INDEX = 6;
  private static final int FORMAT_COLUMN_INDEX = 7;

  private TableView wInputFields;
  protected Button wbSchemaBrowse;
  protected Composite wSchemaFileComposite;
  protected Composite wSchemaFieldComposite;
  protected Group wSourceGroup;
  Button wbGetSchemaFromFile;
  Button wbGetSchemaFromField;
  ComboVar wSchemaFieldNameCombo;
  private TableView wLookupView;
  private AvroInputMeta meta;

  private Button wPassThruFields;
  private Button wAllowNullValues;
  private TextVar wSchemaPath;

  public AvroInputDialog( Shell parent, Object in, TransMeta transMeta, String sname ) {
    super( parent, (AvroInputMeta) in, transMeta, sname );
    this.meta = (AvroInputMeta) in;
  }

  protected Control createAfterFile( Composite afterFile ) {
    CTabFolder wTabFolder = new CTabFolder( afterFile, SWT.BORDER );
    wTabFolder.setSimple( false );

    addSourceTab( wTabFolder );
    addFieldsTab( wTabFolder );
    addLookupFieldsTab( wTabFolder );

    wTabFolder.setLayoutData( new FormDataBuilder().left().top( 0, MARGIN ).right().bottom().result() );
    wTabFolder.setSelection( 0 );

    return wTabFolder;
  }

  protected void populateNestedFieldsTable() {
    // this schema overrides any that might be in a container file
    String schemaFileName = wSchemaPath.getText();
    schemaFileName = transMeta.environmentSubstitute( schemaFileName );

    String avroFileName = wPath.getText();
    avroFileName = transMeta.environmentSubstitute( avroFileName );

    List<? extends IAvroInputField> defaultFields;
    try {
      defaultFields = AvroInput
        .getLeafFields( schemaFileName, avroFileName, transMeta );
      if ( defaultFields != null ) {
        wInputFields.clearAll();
        for ( IAvroInputField field : defaultFields ) {
          TableItem item = new TableItem( wInputFields.table, SWT.NONE );
          if ( field != null ) {
            setField( item, field.getDisplayableAvroFieldName(), AVRO_ORIGINAL_PATH_COLUMN_INDEX );
            setField( item, clearIndexFromFieldName( field.getDisplayableAvroFieldName() ),
              AVRO_DISPLAY_PATH_COLUMN_INDEX );
            setField( item, field.getAvroType().getName(), AVRO_TYPE_COLUMN_INDEX );
            setField( item, field.getIndexedValues(), AVRO_INDEXED_VALUES_COLUMN_INDEX );
            setField( item, field.getPentahoFieldName(), FIELD_NAME_COLUMN_INDEX );
            setField( item, ValueMetaFactory.getValueMetaName( field.getPentahoType() ), FIELD_TYPE_COLUMN_INDEX );
            setField( item, field.getStringFormat(), FORMAT_COLUMN_INDEX );
          }
        }

        wInputFields.removeEmptyRows();
        wInputFields.setRowNums();
        wInputFields.optWidth( true );
      }
    } catch ( Exception ex ) {
      logError( BaseMessages.getString( PKG, "AvroInput.Error.UnableToLoadSchemaFromContainerFile" ), ex );
      new ErrorDialog( shell, stepname, BaseMessages.getString( PKG,
        "AvroInput.Error.UnableToLoadSchemaFromContainerFile", avroFileName ), ex );
    }
  }

  private void setField( TableItem item, String fieldValue, int fieldIndex ) {
    if ( !Utils.isEmpty( fieldValue ) ) {
      item.setText( fieldIndex, fieldValue );
    }
  }

  private void addLookupFieldsTab( CTabFolder wTabFolder ) {
    CTabItem wVarsTab = new CTabItem( wTabFolder, SWT.NONE );
    wVarsTab.setText( BaseMessages.getString( PKG, "AvroInputDialog.VarsTab.Title" ) );
    Composite wVarsComp = new Composite( wTabFolder, SWT.NONE );

    FormLayout varsLayout = new FormLayout();
    varsLayout.marginWidth = MARGIN;
    varsLayout.marginHeight = MARGIN;
    wVarsComp.setLayout( varsLayout );

    // get lookup fields but
    Button wGetLookupFieldsBut = new Button( wVarsComp, SWT.PUSH | SWT.CENTER );
    wGetLookupFieldsBut.setText( BaseMessages.getString( PKG, "AvroInputDialog.Button.GetLookupFields" ) );
    wGetLookupFieldsBut.setLayoutData( new FormDataBuilder().bottom( 100, -Const.MARGIN * 2 ).right().result() );
    wGetLookupFieldsBut.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent e ) {
        // get incoming field names
        getIncomingFields();
      }
    } );

    final ColumnInfo[] colinf2 =
      new ColumnInfo[] {
        new ColumnInfo( BaseMessages.getString( PKG, "AvroInputDialog.Fields.LOOKUP_NAME" ),
          ColumnInfo.COLUMN_TYPE_TEXT, false ),
        new ColumnInfo( BaseMessages.getString( PKG, "AvroInputDialog.Fields.LOOKUP_VARIABLE" ),
          ColumnInfo.COLUMN_TYPE_TEXT, false ),
        new ColumnInfo( BaseMessages.getString( PKG, "AvroInputDialog.Fields.LOOKUP_DEFAULT_VALUE" ),
          ColumnInfo.COLUMN_TYPE_TEXT, false ), };
    colinf2[ 0 ].setAutoResize( false );
    colinf2[ 1 ].setAutoResize( false );
    colinf2[ 2 ].setAutoResize( false );
    wLookupView =
      new TableView( transMeta, wVarsComp, SWT.FULL_SELECTION | SWT.MULTI | SWT.BORDER, colinf2, 1, lsMod, props );
    wLookupView.setLayoutData( new FormDataBuilder().top( 0, Const.MARGIN * 2 )
      .bottom( wGetLookupFieldsBut, -Const.MARGIN * 2 ).left().right().result() );

    ColumnsResizer resizer = new ColumnsResizer( 0, 33, 33, 34 );
    resizer.addColumnResizeListeners( wLookupView.getTable() );
    wLookupView.getTable().addListener( SWT.Resize, resizer );
    wLookupView.optWidth( true );

    wVarsTab.setControl( wVarsComp );
  }

  private void getIncomingFields() {
    try {
      RowMetaInterface r = transMeta.getPrevStepFields( stepname );
      if ( r != null ) {
        BaseStepDialog.getFieldsFromPrevious( r, wLookupView, 1, new int[] { 1 }, null, -1, -1, null );
      }
    } catch ( KettleException e ) {
      new ErrorDialog( shell, BaseMessages.getString( PKG, "System.Dialog.GetFieldsFailed.Title" ), BaseMessages
        .getString( PKG, "System.Dialog.GetFieldsFailed.Message" ), e );
    }
  }

  private void addFieldsTab( CTabFolder wTabFolder ) {
    CTabItem wTab = new CTabItem( wTabFolder, SWT.NONE );
    wTab.setText( BaseMessages.getString( PKG, "AvroInputDialog.FieldsTab.TabTitle" ) );

    Composite wComp = new Composite( wTabFolder, SWT.NONE );

    FormLayout layout = new FormLayout();
    layout.marginWidth = MARGIN;
    layout.marginHeight = MARGIN;
    layout.marginBottom = MARGIN;
    wComp.setLayout( layout );

    //get fields button
    lsGet = e -> populateNestedFieldsTable();
    Button wGetFields = new Button( wComp, SWT.PUSH );
    wGetFields.setText( BaseMessages.getString( PKG, "AvroInputDialog.Fields.Get" ) );
    wGetFields.setLayoutData( new FormDataBuilder().bottom().right().result() );
    wGetFields.addListener( SWT.Selection, lsGet );

    // fields table
    ColumnInfo avroOriginalPathColumnInfo =
      new ColumnInfo( "Original Avro Path", ColumnInfo.COLUMN_TYPE_TEXT,
        false, true );
    ColumnInfo avroDisplayPathColumnInfo =
      new ColumnInfo( BaseMessages.getString( PKG, "AvroInputDialog.Fields.column.Path" ), ColumnInfo.COLUMN_TYPE_NONE,
        false, true );
    ColumnInfo avroTypeColumnInfo =
      new ColumnInfo( BaseMessages.getString( PKG, "AvroInputDialog.Fields.column.avro.type" ),
        ColumnInfo.COLUMN_TYPE_TEXT, false, true );
    ColumnInfo avroIndexColumnInfo =
      new ColumnInfo( BaseMessages.getString( PKG, "AvroInputDialog.Fields.column.avro.indexedValues" ),
        ColumnInfo.COLUMN_TYPE_TEXT, false, false );
    ColumnInfo nameColumnInfo =
      new ColumnInfo( BaseMessages.getString( PKG, "AvroInputDialog.Fields.column.Name" ), ColumnInfo.COLUMN_TYPE_TEXT,
        false, false );
    ColumnInfo typeColumnInfo = new ColumnInfo( BaseMessages.getString( PKG, "AvroInputDialog.Fields.column.Type" ),
      ColumnInfo.COLUMN_TYPE_CCOMBO, ValueMetaFactory.getValueMetaNames() );
    ColumnInfo formatColumnInfo = new ColumnInfo( BaseMessages.getString( PKG, "AvroInputDialog.Fields.column.Format" ),
      ColumnInfo.COLUMN_TYPE_CCOMBO, Const.getDateFormats() );

    ColumnInfo[] parameterColumns =
      new ColumnInfo[] { avroOriginalPathColumnInfo, avroDisplayPathColumnInfo, avroTypeColumnInfo, avroIndexColumnInfo,
        nameColumnInfo, typeColumnInfo, formatColumnInfo };
    parameterColumns[ 1 ].setAutoResize( false );
    parameterColumns[ 3 ].setAutoResize( false );
    parameterColumns[ 4 ].setUsingVariables( true );
    parameterColumns[ 6 ].setAutoResize( false );

    wInputFields =
      new TableView( transMeta, wComp, SWT.FULL_SELECTION | SWT.SINGLE | SWT.BORDER | SWT.NO_SCROLL | SWT.V_SCROLL,
        parameterColumns, 8, null, props );
    ColumnsResizer resizer = new ColumnsResizer( 0, 0, 20, 15, 15, 20, 15, 15 );
    wInputFields.getTable().addListener( SWT.Resize, resizer );
    wInputFields.setLayoutData( new FormDataBuilder().left().right().top( 0, Const.MARGIN * 2 )
      .bottom( wGetFields, -FIELDS_SEP ).result() );

    wInputFields.setRowNums();
    wInputFields.optWidth( true );

    // Accept fields from previous steps?
    wPassThruFields = new Button( wComp, SWT.CHECK );
    wPassThruFields.setText( BaseMessages.getString( PKG, "AvroInputDialog.PassThruFields.Label" ) );
    wPassThruFields.setToolTipText( BaseMessages.getString( PKG, "AvroInputDialog.PassThruFields.Tooltip" ) );
    wPassThruFields.setOrientation( SWT.LEFT_TO_RIGHT );
    wPassThruFields.setLayoutData( new FormDataBuilder().left().top( wInputFields, 10 ).result() );

    // Accept fields from previous steps?
    wAllowNullValues = new Button( wComp, SWT.CHECK );
    wAllowNullValues.setText( BaseMessages.getString( PKG, "AvroInputDialog.AllowNullValues.Label" ) );
    wAllowNullValues.setOrientation( SWT.LEFT_TO_RIGHT );

    wAllowNullValues.setLayoutData( new FormDataBuilder().left().top( wPassThruFields, 5 ).result() );

    wComp.setLayoutData( new FormDataBuilder().left().top().right().bottom().result() );

    wTab.setControl( wComp );
    for ( ColumnInfo col : parameterColumns ) {
      col.setAutoResize( false );
    }
    resizer.addColumnResizeListeners( wInputFields.getTable() );
    setTruncatedColumn( wInputFields.getTable(), 1 );
    if ( !Const.isWindows() ) {
      addColumnTooltip( wInputFields.getTable(), 1 );
    }

    wInputFields.getColumns()[ AVRO_INDEXED_VALUES_COLUMN_INDEX ].setAutoResize( true );
  }

  protected void addSourceTab( CTabFolder wTabFolder ) {
    // Create & Set up a new Tab Item
    CTabItem wTab = new CTabItem( wTabFolder, SWT.NONE );
    wTab.setText( getBaseMsg( "AvroDialog.File.TabTitle" ) );
    Composite wTabComposite = new Composite( wTabFolder, SWT.NONE );
    wTab.setControl( wTabComposite );
    FormLayout formLayout = new FormLayout();
    formLayout.marginHeight = MARGIN;
    wTabComposite.setLayout( formLayout );

    // Create file encoding Drop Down
    Label encodingLabel = new Label( wTabComposite, SWT.NONE );
    encodingLabel.setText( getBaseMsg( "AvroDialog.Encoding.Label" ) );
    encodingLabel.setLayoutData( new FormDataBuilder().top().right( 100, -MARGIN ).left( 0, MARGIN ).result() );

    encodingCombo = new CCombo( wTabComposite, SWT.BORDER | SWT.READ_ONLY );
    String[] availFormats = {
      BaseMessages.getString( PKG, "AvroInputDialog.AvroFile.Label" ),
      BaseMessages.getString( PKG, "AvroInputDialog.JsonDatum.Label" ),
      BaseMessages.getString( PKG, "AvroInputDialog.BinaryDatum.Label" ),
      BaseMessages.getString( PKG, "AvroInputDialog.AvroFile.AlternateSchema.Label" )
    };

    encodingCombo.setItems( availFormats );
    encodingCombo.select( 0 );
    encodingCombo.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent e ) {
        wSourceGroup.setVisible( encodingCombo.getSelectionIndex() > 0 );
      }
    } );
    encodingCombo.setLayoutData( new FormDataBuilder().top( encodingLabel, 5 ).left( 0, MARGIN )
      .right( 0, MARGIN + 200 ).result() );

    // Set up the File settings Group
    Group wFileSettingsGroup = new Group( wTabComposite, SWT.SHADOW_NONE );
    wFileSettingsGroup.setText( getBaseMsg( "AvroDialog.File.FileSettingsTitle" ) );

    FormLayout layout = new FormLayout();
    layout.marginHeight = MARGIN;
    layout.marginWidth = MARGIN;
    wFileSettingsGroup.setLayout( layout );
    wFileSettingsGroup
      .setLayoutData( new FormDataBuilder().top( encodingLabel, 35 ).left( 0, MARGIN ).right( 100, -MARGIN ).result() );

    Label separator = new Label( wFileSettingsGroup, SWT.SEPARATOR | SWT.VERTICAL );
    separator.setLayoutData( new FormDataBuilder().left( 0, RADIO_BUTTON_WIDTH ).top().bottom().result() );

    wbGetDataFromFile = new Button( wFileSettingsGroup, SWT.RADIO );
    wbGetDataFromFile.setText( getBaseMsg( "AvroDialog.File.SpecifyFileName" ) );
    wbGetDataFromFile.setLayoutData( new FormDataBuilder().left().top().width( RADIO_BUTTON_WIDTH ).result() );

    wbGetDataFromField = new Button( wFileSettingsGroup, SWT.RADIO );
    wbGetDataFromField.setText( getBaseMsg( "AvroDialog.File.GetDataFromField" ) );
    wbGetDataFromField.setLayoutData( new FormDataBuilder().left().top( wbGetDataFromFile, FIELDS_SEP )
      .width( RADIO_BUTTON_WIDTH ).result() );

    //Make a composite to hold the dynamic right side of the group
    Composite wFileSettingsDynamicArea = new Composite( wFileSettingsGroup, SWT.NONE );
    FormLayout fileSettingsDynamicAreaLayout = new FormLayout();
    wFileSettingsDynamicArea.setLayout( fileSettingsDynamicAreaLayout );
    wFileSettingsDynamicArea.setLayoutData( new FormDataBuilder().right().left( wbGetDataFromFile, MARGIN )
      .top( 0, -MARGIN ).result() );

    //Put the File selection stuff in it
    wDataFileComposite = new Composite( wFileSettingsDynamicArea, SWT.NONE );
    wDataFileComposite.setLayout( new FormLayout() );
    wDataFileComposite.setLayoutData( new FormDataBuilder().left().right().top().result() );
    addFileWidgets( wDataFileComposite, wDataFileComposite );

    //Setup StreamingFieldName
    wDataFieldComposite = new Composite( wFileSettingsDynamicArea, SWT.NONE );
    FormLayout fieldNameLayout = new FormLayout();
    fieldNameLayout.marginHeight = MARGIN;
    wDataFieldComposite.setLayout( fieldNameLayout );
    wDataFieldComposite.setLayoutData( new FormDataBuilder().left().top().result() );

    Label fieldNameLabel = new Label( wDataFieldComposite, SWT.NONE );
    fieldNameLabel.setText( getBaseMsg( "AvroDialog.FieldName.Label" ) );
    fieldNameLabel.setLayoutData( new FormDataBuilder().left().top( wDataFieldComposite, 0 ).result() );
    wFieldNameCombo = new ComboVar( transMeta, wDataFieldComposite, SWT.LEFT | SWT.BORDER );
    updateIncomingFieldList( wFieldNameCombo );
    wFieldNameCombo.setLayoutData( new FormDataBuilder().left().top( fieldNameLabel ).width( FIELD_MEDIUM ).result() );

    //Setup the radio button event handler
    SelectionAdapter fileSettingRadioSelectionAdapter = new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent e ) {
        wDataFileComposite.setVisible( !wbGetDataFromField.getSelection() );
        wDataFieldComposite.setVisible( wbGetDataFromField.getSelection() );
      }
    };
    wbGetDataFromFile.addSelectionListener( fileSettingRadioSelectionAdapter );
    wbGetDataFromField.addSelectionListener( fileSettingRadioSelectionAdapter );

    //Set widgets from Meta
    wbGetDataFromFile
      .setSelection( meta.getDataLocationType() == AvroInputMetaBase.LocationDescriptor.FILE_NAME );
    wbGetDataFromField
      .setSelection( meta.getDataLocationType() != AvroInputMetaBase.LocationDescriptor.FILE_NAME );
    fileSettingRadioSelectionAdapter.widgetSelected( null );
    wFieldNameCombo.setText(
      meta.getDataLocationType() != AvroInputMetaBase.LocationDescriptor.FIELD_NAME ? ""
        : meta.getDataLocation() );

    // Set up the Source Group
    wSourceGroup = new Group( wTabComposite, SWT.SHADOW_NONE );
    wSourceGroup.setText( BaseMessages.getString( PKG, "AvroInputDialog.Schema.SourceTitle" ) );

    FormLayout schemaLayout = new FormLayout();
    schemaLayout.marginWidth = MARGIN;
    schemaLayout.marginHeight = MARGIN;
    wSourceGroup.setLayout( schemaLayout );
    wSourceGroup.setLayoutData(
      new FormDataBuilder().top( wFileSettingsGroup, 10 ).right( 100, -MARGIN ).left( 0, MARGIN ).result() );

    Label schemaSeparator = new Label( wSourceGroup, SWT.SEPARATOR | SWT.VERTICAL );
    schemaSeparator.setLayoutData( new FormDataBuilder().left( 0, RADIO_BUTTON_WIDTH ).top().bottom().result() );

    wbGetSchemaFromFile = new Button( wSourceGroup, SWT.RADIO );
    wbGetSchemaFromFile.setText( getBaseMsg( "AvroDialog.File.SpecifyFileName" ) );
    wbGetSchemaFromFile.setLayoutData( new FormDataBuilder().left().top().width( RADIO_BUTTON_WIDTH ).result() );

    wbGetSchemaFromField = new Button( wSourceGroup, SWT.RADIO );
    wbGetSchemaFromField.setText( getBaseMsg( "AvroDialog.File.GetDataFromField" ) );
    wbGetSchemaFromField.setLayoutData( new FormDataBuilder().left().top( wbGetSchemaFromFile, FIELDS_SEP )
      .width( RADIO_BUTTON_WIDTH ).result() );

    //Make a composite to hold the dynamic right side of the group
    Composite wSchemaSettingsDynamicArea = new Composite( wSourceGroup, SWT.NONE );
    FormLayout fileSettingsDynamicAreaSchemaLayout = new FormLayout();
    wSchemaSettingsDynamicArea.setLayout( fileSettingsDynamicAreaSchemaLayout );
    wSchemaSettingsDynamicArea.setLayoutData( new FormDataBuilder().right().left( wbGetSchemaFromFile, MARGIN )
      .top( 0, -MARGIN ).result() );

    //Put the File selection stuff in it
    wSchemaFileComposite = new Composite( wSchemaSettingsDynamicArea, SWT.NONE );
    FormLayout schemaFileLayout = new FormLayout();
    wSchemaFileComposite.setLayout( schemaFileLayout );
    wSchemaFileComposite.setLayoutData( new FormDataBuilder().left().right().top().result() );

    Label wlSchemaPath = new Label( wSchemaFileComposite, SWT.RIGHT );
    wlSchemaPath.setText( BaseMessages.getString( PKG, "AvroInputDialog.Schema.FileName" ) );
    wlSchemaPath.setLayoutData( new FormDataBuilder().left().top( 0, MARGIN ).result() );

    wbSchemaBrowse = new Button( wSchemaFileComposite, SWT.PUSH );
    wbSchemaBrowse.setText( BaseMessages.getString( "System.Button.Browse" ) );

    wbSchemaBrowse.setLayoutData( new FormDataBuilder().top( wlSchemaPath ).right().result() );

    wSchemaPath = new TextVar( transMeta, wSchemaFileComposite, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    wSchemaPath.setLayoutData( new FormDataBuilder().left().right( wbSchemaBrowse, -FIELD_LABEL_SEP )
      .top( wlSchemaPath ).result() );

    wbSchemaBrowse.addSelectionListener( new SelectionAdapterFileDialogTextVar(
      log, wSchemaPath, transMeta, new SelectionAdapterOptions( selectionOperation() ) ) );


    wSchemaFieldComposite = new Composite( wSchemaSettingsDynamicArea, SWT.NONE );
    FormLayout schemaFieldLayout = new FormLayout();
    wSchemaFieldComposite.setLayout( schemaFieldLayout );
    wSchemaFieldComposite.setLayoutData( new FormDataBuilder().left().right( 100, RADIO_BUTTON_WIDTH + MARGIN - 15 )
      .top().result() );

    Label fieldNameSchemaLabel = new Label( wSchemaFieldComposite, SWT.NONE );
    fieldNameSchemaLabel.setText( getBaseMsg( "AvroDialog.FieldName.Label" ) );
    fieldNameSchemaLabel.setLayoutData( new FormDataBuilder().left().top( 0, MARGIN ).result() );
    wSchemaFieldNameCombo = new ComboVar( transMeta, wSchemaFieldComposite, SWT.LEFT | SWT.BORDER );
    updateIncomingFieldList( wSchemaFieldNameCombo );
    wSchemaFieldNameCombo.setLayoutData( new FormDataBuilder().left().top( fieldNameSchemaLabel )
      .width( FIELD_MEDIUM ).result() );

    //Setup the radio button event handler
    SelectionAdapter fileSettingRadioSelectionSchemaAdapter = new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent e ) {
        wSchemaFileComposite.setVisible( !wbGetSchemaFromField.getSelection() );
        wSchemaFieldComposite.setVisible( wbGetSchemaFromField.getSelection() );
      }
    };
    wbGetSchemaFromFile.addSelectionListener( fileSettingRadioSelectionSchemaAdapter );
    wbGetSchemaFromField.addSelectionListener( fileSettingRadioSelectionSchemaAdapter );

    wbGetSchemaFromFile.setSelection( true );
    wbGetSchemaFromField.setSelection( false );
    wSchemaFileComposite.setVisible( true );
    wSchemaFieldComposite.setVisible( false );
    wSourceGroup.setVisible( false );
  }

  /**
   * Read the data from the meta object and show it in this dialog.
   */
  @Override
  protected void getData() {
    AvroInputMeta avroInputMeta = (AvroInputMeta) getStepMeta();

    wPath.setText( "" );
    wFieldNameCombo.setText( "" );
    wSchemaPath.setText( "" );
    wbGetDataFromFile.setSelection( true );
    wbGetDataFromField.setSelection( false );
    wDataFileComposite.setVisible( true );
    wDataFieldComposite.setVisible( false );
    encodingCombo.select( avroInputMeta.getFormat() );

    wbGetSchemaFromFile.setSelection( true );
    wbGetSchemaFromField.setSelection( false );
    wSchemaFileComposite.setVisible( true );
    wSchemaFieldComposite.setVisible( false );
    wSourceGroup.setVisible( avroInputMeta.getFormat() > 0 );

    if ( avroInputMeta.getDataLocation() != null ) {
      if ( ( avroInputMeta.getDataLocationType() == AvroInputMetaBase.LocationDescriptor.FILE_NAME ) ) {
        wPath.setText( avroInputMeta.getDataLocation() );
      } else if ( ( avroInputMeta.getDataLocationType() == AvroInputMetaBase.LocationDescriptor.FIELD_NAME ) ) {
        wFieldNameCombo.setText( avroInputMeta.getDataLocation() );
        wbGetDataFromFile.setSelection( false );
        wbGetDataFromField.setSelection( true );
        wDataFileComposite.setVisible( false );
        wDataFieldComposite.setVisible( true );
      }
    }

    if ( avroInputMeta.getSchemaLocation() != null ) {
      if ( ( avroInputMeta.getSchemaLocationType() == AvroInputMetaBase.LocationDescriptor.FILE_NAME ) ) {
        wSchemaPath.setText( avroInputMeta.getSchemaLocation() );
      } else if ( ( avroInputMeta.getSchemaLocationType() == AvroInputMetaBase.LocationDescriptor.FIELD_NAME ) ) {
        wSchemaFieldNameCombo.setText( avroInputMeta.getSchemaLocation() );
        wbGetSchemaFromFile.setSelection( false );
        wbGetSchemaFromField.setSelection( true );
        wSchemaFileComposite.setVisible( false );
        wSchemaFieldComposite.setVisible( true );
      }
    }

    wPassThruFields.setSelection( avroInputMeta.passingThruFields );
    wAllowNullValues.setSelection( avroInputMeta.isAllowNullForMissingFields() );

    int itemIndex = 0;
    for ( AvroInputField inputField : avroInputMeta.getInputFields() ) {
      TableItem item;
      if ( itemIndex < wInputFields.table.getItemCount() ) {
        item = wInputFields.table.getItem( itemIndex );
      } else {
        item = new TableItem( wInputFields.table, SWT.NONE );
      }

      if ( inputField.getAvroFieldName() != null ) {
        item.setText( AVRO_ORIGINAL_PATH_COLUMN_INDEX, inputField.getDisplayableAvroFieldName() );
        item.setText( AVRO_DISPLAY_PATH_COLUMN_INDEX,
          clearIndexFromFieldName( inputField.getDisplayableAvroFieldName() ) );
      }
      if ( inputField.getAvroType() != null ) {
        item.setText( AVRO_TYPE_COLUMN_INDEX, inputField.getAvroType().getName() );
      }
      if ( inputField.getIndexedValues() != null ) {
        item.setText( AVRO_INDEXED_VALUES_COLUMN_INDEX, inputField.getIndexedValues() );
      }
      if ( inputField.getPentahoFieldName() != null ) {
        item.setText( FIELD_NAME_COLUMN_INDEX, inputField.getPentahoFieldName() );
      }
      if ( inputField.getTypeDesc() != null ) {
        item.setText( FIELD_TYPE_COLUMN_INDEX, inputField.getTypeDesc() );
      }
      if ( inputField.getStringFormat() != null ) {
        item.setText( FORMAT_COLUMN_INDEX, inputField.getStringFormat() );
      } else {
        item.setText( FORMAT_COLUMN_INDEX, "" );
      }
      itemIndex++;
    }

    setVariableTableFields( avroInputMeta.getLookupFields() );
  }

  protected void setVariableTableFields( List<AvroLookupField> fields ) {
    wLookupView.clearAll();

    for ( AvroLookupField f : fields ) {
      TableItem item = new TableItem( wLookupView.table, SWT.NONE );

      if ( !Utils.isEmpty( f.fieldName ) ) {
        item.setText( 1, f.fieldName );
      }

      if ( !Utils.isEmpty( f.variableName ) ) {
        item.setText( 2, f.variableName );
      }

      if ( !Utils.isEmpty( f.defaultValue ) ) {
        item.setText( 3, f.defaultValue );
      }
    }

    wLookupView.removeEmptyRows();
    wLookupView.setRowNums();
    wLookupView.optWidth( true );
  }


  /**
   * Fill meta object from UI options.
   */
  @Override
  protected void getInfo( boolean preview ) {

    if ( wbGetDataFromField.getSelection() ) {
      meta.setDataLocation( wFieldNameCombo.getText(), AvroInputMetaBase.LocationDescriptor.FIELD_NAME );
    } else {
      meta.setDataLocation( wPath.getText(), AvroInputMetaBase.LocationDescriptor.FILE_NAME );
    }

    if ( wbGetSchemaFromField.getSelection() ) {
      meta.setSchemaLocation( wSchemaFieldNameCombo.getText(), AvroInputMetaBase.LocationDescriptor.FIELD_NAME );
    } else {
      meta.setSchemaLocation( wSchemaPath.getText(), AvroInputMetaBase.LocationDescriptor.FILE_NAME );
      meta.setCacheSchemas( false );
    }

    meta.passingThruFields = wPassThruFields.getSelection();
    meta.setAllowNullForMissingFields( wAllowNullValues.getSelection() );
    meta.setFormat( encodingCombo.getSelectionIndex() );

    int nrFields = wInputFields.nrNonEmpty();
    meta.inputFields = new AvroInputField[ nrFields ];
    for ( int i = 0; i < nrFields; i++ ) {
      TableItem item = wInputFields.getNonEmpty( i );
      AvroInputField field = new AvroInputField();
      String formatFieldName = extractFieldName( item.getText( AVRO_ORIGINAL_PATH_COLUMN_INDEX ) );
      formatFieldName = fixPath( formatFieldName, item );
      field.setFormatFieldName( formatFieldName );
      field.setAvroType( item.getText( AVRO_TYPE_COLUMN_INDEX ) );
      field.setIndexedValues( item.getText( AVRO_INDEXED_VALUES_COLUMN_INDEX ) );
      field.setPentahoFieldName( item.getText( FIELD_NAME_COLUMN_INDEX ) );
      field.setPentahoType( ValueMetaFactory.getIdForValueMeta( item.getText( FIELD_TYPE_COLUMN_INDEX ) ) );
      field.setStringFormat( item.getText( FORMAT_COLUMN_INDEX ) );
      meta.inputFields[ i ] = field;
    }

    nrFields = wLookupView.nrNonEmpty();
    if ( nrFields > 0 ) {
      List<AvroLookupField> varFields = new ArrayList<>();

      for ( int i = 0; i < nrFields; i++ ) {
        TableItem item = wLookupView.getNonEmpty( i );
        AvroLookupField newField = new AvroLookupField();
        boolean add = false;

        newField.fieldName = item.getText( 1 ).trim();
        if ( !Utils.isEmpty( item.getText( 2 ) ) ) {
          newField.variableName = item.getText( 2 ).trim();
          add = true;
          if ( !Utils.isEmpty( item.getText( 3 ) ) ) {
            newField.defaultValue = item.getText( 3 ).trim();
          }
        }

        if ( add ) {
          varFields.add( newField );
        }
      }
      meta.setLookupFields( varFields );
    }

  }

  private String fixPath( String formatFieldName, TableItem item ) {
    String value = formatFieldName;
    Pattern p = Pattern.compile( "\\[(.*?)\\]" );
    Matcher m = p.matcher( value );
    while ( m.find() ) {
      if ( m.end() - m.start() < 3 ) {
        value = new StringBuilder( value ).insert( m.start() + 1, item.getText( AVRO_INDEXED_VALUES_COLUMN_INDEX ) )
          .toString();
      } else {
        value = value.replace( m.group( 1 ), item.getText( AVRO_INDEXED_VALUES_COLUMN_INDEX ) );
      }
    }
    return value;
  }

  private String extractFieldName( String parquetNameTypeFromUI ) {
    if ( ( parquetNameTypeFromUI != null ) && ( parquetNameTypeFromUI.indexOf( '(' ) >= 0 ) ) {
      return StringUtils.substringBefore( parquetNameTypeFromUI, "(" ).trim();
    }
    return parquetNameTypeFromUI;
  }

  private void doPreview() {
    getInfo( true );
    TransMeta previewMeta = TransPreviewFactory.generatePreviewTransformation( transMeta, meta, wStepname.getText() );
    transMeta.getVariable( "Internal.Transformation.Filename.Directory" );
    previewMeta.getVariable( "Internal.Transformation.Filename.Directory" );

    EnterNumberDialog numberDialog =
      new EnterNumberDialog( shell, props.getDefaultPreviewSize(), BaseMessages.getString( PKG,
        "AvroInputDialog.PreviewSize.DialogTitle" ), BaseMessages.getString( PKG,
        "AvroInputDialog.PreviewSize.DialogMessage" ) );
    int previewSize = numberDialog.open();

    if ( previewSize > 0 ) {
      TransPreviewProgressDialog progressDialog =
        new TransPreviewProgressDialog( shell, previewMeta, new String[] { wStepname.getText() }, new int[] {
          previewSize } );
      progressDialog.open();

      Trans trans = progressDialog.getTrans();
      String loggingText = progressDialog.getLoggingText();

      if ( !progressDialog.isCancelled() && trans.getResult() != null && trans.getResult().getNrErrors() > 0 ) {
        EnterTextDialog etd =
          new EnterTextDialog( shell, BaseMessages.getString( PKG, "System.Dialog.PreviewError.Title" ),
            BaseMessages.getString( PKG, "System.Dialog.PreviewError.Message" ), loggingText, true );
        etd.setReadOnly();
        etd.open();
      }

      PreviewRowsDialog prd =
        new PreviewRowsDialog( shell, transMeta, SWT.NONE, wStepname.getText(), progressDialog.getPreviewRowsMeta(
          wStepname.getText() ), progressDialog.getPreviewRows( wStepname.getText() ), loggingText );
      prd.open();
    }
  }

  private String clearIndexFromFieldName( String fieldName ) {
    String cleanFieldName = fieldName;
    int bracketPos = cleanFieldName.indexOf( '[' );
    if ( bracketPos > -1 ) {
      int closeBracketPos = cleanFieldName.indexOf( ']' );
      cleanFieldName = cleanFieldName.substring( 0, bracketPos + 1 ) + cleanFieldName.substring( closeBracketPos );
    }

    return cleanFieldName;
  }

  @Override
  public String open() {
    Shell parent = getParent();
    Display display = parent.getDisplay();

    shell = new Shell( parent, SWT.DIALOG_TRIM | SWT.RESIZE );
    setShellImage( shell, meta );

    lsMod = e -> meta.setChanged();
    changed = meta.hasChanged();

    createUI();
    props.setLook( shell );

    // Detect X or ALT-F4 or something that kills this window...
    shell.addShellListener( new ShellAdapter() {
      @Override
      public void shellClosed( ShellEvent e ) {
        cancel();
      }
    } );

    int height = Math.max( getMinHeight( shell, getWidth() ), getHeight() );
    shell.setMinimumSize( getWidth(), height );
    shell.setSize( getWidth(), height );
    getData();
    shell.open();
    wStepname.setFocus();
    while ( !shell.isDisposed() ) {
      if ( !display.readAndDispatch() ) {
        display.sleep();
      }
    }
    return stepname;
  }

  @Override
  protected int getWidth() {
    return SHELL_WIDTH;
  }

  @Override
  protected int getHeight() {
    return SHELL_HEIGHT;
  }

  @Override
  protected String getStepTitle() {
    return BaseMessages.getString( PKG, "AvroInputDialog.Shell.Title" );
  }

  @Override
  protected Listener getPreview() {
    return e -> doPreview();
  }

  @Override protected SelectionOperation selectionOperation() {
    return SelectionOperation.FILE_OR_FOLDER;
  }
}
