package org.pentaho.di.cluster.dialog;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.dialog.EnterTextDialog;
import org.pentaho.di.trans.step.BaseStepDialog;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.gui.WindowProperty;
import org.pentaho.di.core.dialog.ErrorDialog;
import org.pentaho.di.core.gui.GUIResource;
import org.pentaho.di.core.widget.TextVar;
import org.pentaho.di.www.AddTransServlet;




/**
 * 
 * Dialog that allows you to edit the settings of the security service connection
 * 
 * @see SlaveServer
 * @author Matt
 * @since 31-10-2006
 *
 */

public class SlaveServerDialog extends Dialog 
{
	private SlaveServer slaveServer;
	
	private CTabFolder   wTabFolder;
	private FormData     fdTabFolder;
	
	private CTabItem     wServiceTab, wProxyTab;

	private Composite    wServiceComp, wProxyComp;
	private FormData     fdServiceComp, fdProxyComp;

	private Shell     shell;

	// Service
	private Text     wName;
	private TextVar  wHostname,  wPort, wUsername,  wPassword;
	private Button   wMaster;

	// Proxy
	private TextVar   wProxyHost, wProxyPort,  wNonProxyHosts;

	private Button    wOK, wCancel;
	
	private ModifyListener lsMod;

	private Props     props;

	private int middle;
	private int margin;

	private SlaveServer originalServer;
	private boolean ok;
    
	public SlaveServerDialog(Shell par, SlaveServer slaveServer)
	{
		super(par, SWT.NONE);
		this.slaveServer=(SlaveServer) slaveServer.clone();
		this.originalServer=slaveServer;
		props=Props.getInstance();
		ok=false;
	}
	
	public boolean open() 
	{
		Shell parent = getParent();
		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN );
		props.setLook(shell);
		shell.setImage((Image) GUIResource.getInstance().getImageConnection());
		
		lsMod = new ModifyListener() 
		{
			public void modifyText(ModifyEvent e) 
			{
				slaveServer.setChanged();
			}
		};

		middle = props.getMiddlePct();
		margin = Const.MARGIN;

		FormLayout formLayout = new FormLayout ();
		formLayout.marginWidth  = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;
		
		shell.setText(Messages.getString("SlaveServerDialog.Shell.Title"));
		shell.setLayout (formLayout);
 		
		// First, add the buttons...
		
		// Buttons
		wOK     = new Button(shell, SWT.PUSH); 
		wOK.setText(" &OK ");

		wCancel = new Button(shell, SWT.PUSH); 
		wCancel.setText(" &Cancel ");

		Button[] buttons = new Button[] { wOK, wCancel };
		BaseStepDialog.positionBottomButtons(shell, buttons, margin, null);
		
		// The rest stays above the buttons...
		
		wTabFolder = new CTabFolder(shell, SWT.BORDER);
		props.setLook(wTabFolder, Props.WIDGET_STYLE_TAB);

		addServiceTab();
		addProxyTab();
        
		fdTabFolder = new FormData();
		fdTabFolder.left  = new FormAttachment(0, 0);
		fdTabFolder.top   = new FormAttachment(0, margin);
		fdTabFolder.right = new FormAttachment(100, 0);
		fdTabFolder.bottom= new FormAttachment(wOK, -margin);
		wTabFolder.setLayoutData(fdTabFolder);

		
		// Add listeners
		wOK.addListener(SWT.Selection, new Listener () { public void handleEvent (Event e) { ok(); } } );
		wCancel.addListener(SWT.Selection, new Listener () { public void handleEvent (Event e) { cancel(); } } );
		
		SelectionAdapter selAdapter=new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } };
		wUsername.addSelectionListener(selAdapter);
		wPassword.addSelectionListener(selAdapter);
		wHostname.addSelectionListener(selAdapter);
		wPort.addSelectionListener(selAdapter);
		wProxyHost.addSelectionListener(selAdapter);
		wProxyPort.addSelectionListener(selAdapter);
		wNonProxyHosts.addSelectionListener(selAdapter);

		// Detect X or ALT-F4 or something that kills this window...
		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { cancel(); } } );
	
		wTabFolder.setSelection(0);
        
		getData();

		BaseStepDialog.setSize(shell);
		
		shell.open();
		Display display = parent.getDisplay();
		while (!shell.isDisposed()) 
		{
			if (!display.readAndDispatch()) display.sleep();
		}
		return ok;
	}
	
	private void addServiceTab()
	{
		//////////////////////////
		// START OF DB TAB   ///
		//////////////////////////
		wServiceTab=new CTabItem(wTabFolder, SWT.NONE);
		wServiceTab.setText("Service");
        
		wServiceComp = new Composite(wTabFolder, SWT.NONE);
		props.setLook(wServiceComp);

		FormLayout GenLayout = new FormLayout();
		GenLayout.marginWidth  = Const.FORM_MARGIN;
		GenLayout.marginHeight = Const.FORM_MARGIN;
		wServiceComp.setLayout(GenLayout);

		// What's the name
		Label wlName = new Label(wServiceComp, SWT.RIGHT); 
		props.setLook(wlName);
		wlName.setText(Messages.getString("SlaveServerDialog.ServerName.Label")); 
		FormData fdlName = new FormData();
		fdlName.top   = new FormAttachment(0, 0);
		fdlName.left  = new FormAttachment(0, 0);  // First one in the left top corner
		fdlName.right = new FormAttachment(middle, -margin);
		wlName.setLayoutData(fdlName);

		wName = new Text(wServiceComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
		props.setLook(wName);
		wName.addModifyListener(lsMod);
		FormData fdName = new FormData();
		fdName.top  = new FormAttachment(0, 0);
		fdName.left = new FormAttachment(middle, 0); // To the right of the label
		fdName.right= new FormAttachment(95, 0);
		wName.setLayoutData(fdName);

		// What's the hostname
		Label wlHostname = new Label(wServiceComp, SWT.RIGHT); 
		props.setLook(wlHostname); 
		wlHostname.setText(Messages.getString("SlaveServerDialog.HostIP.Label"));
		FormData fdlHostname = new FormData();
		fdlHostname.top   = new FormAttachment(wName, margin*2);
		fdlHostname.left  = new FormAttachment(0, 0);  // First one in the left top corner
		fdlHostname.right = new FormAttachment(middle, -margin);
		wlHostname.setLayoutData(fdlHostname);

		wHostname = new TextVar(wServiceComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
		props.setLook(wHostname);
		wHostname.addModifyListener(lsMod);
		FormData fdHostname = new FormData();
		fdHostname.top  = new FormAttachment(wName, margin*2);
		fdHostname.left = new FormAttachment(middle, 0); // To the right of the label
		fdHostname.right= new FormAttachment(95, 0);
		wHostname.setLayoutData(fdHostname);

		// What's the service URL?
		Label wlPort = new Label(wServiceComp, SWT.RIGHT); 
		props.setLook(wlPort);
		wlPort.setText(Messages.getString("SlaveServerDialog.Port.Label"));
		FormData fdlPort = new FormData();
		fdlPort.top   = new FormAttachment(wHostname, margin);
		fdlPort.left  = new FormAttachment(0, 0);  // First one in the left top corner
		fdlPort.right = new FormAttachment(middle, -margin);
		wlPort.setLayoutData(fdlPort);

		wPort = new TextVar(wServiceComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
		props.setLook(wPort);
		wPort.addModifyListener(lsMod);
		FormData fdPort = new FormData();
		fdPort.top  = new FormAttachment(wHostname, margin);
		fdPort.left = new FormAttachment(middle, 0); // To the right of the label
		fdPort.right= new FormAttachment(95, 0);
		wPort.setLayoutData(fdPort);
        
		// Username
		Label wlUsername = new Label(wServiceComp, SWT.RIGHT ); 
		wlUsername.setText(Messages.getString("SlaveServerDialog.UserName.Label")); 
		props.setLook(wlUsername);
		FormData fdlUsername = new FormData();
		fdlUsername.top  = new FormAttachment(wPort, margin);
		fdlUsername.left = new FormAttachment(0,0); 
		fdlUsername.right= new FormAttachment(middle, -margin);
		wlUsername.setLayoutData(fdlUsername);

		wUsername = new TextVar(wServiceComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
		props.setLook(wUsername);
		wUsername.addModifyListener(lsMod);
		FormData fdUsername = new FormData();
		fdUsername.top  = new FormAttachment(wPort, margin);
		fdUsername.left = new FormAttachment(middle, 0); 
		fdUsername.right= new FormAttachment(95, 0);
		wUsername.setLayoutData(fdUsername);

        
		// Password
		Label wlPassword = new Label(wServiceComp, SWT.RIGHT ); 
		wlPassword.setText(Messages.getString("SlaveServerDialog.Password.Label")); 
		props.setLook(wlPassword);
		FormData fdlPassword = new FormData();
		fdlPassword.top  = new FormAttachment(wUsername, margin);
		fdlPassword.left = new FormAttachment(0,0);
		fdlPassword.right= new FormAttachment(middle, -margin);
		wlPassword.setLayoutData(fdlPassword);

		wPassword = new TextVar(wServiceComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
		props.setLook(wPassword);
		wPassword.setEchoChar('*');
		wPassword.addModifyListener(lsMod);
		FormData fdPassword = new FormData();
		fdPassword.top  = new FormAttachment(wUsername, margin);
		fdPassword.left = new FormAttachment(middle, 0); 
		fdPassword.right= new FormAttachment(95, 0);
		wPassword.setLayoutData(fdPassword);

		// Master
		Label wlMaster = new Label(wServiceComp, SWT.RIGHT ); 
		wlMaster.setText(Messages.getString("SlaveServerDialog.IsTheMaster.Label")); 
		props.setLook(wlMaster);
		FormData fdlMaster = new FormData();
		fdlMaster.top  = new FormAttachment(wPassword, margin);
		fdlMaster.left = new FormAttachment(0,0);
		fdlMaster.right= new FormAttachment(middle, -margin);
		wlMaster.setLayoutData(fdlMaster);

		wMaster = new Button(wServiceComp, SWT.CHECK );
		props.setLook(wMaster);
		FormData fdMaster = new FormData();
		fdMaster.top  = new FormAttachment(wPassword, margin);
		fdMaster.left = new FormAttachment(middle, 0); 
		fdMaster.right= new FormAttachment(95, 0);
		wMaster.setLayoutData(fdMaster);

        
		fdServiceComp=new FormData();
		fdServiceComp.left  = new FormAttachment(0, 0);
		fdServiceComp.top   = new FormAttachment(0, 0);
		fdServiceComp.right = new FormAttachment(100, 0);
		fdServiceComp.bottom= new FormAttachment(100, 0);
		wServiceComp.setLayoutData(fdServiceComp);
    
		wServiceComp.layout();
		wServiceTab.setControl(wServiceComp);
        
		/////////////////////////////////////////////////////////////
		/// END OF GEN TAB
		/////////////////////////////////////////////////////////////
	}
    
	private void addProxyTab()
	{
		//////////////////////////
		// START OF POOL TAB///
		///
		wProxyTab=new CTabItem(wTabFolder, SWT.NONE);
		wProxyTab.setText("Proxy");

		FormLayout poolLayout = new FormLayout ();
		poolLayout.marginWidth  = Const.FORM_MARGIN;
		poolLayout.marginHeight = Const.FORM_MARGIN;
        
		wProxyComp = new Composite(wTabFolder, SWT.NONE);
		props.setLook(wProxyComp);
		wProxyComp.setLayout(poolLayout);

		// What's the data tablespace name?
		Label wlProxyHost = new Label(wProxyComp, SWT.RIGHT); 
		props.setLook(wlProxyHost);
		wlProxyHost.setText(Messages.getString("SlaveServerDialog.ProxyServerName.Label")); 
		FormData fdlProxyHost = new FormData();
		fdlProxyHost.top   = new FormAttachment(0, 0);
		fdlProxyHost.left  = new FormAttachment(0, 0);  // First one in the left top corner
		fdlProxyHost.right = new FormAttachment(middle, -margin);
		wlProxyHost.setLayoutData(fdlProxyHost);

		wProxyHost = new TextVar(wProxyComp, SWT.BORDER | SWT.LEFT | SWT.SINGLE );
		props.setLook(wProxyHost);
		wProxyHost.addModifyListener(lsMod);
		FormData fdProxyHost = new FormData();
		fdProxyHost.top  = new FormAttachment(0, 0);
		fdProxyHost.left = new FormAttachment(middle, 0); // To the right of the label
		fdProxyHost.right= new FormAttachment(95, 0);
		wProxyHost.setLayoutData(fdProxyHost);

		// What's the initial pool size
		Label wlProxyPort = new Label(wProxyComp, SWT.RIGHT); 
		props.setLook(wlProxyPort);
		wlProxyPort.setText(Messages.getString("SlaveServerDialog.ProxyServerPort.Label")); 
		FormData fdlProxyPort = new FormData();
		fdlProxyPort.top   = new FormAttachment(wProxyHost, margin);
		fdlProxyPort.left  = new FormAttachment(0, 0);  // First one in the left top corner
		fdlProxyPort.right = new FormAttachment(middle, -margin);
		wlProxyPort.setLayoutData(fdlProxyPort);

		wProxyPort = new TextVar(wProxyComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
		props.setLook(wProxyPort);
		wProxyPort.addModifyListener(lsMod);
		FormData fdProxyPort = new FormData();
		fdProxyPort.top  = new FormAttachment(wProxyHost, margin);
		fdProxyPort.left = new FormAttachment(middle, 0); // To the right of the label
		fdProxyPort.right= new FormAttachment(95, 0);
		wProxyPort.setLayoutData(fdProxyPort);

		// What's the maximum pool size
		Label wlNonProxyHosts = new Label(wProxyComp, SWT.RIGHT); 
		props.setLook(wlNonProxyHosts);
		wlNonProxyHosts.setText(Messages.getString("SlaveServerDialog.IgnoreProxyForHosts.Label")); 
		FormData fdlNonProxyHosts = new FormData();
		fdlNonProxyHosts.top   = new FormAttachment(wProxyPort, margin);
		fdlNonProxyHosts.left  = new FormAttachment(0, 0);  // First one in the left top corner
		fdlNonProxyHosts.right = new FormAttachment(middle, -margin);
		wlNonProxyHosts.setLayoutData(fdlNonProxyHosts);

		wNonProxyHosts = new TextVar(wProxyComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
		props.setLook(wNonProxyHosts);
		wNonProxyHosts.addModifyListener(lsMod);
		FormData fdNonProxyHosts = new FormData();
		fdNonProxyHosts.top  = new FormAttachment(wProxyPort, margin);
		fdNonProxyHosts.left = new FormAttachment(middle, 0); // To the right of the label
		fdNonProxyHosts.right= new FormAttachment(95, 0);
		wNonProxyHosts.setLayoutData(fdNonProxyHosts);

        
		fdProxyComp = new FormData();
		fdProxyComp.left  = new FormAttachment(0, 0);
		fdProxyComp.top   = new FormAttachment(0, 0);
		fdProxyComp.right = new FormAttachment(100, 0);
		fdProxyComp.bottom= new FormAttachment(100, 0);
		wProxyComp.setLayoutData(fdProxyComp);

		wProxyComp.layout();
		wProxyTab.setControl(wProxyComp);
	}

	public void dispose()
	{
		props.setScreen(new WindowProperty(shell));
		shell.dispose();
	}
    
	public void getData()
	{
		wName    .setText( Const.NVL(slaveServer.getName(),     "") );
		wHostname.setText( Const.NVL(slaveServer.getHostname(), "") );
		wPort    .setText( Const.NVL(slaveServer.getPort(),     "") );
		wUsername.setText( Const.NVL(slaveServer.getUsername(), "") );
		wPassword.setText( Const.NVL(slaveServer.getPassword(), "") );

		wProxyHost.setText( Const.NVL(slaveServer.getProxyHostname(), ""));
		wProxyPort.setText( Const.NVL(slaveServer.getProxyPort(), ""));
		wNonProxyHosts.setText( Const.NVL(slaveServer.getNonProxyHosts(), ""));
        
		wMaster.setSelection( slaveServer.isMaster() );
        
		wName.setFocus();
	}
    
	private void cancel()
	{
		originalServer = null;
		dispose();
	}
	
	public void ok()
	{
		getInfo();
		originalServer.setName    (slaveServer.getName());
		originalServer.setHostname(slaveServer.getHostname());
		originalServer.setPort    (slaveServer.getPort());
		originalServer.setUsername(slaveServer.getUsername());
		originalServer.setPassword(slaveServer.getPassword());

		originalServer.setProxyHostname(slaveServer.getProxyHostname());
		originalServer.setProxyPort(slaveServer.getProxyPort());
		originalServer.setNonProxyHosts(slaveServer.getNonProxyHosts());

		originalServer.setMaster( slaveServer.isMaster() );

		originalServer.setChanged();

		ok=true;
        
		dispose();
	}
    
	// Get dialog info in securityService
	private void getInfo()
	{
		slaveServer.setName    (wName    .getText());
		slaveServer.setHostname(wHostname.getText());
		slaveServer.setPort    (wPort    .getText());
		slaveServer.setUsername(wUsername.getText());
		slaveServer.setPassword(wPassword.getText());

		slaveServer.setProxyHostname(wProxyHost.getText());
		slaveServer.setProxyPort(wProxyPort.getText());
		slaveServer.setNonProxyHosts(wNonProxyHosts.getText());

		slaveServer.setMaster(wMaster.getSelection());
	}

	public void test()
	{
		try
		{
			getInfo();
            
			String xml = "<sample/>";
            
			String reply = slaveServer.sendXML(xml, AddTransServlet.CONTEXT_PATH);
            
			String message = Messages.getString("SlaveServer.Replay.Info1")
				+slaveServer.constructUrl(AddTransServlet.CONTEXT_PATH)+Const.CR+
				Messages.getString("SlaveServer.Replay.Info2") +Const.CR+Const.CR;
			message+=xml;
			message+=Const.CR+Const.CR;
			message+="Reply was:"+Const.CR+Const.CR;
			message+=reply+Const.CR;
            
			EnterTextDialog dialog = new EnterTextDialog(shell, "XML", Messages.getString("SlaveServer.RetournedXMLInfo"), message);
			dialog.open();
		}
		catch(Exception e)
		{
			new ErrorDialog(shell, Messages.getString("SlaveServer.ExceptionError"), Messages.getString("SlaveServer.ExceptionUnableGetReplay.Error1")
				+slaveServer.getHostname()+ Messages.getString("SlaveServer.ExceptionUnableGetReplay.Error2"), e);
		}		
	}
}