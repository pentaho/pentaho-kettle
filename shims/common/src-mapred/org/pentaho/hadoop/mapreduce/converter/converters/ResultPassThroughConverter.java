package org.pentaho.hadoop.mapreduce.converter.converters;

import java.io.IOException;

import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Writables;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.hadoop.mapreduce.converter.TypeConversionException;
import org.pentaho.hadoop.mapreduce.converter.spi.ITypeConverter;

/**
 * Dummy "converter" that passes through Result objects as Object. This will allow
 * them to be carried in PDI rows as TYPE_SERIALIZABLE
 * 
 * @author Mark Hall (mhall{[at]}pentaho{[dot]}com)
 * @version $Revision$
 */
public class ResultPassThroughConverter implements ITypeConverter<Result, Object> {

  public boolean canConvert(Class from, Class to) {
    return Result.class.equals(from) && Object.class.equals(to);
  }

  public Object convert(ValueMetaInterface meta, Result obj)
      throws TypeConversionException { 
    
    // we have to make a copy here because the TableRecordReader's next()
    // method in the mapred package re-uses the Result object. This is fine
    // for sequential apps, but not good for multi-threaded mappers.

    Result newResult = new Result();
    try {
      Writables.copyWritable(obj, newResult);
    } catch (IOException ex) {
      throw new TypeConversionException("Problem copying result object!", ex);
    }
    return newResult;
  }

}
