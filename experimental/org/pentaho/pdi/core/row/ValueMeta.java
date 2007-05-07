package org.pentaho.pdi.core.row;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
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
    
    public String convertToString(Object object) throws KettleValueException
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
            }
            throw new KettleValueException("Unknown storage type "+storageType+" specified.");
        case TYPE_DATE:
            switch(storageType)
            {
            case STORAGE_TYPE_NORMAL:       return convertDateToString((Date)object);
            case STORAGE_TYPE_INDEXED:      return convertDateToString((Date)index[((Integer)object).intValue()]);  
            }
            throw new KettleValueException("Unknown storage type "+storageType+" specified.");
        case TYPE_NUMBER:
            switch(storageType)
            {
            case STORAGE_TYPE_NORMAL:       return convertNumberToString((Double)object);
            case STORAGE_TYPE_INDEXED:      return convertNumberToString((Double)index[((Integer)object).intValue()]);
            }
            throw new KettleValueException("Unknown storage type "+storageType+" specified.");
        case TYPE_INTEGER:
            switch(storageType)
            {
            case STORAGE_TYPE_NORMAL:       return convertIntegerToString((Integer)object);
            case STORAGE_TYPE_INDEXED:      return convertIntegerToString((Integer)index[((Integer)object).intValue()]);
            }
            throw new KettleValueException("Unknown storage type "+storageType+" specified.");
        case TYPE_BIGNUMBER:
            switch(storageType)
            {
            case STORAGE_TYPE_NORMAL:       return convertBigNumberToString((BigDecimal)object);
            case STORAGE_TYPE_INDEXED:      return convertBigNumberToString((BigDecimal)index[((Integer)object).intValue()]);
            }
            throw new KettleValueException("Unknown storage type "+storageType+" specified.");        
        case TYPE_BOOLEAN:
            switch(storageType)
            {
            case STORAGE_TYPE_NORMAL:       return convertBooleanToString((Boolean)object);
            case STORAGE_TYPE_INDEXED:      return convertBooleanToString((Boolean)index[((Integer)object).intValue()]);
            }
            throw new KettleValueException("Unknown storage type "+storageType+" specified.");        
        case TYPE_BINARY:
            switch(storageType)
            {
            case STORAGE_TYPE_NORMAL:       return convertBinaryToString((byte[])object);
            case STORAGE_TYPE_INDEXED:      return convertBinaryToString((byte[])index[((Integer)object).intValue()]);
            }
            throw new KettleValueException("Unknown storage type "+storageType+" specified.");
        case TYPE_SERIALIZABLE:
            switch(storageType)
            {
            case STORAGE_TYPE_NORMAL:       return object.toString(); // just go for the default toString()
            case STORAGE_TYPE_INDEXED:      return index[((Integer)object).intValue()].toString(); // just go for the default toString()
            }
            throw new KettleValueException("Unknown storage type "+storageType+" specified.");
        }
        
        throw new KettleValueException("Unknown type "+type+" specified.");
    }

    private String convertDateToString(Date date)
    {
        if (date==null) return null;
        
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat();
        if (Const.isEmpty(conversionMask))
        {
            simpleDateFormat = new SimpleDateFormat(DEFAULT_DATE_FORMAT_MASK);
        }
        else
        {
            simpleDateFormat = new SimpleDateFormat(conversionMask);
        }
        return simpleDateFormat.format(date);
    }
    
    private String convertNumberToString(Double number) throws KettleValueException
    {
        if (number==null) return null;
        
        DecimalFormat df = getDecimalFormat();
        
        try
        {
            return df.format(number);
        }
        catch(Exception e)
        {
            throw new KettleValueException("Couldn't convert Number to String ", e);
        }
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

    private String convertIntegerToString(Integer number) throws KettleValueException
    {
        if (number==null) return null;

        DecimalFormat df = getDecimalFormat();
        
        try
        {
            return df.format(number);
        }
        catch(Exception e)
        {
            throw new KettleValueException("Couldn't convert Integer to String ", e);
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
}
