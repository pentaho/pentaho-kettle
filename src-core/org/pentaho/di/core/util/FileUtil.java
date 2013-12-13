/*******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
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

import org.apache.commons.vfs.FileObject;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.i18n.BaseMessages;

public class FileUtil {
  
  /**
   * Utility method to create parent folder(s) for specified file location
   * @param PKG -- class to be used in logging
   * @param filename -- filename of the file, parent folder is required for
   * @param createParentFolder -- flag indicating if we need to create parent folder
   * @param log -- logging interface to be used
   * @param vs -- variable space containing variables that might appear in filename 
   * @return true -- if folder was created, false -- otherwise
   */
  public static boolean createParentFolder( Class<?> PKG, String filename, boolean createParentFolder,
      LogChannelInterface log, VariableSpace vs ) {
    // Check for parent folder
    FileObject parentfolder = null;
    boolean resultat = true;
    try {
      // Get parent folder
      parentfolder = KettleVFS.getFileObject( filename, vs ).getParent();
      if ( !parentfolder.exists() ) {
        if ( createParentFolder ) {
          if ( log.isDebug() ) {
            log.logDebug( BaseMessages.getString( PKG, "JobTrans.Log.ParentLogFolderNotExist", parentfolder.getName()
              .toString() ) );
          }
          parentfolder.createFolder();
          if ( log.isDebug() ) {
            log.logDebug( BaseMessages.getString( PKG, "JobTrans.Log.ParentLogFolderCreated", parentfolder.getName()
              .toString() ) );
          }
        } else {
          log.logError( BaseMessages.getString( PKG, "JobTrans.Log.ParentLogFolderNotExist", parentfolder.getName()
              .toString() ) );
          resultat = false;
        }
      } else {
        if ( log.isDebug() ) {
          log.logDebug( BaseMessages.getString( PKG, "JobTrans.Log.ParentLogFolderExists", parentfolder.getName()
            .toString() ) );
        }
      }
    } catch ( Exception e ) {
      resultat = false;
      log.logError( BaseMessages.getString( PKG, "JobTrans.Error.ChekingParentLogFolderTitle" ), BaseMessages
          .getString( PKG, "JobTrans.Error.ChekingParentLogFolder", parentfolder.getName().toString() ), e );
    } finally {
      if ( parentfolder != null ) {
        try {
          parentfolder.close();
          parentfolder = null;
        } catch ( Exception ex ) {
          // Ignore
        }
      }
    }

    return resultat;
  }

}
