/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2015 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.execprocess;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.trans.steps.loadsave.LoadSaveTester;

public class ExecProcessMetaTest {
  @Test
  public void testRoundTrip() throws KettleException {
    List<String> attributes =
      Arrays.asList( "processfield", "resultfieldname", "errorfieldname", "exitvaluefieldname",
        "failwhennotsuccess", "outputlinedelimiter" );

    Map<String, String> getterMap = new HashMap<String, String>();
    getterMap.put( "processfield", "getProcessField" );
    getterMap.put( "resultfieldname", "getResultFieldName" );
    getterMap.put( "errorfieldname", "getErrorFieldName" );
    getterMap.put( "exitvaluefieldname", "getExitValueFieldName" );
    getterMap.put( "failwhennotsuccess", "isFailWhenNotSuccess" );
    getterMap.put( "outputlinedelimiter", "getOutputLineDelimiter" );

    Map<String, String> setterMap = new HashMap<String, String>();
    setterMap.put( "processfield", "setProcessField" );
    setterMap.put( "resultfieldname", "setResultFieldName" );
    setterMap.put( "errorfieldname", "setErrorFieldName" );
    setterMap.put( "exitvaluefieldname", "setExitValueFieldName" );
    setterMap.put( "failwhennotsuccess", "setFailWhentNoSuccess" );
    setterMap.put( "outputlinedelimiter", "setOutputLineDelimiter" );

    LoadSaveTester loadSaveTester = new LoadSaveTester( ExecProcessMeta.class, attributes, getterMap, setterMap );

    loadSaveTester.testRepoRoundTrip();
    loadSaveTester.testXmlRoundTrip();
  }
}
