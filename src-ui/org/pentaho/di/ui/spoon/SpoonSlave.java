/*******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.ui.spoon;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TreeEvent;
import org.eclipse.swt.events.TreeListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.EngineMetaInterface;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.ObjectRevision;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.repository.RepositoryObjectType;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.step.StepStatus;
import org.pentaho.di.ui.core.ConstUI;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.dialog.EnterNumberDialog;
import org.pentaho.di.ui.core.dialog.EnterSelectionDialog;
import org.pentaho.di.ui.core.dialog.EnterTextDialog;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.dialog.PreviewRowsDialog;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.TreeMemory;
import org.pentaho.di.ui.core.widget.TreeUtil;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.www.SlaveServerJobStatus;
import org.pentaho.di.www.SlaveServerStatus;
import org.pentaho.di.www.SlaveServerTransStatus;
import org.pentaho.di.www.SniffStepServlet;
import org.pentaho.di.www.WebResult;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * SpoonSlave handles the display of the slave server information in a Spoon tab.
 * 
 * @see org.pentaho.di.spoon.Spoon
 * @author Matt
 * @since 12 nov 2006
 */
public class SpoonSlave extends Composite implements TabItemInterface {
  private static Class<?> PKG = Spoon.class; // for i18n purposes, needed by Translator2!! $NON-NLS-1$

  public static final long UPDATE_TIME_VIEW = 30000L; // 30s

  public static final String STRING_SLAVE_LOG_TREE_NAME = "SLAVE_LOG : ";

  private Shell shell;
  private Display display;
  private SlaveServer slaveServer;
  private Map<String, Integer> lastLineMap;
  private Map<String, String> loggingMap;

  private Spoon spoon;

  private ColumnInfo[] colinf;

  private Tree wTree;
  private Text wText;

  private Button wError;
  private Button wStart;
  private Button wPause;
  private Button wStop;
  private Button wRemove;
  private Button wSniff;
  private Button wRefresh;

  private FormData fdTree, fdText, fdSash;

  private boolean refreshBusy;
  private SlaveServerStatus slaveServerStatus;
  private Timer timer;
  private TimerTask timerTask;

  private TreeItem transParentItem;

  private TreeItem jobParentItem;

  private LogChannelInterface log;

  private class TreeEntry {
    String itemType; // Transformation or Job
    String name;
    String status;
    String id;
    int length;

    public TreeEntry(TreeItem treeItem) {
      String[] path = ConstUI.getTreeStrings(treeItem);
      this.length = path.length;
      if (path.length > 0) {
        itemType = path[0];
      }
      if (path.length > 1) {
        name = path[1];
      }
      if (path.length == 3) {
        treeItem = treeItem.getParentItem();
      }
      status = treeItem.getText(9);
      id = treeItem.getText(13);
    }

    boolean isTransformation() {
      return itemType.equals(transParentItem.getText());
    }

    boolean isJob() {
      return itemType.equals(jobParentItem.getText());
    }

    boolean isRunning() {
      return Trans.STRING_RUNNING.equals(status);
    }

    boolean isStopped() {
      return Trans.STRING_STOPPED.equals(status);
    }

    boolean isFinished() {
      return Trans.STRING_FINISHED.equals(status);
    }

    boolean isPaused() {
      return Trans.STRING_PAUSED.equals(status);
    }

    boolean isWaiting() {
      return Trans.STRING_WAITING.equals(status);
    }
  }

  public SpoonSlave(Composite parent, int style, final Spoon spoon, SlaveServer slaveServer) {
    super(parent, style);
    this.shell = parent.getShell();
    this.display = shell.getDisplay();
    this.spoon = spoon;
    this.slaveServer = slaveServer;
    this.log = spoon.getLog();

    lastLineMap = new HashMap<String, Integer>();
    loggingMap = new HashMap<String, String>();

    FormLayout formLayout = new FormLayout();
    formLayout.marginWidth = Const.FORM_MARGIN;
    formLayout.marginHeight = Const.FORM_MARGIN;

    setLayout(formLayout);

    setVisible(true);
    spoon.props.setLook(this);

    SashForm sash = new SashForm(this, SWT.VERTICAL);

    sash.setLayout(new FillLayout());

    colinf = new ColumnInfo[] { new ColumnInfo(BaseMessages.getString(PKG, "SpoonSlave.Column.Stepname"), ColumnInfo.COLUMN_TYPE_TEXT, false, true), //$NON-NLS-1$
        new ColumnInfo(BaseMessages.getString(PKG, "SpoonSlave.Column.Copynr"), ColumnInfo.COLUMN_TYPE_TEXT, false, true), //$NON-NLS-1$
        new ColumnInfo(BaseMessages.getString(PKG, "SpoonSlave.Column.Read"), ColumnInfo.COLUMN_TYPE_TEXT, false, true), //$NON-NLS-1$
        new ColumnInfo(BaseMessages.getString(PKG, "SpoonSlave.Column.Written"), ColumnInfo.COLUMN_TYPE_TEXT, false, true), //$NON-NLS-1$
        new ColumnInfo(BaseMessages.getString(PKG, "SpoonSlave.Column.Input"), ColumnInfo.COLUMN_TYPE_TEXT, false, true), //$NON-NLS-1$
        new ColumnInfo(BaseMessages.getString(PKG, "SpoonSlave.Column.Output"), ColumnInfo.COLUMN_TYPE_TEXT, false, true), //$NON-NLS-1$
        new ColumnInfo(BaseMessages.getString(PKG, "SpoonSlave.Column.Updated"), ColumnInfo.COLUMN_TYPE_TEXT, false, true), //$NON-NLS-1$
        new ColumnInfo(BaseMessages.getString(PKG, "SpoonSlave.Column.Rejected"), ColumnInfo.COLUMN_TYPE_TEXT, false, true), //$NON-NLS-1$
        new ColumnInfo(BaseMessages.getString(PKG, "SpoonSlave.Column.Errors"), ColumnInfo.COLUMN_TYPE_TEXT, false, true), //$NON-NLS-1$
        new ColumnInfo(BaseMessages.getString(PKG, "SpoonSlave.Column.Active"), ColumnInfo.COLUMN_TYPE_TEXT, false, true), //$NON-NLS-1$
        new ColumnInfo(BaseMessages.getString(PKG, "SpoonSlave.Column.Time"), ColumnInfo.COLUMN_TYPE_TEXT, false, true), //$NON-NLS-1$
        new ColumnInfo(BaseMessages.getString(PKG, "SpoonSlave.Column.Speed"), ColumnInfo.COLUMN_TYPE_TEXT, false, true), //$NON-NLS-1$
        new ColumnInfo(BaseMessages.getString(PKG, "SpoonSlave.Column.PriorityBufferSizes"), ColumnInfo.COLUMN_TYPE_TEXT, false, true), //$NON-NLS-1$
        new ColumnInfo(BaseMessages.getString(PKG, "SpoonSlave.Column.CarteObjectId"), ColumnInfo.COLUMN_TYPE_TEXT, false, true), //$NON-NLS-1$
    };

    colinf[1].setAllignement(SWT.RIGHT);
    colinf[2].setAllignement(SWT.RIGHT);
    colinf[3].setAllignement(SWT.RIGHT);
    colinf[4].setAllignement(SWT.RIGHT);
    colinf[5].setAllignement(SWT.RIGHT);
    colinf[6].setAllignement(SWT.RIGHT);
    colinf[7].setAllignement(SWT.RIGHT);
    colinf[8].setAllignement(SWT.RIGHT);
    colinf[9].setAllignement(SWT.RIGHT);
    colinf[10].setAllignement(SWT.RIGHT);
    colinf[11].setAllignement(SWT.RIGHT);
    colinf[12].setAllignement(SWT.RIGHT);

    wTree = new Tree(sash, SWT.SINGLE | SWT.V_SCROLL | SWT.H_SCROLL);
    wTree.setHeaderVisible(true);
    TreeMemory.addTreeListener(wTree, STRING_SLAVE_LOG_TREE_NAME + slaveServer.toString());
    Rectangle bounds = spoon.tabfolder.getSwtTabset().getBounds();
    for (int i = 0; i < colinf.length; i++) {
      ColumnInfo columnInfo = colinf[i];
      TreeColumn treeColumn = new TreeColumn(wTree, columnInfo.getAllignement());
      treeColumn.setText(columnInfo.getName());
      treeColumn.setWidth(bounds.width / colinf.length);
    }

    transParentItem = new TreeItem(wTree, SWT.NONE);
    transParentItem.setText(Spoon.STRING_TRANSFORMATIONS);
    transParentItem.setImage(GUIResource.getInstance().getImageTransGraph());

    jobParentItem = new TreeItem(wTree, SWT.NONE);
    jobParentItem.setText(Spoon.STRING_JOBS);
    jobParentItem.setImage(GUIResource.getInstance().getImageJobGraph());

    wTree.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent event) {
        enableButtons();
        treeItemSelected((TreeItem) event.item);
        ((TreeItem) event.item).setExpanded(true);
        showLog();
      }
    });

    wTree.addTreeListener(new TreeListener() {

      public void treeExpanded(TreeEvent event) {
        treeItemSelected((TreeItem) event.item);
        showLog();
      }

      public void treeCollapsed(TreeEvent arg0) {
      }
    });

    wText = new Text(sash, SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL | SWT.READ_ONLY | SWT.BORDER);
    spoon.props.setLook(wText);
    wText.setVisible(true);

    wRefresh = new Button(this, SWT.PUSH);
    wRefresh.setText(BaseMessages.getString(PKG, "SpoonSlave.Button.Refresh"));
    wRefresh.setEnabled(true);
    wRefresh.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        refreshViewAndLog();
      }
    });

    wError = new Button(this, SWT.PUSH);
    wError.setText(BaseMessages.getString(PKG, "SpoonSlave.Button.ShowErrorLines")); //$NON-NLS-1$
    wError.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        showErrors();
      }
    });

    wSniff = new Button(this, SWT.PUSH);
    wSniff.setText(BaseMessages.getString(PKG, "SpoonSlave.Button.Sniff"));
    wSniff.setEnabled(false);
    wSniff.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        sniff();
      }
    });

    wStart = new Button(this, SWT.PUSH);
    wStart.setText(BaseMessages.getString(PKG, "SpoonSlave.Button.Start"));
    wStart.setEnabled(false);
    wStart.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        start();
      }
    });

    wPause = new Button(this, SWT.PUSH);
    wPause.setText(BaseMessages.getString(PKG, "SpoonSlave.Button.Pause"));
    wPause.setEnabled(false);
    wPause.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        pause();
      }
    });

    wStop = new Button(this, SWT.PUSH);
    wStop.setText(BaseMessages.getString(PKG, "SpoonSlave.Button.Stop"));
    wStop.setEnabled(false);
    wStop.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        stop();
      }
    });

    wRemove = new Button(this, SWT.PUSH);
    wRemove.setText(BaseMessages.getString(PKG, "SpoonSlave.Button.Remove"));
    wRemove.setEnabled(false);
    wRemove.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        remove();
      }
    });

    BaseStepDialog.positionBottomButtons(this, new Button[] { wRefresh, wSniff, wStart, wPause, wStop, wRemove, wError }, Const.MARGIN, null);

    // Put tree on top
    fdTree = new FormData();
    fdTree.left = new FormAttachment(0, 0);
    fdTree.top = new FormAttachment(0, 0);
    fdTree.right = new FormAttachment(100, 0);
    fdTree.bottom = new FormAttachment(100, 0);
    wTree.setLayoutData(fdTree);

    // Put text in the middle
    fdText = new FormData();
    fdText.left = new FormAttachment(0, 0);
    fdText.top = new FormAttachment(0, 0);
    fdText.right = new FormAttachment(100, 0);
    fdText.bottom = new FormAttachment(100, 0);
    wText.setLayoutData(fdText);

    fdSash = new FormData();
    fdSash.left = new FormAttachment(0, 0); // First one in the left top corner
    fdSash.top = new FormAttachment(0, 0);
    fdSash.right = new FormAttachment(100, 0);
    fdSash.bottom = new FormAttachment(wRefresh, -5);
    sash.setLayoutData(fdSash);

    pack();

    timer = new Timer("SpoonSlave: " + getMeta().getName());

    timerTask = new TimerTask() {
      public void run() {
        if (display != null && !display.isDisposed()) {
          display.asyncExec(new Runnable() {
            public void run() {
              refreshViewAndLog();
            }
          });
        }
      }
    };

    timer.schedule(timerTask, 0L, UPDATE_TIME_VIEW); // schedule to repeat a couple of times per second to get fast feedback

    addDisposeListener(new DisposeListener() {
      public void widgetDisposed(DisposeEvent e) {
        timer.cancel();
      }
    });
  }

  public void treeItemSelected(TreeItem item) {
    // load node upon expansion
    if (item.getData("transStatus") != null) {
      SlaveServerTransStatus transStatus = (SlaveServerTransStatus) item.getData("transStatus");
      try {
        log.logDetailed("Getting transformation status for [{0}] on server [{1}]", transStatus.getTransName(), SpoonSlave.this.slaveServer);

        Integer lastLine = lastLineMap.get(transStatus.getId());
        int lastLineNr = lastLine == null ? 0 : lastLine.intValue();

        SlaveServerTransStatus ts = SpoonSlave.this.slaveServer.getTransStatus(transStatus.getTransName(), transStatus.getId(), lastLineNr);
        log.logDetailed("Finished receiving transformation status for [{0}] from server [{1}]", transStatus.getTransName(), SpoonSlave.this.slaveServer);
        List<StepStatus> stepStatusList = ts.getStepStatusList();
        transStatus.setStepStatusList(stepStatusList);

        lastLineMap.put(transStatus.getId(), ts.getLastLoggingLineNr());
        String logging = loggingMap.get(transStatus.getId());
        if (logging == null) {
          logging = ts.getLoggingString();
        } else {
          logging = new StringBuffer(logging).append(ts.getLoggingString()).toString();
        }

        String[] lines = logging.split("\r\n|\r|\n"); //$NON-NLS-1$
        if(lines.length > PropsUI.getInstance().getMaxNrLinesInLog()) {
          // Trim to view the last x lines
          int offset = lines.length - PropsUI.getInstance().getMaxNrLinesInLog();
          StringBuffer trimmedLog = new StringBuffer();
          // Keep only the text from offset to the end of the log
          while(offset != lines.length) {
            trimmedLog.append(lines[offset++] + '\n');
          }
          logging = trimmedLog.toString();
        }
        
        loggingMap.put(transStatus.getId(), logging);
        
        item.removeAll();
        for (int s = 0; s < stepStatusList.size(); s++) {
          StepStatus stepStatus = stepStatusList.get(s);
          TreeItem stepItem = new TreeItem(item, SWT.NONE);
          stepItem.setText(stepStatus.getSpoonSlaveLogFields());
        }
      } catch (Exception e) {
        transStatus.setErrorDescription("Unable to access transformation details : " + Const.CR + Const.getStackTracker(e));
      }
    } else if (item.getData("jobStatus") != null) {
      SlaveServerJobStatus jobStatus = (SlaveServerJobStatus) item.getData("jobStatus");
      try {
        log.logDetailed("Getting job status for [{0}] on server [{1}]", jobStatus.getJobName(), slaveServer);

        Integer lastLine = lastLineMap.get(jobStatus.getId());
        int lastLineNr = lastLine == null ? 0 : lastLine.intValue();

        SlaveServerJobStatus ts = slaveServer.getJobStatus(jobStatus.getJobName(), jobStatus.getId(), lastLineNr);

        log.logDetailed("Finished receiving job status for [{0}] from server [{1}]", jobStatus.getJobName(), slaveServer);

        lastLineMap.put(jobStatus.getId(), ts.getLastLoggingLineNr());
        String logging = loggingMap.get(jobStatus.getId());
        if (logging == null) {
          logging = ts.getLoggingString();
        } else {
          logging = new StringBuffer(logging).append(ts.getLoggingString()).toString();
        }
        
        String[] lines = logging.split("\r\n|\r|\n"); //$NON-NLS-1$
        if(lines.length > PropsUI.getInstance().getMaxNrLinesInLog()) {
          // Trim to view the last x lines
          int offset = lines.length - PropsUI.getInstance().getMaxNrLinesInLog();
          StringBuffer trimmedLog = new StringBuffer();
          // Keep only the text from offset to the end of the log
          while(offset != lines.length) {
            trimmedLog.append(lines[offset++] + '\n');
          }
          logging = trimmedLog.toString();
        }
        
        loggingMap.put(jobStatus.getId(), logging);

        Result result = ts.getResult();
        if (result != null) {
          item.setText(2, "" + result.getNrLinesRead());
          item.setText(3, "" + result.getNrLinesWritten());
          item.setText(4, "" + result.getNrLinesInput());
          item.setText(5, "" + result.getNrLinesOutput());
          item.setText(6, "" + result.getNrLinesUpdated());
          item.setText(7, "" + result.getNrLinesRejected());
          item.setText(8, "" + result.getNrErrors());
        }
      } catch (Exception e) {
        jobStatus.setErrorDescription("Unable to access transformation details : " + Const.CR + Const.getStackTracker(e));
      }
    }
  }

  protected void enableButtons() {
    TreeEntry treeEntry = getTreeEntry();
    boolean isTrans = treeEntry != null && treeEntry.isTransformation();
    boolean isJob = treeEntry != null && treeEntry.isJob();
    boolean hasId = treeEntry != null && !Const.isEmpty(treeEntry.id);
    boolean isRunning = treeEntry != null && treeEntry.isRunning();
    boolean isStopped = treeEntry != null && treeEntry.isStopped();
    boolean isFinished = treeEntry != null && treeEntry.isFinished();
    boolean isPaused = treeEntry != null && treeEntry.isPaused();
    boolean isWaiting = treeEntry != null && treeEntry.isWaiting();
    boolean isStep = treeEntry != null && treeEntry.length == 3;

    wStart.setEnabled((isTrans || isJob) && hasId && !isRunning && (isFinished || isStopped || isWaiting));
    wPause.setEnabled(isTrans && hasId && (isRunning || isPaused));
    wStop.setEnabled((isTrans || isJob) && hasId && (isRunning || isPaused));
    wRemove.setEnabled((isTrans || isJob) && hasId && (isFinished || isStopped || isWaiting));
    wSniff.setEnabled(isTrans && hasId && isRunning && isStep);
  }

  protected void refreshViewAndLog() {
    String[] selectionPath = null;
    if (wTree.getSelectionCount() == 1) {
      selectionPath = ConstUI.getTreeStrings(wTree.getSelection()[0]);
    }

    refreshView();

    if (selectionPath != null) // Select the same one again
    {
      TreeItem treeItem = TreeUtil.findTreeItem(wTree, selectionPath);
      if (treeItem != null) {
        wTree.setSelection(treeItem);
        wTree.showItem(treeItem);
        treeItemSelected(treeItem);
        treeItem.setExpanded(true);
      }
    }

    showLog();
  }

  public boolean canBeClosed() {
    // It's OK to close this at any time.
    // We just have to make sure we stop the timers etc.
    //
    spoon.tabfolder.setSelected(0);
    return true;
  }

  /**
   * Someone clicks on a line: show the log or error message associated with that in the text-box
   */
  public void showLog() {
    TreeEntry treeEntry = getTreeEntry();
    if (treeEntry == null)
      return;

    if (treeEntry.length <= 1)
      return;

    if (treeEntry.isTransformation()) {
      SlaveServerTransStatus transStatus = slaveServerStatus.findTransStatus(treeEntry.name, treeEntry.id);
      StringBuffer message = new StringBuffer();
      String errorDescription = transStatus.getErrorDescription();
      if (!Const.isEmpty(errorDescription)) {
        message.append(errorDescription).append(Const.CR).append(Const.CR);
      }

      String logging = loggingMap.get(transStatus.getId());
      if (!Const.isEmpty(logging)) {
        message.append(logging).append(Const.CR);
      }

      wText.setText(message.toString());
      wText.setSelection(wText.getText().length());
      wText.showSelection();
      // wText.setTopIndex(wText.getLineCount());
    }

    if (treeEntry.isJob()) {
      // We clicked on a job line item
      //
      SlaveServerJobStatus jobStatus = slaveServerStatus.findJobStatus(treeEntry.name);
      StringBuffer message = new StringBuffer();
      String errorDescription = jobStatus.getErrorDescription();
      if (!Const.isEmpty(errorDescription)) {
        message.append(errorDescription).append(Const.CR).append(Const.CR);
      }

      String logging = loggingMap.get(jobStatus.getId());
      if (!Const.isEmpty(logging)) {
        message.append(logging).append(Const.CR);
      }

      wText.setText(message.toString());
      wText.setSelection(wText.getText().length());
      wText.showSelection();
    }
  }

  protected void start() {
    TreeEntry treeEntry = getTreeEntry();
    if (treeEntry == null)
      return;

    if (treeEntry.isTransformation()) {
      SlaveServerTransStatus transStatus = slaveServerStatus.findTransStatus(treeEntry.name, treeEntry.id);
      if (transStatus != null) {
        if (!transStatus.isRunning()) {
          try {
            WebResult webResult = slaveServer.startTransformation(treeEntry.name, transStatus.getId());
            if (!webResult.getResult().equalsIgnoreCase(WebResult.STRING_OK)) {
              EnterTextDialog dialog = new EnterTextDialog(shell, BaseMessages.getString(PKG, "SpoonSlave.ErrorStartingTrans.Title"), BaseMessages.getString(
                  PKG, "SpoonSlave.ErrorStartingTrans.Message"), webResult.getMessage());
              dialog.setReadOnly();
              dialog.open();
            }
          } catch (Exception e) {
            new ErrorDialog(shell, BaseMessages.getString(PKG, "SpoonSlave.ErrorStartingTrans.Title"), BaseMessages.getString(PKG,
                "SpoonSlave.ErrorStartingTrans.Message"), e);
          }
        }
      }
    }

    if (treeEntry.isJob()) {
      SlaveServerJobStatus jobStatus = slaveServerStatus.findJobStatus(treeEntry.name);
      if (jobStatus != null) {
        if (!jobStatus.isRunning()) {
          try {
            WebResult webResult = slaveServer.startJob(treeEntry.name, treeEntry.id);
            if (!webResult.getResult().equalsIgnoreCase(WebResult.STRING_OK)) {
              EnterTextDialog dialog = new EnterTextDialog(shell, BaseMessages.getString(PKG, "SpoonSlave.ErrorStartingJob.Title"), BaseMessages.getString(PKG,
                  "SpoonSlave.ErrorStartingJob.Message"), webResult.getMessage());
              dialog.setReadOnly();
              dialog.open();
            }
          } catch (Exception e) {
            new ErrorDialog(shell, BaseMessages.getString(PKG, "SpoonSlave.ErrorStartingJob.Title"), BaseMessages.getString(PKG,
                "SpoonSlave.ErrorStartingJob.Message"), e);
          }
        }
      }
    }
  }

  private TreeEntry getTreeEntry() {
    TreeItem ti[] = wTree.getSelection();
    if (ti.length == 1) {
      TreeEntry treeEntry = new TreeEntry(ti[0]);
      if (treeEntry.length <= 1)
        return null;
      return treeEntry;
    } else {
      return null;
    }
  }

  protected void stop() {
    TreeEntry treeEntry = getTreeEntry();
    if (treeEntry == null)
      return;

    // Transformations
    //
    if (treeEntry.isTransformation()) {
      SlaveServerTransStatus transStatus = slaveServerStatus.findTransStatus(treeEntry.name, treeEntry.id);
      if (transStatus != null) {
        if (transStatus.isRunning() || transStatus.isPaused()) {
          try {
            WebResult webResult = slaveServer.stopTransformation(treeEntry.name, transStatus.getId());
            if (!webResult.getResult().equalsIgnoreCase(WebResult.STRING_OK)) {
              EnterTextDialog dialog = new EnterTextDialog(shell, BaseMessages.getString(PKG, "SpoonSlave.ErrorStoppingTrans.Title"), BaseMessages.getString(
                  PKG, "SpoonSlave.ErrorStoppingTrans.Message"), webResult.getMessage());
              dialog.setReadOnly();
              dialog.open();
            }
          } catch (Exception e) {
            new ErrorDialog(shell, BaseMessages.getString(PKG, "SpoonSlave.ErrorStoppingTrans.Title"), BaseMessages.getString(PKG,
                "SpoonSlave.ErrorStoppingTrans.Message"), e);
          }
        }
      }
    }

    // Jobs
    //
    if (treeEntry.isJob()) {
      SlaveServerJobStatus jobStatus = slaveServerStatus.findJobStatus(treeEntry.name);
      if (jobStatus != null) {
        if (jobStatus.isRunning()) {
          try {
            WebResult webResult = slaveServer.stopJob(treeEntry.name, treeEntry.id);
            if (!webResult.getResult().equalsIgnoreCase(WebResult.STRING_OK)) {
              EnterTextDialog dialog = new EnterTextDialog(shell, BaseMessages.getString(PKG, "SpoonSlave.ErrorStoppingJob.Title"), BaseMessages.getString(PKG,
                  "SpoonSlave.ErrorStoppingJob.Message"), webResult.getMessage());
              dialog.setReadOnly();
              dialog.open();
            }
          } catch (Exception e) {
            new ErrorDialog(shell, BaseMessages.getString(PKG, "SpoonSlave.ErrorStoppingJob.Title"), BaseMessages.getString(PKG,
                "SpoonSlave.ErrorStoppingJob.Message"), e);
          }
        }
      }
    }
  }

  protected void remove() {
    TreeEntry treeEntry = getTreeEntry();
    if (treeEntry == null)
      return;

    // Transformations
    //
    if (treeEntry.isTransformation()) {
      SlaveServerTransStatus transStatus = slaveServerStatus.findTransStatus(treeEntry.name, treeEntry.id);
      if (transStatus != null) {
        if (!transStatus.isRunning() && !transStatus.isPaused() && !transStatus.isStopped()) {
          try {
            WebResult webResult = slaveServer.removeTransformation(treeEntry.name, transStatus.getId());
            if (!webResult.getResult().equalsIgnoreCase(WebResult.STRING_OK)) {
              EnterTextDialog dialog = new EnterTextDialog(shell, BaseMessages.getString(PKG, "SpoonSlave.ErrorRemovingTrans.Title"), BaseMessages.getString(
                  PKG, "SpoonSlave.ErrorRemovingTrans.Message"), webResult.getMessage());
              dialog.setReadOnly();
              dialog.open();
            }
          } catch (Exception e) {
            new ErrorDialog(shell, BaseMessages.getString(PKG, "SpoonSlave.ErrorRemovingTrans.Title"), BaseMessages.getString(PKG,
                "SpoonSlave.ErrorRemovingTrans.Message"), e);
          }
        }
      }
    }

    // TODO: support for jobs
  }

  protected void pause() {
    TreeEntry treeEntry = getTreeEntry();
    if (treeEntry == null)
      return;

    // Transformations
    //
    if (treeEntry.isTransformation()) {
      try {
        WebResult webResult = slaveServer.pauseResumeTransformation(treeEntry.name, treeEntry.id);
        if (!webResult.getResult().equalsIgnoreCase(WebResult.STRING_OK)) {
          EnterTextDialog dialog = new EnterTextDialog(shell, BaseMessages.getString(PKG, "SpoonSlave.ErrorPausingOrResumingTrans.Title"),
              BaseMessages.getString(PKG, "SpoonSlave.ErrorPausingOrResumingTrans.Message"), webResult.getMessage());
          dialog.setReadOnly();
          dialog.open();
        }
      } catch (Exception e) {
        new ErrorDialog(shell, BaseMessages.getString(PKG, "SpoonSlave.ErrorPausingOrResumingTrans.Title"), BaseMessages.getString(PKG,
            "SpoonSlave.ErrorPausingOrResumingTrans.Message"), e);
      }
    }
  }

  private synchronized void refreshView() {
    if (wTree.isDisposed())
      return;
    if (refreshBusy)
      return;
    refreshBusy = true;

    log.logDetailed("Refresh");

    transParentItem.removeAll();
    jobParentItem.removeAll();

    // Determine the transformations on the slave servers
    try {
      slaveServerStatus = slaveServer.getStatus();
    } catch (Exception e) {
      slaveServerStatus = new SlaveServerStatus("Error contacting server");
      slaveServerStatus.setErrorDescription(Const.getStackTracker(e));
      wText.setText(slaveServerStatus.getErrorDescription());
    }

    List<SlaveServerTransStatus> transStatusList = slaveServerStatus.getTransStatusList();
    for (int i = 0; i < transStatusList.size(); i++) {
      SlaveServerTransStatus transStatus = transStatusList.get(i);
      TreeItem transItem = new TreeItem(transParentItem, SWT.NONE);
      transItem.setText(0, transStatus.getTransName());
      transItem.setText(9, transStatus.getStatusDescription());
      transItem.setText(13, Const.NVL(transStatus.getId(), ""));
      transItem.setImage(GUIResource.getInstance().getImageTransGraph());
      transItem.setData("transStatus", transStatus);
    }

    for (int i = 0; i < slaveServerStatus.getJobStatusList().size(); i++) {
      SlaveServerJobStatus jobStatus = slaveServerStatus.getJobStatusList().get(i);
      TreeItem jobItem = new TreeItem(jobParentItem, SWT.NONE);
      jobItem.setText(0, jobStatus.getJobName());
      jobItem.setText(9, jobStatus.getStatusDescription());
      jobItem.setText(13, Const.NVL(jobStatus.getId(), ""));
      jobItem.setImage(GUIResource.getInstance().getImageJobGraph());
      jobItem.setData("jobStatus", jobStatus);
    }

    TreeMemory.setExpandedFromMemory(wTree, STRING_SLAVE_LOG_TREE_NAME + slaveServer.toString());
    TreeUtil.setOptimalWidthOnColumns(wTree);
    refreshBusy = false;
  }

  public void showErrors() {
    String all = wText.getText();
    List<String> err = new ArrayList<String>();

    int i = 0;
    int startpos = 0;
    int crlen = Const.CR.length();

    while (i < all.length() - crlen) {
      if (all.substring(i, i + crlen).equalsIgnoreCase(Const.CR)) {
        String line = all.substring(startpos, i);
        String uLine = line.toUpperCase();
        if (uLine.indexOf(BaseMessages.getString(PKG, "TransLog.System.ERROR")) >= 0 || //$NON-NLS-1$
            uLine.indexOf(BaseMessages.getString(PKG, "TransLog.System.EXCEPTION")) >= 0 || //$NON-NLS-1$
            uLine.indexOf("ERROR") >= 0 || // i18n for compatibilty to non translated steps a.s.o. //$NON-NLS-1$ 
            uLine.indexOf("EXCEPTION") >= 0 // i18n for compatibilty to non translated steps a.s.o. //$NON-NLS-1$
        ) {
          err.add(line);
        }
        // New start of line
        startpos = i + crlen;
      }

      i++;
    }
    String line = all.substring(startpos);
    String uLine = line.toUpperCase();
    if (uLine.indexOf(BaseMessages.getString(PKG, "TransLog.System.ERROR2")) >= 0 || //$NON-NLS-1$
        uLine.indexOf(BaseMessages.getString(PKG, "TransLog.System.EXCEPTION2")) >= 0 || //$NON-NLS-1$
        uLine.indexOf("ERROR") >= 0 || // i18n for compatibilty to non translated steps a.s.o. //$NON-NLS-1$ 
        uLine.indexOf("EXCEPTION") >= 0 // i18n for compatibilty to non translated steps a.s.o. //$NON-NLS-1$
    ) {
      err.add(line);
    }

    if (err.size() > 0) {
      String err_lines[] = new String[err.size()];
      for (i = 0; i < err_lines.length; i++)
        err_lines[i] = err.get(i);

      EnterSelectionDialog esd = new EnterSelectionDialog(shell, err_lines,
          BaseMessages.getString(PKG, "TransLog.Dialog.ErrorLines.Title"), BaseMessages.getString(PKG, "TransLog.Dialog.ErrorLines.Message")); //$NON-NLS-1$ //$NON-NLS-2$
      line = esd.open();
      /*
       * TODO: we have multiple transformation we can go to: which one should we pick? if (line != null) { for (i = 0; i < spoon.getTransMeta().nrSteps(); i++)
       * { StepMeta stepMeta = spoon.getTransMeta().getStep(i); if (line.indexOf(stepMeta.getName()) >= 0) { spoon.editStep(stepMeta.getName()); } } //
       * System.out.println("Error line selected: "+line); }
       */
    }
  }

  public String toString() {
    return Spoon.APP_NAME;
  }

  public Object getManagedObject() {
    return slaveServer;
  }

  public boolean hasContentChanged() {
    return false;
  }

  public boolean applyChanges() {
    return true;
  }

  public int showChangedWarning() {
    return SWT.YES;
  }

  public EngineMetaInterface getMeta() {
    return new EngineMetaInterface() {

      public void setModifiedUser(String user) {
      }

      public void setModifiedDate(Date date) {
      }

      public void setInternalKettleVariables() {
      }

      public void setObjectId(ObjectId id) {
      }

      public void setFilename(String filename) {
      }

      public void setCreatedUser(String createduser) {
      }

      public void setCreatedDate(Date date) {
      }

      public void saveSharedObjects() {
      }

      public void nameFromFilename() {
      }

      public String getXML() {
        return null;
      }

      public boolean canSave() {
        return true;
      }

      public String getName() {
        return slaveServer.getName();
      }

      public String getModifiedUser() {
        return null;
      }

      public Date getModifiedDate() {
        return null;
      }

      public String[] getFilterNames() {
        return null;
      }

      public String[] getFilterExtensions() {
        return null;
      }

      public String getFilename() {
        return null;
      }

      public String getFileType() {
        return null;
      }

      public RepositoryDirectoryInterface getRepositoryDirectory() {
        return null;
      }

      public String getDefaultExtension() {
        return null;
      }

      public String getCreatedUser() {
        return null;
      }

      public Date getCreatedDate() {
        return null;
      }

      public void clearChanged() {
      }

      public ObjectId getObjectId() {
        return null;
      }

      public RepositoryObjectType getRepositoryElementType() {
        return null;
      }

      public void setName(String name) {
      }

      public void setRepositoryDirectory(RepositoryDirectoryInterface repositoryDirectory) {
      }

      public String getDescription() {
        return null;
      }

      public void setDescription(String description) {
      }

      public ObjectRevision getObjectRevision() {
        return null;
      }

      public void setObjectRevision(ObjectRevision objectRevision) {
      }
    };
  }

  public void setControlStates() {
    // TODO Auto-generated method stub

  }

  public boolean canHandleSave() {
    return false;
  }

  protected void sniff() {
    TreeItem ti[] = wTree.getSelection();
    if (ti.length == 1) {
      TreeItem treeItem = ti[0];
      String[] path = ConstUI.getTreeStrings(treeItem);

      // Make sure we're positioned on a step
      //
      if (path.length <= 2) {
        return;
      }

      String name = path[1];
      String step = path[2];
      String copy = treeItem.getText(1);

      EnterNumberDialog numberDialog = new EnterNumberDialog(shell, PropsUI.getInstance().getDefaultPreviewSize(), BaseMessages.getString(PKG,
          "SpoonSlave.SniffSizeQuestion.Title"), BaseMessages.getString(PKG, "SpoonSlave.SniffSizeQuestion.Message"));
      int lines = numberDialog.open();
      if (lines <= 0) {
        return;
      }

      EnterSelectionDialog selectionDialog = new EnterSelectionDialog(shell, new String[] { SniffStepServlet.TYPE_INPUT, SniffStepServlet.TYPE_OUTPUT, },
          BaseMessages.getString(PKG, "SpoonSlave.SniffTypeQuestion.Title"), BaseMessages.getString(PKG, "SpoonSlave.SniffTypeQuestion.Message"));
      String type = selectionDialog.open(1);
      if (type == null) {
        return;
      }

      try {
        String xml = slaveServer.sniffStep(name, step, copy, lines, type);

        Document doc = XMLHandler.loadXMLString(xml);
        Node node = XMLHandler.getSubNode(doc, SniffStepServlet.XML_TAG);
        Node metaNode = XMLHandler.getSubNode(node, RowMeta.XML_META_TAG);
        RowMetaInterface rowMeta = new RowMeta(metaNode);

        int nrRows = Const.toInt(XMLHandler.getTagValue(node, "nr_rows"), 0);
        List<Object[]> rowBuffer = new ArrayList<Object[]>();
        for (int i = 0; i < nrRows; i++) {
          Node dataNode = XMLHandler.getSubNodeByNr(node, RowMeta.XML_DATA_TAG, i);
          Object[] row = rowMeta.getRow(dataNode);
          rowBuffer.add(row);
        }

        PreviewRowsDialog prd = new PreviewRowsDialog(shell, new Variables(), SWT.NONE, step, rowMeta, rowBuffer);
        prd.open();
      } catch (Exception e) {
        new ErrorDialog(shell, BaseMessages.getString(PKG, "SpoonSlave.ErrorSniffingStep.Title"), BaseMessages.getString(PKG,
            "SpoonSlave.ErrorSniffingStep.Message"), e);
      }
    }
  }

  public ChangedWarningInterface getChangedWarning() {
    return null;
  }
}
