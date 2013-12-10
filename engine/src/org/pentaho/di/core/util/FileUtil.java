package org.pentaho.di.core.util;

import org.apache.commons.vfs.FileObject;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.i18n.BaseMessages;

public class FileUtil {
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
            log.logDebug( BaseMessages.getString( PKG, "JobTrans.Log.ParentLogFolderNotExist", parentfolder
              .getName().toString() ) );
          }
          parentfolder.createFolder();
          if ( log.isDebug() ) {
            log.logDebug( BaseMessages.getString( PKG, "JobTrans.Log.ParentLogFolderCreated", parentfolder
              .getName().toString() ) );
          }
        } else {
          log.logError( BaseMessages.getString( PKG, "JobTrans.Log.ParentLogFolderNotExist", parentfolder
            .getName().toString() ) );
          resultat = false;
        }
      } else {
        if ( log.isDebug() ) {
          log.logDebug( BaseMessages.getString( PKG, "JobTrans.Log.ParentLogFolderExists", parentfolder
            .getName().toString() ) );
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
