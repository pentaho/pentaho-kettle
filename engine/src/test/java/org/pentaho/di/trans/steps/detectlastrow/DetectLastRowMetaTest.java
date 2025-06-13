/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.di.trans.steps.detectlastrow;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.core.bowl.DefaultBowl;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.trans.TransMeta.TransformationType;
import org.pentaho.di.trans.steps.loadsave.LoadSaveTester;

public class DetectLastRowMetaTest {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

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
    meta.getFields( DefaultBowl.getInstance(), rowMeta, "this step", null, null, new Variables(), null, null );

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
