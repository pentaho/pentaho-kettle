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

import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
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
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.util.GenericStepData;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.jms.JmsConsumerMeta;
import org.pentaho.di.trans.step.jms.JmsDelegate;
import org.pentaho.di.trans.step.jms.JmsProducer;
import org.pentaho.di.trans.step.jms.JmsProducerMeta;

import javax.jms.ConnectionFactory;
import javax.jms.JMSContext;
import javax.jms.JMSProducer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@RunWith( MockitoJUnitRunner.class )
public class ActiveMQProviderTest {
  @Mock LogChannelInterfaceFactory logChannelFactory;
  @Mock LogChannelInterface logChannel;

  static final String PROPERTY_NAME_ONE = "property1";
  static final String PROPERTY_NAME_TWO = "property2";

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

  @Test public void testProperties() throws InterruptedException, KettleException {
    List<JmsProvider> jmsProviders = new ArrayList<>();
    ActiveMQProvider activeMQProvider = spy( new ActiveMQProvider() );
    jmsProviders.add( activeMQProvider );

    JmsDelegate jmsDelegate = new JmsDelegate( jmsProviders );
    jmsDelegate.connectionType = "ACTIVEMQ";
    jmsDelegate.destinationType = "QUEUE";
    jmsDelegate.amqUrl = "vm://0";
    jmsDelegate.amqUsername = "";
    jmsDelegate.amqPassword = "";
    jmsDelegate.destinationName = "testDestination";
    jmsDelegate.receiveTimeout = "0";

    Map<String, String> propertyValuesByName = new LinkedHashMap<>( );
    propertyValuesByName.put( PROPERTY_NAME_ONE, "property1Value" );
    propertyValuesByName.put( PROPERTY_NAME_TWO, "property2Value" );

    JmsProducerMeta meta = new JmsProducerMeta( jmsDelegate );
    meta.setPropertyValuesByName( propertyValuesByName );

    StepMeta stepMeta = new StepMeta( "JmsProducer", meta );

    TransMeta transMeta = new TransMeta( );
    transMeta.addStep( stepMeta );
    Trans trans = new Trans( transMeta );
    GenericStepData data = new GenericStepData();

    JmsProducer step = spy( new JmsProducer( stepMeta, data, 1, transMeta, trans ) );

    //Create a real JMSContext
    ConnectionFactory factory = new ActiveMQConnectionFactory( step.environmentSubstitute( jmsDelegate.amqUrl ) );
    JMSContext jmsContext = spy( factory.createContext( step.environmentSubstitute( jmsDelegate.amqUsername ), step.environmentSubstitute( jmsDelegate.amqPassword ) ) );

    //Return the real context when JmsProvider.getContext(...) is called
    doReturn( jmsContext ).when( activeMQProvider ).getContext( any(), any() );

    //Create a real JMSProducer
    JMSProducer producer = jmsContext.createProducer();

    //Return the real JMSProducer when JMSContext.createProducer() is called
    doReturn( producer ).when( jmsContext ).createProducer();

    //Return row data when step.getRow() is called
    Object[] row = new Object[] { "one", "two" };
    doReturn( row ).when( step ).getRow();
    doNothing().when( step ).putRow( any( RowMetaInterface.class ), any( Object[].class ) );

    //Mock inputRowMeta into the step
    String[] fieldNames = new String[] { "one", "two" };
    RowMetaInterface inputRowMeta = mock( RowMetaInterface.class );
    when( inputRowMeta.clone() ).thenReturn( inputRowMeta );
    when( inputRowMeta.size() ).thenReturn( 2 );
    when( inputRowMeta.getFieldNames() ).thenReturn( fieldNames );
    when( inputRowMeta.indexOfValue( any() ) ).thenReturn( 0 );
    step.setInputRowMeta( inputRowMeta );

    step.init( meta, data );

    trans.prepareExecution( new String[] {} );
    trans.startThreads();
    trans.waitUntilFinished();

    Runnable processRowRunnable = () -> {
      try {
        step.processRow( meta, data );
      } catch ( KettleException e ) {
        fail( e.getMessage() );
      }
    };

    ExecutorService service = Executors.newSingleThreadExecutor();
    service.submit( processRowRunnable );
    service.awaitTermination( 5, TimeUnit.SECONDS );
    step.stopRunning( meta, data );
    service.shutdown();

    //Ensure the producer properties were set
    assertEquals( propertyValuesByName.get( PROPERTY_NAME_ONE ), producer.getStringProperty( PROPERTY_NAME_ONE ) );
    assertEquals( propertyValuesByName.get( PROPERTY_NAME_TWO ), producer.getStringProperty( PROPERTY_NAME_TWO ) );
  }
}
