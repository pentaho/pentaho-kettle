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

package org.pentaho.di.trans.step.jms.ui;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.StepPluginType;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaBase;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.step.StepOption;
import org.pentaho.di.trans.step.jms.JmsDelegate;
import org.pentaho.di.trans.step.jms.JmsProducerMeta;
import org.pentaho.di.ui.core.ConstUI;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.ComboVar;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static com.google.common.base.Strings.nullToEmpty;
import static java.util.Arrays.stream;
import static org.pentaho.di.i18n.BaseMessages.getString;
import static org.pentaho.di.trans.step.jms.JmsConstants.PKG;
import static org.pentaho.di.trans.step.jms.JmsProducerMeta.DELIVERY_DELAY;
import static org.pentaho.di.trans.step.jms.JmsProducerMeta.DELIVERY_MODE;
import static org.pentaho.di.trans.step.jms.JmsProducerMeta.DISABLE_MESSAGE_ID;
import static org.pentaho.di.trans.step.jms.JmsProducerMeta.DISABLE_MESSAGE_TIMESTAMP;
import static org.pentaho.di.trans.step.jms.JmsProducerMeta.JMS_CORRELATION_ID;
import static org.pentaho.di.trans.step.jms.JmsProducerMeta.JMS_TYPE;
import static org.pentaho.di.trans.step.jms.JmsProducerMeta.PRIORITY;
import static org.pentaho.di.trans.step.jms.JmsProducerMeta.TIME_TO_LIVE;
import static org.pentaho.di.ui.trans.step.BaseStreamingDialog.INPUT_WIDTH;


public class JmsProducerDialog extends BaseStepDialog implements StepDialogInterface {
  private static final int SHELL_MIN_WIDTH = 528;
  private static final int SHELL_MIN_HEIGHT = 670;

  private ModifyListener lsMod;
  private final JmsDelegate jmsDelegate;
  private final JmsProducerMeta meta;
  private CTabFolder wTabFolder;
  private ConnectionForm connectionForm;
  private DestinationForm destinationForm;
  private ComboVar wMessageField;
  private TableView propertiesTable;
  private TableView optionsTable;

  private JmsDialogSecurityLayout jmsDialogSecurityLayout;

  private List<StepOption> options;

  public JmsProducerDialog( Shell parent, Object meta,
                            TransMeta transMeta, String stepname ) {
    super( parent, (BaseStepMeta) meta, transMeta, stepname );
    this.meta = (JmsProducerMeta) meta;
    this.jmsDelegate = this.meta.jmsDelegate;
    options = this.meta.retriveOptions();
    lsMod = e -> this.meta.setChanged();
    lsOK = e -> ok();
    lsCancel = e -> cancel();
  }

  @Override public String open() {
    Shell parent = getParent();
    Display display = parent.getDisplay();

    shell = new Shell( parent, SWT.DIALOG_TRIM | SWT.MIN | SWT.MAX | SWT.RESIZE );
    props.setLook( shell );
    setShellImage( shell, meta );
    shell.setMinimumSize( SHELL_MIN_WIDTH, SHELL_MIN_HEIGHT );

    changed = meta.hasChanged();

    FormLayout formLayout = new FormLayout();
    formLayout.marginWidth = 15;
    formLayout.marginHeight = 15;

    shell.setLayout( formLayout );
    shell.setText( BaseMessages.getString( PKG, "JmsProducerDialog.Shell.Title" ) );

    Label wicon = new Label( shell, SWT.RIGHT );
    wicon.setImage( getImage() );
    FormData fdlicon = new FormData();
    fdlicon.top = new FormAttachment( 0, 0 );
    fdlicon.right = new FormAttachment( 100, 0 );
    wicon.setLayoutData( fdlicon );
    props.setLook( wicon );

    wlStepname = new Label( shell, SWT.RIGHT );
    wlStepname.setText( BaseMessages.getString( PKG, "JmsProducerDialog.Stepname.Label" ) );
    props.setLook( wlStepname );
    fdlStepname = new FormData();
    fdlStepname.left = new FormAttachment( 0, 0 );
    fdlStepname.top = new FormAttachment( 0, 0 );
    wlStepname.setLayoutData( fdlStepname );

    wStepname = new Text( shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    wStepname.setText( stepname );
    props.setLook( wStepname );
    wStepname.addModifyListener( lsMod );
    fdStepname = new FormData();
    fdStepname.width = 250;
    fdStepname.left = new FormAttachment( 0, 0 );
    fdStepname.top = new FormAttachment( wlStepname, 5 );
    wStepname.setLayoutData( fdStepname );

    Label spacer = new Label( shell, SWT.HORIZONTAL | SWT.SEPARATOR );
    props.setLook( spacer );
    FormData fdSpacer = new FormData();
    fdSpacer.height = 2;
    fdSpacer.left = new FormAttachment( 0, 0 );
    fdSpacer.top = new FormAttachment( wStepname, 15 );
    fdSpacer.right = new FormAttachment( 100, 0 );
    fdSpacer.width = 497;
    spacer.setLayoutData( fdSpacer );

    // Start of tabbed display
    wTabFolder = new CTabFolder( shell, SWT.BORDER );
    props.setLook( wTabFolder, Props.WIDGET_STYLE_TAB );
    wTabFolder.setSimple( false );
    wTabFolder.setUnselectedCloseVisible( true );

    wCancel = new Button( shell, SWT.PUSH );
    wCancel.setText( BaseMessages.getString( PKG, "System.Button.Cancel" ) );
    FormData fdCancel = new FormData();
    fdCancel.right = new FormAttachment( 100, 0 );
    fdCancel.bottom = new FormAttachment( 100, 0 );
    wCancel.setLayoutData( fdCancel );

    wOK = new Button( shell, SWT.PUSH );
    wOK.setText( BaseMessages.getString( PKG, "System.Button.OK" ) );
    FormData fdOk = new FormData();
    fdOk.right = new FormAttachment( wCancel, -5 );
    fdOk.bottom = new FormAttachment( 100, 0 );
    wOK.setLayoutData( fdOk );

    Label hSpacer = new Label( shell, SWT.HORIZONTAL | SWT.SEPARATOR );
    props.setLook( hSpacer );
    FormData fdhSpacer = new FormData();
    fdhSpacer.height = 2;
    fdhSpacer.left = new FormAttachment( 0, 0 );
    fdhSpacer.bottom = new FormAttachment( wCancel, -15 );
    fdhSpacer.right = new FormAttachment( 100, 0 );
    hSpacer.setLayoutData( fdhSpacer );

    FormData fdTabFolder = new FormData();
    fdTabFolder.left = new FormAttachment( 0, 0 );
    fdTabFolder.top = new FormAttachment( spacer, 15 );
    fdTabFolder.bottom = new FormAttachment( hSpacer, -15 );
    fdTabFolder.right = new FormAttachment( 100, 0 );
    wTabFolder.setLayoutData( fdTabFolder );

    //Setup Tab
    CTabItem wSetupTab = new CTabItem( wTabFolder, SWT.NONE );
    wSetupTab.setText( BaseMessages.getString( PKG, "JmsProducerDialog.SetupTab" ) );

    Composite wSetupComp = new Composite( wTabFolder, SWT.NONE );
    props.setLook( wSetupComp );
    FormLayout setupLayout = new FormLayout();
    setupLayout.marginHeight = 15;
    setupLayout.marginWidth = 15;
    wSetupComp.setLayout( setupLayout );

    jmsDialogSecurityLayout = new JmsDialogSecurityLayout(
      props, wTabFolder, lsMod, transMeta, jmsDelegate.sslEnabled, jmsDelegate );
    jmsDialogSecurityLayout.buildSecurityTab();

    connectionForm = new ConnectionForm( wSetupComp, props, transMeta, lsMod, jmsDelegate, jmsDialogSecurityLayout );
    Group group = connectionForm.layoutForm();

    jmsDialogSecurityLayout.setConnectionForm( connectionForm );

    destinationForm = new DestinationForm(
      wSetupComp, group, props, transMeta, lsMod, jmsDelegate.destinationType, jmsDelegate.destinationName );
    Composite destinationFormComposite = destinationForm.layoutForm();

    Label lbMessageField = new Label( wSetupComp, SWT.LEFT );
    props.setLook( lbMessageField );
    lbMessageField.setText( getString( PKG, "JmsProducerDialog.MessageField" ) );
    FormData fdMessage = new FormData();
    fdMessage.left = new FormAttachment( 0, 0 );
    fdMessage.top = new FormAttachment( destinationFormComposite, 15 );
    fdMessage.width = 250;
    lbMessageField.setLayoutData( fdMessage );

    wMessageField = new ComboVar( transMeta, wSetupComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wMessageField );
    wMessageField.addModifyListener( lsMod );
    FormData fdMessageField = new FormData();
    fdMessageField.left = new FormAttachment( 0, 0 );
    fdMessageField.top = new FormAttachment( lbMessageField, 5 );
    fdMessageField.width = 250;
    wMessageField.setLayoutData( fdMessageField );

    Listener lsMessageFocus = e -> {
      String current = wMessageField.getText();
      wMessageField.getCComboWidget().removeAll();
      wMessageField.setText( current );
      try {
        RowMetaInterface rmi = transMeta.getPrevStepFields( meta.getParentStepMeta().getName() );
        List ls = rmi.getValueMetaList();
        for ( Object l : ls ) {
          ValueMetaBase vmb = (ValueMetaBase) l;
          wMessageField.add( vmb.getName() );
        }
      } catch ( KettleStepException ex ) {
        // do nothing
      }
    };
    wMessageField.getCComboWidget().addListener( SWT.FocusIn, lsMessageFocus );

    FormData fdSetupComp = new FormData();
    fdSetupComp.left = new FormAttachment( 0, 0 );
    fdSetupComp.top = new FormAttachment( 0, 0 );
    fdSetupComp.right = new FormAttachment( 100, 0 );
    fdSetupComp.bottom = new FormAttachment( 100, 0 );
    wSetupComp.setLayoutData( fdSetupComp );
    wSetupComp.layout();
    wSetupTab.setControl( wSetupComp );

    wTabFolder.setSelection( 0 );

    wOK.addListener( SWT.Selection, lsOK );
    wCancel.addListener( SWT.Selection, lsCancel );

    //get data for message field, other fields data is loaded by the forms
    wMessageField.setText( nullToEmpty( meta.getFieldToSend() ) );

    buildOptionsTab();
    buildProperiesTab();

    setSize();

    meta.setChanged( changed );
    shell.open();

    while ( !shell.isDisposed() ) {
      if ( !display.readAndDispatch() ) {
        display.sleep();
      }
    }

    return stepname;
  }

  private void buildProperiesTab() {
    CTabItem wPropertiesTab = new CTabItem( wTabFolder, SWT.NONE );
    wPropertiesTab.setText( BaseMessages.getString( PKG, "JmsProducerDialog.Properties.Tab" ) );
    Composite wPropertiesComp = new Composite( wTabFolder, SWT.NONE );
    props.setLook( wPropertiesComp );
    FormLayout fieldsLayout = new FormLayout();
    fieldsLayout.marginHeight = 15;
    fieldsLayout.marginWidth = 15;
    wPropertiesComp.setLayout( fieldsLayout );

    FormData propertiesFormData = new FormData();
    propertiesFormData.left = new FormAttachment( 0, 0 );
    propertiesFormData.top = new FormAttachment( wPropertiesComp, 0 );
    propertiesFormData.right = new FormAttachment( 100, 0 );
    propertiesFormData.bottom = new FormAttachment( 100, 0 );
    wPropertiesComp.setLayoutData( propertiesFormData );

    buildPropertiesTable( wPropertiesComp );

    wPropertiesComp.layout();
    wPropertiesTab.setControl( wPropertiesComp );
  }

  private void buildPropertiesTable( Composite parentWidget ) {
    ColumnInfo[] columns = getPropertiesColumns();

    int fieldCount = meta.getPropertyValuesByName().size();

    propertiesTable = new TableView(
      transMeta,
      parentWidget,
      SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI,
      columns,
      fieldCount,
      false,
      lsMod,
      props,
      false
    );

    propertiesTable.setSortable( false );
    propertiesTable.getTable().addListener( SWT.Resize, event -> {
      Table table = (Table) event.widget;
      table.getColumn( 1 ).setWidth( 215 );
      table.getColumn( 2 ).setWidth( 215 );
    } );

    populateProperties();

    FormData fdData = new FormData();
    fdData.left = new FormAttachment( 0, 0 );
    fdData.top = new FormAttachment( 0, 0 );
    fdData.right = new FormAttachment( 100, 0 );
    fdData.bottom = new FormAttachment( 100, 0 );

    // resize the columns to fit the data in them
    Arrays.stream( propertiesTable.getTable().getColumns() ).forEach( column -> {
      if ( column.getWidth() > 0 ) {
        // don't pack anything with a 0 width, it will resize it to make it visible (like the index column)
        column.setWidth( 120 );
      }
    } );

    propertiesTable.setLayoutData( fdData );
  }

  private ColumnInfo[] getPropertiesColumns() {

    ColumnInfo propertyName = new ColumnInfo( BaseMessages.getString( PKG, "JmsProducerDialog.Properties.Column.Name" ),
      ColumnInfo.COLUMN_TYPE_TEXT, false, false );
    propertyName.setUsingVariables( true );

    ColumnInfo propertyValue =
      new ColumnInfo( BaseMessages.getString( PKG, "JmsProducerDialog.Properties.Column.Value" ),
        ColumnInfo.COLUMN_TYPE_TEXT, false, false );
    propertyValue.setUsingVariables( true );

    return new ColumnInfo[] { propertyName, propertyValue };
  }

  public static Map<String, String> getMapFromTableView( TableView table ) {
    int itemCount = table.getItemCount();
    Map<String, String> propertyValuesByName = new LinkedHashMap<>();

    for ( int rowIndex = 0; rowIndex < itemCount; rowIndex++ ) {
      TableItem row = table.getTable().getItem( rowIndex );
      String propertyName = row.getText( 1 );
      String propertyValue = row.getText( 2 );
      if ( !StringUtils.isBlank( propertyName ) && !propertyValuesByName.containsKey( propertyName ) ) {
        propertyValuesByName.put( propertyName, propertyValue );
      }
    }
    return propertyValuesByName;
  }

  private void populateProperties() {
    int rowIndex = 0;
    for ( Map.Entry<String, String> entry : meta.getPropertyValuesByName().entrySet() ) {
      TableItem key = propertiesTable.getTable().getItem( rowIndex++ );
      key.setText( 1, entry.getKey() );
      key.setText( 2, entry.getValue() );
    }
  }

  private void buildOptionsTab() {
    CTabItem wOptionsTab = new CTabItem( wTabFolder, SWT.NONE );
    wOptionsTab.setText( BaseMessages.getString( PKG, "JmsDialog.Options.Tab" ) );

    Composite wOptionsComp = new Composite( wTabFolder, SWT.NONE );
    props.setLook( wOptionsComp );
    FormLayout optionsLayout = new FormLayout();
    optionsLayout.marginHeight = 15;
    optionsLayout.marginWidth = 15;
    wOptionsComp.setLayout( optionsLayout );

    FormData fdOptionsComp = new FormData();
    fdOptionsComp.left = new FormAttachment( 0, 0 );
    fdOptionsComp.top = new FormAttachment( 0, 0 );
    fdOptionsComp.right = new FormAttachment( 100, 0 );
    wOptionsComp.setLayoutData( fdOptionsComp );

    ColumnInfo[] columns = getOptionsColumns();

    int fieldCount = 1;

    optionsTable = new TableView(
      transMeta,
      wOptionsComp,
      SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI,
      columns,
      fieldCount,
      false,
      lsMod,
      props,
      false
    );

    optionsTable.setSortable( false );
    optionsTable.getTable().addListener( SWT.Resize, event -> {
      Table table = (Table) event.widget;
      table.getColumn( 1 ).setWidth( 215 );
      table.getColumn( 2 ).setWidth( 215 );
    } );

    populateOptionsTable();

    FormData fdData = new FormData();
    fdData.left = new FormAttachment( 0, 0 );
    fdData.top = new FormAttachment( 0, 0 );
    fdData.right = new FormAttachment( 100, 0 );
    fdData.bottom = new FormAttachment( 100, 0 );
    fdData.width = INPUT_WIDTH + 10;

    // resize the columns to fit the data in them
    stream( optionsTable.getTable().getColumns() ).forEach( column -> {
      if ( column.getWidth() > 0 ) {
        // don't pack anything with a 0 width, it will resize it to make it visible (like the index column)
        column.setWidth( 120 );
      }
    } );

    // don't let any rows get deleted or added (this does not affect the read-only state of the cells)
    optionsTable.setReadonly( true );
    optionsTable.setLayoutData( fdData );

    wOptionsComp.layout();
    wOptionsTab.setControl( wOptionsComp );
  }

  private ColumnInfo[] getOptionsColumns() {
    ColumnInfo optionName = new ColumnInfo( BaseMessages.getString( PKG, "JmsDialog.Options.Column.Name" ),
      ColumnInfo.COLUMN_TYPE_TEXT, false, true );
    ColumnInfo optionValue = new ColumnInfo( BaseMessages.getString( PKG, "JmsDialog.Options.Column.Value" ),
      ColumnInfo.COLUMN_TYPE_TEXT, false, false );
    optionValue.setUsingVariables( true );

    return new ColumnInfo[] { optionName, optionValue };
  }

  private void populateOptionsTable() {
    optionsTable.clearAll();
    options.stream()
      .forEach( option -> optionsTable.add( option.getText(), option.getValue() ) );
    optionsTable.remove( 0 );
  }

  private void ok() {
    stepname = wStepname.getText();

    jmsDelegate.connectionType = connectionForm.getConnectionType();
    jmsDelegate.ibmUrl = connectionForm.getIbmUrl();

    jmsDelegate.amqUrl = connectionForm.getActiveUrl();

    jmsDelegate.destinationType = destinationForm.getDestinationType();
    jmsDelegate.destinationName = destinationForm.getDestinationName();
    meta.setFieldToSend( wMessageField.getText() );
    meta.setPropertyValuesByName( getMapFromTableView( propertiesTable ) );

    jmsDialogSecurityLayout.saveTableValues();
    jmsDialogSecurityLayout.saveAuthentication();
    saveOptions();

    dispose();
  }

  private List<StepOption> saveOptions() {
    IntStream.range( 0, optionsTable.getItemCount() )
      .mapToObj( i -> optionsTable.getItem( i ) )
      .forEach( item ->
        options.stream().forEach( option -> {
          if ( option.getText().equals( item[ 0 ] ) ) {
            switch ( option.getKey() ) {
              case DISABLE_MESSAGE_ID:
                meta.setDisableMessageId( item[ 1 ] );
                break;
              case DISABLE_MESSAGE_TIMESTAMP:
                meta.setDisableMessageTimestamp( item[ 1 ] );
                break;
              case DELIVERY_MODE:
                meta.setDeliveryMode( item[ 1 ] );
                break;
              case PRIORITY:
                meta.setPriority( item[ 1 ] );
                break;
              case TIME_TO_LIVE:
                meta.setTimeToLive( item[ 1 ] );
                break;
              case DELIVERY_DELAY:
                meta.setDeliveryDelay( item[ 1 ] );
                break;
              case JMS_CORRELATION_ID:
                meta.setJmsCorrelationId( item[ 1 ] );
                break;
              case JMS_TYPE:
                meta.setJmsType( item[ 1 ] );
                break;
              default:
                log.logBasic( BaseMessages.getString( PKG, "JmsDialog.Options.OptionNotFound", option.getKey() ) );
            }
          }
        } )
      );
    return options;
  }

  @Override
  public void setSize() {
    setSize( shell );  // sets shell location and preferred size
    shell.setMinimumSize( SHELL_MIN_WIDTH, SHELL_MIN_HEIGHT );
    shell.setSize( SHELL_MIN_WIDTH, SHELL_MIN_HEIGHT ); // force initial size
  }

  private void cancel() {
    meta.setChanged( false );
    dispose();
  }

  private Image getImage() {
    PluginInterface plugin =
      PluginRegistry.getInstance().getPlugin( StepPluginType.class, stepMeta.getStepMetaInterface() );
    String id = plugin.getIds()[ 0 ];
    if ( id != null ) {
      return GUIResource.getInstance().getImagesSteps().get( id ).getAsBitmapForSize( shell.getDisplay(),
        ConstUI.LARGE_ICON_SIZE, ConstUI.LARGE_ICON_SIZE );
    }
    return null;
  }
}
