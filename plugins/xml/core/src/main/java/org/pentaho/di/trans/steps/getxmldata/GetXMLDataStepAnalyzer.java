/*
 * ******************************************************************************
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
 *
 * ******************************************************************************
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
 */

package org.pentaho.di.trans.steps.getxmldata;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.dictionary.DictionaryConst;
import org.pentaho.metaverse.api.IAnalysisContext;
import org.pentaho.metaverse.api.IMetaverseNode;
import org.pentaho.metaverse.api.IMetaverseObjectFactory;
import org.pentaho.metaverse.api.MetaverseAnalyzerException;
import org.pentaho.metaverse.api.MetaverseException;
import org.pentaho.metaverse.api.StepField;
import org.pentaho.metaverse.api.analyzer.kettle.ComponentDerivationRecord;
import org.pentaho.metaverse.api.analyzer.kettle.step.ExternalResourceStepAnalyzer;
import org.pentaho.metaverse.api.analyzer.kettle.step.IClonableStepAnalyzer;
import org.pentaho.metaverse.api.model.IExternalResourceInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * The GetXMLDataStepAnalyzer is responsible for providing nodes and links (i.e. relationships) between itself and
 * other metaverse entities
 */
public class GetXMLDataStepAnalyzer extends ExternalResourceStepAnalyzer<GetXMLDataMeta> {
  private Logger log = LoggerFactory.getLogger( GetXMLDataStepAnalyzer.class );

  @Override
  protected Set<StepField> getUsedFields( GetXMLDataMeta meta ) {
    Set<StepField> usedFields = new HashSet<>();
    if ( meta.isInFields() ) {
      Set<StepField> stepFields = createStepFields( meta.getXMLField(), getInputs() );
      usedFields.addAll( stepFields );
    }
    return usedFields;
  }

  @Override
  public Set<Class<? extends BaseStepMeta>> getSupportedSteps() {
    return new HashSet<Class<? extends BaseStepMeta>>() {
      {
        add( GetXMLDataMeta.class );
      }
    };
  }

  @Override
  protected IMetaverseNode createOutputFieldNode( IAnalysisContext context, ValueMetaInterface fieldMeta,
                                                  String targetStepName, String nodeType ) {
    IMetaverseNode fieldNode = super.createOutputFieldNode( context, fieldMeta, targetStepName, nodeType );
    GetXMLDataField[] fields = baseStepMeta.getInputFields();
    for ( GetXMLDataField field : fields ) {
      if ( fieldMeta.getName().equals( field.getName() ) ) {
        fieldNode.setProperty( "xpath", Const.NVL( field.getXPath(), "" ) );
        fieldNode.setProperty( "element", Const.NVL( field.getElementTypeCode(), "" ) );
        fieldNode.setProperty( "resultType", Const.NVL( field.getResultTypeCode(), "" ) );
        fieldNode.setProperty( "repeat", field.isRepeated() );
        break;
      }
    }
    return fieldNode;
  }

  @Override
  protected Map<String, RowMetaInterface> getInputRowMetaInterfaces( GetXMLDataMeta meta ) {
    Map<String, RowMetaInterface> inputRows = getInputFields( meta );
    if ( inputRows == null ) {
      inputRows = new HashMap<>();
    }
    // Get some boolean flags from the meta for easier access
    boolean isInFields = meta.isInFields();
    boolean isAFile = meta.getIsAFile();
    boolean isAUrl = meta.isReadUrl();

    // only add resource fields if we are NOT getting the xml or file from a field
    if ( !isInFields || isAFile || isAUrl ) {
      RowMetaInterface stepFields = getOutputFields( meta );
      RowMetaInterface clone = stepFields.clone();
      // if there are previous steps providing data, we should remove them from the set of "resource" fields
      for ( RowMetaInterface rowMetaInterface : inputRows.values() ) {
        for ( ValueMetaInterface valueMetaInterface : rowMetaInterface.getValueMetaList() ) {
          try {
            clone.removeValueMeta( valueMetaInterface.getName() );
          } catch ( KettleValueException e ) {
            // could not find it in the output, skip it
          }
        }
      }
      inputRows.put( RESOURCE, clone );
    }
    return inputRows;
  }

  @Override
  public Set<ComponentDerivationRecord> getChangeRecords( GetXMLDataMeta meta )
    throws MetaverseAnalyzerException {
    Set<ComponentDerivationRecord> changes = new HashSet<>();

    boolean isInFields = meta.isInFields();
    boolean isAFile = meta.getIsAFile();
    boolean isAUrl = meta.isReadUrl();

    // if we are getting xml from a field, we need to add the "derives" links from the xml to the output fields
    if ( isInFields && !isAFile && !isAUrl ) {
      GetXMLDataField[] fields = baseStepMeta.getInputFields();
      if ( getInputs() != null ) {
        Set<StepField> inputFields = getInputs().getFieldNames();

        for ( StepField inputField : inputFields ) {
          if ( inputField.getFieldName().equals( meta.getXMLField() ) ) {
            // link this to all of the outputs that come from the xml
            for ( GetXMLDataField field : fields ) {
              ComponentDerivationRecord change = new ComponentDerivationRecord( meta.getXMLField(), field.getName() );
              changes.add( change );
            }
            break;
          }
        }
      }
    }
    return changes;
  }

  @Override
  protected void customAnalyze( GetXMLDataMeta meta, IMetaverseNode node ) throws MetaverseAnalyzerException {
    super.customAnalyze( meta, node );
    // Add the XPath Loop to the step node
    node.setProperty( "loopXPath", meta.getLoopXPath() );
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
    return new GetXMLDataStepAnalyzer();
  }

  ///// used for unit testing
  protected void setObjectFactory( IMetaverseObjectFactory factory ) {
    this.metaverseObjectFactory = factory;
  }
  protected void setRootNode( IMetaverseNode node ) {
    rootNode = node;
  }
  protected void setBaseStepMeta( GetXMLDataMeta meta ) {
    baseStepMeta = meta;
  }
  protected void setParentTransMeta( TransMeta tm ) {
    parentTransMeta = tm;
  }
  protected void setParentStepMeta( StepMeta sm ) {
    parentStepMeta = sm;
  }
  ///// used for unit testing

}
