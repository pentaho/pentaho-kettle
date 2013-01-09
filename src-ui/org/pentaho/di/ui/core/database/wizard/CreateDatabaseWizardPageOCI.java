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

package org.pentaho.di.ui.core.database.wizard;

import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.ui.core.PropsUI;


/**
 * 
 * On page one we specify the OCI TNS connection...
 * 
 * @author Matt
 * @since  04-apr-2005
 */
public class CreateDatabaseWizardPageOCI extends WizardPage
{
	private static Class<?> PKG = CreateDatabaseWizard.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private Label    wlTNS;
	private Text     wTNS;
	private FormData fdlTNS, fdTNS;
	
	private PropsUI props;
	private DatabaseMeta info;
	
	public CreateDatabaseWizardPageOCI(String arg, PropsUI props, DatabaseMeta info)
	{
		super(arg);
		this.props=props;
		this.info = info;
		
		setTitle(BaseMessages.getString(PKG, "CreateDatabaseWizardPageOCI.DialogTitle")); //$NON-NLS-1$
		setDescription(BaseMessages.getString(PKG, "CreateDatabaseWizardPageOCI.DialogMessage")); //$NON-NLS-1$
		
		setPageComplete(false);
	}
	
	public void createControl(Composite parent)
	{
		int margin = Const.MARGIN;
		int middle = props.getMiddlePct();
		
		// create the composite to hold the widgets
		Composite composite = new Composite(parent, SWT.NONE);
 		props.setLook(composite);
	    
	    FormLayout compLayout = new FormLayout();
	    compLayout.marginHeight = Const.FORM_MARGIN;
	    compLayout.marginWidth  = Const.FORM_MARGIN;
		composite.setLayout(compLayout);

		wlTNS = new Label(composite, SWT.RIGHT);
		wlTNS.setText(BaseMessages.getString(PKG, "CreateDatabaseWizardPageOCI.TNS.Label")); //$NON-NLS-1$
 		props.setLook(wlTNS);
		fdlTNS = new FormData();
		fdlTNS.left   = new FormAttachment(0,0);
		fdlTNS.right  = new FormAttachment(middle,0);
		wlTNS.setLayoutData(fdlTNS);
		wTNS = new Text(composite, SWT.SINGLE | SWT.BORDER);
 		props.setLook(wTNS);
		fdTNS = new FormData();
		fdTNS.left    = new FormAttachment(middle, margin);
		fdTNS.right   = new FormAttachment(100, 0);
		wTNS.setLayoutData(fdTNS);
		wTNS.addModifyListener(new ModifyListener()
				{
					public void modifyText(ModifyEvent arg0)
					{
						setPageComplete(false);
					}
				});					
		
		// set the composite as the control for this page
		setControl(composite);
	}
	
	public boolean canFlipToNextPage()
	{
		String name = wTNS.getText()!=null?wTNS.getText().length()>0?wTNS.getText():null:null;
		if (name==null)
		{
			setErrorMessage(BaseMessages.getString(PKG, "CreateDatabaseWizardPageOCI.ErrorMessage.NoTNSName")); //$NON-NLS-1$
			return false;
		}
		else
		{
			getDatabaseInfo();
			setErrorMessage(null);
			setMessage(BaseMessages.getString(PKG, "CreateDatabaseWizardPageOCI.Message.Next")); //$NON-NLS-1$
			return true;
		}
	}	
	
	public DatabaseMeta getDatabaseInfo()
	{
		if (wTNS.getText()!=null && wTNS.getText().length()>0) 
		{
			info.setDBName(wTNS.getText());
		}
		
		info.setDBPort(""); //$NON-NLS-1$
		info.setServername(null);
		
		return info;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.WizardPage#getNextPage()
	 */
	public IWizardPage getNextPage()
	{
		IWizard wiz = getWizard();
		return wiz.getPage("2"); //$NON-NLS-1$
	}
}
