/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2020 by Hitachi Vantara : http://www.pentaho.com
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

import org.apache.commons.vfs2.FileObject;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.i18n.BaseMessages;

import java.io.File;

public class FileUtil {

  private FileUtil() {
  }

  public static boolean createParentFolder( Class<?> pkg, String filename, boolean createParentFolder,
    LogChannelInterface log, VariableSpace vs ) {
    // Check for parent folder
    boolean isParentFolderCreated = true;
    String parentFolderName = null;
    try ( FileObject parentFolder =  KettleVFS.getFileObject( filename, vs ).getParent() ) {
      parentFolderName = parentFolder.getName().toString();
      // Get parent folder
      if ( !parentFolder.exists() ) {
        if ( createParentFolder ) {
          if ( log.isDebug() ) {
            log.logDebug( BaseMessages.getString( pkg, "JobTrans.Log.ParentLogFolderNotExist", parentFolder
              .getName().toString() ) );
          }
          parentFolder.createFolder();
          if ( log.isDebug() ) {
            log.logDebug( BaseMessages.getString( pkg, "JobTrans.Log.ParentLogFolderCreated", parentFolder
              .getName().toString() ) );
          }
        } else {
          log.logError( BaseMessages.getString( pkg, "JobTrans.Log.ParentLogFolderNotExist", parentFolder
            .getName().toString() ) );
          isParentFolderCreated = false;
        }
      } else {
        if ( log.isDebug() ) {
          log.logDebug( BaseMessages.getString( pkg, "JobTrans.Log.ParentLogFolderExists", parentFolder
            .getName().toString() ) );
        }
      }
    } catch ( Exception e ) {
      isParentFolderCreated = false;
      log.logError( BaseMessages.getString( pkg, "JobTrans.Error.ChekingParentLogFolderTitle" ), BaseMessages
        .getString( pkg, "JobTrans.Error.ChekingParentLogFolder", parentFolderName ), e );
    }

    return isParentFolderCreated;
  }

  /**
   * Tests whether this abstract pathname is absolute.
   *
   * The pathname is absolute if its prefix is "/", "\" and on Microsoft Windows systems,
   * a pathname is absolute if its prefix is a drive specifier followed by "\\", or if its prefix is "\\\\".
   */
  public static boolean isFullyQualified( String pathname ) {
    return new File( pathname ).isAbsolute() || pathname.startsWith( "/" ) || pathname.startsWith( "\\" );
  }

}
