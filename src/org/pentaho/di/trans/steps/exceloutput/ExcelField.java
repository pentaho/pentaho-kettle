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
 

package org.pentaho.di.trans.steps.exceloutput;
import org.pentaho.di.core.row.ValueMeta;

/**
 * Describes a single field in an excel file
 * 
 * TODO: allow the width of a column to be set --> data.sheet.setColumnView(column, width);
 * TODO: allow the default font to be set
 * TODO: allow an aggregation formula on one of the columns --> SUM(A2:A151)
 * 
 * @author Matt
 * @since 7-09-2006
 *
 */
public class ExcelField implements Cloneable
{
	private String 	name;
	private int 	type;
	private String 	format;
	
	public ExcelField(String name, int type, String format)
	{
		this.name      		= name;
		this.type			= type;
		this.format			= format;
	}
	
	public ExcelField()
	{
	}

	
	public int compare(Object obj)
	{
		ExcelField field = (ExcelField)obj;
		
		return name.compareTo(field.getName());
	}

	public boolean equal(Object obj)
	{
		ExcelField field = (ExcelField)obj;
		
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
	
	public String toString()
	{
		return name+":"+getTypeDesc();
	}	
}
