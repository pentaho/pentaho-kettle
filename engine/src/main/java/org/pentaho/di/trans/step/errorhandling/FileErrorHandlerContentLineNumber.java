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

import java.util.Date;

import org.apache.commons.vfs2.FileObject;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.step.BaseStep;

public class FileErrorHandlerContentLineNumber extends AbstractFileErrorHandler {
  private static Class<?> PKG = FileErrorHandlerContentLineNumber.class; // for i18n purposes, needed by Translator2!!

  public FileErrorHandlerContentLineNumber( Date date, String destinationDirectory, String fileExtension,
    String encoding, BaseStep baseStep ) {
    super( date, destinationDirectory, fileExtension, encoding, baseStep );
  }

  public void handleLineError( long lineNr, String filePart ) throws KettleException {
    try {
      getWriter( filePart ).write( String.valueOf( lineNr ) );
      getWriter( filePart ).write( Const.CR );
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString(
        PKG, "FileErrorHandlerContentLineNumber.Exception.CouldNotCreateWriteLine" )
        + lineNr, e );

    }
  }

  public void handleNonExistantFile( FileObject file ) {
  }

  public void handleNonAccessibleFile( FileObject file ) {
  }

}
