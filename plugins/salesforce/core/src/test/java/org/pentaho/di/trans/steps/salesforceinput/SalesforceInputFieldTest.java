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


package org.pentaho.di.trans.steps.salesforceinput;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class SalesforceInputFieldTest {

  @Test
  public void testLookups() {
    assertEquals( 0, SalesforceInputField.getTrimTypeByCode( null ) );
    assertTrue( SalesforceInputField.trimTypeCode.length > 2 );
    assertEquals( 0, SalesforceInputField.getTrimTypeByCode( "none" ) );
    assertEquals( 1, SalesforceInputField.getTrimTypeByCode( "left" ) );
    assertEquals( 2, SalesforceInputField.getTrimTypeByCode( "right" ) );
    assertEquals( 3, SalesforceInputField.getTrimTypeByCode( "both" ) );
    assertEquals( 0, SalesforceInputField.getTrimTypeByCode( "invalid" ) );

    assertEquals( 0, SalesforceInputField.getTrimTypeByDesc( null ) );
    assertTrue( SalesforceInputField.trimTypeDesc.length > 2 );
    assertEquals( 0, SalesforceInputField.getTrimTypeByDesc( "invalid" ) );

    assertEquals( "none", SalesforceInputField.getTrimTypeCode( -1 ) );
    assertEquals( "none", SalesforceInputField.getTrimTypeCode( SalesforceInputField.trimTypeCode.length + 1 ) );
    assertEquals( "none", SalesforceInputField.getTrimTypeCode( 0 ) );
    assertEquals( "left", SalesforceInputField.getTrimTypeCode( 1 ) );
    assertEquals( "right", SalesforceInputField.getTrimTypeCode( 2 ) );
    assertEquals( "both", SalesforceInputField.getTrimTypeCode( 3 ) );

    assertEquals( SalesforceInputField.getTrimTypeDesc( 0 ), SalesforceInputField.getTrimTypeDesc( -1 ) );
    assertEquals( SalesforceInputField.getTrimTypeDesc( 0 ),
      SalesforceInputField.getTrimTypeDesc( SalesforceInputField.trimTypeCode.length + 1 ) );
  }
}
