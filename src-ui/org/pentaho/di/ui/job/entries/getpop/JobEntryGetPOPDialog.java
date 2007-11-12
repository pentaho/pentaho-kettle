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

package org.pentaho.di.ui.job.entries.getpop;

import java.util.ArrayList;

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
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.StringUtil;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.ui.core.gui.WindowProperty;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.job.dialog.JobDialog;
import org.pentaho.di.ui.job.entry.JobEntryDialog; 
import org.pentaho.di.job.entry.JobEntryDialogInterface;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.job.entries.getpop.JobEntryGetPOP;
import org.pentaho.di.job.entries.getpop.Messages;


/**
 * This dialog allows you to edit the SQL job entry settings. (select the connection and the sql
 * script to be executed)
 * 
 * @author Matt
 * @since 19-06-2003
 */
public class JobEntryGetPOPDialog extends JobEntryDialog implements JobEntryDialogInterface
{

    private Label wlName;

    private Text wName;

    private FormData fdlName, fdName;

	private Label        wlServerName;
	private TextVar      wServerName;
	private FormData     fdlServerName, fdServerName;

	
	private Label        wlUserName;
	private TextVar      wUserName;
	private FormData     fdlUserName, fdUserName;

	private Label        wlPassword;
	private TextVar      wPassword;
	private FormData     fdlPassword, fdPassword;


	private Label        wlOutputDirectory;
	private TextVar      wOutputDirectory;
	private FormData     fdlOutputDirectory, fdOutputDirectory;

	private Label        wlFilenamePattern;
	private TextVar      wFilenamePattern;
	private FormData     fdlFilenamePattern, fdFilenamePattern;

	
	private Label wlListmails;
	private  CCombo wListmails;
	private FormData fdlListmails, fdListmails;

	private Label        wlFirstmails;
	private TextVar      wFirstmails;
	private FormData     fdlFirstmails, fdFirstmails;

	
	private Label        wlSSLPort;
	private TextVar      wSSLPort;
	private FormData     fdlSSLPort, fdSSLPort;

	private Label        wlUseSSL;
	private Button       wUseSSL;
	private FormData     fdlUseSSL, fdUseSSL;

	private Label        wlDelete;
	private Button       wDelete;
	private FormData     fdlDelete, fdDelete;

    private Button wOK, wCancel;

    private Listener lsOK, lsCancel;

    private JobEntryGetPOP jobEntry;

    private Shell shell;

    private SelectionAdapter lsDef;

    private boolean changed;

    public JobEntryGetPOPDialog(Shell parent, JobEntryInterface jobEntryInt, Repository rep, JobMeta jobMeta)
    {
        super(parent, jobEntryInt, rep, jobMeta);
        jobEntry = (JobEntryGetPOP) jobEntryInt;
        if (this.jobEntry.getName() == null)
            this.jobEntry.setName(Messages.getString("JobGetPOP.Name.Default"));
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
        shell.setText(Messages.getString("JobGetPOP.Title"));

        int middle = props.getMiddlePct();
        int margin = Const.MARGIN;

        // Filename line
        wlName = new Label(shell, SWT.RIGHT);
        wlName.setText(Messages.getString("JobGetPOP.Name.Label"));
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


		// ServerName line
		wlServerName=new Label(shell, SWT.RIGHT);
		wlServerName.setText(Messages.getString("JobGetPOP.Server.Label"));
		props.setLook(wlServerName);
		fdlServerName=new FormData();
		fdlServerName.left = new FormAttachment(0, 0);
		fdlServerName.top  = new FormAttachment(wName, margin);
		fdlServerName.right= new FormAttachment(middle, -margin);
		wlServerName.setLayoutData(fdlServerName);
		wServerName=new TextVar(jobMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(wServerName);
		wServerName.addModifyListener(lsMod);
		fdServerName=new FormData();
		fdServerName.left = new FormAttachment(middle, 0);
		fdServerName.top  = new FormAttachment(wName, margin);
		fdServerName.right= new FormAttachment(100, 0);
		wServerName.setLayoutData(fdServerName);


		// UserName line
		wlUserName=new Label(shell, SWT.RIGHT);
		wlUserName.setText(Messages.getString("JobGetPOP.Username.Label"));
		props.setLook(wlUserName);
		fdlUserName=new FormData();
		fdlUserName.left = new FormAttachment(0, 0);
		fdlUserName.top  = new FormAttachment(wServerName, margin);
		fdlUserName.right= new FormAttachment(middle, -margin);
		wlUserName.setLayoutData(fdlUserName);
		wUserName=new TextVar(jobMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(wUserName);
		wUserName.setToolTipText(Messages.getString("JobGetPOP.Username.Tooltip"));
		wUserName.addModifyListener(lsMod);
		fdUserName=new FormData();
		fdUserName.left = new FormAttachment(middle, 0);
		fdUserName.top  = new FormAttachment(wServerName, margin);
		fdUserName.right= new FormAttachment(100, 0);
		wUserName.setLayoutData(fdUserName);


		// Password line
		wlPassword=new Label(shell, SWT.RIGHT);
		wlPassword.setText(Messages.getString("JobGetPOP.Password.Label"));
		props.setLook(wlPassword);
		fdlPassword=new FormData();
		fdlPassword.left = new FormAttachment(0, 0);
		fdlPassword.top  = new FormAttachment(wUserName, margin);
		fdlPassword.right= new FormAttachment(middle, -margin);
		wlPassword.setLayoutData(fdlPassword);
		wPassword=new TextVar(jobMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(wPassword);
		wPassword.setEchoChar('*');
		wPassword.addModifyListener(lsMod);
		fdPassword=new FormData();
		fdPassword.left = new FormAttachment(middle, 0);
		fdPassword.top  = new FormAttachment(wUserName, margin);
		fdPassword.right= new FormAttachment(100, 0);
		wPassword.setLayoutData(fdPassword);

		// OK, if the password contains a variable, we don't want to have the password hidden...
		wPassword.getTextWidget().addModifyListener(new ModifyListener()
		{
			public void modifyText(ModifyEvent e)
			{
				checkPasswordVisible();
			}
		});


		// USE POP3 connection with SSL
		wlUseSSL=new Label(shell, SWT.RIGHT);
		wlUseSSL.setText(Messages.getString("JobGetPOP.UseSSLMails.Label"));
		props.setLook(wlUseSSL);
		fdlUseSSL=new FormData();
		fdlUseSSL.left = new FormAttachment(0, 0);
		fdlUseSSL.top  = new FormAttachment(wPassword, margin);
		fdlUseSSL.right= new FormAttachment(middle, -margin);
		wlUseSSL.setLayoutData(fdlUseSSL);
		wUseSSL=new Button(shell, SWT.CHECK);
		props.setLook(wUseSSL);
		fdUseSSL=new FormData();
		wUseSSL.setToolTipText(Messages.getString("JobGetPOP.UseSSLMails.Tooltip"));
		fdUseSSL.left = new FormAttachment(middle, 0);
		fdUseSSL.top  = new FormAttachment(wPassword, margin);
		fdUseSSL.right= new FormAttachment(100, 0);
		wUseSSL.setLayoutData(fdUseSSL);

		wUseSSL.addSelectionListener(new SelectionAdapter() 
		{
			public void widgetSelected(SelectionEvent e) 
			{
				
				EnableSSL();
			}
		}
			);

		// port
		wlSSLPort=new Label(shell, SWT.RIGHT);
		wlSSLPort.setText(Messages.getString("JobGetPOP.SSLPort.Label"));
		props.setLook(wlSSLPort);
		fdlSSLPort=new FormData();
		fdlSSLPort.left = new FormAttachment(0, 0);
		fdlSSLPort.top  = new FormAttachment(wUseSSL, margin);
		fdlSSLPort.right= new FormAttachment(middle, -margin);
		wlSSLPort.setLayoutData(fdlSSLPort);
		wSSLPort=new TextVar(jobMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(wSSLPort);
		wSSLPort.setToolTipText(Messages.getString("JobGetPOP.SSLPort.Tooltip"));
		wSSLPort.addModifyListener(lsMod);
		fdSSLPort=new FormData();
		fdSSLPort.left = new FormAttachment(middle, 0);
		fdSSLPort.top  = new FormAttachment(wUseSSL, margin);
		fdSSLPort.right= new FormAttachment(100, 0);
		wSSLPort.setLayoutData(fdSSLPort);


		// OutputDirectory line
		wlOutputDirectory=new Label(shell, SWT.RIGHT);
		wlOutputDirectory.setText(Messages.getString("JobGetPOP.OutputDirectory.Label"));
		props.setLook(wlOutputDirectory);
		fdlOutputDirectory=new FormData();
		fdlOutputDirectory.left = new FormAttachment(0, 0);
		fdlOutputDirectory.top  = new FormAttachment(wSSLPort, margin);
		fdlOutputDirectory.right= new FormAttachment(middle, -margin);
		wlOutputDirectory.setLayoutData(fdlOutputDirectory);
		wOutputDirectory=new TextVar(jobMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(wOutputDirectory);
		wOutputDirectory.setToolTipText(Messages.getString("JobGetPOP.OutputDirectory.Tooltip"));
		wOutputDirectory.addModifyListener(lsMod);
		fdOutputDirectory=new FormData();
		fdOutputDirectory.left = new FormAttachment(middle, 0);
		fdOutputDirectory.top  = new FormAttachment(wSSLPort, margin);
		fdOutputDirectory.right= new FormAttachment(100, 0);
		wOutputDirectory.setLayoutData(fdOutputDirectory);

		// Filename pattern line
		wlFilenamePattern=new Label(shell, SWT.RIGHT);
		wlFilenamePattern.setText(Messages.getString("JobGetPOP.FilenamePattern.Label"));
		props.setLook(wlFilenamePattern);
		fdlFilenamePattern=new FormData();
		fdlFilenamePattern.left = new FormAttachment(0, 0);
		fdlFilenamePattern.top  = new FormAttachment(wOutputDirectory, margin);
		fdlFilenamePattern.right= new FormAttachment(middle, -margin);
		wlFilenamePattern.setLayoutData(fdlFilenamePattern);
		wFilenamePattern=new TextVar(jobMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(wFilenamePattern);
		wFilenamePattern.setToolTipText(Messages.getString("JobGetPOP.FilenamePattern.Tooltip"));
		wFilenamePattern.addModifyListener(lsMod);
		fdFilenamePattern=new FormData();
		fdFilenamePattern.left = new FormAttachment(middle, 0);
		fdFilenamePattern.top  = new FormAttachment(wOutputDirectory, margin);
		fdFilenamePattern.right= new FormAttachment(100, 0);
		wFilenamePattern.setLayoutData(fdFilenamePattern);


        // Whenever something changes, set the tooltip to the expanded version:
        wFilenamePattern.addModifyListener(new ModifyListener()
        {
            public void modifyText(ModifyEvent e)
            {
                wFilenamePattern.setToolTipText(jobMeta.environmentSubstitute(wFilenamePattern.getText()));
            }
        });

		// List of mails of retrieve
		wlListmails = new Label(shell, SWT.RIGHT);
		wlListmails.setText(Messages.getString("JobGetPOP.Listmails.Label"));
		props.setLook(wlListmails);
		fdlListmails = new FormData();
		fdlListmails.left = new FormAttachment(0, 0);
		fdlListmails.right = new FormAttachment(middle, 0);
		fdlListmails.top = new FormAttachment(wFilenamePattern, margin);
		wlListmails.setLayoutData(fdlListmails);
		wListmails = new CCombo(shell, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER);
		wListmails.add(Messages.getString("JobGetPOP.RetrieveAllMails.Label"));
		wListmails.add(Messages.getString("JobGetPOP.RetrieveUnreadMails.Label"));
		wListmails.add(Messages.getString("JobGetPOP.RetrieveFirstMails.Label"));
		wListmails.select(0); // +1: starts at -1

		props.setLook(wListmails);
		fdListmails= new FormData();
		fdListmails.left = new FormAttachment(middle, 0);
		fdListmails.top = new FormAttachment(wFilenamePattern, margin);
		fdListmails.right = new FormAttachment(100, 0);
		wListmails.setLayoutData(fdListmails);

		wListmails.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				ChooseListMails();
				
			}
		});


		// Retieve the first ... mails
		wlFirstmails = new Label(shell, SWT.RIGHT);
		wlFirstmails.setText(Messages.getString("JobGetPOP.Firstmails.Label"));
		props.setLook(wlFirstmails);
		fdlFirstmails = new FormData();
		fdlFirstmails.left = new FormAttachment(0, 0);
		fdlFirstmails.right = new FormAttachment(middle, 0);
		fdlFirstmails.top = new FormAttachment(wListmails, margin);
		wlFirstmails.setLayoutData(fdlFirstmails);

		wFirstmails = new TextVar(jobMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(wFirstmails);
		wFirstmails.addModifyListener(lsMod);
		fdFirstmails = new FormData();
		fdFirstmails.left = new FormAttachment(middle, 0);
		fdFirstmails.top = new FormAttachment(wListmails, margin);
		fdFirstmails.right = new FormAttachment(100, 0);
		wFirstmails.setLayoutData(fdFirstmails);

		// Delete mails after retrieval...
		wlDelete=new Label(shell, SWT.RIGHT);
		wlDelete.setText(Messages.getString("JobGetPOP.DeleteMails.Label"));
		props.setLook(wlDelete);
		fdlDelete=new FormData();
		fdlDelete.left = new FormAttachment(0, 0);
		fdlDelete.top  = new FormAttachment(wFirstmails, margin);
		fdlDelete.right= new FormAttachment(middle, -margin);
		wlDelete.setLayoutData(fdlDelete);
		wDelete=new Button(shell, SWT.CHECK);
		props.setLook(wDelete);
		fdDelete=new FormData();
		wDelete.setToolTipText(Messages.getString("JobGetPOP.DeleteMails.Tooltip"));
		fdDelete.left = new FormAttachment(middle, 0);
		fdDelete.top  = new FormAttachment(wFirstmails, margin);
		fdDelete.right= new FormAttachment(100, 0);
		wDelete.setLayoutData(fdDelete);


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
        wServerName.addSelectionListener(lsDef);

        // Detect X or ALT-F4 or something that kills this window...
        shell.addShellListener(new ShellAdapter()
        {
            public void shellClosed(ShellEvent e)
            {
                cancel();
            }
        });

        getData();
		EnableSSL();
		ChooseListMails();
        BaseStepDialog.setSize(shell);

        shell.open();
        props.setDialogSize(shell, "JobFileExistsDialogSize");
        while (!shell.isDisposed())
        {
            if (!display.readAndDispatch())
                display.sleep();
        }
        return jobEntry;
    }
    
	private void EnableSSL()
	{
	
		wSSLPort.setEnabled(wUseSSL.getSelection());	
	}
	
	public void checkPasswordVisible()
	{
		String password = wPassword.getText();
		java.util.List<String> list = new ArrayList<String>();
		StringUtil.getUsedVariables(password, list, true);
		if (list.isEmpty())
		{
			wPassword.setEchoChar('*');
		}
		else
		{
			wPassword.setEchoChar('\0'); // Show it all...
		}
	}
	
	public void ChooseListMails()
	{
		jobEntry.setChanged();
		if (wListmails.getSelectionIndex()==2)
			wFirstmails.setEnabled(true);
		else
			wFirstmails.setEnabled(false);
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
        if (jobEntry.getServerName() != null)
            wServerName.setText(jobEntry.getServerName());
		if (jobEntry.getUserName() != null)
			wUserName.setText(jobEntry.getUserName());
		if (jobEntry.getPassword() != null)
			wPassword.setText(jobEntry.getPassword());

		wUseSSL.setSelection(jobEntry.getUseSSL());
		
		if (jobEntry.getSSLPort() !=null)
		{
		
			wSSLPort.setText(jobEntry.getSSLPort());
		}
		else
		{
			wSSLPort.setText("995");
		}

		
		if (jobEntry.getOutputDirectory() != null)
			wOutputDirectory.setText(jobEntry.getOutputDirectory());
		if (jobEntry.getFilenamePattern() != null)
			wFilenamePattern.setText(jobEntry.getFilenamePattern());
		if (jobEntry.retrievemails>=0) 
		{
			wListmails.select(jobEntry.retrievemails );
		}
		else
		{
			wListmails.select(0); // Retrieve All Mails
		}

		if (jobEntry.getFirstMails() != null)
			wFirstmails.setText(jobEntry.getFirstMails());

		wDelete.setSelection(jobEntry.getDelete());
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
        jobEntry.setServerName(wServerName.getText());
		jobEntry.setUserName(wUserName.getText());
		jobEntry.setPassword(wPassword.getText());
		jobEntry.setUseSSL(wUseSSL.getSelection());
		jobEntry.setSSLPort(wSSLPort.getText());
		jobEntry.setOutputDirectory(wOutputDirectory.getText());
		jobEntry.setFilenamePattern(wFilenamePattern.getText());
		jobEntry.retrievemails = wListmails.getSelectionIndex();
		jobEntry.setFirstMails(wFirstmails.getText());
		jobEntry.setDelete(wDelete.getSelection());
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