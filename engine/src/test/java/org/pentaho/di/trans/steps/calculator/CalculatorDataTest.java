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


package org.pentaho.di.trans.steps.calculator;

import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;

import static org.junit.Assert.assertTrue;

/**
 * @author Andrey Khayrutdinov
 */
public class CalculatorDataTest {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  @Test
  public void dataReturnsCachedValues() throws Exception {
    KettleEnvironment.init( false );

    CalculatorData data = new CalculatorData();
    ValueMetaInterface valueMeta = data.getValueMetaFor( ValueMetaInterface.TYPE_INTEGER, null );
    ValueMetaInterface shouldBeTheSame = data.getValueMetaFor( ValueMetaInterface.TYPE_INTEGER, null );
    assertTrue( "CalculatorData should cache loaded value meta instances", valueMeta == shouldBeTheSame );
  }
}
