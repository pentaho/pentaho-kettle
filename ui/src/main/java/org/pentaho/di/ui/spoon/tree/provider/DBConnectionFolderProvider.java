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


package org.pentaho.di.ui.spoon.tree.provider;

import org.eclipse.swt.graphics.Image;
import org.pentaho.di.base.AbstractMeta;
import org.pentaho.di.core.bowl.Bowl;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.shared.DatabaseConnectionManager;
import org.pentaho.di.shared.DatabaseManagementInterface;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.widget.tree.LeveledTreeNode;
import org.pentaho.di.ui.core.widget.tree.TreeNode;
import org.pentaho.di.ui.spoon.DatabasesCollector;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.ui.spoon.tree.TreeFolderProvider;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

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
  public void refresh( Optional<AbstractMeta> meta, TreeNode treeNode, String filter ) {
    Bowl currentBowl = Spoon.getInstance().getBowl();
    Bowl globalBowl = Spoon.getInstance().getGlobalManagementBowl();
    try {
      Set<String> projectDbNames = new HashSet<>();
      if ( currentBowl != globalBowl ) {

        DatabaseManagementInterface dbManager = currentBowl.getManager( DatabaseManagementInterface.class );
        DatabasesCollector collector = new DatabasesCollector( dbManager, null );

        for ( String databaseName : collector.getDatabaseNames() ) {
          if ( !filterMatch( databaseName, filter ) ) {
            continue;
          }
          DatabaseMeta databaseMeta = collector.getMetaFor( databaseName );
          projectDbNames.add( databaseMeta.getDisplayName() );
          TreeNode childTreeNode = createTreeNode( treeNode, databaseMeta.getDisplayName(), guiResource
            .getImageConnectionTree(), LeveledTreeNode.LEVEL.PROJECT, false );
        }
      }
      // Global
      DatabaseManagementInterface globalDbConnMgr = globalBowl.getManager( DatabaseManagementInterface.class );
      DatabasesCollector collector = new DatabasesCollector( globalDbConnMgr, null );
      Set<String> globalDbNames = new HashSet<>();
      for ( String name : collector.getDatabaseNames() ) {
        if ( !filterMatch( name, filter ) ) {
          continue;
        }
        DatabaseMeta databaseMeta = collector.getMetaFor( name );
        globalDbNames.add( databaseMeta.getDisplayName() );
        createTreeNode( treeNode, databaseMeta.getDisplayName(), guiResource.getImageConnectionTree(), LeveledTreeNode.LEVEL.GLOBAL,
          containsIgnoreCase( projectDbNames, name ) );
      }

      // Local Db connection
      if ( meta.isPresent() ) {
        DatabaseManagementInterface dbManager = meta.get().getDatabaseManagementInterface();
        collector = new DatabasesCollector( dbManager,  null );
        for ( String name : collector.getDatabaseNames() ) {
          if ( !filterMatch( name, filter ) ) {
            continue;
          }
          DatabaseMeta databaseMeta = collector.getMetaFor( name );
          createTreeNode( treeNode, databaseMeta.getDisplayName(), guiResource.getImageConnectionTree(), LeveledTreeNode.LEVEL.LOCAL,
            containsIgnoreCase( projectDbNames, name ) || containsIgnoreCase( globalDbNames, name ) );
        }
      }
    } catch ( KettleException e ) {
      new ErrorDialog( Spoon.getInstance().getShell(),
        BaseMessages.getString( PKG, "Spoon.ErrorDialog.Title" ),
        BaseMessages.getString( PKG, "Spoon.ErrorDialog.ErrorFetchingFromRepo.DbConnections" ),
        e
      );
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

  public TreeNode createTreeNode( TreeNode parent, String name, Image image, LeveledTreeNode.LEVEL level,
                                 boolean overridden ) {
    LeveledTreeNode childTreeNode = new LeveledTreeNode( name, level, overridden );
    childTreeNode.setImage( image );

    parent.addChild( childTreeNode );
    return childTreeNode;
  }
}
