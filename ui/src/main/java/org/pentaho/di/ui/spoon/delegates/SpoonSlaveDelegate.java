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


package org.pentaho.di.ui.spoon.delegates;


import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.cluster.SlaveServerManagementInterface;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.ui.cluster.dialog.SlaveServerDialog;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.ui.spoon.SpoonSlave;
import org.pentaho.di.ui.spoon.TabMapEntry;
import org.pentaho.di.ui.spoon.TabMapEntry.ObjectType;
import org.pentaho.di.ui.spoon.tree.provider.SlavesFolderProvider;
import org.pentaho.xul.swt.tab.TabItem;
import org.pentaho.xul.swt.tab.TabSet;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;

/**
 * Spoon delegate class that handles all the right click popup menu actions in the Slave Server node in configuration
 * tree
 */
public class SpoonSlaveDelegate extends SpoonSharedObjectDelegate<SlaveServer> {
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

  public void edit( SlaveServerManagementInterface slaveServerManager, SlaveServer slaveServer ) {
    String originalName = slaveServer.getName().trim();
    try {
      SlaveServerDialog dialog = new SlaveServerDialog( spoon.getShell(), slaveServer, slaveServerManager.getAll() );
      if ( dialog.open() ) {
        String newName = slaveServer.getName().trim();
        // This should be case insensitive. We only need to remove if the name changed beyond case. The Managers handle
        // case-only changes.
        if ( !newName.equalsIgnoreCase( originalName ) ) {
          slaveServerManager.remove( originalName );
          // ideally we wouldn't leak this repository-specific concept, but I don't see how at the moment.
          slaveServer.setObjectId( null );
        }
        slaveServerManager.add( slaveServer );
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
    moveCopy( slaveServerManager, spoon.getGlobalManagementBowl().getManager( SlaveServerManagementInterface.class ),
              slaveServer, true, "Spoon.Message.OverwriteSlaveServerYN" );
  }

  public void moveToProject( SlaveServerManagementInterface slaveServerManager, SlaveServer slaveServer )
      throws KettleException {
    moveCopy( slaveServerManager, spoon.getBowl().getManager( SlaveServerManagementInterface.class ), slaveServer, true,
              "Spoon.Message.OverwriteSlaveServerYN" );
  }

  public void copyToGlobal( SlaveServerManagementInterface slaveServerManager, SlaveServer slaveServer )
      throws KettleException {
    moveCopy( slaveServerManager, spoon.getGlobalManagementBowl().getManager( SlaveServerManagementInterface.class ),
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
        slaveServerManager.add( ss );
      }
    };
    dupeSharedObject( slaveServerManager, slaveServer, sed );
  }

  @Override
  protected void refreshTree() {
    spoon.refreshTree( SlavesFolderProvider.STRING_SLAVES );
  }
}
