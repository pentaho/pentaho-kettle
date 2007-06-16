package be.ibridge.kettle.core;

import org.w3c.dom.Node;

/**
 * This is an entry in an XMLHandlerCache
 * 
 * @author Matt
 * @since  22-Apr-2006
 */
public class XMLHandlerCacheEntry
{
    private Node parentNode;
    private String tag;

    /**
     * @param parentNode The parent node
     * @param tag The tag
     */
    public XMLHandlerCacheEntry(Node parentNode, String tag)
    {
        this.parentNode = parentNode;
        this.tag = tag;
    }

    /**
     * @return Returns the parentNode.
     */
    public Node getParentNode()
    {
        return parentNode;
    }

    /**
     * @param parentNode The parentNode to set.
     */
    public void setParentNode(Node parentNode)
    {
        this.parentNode = parentNode;
    }

    /**
     * @return Returns the tag.
     */
    public String getTag()
    {
        return tag;
    }

    /**
     * @param tag The tag to set.
     */
    public void setTag(String tag)
    {
        this.tag = tag;
    }

    public boolean equals(Object object)
    {
        XMLHandlerCacheEntry entry = (XMLHandlerCacheEntry) object;
        
        return parentNode.equals(entry.getParentNode()) && tag.equals(entry.getTag());
    }
    
    public int hashCode()
    {
        return parentNode.hashCode() ^ tag.hashCode();
    }
    
}

