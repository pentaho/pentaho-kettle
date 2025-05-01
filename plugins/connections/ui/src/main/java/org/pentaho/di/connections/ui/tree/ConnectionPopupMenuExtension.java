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


package org.pentaho.di.connections.ui.tree;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Tree;
import org.pentaho.di.vfs.connections.ui.dialog.ConnectionDelegate;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.extension.ExtensionPoint;
import org.pentaho.di.core.extension.ExtensionPointInterface;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.ui.core.ConstUI;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.ui.spoon.TreeSelection;
import org.pentaho.di.connections.vfs.VFSConnectionDetails;
import org.pentaho.di.ui.core.widget.tree.LeveledTreeNode;

import java.util.function.Supplier;

@ExtensionPoint( id = "VFSConnectionPopupMenuExtension", description = "Creates popup menus for VFS Connections",
  extensionPointId = "SpoonPopupMenuExtension" )
public class ConnectionPopupMenuExtension implements ExtensionPointInterface {

  private static final Class<?> PKG = ConnectionPopupMenuExtension.class;

  private Supplier<Spoon> spoonSupplier = Spoon::getInstance;
  private Menu rootMenu;
  private Menu itemMenu;
  private ConnectionDelegate vfsConnectionDelegate;
  private ConnectionTreeItem vfsConnectionTreeItem;

  public ConnectionPopupMenuExtension() {
    this.vfsConnectionDelegate = ConnectionDelegate.getInstance();
  }

  @Override public void callExtensionPoint( LogChannelInterface logChannelInterface, Object extension )
    throws KettleException {
    Menu popupMenu = null;

    Tree selectionTree = (Tree) extension;
    TreeSelection[] objects = spoonSupplier.get().getTreeObjects( selectionTree );
    TreeSelection object = objects[ 0 ];
    Object selection = object.getSelection();

    if ( selection == VFSConnectionDetails.class ) {
      popupMenu = createRootPopupMenu( selectionTree );
    } else if ( selection instanceof ConnectionTreeItem ) {
      vfsConnectionTreeItem = (ConnectionTreeItem) selection;
      popupMenu = createItemPopupMenu( selectionTree );
    }

    if ( popupMenu != null ) {
      ConstUI.displayMenu( popupMenu, selectionTree );
    } else {
      selectionTree.setMenu( null );
    }
  }

  private Menu createRootPopupMenu( Tree tree ) {
    if ( rootMenu == null ) {
      rootMenu = new Menu( tree );
      MenuItem menuItem = new MenuItem( rootMenu, SWT.NONE );
      menuItem.setText( BaseMessages.getString( PKG, "VFSConnectionPopupMenuExtension.MenuItem.New" ) );
      menuItem.addSelectionListener( new SelectionAdapter() {
        @Override
        public void widgetSelected( SelectionEvent selectionEvent ) {
          vfsConnectionDelegate.openDialog();
        }
      } );
    }
    return rootMenu;
  }

  private Menu createItemPopupMenu( Tree tree ) {
    if ( itemMenu == null ) {
      itemMenu = new Menu( tree );
      MenuItem editMenuItem = new MenuItem( itemMenu, SWT.NONE );
      editMenuItem.setText( BaseMessages.getString( PKG, "VFSConnectionPopupMenuExtension.MenuItem.Edit" ) );
      editMenuItem.addSelectionListener( new SelectionAdapter() {
        @Override public void widgetSelected( SelectionEvent selectionEvent ) {
          vfsConnectionDelegate.openDialog( vfsConnectionTreeItem.getName(), vfsConnectionTreeItem.getLevel() );
        }
      } );

      MenuItem deleteMenuItem = new MenuItem( itemMenu, SWT.NONE );
      deleteMenuItem.setText( BaseMessages.getString( PKG, "VFSConnectionPopupMenuExtension.MenuItem.Delete" ) );
      deleteMenuItem.addSelectionListener( new SelectionAdapter() {
        @Override public void widgetSelected( SelectionEvent selectionEvent ) {
          vfsConnectionDelegate.delete( vfsConnectionTreeItem.getName(), vfsConnectionTreeItem.getLevel() );
        }
      } );
    }

    // clear old items
    MenuItem[] items = itemMenu.getItems();
    for ( int i = 2; i < items.length; i++ ) {
      items[i].dispose();
    }

    // customize menu for specific item
    if ( vfsConnectionTreeItem.getLevel() == LeveledTreeNode.LEVEL.PROJECT ) {
      MenuItem moveMenuItem = new MenuItem( itemMenu, SWT.NONE );
      moveMenuItem.setText( BaseMessages.getString( PKG, "VFSConnectionPopupMenuExtension.MenuItem.MoveToGlobal",
              spoonSupplier.get().getGlobalManagementBowl().getLevelDisplayName() ) );
      moveMenuItem.addSelectionListener( new SelectionAdapter() {
        @Override public void widgetSelected( SelectionEvent selectionEvent ) {
          vfsConnectionDelegate.moveToGlobal( vfsConnectionTreeItem.getName() );
        }
      } );
      MenuItem copyMenuItem = new MenuItem( itemMenu, SWT.NONE );
      copyMenuItem.setText( BaseMessages.getString( PKG, "VFSConnectionPopupMenuExtension.MenuItem.CopyToGlobal",
              spoonSupplier.get().getGlobalManagementBowl().getLevelDisplayName() ) );
      copyMenuItem.addSelectionListener( new SelectionAdapter() {
        @Override public void widgetSelected( SelectionEvent selectionEvent ) {
          vfsConnectionDelegate.copyToGlobal( vfsConnectionTreeItem.getName() );
        }
      } );
    }
    if ( vfsConnectionTreeItem.getLevel() == LeveledTreeNode.LEVEL.GLOBAL
         && spoonSupplier.get().getBowl() != spoonSupplier.get().getGlobalManagementBowl() ) {
      MenuItem moveMenuItem = new MenuItem( itemMenu, SWT.NONE );
      moveMenuItem.setText( BaseMessages.getString( PKG, "VFSConnectionPopupMenuExtension.MenuItem.MoveToProject" ) );
      moveMenuItem.addSelectionListener( new SelectionAdapter() {
        @Override public void widgetSelected( SelectionEvent selectionEvent ) {
          vfsConnectionDelegate.moveToProject( vfsConnectionTreeItem.getName() );
        }
      } );
      MenuItem copyMenuItem = new MenuItem( itemMenu, SWT.NONE );
      copyMenuItem.setText( BaseMessages.getString( PKG, "VFSConnectionPopupMenuExtension.MenuItem.CopyToProject" ) );
      copyMenuItem.addSelectionListener( new SelectionAdapter() {
        @Override public void widgetSelected( SelectionEvent selectionEvent ) {
          vfsConnectionDelegate.copyToProject( vfsConnectionTreeItem.getName() );
        }
      } );
    }

    MenuItem duplicateMenuItem = new MenuItem( itemMenu, SWT.NONE );
    if ( vfsConnectionTreeItem.getLevel() == LeveledTreeNode.LEVEL.GLOBAL ) {
      duplicateMenuItem.setText( BaseMessages.getString( PKG, "VFSConnectionPopupMenuExtension.MenuItem.DuplicateGlobal",
              spoonSupplier.get().getGlobalManagementBowl().getLevelDisplayName() ) );
    } else {
      duplicateMenuItem.setText( BaseMessages.getString( PKG, "VFSConnectionPopupMenuExtension.MenuItem.DuplicateProject" ) );
    }
    duplicateMenuItem.addSelectionListener( new SelectionAdapter() {
      @Override public void widgetSelected( SelectionEvent selectionEvent ) {
        vfsConnectionDelegate.duplicate( vfsConnectionTreeItem.getName(), vfsConnectionTreeItem.getLevel() );
      }
    } );

    return itemMenu;
  }
}

