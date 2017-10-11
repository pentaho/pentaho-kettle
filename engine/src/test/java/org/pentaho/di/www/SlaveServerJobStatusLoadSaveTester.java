/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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
