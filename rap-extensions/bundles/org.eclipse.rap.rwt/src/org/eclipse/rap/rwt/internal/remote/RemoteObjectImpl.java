/*******************************************************************************
 * Copyright (c) 2012, 2015 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.rap.rwt.internal.remote;

import java.io.Serializable;

import org.eclipse.rap.json.JsonObject;
import org.eclipse.rap.json.JsonValue;
import org.eclipse.rap.rwt.internal.service.ContextProvider;
import org.eclipse.rap.rwt.internal.util.ParamCheck;
import org.eclipse.rap.rwt.remote.OperationHandler;
import org.eclipse.rap.rwt.remote.RemoteObject;


public abstract class RemoteObjectImpl implements RemoteObject, Serializable {

  private final String id;
  private boolean destroyed;
  private OperationHandler handler;

  public RemoteObjectImpl( String id ) {
    this.id = id;
    destroyed = false;
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public void set( final String name, final int value ) {
    ParamCheck.notNullOrEmpty( name, "name" );
    checkState();
  }

  @Override
  public void set( final String name, final double value ) {
    ParamCheck.notNullOrEmpty( name, "name" );
    checkState();
  }

  @Override
  public void set( final String name, final boolean value ) {
    ParamCheck.notNullOrEmpty( name, "name" );
    checkState();
  }

  @Override
  public void set( final String name, final String value ) {
    ParamCheck.notNullOrEmpty( name, "name" );
    checkState();
  }

  @Override
  public void set( final String name, final JsonValue value ) {
    ParamCheck.notNullOrEmpty( name, "name" );
    ParamCheck.notNull( value, "value" );
    checkState();
  }

  @Override
  public void listen( final String eventType, final boolean listen ) {
    ParamCheck.notNullOrEmpty( eventType, "eventType" );
    checkState();
  }

  @Override
  public void call( final String method, final JsonObject parameters ) {
    ParamCheck.notNullOrEmpty( method, "method" );
    checkState();
  }

  @Override
  public void destroy() {
    checkState();
    destroyed = true;
  }

  public boolean isDestroyed() {
    return destroyed;
  }

  /*
   * In some cases, widgets don't render destroy operations, those have to be marked as destroyed
   * to be removed from the registry.
   */
  public void markDestroyed() {
    destroyed = true;
  }

  @Override
  public void setHandler( OperationHandler handler ) {
    this.handler = handler;
  }

  public OperationHandler getHandler() {
    return handler;
  }

  void checkState() {
    // TODO [rst] Prevent calls with fake context as they break thread confinement
    if( !ContextProvider.hasContext() ) {
      throw new IllegalStateException( "Remote object called from wrong thread" );
    }
    if( destroyed ) {
      throw new IllegalStateException( "Remote object is destroyed" );
    }
  }

}
