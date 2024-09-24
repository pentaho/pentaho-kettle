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

package org.pentaho.di.www;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.pentaho.di.base.LoadSaveBase;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.trans.steps.loadsave.validator.FieldLoadSaveValidator;

public class SlaveServerJobStatusLoadSaveTester extends LoadSaveBase<SlaveServerJobStatus> {

  public SlaveServerJobStatusLoadSaveTester( Class<SlaveServerJobStatus> clazz, List<String> commonAttributes ) {
    super( clazz, commonAttributes );
  }

  public SlaveServerJobStatusLoadSaveTester( Class<SlaveServerJobStatus> clazz, List<String> commonAttributes,
      Map<String, FieldLoadSaveValidator<?>> fieldLoadSaveValidatorAttributeMap ) {
    super( clazz, commonAttributes, new ArrayList<String>(), new ArrayList<String>(), new HashMap<String, String>(),
      new HashMap<String, String>(), fieldLoadSaveValidatorAttributeMap,
      new HashMap<String, FieldLoadSaveValidator<?>>() );
  }

  public void testSerialization() throws KettleException {
    testXmlRoundTrip();
  }

  protected void testXmlRoundTrip() throws KettleException {
    SlaveServerJobStatus metaToSave = createMeta();
    Map<String, FieldLoadSaveValidator<?>> validatorMap =
        createValidatorMapAndInvokeSetters( xmlAttributes, metaToSave );

    String xml = metaToSave.getXML();
    SlaveServerJobStatus metaLoaded = SlaveServerJobStatus.fromXML( xml );
    validateLoadedMeta( xmlAttributes, validatorMap, metaToSave, metaLoaded );
  }
}
