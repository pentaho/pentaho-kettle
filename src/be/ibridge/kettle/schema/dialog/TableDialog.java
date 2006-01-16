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
 * Created on 19-jun-2003
 *
 */

package be.ibridge.kettle.schema.dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import be.ibridge.kettle.core.ColumnInfo;
import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.Props;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.WindowProperty;
import be.ibridge.kettle.core.database.Database;
import be.ibridge.kettle.core.database.DatabaseMeta;
import be.ibridge.kettle.core.dialog.DatabaseDialog;
import be.ibridge.kettle.core.dialog.DatabaseExplorerDialog;
import be.ibridge.kettle.core.dialog.EnterTextDialog;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.value.Value;
import be.ibridge.kettle.core.widget.TableView;
import be.ibridge.kettle.schema.SchemaMeta;
import be.ibridge.kettle.schema.TableField;
import be.ibridge.kettle.schema.TableMeta;
import be.ibridge.kettle.schema.WhereCondition;


public class TableDialog extends Dialog
{
	private LogWriter    log;

	private Label        wlName;
	private Text         wName;
	private FormData     fdlName, fdName;

	private Label        wlTable;
	private Button       wbTable;
	private Text         wTable;
	private FormData     fdlTable, fdbTable, fdTable;

	private Label        wlConnection;
	private CCombo       wConnection;
	private Button		 wbConnection;
	private FormData     fdlConnection, fdbConnection, fdConnection;

	private Label        wlType;
	private CCombo       wType;
	private FormData     fdlType, fdType;

	private Label        wlSize;
	private Text         wSize;
	private FormData     fdlSize, fdSize;

	private CTabFolder   wTabfolder;
	private FormData     fdTabfolder;

	private TableView    wFields;
	private FormData     fdFields;

	private TableView    wConditions;
	private FormData     fdConditions;

	private Button wOK, wGet, wCancel;
	private FormData fdOK, fdGet, fdCancel;
	private Listener lsOK, lsGet, lsCancel;

	private Shell         shell;
	private TableMeta     tableinfo;
	private String        tablename;
	private SchemaMeta    schema;
	
	private SelectionAdapter lsDef;
    private Props props;
	
	public TableDialog(Shell parent, int style, LogWriter l, TableMeta ti, SchemaMeta sch)
	{
		super(parent, style);
		log=l;
		tableinfo=ti;
		schema=sch;
        
        props=Props.getInstance();
	}

	public String open()
	{
		Shell parent = getParent();
		Display display = parent.getDisplay();
		
		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN);
 		props.setLook(shell);

		ModifyListener lsMod = new ModifyListener() 
		{
			public void modifyText(ModifyEvent e) 
			{
				tableinfo.setChanged();
			}
		};

		FormLayout formLayout = new FormLayout ();
		formLayout.marginWidth  = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText("Table properties screen");
		
		int middle = schema.props.getMiddlePct();
		int margin = Const.MARGIN;

		// Name line
		wlName=new Label(shell, SWT.RIGHT);
		wlName.setText("Name ");
 		props.setLook(wlName);
		fdlName=new FormData();
		fdlName.left = new FormAttachment(0, 0);
		fdlName.right= new FormAttachment(middle, -margin);
		fdlName.top  = new FormAttachment(0, margin);
		wlName.setLayoutData(fdlName);
		wName=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wName.setText("");
 		props.setLook(wName);
		wName.addModifyListener(lsMod);
		fdName=new FormData();
		fdName.left = new FormAttachment(middle, 0);
		fdName.top  = new FormAttachment(0, margin);
		fdName.right= new FormAttachment(middle, 350);
		wName.setLayoutData(fdName);

		// Table line...
		wlTable=new Label(shell, SWT.RIGHT);
		wlTable.setText("Target table ");
 		props.setLook(wlTable);
		fdlTable=new FormData();
		fdlTable.left = new FormAttachment(0, 0);
		fdlTable.right= new FormAttachment(middle, -margin);
		fdlTable.top  = new FormAttachment(wName, margin*2);
		wlTable.setLayoutData(fdlTable);
		wTable=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wTable);
		wTable.addModifyListener(lsMod);
		fdTable=new FormData();
		fdTable.left = new FormAttachment(middle, 0);
		fdTable.right= new FormAttachment(middle, 350);
		fdTable.top  = new FormAttachment(wName, margin*2);
		wTable.setLayoutData(fdTable);
		wbTable=new Button(shell, SWT.PUSH| SWT.CENTER);
 		props.setLook(wbTable);
		wbTable.setText("&Browse...");
		fdbTable=new FormData();
		fdbTable.left = new FormAttachment(wTable, 5);
		fdbTable.right= new FormAttachment(middle, 350+margin+75);
		fdbTable.top  = new FormAttachment(wName, margin);
		wbTable.setLayoutData(fdbTable);
		wbTable.addSelectionListener
		(
			new SelectionAdapter()
			{
				public void widgetSelected(SelectionEvent e) 
				{
					getTableName();
				}
			}
		);
		
		// Connection line
		wlConnection=new Label(shell, SWT.RIGHT);
		wlConnection.setText("Connection ");
 		props.setLook(wlConnection);
		fdlConnection=new FormData();
		fdlConnection.left = new FormAttachment(0, 0);
		fdlConnection.top  = new FormAttachment(wTable, margin);
		fdlConnection.right= new FormAttachment(middle, -margin);
		wlConnection.setLayoutData(fdlConnection);
		wConnection=new CCombo(shell, SWT.BORDER | SWT.READ_ONLY);
 		props.setLook(wConnection);
		for (int i=0;i<schema.nrConnections();i++)
		{
			DatabaseMeta ci = schema.getConnection(i);
			wConnection.add(ci.getName());
		}
		wConnection.select(0);
		wConnection.addModifyListener(lsMod);
		fdConnection=new FormData();
		fdConnection.left = new FormAttachment(middle, 0);
		fdConnection.top  = new FormAttachment(wTable, margin);
		fdConnection.right= new FormAttachment(middle, 350);
		wConnection.setLayoutData(fdConnection);
		
		wbConnection=new Button(shell, SWT.PUSH);
		wbConnection.setText("&New...");
		wbConnection.addSelectionListener(new SelectionAdapter() 
		{
			public void widgetSelected(SelectionEvent e) 
			{
				DatabaseMeta ci = new DatabaseMeta();
				DatabaseDialog cid = new DatabaseDialog(shell, SWT.NONE, log, ci, schema.props);
				if (cid.open()!=null)
				{
					schema.addConnection(ci);
					wConnection.add(ci.getName());
					wConnection.select(wConnection.getItemCount()-1);
				}
			}
		});
		fdbConnection=new FormData();
		fdbConnection.left = new FormAttachment(wConnection, margin*2);
		fdbConnection.top  = new FormAttachment(wTable, margin);
		wbConnection.setLayoutData(fdbConnection);


		// Type line
		wlType=new Label(shell, SWT.RIGHT);
		wlType.setText("Type of table: ");
 		props.setLook(wlType);
		fdlType=new FormData();
		fdlType.left = new FormAttachment(0, 0);
		fdlType.right= new FormAttachment(middle, -margin);
		fdlType.top  = new FormAttachment(wConnection, margin);
		wlType.setLayoutData(fdlType);
		wType=new CCombo(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wType.setText("Select the table type");
 		props.setLook(wType);

		for (int i=0;i<TableMeta.typeTableDesc.length;i++)
		{
			wType.add(TableMeta.typeTableDesc[i]);
		}
		wType.addModifyListener(lsMod);

		fdType=new FormData();
		fdType.left = new FormAttachment(middle, 0);
		fdType.top  = new FormAttachment(wConnection, margin);
		fdType.right= new FormAttachment(middle, 350);
		wType.setLayoutData(fdType);

		// Size line
		wlSize=new Label(shell, SWT.RIGHT);
		wlSize.setText("Relative size ");
 		props.setLook(wlSize);
		fdlSize=new FormData();
		fdlSize.left = new FormAttachment(0, 0);
		fdlSize.right= new FormAttachment(middle, -margin);
		fdlSize.top  = new FormAttachment(wType, margin);
		wlSize.setLayoutData(fdlSize);
		wSize=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wSize.setText("");
 		props.setLook(wSize);
		wSize.addModifyListener(lsMod);
		fdSize=new FormData();
		fdSize.left = new FormAttachment(middle, 0);
		fdSize.right= new FormAttachment(middle, 350);
		fdSize.top  = new FormAttachment(wType, margin);
		wSize.setLayoutData(fdSize);

		
		wTabfolder = new CTabFolder(shell, SWT.BORDER);
		props.setLook(wTabfolder, Props.WIDGET_STYLE_TAB);

		CTabItem wItemFields = new CTabItem(wTabfolder, SWT.NONE);
		wItemFields.setText("Fields");
		
		final int FieldsCols=7;
		final int FieldsRows=tableinfo.nrFields();
		
		ColumnInfo[] colinf=new ColumnInfo[FieldsCols];
		colinf[0]=new ColumnInfo("Fieldname",   ColumnInfo.COLUMN_TYPE_TEXT, false, false);
		colinf[1]=new ColumnInfo("DB Formula",  ColumnInfo.COLUMN_TYPE_TEXT, false, false);
		colinf[2]=new ColumnInfo("Field Type",  ColumnInfo.COLUMN_TYPE_CCOMBO, TableField.typeFieldDesc);
		colinf[3]=new ColumnInfo("Aggr. Type",  ColumnInfo.COLUMN_TYPE_CCOMBO, TableField.typeAggregationDesc);
		colinf[4]=new ColumnInfo("Hidden?",     ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] { "N", "Y" } );
		colinf[5]=new ColumnInfo("Exact?",      ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] { "N", "Y" } );
		colinf[6]=new ColumnInfo("Description", ColumnInfo.COLUMN_TYPE_BUTTON, "", "...");
		
		wFields=new TableView(wTabfolder, 
							  SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI, 
							  colinf, 
							  FieldsRows,  
							  false, // read-only
							  lsMod,
							  schema.props
							  );
		
		SelectionAdapter selField = new SelectionAdapter() 
		{
			public void widgetSelected(SelectionEvent e) 
			{
				String str = wFields.getButtonString();
				EnterTextDialog etd = new EnterTextDialog(shell, "Button", "enter text", str);
				etd.setModal();
				String res = etd.open();
				if (res!=null)
				{
					wFields.setButtonString(res);
					wFields.closeActiveButton();
				}
			}
		}
		;
		colinf[6].setSelectionAdapter(selField);
		colinf[6].setToolTip("Click on this button to edit the description...");

		fdFields=new FormData();
		fdFields.left   = new FormAttachment(0, 0);
		fdFields.top    = new FormAttachment(0, 0);
		fdFields.right  = new FormAttachment(100, 0);
		fdFields.bottom = new FormAttachment(100, 0);
		wFields.setLayoutData(fdFields);
		
		wItemFields.setControl(wFields);

		CTabItem wItemConditions = new CTabItem(wTabfolder, SWT.NONE);
		wItemConditions.setText("Conditions");

		final int ConditionsCols=5;
		final int ConditionsRows=tableinfo.nrConditions();
		
		colinf=new ColumnInfo[ConditionsCols];
		colinf[0]=new ColumnInfo("Name",        ColumnInfo.COLUMN_TYPE_TEXT, false, false);
		colinf[1]=new ColumnInfo("Fieldname",   ColumnInfo.COLUMN_TYPE_TEXT, false, false);
		colinf[2]=new ColumnInfo("Comparator",  ColumnInfo.COLUMN_TYPE_CCOMBO, WhereCondition.comparators );
		colinf[3]=new ColumnInfo("DB Formula",  ColumnInfo.COLUMN_TYPE_TEXT, false, false);
		colinf[4]=new ColumnInfo("Description", ColumnInfo.COLUMN_TYPE_BUTTON, "", "...");
		
		wConditions=new TableView(wTabfolder, 
							  SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI, 
							  colinf, 
							  ConditionsRows,  
							  false, // read-only
							  lsMod,
							  schema.props
							  );

		SelectionAdapter selCondition = new SelectionAdapter() 
		{
			public void widgetSelected(SelectionEvent e) 
			{
				String str = wConditions.getButtonString();
				EnterTextDialog etd = new EnterTextDialog(shell, "Button", "enter text", str);
				etd.setModal();
				String res = etd.open();
				if (res!=null)
				{
					wConditions.setButtonString(res);
					wConditions.closeActiveButton();
				}
			}
		}
		;

		colinf[2].setSelectionAdapter(selCondition);
		colinf[2].setToolTip("Click on this button to edit the description...");

		fdConditions=new FormData();
		fdConditions.left   = new FormAttachment(0, 0);
		fdConditions.top    = new FormAttachment(0, 0);
		fdConditions.right  = new FormAttachment(100, 0);
		fdConditions.bottom = new FormAttachment(100, 0);
		wConditions.setLayoutData(fdConditions);
		
		wItemConditions.setControl(wConditions);


		fdTabfolder=new FormData();
		fdTabfolder.left   = new FormAttachment(0, 0);
		fdTabfolder.top    = new FormAttachment(wSize, margin);
		fdTabfolder.right  = new FormAttachment(100, 0);
		fdTabfolder.bottom = new FormAttachment(100, -50);
		wTabfolder.setLayoutData(fdTabfolder);

		
		wOK=new Button(shell, SWT.PUSH);
		wOK.setText(" &OK ");
		wGet=new Button(shell, SWT.PUSH);
		wGet.setText(" &Get ");
		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText(" &Cancel ");
		fdOK=new FormData();
		fdOK.left=new FormAttachment(25, 0);
		fdOK.bottom =new FormAttachment(100, 0);
		wOK.setLayoutData(fdOK);
		fdGet=new FormData();
		fdGet.left=new FormAttachment(50, 0);
		fdGet.bottom =new FormAttachment(100, 0);
		wGet.setLayoutData(fdGet);
		fdCancel=new FormData();
		fdCancel.left=new FormAttachment(75, 0);
		fdCancel.bottom =new FormAttachment(100, 0);
		wCancel.setLayoutData(fdCancel);

		// Add listeners
		lsCancel = new Listener() { public void handleEvent(Event e) { cancel();  } };
		lsGet    = new Listener() { public void handleEvent(Event e) { get(); } };
		lsOK     = new Listener() { public void handleEvent(Event e) { ok(); } };
		
		wCancel.addListener(SWT.Selection, lsCancel );
		wGet   .addListener(SWT.Selection, lsGet );
		wOK    .addListener(SWT.Selection, lsOK );

		lsDef=new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } };
		
		wName.addSelectionListener(lsDef);		
		wTable.addSelectionListener(lsDef);	
		wSize.addSelectionListener(lsDef);

		// Detect X or ALT-F4 or something that kills this window...
		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { cancel(); } } );

	
		WindowProperty winprop = schema.props.getScreen(shell.getText());
		if (winprop!=null) winprop.setShell(shell); else shell.pack();
				
		wTabfolder.setSelection(0);
		
		getData();
		
		shell.open();
		while (!shell.isDisposed())
		{
				if (!display.readAndDispatch()) display.sleep();
		}
		return tablename;
	}

	public void dispose()
	{
		schema.props.setScreen(new WindowProperty(shell));
		shell.dispose();
	}
	
	/**
	 * Copy information from the meta-data input to the dialog fields.
	 */ 
	public void getData()
	{
		if (tableinfo.getName()!=null) wName.setText(tableinfo.getName());	
		if (tableinfo.getDBName()!=null) wTable.setText(tableinfo.getDBName());	
		if (tableinfo.getSize()>=0) wSize.setText(""+tableinfo.getSize());
		if (tableinfo.getDatabase()!=null) wConnection.setText(tableinfo.getDatabase().getName());
		
		wType.select( tableinfo.getType() );
		
		for (int i=0;i<tableinfo.nrFields();i++)
		{
			TableField f = tableinfo.getField(i);
			
			TableItem item = wFields.table.getItem(i);
			String name = f.getName();
			String dbname = f.getDBName();
			String ftype  = f.getFieldTypeDesc();
			String atype  = f.getAggregationDesc();
			String desc   = f.getDescription();

			if (name!=null)   item.setText(1, name);
			if (dbname!=null) item.setText(2, dbname);
			if (ftype!=null)  item.setText(3, ftype);
			if (atype!=null)  item.setText(4, atype);
				
			item.setText(5, f.isHidden()?"Y":"N");		
			item.setText(6, f.isExact()?"Y":"N");	

			if (desc!=null)   item.setText(7, desc);
		}
		wFields.optWidth(true);

		for (int i=0;i<tableinfo.nrConditions();i++)
		{
			WhereCondition c = tableinfo.getCondition(i);
			
			TableItem item = wConditions.table.getItem(i);
			String name  = c.getName();
			String code  = c.getCode();
			String desc  = c.getDescription();
			String comp  = c.getComparator();
			String field = c.getField()!=null?c.getField().getName():null; 

			if (name !=null) item.setText(1, name);
			if (field!=null) item.setText(2, field);
			if (comp !=null) item.setText(3, comp);
			if (code !=null) item.setText(4, code);
			if (desc !=null) item.setText(5, desc);
		}
		wConditions.optWidth(true);
		
		wName.selectAll();
	}
	
	private void cancel()
	{
		tablename=null;
		dispose();
	}
	
	private void ok()
	{
		tableinfo.setName(wName.getText());
		tableinfo.setDBName(wTable.getText());
		tableinfo.setType(wType.getSelectionIndex());
		tableinfo.setSize(Const.toInt(wSize.getText(), -1));
		tableinfo.setDatabase(schema.findConnection(wConnection.getText()));
		
		tableinfo.removeAllFields();
		
		for (int i=0;i<wFields.nrNonEmpty();i++)
		{
			TableItem item = wFields.getNonEmpty(i);
			String name   = item.getText(1);
			String dbname = item.getText(2);
			String ftype  = item.getText(3);
			String atype  = item.getText(4);
			String desc   = item.getText(7);
			
			TableField f = new TableField(name, dbname, TableField.getFieldType(ftype), TableField.getAggregationType(atype), tableinfo);
			f.setHidden("Y".equalsIgnoreCase(item.getText(5)));
			f.setExact("Y".equalsIgnoreCase(item.getText(6)));
			f.setDescription(desc);
			tableinfo.addField(f);
		}

		tableinfo.removeAllConditions();

		for (int i=0;i<wConditions.nrNonEmpty();i++)
		{
			TableItem item = wConditions.getNonEmpty(i);
			String name  = item.getText(1);
			String field = item.getText(2);
			String comp  = item.getText(3);
			String code  = item.getText(4);
			String desc  = item.getText(5);
			
			WhereCondition c = new WhereCondition(tableinfo, name, code);
			c.setField(tableinfo.findField(field));
			c.setComparator(comp);
			c.setDescription(desc);
			
			tableinfo.addCondition(c);
		}
		
		tablename = wName.getText();
		dispose();
	}
	
	private void get()
	{
		Database db = new Database(tableinfo.getDatabase());
		try
		{
			db.connect();
			Row r = db.getTableFields(tableinfo.getDBName());
			if (r!=null)
			{
				for (int i=0;i<r.size();i++)
				{
					Value v = r.getValue(i);
					TableItem item = new TableItem(wFields.table, SWT.NONE);
					item.setText(1, v.getName());
					item.setText(2, v.getName());
					item.setText(3, TableField.getFieldTypeDesc(TableField.TYPE_FIELD_DIMENSION));
					item.setText(4, TableField.getAggregationTypeDesc(TableField.TYPE_AGGREGATION_NONE));
					item.setText(5, "N");
				}
				
				wFields.removeEmptyRows();
				wFields.setRowNums();
				wFields.optWidth(true);
			}
		}
		catch(KettleException e)
		{
			MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
			mb.setMessage("Sorry, I couldn't get the table info because of an error: "+Const.CR+e.getMessage());
			mb.setText("ERROR");
			mb.open();
		}
		finally
		{
			db.disconnect();
		}
	}
	
	private void getTableName()
	{
		// New class: SelectTableDialog
		int connr = wConnection.getSelectionIndex();
		DatabaseMeta inf = schema.getConnection(connr);
					
		log.logDebug(toString(), "Looking at connection: "+inf.toString());
	
		DatabaseExplorerDialog std = new DatabaseExplorerDialog(shell, schema.props, SWT.NONE, inf, schema.databases);
		std.setSelectedTable(wTable.getText());
		String tableName = (String)std.open();
		if (tableName != null)
		{
			wTable.setText(tableName);
		}
	}

}
