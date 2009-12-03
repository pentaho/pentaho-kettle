package org.pentaho.di.ui.spoon.job;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
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
import org.eclipse.swt.widgets.ToolItem;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.ui.spoon.delegates.SpoonDelegate;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.XulLoader;
import org.pentaho.ui.xul.components.XulToolbarbutton;
import org.pentaho.ui.xul.containers.XulToolbar;
import org.pentaho.ui.xul.impl.XulEventHandler;
import org.pentaho.ui.xul.swt.SwtXulLoader;

public class JobHistoryDelegate extends SpoonDelegate implements XulEventHandler {

  private static final String XUL_FILE_TRANS_GRID_TOOLBAR = "ui/job-history-toolbar.xul";
  private static final LogWriter log = LogWriter.getInstance();

  private JobGraph jobGraph;

  private CTabItem jobHistoryTab;

  private ColumnInfo[] colinf;

  private Text wText;

  private TableView wFields;

  private FormData fdText, fdSash;

  private List<RowMetaAndData> rowList;

  private ValueMetaInterface durationMeta;

  private ValueMetaInterface replayDateMeta;

  private XulToolbar toolbar;

  private Composite jobHistoryComposite;

  /**
   * @param spoon
   * @param jobGraph
   */
  public JobHistoryDelegate(Spoon spoon, JobGraph jobGraph) {
    super(spoon);
    this.jobGraph = jobGraph;
  }

  public void addJobHistory() {
    // First, see if we need to add the extra view...
    //
    if (jobGraph.extraViewComposite == null || jobGraph.extraViewComposite.isDisposed()) {
      jobGraph.addExtraView();
    } else {
      if (jobHistoryTab != null && !jobHistoryTab.isDisposed()) {
        // just set this one active and get out...
        //
        jobGraph.extraViewTabFolder.setSelection(jobHistoryTab);
        return;
      }
    }

    // Add a transLogTab : display the logging...
    //
    jobHistoryTab = new CTabItem(jobGraph.extraViewTabFolder, SWT.NONE);
    jobHistoryTab.setImage(GUIResource.getInstance().getImageShowHistory());
    jobHistoryTab.setText(Messages.getString("Spoon.TransGraph.HistoryTab.Name"));

    // Create a composite, slam everything on there like it was in the history tab.
    //
    jobHistoryComposite = new Composite(jobGraph.extraViewTabFolder, SWT.NONE);
    jobHistoryComposite.setLayout(new FormLayout());
    spoon.props.setLook(jobHistoryComposite);

    // Create a composite, slam everything on there like it was in the history tab.
    //
    addToolBar();
 
    SashForm sash = new SashForm(jobHistoryComposite, SWT.VERTICAL);

    sash.setLayout(new FillLayout());

    final int FieldsRows = 1;
    colinf = new ColumnInfo[] { new ColumnInfo(Messages.getString("JobHistory.Column.Name"), ColumnInfo.COLUMN_TYPE_TEXT, true, true), //$NON-NLS-1$
        new ColumnInfo(Messages.getString("JobHistory.Column.BatchID"), ColumnInfo.COLUMN_TYPE_TEXT, true, true), //$NON-NLS-1$
        new ColumnInfo(Messages.getString("JobHistory.Column.Status"), ColumnInfo.COLUMN_TYPE_TEXT, false, true), //$NON-NLS-1$
        new ColumnInfo(Messages.getString("JobHistory.Column.Duration"), ColumnInfo.COLUMN_TYPE_TEXT, true, true), //$NON-NLS-1$
        new ColumnInfo(Messages.getString("JobHistory.Column.Read"), ColumnInfo.COLUMN_TYPE_TEXT, true, true), //$NON-NLS-1$
        new ColumnInfo(Messages.getString("JobHistory.Column.Written"), ColumnInfo.COLUMN_TYPE_TEXT, true, true), //$NON-NLS-1$
        new ColumnInfo(Messages.getString("JobHistory.Column.Updated"), ColumnInfo.COLUMN_TYPE_TEXT, true, true), //$NON-NLS-1$
        new ColumnInfo(Messages.getString("JobHistory.Column.Input"), ColumnInfo.COLUMN_TYPE_TEXT, true, true), //$NON-NLS-1$
        new ColumnInfo(Messages.getString("JobHistory.Column.Output"), ColumnInfo.COLUMN_TYPE_TEXT, true, true), //$NON-NLS-1$
        new ColumnInfo(Messages.getString("JobHistory.Column.Errors"), ColumnInfo.COLUMN_TYPE_TEXT, true, true), //$NON-NLS-1$
        new ColumnInfo(Messages.getString("JobHistory.Column.StartDate"), ColumnInfo.COLUMN_TYPE_TEXT, false, true), //$NON-NLS-1$
        new ColumnInfo(Messages.getString("JobHistory.Column.EndDate"), ColumnInfo.COLUMN_TYPE_TEXT, false, true), //$NON-NLS-1$
        new ColumnInfo(Messages.getString("JobHistory.Column.LogDate"), ColumnInfo.COLUMN_TYPE_TEXT, false, true), //$NON-NLS-1$
        new ColumnInfo(Messages.getString("JobHistory.Column.DependencyDate"), ColumnInfo.COLUMN_TYPE_TEXT, false, true), //$NON-NLS-1$
        new ColumnInfo(Messages.getString("JobHistory.Column.ReplayDate"), ColumnInfo.COLUMN_TYPE_TEXT, false, true) //$NON-NLS-1$
    };

    for (int i = 3; i < 10; i++)
      colinf[i].setAllignement(SWT.RIGHT);

    // Create the duration value meta data
    //
    durationMeta = new ValueMeta("DURATION", ValueMetaInterface.TYPE_NUMBER);
    durationMeta.setConversionMask("0");
    colinf[2].setValueMeta(durationMeta);

    wFields = new TableView(jobGraph.getManagedObject(), sash, SWT.BORDER | SWT.FULL_SELECTION | SWT.SINGLE, colinf, FieldsRows, true, // readonly!
        null, spoon.props);

    wText = new Text(sash, SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL | SWT.READ_ONLY);
    spoon.props.setLook(wText);
    wText.setVisible(true);
    // wText.setText(Messages.getString("TransHistory.PleaseRefresh.Message"));

    // Put text in the middle
    fdText = new FormData();
    fdText.left = new FormAttachment(0, 0);
    fdText.top = new FormAttachment(0, 0);
    fdText.right = new FormAttachment(100, 0);
    fdText.bottom = new FormAttachment(100, 0);
    wText.setLayoutData(fdText);

    fdSash = new FormData();
    fdSash.left = new FormAttachment(0, 0); // First one in the left top corner
    fdSash.top = new FormAttachment((Control) toolbar.getManagedObject(), 0);
    fdSash.right = new FormAttachment(100, 0);
    fdSash.bottom = new FormAttachment(100, 0);
    sash.setLayoutData(fdSash);

    sash.setWeights(new int[] { 60, 40 });

    jobHistoryComposite.pack();

    wFields.table.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        showLogEntry();
      }
    });
    wFields.table.addKeyListener(new KeyListener() {
      public void keyReleased(KeyEvent e) {
        showLogEntry();
      }

      public void keyPressed(KeyEvent e) {
      }

    });

    jobHistoryTab.setControl(jobHistoryComposite);

    jobGraph.extraViewTabFolder.setSelection(jobHistoryTab);

    // Also add a listener to JobGraph to see if a transformation finished...
    //
    final RefreshListener jobRefreshListener = new RefreshListener() {

      public void refreshNeeded() {
        jobGraph.getDisplay().asyncExec(new Runnable() {

          public void run() {
            refreshHistory();
          }
        });

      }

    };
    jobGraph.addRefreshListener(jobRefreshListener);

    // Make sure to clean it up afterwards too.
    jobHistoryTab.addDisposeListener(new DisposeListener() {
      public void widgetDisposed(DisposeEvent e) {
        jobGraph.getRefreshListeners().remove(jobRefreshListener);
      }
    });

    // Launch a refresh in the background...
    //
    jobGraph.getDisplay().asyncExec(new Runnable() {
      public void run() {
        refreshHistory();
        if (!jobHistoryComposite.isDisposed())
          jobHistoryComposite.layout(true, true);
      }
    });
  }

  private void addToolBar() {

    try {
      XulLoader loader = new SwtXulLoader();
      XulDomContainer xulDomContainer = loader.loadXul(XUL_FILE_TRANS_GRID_TOOLBAR);
      xulDomContainer.addEventHandler(this);
      toolbar = (XulToolbar) xulDomContainer.getDocumentRoot().getElementById("nav-toolbar");

      XulToolbarbutton onlyActiveButton = (XulToolbarbutton) toolbar.getElementById("show-inactive");
      if (onlyActiveButton != null) {
        ToolItem swtToolItem = (ToolItem) onlyActiveButton.getManagedObject();
        swtToolItem.setImage(GUIResource.getInstance().getImageHideInactive());
      }

      ToolBar swtToolbar = (ToolBar) toolbar.getManagedObject();
      swtToolbar.layout(true, true);
    } catch (Throwable t) {
      log.logError(toString(), Const.getStackTracker(t));
      new ErrorDialog(jobHistoryComposite.getShell(), Messages.getString("Spoon.Exception.ErrorReadingXULFile.Title"), Messages.getString("Spoon.Exception.ErrorReadingXULFile.Message", XUL_FILE_TRANS_GRID_TOOLBAR), new Exception(t));
    }
  }

  /**
   * User requested to clear the log table.<br>
   * Better ask confirmation
   */
  public void clearLogTable() {
    String logTable = jobGraph.getManagedObject().getLogTable();
    DatabaseMeta databaseMeta = jobGraph.getManagedObject().getLogConnection();

    if (databaseMeta != null && !Const.isEmpty(logTable)) {

      MessageBox mb = new MessageBox(jobGraph.getShell(), SWT.YES | SWT.NO | SWT.ICON_QUESTION);
      mb.setMessage(Messages.getString("JobGraph.Dialog.AreYouSureYouWantToRemoveAllLogEntries.Message", logTable)); // Nothing found that matches your criteria
      mb.setText(Messages.getString("JobGraph.Dialog.AreYouSureYouWantToRemoveAllLogEntries.Title")); // Sorry!
      if (mb.open() == SWT.YES) {
        Database database = new Database(databaseMeta);
        try {
          database.connect();
          database.truncateTable(logTable);
        } catch (Exception e) {
          new ErrorDialog(jobGraph.getShell(), Messages.getString("JobGraph.Dialog.ErrorClearningLoggingTable.Title"), Messages.getString("JobGraph.Dialog.ErrorClearningLoggingTable.Message"), e);
        } finally {
          if (database != null) {
            database.disconnect();
          }

          refreshHistory();
          wText.setText("");
        }
      }

    }
  }

  public void replayHistory() {
    int idx = wFields.getSelectionIndex();
    if (idx >= 0) {
      String fields[] = wFields.getItem(idx);
      String dateString = fields[13];
      try {
        ValueMetaInterface stringValueMeta = replayDateMeta.clone();
        stringValueMeta.setType(ValueMetaInterface.TYPE_STRING);

        Date replayDate = stringValueMeta.getDate(dateString);

        spoon.executeJob(jobGraph.getManagedObject(), true, false, replayDate, false);
      } catch (KettleException e1) {
        new ErrorDialog(jobGraph.getShell(), Messages.getString("TransHistory.Error.ReplayingTransformation2"), //$NON-NLS-1$
            Messages.getString("TransHistory.Error.InvalidReplayDate") + dateString, e1); //$NON-NLS-1$
      }
    }
  }

  /**
   * Refreshes the history window in Spoon: reads entries from the specified log table in the Job Settings dialog.
   */
  public void refreshHistory() {
    JobMeta jobMeta = jobGraph.getManagedObject();

    // See if there is a job loaded that has a connection table specified.
    if (jobMeta != null && !Const.isEmpty(jobMeta.getName())) {
      if (jobMeta.getLogConnection() != null) {
        if (!Const.isEmpty(jobMeta.getLogTable())) {
          Database database = null;
          try {
            // open a connection
            database = new Database(jobMeta.getLogConnection());
            database.shareVariablesWith(jobMeta);
            database.connect();

            RowMetaInterface jobLogMeta = Database.getJobLogrecordFields(false, jobMeta.isBatchIdUsed(), jobMeta.isLogfieldUsed());
            String sql = "SELECT ";
            for (int i = 0; i < jobLogMeta.size(); i++) {
              if (i > 0)
                sql += ", ";
              sql += jobLogMeta.getValueMeta(i).getName();
            }
            sql += " FROM " + jobMeta.getLogTable();
            sql += " WHERE jobName= ?";
            sql += " ORDER BY ID_JOB DESC";

            RowMetaAndData params = new RowMetaAndData();
            params.addValue(new ValueMeta("transname", ValueMetaInterface.TYPE_STRING), jobMeta.getName()); //$NON-NLS-1$
            ResultSet resultSet = database.openQuery(sql, params.getRowMeta(), params.getData()); //$NON-NLS-1$ //$NON-NLS-2$

            rowList = new ArrayList<RowMetaAndData>();
            Object[] rowData = database.getRow(resultSet);
            while (rowData != null) {
              RowMetaInterface rowMeta = database.getReturnRowMeta();
              rowList.add(new RowMetaAndData(rowMeta, rowData));
              if (rowList.size() < Props.getInstance().getMaxNrLinesInHistory() || Props.getInstance().getMaxNrLinesInHistory() <= 0) {
                rowData = database.getRow(resultSet);
              } else {
                break;
              }
            }
            database.closeQuery(resultSet);

            if (rowList.size() > 0) {
              RowMetaInterface displayMeta = null;
              if (wFields.table.isDisposed())
                return;
              wFields.table.clearAll();

              // OK, now that we have a series of rows, we can add them to the table view...
              // This needs to happen in a particular order and it also needs a few calculated fields.
              // As such, it's not that simple...
              //
              for (int i = 0; i < rowList.size(); i++) {
                RowMetaAndData row = rowList.get(i);

                if (displayMeta == null) {
                  displayMeta = row.getRowMeta();

                  // Displaying it just like that adds way too many zeroes to the numbers.
                  // So we set the lengths to -1 of the integers...
                  //
                  for (int v = 0; v < displayMeta.size(); v++) {
                    ValueMetaInterface valueMeta = displayMeta.getValueMeta(v);

                    if (valueMeta.isNumeric()) {
                      valueMeta.setLength(-1, -1);
                    }
                    if (valueMeta.isDate()) {
                      valueMeta.setConversionMask("yyyy/MM/dd HH:mm:ss");
                    }
                  }

                  // Set the correct valueMeta objects on the view
                  //
                  int idx = 0;
                  colinf[idx++].setValueMeta(displayMeta.searchValueMeta("JOBNAME"));
                  colinf[idx++].setValueMeta(displayMeta.searchValueMeta("ID_JOB"));
                  colinf[idx++].setValueMeta(displayMeta.searchValueMeta("STATUS"));
                  colinf[idx++].setValueMeta(durationMeta);
                  colinf[idx++].setValueMeta(displayMeta.searchValueMeta("LINES_READ"));
                  colinf[idx++].setValueMeta(displayMeta.searchValueMeta("LINES_WRITTEN"));
                  colinf[idx++].setValueMeta(displayMeta.searchValueMeta("LINES_UPDATED"));
                  colinf[idx++].setValueMeta(displayMeta.searchValueMeta("LINES_INPUT"));
                  colinf[idx++].setValueMeta(displayMeta.searchValueMeta("LINES_OUTPUT"));
                  colinf[idx++].setValueMeta(displayMeta.searchValueMeta("ERRORS"));
                  colinf[idx++].setValueMeta(displayMeta.searchValueMeta("STARTDATE"));
                  colinf[idx++].setValueMeta(displayMeta.searchValueMeta("ENDDATE"));
                  colinf[idx++].setValueMeta(displayMeta.searchValueMeta("LOGDATE"));
                  colinf[idx++].setValueMeta(displayMeta.searchValueMeta("DEPDATE"));
                  replayDateMeta = displayMeta.searchValueMeta("REPLAYDATE");
                  colinf[idx++].setValueMeta(replayDateMeta);
                }

                TableItem item = new TableItem(wFields.table, SWT.NONE);
                int idJobIndex = row.getRowMeta().indexOfValue("ID_JOB");
                String batchID = idJobIndex < 0 ? "" : row.getString(idJobIndex, "");
                int colnr = 1;

                // Duration : from DEPDATE to ENDDATE
                //
                Long duration = null;
                Date endDate = row.getDate("ENDDATE", null);
                Date depDate = row.getDate("DEPDATE", null);
                if (endDate != null && depDate != null) {
                  duration = (endDate.getTime() - depDate.getTime()) / 1000; // convert to seconds.
                }

                item.setText(colnr++, Const.NVL(row.getString("JOBNAME", ""), "")); //$NON-NLS-1$ //$NON-NLS-2$
                item.setText(colnr++, Const.NVL(batchID, "")); //$NON-NLS-1$
                item.setText(colnr++, Const.NVL(row.getString("STATUS", ""), "")); //$NON-NLS-1$ //$NON-NLS-2$
                item.setText(colnr++, duration == null ? "" : duration.toString()); //$NON-NLS-1$ //$NON-NLS-2$
                item.setText(colnr++, Const.NVL(row.getString("LINES_READ", ""), "")); //$NON-NLS-1$ //$NON-NLS-2$
                item.setText(colnr++, Const.NVL(row.getString("LINES_WRITTEN", ""), "")); //$NON-NLS-1$ //$NON-NLS-2$
                item.setText(colnr++, Const.NVL(row.getString("LINES_UPDATED", ""), "")); //$NON-NLS-1$ //$NON-NLS-2$
                item.setText(colnr++, Const.NVL(row.getString("LINES_INPUT", ""), "")); //$NON-NLS-1$ //$NON-NLS-2$
                item.setText(colnr++, Const.NVL(row.getString("LINES_OUTPUT", ""), "")); //$NON-NLS-1$ //$NON-NLS-2$
                item.setText(colnr++, Const.NVL(row.getString("ERRORS", ""), "")); //$NON-NLS-1$ //$NON-NLS-2$
                item.setText(colnr++, Const.NVL(row.getString("STARTDATE", ""), "")); //$NON-NLS-1$ //$NON-NLS-2$
                item.setText(colnr++, Const.NVL(row.getString("ENDDATE", ""), "")); //$NON-NLS-1$ //$NON-NLS-2$
                item.setText(colnr++, Const.NVL(row.getString("LOGDATE", ""), "")); //$NON-NLS-1$ //$NON-NLS-2$
                item.setText(colnr++, Const.NVL(row.getString("DEPDATE", ""), "")); //$NON-NLS-1$ //$NON-NLS-2$
                item.setText(colnr++, Const.NVL(row.getString("REPLAYDATE", ""), "")); //$NON-NLS-1$ //$NON-NLS-2$
              }

              wFields.removeEmptyRows();
              wFields.setRowNums();
              wFields.optWidth(true);
              wFields.table.setSelection(0);

              showLogEntry();
            }
          } catch (KettleException e) {
            new ErrorDialog(jobGraph.getShell(), Messages.getString("JobHistory.Error.GettingLoggingInfo"), Messages.getString("JobHistory.Error.GettingInfoFromLoggingTable"), e); //$NON-NLS-1$ //$NON-NLS-2$
            wFields.clearAll(false);
          } finally {
            if (database != null)
              database.disconnect();
          }

        } else {
          if (!wFields.isDisposed())
            wFields.clearAll(false);
        }
      } else {
        if (!wFields.isDisposed())
          wFields.clearAll(false);
      }
    } else {
      if (!wFields.isDisposed())
        wFields.clearAll(false);
    }
  }

  public void showLogEntry() {
    if (rowList == null) {
      wText.setText(""); //$NON-NLS-1$
      return;
    }

    // grab the selected line in the table:
    int nr = wFields.table.getSelectionIndex();
    if (nr >= 0 && rowList != null && nr < rowList.size()) {
      // OK, grab this one from the buffer...
      RowMetaAndData row = rowList.get(nr);
      String logging = null;
      try {
        int logFieldIndex = row.getRowMeta().indexOfValue("LOG_FIELD");
        logging = logFieldIndex < 0 ? "-" : row.getString(logFieldIndex, ""); //$NON-NLS-1$ $NON-NLS-2$ $NON-NLS-3$
      } catch (KettleValueException e) {
        new ErrorDialog(jobGraph.getShell(), Messages.getString("JobHistory.Error.GettingLoggingInfo"), Messages.getString("JobHistory.Error.GettingLogFieldFromLoggingTable"), e); //$NON-NLS-1$ //$NON-NLS-2$
      }
      if (logging != null) {
        wText.setText(logging);
      } else {
        wText.setText(""); //$NON-NLS-1$
      }
    }
  }

  /**
   * @return the transHistoryTab
   */
  public CTabItem getJobHistoryTab() {
    return jobHistoryTab;
  }

  /* (non-Javadoc)
   * @see org.pentaho.ui.xul.impl.XulEventHandler#getData()
   */
  public Object getData() {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see org.pentaho.ui.xul.impl.XulEventHandler#getName()
   */
  public String getName() {
    return "history";
  }

  /* (non-Javadoc)
   * @see org.pentaho.ui.xul.impl.XulEventHandler#getXulDomContainer()
   */
  public XulDomContainer getXulDomContainer() {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see org.pentaho.ui.xul.impl.XulEventHandler#setData(java.lang.Object)
   */
  public void setData(Object data) {
    // TODO Auto-generated method stub

  }

  /* (non-Javadoc)
   * @see org.pentaho.ui.xul.impl.XulEventHandler#setName(java.lang.String)
   */
  public void setName(String name) {
    // TODO Auto-generated method stub

  }

  /* (non-Javadoc)
   * @see org.pentaho.ui.xul.impl.XulEventHandler#setXulDomContainer(org.pentaho.ui.xul.XulDomContainer)
   */
  public void setXulDomContainer(XulDomContainer xulDomContainer) {
    // TODO Auto-generated method stub

  }

}
