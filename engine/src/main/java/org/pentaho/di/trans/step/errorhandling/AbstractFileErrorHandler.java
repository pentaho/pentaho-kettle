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

package org.pentaho.di.trans.step.errorhandling;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.vfs2.FileObject;
import org.pentaho.di.core.ResultFile;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.step.BaseStep;

public abstract class AbstractFileErrorHandler implements FileErrorHandler {
  private static Class<?> PKG = AbstractFileErrorHandler.class; // for i18n purposes, needed by Translator2!!

  private static final String DD_MMYYYY_HHMMSS = "ddMMyyyy-HHmmss";

  public static final String NO_PARTS = "NO_PARTS";

  private final String destinationDirectory;

  private final String fileExtension;

  private final String encoding;

  private String processingFilename;

  private Map<Object, Writer> writers;

  private String dateString;

  private BaseStep baseStep;

  public AbstractFileErrorHandler( Date date, String destinationDirectory, String fileExtension, String encoding,
    BaseStep baseStep ) {
    this.destinationDirectory = destinationDirectory;
    this.fileExtension = fileExtension;
    this.encoding = encoding;
    this.baseStep = baseStep;
    this.writers = new HashMap<Object, Writer>();
    initDateFormatter( date );
  }

  private void initDateFormatter( Date date ) {
    dateString = createDateFormat().format( date );
  }

  public static DateFormat createDateFormat() {
    return new SimpleDateFormat( DD_MMYYYY_HHMMSS );
  }

  public static FileObject getReplayFilename( String destinationDirectory, String processingFilename,
    String dateString, String extension, Object source ) throws KettleFileException {
    String name = null;
    String sourceAdding = "";
    if ( !NO_PARTS.equals( source ) ) {
      sourceAdding = "_" + source.toString();
    }
    if ( extension == null || extension.length() == 0 ) {
      name = processingFilename + sourceAdding + "." + dateString;
    } else {
      name = processingFilename + sourceAdding + "." + dateString + "." + extension;
    }
    return KettleVFS.getFileObject( destinationDirectory + "/" + name );
  }

  public static FileObject getReplayFilename( String destinationDirectory, String processingFilename, Date date,
    String extension, Object source ) throws KettleFileException {
    return getReplayFilename(
      destinationDirectory, processingFilename, createDateFormat().format( date ), extension, source );
  }

  /**
   * returns the OutputWiter if exists. Otherwhise it will create a new one.
   *
   * @return
   * @throws KettleException
   */
  Writer getWriter( Object source ) throws KettleException {
    try {
      Writer outputStreamWriter = writers.get( source );
      if ( outputStreamWriter != null ) {
        return outputStreamWriter;
      }
      FileObject file =
        getReplayFilename( destinationDirectory, processingFilename, dateString, fileExtension, source );
      ResultFile resultFile =
        new ResultFile( ResultFile.FILE_TYPE_GENERAL, file, baseStep.getTransMeta().getName(), baseStep
          .getStepname() );
      baseStep.addResultFile( resultFile );
      try {
        if ( encoding == null ) {
          outputStreamWriter = new OutputStreamWriter( KettleVFS.getOutputStream( file, false ) );
        } else {
          outputStreamWriter = new OutputStreamWriter( KettleVFS.getOutputStream( file, false ), encoding );
        }
      } catch ( Exception e ) {
        throw new KettleException( BaseMessages.getString(
          PKG, "AbstractFileErrorHandler.Exception.CouldNotCreateFileErrorHandlerForFile" )
          + file.getName().getURI(), e );
      }
      writers.put( source, outputStreamWriter );
      return outputStreamWriter;
    } catch ( KettleFileException e ) {
      throw new KettleException( BaseMessages.getString(
        PKG, "AbstractFileErrorHandler.Exception.CouldNotCreateFileErrorHandlerForFile" ), e );
    }
  }

  public void close() throws KettleException {
    for ( Iterator<Writer> iter = writers.values().iterator(); iter.hasNext(); ) {
      close( iter.next() );
    }
    writers = new HashMap<Object, Writer>();

  }

  private void close( Writer outputStreamWriter ) throws KettleException {
    if ( outputStreamWriter != null ) {
      try {
        outputStreamWriter.flush();
      } catch ( IOException exception ) {
        baseStep.logError(
          BaseMessages.getString( PKG, "AbstractFileErrorHandler.Log.CouldNotFlushContentToFile" ), exception
            .getLocalizedMessage() );
      }
      try {
        outputStreamWriter.close();
      } catch ( IOException exception ) {
        throw new KettleException( BaseMessages.getString(
          PKG, "AbstractFileErrorHandler.Exception.CouldNotCloseFile" ), exception );
      } finally {
        outputStreamWriter = null;
      }
    }
  }

  public void handleFile( FileObject file ) throws KettleException {
    close();
    this.processingFilename = file.getName().getBaseName();
  }

}
