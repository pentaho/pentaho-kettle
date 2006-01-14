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

package be.ibridge.kettle.trans.step.streamlookup;

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
import be.ibridge.kettle.core.dialog.ErrorDialog;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.value.Value;
import be.ibridge.kettle.core.widget.TableView;
import be.ibridge.kettle.trans.TransMeta;
import be.ibridge.kettle.trans.step.BaseStepDialog;
import be.ibridge.kettle.trans.step.BaseStepMeta;
import be.ibridge.kettle.trans.step.StepDialogInterface;
import be.ibridge.kettle.trans.step.StepMeta;


public class StreamLookupDialog extends BaseStepDialog implements StepDialogInterface
{
	private Label        wlStep;
	private CCombo       wStep;
	private FormData     fdlStep, fdStep;

	private Label        wlKey;
	private TableView    wKey;
	private FormData     fdlKey, fdKey;

	private Label        wlReturn;
	private TableView    wReturn;
	private FormData     fdlReturn, fdReturn;
	
	private Label        wlSortedInput;
	private Button       wSortedInput;
	private FormData     fdlSortedInput, fdSortedInput;

	private StreamLookupMeta input;

    private Button       wGetLU;
    private Listener     lsGetLU;
    
	public StreamLookupDialog(Shell parent, Object in, TransMeta transMeta, String sname)
	{
		super(parent, (BaseStepMeta)in, transMeta, sname);
		input=(StreamLookupMeta)in;
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
		shell.setText("Stream Value Lookup");
		
		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

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

		// Source step line...
		wlStep=new Label(shell, SWT.RIGHT);
		wlStep.setText("Source step ");
 		props.setLook(wlStep);
		fdlStep=new FormData();
		fdlStep.left = new FormAttachment(0, 0);
		fdlStep.right= new FormAttachment(middle, -margin);
		fdlStep.top  = new FormAttachment(wStepname, margin*2);
		wlStep.setLayoutData(fdlStep);
		wStep=new CCombo(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wStep);
		
		for (int i=0;i<transMeta.findNrPrevSteps(stepname, true);i++)
		{
			StepMeta stepMeta = transMeta.findPrevStep(stepname, i, true);
			wStep.add(stepMeta.getName());
		}
		// transMeta.getInfoStep()
		
		wStep.addModifyListener(lsMod);

		fdStep=new FormData();
		fdStep.left = new FormAttachment(middle, 0);
		fdStep.top  = new FormAttachment(wStepname, margin*2);
		fdStep.right= new FormAttachment(100, 0);
		wStep.setLayoutData(fdStep);

		wlKey=new Label(shell, SWT.NONE);
		wlKey.setText("The key(s) to look up the value(s): ");
 		props.setLook(wlKey);
		fdlKey=new FormData();
		fdlKey.left  = new FormAttachment(0, 0);
		fdlKey.top   = new FormAttachment(wStep, margin);
		wlKey.setLayoutData(fdlKey);

		int nrKeyCols=2;
		int nrKeyRows=(input.getKeystream()!=null?input.getKeystream().length:1);
		
		ColumnInfo[] ciKey=new ColumnInfo[nrKeyCols];
		ciKey[0]=new ColumnInfo("Field",        ColumnInfo.COLUMN_TYPE_TEXT,   false);
		ciKey[1]=new ColumnInfo("LookupField",  ColumnInfo.COLUMN_TYPE_TEXT,   false);
		
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
		fdKey.bottom= new FormAttachment(wlKey, 180);
		wKey.setLayoutData(fdKey);

		// THE UPDATE/INSERT TABLE
		wlReturn=new Label(shell, SWT.NONE);
		wlReturn.setText("Specify the fields to retrieve :");
 		props.setLook(wlReturn);
		fdlReturn=new FormData();
		fdlReturn.left  = new FormAttachment(0, 0);
		fdlReturn.top   = new FormAttachment(wKey, margin);
		wlReturn.setLayoutData(fdlReturn);
		
		int UpInsCols=4;
		int UpInsRows= (input.getValue()!=null?input.getValue().length:1);
		
		ColumnInfo[] ciReturn=new ColumnInfo[UpInsCols];
		ciReturn[0]=new ColumnInfo("Field",    ColumnInfo.COLUMN_TYPE_TEXT,   false);
		ciReturn[1]=new ColumnInfo("New name", ColumnInfo.COLUMN_TYPE_TEXT,   false);
		ciReturn[2]=new ColumnInfo("Default",  ColumnInfo.COLUMN_TYPE_TEXT,   false);
		ciReturn[3]=new ColumnInfo("Type",     ColumnInfo.COLUMN_TYPE_CCOMBO, Value.getTypes() );
		
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
		fdReturn.bottom= new FormAttachment(100, -75);
		wReturn.setLayoutData(fdReturn);

		wlSortedInput=new Label(shell, SWT.RIGHT);
		wlSortedInput.setText("Both streams are sorted on lookup key? ");
 		props.setLook(wlSortedInput);
		fdlSortedInput=new FormData();
		fdlSortedInput.left = new FormAttachment(0, 0);
		fdlSortedInput.top  = new FormAttachment(wReturn, margin);
		fdlSortedInput.right= new FormAttachment(middle, -margin);
		wlSortedInput.setLayoutData(fdlSortedInput);
		wSortedInput=new Button(shell, SWT.CHECK );
 		props.setLook(wSortedInput);
		fdSortedInput=new FormData();
		fdSortedInput.left = new FormAttachment(middle, 0);
		fdSortedInput.top  = new FormAttachment(wReturn, margin);
		fdSortedInput.right= new FormAttachment(100, 0);
		wSortedInput.setLayoutData(fdSortedInput);
		wSortedInput.addSelectionListener(new SelectionAdapter() 
			{
				public void widgetSelected(SelectionEvent e) 
				{
					input.setChanged();
				}
			}
		);
		// THE BUTTONS
		wOK=new Button(shell, SWT.PUSH);
		wOK.setText(" &OK ");
		wGet=new Button(shell, SWT.PUSH);
		wGet.setText(" &Get Fields ");
		wGetLU=new Button(shell, SWT.PUSH);
		wGetLU.setText(" Get &lookup fields ");
		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText(" &Cancel ");

		setButtonPositions(new Button[] { wOK, wGet, wGetLU, wCancel }, margin, wSortedInput);

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
	
	/**
	 * Copy information from the meta-data input to the dialog fields.
	 */ 
	public void getData()
	{
		int i;
		log.logDebug(toString(), "getting key info...");
		
		if (input.getKeystream()!=null)
		for (i=0;i<input.getKeystream().length;i++)
		{
			TableItem item = wKey.table.getItem(i);
			if (input.getKeystream()[i]     !=null) item.setText(1, input.getKeystream()[i]);
			if (input.getKeylookup()[i]!=null) item.setText(2, input.getKeylookup()[i]);
		}
		
		if (input.getValue()!=null)
		for (i=0;i<input.getValue().length;i++)
		{
			TableItem item = wReturn.table.getItem(i);
			if (input.getValue()[i]!=null     ) item.setText(1, input.getValue()[i]);
			if (input.getValueName()[i]!=null && !input.getValueName()[i].equals(input.getValue()[i]))
				item.setText(2, input.getValueName()[i]);
			if (input.getValueDefault()[i]!=null  ) item.setText(3, input.getValueDefault()[i]);
			item.setText(4, Value.getTypeDesc(input.getValueDefaultType()[i]));
		}
		
		if (input.getLookupFromStep()!=null && input.getLookupFromStep().getName()!=null) wStep.setText( input.getLookupFromStep().getName() );
		wSortedInput.setSelection(input.isInputSorted());
		
		wStepname.selectAll();
		wKey.setRowNums();
		wKey.optWidth(true);
		wReturn.setRowNums();
		wReturn.optWidth(true);
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
		int nrkeys, nrvalues;
	
		nrkeys             = wKey.nrNonEmpty();
		nrvalues           = wReturn.nrNonEmpty();
		input.allocate(nrkeys, nrvalues);
		input.setInputSorted(wSortedInput.getSelection());

		log.logDebug(toString(), "Found "+nrkeys+" keys");
		for (i=0;i<nrkeys;i++)
		{
			TableItem item     = wKey.getNonEmpty(i);
			input.getKeystream()[i]       = item.getText(1);
			input.getKeylookup()[i] = item.getText(2);
		}
		
		log.logDebug(toString(), "Found "+nrvalues+" fields");
		for (i=0;i<nrvalues;i++)
		{
			TableItem item        = wReturn.getNonEmpty(i);
			input.getValue()[i]        = item.getText(1);
			input.getValueName()[i]    = item.getText(2);
			if (input.getValueName()[i]==null || input.getValueName()[i].length()==0)
				input.getValueName()[i] = input.getValue()[i];
			input.getValueDefault()[i]     = item.getText(3);
			input.getValueDefaultType()[i] = Value.getType(item.getText(4));
		}
		
		input.setLookupFromStep( transMeta.findStep( wStep.getText() ) );
		if (input.getLookupFromStep()==null)
		{
			MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
			mb.setMessage("Step ["+wStep.getText()+"] can't be found: please select an existing source step!");
			mb.setText("ERROR");
			mb.open(); 
		}

		stepname = wStepname.getText(); // return value
		
		dispose();
	}

	private void get()
	{
		try
		{
			Row r = transMeta.getPrevStepFields(stepname);
			if (r!=null)
			{
				Table table=wKey.table;
				for (int i=0;i<r.size();i++)
				{
					Value v = r.getValue(i);
					TableItem ti = new TableItem(table, SWT.NONE);
					ti.setText(1, v.getName());
					ti.setText(2, v.getName());
				}
			}
			wKey.removeEmptyRows();
			wKey.setRowNums();
			wKey.optWidth(true);
		}
		catch(KettleException ke)
		{
			new ErrorDialog(shell, props, "Get fields failed", "Unable to get fields from previous steps because of an error", ke);
		}
	}

	private void getlookup()
	{
		try
		{
			String stepFrom = wStep.getText();
			if (stepFrom!=null)
			{
				Row r = transMeta.getStepFields(stepFrom);
				if (r!=null)
				{
					Table table=wReturn.table;
					for (int i=0;i<r.size();i++)
					{
						Value v = r.getValue(i);
						TableItem ti = new TableItem(table, SWT.NONE);
						ti.setText(1, v.getName());
						ti.setText(2, "");
						ti.setText(3, "");
						ti.setText(4, v.getTypeDesc());
					}
					wReturn.removeEmptyRows();
					wReturn.setRowNums();
					wReturn.optWidth(true);
				}
				else
				{
					MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
					mb.setMessage("Couldn't find any fields, please check the source step!");
					mb.setText("ERROR");
					mb.open(); 
				}
			}
			else
			{
				MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
				mb.setMessage("Please select a step name!");
				mb.setText("ERROR");
				mb.open(); 
			}
		}
		catch(KettleException ke)
		{
			new ErrorDialog(shell, props, "Get fields failed", "Unable to get the fields from previous steps because of an error", ke);
		}

	}
	
	public String toString()
	{
		return this.getClass().getName();
	}
}
