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


package org.pentaho.big.data.kettle.plugins.kafka;

import com.google.common.collect.ImmutableMap;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
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
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.StepPluginType;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.ui.core.ConstUI;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.ComboVar;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.metastore.api.exceptions.MetaStoreException;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.pentaho.big.data.kettle.plugins.kafka.KafkaProducerOutputMeta.ConnectionType.CLUSTER;
import static org.pentaho.big.data.kettle.plugins.kafka.KafkaProducerOutputMeta.ConnectionType.DIRECT;

@SuppressWarnings ( { "FieldCanBeLocal", "unused" } )
public class KafkaProducerOutputDialog extends BaseStepDialog implements StepDialogInterface {

  private static final Class<?> PKG = KafkaProducerOutputMeta.class;
  // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

  private static final ImmutableMap<String, String> DEFAULT_OPTION_VALUES = ImmutableMap.of(
    ProducerConfig.COMPRESSION_TYPE_CONFIG, "none" );

  private final KafkaFactory kafkaFactory = KafkaFactory.defaultFactory();

  private static final int SHELL_MIN_WIDTH = 527;
  private static final int SHELL_MIN_HEIGHT = 569;
  private static final int INPUT_WIDTH = 350;

  private KafkaProducerOutputMeta meta;
  protected ModifyListener lsMod;
  private Label wlClusterName;
  protected ComboVar wClusterName;

  private TextVar wClientId;
  protected ComboVar wTopic;
  private ComboVar wKeyField;
  private ComboVar wMessageField;
  protected TableView optionsTable;
  private CTabFolder wTabFolder;

  private Button wbDirect;
  protected Button wbCluster;
  private Label wlBootstrapServers;
  protected TextVar wBootstrapServers;
  private Composite wOptionsComp;

  public KafkaProducerOutputDialog( Shell parent, Object in,
                                    TransMeta transMeta, String stepName ) {
    super( parent, (BaseStepMeta) in, transMeta, stepName );
    meta = (KafkaProducerOutputMeta) in;
  }

  @Override public String open() {
    Shell parent = getParent();
    Display display = parent.getDisplay();
    changed = meta.hasChanged();

    lsMod = e -> meta.setChanged();
    lsCancel = e -> cancel();
    lsOK = e -> ok();
    lsDef = new SelectionAdapter() {
      @Override public void widgetDefaultSelected( SelectionEvent e ) {
        ok();
      }
    };

    shell = new Shell( parent, SWT.DIALOG_TRIM | SWT.MIN | SWT.MAX | SWT.RESIZE );
    prepareOpen();
    shell.open();
    while ( !shell.isDisposed() ) {
      if ( !display.readAndDispatch() ) {
        display.sleep();
      }
    }
    return stepname;
  }

  /**
   * This method will prepare Shell with necessary data
   * and can be used in EE.
   */
  public void prepareOpen() {
    props.setLook( shell );
    setShellImage( shell, meta );
    shell.setMinimumSize( SHELL_MIN_WIDTH, SHELL_MIN_HEIGHT );
    shell.setText( BaseMessages.getString( PKG, "KafkaProducerOutputDialog.Shell.Title" ) );

    FormLayout formLayout = new FormLayout();
    formLayout.marginWidth = 15;
    formLayout.marginHeight = 15;
    shell.setLayout( formLayout );
    shell.addShellListener( new ShellAdapter() {
      @Override public void shellClosed( ShellEvent e ) {
        cancel();
      }
    } );

    Label wicon = new Label( shell, SWT.RIGHT );
    wicon.setImage( getImage() );
    FormData fdlicon = new FormData();
    fdlicon.top = new FormAttachment( 0, 0 );
    fdlicon.right = new FormAttachment( 100, 0 );
    wicon.setLayoutData( fdlicon );
    props.setLook( wicon );

    wlStepname = new Label( shell, SWT.RIGHT );
    wlStepname.setText( BaseMessages.getString( PKG, "KafkaProducerOutputDialog.Stepname.Label" ) );
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
    wStepname.addSelectionListener( lsDef );

    Label topSeparator = new Label( shell, SWT.HORIZONTAL | SWT.SEPARATOR );
    FormData fdSpacer = new FormData();
    fdSpacer.height = 2;
    fdSpacer.left = new FormAttachment( 0, 0 );
    fdSpacer.top = new FormAttachment( wStepname, 15 );
    fdSpacer.right = new FormAttachment( 100, 0 );
    topSeparator.setLayoutData( fdSpacer );

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
    wCancel.addListener( SWT.Selection, lsCancel );

    wOK = new Button( shell, SWT.PUSH );
    wOK.setText( BaseMessages.getString( PKG, "System.Button.OK" ) );
    FormData fdOk = new FormData();
    fdOk.right = new FormAttachment( wCancel, -5 );
    fdOk.bottom = new FormAttachment( 100, 0 );
    wOK.setLayoutData( fdOk );
    wOK.addListener( SWT.Selection, lsOK );

    Label bottomSeparator = new Label( shell, SWT.HORIZONTAL | SWT.SEPARATOR );
    props.setLook( bottomSeparator );
    FormData fdBottomSeparator = new FormData();
    fdBottomSeparator.height = 2;
    fdBottomSeparator.left = new FormAttachment( 0, 0 );
    fdBottomSeparator.bottom = new FormAttachment( wCancel, -15 );
    fdBottomSeparator.right = new FormAttachment( 100, 0 );
    bottomSeparator.setLayoutData( fdBottomSeparator );

    FormData fdTabFolder = new FormData();
    fdTabFolder.left = new FormAttachment( 0, 0 );
    fdTabFolder.top = new FormAttachment( topSeparator, 15 );
    fdTabFolder.bottom = new FormAttachment( bottomSeparator, -15 );
    fdTabFolder.right = new FormAttachment( 100, 0 );
    wTabFolder.setLayoutData( fdTabFolder );

    buildSetupTab();
    buildOptionsTab();

    getData();

    setSize();

    meta.setChanged( changed );

    wTabFolder.setSelection( 0 );
  }
  private void buildSetupTab() {
    CTabItem wSetupTab = new CTabItem( wTabFolder, SWT.NONE );
    wSetupTab.setText( BaseMessages.getString( PKG, "KafkaProducerOutputDialog.SetupTab" ) );

    Composite wSetupComp = new Composite( wTabFolder, SWT.NONE );
    props.setLook( wSetupComp );
    FormLayout setupLayout = new FormLayout();
    setupLayout.marginHeight = 15;
    setupLayout.marginWidth = 15;
    wSetupComp.setLayout( setupLayout );

    Group wConnectionGroup = new Group( wSetupComp, SWT.SHADOW_ETCHED_IN );
    props.setLook( wConnectionGroup );
    wConnectionGroup.setText( BaseMessages.getString( PKG, "KafkaConsumerInputDialog.Connection" ) );
    FormLayout flConnection = new FormLayout();
    flConnection.marginHeight = 15;
    flConnection.marginWidth = 15;
    wConnectionGroup.setLayout( flConnection );

    FormData fdConnectionGroup = new FormData();
    fdConnectionGroup.left = new FormAttachment( 0, 0 );
    fdConnectionGroup.top = new FormAttachment( 0, 0 );
    fdConnectionGroup.right = new FormAttachment( 100, 0 );
    fdConnectionGroup.width = INPUT_WIDTH;
    wConnectionGroup.setLayoutData( fdConnectionGroup );

    wbDirect = new Button( wConnectionGroup, SWT.RADIO );
    wbDirect.setText( BaseMessages.getString( PKG, "KafkaConsumerInputDialog.Direct" ) );
    FormData fdbDirect = new FormData();
    fdbDirect.left = new FormAttachment( 0, 0 );
    fdbDirect.top = new FormAttachment( 0, 0 );
    wbDirect.setLayoutData( fdbDirect );
    wbDirect.addSelectionListener( new SelectionListener() {
      @Override public void widgetSelected( final SelectionEvent selectionEvent ) {
        lsMod.modifyText( null );
        toggleConnectionType( true );
      }

      @Override public void widgetDefaultSelected( final SelectionEvent selectionEvent ) {
        toggleConnectionType( true );
      }
    } );
    props.setLook( wbDirect );

    wbCluster = new Button( wConnectionGroup, SWT.RADIO );
    wbCluster.setText( BaseMessages.getString( PKG, "KafkaConsumerInputDialog.Cluster" ) );
    FormData fdbCluster = new FormData();
    fdbCluster.left = new FormAttachment( 0, 0 );
    fdbCluster.top = new FormAttachment( wbDirect, 10 );
    wbCluster.setLayoutData( fdbCluster );
    wbCluster.addSelectionListener( new SelectionListener() {
      @Override public void widgetSelected( final SelectionEvent selectionEvent ) {
        lsMod.modifyText( null );
        toggleConnectionType( false );
      }

      @Override public void widgetDefaultSelected( final SelectionEvent selectionEvent ) {
        toggleConnectionType( false );
      }
    } );
    props.setLook( wbCluster );
    wbCluster.setEnabled( meta.getNamedClusterService() != null );

    Label environmentSeparator = new Label( wConnectionGroup, SWT.SEPARATOR | SWT.VERTICAL );
    FormData fdenvironmentSeparator = new FormData();
    fdenvironmentSeparator.top = new FormAttachment( wbDirect, 0, SWT.TOP );
    fdenvironmentSeparator.left = new FormAttachment( wbCluster, 15 );
    fdenvironmentSeparator.bottom = new FormAttachment( wbCluster, 0, SWT.BOTTOM );
    environmentSeparator.setLayoutData( fdenvironmentSeparator );

    wlClusterName = new Label( wConnectionGroup, SWT.LEFT );
    props.setLook( wlClusterName );
    wlClusterName.setText( BaseMessages.getString( PKG, "KafkaProducerOutputDialog.HadoopCluster" ) );
    FormData fdlClusterName = new FormData();
    fdlClusterName.left = new FormAttachment( environmentSeparator, 15 );
    fdlClusterName.top = new FormAttachment( 0, 0 );
    fdlClusterName.right = new FormAttachment( 78, 0 );
    wlClusterName.setLayoutData( fdlClusterName );

    wClusterName = new ComboVar( transMeta, wConnectionGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wClusterName );
    wClusterName.addModifyListener( lsMod );
    FormData fdClusterName = new FormData();
    fdClusterName.left = new FormAttachment( wlClusterName, 0, SWT.LEFT );
    fdClusterName.top = new FormAttachment( wlClusterName, 5 );
    fdClusterName.right = new FormAttachment( 78, 0 );
    wClusterName.setLayoutData( fdClusterName );
    wClusterName.addSelectionListener( lsDef );

    wlBootstrapServers = new Label( wConnectionGroup, SWT.LEFT );
    props.setLook( wlBootstrapServers );
    wlBootstrapServers.setText( BaseMessages.getString( PKG, "KafkaConsumerInputDialog.BootstrapServers" ) );
    FormData fdlBootstrapServers = new FormData();
    fdlBootstrapServers.left = new FormAttachment( environmentSeparator, 15 );
    fdlBootstrapServers.top = new FormAttachment( 0, 0 );
    fdlBootstrapServers.right = new FormAttachment( 78, 0 );
    wlBootstrapServers.setLayoutData( fdlBootstrapServers );

    wBootstrapServers = new TextVar( transMeta, wConnectionGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wBootstrapServers );
    wBootstrapServers.addModifyListener( lsMod );
    FormData fdBootstrapServers = new FormData();
    fdBootstrapServers.left = new FormAttachment( wlBootstrapServers, 0, SWT.LEFT );
    fdBootstrapServers.top = new FormAttachment( wlBootstrapServers, 5 );
    fdBootstrapServers.right = new FormAttachment( 78, 0 );
    wBootstrapServers.setLayoutData( fdBootstrapServers );

    Label wlClientId = new Label( wSetupComp, SWT.LEFT );
    props.setLook( wlClientId );
    wlClientId.setText( BaseMessages.getString( PKG, "KafkaProducerOutputDialog.ClientId" ) );
    FormData fdlClientId = new FormData();
    fdlClientId.left = new FormAttachment( 0, 0 );
    fdlClientId.top = new FormAttachment( wConnectionGroup, 10 );
    fdlClientId.right = new FormAttachment( 50, 0 );
    wlClientId.setLayoutData( fdlClientId );

    wClientId = new TextVar( transMeta, wSetupComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wClientId );
    wClientId.addModifyListener( lsMod );
    FormData fdClientId = new FormData();
    fdClientId.left = new FormAttachment( 0, 0 );
    fdClientId.top = new FormAttachment( wlClientId, 5 );
    fdClientId.right = new FormAttachment( 0, INPUT_WIDTH );
    wClientId.setLayoutData( fdClientId );

    Label wlTopic = new Label( wSetupComp, SWT.LEFT );
    props.setLook( wlTopic );
    wlTopic.setText( BaseMessages.getString( PKG, "KafkaProducerOutputDialog.Topic" ) );
    FormData fdlTopic = new FormData();
    fdlTopic.left = new FormAttachment( 0, 0 );
    fdlTopic.top = new FormAttachment( wClientId, 10 );
    fdlTopic.right = new FormAttachment( 50, 0 );
    wlTopic.setLayoutData( fdlTopic );

    wTopic = new ComboVar( transMeta, wSetupComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wTopic );
    wTopic.addModifyListener( lsMod );
    FormData fdTopic = new FormData();
    fdTopic.left = new FormAttachment( 0, 0 );
    fdTopic.top = new FormAttachment( wlTopic, 5 );
    fdTopic.right = new FormAttachment( 0, INPUT_WIDTH );
    wTopic.setLayoutData( fdTopic );
    wTopic.getCComboWidget().addListener(
      SWT.FocusIn,
      event -> {
        KafkaDialogHelper kafkaDialogHelper = getDialogHelper();
        kafkaDialogHelper.clusterNameChanged( event );
      } );
    Label wlKeyField = new Label( wSetupComp, SWT.LEFT );
    props.setLook( wlKeyField );
    wlKeyField.setText( BaseMessages.getString( PKG, "KafkaProducerOutputDialog.KeyField" ) );
    FormData fdlKeyField = new FormData();
    fdlKeyField.left = new FormAttachment( 0, 0 );
    fdlKeyField.top = new FormAttachment( wTopic, 10 );
    fdlKeyField.right = new FormAttachment( 50, 0 );
    wlKeyField.setLayoutData( fdlKeyField );

    wKeyField = new ComboVar( transMeta, wSetupComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wKeyField );
    wKeyField.addModifyListener( lsMod );
    FormData fdKeyField = new FormData();
    fdKeyField.left = new FormAttachment( 0, 0 );
    fdKeyField.top = new FormAttachment( wlKeyField, 5 );
    fdKeyField.right = new FormAttachment( 0, INPUT_WIDTH );
    wKeyField.setLayoutData( fdKeyField );
    Listener lsKeyFocus = e -> KafkaDialogHelper.populateFieldsList( transMeta, wKeyField, stepname );
    wKeyField.getCComboWidget().addListener( SWT.FocusIn, lsKeyFocus );

    Label wlMessageField = new Label( wSetupComp, SWT.LEFT );
    props.setLook( wlMessageField );
    wlMessageField.setText( BaseMessages.getString( PKG, "KafkaProducerOutputDialog.MessageField" ) );
    FormData fdlMessageField = new FormData();
    fdlMessageField.left = new FormAttachment( 0, 0 );
    fdlMessageField.top = new FormAttachment( wKeyField, 10 );
    fdlMessageField.right = new FormAttachment( 50, 0 );
    wlMessageField.setLayoutData( fdlMessageField );

    wMessageField = new ComboVar( transMeta, wSetupComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wMessageField );
    wMessageField.addModifyListener( lsMod );
    FormData fdMessageField = new FormData();
    fdMessageField.left = new FormAttachment( 0, 0 );
    fdMessageField.top = new FormAttachment( wlMessageField, 5 );
    fdMessageField.right = new FormAttachment( 0, INPUT_WIDTH );
    wMessageField.setLayoutData( fdMessageField );
    Listener lsMessageFocus = e -> KafkaDialogHelper.populateFieldsList( transMeta, wMessageField, stepname );
    wMessageField.getCComboWidget().addListener( SWT.FocusIn, lsMessageFocus );

    FormData fdSetupComp = new FormData();
    fdSetupComp.left = new FormAttachment( 0, 0 );
    fdSetupComp.top = new FormAttachment( 0, 0 );
    fdSetupComp.right = new FormAttachment( 100, 0 );
    fdSetupComp.bottom = new FormAttachment( 100, 0 );
    wSetupComp.setLayoutData( fdSetupComp );
    wSetupComp.layout();
    wSetupTab.setControl( wSetupComp );

    toggleConnectionType( true );
  }

  private void toggleConnectionType( final boolean isDirect ) {
    wlBootstrapServers.setVisible( isDirect );
    wBootstrapServers.setVisible( isDirect );
    wlClusterName.setVisible( !isDirect );
    wClusterName.setVisible( !isDirect );
  }

  private void buildOptionsTab() {
    CTabItem wOptionsTab = new CTabItem( wTabFolder, SWT.NONE );
    wOptionsTab.setText( BaseMessages.getString( PKG, "KafkaProducerOutputDialog.Options.Tab" ) );
    wOptionsComp = new Composite( wTabFolder, SWT.NONE );
    props.setLook( wOptionsComp );
    FormLayout fieldsLayout = new FormLayout();
    fieldsLayout.marginHeight = 15;
    fieldsLayout.marginWidth = 15;
    wOptionsComp.setLayout( fieldsLayout );

    FormData optionsFormData = new FormData();
    optionsFormData.left = new FormAttachment( 0, 0 );
    optionsFormData.top = new FormAttachment( wOptionsComp, 0 );
    optionsFormData.right = new FormAttachment( 100, 0 );
    optionsFormData.bottom = new FormAttachment( 100, 0 );
    wOptionsComp.setLayoutData( optionsFormData );

    buildOptionsTable( wOptionsComp );
    wOptionsComp.layout();
    wOptionsTab.setControl( wOptionsComp );
  }

  private void buildOptionsTable( Composite parentWidget ) {
    ColumnInfo[] columns = getOptionsColumns();

    if ( meta.getConfig().size() == 0 ) {
      // initial call
      List<String> list = KafkaDialogHelper.getProducerAdvancedConfigOptionNames();
      Map<String, String> advancedConfig = new LinkedHashMap<>();
      for ( String item : list ) {
        advancedConfig.put( item, DEFAULT_OPTION_VALUES.getOrDefault( item, "" ) );
      }
      meta.setConfig( advancedConfig );
    }
    int fieldCount = meta.getConfig().size();

    optionsTable = new TableView(
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

    optionsTable.setSortable( false );
    optionsTable.getTable().addListener( SWT.Resize, event -> {
      Table table = (Table) event.widget;
      table.getColumn( 1 ).setWidth( 220 );
      table.getColumn( 2 ).setWidth( 220 );
    } );

    populateOptionsData();

    FormData fdData = new FormData();
    fdData.left = new FormAttachment( 0, 0 );
    fdData.top = new FormAttachment( 0, 0 );
    fdData.right = new FormAttachment( 100, 0 );
    fdData.bottom = new FormAttachment( 100, 0 );

    // resize the columns to fit the data in them
    Arrays.stream( optionsTable.getTable().getColumns() ).forEach( column -> {
      if ( column.getWidth() > 0 ) {
        // don't pack anything with a 0 width, it will resize it to make it visible (like the index column)
        column.setWidth( 120 );
      }
    } );

    optionsTable.setLayoutData( fdData );
  }

  private ColumnInfo[] getOptionsColumns() {

    ColumnInfo optionName =
      new ColumnInfo( BaseMessages.getString( PKG, "KafkaProducerOutputDialog.Options.Column.Name" ),
        ColumnInfo.COLUMN_TYPE_TEXT, false, false );

    ColumnInfo value = new ColumnInfo( BaseMessages.getString( PKG, "KafkaProducerOutputDialog.Options.Column.Value" ),
      ColumnInfo.COLUMN_TYPE_TEXT, false, false );
    value.setUsingVariables( true );

    return new ColumnInfo[] { optionName, value };
  }

  private void populateOptionsData() {
    int rowIndex = 0;
    for ( Map.Entry<String, String> entry : meta.getConfig().entrySet() ) {
      TableItem key = optionsTable.getTable().getItem( rowIndex++ );
      key.setText( 1, entry.getKey() );
      key.setText( 2, entry.getValue() );
    }
  }

  private void setOptionsFromTable() {
    meta.setConfig( KafkaDialogHelper.getConfig( optionsTable ) );
  }

  private void getData() {
    if ( meta.getNamedClusterService() != null ) {
      try {
        List<String> names = meta.getNamedClusterService().listNames(Spoon.getInstance().getMetaStore());
        wClusterName.setItems(names.toArray(new String[0]));
      } catch (MetaStoreException e) {
        log.logError("Failed to get defined named clusters", e);
      }

      if (meta.getClusterName() != null) {
        wClusterName.setText(meta.getClusterName());
      }
    }
    if ( meta.getDirectBootstrapServers() != null ) {
      wBootstrapServers.setText( meta.getDirectBootstrapServers() );
    }

    if ( meta.getClientId() != null ) {
      wClientId.setText( meta.getClientId() );
    }
    if ( meta.getTopic() != null ) {
      wTopic.setText( meta.getTopic() );
    }
    if ( meta.getKeyField() != null ) {
      wKeyField.setText( meta.getKeyField() );
    }
    if ( meta.getMessageField() != null ) {
      wMessageField.setText( meta.getMessageField() );
    }
    wbCluster.setSelection( !isDirect() );
    wbDirect.setSelection( isDirect() );

    toggleConnectionType( isDirect() );
  }

  private boolean isDirect() {
    return DIRECT.equals( meta.getConnectionType() );
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

  protected void cancel() {
    meta.setChanged( false );
    dispose();
  }

  protected void ok() {
    stepname = wStepname.getText();
    meta.setClusterName( wClusterName.getText() );
    meta.setConnectionType( wbDirect.getSelection() ? DIRECT : CLUSTER );
    meta.setDirectBootstrapServers( wBootstrapServers.getText() );
    meta.setClientId( wClientId.getText() );
    meta.setTopic( wTopic.getText() );
    meta.setKeyField( wKeyField.getText() );
    meta.setMessageField( wMessageField.getText() );
    setOptionsFromTable();
    dispose();
  }

  public TextVar getwBootstrapServers() {
    return wBootstrapServers;
  }

  public Button getWbCluster() {
    return wbCluster;
  }

  public ComboVar getwClusterName() {
    return wClusterName;
  }

  public ComboVar getwTopic() {
    return wTopic;
  }

  public TableView getOptionsTable() {
    return optionsTable;
  }

  public CTabFolder getwTabFolder() {
    return wTabFolder;
  }

  protected KafkaDialogHelper getDialogHelper() {
    KafkaDialogHelper helper = new KafkaDialogHelper(
      wClusterName, wTopic, wbCluster, wBootstrapServers, kafkaFactory, meta.getNamedClusterService(),
      meta.getMetastoreLocator(), optionsTable, meta.getParentStepMeta() );
    return helper;
  }

}
