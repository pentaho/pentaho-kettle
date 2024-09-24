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

package org.pentaho.di.trans.steps.jsoninput.analyzer;

import org.apache.commons.lang.StringUtils;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.steps.jsoninput.JsonInputMeta;
import org.pentaho.dictionary.DictionaryConst;
import org.pentaho.metaverse.api.IMetaverseNode;
import org.pentaho.metaverse.api.MetaverseAnalyzerException;
import org.pentaho.metaverse.api.MetaverseException;
import org.pentaho.metaverse.api.StepField;
import org.pentaho.metaverse.api.analyzer.kettle.step.ExternalResourceStepAnalyzer;
import org.pentaho.metaverse.api.analyzer.kettle.step.IClonableStepAnalyzer;
import org.pentaho.metaverse.api.model.IExternalResourceInfo;

import java.util.HashSet;
import java.util.Set;

/**
 * The JsonInputAnalyzer is responsible for providing nodes and links (i.e. relationships) between itself and other
 * metaverse entities.
 */
public class JsonInputAnalyzer extends ExternalResourceStepAnalyzer<JsonInputMeta> {

  @Override
  public Set<Class<? extends BaseStepMeta>> getSupportedSteps() {
    final Set<Class<? extends BaseStepMeta>> supportedSteps = new HashSet<>();
    supportedSteps.add( JsonInputMeta.class );
    return supportedSteps;
  }

  @Override
  public String getResourceInputNodeType() {
    return DictionaryConst.NODE_TYPE_FILE_FIELD;
  }

  @Override
  public String getResourceOutputNodeType() {
    return null;
  }

  @Override
  public boolean isOutput() {
    return false;
  }

  @Override
  public boolean isInput() {
    return true;
  }

  @Override
  protected Set<StepField> getUsedFields( final JsonInputMeta meta ) {
    Set<StepField> usedFields = new HashSet<>();
    if ( meta.isAcceptingFilenames() && StringUtils.isNotEmpty( meta.getAcceptingField() ) ) {
      final Set<String> inpusStepNames = getInputStepNames( meta, meta.getAcceptingField() );
      for ( final String inpusStepName : inpusStepNames ) {
        final StepField stepField = new StepField( inpusStepName, meta.getAcceptingField() );
        usedFields.add( stepField );
      }
    }
    return usedFields;
  }

  @Override
  public IMetaverseNode createResourceNode( final IExternalResourceInfo resource ) throws MetaverseException {
    return createFileNode( resource.getName(), descriptor, DictionaryConst.NODE_TYPE_FILE );
  }

  @Override
  protected void customAnalyze( final JsonInputMeta meta, final IMetaverseNode rootNode )
    throws MetaverseAnalyzerException {
    super.customAnalyze( meta, rootNode );
    if ( meta.isAcceptingFilenames() ) {
      rootNode.setProperty( "sourceField", meta.getAcceptingField() );
      rootNode.setProperty( "sourceFieldIsFile", meta.getIsAFile() );
      rootNode.setProperty( "sourceFieldIsUrl", meta.isReadUrl() );
      rootNode.setProperty( "removeSourceField", meta.isRemoveSourceField() );
    } else {
      rootNode.setProperty( "fileDirName", meta.getFilenameField() );
    }
  }

  @Override
  public IClonableStepAnalyzer newInstance() {
    return new JsonInputAnalyzer();
  }
}
