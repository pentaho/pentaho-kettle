/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2023 by Hitachi Vantara : http://www.pentaho.com
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
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.google.common.annotations.VisibleForTesting;
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
import org.eclipse.swt.widgets.Widget;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
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
 * @see org.pentaho.di.ui.spoon.Spoon
 * @author Matt
 * @since 12 nov 2006
 */
public class SpoonSlave extends Composite implements TabItemInterface {
  private static Class<?> PKG = Spoon.class; // for i18n purposes, needed by Translator2!!

  public static final long UPDATE_TIME_VIEW = Long.parseLong( Const.getEnvironmentVariable( "SPOON_CARTE_VIEW_UPDATE_TIME", "30000" ) );

  public static final String STRING_SLAVE_LOG_TREE_NAME = "SLAVE_LOG : ";

  private String currentLogText;

  private Shell shell;
  private Display display;
  private SlaveServer slaveServer;

  private Spoon spoon;

  private Tree wTree;
  private Text wText;

  private Button wStart;
  private Button wPause;
  private Button wStop;
  private Button wRemove;
  private Button wSniff;

  private boolean refreshBusy;
  private SlaveServerStatus slaveServerStatus;
  private Timer timer;
  private TimerTask timerTask;

  private TreeItem transParentItem;

  private TreeItem jobParentItem;

  private LogChannelInterface log;

  private String lastLoggedId;
  private boolean lastLoggedIsFinishedOrStopped;

  private class TreeEntry {
    String itemType; // Transformation or Job
    String name;
    String status;
    String id;
    String[] path;
    int length;

    public TreeEntry( TreeItem treeItem ) {
      TreeItem treeIt = treeItem;
      path = ConstUI.getTreeStrings( treeIt );
      this.length = path.length;
      if ( path.length > 0 ) {
        itemType = path[0];
      }
      if ( path.length > 1 ) {
        name = path[1];
      }
      if ( path.length == 3 ) {
        treeIt = treeIt.getParentItem();
      }
      status = treeIt.getText( 9 );
      id = treeIt.getText( 13 );
    }

    boolean isTransformation() {
      return itemType.equals( transParentItem.getText() );
    }

    boolean isJob() {
      return itemType.equals( jobParentItem.getText() );
    }

    boolean isRunning() {
      return Trans.STRING_RUNNING.equals( status );
    }

    boolean isStopped() {
      return Trans.STRING_STOPPED.equals( status );
    }

    boolean isFinished() {
      if ( Trans.STRING_FINISHED_WITH_ERRORS.equals( status ) ) {
        return true;
      }
      return Trans.STRING_FINISHED.equals( status );
    }

    boolean isPaused() {
      return Trans.STRING_PAUSED.equals( status );
    }

    boolean isWaiting() {
      return Trans.STRING_WAITING.equals( status );
    }

    @Override
    public boolean equals( Object o ) {
      if ( this == o ) {
        return true;
      }
      if ( !( o instanceof TreeEntry ) ) {
        return false;
      }

      TreeEntry treeEntry = (TreeEntry) o;

      if ( id != null ? !id.equals( treeEntry.id ) : treeEntry.id != null ) {
        return false;
      }
      if ( itemType != null ? !itemType.equals( treeEntry.itemType ) : treeEntry.itemType != null ) {
        return false;
      }
      if ( name != null ? !name.equals( treeEntry.name ) : treeEntry.name != null ) {
        return false;
      }

      return true;
    }

    @Override
    public int hashCode() {
      int result = itemType != null ? itemType.hashCode() : 0;
      result = 31 * result + ( name != null ? name.hashCode() : 0 );
      result = 31 * result + ( id != null ? id.hashCode() : 0 );
      result = 31 * result + ( path != null ? Arrays.hashCode( path ) : 0 );
      result = 31 * result + length;
      return result;
    }

    public TreeItem getTreeItem( Tree tree ) {
      TreeItem[] items = tree.getItems();
      for ( TreeItem item : items ) {
        TreeItem treeItem = findTreeItem( item, 0 );
        if ( treeItem != null ) {
          return treeItem;
        }
      }
      return null;
    }

    private TreeItem findTreeItem( TreeItem treeItem, int level ) {
      if ( treeItem.getText().equals( path[ level ] ) ) {
        if ( level == 1 ) {
          if ( !this.equals( getTreeEntry( treeItem ) ) ) {
            return null;
          }
        }
        if ( level == path.length - 1 ) {
          return treeItem;
        }

        TreeItem[] items = treeItem.getItems();
        for ( TreeItem item : items ) {
          TreeItem found = findTreeItem( item, level + 1 );
          if ( found != null ) {
            return found;
          }
        }
      }
      return null;
    }
  }

  public SpoonSlave( Composite parent, int style, final Spoon spoon, SlaveServer slaveServer ) {
    super( parent, style );

    this.shell = parent.getShell();
    this.display = shell.getDisplay();
    this.spoon = spoon;
    this.slaveServer = slaveServer;
    this.log = spoon.getLog();

    FormLayout formLayout = new FormLayout();
    formLayout.marginWidth = Const.FORM_MARGIN;
    formLayout.marginHeight = Const.FORM_MARGIN;

    setLayout( formLayout );

    setVisible( true );
    spoon.props.setLook( this );

    SashForm sash = new SashForm( this, SWT.VERTICAL );

    sash.setLayout( new FillLayout() );

    //CHECKSTYLE:LineLength:OFF
    ColumnInfo[] colinf = new ColumnInfo[] {
      new ColumnInfo( BaseMessages.getString( PKG, "SpoonSlave.Column.Stepname" ), ColumnInfo.COLUMN_TYPE_TEXT, false, true ),
      new ColumnInfo( BaseMessages.getString( PKG, "SpoonSlave.Column.Copynr" ), ColumnInfo.COLUMN_TYPE_TEXT, false, true ),
      new ColumnInfo( BaseMessages.getString( PKG, "SpoonSlave.Column.Read" ), ColumnInfo.COLUMN_TYPE_TEXT, false, true ),
      new ColumnInfo( BaseMessages.getString( PKG, "SpoonSlave.Column.Written" ), ColumnInfo.COLUMN_TYPE_TEXT, false, true ),
      new ColumnInfo( BaseMessages.getString( PKG, "SpoonSlave.Column.Input" ), ColumnInfo.COLUMN_TYPE_TEXT, false, true ),
      new ColumnInfo( BaseMessages.getString( PKG, "SpoonSlave.Column.Output" ), ColumnInfo.COLUMN_TYPE_TEXT, false, true ),
      new ColumnInfo( BaseMessages.getString( PKG, "SpoonSlave.Column.Updated" ), ColumnInfo.COLUMN_TYPE_TEXT, false, true ),
      new ColumnInfo( BaseMessages.getString( PKG, "SpoonSlave.Column.Rejected" ), ColumnInfo.COLUMN_TYPE_TEXT, false, true ),
      new ColumnInfo( BaseMessages.getString( PKG, "SpoonSlave.Column.Errors" ), ColumnInfo.COLUMN_TYPE_TEXT, false, true ),
      new ColumnInfo( BaseMessages.getString( PKG, "SpoonSlave.Column.Active" ), ColumnInfo.COLUMN_TYPE_TEXT, false, true ),
      new ColumnInfo( BaseMessages.getString( PKG, "SpoonSlave.Column.Time" ), ColumnInfo.COLUMN_TYPE_TEXT, false, true ),
      new ColumnInfo( BaseMessages.getString( PKG, "SpoonSlave.Column.Speed" ), ColumnInfo.COLUMN_TYPE_TEXT, false, true ),
      new ColumnInfo( BaseMessages.getString( PKG, "SpoonSlave.Column.PriorityBufferSizes" ), ColumnInfo.COLUMN_TYPE_TEXT, false, true ),
      new ColumnInfo( BaseMessages.getString( PKG, "SpoonSlave.Column.CarteObjectId" ), ColumnInfo.COLUMN_TYPE_TEXT, false, true ),
      new ColumnInfo( BaseMessages.getString( PKG, "SpoonSlave.Column.LogDate" ), ColumnInfo.COLUMN_TYPE_TEXT, false, true ), };

    colinf[1].setAllignement( SWT.RIGHT );
    colinf[2].setAllignement( SWT.RIGHT );
    colinf[3].setAllignement( SWT.RIGHT );
    colinf[4].setAllignement( SWT.RIGHT );
    colinf[5].setAllignement( SWT.RIGHT );
    colinf[6].setAllignement( SWT.RIGHT );
    colinf[7].setAllignement( SWT.RIGHT );
    colinf[8].setAllignement( SWT.RIGHT );
    colinf[9].setAllignement( SWT.RIGHT );
    colinf[10].setAllignement( SWT.RIGHT );
    colinf[11].setAllignement( SWT.RIGHT );
    colinf[12].setAllignement( SWT.RIGHT );
    colinf[13].setAllignement( SWT.RIGHT );

    wTree = new Tree( sash, SWT.SINGLE | SWT.V_SCROLL | SWT.H_SCROLL );
    wTree.setHeaderVisible( true );
    TreeMemory.addTreeListener( wTree, STRING_SLAVE_LOG_TREE_NAME + slaveServer.toString() );
    Rectangle bounds = spoon.tabfolder.getSwtTabset().getBounds();
    for ( ColumnInfo columnInfo : colinf ) {
      TreeColumn treeColumn = new TreeColumn( wTree, columnInfo.getAllignement() );
      treeColumn.setText( columnInfo.getName() );
      treeColumn.setWidth( bounds.width / colinf.length );
    }

    transParentItem = new TreeItem( wTree, SWT.NONE );
    transParentItem.setText( Spoon.STRING_TRANSFORMATIONS );
    transParentItem.setImage( GUIResource.getInstance().getImageTransGraph() );

    jobParentItem = new TreeItem( wTree, SWT.NONE );
    jobParentItem.setText( Spoon.STRING_JOBS );
    jobParentItem.setImage( GUIResource.getInstance().getImageJobGraph() );

    wTree.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent event ) {
        enableButtons();
        Widget item = event.item;
        if ( item != null ) {
          treeItemSelected( (TreeItem) item );
          ( (TreeItem) item ).setExpanded( true );
        }
        showLog();
      }
    } );

    wTree.addTreeListener( new TreeListener() {

      public void treeExpanded( TreeEvent event ) {
        treeItemSelected( (TreeItem) event.item );
        showLog();
      }

      public void treeCollapsed( TreeEvent arg0 ) {
      }
    } );

    wText = new Text( sash, SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL | SWT.READ_ONLY | SWT.BORDER );
    spoon.props.setLook( wText );
    wText.setVisible( true );

    Button wRefresh = new Button( this, SWT.PUSH );
    wRefresh.setText( BaseMessages.getString( PKG, "SpoonSlave.Button.Refresh" ) );
    wRefresh.setEnabled( true );
    wRefresh.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e ) {
        refreshViewAndLog();
      }
    } );

    Button wError = new Button( this, SWT.PUSH );
    wError.setText( BaseMessages.getString( PKG, "SpoonSlave.Button.ShowErrorLines" ) );
    wError.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e ) {
        showErrors();
      }
    } );

    wSniff = new Button( this, SWT.PUSH );
    wSniff.setText( BaseMessages.getString( PKG, "SpoonSlave.Button.Sniff" ) );
    wSniff.setEnabled( false );
    wSniff.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e ) {
        sniff();
      }
    } );

    wStart = new Button( this, SWT.PUSH );
    wStart.setText( BaseMessages.getString( PKG, "SpoonSlave.Button.Start" ) );
    wStart.setEnabled( false );
    wStart.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e ) {
        start();
      }
    } );

    wPause = new Button( this, SWT.PUSH );
    wPause.setText( BaseMessages.getString( PKG, "SpoonSlave.Button.Pause" ) );
    wPause.setEnabled( false );
    wPause.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e ) {
        pause();
      }
    } );

    wStop = new Button( this, SWT.PUSH );
    wStop.setText( BaseMessages.getString( PKG, "SpoonSlave.Button.Stop" ) );
    wStop.setEnabled( false );
    wStop.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e ) {
        stop();
      }
    } );

    wRemove = new Button( this, SWT.PUSH );
    wRemove.setText( BaseMessages.getString( PKG, "SpoonSlave.Button.Remove" ) );
    wRemove.setEnabled( false );
    wRemove.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e ) {
        remove();
      }
    } );

    BaseStepDialog.positionBottomButtons( this,
        new Button[] { wRefresh, wSniff, wStart, wPause, wStop, wRemove, wError }, Const.MARGIN, null );

    // Put tree on top
    FormData fdTree = new FormData();
    fdTree.left = new FormAttachment( 0, 0 );
    fdTree.top = new FormAttachment( 0, 0 );
    fdTree.right = new FormAttachment( 100, 0 );
    fdTree.bottom = new FormAttachment( 100, 0 );
    wTree.setLayoutData( fdTree );

    // Put text in the middle
    FormData fdText = new FormData();
    fdText.left = new FormAttachment( 0, 0 );
    fdText.top = new FormAttachment( 0, 0 );
    fdText.right = new FormAttachment( 100, 0 );
    fdText.bottom = new FormAttachment( 100, 0 );
    wText.setLayoutData( fdText );

    FormData fdSash = new FormData();
    fdSash.left = new FormAttachment( 0, 0 ); // First one in the left top corner
    fdSash.top = new FormAttachment( 0, 0 );
    fdSash.right = new FormAttachment( 100, 0 );
    fdSash.bottom = new FormAttachment( wRefresh, -5 );
    sash.setLayoutData( fdSash );

    pack();

    // Schedule view and log refresh every UPDATE_TIME_VIEW milliseconds
    engageViewAndLogUpdateTimer();

    addDisposeListener( new DisposeListener() {
      public void widgetDisposed( DisposeEvent e ) {
        timer.cancel();
      }
    } );
  }

  public void treeItemSelected( TreeItem item ) {
    if ( item == null ) {
      // there is nothing to do
      return;
    }
    // load node upon expansion
    if ( item.getData( "transStatus" ) != null ) {
      SlaveServerTransStatus transStatus = (SlaveServerTransStatus) item.getData( "transStatus" );
      try {
        if ( log.isDetailed() ) {
          log.logDetailed( "Getting transformation status for [{0}] on server [{1}]", transStatus.getTransName(),
              SpoonSlave.this.slaveServer );
        }

        SlaveServerTransStatus ts =
          SpoonSlave.this.slaveServer.getTransStatus(
            transStatus.getTransName(), transStatus.getId(), 0 );
        if ( log.isDetailed() ) {
          log.logDetailed( "Finished receiving transformation status for [{0}] from server [{1}]", transStatus
            .getTransName(), SpoonSlave.this.slaveServer );
        }
        List<StepStatus> stepStatusList = ts.getStepStatusList();
        transStatus.setStepStatusList( stepStatusList );

        String logging = ts.getLoggingString();

        String[] lines = logging.split( "\r\n|\r|\n" );
        if ( lines.length > PropsUI.getInstance().getMaxNrLinesInLog() ) {
          // Trim to view the last x lines
          int offset = lines.length - PropsUI.getInstance().getMaxNrLinesInLog();
          StringBuilder trimmedLog = new StringBuilder();
          // Keep only the text from offset to the end of the log
          while ( offset != lines.length ) {
            trimmedLog.append( lines[offset++] ).append( '\n' );
          }
          logging = trimmedLog.toString();
        }

        currentLogText = logging;

        item.removeAll();
        for ( StepStatus stepStatus : stepStatusList ) {
          TreeItem stepItem = new TreeItem( item, SWT.NONE );
          stepItem.setText( stepStatus.getSpoonSlaveLogFields() );
        }
      } catch ( Exception e ) {
        transStatus.setErrorDescription( "Unable to access transformation details : "
          + Const.CR + Const.getStackTracker( e ) );
      }
    } else if ( item.getData( "jobStatus" ) != null ) {
      SlaveServerJobStatus jobStatus = (SlaveServerJobStatus) item.getData( "jobStatus" );
      if ( !jobStatus.isFinished() || !jobStatus.getId().equals( lastLoggedId ) || !lastLoggedIsFinishedOrStopped ) {
        try {
          if ( log.isDetailed() ) {
            log.logDetailed( "Getting job status for [{0}] on server [{1}]", jobStatus.getJobName(), slaveServer );
          }

          SlaveServerJobStatus ts =
            slaveServer.getJobStatusTail( jobStatus.getJobName(), jobStatus.getId(),
              PropsUI.getInstance().getMaxNrLinesInLog() );

          if ( log.isDetailed() ) {
            log.logDetailed(
              "Finished receiving job status for [{0}] from server [{1}]", jobStatus.getJobName(), slaveServer );
          }

          String logging = ts.getLoggingString();

          String[] lines = logging.split( "\r\n|\r|\n" );
          if ( lines.length > PropsUI.getInstance().getMaxNrLinesInLog() ) {
            // Trim to view the last x lines
            int offset = lines.length - PropsUI.getInstance().getMaxNrLinesInLog();
            StringBuilder trimmedLog = new StringBuilder();
            // Keep only the text from offset to the end of the log
            while ( offset != lines.length ) {
              trimmedLog.append( lines[ offset++ ] ).append( '\n' );
            }
            logging = trimmedLog.toString();
          }

          currentLogText = logging;
          lastLoggedId = jobStatus.getId();
          lastLoggedIsFinishedOrStopped = jobStatus.isFinished() || jobStatus.isStopped();

          Result result = ts.getResult();
          if ( result != null ) {
            item.setText( 2, "" + result.getNrLinesRead() );
            item.setText( 3, "" + result.getNrLinesWritten() );
            item.setText( 4, "" + result.getNrLinesInput() );
            item.setText( 5, "" + result.getNrLinesOutput() );
            item.setText( 6, "" + result.getNrLinesUpdated() );
            item.setText( 7, "" + result.getNrLinesRejected() );
            item.setText( 8, "" + result.getNrErrors() );
          }
        } catch ( Exception e ) {
          jobStatus.setErrorDescription( "Unable to access transformation details : "
            + Const.CR + Const.getStackTracker( e ) );
        }
      }
      }
  }

  protected void enableButtons() {
    TreeEntry treeEntry = getTreeEntry();
    boolean isTrans = treeEntry != null && treeEntry.isTransformation();
    boolean isJob = treeEntry != null && treeEntry.isJob();
    boolean hasId = treeEntry != null && !Utils.isEmpty( treeEntry.id );
    boolean isRunning = treeEntry != null && treeEntry.isRunning();
    boolean isStopped = treeEntry != null && treeEntry.isStopped();
    boolean isFinished = treeEntry != null && treeEntry.isFinished();
    boolean isPaused = treeEntry != null && treeEntry.isPaused();
    boolean isWaiting = treeEntry != null && treeEntry.isWaiting();
    boolean isStep = treeEntry != null && treeEntry.length == 3;

    wStart.setEnabled( ( isTrans || isJob ) && hasId && !isRunning && ( isFinished || isStopped || isWaiting ) );
    wPause.setEnabled( isTrans && hasId && ( isRunning || isPaused ) );
    wStop.setEnabled( ( isTrans || isJob ) && hasId && ( isRunning || isPaused ) );
    wRemove.setEnabled( ( isTrans || isJob ) && hasId && ( isFinished || isStopped || isWaiting ) );
    wSniff.setEnabled( isTrans && hasId && isRunning && isStep );
  }

  protected void refreshViewAndLog() {
    String[] selectionPath = null;
    TreeItem selectedItem;
    TreeEntry treeEntry = null;
    if ( wTree.getSelectionCount() == 1 ) {
      selectedItem = wTree.getSelection()[ 0 ];
      treeEntry = new TreeEntry( selectedItem );
      selectionPath = ConstUI.getTreeStrings( selectedItem );
    }

    refreshView();

    if ( treeEntry != null ) { // Select the same one again

      TreeItem treeItem = treeEntry.getTreeItem( wTree );
      if ( treeItem == null ) {
        treeItem = TreeUtil.findTreeItem( wTree, selectionPath );
      }
      if ( treeItem != null ) {
        wTree.setSelection( treeItem );
        if ( treeEntry.length < 3 ) {
          wTree.showItem( treeItem );
          treeItemSelected( treeItem );
          treeItem.setExpanded( true );
        }
      }
    }

    showLog();
  }

  public boolean canBeClosed() {
    // It's OK to close this at any time.
    // We just have to make sure we stop the timers etc.
    //
    spoon.tabfolder.setSelected( 0 );
    return true;
  }

  /**
   * Someone clicks on a line: show the log or error message associated with that in the text-box
   */
  public void showLog() {
    TreeEntry treeEntry = getTreeEntry();
    if ( treeEntry == null ) {
      return;
    }

    if ( treeEntry.length <= 1 ) {
      return;
    }

    if ( treeEntry.isTransformation() ) {
      // Transformation
      SlaveServerTransStatus transStatus = slaveServerStatus.findTransStatus( treeEntry.name, treeEntry.id );
      StringBuilder message = new StringBuilder();
      String errorDescription = transStatus.getErrorDescription();
      if ( !Utils.isEmpty( errorDescription ) ) {
        message.append( errorDescription ).append( Const.CR ).append( Const.CR );
      }

      if ( !Utils.isEmpty( currentLogText ) ) {
        message.append( currentLogText ).append( Const.CR );
      }

      wText.setText( message.toString() );
      wText.setSelection( wText.getText().length() );
      wText.showSelection();
    } else if ( treeEntry.isJob() ) {
      // Job
      SlaveServerJobStatus jobStatus = slaveServerStatus.findJobStatus( treeEntry.name, treeEntry.id );
      StringBuilder message = new StringBuilder();
      String errorDescription = jobStatus.getErrorDescription();
      if ( !Utils.isEmpty( errorDescription ) ) {
        message.append( errorDescription ).append( Const.CR ).append( Const.CR );
      }

      if ( !Utils.isEmpty( currentLogText ) ) {
        message.append( currentLogText ).append( Const.CR );
      }

      wText.setText( message.toString() );
      wText.setSelection( wText.getText().length() );
      wText.showSelection();
    } else {
      currentLogText = null;
    }
  }

  protected void start() {
    TreeEntry treeEntry = getTreeEntry();
    if ( treeEntry == null ) {
      return;
    }

    if ( treeEntry.isTransformation() ) {
      // Transformation
      SlaveServerTransStatus transStatus = slaveServerStatus.findTransStatus( treeEntry.name, treeEntry.id );
      if ( transStatus != null ) {
        if ( !transStatus.isRunning() ) {
          try {
            WebResult webResult = slaveServer.startTransformation( treeEntry.name, transStatus.getId() );
            if ( !WebResult.STRING_OK.equalsIgnoreCase( webResult.getResult() ) ) {
              EnterTextDialog dialog =
                new EnterTextDialog(
                  shell, BaseMessages.getString( PKG, "SpoonSlave.ErrorStartingTrans.Title" ), BaseMessages
                    .getString( PKG, "SpoonSlave.ErrorStartingTrans.Message" ), webResult.getMessage() );
              dialog.setReadOnly();
              dialog.open();
            }
          } catch ( Exception e ) {
            new ErrorDialog(
              shell, BaseMessages.getString( PKG, "SpoonSlave.ErrorStartingTrans.Title" ), BaseMessages
                .getString( PKG, "SpoonSlave.ErrorStartingTrans.Message" ), e );
          }
        }
      }
    } else if ( treeEntry.isJob() ) {
      // Job
      SlaveServerJobStatus jobStatus = slaveServerStatus.findJobStatus( treeEntry.name, treeEntry.id );
      if ( jobStatus != null ) {
        if ( !jobStatus.isRunning() ) {
          try {
            WebResult webResult = slaveServer.startJob( treeEntry.name, jobStatus.getId() );
            if ( !WebResult.STRING_OK.equalsIgnoreCase( webResult.getResult() ) ) {
              EnterTextDialog dialog =
                new EnterTextDialog(
                  shell, BaseMessages.getString( PKG, "SpoonSlave.ErrorStartingJob.Title" ), BaseMessages
                    .getString( PKG, "SpoonSlave.ErrorStartingJob.Message" ), webResult.getMessage() );
              dialog.setReadOnly();
              dialog.open();
            }
          } catch ( Exception e ) {
            new ErrorDialog(
              shell, BaseMessages.getString( PKG, "SpoonSlave.ErrorStartingJob.Title" ), BaseMessages.getString(
                PKG, "SpoonSlave.ErrorStartingJob.Message" ), e );
          }
        }
      }
    }
  }

  private TreeEntry getTreeEntry() {
    TreeItem[] ti = wTree.getSelection();
    if ( ti.length == 1 ) {
      return getTreeEntry( ti[ 0 ] );
    } else {
      return null;
    }
  }

  private TreeEntry getTreeEntry( TreeItem ti ) {
    TreeEntry treeEntry = new TreeEntry( ti );
    if ( treeEntry.length <= 1 ) {
      return null;
    }
    return treeEntry;
  }

  protected void stop() {
    TreeEntry treeEntry = getTreeEntry();
    if ( treeEntry == null ) {
      return;
    }

    if ( treeEntry.isTransformation() ) {
      // Transformation
      SlaveServerTransStatus transStatus = slaveServerStatus.findTransStatus( treeEntry.name, treeEntry.id );
      if ( transStatus != null ) {
        if ( transStatus.isRunning() || transStatus.isPaused() ) {
          try {
            WebResult webResult = slaveServer.stopTransformation( treeEntry.name, transStatus.getId() );
            if ( !WebResult.STRING_OK.equalsIgnoreCase( webResult.getResult() ) ) {
              EnterTextDialog dialog =
                new EnterTextDialog(
                  shell, BaseMessages.getString( PKG, "SpoonSlave.ErrorStoppingTrans.Title" ), BaseMessages
                    .getString( PKG, "SpoonSlave.ErrorStoppingTrans.Message" ), webResult.getMessage() );
              dialog.setReadOnly();
              dialog.open();
            }
          } catch ( Exception e ) {
            new ErrorDialog(
              shell, BaseMessages.getString( PKG, "SpoonSlave.ErrorStoppingTrans.Title" ), BaseMessages
                .getString( PKG, "SpoonSlave.ErrorStoppingTrans.Message" ), e );
          }
        }
      }
    } else if ( treeEntry.isJob() ) {
      // Job
      SlaveServerJobStatus jobStatus = slaveServerStatus.findJobStatus( treeEntry.name, treeEntry.id );
      if ( jobStatus != null ) {
        if ( jobStatus.isRunning() ) {
          try {
            WebResult webResult = slaveServer.stopJob( treeEntry.name, jobStatus.getId() );
            if ( !WebResult.STRING_OK.equalsIgnoreCase( webResult.getResult() ) ) {
              EnterTextDialog dialog =
                new EnterTextDialog(
                  shell, BaseMessages.getString( PKG, "SpoonSlave.ErrorStoppingJob.Title" ), BaseMessages
                    .getString( PKG, "SpoonSlave.ErrorStoppingJob.Message" ), webResult.getMessage() );
              dialog.setReadOnly();
              dialog.open();
            }
          } catch ( Exception e ) {
            new ErrorDialog(
              shell, BaseMessages.getString( PKG, "SpoonSlave.ErrorStoppingJob.Title" ), BaseMessages.getString(
                PKG, "SpoonSlave.ErrorStoppingJob.Message" ), e );
          }
        }
      }
    }
  }

  protected void remove() {
    TreeEntry treeEntry = getTreeEntry();
    if ( treeEntry == null ) {
      return;
    }

    if ( treeEntry.isTransformation() ) {
      // Transformation
      SlaveServerTransStatus transStatus = slaveServerStatus.findTransStatus( treeEntry.name, treeEntry.id );
      if ( transStatus != null ) {
        if ( !transStatus.isRunning() && !transStatus.isPaused() && !transStatus.isStopped() ) {
          try {
            WebResult webResult = slaveServer.removeTransformation( treeEntry.name, transStatus.getId() );
            if ( WebResult.STRING_OK.equalsIgnoreCase( webResult.getResult() ) ) {
              // Force refresh in order to give faster visual feedback and reengage the timer
              wTree.deselectAll();
              engageViewAndLogUpdateTimer();
            } else {
              EnterTextDialog dialog =
                new EnterTextDialog(
                  shell, BaseMessages.getString( PKG, "SpoonSlave.ErrorRemovingTrans.Title" ), BaseMessages
                    .getString( PKG, "SpoonSlave.ErrorRemovingTrans.Message" ), webResult.getMessage() );
              dialog.setReadOnly();
              dialog.open();
            }
          } catch ( Exception e ) {
            new ErrorDialog(
              shell, BaseMessages.getString( PKG, "SpoonSlave.ErrorRemovingTrans.Title" ), BaseMessages
                .getString( PKG, "SpoonSlave.ErrorRemovingTrans.Message" ), e );
          }
        }
      }
    } else if ( treeEntry.isJob() ) {
      // Job
      SlaveServerJobStatus jobStatus = slaveServerStatus.findJobStatus( treeEntry.name, treeEntry.id );
      if ( jobStatus != null ) {
        if ( !jobStatus.isRunning() ) {
          try {
            WebResult webResult = slaveServer.removeJob( treeEntry.name, jobStatus.getId() );

            if ( WebResult.STRING_OK.equalsIgnoreCase( webResult.getResult() ) ) {
              // Force refresh in order to give faster visual feedback and reengage the timer
              wTree.deselectAll();
              engageViewAndLogUpdateTimer();
            } else {
              EnterTextDialog dialog =
                new EnterTextDialog(
                  shell, BaseMessages.getString( PKG, "SpoonSlave.ErrorRemovingJob.Title" ), BaseMessages
                    .getString( PKG, "SpoonSlave.ErrorRemovingJob.Message" ), webResult.getMessage() );
              dialog.setReadOnly();
              dialog.open();
            }
          } catch ( Exception e ) {
            new ErrorDialog(
              shell, BaseMessages.getString( PKG, "SpoonSlave.ErrorRemovingJob.Title" ), BaseMessages.getString(
                PKG, "SpoonSlave.ErrorRemovingJob.Message" ), e );
          }
        }
      }
    }
  }

  protected void pause() {
    TreeEntry treeEntry = getTreeEntry();
    if ( treeEntry == null ) {
      return;
    }

    if ( treeEntry.isTransformation() ) {
      // Transformation
      try {
        WebResult webResult = slaveServer.pauseResumeTransformation( treeEntry.name, treeEntry.id );
        if ( !WebResult.STRING_OK.equalsIgnoreCase( webResult.getResult() ) ) {
          EnterTextDialog dialog =
            new EnterTextDialog( shell,
              BaseMessages.getString( PKG, "SpoonSlave.ErrorPausingOrResumingTrans.Title" ),
              BaseMessages.getString( PKG, "SpoonSlave.ErrorPausingOrResumingTrans.Message" ),
              webResult.getMessage() );
          dialog.setReadOnly();
          dialog.open();
        }
      } catch ( Exception e ) {
        new ErrorDialog( shell,
          BaseMessages.getString( PKG, "SpoonSlave.ErrorPausingOrResumingTrans.Title" ),
          BaseMessages.getString( PKG, "SpoonSlave.ErrorPausingOrResumingTrans.Message" ), e );
      }
    }
  }

  private synchronized void refreshView() {
    if ( wTree.isDisposed() ) {
      return;
    }
    if ( refreshBusy ) {
      return;
    }
    refreshBusy = true;

    if ( log.isDetailed() ) {
      log.logDetailed( "Refresh" );
    }

    transParentItem.removeAll();
    jobParentItem.removeAll();
    wText.setText( "" );
    // Determine the transformations on the slave servers
    try {
      slaveServerStatus = slaveServer.getStatus();
    } catch ( Exception e ) {
      slaveServerStatus = new SlaveServerStatus( "Error contacting server" );
      slaveServerStatus.setErrorDescription( Const.getStackTracker( e ) );
      if ( log.isDebug() ) {
        log.logDebug( slaveServerStatus.getErrorDescription() );
      }
      wText.setText( setExceptionMessage( e ) );
    }

    List<SlaveServerTransStatus> transStatusList = slaveServerStatus.getTransStatusList();
    for ( SlaveServerTransStatus transStatus : transStatusList ) {
      TreeItem transItem = new TreeItem( transParentItem, SWT.NONE );
      transItem.setText( 0, transStatus.getTransName() );
      transItem.setText( 9, transStatus.getStatusDescription() );
      transItem.setText( 13, Const.NVL( transStatus.getId(), "" ) );
      transItem.setText( 14, Const.NVL( XMLHandler.date2string( transStatus.getLogDate() ), "" ) );
      transItem.setImage( GUIResource.getInstance().getImageTransGraph() );
      transItem.setData( "transStatus", transStatus );
    }

    for ( int i = 0; i < slaveServerStatus.getJobStatusList().size(); i++ ) {
      SlaveServerJobStatus jobStatus = slaveServerStatus.getJobStatusList().get( i );
      TreeItem jobItem = new TreeItem( jobParentItem, SWT.NONE );
      jobItem.setText( 0, jobStatus.getJobName() );
      jobItem.setText( 9, jobStatus.getStatusDescription() );
      jobItem.setText( 13, Const.NVL( jobStatus.getId(), "" ) );
      jobItem.setText( 14, Const.NVL( XMLHandler.date2string( jobStatus.getLogDate() ), "" ) );
      jobItem.setImage( GUIResource.getInstance().getImageJobGraph() );
      jobItem.setData( "jobStatus", jobStatus );
    }

    TreeMemory.setExpandedFromMemory( wTree, STRING_SLAVE_LOG_TREE_NAME + slaveServer.toString() );
    TreeUtil.setOptimalWidthOnColumns( wTree );
    refreshBusy = false;
  }

  @VisibleForTesting
  protected String setExceptionMessage( Exception e ) {
    Throwable cause = e.getCause();
    if ( cause != null && cause.getMessage() != null ) {
      return e.getCause().getMessage();
    } else {
      return e.getMessage();
    }
  }

  public void showErrors() {
    String all = wText.getText();
    List<String> err = new ArrayList<String>();

    int i = 0;
    int startpos = 0;
    int crlen = Const.CR.length();

    while ( i < all.length() - crlen ) {
      if ( all.substring( i, i + crlen ).equalsIgnoreCase( Const.CR ) ) {
        String line = all.substring( startpos, i );
        if ( lineHasErrors( line ) ) {
          err.add( line );
        }
        // New start of line
        startpos = i + crlen;
      }

      i++;
    }
    String line = all.substring( startpos );
    if ( lineHasErrors( line ) ) { // i18n for compatibilty to non translated steps a.s.o.
      err.add( line );
    }

    if ( err.size() > 0 ) {
      String[] err_lines = new String[err.size()];
      for ( i = 0; i < err_lines.length; i++ ) {
        err_lines[i] = err.get( i );
      }

      EnterSelectionDialog esd = new EnterSelectionDialog( shell, err_lines,
              BaseMessages.getString( PKG, "TransLog.Dialog.ErrorLines.Title" ), BaseMessages.getString( PKG,
                  "TransLog.Dialog.ErrorLines.Message" ) );
      esd.open();
      /*
       * TODO: we have multiple transformation we can go to: which one should we pick? if (line != null) { for (i = 0; i
       * < spoon.getTransMeta().nrSteps(); i++) { StepMeta stepMeta = spoon.getTransMeta().getStep(i); if
       * (line.indexOf(stepMeta.getName()) >= 0) { spoon.editStep(stepMeta.getName()); } } //
       * System.out.println("Error line selected: "+line); }
       */
    }
  }

  private boolean lineHasErrors( String line ) {
    line = line.toUpperCase();
    return line.contains( BaseMessages.getString( PKG, "TransLog.System.ERROR2" ) )
        || line.contains( BaseMessages.getString( PKG, "TransLog.System.EXCEPTION2" ) ) || line.contains( "ERROR" ) || // i18n
                                                                                                                       // for
                                                                                                                       // compatibilty
                                                                                                                       // to
                                                                                                                       // non
                                                                                                                       // translated
                                                                                                                       // steps
                                                                                                                       // a.s.o.
        line.contains( "EXCEPTION" );
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

      public void setModifiedUser( String user ) {
      }

      public void setModifiedDate( Date date ) {
      }

      public void setInternalKettleVariables() {
      }

      public void setObjectId( ObjectId id ) {
      }

      public void setFilename( String filename ) {
      }

      public void setCreatedUser( String createduser ) {
      }

      public void setCreatedDate( Date date ) {
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

      public void setName( String name ) {
      }

      public void setRepositoryDirectory( RepositoryDirectoryInterface repositoryDirectory ) {
      }

      public String getDescription() {
        return null;
      }

      public void setDescription( String description ) {
      }

      public ObjectRevision getObjectRevision() {
        return null;
      }

      public void setObjectRevision( ObjectRevision objectRevision ) {
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
    TreeItem[] ti = wTree.getSelection();
    if ( ti.length == 1 ) {
      TreeItem treeItem = ti[0];
      String[] path = ConstUI.getTreeStrings( treeItem );

      // Make sure we're positioned on a step
      if ( path.length <= 2 ) {
        return;
      }

      String name = path[1];
      String step = path[2];
      String copy = treeItem.getText( 1 );

      EnterNumberDialog numberDialog = new EnterNumberDialog( shell, PropsUI.getInstance().getDefaultPreviewSize(),
        BaseMessages.getString( PKG, "SpoonSlave.SniffSizeQuestion.Title" ),
        BaseMessages.getString( PKG, "SpoonSlave.SniffSizeQuestion.Message" ) );
      int lines = numberDialog.open();
      if ( lines <= 0 ) {
        return;
      }

      EnterSelectionDialog selectionDialog = new EnterSelectionDialog( shell,
        new String[] { SniffStepServlet.TYPE_INPUT, SniffStepServlet.TYPE_OUTPUT, },
        BaseMessages.getString( PKG, "SpoonSlave.SniffTypeQuestion.Title" ),
        BaseMessages.getString( PKG, "SpoonSlave.SniffTypeQuestion.Message" ) );
      String type = selectionDialog.open( 1 );
      if ( type == null ) {
        return;
      }

      try {
        String xml = slaveServer.sniffStep( name, step, copy, lines, type );

        Document doc = XMLHandler.loadXMLString( xml );
        Node node = XMLHandler.getSubNode( doc, SniffStepServlet.XML_TAG );
        Node metaNode = XMLHandler.getSubNode( node, RowMeta.XML_META_TAG );
        RowMetaInterface rowMeta = new RowMeta( metaNode );

        int nrRows = Const.toInt( XMLHandler.getTagValue( node, "nr_rows" ), 0 );
        List<Object[]> rowBuffer = new ArrayList<Object[]>();
        for ( int i = 0; i < nrRows; i++ ) {
          Node dataNode = XMLHandler.getSubNodeByNr( node, RowMeta.XML_DATA_TAG, i );
          Object[] row = rowMeta.getRow( dataNode );
          rowBuffer.add( row );
        }

        PreviewRowsDialog prd = new PreviewRowsDialog( shell, new Variables(), SWT.NONE, step, rowMeta, rowBuffer );
        prd.open();
      } catch ( Exception e ) {
        new ErrorDialog( shell,
          BaseMessages.getString( PKG, "SpoonSlave.ErrorSniffingStep.Title" ),
          BaseMessages.getString( PKG, "SpoonSlave.ErrorSniffingStep.Message" ), e );
      }
    }
  }

  public ChangedWarningInterface getChangedWarning() {
    return null;
  }

  /**
   * Cancels current timer task if any and schedules new one to be executed immediately and then every UPDATE_TIME_VIEW
   * milliseconds.
   */
  private void engageViewAndLogUpdateTimer() {
    if ( timer == null ) {
      timer = new Timer( "SpoonSlave: " + getMeta().getName() );
    }

    if ( timerTask != null ) {
      timerTask.cancel();
    }

    timerTask = new TimerTask() {
      public void run() {
        if ( display != null && !display.isDisposed() ) {
          display.asyncExec( new Runnable() {
            public void run() {
              refreshViewAndLog();
            }
          } );
        }
      }
    };

    timer.schedule( timerTask, 0L, UPDATE_TIME_VIEW );
  }
}
