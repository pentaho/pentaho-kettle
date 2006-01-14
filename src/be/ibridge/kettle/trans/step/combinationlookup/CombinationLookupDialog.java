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

package be.ibridge.kettle.trans.step.combinationlookup;

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
import be.ibridge.kettle.core.SQLStatement;
import be.ibridge.kettle.core.database.DatabaseMeta;
import be.ibridge.kettle.core.dialog.DatabaseExplorerDialog;
import be.ibridge.kettle.core.dialog.ErrorDialog;
import be.ibridge.kettle.core.dialog.SQLEditor;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.value.Value;
import be.ibridge.kettle.core.widget.TableView;
import be.ibridge.kettle.trans.TransMeta;
import be.ibridge.kettle.trans.step.BaseStepDialog;
import be.ibridge.kettle.trans.step.BaseStepMeta;
import be.ibridge.kettle.trans.step.StepDialogInterface;
import be.ibridge.kettle.trans.step.StepMeta;


public class CombinationLookupDialog extends BaseStepDialog implements StepDialogInterface
{
	private CCombo       wConnection;

	private Label        wlTable;
	private Button       wbTable;
	private Text         wTable;
	private FormData     fdlTable, fdbTable, fdTable;

	private Label        wlCommit;
	private Text         wCommit;
	private FormData     fdlCommit, fdCommit;

	private Label        wlTk;
	private Text         wTk;
	private FormData     fdlTk, fdTk;

	private Label        wlAutoinc;
	private Button       wAutoinc;
	private FormData     fdlAutoinc, fdAutoinc;

	private Label        wlSeq;
	private Text         wSeq;
	private FormData     fdlSeq, fdSeq;

	private Label        wlReplace;
	private Button       wReplace;
	private FormData     fdlReplace, fdReplace;
	
	private Label        wlHashcode;
	private Button       wHashcode;
	private FormData     fdlHashcode, fdHashcode;

	private Label        wlKey;
	private TableView    wKey;
	private FormData     fdlKey, fdKey;

	private Label        wlHashfield;
	private Text         wHashfield;
	private FormData     fdlHashfield, fdHashfield;

	private Button wGet, wCreate;
	private Listener lsGet, lsCreate;

	private CombinationLookupMeta input;
	
	private DatabaseMeta ci;

	public CombinationLookupDialog(Shell parent, Object in, TransMeta transMeta, String sname)
	{
		super(parent, (BaseStepMeta)in, transMeta, sname);
		input=(CombinationLookupMeta)in;
	}

	public String open()
	{
		Shell parent = getParent();
		Display display = parent.getDisplay();

		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN );
 		props.setLook(shell);

		FormLayout formLayout = new FormLayout ();
		formLayout.marginWidth  = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText("Combination Lookup / Update");
		
		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		ModifyListener lsMod = new ModifyListener() 
		{
			public void modifyText(ModifyEvent e) 
			{
				input.setChanged();
			}
		};
		backupChanged = input.hasChanged();
		ci = input.getDatabase();

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
		wConnection.addModifyListener(new ModifyListener()
			{
				public void modifyText(ModifyEvent e)
				{
					// We have new content: change ci connection:
					ci = transMeta.findDatabase(wConnection.getText());
					setAutoinc();
					setSequence();
				}
			}
		);
		
		// Table line...
		wlTable=new Label(shell, SWT.RIGHT);
		wlTable.setText("Target table ");
 		props.setLook(wlTable);
		fdlTable=new FormData();
		fdlTable.left = new FormAttachment(0, 0);
		fdlTable.right= new FormAttachment(middle, -margin);
		fdlTable.top  = new FormAttachment(wConnection, margin*2);
		wlTable.setLayoutData(fdlTable);
		wTable=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wTable);
		wTable.addModifyListener(lsMod);
		fdTable=new FormData();
		fdTable.left = new FormAttachment(middle, 0);
		fdTable.top  = new FormAttachment(wConnection, margin*2);
		fdTable.right= new FormAttachment(100, 0);
		wTable.setLayoutData(fdTable);
		wbTable=new Button(shell, SWT.PUSH| SWT.CENTER);
 		props.setLook(wbTable);
		wbTable.setText("&Browse...");
		fdbTable=new FormData();
		fdbTable.left = new FormAttachment(wTable, margin);
		fdbTable.right= new FormAttachment(100, 0);
		fdbTable.top  = new FormAttachment(wConnection, margin);
		wbTable.setLayoutData(fdbTable);

		// Commit size ...
		wlCommit=new Label(shell, SWT.RIGHT);
		wlCommit.setText("Commit size ");
 		props.setLook(wlCommit);
		fdlCommit=new FormData();
		fdlCommit.left = new FormAttachment(0, 0);
		fdlCommit.right= new FormAttachment(middle, -margin);
		fdlCommit.top  = new FormAttachment(wTable, margin);
		wlCommit.setLayoutData(fdlCommit);
		wCommit=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wCommit);
		wCommit.addModifyListener(lsMod);
		fdCommit=new FormData();
		fdCommit.left = new FormAttachment(middle, 0);
		fdCommit.top  = new FormAttachment(wTable, margin);
		fdCommit.right= new FormAttachment(100, 0);
		wCommit.setLayoutData(fdCommit);

		// The Lookup fields: usualy the key
		//
		wlKey=new Label(shell, SWT.NONE);
		wlKey.setText("Key fields (to look up row in table): ");
 		props.setLook(wlKey);
		fdlKey=new FormData();
		fdlKey.left  = new FormAttachment(0, 0);
		fdlKey.top   = new FormAttachment(wCommit, margin);
		fdlKey.right = new FormAttachment(100, 0);
		wlKey.setLayoutData(fdlKey);
		
		int nrKeyCols=2;
		int nrKeyRows=(input.getKeyField()!=null?input.getKeyField().length:1);
		
		ColumnInfo[] ciKey=new ColumnInfo[nrKeyCols];
		ciKey[0]=new ColumnInfo("Field in stream", ColumnInfo.COLUMN_TYPE_TEXT, false);
		ciKey[1]=new ColumnInfo("Dimension field", ColumnInfo.COLUMN_TYPE_TEXT, false);
		
		wKey=new TableView(shell, 
						      SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL, 
						      ciKey, 
						      nrKeyRows,  
						      lsMod,
							  props
						      );

		// THE BUTTONS
		wOK=new Button(shell, SWT.PUSH);
		wOK.setText(" &OK ");
		wGet=new Button(shell, SWT.PUSH);
		wGet.setText(" &Get Fields ");
		wCreate=new Button(shell, SWT.PUSH);
		wCreate.setText(" &SQL ");
		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText(" &Cancel ");

		setButtonPositions(new Button[] { wOK, wGet, wCreate, wCancel }, margin, null);

		// Technical key field:
		wlHashfield=new Label(shell, SWT.RIGHT);
		wlHashfield.setText("Hashcode field in table ");
 		props.setLook(wlHashfield);
		fdlHashfield=new FormData();
		fdlHashfield.left  = new FormAttachment(0, 0);
		fdlHashfield.right = new FormAttachment(middle, -margin);
		fdlHashfield.bottom= new FormAttachment(wOK, -2*margin);
		wlHashfield.setLayoutData(fdlHashfield);
		wHashfield=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wHashfield);
		wHashfield.addModifyListener(lsMod);
		fdHashfield=new FormData();
		fdHashfield.left  = new FormAttachment(middle, 0);
		fdHashfield.right = new FormAttachment(100, 0);
		fdHashfield.bottom= new FormAttachment(wOK, -2*margin);
		wHashfield.setLayoutData(fdHashfield);

		// Output the input rows or one (1) log-record? 
		wlHashcode=new Label(shell, SWT.RIGHT);
		wlHashcode.setText("Use hashcode?");
 		props.setLook(wlHashcode);
		fdlHashcode=new FormData();
		fdlHashcode.left  = new FormAttachment(0, 0);
		fdlHashcode.right = new FormAttachment(middle, -margin);
		fdlHashcode.bottom= new FormAttachment(wHashfield, -margin);
		wlHashcode.setLayoutData(fdlHashcode);
		wHashcode=new Button(shell, SWT.CHECK);
 		props.setLook(wHashcode);
		fdHashcode=new FormData();
		fdHashcode.left   = new FormAttachment(middle, 0);
		fdHashcode.right  = new FormAttachment(100, 0);
		fdHashcode.bottom = new FormAttachment(wHashfield, -margin);
		wHashcode.setLayoutData(fdHashcode);
		wHashcode.addSelectionListener(new SelectionAdapter() 
			{
				public void widgetSelected(SelectionEvent e) 
				{
					enableFields();
				}
			}
		);

		// Replace lookup fields in the output stream?
		wlReplace=new Label(shell, SWT.RIGHT);
		wlReplace.setText("Remove lookup fields?");
 		props.setLook(wlReplace);
		fdlReplace=new FormData();
		fdlReplace.left  = new FormAttachment(0, 0);
		fdlReplace.right = new FormAttachment(middle, -margin);
		fdlReplace.bottom= new FormAttachment(wHashcode, -margin);
		wlReplace.setLayoutData(fdlReplace);
		wReplace=new Button(shell, SWT.CHECK);
 		props.setLook(wReplace);
		fdReplace=new FormData();
		fdReplace.left  = new FormAttachment(middle, 0);
		fdReplace.bottom= new FormAttachment(wHashcode, -margin);
		fdReplace.right = new FormAttachment(100, 0);
		wReplace.setLayoutData(fdReplace);
		wReplace.addSelectionListener(new SelectionAdapter() 
			{
				public void widgetSelected(SelectionEvent e) 
				{
					enableFields();
				}
			}
		);

		// Sequence key field:
		wlSeq=new Label(shell, SWT.RIGHT);
		wlSeq.setText("Optional Sequence ");
 		props.setLook(wlSeq);
		fdlSeq=new FormData();
		fdlSeq.left  = new FormAttachment(0, 0);
		fdlSeq.right = new FormAttachment(middle, -margin);
		fdlSeq.bottom= new FormAttachment(wReplace, -margin);
		wlSeq.setLayoutData(fdlSeq);
		wSeq=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wSeq);
		wSeq.addModifyListener(lsMod);
		fdSeq=new FormData();
		fdSeq.left  = new FormAttachment(middle, 0);
		fdSeq.right = new FormAttachment(100, 0);
		fdSeq.bottom= new FormAttachment(wReplace, -margin);
		wSeq.setLayoutData(fdSeq);
		setSequence();

		// Use an autoincrement field?
		wlAutoinc=new Label(shell, SWT.RIGHT);
		wlAutoinc.setText("use auto increment field?");
 		props.setLook(wlAutoinc);
		fdlAutoinc=new FormData();
		fdlAutoinc.left  = new FormAttachment(0, 0);
		fdlAutoinc.right = new FormAttachment(middle, -margin);
		fdlAutoinc.bottom= new FormAttachment(wReplace, -margin);
		wlAutoinc.setLayoutData(fdlAutoinc);
		wAutoinc=new Button(shell, SWT.CHECK);
 		props.setLook(wAutoinc);
		fdAutoinc=new FormData();
		fdAutoinc.left  = new FormAttachment(middle, 0);
		fdAutoinc.right = new FormAttachment(100, 0);
		fdAutoinc.bottom= new FormAttachment(wReplace, -margin);
		wAutoinc.setLayoutData(fdAutoinc);
		wAutoinc.setToolTipText("If this field is disabled, get the next value from the indicated sequence."+Const.CR+"If no sequence is supplied, Kettle will generate the appropriate keys");

		// Technical key field:
		wlTk=new Label(shell, SWT.RIGHT);
		wlTk.setText("Technical key field ");
 		props.setLook(wlTk);
		fdlTk=new FormData();
		fdlTk.left   = new FormAttachment(0, 0);
		fdlTk.right  = new FormAttachment(middle, -margin);
		fdlTk.bottom = new FormAttachment(wAutoinc, -margin);
		wlTk.setLayoutData(fdlTk);
		wTk=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wTk);
		fdTk=new FormData();
		fdTk.left   = new FormAttachment(middle, 0);
		fdTk.bottom = new FormAttachment(wAutoinc, -margin);
		fdTk.right  = new FormAttachment(100, 0);
		wTk.setLayoutData(fdTk);

		fdKey=new FormData();
		fdKey.left  = new FormAttachment(0, 0);
		fdKey.top   = new FormAttachment(wlKey, margin);
		fdKey.right = new FormAttachment(100, 0);
		fdKey.bottom= new FormAttachment(wTk, -margin);
		wKey.setLayoutData(fdKey);


		// Add listeners
		lsOK       = new Listener() { public void handleEvent(Event e) { ok();         } };
		lsGet      = new Listener() { public void handleEvent(Event e) { get();        } };
		lsCreate   = new Listener() { public void handleEvent(Event e) { create();     } };
		lsCancel   = new Listener() { public void handleEvent(Event e) { cancel();     } };
		
		wOK.addListener    (SWT.Selection, lsOK    );
		wGet.addListener   (SWT.Selection, lsGet   );
		wCreate.addListener(SWT.Selection, lsCreate);
		wCancel.addListener(SWT.Selection, lsCancel);
		
		lsDef=new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } };
		
		wStepname.addSelectionListener( lsDef );
		
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
	
	public void enableFields()
	{
		wHashfield.setEnabled(wHashcode.getSelection());
		wHashfield.setVisible(wHashcode.getSelection());
		wlHashfield.setEnabled(wHashcode.getSelection());
	}

	public void setAutoinc()
	{
		boolean enable= ci==null || ci.supportsAutoinc();
		wlAutoinc.setEnabled(enable);
		wAutoinc.setEnabled(enable);
	}

	public void setSequence()
	{
		boolean seq = ci==null || ci.supportsSequences();
		wlSeq.setEnabled(seq);
		wSeq.setEnabled(seq);
	}

	/**
	 * Copy information from the meta-data input to the dialog fields.
	 */ 
	public void getData()
	{
		int i;
		log.logDebug(toString(), "getting key info...");
		
		if (input.getKeyField()!=null)
		for (i=0;i<input.getKeyField().length;i++)
		{
			TableItem item = wKey.table.getItem(i);
			if (input.getKeyField()[i]!=null)  item.setText(1, input.getKeyField()[i]);
			if (input.getKeyLookup()[i]!=null) item.setText(2, input.getKeyLookup()[i]);
		}
		
		wReplace.setSelection( input.replaceFields() );
		wHashcode.setSelection( input.useHash() );
		wHashfield.setEnabled(input.useHash());
		wHashfield.setVisible(input.useHash());
		wlHashfield.setEnabled(input.useHash());
		setAutoinc();

		if (input.getTablename()!=null)         wTable.setText( input.getTablename() );
		if (input.getTechnicalKeyField()!=null) wTk.setText(input.getTechnicalKeyField());

		wAutoinc.setSelection( input.isUseAutoinc() );

		if (input.getSequenceFrom()!=null)     wSeq.setText(input.getSequenceFrom());
		if (input.getDatabase()!=null)   wConnection.setText(input.getDatabase().getName());
		else if (transMeta.nrDatabases()==1)
		{
			wConnection.setText( transMeta.getDatabase(0).getName() );
		}
		if (input.getHashField()!=null)    wHashfield.setText(input.getHashField());
		
		wCommit.setText(""+input.getCommitSize());

		wKey.setRowNums();
		wKey.optWidth(true);
		
		wStepname.selectAll();
		
	}
	
	private void cancel()
	{
		stepname=null;
		input.setChanged(backupChanged);
		dispose();
	}
	
	private void ok()
	{
		getInfo(input);
		stepname = wStepname.getText(); // return value

		if (transMeta.findDatabase(wConnection.getText())==null)
		{
			MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
			mb.setMessage("Please select a valid connection!");
			mb.setText("ERROR");
			mb.open();
		}
		
		dispose();
	}
	
	private void getInfo(CombinationLookupMeta in)
	{
		int nrkeys         = wKey.nrNonEmpty();
		
		in.allocate(nrkeys);

		log.logDebug(toString(), "Found "+nrkeys+" keys");
		for (int i=0;i<nrkeys;i++)
		{
			TableItem item = wKey.getNonEmpty(i);
			in.getKeyField()[i]  = item.getText(1);
			in.getKeyLookup()[i] = item.getText(2);
		}

		in.setUseAutoinc( wAutoinc.getSelection() );
		in.setReplaceFields( wReplace.getSelection() );
		in.setUseHash( wHashcode.getSelection() );
		in.setHashField( wHashfield.getText() );
		in.setTablename( wTable.getText() ); 
		in.setTechnicalKeyField( wTk.getText() );
		in.setSequenceFrom( wSeq.getText() );
		in.setDatabase( transMeta.findDatabase(wConnection.getText()) );

		in.setCommitSize( Const.toInt(wCommit.getText(), 0) );
	}

	private void getTableName()
	{
		// New class: SelectTableDialog
		int connr = wConnection.getSelectionIndex();
		DatabaseMeta inf = transMeta.getDatabase(connr);
					
		log.logDebug(toString(), "Looking at connection: "+inf.toString());
	
		DatabaseExplorerDialog std = new DatabaseExplorerDialog(shell, props, SWT.NONE, inf, transMeta.getDatabases());
		std.setSelectedTable(wTable.getText());
		String tableName = (String)std.open();
		if (tableName != null)
		{
			wTable.setText(tableName);
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
					ti.setText(0, ""+(count+i+1));
					ti.setText(1, v.getName());
					ti.setText(2, v.getName());
					ti.setText(3, "N");
				}
				wKey.removeEmptyRows();
				wKey.setRowNums();
				wKey.optWidth(true);
			}
		}
		catch(KettleException ke)
		{
			new ErrorDialog(shell, props, "Get fields failed", "Unable to get fields from previous steps because of an error", ke);
		}
	}

	/** Generate code for create table...
	    Conversions done by Database
	 **/
	private void create()
	{
		try
		{
			// Gather info...
			CombinationLookupMeta info = new CombinationLookupMeta();
			getInfo(info);
			String name = stepname;  // new name might not yet be linked to other steps! 
			StepMeta stepMeta = new StepMeta(log, "CombinationLookup", name, info);
			Row prev = transMeta.getPrevStepFields(stepname);
			
			SQLStatement sql = info.getSQLStatements(transMeta, stepMeta, prev);
			if (!sql.hasError())
			{
				if (sql.hasSQL())
				{
					SQLEditor sqledit = new SQLEditor(shell, SWT.NONE, info.getDatabase(), transMeta.getDbCache(), sql.getSQL());
					sqledit.open();
				}
				else
				{
					MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_INFORMATION );
					mb.setMessage("No SQL needs to be executed to make this step function properly.");
					mb.setText("OK");
					mb.open(); 
				}
			}
			else
			{
				MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
				mb.setMessage(sql.getError());
				mb.setText("ERROR");
				mb.open(); 
			}
		}
		catch(KettleException ke)
		{
			new ErrorDialog(shell, props, "Get SQL failed", "Unable to create the SQL statement because of an error", ke);
		}
	}
	
	public String toString()
	{
		return this.getClass().getName();
	}

}
