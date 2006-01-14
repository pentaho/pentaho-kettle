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

package be.ibridge.kettle.trans.step.dbproc;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Point;
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
import be.ibridge.kettle.core.dialog.EnterSelectionDialog;
import be.ibridge.kettle.core.dialog.ErrorDialog;
import be.ibridge.kettle.core.exception.KettleDatabaseException;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.value.Value;
import be.ibridge.kettle.core.widget.TableView;
import be.ibridge.kettle.trans.TransMeta;
import be.ibridge.kettle.trans.step.BaseStepDialog;
import be.ibridge.kettle.trans.step.BaseStepMeta;
import be.ibridge.kettle.trans.step.StepDialogInterface;


public class DBProcDialog extends BaseStepDialog implements StepDialogInterface
{
	private CCombo       wConnection;

	private Button       wbProcName;
	private Label        wlProcName;
	private Text         wProcName;
	private FormData     fdlProcName, fdbProcName, fdProcName;

	private Label        wlResult;
	private Text         wResult;
	private FormData     fdlResult, fdResult;

	private Label        wlResultType;
	private CCombo       wResultType;
	private FormData     fdlResultType, fdResultType;

	private Label        wlFields;
	private TableView    wFields;
	private FormData     fdlFields, fdFields;

	private Button wGet;
	private Listener lsGet;

	private DBProcMeta input;

	public DBProcDialog(Shell parent, Object in, TransMeta transMeta, String sname)
	{
		super(parent, (BaseStepMeta)in, transMeta, sname);
		input=(DBProcMeta)in;
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
		changed = input.hasChanged();

		FormLayout formLayout = new FormLayout ();
		formLayout.marginWidth  = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText("DB Procedure Call");
		
		int middle = props.getMiddlePct();
		int margin=Const.MARGIN;

		// Stepname line
		wlStepname=new Label(shell, SWT.RIGHT);
		wlStepname.setText("Step name ");
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
		if (input.getDatabase()==null && transMeta.nrDatabases()==1) wConnection.select(0);
		wConnection.addModifyListener(lsMod);

		// ProcName line...
		// add button to get list of procedures on selected connection...
		wbProcName = new Button(shell, SWT.PUSH);
		wbProcName.setText(" &Find it... ");
		fdbProcName = new FormData();
		fdbProcName.right= new FormAttachment(100, 0);
		fdbProcName.top  = new FormAttachment(wConnection, margin*2);
		wbProcName.setLayoutData(fdbProcName);
		wbProcName.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent arg0)
			{
				DatabaseMeta dbInfo = transMeta.findDatabase(wConnection.getText());
				if (dbInfo!=null)
				{
					Database db = new Database(dbInfo);
					try
					{
						db.connect();
						String[] procs = db.getProcedures();
						if (procs!=null && procs.length>0)
						{
							EnterSelectionDialog esd = new EnterSelectionDialog(shell, props, procs, "Enter choice", "Select a procedure");
							String proc = esd.open();
							if (proc!=null)
							{
								wProcName.setText(proc);
							}
						}
						else
						{
							MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_INFORMATION);
							mb.setMessage("I couldn't find any procedures to select from.");
							mb.setText("Sorry");
							mb.open();
						}
					}
					catch(KettleDatabaseException dbe)
					{
						new ErrorDialog(shell, props, "Error", "Error getting list of available procedures", dbe);
					}
					finally
					{
						db.disconnect();
					}
				}
			}
		});
		
		wlProcName=new Label(shell, SWT.RIGHT);
		wlProcName.setText("Proc-name ");
 		props.setLook(wlProcName);
		fdlProcName=new FormData();
		fdlProcName.left = new FormAttachment(0, 0);
		fdlProcName.right= new FormAttachment(middle, -margin);
		fdlProcName.top  = new FormAttachment(wConnection, margin*2);
		wlProcName.setLayoutData(fdlProcName);
		
		wProcName=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wProcName);
		wProcName.addModifyListener(lsMod);
		fdProcName=new FormData();
		fdProcName.left = new FormAttachment(middle, 0);
		fdProcName.top  = new FormAttachment(wConnection, margin*2);
		fdProcName.right= new FormAttachment(wbProcName, -margin);
		wProcName.setLayoutData(fdProcName);
		
		
		// Result line...
		wlResult=new Label(shell, SWT.RIGHT);
		wlResult.setText("Result name ");
 		props.setLook(wlResult);
		fdlResult=new FormData();
		fdlResult.left = new FormAttachment(0, 0);
		fdlResult.right= new FormAttachment(middle, -margin);
		fdlResult.top  = new FormAttachment(wProcName, margin*2);
		wlResult.setLayoutData(fdlResult);
		wResult=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wResult);
		wResult.addModifyListener(lsMod);
		fdResult=new FormData();
		fdResult.left = new FormAttachment(middle, 0);
		fdResult.top  = new FormAttachment(wProcName, margin*2);
		fdResult.right= new FormAttachment(100, 0);
		wResult.setLayoutData(fdResult);


		// ResultType line
		wlResultType=new Label(shell, SWT.RIGHT);
		wlResultType.setText("Result type ");
 		props.setLook(wlResultType);
		fdlResultType=new FormData();
		fdlResultType.left = new FormAttachment(0, 0);
		fdlResultType.right= new FormAttachment(middle, -margin);
		fdlResultType.top  = new FormAttachment(wResult, margin);
		wlResultType.setLayoutData(fdlResultType);
		wResultType=new CCombo(shell, SWT.BORDER | SWT.READ_ONLY);
 		props.setLook(wResultType);
		String types[] = Value.getTypes();
		for (int x=0;x<types.length;x++) wResultType.add(types[x]);
		wResultType.select(0);
		wResultType.addModifyListener(lsMod);
		fdResultType=new FormData();
		fdResultType.left = new FormAttachment(middle, 0);
		fdResultType.top  = new FormAttachment(wResult, margin);
		fdResultType.right= new FormAttachment(100, 0);
		wResultType.setLayoutData(fdResultType);

		wlFields=new Label(shell, SWT.NONE);
		wlFields.setText("Parameters : ");
 		props.setLook(wlFields);
		fdlFields=new FormData();
		fdlFields.left = new FormAttachment(0, 0);
		fdlFields.top  = new FormAttachment(wResultType, margin);
		wlFields.setLayoutData(fdlFields);
		
		final int FieldsCols=3;
		final int FieldsRows=input.getArgument().length;
		
		ColumnInfo[] colinf=new ColumnInfo[FieldsCols];
		colinf[0]=new ColumnInfo("Name",       ColumnInfo.COLUMN_TYPE_TEXT,   false);
		colinf[1]=new ColumnInfo("Direction",  ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] { "IN", "OUT", "INOUT" } );
		colinf[2]=new ColumnInfo("Type",       ColumnInfo.COLUMN_TYPE_CCOMBO, Value.getTypes() );
		
		wFields=new TableView(shell, 
							  SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI, 
							  colinf, 
							  FieldsRows,  
							  lsMod,
							  props
							  );

		fdFields=new FormData();
		fdFields.left  = new FormAttachment(0, 0);
		fdFields.top   = new FormAttachment(wlFields, margin);
		fdFields.right = new FormAttachment(100, 0);
		fdFields.bottom= new FormAttachment(100, -50);
		wFields.setLayoutData(fdFields);


		// THE BUTTONS
		wOK=new Button(shell, SWT.PUSH);
		wOK.setText(" &OK ");
		wGet=new Button(shell, SWT.PUSH);
		wGet.setText(" &Get Fields ");
		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText(" &Cancel ");

		setButtonPositions(new Button[] { wOK, wGet, wCancel }, margin, wFields);

		// Add listeners
		lsOK       = new Listener() { public void handleEvent(Event e) { ok();        } };
		lsGet      = new Listener() { public void handleEvent(Event e) { get();        } };
		lsCancel   = new Listener() { public void handleEvent(Event e) { cancel();    } };
		
		wOK.addListener    (SWT.Selection, lsOK    );
		wGet.addListener   (SWT.Selection, lsGet   );
		wCancel.addListener(SWT.Selection, lsCancel);
		
		lsDef=new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } };
		
		wStepname.addSelectionListener( lsDef );
		
		// Detect X or ALT-F4 or something that kills this window...
		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { cancel(); } } );

		lsResize = new Listener() 
		{
			public void handleEvent(Event event) 
			{
				Point size = shell.getSize();
				wFields.setSize(size.x-10, size.y-50);
				wFields.table.setSize(size.x-10, size.y-50);
				wFields.redraw();
			}
		};
		shell.addListener(SWT.Resize, lsResize);

		// Set the shell size, based upon previous time...
		setSize();
		
		getData();
		input.setChanged(changed);

		shell.open();
		while (!shell.isDisposed())
		{
				if (!display.readAndDispatch()) display.sleep();
		}
		return stepname;
	}

	/**
	 * Copy information from the meta-data input to the dialog fields.
	 */ 
	public void getData()
	{
		int i;
		log.logDebug(toString(), "getting key info...");
		
		if (input.getArgument()!=null)
		for (i=0;i<input.getArgument().length;i++)
		{
			TableItem item = wFields.table.getItem(i);
			if (input.getArgument()[i]      !=null) item.setText(1, input.getArgument()[i]);
			if (input.getArgumentDirection()[i]   !=null) item.setText(2, input.getArgumentDirection()[i]);
			item.setText(3, Value.getTypeDesc(input.getArgumentType()[i]));
		}
		
		if (input.getDatabase()!=null)   wConnection.setText(input.getDatabase().getName());
		else if (transMeta.nrDatabases()==1)
		{
			wConnection.setText( transMeta.getDatabase(0).getName() );
		}
		if (input.getProcedure() !=null)   wProcName.setText(input.getProcedure());
		if (input.getResultName()!=null)   wResult.setText(input.getResultName());
		wResultType.setText(Value.getTypeDesc(input.getResultType()));

		wFields.setRowNums();
		wFields.optWidth(true);
		wStepname.selectAll();
	}
	
	private void cancel()
	{
		stepname=null;
		input.setChanged(changed);
		dispose();
	}
	
	private void ok()
	{
		int i;
	
		int nrargs = wFields.nrNonEmpty();

		input.allocate(nrargs);

		log.logDebug(toString(), "Found "+nrargs+" arguments");
		for (i=0;i<nrargs;i++)
		{
			TableItem item = wFields.getNonEmpty(i);
			input.getArgument()[i]       = item.getText(1);
			input.getArgumentDirection()[i]    = item.getText(2);
			input.getArgumentType()[i]   = Value.getType(item.getText(3));
		}

		input.setDatabase( transMeta.findDatabase(wConnection.getText()) );
		input.setProcedure( wProcName.getText() );
		input.setResultName( wResult.getText() );
		input.setResultType( Value.getType(wResultType.getText()) ); 

		stepname = wStepname.getText(); // return value

		if (input.getDatabase()==null)
		{
			MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
			mb.setMessage("Please select a valid connection!");
			mb.setText("ERROR");
			mb.open();
		}
		
		dispose();
	}

	private void get()
	{
		try
		{
			int i, count;
			Row r = transMeta.getPrevStepFields(stepname);
			if (r!=null)
			{
				Table table=wFields.table;
				count=table.getItemCount();
				for (i=0;i<r.size();i++)
				{
					Value v = r.getValue(i);
					TableItem ti = new TableItem(table, SWT.NONE);
					ti.setText(0, ""+(count+i+1));
					ti.setText(1, v.getName());
					ti.setText(2, "IN");
					ti.setText(3, v.getTypeDesc());
				}
				wFields.removeEmptyRows();
				wFields.setRowNums();
				wFields.optWidth(true);
			}
		}
		catch(KettleException ke)
		{
			new ErrorDialog(shell, props, "Get fields failed", "Unable to get fields from previous steps because of an error", ke);
		}

	}

	public String toString()
	{
		return this.getClass().getName();
	}
}
