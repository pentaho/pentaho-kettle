/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package org.pentaho.di.vfs.connections.ui.dialog;

import org.eclipse.swt.SWT;
import org.pentaho.di.base.AbstractMeta;
import org.pentaho.di.connections.ConnectionManager;
import org.pentaho.di.connections.ui.dialog.ConnectionDeleteDialog;
import org.pentaho.di.connections.ui.tree.ConnectionFolderProvider;
import org.pentaho.di.core.EngineMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.ui.spoon.Spoon;

import java.util.function.Supplier;

/**
 * Created by bmorrise on 2/4/19.
 */
public class ConnectionDelegate {

  private static final Class<?> PKG = ConnectionDelegate.class;
  private Supplier<Spoon> spoonSupplier = Spoon::getInstance;

  private static final int WIDTH = 630;
  private static final int HEIGHT = 630;
  private static ConnectionDelegate instance;

  private ConnectionDelegate() {
    // no-op
  }

  public static ConnectionDelegate getInstance() {
    if ( null == instance ) {
      instance = new ConnectionDelegate();
    }
    return instance;
  }

  public void openDialog() {
    ConnectionDialog connectionDialog = new ConnectionDialog( spoonSupplier.get().getShell(), WIDTH, HEIGHT );
    connectionDialog.open( BaseMessages.getString( PKG, "ConnectionDialog.dialog.new.title" ) );
  }

  public void openDialog( String label ) {
    ConnectionDialog connectionDialog = new ConnectionDialog( spoonSupplier.get().getShell(), WIDTH, HEIGHT );
    connectionDialog.open( BaseMessages.getString( PKG, "ConnectionDialog.dialog.edit.title" ), label );
  }

  public void delete( String label ) {
    ConnectionDeleteDialog connectionDeleteDialog = new ConnectionDeleteDialog( spoonSupplier.get().getShell() );
    if ( connectionDeleteDialog.open( label ) == SWT.YES ) {
      ConnectionManager connectionManager = ConnectionManager.getInstance();
      connectionManager.delete( label );
      spoonSupplier.get().getShell().getDisplay().asyncExec( () -> spoonSupplier.get().refreshTree(
        ConnectionFolderProvider.STRING_VFS_CONNECTIONS ) );
      EngineMetaInterface engineMetaInterface = spoonSupplier.get().getActiveMeta();
      if ( engineMetaInterface instanceof AbstractMeta ) {
        ( (AbstractMeta) engineMetaInterface ).setChanged();
      }
    }
  }

}
