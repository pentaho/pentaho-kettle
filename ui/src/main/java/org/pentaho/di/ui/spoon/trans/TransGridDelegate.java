/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.ToolBar;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.i18n.GlobalMessages;
import org.pentaho.di.trans.step.BaseStepData.StepExecutionStatus;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepStatus;
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

public class TransGridDelegate extends SpoonDelegate implements XulEventHandler {
  private static Class<?> PKG = Spoon.class; // for i18n purposes, needed by Translator2!!

  private static final String XUL_FILE_TRANS_GRID_TOOLBAR = "ui/trans-grid-toolbar.xul";

  private static final int STEP_NUMBER_COLUMN = 0;

  private static final int STEP_NAME_COLUMN = 1;

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

    //ignore whitespace for stepname column valueMeta, causing sorting to ignore whitespace
    String stepNameColumnName = BaseMessages.getString( PKG, "TransLog.Column.Stepname" );
    ValueMetaInterface valueMeta = new ValueMetaString( stepNameColumnName );
    valueMeta.setIgnoreWhitespace( true );
    ColumnInfo stepNameColumnInfo =
      new ColumnInfo( stepNameColumnName, ColumnInfo.COLUMN_TYPE_TEXT, false,
        true );
    stepNameColumnInfo.setValueMeta( valueMeta );

    ColumnInfo[] colinf =
      new ColumnInfo[] {
        stepNameColumnInfo,
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

    // Add a timer to update this view every couple of seconds...
    //
    final Timer tim = new Timer( "TransGraph: " + transGraph.getMeta().getName() );
    final AtomicBoolean busy = new AtomicBoolean( false );

    TimerTask timtask = new TimerTask() {
      public void run() {
        if ( !spoon.getDisplay().isDisposed() ) {
          spoon.getDisplay().asyncExec( new Runnable() {
            public void run() {
              if ( !busy.get() ) {
                busy.set( true );
                refreshView();
                busy.set( false );
              }
            }
          } );
        }
      }
    };

    tim.schedule( timtask, 0L, REFRESH_TIME ); // schedule to repeat a couple of times per second to get fast feedback

    transGridTab.addDisposeListener( new DisposeListener() {
      public void widgetDisposed( DisposeEvent disposeEvent ) {
        tim.cancel();
      }
    } );

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

  private void refreshView() {
    boolean tableCleared = false;
    int numberStepsToDisplay = -1;
    int baseStepCount = -1;

    if ( transGridView == null || transGridView.isDisposed() ) {
      return;
    }
    if ( refresh_busy ) {
      return;
    }

    List<StepMeta> selectedSteps = new ArrayList<StepMeta>();
    if ( showSelectedSteps ) {
      selectedSteps = transGraph.trans.getTransMeta().getSelectedSteps();
    }

    int topIdx = transGridView.getTable().getTopIndex();

    refresh_busy = true;

    Table table = transGridView.table;

    long time = new Date().getTime();
    long msSinceLastUpdate = time - lastUpdateView;
    if ( transGraph.trans != null && !transGraph.trans.isPreparing() && msSinceLastUpdate > UPDATE_TIME_VIEW ) {
      lastUpdateView = time;

      baseStepCount = transGraph.trans.nrSteps();
      if ( hideInactiveSteps ) {
        numberStepsToDisplay = transGraph.trans.nrActiveSteps();
      } else {
        numberStepsToDisplay = baseStepCount;
      }

      StepExecutionStatus[] stepStatusLookup = transGraph.trans.getTransStepExecutionStatusLookup();
      boolean[] isRunningLookup = transGraph.trans.getTransStepIsRunningLookup();

      // Count sub steps
      for ( int i = 0; i < baseStepCount; i++ ) {
        // if inactive steps are hidden, only count sub steps of active base steps
        if ( !hideInactiveSteps || ( isRunningLookup[ i ]
          || stepStatusLookup[ i ] != StepExecutionStatus.STATUS_FINISHED ) ) {
          StepInterface baseStep = transGraph.trans.getRunThread( i );
          numberStepsToDisplay += baseStep.subStatuses().size();
        }
      }

      if ( table.getItemCount() != numberStepsToDisplay ) {
        table.removeAll();
        tableCleared = true;
      }

      if ( numberStepsToDisplay == 0 && table.getItemCount() == 0 ) {
        new TableItem( table, SWT.NONE );
        refresh_busy = false;
        return;
      }

      if ( tableCleared ) {
        // iterate over the base steps and add into table
        for ( int i = 0; i < baseStepCount; i++ ) {
          StepInterface baseStep = transGraph.trans.getRunThread( i );

          // if the step should be displayed
          if ( showSelected( selectedSteps, baseStep )
            && ( hideInactiveSteps && ( isRunningLookup[ i ]
            || stepStatusLookup[ i ] != StepExecutionStatus.STATUS_FINISHED ) )
            || ( !hideInactiveSteps && stepStatusLookup[ i ] != StepExecutionStatus.STATUS_EMPTY ) ) {

            // write base step to table
            TableItem ti = new TableItem( table, SWT.NONE );
            String baseStepNumber = "" + ( i + 1 );
            ti.setText( STEP_NUMBER_COLUMN, baseStepNumber );
            updateRowFromBaseStep( baseStep, ti );

            // write sub steps to table
            int subStepIndex = 1;
            for ( StepStatus subStepStatus : baseStep.subStatuses() ) {
              String[] subFields = subStepStatus.getTransLogFields( baseStep.getStatus().getDescription() );
              subFields[ STEP_NAME_COLUMN ] = "     " + subFields[ STEP_NAME_COLUMN ];
              TableItem subItem = new TableItem( table, SWT.NONE );
              subItem.setText( STEP_NUMBER_COLUMN, baseStepNumber + "." + subStepIndex++ );
              for ( int f = 1; f < subFields.length; f++ ) {
                subItem.setText( f, subFields[ f ] );
              }
            }
          }
        }
      } else {
        // iterate over and update the existing rows in the table
        for ( int rowIndex = 0; rowIndex < numberStepsToDisplay; rowIndex++ ) {
          TableItem ti = table.getItem( rowIndex );

          if ( ti == null ) {
            continue;
          }

          String tableStepNumber = ti.getText( STEP_NUMBER_COLUMN );
          String[] tableStepNumberSplit = tableStepNumber.split( "\\." );
          String tableBaseStepNumber = tableStepNumberSplit[ 0 ];
          boolean isBaseStep = true;

          if ( tableStepNumberSplit.length > 1 ) {
            isBaseStep = false;
          }

          // step numbers displayed on table start at 1 and step number indexes begin at 0
          int baseStepNumber = Integer.parseInt( tableBaseStepNumber ) - 1;

          StepInterface baseStep = transGraph.trans.getRunThread( baseStepNumber );

          // if the step should be displayed
          if ( showSelected( selectedSteps, baseStep )
            && ( hideInactiveSteps && ( isRunningLookup[ baseStepNumber ]
            || stepStatusLookup[ baseStepNumber ] != StepExecutionStatus.STATUS_FINISHED ) )
            || ( !hideInactiveSteps && stepStatusLookup[ baseStepNumber ] != StepExecutionStatus.STATUS_EMPTY ) ) {

            if ( isBaseStep ) {
              updateRowFromBaseStep( baseStep, ti );
            } else {
              // loop through sub steps and update the one that matches the sub step name from the table
              String tableSubStepName = ti.getText( STEP_NAME_COLUMN );
              for ( StepStatus subStepStatus : baseStep.subStatuses() ) {
                String[] subFields = subStepStatus.getTransLogFields( baseStep.getStatus().getDescription() );
                subFields[ STEP_NAME_COLUMN ] = "     " + subFields[ STEP_NAME_COLUMN ];
                if ( (subFields[ STEP_NAME_COLUMN ]).equals( tableSubStepName ) ) {
                  updateCellsIfChanged( subFields, ti );
                }
              }
            }
          }
        }
      }

      int sortColumn = transGridView.getSortField();
      boolean sortDescending = transGridView.isSortingDescending();
      // Only need to re-sort if the output has been sorted differently to the default
      if ( table.getItemCount() > 0 && ( sortColumn != 0 || sortDescending ) ) {
        transGridView.sortTable( transGridView.getSortField(), sortDescending );
      }

      // Alternate row background color
      for ( int i = 0; i < table.getItems().length; i++ ) {
        TableItem item = table.getItem( i );
        item.setForeground( GUIResource.getInstance().getColorBlack() );
        if ( !item.getBackground().equals( GUIResource.getInstance().getColorRed() ) ) {
          item.setBackground(
            i % 2 == 0
              ? GUIResource.getInstance().getColorWhite()
              : GUIResource.getInstance().getColorBlueCustomGrid() );
        }
      }

      // if (updateRowNumbers) { transGridView.setRowNums(); }
      transGridView.optWidth( true );

      int[] selectedItems = transGridView.getSelectionIndices();

      if ( selectedItems != null && selectedItems.length > 0 ) {
        transGridView.setSelection( selectedItems );
      }
      // transGridView.getTable().setTopIndex(topIdx);
      if ( transGridView.getTable().getTopIndex() != topIdx ) {
        transGridView.getTable().setTopIndex( topIdx );
      }
    } else {
      // We need at least one table-item in a table!
      if ( table.getItemCount() == 0 ) {
        new TableItem( table, SWT.NONE );
      }
    }

    refresh_busy = false;
  }

  private void updateRowFromBaseStep( StepInterface baseStep, TableItem row ) {
    StepStatus stepStatus = new StepStatus( baseStep );

    String[] fields = stepStatus.getTransLogFields();

    updateCellsIfChanged( fields, row );

    // Error lines should appear in red:
    if ( baseStep.getErrors() > 0 ) {
      row.setBackground( GUIResource.getInstance().getColorRed() );
    } else {
      row.setBackground( GUIResource.getInstance().getColorWhite() );
    }
  }

  private boolean showSelected( List<StepMeta> selectedSteps, StepInterface baseStep ) {
    // See if the step is selected & in need of display
    boolean showSelected;
    if ( showSelectedSteps ) {
      if ( selectedSteps.size() == 0 ) {
        showSelected = true;
      } else {
        showSelected = false;
        for ( StepMeta stepMeta : selectedSteps ) {
          if ( baseStep.getStepMeta().equals( stepMeta ) ) {
            showSelected = true;
            break;
          }
        }
      }
    } else {
      showSelected = true;
    }
    return showSelected;
  }

  /**
   * Anti-flicker: if nothing has changed, don't change it on the screen!
   *
   * @param fields
   * @param row
   */
  private void updateCellsIfChanged( String[] fields, TableItem row ) {
    for ( int f = 1; f < fields.length; f++ ) {
      if ( !fields[ f ].equalsIgnoreCase( row.getText( f ) ) ) {
        row.setText( f, fields[ f ] );
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
}
