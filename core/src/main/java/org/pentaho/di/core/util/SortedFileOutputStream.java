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

package org.pentaho.di.core.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Vector;

import org.pentaho.di.core.logging.LogChannelInterface;

public class SortedFileOutputStream extends FileOutputStream {
  /** Internal buffer to catch output. Before really writing output, the properties get sorted. */
  private StringBuilder sb = null;

  /** Logger, for the few errors that may occur. */
  private LogChannelInterface log = null;

  /**
   * CT
   *
   * @param file
   * @throws FileNotFoundException
   */
  public SortedFileOutputStream( File file ) throws FileNotFoundException {
    super( file );
  }

  /**
   * Setter
   *
   * @param log
   */
  public void setLogger( LogChannelInterface log ) {
    this.log = log;
  }

  /**
   * Appending to internal StringBuilder, instead of immediately writing to the file
   */
  @Override
  public void write( byte[] b, int off, int len ) throws IOException {
    if ( sb == null ) {
      sb = new StringBuilder();
    }
    sb.append( new String( b, off, len ) );
  }

  /**
   * Appending to internal StringBuilder, instead of immediately writing to the file
   */
  @Override
  public void write( byte[] b ) throws IOException {
    if ( sb == null ) {
      sb = new StringBuilder();
    }
    sb.append( new String( b ) );
  }

  /**
   * Appending to internal StringBuilder, instead of immediately writing to the file
   */
  @Override
  public void write( int b ) throws IOException {
    if ( sb == null ) {
      sb = new StringBuilder();
    }
    sb.append( b );
  }

  /**
   * Catch <code>flush</code> method, don't do nothing
   */
  @Override
  public void flush() throws IOException {
  }

  /**
   * If internally stored content is available, sorting keys of content, then sending content to file. Then calling
   * {@link FileOutputStream#close()} method.
   */
  @Override
  public void close() throws IOException {
    if ( sb == null || sb.length() == 0 ) {
      super.flush();
      super.close();
    }

    int[] iPos = new int[1];
    iPos[0] = 0;
    String sLine = nextLine( iPos );

    Vector<String> lines = new Vector<String>();
    while ( sLine != null ) {
      // Length 0 -> do nothing
      if ( sLine.length() == 0 ) {
        sLine = nextLine( iPos );
        continue;
      }

      // Character at first position is a '#' -> this is a comment
      if ( sLine.charAt( 0 ) == '#' ) {
        super.write( sLine.getBytes() );
        sLine = nextLine( iPos );
        continue;
      }

      // Get first occurrence of '=' character, that is not a position 0 and not
      // escaped by a '\\'
      int idx = sLine.indexOf( '=' );
      if ( idx <= 0 ) {
        // '=' either does not exist or is at first position (that should never happen!).
        // Write line immediately
        log
          .logError(
            this.getClass().getName(), "Unexpected: '=' character not found or found at first position." );
        super.write( sLine.getBytes() );
      } else {
        while ( idx != -1 && sLine.charAt( idx - 1 ) == '\\' ) {
          idx = sLine.indexOf( '=', idx + 1 );
        }

        if ( idx == -1 ) {
          log.logError(
            this.getClass().getName(), "Unexpected: No '=' character found that is not escaped by a '\\'." );
          super.write( sLine.getBytes() );
        } else {
          lines.add( sLine );
        }
      }
      sLine = nextLine( iPos );
    }

    Collections.sort( lines );
    for ( String line : lines ) {
      super.write( line.getBytes() );
    }
    super.flush();
    super.close();
  }

  /**
   * Get next line. The line end is marked at the first occurrence of an unescaped '\n' or '\r' character. All following
   * '\n' or '\r' characters after the first unescaped '\n' or '\r' character are included in the line.
   *
   * @param iPos
   *          The position from where to start at. This is passed as array of size one to <i>pass back</i> the parsing
   *          position (kind of C++ reference pass)
   * @return
   */
  private String nextLine( int[] iPos ) {
    // End of StringBuilder reached?
    if ( iPos[0] >= sb.length() ) {
      return null;
    }

    // Remember start
    int iStart = iPos[0];
    char c = sb.charAt( iPos[0] );

    // Read until end of stream reached or first '\n' or '\r' character found
    while ( iPos[0] < sb.length() && c != '\n' && c != '\r' ) {
      c = sb.charAt( iPos[0]++ );

      // If now we have '\r' or '\n' and they are escaped, we just read the next
      // character. For this at least two characters must have been read.
      if ( iPos[0] >= 2 ) {
        // Is it an escaped '\r' or '\n'?
        if ( ( c == '\n' || c == '\r' ) && ( iPos[0] - 2 == '\\' ) ) {
          // Yes! Just read next character, if not end of stream reached
          if ( iPos[0] < sb.length() ) {
            c = sb.charAt( iPos[0]++ );
          }
        }
      }
    }

    // Either we've found a '\r' or '\n' character or we are at the end of the stream.
    // In either case return.
    if ( iPos[0] == sb.length() ) {
      // Return complete remainder
      return sb.substring( iStart );
    } else {
      // Consume characters as long as '\r' or '\n' is found.
      while ( iPos[0] < sb.length() && ( c == '\n' || c == '\r' ) ) {
        c = sb.charAt( iPos[0]++ );
      }

      // Return complete remainder or part of stream
      if ( iPos[0] == sb.length() ) {
        return sb.substring( iStart );
      } else {
        iPos[0]--;
        return sb.substring( iStart, iPos[0] );
      }
    }
  }
}
