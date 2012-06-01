/*
 * Copyright (c) 2011 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
 */
package org.pentaho.hadoop.mapreduce.test;

import junit.framework.TestCase;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.hadoop.mapreduce.PentahoMapReduceBase;

public class OrdinalExtractionTest extends TestCase {
  
  public void test1() {
    TestHandler.executeTests();
  }
  
  private static class TestHandler extends PentahoMapReduceBase {
    
    public TestHandler() throws KettleException{};
     
    public static void executeTests() {
      TestHandler.test("Invalid Fields", InKeyValueOrdinals.class, getTestRowMeta_InvalidFields(), -1, -1);
      TestHandler.test("Invalid Fields", OutKeyValueOrdinals.class, getTestRowMeta_InvalidFields(), -1, -1);
      
      TestHandler.test("No Fields", InKeyValueOrdinals.class, getTestRowMeta_NoFields(), -1, -1);
      TestHandler.test("No Fields", OutKeyValueOrdinals.class, getTestRowMeta_NoFields(), -1, -1);
      
      TestHandler.test("In Fields First", InKeyValueOrdinals.class, getTestRowMeta_InFieldsFirst(), 0, 1);
      TestHandler.test("In Fields First", OutKeyValueOrdinals.class, getTestRowMeta_InFieldsFirst(), -1, -1);
      
      TestHandler.test("In Fields Last", InKeyValueOrdinals.class, getTestRowMeta_InFieldsLast(), 3, 2);
      TestHandler.test("In Fields Last", OutKeyValueOrdinals.class, getTestRowMeta_InFieldsLast(), -1, -1);
      
      TestHandler.test("Out Fields First", InKeyValueOrdinals.class, getTestRowMeta_OutFieldsFirst(), -1, -1);
      TestHandler.test("Out Fields First", OutKeyValueOrdinals.class, getTestRowMeta_OutFieldsFirst(), 0, 1);
      
      TestHandler.test("Out Fields Last", InKeyValueOrdinals.class, getTestRowMeta_OutFieldsLast(), -1, -1);
      TestHandler.test("Out Fields Last", OutKeyValueOrdinals.class, getTestRowMeta_OutFieldsLast(), 3, 2);
      
      TestHandler.test("One In One Out Value Field", InKeyValueOrdinals.class, getTestRowMeta_OneInOneOutValueField(), -1, 3);
      TestHandler.test("One In One Out Value Field", OutKeyValueOrdinals.class, getTestRowMeta_OneInOneOutValueField(), -1, 2);
      
      TestHandler.test("One In One Out Key Field", InKeyValueOrdinals.class, getTestRowMeta_OneInOneOutKeyField(), 2, -1);
      TestHandler.test("One In One Out Key Field", OutKeyValueOrdinals.class, getTestRowMeta_OneInOneOutKeyField(), 1, -1);
    }
    
    protected static RowMetaInterface getTestRowMeta_InvalidFields() {
      return generateRowMeta(new String[]{"valueOne", "valueTwo", "valueThree", "valueFour"});
    }
    
    protected static RowMetaInterface getTestRowMeta_InFieldsFirst() {
      return generateRowMeta(new String[]{"key", "value", "valueThree", "valueFour"});
    }
    
    protected static RowMetaInterface getTestRowMeta_InFieldsLast() {
      return generateRowMeta(new String[]{"valueOne", "valueTwo", "value", "key"});
    }
    
    protected static RowMetaInterface getTestRowMeta_OutFieldsFirst() {
      return generateRowMeta(new String[]{"outKey", "outValue", "valueThree", "valueFour"});
    }
    
    protected static RowMetaInterface getTestRowMeta_OutFieldsLast() {
      return generateRowMeta(new String[]{"valueOne", "valueTwo", "outValue", "outKey"});
    }
    
    protected static RowMetaInterface getTestRowMeta_OneInOneOutValueField() {
      return generateRowMeta(new String[]{"valueOne", "valueTwo", "outValue", "value"});
    }
    
    protected static RowMetaInterface getTestRowMeta_OneInOneOutKeyField() {
      return generateRowMeta(new String[]{"valueOne", "outKey", "key", "valueFour"});
    }
    
    protected static RowMetaInterface getTestRowMeta_NoFields() {
      return generateRowMeta(new String[]{});
    }
    
    private static RowMetaInterface generateRowMeta(String[] fieldNames) {
      RowMetaInterface rowMeta = new RowMeta();
      
      for(String fieldName : fieldNames) {
        ValueMetaInterface col = new ValueMeta();
        col.setName(fieldName);
        col.setType(ValueMeta.TYPE_STRING);
        rowMeta.addValueMeta(col);
      }      
      return rowMeta;
    }

    private static void test(String testName, Class keyValueOrdinalClass, RowMetaInterface rowMeta, int expectedKey, int expectedValue) {
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

}
