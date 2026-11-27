/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/




package org.pentaho.di.ui.util;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.repository.RepositoryElementMetaInterface;
import org.pentaho.di.shared.DatabaseManagementInterface;
import org.pentaho.di.shared.SharedObjectInterface;
import org.pentaho.di.ui.spoon.Spoon;

import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;

/**
 * @author Andrey Khayrutdinov
 */
public class DialogUtils {

  public static String getPathOf( RepositoryElementMetaInterface object ) {
    if ( object != null && !object.isDeleted() ) {
      RepositoryDirectoryInterface directory = object.getRepositoryDirectory();
      if ( directory != null ) {
        String path = directory.getPath();
        if ( path != null ) {
          if ( !path.endsWith( "/" ) ) {
            path += "/";
          }
          path += object.getName();

          return path;
        }
      }
    }
    return null;
  }

  public static <T extends SharedObjectInterface<T>> boolean objectWithTheSameNameExists( T object, Collection<T> scope ) {
    String objectName = object.getName().trim();
    for ( SharedObjectInterface<T> element : scope ) {
      String elementName = element.getName().trim();
      if ( elementName != null && elementName.equalsIgnoreCase( objectName ) && object != element ) {
        return true;
      }
    }
    return false;
  }

  public static <T extends SharedObjectInterface<T>> void removeMatchingObject( String nameToRemove, Collection<? extends SharedObjectInterface<T>> objects ) {
    if ( nameToRemove == null ) {
      return;
    }
    Iterator<? extends SharedObjectInterface<T>> iter = objects.iterator();
    while ( iter.hasNext() ) {
      if ( nameToRemove.equals( iter.next().getName() ) ) {
        iter.remove();
      }
    }
  }

  public static String getPath( String parentPath, String path ) {
    if ( !parentPath.equals( "/" ) && path.startsWith( parentPath ) ) {
      path = path.replace( parentPath, "${" + Const.INTERNAL_VARIABLE_ENTRY_CURRENT_DIRECTORY + "}" );
    }
    return path;
  }

  /**
   * Resolves the manager containing a connection by name.
   * <p>
   * Lookup order:
   * 1. Spoon's management bowl database manager
   * 2. Spoon's global management bowl database manager
   * 3. The provided local manager (for job/trans local connections)
   *
   * @param spoon the active Spoon instance
   * @param localDbMgr the local job/trans database manager
   * @param originalName the connection name to resolve
   * @return the manager that contains {@code originalName}
   * @throws IllegalArgumentException when the connection does not exist in any candidate manager
   * @throws KettleException if Spoon manager access fails
   */
  public static DatabaseManagementInterface resolveContainingDatabaseManager( Spoon spoon,
                                                                              DatabaseManagementInterface localDbMgr,
                                                                              String originalName )
    throws KettleException {
    Objects.requireNonNull( originalName, "originalName must not be null" );
    DatabaseManagementInterface dbMgr = spoon.getManagementBowl().getManager( DatabaseManagementInterface.class );
    DatabaseManagementInterface globalDbMgr = spoon.getGlobalManagementBowl().getManager( DatabaseManagementInterface.class );

    if ( dbMgr.get( originalName ) != null ) {
      return dbMgr;
    }
    if ( globalDbMgr.get( originalName ) != null ) {
      return globalDbMgr;
    }
    if ( localDbMgr.get( originalName ) != null ) {
      return localDbMgr;
    }
    throw new IllegalArgumentException( "Connection name not found in any database manager: " + originalName );
  }

  /**
   * Persists an edited connection in its original manager.
   * <p>
   * If the name changed beyond case-only differences, the old entry is removed and the edited
   * connection is re-added with a null object id so it is treated as a new object at save time.
   *
   * @param applicableDbMgr manager that owns the original connection
   * @param originalConnection original connection before edit
   * @param editedConnection edited connection to persist
   * @param editedConnectionName final user-approved connection name
   * @throws KettleException if the manager cannot remove/add the connection
   */
  public static void persistEditedConnection( DatabaseManagementInterface applicableDbMgr,
                                              DatabaseMeta originalConnection,
                                              DatabaseMeta editedConnection,
                                              String editedConnectionName ) throws KettleException {
    Objects.requireNonNull( originalConnection.getName(), "originalConnection name must not be null" );
    if ( !editedConnectionName.equalsIgnoreCase( originalConnection.getName() ) ) {
      // To prevent the connection from moving between levels, remove then re-add to original manager.
      applicableDbMgr.remove( originalConnection );
      // Clear objectId because this is persisted as a newly added object.
      editedConnection.setObjectId( null );
    }
    applicableDbMgr.add( editedConnection );
  }

}
