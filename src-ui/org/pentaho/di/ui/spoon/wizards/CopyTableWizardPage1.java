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

package org.pentaho.di.ui.spoon.wizards;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.List;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.ui.core.PropsUI;


/**
 * 
 * On page one we select the source and target databases...
 * 
 * @author Matt
 * @since  29-mar-05
 */
public class CopyTableWizardPage1 extends WizardPage
{
    private static Class<?> PKG = CopyTableWizard.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private List     wSourceDB, wTargetDB;
	private FormData fdSourceDB, fdTargetDB;

	private PropsUI props;
	private java.util.List<DatabaseMeta> databases;
	
    /** @deprecated */
    public CopyTableWizardPage1(String arg, PropsUI props, java.util.List<DatabaseMeta> databases)
    {
        this(arg, databases);
    }

	public CopyTableWizardPage1(String arg, java.util.List<DatabaseMeta> databases)
	{
		super(arg);
		this.props=PropsUI.getInstance();
		this.databases=databases;
		
		setTitle(BaseMessages.getString(PKG, "CopyTableWizardPage1.Dialog.Title")); //$NON-NLS-1$
		setDescription(BaseMessages.getString(PKG, "CopyTableWizardPage1.Dialog.Description")); //$NON-NLS-1$
		
		setPageComplete(false);
	}
	
	public void createControl(Composite parent)
	{
		int margin = Const.MARGIN;
		
		// create the composite to hold the widgets
		Composite composite = new Composite(parent, SWT.NONE);
 		props.setLook(composite);
	    
	    FormLayout compLayout = new FormLayout();
	    compLayout.marginHeight = Const.FORM_MARGIN;
	    compLayout.marginWidth  = Const.FORM_MARGIN;
		composite.setLayout(compLayout);

		wSourceDB = new List(composite, SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
 		props.setLook(wSourceDB);
		for (int i=0;i<databases.size();i++)
		{
			DatabaseMeta dbInfo = (DatabaseMeta)databases.get(i);
			wSourceDB.add(dbInfo.getName());
		}
		fdSourceDB = new FormData();
		fdSourceDB.top    = new FormAttachment(0,0);
		fdSourceDB.left   = new FormAttachment(0,0);
		fdSourceDB.bottom = new FormAttachment(100,0);
		fdSourceDB.right  = new FormAttachment(50,0);
		wSourceDB.setLayoutData(fdSourceDB);
		wSourceDB.addSelectionListener
			(
				new SelectionAdapter()
				{
					public void widgetSelected(SelectionEvent e)
					{
						setPageComplete(false);
					}
				}
			);
		
		wTargetDB = new List(composite, SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
 		props.setLook(wTargetDB);
		for (int i=0;i<databases.size();i++)
		{
			DatabaseMeta dbInfo = (DatabaseMeta)databases.get(i);
			wTargetDB.add(dbInfo.getName());
		}
		fdTargetDB = new FormData();
		fdTargetDB.top    = new FormAttachment(0,0);
		fdTargetDB.left   = new FormAttachment(50,margin);
		fdTargetDB.bottom = new FormAttachment(100,0);
		fdTargetDB.right  = new FormAttachment(100,0);
		wTargetDB.setLayoutData(fdTargetDB);
		wTargetDB.addSelectionListener
			(
				new SelectionAdapter()
				{
					public void widgetSelected(SelectionEvent e)
					{
						setPageComplete(false);
					}
				}
			);
		
		// set the composite as the control for this page
		setControl(composite);
	}
		
	public boolean canFlipToNextPage()
	{
		DatabaseMeta source = getSourceDatabase();
		DatabaseMeta target = getTargetDatabase();
		
		if (source==null && target==null)
		{
			setErrorMessage(BaseMessages.getString(PKG, "CopyTableWizardPage1.SourceAndTargetIsNull.DialogMessage")); //$NON-NLS-1$
			return false;
		}
		else
		if (source==null && target!=null)
		{
			setErrorMessage(BaseMessages.getString(PKG, "CopyTableWizardPage1.SourceIsNull.DialogMessage")); //$NON-NLS-1$
			return false;
		}
		else
		if (source!=null && target==null)
		{
			setErrorMessage(BaseMessages.getString(PKG, "CopyTableWizardPage1.TargetIsNull.DialogMessage")); //$NON-NLS-1$
			return false;
		}
		else
		if (source!=null && target!=null && source.equals(target))
		{
			setErrorMessage(BaseMessages.getString(PKG, "CopyTableWizardPage1.SourceAndTargetIsSame.DialogMessage")); //$NON-NLS-1$
			return false;
		}
		else
		{
			setErrorMessage(null);
			setMessage(BaseMessages.getString(PKG, "CopyTableWizardPage1.GoOnNext.DialogMessage")); //$NON-NLS-1$
			return true;
		}
	}	
	
	public DatabaseMeta getSourceDatabase()
	{
		if (wSourceDB.getSelection().length==1)
		{
			String sourceDbName = wSourceDB.getSelection()[0];
			return DatabaseMeta.findDatabase(databases, sourceDbName);
		}
		return null;
	}
	
	public DatabaseMeta getTargetDatabase()
	{
		if (wTargetDB.getSelection().length==1)
		{
			String targetDbName = wTargetDB.getSelection()[0];
			return DatabaseMeta.findDatabase(databases, targetDbName);
		}
		return null;
	}	
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.WizardPage#getNextPage()
	 */
	public IWizardPage getNextPage()
	{
		CopyTableWizardPage2 page2 = (CopyTableWizardPage2)super.getNextPage();
		if (page2.getInputData())
		{
			page2.getData();
			return page2;
		}
		return this;
	}
}
