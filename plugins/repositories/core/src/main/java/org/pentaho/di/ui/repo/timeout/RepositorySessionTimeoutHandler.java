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


package org.pentaho.di.ui.repo.timeout;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;

import org.apache.commons.lang.ClassUtils;
import org.pentaho.di.connections.ConnectionManager;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.repository.KettleRepositoryLostException;
import org.pentaho.di.metastore.MetaStoreConst;
import org.pentaho.di.repository.IRepositoryService;
import org.pentaho.di.repository.ReconnectableRepository;
import org.pentaho.di.ui.repository.exception.RepositoryExceptionUtils;
import org.pentaho.di.ui.repo.controller.RepositoryConnectController;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.metastore.api.IMetaStore;

public class RepositorySessionTimeoutHandler implements InvocationHandler {

  private static final Object NOT_HANDLED = new Object();

  private static final String CONNECT_METHOD_NAME = "connect";

  private static final String GET_META_STORE_METHOD_NAME = "getMetaStore";

  private static final String GET_SERVICE_METHOD_NAME = "getService";

  private static final int SERVICE_CLASS_ARGUMENT = 0;

  private ReconnectableRepository repository;

  private final SessionTimeoutHandler sessionTimeoutHandler;

  private IMetaStore metaStoreInstance;

  public RepositorySessionTimeoutHandler( ReconnectableRepository repository,
      RepositoryConnectController repositoryConnectController ) {
    this.repository = repository;
    sessionTimeoutHandler = new SessionTimeoutHandler( repositoryConnectController );
  }

  @SuppressWarnings( "unchecked" )
  @Override
  public Object invoke( Object proxy, Method method, Object[] args ) throws Throwable {
    try {
      Object specialResult = handleSpecialMethods( method, args );
      if ( specialResult != NOT_HANDLED ) {
        return specialResult;
      }
      Object result = method.invoke( repository, args );
      if ( CONNECT_METHOD_NAME.equals( method.getName() ) ) {
        metaStoreInstance = wrapMetastoreWithTimeoutHandler( MetaStoreConst.getDefaultMetastore(), sessionTimeoutHandler );
      }
      return result;
    } catch ( InvocationTargetException ex ) {
      return handleInvocationException( ex, method, args );
    }
  }

  @SuppressWarnings( "unchecked" )
  private Object handleSpecialMethods( Method method, Object[] args ) throws KettleException {
    String methodName = method.getName();
    if ( GET_SERVICE_METHOD_NAME.equals( methodName ) ) {
      return wrapRepositoryServiceWithTimeoutHandler(
          (Class<? extends IRepositoryService>) args[SERVICE_CLASS_ARGUMENT] );
    }
    if ( GET_META_STORE_METHOD_NAME.equals( methodName ) ) {
      return metaStoreInstance;
    }
    return NOT_HANDLED;
  }

  private Object handleInvocationException( InvocationTargetException ex, Method method, Object[] args )
      throws Throwable {
    if ( RepositoryExceptionUtils.isSessionExpired( ex.getCause() ) ) {
      return handleSessionExpiry( method, args );
    }
    if ( connectedToRepository() ) {
      return sessionTimeoutHandler.handle( repository, ex.getCause(), method, args );
    }
    throw ex.getCause();
  }

  private Object handleSessionExpiry( Method method, Object[] args ) throws SessionRecoveryRetryException {
    Spoon spoon = Spoon.getInstance();

    sessionTimeoutHandler.getRepositoryConnectController().setRelogin( true );

    disconnectRepository( spoon );

    // Show login dialog - browser auth will be selected automatically if previously used
    boolean loginSuccessful = sessionTimeoutHandler.showLoginScreen( sessionTimeoutHandler.getRepositoryConnectController() );

    if ( loginSuccessful && spoon.getRepository() != null && spoon.getRepository().isConnected() ) {
      return reconnectAndRetry( method, args );
    }
    // Must run on the UI thread since setRepository may trigger UI updates (tree refresh, tab changes)
    if ( spoon.getDisplay() != null ) {
      spoon.getDisplay().syncExec( () -> spoon.setRepository( null ) );
    } else {
      spoon.setRepository( null );
    }
    throw new KettleRepositoryLostException( "Session recovery failed or was canceled" );
  }

  private void disconnectRepository( Spoon spoon ) {
    if ( spoon.getRepository() != null ) {
      try {
        spoon.getRepository().disconnect();
      } catch ( Exception e ) {
        // Ignore errors during emergency disconnect
      }
    }
  }

  private Object reconnectAndRetry( Method method, Object[] args )
      throws SessionRecoveryRetryException {
    try {
      ConnectionManager.getInstance().reset();
    } catch ( Exception ce ) {
      // Intentionally ignore cache reset errors - they should not prevent reconnection
    }
    try {
      metaStoreInstance = wrapMetastoreWithTimeoutHandler( MetaStoreConst.getDefaultMetastore(), sessionTimeoutHandler );
    } catch ( Exception me ) {
      // Intentionally ignore metastore refresh errors - they should not prevent reconnection
    }
    try {
      return method.invoke( this.repository, args );
    } catch ( InvocationTargetException retryEx ) {
      throw new SessionRecoveryRetryException( "Failed to retry repository operation after reconnect",
          retryEx.getCause() );
    } catch ( IllegalAccessException | IllegalArgumentException retryEx ) {
      throw new SessionRecoveryRetryException( "Unable to invoke repository operation after reconnect", retryEx );
    }
  }

  static class SessionRecoveryRetryException extends KettleException {
    private static final long serialVersionUID = 1L;

    SessionRecoveryRetryException( String message, Throwable cause ) {
      super( message, cause );
    }
  }

  boolean connectedToRepository() {
    return repository.isConnected();
  }

  IRepositoryService wrapRepositoryServiceWithTimeoutHandler( Class<? extends IRepositoryService> clazz )
    throws KettleException {
    IRepositoryService service = repository.getService( clazz );
    RepositoryServiceSessionTimeoutHandler timeoutHandler =
        new RepositoryServiceSessionTimeoutHandler( service, sessionTimeoutHandler );
    return wrapObjectWithTimeoutHandler( service, timeoutHandler );
  }

  static IMetaStore wrapMetastoreWithTimeoutHandler( IMetaStore metaStore,
      SessionTimeoutHandler sessionTimeoutHandler ) {
    MetaStoreSessionTimeoutHandler metaStoreSessionTimeoutHandler =
        new MetaStoreSessionTimeoutHandler( metaStore, sessionTimeoutHandler );
    return wrapObjectWithTimeoutHandler( metaStore, metaStoreSessionTimeoutHandler );
  }

  @SuppressWarnings( "unchecked" )
  static <T> T wrapObjectWithTimeoutHandler( T objectToWrap, InvocationHandler timeoutHandler ) {
    List<Class<?>> objectIntrerfaces = ClassUtils.getAllInterfaces( objectToWrap.getClass() );
    Class<?>[] objectIntrerfacesArray = objectIntrerfaces.toArray( new Class<?>[objectIntrerfaces.size()] );
    return (T) Proxy.newProxyInstance( objectToWrap.getClass().getClassLoader(), objectIntrerfacesArray,
        timeoutHandler );
  }
}
