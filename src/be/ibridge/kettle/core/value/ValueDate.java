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
import java.text.SimpleDateFormat;
import java.util.Date;

import be.ibridge.kettle.core.Const;

/**
 * This class contains a Value of type Date.
 * 
 * @author Matt
 * @since 15-10-2004
 *
 */
public class ValueDate implements ValueInterface, Cloneable
{
	private Date date;

	public ValueDate()
	{
		this.date     = null;
	}
	
	public ValueDate(Date date)
	{
		this.date      = date;
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
		SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS");
		return df.format(date);
	}

	public double getNumber()
	{
		if (date==null) return 0.0;
		return (double)date.getTime();
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
        return new BigDecimal(date.getTime());
    }

    public void setBigNumber(BigDecimal number)
    {
        setInteger(number.longValue());
    }

}
