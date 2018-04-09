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
package org.pentaho.di.trans.step.jms.context;

import org.apache.activemq.artemis.junit.EmbeddedJMSResource;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.logging.LogChannelInterfaceFactory;
import org.pentaho.di.core.plugins.StepPluginType;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.jms.JmsConsumerMeta;
import org.pentaho.di.trans.step.jms.JmsProducerMeta;

import java.util.Collections;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith( MockitoJUnitRunner.class )
public class ActiveMQProviderTest {
  @Mock LogChannelInterfaceFactory logChannelFactory;
  @Mock LogChannelInterface logChannel;

  @Rule public EmbeddedJMSResource resource = new EmbeddedJMSResource( 0 );

  @BeforeClass
  public static void setupClass() throws Exception {
    StepPluginType.getInstance().handlePluginAnnotation(
      JmsProducerMeta.class,
      JmsProducerMeta.class.getAnnotation( org.pentaho.di.core.annotations.Step.class ),
      Collections.emptyList(), false, null );
    StepPluginType.getInstance().handlePluginAnnotation(
      JmsConsumerMeta.class,
      JmsConsumerMeta.class.getAnnotation( org.pentaho.di.core.annotations.Step.class ),
      Collections.emptyList(), false, null );
    KettleEnvironment.init();
  }

  @Before
  public void setUp() {
    KettleLogStore.setLogChannelInterfaceFactory( logChannelFactory );
    when( logChannelFactory.create( any(), any() ) ).thenReturn( logChannel );
    when( logChannelFactory.create( any() ) ).thenReturn( logChannel );
  }

  @Test public void testFullCircle() throws KettleException, InterruptedException, TimeoutException,
    ExecutionException {
    TransMeta consumerMeta = new TransMeta( getClass().getResource( "/amq-consumer.ktr" ).getPath() );
    Trans consumerTrans = new Trans( consumerMeta );
    consumerTrans.prepareExecution( new String[] {} );
    consumerTrans.startThreads();

    TransMeta producerMeta = new TransMeta( getClass().getResource( "/amq-producer.ktr" ).getPath() );
    Trans producerTrans = new Trans( producerMeta );
    producerTrans.prepareExecution( new String[] {} );
    producerTrans.startThreads();
    producerTrans.waitUntilFinished();

    Future<?> future = Executors.newSingleThreadExecutor().submit( () -> {
      while ( true ) {
        if ( consumerTrans.getSteps().get( 0 ).step.getLinesWritten() == 10 ) {
          break;
        }
      }
    } );
    future.get( 5, TimeUnit.SECONDS );
    consumerTrans.safeStop();
    assertEquals( 10, consumerTrans.getSteps().get( 0 ).step.getLinesWritten() );
  }
}
