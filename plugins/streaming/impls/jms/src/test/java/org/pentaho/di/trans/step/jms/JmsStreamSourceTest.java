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

import com.ibm.msg.client.jms.JmsContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.jms.Destination;
import javax.jms.JMSConsumer;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith ( MockitoJUnitRunner.class )
public class JmsStreamSourceTest {

  @Mock private JmsContext context;
  @Mock private JmsDelegate meta;
  @Mock private JMSConsumer consumer;
  @Mock private JmsConsumer consumerStep;
  @Mock private Destination destination;
  @Mock private Message message;

  @Captor private ArgumentCaptor<MessageListener> listener;

  @InjectMocks private JmsStreamSource source;

  @Before
  public void before() throws JMSException {
    when( meta.getJmsContext() ).thenReturn( context );
    when( meta.getDestination() ).thenReturn( destination );
    when( context.createConsumer( destination ) ).thenReturn( consumer );
    when( message.getBody( Object.class ) ).thenReturn( "message" );
  }


  @Test ( timeout = 5000 )
  public void testReceiveMessage() {
    source.open();

    verify( meta ).getJmsContext();
    verify( meta ).getDestination();
    verify( consumer ).setMessageListener( listener.capture() );

    listener.getValue().onMessage( message );
    List<Object> sentMessage = source.observable().firstElement().blockingGet( Collections.emptyList() );

    assertThat( sentMessage.size(), equalTo( 1 ) );
    assertThat( sentMessage.get( 0 ), equalTo( "message" ) );
  }


}
