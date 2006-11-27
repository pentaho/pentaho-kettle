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

package be.ibridge.kettle.trans.step.socketreader;

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
import org.eclipse.swt.widgets.Text;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.widget.TextVar;
import be.ibridge.kettle.trans.TransMeta;
import be.ibridge.kettle.trans.step.BaseStepDialog;
import be.ibridge.kettle.trans.step.BaseStepMeta;
import be.ibridge.kettle.trans.step.StepDialogInterface;


public class SocketReaderDialog extends BaseStepDialog implements StepDialogInterface
{
	private SocketReaderMeta input;
    private TextVar wHostname;
    private TextVar wPort;

	public SocketReaderDialog(Shell parent, Object in, TransMeta tr, String sname)
	{
		super(parent, (BaseStepMeta)in, tr, sname);
		input=(SocketReaderMeta )in;
	}

	public String open()
	{
		Shell parent = getParent();
		Display display = parent.getDisplay();

		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MIN | SWT.MAX);
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
		shell.setText(Messages.getString("SocketReaderDialog.Shell.Title")); //$NON-NLS-1$
		
		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		// Stepname line
		wlStepname=new Label(shell, SWT.RIGHT);
		wlStepname.setText(Messages.getString("SocketReaderDialog.Stepname.Label")); //$NON-NLS-1$
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
        
        // Hostname line
        Label wlHostname = new Label(shell, SWT.RIGHT);
        wlHostname.setText(Messages.getString("SocketReaderDialog.Hostname.Label")); //$NON-NLS-1$
        props.setLook(wlHostname);
        FormData fdlHostname = new FormData();
        fdlHostname.left = new FormAttachment(0, 0);
        fdlHostname.right= new FormAttachment(middle, -margin);
        fdlHostname.top  = new FormAttachment(wStepname, margin);
        wlHostname.setLayoutData(fdlHostname);
        wHostname=new TextVar(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        wHostname.setText(stepname);
        props.setLook(wHostname);
        wHostname.addModifyListener(lsMod);
        FormData fdHostname = new FormData();
        fdHostname.left = new FormAttachment(middle, 0);
        fdHostname.top  = new FormAttachment(wStepname, margin);
        fdHostname.right= new FormAttachment(100, 0);
        wHostname.setLayoutData(fdHostname);
        
        // Port line
        Label wlPort = new Label(shell, SWT.RIGHT);
        wlPort.setText(Messages.getString("SocketReaderDialog.Port.Label")); //$NON-NLS-1$
        props.setLook(wlPort);
        FormData fdlPort = new FormData();
        fdlPort.left = new FormAttachment(0, 0);
        fdlPort.right= new FormAttachment(middle, -margin);
        fdlPort.top  = new FormAttachment(wHostname, margin);
        wlPort.setLayoutData(fdlPort);
        wPort=new TextVar(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        wPort.setText(stepname);
        props.setLook(wPort);
        wPort.addModifyListener(lsMod);
        FormData fdPort = new FormData();
        fdPort.left = new FormAttachment(middle, 0);
        fdPort.top  = new FormAttachment(wHostname, margin);
        fdPort.right= new FormAttachment(100, 0);
        wPort.setLayoutData(fdPort);
		
		// Some buttons
		wOK=new Button(shell, SWT.PUSH);
		wOK.setText(Messages.getString("System.Button.OK")); //$NON-NLS-1$
		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText(Messages.getString("System.Button.Cancel")); //$NON-NLS-1$

		setButtonPositions(new Button[] { wOK, wCancel }, margin, wPort);

		// Add listeners
		lsCancel   = new Listener() { public void handleEvent(Event e) { cancel(); } };
		lsOK       = new Listener() { public void handleEvent(Event e) { ok();     } };
		
		wCancel.addListener(SWT.Selection, lsCancel);
		wOK.addListener    (SWT.Selection, lsOK    );
		
		lsDef=new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } };
		
		wStepname.addSelectionListener( lsDef );
        wHostname.addSelectionListener( lsDef );
        wPort.addSelectionListener( lsDef );
		
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
        wPort.setText(Const.NVL(input.getPort(), ""));
        wHostname.setText(Const.NVL(input.getHostname(), ""));
        
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
        input.setHostname(wHostname.getText());
        input.setPort(wPort.getText());
        
		stepname = wStepname.getText(); // return value
		
		dispose();
	}
}
