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

package org.pentaho.di.trans.steps.pentahoreporting.urlrepository;

import java.io.Serializable;
import java.util.Date;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.pentaho.reporting.libraries.repository.ContentEntity;
import org.pentaho.reporting.libraries.repository.ContentLocation;
import org.pentaho.reporting.libraries.repository.LibRepositoryBoot;
import org.pentaho.reporting.libraries.repository.Repository;

/**
 * A content-entity that uses a java.io.File as backend. The entity can read the {@link
 * LibRepositoryBoot#SIZE_ATTRIBUTE} and can read and write the {@link LibRepositoryBoot#VERSION_ATTRIBUTE}.
 *
 * @author Thomas Morgner
 * @author Alexander Buloichik
 */
public abstract class FileObjectContentEntity implements ContentEntity, Serializable {
  private FileObject backend;
  private ContentLocation parent;
  private Repository repository;
  private static final long serialVersionUID = 3962114134995757847L;

  /**
   * Creates a new content-entity for the given file using the given content location as parent.
   *
   * @param parent  the content location representing the parent directory.
   * @param backend the file representing this entity.
   */
  protected FileObjectContentEntity( final ContentLocation parent, final FileObject backend ) {
    if ( backend == null ) {
      throw new NullPointerException( "Backend file must be given." );
    }
    if ( parent == null ) {
      throw new NullPointerException( "Parent file must be given." );
    }
    this.repository = parent.getRepository();
    this.parent = parent;
    this.backend = backend;
  }

  /**
   * Creates a new root content-entity for the given file using the given content location as parent.
   *
   * @param repository the repository for which this entity is created.
   * @param backend    the file representing this entity.
   */
  protected FileObjectContentEntity( final Repository repository, final FileObject backend ) {
    if ( backend == null ) {
      throw new NullPointerException( "Backend file must be given." );
    }
    if ( repository == null ) {
      throw new NullPointerException( "Repository file must be given." );
    }
    this.repository = repository;
    this.backend = backend;
  }

  /**
   * Returns the current repository, to which tis entity belongs.
   *
   * @return the repository.
   */
  public Repository getRepository() {
    return repository;
  }

  /**
   * Returns the name of the file.
   *
   * @return the name, never null.
   */
  public String getName() {
    return backend.getPublicURIString();
  }

  /**
   * Returns the file that provides the backend of this entity.
   *
   * @return the file, never null.
   */
  protected FileObject getBackend() {
    return backend;
  }

  /**
   * Returns a unique identifier. This can be canonical filename or a database key. It must be guaranteed that within
   * the same repository the key will be unique.
   *
   * @return the unique content ID.
   */
  public Object getContentId() {
    return backend;
  }

  /**
   * Returns a attribute value for the given domain (namespace) and attribute-name. Some generic attribute domains and
   * names are defined as constants in the {@link LibRepositoryBoot} class.
   *
   * @param domain the attribute domain.
   * @param key    the name of the attribute.
   * @return the value or null, if the content-entity does not have a value for this attribute.
   */
  public Object getAttribute( final String domain, final String key ) {
    try {
      if ( LibRepositoryBoot.REPOSITORY_DOMAIN.equals( domain ) ) {
        if ( LibRepositoryBoot.SIZE_ATTRIBUTE.equals( key ) ) {
          return new Long( backend.getContent().getSize() );
        } else if ( LibRepositoryBoot.VERSION_ATTRIBUTE.equals( key ) ) {
          return new Date( backend.getContent().getLastModifiedTime() );
        }
      }
    } catch ( FileSystemException ex ) {
      throw new RuntimeException( ex );
    }
    return null;
  }

  /**
   * Updates the attribute value for the given attribute domain and name. If the element is not writable or the
   * attribute could not be updated for any other reason, the method will return false. This method only returns true,
   * if the attribute has been updated successfully.
   *
   * @param domain the attribute domain.
   * @param key    the attribute name
   * @param value  the new attribute value.
   * @return true, if the update was successful, false otherwise.
   */
  public boolean setAttribute( final String domain, final String key, final Object value ) {
    try {
      if ( LibRepositoryBoot.REPOSITORY_DOMAIN.equals( domain ) ) {
        if ( LibRepositoryBoot.VERSION_ATTRIBUTE.equals( key ) ) {
          if ( value instanceof Date ) {
            final Date date = (Date) value;
            backend.getContent().setLastModifiedTime( date.getTime() );
            return true;
          } else if ( value instanceof Number ) {
            final Number time = (Number) value;
            backend.getContent().setLastModifiedTime( time.longValue() );
            return true;
          }
        }
      }
      return false;
    } catch ( FileSystemException ex ) {
      throw new RuntimeException( ex );
    }
  }

  /**
   * Returns a reference to the parent location. If this entity represents the root directory, this method will return
   * null.
   *
   * @return the parent or null, if this is the root-directory.
   */
  public ContentLocation getParent() {
    return parent;
  }

  /**
   * Attempts to delete the entity. After an entity has been deleted, any call to any of the methods of the entity may
   * produce undefined results.
   *
   * @return true, if the entity was deleted and detached from the repository, false otherwise.
   */
  public boolean delete() {
    try {
      backend.delete();
      return false;
    } catch ( FileSystemException ex ) {
      throw new RuntimeException( ex );
    }
  }
}
