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
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.hadoop.mapreduce.converter.TypeConversionException;
import org.pentaho.hadoop.mapreduce.converter.TypeConverterFactory;
import org.pentaho.hadoop.mapreduce.converter.spi.ITypeConverter;

/**
 * Converts any Kettle object to an {@link org.apache.hadoop.io.BytesWritable} object
 */
public class KettleTypeToBytesWritableConverter implements ITypeConverter<Object, BytesWritable> {
  @Override
  public boolean canConvert(Class from, Class to) {
    return TypeConverterFactory.isKettleType(from) && BytesWritable.class.equals(to);
  }

  @Override
  public BytesWritable convert(ValueMetaInterface meta, Object obj) throws TypeConversionException {
    try {
      BytesWritable result = new BytesWritable();
      byte[] binary = meta.getBinary(obj);
      result.set(binary, 0, binary.length);
      return result;
    } catch (Exception ex) {
      throw new TypeConversionException(BaseMessages.getString(TypeConverterFactory.class, "ErrorConverting", BytesWritable.class.getSimpleName(), obj), ex);
    }
  }
}
