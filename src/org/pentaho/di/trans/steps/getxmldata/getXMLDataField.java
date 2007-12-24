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
 

package org.pentaho.di.trans.steps.getxmldata;
import org.w3c.dom.Node;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.XMLHandler;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.exception.KettleValueException;

/**
 * Describes an XML field and the position in an XML field, file
 * 
 * @author Samatar, Brahim
 * @since 20-06-2007
 *
 */
public class getXMLDataField implements Cloneable
{
    public final static int TYPE_TRIM_NONE  = 0;
    public final static int TYPE_TRIM_LEFT  = 1;
    public final static int TYPE_TRIM_RIGHT = 2;
    public final static int TYPE_TRIM_BOTH  = 3;
    
    public final static int ELEMENT_TYPE_NODE  = 0;
    public final static int ELEMENT_TYPE_ATTRIBUT  = 1;
    
    public final static String trimTypeCode[] = { "none", "left", "right", "both" };
    
    public final static String trimTypeDesc[] = {
      Messages.getString("getXMLDataField.TrimType.None"),
      Messages.getString("getXMLDataField.TrimType.Left"),
      Messages.getString("getXMLDataField.TrimType.Right"),
      Messages.getString("getXMLDataField.TrimType.Both")
    };
    
    public final static String ElementTypeCode[] = { "node", "attribut" };
    
    public final static String ElementTypeDesc[] = {
        Messages.getString("getXMLDataField.ElementType.Node"),
        Messages.getString("getXMLDataField.ElementType.Attribut")
      };
    
    
	private String 	  name;
	private String 	  xpath;
	
    private int 	  type;
    private int       length;
    private String    format;
    private int       trimtype;
    private int       elementtype;
    private int       precision;
    private String 	  currencySymbol;
	private String 	  decimalSymbol;
	private String 	  groupSymbol;
	private boolean   repeat;

   
	public getXMLDataField(String fieldname)
	{
		this.name           = fieldname;
		this.xpath          = "";
		this.length         = -1;
		this.type           = ValueMetaInterface.TYPE_STRING;
		this.format         = "";
		this.trimtype       = TYPE_TRIM_NONE;
		this.elementtype    = ELEMENT_TYPE_NODE;
		this.groupSymbol   = "";
		this.decimalSymbol = "";
		this.currencySymbol= "";
		this.precision      = -1;
		this.repeat         = false;
	}
    
    public getXMLDataField()
    {
       this("");
    }


    public String getXML()
    {
        String retval="";
        
        retval+="      <field>"+Const.CR;
        retval+="        "+XMLHandler.addTagValue("name",         getName());
        retval+="        "+XMLHandler.addTagValue("xpath",        getXPath());
        retval+="        "+XMLHandler.addTagValue("element_type", getElementTypeCode());
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

	public getXMLDataField(Node fnode) throws KettleValueException
    {
        setName( XMLHandler.getTagValue(fnode, "name") );
        setXPath( XMLHandler.getTagValue(fnode, "xpath") );
        setElementType( getElementTypeByCode(XMLHandler.getTagValue(fnode, "element_type")) );
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
    
    public final static int getElementTypeByCode(String tt)
    {
        if (tt==null) return 0;
        
        for (int i=0;i<ElementTypeCode.length;i++)
        {
            if (ElementTypeCode[i].equalsIgnoreCase(tt)) return i;
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
    
    public final static int getElementTypeByDesc(String tt)
    {
        if (tt==null) return 0;
        
        for (int i=0;i<ElementTypeDesc.length;i++)
        {
            if (ElementTypeDesc[i].equalsIgnoreCase(tt)) return i;
        }
        return 0;
    }

    public final static String getTrimTypeCode(int i)
    {
        if (i<0 || i>=trimTypeCode.length) return trimTypeCode[0];
        return trimTypeCode[i]; 
    }
    
    public final static String getElementTypeCode(int i)
    {
        if (i<0 || i>=ElementTypeCode.length) return ElementTypeCode[0];
        return ElementTypeCode[i]; 
    }
    
    
    public final static String getTrimTypeDesc(int i)
    {
        if (i<0 || i>=trimTypeDesc.length) return trimTypeDesc[0];
        return trimTypeDesc[i]; 
    }
    
    public final static String getElementTypeDesc(int i)
    {
        if (i<0 || i>=ElementTypeDesc.length) return ElementTypeDesc[0];
        return ElementTypeDesc[i]; 
    }
    
    public Object clone()
	{
		try
		{
			getXMLDataField retval = (getXMLDataField) super.clone();
          
            
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
	
	public String getXPath()
	{
		return xpath;
	}

	
	
	public void setXPath(String fieldxpath)
	{
		this.xpath = fieldxpath;
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
	
/*	public void setSamples(String samples[])
	{
		this.samples = samples;
	}
    
    public String[] getSamples()
    {
        return samples;
    }*/

	public int getTrimType()
	{
		return trimtype;
	}

	public int getElementType()
	{
		return elementtype;
	}
	
  public String getTrimTypeCode()
	{
		return getTrimTypeCode(trimtype);
	}
  
  public String getElementTypeCode()
	{
		return getElementTypeCode(elementtype);
	}

	public String getTrimTypeDesc()
	{
		return getTrimTypeDesc(trimtype);
	}
	
	public String getElementTypeDesc()
	{
		return getElementTypeDesc(elementtype);
	}
	
	public void setTrimType(int trimtype)
	{
		this.trimtype= trimtype;
	}
	
	
	
	public void setElementType(int element_type)
	{
		this.elementtype= element_type;
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

        return enc;
    }
	
	public void guess()
	{
	}

   
}
