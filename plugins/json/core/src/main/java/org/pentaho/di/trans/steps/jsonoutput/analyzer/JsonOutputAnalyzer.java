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
