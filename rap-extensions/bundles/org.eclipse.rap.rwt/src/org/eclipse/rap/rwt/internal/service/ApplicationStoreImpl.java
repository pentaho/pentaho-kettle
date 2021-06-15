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

import org.eclipse.rap.rwt.internal.util.ParamCheck;


public class ApplicationStoreImpl {
  private final Map<String,Object> attributes;

  public ApplicationStoreImpl() {
    attributes = new HashMap<>();
  }

  public Object getAttribute( String name ) {
    ParamCheck.notNull( name, "name" );
    Object result;
    synchronized( attributes ) {
      result = attributes.get( name );
    }
    return result;
  }

  public void setAttribute( String name, Object value ) {
    ParamCheck.notNull( name, "name" );
    synchronized( attributes ) {
      attributes.put( name, value );
    }
  }

  public void removeAttribute( String name ) {
    ParamCheck.notNull( name, "name" );
    synchronized( attributes ) {
      attributes.remove( name );
    }
  }

  public void reset() {
    synchronized( attributes ) {
      attributes.clear();
    }
  }

}
