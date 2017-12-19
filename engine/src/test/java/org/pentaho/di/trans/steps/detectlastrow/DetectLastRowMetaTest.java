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

package org.pentaho.di.trans.steps.detectlastrow;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.trans.TransMeta.TransformationType;
import org.pentaho.di.trans.steps.loadsave.LoadSaveTester;

public class DetectLastRowMetaTest {

  @Test
  public void testStepMeta() throws KettleException {
    List<String> attributes = Arrays.asList( "ResultFieldName" );

    LoadSaveTester<DetectLastRowMeta> loadSaveTester = new LoadSaveTester<>( DetectLastRowMeta.class, attributes );
    loadSaveTester.testSerialization();
  }

  @Test
  public void testDefault() {
    DetectLastRowMeta meta = new DetectLastRowMeta();
    meta.setDefault();
    assertEquals( "result", meta.getResultFieldName() );
  }

  @Test
  public void testGetData() {
    DetectLastRowMeta meta = new DetectLastRowMeta();
    assertTrue( meta.getStepData() instanceof DetectLastRowData );
  }

  @Test
  public void testGetFields() throws KettleStepException {
    DetectLastRowMeta meta = new DetectLastRowMeta();
    meta.setDefault();
    meta.setResultFieldName( "The Result" );
    RowMeta rowMeta = new RowMeta();
    meta.getFields( rowMeta, "this step", null, null, new Variables(), null, null );

    assertEquals( 1, rowMeta.size() );
    assertEquals( "The Result", rowMeta.getValueMeta( 0 ).getName() );
    assertEquals( ValueMetaInterface.TYPE_BOOLEAN, rowMeta.getValueMeta( 0 ).getType() );
  }

  @Test
  public void testSupportedTransformationTypes() {
    DetectLastRowMeta meta = new DetectLastRowMeta();
    assertEquals( 1, meta.getSupportedTransformationTypes().length );
    assertEquals( TransformationType.Normal, meta.getSupportedTransformationTypes()[0] );
  }
}
