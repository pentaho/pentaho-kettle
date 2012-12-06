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

package org.pentaho.di.ui.job.entries.writetofile;

import java.nio.charset.Charset;
import java.util.ArrayList;

import org.eclipse.swt.SWT;
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
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.MessageBox; 

import org.pentaho.di.core.Const;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entries.writetofile.JobEntryWriteToFile;
import org.pentaho.di.job.entry.JobEntryDialogInterface;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.gui.WindowProperty;
import org.pentaho.di.ui.core.widget.ComboVar;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.job.dialog.JobDialog;
import org.pentaho.di.ui.job.entry.JobEntryDialog;
import org.pentaho.di.ui.trans.step.BaseStepDialog;



/**
 * This dialog allows you to edit the Write to file job entry settings.
 *
 * @author Samatar Hassan
 * @since  28-01-2007
 */
public class JobEntryWriteToFileDialog extends JobEntryDialog implements JobEntryDialogInterface
{
	private static Class<?> PKG = JobEntryWriteToFile.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

    private static final String[] FILETYPES = new String[] {
           BaseMessages.getString(PKG, "JobWriteToFile.Filetype.All") };
	
	private Label        wlName;
	private Text         wName;
    private FormData     fdlName, fdName;

	private Label        wlFilename;
	private Button       wbFilename;
	private TextVar      wFilename;
	private FormData     fdlFilename, fdbFilename, fdFilename;
	
    private Label        wlCreateParentFolder;
    private Button       wCreateParentFolder;
    private FormData     fdlCreateParentFolder, fdCreateParentFolder;
    
    private Label        wlAppendFile;
    private Button       wAppendFile;
    private FormData     fdlAppendFile, fdAppendFile;
    
    private Label        wlEncoding;
    private ComboVar       wEncoding;
    private FormData     fdlEncoding, fdEncoding;
    
    private Group wFileGroup;
    private FormData fdFileGroup;
    
    private Group wContentGroup;
    private FormData fdContentGroup;
    
    private Label wlContent;
    private Text wContent;
    private FormData fdlContent, fdContent;

	private Button wOK, wCancel;
	private Listener lsOK, lsCancel;

	private JobEntryWriteToFile jobEntry;
	private Shell       	shell;

	private SelectionAdapter lsDef;

	private boolean changed;

	private boolean gotEncodings=false;
	
    public JobEntryWriteToFileDialog(Shell parent, JobEntryInterface jobEntryInt, Repository rep, JobMeta jobMeta)
    {
        super(parent, jobEntryInt, rep, jobMeta);
        jobEntry = (JobEntryWriteToFile) jobEntryInt;
        if (this.jobEntry.getName() == null) 
			this.jobEntry.setName(BaseMessages.getString(PKG, "JobWriteToFile.Name.Default"));
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
		shell.setText(BaseMessages.getString(PKG, "JobWriteToFile.Title"));

		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		// Filename line
		wlName=new Label(shell, SWT.RIGHT);
		wlName.setText(BaseMessages.getString(PKG, "JobWriteToFile.Name.Label"));
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

		
        wOK = new Button(shell, SWT.PUSH);
        wOK.setText(BaseMessages.getString(PKG, "System.Button.OK"));
        wCancel = new Button(shell, SWT.PUSH);
        wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel"));
        
		BaseStepDialog.positionBottomButtons(shell, new Button[] { wOK, wCancel }, margin, null);

		
		// ////////////////////////
		// START OF File GROUP
		// ////////////////////////

		wFileGroup = new Group(shell, SWT.SHADOW_NONE);
		props.setLook(wFileGroup);
		wFileGroup.setText(BaseMessages.getString(PKG, "JobWriteToFile.Group.File.Label"));
		
		FormLayout FileGroupLayout = new FormLayout();
		FileGroupLayout.marginWidth = 10;
		FileGroupLayout.marginHeight = 10;
		wFileGroup.setLayout(FileGroupLayout);		
		
		// Filename line
		wlFilename=new Label(wFileGroup, SWT.RIGHT);
		wlFilename.setText(BaseMessages.getString(PKG, "JobWriteToFile.Filename.Label"));
 		props.setLook(wlFilename);
		fdlFilename=new FormData();
		fdlFilename.left = new FormAttachment(0, 0);
		fdlFilename.top  = new FormAttachment(wName, margin);
		fdlFilename.right= new FormAttachment(middle, -margin);
		wlFilename.setLayoutData(fdlFilename);

		wbFilename=new Button(wFileGroup, SWT.PUSH| SWT.CENTER);
 		props.setLook(wbFilename);
		wbFilename.setText(BaseMessages.getString(PKG, "System.Button.Browse"));
		fdbFilename=new FormData();
		fdbFilename.right= new FormAttachment(100, 0);
		fdbFilename.top  = new FormAttachment(wName, 0);
		wbFilename.setLayoutData(fdbFilename);

		wFilename=new TextVar(jobMeta, wFileGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wFilename);
		wFilename.addModifyListener(lsMod);
		fdFilename=new FormData();
		fdFilename.left = new FormAttachment(middle, 0);
		fdFilename.top  = new FormAttachment(wName, margin);
		fdFilename.right= new FormAttachment(wbFilename, -margin);
		wFilename.setLayoutData(fdFilename);

		// Whenever something changes, set the tooltip to the expanded version:
		wFilename.addModifyListener(new ModifyListener()
			{
				public void modifyText(ModifyEvent e)
				{
					wFilename.setToolTipText(jobMeta.environmentSubstitute( wFilename.getText() ) );
				}
			}
		);

		wbFilename.addSelectionListener
		(
			new SelectionAdapter()
			{
				public void widgetSelected(SelectionEvent e)
				{
					FileDialog dialog = new FileDialog(shell, SWT.SAVE);
					dialog.setFilterExtensions(new String[] {"*"});
					if (wFilename.getText()!=null)
					{
						dialog.setFileName(jobMeta.environmentSubstitute(wFilename.getText()) );
					}
					dialog.setFilterNames(FILETYPES);
					if (dialog.open()!=null)
					{
						wFilename.setText(dialog.getFilterPath()+Const.FILE_SEPARATOR+dialog.getFileName());
					}
				}
			}
		);

        wlCreateParentFolder = new Label(wFileGroup, SWT.RIGHT);
        wlCreateParentFolder.setText(BaseMessages.getString(PKG, "JobWriteToFile.CreateParentFolder.Label"));
        props.setLook(wlCreateParentFolder);
        fdlCreateParentFolder = new FormData();
        fdlCreateParentFolder.left = new FormAttachment(0, 0);
        fdlCreateParentFolder.top = new FormAttachment(wFilename, margin);
        fdlCreateParentFolder.right = new FormAttachment(middle, -margin);
        wlCreateParentFolder.setLayoutData(fdlCreateParentFolder);
        wCreateParentFolder = new Button(wFileGroup, SWT.CHECK);
        props.setLook(wCreateParentFolder);
        wCreateParentFolder.setToolTipText(BaseMessages.getString(PKG, "JobWriteToFile.CreateParentFolder.Tooltip"));
        fdCreateParentFolder = new FormData();
        fdCreateParentFolder.left = new FormAttachment(middle, 0);
        fdCreateParentFolder.top = new FormAttachment(wFilename, margin);
        fdCreateParentFolder.right = new FormAttachment(100, 0);
        wCreateParentFolder.setLayoutData(fdCreateParentFolder);
        wCreateParentFolder.addSelectionListener(new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent e)
            {
                jobEntry.setChanged();
            }
        });
        
        wlAppendFile = new Label(wFileGroup, SWT.RIGHT);
        wlAppendFile.setText(BaseMessages.getString(PKG, "JobWriteToFile.AppendFile.Label"));
        props.setLook(wlAppendFile);
        fdlAppendFile = new FormData();
        fdlAppendFile.left = new FormAttachment(0, 0);
        fdlAppendFile.top = new FormAttachment(wCreateParentFolder, margin);
        fdlAppendFile.right = new FormAttachment(middle, -margin);
        wlAppendFile.setLayoutData(fdlAppendFile);
        wAppendFile = new Button(wFileGroup, SWT.CHECK);
        props.setLook(wAppendFile);
        wAppendFile.setToolTipText(BaseMessages.getString(PKG, "JobWriteToFile.AppendFile.Tooltip"));
        fdAppendFile = new FormData();
        fdAppendFile.left = new FormAttachment(middle, 0);
        fdAppendFile.top = new FormAttachment(wCreateParentFolder, margin);
        fdAppendFile.right = new FormAttachment(100, 0);
        wAppendFile.setLayoutData(fdAppendFile);
        wAppendFile.addSelectionListener(new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent e)
            {
                jobEntry.setChanged();
            }
        });
        
    	fdFileGroup = new FormData();
    	fdFileGroup.left = new FormAttachment(0, margin);
    	fdFileGroup.top = new FormAttachment(wName, margin);
    	fdFileGroup.right = new FormAttachment(100, -margin);
		wFileGroup.setLayoutData(fdFileGroup);
		
		// ///////////////////////////////////////////////////////////
		// / END OF File  GROUP
		// ///////////////////////////////////////////////////////////

        
        ///////////////////////////
    	// START OF Content GROUP
		// ////////////////////////

		wContentGroup = new Group(shell, SWT.SHADOW_NONE);
		props.setLook(wContentGroup);
		wContentGroup.setText(BaseMessages.getString(PKG, "JobWriteToFile.Group.Content.Label"));
		
		FormLayout ContentGroupLayout = new FormLayout();
		ContentGroupLayout.marginWidth = 10;
		ContentGroupLayout.marginHeight = 10;
		wContentGroup.setLayout(ContentGroupLayout);		
        
        // Encoding
        wlEncoding=new Label(wContentGroup, SWT.RIGHT);
        wlEncoding.setText(BaseMessages.getString(PKG, "JobWriteToFile.Encoding.Label"));
        props.setLook(wlEncoding);
        fdlEncoding=new FormData();
        fdlEncoding.left = new FormAttachment(0, -margin);
        fdlEncoding.top  = new FormAttachment(wAppendFile, margin);
        fdlEncoding.right= new FormAttachment(middle, -margin);
        wlEncoding.setLayoutData(fdlEncoding);
        wEncoding=new ComboVar(jobMeta, wContentGroup, SWT.BORDER | SWT.READ_ONLY);
        wEncoding.setEditable(true);
        props.setLook(wEncoding);
        wEncoding.addModifyListener(lsMod);
        fdEncoding=new FormData();
        fdEncoding.left = new FormAttachment(middle, 0);
        fdEncoding.top  = new FormAttachment(wAppendFile,margin);
        fdEncoding.right= new FormAttachment(100, 0);
        wEncoding.setLayoutData(fdEncoding);
        wEncoding.addFocusListener(new FocusListener()
            {
                public void focusLost(org.eclipse.swt.events.FocusEvent e)
                {
                }
            
                public void focusGained(org.eclipse.swt.events.FocusEvent e)
                {
                    setEncodings();
                }
            }
        );




		wlContent = new Label(wContentGroup, SWT.RIGHT);
        wlContent.setText(BaseMessages.getString(PKG, "JobWriteToFile.Content.Label"));
        props.setLook(wlContent);
        fdlContent = new FormData();
        fdlContent.left = new FormAttachment(0, 0);
        fdlContent.top = new FormAttachment(wEncoding, margin);
		fdlContent.right = new FormAttachment(middle, -margin);
        wlContent.setLayoutData(fdlContent);

        wContent = new Text(wContentGroup, SWT.MULTI | SWT.LEFT | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
        props.setLook(wContent,PropsUI.WIDGET_STYLE_FIXED);
        wContent.addModifyListener(lsMod);
        fdContent = new FormData();
        fdContent.left = new FormAttachment(middle, 0);
        fdContent.top = new FormAttachment(wEncoding, margin);
        fdContent.right = new FormAttachment(100, 0);
		fdContent.bottom =new FormAttachment(100, -margin);
        wContent.setLayoutData(fdContent);

    	fdContentGroup = new FormData();
    	fdContentGroup.left = new FormAttachment(0, margin);
    	fdContentGroup.top = new FormAttachment(wFileGroup, margin);
    	fdContentGroup.right = new FormAttachment(100, -margin);
    	fdContentGroup.bottom =new FormAttachment(wOK, -margin);
		wContentGroup.setLayoutData(fdContentGroup);
		
		// ///////////////////////////////////////////////////////////
		// / END OF Content  GROUP
		// ///////////////////////////////////////////////////////////

		
		
		// Add listeners
		lsCancel   = new Listener() { public void handleEvent(Event e) { cancel(); } };
		lsOK       = new Listener() { public void handleEvent(Event e) { ok();     } };

		wCancel.addListener(SWT.Selection, lsCancel);
		wOK.addListener    (SWT.Selection, lsOK    );

		lsDef=new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } };

		wName.addSelectionListener( lsDef );
		wFilename.addSelectionListener( lsDef );

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

	private void setEncodings()
    {
        // Encoding of the text file:
        if (!gotEncodings)
        {
            gotEncodings = true;
            
            wEncoding.removeAll();
            java.util.List<Charset> values = new ArrayList<Charset>(Charset.availableCharsets().values());
            for (Charset charSet:values)
            {
                wEncoding.add( charSet.displayName() );
            }
            // Now select the default!
            String defEncoding = Const.getEnvironmentVariable("file.encoding", "UTF-8");
            int idx = Const.indexOfString(defEncoding, wEncoding.getItems() );
            if (idx>=0) wEncoding.select( idx );
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
		if (jobEntry.getName()    != null) wName.setText( jobEntry.getName() );
		wName.selectAll();
		if (jobEntry.getFilename()!= null) wFilename.setText( jobEntry.getFilename() );
		wCreateParentFolder.setSelection(jobEntry.isCreateParentFolder());
		wAppendFile.setSelection(jobEntry.isAppendFile());
		if (jobEntry.getContent()!= null) wContent.setText( jobEntry.getContent() );
		if (jobEntry.getEncoding()!= null) wEncoding.setText( jobEntry.getEncoding() );
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
		jobEntry.setFilename(wFilename.getText());
		jobEntry.setCreateParentFolder(wCreateParentFolder.getSelection());
		jobEntry.setAppendFile(wAppendFile.getSelection());
		jobEntry.setContent(wContent.getText());
		jobEntry.setEncoding(wEncoding.getText());
		
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