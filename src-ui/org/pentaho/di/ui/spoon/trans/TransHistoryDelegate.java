package org.pentaho.di.ui.spoon.trans;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
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
import org.pentaho.di.i18n.LanguageChoice;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.spoon.Messages;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.ui.spoon.delegates.SpoonDelegate;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.XulLoader;
import org.pentaho.ui.xul.components.XulToolbarbutton;
import org.pentaho.ui.xul.containers.XulToolbar;
import org.pentaho.ui.xul.impl.XulEventHandler;
import org.pentaho.ui.xul.swt.SwtXulLoader;

public class TransHistoryDelegate extends SpoonDelegate implements XulEventHandler {

  private static final String XUL_FILE_TRANS_GRID_TOOLBAR = "ui/trans-history-toolbar.xul";
  private static final LogWriter log = LogWriter.getInstance();

  private TransGraph transGraph;

  private CTabItem transHistoryTab;

  private ColumnInfo[] colinf;

  private Text wText;

  private TableView wFields;

  private FormData fdText, fdSash;

  private List<RowMetaAndData> rowList;

  private boolean refreshNeeded = true;

  private Object refreshNeededLock = new Object();

  private ValueMetaInterface durationMeta;

  private ValueMetaInterface replayDateMeta;

  private XulToolbar toolbar;

  private Composite transHistoryComposite;

  /**
   * @param spoon
   * @param transGraph
   */
  public TransHistoryDelegate(Spoon spoon, TransGraph transGraph) {
    super(spoon);
    this.transGraph = transGraph;
  }

  public void addTransHistory() {
    // First, see if we need to add the extra view...
    //
    if (transGraph.extraViewComposite == null || transGraph.extraViewComposite.isDisposed()) {
      transGraph.addExtraView();
    } else {
      if (transHistoryTab != null && !transHistoryTab.isDisposed()) {
        // just set this one active and get out...
        //
        transGraph.extraViewTabFolder.setSelection(transHistoryTab);
        return;
      }
    }

    // Add a transLogTab : display the logging...
    //
    transHistoryTab = new CTabItem(transGraph.extraViewTabFolder, SWT.NONE);
    transHistoryTab.setImage(GUIResource.getInstance().getImageShowHistory());
    transHistoryTab.setText(Messages.getString("Spoon.TransGraph.HistoryTab.Name"));

    // Create a composite, slam everything on there like it was in the history tab.
    //
    transHistoryComposite = new Composite(transGraph.extraViewTabFolder, SWT.NONE);
    transHistoryComposite.setLayout(new FormLayout());
    spoon.props.setLook(transHistoryComposite);

    addToolBar();

    SashForm sash = new SashForm(transHistoryComposite, SWT.VERTICAL);

    sash.setLayout(new FillLayout());

    final int FieldsRows = 1;
    colinf = new ColumnInfo[] { new ColumnInfo(Messages.getString("TransHistory.Column.Name"), ColumnInfo.COLUMN_TYPE_TEXT, true, true), //$NON-NLS-1$
        new ColumnInfo(Messages.getString("TransHistory.Column.BatchID"), ColumnInfo.COLUMN_TYPE_TEXT, true, true), //$NON-NLS-1$
        new ColumnInfo(Messages.getString("TransHistory.Column.Status"), ColumnInfo.COLUMN_TYPE_TEXT, false, true), //$NON-NLS-1$
        new ColumnInfo(Messages.getString("TransHistory.Column.Duration"), ColumnInfo.COLUMN_TYPE_TEXT, true, true), //$NON-NLS-1$
        new ColumnInfo(Messages.getString("TransHistory.Column.Read"), ColumnInfo.COLUMN_TYPE_TEXT, true, true), //$NON-NLS-1$
        new ColumnInfo(Messages.getString("TransHistory.Column.Written"), ColumnInfo.COLUMN_TYPE_TEXT, true, true), //$NON-NLS-1$
        new ColumnInfo(Messages.getString("TransHistory.Column.Updated"), ColumnInfo.COLUMN_TYPE_TEXT, true, true), //$NON-NLS-1$
        new ColumnInfo(Messages.getString("TransHistory.Column.Input"), ColumnInfo.COLUMN_TYPE_TEXT, true, true), //$NON-NLS-1$
        new ColumnInfo(Messages.getString("TransHistory.Column.Output"), ColumnInfo.COLUMN_TYPE_TEXT, true, true), //$NON-NLS-1$
        new ColumnInfo(Messages.getString("TransHistory.Column.Errors"), ColumnInfo.COLUMN_TYPE_TEXT, true, true), //$NON-NLS-1$
        new ColumnInfo(Messages.getString("TransHistory.Column.StartDate"), ColumnInfo.COLUMN_TYPE_TEXT, false, true), //$NON-NLS-1$
        new ColumnInfo(Messages.getString("TransHistory.Column.EndDate"), ColumnInfo.COLUMN_TYPE_TEXT, false, true), //$NON-NLS-1$
        new ColumnInfo(Messages.getString("TransHistory.Column.LogDate"), ColumnInfo.COLUMN_TYPE_TEXT, false, true), //$NON-NLS-1$
        new ColumnInfo(Messages.getString("TransHistory.Column.DependencyDate"), ColumnInfo.COLUMN_TYPE_TEXT, false, true), //$NON-NLS-1$
        new ColumnInfo(Messages.getString("TransHistory.Column.ReplayDate"), ColumnInfo.COLUMN_TYPE_TEXT, false, true) //$NON-NLS-1$
    };

    for (int i = 3; i < 10; i++)
      colinf[i].setAllignement(SWT.RIGHT);

    // Create the duration value meta data
    //
    durationMeta = new ValueMeta("DURATION", ValueMetaInterface.TYPE_NUMBER);
    durationMeta.setConversionMask("0");
    colinf[2].setValueMeta(durationMeta);

    wFields = new TableView(transGraph.getManagedObject(), sash, SWT.BORDER | SWT.FULL_SELECTION | SWT.SINGLE, colinf, FieldsRows, true, // readonly!
        null, spoon.props);

    wText = new Text(sash, SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL | SWT.READ_ONLY);
    spoon.props.setLook(wText);
    wText.setVisible(true);

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

    transHistoryComposite.pack();

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

    transHistoryTab.setControl(transHistoryComposite);

    transGraph.extraViewTabFolder.setSelection(transHistoryTab);

    // Launch a refresh in the background...
    //
    transGraph.getDisplay().asyncExec(new Runnable() {
      public void run() {
        refreshHistory();
        if (!transHistoryComposite.isDisposed())
          transHistoryComposite.layout(true, true);
        showLogEntry();
      }
    });

  }

  private void addToolBar() {

    try {
      XulLoader loader = new SwtXulLoader();
      ResourceBundle bundle = ResourceBundle.getBundle("org/pentaho/di/ui/spoon/messages/messages", LanguageChoice.getInstance().getDefaultLocale());
      XulDomContainer xulDomContainer = loader.loadXul(XUL_FILE_TRANS_GRID_TOOLBAR, bundle);
      xulDomContainer.addEventHandler(this);
      toolbar = (XulToolbar) xulDomContainer.getDocumentRoot().getElementById("nav-toolbar");

      XulToolbarbutton onlyActiveButton = (XulToolbarbutton) toolbar.getElementById("show-inactive");
      if (onlyActiveButton != null) {
        ((ToolItem) onlyActiveButton.getManagedObject()).setImage(GUIResource.getInstance().getImageHideInactive());
      }

      ToolBar swtToolBar = (ToolBar) toolbar.getManagedObject();
      swtToolBar.layout(true, true);
    } catch (Throwable t) {
      log.logError(toString(), Const.getStackTracker(t));
      new ErrorDialog(transHistoryComposite.getShell(), Messages.getString("Spoon.Exception.ErrorReadingXULFile.Title"), Messages.getString("Spoon.Exception.ErrorReadingXULFile.Message", XUL_FILE_TRANS_GRID_TOOLBAR), new Exception(t));
    }
  }

  /**
   * User requested to clear the log table.<br>
   * Better ask confirmation
   */
  public void clearLogTable() {
    String logTable = transGraph.getManagedObject().getLogTable();
    DatabaseMeta databaseMeta = transGraph.getManagedObject().getLogConnection();

    if (databaseMeta != null && !Const.isEmpty(logTable)) {

      MessageBox mb = new MessageBox(transGraph.getShell(), SWT.YES | SWT.NO | SWT.ICON_QUESTION);
      mb.setMessage(Messages.getString("TransGraph.Dialog.AreYouSureYouWantToRemoveAllLogEntries.Message", logTable)); // Nothing found that matches your criteria
      mb.setText(Messages.getString("TransGraph.Dialog.AreYouSureYouWantToRemoveAllLogEntries.Title")); // Sorry!
      if (mb.open() == SWT.YES) {
        Database database = new Database(databaseMeta);
        try {
          database.connect();
          database.truncateTable(logTable);
        } catch (Exception e) {
          new ErrorDialog(transGraph.getShell(), Messages.getString("TransGraph.Dialog.ErrorClearningLoggingTable.Title"), Messages.getString("TransGraph.Dialog.ErrorClearningLoggingTable.Message"), e);
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

  public void showHistoryView() {

    // What button?
    //
    // XulToolbarButton showLogXulButton = toolbar.getButtonById("trans-show-log");
    // ToolItem toolBarButton = (ToolItem) showLogXulButton.getNativeObject();

    if (transHistoryTab == null || transHistoryTab.isDisposed()) {
      addTransHistory();
    } else {
      transHistoryTab.dispose();

      transGraph.checkEmptyExtraView();
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

        spoon.executeTransformation(transGraph.getManagedObject(), true, false, false, false, false, replayDate, false);
      } catch (KettleException e1) {
        new ErrorDialog(transGraph.getShell(), Messages.getString("TransHistory.Error.ReplayingTransformation2"), //$NON-NLS-1$
            Messages.getString("TransHistory.Error.InvalidReplayDate") + dateString, e1); //$NON-NLS-1$
      }
    }
  }

  /**
   * Refreshes the history window in Spoon: reads entries from the specified log table in the Transformation Settings dialog.
   */
  public void refreshHistory() {
    transGraph.getDisplay().asyncExec(new Runnable() {
      public void run() {
        getHistoryData();
      }
    });
  }

  public void getHistoryData() {
    // See if there is a transformation loaded that has a connection table specified.
    TransMeta transMeta = transGraph.getManagedObject();
    if (transMeta != null && !Const.isEmpty(transMeta.getName())) {
      if (transMeta.getLogConnection() != null) {
        if (!Const.isEmpty(transMeta.getLogTable())) {
          Database database = null;
          try {
            // open a connection
            database = new Database(transMeta.getLogConnection());
            database.shareVariablesWith(transMeta);
            database.connect();

            RowMetaAndData params = new RowMetaAndData();
            params.addValue(new ValueMeta("transname_literal", ValueMetaInterface.TYPE_STRING), transMeta.getName()); //$NON-NLS-1$
            params.addValue(new ValueMeta("transname_cluster", ValueMetaInterface.TYPE_STRING), transMeta.getName() + " (%"); //$NON-NLS-1$ //$NON-NLS-2$
            ResultSet resultSet = database.openQuery("SELECT * FROM " + transMeta.getLogTable() + " WHERE TRANSNAME LIKE ? OR TRANSNAME LIKE ? ORDER BY ID_BATCH desc", params.getRowMeta(), params.getData()); //$NON-NLS-1$ //$NON-NLS-2$
            // database.getRows("SELECT * FROM "+transMeta.getLogTable()+" WHERE TRANSNAME LIKE 'test-hops-%'", 0);
            rowList = new ArrayList<RowMetaAndData>();
            Object[] rowData = database.getRow(resultSet);
            while (rowData != null) {
              rowList.add(new RowMetaAndData(database.getReturnRowMeta(), rowData));
              if (rowList.size() < Props.getInstance().getMaxNrLinesInHistory() || Props.getInstance().getMaxNrLinesInHistory() <= 0) {
                rowData = database.getRow(resultSet);
              } else {
                break;
              }
            }
            database.closeQuery(resultSet);

            if (wFields.isDisposed())
              return;

            wFields.table.clearAll();

            if (rowList.size() > 0) {
              RowMetaInterface displayMeta = null;

              // OK, now that we have a series of rows, we can add them to the table view...
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
                  colinf[0].setValueMeta(displayMeta.searchValueMeta("TRANSNAME"));
                  colinf[1].setValueMeta(displayMeta.searchValueMeta("ID_BATCH"));
                  colinf[2].setValueMeta(displayMeta.searchValueMeta("STATUS"));
                  colinf[3].setValueMeta(durationMeta);
                  colinf[4].setValueMeta(displayMeta.searchValueMeta("LINES_READ"));
                  colinf[5].setValueMeta(displayMeta.searchValueMeta("LINES_WRITTEN"));
                  colinf[6].setValueMeta(displayMeta.searchValueMeta("LINES_UPDATED"));
                  colinf[7].setValueMeta(displayMeta.searchValueMeta("LINES_INPUT"));
                  colinf[8].setValueMeta(displayMeta.searchValueMeta("LINES_OUTPUT"));
                  colinf[9].setValueMeta(displayMeta.searchValueMeta("ERRORS"));
                  colinf[10].setValueMeta(displayMeta.searchValueMeta("STARTDATE"));
                  colinf[11].setValueMeta(displayMeta.searchValueMeta("ENDDATE"));
                  colinf[12].setValueMeta(displayMeta.searchValueMeta("LOGDATE"));
                  colinf[13].setValueMeta(displayMeta.searchValueMeta("DEPDATE"));
                  replayDateMeta = displayMeta.searchValueMeta("REPLAYDATE");
                  colinf[14].setValueMeta(replayDateMeta);
                }

                TableItem item = new TableItem(wFields.table, SWT.NONE);
                String batchID = row.getString("ID_BATCH", "");
                int index = 1;
                item.setText(index++, Const.NVL(row.getString("TRANSNAME", ""), "")); //$NON-NLS-1$ //$NON-NLS-2$

                // LOGDATE - REPLAYDATE --> duration
                //
                Date logDate = row.getDate("LOGDATE", null);
                Date replayDate = row.getDate("REPLAYDATE", null);

                Double duration = null;
                if (logDate != null && replayDate != null) {
                  duration = new Double(((double) logDate.getTime() - (double) replayDate.getTime()) / 1000);
                }

                // Display the data...
                //
                if (batchID != null)
                  item.setText(index++, batchID); //$NON-NLS-1$ //$NON-NLS-2$
                item.setText(index++, Const.NVL(row.getString("STATUS", ""), "")); //$NON-NLS-1$ //$NON-NLS-2$
                item.setText(index++, Const.NVL(durationMeta.getString(duration), "")); //$NON-NLS-1$ //$NON-NLS-2$
                item.setText(index++, Const.NVL(row.getString("LINES_READ", ""), "")); //$NON-NLS-1$ //$NON-NLS-2$
                item.setText(index++, Const.NVL(row.getString("LINES_WRITTEN", ""), "")); //$NON-NLS-1$ //$NON-NLS-2$
                item.setText(index++, Const.NVL(row.getString("LINES_UPDATED", ""), "")); //$NON-NLS-1$ //$NON-NLS-2$
                item.setText(index++, Const.NVL(row.getString("LINES_INPUT", ""), "")); //$NON-NLS-1$ //$NON-NLS-2$
                item.setText(index++, Const.NVL(row.getString("LINES_OUTPUT", ""), "")); //$NON-NLS-1$ //$NON-NLS-2$
                item.setText(index++, Const.NVL(row.getString("ERRORS", ""), "")); //$NON-NLS-1$ //$NON-NLS-2$
                item.setText(index++, Const.NVL(row.getString("STARTDATE", ""), "")); //$NON-NLS-1$ //$NON-NLS-2$
                item.setText(index++, Const.NVL(row.getString("ENDDATE", ""), "")); //$NON-NLS-1$ //$NON-NLS-2$
                item.setText(index++, Const.NVL(row.getString("LOGDATE", ""), "")); //$NON-NLS-1$ //$NON-NLS-2$
                item.setText(index++, Const.NVL(row.getString("DEPDATE", ""), "")); //$NON-NLS-1$ //$NON-NLS-2$
                item.setText(index++, Const.NVL(row.getString("REPLAYDATE", ""), "")); //$NON-NLS-1$ //$NON-NLS-2$

                String status = row.getString("STATUS", "");
                Long errors = row.getInteger("ERRORS", 0L);
                if (errors != null && errors.longValue() > 0L) {
                  item.setBackground(GUIResource.getInstance().getColorRed());
                } else if ("stop".equals(status)) {
                  item.setBackground(GUIResource.getInstance().getColorYellow());
                }
              }

              wFields.removeEmptyRows();
              wFields.setRowNums();
              wFields.optWidth(true);
              wFields.table.setSelection(0);

              showLogEntry();
            } else {
              new TableItem(wFields.table, SWT.NONE); // Give it an item to prevent errors on various platforms.
            }
          } catch (KettleException e) {
            StringBuffer message = new StringBuffer();
            message.append(Messages.getString("TransHistory.Error.GettingInfoFromLoggingTable")).append(Const.CR).append(Const.CR);
            message.append(e.toString()).append(Const.CR).append(Const.CR);
            message.append(Const.getStackTracker(e)).append(Const.CR);
            wText.setText(message.toString());
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
    if (wText.isDisposed())
      return;

    if (rowList == null) {
      String message;
      TransMeta transMeta = transGraph.getManagedObject();
      if (transMeta.getLogConnection() == null || Const.isEmpty(transMeta.getLogTable())) {
        message = Messages.getString("TransHistory.HistoryConfiguration.Message");
      } else {
        message = Messages.getString("TransHistory.PleaseRefresh.Message");
      }
      wText.setText(message);
      return;
    }

    // grab the selected line in the table:
    int nr = wFields.table.getSelectionIndex();
    if (nr >= 0 && rowList != null && nr < rowList.size()) {
      // OK, grab this one from the buffer...
      RowMetaAndData row = rowList.get(nr);
      try {
        wText.setText(row.getString("LOG_FIELD", ""));
      } catch (KettleValueException e) {
        // Should never happen
      }
    }
  }

  public void refreshHistoryIfNeeded() {
    boolean reallyRefresh = false;
    synchronized (refreshNeededLock) {
      reallyRefresh = refreshNeeded;
      refreshNeeded = false;
    }

    if (reallyRefresh) {
      refreshHistory();
    }
  }

  public void markRefreshNeeded() {
    synchronized (refreshNeededLock) {
      refreshNeeded = true;
    }
  }

  /**
   * @return the transHistoryTab
   */
  public CTabItem getTransHistoryTab() {
    return transHistoryTab;
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
    return "transhistory";
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
