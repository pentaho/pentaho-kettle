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

 


package be.ibridge.kettle.core.widget;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Vector;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.ControlEditor;
import org.eclipse.swt.custom.TableCursor;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import be.ibridge.kettle.core.ColumnInfo;
import be.ibridge.kettle.core.Condition;
import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.Props;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.TransAction;
import be.ibridge.kettle.core.dialog.EnterConditionDialog;
import be.ibridge.kettle.core.value.Value;


/**
 * Widget to display or modify data, displayed in a Table format.
 * 
 * @author Matt
 * @since 27-05-2003
 */
public class TableView extends Composite 
{
	private Composite     parent;
	private int           style;
	private ColumnInfo[]  columns;
	private int           rows;
	private boolean       readonly;
	private int           button_rownr;		
	private int           button_colnr;
	private String        button_content;
	
	private boolean 	  previous_shift;
	private int     	  from_selection;
	
	public  Table         table;
	public  TableCursor   cursor; 
	private ControlEditor editor;
	private TableColumn[] tablecolumn;
	// private ArrayList     items;
	
	private Props         props;

	private Text          text;	
	private CCombo        combo;
	private Button        button;
		
	private KeyListener       lsKeyText,   lsKeyCombo;
	private FocusAdapter      lsFocusText, lsFocusCombo;
	private ModifyListener    lsModCombo;
	private TraverseListener  lsTraverse;
	
	private int sortfield;
	private boolean sort_desc;
	
	private boolean field_changed; 
	
	private Menu mRow;
	private SelectionAdapter lsRowInsBef, lsRowInsAft, lsClipAll, lsCol1, lsCol2, lsRowUp, lsRowDown, lsClear, lsCopyToAll, lsSelAll, lsUnselAll, lsPasteAll, lsCutAll, lsDelAll, lsKeep, lsFilter, lsEditUndo, lsEditRedo;
	
	private ModifyListener lsMod, lsUndo;
	private Clipboard  clipboard;
	private Image      dummy_image;
	private GC         dummy_gc;
    private Font       gridFont;
	
	private int last_carret_position;
	
	private ArrayList undo;
	private int      undo_position;
	private String[] before_edit;
	private MenuItem miEditUndo, miEditRedo;
	
	private int prev_rownr;
	
	private static final String CLIPBOARD_DELIMITER = "\t";

	private Condition condition;

	public TableView(Composite par, int st, ColumnInfo[] c, int r, ModifyListener lsm, Props pr)
	{
		this(par, st, c, r, false, lsm, pr);
	}

	public TableView(Composite par, int st, ColumnInfo[] c, int r, boolean ro, ModifyListener lsm, Props pr)
	{
		super(par, SWT.NO_BACKGROUND | SWT.NO_FOCUS | SWT.NO_MERGE_PAINTS | SWT.NO_RADIO_GROUP);
		parent  = par;
		style   = st;
		columns = c;
		rows    = r;
		props   = pr;
		readonly= ro;
		clipboard=null;
				
		sortfield = 0;
		sort_desc = false;
		
		from_selection = -1;
		previous_shift = false;
		
		last_carret_position = -1;
		
		condition = null;
		
		prev_rownr=-1;
		
		lsMod = lsm;
		
		clearUndo();
		
		lsUndo = new ModifyListener()
			{
				public void modifyText(ModifyEvent arg0)
				{
					field_changed=true;
				}
			}
			;
		
		Display disp = parent.getDisplay();
		dummy_image = new Image(disp, 1, 1);
		dummy_gc = new GC(dummy_image);
        
        gridFont = new Font(disp, props.getGridFont());
        dummy_gc.setFont(gridFont);
		
		FormLayout controlLayout = new FormLayout();
		controlLayout.marginLeft   = 0;
		controlLayout.marginRight  = 0;
		controlLayout.marginTop    = 0;
		controlLayout.marginBottom = 0;
		
		setLayout(controlLayout);
		
		//setLayout(new GridLayout());
		
		// Create table, add columns & rows...
		table=new Table(this, style | SWT.MULTI);
        props.setLook(table, Props.WIDGET_STYLE_TABLE);
		table.setLinesVisible(true);
		// table.setLayout(new FormLayout());
		// table.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		FormData fdTable = new FormData();
		fdTable.left = new FormAttachment(0,0);
		fdTable.right = new FormAttachment(100,0);
		fdTable.top = new FormAttachment(0,0);
		fdTable.bottom = new FormAttachment(100,0);
		table.setLayoutData(fdTable);
			
		tablecolumn=new TableColumn[columns.length+1];
		tablecolumn[0] = new TableColumn(table, SWT.RIGHT);
		tablecolumn[0].setResizable(true);
		tablecolumn[0].setText("#");
		tablecolumn[0].setWidth(25);
		tablecolumn[0].setAlignment(SWT.RIGHT);

		for (int i=0;i<columns.length;i++)
		{
			tablecolumn[i+1]=new TableColumn(table, columns[i].getAllignement());
			tablecolumn[i+1].setResizable(true);
			if (columns[i].getName()!=null) tablecolumn[i+1].setText(columns[i].getName());
			//tablecolumn[i+1].setWidth((width-30)/columns.length);
			tablecolumn[i+1].pack();
		}
		
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		
		// Set the default values...
		if (rows > 0)
		{
            table.setItemCount(rows);
		}
		else
		{
            table.setItemCount(1);
		}
		
		setRowNums();
		
		cursor = new TableCursor(table, SWT.NONE);
		cursor.setVisible(true);
        props.setLook(cursor, Props.WIDGET_STYLE_TABLE);
		
		cursor.layout();
		cursor.pack();
		
		// create a ControlEditor field to edit the contents of a cell
		editor = new ControlEditor(table);
		
		mRow = new Menu(table);
		MenuItem miRowInsBef = new MenuItem(mRow, SWT.NONE); miRowInsBef.setText("Insert before this row");
		MenuItem miRowInsAft = new MenuItem(mRow, SWT.NONE); miRowInsAft.setText("Insert after this row");
		new MenuItem(mRow, SWT.SEPARATOR);
		MenuItem miRowUp     = new MenuItem(mRow, SWT.NONE); miRowUp    .setText("Move up\tCTRL-UP");
		MenuItem miRowDown   = new MenuItem(mRow, SWT.NONE); miRowDown  .setText("Move down\tCTRL-DOWN");
		new MenuItem(mRow, SWT.SEPARATOR);
		MenuItem miCol1      = new MenuItem(mRow, SWT.NONE); miCol1     .setText("Optimal Column size incl. header\tF3");
		MenuItem miCol2      = new MenuItem(mRow, SWT.NONE); miCol2     .setText("Optimal Column size excl. header\tF4");
		new MenuItem(mRow, SWT.SEPARATOR);
		MenuItem miClear     = new MenuItem(mRow, SWT.NONE); miClear    .setText("Clear all");
		new MenuItem(mRow, SWT.SEPARATOR);
		MenuItem miSelAll    = new MenuItem(mRow, SWT.NONE); miSelAll   .setText("Select all rows\tCTRL-A");
		MenuItem miUnselAll  = new MenuItem(mRow, SWT.NONE); miUnselAll .setText("Clear selection\tESC");
		MenuItem miFilter    = new MenuItem(mRow, SWT.NONE); miFilter   .setText("Filtered selection\tCTRL-F");
		new MenuItem(mRow, SWT.SEPARATOR);
		MenuItem miClipAll   = new MenuItem(mRow, SWT.NONE); miClipAll  .setText("Copy selected lines to clipboard\tCTRL-C");
		MenuItem miPasteAll  = new MenuItem(mRow, SWT.NONE); miPasteAll .setText("Paste clipboard to table\tCTRL-V");
		MenuItem miCutAll    = new MenuItem(mRow, SWT.NONE); miCutAll   .setText("Cut selected lines\tCTRL-X");
		MenuItem miDelAll    = new MenuItem(mRow, SWT.NONE); miDelAll   .setText("Delete selected lines\tDEL");
		MenuItem miKeep      = new MenuItem(mRow, SWT.NONE); miKeep     .setText("Keep only selected lines\tCTRL-K");
		new MenuItem(mRow, SWT.SEPARATOR);
		MenuItem miCopyToAll = new MenuItem(mRow, SWT.NONE); miCopyToAll.setText("Copy field value to all rows");
		new MenuItem(mRow, SWT.SEPARATOR);
		miEditUndo           = new MenuItem(mRow, SWT.NONE);
		miEditRedo           = new MenuItem(mRow, SWT.NONE);
		setUndoMenu();
		
		if (readonly)
		{
			miRowInsBef.setEnabled(false);
			miRowInsAft.setEnabled(false);
			miRowUp    .setEnabled(false);
			miRowDown  .setEnabled(false);
			miClear    .setEnabled(false);
			miCopyToAll.setEnabled(false);
			miPasteAll .setEnabled(false);
			miDelAll   .setEnabled(false);
			miCutAll   .setEnabled(false);
			miKeep     .setEnabled(false);
		}
		
		lsRowInsBef = new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { insertRowBefore();  } };		
		lsRowInsAft = new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { insertRowAfter();   } };		
		lsCol1      = new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { optWidth(true);     } };		
		lsCol2      = new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { optWidth(false);    } };		
		lsRowUp     = new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { moveRowUp(false);   } };		
		lsRowDown   = new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { moveRowDown(false); } };		
		lsClear     = new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { clearAll(true);     } };
		lsClipAll   = new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { clipSelected();     } };
		lsCopyToAll = new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { copyToAll();        } };
		lsSelAll    = new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { selectAll();        } };
		lsUnselAll  = new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { unselectAll();      } };
		lsPasteAll  = new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { pasteSelected();    } };
		lsCutAll    = new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { cutSelected();      } };
		lsDelAll    = new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { delSelected();      } };
		lsKeep      = new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { keepSelected();     } };
		lsFilter    = new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { setFilter();        } };
		lsEditUndo  = new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { undoAction();       } };
		lsEditRedo  = new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { redoAction();       } };

		miRowInsBef.addSelectionListener(lsRowInsBef);
		miRowInsAft.addSelectionListener(lsRowInsAft);
		miCol1     .addSelectionListener(lsCol1 );
		miCol2     .addSelectionListener(lsCol2 );
		miRowUp    .addSelectionListener(lsRowUp );
		miRowDown  .addSelectionListener(lsRowDown );
		miClear    .addSelectionListener(lsClear );
		miClipAll  .addSelectionListener(lsClipAll );
		miCopyToAll.addSelectionListener(lsCopyToAll);
		miSelAll   .addSelectionListener(lsSelAll);
		miUnselAll .addSelectionListener(lsUnselAll);
		miPasteAll .addSelectionListener(lsPasteAll);
		miCutAll   .addSelectionListener(lsCutAll);
		miDelAll   .addSelectionListener(lsDelAll);
		miKeep     .addSelectionListener(lsKeep);
		miFilter   .addSelectionListener(lsFilter);
		miEditUndo .addSelectionListener(lsEditUndo);
		miEditRedo .addSelectionListener(lsEditRedo);
	
		table.setMenu(mRow);
		cursor.setMenu(mRow);
		
		lsFocusText = new FocusAdapter()
			{
				public void focusLost(FocusEvent e)
				{
					TableItem row = cursor.getRow();
					if (row==null) return;
					int colnr = cursor.getColumn();
					int rownr = table.indexOf(row);
					row.setText(colnr, text.getText());
					text.dispose();
					cursor.redraw();
															
					String after_edit[] = getItemText(row);
					checkChanged(new String[][] { before_edit }, new String[][] { after_edit }, new int[] { rownr });
				}
			};
		lsFocusCombo = new FocusAdapter()
			{
				public void focusLost(FocusEvent e)
				{
					TableItem row = cursor.getRow();
					if (row==null) return;
					int colnr = cursor.getColumn();
					int rownr = table.indexOf(row);
					if (colnr>0)
					{
						row.setText(colnr, combo.getText());
																
						String after_edit[] = getItemText(row);
						checkChanged(new String[][] { before_edit }, new String[][] { after_edit }, new int[] { rownr });
					}
					combo.dispose();
					cursor.redraw();
				}
			};
		lsModCombo = new ModifyListener()
			{
				public void modifyText(ModifyEvent e)
				{
					TableItem row = cursor.getRow();
					if (row==null) return;
					int colnr = cursor.getColumn();
					int rownr = table.indexOf(row);
					row.setText(colnr, combo.getText());
					
					String after_edit[] = getItemText(row);
					checkChanged(new String[][] { before_edit }, new String[][] { after_edit }, new int[] { rownr });
				}
			};


		// Catch the keys pressed when editing a Text-field...
		lsKeyText = new KeyAdapter() 
			{
				public void keyPressed(KeyEvent e) 
				{
					boolean right=false;
					boolean left=false;
					
					/*
					left  = e.keyCode == SWT.ARROW_LEFT  && last_carret_position==0;
					
					if (text!=null && !text.isDisposed())
					right = e.keyCode == SWT.ARROW_RIGHT && last_carret_position==text.getText().length();
					*/
					
					// "ENTER": close the text editor and copy the data over 
					if (   e.character == SWT.CR
						|| e.keyCode   == SWT.ARROW_DOWN
						|| e.keyCode   == SWT.ARROW_UP
						|| e.keyCode   == SWT.TAB
						|| left || right  
						) 
					{
						TableItem row = cursor.getRow();
						if (row==null) return;
						int rownr = table.indexOf(row);
						int colnr = cursor.getColumn();

						applyTextChange(row, rownr, colnr);
						
						int maxcols=table.getColumnCount();
						int maxrows=table.getItemCount();
											
						boolean sel=false;
						if (e.keyCode == SWT.ARROW_DOWN && rownr<maxrows-1)
						{
							rownr++;
							sel=true;
						}
						if (e.keyCode == SWT.ARROW_UP && rownr>0)
						{
							rownr--;
							sel=true;
						}
						// TAB
						if ( ( e.keyCode == SWT.TAB && (( e.stateMask & SWT.SHIFT )==0 )) ||
							 right
						   )
						{
							colnr++;
							sel=true;
						}
						// Shift Tab
						if ( (e.keyCode == SWT.TAB && (( e.stateMask & SWT.SHIFT )!=0 )) ||
							 left
						   )
						{
							colnr--;
							sel=true;
						}
						if (colnr<1) // from SHIFT-TAB
						{
							colnr=maxcols-1;
							if (rownr>0) rownr--;
						}
						if (colnr>=maxcols)  // from TAB
						{
							colnr=1;
							rownr++;

						}
						// Tab beyond last line: add a line to table!
						if (rownr>=maxrows)
						{
							TableItem item = new TableItem(table, SWT.NONE, rownr);
							item.setText(1, "field"+(table.getItemCount()-1));
							setRowNums();
						}
						if (sel)
						{
							row = table.getItem(rownr);
							edit(rownr, colnr);
						}						
					}
					else
					if (e.keyCode   == SWT.ESC)
					{
						text.dispose();
					}
					
					
					last_carret_position = text.isDisposed()?-1:text.getCaretPosition();
				}
			};

		// Catch the keys pressed when editing a Combo field
		lsKeyCombo = new KeyAdapter() 
			{
				public void keyPressed(KeyEvent e) 
				{
					boolean right=false;
					boolean left=false;
					
					left  = e.keyCode == SWT.ARROW_LEFT  && last_carret_position==0;
					if (combo!=null && !combo.isDisposed())
					right = e.keyCode == SWT.ARROW_RIGHT && last_carret_position==combo.getText().length();
					
					// "ENTER": close the text editor and copy the data over 
					if (   e.character == SWT.CR
						|| e.keyCode   == SWT.TAB
						|| left || right  
						) 
					{
						TableItem row = cursor.getRow();
						if (row==null) return;
						int colnr = cursor.getColumn();
						int rownr = table.indexOf(row);
						row.setText(colnr, combo.getText());
						combo.dispose();

						String after_edit[] = getItemText(row);
						checkChanged(new String[][] { before_edit }, new String[][] { after_edit }, new int[] { rownr });
						
						int maxcols=table.getColumnCount();
						int maxrows=table.getItemCount();
											
						boolean sel=false;
						// TAB
						if ( (e.keyCode == SWT.TAB && (( e.stateMask & SWT.SHIFT )==0 )) ||
							 right
						   )
						{
							colnr++;
							sel=true;
						}
						// Shift Tab
						if ( (e.keyCode == SWT.TAB && (( e.stateMask & SWT.SHIFT )!=0 )) ||
							 right
						   )
						{
							colnr--;
							sel=true;
						}
						if (colnr<1) // from SHIFT-TAB
						{
							colnr=maxcols-1;
							if (rownr>0) rownr--;
						}
						if (colnr>=maxcols)  // from TAB
						{
							colnr=1;
							rownr++;

						}
						// Tab beyond last line: add a line to table!
						if (rownr>=maxrows)
						{
							TableItem item = new TableItem(table, SWT.NONE, rownr);
							item.setText(1, "field"+(table.getItemCount()-1));
							setRowNums();
						}
						if (sel)
						{
							edit(rownr, colnr);
						}
					}
					else
					if (e.keyCode   == SWT.ESC)
					{
						combo.dispose();
					}
					
					last_carret_position = combo.isDisposed()?-1:0;
				}
			};
		
		/*
		 * It seems there is an other keyListener active to help control the cursor.
		 * There is support for keys like LEFT/RIGHT/UP/DOWN/HOME/END/etc
		 * It presents us with a problem because we only get the position of the row/column AFTER the other listener did it's job.
		 * Therefor we added global variables prev_rownr and prev_colnr 
		 */
		
		KeyListener lsKeyCursor = 
			new KeyAdapter() 
			{
				public void keyPressed(KeyEvent e) 
				{
					int colnr, rownr;
					
					colnr   = cursor.getColumn();
					TableItem row     = cursor.getRow();
					if (row==null) return;
					rownr   = table.indexOf(row);
					
					int       maxcols = table.getColumnCount();
					int       maxrows = table.getItemCount();

					boolean shift   = ( e.stateMask & SWT.SHIFT  )!=0;
					if (!previous_shift && shift)
					{
						// Shift is pressed down: reset from_selection
						from_selection=prev_rownr;
					}
					previous_shift = shift;
					
					
					// Move rows up or down shortcuts...					
					if (!readonly && e.keyCode   == SWT.ARROW_DOWN && (( e.stateMask & SWT.CTRL)!=0 ))
					{
						moveRowDown(true);
					}
					else
					if (!readonly && e.keyCode   == SWT.ARROW_UP && (( e.stateMask & SWT.CTRL)!=0 ))
					{
						moveRowUp(false);
					}
					else
					// Select extra row down					
					if (e.keyCode   == SWT.ARROW_DOWN && shift)
					{
						// Select all indeces from "from_selection" to "row"
						selectRows(from_selection, rownr);
						table.showItem(row);
					}
					else
					// Select extra row up
					if (e.keyCode   == SWT.ARROW_UP && shift)
					{
						selectRows(rownr, from_selection);
						table.showItem(row);
					}
					else
					// Select all rows until end					
					if (e.keyCode   == SWT.HOME && shift)
					{
						// Select all indeces from "from_selection" to "row"
						if (prev_rownr>=0) selectRows(rownr, prev_rownr);
						table.showItem(row);
					}
					else
					// Select extra row up
					if (e.keyCode   == SWT.END && shift)
					{
						if (prev_rownr>=0) selectRows(prev_rownr, rownr);
						table.showItem(row);
					}
					// Moved cursor: set selection on the row in question.				
					if ( (e.keyCode   == SWT.ARROW_DOWN && !shift) ||
					     (e.keyCode   == SWT.ARROW_UP   && !shift) ||
					     (e.keyCode   == SWT.HOME       && !shift) ||
					     (e.keyCode   == SWT.END        && !shift) 
					   )
					{
						selectRows(rownr, rownr);
					}
					else
					// CTRL-A --> Select All lines
					if ((int)e.character ==  1 ) 
					{
						selectAll(); 
					}
					else
					// ESC --> unselect all
					if (e.keyCode == SWT.ESC)
					{
						unselectAll();
						selectRows(rownr, rownr);
					} 
					else
					// CTRL-C --> Copy selected lines to clipboard
					if ((int)e.character ==  3 ) 
					{
						clipSelected();
					}
					else
					// CTRL-K --> keep only selected lines
					if (!readonly && (int)e.character == 11  ) 
					{
						keepSelected();
					}
					else
					// CTRL-X --> Cut selected infomation...
					if (!readonly && (int)e.character ==  24  ) 
					{
						cutSelected();
					}
					// CTRL-V --> Paste selected infomation...
					if (!readonly && (int)e.character ==  22  ) 
					{
						pasteSelected();
					}
					else
					// F3 --> optimal width including headers
					if (e.keyCode == SWT.F3) 
					{
						optWidth(true);
					}
					else
					// DEL --> delete selected lines
					if (!readonly && e.keyCode == SWT.DEL) 
					{
						delSelected();
					}
					else
					// F4 --> optimal width excluding headers
					if (e.keyCode == SWT.F4) 
					{
						optWidth(false);
					}
					else
					// CTRL-Y --> redo action
					if ((int)e.character == 25  ) { redoAction(); }
					else 
					// CTRL-Z --> undo action
					if ((int)e.character == 26  ) { undoAction(); } 

					if (colnr>0)
					{
						
						boolean text_char = 
							(e.character>='a' && e.character<='z') ||
							(e.character>='A' && e.character<='Z') ||
							(e.character>='0' && e.character<='9') ||
							(e.character==' ') ||
						    (e.character=='_') ||
 							(e.character==',') ||
						    (e.character=='.') ||
							(e.character=='+') ||
							(e.character=='-') ||
							(e.character=='*') ||
							(e.character=='/') ||
							(e.character==';')							;

						//setSelection(row, rownr, colnr);
						// character a-z, A-Z, 0-9: start typing...
						if ( e.character==SWT.CR || 
						     e.keyCode == SWT.F2 ||
						     text_char
						   )
						{
							boolean select_text = true;
							char extra_char = 0;
							
							if (text_char)
							{
								extra_char=e.character;
								select_text = false;
							}
							edit(rownr, colnr, select_text, extra_char);
						}
						if (e.character==SWT.TAB)
						{
							// TAB
							if (e.keyCode == SWT.TAB && (( e.stateMask & SWT.SHIFT )==0 ))
							{
								colnr++;
							}
							// Shift Tab
							if (e.keyCode == SWT.TAB && (( e.stateMask & SWT.SHIFT )!=0 ))
							{
								colnr--;
							}
							if (colnr<1) // from SHIFT-TAB
							{
								colnr=maxcols-1;
								if (rownr>0) rownr--;
							}
							if (colnr>=maxcols)  // from TAB
							{
								colnr=1;
								rownr++;

							}
							// Tab beyond last line: add a line to table!
							if (rownr>=maxrows)
							{
								TableItem item = new TableItem(table, SWT.NONE, rownr);
								item.setText(1, "field"+(table.getItemCount()-1));
								setRowNums();
							}
							//row = table.getItem(rownr);
							edit(rownr, colnr);
						}
					}
					cursor.redraw();
					
					prev_rownr = rownr;
				}
			};
		cursor.addKeyListener(lsKeyCursor);
		
		// Table listens to the mouse:
		MouseAdapter lsMouseT = 
			new MouseAdapter()
			{
				public void mouseDown(MouseEvent e)
				{
					if (e.button==1)
					{
						boolean shift   = (e.stateMask & SWT.SHIFT)!=0;
						boolean control = (e.stateMask & SWT.CONTROL)!=0;
						if ( !shift && !control )
						{
							editSelected();
						}
					}
				}
			}
			;
		MouseAdapter lsMouseC = 
			new MouseAdapter()
			{
				public void mouseDown(MouseEvent e)
				{
					if (e.button==1)
					{
						editSelected();
					}
				}
			}
			;
		table.addMouseListener(lsMouseT);
		cursor.addMouseListener(lsMouseC);
		
		cursor.addSelectionListener
		(
			new SelectionAdapter() 
			{
				public void widgetDefaultSelected(SelectionEvent e)
				{
					editSelected();
				}
			}
		);
		
		// Add support for sorted columns!
		//
		final int nrcols = tablecolumn.length;
		for (int i=0;i<nrcols;i++)
		{
			final int colnr = i;
			Listener lsSort = new Listener() 
			 {
				public void handleEvent(Event e) 
				{
					// Sorting means: clear undo information!
					clearUndo();

                    sortTable(colnr);
				}
			 };
			 tablecolumn[i].addListener(SWT.Selection, lsSort);
		}
					

		
		lsTraverse= new TraverseListener() { public void keyTraversed(TraverseEvent e) { e.doit=false; }};
		table.addTraverseListener(lsTraverse);
		cursor.addTraverseListener(lsTraverse);
		
		// Clean up the clipboard
		addDisposeListener(new DisposeListener() 
			{
				public void widgetDisposed(DisposeEvent e) 
				{
					if (clipboard!=null) 
					{
						clipboard.dispose();
						clipboard=null;
					} 
					dummy_gc.dispose();
					dummy_image.dispose();
                    gridFont.dispose();
				}
			}
		);

		// Drag & drop source!

		// Drag & Drop for table-viewer
		Transfer[] ttypes = new Transfer[] {TextTransfer.getInstance() };
		
		DragSource ddSource = new DragSource(table, DND.DROP_MOVE | DND.DROP_COPY);
		ddSource.setTransfer(ttypes);
		ddSource.addDragListener(new DragSourceListener() 
			{
				public void dragStart(DragSourceEvent event){ }
	
				public void dragSetData(DragSourceEvent event) 
				{
					event.data = "TableView"+Const.CR+getSelectedText();					
				}
	
				public void dragFinished(DragSourceEvent event) {}
			}
		);
		
		table.layout();
		table.pack();
		
		optWidth(true);

		layout();
		pack();
	}
	
	public void sortTable(int colnr)
    {
        if (sortfield==colnr)
        {
            sort_desc = (!sort_desc);
        }
        else
        {
            sortfield=colnr;
            sort_desc = false;
        }
        
        // First, get all info and put it in a Vector of Rows...
        TableItem[] items = table.getItems();
        Vector v = new Vector();
        
        for (int i = 0; i < items.length; i++)
        {
            TableItem item = items[i];
            Row r = new Row();
            // First value is the color!
            Color bg = item.getBackground();
            Value colorValue = new Value("bg", (long)((bg.getRed()<<16)+(bg.getGreen()<<8)+(bg.getBlue())) );
            r.addValue( colorValue );
            
            for (int j=0;j<table.getColumnCount();j++)
            {
                Value value = new Value("Col#"+j, item.getText(j));
                r.addValue(value);
            }
            v.addElement(r);
        }
        // Sort the vector!
        quickSort(v);
        
        // Clear the table
        table.removeAll();
        
        // Get enumeration!
        Enumeration en = v.elements();
        
        // Refill the table
        while (en.hasMoreElements())
        {
            Row r = (Row)en.nextElement();
            TableItem item = new TableItem(table, SWT.NONE);
            
            Value colorValue = r.getValue(0);
            int red   = (int)( (colorValue.getInteger() & 0xFF0000) >>16 );
            int green = (int)( (colorValue.getInteger() & 0x00FF00) >> 8 );
            int blue  = (int)( (colorValue.getInteger() & 0x0000FF)      );
            Color bg = new Color(parent.getDisplay(), red, green, blue);
            item.setBackground(bg);
            bg.dispose();
            
            for (int i=1;i<r.size();i++)
            {
                Value value = r.getValue(i);
                item.setText(i-1, value.getString());
            }
        }
    }

    private void selectRows(int from, int to)
	{
		if (from>to) table.setSelection(to, from);
		else         table.setSelection(from, to);
	}
	
	private void applyTextChange(TableItem row, int rownr, int colnr)
	{
		row.setText(colnr, text.getText());
		text.dispose();
						
		String after_edit[] = getItemText(row);
		checkChanged(new String[][] { before_edit }, new String[][] { after_edit }, new int[] { rownr });
	}

	public void addModifyListener(ModifyListener ls)
	{
		lsMod = ls;
	}
	
	public void setColumnInfo(int idx, ColumnInfo col)
	{
		columns[idx]=col;
	}
	
	public void setColumnText(int idx, String text)
	{
		TableColumn col = table.getColumn(idx);
		col.setText(text);
	}

	public void setColumnToolTip(int idx, String text)
	{
		columns[idx].setToolTip(text);
	}
	
	private void editSelected()
	{
		int       colnr = cursor.getColumn();
		TableItem row   = cursor.getRow();
		if (row==null) return;
		int       rownr = table.indexOf(row);
		
		if (colnr>0)
		{
			edit(rownr, colnr);
		}
		else
		{
			selectRows(rownr, rownr);
		}
	}
	
	private void checkChanged(String[] before[] , String[] after[], int index[] )
	{
		if (field_changed) // Did we change anything: if so, add undo information
		{
			TransAction ta = new TransAction();
			ta.setChanged(before, after, index);
			addUndo(ta);
		}
	}
	
	private void setModified()
	{
		if (lsMod!=null)
		{
			Event e = new Event();
			e.widget=this;
			lsMod.modifyText(new ModifyEvent(e));
		}
	}

	private void insertRowBefore()
	{
		TableItem row   = cursor.getRow();
		if (row==null) return;
		int       rownr = table.indexOf(row);
		
		TableItem item = new TableItem(table, SWT.NONE, rownr);		
		item.setText(1, "field"+(table.getItemCount()-1));
		
		// Add undo information
		TransAction ta = new TransAction();
		String str[] = getItemText(item);
		ta.setNew(new String[][] { str }, new int[] { rownr });
		addUndo(ta);
		
		edit(rownr, 1);
	}

	private void insertRowAfter()
	{
		TableItem row   = cursor.getRow();
		if (row==null) return;
		int       rownr = table.indexOf(row);
		
		TableItem item = new TableItem(table, SWT.NONE, rownr+1);
		item.setText(1, "field"+(table.getItemCount()-1));

		// Add undo information
		TransAction ta = new TransAction();
		String str[] = getItemText(item);
		ta.setNew(new String[][] { str }, new int[] { rownr+1 });
		addUndo(ta);

		edit(rownr+1, 1);	
	}
	
	public void clearAll(boolean ask)
	{
		int id = SWT.YES;
		if (ask)
		{
			MessageBox mb = new MessageBox(parent.getShell(), SWT.YES | SWT.NO | SWT.ICON_QUESTION);
			mb.setMessage("Are you sure you want to clear this table?  No undo is possible!");
			mb.setText("Question");
			id = mb.open();
		}
		
		if (id==SWT.YES)
		{
			table.removeAll();
			new TableItem(table, SWT.NONE);
			edit(0,1);
		}
	}

	private void moveRowDown(boolean nextRow)
	{
		TableItem row   = cursor.getRow();
		if (row==null) return;
		int rownr = table.indexOf(row);

        int realRowNr = rownr-(nextRow?1:0);
        System.out.println("Move row #"+realRowNr+" down...");
        
		if (rownr<table.getItemCount())
		{
			moveRow(realRowNr, realRowNr+1);

			TransAction ta = new TransAction();
			ta.setItemMove(new int[] { realRowNr }, new int[] { realRowNr+1 } );
			addUndo(ta);
			
			setRowNums();
		}
	}
	
	private void moveRowUp(boolean edit)
	{
		TableItem row   = cursor.getRow();
		if( row==null) return;
		int rownr = table.indexOf(row);

		if (rownr>=0)
		{
			moveRow(rownr+1, rownr);

			TransAction ta = new TransAction();
			ta.setItemMove(new int[] { rownr+1 }, new int[] { rownr } );
			addUndo(ta);

			if (edit) edit(rownr, 1);	

			setRowNums();
		}
	}

	private void moveRow(int from, int to)
	{
		TableItem rowfrom = table.getItem(from);
		TableItem rowto   = table.getItem(to);
		
		// Grab the strings on that line...
		String strfrom[] = getItemText(rowfrom);
		String strto[]   = getItemText(rowto);
		
		// Copy the content
		for (int i=0;i<strfrom.length;i++)
		{
			rowfrom.setText(i+1, strto[i]);
			rowto.setText(i+1, strfrom[i]);
		}
		
		setModified();
	}

	private void copyToAll()
	{
		int       colnr = cursor.getColumn();
		TableItem row   = cursor.getRow();
		if (row==null) return;
		
		if (colnr==0) return;
		
		String str = row.getText(colnr);
		
		// Get undo information: all columns
		int size = table.getItemCount();
		
		String[] before[] = new String[size][];
		String[] after[]  = new String[size][];
		int      index[]  = new int[size];
		
		for (int i=0;i<table.getItemCount();i++)
		{
			TableItem item = table.getItem(i);
			
			index[i] = i;
			before[i] = getItemText(item);
			
			item.setText(colnr, str);
			
			after[i] = getItemText(item);
		}

		// Add the undo information!
		TransAction ta = new TransAction();
		ta.setChanged(before, after, index);
		addUndo(ta);
	}
	
	private void selectAll()
	{
		table.selectAll();
	}
	
	private void unselectAll()
	{
		table.deselectAll();
	}

	private void clipSelected()
	{
		if (clipboard!=null) 
		{
			clipboard.dispose();
			clipboard=null;
		}

		clipboard = new Clipboard(getDisplay());
		TextTransfer tran = TextTransfer.getInstance();

		String clip = getSelectedText();
		
		if (clip==null) return;
		
		clipboard.setContents(new String[] { clip }, new Transfer[] { tran });
	}

	private String getSelectedText()
	{
		String selection = "";

		for (int c=1;c<table.getColumnCount();c++)
		{
			TableColumn tc = table.getColumn(c);
			if (c>1) selection+=CLIPBOARD_DELIMITER;
			selection+=tc.getText();	
		}
		selection+=Const.CR;
		
		TableItem items[] = table.getSelection();
		if (items.length==0) return null;
				
		for (int r=0;r<items.length;r++)
		{
			TableItem ti = items[r];
			for (int c=1;c<table.getColumnCount();c++)
			{
				if (c>1) selection+=CLIPBOARD_DELIMITER;
				selection+=ti.getText(c);	
			}
			selection+=Const.CR;
		}
		return selection;
	}
	
	/*
			Example:	
			-----------------------------------------------------------------
			Field in stream;Dimension field
			TIME;TIME
			DATA_TYPE;DATA_TYPE
			MAP_TYPE;MAP_TYPE
			RESOLUTION;RESOLUTION
			START_TIME;START_TIME
			-----------------------------------------------------------------
 
		!! Paste at the end of the table!
		   --> Create new table item for every line
	 */
	 
	private int getCurrentRownr()
	{
		if (table.getItemCount()<=1) return 0;
		
		TableItem row   = cursor.getRow();
		if (row==null) return 0;
		int       rownr = table.indexOf(row);
		if (rownr<0) rownr=0;
		
		return rownr;		
	}
	
	private void pasteSelected()
	{
		int rownr = getCurrentRownr();

		if (clipboard!=null) 
		{
			clipboard.dispose();
			clipboard=null;
		}
		
		clipboard = new Clipboard(getDisplay());
		TextTransfer tran = TextTransfer.getInstance();

		String text = (String)clipboard.getContents(tran);
		
		if (text!=null)
		{
			String lines[] = convertTextToLines(text);
			if (lines.length>1)
			{
				// ALlocate complete paste grid!
				String[] grid[] = new String[lines.length-1][];
				int      idx[]  = new int[lines.length-1];
				
				for (int i=1;i<lines.length;i++)
				{
					grid[i-1] = convertLineToStrings(lines[i]);
					idx[i-1] = rownr+i;
					addItem(idx[i-1], grid[i-1]);
				}
				
				TransAction ta = new TransAction();
				ta.setNew(grid , idx);
				addUndo(ta);
			}
			if (rownr==0 && table.getItemCount()>rownr+1) // Empty row at rownr?  Remove it!
			{
				if (isEmpty(rownr, -1)) table.remove(rownr);
			}
			setRowNums();
			unEdit();
			
			setModified();
		}
	}
	
	private void addItem(int pos, String str[])
	{
		TableItem item = new TableItem(table, SWT.NONE, pos);
		for (int i=0;i<str.length;i++)
		{
			item.setText(i+1, str[i]);
		}
		setModified();
	}
	
	private String[] convertTextToLines(String text)
	{
		ArrayList strings = new ArrayList();

		int linenr=0;
		int pos =0;
		int start = 0;
		while (pos<text.length())
		{
			// Search for the end of the line: Const.CR
			while (pos<text.length() && !text.substring(pos).startsWith(Const.CR)) pos++;
			if (pos<text.length())
			{
				String line = text.substring(start, pos);
				strings.add(line);
				
				linenr++;
				pos+=Const.CR.length();
				start = pos;
			}
		}
		
		String retval[] = new String[strings.size()];
		for (int i=0;i<retval.length;i++) retval[i] = (String)strings.get(i);	
		
		return retval;
	}
	
	private String[] convertLineToStrings(String line)
	{
		ArrayList fields = new ArrayList();
		
		int pos2 = 0;
		int start2 = 0;
		int colnr  = 1;
		while (pos2<line.length())
		{
			// Search for the end of the field: "\t"
			while (pos2<line.length() && !line.substring(pos2).startsWith("\t")) pos2++;
			String field = line.substring(start2, pos2);
		
			fields.add(field);
				
			colnr++;
			pos2++;
			start2=pos2;
		}

		String retval[] = new String[fields.size()];
		for (int i=0;i<retval.length;i++) retval[i] = (String)fields.get(i);	
		
		return retval;
	}

	private void cutSelected()
	{
		clipSelected(); // copy selected lines to clipboard
		delSelected();
	}
	
	private void delSelected()
	{
		if (nrNonEmpty()==0) return;
		
		// Which items do we delete?
		int items[] = table.getSelectionIndices();
		
		if (items.length==0) return;

		// Save undo information
		String[] before[] = new String[items.length][];
		for (int i=0;i<items.length;i++)
		{
			TableItem ti = table.getItem(items[i]);
			before[i] = getItemText(ti);
		}
		
		TransAction ta = new TransAction();
		ta.setDelete(before, items);
		addUndo(ta);
		
		int rowbefore = table.indexOf(cursor.getRow());
		
		// Delete selected items.
		table.remove(items);
		
		if (table.getItemCount()==0)
		{
			TableItem item = new TableItem(table, SWT.NONE);
			// Save undo infomation!
			String stritem[] = getItemText(item); 
			ta = new TransAction();
			ta.setNew(new String[][] { stritem }, new int[] { 0 } );
			addUndo(ta);
		}

		// If the last row is gone, put the selection back on last-1!
		if (rowbefore >= table.getItemCount())
		{
			rowbefore = table.getItemCount()-1;
		}

		// After the delete, we put the cursor on the same row as before (if we can)
		if (rowbefore < table.getItemCount() && table.getItemCount()>0)
		{
			setPosition(rowbefore, 1);
			table.setSelection(rowbefore);
		}

		setRowNums();
		
		setModified();
	}

	private void keepSelected()
	{
		// Which items are selected?
		int sels[] = table.getSelectionIndices();
		
		int size = table.getItemCount();
		
		// Which items do we delete?
		int items[] = new int[size - sels.length];
		
		if (items.length==0) return;  // everything is selected: keep everything, do nothing.
		
		// Set the item-indices to delete...
		int nr=0;
		for (int i=0;i<table.getItemCount();i++)
		{
			boolean selected = false;
			for (int j=0;j<sels.length && !selected;j++) if (sels[j]==i) selected=true;
			if (!selected)
			{
				items[nr]=i;
				nr++;
			}
		}
		
		// Save undo information
		String[] before[] = new String[items.length][];
		for (int i=0;i<items.length;i++)
		{
			TableItem ti = table.getItem(items[i]);
			before[i] = getItemText(ti);
		}
			
		TransAction ta = new TransAction();
		ta.setDelete(before, items);
		addUndo(ta);
			
		// Delete selected items.
		table.remove(items);
			
		if (table.getItemCount()==0)
		{
			TableItem item = new TableItem(table, SWT.NONE);
			// Save undo infomation!
			String stritem[] = getItemText(item); 
			ta = new TransAction();
			ta.setNew(new String[][] { stritem }, new int[] { 0 } );
			addUndo(ta);
		}
			
		try
		{
			cursor.getRow();		
		}
		catch(Exception e) // Index is too high: lower to last available value
		{
			setPosition(table.getItemCount()-1, 1);
		}
		setRowNums();
		
		setModified();
	}
	
	private void setPosition(int rownr, int colnr)
	{
		if (rownr<table.getItemCount() && table.getItemCount()>0)
		{
			TableItem row   = table.getItem(rownr);
	
			cursor.setSelection(row, colnr);
			cursor.setFocus();
			cursor.setVisible(true);
			table.showItem(row);
			table.setSelection(new TableItem[] { row });
		}
	}

	private void edit(int rownr, int colnr)
	{
		edit(rownr, colnr, true, (char)0);
	}

	private void edit(int rownr, int colnr, boolean select_text, char extra)
	{
		TableItem row   = table.getItem(rownr);
		
		Control oldEditor = editor.getEditor();
		if (oldEditor != null) oldEditor.dispose();

		cursor.setSelection(row, colnr);
		cursor.setFocus();
		cursor.setVisible(true);
		table.showItem(row);
		table.setSelection(new TableItem[] { row });

		switch(columns[colnr-1].getType())
		{
		case ColumnInfo.COLUMN_TYPE_TEXT: editText(row, rownr, colnr, select_text, extra); break;
		case ColumnInfo.COLUMN_TYPE_CCOMBO: editCombo(row, rownr, colnr); break;
		case ColumnInfo.COLUMN_TYPE_BUTTON: editButton(row, rownr, colnr); break;
		default: break;
		}
	}
	
	private String[] getItemText(TableItem row)
	{
		String retval[] = new String[table.getColumnCount()-1];
		for (int i=0;i<retval.length;i++) retval[i] = row.getText(i+1);
		
		return retval;
	}
	
	private void editText(TableItem row, int rownr, int colnr, boolean select_text, char extra)
	{
		before_edit = getItemText(row);
		field_changed = false;
		
		if (columns[colnr-1].isReadOnly()) 
		{
			return;
		}
		
		text   = new Text(cursor, SWT.NONE );
		props.setLook(text, Props.WIDGET_STYLE_TABLE);
		text.addTraverseListener(lsTraverse);
		text.addFocusListener(lsFocusText);
		String content = row.getText(colnr) + (extra!=0?""+extra:"");
		text.setText(content); 
		if (lsMod!=null) text.addModifyListener(lsMod);
		text.addModifyListener(lsUndo);
		String tooltip = columns[colnr-1].getToolTip();
		if (tooltip!=null) text.setToolTipText(tooltip); else text.setToolTipText("");			
		text.setSelection(content.length());
		last_carret_position = content.length();
		text.addKeyListener(lsKeyText);
		
		// Make the column larger so we can still see the string we're entering...
		final int column_number = colnr;
		text.addModifyListener
		(
		    new ModifyListener() 
			{
				public void modifyText(ModifyEvent me) 
				{
					String str = text.getText();
					int strmax = dummy_gc.textExtent(str, SWT.DRAW_TAB | SWT.DRAW_DELIMITER).x+5;
					int colmax = tablecolumn[column_number].getWidth(); 
					if (strmax>colmax) tablecolumn[column_number].setWidth(strmax+20);
				}
			}
		);
				
		editor.horizontalAlignment = SWT.LEFT;
		editor.grabHorizontal = true;
		editor.grabVertical   = true;
		editor.minimumWidth   =   50;
		
		setRowNums();
		editor.layout();
		
		// Open the text editor in the correct column of the selected row.
		editor.setEditor(text);

		// position caret exactly on clicked position?
		/*
		if (lastclick!=null)
		{
			Rectangle tb  = text.getBounds();
			Composite par = text.getParent();
			Point p = par.getLocation();
			tb.x+=p.x;  // position is relative to parent TableCursor
			tb.y+=p.y;

			lastclick.x -= tb.x;
			lastclick.y -= tb.y;
			
			if (lastclick.x < tb.width && lastclick.y < tb.height)  // Obvious, just to make sure!
			{
				// System.out.println("Click = ("+lastclick.x+", "+lastclick.y+")");
				
				// There is no method to put the carret on the x-coordinate.
				// Lets find it out ourselves.
				String str = text.getText();
				int len = 0;
				int xlen = 0;
				while (xlen < lastclick.x && len<str.length())
				{
					String sub = str.substring(0, len);
					xlen = getTextLength( sub ) + 10; // Somehow, the text-box shifts to the left. 
					// System.out.println("String : ["+sub+"], xlen="+xlen);
					len++;					
				}
				if (len>0) len--;
				text.setSelection(len);
			}
		}
		*/
		if (select_text) text.selectAll();

		text.setFocus();
	}
	
	private void editCombo(TableItem row, int rownr, int colnr)
	{
	    if (columns[colnr-1].getSelectionAdapter()!=null)
	    {
			Event e = new Event();
			e.widget=this;
			e.x = colnr;
			e.y = rownr;
	        columns[colnr-1].getSelectionAdapter().widgetSelected(new SelectionEvent(e));
	        return;
	    }
	    

		before_edit = getItemText(row);
		field_changed = false;
		ColumnInfo colinfo = columns[colnr-1];

		combo   = new CCombo(cursor, colinfo.isReadOnly()?SWT.READ_ONLY:SWT.NONE );
        props.setLook(combo, Props.WIDGET_STYLE_TABLE);
		combo.addTraverseListener(lsTraverse);
		combo.addModifyListener(lsModCombo);
		combo.addFocusListener(lsFocusCombo);
		String opt[] = colinfo.getComboValues();

		for (int i=0;i<opt.length;i++) combo.add(opt[i]);
		combo.setText(row.getText(colnr));
		if (lsMod!=null) combo.addModifyListener(lsMod);	
		combo.addModifyListener(lsUndo);
		String tooltip = colinfo.getToolTip();
		if (tooltip!=null) combo.setToolTipText(tooltip); else combo.setToolTipText("");			
		combo.setVisible(true);		
		combo.addKeyListener(lsKeyCombo);

		int width = tablecolumn[colnr].getWidth();
		int height = 20;
		combo.setSize(width, height);
		combo.layout();
		editor.horizontalAlignment = SWT.LEFT;
		editor.verticalAlignment   = SWT.TOP;
		editor.minimumWidth        = width;
		editor.minimumHeight       = height;
		editor.layout();
		// Open the text editor in the correct column of the selected row.
		editor.setEditor (combo);
		combo.setFocus();
	}

	private void editButton(TableItem row, int rownr, int colnr)
	{
		before_edit = getItemText(row);
		field_changed = false;
				
		if (columns[colnr-1].isReadOnly()) 
		{
			return;
		}
		
		button = new Button(cursor, SWT.PUSH );
        props.setLook(button, Props.WIDGET_STYLE_TABLE);
		button.setText(columns[colnr-1].getButtonText());
		Image image = new Image(parent.getDisplay(), getClass().getResourceAsStream(Const.IMAGE_DIRECTORY+"edittext.png"));
		button.setImage(image);
	
		SelectionAdapter selAdpt = columns[colnr-1].getSelectionAdapter();
		if (selAdpt!=null) button.addSelectionListener(selAdpt);
	
		button_rownr = rownr;		
		button_colnr = colnr;
			
		//button.addTraverseListener(lsTraverse);
		button_content = row.getText(colnr); 
	
		String tooltip = columns[colnr-1].getToolTip();
		if (tooltip!=null) button.setToolTipText(tooltip); else button.setToolTipText("");			
		button.addTraverseListener(lsTraverse); // hop to next field
		button.addTraverseListener(new TraverseListener() 
			{
				public void keyTraversed(TraverseEvent arg0) 
				{
					closeActiveButton();
				}
			}
		);
		
		editor.horizontalAlignment = SWT.LEFT;
		editor.verticalAlignment   = SWT.TOP;
		editor.grabHorizontal      = false;
		editor.grabVertical        = false;
	
		Point size = button.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		editor.minimumWidth = size.x;
		editor.minimumHeight = size.y-2;			
			
		// setRowNums();
		editor.layout();
				
		// Open the text editor in the correct column of the selected row.
		editor.setEditor(button);
		
		button.setFocus();
			
		// if the button loses focus, destroy it...
		/*
		button.addFocusListener(new FocusAdapter() 
			{
				public void focusLost(FocusEvent e) 
				{
					button.dispose();
				}
			}
		);
		*/
	}
	
	public void setRowNums()
	{
		int i;
		for (i=0;i<table.getItemCount();i++)
		{
			TableItem item = table.getItem(i);
			String num=""+(i+1);
			for(int j=num.length();j<3;j++) num="0"+num;
			item.setText(0, num);
		}
	}
	
	public void optWidth(boolean header)
	{
		for (int c=0;c<table.getColumnCount();c++)
		{
			TableColumn tc = table.getColumn(c);
			int max=0;
			if (header) 
			{
				max=dummy_gc.textExtent(tc.getText(), SWT.DRAW_TAB | SWT.DRAW_DELIMITER).x;
			} 
				
			for (int r=0;r<table.getItemCount();r++)
			{
				TableItem ti = table.getItem(r);
				String str = "";
				if (c>0)
				{
					switch(columns[c-1].getType())
					{
					case ColumnInfo.COLUMN_TYPE_TEXT:
					    str = ti.getText(c);
						break;
					case ColumnInfo.COLUMN_TYPE_CCOMBO:
					    str = ti.getText(c);
					    int minLength = str.length();
						String[] options = columns[c-1].getComboValues();
						if (options!=null)
						{
							for (int x=0;x<options.length;x++)
							{
							    if (options[x].length()>minLength)
							    {
							        str = options[x];
							        minLength = options[x].length();
							    }
							}
						}
						break;
					case ColumnInfo.COLUMN_TYPE_BUTTON:
					    str = columns[c-1].getButtonText();
						break;
					default:
					    break;
					}
				}
				else
				{
				    str=str = ti.getText(c);
				}
				if (str==null) str="";
				int len = dummy_gc.textExtent(str, SWT.DRAW_TAB | SWT.DRAW_DELIMITER).x;
				if (len>max) max=len;
			}
			try
			{
				tc.setWidth(max+12);
			}
			catch(Exception e) {}
		}
		unEdit();
	}

	
	/* Remove empty rows in the table...
	 * 
	 */
	public void removeEmptyRows() 
	{ 
		removeEmptyRows(-1);
	}
	
	private boolean isEmpty(int rownr, int colnr)
	{
		boolean empty=false;
		TableItem item = table.getItem(rownr);
		if (colnr>=0) 
		{
			String str = item.getText(colnr);
			if (str==null || str.length()==0) empty=true;
		}
		else
		{
			empty=true;
			for (int j=1;j<table.getColumnCount();j++)
			{
				String str = item.getText(j);
				if (str!=null && str.length()>0) empty=false;
			}
		} 
		return empty;
	}
	
	public void removeEmptyRows(int column)
	{
		// Remove "empty" table items, where item.getText(1) is empty, length==0

		for (int i=table.getItemCount()-1;i>=0;i--)
		{
			if (isEmpty(i, column)) table.remove(i);
		}
		if (table.getItemCount()==0) // At least one empty row!
		{
			new TableItem(table, SWT.NONE);
		}
	}
	
	/* Count non-empty rows in the table...
	 * 
	 */
	public int nrNonEmpty()
	{
		int retval=0;
		
		// Count only non-empty rows
		for (int i=0;i<table.getItemCount();i++)
		{
			if (!isEmpty(i,-1)) retval++;
		}
		
		return retval;
	}

	public TableItem getNonEmpty(int selnr)
	{
		int nr=0;
		
		// Count only non-empty rows
		for (int i=0;i<table.getItemCount();i++)
		{
			if (!isEmpty(i,-1)) 
			{ 
				if (selnr==nr) return table.getItem(i);
				nr++;
			}
		}
		
		return null;
	}
	
	public int indexOfString(String str, int column)
	{
		for (int i=0;i<nrNonEmpty();i++)
		{
			String cmp = getNonEmpty(i).getText(column);
			if (str.equalsIgnoreCase(cmp)) return i;
		}
		return -1;
	}
	
	public ScrollBar getHorizontalBar()
	{
		return table.getHorizontalBar();
	}

	public ScrollBar getVerticalBar()
	{
		return table.getVerticalBar();
    }


	/** Sort the entire vector, if it is not empty
	 */
	private synchronized void quickSort(Vector elements)
	{
		if (! elements.isEmpty())
		{ 
			this.quickSort(elements, 0, elements.size()-1);
		}
	}


	/**
	 * QuickSort.java by Henk Jan Nootenboom, 9 Sep 2002
	 * Copyright 2002-2003 SUMit. All Rights Reserved.
	 *
	 * Algorithm designed by prof C. A. R. Hoare, 1962
	 * See http://www.sum-it.nl/en200236.html
	 * for algorithm improvement by Henk Jan Nootenboom, 2002.
	 *
	 * Recursive Quicksort, sorts (part of) a Vector by
	 *  1.  Choose a pivot, an element used for comparison
	 *  2.  dividing into two parts:
	 *      - less than-equal pivot
	 *      - and greater than-equal to pivot.
	 *      A element that is equal to the pivot may end up in any part.
	 *      See www.sum-it.nl/en200236.html for the theory behind this.
	 *  3. Sort the parts recursively until there is only one element left.
	 *
	 * www.sum-it.nl/QuickSort.java this source code
	 * www.sum-it.nl/quicksort.php3 demo of this quicksort in a java applet
	 *
	 * Permission to use, copy, modify, and distribute this java source code
	 * and its documentation for NON-COMMERCIAL or COMMERCIAL purposes and
	 * without fee is hereby granted.
	 * See http://www.sum-it.nl/security/index.html for copyright laws.
	 */
	  private synchronized void quickSort(Vector elements, int lowIndex, int highIndex)
	  { 
		int lowToHighIndex;
		int highToLowIndex;
		int pivotIndex;
		Row pivotValue;  // values are Strings in this demo, change to suit your application
		Row lowToHighValue;
		Row highToLowValue;
		Row parking;
		int newLowIndex;
		int newHighIndex;
		int compareResult;

		lowToHighIndex = lowIndex;
		highToLowIndex = highIndex;
		/** Choose a pivot, remember it's value
		 *  No special action for the pivot element itself.
		 *  It will be treated just like any other element.
		 */
		pivotIndex = (lowToHighIndex + highToLowIndex) / 2;
		pivotValue = (Row)elements.elementAt(pivotIndex);

		/** Split the Vector in two parts.
		 *
		 *  The lower part will be lowIndex - newHighIndex,
		 *  containing elements <= pivot Value
		 *
		 *  The higher part will be newLowIndex - highIndex,
		 *  containting elements >= pivot Value
		 * 
		 */
		newLowIndex = highIndex + 1;
		newHighIndex = lowIndex - 1;
		// loop until low meets high
		while ((newHighIndex + 1) < newLowIndex) // loop until partition complete
		{ // loop from low to high to find a candidate for swapping
		  lowToHighValue = (Row)elements.elementAt(lowToHighIndex);
		  while (lowToHighIndex < newLowIndex
			& lowToHighValue.compare(pivotValue, sortfield+1, sort_desc)<0 )
		  { 
			newHighIndex = lowToHighIndex; // add element to lower part
			lowToHighIndex ++;
			lowToHighValue = (Row)elements.elementAt(lowToHighIndex);
		  }

		  // loop from high to low find other candidate for swapping
		  highToLowValue = (Row)elements.elementAt(highToLowIndex);
		  while (newHighIndex <= highToLowIndex
			& (highToLowValue.compare(pivotValue, sortfield+1, sort_desc)>0)
			)
		  { 
			newLowIndex = highToLowIndex; // add element to higher part
			highToLowIndex --;
			highToLowValue = (Row)elements.elementAt(highToLowIndex);
		  }

		  // swap if needed
		  if (lowToHighIndex == highToLowIndex) // one last element, may go in either part
		  { 
			newHighIndex = lowToHighIndex; // move element arbitrary to lower part
		  }
		  else if (lowToHighIndex < highToLowIndex) // not last element yet
		  { 
			compareResult = lowToHighValue.compare(highToLowValue, sortfield+1, sort_desc);
			if (compareResult >= 0) // low >= high, swap, even if equal
			{ 
			  parking = lowToHighValue;
			  elements.setElementAt(highToLowValue, lowToHighIndex);
			  elements.setElementAt(parking, highToLowIndex);

			  newLowIndex = highToLowIndex;
			  newHighIndex = lowToHighIndex;

			  lowToHighIndex ++;
			  highToLowIndex --;
			}
		  }
		}

		// Continue recursion for parts that have more than one element
		if (lowIndex < newHighIndex)
		{ 
			this.quickSort(elements, lowIndex, newHighIndex); // sort lower subpart
		}
		if (newLowIndex < highIndex)
		{ 
			this.quickSort(elements, newLowIndex, highIndex); // sort higher subpart
		}
	  }
	  
	private void addUndo(TransAction ta)
	{
		while (undo.size()>undo_position+1 && undo.size()>0)
		{
			int last = undo.size()-1;
			undo.remove(last);
		}

		undo.add(ta);
		undo_position++;
	  	
		while (undo.size()>props.getMaxUndo())
		{
			undo.remove(0);
			undo_position--;
		}

		setUndoMenu();
	}

	private void undoAction()
	{
		TransAction ta = previousUndo();
		if (ta==null) return;
		
		// Get the current cursor position
		int rownr = getCurrentRownr();

		setUndoMenu(); // something changed: change the menu
		switch(ta.getType())
		{
			//
			// NEW
			//

			// We created a table item: undo this...
			case TransAction.TYPE_ACTION_NEW_TABLEITEM:
				// Delete the step at correct location:
				{
					int idx[] = ta.getCurrentIndex();
					table.remove(idx);

					for (int i=0;i<idx.length;i++) if (idx[i]<rownr) rownr--; // shift with the rest.

					// See if the table is empty, if so : undo again!!
					if (table.getItemCount()==0) undoAction();
					setRowNums();
				}
				break;
	

			//
			// DELETE
			//

			// un-Delete the rows at correct location: re-insert
			case TransAction.TYPE_ACTION_DELETE_TABLEITEM:
				{
					int idx[] = ta.getCurrentIndex();
					String[] str[] = (String[][])ta.getCurrent();
					for (int i=0;i<idx.length;i++)
					{
						addItem(idx[i], str[i]);
						
						if (idx[i]<=rownr) rownr++;
					}
					setRowNums();
				}
				break;

			//
			// CHANGE
			//

			// Change the item back to the original row-value.
			case TransAction.TYPE_ACTION_CHANGE_TABLEITEM:
				{
					int idx[] = ta.getCurrentIndex();
					String[] prev[] = (String[][])ta.getPrevious();
					for (int x=0;x<idx.length;x++)
					{
						TableItem item = table.getItem(idx[x]);
						for (int i=0;i<prev[x].length;i++) item.setText(i+1, prev[x][i]);
					}
				}
				break;

			//
			// POSITION
			//
			// The position of a row has changed...
			case TransAction.TYPE_ACTION_POSITION_TABLEITEM:
				{
					int curr[] = ta.getCurrentIndex();
					int prev[] = ta.getPreviousIndex();
					for (int i=0;i<curr.length;i++)
					{
						moveRow(prev[i], curr[i]);
					}
					setRowNums();
				}
				break;	
			default: break;
		}

		if (rownr>=table.getItemCount()) rownr=table.getItemCount()-1;
		if (rownr<0) rownr=0;
		
		cursor.setSelection(rownr, 0);
		selectRows(rownr, rownr);
	}
	
	private void redoAction()
	{
		TransAction ta = nextUndo();
		if (ta==null) return;

		// Get the current cursor position
		int rownr = getCurrentRownr();

		setUndoMenu(); // something changed: change the menu
		switch(ta.getType())
		{
		//
		// NEW
		//
		case TransAction.TYPE_ACTION_NEW_TABLEITEM:
			// re-insert the step at correct location:
			{
				int idx[] = ta.getCurrentIndex();
				String[] str[] = (String[][])ta.getCurrent();
				for (int i=0;i<idx.length;i++)
				{
					addItem(idx[i], str[i]);
					if (idx[i]<=rownr) rownr++; // Shift cursor position with the new items...
				}

				setRowNums();
			}
			break;
		
		//	
		// DELETE
		//
		case TransAction.TYPE_ACTION_DELETE_TABLEITEM:
			// re-remove the items at the correct locations:
			{
				int idx[] = ta.getCurrentIndex();
				table.remove(idx);

				for (int i=0;i<idx.length;i++) if (idx[i]<rownr) rownr--; // shift with the rest.

				// See if the table is empty, if so : undo again!!
				if (table.getItemCount()==0) undoAction();
				setRowNums();
			}
			break;


		//
		// CHANGE
		//

		case TransAction.TYPE_ACTION_CHANGE_TABLEITEM:
			// Re-apply changes to the item
			{
				int idx[] = ta.getCurrentIndex();
				String[] curr[] = (String[][])ta.getCurrent();
				for (int x=0;x<idx.length;x++)
				{
					TableItem item = table.getItem(idx[x]);
					for (int i=0;i<curr[x].length;i++) item.setText(i+1, curr[x][i]);
				}
			}
			break;

		//
		// CHANGE POSITION
		//
		case TransAction.TYPE_ACTION_POSITION_TABLEITEM:
			{
				int curr[] = ta.getCurrentIndex();
				int prev[] = ta.getPreviousIndex();
				for (int i=0;i<curr.length;i++)
				{
					moveRow(curr[i], prev[i]);
				}
				setRowNums();
			}
			break;
			
		default: break;
		}

		if (rownr>=table.getItemCount()) rownr=table.getItemCount()-1;
		if (rownr<0) rownr=0;
		
		cursor.setSelection(rownr, 0);
		selectRows(rownr, rownr);
	}
	
	private void setUndoMenu()
	{
		TransAction prev = viewPreviousUndo();
		TransAction next = viewNextUndo();
		
		if (prev!=null) 
		{
			miEditUndo.setEnabled(true);
			miEditUndo.setText("Undo : "+prev.toString()+" \tCTRL-Z");
		} 
		else            
		{
			miEditUndo.setEnabled(false);
			miEditUndo.setText("Undo : not available \tCTRL-Z");
		} 

		if (next!=null) 
		{
			miEditRedo.setEnabled(true);
			miEditRedo.setText("Redo : "+next.toString()+" \tCTRL-Y");
		} 
		else            
		{
			miEditRedo.setEnabled(false);
			miEditRedo.setText("Redo : not available \tCTRL-Y");
		} 

	}


	

	// get previous undo, change position
	private TransAction previousUndo()
	{
		if (undo.size()==0 || undo_position<0) return null;  // No undo left!
		
		TransAction retval = (TransAction)undo.get(undo_position);

		undo_position--;
		
		return retval;
	}

	// View previous undo, don't change position
	private TransAction viewPreviousUndo()
	{
		if (undo.size()==0 || undo_position<0) return null;  // No undo left!
		
		TransAction retval = (TransAction)undo.get(undo_position);
		
		return retval;
	}

	private TransAction nextUndo()
	{
		int size=undo.size();
		if (size==0 || undo_position>=size-1) return null; // no redo left...
		
		undo_position++;
				
		TransAction retval = (TransAction)undo.get(undo_position);
	
		return retval;
	}

	private TransAction viewNextUndo()
	{
		int size=undo.size();
		if (size==0 || undo_position>=size-1) return null; // no redo left...
		
		TransAction retval = (TransAction)undo.get(undo_position+1);
	
		return retval;
	}
	
	private void clearUndo()
	{
		undo = new ArrayList();
		undo_position = -1;
	}


	private Point getButtonPosition()
	{
		return new Point(button_colnr, button_rownr);
	}
		
	public String getButtonString()
	{
		return button_content;
	}
	
	public void setButtonString(String str)
	{
		Point p = getButtonPosition();
		TableItem item = table.getItem(p.y);
		item.setText(p.x, str);
	}
		
	public void closeActiveButton()
	{
		if (button!=null && !button.isDisposed()) button.dispose();
	}

	public void unEdit()
	{
		if (text!=null && !text.isDisposed()) text.dispose();
		if (combo!=null && !combo.isDisposed()) combo.dispose();
	}
	
	
	// Filtering...
	
	
	public void setFilter()
	{
		if (condition==null) condition=new Condition();
		Row f = getRowWithoutValues();
		EnterConditionDialog ecd = new EnterConditionDialog(parent.getShell(), props, SWT.NONE, f, condition);
		Condition cond = ecd.open(); 
		if (cond!=null)
		{
			ArrayList tokeep = new ArrayList();
			
			// Apply the condition to the TableView...
			int nr = table.getItemCount();
			for (int i=nr-1;i>=0;i--)
			{
				Row r = getRow(i);
				boolean keep = cond.evaluate(r);
				if (keep)
				{
					tokeep.add( new Integer(i) );
				}
			}

			int sels[] = new int[tokeep.size()];
			for (int i=0;i<sels.length;i++) sels[i] = ((Integer)tokeep.get(i)).intValue();
			
			table.setSelection(sels);
		}
	}
	
	public Row getRowWithoutValues()
	{
		Row f = new Row();
		for (int i=0;i<columns.length;i++)
		{
			f.addValue(new Value(columns[i].getName(), Value.VALUE_TYPE_STRING));
		}
		f.addValue(0, new Value("#", Value.VALUE_TYPE_INTEGER));
		return f;
	}
	
	public Row getRow(int nr)
	{
		TableItem ti = table.getItem(nr);
		Row r = getRowWithoutValues();
		
		for (int i=1;i<r.size();i++)
		{
			String str = ti.getText(i);
			r.getValue(i).setValue(str);
		}
		
		Value nrval = r.getValue(0); // #
		nrval.setValue(nr);
		
		return r;
	}
	
	public int[] getSelectionIndices()
	{
		return table.getSelectionIndices();
	}
	
	public int getSelectionIndex()
	{
		return table.getSelectionIndex();
	}
	
	public void remove(int index)
	{
		table.remove(index);
		if (table.getItemCount()==0) new TableItem(table, SWT.NONE);
	}
	
	public void remove(int index[])
	{
		table.remove(index);
		if (table.getItemCount()==0) new TableItem(table, SWT.NONE);
	}

	
	public String getItem(int rownr, int colnr)
	{
		TableItem item = table.getItem(rownr);
		if (item!=null)
		{
			return item.getText(colnr);
		}
		else
		{
			return null;
		}
	}
	
	public void add(String string[])
	{
		TableItem item = new TableItem(table, SWT.NONE);
		for (int i=0;i<string.length && i+1<table.getColumnCount();i++)
		{
			if (string[i]!=null) 
			{
				item.setText(i+1, string[i]);
			}
		}
	}
	
	public String[] getItem(int rownr)
	{
		TableItem item = table.getItem(rownr);
		if (item!=null)
		{
			return getItemText(item);
		}
		else
		{
			return null;
		}
	}
	
    /**
     * Get all the strings from a certain column as an array
     * @param colnr The column to return
     * @return the column values as a string array.
     */
	public String[] getItems(int colnr)
	{
		String retval[] = new String[table.getItemCount()];
		for (int i=0;i<retval.length;i++)
		{
			TableItem item = table.getItem(i);
			retval[i]=item.getText(colnr+1);
		}
		return retval;
	}
	
	public void removeAll()
	{
		table.removeAll();
		if (table.getItemCount()==0) new TableItem(table, SWT.NONE);
	}
	
	public int getItemCount()
	{
		return table.getItemCount();
	}
	
	public void setText(String text, int colnr, int rownr)
	{
	    TableItem item = table.getItem(rownr);
	    item.setText(colnr, text);
	}
	
};
