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
 
package org.pentaho.di.ui.trans.steps.blockingstep;

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
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.steps.blockingstep.BlockingStepMeta;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.trans.step.BaseStepDialog;


public class BlockingStepDialog extends BaseStepDialog implements StepDialogInterface
{
	private static Class<?> PKG = BlockingStepMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

    private BlockingStepMeta input;
    
	private Label        wlPassAllRows;
	private Button       wPassAllRows;
	
	private Label        wlSpoolDir;
	private Button       wbSpoolDir;
	private TextVar      wSpoolDir;
	private FormData     fdlSpoolDir, fdbSpoolDir, fdSpoolDir;

	private Label        wlPrefix;
	private Text         wPrefix;
	private FormData     fdlPrefix, fdPrefix;

    private Label        wlCacheSize;
    private Text         wCacheSize;
    private FormData     fdlCacheSize, fdCacheSize;

    private Label        wlCompress;
    private Button       wCompress;
    private FormData     fdlCompress, fdCompress;
   
    public BlockingStepDialog(Shell parent, Object in, TransMeta transMeta, String sname)
    {
        super(parent, (BaseStepMeta)in, transMeta, sname);
        input=(BlockingStepMeta)in;
    }

    public String open()
    {
        Shell parent = getParent();
        Display display = parent.getDisplay();

        shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MIN | SWT.MAX);
        props.setLook(shell);
        setShellImage(shell, input);

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
        shell.setText(BaseMessages.getString(PKG, "BlockingStepDialog.Shell.Title")); //$NON-NLS-1$
        
        int middle = props.getMiddlePct();
        int margin = Const.MARGIN;
        
        // Stepname line
        wlStepname=new Label(shell, SWT.RIGHT);
        wlStepname.setText(BaseMessages.getString(PKG, "BlockingStepDialog.Stepname.Label")); //$NON-NLS-1$
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
        
        // Update the dimension?
        wlPassAllRows=new Label(shell, SWT.RIGHT);
        wlPassAllRows.setText(BaseMessages.getString(PKG, "BlockingStepDialog.PassAllRows.Label")); //$NON-NLS-1$
        props.setLook(wlPassAllRows);
        FormData fdlUpdate=new FormData();
        fdlUpdate.left   = new FormAttachment(0, 0);
        fdlUpdate.right  = new FormAttachment(middle, -margin);
        fdlUpdate.top    = new FormAttachment(wStepname, margin);
        wlPassAllRows.setLayoutData(fdlUpdate);
        wPassAllRows=new Button(shell, SWT.CHECK);
        props.setLook(wPassAllRows);
        FormData fdUpdate=new FormData();
        fdUpdate.left = new FormAttachment(middle, 0);
        fdUpdate.top  = new FormAttachment(wStepname, margin);
        fdUpdate.right= new FormAttachment(100, 0);
        wPassAllRows.setLayoutData(fdUpdate);
        
        // Clicking on update changes the options in the update combo boxes!
        wPassAllRows.addSelectionListener(new SelectionAdapter()
            {
                public void widgetSelected(SelectionEvent e)
                {
                    input.setChanged();
                    setEnableDialog();                
                }
            }
        );                 
        
		// Temp directory for sorting
		wlSpoolDir=new Label(shell, SWT.RIGHT);
		wlSpoolDir.setText(BaseMessages.getString(PKG, "BlockingStepDialog.SpoolDir.Label"));
 		props.setLook(wlSpoolDir);
		fdlSpoolDir=new FormData();
		fdlSpoolDir.left = new FormAttachment(0, 0);
		fdlSpoolDir.right= new FormAttachment(middle, -margin);
		fdlSpoolDir.top  = new FormAttachment(wPassAllRows, margin);
		wlSpoolDir.setLayoutData(fdlSpoolDir);

		wbSpoolDir=new Button(shell, SWT.PUSH| SWT.CENTER);
 		props.setLook(wbSpoolDir);
		wbSpoolDir.setText(BaseMessages.getString(PKG, "System.Button.Browse"));
		fdbSpoolDir=new FormData();
		fdbSpoolDir.right= new FormAttachment(100, 0);
		fdbSpoolDir.top  = new FormAttachment(wPassAllRows, margin);
		wbSpoolDir.setLayoutData(fdbSpoolDir);

		wSpoolDir=new TextVar(transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wSpoolDir);
		wSpoolDir.addModifyListener(lsMod);
		fdSpoolDir=new FormData();
		fdSpoolDir.left = new FormAttachment(middle, 0);
		fdSpoolDir.top  = new FormAttachment(wPassAllRows, margin);
		fdSpoolDir.right= new FormAttachment(wbSpoolDir, -margin);
		wSpoolDir.setLayoutData(fdSpoolDir);
		
		wbSpoolDir.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent arg0)
			{
				DirectoryDialog dd = new DirectoryDialog(shell, SWT.NONE);
				dd.setFilterPath(wSpoolDir.getText());
				String dir = dd.open();
				if (dir!=null)
				{
					wSpoolDir.setText(dir);
				}
			}
		});

		// Whenever something changes, set the tooltip to the expanded version:
		wSpoolDir.addModifyListener(new ModifyListener()
			{
				public void modifyText(ModifyEvent e)
				{
					wSpoolDir.setToolTipText(transMeta.environmentSubstitute( wSpoolDir.getText() ) );
				}
			}
		);

        // Prefix of temporary file
		wlPrefix=new Label(shell, SWT.RIGHT);
		wlPrefix.setText(BaseMessages.getString(PKG, "BlockingStepDialog.Prefix.Label"));
 		props.setLook(wlPrefix);
		fdlPrefix=new FormData();
		fdlPrefix.left = new FormAttachment(0, 0);
		fdlPrefix.right= new FormAttachment(middle, -margin);
		fdlPrefix.top  = new FormAttachment(wbSpoolDir, margin*2);
		wlPrefix.setLayoutData(fdlPrefix);
		wPrefix=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
 		props.setLook(wPrefix);
		wPrefix.addModifyListener(lsMod);
		fdPrefix=new FormData();
		fdPrefix.left  = new FormAttachment(middle, 0);
		fdPrefix.top   = new FormAttachment(wbSpoolDir, margin*2);
		fdPrefix.right = new FormAttachment(100, 0);
		wPrefix.setLayoutData(fdPrefix);

        // Maximum number of lines to keep in memory before using temporary files
        wlCacheSize=new Label(shell, SWT.RIGHT);
        wlCacheSize.setText(BaseMessages.getString(PKG, "BlockingStepDialog.CacheSize.Label"));
        props.setLook(wlCacheSize);
        fdlCacheSize=new FormData();
        fdlCacheSize.left = new FormAttachment(0, 0);
        fdlCacheSize.right= new FormAttachment(middle, -margin);
        fdlCacheSize.top  = new FormAttachment(wPrefix, margin*2);
        wlCacheSize.setLayoutData(fdlCacheSize);
        wCacheSize=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
        props.setLook(wCacheSize);
        wCacheSize.addModifyListener(lsMod);
        fdCacheSize=new FormData();
        fdCacheSize.left  = new FormAttachment(middle, 0);
        fdCacheSize.top   = new FormAttachment(wPrefix, margin*2);
        fdCacheSize.right = new FormAttachment(100, 0);
        wCacheSize.setLayoutData(fdCacheSize);

        // Using compression for temporary files?
        wlCompress=new Label(shell, SWT.RIGHT);
        wlCompress.setText(BaseMessages.getString(PKG, "BlockingStepDialog.Compress.Label"));
        props.setLook(wlCompress);
        fdlCompress=new FormData();
        fdlCompress.left = new FormAttachment(0, 0);
        fdlCompress.right= new FormAttachment(middle, -margin);
        fdlCompress.top  = new FormAttachment(wCacheSize, margin*2);
        wlCompress.setLayoutData(fdlCompress);
        wCompress=new Button(shell, SWT.CHECK);
        props.setLook(wCompress);
        fdCompress=new FormData();
        fdCompress.left  = new FormAttachment(middle, 0);
        fdCompress.top   = new FormAttachment(wCacheSize, margin*2);
        fdCompress.right = new FormAttachment(100, 0);
        wCompress.setLayoutData(fdCompress);
        wCompress.addSelectionListener(new SelectionAdapter() 
			{
				public void widgetSelected(SelectionEvent e) 
				{
					input.setChanged();
				}
			}
        );        

        // Some buttons
        wOK=new Button(shell, SWT.PUSH);
        wOK.setText(BaseMessages.getString(getClass().getPackage().getName(), "System.Button.OK")); //$NON-NLS-1$
        wCancel=new Button(shell, SWT.PUSH);
        wCancel.setText(BaseMessages.getString(getClass().getPackage().getName(), "System.Button.Cancel")); //$NON-NLS-1$
        
        setButtonPositions(new Button[] { wOK, wCancel }, margin, wCompress);        

        // Add listeners
        lsCancel   = new Listener() { public void handleEvent(Event e) { cancel(); } };
        lsOK       = new Listener() { public void handleEvent(Event e) { ok();     } };
        
        wCancel.addListener(SWT.Selection, lsCancel);
        wOK.addListener    (SWT.Selection, lsOK    );
        
        lsDef=new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } };
        
        wStepname.addSelectionListener( lsDef );
                
        // Detect X or ALT-F4 or something that kills this window...
        shell.addShellListener( new ShellAdapter() { public void shellClosed(ShellEvent e) { cancel(); } } );

        // Set the shell size, based upon previous time...
        setSize();
        
        getData();
        input.setChanged(changed);
        
        // Set the enablement of the dialog widgets
        setEnableDialog();
    
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
    	wPassAllRows.setSelection(input.isPassAllRows());
		if (input.getPrefix() != null) wPrefix.setText(input.getPrefix());
		if (input.getDirectory() != null) wSpoolDir.setText(input.getDirectory());
		wCacheSize.setText(""+input.getCacheSize());
		wCompress.setSelection(input.getCompress());
		
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
		if (Const.isEmpty(wStepname.getText())) return;

        stepname = wStepname.getText(); // return value
        
		input.setPrefix( wPrefix.getText() );
		input.setDirectory( wSpoolDir.getText() );
        input.setCacheSize( Const.toInt( wCacheSize.getText(), BlockingStepMeta.CACHE_SIZE ) );
        logDetailed("Compression is set to " + wCompress.getSelection());
        input.setCompress(wCompress.getSelection());
        input.setPassAllRows(wPassAllRows.getSelection());

        dispose();
    }
    
    /**
     * Set the correct state "enabled or not" of the dialog widgets.
     */
    private void setEnableDialog()
    {
    	wlSpoolDir.setEnabled(wPassAllRows.getSelection());
    	wbSpoolDir.setEnabled(wPassAllRows.getSelection());
    	wSpoolDir.setEnabled(wPassAllRows.getSelection());
  	    wlPrefix.setEnabled(wPassAllRows.getSelection());
    	wPrefix.setEnabled(wPassAllRows.getSelection());
        wlCacheSize.setEnabled(wPassAllRows.getSelection());
        wCacheSize.setEnabled(wPassAllRows.getSelection());
        wlCompress.setEnabled(wPassAllRows.getSelection());
        wCompress.setEnabled(wPassAllRows.getSelection());
    }
}