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

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.pentaho.di.base.AbstractMeta;
import org.pentaho.di.core.bowl.Bowl;
import org.pentaho.di.engine.configuration.api.RunConfiguration;
import org.pentaho.di.engine.configuration.impl.pentaho.DefaultRunConfigurationProvider;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.ui.core.ConstUI;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.widget.tree.LeveledTreeNode;
import org.pentaho.di.ui.core.widget.tree.TreeNode;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.ui.spoon.tree.TreeFolderProvider;

import java.util.HashSet;
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
    String levelName;
    if ( !currentBowl.equals( globalBowl ) ) {
      RunConfigurationDelegate bowlDelegate =
        RunConfigurationDelegate.getInstance( () -> currentBowl.getMetastore() );
      for ( RunConfiguration runConfiguration : bowlDelegate.load() ) {
        if ( !filterMatch( runConfiguration.getName(), filter ) ) {
          continue;
        }
        if ( DefaultRunConfigurationProvider.DEFAULT_CONFIG_NAME.equals( runConfiguration.getName() ) ) {
          continue;
        }
        bowlNames.add( runConfiguration.getName() );
        String imageFile = runConfiguration.isReadOnly() ? "images/run_tree_disabled.svg" : "images/run_tree.svg";
        TreeNode childTreeNode = createChildTreeNode( treeNode, runConfiguration.getName(), getRunConfigurationImage(
                guiResource, imageFile ), LeveledTreeNode.LEVEL.PROJECT, currentBowl.getLevelDisplayName(), false );
        if ( runConfiguration.isReadOnly() ) {
          childTreeNode.setForeground( getDisabledColor() );
        }
      }
    }

    RunConfigurationDelegate globalDelegate =
      RunConfigurationDelegate.getInstance( () -> globalBowl.getMetastore() );

    for ( RunConfiguration runConfiguration : globalDelegate.load() ) {
      if ( !filterMatch( runConfiguration.getName(), filter ) ) {
        continue;
      }
      LeveledTreeNode.LEVEL level = LeveledTreeNode.LEVEL.GLOBAL;
      levelName = globalBowl.getLevelDisplayName();

      if ( DefaultRunConfigurationProvider.DEFAULT_CONFIG_NAME.equals( runConfiguration.getName() ) ) {
        level = LeveledTreeNode.LEVEL.DEFAULT;
        levelName = LeveledTreeNode.LEVEL_DEFAULT_DISPLAY_NAME;
      }

      String imageFile = runConfiguration.isReadOnly() ? "images/run_tree_disabled.svg" : "images/run_tree.svg";
      TreeNode childTreeNode = createChildTreeNode( treeNode, runConfiguration.getName(), getRunConfigurationImage(
              guiResource, imageFile ), level, levelName, containsIgnoreCase( bowlNames, runConfiguration.getName() ) );
      if ( runConfiguration.isReadOnly() ) {
        childTreeNode.setForeground( getDisabledColor() );
      }
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
}
