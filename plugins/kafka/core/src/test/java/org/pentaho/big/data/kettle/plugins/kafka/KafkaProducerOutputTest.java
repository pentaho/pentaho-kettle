/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.big.data.kettle.plugins.kafka;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.di.core.bowl.DefaultBowl;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.StepPluginType;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMetaDataCombi;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith( MockitoJUnitRunner.class )
public class KafkaProducerOutputTest {
  @Mock KafkaProducer<Object, Object> kafkaProducer;
  @Mock KafkaFactory kafkaFactory;

  @BeforeClass
  public static void setUp() throws Exception {
    KettleClientEnvironment.init();
    PluginRegistry.addPluginType( StepPluginType.getInstance() );
    PluginRegistry.init();
    if ( !Props.isInitialized() ) {
      Props.init( 0 );
    }
    StepPluginType.getInstance().handlePluginAnnotation(
      KafkaProducerOutputMeta.class,
      KafkaProducerOutputMeta.class.getAnnotation( org.pentaho.di.core.annotations.Step.class ),
      Collections.emptyList(), false, null );
  }

  @Test
  public void testSendsEachRowToProducer() throws Exception {
    TransMeta transMeta = new TransMeta( DefaultBowl.getInstance(),
      getClass().getResource( "/produceFourRows.ktr" ).getPath() );
    Trans trans = new Trans( transMeta );
    trans.setVariable( "keyField", "key" );
    trans.setVariable( "messageField", "message" );
    trans.setVariable( "topic", "kurt" );
    trans.prepareExecution( new String[]{} );

    StepMetaDataCombi combi = trans.getSteps().get( 1 );
    KafkaProducerOutput step = (KafkaProducerOutput) combi.step;
    KafkaProducerOutputData data = (KafkaProducerOutputData) combi.data;

    when( kafkaFactory.producer( any(), any(), any(), any() ) ).thenReturn( kafkaProducer );

    step.setKafkaFactory( kafkaFactory );

    trans.startThreads();
    trans.waitUntilFinished();
    Mockito.verify( kafkaProducer, Mockito.times( 4 ) ).send( new ProducerRecord<>( "kurt", "one", "winning" ), step );
    Mockito.verify( kafkaProducer, Mockito.times( 1 ) ).close();
    assertEquals( 4, trans.getSteps().get( 1 ).step.getLinesOutput() );
  }

  @Test
  public void testSendsEachRowToProducer_noKey() throws Exception {
    TransMeta transMeta = new TransMeta( DefaultBowl.getInstance(),
      getClass().getResource( "/produceFourRows_noKey.ktr" ).getPath() );
    Trans trans = new Trans( transMeta );
    trans.prepareExecution( new String[]{} );

    StepMetaDataCombi combi = trans.getSteps().get( 1 );
    KafkaProducerOutput step = (KafkaProducerOutput) combi.step;
    KafkaProducerOutputData data = (KafkaProducerOutputData) combi.data;

    when( kafkaFactory.producer( any(), any(), any(), any() ) ).thenReturn( kafkaProducer );

    step.setKafkaFactory( kafkaFactory );

    trans.startThreads();
    trans.waitUntilFinished();
    Mockito.verify( kafkaProducer, Mockito.times( 4 ) ).send( new ProducerRecord<>( "kurt", "winning" ), step );
    Mockito.verify( kafkaProducer, Mockito.times( 1 ) ).close();
    assertEquals( 4, trans.getSteps().get( 1 ).step.getLinesOutput() );
  }

  @Test
  public void testSendsEachRowToProducer_nullKey() throws Exception {
    TransMeta transMeta = new TransMeta( DefaultBowl.getInstance(),
      getClass().getResource( "/produceFourRows_nullKey.ktr" ).getPath() );
    Trans trans = new Trans( transMeta );
    trans.prepareExecution( new String[]{} );

    StepMetaDataCombi combi = trans.getSteps().get( 1 );
    KafkaProducerOutput step = (KafkaProducerOutput) combi.step;
    KafkaProducerOutputData data = (KafkaProducerOutputData) combi.data;

    when( kafkaFactory.producer( any(), any(), any(), any() ) ).thenReturn( kafkaProducer );

    step.setKafkaFactory( kafkaFactory );

    trans.startThreads();
    trans.waitUntilFinished();
    Mockito.verify( kafkaProducer, Mockito.times( 4 ) ).send( new ProducerRecord<>( "akastenka", "msg" ), step );
    Mockito.verify( kafkaProducer, Mockito.times( 1 ) ).close();
    assertEquals( 4, trans.getSteps().get( 1 ).step.getLinesOutput() );
  }

  @Test
  public void kafkaClientClosedOnStop() throws Exception {
    TransMeta transMeta = new TransMeta( DefaultBowl.getInstance(),
      getClass().getResource( "/produceForever.ktr" ).getPath() );
    Trans trans = new Trans( transMeta );
    trans.prepareExecution( new String[]{} );

    StepMetaDataCombi combi = trans.getSteps().get( 1 );
    KafkaProducerOutput step = (KafkaProducerOutput) combi.step;

    when( kafkaFactory.producer( any(), any(), any(), any() ) ).thenReturn( kafkaProducer );
    when( kafkaProducer.send( any(), any() ) ).then( ignore -> {
      trans.stopAll();
      return null; } );

    step.setKafkaFactory( kafkaFactory );
    trans.startThreads();
    trans.waitUntilFinished();
    verify( kafkaProducer ).flush();
    verify( kafkaProducer ).close();
  }

}
