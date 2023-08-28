/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2019 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.connections.ui.dialog;

import org.eclipse.swt.SWT;
import org.pentaho.di.base.AbstractMeta;
import org.pentaho.di.connections.ConnectionManager;
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
