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
		this.number = new BigDecimal( Const.toDouble(string, 0.0) );
	}
	
	public void    setNumber(double number)
	{
		this.number = new BigDecimal(number);
	}
	
	public void    setDate(Date date)
	{
		this.number = new BigDecimal( (double)date.getTime() );
	}
	
	public void    setBoolean(boolean bool)
	{
		this.number = new BigDecimal((double)(bool?1.0:0.0));
	}
	
	public void    setInteger(long number)
	{
		this.number = new BigDecimal( (double) number );
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
}
