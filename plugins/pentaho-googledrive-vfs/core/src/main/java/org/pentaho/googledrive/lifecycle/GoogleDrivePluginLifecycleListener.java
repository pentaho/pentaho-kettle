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

package org.pentaho.googledrive.lifecycle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;

import org.pentaho.capabilities.impl.DefaultCapabilityManager;
import org.pentaho.di.core.annotations.KettleLifecyclePlugin;
import org.pentaho.di.core.lifecycle.KettleLifecycleListener;
import org.pentaho.di.core.lifecycle.LifecycleException;
import org.pentaho.di.core.vfs.KettleVFS;

import org.pentaho.googledrive.vfs.GoogleDriveFileObject;
import org.pentaho.googledrive.vfs.GoogleDriveFileProvider;

import java.io.File;
import java.util.Arrays;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

@KettleLifecyclePlugin( id = "GoogleDrivePluginLifecycleListener", name = "GoogleDrivePluginLifecycleListener" )
public class GoogleDrivePluginLifecycleListener implements KettleLifecycleListener {

  private ResourceBundle resourceBundle = PropertyResourceBundle.getBundle( "plugin" );
  private static Log log = LogFactory.getLog( GoogleDrivePluginLifecycleListener.class );

  public void onEnvironmentInit() throws LifecycleException {
    try {
      boolean proceed = true;
      if ( !new File(
          GoogleDriveFileObject.resolveCredentialsPath() + "/" + resourceBundle.getString( "client.secrets" ) )
          .exists() ) {
        proceed = false;
        log.warn(
            "The Google Authorization secrets security token file (" + resourceBundle.getString( "client.secrets" )
                + ") is not present in the credentials folder. This file is necessary to activate the Google Drive VFS plugin." );
      }

      if ( !new File(
          GoogleDriveFileObject.resolveCredentialsPath() + "/" + resourceBundle.getString( "stored.credential" ) )
          .exists() ) {
        DefaultCapabilityManager capabilityManager = DefaultCapabilityManager.getInstance();
        if ( capabilityManager.capabilityExist( "pentaho-server" ) ) {
          proceed = false;
          log.warn( "The Google Authorization Code Flow security token file (" + resourceBundle
              .getString( "stored.credential" ) + ") is not present in the credentials folder.  This file is necessary to activate the Google Drive VFS plugin." );

        }
      }

      /**
       * Registers the GoogleDrive VFS File Provider dynamically since it is bundled with our plugin and will not automatically
       * be registered through the normal class path search the default FileSystemManager performs.
       */
      if ( proceed ) {
        FileSystemManager fsm = KettleVFS.getInstance().getFileSystemManager();
        if ( fsm instanceof DefaultFileSystemManager ) {
          if ( !Arrays.asList( fsm.getSchemes() ).contains( GoogleDriveFileProvider.SCHEME ) ) {
            ( (DefaultFileSystemManager) fsm )
                .addProvider( GoogleDriveFileProvider.SCHEME, new GoogleDriveFileProvider() );
          }
        }
      }
    } catch ( FileSystemException e ) {
      throw new LifecycleException( e.getMessage(), false );
    }
  }

  public void onEnvironmentShutdown() {
  }
}
