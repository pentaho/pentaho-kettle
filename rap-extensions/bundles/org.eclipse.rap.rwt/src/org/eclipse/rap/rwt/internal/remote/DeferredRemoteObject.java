/*******************************************************************************
 * Copyright (c) 2013, 2015 EclipseSource and others.
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
import java.util.ArrayList;
import java.util.List;

import org.eclipse.rap.json.JsonObject;
import org.eclipse.rap.json.JsonValue;
import org.eclipse.rap.rwt.internal.protocol.ProtocolMessageWriter;


/**
 * A remote object implementation that does not write operations directly, but keeps them in a
 * render queue. The {@link RemoteObjectLifeCycleAdapter} will write the operations by calling the
 * <code>render</code> method.
 */
public class DeferredRemoteObject extends RemoteObjectImpl {

  private final List<RenderRunnable> renderQueue;
  private boolean created;

  public DeferredRemoteObject( final String id, final String createType ) {
    super( id );
    renderQueue = new ArrayList<>();
    if( createType != null ) {
      renderQueue.add( new RenderRunnable() {
        @Override
        public void render( ProtocolMessageWriter writer ) {
          writer.appendCreate( id, createType );
        }
      } );
    }
  }

  @Override
  public void set( final String name, final int value ) {
    super.set( name, value );
    renderQueue.add( new RenderRunnable() {
      @Override
      public void render( ProtocolMessageWriter writer ) {
        writer.appendSet( getId(), name, value );
      }
    } );
  }

  @Override
  public void set( final String name, final double value ) {
    super.set( name, value );
    renderQueue.add( new RenderRunnable() {
      @Override
      public void render( ProtocolMessageWriter writer ) {
        writer.appendSet( getId(), name, value );
      }
    } );
  }

  @Override
  public void set( final String name, final boolean value ) {
    super.set( name, value );
    renderQueue.add( new RenderRunnable() {
      @Override
      public void render( ProtocolMessageWriter writer ) {
        writer.appendSet( getId(), name, value );
      }
    } );
  }

  @Override
  public void set( final String name, final String value ) {
    super.set( name, value );
    renderQueue.add( new RenderRunnable() {
      @Override
      public void render( ProtocolMessageWriter writer ) {
        writer.appendSet( getId(), name, value );
      }
    } );
  }

  @Override
  public void set( final String name, final JsonValue value ) {
    super.set( name, value );
    renderQueue.add( new RenderRunnable() {
      @Override
      public void render( ProtocolMessageWriter writer ) {
        writer.appendSet( getId(), name, value );
      }
    } );
  }

  @Override
  public void listen( final String eventType, final boolean listen ) {
    super.listen( eventType, listen );
    renderQueue.add( new RenderRunnable() {
      @Override
      public void render( ProtocolMessageWriter writer ) {
        writer.appendListen( getId(), eventType, listen );
      }
    } );
  }

  @Override
  public void call( final String method, final JsonObject parameters ) {
    super.call( method, parameters );
    renderQueue.add( new RenderRunnable() {
      @Override
      public void render( ProtocolMessageWriter writer ) {
        writer.appendCall( getId(), method, parameters );
      }
    } );
  }

  @Override
  public void destroy() {
    super.destroy();
    renderQueue.add( new RenderRunnable() {
      @Override
      public void render( ProtocolMessageWriter writer ) {
        writer.appendDestroy( getId() );
      }
    } );
  }

  public void render( ProtocolMessageWriter writer ) {
    if( isDestroyed() && !created ) {
      // skip rendering for objects that are disposed just after creation (see bug 395272)
    } else {
      for( RenderRunnable runnable : renderQueue ) {
        runnable.render( writer );
      }
      created = true;
    }
    renderQueue.clear();
  }

  private static interface RenderRunnable extends Serializable {

    void render( ProtocolMessageWriter writer );

  }

}
