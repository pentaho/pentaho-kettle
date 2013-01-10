/*******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.ui.job.entries.deleteresultfilenames;

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
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entries.deleteresultfilenames.JobEntryDeleteResultFilenames;
import org.pentaho.di.job.entry.JobEntryDialogInterface;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.ui.core.gui.WindowProperty;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.job.dialog.JobDialog;
import org.pentaho.di.ui.job.entry.JobEntryDialog;
import org.pentaho.di.ui.trans.step.BaseStepDialog;


/**
 * This dialog allows you to edit the Create Folder job entry settings.
 *
 * @author Samatar
 * @since  27-10-2007
 */
public class JobEntryDeleteResultFilenamesDialog extends JobEntryDialog implements JobEntryDialogInterface
{
	private static Class<?> PKG = JobEntryDeleteResultFilenames.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private Label        wlName;
	private Text         wName;
    private FormData     fdlName, fdName;

	
    private Label        wlSpecifyWildcard;
    private Button       wSpecifyWildcard;
    private FormData     fdlSpecifyWildcard, fdSpecifyWildcard;
    
	private Label        wlWildcard;
	private TextVar      wWildcard;
	private FormData     fdlWildcard, fdWildcard;    
	
	private Label        wlWildcardExclude;
	private TextVar      wWildcardExclude;
	private FormData     fdlWildcardExclude, fdWildcardExclude; 

	private Button wOK, wCancel;
	private Listener lsOK, lsCancel;

	private JobEntryDeleteResultFilenames jobEntry;
	private Shell       	shell;

	private SelectionAdapter lsDef;

	private boolean changed;

	 public JobEntryDeleteResultFilenamesDialog(Shell parent, JobEntryInterface jobEntryInt, Repository rep, JobMeta jobMeta)
	 {	
        super(parent, jobEntryInt, rep, jobMeta);
        jobEntry = (JobEntryDeleteResultFilenames) jobEntryInt;

		if (this.jobEntry.getName() == null) 
			this.jobEntry.setName(BaseMessages.getString(PKG, "JobEntryDeleteResultFilenames.Name.Default"));
	}

	public JobEntryInterface open()
	{
		Shell parent = getParent();
		Display display = parent.getDisplay();

        shell = new Shell(parent, props.getJobsDialogStyle());
        props.setLook(shell);
        JobDialog.setShellImage(shell, jobEntry);

		ModifyListener lsMod = new ModifyListener()
		{
			public void modifyText(ModifyEvent e)
			{
				jobEntry.setChanged();
			}
		};
		changed = jobEntry.hasChanged();

		FormLayout formLayout = new FormLayout ();
		formLayout.marginWidth  = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText(BaseMessages.getString(PKG, "JobEntryDeleteResultFilenames.Title"));

		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		// Foldername line
		wlName=new Label(shell, SWT.RIGHT);
		wlName.setText(BaseMessages.getString(PKG, "JobEntryDeleteResultFilenames.Name.Label"));
 		props.setLook(wlName);
		fdlName=new FormData();
		fdlName.left = new FormAttachment(0, 0);
		fdlName.right= new FormAttachment(middle, -margin);
		fdlName.top  = new FormAttachment(0, margin);
		wlName.setLayoutData(fdlName);
		wName=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wName);
		wName.addModifyListener(lsMod);
		fdName=new FormData();
		fdName.left = new FormAttachment(middle, 0);
		fdName.top  = new FormAttachment(0, margin);
		fdName.right= new FormAttachment(100, 0);
		wName.setLayoutData(fdName);	
	
        // Specify wildcard?
        wlSpecifyWildcard = new Label(shell, SWT.RIGHT);
        wlSpecifyWildcard.setText(BaseMessages.getString(PKG, "JobEntryDeleteResultFilenames.SpecifyWildcard.Label"));
        props.setLook(wlSpecifyWildcard);
        fdlSpecifyWildcard = new FormData();
        fdlSpecifyWildcard.left = new FormAttachment(0, 0);
        fdlSpecifyWildcard.top = new FormAttachment(wName, margin);
        fdlSpecifyWildcard.right = new FormAttachment(middle, -margin);
        wlSpecifyWildcard.setLayoutData(fdlSpecifyWildcard);
        wSpecifyWildcard = new Button(shell, SWT.CHECK);
        props.setLook(wSpecifyWildcard);
        wSpecifyWildcard.setToolTipText(BaseMessages.getString(PKG, "JobEntryDeleteResultFilenames.SpecifyWildcard.Tooltip"));
        fdSpecifyWildcard = new FormData();
        fdSpecifyWildcard.left = new FormAttachment(middle, 0);
        fdSpecifyWildcard.top = new FormAttachment(wName, margin);
        fdSpecifyWildcard.right = new FormAttachment(100, 0);
        wSpecifyWildcard.setLayoutData(fdSpecifyWildcard);
        wSpecifyWildcard.addSelectionListener(new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent e)
            {
                jobEntry.setChanged();
                CheckLimit();
            }
        });
        

        
		// Wildcard line
		wlWildcard=new Label(shell, SWT.RIGHT);
		wlWildcard.setText(BaseMessages.getString(PKG, "JobEntryDeleteResultFilenames.Wildcard.Label"));
 		props.setLook(wlWildcard);
		fdlWildcard=new FormData();
		fdlWildcard.left = new FormAttachment(0, 0);
		fdlWildcard.top  = new FormAttachment(wSpecifyWildcard, margin);
		fdlWildcard.right= new FormAttachment(middle, -margin);
		wlWildcard.setLayoutData(fdlWildcard);
		wWildcard=new TextVar(jobMeta,shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wWildcard.setToolTipText(BaseMessages.getString(PKG, "JobEntryDeleteResultFilenames.Wildcard.Tooltip"));
 		props.setLook(wWildcard);
		wWildcard.addModifyListener(lsMod);
		fdWildcard=new FormData();
		fdWildcard.left = new FormAttachment(middle, 0);
		fdWildcard.top  = new FormAttachment(wSpecifyWildcard, margin);
		fdWildcard.right= new FormAttachment(100, -margin);
		wWildcard.setLayoutData(fdWildcard);
		
		
		// Whenever something changes, set the tooltip to the expanded version:
		wWildcard.addModifyListener(new ModifyListener()
			{
				public void modifyText(ModifyEvent e)
				{
					wWildcard.setToolTipText(jobMeta.environmentSubstitute( wWildcard.getText() ) );
				}
			}
		);

		// wWildcardExclude
		wlWildcardExclude=new Label(shell, SWT.RIGHT);
		wlWildcardExclude.setText(BaseMessages.getString(PKG, "JobEntryDeleteResultFilenames.WildcardExclude.Label"));
 		props.setLook(wlWildcardExclude);
		fdlWildcardExclude=new FormData();
		fdlWildcardExclude.left = new FormAttachment(0, 0);
		fdlWildcardExclude.top  = new FormAttachment(wWildcard, margin);
		fdlWildcardExclude.right= new FormAttachment(middle, -margin);
		wlWildcardExclude.setLayoutData(fdlWildcardExclude);
		wWildcardExclude=new TextVar(jobMeta,shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wWildcardExclude.setToolTipText(BaseMessages.getString(PKG, "JobEntryDeleteResultFilenames.WildcardExclude.Tooltip"));
 		props.setLook(wWildcardExclude);
		wWildcardExclude.addModifyListener(lsMod);
		fdWildcardExclude=new FormData();
		fdWildcardExclude.left = new FormAttachment(middle, 0);
		fdWildcardExclude.top  = new FormAttachment(wWildcard, margin);
		fdWildcardExclude.right= new FormAttachment(100, -margin);
		wWildcardExclude.setLayoutData(fdWildcardExclude);
		
		
		// Whenever something changes, set the tooltip to the expanded version:
		wWildcardExclude.addModifyListener(new ModifyListener()
			{
				public void modifyText(ModifyEvent e)
				{
					wWildcardExclude.setToolTipText(jobMeta.environmentSubstitute( wWildcardExclude.getText() ) );
				}
			}
		);

        wOK = new Button(shell, SWT.PUSH);
        wOK.setText(BaseMessages.getString(PKG, "System.Button.OK"));
        wCancel = new Button(shell, SWT.PUSH);
        wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel"));
        
		BaseStepDialog.positionBottomButtons(shell, new Button[] { wOK, wCancel }, margin, wWildcardExclude);

		// Add listeners
		lsCancel   = new Listener() { public void handleEvent(Event e) { cancel(); } };
		lsOK       = new Listener() { public void handleEvent(Event e) { ok();     } };

		wCancel.addListener(SWT.Selection, lsCancel);
		wOK.addListener    (SWT.Selection, lsOK    );

		lsDef=new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } };

		wName.addSelectionListener( lsDef );
		// Detect X or ALT-F4 or something that kills this window...
		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { cancel(); } } );

		getData();
		CheckLimit();

		BaseStepDialog.setSize(shell);

		shell.open();
		while (!shell.isDisposed())
		{
				if (!display.readAndDispatch()) display.sleep();
		}
		return jobEntry;
	}

	public void dispose()
	{
		WindowProperty winprop = new WindowProperty(shell);
		props.setScreen(winprop);
		shell.dispose();
	}
	private void CheckLimit()
	{
		wlWildcard.setEnabled(wSpecifyWildcard.getSelection());
		wWildcard.setEnabled(wSpecifyWildcard.getSelection());
		wlWildcardExclude.setEnabled(wSpecifyWildcard.getSelection());
		wWildcardExclude.setEnabled(wSpecifyWildcard.getSelection());
	}

	/**
	 * Copy information from the meta-data input to the dialog fields.
	 */
	public void getData()
	{
		if (jobEntry.getName()!= null) wName.setText( jobEntry.getName() );
		wName.selectAll();
		wSpecifyWildcard.setSelection(jobEntry.isSpecifyWildcard());
		if (jobEntry.getWildcard()!= null) wWildcard.setText( jobEntry.getWildcard() );
		if (jobEntry.getWildcardExclude()!= null) wWildcardExclude.setText( jobEntry.getWildcardExclude() );
		
		
	}

	private void cancel()
	{
		jobEntry.setChanged(changed);
		jobEntry=null;
		dispose();
	}

	private void ok()
	{
	   if(Const.isEmpty(wName.getText())) 
        {
			MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
			mb.setText(BaseMessages.getString(PKG, "System.StepJobEntryNameMissing.Title"));
			mb.setMessage(BaseMessages.getString(PKG, "System.JobEntryNameMissing.Msg"));
			mb.open(); 
			return;
        }
		jobEntry.setName(wName.getText());
		jobEntry.setSpecifyWildcard(wSpecifyWildcard.getSelection());
		jobEntry.setWildcard(wWildcard.getText());
		jobEntry.setWildcardExclude(wWildcardExclude.getText());
		
		dispose();
	}

	public boolean evaluates()
	{
		return true;
	}

	public boolean isUnconditional()
	{
		return false;
	}
}