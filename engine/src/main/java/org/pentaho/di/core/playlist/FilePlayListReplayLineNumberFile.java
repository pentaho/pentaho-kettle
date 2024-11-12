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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.vfs2.FileObject;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.vfs.KettleVFS;

class FilePlayListReplayLineNumberFile extends FilePlayListReplayFile {
  Set<Long> lineNumbers = new HashSet<Long>();

  public FilePlayListReplayLineNumberFile( FileObject lineNumberFile, String encoding, FileObject processingFile,
    String filePart ) throws KettleException {
    super( processingFile, filePart );
    initialize( lineNumberFile, encoding );
  }

  private void initialize( FileObject lineNumberFile, String encoding ) throws KettleException {
    BufferedReader reader = null;
    try {
      if ( encoding == null ) {
        reader = new BufferedReader( new InputStreamReader( KettleVFS.getInputStream( lineNumberFile ) ) );
      } else {
        reader =
          new BufferedReader( new InputStreamReader( KettleVFS.getInputStream( lineNumberFile ), encoding ) );
      }
      String line = null;
      while ( ( line = reader.readLine() ) != null ) {
        if ( line.length() > 0 ) {
          lineNumbers.add( Long.valueOf( line ) );
        }
      }
    } catch ( Exception e ) {
      throw new KettleException( "Could not read line number file " + lineNumberFile.getName().getURI(), e );
    } finally {
      if ( reader != null ) {
        try {
          reader.close();
        } catch ( IOException e ) {
          throw new KettleException( "Could not close line number file " + lineNumberFile.getName().getURI(), e );
        }
      }
    }
  }

  public boolean isProcessingNeeded( FileObject file, long lineNr, String filePart ) throws KettleException {
    return lineNumbers.contains( new Long( lineNr ) );
  }
}
