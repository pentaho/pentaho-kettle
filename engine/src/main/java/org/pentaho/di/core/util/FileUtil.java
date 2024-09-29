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
