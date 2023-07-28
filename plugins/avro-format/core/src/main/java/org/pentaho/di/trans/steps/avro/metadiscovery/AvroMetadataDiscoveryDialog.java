/*******************************************************************************
 * HITACHI VANTARA PROPRIETARY AND CONFIDENTIAL
 *
 * Copyright 2018 - 2023 Hitachi Vantara. All rights reserved.
 *
 * NOTICE: All information including source code contained herein is, and
 * remains the sole property of Hitachi Vantara and its licensors. The intellectual
 * and technical concepts contained herein are proprietary and confidential
 * to, and are trade secrets of Hitachi Vantara and may be covered by U.S. and foreign
 * patents, or patents in process, and are protected by trade secret and
 * copyright laws. The receipt or possession of this source code and/or related
 * information does not convey or imply any rights to reproduce, disclose or
 * distribute its contents, or to manufacture, use, or sell anything that it
 * may describe, in whole or in part. Any reproduction, modification, distribution,
 * or public display of this information without the express written authorization
 * from Hitachi Vantara is strictly prohibited and in violation of applicable laws and
 * international treaties. Access to the source code contained herein is strictly
 * prohibited to anyone except those individuals and entities who have executed
 * confidentiality and non-disclosure agreements or other agreements with Hitachi Vantara,
 * explicitly covering such access.
 ******************************************************************************/
package org.pentaho.di.trans.steps.avro.metadiscovery;

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
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.TransPreviewFactory;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.ui.core.ConstUI;
import org.pentaho.di.ui.core.FormDataBuilder;
import org.pentaho.di.ui.core.dialog.EnterNumberDialog;
import org.pentaho.di.ui.core.dialog.EnterTextDialog;
import org.pentaho.di.ui.core.dialog.PreviewRowsDialog;
import org.pentaho.di.ui.core.events.dialog.SelectionAdapterFileDialogTextVar;
import org.pentaho.di.ui.core.events.dialog.SelectionAdapterOptions;
import org.pentaho.di.ui.core.events.dialog.SelectionOperation;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.widget.ComboVar;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.trans.dialog.TransPreviewProgressDialog;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class AvroMetadataDiscoveryDialog extends BaseStepDialog implements StepDialogInterface {


  private static final Class<?> PKG = AvroMetadataDiscoveryDialog.class;
  private AvroMetadataDiscoveryMeta meta;

  public AvroMetadataDiscoveryDialog( Shell parent, Object baseStepMeta, TransMeta transMeta, String stepname ) {
    super( parent, (StepMetaInterface) baseStepMeta, transMeta, stepname );
    this.meta = (AvroMetadataDiscoveryMeta) baseStepMeta;
  }

  private CCombo wSchemaFieldName;
  protected Text wAvroPathFieldName;
  protected Text wAvroNullableFieldName;
  protected Text wAvroTypeFieldName;
  protected Text wAvroKettleTypeFieldName;
  public static final int MARGIN = 15;

  protected CCombo encodingCombo;

  protected Group wSourceGroup;

  protected Button wbGetDataFromFile;

  protected Button wbGetDataFromField;

  private static final int RADIO_BUTTON_WIDTH = 150;

  public static final int FIELDS_SEP = 10;

  protected Composite wDataFileComposite;
  protected Composite wDataFieldComposite;

  protected Button wbBrowse;

  protected TextVar wPath;

  protected ComboVar wFieldNameCombo;

  private Map<String, Integer> incomingFields = new HashMap<>();

  public static final int FIELD_MEDIUM = 250;

  Button wbGetSchemaFromFile;
  Button wbGetSchemaFromField;

  protected Composite wSchemaFileComposite;

  protected Button wbSchemaBrowse;

  private TextVar wSchemaPath;

  public static final int FIELD_LABEL_SEP = 5;

  protected Composite wSchemaFieldComposite;

  ComboVar wSchemaFieldNameCombo;

  protected ModifyListener lsMod;

  private static final int SHELL_WIDTH = 698;

  private static final int SHELL_HEIGHT = 554;

  protected Listener getPreview() {
    return e -> doPreview();
  }


  protected SelectionOperation selectionOperation() {
    return SelectionOperation.FILE_OR_FOLDER;
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

    int height = Math.max( getMinHeight( shell, SHELL_WIDTH ), SHELL_HEIGHT );
    shell.setMinimumSize( SHELL_WIDTH, height );
    shell.setSize( SHELL_WIDTH, height );
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

  protected int getMinHeight( Composite comp, int minWidth ) {
    comp.pack();
    return comp.computeSize( minWidth, SWT.DEFAULT ).y;
  }

  private void ok() {

    stepname = wStepname.getText();

    if ( Utils.isEmpty( wStepname.getText() ) ) {
      return;
    }
    stepname = wStepname.getText();

    getInfo( false );
    dispose();
  }

  protected String getBaseMsg( String key ) {
    return BaseMessages.getString( PKG, key );
  }

  protected void addSourceTab( CTabFolder wTabFolder ) {
    // Create & Set up a new Tab Item
    CTabItem wTab = new CTabItem( wTabFolder, SWT.NONE );
    wTab.setText( getBaseMsg( "AvroMetadataDiscovery.File.TabTitle" ) );
    Composite wTabComposite = new Composite( wTabFolder, SWT.NONE );
    wTab.setControl( wTabComposite );
    FormLayout formLayout = new FormLayout();
    formLayout.marginHeight = MARGIN;
    wTabComposite.setLayout( formLayout );

    // Create file encoding Drop Down
    Label encodingLabel = new Label( wTabComposite, SWT.NONE );
    encodingLabel.setText( getBaseMsg( "AvroMetadataDiscovery.Encoding.Label" ) );
    encodingLabel.setLayoutData( new FormDataBuilder().top().right( 100, -MARGIN ).left( 0, MARGIN ).result() );

    encodingCombo = new CCombo( wTabComposite, SWT.BORDER | SWT.READ_ONLY );
    String[] availFormats = {
      BaseMessages.getString( PKG, "AvroMetadataDiscovery.AvroFile.Label" ),
      BaseMessages.getString( PKG, "AvroMetadataDiscovery.JsonDatum.Label" ),
      BaseMessages.getString( PKG, "AvroMetadataDiscovery.BinaryDatum.Label" ),
      BaseMessages.getString( PKG, "AvroMetadataDiscovery.AvroFile.AlternateSchema.Label" )
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
    wFileSettingsGroup.setText( getBaseMsg( "AvroMetadataDiscovery.File.FileSettingsTitle" ) );

    FormLayout layout = new FormLayout();
    layout.marginHeight = MARGIN;
    layout.marginWidth = MARGIN;
    wFileSettingsGroup.setLayout( layout );
    wFileSettingsGroup
      .setLayoutData( new FormDataBuilder().top( encodingLabel, 35 ).left( 0, MARGIN ).right( 100, -MARGIN ).result() );

    Label separator = new Label( wFileSettingsGroup, SWT.SEPARATOR | SWT.VERTICAL );
    separator.setLayoutData( new FormDataBuilder().left( 0, RADIO_BUTTON_WIDTH ).top().bottom().result() );

    wbGetDataFromFile = new Button( wFileSettingsGroup, SWT.RADIO );
    wbGetDataFromFile.setText( getBaseMsg( "AvroMetadataDiscovery.File.SpecifyFileName" ) );
    wbGetDataFromFile.setLayoutData( new FormDataBuilder().left().top().width( RADIO_BUTTON_WIDTH ).result() );

    wbGetDataFromField = new Button( wFileSettingsGroup, SWT.RADIO );
    wbGetDataFromField.setText( getBaseMsg( "AvroMetadataDiscovery.File.GetDataFromField" ) );
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
    fieldNameLabel.setText( getBaseMsg( "AvroMetadataDiscovery.FieldName.Label" ) );
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

    // Set up the Source Group
    wSourceGroup = new Group( wTabComposite, SWT.SHADOW_NONE );
    wSourceGroup.setText( BaseMessages.getString( PKG, "AvroMetadataDiscovery.Schema.SourceTitle" ) );

    FormLayout schemaLayout = new FormLayout();
    schemaLayout.marginWidth = MARGIN;
    schemaLayout.marginHeight = MARGIN;
    wSourceGroup.setLayout( schemaLayout );
    wSourceGroup.setLayoutData(
      new FormDataBuilder().top( wFileSettingsGroup, 10 ).right( 100, -MARGIN ).left( 0, MARGIN ).result() );

    Label schemaSeparator = new Label( wSourceGroup, SWT.SEPARATOR | SWT.VERTICAL );
    schemaSeparator.setLayoutData( new FormDataBuilder().left( 0, RADIO_BUTTON_WIDTH ).top().bottom().result() );

    wbGetSchemaFromFile = new Button( wSourceGroup, SWT.RADIO );
    wbGetSchemaFromFile.setText( getBaseMsg( "AvroMetadataDiscovery.File.SpecifyFileName" ) );
    wbGetSchemaFromFile.setLayoutData( new FormDataBuilder().left().top().width( RADIO_BUTTON_WIDTH ).result() );

    wbGetSchemaFromField = new Button( wSourceGroup, SWT.RADIO );
    wbGetSchemaFromField.setText( getBaseMsg( "AvroMetadataDiscovery.File.GetDataFromField" ) );
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
    wlSchemaPath.setText( BaseMessages.getString( PKG, "AvroMetadataDiscovery.Schema.FileName" ) );
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
    fieldNameSchemaLabel.setText( getBaseMsg( "AvroMetadataDiscovery.FieldName.Label" ) );
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

  private void addColumnNameTab( CTabFolder wTabFolder ) {
    CTabItem wTab = new CTabItem( wTabFolder, SWT.NONE );
    wTab.setText( BaseMessages.getString( PKG, "AvroMetadataDiscovery.ConfigTab.TabTitle" ) );

    Composite wComp = new Composite( wTabFolder, SWT.NONE );
    wTab.setControl( wComp );
    FormLayout layout = new FormLayout();
    layout.marginWidth = MARGIN;
    layout.marginHeight = MARGIN;
    layout.marginBottom = MARGIN;
    wComp.setLayout( layout );

    Label pathFieldNameLbl = new Label( wComp, SWT.NONE );
    pathFieldNameLbl.setText( BaseMessages.getString( PKG, "AvroMetadataDiscovery.Dialog.AvroPathFieldName" ) );
    pathFieldNameLbl.setLayoutData( new FormDataBuilder().top().right( 100, -MARGIN ).left( 0, MARGIN ).result() );
    wAvroPathFieldName = new Text( wComp, SWT.NONE | SWT.BORDER );
    wAvroPathFieldName.setText( !Utils.isEmpty( meta.getAvroPathFieldName() ) ? meta.getAvroPathFieldName() : "" );
    wAvroPathFieldName.setLayoutData( new FormDataBuilder().top( pathFieldNameLbl, 5 ).left( 0, MARGIN )
      .right( 0, MARGIN + 400 ).result() );

    Label typeFieldNameLbl = new Label( wComp, SWT.NONE );
    typeFieldNameLbl.setText( BaseMessages.getString( PKG, "AvroMetadataDiscovery.Dialog.AvroTypeFieldName" ) );
    typeFieldNameLbl.setLayoutData(
      new FormDataBuilder().top( wAvroPathFieldName, MARGIN ).right( 100, -MARGIN ).left( 0, MARGIN ).result() );
    wAvroTypeFieldName = new Text( wComp, SWT.NONE | SWT.BORDER );
    wAvroTypeFieldName.setText( !Utils.isEmpty( meta.getAvroTypeFieldName() ) ? meta.getAvroTypeFieldName() : "" );
    wAvroTypeFieldName.setLayoutData( new FormDataBuilder().top( typeFieldNameLbl, 5 ).left( 0, MARGIN )
      .right( 0, MARGIN + 400 ).result() );

    Label nullableFieldNameLbl = new Label( wComp, SWT.NONE );
    nullableFieldNameLbl.setText( BaseMessages.getString( PKG, "AvroMetadataDiscovery.Dialog.AvroNullableFieldName" ) );
    nullableFieldNameLbl.setLayoutData(
      new FormDataBuilder().top( wAvroTypeFieldName, MARGIN ).right( 100, -MARGIN ).left( 0, MARGIN ).result() );
    wAvroNullableFieldName = new Text( wComp, SWT.NONE | SWT.BORDER );
    wAvroNullableFieldName.setText( !Utils.isEmpty( meta.getNullableFieldName() ) ? meta.getNullableFieldName() : "" );
    wAvroNullableFieldName.setLayoutData( new FormDataBuilder().top( nullableFieldNameLbl, 5 ).left( 0, MARGIN )
      .right( 0, MARGIN + 400 ).result() );

    Label kettleTypeFieldNameLbl = new Label( wComp, SWT.NONE );
    kettleTypeFieldNameLbl.setText(
      BaseMessages.getString( PKG, "AvroMetadataDiscovery.Dialog.AvroKettleTypeFieldName" ) );
    kettleTypeFieldNameLbl.setLayoutData(
      new FormDataBuilder().top( wAvroNullableFieldName, MARGIN ).right( 100, -MARGIN ).left( 0, MARGIN ).result() );
    wAvroKettleTypeFieldName = new Text( wComp, SWT.NONE | SWT.BORDER );
    wAvroKettleTypeFieldName.setText(
      !Utils.isEmpty( meta.getKettleTypeFieldName() ) ? meta.getKettleTypeFieldName() : "" );
    wAvroKettleTypeFieldName.setLayoutData( new FormDataBuilder().top( kettleTypeFieldNameLbl, 5 ).left( 0, MARGIN )
      .right( 0, MARGIN + 400 ).result() );
  }

  protected Control addFileWidgets( Composite parent, Control prev ) {
    Label wlPath = new Label( parent, SWT.RIGHT );
    wlPath.setText( getBaseMsg( "AvroMetadataDiscovery.Filename.Label" ) );
    wlPath.setLayoutData( new FormDataBuilder().left().top( prev, MARGIN ).result() );

    wbBrowse = new Button( parent, SWT.PUSH );
    wbBrowse.setText( BaseMessages.getString( "System.Button.Browse" ) );

    wbBrowse.setLayoutData( new FormDataBuilder().top( wlPath ).right().result() );

    wPath = new TextVar( transMeta, parent, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    wPath.addModifyListener( event -> {
      if ( wPreview != null ) {
        wPreview.setEnabled( !Utils.isEmpty( wPath.getText() ) );
      }
    } );
    wPath.addModifyListener( lsMod );
    wPath.setLayoutData( new FormDataBuilder().left().right( wbBrowse, -FIELD_LABEL_SEP ).top( wlPath, FIELD_LABEL_SEP )
      .result() );

    wbBrowse.addSelectionListener( new SelectionAdapterFileDialogTextVar(
      log, wPath, transMeta, new SelectionAdapterOptions( selectionOperation() ) ) );

    return wPath;
  }

  protected void updateIncomingFieldList( ComboVar comboVar ) {
    // Search the fields in the background
    StepMeta stepMeta = transMeta.findStep( stepname );
    if ( stepMeta != null ) {
      try {
        RowMetaInterface row = transMeta.getPrevStepFields( stepMeta );
        incomingFields.clear();
        // Remember these fields...
        for ( int i = 0; i < row.size(); i++ ) {
          incomingFields.put( row.getValueMeta( i ).getName(), i );
        }

        // Add the currentMeta fields...
        final Map<String, Integer> fields = new HashMap<>( incomingFields );

        Set<String> keySet = fields.keySet();
        List<String> entries = new ArrayList<>( keySet );

        String[] fieldNames = entries.toArray( new String[ 0 ] );

        Const.sortStrings( fieldNames );
        comboVar.setItems( fieldNames );
      } catch ( KettleException e ) {
        logError( getBaseMsg( "System.Dialog.GetFieldsFailed.Message" ) );
      }
    }
  }

  protected Label createHeader() {
    // main form
    FormLayout formLayout = new FormLayout();
    formLayout.marginWidth = 15;
    formLayout.marginHeight = 15;
    shell.setLayout( formLayout );
    // title
    shell.setText( BaseMessages.getString( PKG, "AvroMetadataDiscovery.Dialog.Title" ) );
    // buttons
    lsOK = e -> ok();
    lsCancel = e -> cancel();

    // Stepname label
    wlStepname = new Label( shell, SWT.RIGHT );
    wlStepname.setText( getBaseMsg( "AvroMetadataDiscovery.StepName.Label" ) );
    wlStepname.setLayoutData( new FormDataBuilder().left().top().result() );
    // Stepname field
    wStepname = new Text( shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    wStepname.setText( stepname );
    wStepname.addModifyListener( lsMod );
    wStepname.setLayoutData( new FormDataBuilder().left().top( wlStepname, FIELD_LABEL_SEP ).width( FIELD_MEDIUM )
      .result() );

    Label separator = new Label( shell, SWT.HORIZONTAL | SWT.SEPARATOR );
    separator.setLayoutData( new FormDataBuilder().left().right().top( wStepname, 15 ).height( 2 ).result() );

    addIcon();
    return separator;
  }

  private void cancel() {
    stepname = null;
    getStepMeta().setChanged( changed );
    dispose();
  }

  protected void addIcon() {
    Label wicon = new Label( shell, SWT.RIGHT );
    String stepId = getStepMeta().getParentStepMeta().getStepID();
    wicon.setImage( GUIResource.getInstance().getImagesSteps().get( stepId ).getAsBitmapForSize( shell.getDisplay(),
      ConstUI.LARGE_ICON_SIZE, ConstUI.LARGE_ICON_SIZE ) );
    wicon.setLayoutData( new FormDataBuilder().top().right().result() );
  }

  public BaseStepMeta getStepMeta() {
    return (BaseStepMeta) baseStepMeta;
  }

  protected void createUI() {
    Control prev = createHeader();

    createFooter( shell );

    Composite afterFile = new Composite( shell, SWT.NONE );
    afterFile.setLayout( new FormLayout() );
    Label separator = new Label( shell, SWT.HORIZONTAL | SWT.SEPARATOR );
    separator.setLayoutData( new FormDataBuilder().left().right().bottom( wCancel, -MARGIN ).height( 2 ).result() );
    afterFile.setLayoutData(
      new FormDataBuilder().left().top( prev, 0 ).right().bottom( separator, -MARGIN ).result() );
    createAfterFile( afterFile );
  }

  protected Control createAfterFile( Composite afterFile ) {
    CTabFolder wTabFolder = new CTabFolder( afterFile, SWT.BORDER );
    wTabFolder.setSimple( false );

    addSourceTab( wTabFolder );
    addColumnNameTab( wTabFolder );

    wTabFolder.setLayoutData( new FormDataBuilder().left().top( 0, MARGIN ).right().bottom().result() );
    wTabFolder.setSelection( 0 );

    return wTabFolder;
  }

  protected Control createFooter( Composite shell ) {

    wCancel = new Button( shell, SWT.PUSH );
    wCancel.setText( BaseMessages.getString( "System.Button.Cancel" ) );
    wCancel.addListener( SWT.Selection, lsCancel );
    wCancel.setLayoutData( new FormDataBuilder().right().bottom().result() );

    // Some buttons
    wOK = new Button( shell, SWT.PUSH );
    wOK.setText( BaseMessages.getString( "System.Button.OK" ) );
    wOK.addListener( SWT.Selection, lsOK );
    wOK.setLayoutData( new FormDataBuilder().right( wCancel, -FIELD_LABEL_SEP ).bottom().result() );
    lsPreview = getPreview();
    if ( lsPreview != null ) {
      wPreview = new Button( shell, SWT.PUSH );
      wPreview.setText( getBaseMsg( "AvroMetadataDiscovery.Preview" ) );
      wPreview.pack();
      wPreview.addListener( SWT.Selection, lsPreview );
      int offset = wPreview.getBounds().width / 2;
      wPreview.setLayoutData( new FormDataBuilder().bottom().left( new FormAttachment( 50, -offset ) ).result() );
    }
    return wCancel;
  }

  private void doPreview() {
    getInfo( true );
    TransMeta previewMeta = TransPreviewFactory.generatePreviewTransformation( transMeta, meta, wStepname.getText() );
    transMeta.getVariable( "Internal.Transformation.Filename.Directory" );
    previewMeta.getVariable( "Internal.Transformation.Filename.Directory" );

    EnterNumberDialog numberDialog =
      new EnterNumberDialog( shell, props.getDefaultPreviewSize(), BaseMessages.getString( PKG,
        "AvroMetadataDiscovery.PreviewSize.DialogTitle" ), BaseMessages.getString( PKG,
        "AvroMetadataDiscovery.PreviewSize.DialogMessage" ) );
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


  protected void getInfo( boolean preview ) {

    if ( wbGetDataFromField.getSelection() ) {
      meta.setDataLocation( wFieldNameCombo.getText(), AvroMetadataDiscoveryMeta.LocationDescriptor.FIELD_NAME );
    } else {
      meta.setDataLocation( wPath.getText(), AvroMetadataDiscoveryMeta.LocationDescriptor.FILE_NAME );
    }

    if ( wbGetSchemaFromField.getSelection() ) {
      meta.setSchemaLocation( wSchemaFieldNameCombo.getText(),
        AvroMetadataDiscoveryMeta.LocationDescriptor.FIELD_NAME );
    } else {
      meta.setSchemaLocation( wSchemaPath.getText(), AvroMetadataDiscoveryMeta.LocationDescriptor.FILE_NAME );
      meta.setCacheSchemas( false );
    }

    meta.setFormat( encodingCombo.getSelectionIndex() );
    meta.setAvroPathFieldName( wAvroPathFieldName.getText() );
    meta.setNullableFieldName( wAvroNullableFieldName.getText() );
    meta.setAvroTypeFieldName( wAvroTypeFieldName.getText() );
    meta.setKettleTypeFieldName( wAvroKettleTypeFieldName.getText() );

  }

  protected void getData() {
    AvroMetadataDiscoveryMeta avroMetadataDiscoveryMeta = (AvroMetadataDiscoveryMeta) getStepMeta();

    wPath.setText( "" );
    wFieldNameCombo.setText( "" );
    wSchemaPath.setText( "" );
    wbGetDataFromFile.setSelection( true );
    wbGetDataFromField.setSelection( false );
    wDataFileComposite.setVisible( true );
    wDataFieldComposite.setVisible( false );
    encodingCombo.select( avroMetadataDiscoveryMeta.getFormat() );

    wbGetSchemaFromFile.setSelection( true );
    wbGetSchemaFromField.setSelection( false );
    wSchemaFileComposite.setVisible( true );
    wSchemaFieldComposite.setVisible( false );
    wSourceGroup.setVisible( avroMetadataDiscoveryMeta.getFormat() > 0 );

    if ( avroMetadataDiscoveryMeta.getDataLocation() != null ) {
      if ( ( avroMetadataDiscoveryMeta.getDataLocationType()
        == AvroMetadataDiscoveryMeta.LocationDescriptor.FILE_NAME ) ) {
        wPath.setText( avroMetadataDiscoveryMeta.getDataLocation() );
      } else if ( ( avroMetadataDiscoveryMeta.getDataLocationType()
        == AvroMetadataDiscoveryMeta.LocationDescriptor.FIELD_NAME ) ) {
        wFieldNameCombo.setText( avroMetadataDiscoveryMeta.getDataLocation() );
        wbGetDataFromFile.setSelection( false );
        wbGetDataFromField.setSelection( true );
        wDataFileComposite.setVisible( false );
        wDataFieldComposite.setVisible( true );
      }
    }

    if ( avroMetadataDiscoveryMeta.getSchemaLocation() != null ) {
      if ( ( avroMetadataDiscoveryMeta.getSchemaLocationType()
        == AvroMetadataDiscoveryMeta.LocationDescriptor.FILE_NAME ) ) {
        wSchemaPath.setText( avroMetadataDiscoveryMeta.getSchemaLocation() );
      } else if ( ( avroMetadataDiscoveryMeta.getSchemaLocationType()
        == AvroMetadataDiscoveryMeta.LocationDescriptor.FIELD_NAME ) ) {
        wSchemaFieldNameCombo.setText( avroMetadataDiscoveryMeta.getSchemaLocation() );
        wbGetSchemaFromFile.setSelection( false );
        wbGetSchemaFromField.setSelection( true );
        wSchemaFileComposite.setVisible( false );
        wSchemaFieldComposite.setVisible( true );
      }
    }
  }
}
