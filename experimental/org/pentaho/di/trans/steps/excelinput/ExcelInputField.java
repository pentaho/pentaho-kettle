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
 

package org.pentaho.di.trans.steps.excelinput;
import be.ibridge.kettle.core.value.Value;

/**
 * Describes a single field in an excel file
 * 
 * @author Matt
 * @since 12-04-2006
 *
 */
public class ExcelInputField implements Cloneable
{
	private String 	name;
	private int 	type;
	private int 	length;
	private int 	precision;
	private int 	trimtype;
	private String 	format;
	private String 	currencySymbol;
	private String 	decimalSymbol;
	private String 	groupSymbol;
	private boolean repeat;
    
	public ExcelInputField(String fieldname, int position, int length)
	{
		this.name      = fieldname;
		this.length         = length;
		this.type           = Value.VALUE_TYPE_STRING;
		this.format         = "";
		this.trimtype       = ExcelInputMeta.TYPE_TRIM_NONE;
		this.groupSymbol   = "";
		this.decimalSymbol = "";
		this.currencySymbol= "";
		this.precision      = -1;
		this.repeat         = false;
	}
	
	public ExcelInputField()
	{
	    this(null, -1, -1);
	}

	public Object clone()
	{
		try
		{
			Object retval = super.clone();
			return retval;
		}
		catch(CloneNotSupportedException e)
		{
			return null;
		}
	}
	
	public int getLength()
	{
		return length;
	}
	
	public void setLength(int length)
	{
		this.length = length;
	}
	
	public String getName()
	{
		return name;
	}
	
	public void setName(String fieldname)
	{
		this.name = fieldname;
	}

	public int getType()
	{
		return type;
	}

	public String getTypeDesc()
	{
		return Value.getTypeDesc(type);
	}
	
	public void setType(int type)
	{
		this.type = type;
	}
	
	public String getFormat()
	{
		return format;
	}
	
	public void setFormat(String format)
	{
		this.format = format;
	}
	
	public int getTrimType()
	{
		return trimtype;
	}

	public String getTrimTypeCode()
	{
		return ExcelInputMeta.getTrimTypeCode(trimtype);
	}
  
  public String getTrimTypeDesc()
	{
		return ExcelInputMeta.getTrimTypeDesc(trimtype);
	}
	
	public void setTrimType(int trimtype)
	{
		this.trimtype= trimtype;
	}

	public String getGroupSymbol()
	{
		return groupSymbol;
	}
	
	public void setGroupSymbol(String group_symbol)
	{
		this.groupSymbol = group_symbol;
	}

	public String getDecimalSymbol()
	{
		return decimalSymbol;
	}
	
	public void setDecimalSymbol(String decimal_symbol)
	{
		this.decimalSymbol = decimal_symbol;
	}

	public String getCurrencySymbol()
	{
		return currencySymbol;
	}
	
	public void setCurrencySymbol(String currency_symbol)
	{
		this.currencySymbol = currency_symbol;
	}

	public int getPrecision()
	{
		return precision;
	}
	
	public void setPrecision(int precision)
	{
		this.precision = precision;
	}
	
	public boolean isRepeated()
	{
		return repeat;
	}
	
	public void setRepeated(boolean repeat)
	{
		this.repeat = repeat;
	}
	
	public void flipRepeated()
	{
		repeat = !repeat;		
	}

	public String toString()
	{
		return name+":"+getTypeDesc()+"("+length+","+precision+")";
	}
}
