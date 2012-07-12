package org.pentaho.hadoop.mapreduce.converter.converters;

import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.hadoop.mapreduce.converter.TypeConversionException;
import org.pentaho.hadoop.mapreduce.converter.spi.ITypeConverter;

/**
 * Dummy "converter" that passes through ImmutableBytesWritable objects as Object. This 
 * will allow them to be carried in PDI rows as TYPE_SERIALIZABLE
 * 
 * @author Mark Hall (mhall{[at]}pentaho{[dot]}com)
 * @version $Revision$
 */
public class ImmutableBytesWritablePassThroughConverter implements
    ITypeConverter<ImmutableBytesWritable, Object> {

  public boolean canConvert(Class from, Class to) {
    return ImmutableBytesWritable.class.equals(from) && Object.class.equals(to);
  }

  public Object convert(ValueMetaInterface meta, ImmutableBytesWritable obj)
      throws TypeConversionException {
    // returning the ImmutableBytesWritable object rather than its payload allows
    // clients to type check
    return obj;
  }
}
