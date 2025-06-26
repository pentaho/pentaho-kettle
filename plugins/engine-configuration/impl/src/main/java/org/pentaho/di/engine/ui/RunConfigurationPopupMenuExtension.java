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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Tree;
import org.pentaho.di.core.bowl.Bowl;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.extension.ExtensionPoint;
import org.pentaho.di.core.extension.ExtensionPointInterface;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.engine.configuration.api.RunConfiguration;
import org.pentaho.di.engine.configuration.api.CheckedMetaStoreSupplier;
import org.pentaho.di.engine.configuration.impl.RunConfigurationManager;
import org.pentaho.di.engine.configuration.impl.pentaho.DefaultRunConfigurationProvider;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.ui.core.ConstUI;
import org.pentaho.di.ui.core.widget.tree.LeveledTreeNode;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.ui.spoon.TreeSelection;

import java.util.function.Supplier;

/**
 * Created by bmorrise on 3/14/17.
 */
@ExtensionPoint( id = "RunConfigurationPopupMenuExtension", description = "Creates popup menus for execution "
  + "environments", extensionPointId = "SpoonPopupMenuExtension" )
public class RunConfigurationPopupMenuExtension implements ExtensionPointInterface {

  private static Class<?> PKG = RunConfigurationPopupMenuExtension.class;

  private Supplier<Spoon> spoonSupplier = Spoon::getInstance;
  private RunConfigurationTreeItem runConfigurationTreeItem;
  private Menu rootMenu;
  private Menu itemMenu;

  @Override public void callExtensionPoint( LogChannelInterface logChannelInterface, Object extension )
    throws KettleException {
    Menu popupMenu = null;

    Tree selectionTree = (Tree) extension;
    TreeSelection[] objects = spoonSupplier.get().getTreeObjects( selectionTree );
    TreeSelection object = objects[ 0 ];
    Object selection = object.getSelection();

    if ( selection == RunConfiguration.class ) {
      popupMenu = createRootPopupMenu( selectionTree );
    } else if ( selection instanceof RunConfigurationTreeItem ) {
      runConfigurationTreeItem = (RunConfigurationTreeItem) selection;
      if ( runConfigurationTreeItem.getName().equalsIgnoreCase( DefaultRunConfigurationProvider.DEFAULT_CONFIG_NAME ) ) {
        return;
      }
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
      menuItem.setText( BaseMessages.getString( PKG, "RunConfigurationPopupMenuExtension.MenuItem.New" ) );
      menuItem.addSelectionListener( new SelectionAdapter() {
        @Override
        public void widgetSelected( SelectionEvent selectionEvent ) {
          // new goes to the Spoon's current bowl
          Bowl bowl = Spoon.getInstance().getManagementBowl();
          CheckedMetaStoreSupplier ms = () -> bowl.getMetastore();
          RunConfigurationDelegate runConfigurationDelegate = RunConfigurationDelegate.getInstance( ms );
          runConfigurationDelegate.create();
        }
      } );
    }
    return rootMenu;
  }

  private Menu createItemPopupMenu( Tree tree ) {
    if ( itemMenu == null ) {
      itemMenu = new Menu( tree );
      MenuItem editMenuItem = new MenuItem( itemMenu, SWT.NONE );
      editMenuItem.setText( BaseMessages.getString( PKG, "RunConfigurationPopupMenuExtension.MenuItem.Edit" ) );
      editMenuItem.addSelectionListener( new SelectionAdapter() {
        @Override public void widgetSelected( SelectionEvent selectionEvent ) {
          Bowl bowl = getEventBowl();
          CheckedMetaStoreSupplier ms = () -> bowl.getMetastore();
          RunConfigurationDelegate runConfigurationDelegate = RunConfigurationDelegate.getInstance( ms );
          RunConfigurationManager runConfigurationManager = RunConfigurationManager.getInstance( ms );
          runConfigurationDelegate.edit( runConfigurationManager.load( runConfigurationTreeItem.getName() ) );
        }
      } );
    }
    // clear old items
    MenuItem[] items = itemMenu.getItems();
    for ( int i = 1; i < items.length; i++ ) {
      items[i].dispose();
    }

    if ( runConfigurationTreeItem.getLevel() == LeveledTreeNode.LEVEL.PROJECT ) {
      MenuItem moveMenuItem = new MenuItem( itemMenu, SWT.NONE );
      moveMenuItem.setText( BaseMessages.getString( PKG, "RunConfigurationPopupMenuExtension.MenuItem.MoveTo",
              spoonSupplier.get().getGlobalManagementBowl().getLevelDisplayName() ) );
      moveMenuItem.addSelectionListener( new SelectionAdapter() {
        @Override public void widgetSelected( SelectionEvent selectionEvent ) {
          Bowl bowl = getEventBowl();
          CheckedMetaStoreSupplier ms = () -> bowl.getMetastore();
          RunConfigurationDelegate runConfigurationDelegate = RunConfigurationDelegate.getInstance( ms );
          RunConfigurationManager runConfigurationManager = RunConfigurationManager.getInstance( ms );
          runConfigurationDelegate.moveToGlobal( runConfigurationManager, runConfigurationManager.load( runConfigurationTreeItem.getName() ) );
        }
      } );

      MenuItem copyMenuItem = new MenuItem( itemMenu, SWT.NONE );
      copyMenuItem.setText( BaseMessages.getString( PKG, "RunConfigurationPopupMenuExtension.MenuItem.CopyTo",
              spoonSupplier.get().getGlobalManagementBowl().getLevelDisplayName() ) );
      copyMenuItem.addSelectionListener( new SelectionAdapter() {
        @Override public void widgetSelected( SelectionEvent selectionEvent ) {
          Bowl bowl = getEventBowl();
          CheckedMetaStoreSupplier ms = () -> bowl.getMetastore();
          RunConfigurationDelegate runConfigurationDelegate = RunConfigurationDelegate.getInstance( ms );
          RunConfigurationManager runConfigurationManager = RunConfigurationManager.getInstance( ms );
          runConfigurationDelegate.copyToGlobal( runConfigurationManager, runConfigurationManager.load( runConfigurationTreeItem.getName() ) );
        }
      } );
    }

    if ( runConfigurationTreeItem.getLevel() == LeveledTreeNode.LEVEL.GLOBAL &&
         spoonSupplier.get().getManagementBowl() != spoonSupplier.get().getGlobalManagementBowl() ) {
      MenuItem moveMenuItem = new MenuItem( itemMenu, SWT.NONE );
      moveMenuItem.setText( BaseMessages.getString( PKG, "RunConfigurationPopupMenuExtension.MenuItem.MoveToProject" ) );
      moveMenuItem.addSelectionListener( new SelectionAdapter() {
        @Override public void widgetSelected( SelectionEvent selectionEvent ) {
          Bowl bowl = getEventBowl();
          CheckedMetaStoreSupplier ms = () -> bowl.getMetastore();
          RunConfigurationDelegate runConfigurationDelegate = RunConfigurationDelegate.getInstance( ms );
          RunConfigurationManager runConfigurationManager = RunConfigurationManager.getInstance( ms );
          runConfigurationDelegate.moveToProject( runConfigurationManager, runConfigurationManager.load( runConfigurationTreeItem.getName() ) );
        }
      } );
      MenuItem copyMenuItem = new MenuItem( itemMenu, SWT.NONE );
      copyMenuItem.setText( BaseMessages.getString( PKG, "RunConfigurationPopupMenuExtension.MenuItem.CopyToProject" ) );
      copyMenuItem.addSelectionListener( new SelectionAdapter() {
        @Override public void widgetSelected( SelectionEvent selectionEvent ) {
          Bowl bowl = getEventBowl();
          CheckedMetaStoreSupplier ms = () -> bowl.getMetastore();
          RunConfigurationDelegate runConfigurationDelegate = RunConfigurationDelegate.getInstance( ms );
          RunConfigurationManager runConfigurationManager = RunConfigurationManager.getInstance( ms );
          runConfigurationDelegate.copyToProject( runConfigurationManager, runConfigurationManager.load( runConfigurationTreeItem.getName() ) );
        }
      } );
    }

    MenuItem duplicateMenuItem = new MenuItem( itemMenu, SWT.NONE );
    if ( runConfigurationTreeItem.getLevel() == LeveledTreeNode.LEVEL.GLOBAL ) {
      duplicateMenuItem.setText( BaseMessages.getString( PKG, "RunConfigurationPopupMenuExtension.MenuItem.Duplicate",
              spoonSupplier.get().getGlobalManagementBowl().getLevelDisplayName() ) );
    } else {
      duplicateMenuItem.setText( BaseMessages.getString( PKG, "RunConfigurationPopupMenuExtension.MenuItem.DuplicateProject" ) );
    }
    duplicateMenuItem.addSelectionListener( new SelectionAdapter() {
      @Override public void widgetSelected( SelectionEvent selectionEvent ) {
        Bowl bowl = getEventBowl();
        CheckedMetaStoreSupplier ms = () -> bowl.getMetastore();
        RunConfigurationDelegate runConfigurationDelegate = RunConfigurationDelegate.getInstance( ms );
        RunConfigurationManager runConfigurationManager = RunConfigurationManager.getInstance( ms );
        runConfigurationDelegate.duplicate( runConfigurationManager.load( runConfigurationTreeItem.getName() ) );
      }
    } );

    MenuItem deleteMenuItem = new MenuItem( itemMenu, SWT.NONE );
    deleteMenuItem.setText( BaseMessages.getString( PKG, "RunConfigurationPopupMenuExtension.MenuItem.Delete" ) );
    deleteMenuItem.addSelectionListener( new SelectionAdapter() {
      @Override public void widgetSelected( SelectionEvent selectionEvent ) {
        Bowl bowl = getEventBowl();
        CheckedMetaStoreSupplier ms = () -> bowl.getMetastore();
        RunConfigurationDelegate runConfigurationDelegate = RunConfigurationDelegate.getInstance( ms );
        RunConfigurationManager runConfigurationManager = RunConfigurationManager.getInstance( ms );
        runConfigurationDelegate.delete( runConfigurationManager.load( runConfigurationTreeItem.getName() ) );
      }
    } );
    return itemMenu;
  }

  private Bowl getEventBowl() {
    // Edit and Delete use the bowl that the item is in
    if ( runConfigurationTreeItem.getLevel().equals( LeveledTreeNode.LEVEL.GLOBAL ) ) {
      return spoonSupplier.get().getGlobalManagementBowl();
    } else {
      return spoonSupplier.get().getManagementBowl();
    }
  }
}

