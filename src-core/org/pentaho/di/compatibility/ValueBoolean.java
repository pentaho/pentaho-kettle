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
 * This class contains a Value of type Boolean.
 * 
 * @author Matt Casters
 * @since 15-10-2004
 */
public class ValueBoolean implements ValueInterface, Cloneable
{
	private boolean bool;

	public ValueBoolean()
	{
		this.bool = false;
	}
	
	public ValueBoolean(boolean bool)
	{
		this.bool = bool;
	}

	public int getType()
	{
		return Value.VALUE_TYPE_BOOLEAN;
	}

	public String getTypeDesc()
	{
		return "Boolean";
	}

	public String getString()
	{
		return bool?"Y":"N";
	}

	public double getNumber()
	{
		return bool?1.0:0.0;
	}

	public Date getDate()
	{
		return null;
	}
    
	public boolean getBoolean()
	{
		return bool;
	}

	public long getInteger()
	{
		return bool?1L:0L;
	}
	
	public void setString(String string)
	{
		this.bool = "Y".equalsIgnoreCase(string) || 
		            "TRUE".equalsIgnoreCase(string) ||
					"YES".equalsIgnoreCase(string);
	}
	
	public void setNumber(double number)
	{
		this.bool = (number == 0.0)?false:true;
	}
	
	public void setDate(Date date)
	{
		this.bool = false;
	}
	
    public void setSerializable(Serializable ser) {
        
    }
    
	public void setBoolean(boolean bool)
	{
		this.bool = bool;
	}
	
	public void setInteger(long number)
	{
		this.bool = (number == 0)?false:true;
	}
	
	public int getLength()
	{
		return -1;
	}
	
	public int getPrecision()
	{
		return -1;
	}
	
	public void setLength(int length, int precision)
	{
	}
	
	public void setLength(int length)
	{
	}
	
	public void setPrecision(int precision)
	{
	}
	
	public Object clone()
	{
		try
		{
			ValueBoolean retval   = (ValueBoolean)super.clone();
			return retval;
		}
		catch(CloneNotSupportedException e)
		{
			return null;
		}
	}

    public BigDecimal getBigNumber()
    {
        return new BigDecimal(bool?1:0);
    }

    public void setBigNumber(BigDecimal number)
    {
        bool = number.intValue()!=0;
    }

    public Serializable getSerializable() {
        return Boolean.valueOf(bool);
    }
    
	public byte[] getBytes() {
		return null;
	}

	public void setBytes(byte[] b) {
	}
}