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
* Copyright (c) 2006 - 2018 Hitachi Vantara and Contributors.  All rights reserved.
*/

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
