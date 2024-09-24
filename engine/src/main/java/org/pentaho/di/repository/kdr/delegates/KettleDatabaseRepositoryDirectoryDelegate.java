/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.repository.kdr.delegates;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.value.ValueMetaInteger;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.LongObjectId;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.RepositoryDirectory;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.repository.kdr.KettleDatabaseRepository;

public class KettleDatabaseRepositoryDirectoryDelegate extends KettleDatabaseRepositoryBaseDelegate {
  private static Class<?> PKG = RepositoryDirectory.class; // for i18n purposes, needed by Translator2!!

  public KettleDatabaseRepositoryDirectoryDelegate( KettleDatabaseRepository repository ) {
    super( repository );
  }

  public RowMetaAndData getDirectory( ObjectId id_directory ) throws KettleException {
    return repository.connectionDelegate.getOneRow(
      quoteTable( KettleDatabaseRepository.TABLE_R_DIRECTORY ),
      quote( KettleDatabaseRepository.FIELD_DIRECTORY_ID_DIRECTORY ), id_directory );
  }

  public RepositoryDirectoryInterface loadPathToRoot( ObjectId id_directory ) throws KettleException {
    List<RepositoryDirectory> path = new ArrayList<>();

    ObjectId directoryId = id_directory;

    RowMetaAndData directoryRow = getDirectory( directoryId );
    Long parentId = directoryRow.getInteger( 1 );

    // Do not load root itself, it doesn't exist.
    //
    while ( parentId != null && parentId >= 0 ) {
      RepositoryDirectory directory = new RepositoryDirectory();
      directory.setName( directoryRow.getString( 2, null ) ); // Name of the directory
      directory.setObjectId( directoryId );
      path.add( directory );

      // System.out.println( "+ dir '" + directory.getName() + "'" );

      directoryId = new LongObjectId( parentId );
      directoryRow = getDirectory( directoryId );
      parentId = directoryRow.getInteger( KettleDatabaseRepository.FIELD_DIRECTORY_ID_DIRECTORY_PARENT );
    }

    RepositoryDirectory root = new RepositoryDirectory();
    root.setObjectId( new LongObjectId( 0 ) );
    path.add( root );

    // Connect the directories to each other.
    //
    for ( int i = 0; i < path.size() - 1; i++ ) {
      RepositoryDirectory item = path.get( i );
      RepositoryDirectory parent = path.get( i + 1 );
      item.setParent( parent );
      parent.addSubdirectory( item );
    }

    RepositoryDirectory repositoryDirectory = path.get( 0 );
    return repositoryDirectory;
  }

  public RepositoryDirectoryInterface loadRepositoryDirectoryTree( RepositoryDirectoryInterface root ) throws KettleException {
    try {
      synchronized ( repository ) {

        root.clear();
        ObjectId[] subids = repository.getSubDirectoryIDs( root.getObjectId() );
        for ( int i = 0; i < subids.length; i++ ) {
          RepositoryDirectory subdir = new RepositoryDirectory();
          loadRepositoryDirectory( subdir, subids[i] );
          root.addSubdirectory( subdir );
        }
      }

      return root;
    } catch ( Exception e ) {
      throw new KettleException( "An error occured loading the directory tree from the repository", e );
    }
  }

  public void loadRepositoryDirectory( RepositoryDirectory repositoryDirectory, ObjectId id_directory ) throws KettleException {
    if ( id_directory == null ) {
      // This is the root directory, id = OL
      id_directory = new LongObjectId( 0L );
    }

    try {
      RowMetaAndData row = getDirectory( id_directory );
      if ( row != null ) {
        repositoryDirectory.setObjectId( id_directory );

        // Content?
        //
        repositoryDirectory.setName( row.getString( "DIRECTORY_NAME", null ) );

        // The sub-directories?
        //
        ObjectId[] subids = repository.getSubDirectoryIDs( repositoryDirectory.getObjectId() );
        for ( int i = 0; i < subids.length; i++ ) {
          RepositoryDirectory subdir = new RepositoryDirectory();
          loadRepositoryDirectory( subdir, subids[i] );
          repositoryDirectory.addSubdirectory( subdir );
        }
      }
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString(
        PKG, "Repository.LoadRepositoryDirectory.ErrorLoading.Exception" ), e );
    }
  }

  /*
   * public synchronized RepositoryDirectory refreshRepositoryDirectoryTree() throws KettleException { try {
   * RepositoryDirectory tree = new RepositoryDirectory(); loadRepositoryDirectory(tree, tree.getID());
   * repository.setDirectoryTree(tree); return tree; } catch (KettleException e) { repository.setDirectoryTree( new
   * RepositoryDirectory() ); throw new KettleException("Unable to read the directory tree from the repository!", e); }
   * }
   */

  private synchronized ObjectId insertDirectory( ObjectId id_directory_parent, RepositoryDirectoryInterface dir ) throws KettleException {
    ObjectId id = repository.connectionDelegate.getNextDirectoryID();

    String tablename = KettleDatabaseRepository.TABLE_R_DIRECTORY;
    RowMetaAndData table = new RowMetaAndData();
    table.addValue( new ValueMetaInteger(
      KettleDatabaseRepository.FIELD_DIRECTORY_ID_DIRECTORY ), id );
    table.addValue(
      new ValueMetaInteger(
        KettleDatabaseRepository.FIELD_DIRECTORY_ID_DIRECTORY_PARENT ),
      id_directory_parent );
    table.addValue( new ValueMetaString(
      KettleDatabaseRepository.FIELD_DIRECTORY_DIRECTORY_NAME ), dir.getName() );

    repository.connectionDelegate.getDatabase().prepareInsert( table.getRowMeta(), tablename );
    repository.connectionDelegate.getDatabase().setValuesInsert( table );
    repository.connectionDelegate.getDatabase().insertRow();
    repository.connectionDelegate.getDatabase().closeInsert();

    return id;
  }

  public synchronized void deleteDirectory( ObjectId id_directory ) throws KettleException {
    repository.connectionDelegate.performDelete( "DELETE FROM "
      + quoteTable( KettleDatabaseRepository.TABLE_R_DIRECTORY ) + " WHERE "
      + quote( KettleDatabaseRepository.FIELD_DIRECTORY_ID_DIRECTORY ) + " = ? ", id_directory );
  }

  public synchronized void deleteDirectory( RepositoryDirectoryInterface dir ) throws KettleException {
    String[] trans = repository.getTransformationNames( dir.getObjectId(), false ); // TODO : include or exclude deleted
                                                                                    // objects?
    String[] jobs = repository.getJobNames( dir.getObjectId(), false ); // TODO : include or exclude deleted objects?
    ObjectId[] subDirectories = repository.getSubDirectoryIDs( dir.getObjectId() );
    if ( trans.length == 0 && jobs.length == 0 && subDirectories.length == 0 ) {
      repository.directoryDelegate.deleteDirectory( dir.getObjectId() );
    } else {
      deleteDirectoryRecursively( dir );
    }
  }

  private synchronized void deleteDirectoryRecursively( RepositoryDirectoryInterface dir ) throws KettleException {
    String[] trans = repository.getTransformationNames( dir.getObjectId(), false ); // TODO : include or exclude deleted
                                                                                    // objects?
    String[] jobs = repository.getJobNames( dir.getObjectId(), false ); // TODO : include or exclude deleted objects?
    for ( String transformation : trans ) {
      ObjectId id = repository.getTransformationID( transformation, dir );
      repository.deleteTransformation( id );
    }
    for ( String job : jobs ) {
      ObjectId id = repository.getJobId( job, dir );
      repository.deleteJob( id );
    }
    for ( RepositoryDirectoryInterface subDir : dir.getChildren() ) {
      deleteDirectoryRecursively( subDir );
    }
    repository.directoryDelegate.deleteDirectory( dir.getObjectId() );
  }

  /**
   * Move / rename a directory in the repository
   *
   * @param id_directory
   *          Id of the directory to be moved/renamed
   * @param id_directory_parent
   *          Id of the new parent directory (null if the parent does not change)
   * @param newName
   *          New name for this directory (null if the name does not change)
   * @throws KettleException
   */
  public synchronized void renameDirectory( ObjectId id_directory, ObjectId id_directory_parent, String newName ) throws KettleException {
    if ( id_directory.equals( id_directory_parent ) ) {
      // Make sure the directory cannot become its own parent
      throw new KettleException( "Failed to copy directory into itself" );
    } else {
      // Make sure the directory does not become a descendant of itself
      RepositoryDirectory rd = new RepositoryDirectory();
      loadRepositoryDirectory( rd, id_directory );
      if ( rd.findDirectory( id_directory_parent ) != null ) {
        // The parent directory is a child of this directory. Do not proceed
        throw new KettleException( "Directory cannot become a child to itself" );
      } else {
        // Check for duplication
        RepositoryDirectory newParent = new RepositoryDirectory();
        loadRepositoryDirectory( newParent, id_directory_parent );
        RepositoryDirectory child = newParent.findChild( newName == null ? rd.getName() : newName );
        if ( child != null ) {
          throw new KettleException( "Destination directory already contains a diectory with requested name" );
        }
      }
    }

    if ( id_directory_parent != null || newName != null ) {
      RowMetaAndData r = new RowMetaAndData();

      String sql = "UPDATE " + quoteTable( KettleDatabaseRepository.TABLE_R_DIRECTORY ) + " SET ";
      boolean additionalParameter = false;

      if ( newName != null ) {
        additionalParameter = true;
        sql += quote( KettleDatabaseRepository.FIELD_DIRECTORY_DIRECTORY_NAME ) + " = ?";
        r.addValue( new ValueMetaString(
          KettleDatabaseRepository.FIELD_DIRECTORY_DIRECTORY_NAME ), newName );
      }
      if ( id_directory_parent != null ) {
        // Add a parameter separator if the first parm was added
        if ( additionalParameter ) {
          sql += ", ";
        }
        sql += quote( KettleDatabaseRepository.FIELD_DIRECTORY_ID_DIRECTORY_PARENT ) + " = ?";
        r.addValue(
          new ValueMetaInteger(
            KettleDatabaseRepository.FIELD_DIRECTORY_ID_DIRECTORY_PARENT ),
          id_directory_parent );
      }

      sql += " WHERE " + quote( KettleDatabaseRepository.FIELD_DIRECTORY_ID_DIRECTORY ) + " = ? ";
      r.addValue( new ValueMetaInteger( "id_directory" ), Long.valueOf( id_directory
        .toString() ) );

      repository.connectionDelegate.getDatabase().execStatement( sql, r.getRowMeta(), r.getData() );
    }
  }

  public synchronized int getNrSubDirectories( ObjectId id_directory ) throws KettleException {
    int retval = 0;

    RowMetaAndData dirParRow = repository.connectionDelegate.getParameterMetaData( id_directory );
    String sql =
      "SELECT COUNT(*) FROM "
        + quoteTable( KettleDatabaseRepository.TABLE_R_DIRECTORY ) + " WHERE "
        + quote( KettleDatabaseRepository.FIELD_DIRECTORY_ID_DIRECTORY_PARENT ) + " = ? ";
    RowMetaAndData r = repository.connectionDelegate.getOneRow( sql, dirParRow.getRowMeta(), dirParRow.getData() );
    if ( r != null ) {
      retval = (int) r.getInteger( 0, 0 );
    }

    return retval;
  }

  public synchronized ObjectId[] getSubDirectoryIDs( ObjectId id_directory ) throws KettleException {
    return repository.connectionDelegate.getIDs( "SELECT "
      + quote( KettleDatabaseRepository.FIELD_DIRECTORY_ID_DIRECTORY ) + " FROM "
      + quoteTable( KettleDatabaseRepository.TABLE_R_DIRECTORY ) + " WHERE "
      + quote( KettleDatabaseRepository.FIELD_DIRECTORY_ID_DIRECTORY_PARENT ) + " = ? ORDER BY "
      + quote( KettleDatabaseRepository.FIELD_DIRECTORY_DIRECTORY_NAME ), id_directory );
  }

  public void saveRepositoryDirectory( RepositoryDirectoryInterface dir ) throws KettleException {
    try {
      ObjectId id_directory_parent = null;
      if ( dir.getParent() != null ) {
        id_directory_parent = dir.getParent().getObjectId();
      }

      dir.setObjectId( insertDirectory( id_directory_parent, dir ) );

      log.logDetailed( "New id of directory = " + dir.getObjectId() );

      repository.commit();
    } catch ( Exception e ) {
      throw new KettleException( "Unable to save directory [" + dir + "] in the repository", e );
    }
  }

  public void delRepositoryDirectory( RepositoryDirectoryInterface dir, boolean deleteNonEmptyFolder ) throws KettleException {
    try {
      if ( !deleteNonEmptyFolder ) {
        String[] trans = repository.getTransformationNames( dir.getObjectId(), false ); // TODO : include or exclude
                                                                                        // deleted objects?
        String[] jobs = repository.getJobNames( dir.getObjectId(), false ); // TODO : include or exclude deleted
                                                                            // objects?
        ObjectId[] subDirectories = repository.getSubDirectoryIDs( dir.getObjectId() );
        if ( trans.length == 0 && jobs.length == 0 && subDirectories.length == 0 ) {
          repository.directoryDelegate.deleteDirectory( dir.getObjectId() );
          repository.commit();
        } else {
          throw new KettleException( "This directory is not empty!" );
        }
      } else {
        repository.directoryDelegate.deleteDirectory( dir );
        repository.commit();
      }
    } catch ( Exception e ) {
      throw new KettleException( "Unexpected error deleting repository directory:", e );
    }
  }

  /**
   * @deprecated use {@link #renameRepositoryDirectory(ObjectId, RepositoryDirectoryInterface, String)}
   *
   * @param dir
   * @return
   * @throws KettleException
   */
  @Deprecated
  public ObjectId renameRepositoryDirectory( RepositoryDirectory dir ) throws KettleException {
    try {
      renameDirectory( dir.getObjectId(), null, dir.getName() );
      return dir.getObjectId(); // doesn't change in this specific case.
    } catch ( Exception e ) {
      throw new KettleException( "Unable to rename the specified repository directory [" + dir + "]", e );
    }
  }

  public ObjectId renameRepositoryDirectory( ObjectId id, RepositoryDirectoryInterface newParentDir, String newName ) throws KettleException {
    ObjectId parentId = null;
    if ( newParentDir != null ) {
      parentId = newParentDir.getObjectId();
    }

    try {
      renameDirectory( id, parentId, newName );
      return id; // doesn't change in this specific case.
    } catch ( Exception e ) {
      throw new KettleException( "Unable to rename the specified repository directory [" + id + "]", e );
    }
  }

  /**
   * Create a new directory, possibly by creating several sub-directies of / at the same time.
   *
   * @param parentDirectory
   *          the parent directory
   * @param directoryPath
   *          The path to the new Repository Directory, to be created.
   * @return The created sub-directory
   * @throws KettleException
   *           In case something goes wrong
   */
  public RepositoryDirectoryInterface createRepositoryDirectory( RepositoryDirectoryInterface parentDirectory,
    String directoryPath ) throws KettleException {

    // RepositoryDirectoryInterface refreshedParentDir =
    // repository.loadRepositoryDirectoryTree().findDirectory(parentDirectory.getPath());

    RepositoryDirectoryInterface refreshedParentDir = parentDirectory;
    String[] path = Const.splitPath( directoryPath, RepositoryDirectory.DIRECTORY_SEPARATOR );

    RepositoryDirectoryInterface parent = refreshedParentDir;
    for ( int level = 0; level < path.length; level++ ) {

      RepositoryDirectoryInterface rd = parent.findChild( path[level] );
      if ( rd == null ) {
        // This child directory doesn't exists, let's add it!
        //
        rd = new RepositoryDirectory( parent, path[level] );
        saveRepositoryDirectory( rd );

        // Don't forget to add this directory to the tree!
        //
        parent.addSubdirectory( rd );

        parent = rd;
      } else {
        parent = rd;
      }
    }
    return parent;
  }

}
