/*******************************************************************************
 * Copyright (c) 2011, 2015 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.rap.rwt.internal.service;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.rap.rwt.service.ServiceHandler;


class ServiceHandlerRegistry {

  private final Map<String, ServiceHandler> handlers;

  ServiceHandlerRegistry() {
    handlers = new HashMap<>();
  }

  ServiceHandler get( String id ) {
    synchronized( handlers ) {
      return handlers.get( id );
    }
  }

  boolean put( String id, ServiceHandler serviceHandler ) {
    synchronized( handlers ) {
      if( !handlers.containsKey( id ) ) {
        handlers.put( id, serviceHandler );
        return true;
      }
    }
    return false;
  }

  void remove( String id ) {
    synchronized( handlers ) {
      handlers.remove( id );
    }
  }

  void clear() {
    synchronized( handlers ) {
      handlers.clear();
    }
  }

}
