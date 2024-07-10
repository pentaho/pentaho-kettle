// CHECKSTYLE:FileLength:OFF
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

package org.pentaho.di.ui.spoon.job;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.Callable;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.window.DefaultToolTip;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.events.MouseWheelListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Widget;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.EngineMetaInterface;
import org.pentaho.di.core.NotePadMeta;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.ResultFile;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.dnd.DragAndDropContainer;
import org.pentaho.di.core.dnd.XMLTransfer;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.extension.ExtensionPointHandler;
import org.pentaho.di.core.extension.KettleExtensionPoint;
import org.pentaho.di.core.gui.AreaOwner;
import org.pentaho.di.core.gui.AreaOwner.AreaType;
import org.pentaho.di.core.gui.GCInterface;
import org.pentaho.di.core.gui.Point;
import org.pentaho.di.core.gui.Redrawable;
import org.pentaho.di.core.gui.SnapAllignDistribute;
import org.pentaho.di.core.logging.HasLogChannelInterface;
import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.logging.LogParentProvidedInterface;
import org.pentaho.di.core.logging.LoggingObjectType;
import org.pentaho.di.core.logging.SimpleLoggingObject;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobAdapter;
import org.pentaho.di.job.JobEntryListener;
import org.pentaho.di.job.JobEntryResult;
import org.pentaho.di.job.JobExecutionConfiguration;
import org.pentaho.di.job.JobHopMeta;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.JobPainter;
import org.pentaho.di.job.entries.job.JobEntryJob;
import org.pentaho.di.job.entries.trans.JobEntryTrans;
import org.pentaho.di.job.entry.JobEntryCopy;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.KettleRepositoryLostException;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.repository.RepositoryObjectType;
import org.pentaho.di.repository.RepositoryOperation;
import org.pentaho.di.shared.SharedObjects;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.TransPainter;
import org.pentaho.di.ui.core.ConstUI;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.dialog.EnterTextDialog;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.widget.CheckBoxToolTip;
import org.pentaho.di.ui.core.widget.CheckBoxToolTipListener;
import org.pentaho.di.ui.job.dialog.JobDialog;
import org.pentaho.di.ui.repository.RepositorySecurityUI;
import org.pentaho.di.ui.repository.dialog.RepositoryExplorerDialog;
import org.pentaho.di.ui.repository.dialog.RepositoryRevisionBrowserDialogInterface;
import org.pentaho.di.ui.spoon.AbstractGraph;
import org.pentaho.di.ui.spoon.SWTGC;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.ui.spoon.SpoonPluginManager;
import org.pentaho.di.ui.spoon.SwtScrollBar;
import org.pentaho.di.ui.spoon.TabItemInterface;
import org.pentaho.di.ui.spoon.TabMapEntry;
import org.pentaho.di.ui.spoon.TabMapEntry.ObjectType;
import org.pentaho.di.ui.spoon.XulSpoonResourceBundle;
import org.pentaho.di.ui.spoon.XulSpoonSettingsManager;
import org.pentaho.di.ui.spoon.dialog.NotePadDialog;
import org.pentaho.di.ui.spoon.trans.DelayListener;
import org.pentaho.di.ui.spoon.trans.DelayTimer;
import org.pentaho.di.ui.spoon.trans.TransGraph;
import org.pentaho.di.ui.xul.KettleXulLoader;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.components.XulMenuitem;
import org.pentaho.ui.xul.components.XulToolbarbutton;
import org.pentaho.ui.xul.containers.XulMenu;
import org.pentaho.ui.xul.containers.XulMenupopup;
import org.pentaho.ui.xul.containers.XulToolbar;
import org.pentaho.ui.xul.dom.Document;
import org.pentaho.ui.xul.impl.XulEventHandler;
import org.pentaho.ui.xul.jface.tags.JfaceMenuitem;
import org.pentaho.ui.xul.jface.tags.JfaceMenupopup;

/**
 * Handles the display of Jobs in Spoon, in a graphical form.
 *
 * @author Matt Created on 17-may-2003
 *
 */
public class JobGraph extends AbstractGraph implements XulEventHandler, Redrawable, TabItemInterface,
  LogParentProvidedInterface, MouseListener, MouseMoveListener, MouseTrackListener, MouseWheelListener,
  KeyListener {

  private static Class<?> PKG = JobGraph.class; // for i18n purposes, needed by Translator2!!

  private static final String XUL_FILE_JOB_GRAPH = "ui/job-graph.xul";

  public static final String START_TEXT = BaseMessages.getString( PKG, "JobLog.Button.Start" );
  public static final String STOP_TEXT = BaseMessages.getString( PKG, "JobLog.Button.Stop" );

  private static final String STRING_PARALLEL_WARNING_PARAMETER = "ParallelJobEntriesWarning";

  private static final int HOP_SEL_MARGIN = 9;

  protected Shell shell;

  protected LogChannelInterface log;

  protected JobMeta jobMeta;

  public Job job;

  protected PropsUI props;

  protected int iconsize;

  protected int linewidth;

  protected Point lastclick;

  protected List<JobEntryCopy> selectedEntries;

  protected JobEntryCopy selectedEntry;

  protected Point[] previousLocations;

  private List<NotePadMeta> selectedNotes;
  protected NotePadMeta selectedNote;

  protected Point previous_note_location;

  protected Point lastMove;

  protected JobHopMeta hop_candidate;

  protected Point drop_candidate;

  protected Spoon spoon;

  // public boolean shift, control;
  protected boolean split_hop;

  protected int lastButton;

  protected JobHopMeta last_hop_split;

  protected org.pentaho.di.core.gui.Rectangle selectionRegion;

  protected static final double theta = Math.toRadians( 10 ); // arrowhead sharpness

  protected static final int size = 30; // arrowhead length

  protected int shadowsize;

  protected Map<String, XulMenupopup> menuMap = new HashMap<>();

  protected int currentMouseX = 0;

  protected int currentMouseY = 0;

  protected JobEntryCopy jobEntry;

  protected NotePadMeta ni = null;

  protected JobHopMeta currentHop;

  // private Text filenameLabel;
  private SashForm sashForm;

  public Composite extraViewComposite;

  public CTabFolder extraViewTabFolder;

  private XulToolbar toolbar;

  public JobLogDelegate jobLogDelegate;

  public JobHistoryDelegate jobHistoryDelegate;

  public JobGridDelegate jobGridDelegate;

  public JobMetricsDelegate jobMetricsDelegate;

  private Composite mainComposite;

  private List<RefreshListener> refreshListeners;

  private Label closeButton;

  private Label minMaxButton;

  private CheckBoxToolTip helpTip;

  private List<AreaOwner> areaOwners;

  private List<JobEntryCopy> mouseOverEntries;

  /** A map that keeps track of which log line was written by which job entry */
  private Map<JobEntryCopy, String> entryLogMap;

  private Map<JobEntryCopy, DelayTimer> delayTimers;

  private JobEntryCopy startHopEntry;
  private Point endHopLocation;

  private JobEntryCopy endHopEntry;
  private JobEntryCopy noInputEntry;
  private DefaultToolTip toolTip;
  private Point[] previous_step_locations;
  private Point[] previous_note_locations;
  private JobEntryCopy currentEntry;

  public JobGraph( Composite par, final Spoon spoon, final JobMeta jobMeta ) {
    super( par, SWT.NONE );
    shell = par.getShell();
    this.log = spoon.getLog();
    this.spoon = spoon;
    this.jobMeta = jobMeta;
    spoon.clearSearchFilter();

    this.props = PropsUI.getInstance();
    this.areaOwners = new ArrayList<>();
    this.mouseOverEntries = new ArrayList<>();
    this.delayTimers = new HashMap<>();

    jobLogDelegate = new JobLogDelegate( spoon, this );
    jobHistoryDelegate = new JobHistoryDelegate( spoon, this );
    jobGridDelegate = new JobGridDelegate( spoon, this );
    jobMetricsDelegate = new JobMetricsDelegate( spoon, this );

    refreshListeners = new ArrayList<RefreshListener>();

    try {
      KettleXulLoader loader = new KettleXulLoader();
      loader.setIconsSize( 16, 16 );
      loader.setSettingsManager( XulSpoonSettingsManager.getInstance() );
      ResourceBundle bundle = new XulSpoonResourceBundle( JobGraph.class );
      XulDomContainer container = loader.loadXul( XUL_FILE_JOB_GRAPH, bundle );
      container.addEventHandler( this );

      SpoonPluginManager.getInstance().applyPluginsForContainer( "job-graph", xulDomContainer );

      setXulDomContainer( container );
    } catch ( XulException e1 ) {
      log.logError( toString(), Const.getStackTracker( e1 ) );
    }

    setLayout( new FormLayout() );
    setLayoutData( new GridData( GridData.FILL_BOTH ) );

    // Add a tool-bar at the top of the tab
    // The form-data is set on the native widget automatically
    //
    addToolBar();

    // The main composite contains the graph view, but if needed also
    // a view with an extra tab containing log, etc.
    //
    mainComposite = new Composite( this, SWT.NONE );
    mainComposite.setLayout( new FillLayout() );

    // Nick's fix below -------
    Control toolbarControl = (Control) toolbar.getManagedObject();

    FormData toolbarFd = new FormData();
    toolbarFd.left = new FormAttachment( 0, 0 );
    toolbarFd.right = new FormAttachment( 100, 0 );

    toolbarControl.setLayoutData( toolbarFd );
    toolbarControl.setParent( this );
    // ------------------------

    FormData fdMainComposite = new FormData();
    fdMainComposite.left = new FormAttachment( 0, 0 );
    fdMainComposite.top = new FormAttachment( (Control) toolbar.getManagedObject(), 0 );
    fdMainComposite.right = new FormAttachment( 100, 0 );
    fdMainComposite.bottom = new FormAttachment( 100, 0 );
    mainComposite.setLayoutData( fdMainComposite );

    // To allow for a splitter later on, we will add the splitter here...
    //
    sashForm = new SashForm( mainComposite, SWT.VERTICAL );

    // Add a canvas below it, use up all space initially
    //
    canvas = new Canvas( sashForm, SWT.V_SCROLL | SWT.H_SCROLL | SWT.NO_BACKGROUND | SWT.BORDER );

    sashForm.setWeights( new int[] { 100, } );

    try {
      Document doc = xulDomContainer.getDocumentRoot();
      menuMap.put( "job-graph-hop", (XulMenupopup) doc.getElementById( "job-graph-hop" ) );
      menuMap.put( "job-graph-note", (XulMenupopup) doc.getElementById( "job-graph-note" ) );
      menuMap.put( "job-graph-background", (XulMenupopup) doc.getElementById( "job-graph-background" ) );
      menuMap.put( "job-graph-entry", (XulMenupopup) doc.getElementById( "job-graph-entry" ) );
    } catch ( Throwable t ) {
      log.logError( Const.getStackTracker( t ) );
      new ErrorDialog(
        shell, BaseMessages.getString( PKG, "JobGraph.Exception.ErrorReadingXULFile.Title" ), BaseMessages
          .getString( PKG, "JobGraph.Exception.ErrorReadingXULFile.Message", Spoon.XUL_FILE_MENUS ),
        new Exception( t ) );
    }

    toolTip = new DefaultToolTip( canvas, ToolTip.NO_RECREATE, true );
    toolTip.setRespectMonitorBounds( true );
    toolTip.setRespectDisplayBounds( true );
    toolTip.setPopupDelay( 350 );
    toolTip.setShift( new org.eclipse.swt.graphics.Point( ConstUI.TOOLTIP_OFFSET, ConstUI.TOOLTIP_OFFSET ) );

    helpTip = new CheckBoxToolTip( canvas );
    helpTip.addCheckBoxToolTipListener( new CheckBoxToolTipListener() {

      public void checkBoxSelected( boolean enabled ) {
        spoon.props.setShowingHelpToolTips( enabled );
      }
    } );

    newProps();

    selectionRegion = null;
    hop_candidate = null;
    last_hop_split = null;

    selectedEntries = null;
    selectedNote = null;

    hori = canvas.getHorizontalBar();
    vert = canvas.getVerticalBar();

    hori.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e ) {
        redraw();
      }
    } );
    vert.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e ) {
        redraw();
      }
    } );
    hori.setThumb( 100 );
    vert.setThumb( 100 );

    hori.setVisible( true );
    vert.setVisible( true );

    setVisible( true );

    canvas.addPaintListener( new PaintListener() {
      public void paintControl( PaintEvent e ) {
        JobGraph.this.paintControl( e );
      }
    } );

    selectedEntries = null;
    lastclick = null;

    canvas.addMouseListener( this );
    canvas.addMouseMoveListener( this );
    canvas.addMouseTrackListener( this );
    canvas.addMouseWheelListener( this );

    // Drag & Drop for steps
    Transfer[] ttypes = new Transfer[] { XMLTransfer.getInstance() };
    DropTarget ddTarget = new DropTarget( canvas, DND.DROP_MOVE );
    ddTarget.setTransfer( ttypes );
    ddTarget.addDropListener( new DropTargetListener() {
      public void dragEnter( DropTargetEvent event ) {
        drop_candidate = PropsUI.calculateGridPosition( getRealPosition( canvas, event.x, event.y ) );
        redraw();
      }

      public void dragLeave( DropTargetEvent event ) {
        drop_candidate = null;
        redraw();
      }

      public void dragOperationChanged( DropTargetEvent event ) {
      }

      public void dragOver( DropTargetEvent event ) {
        drop_candidate = PropsUI.calculateGridPosition( getRealPosition( canvas, event.x, event.y ) );
        redraw();
      }

      public void drop( DropTargetEvent event ) {
        // no data to copy, indicate failure in event.detail
        if ( event.data == null ) {
          event.detail = DND.DROP_NONE;
          return;
        }

        Point p = getRealPosition( canvas, event.x, event.y );

        try {
          DragAndDropContainer container = (DragAndDropContainer) event.data;
          String entry = container.getData();

          switch ( container.getType() ) {
            case DragAndDropContainer.TYPE_BASE_JOB_ENTRY: // Create a new Job Entry on the canvas
              JobEntryCopy jge = spoon.newJobEntry( jobMeta, entry, false );
              if ( jge != null ) {
                PropsUI.setLocation( jge, p.x, p.y );
                jge.setDrawn();
                redraw();

                // See if we want to draw a tool tip explaining how to create new hops...
                //
                if ( jobMeta.nrJobEntries() > 1
                  && jobMeta.nrJobEntries() < 5 && spoon.props.isShowingHelpToolTips() ) {
                  showHelpTip(
                    p.x, p.y, BaseMessages.getString( PKG, "JobGraph.HelpToolTip.CreatingHops.Title" ),
                    BaseMessages.getString( PKG, "JobGraph.HelpToolTip.CreatingHops.Message" ) );
                }
              }
              break;

            case DragAndDropContainer.TYPE_JOB_ENTRY: // Drag existing one onto the canvas
              jge = jobMeta.findJobEntry( entry, 0, true );
              if ( jge != null ) {
                // Create duplicate of existing entry

                // There can be only 1 start!
                if ( jge.isStart() && jge.isDrawn() ) {
                  showOnlyStartOnceMessage( shell );
                  return;
                }

                boolean jge_changed = false;

                // For undo :
                JobEntryCopy before = (JobEntryCopy) jge.clone_deep();

                JobEntryCopy newjge = jge;
                if ( jge.isDrawn() ) {
                  newjge = (JobEntryCopy) jge.clone();
                  if ( newjge != null ) {
                    // newjge.setEntry(jge.getEntry());
                    if ( log.isDebug() ) {
                      log.logDebug( "entry aft = " + ( (Object) jge.getEntry() ).toString() );
                    }

                    newjge.setNr( jobMeta.findUnusedNr( newjge.getName() ) );

                    jobMeta.addJobEntry( newjge );
                    spoon.addUndoNew( jobMeta, new JobEntryCopy[] { newjge }, new int[] { jobMeta
                      .indexOfJobEntry( newjge ) } );
                  } else {
                    if ( log.isDebug() ) {
                      log.logDebug( "jge is not cloned!" );
                    }
                  }
                } else {
                  if ( log.isDebug() ) {
                    log.logDebug( jge.toString() + " is not drawn" );
                  }
                  jge_changed = true;
                }
                PropsUI.setLocation( newjge, p.x, p.y );
                newjge.setDrawn();
                if ( jge_changed ) {
                  spoon.addUndoChange(
                    jobMeta, new JobEntryCopy[] { before }, new JobEntryCopy[] { newjge }, new int[] { jobMeta
                      .indexOfJobEntry( newjge ) } );
                }
                redraw();
                spoon.refreshTree();
                log.logBasic( "DropTargetEvent", "DROP "
                  + newjge.toString() + "!, type=" + newjge.getEntry().getPluginId() );
              } else {
                log.logError( "Unknown job entry dropped onto the canvas." );
              }
              break;

            default:
              break;
          }
        } catch ( Exception e ) {
          new ErrorDialog(
            shell, BaseMessages.getString( PKG, "JobGraph.Dialog.ErrorDroppingObject.Message" ), BaseMessages
              .getString( PKG, "JobGraph.Dialog.ErrorDroppingObject.Title" ), e );
        }
      }

      public void dropAccept( DropTargetEvent event ) {
        drop_candidate = null;
      }
    } );

    canvas.addKeyListener( this );

    setBackground( GUIResource.getInstance().getColorBackground() );

    setControlStates();

    // Add a timer to set correct the state of the run/stop buttons every 2 seconds...
    //
    final Timer timer = new Timer( "JobGraph.setControlStates Timer: " + getMeta().getName() );
    TimerTask timerTask = new TimerTask() {
      public void run() {
        try {
          setControlStates();
        } catch ( KettleRepositoryLostException krle ) {
          if ( log.isBasic() ) {
            log.logBasic( krle.getLocalizedMessage() );
          }
        }
      }
    };
    timer.schedule( timerTask, 2000, 1000 );

    // Make sure the timer stops when we close the tab...
    //
    addDisposeListener( new DisposeListener() {
      public void widgetDisposed( DisposeEvent arg0 ) {
        timer.cancel();
      }
    } );

  }

  protected void hideToolTips() {
    toolTip.hide();
    helpTip.hide();
  }

  public void mouseDoubleClick( MouseEvent e ) {
    clearSettings();

    Point real = screen2real( e.x, e.y );

    // Hide the tooltip!
    hideToolTips();

    try {
      ExtensionPointHandler.callExtensionPoint( LogChannel.GENERAL, KettleExtensionPoint.JobGraphMouseDoubleClick.id,
        new JobGraphExtension( this, e, real ) );
    } catch ( Exception ex ) {
      LogChannel.GENERAL.logError( "Error calling JobGraphMouseDoubleClick extension point", ex );
    }

    JobEntryCopy jobentry = jobMeta.getJobEntryCopy( real.x, real.y, iconsize );
    if ( jobentry != null ) {
      if ( e.button == 1 ) {
        editEntry( jobentry );
      } else {
        // open tab in Spoon
        launchStuff( jobentry );
      }
    } else {
      // Check if point lies on one of the many hop-lines...
      JobHopMeta online = findJobHop( real.x, real.y );
      if ( online == null ) {
        NotePadMeta ni = jobMeta.getNote( real.x, real.y );
        if ( ni != null ) {
          editNote( ni );
        } else {
          // Clicked on the background...
          //
          editJobProperties();
        }
      }

    }
  }

  public void mouseDown( MouseEvent e ) {

    boolean control = ( e.stateMask & SWT.MOD1 ) != 0;
    boolean shift = ( e.stateMask & SWT.SHIFT ) != 0;

    lastButton = e.button;
    Point real = screen2real( e.x, e.y );
    lastclick = new Point( real.x, real.y );

    // Hide the tooltip!
    hideToolTips();

    // Set the pop-up menu
    if ( e.button == 3 ) {
      setMenu( real.x, real.y );
      return;
    }

    try {
      ExtensionPointHandler.callExtensionPoint( LogChannel.GENERAL, KettleExtensionPoint.JobGraphMouseDown.id,
        new JobGraphExtension( this, e, real ) );
    } catch ( Exception ex ) {
      LogChannel.GENERAL.logError( "Error calling JobGraphMouseDown extension point", ex );
    }

    // A single left or middle click on one of the area owners...
    //
    if ( e.button == 1 || e.button == 2 ) {
      AreaOwner areaOwner = getVisibleAreaOwner( real.x, real.y );
      if ( areaOwner != null && areaOwner.getAreaType() != null ) {
        switch ( areaOwner.getAreaType() ) {
          case JOB_ENTRY_MINI_ICON_OUTPUT:
            // Click on the output icon means: start of drag
            // Action: We show the input icons on the other steps...
            //
            selectedEntry = null;
            startHopEntry = (JobEntryCopy) areaOwner.getOwner();
            // stopEntryMouseOverDelayTimer(startHopEntry);
            break;

          case JOB_ENTRY_MINI_ICON_INPUT:
            // Click on the input icon means: start to a new hop
            // In this case, we set the end hop step...
            //
            selectedEntry = null;
            startHopEntry = null;
            endHopEntry = (JobEntryCopy) areaOwner.getOwner();
            // stopEntryMouseOverDelayTimer(endHopEntry);
            break;

          case JOB_ENTRY_MINI_ICON_EDIT:
            clearSettings();
            currentEntry = (JobEntryCopy) areaOwner.getOwner();
            stopEntryMouseOverDelayTimer( currentEntry );
            editEntry( currentEntry );
            break;

          case JOB_ENTRY_MINI_ICON_CONTEXT:
            clearSettings();
            JobEntryCopy jobEntryCopy = (JobEntryCopy) areaOwner.getOwner();
            setMenu( jobEntryCopy.getLocation().x, jobEntryCopy.getLocation().y );
            break;

          case JOB_ENTRY_ICON:
            jobEntryCopy = (JobEntryCopy) areaOwner.getOwner();
            currentEntry = jobEntryCopy;

            if ( hop_candidate != null ) {
              addCandidateAsHop();

            } else if ( e.button == 2 || ( e.button == 1 && shift ) ) {
              // SHIFT CLICK is start of drag to create a new hop
              //
              startHopEntry = jobEntryCopy;

            } else {
              selectedEntries = jobMeta.getSelectedEntries();
              selectedEntry = jobEntryCopy;
              //
              // When an icon is moved that is not selected, it gets
              // selected too late.
              // It is not captured here, but in the mouseMoveListener...
              //
              previous_step_locations = jobMeta.getSelectedLocations();

              Point p = jobEntryCopy.getLocation();
              iconoffset = new Point( real.x - p.x, real.y - p.y );
            }
            redraw();
            break;

          case NOTE:
            ni = (NotePadMeta) areaOwner.getOwner();
            selectedNotes = jobMeta.getSelectedNotes();
            selectedNote = ni;
            Point loc = ni.getLocation();

            previous_note_locations = jobMeta.getSelectedNoteLocations();

            noteoffset = new Point( real.x - loc.x, real.y - loc.y );

            redraw();
            break;

          // If you click on an evaluating icon, change the evaluation...
          //
          case JOB_HOP_ICON:
            JobHopMeta hop = (JobHopMeta) areaOwner.getOwner();
            if ( hop.getFromEntry().evaluates() ) {
              if ( hop.isUnconditional() ) {
                hop.setUnconditional( false );
                hop.setEvaluation( true );
              } else {
                if ( hop.getEvaluation() ) {
                  hop.setEvaluation( false );
                } else {
                  hop.setUnconditional( true );
                }
              }
              spoon.setShellText();
              redraw();
            }
            break;
          default:
            break;
        }
      } else {
        // A hop? --> enable/disable
        //
        JobHopMeta hop = findJobHop( real.x, real.y );
        if ( hop != null ) {
          JobHopMeta before = (JobHopMeta) hop.clone();
          hop.setEnabled( !hop.isEnabled() );
          if ( hop.isEnabled() && ( jobMeta.hasLoop( hop.getToEntry() ) ) ) {
            MessageBox mb = new MessageBox( shell, SWT.CANCEL | SWT.OK | SWT.ICON_WARNING );
            mb.setMessage( BaseMessages.getString( PKG, "JobGraph.Dialog.LoopAfterHopEnabled.Message" ) );
            mb.setText( BaseMessages.getString( PKG, "JobGraph.Dialog.LoopAfterHopEnabled.Title" ) );
            int choice = mb.open();
            if ( choice == SWT.CANCEL ) {
              hop.setEnabled( false );
            }
          }
          JobHopMeta after = (JobHopMeta) hop.clone();
          spoon.addUndoChange(
            jobMeta, new JobHopMeta[] { before }, new JobHopMeta[] { after }, new int[] { jobMeta
              .indexOfJobHop( hop ) } );
          spoon.setShellText();
          redraw();
        } else {
          // No area-owner means: background:
          //
          startHopEntry = null;
          if ( !control ) {
            selectionRegion = new org.pentaho.di.core.gui.Rectangle( real.x, real.y, 0, 0 );
          }
          redraw();
        }
      }
    }
  }

  public void mouseUp( MouseEvent e ) {
    boolean control = ( e.stateMask & SWT.MOD1 ) != 0;

    if ( iconoffset == null ) {
      iconoffset = new Point( 0, 0 );
    }
    Point real = screen2real( e.x, e.y );
    Point icon = new Point( real.x - iconoffset.x, real.y - iconoffset.y );

    // Quick new hop option? (drag from one step to another)
    //
    if ( hop_candidate != null ) {
      addCandidateAsHop();
      redraw();
    } else {
      // Did we select a region on the screen? Mark steps in region as
      // selected
      //
      if ( selectionRegion != null ) {
        selectionRegion.width = real.x - selectionRegion.x;
        selectionRegion.height = real.y - selectionRegion.y;

        jobMeta.unselectAll();
        selectInRect( jobMeta, selectionRegion );
        selectionRegion = null;
        stopEntryMouseOverDelayTimers();
        redraw();
      } else {
        // Clicked on an icon?
        //
        if ( selectedEntry != null && startHopEntry == null ) {
          if ( e.button == 1 ) {
            Point realclick = screen2real( e.x, e.y );
            if ( lastclick.x == realclick.x && lastclick.y == realclick.y ) {
              // Flip selection when control is pressed!
              if ( control ) {
                selectedEntry.flipSelected();
              } else {
                // Otherwise, select only the icon clicked on!
                jobMeta.unselectAll();
                selectedEntry.setSelected( true );
              }
            } else {
              // Find out which Steps & Notes are selected
              selectedEntries = jobMeta.getSelectedEntries();
              selectedNotes = jobMeta.getSelectedNotes();

              // We moved around some items: store undo info...
              //
              boolean also = false;
              if ( selectedNotes != null && selectedNotes.size() > 0 && previous_note_locations != null ) {
                int[] indexes = jobMeta.getNoteIndexes( selectedNotes );

                addUndoPosition(
                  selectedNotes.toArray( new NotePadMeta[selectedNotes.size()] ), indexes,
                  previous_note_locations, jobMeta.getSelectedNoteLocations(), also );
                also = selectedEntries != null && selectedEntries.size() > 0;
              }
              if ( selectedEntries != null && selectedEntries.size() > 0 && previous_step_locations != null ) {
                int[] indexes = jobMeta.getEntryIndexes( selectedEntries );
                addUndoPosition(
                  selectedEntries.toArray( new JobEntryCopy[selectedEntries.size()] ), indexes,
                  previous_step_locations, jobMeta.getSelectedLocations(), also );
              }
            }
          }

          // OK, we moved the step, did we move it across a hop?
          // If so, ask to split the hop!
          if ( split_hop ) {
            JobHopMeta hi = findHop( icon.x + iconsize / 2, icon.y + iconsize / 2, selectedEntry );
            if ( hi != null ) {
              int id = 0;
              if ( !spoon.props.getAutoSplit() ) {
                MessageDialogWithToggle md =
                  new MessageDialogWithToggle(
                    shell,
                    BaseMessages.getString( PKG, "TransGraph.Dialog.SplitHop.Title" ),
                    null,
                    BaseMessages.getString( PKG, "TransGraph.Dialog.SplitHop.Message" )
                      + Const.CR + hi.toString(),
                    MessageDialog.QUESTION,
                    new String[] {
                      BaseMessages.getString( PKG, "System.Button.Yes" ),
                      BaseMessages.getString( PKG, "System.Button.No" ) },
                    0,
                    BaseMessages.getString( PKG, "TransGraph.Dialog.Option.SplitHop.DoNotAskAgain" ),
                    spoon.props.getAutoSplit() );
                MessageDialogWithToggle.setDefaultImage( GUIResource.getInstance().getImageSpoon() );
                id = md.open();
                spoon.props.setAutoSplit( md.getToggleState() );
              }

              if ( ( id & 0xFF ) == 0 ) {
                // Means: "Yes" button clicked!

                // Only split A-->--B by putting C in between IF...
                // C-->--A or B-->--C don't exists...
                // A ==> hi.getFromEntry()
                // B ==> hi.getToEntry();
                // C ==> selected_step
                //
                if ( jobMeta.findJobHop( selectedEntry, hi.getFromEntry() ) == null
                  && jobMeta.findJobHop( hi.getToEntry(), selectedEntry ) == null ) {

                  if ( jobMeta.findJobHop( hi.getFromEntry(), selectedEntry, true ) == null ) {
                    JobHopMeta newhop1 = new JobHopMeta( hi.getFromEntry(), selectedEntry );
                    if ( hi.getFromEntry().getEntry().isUnconditional() ) {
                      newhop1.setUnconditional();
                    }
                    jobMeta.addJobHop( newhop1 );
                    spoon.addUndoNew( jobMeta, new JobHopMeta[] { newhop1, }, new int[] { jobMeta
                      .indexOfJobHop( newhop1 ), }, true );
                  }
                  if ( jobMeta.findJobHop( selectedEntry, hi.getToEntry(), true ) == null ) {
                    JobHopMeta newhop2 = new JobHopMeta( selectedEntry, hi.getToEntry() );
                    if ( selectedEntry.getEntry().isUnconditional() ) {
                      newhop2.setUnconditional();
                    }
                    jobMeta.addJobHop( newhop2 );
                    spoon.addUndoNew( jobMeta, new JobHopMeta[] { newhop2, }, new int[] { jobMeta
                      .indexOfJobHop( newhop2 ), }, true );
                  }

                  int idx = jobMeta.indexOfJobHop( hi );
                  spoon.addUndoDelete( jobMeta, new JobHopMeta[] { hi }, new int[] { idx }, true );
                  jobMeta.removeJobHop( idx );
                  spoon.refreshTree();

                }
                // else: Silently discard this hop-split attempt.
              }
            }
            split_hop = false;
          }

          selectedEntries = null;
          selectedNotes = null;
          selectedEntry = null;
          selectedNote = null;
          startHopEntry = null;
          endHopLocation = null;
          redraw();
          spoon.setShellText();
        } else {
          // Notes?
          if ( selectedNote != null ) {
            if ( e.button == 1 ) {
              if ( lastclick.x == e.x && lastclick.y == e.y ) {
                // Flip selection when control is pressed!
                if ( control ) {
                  selectedNote.flipSelected();
                } else {
                  // Otherwise, select only the note clicked on!
                  jobMeta.unselectAll();
                  selectedNote.setSelected( true );
                }
              } else {
                // Find out which Steps & Notes are selected
                selectedEntries = jobMeta.getSelectedEntries();
                selectedNotes = jobMeta.getSelectedNotes();

                // We moved around some items: store undo info...
                boolean also = false;
                if ( selectedNotes != null && selectedNotes.size() > 0 && previous_note_locations != null ) {
                  int[] indexes = jobMeta.getNoteIndexes( selectedNotes );
                  addUndoPosition(
                    selectedNotes.toArray( new NotePadMeta[selectedNotes.size()] ), indexes,
                    previous_note_locations, jobMeta.getSelectedNoteLocations(), also );
                  also = selectedEntries != null && selectedEntries.size() > 0;
                }
                if ( selectedEntries != null && selectedEntries.size() > 0 && previous_step_locations != null ) {
                  int[] indexes = jobMeta.getEntryIndexes( selectedEntries );
                  addUndoPosition(
                    selectedEntries.toArray( new JobEntryCopy[selectedEntries.size()] ), indexes,
                    previous_step_locations, jobMeta.getSelectedLocations(), also );
                }
              }
            }

            selectedNotes = null;
            selectedEntries = null;
            selectedEntry = null;
            selectedNote = null;
            startHopEntry = null;
            endHopLocation = null;
          } else {
            AreaOwner areaOwner = getVisibleAreaOwner( real.x, real.y );
            if ( areaOwner == null && selectionRegion == null ) {
              // Hit absolutely nothing: clear the settings
              //
              clearSettings();
            }
          }
        }
      }
    }

    lastButton = 0;
  }

  public void mouseMove( MouseEvent e ) {
    boolean shift = ( e.stateMask & SWT.SHIFT ) != 0;
    noInputEntry = null;

    // disable the tooltip
    //
    toolTip.hide();

    Point real = screen2real( e.x, e.y );
    // Remember the last position of the mouse for paste with keyboard
    //
    lastMove = real;

    if ( iconoffset == null ) {
      iconoffset = new Point( 0, 0 );
    }
    Point icon = new Point( real.x - iconoffset.x, real.y - iconoffset.y );

    if ( noteoffset == null ) {
      noteoffset = new Point( 0, 0 );
    }
    Point note = new Point( real.x - noteoffset.x, real.y - noteoffset.y );

    // Moved over an area?
    //
    AreaOwner areaOwner = getVisibleAreaOwner( real.x, real.y );
    if ( areaOwner != null && areaOwner.getAreaType() != null ) {
      JobEntryCopy jobEntryCopy = null;
      switch ( areaOwner.getAreaType() ) {
        case JOB_ENTRY_ICON:
          jobEntryCopy = (JobEntryCopy) areaOwner.getOwner();
          resetDelayTimer( jobEntryCopy );
          break;
        case MINI_ICONS_BALLOON: // Give the timer a bit more time
          jobEntryCopy = (JobEntryCopy) areaOwner.getOwner();
          resetDelayTimer( jobEntryCopy );
          break;
        default:
          break;
      }
    }

    //
    // First see if the icon we clicked on was selected.
    // If the icon was not selected, we should un-select all other
    // icons, selected and move only the one icon
    //
    if ( selectedEntry != null && !selectedEntry.isSelected() ) {
      jobMeta.unselectAll();
      selectedEntry.setSelected( true );
      selectedEntries = new ArrayList<>();
      selectedEntries.add( selectedEntry );
      previous_step_locations = new Point[] { selectedEntry.getLocation() };
      redraw();
    } else if ( selectedNote != null && !selectedNote.isSelected() ) {
      jobMeta.unselectAll();
      selectedNote.setSelected( true );
      selectedNotes = new ArrayList<>();
      selectedNotes.add( selectedNote );
      previous_note_locations = new Point[] { selectedNote.getLocation() };
      redraw();
    } else if ( selectionRegion != null && startHopEntry == null ) {
      // Did we select a region...?
      //
      selectionRegion.width = real.x - selectionRegion.x;
      selectionRegion.height = real.y - selectionRegion.y;
      redraw();
    } else if ( selectedEntry != null && lastButton == 1 && !shift && startHopEntry == null ) {
      // Move around steps & notes
      //
      //
      // One or more icons are selected and moved around...
      //
      // new : new position of the ICON (not the mouse pointer) dx : difference with previous position
      //
      int dx = icon.x - selectedEntry.getLocation().x;
      int dy = icon.y - selectedEntry.getLocation().y;

      // See if we have a hop-split candidate
      //
      JobHopMeta hi = findHop( icon.x + iconsize / 2, icon.y + iconsize / 2, selectedEntry );
      if ( hi != null ) {
        // OK, we want to split the hop in 2
        //
        if ( !hi.getFromEntry().equals( selectedEntry ) && !hi.getToEntry().equals( selectedEntry ) ) {
          split_hop = true;
          last_hop_split = hi;
          hi.split = true;
        }
      } else {
        if ( last_hop_split != null ) {
          last_hop_split.split = false;
          last_hop_split = null;
          split_hop = false;
        }
      }

      selectedNotes = jobMeta.getSelectedNotes();
      selectedEntries = jobMeta.getSelectedEntries();

      // Adjust location of selected steps...
      if ( selectedEntries != null ) {
        for ( int i = 0; i < selectedEntries.size(); i++ ) {
          JobEntryCopy jobEntryCopy = selectedEntries.get( i );
          PropsUI.setLocation( jobEntryCopy, jobEntryCopy.getLocation().x + dx, jobEntryCopy.getLocation().y + dy );
          stopEntryMouseOverDelayTimer( jobEntryCopy );
        }
      }
      // Adjust location of selected hops...
      if ( selectedNotes != null ) {
        for ( int i = 0; i < selectedNotes.size(); i++ ) {
          NotePadMeta ni = selectedNotes.get( i );
          PropsUI.setLocation( ni, ni.getLocation().x + dx, ni.getLocation().y + dy );
        }
      }

      redraw();
    } else if ( ( startHopEntry != null && endHopEntry == null )
      || ( endHopEntry != null && startHopEntry == null ) ) {
      // Are we creating a new hop with the middle button or pressing SHIFT?
      //

      JobEntryCopy jobEntryCopy = jobMeta.getJobEntryCopy( real.x, real.y, iconsize );
      endHopLocation = new Point( real.x, real.y );
      if ( jobEntryCopy != null
        && ( ( startHopEntry != null && !startHopEntry.equals( jobEntryCopy ) ) || ( endHopEntry != null && !endHopEntry
          .equals( jobEntryCopy ) ) ) ) {
        if ( hop_candidate == null ) {
          // See if the step accepts input. If not, we can't create a new hop...
          //
          if ( startHopEntry != null ) {
            if ( !jobEntryCopy.isStart() ) {
              hop_candidate = new JobHopMeta( startHopEntry, jobEntryCopy );
              endHopLocation = null;
            } else {
              noInputEntry = jobEntryCopy;
              toolTip.setImage( null );
              toolTip.setText( "The start entry can only be used at the start of a Job" );
              toolTip.show( new org.eclipse.swt.graphics.Point( real.x, real.y ) );
            }
          } else if ( endHopEntry != null ) {
            hop_candidate = new JobHopMeta( jobEntryCopy, endHopEntry );
            endHopLocation = null;
          }
        }
      } else {
        if ( hop_candidate != null ) {
          hop_candidate = null;
          redraw();
        }
      }

      redraw();
    }

    // Move around notes & steps
    //
    if ( selectedNote != null ) {
      if ( lastButton == 1 && !shift ) {
        /*
         * One or more notes are selected and moved around...
         *
         * new : new position of the note (not the mouse pointer) dx : difference with previous position
         */
        int dx = note.x - selectedNote.getLocation().x;
        int dy = note.y - selectedNote.getLocation().y;

        selectedNotes = jobMeta.getSelectedNotes();
        selectedEntries = jobMeta.getSelectedEntries();

        // Adjust location of selected steps...
        if ( selectedEntries != null ) {
          for ( int i = 0; i < selectedEntries.size(); i++ ) {
            JobEntryCopy jobEntryCopy = selectedEntries.get( i );
            PropsUI.setLocation( jobEntryCopy, jobEntryCopy.getLocation().x + dx, jobEntryCopy.getLocation().y
              + dy );
          }
        }
        // Adjust location of selected hops...
        if ( selectedNotes != null ) {
          for ( int i = 0; i < selectedNotes.size(); i++ ) {
            NotePadMeta ni = selectedNotes.get( i );
            PropsUI.setLocation( ni, ni.getLocation().x + dx, ni.getLocation().y + dy );
          }
        }

        redraw();
      }
    }
  }

  public void mouseHover( MouseEvent e ) {

    boolean tip = true;
    boolean isDeprecated = false;

    // toolTip.hide();
    Point real = screen2real( e.x, e.y );

    AreaOwner areaOwner = getVisibleAreaOwner( real.x, real.y );
    if ( areaOwner != null && areaOwner.getAreaType() != null ) {
      switch ( areaOwner.getAreaType() ) {
        case JOB_ENTRY_ICON:
          JobEntryCopy jobEntryCopy = (JobEntryCopy) areaOwner.getOwner();
          isDeprecated = jobEntryCopy.isDeprecated();
          if ( !jobEntryCopy.isMissing() && !mouseOverEntries.contains( jobEntryCopy ) ) {
            addEntryMouseOverDelayTimer( jobEntryCopy );
            redraw();
            tip = false;
          }
          break;
        default:
          break;
      }
    }

    // Show a tool tip upon mouse-over of an object on the canvas
    if ( ( tip && !helpTip.isVisible() ) || isDeprecated ) {
      setToolTip( real.x, real.y, e.x, e.y );
    }
  }

  public void mouseEnter( MouseEvent event ) {
  }

  public void mouseExit( MouseEvent event ) {
  }

  public void mouseScrolled( MouseEvent e ) {
    /*
     * if (e.count == 3) { // scroll up zoomIn(); } else if (e.count == -3) { // scroll down zoomOut(); }
     */
  }

  private void addCandidateAsHop() {
    if ( hop_candidate != null ) {

      if ( !hop_candidate.getFromEntry().evaluates() && hop_candidate.getFromEntry().isUnconditional() ) {
        hop_candidate.setUnconditional();
      } else {
        hop_candidate.setConditional();
        int nr = jobMeta.findNrNextJobEntries( hop_candidate.getFromEntry() );

        // If there is one green link: make this one red! (or
        // vice-versa)
        if ( nr == 1 ) {
          JobEntryCopy jge = jobMeta.findNextJobEntry( hop_candidate.getFromEntry(), 0 );
          JobHopMeta other = jobMeta.findJobHop( hop_candidate.getFromEntry(), jge );
          if ( other != null ) {
            hop_candidate.setEvaluation( !other.getEvaluation() );
          }
        }
      }

      if ( checkIfHopAlreadyExists( jobMeta, hop_candidate ) ) {
        boolean cancel = false;
        jobMeta.addJobHop( hop_candidate );
        if ( jobMeta.hasLoop( hop_candidate.getToEntry() ) ) {
          MessageBox mb = new MessageBox( spoon.getShell(), SWT.OK | SWT.CANCEL | SWT.ICON_WARNING );
          mb.setMessage( BaseMessages.getString( PKG, "JobGraph.Dialog.HopCausesLoop.Message" ) );
          mb.setText( BaseMessages.getString( PKG, "JobGraph.Dialog.HopCausesLoop.Title" ) );
          int choice = mb.open();
          if ( choice == SWT.CANCEL ) {
            jobMeta.removeJobHop( hop_candidate );
            cancel = true;
          }
        }
        if ( !cancel ) {
          spoon.addUndoNew( jobMeta, new JobHopMeta[] { hop_candidate }, new int[] { jobMeta
            .indexOfJobHop( hop_candidate ) } );
        }
        spoon.refreshTree();
        clearSettings();
        redraw();
      }
    }
  }

  public boolean checkIfHopAlreadyExists( JobMeta jobMeta, JobHopMeta newHop ) {
    boolean ok = true;
    if ( jobMeta.findJobHop( newHop.getFromEntry(), newHop.getToEntry(), true ) != null ) {
      MessageBox mb = new MessageBox( shell, SWT.OK | SWT.ICON_ERROR );
      mb.setMessage( BaseMessages.getString( PKG, "JobGraph.Dialog.HopExists.Message" ) ); // "This hop already exists!"
      mb.setText( BaseMessages.getString( PKG, "JobGraph.Dialog.HopExists.Title" ) ); // Error!
      mb.open();
      ok = false;
    }

    return ok;
  }

  public AreaOwner getVisibleAreaOwner( int x, int y ) {
    for ( int i = areaOwners.size() - 1; i >= 0; i-- ) {
      AreaOwner areaOwner = areaOwners.get( i );
      if ( areaOwner.contains( x, y ) ) {
        return areaOwner;
      }
    }
    return null;
  }

  private synchronized void addEntryMouseOverDelayTimer( final JobEntryCopy jobEntryCopy ) {

    // Don't add the same mouse over delay timer twice...
    //
    if ( mouseOverEntries.contains( jobEntryCopy ) ) {
      return;
    }

    mouseOverEntries.add( jobEntryCopy );

    DelayTimer delayTimer = new DelayTimer( 500, new DelayListener() {
      public void expired() {
        mouseOverEntries.remove( jobEntryCopy );
        delayTimers.remove( jobEntryCopy );
        asyncRedraw();
      }
    }, new Callable<Boolean>() {

      @Override
      public Boolean call() throws Exception {
        Point cursor = getLastMove();
        if ( cursor != null ) {
          AreaOwner areaOwner = getVisibleAreaOwner( cursor.x, cursor.y );
          if ( areaOwner != null && areaOwner.getAreaType() != null ) {
            AreaType areaType = areaOwner.getAreaType();
            if ( areaType == AreaType.JOB_ENTRY_ICON || areaType.belongsToJobContextMenu() ) {
              JobEntryCopy selectedJobEntryCopy = (JobEntryCopy) areaOwner.getOwner();
              return selectedJobEntryCopy == jobEntryCopy;
            }
          }
        }
        return false;
      }
    } );

    new Thread( delayTimer ).start();

    delayTimers.put( jobEntryCopy, delayTimer );
  }

  private void stopEntryMouseOverDelayTimer( final JobEntryCopy jobEntryCopy ) {
    DelayTimer delayTimer = delayTimers.get( jobEntryCopy );
    if ( delayTimer != null ) {
      delayTimer.stop();
    }
  }

  private void stopEntryMouseOverDelayTimers() {
    for ( DelayTimer timer : delayTimers.values() ) {
      timer.stop();
    }
  }

  private void resetDelayTimer( JobEntryCopy jobEntryCopy ) {
    DelayTimer delayTimer = delayTimers.get( jobEntryCopy );
    if ( delayTimer != null ) {
      delayTimer.reset();
    }
  }

  protected void asyncRedraw() {
    spoon.getDisplay().asyncExec( new Runnable() {
      public void run() {
        if ( !isDisposed() ) {
          redraw();
        }
      }
    } );
  }

  private void addToolBar() {

    try {
      toolbar = (XulToolbar) getXulDomContainer().getDocumentRoot().getElementById( "nav-toolbar" );

      ToolBar swtToolbar = (ToolBar) toolbar.getManagedObject();
      swtToolbar.setBackground( GUIResource.getInstance().getColorDemoGray() );
      swtToolbar.pack();

      // Added 1/11/2016 to implement dropdown option for "Run"
      ToolItem runItem = new ToolItem( swtToolbar, SWT.DROP_DOWN, 0 );

      runItem.setImage( GUIResource.getInstance().getImage( "ui/images/run.svg" ) );
      runItem.setToolTipText( BaseMessages.getString( PKG, "Spoon.Tooltip.RunTranformation" ) );
      runItem.addSelectionListener( new SelectionAdapter() {

        @Override
        public void widgetSelected( SelectionEvent e ) {
          if ( e.detail == SWT.DROP_DOWN ) {
            Menu menu = new Menu( shell, SWT.POP_UP );

            MenuItem item1 = new MenuItem( menu, SWT.PUSH );
            item1.setText( BaseMessages.getString( PKG, "Spoon.Run.Run" ) );
            item1.setAccelerator( SWT.F9 );
            item1.addSelectionListener( new SelectionAdapter() {
              @Override
              public void widgetSelected( SelectionEvent e1 ) {
                runJob();
              }
            } );
            MenuItem item2 = new MenuItem( menu, SWT.PUSH );
            item2.setText( BaseMessages.getString( PKG, "Spoon.Run.RunOptions" ) );
            item2.setAccelerator( SWT.F8 );
            item2.addSelectionListener( new SelectionAdapter() {
              @Override
              public void widgetSelected( SelectionEvent e2 ) {
                runOptionsJob();
              }
            } );

            menu.setLocation( shell.getDisplay().map( mainComposite.getParent(), null, mainComposite.getLocation() ) );
            menu.setVisible( true );
          } else {
            runJob();
          }
        }
      } );

      // Hack alert : more XUL limitations...
      //
      ToolItem sep = new ToolItem( swtToolbar, SWT.SEPARATOR );

      zoomLabel = new Combo( swtToolbar, SWT.DROP_DOWN );
      zoomLabel.setItems( TransPainter.magnificationDescriptions );
      zoomLabel.addSelectionListener( new SelectionAdapter() {
        public void widgetSelected( SelectionEvent arg0 ) {
          readMagnification();
        }
      } );

      zoomLabel.addKeyListener( new KeyAdapter() {
        public void keyPressed( KeyEvent event ) {
          if ( event.character == SWT.CR ) {
            readMagnification();
          }
        }
      } );

      setZoomLabel();
      zoomLabel.pack();

      sep.setWidth( 80 );
      sep.setControl( zoomLabel );
      swtToolbar.pack();
    } catch ( Throwable t ) {
      log.logError( Const.getStackTracker( t ) );
      new ErrorDialog( shell,
        BaseMessages.getString( PKG, "Spoon.Exception.ErrorReadingXULFile.Title" ),
        BaseMessages.getString( PKG, "Spoon.Exception.ErrorReadingXULFile.Message", XUL_FILE_JOB_GRAPH ),
        new Exception( t ) );
    }
  }

  /**
   * Allows for magnifying to any percentage entered by the user...
   */
  private void readMagnification() {
    float previousMagnification = magnification;
    String possibleText = zoomLabel.getText();
    possibleText = possibleText.replace( "%", "" );

    float possibleFloatMagnification;
    try {
      possibleFloatMagnification = Float.parseFloat( possibleText ) / 100;
      magnification = possibleFloatMagnification;
      if ( magnification < MIN_ZOOM || magnification > MAX_ZOOM ) {
        magnification = previousMagnification;
        log.logError( "Invalid zoom value: " + zoomLabel.getText() );
        throw new IllegalArgumentException( );
      }
      if ( zoomLabel.getText().indexOf( '%' ) < 0 ) {
        zoomLabel.setText( zoomLabel.getText().concat( "%" ) );
      }
    } catch ( Exception e ) {
      MessageBox mb = new MessageBox( shell, SWT.YES | SWT.ICON_ERROR );
      mb.setMessage( BaseMessages.getString( PKG, "TransGraph.Dialog.InvalidZoomMeasurement.Message", String.valueOf( (int) ( MIN_ZOOM * 100 ) ), String.valueOf( (int) MAX_ZOOM * 100 ) ) );
      mb.setText( BaseMessages.getString( PKG, "TransGraph.Dialog.InvalidZoomMeasurement.Title" ) );
      mb.open();
    }
    redraw();
  }

  public void keyPressed( KeyEvent e ) {

    // Delete
    if ( e.keyCode == SWT.DEL ) {
      List<JobEntryCopy> copies = jobMeta.getSelectedEntries();
      if ( copies != null && copies.size() > 0 ) {
        delSelected();
      }
    }

    if ( e.keyCode == SWT.F1 ) {
      spoon.browseVersionHistory();
    }

    // CTRL-UP : allignTop();
    if ( e.keyCode == SWT.ARROW_UP && ( e.stateMask & SWT.MOD1 ) != 0 ) {
      alligntop();
    }
    // CTRL-DOWN : allignBottom();
    if ( e.keyCode == SWT.ARROW_DOWN && ( e.stateMask & SWT.MOD1 ) != 0 ) {
      allignbottom();
    }
    // CTRL-LEFT : allignleft();
    if ( e.keyCode == SWT.ARROW_LEFT && ( e.stateMask & SWT.MOD1 ) != 0 ) {
      allignleft();
    }
    // CTRL-RIGHT : allignRight();
    if ( e.keyCode == SWT.ARROW_RIGHT && ( e.stateMask & SWT.MOD1 ) != 0 ) {
      allignright();
    }
    // ALT-RIGHT : distributeHorizontal();
    if ( e.keyCode == SWT.ARROW_RIGHT && ( e.stateMask & SWT.ALT ) != 0 ) {
      distributehorizontal();
    }
    // ALT-UP : distributeVertical();
    if ( e.keyCode == SWT.ARROW_UP && ( e.stateMask & SWT.ALT ) != 0 ) {
      distributevertical();
    }
    // ALT-HOME : snap to grid
    if ( e.keyCode == SWT.HOME && ( e.stateMask & SWT.ALT ) != 0 ) {
      snaptogrid( ConstUI.GRID_SIZE );
    }
    // CTRL-W or CTRL-F4 : close tab
    if ( ( e.keyCode == 'w' && ( e.stateMask & SWT.MOD1 ) != 0 )
      || ( e.keyCode == SWT.F4 && ( e.stateMask & SWT.MOD1 ) != 0 ) ) {
      spoon.tabCloseSelected();
    }
  }

  public void keyReleased( KeyEvent e ) {
  }

  public void selectInRect( JobMeta jobMeta, org.pentaho.di.core.gui.Rectangle rect ) {
    int i;
    for ( i = 0; i < jobMeta.nrJobEntries(); i++ ) {
      JobEntryCopy je = jobMeta.getJobEntry( i );
      Point p = je.getLocation();
      if ( ( ( p.x >= rect.x && p.x <= rect.x + rect.width ) || ( p.x >= rect.x + rect.width && p.x <= rect.x ) )
        && ( ( p.y >= rect.y && p.y <= rect.y + rect.height ) || ( p.y >= rect.y + rect.height && p.y <= rect.y ) ) ) {
        je.setSelected( true );
      }
    }
    for ( i = 0; i < jobMeta.nrNotes(); i++ ) {
      NotePadMeta ni = jobMeta.getNote( i );
      Point a = ni.getLocation();
      Point b = new Point( a.x + ni.width, a.y + ni.height );
      if ( rect.contains( a.x, a.y ) && rect.contains( b.x, b.y ) ) {
        ni.setSelected( true );
      }
    }
  }

  public boolean setFocus() {
    xulDomContainer.addEventHandler( this );
    return super.setFocus();
  }

  /**
   * Method gets called, when the user wants to change a job entries name and he indeed entered a different name then
   * the old one. Make sure that no other job entry matches this name and rename in case of uniqueness.
   *
   * @param jobEntry
   * @param newName
   */
  public void renameJobEntry( JobEntryCopy jobEntry, String newName ) {
    JobEntryCopy[] jobs = jobMeta.getAllJobGraphEntries( newName );
    if ( jobs != null && jobs.length > 0 ) {
      MessageBox mb = new MessageBox( shell, SWT.OK | SWT.ICON_INFORMATION );
      mb.setMessage( BaseMessages.getString( PKG, "Spoon.Dialog.JobEntryNameExists.Message", newName ) );
      mb.setText( BaseMessages.getString( PKG, "Spoon.Dialog.JobEntryNameExists.Title" ) );
      mb.open();
    } else {
      jobEntry.setName( newName );
      jobEntry.setChanged();
      spoon.refreshTree(); // to reflect the new name
      spoon.refreshGraph();
    }
  }

  public static void showOnlyStartOnceMessage( Shell shell ) {
    MessageBox mb = new MessageBox( shell, SWT.YES | SWT.ICON_ERROR );
    mb.setMessage( BaseMessages.getString( PKG, "JobGraph.Dialog.OnlyUseStartOnce.Message" ) );
    mb.setText( BaseMessages.getString( PKG, "JobGraph.Dialog.OnlyUseStartOnce.Title" ) );
    mb.open();
  }

  public void delSelected() {
    delSelected( getJobEntry() );
  }

  public void delSelected( JobEntryCopy clickedEntry ) {
    List<JobEntryCopy> copies = jobMeta.getSelectedEntries();
    int nrsels = copies.size();
    if ( nrsels == 0 ) {
      if ( clickedEntry != null ) {
        spoon.deleteJobEntryCopies( jobMeta, clickedEntry );
      }
      return;
    }

    JobEntryCopy[] jobEntries = copies.toArray( new JobEntryCopy[copies.size()] );
    spoon.deleteJobEntryCopies( jobMeta, jobEntries );
  }

  public void clearSettings() {
    selectedEntry = null;
    selectedNote = null;
    selectedEntries = null;
    selectedNotes = null;
    selectionRegion = null;
    hop_candidate = null;
    last_hop_split = null;
    lastButton = 0;
    startHopEntry = null;
    endHopEntry = null;
    iconoffset = null;
    for ( int i = 0; i < jobMeta.nrJobHops(); i++ ) {
      jobMeta.getJobHop( i ).setSplit( false );
    }

    stopEntryMouseOverDelayTimers();
  }

  public Point getRealPosition( Composite canvas, int x, int y ) {
    Point p = new Point( 0, 0 );
    Composite follow = canvas;
    while ( follow != null ) {
      Point xy = new Point( follow.getLocation().x, follow.getLocation().y );
      p.x += xy.x;
      p.y += xy.y;
      follow = follow.getParent();
    }

    p.x = x - p.x - 8;
    p.y = y - p.y - 48;

    return screen2real( p.x, p.y );
  }

  /**
   * See if location (x,y) is on a line between two steps: the hop!
   *
   * @param x
   * @param y
   * @return the transformation hop on the specified location, otherwise: null
   */
  private JobHopMeta findJobHop( int x, int y ) {
    return findHop( x, y, null );
  }

  /**
   * See if location (x,y) is on a line between two steps: the hop!
   *
   * @param x
   * @param y
   * @param exclude
   *          the step to exclude from the hops (from or to location). Specify null if no step is to be excluded.
   * @return the transformation hop on the specified location, otherwise: null
   */
  private JobHopMeta findHop( int x, int y, JobEntryCopy exclude ) {
    int i;
    JobHopMeta online = null;
    for ( i = 0; i < jobMeta.nrJobHops(); i++ ) {
      JobHopMeta hi = jobMeta.getJobHop( i );
      JobEntryCopy fs = hi.getFromEntry();
      JobEntryCopy ts = hi.getToEntry();

      if ( fs == null || ts == null ) {
        return null;
      }

      // If either the "from" or "to" step is excluded, skip this hop.
      //
      if ( exclude != null && ( exclude.equals( fs ) || exclude.equals( ts ) ) ) {
        continue;
      }

      int[] line = getLine( fs, ts );

      if ( pointOnLine( x, y, line ) ) {
        online = hi;
      }
    }
    return online;
  }

  protected int[] getLine( JobEntryCopy fs, JobEntryCopy ts ) {
    if ( fs == null || ts == null ) {
      return null;
    }

    Point from = fs.getLocation();
    Point to = ts.getLocation();
    offset = getOffset();

    int x1 = from.x + iconsize / 2;
    int y1 = from.y + iconsize / 2;

    int x2 = to.x + iconsize / 2;
    int y2 = to.y + iconsize / 2;

    return new int[] { x1, y1, x2, y2 };
  }

  private void showHelpTip( int x, int y, String tipTitle, String tipMessage ) {

    helpTip.setTitle( tipTitle );
    helpTip.setMessage( tipMessage );
    helpTip.setCheckBoxMessage( BaseMessages.getString(
      PKG, "JobGraph.HelpToolTip.DoNotShowAnyMoreCheckBox.Message" ) );
    // helpTip.hide();
    // int iconSize = spoon.props.getIconSize();
    org.eclipse.swt.graphics.Point location = new org.eclipse.swt.graphics.Point( x - 5, y - 5 );

    helpTip.show( location );
  }

  public void setJobEntry( JobEntryCopy jobEntry ) {
    this.jobEntry = jobEntry;
  }

  public JobEntryCopy getJobEntry() {
    return jobEntry;
  }

  public void openTransformation() {
    JobEntryCopy jobEntryCopy = getJobEntry();
    final JobEntryInterface entry = jobEntryCopy.getEntry();
    openTransformation( (JobEntryTrans) entry, jobEntryCopy );
  }

  public void openJob() {
    JobEntryCopy jobEntryCopy = getJobEntry();
    final JobEntryInterface entry = jobEntryCopy.getEntry();
    openJob( (JobEntryJob) entry, jobEntryCopy );
  }

  public void newHopClick() {
    selectedEntries = null;
    newHop();
  }

  public void editEntryClick() {
    selectedEntries = null;
    editEntry( getJobEntry() );
  }

  public void editEntryDescription() {
    String title = BaseMessages.getString( PKG, "JobGraph.Dialog.EditDescription.Title" );
    String message = BaseMessages.getString( PKG, "JobGraph.Dialog.EditDescription.Message" );
    EnterTextDialog dd = new EnterTextDialog( shell, title, message, getJobEntry().getDescription() );
    String des = dd.open();
    if ( des != null ) {
      jobEntry.setDescription( des );
      jobEntry.setChanged();
      spoon.setShellText();
    }
  }

  /**
   * Go from serial to parallel to serial execution
   */
  public void editEntryParallel() {

    JobEntryCopy je = getJobEntry();
    JobEntryCopy jeOld = (JobEntryCopy) je.clone_deep();

    je.setLaunchingInParallel( !je.isLaunchingInParallel() );
    JobEntryCopy jeNew = (JobEntryCopy) je.clone_deep();

    spoon.addUndoChange( jobMeta, new JobEntryCopy[] { jeOld }, new JobEntryCopy[] { jeNew }, new int[] { jobMeta
      .indexOfJobEntry( jeNew ) } );
    jobMeta.setChanged();

    if ( getJobEntry().isLaunchingInParallel() ) {
      // Show a warning (optional)
      //
      if ( "Y".equalsIgnoreCase( spoon.props.getCustomParameter( STRING_PARALLEL_WARNING_PARAMETER, "Y" ) ) ) {
        MessageDialogWithToggle md =
          new MessageDialogWithToggle( shell,
            BaseMessages.getString( PKG, "JobGraph.ParallelJobEntriesWarning.DialogTitle" ),
            null,
            BaseMessages.getString( PKG, "JobGraph.ParallelJobEntriesWarning.DialogMessage", Const.CR ) + Const.CR,
            MessageDialog.WARNING,
            new String[] { BaseMessages.getString( PKG, "JobGraph.ParallelJobEntriesWarning.Option1" ) },
            0,
            BaseMessages.getString( PKG, "JobGraph.ParallelJobEntriesWarning.Option2" ),
            "N".equalsIgnoreCase( spoon.props.getCustomParameter( STRING_PARALLEL_WARNING_PARAMETER, "Y" ) ) );
        MessageDialogWithToggle.setDefaultImage( GUIResource.getInstance().getImageSpoon() );
        md.open();
        spoon.props.setCustomParameter( STRING_PARALLEL_WARNING_PARAMETER, md.getToggleState() ? "N" : "Y" );
        spoon.props.saveProps();
      }
    }
    spoon.refreshGraph();

  }

  public void duplicateEntry() throws KettleException {
    if ( !canDup( jobEntry ) ) {
      JobGraph.showOnlyStartOnceMessage( spoon.getShell() );
    }

    spoon.delegates.jobs.dupeJobEntry( jobMeta, jobEntry );
  }

  public void copyEntry() {
    if ( RepositorySecurityUI.verifyOperations(
        shell, spoon.rep, RepositoryOperation.MODIFY_JOB, RepositoryOperation.EXECUTE_JOB ) ) {
      return;
    }
    List<JobEntryCopy> entries = jobMeta.getSelectedEntries();
    Iterator<JobEntryCopy> iterator = entries.iterator();
    while ( iterator.hasNext() ) {
      JobEntryCopy entry = iterator.next();
      if ( !canDup( entry ) ) {
        iterator.remove();
      }
    }

    spoon.delegates.jobs.copyJobEntries( entries );
  }

  private boolean canDup( JobEntryCopy entry ) {
    return !entry.isStart();
  }

  public void detachEntry() {
    detach( getJobEntry() );
    jobMeta.unselectAll();
  }

  public void hideEntry() {
    getJobEntry().setDrawn( false );
    // nr > 1: delete
    if ( jobEntry.getNr() > 0 ) {
      int ind = jobMeta.indexOfJobEntry( jobEntry );
      jobMeta.removeJobEntry( ind );
      spoon.addUndoDelete( jobMeta, new JobEntryCopy[] { getJobEntry() }, new int[] { ind } );
    }
    redraw();
  }

  public void deleteEntry() {
    delSelected();
    redraw();
  }

  protected synchronized void setMenu( int x, int y ) {

    currentMouseX = x;
    currentMouseY = y;

    final JobEntryCopy jobEntry = jobMeta.getJobEntryCopy( x, y, iconsize );
    setJobEntry( jobEntry );
    Document doc = xulDomContainer.getDocumentRoot();
    if ( jobEntry != null ) {
      // We clicked on a Job Entry!

      XulMenupopup menu = (XulMenupopup) doc.getElementById( "job-graph-entry" );
      if ( menu != null ) {
        List<JobEntryCopy> selection = jobMeta.getSelectedEntries();
        doRightClickSelection( jobEntry, selection );
        int sels = selection.size();

        XulMenuitem item = (XulMenuitem) doc.getElementById( "job-graph-entry-newhop" );
        item.setDisabled( sels < 2 );

        JfaceMenupopup launchMenu = (JfaceMenupopup) doc.getElementById( "job-graph-entry-launch-popup" );
        String[] referencedObjects = jobEntry.getEntry().getReferencedObjectDescriptions();
        boolean[] enabledObjects = jobEntry.getEntry().isReferencedObjectEnabled();
        launchMenu.setDisabled( Utils.isEmpty( referencedObjects ) );

        launchMenu.removeChildren();

        if ( !Utils.isEmpty( referencedObjects ) ) {
          for ( int i = 0; i < referencedObjects.length; i++ ) {
            final int index = i;
            String referencedObject = referencedObjects[i];
            Action action = new Action( referencedObject, Action.AS_PUSH_BUTTON ) {
              public void run() {
                loadReferencedObject( jobEntry, index );
              }
            };
            JfaceMenuitem child =
              new JfaceMenuitem( null, launchMenu, xulDomContainer, referencedObject, i, action );
            child.setLabel( referencedObject );
            child.setDisabled( !enabledObjects[i] );
          }
        }

        item = (XulMenuitem) doc.getElementById( "job-graph-entry-align-snap" );

        item.setAcceltext( "ALT-HOME" );
        item.setLabel( BaseMessages.getString( PKG, "JobGraph.PopupMenu.JobEntry.AllignDistribute.SnapToGrid" ) );
        item.setAccesskey( "alt-home" );

        XulMenu aMenu = (XulMenu) doc.getElementById( "job-graph-entry-align" );
        if ( aMenu != null ) {
          aMenu.setDisabled( sels < 1 );
        }

        item = (XulMenuitem) doc.getElementById( "job-graph-entry-detach" );
        if ( item != null ) {
          item.setDisabled( !jobMeta.isEntryUsedInHops( jobEntry ) );
        }

        item = (XulMenuitem) doc.getElementById( "job-graph-entry-hide" );
        if ( item != null ) {
          item.setDisabled( !( jobEntry.isDrawn() && !jobMeta.isEntryUsedInHops( jobEntry ) ) );
        }

        item = (XulMenuitem) doc.getElementById( "job-graph-entry-delete" );
        if ( item != null ) {
          item.setDisabled( !jobEntry.isDrawn() );
        }

        item = (XulMenuitem) doc.getElementById( "job-graph-entry-parallel" );
        if ( item != null ) {
          item.setSelected( jobEntry.isLaunchingInParallel() );
        }

        try {
          JobGraphJobEntryMenuExtension extension =
            new JobGraphJobEntryMenuExtension( xulDomContainer, doc, jobMeta, jobEntry, this );
          ExtensionPointHandler.callExtensionPoint(
            log, KettleExtensionPoint.JobGraphJobEntrySetMenu.id, extension );
        } catch ( Exception e ) {
          log.logError( "Error handling menu right click on job entry through extension point", e );
        }

        ConstUI.displayMenu( menu, canvas );
      }

    } else {
      // Clear the menu

      final JobHopMeta hi = findJobHop( x, y );
      setCurrentHop( hi );

      if ( hi != null ) {
        // We clicked on a HOP!

        XulMenupopup menu = (XulMenupopup) doc.getElementById( "job-graph-hop" );
        if ( menu != null ) {
          XulMenuitem miPopEvalUncond = (XulMenuitem) doc.getElementById( "job-graph-hop-evaluation-uncond" );
          XulMenuitem miPopEvalTrue = (XulMenuitem) doc.getElementById( "job-graph-hop-evaluation-true" );
          XulMenuitem miPopEvalFalse = (XulMenuitem) doc.getElementById( "job-graph-hop-evaluation-false" );
          XulMenuitem miDisHop = (XulMenuitem) doc.getElementById( "job-graph-hop-enabled" );
          XulMenuitem miFlipHop = (XulMenuitem) doc.getElementById( "job-graph-hop-flip" );

          // Set the checkboxes in the right places...
          //
          if ( miPopEvalUncond != null && miPopEvalTrue != null && miPopEvalFalse != null ) {
            if ( hi.isUnconditional() ) {
              miPopEvalUncond.setSelected( true );
              miPopEvalTrue.setSelected( false );
              miPopEvalFalse.setSelected( false );
            } else {
              if ( hi.getEvaluation() ) {
                miPopEvalUncond.setSelected( false );
                miPopEvalTrue.setSelected( true );
                miPopEvalFalse.setSelected( false );
              } else {
                miPopEvalUncond.setSelected( false );
                miPopEvalTrue.setSelected( false );
                miPopEvalFalse.setSelected( true );
              }
            }
            if ( !hi.getFromEntry().evaluates() ) {
              miPopEvalTrue.setDisabled( true );
              miPopEvalFalse.setDisabled( true );
            } else {
              miPopEvalTrue.setDisabled( false );
              miPopEvalFalse.setDisabled( false );
            }
            if ( !hi.getFromEntry().isUnconditional() ) {
              miPopEvalUncond.setDisabled( true );
            } else {
              miPopEvalUncond.setDisabled( false );
            }
            if ( hi.getFromEntry().isStart() ) {
              miFlipHop.setDisabled( true );
            } else {
              miFlipHop.setDisabled( false );
            }
          }

          if ( miDisHop != null ) {
            if ( hi.isEnabled() ) {
              miDisHop.setLabel( BaseMessages.getString( PKG, "JobGraph.PopupMenu.Hop.Disable" ) );
            } else {
              miDisHop.setLabel( BaseMessages.getString( PKG, "JobGraph.PopupMenu.Hop.Enable" ) );
            }
          }
          ConstUI.displayMenu( menu, canvas );
        }

      } else {
        // Clicked on the background: maybe we hit a note?
        final NotePadMeta ni = jobMeta.getNote( x, y );
        setCurrentNote( ni );
        if ( ni != null ) {
          XulMenupopup menu = (XulMenupopup) doc.getElementById( "job-graph-note" );
          if ( menu != null ) {
            ConstUI.displayMenu( menu, canvas );
          }
        } else {
          XulMenupopup menu = (XulMenupopup) doc.getElementById( "job-graph-background" );
          if ( menu != null ) {
            final String clipcontent = spoon.fromClipboard();
            XulMenuitem item = (XulMenuitem) doc.getElementById( "job-graph-note-paste" );
            if ( item != null ) {
              item.setDisabled( clipcontent == null );
            }

            ConstUI.displayMenu( menu, canvas );
          }
        }
      }
    }
  }

  public void selectAll() {
    spoon.editSelectAll();
  }

  public void clearSelection() {
    spoon.editUnselectAll();
  }

  public void editJobProperties() {
    editProperties( jobMeta, spoon, spoon.getRepository(), true );
  }

  public void pasteNote() {
    final String clipcontent = spoon.fromClipboard();
    Point loc = new Point( currentMouseX, currentMouseY );
    spoon.pasteXML( jobMeta, clipcontent, loc );
  }

  public void newNote() {
    String title = BaseMessages.getString( PKG, "JobGraph.Dialog.EditNote.Title" );
    NotePadDialog dd = new NotePadDialog( jobMeta, shell, title );
    NotePadMeta n = dd.open();
    if ( n != null ) {
      NotePadMeta npi =
          new NotePadMeta( n.getNote(), lastclick.x, lastclick.y, ConstUI.NOTE_MIN_SIZE, ConstUI.NOTE_MIN_SIZE, n
            .getFontName(), n.getFontSize(), n.isFontBold(), n.isFontItalic(), n.getFontColorRed(), n
            .getFontColorGreen(), n.getFontColorBlue(), n.getBackGroundColorRed(), n.getBackGroundColorGreen(), n
            .getBackGroundColorBlue(), n.getBorderColorRed(), n.getBorderColorGreen(), n.getBorderColorBlue(), n
            .isDrawShadow() );
      jobMeta.addNote( npi );
      spoon.addUndoNew( jobMeta, new NotePadMeta[] { npi }, new int[] { jobMeta.indexOfNote( npi ) } );
      redraw();
    }
  }

  public void setCurrentNote( NotePadMeta ni ) {
    this.ni = ni;
  }

  public NotePadMeta getCurrentNote() {
    return ni;
  }

  public void editNote() {
    selectionRegion = null;
    editNote( getCurrentNote() );
  }

  public void deleteNote() {
    selectionRegion = null;
    int idx = jobMeta.indexOfNote( getCurrentNote() );
    if ( idx >= 0 ) {
      jobMeta.removeNote( idx );
      spoon.addUndoDelete( jobMeta, new NotePadMeta[] { getCurrentNote() }, new int[] { idx } );
    }
    redraw();
  }

  public void raiseNote() {
    selectionRegion = null;
    int idx = jobMeta.indexOfNote( getCurrentNote() );
    if ( idx >= 0 ) {
      jobMeta.raiseNote( idx );
      // spoon.addUndoRaise(jobMeta, new NotePadMeta[] {getCurrentNote()}, new int[] {idx} );
    }
    redraw();
  }

  public void lowerNote() {
    selectionRegion = null;
    int idx = jobMeta.indexOfNote( getCurrentNote() );
    if ( idx >= 0 ) {
      jobMeta.lowerNote( idx );
      // spoon.addUndoLower(jobMeta, new NotePadMeta[] {getCurrentNote()}, new int[] {idx} );
    }
    redraw();
  }

  public void flipHop() {
    selectionRegion = null;
    JobEntryCopy origFrom = currentHop.getFromEntry();
    JobEntryCopy origTo = currentHop.getToEntry();
    currentHop.setFromEntry( currentHop.getToEntry() );
    currentHop.setToEntry( origFrom );

    boolean cancel = false;
    if ( jobMeta.hasLoop( currentHop.getToEntry() ) ) {
      MessageBox mb = new MessageBox( shell, SWT.OK | SWT.CANCEL | SWT.ICON_WARNING );
      mb.setMessage( BaseMessages.getString( PKG, "JobGraph.Dialog.HopFlipCausesLoop.Message" ) );
      mb.setText( BaseMessages.getString( PKG, "JobGraph.Dialog.HopCausesLoop.Title" ) );
      int choice = mb.open();
      if ( choice == SWT.CANCEL ) {
        cancel = true;
        currentHop.setFromEntry( origFrom );
        currentHop.setToEntry( origTo );
      }
    }
    if ( !cancel ) {
      currentHop.setChanged();
    }
    spoon.refreshGraph();
    spoon.refreshTree();
    spoon.setShellText();
  }

  public void disableHop() {
    selectionRegion = null;
    boolean orig = currentHop.isEnabled();
    currentHop.setEnabled( !currentHop.isEnabled() );

    if ( !orig && ( jobMeta.hasLoop( currentHop.getToEntry() ) ) ) {
      MessageBox mb = new MessageBox( shell, SWT.CANCEL | SWT.OK | SWT.ICON_WARNING );
      mb.setMessage( BaseMessages.getString( PKG, "JobGraph.Dialog.LoopAfterHopEnabled.Message" ) );
      mb.setText( BaseMessages.getString( PKG, "JobGraph.Dialog.LoopAfterHopEnabled.Title" ) );
      int choice = mb.open();
      if ( choice == SWT.CANCEL ) {
        currentHop.setEnabled( orig );
      }
    }
    spoon.refreshGraph();
    spoon.refreshTree();
  }

  public void deleteHop() {
    selectionRegion = null;
    int idx = jobMeta.indexOfJobHop( currentHop );
    jobMeta.removeJobHop( idx );
    spoon.refreshTree();
    spoon.refreshGraph();
  }

  public void setHopUnconditional() {
    currentHop.setUnconditional();
    spoon.refreshGraph();
  }

  public void setHopEvaluationTrue() {
    currentHop.setConditional();
    currentHop.setEvaluation( true );
    spoon.refreshGraph();
  }

  public void setHopEvaluationFalse() {
    currentHop.setConditional();
    currentHop.setEvaluation( false );
    spoon.refreshGraph();
  }

  protected void setCurrentHop( JobHopMeta hop ) {
    currentHop = hop;
  }

  protected JobHopMeta getCurrentHop() {
    return currentHop;
  }

  public void enableHopsBetweenSelectedEntries() {
    enableHopsBetweenSelectedEntries( true );
  }

  public void disableHopsBetweenSelectedEntries() {
    enableHopsBetweenSelectedEntries( false );
  }

  /**
   * This method enables or disables all the hops between the selected Entries.
   *
   **/
  public void enableHopsBetweenSelectedEntries( boolean enabled ) {
    List<JobEntryCopy> list = jobMeta.getSelectedEntries();

    boolean hasLoop = false;

    for ( int i = 0; i < jobMeta.nrJobHops(); i++ ) {
      JobHopMeta hop = jobMeta.getJobHop( i );
      if ( list.contains( hop.getFromEntry() ) && list.contains( hop.getToEntry() ) ) {

        JobHopMeta before = (JobHopMeta) hop.clone();
        hop.setEnabled( enabled );
        JobHopMeta after = (JobHopMeta) hop.clone();
        spoon.addUndoChange( jobMeta, new JobHopMeta[] { before }, new JobHopMeta[] { after }, new int[] { jobMeta
          .indexOfJobHop( hop ) } );
        if ( jobMeta.hasLoop( hop.getToEntry() ) ) {
          hasLoop = true;
        }
      }
    }

    if ( hasLoop && enabled ) {
      MessageBox mb = new MessageBox( shell, SWT.OK | SWT.ICON_WARNING );
      mb.setMessage( BaseMessages.getString( PKG, "JobGraph.Dialog.LoopAfterHopEnabled.Message" ) );
      mb.setText( BaseMessages.getString( PKG, "JobGraph.Dialog.LoopAfterHopEnabled.Title" ) );
      mb.open();
    }

    spoon.refreshGraph();
  }

  public void enableHopsDownstream() {
    enableDisableHopsDownstream( true );
  }

  public void disableHopsDownstream() {
    enableDisableHopsDownstream( false );
  }

  public void enableDisableHopsDownstream( boolean enabled ) {
    if ( currentHop == null ) {
      return;
    }
    JobHopMeta before = (JobHopMeta) currentHop.clone();
    currentHop.setEnabled( enabled );
    JobHopMeta after = (JobHopMeta) currentHop.clone();
    spoon.addUndoChange( jobMeta, new JobHopMeta[] { before }, new JobHopMeta[] { after }, new int[] { jobMeta
      .indexOfJobHop( currentHop ) } );

    Set<JobEntryCopy> checkedEntries = enableDisableNextHops( currentHop.getToEntry(), enabled, new HashSet<>() );

    if ( checkedEntries.stream().anyMatch( entry -> jobMeta.hasLoop( entry ) ) ) {
      MessageBox mb = new MessageBox( shell, SWT.OK | SWT.ICON_WARNING );
      mb.setMessage( BaseMessages.getString( PKG, "JobGraph.Dialog.LoopAfterHopEnabled.Message" ) );
      mb.setText( BaseMessages.getString( PKG, "JobGraph.Dialog.LoopAfterHopEnabled.Title" ) );
      mb.open();
    }

    spoon.refreshGraph();
  }

  private Set<JobEntryCopy> enableDisableNextHops( JobEntryCopy from, boolean enabled, Set<JobEntryCopy> checkedEntries ) {
    checkedEntries.add( from );
    jobMeta.getJobhops().stream()
            .filter( hop -> from.equals( hop.getFromEntry() ) )
            .forEach( hop -> {
              if ( hop.isEnabled() != enabled ) {
                JobHopMeta before = (JobHopMeta) hop.clone();
                hop.setEnabled( enabled );
                JobHopMeta after = (JobHopMeta) hop.clone();
                spoon.addUndoChange( jobMeta, new JobHopMeta[]{ before }, new JobHopMeta[]{ after }, new int[]{ jobMeta
                        .indexOfJobHop( hop ) } );
              }
              if ( !checkedEntries.contains( hop.getToEntry() ) ) {
                enableDisableNextHops( hop.getToEntry(), enabled, checkedEntries );
              }
            } );
    return checkedEntries;
  }

  protected void setToolTip( int x, int y, int screenX, int screenY ) {
    if ( !spoon.getProperties().showToolTips() ) {
      return;
    }

    canvas.setToolTipText( "-" ); // Some stupid bug in GTK+ causes a phantom tool tip to pop up, even if the tip is
                                  // null
    canvas.setToolTipText( null );

    Image tipImage = null;

    JobHopMeta hi = findJobHop( x, y );

    // check the area owner list...
    //
    StringBuilder tip = new StringBuilder();
    AreaOwner areaOwner = getVisibleAreaOwner( x, y );
    if ( areaOwner != null && areaOwner.getAreaType() != null ) {
      JobEntryCopy jobEntryCopy;
      switch ( areaOwner.getAreaType() ) {
        case JOB_HOP_ICON:
          hi = (JobHopMeta) areaOwner.getOwner();
          if ( hi.isUnconditional() ) {
            tipImage = GUIResource.getInstance().getImageUnconditionalHop();
            tip.append( BaseMessages.getString( PKG, "JobGraph.Hop.Tooltip.Unconditional", hi
              .getFromEntry().getName(), Const.CR ) );
          } else {
            if ( hi.getEvaluation() ) {
              tip.append( BaseMessages.getString( PKG, "JobGraph.Hop.Tooltip.EvaluatingTrue", hi
                .getFromEntry().getName(), Const.CR ) );
              tipImage = GUIResource.getInstance().getImageTrue();
            } else {
              tip.append( BaseMessages.getString( PKG, "JobGraph.Hop.Tooltip.EvaluatingFalse", hi
                .getFromEntry().getName(), Const.CR ) );
              tipImage = GUIResource.getInstance().getImageFalse();
            }
          }
          break;

        case JOB_HOP_PARALLEL_ICON:
          hi = (JobHopMeta) areaOwner.getOwner();
          tip.append( BaseMessages.getString(
            PKG, "JobGraph.Hop.Tooltip.Parallel", hi.getFromEntry().getName(), Const.CR ) );
          tipImage = GUIResource.getInstance().getImageParallelHop();
          break;

        case CUSTOM:
          String message = (String) areaOwner.getOwner();
          tip.append( message );
          tipImage = null;
          GUIResource.getInstance().getImageTransGraph();
          break;

        case JOB_ENTRY_MINI_ICON_INPUT:
          tip.append( BaseMessages.getString( PKG, "JobGraph.EntryInputConnector.Tooltip" ) );
          tipImage = GUIResource.getInstance().getImageHopInput();
          resetDelayTimer( (JobEntryCopy) areaOwner.getOwner() );
          break;

        case JOB_ENTRY_MINI_ICON_OUTPUT:
          tip.append( BaseMessages.getString( PKG, "JobGraph.EntryOutputConnector.Tooltip" ) );
          tipImage = GUIResource.getInstance().getImageHopOutput();
          resetDelayTimer( (JobEntryCopy) areaOwner.getOwner() );
          break;

        case JOB_ENTRY_MINI_ICON_EDIT:
          tip.append( BaseMessages.getString( PKG, "JobGraph.EditStep.Tooltip" ) );
          tipImage = GUIResource.getInstance().getImageEdit();
          resetDelayTimer( (JobEntryCopy) areaOwner.getOwner() );
          break;

        case JOB_ENTRY_MINI_ICON_CONTEXT:
          tip.append( BaseMessages.getString( PKG, "JobGraph.ShowMenu.Tooltip" ) );
          tipImage = GUIResource.getInstance().getImageContextMenu();
          resetDelayTimer( (JobEntryCopy) areaOwner.getOwner() );
          break;

        case JOB_ENTRY_RESULT_FAILURE:
        case JOB_ENTRY_RESULT_SUCCESS:
          JobEntryResult jobEntryResult = (JobEntryResult) areaOwner.getOwner();
          jobEntryCopy = (JobEntryCopy) areaOwner.getParent();
          Result result = jobEntryResult.getResult();
          tip.append( "'" ).append( jobEntryCopy.getName() ).append( "' " );
          if ( result.getResult() ) {
            tipImage = GUIResource.getInstance().getImageTrue();
            tip.append( "finished successfully." );
          } else {
            tipImage = GUIResource.getInstance().getImageFalse();
            tip.append( "failed." );
          }
          tip.append( Const.CR ).append( "------------------------" ).append( Const.CR ).append( Const.CR );
          tip.append( "Result         : " ).append( result.getResult() ).append( Const.CR );
          tip.append( "Errors         : " ).append( result.getNrErrors() ).append( Const.CR );

          if ( result.getNrLinesRead() > 0 ) {
            tip.append( "Lines read     : " ).append( result.getNrLinesRead() ).append( Const.CR );
          }
          if ( result.getNrLinesWritten() > 0 ) {
            tip.append( "Lines written  : " ).append( result.getNrLinesWritten() ).append( Const.CR );
          }
          if ( result.getNrLinesInput() > 0 ) {
            tip.append( "Lines input    : " ).append( result.getNrLinesInput() ).append( Const.CR );
          }
          if ( result.getNrLinesOutput() > 0 ) {
            tip.append( "Lines output   : " ).append( result.getNrLinesOutput() ).append( Const.CR );
          }
          if ( result.getNrLinesUpdated() > 0 ) {
            tip.append( "Lines updated  : " ).append( result.getNrLinesUpdated() ).append( Const.CR );
          }
          if ( result.getNrLinesDeleted() > 0 ) {
            tip.append( "Lines deleted  : " ).append( result.getNrLinesDeleted() ).append( Const.CR );
          }
          if ( result.getNrLinesRejected() > 0 ) {
            tip.append( "Lines rejected : " ).append( result.getNrLinesRejected() ).append( Const.CR );
          }
          if ( result.getResultFiles() != null && !result.getResultFiles().isEmpty() ) {
            tip.append( Const.CR ).append( "Result files:" ).append( Const.CR );
            if ( result.getResultFiles().size() > 10 ) {
              tip.append( " (10 files of " ).append( result.getResultFiles().size() ).append( " shown" );
            }
            List<ResultFile> files = new ArrayList<>( result.getResultFiles().values() );
            for ( int i = 0; i < files.size(); i++ ) {
              ResultFile file = files.get( i );
              tip.append( "  - " ).append( file.toString() ).append( Const.CR );
            }
          }
          if ( result.getRows() != null && !result.getRows().isEmpty() ) {
            tip.append( Const.CR ).append( "Result rows: " );
            if ( result.getRows().size() > 10 ) {
              tip.append( " (10 rows of " ).append( result.getRows().size() ).append( " shown" );
            }
            tip.append( Const.CR );
            for ( int i = 0; i < result.getRows().size() && i < 10; i++ ) {
              RowMetaAndData row = result.getRows().get( i );
              tip.append( "  - " ).append( row.toString() ).append( Const.CR );
            }
          }
          break;

        case JOB_ENTRY_RESULT_CHECKPOINT:
          tip.append( "The job started here since this is the furthest checkpoint "
            + "that was reached last time the transformation was executed." );
          tipImage = GUIResource.getInstance().getImageCheckpoint();
          break;
        case JOB_ENTRY_ICON:
          JobEntryCopy jec = (JobEntryCopy) areaOwner.getOwner();
          if ( jec.isDeprecated() ) { // only need tooltip if job entry is deprecated
            tip.append( BaseMessages.getString( PKG, "JobGraph.DeprecatedEntry.Tooltip.Title" ) ).append( Const.CR );
            String tipNext = BaseMessages.getString( PKG, "JobGraph.DeprecatedEntry.Tooltip.Message1", jec.getName() );
            int length = tipNext.length() + 5;
            for ( int i = 0; i < length; i++ ) {
              tip.append( "-" );
            }
            tip.append( Const.CR ).append( tipNext ).append( Const.CR );
            tip.append( BaseMessages.getString( PKG, "JobGraph.DeprecatedEntry.Tooltip.Message2" ) );
            if ( !Utils.isEmpty( jec.getSuggestion() )
              && !( jec.getSuggestion().startsWith( "!" ) && jec.getSuggestion().endsWith( "!" ) ) ) {
              tip.append( " " );
              tip.append( BaseMessages.getString( PKG, "JobGraph.DeprecatedEntry.Tooltip.Message3",
                jec.getSuggestion() ) );
            }
            tipImage = GUIResource.getInstance().getImageDeprecated();
          }
          break;
        default:
          break;
      }
    }

    if ( hi != null && tip.length() == 0 ) {
      // Set the tooltip for the hop:
      tip.append( BaseMessages.getString( PKG, "JobGraph.Dialog.HopInfo" ) ).append( Const.CR );
      tip.append( BaseMessages.getString( PKG, "JobGraph.Dialog.HopInfo.SourceEntry" ) ).append( " " ).append(
        hi.getFromEntry().getName() ).append( Const.CR );
      tip.append( BaseMessages.getString( PKG, "JobGraph.Dialog.HopInfo.TargetEntry" ) ).append( " " ).append(
        hi.getToEntry().getName() ).append( Const.CR );
      tip.append( BaseMessages.getString( PKG, "TransGraph.Dialog.HopInfo.Status" ) ).append( " " );
      tip.append( ( hi.isEnabled()
        ? BaseMessages.getString( PKG, "JobGraph.Dialog.HopInfo.Enable" ) : BaseMessages.getString(
          PKG, "JobGraph.Dialog.HopInfo.Disable" ) ) );
      if ( hi.isUnconditional() ) {
        tipImage = GUIResource.getInstance().getImageUnconditionalHop();
      } else {
        if ( hi.getEvaluation() ) {
          tipImage = GUIResource.getInstance().getImageTrue();
        } else {
          tipImage = GUIResource.getInstance().getImageFalse();
        }
      }
    }

    if ( tip == null || tip.length() == 0 ) {
      toolTip.hide();
    } else {
      if ( !tip.toString().equalsIgnoreCase( getToolTipText() ) ) {
        if ( tipImage != null ) {
          toolTip.setImage( tipImage );
        } else {
          toolTip.setImage( GUIResource.getInstance().getImageSpoon() );
        }
        toolTip.setText( tip.toString() );
        toolTip.hide();
        toolTip.show( new org.eclipse.swt.graphics.Point( screenX, screenY ) );
      }
    }
  }

  public void launchStuff( JobEntryCopy jobEntryCopy ) {
    String[] references = jobEntryCopy.getEntry().getReferencedObjectDescriptions();
    if ( !Utils.isEmpty( references ) ) {
      loadReferencedObject( jobEntryCopy, 0 );
    }

    /*
     * if (jobEntryCopy.isJob()) { final JobEntryJob entry = (JobEntryJob) jobEntryCopy.getEntry(); if ((entry != null
     * && entry.getJobObjectId() == null && !Utils.isEmpty(entry.getFilename()) && spoon.rep == null) || (entry != null
     * && !Utils.isEmpty(entry.getName()) && spoon.rep != null) || (entry != null && entry.getJobObjectId()!=null &&
     * spoon.rep != null) ) { openJob(entry, jobEntryCopy); } } else if (jobEntryCopy.isTransformation()) { final
     * JobEntryTrans entry = (JobEntryTrans) jobEntryCopy.getEntry(); if ((entry != null && entry.getTransObjectId() ==
     * null && !Utils.isEmpty(entry.getFilename()) && spoon.rep == null) || (entry != null && entry.getName() != null &&
     * spoon.rep != null) || (entry != null && entry.getTransObjectId()!=null && spoon.rep != null) ) {
     * openTransformation(entry, jobEntryCopy); } }
     */
  }

  public void launchStuff() {
    if ( jobEntry != null ) {
      launchStuff( jobEntry );
    }
  }

  protected void loadReferencedObject( JobEntryCopy jobEntryCopy, int index ) {
    try {
      Object referencedMeta =
          jobEntryCopy.getEntry().loadReferencedObject( index, spoon.rep, spoon.getMetaStore(), jobMeta );
      if ( referencedMeta == null ) {
        // Compatible re-try for older plugins.
        referencedMeta =
          compatibleJobEntryLoadReferencedObject( jobEntryCopy.getEntry(), index, spoon.rep, jobMeta );
      }
      if ( referencedMeta != null && ( referencedMeta instanceof TransMeta ) ) {
        TransMeta launchTransMeta = (TransMeta) referencedMeta;

        // Try to see if this transformation is already loaded in another tab...
        //
        TabMapEntry tabEntry = spoon.delegates.tabs.findTabForTransformation( launchTransMeta );
        if ( tabEntry != null ) {
          // Switch to this one!
          //
          spoon.tabfolder.setSelected( tabEntry.getTabItem() );
          return;
        }

        copyInternalJobVariables( jobMeta, launchTransMeta );
        spoon.setParametersAsVariablesInUI( launchTransMeta, launchTransMeta );

        launchTransMeta.clearChanged();
        spoon.addTransGraph( launchTransMeta );

        TransGraph transGraph = spoon.getActiveTransGraph();
        attachActiveTrans( transGraph, launchTransMeta, jobEntryCopy );

        spoon.refreshTree();
        spoon.applyVariables();
      }

      if ( referencedMeta != null && ( referencedMeta instanceof JobMeta ) ) {
        JobMeta launchJobMeta = (JobMeta) referencedMeta;

        // Try to see if this job is already loaded in another tab...
        //
        String tabName = spoon.delegates.tabs.makeTabName( launchJobMeta, true );
        TabMapEntry tabEntry = spoon.delegates.tabs.findTabMapEntry( tabName, ObjectType.JOB_GRAPH );
        if ( tabEntry != null ) {
          // Switch to this one!
          //
          spoon.tabfolder.setSelected( tabEntry.getTabItem() );
          return;
        }

        spoon.setParametersAsVariablesInUI( launchJobMeta, launchJobMeta );

        launchJobMeta.clearChanged();
        spoon.addJobGraph( launchJobMeta );

        JobGraph jobGraph = spoon.getActiveJobGraph();
        attachActiveJob( jobGraph, launchJobMeta, jobEntryCopy );

        spoon.refreshTree();
        spoon.applyVariables();
      }
    } catch ( Exception e ) {
      new ErrorDialog( shell,
        BaseMessages.getString( PKG, "JobGraph.Dialog.ErrorLaunchingSpoonCanNotLoadTransformation.Title" ),
        BaseMessages.getString( PKG, "JobGraph.Dialog.ErrorLaunchingSpoonCanNotLoadTransformation.Message" ), e );
    }
  }

  @SuppressWarnings( "deprecation" )
  private Object compatibleJobEntryLoadReferencedObject( JobEntryInterface entry, int index, Repository rep,
    JobMeta jobMeta2 ) throws KettleException {
    return entry.loadReferencedObject( index, spoon.rep, jobMeta );
  }

  protected void openTransformation( JobEntryTrans entry, JobEntryCopy jobEntryCopy ) {

    try {

      TransMeta launchTransMeta = null;

      switch ( entry.getSpecificationMethod() ) {
        case FILENAME:
          // See if this file is already loaded...
          //
          String exactFilename = jobMeta.environmentSubstitute( entry.getFilename() );
          if ( Utils.isEmpty( exactFilename ) ) {
            throw new Exception( BaseMessages.getString( PKG, "JobGraph.Exception.NoFilenameSpecified" ) );
          }

          // Open the file or create a new one!
          //
          if ( KettleVFS.fileExists( exactFilename ) ) {
            launchTransMeta = new TransMeta( exactFilename );
          } else {
            launchTransMeta = new TransMeta();
          }
          launchTransMeta.setFilename( exactFilename );
          break;

        case REPOSITORY_BY_NAME:
          String exactTransname = jobMeta.environmentSubstitute( entry.getTransname() );
          String exactDirectory = jobMeta.environmentSubstitute( entry.getDirectory() );
          if ( Utils.isEmpty( exactTransname ) ) {
            throw new Exception( BaseMessages.getString( PKG, "JobGraph.Exception.NoTransNameSpecified" ) );
          }
          if ( Utils.isEmpty( exactDirectory ) ) {
            throw new Exception( BaseMessages.getString( PKG, "JobGraph.Exception.NoTransDirectorySpecified" ) );
          }

          // Open the transformation or create a new one...

          // But first we look to see if the directory does exist
          RepositoryDirectoryInterface repositoryDirectoryInterface =
            spoon.rep.findDirectory( jobMeta.environmentSubstitute( entry.getDirectory() ) );
          if ( repositoryDirectoryInterface == null ) {
            throw new Exception( BaseMessages.getString( PKG, "JobGraph.Exception.DirectoryDoesNotExist", jobMeta
              .environmentSubstitute( entry.getDirectory() ) ) );
          }

          boolean exists = spoon.rep.getTransformationID( exactTransname, repositoryDirectoryInterface ) != null;
          if ( !exists ) {
            launchTransMeta = new TransMeta( null, exactTransname );
          } else {
            launchTransMeta =
              spoon.rep.loadTransformation( exactTransname, spoon.rep.findDirectory( jobMeta
                .environmentSubstitute( entry.getDirectory() ) ), null, true, null ); // reads last version
          }
          break;
        case REPOSITORY_BY_REFERENCE:
          if ( entry.getTransObjectId() == null ) {
            throw new Exception( BaseMessages.getString( PKG, "JobGraph.Exception.NoTransReferenceSpecified" ) );
          }
          launchTransMeta = spoon.rep.loadTransformation( entry.getTransObjectId(), null );
          break;
        default:
          break;
      }

      // If we didn't find a valid transformation, stop here...
      //
      if ( launchTransMeta == null ) {
        throw new Exception( BaseMessages.getString( PKG, "JobGraph.Exception.NoValidTransSpecified" ) );
      }

      launchTransMeta.setRepository( spoon.getRepository() );
      launchTransMeta.setMetaStore( spoon.getMetaStore() );

      // Try to see if this transformation is already loaded in another tab...
      //
      TabMapEntry tabEntry = spoon.delegates.tabs.findTabForTransformation( launchTransMeta );
      if ( tabEntry != null ) {
        // Switch to this one!
        //
        spoon.tabfolder.setSelected( tabEntry.getTabItem() );
        return;
      }

      copyInternalJobVariables( jobMeta, launchTransMeta );
      spoon.setParametersAsVariablesInUI( launchTransMeta, launchTransMeta );

      spoon.addTransGraph( launchTransMeta );
      launchTransMeta.clearChanged();

      TransGraph transGraph = spoon.getActiveTransGraph();
      attachActiveTrans( transGraph, launchTransMeta, jobEntryCopy );

      spoon.refreshTree();
      spoon.applyVariables();

    } catch ( Throwable e ) {
      new ErrorDialog( shell,
        BaseMessages.getString( PKG, "JobGraph.Dialog.ErrorLaunchingSpoonCanNotLoadTransformation.Title" ),
        BaseMessages.getString( PKG, "JobGraph.Dialog.ErrorLaunchingSpoonCanNotLoadTransformation.Message" ),
        (Exception) e );
    }
  }

  public void openJob( JobEntryJob entry, JobEntryCopy jobEntryCopy ) {

    try {

      JobMeta launchJobMeta = null;

      switch ( entry.getSpecificationMethod() ) {
        case FILENAME:
          // See if this file is already loaded...
          //
          String exactFilename = jobMeta.environmentSubstitute( entry.getFilename() );
          if ( Utils.isEmpty( exactFilename ) ) {
            throw new Exception( BaseMessages.getString( PKG, "JobGraph.Exception.NoFilenameSpecified" ) );
          }

          // Open the file or create a new one!
          //
          if ( KettleVFS.fileExists( exactFilename ) ) {
            launchJobMeta = new JobMeta( jobMeta, exactFilename, spoon.rep, spoon.getMetaStore(), null );
          } else {
            launchJobMeta = new JobMeta();
          }
          launchJobMeta.setFilename( exactFilename );
          break;

        case REPOSITORY_BY_NAME:
          String exactJobname = jobMeta.environmentSubstitute( entry.getJobName() );
          String exactDirectory = jobMeta.environmentSubstitute( entry.getDirectory() );
          if ( Utils.isEmpty( exactJobname ) ) {
            throw new Exception( BaseMessages.getString( PKG, "JobGraph.Exception.NoJobNameSpecified" ) );
          }
          if ( Utils.isEmpty( exactDirectory ) ) {
            throw new Exception( BaseMessages.getString( PKG, "JobGraph.Exception.NoJobDirectorySpecified" ) );
          }

          // Open the job or create a new one...
          //
          RepositoryDirectoryInterface repDir = spoon.rep.findDirectory( entry.getDirectory() );
          boolean exists = spoon.rep.exists( exactJobname, repDir, RepositoryObjectType.JOB );
          if ( !exists ) {
            launchJobMeta = new JobMeta();
            launchJobMeta.setName( exactJobname );
            launchJobMeta.setRepositoryDirectory( repDir );
          } else {
            // Always reads last revision
            launchJobMeta = spoon.rep.loadJob( exactJobname, repDir, null, null );
          }
          break;
        case REPOSITORY_BY_REFERENCE:
          if ( entry.getJobObjectId() == null ) {
            throw new Exception( BaseMessages.getString( PKG, "JobGraph.Exception.NoJobReferenceSpecified" ) );
          }
          // Always reads last revision
          launchJobMeta = spoon.rep.loadJob( entry.getJobObjectId(), null );
          break;
        default:
          break;
      }

      // If we didn't find a valid job, stop here...
      //
      if ( launchJobMeta == null ) {
        throw new Exception( BaseMessages.getString( PKG, "JobGraph.Exception.NoValidJobSpecified" ) );
      }

      launchJobMeta.setRepository( spoon.getRepository() );
      launchJobMeta.setMetaStore( spoon.getMetaStore() );

      // Try to see if this job is already loaded in another tab...
      //
      String tabName = spoon.delegates.tabs.makeTabName( launchJobMeta, true );
      TabMapEntry tabEntry = spoon.delegates.tabs.findTabMapEntry( tabName, ObjectType.JOB_GRAPH );
      if ( tabEntry != null ) {
        // Switch to this one!
        //
        spoon.tabfolder.setSelected( tabEntry.getTabItem() );
        return;
      }

      spoon.setParametersAsVariablesInUI( launchJobMeta, launchJobMeta );

      spoon.addJobGraph( launchJobMeta );
      launchJobMeta.clearChanged();

      JobGraph jobGraph = spoon.getActiveJobGraph();
      attachActiveJob( jobGraph, launchJobMeta, jobEntryCopy );

      spoon.refreshTree();
      spoon.applyVariables();

    } catch ( Throwable e ) {
      new ErrorDialog( shell,
        BaseMessages.getString( PKG, "JobGraph.Dialog.ErrorLaunchingChefCanNotLoadJob.Title" ),
        BaseMessages.getString( PKG, "JobGraph.Dialog.ErrorLaunchingChefCanNotLoadJob.Message" ), e );
    }
  }

  /**
   * Finds the last active transformation in the running job to the opened transMeta
   *
   * @param transGraph
   * @param jobEntryCopy
   */
  private void attachActiveTrans( TransGraph transGraph, TransMeta newTrans, JobEntryCopy jobEntryCopy ) {
    if ( job != null && transGraph != null ) {
      Trans trans = spoon.findActiveTrans( job, jobEntryCopy );
      transGraph.setTrans( trans );
      if ( !transGraph.isExecutionResultsPaneVisible() ) {
        transGraph.showExecutionResults();
      }
      transGraph.setControlStates();
    }
  }

  /**
   * Finds the last active job in the running job to the openened jobMeta
   *
   * @param jobGraph
   * @param newJob
   */
  private void attachActiveJob( JobGraph jobGraph, JobMeta newJobMeta, JobEntryCopy jobEntryCopy ) {
    if ( job != null && jobGraph != null ) {
      Job subJob = spoon.findActiveJob( job, jobEntryCopy );
      if ( subJob != null ) {
        jobGraph.setJob( subJob );
        jobGraph.jobGridDelegate.setJobTracker( subJob.getJobTracker() );
        if ( !jobGraph.isExecutionResultsPaneVisible() ) {
          jobGraph.showExecutionResults();
        }
        jobGraph.setControlStates();
      }
    }
  }

  public synchronized void setJob( Job job ) {
    this.job = job;
  }

  public static void copyInternalJobVariables( JobMeta sourceJobMeta, TransMeta targetTransMeta ) {
    // Also set some internal JOB variables...
    //
    String[] internalVariables = Const.INTERNAL_JOB_VARIABLES;

    for ( String variableName : internalVariables ) {
      targetTransMeta.setVariable( variableName, sourceJobMeta.getVariable( variableName ) );
    }
  }

  public void paintControl( PaintEvent e ) {
    Point area = getArea();
    if ( area.x == 0 || area.y == 0 ) {
      return; // nothing to do!
    }

    Display disp = shell.getDisplay();

    Image img = getJobImage( disp, area.x, area.y, magnification );
    e.gc.drawImage( img, 0, 0 );
    if ( jobMeta.nrJobEntries() == 0 ) {
      e.gc.setForeground( GUIResource.getInstance().getColorCrystalTextPentaho() );
      e.gc.setBackground( GUIResource.getInstance().getColorBackground() );
      e.gc.setFont( GUIResource.getInstance().getFontMedium() );

      Image pentahoImage = GUIResource.getInstance().getImageJobCanvas();
      int leftPosition = ( area.x - pentahoImage.getBounds().width ) / 2;
      int topPosition = ( area.y - pentahoImage.getBounds().height ) / 2;
      e.gc.drawImage( pentahoImage, leftPosition, topPosition );

    }
    img.dispose();

  }

  public Image getJobImage( Device device, int x, int y, float magnificationFactor ) {
    GCInterface gc = new SWTGC( device, new Point( x, y ), iconsize );

    int gridSize =
        PropsUI.getInstance().isShowCanvasGridEnabled() ? PropsUI.getInstance().getCanvasGridSize() : 1;

    JobPainter jobPainter =
      new JobPainter(
        gc, jobMeta, new Point( x, y ), new SwtScrollBar( hori ), new SwtScrollBar( vert ), hop_candidate,
        drop_candidate, selectionRegion, areaOwners, mouseOverEntries, PropsUI.getInstance().getIconSize(),
        PropsUI.getInstance().getLineWidth(), gridSize, PropsUI
          .getInstance().getShadowSize(), PropsUI.getInstance().isAntiAliasingEnabled(), PropsUI
          .getInstance().getNoteFont().getName(), PropsUI.getInstance().getNoteFont().getHeight() );

    jobPainter.setMagnification( magnificationFactor );
    jobPainter.setEntryLogMap( entryLogMap );
    jobPainter.setStartHopEntry( startHopEntry );
    jobPainter.setEndHopLocation( endHopLocation );
    jobPainter.setEndHopEntry( endHopEntry );
    jobPainter.setNoInputEntry( noInputEntry );
    if ( job != null ) {
      jobPainter.setJobEntryResults( job.getJobEntryResults() );
    } else {
      jobPainter.setJobEntryResults( new ArrayList<JobEntryResult>() );
    }

    List<JobEntryCopy> activeJobEntries = new ArrayList<>();
    if ( job != null ) {
      if ( job.getActiveJobEntryJobs().size() > 0 ) {
        activeJobEntries.addAll( job.getActiveJobEntryJobs().keySet() );
      }
      if ( job.getActiveJobEntryTransformations().size() > 0 ) {
        activeJobEntries.addAll( job.getActiveJobEntryTransformations().keySet() );
      }
    }
    jobPainter.setActiveJobEntries( activeJobEntries );

    jobPainter.drawJob();

    return (Image) gc.getImage();
  }

  protected Point getOffset() {
    Point area = getArea();
    Point max = jobMeta.getMaximum();
    Point thumb = getThumb( area, max );
    return getOffset( thumb, area );
  }

  protected void newHop() {
    List<JobEntryCopy> selection = jobMeta.getSelectedEntries();
    if ( selection == null || selection.size() < 2 ) {
      return;
    }
    JobEntryCopy fr = selection.get( 0 );
    JobEntryCopy to = selection.get( 1 );
    spoon.newJobHop( jobMeta, fr, to );
  }

  protected void editEntry( JobEntryCopy je ) {
    spoon.editJobEntry( jobMeta, je );
  }

  protected void editNote( NotePadMeta ni ) {
    NotePadMeta before = (NotePadMeta) ni.clone();
    String title = BaseMessages.getString( PKG, "JobGraph.Dialog.EditNote.Title" );

    NotePadDialog dd = new NotePadDialog( jobMeta, shell, title, ni );
    NotePadMeta n = dd.open();
    if ( n != null ) {
      ni.setChanged();
      ni.setNote( n.getNote() );
      ni.setFontName( n.getFontName() );
      ni.setFontSize( n.getFontSize() );
      ni.setFontBold( n.isFontBold() );
      ni.setFontItalic( n.isFontItalic() );
      // font color
      ni.setFontColorRed( n.getFontColorRed() );
      ni.setFontColorGreen( n.getFontColorGreen() );
      ni.setFontColorBlue( n.getFontColorBlue() );
      // background color
      ni.setBackGroundColorRed( n.getBackGroundColorRed() );
      ni.setBackGroundColorGreen( n.getBackGroundColorGreen() );
      ni.setBackGroundColorBlue( n.getBackGroundColorBlue() );
      // border color
      ni.setBorderColorRed( n.getBorderColorRed() );
      ni.setBorderColorGreen( n.getBorderColorGreen() );
      ni.setBorderColorBlue( n.getBorderColorBlue() );
      ni.setDrawShadow( n.isDrawShadow() );

      spoon.addUndoChange( jobMeta, new NotePadMeta[] { before }, new NotePadMeta[] { ni }, new int[] { jobMeta
        .indexOfNote( ni ) } );
      ni.width = ConstUI.NOTE_MIN_SIZE;
      ni.height = ConstUI.NOTE_MIN_SIZE;
      spoon.refreshGraph();
    }
  }

  protected void drawArrow( GC gc, int[] line ) {
    int mx, my;
    int x1 = line[0] + offset.x;
    int y1 = line[1] + offset.y;
    int x2 = line[2] + offset.x;
    int y2 = line[3] + offset.y;
    int x3;
    int y3;
    int x4;
    int y4;
    int a, b, dist;
    double factor;
    double angle;

    // gc.setLineWidth(1);
    // WuLine(gc, black, x1, y1, x2, y2);

    gc.drawLine( x1, y1, x2, y2 );

    // What's the distance between the 2 points?
    a = Math.abs( x2 - x1 );
    b = Math.abs( y2 - y1 );
    dist = (int) Math.sqrt( a * a + b * b );

    // determine factor (position of arrow to left side or right side 0-->100%)
    if ( dist >= 2 * iconsize ) {
      factor = 1.5;
    } else {
      factor = 1.2;
    }

    // in between 2 points
    mx = (int) ( x1 + factor * ( x2 - x1 ) / 2 );
    my = (int) ( y1 + factor * ( y2 - y1 ) / 2 );

    // calculate points for arrowhead
    angle = Math.atan2( y2 - y1, x2 - x1 ) + Math.PI;

    x3 = (int) ( mx + Math.cos( angle - theta ) * size );
    y3 = (int) ( my + Math.sin( angle - theta ) * size );

    x4 = (int) ( mx + Math.cos( angle + theta ) * size );
    y4 = (int) ( my + Math.sin( angle + theta ) * size );

    // draw arrowhead
    // gc.drawLine(mx, my, x3, y3);
    // gc.drawLine(mx, my, x4, y4);
    // gc.drawLine( x3, y3, x4, y4 );
    Color fore = gc.getForeground();
    Color back = gc.getBackground();
    gc.setBackground( fore );
    gc.fillPolygon( new int[] { mx, my, x3, y3, x4, y4 } );
    gc.setBackground( back );
  }

  protected boolean pointOnLine( int x, int y, int[] line ) {
    int dx, dy;
    int pm = HOP_SEL_MARGIN / 2;
    boolean retval = false;

    for ( dx = -pm; dx <= pm && !retval; dx++ ) {
      for ( dy = -pm; dy <= pm && !retval; dy++ ) {
        retval = pointOnThinLine( x + dx, y + dy, line );
      }
    }

    return retval;
  }

  protected boolean pointOnThinLine( int x, int y, int[] line ) {
    int x1 = line[0];
    int y1 = line[1];
    int x2 = line[2];
    int y2 = line[3];

    // Not in the square formed by these 2 points: ignore!
    //CHECKSTYLE:LineLength:OFF
    if ( !( ( ( x >= x1 && x <= x2 ) || ( x >= x2 && x <= x1 ) ) && ( ( y >= y1 && y <= y2 ) || ( y >= y2 && y <= y1 ) ) ) ) {
      return false;
    }

    double angle_line = Math.atan2( y2 - y1, x2 - x1 ) + Math.PI;
    double angle_point = Math.atan2( y - y1, x - x1 ) + Math.PI;

    // Same angle, or close enough?
    if ( angle_point >= angle_line - 0.01 && angle_point <= angle_line + 0.01 ) {
      return true;
    }

    return false;
  }

  protected SnapAllignDistribute createSnapAllignDistribute() {

    List<JobEntryCopy> elements = jobMeta.getSelectedEntries();
    int[] indices = jobMeta.getEntryIndexes( elements );
    return new SnapAllignDistribute( jobMeta, elements, indices, spoon, this );
  }

  public void snaptogrid() {
    snaptogrid( ConstUI.GRID_SIZE );
  }

  protected void snaptogrid( int size ) {
    createSnapAllignDistribute().snaptogrid( size );
  }

  public void allignleft() {
    createSnapAllignDistribute().allignleft();
  }

  public void allignright() {
    createSnapAllignDistribute().allignright();
  }

  public void alligntop() {
    createSnapAllignDistribute().alligntop();
  }

  public void allignbottom() {
    createSnapAllignDistribute().allignbottom();
  }

  public void distributehorizontal() {
    createSnapAllignDistribute().distributehorizontal();
  }

  public void distributevertical() {
    createSnapAllignDistribute().distributevertical();
  }

  protected void drawRect( GC gc, Rectangle rect ) {
    if ( rect == null ) {
      return;
    }

    gc.setLineStyle( SWT.LINE_DASHDOT );
    gc.setLineWidth( 1 );
    gc.setForeground( GUIResource.getInstance().getColorDarkGray() );
    // PDI-2619: SWT on Windows doesn't cater for negative rect.width/height so handle here.
    Point s = new Point( rect.x + offset.x, rect.y + offset.y );
    if ( rect.width < 0 ) {
      s.x = s.x + rect.width;
    }
    if ( rect.height < 0 ) {
      s.y = s.y + rect.height;
    }
    gc.drawRoundRectangle( s.x, s.y, Math.abs( rect.width ), Math.abs( rect.height ), 3, 3 );
    gc.setLineStyle( SWT.LINE_SOLID );
  }

  protected void detach( JobEntryCopy je ) {
    JobHopMeta hfrom = jobMeta.findJobHopTo( je );
    JobHopMeta hto = jobMeta.findJobHopFrom( je );

    if ( hfrom != null && hto != null ) {
      if ( jobMeta.findJobHop( hfrom.getFromEntry(), hto.getToEntry() ) == null ) {
        JobHopMeta hnew = new JobHopMeta( hfrom.getFromEntry(), hto.getToEntry() );
        jobMeta.addJobHop( hnew );
        spoon.addUndoNew( jobMeta, new JobHopMeta[] { (JobHopMeta) hnew.clone() }, new int[] { jobMeta
          .indexOfJobHop( hnew ) } );
      }
    }
    if ( hfrom != null ) {
      int fromidx = jobMeta.indexOfJobHop( hfrom );
      if ( fromidx >= 0 ) {
        jobMeta.removeJobHop( fromidx );
        spoon.addUndoDelete( jobMeta, new JobHopMeta[] { hfrom }, new int[] { fromidx } );
      }
    }
    if ( hto != null ) {
      int toidx = jobMeta.indexOfJobHop( hto );
      if ( toidx >= 0 ) {
        jobMeta.removeJobHop( toidx );
        spoon.addUndoDelete( jobMeta, new JobHopMeta[] { hto }, new int[] { toidx } );
      }
    }
    spoon.refreshTree();
    redraw();
  }

  public void newProps() {
    iconsize = spoon.props.getIconSize();
    linewidth = spoon.props.getLineWidth();
  }

  public String toString() {
    if ( jobMeta == null ) {
      return Spoon.APP_NAME;
    } else {
      return jobMeta.getName();
    }
  }

  public EngineMetaInterface getMeta() {
    return jobMeta;
  }

  /**
   * @return the jobMeta / public JobMeta getJobMeta() { return jobMeta; }
   *
   *         /**
   * @param jobMeta
   *          the jobMeta to set
   */
  public void setJobMeta( JobMeta jobMeta ) {
    this.jobMeta = jobMeta;
  }

  public boolean applyChanges() throws KettleException {
    return spoon.saveToFile( jobMeta );
  }

  public boolean canBeClosed() {
    return !jobMeta.hasChanged();
  }

  public JobMeta getManagedObject() {
    return jobMeta;
  }

  public boolean hasContentChanged() {
    return jobMeta.hasChanged();
  }

  public static int showChangedWarning( Shell shell, String name ) {
    MessageBox mb = new MessageBox( shell, SWT.YES | SWT.NO | SWT.CANCEL | SWT.ICON_WARNING );
    mb.setMessage( BaseMessages.getString( PKG, "JobGraph.Dialog.PromptSave.Message", name ) );
    mb.setText( BaseMessages.getString( PKG, "JobGraph.Dialog.PromptSave.Title" ) );
    return mb.open();
  }

  public static boolean editProperties( JobMeta jobMeta, Spoon spoon, Repository rep, boolean allowDirectoryChange ) {
    if ( jobMeta == null ) {
      return false;
    }

    JobDialog jd = new JobDialog( spoon.getShell(), SWT.NONE, jobMeta, rep );
    jd.setDirectoryChangeAllowed( allowDirectoryChange );
    JobMeta ji = jd.open();

    // In this case, load shared objects
    //
    if ( jd.isSharedObjectsFileChanged() ) {
      try {
        SharedObjects sharedObjects =
          rep != null ? rep.readJobMetaSharedObjects( jobMeta ) : jobMeta.readSharedObjects();
        spoon.sharedObjectsFileMap.put( sharedObjects.getFilename(), sharedObjects );
      } catch ( Exception e ) {
        new ErrorDialog( spoon.getShell(),
          BaseMessages.getString( PKG, "Spoon.Dialog.ErrorReadingSharedObjects.Title" ),
          BaseMessages.getString( PKG, "Spoon.Dialog.ErrorReadingSharedObjects.Message", spoon.delegates.tabs.makeTabName( jobMeta, true ) ), e );
      }
    }

    // If we added properties, add them to the variables too, so that they appear in the CTRL-SPACE variable completion.
    //
    spoon.setParametersAsVariablesInUI( jobMeta, jobMeta );

    if ( jd.isSharedObjectsFileChanged() || ji != null ) {
      spoon.refreshTree();
      spoon.delegates.tabs.renameTabs(); // cheap operation, might as will do it anyway
    }

    spoon.setShellText();
    return ji != null;
  }

  /**
   * @return the lastMove
   */
  public Point getLastMove() {
    return lastMove;
  }

  /**
   * @param lastMove
   *          the lastMove to set
   */
  public void setLastMove( Point lastMove ) {
    this.lastMove = lastMove;
  }

  /**
   * Add an extra view to the main composite SashForm
   */
  public void addExtraView() {
    extraViewComposite = new Composite( sashForm, SWT.NONE );
    FormLayout extraCompositeFormLayout = new FormLayout();
    extraCompositeFormLayout.marginWidth = 2;
    extraCompositeFormLayout.marginHeight = 2;
    extraViewComposite.setLayout( extraCompositeFormLayout );

    // Put a close and max button to the upper right corner...
    //
    closeButton = new Label( extraViewComposite, SWT.NONE );
    closeButton.setImage( GUIResource.getInstance().getImageClosePanel() );
    closeButton
      .setToolTipText( BaseMessages.getString( PKG, "JobGraph.ExecutionResultsPanel.CloseButton.Tooltip" ) );
    FormData fdClose = new FormData();
    fdClose.right = new FormAttachment( 100, 0 );
    fdClose.top = new FormAttachment( 0, 0 );
    closeButton.setLayoutData( fdClose );
    closeButton.addMouseListener( new MouseAdapter() {
      public void mouseDown( MouseEvent e ) {
        disposeExtraView();
      }
    } );

    minMaxButton = new Label( extraViewComposite, SWT.NONE );
    minMaxButton.setImage( GUIResource.getInstance().getImageMaximizePanel() );
    minMaxButton
      .setToolTipText( BaseMessages.getString( PKG, "JobGraph.ExecutionResultsPanel.MaxButton.Tooltip" ) );
    FormData fdMinMax = new FormData();
    fdMinMax.right = new FormAttachment( closeButton, -Const.MARGIN );
    fdMinMax.top = new FormAttachment( 0, 0 );
    minMaxButton.setLayoutData( fdMinMax );
    minMaxButton.addMouseListener( new MouseAdapter() {
      public void mouseDown( MouseEvent e ) {
        minMaxExtraView();
      }
    } );

    // Add a label at the top: Results
    //
    Label wResultsLabel = new Label( extraViewComposite, SWT.LEFT );
    wResultsLabel.setFont( GUIResource.getInstance().getFontMediumBold() );
    wResultsLabel.setBackground( GUIResource.getInstance().getColorWhite() );
    wResultsLabel.setText( BaseMessages.getString( PKG, "JobLog.ResultsPanel.NameLabel" ) );
    FormData fdResultsLabel = new FormData();
    fdResultsLabel.left = new FormAttachment( 0, 0 );
    fdResultsLabel.right = new FormAttachment( 100, 0 );
    fdResultsLabel.top = new FormAttachment( 0, 0 );
    wResultsLabel.setLayoutData( fdResultsLabel );

    // Add a tab folder ...
    //
    extraViewTabFolder = new CTabFolder( extraViewComposite, SWT.MULTI );
    spoon.props.setLook( extraViewTabFolder, Props.WIDGET_STYLE_TAB );

    extraViewTabFolder.addMouseListener( new MouseAdapter() {

      @Override
      public void mouseDoubleClick( MouseEvent arg0 ) {
        if ( sashForm.getMaximizedControl() == null ) {
          sashForm.setMaximizedControl( extraViewComposite );
        } else {
          sashForm.setMaximizedControl( null );
        }
      }

    } );

    FormData fdTabFolder = new FormData();
    fdTabFolder.left = new FormAttachment( 0, 0 );
    fdTabFolder.right = new FormAttachment( 100, 0 );
    fdTabFolder.top = new FormAttachment( wResultsLabel, Const.MARGIN );
    fdTabFolder.bottom = new FormAttachment( 100, 0 );
    extraViewTabFolder.setLayoutData( fdTabFolder );

    sashForm.setWeights( new int[] { 60, 40, } );
  }

  /**
   * If the extra tab view at the bottom is empty, we close it.
   */
  public void checkEmptyExtraView() {
    if ( extraViewTabFolder.getItemCount() == 0 ) {
      disposeExtraView();
    }
  }

  private void disposeExtraView() {
    extraViewComposite.dispose();
    sashForm.layout();
    sashForm.setWeights( new int[] { 100, } );

    XulToolbarbutton button = (XulToolbarbutton) toolbar.getElementById( "job-show-results" );
    button.setTooltiptext( BaseMessages.getString( PKG, "Spoon.Tooltip.ShowExecutionResults" ) );
    ToolItem swtToolItem = (ToolItem) button.getManagedObject();
    swtToolItem.setImage( GUIResource.getInstance().getImageShowResults() );
  }

  private void minMaxExtraView() {
    // What is the state?
    //
    boolean maximized = sashForm.getMaximizedControl() != null;
    if ( maximized ) {
      // Minimize
      //
      sashForm.setMaximizedControl( null );
      minMaxButton.setImage( GUIResource.getInstance().getImageMaximizePanel() );
      minMaxButton.setToolTipText( BaseMessages
        .getString( PKG, "JobGraph.ExecutionResultsPanel.MaxButton.Tooltip" ) );
    } else {
      // Maximize
      //
      sashForm.setMaximizedControl( extraViewComposite );
      minMaxButton.setImage( GUIResource.getInstance().getImageMinimizePanel() );
      minMaxButton.setToolTipText( BaseMessages
        .getString( PKG, "JobGraph.ExecutionResultsPanel.MinButton.Tooltip" ) );
    }
  }

  public boolean isExecutionResultsPaneVisible() {
    return extraViewComposite != null && !extraViewComposite.isDisposed();
  }

  public void showExecutionResults() {
    if ( isExecutionResultsPaneVisible() ) {
      disposeExtraView();
    } else {
      addAllTabs();
    }
  }

  public void addAllTabs() {

    CTabItem tabItemSelection = null;
    if ( extraViewTabFolder != null && !extraViewTabFolder.isDisposed() ) {
      tabItemSelection = extraViewTabFolder.getSelection();
    }

    jobHistoryDelegate.addJobHistory();
    jobLogDelegate.addJobLog();
    jobGridDelegate.addJobGrid();
    jobMetricsDelegate.addJobMetrics();

    if ( tabItemSelection != null ) {
      extraViewTabFolder.setSelection( tabItemSelection );
    } else {
      extraViewTabFolder.setSelection( jobGridDelegate.getJobGridTab() );
    }

    XulToolbarbutton button = (XulToolbarbutton) toolbar.getElementById( "job-show-results" );
    button.setTooltiptext( BaseMessages.getString( PKG, "Spoon.Tooltip.HideExecutionResults" ) );
    ToolItem swtToolItem = (ToolItem) button.getManagedObject();
    swtToolItem.setImage( GUIResource.getInstance().getImageHideResults() );
  }

  public void openFile() {
    spoon.openFile();
  }

  public void saveFile() throws KettleException {
    spoon.saveFile();
  }

  public void saveFileAs() throws KettleException {
    spoon.saveFileAs();
  }

  public void saveXMLFileToVfs() {
    spoon.saveXMLFileToVfs();
  }

  public void printFile() {
    spoon.printFile();
  }

  public void runJob() {
    spoon.runFile();
  }

  public void runOptionsJob() {
    spoon.runOptionsFile();
  }

  public void getSQL() {
    spoon.getSQL();
  }

  public XulToolbar getToolbar() {
    return toolbar;
  }

  public void exploreDatabase() {
    spoon.exploreDatabase();
  }

  public void browseVersionHistory() {
    try {
      RepositoryRevisionBrowserDialogInterface dialog =
        RepositoryExplorerDialog.getVersionBrowserDialog( shell, spoon.rep, jobMeta );
      String versionLabel = dialog.open();
      if ( versionLabel != null ) {
        spoon.loadObjectFromRepository( jobMeta.getName(), jobMeta.getRepositoryElementType(), jobMeta
          .getRepositoryDirectory(), versionLabel );
      }
    } catch ( Exception e ) {
      new ErrorDialog(
        shell, BaseMessages.getString( PKG, "JobGraph.VersionBrowserException.Title" ), BaseMessages.getString(
          PKG, "JobGraph.VersionBrowserException.Message" ), e );
    }
  }

  public synchronized void startJob( JobExecutionConfiguration executionConfiguration ) throws KettleException {

    // If the job is not running, start the transformation...
    //
    if ( job == null || ( job.isFinished() || job.isStopped() ) && !job.isActive() ) {
      // Auto save feature...
      //
      handleJobMetaChanges( jobMeta );

      // Is the repository available & name / id set?
      // Is there a filename set and no repository available?
      //
      if ( ( ( jobMeta.getName() != null && jobMeta.getObjectId() != null && spoon.rep != null ) || ( jobMeta
        .getFilename() != null && spoon.rep == null ) )
        && !jobMeta.hasChanged() // Didn't change
      ) {
        if ( job == null || ( job != null && !job.isActive() ) ) {
          try {

            // Make sure we clear the log before executing again...
            //
            if ( executionConfiguration.isClearingLog() ) {
              jobLogDelegate.clearLog();
            }

            // Also make sure to clear the old log entries in the central log
            // store & registry
            //
            if ( job != null ) {
              KettleLogStore.discardLines( job.getLogChannelId(), true );
            }

            JobMeta runJobMeta;

            if ( spoon.rep != null ) {
              runJobMeta = spoon.rep.loadJob( jobMeta.getName(), jobMeta.getRepositoryDirectory(), null, null );
            } else {
              runJobMeta = new JobMeta( null, jobMeta.getFilename(), null, jobMeta.getMetaStore(), null );
            }

            String spoonObjectId = UUID.randomUUID().toString();
            SimpleLoggingObject spoonLoggingObject =
              new SimpleLoggingObject( "SPOON", LoggingObjectType.SPOON, null );
            spoonLoggingObject.setContainerObjectId( spoonObjectId );
            spoonLoggingObject.setLogLevel( executionConfiguration.getLogLevel() );
            job = new Job( spoon.rep, runJobMeta, spoonLoggingObject );

            job.setLogLevel( executionConfiguration.getLogLevel() );
            job.shareVariablesWith( jobMeta );
            job.setInteractive( true );
            job.setGatheringMetrics( executionConfiguration.isGatheringMetrics() );
            job.setArguments( executionConfiguration.getArgumentStrings() );

            // Pass specific extension points...
            //
            job.getExtensionDataMap().putAll( executionConfiguration.getExtensionOptions() );

            // Add job entry listeners
            //
            job.addJobEntryListener( createRefreshJobEntryListener() );

            // If there is an alternative start job entry, pass it to the job
            //
            if ( !Utils.isEmpty( executionConfiguration.getStartCopyName() ) ) {
              JobEntryCopy startJobEntryCopy =
                runJobMeta.findJobEntry( executionConfiguration.getStartCopyName(), executionConfiguration
                  .getStartCopyNr(), false );
              job.setStartJobEntryCopy( startJobEntryCopy );
            }

            // Set the named parameters
            Map<String, String> paramMap = executionConfiguration.getParams();
            Set<String> keys = paramMap.keySet();
            for ( String key : keys ) {
              job.getJobMeta().setParameterValue( key, Const.NVL( paramMap.get( key ), "" ) );
            }
            job.getJobMeta().activateParameters();

            log.logMinimal( BaseMessages.getString( PKG, "JobLog.Log.StartingJob" ) );
            job.start();
            jobGridDelegate.previousNrItems = -1;
            // Link to the new jobTracker!
            jobGridDelegate.jobTracker = job.getJobTracker();

            // Attach a listener to notify us that the transformation has
            // finished.
            job.addJobListener( new JobAdapter() {
              public void jobFinished( Job job ) {
                JobGraph.this.jobFinished();
              }
            } );

            // Show the execution results views
            //
            addAllTabs();
          } catch ( KettleException e ) {
            new ErrorDialog(
              shell, BaseMessages.getString( PKG, "JobLog.Dialog.CanNotOpenJob.Title" ), BaseMessages.getString(
                PKG, "JobLog.Dialog.CanNotOpenJob.Message" ), e );
            job = null;
          }
        } else {
          MessageBox m = new MessageBox( shell, SWT.OK | SWT.ICON_WARNING );
          m.setText( BaseMessages.getString( PKG, "JobLog.Dialog.JobIsAlreadyRunning.Title" ) );
          m.setMessage( BaseMessages.getString( PKG, "JobLog.Dialog.JobIsAlreadyRunning.Message" ) );
          m.open();
        }
      } else {
        if ( jobMeta.hasChanged() ) {
          showSaveFileMessage();
        } else if ( spoon.rep != null && jobMeta.getName() == null ) {
          MessageBox m = new MessageBox( shell, SWT.OK | SWT.ICON_WARNING );
          m.setText( BaseMessages.getString( PKG, "JobLog.Dialog.PleaseGiveThisJobAName.Title" ) );
          m.setMessage( BaseMessages.getString( PKG, "JobLog.Dialog.PleaseGiveThisJobAName.Message" ) );
          m.open();
        } else {
          MessageBox m = new MessageBox( shell, SWT.OK | SWT.ICON_WARNING );
          m.setText( BaseMessages.getString( PKG, "JobLog.Dialog.NoFilenameSaveYourJobFirst.Title" ) );
          m.setMessage( BaseMessages.getString( PKG, "JobLog.Dialog.NoFilenameSaveYourJobFirst.Message" ) );
          m.open();
        }
      }
      setControlStates();
    }
  }

  public void showSaveFileMessage() {
    MessageBox m = new MessageBox( shell, SWT.OK | SWT.ICON_WARNING );
    m.setText( BaseMessages.getString( PKG, "JobLog.Dialog.JobHasChangedSave.Title" ) );
    m.setMessage( BaseMessages.getString( PKG, "JobLog.Dialog.JobHasChangedSave.Message" ) );
    m.open();
  }

  private JobEntryListener createRefreshJobEntryListener() {
    return new JobEntryListener() {

      public void beforeExecution( Job job, JobEntryCopy jobEntryCopy, JobEntryInterface jobEntryInterface ) {
        asyncRedraw();
      }

      public void afterExecution( Job job, JobEntryCopy jobEntryCopy, JobEntryInterface jobEntryInterface,
        Result result ) {
        asyncRedraw();
      }
    };
  }

  /**
   * This gets called at the very end, when everything is done.
   */
  protected void jobFinished() {
    // Do a final check to see if it all ended...
    //
    if ( job != null && job.isInitialized() && job.isFinished() ) {
      for ( RefreshListener listener : refreshListeners ) {
        listener.refreshNeeded();
      }
      jobMetricsDelegate.resetLastRefreshTime();
      jobMetricsDelegate.updateGraph();
      log.logMinimal( BaseMessages.getString( PKG, "JobLog.Log.JobHasEnded" ) );
    }
    setControlStates();
  }

  public synchronized void stopJob() {
    if ( job != null && job.isActive() && job.isInitialized() ) {
      job.stopAll();
      job.waitUntilFinished( 5000 ); // wait until everything is stopped, maximum 5 seconds...

      log.logMinimal( BaseMessages.getString( PKG, "JobLog.Log.JobWasStopped" ) );
    }
    setControlStates();
  }

  private boolean controlDisposed( XulToolbarbutton button ) {
    if ( button.getManagedObject() instanceof Widget ) {
      Widget widget = (Widget) button.getManagedObject();
      return widget.isDisposed();
    }
    return false;
  }

  public void setControlStates() {
    if ( isDisposed() || getDisplay().isDisposed() ) {
      return;
    }

    getDisplay().asyncExec( new Runnable() {

      public void run() {
        boolean operationsNotAllowed = false;
        try {
          operationsNotAllowed = RepositorySecurityUI.verifyOperations( shell, spoon.rep, false,
              RepositoryOperation.EXECUTE_JOB  );
        } catch ( KettleRepositoryLostException krle ) {
          log.logError( krle.getLocalizedMessage() );
          spoon.handleRepositoryLost( krle );
        }

        // Start/Run button...
        //
        boolean running = job != null && job.isActive();
        XulToolbarbutton runButton = (XulToolbarbutton) toolbar.getElementById( "job-run" );
        if ( runButton != null && !controlDisposed( runButton )  && !operationsNotAllowed ) {
          if ( runButton.isDisabled() ^ running ) {
            runButton.setDisabled( running );
          }
        }

        // Stop button...
        //
        XulToolbarbutton stopButton = (XulToolbarbutton) toolbar.getElementById( "job-stop" );
        if ( stopButton != null && !controlDisposed( stopButton ) ) {
          if ( stopButton.isDisabled() ^ !running ) {
            stopButton.setDisabled( !running );
          }
        }

        // Replay button...
        //
        XulToolbarbutton replayButton = (XulToolbarbutton) toolbar.getElementById( "job-replay" );
        if ( replayButton != null && !controlDisposed( replayButton ) && !operationsNotAllowed ) {
          if ( replayButton.isDisabled() ^ running ) {
            replayButton.setDisabled( running );
          }
        }

        // version browser button...
        //
        XulToolbarbutton versionsButton = (XulToolbarbutton) toolbar.getElementById( "browse-versions" );
        if ( versionsButton != null && !controlDisposed( versionsButton ) ) {
          boolean hasRepository = spoon.rep != null;
          boolean enabled =
            hasRepository && spoon.rep.getRepositoryMeta().getRepositoryCapabilities().supportsRevisions();
          if ( versionsButton.isDisabled() ^ !enabled ) {
            versionsButton.setDisabled( !enabled );
          }
        }
      }

    } );

  }

  /**
   * @return the refresh listeners
   */
  public List<RefreshListener> getRefreshListeners() {
    return refreshListeners;
  }

  /**
   * @param refreshListeners
   *          the refresh listeners to set
   */
  public void setRefreshListeners( List<RefreshListener> refreshListeners ) {
    this.refreshListeners = refreshListeners;
  }

  /**
   * @param refreshListener
   *          the job refresh listener to add
   */
  public void addRefreshListener( RefreshListener refreshListener ) {
    refreshListeners.add( refreshListener );
  }

  public String getName() {
    return "jobgraph";
  }

  public XulDomContainer getXulDomContainer() {
    return xulDomContainer;
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
    this.xulDomContainer = xulDomContainer;
  }

  public boolean canHandleSave() {
    return true;
  }

  public HasLogChannelInterface getLogChannelProvider() {
    return new HasLogChannelInterface() {
      @Override
      public LogChannelInterface getLogChannel() {
        return getJob() != null ? getJob().getLogChannel() : getJobMeta().getLogChannel();
      }
    };
  }

  // Change of step, connection, hop or note...
  public void addUndoPosition( Object[] obj, int[] pos, Point[] prev, Point[] curr ) {
    addUndoPosition( obj, pos, prev, curr, false );
  }

  // Change of step, connection, hop or note...
  public void addUndoPosition( Object[] obj, int[] pos, Point[] prev, Point[] curr, boolean nextAlso ) {
    // It's better to store the indexes of the objects, not the objects itself!
    jobMeta.addUndo( obj, null, pos, prev, curr, TransMeta.TYPE_UNDO_POSITION, nextAlso );
    spoon.setUndoMenu( jobMeta );
  }

  @Override
  public int showChangedWarning() throws KettleException {
    return showChangedWarning( jobMeta.getName() );
  }

  public void replayJob() {
    List<JobEntryCopy> selectedEntries = jobMeta.getSelectedEntries();
    if ( selectedEntries.size() != 1 ) {
      MessageBox box = new MessageBox( shell, SWT.ICON_INFORMATION | SWT.CLOSE );
      box.setText( BaseMessages.getString( PKG, "JobGraph.ReplayJob.SelectOneEntryToStartFrom.Title" ) );
      box.setMessage( BaseMessages.getString( PKG, "JobGraph.ReplayJob.SelectOneEntryToStartFrom.Message" ) );
      box.open();
      return;
    }

    JobEntryCopy copy = selectedEntries.get( 0 );

    spoon.executeJob( jobMeta, true, false, null, false, copy.getName(), copy.getNr() );
  }

  public void handleJobMetaChanges( JobMeta jobMeta ) throws KettleException {
    if ( jobMeta.hasChanged() ) {
      if ( spoon.props.getAutoSave() ) {
        if ( log.isDetailed() ) {
          log.logDetailed( BaseMessages.getString( PKG, "JobLog.Log.AutoSaveFileBeforeRunning" ) );
        }
        spoon.saveToFile( jobMeta );
      } else {
        MessageDialogWithToggle md =
          new MessageDialogWithToggle(
            shell, BaseMessages.getString( PKG, "JobLog.Dialog.SaveChangedFile.Title" ), null, BaseMessages
              .getString( PKG, "JobLog.Dialog.SaveChangedFile.Message" )
              + Const.CR
              + BaseMessages.getString( PKG, "JobLog.Dialog.SaveChangedFile.Message2" )
              + Const.CR,
            MessageDialog.QUESTION,
            new String[] {
              BaseMessages.getString( PKG, "System.Button.Yes" ),
              BaseMessages.getString( PKG, "System.Button.No" ) },
            0, BaseMessages.getString( PKG, "JobLog.Dialog.SaveChangedFile.Toggle" ), spoon.props.getAutoSave() );
        int answer = md.open();
        if ( ( answer & 0xFF ) == 0 ) {
          spoon.saveToFile( jobMeta );
        }
        spoon.props.setAutoSave( md.getToggleState() );
      }
    }
  }

  private JobEntryCopy lastChained = null;

  public void addJobEntryToChain( String typeDesc, boolean shift ) {
    JobMeta jobMeta = spoon.getActiveJob();
    if ( jobMeta == null ) {
      return;
    }

    //Is the lastChained entry still valid?
    //
    if ( lastChained != null && jobMeta.findJobEntry( lastChained.getName(), lastChained.getNr(), false ) == null ) {
      lastChained = null;
    }

    // If there is exactly one selected step, pick that one as last chained.
    //
    List<JobEntryCopy> sel = jobMeta.getSelectedEntries();
    if ( sel.size() == 1 ) {
      lastChained = sel.get( 0 );
    }

    // Where do we add this?

    Point p = null;
    if ( lastChained == null ) {
      p = jobMeta.getMaximum();
      p.x -= 100;
    } else {
      p = new Point( lastChained.getLocation().x, lastChained.getLocation().y );
    }

    p.x += 200;

    // Which is the new entry?

    JobEntryCopy newEntry = spoon.newJobEntry( jobMeta, typeDesc, false );
    if ( newEntry == null ) {
      return;
    }
    newEntry.setLocation( p.x, p.y );
    newEntry.setDrawn();

    if ( lastChained != null ) {
      spoon.newJobHop( jobMeta, lastChained, newEntry );
    }

    lastChained = newEntry;
    spoon.refreshGraph();
    spoon.refreshTree();

    if ( shift ) {
      editEntry( newEntry );
    }

    jobMeta.unselectAll();
    newEntry.setSelected( true );
  }

  public Spoon getSpoon() {
    return spoon;
  }

  public void setSpoon( Spoon spoon ) {
    this.spoon = spoon;
  }

  public JobMeta getJobMeta() {
    return jobMeta;
  }

  public Job getJob() {
    return job;
  }
}
