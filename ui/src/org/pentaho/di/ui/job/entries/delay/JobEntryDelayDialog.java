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

package org.pentaho.di.ui.job.entries.delay;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
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
import org.pentaho.di.job.entries.delay.JobEntryDelay;
import org.pentaho.di.job.entry.JobEntryDialogInterface;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.ui.core.gui.WindowProperty;
import org.pentaho.di.ui.core.widget.LabelTextVar;
import org.pentaho.di.ui.job.dialog.JobDialog;
import org.pentaho.di.ui.job.entry.JobEntryDialog;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

/**
 * This dialog allows you to edit the delay job entry settings.
 *
 * @author Samatar Hassan
 * @since  21-02-2007
 */
public class JobEntryDelayDialog extends JobEntryDialog implements JobEntryDialogInterface
{
	private static Class<?> PKG = JobEntryDelay.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private Label        wlName;
	private Text         wName;
	private FormData     fdlName, fdName;

	private CCombo   wScaleTime;
	private FormData fdScaleTime;

	private LabelTextVar wMaximumTimeout;
	private FormData     fdMaximumTimeout;

	private Button   wOK, wCancel;
	private Listener lsOK, lsCancel;

	private JobEntryDelay jobEntry;
	private Shell      	  shell;

	private SelectionAdapter lsDef;
	
	private boolean changed;

    public JobEntryDelayDialog(Shell parent, JobEntryInterface jobEntryInt, Repository rep, JobMeta jobMeta)
    {
        super(parent, jobEntryInt, rep, jobMeta);
        jobEntry = (JobEntryDelay) jobEntryInt;
		if (this.jobEntry.getName() == null)
			this.jobEntry.setName(BaseMessages.getString(PKG, "JobEntryDelay.Title"));
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
		shell.setText(BaseMessages.getString(PKG, "JobEntryDelay.Title"));

		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		// Name line
		wlName=new Label(shell, SWT.RIGHT);
		wlName.setText(BaseMessages.getString(PKG, "JobEntryDelay.Name.Label"));
		props.setLook(wlName);
		fdlName=new FormData();
		fdlName.left = new FormAttachment(0, -margin);
		fdlName.right= new FormAttachment(middle, 0);
		fdlName.top  = new FormAttachment(0, margin);
		wlName.setLayoutData(fdlName);
		wName=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(wName);
		wName.addModifyListener(lsMod);
		fdName=new FormData();
		fdName.left = new FormAttachment(middle, margin);
		fdName.top  = new FormAttachment(0, margin);
		fdName.right= new FormAttachment(100, 0);
		wName.setLayoutData(fdName);

		// MaximumTimeout line
		wMaximumTimeout = new LabelTextVar(jobMeta, shell, BaseMessages.getString(PKG, "JobEntryDelay.MaximumTimeout.Label"), BaseMessages.getString(PKG, "JobEntryDelay.MaximumTimeout.Tooltip"));
		props.setLook(wMaximumTimeout);
		wMaximumTimeout.addModifyListener(lsMod);
		fdMaximumTimeout = new FormData();
		fdMaximumTimeout.left = new FormAttachment(0, -margin);
		fdMaximumTimeout.top = new FormAttachment(wName, margin);
		fdMaximumTimeout.right = new FormAttachment(100, 0);
		wMaximumTimeout.setLayoutData(fdMaximumTimeout);

		// Whenever something changes, set the tooltip to the expanded version:
		wMaximumTimeout.addModifyListener(new ModifyListener()
		    {
			    public void modifyText(ModifyEvent e)
			    {
				    wMaximumTimeout.setToolTipText(jobMeta.environmentSubstitute( wMaximumTimeout.getText() ) );
			    }
		    }
		);

		// Scale time
	
		wScaleTime = new CCombo(shell, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER);
		wScaleTime.add(BaseMessages.getString(PKG, "JobEntryDelay.SScaleTime.Label"));
		wScaleTime.add(BaseMessages.getString(PKG, "JobEntryDelay.MnScaleTime.Label"));
		wScaleTime.add(BaseMessages.getString(PKG, "JobEntryDelay.HrScaleTime.Label"));
		wScaleTime.select(0); // +1: starts at -1

		props.setLook(wScaleTime);
		fdScaleTime= new FormData();
		fdScaleTime.left = new FormAttachment(middle, 0);
		fdScaleTime.top = new FormAttachment(wMaximumTimeout, margin);
		fdScaleTime.right = new FormAttachment(100, 0);
		wScaleTime.setLayoutData(fdScaleTime);

		wOK = new Button(shell, SWT.PUSH);
		wOK.setText(BaseMessages.getString(PKG, "System.Button.OK"));
		wCancel = new Button(shell, SWT.PUSH);
		wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel"));

		BaseStepDialog.positionBottomButtons(shell, new Button[] { wOK, wCancel }, margin, wScaleTime);

		// Add listeners
		lsCancel   = new Listener() { public void handleEvent(Event e) { cancel(); } };
		lsOK       = new Listener() { public void handleEvent(Event e) { ok();     } };

		wCancel.addListener(SWT.Selection, lsCancel);
		wOK.addListener    (SWT.Selection, lsOK    );

		lsDef=new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } };

		wName.addSelectionListener( lsDef );
		wMaximumTimeout.addSelectionListener( lsDef );

		// Detect X or ALT-F4 or something that kills this window...
		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { cancel(); } } );

		getData();

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

	/**
	 * Copy information from the meta-data input to the dialog fields.
	 */
	public void getData()
	{
		if (jobEntry.getName()    != null) wName.setText( jobEntry.getName() );
		wName.selectAll();
		if (jobEntry.getMaximumTimeout()!= null) wMaximumTimeout.setText( jobEntry.getMaximumTimeout() );

		wScaleTime.select(jobEntry.scaleTime );
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
		jobEntry.setMaximumTimeout(wMaximumTimeout.getText());
		jobEntry.scaleTime = wScaleTime.getSelectionIndex();
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