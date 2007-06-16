package org.pentaho.di.core.row;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.exception.KettleValueException;
import be.ibridge.kettle.core.value.Value;

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
    private boolean  caseInsensitive;
    private boolean  sortedDescending;
    private boolean  outputPaddingEnabled;
    private boolean  largeTextField;
    private Locale   dateFormatLocale;
    private boolean  dateFormatLenient;
    
    private SimpleDateFormat dateFormat;
    private boolean dateFormatChanged;
    
    private DecimalFormat    decimalFormat;
    private boolean decimalFormatChanged;
    
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
        this.sortedDescending=false;
        this.outputPaddingEnabled=false;
        this.decimalSymbol = ""+Const.DEFAULT_DECIMAL_SEPARATOR;
        this.groupingSymbol = ""+Const.DEFAULT_GROUPING_SEPARATOR;
        this.dateFormatLocale = Locale.getDefault();
    }
    
    public Object clone()
    {
        try
        {
            ValueMeta valueMeta = (ValueMeta) super.clone();
            valueMeta.dateFormat = null;
            valueMeta.decimalFormat = null;
            if (dateFormatLocale!=null) valueMeta.dateFormatLocale = (Locale) dateFormatLocale.clone();
            return valueMeta;
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
    
    public boolean isIndexed()
    {
        return storageType == STORAGE_TYPE_INDEXED;
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
        dateFormatChanged = true;
        decimalFormatChanged = true;
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
        decimalFormatChanged = true;
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
        decimalFormatChanged = true;
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
        decimalFormatChanged = true;
    }
    
    /**
     * @return the caseInsensitive
     */
    public boolean isCaseInsensitive()
    {
        return caseInsensitive;
    }

    /**
     * @param caseInsensitive the caseInsensitive to set
     */
    public void setCaseInsensitive(boolean caseInsensitive)
    {
        this.caseInsensitive = caseInsensitive;
    }

    
    /**
     * @return the sortedDescending
     */
    public boolean isSortedDescending()
    {
        return sortedDescending;
    }

    /**
     * @param sortedDescending the sortedDescending to set
     */
    public void setSortedDescending(boolean sortedDescending)
    {
        this.sortedDescending = sortedDescending;
    }
    

    /**
     * @return true if output padding is enabled (padding to specified length)
     */
    public boolean isOutputPaddingEnabled()
    {
        return outputPaddingEnabled;
    }

    /**
     * @param outputPaddingEnabled Set to true if output padding is to be enabled (padding to specified length)
     */
    public void setOutputPaddingEnabled(boolean outputPaddingEnabled)
    {
        this.outputPaddingEnabled = outputPaddingEnabled;
    }

    /**
     * @return true if this is a large text field (CLOB, TEXT) with arbitrary length.
     */
    public boolean isLargeTextField()
    {
        return largeTextField;
    }

    /**
     * @param largeTextField Set to true if this is to be a large text field (CLOB, TEXT) with arbitrary length.
     */
    public void setLargeTextField(boolean largeTextField)
    {
        this.largeTextField = largeTextField;
    }
    
    /**
     * @return the dateFormatLenient
     */
    public boolean isDateFormatLenient()
    {
        return dateFormatLenient;
    }

    /**
     * @param dateFormatLenient the dateFormatLenient to set
     */
    public void setDateFormatLenient(boolean dateFormatLenient)
    {
        this.dateFormatLenient = dateFormatLenient;
        dateFormatChanged=true;
    }

    /**
     * @return the dateFormatLocale
     */
    public Locale getDateFormatLocale()
    {
        return dateFormatLocale;
    }

    /**
     * @param dateFormatLocale the dateFormatLocale to set
     */
    public void setDateFormatLocale(Locale dateFormatLocale)
    {
        this.dateFormatLocale = dateFormatLocale;
        dateFormatChanged=true;
    }
    
    

    // DATE + STRING


    private synchronized String convertDateToString(Date date)
    {
        if (date==null) return null;
        
        return getDateFormat().format(date);
    }

    private synchronized Date convertStringToDate(String string) throws KettleValueException
    {
        if (string==null) return null;
        
        try
        {
            return getDateFormat().parse(string);
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

    private synchronized String convertNumberToString(Double number) throws KettleValueException
    {
        if (number==null) return null;
        
        try
        {
            if (Const.isEmpty(conversionMask) && Const.isEmpty(decimalSymbol) && Const.isEmpty(groupingSymbol)) 
            {
                return number.toString();
            }
            return getDecimalFormat().format(number);
        }
        catch(Exception e)
        {
            throw new KettleValueException("Couldn't convert Number to String ", e);
        }
    }
    
    private synchronized Double convertStringToNumber(String string) throws KettleValueException
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
    
    public synchronized SimpleDateFormat getDateFormat()
    {
        if (dateFormat==null || dateFormatChanged)
        {
            dateFormat = new SimpleDateFormat();
            String mask;
            if (Const.isEmpty(conversionMask))
            {
                mask = DEFAULT_DATE_FORMAT_MASK;
            }
            else
            {
                mask = conversionMask;
            }
            
            if (dateFormatLocale==null || dateFormatLocale.equals(Locale.getDefault()))
            {
                dateFormat = new SimpleDateFormat(mask);
            }
            else
            {
                dateFormat = new SimpleDateFormat(mask, dateFormatLocale);
            }
            
            dateFormatChanged=false;
        }
        return dateFormat;
    }

    public synchronized DecimalFormat getDecimalFormat()
    {
        if (decimalFormat==null || decimalFormatChanged)
        {
            decimalFormat        = (DecimalFormat)NumberFormat.getInstance();
            DecimalFormatSymbols decimalFormatSymbols = decimalFormat.getDecimalFormatSymbols();
        
            if (!Const.isEmpty(currencySymbol)) decimalFormatSymbols.setCurrencySymbol( currencySymbol );
            if (!Const.isEmpty(groupingSymbol)) decimalFormatSymbols.setGroupingSeparator( groupingSymbol.charAt(0) );
            if (!Const.isEmpty(decimalSymbol)) decimalFormatSymbols.setDecimalSeparator( decimalSymbol.charAt(0) );
            decimalFormat.setDecimalFormatSymbols(decimalFormatSymbols);
            if (!Const.isEmpty(conversionMask)) decimalFormat.applyPattern(conversionMask);
            
            decimalFormatChanged=false;
        }
        return decimalFormat;
    }

    private synchronized String convertIntegerToString(Long number) throws KettleValueException
    {
        if (number==null) return null;

        try
        {
            if (Const.isEmpty(conversionMask) && Const.isEmpty(decimalSymbol) && Const.isEmpty(groupingSymbol)) 
            {
                return number.toString();
            }
            return getDecimalFormat().format(number);
        }
        catch(Exception e)
        {
            throw new KettleValueException("Couldn't convert Long to String ", e);
        }
    }
    
    private synchronized Long convertStringToInteger(String string) throws KettleValueException
    {
        if (Const.isEmpty(string)) return null;

        try
        {
            return new Long( getDecimalFormat().parse(string).longValue() );
        }
        catch(Exception e)
        {
            throw new KettleValueException("Couldn't convert String to Integer", e);
        }
    }
    
    private synchronized String convertBigNumberToString(BigDecimal number) throws KettleValueException
    {
        if (number==null) return null;

        String string = number.toString();
        /*
        if ( !Const.isEmpty(decimalSymbol) && !".".equalsIgnoreCase(decimalSymbol) )
        {
            string = Const.replace(string, ".", decimalSymbol.substring(0, 1));
        }
        */
        return string;
    }
    
    private synchronized BigDecimal convertStringToBigNumber(String string) throws KettleValueException
    {
        if (string==null) return null;

        /*
        if (!".".equalsIgnoreCase(decimalSymbol))
        {
            string = Const.replace(string, decimalSymbol.substring(0, 1), ".");
        }
        */
        
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
    
    public static Boolean convertStringToBoolean(String string)
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
        
        if (isIndexed())
        {
            return object;                    
        }
        else
        {
            switch(getType())
            {
            case ValueMeta.TYPE_STRING: 
            case ValueMeta.TYPE_NUMBER: 
            case ValueMeta.TYPE_INTEGER: 
            case ValueMeta.TYPE_BOOLEAN:
            case ValueMeta.TYPE_BIGNUMBER: // primitive data types: we can only overwrite these, not change them
                return object;

            case ValueMeta.TYPE_DATE:
                return new Date( ((Date)object).getTime() ); // just to make sure: very inexpensive too.

            case ValueMeta.TYPE_BINARY:
                byte[] origin = (byte[]) object;
                byte[] target = new byte[origin.length];
                System.arraycopy(origin, 0, target, 0, origin.length);
                return target;

            default: throw new KettleValueException("Unable to make copy of value type: "+getType());
            }
        }
        
        /*
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
        */
    }

    public String getString(Object object) throws KettleValueException
    {
        if (object==null) // NULL 
        {
            return null;
        }
        String string;
        
        switch(type)
        {
        case TYPE_STRING:
            switch(storageType)
            {
            case STORAGE_TYPE_NORMAL:       string = (String)object; break;
            case STORAGE_TYPE_INDEXED:      string = (String) index[((Integer)object).intValue()];  break;
            default: throw new KettleValueException("Unknown storage type "+storageType+" specified.");
            }
            break;
            
        case TYPE_DATE:
            switch(storageType)
            {
            case STORAGE_TYPE_NORMAL:       string = convertDateToString((Date)object); break;
            case STORAGE_TYPE_INDEXED:      string = convertDateToString((Date)index[((Integer)object).intValue()]); break;
            default: throw new KettleValueException("Unknown storage type "+storageType+" specified.");
            }
            break;

        case TYPE_NUMBER:
            switch(storageType)
            {
            case STORAGE_TYPE_NORMAL:       string = convertNumberToString((Double)object); break;
            case STORAGE_TYPE_INDEXED:      string = convertNumberToString((Double)index[((Integer)object).intValue()]); break;
            default: throw new KettleValueException("Unknown storage type "+storageType+" specified.");
            }
            break;

        case TYPE_INTEGER:
            switch(storageType)
            {
            case STORAGE_TYPE_NORMAL:       string = convertIntegerToString((Long)object); break;
            case STORAGE_TYPE_INDEXED:      string = convertIntegerToString((Long)index[((Integer)object).intValue()]); break;
            default: throw new KettleValueException("Unknown storage type "+storageType+" specified.");
            }
            break;

        case TYPE_BIGNUMBER:
            switch(storageType)
            {
            case STORAGE_TYPE_NORMAL:       string = convertBigNumberToString((BigDecimal)object); break;
            case STORAGE_TYPE_INDEXED:      string = convertBigNumberToString((BigDecimal)index[((Integer)object).intValue()]); break;
            default: throw new KettleValueException("Unknown storage type "+storageType+" specified.");
            }
            break;

        case TYPE_BOOLEAN:
            switch(storageType)
            {
            case STORAGE_TYPE_NORMAL:       string = convertBooleanToString((Boolean)object); break;
            case STORAGE_TYPE_INDEXED:      string = convertBooleanToString((Boolean)index[((Integer)object).intValue()]); break;
            default: throw new KettleValueException("Unknown storage type "+storageType+" specified.");
            }
            break;

        case TYPE_BINARY:
            switch(storageType)
            {
            case STORAGE_TYPE_NORMAL:       string = convertBinaryToString((byte[])object); break;
            case STORAGE_TYPE_INDEXED:      string = convertBinaryToString((byte[])index[((Integer)object).intValue()]); break;
            default: throw new KettleValueException("Unknown storage type "+storageType+" specified.");
            }
            break;

        case TYPE_SERIALIZABLE:
            switch(storageType)
            {
            case STORAGE_TYPE_NORMAL:       string = object.toString();  break; // just go for the default toString()
            case STORAGE_TYPE_INDEXED:      string = index[((Integer)object).intValue()].toString();  break; // just go for the default toString()
            default: throw new KettleValueException("Unknown storage type "+storageType+" specified.");
            }
            break;

        default: 
            throw new KettleValueException("Unknown type "+type+" specified.");
        }
        
        if (isOutputPaddingEnabled() && getLength()>0)
        {
            return ValueDataUtil.rightPad(string, getLength());
        }
        else
        {
            return string;
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
    
    public boolean isSortedAscending()
    {
        return !isSortedDescending();
    }
    
    /**
     * Return the type of a value in a textual form: "String", "Number", "Integer", "Boolean", "Date", ...
     * @return A String describing the type of value.
     */
    public String getTypeDesc()
    {
        return typeCodes[type];
    }


    public String toString()
    {
        return name+" "+toStringMeta();
    }

    
    /**
     * a String text representation of this Value, optionally padded to the specified length
     * @return a String text representation of this Value, optionally padded to the specified length
     */
    public String toStringMeta()
    {
        // We (Sven Boden) did explicit performance testing for this
        // part. The original version used Strings instead of StringBuffers,
        // performance between the 2 does not differ that much. A few milliseconds
        // on 100000 iterations in the advantage of StringBuffers. The
        // lessened creation of objects may be worth it in the long run.
        StringBuffer retval=new StringBuffer(getTypeDesc());

        switch(getType())
        {
        case TYPE_STRING :
            if (getLength()>0) retval.append('(').append(getLength()).append(')');  
            break;
        case TYPE_NUMBER :
        case TYPE_BIGNUMBER :
            if (getLength()>0)
            {
                retval.append('(').append(getLength());
                if (getPrecision()>0)
                {
                    retval.append(", ").append(getPrecision());
                }
                retval.append(')');
            }
            break;
        case TYPE_INTEGER:
            if (getLength()>0)
            {
                retval.append('(').append(getLength()).append(')');
            }
            break;
        default: break;
        }

        return retval.toString();
    }

    public void writeData(DataOutputStream outputStream, Object object) throws KettleFileException
    {
        try
        {
            // Is the value NULL?
            outputStream.writeBoolean(object==null);

            if (object!=null) // otherwise there is no point
            {
                switch(storageType)
                {
                case STORAGE_TYPE_NORMAL:
                    // Handle Content -- only when not NULL
                    switch(getType())
                    {
                    case TYPE_STRING     : writeString(outputStream, (String)object); break;
                    case TYPE_NUMBER     : writeNumber(outputStream, (Double)object); break;
                    case TYPE_INTEGER    : writeInteger(outputStream, (Long)object); break;
                    case TYPE_DATE       : writeDate(outputStream, (Date)object); break;
                    case TYPE_BIGNUMBER  : writeBigNumber(outputStream, (BigDecimal)object); break;
                    case TYPE_BOOLEAN    : writeBoolean(outputStream, (Boolean)object); break;
                    case TYPE_BINARY     : writeBinary(outputStream, (byte[])object); break;
                    default: throw new KettleFileException("Unable to serialize data type "+getType());
                    }
                    break;
                case STORAGE_TYPE_INDEXED:
                    writeInteger(outputStream, (Integer)object); // just an index 
                    break;
                default: throw new KettleFileException("Unknown storage type "+getStorageType());
                }
            }
        }
        catch(IOException e)
        {
            throw new KettleFileException("Unable to write value data to output stream", e);
        }
        
    }
    
    public Object readData(DataInputStream inputStream) throws KettleFileException
    {
        try
        {
            // Is the value NULL?
            if (inputStream.readBoolean()) return null; // done

            switch(storageType)
            {
            case STORAGE_TYPE_NORMAL:
                // Handle Content -- only when not NULL
                switch(getType())
                {
                case TYPE_STRING     : return readString(inputStream);
                case TYPE_NUMBER     : return readNumber(inputStream);
                case TYPE_INTEGER    : return readInteger(inputStream);
                case TYPE_DATE       : return readDate(inputStream);
                case TYPE_BIGNUMBER  : return readBigNumber(inputStream);
                case TYPE_BOOLEAN    : return readBoolean(inputStream);
                case TYPE_BINARY     : return readBinary(inputStream);
                default: throw new KettleFileException("Unable to de-serialize data of type "+getType());
                }
            case STORAGE_TYPE_INDEXED:
                return readSmallInteger(inputStream); // just an index: 4-bytes should be enough.
                
            default: throw new KettleFileException("Unknown storage type "+getStorageType());
            }
        }
        catch(IOException e)
        {
            throw new KettleFileException("Unable to read value data from input stream", e);
        }
    }

    
    private void writeString(DataOutputStream outputStream, String string) throws IOException
    {
        // Write the length and then the bytes
        if (string==null)
        {
            outputStream.writeInt(-1);
        }
        else
        {
            byte[] chars = string.getBytes(Const.XML_ENCODING);
            outputStream.writeInt(chars.length);
            outputStream.write(chars);
        }
    }

    private String readString(DataInputStream inputStream) throws IOException
    {
        // Read the length and then the bytes
        int length = inputStream.readInt();
        if (length<0) 
        {
            return null;
        }
        if (length>30 || length==0) 
        {
            IOException e = new IOException("Unexpected length for ("+getName()+")"+length);
            // System.out.println(Const.getStackTracker(e));
            throw e;
        }
        
        byte[] chars = new byte[length];
        inputStream.readFully(chars);

        String string = new String(chars, Const.XML_ENCODING);         
        // System.out.println("Read string("+getName()+"), length "+length+": "+string);
        return string;
    }

    private void writeBigNumber(DataOutputStream outputStream, BigDecimal number) throws IOException
    {
        String string = number.toString();
        writeString(outputStream, string);
    }

    private BigDecimal readBigNumber(DataInputStream inputStream) throws IOException
    {
        String string = readString(inputStream);
        // System.out.println("Read big number("+getName()+") ["+string+"]");
        return new BigDecimal(string);
    }

    private void writeDate(DataOutputStream outputStream, Date date) throws IOException
    {
        outputStream.writeLong(date.getTime());
    }
    
    private Date readDate(DataInputStream inputStream) throws IOException
    {
        long time = inputStream.readLong();
        // System.out.println("Read Date("+getName()+") ["+new Date(time)+"]");
        return new Date(time);
    }

    private void writeBoolean(DataOutputStream outputStream, Boolean bool) throws IOException
    {
        outputStream.writeBoolean(bool.booleanValue());
    }
    
    private Boolean readBoolean(DataInputStream inputStream) throws IOException
    {
        Boolean bool = new Boolean( inputStream.readBoolean() );
        // System.out.println("Read boolean("+getName()+") ["+bool+"]");
        return bool;
    }
    
    private void writeNumber(DataOutputStream outputStream, Double number) throws IOException
    {
        outputStream.writeDouble(number.doubleValue());
    }

    private Double readNumber(DataInputStream inputStream) throws IOException
    {
        Double d = new Double( inputStream.readDouble() );
        // System.out.println("Read number("+getName()+") ["+d+"]");
        return d;
    }

    private void writeInteger(DataOutputStream outputStream, Long number) throws IOException
    {
        outputStream.writeLong(number.longValue());
    }

    private Long readInteger(DataInputStream inputStream) throws IOException
    {
        Long l = new Long( inputStream.readLong() );
        // System.out.println("Read integer("+getName()+") ["+l+"]");
        return l;
    }

    private void writeInteger(DataOutputStream outputStream, Integer number) throws IOException
    {
        outputStream.writeInt(number.intValue());
    }
    
    private Integer readSmallInteger(DataInputStream inputStream) throws IOException
    {
        Integer i = new Integer( inputStream.readInt() );
        // System.out.println("Read index integer("+getName()+") ["+i+"]");
        return i;
    }
    
    private void writeBinary(DataOutputStream outputStream, byte[] binary) throws IOException
    {
        outputStream.writeInt(binary.length);
        outputStream.write(binary);
    }
    
    private byte[] readBinary(DataInputStream inputStream) throws IOException
    {
        int size = inputStream.readInt();
        byte[] buffer = new byte[size];
        inputStream.readFully(buffer);
        
        // System.out.println("Read binary("+getName()+") with size="+size);

        return buffer;
    }


    public void writeMeta(DataOutputStream outputStream) throws KettleFileException
    {
        try
        {
            int type=getType();
    
            // Handle type
            outputStream.writeInt(type);
            
            // Handle storage type
            outputStream.writeInt(storageType);
            
            if (storageType==STORAGE_TYPE_INDEXED)
            {
                // Save the indexed strings...
                if (index==null)
                {
                    outputStream.writeInt(-1); // null
                }
                else
                {
                    outputStream.writeInt(index.length);
                    for (int i=0;i<index.length;i++)
                    {
                        switch(type)
                        {
                        case TYPE_STRING:    writeString(outputStream, (String)index[i]); break; 
                        case TYPE_NUMBER:    writeNumber(outputStream, (Double)index[i]); break; 
                        case TYPE_INTEGER:   writeInteger(outputStream, (Long)index[i]); break; 
                        case TYPE_DATE:      writeDate(outputStream, (Date)index[i]); break; 
                        case TYPE_BIGNUMBER: writeBigNumber(outputStream, (BigDecimal)index[i]); break; 
                        case TYPE_BOOLEAN:   writeBoolean(outputStream, (Boolean)index[i]); break; 
                        case TYPE_BINARY:    writeBinary(outputStream, (byte[])index[i]); break;
                        default: throw new KettleFileException("Unable to serialize indexe storage type for data type "+getType());
                        }
                    }
                }
            }
            
            // Handle name-length
            writeString(outputStream, name);  
            
            // length & precision
            outputStream.writeInt(getLength());
            outputStream.writeInt(getPrecision());

            // Origin
            writeString(outputStream, origin);

            // Comments
            writeString(outputStream, comments);
            
            // formatting Mask, decimal, grouping, currency
            writeString(outputStream, conversionMask);
            writeString(outputStream, decimalSymbol);
            writeString(outputStream, groupingSymbol);
            writeString(outputStream, currencySymbol);
            
            // Case sensitivity of compare
            outputStream.writeBoolean(caseInsensitive);  
            
            // Sorting information
            outputStream.writeBoolean(sortedDescending); 

            // Padding information
            outputStream.writeBoolean(outputPaddingEnabled); 
            
            // date format lenient?
            outputStream.writeBoolean(dateFormatLenient);
            
            // date format locale?
            writeString(outputStream, dateFormatLocale!=null ? dateFormatLocale.toString() : null);
        }
        catch(IOException e)
        {
            throw new KettleFileException("Unable to write value metadata to output stream", e);
        }
    }
    
    public ValueMeta(DataInputStream inputStream) throws KettleFileException
    {
        this();
        
        try
        {
            // Handle type
            type=inputStream.readInt();
    
            // Handle storage type
            storageType = inputStream.readInt();
            
            // Read the data in the index
            if (storageType==STORAGE_TYPE_INDEXED)
            {
                int indexSize = inputStream.readInt();
                if (indexSize<0)
                {
                    index=null;
                }
                else
                {
                    index=new Object[indexSize];
                    for (int i=0;i<indexSize;i++)
                    {
                        switch(type)
                        {
                        case TYPE_STRING:    index[i] = readString(inputStream); break; 
                        case TYPE_NUMBER:    index[i] = readNumber(inputStream); break; 
                        case TYPE_INTEGER:   index[i] = readInteger(inputStream); break; 
                        case TYPE_DATE:      index[i] = readDate(inputStream); break; 
                        case TYPE_BIGNUMBER: index[i] = readBigNumber(inputStream); break; 
                        case TYPE_BOOLEAN:   index[i] = readBoolean(inputStream); break; 
                        case TYPE_BINARY:    index[i] = readBinary(inputStream); break;
                        default: throw new KettleFileException("Unable to de-serialize indexed storage type for data type "+getType());
                        }
                    }
                }

            }
            
            // name
            name = readString(inputStream);  
            
            // length & precision
            length = inputStream.readInt();
            precision = inputStream.readInt();
            
            // Origin
            origin = readString(inputStream);

            // Comments
            comments=readString(inputStream);
            
            // formatting Mask, decimal, grouping, currency
            
            conversionMask=readString(inputStream);
            decimalSymbol=readString(inputStream);
            groupingSymbol=readString(inputStream);
            currencySymbol=readString(inputStream);
            
            // Case sensitivity
            caseInsensitive = inputStream.readBoolean();
            
            // Sorting type
            sortedDescending = inputStream.readBoolean();
            
            // Output padding?
            outputPaddingEnabled = inputStream.readBoolean();
            
            // is date parsing lenient?
            dateFormatLenient = inputStream.readBoolean();
            
            String strDateFormatLocale = readString(inputStream);
            if (Const.isEmpty(strDateFormatLocale)) 
            {
                dateFormatLocale = null; 
            }
            else
            {
                dateFormatLocale = new Locale(strDateFormatLocale);
            }
        }
        catch(IOException e)
        {
            throw new KettleFileException("Unable to read value metadata from input stream", e);
        }
    }


    /**
     * get an array of String describing the possible types a Value can have.
     * @return an array of String describing the possible types a Value can have.
     */
    public static final String[] getTypes()
    {
        String retval[] = new String[typeCodes.length-1];
        System.arraycopy(typeCodes, 1, retval, 0, typeCodes.length-1);
        return retval;
    }
    
    /**
     * Get an array of String describing the possible types a Value can have.
     * @return an array of String describing the possible types a Value can have.
     */
    public static final String[] getAllTypes()
    {
        String retval[] = new String[typeCodes.length];
        System.arraycopy(typeCodes, 0, retval, 0, typeCodes.length);
        return retval;
    }
    
    /**
     * TODO: change Desc to Code all over the place.  Make sure we can localise this stuff later on.
     * 
     * @param type the type 
     * @return the description (code) of the type
     */
    public static final String getTypeDesc(int type)
    {
        return typeCodes[type];
    }

    /**
     * Convert the String description of a type to an integer type.
     * @param desc The description of the type to convert
     * @return The integer type of the given String.  (Value.VALUE_TYPE_...)
     */
    public static final int getType(String desc)
    {
        int i;

        for (i=1;i<typeCodes.length;i++)
        {
            if (typeCodes[i].equalsIgnoreCase(desc))
            {
                return i; 
            }
        }

        return TYPE_NONE;
    }
    
    /**
     * Determine if an object is null.
     * This is the case if data==null or if it's an empty string.
     * @param data the object to test
     * @return true if the object is considered null.
     */
    public boolean isNull(Object data)
    {
        return data==null || (isString() && ((String)data).length()==0);
    }
    
    /**
     * Compare 2 values of the same data type
     * @param data1 the first value
     * @param data2 the second value
     * @return 0 if the values are equal, -1 if data1 is smaller than data2 and +1 if it's larger.
     * @throws KettleValueException In case we get conversion errors
     */
    public int compare(Object data1, Object data2) throws KettleValueException
    {
        boolean n1 = isNull(data1);
        boolean n2 = isNull(data2);

        // null is always smaller!
        if (n1 && !n2) return -1;
        if (!n1 && n2) return 1;
        if (n1 && n2) return 0;

        int cmp=0;
        switch (getType())
        {
        case TYPE_STRING:
            {
                String one = Const.rtrim(getString(data1));
                String two = Const.rtrim(getString(data2));
    
                if (caseInsensitive)
                {
                    cmp = one.compareToIgnoreCase(two);
                }
                else
                {
                    cmp = one.compareTo(two);
                }
            }
            break;

        case TYPE_INTEGER:
            {
                cmp = Double.compare(getNumber(data1).doubleValue(), getNumber(data2).doubleValue());
            }
            break;

        case TYPE_NUMBER:
            {
                cmp=Double.compare(getNumber(data1).doubleValue(), getNumber(data2).doubleValue());
            }
            break;

        case TYPE_DATE:
            {
                cmp =  Double.compare(getInteger(data1).longValue(), getInteger(data2).longValue());
            }
            break;

        case TYPE_BIGNUMBER:
            {
                cmp=getBigNumber(data1).compareTo(getBigNumber(data2));
            }
            break;

        case TYPE_BOOLEAN:
            {
                if (getBoolean(data1).booleanValue() == getBoolean(data2).booleanValue()) cmp=0; // true == true, false == false
                else if (getBoolean(data1).booleanValue() && !getBoolean(data2).booleanValue()) cmp=1; // true  > false
                else cmp=-1; // false < true
            }
            break;

        case TYPE_BINARY:
            {
                byte[] b1 = (byte[]) data1;
                byte[] b2 = (byte[]) data2;
                
                int length= b1.length < b2.length ? b1.length : b2.length;
                
                for (int i=0;i<length;i++)
                {
                    cmp = b1[i] - b2[i];
                    if (cmp!=0)
                    {
                        cmp = Math.abs(cmp);
                        break;
                    }
                }
            }
            break;
        default: 
            throw new KettleValueException("Comparing values can not be done with data type : "+getType());
        }
        
        if (isSortedDescending())
        {
            return -cmp;
        }
        else
        {
            return cmp;
        }
    }
    
    /**
     * Compare 2 values of the same data type
     * @param data1 the first value
     * @param meta2 the second value's metadata
     * @param data2 the second value
     * @return 0 if the values are equal, -1 if data1 is smaller than data2 and +1 if it's larger.
     * @throws KettleValueException In case we get conversion errors
     */
    public int compare(Object data1, ValueMetaInterface meta2, Object data2) throws KettleValueException
    {
        // Before we can compare data1 to data2 we need to make sure they have the same data type etc.
        if (getType()==meta2.getType()) return compare(data1, data2);
        
        // If the data types are not the same, the first one is the driver...
        // The second data type is converted to the first one.
        return compare(data1, convertData(meta2, data2));
    }

    /**
     * Convert the specified data to the data type specified in this object.
     * @param meta2 the metadata of the object to be converted
     * @param data2 the data of the object to be converted
     * @return the object in the data type of this value metadata object
     * @throws KettleValueException in case there is a data conversion error
     */
    public Object convertData(ValueMetaInterface meta2, Object data2) throws KettleValueException
    {
        switch(getType())
        {
        case TYPE_STRING    : return meta2.getString(data2);
        case TYPE_NUMBER    : return meta2.getNumber(data2);
        case TYPE_INTEGER   : return meta2.getInteger(data2);
        case TYPE_DATE      : return meta2.getDate(data2);
        case TYPE_BIGNUMBER : return meta2.getBigNumber(data2);
        case TYPE_BOOLEAN   : return meta2.getBoolean(data2);
        case TYPE_BINARY    : return meta2.getBinary(data2);
        default: 
            throw new KettleValueException("I can't convert the specified value to data type : "+getType());
        }
    }
    
    /**
     * Calculate the hashcode of the specified data object
     * @param object the data value to calculate a hashcode for 
     * @return the calculated hashcode
     * @throws KettleValueException 
     */
    public int hashCode(Object object) throws KettleValueException
    {
        int hash=0;
        
        if (isNull(object))
        {
            switch(getType())
            {
            case TYPE_BOOLEAN   : hash^= 1; break;
            case TYPE_DATE      : hash^= 2; break;
            case TYPE_NUMBER    : hash^= 4; break;
            case TYPE_STRING    : hash^= 8; break;
            case TYPE_INTEGER   : hash^=16; break;
            case TYPE_BIGNUMBER : hash^=32; break;
            case TYPE_NONE      : break;
            default: break;
            }
        }
        else
        {
            switch(getType())
            {
            case TYPE_BOOLEAN   : hash^=getBoolean(object).hashCode(); break;
            case TYPE_DATE      : hash^=getDate(object).hashCode(); break;
            case TYPE_INTEGER   : hash^=getInteger(object).hashCode(); break;
            case TYPE_NUMBER    : hash^=getNumber(object).hashCode(); break;
            case TYPE_STRING    : hash^=getString(object).hashCode(); break;
            case TYPE_BIGNUMBER : hash^=getBigNumber(object).hashCode(); break;
            case TYPE_NONE      : break;
            default: break;
            }
        }

        return hash;
    }

    /**
     * Create an old-style value for backward compatibility reasons
     * @param data the data to store in the value
     * @return a newly created Value object
     * @throws KettleValueException  case there is a data conversion problem
     */
    public Value createOriginalValue(Object data) throws KettleValueException
    {
       Value value = new Value(name, type);
       value.setLength(length, precision);
       
       if (isNull(data))
       {
           value.setNull();
       }
       else
       {
           switch(value.getType())
           {
           case TYPE_STRING       : value.setValue( getString(data) ); break;
           case TYPE_NUMBER       : value.setValue( getNumber(data).doubleValue() ); break;
           case TYPE_INTEGER      : value.setValue( getInteger(data).longValue() ); break;
           case TYPE_DATE         : value.setValue( getDate(data) ); break;
           case TYPE_BOOLEAN      : value.setValue( getBoolean(data).booleanValue() ); break;
           case TYPE_BIGNUMBER    : value.setValue( getBigNumber(data) ); break;
           case TYPE_BINARY       : value.setValue( getBinary(data) ); break;
           default: throw new KettleValueException("We can't convert data type "+getTypeDesc()+" to an original (V2) Value");
           }
       }
       return value;
    }


}
