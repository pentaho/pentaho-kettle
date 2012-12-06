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

package org.pentaho.di.ui.trans.steps.syslog;

import java.net.InetAddress;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.FocusListener;
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
import org.eclipse.swt.widgets.Group;
import org.snmp4j.UserTarget;
import org.snmp4j.smi.UdpAddress;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.entries.syslog.SyslogDefs;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.steps.syslog.SyslogMessageMeta;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.widget.ComboVar;
import org.pentaho.di.ui.core.widget.LabelTextVar;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

public class SyslogMessageDialog extends BaseStepDialog implements StepDialogInterface
{
	private static Class<?> PKG = SyslogMessageMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private Label        wlMessageField;
	private CCombo       wMessageField;
	private FormData     fdlMessageField, fdMessageField;
	
	private Group wSettingsGroup;
	private FormData fdSettingsGroup;
	private SyslogMessageMeta input;
	
    private Group wLogSettings;
    private FormData fdLogSettings;
    
    private FormData     fdPort;

    private LabelTextVar wPort;
    
    private FormData     fdFacility;
    private CCombo wFacility;
    
    private Label wlPriority;
    private FormData fdlPriority;
    private FormData     fdPriority;
    private CCombo wPriority;
    
	private Button wTest;
	
	private FormData fdTest;
	
	private Listener lsTest;
	
    private LabelTextVar wServerName;
    private FormData fdServerName;
    
    private Label wlFacility;
    
    private FormData fdlFacility;
    
    
    private Label wlAddTimestamp;
    private FormData fdlAddTimestamp;
    private Button wAddTimestamp;
    private FormData fdAddTimestamp;
    
    private Label wlAddHostName;
    private FormData fdlAddHostName;
    private Button wAddHostName;
    private FormData fdAddHostName;
    
    
    private Label wlDatePattern;
    private FormData fdlDatePattern;
    private FormData     fdDatePattern;
    private ComboVar wDatePattern;
    

	
	private boolean gotPreviousFields=false;
	

	public SyslogMessageDialog(Shell parent, Object in, TransMeta transMeta, String sname)
	{
		super(parent, (BaseStepMeta)in, transMeta, sname);
		input=(SyslogMessageMeta)in;
	}

	public String open()
	{
		Shell parent = getParent();
		Display display = parent.getDisplay();

		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN);
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
		shell.setText(BaseMessages.getString(PKG, "SyslogMessageDialog.Shell.Title")); //$NON-NLS-1$
		
		int middle = props.getMiddlePct();
		int margin=Const.MARGIN;

		// Stepname line
		wlStepname=new Label(shell, SWT.RIGHT);
		wlStepname.setText(BaseMessages.getString(PKG, "SyslogMessageDialog.Stepname.Label")); //$NON-NLS-1$
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
		
		///////////////////////////////// 
		// START OF Settings GROUP  //
		///////////////////////////////// 

		wSettingsGroup = new Group(shell, SWT.SHADOW_NONE);
		props.setLook(wSettingsGroup);
		wSettingsGroup.setText(BaseMessages.getString(PKG, "SyslogMessageDialog.wSettingsGroup.Label"));
		
		FormLayout settingGroupLayout = new FormLayout();
		settingGroupLayout.marginWidth = 10;
		settingGroupLayout.marginHeight = 10;
		wSettingsGroup.setLayout(settingGroupLayout);
		
		 // Server port line
        wServerName = new LabelTextVar(transMeta, wSettingsGroup, BaseMessages.getString(PKG, "SyslogMessageDialog.Server.Label"), BaseMessages.getString(PKG, "SyslogMessageDialog.Server.Tooltip"));
        props.setLook(wServerName);
        wServerName.addModifyListener(lsMod);
        fdServerName = new FormData();
        fdServerName.left 	= new FormAttachment(0, 0);
        fdServerName.top  	= new FormAttachment(wStepname, margin);
        fdServerName.right	= new FormAttachment(100, 0);
        wServerName.setLayoutData(fdServerName);
	    
        // Server port line
        wPort = new LabelTextVar(transMeta, wSettingsGroup, BaseMessages.getString(PKG, "SyslogMessageDialog.Port.Label"), BaseMessages.getString(PKG, "SyslogMessageDialog.Port.Tooltip"));
        props.setLook(wPort);
        wPort.addModifyListener(lsMod);
        fdPort = new FormData();
        fdPort.left 	= new FormAttachment(0, 0);
        fdPort.top  	= new FormAttachment(wServerName, margin);
        fdPort.right	= new FormAttachment(100, 0);
        wPort.setLayoutData(fdPort);
        
		
		// Test connection button
		wTest=new Button(wSettingsGroup,SWT.PUSH);
		wTest.setText(BaseMessages.getString(PKG, "SyslogMessageDialog.TestConnection.Label"));
 		props.setLook(wTest);
		fdTest=new FormData();
		wTest.setToolTipText(BaseMessages.getString(PKG, "SyslogMessageDialog.TestConnection.Tooltip"));
		fdTest.top  = new FormAttachment(wPort, 2*margin);
		fdTest.right= new FormAttachment(100, 0);
		wTest.setLayoutData(fdTest);
        
		fdSettingsGroup = new FormData();
		fdSettingsGroup.left = new FormAttachment(0, margin);
		fdSettingsGroup.top = new FormAttachment(wStepname, margin);
		fdSettingsGroup.right = new FormAttachment(100, -margin);
		wSettingsGroup.setLayoutData(fdSettingsGroup);
		
		///////////////////////////////// 
		// END OF Settings Fields GROUP  //
		
		  // ////////////////////////
	    // START OF Log SETTINGS GROUP///
	    // /
	    wLogSettings = new Group(shell, SWT.SHADOW_NONE);
	    props.setLook(wLogSettings);
	    wLogSettings.setText(BaseMessages.getString(PKG, "SyslogMessageDialog.LogSettings.Group.Label"));

	    FormLayout LogSettingsgroupLayout = new FormLayout();
	    LogSettingsgroupLayout.marginWidth = 10;
	    LogSettingsgroupLayout.marginHeight = 10;

	    wLogSettings.setLayout(LogSettingsgroupLayout);

	    //Facility type
	  	wlFacility= new Label(wLogSettings, SWT.RIGHT);
	  	wlFacility.setText(BaseMessages.getString(PKG, "SyslogMessageDialog.Facility.Label"));
	  	props.setLook(wlFacility);
	  	fdlFacility= new FormData();
	  	fdlFacility.left = new FormAttachment(0, margin);
	  	fdlFacility.right = new FormAttachment(middle, -margin);
	  	fdlFacility.top = new FormAttachment(wSettingsGroup, margin);
	  	wlFacility.setLayoutData(fdlFacility);
	  	wFacility= new CCombo(wLogSettings, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER);
	  	wFacility.setItems(SyslogDefs.FACILITYS);
	  	
		props.setLook(wFacility);
		fdFacility= new FormData();
		fdFacility.left = new FormAttachment(middle, margin);
		fdFacility.top = new FormAttachment(wSettingsGroup, margin);
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
	  	wlPriority.setText(BaseMessages.getString(PKG, "SyslogMessageDialog.Priority.Label"));
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
		
		 // Add HostName?
        wlAddHostName = new Label(wLogSettings, SWT.RIGHT);
        wlAddHostName.setText(BaseMessages.getString(PKG, "SyslogMessageDialog.AddHostName.Label"));
        props.setLook(wlAddHostName);
        fdlAddHostName = new FormData();
        fdlAddHostName.left = new FormAttachment(0, 0);
        fdlAddHostName.top = new FormAttachment(wPriority,margin);
        fdlAddHostName.right = new FormAttachment(middle, -margin);
        wlAddHostName.setLayoutData(fdlAddHostName);
        wAddHostName = new Button(wLogSettings, SWT.CHECK);
        props.setLook(wAddHostName);
        wAddHostName.setToolTipText(BaseMessages.getString(PKG, "SyslogMessageDialog.AddHostName.Tooltip"));
        fdAddHostName = new FormData();
        fdAddHostName.left = new FormAttachment(middle, margin);
        fdAddHostName.top = new FormAttachment(wPriority, margin);
        fdAddHostName.right = new FormAttachment(100, 0);
        wAddHostName.setLayoutData(fdAddHostName);
        wAddHostName.addSelectionListener(new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent e)
            {
                input.setChanged();
            }
        });
        
	 	
        // Add timestamp?
        wlAddTimestamp = new Label(wLogSettings, SWT.RIGHT);
        wlAddTimestamp.setText(BaseMessages.getString(PKG, "SyslogMessageDialog.AddTimestamp.Label"));
        props.setLook(wlAddTimestamp);
        fdlAddTimestamp = new FormData();
        fdlAddTimestamp.left = new FormAttachment(0, 0);
        fdlAddTimestamp.top = new FormAttachment(wAddHostName,margin);
        fdlAddTimestamp.right = new FormAttachment(middle, -margin);
        wlAddTimestamp.setLayoutData(fdlAddTimestamp);
        wAddTimestamp = new Button(wLogSettings, SWT.CHECK);
        props.setLook(wAddTimestamp);
        wAddTimestamp.setToolTipText(BaseMessages.getString(PKG, "SyslogMessageDialog.AddTimestamp.Tooltip"));
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
                input.setChanged();
            }
        });
        
	    //DatePattern type
	  	wlDatePattern= new Label(wLogSettings, SWT.RIGHT);
	  	wlDatePattern.setText(BaseMessages.getString(PKG, "SyslogMessageDialog.DatePattern.Label"));
	  	props.setLook(wlDatePattern);
	  	fdlDatePattern= new FormData();
	  	fdlDatePattern.left = new FormAttachment(0, margin);
	  	fdlDatePattern.right = new FormAttachment(middle, -margin);
	  	fdlDatePattern.top = new FormAttachment(wAddTimestamp, margin);
	  	wlDatePattern.setLayoutData(fdlDatePattern);
	  	wDatePattern= new ComboVar(transMeta, wLogSettings, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER);
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
		

		// MessageField field
		wlMessageField=new Label(wLogSettings, SWT.RIGHT);
		wlMessageField.setText(BaseMessages.getString(PKG, "SyslogMessageDialog.MessageNameField.Label")); //$NON-NLS-1$
 		props.setLook(wlMessageField);
		fdlMessageField=new FormData();
		fdlMessageField.left = new FormAttachment(0, margin);
		fdlMessageField.right= new FormAttachment(middle, -margin);
		fdlMessageField.top  = new FormAttachment(wDatePattern, margin);
		wlMessageField.setLayoutData(fdlMessageField);
		
		wMessageField=new CCombo(wLogSettings, SWT.BORDER | SWT.READ_ONLY);
 		props.setLook(wMessageField);
 		wMessageField.setEditable(true);
 		wMessageField.addModifyListener(lsMod);
		fdMessageField=new FormData();
		fdMessageField.left = new FormAttachment(middle,margin);
		fdMessageField.top  = new FormAttachment(wDatePattern, margin);
		fdMessageField.right= new FormAttachment(100, 0);
		wMessageField.setLayoutData(fdMessageField);
		wMessageField.addFocusListener(new FocusListener()
        {
            public void focusLost(org.eclipse.swt.events.FocusEvent e)
            {
            }
        
            public void focusGained(org.eclipse.swt.events.FocusEvent e)
            {
                get();
            }
        }
    );

	    fdLogSettings = new FormData();
	    fdLogSettings.left = new FormAttachment(0, margin);
	    fdLogSettings.top = new FormAttachment(wSettingsGroup, margin);
	    fdLogSettings.right = new FormAttachment(100, -margin);
	    wLogSettings.setLayoutData(fdLogSettings);
	    // ///////////////////////////////////////////////////////////
	    // / END OF Log SETTINGS GROUP
	    // ///////////////////////////////////////////////////////////

		
		

		// THE BUTTONS
		wOK=new Button(shell, SWT.PUSH);
		wOK.setText(BaseMessages.getString(PKG, "System.Button.OK")); //$NON-NLS-1$
		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel")); //$NON-NLS-1$

		setButtonPositions(new Button[] { wOK, wCancel }, margin, wLogSettings);

		// Add listeners
		lsOK       = new Listener() { public void handleEvent(Event e) { ok();        } };

		lsCancel   = new Listener() { public void handleEvent(Event e) { cancel();    } };
		
		lsTest     = new Listener() { public void handleEvent(Event e) { test(); } };
		
		wOK.addListener    (SWT.Selection, lsOK    );
		wCancel.addListener(SWT.Selection, lsCancel);
		wTest.addListener    (SWT.Selection, lsTest    );
		
		lsDef=new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } };
		
		wStepname.addSelectionListener( lsDef );
		
		// Detect X or ALT-F4 or something that kills this window...
		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { cancel(); } } );

		// Set the shell size, based upon previous time...
		setSize();
		getData();
		activeAddTimestamp();
		input.setChanged(changed);

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
		if(input.getMessageFieldName() !=null)   wMessageField.setText(input.getMessageFieldName());
		if(input.getServerName()!=null) wServerName.setText(input.getServerName());
		if(input.getPort()!=null) wPort.setText(input.getPort());
        if(input.getFacility()!=null) wFacility.setText(input.getFacility());
        if(input.getPriority()!=null) wPriority.setText(input.getPriority());
        if(input.getDatePattern()!=null) wDatePattern.setText(input.getDatePattern());
        wAddTimestamp.setSelection(input.isAddTimestamp());
        wAddHostName.setSelection(input.isAddHostName());
        
		wStepname.selectAll();
	}
   private void activeAddTimestamp()
    {
    	wlDatePattern.setEnabled(wAddTimestamp.getSelection());
    	wDatePattern.setEnabled(wAddTimestamp.getSelection());
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

        input.setServerName(wServerName.getText());
        input.setPort(wPort.getText());
        input.setFacility(wFacility.getText());
        input.setPriority(wPriority.getText());
        input.setMessageFieldName(wMessageField.getText());
        input.addTimestamp(wAddTimestamp.getSelection());
        input.addHostName(wAddHostName.getSelection());
        
        input.setDatePattern(wDatePattern.getText());
        
		stepname = wStepname.getText(); // return value
		
		dispose();
	}

	 private void get()
		{
		 if(!gotPreviousFields)
		 {
			 gotPreviousFields=true;
			try
			{
				String source=wMessageField.getText();
				
				wMessageField.removeAll();
				RowMetaInterface r = transMeta.getPrevStepFields(stepname);
				if (r!=null)
				{
					wMessageField.setItems(r.getFieldNames());
					if(source!=null) wMessageField.setText(source);
				}
			}
			catch(KettleException ke)
			{
				new ErrorDialog(shell, BaseMessages.getString(PKG, "SyslogMessageDialog.FailedToGetFields.DialogTitle"), 
						BaseMessages.getString(PKG, "SyslogMessageDialog.FailedToGetFields.DialogMessage"), ke); //$NON-NLS-1$ //$NON-NLS-2$
			}
		 }
		}
	private void test()
    {
    	boolean testOK=false;
    	String errMsg=null;
    	String hostname=transMeta.environmentSubstitute(wServerName.getText());
    	int nrPort=Const.toInt(transMeta.environmentSubstitute(""+wPort.getText()),SyslogDefs.DEFAULT_PORT);
    	
    	try{
    		UdpAddress udpAddress=new UdpAddress(InetAddress.getByName(hostname), nrPort);
    		UserTarget usertarget = new UserTarget();
    		usertarget.setAddress(udpAddress);
    		
    		testOK=usertarget.getAddress().isValid() ;    	
    	
    		if(!testOK) errMsg=BaseMessages.getString(PKG, "SyslogMessageDialog.CanNotGetAddress",hostname);
	    	
    	}catch(Exception e)
    	{
    		errMsg=e.getMessage();
    	}
    	if(testOK)
    	{
			MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_INFORMATION );
			mb.setMessage(BaseMessages.getString(PKG, "SyslogMessageDialog.Connected.OK",hostname) +Const.CR);
			mb.setText(BaseMessages.getString(PKG, "SyslogMessageDialog.Connected.Title.Ok"));
			mb.open();
		}else
		{
			MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
			mb.setMessage(BaseMessages.getString(PKG, "SyslogMessageDialog.Connected.NOK.ConnectionBad",hostname) +Const.CR+errMsg+Const.CR);
			mb.setText(BaseMessages.getString(PKG, "SyslogMessageDialog.Connected.Title.Bad"));
			mb.open(); 
	    }
	   
    }
	   
}
