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

package org.pentaho.di.compatibility;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import org.pentaho.di.core.Const;


/**
 * This class contains a Value of type Number and the length/precision by which it is described.
 * 
 * @author Matt
 * @since 15-10-2004
 *
 */
public class ValueNumber implements ValueInterface, Cloneable
{
	private double number;
	private int length;
	private int precision;

	public ValueNumber()
	{
		this.number    = 0.0;
		this.length    = -1;
		this.precision = -1;
	}
	
	public ValueNumber(double number)
	{
		this.number    = number;
		this.length    = -1;
		this.precision = -1;
	}

	public int getType()
	{
		return Value.VALUE_TYPE_NUMBER;
	}

	public String getTypeDesc()
	{
		return "Number";
	}

	public String getString()
	{
		return Double.toString(this.number);
	}

    public void setSerializable(Serializable ser) {
        
    }
    
	public double getNumber()
	{
		return this.number;
	}

	public Date getDate()
	{
		return new Date((long)number);
	}

	public boolean getBoolean()
	{
		return number!=0.0;
	}

	public long getInteger()
	{
		return Math.round(number);
	}
	
	public void    setString(String string)
	{
		this.number = Const.toDouble(string, 0.0);
	}
	
	public void    setNumber(double number)
	{
		this.number = number;
	}
	
	public void    setDate(Date date)
	{
		this.number = date.getTime();
	}
	
	public void    setBoolean(boolean bool)
	{
		this.number = bool?1.0:0.0;
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
			ValueNumber retval   = (ValueNumber)super.clone();
			return retval;
		}
		catch(CloneNotSupportedException e)
		{
			return null;
		}
	}

    public BigDecimal getBigNumber()
    {
        return BigDecimal.valueOf(number);
    }

    public void setBigNumber(BigDecimal number)
    {
        this.number = number.doubleValue();
    }
    
    public Serializable getSerializable() {
        return new Double(number);
    }
    
	public byte[] getBytes() {
		return null;
	}

	public void setBytes(byte[] b) {
	}
}
