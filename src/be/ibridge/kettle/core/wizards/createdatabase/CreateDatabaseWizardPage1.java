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

import java.util.ArrayList;

import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Text;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.Props;
import be.ibridge.kettle.core.database.DatabaseMeta;


/**
 * 
 * On page one we select the database connection name, the database type and the access type.
 * 
 * @author Matt
 * @since  04-apr-2005
 */
public class CreateDatabaseWizardPage1 extends WizardPage
{
	private Label    wlName;
	private Text     wName;
	private FormData fdlName, fdName;
	
	private Label    wlDBType;
	private List     wDBType;
	private FormData fdlDBType, fdDBType;
	
	private Label    wlAccType;
	private List     wAccType;
	private FormData fdlAccType, fdAccType;

	private Props props;
	private DatabaseMeta info;
	private ArrayList databases;
	
	public CreateDatabaseWizardPage1(String arg, Props props, DatabaseMeta info, ArrayList databases)
	{
		super(arg);
		this.props=props;
		this.info = info;
		this.databases = databases;
		
		setTitle("Select the database name and type");
		setDescription("Select the database connection name, database type and access type.");
		
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

		wlName = new Label(composite, SWT.RIGHT);
		wlName.setText("Name of the database connection");
 		props.setLook(wlName);
		fdlName = new FormData();
		fdlName.left   = new FormAttachment(0,0);
		fdlName.top    = new FormAttachment(0,0);
		fdlName.right  = new FormAttachment(middle,0);
		wlName.setLayoutData(fdlName);
		wName = new Text(composite, SWT.SINGLE | SWT.BORDER);
 		props.setLook(wName);
		fdName = new FormData();
		fdName.left    = new FormAttachment(middle, margin);
		fdName.right   = new FormAttachment(100, 0);
		wName.setLayoutData(fdName);
		wName.addModifyListener(new ModifyListener()
			{
				public void modifyText(ModifyEvent e)
				{
					setPageComplete(false);
				}
			}
		);
		
		wlDBType = new Label(composite, SWT.RIGHT);
		wlDBType.setText("Type of database to connect to");
 		props.setLook(wlDBType);
		fdlDBType = new FormData();
		fdlDBType.left   = new FormAttachment(0, 0);
		fdlDBType.top    = new FormAttachment(wName, margin);
		fdlDBType.right  = new FormAttachment(middle, 0);
		wlDBType.setLayoutData(fdlDBType);
		wDBType = new List(composite, SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
 		props.setLook(wDBType);
		for (int i=0;i<DatabaseMeta.getDBTypeDescLongList().length;i++)
		{
			wDBType.add(DatabaseMeta.getDBTypeDescLongList()[i]);
		}
		// Select a default: the first
		if (info.getDatabaseType()==DatabaseMeta.TYPE_DATABASE_NONE) 
		{
			wDBType.select(0);
		}
		else
		{
			int idx = wDBType.indexOf(info.getDatabaseTypeDesc());
			if (idx>=0)
			{
				wDBType.select(idx);
			}
			else
			{
				wDBType.select(0);
			}
		}
		fdDBType = new FormData();
		fdDBType.top    = new FormAttachment(wName, margin);
		fdDBType.left   = new FormAttachment(middle, margin);
		fdDBType.bottom = new FormAttachment(80, 0);
		fdDBType.right  = new FormAttachment(100,0);
		wDBType.setLayoutData(fdDBType);
		wDBType.addSelectionListener
			(
				new SelectionAdapter()
				{
					public void widgetSelected(SelectionEvent e)
					{
						setAccessTypes();
						setPageComplete(false);
					}
				}
			);
		
		wlAccType = new Label(composite, SWT.RIGHT);
		wlAccType.setText("Type of database access to use");
 		props.setLook(wlAccType);
		fdlAccType = new FormData();
		fdlAccType.left   = new FormAttachment(0, 0);
		fdlAccType.top    = new FormAttachment(wDBType, margin);
		fdlAccType.right  = new FormAttachment(middle, 0);
		wlAccType.setLayoutData(fdlAccType);
		
		wAccType = new List(composite, SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
 		props.setLook(wAccType);
		fdAccType = new FormData();
		fdAccType.top    = new FormAttachment(wDBType, margin);
		fdAccType.left   = new FormAttachment(middle, margin);
		fdAccType.bottom = new FormAttachment(100, 0);
		fdAccType.right  = new FormAttachment(100, 0);
		wAccType.setLayoutData(fdAccType);
		wAccType.addSelectionListener
			(
				new SelectionAdapter()
				{
					public void widgetSelected(SelectionEvent e)
					{
						setPageComplete(false);
					}
				}
			);
		
		setAccessTypes();
		
		// set the composite as the control for this page
		setControl(composite);
	}
	
	public void setAccessTypes()
	{
		if (wDBType.getSelectionCount()<1) return;
		
		int acc[] = DatabaseMeta.getAccessTypeList(wDBType.getSelection()[0]);
		wAccType.removeAll();
		for (int i=0;i<acc.length;i++)
		{
			wAccType.add( DatabaseMeta.getAccessTypeDescLong(acc[i]) );
		}
		// If nothing is selected: select the first item (mostly the native driver)
		if (wAccType.getSelectionIndex()<0) 
		{
			wAccType.select(0);
		}
	}
	
	public boolean canFlipToNextPage()
	{
		String name = wName.getText()!=null?wName.getText().length()>0?wName.getText():null:null;
		String dbType = wDBType.getSelection().length==1?wDBType.getSelection()[0]:null;
		String acType = wAccType.getSelection().length==1?wAccType.getSelection()[0]:null;
		
		if (name==null || dbType==null || acType==null)
		{
			setErrorMessage("Enter the name of the connection, the database type and the access method.");
			return false;
		}
		if (name!=null && Const.findDatabase(databases, name)!=null)
		{
			setErrorMessage("Database '"+name+"' already exists, please choose another name.");
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
		if (wName.getText()!=null && wName.getText().length()>0) 
		{
			info.setName(wName.getText());
		}
		
		String dbTypeSel[] = wDBType.getSelection();
		if (dbTypeSel!=null && dbTypeSel.length==1)
		{
			info.setDatabaseType(dbTypeSel[0]);
		}
		
		String accTypeSel[] = wAccType.getSelection();
		if (accTypeSel!=null && accTypeSel.length==1)
		{
			info.setAccessType(DatabaseMeta.getAccessType(accTypeSel[0]));
		}
		
		// Also, set the default port in case of JDBC:
		info.setDBPort(info.getDefaultDatabasePort());
		
		return info;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.WizardPage#getNextPage()
	 */
	public IWizardPage getNextPage()
	{
		IWizard wiz = getWizard();
		
		IWizardPage nextPage;
		switch(info.getAccessType())
		{
		case DatabaseMeta.TYPE_ACCESS_OCI:
			nextPage = wiz.getPage("oci"); // OCI
			break;
		case DatabaseMeta.TYPE_ACCESS_ODBC:
			nextPage = wiz.getPage("odbc");; // ODBC
			break;
		default: // Native 
			nextPage = wiz.getPage("jdbc");
			if (nextPage!=null) 
			{
				// Set the port number...
				((CreateDatabaseWizardPageJDBC)nextPage).setData();
			}
			break;
		}
		
		return nextPage;
	}
	
	public boolean canPerformFinish()
	{
		return false;
	}
}
