/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2013 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.ui.spoon.delegates;

import java.util.List;

import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.base.HasNamedConfigurationsInterface;
import org.pentaho.di.core.gui.UndoInterface;
import org.pentaho.di.core.namedconfig.NamedConfigurationManager;
import org.pentaho.di.core.namedconfig.model.NamedConfiguration;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.namedconfig.dialog.NamedConfigurationDialog;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.metastore.api.exceptions.MetaStoreException;

public class SpoonNamedConfigurationDelegate extends SpoonDelegate {
  private static Class<?> PKG = Spoon.class; // for i18n purposes, needed by Translator2!!

  public SpoonNamedConfigurationDelegate( Spoon spoon ) {
    super( spoon );
  }

  public void delNamedConfiguration( HasNamedConfigurationsInterface hasNamedConfigurationsInterface, NamedConfiguration configuration ) {
    int pos = hasNamedConfigurationsInterface.indexOfNamedConfiguration( configuration );

    deleteNamedConfiguration( hasNamedConfigurationsInterface, configuration );
      
    spoon.addUndoDelete(
      (UndoInterface) hasNamedConfigurationsInterface, new NamedConfiguration[] { (NamedConfiguration) configuration.clone() },
      new int[] { pos } );

    spoon.refreshTree();
    spoon.setShellText();
  }
  
  public void editNamedConfiguration( HasNamedConfigurationsInterface hasNamedConfigurationsInterface, NamedConfiguration configuration, Shell shell ) {
    if ( hasNamedConfigurationsInterface == null && spoon.rep == null ) {
      return;
    }
    NamedConfigurationDialog namedConfigurationDialog = new NamedConfigurationDialog( shell , configuration.clone());
    String result = namedConfigurationDialog.open();
    if ( result != null ) {
      deleteNamedConfiguration( hasNamedConfigurationsInterface, configuration );
      saveNamedConfiguration( hasNamedConfigurationsInterface, namedConfigurationDialog.getNamedConfiguration() );
      
      spoon.addUndoChange( (UndoInterface) hasNamedConfigurationsInterface, new NamedConfiguration[] { configuration }, new NamedConfiguration[] { (NamedConfiguration) namedConfigurationDialog.getNamedConfiguration() }, new int[] { hasNamedConfigurationsInterface.indexOfNamedConfiguration( configuration ) } );
      spoon.refreshTree();
    }    
  }

  public void newNamedConfiguration( HasNamedConfigurationsInterface hasNamedConfigurationsInterface, Shell shell) {
    if ( hasNamedConfigurationsInterface == null && spoon.rep == null ) {
      return;
    }
    
    List<NamedConfiguration> configurations = NamedConfigurationManager.getInstance().getConfigurationTemplates( "hadoop-cluster" );
    NamedConfiguration configuration = configurations.get( 0 );
    
    NamedConfigurationDialog namedConfigurationDialog = new NamedConfigurationDialog( shell , configuration );
    String result = namedConfigurationDialog.open();
    
    if ( result != null ) {
      if ( hasNamedConfigurationsInterface instanceof VariableSpace ) {
        configuration.shareVariablesWith( (VariableSpace) hasNamedConfigurationsInterface );
      } else {
        configuration.initializeVariablesFrom( null );
      }
  
      spoon.addUndoNew( (UndoInterface) hasNamedConfigurationsInterface, new NamedConfiguration[] { (NamedConfiguration) configuration.clone() }, 
          new int[] { hasNamedConfigurationsInterface.indexOfNamedConfiguration( configuration ) } );

      saveNamedConfiguration( hasNamedConfigurationsInterface, configuration );
      
      spoon.refreshTree();    
    }
  }
  
  private void deleteNamedConfiguration( HasNamedConfigurationsInterface hasNamedConfigurationsInterface, NamedConfiguration configuration ) {
    int pos = hasNamedConfigurationsInterface.indexOfNamedConfiguration( configuration );
    Repository rep = spoon.getRepository();
    hasNamedConfigurationsInterface.removeNamedConfiguration( pos );
    if ( rep != null ) {
      try {
        NamedConfigurationManager.getInstance().delete( configuration.getName(), rep.getMetaStore() );
      } catch (MetaStoreException e) {
        new ErrorDialog( spoon.getShell(),
            BaseMessages.getString( PKG, "Spoon.Dialog.ErrorDeletingNamedConfiguration.Title" ),
            BaseMessages.getString( PKG, "Spoon.Dialog.ErrorDeletingNamedConfiguration.Message", configuration.getName() ), e );
      }
    }    
  }
  
  private void saveNamedConfiguration( HasNamedConfigurationsInterface hasNamedConfigurationsInterface, NamedConfiguration configuration ) {
    Repository rep = spoon.getRepository();
    hasNamedConfigurationsInterface.addOrReplaceNamedConfiguration( configuration );
    if ( rep != null ) {
      try {
        NamedConfigurationManager.getInstance().create( configuration, rep.getMetaStore() );
      } catch (MetaStoreException e) {
        new ErrorDialog( spoon.getShell(),
            BaseMessages.getString( PKG, "Spoon.Dialog.ErrorSavingNamedConfiguration.Title" ),
            BaseMessages.getString( PKG, "Spoon.Dialog.ErrorSavingNamedConfiguration.Message", configuration.getName() ), e );
      }
    }      
  }  
  
}
