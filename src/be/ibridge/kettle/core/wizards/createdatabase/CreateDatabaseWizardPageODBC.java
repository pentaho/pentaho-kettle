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
 * On page one we select the ODBC DSN Name...
 * 
 * @author Matt
 * @since  04-apr-2005
 */
public class CreateDatabaseWizardPageODBC extends WizardPage
{
	private Label    wlDSN;
	private Text     wDSN;
	private FormData fdlDSN, fdDSN;
	
	private Props props;
	private DatabaseMeta info;
	
	public CreateDatabaseWizardPageODBC(String arg, Props props, DatabaseMeta info)
	{
		super(arg);
		this.props=props;
		this.info = info;
		
		setTitle("Specify the ODBC DSN data source");
		setDescription("Specify the ODBC DSN name as defined in the user- or system-DSN data sources.");
		
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

		wlDSN = new Label(composite, SWT.RIGHT);
		wlDSN.setText("Name of the ODBC DSN data source");
 		props.setLook(wlDSN);
		fdlDSN = new FormData();
		fdlDSN.left   = new FormAttachment(0,0);
		fdlDSN.right  = new FormAttachment(middle,0);
		wlDSN.setLayoutData(fdlDSN);
		wDSN = new Text(composite, SWT.SINGLE | SWT.BORDER);
 		props.setLook(wDSN);
		fdDSN = new FormData();
		fdDSN.left    = new FormAttachment(middle, margin);
		fdDSN.right   = new FormAttachment(100, 0);
		wDSN.setLayoutData(fdDSN);
		
		// set the composite as the control for this page
		setControl(composite);
	}
	
	public boolean canFlipToNextPage()
	{
		String name = wDSN.getText()!=null?wDSN.getText().length()>0?wDSN.getText():null:null;
		if (name==null)
		{
			setErrorMessage("Enter the name of the ODBC DSN data source");
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
		if (wDSN.getText()!=null && wDSN.getText().length()>0) 
		{
			info.setDBName(wDSN.getText());
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
