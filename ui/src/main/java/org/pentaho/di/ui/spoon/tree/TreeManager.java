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


package org.pentaho.di.ui.spoon.tree;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;
import org.pentaho.di.base.AbstractMeta;
import org.pentaho.di.core.Const;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.widget.tree.TreeNode;
import org.pentaho.di.ui.spoon.Spoon;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
    treeNode.setImage( GUIResource.getInstance().getImageMainConfigurations() );
    treeNode.setExpanded( true );
    treeNode.addProviders( providers );
    rootNodes.add( treeNode );
    providers.forEach( p -> p.setTreeManager( this ) );
  }

  public void addTreeProvider( String root, TreeFolderProvider treeFolderProvider ) {
    if ( root == Spoon.STRING_TRANSFORMATIONS || root == Spoon.STRING_JOBS ) {
      root = Spoon.STRING_CONFIGURATIONS;
    }

    RootNode rootNode = getRootTreeNodeByName( root );
    String existingName = rootNode.getNameByType( treeFolderProvider.getType() );
    if ( existingName != null ) {
      return;
    }

    treeFolderProvider.setTreeManager( this );
    rootNode.addProvider( treeFolderProvider );
  }

  public void clear() {
    if ( tree != null ) {
      tree.removeAll();
    }
    treeNodeItemMap.clear();
  }

  public void render() {
    rootNodes.forEach( t -> {
      render( t, tree );
    } );
    setExpanded();
  }

  public void checkUpdate( Optional<AbstractMeta> abstractMeta, String name ) {
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

  public void setFilter( String filter ) {
    this.filter = filter;
    // if we are filtering, everything needs to be updated. Will be cleared by Spoon.showMetaTree()
    updateAll();
  }

  public void reset() {
    rootNodes.forEach( rootNode -> rootNode.clearUpdates() );
  }

  public void update( String name ) {
    rootNodes.forEach( rootNode -> rootNode.update( name ) );
  }

  public void updateAll() {
    rootNodes.forEach( rootNode -> rootNode.updateAll() );
  }

  public boolean shouldUpdate( String name ) {
    for ( RootNode rootNode : rootNodes ) {
      if ( rootNode.shouldUpdate( name ) ) {
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

}
