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

package org.pentaho.di.ui.spoon.job;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import com.google.common.annotations.VisibleForTesting;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.logging.JobEntryLogTable;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogStatus;
import org.pentaho.di.core.logging.LogTableField;
import org.pentaho.di.core.logging.LogTableInterface;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entry.JobEntryCopy;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.ui.spoon.XulSpoonResourceBundle;
import org.pentaho.di.ui.spoon.XulSpoonSettingsManager;
import org.pentaho.di.ui.spoon.delegates.SpoonDelegate;
import org.pentaho.di.ui.xul.KettleXulLoader;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.XulLoader;
import org.pentaho.ui.xul.components.XulToolbarbutton;
import org.pentaho.ui.xul.containers.XulToolbar;
import org.pentaho.ui.xul.impl.XulEventHandler;

public class JobHistoryDelegate extends SpoonDelegate implements XulEventHandler {
  private static Class<?> PKG = JobGraph.class; // for i18n purposes, needed by Translator2!!

  private static final String XUL_FILE_TRANS_GRID_TOOLBAR = "ui/job-history-toolbar.xul";

  private JobGraph jobGraph;

  private CTabItem jobHistoryTab;

  private XulToolbar toolbar;

  private Composite jobHistoryComposite;

  private JobMeta jobMeta;

  private CTabFolder tabFolder;

  private XulToolbarbutton refreshButton;
  private XulToolbarbutton fetchNextBatchButton;
  private XulToolbarbutton fetchAllButton;

  private JobHistoryLogTab[] models;

  private enum Mode {
    INITIAL, NEXT_BATCH, ALL
  }

  /**
   * @param spoon
   *          Spoon instance
   * @param jobGraph
   *          JobGraph instance
   */
  public JobHistoryDelegate( Spoon spoon, JobGraph jobGraph ) {
    super( spoon );
    this.jobGraph = jobGraph;
  }

  public void addJobHistory() {
    // First, see if we need to add the extra view...
    //
    if ( jobGraph.extraViewComposite == null || jobGraph.extraViewComposite.isDisposed() ) {
      jobGraph.addExtraView();
    } else {
      if ( jobHistoryTab != null && !jobHistoryTab.isDisposed() ) {
        // just set this one active and get out...
        //
        jobGraph.extraViewTabFolder.setSelection( jobHistoryTab );
        return;
      }
    }

    jobMeta = jobGraph.getManagedObject();

    // Add a tab to display the logging history tables...
    //
    jobHistoryTab = new CTabItem( jobGraph.extraViewTabFolder, SWT.NONE );
    jobHistoryTab.setImage( GUIResource.getInstance().getImageShowHistory() );
    jobHistoryTab.setText( BaseMessages.getString( PKG, "Spoon.TransGraph.HistoryTab.Name" ) );

    // Create a composite, slam everything on there like it was in the history tab.
    //
    jobHistoryComposite = new Composite( jobGraph.extraViewTabFolder, SWT.NONE );
    jobHistoryComposite.setLayout( new FormLayout() );
    spoon.props.setLook( jobHistoryComposite );

    addToolBar();

    Control toolbarControl = (Control) toolbar.getManagedObject();

    toolbarControl.setLayoutData( new FormData() );
    FormData fd = new FormData();
    fd.left = new FormAttachment( 0, 0 ); // First one in the left top corner
    fd.top = new FormAttachment( 0, 0 );
    fd.right = new FormAttachment( 100, 0 );
    toolbarControl.setLayoutData( fd );

    toolbarControl.setParent( jobHistoryComposite );

    addLogTableTabs();
    tabFolder.setSelection( 0 );

    tabFolder.addSelectionListener( new SelectionListener() {
      @Override
      public void widgetSelected( SelectionEvent arg0 ) {
        setMoreRows( true );
      }

      @Override
      public void widgetDefaultSelected( SelectionEvent arg0 ) {
      }
    } );

    jobHistoryComposite.pack();
    jobHistoryTab.setControl( jobHistoryComposite );
    jobGraph.extraViewTabFolder.setSelection( jobHistoryTab );

    if ( !Props.getInstance().disableInitialExecutionHistory() ) {
      refreshAllHistory();
    }
  }

  private void addLogTableTabs() {
    // Create a nested tab folder in the tab item, on the history composite...
    //
    tabFolder = new CTabFolder( jobHistoryComposite, SWT.MULTI );
    spoon.props.setLook( tabFolder, Props.WIDGET_STYLE_TAB );

    FormData fdTabFolder = new FormData();
    fdTabFolder.left = new FormAttachment( 0, 0 ); // First one in the left top corner
    fdTabFolder.top = new FormAttachment( (Control) toolbar.getManagedObject(), 0 );
    fdTabFolder.right = new FormAttachment( 100, 0 );
    fdTabFolder.bottom = new FormAttachment( 100, 0 );
    tabFolder.setLayoutData( fdTabFolder );

    models = new JobHistoryLogTab[jobMeta.getLogTables().size()];
    for ( int i = 0; i < models.length; i++ ) {
      models[i] = new JobHistoryLogTab( tabFolder, jobMeta.getLogTables().get( i ) );
    }
  }

  private void addToolBar() {
    try {
      XulLoader loader = new KettleXulLoader();
      loader.setSettingsManager( XulSpoonSettingsManager.getInstance() );
      ResourceBundle bundle = new XulSpoonResourceBundle( Spoon.class );
      XulDomContainer xulDomContainer = loader.loadXul( XUL_FILE_TRANS_GRID_TOOLBAR, bundle );
      xulDomContainer.addEventHandler( this );
      toolbar = (XulToolbar) xulDomContainer.getDocumentRoot().getElementById( "nav-toolbar" );

      refreshButton = (XulToolbarbutton) xulDomContainer.getDocumentRoot().getElementById( "refresh-history" );
      fetchNextBatchButton =
        (XulToolbarbutton) xulDomContainer.getDocumentRoot().getElementById( "fetch-next-batch-history" );
      fetchAllButton = (XulToolbarbutton) xulDomContainer.getDocumentRoot().getElementById( "fetch-all-history" );

      ToolBar swtToolBar = (ToolBar) toolbar.getManagedObject();
      spoon.props.setLook( swtToolBar, Props.WIDGET_STYLE_TOOLBAR );
      swtToolBar.layout( true, true );
    } catch ( Throwable t ) {
      log.logError( Const.getStackTracker( t ) );
      new ErrorDialog( jobHistoryComposite.getShell(),
        BaseMessages.getString( PKG, "Spoon.Exception.ErrorReadingXULFile.Title" ),
        BaseMessages.getString( PKG, "Spoon.Exception.ErrorReadingXULFile.Message", XUL_FILE_TRANS_GRID_TOOLBAR ),
        new Exception( t ) );
    }
  }

  /**
   * Public for XUL.
   */
  public void clearLogTable() {
    clearLogTable( tabFolder.getSelectionIndex() );
  }

  /**
   * User requested to clear the log table.<br>
   * Better ask confirmation
   */
  private void clearLogTable( int index ) {
    JobHistoryLogTab model = models[index];
    LogTableInterface logTable = model.logTable;

    if ( logTable.isDefined() ) {
      DatabaseMeta databaseMeta = logTable.getDatabaseMeta();

      MessageBox mb = new MessageBox( jobGraph.getShell(), SWT.YES | SWT.NO | SWT.ICON_QUESTION );
      mb.setMessage( BaseMessages.getString( PKG, "JobGraph.Dialog.AreYouSureYouWantToRemoveAllLogEntries.Message",
        logTable.getQuotedSchemaTableCombination() ) );
      mb.setText( BaseMessages.getString( PKG, "JobGraph.Dialog.AreYouSureYouWantToRemoveAllLogEntries.Title" ) );
      if ( mb.open() == SWT.YES ) {
        Database database = new Database( loggingObject, databaseMeta );
        try {
          database.connect();
          database.truncateTable( logTable.getSchemaName(), logTable.getTableName() );
        } catch ( Exception e ) {
          new ErrorDialog( jobGraph.getShell(),
            BaseMessages.getString( PKG, "JobGraph.Dialog.ErrorClearningLoggingTable.Title" ),
            BaseMessages.getString( PKG, "JobGraph.Dialog.AreYouSureYouWantToRemoveAllLogEntries.Message" ), e );
        } finally {
          database.disconnect();

          refreshHistory();
          if ( model.logDisplayText != null ) {
            model.logDisplayText.setText( "" );
          }
        }
      }
    }
  }

  /**
   * Public for XUL.
   */
  public void replayHistory() {
    JobHistoryLogTab model = models[tabFolder.getSelectionIndex()];

    int idx = model.logDisplayTableView.getSelectionIndex();
    if ( idx >= 0 ) {
      String[] fields = model.logDisplayTableView.getItem( idx );
      int batchId = Const.toInt( fields[0], -1 );
      // String dateString = fields[13];
      // Date replayDate = XMLHandler.stringToDate(dateString);

      List<JobEntryCopyResult> results = null;
      boolean gotResults = false;

      // We check in the Job Entry Logging to see the results from all the various job entries that were executed.
      //
      JobEntryLogTable jeLogTable = jobMeta.getJobEntryLogTable();
      if ( jeLogTable.isDefined() ) {
        try {

          DatabaseMeta databaseMeta = jobMeta.getJobEntryLogTable().getDatabaseMeta();
          Database db = new Database( Spoon.loggingObject, databaseMeta );
          try {
            db.connect();
            String schemaTable =
              databaseMeta.getQuotedSchemaTableCombination( jeLogTable.getActualSchemaName(), jeLogTable
                .getActualTableName() );
            String sql =
              "SELECT * FROM "
                + schemaTable + " WHERE " + databaseMeta.quoteField( jeLogTable.getKeyField().getFieldName() )
                + " = " + batchId;

            List<Object[]> rows = db.getRows( sql, 0 );
            RowMetaInterface rowMeta = db.getReturnRowMeta();
            results = new ArrayList<JobEntryCopyResult>();

            int jobEntryNameIndex =
              rowMeta.indexOfValue( jeLogTable
                .findField( JobEntryLogTable.ID.JOBENTRYNAME.toString() ).getFieldName() );
            int jobEntryResultIndex =
              rowMeta
                .indexOfValue( jeLogTable.findField( JobEntryLogTable.ID.RESULT.toString() ).getFieldName() );
            int jobEntryErrorsIndex =
              rowMeta
                .indexOfValue( jeLogTable.findField( JobEntryLogTable.ID.ERRORS.toString() ).getFieldName() );
            LogTableField copyNrField = jeLogTable.findField( JobEntryLogTable.ID.COPY_NR.toString() );
            int jobEntryCopyNrIndex =
              copyNrField == null ? -1 : ( copyNrField.isEnabled() ? rowMeta.indexOfValue( copyNrField
                .getFieldName() ) : -1 );

            for ( Object[] row : rows ) {
              String jobEntryName = rowMeta.getString( row, jobEntryNameIndex );
              boolean jobEntryResult = rowMeta.getBoolean( row, jobEntryResultIndex );
              long errors = rowMeta.getInteger( row, jobEntryErrorsIndex );
              long copyNr = jobEntryCopyNrIndex < 0 ? 0 : rowMeta.getInteger( row, jobEntryCopyNrIndex );
              JobEntryCopyResult result =
                new JobEntryCopyResult( jobEntryName, jobEntryResult, errors, (int) copyNr );
              results.add( result );
            }

          } finally {
            db.disconnect();
          }

          gotResults = true;
        } catch ( Exception e ) {
          new ErrorDialog(
            spoon.getShell(), BaseMessages.getString(
              PKG, "JobHistoryDelegate.ReplayHistory.UnexpectedErrorReadingJobEntryHistory.Text" ),
            BaseMessages.getString(
              PKG, "JobHistoryDelegate.ReplayHistory.UnexpectedErrorReadingJobEntryHistory.Message" ), e );

        }
      } else {
        MessageBox box = new MessageBox( spoon.getShell(), SWT.ICON_ERROR | SWT.OK );
        box.setText( BaseMessages.getString( PKG, "JobHistoryDelegate.ReplayHistory.NoJobEntryTable.Text" ) );
        box.setMessage( BaseMessages.getString( PKG, "JobHistoryDelegate.ReplayHistory.NoJobEntryTable.Message" ) );
        box.open();
      }

      // spoon.executeJob(jobGraph.getManagedObject(), true, false, replayDate, false);
      if ( !gotResults ) {

        // For some reason we have no execution results, simply list all the job entries so the user can choose...
        //
        results = new ArrayList<JobEntryCopyResult>();
        for ( JobEntryCopy copy : jobMeta.getJobCopies() ) {
          results.add( new JobEntryCopyResult( copy.getName(), null, null, copy.getNr() ) );
        }
      }

      // OK, now that we have our list of job entries, let's first try to find the first job-entry that had a false
      // result or where errors>0
      // If the error was handled, we look further for a more appropriate target.
      //
      JobEntryCopy selection = null;
      boolean more = true;
      JobEntryCopy start = jobMeta.findStart();
      while ( selection == null && more ) {
        int nrNext = jobMeta.findNrNextJobEntries( start );
        more = nrNext > 0;
        for ( int n = 0; n < nrNext; n++ ) {
          JobEntryCopy copy = jobMeta.findNextJobEntry( start, n );

          // See if we can find a result for this job entry...
          //
          JobEntryCopyResult result = JobEntryCopyResult.findResult( results, copy );
          if ( result != null ) {
            System.out.println( "TODO: replay" );
            // Do nothing???
          }
        }
      }

      // Present all job entries to the user.
      //
      for ( JobEntryCopyResult result : results ) {
        System.out.println( "Job entry copy result --  Name="
          + result.getJobEntryName() + ", result=" + result.getResult() + ", errors=" + result.getErrors()
          + ", nr=" + result.getCopyNr() );
      }

    }
  }

  /**
   * Public for XUL.
   */
  public void refreshHistory() {
    refreshHistory( tabFolder.getSelectionIndex(), Mode.INITIAL );
  }

  private void refreshAllHistory() {
    for ( int i = 0; i < models.length; i++ ) {
      refreshHistory( i, Mode.INITIAL );
    }
  }

  /**
   * Background thread refreshes history data
   */
  private void refreshHistory( final int index, final Mode fetchMode ) {
    new Thread( new Runnable() {
      @Override
      public void run() {
        // do gui stuff here
        spoon.getDisplay().syncExec( new Runnable() {
          @Override
          public void run() {
            setQueryInProgress( true );
            JobHistoryLogTab model = models[index];
            model.setLogTable( jobMeta.getLogTables().get( index ) );
          }
        } );

        final boolean moreRows = getHistoryData( index, fetchMode );

        // do gui stuff here
        spoon.getDisplay().syncExec( new Runnable() {
          @Override
          public void run() {
            displayHistoryData( index );
            setQueryInProgress( false );
            setMoreRows( moreRows );
          }
        } );

      }
    } ).start();
  }

  private void setMoreRows( final boolean moreRows ) {
    fetchNextBatchButton.setDisabled( !moreRows );
  }

  /**
   * Don't allow more queries until this one finishes.
   *
   * @param inProgress
   *          is query in progress
   */
  private void setQueryInProgress( final boolean inProgress ) {
    refreshButton.setDisabled( inProgress );
    fetchNextBatchButton.setDisabled( inProgress );
    fetchAllButton.setDisabled( inProgress );
  }

  private boolean getHistoryData( final int index, final Mode mode ) {
    final int BATCH_SIZE = Props.getInstance().getLinesInHistoryFetchSize();
    boolean moreRows = false;
    JobHistoryLogTab model = models[index];
    LogTableInterface logTable = model.logTable;
    // See if there is a job loaded that has a connection table specified.
    //
    if ( jobMeta != null && !Utils.isEmpty( jobMeta.getName() ) && logTable.isDefined() ) {
      Database database = null;
      try {
        DatabaseMeta logConnection = logTable.getDatabaseMeta();

        // open a connection
        database = new Database( loggingObject, logConnection );
        database.shareVariablesWith( jobMeta );
        database.connect();

        int queryLimit = 0;

        switch ( mode ) {
          case ALL:
            model.batchCount = 0;
            queryLimit = Props.getInstance().getMaxNrLinesInHistory();
            break;
          case NEXT_BATCH:
            model.batchCount++;
            queryLimit = BATCH_SIZE * model.batchCount;
            break;
          case INITIAL:
            model.batchCount = 1;
            queryLimit = BATCH_SIZE;
            break;
          default:
            break;
        }
        database.setQueryLimit( queryLimit );

        // First, we get the information out of the database table...
        //
        String schemaTable = logTable.getQuotedSchemaTableCombination();

        StringBuilder sql = new StringBuilder( "SELECT " );
        boolean first = true;
        for ( LogTableField field : logTable.getFields() ) {
          if ( field.isEnabled() && field.isVisible() ) {
            if ( !first ) {
              sql.append( ", " );
            }
            first = false;
            sql.append( logConnection.quoteField( field.getFieldName() ) );
          }
        }
        sql.append( " FROM " ).append( schemaTable );

        RowMetaAndData params = new RowMetaAndData();

        // Do we need to limit the amount of data?
        //
        LogTableField nameField = logTable.getNameField();
        LogTableField keyField = logTable.getKeyField();

        if ( nameField != null ) {
          sql
            .append( " WHERE " ).append( logConnection.quoteField( nameField.getFieldName() ) ).append(
              " LIKE ?" );
          params
            .addValue( new ValueMetaString( "transname_literal", 255, -1 ), jobMeta.getName() );
        }

        if ( keyField != null && keyField.isEnabled() ) {
          sql
            .append( " ORDER BY " ).append( logConnection.quoteField( keyField.getFieldName() ) ).append(
              " DESC" );
        }

        ResultSet resultSet = database.openQuery( sql.toString(), params.getRowMeta(), params.getData() );

        List<Object[]> rows = new ArrayList<Object[]>();
        Object[] rowData = database.getRow( resultSet );
        int rowsFetched = 1;
        while ( rowData != null ) {
          rows.add( rowData );
          rowData = database.getRow( resultSet );
          rowsFetched++;
        }

        if ( rowsFetched >= queryLimit ) {
          moreRows = true;
        }

        database.closeQuery( resultSet );

        models[index].rows = rows;
      } catch ( Exception e ) {
        LogChannel.GENERAL.logError( "Unable to get rows of data from logging table " + models[index].logTable, e );
        models[index].rows = new ArrayList<Object[]>();
      } finally {
        if ( database != null ) {
          database.disconnect();
        }
      }
    } else {
      models[index].rows = new ArrayList<Object[]>();
    }
    return moreRows;
  }

  /**
   * Maps UI columns to DB columns
   * @return {@link Map} with the mapping between UI column names and index of the corresponding DB column
   */
  @VisibleForTesting
  Map<String, Integer> getColumnMappings( JobHistoryDelegate.JobHistoryLogTab model ) {
    Map<String, Integer> map = new HashMap();

    for ( ColumnInfo ci : model.logDisplayTableView.getColumns() ) {
      for ( int i = 0; i < model.logTableFields.size(); i++ ) {
        if ( ci.getValueMeta().getName().equals( model.logTableFields.get( i ).getFieldName() ) ) {
          map.put( model.logTableFields.get( i ).getFieldName(), i );
          break;
        }
      }
    }

    return map;
  }

  /**
   * Returns the {@link ValueMetaInterface} for a specified log table field
   * @param columns The list of UI columns
   * @param field The field to look for
   * @return The {@link ValueMetaInterface} for the specified field
   */
  @VisibleForTesting
  ValueMetaInterface getValueMetaForColumn( ColumnInfo[] columns, LogTableField field ) {
    return Arrays.stream( columns )
      .filter( x -> x.getValueMeta().getName().equals( field.getFieldName() ) )
      .findFirst()
      .get()
      .getValueMeta();
  }

  private void displayHistoryData( final int index ) {
    JobHistoryLogTab model = models[index];

    if ( model.logDisplayTableView == null || model.logDisplayTableView.isDisposed() ) {
      return;
    }

    // display the data in the table view
    ColumnInfo[] colinf = model.logDisplayTableView.getColumns();

    int selectionIndex = model.logDisplayTableView.getSelectionIndex();
    model.logDisplayTableView.table.clearAll();

    List<Object[]> rows = model.rows;

    LogTableField errorsField = model.logTable.getErrorsField();
    LogTableField statusField = model.logTable.getStatusField();

    if ( rows != null && rows.size() > 0 ) {
      // we need to map ui columns to db columns before rendering data
      Map<String, Integer> map = getColumnMappings( model );

      // add row data to the table view
      for ( Object[] rowData : rows ) {
        TableItem item = new TableItem( model.logDisplayTableView.table, SWT.NONE );

        for ( int c = 0; c < colinf.length; c++ ) {
          ColumnInfo column = colinf[c];

          ValueMetaInterface valueMeta = column.getValueMeta();
          String string = null;
          try {
            string = valueMeta.getString( rowData[ map.get( column.getValueMeta().getName() ) ] );
          } catch ( KettleValueException e ) {
            log.logError( "history data conversion issue", e );
          }
          item.setText( c + 1, Const.NVL( string, "" ) );
        }

        // Add some color
        //
        Long errors = null;
        LogStatus status = null;

        if ( errorsField != null ) {
          ValueMetaInterface valueMeta = getValueMetaForColumn( colinf, errorsField );
          try {
            errors = valueMeta.getInteger( rowData[ map.get( valueMeta.getName() ) ] );
          } catch ( KettleValueException e ) {
            log.logError( "history data conversion issue", e );
          }
        }
        if ( statusField != null ) {
          ValueMetaInterface valueMeta = getValueMetaForColumn( colinf, statusField );
          String statusString = null;
          try {
            statusString = valueMeta.getString( rowData[ map.get( valueMeta.getName() ) ] );
          } catch ( KettleValueException e ) {
            log.logError( "history data conversion issue", e );
          }
          if ( statusString != null ) {
            status = LogStatus.findStatus( statusString );
          }
        }

        if ( errors != null && errors > 0L ) {
          item.setBackground( GUIResource.getInstance().getColorRed() );
        } else if ( status != null && LogStatus.STOP.equals( status ) ) {
          item.setBackground( GUIResource.getInstance().getColorYellow() );
        }
      }

      model.logDisplayTableView.removeEmptyRows();
      model.logDisplayTableView.setRowNums();
      model.logDisplayTableView.optWidth( true );
    } else {
      model.logDisplayTableView.clearAll( false );
    }

    if ( selectionIndex >= 0 && selectionIndex < model.logDisplayTableView.getItemCount() ) {
      model.logDisplayTableView.table.select( selectionIndex );
      showLogEntry();
    }
  }

  private void showLogEntry() {
    JobHistoryLogTab model = models[tabFolder.getSelectionIndex()];

    Text text = model.logDisplayText;

    if ( text == null || text.isDisposed() ) {
      return;
    }

    List<Object[]> list = model.rows;

    if ( list == null || list.size() == 0 ) {
      String message;
      if ( model.logTable.isDefined() ) {
        message = BaseMessages.getString( PKG, "JobHistory.PleaseRefresh.Message" );
      } else {
        message = BaseMessages.getString( PKG, "JobHistory.HistoryConfiguration.Message" );
      }
      text.setText( message );
      return;
    }

    // grab the selected line in the table:
    int nr = model.logDisplayTableView.table.getSelectionIndex();
    if ( nr >= 0 && nr < list.size() ) {
      // OK, grab this one from the buffer...
      Object[] row = list.get( nr );

      // What is the name of the log field?
      //
      LogTableField logField = model.logTable.getLogField();
      if ( logField != null ) {
        int index = model.logTableFields.indexOf( logField );
        if ( index >= 0 ) {
          String logText = row[index].toString();

          text.setText( Const.NVL( logText, "" ) );

          text.setSelection( text.getText().length() );
          text.showSelection();
        } else {
          text.setText( BaseMessages.getString( PKG, "JobHistory.HistoryConfiguration.NoLoggingFieldDefined" ) );
        }
      }
    }
  }

  /**
   * @return the jobHistoryTab
   */
  public CTabItem getJobHistoryTab() {
    return jobHistoryTab;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.ui.xul.impl.XulEventHandler#getData()
   */
  @Override
  public Object getData() {
    return null;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.ui.xul.impl.XulEventHandler#getName()
   */
  @Override
  public String getName() {
    return "history";
  }

  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.ui.xul.impl.XulEventHandler#getXulDomContainer()
   */
  @Override
  public XulDomContainer getXulDomContainer() {
    return null;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.ui.xul.impl.XulEventHandler#setData(java.lang.Object)
   */
  @Override
  public void setData( Object data ) {
  }

  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.ui.xul.impl.XulEventHandler#setName(java.lang.String)
   */
  @Override
  public void setName( String name ) {
  }

  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.ui.xul.impl.XulEventHandler#setXulDomContainer(org.pentaho.ui.xul.XulDomContainer)
   */
  @Override
  public void setXulDomContainer( XulDomContainer xulDomContainer ) {
  }

  /**
   * XUL event: fetches next x records for current log table.
   */
  public void fetchNextBatch() {
    int tabIndex = tabFolder.getSelectionIndex();
    refreshHistory( tabIndex, Mode.NEXT_BATCH );
  }

  /**
   * XUL event: loads all load records for current log table.
   */
  public void fetchAll() {
    int tabIndex = tabFolder.getSelectionIndex();
    refreshHistory( tabIndex, Mode.ALL );
  }

  @VisibleForTesting
  class JobHistoryLogTab extends CTabItem {
    private List<LogTableField> logTableFields = new ArrayList<LogTableField>();
    private List<Object[]> rows;
    private LogTableInterface logTable;
    private Text logDisplayText;
    private TableView logDisplayTableView;

    /**
     * Number of batches fetched so far. When the next batch is fetched, the number of rows displayed will be the max of
     * batchCount * BATCH_SIZE and resultSet row count.
     */
    public int batchCount;

    public JobHistoryLogTab( CTabFolder tabFolder, LogTableInterface logTable ) {
      super( tabFolder, SWT.NONE );
      setLogTable( logTable );

      setText( logTable.getLogTableType() );

      Composite logTableComposite = new Composite( tabFolder, SWT.NONE );
      logTableComposite.setLayout( new FormLayout() );
      spoon.props.setLook( logTableComposite );

      setControl( logTableComposite );

      SashForm sash = new SashForm( logTableComposite, SWT.VERTICAL );
      sash.setLayout( new FillLayout() );
      FormData fdSash = new FormData();
      fdSash.left = new FormAttachment( 0, 0 ); // First one in the left top corner
      fdSash.top = new FormAttachment( 0, 0 );
      fdSash.right = new FormAttachment( 100, 0 );
      fdSash.bottom = new FormAttachment( 100, 0 );
      sash.setLayoutData( fdSash );

      logDisplayTableView = createJobLogTableView( sash );

      if ( logTable.getLogField() != null ) {
        logDisplayText = new Text( sash, SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL | SWT.READ_ONLY );
        spoon.props.setLook( logDisplayText );
        logDisplayText.setVisible( true );

        FormData fdText = new FormData();
        fdText.left = new FormAttachment( 0, 0 );
        fdText.top = new FormAttachment( 0, 0 );
        fdText.right = new FormAttachment( 100, 0 );
        fdText.bottom = new FormAttachment( 100, 0 );
        logDisplayText.setLayoutData( fdText );

        sash.setWeights( new int[] { 70, 30, } );
      } else {
        logDisplayText = null;
        sash.setWeights( new int[] { 100, } );
      }
    }

    public void setLogTable( LogTableInterface logTable ) {
      this.logTable = logTable;
      logTableFields.clear();
      for ( LogTableField field : logTable.getFields() ) {
        if ( field.isEnabled() && field.isVisible() ) {
          logTableFields.add( field );
        }
      }

      // Recreate table view as log table has changed
      if ( logDisplayTableView != null ) {
        Composite tableParent = logDisplayTableView.getParent();
        TableView newTable = createJobLogTableView( tableParent );
        newTable.moveAbove( logDisplayTableView );
        logDisplayTableView.dispose();
        tableParent.layout( false );
        logDisplayTableView = newTable;
      }
    }

    private TableView createJobLogTableView( Composite parent ) {
      List<ColumnInfo> columnList = new ArrayList<ColumnInfo>();

      for ( LogTableField field : logTableFields ) {
        if ( !field.isLogField() ) {
          ColumnInfo column = new ColumnInfo( field.getName(), ColumnInfo.COLUMN_TYPE_TEXT, false, true );
          int valueType = field.getDataType();
          String conversionMask = null;

          switch ( field.getDataType() ) {
            case ValueMetaInterface.TYPE_INTEGER:
              conversionMask = "###,###,##0";
              column.setAllignement( SWT.RIGHT );
              break;
            case ValueMetaInterface.TYPE_DATE:
              conversionMask = "yyyy/MM/dd HH:mm:ss";
              column.setAllignement( SWT.CENTER );
              break;
            case ValueMetaInterface.TYPE_NUMBER:
              conversionMask = " ###,###,##0.00;-###,###,##0.00";
              column.setAllignement( SWT.RIGHT );
              break;
            case ValueMetaInterface.TYPE_STRING:
              column.setAllignement( SWT.LEFT );
              break;
            case ValueMetaInterface.TYPE_BOOLEAN:
              DatabaseMeta databaseMeta = logTable.getDatabaseMeta();
              if ( databaseMeta != null ) {
                if ( !databaseMeta.supportsBooleanDataType() ) {
                  // Boolean gets converted to String!
                  //
                  valueType = ValueMetaInterface.TYPE_STRING;
                }
              }
              break;
            default:
              break;
          }

          ValueMetaInterface valueMeta = new ValueMeta( field.getFieldName(), valueType, field.getLength(), -1 );
          if ( conversionMask != null ) {
            valueMeta.setConversionMask( conversionMask );
          }
          column.setValueMeta( valueMeta );
          columnList.add( column );
        }
      }

      TableView tableView = new TableView( jobMeta, parent, SWT.BORDER | SWT.FULL_SELECTION | SWT.SINGLE,
        columnList.toArray( new ColumnInfo[columnList.size()] ), 1,
        true, // readonly!
        null,
        spoon.props );

      tableView.table.addSelectionListener( new SelectionAdapter() {
        @Override
        public void widgetSelected( SelectionEvent arg0 ) {
          showLogEntry();
        }
      } );

      return tableView;
    }
  }
}
