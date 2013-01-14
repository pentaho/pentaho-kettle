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

package org.pentaho.di.ui.job.entries.snmptrap;

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
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Props;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entries.snmptrap.JobEntrySNMPTrap;
import org.pentaho.di.job.entry.JobEntryDialogInterface;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.ui.core.gui.WindowProperty;
import org.pentaho.di.ui.core.widget.LabelText;
import org.pentaho.di.ui.core.widget.LabelTextVar;
import org.pentaho.di.ui.core.widget.StyledTextComp;
import org.pentaho.di.ui.job.dialog.JobDialog;
import org.pentaho.di.ui.job.entry.JobEntryDialog;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.snmp4j.UserTarget;
import org.snmp4j.smi.UdpAddress;

/**
 * This dialog allows you to edit the SNMPTrap job entry settings.
 * 
 * @author Samatar
 * @since 12-09-2008
 */

public class JobEntrySNMPTrapDialog extends JobEntryDialog implements JobEntryDialogInterface
{
	private static Class<?> PKG = JobEntrySNMPTrap.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

    private LabelText wName;

    private FormData fdName;

    private LabelTextVar wServerName;

    private FormData fdServerName;

    private LabelTextVar wTimeout;

    private FormData fdTimeout;
    
    private LabelTextVar wComString;

    private FormData fdComString;
    
    private LabelTextVar wUser;

    private FormData fdUser;
    
    private LabelTextVar wPassphrase;

    private FormData fdPassphrase;
    
    private LabelTextVar wEngineID;

    private FormData fdEngineID;
    
    private LabelTextVar wRetry;

    private FormData fdRetry;

    private Button wOK, wCancel;

    private Listener lsOK, lsCancel;

    private JobEntrySNMPTrap jobEntry;

    private Shell shell;
    
    //private Props props;

    private SelectionAdapter lsDef;

    private boolean changed;    
    
	private Group wServerSettings;
    private FormData fdServerSettings;
    
	private CTabFolder   wTabFolder;
	private Composite    wGeneralComp;	
	private CTabItem     wGeneralTab;
	private FormData	 fdGeneralComp;
	private FormData     fdTabFolder;
    
    private FormData     fdPort;

    private LabelTextVar wPort;
    
    private FormData     fdOID;

    private LabelTextVar wOID;
    
	private Button wTest;
	
	private FormData fdTest;
	
	private Listener lsTest;
	
	private Group wAdvancedSettings;
    private FormData fdAdvancedSettings;
    
	private Group wMessageGroup;
    private FormData fdMessageGroup;
    
    private Label wlMessage;
    private StyledTextComp wMessage;
    private FormData fdlMessage, fdMessage;
    
	private Label wlTargetType;
	private CCombo wTargetType;
	private FormData fdlTargetType, fdTargetType;
	
    public JobEntrySNMPTrapDialog(Shell parent, JobEntryInterface jobEntryInt, Repository rep, JobMeta jobMeta)
    {
        super(parent, jobEntryInt, rep, jobMeta);
        jobEntry = (JobEntrySNMPTrap) jobEntryInt;
        if (this.jobEntry.getName() == null)
            this.jobEntry.setName(BaseMessages.getString(PKG, "JobSNMPTrap.Name.Default"));
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
        shell.setText(BaseMessages.getString(PKG, "JobSNMPTrap.Title"));

        int middle = props.getMiddlePct();
        int margin = Const.MARGIN;

        // Job entry name line
        wName = new LabelText(shell, BaseMessages.getString(PKG, "JobSNMPTrap.Name.Label"), 
        		BaseMessages.getString(PKG, "JobSNMPTrap.Name.Tooltip"));
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
		wGeneralTab.setText(BaseMessages.getString(PKG, "JobSNMPTrap.Tab.General.Label"));
		
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
	    wServerSettings.setText(BaseMessages.getString(PKG, "JobSNMPTrap.ServerSettings.Group.Label"));

	    FormLayout ServerSettingsgroupLayout = new FormLayout();
	    ServerSettingsgroupLayout.marginWidth = 10;
	    ServerSettingsgroupLayout.marginHeight = 10;

	    wServerSettings.setLayout(ServerSettingsgroupLayout);

        // ServerName line
        wServerName = new LabelTextVar(jobMeta,wServerSettings, BaseMessages.getString(PKG, "JobSNMPTrap.Server.Label"), 
        		BaseMessages.getString(PKG, "JobSNMPTrap.Server.Tooltip"));
        props.setLook(wServerName);
        wServerName.addModifyListener(lsMod);
        fdServerName = new FormData();
        fdServerName.left = new FormAttachment(0, 0);
        fdServerName.top = new FormAttachment(wName, margin);
        fdServerName.right = new FormAttachment(100, 0);
        wServerName.setLayoutData(fdServerName);
        
        // Server port line
        wPort = new LabelTextVar(jobMeta,wServerSettings, BaseMessages.getString(PKG, "JobSNMPTrap.Port.Label"), 
        		BaseMessages.getString(PKG, "JobSNMPTrap.Port.Tooltip"));
        props.setLook(wPort);
        wPort.addModifyListener(lsMod);
        fdPort = new FormData();
        fdPort.left 	= new FormAttachment(0, 0);
        fdPort.top  	= new FormAttachment(wServerName, margin);
        fdPort.right	= new FormAttachment(100, 0);
        wPort.setLayoutData(fdPort);
        
        
        // Server OID line
        wOID = new LabelTextVar(jobMeta,wServerSettings, BaseMessages.getString(PKG, "JobSNMPTrap.OID.Label"), 
        		BaseMessages.getString(PKG, "JobSNMPTrap.OID.Tooltip"));
        props.setLook(wOID);
        wOID.addModifyListener(lsMod);
        fdOID = new FormData();
        fdOID.left 	= new FormAttachment(0, 0);
        fdOID.top  	= new FormAttachment(wPort, margin);
        fdOID.right	= new FormAttachment(100, 0);
        wOID.setLayoutData(fdOID);
        
		// Test connection button
		wTest=new Button(wServerSettings,SWT.PUSH);
		wTest.setText(BaseMessages.getString(PKG, "JobSNMPTrap.TestConnection.Label"));
 		props.setLook(wTest);
		fdTest=new FormData();
		wTest.setToolTipText(BaseMessages.getString(PKG, "JobSNMPTrap.TestConnection.Tooltip"));
		fdTest.top  = new FormAttachment(wOID, margin);
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
	     // START OF Advanced SETTINGS GROUP///
	     // /
	     wAdvancedSettings = new Group(wGeneralComp, SWT.SHADOW_NONE);
	     props.setLook(wAdvancedSettings);
	     wAdvancedSettings.setText(BaseMessages.getString(PKG, "JobSNMPTrap.AdvancedSettings.Group.Label"));
	     FormLayout AdvancedSettingsgroupLayout = new FormLayout();
	     AdvancedSettingsgroupLayout.marginWidth = 10;
	     AdvancedSettingsgroupLayout.marginHeight = 10;
	     wAdvancedSettings.setLayout(AdvancedSettingsgroupLayout);

	     //Target type
		  	wlTargetType = new Label(wAdvancedSettings, SWT.RIGHT);
		  	wlTargetType.setText(BaseMessages.getString(PKG, "JobSNMPTrap.TargetType.Label"));
		  	props.setLook(wlTargetType);
		  	fdlTargetType = new FormData();
		  	fdlTargetType.left = new FormAttachment(0, margin);
		  	fdlTargetType.right = new FormAttachment(middle, -margin);
		  	fdlTargetType.top = new FormAttachment(wServerSettings, margin);
		  	wlTargetType.setLayoutData(fdlTargetType);
		  	wTargetType = new CCombo(wAdvancedSettings, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER);
		  	wTargetType.setItems(JobEntrySNMPTrap.target_type_Desc);
		  	
			props.setLook(wTargetType);
			fdTargetType= new FormData();
			fdTargetType.left = new FormAttachment(middle, margin);
			fdTargetType.top = new FormAttachment(wServerSettings, margin);
			fdTargetType.right = new FormAttachment(100, 0);
			wTargetType.setLayoutData(fdTargetType);
			wTargetType.addSelectionListener(new SelectionAdapter()
			{
				public void widgetSelected(SelectionEvent e)
				{
					CheckuseUserTarget();
					
				}
			});
		
      
        // Community String line
        wComString = new LabelTextVar(jobMeta,wAdvancedSettings, BaseMessages.getString(PKG, "JobSNMPTrap.ComString.Label"), 
        		BaseMessages.getString(PKG, "JobSNMPTrap.ComString.Tooltip"));
        props.setLook(wComString);
        wComString.addModifyListener(lsMod);
        fdComString = new FormData();
        fdComString.left = new FormAttachment(0, 0);
        fdComString.top = new FormAttachment(wTargetType, margin);
        fdComString.right = new FormAttachment(100, 0);
        wComString.setLayoutData(fdComString);
        
        // User line
        wUser = new LabelTextVar(jobMeta,wAdvancedSettings, BaseMessages.getString(PKG, "JobSNMPTrap.User.Label"), 
        		BaseMessages.getString(PKG, "JobSNMPTrap.User.Tooltip"));
        props.setLook(wUser);
        wUser.addModifyListener(lsMod);
        fdUser = new FormData();
        fdUser.left = new FormAttachment(0, 0);
        fdUser.top = new FormAttachment(wComString, margin);
        fdUser.right = new FormAttachment(100, 0);
        wUser.setLayoutData(fdUser);    
		
        // Passphrase String line
        wPassphrase = new LabelTextVar(jobMeta,wAdvancedSettings, BaseMessages.getString(PKG, "JobSNMPTrap.Passphrase.Label"), 
        		BaseMessages.getString(PKG, "JobSNMPTrap.Passphrase.Tooltip"));
        props.setLook(wPassphrase);
        wPassphrase.setEchoChar('*');
        wPassphrase.addModifyListener(lsMod);
        fdPassphrase = new FormData();
        fdPassphrase.left = new FormAttachment(0, 0);
        fdPassphrase.top = new FormAttachment(wUser, margin);
        fdPassphrase.right = new FormAttachment(100, 0);
        wPassphrase.setLayoutData(fdPassphrase);  
        
        // EngineID String line
        wEngineID = new LabelTextVar(jobMeta,wAdvancedSettings, BaseMessages.getString(PKG, "JobSNMPTrap.EngineID.Label"), 
        		BaseMessages.getString(PKG, "JobSNMPTrap.EngineID.Tooltip"));
        props.setLook(wEngineID);
        wEngineID.addModifyListener(lsMod);
        fdEngineID = new FormData();
        fdEngineID.left = new FormAttachment(0, 0);
        fdEngineID.top = new FormAttachment(wPassphrase, margin);
        fdEngineID.right = new FormAttachment(100, 0);
        wEngineID.setLayoutData(fdEngineID);  
		
        // Retry line
        wRetry = new LabelTextVar(jobMeta,wAdvancedSettings, BaseMessages.getString(PKG, "JobSNMPTrap.Retry.Label"), 
        		BaseMessages.getString(PKG, "JobSNMPTrap.Retry.Tooltip"));
        props.setLook(wRetry);
        wRetry.addModifyListener(lsMod);
        fdRetry = new FormData();
        fdRetry.left = new FormAttachment(0, 0);
        fdRetry.top = new FormAttachment(wEngineID, margin);
        fdRetry.right = new FormAttachment(100, 0);
        wRetry.setLayoutData(fdRetry);
	     
	     
        // Timeout line
        wTimeout = new LabelTextVar(jobMeta,wAdvancedSettings, BaseMessages.getString(PKG, "JobSNMPTrap.Timeout.Label"), 
        		BaseMessages.getString(PKG, "JobSNMPTrap.Timeout.Tooltip"));
        props.setLook(wTimeout);
        wTimeout.addModifyListener(lsMod);
        fdTimeout = new FormData();
        fdTimeout.left = new FormAttachment(0, 0);
        fdTimeout.top = new FormAttachment(wRetry, margin);
        fdTimeout.right = new FormAttachment(100, 0);
        wTimeout.setLayoutData(fdTimeout);

	    
	     fdAdvancedSettings = new FormData();
	     fdAdvancedSettings.left = new FormAttachment(0, margin);
	     fdAdvancedSettings.top = new FormAttachment(wServerSettings, margin);
	     fdAdvancedSettings.right = new FormAttachment(100, -margin);
	     wAdvancedSettings.setLayoutData(fdAdvancedSettings);
	     // ///////////////////////////////////////////////////////////
	     // / END OF Advanced SETTINGS GROUP
	     // ///////////////////////////////////////////////////////////

	     // ////////////////////////
	     // START OF MESSAGE GROUP///
	     // /
	     wMessageGroup = new Group(wGeneralComp, SWT.SHADOW_NONE);
	     props.setLook(wMessageGroup);
	     wMessageGroup.setText(BaseMessages.getString(PKG, "JobSNMPTrap.MessageGroup.Group.Label"));
	     FormLayout MessageGroupgroupLayout = new FormLayout();
	     MessageGroupgroupLayout.marginWidth = 10;
	     MessageGroupgroupLayout.marginHeight = 10;
	     wMessageGroup.setLayout(MessageGroupgroupLayout);
	     
        // Message line
        wlMessage = new Label(wMessageGroup, SWT.RIGHT);
        wlMessage.setText(BaseMessages.getString(PKG, "JobSNMPTrap.Message.Label"));
        props.setLook(wlMessage);
        fdlMessage = new FormData();
        fdlMessage.left = new FormAttachment(0, 0);
        fdlMessage.top = new FormAttachment(wComString, margin);
        fdlMessage.right = new FormAttachment(middle, -margin);
        wlMessage.setLayoutData(fdlMessage);

        wMessage=new StyledTextComp(jobEntry, wMessageGroup, SWT.MULTI | SWT.LEFT | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL, "");
        props.setLook(wMessage);
        wMessage.addModifyListener(lsMod);
        fdMessage = new FormData();
        fdMessage.left = new FormAttachment(middle, 0);
        fdMessage.top = new FormAttachment(wComString, margin);
        fdMessage.right = new FormAttachment(100, -2*margin);
        fdMessage.bottom = new FormAttachment(100, -margin);
        wMessage.setLayoutData(fdMessage);
        
	     

	     fdMessageGroup = new FormData();
	     fdMessageGroup.left = new FormAttachment(0, margin);
	     fdMessageGroup.top = new FormAttachment(wAdvancedSettings, margin);
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
        wTimeout.addSelectionListener(lsDef);

        // Detect X or ALT-F4 or something that kills this window...
        shell.addShellListener(new ShellAdapter()
        {
            public void shellClosed(ShellEvent e)
            {
                cancel();
            }
        });

        getData();
        CheckuseUserTarget();
        wTabFolder.setSelection(0);
        BaseStepDialog.setSize(shell);

        shell.open();
        props.setDialogSize(shell, "JobSNMPTrapDialogSize");
        while (!shell.isDisposed())
        {
            if (!display.readAndDispatch())
                display.sleep();
        }
        return jobEntry;
    }
    private void CheckuseUserTarget()
    {
    	wComString.setEnabled(wTargetType.getSelectionIndex()==0);
    	wUser.setEnabled(wTargetType.getSelectionIndex()==1);
    	wPassphrase.setEnabled(wTargetType.getSelectionIndex()==1);
    	wEngineID.setEnabled(wTargetType.getSelectionIndex()==1);
    }
    private void test()
    {
    	boolean testOK=false;
    	String errMsg=null;
    	String hostname=jobMeta.environmentSubstitute(wServerName.getText());
    	int nrPort=Const.toInt(jobMeta.environmentSubstitute(""+wPort.getText()),JobEntrySNMPTrap.DEFAULT_PORT);
 
    	
    	try{
    		UdpAddress udpAddress=new UdpAddress(InetAddress.getByName(hostname), nrPort);
    		UserTarget usertarget = new UserTarget();
    		usertarget.setAddress(udpAddress);
    		
    		testOK=usertarget.getAddress().isValid() ;    	
    	
    		if(!testOK) errMsg=BaseMessages.getString(PKG, "JobSNMPTrap.CanNotGetAddress",hostname);
	    	
    	}catch(Exception e)
    	{
    		errMsg=e.getMessage();
    	}
    	if(testOK)
    	{
			MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_INFORMATION );
			mb.setMessage(BaseMessages.getString(PKG, "JobSNMPTrap.Connected.OK",hostname) +Const.CR);
			mb.setText(BaseMessages.getString(PKG, "JobSNMPTrap.Connected.Title.Ok"));
			mb.open();
		}else
		{
			MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
			mb.setMessage(BaseMessages.getString(PKG, "JobSNMPTrap.Connected.NOK.ConnectionBad",hostname) +Const.CR+errMsg+Const.CR);
			mb.setText(BaseMessages.getString(PKG, "JobSNMPTrap.Connected.Title.Bad"));
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
        wPort.setText(jobEntry.getPort());
        wOID.setText(Const.NVL(jobEntry.getOID(), ""));
        wTimeout.setText("" + jobEntry.getTimeout());
        wRetry.setText("" + jobEntry.getRetry());
        wComString.setText(Const.NVL(jobEntry.getComString(), ""));
        wMessage.setText(Const.NVL(jobEntry.getMessage(), ""));
        wTargetType.setText(jobEntry.getTargetTypeDesc(jobEntry.getTargetType()));
        wUser.setText(Const.NVL(jobEntry.getUser(),""));
        wPassphrase.setText(Const.NVL(jobEntry.getPassPhrase(),""));
        wEngineID.setText(Const.NVL(jobEntry.getEngineID(),""));
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
        jobEntry.setPort(wPort.getText());
        jobEntry.setServerName(wServerName.getText());
        jobEntry.setOID(wOID.getText());
        jobEntry.setTimeout(wTimeout.getText());
        jobEntry.setRetry(wTimeout.getText());
        jobEntry.setComString(wComString.getText());
        jobEntry.setMessage(wMessage.getText());
        jobEntry.setTargetType(wTargetType.getText());
        jobEntry.setUser(wUser.getText());
        jobEntry.setPassPhrase(wPassphrase.getText());
        jobEntry.setEngineID(wEngineID.getText());
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