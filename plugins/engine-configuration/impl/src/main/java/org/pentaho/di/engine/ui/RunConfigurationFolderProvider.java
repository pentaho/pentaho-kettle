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

import org.pentaho.di.base.AbstractMeta;
import org.pentaho.di.core.bowl.Bowl;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.engine.configuration.api.RunConfiguration;
import org.pentaho.di.engine.configuration.api.RunConfigurationProvider;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.ui.core.ConstUI;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.widget.tree.LeveledTreeNode;
import org.pentaho.di.ui.core.widget.tree.TreeNode;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.ui.spoon.tree.TreeFolderProvider;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Created by bmorrise on 7/6/18.
 */
public class RunConfigurationFolderProvider extends TreeFolderProvider {

  private static final Class<?> PKG = RunConfigurationViewTreeExtension.class;
  public static String STRING_RUN_CONFIGURATIONS = BaseMessages.getString( PKG, "RunConfigurationTree.Title" );

  public RunConfigurationFolderProvider() {
  }

  @Override
  public void refresh( Optional<AbstractMeta> meta, TreeNode treeNode, String filter ) {
    GUIResource guiResource = GUIResource.getInstance();
    Set<String> bowlNames = new HashSet<>();
    Bowl currentBowl = Spoon.getInstance().getManagementBowl();
    Bowl globalBowl = Spoon.getInstance().getGlobalManagementBowl();
    if ( !currentBowl.equals( globalBowl ) ) {
      for ( RunConfiguration runConfiguration : loadRunConfigurations( currentBowl ) ) {
        if ( filterMatch( runConfiguration.getName(), filter )
          && !RunConfigurationProvider.DEFAULT_CONFIG_NAME.equals( runConfiguration.getName() ) ) {
          bowlNames.add( runConfiguration.getName() );
          addRunConfigurationNode( treeNode, guiResource, runConfiguration,
            LeveledTreeNode.LEVEL.PROJECT, currentBowl.getLevelDisplayName(), false );
        }
      }
    }

    addGlobalRunConfigurationNodes( treeNode, filter, guiResource, bowlNames, globalBowl );
  }

  private void addGlobalRunConfigurationNodes( TreeNode treeNode, String filter, GUIResource guiResource,
                                               Set<String> bowlNames, Bowl globalBowl ) {
    for ( RunConfiguration runConfiguration : loadRunConfigurations( globalBowl ) ) {
      if ( filterMatch( runConfiguration.getName(), filter ) ) {
        boolean isDefault = RunConfigurationProvider.DEFAULT_CONFIG_NAME.equals( runConfiguration.getName() );
        LeveledTreeNode.LEVEL level = isDefault ? LeveledTreeNode.LEVEL.DEFAULT : LeveledTreeNode.LEVEL.GLOBAL;
        String levelName = isDefault ? LeveledTreeNode.LEVEL_DEFAULT_DISPLAY_NAME : globalBowl.getLevelDisplayName();

        addRunConfigurationNode( treeNode, guiResource, runConfiguration, level, levelName,
          containsIgnoreCase( bowlNames, runConfiguration.getName() ) );
      }
    }
  }

  private void addRunConfigurationNode( TreeNode treeNode, GUIResource guiResource,
                                        RunConfiguration runConfiguration, LeveledTreeNode.LEVEL level,
                                        String levelName, boolean overridden ) {
    String imageFile = runConfiguration.isReadOnly() ? "images/run_tree_disabled.svg" : "images/run_tree.svg";
    TreeNode childTreeNode = createChildTreeNode( treeNode, runConfiguration.getName(),
      getRunConfigurationImage( guiResource, imageFile ), level, levelName, overridden );

    if ( runConfiguration.isReadOnly() ) {
      childTreeNode.setForeground( getDisabledColor() );
    }
  }

  private Image getRunConfigurationImage( GUIResource guiResource, String file ) {
    return guiResource
      .getImage( file, getClass().getClassLoader(), ConstUI.MEDIUM_ICON_SIZE, ConstUI.MEDIUM_ICON_SIZE );
  }

  private Color getDisabledColor() {
    Device device = Display.getCurrent();
    return new Color( device, 188, 188, 188 );
  }

  @Override
  public String getTitle() {
    return STRING_RUN_CONFIGURATIONS;
  }

  @Override
  public Class getType() {
    return RunConfiguration.class;
  }

  public TreeNode createChildTreeNode( TreeNode parent, String name, Image image, LeveledTreeNode.LEVEL level, String levelDisplayName,
                                       boolean overridden ) {
    LeveledTreeNode childTreeNode = new LeveledTreeNode( name, level, levelDisplayName, overridden );
    childTreeNode.setImage( image );

    parent.addChild( childTreeNode );
    return childTreeNode;
  }

  @Override
  public TreeNode createTreeNode( TreeNode parent, String text, Image image ) {
    TreeNode treeNode = super.createTreeNode( parent, text, image );
    return treeNode;
  }

  private List<RunConfiguration> loadRunConfigurations( Bowl bowl ) {
    try {
      return RunConfigurationDelegate.getInstance( bowl ).load();
    } catch ( KettleException e ) {
      LogChannel.GENERAL.logError( "Unable to access run configuration delegate", e );
      return Collections.emptyList();
    }
  }
}
