/**********************************************************************
 **                                                                   **
 **               This code belongs to the KETTLE project.            **
 **                                                                   **
 ** Kettle, from version 2.2 on, is released into the public domain   **
 ** under the Lesser GNU Public License (LGPL).                       **
 **                                                                   **
 ** For more details, please read the document LICENSE.txt, included  **
 ** in this project                                                   **
 **                                                                   **
 ** http://www.kettle.be                                              **
 ** info@kettle.be                                                    **
 **                                                                   **
 **********************************************************************/

package be.ibridge.kettle.spoon;

import java.lang.reflect.InvocationTargetException;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Shell;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.DragAndDropContainer;
import be.ibridge.kettle.core.GUIResource;
import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.NotePadMeta;
import be.ibridge.kettle.core.Point;
import be.ibridge.kettle.core.Rectangle;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.dialog.EnterNumberDialog;
import be.ibridge.kettle.core.dialog.EnterTextDialog;
import be.ibridge.kettle.core.dialog.ErrorDialog;
import be.ibridge.kettle.core.dialog.StepFieldsDialog;
import be.ibridge.kettle.spoon.dialog.SearchFieldsProgressDialog;
import be.ibridge.kettle.trans.StepLoader;
import be.ibridge.kettle.trans.StepPlugin;
import be.ibridge.kettle.trans.TransHopMeta;
import be.ibridge.kettle.trans.TransMeta;
import be.ibridge.kettle.trans.step.StepMeta;
import be.ibridge.kettle.trans.step.StepMetaInterface;
import be.ibridge.kettle.trans.step.tableinput.TableInputMeta;

/**
 * This class handles the display of the transformations in a graphical way using icons, arrows, etc.
 * 
 * @author Matt
 * @since 17-mei-2003
 * 
 */

public class SpoonGraph extends Canvas
{
    private static final int HOP_SEL_MARGIN = 9;

    private Shell            shell;

    private SpoonGraph       canvas;

    private LogWriter        log;

    private int              iconsize;

    private int              linewidth;

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

    public boolean           shift, control;

    private boolean          split_hop;

    private int              last_button;

    private TransHopMeta     last_hop_split;

    private Rectangle        selrect;

    private int              shadowsize;

	private Menu mPop;

    public SpoonGraph(Composite par, int style, LogWriter l, Spoon sp)
    {
        super(par, style);
        shell = par.getShell();
        log = l;
        spoon = sp;
        canvas = this;

        iconsize = spoon.props.getIconSize();

        clearSettings();

        hori = getHorizontalBar();
        vert = getVerticalBar();

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

        addPaintListener(new PaintListener()
        {
            public void paintControl(PaintEvent e)
            {
                if (!spoon.isStopped()) SpoonGraph.this.paintControl(e);
            }
        });

        selected_steps = null;
        lastclick = null;

        addKeyListener(spoon.modKeys);

        /*
         * Handle the mouse...
         */

        addMouseListener(new MouseAdapter()
        {
            public void mouseDoubleClick(MouseEvent e)
            {
                clearSettings();
                control = (e.stateMask & SWT.CONTROL) != 0;
                shift = (e.stateMask & SWT.SHIFT) != 0;

                Point real = screen2real(e.x, e.y);

                StepMeta stepMeta = spoon.getTransMeta().getStep(real.x, real.y, iconsize);
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
                        NotePadMeta ni = spoon.getTransMeta().getNote(real.x, real.y);
                        if (ni != null)
                        {
                            selected_note = null;
                            editNote(ni);
                        }
                    }
                }
            }

            public void mouseDown(MouseEvent e)
            {
                clearSettings();
                control = (e.stateMask & SWT.CONTROL) != 0;
                shift = (e.stateMask & SWT.SHIFT) != 0;

                last_button = e.button;
                Point real = screen2real(e.x, e.y);

                // Clear the tooltip!
                setToolTipText(null);

                // Set the pop-up menu
                setMenu(real.x, real.y);

                // Did we click on a step?
                StepMeta stepMeta = spoon.getTransMeta().getStep(real.x, real.y, iconsize);
                if (stepMeta != null)
                {
                    selected_steps = spoon.getTransMeta().getSelectedSteps();
                    selected_step = stepMeta;
                    // 
                    // When an icon is moved that is not selected, it gets
                    // selected too late.
                    // It is not captured here, but in the mouseMoveListener...
                    previous_step_locations = spoon.getTransMeta().getSelectedStepLocations();

                    Point p = stepMeta.getLocation();
                    iconoffset = new Point(real.x - p.x, real.y - p.y);
                }
                else
                {
                    // Dit we hit a note?
                    NotePadMeta ni = spoon.getTransMeta().getNote(real.x, real.y);
                    if (ni != null && last_button == 1)
                    {
                        selected_notes = spoon.getTransMeta().getSelectedNotes();
                        selected_note = ni;
                        Point loc = ni.getLocation();

                        previous_note_locations = spoon.getTransMeta().getSelectedNoteLocations();

                        noteoffset = new Point(real.x - loc.x, real.y - loc.y);
                    }
                    else
                    {
                        if (!control) selrect = new Rectangle(real.x, real.y, 0, 0);
                    }
                }
                lastclick = new Point(real.x, real.y);
                redraw();
            }

            public void mouseUp(MouseEvent e)
            {
                control = (e.stateMask & SWT.CONTROL) != 0;
                shift = (e.stateMask & SWT.SHIFT) != 0;

                if (iconoffset == null) iconoffset = new Point(0, 0);
                Point real = screen2real(e.x, e.y);
                Point icon = new Point(real.x - iconoffset.x, real.y - iconoffset.y);

                // Quick new hop option? (drag from one step to another)
                //
                if (candidate != null)
                {
                    if (spoon.getTransMeta().findTransHop(candidate) == null)
                    {
                        spoon.getTransMeta().addTransHop(candidate);
                        spoon.refreshTree();
                        if (spoon.getTransMeta().hasLoop(candidate.getFromStep()))
                        {
                            MessageBox mb = new MessageBox(shell, SWT.YES | SWT.ICON_WARNING);
                            mb.setMessage(Messages.getString("SpoonGraph.Dialog.HopCausesLoop.Message")); //$NON-NLS-1$
                            mb.setText(Messages.getString("SpoonGraph.Dialog.HopCausesLoop.Title")); //$NON-NLS-1$
                            mb.open();
                            int idx = spoon.getTransMeta().indexOfTransHop(candidate);
                            spoon.getTransMeta().removeTransHop(idx);
                            spoon.refreshTree();
                        }
                        else
                        {
                            spoon.addUndoNew(new TransHopMeta[] { candidate }, new int[] { spoon.getTransMeta().indexOfTransHop(candidate) });
                        }
                    }
                    candidate = null;
                    selected_steps = null;
                    last_button = 0;
                    redraw();
                }
                // Did we select a region on the screen? Mark steps in region as
                // selected
                //
                else
                    if (selrect != null)
                    {
                        selrect.width = real.x - selrect.x;
                        selrect.height = real.y - selrect.y;

                        spoon.getTransMeta().unselectAll();
                        spoon.getTransMeta().selectInRect(selrect);
                        selrect = null;
                        redraw();
                    }
                    // Clicked on an icon?
                    //
                    else
                        if (selected_step != null)
                        {
                            if (e.button == 1)
                            {
                                if (lastclick.x == e.x && lastclick.y == e.y)
                                {
                                    // Flip selection when control is pressed!
                                    if (control)
                                    {
                                        selected_step.flipSelected();
                                    }
                                    else
                                    {
                                        // Otherwise, select only the icon clicked on!
                                        spoon.getTransMeta().unselectAll();
                                        selected_step.setSelected(true);
                                    }
                                }
                                else
                                {
                                    // Find out which Steps & Notes are selected
                                    selected_steps = spoon.getTransMeta().getSelectedSteps();
                                    selected_notes = spoon.getTransMeta().getSelectedNotes();

                                    // We moved around some items: store undo info...
                                    boolean also = false;
                                    if (selected_notes != null && previous_note_locations != null)
                                    {
                                        int indexes[] = spoon.getTransMeta().getNoteIndexes(selected_notes);
                                        spoon.addUndoPosition(selected_notes, indexes, previous_note_locations, spoon.getTransMeta()
                                                .getSelectedNoteLocations(), also);
                                        also = selected_steps != null && selected_steps.length > 0;
                                    }
                                    if (selected_steps != null && previous_step_locations != null)
                                    {
                                        int indexes[] = spoon.getTransMeta().getStepIndexes(selected_steps);
                                        spoon.addUndoPosition(selected_steps, indexes, previous_step_locations, spoon.getTransMeta()
                                                .getSelectedStepLocations(), also);
                                    }
                                }
                            }

                            // OK, we moved the step, did we move it across a hop?
                            // If so, ask to split the hop!
                            if (split_hop)
                            {
                                TransHopMeta hi = findHop(icon.x + iconsize / 2, icon.y + iconsize / 2);
                                if (hi != null)
                                {
                                    int id = 0;
                                    if (!spoon.props.getAutoSplit())
                                    {
                                        MessageDialogWithToggle md = new MessageDialogWithToggle(shell, Messages.getString("SpoonGraph.Dialog.SplitHop.Title"), null, //$NON-NLS-1$
                                                Messages.getString("SpoonGraph.Dialog.SplitHop.Message") + Const.CR + hi.toString(), MessageDialog.QUESTION, new String[] { //$NON-NLS-1$
                                                        Messages.getString("System.Button.Yes"), Messages.getString("System.Button.No") }, 0, Messages.getString("SpoonGraph.Dialog.Option.SplitHop.DoNotAskAgain"), spoon.props.getAutoSplit()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                        id = md.open();
                                        spoon.props.setAutoSplit(md.getToggleState());
                                    }

                                    if (id == 0) // Means: "Yes" button clicked!
                                    {
                                        TransHopMeta newhop1 = new TransHopMeta(hi.getFromStep(), selected_step);
                                        spoon.getTransMeta().addTransHop(newhop1);
                                        spoon
                                                .addUndoNew(new TransHopMeta[] { newhop1 }, new int[] { spoon.getTransMeta().indexOfTransHop(newhop1) },
                                                        true);
                                        TransHopMeta newhop2 = new TransHopMeta(selected_step, hi.getToStep());
                                        spoon.getTransMeta().addTransHop(newhop2);
                                        spoon
                                                .addUndoNew(new TransHopMeta[] { newhop2 }, new int[] { spoon.getTransMeta().indexOfTransHop(newhop2) },
                                                        true);
                                        int idx = spoon.getTransMeta().indexOfTransHop(hi);
                                        spoon.addUndoDelete(new TransHopMeta[] { hi }, new int[] { idx }, true);
                                        spoon.getTransMeta().removeTransHop(idx);
                                        spoon.refreshTree();
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
                                            spoon.getTransMeta().unselectAll();
                                            selected_note.setSelected(true);
                                        }
                                    }
                                    else
                                    {
                                        // Find out which Steps & Notes are selected
                                        selected_steps = spoon.getTransMeta().getSelectedSteps();
                                        selected_notes = spoon.getTransMeta().getSelectedNotes();

                                        // We moved around some items: store undo info...
                                        boolean also = false;
                                        if (selected_notes != null && previous_note_locations != null)
                                        {
                                            int indexes[] = spoon.getTransMeta().getNoteIndexes(selected_notes);
                                            spoon.addUndoPosition(selected_notes, indexes, previous_note_locations, spoon.getTransMeta()
                                                    .getSelectedNoteLocations(), also);
                                            also = selected_steps != null && selected_steps.length > 0;
                                        }
                                        if (selected_steps != null && previous_step_locations != null)
                                        {
                                            int indexes[] = spoon.getTransMeta().getStepIndexes(selected_steps);
                                            spoon.addUndoPosition(selected_steps, indexes, previous_step_locations, spoon.getTransMeta()
                                                    .getSelectedStepLocations(), also);
                                        }
                                    }
                                }

                                selected_notes = null;
                                selected_steps = null;
                                selected_step = null;
                                selected_note = null;
                            }
            }
        });

        addMouseMoveListener(new MouseMoveListener()
        {
            public void mouseMove(MouseEvent e)
            {
                // Remember the last position of the mouse for paste with keyboard
                lastMove = new Point(e.x, e.y);
                Point real = screen2real(e.x, e.y);

                if (iconoffset == null) iconoffset = new Point(0, 0);
                Point icon = new Point(real.x - iconoffset.x, real.y - iconoffset.y);

                if (noteoffset == null) noteoffset = new Point(0, 0);
                Point note = new Point(real.x - noteoffset.x, real.y - noteoffset.y);

                setToolTip(real.x, real.y);

                // 
                // First see if the icon we clicked on was selected.
                // If the icon was not selected, we should unselect all other
                // icons,
                // selected and move only the one icon
                if (selected_step != null && !selected_step.isSelected())
                {
                    // System.out.println("STEPS: Unselected all");
                    spoon.getTransMeta().unselectAll();
                    selected_step.setSelected(true);
                    selected_steps = new StepMeta[] { selected_step };
                    previous_step_locations = new Point[] { selected_step.getLocation() };
                }
                if (selected_note != null && !selected_note.isSelected())
                {
                    // System.out.println("NOTES: Unselected all");
                    spoon.getTransMeta().unselectAll();
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
                            TransHopMeta hi = findHop(icon.x + iconsize / 2, icon.y + iconsize / 2);
                            if (hi != null)
                            {

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

                            selected_notes = spoon.getTransMeta().getSelectedNotes();
                            selected_steps = spoon.getTransMeta().getSelectedSteps();

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
                                StepMeta stepMeta = spoon.getTransMeta().getStep(real.x, real.y, iconsize);
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

                                selected_notes = spoon.getTransMeta().getSelectedNotes();
                                selected_steps = spoon.getTransMeta().getSelectedSteps();

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
        Transfer[] ttypes = new Transfer[] { TextTransfer.getInstance() };
        DropTarget ddTarget = new DropTarget(this, DND.DROP_MOVE | DND.DROP_COPY);
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
                // We expect a piece of XML...
                String xml = (String) event.data;
                
                try
                {
	                DragAndDropContainer container = new DragAndDropContainer(xml);
	                
	                StepMeta stepMeta = null;
	                boolean newstep = false;
	                
	                switch(container.getType())
	                {
	                // Put an existing one on the canvas.
	                case DragAndDropContainer.TYPE_STEP:
		                {
	                		// Drop hidden step onto canvas....		                
		                	stepMeta = spoon.getTransMeta().findStep(container.getData());
		                	if (stepMeta!=null)
		                	{
	    	                    if (stepMeta.isDrawn() || spoon.getTransMeta().isStepUsedInTransHops(stepMeta))
	    	                    {
	    	                        MessageBox mb = new MessageBox(shell, SWT.OK);
	    	                        mb.setMessage(Messages.getString("SpoonGraph.Dialog.StepIsAlreadyOnCanvas.Message")); //$NON-NLS-1$
	    	                        mb.setText(Messages.getString("SpoonGraph.Dialog.StepIsAlreadyOnCanvas.Title")); //$NON-NLS-1$
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
                			stepMeta = spoon.newStep(steptype, steptype, false, true);
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
	                        tii.setDatabaseMeta(spoon.getTransMeta().findDatabase(connectionName));
	
	                        StepLoader steploader = StepLoader.getInstance();
	                        String stepID = steploader.getStepPluginID(tii);
	                        StepPlugin stepPlugin = steploader.findStepPluginWithID(stepID);
	                        String stepName = spoon.getTransMeta().getAlternativeStepname(stepPlugin.getDescription());
	                        stepMeta = new StepMeta(log, stepID, stepName, tii);
	                        if (spoon.editStepInfo(stepMeta) != null)
	                        {
	                            spoon.getTransMeta().addStep(stepMeta);
	                            spoon.refreshTree(true);
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
                            mb.setMessage(Messages.getString("SpoonGraph.Dialog.ItemCanNotBePlacedOnCanvas.Message")); //$NON-NLS-1$
                            mb.setText(Messages.getString("SpoonGraph.Dialog.ItemCanNotBePlacedOnCanvas.Title")); //$NON-NLS-1$
                            mb.open();
                            return;
	                    }
	                }

                    spoon.getTransMeta().unselectAll();

                    StepMeta before = (StepMeta) stepMeta.clone();

                    stepMeta.drawStep();
                    stepMeta.setSelected(true);
                    stepMeta.setLocation(p.x, p.y);

                    if (newstep)
                    {
                        spoon.addUndoNew(new StepMeta[] { stepMeta }, new int[] { spoon.getTransMeta().indexOfStep(stepMeta) });
                    }
                    else
                    {
                        spoon.addUndoChange(new StepMeta[] { before }, new StepMeta[] { (StepMeta) stepMeta.clone() }, new int[] { spoon.getTransMeta()
                                .indexOfStep(stepMeta) });
                    }

                    canvas.forceFocus();
                    redraw();
                }
                catch(Exception e)
                {
                	new ErrorDialog(shell, spoon.props, Messages.getString("SpoonGraph.Dialog.ErrorDroppingObject.Message"), Messages.getString("SpoonGraph.Dialog.ErrorDroppingObject.Title"), e);
                }
            }

            public void dropAccept(DropTargetEvent event)
            {
            }
        });

        // Keyboard shortcuts...
        addKeyListener(new KeyAdapter()
        {
            public void keyPressed(KeyEvent e)
            {
                if ((int) e.character == 1) // CTRL-A
                {
                    spoon.getTransMeta().selectAll();
                    redraw();
                }
                if ((int) e.character == 3) // CTRL-C
                {
                    spoon.copySelected(spoon.getTransMeta().getSelectedSteps(), spoon.getTransMeta().getSelectedNotes());
                }
                if ((int) e.character == 22) // CTRL-V
                {
                    String clipcontent = spoon.fromClipboard();
                    if (clipcontent != null)
                    {
                        if (lastMove != null)
                        {
                            spoon.pasteXML(clipcontent, lastMove);
                        }
                    }

                    // spoon.pasteSteps( );
                }
                if (e.keyCode == SWT.ESC)
                {
                    spoon.getTransMeta().unselectAll();
                    clearSettings();
                    redraw();
                }
                if (e.keyCode == SWT.DEL)
                {
                    StepMeta stepMeta[] = spoon.getTransMeta().getSelectedSteps();
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
                    snaptogrid(Const.GRID_SIZE);
                }

                // SPACE : over a step: show output fields...
                if (e.character == ' ' && lastMove != null)
                {
                    Point real = screen2real(lastMove.x, lastMove.y);

                    // Clear the tooltip!
                    setToolTipText(null);

                    // Set the pop-up menu
                    StepMeta stepMeta = spoon.getTransMeta().getStep(real.x, real.y, iconsize);
                    if (stepMeta != null)
                    {
                        // OK, we found a step, show the output fields...
                        inputOutputFields(stepMeta, false);
                    }
                }
            }
        });

        addKeyListener(spoon.defKeys);

        setBackground(GUIResource.getInstance().getColorBackground());
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
        for (int i = 0; i < spoon.getTransMeta().nrTransHops(); i++)
            spoon.getTransMeta().getTransHop(i).split = false;
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
        getOffset();
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
        getOffset();
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

        p.x = x - p.x - 16;
        p.y = y - p.y - 64;

        return screen2real(p.x, p.y);
    }

    // See if location (x,y) is on a line between two steps: the hop!
    // return the HopInfo if so, otherwise: null
    private TransHopMeta findHop(int x, int y)
    {
        int i;
        TransHopMeta online = null;
        for (i = 0; i < spoon.getTransMeta().nrTransHops(); i++)
        {
            TransHopMeta hi = spoon.getTransMeta().getTransHop(i);
            StepMeta fs = hi.getFromStep();
            StepMeta ts = hi.getToStep();

            if (fs == null || ts == null) return null;

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

    /**
     * This sets the popup-menu on the background of the canvas based on the xy coordinate of the mouse. This method is
     * called after a mouse-click.
     * 
     * @param x X-coordinate on screen
     * @param y Y-coordinate on screen
     */
    private void setMenu(int x, int y)
    {
        final int mousex = x;
        final int mousey = y;
        
        // Dispose the menu if it was allocated beforehand...
        if (mPop!=null && !mPop.isDisposed()) mPop.dispose();

        final StepMeta stepMeta = spoon.getTransMeta().getStep(x, y, iconsize);
        if (stepMeta != null) // We clicked on a Step!
        {
            mPop = new Menu((Control) this);
            MenuItem miNewHop = null;
            MenuItem miHideStep = null;

            int sels = spoon.getTransMeta().nrSelectedSteps();
            if (sels == 2)
            {
                miNewHop = new MenuItem(mPop, SWT.CASCADE);
                miNewHop.setText(Messages.getString("SpoonGraph.PopupMenu.NewHop")); //$NON-NLS-1$
            }
            MenuItem miEditStep = new MenuItem(mPop, SWT.CASCADE);
            miEditStep.setText(Messages.getString("SpoonGraph.PopupMenu.EditStep")); //$NON-NLS-1$
            MenuItem miEditDesc = new MenuItem(mPop, SWT.CASCADE);
            miEditDesc.setText(Messages.getString("SpoonGraph.PopupMenu.EditStepDescription")); //$NON-NLS-1$

            new MenuItem(mPop, SWT.SEPARATOR);
            // ---------------------------------------

            MenuItem miPopDC = new MenuItem(mPop, SWT.CASCADE);
            miPopDC.setText(Messages.getString("SpoonGraph.PopupMenu.DataMovement")); //$NON-NLS-1$

            Menu mPopDC = new Menu(miPopDC);
            MenuItem miStepDist = new MenuItem(mPopDC, SWT.CASCADE | SWT.CHECK);
            miStepDist.setText(Messages.getString("SpoonGraph.PopupMenu.DistributeData")); //$NON-NLS-1$
            MenuItem miStepCopy = new MenuItem(mPopDC, SWT.CASCADE | SWT.CHECK);
            miStepCopy.setText(Messages.getString("SpoonGraph.PopupMenu.CopyData")); //$NON-NLS-1$
            miPopDC.setMenu(mPopDC);

            if (stepMeta.distributes)
                miStepDist.setSelection(true);
            else
                miStepCopy.setSelection(true);

            MenuItem miCopies = new MenuItem(mPop, SWT.CASCADE);
            miCopies.setText(Messages.getString("SpoonGraph.PopupMenu.NumberOfCopies")); //$NON-NLS-1$

            new MenuItem(mPop, SWT.SEPARATOR);
            // ---------------------------------------

            // Clipboard operations...
            MenuItem miCopyStep = new MenuItem(mPop, SWT.CASCADE);
            miCopyStep.setText(Messages.getString("SpoonGraph.PopupMenu.CopyToClipboard")); //$NON-NLS-1$

            MenuItem miDupeStep = new MenuItem(mPop, SWT.CASCADE);
            miDupeStep.setText(Messages.getString("SpoonGraph.PopupMenu.DuplicateStep")); //$NON-NLS-1$

            MenuItem miDelStep = new MenuItem(mPop, SWT.CASCADE);
            miDelStep.setText(Messages.getString("SpoonGraph.PopupMenu.DeleteStep")); //$NON-NLS-1$

            if (stepMeta.isDrawn() && !spoon.getTransMeta().isStepUsedInTransHops(stepMeta))
            {
                miHideStep = new MenuItem(mPop, SWT.CASCADE);
                miHideStep.setText(Messages.getString("SpoonGraph.PopupMenu.HideStep")); //$NON-NLS-1$
                miHideStep.addSelectionListener(new SelectionAdapter()
                {
                    public void widgetSelected(SelectionEvent e)
                    {
                        for (int i = 0; i < spoon.getTransMeta().nrSteps(); i++)
                        {
                            StepMeta sti = spoon.getTransMeta().getStep(i);
                            if (sti.isDrawn() && sti.isSelected())
                            {
                                sti.hideStep();
                                spoon.refreshTree();
                            }
                        }
                        stepMeta.hideStep();
                        spoon.refreshTree();
                        redraw();
                    }
                });
            }

            if (spoon.getTransMeta().isStepUsedInTransHops(stepMeta))
            {
                MenuItem miDetach = new MenuItem(mPop, SWT.CASCADE);
                miDetach.setText(Messages.getString("SpoonGraph.PopupMenu.DetachStep")); //$NON-NLS-1$
                miDetach.addSelectionListener(new SelectionAdapter()
                {
                    public void widgetSelected(SelectionEvent e)
                    {
                        detach(stepMeta);
                        selected_steps = null;
                    }
                });
            }

            new MenuItem(mPop, SWT.SEPARATOR);
            // ---------------------------------------

            MenuItem miPopFieldsBef = new MenuItem(mPop, SWT.CASCADE);
            miPopFieldsBef.setText(Messages.getString("SpoonGraph.PopupMenu.ShowInputFields")); //$NON-NLS-1$
            MenuItem miPopFieldsAft = new MenuItem(mPop, SWT.CASCADE);
            miPopFieldsAft.setText(Messages.getString("SpoonGraph.PopupMenu.ShowOutputFields")); //$NON-NLS-1$

            // Allign & Distribute options...
            new MenuItem(mPop, SWT.SEPARATOR);
            MenuItem miPopAD = new MenuItem(mPop, SWT.CASCADE);
            miPopAD.setText(Messages.getString("SpoonGraph.PopupMenu.AllignDistribute")); //$NON-NLS-1$

            Menu mPopAD = new Menu(miPopAD);
            MenuItem miPopALeft = new MenuItem(mPopAD, SWT.CASCADE);
            miPopALeft.setText(Messages.getString("SpoonGraph.PopupMenu.AllignLeft")); //$NON-NLS-1$
            MenuItem miPopARight = new MenuItem(mPopAD, SWT.CASCADE);
            miPopARight.setText(Messages.getString("SpoonGraph.PopupMenu.AllignRight")); //$NON-NLS-1$
            MenuItem miPopATop = new MenuItem(mPopAD, SWT.CASCADE);
            miPopATop.setText(Messages.getString("SpoonGraph.PopupMenu.AllignTop")); //$NON-NLS-1$
            MenuItem miPopABottom = new MenuItem(mPopAD, SWT.CASCADE);
            miPopABottom.setText(Messages.getString("SpoonGraph.PopupMenu.AllignBottom")); //$NON-NLS-1$
            new MenuItem(mPopAD, SWT.SEPARATOR);
            MenuItem miPopDHoriz = new MenuItem(mPopAD, SWT.CASCADE);
            miPopDHoriz.setText(Messages.getString("SpoonGraph.PopupMenu.DistributeHorizontally")); //$NON-NLS-1$
            MenuItem miPopDVertic = new MenuItem(mPopAD, SWT.CASCADE);
            miPopDVertic.setText(Messages.getString("SpoonGraph.PopupMenu.DistributeVertically")); //$NON-NLS-1$
            new MenuItem(mPopAD, SWT.SEPARATOR);
            MenuItem miPopSSnap = new MenuItem(mPopAD, SWT.CASCADE);
            miPopSSnap.setText(Messages.getString("SpoonGraph.PopupMenu.SnapToGrid") + Const.GRID_SIZE + ")\tALT-HOME"); //$NON-NLS-1$ //$NON-NLS-2$
            miPopAD.setMenu(mPopAD);

            miPopALeft.addSelectionListener(new SelectionAdapter()
            {
                public void widgetSelected(SelectionEvent e)
                {
                    allignleft();
                }
            });
            miPopARight.addSelectionListener(new SelectionAdapter()
            {
                public void widgetSelected(SelectionEvent e)
                {
                    allignright();
                }
            });
            miPopATop.addSelectionListener(new SelectionAdapter()
            {
                public void widgetSelected(SelectionEvent e)
                {
                    alligntop();
                }
            });
            miPopABottom.addSelectionListener(new SelectionAdapter()
            {
                public void widgetSelected(SelectionEvent e)
                {
                    allignbottom();
                }
            });
            miPopDHoriz.addSelectionListener(new SelectionAdapter()
            {
                public void widgetSelected(SelectionEvent e)
                {
                    distributehorizontal();
                }
            });
            miPopDVertic.addSelectionListener(new SelectionAdapter()
            {
                public void widgetSelected(SelectionEvent e)
                {
                    distributevertical();
                }
            });
            miPopSSnap.addSelectionListener(new SelectionAdapter()
            {
                public void widgetSelected(SelectionEvent e)
                {
                    snaptogrid(Const.GRID_SIZE);
                }
            });

            if (sels <= 1)
            {
                miPopAD.setEnabled(false);
            }

            /*
             * 
             * Check...
             */
            new MenuItem(mPop, SWT.SEPARATOR);
            MenuItem miPreview = new MenuItem(mPop, SWT.CASCADE);
            miPreview.setText(Messages.getString("SpoonGraph.PopupMenu.CheckSelectedSteps")); //$NON-NLS-1$
            miPreview.addSelectionListener(new SelectionAdapter()
            {
                public void widgetSelected(SelectionEvent e)
                {
                    spoon.checkTrans(true);
                }
            });

            
            /*
             * 
             * Check...
             */
            MenuItem miMapping = new MenuItem(mPop, SWT.CASCADE);
            miMapping.setText(Messages.getString("SpoonGraph.PopupMenu.GenerateMappingToThisStep")); //$NON-NLS-1$
            miMapping.addSelectionListener(new SelectionAdapter()
            {
                public void widgetSelected(SelectionEvent e)
                {
                    spoon.generateMapping(stepMeta);
                }
            });

            if (sels == 2)
            {
                miNewHop.addSelectionListener(new SelectionAdapter()
                {
                    public void widgetSelected(SelectionEvent e)
                    {
                        selected_steps = null;
                        newHop();
                    }
                });
            }

            miEditStep.addSelectionListener(new SelectionAdapter()
            {
                public void widgetSelected(SelectionEvent e)
                {
                    selected_steps = null;
                    editStep(stepMeta);
                }
            });
            miEditDesc.addSelectionListener(new SelectionAdapter()
            {
                public void widgetSelected(SelectionEvent e)
                {
                    editDescription(stepMeta);
                }
            });
            miStepDist.addSelectionListener(new SelectionAdapter()
            {
                public void widgetSelected(SelectionEvent e)
                {
                    stepMeta.distributes = true;
                    spoon.refreshGraph();
                    spoon.refreshTree();
                }
            });
            miStepCopy.addSelectionListener(new SelectionAdapter()
            {
                public void widgetSelected(SelectionEvent e)
                {
                    stepMeta.distributes = false;
                    spoon.refreshGraph();
                    spoon.refreshTree();
                }
            });
            miCopies.addSelectionListener(new SelectionAdapter()
            {
                public void widgetSelected(SelectionEvent e)
                {
                    selected_steps = null;
                    String tt = Messages.getString("SpoonGraph.Dialog.NrOfCopiesOfStep.Title"); //$NON-NLS-1$
                    String mt = Messages.getString("SpoonGraph.Dialog.NrOfCopiesOfStep.Message"); //$NON-NLS-1$
                    EnterNumberDialog nd = new EnterNumberDialog(shell, spoon.props, stepMeta.getCopies(), tt, mt);
                    int cop = nd.open();
                    if (cop >= 0)
                    {
                        if (cop == 0) cop = 1;
                        if (stepMeta.getCopies() != cop)
                        {
                            stepMeta.setCopies(cop);
                            spoon.refreshGraph();
                        }
                    }
                }
            });
            miDupeStep.addSelectionListener(new SelectionAdapter()
            {
                public void widgetSelected(SelectionEvent e)
                {
                    if (spoon.getTransMeta().nrSelectedSteps() <= 1)
                    {
                        spoon.dupeStep(stepMeta.getName());
                    }
                    else
                    {
                        for (int i = 0; i < spoon.getTransMeta().nrSteps(); i++)
                        {
                            StepMeta stepMeta = spoon.getTransMeta().getStep(i);
                            if (stepMeta.isSelected())
                            {
                                spoon.dupeStep(stepMeta.getName());
                            }
                        }
                    }
                }
            });

            // Copy the selected steps to the clipboard.
            miCopyStep.addSelectionListener(new SelectionAdapter()
            {
                public void widgetSelected(SelectionEvent e)
                {
                    spoon.copySelected(spoon.getTransMeta().getSelectedSteps(), spoon.getTransMeta().getSelectedNotes());
                }
            });

            miDelStep.addSelectionListener(new SelectionAdapter()
            {
                public void widgetSelected(SelectionEvent e)
                {
                    delSelected(stepMeta);
                }
            });

            miPopFieldsBef.addSelectionListener(new SelectionAdapter()
            {
                public void widgetSelected(SelectionEvent e)
                {
                    selected_steps = null;
                    inputOutputFields(stepMeta, true);
                }
            });
            miPopFieldsAft.addSelectionListener(new SelectionAdapter()
            {
                public void widgetSelected(SelectionEvent e)
                {
                    selected_steps = null;
                    inputOutputFields(stepMeta, false);
                }
            });
            setMenu(mPop);
        }
        else
        {
            final TransHopMeta hi = findHop(x, y);
            if (hi != null) // We clicked on a HOP!
            {
                mPop = new Menu((Control) this);
                MenuItem miEditHop = new MenuItem(mPop, SWT.CASCADE);
                miEditHop.setText(Messages.getString("SpoonGraph.PopupMenu.EditHop")); //$NON-NLS-1$
                MenuItem miFlipHop = new MenuItem(mPop, SWT.CASCADE);
                miFlipHop.setText(Messages.getString("SpoonGraph.PopupMenu.FlipDirection")); //$NON-NLS-1$
                MenuItem miDisHop = new MenuItem(mPop, SWT.CASCADE);
                if (hi.isEnabled())
                    miDisHop.setText(Messages.getString("SpoonGraph.PopupMenu.DisableHop")); //$NON-NLS-1$
                else
                    miDisHop.setText(Messages.getString("SpoonGraph.PopupMenu.EnableHop")); //$NON-NLS-1$
                MenuItem miDelHop = new MenuItem(mPop, SWT.CASCADE);
                miDelHop.setText(Messages.getString("SpoonGraph.PopupMenu.DeleteHop")); //$NON-NLS-1$

                miEditHop.addSelectionListener(new SelectionAdapter()
                {
                    public void widgetSelected(SelectionEvent e)
                    {
                        selrect = null;
                        editHop(hi);
                    }
                });
                miFlipHop.addSelectionListener(new SelectionAdapter()
                {
                    public void widgetSelected(SelectionEvent e)
                    {
                        selrect = null;

                        hi.flip();

                        if (spoon.getTransMeta().hasLoop(hi.getFromStep()))
                        {
                            spoon.refreshGraph();
                            MessageBox mb = new MessageBox(shell, SWT.YES | SWT.ICON_WARNING);
                            mb.setMessage(Messages.getString("SpoonGraph.Dialog.LoopsAreNotAllowed.Message")); //$NON-NLS-1$
                            mb.setText(Messages.getString("SpoonGraph.Dialog.LoopsAreNotAllowed.Title")); //$NON-NLS-1$
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
                });
                miDisHop.addSelectionListener(new SelectionAdapter()
                {
                    public void widgetSelected(SelectionEvent e)
                    {
                        selrect = null;
                        TransHopMeta before = (TransHopMeta) hi.clone();
                        hi.setEnabled(!hi.isEnabled());
                        if (spoon.getTransMeta().hasLoop(hi.getToStep()))
                        {
                            hi.setEnabled(!hi.isEnabled());
                            MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_WARNING);
                            mb.setMessage(Messages.getString("SpoonGraph.Dialog.LoopAfterHopEnabled.Message")); //$NON-NLS-1$
                            mb.setText(Messages.getString("SpoonGraph.Dialog.LoopAfterHopEnabled.Title")); //$NON-NLS-1$
                            mb.open();
                        }
                        else
                        {
                            TransHopMeta after = (TransHopMeta) hi.clone();
                            spoon.addUndoChange(new TransHopMeta[] { before }, new TransHopMeta[] { after }, new int[] { spoon.getTransMeta()
                                    .indexOfTransHop(hi) });
                            spoon.refreshGraph();
                            spoon.refreshTree();
                        }
                    }
                });
                miDelHop.addSelectionListener(new SelectionAdapter()
                {
                    public void widgetSelected(SelectionEvent e)
                    {
                        selrect = null;
                        int idx = spoon.getTransMeta().indexOfTransHop(hi);
                        spoon.addUndoDelete(new TransHopMeta[] { (TransHopMeta) hi.clone() }, new int[] { idx });
                        spoon.getTransMeta().removeTransHop(idx);
                        spoon.refreshTree();
                        spoon.refreshGraph();
                    }
                });
                setMenu(mPop);
            }
            else
            {
                // Clicked on the background: maybe we hit a note?
                final NotePadMeta ni = spoon.getTransMeta().getNote(x, y);
                if (ni != null) // A note
                {
                    // Delete note
                    // Edit note
                    Menu mPop = new Menu((Control) this);

                    MenuItem miNoteEdit = new MenuItem(mPop, SWT.CASCADE);
                    miNoteEdit.setText(Messages.getString("SpoonGraph.PopupMenu.EditNote")); //$NON-NLS-1$
                    MenuItem miNoteDel = new MenuItem(mPop, SWT.CASCADE);
                    miNoteDel.setText(Messages.getString("SpoonGraph.PopupMenu.DeleteNote")); //$NON-NLS-1$

                    miNoteEdit.addSelectionListener(new SelectionAdapter()
                    {
                        public void widgetSelected(SelectionEvent e)
                        {
                            selrect = null;
                            editNote(ni);
                        }
                    });
                    miNoteDel.addSelectionListener(new SelectionAdapter()
                    {
                        public void widgetSelected(SelectionEvent e)
                        {
                            selrect = null;
                            int idx = spoon.getTransMeta().indexOfNote(ni);
                            if (idx >= 0)
                            {
                                spoon.getTransMeta().removeNote(idx);
                                spoon.addUndoDelete(new NotePadMeta[] { (NotePadMeta) ni.clone() }, new int[] { idx });
                                redraw();
                            }
                        }
                    });

                    setMenu(mPop);
                }
                else
                // No step, hop or note: clicked on the background....
                {
                    // The popup-menu...
                    mPop = new Menu((Control) this);

                    MenuItem miNoteNew = new MenuItem(mPop, SWT.CASCADE);
                    miNoteNew.setText(Messages.getString("SpoonGraph.PopupMenu.NewNote")); //$NON-NLS-1$
                    miNoteNew.addSelectionListener(new SelectionAdapter()
                    {
                        public void widgetSelected(SelectionEvent e)
                        {
                            selrect = null;
                            String title = Messages.getString("SpoonGraph.Dialog.NoteEditor.Title"); //$NON-NLS-1$
                            String message = Messages.getString("SpoonGraph.Dialog.NoteEditor.Message"); //$NON-NLS-1$
                            EnterTextDialog dd = new EnterTextDialog(shell, title, message, ""); //$NON-NLS-1$
                            String n = dd.open();
                            if (n != null)
                            {
                                NotePadMeta npi = new NotePadMeta(n, lastclick.x, lastclick.y, Const.NOTE_MIN_SIZE, Const.NOTE_MIN_SIZE);
                                spoon.getTransMeta().addNote(npi);
                                spoon.addUndoNew(new NotePadMeta[] { npi }, new int[] { spoon.getTransMeta().indexOfNote(npi) });
                                redraw();
                            }
                        }
                    });

                    MenuItem miStepNew = new MenuItem(mPop, SWT.CASCADE);
                    miStepNew.setText(Messages.getString("SpoonGraph.PopupMenu.NewStep")); //$NON-NLS-1$
                    Menu mStep = new Menu(miStepNew);

                    StepLoader steploader = StepLoader.getInstance();
                    final StepPlugin sp[] = steploader.getStepsWithType(StepPlugin.TYPE_ALL);
                    for (int i = 0; i < sp.length; i++)
                    {
                        // System.out.println("Add step type :
                        // "+sp[i].getDescription());

                        MenuItem miStepX = new MenuItem(mStep, SWT.CASCADE);
                        miStepX.setText(sp[i].getDescription());

                        final String description = sp[i].getDescription();
                        miStepX.addSelectionListener(new SelectionAdapter()
                        {
                            public void widgetSelected(SelectionEvent e)
                            {
                                StepMeta stepMeta = spoon.newStep(description, description, false, true);
                                stepMeta.setLocation(mousex, mousey);
                                stepMeta.setDraw(true);
                                redraw();
                            }
                        });
                    }

                    MenuItem miPasteStep = new MenuItem(mPop, SWT.CASCADE);
                    miPasteStep.setText(Messages.getString("SpoonGraph.PopupMenu.PasteStepFromClipboard")); //$NON-NLS-1$

                    final String clipcontent = spoon.fromClipboard();
                    if (clipcontent == null) miPasteStep.setEnabled(false);
                    // Past steps on the clipboard to the transformation...
                    miPasteStep.addSelectionListener(new SelectionAdapter()
                    {
                        public void widgetSelected(SelectionEvent e)
                        {
                            Point loc = new Point(mousex, mousey);
                            spoon.pasteXML(clipcontent, loc);
                        }
                    });

                    miStepNew.setMenu(mStep);

                    setMenu(mPop);
                }
            }
        }
    }

    private void setToolTip(int x, int y)
    {
        final StepMeta stepMeta = spoon.getTransMeta().getStep(x, y, iconsize);
        if (stepMeta != null) // We clicked on a Step!
        {
            // Also: set the tooltip!
            if (stepMeta.getDescription() != null)
            {
                String desc = stepMeta.getDescription();
                int le = desc.length() >= 200 ? 200 : desc.length();
                String tip = desc.substring(0, le);
                if (!tip.equalsIgnoreCase(getToolTipText()))
                {
                    setToolTipText(tip);
                }
            }
            else
            {
                setToolTipText(stepMeta.getName());
            }
        }
        else
        {
            final TransHopMeta hi = findHop(x, y);
            if (hi != null) // We clicked on a HOP!
            {
                // Set the tooltip for the hop:
                setToolTipText(hi.toString());
            }
            else
            {
                setToolTipText(null);
            }
        }
    }

    public void delSelected(StepMeta stMeta)
    {
        int nrsels = spoon.getTransMeta().nrSelectedSteps();
        if (nrsels == 0)
        {
            spoon.delStep(stMeta.getName());
        }
        else
        {
            if (stMeta != null && !stMeta.isSelected()) nrsels++;

            MessageBox mb = new MessageBox(shell, SWT.YES | SWT.NO | SWT.ICON_WARNING);
            mb.setText(Messages.getString("SpoonGraph.Dialog.Warning.DeleteSteps.Title")); //$NON-NLS-1$
            String message = Messages.getString("SpoonGraph.Dialog.Warning.DeleteSteps.Message") + nrsels + Messages.getString("SpoonGraph.Dialog.Warning.DeleteSteps2.Message") + Const.CR; //$NON-NLS-1$ //$NON-NLS-2$
            for (int i = spoon.getTransMeta().nrSteps() - 1; i >= 0; i--)
            {
                StepMeta stepMeta = spoon.getTransMeta().getStep(i);
                if (stepMeta.isSelected() || (stMeta != null && stMeta.equals(stepMeta)))
                {
                    message += "  --> " + stepMeta.getName() + Const.CR; //$NON-NLS-1$
                }
            }

            mb.setMessage(message);
            int result = mb.open();
            if (result == SWT.YES)
            {
                for (int i = spoon.getTransMeta().nrSteps() - 1; i >= 0; i--)
                {
                    StepMeta stepMeta = spoon.getTransMeta().getStep(i);
                    if (stepMeta.isSelected() || (stMeta != null && stMeta.equals(stepMeta)))
                    {
                        spoon.delStep(stepMeta.getName());
                    }
                }
            }
        }
    }

    public void editDescription(StepMeta stepMeta)
    {
        String title = Messages.getString("SpoonGraph.Dialog.StepDescription.Title"); //$NON-NLS-1$
        String message = Messages.getString("SpoonGraph.Dialog.StepDescription.Message"); //$NON-NLS-1$
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

        SearchFieldsProgressDialog op = new SearchFieldsProgressDialog(spoon.getTransMeta(), stepMeta, before);
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
                        try { spoon.getTransMeta().cancelQueries(); } catch(Exception e) {};
                    }
                }
            };
            // Dump the cancel looker in the background!
            new Thread(run).start();
            

            pmd.run(true, true, op);
        }
        catch (InvocationTargetException e)
        {
            new ErrorDialog(shell, spoon.props, Messages.getString("SpoonGraph.Dialog.GettingFields.Title"), Messages.getString("SpoonGraph.Dialog.GettingFields.Message"), e); //$NON-NLS-1$ //$NON-NLS-2$
        }
        catch (InterruptedException e)
        {
            new ErrorDialog(shell, spoon.props, Messages.getString("SpoonGraph.Dialog.GettingFields.Title"), Messages.getString("SpoonGraph.Dialog.GettingFields.Message"), e); //$NON-NLS-1$ //$NON-NLS-2$
        }

        Row fields = op.getFields();

        if (fields != null && fields.size() > 0)
        {
            StepFieldsDialog sfd = new StepFieldsDialog(shell, SWT.NONE, log, stepMeta.getName(), fields, spoon.props);
            String sn = (String) sfd.open();
            if (sn != null)
            {
                StepMeta esi = spoon.getTransMeta().findStep(sn);
                if (esi != null)
                {
                    editStep(esi);
                }
            }
        }
        else
        {
            MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_INFORMATION);
            mb.setMessage(Messages.getString("SpoonGraph.Dialog.CouldntFindFields.Message")); //$NON-NLS-1$
            mb.setText(Messages.getString("SpoonGraph.Dialog.CouldntFindFields.Title")); //$NON-NLS-1$
            mb.open();
        }

    }

    public void paintControl(PaintEvent e)
    {
        Point area = getArea();
        if (area.x == 0 || area.y == 0) return; // nothing to do!

        Display disp = shell.getDisplay();

        Image img = getTransformationImage(disp, area.x, area.y);
        e.gc.drawImage(img, 0, 0);
        img.dispose();

        spoon.setShellText();
    }

    public Image getTransformationImage(Display disp, int x, int y)
    {
        TransPainter transPainter = new TransPainter(spoon.getTransMeta(), new Point(x, y), hori, vert, candidate, drop_candidate, selrect);
        Image img = transPainter.getTransformationImage();

        return img;
    }

    public void drawTrans(GC gc)
    {
        if (spoon.props.isAntiAliasingEnabled()) gc.setAntialias(SWT.ON);

        shadowsize = spoon.props.getShadowSize();

        Point area = getArea();
        Point max = spoon.getTransMeta().getMaximum();
        Point thumb = getThumb(area, max);
        offset = getOffset(thumb, area);

        hori.setThumb(thumb.x);
        vert.setThumb(thumb.y);

        gc.setFont(GUIResource.getInstance().getFontNote());

        // First the notes
        for (int i = 0; i < spoon.getTransMeta().nrNotes(); i++)
        {
            NotePadMeta ni = spoon.getTransMeta().getNote(i);
            drawNote(gc, ni);
        }

        gc.setFont(GUIResource.getInstance().getFontGraph());
        gc.setBackground(GUIResource.getInstance().getColorGraph());

        if (shadowsize > 0)
        {
            for (int i = 0; i < spoon.getTransMeta().nrSteps(); i++)
            {
                StepMeta stepMeta = spoon.getTransMeta().getStep(i);
                if (stepMeta.isDrawn()) drawStepShadow(gc, stepMeta);
            }
        }

        for (int i = 0; i < spoon.getTransMeta().nrTransHops(); i++)
        {
            TransHopMeta hi = spoon.getTransMeta().getTransHop(i);
            drawHop(gc, hi);
        }

        if (candidate != null)
        {
            drawHop(gc, candidate, true);
        }

        for (int i = 0; i < spoon.getTransMeta().nrSteps(); i++)
        {
            StepMeta stepMeta = spoon.getTransMeta().getStep(i);
            if (stepMeta.isDrawn()) drawStep(gc, stepMeta);
        }

        if (drop_candidate != null)
        {
            gc.setLineStyle(SWT.LINE_SOLID);
            gc.setForeground(GUIResource.getInstance().getColorBlack());
            Point screen = real2screen(drop_candidate.x, drop_candidate.y);
            gc.drawRectangle(screen.x, screen.y, iconsize, iconsize);
        }

        drawRect(gc, selrect);
    }

    private void drawHop(GC gc, TransHopMeta hi)
    {
        drawHop(gc, hi, false);
    }

    private void drawNote(GC gc, NotePadMeta ni)
    {
        int flags = SWT.DRAW_DELIMITER | SWT.DRAW_TAB | SWT.DRAW_TRANSPARENT;

        if (ni.isSelected())
            gc.setLineWidth(2);
        else
            gc.setLineWidth(1);

        org.eclipse.swt.graphics.Point ext = gc.textExtent(ni.getNote(), flags);
        Point p = new Point(ext.x, ext.y);
        Point loc = ni.getLocation();
        Point note = real2screen(loc.x, loc.y);
        int margin = Const.NOTE_MARGIN;
        p.x += 2 * margin;
        p.y += 2 * margin;
        int width = ni.width;
        int height = ni.height;
        if (p.x > width) width = p.x;
        if (p.y > height) height = p.y;

        int noteshape[] = new int[] { note.x, note.y, // Top left
                note.x + width + 2 * margin, note.y, // Top right
                note.x + width + 2 * margin, note.y + height, // bottom right 1
                note.x + width, note.y + height + 2 * margin, // bottom right 2
                note.x + width, note.y + height, // bottom right 3
                note.x + width + 2 * margin, note.y + height, // bottom right 1
                note.x + width, note.y + height + 2 * margin, // bottom right 2
                note.x, note.y + height + 2 * margin // bottom left
        };
        int s = spoon.props.getShadowSize();
        int shadow[] = new int[] { note.x + s, note.y + s, // Top left
                note.x + width + 2 * margin + s, note.y + s, // Top right
                note.x + width + 2 * margin + s, note.y + height + s, // bottom
                // right 1
                note.x + width + s, note.y + height + 2 * margin + s, // bottom
                // right 2
                note.x + s, note.y + height + 2 * margin + s // bottom left
        };

        gc.setForeground(GUIResource.getInstance().getColorLightGray());
        gc.setBackground(GUIResource.getInstance().getColorLightGray());
        gc.fillPolygon(shadow);

        gc.setForeground(GUIResource.getInstance().getColorGray());
        gc.setBackground(GUIResource.getInstance().getColorYellow());

        gc.fillPolygon(noteshape);
        gc.drawPolygon(noteshape);
        // gc.fillRectangle(ni.xloc, ni.yloc, width+2*margin, heigth+2*margin);
        // gc.drawRectangle(ni.xloc, ni.yloc, width+2*margin, heigth+2*margin);
        gc.setForeground(GUIResource.getInstance().getColorBlack());
        gc.drawText(ni.getNote(), note.x + margin, note.y + margin, flags);

        ni.width = width; // Save for the "mouse" later on...
        ni.height = height;

        if (ni.isSelected())
            gc.setLineWidth(1);
        else
            gc.setLineWidth(2);
    }

    private void drawHop(GC gc, TransHopMeta hi, boolean is_candidate)
    {
        StepMeta fs = hi.getFromStep();
        StepMeta ts = hi.getToStep();

        if (fs != null && ts != null)
        {
            if (shadowsize > 0) drawLineShadow(gc, fs, ts, hi, false);
            drawLine(gc, fs, ts, hi, is_candidate);
        }
    }

    private void drawStepShadow(GC gc, StepMeta stepMeta)
    {
        if (stepMeta == null) return;

        Point pt = stepMeta.getLocation();

        int x, y;
        if (pt != null)
        {
            x = pt.x;
            y = pt.y;
        }
        else
        {
            x = 50;
            y = 50;
        }

        Point screen = real2screen(x, y);

        // First draw the shadow...
        gc.setBackground(GUIResource.getInstance().getColorLightGray());
        gc.setForeground(GUIResource.getInstance().getColorLightGray());
        int s = shadowsize;
        gc.fillRectangle(screen.x + s, screen.y + s, iconsize, iconsize);
    }

    private void drawStep(GC gc, StepMeta stepMeta)
    {
        if (stepMeta == null) return;

        Point pt = stepMeta.getLocation();

        int x, y;
        if (pt != null)
        {
            x = pt.x;
            y = pt.y;
        }
        else
        {
            x = 50;
            y = 50;
        }

        Point screen = real2screen(x, y);

        String name = stepMeta.getName();

        if (stepMeta.isSelected())
            gc.setLineWidth(linewidth + 2);
        else
            gc.setLineWidth(linewidth);
        gc.setBackground(GUIResource.getInstance().getColorRed());
        gc.setForeground(GUIResource.getInstance().getColorBlack());
        gc.fillRectangle(screen.x, screen.y, iconsize, iconsize);
        String steptype = stepMeta.getStepID();
        Image im = (Image) GUIResource.getInstance().getImagesSteps().get(steptype);
        if (im != null) // Draw the icon!
        {
            org.eclipse.swt.graphics.Rectangle bounds = im.getBounds();
            gc.drawImage(im, 0, 0, bounds.width, bounds.height, screen.x, screen.y, iconsize, iconsize);
        }
        gc.setBackground(GUIResource.getInstance().getColorBackground());
        gc.drawRectangle(screen.x - 1, screen.y - 1, iconsize + 1, iconsize + 1);
        // gc.setXORMode(true);
        org.eclipse.swt.graphics.Point textsize = gc.textExtent(name);

        int xpos = screen.x + (iconsize / 2) - (textsize.x / 2);
        int ypos = screen.y + iconsize + 5;

        if (shadowsize > 0)
        {
            gc.setForeground(GUIResource.getInstance().getColorLightGray());
            gc.drawText(name, xpos + shadowsize, ypos + shadowsize, SWT.DRAW_TRANSPARENT);
        }

        gc.setForeground(GUIResource.getInstance().getColorBlack());
        gc.drawText(name, xpos, ypos, SWT.DRAW_TRANSPARENT);

        if (stepMeta.getCopies() > 1)
        {
            gc.setBackground(GUIResource.getInstance().getColorBackground());
            gc.setForeground(GUIResource.getInstance().getColorBlack());
            gc.drawText("x" + stepMeta.getCopies(), screen.x - 5, screen.y - 5); //$NON-NLS-1$
        }
    }

    private void drawLineShadow(GC gc, StepMeta fs, StepMeta ts, TransHopMeta hi, boolean is_candidate)
    {
        int line[] = getLine(fs, ts);
        int s = shadowsize;
        for (int i = 0; i < line.length; i++)
            line[i] += s;

        gc.setLineWidth(linewidth);

        gc.setForeground(GUIResource.getInstance().getColorLightGray());

        drawArrow(gc, line);
    }

    private void drawLine(GC gc, StepMeta fs, StepMeta ts, TransHopMeta hi, boolean is_candidate)
    {
        StepMetaInterface fsii = fs.getStepMetaInterface();
        StepMetaInterface tsii = ts.getStepMetaInterface();

        int line[] = getLine(fs, ts);

        gc.setLineWidth(linewidth);
        Color col;

        if (is_candidate)
        {
            col = GUIResource.getInstance().getColorBlue();
        }
        else
        {
            if (hi.isEnabled())
            {
                String[] targetSteps = fsii.getTargetSteps();
                String[] infoSteps = tsii.getInfoSteps();

                // System.out.println("Normal step: "+fs+" --> "+ts+",
                // "+(infoSteps!=null)+", "+(targetSteps!=null));

                if (targetSteps == null) // Normal link: distribute or copy data...
                {
                    // Or perhaps it's an informational link: draw different
                    // color...
                    if (Const.indexOfString(fs.getName(), infoSteps) >= 0)
                    {
                        if (fs.distributes)
                            col = GUIResource.getInstance().getColorYellow();
                        else
                            col = GUIResource.getInstance().getColorMagenta();
                    }
                    else
                    {
                        if (fs.distributes)
                            col = GUIResource.getInstance().getColorGreen();
                        else
                            col = GUIResource.getInstance().getColorRed();
                    }
                }
                else
                {
                    // Visual check to see if the target step is specified...
                    if (Const.indexOfString(ts.getName(), fsii.getTargetSteps()) >= 0)
                    {
                        col = GUIResource.getInstance().getColorBlack();
                    }
                    else
                    {
                        gc.setLineStyle(SWT.LINE_DOT);
                        col = GUIResource.getInstance().getColorOrange();
                    }
                }
            }
            else
            {
                col = GUIResource.getInstance().getColorGray();
            }
        }

        gc.setForeground(col);

        if (hi.split) gc.setLineWidth(linewidth + 2);

        drawArrow(gc, line);

        if (hi.split) gc.setLineWidth(linewidth);

        gc.setForeground(GUIResource.getInstance().getColorBlack());
        gc.setBackground(GUIResource.getInstance().getColorBackground());
        gc.setLineStyle(SWT.LINE_SOLID);
    }

    private Point getArea()
    {
        org.eclipse.swt.graphics.Rectangle rect = getClientArea();
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
        Point max = spoon.getTransMeta().getMaximum();
        Point thumb = getThumb(area, max);
        Point offset = getOffset(thumb, area);

        return offset;
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
        spoon.editStepInfo(stepMeta);
    }

    private void editNote(NotePadMeta ni)
    {
        NotePadMeta before = (NotePadMeta) ni.clone();

        String title = Messages.getString("SpoonGraph.Dialog.EditNote.Title"); //$NON-NLS-1$
        String message = Messages.getString("SpoonGraph.Dialog.EditNote.Message"); //$NON-NLS-1$
        EnterTextDialog dd = new EnterTextDialog(shell, title, message, ni.getNote());
        String n = dd.open();
        if (n != null)
        {
            ni.setChanged();
            ni.setNote(n);
            ni.width = Const.NOTE_MIN_SIZE;
            ni.height = Const.NOTE_MIN_SIZE;

            NotePadMeta after = (NotePadMeta) ni.clone();
            spoon.addUndoChange(new NotePadMeta[] { before }, new NotePadMeta[] { after }, new int[] { spoon.getTransMeta().indexOfNote(ni) });
            spoon.refreshGraph();
        }
    }

    private void editHop(TransHopMeta hopinfo)
    {
        String name = hopinfo.toString();
        log.logDebug(toString(), Messages.getString("SpoonGraph.Logging.EditingHop") + name); //$NON-NLS-1$
        spoon.editHop(name);
    }

    private void newHop()
    {
        StepMeta fr = spoon.getTransMeta().getSelectedStep(0);
        StepMeta to = spoon.getTransMeta().getSelectedStep(1);
        spoon.newHop(fr, to);
    }

    private void drawArrow(GC gc, int line[])
    {
        double theta = Math.toRadians(10); // arrowhead sharpness
        int size = 30 + (linewidth - 1) * 5; // arrowhead length

        Point screen_from = real2screen(line[0], line[1]);
        Point screen_to = real2screen(line[2], line[3]);

        int mx, my;
        int x1 = screen_from.x;
        int y1 = screen_from.y;
        int x2 = screen_to.x;
        int y2 = screen_to.y;
        int x3;
        int y3;
        int x4;
        int y4;
        int a, b, dist;
        double factor, angle;

        gc.drawLine(x1, y1, x2, y2);

        // in between 2 points
        mx = x1 + (x2 - x1) / 2;
        my = y1 + (y2 - y1) / 2;

        a = Math.abs(x2 - x1);
        b = Math.abs(y2 - y1);
        dist = (int) Math.sqrt(a * a + b * b);

        // determine factor (position of arrow to left side or right side
        // 0-->100%)
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
        // gc.drawLine( mx, my, x3, y3 );
        // gc.drawLine( mx, my, x4, y4 );
        // gc.drawLine( x3, y3, x4, y4 );

        Color fore = gc.getForeground();
        Color back = gc.getBackground();
        gc.setBackground(fore);
        gc.fillPolygon(new int[] { mx, my, x3, y3, x4, y4 });
        gc.setBackground(back);
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

    private void snaptogrid(int size)
    {
        if (spoon.getTransMeta().nrSelectedSteps() == 0) return;

        // First look for the minimum x coordinate...

        StepMeta steps[] = new StepMeta[spoon.getTransMeta().nrSelectedSteps()];
        Point before[] = new Point[spoon.getTransMeta().nrSelectedSteps()];
        Point after[] = new Point[spoon.getTransMeta().nrSelectedSteps()];
        int nr = 0;

        for (int i = 0; i < spoon.getTransMeta().nrSteps(); i++)
        {
            StepMeta stepMeta = spoon.getTransMeta().getStep(i);
            if (stepMeta.isSelected() && stepMeta.isDrawn())
            {
                steps[nr] = stepMeta;
                Point p = stepMeta.getLocation();
                before[nr] = new Point(p.x, p.y);

                // What's the modulus ?
                int dx = p.x % size;
                int dy = p.y % size;

                // Correct the location to the nearest grid line!
                // This means for size = 10
                // x = 3: dx=3, dx<=5 --> x=3-3 = 0;
                // x = 7: dx=7, dx> 5 --> x=3+10-3 = 10;
                // x = 10: dx=0, dx<=5 --> x=10-0 = 10;

                if (dx > size / 2)
                    p.x += size - dx;
                else
                    p.x -= dx;
                if (dy > size / 2)
                    p.y += size - dy;
                else
                    p.y -= dy;
                after[nr] = new Point(p.x, p.y);
                nr++;
            }
        }

        spoon.addUndoPosition(steps, spoon.getTransMeta().getStepIndexes(steps), before, after);

        redraw();
    }

    private void allignleft()
    {
        if (spoon.getTransMeta().nrSelectedSteps() == 0) return;

        StepMeta steps[] = new StepMeta[spoon.getTransMeta().nrSelectedSteps()];
        Point before[] = new Point[spoon.getTransMeta().nrSelectedSteps()];
        Point after[] = new Point[spoon.getTransMeta().nrSelectedSteps()];
        int nr = 0;

        int min = 99999;

        // First look for the minimum x coordinate...
        for (int i = 0; i < spoon.getTransMeta().nrSteps(); i++)
        {
            StepMeta stepMeta = spoon.getTransMeta().getStep(i);
            if (stepMeta.isSelected() && stepMeta.isDrawn())
            {
                Point p = stepMeta.getLocation();
                if (p.x < min) min = p.x;
            }
        }
        // Then apply the coordinate...
        for (int i = 0; i < spoon.getTransMeta().nrSteps(); i++)
        {
            StepMeta stepMeta = spoon.getTransMeta().getStep(i);
            if (stepMeta.isSelected() && stepMeta.isDrawn())
            {
                steps[nr] = stepMeta;
                Point p = stepMeta.getLocation();
                before[nr] = new Point(p.x, p.y);
                stepMeta.setLocation(min, p.y);
                after[nr] = new Point(min, p.y);
                nr++;
            }
        }
        spoon.addUndoPosition(steps, spoon.getTransMeta().getStepIndexes(steps), before, after);
        redraw();
    }

    private void allignright()
    {
        if (spoon.getTransMeta().nrSelectedSteps() == 0) return;

        StepMeta steps[] = new StepMeta[spoon.getTransMeta().nrSelectedSteps()];
        Point before[] = new Point[spoon.getTransMeta().nrSelectedSteps()];
        Point after[] = new Point[spoon.getTransMeta().nrSelectedSteps()];
        int nr = 0;

        int max = -99999;

        // First look for the maximum x coordinate...
        for (int i = 0; i < spoon.getTransMeta().nrSteps(); i++)
        {
            StepMeta stepMeta = spoon.getTransMeta().getStep(i);
            if (stepMeta.isSelected() && stepMeta.isDrawn())
            {
                Point p = stepMeta.getLocation();
                if (p.x > max) max = p.x;
            }
        }
        // Then apply the coordinate...
        for (int i = 0; i < spoon.getTransMeta().nrSteps(); i++)
        {
            StepMeta stepMeta = spoon.getTransMeta().getStep(i);
            if (stepMeta.isSelected() && stepMeta.isDrawn())
            {
                steps[nr] = stepMeta;
                Point p = stepMeta.getLocation();
                before[nr] = new Point(p.x, p.y);
                stepMeta.setLocation(max, p.y);
                after[nr] = new Point(max, p.y);
                nr++;
            }
        }
        spoon.addUndoPosition(steps, spoon.getTransMeta().getStepIndexes(steps), before, after);
        redraw();
    }

    private void alligntop()
    {
        if (spoon.getTransMeta().nrSelectedSteps() == 0) return;

        StepMeta steps[] = new StepMeta[spoon.getTransMeta().nrSelectedSteps()];
        Point before[] = new Point[spoon.getTransMeta().nrSelectedSteps()];
        Point after[] = new Point[spoon.getTransMeta().nrSelectedSteps()];
        int nr = 0;

        int min = 99999;

        // First look for the minimum y coordinate...
        for (int i = 0; i < spoon.getTransMeta().nrSteps(); i++)
        {
            StepMeta stepMeta = spoon.getTransMeta().getStep(i);
            if (stepMeta.isSelected() && stepMeta.isDrawn())
            {
                Point p = stepMeta.getLocation();
                if (p.y < min) min = p.y;
            }
        }
        // Then apply the coordinate...
        for (int i = 0; i < spoon.getTransMeta().nrSteps(); i++)
        {
            StepMeta stepMeta = spoon.getTransMeta().getStep(i);
            if (stepMeta.isSelected() && stepMeta.isDrawn())
            {
                steps[nr] = stepMeta;
                Point p = stepMeta.getLocation();
                before[nr] = new Point(p.x, p.y);
                stepMeta.setLocation(p.x, min);
                after[nr] = new Point(p.x, min);
                nr++;
            }
        }
        spoon.addUndoPosition(steps, spoon.getTransMeta().getStepIndexes(steps), before, after);
        redraw();
    }

    private void allignbottom()
    {
        if (spoon.getTransMeta().nrSelectedSteps() == 0) return;

        StepMeta steps[] = new StepMeta[spoon.getTransMeta().nrSelectedSteps()];
        Point before[] = new Point[spoon.getTransMeta().nrSelectedSteps()];
        Point after[] = new Point[spoon.getTransMeta().nrSelectedSteps()];
        int nr = 0;

        int max = -99999;

        // First look for the maximum y coordinate...
        for (int i = 0; i < spoon.getTransMeta().nrSteps(); i++)
        {
            StepMeta stepMeta = spoon.getTransMeta().getStep(i);
            if (stepMeta.isSelected() && stepMeta.isDrawn())
            {
                Point p = stepMeta.getLocation();
                if (p.y > max) max = p.y;
            }
        }
        // Then apply the coordinate...
        for (int i = 0; i < spoon.getTransMeta().nrSteps(); i++)
        {
            StepMeta stepMeta = spoon.getTransMeta().getStep(i);
            if (stepMeta.isSelected() && stepMeta.isDrawn())
            {
                steps[nr] = stepMeta;
                Point p = stepMeta.getLocation();
                before[nr] = new Point(p.x, p.y);
                stepMeta.setLocation(p.x, max);
                after[nr] = new Point(p.x, max);
                nr++;
            }
        }
        spoon.addUndoPosition(steps, spoon.getTransMeta().getStepIndexes(steps), before, after);
        redraw();
    }

    private void distributehorizontal()
    {
        if (spoon.getTransMeta().nrSelectedSteps() == 0) return;

        StepMeta steps[] = new StepMeta[spoon.getTransMeta().nrSelectedSteps()];
        Point before[] = new Point[spoon.getTransMeta().nrSelectedSteps()];
        Point after[] = new Point[spoon.getTransMeta().nrSelectedSteps()];

        int min = 99999;
        int max = -99999;
        int sels = spoon.getTransMeta().nrSelectedSteps();
        if (sels <= 1) return;
        int order[] = new int[sels];

        // First look for the minimum & maximum x coordinate...
        int selnr = 0;
        for (int i = 0; i < spoon.getTransMeta().nrSteps(); i++)
        {
            StepMeta stepMeta = spoon.getTransMeta().getStep(i);
            if (stepMeta.isSelected() && stepMeta.isDrawn())
            {
                Point p = stepMeta.getLocation();
                if (p.x < min) min = p.x;
                if (p.x > max) max = p.x;
                order[selnr] = i;
                selnr++;
            }
        }

        // Difficult to keep the steps in the correct order.
        // If you just set the x-coordinates, you get special effects.
        // Best is to keep the current order of things.
        // First build an arraylist and store the order there.
        // Then sort order[], based upon the coordinate of the step.
        for (int i = 0; i < sels; i++)
        {
            for (int j = 0; j < sels - 1; j++)
            {
                Point p1 = spoon.getTransMeta().getStep(order[j]).getLocation();
                Point p2 = spoon.getTransMeta().getStep(order[j + 1]).getLocation();
                if (p1.x > p2.x) // swap
                {
                    int dummy = order[j];
                    order[j] = order[j + 1];
                    order[j + 1] = dummy;
                }
            }
        }

        // The distance between two steps becomes.
        int distance = (max - min) / (sels - 1);

        for (int i = 0; i < sels; i++)
        {
            steps[i] = spoon.getTransMeta().getStep(order[i]);
            Point p = steps[i].getLocation();
            before[i] = new Point(p.x, p.y);
            p.x = min + (i * distance);
            after[i] = new Point(p.x, p.y);
        }

        // Undo!
        spoon.addUndoPosition(steps, spoon.getTransMeta().getStepIndexes(steps), before, after);

        redraw();
    }

    public void distributevertical()
    {
        if (spoon.getTransMeta().nrSelectedSteps() == 0) return;

        StepMeta steps[] = new StepMeta[spoon.getTransMeta().nrSelectedSteps()];
        Point before[] = new Point[spoon.getTransMeta().nrSelectedSteps()];
        Point after[] = new Point[spoon.getTransMeta().nrSelectedSteps()];

        int min = 99999;
        int max = -99999;
        int sels = spoon.getTransMeta().nrSelectedSteps();
        if (sels <= 1) return;
        int order[] = new int[sels];

        // First look for the minimum & maximum y coordinate...
        int selnr = 0;
        for (int i = 0; i < spoon.getTransMeta().nrSteps(); i++)
        {
            StepMeta stepMeta = spoon.getTransMeta().getStep(i);
            if (stepMeta.isSelected() && stepMeta.isDrawn())
            {
                Point p = stepMeta.getLocation();
                if (p.y < min) min = p.y;
                if (p.y > max) max = p.y;
                order[selnr] = i;
                selnr++;
            }
        }

        // Difficult to keep the steps in the correct order.
        // If you just set the x-coordinates, you get special effects.
        // Best is to keep the current order of things.
        // First build an arraylist and store the order there.
        // Then sort order[], based upon the coordinate of the step.
        for (int i = 0; i < sels; i++)
        {
            for (int j = 0; j < sels - 1; j++)
            {
                Point p1 = spoon.getTransMeta().getStep(order[j]).getLocation();
                Point p2 = spoon.getTransMeta().getStep(order[j + 1]).getLocation();
                if (p1.y > p2.y) // swap
                {
                    int dummy = order[j];
                    order[j] = order[j + 1];
                    order[j + 1] = dummy;
                }
            }
        }

        // The distance between two steps becomes.
        int distance = (max - min) / (sels - 1);

        for (int i = 0; i < sels; i++)
        {
            steps[i] = spoon.getTransMeta().getStep(order[i]);
            Point p = steps[i].getLocation();
            before[i] = new Point(p.x, p.y);
            p.y = min + (i * distance);
            after[i] = new Point(p.x, p.y);
        }

        // Undo!
        spoon.addUndoPosition(steps, spoon.getTransMeta().getStepIndexes(steps), before, after);

        redraw();
    }

    private void detach(StepMeta stepMeta)
    {
        TransHopMeta hfrom = spoon.getTransMeta().findTransHopTo(stepMeta);
        TransHopMeta hto = spoon.getTransMeta().findTransHopFrom(stepMeta);

        if (hfrom != null && hto != null)
        {
            if (spoon.getTransMeta().findTransHop(hfrom.getFromStep(), hto.getToStep()) == null)
            {
                TransHopMeta hnew = new TransHopMeta(hfrom.getFromStep(), hto.getToStep());
                spoon.getTransMeta().addTransHop(hnew);
                spoon.addUndoNew(new TransHopMeta[] { hnew }, new int[] { spoon.getTransMeta().indexOfTransHop(hnew) });
                spoon.refreshTree();
            }
        }
        if (hfrom != null)
        {
            int fromidx = spoon.getTransMeta().indexOfTransHop(hfrom);
            if (fromidx >= 0)
            {
                spoon.getTransMeta().removeTransHop(fromidx);
                spoon.refreshTree();
            }
        }
        if (hto != null)
        {
            int toidx = spoon.getTransMeta().indexOfTransHop(hto);
            if (toidx >= 0)
            {
                spoon.getTransMeta().removeTransHop(toidx);
                spoon.refreshTree();
            }
        }
        spoon.refreshTree();
        redraw();
    }

    private void drawRect(GC gc, Rectangle rect)
    {
        if (rect == null) return;
        gc.setLineStyle(SWT.LINE_DASHDOT);
        gc.setLineWidth(linewidth);
        gc.setForeground(GUIResource.getInstance().getColorGray());
        gc.drawRectangle(rect.x + offset.x, rect.y + offset.y, rect.width, rect.height);
        gc.setLineStyle(SWT.LINE_SOLID);
    }

    // Preview the selected steps...
    public void preview()
    {
        // Create a new transformation
        TransMeta preview = new TransMeta();

        // Copy the selected steps into it...
        for (int i = 0; i < spoon.getTransMeta().nrSteps(); i++)
        {
            StepMeta stepMeta = spoon.getTransMeta().getStep(i);
            if (stepMeta.isSelected())
            {
                preview.addStep(stepMeta);
            }
        }

        // Copy the relevant TransHops into it...
        for (int i = 0; i < spoon.getTransMeta().nrTransHops(); i++)
        {
            TransHopMeta hi = spoon.getTransMeta().getTransHop(i);
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
        linewidth = spoon.props.getLineWidth();
    }

    public String toString()
    {
        return this.getClass().getName();
    }
}
