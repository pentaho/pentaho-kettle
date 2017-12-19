/*
 * ! ******************************************************************************
 *
 *  Pentaho Data Integration
 *
 *  Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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
package org.pentaho.di.trans.ael.websocket;

import org.pentaho.di.engine.api.events.PDIEvent;
import org.pentaho.di.engine.api.model.ModelType;
import org.pentaho.di.engine.api.remote.Message;
import org.pentaho.di.engine.api.remote.RemoteSource;
import org.pentaho.di.engine.api.remote.StopMessage;
import org.pentaho.di.trans.ael.websocket.exception.HandlerRegistrationException;
import org.pentaho.di.trans.ael.websocket.exception.MessageEventFireEventException;
import org.pentaho.di.trans.ael.websocket.handler.MessageEventHandler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Created by fcamara on 8/17/17.
 */
public class MessageEventService {
  /**
   * Contains a list of all events type and their registered handlers
   */
  private Map<Message, List<MessageEventHandler>> registeredHandlers;

  public MessageEventService() {
    registeredHandlers = new HashMap<Message, List<MessageEventHandler>>();
  }

  /**
   * Fire the given Message only to interested handlers
   *
   * @throws MessageEventFireEventException
   */
  public void fireEvent( final Message event ) throws MessageEventFireEventException {
    if ( event != null ) {
      // if at least 1 eventFilter contains a group of handlers for the given event type
      if ( containsHandlerFor( event ) ) {
        // retrieve the handler list concerned by the given event
        final Collection<MessageEventHandler> handlers = getHandlersFor( event );

        if ( handlers != null && handlers.size() > 0 ) {
          MessageEventFireEventException messageEventFireEventException = null;
          for ( final MessageEventHandler handler : handlers ) {
            // for each handler, I check if it's interested or not by the given event
            try {
              handler.execute( event );
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

  /**
   * No handler duplication in a list for a given event type
   */
  public final void addHandler( final Message eventType, final MessageEventHandler handler )
    throws HandlerRegistrationException {
    if ( handler != null && eventType != null ) {
      addHandlerFor( eventType, handler );
    } else {
      //TODO: Log the exception
      throw new HandlerRegistrationException(
        "One of the parameters is null : " + " eventType: " + eventType + " handler:" + handler );
    }
  }

  public final boolean hasHandlers( final Message eventType ) {
    return containsHandlerFor( eventType );
  }

  private boolean containsHandlerFor( final Message eventType ) {
    if ( eventType == null || registeredHandlers.isEmpty() ) {
      return false;
    }
    MessageComparator msgComparator = new MessageComparator();
    for ( Message e : registeredHandlers.keySet() ) {
      if ( msgComparator.compare( e, eventType ) == 0 ) {
        return true;
      }
    }
    return false;
  }

  public List<MessageEventHandler> getHandlersFor( final Message eventType ) {
    List<MessageEventHandler> toRet = null;
    if ( eventType == null || registeredHandlers.isEmpty() ) {
      return toRet;
    }
    MessageComparator msgComparator = new MessageComparator();

    for ( Message e : registeredHandlers.keySet() ) {
      if ( msgComparator.compare( e, eventType ) == 0 ) {
        toRet = registeredHandlers.get( e );
      }
    }
    return toRet;
  }

  private void addHandlerFor( final Message eventType, final MessageEventHandler handler )
    throws HandlerRegistrationException {
    // check if the given event type is already registered in the Message Service
    if ( containsHandlerFor( eventType ) ) {
      // if the handler already exists for the same eventType, an Exception is thrown
      final List<MessageEventHandler> handlers = getHandlersFor( eventType );

      // Check if another handler of the same class is already registered
      for ( MessageEventHandler tmpHandler : handlers ) {
        if ( tmpHandler.getIdentifier().equals( handler.getIdentifier() ) ) {
          throw new HandlerRegistrationException(
            "The handler with identifier " + tmpHandler.getIdentifier() + " is already registered for the event "
              + eventType );
        }
      }

      handlers.add( handler );
    } else {
      // if the given type doesn't already exist in the eventFilters list, we create it
      final List<MessageEventHandler> newHandlerList =
        new ArrayList<>( 10 );
      newHandlerList.add( handler );
      registeredHandlers.put( eventType, newHandlerList );
    }
  }

  private class MessageComparator implements Comparator<Message> {
    @Override
    public int compare( Message o1, Message o2 ) {
      if ( o1 == null || o2 == null ) {
        return -1;
      }
      if ( o1.getClass().getName().equals( o2.getClass().getName() ) ) {
        //StopMessage not PDIEvent instance
        if ( o2 instanceof StopMessage ) {
          return 0;
        } else if ( o2 instanceof PDIEvent ) {
          //others messages from the Daemon to Client are PDIEvent <RemoteSource, ?>
          RemoteSource keyRemoteSource = ( (RemoteSource) ( (PDIEvent) o1 ).getSource() );
          RemoteSource remoteSource = ( (RemoteSource) ( (PDIEvent) o2 ).getSource() );

          if ( sameType( keyRemoteSource, remoteSource )
               && ( sameId( keyRemoteSource, remoteSource ) || isTrans( keyRemoteSource ) ) ) {
            return 0;
          }
        }
      }
      return -1;
    }

    private boolean isTrans( RemoteSource source1 ) {
      return source1.getModelType() == ModelType.TRANSFORMATION;
    }

    private boolean sameId( RemoteSource source1, RemoteSource source2 ) {
      return ( Objects.isNull( source1.getId() ) && Objects.isNull( source2.getId() ) )
        || ( Objects.nonNull( source1.getId() ) && source1.getId().equals( source2.getId() ) );
    }

    private boolean sameType( RemoteSource source1, RemoteSource source2 ) {
      return source1.getModelType() == source2.getModelType();
    }
  }
}
