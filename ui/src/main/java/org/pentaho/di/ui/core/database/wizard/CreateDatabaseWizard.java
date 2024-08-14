/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.ui.core.database.wizard;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.plugins.DatabasePluginType;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.gui.GUIResource;

/**
 * Shows a wizard that creates a new database connection... (Code 'normalized' from Spoon)
 *
 * @author Matt, Jens
 * @since 29-mar-2006
 *
 */
public class CreateDatabaseWizard {

  private boolean wizardFinished = false; // true when wizard finished

  private List<WizardPage> additionalPages = new ArrayList<>();

  /**
   * Shows a wizard that creates a new database connection...
   *
   * @param shell
   * @param props
   * @param databases
   * @return DatabaseMeta when finished or null when canceled
   */
  public DatabaseMeta createAndRunDatabaseWizard( Shell shell, PropsUI props, List<DatabaseMeta> databases ) {

    DatabaseMeta newDBInfo = new DatabaseMeta();

    final CreateDatabaseWizardPage1 page1 = new CreateDatabaseWizardPage1( "1", props, newDBInfo, databases );

    final CreateDatabaseWizardPageInformix pageifx =
      new CreateDatabaseWizardPageInformix( "ifx", props, newDBInfo );

    final CreateDatabaseWizardPageJDBC pagejdbc = new CreateDatabaseWizardPageJDBC( "jdbc", props, newDBInfo );

    final CreateDatabaseWizardPageOCI pageoci = new CreateDatabaseWizardPageOCI( "oci", props, newDBInfo );

    final CreateDatabaseWizardPageOracle pageoracle =
      new CreateDatabaseWizardPageOracle( "oracle", props, newDBInfo );

    final CreateDatabaseWizardPageGeneric pageGeneric =
      new CreateDatabaseWizardPageGeneric( "generic", props, newDBInfo );

    final CreateDatabaseWizardPage2 page2 = new CreateDatabaseWizardPage2( "2", props, newDBInfo );

    for ( PluginInterface pluginInterface : PluginRegistry.getInstance().getPlugins( DatabasePluginType.class ) ) {
      try {
        Object plugin = PluginRegistry.getInstance().loadClass( pluginInterface );
        if ( plugin instanceof WizardPageFactory ) {
          WizardPageFactory factory = (WizardPageFactory) plugin;
          additionalPages.add( factory.createWizardPage( props, newDBInfo ) );
        }
      } catch ( KettlePluginException kpe ) {
        // Don't do anything
      }
    }

    wizardFinished = false; // set to false for safety only

    Wizard wizard = new Wizard() {
      /**
       * @see org.eclipse.jface.wizard.Wizard#performFinish()
       */
      public boolean performFinish() {
        wizardFinished = true;
        return true;
      }

      /**
       * @see org.eclipse.jface.wizard.Wizard#canFinish()
       */
      public boolean canFinish() {
        return page2.canFinish();
      }
    };

    wizard.addPage( page1 );
    wizard.addPage( pageoci );
    wizard.addPage( pagejdbc );
    wizard.addPage( pageoracle );
    wizard.addPage( pageifx );
    wizard.addPage( pageGeneric );
    for ( WizardPage page : additionalPages ) {
      wizard.addPage( page );
    }
    wizard.addPage( page2 );

    WizardDialog wd = new WizardDialog( shell, wizard );
    WizardDialog.setDefaultImage( GUIResource.getInstance().getImageWizard() );
    wd.setMinimumPageSize( 700, 400 );
    wd.updateSize();
    wd.open();

    if ( !wizardFinished ) {
      newDBInfo = null;
    }
    return newDBInfo;
  }

}
