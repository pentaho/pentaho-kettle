package org.pentaho.di.trans.steps.accessinput;

import java.util.ArrayList;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleValueException;

public class AccessInputFieldPosition implements Cloneable
{
    public static final int XML_ELEMENT   = 1;
    public static final int XML_ATTRIBUTE = 2;
    public static final int XML_ROOT      = 3;
    
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
    public AccessInputFieldPosition(String name, int type)
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
    public AccessInputFieldPosition(String name, int type, int elementNr)
    {
        this.name = name;
        this.type = type;
        this.elementNr = elementNr;
    }
    
    public String toString()
    {
        String enc="";
        
        switch(type)
        {
        case XML_ELEMENT:   enc+="E"; break;
        case XML_ATTRIBUTE: enc+="A"; break;
        case XML_ROOT:      enc+="R"; break;
        default:            enc+="?"; break;
        }
        enc+="=";
        enc+=name;
        enc+=NR_MARKER+( elementNr<=0 ? 1 : elementNr );
        
        return enc;
    }
    
    /**
     * Construnct a new XMLFieldPosition based on an a code: E=Elementame, A=Attributename
     * @param encoded
     */
    public AccessInputFieldPosition(String encoded) throws KettleValueException
    {
        int equalIndex = encoded.indexOf("=");
        if (equalIndex<0)
        {
            throw new KettleValueException(Messages.getString("AccessInput.Exception.InvalidXMLFieldPosition", encoded));
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
        if (positionType.equalsIgnoreCase("R"))   // Root of the repeating element.  There is only one
        {
            this.type = XML_ROOT;
            this.name = positionName;
        }
        else
        {
            throw new KettleValueException(Messages.getString("AccessInput.Exception.WrongPositionType", positionType));
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
     * @param path An ArrayList of AccessInput
     * @return the path encoded
     */
    public static final String encodePath(ArrayList path)
    {
        String encoded = "";
        for (int p=0;p<path.size();p++) 
        {
            AccessInput pos = (AccessInput)path.get(p); 
            String elementName = pos.toString();
            if (p>0) encoded+=AccessInputField.POSITION_MARKER;
            encoded+=elementName;
        }
        
        return encoded;
    }
}