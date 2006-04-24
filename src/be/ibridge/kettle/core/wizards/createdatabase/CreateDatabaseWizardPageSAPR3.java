/***********************************************************************
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
import be.ibridge.kettle.core.database.SAPR3DatabaseMeta;


/**
 * 
 * On page one we select the database connection SAP/R3 specific settings
 * 1) The data tablespace
 * 2) The index tablespace
 * 
 * @author Jens Bleuel
 * @since  22-mar-2006
 */
public class CreateDatabaseWizardPageSAPR3 extends WizardPage
{
	private Label    wlHostname;
	private Text     wHostname;
	private FormData fdlHostname, fdHostname;
	
    // SAP
    private Label    wlSAPLanguage, wlSAPSystemNumber, wlSAPClient;
    private Text     wSAPLanguage, wSAPSystemNumber, wSAPClient;
    private FormData fdlSAPLanguage, fdlSAPSystemNumber, fdlSAPClient;
    private FormData fdSAPLanguage, fdSAPSystemNumber, fdSAPClient;
    
	private Props props;
	private DatabaseMeta info;
	
	public CreateDatabaseWizardPageSAPR3(String arg, Props props, DatabaseMeta info)
	{
		super(arg);
		this.props=props;
		this.info = info;
		
		setTitle("Specify the SAP/R3 specific settings");
		setDescription("Specify the server hostname, language, system number and client.");
		
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
		wlHostname.setText("Host name of the SAP/3 system");
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
		
		wlSAPLanguage = new Label(composite, SWT.RIGHT);
		wlSAPLanguage.setText("Language");
 		props.setLook(wlSAPLanguage);
		fdlSAPLanguage = new FormData();
		fdlSAPLanguage.top    = new FormAttachment(wHostname, margin);
		fdlSAPLanguage.left   = new FormAttachment(0, 0);
		fdlSAPLanguage.right  = new FormAttachment(middle,0);
		wlSAPLanguage.setLayoutData(fdlSAPLanguage);
		wSAPLanguage = new Text(composite, SWT.SINGLE | SWT.BORDER);
 		props.setLook(wSAPLanguage);
		fdSAPLanguage = new FormData();
		fdSAPLanguage.top     = new FormAttachment(wHostname, margin);
		fdSAPLanguage.left    = new FormAttachment(middle, margin);
		fdSAPLanguage.right   = new FormAttachment(100, 0);
		wSAPLanguage.setLayoutData(fdSAPLanguage);
		wSAPLanguage.addModifyListener(new ModifyListener()
		{
			public void modifyText(ModifyEvent arg0)
			{
				setPageComplete(false);
			}
		});
		
		wlSAPSystemNumber = new Label(composite, SWT.RIGHT);
		wlSAPSystemNumber.setText("System Number");
 		props.setLook(wlSAPSystemNumber);
		fdlSAPSystemNumber = new FormData();
		fdlSAPSystemNumber.top    = new FormAttachment(wSAPLanguage, margin);
		fdlSAPSystemNumber.left   = new FormAttachment(0, 0);
		fdlSAPSystemNumber.right  = new FormAttachment(middle, 0);
		wlSAPSystemNumber.setLayoutData(fdlSAPSystemNumber);
		wSAPSystemNumber = new Text(composite, SWT.SINGLE | SWT.BORDER);
 		props.setLook(wSAPSystemNumber);
		fdSAPSystemNumber = new FormData();
		fdSAPSystemNumber.top    = new FormAttachment(wSAPLanguage, margin);
		fdSAPSystemNumber.left   = new FormAttachment(middle, margin);
		fdSAPSystemNumber.right  = new FormAttachment(100,0);
		wSAPSystemNumber.setLayoutData(fdSAPSystemNumber);
		wSAPSystemNumber.addModifyListener(new ModifyListener()
		{
			public void modifyText(ModifyEvent arg0)
			{
				setPageComplete(false);
			}
		});

		wlSAPClient = new Label(composite, SWT.RIGHT);
		wlSAPClient.setText("SAP Client");
 		props.setLook(wlSAPClient);
		fdlSAPClient = new FormData();
		fdlSAPClient.top    = new FormAttachment(wSAPSystemNumber, margin);
		fdlSAPClient.left   = new FormAttachment(0, 0);
		fdlSAPClient.right  = new FormAttachment(middle, 0);
		wlSAPClient.setLayoutData(fdlSAPClient);
		wSAPClient = new Text(composite, SWT.SINGLE | SWT.BORDER);
 		props.setLook(wSAPClient);
		fdSAPClient = new FormData();
		fdSAPClient.top    = new FormAttachment(wSAPSystemNumber, margin);
		fdSAPClient.left   = new FormAttachment(middle, margin);
		fdSAPClient.right  = new FormAttachment(100,0);
		wSAPClient.setLayoutData(fdSAPClient);
		wSAPClient.addModifyListener(new ModifyListener()
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

		wSAPLanguage.setText(Const.NVL(info.getAttributes().getProperty(SAPR3DatabaseMeta.ATTRIBUTE_SAP_LANGUAGE, ""), ""));
		wSAPSystemNumber.setText(Const.NVL(info.getAttributes().getProperty(SAPR3DatabaseMeta.ATTRIBUTE_SAP_SYSTEM_NUMBER, ""), ""));
		wSAPClient.setText(Const.NVL(info.getAttributes().getProperty(SAPR3DatabaseMeta.ATTRIBUTE_SAP_CLIENT, ""), ""));
	}
	
	public boolean canFlipToNextPage()
	{
		String server = wHostname.getText()!=null?wHostname.getText().length()>0?wHostname.getText():null:null;
		String language   = wSAPLanguage.getText()!=null?wSAPLanguage.getText().length()>0?wSAPLanguage.getText():null:null;
		String systemNumber = wSAPSystemNumber.getText()!=null?wSAPSystemNumber.getText().length()>0?wSAPSystemNumber.getText():null:null;
		String client = wSAPClient.getText()!=null?wSAPClient.getText().length()>0?wSAPClient.getText():null:null;
		
		if (server==null || language==null || systemNumber==null || client==null)
		{
			setErrorMessage("Specify the server hostname, language, system number and client.");
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

		if (wSAPLanguage.getText()!=null && wSAPLanguage.getText().length()>0) 
		{
	        info.getAttributes().put(SAPR3DatabaseMeta.ATTRIBUTE_SAP_LANGUAGE,     wSAPLanguage.getText());
		}
		
		if (wSAPSystemNumber.getText()!=null && wSAPSystemNumber.getText().length()>0)
		{
			info.getAttributes().put(SAPR3DatabaseMeta.ATTRIBUTE_SAP_SYSTEM_NUMBER, wSAPSystemNumber.getText());
		}

		if (wSAPClient.getText()!=null && wSAPClient.getText().length()>0)
		{
	        info.getAttributes().put(SAPR3DatabaseMeta.ATTRIBUTE_SAP_CLIENT,       wSAPClient.getText());
		}

		return info;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.WizardPage#getNextPage()
	 */
	public IWizardPage getNextPage()
	{
		IWizard wiz = getWizard();
		return wiz.getPage("2");
	}
	
}
