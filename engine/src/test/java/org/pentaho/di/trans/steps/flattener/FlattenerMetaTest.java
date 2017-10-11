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

package org.pentaho.di.trans.steps.flattener;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.trans.steps.loadsave.LoadSaveTester;

public class FlattenerMetaTest {
  @Test
  public void testRoundTrip() throws KettleException {
    List<String> attributes = Arrays.asList( "field_name", "target" );

    Map<String, String> getterMap = new HashMap<String, String>();
    getterMap.put( "field_name", "getFieldName" );
    getterMap.put( "target", "getTargetField" );

    Map<String, String> setterMap = new HashMap<String, String>();
    setterMap.put( "field_name", "setFieldName" );
    setterMap.put( "target", "setTargetField" );

    LoadSaveTester loadSaveTester = new LoadSaveTester( FlattenerMeta.class, attributes, getterMap, setterMap );

    loadSaveTester.testSerialization();
  }
}
