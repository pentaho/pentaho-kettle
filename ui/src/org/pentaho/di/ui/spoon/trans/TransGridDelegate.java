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
import org.pentaho.di.ui.spoon.trans.executionstate.api.ExecutionState;
import org.pentaho.di.ui.spoon.trans.executionstate.api.ExecutionStateEvent;
import org.pentaho.di.ui.spoon.trans.executionstate.api.ExecutionStateSubscriber;
import org.pentaho.di.ui.xul.KettleXulLoader;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.XulLoader;
import org.pentaho.ui.xul.containers.XulToolbar;
import org.pentaho.ui.xul.impl.XulEventHandler;
import org.pentaho.ui.xul.swt.tags.SwtToolbarbutton;

import java.util.List;
import java.util.ResourceBundle;

import static org.pentaho.di.ui.spoon.trans.executionstate.api.ExecutionState.StepState.StepStateField.*;

public class TransGridDelegate extends SpoonDelegate implements XulEventHandler, ExecutionStateSubscriber {
  private static Class<?> PKG = Spoon.class; // for i18n purposes, needed by Translator2!!

  private static final String XUL_FILE_TRANS_GRID_TOOLBAR = "ui/trans-grid-toolbar.xul";

  public static final long REFRESH_TIME = 100L;

  public static final long UPDATE_TIME_VIEW = 1000L;

  private TransGraph transGraph;

  private CTabItem transGridTab;

  private TableView transGridView;

  private boolean refresh_busy;

  private long lastUpdateView;

  private XulToolbar toolbar;

  private Composite transGridComposite;

  private boolean hideInactiveSteps;

  private boolean showSelectedSteps;

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
   *
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

    colinf[1].setAllignement( SWT.RIGHT );
    colinf[2].setAllignement( SWT.RIGHT );
    colinf[3].setAllignement( SWT.RIGHT );
    colinf[4].setAllignement( SWT.RIGHT );
    colinf[5].setAllignement( SWT.RIGHT );
    colinf[6].setAllignement( SWT.RIGHT );
    colinf[7].setAllignement( SWT.RIGHT );
    colinf[8].setAllignement( SWT.RIGHT );
    colinf[9].setAllignement( SWT.LEFT );
    colinf[10].setAllignement( SWT.RIGHT );
    colinf[11].setAllignement( SWT.RIGHT );
    colinf[12].setAllignement( SWT.RIGHT );

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
  }

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

  /**
   * Reworking of view refresh based on pushed events rather than polling.
   */
  @Override public void execStateChanged( ExecutionStateEvent event ) {
    if ( transGridView == null || transGridView.isDisposed() || transGridView.table == null ) {
      return;
    }
    transGridView.table.removeAll();  // completely recreates with each change.  Could improve.

    event.getState().getStepStates().stream()
      .filter( this::showStepCriteria )
      .forEach( this::updateTableData );
    }

  /**
   *   showHideSelectd doesn't seem to be used.  Is it?
   *   Need to closely compare original show logic.
   */
  private boolean showStepCriteria( ExecutionState.StepState stepState ) {
    StepExecutionStatus status = stepState.getExecStatus();
    return status != StepExecutionStatus.STATUS_EMPTY
      && ( !hideInactiveSteps || status != StepExecutionStatus.STATUS_FINISHED );
  }

  /**
   * TODO handle background formatting, for errors and alternating rows.
   */
  private void updateTableData( ExecutionState.StepState stepState ) {
    // request fields corresponding to table layout
    List<String> values = stepState.getStringFieldValues(
      NAME, COPY, READ, WRITTEN, INPUT, OUTPUT,
      UPDATED, REJECTED, ERRORS, DESC, SECONDS, SPEED, PRIORITY );
    values.add( 0, "" );  // Row number

    TableItem ti = new TableItem( transGridView.table, SWT.NONE );
    ti.setText( values.toArray( new String[ values.size() ] ) );
  }

}
