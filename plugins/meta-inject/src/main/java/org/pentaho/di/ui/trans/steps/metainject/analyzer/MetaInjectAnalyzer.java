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

import com.tinkerpop.blueprints.Vertex;
import org.apache.commons.lang.StringUtils;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.util.StringUtil;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.steps.metainject.MetaInjectMeta;
import org.pentaho.di.trans.steps.metainject.SourceStepField;
import org.pentaho.di.trans.steps.metainject.TargetStepAttribute;
import org.pentaho.dictionary.DictionaryConst;
import org.pentaho.metaverse.api.IMetaverseNode;
import org.pentaho.metaverse.api.MetaverseAnalyzerException;
import org.pentaho.metaverse.api.StepField;
import org.pentaho.metaverse.api.analyzer.kettle.KettleAnalyzerUtil;
import org.pentaho.metaverse.api.analyzer.kettle.step.IClonableStepAnalyzer;
import org.pentaho.metaverse.api.analyzer.kettle.step.StepAnalyzer;

import java.util.ArrayList;
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

    final Iterator<Map.Entry<TargetStepAttribute, SourceStepField>> fieldMappingsIter
      = meta.getTargetSourceMapping().entrySet().iterator();
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

    KettleAnalyzerUtil.analyze( this, parentTransMeta, meta, rootNode );
  }

  private List<String> getOutputFieldNames( final TransMeta transMeta, final String stepName ) {
    final Map<String, RowMetaInterface> targetFieldsMap = getOutputRowMetaInterfaces( transMeta,
      transMeta.findStep( stepName ), null, false );
    final List<String> targetFieldNames = new ArrayList();
    if ( targetFieldsMap != null ) {
      final List<ValueMetaInterface> fieldValues = targetFieldsMap.values().iterator().next().getValueMetaList();
      for ( final ValueMetaInterface fieldValue : fieldValues ) {
        targetFieldNames.add( fieldValue.getName() );
      }
    }
    return targetFieldNames;
  }

  @Override
  public void postAnalyze( final MetaInjectMeta meta )
    throws MetaverseAnalyzerException {

    final String transformationPath = parentTransMeta.environmentSubstitute( meta.getFileName() );

    final TransMeta subTransMeta = KettleAnalyzerUtil.getSubTransMeta( meta );
    subTransMeta.setFilename( transformationPath );

    // get the vertex corresponding to this step
    final Vertex stepVertex = findStepVertex( parentTransMeta, parentStepMeta.getName() );

    // are we streaming data directly from an injector step to a template step?
    if ( !StringUtil.isEmpty( meta.getStreamSourceStepname() )
      && !StringUtil.isEmpty( meta.getStreamTargetStepname() ) ) {
      // get the field names flowing from the stream source step into the template ktr's streaming target
      // step directly and the output fields of the streamTargetStepVertex, and create "derives" links between the
      // pairs at each index - we look at the outputRowMeta rather than just finding the vertices in the graph,
      // because we need to preserve field order, and the gtraph might give us the field Vertices out of order;
      // the returned maps might contain multiple key-value pairs for multiple steps, but the values in all should be
      // the same, so we can graph the first value we encounter
      final List<String> sourceFieldNames = getOutputFieldNames( parentTransMeta, meta.getStreamSourceStepname() );
      final List<String> targetFieldNames = getOutputFieldNames( subTransMeta, meta.getStreamTargetStepname() );

      int index = 0;
      for ( final String sourceFieldName : sourceFieldNames ) {

        final Vertex streamSourceStepOutputField = findFieldVertex( parentTransMeta, meta.getStreamSourceStepname(),
          sourceFieldName );
        // get the target field at the same index, if it exists
        if ( index < targetFieldNames.size() ) {
          final Vertex streamTargetStepOutputField = findFieldVertex( subTransMeta, meta.getStreamTargetStepname(),
            targetFieldNames.get( index++ ) );
          getMetaverseBuilder().addLink( streamSourceStepOutputField, DictionaryConst.LINK_DERIVES,
            streamTargetStepOutputField );
        } else {
          // we have mapped all the source fields we can, there are no more target steps to map to
          break;
        }
      }
    }

    final String sourceStepName = parentTransMeta.environmentSubstitute( meta.getSourceStepName() );
    final Iterator<Map.Entry<TargetStepAttribute, SourceStepField>> fieldMappingsIter
      = meta.getTargetSourceMapping().entrySet().iterator();

    final List<String> verboseProperties = new ArrayList();
    int mappingCount = 1;
    int ignoredMappingCount = 1;
    // process the injection mappings
    while ( fieldMappingsIter.hasNext() ) {
      final Map.Entry<TargetStepAttribute, SourceStepField> entry = fieldMappingsIter.next();
      final TargetStepAttribute targetTemplateStepAttr = entry.getKey();
      final SourceStepField sourceInjectorStepField = entry.getValue();
      final String targetTemplateStepName = targetTemplateStepAttr.getStepname();

      // if the source step is set to stream data directly to a step in the template (target) transformation, the
      // mappings are ignored, and instead, data is sent directly
      final boolean ignoreMapping = sourceInjectorStepField.getStepname().equalsIgnoreCase(
        meta.getStreamSourceStepname() );
      String mappingKey;
      if ( !ignoreMapping ) {
        mappingKey = "mapping [" + mappingCount++ + "]";
        // if the target template step name is the same as the step we read from (sourceStepName), we want to get the
        // output fields from the target template step and pass them back (input) into the parent injector step
        if ( targetTemplateStepName.equalsIgnoreCase( sourceStepName ) ) {

          final List<Vertex> targetTemplateFields = findFieldVertices( subTransMeta, targetTemplateStepName );
          for ( final Vertex targetTemplateField : targetTemplateFields ) {
            getMetaverseBuilder().addLink( targetTemplateField, DictionaryConst.LINK_INPUTS, stepVertex );
          }
        }
        // create "pseudo" step property nodes - these are ANNOTATIONS assigned to step properties
        // Note, since the sub-transformation has already been analysed, this node will already exist, and therefore
        // we need to fetch the Vertex directly, as we currenlty have no way ot finding nodes in the graph
        final Vertex targetTemplateStepVertex = findStepVertex( subTransMeta, targetTemplateStepName );

        final IMetaverseNode subTransPropertyNode = getNode( targetTemplateStepAttr.getAttributeKey(),
          DictionaryConst.NODE_TYPE_STEP_PROPERTY, (String) targetTemplateStepVertex.getProperty(
            DictionaryConst.PROPERTY_LOGICAL_ID ), targetTemplateStepName + ":"
            + targetTemplateStepAttr.getAttributeKey(), null );
        getMetaverseBuilder().addNode( subTransPropertyNode );

        // now that the property node has been added, find the corresponding vertex to add the "contains" link from
        // the target template step
        final Vertex subTransPropertyVertex = findVertexById( subTransPropertyNode.getStringID() );
        if ( subTransPropertyVertex != null ) {
          getMetaverseBuilder().addLink( targetTemplateStepVertex, DictionaryConst.LINK_CONTAINS, subTransPropertyVertex );
        }

        final String injectorStepName = sourceInjectorStepField.getStepname();
        final String injectotFieldName = sourceInjectorStepField.getField();
        final IMetaverseNode matchingInjectorFieldNode = getInputs().findNode( injectorStepName, injectotFieldName );
        if ( matchingInjectorFieldNode != null ) {
          // add 'populates' links back to the real ETL meta output fields
          getMetaverseBuilder().addLink( matchingInjectorFieldNode, DictionaryConst.LINK_POPULATES, subTransPropertyNode );
        }
      } else {
        mappingKey = "ignored mapping [" + ignoredMappingCount++ + "]";
      }
      final StringBuilder mapping = new StringBuilder();
      mapping.append( sourceInjectorStepField.getStepname() ).append( ": " )
        .append( sourceInjectorStepField.getField() ).append( " > [" ).append( subTransMeta.getName() ).append( "] " )
        .append( targetTemplateStepName ).append( ": " ).append( targetTemplateStepAttr.getAttributeKey() );
      verboseProperties.add( mappingKey );
      stepVertex.setProperty( mappingKey, mapping.toString() );
    }

    // if reading from a sub-transformation step directly, add "input" links from the source step fields to the root
    // node and  "derives" links from those fields to the corresponding output fields of the root node
    if ( StringUtils.isNotBlank( sourceStepName ) ) {
      // we created this node earlier, so we know it exists
      final List<Vertex> sourceStepFields = findFieldVertices( subTransMeta, sourceStepName );
      for ( final Vertex sourceStepField : sourceStepFields ) {
        getMetaverseBuilder().addLink( sourceStepField, DictionaryConst.LINK_INPUTS, stepVertex );
        // find a field in this step with the same name as the source step field
        final Vertex derivedField = findFieldVertex( parentTransMeta, stepVertex.getProperty(
          DictionaryConst.PROPERTY_NAME ).toString(),
          sourceStepField.getProperty( DictionaryConst.PROPERTY_NAME ).toString() );
        if ( derivedField != null ) {
          getMetaverseBuilder().addLink( sourceStepField, DictionaryConst.LINK_DERIVES, derivedField );
        }
      }
    }
    stepVertex.setProperty( DictionaryConst.PROPERTY_VERBOSE_DETAILS, StringUtils.join( verboseProperties, "," ) );
  }

  @Override protected IClonableStepAnalyzer newInstance() {
    return new MetaInjectAnalyzer();
  }

}
