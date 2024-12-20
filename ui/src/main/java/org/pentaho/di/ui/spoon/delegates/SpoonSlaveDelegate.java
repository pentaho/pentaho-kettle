/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.ui.spoon.delegates;


import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.cluster.SlaveServerManagementInterface;
import org.pentaho.di.core.bowl.DefaultBowl;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.ui.cluster.dialog.SlaveServerDialog;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.widget.TreeUtil;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.ui.spoon.SpoonSlave;
import org.pentaho.di.ui.spoon.TabMapEntry;
import org.pentaho.di.ui.spoon.TabMapEntry.ObjectType;
import org.pentaho.di.ui.spoon.tree.provider.SlavesFolderProvider;
import org.pentaho.xul.swt.tab.TabItem;
import org.pentaho.xul.swt.tab.TabSet;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;

/**
 * Spoon delegate class that handles all the right click popup menu actions in the Slave Server node in configuration
 * tree
 */
public class SpoonSlaveDelegate extends SpoonSharedObjectDelegate {
  private static Class<?> PKG = Spoon.class; // for i18n purposes, needed by Translator2!!

  public SpoonSlaveDelegate( Spoon spoon ) {
    super( spoon );
  }

  public void addSpoonSlave( SlaveServer slaveServer ) {
    TabSet tabfolder = spoon.tabfolder;

    // See if there is a SpoonSlave for this slaveServer...
    String tabName = spoon.delegates.tabs.makeSlaveTabName( slaveServer );
    TabMapEntry tabMapEntry = spoon.delegates.tabs.findTabMapEntry( tabName, ObjectType.SLAVE_SERVER );
    if ( tabMapEntry == null ) {
      SpoonSlave spoonSlave = new SpoonSlave( tabfolder.getSwtTabset(), SWT.NONE, spoon, slaveServer );
      PropsUI props = PropsUI.getInstance();
      TabItem tabItem = new TabItem( tabfolder, tabName, tabName, props.getSashWeights() );
      tabItem.setToolTipText( "Status of slave server : "
        + slaveServer.getName() + " : " + slaveServer.getServerAndPort() );
      tabItem.setControl( spoonSlave );

      tabMapEntry = new TabMapEntry( tabItem, null, tabName, null, null, spoonSlave, ObjectType.SLAVE_SERVER );
      spoon.delegates.tabs.addTab( tabMapEntry );
    }
    int idx = tabfolder.indexOf( tabMapEntry.getTabItem() );
    tabfolder.setSelected( idx );
  }

  public void delSlaveServer( SlaveServerManagementInterface slaveServerManager, SlaveServer slaveServer  )
      throws KettleException {

    MessageBox mb = new MessageBox( spoon.getShell(), SWT.YES | SWT.NO | SWT.ICON_QUESTION );
    mb.setMessage( BaseMessages.getString( PKG, "Spoon.Message.DeleteSlaveServerAsk.Message", slaveServer.getName() ) );
    mb.setText( BaseMessages.getString( PKG, "Spoon.ExploreDB.DeleteConnectionAsk.Title" ) );
    int response = mb.open();

    if ( response != SWT.YES ) {
      return;
    }
    spoon.getLog().logBasic( "Deleting the slave server " +  slaveServer.getName() );

    try {
      slaveServerManager.remove( slaveServer );

      Repository rep = spoon.getRepository();
      if ( rep != null && slaveServer.getObjectId() != null ) {
        // remove the slave server from the repository too...
        rep.deleteSlave( slaveServer.getObjectId() );
        if ( sharedObjectSyncUtil != null ) {
          sharedObjectSyncUtil.deleteSlaveServer( slaveServer );
        }
      }
      refreshTree();
    } catch ( Exception exception ) {
      new ErrorDialog( spoon.getShell(), BaseMessages.getString( PKG, "Spoon.Dialog.ErrorSavingSlave.Title" ),
        BaseMessages.getString( PKG, "Spoon.Dialog.UnexpectedDbError.Message", slaveServer.getName() ), exception );
    }
  }


  public void newSlaveServer() {
    SlaveServer slaveServer = new SlaveServer();
    try {
      SlaveServerManagementInterface slaveServerManagementInterface =
        spoon.getBowl().getManager( SlaveServerManagementInterface.class );
      SlaveServerDialog dialog =
        new SlaveServerDialog( spoon.getShell(), slaveServer, slaveServerManagementInterface.getAll() );
      if ( dialog.open() ) {
        spoon.getLog().logBasic( "Creating a new Slave Server " + slaveServer.getName() );
        slaveServerManagementInterface.add( slaveServer );
        refreshTree();
      }
    } catch ( Exception exception ) {
      new ErrorDialog( spoon.getShell(), BaseMessages.getString( PKG, "Spoon.Dialog.ErrorSavingSlave.Title" ),
        BaseMessages.getString( PKG, "Spoon.Dialog.UnexpectedDbError.Message", slaveServer.getName() ), exception );
    }

  }

  /**
   * For saving the slaveServer object in the repository.
   * @param slaveServer
   */
  private void saveSlaveServerInRepository( SlaveServer slaveServer ) {
    if ( spoon.rep != null ) {
      try {
        if ( !spoon.rep.getSecurityProvider().isReadOnly() ) {
          spoon.rep.save( slaveServer, Const.VERSION_COMMENT_INITIAL_VERSION, null );
          // repository objects are "global"
          if ( sharedObjectSyncUtil != null ) {
            sharedObjectSyncUtil.reloadJobRepositoryObjects( false );
            sharedObjectSyncUtil.reloadTransformationRepositoryObjects( false );
          }
        } else {
          showSaveErrorDialog( slaveServer,
            new KettleException( BaseMessages.getString( PKG, "Spoon.Dialog.Exception.ReadOnlyRepositoryUser" ) ) );
        }
      } catch ( KettleException e ) {
        showSaveErrorDialog( slaveServer, e );
      }
    }
  }

  public void edit( SlaveServerManagementInterface slaveServerManager, SlaveServer slaveServer ) {
    String originalName = slaveServer.getName().trim();
    try {
      SlaveServerDialog dialog = new SlaveServerDialog( spoon.getShell(), slaveServer, slaveServerManager.getAll() );
      if ( dialog.open() ) {
        String newName = slaveServer.getName().trim();
        if ( !newName.equals( originalName ) ) {
          slaveServerManager.remove( originalName );
          refreshTree();
        }
        slaveServerManager.add( slaveServer );

        if ( spoon.rep != null ) {
          try {
            saveSharedObjectToRepository( slaveServer, null );
          } catch ( KettleException e ) {
            showSaveErrorDialog( slaveServer, e );
          }
        }
       /* Keeping it for now, until we decide what we want to do when connected with repository
         if (sharedObjectSyncUtil != null) {
          sharedObjectSyncUtil.synchronizeSlaveServers(slaveServer, originalName);
        }*/
      }
      refreshTree();
      spoon.refreshGraph();
    } catch ( Exception exception ) {
      new ErrorDialog( spoon.getShell(), BaseMessages.getString( PKG, "Spoon.Dialog.ErrorSavingSlave.Title" ),
        BaseMessages.getString( PKG, "Spoon.Dialog.UnexpectedDbError.Message", slaveServer.getName() ), exception );
    }
  }

  public void moveToGlobal( SlaveServerManagementInterface slaveServerManager, SlaveServer slaveServer )
      throws KettleException {
    moveCopy( slaveServerManager, DefaultBowl.getInstance().getManager( SlaveServerManagementInterface.class ),
              slaveServer, true, "Spoon.Message.OverwriteSlaveServerYN" );
  }

  public void moveToProject( SlaveServerManagementInterface slaveServerManager, SlaveServer slaveServer )
      throws KettleException {
    moveCopy( slaveServerManager, spoon.getBowl().getManager( SlaveServerManagementInterface.class ), slaveServer, true,
              "Spoon.Message.OverwriteSlaveServerYN" );
  }

  public void copyToGlobal( SlaveServerManagementInterface slaveServerManager, SlaveServer slaveServer )
      throws KettleException {
    moveCopy( slaveServerManager, DefaultBowl.getInstance().getManager( SlaveServerManagementInterface.class ),
              slaveServer, false, "Spoon.Message.OverwriteSlaveServerYN" );
  }

  public void copyToProject( SlaveServerManagementInterface slaveServerManager, SlaveServer slaveServer )
      throws KettleException {
    moveCopy( slaveServerManager, spoon.getBowl().getManager( SlaveServerManagementInterface.class ), slaveServer,
              false, "Spoon.Message.OverwriteSlaveServerYN" );
  }

  public void dupeSlaveServer( SlaveServerManagementInterface slaveServerManager, SlaveServer slaveServer ) {
    ShowEditDialog<SlaveServer> sed = ( ss, servers ) -> {
      SlaveServerDialog dialog = new SlaveServerDialog( spoon.getShell(), ss, servers );
      if ( dialog.open() ) {
        String newServerName = ss.getName().trim();
        slaveServerManager.add( ss );
      }
    };
    dupeSharedObject( slaveServerManager, slaveServer, sed );
  }

  private void showSaveErrorDialog( SlaveServer slaveServer, Exception e ) {
    new ErrorDialog(
        spoon.getShell(), BaseMessages.getString( PKG, "Spoon.Dialog.ErrorSavingSlave.Title" ),
        BaseMessages.getString( PKG, "Spoon.Dialog.ErrorSavingSlave.Message", slaveServer.getName() ), e );
  }

  @Override
  protected void refreshTree() {
    spoon.refreshTree( SlavesFolderProvider.STRING_SLAVES );
  }
}
