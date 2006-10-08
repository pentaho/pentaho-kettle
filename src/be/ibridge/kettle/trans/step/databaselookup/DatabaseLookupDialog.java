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
 * Created on 2-jul-2003
 *
 */

package be.ibridge.kettle.trans.step.databaselookup;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
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
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import be.ibridge.kettle.core.ColumnInfo;
import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.database.Database;
import be.ibridge.kettle.core.database.DatabaseMeta;
import be.ibridge.kettle.core.dialog.DatabaseExplorerDialog;
import be.ibridge.kettle.core.dialog.ErrorDialog;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.value.Value;
import be.ibridge.kettle.core.widget.TableView;
import be.ibridge.kettle.trans.TransMeta;
import be.ibridge.kettle.trans.step.BaseStepDialog;
import be.ibridge.kettle.trans.step.BaseStepMeta;
import be.ibridge.kettle.trans.step.StepDialogInterface;


public class DatabaseLookupDialog extends BaseStepDialog implements StepDialogInterface
{
	private CCombo       wConnection;

	private Label        wlCache;
	private Button       wCache;
	private FormData     fdlCache, fdCache;

	private Label        wlCachesize;
	private Text         wCachesize;
	private FormData     fdlCachesize, fdCachesize;

	private Label        wlKey;
	private TableView    wKey;
	private FormData     fdlKey, fdKey;

	private Label        wlTable;
	private Button       wbTable;
	private Text         wTable;
	private FormData     fdlTable, fdbTable, fdTable;

	private Label        wlReturn;
	private TableView    wReturn;
	private FormData     fdlReturn, fdReturn;

	private Label        wlOrderBy;
	private Text         wOrderBy;
	private FormData     fdlOrderBy, fdOrderBy;

    private Label        wlFailMultiple;
    private Button       wFailMultiple;
    private FormData     fdlFailMultiple, fdFailMultiple;

    private Label        wlEatRows;
    private Button       wEatRows;
    private FormData     fdlEatRows, fdEatRows;

	private Button wGet, wGetLU;
	private Listener lsGet, lsGetLU;

	private DatabaseLookupMeta input;

	public DatabaseLookupDialog(Shell parent, Object in, TransMeta transMeta, String sname)
	{
		super(parent, (BaseStepMeta)in, transMeta, sname);
		input=(DatabaseLookupMeta)in;
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
				input.setChanged();
			}
		};
		backupChanged = input.hasChanged();

		FormLayout formLayout = new FormLayout ();
		formLayout.marginWidth  = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText(Messages.getString("DatabaseLookupDialog.shell.Title")); //$NON-NLS-1$
		
		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		// Stepname line
		wlStepname=new Label(shell, SWT.RIGHT);
		wlStepname.setText(Messages.getString("DatabaseLookupDialog.Stepname.Label")); //$NON-NLS-1$
 		props.setLook(wlStepname);
		fdlStepname=new FormData();
		fdlStepname.left = new FormAttachment(0, 0);
		fdlStepname.right= new FormAttachment(middle, -margin);
		fdlStepname.top  = new FormAttachment(0, margin);
		wlStepname.setLayoutData(fdlStepname);
		wStepname=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wStepname.setText(stepname);
 		props.setLook(wStepname);
		wStepname.addModifyListener(lsMod);
		fdStepname=new FormData();
		fdStepname.left = new FormAttachment(middle, 0);
		fdStepname.top  = new FormAttachment(0, margin);
		fdStepname.right= new FormAttachment(100, 0);
		wStepname.setLayoutData(fdStepname);

		// Connection line
		wConnection = addConnectionLine(shell, wStepname, middle, margin);
		if (input.getDatabaseMeta()==null && transMeta.nrDatabases()==1) wConnection.select(0);
		wConnection.addModifyListener(lsMod);


		// Table line...
		wlTable=new Label(shell, SWT.RIGHT);
		wlTable.setText(Messages.getString("DatabaseLookupDialog.Lookuptable.Label")); //$NON-NLS-1$
 		props.setLook(wlTable);
		fdlTable=new FormData();
		fdlTable.left = new FormAttachment(0, 0);
		fdlTable.right= new FormAttachment(middle, -margin);
		fdlTable.top  = new FormAttachment(wConnection, margin*2);
		wlTable.setLayoutData(fdlTable);

		wbTable=new Button(shell, SWT.PUSH| SWT.CENTER);
 		props.setLook(wbTable);
		wbTable.setText(Messages.getString("DatabaseLookupDialog.Browse.Button")); //$NON-NLS-1$
		fdbTable=new FormData();
		fdbTable.right= new FormAttachment(100, 0);
		fdbTable.top  = new FormAttachment(wConnection, margin);
		wbTable.setLayoutData(fdbTable);

		wTable=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wTable);
		wTable.addModifyListener(lsMod);
		fdTable=new FormData();
		fdTable.left = new FormAttachment(middle, 0);
		fdTable.top  = new FormAttachment(wConnection, margin*2);
		fdTable.right= new FormAttachment(wbTable, -margin);
		wTable.setLayoutData(fdTable);

		// Cache?
		wlCache=new Label(shell, SWT.RIGHT);
		wlCache.setText(Messages.getString("DatabaseLookupDialog.Cache.Label")); //$NON-NLS-1$
 		props.setLook(wlCache);
		fdlCache=new FormData();
		fdlCache.left = new FormAttachment(0, 0);
		fdlCache.right= new FormAttachment(middle, -margin);
		fdlCache.top  = new FormAttachment(wbTable, margin);
		wlCache.setLayoutData(fdlCache);
		wCache=new Button(shell, SWT.CHECK);
 		props.setLook(wCache);
		fdCache=new FormData();
		fdCache.left = new FormAttachment(middle, 0);
		fdCache.top  = new FormAttachment(wbTable, margin);
		wCache.setLayoutData(fdCache);
		wCache.addSelectionListener(new SelectionAdapter() 
			{
				public void widgetSelected(SelectionEvent e) 
				{
					input.setChanged();
					
					wCachesize.setEnabled(wCache.getSelection());
					wlCachesize.setEnabled(wCache.getSelection());
				}
			}
		);

		// Cache size line
		wlCachesize=new Label(shell, SWT.RIGHT);
		wlCachesize.setText(Messages.getString("DatabaseLookupDialog.Cachesize.Label")); //$NON-NLS-1$
 		props.setLook(wlCachesize);
		wlCachesize.setEnabled(input.isCached());
		fdlCachesize=new FormData();
		fdlCachesize.left   = new FormAttachment(0, 0);
		fdlCachesize.right  = new FormAttachment(middle, -margin);
		fdlCachesize.top    = new FormAttachment(wlCache, margin);
		wlCachesize.setLayoutData(fdlCachesize);
		wCachesize=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wCachesize);
		wCachesize.setEnabled(input.isCached());
		wCachesize.addModifyListener(lsMod);
		fdCachesize=new FormData();
		fdCachesize.left   = new FormAttachment(middle, 0);
		fdCachesize.right  = new FormAttachment(100, 0);
		fdCachesize.top    = new FormAttachment(wlCache, margin);
		wCachesize.setLayoutData(fdCachesize);


		wlKey=new Label(shell, SWT.NONE);
		wlKey.setText(Messages.getString("DatabaseLookupDialog.Keys.Label")); //$NON-NLS-1$
 		props.setLook(wlKey);
		fdlKey=new FormData();
		fdlKey.left  = new FormAttachment(0, 0);
		fdlKey.top   = new FormAttachment(wCachesize, margin);
		wlKey.setLayoutData(fdlKey);

		int nrKeyCols=4;
		int nrKeyRows=(input.getStreamKeyField1()!=null?input.getStreamKeyField1().length:1);
		
		ColumnInfo[] ciKey=new ColumnInfo[nrKeyCols];
		ciKey[0]=new ColumnInfo(Messages.getString("DatabaseLookupDialog.ColumnInfo.Tablefield"),  ColumnInfo.COLUMN_TYPE_TEXT,   false); //$NON-NLS-1$
		ciKey[1]=new ColumnInfo(Messages.getString("DatabaseLookupDialog.ColumnInfo.Comparator"),   ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] { "=", "<>", "<", "<=", ">", ">=", "LIKE", "BETWEEN", "IS NULL", "IS NOT NULL" } ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$ //$NON-NLS-10$ //$NON-NLS-11$
		ciKey[2]=new ColumnInfo(Messages.getString("DatabaseLookupDialog.ColumnInfo.Field1"),       ColumnInfo.COLUMN_TYPE_TEXT,   false); //$NON-NLS-1$
		ciKey[3]=new ColumnInfo(Messages.getString("DatabaseLookupDialog.ColumnInfo.Field2"),       ColumnInfo.COLUMN_TYPE_TEXT,   false); //$NON-NLS-1$
		
		wKey=new TableView(shell, 
						      SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL, 
						      ciKey, 
						      nrKeyRows,  
						      lsMod,
							  props
						      );

		fdKey=new FormData();
		fdKey.left  = new FormAttachment(0, 0);
		fdKey.top   = new FormAttachment(wlKey, margin);
		fdKey.right = new FormAttachment(100, 0);
		fdKey.bottom= new FormAttachment(wlKey, 150);
		wKey.setLayoutData(fdKey);

		// THE BUTTONS
		wOK=new Button(shell, SWT.PUSH);
		wOK.setText(Messages.getString("System.Button.OK")); //$NON-NLS-1$
		wGet=new Button(shell, SWT.PUSH);
		wGet.setText(Messages.getString("DatabaseLookupDialog.GetFields.Button")); //$NON-NLS-1$
		wGetLU=new Button(shell, SWT.PUSH);
		wGetLU.setText(Messages.getString("DatabaseLookupDialog.GetLookupFields.Button")); //$NON-NLS-1$
		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText(Messages.getString("System.Button.Cancel")); //$NON-NLS-1$

		setButtonPositions(new Button[] { wOK, wGet, wGetLU, wCancel }, margin, null);

		// OderBy line
		wlOrderBy=new Label(shell, SWT.RIGHT);
		wlOrderBy.setText(Messages.getString("DatabaseLookupDialog.Orderby.Label")); //$NON-NLS-1$
 		props.setLook(wlOrderBy);
		fdlOrderBy=new FormData();
		fdlOrderBy.left   = new FormAttachment(0, 0);
		fdlOrderBy.right  = new FormAttachment(middle, -margin);
		fdlOrderBy.bottom = new FormAttachment(wOK, -2*margin);
		wlOrderBy.setLayoutData(fdlOrderBy);
		wOrderBy=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wOrderBy);
		fdOrderBy=new FormData();
		fdOrderBy.left   = new FormAttachment(middle, 0);
		fdOrderBy.bottom = new FormAttachment(wOK, -2*margin);
		fdOrderBy.right  = new FormAttachment(100, 0);
		wOrderBy.setLayoutData(fdOrderBy);

        // FailMultiple?
        wlFailMultiple=new Label(shell, SWT.RIGHT);
        wlFailMultiple.setText(Messages.getString("DatabaseLookupDialog.FailMultiple.Label")); //$NON-NLS-1$
        props.setLook(wlFailMultiple);
        fdlFailMultiple=new FormData();
        fdlFailMultiple.left   = new FormAttachment(0, 0);
        fdlFailMultiple.right  = new FormAttachment(middle, -margin);
        fdlFailMultiple.bottom = new FormAttachment(wOrderBy, -margin);
        wlFailMultiple.setLayoutData(fdlFailMultiple);
        wFailMultiple=new Button(shell, SWT.CHECK);
        props.setLook(wFailMultiple);
        fdFailMultiple=new FormData();
        fdFailMultiple.left   = new FormAttachment(middle, 0);
        fdFailMultiple.bottom = new FormAttachment(wOrderBy, -margin);
        wFailMultiple.setLayoutData(fdFailMultiple);
        wFailMultiple.addSelectionListener(new SelectionAdapter()
            {
                public void widgetSelected(SelectionEvent e)
                {
                    setFlags();
                }
            }
        );

        // EatRows?
        wlEatRows=new Label(shell, SWT.RIGHT);
        wlEatRows.setText(Messages.getString("DatabaseLookupDialog.EatRows.Label")); //$NON-NLS-1$
        props.setLook(wlEatRows);
        fdlEatRows=new FormData();
        fdlEatRows.left   = new FormAttachment(0, 0);
        fdlEatRows.right  = new FormAttachment(middle, -margin);
        fdlEatRows.bottom = new FormAttachment(wFailMultiple, -margin);
        wlEatRows.setLayoutData(fdlEatRows);
        wEatRows=new Button(shell, SWT.CHECK);
        props.setLook(wEatRows);
        fdEatRows=new FormData();
        fdEatRows.left   = new FormAttachment(middle, 0);
        fdEatRows.bottom = new FormAttachment(wFailMultiple, -margin);
        wEatRows.setLayoutData(fdEatRows);
        wEatRows.addSelectionListener(new SelectionAdapter()
            {
                public void widgetSelected(SelectionEvent e)
                {
                    setFlags();
                }
            }
        );

        
        // THE UPDATE/INSERT TABLE
        wlReturn=new Label(shell, SWT.NONE);
        wlReturn.setText(Messages.getString("DatabaseLookupDialog.Return.Label")); //$NON-NLS-1$
        props.setLook(wlReturn);
        fdlReturn=new FormData();
        fdlReturn.left  = new FormAttachment(0, 0);
        fdlReturn.top   = new FormAttachment(wKey, margin);
        wlReturn.setLayoutData(fdlReturn);
        
        int UpInsCols=4;
        int UpInsRows= (input.getReturnValueField()!=null?input.getReturnValueField().length:1);
        
        ColumnInfo[] ciReturn=new ColumnInfo[UpInsCols];
        ciReturn[0]=new ColumnInfo(Messages.getString("DatabaseLookupDialog.ColumnInfo.Field"),    ColumnInfo.COLUMN_TYPE_TEXT,   false); //$NON-NLS-1$
        ciReturn[1]=new ColumnInfo(Messages.getString("DatabaseLookupDialog.ColumnInfo.Newname"), ColumnInfo.COLUMN_TYPE_TEXT,   false); //$NON-NLS-1$
        ciReturn[2]=new ColumnInfo(Messages.getString("DatabaseLookupDialog.ColumnInfo.Default"),  ColumnInfo.COLUMN_TYPE_TEXT,   false); //$NON-NLS-1$
        ciReturn[3]=new ColumnInfo(Messages.getString("DatabaseLookupDialog.ColumnInfo.Type"),     ColumnInfo.COLUMN_TYPE_CCOMBO, Value.getTypes()); //$NON-NLS-1$
        
        wReturn=new TableView(shell, 
                              SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL, 
                              ciReturn, 
                              UpInsRows,  
                              lsMod,
                              props
                              );

        fdReturn=new FormData();
        fdReturn.left  = new FormAttachment(0, 0);
        fdReturn.top   = new FormAttachment(wlReturn, margin);
        fdReturn.right = new FormAttachment(100, 0);
        fdReturn.bottom= new FormAttachment(wEatRows, -margin);
        wReturn.setLayoutData(fdReturn);


        
		// Add listeners
		lsOK       = new Listener() { public void handleEvent(Event e) { ok();        } };
		lsGet      = new Listener() { public void handleEvent(Event e) { get();       } };
		lsGetLU    = new Listener() { public void handleEvent(Event e) { getlookup(); } };
		lsCancel   = new Listener() { public void handleEvent(Event e) { cancel();    } };
		
		wOK.addListener    (SWT.Selection, lsOK    );
		wGet.addListener   (SWT.Selection, lsGet   );
		wGetLU.addListener (SWT.Selection, lsGetLU );
		wCancel.addListener(SWT.Selection, lsCancel);
		
		lsDef=new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } };
		
		wStepname.addSelectionListener( lsDef );
		wTable.addSelectionListener( lsDef );
		wOrderBy.addSelectionListener( lsDef );
		wCachesize.addSelectionListener( lsDef );
		
		// Detect X or ALT-F4 or something that kills this window...
		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { cancel(); } } );

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

		// Set the shell size, based upon previous time...
		setSize();
				
		getData();
		input.setChanged(backupChanged);

		shell.open();
		while (!shell.isDisposed())
		{
				if (!display.readAndDispatch()) display.sleep();
		}
		return stepname;
	}

	private void setFlags()
    {
        wlOrderBy.setEnabled( !wFailMultiple.getSelection() );
        wOrderBy.setEnabled( !wFailMultiple.getSelection() );
    }

    /**
	 * Copy information from the meta-data input to the dialog fields.
	 */ 
	public void getData()
	{
		int i;
		log.logDebug(toString(), Messages.getString("DatabaseLookupDialog.Log.GettingKeyInfo")); //$NON-NLS-1$
		
		wCache.setSelection(input.isCached());
		wCachesize.setText(""+input.getCacheSize()); //$NON-NLS-1$
		
		if (input.getStreamKeyField1()!=null)
		for (i=0;i<input.getStreamKeyField1().length;i++)
		{
			TableItem item = wKey.table.getItem(i);
			if (input.getTableKeyField()[i]   !=null) item.setText(1, input.getTableKeyField()[i]);
			if (input.getKeyCondition()[i]!=null) item.setText(2, input.getKeyCondition()[i]);
			if (input.getStreamKeyField1()[i]         !=null) item.setText(3, input.getStreamKeyField1()[i]);
			if (input.getStreamKeyField2()[i]        !=null) item.setText(4, input.getStreamKeyField2()[i]);
		}
		
		if (input.getReturnValueField()!=null)
		for (i=0;i<input.getReturnValueField().length;i++)
		{
			TableItem item = wReturn.table.getItem(i);
			if (input.getReturnValueField()[i]!=null     ) item.setText(1, input.getReturnValueField()[i]);
			if (input.getReturnValueNewName()[i]!=null && !input.getReturnValueNewName()[i].equals(input.getReturnValueField()[i]))
				item.setText(2, input.getReturnValueNewName()[i]);
			
			if (input.getReturnValueDefault()[i]!=null  ) item.setText(3, input.getReturnValueDefault()[i]);
			item.setText(4, Value.getTypeDesc(input.getReturnValueDefaultType()[i]));
		}
		
		if (input.getTablename()!=null)        wTable.setText( input.getTablename() );
		if (input.getDatabaseMeta()!=null)   wConnection.setText(input.getDatabaseMeta().getName());
		else if (transMeta.nrDatabases()==1)
		{
			wConnection.setText( transMeta.getDatabase(0).getName() );
		}
		if (input.getOrderByClause()!=null)      wOrderBy.setText(input.getOrderByClause());
		wFailMultiple.setSelection(input.isFailingOnMultipleResults());
		wEatRows.setSelection(input.isEatingRowOnLookupFailure());
        
		wStepname.selectAll();
		wKey.setRowNums();
		wKey.optWidth(true);
		wReturn.setRowNums();
		wReturn.optWidth(true);
        
        setFlags();
	}
	
	private void cancel()
	{
		stepname=null;
		input.setChanged(backupChanged);
		dispose();
	}
	
	private void ok()
	{	
		int nrkeys = wKey.nrNonEmpty();
		int nrfields = wReturn.nrNonEmpty();
		
		input.allocate(nrkeys, nrfields);
		
		input.setCached( wCache.getSelection() );
		input.setCacheSize( Const.toInt(wCachesize.getText(), 0) );
		
		log.logDebug(toString(), Messages.getString("DatabaseLookupDialog.Log.FoundKeys",String.valueOf(nrkeys))); //$NON-NLS-1$ //$NON-NLS-2$
		for (int i=0;i<nrkeys;i++)
		{
			TableItem item = wKey.getNonEmpty(i);
			input.getTableKeyField()[i]    = item.getText(1);
			input.getKeyCondition()[i]     = item.getText(2);
			input.getStreamKeyField1()[i]  = item.getText(3);
			input.getStreamKeyField2()[i]  = item.getText(4);
		}

		log.logDebug(toString(), Messages.getString("DatabaseLookupDialog.Log.FoundFields",String.valueOf(nrfields))); //$NON-NLS-1$ //$NON-NLS-2$
		for (int i=0;i<nrfields;i++)
		{
			TableItem item        = wReturn.getNonEmpty(i);
			input.getReturnValueField()[i]        = item.getText(1);
			input.getReturnValueNewName()[i]    = item.getText(2);
			if (input.getReturnValueNewName()[i]==null || input.getReturnValueNewName()[i].length()==0)
				input.getReturnValueNewName()[i] = input.getReturnValueField()[i];
			
			input.getReturnValueDefault()[i]     = item.getText(3);
			input.getReturnValueDefaultType()[i] = Value.getType(item.getText(4));
			
			if (input.getReturnValueDefaultType()[i]<0)
			{
				input.getReturnValueDefaultType()[i]=Value.VALUE_TYPE_STRING;
			}
		}
		
		input.setTablename( wTable.getText() ); 
		input.setDatabaseMeta( transMeta.findDatabase(wConnection.getText()) );
		input.setOrderByClause( wOrderBy.getText() );
		input.setFailingOnMultipleResults( wFailMultiple.getSelection() );
		input.setEatingRowOnLookupFailure( wEatRows.getSelection() );
        
		stepname = wStepname.getText(); // return value

		if (transMeta.findDatabase(wConnection.getText())==null)
		{
			MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
			mb.setMessage(Messages.getString("DatabaseLookupDialog.InvalidConnection.DialogMessage")); //$NON-NLS-1$
			mb.setText(Messages.getString("DatabaseLookupDialog.InvalidConnection.DialogTitle")); //$NON-NLS-1$
			mb.open();
		}
		
		dispose();
	}

	private void getTableName()
	{
		DatabaseMeta inf=null;
		// New class: SelectTableDialog
		int connr = wConnection.getSelectionIndex();
		if (connr>=0) inf = transMeta.getDatabase(connr);
		
		if (inf!=null)
		{
			log.logDebug(toString(), Messages.getString("DatabaseLookupDialog.Log.LookingAtConnection")+inf.toString()); //$NON-NLS-1$
		
			DatabaseExplorerDialog std = new DatabaseExplorerDialog(shell, SWT.NONE, inf, transMeta.getDatabases());
			std.setSelectedTable(wTable.getText());
			String tableName = (String)std.open();
			if (tableName != null)
			{
				wTable.setText(tableName);
			}
		}
		else
		{
			MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
			mb.setMessage(Messages.getString("DatabaseLookupDialog.InvalidConnection.DialogMessage")); //$NON-NLS-1$
			mb.setText(Messages.getString("DatabaseLookupDialog.InvalidConnection.DialogTitle")); //$NON-NLS-1$
			mb.open(); 
		}
	}

	private void get()
	{
		try
		{
			int i, count;
			Row r = transMeta.getPrevStepFields(stepname);
			if (r!=null)
			{
				Table table=wKey.table;
				count=table.getItemCount();
				for (i=0;i<r.size();i++)
				{
					Value v = r.getValue(i);
					TableItem ti = new TableItem(table, SWT.NONE);
					ti.setText(0, ""+(count+i+1)); //$NON-NLS-1$
					ti.setText(1, v.getName());
					ti.setText(2, "="); //$NON-NLS-1$
					ti.setText(3, v.getName());
					ti.setText(4, ""); //$NON-NLS-1$
				}
				wKey.removeEmptyRows();
				wKey.setRowNums();
				wKey.optWidth(true);
			}
		}
		catch(KettleException ke)
		{
			new ErrorDialog(shell, Messages.getString("DatabaseLookupDialog.GetFieldsFailed.DialogTitle"), Messages.getString("DatabaseLookupDialog.GetFieldsFailed.DialogMessage"), ke); //$NON-NLS-1$ //$NON-NLS-2$
		}

	}
	
	private void getlookup()
	{
		int i;
		
		DatabaseMeta ci = transMeta.findDatabase(wConnection.getText());
		if (ci!=null)
		{
			Database db = new Database(ci);
			try
			{
				db.connect();
				
				String tablename = wTable.getText();
				if (tablename!=null && tablename.length()!=0)
				{
					Row r = db.getTableFields(tablename);
					if (r!=null)
					{
						log.logDebug(toString(), Messages.getString("DatabaseLookupDialog.Log.FoundTableFields")+tablename+" --> "+r.toStringMeta()); //$NON-NLS-1$ //$NON-NLS-2$

                        Table table = wReturn.table;
						int count = table.getItemCount();
						for (i=0;i<r.size();i++)
						{
							Value v = r.getValue(i);
							TableItem ti = new TableItem(table, SWT.NONE);
							ti.setText(0, ""+(count+i+1)); //$NON-NLS-1$
							ti.setText(1, v.getName());
							ti.setText(2, v.getName());
							ti.setText(3, ""); //$NON-NLS-1$
							ti.setText(4, v.getTypeDesc());
						}
						wReturn.removeEmptyRows();
						wReturn.setRowNums();
						wReturn.optWidth(true);
					}
					else
					{
						MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
						mb.setMessage(Messages.getString("DatabaseLookupDialog.CouldNotReadTableInfo.DialogMessage")); //$NON-NLS-1$
						mb.setText(Messages.getString("DatabaseLookupDialog.CouldNotReadTableInfo.DialogTitle")); //$NON-NLS-1$
						mb.open(); 
					}
				}
			}
			catch(KettleException e)
			{
				MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
				mb.setMessage(Messages.getString("DatabaseLookupDialog.ErrorOccurred.DialogMessage")+Const.CR+e.getMessage()); //$NON-NLS-1$
				mb.setText(Messages.getString("DatabaseLookupDialog.ErrorOccurred.DialogTitle")); //$NON-NLS-1$
				mb.open(); 
			}
		}
		else
		{
			MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
			mb.setMessage(Messages.getString("DatabaseLookupDialog.InvalidConnectionName.DialogMessage")); //$NON-NLS-1$
			mb.setText(Messages.getString("DatabaseLookupDialog.InvalidConnectionName.DialogTitle")); //$NON-NLS-1$
			mb.open(); 
		}
	}
	
	public String toString()
	{
		return this.getClass().getName();
	}
}
