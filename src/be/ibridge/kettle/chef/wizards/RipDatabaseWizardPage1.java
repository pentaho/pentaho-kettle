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


package be.ibridge.kettle.chef.wizards;

import java.util.ArrayList;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.List;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.Props;
import be.ibridge.kettle.core.database.DatabaseMeta;


/**
 * 
 * On page one we select the source and target databases...
 * 
 * @author Matt
 * @since  17-apr-04
 */
public class RipDatabaseWizardPage1 extends WizardPage
{
	private List     wSourceDB, wTargetDB;
	private FormData fdSourceDB, fdTargetDB;

	private Props props;
	private ArrayList databases;
	
    /** @deprecated */
    public RipDatabaseWizardPage1(String arg, Props props, ArrayList databases)
    {
        this(arg, databases);
    }

	public RipDatabaseWizardPage1(String arg, ArrayList databases)
	{
		super(arg);
		this.props=Props.getInstance();
		this.databases=databases;
		
		setTitle("Enter the source and target database");
		setDescription("Select the source and target databases.");
		
		setPageComplete(false);
	}
	
	public void createControl(Composite parent)
	{
		int margin = Const.MARGIN;
		
		// create the composite to hold the widgets
		Composite composite = new Composite(parent, SWT.NONE);
 		props.setLook(composite);
	    
	    FormLayout compLayout = new FormLayout();
	    compLayout.marginHeight = Const.FORM_MARGIN;
	    compLayout.marginWidth  = Const.FORM_MARGIN;
		composite.setLayout(compLayout);

		wSourceDB = new List(composite, SWT.SINGLE | SWT.BORDER);
 		props.setLook(wSourceDB);
		for (int i=0;i<databases.size();i++)
		{
			DatabaseMeta dbInfo = (DatabaseMeta)databases.get(i);
			wSourceDB.add(dbInfo.getName());
		}
		fdSourceDB = new FormData();
		fdSourceDB.top    = new FormAttachment(0,0);
		fdSourceDB.left   = new FormAttachment(0,0);
		fdSourceDB.bottom = new FormAttachment(100,0);
		fdSourceDB.right  = new FormAttachment(50,0);
		wSourceDB.setLayoutData(fdSourceDB);
		wSourceDB.addSelectionListener
			(
				new SelectionAdapter()
				{
					public void widgetSelected(SelectionEvent e)
					{
						setPageComplete(false);
					}
				}
			);
		
		wTargetDB = new List(composite, SWT.SINGLE | SWT.BORDER);
 		props.setLook(wTargetDB);
		for (int i=0;i<databases.size();i++)
		{
			DatabaseMeta dbInfo = (DatabaseMeta)databases.get(i);
			wTargetDB.add(dbInfo.getName());
		}
		fdTargetDB = new FormData();
		fdTargetDB.top    = new FormAttachment(0,0);
		fdTargetDB.left   = new FormAttachment(50,margin);
		fdTargetDB.bottom = new FormAttachment(100,0);
		fdTargetDB.right  = new FormAttachment(100,0);
		wTargetDB.setLayoutData(fdTargetDB);
		wTargetDB.addSelectionListener
			(
				new SelectionAdapter()
				{
					public void widgetSelected(SelectionEvent e)
					{
						setPageComplete(false);
					}
				}
			);
		
		// set the composite as the control for this page
		setControl(composite);
	}
		
	public boolean canFlipToNextPage()
	{
		DatabaseMeta source = getSourceDatabase();
		DatabaseMeta target = getTargetDatabase();
		
		if (source==null && target==null)
		{
			setErrorMessage("Select both the source and target database!");
			return false;
		}
		else
		if (source==null && target!=null)
		{
			setErrorMessage("Select the source database!");
			return false;
		}
		else
		if (source!=null && target==null)
		{
			setErrorMessage("Select the target database!");
			return false;
		}
		else
		if (source!=null && target!=null && source.equals(target))
		{
			setErrorMessage("The source and target database can't be the same!");
			return false;
		}
		else
		{
			setErrorMessage(null);
			setMessage("Select 'next' to proceed");
			return true;
		}
	}	
	
	public DatabaseMeta getSourceDatabase()
	{
		if (wSourceDB.getSelection().length==1)
		{
			String sourceDbName = wSourceDB.getSelection()[0];
			return Const.findDatabase(databases, sourceDbName);
		}
		return null;
	}
	
	public DatabaseMeta getTargetDatabase()
	{
		if (wTargetDB.getSelection().length==1)
		{
			String targetDbName = wTargetDB.getSelection()[0];
			return Const.findDatabase(databases, targetDbName);
		}
		return null;
	}	
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.WizardPage#getNextPage()
	 */
	public IWizardPage getNextPage()
	{
		RipDatabaseWizardPage2 page2 = (RipDatabaseWizardPage2)super.getNextPage();
		if (page2.getInputData())
		{
			page2.getData();
			return page2;
		}
		return this;
	}
}
