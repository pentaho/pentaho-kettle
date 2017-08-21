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

package org.pentaho.di.trans.ael.websocket.event;

import org.pentaho.di.engine.api.remote.Message;
import org.pentaho.di.trans.ael.websocket.exception.HandlerRegistrationException;
import org.pentaho.di.trans.ael.websocket.exception.MessageEventFireEventException;
import org.pentaho.di.trans.ael.websocket.handler.MessageEventHandler;

import java.util.Set;

/**
 * Created by fcamara on 8/17/17.
 */
public interface MessageEventService {
  /**
   * Fire the specified DaemonMessageEvent to the registered handlers.
   *
   * @param event A specific DaemonMessageEvent
   */
  void fireEvent( final Message event ) throws MessageEventFireEventException;

  /**
   * Allows to check if an handler is listening to this event type
   *
   * @param event the type of the event
   * @return true if an handler is interested by the event having type eventType
   */
  boolean hasHandlers( final MessageEvent event );

  /**
   * Add the given handler to the DaemonMessageEvent Manager's handlers list.
   *
   * @param event       The type of the event the handler is interested in.
   * @param userHandler The handler to register in the DaemonMessageEvent Manager
   * @throws HandlerRegistrationException
   */
  void addHandler( final MessageEvent event, final MessageEventHandler<MessageEvent> userHandler )
    throws HandlerRegistrationException;

  /**
   * Retrieve the list of all registered Handlers or the given MessageEventType
   */
  Set<MessageEventHandler<MessageEvent>> getHandlers( MessageEvent event );
}
