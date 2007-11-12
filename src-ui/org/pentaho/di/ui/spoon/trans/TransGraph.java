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

package org.pentaho.di.ui.spoon.trans;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.window.DefaultToolTip;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.EngineMetaInterface;
import org.pentaho.di.core.NotePadMeta;
import org.pentaho.di.core.dnd.DragAndDropContainer;
import org.pentaho.di.core.dnd.XMLTransfer;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.gui.GUIPositionInterface;
import org.pentaho.di.core.gui.Point;
import org.pentaho.di.core.gui.Redrawable;
import org.pentaho.di.core.gui.SnapAllignDistribute;
import org.pentaho.di.core.gui.SpoonInterface;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.i18n.LanguageChoice;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.DatabaseImpact;
import org.pentaho.di.trans.StepLoader;
import org.pentaho.di.trans.StepPlugin;
import org.pentaho.di.trans.TransHopMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.RemoteStep;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.tableinput.TableInputMeta;
import org.pentaho.di.ui.core.ConstUI;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.dialog.EnterNumberDialog;
import org.pentaho.di.ui.core.dialog.EnterTextDialog;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.dialog.StepFieldsDialog;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.gui.XulHelper;
import org.pentaho.di.ui.spoon.AreaOwner;
import org.pentaho.di.ui.spoon.Messages;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.ui.spoon.TabItemInterface;
import org.pentaho.di.ui.spoon.TransPainter;
import org.pentaho.di.ui.spoon.XulMessages;
import org.pentaho.di.ui.spoon.dialog.DeleteMessageBox;
import org.pentaho.di.ui.spoon.dialog.SearchFieldsProgressDialog;
import org.pentaho.di.ui.trans.dialog.TransDialog;
import org.pentaho.xul.menu.XulMenu;
import org.pentaho.xul.menu.XulMenuChoice;
import org.pentaho.xul.menu.XulPopupMenu;
import org.pentaho.xul.swt.menu.MenuChoice;


/**
 * This class handles the display of the transformations in a graphical way using icons, arrows, etc.
 * One transformation is handled per TransGraph
 * 
 * @author Matt
 * @since 17-mei-2003
 * 
 */
public class TransGraph extends Composite implements Redrawable, TabItemInterface
{
    private static final LogWriter log = LogWriter.getInstance();
    private static final int HOP_SEL_MARGIN = 9;

    private TransMeta        transMeta;

    private Shell            shell;

    private Canvas           canvas;
    
    private DefaultToolTip   toolTip;
    
    // private Props            props;

    private int              iconsize;

    private Point            lastclick;

    private Point            lastMove;

    private Point            previous_step_locations[];

    private Point            previous_note_locations[];

    private StepMeta         selected_steps[];

    private StepMeta         selected_step;

    private NotePadMeta      selected_notes[];

    private NotePadMeta      selected_note;

    private TransHopMeta     candidate;

    private Point            drop_candidate;

    private Spoon            spoon;

    private Point            offset, iconoffset, noteoffset;

    private ScrollBar        hori;

    private ScrollBar        vert;

    // public boolean           shift, control;

    private boolean          split_hop;

    private int              last_button;

    private TransHopMeta     last_hop_split;

    private Rectangle        selrect;

    /**
     * A list of remarks on the current Transformation...
     */
    private List<CheckResultInterface> remarks;
    
    /**
     * A list of impacts of the current transformation on the used databases.
     */
    private List<DatabaseImpact> impact;

    /**
     * Indicates whether or not an impact analyses has already run.
     */
    private boolean impactFinished;

    protected static Map<String, org.pentaho.xul.swt.menu.Menu> menuMap = new HashMap<String, org.pentaho.xul.swt.menu.Menu>();
	protected int currentMouseX = 0;
	protected int currentMouseY = 0;
	protected NotePadMeta ni = null;
	protected TransHopMeta currentHop;
	protected StepMeta currentStep;
	private List<AreaOwner> areaOwners;

	public void setCurrentNote( NotePadMeta ni ) {
		this.ni = ni;
	}

	public NotePadMeta getCurrentNote() {
		return ni;
	}

    public TransHopMeta getCurrentHop() {
		return currentHop;
	}

	public void setCurrentHop(TransHopMeta currentHop) {
		this.currentHop = currentHop;
	}

	public StepMeta getCurrentStep() {
		return currentStep;
	}

	public void setCurrentStep(StepMeta currentStep) {
		this.currentStep = currentStep;
	}

	public TransGraph(Composite parent, final Spoon spoon, final TransMeta transMeta)
    {
        super(parent, SWT.NONE);
        this.shell = parent.getShell();
        this.spoon = spoon;
        this.transMeta = transMeta;
        
        this.areaOwners = new ArrayList<AreaOwner>();
        
        // this.props = Props.getInstance();
		try {
    		// first get the XML document
    			menuMap = XulHelper.createPopupMenus(SpoonInterface.XUL_FILE_MENUS, shell, new XulMessages(),"trans-graph-hop",
    					"trans-graph-entry" ,"trans-graph-background","trans-graph-note" );
		} catch (Throwable t ) {
			// TODO log this
			t.printStackTrace();
		}
        
        setLayout(new FillLayout());
        
        canvas = new Canvas(this, SWT.V_SCROLL | SWT.H_SCROLL | SWT.NO_BACKGROUND);

        toolTip = new DefaultToolTip(canvas, ToolTip.NO_RECREATE, true);
        // toolTip.setHideOnMouseDown(true);
        toolTip.setRespectMonitorBounds(true);
        toolTip.setRespectDisplayBounds(true);
        toolTip.setPopupDelay(350);
        toolTip.setShift(new org.eclipse.swt.graphics.Point(ConstUI.TOOLTIP_OFFSET,ConstUI.TOOLTIP_OFFSET));
        
        iconsize = spoon.props.getIconSize();

        clearSettings();
        
        
        remarks = new ArrayList<CheckResultInterface>();
        impact  = new ArrayList<DatabaseImpact>();
        impactFinished = false;

        hori = canvas.getHorizontalBar();
        vert = canvas.getVerticalBar();

        hori.addSelectionListener(new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent e)
            {
                redraw();
            }
        });
        vert.addSelectionListener(new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent e)
            {
                redraw();
            }
        });
        hori.setThumb(100);
        vert.setThumb(100);

        hori.setVisible(true);
        vert.setVisible(true);

        setVisible(true);
        newProps();

        canvas.setBackground(GUIResource.getInstance().getColorBackground());

        canvas.addPaintListener(new PaintListener()
        {
            public void paintControl(PaintEvent e)
            {
                if (!spoon.isStopped()) TransGraph.this.paintControl(e);
            }
        });

        selected_steps = null;
        lastclick = null;

        /*
         * Handle the mouse...
         */

        canvas.addMouseListener(new MouseAdapter()
        {
            public void mouseDoubleClick(MouseEvent e)
            {
                clearSettings();
                
                Point real = screen2real(e.x, e.y);

                StepMeta stepMeta = transMeta.getStep(real.x, real.y, iconsize);
                if (stepMeta != null)
                {
                    if (e.button == 1)
                        editStep(stepMeta);
                    else
                        editDescription(stepMeta);
                }
                else
                {
                    // Check if point lies on one of the many hop-lines...
                    TransHopMeta online = findHop(real.x, real.y);
                    if (online != null)
                    {
                        editHop(online);
                    }
                    else
                    {
                        NotePadMeta ni = transMeta.getNote(real.x, real.y);
                        if (ni != null)
                        {
                            selected_note = null;
                            editNote(ni);
                        }
                        else
                        {
                        	// See if the double click was in one of the area's...
                        	//
                        	for (AreaOwner areaOwner : areaOwners) {
                        		if (areaOwner.contains(real.x, real.y)) {
            	            		if ( areaOwner.getParent() instanceof StepMeta && areaOwner.getOwner().equals(TransPainter.STRING_PARTITIONING_CURRENT_STEP) ) {
            	            			StepMeta step = (StepMeta) areaOwner.getParent();
            	            			spoon.editPartitioning(transMeta, step);
            	            			break;
            	            		}
                        		}
                        	}

                        }
                    }
                }
            }

            public void mouseDown(MouseEvent e)
            {
                clearSettings();
                boolean alt     = (e.stateMask & SWT.ALT) != 0;
                boolean control = (e.stateMask & SWT.CONTROL) != 0;
                
                last_button = e.button;
                Point real = screen2real(e.x, e.y);
                lastclick = new Point(real.x, real.y);

                // Hide the tooltip!
                toolTip.hide();

                // Set the pop-up menu
                if (e.button==3)
                {
                    setMenu(real.x, real.y);
                    return;
                }
                
                // Did we click on a step?
                StepMeta stepMeta = transMeta.getStep(real.x, real.y, iconsize);
                if (stepMeta != null)
                {
                    // ALT-Click: edit error handling
                    if (e.button==1 && alt && stepMeta.supportsErrorHandling())
                    {
                        spoon.editStepErrorHandling(transMeta, stepMeta);
                        return;
                    }

                    selected_steps = transMeta.getSelectedSteps();
                    selected_step = stepMeta;
                    // 
                    // When an icon is moved that is not selected, it gets
                    // selected too late.
                    // It is not captured here, but in the mouseMoveListener...
                    previous_step_locations = transMeta.getSelectedStepLocations();

                    Point p = stepMeta.getLocation();
                    iconoffset = new Point(real.x - p.x, real.y - p.y);
                }
                else
                {
                    // Dit we hit a note?
                    NotePadMeta ni = transMeta.getNote(real.x, real.y);
                    if (ni != null && last_button == 1)
                    {
                        selected_notes = transMeta.getSelectedNotes();
                        selected_note = ni;
                        Point loc = ni.getLocation();

                        previous_note_locations = transMeta.getSelectedNoteLocations();

                        noteoffset = new Point(real.x - loc.x, real.y - loc.y);
                    }
                    else
                    {
                        if (!control) selrect = new Rectangle(real.x, real.y, 0, 0);
                    }
                }
                redraw();
            }

            public void mouseUp(MouseEvent e)
            {
                boolean control = (e.stateMask & SWT.CONTROL) != 0;
                
                if (iconoffset == null) iconoffset = new Point(0, 0);
                Point real = screen2real(e.x, e.y);
                Point icon = new Point(real.x - iconoffset.x, real.y - iconoffset.y);

                // Quick new hop option? (drag from one step to another)
                //
                if (candidate != null)
                {
                    if (transMeta.findTransHop(candidate) == null)
                    {
                        spoon.newHop(transMeta, candidate);
                    }
                    candidate = null;
                    selected_steps = null;
                    redraw();
                }
                // Did we select a region on the screen? Mark steps in region as
                // selected
                //
                else
                {
                    if (selrect != null)
                    {
                        selrect.width = real.x - selrect.x;
                        selrect.height = real.y - selrect.y;

                        transMeta.unselectAll();
                        selectInRect(transMeta,selrect);
                        selrect = null;
                        redraw();
                    }
                    // Clicked on an icon?
                    //
                    else
                    {
                        if (selected_step != null)
                        {
                            if (e.button == 1)
                            {
                            	Point realclick = screen2real(e.x, e.y);
                                if (lastclick.x == realclick.x && lastclick.y == realclick.y)
                                {
                                    // Flip selection when control is pressed!
                                    if (control)
                                    {
                                        selected_step.flipSelected();
                                    }
                                    else
                                    {
                                        // Otherwise, select only the icon clicked on!
                                        transMeta.unselectAll();
                                        selected_step.setSelected(true);
                                    }
                                }
                                else
                                {
                                    // Find out which Steps & Notes are selected
                                    selected_steps = transMeta.getSelectedSteps();
                                    selected_notes = transMeta.getSelectedNotes();

                                    // We moved around some items: store undo info...
                                    boolean also = false;
                                    if (selected_notes != null && previous_note_locations != null)
                                    {
                                        int indexes[] = transMeta.getNoteIndexes(selected_notes);
                                        addUndoPosition(selected_notes, indexes, previous_note_locations, transMeta.getSelectedNoteLocations(), also);
                                        also = selected_steps != null && selected_steps.length > 0;
                                    }
                                    if (selected_steps != null && previous_step_locations != null)
                                    {
                                        int indexes[] = transMeta.getStepIndexes(selected_steps);
                                        addUndoPosition(selected_steps, indexes, previous_step_locations, transMeta.getSelectedStepLocations(), also);
                                    }
                                }
                            }

                            // OK, we moved the step, did we move it across a hop?
                            // If so, ask to split the hop!
                            if (split_hop)
                            {
                                TransHopMeta hi = findHop(icon.x + iconsize / 2, icon.y + iconsize / 2, selected_step);
                                if (hi != null)
                                {
                                    int id = 0;
                                    if (!spoon.props.getAutoSplit())
                                    {
                                        MessageDialogWithToggle md = new MessageDialogWithToggle(shell, Messages.getString("TransGraph.Dialog.SplitHop.Title"), null, //$NON-NLS-1$
                                                Messages.getString("TransGraph.Dialog.SplitHop.Message") + Const.CR + hi.toString(), MessageDialog.QUESTION, new String[] { //$NON-NLS-1$
                                                        Messages.getString("System.Button.Yes"), Messages.getString("System.Button.No") }, 0, Messages.getString("TransGraph.Dialog.Option.SplitHop.DoNotAskAgain"), spoon.props.getAutoSplit()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                        id = md.open();
                                        spoon.props.setAutoSplit(md.getToggleState());
                                    }

                                    if ( (id&0xFF) == 0) // Means: "Yes" button clicked!
                                    {
                                    	// Only split A-->--B by putting C in between IF...
                                    	// C-->--A or B-->--C don't exists...
                                    	// A ==> hi.getFromStep()
                                    	// B ==> hi.getToStep();
                                    	// C ==> selected_step
                                    	//
                                    	if (transMeta.findTransHop(selected_step, hi.getFromStep())==null && transMeta.findTransHop(hi.getToStep(), selected_step)==null)
                                    	{
	                                        TransHopMeta newhop1 = new TransHopMeta(hi.getFromStep(), selected_step);
	                                        transMeta.addTransHop(newhop1);
	                                        spoon.addUndoNew(transMeta, new TransHopMeta[] { newhop1 }, new int[] { transMeta.indexOfTransHop(newhop1) }, true);
	                                        TransHopMeta newhop2 = new TransHopMeta(selected_step, hi.getToStep());
	                                        transMeta.addTransHop(newhop2);
	                                        spoon.addUndoNew(transMeta, new TransHopMeta[] { newhop2 }, new int[] { transMeta.indexOfTransHop(newhop2) }, true);
	                                        int idx = transMeta.indexOfTransHop(hi);
	                                        spoon.addUndoDelete(transMeta, new TransHopMeta[] { hi }, new int[] { idx }, true);
	                                        transMeta.removeTransHop(idx);
	                                        spoon.refreshTree();
                                    	}
                                    	else
                                    	{
                                    		// Silently discard this hop-split attempt. 
                                    	}
                                    }
                                }
                                split_hop = false;
                            }

                            selected_steps = null;
                            selected_notes = null;
                            selected_step = null;
                            selected_note = null;
                            redraw();
                        }

                        // Notes?
                        else 
                        {
                            if (selected_note != null)
                            {
                                if (e.button == 1)
                                {
                                    if (lastclick.x == e.x && lastclick.y == e.y)
                                    {
                                        // Flip selection when control is pressed!
                                        if (control)
                                        {
                                            selected_note.flipSelected();
                                        }
                                        else
                                        {
                                            // Otherwise, select only the note clicked on!
                                            transMeta.unselectAll();
                                            selected_note.setSelected(true);
                                        }
                                    }
                                    else
                                    {
                                        // Find out which Steps & Notes are selected
                                        selected_steps = transMeta.getSelectedSteps();
                                        selected_notes = transMeta.getSelectedNotes();

                                        // We moved around some items: store undo info...
                                        boolean also = false;
                                        if (selected_notes != null && previous_note_locations != null)
                                        {
                                            int indexes[] = transMeta.getNoteIndexes(selected_notes);
                                            addUndoPosition(selected_notes, indexes, previous_note_locations, transMeta.getSelectedNoteLocations(), also);
                                            also = selected_steps != null && selected_steps.length > 0;
                                        }
                                        if (selected_steps != null && previous_step_locations != null)
                                        {
                                            int indexes[] = transMeta.getStepIndexes(selected_steps);
                                            addUndoPosition(selected_steps, indexes, previous_step_locations, transMeta.getSelectedStepLocations(), also);
                                        }
                                    }
                                }

                                selected_notes = null;
                                selected_steps = null;
                                selected_step = null;
                                selected_note = null;
                            }
                        }
                    }
                }
                
                last_button = 0;
            }
        });

        canvas.addMouseMoveListener(new MouseMoveListener()
        {
            public void mouseMove(MouseEvent e)
            {
				boolean shift = (e.stateMask & SWT.SHIFT) != 0;

                // Remember the last position of the mouse for paste with keyboard
                lastMove = new Point(e.x, e.y);
                Point real = screen2real(e.x, e.y);

                if (iconoffset == null) iconoffset = new Point(0, 0);
                Point icon = new Point(real.x - iconoffset.x, real.y - iconoffset.y);

                if (noteoffset == null) noteoffset = new Point(0, 0);
                Point note = new Point(real.x - noteoffset.x, real.y - noteoffset.y);

                if (last_button==0) setToolTip(real.x, real.y);
                
                // 
                // First see if the icon we clicked on was selected.
                // If the icon was not selected, we should unselect all other
                // icons,
                // selected and move only the one icon
                if (selected_step != null && !selected_step.isSelected())
                {
                    // System.out.println("STEPS: Unselected all");
                    transMeta.unselectAll();
                    selected_step.setSelected(true);
                    selected_steps = new StepMeta[] { selected_step };
                    previous_step_locations = new Point[] { selected_step.getLocation() };
                }
                if (selected_note != null && !selected_note.isSelected())
                {
                    // System.out.println("NOTES: Unselected all");
                    transMeta.unselectAll();
                    selected_note.setSelected(true);
                    selected_notes = new NotePadMeta[] { selected_note };
                    previous_note_locations = new Point[] { selected_note.getLocation() };
                }

                // Did we select a region...?
                if (selrect != null)
                {
                    selrect.width = real.x - selrect.x;
                    selrect.height = real.y - selrect.y;
                    redraw();
                }
                // Move around steps & notes
                else
                    if (selected_step != null)
                    {
                        if (last_button == 1 && !shift)
                        {
                            /*
                             * One or more icons are selected and moved around...
                             * 
                             * new : new position of the ICON (not the mouse pointer) dx : difference with previous
                             * position
                             */
                            int dx = icon.x - selected_step.getLocation().x;
                            int dy = icon.y - selected_step.getLocation().y;

                            // See if we have a hop-split candidate
                            //
                            TransHopMeta hi = findHop(icon.x + iconsize / 2, icon.y + iconsize / 2, selected_step);
                            if (hi != null)
                            {
                            	// OK, we want to split the hop in 2
                            	// 
                                if (!hi.getFromStep().equals(selected_step) && !hi.getToStep().equals(selected_step))
                                {
                                    split_hop = true;
                                    last_hop_split = hi;
                                    hi.split = true;
                                }
                            }
                            else
                            {
                                if (last_hop_split != null)
                                {
                                    last_hop_split.split = false;
                                    last_hop_split = null;
                                    split_hop = false;
                                }
                            }

                            selected_notes = transMeta.getSelectedNotes();
                            selected_steps = transMeta.getSelectedSteps();

                            // Adjust location of selected steps...
                            if (selected_steps != null) for (int i = 0; i < selected_steps.length; i++)
                            {
                                StepMeta stepMeta = selected_steps[i];
                                stepMeta.setLocation(stepMeta.getLocation().x + dx, stepMeta.getLocation().y + dy);
                            }
                            // Adjust location of selected hops...
                            if (selected_notes != null) for (int i = 0; i < selected_notes.length; i++)
                            {
                                NotePadMeta ni = selected_notes[i];
                                ni.setLocation(ni.getLocation().x + dx, ni.getLocation().y + dy);
                            }

                            redraw();
                        }
                        // The middle button perhaps?
                        else
                            if (last_button == 2 || (last_button == 1 && shift))
                            {
                                StepMeta stepMeta = transMeta.getStep(real.x, real.y, iconsize);
                                if (stepMeta != null && !selected_step.equals(stepMeta))
                                {
                                    if (candidate == null)
                                    {
                                        candidate = new TransHopMeta(selected_step, stepMeta);
                                        redraw();
                                    }
                                }
                                else
                                {
                                    if (candidate != null)
                                    {
                                        candidate = null;
                                        redraw();
                                    }
                                }
                            }
                    }
                    // Move around notes & steps
                    else
                        if (selected_note != null)
                        {
                            if (last_button == 1 && !shift)
                            {
                                /*
                                 * One or more notes are selected and moved around...
                                 * 
                                 * new : new position of the note (not the mouse pointer) dx : difference with previous
                                 * position
                                 */
                                int dx = note.x - selected_note.getLocation().x;
                                int dy = note.y - selected_note.getLocation().y;

                                selected_notes = transMeta.getSelectedNotes();
                                selected_steps = transMeta.getSelectedSteps();

                                // Adjust location of selected steps...
                                if (selected_steps != null) for (int i = 0; i < selected_steps.length; i++)
                                {
                                    StepMeta stepMeta = selected_steps[i];
                                    stepMeta.setLocation(stepMeta.getLocation().x + dx, stepMeta.getLocation().y + dy);
                                }
                                // Adjust location of selected hops...
                                if (selected_notes != null) for (int i = 0; i < selected_notes.length; i++)
                                {
                                    NotePadMeta ni = selected_notes[i];
                                    ni.setLocation(ni.getLocation().x + dx, ni.getLocation().y + dy);
                                }

                                redraw();
                            }
                        }
            }
        });

        // Drag & Drop for steps
        Transfer[] ttypes = new Transfer[] { XMLTransfer.getInstance() };
        DropTarget ddTarget = new DropTarget(canvas, DND.DROP_MOVE);
        ddTarget.setTransfer(ttypes);
        ddTarget.addDropListener(new DropTargetListener()
            {
                public void dragEnter(DropTargetEvent event)
                {
                    clearSettings();
    
                    drop_candidate = getRealPosition(canvas, event.x, event.y);
    
                    redraw();
                }
    
                public void dragLeave(DropTargetEvent event)
                {
                    drop_candidate = null;
                    redraw();
                }
    
                public void dragOperationChanged(DropTargetEvent event)
                {
                }
    
                public void dragOver(DropTargetEvent event)
                {
                    drop_candidate = getRealPosition(canvas, event.x, event.y);
    
                    redraw();
                }
    
                public void drop(DropTargetEvent event)
                {
                    // no data to copy, indicate failure in event.detail
                    if (event.data == null)
                    {
                        event.detail = DND.DROP_NONE;
                        return;
                    }
    
                    // System.out.println("Dropping a step!!");
    
                    // What's the real drop position?
                    Point p = getRealPosition(canvas, event.x, event.y);
    
                    // 
                    // We expect a Drag and Drop container... (encased in XML)
                    try
                    {
                        DragAndDropContainer container = (DragAndDropContainer)event.data;
    	                
    	                StepMeta stepMeta = null;
    	                boolean newstep = false;
    	                
    	                switch(container.getType())
    	                {
    	                // Put an existing one on the canvas.
    	                case DragAndDropContainer.TYPE_STEP:
    		                {
    	                		// Drop hidden step onto canvas....		                
    		                	stepMeta = transMeta.findStep(container.getData());
    		                	if (stepMeta!=null)
    		                	{
    	    	                    if (stepMeta.isDrawn() || transMeta.isStepUsedInTransHops(stepMeta))
    	    	                    {
    	    	                        MessageBox mb = new MessageBox(shell, SWT.OK);
    	    	                        mb.setMessage(Messages.getString("TransGraph.Dialog.StepIsAlreadyOnCanvas.Message")); //$NON-NLS-1$
    	    	                        mb.setText(Messages.getString("TransGraph.Dialog.StepIsAlreadyOnCanvas.Title")); //$NON-NLS-1$
    	    	                        mb.open();
    	    	                        return;
    	    	                    }
    	    	                    // This step gets the drawn attribute and position set below.
    		                	}
    		                	else
    		                	{
    		                		// Unknown step dropped: ignore this to be safe!
    		                		return;
    		                	}
    		                }
    		                break;
    						
    	                // Create a new step 
    	                case DragAndDropContainer.TYPE_BASE_STEP_TYPE:
    						{
                    			// Not an existing step: data refers to the type of step to create
                    			String steptype = container.getData();
                    			stepMeta = spoon.newStep(transMeta, steptype, steptype, false, true);
                    			if (stepMeta!=null)
                    			{
                    				newstep=true;
                    			}
                    			else
                    			{
                    				return; // Cancelled pressed in dialog or unable to create step.
                    			}
    						}
    						break;
    						
    					// Create a new TableInput step using the selected connection...
    	                case DragAndDropContainer.TYPE_DATABASE_CONNECTION: 
    	                    {
    	                        newstep = true;
    	                        String connectionName = container.getData();
    	                        TableInputMeta tii = new TableInputMeta();
    	                        tii.setDatabaseMeta(transMeta.findDatabase(connectionName));
    	
    	                        StepLoader steploader = StepLoader.getInstance();
    	                        String stepID = steploader.getStepPluginID(tii);
    	                        StepPlugin stepPlugin = steploader.findStepPluginWithID(stepID);
    	                        String stepName = transMeta.getAlternativeStepname(stepPlugin.getDescription());
    	                        stepMeta = new StepMeta(stepID, stepName, tii);
    	                        if (spoon.editStep(transMeta, stepMeta) != null)
    	                        {
                                    transMeta.addStep(stepMeta);
    	                            spoon.refreshTree();
    	                            spoon.refreshGraph();
    	                        }            
    	                        else
    	                        {
    	                        	return;
    	                        }
    	                    }
    	                    break;
    	                
    	                // Drag hop on the canvas: create a new Hop...
    	                case DragAndDropContainer.TYPE_TRANS_HOP:
    		                {
    	                		newHop();
    	                		return;
    		                }
    		            
    		            default:
    	                    {
    	                        // Nothing we can use: give an error!
                                MessageBox mb = new MessageBox(shell, SWT.OK);
                                mb.setMessage(Messages.getString("TransGraph.Dialog.ItemCanNotBePlacedOnCanvas.Message")); //$NON-NLS-1$
                                mb.setText(Messages.getString("TransGraph.Dialog.ItemCanNotBePlacedOnCanvas.Title")); //$NON-NLS-1$
                                mb.open();
                                return;
    	                    }
    	                }
    
                        transMeta.unselectAll();
    
                        StepMeta before = (StepMeta) stepMeta.clone();
    
                        stepMeta.drawStep();
                        stepMeta.setSelected(true);
                        stepMeta.setLocation(p.x, p.y);
    
                        if (newstep)
                        {
                            spoon.addUndoNew(transMeta, new StepMeta[] { stepMeta }, new int[] { transMeta.indexOfStep(stepMeta) });
                        }
                        else
                        {
                            spoon.addUndoChange(transMeta, new StepMeta[] { before }, new StepMeta[] { (StepMeta) stepMeta.clone() }, new int[] { transMeta.indexOfStep(stepMeta) });
                        }
    
                        canvas.forceFocus();
                        redraw();
                    }
                    catch(Exception e)
                    {
                    	new ErrorDialog(shell, Messages.getString("TransGraph.Dialog.ErrorDroppingObject.Message"), Messages.getString("TransGraph.Dialog.ErrorDroppingObject.Title"), e);
                    }
                }
    
                public void dropAccept(DropTargetEvent event)
                {
                }
            }
        );

        // Keyboard shortcuts...
        addKeyListener(canvas);
        addKeyListener(this);

        canvas.addKeyListener(spoon.defKeys);

        setBackground(GUIResource.getInstance().getColorBackground());
    }
    
    /**
     * Select all the steps in a certain (screen) rectangle
     *
     * @param rect The selection area as a rectangle
     */
    public void selectInRect(TransMeta transMeta, Rectangle rect)
    {
    	if ( rect.height < 0 || rect.width < 0 )
    	{
    		Rectangle rectified = new Rectangle(rect.x, rect.y, rect.width,
    				                            rect.height);
    		
    		// Only for people not dragging from left top to right bottom
    		if ( rectified.height < 0 )
    		{
    		    rectified.y = rectified.y + rectified.height;
    		    rectified.height = -rectified.height;
    		}
    		if ( rectified.width < 0 )
    		{
    		    rectified.x = rectified.x + rectified.width;
    		    rectified.width = -rectified.width;
    		}    		
    		rect = rectified;
    	}
    	
        for (int i = 0; i < transMeta.nrSteps(); i++)
        {
            StepMeta stepMeta = transMeta.getStep(i);
            Point a = stepMeta.getLocation();
            if (rect.contains(a.x, a.y)) stepMeta.setSelected(true);
        }

        for (int i = 0; i < transMeta.nrNotes(); i++)
        {
            NotePadMeta ni = transMeta.getNote(i);
            Point a = ni.getLocation();
            Point b = new Point(a.x + ni.width, a.y + ni.height);
            if (rect.contains(a.x, a.y) && rect.contains(b.x, b.y)) ni.setSelected(true);
        }
    }


    private void addKeyListener(Control control)
    {
        KeyAdapter keyAdapter = new KeyAdapter()
        {
            public void keyPressed(KeyEvent e)
            {
                // F2 --> rename step
                if (e.keyCode == SWT.F2)    { renameStep(); }

                if (e.character == 1) // CTRL-A
                {
                    transMeta.selectAll();
                    redraw();
                }
                if (e.keyCode == SWT.ESC)
                {
                    transMeta.unselectAll();
                    clearSettings();
                    redraw();
                }
                if (e.keyCode == SWT.DEL)
                {
                    StepMeta stepMeta[] = transMeta.getSelectedSteps();
                    if (stepMeta != null && stepMeta.length > 0)
                    {
                        delSelected(null);
                    }
                }

                // CTRL-UP : allignTop();
                if (e.keyCode == SWT.ARROW_UP && (e.stateMask & SWT.CONTROL) != 0)
                {
                    alligntop();
                }
                // CTRL-DOWN : allignBottom();
                if (e.keyCode == SWT.ARROW_DOWN && (e.stateMask & SWT.CONTROL) != 0)
                {
                    allignbottom();
                }
                // CTRL-LEFT : allignleft();
                if (e.keyCode == SWT.ARROW_LEFT && (e.stateMask & SWT.CONTROL) != 0)
                {
                    allignleft();
                }
                // CTRL-RIGHT : allignRight();
                if (e.keyCode == SWT.ARROW_RIGHT && (e.stateMask & SWT.CONTROL) != 0)
                {
                    allignright();
                }
                // ALT-RIGHT : distributeHorizontal();
                if (e.keyCode == SWT.ARROW_RIGHT && (e.stateMask & SWT.ALT) != 0)
                {
                    distributehorizontal();
                }
                // ALT-UP : distributeVertical();
                if (e.keyCode == SWT.ARROW_UP && (e.stateMask & SWT.ALT) != 0)
                {
                    distributevertical();
                }
                // ALT-HOME : snap to grid
                if (e.keyCode == SWT.HOME && (e.stateMask & SWT.ALT) != 0)
                {
                    snaptogrid(ConstUI.GRID_SIZE);
                }

                // SPACE : over a step: show output fields...
                if (e.character == ' ' && lastMove != null)
                {
                    Point real = screen2real(lastMove.x, lastMove.y);

                    // Hide the tooltip!
                    toolTip.hide();

                    // Set the pop-up menu
                    StepMeta stepMeta = transMeta.getStep(real.x, real.y, iconsize);
                    if (stepMeta != null)
                    {
                        // OK, we found a step, show the output fields...
                        inputOutputFields(stepMeta, false);
                    }
                }
            }
        };
        control.addKeyListener(keyAdapter);
    }
    
    public void redraw()
    {
        canvas.redraw();
    }
        
    public boolean forceFocus()
    {
        return canvas.forceFocus();
    }
    
    public boolean setFocus()
    {
        return canvas.setFocus();
    }    

    public void renameStep()
    {
        StepMeta[] selection = transMeta.getSelectedSteps();
        if (selection!=null && selection.length==1)
        {
            final StepMeta stepMeta = selection[0];
            
            // What is the location of the step?
            String name = stepMeta.getName();
            Point stepLocation = stepMeta.getLocation();
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
            fdText.right= new FormAttachment(0, namePosition.x+width);
            fdText.top  = new FormAttachment(0, namePosition.y);
            text.setLayoutData(fdText);
            
            // Add a listener!
            // Catch the keys pressed when editing a Text-field...
            KeyListener lsKeyText = new KeyAdapter() 
                {
                    public void keyPressed(KeyEvent e) 
                    {
                        // "ENTER": close the text editor and copy the data over 
                        if (   e.character == SWT.CR ) 
                        {
                            String newName = text.getText();
                            text.dispose();
                            renameStep(stepMeta, newName);
                        }
                            
                        if (e.keyCode   == SWT.ESC)
                        {
                            text.dispose();
                        }
                    }
                };

            text.addKeyListener(lsKeyText);
            text.addFocusListener(new FocusAdapter()
                {
                    public void focusLost(FocusEvent e)
                    {
                        String newName = text.getText();
                        text.dispose();
                        renameStep(stepMeta, newName);
                    }
                }
            );
            
            this.layout(true, true);
            
            text.setFocus();
            text.setSelection(0, name.length());
        }
    }

    public void renameStep(StepMeta stepMeta, String stepname)
    {
        String newname = stepname;
        
        StepMeta smeta = transMeta.findStep(newname, stepMeta);
        int nr = 2;
        while (smeta != null)
        {
            newname = stepname + " " + nr;
            smeta = transMeta.findStep(newname);
            nr++;
        }
        if (nr > 2)
        {
            stepname = newname;
            MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_INFORMATION);
            mb.setMessage(Messages.getString("Spoon.Dialog.StepnameExists.Message", stepname)); // $NON-NLS-1$
            mb.setText(Messages.getString("Spoon.Dialog.StepnameExists.Title")); // $NON-NLS-1$
            mb.open();
        }
        stepMeta.setName(stepname);
        stepMeta.setChanged();
        spoon.refreshTree(); // to reflect the new name
        spoon.refreshGraph();
    }

    public void clearSettings()
    {
        selected_step = null;
        selected_note = null;
        selected_steps = null;
        selrect = null;
        candidate = null;
        last_hop_split = null;
        last_button = 0;
        iconoffset = null;
        for (int i = 0; i < transMeta.nrTransHops(); i++)
            transMeta.getTransHop(i).split = false;
    }

    public String[] getDropStrings(String str, String sep)
    {
        StringTokenizer strtok = new StringTokenizer(str, sep);
        String retval[] = new String[strtok.countTokens()];
        int i = 0;
        while (strtok.hasMoreElements())
        {
            retval[i] = strtok.nextToken();
            i++;
        }
        return retval;
    }

    public Point screen2real(int x, int y)
    {
        offset = getOffset();
        Point real;
        if (offset != null)
        {
            real = new Point(x - offset.x, y - offset.y);
        }
        else
        {
            real = new Point(x, y);
        }

        return real;
    }

    public Point real2screen(int x, int y)
    {
        offset = getOffset();
        Point screen = new Point(x + offset.x, y + offset.y);

        return screen;
    }

    public Point getRealPosition(Composite canvas, int x, int y)
    {
        Point p = new Point(0, 0);
        Composite follow = canvas;
        while (follow != null)
        {
            org.eclipse.swt.graphics.Point loc = follow.getLocation();
            Point xy = new Point(loc.x, loc.y);
            p.x += xy.x;
            p.y += xy.y;
            follow = follow.getParent();
        }

        int offsetX = -16;
        int offsetY = -64;
        if (Const.isOSX())
        {
        	offsetX=-2;
        	offsetY=-24;
        }
        p.x = x - p.x + offsetX;
        p.y = y - p.y + offsetY;

        return screen2real(p.x, p.y);
    }

    /**
     *  See if location (x,y) is on a line between two steps: the hop!
     *  @param x
     *  @param y
     *  @return the transformation hop on the specified location, otherwise: null 
     */
    private TransHopMeta findHop(int x, int y)
    {
    	return findHop(x, y, null);
    }
    
    /**
     *  See if location (x,y) is on a line between two steps: the hop!
     *  @param x
     *  @param y
     *  @param exclude the step to exclude from the hops (from or to location). Specify null if no step is to be excluded.
     *  @return the transformation hop on the specified location, otherwise: null 
     */
    private TransHopMeta findHop(int x, int y, StepMeta exclude)
    {
        int i;
        TransHopMeta online = null;
        for (i = 0; i < transMeta.nrTransHops(); i++)
        {
            TransHopMeta hi = transMeta.getTransHop(i);
            StepMeta fs = hi.getFromStep();
            StepMeta ts = hi.getToStep();

            if (fs == null || ts == null) return null;
            
            // If either the "from" or "to" step is excluded, skip this hop.
            //
            if (exclude!=null && ( exclude.equals(fs) || exclude.equals(ts))) continue;

            int line[] = getLine(fs, ts);

            if (pointOnLine(x, y, line)) online = hi;
        }
        return online;
    }

    private int[] getLine(StepMeta fs, StepMeta ts)
    {
        Point from = fs.getLocation();
        Point to = ts.getLocation();
        offset = getOffset();

        int x1 = from.x + iconsize / 2;
        int y1 = from.y + iconsize / 2;

        int x2 = to.x + iconsize / 2;
        int y2 = to.y + iconsize / 2;

        return new int[] { x1, y1, x2, y2 };
    }

    public void hideStep()
    {
        for (int i = 0; i < transMeta.nrSteps(); i++)
        {
            StepMeta sti = transMeta.getStep(i);
            if (sti.isDrawn() && sti.isSelected())
            {
                sti.hideStep();
                spoon.refreshTree();
            }
        }
        getCurrentStep().hideStep();
        spoon.refreshTree();
        redraw();
    }
    
    public void checkSelectedSteps()
    {
        spoon.checkTrans(transMeta, true);
    }

    public void detachStep()
    {
        detach(getCurrentStep());
        selected_steps = null;
    }

    public void generateMappingToThisStep()
    {
        spoon.generateMapping(transMeta, getCurrentStep());
    }

    public void partitioning()
    {
        spoon.editPartitioning(transMeta, getCurrentStep());
    }

    public void clustering()
    {
        spoon.editClustering(transMeta, getCurrentStep());
    }

    public void errorHandling()
    {
        spoon.editStepErrorHandling(transMeta, getCurrentStep());
    }

    public void newHopChoice()
    {
        selected_steps = null;
        newHop();
    }

    public void editStep()
    {
        selected_steps = null;
        editStep(getCurrentStep());
    }

    public void editDescription()
    {
        editDescription(getCurrentStep());
    }

    public void setDistributes()
    {
    		getCurrentStep().setDistributes(true);
        spoon.refreshGraph();
        spoon.refreshTree();
    }
    
    public void setCopies()
    {
    		getCurrentStep().setDistributes(false);
        spoon.refreshGraph();
        spoon.refreshTree();
    }

    public void copies()
    {
        final boolean multipleOK = checkNumberOfCopies(transMeta, getCurrentStep());
        selected_steps = null;
        String tt = Messages.getString("TransGraph.Dialog.NrOfCopiesOfStep.Title"); //$NON-NLS-1$
        String mt = Messages.getString("TransGraph.Dialog.NrOfCopiesOfStep.Message"); //$NON-NLS-1$
        EnterNumberDialog nd = new EnterNumberDialog(shell, getCurrentStep().getCopies(), tt, mt);
        int cop = nd.open();
        if (cop >= 0)
        {
            if (cop == 0) cop = 1;
            
            if (!multipleOK)
            {
                cop = 1;
                
                MessageBox mb = new MessageBox(shell, SWT.YES | SWT.ICON_WARNING);
                mb.setMessage(Messages.getString("TransGraph.Dialog.MultipleCopiesAreNotAllowedHere.Message")); //$NON-NLS-1$
                mb.setText(Messages.getString("TransGraph.Dialog.MultipleCopiesAreNotAllowedHere.Title")); //$NON-NLS-1$
                mb.open();
                
            }
            
            if (getCurrentStep().getCopies() != cop)
            {
            		getCurrentStep().setCopies(cop);
                spoon.refreshGraph();
            }
        }
    }

    public void dupeStep()
    {
        try
        {
            if (transMeta.nrSelectedSteps() <= 1)
            {
                spoon.dupeStep(transMeta, getCurrentStep());
            }
            else
            {
                for (int i = 0; i < transMeta.nrSteps(); i++)
                {
                    StepMeta stepMeta = transMeta.getStep(i);
                    if (stepMeta.isSelected())
                    {
                        spoon.dupeStep(transMeta, stepMeta);
                    }
                }
            }
        }
        catch(Exception ex)
        {
            new ErrorDialog(shell, Messages.getString("TransGraph.Dialog.ErrorDuplicatingStep.Title"), Messages.getString("TransGraph.Dialog.ErrorDuplicatingStep.Message"), ex); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }
    
    public void copyStep()
    {
        spoon.copySelected(transMeta, transMeta.getSelectedSteps(), transMeta.getSelectedNotes());
    }
    
    public void delSelected()
    {
        delSelected(getCurrentStep());
    }

    public void fieldsBefore()
    {
        selected_steps = null;
        inputOutputFields(getCurrentStep(), true);
    }
    public void fieldsAfter()
    {
        selected_steps = null;
        inputOutputFields(getCurrentStep(), false);
    }

    public void editHop()
    {
        selrect = null;
        editHop(getCurrentHop());
    }

    public void flipHopDirection()
    {
        selrect = null;
        TransHopMeta hi = getCurrentHop();

        hi.flip();

        if (transMeta.hasLoop(hi.getFromStep()))
        {
            spoon.refreshGraph();
            MessageBox mb = new MessageBox(shell, SWT.YES | SWT.ICON_WARNING);
            mb.setMessage(Messages.getString("TransGraph.Dialog.LoopsAreNotAllowed.Message")); //$NON-NLS-1$
            mb.setText(Messages.getString("TransGraph.Dialog.LoopsAreNotAllowed.Title")); //$NON-NLS-1$
            mb.open();

            hi.flip();
            spoon.refreshGraph();
        }
        else
        {
            hi.setChanged();
            spoon.refreshGraph();
            spoon.refreshTree();
            spoon.setShellText();
        }
    }

    public void enableHop()
    {
        selrect = null;
        TransHopMeta hi = getCurrentHop();
        TransHopMeta before = (TransHopMeta) hi.clone();
        hi.setEnabled(!hi.isEnabled());
        if (transMeta.hasLoop(hi.getToStep()))
        {
            hi.setEnabled(!hi.isEnabled());
            MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_WARNING);
            mb.setMessage(Messages.getString("TransGraph.Dialog.LoopAfterHopEnabled.Message")); //$NON-NLS-1$
            mb.setText(Messages.getString("TransGraph.Dialog.LoopAfterHopEnabled.Title")); //$NON-NLS-1$
            mb.open();
        }
        else
        {
            TransHopMeta after = (TransHopMeta) hi.clone();
            spoon.addUndoChange(transMeta, new TransHopMeta[] { before }, new TransHopMeta[] { after }, new int[] { transMeta.indexOfTransHop(hi) });
            spoon.refreshGraph();
            spoon.refreshTree();
        }
    }

    public void deleteHop()
    {
        selrect = null;
        TransHopMeta hi = getCurrentHop();
        int idx = transMeta.indexOfTransHop(hi);
        spoon.addUndoDelete(transMeta, new TransHopMeta[] { (TransHopMeta) hi.clone() }, new int[] { idx });
        transMeta.removeTransHop(idx);
        spoon.refreshTree();
        spoon.refreshGraph();
    }

	public void editNote() { 
		selrect=null;
		editNote( getCurrentNote() );
	} 

	public void deleteNote() {
        selrect = null;
        int idx = transMeta.indexOfNote(ni);
        if (idx >= 0)
        {
            transMeta.removeNote(idx);
            spoon.addUndoDelete(transMeta, new NotePadMeta[] { (NotePadMeta) ni.clone() }, new int[] { idx });
            redraw();
        }
	}

    public void raiseNote()
    {
		selrect=null; 
		int idx = transMeta.indexOfNote(getCurrentNote());
		if (idx>=0) 
		{
			transMeta.raiseNote(idx);
			//TBD: spoon.addUndoRaise(transMeta, new NotePadMeta[] {getCurrentNote()}, new int[] {idx} );
		} 
		redraw();
    }
    
    public void lowerNote()
    {
		selrect=null; 
		int idx = transMeta.indexOfNote(getCurrentNote());
		if (idx>=0) 
		{
			transMeta.lowerNote(idx);
			//TBD: spoon.addUndoLower(transMeta, new NotePadMeta[] {getCurrentNote()}, new int[] {idx} );
		} 
		redraw();
    }
    
    public void newNote()
    {
        selrect = null;
        String title = Messages.getString("TransGraph.Dialog.NoteEditor.Title"); //$NON-NLS-1$
        String message = Messages.getString("TransGraph.Dialog.NoteEditor.Message"); //$NON-NLS-1$
        EnterTextDialog dd = new EnterTextDialog(shell, title, message, ""); //$NON-NLS-1$
        String n = dd.open();
        if (n != null)
        {
            NotePadMeta npi = new NotePadMeta(n, lastclick.x, lastclick.y, ConstUI.NOTE_MIN_SIZE, ConstUI.NOTE_MIN_SIZE);
            transMeta.addNote(npi);
            spoon.addUndoNew(transMeta, new NotePadMeta[] { npi }, new int[] { transMeta.indexOfNote(npi) });
            redraw();
        }
    }

    public void paste()
    {
	    final String clipcontent = spoon.fromClipboard();
        Point loc = new Point(currentMouseX, currentMouseY);
        spoon.pasteXML(transMeta, clipcontent, loc);
    }

    public void settings()
    {
        editProperties( transMeta, spoon, spoon.getRepository(), true);
    }

    public void newStep( String description )
    {
        StepMeta stepMeta = spoon.newStep(transMeta, description, description, false, true);
        stepMeta.setLocation(currentMouseX, currentMouseY);
        stepMeta.setDraw(true);
        redraw();
    }

    /**
     * This sets the popup-menu on the background of the canvas based on the xy coordinate of the mouse. This method is
     * called after a mouse-click.
     * 
     * @param x X-coordinate on screen
     * @param y Y-coordinate on screen
     */
    private void setMenu(int x, int y)
    {
    	try
    	{
	    	currentMouseX = x;
	        currentMouseY = y;
	
	        final StepMeta stepMeta = transMeta.getStep(x, y, iconsize);
	        if (stepMeta != null) // We clicked on a Step!
	        {
	        		setCurrentStep( stepMeta );
	        		
	        		XulPopupMenu menu = (XulPopupMenu) menuMap.get( "trans-graph-entry" ); //$NON-NLS-1$
	    			if( menu != null ) {
	    	            int sels = transMeta.nrSelectedSteps();
	    	            
	    				XulMenuChoice item  = menu.getMenuItemById( "trans-graph-entry-newhop" ); //$NON-NLS-1$
	    				menu.addMenuListener( "trans-graph-entry-newhop", this, TransGraph.class, "newHop" ); //$NON-NLS-1$ //$NON-NLS-2$
	    				item.setEnabled( sels == 2 );
	    				
	    				item = menu.getMenuItemById( "trans-graph-entry-align-snap" ); //$NON-NLS-1$
		            	item.setText(Messages.getString("TransGraph.PopupMenu.SnapToGrid") + ConstUI.GRID_SIZE + ")\tALT-HOME");
	
					XulMenu aMenu = menu.getMenuById( "trans-graph-entry-align" ); //$NON-NLS-1$
					if( aMenu != null ) {
						aMenu.setEnabled( sels > 1 );
					}
	
					menu.addMenuListener( "trans-graph-entry-align-left", this, "allignleft" ); //$NON-NLS-1$ //$NON-NLS-2$
					menu.addMenuListener( "trans-graph-entry-align-right", this, "allignright" ); //$NON-NLS-1$ //$NON-NLS-2$
					menu.addMenuListener( "trans-graph-entry-align-top", this, "alligntop" ); //$NON-NLS-1$ //$NON-NLS-2$
					menu.addMenuListener( "trans-graph-entry-align-bottom", this, "allignbottom" ); //$NON-NLS-1$ //$NON-NLS-2$
					menu.addMenuListener( "trans-graph-entry-align-horz", this, "distributehorizontal" ); //$NON-NLS-1$ //$NON-NLS-2$
					menu.addMenuListener( "trans-graph-entry-align-vert", this, "distributevertical" ); //$NON-NLS-1$ //$NON-NLS-2$
					menu.addMenuListener( "trans-graph-entry-align-snap", this, "snaptogrid" ); //$NON-NLS-1$ //$NON-NLS-2$
	
					item = menu.getMenuItemById( "trans-graph-entry-data-movement-distribute" ); //$NON-NLS-1$
					item.setChecked( stepMeta.isDistributes() );
					item = menu.getMenuItemById( "trans-graph-entry-data-movement-copy" ); //$NON-NLS-1$
					item.setChecked( !stepMeta.isDistributes() );
	
					item = menu.getMenuItemById( "trans-graph-entry-hide" ); //$NON-NLS-1$
					item.setEnabled( stepMeta.isDrawn() && !transMeta.isStepUsedInTransHops(stepMeta) );
					
					item = menu.getMenuItemById( "trans-graph-entry-detach" ); //$NON-NLS-1$
					item.setEnabled( transMeta.isStepUsedInTransHops(stepMeta) );
					
					item = menu.getMenuItemById( "trans-graph-entry-errors" ); //$NON-NLS-1$
					item.setEnabled( stepMeta.supportsErrorHandling() );
					
					menu.addMenuListener( "trans-graph-entry-newhop", this, "newHopChoice" ); //$NON-NLS-1$ //$NON-NLS-2$
					menu.addMenuListener( "trans-graph-entry-edit", this, "editStep" ); //$NON-NLS-1$ //$NON-NLS-2$
					menu.addMenuListener( "trans-graph-entry-edit-description", this, "editDescription" ); //$NON-NLS-1$ //$NON-NLS-2$
					
					menu.addMenuListener( "trans-graph-entry-data-movement-distribute", this, "setDistributes" ); //$NON-NLS-1$ //$NON-NLS-2$
					menu.addMenuListener( "trans-graph-entry-data-movement-copy", this, "setCopies" ); //$NON-NLS-1$ //$NON-NLS-2$
					menu.addMenuListener( "trans-graph-entry-copies", this, "copies" ); //$NON-NLS-1$ //$NON-NLS-2$
					menu.addMenuListener( "trans-graph-entry-copy", this, "copyStep" ); //$NON-NLS-1$ //$NON-NLS-2$
					menu.addMenuListener( "trans-graph-entry-duplicate", this, "dupeStep" ); //$NON-NLS-1$ //$NON-NLS-2$
					menu.addMenuListener( "trans-graph-entry-delete", this, "delSelected" ); //$NON-NLS-1$ //$NON-NLS-2$
					menu.addMenuListener( "trans-graph-entry-hide", this, "hideStep" ); //$NON-NLS-1$ //$NON-NLS-2$
					menu.addMenuListener( "trans-graph-entry-detach", this, "detachStep" ); //$NON-NLS-1$ //$NON-NLS-2$
					menu.addMenuListener( "trans-graph-entry-inputs", this, "fieldsBefore" ); //$NON-NLS-1$ //$NON-NLS-2$
					menu.addMenuListener( "trans-graph-entry-outputs", this, "fieldsAfter" ); //$NON-NLS-1$ //$NON-NLS-2$
					menu.addMenuListener( "trans-graph-entry-verify", this, "checkSelectedSteps" ); //$NON-NLS-1$ //$NON-NLS-2$
					menu.addMenuListener( "trans-graph-entry-mapping", this, "generateMappingToThisStep" ); //$NON-NLS-1$ //$NON-NLS-2$
					menu.addMenuListener( "trans-graph-entry-partitioning", this, "partitioning" ); //$NON-NLS-1$ //$NON-NLS-2$
					menu.addMenuListener( "trans-graph-entry-clustering", this, "clustering" ); //$NON-NLS-1$ //$NON-NLS-2$
					menu.addMenuListener( "trans-graph-entry-errors", this, "errorHandling" ); //$NON-NLS-1$ //$NON-NLS-2$
	
					canvas.setMenu((Menu)menu.getNativeObject());
	    			}
	    			
	        }
	        else
	        {
	            final TransHopMeta hi = findHop(x, y);
	            if (hi != null) // We clicked on a HOP!
	            {
	            	
	            		XulPopupMenu menu = (XulPopupMenu) menuMap.get( "trans-graph-hop" ); //$NON-NLS-1$
	    				if( menu != null ) {
	    					setCurrentHop( hi );
	    				
	    					XulMenuChoice item  = menu.getMenuItemById( "trans-graph-hop-enabled" ); //$NON-NLS-1$
	    					if( item != null ) {
	    						if (hi.isEnabled()) {
	    							item.setText(Messages.getString("TransGraph.PopupMenu.DisableHop")); //$NON-NLS-1$
	    						} else {
	    							item.setText(Messages.getString("TransGraph.PopupMenu.EnableHop")); //$NON-NLS-1$
	    						}
	    					}
	
	    					menu.addMenuListener( "trans-graph-hop-edit", this, "editHop" ); //$NON-NLS-1$ //$NON-NLS-2$
	    					menu.addMenuListener( "trans-graph-hop-flip", this, "flipHopDirection" ); //$NON-NLS-1$ //$NON-NLS-2$
	    					menu.addMenuListener( "trans-graph-hop-enabled", this, "enableHop" ); //$NON-NLS-1$ //$NON-NLS-2$
	    					menu.addMenuListener( "trans-graph-hop-delete", this, "deleteHop" ); //$NON-NLS-1$ //$NON-NLS-2$
	
	    					canvas.setMenu((Menu)menu.getNativeObject());
	
	    				}
	
	            }
	            else
	            {
	            	
					// Clicked on the background: maybe we hit a note?
					final NotePadMeta ni = transMeta.getNote(x, y);
					setCurrentNote( ni );
					if (ni!=null)
					{
	
						XulPopupMenu menu = (XulPopupMenu) menuMap.get( "trans-graph-note" ); //$NON-NLS-1$
						if( menu != null ) {
						
							menu.addMenuListener( "trans-graph-note-edit", this, "editNote" ); //$NON-NLS-1$ //$NON-NLS-2$
							menu.addMenuListener( "trans-graph-note-delete", this, "deleteNote" ); //$NON-NLS-1$ //$NON-NLS-2$
							menu.addMenuListener( "trans-graph-note-raise", this, "raiseNote" ); //$NON-NLS-1$ //$NON-NLS-2$
							menu.addMenuListener( "trans-graph-note-lower", this, "lowerNote" ); //$NON-NLS-1$ //$NON-NLS-2$
							canvas.setMenu((Menu)menu.getNativeObject());
	
						}
					}
	                else
	                {
	
	                	XulPopupMenu menu = (XulPopupMenu) menuMap.get( "trans-graph-background" ); //$NON-NLS-1$
						if( menu != null ) {
						
							menu.addMenuListener( "trans-graph-background-new-note", this, "newNote" ); //$NON-NLS-1$ //$NON-NLS-2$
							menu.addMenuListener( "trans-graph-background-paste", this, "paste" ); //$NON-NLS-1$ //$NON-NLS-2$
							menu.addMenuListener( "trans-graph-background-settings", this, "settings" ); //$NON-NLS-1$ //$NON-NLS-2$
							
							
		                    final String clipcontent = spoon.fromClipboard();
		                    XulMenuChoice item  = menu.getMenuItemById( "trans-graph-background-paste" ); //$NON-NLS-1$
		                    if( item != null ) {
		                    		item.setEnabled( clipcontent != null );
		                    }
		        			String locale = LanguageChoice.getInstance().getDefaultLocale().toString().toLowerCase();
	
		                    XulMenu subMenu = menu.getMenuById( "trans-graph-background-new-step" );
		                    if( subMenu.getItemCount() == 0 ) {
		                        // group these by type so the menu doesn't stretch the height of the screen and to be friendly to testing tools
		                        StepLoader steploader = StepLoader.getInstance();
		                        // get a list of the categories
		            			String basecat[] = steploader.getCategories(StepPlugin.TYPE_ALL, locale );
		            			// get all the plugins
		            			StepPlugin basesteps[] = steploader.getStepsWithType(StepPlugin.TYPE_ALL);
	
	        	        		XulMessages xulMessages = new XulMessages();
		            			for( int cat=0; cat<basecat.length; cat++ ) {
		            				// create a submenu for this category
		            				org.pentaho.xul.swt.menu.Menu catMenu = new org.pentaho.xul.swt.menu.Menu( (org.pentaho.xul.swt.menu.Menu) subMenu, basecat[cat], basecat[cat], null);
		            				for( int step=0; step<basesteps.length; step++ ) {
		            					// find the steps for this category
		            					if( basesteps[step].getCategory(locale).equalsIgnoreCase(basecat[cat])) 
		            					{
		            						// create a menu option for this step
		                	        		final String name = basesteps[step].getDescription();
		                	        		new MenuChoice( catMenu, name, name, null, null, MenuChoice.TYPE_PLAIN, xulMessages);
		                	        		menu.addMenuListener( name, this, "newStep" ); //$NON-NLS-1$ //$NON-NLS-2$
		            					}
		            				}
		            			}
		            			
		                    }
		                    
		    				canvas.setMenu((Menu)menu.getNativeObject());
	
						}
	
	                }
	            }
	        }
    	}
    	catch(Throwable t) {
    		// TODO: fix this: log somehow, is IGNORED for now.
    		t.printStackTrace();
    	}
    }

    private boolean checkNumberOfCopies(TransMeta transMeta, StepMeta stepMeta)
    {
        boolean enabled = true;
        StepMeta[] prevSteps = transMeta.getPrevSteps(stepMeta);
        for (int i=0;i<prevSteps.length && enabled;i++)
        {
            // See what the target steps are.  
            // If one of the target steps is our original step, we can't start multiple copies
            // 
            String[] targetSteps = prevSteps[i].getStepMetaInterface().getTargetSteps();
            if (targetSteps!=null)
            {
                for (int t=0;t<targetSteps.length && enabled;t++)
                {
                   if (targetSteps[t].equalsIgnoreCase(stepMeta.getName())) enabled=false; 
                }
            }
        }
        return enabled;
    }

    private void setToolTip(int x, int y)
    {
    	if (!spoon.getProperties().showToolTips())
			return;
    	
    	String newTip = null;
        Image  tipImage = null;
        
        final StepMeta stepMeta = transMeta.getStep(x, y, iconsize);
        if (stepMeta != null) // We clicked on a Step!
        {
            // Also: set the tooltip!
        	// 
            if (stepMeta.getDescription() != null)
            {
                String desc = stepMeta.getDescription();
                int le = desc.length() >= 200 ? 200 : desc.length();
                newTip = desc.substring(0, le);
            }
            else
            {
                newTip=stepMeta.getName();
            }
            
            // Add the steps description
            //
            StepPlugin stepPlugin = StepLoader.getInstance().getStepPlugin(stepMeta.getStepMetaInterface());
            if (stepPlugin!=null)
            {
                newTip+=Const.CR+Const.CR+stepPlugin.getTooltip(LanguageChoice.getInstance().getDefaultLocale().toString());
                tipImage = GUIResource.getInstance().getImagesSteps().get(stepPlugin.getID()[0]);
            }
            else
            {
            	System.out.println("WTF!!");
            }
            
            // Add the partitioning info
            //
            if (stepMeta.isPartitioned()) {
            	newTip+=Const.CR+Const.CR+Messages.getString("TransGraph.Step.Tooltip.CurrentPartitioning")+stepMeta.getStepPartitioningMeta().toString();
            }
            // Add the partitioning info
            //
            if (stepMeta.getTargetStepPartitioningMeta()!=null) {
            	newTip+=Const.CR+Const.CR+Messages.getString("TransGraph.Step.Tooltip.NextPartitioning")+stepMeta.getTargetStepPartitioningMeta().toString();
            }
        }
        else
        {
            final TransHopMeta hi = findHop(x, y);
            if (hi != null) // We clicked on a HOP!
            {
                // Set the tooltip for the hop:
                newTip = hi.toString();
                tipImage = GUIResource.getInstance().getImageHop();
            }
            else
            {
            	// check the area owner list...
            	//
            	StringBuffer tip = new StringBuffer();
            	for (AreaOwner areaOwner : areaOwners) {
            		if (areaOwner.contains(x, y)) {
	            		if ( areaOwner.getParent() instanceof StepMeta && areaOwner.getOwner().equals(TransPainter.STRING_REMOTE_INPUT_STEPS) ) {
	            			StepMeta step = (StepMeta) areaOwner.getParent();
	            			tip.append("Remote input steps:").append(Const.CR).append("-----------------------").append(Const.CR);
	            			for (RemoteStep remoteStep : step.getRemoteInputSteps()) {
	            				tip.append(remoteStep.toString()).append(Const.CR);
	            			}
	            			
	            		}
	            		if ( areaOwner.getParent() instanceof StepMeta && areaOwner.getOwner().equals(TransPainter.STRING_REMOTE_OUTPUT_STEPS) ) {
	            			StepMeta step = (StepMeta) areaOwner.getParent();
	            			tip.append("Remote output steps:").append(Const.CR).append("-----------------------").append(Const.CR);
	            			for (RemoteStep remoteStep : step.getRemoteOutputSteps()) {
	            				tip.append(remoteStep.toString()).append(Const.CR);
	            			}
	            		}
	            		if ( areaOwner.getParent() instanceof StepMeta && areaOwner.getOwner().equals(TransPainter.STRING_PARTITIONING_CURRENT_STEP) ) {
	            			StepMeta step = (StepMeta) areaOwner.getParent();
	            			tip.append("Step partitioning:").append(Const.CR).append("-----------------------").append(Const.CR);
	            			tip.append(step.getStepPartitioningMeta().toString()).append(Const.CR);
	            			if (step.getTargetStepPartitioningMeta()!=null) {
	            				tip.append(Const.CR).append(Const.CR).append("TARGET: "+step.getTargetStepPartitioningMeta().toString()).append(Const.CR);
	            			}
	            		}
	            		if ( areaOwner.getParent() instanceof StepMeta && areaOwner.getOwner().equals(TransPainter.STRING_PARTITIONING_CURRENT_NEXT) ) {
	            			StepMeta step = (StepMeta) areaOwner.getParent();
	            			tip.append("Target partitioning:").append(Const.CR).append("-----------------------").append(Const.CR);
	            			tip.append(step.getStepPartitioningMeta().toString()).append(Const.CR);
	            		}
            		}
            	}
            	if (tip.length()==0) {
            		newTip = null;
            	}
            	else {
            		newTip = tip.toString();
            	}
            }
        }
        
    	if (newTip==null) {
    		toolTip.hide();
    	}
    	else if (!newTip.equalsIgnoreCase(getToolTipText())) {
    		if (tipImage!=null) {
    			toolTip.setImage(tipImage);
    		}
    		else {
    			toolTip.setImage(GUIResource.getInstance().getImageSpoon());
    		}
            toolTip.setText(newTip);
            toolTip.hide();
            toolTip.show(new org.eclipse.swt.graphics.Point(x,y));
    	}
    }

    public void delSelected(StepMeta stMeta)
    {
        if (transMeta.nrSelectedSteps() == 0)
        {
            spoon.delStep(transMeta, stMeta);
            return;
        }

          // Get the list of steps that would be deleted
          List<String> stepList = new ArrayList<String>();
          for (int i = transMeta.nrSteps() - 1; i >= 0; i--)
          {
              StepMeta stepMeta = transMeta.getStep(i);
              if (stepMeta.isSelected() || (stMeta != null && stMeta.equals(stepMeta)))
              {
                  stepList.add(stepMeta.getName());
              }
          }

          // Create and display the delete confirmation dialog
          MessageBox mb = new DeleteMessageBox(shell, 
              Messages.getString("TransGraph.Dialog.Warning.DeleteSteps.Message"), //$NON-NLS-1$
              stepList);
          int result = mb.open();
          if (result == SWT.YES)
          {
              // Delete the steps
              for (int i = transMeta.nrSteps() - 1; i >= 0; i--)
              {
                  StepMeta stepMeta = transMeta.getStep(i);
                  if (stepMeta.isSelected() || (stMeta != null && stMeta.equals(stepMeta)))
                  {
                      spoon.delStep(transMeta, stepMeta);
                  }
              }
          }
    }

    public void editDescription(StepMeta stepMeta)
    {
        String title = Messages.getString("TransGraph.Dialog.StepDescription.Title"); //$NON-NLS-1$
        String message = Messages.getString("TransGraph.Dialog.StepDescription.Message"); //$NON-NLS-1$
        EnterTextDialog dd = new EnterTextDialog(shell, title, message, stepMeta.getDescription());
        String d = dd.open();
        if (d != null) stepMeta.setDescription(d);
    }

    /**
     * Display the input- or outputfields for a step.
     * 
     * @param stepMeta The step (it's metadata) to query
     * @param before set to true if you want to have the fields going INTO the step, false if you want to see all the
     * fields that exit the step.
     */
    private void inputOutputFields(StepMeta stepMeta, boolean before)
    {
        spoon.refreshGraph();

        SearchFieldsProgressDialog op = new SearchFieldsProgressDialog(transMeta, stepMeta, before);
        try
        {
            final ProgressMonitorDialog pmd = new ProgressMonitorDialog(shell);
            
            // Run something in the background to cancel active database queries, forecably if needed!
            Runnable run = new Runnable()
            {
                public void run()
                {
                    IProgressMonitor monitor = pmd.getProgressMonitor();
                    while (pmd.getShell()==null || ( !pmd.getShell().isDisposed() && !monitor.isCanceled() ))
                    {
                        try { Thread.sleep(250); } catch(InterruptedException e) { };
                    }
                    
                    if (monitor.isCanceled()) // Disconnect and see what happens!
                    {
                        try { transMeta.cancelQueries(); } catch(Exception e) {};
                    }
                }
            };
            // Dump the cancel looker in the background!
            new Thread(run).start();
            

            pmd.run(true, true, op);
        }
        catch (InvocationTargetException e)
        {
            new ErrorDialog(shell, Messages.getString("TransGraph.Dialog.GettingFields.Title"), Messages.getString("TransGraph.Dialog.GettingFields.Message"), e); //$NON-NLS-1$ //$NON-NLS-2$
        }
        catch (InterruptedException e)
        {
            new ErrorDialog(shell, Messages.getString("TransGraph.Dialog.GettingFields.Title"), Messages.getString("TransGraph.Dialog.GettingFields.Message"), e); //$NON-NLS-1$ //$NON-NLS-2$
        }

        RowMetaInterface fields = op.getFields();

        if (fields != null && fields.size() > 0)
        {
            StepFieldsDialog sfd = new StepFieldsDialog(shell, transMeta, SWT.NONE, stepMeta.getName(), fields);
            String sn = (String) sfd.open();
            if (sn != null)
            {
                StepMeta esi = transMeta.findStep(sn);
                if (esi != null)
                {
                    editStep(esi);
                }
            }
        }
        else
        {
            MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_INFORMATION);
            mb.setMessage(Messages.getString("TransGraph.Dialog.CouldntFindFields.Message")); //$NON-NLS-1$
            mb.setText(Messages.getString("TransGraph.Dialog.CouldntFindFields.Title")); //$NON-NLS-1$
            mb.open();
        }

    }

    public void paintControl(PaintEvent e)
    {
        Point area = getArea();
        if (area.x == 0 || area.y == 0) return; // nothing to do!

        Display disp = shell.getDisplay();

        Image img = getTransformationImage(disp, area.x, area.y, true);
        e.gc.drawImage(img, 0, 0);
        img.dispose();

        // spoon.setShellText();
    }

    public Image getTransformationImage(Device device, int x, int y, boolean branded)
    {
        TransPainter transPainter = new TransPainter(transMeta, new Point(x, y), hori, vert, candidate, drop_candidate, selrect, areaOwners);
        Image img = transPainter.getTransformationImage(device, PropsUI.getInstance().isBrandingActive());

        return img;
    }


    private Point getArea()
    {
        org.eclipse.swt.graphics.Rectangle rect = canvas.getClientArea();
        Point area = new Point(rect.width, rect.height);

        return area;
    }

    private Point getThumb(Point area, Point max)
    {
        Point thumb = new Point(0, 0);
        if (max.x <= area.x)
            thumb.x = 100;
        else
            thumb.x = 100 * area.x / max.x;

        if (max.y <= area.y)
            thumb.y = 100;
        else
            thumb.y = 100 * area.y / max.y;

        return thumb;
    }

    private Point getOffset()
    {
        Point area = getArea();
        Point max = transMeta.getMaximum();
        Point thumb = getThumb(area, max);

        return getOffset(thumb, area);
    }

    private Point getOffset(Point thumb, Point area)
    {
        Point p = new Point(0, 0);
        Point sel = new Point(hori.getSelection(), vert.getSelection());

        if (thumb.x == 0 || thumb.y == 0) return p;

        p.x = -sel.x * area.x / thumb.x;
        p.y = -sel.y * area.y / thumb.y;

        return p;
    }

    public int sign(int n)
    {
        return n < 0 ? -1 : (n > 0 ? 1 : 1);
    }

    private void editStep(StepMeta stepMeta)
    {
        spoon.editStep(transMeta, stepMeta);
    }

    private void editNote(NotePadMeta ni)
    {
        NotePadMeta before = (NotePadMeta) ni.clone();

        String title = Messages.getString("TransGraph.Dialog.EditNote.Title"); //$NON-NLS-1$
        String message = Messages.getString("TransGraph.Dialog.EditNote.Message"); //$NON-NLS-1$
        EnterTextDialog dd = new EnterTextDialog(shell, title, message, ni.getNote());
        String n = dd.open();
        if (n != null)
        {
            ni.setChanged();
            ni.setNote(n);
            ni.width = ConstUI.NOTE_MIN_SIZE;
            ni.height = ConstUI.NOTE_MIN_SIZE;

            NotePadMeta after = (NotePadMeta) ni.clone();
            spoon.addUndoChange(transMeta, new NotePadMeta[] { before }, new NotePadMeta[] { after }, new int[] { transMeta.indexOfNote(ni) });
            spoon.refreshGraph();
        }
    }

    private void editHop(TransHopMeta transHopMeta)
    {
        String name = transHopMeta.toString();
        log.logDebug(toString(), Messages.getString("TransGraph.Logging.EditingHop") + name); //$NON-NLS-1$
        spoon.editHop(transMeta, transHopMeta);
    }

    private void newHop()
    {
        StepMeta fr = transMeta.getSelectedStep(0);
        StepMeta to = transMeta.getSelectedStep(1);
        spoon.newHop(transMeta, fr, to);
    }

    private boolean pointOnLine(int x, int y, int line[])
    {
        int dx, dy;
        int pm = HOP_SEL_MARGIN / 2;
        boolean retval = false;

        for (dx = -pm; dx <= pm && !retval; dx++)
        {
            for (dy = -pm; dy <= pm && !retval; dy++)
            {
                retval = pointOnThinLine(x + dx, y + dy, line);
            }
        }

        return retval;
    }

    private boolean pointOnThinLine(int x, int y, int line[])
    {
        int x1 = line[0];
        int y1 = line[1];
        int x2 = line[2];
        int y2 = line[3];

        // Not in the square formed by these 2 points: ignore!
        if (!(((x >= x1 && x <= x2) || (x >= x2 && x <= x1)) && ((y >= y1 && y <= y2) || (y >= y2 && y <= y1)))) return false;

        double angle_line = Math.atan2(y2 - y1, x2 - x1) + Math.PI;
        double angle_point = Math.atan2(y - y1, x - x1) + Math.PI;

        // Same angle, or close enough?
        if (angle_point >= angle_line - 0.01 && angle_point <= angle_line + 0.01) return true;

        return false;
    }

    private SnapAllignDistribute createSnapAllignDistribute()
    {
        List<GUIPositionInterface> elements = transMeta.getSelectedDrawnStepsList();
        int[] indices = transMeta.getStepIndexes(elements.toArray(new StepMeta[elements.size()]));

        return new SnapAllignDistribute(transMeta, elements, indices, spoon, this);
    }
    
    public void snaptogrid()
    {
    		snaptogrid( ConstUI.GRID_SIZE );
    	}

    private void snaptogrid(int size)
    {
        createSnapAllignDistribute().snaptogrid(size);
    }

    public void allignleft()
    {
        createSnapAllignDistribute().allignleft();
    }

    public void allignright()
    {
        createSnapAllignDistribute().allignright();
    }

    public void alligntop()
    {
        createSnapAllignDistribute().alligntop();
    }

    public void allignbottom()
    {
        createSnapAllignDistribute().allignbottom();
    }

    public void distributehorizontal()
    {
        createSnapAllignDistribute().distributehorizontal();
    }

    public void distributevertical()
    {
        createSnapAllignDistribute().distributevertical();
    }

    private void detach(StepMeta stepMeta)
    {
        TransHopMeta hfrom = transMeta.findTransHopTo(stepMeta);
        TransHopMeta hto = transMeta.findTransHopFrom(stepMeta);

        if (hfrom != null && hto != null)
        {
            if (transMeta.findTransHop(hfrom.getFromStep(), hto.getToStep()) == null)
            {
                TransHopMeta hnew = new TransHopMeta(hfrom.getFromStep(), hto.getToStep());
                transMeta.addTransHop(hnew);
                spoon.addUndoNew(transMeta, new TransHopMeta[] { hnew }, new int[] { transMeta.indexOfTransHop(hnew) });
                spoon.refreshTree();
            }
        }
        if (hfrom != null)
        {
            int fromidx = transMeta.indexOfTransHop(hfrom);
            if (fromidx >= 0)
            {
                transMeta.removeTransHop(fromidx);
                spoon.refreshTree();
            }
        }
        if (hto != null)
        {
            int toidx = transMeta.indexOfTransHop(hto);
            if (toidx >= 0)
            {
                transMeta.removeTransHop(toidx);
                spoon.refreshTree();
            }
        }
        spoon.refreshTree();
        redraw();
    }

    // Preview the selected steps...
    public void preview()
    {
        // Create a new transformation
        TransMeta preview = new TransMeta();

        // Copy the selected steps into it...
        for (int i = 0; i < transMeta.nrSteps(); i++)
        {
            StepMeta stepMeta = transMeta.getStep(i);
            if (stepMeta.isSelected())
            {
                preview.addStep(stepMeta);
            }
        }

        // Copy the relevant TransHops into it...
        for (int i = 0; i < transMeta.nrTransHops(); i++)
        {
            TransHopMeta hi = transMeta.getTransHop(i);
            if (hi.isEnabled())
            {
                StepMeta fr = hi.getFromStep();
                StepMeta to = hi.getToStep();
                if (fr.isSelected() && to.isSelected())
                {
                    preview.addTransHop(hi);
                }
            }
        }
    }

    public void newProps()
    {
        GUIResource.getInstance().reload();

        iconsize = spoon.props.getIconSize();
    }

    public String toString()
    {
        return this.getClass().getName();
    }

    public EngineMetaInterface getMeta() {
    	return transMeta;
    }

    /**
     * @return the transMeta
     * /
    public TransMeta getTransMeta()
    {
        return transMeta;
    }

    /**
     * @param transMeta the transMeta to set
     */
    public void setTransMeta(TransMeta transMeta)
    {
        this.transMeta = transMeta;
    }
    
    // Change of step, connection, hop or note...
    public void addUndoPosition(Object obj[], int pos[], Point prev[], Point curr[])
    {
        addUndoPosition(obj, pos, prev, curr, false);
    }

    // Change of step, connection, hop or note...
    public void addUndoPosition(Object obj[], int pos[], Point prev[], Point curr[], boolean nextAlso)
    {
        // It's better to store the indexes of the objects, not the objects itself!
        transMeta.addUndo(obj, null, pos, prev, curr, TransMeta.TYPE_UNDO_POSITION, nextAlso);
        spoon.setUndoMenu(transMeta);
    }

    /*
     * Shows a 'model has changed' warning if required
     * @return true if nothing has changed or the changes are rejected by the user.
     */
    public int showChangedWarning()
    {
        MessageBox mb = new MessageBox(shell,  SWT.YES | SWT.NO | SWT.CANCEL | SWT.ICON_WARNING );
        mb.setMessage(Messages.getString("Spoon.Dialog.PromptSave.Message", spoon.makeTransGraphTabName(transMeta)));//"This model has changed.  Do you want to save it?"
        mb.setText(Messages.getString("Spoon.Dialog.PromptSave.Title"));
        return mb.open();
    }
    
    public boolean applyChanges()
    {
        return spoon.saveToFile(transMeta);
    }

    public boolean canBeClosed()
    {
        return !transMeta.hasChanged();
    }
    
    public TransMeta getManagedObject()
    {
        return transMeta;
    }
    
    public boolean hasContentChanged()
    {
        return transMeta.hasChanged();
    }

    public List<CheckResultInterface> getRemarks()
    {
        return remarks;
    }

    public void setRemarks(List<CheckResultInterface> remarks)
    {
        this.remarks = remarks;
    }

    public List<DatabaseImpact> getImpact()
    {
        return impact;
    }

    public void setImpact(List<DatabaseImpact> impact)
    {
        this.impact = impact;
    }

    public boolean isImpactFinished()
    {
        return impactFinished;
    }

    public void setImpactFinished(boolean impactHasRun)
    {
        this.impactFinished = impactHasRun;
    }

    /**
     * @return the lastMove
     */
    public Point getLastMove()
    {
        return lastMove;
    }
    
    public static boolean editProperties(TransMeta transMeta, Spoon spoon, Repository rep, boolean allowDirectoryChange)
    {
        if (transMeta==null) return false;
        
        TransDialog tid = new TransDialog(spoon.getShell(), SWT.NONE, transMeta, rep);
        tid.setDirectoryChangeAllowed(allowDirectoryChange);
        TransMeta ti = tid.open();
        
        // In this case, load shared objects
        //
        if (tid.isSharedObjectsFileChanged())
        {
            try
            {
            	transMeta.readSharedObjects(rep);
            }
            catch(Exception e)
            {
                new ErrorDialog(spoon.getShell(), Messages.getString("Spoon.Dialog.ErrorReadingSharedObjects.Title"), Messages.getString("Spoon.Dialog.ErrorReadingSharedObjects.Message", spoon.makeTransGraphTabName(transMeta)), e);
            }
        }
        
        if (tid.isSharedObjectsFileChanged() || ti!=null)
        {
            try
            {
                transMeta.readSharedObjects(rep);
            }
            catch(KettleException e)
            {
                new ErrorDialog(spoon.getShell(), Messages.getString("Spoon.Dialog.ErrorReadingSharedObjects.Title"), Messages.getString("Spoon.Dialog.ErrorReadingSharedObjects.Message", spoon.makeTransGraphTabName(transMeta)), e);
            }                                
            spoon.refreshTree();
            spoon.delegates.tabs.renameTabs(); // cheap operation, might as will do it anyway
        }
        
        spoon.setShellText();
        return ti!=null;
    }

}