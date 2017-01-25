/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.ui.repo;

import org.pentaho.di.core.LastUsedFile;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.extension.ExtensionPoint;
import org.pentaho.di.core.extension.ExtensionPointInterface;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.repository.RepositoryMeta;
import org.pentaho.di.ui.spoon.Spoon;

/**
 * Created by bmorrise on 5/25/16.
 */
@ExtensionPoint(
  id = "RepositoryOpenRecentExtensionPoint",
  extensionPointId = "OpenRecent",
  description = "Do or display login for default repository"
  )
public class RepositoryOpenRecentFileExtensionPoint implements ExtensionPointInterface {

  private RepositoryConnectController repositoryConnectController;

  public RepositoryOpenRecentFileExtensionPoint( RepositoryConnectController repositoryConnectController ) {
    this.repositoryConnectController = repositoryConnectController;
  }

  @Override public void callExtensionPoint( LogChannelInterface log, Object object ) throws KettleException {
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
            new RepositoryDialog( getSpoon().getShell(), repositoryConnectController ).openLogin( repositoryMeta );
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
