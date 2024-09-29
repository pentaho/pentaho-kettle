/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/


package org.pentaho.di.trans.steps.jsoninput.analyzer;

import org.pentaho.di.trans.steps.jsoninput.JsonInput;
import org.pentaho.di.trans.steps.jsoninput.JsonInputMeta;
import org.pentaho.metaverse.api.analyzer.kettle.step.BaseStepExternalResourceConsumer;

public class JsonInputExternalResourceConsumer extends BaseStepExternalResourceConsumer<JsonInput, JsonInputMeta> {

  @Override
  public Class<JsonInputMeta> getMetaClass() {
    return JsonInputMeta.class;
  }

  @Override
  public boolean isDataDriven( final JsonInputMeta meta ) {
    return meta.isAcceptingFilenames();
  }
}
