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

package org.pentaho.di.ui.repo.extension;

import org.pentaho.di.core.LastUsedFile;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.extension.ExtensionPoint;
import org.pentaho.di.core.extension.ExtensionPointInterface;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.repository.RepositoryMeta;
import org.pentaho.di.ui.repo.controller.RepositoryConnectController;
import org.pentaho.di.ui.repo.dialog.RepositoryConnectionDialog;
import org.pentaho.di.ui.spoon.Spoon;

/**
 * Created by bmorrise on 5/25/16.
 * Modified by amit kumar on 8/sep/22.
 */
@ExtensionPoint(
  id = "RepositoryOpenRecentExtensionPoint",
  extensionPointId = "OpenRecent",
  description = "Do or display login for default repository"
)
public class RepositoryOpenRecentFileExtensionPoint implements ExtensionPointInterface {

  private RepositoryConnectController repositoryConnectController;

  public RepositoryOpenRecentFileExtensionPoint() {
    this.repositoryConnectController = RepositoryConnectController.getInstance();
  }

  @SuppressWarnings( { "squid:S3776", "squid:S1066" } )
  @Override
  public void callExtensionPoint( LogChannelInterface log, Object object ) throws KettleException {
    if ( !( object instanceof LastUsedFile ) ) {
      return;
    }

    LastUsedFile recentFile = (LastUsedFile) object;


    if ( recentFile.isSourceRepository() && !repositoryConnectController
      .isConnected( recentFile.getRepositoryName() ) ) {
      if ( getSpoon().promptForSave() ) {
        RepositoryMeta
          repositoryMeta =
          repositoryConnectController.getRepositoryMetaByName( recentFile.getRepositoryName() );
        if ( repositoryMeta != null ) {
          if ( repositoryMeta.getId().equals( "KettleFileRepository" ) ) {
            getSpoon().closeRepository();
            repositoryConnectController.connectToRepository( repositoryMeta );
          } else {
            new RepositoryConnectionDialog( getSpoon().getShell() ).createDialog( repositoryMeta.getName() );
          }
          if ( repositoryConnectController.isConnected( repositoryMeta.getName() ) ) {
            getSpoon().loadLastUsedFile( recentFile, repositoryMeta.getName() );
            getSpoon().addMenuLast();
          }
        }
      }
    }
  }

  private Spoon getSpoon() {
    return Spoon.getInstance();
  }
}
