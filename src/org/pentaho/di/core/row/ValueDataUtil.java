/*
 * Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
*/
package org.pentaho.di.core.row;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Locale;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleValueException;

public class ValueDataUtil
{
    public static final String leftTrim(String string)
    {
        int min = 0;
        int max = string.length() - 1;
        
        while (min <= max && isSpace(string.charAt(min)))
            min++;

        return string.substring(min);
    }
    
    public static final String rightTrim(String string)
    {
        int max = string.length() - 1;
        
        while (max >= 0  && isSpace(string.charAt(max)))
            max--;

        return string.substring(0, max + 1);
    }

    /**
     * Determines whether or not a character is considered a space.
     * A character is considered a space in Kettle if it is a space, a tab, a newline or a cariage return.
     * @param c The character to verify if it is a space.
     * @return true if the character is a space. false otherwise. 
     */
    public static final boolean isSpace(char c)
    {
        return c == ' ' || c == '\t' || c == '\r' || c == '\n';
    }
    
    /**
     * Trims a string: removes the leading and trailing spaces of a String.
     * @param string The string to trim
     * @return The trimmed string.
     */
    public static final String trim(String string)
    {
        int max = string.length() - 1;
        int min = 0;    

        while (min <= max && isSpace(string.charAt(min)))
            min++;
        while (max >= 0 && isSpace(string.charAt(max)))
            max--;

        if (max < min)
            return "";

        return string.substring(min, max + 1);
    }
    
    
    public static Object plus(ValueMetaInterface metaA, Object dataA, ValueMetaInterface metaB, Object dataB) throws KettleValueException
    {
        if (dataA==null || dataB==null) return null;
        
        switch(metaA.getType())
        {
        case ValueMetaInterface.TYPE_STRING    : 
            return metaA.getString(dataA)+metaB.getString(dataB);
        case ValueMetaInterface.TYPE_NUMBER    : 
            return new Double( metaA.getNumber(dataA).doubleValue()+metaB.getNumber(dataB).doubleValue());
        case ValueMetaInterface.TYPE_INTEGER   : 
            return new Long( metaA.getInteger(dataA).longValue()+metaB.getInteger(dataB).longValue());
        case ValueMetaInterface.TYPE_BOOLEAN   : 
            return Boolean.valueOf( metaA.getBoolean(dataA).booleanValue() || metaB.getBoolean(dataB).booleanValue());
        case ValueMetaInterface.TYPE_BIGNUMBER : 
            return metaA.getBigNumber(dataA).add( metaB.getBigNumber(dataB));
            
        default: throw new KettleValueException("The 'plus' function only works on numeric data and Strings." );
        }
    }
    
    public static Object minus(ValueMetaInterface metaA, Object dataA, ValueMetaInterface metaB, Object dataB) throws KettleValueException
    {
        if (dataA==null || dataB==null) return null;
        
        switch(metaA.getType())
        {
        case ValueMetaInterface.TYPE_NUMBER    : 
            return new Double( metaA.getNumber(dataA).doubleValue()-metaB.getNumber(dataB).doubleValue());
        case ValueMetaInterface.TYPE_INTEGER   : 
            return new Long( metaA.getInteger(dataA).longValue()-metaB.getInteger(dataB).longValue());
        case ValueMetaInterface.TYPE_BIGNUMBER : 
            return metaA.getBigNumber(dataA).subtract( metaB.getBigNumber(dataB));
            
        default: throw new KettleValueException("The 'minus' function only works on numeric data." );
        }
    }

    public static Object multiply(ValueMetaInterface metaA, Object dataA, ValueMetaInterface metaB, Object dataB) throws KettleValueException
    {
        if (dataA==null || dataB==null) return null;

        if ((metaB.isString() && metaA.isNumeric()) || (metaB.isNumeric() && metaA.isString()))
        {
            StringBuffer s;
            String append="";
            int n;
            if (metaB.isString())
            {
                s=new StringBuffer(metaB.getString(dataB));
                append=metaB.getString(dataB);
                n=metaA.getInteger(dataA).intValue();
            }
            else
            {
                s=new StringBuffer(metaA.getString(dataA));
                append=metaA.getString(dataA);
                n=metaB.getInteger(dataB).intValue();
            }

            if (n==0) s.setLength(0);
            else
            for (int i=1;i<n;i++) s.append(append);

            return s.toString();
        }
        
        switch(metaA.getType())
        {
        case ValueMetaInterface.TYPE_NUMBER    : 
            return new Double( metaA.getNumber(dataA).doubleValue()*metaB.getNumber(dataB).doubleValue());
        case ValueMetaInterface.TYPE_INTEGER   : 
            return new Long( metaA.getInteger(dataA).longValue()*metaB.getInteger(dataB).longValue());
        case ValueMetaInterface.TYPE_BIGNUMBER : 
            return metaA.getBigNumber(dataA).multiply( metaB.getBigNumber(dataB));
            
        default: throw new KettleValueException("The 'multiply' function only works on numeric data optionally multiplying strings." );
        }
    }

    public static Object divide(ValueMetaInterface metaA, Object dataA, ValueMetaInterface metaB, Object dataB) throws KettleValueException
    {
        if (dataA==null || dataB==null) return null;

        switch(metaA.getType())
        {
        case ValueMetaInterface.TYPE_NUMBER    : 
            return new Double( metaA.getNumber(dataA).doubleValue() / metaB.getNumber(dataB).doubleValue());
        case ValueMetaInterface.TYPE_INTEGER   : 
            return new Long( metaA.getInteger(dataA).longValue() / metaB.getInteger(dataB).longValue());
        case ValueMetaInterface.TYPE_BIGNUMBER : 
            return metaA.getBigNumber(dataA).divide( metaB.getBigNumber(dataB), BigDecimal.ROUND_HALF_UP);
            
        default: throw new KettleValueException("The 'multiply' function only works on numeric data optionally multiplying strings." );
        }
    }
    
    public static Object sqrt(ValueMetaInterface metaA, Object dataA) throws KettleValueException
    {
        if (dataA==null) return null;

        switch(metaA.getType())
        {
        case ValueMetaInterface.TYPE_NUMBER    : 
            return new Double( Math.sqrt( metaA.getNumber(dataA).doubleValue()) );
        case ValueMetaInterface.TYPE_INTEGER   : 
            return new Long( Math.round( Math.sqrt( metaA.getNumber(dataA).doubleValue()) ) );
        case ValueMetaInterface.TYPE_BIGNUMBER : 
            return new BigDecimal( Math.sqrt( metaA.getNumber(dataA).doubleValue()) );
            
        default: throw new KettleValueException("The 'multiply' function only works on numeric data optionally multiplying strings." );
        }
    }
    
    /**
     * 100 * A / B
     *  
     * @param metaA
     * @param dataA
     * @param metaB
     * @param dataB
     * @return
     * @throws KettleValueException
     */
    public static Object percent1(ValueMetaInterface metaA, Object dataA, ValueMetaInterface metaB, Object dataB) throws KettleValueException
    {
        if (dataA==null || dataB==null) return null;

        switch(metaA.getType())
        {
        case ValueMetaInterface.TYPE_NUMBER    : 
            return new Double( 100.0 * metaA.getNumber(dataA).doubleValue() / metaB.getNumber(dataB).doubleValue());
        case ValueMetaInterface.TYPE_INTEGER   : 
            return new Long( 100 * metaA.getInteger(dataA).longValue() / metaB.getInteger(dataB).longValue());
        case ValueMetaInterface.TYPE_BIGNUMBER : 
            return metaA.getBigNumber(dataA).multiply( new BigDecimal(100) ).divide( metaB.getBigNumber(dataB), BigDecimal.ROUND_HALF_UP );
            
        default: throw new KettleValueException("The 'percent1' function only works on numeric data" );
        }
    }
    
    /**
     * A - ( A * B / 100 )
     *  
     * @param metaA
     * @param dataA
     * @param metaB
     * @param dataB
     * @return
     * @throws KettleValueException
     */
    public static Object percent2(ValueMetaInterface metaA, Object dataA, ValueMetaInterface metaB, Object dataB) throws KettleValueException
    {
        if (dataA==null || dataB==null) return null;

        switch(metaA.getType())
        {
        case ValueMetaInterface.TYPE_NUMBER    : 
            return new Double( metaA.getNumber(dataA).doubleValue()  - ( 100.0 * metaA.getNumber(dataA).doubleValue() / metaB.getNumber(dataB).doubleValue() ));
        case ValueMetaInterface.TYPE_INTEGER   : 
            return new Long( metaA.getInteger(dataA).longValue() - ( 100 * metaA.getInteger(dataA).longValue() / metaB.getInteger(dataB).longValue() ));
        case ValueMetaInterface.TYPE_BIGNUMBER : 
            BigDecimal percentTotal = metaA.getBigNumber(dataA).multiply( new BigDecimal(100) ).divide( metaB.getBigNumber(dataB), BigDecimal.ROUND_HALF_UP );
            return metaA.getBigNumber(dataA).subtract(percentTotal);
            
        default: throw new KettleValueException("The 'percent2' function only works on numeric data" );
        }
    }
    
    /**
     * A + ( A * B / 100 )
     *  
     * @param metaA
     * @param dataA
     * @param metaB
     * @param dataB
     * @return
     * @throws KettleValueException
     */
    public static Object percent3(ValueMetaInterface metaA, Object dataA, ValueMetaInterface metaB, Object dataB) throws KettleValueException
    {
        if (dataA==null || dataB==null) return null;

        switch(metaA.getType())
        {
        case ValueMetaInterface.TYPE_NUMBER    : 
            return new Double( metaA.getNumber(dataA).doubleValue()  + ( 100.0 * metaA.getNumber(dataA).doubleValue() / metaB.getNumber(dataB).doubleValue() ));
        case ValueMetaInterface.TYPE_INTEGER   : 
            return new Long( metaA.getInteger(dataA).longValue() + ( 100 * metaA.getInteger(dataA).longValue() / metaB.getInteger(dataB).longValue() ));
        case ValueMetaInterface.TYPE_BIGNUMBER : 
            BigDecimal percentTotal = metaA.getBigNumber(dataA).multiply( new BigDecimal(100) ).divide( metaB.getBigNumber(dataB), BigDecimal.ROUND_HALF_UP );
            return metaA.getBigNumber(dataA).add(percentTotal);
            
        default: throw new KettleValueException("The 'percent3' function only works on numeric data" );
        }
    }
    
    /**
     * A + B * C
     *  
     * @param metaA
     * @param dataA
     * @param metaB
     * @param dataB
     * @return
     * @throws KettleValueException
     */
    public static Object combination1(ValueMetaInterface metaA, Object dataA, ValueMetaInterface metaB, Object dataB, ValueMetaInterface metaC, Object dataC) throws KettleValueException
    {
        if (dataA==null || dataB==null || dataC==null) return null;

        switch(metaA.getType())
        {
        case ValueMetaInterface.TYPE_NUMBER    : 
            return new Double( metaA.getNumber(dataA).doubleValue()  + ( metaB.getNumber(dataB).doubleValue() * metaC.getNumber(dataC).doubleValue() ));
        case ValueMetaInterface.TYPE_INTEGER   : 
            return new Long( metaA.getInteger(dataA).longValue() + ( metaB.getInteger(dataB).longValue() * metaC.getInteger(dataC).longValue() ));
        case ValueMetaInterface.TYPE_BIGNUMBER : 
            BigDecimal product = metaB.getBigNumber(dataB).multiply( metaC.getBigNumber(dataC));
            return metaA.getBigNumber(dataA).add(product);
            
        default: throw new KettleValueException("The 'combination1' function only works on numeric data" );
        }
    }
    
    
    
     /**
     * SQRT( A*A + B*B )
     *  
     * @param metaA
     * @param dataA
     * @param metaB
     * @param dataB
     * @return
     * @throws KettleValueException
     */
    public static Object combination2(ValueMetaInterface metaA, Object dataA, ValueMetaInterface metaB, Object dataB) throws KettleValueException
    {
        if (dataA==null || dataB==null) return null;

        switch(metaA.getType())
        {
        case ValueMetaInterface.TYPE_NUMBER    : 
            return new Double( Math.sqrt( metaA.getNumber(dataA).doubleValue() * metaA.getNumber(dataA).doubleValue() + metaB.getNumber(dataB).doubleValue() * metaB.getNumber(dataB).doubleValue() ));
            
        case ValueMetaInterface.TYPE_INTEGER   : 
            return new Long( Math.round( Math.sqrt( metaA.getInteger(dataA).longValue() * metaA.getInteger(dataA).longValue() + metaB.getInteger(dataB).longValue() / metaB.getInteger(dataB).longValue() )));

        case ValueMetaInterface.TYPE_BIGNUMBER : 
            return new BigDecimal( Math.sqrt( metaA.getNumber(dataA).doubleValue() * metaA.getNumber(dataA).doubleValue() + metaB.getNumber(dataB).doubleValue() * metaB.getNumber(dataB).doubleValue() ));
            
        default: throw new KettleValueException("The 'combination2' function only works on numeric data" );
        }
    }
    
    public static Object round(ValueMetaInterface metaA, Object dataA) throws KettleValueException
    {
        if (dataA==null) return null;

        switch(metaA.getType())
        {
        case ValueMetaInterface.TYPE_NUMBER    : 
            return new Double( Math.round( metaA.getNumber(dataA).doubleValue()) );
        case ValueMetaInterface.TYPE_INTEGER   : 
            return metaA.getInteger(dataA);
        case ValueMetaInterface.TYPE_BIGNUMBER : 
            return new BigDecimal( Math.round( metaA.getNumber(dataA).doubleValue()) );
            
        default: throw new KettleValueException("The 'round' function only works on numeric data" );
        }
    }
    
    
    public static Object round(ValueMetaInterface metaA, Object dataA, ValueMetaInterface metaB, Object dataB) throws KettleValueException
    {
        if (dataA==null || dataB==null) return null;

        switch(metaA.getType())
        {
        case ValueMetaInterface.TYPE_NUMBER    : 
            return new Double( Const.round( metaA.getNumber(dataA).doubleValue(), metaB.getInteger(dataB).intValue()) );
        case ValueMetaInterface.TYPE_INTEGER   : 
            return metaA.getInteger(dataA);
        case ValueMetaInterface.TYPE_BIGNUMBER : 
            
            // Round it to the desired number of digits.
            BigDecimal number = metaA.getBigNumber(dataA);
            return number.setScale( metaB.getInteger(dataB).intValue(), BigDecimal.ROUND_HALF_EVEN); 
            
        default: throw new KettleValueException("The 'round' function only works on numeric data" );
        }
    }
    
    public static Object nvl(ValueMetaInterface metaA, Object dataA, ValueMetaInterface metaB, Object dataB) throws KettleValueException
    {
        switch(metaA.getType())
        {
        case ValueMetaInterface.TYPE_STRING: 
            if (dataA==null) return metaB.getString(dataB); else return metaA.getString(dataA);

        case ValueMetaInterface.TYPE_NUMBER    :
            if (dataA==null) return metaB.getNumber(dataB); else return metaA.getNumber(dataA);
            
        case ValueMetaInterface.TYPE_INTEGER   : 
            if (dataA==null) return metaB.getInteger(dataB); else return metaA.getInteger(dataA);

        case ValueMetaInterface.TYPE_BIGNUMBER : 
            if (dataA==null) return metaB.getBigNumber(dataB); else return metaA.getBigNumber(dataA);

        case ValueMetaInterface.TYPE_DATE: 
            if (dataA==null) return metaB.getDate(dataB); else return metaA.getDate(dataA);

        case ValueMetaInterface.TYPE_BOOLEAN: 
            if (dataA==null) return metaB.getBoolean(dataB); else return metaA.getBoolean(dataA);

        case ValueMetaInterface.TYPE_BINARY: 
            if (dataA==null) return metaB.getBinary(dataB); else return metaA.getBinary(dataA);

        default: throw new KettleValueException("The 'nvl' function doesn't know how to handle data type "+metaA.getType() );
        }
    }
    
    public static Object addDays(ValueMetaInterface metaA, Object dataA, ValueMetaInterface metaB, Object dataB) throws KettleValueException
    {
        if (metaA.isDate() && metaB.isInteger())
        {
            Calendar cal = Calendar.getInstance();
            cal.setTime(metaA.getDate(dataA));
            cal.add(Calendar.DAY_OF_YEAR, metaB.getInteger(dataB).intValue());
            
            return cal.getTime();
        }

        throw new KettleValueException("The 'addDays' function only works with a date and an integer");
    }
    
    public static Object yearOfDate(ValueMetaInterface metaA, Object dataA) throws KettleValueException
    {
        if (dataA==null) return null;

        if (metaA.isDate())
        {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime( metaA.getDate(dataA) );
            return new Long( calendar.get(Calendar.YEAR) );
        }
        
        throw new KettleValueException("The 'yearOfDate' function only works with dates");
    }
    
    public static Object monthOfDate(ValueMetaInterface metaA, Object dataA) throws KettleValueException
    {
        if (dataA==null) return null;

        if (metaA.isDate())
        {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime( metaA.getDate(dataA) );
            return new Long( calendar.get(Calendar.MONTH) + 1 );
        }
        
        throw new KettleValueException("The 'monthOfDate' function only works with dates");
    }
    
    public static Object dayOfYear(ValueMetaInterface metaA, Object dataA) throws KettleValueException
    {
        if (dataA==null) return null;

        if (metaA.isDate())
        {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime( metaA.getDate(dataA) );
            return new Long( calendar.get(Calendar.DAY_OF_YEAR) );
        }
        
        throw new KettleValueException("The 'dayOfYear' function only works with dates");
    }
    
    public static Object dayOfMonth(ValueMetaInterface metaA, Object dataA) throws KettleValueException
    {
        if (dataA==null) return null;

        if (metaA.isDate())
        {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime( metaA.getDate(dataA) );
            return new Long( calendar.get(Calendar.DAY_OF_MONTH) );
        }
        
        throw new KettleValueException("The 'dayOfMonth' function only works with dates");
    }
    
    public static Object dayOfWeek(ValueMetaInterface metaA, Object dataA) throws KettleValueException
    {
        if (dataA==null) return null;

        if (metaA.isDate())
        {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime( metaA.getDate(dataA) );
            return new Long( calendar.get(Calendar.DAY_OF_WEEK) );
        }
        
        throw new KettleValueException("The 'dayOfWeek' function only works with dates");
    }
    
    public static Object weekOfYear(ValueMetaInterface metaA, Object dataA) throws KettleValueException
    {
        if (dataA==null) return null;

        if (metaA.isDate())
        {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime( metaA.getDate(dataA) );
            return new Long( calendar.get(Calendar.WEEK_OF_YEAR) );
        }
        
        throw new KettleValueException("The 'weekOfYear' function only works with dates");
    }
    
    public static Object weekOfYearISO8601(ValueMetaInterface metaA, Object dataA) throws KettleValueException
    {
        if (dataA==null) return null;

        if (metaA.isDate())
        {
            Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
            calendar.setMinimalDaysInFirstWeek(4);
            calendar.setFirstDayOfWeek(Calendar.MONDAY);
            calendar.setTime( metaA.getDate(dataA) );
            return new Long( calendar.get(Calendar.WEEK_OF_YEAR) );
        }
        
        throw new KettleValueException("The 'weekOfYearISO8601' function only works with dates");
    }
    
    public static Object yearOfDateISO8601(ValueMetaInterface metaA, Object dataA) throws KettleValueException
    {
        if (dataA==null) return null;

        if (metaA.isDate())
        {
            Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
            calendar.setMinimalDaysInFirstWeek(4);
            calendar.setFirstDayOfWeek(Calendar.MONDAY);
            calendar.setTime( metaA.getDate(dataA) );
            
            int week  = calendar.get(Calendar.WEEK_OF_YEAR);
            int month = calendar.get(Calendar.MONTH);
            int year = calendar.get(Calendar.YEAR);
            
            // fix up for the year taking into account ISO8601 weeks
            if ( week >= 52 && month == 0  ) year--;
            if ( week <= 2  && month == 11 ) year++;
            
            return new Long( year );
        }
        
        throw new KettleValueException("The 'yearOfDateISO8601' function only works with dates");
    }
    
    /**
     * Change a hexadecimal string into normal ASCII representation. E.g. if Value
     * contains string "61" afterwards it would contain value "a". If the
     * hexadecimal string is of odd length a leading zero will be used.
     *
     * Note that only the low byte of a character will be processed, this
     * is for binary transformations.
     *
     * @return Value itself
     * @throws KettleValueException  
     */    
    public static String hexToByteDecode(ValueMetaInterface meta, Object data) throws KettleValueException 
    {
        if (meta.isNull(data)) 
        {           
            return null;
        }
        
        String hexString = meta.getString(data);
        
        int len = hexString.length();
        char chArray[] = new char[(len + 1) / 2];
        boolean evenByte = true;
        int nextByte = 0;
        
        // we assume a leading 0 if the length is not even.
        if ((len % 2) == 1)
            evenByte = false;
        
        int nibble;
        int i, j;
        for (i = 0, j = 0; i < len; i++)
        {
            char    c = hexString.charAt(i);
            
            if ((c >= '0') && (c <= '9'))
                nibble = c - '0';
            else if ((c >= 'A') && (c <= 'F'))
                nibble = c - 'A' + 0x0A;
            else if ((c >= 'a') && (c <= 'f'))
                nibble = c - 'a' + 0x0A;
            else
                throw new KettleValueException("invalid hex digit '" + c + "'.");
            
            if (evenByte)
            {
                nextByte = (nibble << 4);
            }
            else
            {
                nextByte += nibble;
                chArray[j] = (char)nextByte;
                j++;
            }
            
            evenByte = ! evenByte;
        }
        return new String(chArray);
    }
    
    /**
     * Change a string into its hexadecimal representation. E.g. if Value
     * contains string "a" afterwards it would contain value "0061".
     * 
     * Note that transformations happen in groups of 4 hex characters, so
     * the value of a characters is always in the range 0-65535.
     *  
     * @return 
     * @throws KettleValueException
     */
    public static String byteToHexEncode(ValueMetaInterface metaA, Object dataA) throws KettleValueException
    {
        if (dataA==null) return null;

        final char hexDigits[] =
        { '0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F' };
        
        String hex = metaA.getString(dataA);
        
        char[] s = hex.toCharArray();
        StringBuffer hexString = new StringBuffer(2 * s.length);
        
        for (int i = 0; i < s.length; i++)
        {
            hexString.append(hexDigits[(s[i] & 0x00F0) >> 4]); // hi nibble
            hexString.append(hexDigits[s[i] & 0x000F]);        // lo nibble
        }
        
        return hexString.toString();
    }
    
    /**
     * Change a string into its hexadecimal representation. E.g. if Value
     * contains string "a" afterwards it would contain value "0061".
     * 
     * Note that transformations happen in groups of 4 hex characters, so
     * the value of a characters is always in the range 0-65535.
     *  
     * @return A string with Hex code
     * @throws KettleValueException In case of a data conversion problem.
     */
    public static String charToHexEncode(ValueMetaInterface meta, Object data) throws KettleValueException
    {
        final char hexDigits[] = { '0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F' };

        if (meta.isNull(data)) 
        {
            return null;
        }
        
        String hex = meta.getString(data);
        
        char[] s = hex.toCharArray();
        StringBuffer hexString = new StringBuffer(2 * s.length);
        
        for (int i = 0; i < s.length; i++)
        {
            hexString.append(hexDigits[(s[i] & 0xF000) >> 12]); // hex 1
            hexString.append(hexDigits[(s[i] & 0x0F00) >> 8]);  // hex 2
            hexString.append(hexDigits[(s[i] & 0x00F0) >> 4]);  // hex 3
            hexString.append(hexDigits[s[i] & 0x000F]);         // hex 4
        }
        
        return hexString.toString();
    }

    /**
     * Change a hexadecimal string into normal ASCII representation. E.g. if Value
     * contains string "61" afterwards it would contain value "a". If the
     * hexadecimal string is of a wrong length leading zeroes will be used.
     *
     * Note that transformations happen in groups of 4 hex characters, so
     * the value of a characters is always in the range 0-65535.
     *
     * @return A hex-to-char decoded String 
     * @throws KettleValueException  
     */    
    public static String hexToCharDecode(ValueMetaInterface meta, Object data) throws KettleValueException 
    {
        if (meta.isNull(data)) 
        {           
            return null;
        }
        
        String hexString = meta.getString(data);
        
        int len = hexString.length();
        char chArray[] = new char[(len + 3) / 4];
        int charNr;
        int nextChar = 0;
        
        // we assume a leading 0s if the length is not right.
        charNr = (len % 4);
        if ( charNr == 0 ) charNr = 4;
        
        int nibble;
        int i, j;
        for (i = 0, j = 0; i < len; i++)
        {
            char    c = hexString.charAt(i);
            
            if ((c >= '0') && (c <= '9'))
                nibble = c - '0';
            else if ((c >= 'A') && (c <= 'F'))
                nibble = c - 'A' + 0x0A;
            else if ((c >= 'a') && (c <= 'f'))
                nibble = c - 'a' + 0x0A;
            else
                throw new KettleValueException("invalid hex digit '" + c + "'.");

            if (charNr == 4)
            {
                nextChar = (nibble << 12);
                charNr--;
            }           
            else if (charNr == 3)
            {
                nextChar += (nibble << 8);
                charNr--;
            }
            else if (charNr == 2)
            {
                nextChar += (nibble << 4);
                charNr--;
            }
            else // charNr == 1
            {
                nextChar += nibble;
                chArray[j] = (char)nextChar;
                charNr = 4;
                j++;
            }
        }
        
        return new String(chArray);
    }

    

    /**
     * Right pad a string: adds spaces to a string until a certain length.
     * If the length is smaller then the limit specified, the String is truncated.
     * @param ret The string to pad
     * @param limit The desired length of the padded string.
     * @return The padded String.
     */
    public static final String rightPad(String ret, int limit)
    {
        if (ret == null)
            return rightPad(new StringBuffer(), limit);
        else
            return rightPad(new StringBuffer(ret), limit);
    }

    /**
     * Right pad a StringBuffer: adds spaces to a string until a certain length.
     * If the length is smaller then the limit specified, the String is truncated.
     * @param ret The StringBuffer to pad
     * @param limit The desired length of the padded string.
     * @return The padded String.
     */
    public static final String rightPad(StringBuffer ret, int limit)
    {
        int len = ret.length();
        int l;

        if (len > limit)
        {
            ret.setLength(limit);
        } else
        {
            for (l = len; l < limit; l++)
                ret.append(' ');
        }
        return ret.toString();
    }

    /**
     * Replace value occurances in a String with another value.
     * @param string The original String.
     * @param repl The text to replace
     * @param with The new text bit
     * @return The resulting string with the text pieces replaced.
     */
    public static final String replace(String string, String repl, String with)
    {
        StringBuffer str = new StringBuffer(string);
        for (int i = str.length() - 1; i >= 0; i--)
        {
            if (str.substring(i).startsWith(repl))
            {
                str.delete(i, i + repl.length());
                str.insert(i, with);
            }
        }
        return str.toString();
    }

    /**
     * Alternate faster version of string replace using a stringbuffer as input.
     * 
     * @param str The string where we want to replace in
     * @param code The code to search for
     * @param repl The replacement string for code
     */
    public static void replaceBuffer(StringBuffer str, String code, String repl)
    {
        int clength = code.length();

        int i = str.length() - clength;

        while (i >= 0)
        {
            String look = str.substring(i, i + clength);
            if (look.equalsIgnoreCase(code)) // Look for a match!
            {
                str.replace(i, i + clength, repl);
            }
            i--;
        }
    }

    /**
     * Count the number of spaces to the left of a text. (leading)
     * @param field The text to examine
     * @return The number of leading spaces found.
     */
    public static final int nrSpacesBefore(String field)
    {
        int nr = 0;
        int len = field.length();
        while (nr < len && field.charAt(nr) == ' ')
        {
            nr++;
        }
        return nr;
    }

    /**
     * Count the number of spaces to the right of a text. (trailing)
     * @param field The text to examine
     * @return The number of trailing spaces found.
     */
    public static final int nrSpacesAfter(String field)
    {
        int nr = 0;
        int len = field.length();
        while (nr < len && field.charAt(field.length() - 1 - nr) == ' ')
        {
            nr++;
        }
        return nr;
    }

    /**
     * Checks whether or not a String consists only of spaces.
     * @param str The string to check
     * @return true if the string has nothing but spaces.
     */
    public static final boolean onlySpaces(String str)
    {
        for (int i = 0; i < str.length(); i++)
            if (!isSpace(str.charAt(i)))
                return false;
        return true;
    }
}