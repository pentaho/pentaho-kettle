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

package org.pentaho.di.trans.step.jms.analyzer;

import org.pentaho.di.trans.ISubTransAwareMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.jms.JmsConsumerMeta;
import org.pentaho.di.trans.step.jms.JmsDelegate;
import org.pentaho.dictionary.DictionaryConst;
import org.pentaho.metaverse.api.IMetaverseNode;
import org.pentaho.metaverse.api.MetaverseAnalyzerException;
import org.pentaho.metaverse.api.StepField;
import org.pentaho.metaverse.api.analyzer.kettle.KettleAnalyzerUtil;
import org.pentaho.metaverse.api.analyzer.kettle.step.IClonableStepAnalyzer;
import org.pentaho.metaverse.api.analyzer.kettle.step.StepAnalyzer;

import com.tinkerpop.blueprints.Vertex;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class JmsConsumerAnalyzer extends StepAnalyzer<JmsConsumerMeta> {

  @Override
  public Set<Class<? extends BaseStepMeta>> getSupportedSteps() {
    final Set<Class<? extends BaseStepMeta>> supportedSteps = new HashSet<>();
    supportedSteps.add( JmsConsumerMeta.class );
    return supportedSteps;
  }

  @Override
  protected Set<StepField> getUsedFields( final JmsConsumerMeta meta ) {
    return Collections.emptySet();
  }

  @Override
  protected void customAnalyze( final JmsConsumerMeta meta, final IMetaverseNode rootNode )
    throws MetaverseAnalyzerException {
    // TODO: When/If adding JmsProducerAnalyzer, move common code to new base class.

    final JmsDelegate jmsDelegate = meta.getJmsDelegate();

    rootNode.setProperty( "batchSize", parentTransMeta.environmentSubstitute( meta.getBatchSize() ) );
    rootNode.setProperty( "batchDuration", parentTransMeta.environmentSubstitute( meta.getBatchDuration() ) );
    rootNode.setProperty( "connectionType", jmsDelegate.getConnectionType() );
    rootNode.setProperty( "connectionUrl", parentTransMeta.environmentSubstitute( jmsDelegate.getConnectionUrl() ) );
    rootNode.setProperty( "destinationType",
      parentTransMeta.environmentSubstitute( jmsDelegate.getDestinationType() ) );
    rootNode.setProperty( "destinationName",
      parentTransMeta.environmentSubstitute( jmsDelegate.getDestinationName() ) );
    rootNode.setProperty( "receiveTimeout", parentTransMeta.environmentSubstitute( meta.getReceiveTimeout() ) );

    KettleAnalyzerUtil.analyze( this, parentTransMeta, meta, rootNode );
  }

  @Override
  public void postAnalyze( final JmsConsumerMeta meta )
    throws MetaverseAnalyzerException {

    final String transformationPath = parentTransMeta.environmentSubstitute( meta.getFileName() );
    final TransMeta subTransMeta = KettleAnalyzerUtil.getSubTransMeta( (ISubTransAwareMeta) meta );
    subTransMeta.setFilename( transformationPath );

    // get the vertex corresponding to this step
    final Vertex stepVertex = findStepVertex( parentTransMeta, parentStepMeta.getName() );

    // for each output field of the sub step name, map the field to the output field of this jms consumer step with
    // the same name
    String subTransResultStepName = null;
    for ( final StepMeta subTransStep : subTransMeta.getSteps() ) {
      if ( meta.getSubStep().equals( subTransStep.getName() ) ) { // TODO: constant for RecordsFromStream
        subTransResultStepName = subTransStep.getName();
        break;
      }
    }
    if ( subTransResultStepName != null ) {
      final List<Vertex> subTransResultFields = findFieldVertices( subTransMeta, subTransResultStepName, null );
      for ( final Vertex subTransResultField : subTransResultFields ) {
        // find the corresponding output field in this jms consumer step
        final Vertex stepOutputField = findFieldVertex( parentTransMeta, parentStepMeta.getName(),
          subTransResultField.getProperty( DictionaryConst.PROPERTY_NAME ).toString() );
        if ( stepOutputField != null ) {
          getMetaverseBuilder().addLink( subTransResultField, DictionaryConst.LINK_INPUTS, stepVertex );
          getMetaverseBuilder().addLink( subTransResultField, DictionaryConst.LINK_DERIVES, stepOutputField );
        } else {
          // TODO: log
        }
      }
    } else {
      // TODO: log
    }
  }

  @Override protected IClonableStepAnalyzer newInstance() {
    return new JmsConsumerAnalyzer();
  }

}
