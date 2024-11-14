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


package org.pentaho.di.trans.step.errorhandling;

import java.util.Date;

import org.apache.commons.vfs2.FileObject;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.step.BaseStep;

public class FileErrorHandlerMissingFiles extends AbstractFileErrorHandler {

  private static Class<?> PKG = FileErrorHandlerMissingFiles.class; // for i18n purposes, needed by Translator2!!

  public static final String THIS_FILE_DOES_NOT_EXIST = BaseMessages.getString(
    PKG, "FileErrorHandlerMissingFiles.FILE_DOES_NOT_EXIST" );

  public static final String THIS_FILE_WAS_NOT_ACCESSIBLE = BaseMessages.getString(
    PKG, "FileErrorHandlerMissingFiles.FILE_WAS_NOT_ACCESSIBLE" );

  public FileErrorHandlerMissingFiles( Date date, String destinationDirectory, String fileExtension,
    String encoding, BaseStep baseStep ) {
    super( date, destinationDirectory, fileExtension, encoding, baseStep );
  }

  public void handleLineError( long lineNr, String filePart ) {

  }

  public void handleNonExistantFile( FileObject file ) throws KettleException {
    handleFile( file );
    try {
      getWriter( NO_PARTS ).write( THIS_FILE_DOES_NOT_EXIST );
      getWriter( NO_PARTS ).write( Const.CR );
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString(
        PKG, "FileErrorHandlerMissingFiles.Exception.CouldNotCreateNonExistantFile" )
        + file.getName().getURI(), e );
    }
  }

  public void handleNonAccessibleFile( FileObject file ) throws KettleException {
    handleFile( file );
    try {
      getWriter( NO_PARTS ).write( THIS_FILE_WAS_NOT_ACCESSIBLE );
      getWriter( NO_PARTS ).write( Const.CR );
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString(
        PKG, "FileErrorHandlerMissingFiles.Exception.CouldNotCreateNonAccessibleFile" )
        + file.getName().getURI(), e );
    }
  }

}
