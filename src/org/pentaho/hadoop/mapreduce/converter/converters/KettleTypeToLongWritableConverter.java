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

import org.apache.hadoop.io.LongWritable;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.hadoop.mapreduce.converter.TypeConversionException;
import org.pentaho.hadoop.mapreduce.converter.TypeConverterFactory;
import org.pentaho.hadoop.mapreduce.converter.spi.ITypeConverter;

/**
 * Converts any Kettle object to a {@link LongWritable} object
 */
public class KettleTypeToLongWritableConverter implements ITypeConverter<Object, LongWritable> {
  @Override
  public boolean canConvert(Class from, Class to) {
    return TypeConverterFactory.isKettleType(from) && LongWritable.class.equals(to);
  }

  @Override
  public LongWritable convert(ValueMetaInterface meta, Object obj) throws TypeConversionException {
    try {
      LongWritable result = new LongWritable();
      result.set(meta.getInteger(obj));
      return result;
    } catch (KettleValueException ex) {
      throw new TypeConversionException(BaseMessages.getString(TypeConverterFactory.class, "ErrorConverting", LongWritable.class.getSimpleName(), obj), ex);
    }
  }
}
