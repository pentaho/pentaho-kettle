/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2016 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.abort;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.trans.steps.loadsave.LoadSaveTester;

public class AbortMetaTest {

  @Test
  public void testRoundTrip() throws KettleException {
    List<String> attributes = Arrays.asList( "row_threshold", "message", "always_log_rows" );

    Map<String, String> getterMap = new HashMap<String, String>();
    getterMap.put( "row_threshold", "getRowThreshold" );
    getterMap.put( "message", "getMessage" );
    getterMap.put( "always_log_rows", "isAlwaysLogRows" );

    Map<String, String> setterMap = new HashMap<String, String>();
    setterMap.put( "row_threshold", "setRowThreshold" );
    setterMap.put( "message", "setMessage" );
    setterMap.put( "always_log_rows", "setAlwaysLogRows" );

    LoadSaveTester loadSaveTester = new LoadSaveTester( AbortMeta.class, attributes, getterMap, setterMap );
    loadSaveTester.testSerialization();
  }
}
