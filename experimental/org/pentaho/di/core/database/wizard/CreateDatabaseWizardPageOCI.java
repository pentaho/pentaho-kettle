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


package org.pentaho.di.core.database.wizard;

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
import org.pentaho.di.core.database.DatabaseMeta;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.Props;


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
		
		setTitle(Messages.getString("CreateDatabaseWizardPageOCI.DialogTitle")); //$NON-NLS-1$
		setDescription(Messages.getString("CreateDatabaseWizardPageOCI.DialogMessage")); //$NON-NLS-1$
		
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
		wlTNS.setText(Messages.getString("CreateDatabaseWizardPageOCI.TNS.Label")); //$NON-NLS-1$
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
		wTNS.addModifyListener(new ModifyListener()
				{
					public void modifyText(ModifyEvent arg0)
					{
						setPageComplete(false);
					}
				});					
		
		// set the composite as the control for this page
		setControl(composite);
	}
	
	public boolean canFlipToNextPage()
	{
		String name = wTNS.getText()!=null?wTNS.getText().length()>0?wTNS.getText():null:null;
		if (name==null)
		{
			setErrorMessage(Messages.getString("CreateDatabaseWizardPageOCI.ErrorMessage.NoTNSName")); //$NON-NLS-1$
			return false;
		}
		else
		{
			getDatabaseInfo();
			setErrorMessage(null);
			setMessage(Messages.getString("CreateDatabaseWizardPageOCI.Message.Next")); //$NON-NLS-1$
			return true;
		}
	}	
	
	public DatabaseMeta getDatabaseInfo()
	{
		if (wTNS.getText()!=null && wTNS.getText().length()>0) 
		{
			info.setDBName(wTNS.getText());
		}
		
		info.setDBPort(""); //$NON-NLS-1$
		info.setServername(null);
		
		return info;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.WizardPage#getNextPage()
	 */
	public IWizardPage getNextPage()
	{
		IWizard wiz = getWizard();
		return wiz.getPage("2"); //$NON-NLS-1$
	}
}
