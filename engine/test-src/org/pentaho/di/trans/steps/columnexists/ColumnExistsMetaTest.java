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
package org.pentaho.di.trans.steps.columnexists;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.trans.steps.loadsave.LoadSaveTester;
import org.pentaho.di.trans.steps.loadsave.validator.DatabaseMetaLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.FieldLoadSaveValidator;


public class ColumnExistsMetaTest {
  LoadSaveTester loadSaveTester;
  Class<ColumnExistsMeta> testMetaClass = ColumnExistsMeta.class;

  @Before
  public void setUpLoadSave() throws Exception {
    KettleEnvironment.init();
    PluginRegistry.init( true );
    List<String> attributes =
        Arrays.asList( "schemaname", "tablename", "tablenamefield", "columnnamefield", "resultfieldname", "istablenameInfield", "database" );

    Map<String, String> getterMap = new HashMap<String, String>() {
      {
        put( "schemaname", "getSchemaname" );
        put( "tablename", "getTablename" );
        put( "tablenamefield", "getDynamicTablenameField" );
        put( "columnnamefield", "getDynamicColumnnameField" );
        put( "resultfieldname", "getResultFieldName" );
        put( "istablenameInfield", "isTablenameInField" );
        put( "database", "getDatabase" );
      }
    };
    Map<String, String> setterMap = new HashMap<String, String>() {
      {
        put( "schemaname", "setSchemaname" );
        put( "tablename", "setTablename" );
        put( "tablenamefield", "setDynamicTablenameField" );
        put( "columnnamefield", "setDynamicColumnnameField" );
        put( "resultfieldname", "setResultFieldName" );
        put( "istablenameInfield", "setTablenameInField" );
        put( "database", "setDatabase" );
      }
    };

    Map<String, FieldLoadSaveValidator<?>> attrValidatorMap = new HashMap<String, FieldLoadSaveValidator<?>>();
    attrValidatorMap.put( "database", new DatabaseMetaLoadSaveValidator() );

    Map<String, FieldLoadSaveValidator<?>> typeValidatorMap = new HashMap<String, FieldLoadSaveValidator<?>>();

    loadSaveTester =
        new LoadSaveTester( testMetaClass, attributes, getterMap, setterMap, attrValidatorMap, typeValidatorMap );
  }

  @Test
  public void testSerialization() throws KettleException {
    loadSaveTester.testSerialization();
  }
}
