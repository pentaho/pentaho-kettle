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

package be.ibridge.kettle.spoon.dialog;
import java.util.ArrayList;

import org.eclipse.swt.SWT;
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
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.Props;
import be.ibridge.kettle.core.WindowProperty;
import be.ibridge.kettle.core.dialog.PreviewRowsDialog;
import be.ibridge.kettle.trans.step.BaseStepDialog;


/**
 * Shows a dialog that allows you to select the steps you want to preview by entering a number of rows.
 *  
 * @author Matt
 *
 */
public class EnterPreviewRowsDialog extends Dialog
{
	private String       stepname;
		
	private Label        wlStepList;
	private List         wStepList;
    private FormData     fdlStepList, fdStepList;
	
	private Button wShow, wClose;
	private Listener lsShow, lsClose;

	private Shell         shell;
	private ArrayList     names, buffers;
	private Props 		  props;

    /** @deprecated */
    public EnterPreviewRowsDialog(Shell parent, int style, LogWriter l, Props pr, ArrayList nam, ArrayList buf)
    {
        this(parent, style, nam, buf);
    }

	public EnterPreviewRowsDialog(Shell parent, int style, ArrayList nam, ArrayList buf)
	{
			super(parent, style);
			names=nam;
			buffers=buf;
			props=Props.getInstance();
	}

	public Object open()
	{
		Shell parent = getParent();
		Display display = parent.getDisplay();

		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX);
 		props.setLook(shell);

		FormLayout formLayout = new FormLayout ();
		formLayout.marginWidth  = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText(Messages.getString("EnterPreviewRowsDialog.Dialog.PreviewStep.Title")); //Select the preview step:
		
		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		// Filename line
		wlStepList=new Label(shell, SWT.NONE);
		wlStepList.setText(Messages.getString("EnterPreviewRowsDialog.Dialog.PreviewStep.Message")); //Step name : 
 		props.setLook(wlStepList);
		fdlStepList=new FormData();
		fdlStepList.left = new FormAttachment(0, 0);
		fdlStepList.top  = new FormAttachment(0, margin);
		wlStepList.setLayoutData(fdlStepList);
		wStepList=new List(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER | SWT.READ_ONLY);
		for (int i=0;i<names.size();i++)
		{
			wStepList.add((String)names.get(i)); 
		}
		wStepList.select(0);
 		props.setLook(wStepList);
		fdStepList=new FormData();
		fdStepList.left   = new FormAttachment(middle, 0);
		fdStepList.top    = new FormAttachment(0, margin);
		fdStepList.bottom = new FormAttachment(100, -60);
		fdStepList.right  = new FormAttachment(100, 0);
		wStepList.setLayoutData(fdStepList);
		wStepList.addSelectionListener(new SelectionAdapter()
		{
			public void widgetDefaultSelected(SelectionEvent arg0)
			{
				show();
			}
		});

		wShow=new Button(shell, SWT.PUSH);
		wShow.setText(Messages.getString("System.Button.Show"));

		wClose=new Button(shell, SWT.PUSH);
		wClose.setText(Messages.getString("System.Button.Close"));

		BaseStepDialog.positionBottomButtons(shell, new Button[] { wShow, wClose }, margin, null);
		// Add listeners
		lsShow       = new Listener() { public void handleEvent(Event e) { show();     } };
		lsClose   = new Listener() { public void handleEvent(Event e) { close(); } };

		wShow.addListener (SWT.Selection, lsShow    );
		wClose.addListener(SWT.Selection, lsClose    );
		
		// Detect X or ALT-F4 or something that kills this window...
		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { close(); } } );

		getData();

		BaseStepDialog.setSize(shell);

		// Immediately show the only preview entry
		if (names.size()==1)
		{
			wStepList.select(0);
			show();
		}
		
		shell.open();
		while (!shell.isDisposed())
		{
				if (!display.readAndDispatch()) display.sleep();
		}
		return stepname;
	}

	public void dispose()
	{
		props.setScreen(new WindowProperty(shell));
		shell.dispose();
	}
	
	/**
	 * Copy information from the meta-data input to the dialog fields.
	 */ 
	public void getData()
	{	
	}

	private void close()
	{
		dispose();
	}
	
	private void show()
	{
		if (buffers.size()==0) return;
		
		int nr = wStepList.getSelectionIndex();

		ArrayList buffer = (ArrayList)buffers.get(nr);
		String    name   = (String)names.get(nr);
		
		PreviewRowsDialog prd = new PreviewRowsDialog(shell, SWT.NONE, name, buffer);
		prd.open();		

	}
}
