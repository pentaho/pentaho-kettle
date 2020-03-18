/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2019 by Hitachi Vantara : http://www.pentaho.com
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


package org.pentaho.di.trans.step.jms;

import org.pentaho.di.trans.streaming.common.BaseStreamStep;
import org.pentaho.di.trans.streaming.common.BlockingQueueStreamSource;

import javax.jms.JMSConsumer;
import javax.jms.JMSException;
import javax.jms.JMSRuntimeException;
import javax.jms.Message;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.util.Collections.singletonList;
import static org.pentaho.di.i18n.BaseMessages.getString;
import static org.pentaho.di.trans.step.jms.JmsConstants.PKG;

public class JmsStreamSource extends BlockingQueueStreamSource<List<Object>> {

  private final JmsDelegate jmsDelegate;
  private final int receiverTimeout;
  private JMSConsumer consumer;
  private AtomicBoolean closed = new AtomicBoolean( false );

  JmsStreamSource( BaseStreamStep streamStep, JmsDelegate jmsDelegate, int receiverTimeout ) {
    super( streamStep );
    this.jmsDelegate = jmsDelegate;
    this.receiverTimeout = receiverTimeout;
  }

  @Override public void open() {
    consumer = jmsDelegate.getJmsContext()
      .createConsumer( jmsDelegate.getDestination() );
    Executors.newSingleThreadExecutor().submit( this::receiveLoop  );
  }

  /**
   * Will receive messages from consumer.  If timeout is hit, consumer.receive(timeout)
   * will return null, and the observable will be completed.
   */
  private void receiveLoop() {
    Message message;
    try {
      while ( !closed.get() && ( message = consumer.receive( receiverTimeout ) ) != null ) {
        streamStep.logDebug( message.toString() );
        Date date = new Date( message.getJMSTimestamp() );
        DateFormat formatter = new SimpleDateFormat( "MM-dd-yyyy HH:mm:ss a" );
        formatter.setTimeZone( TimeZone.getTimeZone( "UTC" ) );
        String jmsTimestamp = formatter.format( date );
        acceptRows( singletonList( Arrays.asList( message.getBody( Object.class ), jmsDelegate.destinationName, message.getJMSMessageID(), jmsTimestamp, message.getJMSRedelivered() ) ) );
      }
    } catch ( JMSRuntimeException | JMSException jmsException ) {
      error( jmsException );
    } finally {
      super.close();
      if ( !closed.get() ) {
        close();
        streamStep.logBasic( getString( PKG, "JmsStreamSource.HitReceiveTimeout" ) );
      }
    }
  }

  @Override public void close() {
    //don't call super.close().  need to wait for the receiveLoop to be done
    if ( consumer != null && !closed.getAndSet( true ) ) {
      consumer.close();
      jmsDelegate.getJmsContext().close();
    }
  }
}
