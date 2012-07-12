/*******************************************************************************
 *
 * Pentaho Big Data
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
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

package org.pentaho.hadoop.mapreduce;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;

public class OrdinalExtractionTest {

  private RowMetaInterface generateRowMeta(String[] fieldNames) {
    RowMetaInterface rowMeta = new RowMeta();
    
    for(String fieldName : fieldNames) {
      ValueMetaInterface col = new ValueMeta();
      col.setName(fieldName);
      col.setType(ValueMeta.TYPE_STRING);
      rowMeta.addValueMeta(col);
    }      
    return rowMeta;
  }

  @Test
  public void invalidFields() {
    RowMetaInterface meta = generateRowMeta(new String[]{"valueOne", "valueTwo", "valueThree", "valueFour"});
    test("Invalid Fields", InKeyValueOrdinals.class, meta, -1, -1);
    test("Invalid Fields", OutKeyValueOrdinals.class, meta, -1, -1);
  }
  
  @Test
  public void noFields() {
    RowMetaInterface meta = generateRowMeta(new String[]{});
    test("No Fields", InKeyValueOrdinals.class, meta, -1, -1);
    test("No Fields", OutKeyValueOrdinals.class, meta, -1, -1);
  }
  
  @Test
  public void inFieldsFirst() {
    RowMetaInterface meta = generateRowMeta(new String[]{"key", "value", "valueThree", "valueFour"});
    test("In Fields First", InKeyValueOrdinals.class, meta, 0, 1);
    test("In Fields First", OutKeyValueOrdinals.class, meta, -1, -1);
  }
  
  @Test
  public void inFieldsLast() { 
    RowMetaInterface meta = generateRowMeta(new String[]{"valueOne", "valueTwo", "value", "key"});
    test("In Fields Last", InKeyValueOrdinals.class, meta, 3, 2);
    test("In Fields Last", OutKeyValueOrdinals.class, meta, -1, -1);
  }
  
  @Test
  public void outFieldsFirst() {    
    RowMetaInterface meta = generateRowMeta(new String[]{"outKey", "outValue", "valueThree", "valueFour"});
    test("Out Fields First", InKeyValueOrdinals.class, meta, -1, -1);
    test("Out Fields First", OutKeyValueOrdinals.class, meta, 0, 1);
  }
  
  @Test
  public void outFieldsLast() { 
    RowMetaInterface meta = generateRowMeta(new String[]{"valueOne", "valueTwo", "outValue", "outKey"});;
    test("Out Fields Last", InKeyValueOrdinals.class, meta, -1, -1);
    test("Out Fields Last", OutKeyValueOrdinals.class, meta, 3, 2);
  }
  
  @Test
  public void oneInOneOutValueField() { 
    RowMetaInterface meta = generateRowMeta(new String[]{"valueOne", "valueTwo", "outValue", "value"});
    test("One In One Out Value Field", InKeyValueOrdinals.class, meta, -1, 3);
    test("One In One Out Value Field", OutKeyValueOrdinals.class, meta, -1, 2);
  }
  
  @Test
  public void oneInOneOutKeyField() { 
    RowMetaInterface meta = generateRowMeta(new String[]{"valueOne", "outKey", "key", "valueFour"});
    test("One In One Out Key Field", InKeyValueOrdinals.class, meta, 2, -1);
    test("One In One Out Key Field", OutKeyValueOrdinals.class, meta, 1, -1);
  }

  private void test(String testName, Class<?> keyValueOrdinalClass, RowMetaInterface rowMeta, int expectedKey, int expectedValue) {
    BaseKeyValueOrdinals ordinals;
    try {
      ordinals = (BaseKeyValueOrdinals) keyValueOrdinalClass.getConstructor(RowMetaInterface.class).newInstance(rowMeta);
      
      assertEquals(testName + ": key", expectedKey, ordinals.getKeyOrdinal());
      assertEquals(testName + ": value", expectedValue, ordinals.getValueOrdinal());
      
    } catch (Exception e) {
      assertTrue("Unexpected exception creating class [" + keyValueOrdinalClass.getName() + "] from constructor", false);
    } 
  }
}
