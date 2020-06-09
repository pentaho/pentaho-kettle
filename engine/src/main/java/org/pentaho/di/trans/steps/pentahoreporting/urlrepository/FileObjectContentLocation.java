/*
* This program is free software; you can redistribute it and/or modify it under the
* terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
* Foundation.
*
* You should have received a copy of the GNU Lesser General Public License along with this
* program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
* or from the Free Software Foundation, Inc.,
* 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*
* This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
* without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
* See the GNU Lesser General Public License for more details.
*
* Copyright (c) 2006 - 2020 Hitachi Vantara and Contributors.  All rights reserved.
*/

package org.pentaho.di.trans.steps.pentahoreporting.urlrepository;

import java.io.File;
import java.io.IOException;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.pentaho.reporting.libraries.repository.ContentCreationException;
import org.pentaho.reporting.libraries.repository.ContentEntity;
import org.pentaho.reporting.libraries.repository.ContentIOException;
import org.pentaho.reporting.libraries.repository.ContentItem;
import org.pentaho.reporting.libraries.repository.ContentLocation;
import org.pentaho.reporting.libraries.repository.Repository;
import org.pentaho.reporting.libraries.repository.RepositoryUtilities;

/**
 * A content-location that uses a directory as backend.
 *
 * @author Thomas Morgner
 * @author Alexander Buloichik
 */
public class FileObjectContentLocation extends FileObjectContentEntity implements ContentLocation {
  private static final long serialVersionUID = -5452372293937107734L;

  /**
   * Creates a new location for the given parent and directory.
   *
   * @param parent  the parent location.
   * @param backend the backend.
   * @throws ContentIOException if an error occured or the file did not point to a directory.
   */
  public FileObjectContentLocation( final ContentLocation parent, final FileObject backend ) throws ContentIOException {
    super( parent, backend );
    boolean error;
    try {
      error = backend.exists() == false || backend.isFolder() == false;
    } catch ( FileSystemException e ) {
      throw new RuntimeException( e );
    }
    if ( error ) {
      throw new ContentIOException( "The given backend-file is not a directory." );
    }
  }

  /**
   * Creates a new root-location for the given repository and directory.
   *
   * @param repository the repository for which a location should be created.
   * @param backend    the backend.
   * @throws ContentIOException if an error occured or the file did not point to a directory.
   */
  public FileObjectContentLocation( final Repository repository, final FileObject backend ) throws ContentIOException {
    super( repository, backend );
    boolean error;
    try {
      error = backend.exists() == false || backend.isFolder() == false;
    } catch ( FileSystemException e ) {
      throw new RuntimeException( e );
    }
    if ( error ) {
      throw new ContentIOException( "The given backend-file is not a directory." );
    }
  }

  /**
   * Lists all content entities stored in this content-location. This method filters out all files that have an invalid
   * name (according to the repository rules).
   *
   * @return the content entities for this location.
   * @throws ContentIOException if an repository error occured.
   */
  public ContentEntity[] listContents() throws ContentIOException {
    try {
      final FileObject file = getBackend();
      final FileObject[] files = file.getChildren();
      final ContentEntity[] entities = new ContentEntity[files.length];
      for ( int i = 0; i < files.length; i++ ) {
        final FileObject child = files[i];
        if ( RepositoryUtilities.isInvalidPathName( child.getPublicURIString() ) ) {
          continue;
        }

        if ( child.isFolder() ) {
          entities[i] = new FileObjectContentLocation( this, child );
        } else if ( child.isFile() ) {
          entities[i] = new FileObjectContentLocation( this, child );
        }
      }
      return entities;
    } catch ( FileSystemException e ) {
      throw new RuntimeException( e );
    }
  }

  /**
   * Returns the content entity with the given name. If the entity does not exist, an Exception will be raised.
   *
   * @param name the name of the entity to be retrieved.
   * @return the content entity for this name, never null.
   * @throws ContentIOException if an repository error occured.
   */
  public ContentEntity getEntry( final String name ) throws ContentIOException {
    try {
      if ( RepositoryUtilities.isInvalidPathName( name ) ) {
        throw new IllegalArgumentException( "The name given is not valid." );
      }

      final FileObject file = getBackend();
      final FileObject child = file.resolveFile( name );
      if ( child.exists() == false ) {
        throw new ContentIOException( "Not found:" + child );
      }

      if ( child.isFolder() ) {
        return new FileObjectContentLocation( this, child );
      } else if ( child.isFile() ) {
        return new FileObjectContentItem( this, child );
      } else {
        throw new ContentIOException( "Not File nor directory." );
      }
    } catch ( FileSystemException e ) {
      throw new RuntimeException( e );
    }
  }

  /**
   * Creates a new data item in the current location. This method must never return null. This method will fail if an
   * entity with the same name exists in this location.
   *
   * @param name the name of the new entity.
   * @return the newly created entity, never null.
   * @throws ContentCreationException if the item could not be created.
   */
  public ContentItem createItem( final String name ) throws ContentCreationException {
    String fileName = new File( name ).getName();
    if ( RepositoryUtilities.isInvalidPathName( fileName ) ) {
      throw new IllegalArgumentException( "The name given is not valid." );
    }
    try {
      final FileObject file = getBackend();
      final FileObject child = file.resolveFile( fileName );
      if ( child.exists() ) {
        if ( child.getContent().getSize() == 0 ) {
          // probably one of the temp files created by the pentaho-system
          return new FileObjectContentItem( this, child );
        }
        throw new ContentCreationException( "File already exists: " + child );
      }
      try {
        child.createFile();
        return new FileObjectContentItem( this, child );
      } catch ( IOException e ) {
        throw new ContentCreationException( "IOError while create", e );
      }
    } catch ( FileSystemException e ) {
      throw new RuntimeException( e );
    }
  }

  /**
   * Creates a new content location in the current location. This method must never return null. This method will fail
   * if an entity with the same name exists in this location.
   *
   * @param name the name of the new entity.
   * @return the newly created entity, never null.
   * @throws ContentCreationException if the item could not be created.
   */
  public ContentLocation createLocation( final String name ) throws ContentCreationException {
    if ( RepositoryUtilities.isInvalidPathName( name ) ) {
      throw new IllegalArgumentException( "The name given is not valid." );
    }
    try {
      final FileObject file = getBackend();
      final FileObject child = file.resolveFile( name );
      if ( child.exists() ) {
        throw new ContentCreationException( "File already exists." );
      }
      child.createFile();
      try {
        return new FileObjectContentLocation( this, child );
      } catch ( ContentIOException e ) {
        throw new ContentCreationException( "Failed to create the content-location", e );
      }
    } catch ( FileSystemException e ) {
      throw new RuntimeException( e );
    }
  }

  /**
   * Checks, whether an content entity with the given name exists in this content location. This method will report
   * invalid filenames as non-existent.
   *
   * @param name the name of the new entity.
   * @return true, if an entity exists with this name, false otherwise.
   */
  public boolean exists( final String name ) {
    if ( RepositoryUtilities.isInvalidPathName( name ) ) {
      return false;
    }
    try {
      final FileObject file = getBackend();
      final FileObject child = file.resolveFile( name );
      return child.exists();
    } catch ( FileSystemException e ) {
      throw new RuntimeException( e );
    }
  }
}
