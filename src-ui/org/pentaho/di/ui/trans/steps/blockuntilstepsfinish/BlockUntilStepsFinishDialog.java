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

package org.pentaho.di.ui.trans.steps.blockuntilstepsfinish;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
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
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.blockuntilstepsfinish.BlockUntilStepsFinishMeta;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.trans.step.BaseStepDialog;


public class BlockUntilStepsFinishDialog extends BaseStepDialog implements StepDialogInterface
{
	private static Class<?> PKG = BlockUntilStepsFinishMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private String previousSteps[] ;
	private BlockUntilStepsFinishMeta input;

	private Label        wlFields;
	private TableView    wFields;
	private FormData     fdlFields, fdFields;
	
	public BlockUntilStepsFinishDialog(Shell parent, Object in, TransMeta tr, String sname)
	{
		super(parent, (BaseStepMeta)in, tr, sname);
		input=(BlockUntilStepsFinishMeta)in;
	}

	public String open()
	{
		Shell parent = getParent();
		Display display = parent.getDisplay();

		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MIN | SWT.MAX);
 		props.setLook(shell);
 		setShellImage(shell, input);
        
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
		shell.setText(BaseMessages.getString(PKG, "BlockUntilStepsFinishDialog.Shell.Title")); //$NON-NLS-1$
		
		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		// Stepname line
		wlStepname=new Label(shell, SWT.RIGHT);
		wlStepname.setText(BaseMessages.getString(PKG, "BlockUntilStepsFinishDialog.Stepname.Label")); //$NON-NLS-1$
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
		
        // Get the previous steps...
        setStepNames();
        
		wOK=new Button(shell, SWT.PUSH);
		wOK.setText(BaseMessages.getString(PKG, "System.Button.OK"));
		wGet=new Button(shell, SWT.PUSH);
		wGet.setText(BaseMessages.getString(PKG, "BlockUntilStepsFinishDialog.getSteps.Label"));
		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel"));

		setButtonPositions(new Button[] { wOK, wGet, wCancel }, margin, null);
        
	
        // Table with fields
		wlFields=new Label(shell, SWT.NONE);
		wlFields.setText(BaseMessages.getString(PKG, "BlockUntilStepsFinishDialog.Fields.Label"));
 		props.setLook(wlFields);
		fdlFields=new FormData();
		fdlFields.left = new FormAttachment(0, 0);
		fdlFields.top  = new FormAttachment(wStepname, margin);
		wlFields.setLayoutData(fdlFields);
		
		final int FieldsCols=2;
		final int FieldsRows=input.getStepName().length;
		
		ColumnInfo[] colinf=new ColumnInfo[FieldsCols];
		colinf[0]=new ColumnInfo(BaseMessages.getString(PKG, "BlockUntilStepsFinishDialog.Fieldname.Step"),  ColumnInfo.COLUMN_TYPE_CCOMBO,  previousSteps, false);
		colinf[1]=new ColumnInfo(BaseMessages.getString(PKG, "BlockUntilStepsFinishDialog.Fieldname.CopyNr"),  ColumnInfo.COLUMN_TYPE_TEXT, false);
		colinf[1].setUsingVariables(true);
		wFields=new TableView(transMeta, shell, 
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
		fdFields.bottom= new FormAttachment(wOK, -2*margin);
		wFields.setLayoutData(fdFields);

		// Add listeners
		lsCancel   = new Listener() { public void handleEvent(Event e) { cancel(); } };
		lsGet      = new Listener() { public void handleEvent(Event e) { get();    } };
		lsOK       = new Listener() { public void handleEvent(Event e) { ok();     } };
		
		
		wCancel.addListener(SWT.Selection, lsCancel);
		wOK.addListener    (SWT.Selection, lsOK    );
		wGet.addListener   (SWT.Selection, lsGet   );
		
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
	private void setStepNames()
	{
		previousSteps=transMeta.getStepNames();
		String[] nextSteps=transMeta.getNextStepNames(stepMeta);
		
		List<String> entries = new ArrayList<String>();
		for(int i=0;i<previousSteps.length;i++) {
			if(!previousSteps[i].equals(stepname)) {	
				if(nextSteps!=null) {
					for(int j=0;j<nextSteps.length;j++) {
						if(!nextSteps[j].equals(previousSteps[i])) entries.add(previousSteps[i]);	
					}
				}
			}
		}
		previousSteps = (String[]) entries.toArray(new String[entries.size()]);
	}
	private void get()
	{
		wFields.removeAll();
		Table table = wFields.table;

		for(int i=0;i<previousSteps.length;i++) {
			TableItem ti = new TableItem(table, SWT.NONE);
			ti.setText(0, ""+(i+1));
			ti.setText(1,previousSteps[i]);
			ti.setText(2, "0");
		}
		wFields.removeEmptyRows();
        wFields.setRowNums();
		wFields.optWidth(true);
		
	}
	/**
	 * Copy information from the meta-data input to the dialog fields.
	 */ 
	public void getData()
	{
		Table table = wFields.table;
		if (input.getStepName().length>0) table.removeAll();
		for (int i=0;i<input.getStepName().length;i++)
		{
			TableItem ti = new TableItem(table, SWT.NONE);
			ti.setText(0, ""+(i+1));
			if(input.getStepName()[i]!=null)
			{
				ti.setText(1, input.getStepName()[i]);
				ti.setText(2, ""+Const.toInt(input.getStepCopyNr()[i],0));
			}
		}

		wFields.removeEmptyRows();
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
		if (Const.isEmpty(wStepname.getText())) return;
		stepname = wStepname.getText(); // return value
		
		int nrsteps = wFields.nrNonEmpty();
		input.allocate(nrsteps);
		for (int i=0;i<nrsteps;i++)
		{
			TableItem ti = wFields.getNonEmpty(i);
			StepMeta tm=transMeta.findStep(ti.getText(1));
			if(tm!=null){
				input.getStepName()[i] =tm.getName();
				input.getStepCopyNr()[i] =String.valueOf(Const.toInt(ti.getText(2),0));
			}

		}
		dispose();
	}
}
