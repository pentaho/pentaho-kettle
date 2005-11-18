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

package be.ibridge.kettle.trans.step.addsequence;

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
import org.eclipse.swt.widgets.Text;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.trans.TransMeta;
import be.ibridge.kettle.trans.step.BaseStepDialog;
import be.ibridge.kettle.trans.step.BaseStepMeta;
import be.ibridge.kettle.trans.step.StepDialogInterface;


public class AddSequenceDialog extends BaseStepDialog implements StepDialogInterface
{
	private Label        wlValuename;
	private Text         wValuename;
	private FormData     fdlValuename, fdValuename;

	private Label        wlUseDatabase;
	private Button       wUseDatabase;
	private FormData     fdlUseDatabase, fdUseDatabase;

	private CCombo       wConnection;

	private Label        wlSeqname;
	private Text         wSeqname;
	private FormData     fdlSeqname, fdSeqname;

	private Label        wlUseCounter;
	private Button       wUseCounter;
	private FormData     fdlUseCounter, fdUseCounter;

	private Label        wlStartAt;
	private Text         wStartAt;
	private FormData     fdlStartAt, fdStartAt;

	private Label        wlIncrBy;
	private Text         wIncrBy;
	private FormData     fdlIncrBy, fdIncrBy;

	private Label        wlMaxVal;
	private Text         wMaxVal;
	private FormData     fdlMaxVal, fdMaxVal;

	private AddSequenceMeta input;
	
	public AddSequenceDialog(Shell parent, Object in, TransMeta transMeta, String sname)
	{
		super(parent, (BaseStepMeta)in, transMeta, sname);
		input=(AddSequenceMeta)in;
	}

	public String open()
	{
		Shell parent = getParent();
		Display display = parent.getDisplay();

		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN);
 		props.setLook(		shell);
		
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
		shell.setText("Get sequence value from database");
		
		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		// Stepname line
		wlStepname=new Label(shell, SWT.RIGHT);
		wlStepname.setText("Step name ");
 		props.setLook(		wlStepname);
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

		// Valuename line
		wlValuename=new Label(shell, SWT.RIGHT);
		wlValuename.setText("Name of value ");
 		props.setLook(wlValuename);
		fdlValuename=new FormData();
		fdlValuename.left = new FormAttachment(0, 0);
		fdlValuename.top  = new FormAttachment(wStepname, margin);
		fdlValuename.right= new FormAttachment(middle, -margin);
		wlValuename.setLayoutData(fdlValuename);
		wValuename=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wValuename.setText("");
 		props.setLook(wValuename);
		wValuename.addModifyListener(lsMod);
		fdValuename=new FormData();
		fdValuename.left = new FormAttachment(middle, 0);
		fdValuename.top  = new FormAttachment(wStepname, margin);
		fdValuename.right= new FormAttachment(100, 0);
		wValuename.setLayoutData(fdValuename);

		wlUseDatabase=new Label(shell, SWT.RIGHT);
		wlUseDatabase.setText("Use DB to get sequence? ");
 		props.setLook(wlUseDatabase);
		fdlUseDatabase=new FormData();
		fdlUseDatabase.left = new FormAttachment(0, 0);
		fdlUseDatabase.top  = new FormAttachment(wValuename, margin);
		fdlUseDatabase.right= new FormAttachment(middle, -margin);
		wlUseDatabase.setLayoutData(fdlUseDatabase);
		wUseDatabase=new Button(shell, SWT.CHECK );
 		props.setLook(wUseDatabase);
		wUseDatabase.setToolTipText("Check this to use a database (probably Oracle) to get the value of a sequence.");
		fdUseDatabase=new FormData();
		fdUseDatabase.left = new FormAttachment(middle, 0);
		fdUseDatabase.top  = new FormAttachment(wValuename, margin);
		wUseDatabase.setLayoutData(fdUseDatabase);
		wUseDatabase.addSelectionListener(new SelectionAdapter() 
			{
				public void widgetSelected(SelectionEvent e) 
				{
					input.setUseDatabase(!input.isDatabaseUsed());
					input.setChanged();
					enableFields();
				}
			}
		);

		// Connection line
		// Connection line
		wConnection = addConnectionLine(shell, wUseDatabase, middle, margin);
		if (input.getDatabase()==null && transMeta.nrDatabases()==1) wConnection.select(0);
		wConnection.addModifyListener(lsMod);

		// Seqname line
		wlSeqname=new Label(shell, SWT.RIGHT);
		wlSeqname.setText("Sequence name ");
 		props.setLook(wlSeqname);
		fdlSeqname=new FormData();
		fdlSeqname.left = new FormAttachment(0, 0);
		fdlSeqname.right= new FormAttachment(middle, -margin);
		fdlSeqname.top  = new FormAttachment(wConnection, margin);
		wlSeqname.setLayoutData(fdlSeqname);
		wSeqname=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wSeqname.setText("");
 		props.setLook(wSeqname);
		wSeqname.addModifyListener(lsMod);
		fdSeqname=new FormData();
		fdSeqname.left = new FormAttachment(middle, 0);
		fdSeqname.top  = new FormAttachment(wConnection, margin);
		fdSeqname.right= new FormAttachment(100, 0);
		wSeqname.setLayoutData(fdSeqname);


		wlUseCounter=new Label(shell, SWT.RIGHT);
		wlUseCounter.setText("Use counter to calculate sequence? ");
 		props.setLook(wlUseCounter);
		fdlUseCounter=new FormData();
		fdlUseCounter.left = new FormAttachment(0, 0);
		fdlUseCounter.top  = new FormAttachment(wSeqname, margin);
		fdlUseCounter.right= new FormAttachment(middle, -margin);
		wlUseCounter.setLayoutData(fdlUseCounter);
		wUseCounter=new Button(shell, SWT.CHECK );
 		props.setLook(wUseCounter);
		wUseCounter.setToolTipText("Check this to use an internal Kettle counter to calculate the value of the sequence.");
		fdUseCounter=new FormData();
		fdUseCounter.left = new FormAttachment(middle, 0);
		fdUseCounter.top  = new FormAttachment(wSeqname, margin);
		wUseCounter.setLayoutData(fdUseCounter);
		wUseCounter.addSelectionListener(new SelectionAdapter() 
			{
				public void widgetSelected(SelectionEvent e) 
				{
					input.setUseCounter(!input.isCounterUsed() );
					input.setChanged();
					enableFields();
				}
			}
		);

		// StartAt line
		wlStartAt=new Label(shell, SWT.RIGHT);
		wlStartAt.setText("Start at value ");
 		props.setLook(wlStartAt);
		fdlStartAt=new FormData();
		fdlStartAt.left = new FormAttachment(0, 0);
		fdlStartAt.right= new FormAttachment(middle, -margin);
		fdlStartAt.top  = new FormAttachment(wUseCounter, margin);
		wlStartAt.setLayoutData(fdlStartAt);
		wStartAt=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wStartAt.setText("");
 		props.setLook(wStartAt);
		wStartAt.addModifyListener(lsMod);
		fdStartAt=new FormData();
		fdStartAt.left = new FormAttachment(middle, 0);
		fdStartAt.top  = new FormAttachment(wUseCounter, margin);
		fdStartAt.right= new FormAttachment(100, 0);
		wStartAt.setLayoutData(fdStartAt);

		// IncrBy line
		wlIncrBy=new Label(shell, SWT.RIGHT);
		wlIncrBy.setText("Increment by ");
 		props.setLook(wlIncrBy);
		fdlIncrBy=new FormData();
		fdlIncrBy.left = new FormAttachment(0, 0);
		fdlIncrBy.right= new FormAttachment(middle, -margin);
		fdlIncrBy.top  = new FormAttachment(wStartAt, margin);
		wlIncrBy.setLayoutData(fdlIncrBy);
		wIncrBy=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wIncrBy.setText("");
 		props.setLook(wIncrBy);
		wIncrBy.addModifyListener(lsMod);
		fdIncrBy=new FormData();
		fdIncrBy.left = new FormAttachment(middle, 0);
		fdIncrBy.top  = new FormAttachment(wStartAt, margin);
		fdIncrBy.right= new FormAttachment(100, 0);
		wIncrBy.setLayoutData(fdIncrBy);

		// MaxVal line
		wlMaxVal=new Label(shell, SWT.RIGHT);
		wlMaxVal.setText("Maximum value ");
 		props.setLook(wlMaxVal);
		fdlMaxVal=new FormData();
		fdlMaxVal.left = new FormAttachment(0, 0);
		fdlMaxVal.right= new FormAttachment(middle, -margin);
		fdlMaxVal.top  = new FormAttachment(wIncrBy, margin);
		wlMaxVal.setLayoutData(fdlMaxVal);
		wMaxVal=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wMaxVal.setText("");
 		props.setLook(wMaxVal);
		wMaxVal.addModifyListener(lsMod);
		fdMaxVal=new FormData();
		fdMaxVal.left = new FormAttachment(middle, 0);
		fdMaxVal.top  = new FormAttachment(wIncrBy, margin);
		fdMaxVal.right= new FormAttachment(100, 0);
		wMaxVal.setLayoutData(fdMaxVal);

		
		// THE BUTTONS
		wOK=new Button(shell, SWT.PUSH);
		wOK.setText(" &OK ");
		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText(" &Cancel ");

		setButtonPositions(new Button[] { wOK, wCancel }, margin, wMaxVal);

		// Add listeners
		lsOK       = new Listener() { public void handleEvent(Event e) { ok();        } };
		lsCancel   = new Listener() { public void handleEvent(Event e) { cancel();    } };
		
		wOK.addListener    (SWT.Selection, lsOK    );
		wCancel.addListener(SWT.Selection, lsCancel);
		
		lsDef=new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } };
		
		wStepname.addSelectionListener( lsDef );
		wValuename.addSelectionListener( lsDef);
		wSeqname.addSelectionListener( lsDef );
		wStartAt.addSelectionListener( lsDef );
		wIncrBy.addSelectionListener( lsDef );
		wMaxVal.addSelectionListener( lsDef );
		
		// Detect X or ALT-F4 or something that kills this window...
		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { cancel(); } } );

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
	
	public void enableFields()
	{
		//wlConnection.setEnabled(input.isDatabaseUsed());
		wConnection.setEnabled(input.isDatabaseUsed());
		//wbConnection.setEnabled(input.isDatabaseUsed());
		wlSeqname.setEnabled(input.isDatabaseUsed());
		wSeqname.setEnabled(input.isDatabaseUsed());
		
		wlStartAt.setEnabled(input.isCounterUsed());
		wStartAt.setEnabled(input.isCounterUsed());
		wlIncrBy.setEnabled(input.isCounterUsed());
		wIncrBy.setEnabled(input.isCounterUsed());
		wlMaxVal.setEnabled(input.isCounterUsed());
		wMaxVal.setEnabled(input.isCounterUsed());
	}

	/**
	 * Copy information from the meta-data input to the dialog fields.
	 */ 
	public void getData()
	{
		log.logDebug(toString(), "getting key info...");

		if (input.getValuename()!=null) wValuename.setText(input.getValuename());
		
		wUseDatabase.setSelection(input.isDatabaseUsed());
		if (input.getDatabase()!=null) wConnection.setText(input.getDatabase().getName());
		else if (transMeta.nrDatabases()==1)
		{
			wConnection.setText( transMeta.getDatabase(0).getName() );
		}
		if (input.getSequenceName()!=null) wSeqname.setText(input.getSequenceName());
		
		wUseCounter.setSelection(input.isCounterUsed());
		wStartAt.setText(""+input.getStartAt());
		wIncrBy.setText(""+input.getIncrementBy());
		wMaxVal.setText(""+input.getMaxValue());
		
		enableFields();
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
		stepname = wStepname.getText(); // return value

		String connection=wConnection.getText();
		input.setDatabase(transMeta.findDatabase(connection));
		input.setSequenceName(wSeqname.getText());
		input.setValuename(wValuename.getText());
		
		input.setStartAt    ( Const.toLong(wStartAt.getText(), 1L) );
		input.setIncrementBy( Const.toLong(wIncrBy.getText(), 1L) );
		input.setMaxValue   ( Const.toLong(wMaxVal.getText(), 99999999999L) );
		
		if (input.isDatabaseUsed() && transMeta.findDatabase(connection)==null)
		{
			MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
			mb.setMessage("Please select a valid connection!");
			mb.setText("ERROR");
			mb.open();
		}
		
		dispose();
	}
	
	public String toString()
	{
		return this.getClass().getName();
	}
}
