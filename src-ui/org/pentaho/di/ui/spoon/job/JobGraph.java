/* Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
* This software was developed by Pentaho Corporation and is provided under the terms 
* of the GNU Lesser General Public License, Version 2.1. You may not use 
* this file except in compliance with the license. If you need a copy of the license, 
* please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
* Data Integration.  The Initial Developer is Pentaho Corporation.
*
* Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
* basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
* the license for the specific language governing your rights and limitations.*/

package org.pentaho.di.ui.spoon.job;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
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
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseWheelListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.LineAttributes;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Transform;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.EngineMetaInterface;
import org.pentaho.di.core.NotePadMeta;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.dnd.DragAndDropContainer;
import org.pentaho.di.core.dnd.XMLTransfer;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleJobException;
import org.pentaho.di.core.gui.GUIPositionInterface;
import org.pentaho.di.core.gui.Point;
import org.pentaho.di.core.gui.Redrawable;
import org.pentaho.di.core.gui.SnapAllignDistribute;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.i18n.LanguageChoice;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobEntryType;
import org.pentaho.di.job.JobExecutionConfiguration;
import org.pentaho.di.job.JobHopMeta;
import org.pentaho.di.job.JobListener;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entries.job.JobEntryJob;
import org.pentaho.di.job.entries.trans.JobEntryTrans;
import org.pentaho.di.job.entry.JobEntryCopy;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.shared.SharedObjects;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.ui.core.ConstUI;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.dialog.EnterTextDialog;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.widget.CheckBoxToolTip;
import org.pentaho.di.ui.core.widget.CheckBoxToolTipListener;
import org.pentaho.di.ui.job.dialog.JobDialog;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.ui.spoon.TabItemInterface;
import org.pentaho.di.ui.spoon.TabMapEntry;
import org.pentaho.di.ui.spoon.TransPainter;
import org.pentaho.di.ui.spoon.dialog.DeleteMessageBox;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.XulLoader;
import org.pentaho.ui.xul.components.XulMenuitem;
import org.pentaho.ui.xul.components.XulToolbarbutton;
import org.pentaho.ui.xul.containers.XulMenu;
import org.pentaho.ui.xul.containers.XulMenupopup;
import org.pentaho.ui.xul.containers.XulToolbar;
import org.pentaho.ui.xul.dom.Document;
import org.pentaho.ui.xul.impl.XulEventHandler;
import org.pentaho.ui.xul.swt.SwtXulLoader;
import org.pentaho.xul.swt.tab.TabItem;

/**
 * Handles the display of Jobs in Spoon, in a graphical form.
 * 
 * @author Matt
 * Created on 17-may-2003
 *
 */
public class JobGraph extends Composite implements XulEventHandler, Redrawable, TabItemInterface {
  public static final String START_TEXT = Messages.getString("JobLog.Button.Start"); //$NON-NLS-1$
  public static final String STOP_TEXT = Messages.getString("JobLog.Button.Stop"); //$NON-NLS-1$

  private static final String XUL_FILE_JOB_TOOLBAR = "ui/job-toolbar.xul";
  private static final String STRING_PARALLEL_WARNING_PARAMETER = "ParallelJobEntriesWarning";
  private static final int HOP_SEL_MARGIN = 9;

  protected Shell shell;

  protected Canvas canvas;

  protected LogWriter log;

  protected JobMeta jobMeta;

  private Job job;

  protected int iconsize;

  protected int linewidth;

  protected Point lastclick;

  protected JobEntryCopy selected_entries[];

  protected JobEntryCopy selected_icon;

  protected Point prev_locations[];

  protected NotePadMeta selected_note;

  protected Point previous_note_location;

  protected Point lastMove;

  protected JobHopMeta hop_candidate;

  protected Point drop_candidate;

  protected Spoon spoon;

  protected Point offset, iconoffset, noteoffset;

  protected ScrollBar hori;

  protected ScrollBar vert;

  protected boolean split_hop;

  protected int last_button;

  protected JobHopMeta last_hop_split;

  protected Rectangle selrect;

  protected static final double theta = Math.toRadians(10); // arrowhead sharpness

  protected static final int size = 30; // arrowhead length

  protected int shadowsize;

  protected Map<String, XulMenupopup> menuMap = new HashMap<String, XulMenupopup>();

  protected int currentMouseX = 0;

  protected int currentMouseY = 0;

  protected JobEntryCopy jobEntry;

  protected NotePadMeta ni = null;

  protected JobHopMeta currentHop;

  private SashForm sashForm;

  public Composite extraViewComposite;

  public CTabFolder extraViewTabFolder;

  private XulToolbar toolbar;

  private float magnification = 1.0f;

  public JobLogDelegate jobLogDelegate;

  public JobHistoryDelegate jobHistoryDelegate;

  public JobGridDelegate jobGridDelegate;

  private boolean running;

  private Composite mainComposite;

  private List<RefreshListener> refreshListeners;

  private Label closeButton;

  private Label minMaxButton;

  private Combo zoomLabel;

  private int gridSize;

  private float translationX;

  private float translationY;

  private boolean shadow;
  
  private CheckBoxToolTip helpTip;

  public JobGraph(Composite par, final Spoon spoon, final JobMeta jobMeta) {
    super(par, SWT.NONE);
    shell = par.getShell();
    this.log = LogWriter.getInstance();
    this.spoon = spoon;
    this.jobMeta = jobMeta;
    // this.props = Props.getInstance();

    jobLogDelegate = new JobLogDelegate(spoon, this);
    jobHistoryDelegate = new JobHistoryDelegate(spoon, this);
    jobGridDelegate = new JobGridDelegate(spoon, this);

    refreshListeners = new ArrayList<RefreshListener>();

    setLayout(new FormLayout());

    // Add a tool-bar at the top of the tab
    // The form-data is set on the native widget automatically
    //
    addToolBar();

    // The main composite contains the graph view, but if needed also 
    // a view with an extra tab containing log, etc.
    //
    mainComposite = new Composite(this, SWT.NONE);
    mainComposite.setLayout(new FillLayout());
    
    // Nick's fix below -------
    Control toolbarControl = (Control) toolbar.getManagedObject();
    
    toolbarControl.setLayoutData(new FormData());
    toolbarControl.setParent(this);
    // ------------------------

    FormData fdMainComposite = new FormData();
    fdMainComposite.left = new FormAttachment(0, 0);
    fdMainComposite.top = new FormAttachment((Control) toolbar.getManagedObject(), 0);
    fdMainComposite.right = new FormAttachment(100, 0);
    fdMainComposite.bottom = new FormAttachment(100, 0);
    mainComposite.setLayoutData(fdMainComposite);

    // To allow for a splitter later on, we will add the splitter here...
    //
    sashForm = new SashForm(mainComposite, SWT.VERTICAL);
    
    // Add a canvas below it, use up all space initially
    //
    canvas = new Canvas(sashForm, SWT.V_SCROLL | SWT.H_SCROLL | SWT.NO_BACKGROUND | SWT.BORDER);

   
    sashForm.setWeights(new int[] { 100, });

    try {
      Document doc = spoon.getMainSpoonContainer().getDocumentRoot();
      menuMap.put("job-graph-hop", (XulMenupopup) doc.getElementById("job-graph-hop"));
      menuMap.put("job-graph-note", (XulMenupopup) doc.getElementById("job-graph-note"));
      menuMap.put("job-graph-background", (XulMenupopup) doc.getElementById("job-graph-background"));
      menuMap.put("job-graph-entry", (XulMenupopup) doc.getElementById("job-graph-entry"));
    } catch (Throwable t) {
      log.logError(toString(), Const.getStackTracker(t));
      new ErrorDialog(shell, Messages.getString("JobGraph.Exception.ErrorReadingXULFile.Title"), 
    		  Messages.getString("JobGraph.Exception.ErrorReadingXULFile.Message", Spoon.XUL_FILE_MENUS), new Exception(t));
    }
    

    helpTip = new CheckBoxToolTip(canvas);
    helpTip.addCheckBoxToolTipListener(new CheckBoxToolTipListener() {

      public void checkBoxSelected(boolean enabled) {
        spoon.props.setShowingHelpToolTips(enabled);
      }
    });


    newProps();

    selrect = null;
    hop_candidate = null;
    last_hop_split = null;

    selected_entries = null;
    selected_note = null;

    hori = canvas.getHorizontalBar();
    vert = canvas.getVerticalBar();

    hori.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        redraw();
      }
    });
    vert.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        redraw();
      }
    });
    hori.setThumb(100);
    vert.setThumb(100);

    hori.setVisible(true);
    vert.setVisible(true);

    setVisible(true);

    canvas.addPaintListener(new PaintListener() {
      public void paintControl(PaintEvent e) {
        JobGraph.this.paintControl(e);
      }
    });

    canvas.addMouseWheelListener(new MouseWheelListener() {

      public void mouseScrolled(MouseEvent e) {
        if (e.count == 3) {
          // scroll up
          zoomIn();
        } else if (e.count == -3) {
          // scroll down 
          zoomOut();
        }

      }
    });

    selected_entries = null;
    lastclick = null;

    canvas.addMouseListener(new MouseAdapter() {
      public void mouseDoubleClick(MouseEvent e) {
        clearSettings();

        Point real = screen2real(e.x, e.y);

        JobEntryCopy jobentry = jobMeta.getJobEntryCopy(real.x, real.y, iconsize);
        if (jobentry != null) {
          if (e.button == 1) {
            editEntry(jobentry);
          } else // open tab in Spoon 
          {
            launchStuff(jobentry);
          }
        } else {
          // Check if point lies on one of the many hop-lines...
          JobHopMeta online = findJobHop(real.x, real.y);
          if (online != null) {
            // editJobHop(online);
          } else {
            NotePadMeta ni = jobMeta.getNote(real.x, real.y);
            if (ni != null) {
              editNote(ni);
            }
          }

        }
      }

      public void mouseDown(MouseEvent e) {
        clearSettings();

        last_button = e.button;
        Point real = screen2real(e.x, e.y);
        lastclick = new Point(real.x, real.y);

        // Clear the tooltip!
        if (spoon.getProperties().showToolTips())
          setToolTipText(null);

        // Set the pop-up menu
        if (e.button == 3) {
          setMenu(real.x, real.y);
          return;
        }

        JobEntryCopy je = jobMeta.getJobEntryCopy(real.x, real.y, iconsize);
        if (je != null) {
          selected_entries = jobMeta.getSelectedEntries();
          selected_icon = je;
          // make sure this is correct!!!
          // When an icon is moved that is not selected, it gets selected too late.
          // It is not captured here, but in the mouseMoveListener...
          prev_locations = jobMeta.getSelectedLocations();

          Point p = je.getLocation();
          iconoffset = new Point(real.x - p.x, real.y - p.y);
        } else {
          // Dit we hit a note?
          NotePadMeta ni = jobMeta.getNote(real.x, real.y);
          if (ni != null && last_button == 1) {
            selected_note = ni;
            Point loc = ni.getLocation();
            previous_note_location = new Point(loc.x, loc.y);
            noteoffset = new Point(real.x - loc.x, real.y - loc.y);
            // System.out.println("We hit a note!!");
          } else {
            selrect = new Rectangle(real.x, real.y, 0, 0);
          }
        }
        redraw();
      }

      public void mouseUp(MouseEvent e) {
        boolean control = (e.stateMask & SWT.CONTROL) != 0;

        if (iconoffset == null)
          iconoffset = new Point(0, 0);
        Point real = screen2real(e.x, e.y);
        Point icon = new Point(real.x - iconoffset.x, real.y - iconoffset.y);

        // See if we need to add a hop...
        if (hop_candidate != null) {
          // hop doesn't already exist
          if (jobMeta.findJobHop(hop_candidate.from_entry, hop_candidate.to_entry) == null) {
            if (!hop_candidate.from_entry.evaluates() && hop_candidate.from_entry.isUnconditional()) {
              hop_candidate.setUnconditional();
            } else {
              hop_candidate.setConditional();
              int nr = jobMeta.findNrNextJobEntries(hop_candidate.from_entry);

              // If there is one green link: make this one red! (or vice-versa)
              if (nr == 1) {
                JobEntryCopy jge = jobMeta.findNextJobEntry(hop_candidate.from_entry, 0);
                JobHopMeta other = jobMeta.findJobHop(hop_candidate.from_entry, jge);
                if (other != null) {
                  hop_candidate.setEvaluation(!other.getEvaluation());
                }
              }
            }

            jobMeta.addJobHop(hop_candidate);
            spoon.addUndoNew(jobMeta, new JobHopMeta[] { hop_candidate }, new int[] { jobMeta
                .indexOfJobHop(hop_candidate) });
            spoon.refreshTree();
          }
          hop_candidate = null;
          selected_entries = null;
          last_button = 0;
          redraw();
        }

        // Did we select a region on the screen?  
        else if (selrect != null) {
          selrect.width = real.x - selrect.x;
          selrect.height = real.y - selrect.y;

          jobMeta.unselectAll();
          selectInRect(jobMeta, selrect);
          selrect = null;
          redraw();
        }

        // Clicked on an icon?
        //
        else if (selected_icon != null) {
          if (e.button == 1) {
            if (lastclick.x == real.x && lastclick.y == real.y) {
              // Flip selection when control is pressed!
              if (control) {
                selected_icon.flipSelected();
              } else {
                // Otherwise, select only the icon clicked on!
                jobMeta.unselectAll();
                selected_icon.setSelected(true);
              }
            } else // We moved around some items: store undo info...
            if (selected_entries != null && prev_locations != null) {
              int indexes[] = jobMeta.getEntryIndexes(selected_entries);
              spoon.addUndoPosition(jobMeta, selected_entries, indexes, prev_locations, jobMeta.getSelectedLocations());
            }
          }

          // OK, we moved the step, did we move it across a hop?
          // If so, ask to split the hop!
          if (split_hop) {
            JobHopMeta hi = findJobHop(icon.x + iconsize / 2, icon.y + iconsize / 2);
            if (hi != null) {
              int id = 0;
              if (!spoon.props.getAutoSplit()) {
                MessageDialogWithToggle md = new MessageDialogWithToggle(shell, Messages
                    .getString("JobGraph.Dialog.SplitHop.Title"), null, Messages
                    .getString("JobGraph.Dialog.SplitHop.Message")
                    + Const.CR + hi.from_entry.getName() + " --> " + hi.to_entry.getName(), MessageDialog.QUESTION,
                    new String[] { Messages.getString("System.Button.Yes"), Messages.getString("System.Button.No") },
                    0, Messages.getString("JobGraph.Dialog.SplitHop.Toggle"), spoon.props.getAutoSplit());
                MessageDialogWithToggle.setDefaultImage(GUIResource.getInstance().getImageSpoon());
                id = md.open();
                spoon.props.setAutoSplit(md.getToggleState());
              }

              if ((id & 0xFF) == 0) {
                JobHopMeta newhop1 = new JobHopMeta(hi.from_entry, selected_icon);
                jobMeta.addJobHop(newhop1);
                JobHopMeta newhop2 = new JobHopMeta(selected_icon, hi.to_entry);
                jobMeta.addJobHop(newhop2);
                if (!selected_icon.evaluates())
                  newhop2.setUnconditional();

                spoon.addUndoNew(jobMeta,
                    new JobHopMeta[] { (JobHopMeta) newhop1.clone(), (JobHopMeta) newhop2.clone() }, new int[] {
                        jobMeta.indexOfJobHop(newhop1), jobMeta.indexOfJobHop(newhop2) });
                int idx = jobMeta.indexOfJobHop(hi);
                spoon.addUndoDelete(jobMeta, new JobHopMeta[] { (JobHopMeta) hi.clone() }, new int[] { idx });
                jobMeta.removeJobHop(idx);
                spoon.refreshTree();

              }
            }
            split_hop = false;
          }

          selected_entries = null;
          redraw();
        }

        // Notes?
        else if (selected_note != null) {
          Point note = new Point(real.x - noteoffset.x, real.y - noteoffset.y);
          if (last_button == 1) {
            if (lastclick.x != real.x || lastclick.y != real.y) {
              int indexes[] = new int[] { jobMeta.indexOfNote(selected_note) };
              spoon.addUndoPosition(jobMeta, new NotePadMeta[] { selected_note }, indexes,
                  new Point[] { previous_note_location }, new Point[] { note });
            }
          }
          selected_note = null;
        }
      }
    });

    canvas.addMouseMoveListener(new MouseMoveListener() {
      public void mouseMove(MouseEvent e) {
        boolean shift = (e.stateMask & SWT.SHIFT) != 0;

        // Remember the last position of the mouse for paste with keyboard
        lastMove = new Point(e.x, e.y);

        if (iconoffset == null)
          iconoffset = new Point(0, 0);
        Point real = screen2real(e.x, e.y);
        Point icon = new Point(real.x - iconoffset.x, real.y - iconoffset.y);

        setToolTip(real.x, real.y, e.x, e.y);

        // First see if the icon we clicked on was selected.
        // If the icon was not selected, we should unselect all other icons,
        // selected and move only the one icon
        if (selected_icon != null && !selected_icon.isSelected()) {
          jobMeta.unselectAll();
          selected_icon.setSelected(true);
          selected_entries = new JobEntryCopy[] { selected_icon };
          prev_locations = new Point[] { selected_icon.getLocation() };
        }

        // Did we select a region...?
        if (selrect != null) {
          selrect.width = real.x - selrect.x;
          selrect.height = real.y - selrect.y;
          redraw();
        } else

        // Or just one entry on the screen?
        if (selected_entries != null) {
          if (last_button == 1 && !shift) {
            /*
             * One or more icons are selected and moved around...
             * 
             * new : new position of the ICON (not the mouse pointer)
             * dx  : difference with previous position
             */
            int dx = icon.x - selected_icon.getLocation().x;
            int dy = icon.y - selected_icon.getLocation().y;

            JobHopMeta hi = findJobHop(icon.x + iconsize / 2, icon.y + iconsize / 2);
            if (hi != null) {
              //log.logBasic("MouseMove", "Split hop candidate B!");
              if (!jobMeta.isEntryUsedInHops(selected_icon)) {
                //log.logBasic("MouseMove", "Split hop candidate A!");
                split_hop = true;
                last_hop_split = hi;
                hi.setSplit(true);
              }
            } else {
              if (last_hop_split != null) {
                last_hop_split.setSplit(false);
                last_hop_split = null;
                split_hop = false;
              }
            }

            //
            // One or more job entries are being moved around!
            //
            for (int i = 0; i < jobMeta.nrJobEntries(); i++) {
              JobEntryCopy je = jobMeta.getJobEntry(i);
              if (je.isSelected()) {
                PropsUI.setLocation(je, je.getLocation().x + dx, je.getLocation().y + dy);
              }
            }

            redraw();
          } else
          //	The middle button perhaps?
          if (last_button == 2 || (last_button == 1 && shift)) {
            JobEntryCopy si = jobMeta.getJobEntryCopy(real.x, real.y, iconsize);
            if (si != null && !selected_icon.equals(si)) {
              if (hop_candidate == null) {
                hop_candidate = new JobHopMeta(selected_icon, si);
                redraw();
              }
            } else {
              if (hop_candidate != null) {
                hop_candidate = null;
                redraw();
              }
            }
          }
        } else
        // are we moving a note around? 
        if (selected_note != null) {
          if (last_button == 1) {
            Point note = new Point(real.x - noteoffset.x, real.y - noteoffset.y);
            PropsUI.setLocation(selected_note, note.x, note.y);
            redraw();
            //spoon.refreshGraph();  removed in 2.4.1 (SB: defect #4862)
          }
        }
      }
    });

    // Drag & Drop for steps
    Transfer[] ttypes = new Transfer[] { XMLTransfer.getInstance() };
    DropTarget ddTarget = new DropTarget(canvas, DND.DROP_MOVE);
    ddTarget.setTransfer(ttypes);
    ddTarget.addDropListener(new DropTargetListener() {
      public void dragEnter(DropTargetEvent event) {
        drop_candidate = PropsUI.calculateGridPosition(getRealPosition(canvas, event.x, event.y));
        redraw();
      }

      public void dragLeave(DropTargetEvent event) {
        drop_candidate = null;
        redraw();
      }

      public void dragOperationChanged(DropTargetEvent event) {
      }

      public void dragOver(DropTargetEvent event) {
        drop_candidate = PropsUI.calculateGridPosition(getRealPosition(canvas, event.x, event.y));
        redraw();
      }

      public void drop(DropTargetEvent event) {
        // no data to copy, indicate failure in event.detail 
        if (event.data == null) {
          event.detail = DND.DROP_NONE;
          return;
        }

        Point p = getRealPosition(canvas, event.x, event.y);

        try {
          DragAndDropContainer container = (DragAndDropContainer) event.data;
          String entry = container.getData();

          switch (container.getType()) {
            case DragAndDropContainer.TYPE_BASE_JOB_ENTRY: // Create a new Job Entry on the canvas
            {
              JobEntryCopy jge = spoon.newJobEntry(jobMeta, entry, false);
              if (jge != null) {
                PropsUI.setLocation(jge, p.x, p.y);
                jge.setDrawn();
                redraw();
                
                // See if we want to draw a tool tip explaining how to create new hops...
                //
                if (jobMeta.nrJobEntries() > 1 && jobMeta.nrJobEntries() < 5 && spoon.props.isShowingHelpToolTips()) {
                  showHelpTip(p.x, p.y, Messages.getString("JobGraph.HelpToolTip.CreatingHops.Title"), Messages
                      .getString("JobGraph.HelpToolTip.CreatingHops.Message"));
                }
              }
            }
              break;
            case DragAndDropContainer.TYPE_JOB_ENTRY: // Drag existing one onto the canvas
            {
              JobEntryCopy jge = jobMeta.findJobEntry(entry, 0, true);
              if (jge != null) // Create duplicate of existing entry 
              {
                // There can be only 1 start!
                if (jge.isStart() && jge.isDrawn()) {
                  showOnlyStartOnceMessage(shell);
                  return;
                }

                boolean jge_changed = false;

                // For undo :
                JobEntryCopy before = (JobEntryCopy) jge.clone_deep();

                JobEntryCopy newjge = jge;
                if (jge.isDrawn()) {
                  newjge = (JobEntryCopy) jge.clone();
                  if (newjge != null) {
                    // newjge.setEntry(jge.getEntry());
                    if(log.isDebug()) log.logDebug(toString(), "entry aft = " + ((Object) jge.getEntry()).toString()); //$NON-NLS-1$

                    newjge.setNr(jobMeta.findUnusedNr(newjge.getName()));

                    jobMeta.addJobEntry(newjge);
                    spoon.addUndoNew(jobMeta, new JobEntryCopy[] { newjge }, new int[] { jobMeta
                        .indexOfJobEntry(newjge) });
                  } else {
                	  if(log.isDebug()) log.logDebug(toString(), "jge is not cloned!"); //$NON-NLS-1$
                  }
                } else {
                	if(log.isDebug()) log.logDebug(toString(), jge.toString() + " is not drawn"); //$NON-NLS-1$
                  jge_changed = true;
                }
                PropsUI.setLocation(newjge, p.x, p.y);
                newjge.setDrawn();
                if (jge_changed) {
                  spoon.addUndoChange(jobMeta, new JobEntryCopy[] { before }, new JobEntryCopy[] { newjge },
                      new int[] { jobMeta.indexOfJobEntry(newjge) });
                }
                redraw();
                spoon.refreshTree();
                log.logBasic("DropTargetEvent", "DROP " + newjge.toString() + "!, type="
                    + JobEntryCopy.getTypeDesc(newjge.getEntry()));
              } else {
                log.logError(toString(), "Unknown job entry dropped onto the canvas.");
              }
            }
              break;
            default:
              break;
          }
        } catch (Exception e) {
          new ErrorDialog(shell, Messages.getString("JobGraph.Dialog.ErrorDroppingObject.Message"), Messages.getString("JobGraph.Dialog.ErrorDroppingObject.Title"), e);
        }
      }

      public void dropAccept(DropTargetEvent event) {
        drop_candidate = null;
      }
    });

    addKeyListener(canvas);
    setBackground(GUIResource.getInstance().getColorBackground());
    setControlStates();
    
    // Add a timer to set correct the state of the run/stop buttons every 2 seconds...
    //
    final Timer timer = new Timer();
    TimerTask timerTask = new TimerTask() {
			public void run() {
				setControlStates();
			}
		};
	timer.schedule(timerTask, 2000, 1000);

	// Make sure the timer stops when we close the tab...
	//
	addDisposeListener(new DisposeListener() {
		public void widgetDisposed(DisposeEvent arg0) {
			timer.cancel();
		}
	});

  }

  private void addToolBar() {

    try {
      XulLoader loader = new SwtXulLoader();
      ResourceBundle bundle = ResourceBundle.getBundle("org/pentaho/di/ui/spoon/messages/messages", LanguageChoice.getInstance().getDefaultLocale());
      XulDomContainer xulDomContainer = loader.loadXul(XUL_FILE_JOB_TOOLBAR, bundle);
      xulDomContainer.addEventHandler(this);
      toolbar = (XulToolbar) xulDomContainer.getDocumentRoot().getElementById("nav-toolbar");
      
      ToolBar swtToolbar = (ToolBar) toolbar.getManagedObject();
      swtToolbar.pack();

      // Hack alert : more XUL limitations...
      //
      ToolItem sep = new ToolItem(swtToolbar, SWT.SEPARATOR);

      zoomLabel = new Combo(swtToolbar, SWT.DROP_DOWN);
      zoomLabel.setItems(TransPainter.magnificationDescriptions);
      zoomLabel.addSelectionListener(new SelectionAdapter() {
        public void widgetSelected(SelectionEvent arg0) {
          readMagnification();
        }
      });

      zoomLabel.addKeyListener(new KeyAdapter() {
        public void keyPressed(KeyEvent event) {
          if (event.character == SWT.CR) {
            readMagnification();
          }
        }
      });

      setZoomLabel();
      zoomLabel.pack();

      sep.setWidth(80);
      sep.setControl(zoomLabel);
      swtToolbar.pack();
    } catch (Throwable t) {
      log.logError(toString(), Const.getStackTracker(t));
      new ErrorDialog(shell, Messages.getString("Spoon.Exception.ErrorReadingXULFile.Title"), 
    		  Messages.getString("Spoon.Exception.ErrorReadingXULFile.Message", XUL_FILE_JOB_TOOLBAR), new Exception(t));
    }
  }

  private void setZoomLabel() {
    zoomLabel.setText(Integer.toString(Math.round(magnification * 100)) + "%");
  }
  
  /**
   * Allows for magnifying to any percentage entered by the user...
   */
  private void readMagnification(){
    String possibleText = zoomLabel.getText();
    possibleText = possibleText.replace("%", "");

    float possibleFloatMagnification;
    try {
      possibleFloatMagnification = Float.parseFloat(possibleText) / 100;
      magnification = possibleFloatMagnification;
      if (zoomLabel.getText().indexOf('%') < 0) {
        zoomLabel.setText(zoomLabel.getText().concat("%"));
      }
    } catch (Exception e) {
      MessageBox mb = new MessageBox(shell, SWT.YES | SWT.ICON_ERROR);
      mb.setMessage(Messages.getString("TransGraph.Dialog.InvalidZoomMeasurement.Message", zoomLabel.getText())); //$NON-NLS-1$
      mb.setText(Messages.getString("TransGraph.Dialog.InvalidZoomMeasurement.Title")); //$NON-NLS-1$
      mb.open();
    }
    redraw();
  }

  private void addKeyListener(Control control) {
    // Keyboard shortcuts...
    //
    control.addKeyListener(new KeyAdapter() {
      public void keyPressed(KeyEvent e) {
        // F2 --> rename Job entry
        if (e.keyCode == SWT.F2) {
          renameJobEntry();
        }

        // Delete
        if (e.keyCode == SWT.DEL) {
          JobEntryCopy copies[] = jobMeta.getSelectedEntries();
          if (copies != null && copies.length > 0) {
            delSelected();
          }
        }
        // CTRL-UP : allignTop();
        if (e.keyCode == SWT.ARROW_UP && (e.stateMask & SWT.CONTROL) != 0) {
          alligntop();
        }
        // CTRL-DOWN : allignBottom();
        if (e.keyCode == SWT.ARROW_DOWN && (e.stateMask & SWT.CONTROL) != 0) {
          allignbottom();
        }
        // CTRL-LEFT : allignleft();
        if (e.keyCode == SWT.ARROW_LEFT && (e.stateMask & SWT.CONTROL) != 0) {
          allignleft();
        }
        // CTRL-RIGHT : allignRight();
        if (e.keyCode == SWT.ARROW_RIGHT && (e.stateMask & SWT.CONTROL) != 0) {
          allignright();
        }
        // ALT-RIGHT : distributeHorizontal();
        if (e.keyCode == SWT.ARROW_RIGHT && (e.stateMask & SWT.ALT) != 0) {
          distributehorizontal();
        }
        // ALT-UP : distributeVertical();
        if (e.keyCode == SWT.ARROW_UP && (e.stateMask & SWT.ALT) != 0) {
          distributevertical();
        }
        // ALT-HOME : snap to grid
        if (e.keyCode == SWT.HOME && (e.stateMask & SWT.ALT) != 0) {
          snaptogrid(ConstUI.GRID_SIZE);
        }
      }
    });

  }

  public void selectInRect(JobMeta jobMeta, Rectangle rect) {
    int i;
    for (i = 0; i < jobMeta.nrJobEntries(); i++) {
      JobEntryCopy je = jobMeta.getJobEntry(i);
      Point p = je.getLocation();
      if (((p.x >= rect.x && p.x <= rect.x + rect.width) || (p.x >= rect.x + rect.width && p.x <= rect.x))
          && ((p.y >= rect.y && p.y <= rect.y + rect.height) || (p.y >= rect.y + rect.height && p.y <= rect.y)))
        je.setSelected(true);
    }
  }

  public void redraw() {
    canvas.redraw();
    setZoomLabel();
  }

  public boolean forceFocus() {
    return canvas.forceFocus();
  }

  public boolean setFocus() {
    spoon.getMainSpoonContainer().addEventHandler(this);
    return canvas.setFocus();
  }

  public void renameJobEntry() {
    JobEntryCopy[] selection = jobMeta.getSelectedEntries();
    if (selection != null && selection.length == 1) {
      final JobEntryCopy jobEntryMeta = selection[0];

      // What is the location of the step?
      final String name = jobEntryMeta.getName();
      Point stepLocation = jobEntryMeta.getLocation();
      Point realStepLocation = real2screen(stepLocation.x, stepLocation.y);

      // The location of the step name?
      GC gc = new GC(shell.getDisplay());
      gc.setFont(GUIResource.getInstance().getFontGraph());
      Point namePosition = TransPainter.getNamePosition(gc, name, realStepLocation, iconsize);
      int width = gc.textExtent(name).x + 30;
      gc.dispose();

      // at this very point, create a new text widget...
      final Text text = new Text(this, SWT.SINGLE | SWT.BORDER);
      text.setText(name);
      FormData fdText = new FormData();
      fdText.left = new FormAttachment(0, namePosition.x);
      fdText.right = new FormAttachment(0, namePosition.x + width);
      fdText.top = new FormAttachment(0, namePosition.y);
      text.setLayoutData(fdText);

      // Add a listener!
      // Catch the keys pressed when editing a Text-field...
      KeyListener lsKeyText = new KeyAdapter() {
        public void keyPressed(KeyEvent e) {
          // "ENTER": close the text editor and copy the data over 
          if (e.character == SWT.CR) {
            String newName = text.getText();
            text.dispose();
            if (!name.equals(newName))
              renameJobEntry(jobEntryMeta, newName);
          }

          if (e.keyCode == SWT.ESC) {
            text.dispose();
          }
        }
      };

      text.addKeyListener(lsKeyText);
      text.addFocusListener(new FocusAdapter() {
        public void focusLost(FocusEvent e) {
          String newName = text.getText();
          text.dispose();
          if (!name.equals(newName))
            renameJobEntry(jobEntryMeta, newName);
        }
      });

      this.layout(true, true);

      text.setFocus();
      text.setSelection(0, name.length());
    }
  }

  /**
   * Method gets called, when the user wants to change a job entries name and he indeed entered
   * a different name then the old one. Make sure that no other job entry matches this name
   * and rename in case of uniqueness.
   * 
   * @param jobEntry
   * @param newName
   */
  public void renameJobEntry(JobEntryCopy jobEntry, String newName) {
    JobEntryCopy[] jobs = jobMeta.getAllJobGraphEntries(newName);
    if (jobs != null && jobs.length > 0) {
      MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_INFORMATION);
      mb.setMessage(Messages.getString("Spoon.Dialog.JobEntryNameExists.Message", newName));
      mb.setText(Messages.getString("Spoon.Dialog.JobEntryNameExists.Title"));
      mb.open();
    } else {
      jobEntry.setName(newName);
      jobEntry.setChanged();
      spoon.refreshTree(); // to reflect the new name
      spoon.refreshGraph();
    }
  }

  public static void showOnlyStartOnceMessage(Shell shell) {
    MessageBox mb = new MessageBox(shell, SWT.YES | SWT.ICON_ERROR);
    mb.setMessage(Messages.getString("JobGraph.Dialog.OnlyUseStartOnce.Message"));
    mb.setText(Messages.getString("JobGraph.Dialog.OnlyUseStartOnce.Title"));
    mb.open();
  }

  public void delSelected() {
    JobEntryCopy[] copies = jobMeta.getSelectedEntries();
    int nrsels = copies.length;
    if (nrsels == 0)
      return;

    // Load the list of steps
    List<String> stepList = new ArrayList<String>();
    for (int i = 0; i < copies.length; ++i) {
      stepList.add(copies[i].toString());
    }

    // Display the delete confirmation message box
    MessageBox mb = new DeleteMessageBox(shell, Messages.getString("Spoon.Dialog.DeletionConfirm.Message"), //$NON-NLS-1$
        stepList);
    int answer = mb.open();
    if (answer == SWT.YES) {
      // Perform the delete
      for (int i = 0; i < copies.length; i++) {
        spoon.deleteJobEntryCopies(jobMeta, copies[i]);
      }
      spoon.refreshTree();
      spoon.refreshGraph();
    }
  }

  public void clearSettings() {
    selected_icon = null;
    selected_note = null;
    selected_entries = null;
    selrect = null;
    hop_candidate = null;
    last_hop_split = null;
    last_button = 0;
    iconoffset = null;
    for (int i = 0; i < jobMeta.nrJobHops(); i++)
      jobMeta.getJobHop(i).setSplit(false);
  }

  public Point screen2real(int x, int y) {
    getOffset();
    Point real;
    if (offset != null) {
      real = new Point(Math.round((x / magnification - offset.x)), Math.round((y / magnification - offset.y)));
    } else {
      real = new Point(x, y);
    }

    return real;
  }

  public Point real2screen(int x, int y) {
    getOffset();
    Point screen = new Point(x + offset.x, y + offset.y);

    return screen;
  }

  public Point getRealPosition(Composite canvas, int x, int y) {
    Point p = new Point(0, 0);
    Composite follow = canvas;
    while (follow != null) {
      Point xy = new Point(follow.getLocation().x, follow.getLocation().y);
      p.x += xy.x;
      p.y += xy.y;
      follow = follow.getParent();
    }

    p.x = x - p.x - 8;
    p.y = y - p.y - 48;

    return screen2real(p.x, p.y);
  }

  // See if location (x,y) is on a line between two steps: the hop!
  // return the HopInfo if so, otherwise: null	
  protected JobHopMeta findJobHop(int x, int y) {
    int i;
    JobHopMeta online = null;
    for (i = 0; i < jobMeta.nrJobHops(); i++) {
      JobHopMeta hi = jobMeta.getJobHop(i);

      int line[] = getLine(hi.from_entry, hi.to_entry);

      if (line != null && pointOnLine(x, y, line))
        online = hi;
    }
    return online;
  }

  protected int[] getLine(JobEntryCopy fs, JobEntryCopy ts) {
    if (fs == null || ts == null)
      return null;

    Point from = fs.getLocation();
    Point to = ts.getLocation();
    offset = getOffset();

    int x1 = from.x + iconsize / 2;
    int y1 = from.y + iconsize / 2;

    int x2 = to.x + iconsize / 2;
    int y2 = to.y + iconsize / 2;

    return new int[] { x1, y1, x2, y2 };
  }

  private void showHelpTip(int x, int y, String tipTitle, String tipMessage) {

    helpTip.setTitle(tipTitle);
    helpTip.setMessage(tipMessage);
    helpTip.setCheckBoxMessage(Messages.getString("JobGraph.HelpToolTip.DoNotShowAnyMoreCheckBox.Message"));
    // helpTip.hide();
    // int iconSize = spoon.props.getIconSize();
    org.eclipse.swt.graphics.Point location = new org.eclipse.swt.graphics.Point(x - 5, y - 5);

    helpTip.show(location);
  }
  public void setJobEntry(JobEntryCopy jobEntry) {
    this.jobEntry = jobEntry;
  }

  public JobEntryCopy getJobEntry() {
    return jobEntry;
  }

  public void openTransformation() {
    final JobEntryInterface entry = getJobEntry().getEntry();
    openTransformation((JobEntryTrans) entry);
  }

  public void openJob() {
    final JobEntryInterface entry = getJobEntry().getEntry();
    openJob((JobEntryJob) entry);
  }

  public void newHopClick() {
    selected_entries = null;
    newHop();
  }

  public void editEntryClick() {
    selected_entries = null;
    editEntry(getJobEntry());
  }

  public void editEntryDescription() {
    String title = Messages.getString("JobGraph.Dialog.EditDescription.Title"); //$NON-NLS-1$
    String message = Messages.getString("JobGraph.Dialog.EditDescription.Message"); //$NON-NLS-1$
    EnterTextDialog dd = new EnterTextDialog(shell, title, message, getJobEntry().getDescription());
    String des = dd.open();
    if (des != null)
      jobEntry.setDescription(des);
  }

  /**
   * Go from serial to parallel to serial execution
   */
  public void editEntryParallel() {
    getJobEntry().setLaunchingInParallel(!getJobEntry().isLaunchingInParallel());
    if (getJobEntry().isLaunchingInParallel()) {
      // Show a warning (optional)
      //
      if ("Y".equalsIgnoreCase(spoon.props.getCustomParameter(STRING_PARALLEL_WARNING_PARAMETER, "Y"))) //$NON-NLS-1$ //$NON-NLS-2$
      {
        MessageDialogWithToggle md = new MessageDialogWithToggle(shell, Messages
            .getString("JobGraph.ParallelJobEntriesWarning.DialogTitle"), //$NON-NLS-1$
            null, Messages.getString("JobGraph.ParallelJobEntriesWarning.DialogMessage", Const.CR) + Const.CR, //$NON-NLS-1$ //$NON-NLS-2$
            MessageDialog.WARNING, new String[] { Messages.getString("JobGraph.ParallelJobEntriesWarning.Option1") }, //$NON-NLS-1$
            0, Messages.getString("JobGraph.ParallelJobEntriesWarning.Option2"), //$NON-NLS-1$
            "N".equalsIgnoreCase(spoon.props.getCustomParameter(STRING_PARALLEL_WARNING_PARAMETER, "Y")) //$NON-NLS-1$ //$NON-NLS-2$
        );
        MessageDialogWithToggle.setDefaultImage(GUIResource.getInstance().getImageSpoon());
        md.open();
        spoon.props.setCustomParameter(STRING_PARALLEL_WARNING_PARAMETER, md.getToggleState() ? "N" : "Y"); //$NON-NLS-1$ //$NON-NLS-2$
        spoon.props.saveProps();
      }
    }
    redraw();
  }

  public void duplicateEntry() throws KettleException {
    if (!canDup(jobEntry)) {
      JobGraph.showOnlyStartOnceMessage(spoon.getShell());
    }

    spoon.delegates.jobs.dupeJobEntry(jobMeta, jobEntry);
  }

  public void copyEntry() {
    JobEntryCopy[] entries = jobMeta.getSelectedEntries();
    for (int i = 0; i < entries.length; i++) {
      if (!canDup(entries[i]))
        entries[i] = null;

    }

    spoon.delegates.jobs.copyJobEntries(jobMeta, entries);
  }

  private boolean canDup(JobEntryCopy entry) {
    return !entry.isStart();
  }

  public void detatchEntry() {
    detach(getJobEntry());
    jobMeta.unselectAll();
  }

  public void hideEntry() {
    getJobEntry().setDrawn(false);
    // nr > 1: delete
    if (jobEntry.getNr() > 0) {
      int ind = jobMeta.indexOfJobEntry(jobEntry);
      jobMeta.removeJobEntry(ind);
      spoon.addUndoDelete(jobMeta, new JobEntryCopy[] { getJobEntry() }, new int[] { ind });
    }
    redraw();
  }

  public void deleteEntry() {
    spoon.deleteJobEntryCopies(jobMeta, getJobEntry());
    redraw();
  }

  protected synchronized void setMenu(int x, int y) {

    currentMouseX = x;
    currentMouseY = y;

    final JobEntryCopy jobEntry = jobMeta.getJobEntryCopy(x, y, iconsize);
    setJobEntry(jobEntry);
    Document doc = spoon.getMainSpoonContainer().getDocumentRoot();
    if (jobEntry != null) // We clicked on a Job Entry!
    {
      XulMenupopup menu = (XulMenupopup)doc.getElementById("job-graph-entry");
      if (menu != null) {
        int sels = jobMeta.nrSelected();
        XulMenuitem item = (XulMenuitem) doc.getElementById("job-graph-entry-newhop");
        item.setDisabled(sels == 2);
        item = (XulMenuitem) doc.getElementById("job-graph-entry-launch");
        switch (jobEntry.getJobEntryType()) {
          case TRANS: {
            item.setDisabled(false);
            item.setLabel(Messages.getString("JobGraph.PopupMenu.JobEntry.LaunchSpoon"));
            break;
          }
          case JOB: {
            item.setDisabled(false);
            item.setLabel(Messages.getString("JobGraph.PopupMenu.JobEntry.LaunchChef"));
          }
            break;
          default: {
            item.setDisabled(true);
          }
            break;
        }

        item = (XulMenuitem) doc.getElementById("job-graph-entry-align-snap");
        item.setLabel(Messages.getString("JobGraph.PopupMenu.JobEntry.AllignDistribute.SnapToGrid") + ConstUI.GRID_SIZE
            + ")\tALT-HOME");

        XulMenu aMenu = (XulMenu) doc.getElementById("job-graph-entry-align");
        if (aMenu != null) {
          aMenu.setDisabled(sels < 1);
        }

        item = (XulMenuitem) doc.getElementById("job-graph-entry-detach");
        if (item != null) {
          item.setDisabled(!jobMeta.isEntryUsedInHops(jobEntry));
        }

        item = (XulMenuitem) doc.getElementById("job-graph-entry-hide");
        if (item != null) {
          item.setDisabled(!(jobEntry.isDrawn() && !jobMeta.isEntryUsedInHops(jobEntry)));
        }

        item = (XulMenuitem) doc.getElementById("job-graph-entry-delete");
        if (item != null) {
          item.setDisabled(!jobEntry.isDrawn());
        }

        item = (XulMenuitem) doc.getElementById("job-graph-entry-parallel");
        if (item != null) {
          item.setSelected(jobEntry.isLaunchingInParallel());
        }

        ConstUI.displayMenu((Menu)menu.getManagedObject(), canvas);
      }

    } else // Clear the menu
    {
      final JobHopMeta hi = findJobHop(x, y);
      setCurrentHop(hi);

      if (hi != null) // We clicked on a HOP!
      {

        XulMenupopup menu = (XulMenupopup) doc.getElementById("job-graph-hop");
        if (menu != null) {
          XulMenuitem miPopEvalUncond = (XulMenuitem) doc.getElementById("job-graph-hop-evaluation-uncond");
          XulMenuitem miPopEvalTrue = (XulMenuitem) doc.getElementById("job-graph-hop-evaluation-true");
          XulMenuitem miPopEvalFalse = (XulMenuitem) doc.getElementById("job-graph-hop-evaluation-uncond");
          XulMenuitem miDisHop = (XulMenuitem) doc.getElementById("job-graph-hop-enabled");

          if (hi.isUnconditional()) {
            if (miPopEvalUncond != null)
              miPopEvalUncond.setSelected(true);
            if (miPopEvalTrue != null)
              miPopEvalTrue.setSelected(false);
            if (miPopEvalFalse != null)
              miPopEvalFalse.setSelected(false);
          } else {
            if (hi.getEvaluation()) {
              if (miPopEvalUncond != null)
                miPopEvalUncond.setSelected(false);
              if (miPopEvalTrue != null)
                miPopEvalTrue.setSelected(true);
              if (miPopEvalFalse != null)
                miPopEvalFalse.setSelected(false);
            } else {
              if (miPopEvalUncond != null)
                miPopEvalUncond.setSelected(false);
              if (miPopEvalTrue != null)
                miPopEvalTrue.setSelected(false);
              if (miPopEvalFalse != null)
                miPopEvalFalse.setSelected(true);
            }
          }
          if (!hi.from_entry.evaluates()) {
            if (miPopEvalTrue != null)
              miPopEvalTrue.setDisabled(true);
            if (miPopEvalFalse != null)
              miPopEvalFalse.setDisabled(true);
          }
          if (!hi.from_entry.isUnconditional()) {
            if (miPopEvalUncond != null)
              miPopEvalUncond.setDisabled(true);
          }

          if (miDisHop != null) {
            if (hi.isEnabled())
              miDisHop.setLabel(Messages.getString("JobGraph.PopupMenu.Hop.Disable")); //$NON-NLS-1$
            else
              miDisHop.setLabel(Messages.getString("JobGraph.PopupMenu.Hop.Enable")); //$NON-NLS-1$
          }
          ConstUI.displayMenu((Menu)menu.getManagedObject(), canvas);
        }

      } else {
        // Clicked on the background: maybe we hit a note?
        final NotePadMeta ni = jobMeta.getNote(x, y);
        setCurrentNote(ni);
        if (ni != null) {
          XulMenupopup menu = (XulMenupopup) doc.getElementById("job-graph-note");
          if (menu != null) {
            ConstUI.displayMenu((Menu)menu.getManagedObject(), canvas);
          }
        } else {
          XulMenupopup menu = (XulMenupopup) doc.getElementById("job-graph-background");
          if (menu != null) {
            final String clipcontent = spoon.fromClipboard();
            XulMenuitem item = (XulMenuitem) doc.getElementById("job-graph-note-paste");
            if (item != null) {
              item.setDisabled(clipcontent == null);
            }

            ConstUI.displayMenu((Menu)menu.getManagedObject(), canvas);
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
    editProperties(jobMeta, spoon, spoon.getRepository(), true);
  }

  public void pasteNote() {
    final String clipcontent = spoon.fromClipboard();
    Point loc = new Point(currentMouseX, currentMouseY);
    spoon.pasteXML(jobMeta, clipcontent, loc);
  }

  public void newNote() {
    selrect = null;
    String title = Messages.getString("JobGraph.Dialog.EditNote.Title");
    String message = Messages.getString("JobGraph.Dialog.EditNote.Message");
    EnterTextDialog dd = new EnterTextDialog(shell, title, message, "");
    String n = dd.open();
    if (n != null) {
      NotePadMeta npi = new NotePadMeta(n, lastclick.x, lastclick.y, ConstUI.NOTE_MIN_SIZE, ConstUI.NOTE_MIN_SIZE);
      jobMeta.addNote(npi);
      spoon.addUndoNew(jobMeta, new NotePadMeta[] { npi }, new int[] { jobMeta.indexOfNote(npi) });
      redraw();
    }
  }

  public void setCurrentNote(NotePadMeta ni) {
    this.ni = ni;
  }

  public NotePadMeta getCurrentNote() {
    return ni;
  }

  public void editNote() {
    selrect = null;
    editNote(getCurrentNote());
  }

  public void deleteNote() {
    selrect = null;
    int idx = jobMeta.indexOfNote(getCurrentNote());
    if (idx >= 0) {
      jobMeta.removeNote(idx);
      spoon.addUndoDelete(jobMeta, new NotePadMeta[] { getCurrentNote() }, new int[] { idx });
    }
    redraw();
  }

  public void raiseNote() {
    selrect = null;
    int idx = jobMeta.indexOfNote(getCurrentNote());
    if (idx >= 0) {
      jobMeta.raiseNote(idx);
    }
    redraw();
  }

  public void lowerNote() {
    selrect = null;
    int idx = jobMeta.indexOfNote(getCurrentNote());
    if (idx >= 0) {
      jobMeta.lowerNote(idx);
    }
    redraw();
  }

  public void flipHop() {
    selrect = null;
    JobEntryCopy dummy = currentHop.from_entry;
    currentHop.from_entry = currentHop.to_entry;
    currentHop.to_entry = dummy;

    if (jobMeta.hasLoop(currentHop.from_entry)) {
      spoon.refreshGraph();
      MessageBox mb = new MessageBox(shell, SWT.YES | SWT.ICON_WARNING);
      mb.setMessage(Messages.getString("JobGraph.Dialog.HopFlipCausesLoop.Message"));
      mb.setText(Messages.getString("JobGraph.Dialog.HopFlipCausesLoop.Title"));
      mb.open();

      dummy = currentHop.from_entry;
      currentHop.from_entry = currentHop.to_entry;
      currentHop.to_entry = dummy;
      spoon.refreshGraph();
    } else {
      currentHop.setChanged();
      spoon.refreshGraph();
      spoon.refreshTree();
      spoon.setShellText();
    }
  }

  public void disableHop() {
    selrect = null;
    currentHop.setEnabled(!currentHop.isEnabled());
    spoon.refreshGraph();
    spoon.refreshTree();
  }

  public void deleteHop() {
    selrect = null;
    int idx = jobMeta.indexOfJobHop(currentHop);
    jobMeta.removeJobHop(idx);
    spoon.refreshTree();
    spoon.refreshGraph();
  }

  public void setHopConditional(String id) {

    if ("job-graph-hop-evaluation-uncond".equals(id)) { //$NON-NLS-1$
      currentHop.setUnconditional();
      spoon.refreshGraph();
    } else if ("job-graph-hop-evaluation-true".equals(id)) { //$NON-NLS-1$
      currentHop.setConditional();
      currentHop.setEvaluation(true);
      spoon.refreshGraph();
    } else if ("job-graph-hop-evaluation-false".equals(id)) { //$NON-NLS-1$
      currentHop.setConditional();
      currentHop.setEvaluation(false);
      spoon.refreshGraph();
    }

  }

  protected void setCurrentHop(JobHopMeta hop) {
    currentHop = hop;
  }

  protected JobHopMeta getCurrentHop() {
    return currentHop;
  }

  protected void setToolTip(int x, int y, int screenX, int screenY) {
    if (!spoon.getProperties().showToolTips())
      return;

    canvas.setToolTipText("-"); // Some stupid bug in GTK+ causes a phantom tool tip to pop up, even if the tip is null
    canvas.setToolTipText(null);

    String newTip = null;

    final JobEntryCopy je = jobMeta.getJobEntryCopy(x, y, iconsize);
    if (je != null && je.isDrawn()) // We hover above a Step!
    {
      // Set the tooltip!
      String desc = je.getDescription();
      if (desc != null) {
        int le = desc.length() >= 200 ? 200 : desc.length();
        newTip = desc.substring(0, le);
      } else {
        newTip = je.toString();
      }
    } else {
      offset = getOffset();
      JobHopMeta hi = findJobHop(x + offset.x, y + offset.x);
      if (hi != null) {
        newTip = hi.toString();
      } else {
        newTip = null;
      }
    }

    if (newTip == null || !newTip.equalsIgnoreCase(getToolTipText())) {
      canvas.setToolTipText(newTip);
    }
  }

  public void launchStuff(JobEntryCopy jobentry) {
    if (jobentry.getJobEntryType() == JobEntryType.JOB) {
      final JobEntryJob entry = (JobEntryJob) jobentry.getEntry();
      if ((entry != null && entry.getFilename() != null && spoon.rep == null)
          || (entry != null && entry.getName() != null && spoon.rep != null)) {
        openJob(entry);
      }
    } else if (jobentry.getJobEntryType() == JobEntryType.TRANS) {
      final JobEntryTrans entry = (JobEntryTrans) jobentry.getEntry();
      if ((entry != null && entry.getFilename() != null && spoon.rep == null)
          || (entry != null && entry.getName() != null && spoon.rep != null)) {
        openTransformation(entry);
      }
    }
  }
  
  public void launchStuff() {
    launchStuff(jobEntry);
  }

  protected void openTransformation(JobEntryTrans entry) {
    String exactFilename = jobMeta.environmentSubstitute(entry.getFilename());
    String exactTransname = jobMeta.environmentSubstitute(entry.getTransname());

    // check, whether a tab of this name is already opened
    TabItem tab = spoon.delegates.tabs.findTabItem(exactFilename, TabMapEntry.OBJECT_TYPE_TRANSFORMATION_GRAPH);
    if (tab == null) {
      tab = spoon.delegates.tabs.findTabItem(Const.filenameOnly(exactFilename),
          TabMapEntry.OBJECT_TYPE_TRANSFORMATION_GRAPH);
    }
    if (tab != null) {
      spoon.tabfolder.setSelected(tab);
      return;
    }

    // Load from repository?
    if (TransMeta.isRepReference(exactFilename, exactTransname)) {
      try {
        // New transformation?
        //
        long id = spoon.rep.getTransformationID(exactTransname, spoon.rep.getDirectoryTree().findDirectory(entry.getDirectory()).getID());
        TransMeta newTrans;
        if (id < 0) // New
        {
          newTrans = new TransMeta(null, exactTransname, entry.arguments);
        } else {
          newTrans = new TransMeta(spoon.rep, exactTransname, spoon.rep.getDirectoryTree().findDirectory(entry.getDirectory()));
        }

        copyInternalJobVariables(jobMeta, newTrans);
        spoon.setParametersAsVariablesInUI(newTrans, newTrans);

        spoon.addTransGraph(newTrans);
        newTrans.clearChanged();
        spoon.open();
      } catch (Throwable e) {
        new ErrorDialog(shell, Messages.getString("JobGraph.Dialog.ErrorLaunchingSpoonCanNotLoadTransformation.Title"),
            Messages.getString("JobGraph.Dialog.ErrorLaunchingSpoonCanNotLoadTransformation.Message"), (Exception) e);
      }
    } else {
      try {
        // only try to load if the file exists...
        if (Const.isEmpty(exactFilename)) {
          throw new Exception(Messages.getString("JobGraph.Exception.NoFilenameSpecified"));
        }
        TransMeta launchTransMeta = null;
        if (KettleVFS.fileExists(exactFilename)) {
          launchTransMeta = new TransMeta(exactFilename);
        } else {
          launchTransMeta = new TransMeta();
        }

        launchTransMeta.clearChanged();
        launchTransMeta.setFilename(exactFilename);

        copyInternalJobVariables(jobMeta, launchTransMeta);
        spoon.setParametersAsVariablesInUI(launchTransMeta, launchTransMeta);

        spoon.addTransGraph(launchTransMeta);
        spoon.open();
      } catch (Throwable e) {
        new ErrorDialog(shell, Messages.getString("JobGraph.Dialog.ErrorLaunchingSpoonCanNotLoadTransformationFromXML.Title"), 
        		Messages.getString("JobGraph.Dialog.ErrorLaunchingSpoonCanNotLoadTransformationFromXML.Message"), (Exception) e);
      }

    }
    spoon.applyVariables();
  }

  public static void copyInternalJobVariables(JobMeta sourceJobMeta, TransMeta targetTransMeta) {
    // Also set some internal JOB variables...
    //
    String[] internalVariables = Const.INTERNAL_JOB_VARIABLES;

    for (String variableName : internalVariables) {
      targetTransMeta.setVariable(variableName, sourceJobMeta.getVariable(variableName));
    }
  }

  public void openJob(JobEntryJob entry) {
    String exactFilename = jobMeta.environmentSubstitute(entry.getFilename());
    String exactJobname = jobMeta.environmentSubstitute(entry.getJobName());

    // Load from repository?
    if (Const.isEmpty(exactFilename) && !Const.isEmpty(exactJobname)) {
      try {
        JobMeta newJobMeta = new JobMeta(log, spoon.rep, exactJobname, spoon.rep.getDirectoryTree().findDirectory(entry.getDirectory()));
        newJobMeta.clearChanged();
        spoon.setParametersAsVariablesInUI(newJobMeta, newJobMeta);
        spoon.delegates.jobs.addJobGraph(newJobMeta);
      } catch (Throwable e) {
        new ErrorDialog(shell, Messages.getString("JobGraph.Dialog.ErrorLaunchingChefCanNotLoadJob.Title"), 
        		Messages.getString("JobGraph.Dialog.ErrorLaunchingChefCanNotLoadJob.Message"), e);
      }
    } else {
      try {
        if (Const.isEmpty(exactFilename)) {
          throw new Exception(Messages.getString("JobGraph.Exception.NoFilenameSpecified"));
        }

        JobMeta newJobMeta;

        if (KettleVFS.fileExists(exactFilename)) {
          newJobMeta = new JobMeta(log, exactFilename, spoon.rep, spoon);
        } else {
          newJobMeta = new JobMeta(log);
        }
        
        spoon.setParametersAsVariablesInUI(newJobMeta, newJobMeta);

        newJobMeta.setFilename(exactFilename);
        newJobMeta.clearChanged();
        spoon.delegates.jobs.addJobGraph(newJobMeta);
      } catch (Throwable e) {
        new ErrorDialog(shell, Messages.getString("JobGraph.Dialog.ErrorLaunchingChefCanNotLoadJobFromXML.Title"),
            Messages.getString("JobGraph.Dialog.ErrorLaunchingChefCanNotLoadJobFromXML.Message"), e);
      }
    }
    spoon.applyVariables();
  }

  public void paintControl(PaintEvent e) {
    Point area = getArea();
    if (area.x == 0 || area.y == 0)
      return; // nothing to do!

    Display disp = shell.getDisplay();
    if (disp.isDisposed())
      return; // Nothing to do!

    Image img = new Image(disp, area.x, area.y);
    GC gc = new GC(img);
    drawJob(disp, gc, PropsUI.getInstance().isBrandingActive());
    e.gc.drawImage(img, 0, 0);
    gc.dispose();
    img.dispose();

    // spoon.setShellText();
  }

  public void drawJob(Device device, GC gc, boolean branded) {
    if (spoon.props.isAntiAliasingEnabled())
      gc.setAntialias(SWT.ON);

    shadowsize = spoon.props.getShadowSize();
    gridSize = spoon.props.getCanvasGridSize();

    Point area = getArea();
    Point max = jobMeta.getMaximum();
    Point thumb = getThumb(area, max);
    offset = getOffset(thumb, area);

    gc.setBackground(GUIResource.getInstance().getColorBackground());

    hori.setThumb(thumb.x);
    vert.setThumb(thumb.y);

    if (branded) {
      Image gradient = GUIResource.getInstance().getImageBanner();
      gc.drawImage(gradient, 0, 0);

      Image logo = GUIResource.getInstance().getImageKettleLogo();
      org.eclipse.swt.graphics.Rectangle logoBounds = logo.getBounds();
      gc.drawImage(logo, 20, area.y - logoBounds.height);
    }

    // If there is a shadow, we draw the transformation first with an alpha setting
    //
    if (shadowsize > 0) {
      Transform transform = new Transform(device);
      transform.scale(magnification, magnification);
      transform.translate(translationX + shadowsize * magnification, translationY + shadowsize * magnification);
      gc.setAlpha(20);
      gc.setTransform(transform);

      shadow = true;
      drawJobElements(gc);
    }

    // Draw the transformation onto the image
    Transform transform = new Transform(device);
    transform.scale(magnification, magnification);
    transform.translate(translationX, translationY);
    gc.setAlpha(255);
    gc.setTransform(transform);

    shadow = false;
    drawJobElements(gc);

  }

  private void drawJobElements(GC gc) {
    if (!shadow && gridSize > 1) {
      drawGrid(gc);
    }

    // First draw the notes...
    gc.setFont(GUIResource.getInstance().getFontNote());

    for (int i = 0; i < jobMeta.nrNotes(); i++) {
      NotePadMeta ni = jobMeta.getNote(i);
      drawNote(gc, ni);
    }

    gc.setFont(GUIResource.getInstance().getFontGraph());

    // ... and then the rest on top of it...
    for (int i = 0; i < jobMeta.nrJobHops(); i++) {
      JobHopMeta hi = jobMeta.getJobHop(i);
      drawJobHop(gc, hi, false);
    }

    if (hop_candidate != null) {
      drawJobHop(gc, hop_candidate, true);
    }

    for (int j = 0; j < jobMeta.nrJobEntries(); j++) {
      JobEntryCopy je = jobMeta.getJobEntry(j);
      drawJobEntryCopy(gc, je);
    }

    if (drop_candidate != null) {
      gc.setLineStyle(SWT.LINE_SOLID);
      gc.setForeground(GUIResource.getInstance().getColorBlack());
      Point screen = real2screen(drop_candidate.x, drop_candidate.y);
      gc.drawRectangle(screen.x, screen.y, iconsize, iconsize);
    }

    if (!shadow) {
      drawRect(gc, selrect);
    }
  }

  private void drawGrid(GC gc) {
    int gridSize = spoon.props.getCanvasGridSize();
    Rectangle bounds = gc.getDevice().getBounds();
    for (int x = 0; x < bounds.width; x += gridSize) {
      for (int y = 0; y < bounds.height; y += gridSize) {
        gc.drawPoint(x + (offset.x % gridSize), y + (offset.y % gridSize));
      }
    }
  }

  protected void drawJobHop(GC gc, JobHopMeta hop, boolean candidate) {
    if (hop == null || hop.from_entry == null || hop.to_entry == null)
      return;
    if (!hop.from_entry.isDrawn() || !hop.to_entry.isDrawn())
      return;

    drawLine(gc, hop, candidate);
  }

  public Image getIcon(JobEntryCopy je) {
    Image im = null;
    if (je == null)
      return null;

    switch (je.getJobEntryType()) {
      case SPECIAL:
        if (je.isStart())
          im = GUIResource.getInstance().getImageStart();
        if (je.isDummy())
          im = GUIResource.getInstance().getImageDummy();
        break;
      default:
        String configId = je.getEntry().getConfigId();
        if (configId != null) {
          im = (Image) GUIResource.getInstance().getImagesJobentries().get(configId);
        }
    }
    return im;
  }

  protected void drawJobEntryCopy(GC gc, JobEntryCopy je) {
    if (!je.isDrawn())
      return;

    Point pt = je.getLocation();

    int x, y;
    if (pt != null) {
      x = pt.x;
      y = pt.y;
    } else {
      x = 50;
      y = 50;
    }
    String name = je.getName();
    if (je.isSelected())
      gc.setLineWidth(3);
    else
      gc.setLineWidth(1);

    Image im = getIcon(je);
    if (im != null) // Draw the icon!
    {
      Rectangle bounds = new Rectangle(im.getBounds().x, im.getBounds().y, im.getBounds().width, im.getBounds().height);
      gc.drawImage(im, 0, 0, bounds.width, bounds.height, offset.x + x, offset.y + y, iconsize, iconsize);
    }
    gc.setBackground(GUIResource.getInstance().getColorWhite());
    gc.drawRectangle(offset.x + x - 1, offset.y + y - 1, iconsize + 1, iconsize + 1);
    //gc.setXORMode(true);
    Point textsize = new Point(gc.textExtent("" + name).x, gc.textExtent("" + name).y);

    gc.setBackground(GUIResource.getInstance().getColorBackground());
    gc.setLineWidth(1);

    int xpos = offset.x + x + (iconsize / 2) - (textsize.x / 2);
    int ypos = offset.y + y + iconsize + 5;

    gc.setForeground(GUIResource.getInstance().getColorBlack());
    gc.drawText(name, xpos, ypos, true);

  }

  protected void drawNote(GC gc, NotePadMeta ni) {
    int flags = SWT.DRAW_DELIMITER | SWT.DRAW_TAB | SWT.DRAW_TRANSPARENT;

    org.eclipse.swt.graphics.Point ext = gc.textExtent(ni.getNote(), flags);
    Point p = new Point(ext.x, ext.y);
    Point loc = ni.getLocation();
    Point note = real2screen(loc.x, loc.y);
    int margin = Const.NOTE_MARGIN;
    p.x += 2 * margin;
    p.y += 2 * margin;
    int width = ni.width;
    int height = ni.height;
    if (p.x > width)
      width = p.x;
    if (p.y > height)
      height = p.y;

    int noteshape[] = new int[] { note.x, note.y, // Top left
        note.x + width + 2 * margin, note.y, // Top right
        note.x + width + 2 * margin, note.y + height, // bottom right 1
        note.x + width, note.y + height + 2 * margin, // bottom right 2
        note.x + width, note.y + height, // bottom right 3
        note.x + width + 2 * margin, note.y + height, // bottom right 1
        note.x + width, note.y + height + 2 * margin, // bottom right 2
        note.x, note.y + height + 2 * margin // bottom left
    };

    gc.setForeground(GUIResource.getInstance().getColorDarkGray());
    gc.setBackground(GUIResource.getInstance().getColorYellow());

    gc.fillPolygon(noteshape);
    gc.drawPolygon(noteshape);
    gc.setForeground(GUIResource.getInstance().getColorBlack());
    gc.drawText(ni.getNote(), note.x + margin, note.y + margin, flags);

    ni.width = width; // Save for the "mouse" later on...
    ni.height = height;
  }

  protected void drawLine(GC gc, JobHopMeta hop, boolean is_candidate) {
    int line[] = getLine(hop.from_entry, hop.to_entry);

    gc.setLineWidth(linewidth);
    Color col;

    if (hop.from_entry.isLaunchingInParallel()) {
      gc.setLineAttributes(new LineAttributes((float) linewidth, SWT.CAP_FLAT, SWT.JOIN_MITER, SWT.LINE_CUSTOM,
          new float[] { 5, 3, }, 0, 10));
    } else {
      gc.setLineStyle(SWT.LINE_SOLID);
    }

    if (is_candidate) {
      col = GUIResource.getInstance().getColorBlue();
    } else if (hop.isEnabled()) {
      if (hop.isUnconditional()) {
        col = GUIResource.getInstance().getColorBlack();
      } else {
        if (hop.getEvaluation()) {
          col = GUIResource.getInstance().getColorGreen();
        } else {
          col = GUIResource.getInstance().getColorRed();
        }
      }
    } else {
      col = GUIResource.getInstance().getColorGray();
    }

    gc.setForeground(col);

    if (hop.isSplit())
      gc.setLineWidth(linewidth + 2);
    drawArrow(gc, line);
    if (hop.isSplit())
      gc.setLineWidth(linewidth);

    gc.setForeground(GUIResource.getInstance().getColorBlack());
    gc.setBackground(GUIResource.getInstance().getColorBackground());
    gc.setLineStyle(SWT.LINE_SOLID);
  }

  protected Point getArea() {
    org.eclipse.swt.graphics.Rectangle rect = canvas.getClientArea();
    Point area = new Point(rect.width, rect.height);

    return area;
  }

  private Point magnifyPoint(Point p) {
    return new Point(Math.round(p.x * magnification), Math.round(p.y * magnification));
  }

  private Point getThumb(Point area, Point transMax) {
    Point resizedMax = magnifyPoint(transMax);

    Point thumb = new Point(0, 0);
    if (resizedMax.x <= area.x)
      thumb.x = 100;
    else
      thumb.x = 100 * area.x / resizedMax.x;

    if (resizedMax.y <= area.y)
      thumb.y = 100;
    else
      thumb.y = 100 * area.y / resizedMax.y;

    return thumb;
  }

  protected Point getOffset() {
    Point area = getArea();
    Point max = jobMeta.getMaximum();
    Point thumb = getThumb(area, max);

    return getOffset(thumb, area);

  }

  protected Point getOffset(Point thumb, Point area) {
    Point p = new Point(0, 0);
    Point sel = new Point(hori.getSelection(), vert.getSelection());

    if (thumb.x == 0 || thumb.y == 0)
      return p;

    p.x = -sel.x * area.x / thumb.x;
    p.y = -sel.y * area.y / thumb.y;

    return p;
  }

  public int sign(int n) {
    return n < 0 ? -1 : (n > 0 ? 1 : 1);
  }

  protected void newHop() {
    JobEntryCopy fr = jobMeta.getSelected(0);
    JobEntryCopy to = jobMeta.getSelected(1);
    spoon.newJobHop(jobMeta, fr, to);
  }

  protected void editEntry(JobEntryCopy je) {
    spoon.editJobEntry(jobMeta, je);
  }

  protected void editNote(NotePadMeta ni) {
    NotePadMeta before = (NotePadMeta) ni.clone();
    String title = Messages.getString("JobGraph.Dialog.EditNote.Title");
    String message = Messages.getString("JobGraph.Dialog.EditNote.Message");
    EnterTextDialog dd = new EnterTextDialog(shell, title, message, ni.getNote());
    String n = dd.open();
    if (n != null) {
      spoon.addUndoChange(jobMeta, new NotePadMeta[] { before }, new NotePadMeta[] { ni }, new int[] { jobMeta
          .indexOfNote(ni) });
      ni.setChanged();
      ni.setNote(n);
      ni.width = ConstUI.NOTE_MIN_SIZE;
      ni.height = ConstUI.NOTE_MIN_SIZE;
      spoon.refreshGraph();
    }
  }

  protected void drawArrow(GC gc, int line[]) {
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

    //gc.setLineWidth(1);
    //WuLine(gc, black, x1, y1, x2, y2);

    gc.drawLine(x1, y1, x2, y2);

    // What's the distance between the 2 points?
    a = Math.abs(x2 - x1);
    b = Math.abs(y2 - y1);
    dist = (int) Math.sqrt(a * a + b * b);

    // determine factor (position of arrow to left side or right side 0-->100%)
    if (dist >= 2 * iconsize)
      factor = 1.5;
    else
      factor = 1.2;

    // in between 2 points
    mx = (int) (x1 + factor * (x2 - x1) / 2);
    my = (int) (y1 + factor * (y2 - y1) / 2);

    // calculate points for arrowhead
    angle = Math.atan2(y2 - y1, x2 - x1) + Math.PI;

    x3 = (int) (mx + Math.cos(angle - theta) * size);
    y3 = (int) (my + Math.sin(angle - theta) * size);

    x4 = (int) (mx + Math.cos(angle + theta) * size);
    y4 = (int) (my + Math.sin(angle + theta) * size);

    // draw arrowhead
    //gc.drawLine(mx, my, x3, y3);
    //gc.drawLine(mx, my, x4, y4);
    //gc.drawLine( x3, y3, x4, y4 );
    Color fore = gc.getForeground();
    Color back = gc.getBackground();
    gc.setBackground(fore);
    gc.fillPolygon(new int[] { mx, my, x3, y3, x4, y4 });
    gc.setBackground(back);
  }

  protected boolean pointOnLine(int x, int y, int line[]) {
    int dx, dy;
    int pm = HOP_SEL_MARGIN / 2;
    boolean retval = false;

    for (dx = -pm; dx <= pm && !retval; dx++) {
      for (dy = -pm; dy <= pm && !retval; dy++) {
        retval = pointOnThinLine(x + dx, y + dy, line);
      }
    }

    return retval;
  }

  protected boolean pointOnThinLine(int x, int y, int line[]) {
    int x1 = line[0];
    int y1 = line[1];
    int x2 = line[2];
    int y2 = line[3];

    // Not in the square formed by these 2 points: ignore!		
    if (!(((x >= x1 && x <= x2) || (x >= x2 && x <= x1)) && ((y >= y1 && y <= y2) || (y >= y2 && y <= y1))))
      return false;

    double angle_line = Math.atan2(y2 - y1, x2 - x1) + Math.PI;
    double angle_point = Math.atan2(y - y1, x - x1) + Math.PI;

    // Same angle, or close enough?
    if (angle_point >= angle_line - 0.01 && angle_point <= angle_line + 0.01)
      return true;

    return false;
  }

  protected SnapAllignDistribute createSnapAllignDistribute() {
    List<GUIPositionInterface> elements = jobMeta.getSelectedDrawnJobEntryList();
    int[] indices = jobMeta.getEntryIndexes(elements.toArray(new JobEntryCopy[elements.size()]));

    return new SnapAllignDistribute(jobMeta, elements, indices, spoon, this);
  }

  public void snaptogrid() {
    snaptogrid(ConstUI.GRID_SIZE);
  }

  protected void snaptogrid(int size) {
    createSnapAllignDistribute().snaptogrid(size);
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

  public void zoomIn() {
    /*    	if (magnificationIndex+1<TransPainter.magnifications.length) {
        		magnification = TransPainter.magnifications[++magnificationIndex];
        	}
    */
    magnification += .1f;
    redraw();
  }

  public void zoomOut() {
    /*    	if (magnificationIndex>0) {
        		magnification = TransPainter.magnifications[--magnificationIndex];
        	}
    */
    magnification -= .1f;
    redraw();
  }

  public void zoom100Percent() {
    //magnificationIndex=TransPainter.MAGNIFICATION_100_PERCENT_INDEX;
    //magnification = TransPainter.magnifications[magnificationIndex];
    magnification = 1.0f;
    redraw();
  }

  protected void drawRect(GC gc, Rectangle rect) {
    if (rect == null)
      return;

    gc.setLineStyle(SWT.LINE_DASHDOT);
    gc.setLineWidth(1);
    gc.setForeground(GUIResource.getInstance().getColorDarkGray());
    gc.drawRectangle(rect.x + offset.x, rect.y + offset.y, rect.width, rect.height);
    gc.setLineStyle(SWT.LINE_SOLID);
  }

  protected void detach(JobEntryCopy je) {
    JobHopMeta hfrom = jobMeta.findJobHopTo(je);
    JobHopMeta hto = jobMeta.findJobHopFrom(je);

    if (hfrom != null && hto != null) {
      if (jobMeta.findJobHop(hfrom.from_entry, hto.to_entry) == null) {
        JobHopMeta hnew = new JobHopMeta(hfrom.from_entry, hto.to_entry);
        jobMeta.addJobHop(hnew);
        spoon.addUndoNew(jobMeta, new JobHopMeta[] { (JobHopMeta) hnew.clone() }, new int[] { jobMeta
            .indexOfJobHop(hnew) });
      }
    }
    if (hfrom != null) {
      int fromidx = jobMeta.indexOfJobHop(hfrom);
      if (fromidx >= 0) {
        jobMeta.removeJobHop(fromidx);
        spoon.addUndoDelete(jobMeta, new JobHopMeta[] { hfrom }, new int[] { fromidx });
      }
    }
    if (hto != null) {
      int toidx = jobMeta.indexOfJobHop(hto);
      if (toidx >= 0) {
        jobMeta.removeJobHop(toidx);
        spoon.addUndoDelete(jobMeta, new JobHopMeta[] { hto }, new int[] { toidx });
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
    return Spoon.APP_NAME;
  }

  public EngineMetaInterface getMeta() {
    return jobMeta;
  }

  /**
   * @return the jobMeta
   * /
  public JobMeta getJobMeta()
  {
      return jobMeta;
  }

  /**
   * @param jobMeta the jobMeta to set
   */
  public void setJobMeta(JobMeta jobMeta) {
    this.jobMeta = jobMeta;
  }

  public boolean applyChanges() {
    return spoon.saveToFile(jobMeta);
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

  public int showChangedWarning() {
    MessageBox mb = new MessageBox(shell, SWT.YES | SWT.NO | SWT.CANCEL | SWT.ICON_WARNING);
    mb.setMessage(Messages.getString("Spoon.Dialog.FileChangedSaveFirst.Message", spoon.delegates.tabs
        .makeJobGraphTabName(jobMeta)));//"This model has changed.  Do you want to save it?"
    mb.setText(Messages.getString("Spoon.Dialog.FileChangedSaveFirst.Title"));
    return mb.open();
  }

  public static int showChangedWarning(Shell shell, String name) {
    MessageBox mb = new MessageBox(shell, SWT.YES | SWT.NO | SWT.CANCEL | SWT.ICON_WARNING);
    mb.setMessage(Messages.getString("JobGraph.Dialog.PromptSave.Message", name));
    mb.setText(Messages.getString("JobGraph.Dialog.PromptSave.Title"));
    return mb.open();
  }

  public static boolean editProperties(JobMeta jobMeta, Spoon spoon, Repository rep, boolean allowDirectoryChange) {
    if (jobMeta == null)
      return false;

    JobDialog jd = new JobDialog(spoon.getShell(), SWT.NONE, jobMeta, rep);
    jd.setDirectoryChangeAllowed(allowDirectoryChange);
    JobMeta ji = jd.open();

    // In this case, load shared objects
    //
    if (jd.isSharedObjectsFileChanged()) {
      try {
        SharedObjects sharedObjects = jobMeta.readSharedObjects(rep);
        spoon.sharedObjectsFileMap.put(sharedObjects.getFilename(), sharedObjects);
      } catch (Exception e) {
        new ErrorDialog(spoon.getShell(), Messages.getString("Spoon.Dialog.ErrorReadingSharedObjects.Title"), 
        		Messages.getString("Spoon.Dialog.ErrorReadingSharedObjects.Message", spoon.delegates.tabs
                .makeJobGraphTabName(jobMeta)), e);
      }
    }
    
    // If we added properties, add them to the variables too, so that they appear in the CTRL-SPACE variable completion.
    //
    spoon.setParametersAsVariablesInUI(jobMeta, jobMeta);

    if (jd.isSharedObjectsFileChanged() || ji != null) {
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
   * @param lastMove the lastMove to set
   */
  public void setLastMove(Point lastMove) {
    this.lastMove = lastMove;
  }

  /**
   * Add an extra view to the main composite SashForm
   */
  public void addExtraView() {
    extraViewComposite = new Composite(sashForm, SWT.NONE);
    FormLayout extraCompositeFormLayout = new FormLayout();
    extraCompositeFormLayout.marginWidth = 2;
    extraCompositeFormLayout.marginHeight = 2;
    extraViewComposite.setLayout(extraCompositeFormLayout);

    // Put a close and max button to the upper right corner...
    //
    closeButton = new Label(extraViewComposite, SWT.NONE);
    closeButton.setImage(GUIResource.getInstance().getImageClosePanel());
    closeButton.setToolTipText(Messages.getString("JobGraph.ExecutionResultsPanel.CloseButton.Tooltip"));
    FormData fdClose = new FormData();
    fdClose.right = new FormAttachment(100, 0);
    fdClose.top = new FormAttachment(0, 0);
    closeButton.setLayoutData(fdClose);
    closeButton.addMouseListener(new MouseAdapter() {
      public void mouseDown(MouseEvent e) {
        disposeExtraView();
      }
    });

    minMaxButton = new Label(extraViewComposite, SWT.NONE);
    minMaxButton.setImage(GUIResource.getInstance().getImageMaximizePanel());
    minMaxButton.setToolTipText(Messages.getString("JobGraph.ExecutionResultsPanel.MaxButton.Tooltip"));
    FormData fdMinMax = new FormData();
    fdMinMax.right = new FormAttachment(closeButton, -Const.MARGIN);
    fdMinMax.top = new FormAttachment(0, 0);
    minMaxButton.setLayoutData(fdMinMax);
    minMaxButton.addMouseListener(new MouseAdapter() {
      public void mouseDown(MouseEvent e) {
        minMaxExtraView();
      }
    });

    // Add a label at the top: Results
    //
    Label wResultsLabel = new Label(extraViewComposite, SWT.LEFT);
    wResultsLabel.setFont(GUIResource.getInstance().getFontMediumBold());
    wResultsLabel.setBackground(GUIResource.getInstance().getColorLightGray());
    wResultsLabel.setText(Messages.getString("JobLog.ResultsPanel.NameLabel"));
    FormData fdResultsLabel = new FormData();
    fdResultsLabel.left = new FormAttachment(0, 0);
    fdResultsLabel.right = new FormAttachment(100, 0);
    fdResultsLabel.top = new FormAttachment(0, 0);
    wResultsLabel.setLayoutData(fdResultsLabel);

    // Add a tab folder ...
    //
    extraViewTabFolder = new CTabFolder(extraViewComposite, SWT.MULTI);
    spoon.props.setLook(extraViewTabFolder, Props.WIDGET_STYLE_TAB);

    extraViewTabFolder.addMouseListener(new MouseAdapter() {

      @Override
      public void mouseDoubleClick(MouseEvent arg0) {
        if (sashForm.getMaximizedControl() == null) {
          sashForm.setMaximizedControl(extraViewComposite);
        } else {
          sashForm.setMaximizedControl(null);
        }
      }

    });

    FormData fdTabFolder = new FormData();
    fdTabFolder.left = new FormAttachment(0, 0);
    fdTabFolder.right = new FormAttachment(100, 0);
    fdTabFolder.top = new FormAttachment(wResultsLabel, Const.MARGIN);
    fdTabFolder.bottom = new FormAttachment(100, 0);
    extraViewTabFolder.setLayoutData(fdTabFolder);

    sashForm.setWeights(new int[] { 60, 40, });
  }

  /**
   * If the extra tab view at the bottom is empty, we close it.
   */
  public void checkEmptyExtraView() {
    if (extraViewTabFolder.getItemCount() == 0) {
      disposeExtraView();
    }
  }

  private void disposeExtraView() {
    extraViewComposite.dispose();
    sashForm.layout();
    sashForm.setWeights(new int[] { 100, });
    
    XulToolbarbutton button = (XulToolbarbutton) toolbar.getElementById("job-show-results");
    button.setTooltiptext(Messages.getString("Spoon.Tooltip.ShowExecutionResults"));
    ToolItem swtToolItem = (ToolItem) button.getManagedObject();
    swtToolItem.setImage(GUIResource.getInstance().getImageShowResults());
  }

  private void minMaxExtraView() {
    // What is the state?
    //
    boolean maximized = sashForm.getMaximizedControl() != null;
    if (maximized) {
      // Minimize 
      //
      sashForm.setMaximizedControl(null);
      minMaxButton.setImage(GUIResource.getInstance().getImageMaximizePanel());
      minMaxButton.setToolTipText(Messages.getString("JobGraph.ExecutionResultsPanel.MaxButton.Tooltip"));
    } else {
      // Maximize
      //
      sashForm.setMaximizedControl(extraViewComposite);
      minMaxButton.setImage(GUIResource.getInstance().getImageMinimizePanel());
      minMaxButton.setToolTipText(Messages.getString("JobGraph.ExecutionResultsPanel.MinButton.Tooltip"));
    }
  }

  public void showExecutionResults() {
    if (extraViewComposite == null || extraViewComposite.isDisposed()) {
      addAllTabs();
    } else {
      disposeExtraView();
    }
  }

  public void addAllTabs() {
	  
	CTabItem tabItemSelection = null;
	if (extraViewTabFolder!=null && !extraViewTabFolder.isDisposed()) {
		tabItemSelection = extraViewTabFolder.getSelection();
	}

    jobHistoryDelegate.addJobHistory();
    jobLogDelegate.addJobLog();
    jobGridDelegate.addJobGrid();
    
    if (tabItemSelection!=null) {
    	extraViewTabFolder.setSelection(tabItemSelection);
    } else {
    	extraViewTabFolder.setSelection(jobGridDelegate.getJobGridTab());
    }
    
    XulToolbarbutton button = (XulToolbarbutton) toolbar.getElementById("job-show-results");
    button.setTooltiptext(Messages.getString("Spoon.Tooltip.HideExecutionResults"));
    ToolItem swtToolItem = (ToolItem) button.getManagedObject();
    swtToolItem.setImage(GUIResource.getInstance().getImageHideResults());
  }

  public void openFile() {
    spoon.openFile();
  }

  public void saveFile() {
    spoon.saveFile();
  }

  public void saveFileAs() {
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

  public void getSQL() {
    spoon.getSQL();
  }

  public void exploreDatabase() {
    spoon.exploreDatabase();
  }

  public synchronized void startJob(JobExecutionConfiguration executionConfiguration) {
    if (job == null) // Not running, start the transformation...
    {
      // Auto save feature...
      if (jobMeta.hasChanged()) {
        if (spoon.props.getAutoSave()) {
        	if(log.isDetailed()) log.logDetailed(toString(), Messages.getString("JobLog.Log.AutoSaveFileBeforeRunning")); //$NON-NLS-1$
          System.out.println(Messages.getString("JobLog.Log.AutoSaveFileBeforeRunning2")); //$NON-NLS-1$
          spoon.saveToFile(jobMeta);
        } else {
          MessageDialogWithToggle md = new MessageDialogWithToggle(
              shell,
              Messages.getString("JobLog.Dialog.SaveChangedFile.Title"), //$NON-NLS-1$
              null,
              Messages.getString("JobLog.Dialog.SaveChangedFile.Message") + Const.CR + Messages.getString("JobLog.Dialog.SaveChangedFile.Message2") + Const.CR, //$NON-NLS-1$ //$NON-NLS-2$
              MessageDialog.QUESTION, new String[] {
                  Messages.getString("System.Button.Yes"), Messages.getString("System.Button.No") }, //$NON-NLS-1$ //$NON-NLS-2$
              0, Messages.getString("JobLog.Dialog.SaveChangedFile.Toggle"), //$NON-NLS-1$
              spoon.props.getAutoSave());
          int answer = md.open();
          if ((answer & 0xFF) == 0) {
            spoon.saveToFile(jobMeta);
          }
          spoon.props.setAutoSave(md.getToggleState());
        }
      }

      if (((jobMeta.getName() != null && spoon.rep != null) || // Repository available & name set
          (jobMeta.getFilename() != null && spoon.rep == null) // No repository & filename set
          )
          && !jobMeta.hasChanged() // Didn't change
      ) {
        if (job == null || (job != null && !job.isActive())) {
          try {
        	  
        	// Make sure we clear the log before executing again...
        	//
            if (executionConfiguration.isClearingLog()) {
            	jobLogDelegate.clearLog();
            }
        	  
            job = new Job(log, jobMeta.getName(), jobMeta.getFilename(), null);
            job.open(spoon.rep, jobMeta.getFilename(), jobMeta.getName(), jobMeta.getDirectory().getPath(), spoon);
            job.getJobMeta().setArguments(jobMeta.getArguments());
            job.shareVariablesWith(jobMeta);
            
            // Set the named parameters
            Map<String, String> paramMap = executionConfiguration.getParams();
            Set<String> keys = paramMap.keySet();
            for ( String key : keys )  {
            	job.getJobMeta().setParameterValue(key, Const.NVL(paramMap.get(key), ""));
            } 
            job.getJobMeta().activateParameters();
            
            log.logMinimal(Spoon.APP_NAME, Messages.getString("JobLog.Log.StartingJob")); //$NON-NLS-1$
            job.start();
            jobGridDelegate.previousNrItems = -1;
            // Link to the new jobTracker!
            jobGridDelegate.jobTracker = job.getJobTracker();
            running = true;

            // Attach a listener to notify us that the transformation has finished.
            job.addJobListener(new JobListener() {

              public void jobFinished(Job job) {
                JobGraph.this.jobFinished();
              }

            });

            // Show the execution results views
            //
            addAllTabs();
          } catch (KettleException e) {
            new ErrorDialog(
                shell,
                Messages.getString("JobLog.Dialog.CanNotOpenJob.Title"), Messages.getString("JobLog.Dialog.CanNotOpenJob.Message"), e); //$NON-NLS-1$ //$NON-NLS-2$
            job = null;
          }
        } else {
          MessageBox m = new MessageBox(shell, SWT.OK | SWT.ICON_WARNING);
          m.setText(Messages.getString("JobLog.Dialog.JobIsAlreadyRunning.Title")); //$NON-NLS-1$
          m.setMessage(Messages.getString("JobLog.Dialog.JobIsAlreadyRunning.Message")); //$NON-NLS-1$
          m.open();
        }
      } else {
        if (jobMeta.hasChanged()) {
          MessageBox m = new MessageBox(shell, SWT.OK | SWT.ICON_WARNING);
          m.setText(Messages.getString("JobLog.Dialog.JobHasChangedSave.Title")); //$NON-NLS-1$
          m.setMessage(Messages.getString("JobLog.Dialog.JobHasChangedSave.Message")); //$NON-NLS-1$
          m.open();
        } else if (spoon.rep != null && jobMeta.getName() == null) {
          MessageBox m = new MessageBox(shell, SWT.OK | SWT.ICON_WARNING);
          m.setText(Messages.getString("JobLog.Dialog.PleaseGiveThisJobAName.Title")); //$NON-NLS-1$
          m.setMessage(Messages.getString("JobLog.Dialog.PleaseGiveThisJobAName.Message")); //$NON-NLS-1$
          m.open();
        } else {
          MessageBox m = new MessageBox(shell, SWT.OK | SWT.ICON_WARNING);
          m.setText(Messages.getString("JobLog.Dialog.NoFilenameSaveYourJobFirst.Title")); //$NON-NLS-1$
          m.setMessage(Messages.getString("JobLog.Dialog.NoFilenameSaveYourJobFirst.Message")); //$NON-NLS-1$
          m.open();
        }
      }
      setControlStates();
    }
  }

  /**
   * This gets called at the very end, when everything is done.
   */
  protected void jobFinished() {
    // Do a final check to see if it all ended...
    //
    if (running && job != null && job.isInitialized() && job.isFinished()) {
      job = null;
      running = false;
      for (RefreshListener listener : refreshListeners)
        listener.refreshNeeded();
      log.logMinimal(Spoon.APP_NAME, Messages.getString("JobLog.Log.JobHasEnded")); //$NON-NLS-1$
    }
    setControlStates();
  }

  public synchronized void stopJob() {
    try {
      if (job != null && running && job.isInitialized()) {
        job.stopAll();
        job.endProcessing("stop", new Result()); //$NON-NLS-1$
        job.waitUntilFinished(5000); // wait until everything is stopped, maximum 5 seconds...
        job = null;
        running = false;
        log.logMinimal(Spoon.APP_NAME, Messages.getString("JobLog.Log.JobWasStopped")); //$NON-NLS-1$
      }
    } catch (KettleJobException je) {
      MessageBox m = new MessageBox(shell, SWT.OK | SWT.ICON_WARNING);
      m.setText(Messages.getString("JobLog.Dialog.UnableToSaveStopLineInLoggingTable.Title")); //$NON-NLS-1$
      m
          .setMessage(Messages.getString("JobLog.Dialog.UnableToSaveStopLineInLoggingTable.Message") + Const.CR + je.toString()); //$NON-NLS-1$
      m.open();
    } finally {
      setControlStates();
    }
  }

  private void setControlStates() {
	  if (getDisplay().isDisposed()) return;
	  
      getDisplay().asyncExec(new Runnable() {

      public void run() {
        // Start/Run button...
        //
        XulToolbarbutton runButton = (XulToolbarbutton) toolbar.getElementById("job-run");
        if (runButton != null) {
          runButton.setDisabled(running);
        }

        /* TODO add pause button
         *
        // Pause button...
        //
        XulToolbarButton pauseButton = toolbar.getButtonById("trans-pause");
        if (pauseButton!=null)
        {
        	pauseButton.setEnable(running);
        	pauseButton.setText( pausing ? RESUME_TEXT : PAUSE_TEXT );
        	pauseButton.setHint( pausing ? Messages.getString("Spoon.Tooltip.ResumeTranformation") : Messages.getString("Spoon.Tooltip.PauseTranformation"));
        }
        */
        // Stop button...
        //
        XulToolbarbutton stopButton = (XulToolbarbutton) toolbar.getElementById("job-stop");
        if (stopButton != null) {
          stopButton.setDisabled(!running);
        }

        // TODO: enable/disable Job menu entries too
      }

    });

  }

  /**
   * @return the refresh listeners
   */
  public List<RefreshListener> getRefreshListeners() {
    return refreshListeners;
  }

  /**
   * @param refreshListeners the refresh listeners to set
   */
  public void setRefreshListeners(List<RefreshListener> refreshListeners) {
    this.refreshListeners = refreshListeners;
  }

  /**
   * @param refreshListener the job refresh listener to add
   */
  public void addRefreshListener(RefreshListener refreshListener) {
    refreshListeners.add(refreshListener);
  }

  /* (non-Javadoc)
   * @see org.pentaho.ui.xul.impl.XulEventHandler#getName()
   */
  public String getName() {
    // TODO Auto-generated method stub
    return "jobgraph";
  }

  /* (non-Javadoc)
   * @see org.pentaho.ui.xul.impl.XulEventHandler#getXulDomContainer()
   */
  public XulDomContainer getXulDomContainer() {
    // TODO Auto-generated method stub
    return null;
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

  public boolean canHandleSave() {
    return true;
  }
  
  
}