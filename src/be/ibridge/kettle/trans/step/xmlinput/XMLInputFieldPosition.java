package be.ibridge.kettle.trans.step.xmlinput;

public class XMLInputFieldPosition
{
    public static final int XML_ELEMENT   = 1;
    public static final int XML_ATTRIBUTE = 2;
    
    private String name;
    private int    type;
    
    /**
     * Create a new XML Input Field position.
     * 
     * @param name the name of the element or attribute
     * @param type Element or Attribute (XML_ELEMENT, XML_ATTRIBUTE)
     */
    public XMLInputFieldPosition(String name, int type)
    {
        this.name = name;
        this.type = type;
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
        
        return enc;
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

}
