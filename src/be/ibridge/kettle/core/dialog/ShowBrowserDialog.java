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
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.Props;

/**
 * Displays an HTML page.
 * 
 * @author Matt
 * @since 22-12-2005
 */
public class ShowBrowserDialog extends Dialog
{
    private String       dialogTitle;
    private String       content;
		
    private Button       wOK;
    private FormData     fdOK;
    private Listener     lsOK;
    
	private Browser      wBrowser;
    private FormData     fdBrowser;
    
	private Shell  shell;
	private Props props;
	
	private int prefWidth = -1;
	private int prefHeight = -1;
	
	private int buttonHeight = 30;

    public ShowBrowserDialog(Shell parent, String dialogTitle, String content)
    {
        super(parent, SWT.NONE);
        props=Props.getInstance();
        this.dialogTitle = dialogTitle;
        this.content = content;
        prefWidth = -1;
        prefHeight = -1;
    }

	public void open()
	{
		Shell parent = getParent();
		Display display = parent.getDisplay();
		
		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.RESIZE | SWT.MAX | SWT.MIN);
 		props.setLook(shell);

		FormLayout formLayout = new FormLayout ();
		formLayout.marginWidth  = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText(dialogTitle);
		
		int margin = Const.MARGIN;
		
		// Canvas
		wBrowser=new Browser(shell, SWT.NONE);
 		props.setLook(wBrowser);

		fdBrowser=new FormData();
		fdBrowser.left  = new FormAttachment(0, 0);
		fdBrowser.top   = new FormAttachment(0, margin);
		fdBrowser.right = new FormAttachment(100, 0);
		fdBrowser.bottom= new FormAttachment(100, -buttonHeight);
		wBrowser.setLayoutData(fdBrowser);

		// Some buttons
		wOK=new Button(shell, SWT.PUSH);
		wOK.setText("  &OK  ");
		fdOK=new FormData();
		fdOK.left       = new FormAttachment(50, 0);
		fdOK.bottom     = new FormAttachment(100, 0);
		wOK.setLayoutData(fdOK);

		// Add listeners
		lsOK       = new Listener() { public void handleEvent(Event e) { ok();     } };
		
		wOK.addListener    (SWT.Selection, lsOK     );
				
		// Detect [X] or ALT-F4 or something that kills this window...
		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { ok(); } } );


		//shell.pack();
		if (prefWidth>0 && prefHeight>0)
		{
			shell.setSize(prefWidth, prefHeight);
			Rectangle r = shell.getClientArea();
			int diffx = prefWidth - r.width;
			int diffy = prefHeight - r.height;
			shell.setSize(prefWidth+diffx, prefHeight+diffy);
		}
		else
		{
			shell.setSize(400, 400);
		}

		getData();
		
		shell.open();
		while (!shell.isDisposed())
		{
				if (!display.readAndDispatch()) display.sleep();
		}
	}

	public void dispose()
	{
		shell.dispose();
	}
	
	public void getData()
	{
        wBrowser.setText(content);
	}
	
	private void ok()
	{
		dispose();
	}

}
