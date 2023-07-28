/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2023 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.ui.repo.timeout;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;

import org.apache.commons.lang.ClassUtils;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.metastore.MetaStoreConst;
import org.pentaho.di.repository.IRepositoryService;
import org.pentaho.di.repository.ReconnectableRepository;
import org.pentaho.di.ui.repo.controller.RepositoryConnectController;
import org.pentaho.metastore.api.IMetaStore;

public class RepositorySessionTimeoutHandler implements InvocationHandler {

  private static final String CONNECT_METHOD_NAME = "connect";

  private static final String GET_META_STORE_METHOD_NAME = "getMetaStore";

  private static final String GET_SERVICE_METHOD_NAME = "getService";

  private static final int SERVICE_CLASS_ARGUMENT = 0;

  private final ReconnectableRepository repository;

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
      String methodName = method.getName();
      if ( GET_SERVICE_METHOD_NAME.equals( methodName ) ) {
        return wrapRepositoryServiceWithTimeoutHandler(
            (Class<? extends IRepositoryService>) args[SERVICE_CLASS_ARGUMENT] );
      }
      if ( GET_META_STORE_METHOD_NAME.equals( methodName ) ) {
        return metaStoreInstance;
      }
      Object result = method.invoke( repository, args );
      if ( CONNECT_METHOD_NAME.equals( methodName ) ) {
        IMetaStore metaStore = MetaStoreConst.getDefaultMetastore();
        metaStoreInstance = wrapMetastoreWithTimeoutHandler( metaStore, sessionTimeoutHandler );
      }
      return result;
    } catch ( InvocationTargetException ex ) {
      if ( connectedToRepository() ) {
        return sessionTimeoutHandler.handle( repository, ex.getCause(), method, args );
      }
      throw ex.getCause();
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
