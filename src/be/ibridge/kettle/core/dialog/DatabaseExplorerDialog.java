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

 


package be.ibridge.kettle.core.dialog;

import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.DBCache;
import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.Props;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.WindowProperty;
import be.ibridge.kettle.core.database.Catalog;
import be.ibridge.kettle.core.database.Database;
import be.ibridge.kettle.core.database.DatabaseMeta;
import be.ibridge.kettle.core.database.DatabaseMetaInformation;
import be.ibridge.kettle.core.database.Schema;
import be.ibridge.kettle.core.exception.KettleDatabaseException;
import be.ibridge.kettle.test.EditDatabaseTable;
import be.ibridge.kettle.trans.step.BaseStepDialog;


/**
 * This dialog represents an explorer type of interface on a given database connection.
 * It shows the tables defined in the visible schemas or catalogs on that connection.
 * The interface also allows you to get all kinds of information on those tables.
 * 
 * @author Matt
 * @since 18-05-2003
 *
 */
public class DatabaseExplorerDialog extends Dialog 
{
	private LogWriter log;
	private Props props;
	private DatabaseMeta dbMeta;
	private DBCache dbcache;
	
	private static final String STRING_CATALOG  = "Catalogs";
	private static final String STRING_SCHEMAS  = "Schema's";
	private static final String STRING_TABLES   = "Tables";
	private static final String STRING_VIEWS    = "Views";
	private static final String STRING_SYNONYMS = "Synonyms";
	
	private Shell     shell;
	private Tree      wTree;
	private TreeItem  tiTree;
	 
	private Button    wOK;
	private Button    wRefresh;
	private Button    wCancel;
	
	/** This is the return value*/
	private String    tableName; 
	
	private boolean justLook;
	private String selectTable;
	private ArrayList databases;
	
	public DatabaseExplorerDialog(Shell par, Props pr, int style, DatabaseMeta conn, ArrayList databases)
	{
		super(par, style);
		props=pr;
		log=LogWriter.getInstance();
		dbMeta=conn;
		dbcache = DBCache.getInstance();
		this.databases = databases;
		justLook=false;
		selectTable=null;
	}

	public DatabaseExplorerDialog(Shell par, Props pr, int style, DatabaseMeta conn, ArrayList databases, boolean look)
	{
		this(par, pr, style, conn, databases);
		justLook=look;
	}

	public void setSelectedTable(String selectedTable)
	{
		// System.out.println("Table to select: "+selectedTable);
		this.selectTable = selectedTable;
	}
	
	public Object open() 
	{
		tableName = null;

		Shell parent = getParent();
		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN);
 		props.setLook(shell);
		shell.setText("Database Explorer on ["+dbMeta.toString()+"]");
		
		FormLayout formLayout = new FormLayout ();
		formLayout.marginWidth  = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;
		
		shell.setLayout (formLayout);
 		
 		// Tree
 		wTree = new Tree(shell, SWT.SINGLE | SWT.BORDER /*| (multiple?SWT.CHECK:SWT.NONE)*/);
 		props.setLook( 		wTree);
 				
		if (!getData()) return null;
 		
 		// Buttons
		wOK = new Button(shell, SWT.PUSH); 
		wOK.setText("  &OK  ");

		wRefresh = new Button(shell, SWT.PUSH); 
		wRefresh.setText("  &Refresh  ");
		
		if (!justLook) 
		{
			wCancel = new Button(shell, SWT.PUSH);
			wCancel.setText("  &Cancel  ");
		}
		
		FormData fdTree      = new FormData(); 

		int margin =  10;

		fdTree.left   = new FormAttachment(0, 0); // To the right of the label
		fdTree.top    = new FormAttachment(0, 0);
		fdTree.right  = new FormAttachment(100, 0);
		fdTree.bottom = new FormAttachment(100, -50);
		wTree.setLayoutData(fdTree);

		if (!justLook) 
		{
			BaseStepDialog.positionBottomButtons(shell, new Button[] { wOK, wRefresh, wCancel}, margin, null);

			// Add listeners
			wCancel.addListener(SWT.Selection, new Listener ()
				{
					public void handleEvent (Event e) 
					{
						log.logDebug("SelectTableDialog", "CANCEL SelectTableDialog");
						dbMeta=null;
						dispose();
					}
				}
			);
		}
		else
		{
			BaseStepDialog.positionBottomButtons(shell, new Button[] { wOK, wRefresh }, margin, null);		    
		}

		// Add listeners
		wOK.addListener(SWT.Selection, new Listener ()
			{
				public void handleEvent (Event e) 
				{
					handleOK();
				}
			}
		);
		wRefresh.addListener(SWT.Selection, new Listener ()
			{
				public void handleEvent (Event e) 
				{
					getData();
				}
			}
		);
		SelectionAdapter selAdapter=new SelectionAdapter()
			{
				public void widgetDefaultSelected(SelectionEvent e)
				{
					openSchema(e);	
				}
			};
		wTree.addSelectionListener(selAdapter);
		
		wTree.addMouseListener(new MouseAdapter()
			{
				public void mouseDown(MouseEvent e)
				{
					if (e.button == 3) // right click!
					{
						setTreeMenu();
					}
				}
			}
		);

		// Detect X or ALT-F4 or something that kills this window...
		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { dispose(); } } );


		WindowProperty winprop = props.getScreen(shell.getText());
		if (winprop!=null) winprop.setShell(shell); else 
		{
		    shell.pack();
		    shell.setSize(320, 480);
		}

		shell.open();
		Display display = parent.getDisplay();
		while (!shell.isDisposed()) 
		{
			if (!display.readAndDispatch()) display.sleep();
		}
		return tableName;
	}
	
	private boolean getData()
	{
		GetDatabaseInfoProgressDialog gdipd = new GetDatabaseInfoProgressDialog(log, props, shell, dbMeta);
		DatabaseMetaInformation dmi = gdipd.open();
		if (dmi!=null)
		{
			// Clear the tree top entry
			if (tiTree!=null && !tiTree.isDisposed()) tiTree.dispose();
				
			// New entry in the tree
			tiTree = new TreeItem(wTree, SWT.NONE); 
			tiTree.setText(dbMeta==null?"":dbMeta.getName());

			// Show the catalogs...
			Catalog[] catalogs = dmi.getCatalogs();
			if (catalogs!=null)
			{
				TreeItem tiCat = new TreeItem(tiTree, SWT.NONE); 
				tiCat.setText(STRING_CATALOG);
				
				for (int i=0;i<catalogs.length;i++)
				{
					TreeItem newCat = new TreeItem(tiCat, SWT.NONE);
					newCat.setText(catalogs[i].getCatalogName());
					
					for (int j=0;j<catalogs[i].getItems().length;j++)
					{
						String tableName = catalogs[i].getItems()[j];
	
						TreeItem ti = new TreeItem(newCat, SWT.NONE);
						ti.setText(tableName);
					}
				}
			}
				
			// The schema's
			Schema[] schemas= dmi.getSchemas();
			if (schemas!=null)
			{
				TreeItem tiSch = new TreeItem(tiTree, SWT.NONE); 
				tiSch.setText(STRING_SCHEMAS);
	
				for (int i=0;i<schemas.length;i++)
				{
					TreeItem newSch = new TreeItem(tiSch, SWT.NONE);
					newSch.setText(schemas[i].getSchemaName());
					
					for (int j=0;j<schemas[i].getItems().length;j++)
					{
						String tableName = schemas[i].getItems()[j];
	
						TreeItem ti = new TreeItem(newSch, SWT.NONE);
						ti.setText(tableName);
					}
				}
			}

			// The tables in general...
			TreeItem tiTab = null;
			String tabnames[] = dmi.getTables();
			if (tabnames!=null)
			{
				tiTab = new TreeItem(tiTree, SWT.NONE); 
				tiTab.setText(STRING_TABLES);
				tiTab.setExpanded(true);
				
				for (int i = 0; i < tabnames.length; i++)
				{
					TreeItem newTab = new TreeItem(tiTab, SWT.NONE);
					newTab.setText(tabnames[i]);
				}
			}
			
			// The views...
			TreeItem tiView = null;
			String views[] = dmi.getViews();
			if (views!=null)
			{
				tiView = new TreeItem(tiTree, SWT.NONE); 
				tiView.setText(STRING_VIEWS);
				for (int i = 0; i < views.length; i++)
				{
					TreeItem newView = new TreeItem(tiView, SWT.NONE);
					newView.setText(views[i]);
				}
			}
				

			// The synonyms
			TreeItem tiSyn = null;
			String[] syn = dmi.getSynonyms();
			if (syn!=null)
			{
				tiSyn = new TreeItem(tiTree, SWT.NONE); 
				tiSyn.setText(STRING_SYNONYMS);
				for (int i = 0; i < syn.length; i++)
				{
					TreeItem newSyn = new TreeItem(tiSyn, SWT.NONE);
					newSyn.setText(syn[i]);
				}
			}
				
			// Make sure the selected table is shown...
			// System.out.println("Selecting table "+k);
			if (selectTable!=null && selectTable.length()>0)
			{
				TreeItem ti = null;
                if (ti==null && tiTab!=null) Const.findTreeItem(tiTab,  selectTable);
				if (ti==null && tiView!=null) Const.findTreeItem(tiView, selectTable);
				if (ti==null && tiTree!=null) Const.findTreeItem(tiTree, selectTable);
				if (ti==null && tiSyn!=null) Const.findTreeItem(tiSyn,  selectTable);
				
				if (ti!=null)
				{
					// System.out.println("Selection set on "+ti.getText());
					wTree.setSelection(new TreeItem[] { ti });
					wTree.showSelection();
				}
				
				selectTable=null;
			}
			
			tiTree.setExpanded(true);
		}
		else
		{
			return false;
		}
		
		return true;
	}

	public void setTreeMenu()
	{
		Menu mTree = null;
		
		TreeItem ti[]=wTree.getSelection();
		if (ti.length==1)
		{
			// Get the parent.
			TreeItem parent = ti[0].getParentItem();
			if (parent!=null)
			{
				String schemaName = parent.getText();
				String tableName  = ti[0].getText();

				if (ti[0].getItemCount()==0) // No children, only the tables themselves...
				{
					String tab = null;
					if (schemaName.equalsIgnoreCase(STRING_TABLES) ||
						schemaName.equalsIgnoreCase(STRING_VIEWS) ||
						schemaName.equalsIgnoreCase(STRING_SYNONYMS) ||
						( schemaName!=null && schemaName.length()==0 )
						)
					{
						tab = tableName;
					}
					else
					{
						tab = dbMeta.getSchemaTableCombination(schemaName, tableName);
					}
					final String table = tab;
					
					mTree = new Menu(shell, SWT.POP_UP);
					MenuItem miPrev  = new MenuItem(mTree, SWT.PUSH); miPrev.setText("&Preview first 100 rows of ["+table+"]");
					miPrev.addSelectionListener( new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { previewTable(table, false); }});
					MenuItem miPrevN  = new MenuItem(mTree, SWT.PUSH); miPrevN.setText("Preview &first ... rows of ["+table+"]");
					miPrevN.addSelectionListener( new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { previewTable(table, true); }});
					//MenuItem miEdit   = new MenuItem(mTree, SWT.PUSH); miEdit.setText("Open ["+table+"] for editing");
					//miEdit.addSelectionListener( new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { editTable(table); }});
					MenuItem miCount = new MenuItem(mTree, SWT.PUSH); miCount.setText("Show size of ["+table+"]");
					miCount.addSelectionListener( new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { showCount(table); }});

					new MenuItem(mTree, SWT.SEPARATOR);
					
					MenuItem miShow  = new MenuItem(mTree, SWT.PUSH); miShow.setText("Show layout of ["+table+"]");
					miShow.addSelectionListener( new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { showTable(table); }});
					MenuItem miDDL  = new MenuItem(mTree, SWT.PUSH); miDDL.setText("Generate DDL");
					miDDL.addSelectionListener( new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { getDDL(table); }});
					MenuItem miDDL2  = new MenuItem(mTree, SWT.PUSH); miDDL2.setText("Generate DDL for other connection");
					miDDL2.addSelectionListener( new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { getDDLForOther(table); }});
					MenuItem miSQL  = new MenuItem(mTree, SWT.PUSH); miSQL.setText("Open SQL for ["+table+"]");
					miSQL.addSelectionListener( new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { getSQL(table); }});
				}
			}
		}
		wTree.setMenu(mTree);
	}

	public void previewTable(String tableName, boolean asklimit)
	{
		int limit = 100;
		if (asklimit)
		{
			// Ask how many lines we should preview.
			String shellText = "Preview limit";
			String lineText = "Number of lines to preview (0=all lines)";
			EnterNumberDialog end = new EnterNumberDialog(shell, props, limit, shellText, lineText);
			int samples = end.open();
			if (samples>=0) limit=samples;
		}

	    GetPreviewTableProgressDialog pd = new GetPreviewTableProgressDialog(log, props, shell, dbMeta, tableName, limit);
	    ArrayList rows = pd.open();
	    if (rows!=null) // otherwise an already shown error...
	    {
			if (rows.size()>0)
			{
				PreviewRowsDialog prd = new PreviewRowsDialog(shell, SWT.NONE, tableName, rows);
				prd.open();
			}
			else
			{
				MessageBox mb = new MessageBox(shell, SWT.ICON_INFORMATION | SWT.OK);
				mb.setMessage("This table contains no rows!");
				mb.setText("Sorry");
				mb.open();
			}
	    }
	}

	public void editTable(String tableName)
	{
		EditDatabaseTable edt = new EditDatabaseTable(shell, SWT.NONE, props, dbMeta, tableName, 20);
		edt.open();
	}

	public void showTable(String tableName)
	{
	    String sql = dbMeta.getSQLQueryFields(tableName);
	    GetQueryFieldsProgressDialog pd = new GetQueryFieldsProgressDialog(log, props, shell, dbMeta, sql);
	    Row result = pd.open();         
		if (result!=null)
		{
			StepFieldsDialog sfd = new StepFieldsDialog(shell, SWT.NONE, log, tableName, result, props);
			sfd.open();
		}
	}

	public void showCount(String tableName)
	{
	    GetTableSizeProgressDialog pd = new GetTableSizeProgressDialog(log, props, shell, dbMeta, tableName);
		Row r = pd.open();
		if (r!=null)
		{
			String result = "Table ["+tableName+"] has "+r.getValue(0).getInteger()+" rows.";
			
			EnterTextDialog etd = new EnterTextDialog(shell, "Count", "# rows in "+tableName, result, true);
			etd.open();
		}
	}

	public void getDDL(String tableName)
	{
		Database db = new Database(dbMeta);
		try
		{
			db.connect();
			Row r = db.getTableFields(tableName);
			String sql = db.getCreateTableStatement(tableName, r, null, false, null, true);
			SQLEditor se = new SQLEditor(shell, SWT.NONE, dbMeta, dbcache, sql);
			se.open();
		}
		catch(KettleDatabaseException dbe)
		{
			new ErrorDialog(shell, props, "Error", "Couldn't retrieve the table layout.", dbe);
		}
		finally
		{
			db.disconnect();
		}
	}
	
	public void getDDLForOther(String tableName)
	{
        if (databases!=null)
        {
    		Database db = new Database(dbMeta);
    		try
    		{
    			db.connect();
    			
    			Row r = db.getTableFields(tableName);
    
    			// Now select the other connection...
                
                // Only take non-SAP R/3 connections....
                ArrayList dbs = new ArrayList();
                for (int i=0;i<databases.size();i++) 
                    if (((DatabaseMeta)databases.get(i)).getDatabaseType()!=DatabaseMeta.TYPE_DATABASE_SAPR3) dbs.add(databases.get(i));
                
                String conn[] = new String[dbs.size()];
    			for (int i=0;i<conn.length;i++) conn[i] = ((DatabaseMeta)dbs.get(i)).getName();
    			
    			EnterSelectionDialog esd = new EnterSelectionDialog(shell, props, conn, "Target database:", "Select the target database:");
    			String target = esd.open();
    			if (target!=null)
    			{
    				DatabaseMeta targetdbi = Const.findDatabase(dbs, target);
    				Database targetdb = new Database(targetdbi);
    
    				String sql = targetdb.getCreateTableStatement(tableName, r, null, false, null, true);
    				SQLEditor se = new SQLEditor(shell, SWT.NONE, dbMeta, dbcache, sql);
    				se.open();
    			}
    		}
    		catch(KettleDatabaseException dbe)
    		{
    			new ErrorDialog(shell, props, "Error", "Couldn't generate the DDL", dbe);
    		}
    		finally
    		{
    			db.disconnect();
    		}
        }
        else
        {
            MessageBox mb = new MessageBox(shell, SWT.NONE | SWT.ICON_INFORMATION);
            mb.setMessage("I'm unable to perform this operation as I don't know the other available connections at this point.");
            mb.setText("Sorry");
            mb.open();
        }
	}

	
	public void getSQL(String tableName)
	{
		SQLEditor sql = new SQLEditor(shell, SWT.NONE, dbMeta, dbcache, "SELECT * FROM "+tableName);
		sql.open();
	}

	public void dispose()
	{
		props.setScreen(new WindowProperty(shell));
		shell.dispose();
	}
	
	public void handleOK()
	{
		if (justLook) 
		{
			dispose();
			return;
		} 
		TreeItem ti[]=wTree.getSelection();
		if (ti.length==1)
		{
			// Get the parent.
			TreeItem parent = ti[0].getParentItem();
			if (parent!=null)
			{
				String schemaName = parent.getText();
				String tablePart  = ti[0].getText();
				
				String tab = null;
				if (schemaName.equalsIgnoreCase(STRING_TABLES))
				{
					tab = tablePart;
				}
				else
				{
					tab = dbMeta.getSchemaTableCombination(schemaName, tablePart);
				}
				tableName = tab;
								
				dispose();
			}
		}
	}
	
	public void openSchema(SelectionEvent e)
	{
		TreeItem sel = (TreeItem)e.item;
		
		log.logDebug("SelectTableDialog", "Open :"+sel.getText());
		
		TreeItem up1 = sel.getParentItem();
		if (up1 != null)
		{
			TreeItem up2 = up1.getParentItem();
			if (up2 != null)
			{
				TreeItem up3 = up2.getParentItem();
				if (up3 != null)
				{
					tableName = sel.getText();
					if (!justLook) handleOK();
					else previewTable(tableName, false);
				}
			}
		}
	}
	
	public String toString()
	{
		return this.getClass().getName();
	}

}
