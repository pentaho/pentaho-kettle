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

package org.pentaho.di.trans.steps.fileinput.text;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * User: Dzmitry Stsiapanau Date: 3/11/14 Time: 11:44 AM
 */
public class EncodingTypeTest {
  @Test
  public void testIsReturn() throws Exception {
    int lineFeed = '\n';
    int carriageReturn = '\r';
    assertTrue( "SINGLE.isLineFeed is not line feed", EncodingType.SINGLE.isLinefeed( lineFeed ) );
    assertTrue( "DOUBLE_BIG_ENDIAN is not line feed", EncodingType.DOUBLE_BIG_ENDIAN.isLinefeed( lineFeed ) );
    assertTrue( "DOUBLE_LITTLE_ENDIAN.isLineFeed is not line feed",
      EncodingType.DOUBLE_LITTLE_ENDIAN.isLinefeed( lineFeed ) );
    assertFalse( "SINGLE.isLineFeed is carriage return", EncodingType.SINGLE.isLinefeed( carriageReturn ) );
    assertFalse( "DOUBLE_BIG_ENDIAN.isLineFeed is carriage return",
      EncodingType.DOUBLE_BIG_ENDIAN.isLinefeed( carriageReturn ) );
    assertFalse( "DOUBLE_LITTLE_ENDIAN.isLineFeed is carriage return",
      EncodingType.DOUBLE_LITTLE_ENDIAN.isLinefeed( carriageReturn ) );
  }

  @Test
  public void testIsLinefeed() throws Exception {
    int lineFeed = '\n';
    int carriageReturn = '\r';
    assertFalse( "SINGLE.isReturn is line feed", EncodingType.SINGLE.isReturn( lineFeed ) );
    assertFalse( "DOUBLE_BIG_ENDIAN.isReturn is line feed", EncodingType.DOUBLE_BIG_ENDIAN.isReturn( lineFeed ) );
    assertFalse( "DOUBLE_LITTLE_ENDIAN.isReturn is line feed",
      EncodingType.DOUBLE_LITTLE_ENDIAN.isReturn( lineFeed ) );
    assertTrue( "SINGLE.isReturn is not carriage return", EncodingType.SINGLE.isReturn( carriageReturn ) );
    assertTrue( "DOUBLE_BIG_ENDIAN.isReturn is not carriage return",
      EncodingType.DOUBLE_BIG_ENDIAN.isReturn( carriageReturn ) );
    assertTrue( "DOUBLE_LITTLE_ENDIAN.isReturn is not carriage return",
      EncodingType.DOUBLE_LITTLE_ENDIAN.isReturn( carriageReturn ) );
  }
}
