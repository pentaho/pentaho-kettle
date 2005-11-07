 /**********************************************************************
 **                                                                   **
 **               This code belongs to the KETTLE project.            **
 **                                                                   **
 ** It belongs to, is maintained by and is copyright 1999-2005 by     **
 **                                                                   **
 **      i-Bridge bvba                                                **
 **      Fonteinstraat 70                                             **
 **      9400 OKEGEM                                                  **
 **      Belgium                                                      **
 **      http://www.kettle.be                                         **
 **      info@kettle.be                                               **
 **                                                                   **
 **********************************************************************/
 
package be.ibridge.kettle.core.value;

import java.math.BigDecimal;
import java.util.Date;

import be.ibridge.kettle.core.Const;

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
		return ""+this.number;
	}

	public double getNumber()
	{
		return (double)this.number;
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

}
