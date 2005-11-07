 /**********************************************************************
 **                                                                   **
 **               This code belongs to the KETTLE project.            **
 **                                                                   **
 ** It belongs to, is maintained by and is copyright 1999-2005 by     **
 **                                                                   **
 **      i-Bridge bvba                                                **
 **      Fonteinstraat 70                                             **
 **      9400 OKEGEM                                                  **
 **      Belgium                                                      **
 **      http://www.kettle.be                                         **
 **      info@kettle.be                                               **
 **                                                                   **
 **********************************************************************/
 
/*
 * Created on 18-mei-2003
 *
 */

package be.ibridge.kettle.spoon.dialog;
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
import be.ibridge.kettle.core.Encr;
import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.Props;
import be.ibridge.kettle.core.WindowProperty;
import be.ibridge.kettle.core.license.License;
import be.ibridge.kettle.core.license.Licenses;

public class ShowLicenseDialog
{
	private Props props;
	private Display disp;
	private int     retval;
	
	public  Shell     shell;
	private Label     wlNr, wlMAC, wlCMAC, wlUsername, wlCompany, wlProducts, wlSignature;
	private CCombo    wNr;
	private Text      wMAC;
	private Text      wCMAC;
	private Text      wUsername;
	private Text      wCompany;
	private Text      wProducts;
	private Text      wSignature;
			
	private Button    wOK, wCont, wCancel;
	
	// private String    product;
	private int       nr;
	
	public ShowLicenseDialog(LogWriter lg, Props pr, Display d, String prod)
	{
		disp=d;
		// product=prod;

		retval=SWT.NO;
				
		shell = new Shell(disp, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN );
		shell.setText("Kettle Licence");

		props=pr;
		
		nr=1;
	}
	
	public int open() 
	{
 		props.setLook(shell);

		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		FormLayout formLayout = new FormLayout ();
		formLayout.marginWidth  = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;
		
		shell.setLayout (formLayout);
 		
 		// Nr of licences:
		wlNr = new Label(shell, SWT.RIGHT); 
 		props.setLook(wlNr);
		wlNr.setText("Nr "); 
		wNr = new CCombo(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
        Licenses licenses = Licenses.getInstance();
		for (int i=0;i<licenses.getNrLicenses();i++) wNr.add( ""+(i+1) );
 		props.setLook(wNr );
		wNr.addSelectionListener(new SelectionAdapter() 
			{
				public void widgetSelected(SelectionEvent arg0) 
				{
					nr = Const.toInt(wNr.getText(), 1);
					getData();
				}
			}
		);
 		
		// What's the username?
		wlUsername = new Label(shell, SWT.RIGHT); 
 		props.setLook(wlUsername);
		wlUsername.setText("Username "); 
		wUsername = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
 		props.setLook(wUsername );

		// What types are there?
		wlCompany = new Label(shell, SWT.RIGHT); 
		wlCompany.setText("Company "); 
 		props.setLook(wlCompany);
		wCompany= new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
 		props.setLook(wCompany);
		
		// Products
		wlProducts = new Label(shell, SWT.RIGHT); 
		wlProducts.setText("Products "); 
 		props.setLook(wlProducts);
		wProducts = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
 		props.setLook(wProducts);

		// What's the MAC address of the licence?
		wlMAC = new Label(shell, SWT.RIGHT); 
 		props.setLook(wlMAC);
		wlMAC.setText("MAC address (licence)"); 
		wMAC = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
 		props.setLook(wMAC );
		wMAC .setEditable(false);
				
		// What's the MAC address of the network card?
		wlCMAC = new Label(shell, SWT.RIGHT); 
 		props.setLook(wlCMAC);
		wlCMAC.setText("MAC address (network card)"); 
		wCMAC = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
 		props.setLook(wCMAC );
		wCMAC .setEditable(false);
				
		// Signature
		wlSignature = new Label(shell, SWT.RIGHT ); 
		wlSignature.setText("CODE "); 
 		props.setLook(wlSignature);
		wSignature = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
 		props.setLook(wSignature);

		// Buttons
		wOK     = new Button(shell, SWT.PUSH); 
		wOK.setText(" &Add ");
		wCont    = new Button(shell, SWT.PUSH); 
		wCont.setText(" &Demo mode ");
		wCancel = new Button(shell, SWT.PUSH); 
		wCancel.setText(" &Cancel ");
		
		FormData fdlNr       = new FormData(); 
		FormData fdNr        = new FormData(); 
		FormData fdlUsername = new FormData(); 
		FormData fdUsername  = new FormData(); 
		FormData fdlCompany  = new FormData();
		FormData fdCompany   = new FormData();
		FormData fdlProducts = new FormData();
		FormData fdProducts  = new FormData();
		FormData fdlMAC      = new FormData(); 
		FormData fdMAC       = new FormData(); 
		FormData fdlCMAC     = new FormData(); 
		FormData fdCMAC      = new FormData(); 
		FormData fdlSignature= new FormData();
		FormData fdSignature = new FormData();
		FormData fdOK        = new FormData();
		FormData fdCont      = new FormData();
		FormData fdCancel    = new FormData();

		fdlNr.left  = new FormAttachment(0, 0);  // First one in the left top corner
		fdlNr.right = new FormAttachment(middle, -margin);
		fdlNr.top   = new FormAttachment(0, 0);
		wlNr.setLayoutData(fdlNr);
		
		fdNr.left = new FormAttachment(middle, 0); // To the right of the label
		fdNr.top  = new FormAttachment(0, 0);
		fdNr.right= new FormAttachment(100, 0);
		wNr.setLayoutData(fdNr);

		fdlUsername.left  = new FormAttachment(0, 0);  // First one in the left top corner
		fdlUsername.right = new FormAttachment(middle, -margin);
		fdlUsername.top   = new FormAttachment(wNr, 0);
		wlUsername.setLayoutData(fdlUsername);
		
		fdUsername.left = new FormAttachment(middle, 0); // To the right of the label
		fdUsername.right= new FormAttachment(100, 0);
		fdUsername.top  = new FormAttachment(wNr, 0);
		wUsername.setLayoutData(fdUsername);

		fdlCompany.left = new FormAttachment(0,0); 
		fdlCompany.right= new FormAttachment(middle, -margin);
		fdlCompany.top  = new FormAttachment(wUsername, margin);  // below the line above
		wlCompany.setLayoutData(fdlCompany);

		fdCompany.left = new FormAttachment(middle, 0);  // right of the label
		fdCompany.right= new FormAttachment(100, 0);
		fdCompany.top  = new FormAttachment(wUsername, margin);
		wCompany.setLayoutData(fdCompany);

		fdlProducts.left = new FormAttachment(0,0);
		fdlProducts.right= new FormAttachment(middle, -margin);
		fdlProducts.top  = new FormAttachment(wCompany, margin);
		wlProducts.setLayoutData(fdlProducts);

		fdProducts.left = new FormAttachment(middle, 0); 
		fdProducts.right= new FormAttachment(100, 0);
		fdProducts.top  = new FormAttachment(wCompany, margin);
		wProducts.setLayoutData(fdProducts);

		fdlMAC.left = new FormAttachment(0,0);
		fdlMAC.right= new FormAttachment(middle, -margin);
		fdlMAC.top  = new FormAttachment(wProducts, margin);
		wlMAC.setLayoutData(fdlMAC);

		fdMAC.left = new FormAttachment(middle, 0); 
		fdMAC.right= new FormAttachment(100, 0);
		fdMAC.top  = new FormAttachment(wProducts, margin);
		wMAC.setLayoutData(fdMAC);

		fdlCMAC.left = new FormAttachment(0,0);
		fdlCMAC.right= new FormAttachment(middle, -margin);
		fdlCMAC.top  = new FormAttachment(wMAC, margin);
		wlCMAC.setLayoutData(fdlCMAC);

		fdCMAC.left = new FormAttachment(middle, 0); 
		fdCMAC.right= new FormAttachment(100, 0);
		fdCMAC.top  = new FormAttachment(wMAC, margin);
		wCMAC.setLayoutData(fdCMAC);
		
		fdlSignature.left = new FormAttachment(0,0);	
		fdlSignature.right= new FormAttachment(middle, -margin);
		fdlSignature.top  = new FormAttachment(wCMAC, margin);
		wlSignature.setLayoutData(fdlSignature);

		fdSignature.left = new FormAttachment(middle, 0);
		fdSignature.right= new FormAttachment(100, 0);
		fdSignature.top  = new FormAttachment(wCMAC, margin);
		wSignature.setLayoutData(fdSignature);
		
		fdOK.left    = new FormAttachment(25, 0); 
		fdOK.top     = new FormAttachment(wSignature, margin*3);
		wOK.setLayoutData(fdOK);

		fdCont.left   = new FormAttachment(wOK, margin*2); 
		fdCont.top    = new FormAttachment(wSignature, margin*3);
		wCont.setLayoutData(fdCont);

		fdCancel.left   = new FormAttachment(wCont, margin*2); 
		fdCancel.top    = new FormAttachment(wSignature, margin*3);
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
		wCont.addListener(SWT.Selection, new Listener ()
			{
				public void handleEvent (Event e) 
				{
					cont();
				}
			}
		);
		wOK.addListener(SWT.Selection, new Listener ()
			{
				public void handleEvent (Event e) 
				{
					ok();
				}
			}
		);
		SelectionAdapter selAdapter=new SelectionAdapter()
			{
				public void widgetDefaultSelected(SelectionEvent e)
				{
					ok();	
				}
			};
		wUsername.addSelectionListener(selAdapter);;
		wCompany.addSelectionListener(selAdapter);;
		wProducts.addSelectionListener(selAdapter);;
		wSignature.addSelectionListener(selAdapter);;

		// Detect X or ALT-F4 or something that kills this window...
		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { cancel(); } } );
		
		getData();

		WindowProperty winprop = props.getScreen(shell.getText());
		if (winprop!=null) winprop.setShell(shell); else shell.pack();

		shell.open();
		while (!shell.isDisposed()) {
			if (!disp.readAndDispatch()) disp.sleep();
		}
		return retval;
	}
	
	public void dispose()
	{
		props.setScreen(new WindowProperty(shell));
		shell.dispose();
	}

	private void cancel()
	{
		retval=SWT.CANCEL;
		dispose();
	}

	public void ok()
	{
		String   mac      = wCMAC.getText();
		String   username = wUsername.getText();
		String   company  = wCompany.getText();
		String   products = wProducts.getText();
        String   attempt  = wSignature.getText();
        
        License license = new License(username, company, products, mac, "");
        
        if (license.checkLicenseCode(attempt))
		{
			System.out.println("License is OK!");
			// OK, so the license code supplied is correct!
			// --> Add the license, keep all others...
			// Only Add if no license exists...
            
            Licenses licenses = Licenses.getInstance();
            
			if (licenses.findLicense(attempt)==null)
			{
				System.out.println("Adding code to licence file.");
				licenses.addLicense(license);
				licenses.storeLicenses();
			}
			retval=SWT.YES;
			dispose();
		}
		else
		{
			MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
			mb.setMessage("This code is not valid!");
			mb.setText("ERROR");
			mb.open(); 
		}
	}
	
	// 
	// Continue without license!
	private void cont()
	{
		retval=SWT.NO;
		dispose();
	}
	
	// get available license for this host
	//
	private void getData()
	{
        Licenses licenses = Licenses.getInstance();
		String mac = Const.getMACAddress();
		
		if (nr>=1 && nr<=licenses.getNrLicenses())
		{
            License license = licenses.getLicense(nr-1);
            
			String username  = license.getUserName();
			String company   = license.getCompany();
			String products  = license.getProducts();
			String attempt   = license.getLicenseCode();
			String maclic    = license.getMacAddress();
			String lic_short = Encr.getSignatureShort(attempt);

			wUsername.setText(username);
			wCompany.setText(company);
			wProducts.setText(products);
			wSignature.setText(lic_short);
			if (maclic.length()>0) wMAC.setText(maclic); else wMAC.setText(mac);
			wNr.setText(""+nr);
		}
        
        wCMAC.setText(mac);
	}
}