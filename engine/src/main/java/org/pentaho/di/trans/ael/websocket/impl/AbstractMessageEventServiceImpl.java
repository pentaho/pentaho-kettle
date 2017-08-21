/*
 * ! ******************************************************************************
 *
 *  Pentaho Data Integration
 *
 *  Copyright (C) 2002-2017 by Pentaho : http://www.pentaho.com
 *
 * ******************************************************************************
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 * *****************************************************************************
 */
package org.pentaho.di.trans.ael.websocket.impl;

import org.pentaho.di.engine.api.remote.Message;
import org.pentaho.di.trans.ael.websocket.event.MessageEventService;
import org.pentaho.di.trans.ael.websocket.event.MessageEvent;
import org.pentaho.di.trans.ael.websocket.exception.HandlerRegistrationException;
import org.pentaho.di.trans.ael.websocket.exception.MessageEventFireEventException;
import org.pentaho.di.trans.ael.websocket.handler.MessageEventHandler;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by fcamara on 8/17/17.
 */
public abstract class AbstractMessageEventServiceImpl implements MessageEventService {

  /**
   * Fire the given DaemonMessageEvent only to interested handlers
   *
   * @throws MessageEventFireEventException
   */
  @Override
  public void fireEvent( final Message event ) throws MessageEventFireEventException {
    if ( event != null ) {
      DaemonMessageEvent
        daemonMessageEventType = new DaemonMessageEvent( event );
      // if at least 1 eventFilter contains a group of handlers for the given event type
      if ( containsHandlerFor( daemonMessageEventType ) ) {
        // retrieve the handler list concerned by the given event
        final Collection<MessageEventHandler<MessageEvent>> handlers = getHandlersFor( daemonMessageEventType );

        if ( handlers.size() > 0 ) {
          MessageEventFireEventException messageEventFireEventException = null;
          for ( final MessageEventHandler<MessageEvent> handler : handlers ) {
            // for each handler, I check if it's interested or not by the given event
            try {
              if ( handler.isInterested( daemonMessageEventType ) ) {
                handler.execute( event );
              }
            } catch ( final Exception e ) {
              if ( messageEventFireEventException == null ) {
                messageEventFireEventException =
                  new MessageEventFireEventException( "Unable to execute some handler." );
              }
              messageEventFireEventException.addHandlerException( e );
              // TODO: log exception
            }
          }
          if ( messageEventFireEventException != null ) {
            throw messageEventFireEventException;
          }
        }
      }
    } else {
      //TODO: log exception
      throw new MessageEventFireEventException( "Unable to fire a null event" );
    }
  }

  protected abstract Collection<MessageEventHandler<MessageEvent>> getHandlersFor( final MessageEvent type );

  protected abstract boolean containsHandlerFor( final org.pentaho.di.trans.ael.websocket.event.MessageEvent type );

  /**
   * No handler duplication in a list for a given event type
   */
  @Override
  public final void addHandler( final MessageEvent eventType, final MessageEventHandler<MessageEvent> handler )
    throws HandlerRegistrationException {
    if ( handler != null && eventType != null ) {
      addHandlerFor( eventType, handler );
    } else {
      //TODO: Log the exception
      throw new HandlerRegistrationException(
        "One of the parameters is null : " + " eventType: " + eventType + " handler:" + handler );
    }
  }

  protected abstract void addHandlerFor( MessageEvent eventType, MessageEventHandler<MessageEvent> handler )
    throws HandlerRegistrationException;

  @Override
  public final Set<MessageEventHandler<MessageEvent>> getHandlers(
    final org.pentaho.di.trans.ael.websocket.event.MessageEvent eventType ) {
    final Collection<MessageEventHandler<MessageEvent>> handlers = getHandlersFor( eventType );
    if ( handlers == null ) {
      return Collections.emptySet();
    }
    final HashSet<MessageEventHandler<MessageEvent>> hashSet = new HashSet<>( handlers.size() );
    hashSet.addAll( handlers );
    return hashSet;
  }

  @Override
  public final boolean hasHandlers( final org.pentaho.di.trans.ael.websocket.event.MessageEvent eventType ) {
    return containsHandlerFor( eventType );
  }
}
