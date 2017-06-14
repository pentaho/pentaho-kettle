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

package org.pentaho.di.trans.steps.jsoninput;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.TransTestFactory;

public class JsonInputIntTest {

  @BeforeClass
  public static void setUpBeforeClass() throws KettleException {
    KettleEnvironment.init( false );
  }

  @Test
  public void testNoInput() throws KettleException {
    String stepName = "noInputStep";
    JsonInputMeta meta = new JsonInputMeta();
    meta.setInFields( true );
    meta.setFieldValue( "myJSONStringField" );
    JsonInputField field = new JsonInputField( "test" );
    field.setPath( "$.value" );
    meta.setInputFields( new JsonInputField[]{ field } );

    TransMeta transMeta = TransTestFactory.generateTestTransformation( new Variables(), meta, stepName );
    List<RowMetaAndData> result = TransTestFactory.executeTestTransformation( transMeta,
      TransTestFactory.INJECTOR_STEPNAME, stepName, TransTestFactory.DUMMY_STEPNAME, new ArrayList<RowMetaAndData>() );

    assertNotNull( result );
    assertTrue( result.isEmpty() );
  }
}
