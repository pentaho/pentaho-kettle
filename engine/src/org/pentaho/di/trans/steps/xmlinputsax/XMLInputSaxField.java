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

package org.pentaho.di.trans.steps.xmlinputsax;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.xml.XMLHandler;
import org.w3c.dom.Node;



/**
 * Describes an XML field and the position in an XML file
 * 
 * @author Matt
 * @since 16-12-2005
 *
 */
public class XMLInputSaxField implements Cloneable
{
    public final static int TYPE_TRIM_NONE  = 0;
    public final static int TYPE_TRIM_LEFT  = 1;
    public final static int TYPE_TRIM_RIGHT = 2;
    public final static int TYPE_TRIM_BOTH  = 3;
    
    public final static String trimTypeDesc[] = { "none", "left", "right", "both" };
    
    public final static String POSITION_MARKER  = ",";
    
	private String 	  name;
	private XMLInputSaxFieldPosition[] fieldPosition;
	
    private int 	  type;
    private int       length;
    private String    format;
    private int       trimtype;
    private int       precision;
    private String 	  currencySymbol;
	private String 	  decimalSymbol;
	private String 	  groupSymbol;
	private boolean   repeat;

    private String    samples[];

    
	public XMLInputSaxField(String fieldname, XMLInputSaxFieldPosition[] xmlInputFieldPositions)
	{
		this.name                     = fieldname;
		this.fieldPosition   = xmlInputFieldPositions;
		this.length         = -1;
		this.type           = ValueMeta.TYPE_STRING;
		this.format         = "";
		this.trimtype       = TYPE_TRIM_NONE;
		this.groupSymbol   = "";
		this.decimalSymbol = "";
		this.currencySymbol= "";
		this.precision      = -1;
		this.repeat         = false;
	}
    
    public XMLInputSaxField()
    {
        this(null, null);
    }


    public String getXML()
    {
        String retval="";
        
        retval+="      <field>"+Const.CR;
        retval+="        "+XMLHandler.addTagValue("name",         getName());
        retval+="        "+XMLHandler.addTagValue("type",         getTypeDesc());
        retval+="        "+XMLHandler.addTagValue("format",       getFormat());
        retval+="        "+XMLHandler.addTagValue("currency",     getCurrencySymbol());
        retval+="        "+XMLHandler.addTagValue("decimal",      getDecimalSymbol());
        retval+="        "+XMLHandler.addTagValue("group",        getGroupSymbol());
        retval+="        "+XMLHandler.addTagValue("length",       getLength());
        retval+="        "+XMLHandler.addTagValue("precision",    getPrecision());
        retval+="        "+XMLHandler.addTagValue("trim_type",    getTrimTypeDesc());
        retval+="        "+XMLHandler.addTagValue("repeat",       isRepeated());
        
        retval+="        <positions>";
        for (int i=0;i<fieldPosition.length;i++)
        {
            retval+=XMLHandler.addTagValue("position", fieldPosition[i].toString(), false);
        }
        retval+="        </positions>"+Const.CR;

        retval+="        </field>"+Const.CR;
        
        return retval;
    }

	public XMLInputSaxField(Node fnode) throws KettleValueException
    {
        setName( XMLHandler.getTagValue(fnode, "name") );
        setType( ValueMeta.getType(XMLHandler.getTagValue(fnode, "type")) );
        setFormat( XMLHandler.getTagValue(fnode, "format") );
        setCurrencySymbol( XMLHandler.getTagValue(fnode, "currency") );
        setDecimalSymbol( XMLHandler.getTagValue(fnode, "decimal") );
        setGroupSymbol( XMLHandler.getTagValue(fnode, "group") );
        setLength( Const.toInt(XMLHandler.getTagValue(fnode, "length"), -1) );
        setPrecision( Const.toInt(XMLHandler.getTagValue(fnode, "precision"), -1) );
        setTrimType( getTrimType(XMLHandler.getTagValue(fnode, "trim_type")) );
        setRepeated( !"N".equalsIgnoreCase(XMLHandler.getTagValue(fnode, "repeat")) ); 
        
        Node positions = XMLHandler.getSubNode(fnode, "positions");
        int nrPositions = XMLHandler.countNodes(positions, "position");
        
        fieldPosition = new XMLInputSaxFieldPosition[nrPositions];
        
        for (int i=0;i<nrPositions;i++)
        {
            Node positionnode = XMLHandler.getSubNodeByNr(positions, "position", i); 
            String encoded = XMLHandler.getNodeValue(positionnode);
            fieldPosition[i] = new XMLInputSaxFieldPosition(encoded);
        }
    }

    public final static int getTrimType(String tt)
    {
        if (tt==null) return 0;
        
        for (int i=0;i<trimTypeDesc.length;i++)
        {
            if (trimTypeDesc[i].equalsIgnoreCase(tt)) return i;
        }
        return 0;
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
			XMLInputSaxField retval = (XMLInputSaxField) super.clone();
            
            if (fieldPosition!=null)
            {
                XMLInputSaxFieldPosition[] positions = new XMLInputSaxFieldPosition[fieldPosition.length];
                for (int i=0;i<fieldPosition.length;i++)
                {
                    positions[i] = (XMLInputSaxFieldPosition)fieldPosition[i].clone();
                }
                retval.setFieldPosition(positions);
            }
            
			return retval;
		}
		catch(CloneNotSupportedException e)
		{
			return null;
		}
	}
	
    
	/**
     * @return Returns the xmlInputFieldPositions.
     */
    public XMLInputSaxFieldPosition[] getFieldPosition()
    {
        return fieldPosition;
    }

    /**
     * @param xmlInputFieldPositions The xmlInputFieldPositions to set.
     */
    public void setFieldPosition(XMLInputSaxFieldPosition[] xmlInputFieldPositions)
    {
        this.fieldPosition = xmlInputFieldPositions;
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
	
	public void flipRepeated()
	{
		repeat = !repeat;		
	}
    
    public String getFieldPositionsCode()
    {
        String enc="";
        
        for (int i=0;i<fieldPosition.length;i++)
        {
            XMLInputSaxFieldPosition pos = fieldPosition[i];
            if (i>0) enc+=POSITION_MARKER;
            enc+=pos.toString();
        }
        
        return enc;
    }
    
    public String getFieldPositionsCode (int startPosition){
        String enc="";
        
        for (int i=startPosition;i<fieldPosition.length;i++)
        {
            XMLInputSaxFieldPosition pos = fieldPosition[i];
            if (i>startPosition) enc+=POSITION_MARKER;
            enc+=pos.toString();
        }
        
        return enc;
    }
	
	public void guess()
	{
	}

    public void setFieldPosition(String encoded) throws KettleException
    {
        try
        {
            String codes[] = encoded.split(POSITION_MARKER);
            fieldPosition = new XMLInputSaxFieldPosition[codes.length];
            for (int i=0;i<codes.length;i++)
            {
                fieldPosition[i] = new XMLInputSaxFieldPosition(codes[i]);
            }
        }
        catch(Exception e)
        {
            throw new KettleException("Unable to parse the field positions because of an error"+Const.CR+"Please use E=element or A=attribute in a comma separated list (code: "+encoded+")", e);
        }
    }
    
    public boolean equals(Object arg)
    {
    	if(arg instanceof XMLInputSaxField)
    	{
    		XMLInputSaxField f=(XMLInputSaxField)arg;
    		if(this.name.equals(f.getName()) && this.getFieldPositionsCode().equals(f.getFieldPositionsCode())){
    			return true;
    		}
    		else
    		{
    			return false;
    		}
    	}
    	else
    	{
    		return false;
    	}
    	
    }
}

