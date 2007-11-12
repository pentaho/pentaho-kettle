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
 

package org.pentaho.di.trans.steps.excelinput;

import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;

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
		this.type           = ValueMetaInterface.TYPE_STRING;
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
		return ValueMeta.getTypeDesc(type);
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
