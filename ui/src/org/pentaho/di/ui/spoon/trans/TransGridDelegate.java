/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2013 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.ui.spoon.trans;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.ToolBar;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Props;
import org.pentaho.di.engine.api.IOperation;
import org.pentaho.di.engine.api.reporting.Status;
import org.pentaho.di.engine.api.reporting.Metrics;
import org.pentaho.di.engine.kettleclassic.ClassicKettleMetrics;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.i18n.GlobalMessages;
import org.pentaho.di.trans.step.BaseStepData.StepExecutionStatus;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.ui.spoon.XulSpoonSettingsManager;
import org.pentaho.di.ui.spoon.delegates.SpoonDelegate;
import org.pentaho.di.ui.xul.KettleXulLoader;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.XulLoader;
import org.pentaho.ui.xul.containers.XulToolbar;
import org.pentaho.ui.xul.impl.XulEventHandler;
import org.pentaho.ui.xul.swt.tags.SwtToolbarbutton;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class TransGridDelegate extends SpoonDelegate implements XulEventHandler {
  private static Class<?> PKG = Spoon.class; // for i18n purposes, needed by Translator2!!

  private static final String XUL_FILE_TRANS_GRID_TOOLBAR = "ui/trans-grid-toolbar.xul";

  public static final long REFRESH_TIME = 100L;

  public static final long UPDATE_TIME_VIEW = 1000L;

  private TransGraph transGraph;

  private CTabItem transGridTab;

  private TableView transGridView;

  private XulToolbar toolbar;

  private Composite transGridComposite;

  private boolean hideInactiveSteps;

  private boolean showSelectedSteps;

  // These 2 maps should only be used within the Swt thread.
  // Used to handle grid updates that are independent of execution events (like show-hide inactive).
  private Map<String, Status> stepStatus = new HashMap<>();
  private Map<String, Metrics> stepMetrics = new HashMap<>();

  private static final Map<Status, StepExecutionStatus> statusMap = ImmutableMap.of(
    Status.RUNNING, StepExecutionStatus.STATUS_RUNNING,
    Status.STOPPED, StepExecutionStatus.STATUS_STOPPED );

  /**
   * @param spoon
   * @param transGraph
   */
  public TransGridDelegate( Spoon spoon, TransGraph transGraph ) {
    super( spoon );
    this.transGraph = transGraph;

    hideInactiveSteps = false;
  }

  public void showGridView() {

    if ( transGridTab == null || transGridTab.isDisposed() ) {
      addTransGrid();
    } else {
      transGridTab.dispose();

      transGraph.checkEmptyExtraView();
    }
  }

  /**
   * Add a grid with the execution metrics per step in a table view
   */
  public void addTransGrid() {

    // First, see if we need to add the extra view...
    //
    if ( transGraph.extraViewComposite == null || transGraph.extraViewComposite.isDisposed() ) {
      transGraph.addExtraView();
    } else {
      if ( transGridTab != null && !transGridTab.isDisposed() ) {
        // just set this one active and get out...
        //
        transGraph.extraViewTabFolder.setSelection( transGridTab );
        return;
      }
    }

    transGridTab = new CTabItem( transGraph.extraViewTabFolder, SWT.NONE );
    transGridTab.setImage( GUIResource.getInstance().getImageShowGrid() );
    transGridTab.setText( BaseMessages.getString( PKG, "Spoon.TransGraph.GridTab.Name" ) );

    transGridComposite = new Composite( transGraph.extraViewTabFolder, SWT.NONE );
    transGridComposite.setLayout( new FormLayout() );

    addToolBar();

    Control toolbarControl = (Control) toolbar.getManagedObject();

    toolbarControl.setLayoutData( new FormData() );
    FormData fd = new FormData();
    fd.left = new FormAttachment( 0, 0 ); // First one in the left top corner
    fd.top = new FormAttachment( 0, 0 );
    fd.right = new FormAttachment( 100, 0 );
    toolbarControl.setLayoutData( fd );

    toolbarControl.setParent( transGridComposite );

    ColumnInfo[] colinf =
      new ColumnInfo[] {
        new ColumnInfo(
          BaseMessages.getString( PKG, "TransLog.Column.Stepname" ), ColumnInfo.COLUMN_TYPE_TEXT, false,
          true ),
        new ColumnInfo(
          BaseMessages.getString( PKG, "TransLog.Column.Copynr" ), ColumnInfo.COLUMN_TYPE_TEXT, false, true ),
        new ColumnInfo(
          BaseMessages.getString( PKG, "TransLog.Column.Read" ), ColumnInfo.COLUMN_TYPE_TEXT, false, true ),
        new ColumnInfo(
          BaseMessages.getString( PKG, "TransLog.Column.Written" ), ColumnInfo.COLUMN_TYPE_TEXT, false, true ),
        new ColumnInfo(
          BaseMessages.getString( PKG, "TransLog.Column.Input" ), ColumnInfo.COLUMN_TYPE_TEXT, false, true ),
        new ColumnInfo(
          BaseMessages.getString( PKG, "TransLog.Column.Output" ), ColumnInfo.COLUMN_TYPE_TEXT, false, true ),
        new ColumnInfo(
          BaseMessages.getString( PKG, "TransLog.Column.Updated" ), ColumnInfo.COLUMN_TYPE_TEXT, false, true ),
        new ColumnInfo(
          BaseMessages.getString( PKG, "TransLog.Column.Rejected" ), ColumnInfo.COLUMN_TYPE_TEXT, false,
          true ),
        new ColumnInfo(
          BaseMessages.getString( PKG, "TransLog.Column.Errors" ), ColumnInfo.COLUMN_TYPE_TEXT, false, true ),
        new ColumnInfo(
          BaseMessages.getString( PKG, "TransLog.Column.Active" ), ColumnInfo.COLUMN_TYPE_TEXT, false, true ),
        new ColumnInfo(
          BaseMessages.getString( PKG, "TransLog.Column.Time" ), ColumnInfo.COLUMN_TYPE_TEXT, false, true ),
        new ColumnInfo(
          BaseMessages.getString( PKG, "TransLog.Column.Speed" ), ColumnInfo.COLUMN_TYPE_TEXT, false, true ),
        new ColumnInfo(
          BaseMessages.getString( PKG, "TransLog.Column.PriorityBufferSizes" ), ColumnInfo.COLUMN_TYPE_TEXT,
          false, true ), };

    colinf[ 1 ].setAllignement( SWT.RIGHT );
    colinf[ 2 ].setAllignement( SWT.RIGHT );
    colinf[ 3 ].setAllignement( SWT.RIGHT );
    colinf[ 4 ].setAllignement( SWT.RIGHT );
    colinf[ 5 ].setAllignement( SWT.RIGHT );
    colinf[ 6 ].setAllignement( SWT.RIGHT );
    colinf[ 7 ].setAllignement( SWT.RIGHT );
    colinf[ 8 ].setAllignement( SWT.RIGHT );
    colinf[ 9 ].setAllignement( SWT.LEFT );
    colinf[ 10 ].setAllignement( SWT.RIGHT );
    colinf[ 11 ].setAllignement( SWT.RIGHT );
    colinf[ 12 ].setAllignement( SWT.RIGHT );

    transGridView = new TableView( transGraph.getManagedObject(), transGridComposite, SWT.BORDER
      | SWT.FULL_SELECTION | SWT.MULTI, colinf, 1,
      true, // readonly!
      null, // Listener
      spoon.props );
    FormData fdView = new FormData();
    fdView.left = new FormAttachment( 0, 0 );
    fdView.right = new FormAttachment( 100, 0 );
    fdView.top = new FormAttachment( (Control) toolbar.getManagedObject(), 0 );
    fdView.bottom = new FormAttachment( 100, 0 );
    transGridView.setLayoutData( fdView );

    transGridTab.setControl( transGridComposite );

    transGraph.extraViewTabFolder.setSelection( transGridTab );
  }

  private void addToolBar() {

    try {
      XulLoader loader = new KettleXulLoader();
      loader.setSettingsManager( XulSpoonSettingsManager.getInstance() );
      ResourceBundle bundle = GlobalMessages.getBundle( "org/pentaho/di/ui/spoon/messages/messages" );
      XulDomContainer xulDomContainer = loader.loadXul( XUL_FILE_TRANS_GRID_TOOLBAR, bundle );
      xulDomContainer.addEventHandler( this );
      toolbar = (XulToolbar) xulDomContainer.getDocumentRoot().getElementById( "nav-toolbar" );

      ToolBar swtToolBar = (ToolBar) toolbar.getManagedObject();
      spoon.props.setLook( swtToolBar, Props.WIDGET_STYLE_TOOLBAR );
      swtToolBar.layout( true, true );
    } catch ( Throwable t ) {
      log.logError( toString(), Const.getStackTracker( t ) );
      new ErrorDialog( transGridComposite.getShell(),
        BaseMessages.getString( PKG, "Spoon.Exception.ErrorReadingXULFile.Title" ),
        BaseMessages.getString( PKG, "Spoon.Exception.ErrorReadingXULFile.Message", XUL_FILE_TRANS_GRID_TOOLBAR ),
        new Exception( t ) );
    }
  }

  public void showHideInactive() {
    hideInactiveSteps = !hideInactiveSteps;

    SwtToolbarbutton onlyActiveButton = (SwtToolbarbutton) toolbar.getElementById( "show-inactive" );
    if ( onlyActiveButton != null ) {
      onlyActiveButton.setSelected( hideInactiveSteps );
      if ( hideInactiveSteps ) {
        onlyActiveButton.setImage( GUIResource.getInstance().getImageHideInactive() );
      } else {
        onlyActiveButton.setImage( GUIResource.getInstance().getImageShowInactive() );
      }
    }
    // TODO This doesn't persist ordering.
    List<String> stepsToShow = stepStatus.keySet().stream()
      .filter( k -> !( hideInactiveSteps && stepStatus.get( k ).equals( Status.STOPPED ) ) )
      .collect( Collectors.toList() );
    if ( stepsToShow.size() != transGridView.table.getItemCount() ) {
      transGridView.table.removeAll(); // table changing.  Clear out old items and reconstruct.
      stepsToShow.stream()
        .forEach( step -> {
          TableItem tableItemForOp = getTableItemForOp( step );
          applyMetrics( stepMetrics.get( step ), tableItemForOp );
          applyStatus( stepStatus.get( step), tableItemForOp );
          tableUpdate();
        } );
    }




  }

  //  TODO:  Doesn't appear to be used.  Yank.
  public void showHideSelected() {
    showSelectedSteps = !showSelectedSteps;

    SwtToolbarbutton onlySelectedButton = (SwtToolbarbutton) toolbar.getElementById( "show-selected" );
    if ( onlySelectedButton != null ) {
      onlySelectedButton.setSelected( showSelectedSteps );
      if ( showSelectedSteps ) {
        onlySelectedButton.setImage( GUIResource.getInstance().getImageShowSelected() );
      } else {
        onlySelectedButton.setImage( GUIResource.getInstance().getImageShowAll() );
      }
    }
  }


  public CTabItem getTransGridTab() {
    return transGridTab;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.ui.xul.impl.XulEventHandler#getData()
   */
  public Object getData() {
    // TODO Auto-generated method stub
    return null;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.ui.xul.impl.XulEventHandler#getName()
   */
  public String getName() {
    return "transgrid";
  }

  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.ui.xul.impl.XulEventHandler#getXulDomContainer()
   */
  public XulDomContainer getXulDomContainer() {
    // TODO Auto-generated method stub
    return null;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.ui.xul.impl.XulEventHandler#setData(java.lang.Object)
   */
  public void setData( Object data ) {
    // TODO Auto-generated method stub

  }

  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.ui.xul.impl.XulEventHandler#setName(java.lang.String)
   */
  public void setName( String name ) {
    // TODO Auto-generated method stub

  }

  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.ui.xul.impl.XulEventHandler#setXulDomContainer(org.pentaho.ui.xul.XulDomContainer)
   */
  public void setXulDomContainer( XulDomContainer xulDomContainer ) {
    // TODO Auto-generated method stub

  }

  public void rowMetricEvent( IOperation op, Metrics metric ) {
    inSwtThread( () -> {
      stepMetrics.put( op.getId(), metric );
      if ( stepStatus.getOrDefault( op.getId(), Status.RUNNING ).equals( Status.STOPPED ) && hideInactiveSteps ) {
        return;
      }
      TableItem tableItem = getTableItemForOp( op.getId() );
      applyMetrics( metric, tableItem );
      tableUpdate();
    } );
  }

  public void statusEvent( IOperation op, Status status ) {
    inSwtThread( () -> {
      stepStatus.put( op.getId(), status );
      TableItem item = getTableItemForOp( op.getId() );
      applyStatus( status, item );
    } );
  }

  private void applyMetrics( Metrics metric, TableItem item ) {
    Preconditions.checkArgument( metric != null  && item != null );
    item.setText( 5, Long.toString( metric.getIn() ) );
    item.setText( 6, Long.toString( metric.getOut() ) );
    maybeApplyClassicKettleMetrics( metric, item );
  }

  private void maybeApplyClassicKettleMetrics( Metrics metric, TableItem item ) {
    Optional<ClassicKettleMetrics> kettleMetrics = metric.unwrap( ClassicKettleMetrics.class );
    if ( kettleMetrics.isPresent() ) {
      item.setText( 2, Long.toString( kettleMetrics.get().getCopy() ) );
      item.setText( 3, Long.toString( kettleMetrics.get().getRead() ) );
      item.setText( 4, Long.toString( kettleMetrics.get().getWritten() ) );
      item.setText( 7, Long.toString( kettleMetrics.get().getUpdated() ) );
      item.setText( 8, Long.toString( kettleMetrics.get().getRejected() ) );
      item.setText( 9, Long.toString( kettleMetrics.get().getErrors() ) );
    }
  }

  private void applyStatus( Status status, TableItem item ) {
    Preconditions.checkArgument( status != null && item != null );
    item.setText( 10, statusMap.get( status ).toString() );
    if ( Status.FAILED.equals( status ) ) {
      setError( item );
    }
  }

  private void setError( TableItem item ) {
    item.setBackground( GUIResource.getInstance().getColorRed() );
  }

  private void tableUpdate() {
    // Update row numbers
    Arrays.stream( transGridView.table.getItems() )
      .filter( tableItem -> tableItem.getText( 0 ).isEmpty() )
      .forEach( tableItem -> {
        tableItem.setText( 0, Integer.toString( tableItemRowNum( tableItem ) ) );
        setBackground( tableItem );
      } );
  }

  private int tableItemRowNum( TableItem tableItem ) {
    return transGridView.table.indexOf( tableItem ) + 1;
  }

  private void setBackground( TableItem tableItem ) {
    int rowNum = tableItemRowNum( tableItem );
    if ( tableItem.getBackground().equals( GUIResource.getInstance().getColorRed() ) ) {
      // error row, leave as is
      return;
    }
    if ( rowNum % 2 == 0 ) {
      tableItem.setBackground( GUIResource.getInstance().getColorWhite() );
    } else {
      tableItem.setBackground( GUIResource.getInstance().getColorBlueCustomGrid() );
    }
  }

  private void inSwtThread( Runnable runnable ) {
    spoon.getDisplay().asyncExec( () -> runnable.run() );
  }

  private TableItem getTableItemForOp( String opName ) {
    TableItem ti = Arrays.stream( transGridView.table.getItems() )
      .filter( tableItem -> ( tableItemMatchesOp( opName, tableItem ) || onlyPlaceholderRowPresent( tableItem ) ) )
      .findFirst()
      .orElseGet( () -> new TableItem( transGridView.table, SWT.NONE ) );
    ti.setText( 1, opName );
    return ti;
  }

  private boolean tableItemMatchesOp( String stepName, TableItem tableItem ) {
    return stepName.equals( tableItem.getText( 1 ) );
  }

  private boolean onlyPlaceholderRowPresent( TableItem tableItem ) {
    return tableItem.getText( 1 ).isEmpty() && transGridView.table.getItemCount() == 1;
  }
}
