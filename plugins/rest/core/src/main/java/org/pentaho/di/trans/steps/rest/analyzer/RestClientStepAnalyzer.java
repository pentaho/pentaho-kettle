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


package org.pentaho.di.trans.steps.rest.analyzer;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.steps.rest.RestMeta;
import org.pentaho.dictionary.DictionaryConst;
import org.pentaho.metaverse.api.IMetaverseNode;
import org.pentaho.metaverse.api.IMetaverseObjectFactory;
import org.pentaho.metaverse.api.MetaverseComponentDescriptor;
import org.pentaho.metaverse.api.MetaverseException;
import org.pentaho.metaverse.api.StepField;
import org.pentaho.metaverse.api.analyzer.kettle.step.ExternalResourceStepAnalyzer;
import org.pentaho.metaverse.api.analyzer.kettle.step.IClonableStepAnalyzer;
import org.pentaho.metaverse.api.model.IExternalResourceInfo;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class RestClientStepAnalyzer extends ExternalResourceStepAnalyzer<RestMeta> {

  @Override
  protected Set<StepField> getUsedFields( RestMeta stepMeta ) {
    Set<StepField> usedFields = new HashSet<>();

    // add url field
    if ( stepMeta.isUrlInField() && StringUtils.isNotEmpty( stepMeta.getUrlField() ) ) {
      usedFields.addAll( createStepFields( stepMeta.getUrlField(), getInputs() ) );
    }

    // add method field
    if ( stepMeta.isDynamicMethod() && StringUtils.isNotEmpty( stepMeta.getMethodFieldName() ) ) {
      usedFields.addAll( createStepFields( stepMeta.getMethodFieldName(), getInputs() ) );
    }

    // add body field
    if ( StringUtils.isNotEmpty( stepMeta.getBodyField() ) ) {
      usedFields.addAll( createStepFields( stepMeta.getBodyField(), getInputs() ) );
    }

    // add parameters as used fields
    String[] parameterFields = stepMeta.getParameterField();
    if ( ArrayUtils.isNotEmpty( parameterFields ) ) {
      for ( String paramField : parameterFields ) {
        usedFields.addAll( createStepFields( paramField, getInputs() ) );
      }
    }

    // add headers as used fields
    String[] headerFields = stepMeta.getHeaderField();
    if ( ArrayUtils.isNotEmpty( headerFields ) ) {
      for ( String headerField : headerFields ) {
        usedFields.addAll( createStepFields( headerField, getInputs() ) );
      }
    }

    return usedFields;
  }

  @Override
  protected Map<String, RowMetaInterface> getInputRowMetaInterfaces( RestMeta meta ) {
    return getInputFields( meta );
  }
  @Override
  public Set<Class<? extends BaseStepMeta>> getSupportedSteps() {
    Set<Class<? extends BaseStepMeta>> supportedSteps = new HashSet<>();
    supportedSteps.add( RestMeta.class );
    return supportedSteps;
  }

  @Override public IMetaverseNode createResourceNode( IExternalResourceInfo resource ) throws MetaverseException {
    MetaverseComponentDescriptor componentDescriptor = new MetaverseComponentDescriptor(
      resource.getName(), getResourceInputNodeType(), descriptor.getNamespace(), descriptor.getContext()
    );
    IMetaverseNode node = createNodeFromDescriptor( componentDescriptor );
    return node;
  }

  @Override public String getResourceInputNodeType() {
    return DictionaryConst.NODE_TYPE_WEBSERVICE;
  }

  @Override public String getResourceOutputNodeType() {
    return null;
  }

  @Override public boolean isOutput() {
    return false;
  }

  @Override public boolean isInput() {
    return true;
  }

  @Override protected IClonableStepAnalyzer newInstance() {
    return new RestClientStepAnalyzer();
  }
  @Override public String toString() {
    return this.getClass().getName();
  }

  ////// used in unit testing
  protected void setObjectFactory( IMetaverseObjectFactory objectFactory ) {
    this.metaverseObjectFactory = objectFactory;
  }
}
