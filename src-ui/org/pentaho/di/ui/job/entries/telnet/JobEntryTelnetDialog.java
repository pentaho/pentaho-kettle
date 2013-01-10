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

package org.pentaho.di.ui.job.entries.telnet;

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
import org.eclipse.swt.widgets.MessageBox; 

import org.pentaho.di.core.Const;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entries.telnet.JobEntryTelnet;
import org.pentaho.di.job.entry.JobEntryDialogInterface;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.ui.core.gui.WindowProperty;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.job.dialog.JobDialog;
import org.pentaho.di.ui.job.entry.JobEntryDialog;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

/**
 * This dialog allows you to edit the Telnet job entry settings. 
 * 
 * @author Samatar
 * @since 19-06-2006
 */
public class JobEntryTelnetDialog extends JobEntryDialog implements JobEntryDialogInterface
{
	private static Class<?> PKG = JobEntryTelnet.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

    private Label wlName;

    private Text wName;

    private FormData fdlName, fdName;

    private Label wlHostname;

    private TextVar wHostname;

    private FormData fdlHostname,  fdHostname;

	private Label        wlTimeOut;
	private TextVar      wTimeOut;
	private FormData     fdlTimeOut, fdTimeOut;
	
	private Label    wlPort;
	private TextVar  wPort;
	private FormData fdPort,fdlPort;

    private Button wOK, wCancel;

    private Listener lsOK, lsCancel;

    private JobEntryTelnet jobEntry;

    private Shell shell;

    private SelectionAdapter lsDef;
    
    private boolean changed;


    public JobEntryTelnetDialog(Shell parent, JobEntryInterface jobEntryInt, Repository rep, JobMeta jobMeta)
    {
        super(parent, jobEntryInt, rep, jobMeta);
        jobEntry = (JobEntryTelnet) jobEntryInt;
        if (this.jobEntry.getName() == null)
            this.jobEntry.setName(BaseMessages.getString(PKG, "JobTelnet.Name.Default"));
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
        shell.setText(BaseMessages.getString(PKG, "JobTelnet.Title"));

        int middle = props.getMiddlePct();
        int margin = Const.MARGIN;

        // Filename line
        wlName = new Label(shell, SWT.RIGHT);
        wlName.setText(BaseMessages.getString(PKG, "JobTelnet.Name.Label"));
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
        wlHostname.setText(BaseMessages.getString(PKG, "JobTelnet.Hostname.Label"));
        props.setLook(wlHostname);
        fdlHostname = new FormData();
        fdlHostname.left = new FormAttachment(0, -margin);
        fdlHostname.top = new FormAttachment(wName, margin);
        fdlHostname.right = new FormAttachment(middle, -margin);
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
    	
		wlPort = new Label(shell, SWT.RIGHT);
		wlPort.setText(BaseMessages.getString(PKG, "JobTelnet.Port.Label"));
		props.setLook(wlPort);
		fdlPort = new FormData();
		fdlPort.left = new FormAttachment(0, -margin);
		fdlPort.right = new FormAttachment(middle, -margin);
		fdlPort.top = new FormAttachment(wHostname, margin);
		wlPort.setLayoutData(fdlPort);

		wPort = new TextVar(jobMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(wPort);
		wPort.addModifyListener(lsMod);
		fdPort = new FormData();
		fdPort.left = new FormAttachment(middle, 0);
		fdPort.top = new FormAttachment(wHostname, margin);
		fdPort.right = new FormAttachment(100, 0);
		wPort.setLayoutData(fdPort);



		wlTimeOut = new Label(shell, SWT.RIGHT);
		wlTimeOut.setText(BaseMessages.getString(PKG, "JobTelnet.TimeOut.Label"));
		props.setLook(wlTimeOut);
		fdlTimeOut = new FormData();
		fdlTimeOut.left = new FormAttachment(0, -margin);
		fdlTimeOut.right = new FormAttachment(middle, -margin);
		fdlTimeOut.top = new FormAttachment(wPort, margin);
		wlTimeOut.setLayoutData(fdlTimeOut);

		wTimeOut = new TextVar(jobMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(wTimeOut);
		wTimeOut.addModifyListener(lsMod);
		fdTimeOut = new FormData();
		fdTimeOut.left = new FormAttachment(middle, 0);
		fdTimeOut.top = new FormAttachment(wPort, margin);
		fdTimeOut.right = new FormAttachment(100, 0);
		wTimeOut.setLayoutData(fdTimeOut);
		
	
        wOK = new Button(shell, SWT.PUSH);
        wOK.setText(BaseMessages.getString(PKG, "System.Button.OK"));
        wCancel = new Button(shell, SWT.PUSH);
        wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel"));
        
        BaseStepDialog.positionBottomButtons(shell, new Button[] { wOK, wCancel }, margin, wTimeOut);
        

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
        props.setDialogSize(shell, "JobTelnetDialogSize");
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
        if (jobEntry.getName() != null)  wName.setText(jobEntry.getName());
        wName.selectAll();
        if (jobEntry.getHostname() != null) wHostname.setText(jobEntry.getHostname());

		wPort.setText(Const.NVL(jobEntry.getPort(), String.valueOf(JobEntryTelnet.DEFAULT_PORT)));
		wTimeOut.setText(Const.NVL(jobEntry.getTimeOut(), String.valueOf(JobEntryTelnet.DEFAULT_TIME_OUT)));

    }

    private void cancel()
    {
        jobEntry.setChanged(changed);
        jobEntry = null;
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
        jobEntry.setHostname(wHostname.getText());
		jobEntry.setPort(wPort.getText());
		jobEntry.setTimeOut(wTimeOut.getText());
		
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
