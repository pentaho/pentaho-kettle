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

package org.pentaho.di.ui.trans.steps.tableoutput;

import org.junit.Before;
import org.junit.Test;

import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaString;

import java.lang.reflect.Method;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TableOutputDialogTest {

  private static RowMetaInterface filled;
  private static RowMetaInterface empty;
  private static String[] sample = { "1", "2", "3" };

  @Before
  public void setup() {
    filled = createRowMeta( sample, false );
    empty = createRowMeta( sample, true );
  }

  @Test
  public void validationRowMetaTest() throws Exception {
    Method m = TableOutputDialog.class.getDeclaredMethod( "isValidRowMeta", RowMetaInterface.class );
    m.setAccessible( true );
    Object result1 = m.invoke( null, filled );
    Object result2 = m.invoke( null, empty );
    assertTrue( Boolean.parseBoolean( result1 + "" ) );
    assertFalse( Boolean.parseBoolean( result2 + "" ) );
  }

  private RowMetaInterface createRowMeta( String[] args, boolean hasEmptyFields ) {
    RowMetaInterface result = new RowMeta();
    if ( hasEmptyFields ) {
      result.addValueMeta( new ValueMetaString( "" ) );
    }
    for ( String s : args ) {
      result.addValueMeta( new ValueMetaString( s ) );
    }
    return result;
  }
}
