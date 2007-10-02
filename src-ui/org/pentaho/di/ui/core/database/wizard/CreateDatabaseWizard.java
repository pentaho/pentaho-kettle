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

package org.pentaho.di.ui.core.database.wizard;

import java.util.List;

import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.ui.core.database.wizard.CreateDatabaseWizardPage1;
import org.pentaho.di.ui.core.database.wizard.CreateDatabaseWizardPage2;
import org.pentaho.di.ui.core.database.wizard.CreateDatabaseWizardPageGeneric;
import org.pentaho.di.ui.core.database.wizard.CreateDatabaseWizardPageInformix;
import org.pentaho.di.ui.core.database.wizard.CreateDatabaseWizardPageJDBC;
import org.pentaho.di.ui.core.database.wizard.CreateDatabaseWizardPageOCI;
import org.pentaho.di.ui.core.database.wizard.CreateDatabaseWizardPageODBC;
import org.pentaho.di.ui.core.database.wizard.CreateDatabaseWizardPageOracle;
import org.pentaho.di.ui.core.database.wizard.CreateDatabaseWizardPageSAPR3;
import org.pentaho.di.ui.core.PropsUI;



/**
 * Shows a wizard that creates a new database connection...
 * (Code 'normalized' from Spoon)
 * 
 * @author Matt, Jens
 * @since  29-mar-2006
 *
 */
public class CreateDatabaseWizard {


	private boolean wizardFinished=false; // true when wizard finished

    /**
     * Shows a wizard that creates a new database connection...
     * @param shell
     * @param props
     * @param databases
     * @return DatabaseMeta when finished or null when canceled
     */
    public DatabaseMeta createAndRunDatabaseWizard(Shell shell, PropsUI props, List<DatabaseMeta> databases)
    {

        DatabaseMeta newDBInfo = new DatabaseMeta();
        
        final CreateDatabaseWizardPage1 page1 = new CreateDatabaseWizardPage1("1", props, newDBInfo, databases);
        page1.createControl(shell);
        
        final  CreateDatabaseWizardPageInformix pageifx = new CreateDatabaseWizardPageInformix("ifx", props, newDBInfo);
        pageifx.createControl(shell);
        
        final  CreateDatabaseWizardPageJDBC pagejdbc = new CreateDatabaseWizardPageJDBC("jdbc", props, newDBInfo);
        pagejdbc.createControl(shell);
        
        final  CreateDatabaseWizardPageOCI pageoci = new CreateDatabaseWizardPageOCI("oci", props, newDBInfo);
        pageoci.createControl(shell);
        
        final CreateDatabaseWizardPageODBC pageodbc = new CreateDatabaseWizardPageODBC("odbc", props, newDBInfo);
        pageodbc.createControl(shell);
        
        final CreateDatabaseWizardPageOracle pageoracle = new CreateDatabaseWizardPageOracle("oracle", props, newDBInfo);
        
        final CreateDatabaseWizardPageSAPR3 pageSAPR3 = new CreateDatabaseWizardPageSAPR3("SAPR3", props, newDBInfo);
        pageSAPR3.createControl(shell);
        
        final CreateDatabaseWizardPageGeneric pageGeneric = new CreateDatabaseWizardPageGeneric("generic", props, newDBInfo);
        pageGeneric.createControl(shell);
        
        final CreateDatabaseWizardPage2 page2 = new CreateDatabaseWizardPage2("2", props, newDBInfo);
        page2.createControl(shell);

    	wizardFinished=false; // set to false for safety only
    	
        Wizard wizard = new Wizard() 
        {
            /**
             * @see org.eclipse.jface.wizard.Wizard#performFinish()
             */
        	public boolean performFinish() 
            {
            	wizardFinished=true;
                return true;
            }
            
            /**
             * @see org.eclipse.jface.wizard.Wizard#canFinish()
             */
            public boolean canFinish()
            {
                return page2.canFinish();
            }
        };
                
        wizard.addPage(page1);        
        wizard.addPage(pageoci);
        wizard.addPage(pageodbc);
        wizard.addPage(pagejdbc);
        wizard.addPage(pageoracle);
        wizard.addPage(pageifx);
        wizard.addPage(pageSAPR3);
        wizard.addPage(pageGeneric);
        wizard.addPage(page2);
                
        WizardDialog wd = new WizardDialog(shell, wizard);
        wd.setMinimumPageSize(700,400);
        wd.updateSize();
        wd.open();
        
        if(!wizardFinished){
        	newDBInfo=null;
        }
        return newDBInfo;
    }


	
}
