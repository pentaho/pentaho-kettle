 /**********************************************************************
 **                                                                   **
 **               This code belongs to the KETTLE project.            **
 **                                                                   **
 ** Kettle, from version 2.2 on, is released into the public domain   **
 ** under the Lesser GNU Public License (LGPL).                       **
 **                                                                   **
 ** For more details, please read the document LICENSE.txt, included  **
 ** in this project                                                   **
 **                                                                   **
 ** http://www.kettle.be                                              **
 ** info@kettle.be                                                    **
 **                                                                   **
 **********************************************************************/

 
package be.ibridge.kettle.core.value;

import java.math.BigDecimal;
import java.util.Date;

import be.ibridge.kettle.core.Const;

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
		return ""+this.number;
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
		this.number = (double)date.getTime();
	}
	
	public void    setBoolean(boolean bool)
	{
		this.number = bool?1.0:0.0;
	}
	
	public void    setInteger(long number)
	{
		this.number = (double)number;
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
        return new BigDecimal(number);
    }

    public void setBigNumber(BigDecimal number)
    {
        this.number = number.doubleValue();
    }
}
