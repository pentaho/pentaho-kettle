package be.ibridge.kettle.www.dialog;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
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

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.Props;
import be.ibridge.kettle.core.WindowProperty;
import be.ibridge.kettle.core.dialog.EnterTextDialog;
import be.ibridge.kettle.core.dialog.ErrorDialog;
import be.ibridge.kettle.trans.step.BaseStepDialog;
import be.ibridge.kettle.www.SlaveServer;


/**
 * 
 * Dialog that allows you to edit the settings of the security service connection
 * 
 * @see <code>SecurityService</code>
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
	private Label    wlServiceURL, wlUsername, wlPassword;
	private Text     wServiceURL,  wUsername,  wPassword;

    // Proxy
    private Label    wlProxyHost, wlProxyPort, wlNonProxyHosts;
    private Text     wProxyHost, wProxyPort,  wNonProxyHosts;

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
		
		shell.setText("Slave Server dialog");
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
		wServiceURL.addSelectionListener(selAdapter);

		// Detect X or ALT-F4 or something that kills this window...
		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { cancel(); } } );
	
        wTabFolder.setSelection(0);
        
		getData();

		BaseStepDialog.setSize(shell);
		
		shell.open();
		Display display = parent.getDisplay();
		while (!shell.isDisposed()) {
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

        // What's the service URL?
        wlServiceURL = new Label(wServiceComp, SWT.RIGHT); 
        props.setLook(wlServiceURL);
        wlServiceURL.setText("Service URL: ");
        FormData fdlServiceURL = new FormData();
        fdlServiceURL.top   = new FormAttachment(0, 0);
        fdlServiceURL.left  = new FormAttachment(0, 0);  // First one in the left top corner
        fdlServiceURL.right = new FormAttachment(middle, -margin);
        wlServiceURL.setLayoutData(fdlServiceURL);

        wServiceURL = new Text(wServiceComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
        props.setLook(wServiceURL);
        wServiceURL.addModifyListener(lsMod);
        FormData fdServiceURL = new FormData();
        fdServiceURL.top  = new FormAttachment(0, 0);
        fdServiceURL.left = new FormAttachment(middle, 0); // To the right of the label
        fdServiceURL.right= new FormAttachment(95, 0);
        wServiceURL.setLayoutData(fdServiceURL);

        // Username
        wlUsername = new Label(wServiceComp, SWT.RIGHT ); 
        wlUsername.setText("Username: "); 
        props.setLook(wlUsername);
        FormData fdlUsername = new FormData();
        fdlUsername.top  = new FormAttachment(wServiceURL, margin);
        fdlUsername.left = new FormAttachment(0,0); 
        fdlUsername.right= new FormAttachment(middle, -margin);
        wlUsername.setLayoutData(fdlUsername);

        wUsername = new Text(wServiceComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
        props.setLook(wUsername);
        wUsername.addModifyListener(lsMod);
        FormData fdUsername = new FormData();
        fdUsername.top  = new FormAttachment(wServiceURL, margin);
        fdUsername.left = new FormAttachment(middle, 0); 
        fdUsername.right= new FormAttachment(95, 0);
        wUsername.setLayoutData(fdUsername);

        
        // Password
        wlPassword = new Label(wServiceComp, SWT.RIGHT ); 
        wlPassword.setText("Password: "); 
        props.setLook(wlPassword);
        FormData fdlPassword = new FormData();
        fdlPassword.top  = new FormAttachment(wUsername, margin);
        fdlPassword.left = new FormAttachment(0,0);
        fdlPassword.right= new FormAttachment(middle, -margin);
        wlPassword.setLayoutData(fdlPassword);

        wPassword = new Text(wServiceComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
        props.setLook(wPassword);
        wPassword.setEchoChar('*');
        wPassword.addModifyListener(lsMod);
        FormData fdPassword = new FormData();
        fdPassword.top  = new FormAttachment(wUsername, margin);
        fdPassword.left = new FormAttachment(middle, 0); 
        fdPassword.right= new FormAttachment(95, 0);
        wPassword.setLayoutData(fdPassword);

        
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
        wlProxyHost = new Label(wProxyComp, SWT.RIGHT); 
        props.setLook(wlProxyHost);
        wlProxyHost.setText("Proxy server hostname: "); 
        FormData fdlProxyHost = new FormData();
        fdlProxyHost.top   = new FormAttachment(0, 0);
        fdlProxyHost.left  = new FormAttachment(0, 0);  // First one in the left top corner
        fdlProxyHost.right = new FormAttachment(middle, -margin);
        wlProxyHost.setLayoutData(fdlProxyHost);

        wProxyHost = new Text(wProxyComp, SWT.BORDER | SWT.LEFT | SWT.SINGLE );
        props.setLook(wProxyHost);
        wProxyHost.addModifyListener(lsMod);
        FormData fdProxyHost = new FormData();
        fdProxyHost.top  = new FormAttachment(0, 0);
        fdProxyHost.left = new FormAttachment(middle, 0); // To the right of the label
        fdProxyHost.right= new FormAttachment(95, 0);
        wProxyHost.setLayoutData(fdProxyHost);

        // What's the initial pool size
        wlProxyPort = new Label(wProxyComp, SWT.RIGHT); 
        props.setLook(wlProxyPort);
        wlProxyPort.setText("The proxy server port: "); 
        FormData fdlProxyPort = new FormData();
        fdlProxyPort.top   = new FormAttachment(wProxyHost, margin);
        fdlProxyPort.left  = new FormAttachment(0, 0);  // First one in the left top corner
        fdlProxyPort.right = new FormAttachment(middle, -margin);
        wlProxyPort.setLayoutData(fdlProxyPort);

        wProxyPort = new Text(wProxyComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
        props.setLook(wProxyPort);
        wProxyPort.addModifyListener(lsMod);
        FormData fdProxyPort = new FormData();
        fdProxyPort.top  = new FormAttachment(wProxyHost, margin);
        fdProxyPort.left = new FormAttachment(middle, 0); // To the right of the label
        fdProxyPort.right= new FormAttachment(95, 0);
        wProxyPort.setLayoutData(fdProxyPort);

        // What's the maximum pool size
        wlNonProxyHosts = new Label(wProxyComp, SWT.RIGHT); 
        props.setLook(wlNonProxyHosts);
        wlNonProxyHosts.setText("Ignore proxy for hosts: regexp | separated: "); 
        FormData fdlNonProxyHosts = new FormData();
        fdlNonProxyHosts.top   = new FormAttachment(wProxyPort, margin);
        fdlNonProxyHosts.left  = new FormAttachment(0, 0);  // First one in the left top corner
        fdlNonProxyHosts.right = new FormAttachment(middle, -margin);
        wlNonProxyHosts.setLayoutData(fdlNonProxyHosts);

        wNonProxyHosts = new Text(wProxyComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
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
		wServiceURL.setText( Const.NVL(slaveServer.getServiceUrl(), "") );
        wUsername.setText( Const.NVL(slaveServer.getUsername(), "") );
		wPassword.setText( Const.NVL(slaveServer.getPassword(), "") );

        wProxyHost.setText( Const.NVL(slaveServer.getProxyHostname(), ""));
        wProxyPort.setText( Const.NVL(slaveServer.getProxyPort(), ""));
        wNonProxyHosts.setText( Const.NVL(slaveServer.getNonProxyHosts(), ""));
        
		wServiceURL.setFocus();
	}
    
	private void cancel()
	{
		originalServer = null;
		dispose();
	}
	
	public void ok()
	{
        getInfo();
        originalServer.setServiceUrl(slaveServer.getServiceUrl());
        originalServer.setUsername(slaveServer.getUsername());
        originalServer.setPassword(slaveServer.getPassword());

        originalServer.setProxyHostname(slaveServer.getProxyHostname());
        originalServer.setProxyPort(slaveServer.getProxyPort());
        originalServer.setNonProxyHosts(slaveServer.getNonProxyHosts());
        
        originalServer.setChanged();

        ok=true;
        
        dispose();
	}
    
    // Get dialog info in securityService
	private void getInfo()
    {
        slaveServer.setServiceUrl(wServiceURL.getText());
        slaveServer.setUsername(wUsername.getText());
        slaveServer.setPassword(wPassword.getText());

        slaveServer.setProxyHostname(wProxyHost.getText());
        slaveServer.setProxyPort(wProxyPort.getText());
        slaveServer.setNonProxyHosts(wNonProxyHosts.getText());
    }

	public void test()
	{
		try
		{
			getInfo();
            
            String xml = "<sample/>";
            
            String reply = slaveServer.sendXML(xml);
            
            String message = "Testing reply from server URL: "+slaveServer.getServiceUrl()+"Using content: "+Const.CR+Const.CR;
            message+=xml;
            message+=Const.CR+Const.CR;
            message+="Reply was:"+Const.CR+Const.CR;
            message+=reply+Const.CR;
            
			EnterTextDialog dialog = new EnterTextDialog(shell, "XML", "The XML returned is:", message);
            dialog.open();
		}
		catch(Exception e)
		{
			new ErrorDialog(shell, "Error", "Unable to get a reply back from URL ["+slaveServer.getServiceUrl()+"]", e);
		}		
	}
}