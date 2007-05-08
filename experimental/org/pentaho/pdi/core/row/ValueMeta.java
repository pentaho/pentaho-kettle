package org.pentaho.pdi.core.row;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.exception.KettleValueException;

public class ValueMeta implements ValueMetaInterface
{
    public static final String DEFAULT_DATE_FORMAT_MASK = "yyyy/MM/dd HH:mm:ss.SSS";
    
    private String   name;
    private int      length;
    private int      precision;
    private int      type;
    private int      storageType;
    private String   origin;
    private String   comments;
    private Object[] index;
    private String   conversionMask;
    private String   stringEncoding;
    private String   decimalSymbol;
    private String   groupingSymbol;
    private String   currencySymbol;
    
    public ValueMeta()
    {
        this(null, ValueMetaInterface.TYPE_NONE, -1, -1);
    }
    
    public ValueMeta(String name)
    {
        this(name, ValueMetaInterface.TYPE_NONE, -1, -1);
    }

    public ValueMeta(String name, int type)
    {
        this(name, type, -1, -1);
    }
    
    public ValueMeta(String name, int type, int length)
    {
        this(name, type, length, -1);
    }
    
    public ValueMeta(String name, int type, int length, int precision)
    {
        this.name = name;
        this.type = type;
        this.length = length;
        this.precision = precision;
        this.storageType=STORAGE_TYPE_NORMAL;
    }
    
    public Object clone()
    {
        try
        {
            return super.clone();
        }
        catch (CloneNotSupportedException e)
        {
            return null;
        }
    }

    /**
     * @return the comments
     */
    public String getComments()
    {
        return comments;
    }
    
    /**
     * @param comments the comments to set
     */
    public void setComments(String comments)
    {
        this.comments = comments;
    }
    
    /**
     * @return the index
     */
    public Object[] getIndex()
    {
        return index;
    }
    
    /**
     * @param index the index to set
     */
    public void setIndex(Object[] index)
    {
        this.index = index;
    }
    
    /**
     * @return the length
     */
    public int getLength()
    {
        return length;
    }
    
    /**
     * @param length the length to set
     */
    public void setLength(int length)
    {
        this.length = length;
    }
    
    /**
     * @return the name
     */
    public String getName()
    {
        return name;
    }
    
    /**
     * @param name the name to set
     */
    public void setName(String name)
    {
        this.name = name;
    }
    
    /**
     * @return the origin
     */
    public String getOrigin()
    {
        return origin;
    }
    
    /**
     * @param origin the origin to set
     */
    public void setOrigin(String origin)
    {
        this.origin = origin;
    }
    
    /**
     * @return the precision
     */
    public int getPrecision()
    {
        return precision;
    }
    
    /**
     * @param precision the precision to set
     */
    public void setPrecision(int precision)
    {
        this.precision = precision;
    }
    
    /**
     * @return the storageType
     */
    public int getStorageType()
    {
        return storageType;
    }
    
    /**
     * @param storageType the storageType to set
     */
    public void setStorageType(int storageType)
    {
        this.storageType = storageType;
    }
    
    /**
     * @return the type
     */
    public int getType()
    {
        return type;
    }
    
    /**
     * @param type the type to set
     */
    public void setType(int type)
    {
        this.type = type;
    }
    
    /**
     * @return the conversionMask
     */
    public String getConversionMask()
    {
        return conversionMask;
    }
    /**
     * @param conversionMask the conversionMask to set
     */
    public void setConversionMask(String conversionMask)
    {
        this.conversionMask = conversionMask;
    }
    

    /**
     * @return the encoding
     */
    public String getStringEncoding()
    {
        return stringEncoding;
    }

    /**
     * @param encoding the encoding to set
     */
    public void setStringEncoding(String encoding)
    {
        this.stringEncoding = encoding;
    }
    
    /**
     * @return the decimalSymbol
     */
    public String getDecimalSymbol()
    {
        return decimalSymbol;
    }

    /**
     * @param decimalSymbol the decimalSymbol to set
     */
    public void setDecimalSymbol(String decimalSymbol)
    {
        this.decimalSymbol = decimalSymbol;
    }

    /**
     * @return the groupingSymbol
     */
    public String getGroupingSymbol()
    {
        return groupingSymbol;
    }

    /**
     * @param groupingSymbol the groupingSymbol to set
     */
    public void setGroupingSymbol(String groupingSymbol)
    {
        this.groupingSymbol = groupingSymbol;
    }
    

    /**
     * @return the currencySymbol
     */
    public String getCurrencySymbol()
    {
        return currencySymbol;
    }

    /**
     * @param currencySymbol the currencySymbol to set
     */
    public void setCurrencySymbol(String currencySymbol)
    {
        this.currencySymbol = currencySymbol;
    }

    // DATE + STRING
    
    private String convertDateToString(Date date)
    {
        if (date==null) return null;
        
        return getSimpleDateFormat().format(date);
    }

    private Date convertStringToDate(String string) throws KettleValueException
    {
        if (string==null) return null;
        
        try
        {
            return getSimpleDateFormat().parse(string);
        }
        catch (ParseException e)
        {
            throw new KettleValueException("Unable to convert string ["+string+"] to a date", e);
        }
    }

    // DATE + NUMBER 

    private Double convertDateToNumber(Date date)
    {
        return new Double( date.getTime() );
    }

    private Date convertNumberToDate(Double number)
    {
        return new Date( number.longValue() );
    }

    // DATE + INTEGER
    
    private Long convertDateToInteger(Date date)
    {
        return new Long( date.getTime() );
    }

    private Date convertIntegerToDate(Long number)
    {
        return new Date( number.longValue() );
    }

    // DATE + BIGNUMBER
    
    private BigDecimal convertDateToBigNumber(Date date)
    {
        return new BigDecimal( date.getTime() );
    }

    private Date convertBigNumberToDate(BigDecimal number)
    {
        return new Date( number.longValue() );
    }

    private String convertNumberToString(Double number) throws KettleValueException
    {
        if (number==null) return null;
        
        try
        {
            return getDecimalFormat().format(number);
        }
        catch(Exception e)
        {
            throw new KettleValueException("Couldn't convert Number to String ", e);
        }
    }
    
    private Double convertStringToNumber(String string) throws KettleValueException
    {
        if (string==null) return null;
        
        try
        {
            return new Double( getDecimalFormat().parse(string).doubleValue() );
        }
        catch(Exception e)
        {
            throw new KettleValueException("Couldn't convert String to number ", e);
        }
    }
    
    public SimpleDateFormat getSimpleDateFormat()
    {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat();
        if (Const.isEmpty(conversionMask))
        {
            simpleDateFormat = new SimpleDateFormat(DEFAULT_DATE_FORMAT_MASK);
        }
        else
        {
            simpleDateFormat = new SimpleDateFormat(conversionMask);
        }
        return simpleDateFormat;
    }

    public DecimalFormat getDecimalFormat()
    {
        NumberFormat         nf  = NumberFormat.getInstance();
        DecimalFormat        df  = (DecimalFormat)nf;
        DecimalFormatSymbols dfs = df.getDecimalFormatSymbols();
    
        if (!Const.isEmpty(currencySymbol)) dfs.setCurrencySymbol( currencySymbol );
        if (!Const.isEmpty(groupingSymbol)) dfs.setGroupingSeparator( groupingSymbol.charAt(0) );
        if (!Const.isEmpty(decimalSymbol)) dfs.setDecimalSeparator( decimalSymbol.charAt(0) );
        df.setDecimalFormatSymbols(dfs);
        if (!Const.isEmpty(conversionMask)) df.applyPattern(conversionMask);
        
        return df;
    }

    private String convertIntegerToString(Long number) throws KettleValueException
    {
        if (number==null) return null;

        try
        {
            return getDecimalFormat().format(number);
        }
        catch(Exception e)
        {
            throw new KettleValueException("Couldn't convert Long to String ", e);
        }
    }
    
    private Long convertStringToInteger(String string) throws KettleValueException
    {
        if (string==null) return null;

        try
        {
            return new Long( getDecimalFormat().parse(string).longValue() );
        }
        catch(Exception e)
        {
            throw new KettleValueException("Couldn't convert String to Integer", e);
        }
    }
    
    private String convertBigNumberToString(BigDecimal number) throws KettleValueException
    {
        if (number==null) return null;

        String string = number.toString();
        if (!".".equalsIgnoreCase(decimalSymbol))
        {
            string = Const.replace(string, ".", decimalSymbol.substring(0, 1));
        }
        return string;
    }
    
    private BigDecimal convertStringToBigNumber(String string) throws KettleValueException
    {
        if (string==null) return null;

        if (!".".equalsIgnoreCase(decimalSymbol))
        {
            string = Const.replace(string, decimalSymbol.substring(0, 1), ".");
        }
        return new BigDecimal( string );
    }

    // BOOLEAN + STRING
    
    private String convertBooleanToString(Boolean bool)
    {
        if (length>=3)
        {
            return bool.booleanValue()?"true":"false";
        }
        else
        {
            return bool.booleanValue()?"Y":"N";
        }
    }
    
    private Boolean convertStringToBoolean(String string)
    {
        return new Boolean( "Y".equalsIgnoreCase(string) || "TRUE".equalsIgnoreCase(string) || "YES".equalsIgnoreCase(string) );
    }
    
    // BOOLEAN + NUMBER
    
    private Double convertBooleanToNumber(Boolean bool)
    {
        return new Double( bool.booleanValue() ? 1.0 : 0.0 );
    }
    
    private Boolean convertNumberToBoolean(Double number)
    {
        return new Boolean( number.intValue() != 0 );
    }

    // BOOLEAN + INTEGER

    private Long convertBooleanToInteger(Boolean bool)
    {
        return new Long( bool.booleanValue() ? 1L : 0L );
    }

    private Boolean convertIntegerToBoolean(Long number)
    {
        return new Boolean( number.longValue() != 0 );
    }
    
    // BOOLEAN + BIGNUMBER
    
    private BigDecimal convertBooleanToBigNumber(Boolean bool)
    {
        return new BigDecimal( bool.booleanValue() ? 1.0 : 0.0 );
    }
    
    private Boolean convertBigNumberToBoolean(BigDecimal number)
    {
        return new Boolean( number.intValue() != 0 );
    }
    
    
    private String convertBinaryToString(byte[] binary) throws KettleValueException
    {
        if (Const.isEmpty(stringEncoding))
        {
            return new String(binary);
        }
        else
        {
            try
            {
                return new String(binary, stringEncoding);
            }
            catch(UnsupportedEncodingException e)
            {
                throw new KettleValueException("Unable to convert binary value to String with specified string encoding ["+stringEncoding+"]", e);
            }
        }
    }

    private byte[] convertStringToBinary(String string) throws KettleValueException
    {
        if (Const.isEmpty(stringEncoding))
        {
            return string.getBytes();
        }
        else
        {
            try
            {
                return string.getBytes(stringEncoding);
            }
            catch(UnsupportedEncodingException e)
            {
                throw new KettleValueException("Unable to convert String to Binary with specified string encoding ["+stringEncoding+"]", e);
            }
        }
    }

    public Object cloneValueData(Object object) throws KettleValueException
    {
        if (object==null) return null;
        
        switch(storageType)
        {
        case ValueMetaInterface.STORAGE_TYPE_NORMAL:
            switch(type)
            {
            case ValueMetaInterface.TYPE_STRING : 
                return object; 
            case ValueMetaInterface.TYPE_INTEGER : 
                return new Long( ((Long)object).longValue() ); 
            case ValueMetaInterface.TYPE_NUMBER : 
                return new Double( ((Double)object).doubleValue() ); 
            case ValueMetaInterface.TYPE_BIGNUMBER: 
                return new BigDecimal( ((BigDecimal)object).toString() ); 
            case ValueMetaInterface.TYPE_DATE: 
                return new Date( ((Date)object).getTime() );
            case ValueMetaInterface.TYPE_BOOLEAN: 
                return new Boolean( ((Boolean)object).booleanValue() );
            case ValueMetaInterface.TYPE_BINARY: 
                byte[] source = (byte[])object;
                byte[] target = new byte[source.length];
                for (int x=0;x<source.length;x++) target[x] = source[x]; 
                return target;
            case ValueMetaInterface.TYPE_SERIALIZABLE:
                return object; // can't really clone this one.
            default: 
                throw new KettleValueException("Unknown type "+type+" specified.");
            }
            
        case ValueMetaInterface.STORAGE_TYPE_INDEXED:
            // This is easier: we always expect an Long here, so we return the same Long. 
            return new Long( ((Integer)object).intValue() );
            
        default: 
            throw new KettleValueException("Unknown storage type "+storageType+" specified.");
        }
    }

    public String getString(Object object) throws KettleValueException
    {
        if (object==null) // NULL 
        {
            return null;
        }
        switch(type)
        {
        case TYPE_STRING:
            switch(storageType)
            {
            case STORAGE_TYPE_NORMAL:       return (String)object;
            case STORAGE_TYPE_INDEXED:      return (String) index[((Integer)object).intValue()]; 
            default: throw new KettleValueException("Unknown storage type "+storageType+" specified.");
            }
        case TYPE_DATE:
            switch(storageType)
            {
            case STORAGE_TYPE_NORMAL:       return convertDateToString((Date)object);
            case STORAGE_TYPE_INDEXED:      return convertDateToString((Date)index[((Integer)object).intValue()]);  
            default: throw new KettleValueException("Unknown storage type "+storageType+" specified.");
            }
        case TYPE_NUMBER:
            switch(storageType)
            {
            case STORAGE_TYPE_NORMAL:       return convertNumberToString((Double)object);
            case STORAGE_TYPE_INDEXED:      return convertNumberToString((Double)index[((Integer)object).intValue()]);
            default: throw new KettleValueException("Unknown storage type "+storageType+" specified.");
            }
        case TYPE_INTEGER:
            switch(storageType)
            {
            case STORAGE_TYPE_NORMAL:       return convertIntegerToString((Long)object);
            case STORAGE_TYPE_INDEXED:      return convertIntegerToString((Long)index[((Integer)object).intValue()]);
            default: throw new KettleValueException("Unknown storage type "+storageType+" specified.");
            }
        case TYPE_BIGNUMBER:
            switch(storageType)
            {
            case STORAGE_TYPE_NORMAL:       return convertBigNumberToString((BigDecimal)object);
            case STORAGE_TYPE_INDEXED:      return convertBigNumberToString((BigDecimal)index[((Integer)object).intValue()]);
            default: throw new KettleValueException("Unknown storage type "+storageType+" specified.");
            }
        case TYPE_BOOLEAN:
            switch(storageType)
            {
            case STORAGE_TYPE_NORMAL:       return convertBooleanToString((Boolean)object);
            case STORAGE_TYPE_INDEXED:      return convertBooleanToString((Boolean)index[((Integer)object).intValue()]);
            default: throw new KettleValueException("Unknown storage type "+storageType+" specified.");
            }
        case TYPE_BINARY:
            switch(storageType)
            {
            case STORAGE_TYPE_NORMAL:       return convertBinaryToString((byte[])object);
            case STORAGE_TYPE_INDEXED:      return convertBinaryToString((byte[])index[((Integer)object).intValue()]);
            default: throw new KettleValueException("Unknown storage type "+storageType+" specified.");
            }
            
        case TYPE_SERIALIZABLE:
            switch(storageType)
            {
            case STORAGE_TYPE_NORMAL:       return object.toString(); // just go for the default toString()
            case STORAGE_TYPE_INDEXED:      return index[((Integer)object).intValue()].toString(); // just go for the default toString()
            default: throw new KettleValueException("Unknown storage type "+storageType+" specified.");
            }
            
        default: 
            throw new KettleValueException("Unknown type "+type+" specified.");
        }
    }

    public Double getNumber(Object object) throws KettleValueException
    {
        if (object==null) // NULL 
        {
            return null;
        }
        switch(type)
        {
        case TYPE_NUMBER:
            switch(storageType)
            {
            case STORAGE_TYPE_NORMAL:       return (Double)object;
            case STORAGE_TYPE_INDEXED:      return (Double)index[((Integer)object).intValue()];
            default: throw new KettleValueException("Unknown storage type "+storageType+" specified.");
            }
        case TYPE_STRING:
            switch(storageType)
            {
            case STORAGE_TYPE_NORMAL:       return convertStringToNumber((String)object);
            case STORAGE_TYPE_INDEXED:      return convertStringToNumber((String) index[((Integer)object).intValue()]); 
            default: throw new KettleValueException("Unknown storage type "+storageType+" specified.");
            }
        case TYPE_DATE:
            switch(storageType)
            {
            case STORAGE_TYPE_NORMAL:       return convertDateToNumber((Date)object);
            case STORAGE_TYPE_INDEXED:      return new Double( ((Date)index[((Integer)object).intValue()]).getTime() );  
            default: throw new KettleValueException("Unknown storage type "+storageType+" specified.");
            }
        case TYPE_INTEGER:
            switch(storageType)
            {
            case STORAGE_TYPE_NORMAL:       return new Double( ((Long)object).doubleValue() );
            case STORAGE_TYPE_INDEXED:      return new Double( ((Long)index[((Integer)object).intValue()]).doubleValue() );
            default: throw new KettleValueException("Unknown storage type "+storageType+" specified.");
            }
        case TYPE_BIGNUMBER:
            switch(storageType)
            {
            case STORAGE_TYPE_NORMAL:       return new Double( ((BigDecimal)object).doubleValue() );
            case STORAGE_TYPE_INDEXED:      return new Double( ((BigDecimal)index[((Integer)object).intValue()]).doubleValue() );
            default: throw new KettleValueException("Unknown storage type "+storageType+" specified.");
            }
        case TYPE_BOOLEAN:
            switch(storageType)
            {
            case STORAGE_TYPE_NORMAL:       return convertBooleanToNumber( (Boolean)object );
            case STORAGE_TYPE_INDEXED:      return convertBooleanToNumber( (Boolean)index[((Integer)object).intValue()] );
            default: throw new KettleValueException("Unknown storage type "+storageType+" specified.");
            }
        case TYPE_BINARY:
            throw new KettleValueException("I don't know how to convert binary values to numbers.");
        case TYPE_SERIALIZABLE:
            throw new KettleValueException("I don't know how to convert serializable values to numbers.");
        default:
            throw new KettleValueException("Unknown type "+type+" specified.");
        }
    }

    public Long getInteger(Object object) throws KettleValueException
    {
        if (object==null) // NULL 
        {
            return null;
        }
        switch(type)
        {
        case TYPE_INTEGER:
            switch(storageType)
            {
            case STORAGE_TYPE_NORMAL:       return (Long)object;
            case STORAGE_TYPE_INDEXED:      return (Long)index[((Integer)object).intValue()];
            default: throw new KettleValueException("Unknown storage type "+storageType+" specified.");
            }
        case TYPE_STRING:
            switch(storageType)
            {
            case STORAGE_TYPE_NORMAL:       return convertStringToInteger((String)object);
            case STORAGE_TYPE_INDEXED:      return convertStringToInteger((String) index[((Integer)object).intValue()]); 
            default: throw new KettleValueException("Unknown storage type "+storageType+" specified.");
            }
        case TYPE_NUMBER:
            switch(storageType)
            {
            case STORAGE_TYPE_NORMAL:       return new Long( ((Double)object).longValue() );
            case STORAGE_TYPE_INDEXED:      return new Long( ((Double)index[((Integer)object).intValue()]).longValue() );
            default: throw new KettleValueException("Unknown storage type "+storageType+" specified.");
            }
        case TYPE_DATE:
            switch(storageType)
            {
            case STORAGE_TYPE_NORMAL:       return convertDateToInteger( (Date)object);
            case STORAGE_TYPE_INDEXED:      return convertDateToInteger( (Date)index[((Integer)object).intValue()]);  
            default: throw new KettleValueException("Unknown storage type "+storageType+" specified.");
            }
        case TYPE_BIGNUMBER:
            switch(storageType)
            {
            case STORAGE_TYPE_NORMAL:       return new Long( ((BigDecimal)object).longValue() );
            case STORAGE_TYPE_INDEXED:      return new Long( ((BigDecimal)index[((Integer)object).intValue()]).longValue() );
            default: throw new KettleValueException("Unknown storage type "+storageType+" specified.");
            }
        case TYPE_BOOLEAN:
            switch(storageType)
            {
            case STORAGE_TYPE_NORMAL:       return convertBooleanToInteger( (Boolean)object );
            case STORAGE_TYPE_INDEXED:      return convertBooleanToInteger( (Boolean)index[((Integer)object).intValue()] );
            default: throw new KettleValueException("Unknown storage type "+storageType+" specified.");
            }
        case TYPE_BINARY:
            throw new KettleValueException("I don't know how to convert binary values to integers.");
        case TYPE_SERIALIZABLE:
            throw new KettleValueException("I don't know how to convert serializable values to integers.");
        default:
            throw new KettleValueException("Unknown type "+type+" specified.");
        }
    }

    public BigDecimal getBigNumber(Object object) throws KettleValueException
    {
        if (object==null) // NULL 
        {
            return null;
        }
        switch(type)
        {
        case TYPE_BIGNUMBER:
            switch(storageType)
            {
            case STORAGE_TYPE_NORMAL:       return (BigDecimal)object;
            case STORAGE_TYPE_INDEXED:      return (BigDecimal)index[((Integer)object).intValue()];
            default: throw new KettleValueException("Unknown storage type "+storageType+" specified.");
            }
        case TYPE_STRING:
            switch(storageType)
            {
            case STORAGE_TYPE_NORMAL:       return convertStringToBigNumber((String)object);
            case STORAGE_TYPE_INDEXED:      return convertStringToBigNumber((String) index[((Integer)object).intValue()]); 
            default: throw new KettleValueException("Unknown storage type "+storageType+" specified.");
            }
        case TYPE_INTEGER:
            switch(storageType)
            {
            case STORAGE_TYPE_NORMAL:       return new BigDecimal( ((Long)object).doubleValue() );
            case STORAGE_TYPE_INDEXED:      return new BigDecimal( ((Long)index[((Integer)object).intValue()]).doubleValue() );
            default: throw new KettleValueException("Unknown storage type "+storageType+" specified.");
            }
        case TYPE_NUMBER:
            switch(storageType)
            {
            case STORAGE_TYPE_NORMAL:       return new BigDecimal( ((Double)object).doubleValue() );
            case STORAGE_TYPE_INDEXED:      return new BigDecimal( ((Double)index[((Integer)object).intValue()]).doubleValue() );
            default: throw new KettleValueException("Unknown storage type "+storageType+" specified.");
            }
        case TYPE_DATE:
            switch(storageType)
            {
            case STORAGE_TYPE_NORMAL:       return convertDateToBigNumber( (Date)object );
            case STORAGE_TYPE_INDEXED:      return convertDateToBigNumber( (Date)index[((Integer)object).intValue()] );  
            default: throw new KettleValueException("Unknown storage type "+storageType+" specified.");
            }
        case TYPE_BOOLEAN:
            switch(storageType)
            {
            case STORAGE_TYPE_NORMAL:       return convertBooleanToBigNumber( (Boolean)object );
            case STORAGE_TYPE_INDEXED:      return convertBooleanToBigNumber( (Boolean)index[((Integer)object).intValue()] );
            default: throw new KettleValueException("Unknown storage type "+storageType+" specified.");
            }
        case TYPE_BINARY:
            throw new KettleValueException("I don't know how to convert binary values to integers.");
        case TYPE_SERIALIZABLE:
            throw new KettleValueException("I don't know how to convert serializable values to integers.");
        default:
            throw new KettleValueException("Unknown type "+type+" specified.");
        }
    }
    
    public Boolean getBoolean(Object object) throws KettleValueException
    {
        if (object==null) // NULL 
        {
            return null;
        }
        switch(type)
        {
        case TYPE_BOOLEAN:
            switch(storageType)
            {
            case STORAGE_TYPE_NORMAL:       return (Boolean)object;
            case STORAGE_TYPE_INDEXED:      return (Boolean)index[((Integer)object).intValue()];
            default: throw new KettleValueException("Unknown storage type "+storageType+" specified.");
            }
        case TYPE_STRING:
            switch(storageType)
            {
            case STORAGE_TYPE_NORMAL:       return convertStringToBoolean( (String)object );
            case STORAGE_TYPE_INDEXED:      return convertStringToBoolean( (String) index[((Integer)object).intValue()] ); 
            default: throw new KettleValueException("Unknown storage type "+storageType+" specified.");
            }
        case TYPE_INTEGER:
            switch(storageType)
            {
            case STORAGE_TYPE_NORMAL:       return convertIntegerToBoolean( (Long)object );
            case STORAGE_TYPE_INDEXED:      return convertIntegerToBoolean( (Long)index[((Integer)object).intValue()] );
            default: throw new KettleValueException("Unknown storage type "+storageType+" specified.");
            }
        case TYPE_NUMBER:
            switch(storageType)
            {
            case STORAGE_TYPE_NORMAL:       return convertNumberToBoolean( (Double)object );
            case STORAGE_TYPE_INDEXED:      return convertNumberToBoolean( (Double)index[((Integer)object).intValue()] );
            default: throw new KettleValueException("Unknown storage type "+storageType+" specified.");
            }
        case TYPE_BIGNUMBER:
            switch(storageType)
            {
            case STORAGE_TYPE_NORMAL:       return convertBigNumberToBoolean( (BigDecimal)object );
            case STORAGE_TYPE_INDEXED:      return convertBigNumberToBoolean( (BigDecimal)index[((Integer)object).intValue()] );
            default: throw new KettleValueException("Unknown storage type "+storageType+" specified.");
            }
        case TYPE_DATE:
            throw new KettleValueException("I don't know how to convert date values to booleans.");
        case TYPE_BINARY:
            throw new KettleValueException("I don't know how to convert binary values to booleans.");
        case TYPE_SERIALIZABLE:
            throw new KettleValueException("I don't know how to convert serializable values to booleans.");
        default:
            throw new KettleValueException("Unknown type "+type+" specified.");
        }
    }
    
    public Date getDate(Object object) throws KettleValueException
    {
        if (object==null) // NULL 
        {
            return null;
        }
        switch(type)
        {
        case TYPE_DATE:
            switch(storageType)
            {
            case STORAGE_TYPE_NORMAL:       return (Date)object;
            case STORAGE_TYPE_INDEXED:      return (Date)index[((Integer)object).intValue()];  
            default: throw new KettleValueException("Unknown storage type "+storageType+" specified.");
            }
        case TYPE_STRING:
            switch(storageType)
            {
            case STORAGE_TYPE_NORMAL:       return convertStringToDate( (String)object );
            case STORAGE_TYPE_INDEXED:      return convertStringToDate( (String) index[((Integer)object).intValue()] ); 
            default: throw new KettleValueException("Unknown storage type "+storageType+" specified.");
            }
        case TYPE_NUMBER:
            switch(storageType)
            {
            case STORAGE_TYPE_NORMAL:       return convertNumberToDate((Double)object);
            case STORAGE_TYPE_INDEXED:      return convertNumberToDate((Double)index[((Integer)object).intValue()]);
            default: throw new KettleValueException("Unknown storage type "+storageType+" specified.");
            }
        case TYPE_INTEGER:
            switch(storageType)
            {
            case STORAGE_TYPE_NORMAL:       return convertIntegerToDate((Long)object);
            case STORAGE_TYPE_INDEXED:      return convertIntegerToDate((Long)index[((Integer)object).intValue()]);
            default: throw new KettleValueException("Unknown storage type "+storageType+" specified.");
            }
        case TYPE_BIGNUMBER:
            switch(storageType)
            {
            case STORAGE_TYPE_NORMAL:       return convertBigNumberToDate((BigDecimal)object);
            case STORAGE_TYPE_INDEXED:      return convertBigNumberToDate((BigDecimal)index[((Integer)object).intValue()]);
            default: throw new KettleValueException("Unknown storage type "+storageType+" specified.");
            }
        case TYPE_BOOLEAN:
            throw new KettleValueException("I don't know how to convert a boolean to a date.");
        case TYPE_BINARY:
            throw new KettleValueException("I don't know how to convert a binary value to date.");
        case TYPE_SERIALIZABLE:
            throw new KettleValueException("I don't know how to convert a serializable value to date.");
            
        default: 
            throw new KettleValueException("Unknown type "+type+" specified.");
        }
    }

    public byte[] getBinary(Object object) throws KettleValueException
    {
        if (object==null) // NULL 
        {
            return null;
        }
        switch(type)
        {
        case TYPE_BINARY:
            switch(storageType)
            {
            case STORAGE_TYPE_NORMAL:       return (byte[])object;
            case STORAGE_TYPE_INDEXED:      return (byte[])index[((Integer)object).intValue()];  
            default: throw new KettleValueException("Unknown storage type "+storageType+" specified.");
            }
        case TYPE_DATE:
            throw new KettleValueException("I don't know how to convert a date to binary.");
        case TYPE_STRING:
            switch(storageType)
            {
            case STORAGE_TYPE_NORMAL:       return convertStringToBinary( (String)object );
            case STORAGE_TYPE_INDEXED:      return convertStringToBinary( (String) index[((Integer)object).intValue()] ); 
            default: throw new KettleValueException("Unknown storage type "+storageType+" specified.");
            }
        case TYPE_NUMBER:
            throw new KettleValueException("I don't know how to convert a number to binary.");
        case TYPE_INTEGER:
            throw new KettleValueException("I don't know how to convert an integer to binary.");
        case TYPE_BIGNUMBER:
            throw new KettleValueException("I don't know how to convert a bignumber to binary.");
        case TYPE_BOOLEAN:
            throw new KettleValueException("I don't know how to convert a boolean to binary.");
        case TYPE_SERIALIZABLE:
            throw new KettleValueException("I don't know how to convert a serializable to binary.");
            
        default: 
            throw new KettleValueException("Unknown type "+type+" specified.");
        }
    }
    
    /**
     * Checks wheter or not the value is a String.
     * @return true if the value is a String.
     */
    public boolean isString()
    {
        return type==TYPE_STRING;
    }

    /**
     * Checks whether or not this value is a Date
     * @return true if the value is a Date
     */
    public boolean isDate()
    {
        return type==TYPE_DATE;
    }

    /**
     * Checks whether or not the value is a Big Number
     * @return true is this value is a big number
     */
    public boolean isBigNumber()
    {
        return type==TYPE_BIGNUMBER;
    }

    /**
     * Checks whether or not the value is a Number
     * @return true is this value is a number
     */
    public boolean isNumber()
    {
        return type==TYPE_NUMBER;
    }

    /**
     * Checks whether or not this value is a boolean
     * @return true if this value has type boolean.
     */
    public boolean isBoolean()
    {
        return type==TYPE_BOOLEAN;
    }

    /**
     * Checks whether or not this value is of type Serializable
     * @return true if this value has type Serializable
     */
    public boolean isSerializableType() {
        return type == TYPE_SERIALIZABLE;
    }

    /**
     * Checks whether or not this value is of type Binary
     * @return true if this value has type Binary
     */
    public boolean isBinary() {
        return type == TYPE_BINARY;
    }   
    
    /**
     * Checks whether or not this value is an Integer
     * @return true if this value is an integer
     */
    public boolean isInteger()
    {
        return type==TYPE_INTEGER;
    }

    /**
     * Checks whether or not this Value is Numeric
     * A Value is numeric if it is either of type Number or Integer
     * @return true if the value is either of type Number or Integer
     */
    public boolean isNumeric()
    {
        return isInteger() || isNumber() || isBigNumber();
    }
    
    /**
     * Checks whether or not the specified type is either Integer or Number
     * @param t the type to check
     * @return true if the type is Integer or Number
     */
    public static final boolean isNumeric(int t)
    {
        return t==TYPE_INTEGER || t==TYPE_NUMBER || t==TYPE_BIGNUMBER;
    }
}
