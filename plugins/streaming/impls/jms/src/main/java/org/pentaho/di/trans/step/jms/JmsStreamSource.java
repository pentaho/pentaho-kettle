/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
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

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import org.pentaho.di.trans.streaming.common.BaseStreamStep;
import org.pentaho.di.trans.streaming.common.BlockingQueueStreamSource;

import javax.jms.JMSConsumer;
import javax.jms.JMSRuntimeException;
import javax.jms.Message;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.google.common.collect.ImmutableList.of;
import static io.reactivex.schedulers.Schedulers.io;
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
    consumer = jmsDelegate.getJmsContext( streamStep )
      .createConsumer( jmsDelegate.getDestination( streamStep ) );
    Observable
      .create( receiveLoop() )  // jms loop
      .subscribeOn( io() )   // subscribe and observe on new io threads
      .observeOn( io() )
      .doOnNext( message -> acceptRows( singletonList( of( message, jmsDelegate.destinationName ) ) ) )
      .doOnComplete( this::close )
      .doOnError( this::error )
      .publish() // publish/connect will "start" the receive loop
      .connect();
  }

  /**
   * Will receive messages from consumer.  If timeout is hit, consumer.receive(timeout)
   * will return null, and the observable will be completed.
   */
  private ObservableOnSubscribe<Object> receiveLoop() {
    return emitter -> {
      Message message;
      try {
        while ( ( message = consumer.receive( receiverTimeout ) ) != null ) {
          streamStep.logDebug( message.toString() );
          emitter.onNext( message.getBody( Object.class ) );
        }
      } catch ( JMSRuntimeException jmsException ) {
        emitter.onError( jmsException );
      }
      if ( !closed.get() ) {
        streamStep.logBasic( getString( PKG, "JmsStreamSource.HitReceiveTimeout" ) );
      }
      emitter.onComplete();
    };
  }

  @Override public void close() {
    super.close();
    closed.set( true );
    if ( consumer != null ) {
      consumer.close();
    }
  }
}
