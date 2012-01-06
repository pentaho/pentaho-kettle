/*******************************************************************************
 *
 * Pentaho Data Integration
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

package org.pentaho.di.core.row;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.SocketTimeoutException;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.pentaho.di.compatibility.Value;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleEOFException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.util.EnvUtil;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.w3c.dom.Node;

/**
 * @author jb
 *
 */
public class ValueMeta implements ValueMetaInterface
{
	private static Class<?> PKG = Const.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

    public static final String DEFAULT_DATE_FORMAT_MASK = "yyyy/MM/dd HH:mm:ss.SSS";
    
	public static final String XML_META_TAG = "value-meta";
	public static final String XML_DATA_TAG = "value-data";
	
	public static final boolean EMPTY_STRING_AND_NULL_ARE_DIFFERENT = convertStringToBoolean( Const.NVL(System.getProperty(Const.KETTLE_EMPTY_STRING_DIFFERS_FROM_NULL, "N"), "N") );
	
    private String   name;
    private int      length;
    private int      precision;
    private int      type;
    private int      trimType;
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
    
    private ValueMetaInterface storageMetadata;
    private boolean identicalFormat;

	private ValueMetaInterface conversionMetadata;
	
	boolean singleByteEncoding;
	
	private long numberOfBinaryStringConversions;

    private boolean bigNumberFormatting;
    

    // get & store original result set meta data for later use
	// @see java.sql.ResultSetMetaData
    private int originalColumnType;
    private String originalColumnTypeName;
    private int originalPrecision;
    private int originalScale;
    private boolean originalAutoIncrement;
    private int originalNullable;
    private boolean originalSigned;
	
	/**
	 * The trim type codes
	 */
	public final static String trimTypeCode[] = { "none", "left", "right", "both" };

	/** 
	 * The trim description
	 */
	public final static String trimTypeDesc[] = { BaseMessages.getString(PKG, "ValueMeta.TrimType.None"), BaseMessages.getString(PKG, "ValueMeta.TrimType.Left"),
		BaseMessages.getString(PKG, "ValueMeta.TrimType.Right"), BaseMessages.getString(PKG, "ValueMeta.TrimType.Both") };

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
    
    public ValueMeta(String name, int type, int storageType)
    {
        this(name, type, -1, -1);
        this.storageType = storageType;
        setDefaultConversionMask();
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
        this.identicalFormat = true;
        this.bigNumberFormatting = true;
        
        determineSingleByteEncoding();
        setDefaultConversionMask();
    }
    
    public static final String[] SINGLE_BYTE_ENCODINGS = new String[] {
    	"ISO8859_1", "Cp1252", "ASCII", "Cp037", "Cp273", "Cp277", "Cp278", "Cp280", "Cp284", "Cp285", 
    	"Cp297", "Cp420","Cp424", "Cp437", "Cp500", "Cp737", "Cp775", "Cp850", "Cp852", "Cp855", "Cp856", "Cp857", "Cp858", "Cp860",   
    	"Cp861", "Cp862", "Cp863", "Cp865", "Cp866", "Cp869", "Cp870", "Cp871", "Cp875", "Cp918", "Cp921", "Cp922", 
    	"Cp1140", "Cp1141", "Cp1142", "Cp1143", "Cp1144", "Cp1145", "Cp1146", "Cp1147", "Cp1148", "Cp1149", 
    	"Cp1250", "Cp1251", "Cp1253", "Cp1254", "Cp1255", "Cp1257",
    	"ISO8859_2", "ISO8859_3", "ISO8859_5", "ISO8859_5", "ISO8859_6", "ISO8859_7", "ISO8859_8", "ISO8859_9", "ISO8859_13", "ISO8859_15", "ISO8859_15_FDIS", 
    	"MacCentralEurope", "MacCroatian", "MacCyrillic", "MacDingbat", "MacGreek", "MacHebrew", "MacIceland", "MacRoman", "MacRomania", "MacSymbol", "MacTurkish", "MacUkraine",
    };
    
    private void setDefaultConversionMask()
    {
        // Set some sensible default mask on the numbers
        //
		switch(type)
		{
		case TYPE_INTEGER: setConversionMask("#;-#"); break;
		case TYPE_NUMBER: setConversionMask("#.#;-#.#"); break;
		case TYPE_BIGNUMBER: 
			setConversionMask("#.###############################################;-#.###############################################");
			setGroupingSymbol(null);
			setDecimalSymbol(".");  // For backward compatibility reasons!
			break;
		default: break;
		}
    }
    
    private void determineSingleByteEncoding() 
    {
    	singleByteEncoding=false;
    	
    	Charset cs;
    	if (Const.isEmpty(stringEncoding))
    	{
    		cs = Charset.defaultCharset();
    	}
    	else
    	{
    		cs = Charset.forName(stringEncoding);
    	}
    	
    	// See if the default character set for input is single byte encoded.
    	//
    	for (String charSetEncoding : SINGLE_BYTE_ENCODINGS) {
    		if (cs.toString().equalsIgnoreCase(charSetEncoding)) singleByteEncoding=true;
    	}
    }
    
    public ValueMeta clone()
    {
        try
        {
            ValueMeta valueMeta = (ValueMeta) super.clone();
            valueMeta.dateFormat = null;
            valueMeta.decimalFormat = null;
            if (dateFormatLocale!=null) valueMeta.dateFormatLocale = (Locale) dateFormatLocale.clone();
            if (storageMetadata!=null) valueMeta.storageMetadata = storageMetadata.clone();
            if (conversionMetadata!=null) valueMeta.conversionMetadata = conversionMetadata.clone();
            
            valueMeta.compareStorageAndActualFormat();
            
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
     * @param length the length to set
     */
    public void setLength(int length, int precision)
    {
        this.length = length;
        this.precision = precision;
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
    	// For backward compatibility we need to tweak a bit...
    	//
    	if (isInteger() || isBinary()) return 0;
    	if (isString() || isBoolean()) return -1;
    	
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

    public boolean isStorageNormal()
    {
        return storageType == STORAGE_TYPE_NORMAL;
    }

    public boolean isStorageIndexed()
    {
        return storageType == STORAGE_TYPE_INDEXED;
    }

    public boolean isStorageBinaryString()
    {
        return storageType == STORAGE_TYPE_BINARY_STRING;
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
        compareStorageAndActualFormat();
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
        determineSingleByteEncoding();
        compareStorageAndActualFormat();
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
        compareStorageAndActualFormat();
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
        compareStorageAndActualFormat();
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
    
    private static SimpleDateFormat compatibleDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS"); 

    private synchronized String convertDateToCompatibleString(Date date)
    {
        if (date==null) return null;
        return compatibleDateFormat.format(date);
    }

    private synchronized Date convertStringToDate(String string) throws KettleValueException
    {
        string = Const.trimToType(string, getTrimType()); // see if  trimming needs to be performed before conversion

        if (Const.isEmpty(string)) return null;
        
        try
        {
            return getDateFormat().parse(string);
        }
        catch (ParseException e)
        {
        	String dateFormat = (getDateFormat() != null) ? getDateFormat().toPattern() : "null";
            throw new KettleValueException(toString()+" : couldn't convert string ["+string+"] to a date using format ["+dateFormat+"]", e);
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
        if (number==null) {
        	if (!outputPaddingEnabled || length<1) {
        		return null;
        	}
        	else {
        		// Return strings padded to the specified length...
        		// This is done for backward compatibility with 2.5.x 
        		// We just optimized this a bit...
        		//
        		String[] emptyPaddedStrings = Const.getEmptyPaddedStrings();
        		if (length<emptyPaddedStrings.length) {
        			return emptyPaddedStrings[length];
        		}
        		else {
        			return Const.rightPad("", length);
        		}
        	}
        }
        
        try
        {
            return getDecimalFormat(false).format(number);
        }
        catch(Exception e)
        {
            throw new KettleValueException(toString()+" : couldn't convert Number to String ", e);
        }
    }
    
    private synchronized String convertNumberToCompatibleString(Double number) throws KettleValueException
    {
        if (number==null) return null;
        return Double.toString(number);
    }
    
    private synchronized Double convertStringToNumber(String string) throws KettleValueException
    {
        string = Const.trimToType(string, getTrimType()); // see if  trimming needs to be performed before conversion

        if (Const.isEmpty(string)) return null;

        try
        {
            return new Double( getDecimalFormat(false).parse(string).doubleValue() );
        }
        catch(Exception e)
        {
            throw new KettleValueException(toString()+" : couldn't convert String to number ", e);
        }
    }
    
    public synchronized SimpleDateFormat getDateFormat()
    {
    	// If we have a Date that is represented as a String
    	// In that case we can set the format of the original Date on the String value metadata in the form of a conversion metadata object.
    	// That way, we can always convert from Date to String and back without a problem, no matter how complex the format was.
    	// As such, we should return the date SimpleDateFormat of the conversion metadata.
    	//
    	if (conversionMetadata!=null ) {
    		return conversionMetadata.getDateFormat();
    	}
    	
        if (dateFormat==null || dateFormatChanged)
        {
        	// This may not become static as the class is not thread-safe!
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
            
            // Set the conversion leniency as well
            //
            dateFormat.setLenient(dateFormatLenient);
            
            dateFormatChanged=false;
        }
        return dateFormat;
    }

    public synchronized DecimalFormat getDecimalFormat()
    {
        return getDecimalFormat(false);
    }

    public synchronized DecimalFormat getDecimalFormat(boolean useBigDecimal)
    {
    	// If we have an Integer that is represented as a String
    	// In that case we can set the format of the original Integer on the String value metadata in the form of a conversion metadata object.
    	// That way, we can always convert from Integer to String and back without a problem, no matter how complex the format was.
    	// As such, we should return the decimal format of the conversion metadata.
    	//
    	if (conversionMetadata!=null ) {
    		return conversionMetadata.getDecimalFormat(useBigDecimal);
    	}
    	
    	// Calculate the decimal format as few times as possible.
    	// That is because creating or changing a DecimalFormat object is very CPU hungry.
    	//
        if (decimalFormat==null || decimalFormatChanged)
        {
            decimalFormat        = (DecimalFormat)NumberFormat.getInstance();
            decimalFormat.setParseBigDecimal(useBigDecimal);
            DecimalFormatSymbols decimalFormatSymbols = decimalFormat.getDecimalFormatSymbols();
        
            if (!Const.isEmpty(currencySymbol)) decimalFormatSymbols.setCurrencySymbol( currencySymbol );
            if (!Const.isEmpty(groupingSymbol)) decimalFormatSymbols.setGroupingSeparator( groupingSymbol.charAt(0) );
            if (!Const.isEmpty(decimalSymbol)) decimalFormatSymbols.setDecimalSeparator( decimalSymbol.charAt(0) );
            decimalFormat.setDecimalFormatSymbols(decimalFormatSymbols);
            
            // Apply the conversion mask if we have one...
            if (!Const.isEmpty(conversionMask)) {
            	decimalFormat.applyPattern(conversionMask);
            }
            else {
            	switch(type) {
            	case TYPE_INTEGER:
	            	{
		            	if (length<1) {
		            		decimalFormat.applyPattern(" ###############0;-###############0"); // Same as before version 3.0
		            	}
		            	else {
		    				StringBuffer integerPattern=new StringBuffer();
		    				
		    				// First the format for positive integers...
		    				//
		    				integerPattern.append(" ");
		    				for (int i=0;i<getLength();i++) integerPattern.append('0'); // all zeroes.
		    				integerPattern.append(";");
		    				
		    				// Then the format for the negative numbers...
		    				//
		    				integerPattern.append("-");
		    				for (int i=0;i<getLength();i++) integerPattern.append('0'); // all zeroes.
		    				decimalFormat.applyPattern(integerPattern.toString());
		            	}
	            	}
	            	break;
            	case TYPE_BIGNUMBER:
            	case TYPE_NUMBER:
	            	{
	            		if (length<1) {
	            			decimalFormat.applyPattern(" ##########0.0########;-#########0.0########");
	            		}
	            		else {
	    					StringBuffer numberPattern=new StringBuffer();

	    					// First do the format for positive numbers...
	    					//
	    					numberPattern.append(' '); // to compensate for minus sign.
	    					if (precision<0)  // Default: two decimals
	    					{
	    						for (int i=0;i<length;i++) numberPattern.append('0');
	    						numberPattern.append(".00"); // for the .00
	    					}
	    					else  // Floating point format   00001234,56  --> (12,2)
	    					{
	    						for (int i=0;i<=length;i++) numberPattern.append('0'); // all zeroes.
	    						int pos = length-precision+1;
	    						if (pos>=0 && pos <numberPattern.length())
	    						{
	    							numberPattern.setCharAt(length-precision+1, '.'); // one 'comma'
	    						}
	    					}

	    					// Now do the format for negative numbers...
	    					//
	    					StringBuffer negativePattern = new StringBuffer(numberPattern);
	    					negativePattern.setCharAt(0, '-');

	    					numberPattern.append(";");
	    					numberPattern.append(negativePattern);
	    					
	    					// Apply the pattern...
	    					//
	    					decimalFormat.applyPattern(numberPattern.toString());
	            		}
	            	}
            	}

            }
            
            decimalFormatChanged=false;
        }
        return decimalFormat;
    }
   
    private synchronized String convertIntegerToString(Long integer) throws KettleValueException
    {
        if (integer==null) {
        	if (!outputPaddingEnabled || length<1) {
        		return null;
        	}
        	else {
        		// Return strings padded to the specified length...
        		// This is done for backward compatibility with 2.5.x 
        		// We just optimized this a bit...
        		//
        		String[] emptyPaddedStrings = Const.getEmptyPaddedStrings();
        		if (length<emptyPaddedStrings.length) {
        			return emptyPaddedStrings[length];
        		}
        		else {
        			return Const.rightPad("", length);
        		}
        	}
        }

        try
        {
            return getDecimalFormat(false).format(integer);
        }
        catch(Exception e)
        {
            throw new KettleValueException(toString()+" : couldn't convert Long to String ", e);
        }
    }
    
    private synchronized String convertIntegerToCompatibleString(Long integer) throws KettleValueException
    {
        if (integer==null) return null;
        return Long.toString(integer);
    }
    
    private synchronized Long convertStringToInteger(String string) throws KettleValueException
    {
        string = Const.trimToType(string, getTrimType()); // see if  trimming needs to be performed before conversion

        if (Const.isEmpty(string)) return null;
        
        try
        {
        	return new Long( getDecimalFormat(false).parse(string).longValue() );
        }
        catch(Exception e)
        {
            throw new KettleValueException(toString()+" : couldn't convert String to Integer", e);
        }
    }
    
    private synchronized String convertBigNumberToString(BigDecimal number) throws KettleValueException
    {
        if (number==null) return null;

        try
        {
            return getDecimalFormat(bigNumberFormatting).format(number);
        }
        catch(Exception e)
        {
            throw new KettleValueException(toString()+" : couldn't convert BigNumber to String ", e);
        }
    }
    
    private synchronized BigDecimal convertStringToBigNumber(String string) throws KettleValueException
    {
        string = Const.trimToType(string, getTrimType()); // see if  trimming needs to be performed before conversion

        if (Const.isEmpty(string)) return null;

        try
        {
            return (BigDecimal)getDecimalFormat(bigNumberFormatting).parse(string);
        }
        catch(Exception e)
        {
        	// We added this workaround for PDI-1824
        	// 
        	try
            {
            	return new BigDecimal( string );
            }
            catch(NumberFormatException ex)
            {
            	throw new KettleValueException(toString()+" : couldn't convert string value '" + string + "' to a big number.", ex);
            }
        }
}

    // BOOLEAN + STRING
    
    private String convertBooleanToString(Boolean bool)
    {
    	if (bool==null) return null;
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
        if (Const.isEmpty(string)) return null;
        return Boolean.valueOf( "Y".equalsIgnoreCase(string) || "TRUE".equalsIgnoreCase(string) || "YES".equalsIgnoreCase(string) || "1".equals(string) );
    }
    
    // BOOLEAN + NUMBER
    
    private Double convertBooleanToNumber(Boolean bool)
    {
    	if (bool==null) return null;
        return new Double( bool.booleanValue() ? 1.0 : 0.0 );
    }
    
    private Boolean convertNumberToBoolean(Double number)
    {
    	if (number==null) return null;
        return Boolean.valueOf( number.intValue() != 0 );
    }

    // BOOLEAN + INTEGER

    private Long convertBooleanToInteger(Boolean bool)
    {
    	if (bool==null) return null;
        return Long.valueOf( bool.booleanValue() ? 1L : 0L );
    }

    private Boolean convertIntegerToBoolean(Long number)
    {
    	if (number==null) return null;
        return Boolean.valueOf( number.longValue() != 0 );
    }
    
    // BOOLEAN + BIGNUMBER
    
    private BigDecimal convertBooleanToBigNumber(Boolean bool)
    {
    	if (bool==null) return null;
        return bool.booleanValue() ? BigDecimal.ONE : BigDecimal.ZERO;
    }
    
    private Boolean convertBigNumberToBoolean(BigDecimal number)
    {
    	if (number==null) return null;
        return Boolean.valueOf( number.intValue() != 0 );
    }    
    
    /**
     * Converts a byte[] stored in a binary string storage type into a String;
     * 
     * @param binary the binary string
     * @return the String in the correct encoding.
     * @throws KettleValueException
     */
    private String convertBinaryStringToString(byte[] binary) throws KettleValueException
    {
        // OK, so we have an internal representation of the original object, read from file.
        // Before we release it back, we have to see if we don't have to do a String-<type>-String 
        // conversion with different masks.
        // This obviously only applies to numeric data and dates.
        // We verify if this is true or false in advance for performance reasons
        //
    	//if (binary==null || binary.length==0) return null;
      if (binary==null || binary.length==0) {
        return (EMPTY_STRING_AND_NULL_ARE_DIFFERENT && binary != null) ? "" : null;
      }
    	
    	String encoding;
    	if (identicalFormat) encoding = getStringEncoding();
    	else encoding = storageMetadata.getStringEncoding();
    	
    	if (Const.isEmpty(encoding))
        {
            return new String(binary);
        }
        else
        {
            try
            {
                return new String(binary, encoding);
            }
            catch(UnsupportedEncodingException e)
            {
                throw new KettleValueException(toString()+" : couldn't convert binary value to String with specified string encoding ["+stringEncoding+"]", e);
            }
        }
    }
    
    /**
     * Converts the specified data object to the normal storage type.
     * @param object the data object to convert
     * @return the data in a normal storage type
     * @throws KettleValueException In case there is a data conversion error.
     */
    public Object convertToNormalStorageType(Object object) throws KettleValueException
    {
    	if (object==null) return null;
    	
    	switch(storageType)
    	{
    	case STORAGE_TYPE_NORMAL: 
    		return object;
    	case STORAGE_TYPE_BINARY_STRING : 
    		return convertBinaryStringToNativeType((byte[])object);
    	case STORAGE_TYPE_INDEXED : 
    		return index[(Integer)object];
        default: 
        	throw new KettleValueException(toStringMeta()+" : Unknown storage type ["+storageType+"] while converting to normal storage type");
    	}
    }

    /**
     * Converts the specified data object to the binary string storage type.
     * @param object the data object to convert
     * @return the data in a binary string storage type
     * @throws KettleValueException In case there is a data conversion error.
     */
    public Object convertToBinaryStringStorageType(Object object) throws KettleValueException
    {
    	if (object==null) return null;
    	
    	switch(storageType)
    	{
    	case STORAGE_TYPE_NORMAL: 
    		return convertNormalStorageTypeToBinaryString(object);
    	case STORAGE_TYPE_BINARY_STRING : 
    		return object;
    	case STORAGE_TYPE_INDEXED : 
    		return convertNormalStorageTypeToBinaryString( index[(Integer)object] );
        default: 
        	throw new KettleValueException(toStringMeta()+" : Unknown storage type ["+storageType+"] while converting to normal storage type");
    	}
    }

    /**
     * Convert the binary data to the actual data type.<br> 
     * - byte[] --> Long (Integer)
     * - byte[] --> Double (Number)
     * - byte[] --> BigDecimal (BigNumber)
     * - byte[] --> Date (Date)
     * - byte[] --> Boolean (Boolean)
     * - byte[] --> byte[] (Binary)
     * 
     * @param binary
     * @return
     * @throws KettleValueException
     */
    public Object convertBinaryStringToNativeType(byte[] binary) throws KettleValueException
    {
    	if (binary==null) return null;

    	numberOfBinaryStringConversions++; 
    	
    	// OK, so we have an internal representation of the original object, read from file.
        // First we decode it in the correct encoding 
        //
    	String string = convertBinaryStringToString(binary);
    	
    	// In this method we always must convert the data.
    	// We use the storageMetadata object to convert the binary string object. 
    	//
    	// --> Convert from the String format to the current data type...
        //
        return convertData(storageMetadata, string);
    }
    
    public Object convertNormalStorageTypeToBinaryString(Object object) throws KettleValueException
    {
    	if (object==null) return null;
    	
    	String string = getString(object);
    	
    	return convertStringToBinaryString(string);
    }


    private byte[] convertStringToBinaryString(String string) throws KettleValueException
    {
    	if (string==null) return null;
    	
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
                throw new KettleValueException(toString()+" : couldn't convert String to Binary with specified string encoding ["+stringEncoding+"]", e);
            }
        }
    }
    
    /**
     * Clones the data.  Normally, we don't have to do anything here, but just for arguments and safety, 
     * we do a little extra work in case of binary blobs and Date objects.
     * We should write a programmers manual later on to specify in all clarity that 
     * "we always overwrite/replace values in the Object[] data rows, we never modify them".
     * 
     * @return a cloned data object if needed
     */
    public Object cloneValueData(Object object) throws KettleValueException
    {
        if (object==null) return null;
        
        if (storageType==STORAGE_TYPE_NORMAL)
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
                
            case ValueMeta.TYPE_SERIALIZABLE:
              // Let's not create a copy but simply return the same value.
              //
              return object;

            default: throw new KettleValueException(toString()+": unable to make copy of value type: "+getType());
            }
        }
        else {
        	
        	return object;
        	
        }
    }
    
    public String getCompatibleString(Object object) throws KettleValueException
    {
        try
        {
            String string;
            
            switch(type)
            {
            case TYPE_DATE:
                switch(storageType)
                {
                case STORAGE_TYPE_NORMAL:         string = convertDateToCompatibleString((Date)object); break;
                case STORAGE_TYPE_BINARY_STRING:  string = convertDateToCompatibleString((Date)convertBinaryStringToNativeType((byte[])object)); break;
                case STORAGE_TYPE_INDEXED:        string = object==null ? null : convertDateToCompatibleString((Date)index[((Integer)object).intValue()]); break;
                default: throw new KettleValueException(toString()+" : Unknown storage type "+storageType+" specified.");
                }
                break;
    
            case TYPE_NUMBER:
                switch(storageType)
                {
                case STORAGE_TYPE_NORMAL:         string = convertNumberToCompatibleString((Double)object); break;
                case STORAGE_TYPE_BINARY_STRING:  string = convertNumberToCompatibleString((Double)convertBinaryStringToNativeType((byte[])object)); break;
                case STORAGE_TYPE_INDEXED:        string = object==null ? null : convertNumberToCompatibleString((Double)index[((Integer)object).intValue()]); break;
                default: throw new KettleValueException(toString()+" : Unknown storage type "+storageType+" specified.");
                }
                break;
    
            case TYPE_INTEGER:
                switch(storageType)
                {
                case STORAGE_TYPE_NORMAL:         string = convertIntegerToCompatibleString((Long)object); break;
                case STORAGE_TYPE_BINARY_STRING:  string = convertIntegerToCompatibleString((Long)convertBinaryStringToNativeType((byte[])object)); break;
                case STORAGE_TYPE_INDEXED:        string = object==null ? null : convertIntegerToCompatibleString((Long)index[((Integer)object).intValue()]); break;
                default: throw new KettleValueException(toString()+" : Unknown storage type "+storageType+" specified.");
                }
                break;
    
            default: 
                return getString(object);
            }
            
            return string;
        }
        catch(ClassCastException e)
        {
        	throw new KettleValueException(toString()+" : There was a data type error: the data type of "+object.getClass().getName()+" object ["+object+"] does not correspond to value meta ["+toStringMeta()+"]");
        }
    }

    public String getString(Object object) throws KettleValueException
    {
        try
        {
            String string;
            
            switch(type)
            {
            case TYPE_STRING:
                switch(storageType)
                {
                case STORAGE_TYPE_NORMAL:         string = object==null ? null : object.toString(); break;
                case STORAGE_TYPE_BINARY_STRING:  string = (String)convertBinaryStringToNativeType((byte[])object); break;
                case STORAGE_TYPE_INDEXED:        string = object==null ? null : (String) index[((Integer)object).intValue()];  break;
                default: throw new KettleValueException(toString()+" : Unknown storage type "+storageType+" specified.");
                }
                if ( string != null )
                    string = trim(string);
                break;
                
            case TYPE_DATE:
                switch(storageType)
                {
                case STORAGE_TYPE_NORMAL:         string = convertDateToString((Date)object); break;
                case STORAGE_TYPE_BINARY_STRING:  string = convertDateToString((Date)convertBinaryStringToNativeType((byte[])object)); break;
                case STORAGE_TYPE_INDEXED:        string = object==null ? null : convertDateToString((Date)index[((Integer)object).intValue()]); break;
                default: throw new KettleValueException(toString()+" : Unknown storage type "+storageType+" specified.");
                }
                break;
    
            case TYPE_NUMBER:
                switch(storageType)
                {
                case STORAGE_TYPE_NORMAL:         string = convertNumberToString((Double)object); break;
                case STORAGE_TYPE_BINARY_STRING:  string = convertNumberToString((Double)convertBinaryStringToNativeType((byte[])object)); break;
                case STORAGE_TYPE_INDEXED:        string = object==null ? null : convertNumberToString((Double)index[((Integer)object).intValue()]); break;
                default: throw new KettleValueException(toString()+" : Unknown storage type "+storageType+" specified.");
                }
                break;
    
            case TYPE_INTEGER:
                switch(storageType)
                {
                case STORAGE_TYPE_NORMAL:         string = convertIntegerToString((Long)object); break;
                case STORAGE_TYPE_BINARY_STRING:  string = convertIntegerToString((Long)convertBinaryStringToNativeType((byte[])object)); break;
                case STORAGE_TYPE_INDEXED:        string = object==null ? null : convertIntegerToString((Long)index[((Integer)object).intValue()]); break;
                default: throw new KettleValueException(toString()+" : Unknown storage type "+storageType+" specified.");
                }
                break;
    
            case TYPE_BIGNUMBER:
                switch(storageType)
                {
                case STORAGE_TYPE_NORMAL:         string = convertBigNumberToString((BigDecimal)object); break;
                case STORAGE_TYPE_BINARY_STRING:  string = convertBigNumberToString((BigDecimal)convertBinaryStringToNativeType((byte[])object)); break;
                case STORAGE_TYPE_INDEXED:        string = object==null ? null : convertBigNumberToString((BigDecimal)index[((Integer)object).intValue()]); break;
                default: throw new KettleValueException(toString()+" : Unknown storage type "+storageType+" specified.");
                }
                break;
    
            case TYPE_BOOLEAN:
                switch(storageType)
                {
                case STORAGE_TYPE_NORMAL:         string = convertBooleanToString((Boolean)object); break;
                case STORAGE_TYPE_BINARY_STRING:  string = convertBooleanToString((Boolean)convertBinaryStringToNativeType((byte[])object)); break;
                case STORAGE_TYPE_INDEXED:        string = object==null ? null : convertBooleanToString((Boolean)index[((Integer)object).intValue()]); break;
                default: throw new KettleValueException(toString()+" : Unknown storage type "+storageType+" specified.");
                }
                break;
    
            case TYPE_BINARY:
                switch(storageType)
                {
                case STORAGE_TYPE_NORMAL:         string = convertBinaryStringToString((byte[])object); break;
                case STORAGE_TYPE_BINARY_STRING:  string = convertBinaryStringToString((byte[])object); break;
                case STORAGE_TYPE_INDEXED:        string = object==null ? null : convertBinaryStringToString((byte[])index[((Integer)object).intValue()]); break;
                default: throw new KettleValueException(toString()+" : Unknown storage type "+storageType+" specified.");
                }
                break;
    
            case TYPE_SERIALIZABLE:
                switch(storageType)
                {
                case STORAGE_TYPE_NORMAL:         string = object.toString();  break; // just go for the default toString()
                case STORAGE_TYPE_BINARY_STRING:  string = convertBinaryStringToString((byte[])object); break;
                case STORAGE_TYPE_INDEXED:        string = object==null ? null : index[((Integer)object).intValue()].toString();  break; // just go for the default toString()
                default: throw new KettleValueException(toString()+" : Unknown storage type "+storageType+" specified.");
                }
                break;
    
            default: 
                throw new KettleValueException(toString()+" : Unknown type "+type+" specified.");
            }
            
            if (isOutputPaddingEnabled() && getLength()>0)
            {
                string = ValueDataUtil.rightPad(string, getLength());
            }

            return string;
        }
        catch(ClassCastException e)
        {
        	throw new KettleValueException(toString()+" : There was a data type error: the data type of "+object.getClass().getName()+" object ["+object+"] does not correspond to value meta ["+toStringMeta()+"]");
        }
    }

    private String trim(String string) {
        switch(getTrimType()) {
        case TRIM_TYPE_NONE : break;
        case TRIM_TYPE_RIGHT : string = Const.rtrim(string); break;
        case TRIM_TYPE_LEFT  : string = Const.ltrim(string); break;
        case TRIM_TYPE_BOTH  : string = Const.trim(string); break;
        default: break;
        }
        return string;
	}

	public Double getNumber(Object object) throws KettleValueException
    {
		try
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
	            case STORAGE_TYPE_NORMAL:         return (Double)object;
	            case STORAGE_TYPE_BINARY_STRING:  return (Double)convertBinaryStringToNativeType((byte[])object);
	            case STORAGE_TYPE_INDEXED:        return (Double)index[((Integer)object).intValue()];
	            default: throw new KettleValueException(toString()+" : Unknown storage type "+storageType+" specified.");
	            }
	        case TYPE_STRING:
	            switch(storageType)
	            {
	            case STORAGE_TYPE_NORMAL:         return convertStringToNumber((String)object);
	            case STORAGE_TYPE_BINARY_STRING:  return convertStringToNumber((String)convertBinaryStringToNativeType((byte[])object));
	            case STORAGE_TYPE_INDEXED:        return convertStringToNumber((String) index[((Integer)object).intValue()]); 
	            default: throw new KettleValueException(toString()+" : Unknown storage type "+storageType+" specified.");
	            }
	        case TYPE_DATE:
	            switch(storageType)
	            {
	            case STORAGE_TYPE_NORMAL:         return convertDateToNumber((Date)object);
	            case STORAGE_TYPE_BINARY_STRING:  return convertDateToNumber((Date)convertBinaryStringToNativeType((byte[])object));
	            case STORAGE_TYPE_INDEXED:        return new Double( ((Date)index[((Integer)object).intValue()]).getTime() );  
	            default: throw new KettleValueException(toString()+" : Unknown storage type "+storageType+" specified.");
	            }
	        case TYPE_INTEGER:
	            switch(storageType)
	            {
	            case STORAGE_TYPE_NORMAL:         return new Double( ((Long)object).doubleValue() );
	            case STORAGE_TYPE_BINARY_STRING:  return new Double( ((Long)convertBinaryStringToNativeType((byte[])object)).doubleValue() );
	            case STORAGE_TYPE_INDEXED:        return new Double( ((Long)index[((Integer)object).intValue()]).doubleValue() );
	            default: throw new KettleValueException(toString()+" : Unknown storage type "+storageType+" specified.");
	            }
	        case TYPE_BIGNUMBER:
	            switch(storageType)
	            {
	            case STORAGE_TYPE_NORMAL:         return new Double( ((BigDecimal)object).doubleValue() );
	            case STORAGE_TYPE_BINARY_STRING:  return new Double( ((BigDecimal)convertBinaryStringToNativeType((byte[])object)).doubleValue() );
	            case STORAGE_TYPE_INDEXED:        return new Double( ((BigDecimal)index[((Integer)object).intValue()]).doubleValue() );
	            default: throw new KettleValueException(toString()+" : Unknown storage type "+storageType+" specified.");
	            }
	        case TYPE_BOOLEAN:
	            switch(storageType)
	            {
	            case STORAGE_TYPE_NORMAL:         return convertBooleanToNumber( (Boolean)object );
	            case STORAGE_TYPE_BINARY_STRING:  return convertBooleanToNumber( (Boolean)convertBinaryStringToNativeType((byte[])object) );
	            case STORAGE_TYPE_INDEXED:        return convertBooleanToNumber( (Boolean)index[((Integer)object).intValue()] );
	            default: throw new KettleValueException(toString()+" : Unknown storage type "+storageType+" specified.");
	            }
	        case TYPE_BINARY:
	            throw new KettleValueException(toString()+" : I don't know how to convert binary values to numbers.");
	        case TYPE_SERIALIZABLE:
	            throw new KettleValueException(toString()+" : I don't know how to convert serializable values to numbers.");
	        default:
	            throw new KettleValueException(toString()+" : Unknown type "+type+" specified.");
	        }
    	}
    	catch(Exception e)
    	{
    		throw new KettleValueException("Unexpected conversion error while converting value ["+toString()+"] to a Number", e);
    	}
    }

    public Long getInteger(Object object) throws KettleValueException
    {
    	try
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
	            case STORAGE_TYPE_NORMAL:         return (Long)object;
	            case STORAGE_TYPE_BINARY_STRING:  return (Long)convertBinaryStringToNativeType((byte[])object);
	            case STORAGE_TYPE_INDEXED:        return (Long)index[((Integer)object).intValue()];
	            default: throw new KettleValueException(toString()+" : Unknown storage type "+storageType+" specified.");
	            }
	        case TYPE_STRING:
	            switch(storageType)
	            {
	            case STORAGE_TYPE_NORMAL:         return convertStringToInteger((String)object);
	            case STORAGE_TYPE_BINARY_STRING:  return convertStringToInteger((String)convertBinaryStringToNativeType((byte[])object));
	            case STORAGE_TYPE_INDEXED:        return convertStringToInteger((String) index[((Integer)object).intValue()]); 
	            default: throw new KettleValueException(toString()+" : Unknown storage type "+storageType+" specified.");
	            }
	        case TYPE_NUMBER:
	            switch(storageType)
	            {
	            case STORAGE_TYPE_NORMAL:         return new Long( Math.round(((Double)object).doubleValue()) );
	            case STORAGE_TYPE_BINARY_STRING:  return new Long( Math.round(((Double)convertBinaryStringToNativeType((byte[])object)).doubleValue()) );
	            case STORAGE_TYPE_INDEXED:        return new Long( Math.round(((Double)index[((Integer)object).intValue()]).doubleValue()) );
	            default: throw new KettleValueException(toString()+" : Unknown storage type "+storageType+" specified.");
	            }
	        case TYPE_DATE:
	            switch(storageType)
	            {
	            case STORAGE_TYPE_NORMAL:         return convertDateToInteger( (Date)object);
	            case STORAGE_TYPE_BINARY_STRING:  return new Long( ((Date)convertBinaryStringToNativeType((byte[])object)).getTime() );
	            case STORAGE_TYPE_INDEXED:        return convertDateToInteger( (Date)index[((Integer)object).intValue()]);  
	            default: throw new KettleValueException(toString()+" : Unknown storage type "+storageType+" specified.");
	            }
	        case TYPE_BIGNUMBER:
	            switch(storageType)
	            {
	            case STORAGE_TYPE_NORMAL:         return new Long( ((BigDecimal)object).longValue() );
	            case STORAGE_TYPE_BINARY_STRING:  return new Long( ((BigDecimal)convertBinaryStringToNativeType((byte[])object)).longValue() );
	            case STORAGE_TYPE_INDEXED:        return new Long( ((BigDecimal)index[((Integer)object).intValue()]).longValue() );
	            default: throw new KettleValueException(toString()+" : Unknown storage type "+storageType+" specified.");
	            }
	        case TYPE_BOOLEAN:
	            switch(storageType)
	            {
	            case STORAGE_TYPE_NORMAL:         return convertBooleanToInteger( (Boolean)object );
	            case STORAGE_TYPE_BINARY_STRING:  return convertBooleanToInteger( (Boolean)convertBinaryStringToNativeType((byte[])object) );
	            case STORAGE_TYPE_INDEXED:        return convertBooleanToInteger( (Boolean)index[((Integer)object).intValue()] );
	            default: throw new KettleValueException(toString()+" : Unknown storage type "+storageType+" specified.");
	            }
	        case TYPE_BINARY:
	            throw new KettleValueException(toString()+" : I don't know how to convert binary values to integers.");
	        case TYPE_SERIALIZABLE:
	            throw new KettleValueException(toString()+" : I don't know how to convert serializable values to integers.");
	        default:
	            throw new KettleValueException(toString()+" : Unknown type "+type+" specified.");
	        }
    	}
    	catch(Exception e)
    	{
    		throw new KettleValueException("Unexpected conversion error while converting value ["+toString()+"] to an Integer", e);
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
            case STORAGE_TYPE_NORMAL:         return (BigDecimal)object;
            case STORAGE_TYPE_BINARY_STRING:  return (BigDecimal)convertBinaryStringToNativeType((byte[])object);
            case STORAGE_TYPE_INDEXED:        return (BigDecimal)index[((Integer)object).intValue()];
            default: throw new KettleValueException(toString()+" : Unknown storage type "+storageType+" specified.");
            }
        case TYPE_STRING:
            switch(storageType)
            {
            case STORAGE_TYPE_NORMAL:         return convertStringToBigNumber( (String)object );
            case STORAGE_TYPE_BINARY_STRING:  return convertStringToBigNumber( (String)convertBinaryStringToNativeType((byte[])object) );
            case STORAGE_TYPE_INDEXED:        return convertStringToBigNumber((String) index[((Integer)object).intValue()]); 
            default: throw new KettleValueException(toString()+" : Unknown storage type "+storageType+" specified.");
            }
        case TYPE_INTEGER:
            switch(storageType)
            {
            case STORAGE_TYPE_NORMAL:         return BigDecimal.valueOf( ((Long)object).longValue() );
            case STORAGE_TYPE_BINARY_STRING:  return BigDecimal.valueOf( ((Long)convertBinaryStringToNativeType((byte[])object)).longValue() );
            case STORAGE_TYPE_INDEXED:        return BigDecimal.valueOf( ((Long)index[((Integer)object).intValue()]).longValue() );
            default: throw new KettleValueException(toString()+" : Unknown storage type "+storageType+" specified.");
            }
        case TYPE_NUMBER:
            switch(storageType)
            {
            case STORAGE_TYPE_NORMAL:         return BigDecimal.valueOf( ((Double)object).doubleValue() );
            case STORAGE_TYPE_BINARY_STRING:  return BigDecimal.valueOf( ((Double)convertBinaryStringToNativeType((byte[])object)).doubleValue() );
            case STORAGE_TYPE_INDEXED:        return BigDecimal.valueOf( ((Double)index[((Integer)object).intValue()]).doubleValue() );
            default: throw new KettleValueException(toString()+" : Unknown storage type "+storageType+" specified.");
            }
        case TYPE_DATE:
            switch(storageType)
            {
            case STORAGE_TYPE_NORMAL:         return convertDateToBigNumber( (Date)object );
            case STORAGE_TYPE_BINARY_STRING:  return convertDateToBigNumber( (Date)convertBinaryStringToNativeType((byte[])object) );
            case STORAGE_TYPE_INDEXED:        return convertDateToBigNumber( (Date)index[((Integer)object).intValue()] );  
            default: throw new KettleValueException(toString()+" : Unknown storage type "+storageType+" specified.");
            }
        case TYPE_BOOLEAN:
            switch(storageType)
            {
            case STORAGE_TYPE_NORMAL:         return convertBooleanToBigNumber( (Boolean)object );
            case STORAGE_TYPE_BINARY_STRING:  return convertBooleanToBigNumber( (Boolean)convertBinaryStringToNativeType((byte[])object) );
            case STORAGE_TYPE_INDEXED:        return convertBooleanToBigNumber( (Boolean)index[((Integer)object).intValue()] );
            default: throw new KettleValueException(toString()+" : Unknown storage type "+storageType+" specified.");
            }
        case TYPE_BINARY:
            throw new KettleValueException(toString()+" : I don't know how to convert binary values to integers.");
        case TYPE_SERIALIZABLE:
            throw new KettleValueException(toString()+" : I don't know how to convert serializable values to integers.");
        default:
            throw new KettleValueException(toString()+" : Unknown type "+type+" specified.");
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
            case STORAGE_TYPE_NORMAL:         return (Boolean)object;
            case STORAGE_TYPE_BINARY_STRING:  return (Boolean)convertBinaryStringToNativeType((byte[])object);
            case STORAGE_TYPE_INDEXED:        return (Boolean)index[((Integer)object).intValue()];
            default: throw new KettleValueException(toString()+" : Unknown storage type "+storageType+" specified.");
            }
        case TYPE_STRING:
            switch(storageType)
            {
            case STORAGE_TYPE_NORMAL:         return convertStringToBoolean( trim((String)object) );
            case STORAGE_TYPE_BINARY_STRING:  return convertStringToBoolean( trim((String)convertBinaryStringToNativeType((byte[])object)) );
            case STORAGE_TYPE_INDEXED:        return convertStringToBoolean( trim((String) index[((Integer)object).intValue()] )); 
            default: throw new KettleValueException(toString()+" : Unknown storage type "+storageType+" specified.");
            }
        case TYPE_INTEGER:
            switch(storageType)
            {
            case STORAGE_TYPE_NORMAL:         return convertIntegerToBoolean( (Long)object );
            case STORAGE_TYPE_BINARY_STRING:  return convertIntegerToBoolean( (Long)convertBinaryStringToNativeType((byte[])object) );
            case STORAGE_TYPE_INDEXED:        return convertIntegerToBoolean( (Long)index[((Integer)object).intValue()] );
            default: throw new KettleValueException(toString()+" : Unknown storage type "+storageType+" specified.");
            }
        case TYPE_NUMBER:
            switch(storageType)
            {
            case STORAGE_TYPE_NORMAL:         return convertNumberToBoolean( (Double)object );
            case STORAGE_TYPE_BINARY_STRING:  return convertNumberToBoolean( (Double)convertBinaryStringToNativeType((byte[])object) );
            case STORAGE_TYPE_INDEXED:        return convertNumberToBoolean( (Double)index[((Integer)object).intValue()] );
            default: throw new KettleValueException(toString()+" : Unknown storage type "+storageType+" specified.");
            }
        case TYPE_BIGNUMBER:
            switch(storageType)
            {
            case STORAGE_TYPE_NORMAL:         return convertBigNumberToBoolean( (BigDecimal)object );
            case STORAGE_TYPE_BINARY_STRING:  return convertBigNumberToBoolean( (BigDecimal)convertBinaryStringToNativeType((byte[])object) );
            case STORAGE_TYPE_INDEXED:        return convertBigNumberToBoolean( (BigDecimal)index[((Integer)object).intValue()] );
            default: throw new KettleValueException(toString()+" : Unknown storage type "+storageType+" specified.");
            }
        case TYPE_DATE:
            throw new KettleValueException(toString()+" : I don't know how to convert date values to booleans.");
        case TYPE_BINARY:
            throw new KettleValueException(toString()+" : I don't know how to convert binary values to booleans.");
        case TYPE_SERIALIZABLE:
            throw new KettleValueException(toString()+" : I don't know how to convert serializable values to booleans.");
        default:
            throw new KettleValueException(toString()+" : Unknown type "+type+" specified.");
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
            case STORAGE_TYPE_NORMAL:         return (Date)object;
            case STORAGE_TYPE_BINARY_STRING:  return (Date)convertBinaryStringToNativeType((byte[])object);
            case STORAGE_TYPE_INDEXED:        return (Date)index[((Integer)object).intValue()];  
            default: throw new KettleValueException(toString()+" : Unknown storage type "+storageType+" specified.");
            }
        case TYPE_STRING:
            switch(storageType)
            {
            case STORAGE_TYPE_NORMAL:         return convertStringToDate( (String)object );
            case STORAGE_TYPE_BINARY_STRING:  return convertStringToDate( (String)convertBinaryStringToNativeType((byte[])object) );
            case STORAGE_TYPE_INDEXED:        return convertStringToDate( (String) index[((Integer)object).intValue()] ); 
            default: throw new KettleValueException(toString()+" : Unknown storage type "+storageType+" specified.");
            }
        case TYPE_NUMBER:
            switch(storageType)
            {
            case STORAGE_TYPE_NORMAL:         return convertNumberToDate((Double)object);
            case STORAGE_TYPE_BINARY_STRING:  return convertNumberToDate((Double)convertBinaryStringToNativeType((byte[])object) );
            case STORAGE_TYPE_INDEXED:        return convertNumberToDate((Double)index[((Integer)object).intValue()]);
            default: throw new KettleValueException(toString()+" : Unknown storage type "+storageType+" specified.");
            }
        case TYPE_INTEGER:
            switch(storageType)
            {
            case STORAGE_TYPE_NORMAL:         return convertIntegerToDate((Long)object);
            case STORAGE_TYPE_BINARY_STRING:  return convertIntegerToDate((Long)convertBinaryStringToNativeType((byte[])object));
            case STORAGE_TYPE_INDEXED:        return convertIntegerToDate((Long)index[((Integer)object).intValue()]);
            default: throw new KettleValueException(toString()+" : Unknown storage type "+storageType+" specified.");
            }
        case TYPE_BIGNUMBER:
            switch(storageType)
            {
            case STORAGE_TYPE_NORMAL:         return convertBigNumberToDate((BigDecimal)object);
            case STORAGE_TYPE_BINARY_STRING:  return convertBigNumberToDate((BigDecimal)convertBinaryStringToNativeType((byte[])object));
            case STORAGE_TYPE_INDEXED:        return convertBigNumberToDate((BigDecimal)index[((Integer)object).intValue()]);
            default: throw new KettleValueException(toString()+" : Unknown storage type "+storageType+" specified.");
            }
        case TYPE_BOOLEAN:
            throw new KettleValueException(toString()+" : I don't know how to convert a boolean to a date.");
        case TYPE_BINARY:
            throw new KettleValueException(toString()+" : I don't know how to convert a binary value to date.");
        case TYPE_SERIALIZABLE:
            throw new KettleValueException(toString()+" : I don't know how to convert a serializable value to date.");
            
        default: 
            throw new KettleValueException(toString()+" : Unknown type "+type+" specified.");
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
            case STORAGE_TYPE_NORMAL:         return (byte[])object;
            case STORAGE_TYPE_BINARY_STRING:  return (byte[])object;
            case STORAGE_TYPE_INDEXED:        return (byte[])index[((Integer)object).intValue()];  
            default: throw new KettleValueException(toString()+" : Unknown storage type "+storageType+" specified.");
            }
        case TYPE_DATE:
            throw new KettleValueException(toString()+" : I don't know how to convert a date to binary.");
        case TYPE_STRING:
            switch(storageType)
            {
            case STORAGE_TYPE_NORMAL:         return convertStringToBinaryString( (String)object );
            case STORAGE_TYPE_BINARY_STRING:  return (byte[])object;
            case STORAGE_TYPE_INDEXED:        return convertStringToBinaryString( (String) index[((Integer)object).intValue()] ); 
            default: throw new KettleValueException(toString()+" : Unknown storage type "+storageType+" specified.");
            }
        case TYPE_NUMBER:
            throw new KettleValueException(toString()+" : I don't know how to convert a number to binary.");
        case TYPE_INTEGER:
            throw new KettleValueException(toString()+" : I don't know how to convert an integer to binary.");
        case TYPE_BIGNUMBER:
            throw new KettleValueException(toString()+" : I don't know how to convert a bignumber to binary.");
        case TYPE_BOOLEAN:
            throw new KettleValueException(toString()+" : I don't know how to convert a boolean to binary.");
        case TYPE_SERIALIZABLE:
            throw new KettleValueException(toString()+" : I don't know how to convert a serializable to binary.");
            
        default: 
            throw new KettleValueException(toString()+" : Unknown type "+type+" specified.");
        }
    }
    
    public byte[] getBinaryString(Object object) throws KettleValueException
    {
    	// If the input is a binary string, we should return the exact same binary object IF
    	// and only IF the formatting options for the storage metadata and this object are the same.
    	//
    	if (isStorageBinaryString() && identicalFormat)
    	{
    		return (byte[]) object; // shortcut it directly for better performance.
    	}
    	
        try
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
                case STORAGE_TYPE_NORMAL:         return convertStringToBinaryString((String)object);
                case STORAGE_TYPE_BINARY_STRING:  return convertStringToBinaryString((String)convertBinaryStringToNativeType((byte[])object));
                case STORAGE_TYPE_INDEXED:        return convertStringToBinaryString((String) index[((Integer)object).intValue()]);
                default: throw new KettleValueException(toString()+" : Unknown storage type "+storageType+" specified.");
                }
                
            case TYPE_DATE:
                switch(storageType)
                {
                case STORAGE_TYPE_NORMAL:         return convertStringToBinaryString(convertDateToString((Date)object));
                case STORAGE_TYPE_BINARY_STRING:  return convertStringToBinaryString(convertDateToString((Date)convertBinaryStringToNativeType((byte[])object)));
                case STORAGE_TYPE_INDEXED:        return convertStringToBinaryString(convertDateToString((Date)index[((Integer)object).intValue()]));
                default: throw new KettleValueException(toString()+" : Unknown storage type "+storageType+" specified.");
                }
    
            case TYPE_NUMBER:
                switch(storageType)
                {
                case STORAGE_TYPE_NORMAL:         return convertStringToBinaryString(convertNumberToString((Double)object));
                case STORAGE_TYPE_BINARY_STRING:  return convertStringToBinaryString(convertNumberToString((Double)convertBinaryStringToNativeType((byte[])object)));
                case STORAGE_TYPE_INDEXED:        return convertStringToBinaryString(convertNumberToString((Double)index[((Integer)object).intValue()]));
                default: throw new KettleValueException(toString()+" : Unknown storage type "+storageType+" specified.");
                }
    
            case TYPE_INTEGER:
                switch(storageType)
                {
                case STORAGE_TYPE_NORMAL:         return convertStringToBinaryString(convertIntegerToString((Long)object));
                case STORAGE_TYPE_BINARY_STRING:  return convertStringToBinaryString(convertIntegerToString((Long)convertBinaryStringToNativeType((byte[])object)));
                case STORAGE_TYPE_INDEXED:        return convertStringToBinaryString(convertIntegerToString((Long)index[((Integer)object).intValue()]));
                default: throw new KettleValueException(toString()+" : Unknown storage type "+storageType+" specified.");
                }
    
            case TYPE_BIGNUMBER:
                switch(storageType)
                {
                case STORAGE_TYPE_NORMAL:         return convertStringToBinaryString(convertBigNumberToString((BigDecimal)object));
                case STORAGE_TYPE_BINARY_STRING:  return convertStringToBinaryString(convertBigNumberToString((BigDecimal)convertBinaryStringToNativeType((byte[])object)));
                case STORAGE_TYPE_INDEXED:        return convertStringToBinaryString(convertBigNumberToString((BigDecimal)index[((Integer)object).intValue()]));
                default: throw new KettleValueException(toString()+" : Unknown storage type "+storageType+" specified.");
                }
    
            case TYPE_BOOLEAN:
                switch(storageType)
                {
                case STORAGE_TYPE_NORMAL:         return convertStringToBinaryString(convertBooleanToString((Boolean)object));
                case STORAGE_TYPE_BINARY_STRING:  return convertStringToBinaryString(convertBooleanToString((Boolean)convertBinaryStringToNativeType((byte[])object)));
                case STORAGE_TYPE_INDEXED:        return convertStringToBinaryString(convertBooleanToString((Boolean)index[((Integer)object).intValue()]));
                default: throw new KettleValueException(toString()+" : Unknown storage type "+storageType+" specified.");
                }
    
            case TYPE_BINARY:
                switch(storageType)
                {
                case STORAGE_TYPE_NORMAL:         return (byte[])object;
                case STORAGE_TYPE_BINARY_STRING:  return (byte[])object;
                case STORAGE_TYPE_INDEXED:        return (byte[])index[((Integer)object).intValue()];
                default: throw new KettleValueException(toString()+" : Unknown storage type "+storageType+" specified.");
                }
    
            case TYPE_SERIALIZABLE:
                switch(storageType)
                {
                case STORAGE_TYPE_NORMAL:         return convertStringToBinaryString(object.toString());
                case STORAGE_TYPE_BINARY_STRING:  return (byte[])object;
                case STORAGE_TYPE_INDEXED:        return convertStringToBinaryString( index[((Integer)object).intValue()].toString() );
                default: throw new KettleValueException(toString()+" : Unknown storage type "+storageType+" specified.");
                }
    
            default: 
                throw new KettleValueException(toString()+" : Unknown type "+type+" specified.");
            }
        }
        catch(ClassCastException e)
        {
            throw new KettleValueException(toString()+" : There was a data type error: the data type of "+object.getClass().getName()+" object ["+object+"] does not correspond to value meta ["+toStringMeta()+"]");
        }
    }
    
    
    /**
     * Checks whether or not the value is a String.
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

    /**
     * Return the storage type of a value in a textual form: "normal", "binary-string", "indexes"
     * @return A String describing the storage type of the value metadata
     */
    public String getStorageTypeDesc()
    {
        return storageTypeCodes[storageType];
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
        
        if (!isStorageNormal())
        {
        	retval.append("<").append(getStorageTypeDesc()).append(">");
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
                    default: throw new KettleFileException(toString()+" : Unable to serialize data type "+getType());
                    }
                    break;
                    
                case STORAGE_TYPE_BINARY_STRING:
                    // Handle binary string content -- only when not NULL
                	// In this case, we opt not to convert anything at all for speed.
                	// That way, we can save on CPU power.
                	// Since the streams can be compressed, volume shouldn't be an issue at all.
                	//
                	writeBinaryString(outputStream, (byte[])object);
                    break;
                    
                case STORAGE_TYPE_INDEXED:
                    writeInteger(outputStream, (Integer)object); // just an index 
                    break;
                    
                default: throw new KettleFileException(toString()+" : Unknown storage type "+getStorageType());
                }
            }
        }
        catch(ClassCastException e) {
        	throw new RuntimeException(toString()+" : There was a data type error: the data type of "+object.getClass().getName()+" object ["+object+"] does not correspond to value meta ["+toStringMeta()+"]");
        }
        catch(IOException e)
        {
            throw new KettleFileException(toString()+" : Unable to write value data to output stream", e);
        }
        
    }
    
    public Object readData(DataInputStream inputStream) throws KettleFileException, KettleEOFException, SocketTimeoutException
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
                default: throw new KettleFileException(toString()+" : Unable to de-serialize data of type "+getType());
                }
            
            case STORAGE_TYPE_BINARY_STRING:
                return readBinaryString(inputStream);
                
            case STORAGE_TYPE_INDEXED:
                return readSmallInteger(inputStream); // just an index: 4-bytes should be enough.
                
            default: throw new KettleFileException(toString()+" : Unknown storage type "+getStorageType());
            }
        }
        catch(EOFException e)
        {
        	throw new KettleEOFException(e);
        }
        catch(SocketTimeoutException e)
        {
        	throw e;
        }
        catch(IOException e)
        {
            throw new KettleFileException(toString()+" : Unable to read value data from input stream", e);
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
    
    private void writeBinaryString(DataOutputStream outputStream, byte[] binaryString) throws IOException
    {
        // Write the length and then the bytes
        if (binaryString==null)
        {
            outputStream.writeInt(-1);
        }
        else
        {
            outputStream.writeInt(binaryString.length);
            outputStream.write(binaryString);
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
        
        byte[] chars = new byte[length];
        inputStream.readFully(chars);

        String string = new String(chars, Const.XML_ENCODING);         
        // System.out.println("Read string("+getName()+"), length "+length+": "+string);
        return string;
    }
    
    private byte[] readBinaryString(DataInputStream inputStream) throws IOException
    {
        // Read the length and then the bytes
        int length = inputStream.readInt();
        if (length<0) 
        {
            return null;
        }
        
        byte[] chars = new byte[length];
        inputStream.readFully(chars);

        return chars;
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
        Boolean bool = Boolean.valueOf( inputStream.readBoolean() );
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
        Integer i = Integer.valueOf( inputStream.readInt() );
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
            
            switch(storageType) {
            case STORAGE_TYPE_INDEXED:
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
		                    try {
							    switch(type)
							    {
							    case TYPE_STRING:    writeString(outputStream, (String)index[i]); break; 
							    case TYPE_NUMBER:    writeNumber(outputStream, (Double)index[i]); break; 
							    case TYPE_INTEGER:   writeInteger(outputStream, (Long)index[i]); break; 
							    case TYPE_DATE:      writeDate(outputStream, (Date)index[i]); break; 
							    case TYPE_BIGNUMBER: writeBigNumber(outputStream, (BigDecimal)index[i]); break; 
							    case TYPE_BOOLEAN:   writeBoolean(outputStream, (Boolean)index[i]); break; 
							    case TYPE_BINARY:    writeBinary(outputStream, (byte[])index[i]); break;
							    default: throw new KettleFileException(toString()+" : Unable to serialize indexe storage type for data type "+getType());
							    }
							} catch (ClassCastException e) {
					        	throw new RuntimeException(toString()+" : There was a data type error: the data type of "+index[i].getClass().getName()+" object ["+index[i]+"] does not correspond to value meta ["+toStringMeta()+"]");
							}
						}
	                }
	            }
	            break;
	        
            case STORAGE_TYPE_BINARY_STRING:
	            {
	            	// Save the storage meta data...
	            	//
	            	outputStream.writeBoolean(storageMetadata!=null);
	            	
	            	if (storageMetadata!=null) {
	            		storageMetadata.writeMeta(outputStream);
	            	}
	            }
	            break;
	            
	       default:
	    	   break;
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
            outputStream.writeInt(trimType);
            
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
            throw new KettleFileException(toString()+" : Unable to write value metadata to output stream", e);
        }
    }
    
    public ValueMeta(DataInputStream inputStream) throws KettleFileException, KettleEOFException
    {
        this();
        
        try
        {
            // Handle type
            type=inputStream.readInt();
    
            // Handle storage type
            storageType = inputStream.readInt();
            
            // Read the data in the index
            switch(storageType) {
            case STORAGE_TYPE_INDEXED:
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
	                        default: throw new KettleFileException(toString()+" : Unable to de-serialize indexed storage type for data type "+getType());
	                        }
	                    }
	                }
	            }
	            break;
	            
            case STORAGE_TYPE_BINARY_STRING:
	            {
	            	// In case we do have storage metadata defined, we read that back in as well..
	            	if (inputStream.readBoolean()) {
	            		storageMetadata = new ValueMeta(inputStream);
	            	}
	            }
	            break;
	            
	        default:
	        	break;
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
            trimType=inputStream.readInt();
            
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
                dateFormatLocale = EnvUtil.createLocale(strDateFormatLocale);
            }
        }
        catch(EOFException e)
        {
        	throw new KettleEOFException(e);
        }
        catch(IOException e)
        {
            throw new KettleFileException(toString()+" : Unable to read value metadata from input stream", e);
        }
    }
    
    public String getMetaXML() throws IOException
    {
    	StringBuffer xml = new StringBuffer();
    	
    	xml.append(XMLHandler.openTag(XML_META_TAG));
    	
        xml.append( XMLHandler.addTagValue("type", getTypeDesc()) ) ;
        xml.append( XMLHandler.addTagValue("storagetype", getStorageTypeCode(getStorageType())) );

        switch(storageType) {
        case STORAGE_TYPE_INDEXED:
            {
            	xml.append( XMLHandler.openTag("index"));

                // Save the indexed strings...
            	//
                if (index!=null)
                {
                    for (int i=0;i<index.length;i++)
                    {
                    	try {
	                        switch(type)
	                        {
	                        case TYPE_STRING:    xml.append( XMLHandler.addTagValue( "value", (String)index[i]) ); break; 
	                        case TYPE_NUMBER:    xml.append( XMLHandler.addTagValue( "value",  (Double)index[i]) ); break; 
	                        case TYPE_INTEGER:   xml.append( XMLHandler.addTagValue( "value", (Long)index[i]) ); break; 
	                        case TYPE_DATE:      xml.append( XMLHandler.addTagValue( "value", (Date)index[i]) ); break; 
	                        case TYPE_BIGNUMBER: xml.append( XMLHandler.addTagValue( "value", (BigDecimal)index[i]) ); break; 
	                        case TYPE_BOOLEAN:   xml.append( XMLHandler.addTagValue( "value", (Boolean)index[i]) ); break; 
	                        case TYPE_BINARY:    xml.append( XMLHandler.addTagValue( "value", (byte[])index[i]) ); break;
	                        default: throw new IOException(toString()+" : Unable to serialize indexe storage type to XML for data type "+getType());
	                        }
						} catch (ClassCastException e) {
				        	throw new RuntimeException(toString()+" : There was a data type error: the data type of "+index[i].getClass().getName()+" object ["+index[i]+"] does not correspond to value meta ["+toStringMeta()+"]");
						}
                    }
                }
            	xml.append( XMLHandler.closeTag("index"));
            }
            break;
        
        case STORAGE_TYPE_BINARY_STRING:
            {
            	// Save the storage meta data...
            	//
            	if (storageMetadata!=null)
            	{
            		xml.append(XMLHandler.openTag("storage-meta"));
            		xml.append(storageMetadata.getMetaXML());
            		xml.append(XMLHandler.closeTag("storage-meta"));
            	}
            }
            break;
            
       default:
    	   break;
       }
        
        xml.append( XMLHandler.addTagValue("name", name) );  
        xml.append( XMLHandler.addTagValue("length", length) );  
        xml.append( XMLHandler.addTagValue("precision", precision) );  
        xml.append( XMLHandler.addTagValue("origin", origin) );  
        xml.append( XMLHandler.addTagValue("comments", comments) );  
        xml.append( XMLHandler.addTagValue("conversion_Mask", conversionMask) );  
        xml.append( XMLHandler.addTagValue("decimal_symbol", decimalSymbol) );  
        xml.append( XMLHandler.addTagValue("grouping_symbol", groupingSymbol) );  
        xml.append( XMLHandler.addTagValue("currency_symbol", currencySymbol) );  
        xml.append( XMLHandler.addTagValue("trim_type", getTrimTypeCode(trimType)) );
        xml.append( XMLHandler.addTagValue("case_insensitive", caseInsensitive) );
        xml.append( XMLHandler.addTagValue("sort_descending", sortedDescending) );
        xml.append( XMLHandler.addTagValue("output_padding", outputPaddingEnabled) );
        xml.append( XMLHandler.addTagValue("date_format_lenient", dateFormatLenient) );
        xml.append( XMLHandler.addTagValue("date_format_locale", dateFormatLocale.toString()) );
        
    	xml.append(XMLHandler.closeTag(XML_META_TAG));
    	
    	return xml.toString();
    }
    
    public ValueMeta(Node node) throws KettleException 
    {
    	this();
    	
        type = getType( XMLHandler.getTagValue(node, "type") ) ;
        storageType = getStorageType( XMLHandler.getTagValue(node, "storagetype") );

        switch(storageType) {
        case STORAGE_TYPE_INDEXED:
            {
            	Node indexNode = XMLHandler.getSubNode(node, "index");
            	int nrIndexes = XMLHandler.countNodes(indexNode, "value");
            	index = new Object[nrIndexes];
            	
        	    for (int i=0;i<index.length;i++)
                {
        	    	Node valueNode = XMLHandler.getSubNodeByNr(indexNode, "value", i);
        	    	String valueString = XMLHandler.getNodeValue(valueNode);
        	    	if (Const.isEmpty(valueString))
        	    	{
        	    		index[i] = null;
        	    	}
        	    	else
        	    	{
	                    switch(type)
	                    {
	                    case TYPE_STRING:    index[i] = valueString; break; 
	                    case TYPE_NUMBER:    index[i] = Double.parseDouble( valueString ); break; 
	                    case TYPE_INTEGER:   index[i] = Long.parseLong( valueString ); break; 
	                    case TYPE_DATE:      index[i] = XMLHandler.stringToDate( valueString ); ; break; 
	                    case TYPE_BIGNUMBER: index[i] = new BigDecimal( valueString ); ; break; 
	                    case TYPE_BOOLEAN:   index[i] = Boolean.valueOf("Y".equalsIgnoreCase( valueString)); break; 
	                    case TYPE_BINARY:    index[i] = XMLHandler.stringToBinary( valueString ); break;
	                    default: throw new KettleException(toString()+" : Unable to de-serialize indexe storage type from XML for data type "+getType());
	                    }
        	    	}
                }
            }
            break;
        
        case STORAGE_TYPE_BINARY_STRING:
            {
            	// Save the storage meta data...
            	//
            	Node storageMetaNode = XMLHandler.getSubNode(node, "storage-meta");
            	if (storageMetaNode!=null)
            	{
            		storageMetadata = new ValueMeta(storageMetaNode);
            	}
            }
            break;
            
       default:
    	   break;
       }
        
        name = XMLHandler.getTagValue(node, "name");  
        length =  Integer.parseInt( XMLHandler.getTagValue(node, "length") );  
        precision = Integer.parseInt( XMLHandler.getTagValue(node, "precision") );  
        origin = XMLHandler.getTagValue(node, "origin");  
        comments = XMLHandler.getTagValue(node, "comments");  
        conversionMask = XMLHandler.getTagValue(node, "conversion_Mask");  
        decimalSymbol = XMLHandler.getTagValue(node, "decimal_symbol");  
        groupingSymbol = XMLHandler.getTagValue(node, "grouping_symbol");  
        currencySymbol = XMLHandler.getTagValue(node, "currency_symbol");  
        trimType = getTrimTypeByCode( XMLHandler.getTagValue(node, "trim_type") );
        caseInsensitive = "Y".equalsIgnoreCase( XMLHandler.getTagValue(node, "case_insensitive") );
        sortedDescending = "Y".equalsIgnoreCase( XMLHandler.getTagValue(node, "sort_descending") );
        outputPaddingEnabled = "Y".equalsIgnoreCase( XMLHandler.getTagValue(node, "output_padding") );
        dateFormatLenient = "Y".equalsIgnoreCase( XMLHandler.getTagValue(node, "date_format_lenient") );
        String dateFormatLocaleString = XMLHandler.getTagValue(node, "date_format_locale");
        if (!Const.isEmpty( dateFormatLocaleString ))
        {
        	dateFormatLocale = EnvUtil.createLocale(dateFormatLocaleString);
        }
	}

    public String getDataXML(Object object) throws IOException
    {
    	StringBuffer xml = new StringBuffer();
    	
    	xml.append(XMLHandler.openTag(XML_DATA_TAG));
    	
        if (object!=null) // otherwise there is no point
        {
        	try {
	            switch(storageType)
	            {
	            case STORAGE_TYPE_NORMAL:
	                // Handle Content -- only when not NULL
	            	//
	                switch(getType())
	                {
	                case TYPE_STRING     : xml.append( (String)object ); break;
	                case TYPE_NUMBER     : xml.append( (Double)object ); break;
	                case TYPE_INTEGER    : xml.append( (Long)object ); break;
	                case TYPE_DATE       : xml.append( XMLHandler.date2string( (Date)object ) ); break;
	                case TYPE_BIGNUMBER  : xml.append( (BigDecimal)object ); break;
	                case TYPE_BOOLEAN    : xml.append( (Boolean)object ); break;
	                case TYPE_BINARY     : xml.append( XMLHandler.addTagValue("binary-value", (byte[])object) ); break;
	                default: throw new IOException(toString()+" : Unable to serialize data type to XML "+getType());
	                }
	                break;
	                
	            case STORAGE_TYPE_BINARY_STRING:
	                // Handle binary string content -- only when not NULL
	            	// In this case, we opt not to convert anything at all for speed.
	            	// That way, we can save on CPU power.
	            	// Since the streams can be compressed, volume shouldn't be an issue at all.
	            	//
	            	xml.append( XMLHandler.addTagValue("binary-string", (byte[])object) );
	                break;
	                
	            case STORAGE_TYPE_INDEXED:
	            	xml.append( XMLHandler.addTagValue("index-value", (Integer)object) ); // just an index 
	                break;
	                
	            default: throw new IOException(toString()+" : Unknown storage type "+getStorageType());
	            }
			} catch (ClassCastException e) {
		    	throw new RuntimeException(toString()+" : There was a data type error: the data type of "+object.getClass().getName()+" object ["+object+"] does not correspond to value meta ["+toStringMeta()+"]");
			}
        }
    	xml.append(XMLHandler.closeTag(XML_DATA_TAG));
    	
    	return xml.toString();
    }

    /**
     * Convert a data XML node to an Object that corresponds to the metadata.
     * This is basically String to Object conversion that is being done.
     * @param node the node to retrieve the data value from
     * @return the converted data value
     * @throws IOException thrown in case there is a problem with the XML to object conversion
     */
	public Object getValue(Node node) throws KettleException {
		
        switch(storageType)
        {
        case STORAGE_TYPE_NORMAL:
    		String valueString = XMLHandler.getNodeValue(node);
    		if (Const.isEmpty(valueString)) return null;
    		
            // Handle Content -- only when not NULL
        	//
            switch(getType())
            {
            case TYPE_STRING:    return valueString;
            case TYPE_NUMBER:    return Double.parseDouble( valueString ); 
            case TYPE_INTEGER:   return Long.parseLong( valueString );
            case TYPE_DATE:      return XMLHandler.stringToDate( valueString ); 
            case TYPE_BIGNUMBER: return new BigDecimal( valueString );
            case TYPE_BOOLEAN:   return Boolean.valueOf("Y".equalsIgnoreCase( valueString)); 
            case TYPE_BINARY:    return XMLHandler.stringToBinary( XMLHandler.getTagValue(node, "binary-value") );
            default: throw new KettleException(toString()+" : Unable to de-serialize '"+valueString+"' from XML for data type "+getType());
            }
            
        case STORAGE_TYPE_BINARY_STRING:
            // Handle binary string content -- only when not NULL
        	// In this case, we opt not to convert anything at all for speed.
        	// That way, we can save on CPU power.
        	// Since the streams can be compressed, volume shouldn't be an issue at all.
        	//
        	String binaryString = XMLHandler.getTagValue(node, "binary-string");
    		if (Const.isEmpty(binaryString)) return null;
    		
    		return XMLHandler.stringToBinary(binaryString);
            
        case STORAGE_TYPE_INDEXED:
        	String indexString = XMLHandler.getTagValue(node, "index-value");
    		if (Const.isEmpty(indexString)) return null;

    		return Integer.parseInt(indexString); 
            
        default: throw new KettleException(toString()+" : Unknown storage type "+getStorageType());
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
     * @return The integer type of the given String.  (ValueMetaInterface.TYPE_...)
     */
    public static final int getType(String desc)
    {
        for (int i=1;i<typeCodes.length;i++)
        {
            if (typeCodes[i].equalsIgnoreCase(desc))
            {
                return i; 
            }
        }

        return TYPE_NONE;
    }
    

    /**
     * Convert the String description of a storage type to an integer type.
     * @param desc The description of the storage type to convert
     * @return The integer storage type of the given String.  (ValueMetaInterface.STORAGE_TYPE_...) or -1 if the storage type code not be found.
     */
    public static final int getStorageType(String desc)
    {
        for (int i=0;i<storageTypeCodes.length;i++)
        {
            if (storageTypeCodes[i].equalsIgnoreCase(desc))
            {
                return i; 
            }
        }

        return -1;
    }
    
    public static final String getStorageTypeCode(int storageType)
    {
    	if (storageType>=STORAGE_TYPE_NORMAL && storageType<=STORAGE_TYPE_INDEXED)
    	{
    		return storageTypeCodes[storageType];
    	}
    	return null;
    }
    
    /**
     * Determine if an object is null.
     * This is the case if data==null or if it's an empty string.
     * @param data the object to test
     * @return true if the object is considered null.
     * @throws KettleValueException in case there is a conversion error (only thrown in case of lazy conversion)
     */
    public boolean isNull(Object data) throws KettleValueException
    {
		try{
	        Object value = data;
	        	        
	        if (isStorageBinaryString()) {
	        	if (value==null || !EMPTY_STRING_AND_NULL_ARE_DIFFERENT && ((byte[])value).length==0) return true; // shortcut
	        	value = convertBinaryStringToNativeType((byte[])data);
	        }

	        // Re-check for null, even for lazy conversion.
	        // A value (5 spaces for example) can be null after trim and conversion
	        //
	        if (value==null) return true;
	        
	        if (EMPTY_STRING_AND_NULL_ARE_DIFFERENT) {
	        	return false;
	        }

	        // If it's a string and the string is empty, it's a null value as well
	        //
	        if (isString()) {
	        	if (value.toString().length()==0) return true;
	        }
	        
	        // We tried everything else so we assume this value is not null.
	        //
	        return false;
		}
		catch(ClassCastException e)
		{
			throw new RuntimeException("Unable to verify if ["+toString()+"] is null or not because of an error:"+e.toString(), e);
		}
    }
    
    /*
     * Compare 2 binary strings, one byte at a time.<br>
     * This algorithm is very fast but most likely wrong as well.<br>
     * 
     * @param one The first binary string to compare with
     * @param two the second binary string to compare to
     * @return -1 if <i>one</i> is smaller than <i>two</i>, 0 is both byte arrays are identical and 1 if <i>one</i> is larger than <i>two</i>
    private int compareBinaryStrings(byte[] one, byte[] two) {
    	
    	for (int i=0;i<one.length;i++)
    	{
    		if (i>=two.length) return 1; // larger
    		if (one[i]>two[i]) return 1; // larger
    		if (one[i]<two[i]) return -1; // smaller
    	}
    	if (one.length>two.length) return 1; // larger
    	if (one.length>two.length) return -11; // smaller
    	return 0;
    }
     */
    
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
            	// if (isStorageBinaryString() && identicalFormat && storageMetadata.isSingleByteEncoding()) return compareBinaryStrings((byte[])data1, (byte[])data2); TODO
            	String one = getString(data1);
                String two = getString(data2);
    
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
            	// if (isStorageBinaryString() && identicalFormat) return compareBinaryStrings((byte[])data1, (byte[])data2); TODO
            	long compare = getInteger(data1).longValue() - getInteger(data2).longValue();
                if (compare<0) cmp=-1;
                else if (compare>0) cmp=1;
                else cmp=0;
            }
            break;

        case TYPE_NUMBER:
            {
                cmp=Double.compare(getNumber(data1).doubleValue(), getNumber(data2).doubleValue());
            }
            break;

        case TYPE_DATE:
            {
            	long compare =  getDate(data1).getTime() - getDate(data2).getTime();
                if (compare<0) cmp=-1;
                else if (compare>0) cmp=1;
                else cmp=0;
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
                        cmp = cmp < 0 ? -1 : 1;
                        break;
                    }
                }
                
                cmp = b1.length - b2.length; 
            }
            break;
        default: 
            throw new KettleValueException(toString()+" : Comparing values can not be done with data type : "+getType());
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
		if (meta2==null) {
			throw new KettleValueException(toStringMeta()+" : Second meta data (meta2) is null, please check one of the previous steps.");
		}

    	try
    	{
	        // Before we can compare data1 to data2 we need to make sure they have the same data type etc.
	    	//
	        if (getType()==meta2.getType()) 
	        {
	        	if (getStorageType()==meta2.getStorageType()) return compare(data1, data2);
	        	
	        	// Convert the storage type to compare the data.
	        	//
	        	switch(getStorageType())
	        	{
	        	case STORAGE_TYPE_NORMAL        :
	        		return compare(data1, meta2.convertToNormalStorageType(data2));
	
	        	case STORAGE_TYPE_BINARY_STRING : 
	        		return compare(data1, meta2.convertToBinaryStringStorageType(data2));
	        		
	        	case STORAGE_TYPE_INDEXED       : 
	        		switch(meta2.getStorageType())
	        		{
	        		case STORAGE_TYPE_INDEXED: 
	        			return compare(data1, data2); // not accessible, just to make sure.
	        		case STORAGE_TYPE_NORMAL: 
	        			return -meta2.compare(data2, convertToNormalStorageType(data1));
	        		case STORAGE_TYPE_BINARY_STRING: 
	        			return -meta2.compare(data2, convertToBinaryStringStorageType(data1));
	            	default: 
	            		throw new KettleValueException(meta2.toStringMeta()+" : Unknown storage type : "+meta2.getStorageType());
	        		
	        		}
	        	default: throw new KettleValueException(toStringMeta()+" : Unknown storage type : "+getStorageType());
	        	}
	        }
	        
	        // If the data types are not the same, the first one is the driver...
	        // The second data type is converted to the first one.
	        //
	        return compare(data1, convertData(meta2, data2));
    	}
    	catch(Exception e)
    	{
    		throw new KettleValueException(toStringMeta()+" : Unable to compare with value ["+meta2.toStringMeta()+"]", e);
    	}
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
            throw new KettleValueException(toString()+" : I can't convert the specified value to data type : "+getType());
        }
    }
    
    /**
     * Convert the specified data to the data type specified in this object.
     * For String conversion, be compatible with version 2.5.2.
     * 
     * @param meta2 the metadata of the object to be converted
     * @param data2 the data of the object to be converted
     * @return the object in the data type of this value metadata object
     * @throws KettleValueException in case there is a data conversion error
     */
    public Object convertDataCompatible(ValueMetaInterface meta2, Object data2) throws KettleValueException
    {
        switch(getType())
        {
        case TYPE_STRING    : return meta2.getCompatibleString(data2);
        case TYPE_NUMBER    : return meta2.getNumber(data2);
        case TYPE_INTEGER   : return meta2.getInteger(data2);
        case TYPE_DATE      : return meta2.getDate(data2);
        case TYPE_BIGNUMBER : return meta2.getBigNumber(data2);
        case TYPE_BOOLEAN   : return meta2.getBoolean(data2);
        case TYPE_BINARY    : return meta2.getBinary(data2);
        default: 
            throw new KettleValueException(toString()+" : I can't convert the specified value to data type : "+getType());
        }
    }

    /**
     * Convert an object to the data type specified in the conversion metadata
     * @param data The data
     * @return The data converted to the storage data type
     * @throws KettleValueException in case there is a conversion error.
     */
    public Object convertDataUsingConversionMetaData(Object data2) throws KettleValueException {
    	if (conversionMetadata==null) {
    		throw new KettleValueException("API coding error: please specify the conversion metadata before attempting to convert value "+name);
    	}
    	
    	// Suppose we have an Integer 123, length 5
    	// The string variation of this is " 00123"
    	// To convert this back to an Integer we use the storage metadata
    	// Specifically, in method convertStringToInteger() we consult the storageMetaData to get the correct conversion mask
    	// That way we're always sure that a conversion works both ways.
    	// 
    	
    	switch(conversionMetadata.getType()) {
        case TYPE_STRING    : return getString(data2);
        case TYPE_INTEGER   : return getInteger(data2); 
        case TYPE_NUMBER    : return getNumber(data2);
        case TYPE_DATE      : return getDate(data2);
        case TYPE_BIGNUMBER : return getBigNumber(data2);
        case TYPE_BOOLEAN   : return getBoolean(data2);
        case TYPE_BINARY    : return getBinary(data2);
        default: 
            throw new KettleValueException(toString()+" : I can't convert the specified value to data type : "+storageMetadata.getType());
        }
    }

    /**
     * Convert the specified string to the data type specified in this object.
     * @param pol the string to be converted
     * @param convertMeta the metadata of the object (only string type) to be converted
     * @param nullIf set the object to null if pos equals nullif (IgnoreCase)
     * @param ifNull set the object to ifNull when pol is empty or null
     * @param trim_type the trim type to be used (ValueMetaInterface.TRIM_TYPE_XXX)
     * @return the object in the data type of this value metadata object
     * @throws KettleValueException in case there is a data conversion error
     */
    public Object convertDataFromString(String pol, ValueMetaInterface convertMeta, String nullIf, String ifNull, int trim_type) throws KettleValueException
    {
        // null handling and conversion of value to null
        //
		String null_value = nullIf;
		if (null_value == null)
		{
			switch (convertMeta.getType())
			{
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
			// Note that you can't pull the pad method up here as a nullComp variable because you could get an NPE since you haven't checked isEmpty(pol) yet!
			if (Const.isEmpty(pol) || pol.equalsIgnoreCase(Const.rightPad(new StringBuffer(null_value), pol.length())))
			{
				pol = ifNull;
			}
        }

		// See if the polled value is empty
        // In that case, we have a null value on our hands...
        //
		if (Const.isEmpty(pol))
		{
            return null;
        }
		else
		{
			// if the null_value is specified, we try to match with that.
			//
			if (!Const.isEmpty(null_value))
			{
				if (null_value.length()<=pol.length())
				{
					// If the polled value is equal to the spaces right-padded null_value, we have a match
					//
					if (pol.equalsIgnoreCase(Const.rightPad(new StringBuffer(null_value), pol.length())))
					{
						return null;
					}
				}
			}
			else
			{
				// Verify if there are only spaces in the polled value...
				// We consider that empty as well...
				//
				if (Const.onlySpaces(pol))
				{
					return null;
				}
			}
		}
 
        
        // Trimming
        switch (trim_type)
        {
        case ValueMetaInterface.TRIM_TYPE_LEFT:
            {
                StringBuffer strpol = new StringBuffer(pol);
                while (strpol.length() > 0 && strpol.charAt(0) == ' ')
                    strpol.deleteCharAt(0);
                pol=strpol.toString();
            }
            break;
        case ValueMetaInterface.TRIM_TYPE_RIGHT:
            {
                StringBuffer strpol = new StringBuffer(pol);
                while (strpol.length() > 0 && strpol.charAt(strpol.length() - 1) == ' ')
                    strpol.deleteCharAt(strpol.length() - 1);
                pol=strpol.toString();
            }
            break;
        case ValueMetaInterface.TRIM_TYPE_BOTH:
            StringBuffer strpol = new StringBuffer(pol);
            {
                while (strpol.length() > 0 && strpol.charAt(0) == ' ')
                    strpol.deleteCharAt(0);
                while (strpol.length() > 0 && strpol.charAt(strpol.length() - 1) == ' ')
                    strpol.deleteCharAt(strpol.length() - 1);
                pol=strpol.toString();
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
           default: throw new KettleValueException(toString()+" : We can't convert data type "+getTypeDesc()+" to an original (V2) Value");
           }
       }
       return value;
    }
    
    
    /**
     * Extracts the primitive data from an old style Value object 
     * @param value the old style Value object 
     * @return the value's data, NOT the meta data.
     * @throws KettleValueException  case there is a data conversion problem
     */
    public Object getValueData(Value value) throws KettleValueException
    {
       if (value==null || value.isNull()) return null;
       
       // So far the old types and the new types map to the same thing.
       // For compatibility we just ask the old-style value to convert to the new one.
       // In the old transformation this would happen sooner or later anyway.
       // It doesn't throw exceptions or complain either (unfortunately).
       //
       
       switch(getType())
       {
       case ValueMetaInterface.TYPE_STRING       : return value.getString();
       case ValueMetaInterface.TYPE_NUMBER       : return value.getNumber();
       case ValueMetaInterface.TYPE_INTEGER      : return value.getInteger();
       case ValueMetaInterface.TYPE_DATE         : return value.getDate();
       case ValueMetaInterface.TYPE_BOOLEAN      : return value.getBoolean();
       case ValueMetaInterface.TYPE_BIGNUMBER    : return value.getBigNumber();
       case ValueMetaInterface.TYPE_BINARY       : return value.getBytes();
       default: throw new KettleValueException(toString()+" : We can't convert original data type "+value.getTypeDesc()+" to a primitive data type");
       }
    }

	/**
	 * @return the storageMetadata
	 */
	public ValueMetaInterface getStorageMetadata() {
		return storageMetadata;
	}

	/**
	 * @param storageMetadata the storageMetadata to set
	 */
	public void setStorageMetadata(ValueMetaInterface storageMetadata) {
		this.storageMetadata = storageMetadata;
		compareStorageAndActualFormat();
	}

	private void compareStorageAndActualFormat() {
		
		if (storageMetadata==null) {
			identicalFormat = true;
		} 
		else {
			
			// If a trim type is set, we need to at least try to trim the strings.
			// In that case, we have to set the identical format off.
			//
			if (trimType!=TRIM_TYPE_NONE) {
				identicalFormat = false;
			}
			else {
			
				// If there is a string encoding set and it's the same encoding in the binary string, then we don't have to convert
				// If there are no encodings set, then we're certain we don't have to convert as well.
				//
				if (getStringEncoding()!=null && getStringEncoding().equals(storageMetadata.getStringEncoding()) || 
					getStringEncoding()==null && storageMetadata.getStringEncoding()==null) {
					
					// However, perhaps the conversion mask changed since we read the binary string?
					// The output can be different from the input.  If the mask is different, we need to do conversions.
					// Otherwise, we can just ignore it...
					//
					if (isDate()) {
						if ( (getConversionMask()!=null && getConversionMask().equals(storageMetadata.getConversionMask())) ||
							(getConversionMask()==null && storageMetadata.getConversionMask()==null) ) {
							identicalFormat = true;
						}
						else {
							identicalFormat = false;
						}
					}
					else if (isNumeric()) {
						// Check the lengths first
						// 
						if (getLength()!=storageMetadata.getLength()) identicalFormat=false;
						else if (getPrecision()!=storageMetadata.getPrecision()) identicalFormat=false;
						else
						// For the same reasons as above, if the conversion mask, the decimal or the grouping symbol changes
						// we need to convert from the binary strings to the target data type and then back to a string in the required format.
						//
						if ( (getConversionMask()!=null && getConversionMask().equals(storageMetadata.getConversionMask()) ||
								(getConversionMask()==null && storageMetadata.getConversionMask()==null))
						   ) {
							if ( (getGroupingSymbol()!=null && getGroupingSymbol().equals(storageMetadata.getGroupingSymbol())) || 
									(getConversionMask()==null && storageMetadata.getConversionMask()==null) ) {
								if ( (getDecimalFormat(false)!=null && getDecimalFormat(false).equals(storageMetadata.getDecimalFormat(false))) || 
										(getDecimalFormat(false)==null && storageMetadata.getDecimalFormat(false)==null) ) {
									identicalFormat = true;
								}
								else {
									identicalFormat = false;
								}
							} 
							else {
								identicalFormat = false;
							}
						}
						else {
							identicalFormat = false;
						}
					}
				}
			}
		}
	}

	/**
	 * @return the trimType
	 */
	public int getTrimType() {
		return trimType;
	}

	/**
	 * @param trimType the trimType to set
	 */
	public void setTrimType(int trimType) {
		this.trimType = trimType;
	}
	
	public final static int getTrimTypeByCode(String tt)
	{
		if (tt == null) return 0;

		for (int i = 0; i < trimTypeCode.length; i++)
		{
			if (trimTypeCode[i].equalsIgnoreCase(tt)) return i;
		}
		return 0;
	}

	public final static int getTrimTypeByDesc(String tt)
	{
		if (tt == null) return 0;

		for (int i = 0; i < trimTypeDesc.length; i++)
		{
			if (trimTypeDesc[i].equalsIgnoreCase(tt)) return i;
		}

        // If this fails, try to match using the code.
        return getTrimTypeByCode(tt);
	}

	public final static String getTrimTypeCode(int i)
	{
		if (i < 0 || i >= trimTypeCode.length) return trimTypeCode[0];
		return trimTypeCode[i];
	}

	public final static String getTrimTypeDesc(int i)
	{
		if (i < 0 || i >= trimTypeDesc.length) return trimTypeDesc[0];
		return trimTypeDesc[i];
	}

	/**
	 * @return the conversionMetadata
	 */
	public ValueMetaInterface getConversionMetadata() 
	{
		return conversionMetadata;
	}

	/**
	 * @param conversionMetadata the conversionMetadata to set
	 */
	public void setConversionMetadata(ValueMetaInterface conversionMetadata) 
	{
		this.conversionMetadata = conversionMetadata;
	}

	/**
	 * @return true if the String encoding used (storage) is single byte encoded.
	 */
	public boolean isSingleByteEncoding() 
	{
		return singleByteEncoding;
	}

	/**
	 * @return the number of binary string to native data type conversions done with this object conversions
	 */
	public long getNumberOfBinaryStringConversions() {
		return numberOfBinaryStringConversions;
	}

	/**
	 * @param numberOfBinaryStringConversions the number of binary string to native data type done with this object conversions to set
	 */
	public void setNumberOfBinaryStringConversions(long numberOfBinaryStringConversions) {
		this.numberOfBinaryStringConversions = numberOfBinaryStringConversions;
	}

	/* Original JDBC RecordSetMetaData
	 * @see java.sql.ResultSetMetaData#isAutoIncrement()
	 */
	public boolean isOriginalAutoIncrement() {
		return originalAutoIncrement;
	}

	/* Original JDBC RecordSetMetaData
	 * @see java.sql.ResultSetMetaData#setAutoIncrement(boolean)
	 */
	public void setOriginalAutoIncrement(boolean originalAutoIncrement) {
		this.originalAutoIncrement=originalAutoIncrement;
	}

	/* Original JDBC RecordSetMetaData
	 * @see java.sql.ResultSetMetaData#getColumnType()
	 */
	public int getOriginalColumnType() {
		return originalColumnType;
	}

	/* Original JDBC RecordSetMetaData
	 * @see java.sql.ResultSetMetaData#setColumnType(int)
	 */
	public void setOriginalColumnType(int originalColumnType) {
		this.originalColumnType=originalColumnType;
	}

	/* Original JDBC RecordSetMetaData
	 * @see java.sql.ResultSetMetaData#getColumnTypeName()
	 */
	public String getOriginalColumnTypeName() {
		return originalColumnTypeName;
	}

	/* Original JDBC RecordSetMetaData
	 * @see java.sql.ResultSetMetaData#setColumnTypeName(java.lang.String)
	 */
	public void setOriginalColumnTypeName(String originalColumnTypeName) {
		this.originalColumnTypeName=originalColumnTypeName;
		
	}

	/* Original JDBC RecordSetMetaData
	 * @see java.sql.ResultSetMetaData#isNullable()
	 */
	public int isOriginalNullable() {
		return originalNullable;
	}

	/* Original JDBC RecordSetMetaData
	 * @see java.sql.ResultSetMetaData#setNullable(int)
	 */
	public void setOriginalNullable(int originalNullable) {
		this.originalNullable=originalNullable;
		
	}

	/* Original JDBC RecordSetMetaData
	 * @see java.sql.ResultSetMetaData#getPrecision()
	 */
	public int getOriginalPrecision() {
		return originalPrecision;
	}

	/* Original JDBC RecordSetMetaData
	 * @see java.sql.ResultSetMetaData#setPrecision(int)
	 */
	public void setOriginalPrecision(int originalPrecision) {
		this.originalPrecision=originalPrecision;
	}

	/* Original JDBC RecordSetMetaData
	 * @see java.sql.ResultSetMetaData#getScale()
	 */
	public int getOriginalScale() {
		return originalScale;
	}

	/* Original JDBC RecordSetMetaData
	 * @see java.sql.ResultSetMetaData#setScale(int)
	 */
	public void setOriginalScale(int originalScale) {
		this.originalScale=originalScale;
		
	}
	
	/* Original JDBC RecordSetMetaData
	 * @see java.sql.ResultSetMetaData#isSigned()
	 */
	public boolean isOriginalSigned() {
		return originalSigned;
	}

	/* Original JDBC RecordSetMetaData
	 * @see java.sql.ResultSetMetaData#setOriginalSigned(boolean)
	 */
	public void setOriginalSigned(boolean originalSigned) {
		this.originalSigned=originalSigned;
	}

	/**
	 * @return the bigNumberFormatting flag : true if BigNumbers of formatted as well
	 */
	public boolean isBigNumberFormatting() {
		return bigNumberFormatting;
	}

	/**
	 * @param bigNumberFormatting the bigNumberFormatting flag to set : true if BigNumbers of formatted as well
	 */
	public void setBigNumberFormatting(boolean bigNumberFormatting) {
		this.bigNumberFormatting = bigNumberFormatting;
	}
	
	/**
	 * @return The available trim type codes (NOT localized, use for persistence)
	 */
	public static String[] getTrimTypeCodes() {
		return trimTypeCode;
	}

	/**
	 * @return The available trim type descriptions (localized)
	 */
	public static String[] getTrimTypeDescriptions() {
		return trimTypeDesc;
	}
	
	public boolean requiresRealClone() {
		return type==TYPE_BINARY || type==TYPE_SERIALIZABLE;
	}
}