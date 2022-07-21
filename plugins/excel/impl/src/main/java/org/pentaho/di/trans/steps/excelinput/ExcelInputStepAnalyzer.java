/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2022 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.excelinput;

import org.apache.commons.lang.StringUtils;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.dictionary.DictionaryConst;
import org.pentaho.metaverse.api.IMetaverseNode;
import org.pentaho.metaverse.api.IMetaverseObjectFactory;
import org.pentaho.metaverse.api.MetaverseException;
import org.pentaho.metaverse.api.StepField;
import org.pentaho.metaverse.api.analyzer.kettle.step.ExternalResourceStepAnalyzer;
import org.pentaho.metaverse.api.analyzer.kettle.step.IClonableStepAnalyzer;
import org.pentaho.metaverse.api.model.IExternalResourceInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

/**
 * The ExcelInputStepAnalyzer is responsible for providing nodes and links (i.e. relationships) between itself and other
 * metaverse entities
 */
public class ExcelInputStepAnalyzer extends ExternalResourceStepAnalyzer<ExcelInputMeta> {
  private Logger log = LoggerFactory.getLogger( ExcelInputStepAnalyzer.class );

  @Override
  public Set<Class<? extends BaseStepMeta>> getSupportedSteps() {
    return new HashSet<Class<? extends BaseStepMeta>>() {
      {
        add( ExcelInputMeta.class );
      }
    };
  }

  @Override
  protected Set<StepField> getUsedFields( ExcelInputMeta meta ) {
    Set<StepField> usedFields = new HashSet<>();
    if ( meta.isAcceptingFilenames() && StringUtils.isNotEmpty( meta.getAcceptingStepName() ) ) {
      StepField stepField = new StepField( meta.getAcceptingStepName(), meta.getAcceptingField() );
      usedFields.add( stepField );
    }
    return usedFields;
  }

  @Override
  public IMetaverseNode createResourceNode( IExternalResourceInfo resource ) throws MetaverseException {
    return createFileNode( resource.getName(), descriptor );
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
  public IClonableStepAnalyzer newInstance() {
    return new ExcelInputStepAnalyzer();
  }

  // used for unit testing
  protected void setObjectFactory( IMetaverseObjectFactory factory ) {
    this.metaverseObjectFactory = factory;
  }
}
