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

package org.pentaho.di.trans.steps.gettablenames;

import java.util.Arrays;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.trans.steps.loadsave.LoadSaveTester;

public class GetTableNamesMetaTest {

  @BeforeClass
  public static void setUpBeforeClass() throws KettleException {
    KettleEnvironment.init( false );
  }

  @Test
  public void testLoadSave() throws KettleException {
    List<String> attributes = Arrays.asList( "Database", "SchemaName", "TablenameFieldName", "ObjectTypeFieldName",
      "SystemObjectFieldName", "SQLCreationFieldName", "includeCatalog", "includeSchema", "includeTable",
      "includeView", "includeProcedure", "includeSynonym", "AddSchemaInOut", "DynamicSchema", "SchemaFieldName" );

    LoadSaveTester<GetTableNamesMeta> loadSaveTester =
      new LoadSaveTester<GetTableNamesMeta>( GetTableNamesMeta.class, attributes );

    loadSaveTester.testSerialization();
  }
}
