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


package org.pentaho.di.engine.ui;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.extension.ExtensionPoint;
import org.pentaho.di.core.extension.ExtensionPointInterface;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.engine.configuration.api.RunConfiguration;
import org.pentaho.di.engine.configuration.impl.pentaho.DefaultRunConfigurationProvider;
import org.pentaho.di.ui.core.widget.tree.LeveledTreeNode;
import org.pentaho.di.ui.spoon.TreeSelection;
import org.pentaho.di.ui.spoon.delegates.SpoonTreeDelegateExtension;

import java.util.List;
import org.eclipse.swt.widgets.TreeItem;

/**
 * Created by bmorrise on 3/14/17.
 */
@ExtensionPoint( id = "RunConfigurationTreeDelegateExtension", description = "",
  extensionPointId = "SpoonTreeDelegateExtension" )
public class RunConfigurationTreeDelegateExtension implements ExtensionPointInterface {

  @Override public void callExtensionPoint( LogChannelInterface log, Object extension ) throws KettleException {
    SpoonTreeDelegateExtension treeDelExt = (SpoonTreeDelegateExtension) extension;

    int caseNumber = treeDelExt.getCaseNumber();
    String[] path = treeDelExt.getPath();
    List<TreeSelection> objects = treeDelExt.getObjects();

    TreeSelection object = null;

    if ( path[1].equals( RunConfigurationViewTreeExtension.TREE_LABEL ) ) {
      switch ( caseNumber ) {
        case 2:
          object = new TreeSelection( path[1], RunConfiguration.class );
          break;
        case 3:
          try {
            TreeItem treeItem = treeDelExt.getTreeItem();
            String name = LeveledTreeNode.getName( treeItem );
            LeveledTreeNode.LEVEL level = LeveledTreeNode.getLevel( treeItem );

            if ( !name.equalsIgnoreCase( DefaultRunConfigurationProvider.DEFAULT_CONFIG_NAME ) ) {
              object = new TreeSelection( treeItem, name, new RunConfigurationTreeItem( name, level ) );
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
