 /* Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.*/

 

package org.pentaho.di.ui.core.dialog;
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
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Props;
import org.pentaho.di.ui.core.dialog.Messages;
import org.pentaho.di.ui.core.gui.WindowProperty;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.ui.core.PropsUI;

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
	private Listener lsOK, lsCancel;

	private Shell  shell;
	private SelectionAdapter lsDef;
	
	private String choices[];
	private String selection;
	private int    selectionNr;
	private String shellText;
	private String lineText;
	private PropsUI props;
	
	private boolean viewOnly, modal;
	private int selectedNrs[];
    private boolean multi;
    private int[] indices;
    private boolean fixed;
    
    /**
     * @deprecated Use CT without <i>props</i> parameter
     */
    public EnterSelectionDialog(Shell parent, PropsUI props, String choices[], String shellText, String lineText)
    {
        this(parent, choices, shellText, lineText);
        this.props=props;
    }
    
    /**
     * Create a new dialog allow someone to pick one value out of a list of values
     * @param parent the parent shell.
     * @param choices The available list of options
     * @param shellText The shell text
     * @param message the message to display as extra information about the possible choices
     */
    public EnterSelectionDialog(Shell parent, String choices[], String shellText, String message)
    {
        super(parent, SWT.NONE);
        
        this.choices = choices;
        this.shellText = shellText;
        this.lineText = message;
        
        props=PropsUI.getInstance();
        selection = null;
        viewOnly = false;
        modal = true;
        selectedNrs = new int[] {};
        multi=false;
        fixed=false;
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
		selectedNrs = new int[] { nr };
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
		shell.setImage(GUIResource.getInstance().getImageSpoon());
		
		int margin = Const.MARGIN;

		// From step line
		wlSelection=new Label(shell, SWT.NONE);
		wlSelection.setText(lineText);
 		props.setLook(wlSelection);
		fdlSelection=new FormData();
		fdlSelection.left = new FormAttachment(0, 0);
		fdlSelection.top  = new FormAttachment(0, margin);
		wlSelection.setLayoutData(fdlSelection);
        
        int options = SWT.LEFT | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL;
        if (multi) options|=SWT.MULTI; else options|=SWT.SINGLE;  
        
		wSelection=new List(shell, options );
		for (int i=0;i<choices.length;i++) wSelection.add(choices[i]);
		if (selectedNrs!=null)
		{
			wSelection.select(selectedNrs);
			wSelection.showSelection();
		}
        if (fixed)
        {
            props.setLook(wSelection, Props.WIDGET_STYLE_FIXED);
        }
        else
        {
            props.setLook(wSelection);
        }
		

		// Some buttons
		wOK=new Button(shell, SWT.PUSH);
		if (viewOnly) 
		{
			wOK.setText(Messages.getString("System.Button.Close"));
		} 
		else
		{
			wOK.setText(Messages.getString("System.Button.OK"));
		}
		lsOK       = new Listener() { public void handleEvent(Event e) { ok();     } };
		wOK.addListener    (SWT.Selection, lsOK     );
        
        Button[] buttons = new Button[] { wOK };
		
		if (!viewOnly)
		{
			wCancel=new Button(shell, SWT.PUSH);
			wCancel.setText(Messages.getString("System.Button.Cancel"));
			lsCancel   = new Listener() { public void handleEvent(Event e) { cancel(); } };
			wCancel.addListener(SWT.Selection, lsCancel );
            
            buttons = new Button[] { wOK, wCancel };
		}
        
        BaseStepDialog.positionBottomButtons(shell, buttons, margin, null);

        fdSelection=new FormData();
        fdSelection.left = new FormAttachment(0, 0);
        fdSelection.right= new FormAttachment(100, 0);
        fdSelection.top  = new FormAttachment(wlSelection, margin);
        fdSelection.bottom= new FormAttachment(wOK, -margin*3);
        wSelection.setLayoutData(fdSelection);
        
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
		
		BaseStepDialog.setSize(shell);
		
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
            indices = wSelection.getSelectionIndices();
		}
		else
		{
			selection = null;
			selectionNr = -1;
            indices = new int[0];
		}
		dispose();
	}
	
	public int getSelectionNr(String str)
	{
		for (int i=0;i<choices.length;i++)
		{
			if (choices[i].equalsIgnoreCase(str)) return i;
		}
		return -1;
	}
	
	public int getSelectionNr()
	{
		return selectionNr;
	}

    public boolean isMulti()
    {
        return multi;
    }

    public void setMulti(boolean multi)
    {
        this.multi = multi;
    }
    
    public int[] getSelectionIndeces()
    {
        return indices;
    }

    /**
     * @return the fixed
     */
    public boolean isFixed()
    {
        return fixed;
    }

    /**
     * @param fixed the fixed to set
     */
    public void setFixed(boolean fixed)
    {
        this.fixed = fixed;
    }

    /**
     * @return the selectedNrs
     */
    public int[] getSelectedNrs()
    {
        return selectedNrs;
    }

    /**
     * @param selectedNrs the selectedNrs to set
     */
    public void setSelectedNrs(int[] selectedNrs)
    {
        this.selectedNrs = selectedNrs;
    }
}
