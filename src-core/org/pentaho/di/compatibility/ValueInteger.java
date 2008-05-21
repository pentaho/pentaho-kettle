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
import java.util.Date;

import org.pentaho.di.core.Const;



/**
 * This class contains a Value of type Integer and the length by which it is described.
 * 
 * @author Matt
 * @since 15-10-2004
 *
 */
public class ValueInteger implements ValueInterface, Cloneable
{
	private long number;
	private int length;

	public ValueInteger()
	{
		this.number    = 0L;
		this.length    = -1;
	}
	
	public ValueInteger(long number)
	{
		this.number    = number;
		this.length    = -1;
	}

	public int getType()
	{
		return Value.VALUE_TYPE_INTEGER;
	}

	public String getTypeDesc()
	{
		return "Integer";
	}

	public String getString()
	{
		return Long.toString(number);
	}

	public double getNumber()
	{
		return this.number;
	}

	public Date getDate()
	{
		return new Date(number);
	}

	public boolean getBoolean()
	{
		return number!=0L;
	}

	public long getInteger()
	{
		return number;
	}	
	
    public void setSerializable(Serializable ser) {
        
    }
    
	public void    setString(String string)
	{
		this.number = Const.toLong(string, 0L);
	}
	
	public void    setNumber(double number)
	{
		this.number = Math.round(number);
	}
	
	public void    setDate(Date date)
	{
		this.number = date.getTime();
	}
	
	public void    setBoolean(boolean bool)
	{
		this.number = bool?1L:0L;
	}
	
	public void    setInteger(long number)
	{
		this.number = number;
	}

	
	
	public int getLength()
	{
		return length;
	}
	
	public int getPrecision()
	{
		return 0;
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
			ValueInteger retval   = (ValueInteger)super.clone();
			return retval;
		}
		catch(CloneNotSupportedException e)
		{
			return null;
		}
	}

    public BigDecimal getBigNumber()
    {
        return new BigDecimal(number);
    }

    public void setBigNumber(BigDecimal number)
    {
        this.number = number.longValue();
        
    }
    public Serializable getSerializable() {
        return new Long(number);
    }
    
	public byte[] getBytes() {
		return null;
	}

	public void setBytes(byte[] b) {
	}
}
