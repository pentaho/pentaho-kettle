/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2018-2024 by Hitachi Vantara : http://www.pentaho.com
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

import org.apache.activemq.artemis.junit.EmbeddedJMSResource;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.logging.LogChannelInterfaceFactory;
import org.pentaho.di.core.plugins.StepPluginType;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.util.GenericStepData;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaDataCombi;
import org.pentaho.di.trans.step.jms.context.ActiveMQProvider;
import org.pentaho.di.trans.step.jms.context.JmsProvider;

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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith ( MockitoJUnitRunner.StrictStubs.class )
public class JmsProducerTest {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();
  @Mock LogChannelInterfaceFactory logChannelFactory;
  @Mock LogChannelInterface logChannel;
  @Mock JMSContext jmsContext;
  @Mock JmsProvider jmsProvider;
  @Mock JMSProducer jmsProducer;

  @Rule public EmbeddedJMSResource resource = new EmbeddedJMSResource( 0 );

  static final String PROPERTY_NAME_ONE = "property1";
  static final String PROPERTY_NAME_TWO = "property2";
  private Trans trans;
  private JmsProducer step;
  private JmsProducerMeta meta;
  private GenericStepData data;

  @BeforeClass
  public static void setupClass() throws Exception {
    StepPluginType.getInstance().handlePluginAnnotation(
      JmsProducerMeta.class,
      JmsProducerMeta.class.getAnnotation( org.pentaho.di.core.annotations.Step.class ),
      Collections.emptyList(), false, null );
  }

  @Before
  public void setUp() throws KettleException {
    KettleLogStore.setLogChannelInterfaceFactory( logChannelFactory );
    when( logChannelFactory.create( any(), any() ) ).thenReturn( logChannel );
    lenient().when( logChannelFactory.create( any() ) ).thenReturn( logChannel );

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


    meta = new JmsProducerMeta( jmsDelegate );

    StepMeta stepMeta = new StepMeta( "JmsProducer", meta );

    TransMeta transMeta = new TransMeta();
    transMeta.addStep( stepMeta );
    trans = new Trans( transMeta );
    data = new GenericStepData();

    step = spy( new JmsProducer( stepMeta, data, 1, transMeta, trans ) );

    //Return row data when step.getRow() is called
    Object[] row = new Object[] { "one", "two" };
    doReturn( row ).when( step ).getRow();
    doNothing().when( step ).putRow( any( RowMetaInterface.class ), any( Object[].class ) );

    //Mock inputRowMeta into the step
    String[] fieldNames = new String[] { "one", "two" };
    RowMetaInterface inputRowMeta = mock( RowMetaInterface.class );
    lenient().when( inputRowMeta.clone() ).thenReturn( inputRowMeta );
    lenient().when( inputRowMeta.size() ).thenReturn( 2 );
    lenient().when( inputRowMeta.getFieldNames() ).thenReturn( fieldNames );
    when( inputRowMeta.indexOfValue( any() ) ).thenReturn( 0 );
    step.setInputRowMeta( inputRowMeta );
  }

  @Test public void testProperties() throws InterruptedException, KettleException, TimeoutException,
    ExecutionException {
    Map<String, String> propertyValuesByName = new LinkedHashMap<>();
    propertyValuesByName.put( PROPERTY_NAME_ONE, "property1Value" );
    propertyValuesByName.put( PROPERTY_NAME_TWO, "property2Value" );
    meta.setPropertyValuesByName( propertyValuesByName );

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
    service.submit( processRowRunnable ).get( 5, TimeUnit.SECONDS );
    service.awaitTermination( 5, TimeUnit.SECONDS );
    step.stopRunning( meta, data );
    service.shutdown();

    //Ensure the producer properties were set
    assertEquals( propertyValuesByName.get( PROPERTY_NAME_ONE ), step.producer.getStringProperty( PROPERTY_NAME_ONE ) );
    assertEquals( propertyValuesByName.get( PROPERTY_NAME_TWO ), step.producer.getStringProperty( PROPERTY_NAME_TWO ) );
  }

  @Test
  public void testSetOptions() throws KettleException {
    step.init( meta, data );

    //Defaults
    step.processRow( meta, data );

    assertEquals( false, step.producer.getDisableMessageID() );
    assertEquals( false, step.producer.getDisableMessageTimestamp() );
    assertEquals( 2, step.producer.getDeliveryMode() );
    assertEquals( 4, step.producer.getPriority() );
    assertEquals( 0, step.producer.getTimeToLive() );
    assertEquals( 0, step.producer.getDeliveryDelay() );
    assertNull( step.producer.getJMSCorrelationID() );
    assertNull( step.producer.getJMSType() );
  }

  @Test
  public void testUserDrivenSetOptions() throws KettleException {
    //User driven
    meta.setDisableMessageId( "true" );
    meta.setDisableMessageTimestamp( "false" );
    meta.setDeliveryMode( "1" );
    meta.setPriority( "2" );
    meta.setTimeToLive( "3" );
    meta.setDeliveryDelay( "4" );
    meta.setJmsCorrelationId( "ASDF" );
    meta.setJmsType( "JMSType" );

    step.first = true;

    step.init( meta, data );

    step.processRow( meta, data );

    assertEquals( true, step.producer.getDisableMessageID() );
    assertEquals( false, step.producer.getDisableMessageTimestamp() );
    assertEquals( 1, step.producer.getDeliveryMode() );
    assertEquals( 2, step.producer.getPriority() );
    assertEquals( 3, step.producer.getTimeToLive() );
    assertEquals( 4, step.producer.getDeliveryDelay() );
    assertEquals( "ASDF", step.producer.getJMSCorrelationID() );
    assertEquals( "JMSType", step.producer.getJMSType() );
  }

  @Test
  public void testInit() {
    meta.setDisableMessageId( "true" );
    meta.setDisableMessageTimestamp( "false" );
    meta.setDeliveryMode( "1" );
    meta.setPriority( "2" );
    meta.setTimeToLive( "3" );
    meta.setDeliveryDelay( "4" );
    meta.setJmsCorrelationId( "ASDF" );
    meta.setJmsType( "JMSType" );

    assertTrue( step.init( meta, data ) );

    meta.setDisableMessageTimestamp( "asdf" );
    meta.setDisableMessageId( "asdf" );
    meta.setDeliveryMode( "asdf" );
    meta.setPriority( "asdf" );
    meta.setPriority( "asdf" );
    meta.setTimeToLive( "asdf" );

    assertFalse( step.init( meta, data ) );
  }

  @Test
  public void jmsContextClosedOnStop() throws Exception {
    TransMeta transMeta = new TransMeta( getClass().getResource( "/jms-generate-produce.ktr" ).getPath() );
    Trans trans = new Trans( transMeta );
    trans.prepareExecution( new String[] {} );

    StepMetaDataCombi combi = trans.getSteps().get( 1 );
    JmsProducer step = (JmsProducer) combi.step;
    JmsProducerMeta jmsMeta = step.meta;
    jmsMeta.jmsDelegate.jmsProviders = Collections.singletonList( jmsProvider );
    when( jmsProvider.supports( JmsProvider.ConnectionType.ACTIVEMQ ) ).thenReturn( true );
    when( jmsProvider.getContext( jmsMeta.jmsDelegate ) ).thenReturn( jmsContext );
    when( jmsContext.createProducer() ).thenReturn( jmsProducer );
    when( jmsProducer.send( jmsMeta.jmsDelegate.getDestination(), "ackbar" ) ).then( ignore -> {
      trans.stopAll();
      return null;
    } );

    trans.startThreads();
    trans.waitUntilFinished();
    verify( jmsContext ).close();
  }

  @Test
  public void testProcessRow() throws Exception {

    doReturn( null ).when( step ).getRow();

    assertFalse( step.processRow( meta, data ) );
  }
}
