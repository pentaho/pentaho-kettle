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
 * Created on 18-mei-2003
 *
 */

package be.ibridge.kettle.schema.dialog;
import java.util.StringTokenizer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.Props;
import be.ibridge.kettle.core.WindowProperty;
import be.ibridge.kettle.schema.SchemaMeta;
import be.ibridge.kettle.schema.TableField;
import be.ibridge.kettle.schema.TableMeta;
import be.ibridge.kettle.schema.WhereCondition;


public class SelectFieldDialog extends Dialog 
{
	private LogWriter log;
	private SchemaMeta schema;
	
	private static final String STRING_FIELDS     = "Fields";
	private static final String STRING_CONDITIONS = "Conditions";
	
	private static final String STRING_FILTER_EXT[]   = new String[] { "*.cub", "*.*" };
	private static final String STRING_FILTER_NAMES[] = new String[] { "Kettle Cube files", "All files" };

	private Shell     shell;
	private Tree      wTree;
	private Button    wOK;
	private Button    wCancel;
	
	private Label     wlCube;
	private Text      wCube;
	private Button    wbCube;

	private Label     wlList, wlCondition;
	private List      wList, wCondition;
	
	private boolean   retval;
	public  TableField     fields[];
	public  WhereCondition conditions[];
    
    private Props props;
		
	public SelectFieldDialog(Shell par, int style, LogWriter l, SchemaMeta sch)
	{
		super(par, style);
		schema=sch;
		log=l;
		
		retval=false;
        props=Props.getInstance();
	}

	public boolean open() 
	{
		Shell parent = getParent();
		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN);
 		props.setLook(shell);
		shell.setText("Field selection screen");
		
		shell.setLayout(new FormLayout());
		
		int middle = schema.props.getMiddlePct();
		int margin = Const.MARGIN;
		

		//////////////////////////////////////////
		// THE CUBE FILENAME
		//////////////////////////////////////////
		
		wlCube = new Label(shell, SWT.NONE);
		wlCube.setText("Cube filename");
 		props.setLook(wlCube);

		FormData fdlCube  = new FormData(); 
		fdlCube.left   = new FormAttachment(0, 0); // To the right of the label
		fdlCube.right  = new FormAttachment(middle, 0);
		fdlCube.top    = new FormAttachment(0, 0);
		wlCube.setLayoutData(fdlCube);
		
		wbCube = new Button(shell, SWT.PUSH );
		wbCube.setText("File...");
 		props.setLook(wbCube);

		FormData fdbCube  = new FormData(); 
		fdbCube.right  = new FormAttachment(100, 0);
		fdbCube.top    = new FormAttachment(0, 0);
		wbCube.setLayoutData(fdbCube);
		
		wCube = new Text(shell, SWT.SINGLE | SWT.BORDER);
 		props.setLook(wCube);

		FormData fdCube  = new FormData(); 
		fdCube.left   = new FormAttachment(middle, margin); // To the right of the label
		fdCube.top    = new FormAttachment(0, 0);
		fdCube.right  = new FormAttachment(wbCube, 0);
		wCube.setLayoutData(fdCube);
		
		wbCube.addSelectionListener(new SelectionAdapter() 
			{
				public void widgetSelected(SelectionEvent arg0) 
				{
					FileDialog dialog = new FileDialog(shell, SWT.OPEN);
					dialog.setFilterPath("C:\\Projects\\kettle\\source\\");
					dialog.setFilterExtensions(STRING_FILTER_EXT);
					dialog.setFilterNames(STRING_FILTER_NAMES);
					String fname = dialog.open();
					if (fname!=null) 
					{
						wCube.setText(fname);
					}
				}
			}
		);
	
		
		////////////////////////////////////////////////////
		// Sashform
		////////////////////////////////////////////////////
		
		
		SashForm sashform = new SashForm(shell, SWT.HORIZONTAL); 
		sashform.setLayout(new FillLayout());
 		props.setLook(sashform);
		
		Composite leftsplit = new Composite(sashform, SWT.NONE);
 		props.setLook(leftsplit);
		
		FormLayout formLayout = new FormLayout ();
		formLayout.marginWidth  = margin;
		formLayout.marginHeight = margin;		
		leftsplit.setLayout (formLayout);
 		
 		// Tree
 		wTree = new Tree(leftsplit, SWT.MULTI | SWT.BORDER );
 		props.setLook( 		wTree);
 		
 		// TreeItem tiTree = new TreeItem(wTree, SWT.NONE); tiTree.setText("Schema");
 		
 		// List all tables and fields...
		// The catalogs...				
		TreeItem tiFld = new TreeItem(wTree, SWT.NONE); tiFld.setText(STRING_FIELDS);
		TreeItem tiCon = new TreeItem(wTree, SWT.NONE); tiCon.setText(STRING_CONDITIONS);

		FormData fdSash  = new FormData(); 
		fdSash.left   = new FormAttachment(0, 0); // To the right of the label
		fdSash.top    = new FormAttachment(wCube, margin);
		fdSash.right  = new FormAttachment(100, 0);
		fdSash.bottom = new FormAttachment(100, 0);
		sashform.setLayoutData(fdSash);

		// Fill in the field info...
		for (int i=0;i<schema.nrTables();i++)
		{
			TableMeta table = schema.getTable(i);
			
			String tablename = table.getName();
			TreeItem newTab = new TreeItem(tiFld, SWT.NONE); newTab.setText(tablename);
			
			for (int j = 0; j < table.nrFields(); j++)
			{
				TableField f = table.getField(j);
				if (!f.isHidden())
				{
					String fieldname = f.getName();
					
					TreeItem ti = new TreeItem(newTab, SWT.NONE); ti.setText(fieldname);
				}
			}
 		}
 		
 		// Fill in the conditions information
		for (int i=0;i<schema.nrTables();i++)
		{
			TableMeta table = schema.getTable(i);
			
			String tablename = table.getName();
			TreeItem newCon = new TreeItem(tiCon, SWT.NONE); newCon.setText(tablename);
			
			for (int j = 0; j < table.nrConditions(); j++)
			{
				WhereCondition c = table.getCondition(j);
				String name = c.getName();
				TreeItem ti = new TreeItem(newCon, SWT.NONE); ti.setText(name);
			}
		}
 
		//tiTree.setExpanded(true);
		tiFld.setExpanded(true);
		tiCon.setExpanded(true);
 		
 		// Buttons
		wOK = new Button(leftsplit, SWT.PUSH); 
		wOK.setText("  &OK  ");
		
		wCancel = new Button(leftsplit, SWT.PUSH);
		wCancel.setText("  &Cancel  ");
		
		FormData fdTree      = new FormData(); 
		FormData fdOK        = new FormData();
		FormData fdCancel    = new FormData();

		fdTree.left   = new FormAttachment(0, 0); // To the right of the label
		fdTree.top    = new FormAttachment(0, 0);
		fdTree.right  = new FormAttachment(100, 0);
		fdTree.bottom = new FormAttachment(100, -50);
		wTree.setLayoutData(fdTree);

		fdOK.left    = new FormAttachment(wTree, 30, SWT.CENTER); 
		fdOK.bottom  = new FormAttachment(100, 0);
		wOK.setLayoutData(fdOK);

		fdCancel.left = new FormAttachment(wOK, 10); 
		fdCancel.bottom  = new FormAttachment(100, 0);
		wCancel.setLayoutData(fdCancel);
	
		// Add listeners
		wCancel.addListener(SWT.Selection, new Listener ()
			{
				public void handleEvent (Event e) 
				{
					log.logDebug(this.getClass().getName(), "CANCEL SelectFieldsDialog");
					dispose();
				}
			}
		);

		// Add listeners
		wOK.addListener(SWT.Selection, new Listener ()
			{
				public void handleEvent (Event e) 
				{
					handleOK();
				}
			}
		);
		
		
		
		Composite compmiddle = new Composite(sashform, SWT.NONE);
 		props.setLook(compmiddle);
		
		FormLayout middleLayout = new FormLayout ();
		middleLayout.marginWidth  = margin;
		middleLayout.marginHeight = margin;		
		compmiddle.setLayout (middleLayout);

		Button wAdd = new Button(compmiddle, SWT.PUSH);
		wAdd.setText(" > ");

		Button wRemove = new Button(compmiddle, SWT.PUSH);
		wRemove.setText(" < ");

		FormData fdAdd = new FormData();
		fdAdd.left   = new FormAttachment(0, 0); // To the right of the label
		fdAdd.top    = new FormAttachment(40, 0);
		fdAdd.right  = new FormAttachment(100, 0);
		wAdd.setLayoutData(fdAdd);

		FormData fdRemove = new FormData();
		fdRemove.left   = new FormAttachment(0, 0); // To the right of the label
		fdRemove.top    = new FormAttachment(wAdd, margin*2);
		fdRemove.right  = new FormAttachment(100, 0);
		wRemove.setLayoutData(fdRemove);
		
		Composite rightsplit = new Composite(sashform, SWT.NONE);
 		props.setLook(rightsplit);
		
		FormLayout rightLayout = new FormLayout ();
		rightLayout.marginWidth  = margin;
		rightLayout.marginHeight = margin;		
		rightsplit.setLayout (rightLayout);

		wlList = new Label(rightsplit, SWT.LEFT);
		wlList.setText("Selected fields: ");
 		props.setLook(wlList);
		
		
		wList = new List(rightsplit, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
 		props.setLook(wList);

		wlCondition = new Label(rightsplit, SWT.LEFT);
		wlCondition.setText("Selected conditions: ");
 		props.setLook(wlCondition);

		wCondition = new List(rightsplit, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
 		props.setLook(wCondition);

		FormData fdlList = new FormData();
		fdlList.left   = new FormAttachment(0, 0);
		fdlList.top    = new FormAttachment(0, margin);
		wlList.setLayoutData(fdlList);

		FormData fdList = new FormData();
		fdList.left   = new FormAttachment(0, 0); 
		fdList.top    = new FormAttachment(wlList, 0);
		fdList.right  = new FormAttachment(100, 0);
		fdList.bottom = new FormAttachment(75, 0);
		wList.setLayoutData(fdList);

		FormData fdlCondition = new FormData();
		fdlCondition.left   = new FormAttachment(0, 0);
		fdlCondition.top    = new FormAttachment(wList, margin*2);
		wlCondition.setLayoutData(fdlCondition);

		FormData fdCondition = new FormData();
		fdCondition.left   = new FormAttachment(0, 0); // To the right of the label
		fdCondition.top    = new FormAttachment(wlCondition, 0);
		fdCondition.right  = new FormAttachment(100, 0);
		fdCondition.bottom = new FormAttachment(100, 0);
		wCondition.setLayoutData(fdCondition);
		 		
		sashform.setWeights(new int[] { 46, 8, 46 });
		
		// Drag & Drop for steps
		Transfer[] ttypes = new Transfer[] {TextTransfer.getInstance() };
		
		DragSource ddSource = new DragSource(wTree, DND.DROP_MOVE | DND.DROP_COPY);
		ddSource.setTransfer(ttypes);
		ddSource.addDragListener(new DragSourceListener() 
			{
				public void dragStart(DragSourceEvent event){ }
	
				public void dragSetData(DragSourceEvent event) 
				{
					TreeItem ti[] = wTree.getSelection();
					String data = new String();
					for (int i=0;i<ti.length;i++) 
					{
						String itemname = ti[i].getText();
						TreeItem parent = ti[i].getParentItem();
						if (parent!=null)
						{
							String parentname = parent.getText();
							TreeItem grandparent = parent.getParentItem();
							if (grandparent!=null)
							{
								String grandparentname = grandparent.getText();
								
								System.out.println("Grandparent = "+grandparentname+", parent="+parentname+", item="+itemname);
								if (grandparentname.equalsIgnoreCase(STRING_FIELDS) ||
								    grandparentname.equalsIgnoreCase(STRING_CONDITIONS))
								{
									data+=STRING_FIELDS+"\t"+parentname+"\t"+itemname+Const.CR;
								}
							}
						}
					} 
					event.data = data;
				}
	
				public void dragFinished(DragSourceEvent event) {}
			}
		);
		DropTarget ddTarget = new DropTarget(wList, DND.DROP_MOVE | DND.DROP_COPY);
		ddTarget.setTransfer(ttypes);
		ddTarget.addDropListener(new DropTargetListener() 
		{
			public void dragEnter(DropTargetEvent event) { }
			public void dragLeave(DropTargetEvent event) { }
			public void dragOperationChanged(DropTargetEvent event) { }
			public void dragOver(DropTargetEvent event) { }
			public void drop(DropTargetEvent event) 
			{
				if (event.data == null) { // no data to copy, indicate failure in event.detail
					event.detail = DND.DROP_NONE;
					return;
				}
				StringTokenizer strtok = new StringTokenizer((String)event.data, Const.CR);
				while (strtok.hasMoreTokens())
				{
					String   source = strtok.nextToken();
					int idx  = source.indexOf("\t");
					int idx2 = source.indexOf("\t", idx+1); 
					if (idx>=0)
					{
						String fieldtype = source.substring(0, idx);
						String tablename = source.substring(idx+1, idx2);
						String fieldname = source.substring(idx2+1);
						if (fieldtype.equalsIgnoreCase(STRING_FIELDS))
						{
							addToFieldsList(tablename, fieldname);
						}
					}
				}
			}

			public void dropAccept(DropTargetEvent event) 
			{
			}
		});
		
		// Double click adds to List.
		wTree.addSelectionListener(new SelectionAdapter()
			{
				public void widgetDefaultSelected(SelectionEvent e)
				{
					addSelectedToList();					
				}
			}
		);
		wAdd.addSelectionListener(new SelectionAdapter()
			{
				public void widgetSelected(SelectionEvent e)
				{
					addSelectedToList();					
				}
			}
		);
		
		wList.addKeyListener(new KeyAdapter() 
			{
				public void keyPressed(KeyEvent e) 
				{
					// Delete the selected items, last to first
					if (e.character == SWT.DEL)
					{
						delSelectedFields();
					}
				}
			}
		);

		wCondition.addKeyListener(new KeyAdapter() 
			{
				public void keyPressed(KeyEvent e) 
				{
					// Delete the selected items, last to first
					if (e.character == SWT.DEL)
					{
						delSelectedConditions();
					}
				}
			}
		);
		
		wRemove.addSelectionListener(new SelectionAdapter() 
			{
				public void widgetSelected(SelectionEvent e)
				{
					delSelectedFields();					
					delSelectedConditions();					
				}
			}
		);
		
		getData();

		WindowProperty winprop = schema.props.getScreen(shell.getText());
		if (winprop!=null) winprop.setShell(shell); else shell.pack();
		
		shell.open();
		Display display = parent.getDisplay();
		while (!shell.isDisposed()) 
		{
			if (!display.readAndDispatch()) display.sleep();
		}
		return retval;
	}
	
	public void getData()
	{
		for (int i=0;i<schema.nrSelFields();i++)
		{
			TableField f = schema.getSelField(i);
			
			addToFieldsList(f.getTable().getName(), f.getName());
		}

		for (int i=0;i<schema.nrSelConditions();i++)
		{
			WhereCondition c= schema.getSelCondition(i);
			
			addToConditionsList(c.getTable().getName(), c.getName());
		}

		String cf = schema.getCubeFile();
		if (cf!=null) wCube.setText(cf);
	}
	
	public void addSelectedToList()
	{

		TreeItem ti[] = wTree.getSelection();
		
        for (int i=0;i<ti.length;i++) 
		{
			String itemname = ti[i].getText();
			TreeItem parent = ti[i].getParentItem();
			if (parent!=null)
			{
				String parentname = parent.getText();
				TreeItem grandparent = parent.getParentItem();
				if (grandparent!=null)
				{
					String grandparentname = grandparent.getText();
								
					System.out.println("Grandparent = "+grandparentname+", parent="+parentname+", item="+itemname);
					if (grandparentname.equalsIgnoreCase(STRING_FIELDS))
					{
						addToFieldsList(parentname, itemname);
					}
					else
					if (grandparentname.equalsIgnoreCase(STRING_CONDITIONS))
					{
						addToConditionsList(parentname, itemname);
					}
				}
			}
		} 
	}

	public void addTableToList(String tablename)
	{	
		TableMeta tab = schema.findTable(tablename);
		if (tab!=null)
		{
			for (int j=0;j<tab.nrFields();j++)
			{
				TableField f = tab.getField(j);
				if (!f.isHidden())
				{
					String fieldname = tab.getField(j).getName();
					addToFieldsList(tablename, fieldname);
				}
			}
		}
	}


	public void addToFieldsList(String tablename, String fieldname)
	{
		String display = tablename+" . "+fieldname;
		int idx = wList.indexOf(display);
		if (idx<0) wList.add(display);
	}

	public void addToConditionsList(String tablename, String conditionname)
	{
		String display = tablename+" . "+conditionname;
		int idx = wCondition.indexOf(display);
		if (idx<0) wCondition.add(display);
	}
	
	public void delSelectedFields()
	{
		int idx[] = wList.getSelectionIndices();
		wList.remove(idx);
	}

	public void delSelectedConditions()
	{
		int idx[] = wCondition.getSelectionIndices();
		wCondition.remove(idx);
	}
	
	public void dispose()
	{
		WindowProperty winprop = new WindowProperty(shell);
		schema.props.setScreen(winprop);
		shell.dispose();
	}
	
	public void handleOK()
	{
		retval=true;
		String str[] = wList.getItems();
		String con[] = wCondition.getItems();
		
		fields     = new TableField[str.length];
		conditions = new WhereCondition[con.length];

		// The selected fields...		
		for (int i=0;i<str.length;i++)
		{
			int idx = str[i].indexOf(" . ");
			if (idx>=0)
			{
				String tablename = str[i].substring(0, idx);
				String fieldname = str[i].substring(idx+3);
				
				TableMeta tab = schema.findTable(tablename);
				if (tab!=null)
				{
					fields[i] = tab.findField(fieldname);
				}
			}
		}

		// The selected conditions...		
		for (int i=0;i<con.length;i++)
		{
			int idx = con[i].indexOf(" . ");
			if (idx>=0)
			{
				String tablename = con[i].substring(0, idx);
				String condiname = con[i].substring(idx+3);
				
				TableMeta tab = schema.findTable(tablename);
				if (tab!=null)
				{
					conditions[i] = tab.findCondition(condiname);
				}
			}
		}

		schema.setCubeFile(wCube.getText());

		dispose();
	}
	
	public String toString()
	{
		return this.getClass().getName();
	}

}
