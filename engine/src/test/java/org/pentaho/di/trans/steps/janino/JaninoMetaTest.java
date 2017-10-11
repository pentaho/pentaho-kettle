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

package org.pentaho.di.trans.steps.janino;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.row.value.ValueMetaFactory;
import org.pentaho.di.core.row.value.ValueMetaPluginType;
import org.pentaho.di.trans.steps.loadsave.LoadSaveTester;
import org.pentaho.di.trans.steps.loadsave.validator.ArrayLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.FieldLoadSaveValidator;

public class JaninoMetaTest {

  @BeforeClass
  public static void setUpBeforeClass() throws KettlePluginException {
    PluginRegistry.addPluginType( ValueMetaPluginType.getInstance() );
    PluginRegistry.init( true );
  }

  @Test
  public void testRoundTrip() throws KettleException {
    List<String> attributes = Arrays.asList( "formula" );

    Map<String, FieldLoadSaveValidator<?>> fieldLoadSaveValidatorAttributeMap =
      new HashMap<String, FieldLoadSaveValidator<?>>();

    FieldLoadSaveValidator<JaninoMetaFunction[]> janinoMetaFunctionArrayLoadSaveValidator =
      new ArrayLoadSaveValidator<JaninoMetaFunction>( new JaninoMetaFunctionFieldLoadSaveValidator(), 25 );

    fieldLoadSaveValidatorAttributeMap.put( "formula", janinoMetaFunctionArrayLoadSaveValidator );

    LoadSaveTester loadSaveTester =
      new LoadSaveTester( JaninoMeta.class, attributes, new HashMap<String, String>(), new HashMap<String, String>(),
          fieldLoadSaveValidatorAttributeMap, new HashMap<String, FieldLoadSaveValidator<?>>() );

    loadSaveTester.testSerialization();
  }

  public class JaninoMetaFunctionFieldLoadSaveValidator implements FieldLoadSaveValidator<JaninoMetaFunction> {
    @Override
    public JaninoMetaFunction getTestObject() {
      Random random = new Random();
      return new JaninoMetaFunction(
        UUID.randomUUID().toString(),
        UUID.randomUUID().toString(),
        random.nextInt( ValueMetaFactory.getAllValueMetaNames().length ),
        random.nextInt( Integer.MAX_VALUE ),
        random.nextInt( Integer.MAX_VALUE ),
        UUID.randomUUID().toString() );
    }

    @Override
    public boolean validateTestObject( JaninoMetaFunction testObject, Object actual ) {
      return testObject.equals( actual );
    }
  }
}
