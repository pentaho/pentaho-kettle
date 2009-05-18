 /* Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.*/
 

/*
 * Created on 17-apr-04
 *
 */

package org.pentaho.di.ui.trans.steps.fixedinput;

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
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.steps.fixedinput.FixedFileInputField;
import org.pentaho.di.trans.steps.fixedinput.FixedInputMeta;
import org.pentaho.di.ui.core.PropsUI;

public class FixedFileImportWizardPage1 extends WizardPage // implements Listener 
{
	private static Class<?> PKG = FixedInputMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private FixedTableDraw wTable;
	private FormData fdTable;

	private PropsUI props;
	private List<String> rows;
	private List<FixedFileInputField> fields;
	
	public FixedFileImportWizardPage1(String arg, PropsUI props, List<String> rows, List<FixedFileInputField> fields)
	{
		super(arg);
		this.props=props;
		this.rows=rows;
		this.fields=fields;
		
		setTitle(BaseMessages.getString(PKG, "FixedFileImportWizardPage1.DialogTitle"));
		setDescription(BaseMessages.getString(PKG, "FixedFileImportWizardPage1.DialogMessage"));
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
			setErrorMessage(BaseMessages.getString(PKG, "FixedFileImportWizardPage1.ErrorMarkerNeeded"));
			return false;
		}
	}	
	
	public int getSize()
	{
		return wTable.getFields().size();
	}
}
