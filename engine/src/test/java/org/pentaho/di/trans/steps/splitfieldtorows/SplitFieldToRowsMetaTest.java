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

package org.pentaho.di.trans.steps.splitfieldtorows;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.trans.steps.loadsave.LoadSaveTester;
import org.pentaho.di.trans.steps.loadsave.validator.FieldLoadSaveValidator;

public class SplitFieldToRowsMetaTest {

  @BeforeClass
  public static void setUpBeforeClass() throws KettleException {
    KettleEnvironment.init( false );
  }

  @Test
  public void loadSaveTest() throws KettleException {
    List<String> attributes = Arrays.asList( "splitField", "delimiter", "newFieldname", "includeRowNumber",
      "rowNumberField", "resetRowNumber", "delimiterRegex" );

    Map<String, String> getterMap = new HashMap<String, String>();
    getterMap.put( "includeRowNumber", "includeRowNumber" );
    getterMap.put( "resetRowNumber", "resetRowNumber" );

    Map<String, String> setterMap = new HashMap<String, String>();

    LoadSaveTester loadSaveTester = new LoadSaveTester(
      SplitFieldToRowsMeta.class, attributes, getterMap, setterMap,
      new HashMap<String, FieldLoadSaveValidator<?>>(), new HashMap<String, FieldLoadSaveValidator<?>>() );
    loadSaveTester.testSerialization();
  }
}
