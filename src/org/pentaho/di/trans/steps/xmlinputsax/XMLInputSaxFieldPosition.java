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

import java.util.List;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleValueException;



public class XMLInputSaxFieldPosition
{
    public static final int XML_ELEMENT_POS   = 1;
    public static final int XML_ELEMENT_ATT   = 2;
    public static final int XML_ATTRIBUTE = 3;
    
    public static final String NR_MARKER = "/";
    public static final String ATT_MARKER = ":";
    
    private String name;
    /**XML attribute that define element*/
    private String attribute; 
    /**XML attribute value that define the field position*/
    private String value;
    private int    type;
    private int    elementNr;
    
    /**
     * Create a new XML Input Field position.
     * 
     * @param name the name of the element or attribute
     * @param type Element or Attribute (XML_ELEMENT, XML_ATTRIBUTE)
     */
    public XMLInputSaxFieldPosition(String name, int type) throws KettleValueException
    {
    	if(type!=XML_ELEMENT_ATT)
    	{
    		this.name = name;
    		this.type = type;
    		this.elementNr = 1;
    	}
    	else
    	{
    		throw new KettleValueException("This constructor is for position defined attributes or elements");
    	}
    }
    
    /**
     * Create a new XML Input Field position.
     * 
     * @param name the name of the element or attribute
     * @param type Element or Attribute (XML_ELEMENT, XML_ATTRIBUTE)
     * @param elementNr the element number to pick.
     */
    public XMLInputSaxFieldPosition(String name, int type, int elementNr) throws KettleValueException
    {
    	if(type!=XML_ELEMENT_ATT)
    	{
    		this.name = name;
    		this.type = type;
    		this.elementNr = elementNr;
    	}
    	else
    	{
    		throw new KettleValueException("This constructor is for position defined attributes or elements");
    	}
    }
    
    public XMLInputSaxFieldPosition(String name, String attribute, String value)
    {
        this.name = name;
        this.type = XML_ELEMENT_ATT;
        this.attribute = attribute;
        this.value = value;
    }
    
    public String toString()
    {
        String enc="";
        
        if (type==XML_ATTRIBUTE)
        {
            enc+="A=";
            enc+=name;
            enc+=NR_MARKER+( elementNr<=0 ? 1 : elementNr );
        }
        else
        {
            
            if (type==XML_ELEMENT_ATT){
            	enc+="Ea=";
            	enc+=name;
                enc+=NR_MARKER+attribute+ATT_MARKER+value;
            }
            else
            {
            	enc+="Ep=";
            	enc+=name;
                enc+=NR_MARKER+( elementNr<=0 ? 1 : elementNr );
            }
        }
        
        return enc;
    }
    
    /**
     * Construnct a new XMLFieldPosition based on an a code: E=Elementame, A=Attributename
     * @param encoded
     */
    public XMLInputSaxFieldPosition(String encoded) throws KettleValueException
    {
        int equalIndex = encoded.indexOf('=');
        if (equalIndex<0)
        {
            throw new KettleValueException("Sorry, this is not a valid XML Field Position (no equal sign in code: '"+encoded+"')");
        }
        
        String positionType  = Const.trim( encoded.substring(0, equalIndex) );
        String nameAndNumber = Const.trim( encoded.substring(equalIndex+1) );
        String positionName = nameAndNumber;
        
        // Is there an element number?
        int semiIndex = nameAndNumber.indexOf(NR_MARKER);
        
        // Is there an element defining attribute
        int semiIndex2 = nameAndNumber.indexOf(ATT_MARKER);
        
        if (positionType.equalsIgnoreCase("Ea"))   // Element
        {
            this.type = XML_ELEMENT_ATT;
            //this.name = positionName;
        }
        else
        if (positionType.equalsIgnoreCase("Ep"))
        	{
        	this.type = XML_ELEMENT_POS;
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
            throw new KettleValueException("Sorry, the position type can either be Ep (element defined by position) or Ea (element defined by an attribute value) or A (attribute), you specified "+positionType);
        }
        
        
        if(this.type==XML_ELEMENT_ATT)
        {
        	if(semiIndex2>=0){
        		this.attribute=nameAndNumber.substring( semiIndex+1, semiIndex2 );
        		this.value=nameAndNumber.substring( semiIndex2+1 );
        		this.name = nameAndNumber.substring( 0, semiIndex );
        	}
        	else
        	{
        		throw new KettleValueException("Sorry, when the position type is Ea, defining attibute and its value must be specified : Ea=element/attribute:value"+"("+semiIndex2+")");
        	}
        }
        else
        {
        	if (semiIndex>=0)
        	{
        		this.elementNr = Const.toInt( nameAndNumber.substring(semiIndex+1), 1 );  // Unreadable: default to 1
        		this.name = nameAndNumber.substring(0, semiIndex );
        	}
        	else
        	{
        		this.elementNr = 1;
        	}
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
    
    public String getAttribute(){
    	return attribute;
    }
    
    public String getAttributeValue(){
    	return value;
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
    public static final String encodePath(List<XMLInputSaxFieldPosition> path)
    {
        String encoded = "";
        for (int p=0;p<path.size();p++) 
        {
            XMLInputSaxFieldPosition pos = (XMLInputSaxFieldPosition)path.get(p); 
            String elementName = pos.toString();
            if (p>0) encoded+=XMLInputSaxField.POSITION_MARKER;
            encoded+=elementName;
        }
        
        return encoded;
    }

}
