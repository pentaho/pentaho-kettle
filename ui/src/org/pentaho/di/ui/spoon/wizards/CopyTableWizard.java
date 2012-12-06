/*******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.ui.spoon.wizards;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardContainer;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.core.logging.LoggingObjectType;
import org.pentaho.di.core.logging.SimpleLoggingObject;
import org.pentaho.di.i18n.BaseMessages;

/**
 * This wizard helps you create a transformation that copies a table from one database to another.
 * 
 * @since 29-mar-05
 * @author Matt
 *
 */
public class CopyTableWizard implements IWizard
{
    private static Class<?> PKG = CopyTableWizard.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

    public static final LoggingObjectInterface loggingObject = new SimpleLoggingObject("Copy tables wizard", LoggingObjectType.SPOON, null);
    
	public CopyTableWizard()
	{
		super();
	}
	
	// Adds any last-minute pages to this wizard.
	public void addPages()
	{ 
	} 
	
	// Returns whether this wizard could be finished without further user interaction.
	public boolean canFinish()
	{
		return false;
	} 
			  
	// Creates this wizard's controls in the given parent control. 
	public void createPageControls(Composite pageContainer)
	{
	}
	 
	// Disposes of this wizard and frees all SWT resources. 
	public void dispose() 
	{
	}
	
	// Returns the container of this wizard. 
	public IWizardContainer getContainer() 
	{
		return null;
	}

	// Returns the default page image for this wizard. 
	public Image getDefaultPageImage() 
	{
		return null;
	}
	
	// Returns the dialog settings for this wizard. 
	public IDialogSettings getDialogSettings() 
	{
		return null;
	}
	
	// Returns the successor of the given page. 
	public IWizardPage getNextPage(IWizardPage page) 
	{
		return null;
	}
	
	// Returns the wizard page with the given name belonging to this wizard. 
	public IWizardPage getPage(String pageName) 
	{
		return null;
	}
	
	// Returns the number of pages in this wizard. 
	public int getPageCount() 
	{
		return 3;
	}
	
	// Returns all the pages in this wizard. 
	public IWizardPage[] getPages() 
	{
		return null;
	}
	
	// Returns the predecessor of the given page. 
	public IWizardPage getPreviousPage(IWizardPage page) 
	{
		return null;
	}
	
	// Returns the first page to be shown in this wizard. 
	public IWizardPage getStartingPage() 
	{
		return null;
	}
	
	// Returns the title bar color for this wizard. 
	public RGB getTitleBarColor() 
	{
		return null;
	}
	
	// Returns the window title string for this wizard. 
	public String getWindowTitle() 
	{
		return BaseMessages.getString(PKG, "CopyTableWizard.DialogTitle"); //$NON-NLS-1$
	}
	
	// Returns whether help is available for this wizard. 
	public boolean isHelpAvailable() 
	{
		return false;
	}
	
	// Returns whether this wizard needs Previous and Next buttons. 
	public boolean needsPreviousAndNextButtons() 
	{
		return true;
	}
	
	//Returns whether this wizard needs a progress monitor. 
	public boolean needsProgressMonitor() 
	{
		return false;
	}
	
	// Performs any actions appropriate in response to the user having pressed the Cancel button, or refuse if canceling now is not permitted. 
	public boolean performCancel() 
	{
		return false;
	}
	
	// Performs any actions appropriate in response to the user having pressed the Finish button, or refuse if finishing now is not permitted. 
	public boolean performFinish() 
	{
		return false;
	}
	
	// Sets or clears the container of this wizard. 
	public void setContainer(IWizardContainer wizardContainer) 
	{
	}
	
}
