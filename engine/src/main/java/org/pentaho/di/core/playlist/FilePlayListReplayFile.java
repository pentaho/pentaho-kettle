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


package org.pentaho.di.core.playlist;

import org.apache.commons.vfs2.FileObject;
import org.pentaho.di.core.exception.KettleException;

public class FilePlayListReplayFile implements FilePlayList {
  private FileObject processingFile;
  private String processingFilePart;

  public FilePlayListReplayFile( FileObject processingFile, String processingFilePart ) {
    this.processingFile = processingFile;
    this.processingFilePart = processingFilePart;
  }

  FileObject getProcessingFile() {
    return processingFile;
  }

  String getProcessingFilePart() {
    return processingFilePart;
  }

  public boolean isProcessingNeeded( FileObject file, long lineNr, String filePart ) throws KettleException {
    return false;
  }
}
