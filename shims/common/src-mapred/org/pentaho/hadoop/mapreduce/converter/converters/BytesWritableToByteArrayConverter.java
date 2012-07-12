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

package org.pentaho.hadoop.mapreduce.converter.converters;

import org.apache.hadoop.io.BytesWritable;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.hadoop.mapreduce.converter.TypeConversionException;
import org.pentaho.hadoop.mapreduce.converter.spi.ITypeConverter;

/**
 * Converts {@link org.apache.hadoop.io.BytesWritable} objects to {@link byte[]} objects
 */
public class BytesWritableToByteArrayConverter implements ITypeConverter<BytesWritable, byte[]> {
  @Override
  public boolean canConvert(Class from, Class to) {
    return BytesWritable.class.equals(from) && byte[].class.equals(to);
  }

  @Override
  public byte[] convert(ValueMetaInterface meta, BytesWritable obj) throws TypeConversionException {
    return obj.getBytes();
  }
}
