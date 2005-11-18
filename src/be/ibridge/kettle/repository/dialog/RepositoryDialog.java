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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.Props;
import be.ibridge.kettle.core.WindowProperty;
import be.ibridge.kettle.core.database.DatabaseMeta;
import be.ibridge.kettle.core.dialog.DatabaseDialog;
import be.ibridge.kettle.core.dialog.EnterPasswordDialog;
import be.ibridge.kettle.core.dialog.ErrorDialog;
import be.ibridge.kettle.core.exception.KettleDatabaseException;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.repository.RepositoriesMeta;
import be.ibridge.kettle.repository.Repository;
import be.ibridge.kettle.repository.RepositoryMeta;
import be.ibridge.kettle.repository.UserInfo;
import be.ibridge.kettle.trans.StepLoader;


/**
 * This dialog is used to edit/enter the name and description of a repository.
 * You can also use this dialog to create, upgrade or remove a repository.
 * 
 * @author Matt
 * @since 19-jun-2003
 *
 */

public class RepositoryDialog
{
	private LogWriter    log;

	private Label        wlConnection;
	private Button       wnConnection, weConnection, wdConnection;
	private CCombo       wConnection;
	private FormData     fdlConnection, fdConnection, fdnConnection, fdeConnection, fddConnection;

	private Label        wlName;
	private Text         wName;
	private FormData     fdlName, fdName;

	private Label        wlDescription;
	private Text         wDescription;
	private FormData     fdlDescription, fdDescription;

	private Button wOK, wCreate, wDrop, wCancel;
	private FormData fdOK, fdCreate, fdDrop, fdCancel;
	private Listener lsOK, lsCreate, lsDrop, lsCancel;

	private Display       display;
	private Shell         shell, parent;
	private Props         props;
	
	private RepositoryMeta   input;
	private RepositoriesMeta repositories;
	private StepLoader       steploader;
	
	public RepositoryDialog(Shell par, int style, LogWriter l, Props pr, RepositoryMeta in, RepositoriesMeta rep, StepLoader steploader)
	{
		parent  = par;
		display = parent.getDisplay();
		this.steploader = steploader;
		shell = new Shell(display, style | SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN );
		shell.setText("Kettle repository");

		log=l;
		props=pr;
		input=in;
		repositories=rep;
		
		//System.out.println("input.connection = "+input.getConnection());
	}

	public RepositoryMeta open()
	{
		props.setLook(shell);

		FormLayout formLayout = new FormLayout ();
		formLayout.marginWidth  = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText("Repository information ");
		
		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		// Connection line
		wlConnection=new Label(shell, SWT.RIGHT);
		wlConnection.setText("Select database connection ");
 		props.setLook(wlConnection);
		fdlConnection=new FormData();
		fdlConnection.left = new FormAttachment(0, 0);
		fdlConnection.right= new FormAttachment(middle, -margin);
		fdlConnection.top  = new FormAttachment(0, margin);
		wlConnection.setLayoutData(fdlConnection);

		// Add the connection buttons :
		wnConnection = new Button(shell, SWT.PUSH);  wnConnection.setText("New");
		weConnection = new Button(shell, SWT.PUSH);  weConnection.setText("Edit");
		wdConnection = new Button(shell, SWT.PUSH);  wdConnection.setText("Delete");

		// Button positions...
		fddConnection = new FormData();		
		fddConnection.right= new FormAttachment(100, 0);
		fddConnection.top  = new FormAttachment(0, margin);
		wdConnection.setLayoutData(fddConnection);

		fdeConnection = new FormData();		
		fdeConnection.right= new FormAttachment(wdConnection, -margin);
		fdeConnection.top  = new FormAttachment(0, margin);
		weConnection.setLayoutData(fdeConnection);

		fdnConnection = new FormData();		
		fdnConnection.right = new FormAttachment(weConnection, -margin);
		fdnConnection.top   = new FormAttachment(0, margin);
		wnConnection.setLayoutData(fdnConnection);

		wConnection=new CCombo(shell, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER);
 		props.setLook(wConnection);
		fdConnection=new FormData();
		fdConnection.left = new FormAttachment(middle, 0);
		fdConnection.top  = new FormAttachment(0, margin);
		fdConnection.right= new FormAttachment(wnConnection, -margin);
		wConnection.setLayoutData(fdConnection);

		fillConnections();		
		

		
		// Add the listeners
		// New connection
		wnConnection.addSelectionListener(new SelectionAdapter() 
			{
				public void widgetSelected(SelectionEvent arg0) 
				{
					DatabaseMeta dbinfo = new DatabaseMeta();
					DatabaseDialog dd = new DatabaseDialog(shell, SWT.APPLICATION_MODAL, log, dbinfo, props);
					if (dd.open()!=null)
					{
						repositories.addDatabase(dbinfo);
						fillConnections();
						
						int idx = repositories.indexOfDatabase(dbinfo);
						wConnection.select(idx);
						
					}
				}
			}
		);
	
		// Edit connection
		weConnection.addSelectionListener(new SelectionAdapter() 
			{
				public void widgetSelected(SelectionEvent arg0) 
				{
					DatabaseMeta dbinfo = repositories.searchDatabase(wConnection.getText());
					if (dbinfo!=null)
					{
						DatabaseDialog dd = new DatabaseDialog(shell, SWT.APPLICATION_MODAL, log, dbinfo, props);
						if (dd.open()!=null)
						{
							fillConnections();
							int idx = repositories.indexOfDatabase(dbinfo);
							wConnection.select(idx);
						}
					}
				}
			}
		);
	
		// Delete connection
		wdConnection.addSelectionListener(new SelectionAdapter() 
			{
				public void widgetSelected(SelectionEvent arg0) 
				{
					DatabaseMeta dbinfo = repositories.searchDatabase(wConnection.getText());
					if (dbinfo!=null)
					{
						int idx = repositories.indexOfDatabase(dbinfo);
						repositories.removeDatabase(idx);
						fillConnections();
					}
				}
			}
		);
		
		// Name line
		wlName=new Label(shell, SWT.RIGHT);
		wlName.setText("Name ");
 		props.setLook(wlName);
		fdlName=new FormData();
		fdlName.left = new FormAttachment(0, 0);
		fdlName.top  = new FormAttachment(wnConnection, margin*2);
		fdlName.right= new FormAttachment(middle, -margin);
		wlName.setLayoutData(fdlName);
		wName=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wName);
		fdName=new FormData();
		fdName.left = new FormAttachment(middle, 0);
		fdName.top  = new FormAttachment(wnConnection, margin*2);
		fdName.right= new FormAttachment(100, 0);
		wName.setLayoutData(fdName);

		// Description line
		wlDescription=new Label(shell, SWT.RIGHT);
		wlDescription.setText("Description ");
 		props.setLook(wlDescription);
		fdlDescription=new FormData();
		fdlDescription.left = new FormAttachment(0, 0);
		fdlDescription.top  = new FormAttachment(wName, margin);
		fdlDescription.right= new FormAttachment(middle, -margin);
		wlDescription.setLayoutData(fdlDescription);
		wDescription=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wDescription);
		fdDescription=new FormData();
		fdDescription.left = new FormAttachment(middle, 0);
		fdDescription.top  = new FormAttachment(wName, margin);
		fdDescription.right= new FormAttachment(100, 0);
		wDescription.setLayoutData(fdDescription);


		

		wOK=new Button(shell, SWT.PUSH);
		wOK.setText(" &OK ");
		fdOK=new FormData();
		fdOK.left   = new FormAttachment(30, 0);
		fdOK.top    = new FormAttachment(wDescription, margin*3);
		wOK.setLayoutData(fdOK);
		lsOK       = new Listener() { public void handleEvent(Event e) { ok();     } };
		wOK.addListener    (SWT.Selection, lsOK    );

		wCreate=new Button(shell, SWT.PUSH);
		wCreate.setText(" &Create or Upgrade ");
		fdCreate=new FormData();
		fdCreate.left   = new FormAttachment(wOK, 30);
		fdCreate.top    = new FormAttachment(wDescription, margin*3);
		wCreate.setLayoutData(fdCreate);
		lsCreate   = new Listener() { public void handleEvent(Event e) { create(steploader); } };
		wCreate.addListener(SWT.Selection, lsCreate);

		wDrop=new Button(shell, SWT.PUSH);
		wDrop.setText(" &Remove ");
		fdDrop=new FormData();
		fdDrop.left   = new FormAttachment(wCreate, 30);
		fdDrop.top    = new FormAttachment(wDescription, margin*3);
		wDrop.setLayoutData(fdDrop);
		lsDrop     = new Listener() { public void handleEvent(Event e) { drop();   } };
		wDrop.addListener  (SWT.Selection, lsDrop  );

		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText(" &Cancel ");
		fdCancel=new FormData();
		fdCancel.left   = new FormAttachment(wDrop, 30);
		fdCancel.top    = new FormAttachment(wDescription, margin*3);
		wCancel.setLayoutData(fdCancel);
		lsCancel   = new Listener() { public void handleEvent(Event e) { cancel(); } };
		wCancel.addListener(SWT.Selection, lsCancel);
		
		// Detect X or ALT-F4 or something that kills this window...
		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { cancel(); } } );

		getData();

		WindowProperty winprop = props.getScreen(shell.getText());
		if (winprop!=null) winprop.setShell(shell); else shell.pack();
		
		shell.open();
		while (!shell.isDisposed())
		{
				if (!display.readAndDispatch()) display.sleep();
		}
		return input;
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
		if (input.getName()!=null) wName.setText(input.getName());
		if (input.getDescription()!=null) wDescription.setText(input.getDescription());
		if (input.getConnection()!=null) wConnection.setText(input.getConnection().getName());	
	}
	
	private void cancel()
	{
		input = null;
		dispose();
	}
	
	private void getInfo(RepositoryMeta info)
	{
		info.setName(wName.getText());
		info.setDescription(wDescription.getText());
		
		int idx = wConnection.getSelectionIndex();
		if (idx>=0)
		{
			DatabaseMeta dbinfo = repositories.getDatabase(idx);
			info.setConnection(dbinfo);
		}
		else
		{
			info.setConnection(null);
		}
	}
	
	private void ok()
	{
		getInfo(input);
		dispose();
	}
	
	private void fillConnections()
	{
		wConnection.removeAll();
		
		// Fill in the available connections...
		for (int i=0;i<repositories.nrDatabases();i++) wConnection.add( repositories.getDatabase(i).getName() );
	}
	
	private void create(StepLoader steploader)
	{
		System.out.println("Loading repository info...");
		
		RepositoryMeta repinfo = new RepositoryMeta();
		getInfo(repinfo);
		
		if (repinfo.getConnection()!=null)
		{
			System.out.println("Allocating repository...");
			Repository rep = new Repository(log, repinfo, null);
	
			System.out.println("Connecting to database for repository creation...");
			if (rep.connect(true, false, getClass().getName()))
			{
				boolean upgrade=false;
				String  cu = "create";
				
				try
				{
					upgrade = rep.getDatabase().checkTableExists("R_USER");
					if (upgrade) cu="upgrade";
				}
				catch(KettleDatabaseException dbe)
				{
				    rep.rollback();
				    // Don't show the error anymore, just go ahead and create the damw thing!
					// ErrorDialog ed = new ErrorDialog(shell, props, "ERROR", "An unexpected error occured trying to check the existence of table R_USER in the repository."+Const.CR+"We consider the table to be non-existent and we will try to create the repository. ", dbe);
				}
				
				MessageBox qmb = new MessageBox(shell, SWT.ICON_WARNING | SWT.YES | SWT.NO);
				qmb.setMessage("Are you sure you want to "+cu+" the repository on the specified database connection?");
				qmb.setText("OK");
				int answer = qmb.open();	
				
				if (answer == SWT.YES)
				{
					boolean goAhead = !upgrade;
					
					if (!goAhead)
					{
						EnterPasswordDialog etd = new EnterPasswordDialog(shell, props, "Confirm the password", "Give the password of the admin user!", "");
						String pwd = etd.open();
						if (pwd!=null)
						{
							try
							{
								// OK, what's the admin password?
								UserInfo ui = new UserInfo(rep, "admin");
								
								if (pwd.equalsIgnoreCase( ui.getPassword() ) )
								{
									goAhead=true;
								}
								else
								{
									MessageBox mb = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
									mb.setMessage("Kettle couldn't upgrade the repository tables on this connection.  The password was not correct.");
									mb.setText("ERROR");
									mb.open();
								}
							}
							catch(KettleException e)
							{
								new ErrorDialog(shell, props, "ERROR", "Kettle couldn't verify the user: ", e);
							}
						}
					}
					
					if (goAhead)
					{
						System.out.println("Trying to "+cu+" the Kettle repository tables...");
						UpgradeRepositoryProgressDialog urpd = new UpgradeRepositoryProgressDialog(log, props, shell, rep, upgrade);
						if (urpd.open())
						{
							MessageBox mb = new MessageBox(shell, SWT.ICON_INFORMATION | SWT.OK);
							mb.setMessage("Kettle "+cu+"d the repository on the specified connection.");
							mb.setText("OK");
							mb.open();
						}
					}
				}

				rep.disconnect();
			}
			else
			{
				MessageBox mb = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
				mb.setMessage("Kettle couldn't connect to the database to create or upgrade the repository."+Const.CR);
				mb.setText("ERROR");
				mb.open();
			}
		}
		else
		{
			MessageBox mb = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
			mb.setMessage("Please, first select or create a valid repository database connection!");
			mb.setText("ERROR");
			mb.open();
		}
	}

	private void drop()
	{
		RepositoryMeta repinfo = new RepositoryMeta();
		getInfo(repinfo);
		
		Repository rep = new Repository(log, repinfo, null);
		if (rep.connect(getClass().getName()))
		{
			MessageBox qmb = new MessageBox(shell, SWT.ICON_WARNING | SWT.YES | SWT.NO);
			qmb.setMessage("Are you sure you want to remove all repository tables on this connection?");
			qmb.setText("OK");
			int answer = qmb.open();
			
			if (answer == SWT.YES)
			{
				EnterPasswordDialog etd = new EnterPasswordDialog(shell, props, "Confirm the password", "Give the password of the admin user!", "");
				String pwd = etd.open();
				if (pwd!=null)
				{
					try
					{
						// OK, what's the admin password?
						UserInfo ui = new UserInfo(rep, "admin");
						
						if (pwd.equalsIgnoreCase( ui.getPassword() ) )
						{		
							try
							{
								rep.dropRepositorySchema();
								
								MessageBox mb = new MessageBox(shell, SWT.ICON_INFORMATION | SWT.OK);
								mb.setMessage("Kettle removed all repository tables on this connection.");
								mb.setText("OK");
								mb.open();
							}
							catch(KettleDatabaseException dbe)
							{
								MessageBox mb = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
								mb.setMessage("Kettle couldn't remove the repository tables on this connection.:"+Const.CR+dbe.getMessage());
								mb.setText("ERROR");
								mb.open();
							}
						}
						else
						{
							MessageBox mb = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
							mb.setMessage("Kettle couldn't remove the repository tables on this connection.  The password was not correct.");
							mb.setText("ERROR");
							mb.open();
						}
					}
					catch(KettleException e)
					{
						new ErrorDialog(shell, props, "ERROR", "Kettle couldn't verify the administrator user!", e);
					}
				}
			}
			rep.disconnect();
		}
		else
		{
			MessageBox mb = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
			mb.setMessage("Kettle didn't find a repository on this connection.");
			mb.setText("ERROR");
			mb.open();
		}
	}
}
