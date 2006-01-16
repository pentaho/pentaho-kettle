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
 
/*
 * Created on 17-mei-2003
 *  
 */

package be.ibridge.kettle.menu;
import java.util.ArrayList;
import java.util.StringTokenizer;

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
import be.ibridge.kettle.core.GUIResource;
import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.NotePadMeta;
import be.ibridge.kettle.core.Point;
import be.ibridge.kettle.core.Rectangle;
import be.ibridge.kettle.core.dialog.EnterTextDialog;
import be.ibridge.kettle.schema.RelationshipMeta;
import be.ibridge.kettle.schema.SchemaMeta;
import be.ibridge.kettle.schema.TableMeta;


public class PMenuGraph extends Canvas
{
	private static final int HOP_SEL_MARGIN = 9;

	private SchemaMeta schema;
	private Shell shell;
	private PMenuGraph canvas;
	private LogWriter log;

	private int iconsize;
	private int linewidth;
	private Point lastclick;

	private TableMeta selected_steps[];
	private TableMeta selected_icon;
	private Point prev_locations[];
	private NotePadMeta selected_note;
	private Point previous_note_location;
	private RelationshipMeta candidate;
	private Point drop_candidate;
	private PMenu spoon;

	private Point offset, iconoffset, noteoffset;
	private ScrollBar hori;
	private ScrollBar vert;

	public boolean shift, control;
	private int last_button;
	private Rectangle selrect;

	public PMenuGraph(Composite par, int style, SchemaMeta sch, PMenu sp)
	{
		super(par, style);
		shell = par.getShell();
		schema = sch;
		log = LogWriter.getInstance();
		spoon = sp;
		canvas = this;

		iconsize = schema.props.getIconSize();

		selrect = null;
		candidate = null;
		last_button = 0;
		selected_steps = null;
		selected_note = null;

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
				PMenuGraph.this.paintControl(e);
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
				selected_steps = null;
				selected_icon = null;
				selected_note = null;
				candidate = null;
				iconoffset = null;
				selrect = null;

				Point real = screen2real(e.x, e.y);

				TableMeta tableinfo = schema.getTable(real.x, real.y, iconsize);
				if (tableinfo != null)
				{
					if (e.button==1) editTable(tableinfo); else editDescription(tableinfo);
				}
				else
				{
					// Check if point lies on one of the many hop-lines...
					RelationshipMeta online = findRelationship(real.x, real.y);
					if (online != null)
					{
						editRelationship(online);
					}
					else
					{
						NotePadMeta ni = schema.getNote(real.x, real.y);
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
				last_button = e.button;

				selected_steps = null;
				selected_icon = null;
				selected_note = null;
				candidate = null;
				iconoffset = null;
				selrect = null;
				
				Point real = screen2real(e.x, e.y);

				// Clear the tooltip!
				setToolTipText(null);

				setMenu(real.x, real.y);

				// Did we click on a step?
				TableMeta ti = schema.getTable(real.x, real.y, iconsize);
				if (ti != null)
				{
					selected_steps = schema.getSelectedTables();
					selected_icon = ti;
					// make sure this is correct!!!
					// When an icon is moved that is not selected, it gets selected too late.
					// It is not captured here, but in the mouseMoveListener...
					prev_locations = schema.getSelectedLocations();

					Point p = ti.getLocation();
					iconoffset = new Point(real.x - p.x, real.y - p.y);
				}
				else
				{
					// Dit we hit a note?
					NotePadMeta ni = schema.getNote(real.x, real.y);
					if (ni != null && last_button == 1)
					{
						selected_note = ni;
						Point loc = ni.getLocation();
						previous_note_location = new Point(loc.x, loc.y);
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
				if (iconoffset == null) iconoffset = new Point(0, 0);
				Point real = screen2real(e.x, e.y);

				// Quick new hop option? (drag from one step to another)
				//
				if (candidate != null)
				{
					if (schema.findRelationship(candidate.getTableFrom().getName(), candidate.getTableTo().getName()) == null)
					{
						schema.addRelationship(candidate);
						spoon.refreshTree();
					}
					candidate = null;
					selected_steps = null;
					last_button = 0;
					redraw();
				}
				// Did we select a region on the screen? Mark steps in region as selected
				//
				else if (selrect != null)
				{
					selrect.width = real.x - selrect.x;
					selrect.height = real.y - selrect.y;

					schema.unselectAll();
					schema.selectInRect(selrect);
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
								schema.unselectAll();
								selected_icon.setSelected(true);
							}
						}
						else // We moved around some items: store undo info...
						if (selected_steps != null && prev_locations != null)
						{
							int indexes[] = schema.getTableIndexes(selected_steps); 
							spoon.addUndoPosition(selected_steps, indexes, prev_locations, schema.getSelectedLocations());
						}
					}

					selected_steps = null;
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
							int indexes[] = new int[] { schema.indexOfNote(selected_note) };
							spoon.addUndoPosition(new NotePadMeta[] { selected_note }, indexes, new Point[] { previous_note_location }, new Point[] { note });
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
				if (iconoffset == null) iconoffset = new Point(0, 0);
				Point real = screen2real(e.x, e.y);
				Point icon = new Point(real.x - iconoffset.x, real.y - iconoffset.y);

				setToolTip(real.x, real.y);

				// First see if the icon we clicked on was selected.
				// If the icon was not selected, we should unselect all other icons,
				// selected and move only the one icon
				if (selected_icon != null && !selected_icon.isSelected())
				{
					schema.unselectAll();
					selected_icon.setSelected(true);
					selected_steps = new TableMeta[] { selected_icon };
					prev_locations = new Point[] { selected_icon.getLocation()};
				}
				
				// Did we select a region...?
				if (selrect != null)
				{
					selrect.width = real.x - selrect.x;
					selrect.height = real.y - selrect.y;
					redraw();
				}
				
				// Or just one entry on the screen?
				else if (selected_steps != null)
				{
					if (last_button == 1)
					{
						/*
						 * One or more icons are selected and moved around...
						 * 
						 * new : new position of the ICON (not the mouse pointer) dx : difference with previous position
						 */
						int dx = icon.x - selected_icon.getLocation().x;
						int dy = icon.y - selected_icon.getLocation().y;

						// Adjust position (location) of selected steps...
						for (int i = 0; i < selected_steps.length; i++)
						{
							TableMeta te = selected_steps[i];
							te.setLocation(te.getLocation().x + dx, te.getLocation().y + dy);
						}

						redraw();
					}
					// The middle button perhaps?
					else if (last_button == 2)
					{
						TableMeta ti = schema.getTable(real.x, real.y, iconsize);
						if (ti != null && !selected_icon.equals(ti))
						{
							if (candidate == null)
							{
								candidate = new RelationshipMeta(selected_icon, ti, -1, -1);
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
				else if (selected_note != null)
				{
					// Move around a note...
					if (last_button == 1)
					{
						Point note = new Point(real.x - noteoffset.x, real.y - noteoffset.y);
						selected_note.setLocation(note.x, note.y);
						redraw();
					}
				}
			}
		});

		// Drag & Drop for steps
		Transfer[] ttypes = new Transfer[] { TextTransfer.getInstance()};
		DropTarget ddTarget = new DropTarget(this, DND.DROP_MOVE | DND.DROP_COPY);
		ddTarget.setTransfer(ttypes);
		ddTarget.addDropListener(new DropTargetListener()
		{
			public void dragEnter(DropTargetEvent event)
			{
				selected_steps = null;
				selected_icon = null;
				selrect = null;
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

				// What's the real drop position?
				Point p = getRealPosition(canvas, event.x, event.y);

				// We accept strings, separated by Cariage Returns
				// 
				StringTokenizer strtok = new StringTokenizer((String) event.data, Const.CR);
				if (strtok.countTokens() == 1)
				{
					String table = strtok.nextToken();
					TableMeta ti = schema.findTable(table);
					boolean newtable=false;

					if (ti!=null && ( ti.isDrawn() || schema.isTableUsedInRelationships(table)))
					{
						MessageBox mb = new MessageBox(shell, SWT.OK);
						mb.setMessage("Table is allready on canvas!");
						mb.setText("Warning!");
						mb.open();
						return;
					}
                    else
                    {
                        if (ti==null) return;
                    }

					schema.unselectAll();
					
					TableMeta before = (TableMeta)ti.clone();
					
					ti.draw();
					ti.setSelected(true);
					ti.setLocation(p.x, p.y);
					
					if (newtable)
					{	
						spoon.addUndoNew(new TableMeta[] { (TableMeta)ti.clone() }, new int[] { schema.indexOfTable(ti) });
					}
					else
					{
						spoon.addUndoChange(new TableMeta[] { before }, new TableMeta[] { (TableMeta)ti.clone() }, new int[] { schema.indexOfTable(ti) });
					}

					canvas.forceFocus();
					redraw();
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
					schema.selectAll();
					redraw();
				}
				if (e.keyCode == SWT.ESC)
				{
					schema.unselectAll();
					redraw();
				}
			}
		});

		addKeyListener(spoon.defKeys);

		setBackground(GUIResource.getInstance().getColorBackground());
	}

	public Point screen2real(int x, int y)
	{
		getOffset();
		Point real = new Point(x - offset.x, y - offset.y);

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

		p.x = x - p.x - 8;
		p.y = y - p.y - 48;

		return screen2real(p.x, p.y);
	}

	// See if location (x,y) is on a line between two entities: the relationship!
	// return the Relationship if so, otherwise: null
	private RelationshipMeta findRelationship(int x, int y)
	{
		int i;
		RelationshipMeta online = null;
		for (i = 0; i < schema.nrRelationships(); i++)
		{
			RelationshipMeta ri = schema.getRelationship(i);
			TableMeta fs  = ri.getTableFrom();
			TableMeta ts  = ri.getTableTo();

			if (fs == null || ts == null)
				return null;
			
			int line[] = new int[4];
			
			getLine(fs, ts, line);

			if (pointOnLine(x, y, line)) online = ri;
		}
		return online;
	}

	private double getLine(TableMeta fs, TableMeta ts, int line[])
	{
		Point from = fs.getLocation();
		Point to = ts.getLocation();
		offset = getOffset();

		Point A = new Point(from.x, from.y);
		Point B = new Point(to.x, to.y);

		Point X = new Point(0,0);
		Point Y = new Point(0,0);
		
		double angle = calcRelationshipLine(A,B, X, Y);
		
		line[0] = X.x;
		line[1] = X.y;
		line[2] = Y.x;
		line[3] = Y.y;

		return angle; 
	}


	private void setMenu(int x, int y)
	{
		final TableMeta ti = schema.getTable(x, y, iconsize);
		if (ti != null) // We clicked on a Step!
		{
			Menu mPop = new Menu((Control) this);
			MenuItem miNewHop = null;
			MenuItem miHideStep = null;

			int sels = schema.nrSelected();
			if (sels == 2)
			{
				miNewHop = new MenuItem(mPop, SWT.CASCADE);
				miNewHop.setText("Add relationship");
			}
			MenuItem miEditStep = new MenuItem(mPop, SWT.CASCADE);
			miEditStep.setText("Edit table");
			MenuItem miEditDesc = new MenuItem(mPop, SWT.CASCADE);
			miEditDesc.setText("Edit table description");

			MenuItem miDupeStep = new MenuItem(mPop, SWT.CASCADE);
			miDupeStep.setText("Duplicate table");
			MenuItem miDelStep = new MenuItem(mPop, SWT.CASCADE);
			miDelStep.setText("Delete table");

			// Allign & Distribute options...
			if (sels > 1)
			{
				new MenuItem(mPop, SWT.SEPARATOR);
				MenuItem miPopAD = new MenuItem(mPop, SWT.CASCADE);
				miPopAD.setText("Allign / Distribute");

				Menu mPopAD = new Menu(miPopAD);
				MenuItem miPopALeft = new MenuItem(mPopAD, SWT.CASCADE);
				miPopALeft.setText("Allign left");
				MenuItem miPopARight = new MenuItem(mPopAD, SWT.CASCADE);
				miPopARight.setText("Allign right");
				MenuItem miPopATop = new MenuItem(mPopAD, SWT.CASCADE);
				miPopATop.setText("Allign top");
				MenuItem miPopABottom = new MenuItem(mPopAD, SWT.CASCADE);
				miPopABottom.setText("Allign bottom");
				new MenuItem(mPopAD, SWT.SEPARATOR);
				MenuItem miPopDHoriz = new MenuItem(mPopAD, SWT.CASCADE);
				miPopDHoriz.setText("Distribute horizontally");
				MenuItem miPopDVertic = new MenuItem(mPopAD, SWT.CASCADE);
				miPopDVertic.setText("Distribute vertically");
				new MenuItem(mPopAD, SWT.SEPARATOR);
				MenuItem miPopSSnap = new MenuItem(mPopAD, SWT.CASCADE);
				miPopSSnap.setText("Snap to grid (size " + Const.GRID_SIZE + ")");
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
			}

			if (sels == 2)
			{
				miNewHop.addSelectionListener(new SelectionAdapter()
				{
					public void widgetSelected(SelectionEvent e)
					{
						selected_steps = null;
						newRelationship();
					}
				});
			}
			if (ti.isDrawn() && !schema.isTableUsedInRelationships(ti.getName()))
			{
				miHideStep = new MenuItem(mPop, SWT.CASCADE);
				miHideStep.setText("Hide step");
				miHideStep.addSelectionListener(new SelectionAdapter()
				{
					public void widgetSelected(SelectionEvent e)
					{
						for (int i = 0; i < schema.nrSelected(); i++)
						{
							TableMeta ti = schema.getSelected(i);
							if (ti.isDrawn() && ti.isSelected())
							{
								ti.hide();
								spoon.refreshTree();
							}
						}
						ti.hide();
						spoon.refreshTree();
						redraw();
					}
				});
			}

			miEditStep.addSelectionListener(new SelectionAdapter()
			{
				public void widgetSelected(SelectionEvent e)
				{
					selected_steps = null;
					editTable(ti);
				}
			});
			miEditDesc.addSelectionListener(new SelectionAdapter()
			{
				public void widgetSelected(SelectionEvent e)
				{
					editDescription(ti);
				}
			});
			miDupeStep.addSelectionListener(new SelectionAdapter()
			{
				public void widgetSelected(SelectionEvent e)
				{
					if (schema.nrSelected() <= 1)
					{
						spoon.dupeTable(ti.getName());
					}
					else
					{
						for (int i = 0; i < schema.nrTables(); i++)
						{
							TableMeta tableinfo = schema.getTable(i);
							if (tableinfo.isSelected())
							{
								spoon.dupeTable(tableinfo.getName());
							}
						}
					}
				}
			});

			miDelStep.addSelectionListener(new SelectionAdapter()
			{
				public void widgetSelected(SelectionEvent e)
				{
					int nrsels = schema.nrSelected();
					if (nrsels == 0)
					{
						spoon.delTable(ti.getName());
					}
					else
					{
						if (!ti.isSelected()) nrsels++;

						MessageBox mb = new MessageBox(shell, SWT.YES | SWT.NO | SWT.ICON_WARNING);
						mb.setText("WARNING!");
						String message = "Do you want to delete the " + nrsels + " following tables?"+Const.CR;
						for (int i = schema.nrTables() - 1; i >= 0; i--)
						{
							TableMeta tableinfo = schema.getTable(i);
							if (tableinfo.isSelected() || ti.equals(tableinfo))
							{
								message += "  --> " + tableinfo.getName() + Const.CR;
							}
						}

						mb.setMessage(message);
						int result = mb.open();
						if (result == SWT.YES)
						{
							for (int i = schema.nrTables() - 1; i >= 0; i--)
							{
								TableMeta tableinfo = schema.getTable(i);
								if (tableinfo.isSelected() || ti.equals(tableinfo))
								{
									spoon.delTable(tableinfo.getName());
								}
							}
						}
					}
				}
			});

			setMenu(mPop);
		}
		else
		{
			final RelationshipMeta hi = findRelationship(x, y);
			if (hi != null) // We clicked on a HOP!
			{
				Menu mPop = new Menu((Control) this);
				MenuItem miEditHop = new MenuItem(mPop, SWT.CASCADE);
				miEditHop.setText("Edit hop");
				MenuItem miFlipHop = new MenuItem(mPop, SWT.CASCADE);
				miFlipHop.setText("Flip direction");
				MenuItem miDelHop = new MenuItem(mPop, SWT.CASCADE);
				miDelHop.setText("Delete relationship");

				miEditHop.addSelectionListener(new SelectionAdapter()
				{
					public void widgetSelected(SelectionEvent e)
					{
						selrect = null;
						editRelationship(hi);
					}
				});
				miFlipHop.addSelectionListener(new SelectionAdapter()
				{
					public void widgetSelected(SelectionEvent e)
					{
						selrect = null;
						TableMeta dummy = hi.getTableFrom();
						hi.setTableFrom(  hi.getTableTo() );
						hi.setTableTo  (  dummy );

						hi.setChanged();
						spoon.refreshGraph();
						spoon.refreshTree();
						spoon.setShellText();
					}
				});
				miDelHop.addSelectionListener(new SelectionAdapter()
				{
					public void widgetSelected(SelectionEvent e)
					{
						selrect = null;
						int idx = schema.indexOfRelationship(hi);
						spoon.addUndoDelete(new RelationshipMeta[] { (RelationshipMeta)hi.clone() }, new int[] { idx });
						schema.removeRelationship(idx);
						spoon.refreshTree();
						spoon.refreshGraph();
					}
				});
				setMenu(mPop);
			}
			else
			{
				// Clicked on the background: maybe we hit a note?
				final NotePadMeta ni = schema.getNote(x, y);
				if (ni != null)
				{
					// Delete note
					// Edit note
					Menu mPop = new Menu((Control) this);

					MenuItem miNoteEdit = new MenuItem(mPop, SWT.CASCADE);
					miNoteEdit.setText("Edit note");
					MenuItem miNoteDel = new MenuItem(mPop, SWT.CASCADE);
					miNoteDel.setText("Delete note");

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
							int idx = schema.indexOfNote(ni);
							if (idx >= 0)
							{
								schema.removeNote(idx);
								spoon.addUndoDelete(new NotePadMeta[] { (NotePadMeta)ni.clone() }, new int[] { idx });
								redraw();
							}
						}
					});

					setMenu(mPop);
				}
				else
				{
					// New note
					Menu mPop = new Menu((Control) this);

					MenuItem miNoteNew = new MenuItem(mPop, SWT.CASCADE);
					miNoteNew.setText("New note");
					miNoteNew.addSelectionListener(new SelectionAdapter()
					{
						public void widgetSelected(SelectionEvent e)
						{
							selrect = null;
							String title = "Notes";
							String message = "Note text:";
							EnterTextDialog dd = new EnterTextDialog(shell, title, message, "");
							String n = dd.open();
							if (n != null)
							{
								NotePadMeta npi = new NotePadMeta(n, lastclick.x, lastclick.y, Const.NOTE_MIN_SIZE, Const.NOTE_MIN_SIZE);
								schema.addNote(npi);
								spoon.addUndoNew(new NotePadMeta[] { (NotePadMeta)npi.clone() }, new int[] { schema.indexOfNote(npi)});
								redraw();
							}
						}
					});

					setMenu(mPop);
				}
			}
		}
	}

	private void setToolTip(int x, int y)
	{
		final TableMeta ti = schema.getTable(x, y, iconsize);
		if (ti != null) // We clicked on a Step!
		{
			// Also: set the tooltip!
			if (ti.description != null)
			{
				String desc = ti.description;
				int le = desc.length() >= 200 ? 200 : desc.length();
				String tip = desc.substring(0, le);
				if (!tip.equalsIgnoreCase(getToolTipText()))
				{
					setToolTipText(tip);
				}
			}
			else
			{
				setToolTipText(null);
			}
		}
		else
		{
			final RelationshipMeta hi = findRelationship(x, y);
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

	public void editDescription(TableMeta si)
	{	
		String title = "Table description dialog";
		String message = "Table description:";
		EnterTextDialog dd = new EnterTextDialog(shell, title, message, si.description);
		String d = dd.open();
		if (d != null) si.description = d;
	}

	public void paintControl(PaintEvent e)
	{
		Point area = getArea();
		if (area.x==0 || area.y==0) return; // nothing to do!

		Display disp = shell.getDisplay();
		Image img = new Image(disp, area.x, area.y);
		GC gc = new GC(img);
        
		// First clear the image in the background color
		gc.setBackground(GUIResource.getInstance().getColorBackground());
		gc.fillRectangle(0, 0, area.x, area.y);

		// Then draw stuff on it!
		drawTrans(gc);
		e.gc.drawImage(img, 0, 0);
		gc.dispose();
		img.dispose();

		spoon.setShellText();
	}

	public void drawTrans(GC gc)
	{
		int i, j, h;

		gc.setFont(GUIResource.getInstance().getFontGraph());
		gc.setBackground(GUIResource.getInstance().getColorBackground());

		h = schema.nrRelationships();

		Point area = getArea();
		Point max  = schema.getMaximum();
		Point thumb = getThumb(area, max);
		offset = getOffset(thumb, area);

		hori.setThumb(thumb.x);
		vert.setThumb(thumb.y);

		// First the notes
		for (i = 0; i < schema.nrNotes(); i++)
		{
			NotePadMeta ni = schema.getNote(i);
			drawNote(gc, ni);
		}
		for (j = 0; j < h; j++)
		{
			RelationshipMeta hi = schema.getRelationship(j);
			drawRelationship(gc, hi);
		}

		if (candidate != null)
		{
			drawRelationship(gc, candidate, true);
		}

		ArrayList st = schema.getRelationshipTables();

		for (j = 0; j < st.size(); j++)
		{
			TableMeta si = (TableMeta) st.get(j);
			drawTable(gc, si);
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

	private void drawRelationship(GC gc, RelationshipMeta hi)
	{
		drawRelationship(gc, hi, false);
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

		gc.setForeground(GUIResource.getInstance().getColorGray());
		gc.setBackground(GUIResource.getInstance().getColorYellow());

		int noteshape[] = new int[] { note.x, note.y, // Top left
			note.x + width + 2 * margin, note.y, // Top right
			note.x + width + 2 * margin, note.y + height, // bottom right 1
			note.x + width, note.y + height + 2 * margin, // bottom right 2
			note.x + width, note.y + height, // bottom right 3
			note.x + width + 2 * margin, note.y + height, // bottom right 1
			note.x + width, note.y + height + 2 * margin, // bottom right 2
			note.x, note.y + height + 2 * margin // bottom left
		};
		gc.fillPolygon(noteshape);
		gc.drawPolygon(noteshape);
		//gc.fillRectangle(ni.xloc, ni.yloc, width+2*margin, heigth+2*margin);
		//gc.drawRectangle(ni.xloc, ni.yloc, width+2*margin, heigth+2*margin);
		gc.setForeground(GUIResource.getInstance().getColorBlack());
		gc.drawText(ni.getNote(), note.x + margin, note.y + margin, flags);

		ni.width = width; // Save for the "mouse" later on...
		ni.height = height;
	}

	private void drawRelationship(GC gc, RelationshipMeta hi, boolean is_candidate)
	{
		TableMeta fs = hi.getTableFrom();
		TableMeta ts = hi.getTableTo();

		if (fs != null && ts != null)
		{
			drawLine(gc, fs, ts, hi, is_candidate);
		}
	}

	private void drawTable(GC gc, TableMeta si)
	{
		Point pt = si.getLocation();

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

		String name = si.getName();
		if (si.isSelected())
			gc.setLineWidth(linewidth + 2);
		else
			gc.setLineWidth(linewidth);
		if (si.isFact())
		{
			gc.setBackground(GUIResource.getInstance().getColorOrange());
		}
		else
		{
			gc.setBackground(GUIResource.getInstance().getColorYellow());
		}
		gc.setForeground(GUIResource.getInstance().getColorBlack());
		gc.fillRectangle(screen.x, screen.y, iconsize, iconsize);

		/*
		Image im = step_images[si.getStepType()];
		if (im != null) // Draw the icon!
		{
			Rectangle bounds = im.getBounds();
			gc.drawImage(im, 0, 0, bounds.width, bounds.height, screen.x, screen.y, iconsize, iconsize);
		}
		*/
		gc.setBackground(GUIResource.getInstance().getColorBackground());
		gc.drawRectangle(screen.x - 1, screen.y - 1, iconsize + 1, iconsize + 1);
		//gc.setXORMode(true);
        org.eclipse.swt.graphics.Point ext = gc.textExtent(name);
		Point textsize = new Point(ext.x, ext.y);
		gc.setBackground(GUIResource.getInstance().getColorDarkGray());
		gc.setForeground(GUIResource.getInstance().getColorBlack());
		gc.setLineWidth(linewidth);
		int xpos = screen.x + (iconsize / 2) - (textsize.x / 2);
		int ypos = screen.y + iconsize + Const.SYMBOLSIZE+4;
		//gc.fillRectangle(xpos - xmargin, ypos - ymargin, textsize.x + xmargin * 2, textsize.y + ymargin * 2);
		//gc.drawRectangle( xpos-xmargin, ypos-ymargin, textsize.x+xmargin*2, textsize.y+ymargin*2);
		gc.drawText(name, xpos, ypos, SWT.DRAW_TRANSPARENT);
		//gc.setXORMode(false);

	}

	private void drawLine(GC gc, TableMeta fs, TableMeta ts, RelationshipMeta hi, boolean is_candidate)
	{
		int line[] = new int[4];
		double angle = getLine(fs, ts, line);

		gc.setLineWidth(linewidth);
		Color col;

		if (is_candidate)
		{
			col = GUIResource.getInstance().getColorBlue();
		}
		else 
		{
			col = GUIResource.getInstance().getColorBlack();
		}
		gc.setForeground(col);

		drawArrow(gc, line, hi, angle);

		gc.setForeground(GUIResource.getInstance().getColorBlack());
		gc.setBackground(GUIResource.getInstance().getColorBackground());
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
		Point max = schema.getMaximum();
		Point thumb = getThumb(area, max);
		Point offset = getOffset(thumb, area);

		return offset;

	}
	private Point getOffset(Point thumb, Point area)
	{
		Point p = new Point(0, 0);
		Point sel = new Point(hori.getSelection(), vert.getSelection());

		if (thumb.x ==0 || thumb.y==0) return p;
		
		p.x = -sel.x * area.x / thumb.x;
		p.y = -sel.y * area.y / thumb.y;

		return p;
	}

	public int sign(int n)
	{
		return n < 0 ? -1 : (n > 0 ? 1 : 1);
	}

	private void editTable(TableMeta tableinfo)
	{
		spoon.editTableInfo(tableinfo);
	}

	private void editNote(NotePadMeta ni)
	{
		NotePadMeta before = (NotePadMeta)ni.clone();
		
		String title = "Notes";
		String message = "Note text:";
		EnterTextDialog dd = new EnterTextDialog(shell, title, message, ni.getNote());
		String n = dd.open();
		if (n != null)
		{
			ni.setChanged();
			ni.setNote( n );
			ni.width = Const.NOTE_MIN_SIZE;
			ni.height = Const.NOTE_MIN_SIZE;

			NotePadMeta after = (NotePadMeta)ni.clone();
			spoon.addUndoChange(new NotePadMeta[] { before }, new NotePadMeta[] { after }, new int[] { schema.indexOfNote(ni) }  );
			spoon.refreshGraph();
		}
	}

	private void editRelationship(RelationshipMeta hopinfo)
	{
		String name = hopinfo.toString();
		log.logDebug(toString(), "Editing relationship: " + name);
		spoon.editRelationship(name);
	}

	private void newRelationship()
	{
		String fr = schema.getSelectedName(0);
		String to = schema.getSelectedName(1);
		spoon.newRelationship(fr, to);
	}

	private double calcRelationshipLine(Point A, Point B, Point X, Point Y)
	{
		double angle = calcAngle(A,B);
		
		if (angle>-45 && angle<=45)
		{
			X.x = A.x + iconsize + Const.SYMBOLSIZE;	Y.x = B.x - Const.SYMBOLSIZE;
			X.y = A.y + iconsize/2;				Y.y = B.y + iconsize/2;
		}
		else
		if (angle>45 && angle <=135)
		{
			X.x = A.x + iconsize/2;				Y.x = B.x + iconsize/2;
			X.y = A.y + iconsize + Const.SYMBOLSIZE;	Y.y = B.y - Const.SYMBOLSIZE;
		}
		else
		if (angle>135 || angle<=-135)
		{
			X.x = A.x - Const.SYMBOLSIZE;				Y.x = B.x + iconsize + Const.SYMBOLSIZE;
			X.y = A.y + iconsize/2;				Y.y = B.y + iconsize / 2;
		}
		else
		if (angle<=-45)
		{
			X.x = A.x + iconsize/2;				Y.x = B.x + iconsize/2;
			X.y = A.y - Const.SYMBOLSIZE;				Y.y = B.y + iconsize + Const.SYMBOLSIZE;
		}
		
		return angle;
	}
	
	private double calcAngle(Point A, Point B)
	{
		return Math.atan2( B.y - A.y, B.x - A.x) * 360 / ( 2 * Math.PI );
	}

	private void drawArrow(GC gc, int line[], RelationshipMeta ri, double angle)
	{
		Point X = real2screen(line[0], line[1]);
		Point Y = real2screen(line[2], line[3]);
		
		// Main line connecting the 2 entities (tables)
		gc.drawLine(X.x, X.y, Y.x, Y.y);
		
		Point a,b,c;
		Point a2,b2,c2;
		
		// Start of the relationship N:, 1:, 0:
		// 1:
		if (ri.getType()==RelationshipMeta.TYPE_RELATIONSHIP_1_0 ||
			ri.getType()==RelationshipMeta.TYPE_RELATIONSHIP_1_1 ||
			ri.getType()==RelationshipMeta.TYPE_RELATIONSHIP_1_N ||
			ri.isComplex()
			)
		{
			if (angle>-45 && angle<=45)
			{
				b = new Point(X.x - Const.SYMBOLSIZE, X.y             );
			}
			else
			if (angle>45 && angle <=135)
			{
				b = new Point(X.x             , X.y - Const.SYMBOLSIZE);
			}
			else
			if (angle>135 || angle<=-135)
			{
				b = new Point(X.x + Const.SYMBOLSIZE, X.y             );
			}
			else  // (angle<=-45)
			{
				b = new Point(X.x             , X.y + Const.SYMBOLSIZE);
			}
			
			gc.drawLine(X.x, X.y, b.x, b.y);
		}
		else
		// N:
		if (ri.getType()==RelationshipMeta.TYPE_RELATIONSHIP_N_0 ||
			ri.getType()==RelationshipMeta.TYPE_RELATIONSHIP_N_1 ||
			ri.getType()==RelationshipMeta.TYPE_RELATIONSHIP_N_N
			)
		{
			if (angle>-45 && angle<=45)
			{
				a = new Point(X.x - Const.SYMBOLSIZE, X.y - Const.SYMBOLSIZE);
				b = new Point(X.x - Const.SYMBOLSIZE, X.y             );
				c = new Point(X.x - Const.SYMBOLSIZE, X.y + Const.SYMBOLSIZE);
			}
			else
			if (angle>45 && angle <=135)
			{
				a = new Point(X.x - Const.SYMBOLSIZE, X.y - Const.SYMBOLSIZE);
				b = new Point(X.x             , X.y - Const.SYMBOLSIZE);
				c = new Point(X.x + Const.SYMBOLSIZE, X.y - Const.SYMBOLSIZE);
			}
			else
			if (angle>135 || angle<=-135)
			{
				a = new Point(X.x + Const.SYMBOLSIZE, X.y - Const.SYMBOLSIZE);
				b = new Point(X.x + Const.SYMBOLSIZE, X.y             );
				c = new Point(X.x + Const.SYMBOLSIZE, X.y + Const.SYMBOLSIZE);
			}
			else  // (angle<=-45)
			{
				a = new Point(X.x - Const.SYMBOLSIZE, X.y + Const.SYMBOLSIZE);
				b = new Point(X.x             , X.y + Const.SYMBOLSIZE);
				c = new Point(X.x + Const.SYMBOLSIZE, X.y + Const.SYMBOLSIZE);
			}
			
			gc.drawLine(X.x, X.y, a.x, a.y);
			gc.drawLine(X.x, X.y, b.x, b.y);
			gc.drawLine(X.x, X.y, c.x, c.y);
		}
		else // 0:
		{
			if (angle>-45 && angle<=45)
			{
				a = new Point(X.x - Const.SYMBOLSIZE, X.y - Const.SYMBOLSIZE/2);
			}
			else
			if (angle>45 && angle <=135)
			{
				a = new Point(X.x - Const.SYMBOLSIZE/2, X.y-Const.SYMBOLSIZE);
			}
			else
			if (angle>135 || angle<=-135)
			{
				a = new Point(X.x, X.y - Const.SYMBOLSIZE/2);
			}
			else  // (angle<=-45)
			{
				a = new Point(X.x - Const.SYMBOLSIZE/2, X.y);
			}
			
			gc.drawOval(a.x, a.y, Const.SYMBOLSIZE, Const.SYMBOLSIZE);
		}


		// Start of the relationship :N, :1, :0
		// :1
		if (ri.getType()==RelationshipMeta.TYPE_RELATIONSHIP_0_1 ||
			ri.getType()==RelationshipMeta.TYPE_RELATIONSHIP_1_1 ||
			ri.getType()==RelationshipMeta.TYPE_RELATIONSHIP_N_1 ||
			ri.isComplex()
			)
		{
			if (angle>-45 && angle<=45)  // -->
			{
				b2 = new Point( Y.x + Const.SYMBOLSIZE, Y.y             );
			}
			else
			if (angle>45 && angle <=135)  //    |
			{                             //   \|/
										  //    '
				b2 = new Point( Y.x 	        , Y.y + Const.SYMBOLSIZE );
			}
			else
			if (angle>135 || angle<=-135)  //  <--
			{
				b2 = new Point( Y.x - Const.SYMBOLSIZE, Y.y              );
			}
			else  // (angle<=-45)          //   .
			{                              //  /|\
										   //   |
				b2 = new Point( Y.x 	        , Y.y - Const.SYMBOLSIZE );
			}
			
			gc.drawLine(Y.x, Y.y, b2.x, b2.y);
		}
		else // :N
		if (ri.getType()==RelationshipMeta.TYPE_RELATIONSHIP_0_N ||
			ri.getType()==RelationshipMeta.TYPE_RELATIONSHIP_1_N ||
			ri.getType()==RelationshipMeta.TYPE_RELATIONSHIP_N_N
			)
		{
			if (angle>-45 && angle<=45)  // -->
			{
				a2 = new Point( Y.x + Const.SYMBOLSIZE, Y.y - Const.SYMBOLSIZE);
				b2 = new Point( Y.x + Const.SYMBOLSIZE, Y.y             );
				c2 = new Point( Y.x + Const.SYMBOLSIZE, Y.y + Const.SYMBOLSIZE);
			}
			else
			if (angle>45 && angle <=135)  //    |
			{                             //   \|/
										  //    '
				a2 = new Point( Y.x + Const.SYMBOLSIZE, Y.y + Const.SYMBOLSIZE );
				b2 = new Point( Y.x 	        , Y.y + Const.SYMBOLSIZE );
				c2 = new Point( Y.x - Const.SYMBOLSIZE, Y.y + Const.SYMBOLSIZE );
			}
			else
			if (angle>135 || angle<=-135)  //  <--
			{
				a2 = new Point( Y.x - Const.SYMBOLSIZE, Y.y - Const.SYMBOLSIZE );
				b2 = new Point( Y.x - Const.SYMBOLSIZE, Y.y              );
				c2 = new Point( Y.x - Const.SYMBOLSIZE, Y.y + Const.SYMBOLSIZE );
			}
			else  // (angle<=-45)          //   .
			{                              //  /|\
										   //   |
				a2 = new Point( Y.x + Const.SYMBOLSIZE, Y.y - Const.SYMBOLSIZE );
				b2 = new Point( Y.x 	        , Y.y - Const.SYMBOLSIZE );
				c2 = new Point( Y.x - Const.SYMBOLSIZE, Y.y - Const.SYMBOLSIZE );
			}
			
			gc.drawLine(Y.x, Y.y, a2.x, a2.y);
			gc.drawLine(Y.x, Y.y, b2.x, b2.y);
			gc.drawLine(Y.x, Y.y, c2.x, c2.y);
		}
		else // :0
		{
			if (angle>-45 && angle<=45)
			{
				a2 = new Point(Y.x, Y.y - Const.SYMBOLSIZE/2);
			}
			else
			if (angle>45 && angle <=135)
			{
				a2 = new Point(Y.x - Const.SYMBOLSIZE/2, Y.y);
			}
			else
			if (angle>135 || angle<=-135)
			{
				a2 = new Point(Y.x - Const.SYMBOLSIZE, Y.y - Const.SYMBOLSIZE/2);
			}
			else  // (angle<=-45)
			{
				a2 = new Point(Y.x - Const.SYMBOLSIZE/2, Y.y - Const.SYMBOLSIZE);
			}
			
			gc.drawOval(a2.x, a2.y, Const.SYMBOLSIZE, Const.SYMBOLSIZE);
		}

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
		if (!(((x >= x1 && x <= x2) || (x >= x2 && x <= x1)) && ((y >= y1 && y <= y2) || (y >= y2 && y <= y1))))
			return false;

		double angle_line = Math.atan2(y2 - y1, x2 - x1) + Math.PI;
		double angle_point = Math.atan2(y - y1, x - x1) + Math.PI;

		// Same angle, or close enough?
		if (angle_point >= angle_line - 0.01 && angle_point <= angle_line + 0.01)
			return true;

		return false;
	}

	private void snaptogrid(int size)
	{
		// First look for the minimum x coordinate...
		for (int i = 0; i < schema.nrTables(); i++)
		{
			TableMeta si = schema.getTable(i);
			if (si.isSelected())
			{
				Point p = si.getLocation();
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
			}
		}
		redraw();
	}

	private void allignleft()
	{
		int min = 99999;

		// First look for the minimum x coordinate...
		for (int i = 0; i < schema.nrTables(); i++)
		{
			TableMeta si = schema.getTable(i);
			if (si.isSelected())
			{
				Point p = si.getLocation();
				if (p.x < min)
					min = p.x;
			}
		}
		// Then apply the coordinate...
		for (int i = 0; i < schema.nrTables(); i++)
		{
			TableMeta si = schema.getTable(i);
			if (si.isSelected())
			{
				Point p = si.getLocation();
				p.x = min;
			}
		}
		redraw();
	}

	private void allignright()
	{
		int max = -99999;

		// First look for the maximum x coordinate...
		for (int i = 0; i < schema.nrTables(); i++)
		{
			TableMeta si = schema.getTable(i);
			if (si.isSelected())
			{
				Point p = si.getLocation();
				if (p.x > max)
					max = p.x;
			}
		}
		// Then apply the coordinate...
		for (int i = 0; i < schema.nrTables(); i++)
		{
			TableMeta si = schema.getTable(i);
			if (si.isSelected())
			{
				Point p = si.getLocation();
				p.x = max;
			}
		}
		redraw();
	}

	private void alligntop()
	{
		int min = 99999;

		// First look for the minimum y coordinate...
		for (int i = 0; i < schema.nrTables(); i++)
		{
			TableMeta si = schema.getTable(i);
			if (si.isSelected())
			{
				Point p = si.getLocation();
				if (p.y < min)
					min = p.y;
			}
		}
		// Then apply the coordinate...
		for (int i = 0; i < schema.nrTables(); i++)
		{
			TableMeta si = schema.getTable(i);
			if (si.isSelected())
			{
				Point p = si.getLocation();
				p.y = min;
			}
		}
		redraw();
	}

	private void allignbottom()
	{
		int max = -99999;

		// First look for the maximum y coordinate...
		for (int i = 0; i < schema.nrTables(); i++)
		{
			TableMeta si = schema.getTable(i);
			if (si.isSelected())
			{
				Point p = si.getLocation();
				if (p.y > max)
					max = p.y;
			}
		}
		// Then apply the coordinate...
		for (int i = 0; i < schema.nrTables(); i++)
		{
			TableMeta si = schema.getTable(i);
			if (si.isSelected())
			{
				Point p = si.getLocation();
				p.y = max;
			}
		}
		redraw();
	}

	private void distributehorizontal()
	{
		int min = 99999;
		int max = -99999;
		int nr = schema.nrSelected();
		if (nr <= 1)
			return;
		int order[] = new int[nr];

		// First look for the minimum & maximum x coordinate...
		int selnr = 0;
		for (int i = 0; i < schema.nrTables(); i++)
		{
			TableMeta si = schema.getTable(i);
			if (si.isSelected())
			{
				Point p = si.getLocation();
				if (p.x < min)
					min = p.x;
				if (p.x > max)
					max = p.x;
				order[selnr] = i;
				selnr++;
			}
		}

		// Difficult to keep the steps in the correct order.
		// If you just set the x-coordinates, you get special effects.
		// Best is to keep the current order of things.
		// First build an arraylist and store the order there.
		// Then sort order[], based upon the coordinate of the step.
		for (int i = 0; i < nr - 1; i++)
		{
			for (int j = i; j < nr - 1; j++)
			{
				Point p1 = schema.getTable(order[j]).getLocation();
				Point p2 = schema.getTable(order[j + 1]).getLocation();
				if (p1.x > p2.x) // swap
				{
					int dummy = order[j];
					order[j] = order[j + 1];
					order[j + 1] = dummy;
				}
			}
		}

		// The distance between two steps becomes.
		int distance = (max - min) / (nr - 1);

		for (int i = 0; i < nr; i++)
		{
			Point p = schema.getTable(order[i]).getLocation();
			p.x = min + (i * distance);
		}

		redraw();
	}

	public void distributevertical()
	{
		int min = 99999;
		int max = -99999;
		int nr = schema.nrSelected();
		if (nr <= 1)
			return;
		int order[] = new int[nr];

		// First look for the minimum & maximum y coordinate...
		int selnr = 0;
		for (int i = 0; i < schema.nrTables(); i++)
		{
			TableMeta si = schema.getTable(i);
			if (si.isSelected())
			{
				Point p = si.getLocation();
				if (p.y < min)
					min = p.y;
				if (p.y > max)
					max = p.y;
				order[selnr] = i;
				selnr++;
			}
		}

		// Difficult to keep the steps in the correct order.
		// If you just set the x-coordinates, you get special effects.
		// Best is to keep the current order of things.
		// First build an arraylist and store the order there.
		// Then sort order[], based upon the coordinate of the step.
		for (int i = 0; i < nr - 1; i++)
		{
			for (int j = i; j < nr - 1; j++)
			{
				Point p1 = schema.getTable(order[j]).getLocation();
				Point p2 = schema.getTable(order[j + 1]).getLocation();
				if (p1.y > p2.y) // swap
				{
					int dummy = order[j];
					order[j] = order[j + 1];
					order[j + 1] = dummy;
				}
			}
		}

		// The distance between two steps becomes.
		int distance = (max - min) / (nr - 1);

		for (int i = 0; i < nr; i++)
		{
			Point p = schema.getTable(order[i]).getLocation();
			p.y = min + (i * distance);
		}

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

	public void newProps()
	{
        GUIResource.getInstance().reload();
		iconsize = schema.props.getIconSize();
		linewidth = schema.props.getLineWidth();
	}

	public String toString()
	{
		return this.getClass().getName();
	}
}
