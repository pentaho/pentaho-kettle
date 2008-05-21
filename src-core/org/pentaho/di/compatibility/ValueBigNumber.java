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


/**
 * This class contains a Value of type BigNumber and the length/precision by which it is described.
 * 
 * @author Matt
 * @since 05-09-2005
 *
 */
public class ValueBigNumber implements ValueInterface, Cloneable
{
	private BigDecimal number;
	private int length;
	private int precision;

	public ValueBigNumber()
	{
		this.number    = null;
		this.length    = -1;
		this.precision = -1;
	}
	
	public ValueBigNumber(BigDecimal number)
	{
        // System.out.println("new ValueBigNumber("+number+")"); OK
        
		this.number    = number;
		this.length    = -1;
		this.precision = -1;
	}

	public int getType()
	{
		return Value.VALUE_TYPE_BIGNUMBER;
	}
    
    public Serializable getSerializable() {
        return number;
    }

	public String getTypeDesc()
	{
		return "BigNumber";
	}

	public String getString()
	{
        if (number==null) return null;
		return number.toString();
	}

	public double getNumber()
	{
        if (number==null) return 0.0;
		return this.number.doubleValue();
	}

	public Date getDate()
	{
        if (number==null) return null;
		return new Date(number.longValue());
	}

	public boolean getBoolean()
	{
        if (number==null) return false;
		return number.longValue()!=0L;
	}

	public long getInteger()
	{
        if (number==null) return 0L;
		return number.longValue();
	}
	
	public void    setString(String string)
	{
		try
        {
            this.number = new BigDecimal( string );
        }
        catch (NumberFormatException e)
        {
            this.number = BigDecimal.ZERO;
        }
	}
	
	public void    setNumber(double number)
	{
		this.number = BigDecimal.valueOf(number);
	}
	
	public void    setDate(Date date)
	{
		this.number = new BigDecimal( date.getTime() );
	}
	
	public void    setBoolean(boolean bool)
	{
		this.number = bool ? BigDecimal.ONE : BigDecimal.ZERO;
	}
	
	public void    setInteger(long number)
	{
		this.number = new BigDecimal( number );
	}

    public void setSerializable(Serializable ser) {
        
    }
	
	
	public int getLength()
	{
		return length;
	}
	
	public int getPrecision()
	{
		return precision;
	}
	
	public void setLength(int length, int precision)
	{
		this.length = length;
		this.precision = precision;
	}
	
	public void setLength(int length)
	{
		this.length = length;
	}
	
	public void setPrecision(int precision)
	{
		this.precision = precision;
	}
	
	public Object clone()
	{
		try
		{
			ValueBigNumber retval   = (ValueBigNumber)super.clone();
			return retval;
		}
		catch(CloneNotSupportedException e)
		{
			return null;
		}
	}

    public BigDecimal getBigNumber()
    {
        return number;
    }

    public void setBigNumber(BigDecimal number)
    {
        this.number = number;
    }

	public byte[] getBytes() {
		return null;
	}

	public void setBytes(byte[] b) {
	}
}
