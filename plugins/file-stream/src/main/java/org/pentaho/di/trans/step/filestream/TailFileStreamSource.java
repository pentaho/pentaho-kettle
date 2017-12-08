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

package org.pentaho.di.trans.step.filestream;

import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.streaming.api.StreamSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A simple example implementation of StreamSource which streams rows from a specified file to an iterable.
 * <p>
 * Note that this class is strictly meant as an example and not intended for real use. It uses a simplistic strategy of
 * leaving a BufferedReader open in order to load rows as they come in, without real consideration of error conditions.
 */
public class TailFileStreamSource implements StreamSource<List<String>> {

  private static Class<?> PKG = FileStream.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$
  private final AtomicReference<BufferedReader> reader = new AtomicReference<>();
  private final AtomicBoolean paused = new AtomicBoolean( false );
  private final AtomicBoolean closed = new AtomicBoolean( false );

  private final Logger logger = LoggerFactory.getLogger( getClass() );

  public TailFileStreamSource( String filename ) throws FileNotFoundException {
    reader.set( new BufferedReader( new FileReader( filename ) ) );
  }

  @Override public Iterable<List<String>> rows() {
    return () -> fileIterator();
  }

  @Override public void close() {
    closed.set( true );
    try {
      if ( reader != null ) {
        reader.get().close();
      }
    } catch ( IOException e ) {
      logger.error( BaseMessages.getString( PKG, "FileStream.Error.FileCloseError" ), e );

    }
  }

  @Override public void pause() {
    this.paused.set( true );
  }

  @Override public void resume() {
    this.paused.set( false );
  }

  private Iterator<List<String>> fileIterator() {
    return new Iterator<List<String>>() {

      @Override public boolean hasNext() {
        return !closed.get();
      }

      @Override public List<String> next() {
        String currentLine = null;
        try {

          while ( paused.get() || ( currentLine = reader.get().readLine() ) == null ) {
            Thread.sleep( 500 );
          }
        } catch ( IOException | InterruptedException e ) {
          logger.error( BaseMessages.getString( PKG, "FileStream.Error.FileStreamError" ), e );
        }
        return Collections.singletonList( currentLine );
      }
    };

  }
}
