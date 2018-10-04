/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2018 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.ui.trans.steps.metainject.analyzer;

import org.apache.commons.lang.StringUtils;
import org.pentaho.di.core.util.StringUtil;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.steps.metainject.MetaInjectMeta;
import org.pentaho.di.trans.steps.metainject.SourceStepField;
import org.pentaho.di.trans.steps.metainject.TargetStepAttribute;
import org.pentaho.dictionary.DictionaryConst;
import org.pentaho.metaverse.api.IComponentDescriptor;
import org.pentaho.metaverse.api.IMetaverseNode;
import org.pentaho.metaverse.api.INamespace;
import org.pentaho.metaverse.api.MetaverseAnalyzerException;
import org.pentaho.metaverse.api.MetaverseComponentDescriptor;
import org.pentaho.metaverse.api.Namespace;
import org.pentaho.metaverse.api.StepField;
import org.pentaho.metaverse.api.analyzer.kettle.KettleAnalyzerUtil;
import org.pentaho.metaverse.api.analyzer.kettle.step.IClonableStepAnalyzer;
import org.pentaho.metaverse.api.analyzer.kettle.step.StepAnalyzer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MetaInjectAnalyzer extends StepAnalyzer<MetaInjectMeta> {

  @Override
  public Set<Class<? extends BaseStepMeta>> getSupportedSteps() {
    Set<Class<? extends BaseStepMeta>> supported = new HashSet<>();
    supported.add( MetaInjectMeta.class );
    return supported;
  }

  @Override
  protected Set<StepField> getUsedFields( MetaInjectMeta meta ) {

    final Set<StepField> usedFields = new HashSet();

    final Map<TargetStepAttribute, SourceStepField> fieldMappings = meta.getTargetSourceMapping();
    final Iterator<Map.Entry<TargetStepAttribute, SourceStepField>> fieldMappingsIter
      = fieldMappings.entrySet().iterator();
    while ( fieldMappingsIter.hasNext() ) {
      final Map.Entry<TargetStepAttribute, SourceStepField> entry = fieldMappingsIter.next();
      final SourceStepField value = entry.getValue();
      final Iterator<StepField> stepFields = createStepFields( value.getField(), getInputs() ).iterator();
      while ( stepFields.hasNext() ) {
        final StepField stepField = stepFields.next();
        usedFields.add( stepField );
      }
    }

    return usedFields;
  }

  @Override
  protected void customAnalyze( MetaInjectMeta meta, IMetaverseNode rootNode ) throws MetaverseAnalyzerException {

    final String sourceStepName = parentTransMeta.environmentSubstitute( meta.getSourceStepName() );
    rootNode.setProperty( "sourceStepName", sourceStepName );
    rootNode.setProperty( "targetFile", parentTransMeta.environmentSubstitute( meta.getTargetFile() ) );
    rootNode.setProperty( "streamSourceStepname",
      parentTransMeta.environmentSubstitute( meta.getStreamSourceStepname() ) );
    rootNode.setProperty( "streamTargetStepname",
      parentTransMeta.environmentSubstitute( meta.getStreamTargetStepname() ) );
    rootNode.setProperty( "runResultingTransformation", !meta.isNoExecution() );

    final TransMeta subTransMeta = KettleAnalyzerUtil.getSubTransMeta( meta );

    final IMetaverseNode subTransNode = getNode( subTransMeta.getName(), DictionaryConst.NODE_TYPE_TRANS,
      descriptor.getNamespace(), null, null );
    subTransNode.setProperty( DictionaryConst.PROPERTY_PATH,
      KettleAnalyzerUtil.getSubTransMetaPath( meta, subTransMeta ) );
    subTransNode.setLogicalIdGenerator( DictionaryConst.LOGICAL_ID_GENERATOR_DOCUMENT );

    metaverseBuilder.addLink( rootNode, DictionaryConst.LINK_EXECUTES, subTransNode );

    // look at the mdi mappings to mimic the steps within the sub transformation
    final Map<String, IMetaverseNode> subTransSteps = new HashMap();
    final Map<String, IMetaverseNode> subTransFields = new HashMap();

    // create a node for the source step - we will need it later
    if ( StringUtils.isNotBlank( sourceStepName ) ) {
      final IMetaverseNode sourceStepNode = getNode( sourceStepName, DictionaryConst.NODE_TYPE_TRANS_STEP,
        descriptor.getNamespace(), sourceStepName, subTransSteps );
      // it does not matter what the step name, we just need this property to exist
      sourceStepNode.setProperty( DictionaryConst.PROPERTY_STEP_TYPE, "N/A" );
      metaverseBuilder.addLink( subTransNode, DictionaryConst.LINK_CONTAINS, sourceStepNode );
    }

    final Map<TargetStepAttribute, SourceStepField> fieldMappings = meta.getTargetSourceMapping();
    final Iterator<Map.Entry<TargetStepAttribute, SourceStepField>> fieldMappingsIter
      = fieldMappings.entrySet().iterator();

    boolean streaming = false;
    // are we streaming data directly from an injector step to a template step?
    if ( !StringUtil.isEmpty( meta.getStreamSourceStepname() ) && !StringUtil.isEmpty(
      meta.getStreamTargetStepname() ) ) {
      streaming = true;

      final IMetaverseNode streamTargetStepNode = getNode( meta.getStreamTargetStepname(),
        DictionaryConst.NODE_TYPE_TRANS_STEP, descriptor.getNamespace(), meta.getStreamTargetStepname(),
        subTransSteps );
      metaverseBuilder.addLink( subTransNode, DictionaryConst.LINK_CONTAINS, streamTargetStepNode );

      // get the field names names flowing from the stream source step into the template ktr's streaming target
      // step directly
      final Set<String> streamSourceInputStepNames = getInputs().getFieldNames( meta.getStreamSourceStepname() );
      int index = 0;
      for ( final String streamSourceInputStepName : streamSourceInputStepNames ) {
        final IMetaverseNode streamSourceFieldNode = getInputs().findNode( meta.getStreamSourceStepname(),
          streamSourceInputStepName );

        final String targetFieldName = "field_" + StringUtils.leftPad( "" + index, 3 );
        final IMetaverseNode streamTargetFieldNode = getNode( targetFieldName, DictionaryConst.NODE_TYPE_TRANS_FIELD,
          (String) streamTargetStepNode.getProperty( DictionaryConst.PROPERTY_LOGICAL_ID ),
          meta.getStreamTargetStepname() + ":" + targetFieldName, subTransFields );
        metaverseBuilder.addLink( streamSourceFieldNode, DictionaryConst.LINK_DERIVES, streamTargetFieldNode );
        metaverseBuilder.addLink( streamTargetStepNode, DictionaryConst.LINK_OUTPUTS, streamTargetFieldNode );
        index++;
      }
    }

    final StringBuilder usedMdiMappings = new StringBuilder();
    final StringBuilder unusedMdiMappings = new StringBuilder();

    String mdiDelim = "";
    // process the injection mappings
    while ( fieldMappingsIter.hasNext() ) {
      final Map.Entry<TargetStepAttribute, SourceStepField> entry = fieldMappingsIter.next();
      final TargetStepAttribute targetTemplateStepAttr = entry.getKey();
      final SourceStepField sourceInjectorStepField = entry.getValue();

      final IMetaverseNode templateStepNode = getNode( targetTemplateStepAttr.getStepname(),
        DictionaryConst.NODE_TYPE_TRANS_STEP, descriptor.getNamespace(), targetTemplateStepAttr.getStepname(),
        subTransSteps );
      // it does not matter what the step name, we just need this property to exist
      templateStepNode.setProperty( DictionaryConst.PROPERTY_STEP_TYPE, "N/A" );
      metaverseBuilder.addLink( subTransNode, DictionaryConst.LINK_CONTAINS, templateStepNode );

      // if the source step is set to stream data directly to a step in the template (target) transformation, the
      // mappings are ignored, and instead, data is sent directly
      if ( sourceInjectorStepField.getStepname().equalsIgnoreCase( meta.getStreamSourceStepname() ) ) {
        unusedMdiMappings.append( mdiDelim ).append( sourceInjectorStepField.getStepname() ).append( ": " ).append(
          sourceInjectorStepField.getField() ).append( " > [" ).append( subTransNode.getName() ).append( "] " )
          .append( targetTemplateStepAttr.getStepname() ).append( ": " ).append(
          targetTemplateStepAttr.getAttributeKey() );
        mdiDelim = ", ";
      } else {
        usedMdiMappings.append( mdiDelim ).append( sourceInjectorStepField.getStepname() ).append( ": " ).append(
          sourceInjectorStepField.getField() ).append( " > [" ).append( subTransNode.getName() ).append( "] " )
          .append( targetTemplateStepAttr.getStepname() ).append( ": " ).append(
          targetTemplateStepAttr.getAttributeKey() );
        mdiDelim = ", ";

        // if the target template step name is the same as the step we read from, we want to get the fields from the
        // target template step and pass them back to the parant injector step
        if ( targetTemplateStepAttr.getStepname().equalsIgnoreCase( sourceStepName ) ) {
          final IMetaverseNode sourceStepNode = subTransSteps.get( sourceStepName );
          final Set<String> inputStepNames = getInputs().getStepNames();
          for ( final String inputStepName : inputStepNames ) {
            if ( inputStepName.equalsIgnoreCase( sourceStepName ) ) {
              final Set<String> inputFieldNames = getInputs().getFieldNames( inputStepName );
              for ( final String inputFieldName : inputFieldNames ) {
                // create node for each field for the source template Node and link "input" from the inputStep's
                // equivalent field node
                final IMetaverseNode subTransFieldNode = getNode( sourceInjectorStepField.getField(),
                  DictionaryConst.NODE_TYPE_TRANS_FIELD, (String) sourceStepNode.getProperty(
                    DictionaryConst.PROPERTY_LOGICAL_ID ), sourceInjectorStepField.getStepname() + ":"
                    + sourceInjectorStepField.getField(), subTransFields );
                subTransFieldNode.setProperty( DictionaryConst.PROPERTY_CATEGORY, DictionaryConst.CATEGORY_FIELD );
                // get the field node from the template and link it to this subTransFieldNode
                IMetaverseNode sourceStepFieldNode = getOutputs().findNode( inputStepName, inputFieldName );
                metaverseBuilder.addLink( sourceStepFieldNode, DictionaryConst.LINK_INPUTS, subTransFieldNode );
              }
            }
          }
        }

        // create "pseudo" step property nodes - these are ANNOTATIONS assigned to step properties
        final IMetaverseNode subTransStepNode = getNode( targetTemplateStepAttr.getStepname(),
          DictionaryConst.NODE_TYPE_TRANS_STEP, descriptor.getNamespace(),
          targetTemplateStepAttr.getStepname(), subTransSteps );

        final IMetaverseNode subTransPropertyNode = getNode( targetTemplateStepAttr.getAttributeKey(),
          DictionaryConst.NODE_TYPE_STEP_PROPERTY, (String) subTransStepNode.getProperty(
            DictionaryConst.PROPERTY_LOGICAL_ID ), targetTemplateStepAttr.getStepname() + ":"
            + targetTemplateStepAttr.getAttributeKey(), subTransSteps );

        metaverseBuilder.addLink( templateStepNode, DictionaryConst.LINK_CONTAINS, subTransPropertyNode );

        final String injectorStepName = sourceInjectorStepField.getStepname();
        final String injectotFieldName = sourceInjectorStepField.getField();
        final IMetaverseNode matchingInjectorFieldNode = getInputs().findNode( injectorStepName, injectotFieldName );
        if ( matchingInjectorFieldNode != null ) {
          // add 'populates' links back to the real ETL meta output fields
          metaverseBuilder.addLink( matchingInjectorFieldNode, DictionaryConst.LINK_POPULATES, subTransPropertyNode );
        }
      }
    }

    // if reading from a sub-transformation step directly, create virtual output fields from that virtual step, add
    // "input" links from those fields to the root node and "derives" links from those fields to the corresponding
    // output fields of the root node
    if ( StringUtils.isNotBlank( sourceStepName ) ) {
      // we created this node earlier, so we know it exists
      IMetaverseNode sourceStepNode = subTransSteps.get( sourceStepName );

      final Set<StepField> outputFields = getOutputs().getFieldNames();
      for ( final StepField outputField : outputFields ) {

        final IMetaverseNode subTransFieldNode = getNode( outputField.getFieldName(),
          DictionaryConst.NODE_TYPE_TRANS_FIELD, (String) sourceStepNode.getProperty(
            DictionaryConst.PROPERTY_LOGICAL_ID ), outputField.getStepName() + ":" + outputField.getFieldName(),
          subTransFields );
        metaverseBuilder.addLink( sourceStepNode, DictionaryConst.LINK_OUTPUTS, subTransFieldNode );
        metaverseBuilder.addLink( subTransFieldNode, DictionaryConst.LINK_INPUTS, rootNode );
        subTransFieldNode.setProperty( DictionaryConst.PROPERTY_CATEGORY, DictionaryConst.CATEGORY_FIELD );

        // get the field node from the template and link it to this subTransFieldNode
        final List<IMetaverseNode> outputFieldNodes = getOutputs().findNodes( outputField.getFieldName() );
        if ( outputFieldNodes.size() > 0 ) {
          final IMetaverseNode outputFieldNode = outputFieldNodes.get( 0 );
          metaverseBuilder.addLink( subTransFieldNode, DictionaryConst.LINK_DERIVES, outputFieldNode );
        }
      }
    }
    if ( !streaming ) {
      // used and unused mappings are considered "verbose" details
      rootNode.setProperty( DictionaryConst.PROPERTY_VERBOSE_DETAILS, "usedMappings,unusedMappings" );
      rootNode.setProperty( "usedMappings", usedMdiMappings.toString() );
      rootNode.setProperty( "unusedMappings", unusedMdiMappings.toString() );
    }
  }

  private IMetaverseNode getNode( final String name, final String type, final String namespaceId, final String nodeKey,
                                  final Map<String, IMetaverseNode> nodeMap ) {
    return getNode( name, type, new Namespace( namespaceId ), nodeKey, nodeMap );
  }

  private IMetaverseNode getNode( final String name, final String type, final INamespace namespace,
                                  final String nodeKey, final Map<String, IMetaverseNode> nodeMap ) {
    IMetaverseNode node = nodeMap == null ? null : nodeMap.get( nodeKey );
    if ( node == null ) {
      node = createNode( name, type, namespace );
      if ( nodeMap != null ) {
        nodeMap.put( nodeKey, node );
      }
    }
    return node;
  }

  private IMetaverseNode createNode( final String name, final String type, final INamespace namespace ) {
    final IComponentDescriptor descriptor = new MetaverseComponentDescriptor( name, type, namespace );
    final IMetaverseNode node = createNodeFromDescriptor( descriptor );
    node.setProperty( DictionaryConst.NODE_VIRTUAL, true );
    return node;
  }

  @Override public IClonableStepAnalyzer cloneAnalyzer() {
    return new MetaInjectAnalyzer();
  }

}
