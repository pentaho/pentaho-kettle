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

package org.pentaho.di.core.spreadsheet;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class KCellTypeTest {

  @Test
  public void testEnums() {
    assertEquals( "Empty", KCellType.EMPTY.getDescription() );
    assertEquals( "Boolean", KCellType.BOOLEAN.getDescription() );
    assertEquals( "Boolean formula", KCellType.BOOLEAN_FORMULA.getDescription() );
    assertEquals( "Date", KCellType.DATE.getDescription() );
    assertEquals( "Date formula", KCellType.DATE_FORMULA.getDescription() );
    assertEquals( "Label", KCellType.LABEL.getDescription() );
    assertEquals( "String formula", KCellType.STRING_FORMULA.getDescription() );
    assertEquals( "Number", KCellType.NUMBER.getDescription() );
    assertEquals( "Number formula", KCellType.NUMBER_FORMULA.getDescription() );
  }
}
