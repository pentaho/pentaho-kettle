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

package org.pentaho.di.ui.job.entries.syslog;

import java.net.InetAddress;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.snmp4j.UserTarget;
import org.snmp4j.smi.UdpAddress;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.Props;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entries.syslog.JobEntrySyslog;
import org.pentaho.di.job.entries.syslog.SyslogDefs;
import org.pentaho.di.job.entry.JobEntryDialogInterface;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.ui.core.gui.WindowProperty;
import org.pentaho.di.ui.core.widget.ComboVar;
import org.pentaho.di.ui.core.widget.LabelText;
import org.pentaho.di.ui.core.widget.LabelTextVar;
import org.pentaho.di.ui.core.widget.StyledTextComp;
import org.pentaho.di.ui.job.dialog.JobDialog;
import org.pentaho.di.ui.job.entry.JobEntryDialog;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

/**
 * This dialog allows you to edit the Syslog job entry settings.
 * 
 * @author Samatar
 * @since 19-06-2003
 */

public class JobEntrySyslogDialog extends JobEntryDialog implements JobEntryDialogInterface
{
	private static Class<?> PKG = JobEntrySyslog.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$
	
    private LabelText wName;

    private FormData fdName;

    private LabelTextVar wServerName;
    private FormData fdServerName;
    
    private Label wlFacility;
    
    private FormData fdlFacility;

    private Button wOK, wCancel;

    private Listener lsOK, lsCancel;

    private JobEntrySyslog jobEntry;

    private Shell shell;

    private SelectionAdapter lsDef;

    private boolean changed;    
    
	private Group wServerSettings;
    private FormData fdServerSettings;
    
    private Group wLogSettings;
    private FormData fdLogSettings;
    
	private CTabFolder   wTabFolder;
	private Composite    wGeneralComp;	
	private CTabItem     wGeneralTab;
	private FormData	 fdGeneralComp;
	private FormData     fdTabFolder;
    
    private FormData     fdPort;

    private LabelTextVar wPort;
    
    private FormData     fdFacility;
    private CCombo wFacility;
    
    private Label wlPriority;
    private FormData fdlPriority;
    private FormData     fdPriority;
    private CCombo wPriority;
    
    private Label wlDatePattern;
    private FormData fdlDatePattern;
    private FormData     fdDatePattern;
    private ComboVar wDatePattern;
    
	private Button wTest;
	
	private FormData fdTest;
	
	private Listener lsTest;
    
	private Group wMessageGroup;
    private FormData fdMessageGroup;
    
    private Label wlMessage;
    private StyledTextComp wMessage;
    private FormData fdlMessage, fdMessage;
    
    private Label wlAddTimestamp;
    private FormData fdlAddTimestamp;
    private Button wAddTimestamp;
    private FormData fdAddTimestamp;
    
    private Label wlAddHostName;
    private FormData fdlAddHostName;
    private Button wAddHostName;
    private FormData fdAddHostName;
	

    public JobEntrySyslogDialog(Shell parent, JobEntryInterface jobEntryInt, Repository rep, JobMeta jobMeta)
    {
        super(parent, jobEntryInt, rep, jobMeta);
        jobEntry = (JobEntrySyslog) jobEntryInt;
        if (this.jobEntry.getName() == null)
            this.jobEntry.setName(BaseMessages.getString(PKG, "JobEntrySyslog.Name.Default"));
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
        shell.setText(BaseMessages.getString(PKG, "JobEntrySyslog.Title"));

        int middle = props.getMiddlePct();
        int margin = Const.MARGIN;

        // Job entry name line
        wName = new LabelText(shell, BaseMessages.getString(PKG, "JobEntrySyslog.Name.Label"), BaseMessages.getString(PKG, "JobEntrySyslog.Name.Tooltip"));
        wName.addModifyListener(lsMod);
        fdName = new FormData();
        fdName.top = new FormAttachment(0, 0);
        fdName.left = new FormAttachment(0, 0);
        fdName.right = new FormAttachment(100, 0);
        wName.setLayoutData(fdName);
        
        
        wTabFolder = new CTabFolder(shell, SWT.BORDER);
 		props.setLook(wTabFolder, Props.WIDGET_STYLE_TAB);
 		
 		//////////////////////////
		// START OF GENERAL TAB   ///
		//////////////////////////
		
		
		
		wGeneralTab=new CTabItem(wTabFolder, SWT.NONE);
		wGeneralTab.setText(BaseMessages.getString(PKG, "JobEntrySyslog.Tab.General.Label"));
		
		wGeneralComp = new Composite(wTabFolder, SWT.NONE);
 		props.setLook(wGeneralComp);

		FormLayout generalLayout = new FormLayout();
		generalLayout.marginWidth  = 3;
		generalLayout.marginHeight = 3;
		wGeneralComp.setLayout(generalLayout);
        
	     // ////////////////////////
	     // START OF SERVER SETTINGS GROUP///
	     // /
	    wServerSettings = new Group(wGeneralComp, SWT.SHADOW_NONE);
	    props.setLook(wServerSettings);
	    wServerSettings.setText(BaseMessages.getString(PKG, "JobEntrySyslog.ServerSettings.Group.Label"));

	    FormLayout ServerSettingsgroupLayout = new FormLayout();
	    ServerSettingsgroupLayout.marginWidth = 10;
	    ServerSettingsgroupLayout.marginHeight = 10;

	    wServerSettings.setLayout(ServerSettingsgroupLayout);

	    // Server port line
        wServerName = new LabelTextVar(jobMeta, wServerSettings, BaseMessages.getString(PKG, "JobEntrySyslog.Server.Label"), BaseMessages.getString(PKG, "JobEntrySyslog.Server.Tooltip"));
        props.setLook(wServerName);
        wServerName.addModifyListener(lsMod);
        fdServerName = new FormData();
        fdServerName.left 	= new FormAttachment(0, 0);
        fdServerName.top  	= new FormAttachment(wName, margin);
        fdServerName.right	= new FormAttachment(100, 0);
        wServerName.setLayoutData(fdServerName);
	    
        // Server port line
        wPort = new LabelTextVar(jobMeta, wServerSettings, BaseMessages.getString(PKG, "JobEntrySyslog.Port.Label"), BaseMessages.getString(PKG, "JobEntrySyslog.Port.Tooltip"));
        props.setLook(wPort);
        wPort.addModifyListener(lsMod);
        fdPort = new FormData();
        fdPort.left 	= new FormAttachment(0, 0);
        fdPort.top  	= new FormAttachment(wServerName, margin);
        fdPort.right	= new FormAttachment(100, 0);
        wPort.setLayoutData(fdPort);
        
		
		// Test connection button
		wTest=new Button(wServerSettings,SWT.PUSH);
		wTest.setText(BaseMessages.getString(PKG, "JobEntrySyslog.TestConnection.Label"));
 		props.setLook(wTest);
		fdTest=new FormData();
		wTest.setToolTipText(BaseMessages.getString(PKG, "JobEntrySyslog.TestConnection.Tooltip"));
		fdTest.top  = new FormAttachment(wPort, 2*margin);
		fdTest.right= new FormAttachment(100, 0);
		wTest.setLayoutData(fdTest);
        
        
	     fdServerSettings = new FormData();
	     fdServerSettings.left = new FormAttachment(0, margin);
	     fdServerSettings.top = new FormAttachment(wName, margin);
	     fdServerSettings.right = new FormAttachment(100, -margin);
	     wServerSettings.setLayoutData(fdServerSettings);
	     // ///////////////////////////////////////////////////////////
	     // / END OF SERVER SETTINGS GROUP
	     // ///////////////////////////////////////////////////////////
        
	    // ////////////////////////
	    // START OF Log SETTINGS GROUP///
	    // /
	    wLogSettings = new Group(wGeneralComp, SWT.SHADOW_NONE);
	    props.setLook(wLogSettings);
	    wLogSettings.setText(BaseMessages.getString(PKG, "JobEntrySyslog.LogSettings.Group.Label"));

	    FormLayout LogSettingsgroupLayout = new FormLayout();
	    LogSettingsgroupLayout.marginWidth = 10;
	    LogSettingsgroupLayout.marginHeight = 10;

	    wLogSettings.setLayout(LogSettingsgroupLayout);

	    //Facility type
	  	wlFacility= new Label(wLogSettings, SWT.RIGHT);
	  	wlFacility.setText(BaseMessages.getString(PKG, "JobEntrySyslog.Facility.Label"));
	  	props.setLook(wlFacility);
	  	fdlFacility= new FormData();
	  	fdlFacility.left = new FormAttachment(0, margin);
	  	fdlFacility.right = new FormAttachment(middle, -margin);
	  	fdlFacility.top = new FormAttachment(wServerSettings, margin);
	  	wlFacility.setLayoutData(fdlFacility);
	  	wFacility= new CCombo(wLogSettings, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER);
	  	wFacility.setItems(SyslogDefs.FACILITYS);
	  	
		props.setLook(wFacility);
		fdFacility= new FormData();
		fdFacility.left = new FormAttachment(middle, margin);
		fdFacility.top = new FormAttachment(wServerSettings, margin);
		fdFacility.right = new FormAttachment(100, 0);
		wFacility.setLayoutData(fdFacility);
		wFacility.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				
				
			}
		});
		
	      //Priority type
	  	wlPriority= new Label(wLogSettings, SWT.RIGHT);
	  	wlPriority.setText(BaseMessages.getString(PKG, "JobEntrySyslog.Priority.Label"));
	  	props.setLook(wlPriority);
	  	fdlPriority= new FormData();
	  	fdlPriority.left = new FormAttachment(0, margin);
	  	fdlPriority.right = new FormAttachment(middle, -margin);
	  	fdlPriority.top = new FormAttachment(wFacility, margin);
	  	wlPriority.setLayoutData(fdlPriority);
	  	wPriority= new CCombo(wLogSettings, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER);
	  	wPriority.setItems(SyslogDefs.PRIORITYS);
	  	
		props.setLook(wPriority);
		fdPriority= new FormData();
		fdPriority.left = new FormAttachment(middle, margin);
		fdPriority.top = new FormAttachment(wFacility, margin);
		fdPriority.right = new FormAttachment(100, 0);
		wPriority.setLayoutData(fdPriority);
		wPriority.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				
				
			}
		});

	    fdLogSettings = new FormData();
	    fdLogSettings.left = new FormAttachment(0, margin);
	    fdLogSettings.top = new FormAttachment(wServerSettings, margin);
	    fdLogSettings.right = new FormAttachment(100, -margin);
	    wLogSettings.setLayoutData(fdLogSettings);
	    // ///////////////////////////////////////////////////////////
	    // / END OF Log SETTINGS GROUP
	    // ///////////////////////////////////////////////////////////
        

	     // ////////////////////////
	     // START OF MESSAGE GROUP///
	     // /
	     wMessageGroup = new Group(wGeneralComp, SWT.SHADOW_NONE);
	     props.setLook(wMessageGroup);
	     wMessageGroup.setText(BaseMessages.getString(PKG, "JobEntrySyslog.MessageGroup.Group.Label"));
	     FormLayout MessageGroupgroupLayout = new FormLayout();
	     MessageGroupgroupLayout.marginWidth = 10;
	     MessageGroupgroupLayout.marginHeight = 10;
	     wMessageGroup.setLayout(MessageGroupgroupLayout);
	     
	     // Add HostName?
        wlAddHostName = new Label(wMessageGroup, SWT.RIGHT);
        wlAddHostName.setText(BaseMessages.getString(PKG, "JobEntrySyslog.AddHostName.Label"));
        props.setLook(wlAddHostName);
        fdlAddHostName = new FormData();
        fdlAddHostName.left = new FormAttachment(0, 0);
        fdlAddHostName.top = new FormAttachment(wLogSettings,margin);
        fdlAddHostName.right = new FormAttachment(middle, -margin);
        wlAddHostName.setLayoutData(fdlAddHostName);
        wAddHostName = new Button(wMessageGroup, SWT.CHECK);
        props.setLook(wAddHostName);
        wAddHostName.setToolTipText(BaseMessages.getString(PKG, "JobEntrySyslog.AddHostName.Tooltip"));
        fdAddHostName = new FormData();
        fdAddHostName.left = new FormAttachment(middle, margin);
        fdAddHostName.top = new FormAttachment(wLogSettings, margin);
        fdAddHostName.right = new FormAttachment(100, 0);
        wAddHostName.setLayoutData(fdAddHostName);
        wAddHostName.addSelectionListener(new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent e)
            {
                jobEntry.setChanged();
            }
        });
 	
        // Add timestamp?
        wlAddTimestamp = new Label(wMessageGroup, SWT.RIGHT);
        wlAddTimestamp.setText(BaseMessages.getString(PKG, "JobEntrySyslog.AddTimestamp.Label"));
        props.setLook(wlAddTimestamp);
        fdlAddTimestamp = new FormData();
        fdlAddTimestamp.left = new FormAttachment(0, 0);
        fdlAddTimestamp.top = new FormAttachment(wAddHostName,margin);
        fdlAddTimestamp.right = new FormAttachment(middle, -margin);
        wlAddTimestamp.setLayoutData(fdlAddTimestamp);
        wAddTimestamp = new Button(wMessageGroup, SWT.CHECK);
        props.setLook(wAddTimestamp);
        wAddTimestamp.setToolTipText(BaseMessages.getString(PKG, "JobEntrySyslog.AddTimestamp.Tooltip"));
        fdAddTimestamp = new FormData();
        fdAddTimestamp.left = new FormAttachment(middle, margin);
        fdAddTimestamp.top = new FormAttachment(wAddHostName, margin);
        fdAddTimestamp.right = new FormAttachment(100, 0);
        wAddTimestamp.setLayoutData(fdAddTimestamp);
        wAddTimestamp.addSelectionListener(new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent e)
            {
            	activeAddTimestamp();	
                jobEntry.setChanged();
            }
        });
        
	    //DatePattern type
	  	wlDatePattern= new Label(wMessageGroup, SWT.RIGHT);
	  	wlDatePattern.setText(BaseMessages.getString(PKG, "JobEntrySyslog.DatePattern.Label"));
	  	props.setLook(wlDatePattern);
	  	fdlDatePattern= new FormData();
	  	fdlDatePattern.left = new FormAttachment(0, margin);
	  	fdlDatePattern.right = new FormAttachment(middle, -margin);
	  	fdlDatePattern.top = new FormAttachment(wAddTimestamp, margin);
	  	wlDatePattern.setLayoutData(fdlDatePattern);
	  	wDatePattern= new ComboVar(jobMeta, wMessageGroup, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER);
	  	wDatePattern.setItems(Const.getDateFormats());
		props.setLook(wDatePattern);
		fdDatePattern= new FormData();
		fdDatePattern.left = new FormAttachment(middle, margin);
		fdDatePattern.top = new FormAttachment(wAddTimestamp, margin);
		fdDatePattern.right = new FormAttachment(100, 0);
		wDatePattern.setLayoutData(fdDatePattern);
		wDatePattern.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				
				
			}
		});
	        
	     
        // Message line
        wlMessage = new Label(wMessageGroup, SWT.RIGHT);
        wlMessage.setText(BaseMessages.getString(PKG, "JobEntrySyslog.Message.Label"));
        props.setLook(wlMessage);
        fdlMessage = new FormData();
        fdlMessage.left = new FormAttachment(0, margin);
        fdlMessage.top = new FormAttachment(wLogSettings, margin);
        fdlMessage.right = new FormAttachment(middle, -margin);
        wlMessage.setLayoutData(fdlMessage);
        
        wMessage=new StyledTextComp(jobEntry, wMessageGroup, SWT.MULTI | SWT.LEFT | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL, "");
        props.setLook(wMessage);
        wMessage.addModifyListener(lsMod);
        fdMessage = new FormData();
        fdMessage.left = new FormAttachment(middle, margin);
        fdMessage.top = new FormAttachment(wDatePattern, margin);
        fdMessage.right = new FormAttachment(100, -2*margin);
        fdMessage.bottom = new FormAttachment(100, -margin);
        wMessage.setLayoutData(fdMessage);

	     fdMessageGroup = new FormData();
	     fdMessageGroup.left = new FormAttachment(0, margin);
	     fdMessageGroup.top = new FormAttachment(wLogSettings, margin);
	     fdMessageGroup.right = new FormAttachment(100, -margin);
	     fdMessageGroup.bottom = new FormAttachment(100, -margin);
	     wMessageGroup.setLayoutData(fdMessageGroup);
	     // ///////////////////////////////////////////////////////////
	     // / END OF MESSAGE GROUP
	     // ///////////////////////////////////////////////////////////
	    
	    
	     
	     
		fdGeneralComp=new FormData();
		fdGeneralComp.left  = new FormAttachment(0, 0);
		fdGeneralComp.top   = new FormAttachment(0, 0);
		fdGeneralComp.right = new FormAttachment(100, 0);
		fdGeneralComp.bottom= new FormAttachment(100, 0);
		wGeneralComp.setLayoutData(fdGeneralComp);
		
		wGeneralComp.layout();
		wGeneralTab.setControl(wGeneralComp);
 		props.setLook(wGeneralComp);
 		
 		
 		
		/////////////////////////////////////////////////////////////
		/// END OF GENERAL TAB
		/////////////////////////////////////////////////////////////
		

		
		fdTabFolder = new FormData();
		fdTabFolder.left  = new FormAttachment(0, 0);
		fdTabFolder.top   = new FormAttachment(wName, margin);
		fdTabFolder.right = new FormAttachment(100, 0);
		fdTabFolder.bottom= new FormAttachment(100, -50);
		wTabFolder.setLayoutData(fdTabFolder);
		
		
        wOK = new Button(shell, SWT.PUSH);
        wOK.setText(BaseMessages.getString(PKG, "System.Button.OK"));
        wCancel = new Button(shell, SWT.PUSH);
        wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel"));

        BaseStepDialog.positionBottomButtons(shell, new Button[] { wOK, wCancel }, margin, wTabFolder);

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
        lsTest     = new Listener() { public void handleEvent(Event e) { test(); } };

        wCancel.addListener(SWT.Selection, lsCancel);
        wOK.addListener(SWT.Selection, lsOK);
        wTest.addListener    (SWT.Selection, lsTest    );
        
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
        activeAddTimestamp();
        wTabFolder.setSelection(0);
        BaseStepDialog.setSize(shell);

        shell.open();
        props.setDialogSize(shell, "JobEntrySyslogDialogSize");
        while (!shell.isDisposed())
        {
            if (!display.readAndDispatch())
                display.sleep();
        }
        return jobEntry;
    }
    private void activeAddTimestamp()
    {
    	wlDatePattern.setEnabled(wAddTimestamp.getSelection());
    	wDatePattern.setEnabled(wAddTimestamp.getSelection());
    }
    private void test()
    {
    	boolean testOK=false;
    	String errMsg=null;
    	String hostname=jobMeta.environmentSubstitute(wServerName.getText());
    	int nrPort=Const.toInt(jobMeta.environmentSubstitute(""+wPort.getText()),SyslogDefs.DEFAULT_PORT);
 
    	
    	try{
    		UdpAddress udpAddress=new UdpAddress(InetAddress.getByName(hostname), nrPort);
    		UserTarget usertarget = new UserTarget();
    		usertarget.setAddress(udpAddress);
    		
    		testOK=usertarget.getAddress().isValid() ;    	
    	
    		if(!testOK) errMsg=BaseMessages.getString(PKG, "JobEntrySyslog.CanNotGetAddress",hostname);
	    	
    	}catch(Exception e)
    	{
    		errMsg=e.getMessage();
    	}
    	if(testOK)
    	{
			MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_INFORMATION );
			mb.setMessage(BaseMessages.getString(PKG, "JobEntrySyslog.Connected.OK",hostname) +Const.CR);
			mb.setText(BaseMessages.getString(PKG, "JobEntrySyslog.Connected.Title.Ok"));
			mb.open();
		}else
		{
			MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
			mb.setMessage(BaseMessages.getString(PKG, "JobEntrySyslog.Connected.NOK.ConnectionBad",hostname) +Const.CR+errMsg+Const.CR);
			mb.setText(BaseMessages.getString(PKG, "JobEntrySyslog.Connected.Title.Bad"));
			mb.open(); 
	    }
	   
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
        wName.getTextWidget().selectAll();

        wServerName.setText(Const.NVL(jobEntry.getServerName(), ""));
        wPort.setText(Const.NVL(jobEntry.getPort(), String.valueOf(SyslogDefs.DEFAULT_PORT)));
        if(jobEntry.getFacility()!=null) wFacility.setText(jobEntry.getFacility());
        if(jobEntry.getPriority()!=null) wPriority.setText(jobEntry.getPriority());
        if(jobEntry.getMessage()!=null) wMessage.setText(jobEntry.getMessage());
        if(jobEntry.getDatePattern()!=null) wDatePattern.setText(jobEntry.getDatePattern());
        wAddTimestamp.setSelection(jobEntry.isAddTimestamp());
        wAddHostName.setSelection(jobEntry.isAddHostName());
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
			mb.setMessage(BaseMessages.getString(PKG, "JobEntrySyslog.PleaseGiveJobEntryAName.Message"));
			mb.setText(BaseMessages.getString(PKG, "JobEntrySyslog.PleaseGiveJobEntryAName.Title"));
			mb.open(); 
			return;
        }
        jobEntry.setName(wName.getText());
        jobEntry.setPort(wPort.getText());
        jobEntry.setServerName(wServerName.getText());
        jobEntry.setFacility(wFacility.getText());
        jobEntry.setPriority(wPriority.getText());
        jobEntry.setMessage(wMessage.getText());
        jobEntry.addTimestamp(wAddTimestamp.getSelection());
        jobEntry.setDatePattern(wDatePattern.getText());
        jobEntry.addHostName(wAddHostName.getSelection());
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