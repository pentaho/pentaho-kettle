/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2022 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.vfs.connections.ui.dialog;

import org.eclipse.swt.SWT;
import org.pentaho.di.base.AbstractMeta;
import org.pentaho.di.connections.ConnectionManager;
import org.pentaho.di.connections.ui.dialog.ConnectionDeleteDialog;
import org.pentaho.di.connections.ui.tree.ConnectionFolderProvider;
import org.pentaho.di.core.bowl.Bowl;
import org.pentaho.di.core.bowl.DefaultBowl;
import org.pentaho.di.core.EngineMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.metastore.api.exceptions.MetaStoreException;

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
    try {
      Spoon spoon = spoonSupplier.get();
      Bowl bowl = spoon.getBowl();
      ConnectionDialog connectionDialog = new ConnectionDialog( spoon.getShell(), WIDTH, HEIGHT,
                                                                bowl.getExplicitConnectionManager() );
      connectionDialog.open( BaseMessages.getString( PKG, "ConnectionDialog.dialog.new.title" ) );
      resetConnectionManagerIfNeeded( bowl );
    } catch ( MetaStoreException e ) {
      showError( e );
    }
  }

  public void openDialog( String label ) {
    try {
      Spoon spoon = spoonSupplier.get();
      Bowl bowl = spoon.getBowl();
      ConnectionDialog connectionDialog = new ConnectionDialog( spoon.getShell(), WIDTH, HEIGHT,
                                                                bowl.getExplicitConnectionManager() );
      connectionDialog.open( BaseMessages.getString( PKG, "ConnectionDialog.dialog.edit.title" ), label );
      resetConnectionManagerIfNeeded( bowl );
    } catch ( MetaStoreException e ) {
      showError( e );
    }
  }

  public void delete( String label ) {
    try {
      ConnectionDeleteDialog connectionDeleteDialog = new ConnectionDeleteDialog( spoonSupplier.get().getShell() );
      if ( connectionDeleteDialog.open( label ) == SWT.YES ) {
        Spoon spoon = spoonSupplier.get();
        Bowl bowl = spoon.getBowl();
        ConnectionManager connectionManager = bowl.getExplicitConnectionManager();
        connectionManager.delete( label );
        resetConnectionManagerIfNeeded( bowl );

        spoonSupplier.get().getShell().getDisplay().asyncExec( () -> spoonSupplier.get().refreshTree(
        ConnectionFolderProvider.STRING_VFS_CONNECTIONS ) );
        EngineMetaInterface engineMetaInterface = spoonSupplier.get().getActiveMeta();
        if ( engineMetaInterface instanceof AbstractMeta ) {
          ( (AbstractMeta) engineMetaInterface ).setChanged();
        }
      }
    } catch ( MetaStoreException e ) {
      showError( e );
    }
  }

  private void resetConnectionManagerIfNeeded( Bowl bowl ) throws MetaStoreException {
    // if bowl isn't default, reset it's (non-explicit) ConnectionManager
    if ( !bowl.equals( DefaultBowl.getInstance() ) ) {
      bowl.getConnectionManager().reset();
    }
  }

  private void showError( Exception e ) {
    new ErrorDialog( spoonSupplier.get().getShell(),
                     BaseMessages.getString( PKG, "Spoon.ErrorDialog.Title" ),
                     BaseMessages.getString( PKG, "Spoon.ErrorDialog.ErrorFetchingVFSConnections" ),
                     e
                     );
  }
}

