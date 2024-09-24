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
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.pentaho.reporting.libraries.repository.ContentIOException;
import org.pentaho.reporting.libraries.repository.ContentLocation;
import org.pentaho.reporting.libraries.repository.DefaultMimeRegistry;
import org.pentaho.reporting.libraries.repository.MimeRegistry;
import org.pentaho.reporting.libraries.repository.UrlRepository;


/**
 * A file-repository uses a subset of the local filesystem to provide a repository view on top of it. This repository
 * type is the most commonly used repository, as most applications are allowed to access the local filesystem.
 *
 * @author Thomas Morgner
 * 
 * Implementation for VFS. Original implementation(FileRepository) should be removed after reporting will use VFS.
 * 
 * @author Alexander Buloichik
 */
public class FileObjectRepository implements UrlRepository, Serializable {
  private static final long serialVersionUID = -6221548332596506480L;

  private MimeRegistry mimeRegistry;
  private FileObjectContentLocation root;

  /**
   * Creates a new repository for the given file. The file must point to a directory. This constructor uses the default
   * mime-registry.
   *
   * @param file the directory, which should form the root of the repository.
   * @throws ContentIOException if an error prevents the repository creation.
   */
  public FileObjectRepository( final FileObject file ) throws ContentIOException {
    this( file, new DefaultMimeRegistry() );
  }

  /**
   * Creates a new repository for the given file. The file must point to a directory.
   *
   * @param file         the directory, which should form the root of the repository.
   * @param mimeRegistry the mime registry to be used.
   * @throws ContentIOException if an error prevents the repository creation.
   */
  public FileObjectRepository( final FileObject file, final MimeRegistry mimeRegistry ) throws ContentIOException {
    if ( mimeRegistry == null ) {
      throw new NullPointerException( "MimeRegistry must be given" );
    }
    if ( file == null ) {
      throw new NullPointerException( "File must be given" );
    }
    this.mimeRegistry = mimeRegistry;
    this.root = new FileObjectContentLocation( this, file );
  }

  /**
   * Returns the mime-registry for the repository.
   *
   * @return the mime-registry.
   */
  public MimeRegistry getMimeRegistry() {
    return mimeRegistry;
  }

  /**
   * Returns the repositories root directory entry.
   *
   * @return the root directory.
   * @throws ContentIOException if an error occurs.
   */
  public ContentLocation getRoot() throws ContentIOException {
    return root;
  }

  /**
   * Returns the URL that represents this repository. The meaning of the URL returned here is implementation specific
   * and is probably not suitable to resolve names to global objects.
   *
   * @return the repository's URL.
   * @throws MalformedURLException if the URL could not be computed.
   */
  public URL getURL() throws MalformedURLException {
    try {
      return root.getBackend().getURL();
    } catch ( FileSystemException e ) {
      throw new RuntimeException( e );
    }
  }
}
