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
