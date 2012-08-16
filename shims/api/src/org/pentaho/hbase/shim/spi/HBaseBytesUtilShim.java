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

package org.pentaho.hbase.shim.spi;

public interface HBaseBytesUtilShim {

  int getSizeOfFloat();

  int getSizeOfDouble();

  int getSizeOfInt();

  int getSizeOfLong();

  int getSizeOfShort();

  int getSizeOfByte();

  byte[] toBytes(String aString);

  byte[] toBytes(int anInt);

  byte[] toBytes(long aLong);

  byte[] toBytes(float aFloat);

  byte[] toBytes(double aDouble);

  byte[] toBytesBinary(String value);

  String toString(byte[] value);

  long toLong(byte[] value);

  int toInt(byte[] value);

  float toFloat(byte[] value);

  double toDouble(byte[] value);

  short toShort(byte[] value);
}
