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


package org.pentaho.di.ui.core.runconfiguration.impl;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.pentaho.di.base.AbstractMeta;
import org.pentaho.di.core.runconfiguration.api.RunConfiguration;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.ui.core.ConstUI;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.widget.tree.TreeNode;
import org.pentaho.di.ui.spoon.tree.TreeFolderProvider;

/**
 * Created by bmorrise on 7/6/18.
 */
public class RunConfigurationFolderProvider extends TreeFolderProvider {

  private static final Class<?> PKG = RunConfigurationViewTreeExtension.class;
  public static String STRING_RUN_CONFIGURATIONS = BaseMessages.getString( PKG, "RunConfigurationTree.Title" );

  private RunConfigurationDelegate runConfigurationDelegate;

  public RunConfigurationFolderProvider( RunConfigurationDelegate runConfigurationDelegate ) {
    this.runConfigurationDelegate = runConfigurationDelegate;
  }

  @Override
  public void refresh( AbstractMeta meta, TreeNode treeNode, String filter ) {
    GUIResource guiResource = GUIResource.getInstance();
    for ( RunConfiguration runConfiguration : runConfigurationDelegate.load() ) {
      if ( !filterMatch( runConfiguration.getName(), filter ) ) {
        continue;
      }
      String imageFile = runConfiguration.isReadOnly() ? "images/run_tree_disabled.svg" : "images/run_tree.svg";
      TreeNode childTreeNode = createChildTreeNode( treeNode, runConfiguration.getName(), getRunConfigurationImage(
              guiResource, imageFile ) );
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

  private TreeNode createChildTreeNode( TreeNode parent, String text, Image image ) {
    return super.createTreeNode( parent, text, image );
  }

  @Override
  public TreeNode createTreeNode( TreeNode parent, String text, Image image ) {
    TreeNode treeNode = super.createTreeNode( parent, text, image );
    treeNode.setIndex( 0 );
    return treeNode;
  }
}
