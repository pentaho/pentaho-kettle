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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.database.SAPR3DatabaseMeta;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.database.dialog.DatabaseDialog;

/**
 * 
 * On page one we select the username and password. We also provide a test button.
 * 
 * @author Matt
 * @since 04-apr-2005
 */
public class CreateDatabaseWizardPage2 extends WizardPage
{
	private static Class<?> PKG = CreateDatabaseWizard.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private Label			wlUsername;
	private Text			wUsername;
	private FormData		fdlUsername, fdUsername;

	private Label			wlPassword;
	private Text			wPassword;
	private FormData		fdlPassword, fdPassword;
	
	private Button          wTest;
	private FormData        fdTest;

	private PropsUI			props;
	private DatabaseMeta	databaseMeta;

	public CreateDatabaseWizardPage2(String arg, PropsUI props, DatabaseMeta info)
	{
		super(arg);
		this.props = props;
		this.databaseMeta = info;

		setTitle(BaseMessages.getString(PKG, "CreateDatabaseWizardPage2.DialogTitle")); //$NON-NLS-1$
		setDescription(BaseMessages.getString(PKG, "CreateDatabaseWizardPage2.DialogMessage")); //$NON-NLS-1$

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
		compLayout.marginWidth = Const.FORM_MARGIN;
		composite.setLayout(compLayout);

		// USERNAME
		wlUsername = new Label(composite, SWT.RIGHT);
		wlUsername.setText(BaseMessages.getString(PKG, "CreateDatabaseWizardPage2.Username.Label")); //$NON-NLS-1$
 		props.setLook(wlUsername);
		fdlUsername = new FormData();
		fdlUsername.top = new FormAttachment(0, 0);
		fdlUsername.left = new FormAttachment(0, 0);
		fdlUsername.right = new FormAttachment(middle, 0);
		wlUsername.setLayoutData(fdlUsername);
		wUsername = new Text(composite, SWT.SINGLE | SWT.BORDER);
 		props.setLook(wUsername);
		fdUsername = new FormData();
		fdUsername.top = new FormAttachment(0, 0);
		fdUsername.left = new FormAttachment(middle, margin);
		fdUsername.right = new FormAttachment(100, 0);
		wUsername.setLayoutData(fdUsername);
		wUsername.addModifyListener(new ModifyListener()
		{
			public void modifyText(ModifyEvent arg0)
			{
				setPageComplete(false);
			}
		});

		// PASSWORD
		wlPassword = new Label(composite, SWT.RIGHT);
		wlPassword.setText(BaseMessages.getString(PKG, "CreateDatabaseWizardPage2.Password.Label")); //$NON-NLS-1$
 		props.setLook(wlPassword);
		fdlPassword = new FormData();
		fdlPassword.top = new FormAttachment(wUsername, margin);
		fdlPassword.left = new FormAttachment(0, 0);
		fdlPassword.right = new FormAttachment(middle, 0);
		wlPassword.setLayoutData(fdlPassword);
		wPassword = new Text(composite, SWT.SINGLE | SWT.BORDER);
 		props.setLook(wPassword);
		wPassword.setEchoChar('*');
		fdPassword = new FormData();
		fdPassword.top = new FormAttachment(wUsername, margin);
		fdPassword.left = new FormAttachment(middle, margin);
		fdPassword.right = new FormAttachment(100, 0);
		wPassword.setLayoutData(fdPassword);
		wPassword.addModifyListener(new ModifyListener()
		{
			public void modifyText(ModifyEvent arg0)
			{
				setPageComplete(false);
			}
		});
		
		wTest = new Button(composite, SWT.PUSH);
		wTest.setText(BaseMessages.getString(PKG, "CreateDatabaseWizardPage2.TestConnection.Button")); //$NON-NLS-1$
		fdTest = new FormData();
		fdTest.top = new FormAttachment(wPassword, margin*4);
		fdTest.left = new FormAttachment(50, 0);
		wTest.setLayoutData(fdTest);
		wTest.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent arg0)
			{
				test();
			}
		});

		// set the composite as the control for this page
		setControl(composite);
	}
	
	public void test()
	{
		Shell shell = getWizard().getContainer().getShell();
		DatabaseDialog.test(shell, databaseMeta);
	}

	public boolean canFlipToNextPage()
	{
		return false;
	}

	public DatabaseMeta getDatabaseInfo()
	{
		if (wUsername.getText() != null && wUsername.getText().length() > 0)
		{
			databaseMeta.setUsername(wUsername.getText());
		}

		if (wPassword.getText() != null && wPassword.getText().length() > 0)
		{
			databaseMeta.setPassword(wPassword.getText());
		}

		wTest.setEnabled( !(databaseMeta.getDatabaseInterface() instanceof SAPR3DatabaseMeta) );
		
		return databaseMeta;
	}

	public boolean canFinish()
	{
		getDatabaseInfo();

        String[] remarks = databaseMeta.checkParameters(); 
		if (remarks.length == 0)
		{
			setErrorMessage(null);
			setMessage(BaseMessages.getString(PKG, "CreateDatabaseWizardPage2.Message.Finish")); //$NON-NLS-1$
			return true;
		}
		else
		{
			setErrorMessage(BaseMessages.getString(PKG, "CreateDatabaseWizardPage2.ErrorMessage.InvalidInput")); //$NON-NLS-1$
			// setMessage("Select 'Finish' to create the database connection");
			return false;
		}
	}
}