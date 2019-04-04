/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
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

import org.pentaho.metastore.api.IMetaStore;

public class MetaStoreSessionTimeoutHandler implements InvocationHandler {

  private final IMetaStore metaStore;

  private final SessionTimeoutHandler sessionTimeoutHandler;

  public MetaStoreSessionTimeoutHandler( IMetaStore metaStore, SessionTimeoutHandler sessionTimeoutHandler ) {
    this.metaStore = metaStore;
    this.sessionTimeoutHandler = sessionTimeoutHandler;
  }

  @Override
  public Object invoke( Object proxy, Method method, Object[] args ) throws Throwable {
    try {
      return method.invoke( metaStore, args );
    } catch ( InvocationTargetException ex ) {
      return sessionTimeoutHandler.handle( metaStore, ex.getCause(), method, args );
    }
  }

}
