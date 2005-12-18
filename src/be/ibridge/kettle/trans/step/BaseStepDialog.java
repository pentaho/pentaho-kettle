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
		}
		
		// Make buttons a bit larger... (nicer)
		largest.width+=10;
		
		// What's the total size of the shell...
		Rectangle sb = composite.getBounds();
		// --> sb.width is the room we have.
		// So let's put the buttons as close to the center as possible
		
		int spaceLeft = sb.width - ( largest.width*buttons.length + (buttons.length-1)*margin ); 
		int leftpct = 100 * spaceLeft / ( 2 * sb.width ); 
		
		// OEPS, this doesn't work as the shell isn't displayed yet!!!
		// No real solution, just put it at 20% always...
		leftpct = 20;
		
		// System.out.println("Shell width: "+sb.width+", spaceLeft="+spaceLeft+", leftpct="+leftpct);
		
		// System.out.println("width="+sb.width+", spaceLeft="+spaceLeft+", leftpct="+leftpct);
		
		// Set the layouts of the buttons...
		for (int i=0;i<buttons.length;i++)
		{
			FormData fd = new FormData();
			fd.left   = new FormAttachment(leftpct, i*(margin+largest.width));
			if (lastControl!=null) fd.top = new FormAttachment(lastControl, margin*3);
			fd.right  = new FormAttachment(leftpct, i*(margin+largest.width)+largest.width);
			if (lastControl==null) fd.bottom = new FormAttachment(100, 0);
			buttons[i].setLayoutData(fd);
			
			// Also, set the tooltip the same as the name if we don't have one...
			if (buttons[i].getToolTipText()==null)
			{
				buttons[i].setToolTipText( Const.replace(buttons[i].getText(), "&", ""));
			}
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
		wlConnection.setText("Connection ");
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
		wbnConnection.setText("&New...");
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
		wbeConnection.setText("&Edit...");
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
}
