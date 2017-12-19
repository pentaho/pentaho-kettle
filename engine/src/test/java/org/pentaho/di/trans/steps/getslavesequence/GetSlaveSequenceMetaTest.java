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

package org.pentaho.di.trans.steps.getslavesequence;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.trans.steps.loadsave.LoadSaveTester;

public class GetSlaveSequenceMetaTest {

  @Test
  public void testRoundTrip() throws KettleException {
    List<String> attributes =
        Arrays.asList( "valuename", "slave", "seqname", "increment" );
    Map<String, String> getterMap = new HashMap<String, String>();
    getterMap.put( "valuename", "getValuename" );
    getterMap.put( "slave", "getSlaveServerName" );
    getterMap.put( "seqname", "getSequenceName" );
    getterMap.put( "increment", "getIncrement" );

    Map<String, String> setterMap = new HashMap<String, String>();
    setterMap.put( "valuename", "setValuename" );
    setterMap.put( "slave", "setSlaveServerName" );
    setterMap.put( "seqname", "setSequenceName" );
    setterMap.put( "increment", "setIncrement" );

    LoadSaveTester tester = new LoadSaveTester( GetSlaveSequenceMeta.class, attributes, getterMap, setterMap );

    tester.testSerialization();
  }
}
