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

package org.pentaho.di.trans.steps.sort;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.trans.steps.loadsave.LoadSaveTester;
import org.pentaho.di.trans.steps.loadsave.validator.ArrayLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.BooleanLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.FieldLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.PrimitiveBooleanArrayLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.StringLoadSaveValidator;

public class SortRowsMetaUnit {

  /**
   * Replaced previous testRep with load/save tester. Should cover http://jira.pentaho.com/browse/BACKLOG-377
   * @throws KettleException
   */
  @Test
  public void testRoundTrips() throws KettleException {
    List<String> attributes = Arrays.asList( "directory", "prefix", "sort_size", "free_memory", "compress",
      "compress_variable", "unique_rows", "name", "ascending", "case_sensitive", "presorted" );

    Map<String, String> getterMap = new HashMap<String, String>();
    getterMap.put( "sort_size", "getSortSize" );
    getterMap.put( "free_memory", "getFreeMemoryLimit" );
    getterMap.put( "compress", "getCompressFiles" );
    getterMap.put( "compress_variable", "getCompressFilesVariable" );
    getterMap.put( "unique_rows", "isOnlyPassingUniqueRows" );
    getterMap.put( "name", "getFieldName" );
    getterMap.put( "case_sensitive", "getCaseSensitive" );
    getterMap.put( "presorted", "getPreSortedField" );

    Map<String, String> setterMap = new HashMap<String, String>();
    setterMap.put( "sort_size", "setSortSize" );
    setterMap.put( "free_memory", "setFreeMemoryLimit" );
    setterMap.put( "compress", "setCompressFiles" );
    setterMap.put( "compress_variable", "setCompressFilesVariable" );
    setterMap.put( "name", "setFieldName" );
    setterMap.put( "case_sensitive", "setCaseSensitive" );
    setterMap.put( "presorted", "setPreSortedField" );
    setterMap.put( "unique_rows", "setOnlyPassingUniqueRows" );

    Map<String, FieldLoadSaveValidator<?>> fieldLoadSaveValidatorAttributeMap =
      new HashMap<String, FieldLoadSaveValidator<?>>();
    FieldLoadSaveValidator<String[]> stringArrayLoadSaveValidator =
      new ArrayLoadSaveValidator<String>( new StringLoadSaveValidator(), 25 );
    FieldLoadSaveValidator<boolean[]> booleanArrayLoadSaveValidator =
      new PrimitiveBooleanArrayLoadSaveValidator( new BooleanLoadSaveValidator(), 25 );

    fieldLoadSaveValidatorAttributeMap.put( "name", stringArrayLoadSaveValidator );
    fieldLoadSaveValidatorAttributeMap.put( "ascending", booleanArrayLoadSaveValidator );
    fieldLoadSaveValidatorAttributeMap.put( "case_sensitive", booleanArrayLoadSaveValidator );
    fieldLoadSaveValidatorAttributeMap.put( "presorted", booleanArrayLoadSaveValidator );

    LoadSaveTester loadSaveTester =
      new LoadSaveTester( SortRowsMeta.class, attributes, getterMap, setterMap,
        fieldLoadSaveValidatorAttributeMap, new HashMap<String, FieldLoadSaveValidator<?>>() );

    loadSaveTester.testSerialization();
  }
}
