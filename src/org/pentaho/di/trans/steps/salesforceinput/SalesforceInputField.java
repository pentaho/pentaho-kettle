/*******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.trans.steps.salesforceinput;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.w3c.dom.Node;

/**
 * Describes an SalesforceInput field
 * 
 * @author Samatar Hassan
 * @since 10-06-2007
 */
public class SalesforceInputField implements Cloneable
{
	private static Class<?> PKG = SalesforceInputMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

    public final static int TYPE_TRIM_NONE  = 0;
    public final static int TYPE_TRIM_LEFT  = 1;
    public final static int TYPE_TRIM_RIGHT = 2;
    public final static int TYPE_TRIM_BOTH  = 3;
    
    public final static String trimTypeCode[] = { "none", "left", "right", "both" };
    
    public final static String trimTypeDesc[] = {
      BaseMessages.getString(PKG, "SalesforceInputField.TrimType.None"),
      BaseMessages.getString(PKG, "SalesforceInputField.TrimType.Left"),
      BaseMessages.getString(PKG, "SalesforceInputField.TrimType.Right"),
      BaseMessages.getString(PKG, "SalesforceInputField.TrimType.Both")
    };
    
	private String 	  name;
	private String 	  field;
    private int 	  type;
    private int       length;
    private String    format;
    private int       trimtype;
    private int       precision;
    private String 	  currencySymbol;
	private String 	  decimalSymbol;
	private String 	  groupSymbol;
	private boolean   repeat;
	private boolean  idlookup;

    private String    samples[];

    
	public SalesforceInputField(String fieldname)
	{
		this.name             = fieldname;
		this.field        = "";
		this.length         = -1;
		this.type           = ValueMeta.TYPE_STRING;
		this.format         = "";
		this.trimtype       = TYPE_TRIM_NONE;
		this.groupSymbol   = "";
		this.decimalSymbol = "";
		this.currencySymbol= "";
		this.precision      = -1;
		this.repeat         = false;
		this.idlookup	=false;
	}
    
    public SalesforceInputField()
    {
        this("");
    }

    public String getXML()
    {
        String retval="";
        
        retval+="      <field>"+Const.CR;
        retval+="        "+XMLHandler.addTagValue("name",         getName());
        retval+="        "+XMLHandler.addTagValue("field",      getField());
        retval+="        "+XMLHandler.addTagValue("idlookup",     isIdLookup());
        retval+="        "+XMLHandler.addTagValue("type",         getTypeDesc());
        retval+="        "+XMLHandler.addTagValue("format",       getFormat());
        retval+="        "+XMLHandler.addTagValue("currency",     getCurrencySymbol());
        retval+="        "+XMLHandler.addTagValue("decimal",      getDecimalSymbol());
        retval+="        "+XMLHandler.addTagValue("group",        getGroupSymbol());
        retval+="        "+XMLHandler.addTagValue("length",       getLength());
        retval+="        "+XMLHandler.addTagValue("precision",    getPrecision());
        retval+="        "+XMLHandler.addTagValue("trim_type",    getTrimTypeCode());
        retval+="        "+XMLHandler.addTagValue("repeat",       isRepeated());
        retval+="        </field>"+Const.CR;
        return retval;
    }

	public SalesforceInputField(Node fnode) throws KettleStepException
    {
        setName( XMLHandler.getTagValue(fnode, "name") );
        setField( XMLHandler.getTagValue(fnode, "field") );
        setIdLookup("Y".equalsIgnoreCase(XMLHandler.getTagValue(fnode, "idlookup")) ); 
        setType( ValueMeta.getType(XMLHandler.getTagValue(fnode, "type")) );
        setFormat( XMLHandler.getTagValue(fnode, "format") );
        setCurrencySymbol( XMLHandler.getTagValue(fnode, "currency") );
        setDecimalSymbol( XMLHandler.getTagValue(fnode, "decimal") );
        setGroupSymbol( XMLHandler.getTagValue(fnode, "group") );
        setLength( Const.toInt(XMLHandler.getTagValue(fnode, "length"), -1) );
        setPrecision( Const.toInt(XMLHandler.getTagValue(fnode, "precision"), -1) );
        setTrimType( getTrimTypeByCode(XMLHandler.getTagValue(fnode, "trim_type")) );
        setRepeated( !"N".equalsIgnoreCase(XMLHandler.getTagValue(fnode, "repeat")) );
    }

    public final static int getTrimTypeByCode(String tt)
    {
        if (tt==null) return 0;
        
        for (int i=0;i<trimTypeCode.length;i++)
        {
            if (trimTypeCode[i].equalsIgnoreCase(tt)) return i;
        }
        return 0;
    }
       
    public final static int getTrimTypeByDesc(String tt)
    {
        if (tt==null) return 0;
        
        for (int i=0;i<trimTypeDesc.length;i++)
        {
            if (trimTypeDesc[i].equalsIgnoreCase(tt)) return i;
        }
        return 0;
    }
    
    public final static String getTrimTypeCode(int i)
    {
        if (i<0 || i>=trimTypeCode.length) return trimTypeCode[0];
        return trimTypeCode[i]; 
    }
    
    public final static String getTrimTypeDesc(int i)
    {
        if (i<0 || i>=trimTypeDesc.length) return trimTypeDesc[0];
        return trimTypeDesc[i]; 
    }
    
    public Object clone()
	{
		try
		{
			SalesforceInputField retval = (SalesforceInputField) super.clone();
            
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
	
	public String getField()
	{
		return field;
	}

	public void setField(String fieldvalue)
	{
		this.field = fieldvalue;
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
	
	public void setSamples(String samples[])
	{
		this.samples = samples;
	}
    
    public String[] getSamples()
    {
        return samples;
    }

	public int getTrimType()
	{
		return trimtype;
	}
	
    public String getTrimTypeCode()
	{
		return getTrimTypeCode(trimtype);
	}
  

	public String getTrimTypeDesc()
	{
		return getTrimTypeDesc(trimtype);
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
	
	public boolean isIdLookup()
	{
		return idlookup;
	}
	
	public void setIdLookup(boolean idlookup)
	{
		this.idlookup = idlookup;
	}
	
	public void flipRepeated()
	{
		repeat = !repeat;		
	}

}