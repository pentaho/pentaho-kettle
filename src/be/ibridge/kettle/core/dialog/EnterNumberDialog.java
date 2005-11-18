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

 
package be.ibridge.kettle.core.dialog;
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
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.Props;
import be.ibridge.kettle.core.WindowProperty;

/**
 * This dialog allows you to enter a number.
 * 
 * @author Matt
 * @since 19-06-2003
 */
public class EnterNumberDialog extends Dialog
{
	private Label        wlNumber;
	private Text         wNumber;
    private FormData     fdlNumber, fdNumber;
		
	private Button wOK, wCancel;
	private FormData fdOK, fdCancel;
	private Listener lsOK, lsCancel;

	private Shell  shell;
	private SelectionAdapter lsDef;
	
	private int samples;
	private String shellText;
	private String lineText;
	private Props props;
		
	public EnterNumberDialog(Shell parent, Props pr, int s, String st, String lt)
	{
		super(parent, SWT.NONE);
		props=pr;
		samples=s;
		shellText = st;
		lineText = lt;
	}

	public int open()
	{
		Shell parent = getParent();
		Display display = parent.getDisplay();

		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
 		props.setLook(shell);

		FormLayout formLayout = new FormLayout ();
		formLayout.marginWidth  = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText(shellText);

		int length = Const.LENGTH;
		int margin = Const.MARGIN;

		// From step line
		wlNumber=new Label(shell, SWT.NONE);
		wlNumber.setText(lineText);
 		props.setLook(wlNumber);
		fdlNumber=new FormData();
		fdlNumber.left = new FormAttachment(0, 0);
		fdlNumber.top  = new FormAttachment(0, margin);
		wlNumber.setLayoutData(fdlNumber);
		wNumber=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wNumber.setText("100");
 		props.setLook(wNumber);
		fdNumber=new FormData();
		fdNumber.left = new FormAttachment(0, 0);
		fdNumber.top  = new FormAttachment(wlNumber, margin);
		fdNumber.right= new FormAttachment(0, length);
		wNumber.setLayoutData(fdNumber);

		// Some buttons
		wOK=new Button(shell, SWT.PUSH);
		wOK.setText("  &OK  ");
		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText("  &Cancel  ");
		fdOK=new FormData();
		fdOK.left       = new FormAttachment(33, 0);
		fdOK.top        = new FormAttachment(wNumber, margin*2);
		wOK.setLayoutData(fdOK);
		fdCancel=new FormData();
		fdCancel.left   = new FormAttachment(66, 0);
		fdCancel.top    = new FormAttachment(wNumber, margin*2);
		wCancel.setLayoutData(fdCancel);

		// Add listeners
		lsCancel   = new Listener() { public void handleEvent(Event e) { cancel(); } };
		lsOK       = new Listener() { public void handleEvent(Event e) { ok();     } };
		
		wOK.addListener    (SWT.Selection, lsOK     );
		wCancel.addListener(SWT.Selection, lsCancel );
		
		lsDef=new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } };
		wNumber.addSelectionListener(lsDef);
		
		// Detect [X] or ALT-F4 or something that kills this window...
		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { cancel(); } } );


		getData();
		
		WindowProperty winprop = props.getScreen(shell.getText());
		if (winprop!=null) winprop.setShell(shell); else shell.pack();
		
		shell.open();
		while (!shell.isDisposed())
		{
				if (!display.readAndDispatch()) display.sleep();
		}
		return samples;
	}

	public void dispose()
	{
		props.setScreen(new WindowProperty(shell));
		shell.dispose();
	}
	
	public void getData()
	{
		wNumber.setText(""+samples);
		wNumber.selectAll();
	}
	
	private void cancel()
	{
		samples=-1;
		dispose();
	}
	
	private void ok()
	{
		try 
		{ 
			samples = Integer.parseInt(wNumber.getText()); 
			dispose();
		}
		catch(Exception e) 
		{ 
			MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
			mb.setMessage("Please enter an integer number!"+Const.CR);
			mb.setText("ERROR");
			mb.open(); 
			wNumber.selectAll();
		}
	}
}
