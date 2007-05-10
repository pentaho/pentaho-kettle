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

package org.pentaho.di.trans.steps.textfileinput;

import java.util.ArrayList;
import java.util.Vector;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.Props;
import be.ibridge.kettle.core.widget.TableDraw;


public class TextFileImportWizardPage1 extends WizardPage // implements Listener 
{
	private TableDraw wTable;
	private FormData fdTable;

	private Props props;
	private ArrayList rows;
	private Vector fields;
	
	public TextFileImportWizardPage1(String arg, Props props, ArrayList rows, Vector fields)
	{
		super(arg);
		this.props=props;
		this.rows=rows;
		this.fields=fields;
		
		setTitle(Messages.getString("TextFileImportWizardPage1.DialogTitle"));
		setDescription(Messages.getString("TextFileImportWizardPage1.DialogMessage"));
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
	
		wTable=new TableDraw(composite, props, this, fields);
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
	
	public void setFields(Vector fields)
	{
		wTable.setFields(fields);
	}
	
	public Vector getFields()
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
			setErrorMessage(Messages.getString("TextFileImportWizardPage1.ErrorMarkerNeeded"));
			return false;
		}
	}	
	
	public int getSize()
	{
		return wTable.getFields().size();
	}
}
