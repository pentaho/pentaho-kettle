/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
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
