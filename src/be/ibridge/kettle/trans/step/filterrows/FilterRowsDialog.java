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

package be.ibridge.kettle.trans.step.filterrows;

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
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import be.ibridge.kettle.core.Condition;
import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.dialog.ErrorDialog;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.widget.ConditionEditor;
import be.ibridge.kettle.trans.TransMeta;
import be.ibridge.kettle.trans.step.BaseStepDialog;
import be.ibridge.kettle.trans.step.BaseStepMeta;
import be.ibridge.kettle.trans.step.StepDialogInterface;
import be.ibridge.kettle.trans.step.StepMeta;


public class FilterRowsDialog extends BaseStepDialog implements StepDialogInterface
{
	private Label        wlTrueTo;
	private CCombo       wTrueTo;
	private FormData     fdlTrueTo, fdTrueTo;

	private Label        wlFalseTo;
	private CCombo       wFalseTo;
	private FormData     fdlFalseTo, fdFalseFrom;

	private Label           wlCondition;
	private ConditionEditor wCondition;
	private FormData        fdlCondition, fdCondition;

	private FilterRowsMeta input;
	private Condition      condition;
	
	private Condition      backupCondition;

	public FilterRowsDialog(Shell parent, Object in, TransMeta tr, String sname)
	{
		super(parent, (BaseStepMeta)in, tr, sname);
		input=(FilterRowsMeta)in;
        
 		condition = (Condition)input.getCondition().clone();
    }

	public String open()
	{
		Shell parent = getParent();
		Display display = parent.getDisplay();

        shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MIN | SWT.MAX );
 		props.setLook(shell);

		
		ModifyListener lsMod = new ModifyListener() 
		{
			public void modifyText(ModifyEvent e) 
			{
				input.setChanged();
			}
		};
		backupChanged = input.hasChanged();
		backupCondition = (Condition)condition.clone(); 
			
		FormLayout formLayout = new FormLayout ();
		formLayout.marginWidth  = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText("Filter rows");
		
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

		// Send 'True' data to...
		wlTrueTo=new Label(shell, SWT.RIGHT);
		wlTrueTo.setText("Send 'True' data to step:");
 		props.setLook(wlTrueTo);
		fdlTrueTo=new FormData();
		fdlTrueTo.left = new FormAttachment(0, 0);
		fdlTrueTo.right= new FormAttachment(middle, -margin);
		fdlTrueTo.top  = new FormAttachment(wStepname, margin);
		wlTrueTo.setLayoutData(fdlTrueTo);
		wTrueTo=new CCombo(shell, SWT.BORDER );
 		props.setLook(wTrueTo);

		StepMeta stepinfo = transMeta.findStep(stepname);
		if (stepinfo!=null)
		{
			for (int i=0;i<transMeta.findNrNextSteps(stepinfo);i++)
			{
				StepMeta stepMeta = transMeta.findNextStep(stepinfo, i);
				wTrueTo.add(stepMeta.getName());
			}
		}
		
		wTrueTo.addModifyListener(lsMod);
		fdTrueTo=new FormData();
		fdTrueTo.left = new FormAttachment(middle, 0);
		fdTrueTo.top  = new FormAttachment(wStepname, margin);
		fdTrueTo.right= new FormAttachment(100, 0);
		wTrueTo.setLayoutData(fdTrueTo);

		// Send 'False' data to...
		wlFalseTo=new Label(shell, SWT.RIGHT);
		wlFalseTo.setText("Send 'false' data to step:");
 		props.setLook(wlFalseTo);
		fdlFalseTo=new FormData();
		fdlFalseTo.left = new FormAttachment(0, 0);
		fdlFalseTo.right= new FormAttachment(middle, -margin);
		fdlFalseTo.top  = new FormAttachment(wTrueTo, margin);
		wlFalseTo.setLayoutData(fdlFalseTo);
		wFalseTo=new CCombo(shell, SWT.BORDER );
 		props.setLook(wFalseTo);

		stepinfo = transMeta.findStep(stepname);
		if (stepinfo!=null)
		{
			for (int i=0;i<transMeta.findNrNextSteps(stepinfo);i++)
			{
				StepMeta stepMeta = transMeta.findNextStep(stepinfo, i);
				wFalseTo.add(stepMeta.getName());
			}
		}
		
		wFalseTo.addModifyListener(lsMod);
		fdFalseFrom=new FormData();
		fdFalseFrom.left = new FormAttachment(middle, 0);
		fdFalseFrom.top  = new FormAttachment(wTrueTo, margin);
		fdFalseFrom.right= new FormAttachment(100, 0);
		wFalseTo.setLayoutData(fdFalseFrom);

		
		wlCondition=new Label(shell, SWT.NONE);
		wlCondition.setText("The condition: ");
 		props.setLook(wlCondition);
		fdlCondition=new FormData();
		fdlCondition.left  = new FormAttachment(0, 0);
		fdlCondition.top   = new FormAttachment(wFalseTo, margin);
		wlCondition.setLayoutData(fdlCondition);
		
		Row inputfields = null;
		try
		{
			inputfields = transMeta.getPrevStepFields(stepname);
		}
		catch(KettleException ke)
		{
			inputfields = new Row();
			new ErrorDialog(shell, props, "Get fields failed", "Unable to get fields from previous steps because of an error", ke);
		}

		// Some buttons
		wOK=new Button(shell, SWT.PUSH);
		wOK.setText("  &OK  ");
		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText("  &Cancel  ");

		setButtonPositions(new Button[] { wOK, wCancel }, margin, null);

		wCondition = new ConditionEditor(shell, SWT.BORDER, condition, props, inputfields);
		
		fdCondition=new FormData();
		fdCondition.left  = new FormAttachment(0, 0);
		fdCondition.top   = new FormAttachment(wlCondition, margin);
		fdCondition.right = new FormAttachment(100, 0);
		fdCondition.bottom= new FormAttachment(wOK, -2*margin);
		wCondition.setLayoutData(fdCondition);
		wCondition.addModifyListener(lsMod);

		// Add listeners
		lsCancel   = new Listener() { public void handleEvent(Event e) { cancel(); } };
		lsOK       = new Listener() { public void handleEvent(Event e) { ok();     } };
		
		wCancel.addListener(SWT.Selection, lsCancel);
		wOK.addListener    (SWT.Selection, lsOK    );
		
		lsDef=new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } };
		
		wStepname.addSelectionListener( lsDef );
		
		// Detect X or ALT-F4 or something that kills this window...
		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { cancel(); } } );


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
	
	/**
	 * Copy information from the meta-data input to the dialog fields.
	 */ 
	public void getData()
	{
		if (input.getSendTrueStepname() != null) wTrueTo.setText(input.getSendTrueStepname());
		if (input.getSendFalseStepname() != null) wFalseTo.setText(input.getSendFalseStepname());
		wStepname.selectAll();
	}
	
	private void cancel()
	{
		stepname=null;
		input.setChanged(backupChanged);
		// Also change the condition back to what it was...
		input.setCondition(backupCondition);
		dispose();
	}
	
	private void ok()
	{		
		if (wCondition.getLevel()>0) 
		{
			wCondition.goUp();
		}
		else
		{
			input.setSendTrueStep( transMeta.findStep( wTrueTo.getText() ) );
			input.setSendFalseStep( transMeta.findStep( wFalseTo.getText() ) );
			stepname = wStepname.getText(); // return value
			input.setCondition( condition );
			
			dispose();
		}
	}
}
