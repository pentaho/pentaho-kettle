/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.di.ui.core.runconfiguration;

import org.pentaho.di.base.AbstractMeta;
import org.pentaho.di.core.runconfiguration.api.RunConfiguration;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.extension.ExtensionPoint;
import org.pentaho.di.core.extension.ExtensionPointInterface;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.runconfiguration.api.RunConfigurationService;
import org.pentaho.di.core.runconfiguration.impl.RunConfigurationManager;
import org.pentaho.di.core.runconfiguration.impl.pentaho.DefaultRunConfigurationProvider;
import org.pentaho.di.ui.spoon.TreeSelection;
import org.pentaho.di.ui.spoon.delegates.SpoonTreeDelegateExtension;

import java.util.List;

/**
 * Created by bmorrise on 3/14/17.
 */
@ExtensionPoint( id = "RunConfigurationTreeDelegateExtension", description = "",
  extensionPointId = "SpoonTreeDelegateExtension" )
public class RunConfigurationTreeDelegateExtension implements ExtensionPointInterface {

  private RunConfigurationService runConfigurationManager = RunConfigurationManager.getInstance();

  @Override public void callExtensionPoint( LogChannelInterface log, Object extension ) throws KettleException {
    SpoonTreeDelegateExtension treeDelExt = (SpoonTreeDelegateExtension) extension;

    int caseNumber = treeDelExt.getCaseNumber();
    AbstractMeta meta = treeDelExt.getTransMeta();
    String[] path = treeDelExt.getPath();
    List<TreeSelection> objects = treeDelExt.getObjects();

    TreeSelection object = null;

    if ( path[2].equals( RunConfigurationViewTreeExtension.TREE_LABEL ) ) {
      switch ( caseNumber ) {
        case 3:
          object = new TreeSelection( path[2], RunConfiguration.class, meta );
          break;
        case 4:
          try {
            final String name = path[ 3 ];
            if ( !name.equalsIgnoreCase( DefaultRunConfigurationProvider.DEFAULT_CONFIG_NAME ) ) {
              object = new TreeSelection( path[ 3 ], path[ 3 ], meta );
            }
          } catch ( Exception e ) {
            // Do Nothing
          }
          break;
      }
    }

    if ( object != null ) {
      objects.add( object );
    }
  }
}
