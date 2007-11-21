 /* Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.*/

 

package org.pentaho.di.compatibility;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.pentaho.di.core.Const;

/**
 * This class contains a Value of type String and the length by which it is described.
 * 
 * @author Matt
 * @since 15-10-2004
 *
 */
public class ValueString implements ValueInterface, Cloneable
{
	private String string;
	private int length;
	
	private static final ThreadLocal<SimpleDateFormat>  LOCAL_SIMPLE_DATE_PARSER = new ThreadLocal<SimpleDateFormat>() {
		protected SimpleDateFormat initialValue() {
			return new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS");
     	}
    };

	public ValueString()
	{
		string = null;
		length = -1;
	}
	public ValueString(String string)
	{
		this.string = string;
		length = -1;
	}

	public int getType()
	{
		return Value.VALUE_TYPE_STRING;
	}

	public String getTypeDesc()
	{
		return "String";
	}

	public String getString()
	{
		return this.string;
	}

	public double getNumber()
	{
		return Const.toDouble( string, 0.0);
	}

	public Date getDate()
	{
	    if (string!=null)
	    {
			try
			{
				return ((SimpleDateFormat)LOCAL_SIMPLE_DATE_PARSER.get()).parse(string);
			}
			catch(ParseException e)
			{
			}
	    }
	    return null;
	}

	public boolean getBoolean()
	{
		return "Y".equalsIgnoreCase(string) || "TRUE".equalsIgnoreCase(string) || "YES".equalsIgnoreCase(string) || "1".equalsIgnoreCase(string);
	}

	public long getInteger()
	{
		return Const.toLong(Const.ltrim(string), 0L); // Remove the leading space to make "int to string to int" conversion possible.
	}
	
	public void    setString(String string)
	{
		this.string = string;
	}
	
	public void    setNumber(double number)
	{
		this.string = ""+number;
	}
	
	public void    setDate(Date date)
	{
		this.string = ((SimpleDateFormat)LOCAL_SIMPLE_DATE_PARSER.get()).format(date);
	}
	
	public void    setBoolean(boolean bool)
	{
		this.string = bool?"Y":"N";
	}
	
	public void    setInteger(long number)
	{
		this.string = ""+number;
	}
	
	public int getLength()
	{
		return length;
	}
	
	public int getPrecision()
	{
		return -1;
	}
	
	public void setLength(int length, int precision)
	{
		this.length = length;
	}
	
	public void setLength(int length)
	{
		this.length = length;
	}
	
	public void setPrecision(int precision)
	{
	}

	public Object clone()
	{
		try
		{
			ValueString retval   = (ValueString)super.clone();
			return retval;
		}
		catch(CloneNotSupportedException e)
		{
			return null;
		}
	}
    
    public BigDecimal getBigNumber()
    {
        if (Const.isEmpty(string)) return null;
        
        // Localise , to .
        if (Const.DEFAULT_DECIMAL_SEPARATOR!='.')
        {
            string = string.replace(Const.DEFAULT_DECIMAL_SEPARATOR, '.');
        }
        
        return new BigDecimal(string);
    }
    
    public void setBigNumber(BigDecimal number)
    {
        string = number.toString();
    }
    
    public Serializable getSerializable() {
        return string;
    }

    public void setSerializable(Serializable ser) {
        ser.toString();
    }

	public byte[] getBytes() {
        if (string==null) return null;
        
        char arr[] = string.toCharArray();
        byte retByte[] = new byte[arr.length];
        
        for (int i = 0; i < arr.length; i++)
        {
        	// only take low byte of char.
        	retByte[i] = (byte)(arr[i] & 0xFF);
        }
        return retByte;
	}

	public void setBytes(byte[] b) {
		try
		{
            string = new String(b, "US-ASCII");
		}
		catch (UnsupportedEncodingException e)
		{
			// we should not get here, ASCII is a mandatory encoding
		    string = null;
		}
	}    
}