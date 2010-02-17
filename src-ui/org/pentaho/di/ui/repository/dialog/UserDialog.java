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

 
/*
 * Created on 18-mei-2003
 *
 */

package org.pentaho.di.ui.repository.dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
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
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.ProfileMeta;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositorySecurityManager;
import org.pentaho.di.repository.UserInfo;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.gui.WindowProperty;
import org.pentaho.di.ui.trans.step.BaseStepDialog;



public class UserDialog extends Dialog 
{
	private static Class<?> PKG = KettleDatabaseRepositoryDialog.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private Shell     shell;
	private Label     wlLogin, wlPassword, wlUsername, wlDescription;
	private Text      wLogin, wPassword, wUsername, wDescription;

	private Label        wlProfile;
	private Button       wnProfile, weProfile, wdProfile;
	private CCombo       wProfile;
	private FormData     fdlProfile, fdProfile, fdnProfile, fdeProfile, fddProfile;
	
	private Button    wOK, wCancel;
	
	private PropsUI      props;
	private UserInfo   userinfo;
	
	private boolean    newUser = false;

	private RepositorySecurityManager	securityManager;
	private Repository  repository;
   
	/**
     * This dialog grabs a UserMeta structure, valid for the specified repository.
     */
	public UserDialog(Shell parent, int style, Repository repository, UserInfo userInfo)
	{
		super(parent, style);
		this.securityManager = repository.getSecurityManager();
		this.repository = repository;
		this.userinfo=userInfo;

		this.props=PropsUI.getInstance();
	}
	
	public UserInfo open() 
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

		// Profile selector
		wlProfile=new Label(shell, SWT.RIGHT);
		wlProfile.setText(BaseMessages.getString(PKG, "UserDialog.Label.Profile")); //$NON-NLS-1$
 		props.setLook(wlProfile);
		fdlProfile=new FormData();
		fdlProfile.left = new FormAttachment(0, 0);
		fdlProfile.right= new FormAttachment(middle, -margin);
		fdlProfile.top  = new FormAttachment(wDescription, margin);
		wlProfile.setLayoutData(fdlProfile);
	
		// Add the Profile buttons :
		wnProfile = new Button(shell, SWT.PUSH);  wnProfile.setText(BaseMessages.getString(PKG, "System.Button.New")); //$NON-NLS-1$
		weProfile = new Button(shell, SWT.PUSH);  weProfile.setText(BaseMessages.getString(PKG, "System.Button.Edit")); //$NON-NLS-1$
		wdProfile = new Button(shell, SWT.PUSH);  wdProfile.setText(BaseMessages.getString(PKG, "System.Button.Delete")); //$NON-NLS-1$

		// Button positions...
		fddProfile = new FormData();		
		fddProfile.right= new FormAttachment(100, 0);
		fddProfile.top  = new FormAttachment(wDescription, margin);
		wdProfile.setLayoutData(fddProfile);

		fdeProfile = new FormData();		
		fdeProfile.right= new FormAttachment(wdProfile, -margin);
		fdeProfile.top  = new FormAttachment(wDescription, margin);
		weProfile.setLayoutData(fdeProfile);

		fdnProfile = new FormData();		
		fdnProfile.right= new FormAttachment(weProfile, -margin);
		fdnProfile.top  = new FormAttachment(wDescription, margin);
		wnProfile.setLayoutData(fdnProfile);

		wProfile=new CCombo(shell, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER);
 		props.setLook(wProfile);
		fdProfile=new FormData();
		fdProfile.left = new FormAttachment(middle, 0);
		fdProfile.right= new FormAttachment(wnProfile, -margin);
		fdProfile.top  = new FormAttachment(wDescription, margin);
		wProfile.setLayoutData(fdProfile);
        
        // If the repository user is not an administrator, changing the users' profile is not allowed...
        //
        if (!repository.getUserInfo().isAdministrator() || userinfo.isAdministrator())
        {
            wlProfile.setEnabled(false);
            wProfile.setEnabled(false);
            wnProfile.setEnabled(false);
            weProfile.setEnabled(false);
            wdProfile.setEnabled(false);
        }

		// Buttons
		wOK     = new Button(shell, SWT.PUSH); 
		wOK.setText(BaseMessages.getString(PKG, "System.Button.OK")); //$NON-NLS-1$
		wCancel = new Button(shell, SWT.PUSH); 
		wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel")); //$NON-NLS-1$
		
		FormData fdOK        = new FormData();
		FormData fdCancel    = new FormData();

		fdOK.left    = new FormAttachment(45, 0); 
		fdOK.top  = new FormAttachment(wProfile, 30);
		wOK.setLayoutData(fdOK);

		fdCancel.left    = new FormAttachment(wOK, margin); 
		fdCancel.top     = new FormAttachment(wProfile, 30);
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

		// Add profiles to Combo box...
		fillProfiles();
		
		if (userinfo.getProfile()!=null) wProfile.setText( userinfo.getProfile().getName() );
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
			String profname = wProfile.getText();
			
			ObjectId idProfile = securityManager.getProfileID(profname);
			ProfileMeta profinfo = securityManager.loadProfileMeta(idProfile);
			userinfo.setProfile( profinfo);
            
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
	
	public void fillProfiles()
	{
		try
		{
			wProfile.removeAll();
			String prof[] = securityManager.getProfiles();
			for (int i=0;i<prof.length;i++)
			{
				wProfile.add(prof[i]);
			}
		}
		catch(KettleException e)
		{
			MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
			mb.setMessage(BaseMessages.getString(PKG, "UserDialog.Dialog.ErrorRetrievingProfiles.Message")+Const.CR+e.getMessage()); //$NON-NLS-1$
			mb.setText(BaseMessages.getString(PKG, "UserDialog.Dialog.ErrorRetrievingProfiles.Title")); //$NON-NLS-1$
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