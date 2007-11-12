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

/*
 * Created on 19-jun-2003
 *
 */

package org.pentaho.di.ui.job.entries.ping;

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
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.ui.core.gui.WindowProperty;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.job.dialog.JobDialog;
import org.pentaho.di.ui.job.entry.JobEntryDialog; 
import org.pentaho.di.job.entry.JobEntryDialogInterface;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.job.entries.ping.JobEntryPing;
import org.pentaho.di.job.entries.ping.Messages;

/**
 * This dialog allows you to edit the ping job entry settings. 
 * 
 * @author Samatar Hassan
 * @since  Mar-2007
 */
public class JobEntryPingDialog extends JobEntryDialog implements JobEntryDialogInterface
{
    private Label    wlName;
    private Text     wName;
    private FormData fdlName, fdName;

    private Label    wlHostname;
    private TextVar  wHostname;
    private FormData fdlHostname,  fdHostname;

	private Label    wlNbrPackets;
	private TextVar  wNbrPackets;
	private FormData fdlNbrPackets, fdNbrPackets;

    private Button   wOK, wCancel;

    private Listener lsOK, lsCancel;

    private JobEntryPing jobEntry;

    private Shell shell;

    private SelectionAdapter lsDef;
    
    private boolean changed;


    public JobEntryPingDialog(Shell parent, JobEntryInterface jobEntryInt, Repository rep, JobMeta jobMeta)
    {
        super(parent, jobEntryInt, rep, jobMeta);
        jobEntry = (JobEntryPing) jobEntryInt;
        if (this.jobEntry.getName() == null)
            this.jobEntry.setName(Messages.getString("JobPing.Name.Default"));
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

        FormLayout formLayout = new FormLayout();
        formLayout.marginWidth = Const.FORM_MARGIN;
        formLayout.marginHeight = Const.FORM_MARGIN;

        shell.setLayout(formLayout);
        shell.setText(Messages.getString("JobPing.Title"));

        int middle = props.getMiddlePct();
        int margin = Const.MARGIN;

        // Filename line
        wlName = new Label(shell, SWT.RIGHT);
        wlName.setText(Messages.getString("JobPing.Name.Label"));
        props.setLook(wlName);
        fdlName = new FormData();
        fdlName.left = new FormAttachment(0, 0);
        fdlName.right = new FormAttachment(middle, -margin);
        fdlName.top = new FormAttachment(0, margin);
        wlName.setLayoutData(fdlName);
        wName = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(wName);
        wName.addModifyListener(lsMod);
        fdName = new FormData();
        fdName.left = new FormAttachment(middle, 0);
        fdName.top = new FormAttachment(0, margin);
        fdName.right = new FormAttachment(100, 0);
        wName.setLayoutData(fdName);

        // hostname line
        wlHostname = new Label(shell, SWT.RIGHT);
        wlHostname.setText(Messages.getString("JobPing.Hostname.Label"));
        props.setLook(wlHostname);
        fdlHostname = new FormData();
        fdlHostname.left = new FormAttachment(0, 0);
        fdlHostname.top = new FormAttachment(wName, margin);
        fdlHostname.right = new FormAttachment(middle, 0);
        wlHostname.setLayoutData(fdlHostname);

        wHostname = new TextVar(jobMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(wHostname);
        wHostname.addModifyListener(lsMod);
        fdHostname = new FormData();
        fdHostname.left = new FormAttachment(middle, 0);
        fdHostname.top = new FormAttachment(wName, margin);
        fdHostname.right = new FormAttachment(100, 0);
        wHostname.setLayoutData(fdHostname);

        // Whenever something changes, set the tooltip to the expanded version:
        wHostname.addModifyListener(new ModifyListener()
        {
            public void modifyText(ModifyEvent e)
            {
                wHostname.setToolTipText(jobMeta.environmentSubstitute(wHostname.getText()));
            }
        });

		// Nbr response to get
		wlNbrPackets = new Label(shell, SWT.RIGHT);
		wlNbrPackets.setText(Messages.getString("JobPing.NbrPaquets.Label"));
		props.setLook(wlNbrPackets);
		fdlNbrPackets = new FormData();
		fdlNbrPackets.left = new FormAttachment(0, 0);
		fdlNbrPackets.right = new FormAttachment(middle, 0);
		fdlNbrPackets.top = new FormAttachment(wHostname, margin);
		wlNbrPackets.setLayoutData(fdlNbrPackets);

		wNbrPackets = new TextVar(jobMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(wNbrPackets);
		wNbrPackets.addModifyListener(lsMod);
		fdNbrPackets = new FormData();
		fdNbrPackets.left = new FormAttachment(middle, 0);
		fdNbrPackets.top = new FormAttachment(wHostname, margin);
		fdNbrPackets.right = new FormAttachment(100, 0);
		wNbrPackets.setLayoutData(fdNbrPackets);

        wOK = new Button(shell, SWT.PUSH);
        wOK.setText(Messages.getString("System.Button.OK"));
        FormData fd = new FormData();
        fd.right = new FormAttachment(50, -10);
        fd.bottom = new FormAttachment(100, 0);
        fd.width = 100;
        wOK.setLayoutData(fd);

        wCancel = new Button(shell, SWT.PUSH);
        wCancel.setText(Messages.getString("System.Button.Cancel"));
        fd = new FormData();
        fd.left = new FormAttachment(50, 10);
        fd.bottom = new FormAttachment(100, 0);
        fd.width = 100;
        wCancel.setLayoutData(fd);

        // Add listeners
        lsCancel = new Listener()
        {
            public void handleEvent(Event e)
            {
                cancel();
            }
        };
        lsOK = new Listener()
        {
            public void handleEvent(Event e)
            {
                ok();
            }
        };

        wCancel.addListener(SWT.Selection, lsCancel);
        wOK.addListener(SWT.Selection, lsOK);

        lsDef = new SelectionAdapter()
        {
            public void widgetDefaultSelected(SelectionEvent e)
            {
                ok();
            }
        };

        wName.addSelectionListener(lsDef);
        wHostname.addSelectionListener(lsDef);

        // Detect X or ALT-F4 or something that kills this window...
        shell.addShellListener(new ShellAdapter()
        {
            public void shellClosed(ShellEvent e)
            {
                cancel();
            }
        });

        getData();

        BaseStepDialog.setSize(shell);

        shell.open();
        props.setDialogSize(shell, "JobPingDialogSize");
        while (!shell.isDisposed())
        {
            if (!display.readAndDispatch())
                display.sleep();
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
        if (jobEntry.getName() != null)
            wName.setText(jobEntry.getName());
        wName.selectAll();
        if (jobEntry.getHostname() != null)
            wHostname.setText(jobEntry.getHostname());
		if (jobEntry.getNbrPackets() != null)
			wNbrPackets.setText(jobEntry.getNbrPackets());
    }

    private void cancel()
    {
        jobEntry.setChanged(changed);
        jobEntry = null;
        dispose();
    }

    private void ok()
    {
        jobEntry.setName(wName.getText());
        jobEntry.setHostname(wHostname.getText());
		jobEntry.setNbrPackets(wNbrPackets.getText());
	
        dispose();
    }

    public String toString()
    {
        return this.getClass().getName();
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