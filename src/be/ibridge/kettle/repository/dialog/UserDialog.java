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

 
/*
 * Created on 18-mei-2003
 *
 */

package be.ibridge.kettle.repository.dialog;
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

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.Props;
import be.ibridge.kettle.core.WindowProperty;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.repository.ProfileMeta;
import be.ibridge.kettle.repository.Repository;
import be.ibridge.kettle.repository.UserInfo;


public class UserDialog extends Dialog 
{
	private Shell     shell;
	private Label     wlLogin, wlPassword, wlUsername, wlDescription;
	private Text      wLogin, wPassword, wUsername, wDescription;

	private Label        wlProfile;
	private Button       wnProfile, weProfile, wdProfile;
	private CCombo       wProfile;
	private FormData     fdlProfile, fdProfile, fdnProfile, fdeProfile, fddProfile;
	
	private Button    wOK, wCancel;
	
	private Props    props;
	private Repository rep;
	private UserInfo userinfo;

	/** This dialog grabs a UserMeta structure, valid for the specified repository.*/
	public UserDialog(Shell par, int style, LogWriter lg, Props pr, Repository rep, UserInfo ui)
	{
		super(par, style);
		props=pr;
		this.rep = rep;
		userinfo=ui;
	}
	
	public UserInfo open() 
	{
		Shell parent = getParent();
		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN );
 		props.setLook(shell);
		
		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		FormLayout formLayout = new FormLayout ();
		formLayout.marginWidth  = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;
		
		shell.setText("User information");
		shell.setLayout (formLayout);
 		
		// Username
		wlLogin = new Label(shell, SWT.RIGHT ); 
		wlLogin.setText("Login: "); 
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
		wlPassword.setText("Password: "); 
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
		wlUsername.setText("Full name: "); 
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
		wlDescription.setText("Description: "); 
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
		wlProfile.setText("Profile ");
 		props.setLook(wlProfile);
		fdlProfile=new FormData();
		fdlProfile.left = new FormAttachment(0, 0);
		fdlProfile.right= new FormAttachment(middle, -margin);
		fdlProfile.top  = new FormAttachment(wDescription, margin);
		wlProfile.setLayoutData(fdlProfile);
	
		// Add the Profile buttons :
		wnProfile = new Button(shell, SWT.PUSH);  wnProfile.setText("New");
		weProfile = new Button(shell, SWT.PUSH);  weProfile.setText("Edit");
		wdProfile = new Button(shell, SWT.PUSH);  wdProfile.setText("Delete");

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

		// Buttons
		wOK     = new Button(shell, SWT.PUSH); 
		wOK.setText(" &OK ");
		wCancel = new Button(shell, SWT.PUSH); 
		wCancel.setText(" &Cancel ");
		
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

		WindowProperty winprop = props.getScreen(shell.getText());
		if (winprop!=null) winprop.setShell(shell); else shell.pack();
		
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
		if (userinfo.getName()!=null) wUsername.setText(userinfo.getName());
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
			userinfo.setLogin(wLogin.getText());
			userinfo.setPassword(wPassword.getText());
			userinfo.setName(wUsername.getText());
			userinfo.setDescription(wDescription.getText());
			String profname = wProfile.getText();
			
			long idProfile = rep.getProfileID(profname);
			ProfileMeta profinfo = new ProfileMeta(rep, idProfile);
			userinfo.setProfile( profinfo);
            
            userinfo.saveRep(rep);
            rep.commit();
	
			dispose();
		}
		catch(KettleException e)
		{
			MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
			mb.setMessage("Sorry, an error occurred: "+e.getMessage());
			mb.setText("ERROR");
			mb.open(); 
		}
	}
	
	public void fillProfiles()
	{
		try
		{
			wProfile.removeAll();
			String prof[] = rep.getProfiles();
			for (int i=0;i<prof.length;i++)
			{
				wProfile.add(prof[i]);
			}
		}
		catch(KettleException e)
		{
			MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
			mb.setMessage("I couldn't retrieve the available profiles from the repository: "+Const.CR+e.getMessage());
			mb.setText("ERROR");
			mb.open();
		}
	}
}