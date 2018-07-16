/*
 * *****************************************************************************
 *
 *  Pentaho Data Integration
 *
 *  Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
 *
 *  *******************************************************************************
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use
 *  this file except in compliance with the License. You may obtain a copy of the
 *  License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 * *****************************************************************************
 *
 */

package org.pentaho.di.engine.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Tree;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.extension.ExtensionPoint;
import org.pentaho.di.core.extension.ExtensionPointInterface;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.engine.configuration.api.RunConfiguration;
import org.pentaho.di.engine.configuration.api.RunConfigurationService;
import org.pentaho.di.engine.configuration.impl.pentaho.DefaultRunConfigurationProvider;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.ui.core.ConstUI;
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
  private String runConfiguration;
  private RunConfigurationDelegate runConfigurationDelegate;
  private RunConfigurationService runConfigurationManager;
  private Menu rootMenu;
  private Menu itemMenu;

  public RunConfigurationPopupMenuExtension( RunConfigurationDelegate runConfigurationDelegate,
                                             RunConfigurationService runConfigurationManager ) {
    this.runConfigurationDelegate = runConfigurationDelegate;
    this.runConfigurationManager = runConfigurationManager;
  }

  @Override public void callExtensionPoint( LogChannelInterface logChannelInterface, Object extension )
    throws KettleException {
    Menu popupMenu = null;

    Tree selectionTree = (Tree) extension;
    TreeSelection[] objects = spoonSupplier.get().getTreeObjects( selectionTree );
    TreeSelection object = objects[ 0 ];
    Object selection = object.getSelection();

    if ( selection == RunConfiguration.class ) {
      popupMenu = createRootPopupMenu( selectionTree );
    } else if ( selection instanceof String ) {
      runConfiguration = (String) selection;
      if ( runConfiguration.equalsIgnoreCase( DefaultRunConfigurationProvider.DEFAULT_CONFIG_NAME ) ) {
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
          runConfigurationDelegate.edit( runConfigurationManager.load( runConfiguration ) );
        }
      } );

      MenuItem deleteMenuItem = new MenuItem( itemMenu, SWT.NONE );
      deleteMenuItem.setText( BaseMessages.getString( PKG, "RunConfigurationPopupMenuExtension.MenuItem.Delete" ) );
      deleteMenuItem.addSelectionListener( new SelectionAdapter() {
        @Override public void widgetSelected( SelectionEvent selectionEvent ) {
          runConfigurationDelegate.delete( runConfigurationManager.load( runConfiguration ) );
        }
      } );
    }
    return itemMenu;
  }
}

