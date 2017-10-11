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

package org.pentaho.di.trans.steps.fixedinput;

import org.junit.Test;

public class FixedInputContentParsingTest extends BaseFixedParsingTest {
  @Test
  public void testDefaultOptions() throws Exception {
    meta.setLineWidth( "24" );
    init( "default.txt" );

    FixedFileInputField f1 = new FixedFileInputField();
    FixedFileInputField f2 = new FixedFileInputField();
    FixedFileInputField f3 = new FixedFileInputField();
    f1.setWidth( 8 );
    f2.setWidth( 8 );
    f3.setWidth( 8 );
    setFields( f1, f2, f3 );

    process();

    check( new Object[][] { { "first   ", "1       ", "1.1     " }, { "second  ", "2       ", "2.2     " }, {
        "third   ", "3       ", "3.3     " } } );
  }
}
