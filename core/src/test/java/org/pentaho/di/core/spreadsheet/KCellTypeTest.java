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
