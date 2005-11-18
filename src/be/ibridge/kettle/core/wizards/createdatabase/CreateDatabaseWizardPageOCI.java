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

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
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
 * On page one we specify the OCI TNS connection...
 * 
 * @author Matt
 * @since  04-apr-2005
 */
public class CreateDatabaseWizardPageOCI extends WizardPage
{
	private Label    wlTNS;
	private Text     wTNS;
	private FormData fdlTNS, fdTNS;
	
	private Props props;
	private DatabaseMeta info;
	
	public CreateDatabaseWizardPageOCI(String arg, Props props, DatabaseMeta info)
	{
		super(arg);
		this.props=props;
		this.info = info;
		
		setTitle("Specify the Oracle TNS database");
		setDescription("Specify the TNS database as defined in your Oracle client.");
		
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

		wlTNS = new Label(composite, SWT.RIGHT);
		wlTNS.setText("Name of the Oracle TNS database");
 		props.setLook(wlTNS);
		fdlTNS = new FormData();
		fdlTNS.left   = new FormAttachment(0,0);
		fdlTNS.right  = new FormAttachment(middle,0);
		wlTNS.setLayoutData(fdlTNS);
		wTNS = new Text(composite, SWT.SINGLE | SWT.BORDER);
 		props.setLook(wTNS);
		fdTNS = new FormData();
		fdTNS.left    = new FormAttachment(middle, margin);
		fdTNS.right   = new FormAttachment(100, 0);
		wTNS.setLayoutData(fdTNS);
		
		// set the composite as the control for this page
		setControl(composite);
	}
	
	public boolean canFlipToNextPage()
	{
		String name = wTNS.getText()!=null?wTNS.getText().length()>0?wTNS.getText():null:null;
		if (name==null)
		{
			setErrorMessage("Enter the name of the Oracle TNSNAMES database");
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
		if (wTNS.getText()!=null && wTNS.getText().length()>0) 
		{
			info.setDBName(wTNS.getText());
		}
		
		info.setDBPort(-1);
		info.setServername(null);
		
		return info;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.WizardPage#getNextPage()
	 */
	public IWizardPage getNextPage()
	{
		IWizardPage nextPage;
		switch(info.getDatabaseType())
		{
		case DatabaseMeta.TYPE_DATABASE_ORACLE:
			nextPage = null; // Oracle
			break;
		case DatabaseMeta.TYPE_DATABASE_INFORMIX:
			nextPage = null; // Informix
			break;
		default: 
			nextPage = null; // page 2
			break;
		}
		
		return nextPage;
	}
}
