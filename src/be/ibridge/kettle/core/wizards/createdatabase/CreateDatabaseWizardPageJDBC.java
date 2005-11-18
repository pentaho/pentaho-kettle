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

import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.Props;
import be.ibridge.kettle.core.database.DatabaseMeta;


/**
 * 
 * On page one we select the database connection JDBC settings
 * 1) The servername
 * 2) The port
 * 3) The database name
 * 
 * @author Matt
 * @since  04-apr-2005
 */
public class CreateDatabaseWizardPageJDBC extends WizardPage
{
	private Label    wlHostname;
	private Text     wHostname;
	private FormData fdlHostname, fdHostname;
	
	private Label    wlPort;
	private Text     wPort;
	private FormData fdlPort, fdPort;
	
	private Label    wlDBName;
	private Text     wDBName;
	private FormData fdlDBName, fdDBName;

	private Props props;
	private DatabaseMeta info;
	
	public CreateDatabaseWizardPageJDBC(String arg, Props props, DatabaseMeta info)
	{
		super(arg);
		this.props=props;
		this.info = info;
		
		setTitle("Set the JDBC Settings");
		setDescription("Specify the server hostname, the port and the database name.");
		
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
	    compLayout.marginWidth  = Const.FORM_MARGIN;
		composite.setLayout(compLayout);

		// HOSTNAME
		wlHostname = new Label(composite, SWT.RIGHT);
		wlHostname.setText("Host name of the database server");
 		props.setLook(wlHostname);
		fdlHostname = new FormData();
		fdlHostname.top    = new FormAttachment(0, 0);
		fdlHostname.left   = new FormAttachment(0, 0);
		fdlHostname.right  = new FormAttachment(middle,0);
		wlHostname.setLayoutData(fdlHostname);
		wHostname = new Text(composite, SWT.SINGLE | SWT.BORDER);
 		props.setLook(wHostname);
		fdHostname = new FormData();
		fdHostname.top     = new FormAttachment(0, 0);
		fdHostname.left    = new FormAttachment(middle, margin);
		fdHostname.right   = new FormAttachment(100, 0);
		wHostname.setLayoutData(fdHostname);
		wHostname.addModifyListener(new ModifyListener()
		{
			public void modifyText(ModifyEvent arg0)
			{
				setPageComplete(false);
			}
		});
		
		// PORT
		wlPort = new Label(composite, SWT.RIGHT);
		wlPort.setText("The TCP/IP port");
 		props.setLook(wlPort);
		fdlPort = new FormData();
		fdlPort.top    = new FormAttachment(wHostname, margin);
		fdlPort.left   = new FormAttachment(0, 0);
		fdlPort.right  = new FormAttachment(middle, 0);
		wlPort.setLayoutData(fdlPort);
		wPort = new Text(composite, SWT.SINGLE | SWT.BORDER);
 		props.setLook(wPort);
		if (info.getDatabasePortNumber()>0) wPort.setText(""+info.getDatabasePortNumber());
		fdPort = new FormData();
		fdPort.top    = new FormAttachment(wHostname, margin);
		fdPort.left   = new FormAttachment(middle, margin);
		fdPort.right  = new FormAttachment(100,0);
		wPort.setLayoutData(fdPort);
		wPort.addModifyListener(new ModifyListener()
		{
			public void modifyText(ModifyEvent arg0)
			{
				setPageComplete(false);
			}
		});

		// DATABASE NAME
		wlDBName = new Label(composite, SWT.RIGHT);
		wlDBName.setText("The name of the database");
 		props.setLook(wlDBName);
		fdlDBName = new FormData();
		fdlDBName.top    = new FormAttachment(wPort, margin);
		fdlDBName.left   = new FormAttachment(0, 0);
		fdlDBName.right  = new FormAttachment(middle, 0);
		wlDBName.setLayoutData(fdlDBName);
		wDBName = new Text(composite, SWT.SINGLE | SWT.BORDER);
 		props.setLook(wDBName);
		fdDBName = new FormData();
		fdDBName.top    = new FormAttachment(wPort, margin);
		fdDBName.left   = new FormAttachment(middle, margin);
		fdDBName.right  = new FormAttachment(100,0);
		wDBName.setLayoutData(fdDBName);
		wDBName.addModifyListener(new ModifyListener()
		{
			public void modifyText(ModifyEvent arg0)
			{
				setPageComplete(false);
			}
		});
		
		// set the composite as the control for this page
		setControl(composite);
	}
	
	public void setData()
	{
		wHostname.setText(Const.NVL(info.getHostname(), ""));
		
		if (info.getDatabasePortNumber()>0) wPort.setText(""+info.getDatabasePortNumber()); 
		else wPort.setText("");
		
		wDBName.setText(Const.NVL(info.getDatabaseName(), ""));
	}
	
	public boolean canFlipToNextPage()
	{
		String server = wHostname.getText()!=null?wHostname.getText().length()>0?wHostname.getText():null:null;
		String port   = wPort.getText()!=null?wPort.getText().length()>0?wPort.getText():null:null;
		String dbname = wDBName.getText()!=null?wDBName.getText().length()>0?wDBName.getText():null:null;
		
		if (server==null || port==null || dbname==null)
		{
			setErrorMessage("Enter the hostname of the database server, the port and the database name");
			return false;
		}
		else
		{
			getDatabaseInfo();
			setErrorMessage(null);
			setMessage("Select 'next' to proceed");
			return true;
		}
	}	
	
	public DatabaseMeta getDatabaseInfo()
	{
		if (wHostname.getText()!=null && wHostname.getText().length()>0) 
		{
			info.setHostname(wHostname.getText());
		}
		
		if (wPort.getText()!=null && wPort.getText().length()>0)
		{
			info.setDBPort(Const.toInt(wPort.getText(), -1));
		}
		
		if (wDBName.getText()!=null && wDBName.getText().length()>0)
		{
			info.setDBName(wDBName.getText());
		}
		
		return info;
	}
	
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.WizardPage#getNextPage()
	 */
	public IWizardPage getNextPage()
	{
		IWizard wiz = getWizard();
		
		IWizardPage nextPage;
		switch(info.getDatabaseType())
		{
		case DatabaseMeta.TYPE_DATABASE_ORACLE:
			nextPage = wiz.getPage("oracle"); // Oracle
			break;
		case DatabaseMeta.TYPE_DATABASE_INFORMIX:
			nextPage = wiz.getPage("ifx"); // Informix
			break;
		default: 
			nextPage = wiz.getPage("2"); // page 2
			break;
		}
		
		return nextPage;
	}
}
