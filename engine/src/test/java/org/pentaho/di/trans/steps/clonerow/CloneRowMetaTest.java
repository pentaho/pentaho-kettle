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

package org.pentaho.di.trans.steps.clonerow;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.trans.steps.loadsave.LoadSaveTester;

public class CloneRowMetaTest {

  @Test
  public void testRoundTrip() throws KettleException {
    List<String> attributes = Arrays.asList(
      "nrclones",
      "addcloneflag",
      "cloneflagfield",
      "nrcloneinfield",
      "nrclonefield",
      "addclonenum",
      "clonenumfield" );

    Map<String, String> getterMap = new HashMap<String, String>();
    getterMap.put( "nrclones", "getNrClones" );
    getterMap.put( "addcloneflag", "isAddCloneFlag" );
    getterMap.put( "cloneflagfield", "getCloneFlagField" );
    getterMap.put( "nrcloneinfield", "isNrCloneInField" );
    getterMap.put( "nrclonefield", "getNrCloneField" );
    getterMap.put( "addclonenum", "isAddCloneNum" );
    getterMap.put( "clonenumfield", "getCloneNumField" );

    Map<String, String> setterMap = new HashMap<String, String>();
    setterMap.put( "nrclones", "setNrClones" );
    setterMap.put( "addcloneflag", "setAddCloneFlag" );
    setterMap.put( "cloneflagfield", "setCloneFlagField" );
    setterMap.put( "nrcloneinfield", "setNrCloneInField" );
    setterMap.put( "nrclonefield", "setNrCloneField" );
    setterMap.put( "addclonenum", "setAddCloneNum" );
    setterMap.put( "clonenumfield", "setCloneNumField" );

    LoadSaveTester loadSaveTester = new LoadSaveTester( CloneRowMeta.class, attributes, getterMap, setterMap );
    loadSaveTester.testSerialization();
  }
}
