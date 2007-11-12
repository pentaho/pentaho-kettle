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

package org.pentaho.di.trans.steps.textfileoutput;

import org.pentaho.di.core.row.ValueMeta;

/**
 * Describes a single field in a text file
 * 
 * @author Matt
 * @since 11-05-2005
 * 
 */
public class TextFileField implements Cloneable
{
	private String name; 

	private int type;

	private String format;

	private int length;

	private int precision;

	private String currencySymbol;

	private String decimalSymbol;

	private String groupingSymbol;

	private String nullString;

	private int trimType;

	public TextFileField(String name, int type, String format, int length, int precision,
			String currencySymbol, String decimalSymbol, String groupSymbol, String nullString)
	{
		this.name = name;
		this.type = type;
		this.format = format;
		this.length = length;
		this.precision = precision;
		this.currencySymbol = currencySymbol;
		this.decimalSymbol = decimalSymbol;
		this.groupingSymbol = groupSymbol;
		this.nullString = nullString;
	}

	public TextFileField()
	{
	}

	public int compare(Object obj)
	{
		TextFileField field = (TextFileField) obj;

		return name.compareTo(field.getName());
	}

	public boolean equal(Object obj)
	{
		TextFileField field = (TextFileField) obj;

		return name.equals(field.getName());
	}

	public Object clone()
	{
		try
		{
			Object retval = super.clone();
			return retval;
		} catch (CloneNotSupportedException e)
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

	public void setType(String typeDesc)
	{
		this.type = ValueMeta.getType(typeDesc);
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
		return name + ":" + getTypeDesc();
	}

	public int getTrimType()
	{
		return trimType;
	}

	public void setTrimType(int trimType)
	{
		this.trimType = trimType;
	}

	public String getTrimTypeCode()
	{
		return ValueMeta.getTrimTypeCode(trimType);
	}

	public String getTrimTypeDesc()
	{
		return ValueMeta.getTrimTypeDesc(trimType);
	}
}
