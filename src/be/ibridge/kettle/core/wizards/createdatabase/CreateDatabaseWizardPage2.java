/**********************************************************************
 **                                                                   **
 **               This code belongs to the KETTLE project.            **
 **                                                                   **
 ** Kettle, from version 2.2 on, is released into the public domain   **
 ** under the Lesser GNU Public License (LGPL).                       **
 **                                                                   **
 ** For more details, please read the document LICENSE.txt, included  **
 ** in this project                                                   **
 **                                                                   **
 ** http://www.kettle.be                                              **
 ** info@kettle.be                                                    **
 **                                                                   **
 **********************************************************************/


package be.ibridge.kettle.core.wizards.createdatabase;

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

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.Props;
import be.ibridge.kettle.core.database.DatabaseMeta;
import be.ibridge.kettle.core.dialog.DatabaseDialog;

/**
 * 
 * On page one we select the username and password. We also provide a test button.
 * 
 * @author Matt
 * @since 04-apr-2005
 */
public class CreateDatabaseWizardPage2 extends WizardPage
{
	private Label			wlUsername;
	private Text			wUsername;
	private FormData		fdlUsername, fdUsername;

	private Label			wlPassword;
	private Text			wPassword;
	private FormData		fdlPassword, fdPassword;
	
	private Button          wTest;
	private FormData        fdTest;

	private Props			props;
	private DatabaseMeta	info;

	public CreateDatabaseWizardPage2(String arg, Props props, DatabaseMeta info)
	{
		super(arg);
		this.props = props;
		this.info = info;

		setTitle("Set the username and password");
		setDescription("Specify the username and password to connect to the database.");

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
		wlUsername.setText("The username");
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
		wlPassword.setText("The password");
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
		wTest.setText("Test database connection");
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
		DatabaseDialog.test(shell, info, props);
	}

	public boolean canFlipToNextPage()
	{
		return false;
	}

	public DatabaseMeta getDatabaseInfo()
	{
		if (wUsername.getText() != null && wUsername.getText().length() > 0)
		{
			info.setUsername(wUsername.getText());
		}

		if (wPassword.getText() != null && wPassword.getText().length() > 0)
		{
			info.setPassword(wPassword.getText());
		}

		return info;
	}

	public boolean canFinish()
	{
		getDatabaseInfo();

        String[] remarks = info.checkParameters(); 
		if (remarks.length == 0)
		{
			setErrorMessage(null);
			setMessage("Select 'Finish' to create the database connection");
			return true;
		}
		else
		{
			setErrorMessage("Please make sure that at least the name of the connection, the database name and the database type are specified!");
			// setMessage("Select 'Finish' to create the database connection");
			return false;
		}
	}
}