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


package org.pentaho.di.workarounds;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Workaround for stream close issue under Java 8.
 *
 * The problem is: during Workbook writing to stream, Apache POI closes output stream itself. After that, ExcelWriteStep
 * closes this stream also because it was open in the ExcelWriterStep. But Java 8 contains bug
 * https://bugs.openjdk.java.net/browse/JDK-8042377 with second stream closing. As result, second close() throws
 * exception.
 */
public class BufferedOutputStreamWithCloseDetection extends BufferedOutputStream {
  boolean alreadyClosed = false;

  public BufferedOutputStreamWithCloseDetection( OutputStream out ) {
    super( out );
  }

  /**
   * Don't flush empty buffer if already closed.
   */
  @Override
  public synchronized void flush() throws IOException {
    if ( alreadyClosed && count == 0 ) {
      return;
    }
    super.flush();
  }

  /**
   * Close only once.
   */
  @Override
  public void close() throws IOException {
    if ( !alreadyClosed ) {
      super.close();
      alreadyClosed = true;
    }
  }
}
