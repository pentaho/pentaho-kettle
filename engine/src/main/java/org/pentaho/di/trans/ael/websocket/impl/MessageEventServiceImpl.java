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

import org.pentaho.di.trans.ael.websocket.event.MessageEvent;
import org.pentaho.di.trans.ael.websocket.exception.HandlerRegistrationException;
import org.pentaho.di.trans.ael.websocket.handler.MessageEventHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by fcamara on 8/17/17.
 */
public class MessageEventServiceImpl extends AbstractMessageEventServiceImpl {
  /**
   * Contains a list of all events type and their registered handlers
   */
  protected Map<MessageEvent, List<MessageEventHandler<MessageEvent>>> registeredHandlers;

  public MessageEventServiceImpl() {
    registeredHandlers = new HashMap<MessageEvent, List<MessageEventHandler<MessageEvent>>>();
  }

  @Override
  protected boolean containsHandlerFor( final MessageEvent eventType ) {
    if ( eventType == null || registeredHandlers.isEmpty() ) {
      return false;
    }
    for ( MessageEvent e : registeredHandlers.keySet() ) {
      if ( eventType.getType() == e.getType() && ( ( eventType.getObjectId() == null && e.getObjectId() == null )
        || ( eventType.getObjectId() != null && eventType.getObjectId().equals( e.getObjectId() ) ) ) ) {
        return true;
      }
    }
    return false;
  }

  @Override
  protected List<MessageEventHandler<MessageEvent>> getHandlersFor( final MessageEvent eventType ) {
    List<MessageEventHandler<MessageEvent>> toRet = null;
    if ( eventType == null || registeredHandlers.isEmpty() ) {
      return toRet;
    }
    for ( MessageEvent e : registeredHandlers.keySet() ) {
      if ( eventType.getType() == e.getType() && ( ( eventType.getObjectId() == null && e.getObjectId() == null )
        || ( eventType.getObjectId() != null && eventType.getObjectId().equals( e.getObjectId() ) ) ) ) {
        toRet = registeredHandlers.get( e );
      }
    }
    return toRet;
  }

  @Override
  protected void addHandlerFor( final MessageEvent eventType, final MessageEventHandler<MessageEvent> handler )
    throws HandlerRegistrationException {
    // check if the given event type is already registered in the DaemonMessageEvent Service
    if ( containsHandlerFor( eventType ) ) {
      // if the handler already exists for the same eventType, an Exception is thrown
      final List<MessageEventHandler<MessageEvent>> handlers = getHandlersFor( eventType );

      // Check if another handler of the same class is already registered
      for ( MessageEventHandler<MessageEvent> tmpHandler : handlers ) {
        if ( tmpHandler.getIdentifier().equals( handler.getIdentifier() ) ) {
          throw new HandlerRegistrationException(
            "The handler with identifier " + tmpHandler.getIdentifier() + " is already registered for the event "
              + eventType );
        }
      }

      handlers.add( handler );
    } else {
      // if the given type doesn't already exist in the eventFilters list, we create it
      final List<MessageEventHandler<MessageEvent>> newHandlerList =
        new ArrayList<MessageEventHandler<MessageEvent>>( 10 );
      newHandlerList.add( handler );
      registeredHandlers.put( eventType, newHandlerList );
    }
  }
}
