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


package org.pentaho.di.trans.steps.creditcardvalidator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.trans.steps.loadsave.LoadSaveTester;

public class CreditCardValidatorMetaTest {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  @Test
  public void testLoadSave() throws KettleException {
    List<String> attributes =
      Arrays.asList( "DynamicField", "ResultFieldName", "CardType", "OnlyDigits", "NotValidMsg" );
    LoadSaveTester<CreditCardValidatorMeta> loadSaveTester =
      new LoadSaveTester<CreditCardValidatorMeta>( CreditCardValidatorMeta.class, attributes );

    loadSaveTester.testSerialization();
  }

  @Test
  public void testSupportsErrorHandling() {
    assertTrue( new CreditCardValidatorMeta().supportsErrorHandling() );
  }

  @Test
  public void testDefaults() {
    CreditCardValidatorMeta meta = new CreditCardValidatorMeta();
    meta.setDefault();
    assertEquals( "result", meta.getResultFieldName() );
    assertFalse( meta.isOnlyDigits() );
    assertEquals( "card type", meta.getCardType() );
    assertEquals( "not valid message", meta.getNotValidMsg() );
  }

  @Test
  public void testGetFields() throws KettleStepException {
    CreditCardValidatorMeta meta = new CreditCardValidatorMeta();
    meta.setDefault();
    meta.setResultFieldName( "The Result Field" );
    meta.setCardType( "The Card Type Field" );
    meta.setNotValidMsg( "Is Card Valid" );

    RowMeta rowMeta = new RowMeta();
    meta.getFields( rowMeta, "this step", null, null, new Variables(), null, null );
    assertEquals( 3, rowMeta.size() );
    assertEquals( "The Result Field", rowMeta.getValueMeta( 0 ).getName() );
    assertEquals( ValueMetaInterface.TYPE_BOOLEAN, rowMeta.getValueMeta( 0 ).getType() );
    assertEquals( "this step", rowMeta.getValueMeta( 0 ).getOrigin() );
    assertEquals( "The Card Type Field", rowMeta.getValueMeta( 1 ).getName() );
    assertEquals( ValueMetaInterface.TYPE_STRING, rowMeta.getValueMeta( 1 ).getType() );
    assertEquals( "this step", rowMeta.getValueMeta( 1 ).getOrigin() );
    assertEquals( "Is Card Valid", rowMeta.getValueMeta( 2 ).getName() );
    assertEquals( ValueMetaInterface.TYPE_STRING, rowMeta.getValueMeta( 2 ).getType() );
    assertEquals( "this step", rowMeta.getValueMeta( 2 ).getOrigin() );
  }
}
