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
	private String 	  name;
	private XMLInputFieldPosition[] xmlInputFieldPositions;
	private int 	  type;
	private int 	  trimtype;
    private int       length;
	private int 	  precision;
    private String    format;
	private String 	  currencySymbol;
	private String 	  decimalSymbol;
	private String 	  groupSymbol;
	private boolean   repeat;

    private String    samples[];
    /*
	private static final String date_formats[] = new String[] 
		{
			"yyyy/MM/dd HH:mm:ss.SSS", 
			"yyyy/MM/dd HH:mm:ss",
			"dd/MM/yyyy",
			"dd-MM-yyyy",
			"yyyy/MM/dd",
			"yyyy-MM-dd",
			"yyyyMMdd",
			"ddMMyyyy",
			"d-M-yyyy",
			"d/M/yyyy",
			"d-M-yy",
			"d/M/yy",
		}
		;

	private static final String number_formats[] = new String[] 
		{
			"",
			"#",
			Const.DEFAULT_NUMBER_FORMAT,
			"0.00",
			"0000000000000",
			"###,###,###.#######", 
			"###############.###############",
			"#####.###############%",
		}
		;
	*/
    
	public XMLInputField(String fieldname, XMLInputFieldPosition[] xmlInputFieldPositions)
	{
		this.name                     = fieldname;
		this.xmlInputFieldPositions   = xmlInputFieldPositions;
		this.length         = -1;
		this.type           = Value.VALUE_TYPE_STRING;
		this.format         = "";
		this.trimtype       = XMLInputMeta.TYPE_TRIM_NONE;
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

	
	public Object clone()
	{
		try
		{
			XMLInputField retval = (XMLInputField) super.clone();
            XMLInputFieldPosition[] positions = new XMLInputFieldPosition[xmlInputFieldPositions.length];
            for (int i=0;i<xmlInputFieldPositions.length;i++)
            {
                positions[i] = (XMLInputFieldPosition)xmlInputFieldPositions[i].clone();
            }
            retval.setXmlInputFieldPositions(positions);
            
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
    public XMLInputFieldPosition[] getXmlInputFieldPositions()
    {
        return xmlInputFieldPositions;
    }

    /**
     * @param xmlInputFieldPositions The xmlInputFieldPositions to set.
     */
    public void setXmlInputFieldPositions(XMLInputFieldPosition[] xmlInputFieldPositions)
    {
        this.xmlInputFieldPositions = xmlInputFieldPositions;
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
		return XMLInputMeta.getTrimTypeDesc(trimtype);
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
    
    public String encodePosition()
    {
        String enc="";
        
        for (int i=0;i<xmlInputFieldPositions.length;i++)
        {
            XMLInputFieldPosition pos = xmlInputFieldPositions[i];
            if (i>0) enc+=", ";
            enc+=pos.toString();
        }
        
        return enc;
    }
	
	public void guess()
	{
	}
}
