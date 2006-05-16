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
 * Created on 2-jul-2003
 *
 */

package be.ibridge.kettle.trans.step;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.Props;
import be.ibridge.kettle.core.WindowProperty;
import be.ibridge.kettle.core.database.DatabaseMeta;
import be.ibridge.kettle.core.dialog.DatabaseDialog;
import be.ibridge.kettle.repository.Repository;
import be.ibridge.kettle.trans.TransMeta;


public class BaseStepDialog extends Dialog
{
	protected LogWriter    log;
	protected String       stepname;
		
	protected Label        wlStepname;
	protected Text         wStepname;
	protected FormData     fdlStepname, fdStepname;

	protected Button wOK, wGet, wPreview, wSQL, wCreate, wCancel;
	protected FormData fdOK, fdGet, fdPreview, fdSQL, fdCreate, fdCancel;
	protected Listener lsOK, lsGet, lsPreview, lsSQL, lsCreate, lsCancel;

	protected TransMeta transMeta;
	protected Shell  shell;
	
	protected SelectionAdapter lsDef;
	protected Listener lsResize;
	protected boolean changed, backupChanged;
	protected BaseStepMeta baseInput;
	protected Props props;
    protected Repository repository;
	
	public BaseStepDialog(Shell parent, BaseStepMeta in, TransMeta transMeta, String sname)
	{
		super(parent, SWT.NONE);
		log=LogWriter.getInstance();
		this.transMeta=transMeta;
		stepname=sname;
		baseInput=in;
		backupChanged=in.hasChanged();
		this.props = Props.getInstance();
	}

	public BaseStepDialog(Shell parent, int nr, BaseStepMeta in, TransMeta tr)
	{
		this(parent, in, tr, null);
	}

	public void dispose()
	{
		WindowProperty winprop = new WindowProperty(shell);
		props.setScreen(winprop);
		shell.dispose();
	}
	
	/**
	 * Set the shell size, based upon the previous time the geometry was saved in the Properties file.
	 */
	public void setSize()
	{
		WindowProperty winprop = props.getScreen(shell.getText());
		if (winprop!=null) winprop.setShell(shell); else shell.pack();
	}

	public static final void setSize(Shell sh, boolean max, Rectangle r)
	{
		// Set the shell size, based upon previous time...
		sh.setMaximized(max);
		if (r!=null && r.x>=0 && r.y>=0 && r.width>=0 && r.height>=0)
		{
			if (r.x>0 && r.y>0) sh.setSize(r.width, r.height);
			if (r.width>0 && r.height>0) sh.setLocation(r.x, r.y);
		}
		else
		{
			sh.pack();
		}	
	}
	
	protected void setButtonPositions(Button buttons[], int margin, Control lastControl)
	{
		BaseStepDialog.positionBottomButtons(shell, buttons, margin, lastControl);
	}

	/**
	 * Position the specified buttons at the bottom of the parent composite.
	 * Also, make the buttons all the same width: the width of the largest button.
	 * @param buttons The buttons to position.
	 * @param margin The margin between the buttons in pixels
	 */
	public static final void positionBottomButtons(Composite composite, Button buttons[], int margin, Control lastControl)
	{
		// Determine the largest button in the array
		Rectangle largest = null;
		for (int i=0;i<buttons.length;i++)
		{
			buttons[i].pack(true);
			Rectangle r = buttons[i].getBounds();
			if (largest==null || r.width > largest.width) largest = r;
			
			// Also, set the tooltip the same as the name if we don't have one...
			if (buttons[i].getToolTipText()==null)
			{
				buttons[i].setToolTipText( Const.replace(buttons[i].getText(), "&", "")); //$NON-NLS-1$ //$NON-NLS-2$
			} 
		}
		
		// Make buttons a bit larger... (nicer)
		largest.width+=10;
		if ( (largest.width % 2) == 1 ) largest.width++;
				
		int middle_left  = 0;
		int middle_right = 0;
		int middle = buttons.length / 2;
		if ( (buttons.length % 2) != 0 ) 
		{
			// odd number of buttons... the center of the middle 
			// button will be on the center of the parent
			middle_left  = middle;
			middle_right = middle;

			FormData fd1 = new FormData();	
			fd1.left   = new FormAttachment(50, -(largest.width + margin)/2);
			fd1.right  = new FormAttachment(50, (largest.width + margin)/2);
			if (lastControl!=null) fd1.top = new FormAttachment(lastControl, margin*3);
			if (lastControl==null) fd1.bottom = new FormAttachment(100, 0);

			buttons[middle].setLayoutData(fd1);					
		}
		else
		{
			// Even number of buttons
			middle_left  = middle - 1;
			middle_right = middle;

			FormData fd1 = new FormData();	
			fd1.left   = new FormAttachment(50, -(largest.width + margin) - margin);
			fd1.right  = new FormAttachment(50, -margin);
			if (lastControl!=null) fd1.top = new FormAttachment(lastControl, margin*3);
			if (lastControl==null) fd1.bottom = new FormAttachment(100, 0);
			buttons[middle_left].setLayoutData(fd1);			

			FormData fd2 = new FormData();	
			fd2.left     = new FormAttachment(buttons[middle_left], margin);
			fd2.right    = new FormAttachment(buttons[middle_right], largest.width + margin); // 2
			if (lastControl!=null) fd2.top = new FormAttachment(lastControl, margin*3);
			if (lastControl==null) fd2.bottom = new FormAttachment(100, 0);
			buttons[middle_right].setLayoutData(fd2);							
		}
		
		for ( int ydx = middle_right+1; ydx < buttons.length; ydx++ )
		{
			// Do the buttons to the right of the middle button
			FormData fd = new FormData();
			fd.left = new FormAttachment(buttons[ydx-1], margin);
			fd.right = new FormAttachment(buttons[ydx], largest.width + margin);
			if (lastControl!=null) fd.top = new FormAttachment(lastControl, margin*3);
			if (lastControl==null) fd.bottom = new FormAttachment(100, 0);
			
			buttons[ydx].setLayoutData(fd);							
		}
	
		for ( int zdx = middle_left-1; zdx >= 0; zdx-- )
		{
			// Do the buttons to the left of the middle button
			FormData fd = new FormData();
			fd.left = new FormAttachment(buttons[zdx+1], -(2 * (largest.width + margin)) - margin);
			fd.right = new FormAttachment(buttons[zdx], largest.width + margin);
			if (lastControl!=null) fd.top = new FormAttachment(lastControl, margin*3);
			if (lastControl==null) fd.bottom = new FormAttachment(100, 0);
			
			buttons[zdx].setLayoutData(fd);							

		}
	}		
	
	public void addDatabases(CCombo wConnection)
	{
		for (int i=0;i<transMeta.nrDatabases();i++)
		{
			DatabaseMeta ci = transMeta.getDatabase(i);
			wConnection.add(ci.getName());
		}
	}
	
	public void selectDatabase(CCombo wConnection, String name)
	{
		int idx = wConnection.indexOf(name);
		if (idx>=0)
		{
			wConnection.select(idx);
		}
	}
	
	public CCombo addConnectionLine(Composite parent, 
									Control previous, 
									int middle, 
                                    int margin
									)
	{
		final Label        wlConnection;
		final Button       wbnConnection;
		final Button       wbeConnection;
		final CCombo       wConnection;
		final FormData     fdlConnection, fdbConnection, fdeConnection, fdConnection;

		wConnection=new CCombo(parent, SWT.BORDER | SWT.READ_ONLY);
 		props.setLook(wConnection);

		addDatabases(wConnection);

		wlConnection=new Label(parent, SWT.RIGHT);
		wlConnection.setText(Messages.getString("BaseStepDialog.Connection.Label")); //$NON-NLS-1$
 		props.setLook(wlConnection);
		fdlConnection=new FormData();
		fdlConnection.left = new FormAttachment(0, 0);
		fdlConnection.right= new FormAttachment(middle, -margin);
		if (previous!=null) fdlConnection.top  = new FormAttachment(previous, margin);
		else                fdlConnection.top  = new FormAttachment(0, 0);
		wlConnection.setLayoutData(fdlConnection);

		// 
		// NEW button
		//
		wbnConnection=new Button(parent, SWT.PUSH);
		wbnConnection.setText(Messages.getString("BaseStepDialog.NewConnectionButton.Label")); //$NON-NLS-1$
		wbnConnection.addSelectionListener(new SelectionAdapter() 
		{
			public void widgetSelected(SelectionEvent e) 
			{
				DatabaseMeta ci = new DatabaseMeta();
				DatabaseDialog cid = new DatabaseDialog(shell, SWT.NONE, log, ci, props);
				if (cid.open()!=null)
				{
					transMeta.addDatabase(ci);
					wConnection.removeAll();
					addDatabases(wConnection);
					selectDatabase(wConnection, ci.getName());
				}
			}
		});
		fdbConnection=new FormData();
		fdbConnection.right= new FormAttachment(100, 0);
		if (previous!=null) fdbConnection.top  = new FormAttachment(previous, margin);
		else                fdbConnection.top  = new FormAttachment(0,0);
		wbnConnection.setLayoutData(fdbConnection);

		//
		// Edit button
		//
		wbeConnection=new Button(parent, SWT.PUSH);
		wbeConnection.setText(Messages.getString("BaseStepDialog.EditConnectionButton.Label")); //$NON-NLS-1$
		wbeConnection.addSelectionListener(new SelectionAdapter() 
		{
			public void widgetSelected(SelectionEvent e) 
			{
				DatabaseMeta ci = transMeta.findDatabase(wConnection.getText());
				if (ci!=null)
				{
					DatabaseDialog cid = new DatabaseDialog(shell, SWT.NONE, log, ci, props);
					if (cid.open()!=null)
					{
						wConnection.removeAll();
						addDatabases(wConnection);
						selectDatabase(wConnection, ci.getName());
					}
				}
			}
		});
		fdeConnection=new FormData();
		fdeConnection.right= new FormAttachment(wbnConnection, -margin);
		if (previous!=null) fdeConnection.top  = new FormAttachment(previous, margin);
		else                fdeConnection.top  = new FormAttachment(0, 0);
		wbeConnection.setLayoutData(fdeConnection);

		//
		// what's left of the line: combo box
		//
		fdConnection=new FormData();
		fdConnection.left = new FormAttachment(middle, 0);
		if (previous!=null) fdConnection.top  = new FormAttachment(previous, margin);
		else                fdConnection.top  = new FormAttachment(0, 0);
		fdConnection.right= new FormAttachment(wbeConnection, -margin);
		wConnection.setLayoutData(fdConnection);

		return wConnection;
	}
		
	public void storeScreenSize()
	{
		props.setScreen(new WindowProperty(shell));
	}
	
	public String toString()
	{
		return this.getClass().getName();
	}

    /**
     * @return Returns the repository.
     */
    public Repository getRepository()
    {
        return repository;
    }

    /**
     * @param repository The repository to set.
     */
    public void setRepository(Repository repository)
    {
        this.repository = repository;
    }

    public static void setMinimalShellHeight(Shell shell, Control[] controls, int margin, int extra)
    {
        int height = 0;
        
        for (int i=0;i<controls.length;i++)
        {
            Rectangle bounds = controls[i].getBounds();
            height+=bounds.height+margin;
        }
        height+=extra;
        shell.setSize(shell.getBounds().width, height);
    }
}
