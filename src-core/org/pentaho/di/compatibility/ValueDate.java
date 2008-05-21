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
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.pentaho.di.core.Const;


/**
 * This class contains a Value of type Date.
 * 
 * @author Matt
 * @since 15-10-2004
 *
 */
public class ValueDate implements ValueInterface, Cloneable
{
	public static final String DATE_FORMAT = "yyyy/MM/dd HH:mm:ss.SSS";
	private Date date;
    public int precision;

	public ValueDate()
	{
		this.date     = null;
        this.precision = -1;
	}
	
	public ValueDate(Date date)
	{
		this.date      = date;
        this.precision = -1;
	}

	public int getType()
	{
		return Value.VALUE_TYPE_DATE;
	}

	public String getTypeDesc()
	{
		return "Date";
	}

	public String getString()
	{
		if (date==null) return null;
		SimpleDateFormat df = new SimpleDateFormat(DATE_FORMAT);
		return df.format(date);
	}

	public double getNumber()
	{
		if (date==null) return 0.0;
		return date.getTime();
	}

	public Date getDate()
	{
		return date;
	}

	public boolean getBoolean()
	{
		return false;
	}

	public long getInteger()
	{
		if (date==null) return 0L;
		return date.getTime();
	}
	
	public void    setString(String string)
	{
		this.date = Const.toDate(string, null);
	}
	
    public void setSerializable(Serializable ser) {
        
    }
    
	public void    setNumber(double number)
	{
		this.date = new Date((long)number);
	}
	
	public void    setDate(Date date)
	{
		this.date = date;
	}
	
	public void    setBoolean(boolean bool)
	{
		this.date = null;
	}
	
	public void    setInteger(long number)
	{
		this.date = new Date(number);
	}

	
	public int getLength()
	{
		return -1;
	}
	
	public int getPrecision()
	{
		return precision;
	}
	
	public void setLength(int length, int precision)
	{
        this.precision = precision;
	}
	
	public void setLength(int length)
	{
	}
	
	public void setPrecision(int precision)
	{
        this.precision = precision;
	}
	
	public Object clone()
	{
		try
		{
			ValueDate retval   = (ValueDate)super.clone();
			return retval;
		}
		catch(CloneNotSupportedException e)
		{
			return null;
		}
	}

    public BigDecimal getBigNumber()
    {
        if (date==null) return BigDecimal.ZERO;
        return new BigDecimal(date.getTime());
    }

    public void setBigNumber(BigDecimal number)
    {
        setInteger(number.longValue());
    }
    
    public Serializable getSerializable() {
        return date;
    }
    
	public byte[] getBytes() {
		return null;
	}

	public void setBytes(byte[] b) {
	}
}
