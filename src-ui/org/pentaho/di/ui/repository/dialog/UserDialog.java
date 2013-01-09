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

package org.pentaho.di.ui.repository.dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.IUser;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositorySecurityManager;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.gui.WindowProperty;
import org.pentaho.di.ui.trans.step.BaseStepDialog;



public class UserDialog extends Dialog 
{
	private static Class<?> PKG = RepositoryDialogInterface.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private Shell     shell;
	private Label     wlLogin, wlPassword, wlUsername, wlDescription;
	private Text      wLogin, wPassword, wUsername, wDescription;
	
	private Button    wOK, wCancel;
	
	private PropsUI      props;
	private IUser   userinfo;
	
	private boolean    newUser = false;

	private RepositorySecurityManager	securityManager;
	// private Repository  repository;
   
	/**
     * This dialog grabs a UserMeta structure, valid for the specified repository.
     */
	public UserDialog(Shell parent, int style, Repository repository, IUser userInfo)
	{
		super(parent, style);
		this.securityManager = repository.getSecurityManager();
		// this.repository = repository;
		this.userinfo=userInfo;

		this.props=PropsUI.getInstance();
	}
	
	public IUser open() 
	{
		Shell parent = getParent();
		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN );
 		props.setLook(shell);
		shell.setImage(GUIResource.getInstance().getImageUser());
		
		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		FormLayout formLayout = new FormLayout ();
		formLayout.marginWidth  = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;
		
		shell.setText(BaseMessages.getString(PKG, "UserDialog.Dialog.Main.Title")); //$NON-NLS-1$
		shell.setLayout (formLayout);
 		
		// Username
		wlLogin = new Label(shell, SWT.RIGHT ); 
		wlLogin.setText(BaseMessages.getString(PKG, "UserDialog.Label.Login"));  //$NON-NLS-1$
 		props.setLook(wlLogin);
		FormData fdlLogin = new FormData();
		fdlLogin.left = new FormAttachment(0,0); 
		fdlLogin.right= new FormAttachment(middle, -margin);
		fdlLogin.top  = new FormAttachment(0, margin);
		wlLogin.setLayoutData(fdlLogin);
		wLogin = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
 		props.setLook(wLogin);
		FormData fdLogin  = new FormData();
		fdLogin.left = new FormAttachment(middle, 0); 
		fdLogin.right= new FormAttachment(100, 0);
		fdLogin.top  = new FormAttachment(0, margin);
		wLogin.setLayoutData(fdLogin);
		
		// Password
		wlPassword = new Label(shell, SWT.RIGHT ); 
		wlPassword.setText(BaseMessages.getString(PKG, "UserDialog.Label.Password"));  //$NON-NLS-1$
 		props.setLook(wlPassword);
		FormData fdlPassword = new FormData();
		fdlPassword.left = new FormAttachment(0,0);
		fdlPassword.right= new FormAttachment(middle, -margin);
		fdlPassword.top  = new FormAttachment(wLogin, margin);
		wlPassword.setLayoutData(fdlPassword);
		wPassword = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
 		props.setLook(wPassword);
		wPassword.setEchoChar('*');
		FormData fdPassword  = new FormData();
		fdPassword.left = new FormAttachment(middle, 0); 
		fdPassword.right= new FormAttachment(100, 0);
		fdPassword.top  = new FormAttachment(wLogin, margin);
		wPassword.setLayoutData(fdPassword);

		// Username
		wlUsername = new Label(shell, SWT.RIGHT ); 
		wlUsername.setText(BaseMessages.getString(PKG, "UserDialog.Label.FullName"));  //$NON-NLS-1$
 		props.setLook(wlUsername);
		FormData fdlUsername = new FormData();
		fdlUsername.left = new FormAttachment(0,0);
		fdlUsername.right= new FormAttachment(middle, -margin);
		fdlUsername.top  = new FormAttachment(wPassword, margin);
		wlUsername.setLayoutData(fdlUsername);
		wUsername = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
 		props.setLook(wUsername);
		FormData fdUsername  = new FormData();
		fdUsername.left = new FormAttachment(middle, 0); 
		fdUsername.right= new FormAttachment(100, 0);
		fdUsername.top  = new FormAttachment(wPassword, margin);
		wUsername.setLayoutData(fdUsername);

		// Description
		wlDescription = new Label(shell, SWT.RIGHT ); 
		wlDescription.setText(BaseMessages.getString(PKG, "UserDialog.Label.Description"));  //$NON-NLS-1$
 		props.setLook(wlDescription);
		FormData fdlDescription = new FormData();
		fdlDescription.left = new FormAttachment(0,0);
		fdlDescription.right= new FormAttachment(middle, -margin);
		fdlDescription.top  = new FormAttachment(wUsername, margin);
		wlDescription.setLayoutData(fdlDescription);
		wDescription = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
 		props.setLook(wDescription);
		FormData fdDescription  = new FormData();
		fdDescription.left = new FormAttachment(middle, 0); 
		fdDescription.right= new FormAttachment(100, 0);
		fdDescription.top  = new FormAttachment(wUsername, margin);
		wDescription.setLayoutData(fdDescription);

		// Buttons
		wOK     = new Button(shell, SWT.PUSH); 
		wOK.setText(BaseMessages.getString(PKG, "System.Button.OK")); //$NON-NLS-1$
		wCancel = new Button(shell, SWT.PUSH); 
		wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel")); //$NON-NLS-1$
		
		FormData fdOK        = new FormData();
		FormData fdCancel    = new FormData();

		fdOK.left    = new FormAttachment(45, 0); 
		fdOK.top  = new FormAttachment(wDescription, 30);
		wOK.setLayoutData(fdOK);

		fdCancel.left    = new FormAttachment(wOK, margin); 
		fdCancel.top     = new FormAttachment(wDescription, 30);
		wCancel.setLayoutData(fdCancel);
		
		// Add listeners
		wCancel.addListener(SWT.Selection, new Listener ()
			{
				public void handleEvent (Event e) 
				{
					cancel();
				}
			}
		);
		wOK.addListener(SWT.Selection, new Listener ()
			{
				public void handleEvent (Event e) 
				{
					handleOK();
				}
			}
		);
		SelectionAdapter selAdapter=new SelectionAdapter()
			{
				public void widgetDefaultSelected(SelectionEvent e)
				{
					handleOK();	
				}
			};
		wLogin.addSelectionListener(selAdapter);;
		wPassword.addSelectionListener(selAdapter);;
		
		// Detect X or ALT-F4 or something that kills this window...
		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { cancel(); } } );
		
		getData();

		BaseStepDialog.setSize(shell);
		
		if ( userinfo.getObjectId() == null )
		{
		    setNewUser(true);  
		}

		shell.open();
		Display display = parent.getDisplay();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) display.sleep();
		}
		return userinfo;
	}
	
	public void dispose()
	{
		props.setScreen(new WindowProperty(shell));
		shell.dispose();
	}
	
	public void getData()
	{
		if (userinfo.getLogin()!=null) wLogin.setText(userinfo.getLogin());
		if (userinfo.getPassword()!=null) wPassword.setText(userinfo.getPassword());
		if (userinfo.getUsername()!=null) wUsername.setText(userinfo.getUsername());
		if (userinfo.getDescription()!=null) wDescription.setText(userinfo.getDescription());
		//WANTED: Add enabled option from UserInfo here!!!!

	}

	private void cancel()
	{
		userinfo = null;
		dispose();
	}
	
	public void handleOK()
	{
		try
		{
			String login = wLogin.getText();
			
			if ( login == null || login.length() == 0 )
			{
				MessageBox mb = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
				mb.setMessage(BaseMessages.getString(PKG, "UserDialog.Dialog.User.New.EmptyLogin.Message")); //$NON-NLS-1$
				mb.setText(BaseMessages.getString(PKG, "UserDialog.Dialog.User.New.EmptyLogin.Title")); //$NON-NLS-1$
				mb.open();
				
				// don't dispose
				return;			
			}
			
		    if ( isNewUser() )
		    {
		    	ObjectId id = securityManager.getUserID(login);
		    	if ( id != null )
		    	{
					MessageBox mb = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
					mb.setMessage(BaseMessages.getString(PKG, "UserDialog.Dialog.User.New.AlreadyExists.Message")); //$NON-NLS-1$
					mb.setText(BaseMessages.getString(PKG, "UserDialog.Dialog.User.New.AlreadyExists.Title")); //$NON-NLS-1$
					mb.open();
					
					// don't dispose
					return;
		    	}
		    }						    	

			userinfo.setLogin(login);
			userinfo.setPassword(wPassword.getText());
			userinfo.setUsername(wUsername.getText());
			userinfo.setDescription(wDescription.getText());
			securityManager.saveUserInfo(userinfo);
	
			dispose();
		}
		catch(KettleException e)
		{
			MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
			mb.setMessage(BaseMessages.getString(PKG, "UserDialog.Dialog.UnexpectedError.Message")+e.getMessage()); //$NON-NLS-1$
			mb.setText(BaseMessages.getString(PKG, "UserDialog.Dialog.UnexpectedError.Title")); //$NON-NLS-1$
			mb.open(); 
		}
	}
	
	/**
	 * Set the flag this dialog is opened to create a new user
	 * 
	 * @param flag 
	 */
	private void setNewUser(boolean flag)
	{
		newUser = flag;
	}
	
	/**
	 * Get the flag whether this dialog is for a new user or not.
	 * 
	 * @return true when used for a new user else false
	 */
	private boolean isNewUser()
	{
		return newUser;
	}
}