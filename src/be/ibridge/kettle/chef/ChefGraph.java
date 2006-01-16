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

 
package be.ibridge.kettle.chef;
import java.util.StringTokenizer;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
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
import be.ibridge.kettle.core.GUIResource;
import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.NotePadMeta;
import be.ibridge.kettle.core.Point;
import be.ibridge.kettle.core.Rectangle;
import be.ibridge.kettle.core.dialog.EnterTextDialog;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.exception.KettleXMLException;
import be.ibridge.kettle.job.JobHopMeta;
import be.ibridge.kettle.job.JobMeta;
import be.ibridge.kettle.job.entry.JobEntryCopy;
import be.ibridge.kettle.job.entry.JobEntryInterface;
import be.ibridge.kettle.job.entry.job.JobEntryJob;
import be.ibridge.kettle.job.entry.trans.JobEntryTrans;
import be.ibridge.kettle.spoon.Spoon;
import be.ibridge.kettle.trans.TransMeta;


/**
 * Handles the display of Jobs in Chef, in a graphical form.
 * 
 * @author Matt
 * Created on 17-mei-2003
 *
 */

public class ChefGraph extends Canvas 
{
	private static final int HOP_SEL_MARGIN = 9;

	private Shell shell;
	private ChefGraph canvas;
	private LogWriter log;
    
	private int iconsize;
	private int linewidth;
	private Point lastclick;

	private JobEntryCopy selected_entries[];
	private JobEntryCopy selected_icon;
	private Point          prev_locations[];
	private NotePadMeta    selected_note;
	private Point previous_note_location;
    private Point          lastMove;

	private JobHopMeta     hop_candidate;
	private Point drop_candidate;
	private Chef chef;

	private Point offset, iconoffset, noteoffset;
	private ScrollBar hori;
	private ScrollBar vert;

	public boolean shift, control;
	private boolean split_hop;
	private int last_button;
	private JobHopMeta last_hop_split;
	private Rectangle selrect;

	public Image images[]; // TODO: move to GUIResource once we load from JobEntryLoader

	private static final double theta = Math.toRadians(10); // arrowhead sharpness
	private static final int    size  = 30; // arrowhead length
	
	private int shadowsize;

    /** @deprecated */
    public ChefGraph(Composite par, int style, LogWriter l, Chef je) 
    {
        this(par, style, je);
    }

	public ChefGraph(Composite par, int style, Chef je) 
	{
		super(par, style);
		shell = par.getShell();
		log = LogWriter.getInstance();
		chef = je;
		canvas = this;
        
		newProps();
		
		selrect = null;
		hop_candidate = null;
		last_hop_split = null;

		selected_entries = null;
		selected_note = null;
        
        hori = getHorizontalBar();
		vert = getVerticalBar();

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
		
		loadImages();

		addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent e) {
				ChefGraph.this.paintControl(e);
			}
		});

		selected_entries = null;
		lastclick = null;

		addKeyListener(new KeyAdapter() 
		{
			public void keyPressed(KeyEvent e) 
			{
				shift = (e.keyCode == SWT.SHIFT);
				control = (e.keyCode == SWT.CONTROL);
			}

			public void keyReleased(KeyEvent e) 
			{
				shift = (e.keyCode == SWT.SHIFT);
				control = (e.keyCode == SWT.CONTROL);
			}
		});

		addMouseListener(new MouseAdapter() 
		{
			public void mouseDoubleClick(MouseEvent e) 
			{
			    clearSettings();
                control = (e.stateMask & SWT.CONTROL) != 0;
                shift = (e.stateMask & SWT.SHIFT) != 0;

				Point real = screen2real(e.x, e.y);
				
				JobEntryCopy jobentry = chef.jobMeta.getChefGraphEntry(real.x, real.y, iconsize);
				if (jobentry != null) 
				{
					if (e.button==1) 
					{
						editEntry(jobentry);
					}
					else // launch Chef or Spoon 
					{
						launchStuff(jobentry);
					}
				} 
				else 
				{
					// Check if point lies on one of the many hop-lines...
					JobHopMeta online = findJobHop(real.x, real.y);
					if (online != null) 
					{
						// editJobHop(online);
					}
					else
					{
						NotePadMeta ni = chef.jobMeta.getNote(real.x, real.y);
						if (ni!=null)
						{
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
				
				JobEntryCopy je = chef.jobMeta.getChefGraphEntry(real.x, real.y, iconsize);
				if (je != null) 
				{
					selected_entries = chef.jobMeta.getSelectedEntries();
					selected_icon = je;
					// make sure this is correct!!!
					// When an icon is moved that is not selected, it gets selected too late.
					// It is not captured here, but in the mouseMoveListener...
					prev_locations = chef.jobMeta.getSelectedLocations();

					Point p = je.getLocation();
					iconoffset = new Point(real.x - p.x, real.y - p.y);
				} 
				else 
				{
					// Dit we hit a note?
					NotePadMeta ni = chef.jobMeta.getNote(real.x, real.y);
					if (ni!=null && last_button == 1)
					{
						selected_note = ni;
						Point loc = ni.getLocation();
						previous_note_location = new Point(loc.x, loc.y);
						noteoffset = new Point(real.x - loc.x, real.y - loc.y);
						System.out.println("We hit a note!!");
					}
					else
					{
						selrect = new Rectangle(real.x, real.y, 0, 0);
					}
				}
				lastclick = new Point(real.x, real.y);
				redraw();
			}

			public void mouseUp(MouseEvent e) 
			{
                control = (e.stateMask & SWT.CONTROL) != 0;
                shift = (e.stateMask & SWT.SHIFT) != 0;

				if (iconoffset==null) iconoffset=new Point(0,0);
				Point real = screen2real(e.x, e.y);
				Point icon = new Point(real.x - iconoffset.x, real.y - iconoffset.y);

				// See if we need to add a hop...
				if (hop_candidate != null) 
				{
					// hop doesn't already exist
					if (chef.jobMeta.findJobHop(hop_candidate.from_entry, hop_candidate.to_entry) == null) 
					{
						if (!hop_candidate.from_entry.evaluates() && hop_candidate.from_entry.isUnconditional())
						{
							hop_candidate.setUnconditional();
						}
						else
						{
							hop_candidate.setConditional();
							int nr = chef.jobMeta.findNrNextChefGraphEntries(hop_candidate.from_entry);
	
							// If there is one green link: make this one red! (or vice-versa)
							if (nr == 1) 
							{
								JobEntryCopy jge = chef.jobMeta.findNextChefGraphEntry(hop_candidate.from_entry, 0);
								JobHopMeta other = chef.jobMeta.findJobHop(hop_candidate.from_entry, jge);
								if (other != null) 
								{
									hop_candidate.setEvaluation(!other.getEvaluation());
								}
							}
						}
						
						chef.jobMeta.addJobHop(hop_candidate);
						chef.addUndoNew(new JobHopMeta[] { hop_candidate }, new int[] { chef.jobMeta.indexOfJobHop(hop_candidate) } );
						chef.refreshTree();
					}
					hop_candidate = null;
					selected_entries = null;
					last_button = 0;
					redraw();
				} 
				
				// Did we select a region on the screen?  
				else if (selrect != null) 
				{
					selrect.width  = real.x - selrect.x;
					selrect.height = real.y - selrect.y;

					chef.jobMeta.unselectAll();
					chef.jobMeta.selectInRect(selrect);
					selrect = null;
					redraw();
				}
				
				// Clicked on an icon?
				//
				else if (selected_icon != null)
				{
					if (e.button == 1)
					{
						if (lastclick.x == e.x && lastclick.y == e.y)
						{
							// Flip selection when control is pressed!
							if (control)
							{
								selected_icon.flipSelected();
							}
							else
							{
								// Otherwise, select only the icon clicked on!
								chef.jobMeta.unselectAll();
								selected_icon.setSelected(true);
							}
						}
						else // We moved around some items: store undo info...
						if (selected_entries != null && prev_locations != null)
						{
							int indexes[] = chef.jobMeta.getEntryIndexes(selected_entries); 
							chef.addUndoPosition(selected_entries, indexes, prev_locations, chef.jobMeta.getSelectedLocations());
						}
					}

					// OK, we moved the step, did we move it across a hop?
					// If so, ask to split the hop!
					if (split_hop)
					{
						JobHopMeta hi = findJobHop(icon.x + iconsize / 2, icon.y + iconsize / 2);
						if (hi != null)
						{
							int id = 0;
							if (!chef.props.getAutoSplit())
							{
								MessageDialogWithToggle md = new MessageDialogWithToggle(shell, 
																						 "Split hop?", 
																						 null,
																						 "Do you want to split this hop?"+Const.CR+hi.from_entry.getName()+" --> "+hi.to_entry.getName(),
																						 MessageDialog.QUESTION,
																						 new String[] { "Yes", "No" },
																						 0,
																						 "Don't ask again",
																						 chef.props.getAutoSplit()
																						 );
								id = md.open();
								chef.props.setAutoSplit(md.getToggleState());
							}
							
							if (id == 0)
							{
								JobHopMeta newhop1 = new JobHopMeta(hi.from_entry, selected_icon);
								chef.jobMeta.addJobHop(newhop1);
								JobHopMeta newhop2 = new JobHopMeta(selected_icon, hi.to_entry);
								chef.jobMeta.addJobHop(newhop2);
								if (!selected_icon.evaluates()) newhop2.setUnconditional();

								chef.addUndoNew(new JobHopMeta[] { (JobHopMeta)newhop1.clone(), (JobHopMeta)newhop2.clone() }, new int[] { chef.jobMeta.indexOfJobHop(newhop1), chef.jobMeta.indexOfJobHop(newhop2)});
								int idx = chef.jobMeta.indexOfJobHop(hi);
								chef.addUndoDelete(new JobHopMeta[] { (JobHopMeta)hi.clone() }, new int[] { idx });
								chef.jobMeta.removeJobHop(idx);
								chef.refreshTree();

							}
						}
						split_hop = false;
					}

					selected_entries = null;
					redraw();
				}
 
				// Notes?
				else if (selected_note != null)
				{
					Point note = new Point(real.x - noteoffset.x, real.y - noteoffset.y);
					if (last_button == 1)
					{
						if (lastclick.x != e.x || lastclick.y != e.y)
						{
							int indexes[] = new int[] { chef.jobMeta.indexOfNote(selected_note) };
							chef.addUndoPosition(new NotePadMeta[] { selected_note }, indexes, new Point[] { previous_note_location }, new Point[] { note });
						}
					}
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

				if (iconoffset==null) iconoffset=new Point(0,0);
				Point real = screen2real(e.x, e.y);
				Point icon = new Point(real.x - iconoffset.x, real.y - iconoffset.y);

				setToolTip(real.x, real.y);

				// First see if the icon we clicked on was selected.
				// If the icon was not selected, we should unselect all other icons,
				// selected and move only the one icon
				if (selected_icon != null && !selected_icon.isSelected())
				{
					chef.jobMeta.unselectAll();
					selected_icon.setSelected(true);
					selected_entries = new JobEntryCopy[] { selected_icon };
					prev_locations = new Point[] { selected_icon.getLocation()};
				}

				// Did we select a region...?
				if (selrect != null) 
				{
					selrect.width = real.x - selrect.x;
					selrect.height = real.y - selrect.y;
					redraw();
				} 
				else
				
				// Or just one entry on the screen?
				if (selected_entries != null) 
				{
					if (last_button == 1 && !shift) 
					{
						/*
						 * One or more icons are selected and moved around...
						 * 
						 * new : new position of the ICON (not the mouse pointer)
						 * dx  : difference with previous position
						 */
						int dx = icon.x - selected_icon.getLocation().x;
						int dy = icon.y - selected_icon.getLocation().y;

						JobHopMeta hi =findJobHop(icon.x+iconsize/2, icon.y+iconsize/2);
						if (hi != null) 
						{
							//log.logBasic("MouseMove", "Split hop candidate B!");
							if (!chef.jobMeta.isEntryUsedInHops(selected_icon)) 
							{
								//log.logBasic("MouseMove", "Split hop candidate A!");
								split_hop = true;
								last_hop_split = hi;
								hi.setSplit(true);
							}
						} 
						else 
						{
							if (last_hop_split != null) 
							{
								last_hop_split.setSplit(false);
								last_hop_split = null;
								split_hop = false;
							}
						}

						//
						// One or more job entries are being moved around!
						//
						for (int i = 0; i < chef.jobMeta.nrJobEntries(); i++) 
						{
							JobEntryCopy je = chef.jobMeta.getJobEntry(i);
							if (je.isSelected()) 
							{
								je.setLocation(je.getLocation().x + dx, je.getLocation().y + dy);

							}
						}
						// selected_icon.setLocation(icon.x, icon.y);

						redraw();
					} 
					else
                    //	The middle button perhaps?
					if (last_button == 2 || (last_button == 1 && shift))	
					{
						JobEntryCopy si = chef.jobMeta.getChefGraphEntry(real.x, real.y, iconsize);
						if (si != null && !selected_icon.equals(si)) 
						{
							if (hop_candidate == null) 
							{
								hop_candidate =	new JobHopMeta(selected_icon, si);
								redraw();
							}
						} 
						else 
						{
							if (hop_candidate != null) 
							{
								hop_candidate = null;
								redraw();
							}
						}
					}
				}
				else
				// are we moving a note around? 
				if (selected_note!=null)
				{
					if (last_button==1)
					{
						Point note = new Point(real.x - noteoffset.x, real.y - noteoffset.y);
						selected_note.setLocation(note.x, note.y);
						chef.refreshGraph();
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

				Point p = getRealPosition(canvas, event.x, event.y);
				
				StringTokenizer strtok = new StringTokenizer((String) event.data, Const.CR);
				if (strtok.countTokens() == 1) 
				{
					String entry = strtok.nextToken();
					//System.out.println("new entry: "+entry);
					
					JobEntryCopy jge = chef.jobMeta.findJobEntry(entry, 0);

					if (jge != null)  // Create duplicate of existing entry 
					{
						log.logDebug(toString(), "DROP "+jge.toString()+", type="+ JobEntryCopy.getTypeDesc(jge.getType())+", start="+jge.isStart()+", drawn="+jge.isDrawn());
						
						// There can be only 1 start!
						if (jge.isStart() && jge.isDrawn()) 
						{
							MessageBox mb = new MessageBox(shell, SWT.YES | SWT.ICON_ERROR);
							mb.setMessage("You can only use the start icon once in a job.");
							mb.setText("Sorry!");
							mb.open();

							return;
						}

						boolean jge_changed=false;
						
						// For undo :
						JobEntryCopy before = (JobEntryCopy)jge.clone_deep();
						
						JobEntryCopy newjge = jge;
						if (jge.isDrawn()) 
						{
							newjge = (JobEntryCopy)jge.clone();
							if (newjge!=null)
							{
								// newjge.setEntry(jge.getEntry());
								log.logDebug(toString(), "entry aft = "+((Object)jge.getEntry()).toString());
								
								newjge.setNr(chef.jobMeta.findUnusedNr(newjge.getName()));
								
								chef.jobMeta.addJobEntry(newjge);
								chef.addUndoNew(new JobEntryCopy[] {newjge}, new int[] { chef.jobMeta.indexOfJobEntry(newjge)} );
							}
							else
							{
								log.logDebug(toString(), "jge is not cloned!");
							}
						}
						else
						{
							log.logDebug(toString(), jge.toString()+" is not drawn");
							jge_changed=true;
						}
						newjge.setLocation(p.x, p.y);
						newjge.setDrawn();
						if (jge_changed)
						{
							chef.addUndoChange(new JobEntryCopy[] { before }, new JobEntryCopy[] {newjge}, new int[] { chef.jobMeta.indexOfJobEntry(newjge)});
						}
						redraw();
						chef.refreshTree();
						log.logBasic("DropTargetEvent", "DROP "+newjge.toString()+"!, type="+ JobEntryCopy.getTypeDesc(newjge.getType()));
					} 
					else // Entry doesn't exist: create new one.
					{
						log.logDebug(toString(), "New entry of type ["+entry+"]"); 
						
						jge = chef.newChefGraphEntry(entry);
						if (jge != null) 
						{
							jge.setLocation(p.x, p.y);
							jge.setDrawn();
							redraw();
						} 
					}
				}
			}

			public void dropAccept(DropTargetEvent event) 
			{
				drop_candidate = null;
			}
		});

		// Keyboard shortcuts...
		addKeyListener(new KeyAdapter() 
		{
			public void keyPressed(KeyEvent e) 
			{
				if ((int) e.character == 1) // CTRL-A
				{
					chef.jobMeta.selectAll();
					redraw();
				}
                if ((int) e.character == 3) // CTRL-C
                {
                    chef.copyJobEntries(chef.jobMeta.getSelectedEntries());
                }
                if ((int) e.character == 22) // CTRL-V
                {
                    String clipcontent = chef.fromClipboard();
                    if (clipcontent != null)
                    {
                        if (lastMove != null)
                        {
                            chef.pasteSteps(clipcontent, lastMove);
                        }
                    }

                    //spoon.pasteSteps( );
                }
				if (e.keyCode == SWT.ESC) 
				{
					chef.jobMeta.unselectAll();
					redraw();
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
			}
		});

		addKeyListener(chef.defKeys);

		setBackground(GUIResource.getInstance().getColorBackground());
	}
	
	public void clearSettings()
	{
		selected_icon = null;
		selected_note = null;
		selected_entries = null;
		selrect = null;
		hop_candidate = null;
		last_hop_split = null;
		last_button = 0;
		iconoffset = null;
		for (int i = 0; i < chef.jobMeta.nrJobHops(); i++)
			chef.jobMeta.getJobHop(i).setSplit(false);
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
		Point screen = new Point(x+offset.x, y+offset.y);
				
		return screen;
	}

	public Point getRealPosition(Composite canvas, int x, int y) 
	{
		Point p = new Point(0, 0);
		Composite follow = canvas;
		while (follow != null) 
		{
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
	private JobHopMeta findJobHop(int x, int y) 
	{
		int i;
		JobHopMeta online = null;
		for (i = 0; i < chef.jobMeta.nrJobHops(); i++) 
		{
			JobHopMeta hi = chef.jobMeta.getJobHop(i);

			int line[] = getLine(hi.from_entry, hi.to_entry);

			if (line!=null && pointOnLine(x, y, line)) online = hi;
		}
		return online;
	}

	private int[] getLine(JobEntryCopy fs, JobEntryCopy ts) 
	{
		if (fs==null || ts==null) return null;
		
		Point from = fs.getLocation();
		Point to = ts.getLocation();
		offset = getOffset();

		int x1 = from.x + iconsize / 2;
		int y1 = from.y + iconsize / 2;

		int x2 = to.x + iconsize / 2;
		int y2 = to.y + iconsize / 2;

		return new int[] { x1, y1, x2, y2 };
	}

	private void setMenu(int x, int y) 
	{
		final JobEntryCopy je = chef.jobMeta.getChefGraphEntry(x, y, iconsize);
		if (je != null) // We clicked on a Job Entry!
		{
			Menu mPop = new Menu((Control) this);
			MenuItem miNewHop = null;

			int sels = chef.jobMeta.nrSelected();
			if (sels == 2) 
			{
				miNewHop = new MenuItem(mPop, SWT.CASCADE);
				miNewHop.setText("New hop");
			}

			final JobEntryInterface entry = je.getEntry();

			switch(je.getType())
			{
			case JobEntryInterface.TYPE_JOBENTRY_TRANSFORMATION:
				{
					MenuItem miLaunch = new MenuItem(mPop, SWT.CASCADE);
					miLaunch.setText("Launch Spoon");
							
					miLaunch.addSelectionListener(new SelectionAdapter() 
						{
							public void widgetSelected(SelectionEvent e) 
							{
								launchSpoon((JobEntryTrans)entry);
							}
						}
					);
				}
				break;
			case JobEntryInterface.TYPE_JOBENTRY_JOB:
				{
					MenuItem miLaunch = new MenuItem(mPop, SWT.CASCADE);
					miLaunch.setText("Launch Chef");
				
					miLaunch.addSelectionListener(new SelectionAdapter() 
						{
							public void widgetSelected(SelectionEvent e) 
							{
								launchChef((JobEntryJob)entry);
							}
						}
					);
				}
				break;
			default: break;
			}
			MenuItem miEditStep = new MenuItem(mPop, SWT.CASCADE);
			miEditStep.setText("Edit job entry");
			
			MenuItem miEditDesc = new MenuItem(mPop, SWT.CASCADE);
			miEditDesc.setText("Edit job entry description");
			
			new MenuItem(mPop, SWT.SEPARATOR);
			//----------------------------------------------------------
			
			MenuItem miDupeStep = new MenuItem(mPop, SWT.CASCADE);
			miDupeStep.setText("Duplicate job entry");

			MenuItem miCopy = new MenuItem(mPop, SWT.CASCADE);
			miCopy.setText("Copy selected entries to clipboard\tCTRL-C");
			
			
            // Allign & Distribute options...
            new MenuItem(mPop, SWT.SEPARATOR);
            MenuItem miPopAD = new MenuItem(mPop, SWT.CASCADE);
            miPopAD.setText("Allign / Distribute");

            Menu mPopAD = new Menu(miPopAD);
            MenuItem miPopALeft = new MenuItem(mPopAD, SWT.CASCADE);
            miPopALeft.setText("Allign left\tCTRL-LEFT");
            MenuItem miPopARight = new MenuItem(mPopAD, SWT.CASCADE);
            miPopARight.setText("Allign right\tCTRL-RIGHT");
            MenuItem miPopATop = new MenuItem(mPopAD, SWT.CASCADE);
            miPopATop.setText("Allign top\tCTRL-UP");
            MenuItem miPopABottom = new MenuItem(mPopAD, SWT.CASCADE);
            miPopABottom.setText("Allign bottom\tCTRL-DOWN");
            new MenuItem(mPopAD, SWT.SEPARATOR);
            MenuItem miPopDHoriz = new MenuItem(mPopAD, SWT.CASCADE);
            miPopDHoriz.setText("Distribute horizontally\tALT-RIGHT");
            MenuItem miPopDVertic = new MenuItem(mPopAD, SWT.CASCADE);
            miPopDVertic.setText("Distribute vertically\tALT-UP");
            new MenuItem(mPopAD, SWT.SEPARATOR);
            MenuItem miPopSSnap = new MenuItem(mPopAD, SWT.CASCADE);
            miPopSSnap.setText("Snap to grid (size " + Const.GRID_SIZE + ")\tALT-HOME");
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

			
			if (sels == 2) 
			{
				miNewHop.addSelectionListener(new SelectionAdapter() 
				{
					public void widgetSelected(SelectionEvent e) 
					{
						selected_entries = null;
						newHop();
					}
				});
			}

			miEditStep.addSelectionListener(new SelectionAdapter() 
			{
				public void widgetSelected(SelectionEvent e) 
				{
					selected_entries = null;
					editEntry(je);
				}
			});
			miEditDesc.addSelectionListener(new SelectionAdapter() 
			{
				public void widgetSelected(SelectionEvent e) 
				{
					String title = "Step description dialog";
					String message = "Step description:";
					EnterTextDialog dd = new EnterTextDialog(shell, title, message, je.getDescription());
					String des = dd.open();
					if (des != null) je.setDescription(des);
				}
			});
			miDupeStep.addSelectionListener(new SelectionAdapter() 
			{
				public void widgetSelected(SelectionEvent e) 
				{
					chef.dupeChefGraphEntry(je.getName());
				}
			});
			miCopy.addSelectionListener(new SelectionAdapter() 
			{
				public void widgetSelected(SelectionEvent e) 
				{
				    chef.copyJobEntries(chef.jobMeta.getSelectedEntries());
				}
			});

			if (chef.jobMeta.isEntryUsedInHops(je))
			{
				new MenuItem(mPop, SWT.SEPARATOR);
				MenuItem miDetach = new MenuItem(mPop, SWT.CASCADE);
				miDetach.setText("Detach entry");
				miDetach.addSelectionListener(new SelectionAdapter()
				{
					public void widgetSelected(SelectionEvent e)
					{
						detach(je);
						chef.jobMeta.unselectAll();
					}
				});
			}
			if (je.isDrawn() && !chef.jobMeta.isEntryUsedInHops(je)) 
			{
				new MenuItem(mPop, SWT.SEPARATOR);
				MenuItem miHide = new MenuItem(mPop, SWT.CASCADE);
				miHide.setText("Hide entry");
				miHide.addSelectionListener(new SelectionAdapter() 
				{
					public void widgetSelected(SelectionEvent e) 
					{
						je.setDrawn(false);
						// nr > 1: delete
						if (je.getNr() > 0) 
						{
							int ind = chef.jobMeta.indexOfJobEntry(je);
							chef.jobMeta.removeJobEntry(ind);
							chef.addUndoDelete(new JobEntryCopy[] {je}, new int[] {ind});
						}
						redraw();
					}
				});
			}
			setMenu(mPop);
		} 
		else // Clear the menu
		{
			final JobHopMeta hi = findJobHop(x, y);
			if (hi != null) // We clicked on a HOP!
			{
				Menu mPop = new Menu((Control) this);				
				
				// Evaluation...
				MenuItem miPopEval = new MenuItem(mPop, SWT.CASCADE);
				miPopEval.setText("Evaluation");

				Menu mPopAD = new Menu(miPopEval);
				MenuItem miPopEvalUncond = new MenuItem(mPopAD, SWT.CASCADE | SWT.CHECK);
				miPopEvalUncond.setText("Unconditional");
				miPopEvalUncond.addSelectionListener(new SelectionAdapter() { public void widgetSelected(SelectionEvent arg0) {	hi.setUnconditional(); chef.refreshGraph();}} );
				
				MenuItem miPopEvalTrue = new MenuItem(mPopAD, SWT.CASCADE | SWT.CHECK);
				miPopEvalTrue.setText("Follow when result is true");
				miPopEvalTrue.addSelectionListener(new SelectionAdapter() { public void widgetSelected(SelectionEvent arg0) {	hi.setConditional(); hi.setEvaluation(true); chef.refreshGraph(); }} );
				
				MenuItem miPopEvalFalse = new MenuItem(mPopAD, SWT.CASCADE | SWT.CHECK);
				miPopEvalFalse.setText("Follow when result is false");
				miPopEvalFalse.addSelectionListener(new SelectionAdapter() { public void widgetSelected(SelectionEvent arg0) {	hi.setConditional(); hi.setEvaluation(false); chef.refreshGraph(); }} );

				if (hi.isUnconditional())
				{
					miPopEvalUncond.setSelection(true);
					miPopEvalTrue.setSelection(false);
					miPopEvalFalse.setSelection(false);
				}
				else
				{
					if (hi.getEvaluation())
					{
						miPopEvalUncond.setSelection(false);
						miPopEvalTrue.setSelection(true);
						miPopEvalFalse.setSelection(false);						
					}
					else
					{
						miPopEvalUncond.setSelection(false);
						miPopEvalTrue.setSelection(false);
						miPopEvalFalse.setSelection(true);
					}
				}

				if (!hi.from_entry.evaluates())
				{
					miPopEvalTrue.setEnabled(false);
					miPopEvalFalse.setEnabled(false);
				}
				if (!hi.from_entry.isUnconditional())
				{
					miPopEvalUncond.setEnabled(false);
				}

				miPopEval.setMenu(mPopAD);


				MenuItem miFlipHop = new MenuItem(mPop, SWT.CASCADE);
				miFlipHop.setText("Flip direction");
				MenuItem miDisHop = new MenuItem(mPop, SWT.CASCADE);
				if (hi.isEnabled()) miDisHop.setText("Disable hop");
				else                miDisHop.setText("Enable hop");
				MenuItem miDelHop = new MenuItem(mPop, SWT.CASCADE);
				miDelHop.setText("Delete hop");

				
				miFlipHop.addSelectionListener(new SelectionAdapter() 
					{
						public void widgetSelected(SelectionEvent e) 
						{
							selrect = null;
							JobEntryCopy dummy = hi.from_entry;
							hi.from_entry = hi.to_entry;
							hi.to_entry = dummy;
	
							if (chef.jobMeta.hasLoop(hi.from_entry)) 
							{
								chef.refreshGraph();
								MessageBox mb = new MessageBox(shell, SWT.YES | SWT.ICON_WARNING);
								mb.setMessage("This hop flip causes a loop!  Loops are not allowed.");
								mb.setText("Warning!");
								mb.open();
	
								dummy = hi.from_entry;
								hi.from_entry = hi.to_entry;
								hi.to_entry = dummy;
								chef.refreshGraph();
							} 
							else 
							{
								hi.setChanged();
								chef.refreshGraph();
								chef.refreshTree();
								chef.setShellText();
							}
						}
					}
				);
				miDisHop.addSelectionListener(new SelectionAdapter() 
					{
						public void widgetSelected(SelectionEvent e) 
						{
							selrect = null;
							hi.setEnabled(!hi.isEnabled());
							chef.refreshGraph();
							chef.refreshTree();
						}
					}
				);
				miDelHop.addSelectionListener(new SelectionAdapter() 
					{
						public void widgetSelected(SelectionEvent e) 
						{
							selrect = null;
							int idx = chef.jobMeta.indexOfJobHop(hi);
							chef.jobMeta.removeJobHop(idx);
							chef.refreshTree();
							chef.refreshGraph();
						}
					}
				);
				setMenu(mPop);
			} 
			else 
			{
				// Clicked on the background: maybe we hit a note?
				final NotePadMeta ni = chef.jobMeta.getNote(x, y);
				if (ni!=null)
				{
					// Delete note
					// Edit note
					Menu mPop = new Menu((Control)this);

					MenuItem miNoteEdit = new MenuItem(mPop, SWT.CASCADE); miNoteEdit.setText("Edit note");
					MenuItem miNoteDel  = new MenuItem(mPop, SWT.CASCADE); miNoteDel .setText("Delete note");

					miNoteEdit.addSelectionListener(
						new SelectionAdapter() 
						{ 
							public void widgetSelected(SelectionEvent e) 
							{ 
								selrect=null;
								editNote(ni);
							} 
						} 
					);
					miNoteDel.addSelectionListener(
						new SelectionAdapter() 
						{ 
							public void widgetSelected(SelectionEvent e) 
							{ 
								selrect=null; 
								int idx = chef.jobMeta.indexOfNote(ni);
								if (idx>=0) 
								{
									chef.jobMeta.removeNote(idx);
									chef.addUndoDelete(new NotePadMeta[] {ni}, new int[] {idx} );
								} 
								redraw();
							} 
						} 
					);
					
					setMenu(mPop);
				}
				else
				{
					// New note
					Menu mPop = new Menu((Control)this);

					MenuItem miNoteNew = new MenuItem(mPop, SWT.CASCADE); miNoteNew.setText("New note");
					miNoteNew.addSelectionListener(
						new SelectionAdapter() 
						{ 
							public void widgetSelected(SelectionEvent e) 
							{ 
								selrect=null;
								String title = "Notes";
								String message = "Note text:";
								EnterTextDialog dd = new EnterTextDialog(shell, title, message, "");
								String n = dd.open();
								if (n!=null) 
								{
									NotePadMeta npi = new NotePadMeta(n, lastclick.x, lastclick.y, Const.NOTE_MIN_SIZE, Const.NOTE_MIN_SIZE);
									chef.jobMeta.addNote(npi);
									chef.addUndoNew(new NotePadMeta[] {npi}, new int[] { chef.jobMeta.indexOfNote(npi)} );
									redraw();
								} 
							} 
						} 
					);

					setMenu(mPop);
				}
			}
		}
	}

	private void setToolTip(int x, int y) 
	{
		final JobEntryCopy je = chef.jobMeta.getChefGraphEntry(x, y, iconsize);
		if (je != null && je.isDrawn()) // We hover above a Step!
		{
			// Set the tooltip!
			String desc = je.getDescription();
			if (desc != null) 
			{
				int le = desc.length() >= 200 ? 200 : desc.length();
				String tip = desc.substring(0, le);
				if (!tip.equalsIgnoreCase(getToolTipText())) 
				{
					setToolTipText(tip);
				}
			} 
			else 
			{
				setToolTipText(je.toString());
			}
		} 
		else 
		{
			offset = getOffset();
			JobHopMeta hi = findJobHop(x + offset.x, y + offset.x);
			if (hi != null) 
			{
				setToolTipText(hi.toString());
			} 
			else 
			{
				setToolTipText(null);
			}
		}
	}
	
	public void launchStuff(JobEntryCopy jobentry)
	{
		if (jobentry.getType()==JobEntryInterface.TYPE_JOBENTRY_JOB)
		{
			final JobEntryJob entry = (JobEntryJob)jobentry.getEntry();
			if ( ( entry!=null && entry.getFileName()!=null && chef.rep==null) ||
			     ( entry!=null && entry.getName()!=null && chef.rep!=null)
			   )
			{
				launchChef(entry);
			}
		}
		else
		if (jobentry.getType()==JobEntryInterface.TYPE_JOBENTRY_TRANSFORMATION)
		{
			final JobEntryTrans entry = (JobEntryTrans)jobentry.getEntry();
			if ( ( entry!=null && entry.getFileName()!=null && chef.rep==null) ||
			     ( entry!=null && entry.getName()!=null && chef.rep!=null)
			   )
			{
				launchSpoon(entry);
			}
		}
	}
	
	public void launchSpoon(JobEntryTrans entry)
	{
		// Load from repository?
		if ( (entry.getFileName()==null || entry.getFileName().length()==0) &&
		     (entry.getTransname()!=null && entry.getTransname().length()>0)
		   )
		{
			try
			{
				Spoon sp = new Spoon(log, chef.disp, chef.rep);
				// New transformation?
				//
				long id = sp.rep.getTransformationID(entry.getTransname(), entry.getDirectory().getID());
				if (id<0) // New
				{
					sp.transMeta = new TransMeta(null, entry.getTransname(), entry.arguments);
				}
				else
				{
					sp.transMeta = new TransMeta(sp.rep, entry.getTransname(), entry.getDirectory());
				}
				sp.transMeta.clearChanged();
				sp.open();
			}
			catch(KettleException ke)
			{
				MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
				mb.setMessage("Sorry, an error occurred loading the new transformation from the repository:"+Const.CR+ke.getMessage());
				mb.setText("Error!");
				mb.open();
			}
		}
		else
		{
			try
			{
				// Read from file...
				Spoon sp = new Spoon(log, chef.disp, null);
				sp.transMeta = new TransMeta(entry.getFileName());
				sp.transMeta.clearChanged();
				sp.setFilename(entry.getFileName());
				sp.open();
			}
			catch(KettleXMLException xe)
			{
				MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
				mb.setMessage("Sorry, an error occurred loading the new transformation from the XML file:"+Const.CR+xe.getMessage());
				mb.setText("Error!");
				mb.open();
			}

		}
	}

	public void launchChef(JobEntryJob entry)
	{
		// Load from repository?
		if ( (entry.getFileName()==null || entry.getFileName().length()==0) &&
			 (entry.getName()!=null && entry.getName().length()>0)
		   )
		{
			try
			{
				Chef ch = new Chef(log, chef.disp, chef.rep);
				ch.jobMeta = new JobMeta(log, ch.rep, entry.getJobName(), entry.getDirectory());
	
				ch.jobMeta.clearChanged();
				ch.open();
			}
			catch(KettleException e)
			{
				MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
				mb.setMessage("Sorry, I was unable to load the job from the repository."+Const.CR+e.getMessage());
				mb.setText("Error!");
				mb.open();
			}
		}
		else
		{
			try
			{
				Chef ch = new Chef(log, chef.disp, null);
				ch.jobMeta = new JobMeta(log, entry.getFileName());
				ch.jobMeta.setFilename( entry.getFileName() );
				ch.jobMeta.clearChanged();
				ch.refreshTree();
				ch.refreshGraph();
				ch.open();
			}
			catch(KettleException e)
			{
				MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
				mb.setMessage("Sorry, I was unable to load the job from XML file."+Const.CR+e.getMessage());
				mb.setText("Error!");
				mb.open();
			}
		}
	}

	public void paintControl(PaintEvent e) 
	{
		Point area = getArea();
		if (area.x==0 || area.y==0) return; // nothing to do!

		Display disp = shell.getDisplay();
        if (disp.isDisposed()) return; // Nothing to do!
        
		Image img = new Image(disp, area.x, area.y);
		GC gc = new GC(img);
		drawJob(gc);
		e.gc.drawImage(img, 0, 0);
		gc.dispose();
		img.dispose();

		chef.setShellText();
	}
    
	public void drawJob(GC gc) 
	{
        if (chef.props.isAntiAliasingEnabled() && Const.getOS().startsWith("Windows")) gc.setAntialias(SWT.ON);
        
		shadowsize = chef.props.getShadowSize();

		gc.setBackground(GUIResource.getInstance().getColorBackground());

		Point area = getArea();
		Point max = chef.jobMeta.getMaximum();
		Point thumb = getThumb(area, max);
		offset = getOffset(thumb, area);

		hori.setThumb(thumb.x);
		vert.setThumb(thumb.y);

		// First draw the notes...
        gc.setFont(GUIResource.getInstance().getFontNote());

        for (int i = 0; i < chef.jobMeta.nrNotes(); i++) 
		{
			NotePadMeta ni = chef.jobMeta.getNote(i);
			drawNote(gc, ni);
		}
        
        gc.setFont(GUIResource.getInstance().getFontGraph());
		
		if (shadowsize>0)
		for (int j = 0; j < chef.jobMeta.nrJobEntries(); j++)
		{
			JobEntryCopy cge = chef.jobMeta.getJobEntry(j);
			drawChefGraphEntryShadow(gc, cge);
		}

		// ... and then the rest on top of it...
		for (int i = 0; i < chef.jobMeta.nrJobHops(); i++) 
		{
			JobHopMeta hi = chef.jobMeta.getJobHop(i);
			drawJobHop(gc, hi, false);
		}

		if (hop_candidate != null) 
		{
			drawJobHop(gc, hop_candidate, true);
		}

		for (int j = 0; j < chef.jobMeta.nrJobEntries(); j++) 
		{
			JobEntryCopy je = chef.jobMeta.getJobEntry(j);
			drawChefGraphEntry(gc, je);
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

	private void drawJobHop(GC gc, JobHopMeta hi, boolean candidate) 
	{
		if (hi==null || hi.from_entry==null || hi.to_entry==null) return;
		if (!hi.from_entry.isDrawn() || !hi.to_entry.isDrawn())	return;
		
		if (shadowsize>0) drawLineShadow(gc, hi, false);
		drawLine(gc, hi, candidate);
	}
	
	public Image getIcon(JobEntryCopy je)
	{
		Image im=null;
		if (je==null) return null;
		
		switch (je.getType()) 
		{
		case JobEntryInterface.TYPE_JOBENTRY_SPECIAL        :
			if (je.isStart()) im = GUIResource.getInstance().getImageStart();
			if (je.isDummy()) im = GUIResource.getInstance().getImageDummy();
			break;
		default: im = images[je.getType()];
		}
		return im;
	}

	private void drawChefGraphEntry(GC gc, JobEntryCopy je) 
	{
		if (!je.isDrawn()) return;

		Point pt = je.getLocation();

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
		String name = je.getName();
		if (je.isSelected()) gc.setLineWidth(3);
		else			     gc.setLineWidth(1);
		gc.setBackground(GUIResource.getInstance().getColorRed());
		gc.setForeground(GUIResource.getInstance().getColorBlack());
		gc.fillRectangle(offset.x + x, offset.y + y, iconsize, iconsize);
		Image im = getIcon(je);
		if (im != null) // Draw the icon!
		{
			Rectangle bounds = new Rectangle(im.getBounds().x, im.getBounds().y, im.getBounds().width, im.getBounds().height);
			gc.drawImage(im, 0, 0, bounds.width, bounds.height, offset.x + x, offset.y + y, iconsize, iconsize);
		}
		gc.setBackground(GUIResource.getInstance().getColorWhite());
		gc.drawRectangle(offset.x + x - 1, offset.y + y - 1, iconsize + 1, iconsize + 1);
		//gc.setXORMode(true);
		Point textsize = new Point(gc.textExtent(""+name).x, gc.textExtent(""+name).y);

		gc.setBackground(GUIResource.getInstance().getColorBackground());
		gc.setLineWidth(1);
		
		int xpos = offset.x + x + (iconsize / 2) - (textsize.x / 2);
		int ypos = offset.y + y + iconsize + 5;

		if (shadowsize>0)
		{
			gc.setForeground(GUIResource.getInstance().getColorLightGray());
			gc.drawText(""+name, xpos+shadowsize, ypos+shadowsize, SWT.DRAW_TRANSPARENT);
		}
		
		gc.setForeground(GUIResource.getInstance().getColorBlack());
		gc.drawText(name, xpos, ypos, true);

	}

	private void drawChefGraphEntryShadow(GC gc, JobEntryCopy je) 
	{
		if (je==null) return;
		if (!je.isDrawn()) return;
		
		Point pt = je.getLocation();

		int x, y;
		if (pt != null) { x = pt.x; y = pt.y; }	else { x = 50; y = 50; }

		Point screen = real2screen(x, y);

		// Draw the shadow...
		gc.setBackground(GUIResource.getInstance().getColorLightGray());
		gc.setForeground(GUIResource.getInstance().getColorLightGray());
		int s = shadowsize;
		gc.fillRectangle(screen.x + s, screen.y + s, iconsize, iconsize);
	}

	private void drawNote(GC gc, NotePadMeta ni)
	{
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
		int s = chef.props.getShadowSize();
		int shadow[] = new int[] { note.x+s, note.y+s, // Top left
			note.x + width + 2 * margin+s, note.y+s, // Top right
			note.x + width + 2 * margin+s, note.y + height+s, // bottom right 1
			note.x + width+s, note.y + height + 2 * margin+s, // bottom right 2
			note.x+s, note.y + height + 2 * margin+s // bottom left
		};

		gc.setForeground(GUIResource.getInstance().getColorLightGray());
		gc.setBackground(GUIResource.getInstance().getColorLightGray());
		gc.fillPolygon(shadow);
		
		gc.setForeground(GUIResource.getInstance().getColorGray());
		gc.setBackground(GUIResource.getInstance().getColorYellow());

		gc.fillPolygon(noteshape);
		gc.drawPolygon(noteshape);
		//gc.fillRectangle(ni.xloc, ni.yloc, width+2*margin, heigth+2*margin);
		//gc.drawRectangle(ni.xloc, ni.yloc, width+2*margin, heigth+2*margin);
		gc.setForeground(GUIResource.getInstance().getColorBlack());
		gc.drawText(ni.getNote(), note.x + margin, note.y + margin, flags);

		ni.width = width; // Save for the "mouse" later on...
		ni.height = height;
	}

	private void drawLine(GC gc, JobHopMeta hi, boolean is_candidate) 
	{
		int line[] = getLine(hi.from_entry, hi.to_entry);

		gc.setLineWidth(linewidth);
		Color col;

		if (is_candidate) 
		{
			col = GUIResource.getInstance().getColorBlue();
		}
		else 
		if (hi.isEnabled()) 
		{
			if (hi.isUnconditional())
			{
				col = GUIResource.getInstance().getColorBlack();
			}
			else
			{
				if (hi.getEvaluation()) 
				{
					col = GUIResource.getInstance().getColorGreen(); 
				}
				else 
				{
					col = GUIResource.getInstance().getColorRed();
				}
			}
		} 
		else 
		{
			col = GUIResource.getInstance().getColorGray();
		}

		gc.setForeground(col);

		if (hi.isSplit()) gc.setLineWidth(linewidth + 2);
		drawArrow(gc, line);
		if (hi.isSplit()) gc.setLineWidth(linewidth);

		gc.setForeground(GUIResource.getInstance().getColorBlack());
		gc.setBackground(GUIResource.getInstance().getColorBackground());
	}

	private void drawLineShadow(GC gc, JobHopMeta hi, boolean is_candidate)
	{
		int line[] = getLine(hi.from_entry, hi.to_entry);
		int s = shadowsize;
		for (int i=0;i<line.length;i++) line[i]+=s;

		gc.setLineWidth(linewidth);
		
		gc.setForeground(GUIResource.getInstance().getColorLightGray());

		drawArrow(gc, line);
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
		if (max.x <= area.x) thumb.x = 100;
		else                 thumb.x = 100 * area.x / max.x;
		
		if (max.y <= area.y) thumb.y = 100;
		else                 thumb.y = 100 * area.y / max.y;

		return thumb;
	}

	private Point getOffset() 
	{
		Point area = getArea();
		Point max = chef.jobMeta.getMaximum();
		Point thumb = getThumb(area, max);
		Point offset = getOffset(thumb, area);

		return offset;

	}
	
	private Point getOffset(Point thumb, Point area) 
	{
		Point p = new Point(0, 0);
		Point sel = new Point(hori.getSelection(), vert.getSelection());

		if (thumb.x==0 || thumb.y==0) return p;

		p.x = -sel.x * area.x / thumb.x;
		p.y = -sel.y * area.y / thumb.y;

		return p;
	}

	public int sign(int n) 
	{
		return n < 0 ? -1 : (n > 0 ? 1 : 1);
	}

	private void newHop() 
	{
		JobEntryCopy fr = chef.jobMeta.getSelected(0);
		JobEntryCopy to = chef.jobMeta.getSelected(1);
		chef.newJobHop(fr, to);
	}

	private void editEntry(JobEntryCopy je) 
	{
		chef.editChefGraphEntry(je);
	}
	
	private void editNote(NotePadMeta ni)
	{	
		NotePadMeta before = (NotePadMeta)ni.clone();
		String title = "Notes";
		String message = "Note text:";
		EnterTextDialog dd = new EnterTextDialog(shell, title, message, ni.getNote());
		String n = dd.open();
		if (n!=null) 
		{
			chef.addUndoChange(new NotePadMeta[] {before}, new NotePadMeta[] {ni}, new int[] {chef.jobMeta.indexOfNote(ni)});
			ni.setChanged();
			ni.setNote( n );
			ni.width = Const.NOTE_MIN_SIZE;
			ni.height = Const.NOTE_MIN_SIZE;
			redraw();
		} 
	}

	private void drawArrow(GC gc, int line[]) 
	{
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
		if (dist >= 2 * iconsize) factor = 1.5; else factor = 1.2;
		
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
		gc.fillPolygon(new int[] {mx, my, x3, y3, x4, y4} );
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
		if (!(((x >= x1 && x <= x2) || (x >= x2 && x <= x1))
	       && ((y >= y1 && y <= y2) || (y >= y2 && y <= y1)))
	       )
			return false;

		double angle_line = Math.atan2(y2 - y1, x2 - x1) + Math.PI;
		double angle_point = Math.atan2(y - y1, x - x1) + Math.PI;

		// Same angle, or close enough?
		if (angle_point >= angle_line - 0.01
			&& angle_point <= angle_line + 0.01)
			return true;

		return false;
	}

    private void snaptogrid(int size)
    {
        // First look for the minimum x coordinate...
        
        JobEntryCopy jobentries[] = new JobEntryCopy[chef.jobMeta.nrSelected()];
        Point before[]   = new Point[chef.jobMeta.nrSelected()];
        Point after[]    = new Point[chef.jobMeta.nrSelected()];
        int nr = 0;
        
        for (int i = 0; i < chef.jobMeta.nrJobEntries(); i++)
        {
            JobEntryCopy si = chef.jobMeta.getJobEntry(i);
            if (si.isSelected())
            {
                jobentries[nr] = si;
                Point p = si.getLocation();
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
        
        chef.addUndoPosition(jobentries, chef.jobMeta.getEntryIndexes(jobentries), before, after );
        
        redraw();
    }

    private void allignleft()
    {
        JobEntryCopy jobentries[] = new JobEntryCopy[chef.jobMeta.nrSelected()];
        Point before[]   = new Point[chef.jobMeta.nrSelected()];
        Point after[]    = new Point[chef.jobMeta.nrSelected()];
        int nr = 0;

        int min = 99999;

        // First look for the minimum x coordinate...
        for (int i = 0; i < chef.jobMeta.nrJobEntries(); i++)
        {
            JobEntryCopy si = chef.jobMeta.getJobEntry(i);
            if (si.isSelected())
            {
                Point p = si.getLocation();
                if (p.x < min) min = p.x;
            }
        }
        // Then apply the coordinate...
        for (int i = 0; i < chef.jobMeta.nrJobEntries(); i++)
        {
            JobEntryCopy si = chef.jobMeta.getJobEntry(i);
            if (si.isSelected())
            {
                jobentries[nr] = si;
                Point p = si.getLocation();
                before[nr] = new Point(p.x, p.y);
                si.setLocation(min, p.y);
                after[nr] = new Point(min, p.y);
                nr++;
            }
        }
        chef.addUndoPosition(jobentries, chef.jobMeta.getEntryIndexes(jobentries), before, after );
        redraw();
    }

    private void allignright()
    {
        JobEntryCopy jobentries[] = new JobEntryCopy[chef.jobMeta.nrSelected()];
        Point before[]   = new Point[chef.jobMeta.nrSelected()];
        Point after[]    = new Point[chef.jobMeta.nrSelected()];
        int nr = 0;

        int max = -99999;

        // First look for the maximum x coordinate...
        for (int i = 0; i < chef.jobMeta.nrJobEntries(); i++)
        {
            JobEntryCopy si = chef.jobMeta.getJobEntry(i);
            if (si.isSelected())
            {
                Point p = si.getLocation();
                if (p.x > max) max = p.x;
            }
        }
        // Then apply the coordinate...
        for (int i = 0; i < chef.jobMeta.nrJobEntries(); i++)
        {
            JobEntryCopy si = chef.jobMeta.getJobEntry(i);
            if (si.isSelected())
            {
                jobentries[nr] = si;
                Point p = si.getLocation();
                before[nr] = new Point(p.x, p.y);
                si.setLocation(max, p.y);
                after[nr] = new Point(max, p.y);
                nr++;
           }
        }
        chef.addUndoPosition(jobentries, chef.jobMeta.getEntryIndexes(jobentries), before, after );
        redraw();
    }

    private void alligntop()
    {
        JobEntryCopy jobentries[] = new JobEntryCopy[chef.jobMeta.nrSelected()];
        Point before[]   = new Point[chef.jobMeta.nrSelected()];
        Point after[]    = new Point[chef.jobMeta.nrSelected()];
        int nr = 0;
        
        int min = 99999;

        // First look for the minimum y coordinate...
        for (int i = 0; i < chef.jobMeta.nrJobEntries(); i++)
        {
            JobEntryCopy si = chef.jobMeta.getJobEntry(i);
            if (si.isSelected())
            {
                Point p = si.getLocation();
                if (p.y < min) min = p.y;
            }
        }
        // Then apply the coordinate...
        for (int i = 0; i < chef.jobMeta.nrJobEntries(); i++)
        {
            JobEntryCopy si = chef.jobMeta.getJobEntry(i);
            if (si.isSelected())
            {
                jobentries[nr] = si;
                Point p = si.getLocation();
                before[nr] = new Point(p.x, p.y);
                si.setLocation(p.x, min);
                after[nr] = new Point(p.x, min);
                nr++;
           }
        }
        chef.addUndoPosition(jobentries, chef.jobMeta.getEntryIndexes(jobentries), before, after );
        redraw();
    }

    private void allignbottom()
    {
        JobEntryCopy jobentries[] = new JobEntryCopy[chef.jobMeta.nrSelected()];
        Point before[]   = new Point[chef.jobMeta.nrSelected()];
        Point after[]    = new Point[chef.jobMeta.nrSelected()];
        int nr = 0;
        
        int max = -99999;

        // First look for the maximum y coordinate...
        for (int i = 0; i < chef.jobMeta.nrJobEntries(); i++)
        {
            JobEntryCopy si = chef.jobMeta.getJobEntry(i);
            if (si.isSelected())
            {
                Point p = si.getLocation();
                if (p.y > max) max = p.y;
            }
        }
        // Then apply the coordinate...
        for (int i = 0; i < chef.jobMeta.nrJobEntries(); i++)
        {
            JobEntryCopy si = chef.jobMeta.getJobEntry(i);
            if (si.isSelected())
            {
                jobentries[nr] = si;
                Point p = si.getLocation();
                before[nr] = new Point(p.x, p.y);
                si.setLocation(p.x, max);
                after[nr] = new Point(p.x, max);
                nr++;
            }
        }
        chef.addUndoPosition(jobentries, chef.jobMeta.getEntryIndexes(jobentries), before, after );
        redraw();
    }

    private void distributehorizontal()
    {
        JobEntryCopy jobentries[] = new JobEntryCopy[chef.jobMeta.nrSelected()];
        Point before[]   = new Point[chef.jobMeta.nrSelected()];
        Point after[]    = new Point[chef.jobMeta.nrSelected()];
        
        int min = 99999;
        int max = -99999;
        int sels = chef.jobMeta.nrSelected();
        if (sels <= 1) return;
        int order[] = new int[sels];

        // First look for the minimum & maximum x coordinate...
        int selnr = 0;
        for (int i = 0; i < chef.jobMeta.nrJobEntries(); i++)
        {
            JobEntryCopy si = chef.jobMeta.getJobEntry(i);
            if (si.isSelected())
            {
                Point p = si.getLocation();
                if (p.x < min) min = p.x;
                if (p.x > max) max = p.x;
                order[selnr] = i;
                selnr++;
            }
        }

        // Difficult to keep the jobentries in the correct order.
        // If you just set the x-coordinates, you get special effects.
        // Best is to keep the current order of things.
        // First build an arraylist and store the order there.
        // Then sort order[], based upon the coordinate of the step.
        for (int i = 0; i < sels; i++)
        {
            for (int j = 0; j < sels - 1; j++)
            {
                Point p1 = chef.jobMeta.getJobEntry(order[j]).getLocation();
                Point p2 = chef.jobMeta.getJobEntry(order[j + 1]).getLocation();
                if (p1.x > p2.x) // swap
                {
                    int dummy = order[j];
                    order[j] = order[j + 1];
                    order[j + 1] = dummy;
                }
            }
        }

        // The distance between two jobentries becomes.
        int distance = (max - min) / (sels - 1);

        for (int i = 0; i < sels; i++)
        {
            jobentries[i] = chef.jobMeta.getJobEntry(order[i]);
            Point p = jobentries[i].getLocation();
            before[i] = new Point(p.x, p.y);
            p.x = min + (i * distance);
            after[i] = new Point(p.x, p.y);
        }
        
        // Undo!
        chef.addUndoPosition(jobentries, chef.jobMeta.getEntryIndexes(jobentries), before, after );

        redraw();
    }

    public void distributevertical()
    {
        JobEntryCopy jobentries[] = new JobEntryCopy[chef.jobMeta.nrSelected()];
        Point before[]   = new Point[chef.jobMeta.nrSelected()];
        Point after[]    = new Point[chef.jobMeta.nrSelected()];

        int min = 99999;
        int max = -99999;
        int sels = chef.jobMeta.nrSelected();
        if (sels <= 1) return;
        int order[] = new int[sels];

        // First look for the minimum & maximum y coordinate...
        int selnr = 0;
        for (int i = 0; i < chef.jobMeta.nrJobEntries(); i++)
        {
            JobEntryCopy si = chef.jobMeta.getJobEntry(i);
            if (si.isSelected())
            {
                Point p = si.getLocation();
                if (p.y < min) min = p.y;
                if (p.y > max) max = p.y;
                order[selnr] = i;
                selnr++;
            }
        }

        // Difficult to keep the jobentries in the correct order.
        // If you just set the x-coordinates, you get special effects.
        // Best is to keep the current order of things.
        // First build an arraylist and store the order there.
        // Then sort order[], based upon the coordinate of the step.
        for (int i = 0; i < sels; i++)
        {
            for (int j = 0; j < sels - 1; j++)
            {
                Point p1 = chef.jobMeta.getJobEntry(order[j]).getLocation();
                Point p2 = chef.jobMeta.getJobEntry(order[j + 1]).getLocation();
                if (p1.y > p2.y) // swap
                {
                    int dummy = order[j];
                    order[j] = order[j + 1];
                    order[j + 1] = dummy;
                }
            }
        }

        // The distance between two jobentries becomes.
        int distance = (max - min) / (sels - 1);

        for (int i = 0; i < sels; i++)
        {
            jobentries[i] = chef.jobMeta.getJobEntry(order[i]);
            Point p = jobentries[i].getLocation();
            before[i] = new Point(p.x, p.y);
            p.y = min + (i * distance);
            after[i] = new Point(p.x, p.y);
        }

        // Undo!
        chef.addUndoPosition(jobentries, chef.jobMeta.getEntryIndexes(jobentries), before, after );
        
        redraw();
    }

	private void drawRect(GC gc, Rectangle rect) 
	{
		if (rect == null) return;
		
		gc.setLineStyle(SWT.LINE_DASHDOT);
		gc.setLineWidth(1);
		gc.setForeground(GUIResource.getInstance().getColorDarkGray());
		gc.drawRectangle(rect.x + offset.x, rect.y + offset.y,rect.width, rect.height);
		gc.setLineStyle(SWT.LINE_SOLID);
	}

	private void detach(JobEntryCopy je)
	{
		JobHopMeta hfrom = chef.jobMeta.findJobHopTo(je);
		JobHopMeta hto   = chef.jobMeta.findJobHopFrom(je);

		if (hfrom != null && hto != null)
		{
			if (chef.jobMeta.findJobHop(hfrom.from_entry, hto.to_entry) == null)
			{
				JobHopMeta hnew = new JobHopMeta(hfrom.from_entry, hto.to_entry);
				chef.jobMeta.addJobHop(hnew);
				chef.addUndoNew(new JobHopMeta[] { (JobHopMeta)hnew.clone() }, new int[] { chef.jobMeta.indexOfJobHop(hnew)});
			}
		}
		if (hfrom != null)
		{
			int fromidx = chef.jobMeta.indexOfJobHop(hfrom);
			if (fromidx >= 0)
			{
				chef.jobMeta.removeJobHop(fromidx);
				chef.addUndoDelete(new JobHopMeta[] {hfrom}, new int[] {fromidx} );
			}
		}
		if (hto != null)
		{
			int toidx = chef.jobMeta.indexOfJobHop(hto);
			if (toidx >= 0)
			{
				chef.jobMeta.removeJobHop(toidx);
				chef.addUndoDelete(new JobHopMeta[] {hto}, new int[] {toidx} );
			}
		}
		chef.refreshTree();
		redraw();
	}

	private void loadImages() 
	{
		String png[] = JobEntryInterface.icon_filename;
		images = new Image[png.length];
		
		for (int i=1;i<png.length;i++)
		{
            if (png[i]!=null && png[i].length()>0)
            {
                // System.out.println("Loading image: "+png[i]);
                try
                {
                    final Image image = new Image(getDisplay(), getClass().getResourceAsStream(Const.IMAGE_DIRECTORY + png[i])); 
                    images[i] = image;
                    shell.addDisposeListener(new DisposeListener() { public void widgetDisposed(DisposeEvent e) { image.dispose(); } });
                    
                }
                catch(Exception e)
                {
                    log.logError(toString(), "Unable to find required image file ["+(Const.IMAGE_DIRECTORY + png[i])+" : "+e.toString());
                    images[i] = new Image(shell.getDisplay(), Const.ICON_SIZE, Const.ICON_SIZE);
                    GC gc = new GC(images[i]);
                    gc.drawRectangle(0,0,Const.ICON_SIZE, Const.ICON_SIZE);
                    gc.drawLine(0,0,Const.ICON_SIZE, Const.ICON_SIZE);
                    gc.drawLine(Const.ICON_SIZE, 0, 0, Const.ICON_SIZE);
                    gc.dispose();
                }
            }
		}
	}

	// Anti-aliased lines implementation
	public static int    trunc(double d)   { return (int)Math.floor(d); }
	public static double frac(double d)    { return d - Math.floor(d); }
	public static double invfrac(double d) { return 1 - (d - Math.floor(d)); }
	
	public void newProps()
	{
		iconsize = chef.props.getIconSize();
		linewidth = chef.props.getLineWidth();
	}
	
	public String toString()
	{
		return Chef.APP_NAME;
	}

}
