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
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
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
import be.ibridge.kettle.core.Props;
import be.ibridge.kettle.core.WindowProperty;

/**
 * Allows the user to make a selection from a list of values.
 * 
 * @author Matt
 * @since 19-06-2003
 */
public class EnterSelectionDialog extends Dialog
{
	private Label        wlSelection;
	private List         wSelection;
    private FormData     fdlSelection, fdSelection;
		
	private Button wOK, wCancel;
	private FormData fdOK, fdCancel;
	private Listener lsOK, lsCancel;

	private Shell  shell;
	private SelectionAdapter lsDef;
	
	private String list[];
	private String selection;
	private int    selectionNr;
	private String shellText;
	private String lineText;
	private Props props;
	
	private boolean viewOnly, modal;
	private int selectedNr;
		
	/**
	 * Create a new dialog allow someone to pick one value out of a list of values
	 * @param parent
	 * @param pr
	 * @param lst The list of options
	 * @param st The shell text
	 * @param lt the line text to display as information
	 */
	public EnterSelectionDialog(Shell parent, Props pr, String lst[], String st, String lt)
	{
		super(parent, SWT.NONE);
		props=pr;
		list = lst;
		shellText = st;
		lineText = lt;
		selection = null;
		viewOnly = false;
		modal = true;
		selectedNr = -1;
	}
	
	public void setViewOnly()
	{
		viewOnly = true;
	}
	
	public void clearModal()
	{
		modal = false;
	}
	
	public String open(int nr)
	{
		selectedNr = nr;
		return open();
	}

	public String open()
	{
		Shell parent = getParent();
		Display display = parent.getDisplay();

		shell = new Shell(parent, SWT.DIALOG_TRIM | (modal?SWT.APPLICATION_MODAL:SWT.NONE) | SWT.RESIZE | SWT.MIN | SWT.MAX );
 		props.setLook(shell);

		FormLayout formLayout = new FormLayout ();
		formLayout.marginWidth  = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText(shellText);
		
		int margin = Const.MARGIN;

		// From step line
		wlSelection=new Label(shell, SWT.NONE);
		wlSelection.setText(lineText);
 		props.setLook(wlSelection);
		fdlSelection=new FormData();
		fdlSelection.left = new FormAttachment(0, 0);
		fdlSelection.top  = new FormAttachment(0, margin);
		wlSelection.setLayoutData(fdlSelection);
		wSelection=new List(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		for (int i=0;i<list.length;i++) wSelection.add(list[i]);
		if (selectedNr>=0)
		{
			wSelection.select(selectedNr);
			wSelection.showSelection();
		}
 		props.setLook(wSelection);
		fdSelection=new FormData();
		fdSelection.left = new FormAttachment(0, 0);
		fdSelection.right= new FormAttachment(100, 0);
		fdSelection.top  = new FormAttachment(wlSelection, margin);
		fdSelection.bottom= new FormAttachment(100, -30);
		wSelection.setLayoutData(fdSelection);

		// Some buttons
		wOK=new Button(shell, SWT.PUSH);
		fdOK=new FormData();
		if (viewOnly) 
		{
			wOK.setText(" Cl&ose ");
			fdOK.left       = new FormAttachment(50, 0);
			fdOK.bottom     = new FormAttachment(100, 0);
		} 
		else
		{
			wOK.setText("  &OK  ");
			fdOK.left       = new FormAttachment(33, 0);
			fdOK.bottom     = new FormAttachment(100, 0);
		}
		wOK.setLayoutData(fdOK);
		lsOK       = new Listener() { public void handleEvent(Event e) { ok();     } };
		wOK.addListener    (SWT.Selection, lsOK     );
		
		if (!viewOnly)
		{
			wCancel=new Button(shell, SWT.PUSH);
			wCancel.setText("  &Cancel  ");
			fdCancel=new FormData();
			fdCancel.left   = new FormAttachment(66, 0);
			fdCancel.bottom = new FormAttachment(100, 0);
			wCancel.setLayoutData(fdCancel);
			lsCancel   = new Listener() { public void handleEvent(Event e) { cancel(); } };
			wCancel.addListener(SWT.Selection, lsCancel );
		}

		// Add listeners
		
		lsDef=new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } };
		wSelection.addSelectionListener(lsDef);
		wSelection.addKeyListener(new KeyAdapter() 
			{
				public void keyPressed(KeyEvent e) 
				{
					if (e.character == SWT.CR) ok();
				}
			}
		);
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
		return selection;
	}

	public void dispose()
	{
		props.setScreen(new WindowProperty(shell));
		shell.dispose();
	}
	
	public void getData()
	{
	}
	
	private void cancel()
	{
		selection=null;
		dispose();
	}
	
	private void ok()
	{
		if (wSelection.getSelectionCount()>0)
		{
			selection    = wSelection.getSelection()[0];
			selectionNr = wSelection.getSelectionIndices()[0];
		}
		else
		{
			selection = null;
			selectionNr = -1;
		}
		dispose();
	}
	
	public int getSelectionNr(String str)
	{
		for (int i=0;i<list.length;i++)
		{
			if (list[i].equalsIgnoreCase(str)) return i;
		}
		return -1;
	}
	
	public int getSelectionNr()
	{
		return selectionNr;
	}
}
