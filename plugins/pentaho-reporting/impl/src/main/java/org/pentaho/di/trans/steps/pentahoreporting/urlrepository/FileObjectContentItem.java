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
