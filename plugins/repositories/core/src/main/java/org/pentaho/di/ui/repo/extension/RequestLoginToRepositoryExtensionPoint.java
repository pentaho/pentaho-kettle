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

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.extension.ExtensionPoint;
import org.pentaho.di.core.extension.ExtensionPointInterface;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryMeta;
import org.pentaho.di.ui.repo.controller.RepositoryConnectController;
import org.pentaho.di.ui.repo.dialog.RepositoryConnectionDialog;
import org.pentaho.di.ui.repository.ILoginCallback;
import org.pentaho.di.ui.spoon.Spoon;

@ExtensionPoint(
  id = "RequestLoginToRepositoryExtensionPoint",
  extensionPointId = "RequestLoginToRepository",
  description = "Handles login requests"
)
public class RequestLoginToRepositoryExtensionPoint implements ExtensionPointInterface {

  private static Class<?> PKG = RequestLoginToRepositoryExtensionPoint.class;

  private static final String KETTE_FILE_REPOSITORY_ID = "KettleFileRepository";

  private final ILoginCallback defaultLoginCallback = new DefaultLoginCallback();

  private final RepositoryConnectController repositoryConnectController;

  public RequestLoginToRepositoryExtensionPoint( RepositoryConnectController repositoryConnectController ) {
    this.repositoryConnectController = repositoryConnectController;
  }

  @Override
  public void callExtensionPoint( LogChannelInterface log, Object object ) throws KettleException {
    ILoginCallback loginCallback = defaultLoginCallback;
    if ( ( object instanceof ILoginCallback ) ) {
      loginCallback = (ILoginCallback) object;
    }
    RepositoryMeta repositoryMeta = findRepositoryToConnect();
    if ( repositoryMeta != null ) {
      if ( isKettleFileRepository( repositoryMeta ) ) {
        repositoryConnectController.connectToRepository( repositoryMeta );
        loginCallback.onSuccess( repositoryConnectController.getConnectedRepositoryInstance() );
      } else {
        loginToRepository( repositoryMeta, loginCallback );
      }
    } else {
      String errorMessage = BaseMessages.getString( PKG, "Repository.NoDefinedRepositoryToConnect" );
      KettleException exception = new KettleException( errorMessage );
      loginCallback.onError( exception );
    }
  }

  void loginToRepository( RepositoryMeta repositoryMeta, ILoginCallback loginCallback ) {
    RepositoryConnectionDialog dialog = getRepositoryDialog();
    boolean result = dialog.createDialog( repositoryMeta.getName());
    if ( result ) {
      loginCallback.onSuccess( repositoryConnectController.getConnectedRepositoryInstance() );
    } else {
      loginCallback.onCancel();
    }
  }

  RepositoryConnectionDialog getRepositoryDialog() {
    return new RepositoryConnectionDialog( getSpoon().getShell());
  }

  RepositoryMeta findRepositoryToConnect() {
    RepositoryMeta repositoryMeta = repositoryConnectController.getCurrentRepository();
    if ( repositoryMeta == null ) {
      repositoryMeta = repositoryConnectController.getDefaultRepositoryMeta();
    }
    return repositoryMeta;
  }

  static boolean isKettleFileRepository( RepositoryMeta repositoryMeta ) {
    return KETTE_FILE_REPOSITORY_ID.equals( repositoryMeta.getId() );
  }

  private Spoon getSpoon() {
    return Spoon.getInstance();
  }

  private final class DefaultLoginCallback implements ILoginCallback {

    @Override
    public void onSuccess( Repository repository ) {
    }

    @Override
    public void onCancel() {
    }

    @Override
    public void onError( Throwable t ) {
    }
  }
}