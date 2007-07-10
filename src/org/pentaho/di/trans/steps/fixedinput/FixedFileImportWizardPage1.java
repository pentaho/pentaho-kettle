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
 

/*
 * Created on 17-apr-04
 *
 */

package org.pentaho.di.trans.steps.fixedinput;

import java.util.List;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Props;


public class FixedFileImportWizardPage1 extends WizardPage // implements Listener 
{
	private FixedTableDraw wTable;
	private FormData fdTable;

	private Props props;
	private List<String> rows;
	private List<FixedFileInputField> fields;
	
	public FixedFileImportWizardPage1(String arg, Props props, List<String> rows, List<FixedFileInputField> fields)
	{
		super(arg);
		this.props=props;
		this.rows=rows;
		this.fields=fields;
		
		setTitle(Messages.getString("FixedFileImportWizardPage1.DialogTitle"));
		setDescription(Messages.getString("FixedFileImportWizardPage1.DialogMessage"));
	}
	
	public void createControl(Composite parent)
	{
		// create the composite to hold the widgets
		Composite composite = new Composite(parent, SWT.NONE);
 		props.setLook(composite);
	    
	    FormLayout compLayout = new FormLayout();
	    compLayout.marginHeight = Const.FORM_MARGIN;
	    compLayout.marginWidth  = Const.FORM_MARGIN;
		composite.setLayout(compLayout);

		MouseAdapter lsMouse = new MouseAdapter()
  			{
				public void mouseDown(MouseEvent e) 
				{
					int s = getSize();
					// System.out.println("size = "+s);
					setPageComplete(s>0);
				}
			};
	
		wTable=new FixedTableDraw(composite, props, this, fields);
		wTable.setRows(rows);
 		props.setLook(wTable);
		wTable.setFields(fields);
		fdTable=new FormData();
		fdTable.left   = new FormAttachment(0, 0);
		fdTable.right  = new FormAttachment(100, 0);
		fdTable.top    = new FormAttachment(0, 0);
		fdTable.bottom = new FormAttachment(100, 0);
		wTable.setLayoutData(fdTable);
        wTable.addMouseListener(lsMouse);
			
		// set the composite as the control for this page
		setControl(composite);
	}
	
	public void setFields(List<FixedFileInputField> fields)
	{
		wTable.setFields(fields);
	}
	
	public List<FixedFileInputField> getFields()
	{
		return wTable.getFields();
	}
	
	public boolean canFlipToNextPage()
	{
		int size = getSize();
		if (size>0) 
		{
			setErrorMessage(null);
			return true;
		} 
		else
		{
			setErrorMessage(Messages.getString("FixedFileImportWizardPage1.ErrorMarkerNeeded"));
			return false;
		}
	}	
	
	public int getSize()
	{
		return wTable.getFields().size();
	}
}
