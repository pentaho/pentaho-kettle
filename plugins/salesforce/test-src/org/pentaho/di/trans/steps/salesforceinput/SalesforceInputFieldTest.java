/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2016 by Pentaho : http://www.pentaho.com
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
