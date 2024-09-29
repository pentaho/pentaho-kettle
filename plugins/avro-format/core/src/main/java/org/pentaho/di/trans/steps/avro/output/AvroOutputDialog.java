/*******************************************************************************
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

package org.pentaho.di.trans.steps.avro.output;

import org.pentaho.di.trans.steps.avro.AvroSpec;
import org.pentaho.di.trans.steps.avro.AvroTypeConverter;
import org.pentaho.di.trans.steps.avro.BaseAvroStepDialog;
import org.pentaho.di.trans.steps.avro.NullableValuesEnum;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.annotations.PluginDialog;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.ui.core.FormDataBuilder;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.events.dialog.SelectionAdapterFileDialogTextVar;
import org.pentaho.di.ui.core.events.dialog.SelectionAdapterOptions;
import org.pentaho.di.ui.core.events.dialog.SelectionOperation;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.ColumnsResizer;
import org.pentaho.di.ui.core.widget.ComboVar;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.trans.step.TableItemInsertListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;

@PluginDialog( id = "AvroOutput", image = "AO.svg", pluginType = PluginDialog.PluginType.STEP,
        documentationUrl = "mk-95pdia003/pdi-transformation-steps/avro-output" )
public class AvroOutputDialog extends BaseAvroStepDialog implements StepDialogInterface {

  private static final Class<?> PKG = AvroOutputMeta.class;

  private static final int SHELL_WIDTH = 698;
  private static final int SHELL_HEIGHT = 554;
  private static final int H_OFFSET_DATETIME_COMBO_BOX = 15;
  private static final String[] SUPPORTED_AVRO_TYPE_NAMES = {
    AvroSpec.DataType.STRING.getName(),
    AvroSpec.DataType.INTEGER.getName(),
    AvroSpec.DataType.LONG.getName(),
    AvroSpec.DataType.FLOAT.getName(),
    AvroSpec.DataType.DOUBLE.getName(),
    AvroSpec.DataType.BOOLEAN.getName(),
    AvroSpec.DataType.DECIMAL.getName(),
    AvroSpec.DataType.DATE.getName(),
    AvroSpec.DataType.TIMESTAMP_MILLIS.getName(),
    AvroSpec.DataType.BYTES.getName()
  };

  private TableView wOutputFields;

  protected ComboVar wCompression;
  protected TextVar wRecordName;
  protected TextVar wDocValue;
  protected TextVar wNameSpace;
  protected TextVar wSchemaPath;
  protected Button wbSchemaBrowse;
  private Button wOverwriteExistingFile;
  private Button wDateInFileName;
  private Button wTimeInFileName;
  private Button wSpecifyDateTimeFormat;
  private ComboVar wDateTimeFormat;
  private AvroOutputMeta meta;

  public AvroOutputDialog( Shell parent, Object avroOutputMeta, TransMeta transMeta, String sname ) {
    this( parent, (AvroOutputMeta) avroOutputMeta, transMeta, sname );
    this.meta = (AvroOutputMeta) avroOutputMeta;
  }

  public AvroOutputDialog( Shell parent, AvroOutputMeta avroOutputMeta, TransMeta transMeta, String sname ) {
    super( parent, avroOutputMeta, transMeta, sname );
    this.meta = avroOutputMeta;
  }

  // TODO name
  protected Control createAfterFile( Composite afterFile ) {
    wOverwriteExistingFile = new Button( afterFile, SWT.CHECK );
    wOverwriteExistingFile.setText( BaseMessages.getString( PKG, "AvroOutputDialog.OverwriteFile.Label" ) );
    wOverwriteExistingFile.setLayoutData( new FormDataBuilder().left().top( afterFile, FIELDS_SEP ).result() );
    wOverwriteExistingFile.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent e ) {
        meta.setChanged();
      }
    } );

    CTabFolder wTabFolder = new CTabFolder( afterFile, SWT.BORDER );
    wTabFolder.setSimple( false );

    addFieldsTab( wTabFolder );
    addSchemaTab( wTabFolder );
    addOptionsTab( wTabFolder );

    wTabFolder.setLayoutData( new FormDataBuilder().left().top( wOverwriteExistingFile, MARGIN ).right().bottom()
      .result() );
    wTabFolder.setSelection( 0 );
    return wTabFolder;
  }

  @Override
  protected String getStepTitle() {
    return BaseMessages.getString( PKG, "AvroOutputDialog.Shell.Title" );
  }

  private void addFieldsTab( CTabFolder wTabFolder ) {
    CTabItem wTab = new CTabItem( wTabFolder, SWT.NONE );
    wTab.setText( BaseMessages.getString( PKG, "AvroOutputDialog.FieldsTab.TabTitle" ) );

    Composite wComp = new Composite( wTabFolder, SWT.NONE );

    FormLayout layout = new FormLayout();
    layout.marginWidth = MARGIN;
    layout.marginHeight = MARGIN;
    wComp.setLayout( layout );

    lsGet = e -> getFields();

    Button wGetFields = new Button( wComp, SWT.PUSH );
    wGetFields.setText( BaseMessages.getString( PKG, "AvroOutputDialog.Fields.Get" ) );
    wGetFields.setLayoutData( new FormDataBuilder().bottom().right().result() );

    wGetFields.addListener( SWT.Selection, lsGet );

    ColumnInfo[] parameterColumns = new ColumnInfo[] {
      new ColumnInfo( BaseMessages.getString( PKG, "AvroOutputDialog.Fields.column.Path" ),
          ColumnInfo.COLUMN_TYPE_TEXT, false, false ),
      new ColumnInfo( BaseMessages.getString( PKG, "AvroOutputDialog.Fields.column.Name" ),
          ColumnInfo.COLUMN_TYPE_TEXT, false, false ),
      new ColumnInfo( BaseMessages.getString( PKG, "AvroOutputDialog.Fields.column.Type" ),
          ColumnInfo.COLUMN_TYPE_CCOMBO, SUPPORTED_AVRO_TYPE_NAMES ),
      new ColumnInfo( BaseMessages.getString( PKG, "AvroOutputDialog.Fields.column.Precision" ),
          ColumnInfo.COLUMN_TYPE_TEXT, false, false ),
      new ColumnInfo( BaseMessages.getString( PKG, "AvroOutputDialog.Fields.column.Scale" ),
          ColumnInfo.COLUMN_TYPE_TEXT, false, false ),
      new ColumnInfo( BaseMessages.getString( PKG, "AvroOutputDialog.Fields.column.Default" ),
          ColumnInfo.COLUMN_TYPE_TEXT, false, false ),
      new ColumnInfo( BaseMessages.getString( PKG, "AvroOutputDialog.Fields.column.Null" ),
          ColumnInfo.COLUMN_TYPE_CCOMBO, NullableValuesEnum.getValuesArr(), true ) };

    parameterColumns[0].setAutoResize( false );
    parameterColumns[1].setUsingVariables( true );
    wOutputFields =
        new TableView( transMeta, wComp, SWT.FULL_SELECTION | SWT.SINGLE | SWT.BORDER | SWT.NO_SCROLL | SWT.V_SCROLL,
            parameterColumns, 7, lsMod, props );
    ColumnsResizer resizer = new ColumnsResizer( 0, 30, 20, 10, 10, 10, 15, 5 );
    wOutputFields.getTable().addListener( SWT.Resize, resizer );

    wOutputFields.setLayoutData( new FormDataBuilder().left().right().top( wComp, 0 ).bottom( wGetFields, -FIELDS_SEP )
      .result() );

    wOutputFields.setRowNums();
    wOutputFields.optWidth( true );

    wComp.setLayoutData( new FormDataBuilder().left().top().right().bottom().result() );

    wTab.setControl( wComp );
    for ( ColumnInfo col : parameterColumns ) {
      col.setAutoResize( false );
    }
    resizer.addColumnResizeListeners( wOutputFields.getTable() );
    setTruncatedColumn( wOutputFields.getTable(), 1 );
    if ( !Const.isWindows() ) {
      addColumnTooltip( wOutputFields.getTable(), 1 );
    }
  }

  private void addSchemaTab( CTabFolder wTabFolder ) {
    // Create & Set up a new Tab Item
    CTabItem wTab = new CTabItem( wTabFolder, SWT.NONE );
    wTab.setText( BaseMessages.getString( PKG, "AvroOutputDialog.Schema.TabTitle" ) );
    Composite wTabComposite = new Composite( wTabFolder, SWT.NONE );
    wTab.setControl( wTabComposite );
    FormLayout formLayout = new FormLayout();
    formLayout.marginHeight = MARGIN;
    wTabComposite.setLayout( formLayout );

    // Set up the Source Group
    Group wSourceGroup = new Group( wTabComposite, SWT.SHADOW_NONE );
    wSourceGroup.setText( BaseMessages.getString( PKG, "AvroOutputDialog.Schema.SourceTitle" ) );

    FormLayout layout = new FormLayout();
    layout.marginWidth = MARGIN;
    wSourceGroup.setLayout( layout );

    FormData fdSource = new FormData();
    fdSource.top = new FormAttachment( 0, 0 );
    fdSource.right = new FormAttachment( 100, -15 );
    fdSource.left = new FormAttachment( 0, 15 );

    wSourceGroup.setLayoutData( fdSource );

    Label wlSchemaPath = new Label( wSourceGroup, SWT.RIGHT );
    wlSchemaPath.setText( BaseMessages.getString( PKG, "AvroOutputDialog.Schema.FileName" ) );
    wlSchemaPath.setLayoutData( new FormDataBuilder().left().top().result() );
    wSchemaPath = new TextVar( transMeta, wSourceGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    wSchemaPath.setLayoutData( new FormDataBuilder().left().top( wlSchemaPath, FIELD_LABEL_SEP )
      .width( FIELD_LARGE + VAR_EXTRA_WIDTH ).result() );

    wbSchemaBrowse = new Button( wSourceGroup, SWT.PUSH );
    wbSchemaBrowse.setText( BaseMessages.getString( "System.Button.Browse" ) );

    wbSchemaBrowse.addSelectionListener( new SelectionAdapterFileDialogTextVar(
      log, wSchemaPath, transMeta, new SelectionAdapterOptions( SelectionOperation.SAVE ) ) );
    int bOffset = ( wbSchemaBrowse.computeSize( SWT.DEFAULT, SWT.DEFAULT, false ).y - wSchemaPath.
        computeSize( SWT.DEFAULT, SWT.DEFAULT, false ).y ) / 2;
    wbSchemaBrowse.setLayoutData( new FormDataBuilder().left( wSchemaPath, FIELD_LABEL_SEP ).top( wlSchemaPath, FIELD_LABEL_SEP - bOffset ).result() );

    // Set up Avro Details Group
    Group wAvroDetailsGroup = new Group( wTabComposite, SWT.SHADOW_NONE );
    wAvroDetailsGroup.setText( BaseMessages.getString( PKG, "AvroOutputDialog.Schema.AvroDetailsTitle" ) );

    layout = new FormLayout();
    layout.marginWidth = MARGIN;
    layout.marginHeight = MARGIN;
    wSourceGroup.setLayout( layout );

    wAvroDetailsGroup.setLayout( layout );

    FormData fdAvroDetailsGroup = new FormData();
    fdAvroDetailsGroup.top = new FormAttachment( wSourceGroup, 15 );
    fdAvroDetailsGroup.right = new FormAttachment( 100, -15 );
    fdAvroDetailsGroup.left = new FormAttachment( 0, 15 );

    wAvroDetailsGroup.setLayoutData( fdAvroDetailsGroup );
    // Set up the Namespace Text Box
    Label wlNameSpace = new Label( wAvroDetailsGroup, SWT.RIGHT );
    wlNameSpace.setText( BaseMessages.getString( PKG, "AvroOutputDialog.Schema.Namespace" ) );
    wlNameSpace.setLayoutData( new FormDataBuilder().left().top().result() );
    wNameSpace = new TextVar( transMeta, wAvroDetailsGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    wNameSpace.setLayoutData( new FormDataBuilder().left().top( wlNameSpace ).width( 250 ).result() );
    wNameSpace.setText( BaseMessages.getString( PKG, "AvroOutputDialog.Schema.RequiredItem" ) );

    // Set up the RecordName Text Box
    Label wlRecordName = new Label( wAvroDetailsGroup, SWT.RIGHT );
    wlRecordName.setText( BaseMessages.getString( PKG, "AvroOutputDialog.Schema.RecordName" ) );
    wlRecordName.setLayoutData( new FormDataBuilder().left().top( wNameSpace, FIELDS_SEP ).result() );
    wRecordName = new TextVar( transMeta, wAvroDetailsGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    wRecordName.setLayoutData( new FormDataBuilder().left().top( wlRecordName ).width( 250 ).result() );
    wRecordName.setText( BaseMessages.getString( PKG, "AvroOutputDialog.Schema.RequiredItem" ) );

    // Set up the DocValue Text Box
    Label wlDocValue = new Label( wAvroDetailsGroup, SWT.RIGHT );
    wlDocValue.setText( BaseMessages.getString( PKG, "AvroOutputDialog.Schema.DocValue" ) );
    wlDocValue.setLayoutData( new FormDataBuilder().left().top( wRecordName, FIELDS_SEP ).result() );
    wDocValue = new TextVar( transMeta, wAvroDetailsGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    wDocValue.setLayoutData( new FormDataBuilder().left().top( wlDocValue ).width( 250 ).result() );
  }

  private void addOptionsTab( CTabFolder wTabFolder ) {
    CTabItem wTab = new CTabItem( wTabFolder, SWT.NONE );
    wTab.setText( BaseMessages.getString( PKG, "AvroOutputDialog.Options.TabTitle" ) );
    Composite wComp = new Composite( wTabFolder, SWT.NONE );
    wTab.setControl( wComp );
    FormLayout formLayout = new FormLayout();
    formLayout.marginHeight = formLayout.marginWidth = MARGIN;
    wComp.setLayout( formLayout );

    Label lCompression = createLabel( wComp, "AvroOutputDialog.Options.Compression" );
    lCompression.setLayoutData( new FormDataBuilder().left().top( wComp, 0 ).result() );
    wCompression = createComboVar( wComp, meta.getCompressionTypes() );
    wCompression.setLayoutData( new FormDataBuilder().left().top( lCompression, FIELD_LABEL_SEP )
      .width( FIELD_SMALL + VAR_EXTRA_WIDTH ).result() );

    wDateInFileName = new Button( wComp, SWT.CHECK );
    wDateInFileName.setLayoutData( new FormDataBuilder().left().top( wCompression, FIELDS_SEP ).result() );
    wDateInFileName.setText( BaseMessages.getString( PKG, "AvroOutputDialog.Options.DateInFileName" ) );
    wDateInFileName.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent e ) {
        meta.setChanged();
        boolean isSelected = wDateInFileName.getSelection();
        if ( isSelected ) {
          wSpecifyDateTimeFormat.setSelection( false );
          wDateTimeFormat.setText( "" );
          wDateTimeFormat.setEnabled( false );
        }
      }
    } );


    wTimeInFileName = new Button( wComp, SWT.CHECK );
    wTimeInFileName.setLayoutData( new FormDataBuilder().left().top( wDateInFileName, FIELDS_SEP ).result() );
    wTimeInFileName.setText( BaseMessages.getString( PKG, "AvroOutputDialog.Options.TimeInFileName" ) );
    wTimeInFileName.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent e ) {
        meta.setChanged();
        boolean isSelected = wTimeInFileName.getSelection();
        if ( isSelected ) {
          wSpecifyDateTimeFormat.setSelection( false );
          wDateTimeFormat.setText( "" );
          wDateTimeFormat.setEnabled( false );
        }
      }
    } );


    wSpecifyDateTimeFormat = new Button( wComp, SWT.CHECK );
    wSpecifyDateTimeFormat.setLayoutData( new FormDataBuilder().left().top( wTimeInFileName, FIELDS_SEP ).result() );
    wSpecifyDateTimeFormat.setText( BaseMessages.getString( PKG, "AvroOutputDialog.Options.SpecifyDateTimeFormat" ) );
    wSpecifyDateTimeFormat.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent e ) {
        meta.setChanged();
        boolean isSelected = wSpecifyDateTimeFormat.getSelection();
        wDateTimeFormat.setEnabled( isSelected );
        if ( !isSelected ) {
          wDateTimeFormat.setText( "" );
          wTimeInFileName.setEnabled( true );
          wDateInFileName.setEnabled( true );
        } else {
          wTimeInFileName.setSelection( false );
          wDateInFileName.setSelection( false );
          wTimeInFileName.setEnabled( false );
          wDateInFileName.setEnabled( false );
        }
      }
    } );

    String[] dates = Const.getDateFormats();
    dates =
        Arrays.stream( dates ).filter( d -> d.indexOf( '/' ) < 0 && d.indexOf( '\\' ) < 0 && d.indexOf( ':' ) < 0 )
            .toArray( String[]::new ); // remove formats with slashes and colons
    wDateTimeFormat = createComboVar( wComp, dates );
    wDateTimeFormat.setLayoutData( new FormDataBuilder().left( 0, H_OFFSET_DATETIME_COMBO_BOX )
      .top( wSpecifyDateTimeFormat ).width( 200 ).result() );
  }

  protected ComboVar createComboVar( Composite container, String[] options ) {
    ComboVar combo = new ComboVar( transMeta, container, SWT.LEFT | SWT.BORDER );
    combo.setItems( options );
    combo.addModifyListener( lsMod );
    return combo;
  }

  private Label createLabel( Composite container, String labelRef ) {
    Label label = new Label( container, SWT.NONE );
    label.setText( BaseMessages.getString( PKG, labelRef ) );
    return label;
  }

  @Override
  protected void ok() {
    if ( Utils.isEmpty( wStepname.getText() ) ) {
      return;
    }
    stepname = wStepname.getText();

    List<String> validationErrorFields = validateOutputFields( wOutputFields );

    if ( validationErrorFields != null && !validationErrorFields.isEmpty() ) {
      MessageBox mb = new MessageBox( shell, SWT.OK | SWT.ICON_ERROR );
      mb.setText( BaseMessages.getString( PKG, "AvroOutput.MissingDefaultFields.Title" ) );
      mb.setMessage( BaseMessages.getString( PKG, "AvroOutput.MissingDefaultFields.Msg" ) );
      mb.open();
      return;
    }

    getInfo( false );
    dispose();
  }

  /**
   * Read the data from the meta object and show it in this dialog.
   */
  protected void getData(  ) {
    wOverwriteExistingFile.setSelection( meta.isOverrideOutput() );

    if ( meta.getFilename() != null ) {
      wPath.setText( meta.getFilename() );
    }
    if ( meta.getSchemaFilename() != null ) {
      wSchemaPath.setText( meta.getSchemaFilename() );
    }
    if ( meta.getDocValue() != null ) {
      wDocValue.setText( meta.getDocValue() );
    }
    if ( meta.getNamespace() != null ) {
      wNameSpace.setText( meta.getNamespace() );
    }
    if ( meta.getRecordName() != null ) {
      wRecordName.setText( meta.getRecordName() );
    }
    populateFieldsUI( meta, wOutputFields );
    wCompression.setText( meta.getCompressionType() );
    String dateTimeFormat = coalesce( meta.getDateTimeFormat() );
    if ( !dateTimeFormat.isEmpty() ) {
      wTimeInFileName.setSelection( false );
      wDateInFileName.setSelection( false );
      wTimeInFileName.setEnabled( false );
      wDateInFileName.setEnabled( false );
      wSpecifyDateTimeFormat.setSelection( true );
      wDateTimeFormat.setText( dateTimeFormat );
      wDateTimeFormat.setEnabled( true );
    } else {
      wTimeInFileName.setEnabled( true );
      wDateInFileName.setEnabled( true );
      wTimeInFileName.setSelection( meta.isTimeInFileName() );
      wDateInFileName.setSelection( meta.isDateInFileName() );
      wSpecifyDateTimeFormat.setSelection( false );
      wDateTimeFormat.setEnabled( false );
      wDateTimeFormat.setText( "" );
    }

  }

  // ui -> meta
  @Override
  protected void getInfo( boolean preview ) {
    meta.setOverrideOutput( wOverwriteExistingFile.getSelection() );
    meta.setFilename( wPath.getText() );
    meta.setDocValue( wDocValue.getText() );
    meta.setNamespace( wNameSpace.getText() );
    meta.setRecordName( wRecordName.getText() );
    meta.setSchemaFilename( wSchemaPath.getText() );
    meta.setCompressionType( wCompression.getText() );

    int nrFields = wOutputFields.nrNonEmpty();

    List<AvroOutputField> outputFields = new ArrayList<>();
    for ( int i = 0; i < nrFields; i++ ) {
      TableItem item = wOutputFields.getNonEmpty( i );

      int j = 1;
      AvroOutputField field = new AvroOutputField();
      field.setFormatFieldName( item.getText( j++ ) );
      field.setPentahoFieldName( item.getText( j++ ) );
      field.setFormatType( item.getText( j++ ) );

      if ( field.getAvroType().equals( AvroSpec.DataType.DECIMAL ) ) {
        field.setPrecision( item.getText( j++ ) );
        field.setScale( item.getText( j++ ) );
      } else if ( field.getAvroType().equals( AvroSpec.DataType.FLOAT ) || field.getAvroType().equals( AvroSpec.DataType.DOUBLE ) ) {
        j++;
        field.setScale( item.getText( j++ ) );
      } else {
        j += 2;
      }

      field.setDefaultValue( item.getText( j++ ) );
      field.setAllowNull( getNullableValue( item.getText( j++ ) ) );

      outputFields.add( field );
    }
    if ( wSpecifyDateTimeFormat.getSelection() ) {
      meta.setTimeInFileName( false );
      meta.setDateInFileName( false );
      meta.setDateTimeFormat( wDateTimeFormat.getText().trim() );
    } else {
      meta.setTimeInFileName( wTimeInFileName.getSelection() );
      meta.setDateInFileName( wDateInFileName.getSelection() );
      meta.setDateTimeFormat( "" );
    }
    meta.setOutputFields( outputFields );
  }

  private List<String> validateOutputFields( TableView wFields ) {
    int nrFields = wFields.nrNonEmpty();
    List<String> validationErrorFields = new ArrayList<>();

    for ( int i = 0; i < nrFields; i++ ) {
      TableItem item = wFields.getNonEmpty( i );

      int j = 1;

      String path = item.getText( j++ );
      String name = item.getText( 2 );
      String type = item.getText( j++ );
      String precision = item.getText( j++ );
      if ( precision == null || precision.trim().isEmpty() ) {
        item.setText( 4, Integer.toString( AvroSpec.DEFAULT_DECIMAL_PRECISION ) );
      }

      String scale = item.getText( j++ );
      if ( scale == null || scale.trim().isEmpty() ) {
        item.setText( 5, Integer.toString( AvroSpec.DEFAULT_DECIMAL_SCALE ) );
      }

      String defaultValue = item.getText( j++ );
      String nullString = getNullableValue( item.getText( j++ ) );

      if ( nullString.equals( NullableValuesEnum.NO.getValue() ) && ( defaultValue == null || defaultValue.trim().isEmpty() ) ) {
        validationErrorFields.add( name );
      }
    }
    return validationErrorFields;
  }

  private String getNullableValue( String nullString ) {
    return ( nullString != null && !nullString.isEmpty() ) ? nullString : NullableValuesEnum.getDefaultValue().getValue();
  }

  private void populateFieldsUI( AvroOutputMeta meta, TableView wOutputFields ) {
    populateFieldsUI( meta.getOutputFields(), wOutputFields, ( field, item ) -> {
      int i = 1;
      item.setText( i++, coalesce( field.getFormatFieldName() ) );
      item.setText( i++, coalesce( field.getPentahoFieldName() ) );
      item.setText( i++, coalesce( field.getAvroType().getName() ) );
      if ( field.getAvroType().equals( AvroSpec.DataType.DECIMAL ) ) {
        item.setText( i++, coalesce( String.valueOf( field.getPrecision() ) ) );
        item.setText( i++, coalesce( String.valueOf( field.getScale() ) ) );
      } else if ( field.getAvroType().equals( AvroSpec.DataType.FLOAT ) || field.getAvroType().equals( AvroSpec.DataType.DOUBLE ) ) {
        i++;
        item.setText( i++, field.getScale() > 0 ? String.valueOf( field.getScale() ) : "" );
      } else {
        i += 2;
      }
      item.setText( i++, coalesce( field.getDefaultValue() ) );
      item.setText( i++, field.getAllowNull() ? NullableValuesEnum.YES.getValue() : NullableValuesEnum.NO.getValue() );
    } );
  }

  private String coalesce( String value ) {
    return value == null ? "" : value;
  }

  private void populateFieldsUI( List<AvroOutputField> fields, TableView wFields,
                                BiConsumer<AvroOutputField, TableItem> converter ) {
    int nrFields = fields.size();
    for ( int i = 0; i < nrFields; i++ ) {
      TableItem item = null;
      if ( i < wFields.table.getItemCount() ) {
        item = wFields.table.getItem( i );
      } else {
        item = new TableItem( wFields.table, SWT.NONE );
      }
      converter.accept( fields.get( i ), item );
    }
  }

  protected void getFields() {
    try {
      RowMetaInterface r = transMeta.getPrevStepFields( stepname );
      if ( r != null ) {
        TableItemInsertListener listener = ( tableItem, v ) -> true;
        getFieldsFromPreviousStep( r, wOutputFields, 1, new int[] { 1, 2 }, new int[] { 3 }, 4,
            5, true, listener );
      }
    } catch ( KettleException ke ) {
      new ErrorDialog( shell, BaseMessages.getString( PKG, "System.Dialog.GetFieldsFailed.Title" ), BaseMessages
          .getString( PKG, "System.Dialog.GetFieldsFailed.Message" ), ke );
    }
  }

  private MessageDialog getFieldsChoiceDialog( Shell shell, int existingFields, int newFields ) {
    MessageDialog messageDialog =
        new MessageDialog( shell,
            BaseMessages.getString( PKG, "AvroOutputDialog.GetFieldsChoice.Title" ), // "Warning!"
            null,
            BaseMessages.getString( PKG, "AvroOutputDialog.GetFieldsChoice.Message", "" + existingFields, "" + newFields ),
            MessageDialog.WARNING, new String[] {
            BaseMessages.getString( PKG, "AvroOutputDialog.AddNew" ),
            BaseMessages.getString( PKG, "AvroOutputDialog.Add" ),
            BaseMessages.getString( PKG, "AvroOutputDialog.ClearAndAdd" ),
            BaseMessages.getString( PKG, "AvroOutputDialog.Cancel" ), }, 0 );
    MessageDialog.setDefaultImage( GUIResource.getInstance().getImageSpoon() );
    return messageDialog;
  }

  private void getFieldsFromPreviousStep( RowMetaInterface row, TableView tableView, int keyColumn,
                                          int[] nameColumn, int[] dataTypeColumn, int lengthColumn,
                                          int precisionColumn, boolean optimizeWidth,
                                          TableItemInsertListener listener ) {
    if ( row == null || row.size() == 0 ) {
      return; // nothing to do
    }

    Table table = tableView.table;

    // get a list of all the non-empty keys (names)
    //
    List<String> keys = new ArrayList<>();
    for ( int i = 0; i < table.getItemCount(); i++ ) {
      TableItem tableItem = table.getItem( i );
      String key = tableItem.getText( keyColumn );
      if ( !Utils.isEmpty( key ) && keys.indexOf( key ) < 0 ) {
        keys.add( key );
      }
    }

    int choice = 0;

    if ( !keys.isEmpty() ) {
      // Ask what we should do with the existing data in the step.
      //
      MessageDialog getFieldsChoiceDialog = getFieldsChoiceDialog( tableView.getShell(), keys.size(), row.size() );

      int idx = getFieldsChoiceDialog.open();
      choice = idx & 0xFF;
    }

    if ( choice == 3 || choice == 255 ) {
      return; // Cancel clicked
    }

    if ( choice == 2 ) {
      tableView.clearAll( false );
    }

    for ( int i = 0; i < row.size(); i++ ) {
      ValueMetaInterface v = row.getValueMeta( i );

      boolean add = true;

      // hang on, see if it's not yet in the table view
      if ( choice == 0 && keys.indexOf( v.getName() ) >= 0 ) {
        add = false;
      }

      if ( add ) {
        TableItem tableItem = new TableItem( table, SWT.NONE );

        for ( int c = 0; c < nameColumn.length; c++ ) {
          tableItem.setText( nameColumn[c], Const.NVL( v.getName(), "" ) );
        }

        String avroTypeName = AvroTypeConverter.convertToAvroType( v.getType() );
        if ( dataTypeColumn != null ) {
          for ( int c = 0; c < dataTypeColumn.length; c++ ) {
            tableItem.setText( dataTypeColumn[c], avroTypeName );
          }
        }

        if ( avroTypeName.equals( AvroSpec.DataType.DECIMAL.getName() ) ) {
          if ( lengthColumn > 0 && v.getLength() > 0 ) {
            tableItem.setText( lengthColumn, Integer.toString( v.getLength() ) );
          } else {
            // Set the default precision
            tableItem.setText( lengthColumn, Integer.toString( AvroSpec.DEFAULT_DECIMAL_PRECISION ) );
          }

          if ( precisionColumn > 0 && v.getPrecision() >= 0 ) {
            tableItem.setText( precisionColumn, Integer.toString( v.getPrecision() ) );
          } else {
            // Set the default scale
            tableItem.setText( precisionColumn, Integer.toString( AvroSpec.DEFAULT_DECIMAL_SCALE ) );
          }
        } else if ( avroTypeName.equals( AvroSpec.DataType.FLOAT.getName() ) || avroTypeName.equals( AvroSpec.DataType.DOUBLE.getName() ) ) {
          if ( precisionColumn > 0 && v.getPrecision() > 0 ) {
            tableItem.setText( precisionColumn, Integer.toString( v.getPrecision() ) );
          }
        }

        if ( listener != null && !listener.tableItemInserted( tableItem, v ) ) {
          tableItem.dispose(); // remove it again
        }
      }
    }
    tableView.removeEmptyRows();
    tableView.setRowNums();
    if ( optimizeWidth ) {
      tableView.optWidth( true );
    }
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
  protected Listener getPreview() {
    return null;
  }

  @Override protected SelectionOperation selectionOperation() {
    return SelectionOperation.SAVE_TO_FILE_FOLDER;
  }
}

