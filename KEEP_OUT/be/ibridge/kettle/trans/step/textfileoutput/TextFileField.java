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
 

package be.ibridge.kettle.trans.step.textfileoutput;
import be.ibridge.kettle.core.value.Value;

/**
 * Describes a single field in a text file
 * 
 * @author Matt
 * @since 11-05-2005
 *
 */
public class TextFileField implements Cloneable
{
	private String 	name;
	private int 	type;
	private String 	format;
	private int 	length;
	private int 	precision;
	private String 	currencySymbol;
	private String 	decimalSymbol;
	private String 	groupingSymbol;
	private String 	nullString;
	
	public TextFileField(String name, int type, String format, int length, int precision, String currencySymbol, String decimalSymbol, String groupSymbol, String nullString)
	{
		this.name      		= name;
		this.type			= type;
		this.format			= format;
		this.length			= length;
		this.precision  	= precision;
		this.currencySymbol	= currencySymbol;
		this.decimalSymbol	= decimalSymbol;
		this.groupingSymbol	= groupSymbol;
		this.nullString		= nullString; 
	}
	
	public TextFileField()
	{
	}

	
	public int compare(Object obj)
	{
		TextFileField field = (TextFileField)obj;
		
		return name.compareTo(field.getName());
	}

	public boolean equal(Object obj)
	{
		TextFileField field = (TextFileField)obj;
		
		return name.equals(field.getName());
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
	
	public void setType(String typeDesc)
	{
	    this.type = Value.getType(typeDesc);
	}
	
	public String getFormat()
	{
		return format;
	}
	
	public void setFormat(String format)
	{
		this.format = format;
	}
	
	public String getGroupingSymbol()
	{
		return groupingSymbol;
	}
	
	public void setGroupingSymbol(String group_symbol)
	{
		this.groupingSymbol = group_symbol;
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
	
	public String getNullString()
	{
		return nullString;	
	}
	
	public void setNullString(String null_string)
	{
		this.nullString = null_string;
	}
	
	public String toString()
	{
		return name+":"+getTypeDesc();
	}	
}
