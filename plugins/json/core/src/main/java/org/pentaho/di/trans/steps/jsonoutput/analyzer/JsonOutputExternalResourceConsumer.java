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

import org.pentaho.di.trans.steps.jsonoutput.JsonOutput;
import org.pentaho.di.trans.steps.jsonoutput.JsonOutputMeta;
import org.pentaho.metaverse.api.analyzer.kettle.step.BaseStepExternalResourceConsumer;

public class JsonOutputExternalResourceConsumer extends BaseStepExternalResourceConsumer<JsonOutput, JsonOutputMeta> {

  @Override
  public Class<JsonOutputMeta> getMetaClass() {
    return JsonOutputMeta.class;
  }

  @Override
  public boolean isDataDriven( final JsonOutputMeta meta ) {
    return false;
  }
}
