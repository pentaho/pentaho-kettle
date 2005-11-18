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

package be.ibridge.kettle.trans.dialog;
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
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.Props;
import be.ibridge.kettle.core.WindowProperty;
import be.ibridge.kettle.trans.TransHopMeta;
import be.ibridge.kettle.trans.TransMeta;
import be.ibridge.kettle.trans.step.StepMeta;


public class TransHopDialog extends Dialog
{	
	private Label        wlFrom;
	private CCombo       wFrom;
    private FormData     fdlFrom, fdFrom;
	
	private Label        wlTo;
	private Button       wFlip;
	private CCombo       wTo;
	private FormData     fdlTo, fdFlip, fdTo;

	private Label        wlEnabled;
	private Button       wEnabled;
	private FormData     fdlEnabled, fdEnabled;
	
	private Button wOK, wCancel;
	private FormData fdOK, fdCancel;
	private Listener lsOK, lsCancel, lsFlip;

	private TransHopMeta input;
	private Shell  shell;
	private TransMeta transMeta;
	private Props props;
	
	private ModifyListener lsMod;
	
	private boolean changed;
	
    /** @deprecated */
    public TransHopDialog(Shell parent, int style, LogWriter l, Props props, Object in, TransMeta tr)
    {
        this(parent, style, in, tr);
    }
    
	public TransHopDialog(Shell parent, int style, Object in, TransMeta tr)
	{
		super(parent, style);
		this.props=Props.getInstance();
		input=(TransHopMeta)in;
		transMeta=tr;
	}

	public Object open()
	{
		Shell parent = getParent();
		Display display = parent.getDisplay();

		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE);
 		props.setLook(shell);
		
		lsMod = new ModifyListener() 
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
		shell.setText("Hop: From --> To");
		
		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;
        
		// From step line
		wlFrom=new Label(shell, SWT.RIGHT);
		wlFrom.setText("From step: ");
 		props.setLook(wlFrom);
		fdlFrom=new FormData();
		fdlFrom.left = new FormAttachment(0, 0);
		fdlFrom.right= new FormAttachment(middle, -margin);
		fdlFrom.top  = new FormAttachment(0, margin);
		wlFrom.setLayoutData(fdlFrom);
		wFrom=new CCombo(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wFrom.setText("Select the source");
 		props.setLook(wFrom);

		for (int i=0;i<transMeta.nrSteps();i++)
		{
			StepMeta stepMeta = transMeta.getStep(i);
			wFrom.add(stepMeta.getName());
		}
		wFrom.addModifyListener(lsMod);

		fdFrom=new FormData();
		fdFrom.left = new FormAttachment(middle, 0);
		fdFrom.top  = new FormAttachment(0, margin);
		fdFrom.right= new FormAttachment(100, 0);
		wFrom.setLayoutData(fdFrom);

		// To line
		wlTo=new Label(shell, SWT.RIGHT);
		wlTo.setText("To step: ");
 		props.setLook(wlTo);
		fdlTo=new FormData();
		fdlTo.left = new FormAttachment(0, 0);
		fdlTo.right= new FormAttachment(middle, -margin);
		fdlTo.top  = new FormAttachment(wFrom, margin);
		wlTo.setLayoutData(fdlTo);
		wTo=new CCombo(shell, SWT.BORDER | SWT.READ_ONLY);
		wTo.setText("Select the destination");
 		props.setLook(wTo);

		for (int i=0;i<transMeta.nrSteps();i++)
		{
			StepMeta stepMeta = transMeta.getStep(i);
			wTo.add(stepMeta.getName());
		} 
		wTo.addModifyListener(lsMod);
		
		fdTo=new FormData();
		fdTo.left = new FormAttachment(middle, 0);
		fdTo.top  = new FormAttachment(wFrom, margin);
		fdTo.right= new FormAttachment(100, 0);
		wTo.setLayoutData(fdTo);
		
		wFlip = new Button(shell, SWT.PUSH);
		wFlip.setText("&From <-> To");
		fdFlip = new FormData();
		fdFlip.left = new FormAttachment(middle, margin);
		fdFlip.top  = new FormAttachment(wTo, margin*2);
		wFlip.setLayoutData(fdFlip);
	
		// Enabled?
		wlEnabled=new Label(shell, SWT.RIGHT);
		wlEnabled.setText("Enable hop?");
 		props.setLook(wlEnabled);
		fdlEnabled=new FormData();
		fdlEnabled.left = new FormAttachment(0, 0);
		fdlEnabled.right= new FormAttachment(middle, -margin);
		fdlEnabled.top  = new FormAttachment(wFlip, margin*2);
		wlEnabled.setLayoutData(fdlEnabled);
		wEnabled=new Button(shell, SWT.CHECK);
 		props.setLook(wEnabled);
		fdEnabled=new FormData();
		fdEnabled.left = new FormAttachment(middle, 0);
		fdEnabled.top  = new FormAttachment(wFlip, margin*2);
		fdEnabled.right= new FormAttachment(100, 0);
		wEnabled.setLayoutData(fdEnabled);
		wEnabled.addSelectionListener(new SelectionAdapter() 
			{
				public void widgetSelected(SelectionEvent e) 
				{
					input.setEnabled( !input.isEnabled());
					input.setChanged();
				}
			}
		);
		
		// Some buttons
		wOK=new Button(shell, SWT.PUSH);
		wOK.setText("  &OK  ");
		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText("  &Cancel  ");
		fdOK=new FormData();
		fdOK.left       = new FormAttachment(33, 0);
		fdOK.top        = new FormAttachment(wEnabled, margin*2);
		wOK.setLayoutData(fdOK);
		fdCancel=new FormData();
		fdCancel.left   = new FormAttachment(66, 0);
		fdCancel.top    = new FormAttachment(wEnabled, margin*2);
		wCancel.setLayoutData(fdCancel);

		// Add listeners
		lsCancel   = new Listener() { public void handleEvent(Event e) { cancel(); } };
		lsOK       = new Listener() { public void handleEvent(Event e) { ok();     } };
		lsFlip     = new Listener() { public void handleEvent(Event e) { flip();   } };
		
		wOK.addListener    (SWT.Selection, lsOK     );
		wCancel.addListener(SWT.Selection, lsCancel );
		wFlip.addListener  (SWT.Selection, lsFlip );
		
		// Detect [X] or ALT-F4 or something that kills this window...
		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { cancel(); } } );

		getData();

		WindowProperty winprop = props.getScreen(shell.getText());
		if (winprop!=null) winprop.setShell(shell); else shell.pack();

		input.setChanged(changed);
	
		shell.open();
		while (!shell.isDisposed())
		{
				if (!display.readAndDispatch()) display.sleep();
		}
		return input;
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
		if (input.getFromStep() != null) wFrom.setText(input.getFromStep().getName());
		if (input.getToStep()   != null) wTo.setText(input.getToStep().getName());
		wEnabled.setSelection(input.isEnabled());  
	}
	
	private void cancel()
	{
		input.setChanged(changed);
		input=null;
		dispose();
	}
	
	private void ok()
	{
		StepMeta fromBackup = input.getFromStep();
		StepMeta toBackup = input.getToStep(); 
		input.setFromStep( transMeta.findStep( wFrom.getText() ));
		input.setToStep  ( transMeta.findStep( wTo.getText()   ));
		
		if (transMeta.hasLoop(input.getFromStep()))
		{
			input.setFromStep(fromBackup);
			input.setToStep(toBackup);
			MessageBox mb = new MessageBox(shell, SWT.YES | SWT.ICON_WARNING );
			mb.setMessage("This hop causes a loop in the transformation.  Loops are not allowed!");
			mb.setText("Warning!");
			mb.open();
		}
		else
		{
			if (input.getFromStep()==null)
			{
				MessageBox mb = new MessageBox(shell, SWT.YES | SWT.ICON_WARNING );
				mb.setMessage("Step ["+wFrom.getText()+"] doesn't exist!");
				mb.setText("Warning!");
				mb.open();
			}
			else
			{
				if (input.getToStep()==null)
				{
					MessageBox mb = new MessageBox(shell, SWT.YES | SWT.ICON_WARNING );
					mb.setMessage("Step ["+wTo.getText()+"] doesn't exist!");
					mb.setText("Warning!");
					mb.open();
				}
				else
				{
					if (input.getFromStep().equals(input.getToStep()))
					{
						MessageBox mb = new MessageBox(shell, SWT.YES | SWT.ICON_WARNING );
						mb.setMessage("A hop can't go to the same step!");
						mb.setText("Warning!");
						mb.open();
					}
					else
					{
						dispose();
					}
				}
			}
		}
	}
	
	private void flip()
	{
		String dummy;
		dummy = wFrom.getText();
		wFrom.setText(wTo.getText());
		wTo.setText(dummy);
		input.setChanged();
	}
}
