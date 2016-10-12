/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2016 by Pentaho : http://www.pentaho.com
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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;

import org.apache.commons.lang.ClassUtils;
import org.pentaho.di.repository.ReconnectableRepository;
import org.pentaho.metastore.api.IMetaStore;

public class RepositorySessionTimeoutHandler implements InvocationHandler {

  private static final String CONNECT_METHOD_NAME = "connect";

  private static final String GET_META_STORE_METHOD_NAME = "getMetaStore";

  private final ReconnectableRepository repository;

  private final SessionTimeoutHandler sessionTimeoutHandler;

  private IMetaStore metaStoreInstance;

  public RepositorySessionTimeoutHandler( ReconnectableRepository repository,
      RepositoryConnectController repositoryConnectController ) {
    this.repository = repository;
    sessionTimeoutHandler = new SessionTimeoutHandler( repositoryConnectController );
  }

  @Override
  public Object invoke( Object proxy, Method method, Object[] args ) throws Throwable {
    try {
      if ( GET_META_STORE_METHOD_NAME.equals( method.getName() ) ) {
        return metaStoreInstance;
      }
      Object result = method.invoke( repository, args );
      if ( CONNECT_METHOD_NAME.equals( method.getName() ) ) {
        IMetaStore metaStore = repository.getMetaStore();
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

  @SuppressWarnings( "unchecked" )
  static IMetaStore wrapMetastoreWithTimeoutHandler( IMetaStore metaStore,
      SessionTimeoutHandler sessionTimeoutHandler ) {
    List<Class<?>> metaStoreIntrerfaces = ClassUtils.getAllInterfaces( metaStore.getClass() );
    Class<?>[] metaStoreIntrerfacesArray = metaStoreIntrerfaces.toArray( new Class<?>[metaStoreIntrerfaces.size()] );
    return (IMetaStore) Proxy.newProxyInstance( metaStore.getClass().getClassLoader(), metaStoreIntrerfacesArray,
        new MetaStoreSessionTimeoutHandler( metaStore, sessionTimeoutHandler ) );
  }

}
