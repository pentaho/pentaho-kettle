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
import be.ibridge.kettle.trans.step.BaseStepDialog;

/**
 * Dialog to enter a text. (descriptions etc.)
 * 
 * @author Matt
 * @since 19-06-2003
 */
public class EnterTextDialog extends Dialog
{
	private String title, message;
		
	private Label        wlDesc;
	private Text         wDesc;
    private FormData     fdlDesc, fdDesc;
		
	private Button wOK, wCancel;
	private Listener lsOK, lsCancel;

	private Shell  shell;
	private SelectionAdapter lsDef;
	private Props props;
	
	private String text;
	private boolean fixed;
	private boolean readonly, modal, singleLine;

    /**
     * @deprecated Please use the constructor without Props parameter 
     */
    public EnterTextDialog(Shell parent, Props pr, String t, String m, String desc, boolean fi)
    {
        this(parent, t, m, desc, fi);
    }

    /**
     * @deprecated Please use the constructor without Props parameter 
     */
    public EnterTextDialog(Shell parent, Props pr, String t, String m, String desc)
    {
        this(parent, t, m, desc);
    }

    /**
     * Dialog to allow someone to show or enter a text
     * @param parent The parent shell to use
     * @param title The dialog title
     * @param message The message to display
     * @param text The text to display or edit
     * @param fi true if you want the font to be in fixed-width
     */
	public EnterTextDialog(Shell parent, String title, String message, String text, boolean fi)
	{
		this(parent, title, message, text);
		fixed=fi;
	}

     /**
     * Dialog to allow someone to show or enter a text in variable width font
     * @param parent The parent shell to use
     * @param title The dialog title
     * @param message The message to display
     * @param text The text to display or edit
     */
	public EnterTextDialog(Shell parent, String title, String message, String text)
	{
		super(parent, SWT.NONE);
		props=Props.getInstance();
		this.title=title;
		this.message=message;
		this.text=text;
		fixed=false;
		readonly=false;
		singleLine=false;
	}
	
	public void setReadOnly()
	{
		readonly=true;
	}
	
	public void setModal()
	{
		modal=true;
	}
	
	public void setSingleLine()
	{
		singleLine=true;
	}

	public String open()
	{
		Shell parent = getParent();
		Display display = parent.getDisplay();

		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN |  (modal?SWT.APPLICATION_MODAL:SWT.NONE));
 		props.setLook(shell);

		FormLayout formLayout = new FormLayout ();
		formLayout.marginWidth  = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText(title);
		
		int margin = Const.MARGIN;

		// From step line
		wlDesc=new Label(shell, SWT.NONE);
		wlDesc.setText(message);
 		props.setLook(wlDesc);
		fdlDesc=new FormData();
		fdlDesc.left = new FormAttachment(0, 0);
		fdlDesc.top  = new FormAttachment(0, margin);
		wlDesc.setLayoutData(fdlDesc);
		
		if (singleLine)
			 wDesc=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER  );
		else wDesc=new Text(shell, SWT.MULTI  | SWT.LEFT | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL );
		
		wDesc.setText("");
 		if (fixed) props.setLook(wDesc, Props.WIDGET_STYLE_FIXED); else props.setLook(wDesc); 
		fdDesc=new FormData();
		fdDesc.left  = new FormAttachment(0, 0);
		fdDesc.top   = new FormAttachment(wlDesc, margin);
		fdDesc.right = new FormAttachment(100, 0);
		fdDesc.bottom= new FormAttachment(100, -50);
		wDesc.setLayoutData(fdDesc);
		wDesc.setEditable(!readonly);

		// Some buttons
		if (!readonly)
		{
			wOK=new Button(shell, SWT.PUSH);
			wOK.setText("  &OK  ");
			wCancel=new Button(shell, SWT.PUSH);
			wCancel.setText("  &Cancel  ");
			
			BaseStepDialog.positionBottomButtons(shell, new Button[] { wOK, wCancel }, margin, null);

			// Add listeners
			lsCancel   = new Listener() { public void handleEvent(Event e) { cancel(); } };
			lsOK       = new Listener() { public void handleEvent(Event e) { ok();     } };
			
			wOK.addListener    (SWT.Selection, lsOK     );
			wCancel.addListener(SWT.Selection, lsCancel );
		}
		else
		{
			wOK=new Button(shell, SWT.PUSH);
			wOK.setText("  &Close  ");

			BaseStepDialog.positionBottomButtons(shell, new Button[] { wOK }, margin, null);

			// Add listeners
			lsOK       = new Listener() { public void handleEvent(Event e) { ok();     } };
			wOK.addListener    (SWT.Selection, lsOK     );
		}
		
		lsDef=new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } };
		wDesc.addSelectionListener(lsDef);
		
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
		return text;
	}

	public void dispose()
	{
		props.setScreen(new WindowProperty(shell));
		shell.dispose();
	}
	
	public void getData()
	{
		if (text!=null) wDesc.setText(text);
	}
	
	private void cancel()
	{
		text=null;
		dispose();
	}
	
	private void ok()
	{
		text = wDesc.getText();
		dispose();
	}
}
