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
 * Comparator for use in HBase column filtering that deserializes a numeric
 * column value before performing a comparison. Comparators built-in to HBase
 * only perform comparisons based on the lexicographical ordering of the bytes
 * in values. This works ok for positive numbers but does not work when there
 * are negative numbers involved in the comparison.
 * 
 * @author Mark Hall (mhall{[at]}pentaho{[dot]}com)
 * @version $Revision$
 *
 */
public class DeserializedNumericComparator extends WritableByteArrayComparable {
  
  protected long m_longValue;
  
  protected double m_doubleValue;
  
  protected boolean m_isInteger;
  protected boolean m_isLongOrDouble;
  
  /** Nullary constructor for Writable, do not use */
  public DeserializedNumericComparator() {
    super();
  }
  
  public DeserializedNumericComparator(boolean isInteger, boolean isLongOrDouble, long value) {
    m_isInteger = isInteger;
    m_isLongOrDouble = isLongOrDouble;
    m_longValue = value;
  }
  
  public DeserializedNumericComparator(boolean isInteger, boolean isLongOrDouble, double value) {
    m_isInteger = isInteger;
    m_isLongOrDouble = isLongOrDouble;
    
    m_doubleValue = value;
  }
  
  public byte[] getValue() {
    if (m_isInteger) {
      return Bytes.toBytes(m_longValue);
    }

    return Bytes.toBytes(m_doubleValue);
  }

  @Override
  public void readFields(DataInput in) throws IOException {
    m_isInteger = in.readBoolean();
    m_isLongOrDouble = in.readBoolean();
    m_longValue = in.readLong();
    m_doubleValue = in.readDouble();    
  }

  @Override
  public void write(DataOutput out) throws IOException {
    out.writeBoolean(m_isInteger);
    out.writeBoolean(m_isLongOrDouble);
    out.writeLong(m_longValue);
    out.writeDouble(m_doubleValue);    
  }

  @Override
  public int compareTo(byte [] value) {
    if (m_isInteger) {
      long compV;
      if (value.length == Bytes.SIZEOF_LONG) {
        compV = Bytes.toLong(value);
      } else if (value.length == Bytes.SIZEOF_INT) {
        compV = Bytes.toInt(value);
      } else {
        compV = Bytes.toShort(value);
      }

      Long l = new Long(m_longValue);
      return l.compareTo(compV);
    }
    
    double compV;
    if (value.length == Bytes.SIZEOF_DOUBLE) {
      compV = Bytes.toDouble(value);
    } else {
      compV = Bytes.toFloat(value);
    }
    
    Double d = new Double(m_doubleValue);
    return d.compareTo(compV);        
  }
}
