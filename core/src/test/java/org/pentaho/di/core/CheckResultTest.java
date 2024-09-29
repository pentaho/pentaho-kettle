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

package org.pentaho.di.core;

import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CheckResultTest {
  @Test
  public void testClass() {
    final int type = CheckResultInterface.TYPE_RESULT_ERROR;
    final String text = "some text";
    final String sourceMetaName = "meta name";
    final CheckResultSourceInterface sourceMeta = mock( CheckResultSourceInterface.class );
    final String errorCode = "error code";

    CheckResult cr = new CheckResult();
    assertEquals( CheckResultInterface.TYPE_RESULT_NONE, cr.getType() );
    assertTrue( cr.getTypeDesc() != null && cr.getTypeDesc().isEmpty() );
    cr.setType( type );
    assertEquals( type, cr.getType() );

    assertTrue( cr.getText().isEmpty() );
    cr.setText( text );
    assertSame( text, cr.getText() );

    assertNull( null, cr.getSourceInfo() );

    assertNull( cr.getErrorCode() );
    cr.setErrorCode( errorCode );
    assertSame( errorCode, cr.getErrorCode() );

    when( sourceMeta.getName() ).thenReturn( sourceMetaName );
    cr = new CheckResult( type, text, sourceMeta );
    assertSame( sourceMeta, cr.getSourceInfo() );
    assertTrue( cr.getTypeDesc() != null && !cr.getTypeDesc().isEmpty() );
    final String stringValue = String.format( "%s: %s (%s)", cr.getTypeDesc(), text, sourceMetaName );
    assertEquals( stringValue, cr.toString() );

    cr = new CheckResult( type, errorCode, text, sourceMeta );
    assertSame( errorCode, cr.getErrorCode() );
  }
}
