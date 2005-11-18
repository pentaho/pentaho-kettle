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
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.Props;
import be.ibridge.kettle.core.WindowProperty;
import be.ibridge.kettle.core.dialog.PreviewRowsDialog;


/**
 * Shows a dialog that allows you to select the steps you want to preview by entering a number of rows.
 *  
 * @author Matt
 *
 */
public class EnterPreviewRowsDialog extends Dialog
{
	private String       stepname;
		
	private Label        wlStepname;
	private List         wStepname;
    private FormData     fdlStepname, fdStepname;
	
	private Button wShow, wClose;
	private FormData fdShow, fdClose;
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
		shell.setText("Select the preview step:");
		
		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		// Filename line
		wlStepname=new Label(shell, SWT.NONE);
		wlStepname.setText("Step name : ");
 		props.setLook(wlStepname);
		fdlStepname=new FormData();
		fdlStepname.left = new FormAttachment(0, 0);
		fdlStepname.top  = new FormAttachment(0, margin);
		wlStepname.setLayoutData(fdlStepname);
		wStepname=new List(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER | SWT.READ_ONLY);
		for (int i=0;i<names.size();i++)
		{
			wStepname.add((String)names.get(i)); 
		}
		wStepname.select(0);
 		props.setLook(wStepname);
		fdStepname=new FormData();
		fdStepname.left = new FormAttachment(middle, 0);
		fdStepname.top  = new FormAttachment(0, margin);
		fdStepname.right= new FormAttachment(100, 0);
		wStepname.setLayoutData(fdStepname);
		wStepname.addSelectionListener(new SelectionAdapter()
		{
			public void widgetDefaultSelected(SelectionEvent arg0)
			{
				show();
			}
		});

		wShow=new Button(shell, SWT.PUSH);
		wShow.setText(" &Show ");
		fdShow=new FormData();
		fdShow.left=new FormAttachment(33, 0);
		fdShow.top =new FormAttachment(wStepname, 30);
		wShow.setLayoutData(fdShow);

		wClose=new Button(shell, SWT.PUSH);
		wClose.setText(" &Close ");
		fdClose=new FormData();
		fdClose.left=new FormAttachment(wShow, 10);
		fdClose.top =new FormAttachment(wStepname, 30);
		wClose.setLayoutData(fdClose);


		// Add listeners
		lsShow       = new Listener() { public void handleEvent(Event e) { show();     } };
		lsClose   = new Listener() { public void handleEvent(Event e) { close(); } };

		wShow.addListener (SWT.Selection, lsShow    );
		wClose.addListener(SWT.Selection, lsClose    );
		
		// Detect X or ALT-F4 or something that kills this window...
		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { close(); } } );

		getData();

		WindowProperty winprop = props.getScreen(shell.getText());
		if (winprop!=null) winprop.setShell(shell); else shell.pack();
		
		// Immediately show the only preview entry
		if (names.size()==1)
		{
			wStepname.select(0);
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
		
		int nr = wStepname.getSelectionIndex();

		ArrayList buffer = (ArrayList)buffers.get(nr);
		String    name   = (String)names.get(nr);
		
		if (buffer.size()>0)
		{
			PreviewRowsDialog prd = new PreviewRowsDialog(shell, SWT.NONE, name, buffer);
			prd.open();		
		}
		else
		{
			MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_WARNING );
			mb.setMessage("No rows found in previewing this step.");
			mb.setText("WARNING");
			mb.open();
		}
	}
}
