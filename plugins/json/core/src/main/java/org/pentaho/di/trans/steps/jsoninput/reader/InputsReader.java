/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2016-2017 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.jsoninput.reader;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Iterator;

import org.apache.commons.io.IOUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.steps.file.BaseFileInputStepData;
import org.pentaho.di.trans.steps.jsoninput.JsonInput;
import org.pentaho.di.trans.steps.jsoninput.JsonInputData;
import org.pentaho.di.trans.steps.jsoninput.JsonInputMeta;

public class InputsReader implements Iterable<InputStream> {

  private JsonInput step;
  private JsonInputMeta meta;
  private JsonInputData data;
  private ErrorHandler errorHandler;

  public InputsReader( JsonInput step, JsonInputMeta meta, JsonInputData data, ErrorHandler errorHandler ) {
    this.step = step;
    this.meta = meta;
    this.data = data;
    this.errorHandler = errorHandler;
  }

  @Override
  public Iterator<InputStream> iterator() {
    if ( !meta.isInFields() || meta.getIsAFile() ) {
      Iterator<FileObject> files;
      if ( meta.inputFiles.acceptingFilenames ) {
        // paths from input
        files = new FileNamesIterator( step, errorHandler, getFieldIterator() );
      } else {
        // from inner file list
        if ( data.files == null ) {
          data.files = meta.getFileInputList( step );
        }
        files = data.files.getFiles().listIterator( data.currentFileIndex );
      }
      return new FileContentIterator( files, data, errorHandler );
    } else if ( meta.isReadUrl() ) {
      return  new URLContentIterator( errorHandler, getFieldIterator() );
    } else {
      // direct content
      return new ChainedIterator<InputStream, String>( getFieldIterator(), errorHandler ) {
        protected InputStream tryNext() throws IOException {
          String next = inner.next();
          return next == null ? null : IOUtils.toInputStream( next, meta.getEncoding() );
        }
      };
    }
  }

  protected StringFieldIterator getFieldIterator() {
    return new StringFieldIterator(
        new RowIterator( step, data, errorHandler ), data.indexSourceField );
  }

  public static interface ErrorHandler {
    /**
     * Generic (unexpected errors)
     */
    void error( Exception thrown );

    void fileOpenError( FileObject file, FileSystemException exception );
    void fileCloseError( FileObject file, FileSystemException exception );

  }

  protected abstract class ChainedIterator<T, C> implements Iterator<T> {

    protected Iterator<C> inner;
    protected ErrorHandler handler;

    ChainedIterator( Iterator<C> inner, ErrorHandler handler ) {
      this.inner = inner;
      this.handler = handler;
    }

    @Override
    public boolean hasNext() {
      return inner.hasNext();
    }

    @Override
    public T next() {
      try {
        return tryNext();
      } catch ( Exception e ) {
        handler.error( e );
        return null;
      }
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException( "remove" );
    }

    protected abstract T tryNext() throws Exception;
  }

  protected class FileContentIterator extends ChainedIterator<InputStream, FileObject> {

    ErrorHandler handler;
    BaseFileInputStepData data;
    FileContentIterator( Iterator<FileObject> inner, BaseFileInputStepData data, ErrorHandler handler ) {
      super( inner, handler );
      this.data = data;
    }

    @Override
    public InputStream tryNext() {
      if ( hasNext() ) {
        if ( data.file != null ) {
          try {
            data.file.close();
          } catch ( FileSystemException e ) {
            handler.fileCloseError( data.file, e );
          }
        }
        try {
          data.file = inner.next();
          data.currentFileIndex++;
          if ( step.onNewFile( data.file ) ) {
            return KettleVFS.getInputStream( data.file );
          }
        } catch ( FileSystemException e ) {
          handler.fileOpenError( data.file, e );
        }
      }
      return null;
    }
  }

  protected class FileNamesIterator extends ChainedIterator<FileObject, String> {

    private VariableSpace vars;

    public FileNamesIterator( VariableSpace varSpace, ErrorHandler handler, Iterator<String> fileNames ) {
      super( fileNames, handler );
      vars = varSpace;
    }

    @Override
    public FileObject tryNext() throws KettleFileException {
      String fileName = step.environmentSubstitute( inner.next() );
      return fileName == null ? null : KettleVFS.getFileObject( fileName, vars );
    }
  }

  protected class URLContentIterator extends ChainedIterator<InputStream, String> {

    public URLContentIterator( ErrorHandler handler, Iterator<String> urls ) {
      super( urls, handler );
    }

    @Override protected InputStream tryNext() throws Exception {
      if ( hasNext() ) {
        URL url = new URL( step.environmentSubstitute( inner.next() ) );
        URLConnection connection = url.openConnection();
        return connection.getInputStream();
      }
      return null;
    }
  }

  protected class StringFieldIterator implements Iterator<String> {

    private RowIterator rowIter;
    private int idx;

    public StringFieldIterator( RowIterator rowIter, int idx ) {
      this.rowIter = rowIter;
      this.idx = idx;
    }

    public boolean hasNext() {
      return rowIter.hasNext();
    }

    public String next() {
      Object[] row = rowIter.next();
      return ( row == null || row.length <= idx )
          ? null
          : (String) row[idx];
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException( "remove" );
    }
  }

  protected class RowIterator implements Iterator<Object[]> {

    private StepInterface step;
    private ErrorHandler errorHandler;
    private boolean gotNext;

    public RowIterator( StepInterface step, JsonInputData data, ErrorHandler errorHandler ) {
      this.step = step;
      this.errorHandler = errorHandler;
      gotNext = data.readrow != null;
    }

    protected void fetchNext() {
      try {
        data.readrow = step.getRow();
        gotNext = true;
      } catch ( KettleException e ) {
        errorHandler.error( e );
      }
    }

    @Override
    public boolean hasNext() {
      if ( !gotNext ) {
        fetchNext();
      }
      return data.readrow != null;
    }

    @Override
    public Object[] next() {
      if ( hasNext() ) {
        gotNext = false;
        return data.readrow;
      }
      return null;
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException( "remove" );
    }
  }

}
