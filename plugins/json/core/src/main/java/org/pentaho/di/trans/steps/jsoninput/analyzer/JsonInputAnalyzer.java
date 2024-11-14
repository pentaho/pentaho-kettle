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
