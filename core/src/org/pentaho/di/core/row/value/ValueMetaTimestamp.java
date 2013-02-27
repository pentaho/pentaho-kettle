package org.pentaho.di.core.row.value;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.pentaho.di.compatibility.Value;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.database.DatabaseInterface;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.row.ValueMetaInterface;

public class ValueMetaTimestamp extends ValueMetaDate {
	
	public ValueMetaTimestamp() {
		this(null);
	}
	
	public ValueMetaTimestamp(String name) {
		super(name, ValueMetaInterface.TYPE_TIMESTAMP);
	}
	
	@Override
	public Date getDate(Object object) throws KettleValueException {
	  Timestamp timestamp = getTimestamp(object);
	  if (timestamp==null) {
	    return null;
	  }
	  return timestamp;
	}
	
	@Override
	public Long getInteger(Object object) throws KettleValueException {
	  Timestamp timestamp = getTimestamp(object);
    if (timestamp==null) {
      return null;
    }

    long ms = timestamp.getTime();
	  return ms;
	}

	@Override
  public Double getNumber(Object object) throws KettleValueException {
    Timestamp timestamp = getTimestamp(object);
    if (timestamp==null) {
      return null;
    }
    long ms = timestamp.getTime();
    return Long.valueOf(ms).doubleValue();
  }

	@Override
	public BigDecimal getBigNumber(Object object) throws KettleValueException {
    Timestamp timestamp = getTimestamp(object);
    if (timestamp==null) {
      return null;
    }
    BigDecimal nanos = BigDecimal.valueOf(timestamp.getTime()).multiply( BigDecimal.valueOf(1000000000L) ).add( BigDecimal.valueOf(timestamp.getNanos()) );
    return nanos;
	}
	 
	@Override
	public Boolean getBoolean(Object object) throws KettleValueException {
	  throw new KettleValueException(toStringMeta()+": it's not possible to convert from Timestamp to Boolean");
	}
	
	@Override
	public String getString(Object object) throws KettleValueException {
	  return convertTimestampToString(getTimestamp(object));
	}
	
	public Timestamp getTimestamp(Object object) throws KettleValueException {
    if (object == null) // NULL
    {
      return null;
    }
    switch (type) {
    case TYPE_TIMESTAMP:
      switch (storageType) {
      case STORAGE_TYPE_NORMAL:
        return (Timestamp) object;
      case STORAGE_TYPE_BINARY_STRING:
        return (Timestamp) convertBinaryStringToNativeType((byte[]) object);
      case STORAGE_TYPE_INDEXED:
        return (Timestamp) index[((Integer) object).intValue()];
      default:
        throw new KettleValueException(toString() + " : Unknown storage type " + storageType + " specified.");
      }
    case TYPE_STRING:
      switch (storageType) {
      case STORAGE_TYPE_NORMAL:
        return convertStringToTimestamp((String) object);
      case STORAGE_TYPE_BINARY_STRING:
        return convertStringToTimestamp((String) convertBinaryStringToNativeType((byte[]) object));
      case STORAGE_TYPE_INDEXED:
        return convertStringToTimestamp((String) index[((Integer) object).intValue()]);
      default:
        throw new KettleValueException(toString() + " : Unknown storage type " + storageType + " specified.");
      }
    case TYPE_NUMBER:
      switch (storageType) {
      case STORAGE_TYPE_NORMAL:
        return convertNumberToTimestamp((Double) object);
      case STORAGE_TYPE_BINARY_STRING:
        return convertNumberToTimestamp((Double) convertBinaryStringToNativeType((byte[]) object));
      case STORAGE_TYPE_INDEXED:
        return convertNumberToTimestamp((Double) index[((Integer) object).intValue()]);
      default:
        throw new KettleValueException(toString() + " : Unknown storage type " + storageType + " specified.");
      }
    case TYPE_INTEGER:
      switch (storageType) {
      case STORAGE_TYPE_NORMAL:
        return convertIntegerToTimestamp((Long) object);
      case STORAGE_TYPE_BINARY_STRING:
        return convertIntegerToTimestamp((Long) convertBinaryStringToNativeType((byte[]) object));
      case STORAGE_TYPE_INDEXED:
        return convertIntegerToTimestamp((Long) index[((Integer) object).intValue()]);
      default:
        throw new KettleValueException(toString() + " : Unknown storage type " + storageType + " specified.");
      }
    case TYPE_BIGNUMBER:
      switch (storageType) {
      case STORAGE_TYPE_NORMAL:
        return convertBigNumberToTimestamp((BigDecimal) object);
      case STORAGE_TYPE_BINARY_STRING:
        return convertBigNumberToTimestamp((BigDecimal) convertBinaryStringToNativeType((byte[]) object));
      case STORAGE_TYPE_INDEXED:
        return convertBigNumberToTimestamp((BigDecimal) index[((Integer) object).intValue()]);
      default:
        throw new KettleValueException(toString() + " : Unknown storage type " + storageType + " specified.");
      }
    case TYPE_BOOLEAN:
      throw new KettleValueException(toString() + " : I don't know how to convert a boolean to a timestamp.");
    case TYPE_BINARY:
      throw new KettleValueException(toString() + " : I don't know how to convert a binary value to timestamp.");
    case TYPE_SERIALIZABLE:
      throw new KettleValueException(toString() + " : I don't know how to convert a serializable value to timestamp.");

    default:
      throw new KettleValueException(toString() + " : Unknown type " + type + " specified.");
    }
  }
	
  protected Timestamp convertBigNumberToTimestamp(BigDecimal bd) {
    if (bd==null) {
      return null;
    }
    return convertIntegerToTimestamp(bd.longValue());
  }

  protected Timestamp convertNumberToTimestamp(Double d) {
    if (d==null) {
      return null;
    }
    long nanos = d.longValue();
    
    return convertIntegerToTimestamp(nanos);
  }

  protected Timestamp convertIntegerToTimestamp(Long nanos) {
    if (nanos==null) {
      return null;
    }

    long msSinceEpoch = nanos/1000000;
    int leftNanos = (int) (nanos - (msSinceEpoch*1000000));
    Timestamp timestamp = new Timestamp(msSinceEpoch);
    timestamp.setNanos(leftNanos);
    
    return timestamp;
  }

  protected synchronized Timestamp convertStringToTimestamp(String string) throws KettleValueException {
    // See if trimming needs to be performed before conversion
    //
    string = Const.trimToType(string, getTrimType()); 

    if (Const.isEmpty(string))
      return null;

    try {
      return Timestamp.valueOf(string);
    } catch (IllegalArgumentException e) {
      throw new KettleValueException(toString() + " : couldn't convert string [" + string
          + "] to a timestamp, expecting format [yyyy-mm-dd hh:mm:ss.ffffff]", e);
    }
  }

  protected synchronized String convertTimestampToString(Timestamp timestamp) throws KettleValueException {

    if (timestamp==null) {
      return null;
    }
    
    String dateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(timestamp);
    dateTime+="."+new DecimalFormat("000000000").format(timestamp.getNanos());
    // return timestamp.toString();
    
    return dateTime;
  }
  
  public Object convertDataFromString(String pol, ValueMetaInterface convertMeta, String nullIf, String ifNull, int trim_type) throws KettleValueException {
    // null handling and conversion of value to null
    //
    String null_value = nullIf;
    if (null_value == null) {
      switch (convertMeta.getType()) {
      case Value.VALUE_TYPE_BOOLEAN:
        null_value = Const.NULL_BOOLEAN;
        break;
      case Value.VALUE_TYPE_STRING:
        null_value = Const.NULL_STRING;
        break;
      case Value.VALUE_TYPE_BIGNUMBER:
        null_value = Const.NULL_BIGNUMBER;
        break;
      case Value.VALUE_TYPE_NUMBER:
        null_value = Const.NULL_NUMBER;
        break;
      case Value.VALUE_TYPE_INTEGER:
        null_value = Const.NULL_INTEGER;
        break;
      case Value.VALUE_TYPE_DATE:
        null_value = Const.NULL_DATE;
        break;
      case Value.VALUE_TYPE_BINARY:
        null_value = Const.NULL_BINARY;
        break;
      default:
        null_value = Const.NULL_NONE;
        break;
      }
    }

    // See if we need to convert a null value into a String
    // For example, we might want to convert null into "Empty".
    //
    if (!Const.isEmpty(ifNull)) {
      // Note that you can't pull the pad method up here as a nullComp variable
      // because you could get an NPE since you haven't checked isEmpty(pol)
      // yet!
      if (Const.isEmpty(pol) || pol.equalsIgnoreCase(Const.rightPad(new StringBuffer(null_value), pol.length()))) {
        pol = ifNull;
      }
    }

    // See if the polled value is empty
    // In that case, we have a null value on our hands...
    //
    if (Const.isEmpty(pol)) {
      return null;
    } else {
      // if the null_value is specified, we try to match with that.
      //
      if (!Const.isEmpty(null_value)) {
        if (null_value.length() <= pol.length()) {
          // If the polled value is equal to the spaces right-padded null_value,
          // we have a match
          //
          if (pol.equalsIgnoreCase(Const.rightPad(new StringBuffer(null_value), pol.length()))) {
            return null;
          }
        }
      } else {
        // Verify if there are only spaces in the polled value...
        // We consider that empty as well...
        //
        if (Const.onlySpaces(pol)) {
          return null;
        }
      }
    }

    // Trimming
    switch (trim_type) {
    case ValueMetaInterface.TRIM_TYPE_LEFT: {
      StringBuffer strpol = new StringBuffer(pol);
      while (strpol.length() > 0 && strpol.charAt(0) == ' ')
        strpol.deleteCharAt(0);
      pol = strpol.toString();
    }
      break;
    case ValueMetaInterface.TRIM_TYPE_RIGHT: {
      StringBuffer strpol = new StringBuffer(pol);
      while (strpol.length() > 0 && strpol.charAt(strpol.length() - 1) == ' ')
        strpol.deleteCharAt(strpol.length() - 1);
      pol = strpol.toString();
    }
      break;
    case ValueMetaInterface.TRIM_TYPE_BOTH:
      StringBuffer strpol = new StringBuffer(pol);
      {
        while (strpol.length() > 0 && strpol.charAt(0) == ' ')
          strpol.deleteCharAt(0);
        while (strpol.length() > 0 && strpol.charAt(strpol.length() - 1) == ' ')
          strpol.deleteCharAt(strpol.length() - 1);
        pol = strpol.toString();
      }
      break;
    default:
      break;
    }

    // On with the regular program...
    // Simply call the ValueMeta routines to do the conversion
    // We need to do some effort here: copy all
    //
    return convertData(convertMeta, pol);
  }
  
  public Timestamp convertDateToTimestamp(Date date) throws KettleValueException {
    if (date==null) return null;
    
    return new Timestamp(date.getTime());
  }
  
  /**
   * Convert the specified data to the data type specified in this object.
   * 
   * @param meta2
   *          the metadata of the object to be converted
   * @param data2
   *          the data of the object to be converted
   * @return the object in the data type of this value metadata object
   * @throws KettleValueException
   *           in case there is a data conversion error
   */
  public Object convertData(ValueMetaInterface meta2, Object data2) throws KettleValueException {
    switch(meta2.getType()) {
    case TYPE_STRING: return convertStringToTimestamp(meta2.getString(data2)); 
    case TYPE_INTEGER: return convertIntegerToTimestamp(meta2.getInteger(data2));
    case TYPE_NUMBER: return convertNumberToTimestamp(meta2.getNumber(data2));
    case TYPE_DATE: return convertDateToTimestamp(meta2.getDate(data2));
    case TYPE_BIGNUMBER: return convertBigNumberToTimestamp(meta2.getBigNumber(data2));
    default: 
      throw new KettleValueException(meta2.toStringMeta()+" : can't be converted to a timestamp");
    }
  }
  
  @Override
  public Object cloneValueData(Object object) throws KettleValueException {
    Timestamp timestamp = getTimestamp(object);
    if (timestamp==null) return null;
    
    Timestamp clone = new Timestamp(timestamp.getTime());
    clone.setNanos(timestamp.getNanos());
    return clone;
  }
  
  @Override
  public ValueMetaInterface getValueFromSQLType(DatabaseMeta databaseMeta, String name, ResultSetMetaData rm, int index, boolean ignoreLength, boolean lazyConversion) throws KettleDatabaseException {

    try {
      int type = rm.getColumnType(index);
      if (type == java.sql.Types.TIMESTAMP) {
        int length = rm.getScale(index);
        ValueMetaInterface valueMeta;
        if (databaseMeta.supportsTimestampDataType()) {
          valueMeta = new ValueMetaTimestamp(name);
        } else {
          valueMeta = new ValueMetaDate(name);
        }
        valueMeta.setLength(length);
        
        // Also get original column details, comment, etc.
        //
        getOriginalColumnMetadata(valueMeta, rm, index, ignoreLength);
        
        return valueMeta;
      }
      
      return null;
    } catch(Exception e) {
      throw new KettleDatabaseException("Error evaluating timestamp value metadata", e);
    }
  }
  
  @Override
  public Object getValueFromResultSet(DatabaseInterface databaseInterface, ResultSet resultSet, int index)
      throws KettleDatabaseException {

    try {
      
      return resultSet.getTimestamp(index+1);
      
    } catch(Exception e) {
      throw new KettleDatabaseException(toStringMeta()+" : Unable to get timestamp from resultset at index "+index, e);
    }
    
  }
  
  @Override
  public void setPreparedStatementValue(DatabaseMeta databaseMeta, PreparedStatement preparedStatement, int index, Object data) throws KettleDatabaseException {
    
    try {
      
      preparedStatement.setTimestamp(index, getTimestamp(data));
      
    } catch(Exception e) {
      throw new KettleDatabaseException(toStringMeta()+" : Unable to set value on prepared statement on index "+index, e);
    }
    
  }
}
