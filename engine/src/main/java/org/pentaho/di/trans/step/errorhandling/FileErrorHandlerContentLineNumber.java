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
