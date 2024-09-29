/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2018-2021 by Hitachi Vantara : http://www.pentaho.com
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

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;
import org.pentaho.di.base.AbstractMeta;
import org.pentaho.di.core.Const;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.widget.tree.TreeNode;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by bmorrise on 5/25/18.
 */
public class TreeManager {

  private Tree tree;

  private List<RootNode> rootNodes = new ArrayList<>();
  private BiMap<TreeNode, TreeItem> treeNodeItemMap = HashBiMap.create();

  private String filter;
  private GUIResource guiResource;

  public TreeManager( Tree tree, GUIResource guiResource ) {
    this.guiResource = guiResource;
    this.tree = tree;
    tree.addListener( SWT.Expand, e -> setExpanded( (TreeItem) e.item, true ) );
    tree.addListener( SWT.Collapse, e -> setExpanded( (TreeItem) e.item, false ) );
  }

  public TreeManager( Tree tree ) {
    this( tree, GUIResource.getInstance() );
  }

  public void addRoot( String label, List<TreeFolderProvider> providers ) {
    RootNode treeNode = new RootNode( label, guiResource.getImageFolder(), true );
    treeNode.setLabel( label );
    treeNode.setImage( GUIResource.getInstance().getImageFolder() );
    treeNode.setExpanded( true );
    treeNode.addProviders( providers );
    rootNodes.add( treeNode );
    providers.forEach( p -> p.setTreeManager( this ) );
  }

  public void addTreeProvider( String root, TreeFolderProvider treeFolderProvider ) {
    treeFolderProvider.setTreeManager( this );
    getRootTreeNodeByName( root ).addProvider( treeFolderProvider );
  }

  public boolean hasNode( AbstractMeta abstractMeta ) {
    return getTreeNode( abstractMeta ) != null;
  }

  public void clear() {
    if ( tree != null ) {
      tree.removeAll();
    }
    treeNodeItemMap.clear();
    hideAll();
  }

  public void render() {
    rootNodes.forEach( t -> {
      render( t, tree );
    } );
    setExpanded();
  }

  public void create( AbstractMeta abstractMeta, String name, boolean expanded ) {
    RootNode rootNode = getRootTreeNodeByName( name );
    if ( rootNode != null ) {
      Image image = abstractMeta instanceof TransMeta ? guiResource.getImageTransTree() : guiResource.getImageJobTree();
      getRootTreeNodeByName( name ).create( abstractMeta, image, expanded );
    }
  }

  public void checkUpdate( AbstractMeta abstractMeta, String name ) {
    RootNode rootNode = getRootTreeNodeByName( name );
    if ( rootNode != null ) {
      rootNode.checkUpdate( abstractMeta, filter );
    }
  }

  public void showRoot( String name, boolean show ) {
    getRootTreeNodeByName( name ).setHidden( !show );
  }

  public void hideAll() {
    rootNodes.forEach( treeNode -> {
      treeNode.getChildren().forEach( childTreeNode -> {
        childTreeNode.setHidden( true );
      } );
    } );
  }

  public void show( AbstractMeta abstractMeta ) {
    TreeNode treeNode = getTreeNode( abstractMeta );
    if ( treeNode != null ) {
      treeNode.setHidden( false );
    }
  }

  public void hide( AbstractMeta abstractMeta ) {
    TreeNode treeNode = getTreeNode( abstractMeta );
    if ( treeNode != null ) {
      treeNode.setHidden( true );
    }
  }

  public void setFilter( String filter ) {
    this.filter = filter;
  }

  public void reset( AbstractMeta abstractMeta ) {
    rootNodes.forEach( rootNode -> rootNode.clearUpdates( abstractMeta ) );
  }

  public void update( String name ) {
    rootNodes.forEach( rootNode -> rootNode.update( name ) );
  }

  public boolean shouldUpdate( AbstractMeta abstractMeta, String name ) {
    for ( RootNode rootNode : rootNodes ) {
      if ( rootNode.hasNode( abstractMeta ) && rootNode.shouldUpdate( abstractMeta, name ) ) {
        return true;
      }
    }
    return false;
  }

  private void setExpanded() {
    rootNodes.forEach( this::setExpanded );
  }

  private void setExpanded( TreeItem treeItem, boolean expanded ) {
    TreeNode treeNode = treeNodeItemMap.inverse().get( treeItem );
    if ( treeNode != null ) {
      treeNode.setExpanded( expanded );
    }
  }

  private void setExpanded( TreeNode treeNode ) {
    TreeItem treeItem = treeNodeItemMap.get( treeNode );
    if ( treeItem != null ) {
      treeItem.setExpanded( treeNode.isExpanded() );
      for ( TreeNode childTreeNode : treeNode.getChildren() ) {
        setExpanded( childTreeNode );
      }
    }
  }

  private <T> void render( TreeNode treeNode, T tree ) {
    if ( treeNode.isHidden() ) {
      return;
    }
    TreeItem childTreeItem = createTreeItem( treeNode, tree );
    if ( treeNode.hasChildren() ) {
      for ( TreeNode childTreeNode : treeNode.getChildren() ) {
        render( childTreeNode, childTreeItem );
      }
    }
  }

  private TreeItem createTreeItem( TreeNode treeNode, Object tree ) {
    TreeItem childTreeItem = createTreeItem( tree, treeNode.getIndex() );
    if ( Const.isRunningOnWebspoonMode() && treeNode.hasChildren() ) {
      try {
        Class webSpoonUtils = Class.forName( "org.pentaho.di.webspoon.WebSpoonUtils" );
        Method setTestId = webSpoonUtils.getDeclaredMethod( "setTestId", Widget.class, String.class );
        setTestId.invoke( null, childTreeItem, "view_" + treeNode.getLabel() );
      } catch ( ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e ) {
        e.printStackTrace();
      }
    }
    populateTreeItem( childTreeItem, treeNode );
    return childTreeItem;
  }

  private <T> TreeItem createTreeItem( T tree, int index ) {
    if ( tree instanceof Tree ) {
      return index != -1 ? new TreeItem( (Tree) tree, SWT.NONE, index ) : new TreeItem( (Tree) tree, SWT.NONE );
    } else {
      return index != -1 ? new TreeItem( (TreeItem) tree, SWT.NONE, index ) : new TreeItem( (TreeItem) tree, SWT.NONE );
    }
  }

  private void populateTreeItem( TreeItem childTreeItem, TreeNode treeNode ) {
    childTreeItem.setText( treeNode.getLabel() != null ? treeNode.getLabel() : "" );
    childTreeItem.setImage( treeNode.getImage() );
    childTreeItem.setData( treeNode.getData() );
    childTreeItem.setForeground( treeNode.getForeground() );
    childTreeItem.setBackground( treeNode.getBackground() );
    childTreeItem.setFont( treeNode.getFont() );
    treeNode.getData().forEach( childTreeItem::setData );
    treeNodeItemMap.put( treeNode, childTreeItem );
  }

  private RootNode getRootTreeNodeByName( String name ) {
    return rootNodes.stream()
            .filter( treeNode -> treeNode.getLabel().equalsIgnoreCase( name ) )
            .findFirst()
            .orElse( null );
  }

  public String getNameByType( Class clazz ) {
    for ( RootNode rootNode : rootNodes ) {
      String name = rootNode.getNameByType( clazz );
      if ( name != null ) {
        return name;
      }
    }
    return null;
  }

  public void remove( AbstractMeta abstractMeta ) {
    rootNodes.forEach( rootNode -> rootNode.remove( abstractMeta ) );
  }

  public TreeNode getTreeNode( AbstractMeta abstractMeta ) {
    for ( RootNode rootNode : rootNodes ) {
      TreeNode treeNode = rootNode.getTreeNode( abstractMeta );
      if ( treeNode != null ) {
        return treeNode;
      }
    }
    return null;
  }
}
