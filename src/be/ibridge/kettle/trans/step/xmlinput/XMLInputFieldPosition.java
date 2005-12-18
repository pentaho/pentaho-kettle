package be.ibridge.kettle.trans.step.xmlinput;

import java.util.ArrayList;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.exception.KettleValueException;

public class XMLInputFieldPosition
{
    public static final int XML_ELEMENT   = 1;
    public static final int XML_ATTRIBUTE = 2;
    
    public static final String NR_MARKER = "/";
    
    private String name;
    private int    type;
    private int    elementNr;
    
    /**
     * Create a new XML Input Field position.
     * 
     * @param name the name of the element or attribute
     * @param type Element or Attribute (XML_ELEMENT, XML_ATTRIBUTE)
     */
    public XMLInputFieldPosition(String name, int type)
    {
        this.name      = name;
        this.type      = type;
        this.elementNr = 1;
    }
    
    /**
     * Create a new XML Input Field position.
     * 
     * @param name the name of the element or attribute
     * @param type Element or Attribute (XML_ELEMENT, XML_ATTRIBUTE)
     * @param elementNr the element number to pick.
     */
    public XMLInputFieldPosition(String name, int type, int elementNr)
    {
        this.name = name;
        this.type = type;
        this.elementNr = elementNr;
    }
    
    public String toString()
    {
        String enc="";
        
        if (type==XML_ELEMENT)
        {
            enc+="E=";
        }
        else
        {
            enc+="A=";
        }
        enc+=name;
        enc+=NR_MARKER+( elementNr<=0 ? 1 : elementNr );
        
        return enc;
    }
    
    /**
     * Construnct a new XMLFieldPosition based on an a code: E=Elementame, A=Attributename
     * @param encoded
     */
    public XMLInputFieldPosition(String encoded) throws KettleValueException
    {
        int equalIndex = encoded.indexOf("=");
        if (equalIndex<0)
        {
            throw new KettleValueException("Sorry, this is not a valid XML Field Position (no equal sign in code: '"+encoded+"')");
        }
        
        String positionType  = Const.trim( encoded.substring(0, equalIndex) );
        String nameAndNumber = Const.trim( encoded.substring(equalIndex+1) );
        String positionName = nameAndNumber;
        
        // Is there an element number?
        int semiIndex = nameAndNumber.indexOf(NR_MARKER);
        
        if (semiIndex>=0)
        {
            this.elementNr = Const.toInt( nameAndNumber.substring(semiIndex+1), 1 );  // Unreadable: default to 1
            positionName = nameAndNumber.substring(0, semiIndex );
        }
        else
        {
            this.elementNr = 1;
        }
        
        if (positionType.equalsIgnoreCase("E"))   // Element
        {
            this.type = XML_ELEMENT;
            this.name = positionName;
        }
        else
        if (positionType.equalsIgnoreCase("A"))   // Attribute
        {
            this.type = XML_ATTRIBUTE;
            this.name = positionName;
        }
        else
        {
            throw new KettleValueException("Sorry, the position type can either be E (element) or A (attribute), you specified "+positionType);
        }
        
        // Get the element nr
        
    }
    
    /**
     * @return Returns the name.
     */
    public String getName()
    {
        return name;
    }

    /**
     * @param name The name to set.
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * @return Returns the type.
     */
    public int getType()
    {
        return type;
    }

    /**
     * @param type The type to set.
     */
    public void setType(int type)
    {
        this.type = type;
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

    /**
     * @return Returns the elementNr.
     */
    public int getElementNr()
    {
        return elementNr;
    }

    /**
     * @param elementNr The elementNr to set.
     */
    public void setElementNr(int elementNr)
    {
        this.elementNr = elementNr;
    }
    
    /**
     * Encode the path to an XML element or attribute
     * @param path An ArrayList of XMLInputFieldPosition
     * @return the path encoded
     */
    public static final String encodePath(ArrayList path)
    {
        String encoded = "";
        for (int p=0;p<path.size();p++) 
        {
            XMLInputFieldPosition pos = (XMLInputFieldPosition)path.get(p); 
            String elementName = pos.toString();
            if (p>0) encoded+=XMLInputField.POSITION_MARKER;
            encoded+=elementName;
        }
        
        return encoded;
    }

}
