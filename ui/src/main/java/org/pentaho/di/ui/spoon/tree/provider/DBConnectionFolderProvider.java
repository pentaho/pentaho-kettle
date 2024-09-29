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

package org.pentaho.di.ui.spoon.tree.provider;

import org.pentaho.di.base.AbstractMeta;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.KettleRepositoryLostException;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.widget.tree.TreeNode;
import org.pentaho.di.ui.spoon.DatabasesCollector;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.ui.spoon.tree.TreeFolderProvider;

/**
 * Created by bmorrise on 6/28/18.
 */
public class DBConnectionFolderProvider extends TreeFolderProvider {

  private static Class<?> PKG = Spoon.class;
  public static final String STRING_CONNECTIONS = BaseMessages.getString( PKG, "Spoon.STRING_CONNECTIONS" );

  private GUIResource guiResource;
  private Spoon spoon;

  public DBConnectionFolderProvider( GUIResource guiResource, Spoon spoon ) {
    this.guiResource = guiResource;
    this.spoon = spoon;
  }

  public DBConnectionFolderProvider() {
    this( GUIResource.getInstance(), Spoon.getInstance() );
  }

  @Override
  public void refresh( AbstractMeta meta, TreeNode treeNode, String filter ) {
    DatabasesCollector collector = new DatabasesCollector( meta, spoon.getRepository() );
    try {
      try {
        collector.collectDatabases();
      } catch ( KettleException e ) {
        if ( e.getCause() instanceof KettleRepositoryLostException ) {
          Spoon.getInstance().handleRepositoryLost( (KettleRepositoryLostException) e.getCause() );
          collector = new DatabasesCollector( meta, null );
          collector.collectDatabases();
        } else {
          throw e;
        }
      }
    } catch ( KettleException e ) {
      new ErrorDialog( Spoon.getInstance().getShell(),
              BaseMessages.getString( PKG, "Spoon.ErrorDialog.Title" ),
              BaseMessages.getString( PKG, "Spoon.ErrorDialog.ErrorFetchingFromRepo.DbConnections" ),
              e
      );
    }

    for ( String dbName : collector.getDatabaseNames() ) {
      if ( !filterMatch( dbName, filter ) ) {
        continue;
      }
      DatabaseMeta databaseMeta = collector.getMetaFor( dbName );

      TreeNode childTreeNode = createTreeNode( treeNode, databaseMeta.getDisplayName(), guiResource
              .getImageConnectionTree() );
      if ( databaseMeta.isShared() ) {
        childTreeNode.setFont( guiResource.getFontBold() );
      }
    }
  }

  @Override
  public String getTitle() {
    return STRING_CONNECTIONS;
  }

  @Override
  public Class getType() {
    return DatabaseMeta.class;
  }
}
