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

package org.pentaho.hbase.mapping;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.hbase.filter.WritableByteArrayComparable;
import org.apache.hadoop.hbase.util.Bytes;

/**
 * Comparator for use in HBase column filtering that deserializes a boolean
 * column value before performing a comparison. Various string and numeric
 * deserializations are tried. All representations of "false" (for a given
 * encoding type) sort before "true" - e.g. "false" < "true"; 0 < 1; "F" < "T";
 * "N" < "Y" etc. Thus < or > comparisons (excluding < false and > true) are
 * equivalent to !=.
 * 
 * 
 * @author Mark Hall (mhall{[at]}pentaho{[dot]}com)
 * @version $Revision$
 *
 */
public class DeserializedBooleanComparator extends WritableByteArrayComparable {
  
  protected Boolean m_value;  
  
  public DeserializedBooleanComparator() {
    super();
  }
  
  public DeserializedBooleanComparator(boolean value) {
    m_value = value;
  }
  
  public byte[] getValue() {
    return Bytes.toBytes(m_value.booleanValue());
  }
  
  public void readFields(DataInput in) throws IOException {
    m_value = new Boolean(in.readBoolean());
  }
  
  public void write(DataOutput out) throws IOException {
    out.writeBoolean(m_value.booleanValue());
  }
  
  public int compareTo(byte[] value) {
    
    Boolean decodedValue = decodeBoolFromString(value);
    
    // if null then try as a number
    if (decodedValue == null) {
      decodedValue = decodeBoolFromNumber(value);
    }
    
    if (decodedValue != null) {
      if (m_value.equals(decodedValue)) {
        return 0;
      }
      
      if (m_value.booleanValue() == false && decodedValue.booleanValue() == true) {
        return -1;
      }
      
      return 1;
    }
    
    // doesn't matter what we return here because the step wont be able to
    // decode this value either so an Exception will be raised
    return 0;
  }
  
  public static Boolean decodeBoolFromString(byte[] rawEncoded) {
    String tempString = Bytes.toString(rawEncoded);
    if (tempString.equalsIgnoreCase("Y") || tempString.equalsIgnoreCase("N") ||
        tempString.equalsIgnoreCase("YES") || tempString.equalsIgnoreCase("NO") ||
        tempString.equalsIgnoreCase("TRUE") || tempString.equalsIgnoreCase("FALSE") ||
        tempString.equalsIgnoreCase("T") || tempString.equalsIgnoreCase("F") ||
        tempString.equalsIgnoreCase("1") || tempString.equalsIgnoreCase("0")) {

      return Boolean.valueOf(tempString.equalsIgnoreCase("Y") || 
          tempString.equalsIgnoreCase("YES") ||
          tempString.equalsIgnoreCase("TRUE") ||
          tempString.equalsIgnoreCase("T") ||
          tempString.equalsIgnoreCase("1"));
    }

    // not identifiable from a string
    return null;
  }

  public static Boolean decodeBoolFromNumber(byte[] rawEncoded) {
    if (rawEncoded.length == Bytes.SIZEOF_BYTE) {
      byte val = rawEncoded[0];
      if (val == 0 || val == 1) {
        return new Boolean(val == 1);
      }
    }

    if (rawEncoded.length == Bytes.SIZEOF_SHORT) {
      short tempShort = Bytes.toShort(rawEncoded);

      if (tempShort == 0 || tempShort == 1) {
        return new Boolean(tempShort == 1);
      }
    }

    if (rawEncoded.length == Bytes.SIZEOF_INT || 
        rawEncoded.length == Bytes.SIZEOF_FLOAT) {
      int tempInt = Bytes.toInt(rawEncoded);
      if (tempInt == 1 || tempInt == 0) {
        return new Boolean(tempInt == 1);
      }

      float tempFloat = Bytes.toFloat(rawEncoded);
      if (tempFloat == 0.0f || tempFloat == 1.0f) {
        return new Boolean(tempFloat == 1.0f);
      }
    }

    if (rawEncoded.length == Bytes.SIZEOF_LONG ||
        rawEncoded.length == Bytes.SIZEOF_DOUBLE) {
      long tempLong = Bytes.toLong(rawEncoded);
      if (tempLong == 0L || tempLong == 1L) {
        return new Boolean(tempLong == 1L);
      }

      double tempDouble = Bytes.toDouble(rawEncoded);
      if (tempDouble == 0.0 || tempDouble == 1.0) {
        return new Boolean(tempDouble == 1.0);
      }
    }

    // not identifiable from a number
    return null;
  }
  
}
