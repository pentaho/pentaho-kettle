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

/**
 * This class contains a Value of type Boolean.
 * 
 * @author Matt
 * @since 15-10-2004
 *
 */
public class ValueBoolean implements ValueInterface, Cloneable
{
	private boolean bool;

	public ValueBoolean()
	{
		this.bool     = false;
	}
	
	public ValueBoolean(boolean bool)
	{
		this.bool      = bool;
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
	
	public void    setString(String string)
	{
		this.bool = "Y".equalsIgnoreCase(string) || 
		            "TRUE".equalsIgnoreCase(string) ||
					"YES".equalsIgnoreCase(string);
	}
	
	public void    setNumber(double number)
	{
		this.bool = number==0.0?false:true;
	}
	
	public void    setDate(Date date)
	{
		this.bool = false;
	}
	
	public void    setBoolean(boolean bool)
	{
		this.bool = bool;
	}
	
	public void    setInteger(long number)
	{
		this.bool = number==0?false:true;
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

}
