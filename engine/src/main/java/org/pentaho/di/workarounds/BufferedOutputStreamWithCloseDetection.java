/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
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
