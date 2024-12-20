/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.ui.cluster.dialog;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.cluster.ClusterSchema;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.Const;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.dialog.EnterSelectionDialog;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.gui.WindowProperty;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.ui.util.DialogUtils;

/**
 *
 * Dialog that allows you to edit the settings of the cluster schema
 *
 * @see ClusterSchema
 * @author Matt
 * @since 17-11-2006
 *
 */

public class ClusterSchemaDialog extends Dialog {
  private static Class<?> PKG = ClusterSchemaDialog.class; // for i18n purposes, needed by Translator2!!

  // private static LogWriter log = LogWriter.getInstance();

  private ClusterSchema clusterSchema;

  private Collection<ClusterSchema> existingSchemas;

  private Shell shell;

  // Name
  private Text wName;

  // Servers
  private TableView wServers;

  private Button wOK, wCancel;

  private ModifyListener lsMod;

  private PropsUI props;

  private int middle;
  private int margin;

  private ClusterSchema originalSchema;
  private boolean ok;

  private Button wSelect;

  private TextVar wPort;

  private TextVar wBufferSize;

  private TextVar wFlushInterval;

  private Button wCompressed;

  private Button wDynamic;

  private List<SlaveServer> slaveServers;

  public ClusterSchemaDialog( Shell par, ClusterSchema clusterSchema, Collection<ClusterSchema> existingSchemas,
      List<SlaveServer> slaveServers ) {
    super( par, SWT.NONE );
    this.clusterSchema = clusterSchema.clone();
    this.originalSchema = clusterSchema;
    this.existingSchemas = existingSchemas;
    this.slaveServers = slaveServers;

    props = PropsUI.getInstance();
    ok = false;
  }

  public ClusterSchemaDialog( Shell par, ClusterSchema clusterSchema, List<SlaveServer> slaveServers ) {
    this( par, clusterSchema, Collections.<ClusterSchema>emptyList(), slaveServers );
  }

  public boolean open() {
    Shell parent = getParent();
    shell = new Shell( parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN );
    props.setLook( shell );
    shell.setImage( GUIResource.getInstance().getImageCluster() );

    lsMod = new ModifyListener() {
      public void modifyText( ModifyEvent e ) {
        clusterSchema.setChanged();
      }
    };

    middle = props.getMiddlePct();
    margin = Const.MARGIN;

    FormLayout formLayout = new FormLayout();
    formLayout.marginWidth = Const.FORM_MARGIN;
    formLayout.marginHeight = Const.FORM_MARGIN;

    shell.setText( BaseMessages.getString( PKG, "ClusterSchemaDialog.Shell.Title" ) );
    shell.setLayout( formLayout );

    // First, add the buttons...

    // Buttons
    wOK = new Button( shell, SWT.PUSH );
    wOK.setText( BaseMessages.getString( PKG, "System.Button.OK" ) );

    wCancel = new Button( shell, SWT.PUSH );
    wCancel.setText( BaseMessages.getString( PKG, "System.Button.Cancel" ) );

    Button[] buttons = new Button[] { wOK, wCancel };
    BaseStepDialog.positionBottomButtons( shell, buttons, margin, null );

    // The rest stays above the buttons, so we added those first...

    // What's the schema name??
    Label wlName = new Label( shell, SWT.RIGHT );
    props.setLook( wlName );
    wlName.setText( BaseMessages.getString( PKG, "ClusterSchemaDialog.Schema.Label" ) );
    FormData fdlName = new FormData();
    fdlName.top = new FormAttachment( 0, 0 );
    fdlName.left = new FormAttachment( 0, 0 ); // First one in the left top corner
    fdlName.right = new FormAttachment( middle, 0 );
    wlName.setLayoutData( fdlName );

    wName = new Text( shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wName );
    wName.addModifyListener( lsMod );
    FormData fdName = new FormData();
    fdName.top = new FormAttachment( 0, 0 );
    fdName.left = new FormAttachment( middle, margin ); // To the right of the label
    fdName.right = new FormAttachment( 95, 0 );
    wName.setLayoutData( fdName );

    // What's the base port??
    Label wlPort = new Label( shell, SWT.RIGHT );
    props.setLook( wlPort );
    wlPort.setText( BaseMessages.getString( PKG, "ClusterSchemaDialog.Port.Label" ) );
    FormData fdlPort = new FormData();
    fdlPort.top = new FormAttachment( wName, margin );
    fdlPort.left = new FormAttachment( 0, 0 ); // First one in the left top corner
    fdlPort.right = new FormAttachment( middle, 0 );
    wlPort.setLayoutData( fdlPort );

    wPort = new TextVar( clusterSchema, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wPort );
    wPort.addModifyListener( lsMod );
    FormData fdPort = new FormData();
    fdPort.top = new FormAttachment( wName, margin );
    fdPort.left = new FormAttachment( middle, margin ); // To the right of the label
    fdPort.right = new FormAttachment( 95, 0 );
    wPort.setLayoutData( fdPort );

    // What are the sockets buffer sizes??
    Label wlBufferSize = new Label( shell, SWT.RIGHT );
    props.setLook( wlBufferSize );
    wlBufferSize.setText( BaseMessages.getString( PKG, "ClusterSchemaDialog.SocketBufferSize.Label" ) );
    FormData fdlBufferSize = new FormData();
    fdlBufferSize.top = new FormAttachment( wPort, margin );
    fdlBufferSize.left = new FormAttachment( 0, 0 ); // First one in the left top corner
    fdlBufferSize.right = new FormAttachment( middle, 0 );
    wlBufferSize.setLayoutData( fdlBufferSize );

    wBufferSize = new TextVar( clusterSchema, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wBufferSize );
    wBufferSize.addModifyListener( lsMod );
    FormData fdBufferSize = new FormData();
    fdBufferSize.top = new FormAttachment( wPort, margin );
    fdBufferSize.left = new FormAttachment( middle, margin ); // To the right of the label
    fdBufferSize.right = new FormAttachment( 95, 0 );
    wBufferSize.setLayoutData( fdBufferSize );

    // What are the sockets buffer sizes??
    Label wlFlushInterval = new Label( shell, SWT.RIGHT );
    props.setLook( wlFlushInterval );
    wlFlushInterval.setText( BaseMessages.getString( PKG, "ClusterSchemaDialog.SocketFlushRows.Label" ) );
    FormData fdlFlushInterval = new FormData();
    fdlFlushInterval.top = new FormAttachment( wBufferSize, margin );
    fdlFlushInterval.left = new FormAttachment( 0, 0 ); // First one in the left top corner
    fdlFlushInterval.right = new FormAttachment( middle, 0 );
    wlFlushInterval.setLayoutData( fdlFlushInterval );

    wFlushInterval = new TextVar( clusterSchema, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wFlushInterval );
    wFlushInterval.addModifyListener( lsMod );
    FormData fdFlushInterval = new FormData();
    fdFlushInterval.top = new FormAttachment( wBufferSize, margin );
    fdFlushInterval.left = new FormAttachment( middle, margin ); // To the right of the label
    fdFlushInterval.right = new FormAttachment( 95, 0 );
    wFlushInterval.setLayoutData( fdFlushInterval );

    // What are the sockets buffer sizes??
    Label wlCompressed = new Label( shell, SWT.RIGHT );
    props.setLook( wlCompressed );
    wlCompressed.setText( BaseMessages.getString( PKG, "ClusterSchemaDialog.SocketDataCompressed.Label" ) );
    FormData fdlCompressed = new FormData();
    fdlCompressed.top = new FormAttachment( wFlushInterval, margin );
    fdlCompressed.left = new FormAttachment( 0, 0 ); // First one in the left top corner
    fdlCompressed.right = new FormAttachment( middle, 0 );
    wlCompressed.setLayoutData( fdlCompressed );

    wCompressed = new Button( shell, SWT.CHECK );
    props.setLook( wCompressed );
    FormData fdCompressed = new FormData();
    fdCompressed.top = new FormAttachment( wFlushInterval, margin );
    fdCompressed.left = new FormAttachment( middle, margin ); // To the right of the label
    fdCompressed.right = new FormAttachment( 95, 0 );
    wCompressed.setLayoutData( fdCompressed );

    // What are the sockets buffer sizes??
    Label wlDynamic = new Label( shell, SWT.RIGHT );
    wlDynamic.setToolTipText( BaseMessages.getString( PKG, "ClusterSchemaDialog.DynamicCluster.Tooltip" ) );
    props.setLook( wlDynamic );
    wlDynamic.setText( BaseMessages.getString( PKG, "ClusterSchemaDialog.DynamicCluster.Label" ) );
    FormData fdlDynamic = new FormData();
    fdlDynamic.top = new FormAttachment( wCompressed, margin );
    fdlDynamic.left = new FormAttachment( 0, 0 ); // First one in the left top corner
    fdlDynamic.right = new FormAttachment( middle, 0 );
    wlDynamic.setLayoutData( fdlDynamic );

    wDynamic = new Button( shell, SWT.CHECK );
    wDynamic.setToolTipText( BaseMessages.getString( PKG, "ClusterSchemaDialog.DynamicCluster.Tooltip" ) );
    props.setLook( wDynamic );
    FormData fdDynamic = new FormData();
    fdDynamic.top = new FormAttachment( wCompressed, margin );
    fdDynamic.left = new FormAttachment( middle, margin ); // To the right of the label
    fdDynamic.right = new FormAttachment( 95, 0 );
    wDynamic.setLayoutData( fdDynamic );

    // Schema servers:
    Label wlServers = new Label( shell, SWT.RIGHT );
    wlServers.setText( BaseMessages.getString( PKG, "ClusterSchemaDialog.SlaveServers.Label" ) );
    props.setLook( wlServers );
    FormData fdlServers = new FormData();
    fdlServers.left = new FormAttachment( 0, 0 );
    fdlServers.right = new FormAttachment( middle, 0 );
    fdlServers.top = new FormAttachment( wDynamic, margin );
    wlServers.setLayoutData( fdlServers );

    // Some buttons to manage...
    wSelect = new Button( shell, SWT.PUSH );
    wSelect.setText( BaseMessages.getString( PKG, "ClusterSchemaDialog.SelectSlaveServers.Label" ) );
    props.setLook( wSelect );
    FormData fdSelect = new FormData();
    fdSelect.right = new FormAttachment( 100, 0 );
    fdSelect.top = new FormAttachment( wlServers, 5 * margin );
    wSelect.setLayoutData( fdSelect );
    wSelect.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e ) {
        selectSlaveServers();
      }
    } );

    ColumnInfo[] partitionColumns =
      new ColumnInfo[] {
        new ColumnInfo(
          BaseMessages.getString( PKG, "ClusterSchemaDialog.ColumnInfoName.Label" ),
          ColumnInfo.COLUMN_TYPE_TEXT, true, false ),
        new ColumnInfo(
          BaseMessages.getString( PKG, "ClusterSchemaDialog.ColumnInfoServiceURL.Label" ),
          ColumnInfo.COLUMN_TYPE_TEXT, true, true ),
        new ColumnInfo(
          BaseMessages.getString( PKG, "ClusterSchemaDialog.ColumnInfoMaster.Label" ),
          ColumnInfo.COLUMN_TYPE_TEXT, true, true ), };
    wServers =
      new TableView(
        clusterSchema, shell, SWT.BORDER | SWT.FULL_SELECTION | SWT.SINGLE, partitionColumns, 1, lsMod, props );
    wServers.setReadonly( false );
    props.setLook( wServers );
    FormData fdServers = new FormData();
    fdServers.left = new FormAttachment( middle, margin );
    fdServers.right = new FormAttachment( wSelect, -2 * margin );
    fdServers.top = new FormAttachment( wDynamic, margin );
    fdServers.bottom = new FormAttachment( wOK, -margin * 2 );
    wServers.setLayoutData( fdServers );
    wServers.table.addSelectionListener( new SelectionAdapter() {
      public void widgetDefaultSelected( SelectionEvent e ) {
        editSlaveServer();
      }
    } );

    // Add listeners
    wOK.addListener( SWT.Selection, new Listener() {
      public void handleEvent( Event e ) {
        ok();
      }
    } );
    wCancel.addListener( SWT.Selection, new Listener() {
      public void handleEvent( Event e ) {
        cancel();
      }
    } );

    SelectionAdapter selAdapter = new SelectionAdapter() {
      public void widgetDefaultSelected( SelectionEvent e ) {
        ok();
      }
    };
    wName.addSelectionListener( selAdapter );
    wPort.addSelectionListener( selAdapter );
    wBufferSize.addSelectionListener( selAdapter );
    wFlushInterval.addSelectionListener( selAdapter );

    // Detect X or ALT-F4 or something that kills this window...
    shell.addShellListener( new ShellAdapter() {
      public void shellClosed( ShellEvent e ) {
        cancel();
      }
    } );

    getData();

    BaseStepDialog.setSize( shell );

    shell.open();
    Display display = parent.getDisplay();
    while ( !shell.isDisposed() ) {
      if ( !display.readAndDispatch() ) {
        display.sleep();
      }
    }
    return ok;
  }

  private void editSlaveServer() {
    int idx = wServers.getSelectionIndex();
    if ( idx >= 0 ) {
      SlaveServer slaveServer = clusterSchema.findSlaveServer( wServers.getItems( 0 )[idx] );
      if ( slaveServer != null ) {
        SlaveServerDialog dialog = new SlaveServerDialog( shell, slaveServer, slaveServers );
        if ( dialog.open() ) {
          refreshSlaveServers();
        }
      }
    }
  }

  private void selectSlaveServers() {
    String[] names = SlaveServer.getSlaveServerNames( slaveServers );
    int[] idx = Const.indexsOfFoundStrings( wServers.getItems( 0 ), names );

    EnterSelectionDialog dialog =
      new EnterSelectionDialog( shell, names,
        BaseMessages.getString( PKG, "ClusterSchemaDialog.SelectServers.Label" ),
        BaseMessages.getString( PKG, "ClusterSchemaDialog.SelectServersCluster.Label" ) );
    dialog.setAvoidQuickSearch();
    dialog.setSelectedNrs( idx );
    dialog.setMulti( true );
    if ( dialog.open() != null ) {
      clusterSchema.getSlaveServers().clear();
      int[] indeces = dialog.getSelectionIndeces();
      for ( int i = 0; i < indeces.length; i++ ) {
        SlaveServer slaveServer = SlaveServer.findSlaveServer( slaveServers, names[indeces[i]] );
        clusterSchema.getSlaveServers().add( slaveServer );
      }

      refreshSlaveServers();
    }
  }

  public void dispose() {
    props.setScreen( new WindowProperty( shell ) );
    shell.dispose();
  }

  public void getData() {
    wName.setText( Const.NVL( clusterSchema.getName(), "" ) );
    wPort.setText( Const.NVL( clusterSchema.getBasePort(), "" ) );
    wBufferSize.setText( Const.NVL( clusterSchema.getSocketsBufferSize(), "" ) );
    wFlushInterval.setText( Const.NVL( clusterSchema.getSocketsFlushInterval(), "" ) );
    wCompressed.setSelection( clusterSchema.isSocketsCompressed() );
    wDynamic.setSelection( clusterSchema.isDynamic() );

    refreshSlaveServers();

    wName.setFocus();
  }

  private void refreshSlaveServers() {
    wServers.clearAll( false );
    List<SlaveServer> slServers = clusterSchema.getSlaveServers();
    for ( int i = 0; i < slServers.size(); i++ ) {
      TableItem item = new TableItem( wServers.table, SWT.NONE );
      SlaveServer slaveServer = slServers.get( i );
      item.setText( 1, Const.NVL( slaveServer.getName(), "" ) );
      item.setText( 2, Const.NVL( slaveServer.toString(), "" ) );
      item.setText( 3, slaveServer.isMaster() ? "Y" : "N" );
    }
    wServers.removeEmptyRows();
    wServers.setRowNums();
    wServers.optWidth( true );
  }

  private void cancel() {
    originalSchema = null;
    dispose();
  }

  public void ok() {
    getInfo();

    clusterSchema.setName( clusterSchema.getName().trim() );
    if ( !clusterSchema.getName().equals( originalSchema.getName() ) ) {
      DialogUtils.removeMatchingObject( originalSchema.getName(), existingSchemas );
      if ( DialogUtils.objectWithTheSameNameExists( clusterSchema, existingSchemas ) ) {
        String title = BaseMessages.getString( PKG, "ClusterSchemaDialog.ClusterSchemaNameExists.Title" );
        String message =
            BaseMessages.getString( PKG, "ClusterSchemaDialog.ClusterSchemaNameExists", clusterSchema.getName() );
        String okButton = BaseMessages.getString( PKG, "System.Button.OK" );
        MessageDialog dialog =
            new MessageDialog( shell, title, null, message, MessageDialog.ERROR, new String[] { okButton }, 0 );

        dialog.open();
        return;
      }
    }

    originalSchema.setName( clusterSchema.getName() );
    originalSchema.setBasePort( clusterSchema.getBasePort() );
    originalSchema.setSocketsBufferSize( clusterSchema.getSocketsBufferSize() );
    originalSchema.setSocketsFlushInterval( clusterSchema.getSocketsFlushInterval() );
    originalSchema.setSocketsCompressed( clusterSchema.isSocketsCompressed() );
    originalSchema.setDynamic( clusterSchema.isDynamic() );
    originalSchema.setSlaveServers( clusterSchema.getSlaveServers() );
    originalSchema.setChanged();

    ok = true;

    // Debug: dynamic lis names/urls of slaves on the console
    //
    /*
     * if (originalSchema.isDynamic()) { // Find a master that is available // List<SlaveServer> dynamicSlaves = null;
     * for (SlaveServer slave : originalSchema.getSlaveServers()) { if (slave.isMaster() && dynamicSlaves==null) { try {
     * List<SlaveServerDetection> detections = slave.getSlaveServerDetections(); dynamicSlaves = new
     * ArrayList<SlaveServer>(); for (SlaveServerDetection detection : detections) { if (detection.isActive()) {
     * dynamicSlaves.add(detection.getSlaveServer());
     * logBasic("Found dynamic slave : "+detection.getSlaveServer().getName
     * ()+" --> "+detection.getSlaveServer().getServerAndPort()); } } } catch (Exception e) {
     * logError("Unable to contact master : "+slave.getName()+" --> "+slave.getServerAndPort(), e); } } } }
     */

    dispose();
  }

  private void getInfo() {
    clusterSchema.setName( wName.getText() );
    clusterSchema.setBasePort( wPort.getText() );
    clusterSchema.setSocketsBufferSize( wBufferSize.getText() );
    clusterSchema.setSocketsFlushInterval( wFlushInterval.getText() );
    clusterSchema.setSocketsCompressed( wCompressed.getSelection() );
    clusterSchema.setDynamic( wDynamic.getSelection() );

    String[] names = SlaveServer.getSlaveServerNames( slaveServers );
    int[] idx = Const.indexsOfFoundStrings( wServers.getItems( 0 ), names );

    clusterSchema.getSlaveServers().clear();
    for ( int i = 0; i < idx.length; i++ ) {
      SlaveServer slaveServer = SlaveServer.findSlaveServer( slaveServers, names[idx[i]] );
      clusterSchema.getSlaveServers().add( slaveServer );
    }

  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }
}
