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
package org.pentaho.di.trans.steps.propertyinput;

import org.junit.Test;

public class PropertyInputContentParsingTest extends BasePropertyParsingTest {
  @Test
  public void testDefaultOptions() throws Exception {
    init( "default.properties" );

    PropertyInputField f1 = new PropertyInputField();
    f1.setColumn( PropertyInputField.COLUMN_KEY );
    PropertyInputField f2 = new PropertyInputField();
    f2.setColumn( PropertyInputField.COLUMN_VALUE );
    setFields( f1, f2 );

    process();

    check( new Object[][] { { "f1", "d1" }, { "f2", "d2" } } );
  }
}
