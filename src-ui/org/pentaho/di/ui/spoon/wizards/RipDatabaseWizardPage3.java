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


package org.pentaho.di.ui.spoon.wizards;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectory;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.repository.dialog.SelectDirectoryDialog;




/**
 * 
 * On page one we select the name of the target job and the directory.
 * 
 * @author Matt
 * @since  17-apr-04
 */
public class RipDatabaseWizardPage3 extends WizardPage
{
	private Label        wlJobname;
	private Text         wJobname;
    private FormData     fdlJobname, fdJobname;

    private Label        wlDirectory;
	private Text         wDirectory;
	private Button       wbDirectory;
    private FormData     fdlDirectory, fdbDirectory, fdDirectory;    

	private PropsUI props;
	private Repository rep;
	private RepositoryDirectoryInterface repositoryDirectory;
	private String directory;
	private Shell shell;

	public RipDatabaseWizardPage3(String arg, Repository rep)
	{
		super(arg);
		this.props=PropsUI.getInstance();
		this.rep = rep;
	
		setTitle("Enter the job details");
		setDescription("Enter the name of the target job and the directory to put everything in.");
		
		setPageComplete(false);
	}
	
	public void createControl(Composite parent)
	{
		shell = parent.getShell();

        int margin = Const.MARGIN;
		int middle = props.getMiddlePct();
		
		ModifyListener lsMod = new ModifyListener()
		{
			public void modifyText(ModifyEvent arg0)
			{
				setPageComplete(canFlipToNextPage());
			}
		};
		
		// create the composite to hold the widgets
		Composite composite = new Composite(parent, SWT.NONE);
 		props.setLook(composite);
	    
	    FormLayout compLayout = new FormLayout();
	    compLayout.marginHeight = Const.FORM_MARGIN;
	    compLayout.marginWidth  = Const.FORM_MARGIN;
		composite.setLayout(compLayout);

		// Job name:
		wlJobname=new Label(composite, SWT.RIGHT);
		wlJobname.setText("Job name :");
 		props.setLook(wlJobname);
		fdlJobname=new FormData();
		fdlJobname.left = new FormAttachment(0, 0);
		fdlJobname.right= new FormAttachment(middle, -margin);
		fdlJobname.top  = new FormAttachment(0, margin);
		wlJobname.setLayoutData(fdlJobname);
		wJobname=new Text(composite, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wJobname);
		wJobname.addModifyListener(lsMod);
		fdJobname=new FormData();
		fdJobname.left = new FormAttachment(middle, 0);
		fdJobname.top  = new FormAttachment(0, margin);
		fdJobname.right= new FormAttachment(100, 0);
		wJobname.setLayoutData(fdJobname);
		
		// Directory:
		wlDirectory=new Label(composite, SWT.RIGHT);
		wlDirectory.setText("Directory :");
 		props.setLook(wlDirectory);
		fdlDirectory=new FormData();
		fdlDirectory.left = new FormAttachment(0, 0);
		fdlDirectory.right= new FormAttachment(middle, -margin);
		fdlDirectory.top  = new FormAttachment(wJobname, margin);
		wlDirectory.setLayoutData(fdlDirectory);

		wbDirectory=new Button(composite, SWT.PUSH);
		wbDirectory.setText("...");
 		props.setLook(wbDirectory);
		fdbDirectory=new FormData();
		fdbDirectory.right= new FormAttachment(100, 0);
		fdbDirectory.top  = new FormAttachment(wJobname, margin);
		wbDirectory.setLayoutData(fdbDirectory);
		wbDirectory.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent arg0)
			{
                if (rep!=null)
                {
    				SelectDirectoryDialog sdd = new SelectDirectoryDialog(shell, SWT.NONE, rep);
    				repositoryDirectory = sdd.open();
    				if (repositoryDirectory!=null)
    				{
    					wDirectory.setText(repositoryDirectory.getPath());
    					setPageComplete(canFlipToNextPage());
    				}
                }
                else
                {
                    DirectoryDialog directoryDialog = new DirectoryDialog(shell, SWT.NONE);
                    directoryDialog.setFilterPath(wDirectory.getText());
                    directoryDialog.setText("Select a target directory");
                    directoryDialog.setMessage("Select the target directory of the job and transformations:");
                    String target = directoryDialog.open();
                    if (target!=null)
                    {
                        wDirectory.setText(target);
                        directory = target;
                        setPageComplete(canFlipToNextPage());
                    }
                }
			}
		});

		wDirectory=new Text(composite, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wDirectory);
		wDirectory.setEditable(false);
		fdDirectory=new FormData();
		fdDirectory.left = new FormAttachment(middle, 0);
		fdDirectory.top  = new FormAttachment(wJobname, margin);
		fdDirectory.right= new FormAttachment(wbDirectory, 0);
		wDirectory.setLayoutData(fdDirectory);
		
		// set the composite as the control for this page
		setControl(composite);
	}
		
	public boolean canFlipToNextPage()
	{
		return false;  
	}	
	
	public String getJobname()
	{
		String jobname = wJobname.getText();
		if (jobname!=null && jobname.length()==0) jobname=null;
		
		return jobname;
	}

	/**
	 * @return Returns the directory.
	 */
	public RepositoryDirectoryInterface getRepositoryDirectory()
	{
		return repositoryDirectory;
	}
	
	public boolean canFinish()
	{
		return !Const.isEmpty(getJobname()) && ( getRepositoryDirectory()!=null || !Const.isEmpty(getDirectory()) );
	}

    /**
     * @return the directory
     */
    public String getDirectory()
    {
        return directory;
    }
}
