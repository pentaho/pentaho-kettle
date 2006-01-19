/***********************************************************************
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

 

package be.ibridge.kettle.core;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import be.ibridge.kettle.core.exception.KettleXMLException;
import be.ibridge.kettle.core.value.Value;

/**
 * This class contains a number of (static final) methods to facilitate 
 * the retreival of information from XML Node(s).
 * @author Matt
 * @since 04-04-2003
 *
 */
public class XMLHandler
{
	/**
	 * The header string to specify encoding in UTF-8 for XML files
	 * 
	 * @return The XML header.
	 */
	public static final String getXMLHeader()
	{
		return getXMLHeader(Const.XML_ENCODING);
	}

    /**
     * The header string to specify encoding in an XML file
     * @param encoding The desired encoding to use in the XML file
     * @return The XML header.
     */
    public static final String getXMLHeader(String encoding)
    {
        return "<?xml version=\"1.0\" encoding=\""+encoding+"\"?>"+Const.CR; // "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>"+Const.CR;
    }

	/**
	 * Get the value of a tag in a node
	 * @param n The node to look in
	 * @param tag The tag to look for
	 * @return The value of the tag or null if nothing was found.
	 */
	public static final String getTagValue(Node n, String tag)
	{
		NodeList children;
		Node childnode;
		
		if (n==null) return null;
		
		children=n.getChildNodes();
		for (int i=0;i<children.getLength();i++)
		{
			childnode=children.item(i);
			if (childnode.getNodeName().equalsIgnoreCase(tag))
			{
				if (childnode.getFirstChild()!=null) return childnode.getFirstChild().getNodeValue();		
			}
		}
		return null;
	}

	/**
	 * Search a node for a certain tag, in that subnode search for a certain subtag.
	 * Return the value of that subtag. 
	 * @param n The node to look in
	 * @param tag The tag to look for
	 * @param subtag The subtag to look for
	 * @return The string of the subtag or null if nothing was found.
	 */
	public static final String getTagValue(Node n, String tag, String subtag)
	{
		NodeList children, tags;
		Node childnode, tagnode;
		
		if (n==null) return null;
		
		// Haal alle <step> delen één voor één uit het document.
		//
		children=n.getChildNodes();
		for (int i=0;i<children.getLength();i++)
		{
			childnode=children.item(i);
			if (childnode.getNodeName().equalsIgnoreCase(tag))  // <file>
			{
				tags=childnode.getChildNodes();
				for (int j=0;j<tags.getLength();j++)
				{
					tagnode=tags.item(j);					
					if (tagnode.getNodeName().equalsIgnoreCase(subtag))
					{
						if (tagnode.getFirstChild()!=null)
						  return tagnode.getFirstChild().getNodeValue(); 
					}
				}
			}
		}
		return null;
	}
	
	/**
	 * Count nodes with a certain tag
	 * 
	 * @param n The node to look in
	 * @param tag The tags to count
	 * @return The number of nodes found with a certain tag
	 */
	public static final int countNodes(Node n, String tag)
	{
		NodeList children;
		Node childnode;
		
		int count=0;
		
		// Haal alle <hop>'s één voor één uit <order>
		//
		if (n==null) return 0;
		
		children=n.getChildNodes();
		for (int i=0;i<children.getLength();i++)
		{
			childnode=children.item(i);
			if (childnode.getNodeName().equalsIgnoreCase(tag))  // <file>
			{
				count++;
			}
		}
		return count;
	}

	/**
	 * Count nodes with a certain tag and where the subtag as a certain value.
	 * @param n The node to look in
	 * @param tag The tag to consider looking further in
	 * @param subtag The tag in the child to consider
	 * @param subtagvalue The value that the subtag should have
	 * @return The nr of sub-nodes found.
	 */
	public static final int countNodesWithTagValue(Node n, String tag, String subtag, String subtagvalue)
	{
		NodeList children;
		Node childnode, tagnode;
		String value;
		
		int count=0;
		
		// Haal alle <hop>'s één voor één uit <order>
		//
		children=n.getChildNodes();
		for (int i=0;i<children.getLength();i++)
		{
			childnode=children.item(i);
			if (childnode.getNodeName().equalsIgnoreCase(tag))  // <hop>
			{
				tagnode=getSubNode(childnode, subtag);
				if (tagnode!=null)
				{
					value=getNodeValue(tagnode);
					if (value!=null && value.equalsIgnoreCase(subtagvalue))
					{
						count++;
					}
				}
			}
		}
		return count;
	}

	/**
	 * Count nodes with a certain tag and where 2 tags are set as specified.
	 * 
	 * @param n The node to search in
	 * @param tag The tag to look for in the node
	 * @param subtag The subtag to look for in the children of the node
	 * @param subtagvalue The value the first subtag should have
	 * @param valtag The second subtag to look for in the children of the node
	 * @param valcont The value the second subtag should have
	 * @return The number of nodes found
	 */
	public static final int countNodesWithTagValueAndAnother(Node n, String tag, String subtag, String subtagvalue, String valtag, String valcont)
	{
		NodeList children;
		Node childnode, tagnode;
		String value;
		
		int count=0;
		
		// Haal alle <hop>'s één voor één uit <order>
		//
		children=n.getChildNodes();
		for (int i=0;i<children.getLength();i++)
		{
			childnode=children.item(i);
			if (childnode.getNodeName().equalsIgnoreCase(tag))  // <hop>
			{
				tagnode=getSubNode(childnode, subtag);
				if (tagnode!=null)
				{
					value=getNodeValue(tagnode);
					if (value!=null && value.equalsIgnoreCase(subtagvalue))
					{
						// Now also check another field of the same childnode 
						tagnode=getSubNode(childnode, valtag);
						if (tagnode!=null)
						{
							value=getNodeValue(tagnode);
							if (value!=null && value.equalsIgnoreCase(valcont))
							{
								count++;
							}
						}
					}
				}
			}
		}
		return count;
	}


	/**
	 * Get node child with a certain subtag set to a certain value
	 * @param n The node to search in
	 * @param tag The tag to look for
	 * @param subtag The subtag to look for
	 * @param subtagvalue The value the subtag should have
	 * @param nr The nr of occurance of the value
	 * @return The node found or null if we couldn't find anything.
	 */
	public static final Node getNodeWithTagValue(Node n, String tag, String subtag, String subtagvalue, int nr)
	{
		NodeList children;
		Node childnode, tagnode;
		String value;
		
		int count=0;
		
		// Haal alle <hop>'s één voor één uit <order>
		//
		children=n.getChildNodes();
		for (int i=0;i<children.getLength();i++)
		{
			childnode=children.item(i);
			if (childnode.getNodeName().equalsIgnoreCase(tag))  // <hop>
			{
				tagnode=getSubNode(childnode, subtag);
				value=getNodeValue(tagnode);
				if (value.equalsIgnoreCase(subtagvalue))
				{
					if (count==nr) return childnode;
					count++;
				}
			}
		}
		return null;
	}

	/**
	 * Get the n'th node with a certain tag and where 2 tags are set as specified.
	 * 
	 * @param n The node to search in
	 * @param tag The tag to look for in the node
	 * @param subtag The subtag to look for in the children of the node
	 * @param subtagvalue The value the first subtag should have
	 * @param nr The position of the subnode
	 * @param valtag The second subtag to look for in the children of the node
	 * @param valcont The value the second subtag should have
	 * @return The number of nodes found
	 */
	public static final Node getNodeWithTagValueAndAnother(Node n, String tag, String subtag, String subtagvalue, int nr, String valtag, String valcont)
	{
		NodeList children;
		Node childnode, tagnode;
		String value;
		
		int count=0;
		
		// Haal alle <hop>'s één voor één uit <order>
		//
		children=n.getChildNodes();
		for (int i=0;i<children.getLength();i++)
		{
			childnode=children.item(i);
			if (childnode.getNodeName().equalsIgnoreCase(tag))  // <hop>
			{
				tagnode=getSubNode(childnode, subtag);
				value=getNodeValue(tagnode);
				if (value!=null && value.equalsIgnoreCase(subtagvalue))
				{
					// Now also check another field of the same childnode 
					tagnode=getSubNode(childnode, valtag);
					if (tagnode!=null)
					{
						value=getNodeValue(tagnode);
						if (value!=null && value.equalsIgnoreCase(valcont))
						{
							if (count==nr) return childnode;
							count++;
						}
					}
				}
			}
		}
		return null;
	}

	/**
	 * Search for a subnode in the node with a certain tag.
	 * @param n The node to look in
	 * @param tag The tag to look for
	 * @return The subnode if the tag was found, or null if nothing was found.
	 */
	public static final Node getSubNode(Node n, String tag)
	{
		int i;
		NodeList children;
		Node childnode;
		
		if (n==null) return null;
		
		// Get the childres one by one out of the node, 
		// compare the tags and return the first found.
		//
		children=n.getChildNodes();
		for (i=0;i<children.getLength();i++)
		{
			childnode=children.item(i);
			if (childnode.getNodeName().equalsIgnoreCase(tag))
			{
				return childnode;
			}
		}
		return null;
	}
	
	/**
	 * Search a node for a child of child
	 * 
	 * @param n The node to look in
	 * @param tag The tag to look for in the node
	 * @param subtag The tag to look for in the children of the node
	 * @return The sub-node found or null if nothing was found.
	 */
	public static final Node getSubNode(Node n, String tag, String subtag)
	{
		Node t = getSubNode(n, tag);
		if (t!=null) return getSubNode(t, subtag);
		return null; 
	}

	/**
	 * Get a subnode in a node by nr.
	 * 
	 * @param n The node to look in
	 * @param tag The tag to count
	 * @param nr The position in the node
	 * @return The subnode found or null in case the position was invalid.
	 */
	public static final Node getSubNodeByNr(Node n, String tag, int nr)
	{
		NodeList children;
		Node childnode;
		
		if (n==null) return null;
		
		int count=0;
		// Find the child-nodes of this Node n:
		children=n.getChildNodes();
		for (int i=0;i<children.getLength();i++)  // Try all children
		{
			childnode=children.item(i);
			if (childnode.getNodeName().equalsIgnoreCase(tag))  // We found the right tag
			{
				if (count==nr)
				{
					return childnode;
				}
				count++;
			}
		}
		return null;
	}
	
	/**
	 * In a node, find the direct subnode where a given tag has a value 
	 * 
	 * @param n The node to search in
	 * @param tag The tag to look at
	 * @param tagvalue The tag value to search for
	 * @return The node found or null in case we don't find anything.
	 */
	public static final Node getSubNodeByValue(Node n, String tag, String tagvalue)
	{
		int i;
		NodeList children;
		Node childnode;
		String retval;
		
		if (n==null) return null;
		
		// Find the child-nodes of this Node n:
		children=n.getChildNodes();
		for (i=0;i<children.getLength();i++)  // Try all children
		{
			childnode=children.item(i);
			if (childnode.getNodeName().equalsIgnoreCase(tag))  // We found the right tag
			{
				retval=childnode.getNodeValue();
				if (retval.equalsIgnoreCase(tagvalue))
				{
					return childnode;
				}
			}
		}
		return null;
	}

	/**
	 * Find the value entry in a node
	 * @param n The node
	 * @return The value entry as a string
	 */
	public static final String getNodeValue(Node n)
	{
		int i;
		NodeList children;
		Node childnode;
		String retval;
		
		if (n==null) return null;
		
		// Find the child-nodes of this Node n:
		children=n.getChildNodes();
		for (i=0;i<children.getLength();i++)  // Try all children
		{
			childnode=children.item(i);
			retval=childnode.getNodeValue();
			if (retval!=null)  // We found the right value
			{
				return retval;
			}
		}
		return null;
	}
	
	public static final String getTagAttribute(Node node, String attribute)
	{
		String retval = null;

		NamedNodeMap nnm = node.getAttributes();
		if (nnm!=null)
		{
			Node attr   = nnm.getNamedItem(attribute);
			if (attr!=null)
			{
				retval = attr.getNodeValue();
			}
		}
		return retval;
	}

	/**
	 * Load a file into an XML document
	 * @param filename The filename to load into a document
	 * @return the Document if all went well, null if an error occured!
	 */
	public static final Document loadXMLFile(String filename) throws KettleXMLException
	{
	    LogWriter log = LogWriter.getInstance();

		DocumentBuilderFactory dbf;
		DocumentBuilder db;
		Document doc;
		
		try
		{			
			// Check and open XML document
			dbf  = DocumentBuilderFactory.newInstance();
			db   = dbf.newDocumentBuilder();
			try
			{
				doc  = db.parse(new File(filename));
			}
			catch(FileNotFoundException ef)
			{
				log.logError(getString(), "Error opening file: "+filename+" : "+ef.toString());
				return null;
			}
				
			return doc;
		}
		catch(Exception e)
		{
			throw new KettleXMLException("Error reading information from file", e);
		}
	}

	/**
	 * Load a String into an XML document
	 * @param string The XML text to load into a document
	 * @return the Document if all went well, null if an error occured!
	 */
	public static final Document loadXMLString(String string) throws KettleXMLException
	{
		DocumentBuilderFactory dbf;
		DocumentBuilder db;
		Document doc;
		
		try
		{			
			// Check and open XML document
			dbf  = DocumentBuilderFactory.newInstance();
			db   = dbf.newDocumentBuilder();
			try
			{
				doc  = db.parse(new InputSource(new java.io.StringReader(string)));
			}
			catch(FileNotFoundException ef)
			{
				throw new KettleXMLException("Error parsing XML", ef);
			}
				
			return doc;
		}
		catch(Exception e)
		{
			throw new KettleXMLException("Error reading information from XML string : ", e);
		}
	}

	public static final String getString()
	{
		return XMLHandler.class.getName();
	}

	/**
	 * Build an XML string for a certain tag String value
	 * @param tag The XML tag
	 * @param val The String value of the tag
	 * @param cr true if a cariage return is desired after the ending tag.
	 * @return The XML String for the tag.
	 */
	public static final String addTagValue(String tag, String val, boolean cr)
	{
		StringBuffer value;
		
		if (val!=null)
		{
			value = new StringBuffer(val);
			Const.repl(value, "&", "&amp;");
			Const.repl(value, "'", "&apos;");
			Const.repl(value, "<", "&lt;");
			Const.repl(value, ">", "&gt;");
			Const.repl(value, "\"", "&quot;");
		}
		else
		{
			value = new StringBuffer();
		}
	
		return "<"+tag+">"+value+"</"+tag+">"+(cr?Const.CR:"");
	}

	/**
	 * Build an XML string (including a cariage return) for a certain tag String value
	 * @param tag The XML tag
	 * @param val The String value of the tag
	 * @return The XML String for the tag.
	 */
	public static final String addTagValue(String tag, String val)
	{
		return addTagValue(tag, val, true);
	}

	/**
	 * Build an XML string (including a cariage return) for a certain tag boolean value
	 * @param tag The XML tag
	 * @param bool The boolean value of the tag
	 * @return The XML String for the tag.
	 */
	public static final String addTagValue(String tag, boolean bool)
	{
		return addTagValue(tag, bool, true);
	}

	/**
	 * Build an XML string for a certain tag boolean value
	 * @param tag The XML tag
	 * @param bool The boolean value of the tag
	 * @param cr true if a cariage return is desired after the ending tag.
	 * @return The XML String for the tag.
	 */
	public static final String addTagValue(String tag, boolean bool, boolean cr)
	{
		return addTagValue(tag, bool?"Y":"N", cr);
	}

	/**
	 * Build an XML string for a certain tag long integer value
	 * @param tag The XML tag
	 * @param l The long integer value of the tag
	 * @return The XML String for the tag.
	 */
	public static final String addTagValue(String tag, long l)
	{
		return addTagValue(tag, l, true);
	}

	/**
	 * Build an XML string for a certain tag long integer value
	 * @param tag The XML tag
	 * @param l The long integer value of the tag
	 * @param cr true if a cariage return is desired after the ending tag.
	 * @return The XML String for the tag.
	 */
	public static final String addTagValue(String tag, long l, boolean cr)
	{
		return addTagValue(tag, ""+l, cr);
	}

	/**
	 * Build an XML string (with cariage return) for a certain tag integer value
	 * @param tag The XML tag
	 * @param i The integer value of the tag
	 * @return The XML String for the tag.
	 */
	public static final String addTagValue(String tag, int i)
	{
		return addTagValue(tag, i, true);
	}

	/**
	 * Build an XML string for a certain tag integer value
	 * @param tag The XML tag
	 * @param i The integer value of the tag
	 * @param cr true if a cariage return is desired after the ending tag.
	 * @return The XML String for the tag.
	 */
	public static final String addTagValue(String tag, int i, boolean cr)
	{
		return addTagValue(tag, ""+i, cr);
	}

	/**
	 * Build an XML string (with cariage return) for a certain tag double value
	 * @param tag The XML tag
	 * @param d The double value of the tag
	 * @return The XML String for the tag.
	 */
	public static final String addTagValue(String tag, double d)
	{
		return addTagValue(tag, d, true);
	}

	/**
	 * Build an XML string for a certain tag double value
	 * @param tag The XML tag
	 * @param d The double value of the tag
	 * @param cr true if a cariage return is desired after the ending tag.
	 * @return The XML String for the tag.
	 */
	public static final String addTagValue(String tag, double d, boolean cr)
	{
		return addTagValue(tag, ""+d, cr);
	}

	/**
	 * Build an XML string (with cariage return) for a certain tag Date value
	 * @param tag The XML tag
	 * @param date The Date value of the tag
	 * @return The XML String for the tag.
	 */
	public static final String addTagValue(String tag, Date date)
	{
		return addTagValue(tag, date, true);
	}

	/**
	 * Build an XML string for a certain tag Date value
	 * @param tag The XML tag
	 * @param date The Date value of the tag
	 * @param cr true if a cariage return is desired after the ending tag.
	 * @return The XML String for the tag.
	 */
	public static final String addTagValue(String tag, Date date, boolean cr)
	{
		if (date==null) addTagValue(tag, (String)null, cr);
		Value v = new Value("date", date);
		return addTagValue(tag, v.getString(), cr);
	}

    /**
     * Get all the attributes in a certain node (on the root level)
     * @param node The node to examine
     * @return an array of strings containing the names of the attributes.
     */
    public static String[] getNodeAttributes(Node node)
    {
        NamedNodeMap nnm = node.getAttributes();
        if (nnm!=null)
        {
            String attributes[] = new String[nnm.getLength()];
            for (int i=0;i<nnm.getLength();i++)
            {
                Node attr   = nnm.item(i);
                attributes[i] = attr.getNodeName();
            }
            return attributes;
        }
        return null;

    }

    public static String[] getNodeElements(Node node)
    {
        ArrayList elements = new ArrayList(); // List of String 
        
        NodeList nodeList = node.getChildNodes();
        if (nodeList==null) return null;
        
        for (int i=0;i<nodeList.getLength();i++)
        {
            String nodeName = nodeList.item(i).getNodeName();
            if (elements.indexOf(nodeName)<0) elements.add(nodeName); 
        }
        
        if (elements.size()==0) return null;
        
        return (String[])elements.toArray(new String[elements.size()]);
    }

}
	