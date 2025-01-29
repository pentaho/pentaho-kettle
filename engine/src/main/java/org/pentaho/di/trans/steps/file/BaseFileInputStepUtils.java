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


package org.pentaho.di.trans.steps.file;

import java.util.List;

import org.apache.commons.vfs2.FileObject;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.fileinput.FileInputList;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.trans.step.errorhandling.FileErrorHandler;

/**
 * Utils for file-based input steps.
 *
 * @author Alexander Buloichik
 */
public class BaseFileInputStepUtils {

  public static void handleMissingFiles( FileInputList files, LogChannelInterface log, boolean isErrorIgnored,
      FileErrorHandler errorHandler ) throws KettleException {
    List<FileObject> nonExistantFiles = files.getNonExistantFiles();

    if ( !nonExistantFiles.isEmpty() ) {
      String message = FileInputList.getRequiredFilesDescription( nonExistantFiles );
      if ( log.isBasic() ) {
        log.logBasic( "Required files", "WARNING: Missing " + message );
      }
      if ( isErrorIgnored ) {
        for ( FileObject fileObject : nonExistantFiles ) {
          errorHandler.handleNonExistantFile( fileObject );
        }
      } else {
        throw new KettleException( "Following required files are missing: " + message );
      }
    }

    List<FileObject> nonAccessibleFiles = files.getNonAccessibleFiles();
    if ( !nonAccessibleFiles.isEmpty() ) {
      String message = FileInputList.getRequiredFilesDescription( nonAccessibleFiles );
      if ( log.isBasic() ) {
        log.logBasic( "Required files", "WARNING: Not accessible " + message );
      }
      if ( isErrorIgnored ) {
        for ( FileObject fileObject : nonAccessibleFiles ) {
          errorHandler.handleNonAccessibleFile( fileObject );
        }
      } else {
        throw new KettleException( "Following required files are not accessible: " + message );
      }
    }
  }

  /**
   * Adds <code>String</code> value meta with given name if not present and returns index
   *
   * @param rowMeta
   * @param fieldName
   * @return Index in row meta of value meta with <code>fieldName</code>
   */
  public static int addValueMeta( String stepName, RowMetaInterface rowMeta, String fieldName ) {
    ValueMetaInterface valueMeta = new ValueMetaString( fieldName );
    valueMeta.setOrigin( stepName );
    // add if doesn't exist
    int index = -1;
    if ( !rowMeta.exists( valueMeta ) ) {
      index = rowMeta.size();
      rowMeta.addValueMeta( valueMeta );
    } else {
      index = rowMeta.indexOfValue( fieldName );
    }
    return index;
  }
}
