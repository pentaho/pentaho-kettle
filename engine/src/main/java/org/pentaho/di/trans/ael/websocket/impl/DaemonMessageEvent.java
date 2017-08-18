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


import org.pentaho.di.engine.api.events.LogEvent;
import org.pentaho.di.engine.api.events.MetricsEvent;
import org.pentaho.di.engine.api.events.StatusEvent;
import org.pentaho.di.engine.api.events.DataEvent;
import org.pentaho.di.engine.api.model.Transformation;
import org.pentaho.di.engine.api.remote.Message;
import org.pentaho.di.engine.api.remote.StopMessage;
import org.pentaho.di.trans.ael.websocket.event.MessageEventType;
import org.pentaho.di.trans.ael.websocket.event.MessageEvent;
import org.pentaho.di.trans.ael.websocket.exception.MessageEventFireEventException;


/**
 * Created by fcamara on 8/17/17.
 */
public class DaemonMessageEvent implements MessageEvent {
  private MessageEventType messageEventType = null;
  private String objectID = null;

  public DaemonMessageEvent( MessageEventType messageEventType ) {
    this( messageEventType, null );
  }

  public DaemonMessageEvent( MessageEventType messageEventType, String objectID ) {
    this.messageEventType = messageEventType;
    this.objectID = objectID;
  }

  public DaemonMessageEvent( Message message ) throws MessageEventFireEventException {
    if ( message instanceof MetricsEvent ) {
      messageEventType = MessageEventType.METRICS;
      objectID = ( (MetricsEvent) message ).getSource().getId();
    } else if ( message instanceof LogEvent ) {
      messageEventType =
        ( (LogEvent) message ).getSource() instanceof Transformation ? MessageEventType.TRANSFORMATION_LOG
          : MessageEventType.OPERATION_LOG;
      objectID = messageEventType == MessageEventType.OPERATION_LOG ? ( (LogEvent) message ).getSource().getId() : null;
    } else if ( message instanceof StatusEvent ) {
      messageEventType =
        ( (StatusEvent) message ).getSource() instanceof Transformation ? MessageEventType.TRANSFORMATION_STATUS
          : MessageEventType.OPERATION_STATUS;
      objectID =
        messageEventType == MessageEventType.OPERATION_STATUS ? ( (StatusEvent) message ).getSource().getId() : null;
    } else if ( message instanceof DataEvent ) {
      messageEventType = MessageEventType.ROWS;
      objectID = ( (DataEvent) message ).getSource().getId();
    } else if ( message instanceof StopMessage ) {
      messageEventType = MessageEventType.STOP;
    } else {
      throw new MessageEventFireEventException( "Unexpected Message Type: " + message.getClass().getName() );
    }
  }

  @Override
  public String getObjectId() {
    return objectID;
  }

  @Override
  public MessageEventType getType() {
    return messageEventType;
  }
}
