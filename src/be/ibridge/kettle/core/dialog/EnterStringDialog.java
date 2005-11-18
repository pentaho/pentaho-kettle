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
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.Props;
import be.ibridge.kettle.core.WindowProperty;

/**
 * This dialog allows you to enter a (single line) String.
 * 
 * @author Matt
 * @since 21-11-2004
 */
public class EnterStringDialog extends Dialog
{
	private Label        wlString;
	private Text         wString;
    private FormData     fdlString, fdString;
		
	private Button wOK, wCancel;
	private FormData fdOK, fdCancel;
	private Listener lsOK, lsCancel;

	private Shell  shell;
	private SelectionAdapter lsDef;
	
	private String string;
	private String shellText;
	private String lineText;
	private Props props;
		
	public EnterStringDialog(Shell parent, Props props, String string, String shellText, String lineText)
	{
		super(parent, SWT.NONE);
		this.props      = props;
		this.string     = string;
		this.shellText  = shellText;
		this.lineText   = lineText;
	}

	public String open()
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

		// The String line...
		wlString=new Label(shell, SWT.NONE);
		wlString.setText(lineText);
 		props.setLook(wlString);
		fdlString=new FormData();
		fdlString.left = new FormAttachment(0, 0);
		fdlString.top  = new FormAttachment(0, margin);
		wlString.setLayoutData(fdlString);
		wString=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wString.setText(string);
 		props.setLook(wString);
		fdString=new FormData();
		fdString.left = new FormAttachment(0, 0);
		fdString.top  = new FormAttachment(wlString, margin);
		fdString.right= new FormAttachment(0, length);
		wString.setLayoutData(fdString);

		// Some buttons
		wOK=new Button(shell, SWT.PUSH);
		wOK.setText("  &OK  ");
		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText("  &Cancel  ");
		fdOK=new FormData();
		fdOK.left       = new FormAttachment(33, 0);
		fdOK.top        = new FormAttachment(wString, margin*2);
		wOK.setLayoutData(fdOK);
		fdCancel=new FormData();
		fdCancel.left   = new FormAttachment(66, 0);
		fdCancel.top    = new FormAttachment(wString, margin*2);
		wCancel.setLayoutData(fdCancel);

		// Add listeners
		lsCancel   = new Listener() { public void handleEvent(Event e) { cancel(); } };
		lsOK       = new Listener() { public void handleEvent(Event e) { ok();     } };
		
		wOK.addListener    (SWT.Selection, lsOK     );
		wCancel.addListener(SWT.Selection, lsCancel );
		
		lsDef=new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } };
		wString.addSelectionListener(lsDef);
		
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
		return string;
	}

	public void dispose()
	{
		props.setScreen(new WindowProperty(shell));
		shell.dispose();
	}
	
	public void getData()
	{
		wString.setText(string);
		wString.selectAll();
	}
	
	private void cancel()
	{
		string=null;
		dispose();
	}
	
	private void ok()
	{
		string = wString.getText(); 
		dispose();
	}
}
