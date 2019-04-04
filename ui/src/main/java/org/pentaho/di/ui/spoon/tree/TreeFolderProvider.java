/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2018-2019 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.ui.spoon.tree;

import org.eclipse.swt.graphics.Image;
import org.pentaho.di.base.AbstractMeta;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.widget.tree.TreeNode;

/**
 * Created by bmorrise on 6/26/18.
 */
public abstract class TreeFolderProvider {

  protected TreeManager treeManager;

  public abstract void refresh( AbstractMeta meta, TreeNode treeNode, String filter );

  public Class getType() {
    return Object.class;
  }

  public void checkUpdate( AbstractMeta meta, TreeNode treeNode, String filter ) {
    if ( treeManager.shouldUpdate( meta, getTitle() ) ) {
      treeNode.removeAll();
      refresh( meta, treeNode, filter );
    }
  }

  public abstract String getTitle();

  protected boolean filterMatch( String string, String filter ) {
    return Utils.isEmpty( string ) || Utils.isEmpty( filter ) || string.toUpperCase().contains( filter.toUpperCase() );
  }

  public void create( AbstractMeta meta, TreeNode parent ) {
    refresh( meta, createTreeNode( parent, getTitle(), getTreeImage() ), null );
  }

  protected Image getTreeImage() {
    return GUIResource.getInstance().getImageFolder();
  }

  public TreeNode createTreeNode( TreeNode parent, String text, Image image ) {
    TreeNode childTreeNode = new TreeNode();
    childTreeNode.setLabel( text );
    childTreeNode.setImage( image );

    parent.addChild( childTreeNode );
    return childTreeNode;
  }

  public void setTreeManager( TreeManager treeManager ) {
    this.treeManager = treeManager;
  }
}
