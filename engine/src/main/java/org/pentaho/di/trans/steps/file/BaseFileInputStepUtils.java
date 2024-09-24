/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2017 by Hitachi Vantara : http://www.pentaho.com
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
