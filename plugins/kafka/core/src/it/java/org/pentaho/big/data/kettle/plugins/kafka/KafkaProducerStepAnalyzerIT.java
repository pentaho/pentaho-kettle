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

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.StepPluginType;
import org.pentaho.dictionary.DictionaryConst;
import org.pentaho.metaverse.frames.Concept;
import org.pentaho.metaverse.frames.FramedMetaverseNode;
import org.pentaho.metaverse.frames.TransformationNode;
import org.pentaho.metaverse.frames.TransformationStepNode;
import org.pentaho.metaverse.impl.MetaverseConfig;
import org.pentaho.metaverse.step.StepAnalyzerValidationIT;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.apache.commons.collections4.IteratorUtils.toList;
import static org.junit.Assert.assertEquals;
import static org.pentaho.big.data.kettle.plugins.kafka.KafkaLineageConstants.KAFKA_SERVER_METAVERSE;
import static org.pentaho.big.data.kettle.plugins.kafka.KafkaLineageConstants.KAFKA_TOPIC_METAVERSE;
import static org.pentaho.dictionary.DictionaryConst.LINK_CONTAINS;
import static org.pentaho.dictionary.DictionaryConst.LINK_CONTAINS_CONCEPT;
import static org.pentaho.dictionary.DictionaryConst.LINK_DEPENDENCYOF;
import static org.pentaho.dictionary.DictionaryConst.LINK_TYPE_CONCEPT;

@RunWith( PowerMockRunner.class )
@PrepareForTest( MetaverseConfig.class )
public class KafkaProducerStepAnalyzerIT extends StepAnalyzerValidationIT {

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
  public void analyzerProducer() throws Exception {
    final String transNodeName = "generateAndProduce";
    initTest( transNodeName );

    final TransformationNode transformationNode = verifyTransformationNode( transNodeName, false );
    assertEquals( "Unexpected number of nodes", 19, getIterableSize( framedGraph.getVertices() ) );
    assertEquals( "Unexpected number of edges", 34, getIterableSize( framedGraph.getEdges() ) );

    final Map<String, FramedMetaverseNode> stepNodeMap = verifyTransformationSteps( transformationNode,
      new String[] { "Generate Rows", "Kafka Producer" },  false );
    TransformationStepNode kafkaProducer = (TransformationStepNode) stepNodeMap.get( "Kafka Producer" );
    FramedMetaverseNode kurt = verifyLinkedNode( kafkaProducer, DictionaryConst.LINK_WRITESTO, "kurt" );
    verifyLinkedNode( kurt, LINK_CONTAINS, KafkaLineageConstants.KEY );
    verifyLinkedNode( kurt, LINK_CONTAINS, KafkaLineageConstants.MESSAGE );

    List<Concept> topicConcept = toList( kurt.getInNodes( LINK_TYPE_CONCEPT ).iterator() );
    assertEquals( 1, topicConcept.size() );
    assertEquals( KAFKA_TOPIC_METAVERSE, topicConcept.get( 0 ).getName() );

    List<Concept> serverConcept = toList( topicConcept.get( 0 ).getInNodes( LINK_CONTAINS_CONCEPT ).iterator() );
    assertEquals( 1, serverConcept.size() );
    assertEquals( KAFKA_SERVER_METAVERSE, serverConcept.get( 0 ).getName() );

    List<Concept> producerDependency = toList( kafkaProducer.getInNodes( LINK_DEPENDENCYOF ).iterator() );
    assertEquals( 1, producerDependency.size() );
    assertEquals( "10.177.178.135:9092", producerDependency.get( 0 ).getName() );

    List<Concept> serverType = toList( producerDependency.get( 0 ).getInNodes( LINK_TYPE_CONCEPT ).iterator() );
    assertEquals( 1, serverType.size() );
    assertEquals( KAFKA_SERVER_METAVERSE, serverType.get( 0 ).getName() );
  }
}
