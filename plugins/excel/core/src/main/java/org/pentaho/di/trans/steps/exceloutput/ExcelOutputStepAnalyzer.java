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

package org.pentaho.di.trans.steps.exceloutput;

import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.steps.exceloutput.ExcelField;
import org.pentaho.di.trans.steps.exceloutput.ExcelOutputMeta;
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
 * The TextFileOutputStepAnalyzer is responsible for providing nodes and links (i.e. relationships) between for the
 * fields operated on by Text File Output steps.
 */
public class ExcelOutputStepAnalyzer extends ExternalResourceStepAnalyzer<ExcelOutputMeta> {

  private Logger log = LoggerFactory.getLogger( ExcelOutputStepAnalyzer.class );

  @Override
  public IMetaverseNode createResourceNode( IExternalResourceInfo resource ) throws MetaverseException {
    return createFileNode( resource.getName(), descriptor );
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
  public Set<Class<? extends BaseStepMeta>> getSupportedSteps() {
    return new HashSet<Class<? extends BaseStepMeta>>() {
      {
        add( ExcelOutputMeta.class );
      }
    };
  }

  @Override
  protected Set<StepField> getUsedFields( ExcelOutputMeta meta ) {
    return null;
  }

  @Override
  public Set<String> getOutputResourceFields( ExcelOutputMeta meta ) {
    Set<String> fields = new HashSet<>();
    ExcelField[] outputFields = meta.getOutputFields();
    for ( int i = 0; i < outputFields.length; i++ ) {
      ExcelField outputField = outputFields[ i ];
      fields.add( outputField.getName() );
    }
    return fields;
  }

  // used for unit testing
  protected void setObjectFactory( IMetaverseObjectFactory factory ) {
    this.metaverseObjectFactory = factory;
  }

  @Override protected IClonableStepAnalyzer newInstance() {
    return new ExcelOutputStepAnalyzer();
  }

  @Override public String toString() {
    return this.getClass().getName();
  }
}
