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

package org.pentaho.hbase.shim.common;

import org.apache.hadoop.hbase.util.Bytes;
import org.pentaho.hbase.shim.spi.HBaseBytesUtilShim;

public class CommonHBaseBytesUtil implements HBaseBytesUtilShim {

  public int getSizeOfFloat() {
    return Bytes.SIZEOF_FLOAT;
  }

  public int getSizeOfDouble() {
    return Bytes.SIZEOF_DOUBLE;
  }

  public int getSizeOfInt() {
    return Bytes.SIZEOF_INT;
  }

  public int getSizeOfLong() {
    return Bytes.SIZEOF_LONG;
  }

  public int getSizeOfShort() {
    return Bytes.SIZEOF_SHORT;
  }

  public int getSizeOfByte() {
    return Bytes.SIZEOF_BYTE;
  }

  public byte[] toBytes(String aString) {
    return Bytes.toBytes(aString);
  }

  public byte[] toBytes(int anInt) {
    return Bytes.toBytes(anInt);
  }

  public byte[] toBytes(long aLong) {
    return Bytes.toBytes(aLong);
  }

  public byte[] toBytes(float aFloat) {
    return Bytes.toBytes(aFloat);
  }

  public byte[] toBytes(double aDouble) {
    return Bytes.toBytes(aDouble);
  }

  public byte[] toBytesBinary(String value) {
    return Bytes.toBytesBinary(value);
  }

  public String toString(byte[] value) {
    return Bytes.toString(value);
  }

  public long toLong(byte[] value) {
    return Bytes.toLong(value);
  }

  public int toInt(byte[] value) {
    return Bytes.toInt(value);
  }

  public float toFloat(byte[] value) {
    return Bytes.toFloat(value);
  }

  public double toDouble(byte[] value) {
    return Bytes.toDouble(value);
  }

  public short toShort(byte[] value) {
    return Bytes.toShort(value);
  }
}
