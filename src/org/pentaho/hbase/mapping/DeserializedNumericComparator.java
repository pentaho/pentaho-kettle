/* Copyright (c) 2011 Pentaho Corporation.  All rights reserved. 
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

package org.pentaho.hbase.mapping;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.hbase.filter.WritableByteArrayComparable;
import org.apache.hadoop.hbase.util.Bytes;

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
