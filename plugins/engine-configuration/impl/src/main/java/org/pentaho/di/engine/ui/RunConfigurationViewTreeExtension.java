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

import org.eclipse.swt.widgets.TreeItem;
import org.pentaho.di.core.bowl.Bowl;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.extension.ExtensionPoint;
import org.pentaho.di.core.extension.ExtensionPointInterface;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.engine.configuration.api.RunConfiguration;
import org.pentaho.di.engine.configuration.api.CheckedMetaStoreSupplier;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.ui.core.widget.tree.LeveledTreeNode;
import org.pentaho.di.ui.spoon.SelectionTreeExtension;
import org.pentaho.di.ui.spoon.Spoon;

@ExtensionPoint( id = "RunConfigurationViewTreeExtension", description = "Refreshes execution environment subtree",
  extensionPointId = "SpoonViewTreeExtension" )
public class RunConfigurationViewTreeExtension implements ExtensionPointInterface {

  private static final Class<?> PKG = RunConfigurationViewTreeExtension.class;
  public static String TREE_LABEL = BaseMessages.getString( PKG, "RunConfigurationTree.Title" );

  @Override public void callExtensionPoint( LogChannelInterface log, Object object ) throws KettleException {
    SelectionTreeExtension selectionTreeExtension = (SelectionTreeExtension) object;
    if ( selectionTreeExtension.getAction().equals( Spoon.EDIT_SELECTION_EXTENSION ) ) {
      if ( selectionTreeExtension.getSelection() instanceof RunConfigurationTreeItem ) {
        TreeItem treeItem = selectionTreeExtension.getTreeItem();
        String name = LeveledTreeNode.getName( treeItem );
        LeveledTreeNode.LEVEL level = LeveledTreeNode.getLevel( treeItem );

        Bowl bowl;
        if ( level.equals( LeveledTreeNode.LEVEL.GLOBAL ) ) {
          bowl = Spoon.getInstance().getGlobalManagementBowl();
        } else {
          bowl = Spoon.getInstance().getManagementBowl();
        }
        RunConfigurationDelegate runConfigurationDelegate =
          RunConfigurationDelegate.getInstance( () -> bowl.getMetastore() );

        runConfigurationDelegate.edit( runConfigurationDelegate.load( name ) );
      }
    } else if ( selectionTreeExtension.getAction().equals( Spoon.CREATE_NEW_SELECTION_EXTENSION ) ) {
      if ( selectionTreeExtension.getSelection().equals( RunConfiguration.class ) ) {
        // Create new RunConfiguration
        Bowl bowl = Spoon.getInstance().getManagementBowl();
        CheckedMetaStoreSupplier ms = () -> bowl.getMetastore();
        RunConfigurationDelegate runConfigurationDelegate = RunConfigurationDelegate.getInstance( ms );
        runConfigurationDelegate.create();
      }
    }
  }
}
