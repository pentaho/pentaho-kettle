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
 

package be.ibridge.kettle.trans.step.xmlinput;
import org.w3c.dom.Node;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.XMLHandler;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.exception.KettleValueException;
import be.ibridge.kettle.core.value.Value;

/**
 * Describes an XML field and the position in an XML file
 * 
 * @author Matt
 * @since 16-12-2005
 *
 */
public class XMLInputField implements Cloneable
{
    public final static int TYPE_TRIM_NONE  = 0;
    public final static int TYPE_TRIM_LEFT  = 1;
    public final static int TYPE_TRIM_RIGHT = 2;
    public final static int TYPE_TRIM_BOTH  = 3;
    
    public final static String trimTypeDesc[] = { "none", "left", "right", "both" };
    
    public final static String POSITION_MARKER  = ",";
    
	private String 	  name;
	private XMLInputFieldPosition[] fieldPosition;
	
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

    
	public XMLInputField(String fieldname, XMLInputFieldPosition[] xmlInputFieldPositions)
	{
		this.name                     = fieldname;
		this.fieldPosition   = xmlInputFieldPositions;
		this.length         = -1;
		this.type           = Value.VALUE_TYPE_STRING;
		this.format         = "";
		this.trimtype       = TYPE_TRIM_NONE;
		this.groupSymbol   = "";
		this.decimalSymbol = "";
		this.currencySymbol= "";
		this.precision      = -1;
		this.repeat         = false;
	}
    
    public XMLInputField()
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

	public XMLInputField(Node fnode) throws KettleValueException
    {
        setName( XMLHandler.getTagValue(fnode, "name") );
        setType( Value.getType(XMLHandler.getTagValue(fnode, "type")) );
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
        
        fieldPosition = new XMLInputFieldPosition[nrPositions];
        
        for (int i=0;i<nrPositions;i++)
        {
            Node positionnode = XMLHandler.getSubNodeByNr(positions, "position", i); 
            String encoded = XMLHandler.getNodeValue(positionnode);
            fieldPosition[i] = new XMLInputFieldPosition(encoded);
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
			XMLInputField retval = (XMLInputField) super.clone();
            
            if (fieldPosition!=null)
            {
                XMLInputFieldPosition[] positions = new XMLInputFieldPosition[fieldPosition.length];
                for (int i=0;i<fieldPosition.length;i++)
                {
                    positions[i] = (XMLInputFieldPosition)fieldPosition[i].clone();
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
    public XMLInputFieldPosition[] getFieldPosition()
    {
        return fieldPosition;
    }

    /**
     * @param xmlInputFieldPositions The xmlInputFieldPositions to set.
     */
    public void setFieldPosition(XMLInputFieldPosition[] xmlInputFieldPositions)
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
            XMLInputFieldPosition pos = fieldPosition[i];
            if (i>0) enc+=POSITION_MARKER;
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
            fieldPosition = new XMLInputFieldPosition[codes.length];
            for (int i=0;i<codes.length;i++)
            {
                fieldPosition[i] = new XMLInputFieldPosition(codes[i]);
            }
        }
        catch(Exception e)
        {
            throw new KettleException("Unable to parse the field positions because of an error"+Const.CR+"Please use E=element or A=attribute in a comma separated list (code: "+encoded+")", e);
        }
    }
}
