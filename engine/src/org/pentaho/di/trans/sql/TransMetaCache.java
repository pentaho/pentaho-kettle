package org.pentaho.di.trans.sql;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.vfs.FileObject;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectory;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.repository.RepositoryObject;
import org.pentaho.di.repository.RepositoryObjectType;
import org.pentaho.di.trans.TransMeta;

public class TransMetaCache {
  private Repository repository;

  private Map<ObjectId, TransMeta> objectIdMap;

  private Map<String, TransMeta> pathMap;

  private Map<String, TransMeta> fileMap;

  private int maxElements;

  /**
   * Instantiate a new TransMeta cache.
   * @param repository The repository to cache or null if no repository is used.
   * @param maxElements The maximum number of elements in either the repository or the file cache.
   */
  public TransMetaCache( Repository repository, int maxElements ) {
    this.repository = repository;
    objectIdMap = new HashMap<ObjectId, TransMeta>();
    pathMap = new HashMap<String, TransMeta>();
    fileMap = new HashMap<String, TransMeta>();
  }

  public TransMeta loadTransMeta( ObjectId transObjectId ) throws KettleException {

    if ( repository == null ) {
      throw new KettleException( "No repository available. Unable to load transformation by object ID" );
    }

    // Check the cache for the transformation...
    //
    TransMeta transMeta = objectIdMap.get( transObjectId );
    if ( transMeta == null ) {
      // Keep the transformation in the cache!
      transMeta = updateCache( transObjectId );
    } else {
      RepositoryObject objectInformation = repository.getObjectInformation( transObjectId, RepositoryObjectType.TRANSFORMATION );
      if ( objectInformation == null
        || objectInformation.getModifiedDate() == null
        || transMeta.getModifiedDate() == null
        || transMeta.getModifiedDate().getTime() < objectInformation.getModifiedDate().getTime() ) {
        // The file has been modified in the repository
        //
        transMeta = updateCache( transObjectId );
      }
    }
    return transMeta;
  }

  public TransMeta loadTransMeta( String name, RepositoryDirectoryInterface directory ) throws KettleException {

    if ( repository == null ) {
      throw new KettleException( "No repository available. Unable to load transformation by name and directory." );
    }

    String path = getPath( name, directory );
    // Check the cache for the transformation...
    //
    TransMeta transMeta = pathMap.get( path );
    if ( transMeta == null ) {
      // Keep the transformation in the cache!
      transMeta = updateCache( name, directory );
    } else {
      RepositoryObject objectInformation = repository.getObjectInformation( transMeta.getObjectId(), RepositoryObjectType.TRANSFORMATION );
      if ( objectInformation == null
        || objectInformation.getModifiedDate() == null
        || transMeta.getModifiedDate() == null
        || transMeta.getModifiedDate().getTime() < objectInformation.getModifiedDate().getTime() ) {
        // The file has been modified in the repository
        //
        transMeta = updateCache( name, directory );
      }
    }
    return transMeta;
  }

  public TransMeta loadTransMeta( String filename ) throws KettleException {

    try {
      FileObject fileObject = KettleVFS.getFileObject( filename );
      long lastModified = fileObject.getContent().getLastModifiedTime();

      TransMeta transMeta = fileMap.get( filename );
      if ( transMeta == null ) {
        // Keep the transformation in the cache!
        transMeta = updateCache( filename );
        transMeta.setModifiedDate( new Date( lastModified ) );
      } else {
        if ( transMeta.getModifiedDate() == null
          || transMeta.getModifiedDate().getTime() < lastModified ) {
          // The file has been modified, reload
          //
          transMeta = updateCache( filename );
          transMeta.setModifiedDate( new Date( lastModified ) );
        }
      }
      return transMeta;
    } catch ( org.apache.commons.vfs.FileSystemException e ) {
      throw new KettleException( "Unable to check modification date of file", e );
    }

  }

  private TransMeta updateCache( String filename ) throws KettleException {
    TransMeta transMeta = new TransMeta( filename );
    fileMap.put( filename, transMeta );

    return transMeta;
  }

  private TransMeta updateCache( ObjectId transObjectId ) throws KettleException {
    TransMeta transMeta = repository.loadTransformation( transObjectId, null ); // always last version
    String path = getPath( transMeta.getName(), transMeta.getRepositoryDirectory() );
    updateCache( transObjectId, path, transMeta );
    return transMeta;
  }

  private TransMeta updateCache( String name, RepositoryDirectoryInterface directory ) throws KettleException {
    TransMeta transMeta = repository.loadTransformation( name, directory, null, true, null ); // always last version
    String path = getPath( transMeta.getName(), transMeta.getRepositoryDirectory() );
    updateCache( transMeta.getObjectId(), path, transMeta );
    return transMeta;
  }

  private void updateCache( ObjectId transObjectId, String path, TransMeta transMeta ) {
    objectIdMap.put( transObjectId, transMeta );
    pathMap.put( path, transMeta );

    // Remove the oldest ones in case we exceed the maximum nr of elements...
    // We use a buffer-overrun to be a bit more efficient when clearing old members in the cache.
    //
    int bufferOverRun = 5;
    if ( objectIdMap.size() > maxElements + bufferOverRun ) {
      List<TransMeta> list = new ArrayList<TransMeta>( objectIdMap.values() );
      Collections.sort( list, new Comparator<TransMeta>() {
        public int compare( TransMeta o1, TransMeta o2 ) {
          return o1.getModifiedDate().compareTo( o2.getModifiedDate() );
        }
      } );

      // Simply Remove the oldest ones
      for ( int i = 0; i < bufferOverRun; i++ ) {
        TransMeta oldest = list.get( i );
        objectIdMap.remove( oldest.getObjectId() );
        pathMap.remove( getPath( oldest.getName(), oldest.getRepositoryDirectory() ) );
      }
    }
  }

  private String getPath( String transName, RepositoryDirectoryInterface directory ) {
    String path;
    if ( directory.isRoot() ) {
      path = directory.getPath() + transName;
    } else {
      path = directory.getPath() + RepositoryDirectory.DIRECTORY_SEPARATOR + transName;
    }
    return path;
  }

  /**
   * @return the objectIdMap
   */
  public Map<ObjectId, TransMeta> getObjectIdMap() {
    return objectIdMap;
  }

  /**
   * @return the pathMap
   */
  public Map<String, TransMeta> getPathMap() {
    return pathMap;
  }

  /**
   * @return the fileMap
   */
  public Map<String, TransMeta> getFileMap() {
    return fileMap;
  }
}
