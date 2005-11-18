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

 
package be.ibridge.kettle.repository.dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Font;
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

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.Props;
import be.ibridge.kettle.core.WindowProperty;
import be.ibridge.kettle.core.exception.KettleDatabaseException;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.repository.PermissionMeta;
import be.ibridge.kettle.repository.RepositoriesMeta;
import be.ibridge.kettle.repository.Repository;
import be.ibridge.kettle.repository.RepositoryMeta;
import be.ibridge.kettle.repository.UserInfo;
import be.ibridge.kettle.trans.StepLoader;
import be.ibridge.kettle.trans.step.BaseStepDialog;

/**
 * This dialog allows you to select, create or update a repository and log in to it.
 * 
 * @author Matt
 * @since  19-jun-2003
 */
public class RepositoriesDialog
{
	private LogWriter    log;

	private Label        wlKettle;
	private FormData     fdlKettle;
	
	private Label        wlRepository;
	private Button       wnRepository, weRepository, wdRepository;
	private CCombo       wRepository;
	private FormData     fdlRepository, fdRepository, fdnRepository, fdeRepository, fddRepository;

	private Label        wlUsername;
	private CCombo       wUsername;
	private FormData     fdlUsername, fdUsername;

	private Label        wlPassword;
	private Text         wPassword;
	private FormData     fdlPassword, fdPassword;

	private Button wOK, wNorep, wCancel;
	private Listener lsOK, lsNorep, lsCancel;
	
	private SelectionListener lsDef;
	private KeyListener       lsKey, lsJmp;

	private   Display       display;
	private   Shell         shell;
	private   Props         props;
	
	private RepositoriesMeta input;
	private RepositoryMeta   repinfo;
	private UserInfo         userinfo;
	private String           prefRepositoryName;
	private boolean          cancelled;
	private String   		 toolName;
	
	private int toolsPermissions[];
	private StepLoader steploader;

    /** @deprecated */
    public RepositoriesDialog(Display disp, int style, LogWriter l, Props pr, int perm[], String toolName)
    {
        this(disp, style, perm, toolName);
    }

	public RepositoriesDialog(Display disp, int style, int perm[], String toolName)
	{
		display = disp;
		toolsPermissions = perm;
		steploader = StepLoader.getInstance();
		this.toolName = toolName;
		
		shell = new Shell(disp, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN );
		shell.setText("Kettle repositories");

		log=LogWriter.getInstance();
		props=Props.getInstance();
		input=new RepositoriesMeta(log);
		repinfo=null;
		userinfo=null;
		cancelled = false;
			
		input.readData();
	}
	
	public void setRepositoryName(String repname)
	{
		prefRepositoryName = repname;
	}

	public boolean open()
	{
		props.setLook(shell);

		FormLayout formLayout = new FormLayout();
		formLayout.marginWidth  = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText("Select a repository");

		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		// Kettle welcome
		wlKettle = new Label(shell, SWT.CENTER);
		wlKettle.setText("Welcome to Kettle - "+toolName);
		props.setLook(wlKettle);
        final Font f = new Font(shell.getDisplay(), "Arial", 18, SWT.BOLD | SWT.ITALIC);
        wlKettle.addDisposeListener(new DisposeListener() { public void widgetDisposed(DisposeEvent e) {  f.dispose(); } });
        wlKettle.setFont(f);
        fdlKettle=new FormData();
		fdlKettle.left = new FormAttachment(0, 0);
		fdlKettle.right= new FormAttachment(100, 0);
		fdlKettle.top  = new FormAttachment(0, margin);
		wlKettle.setLayoutData(fdlKettle);

		// Repository selector
		wlRepository=new Label(shell, SWT.RIGHT);
		wlRepository.setText("Repository ");
 		props.setLook(wlRepository);
		fdlRepository=new FormData();
		fdlRepository.left = new FormAttachment(0, 0);
		fdlRepository.right= new FormAttachment(middle, -margin);
		fdlRepository.top  = new FormAttachment(wlKettle, 20);
		wlRepository.setLayoutData(fdlRepository);
		wRepository=new CCombo(shell, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER);

		// Add the Repository buttons :
		wnRepository = new Button(shell, SWT.PUSH);  wnRepository.setText("New");
		weRepository = new Button(shell, SWT.PUSH);  weRepository.setText("Edit");
		wdRepository = new Button(shell, SWT.PUSH);  wdRepository.setText("Delete");

		// Button positions...
		fddRepository = new FormData();		
		fddRepository.right= new FormAttachment(100, 0);
		fddRepository.top  = new FormAttachment(wlKettle, 20);
		wdRepository.setLayoutData(fddRepository);

		fdeRepository = new FormData();		
		fdeRepository.right = new FormAttachment(wdRepository, -margin);
		fdeRepository.top  = new FormAttachment(wlKettle, 20);
		weRepository.setLayoutData(fdeRepository);

		fdnRepository = new FormData();		
		fdnRepository.right= new FormAttachment(weRepository, -margin);
		fdnRepository.top  = new FormAttachment(wlKettle, 20);
		wnRepository.setLayoutData(fdnRepository);

 		props.setLook(wRepository);
		fdRepository=new FormData();
		fdRepository.left = new FormAttachment(middle, 0);
		fdRepository.right= new FormAttachment(wnRepository, -margin);
		fdRepository.top  = new FormAttachment(wlKettle, 20);
		wRepository.setLayoutData(fdRepository);

		// Add the listeners
		// New repository
		wnRepository.addSelectionListener(new SelectionAdapter() 
			{
				public void widgetSelected(SelectionEvent arg0) 
				{
					RepositoryMeta ri = new RepositoryMeta();
					RepositoryDialog dd = new RepositoryDialog(shell, SWT.APPLICATION_MODAL, log, props, ri, input, steploader);
					if (dd.open()!=null)
					{
						input.addRepository(ri);
						fillRepositories();
						int idx = input.indexOfRepository(ri);
						wRepository.select(idx);
					}
				}
			}
		);

		// Edit repository
		weRepository.addSelectionListener(new SelectionAdapter() 
			{
				public void widgetSelected(SelectionEvent arg0) 
				{
					RepositoryMeta ri = input.searchRepository(wRepository.getText());
					if (ri!=null)
					{
						RepositoryDialog dd = new RepositoryDialog(shell, SWT.APPLICATION_MODAL, log, props, ri, input, steploader);
						if (dd.open()!=null)
						{
							fillRepositories();
							int idx = input.indexOfRepository(ri);
							wRepository.select(idx);
						}
					}
				}
			}
		);

		// Delete connection
		wdRepository.addSelectionListener(new SelectionAdapter() 
			{
				public void widgetSelected(SelectionEvent arg0) 
				{
					RepositoryMeta ri = input.searchRepository(wRepository.getText());
					if (ri!=null)
					{
						int idx = input.indexOfRepository(ri);
						input.removeRepository(idx);
						fillRepositories();
					}
				}
			}
		);

        // TODO: keep the last users in the Combo box, store in Props somewhere...
		// Username
		wlUsername = new Label(shell, SWT.RIGHT ); 
		wlUsername.setText("Login: "); 
 		props.setLook(wlUsername);
		fdlUsername = new FormData();
		fdlUsername.left = new FormAttachment(0,0); 
		fdlUsername.right= new FormAttachment(middle, -margin);
		fdlUsername.top  = new FormAttachment(wdRepository, margin);
		wlUsername.setLayoutData(fdlUsername);
		wUsername = new CCombo(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
 		props.setLook(wUsername);
		fdUsername = new FormData();
		fdUsername.left = new FormAttachment(middle, 0); 
		fdUsername.right= new FormAttachment(100, 0);
		fdUsername.top  = new FormAttachment(wdRepository, margin);
		wUsername.setLayoutData(fdUsername);

		// Password
		wlPassword = new Label(shell, SWT.RIGHT ); 
		wlPassword.setText("Password: "); 
 		props.setLook(wlPassword);
		fdlPassword = new FormData();
		fdlPassword.left   = new FormAttachment(0,0);
		fdlPassword.right  = new FormAttachment(middle, -margin);
		fdlPassword.top    = new FormAttachment(wUsername, margin);
		wlPassword.setLayoutData(fdlPassword);
		wPassword = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
 		props.setLook(wPassword);
		wPassword.setEchoChar('*');
		fdPassword = new FormData();
		fdPassword.left   = new FormAttachment(middle, 0); 
		fdPassword.right  = new FormAttachment(100, 0);
		fdPassword.top    = new FormAttachment(wUsername, margin);
		wPassword.setLayoutData(fdPassword);

		wOK=new Button(shell, SWT.PUSH);
		wOK.setText(" &OK ");
		wNorep=new Button(shell, SWT.PUSH);
		wNorep.setText(" &No repository ");
		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText(" &Cancel ");

		Button[] buttons = new Button[] { wOK, wNorep, wCancel };
		BaseStepDialog.positionBottomButtons(shell, buttons, margin, wPassword);

		// Add listeners
		lsOK       = new Listener() { public void handleEvent(Event e) { ok();     } };
		lsNorep    = new Listener() { public void handleEvent(Event e) { norep(); } };
		lsCancel   = new Listener() { public void handleEvent(Event e) { cancel(); } };
		
		wOK.addListener    (SWT.Selection, lsOK    );
		wNorep.addListener (SWT.Selection, lsNorep);
		wCancel.addListener(SWT.Selection, lsCancel);

		// Detect X or ALT-F4 or something that kills this window...
		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { cancel(); } } );
		// Clean up used resources!

		
		lsDef = new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } };
		lsKey = new KeyAdapter() { public void keyPressed(KeyEvent e) 
				{ 
					if (e.character == SWT.CR) ok(); 
				} 
			};
		lsJmp = new KeyAdapter() { public void keyPressed(KeyEvent e) 
				{ 
					if (e.character == SWT.CR)
					{
						if (wPassword.getText().length()!=0) ok();
						else 
						{
							wPassword.setFocus();
							wPassword.selectAll();
						}
					}
				} 
			};

		wRepository.addKeyListener(lsKey);
		wUsername.addKeyListener( lsJmp );
		wPassword.addSelectionListener( lsDef );

		getData();
		
		WindowProperty winprop = props.getScreen(shell.getText());
		if (winprop!=null) winprop.setShell(shell); else shell.pack();
	
		shell.open();
		while (!shell.isDisposed())
		{
				if (!display.readAndDispatch()) display.sleep();
		}
		return repinfo!=null;
	}

	public void dispose()
	{
		props.setScreen(new WindowProperty(shell));
		shell.dispose();
	}
	
	/**
	 * Copy information from the meta-data input to the dialog fields.
	 */ 
	public void getData()
	{	
		fillRepositories();
	
		String repname = props.getLastRepository();
		if (repname!=null)
		{
			int idx =wRepository.indexOf(repname); 
			if (idx>=0)
			{
				wRepository.select(idx);
				wRepository.setFocus();
				
				// fillUsernames();
				
				String username = props.getLastRepositoryLogin();
				if (username!=null)
				{
					wUsername.setText(username);
					wPassword.setFocus();
				}
			}
		}

		//Do we have a preferred repository name to select
		if (prefRepositoryName!=null)
		{
			int idx = wRepository.indexOf(prefRepositoryName);
			if (idx>=0) wRepository.select(idx);
		}

	}
	
	private void norep()
	{
		repinfo = null;
		dispose();
	}

	private void cancel()
	{
		repinfo = null;
		cancelled = true;
		dispose();
	}	
		
	private void ok()
	{
		if (wRepository.getItemCount()!=0)
		{
			int idx=wRepository.getSelectionIndex();
			
			repinfo = input.getRepository(idx);
			
			// OK, now try the username and password
			Repository rep = new Repository(log, repinfo, userinfo);
			if (rep.connect(getClass().getName()))
			{
				try
				{
					userinfo = new UserInfo(rep, wUsername.getText(), wPassword.getText());
					props.setLastRepository(repinfo.getName());
					props.setLastRepositoryLogin(wUsername.getText());
				}
				catch(KettleException e)
				{
					userinfo=null;
					repinfo=null;
				}
				finally
				{
					rep.disconnect();
				}
			}
			else
			{
				MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
				mb.setMessage("I couldn't connect to this repository!"+Const.CR+"Please check the settings in the repository connection.");
				mb.setText("Sorry");
				mb.open(); 
				
				return;
			}
		}

		input.writeData(); // Save changes to disk!
		
		if (userinfo==null)
		{
			MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
			mb.setMessage("Incorrect username or password!");
			mb.setText("ERROR");
			mb.open(); 
		}
		else
		{
			// Check the permissions of the user
			boolean ok = true;
			String mess = "";
			for (int i=0;i<toolsPermissions.length;i++)
			{
				switch(toolsPermissions[i])
				{
				case PermissionMeta.TYPE_PERMISSION_TRANSFORMATION:
					ok = ok && userinfo.useTransformations();
					mess += mess.length()>0?", ":""; 
					mess+="Spoon"; 
					break;
				case PermissionMeta.TYPE_PERMISSION_SCHEMA:
					ok = ok && userinfo.useSchemas(); 
					mess += mess.length()>0?", ":""; 
					mess+="Menu"; 
					break;
				case PermissionMeta.TYPE_PERMISSION_JOB:
					ok = ok && userinfo.useJobs(); 
					mess += mess.length()>0?", ":""; 
					mess+="Chef"; 
					break;
				default: break;
				}
			}
					
			// Sorry, you can't use all these tools...
			if (!ok)
			{
				int idx = mess.lastIndexOf(',');
				if (idx>0) mess = mess.substring(0, idx) + "and"+ mess.substring(idx+1);
				MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
				mb.setMessage("Sorry, you don't have permissions to run "+mess);
				mb.setText("ERROR");
				mb.open(); 

				userinfo=null;
				repinfo=null;
			}
			else
			{
				dispose();
			}
		}
	}
	
	public void fillRepositories()
	{
		wRepository.removeAll();
		// Fill in the available repositories...
		for (int i=0;i<input.nrRepositories();i++) wRepository.add( input.getRepository(i).getName() );
	}
	
	public void fillUsernames()
	{
		String repname = wRepository.getText();
		RepositoryMeta ri = input.findRepository(repname);
		if (ri!=null)
		{
			Repository rep = new Repository(log, ri, userinfo);
			if (rep.connect(getClass().getName()))
			{
				wUsername.removeAll();
				try
				{
					String logins[] = rep.getUserLogins();
					if (logins!=null)
					{
						for (int i=0;i<logins.length;i++) wUsername.add(logins[i]);
					}
				}
				catch(KettleDatabaseException dbe)
				{
					MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
					mb.setMessage("I was unable to get the list of users from the repository connection.");
					mb.setText("ERROR");
					mb.open(); 
				}
				rep.disconnect();
			}
		}
	}
	
	public RepositoryMeta getRepository()
	{
		return repinfo;
	}
	
	public UserInfo getUser()
	{
		return userinfo;
	}
	
	public boolean isCancelled()
	{
		return cancelled;
	}
	
	public Shell getShell()
	{
		return shell;
	}
}
