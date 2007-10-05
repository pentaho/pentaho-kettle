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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Image;
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
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.GUIResource;
import be.ibridge.kettle.core.LocalVariables;
import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.Props;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.WindowProperty;
import be.ibridge.kettle.core.database.DatabaseMeta;
import be.ibridge.kettle.core.dialog.DatabaseDialog;
import be.ibridge.kettle.core.dialog.ErrorDialog;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.util.StringUtil;
import be.ibridge.kettle.core.value.Value;
import be.ibridge.kettle.core.widget.TableView;
import be.ibridge.kettle.repository.Repository;
import be.ibridge.kettle.trans.StepLoader;
import be.ibridge.kettle.trans.TransMeta;

public class BaseStepDialog extends Dialog
{
    protected static LocalVariables localVariables = LocalVariables.getInstance();
    
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
    
    public void setShellImage(Shell shell, StepMetaInterface stepMetaInterface)
    {
        try
        {
            String id = StepLoader.getInstance().getStepPluginID(stepMetaInterface);
            if (id!=null)
            {
                shell.setImage((Image) GUIResource.getInstance().getImagesSteps().get(id));
            }
        }
        catch(Throwable e)
        {
        }
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
		setSize(shell);
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
    
    public static final ModifyListener getModifyListenerTooltipText(final Text textField)
    {
        return new ModifyListener()
        {
            public void modifyText(ModifyEvent e)
            {
                textField.setToolTipText(StringUtil.environmentSubstitute( textField.getText() ) );
            }
        };
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

    public CCombo addConnectionLine(Composite parent, Control previous, int middle, int margin)
    {
        return addConnectionLine(parent, previous, middle, margin, new Label(parent, SWT.RIGHT), new Button(parent, SWT.PUSH), new Button(parent, SWT.PUSH));
    }

	public CCombo addConnectionLine(Composite parent, Control previous, int middle, int margin, final Label wlConnection, final Button wbnConnection, final Button wbeConnection)
	{
		final CCombo       wConnection;
		final FormData     fdlConnection, fdbConnection, fdeConnection, fdConnection;

		wConnection=new CCombo(parent, SWT.BORDER | SWT.READ_ONLY);
 		props.setLook(wConnection);

		addDatabases(wConnection);

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
		wbnConnection.setText(Messages.getString("BaseStepDialog.NewConnectionButton.Label")); //$NON-NLS-1$
		wbnConnection.addSelectionListener(new SelectionAdapter() 
		{
			public void widgetSelected(SelectionEvent e) 
			{
				DatabaseMeta databaseMeta = new DatabaseMeta();
				DatabaseDialog cid = new DatabaseDialog(shell, databaseMeta);
				cid.setModalDialog(true);
				if (cid.open()!=null)
				{
					transMeta.addDatabase(databaseMeta);
					wConnection.removeAll();
					addDatabases(wConnection);
					selectDatabase(wConnection, databaseMeta.getName());
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
		wbeConnection.setText(Messages.getString("BaseStepDialog.EditConnectionButton.Label")); //$NON-NLS-1$
		wbeConnection.addSelectionListener(new SelectionAdapter() 
		{
			public void widgetSelected(SelectionEvent e) 
			{
				DatabaseMeta databaseMeta = transMeta.findDatabase(wConnection.getText());
				if (databaseMeta!=null)
				{
					DatabaseDialog cid = new DatabaseDialog(shell, databaseMeta);
					cid.setModalDialog(true);
					if (cid.open()!=null)
					{
						wConnection.removeAll();
						addDatabases(wConnection);
						selectDatabase(wConnection, databaseMeta.getName());
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

	public static void setSize(Shell shell)
	{
		setSize(shell, -1, -1, true);
	}

	public static void setSize(Shell shell, int minWidth, int minHeight, boolean packIt)
	{
		Props props = Props.getInstance();
		
		WindowProperty winprop = props.getScreen(shell.getText());
		if (winprop!=null) 
		{
			winprop.setShell(shell, minWidth, minHeight);
		}
		else
		{
			if (packIt) shell.pack(); else shell.layout();
			
			// OK, sometimes this produces dialogs that are waay too big.
			// Try to limit this a bit, m'kay?
			// Use the same algorithm by cheating :-)
			//
			winprop = new WindowProperty(shell);
			winprop.setShell(shell, minWidth, minHeight);
			
			// Now, as this is the first time it gets opened, try to put it in the middle of the screen...
			Rectangle shellBounds = shell.getBounds();
			Rectangle dispBounds = shell.getDisplay().getPrimaryMonitor().getBounds();
			
			int middleX = (dispBounds.width - shellBounds.width)/2;
			int middleY = (dispBounds.height - shellBounds.height)/2;
			
			shell.setLocation(middleX, middleY);
		}
	}

    public static final void setTraverseOrder(final Control[] controls)
    {
        for (int i=0;i<controls.length;i++)
        {
            final int controlNr = i;
            if (i<controls.length-1)
            {
                controls[i].addTraverseListener(new TraverseListener()
                    {
                        public void keyTraversed(TraverseEvent te)
                        {
                            te.doit=false;
                            // set focus on the next control.
                            // What is the next control?
                            int thisOne = controlNr+1;
                            while (!controls[thisOne].isEnabled())
                            {
                                thisOne++;
                                if (thisOne>=controls.length) thisOne=0;
                                if (thisOne==controlNr) return; // already tried all others, time to quit.
                            }
                            controls[thisOne].setFocus();
                        }
                    }
                );
            }
            else // Link last item to first.
            {
                controls[i].addTraverseListener(new TraverseListener()
                    {
                        public void keyTraversed(TraverseEvent te)
                        {
                            te.doit=false;
                            // set focus on the next control.
                            // set focus on the next control.
                            // What is the next control : 0
                            int thisOne = 0;
                            while (!controls[thisOne].isEnabled())
                            {
                                thisOne++;
                                if (thisOne>=controls.length) return; // already tried all others, time to quit.
                            }
                            controls[thisOne].setFocus();
                        }
                    }
                );            
            }
        }
    }
    
    /**
     * Gets unused fields from previous steps and inserts them as rows into a table view.
     * @param r
     * @param fields
     * @param i
     * @param js the column in the table view to match with the names of the fields, checks for existance if >0 
     * @param nameColumn
     * @param j
     * @param lengthColumn
     * @param listener
     */
    public static final void getFieldsFromPrevious(TransMeta transMeta, StepMeta stepMeta, TableView tableView, int keyColumn, int nameColumn[], int dataTypeColumn[], int lengthColumn, int precisionColumn, TableItemInsertListener listener)
    {
        try
        {
            Row row = transMeta.getPrevStepFields(stepMeta);
            if (row!=null)
            {
                getFieldsFromPrevious(row, tableView, keyColumn, nameColumn, dataTypeColumn, lengthColumn, precisionColumn, listener);
            }
        }
        catch(KettleException ke)
        {
            new ErrorDialog(tableView.getShell(), Messages.getString("BaseStepDialog.FailedToGetFields.Title"), Messages.getString("BaseStepDialog.FailedToGetFields.Message", stepMeta.getName()), ke); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }
    
    /**
     * Gets unused fields from previous steps and inserts them as rows into a table view.
     * @param row the input fields
     * @param tableView the table view to modify
     * @param keyColumn the column in the table view to match with the names of the fields, checks for existance if >0 
     * @param nameColumn the column numbers in which the name should end up in
     * @param dataTypeColumn the target column numbers in which the data type should end up in
     * @param lengthColumn the length column where the length should end up in (if >0)
     * @param precisionColumn the length column where the precision should end up in (if >0)
     * @param listener A listener that you can use to do custom modifications to the inserted table item, based on a value from the provided row
     */
    public static final void getFieldsFromPrevious(Row row, TableView tableView, int keyColumn, int nameColumn[], int dataTypeColumn[], int lengthColumn, int precisionColumn, TableItemInsertListener listener)
    {
        if (row==null || row.size()==0) return; // nothing to do
        
        Table table = tableView.table;
        
        // get a list of all the non-empty keys (names)
        //
        List keys = new ArrayList();
        for (int i=0;i<table.getItemCount();i++)
        {
            TableItem tableItem = table.getItem(i);
            String key = tableItem.getText(keyColumn);
            if (!Const.isEmpty(key) && keys.indexOf(key)<0) keys.add(key);
        }
        
        int choice = 0;
        
        if (keys.size()>0)
        {
            // Ask what we should do with the existing data in the step.
            //
            MessageDialog md = new MessageDialog(tableView.getShell(), 
                    Messages.getString("BaseStepDialog.GetFieldsChoice.Title"),//"Warning!" 
                    null,
                    Messages.getString("BaseStepDialog.GetFieldsChoice.Message", ""+keys.size(), ""+row.size()),
                    MessageDialog.WARNING,
                    new String[] { Messages.getString("BaseStepDialog.AddNew"), Messages.getString("BaseStepDialog.Add"), Messages.getString("BaseStepDialog.ClearAndAdd"), Messages.getString("BaseStepDialog.Cancel"), },   
                    0
                );
            int idx = md.open();
            choice = idx&0xFF;
        }
        
        if (choice==3 || choice == 255 /* 255 = escape pressed */) return; // Cancel clicked

        if (choice==2)
        {
            tableView.clearAll(false);
        }
        
        for (int i=0;i<row.size();i++)
        {
            Value v = row.getValue(i);
            
            boolean add = true;
            
            if (choice==0) // hang on, see if it's not yet in the table view
            {
                if (keys.indexOf(v.getName())>=0) add=false; 
            }
            
            if (add)
            {
                TableItem tableItem = new TableItem(table, SWT.NONE);
                
                for (int c=0;c<nameColumn.length;c++)
                {
                    tableItem.setText(nameColumn[c], Const.NVL(v.getName(), ""));
                }
                if ( dataTypeColumn != null )
                {
                    for (int c=0;c<dataTypeColumn.length;c++)
                    {
                        tableItem.setText(dataTypeColumn[c], v.getTypeDesc());
                    }
                }
                if (lengthColumn>0)
                {
                    if (v.getLength()>=0) tableItem.setText(lengthColumn, Integer.toString(v.getLength()) );
                }
                if (precisionColumn>0)
                {
                    if (v.getPrecision()>=0) tableItem.setText(precisionColumn, Integer.toString(v.getPrecision()) );
                }
                
                if (listener!=null)
                {
                    if (!listener.tableItemInserted(tableItem, v))
                    {
                        tableItem.dispose(); // remove it again
                    }
                }
            }
        }
        tableView.removeEmptyRows();
        tableView.setRowNums();
        tableView.optWidth(true);
    }
}
