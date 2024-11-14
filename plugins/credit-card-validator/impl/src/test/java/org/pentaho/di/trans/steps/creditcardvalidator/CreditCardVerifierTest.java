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


package org.pentaho.di.trans.steps.creditcardvalidator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class CreditCardVerifierTest {

  @Test
  public void testStatics() {
    int totalCardNames = -1;
    int totalNotValidCardNames = -1;
    for ( int i = 0; i < 50; i++ ) {
      String result = CreditCardVerifier.getCardName( i );
      if ( result == null ) {
        totalCardNames = i - 1;
        break;
      }
    }
    for ( int i = 0; i < 50; i++ ) {
      String result = CreditCardVerifier.getNotValidCardNames( i );
      if ( result == null ) {
        totalNotValidCardNames = i - 1;
        break;
      }
    }
    assertNotSame( -1, totalCardNames );
    assertNotSame( -1, totalNotValidCardNames );
    assertEquals( totalCardNames, totalNotValidCardNames );
  }

  @Test
  public void testIsNumber() {
    assertFalse( CreditCardVerifier.isNumber( "" ) );
    assertFalse( CreditCardVerifier.isNumber( "a" ) );
    assertTrue( CreditCardVerifier.isNumber( "1" ) );
    assertTrue( CreditCardVerifier.isNumber( "1.01" ) );
  }
}
