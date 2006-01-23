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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import be.ibridge.kettle.core.Const;

/**
 * This class contains a Value of type String and the length by which it is described.
 * 
 * @author Matt
 * @since 15-10-2004
 *
 */
public class ValueString implements ValueInterface, Cloneable
{
	private String string;
	private int length;

	public ValueString()
	{
		string = null;
		length = -1;
	}
	public ValueString(String string)
	{
		this.string = string;
		length = -1;
	}

	public int getType()
	{
		return Value.VALUE_TYPE_STRING;
	}

	public String getTypeDesc()
	{
		return "String";
	}

	public String getString()
	{
		return this.string;
	}

	public double getNumber()
	{
		return Const.toDouble( string, 0.0);
	}

	public Date getDate()
	{
	    if (string!=null)
	    {
			SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS");
			try
			{
				return df.parse(string);
			}
			catch(ParseException e)
			{
			}
	    }
	    return null;
	}

	public boolean getBoolean()
	{
		return "Y".equalsIgnoreCase(string) || "TRUE".equalsIgnoreCase(string) || "YES".equalsIgnoreCase(string) || "1".equalsIgnoreCase(string);
	}

	public long getInteger()
	{
		return Const.toLong(string, 0L);
	}
	
	public void    setString(String string)
	{
		this.string = string;
	}
	
	public void    setNumber(double number)
	{
		this.string = ""+number;
	}
	
	public void    setDate(Date date)
	{
		SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS");
		this.string = df.format(date);
	}
	
	public void    setBoolean(boolean bool)
	{
		this.string = bool?"Y":"N";
	}
	
	public void    setInteger(long number)
	{
		this.string = ""+number;
	}
	
	public int getLength()
	{
		return length;
	}
	
	public int getPrecision()
	{
		return -1;
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
			ValueString retval   = (ValueString)super.clone();
			return retval;
		}
		catch(CloneNotSupportedException e)
		{
			return null;
		}
	}
    
    public BigDecimal getBigNumber()
    {
        if (string==null) return null;
        return new BigDecimal(string);
    }
    
    public void setBigNumber(BigDecimal number)
    {
        string = number.toString();
    }

}
