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

import org.pentaho.di.base.HasNamedConfigurationsInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.gui.UndoInterface;
import org.pentaho.di.core.namedconfig.ConfigurationTemplateManager;
import org.pentaho.di.core.namedconfig.model.NamedConfiguration;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.namedconfig.dialog.NamedConfigurationDialog;
import org.pentaho.di.ui.spoon.Spoon;

public class SpoonNamedConfigurationDelegate extends SpoonDelegate {
  private static Class<?> PKG = Spoon.class; // for i18n purposes, needed by Translator2!!

  public SpoonNamedConfigurationDelegate( Spoon spoon ) {
    super( spoon );
  }

  public void delNamedConfiguration( HasNamedConfigurationsInterface hasNamedConfigurationsInterface, NamedConfiguration configuration ) {
    Repository rep = spoon.getRepository();

    if ( rep != null && configuration.getObjectId() != null ) {
      // remove the slave server from the repository too...
      //rep.deleteNamedConfiguration( configuration.getObjectId() );
    }

    int idx = hasNamedConfigurationsInterface.getNamedConfigurations().indexOf( configuration );
    hasNamedConfigurationsInterface.getNamedConfigurations().remove( idx );
    spoon.refreshTree();
  }
  
  public void editNamedConfiguration( HasNamedConfigurationsInterface hasNamedConfigurationsInterface, NamedConfiguration configuration ) {
    if ( hasNamedConfigurationsInterface == null && spoon.rep == null ) {
      return;
    }
    
    NamedConfigurationDialog namedConfigurationDialog = new NamedConfigurationDialog( spoon.getShell() , configuration);
    String result = namedConfigurationDialog.open();
  }

  public void newNamedConfiguration( HasNamedConfigurationsInterface hasNamedConfigurationsInterface ) {
    if ( hasNamedConfigurationsInterface == null && spoon.rep == null ) {
      return;
    }
    
    List<NamedConfiguration> configurations = ConfigurationTemplateManager.getInstance().getConfigurationTemplates( "hadoop-cluster" );
    NamedConfiguration configuration = configurations.get( 0 );   
    
    NamedConfigurationDialog namedConfigurationDialog = new NamedConfigurationDialog( spoon.getShell() , configuration);
    String result = namedConfigurationDialog.open();
    
    if ( result != null ) {
      if ( hasNamedConfigurationsInterface instanceof VariableSpace ) {
        configuration.shareVariablesWith( (VariableSpace) hasNamedConfigurationsInterface );
      } else {
        configuration.initializeVariablesFrom( null );
      }
  
      hasNamedConfigurationsInterface.addNamedConfiguration( configuration );
      spoon.addUndoNew( (UndoInterface) hasNamedConfigurationsInterface, new NamedConfiguration[] { (NamedConfiguration) configuration.clone() }, 
          new int[] { hasNamedConfigurationsInterface.indexOfNamedConfiguration( configuration ) } );
      if ( spoon.rep != null ) {
        try {
          if ( !spoon.rep.getSecurityProvider().isReadOnly() ) {
            spoon.rep.save( configuration, Const.VERSION_COMMENT_INITIAL_VERSION, null );
          } else {
            throw new KettleException( BaseMessages.getString(
              PKG, "Spoon.Dialog.Exception.ReadOnlyRepositoryUser" ) );
          }
        } catch ( KettleException e ) {
          new ErrorDialog( spoon.getShell(),
            BaseMessages.getString( PKG, "Spoon.Dialog.ErrorSavingNamedConfiguration.Title" ),
            BaseMessages.getString( PKG, "Spoon.Dialog.ErrorSavingNamedConfiguration.Message", configuration.getName() ), e );
        }
      }
      spoon.refreshTree();    
    }
  }
}
