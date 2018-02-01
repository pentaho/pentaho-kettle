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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.pentaho.reporting.libraries.repository.ContentIOException;
import org.pentaho.reporting.libraries.repository.ContentItem;
import org.pentaho.reporting.libraries.repository.ContentLocation;

/**
 * A content item wrapping a file.
 *
 * @author Thomas Morgner
 * @author Alexander Buloichik
 */
public class FileObjectContentItem extends FileObjectContentEntity implements ContentItem {
  private static final long serialVersionUID = 5080072160607835550L;

  /**
   * Creates a new file based content item for the given file and parent location.
   *
   * @param parent  the parent.
   * @param backend the backend.
   */
  public FileObjectContentItem( final ContentLocation parent, final FileObject backend ) {
    super( parent, backend );
  }

  public String getMimeType() throws ContentIOException {
    final FileObjectRepository fileRepository = (FileObjectRepository) getRepository();
    return fileRepository.getMimeRegistry().getMimeType( this );
  }

  public OutputStream getOutputStream() throws ContentIOException, IOException {
    return getBackend().getContent().getOutputStream();
  }

  public InputStream getInputStream()
    throws ContentIOException, IOException {
    return getBackend().getContent().getInputStream();
  }

  public boolean isReadable() {
    try {
      return getBackend().isReadable();
    } catch ( FileSystemException e ) {
      throw new RuntimeException( e );
    }
  }

  public boolean isWriteable() {
    try {
      return getBackend().isWriteable();
    } catch ( FileSystemException e ) {
      throw new RuntimeException( e );
    }
  }
}
