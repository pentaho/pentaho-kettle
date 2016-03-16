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

package org.pentaho.di.trans.steps.rowsfromresult;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

public class RowsFromResultMetaTest {

  @Test
  public void testClone() throws Exception {
    RowsFromResultMeta meta = new RowsFromResultMeta();
    meta.setFieldname( new String[] { "field1", "field2" } );
    meta.setLength( new int[] { 5, 5 } );
    meta.setPrecision( new int[] { 5, 5 } );
    meta.setType( new int[] { 0, 0 } );

    RowsFromResultMeta cloned = (RowsFromResultMeta) meta.clone();
    assertFalse( cloned.getFieldname() == meta.getFieldname() );
    assertTrue( Arrays.equals( cloned.getFieldname(), meta.getFieldname() ) );
    assertFalse( cloned.getLength() == meta.getLength() );
    assertTrue( Arrays.equals( cloned.getLength(), meta.getLength() ) );
    assertFalse( cloned.getPrecision() == meta.getPrecision() );
    assertTrue( Arrays.equals( cloned.getPrecision(), meta.getPrecision() ) );
    assertFalse( cloned.getType() == meta.getType() );
    assertTrue( Arrays.equals( cloned.getType(), meta.getType() ) );
  }

}
