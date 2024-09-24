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

package org.pentaho.di.trans.steps.jsonoutput.analyzer;

import org.apache.commons.lang.StringUtils;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.steps.jsonoutput.JsonOutputMeta;
import org.pentaho.dictionary.DictionaryConst;
import org.pentaho.metaverse.api.IMetaverseNode;
import org.pentaho.metaverse.api.MetaverseAnalyzerException;
import org.pentaho.metaverse.api.MetaverseException;
import org.pentaho.metaverse.api.StepField;
import org.pentaho.metaverse.api.analyzer.kettle.step.ExternalResourceStepAnalyzer;
import org.pentaho.metaverse.api.analyzer.kettle.step.IClonableStepAnalyzer;
import org.pentaho.metaverse.api.model.IExternalResourceInfo;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * The JsonOutputAnalyzer is responsible for providing nodes and links (i.e. relationships) between itself and other
 * metaverse entities.
 */
public class JsonOutputAnalyzer extends ExternalResourceStepAnalyzer<JsonOutputMeta> {

  @Override
  public Set<Class<? extends BaseStepMeta>> getSupportedSteps() {
    final Set<Class<? extends BaseStepMeta>> supportedSteps = new HashSet<>();
    supportedSteps.add( JsonOutputMeta.class );
    return supportedSteps;
  }

  @Override
  public String getResourceInputNodeType() {
    return null;
  }

  @Override
  public String getResourceOutputNodeType() {
    return DictionaryConst.NODE_TYPE_FILE_FIELD;
  }

  @Override
  public boolean isOutput() {
    return true;
  }

  @Override
  public boolean isInput() {
    return false;
  }

  @Override
  protected Set<StepField> getUsedFields( final JsonOutputMeta meta ) {
    return Collections.emptySet();
  }

  @Override
  public IMetaverseNode createResourceNode( final IExternalResourceInfo resource ) throws MetaverseException {
    return createFileNode( resource.getName(), descriptor, DictionaryConst.NODE_TYPE_FILE );
  }

  @Override
  protected void customAnalyze( final JsonOutputMeta meta, final IMetaverseNode rootNode )
    throws MetaverseAnalyzerException {
    super.customAnalyze( meta, rootNode );
    rootNode.setProperty( "isFileAppended", meta.isFileAppended() );
    rootNode.setProperty( "passDataToServletOutput", meta.passDataToServletOutput() );
    rootNode.setProperty( "addToResult", meta.AddToResult() );
    rootNode.setProperty( "jsonBloc", meta.getJsonBloc() );
    rootNode.setProperty( "operationType", meta.getOperationType() );
    if ( !StringUtils.isBlank( meta.getOutputValue() ) ) {
      rootNode.setProperty( "outputValue", meta.getOutputValue() );
    }
  }

  @Override
  public boolean hasOutputResource( final JsonOutputMeta meta ) {
    return meta.writesToFile()
      && ( meta.getOperationType() == JsonOutputMeta.OPERATION_TYPE_WRITE_TO_FILE
      || meta.getOperationType() == JsonOutputMeta.OPERATION_TYPE_BOTH );
  }

  @Override
  public IClonableStepAnalyzer newInstance() {
    return new JsonOutputAnalyzer();
  }
}
