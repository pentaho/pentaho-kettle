/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.di.core.playlist;

import java.io.IOException;

import org.apache.commons.vfs2.FileObject;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.trans.step.errorhandling.AbstractFileErrorHandler;

public class FilePlayListReplayErrorFile extends FilePlayListReplayFile {

  private FileObject errorFile;

  public FilePlayListReplayErrorFile( FileObject errorFile, FileObject processingFile ) {
    super( processingFile, AbstractFileErrorHandler.NO_PARTS );
    this.errorFile = errorFile;
  }

  public boolean isProcessingNeeded( FileObject file, long lineNr, String filePart ) throws KettleException {
    try {
      return errorFile.exists();
    } catch ( IOException e ) {
      throw new KettleException( e );
    }
  }

}
