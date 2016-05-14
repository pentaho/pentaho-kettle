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
package org.pentaho.di.trans.steps.constant;

import org.junit.Test;
import java.util.Arrays;
import static org.junit.Assert.*;

public class ConstantMetaTest {

  @Test
  public void cloneTest() throws Exception {
    ConstantMeta meta = new ConstantMeta();
    meta.allocate( 2 );
    meta.setFieldName( new String[] { "fieldname1", "fieldname2" } );
    meta.setFieldType( new String[] { "fieldtype1", "fieldtype2" } );
    meta.setFieldFormat( new String[] { "fieldformat1", "fieldformat2" } );
    meta.setFieldLength( new int[] { 10, 20 } );
    meta.setFieldPrecision( new int[] { 3, 5 } );
    meta.setCurrency( new String[] { "currency1", "currency2" } );
    meta.setDecimal( new String[] { "decimal1", "decimal2" } );
    meta.setGroup( new String[] { "group1", "group2" } );
    meta.setValue( new String[] { "value1", "value2" } );
    meta.setEmptyString( new boolean[] { false, true } );
    ConstantMeta aClone = (ConstantMeta) meta.clone();
    assertFalse( aClone == meta );
    assertTrue( Arrays.equals( meta.getFieldName(), aClone.getFieldName() ) );
    assertTrue( Arrays.equals( meta.getFieldType(), aClone.getFieldType() ) );
    assertTrue( Arrays.equals( meta.getFieldFormat(), aClone.getFieldFormat() ) );
    assertTrue( Arrays.equals( meta.getFieldLength(), aClone.getFieldLength() ) );
    assertTrue( Arrays.equals( meta.getFieldPrecision(), aClone.getFieldPrecision() ) );
    assertTrue( Arrays.equals( meta.getCurrency(), aClone.getCurrency() ) );
    assertTrue( Arrays.equals( meta.getDecimal(), aClone.getDecimal() ) );
    assertTrue( Arrays.equals( meta.getGroup(), aClone.getGroup() ) );
    assertTrue( Arrays.equals( meta.getValue(), aClone.getValue() ) );
    assertTrue( Arrays.equals( meta.isSetEmptyString(), aClone.isSetEmptyString() ) );
    assertEquals( meta.getXML(), aClone.getXML() );
  }
}
