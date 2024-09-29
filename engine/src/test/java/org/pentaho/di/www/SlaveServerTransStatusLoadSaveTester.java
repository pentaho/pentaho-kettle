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

public class SlaveServerTransStatusLoadSaveTester extends LoadSaveBase<SlaveServerTransStatus> {

  public SlaveServerTransStatusLoadSaveTester( Class<SlaveServerTransStatus> clazz, List<String> commonAttributes ) {
    super( clazz, commonAttributes );
  }

  public SlaveServerTransStatusLoadSaveTester( Class<SlaveServerTransStatus> clazz, List<String> commonAttributes,
      Map<String, FieldLoadSaveValidator<?>> fieldLoadSaveValidatorAttributeMap ) {
    super( clazz, commonAttributes, new ArrayList<String>(), new ArrayList<String>(), new HashMap<String, String>(),
      new HashMap<String, String>(), fieldLoadSaveValidatorAttributeMap,
      new HashMap<String, FieldLoadSaveValidator<?>>() );
  }

  public void testSerialization() throws KettleException {
    testXmlRoundTrip();
  }

  protected void testXmlRoundTrip() throws KettleException {
    SlaveServerTransStatus metaToSave = createMeta();
    Map<String, FieldLoadSaveValidator<?>> validatorMap =
        createValidatorMapAndInvokeSetters( xmlAttributes, metaToSave );

    String xml = metaToSave.getXML();
    SlaveServerTransStatus metaLoaded = SlaveServerTransStatus.fromXML( xml );
    validateLoadedMeta( xmlAttributes, validatorMap, metaToSave, metaLoaded );
  }
}
