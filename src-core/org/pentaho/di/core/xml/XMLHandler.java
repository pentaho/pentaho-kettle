/* * Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.*/

 

package org.pentaho.di.core.xml;
import java.io.ByteArrayInputStream;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;


import org.apache.commons.codec.binary.Base64;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.vfs.KettleVFS;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;

/**
 * This class contains a number of (static final) methods to facilitate 
 * the retreival of information from XML Node(s).
 * @author Matt
 * @since 04-04-2003
 *
 */
public class XMLHandler
{
    private static XMLHandlerCache cache = XMLHandlerCache.getInstance();
    private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat(ValueMeta.DEFAULT_DATE_FORMAT_MASK); 
    
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
        return "<?xml version=\"1.0\" encoding=\""+encoding+"\"?>"+Const.CR;
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
	 * Get the value of a tag in a node
	 * @param n The node to look in
	 * @param tag The tag to look for
	 * @return The value of the tag or null if nothing was found.
	 */
	public static final String getTagValueWithAttribute(Node n, String tag,String attribute)
	{
		NodeList children;
		Node childnode;
		
		if (n==null) return null;
		
		children=n.getChildNodes();
		for (int i=0;i<children.getLength();i++)
		{
			childnode=children.item(i);
			if (childnode.getNodeName().equalsIgnoreCase(tag) && childnode.getAttributes().getNamedItem(attribute)!=null)
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
	 * Get nodes with a certain tag one level down
	 * 
	 * @param n The node to look in
	 * @param tag The tags to count
	 * @return The list of nodes found with the specified tag
	 */
	public static final List<Node> getNodes(Node n, String tag)
	{
		NodeList children;
		Node childnode;
		
		List<Node> nodes = new ArrayList<Node>();
		
		if (n==null) return nodes;
		
		children=n.getChildNodes();
		for (int i=0;i<children.getLength();i++)
		{
			childnode=children.item(i);
			if (childnode.getNodeName().equalsIgnoreCase(tag))  // <file>
			{
				nodes.add(childnode);
			}
		}
		return nodes;
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
	 * Get node child with a certain subtag set to a certain value
	 * @param n The node to search in
	 * @param tag The tag to look for
	 * @param subtag The subtag to look for
	 * @param subtagvalue The value the subtag should have
	 * @param nr The nr of occurance of the value
	 * @return The node found or null if we couldn't find anything.
	 */
	public static final Node getNodeWithAttributeValue(Node n, String tag, String attributeName,String attributeValue)
	{
		NodeList children;
		Node childnode;
		
		children=n.getChildNodes();
		for (int i=0;i<children.getLength();i++)
		{
			childnode=children.item(i);
			if (childnode.getNodeName().equalsIgnoreCase(tag))  // <hop>
			{
				Node attribute = childnode.getAttributes().getNamedItem(attributeName);
				
				if (attribute!=null && attributeValue.equals(attribute.getTextContent()))
					return childnode;
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
     * Get a subnode in a node by nr.<br>
     * This method uses caching and assumes you loop over subnodes in sequential order (nr is increasing by 1 each call)
     * 
     * @param n The node to look in
     * @param tag The tag to count
     * @param nr The position in the node
     * @return The subnode found or null in case the position was invalid.
     */
    public static final Node getSubNodeByNr(Node n, String tag, int nr)
    {
        return getSubNodeByNr(n, tag, nr, true);
    }
    
	/**
	 * Get a subnode in a node by nr.<br>  
     * It optially allows you to use caching.<br>
     * Caching assumes that you loop over subnodes in sequential order (nr is increasing by 1 each call)
	 * 
	 * @param n The node to look in
	 * @param tag The tag to count
	 * @param nr The position in the node
     * @param useCache set this to false if you don't want to use caching. For example in cases where you want to loop over subnodes of a certain tag in reverse or random order.
	 * @return The subnode found or null in case the position was invalid.
	 */
	public static final Node getSubNodeByNr(Node n, String tag, int nr, boolean useCache)
	{
		NodeList children;
		Node childnode;
		
		if (n==null) return null;
		
		int count=0;
		// Find the child-nodes of this Node n:
		children=n.getChildNodes();
        
        int lastChildNr=-1;
        XMLHandlerCacheEntry entry = null;
        
        if (useCache)
        {
            entry = new XMLHandlerCacheEntry(n, tag);
            lastChildNr = cache.getLastChildNr(entry);
        }
        if (lastChildNr<0)
        {
            lastChildNr=0;
        }
        else
        {
            count=nr; // we assume we found the previous nr-1 at the lastChildNr
            lastChildNr++; // we left off at the previouso one, so continue with the next.
        }
        
		for (int i=lastChildNr;i<children.getLength();i++)  // Try all children
		{
			childnode=children.item(i);
			if (childnode.getNodeName().equalsIgnoreCase(tag))  // We found the right tag
			{
				if (count==nr)
				{
                    if (useCache) cache.storeCache(entry, i);
					return childnode;
				}
				count++;
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
		if (n==null) return null;
		
		// Find the child-nodes of this Node n:
        NodeList children=n.getChildNodes();
		for (int i=0;i<children.getLength();i++)  // Try all children
		{
            Node childnode=children.item(i);
			String retval=childnode.getNodeValue();
			if (retval!=null)  // We found the right value
			{
				return retval;
			}
		}
		return null;
	}
	
	public static final String getTagAttribute(Node node, String attribute)
	{
		if (node==null) return null;
		
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
     * @return the Document if all went well, null if an error occurred!
     */
    public static final Document loadXMLFile(String filename) throws KettleXMLException
    {
        try
        {
            return loadXMLFile(KettleVFS.getFileObject(filename));
        }
        catch(Exception e)
        {
            throw new KettleXMLException(e);
        }
    }

    /**
     * Load a file into an XML document
     * @param filename The filename to load into a document
     * @return the Document if all went well, null if an error occured!
     */
    public static final Document loadXMLFile(FileObject fileObject) throws KettleXMLException
    {
    	return loadXMLFile(fileObject, null, false, false);
    }

    /**
     * Load a file into an XML document
     * @param filename The filename to load into a document
     * @param systemId Provide a base for resolving relative URIs.
     * @param ignoreEntities Ignores external entities and returns an empty dummy.
     * @param namespaceAware support XML namespaces.
     * @return the Document if all went well, null if an error occured!
     */
    public static final Document loadXMLFile(FileObject fileObject, String systemID, boolean ignoreEntities, boolean namespaceAware) throws KettleXMLException
    {
    	try {
			return loadXMLFile(KettleVFS.getInputStream(fileObject), systemID, ignoreEntities, namespaceAware);
		} catch (FileSystemException e) {
			throw new KettleXMLException("Unable to read file ["+fileObject.toString()+"]", e);
		}
    }

    /**
     * Read in an XML file from the passed input stream and return an XML document
     * @param inputStream The filename input stream to read the document from
     * @return the Document if all went well, null if an error occurred!
     */
    public static final Document loadXMLFile(InputStream inputStream) throws KettleXMLException
    {
        return loadXMLFile(inputStream, null, false, false);
    }

    
    /**
     * Load a file into an XML document
     * @param inputStream The stream to load a document from
     * @param systemId Provide a base for resolving relative URIs.
     * @param ignoreEntities Ignores external entities and returns an empty dummy.
     * @param namespaceAware support XML namespaces.
     * @return the Document if all went well, null if an error occured!
     */
    public static final Document loadXMLFile(InputStream inputStream, String systemID, boolean ignoreEntities, boolean namespaceAware) throws KettleXMLException
    {
        DocumentBuilderFactory dbf;
        DocumentBuilder db;
        Document doc;
        
        try
        {           
            // Check and open XML document
        	//
            dbf  = DocumentBuilderFactory.newInstance();
            dbf.setIgnoringComments(true);
            dbf.setNamespaceAware(namespaceAware);
            db   = dbf.newDocumentBuilder();
            
            // even dbf.setValidating(false) will the parser NOT prevent from checking the existance of the DTD
            // thus we need to give the BaseURI (systemID) below to have a chance to get it
            // or return empty dummy documents for all external entities (sources)
            //
            if (ignoreEntities) { 
            	db.setEntityResolver(new DTDIgnoringEntityResolver()); 
            }
            
            try
            {
            	if (Const.isEmpty(systemID)) {
            		// Normal parsing
            		//
            		doc  = db.parse(inputStream);
            	} else {
            		// Do extra verifications
            		//
            		String systemIDwithEndingSlash=systemID.trim();
            		
            		//make sure we have an ending slash, otherwise the last part will be ignored
            		//
            		if (!systemIDwithEndingSlash.endsWith("/") && !systemIDwithEndingSlash.endsWith("\\")) {
            			systemIDwithEndingSlash=systemIDwithEndingSlash.concat("/");
            		}
            		doc  = db.parse(inputStream,systemIDwithEndingSlash);
            	}
            }
            catch(FileNotFoundException ef)
            {
                throw new KettleXMLException(ef);
            }
            finally
            {
            	if (inputStream!=null) inputStream.close();
            }
                
            return doc;
        }
        catch(Exception e)
        {
            throw new KettleXMLException("Error reading information from input stream", e);
        }
    }
    
    public static final Document loadXMLFile(File resource) throws KettleXMLException
	{
    	try
    	{
    		return loadXMLFile(resource.toURI().toURL());
    	}
    	catch(MalformedURLException e)
    	{
    		throw new KettleXMLException(e);
    	}
	}
    
	/**
	 * Load a file into an XML document
	 * @param file The file to load into a document
	 * @return the Document if all went well, null if an error occured!
	 */
	public static final Document loadXMLFile(URL resource) throws KettleXMLException
	{
	    DocumentBuilderFactory dbf;
		DocumentBuilder db;
		Document doc;
		
		try
		{			
			// Check and open XML document
			dbf  = DocumentBuilderFactory.newInstance();
			db   = dbf.newDocumentBuilder();
			InputStream inputStream = resource.openStream();
			try
			{
				doc  = db.parse(inputStream);
			}
			catch(IOException ef)
			{
				throw new KettleXMLException(ef);
			}
			finally
			{
				inputStream.close();
			}
				
			return doc;
		}
		catch(Exception e)
		{
			throw new KettleXMLException("Error reading information from resource", e);
		}
	}

	/**
	 * Load a String into an XML document
	 * @param string The XML text to load into a document
	 * @return the Document if all went well, null if an error occurred!
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
			StringReader stringReader = new java.io.StringReader(string);
			InputSource inputSource = new InputSource(stringReader);
			try
			{
				doc  = db.parse(inputSource);
			}
			catch(IOException ef)
			{
				throw new KettleXMLException("Error parsing XML", ef);
			}
			finally
			{
				stringReader.close();
			}
				
			return doc;
		}
		catch(Exception e)
		{
			throw new KettleXMLException("Error reading information from XML string : "+Const.CR+string, e);
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
	 * @param cr true if a carriage return is desired after the ending tag.
	 * @return The XML String for the tag.
	 */
	public static final String addTagValue(String tag, String val, boolean cr,String... attributes)
	{
		StringBuffer value;
        
        if (val!=null && val.length()>0)
		{
            value = new StringBuffer("<");
            value.append(tag);
            
            for (int i=0;i<attributes.length;i+=2)
        	   value.append(" ").append(attributes[i]).append("=\"").append(attributes[i+1]).append("\" ");
           
            value.append('>');
            
            appendReplacedChars(value, val);
            
            value.append("</");
            value.append(tag);
            value.append('>');
		}
		else
		{
			value = new StringBuffer("<");
            value.append(tag);
            
            for (int i=0;i<attributes.length;i+=2)
         	   value.append(" ").append(attributes[i]).append("=\"").append(attributes[i+1]).append("\" ");
            
            value.append("/>");
		}
        
        if (cr)
        {
            value.append(Const.CR);
        }
        
		return value.toString();
	}

    /**
     * Take the characters from string val and append them to the value stringbuffer
     * In case a character is not allowed in XML, we convert it to an XML code
     * 
     * @param value the stringbuffer to append to
     * @param string the string to "encode"
     */
	public static void appendReplacedChars(StringBuffer value, String string)
    {
        // If it's a CDATA content block, leave those parts alone.
        //
        boolean isCDATA = string.startsWith("<![CDATA[") && string.endsWith("]]>");
        
        for (int i=0;i<string.length();i++)
        {
            char c = string.charAt(i);
            switch(c)
            {
            case '&'  : value.append("&amp;"); break;
            case '\'' : value.append("&apos;"); break;
            case '<'  :
                if (i!=0 || !isCDATA)
                {
                    value.append("&lt;"); 
                }
                else
                {
                    value.append(c);
                }
                break;
            case '>'  :
                if (i!=string.length()-1 || !isCDATA)
                {
                    value.append("&gt;"); 
                }
                else
                {
                    value.append(c);
                }
                break;
            case '"'  : value.append("&quot;"); break;
            case '/'  :
                if (isCDATA) // Don't replace slashes in a CDATA block, it's just not right.
                {
                    value.append(c);
                }
                else
                {
                    value.append("&#47;"); 
                }
                break;
            case 0x1A : value.append("{ILLEGAL XML CHARACTER 0x1A}"); break;
            default: 
                value.append(c);
            }
        }        
    }

    /**
	 * Build an XML string (including a carriage return) for a certain tag String value
	 * @param tag The XML tag
	 * @param val The String value of the tag
	 * @return The XML String for the tag.
	 */
	public static final String addTagValue(String tag, String val)
	{
		return addTagValue(tag, val, true);
	}
	
	

	/**
	 * Build an XML string (including a carriage return) for a certain tag boolean value
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
	 * @param cr true if a carriage return is desired after the ending tag.
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
	 * @param cr true if a carriage return is desired after the ending tag.
	 * @return The XML String for the tag.
	 */
	public static final String addTagValue(String tag, long l, boolean cr)
	{
		//Tom modified this for performance
//		return addTagValue(tag, ""+l, cr);
		return addTagValue(tag, String.valueOf(l), cr);
	}

	/**
	 * Build an XML string (with carriage return) for a certain tag integer value
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
	 * @param cr true if a carriage return is desired after the ending tag.
	 * @return The XML String for the tag.
	 */
	public static final String addTagValue(String tag, int i, boolean cr)
	{
		return addTagValue(tag, ""+i, cr);
	}

	/**
	 * Build an XML string (with carriage return) for a certain tag double value
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
	 * @param cr true if a carriage return is desired after the ending tag.
	 * @return The XML String for the tag.
	 */
	public static final String addTagValue(String tag, double d, boolean cr)
	{
		return addTagValue(tag, ""+d, cr);
	}

	/**
	 * Build an XML string (with carriage return) for a certain tag Date value
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
	 * @param cr true if a carriage return is desired after the ending tag.
	 * @return The XML String for the tag.
	 */
	public static final String addTagValue(String tag, Date date, boolean cr)
	{
		return addTagValue(tag, date2string(date), cr);
	}
	

    /**
	 * Build an XML string (including a carriage return) for a certain tag BigDecimal value
	 * @param tag The XML tag
	 * @param val The BigDecimal value of the tag
	 * @return The XML String for the tag.
	 */
	public static final String addTagValue(String tag, BigDecimal val)
	{
		return addTagValue(tag, val, true);
	}
	
    /**
	 * Build an XML string (including a carriage return) for a certain tag BigDecimal value
	 * @param tag The XML tag
	 * @param val The BigDecimal value of the tag
	 * 
	 * @return The XML String for the tag.
	 */
	public static final String addTagValue(String tag, BigDecimal val, boolean cr)
	{
		return addTagValue(tag, val!=null ? val.toString() : (String)null, true);
	}

    /**
	 * Build an XML string (including a carriage return) for a certain tag binary (byte[]) value
	 * @param tag The XML tag
	 * @param val The binary value of the tag
	 * @return The XML String for the tag.
     * @throws IOException in case there is an Base64 or GZip encoding problem   
	 */
	public static final String addTagValue(String tag, byte[] val) throws IOException
	{
		return addTagValue(tag, val, true);
	}
	
    /**
	 * Build an XML string (including a carriage return) for a certain tag binary (byte[]) value
	 * @param tag The XML tag
	 * @param val The binary value of the tag
	 * @return The XML String for the tag.
     * @throws IOException in case there is an Base64 or GZip encoding problem  
	 */
	public static final String addTagValue(String tag, byte[] val, boolean cr) throws IOException
	{
		String string;
		if (val==null)
		{
			string=null;
		}
		else
		{
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            GZIPOutputStream gzos = new GZIPOutputStream(baos);
            BufferedOutputStream bos = new BufferedOutputStream(gzos);
            bos.write( val );
            bos.flush();
            bos.close();
            
            string = new String(Base64.encodeBase64(baos.toByteArray()));
		}
		
		return addTagValue(tag, string, true);
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
        ArrayList<String> elements = new ArrayList<String>(); // List of String 
        
        NodeList nodeList = node.getChildNodes();
        if (nodeList==null) return null;
        
        for (int i=0;i<nodeList.getLength();i++)
        {
            String nodeName = nodeList.item(i).getNodeName();
            if (elements.indexOf(nodeName)<0) elements.add(nodeName); 
        }
        
        if (elements.isEmpty()) return null;
        
        return elements.toArray(new String[elements.size()]);
    }

    public static Date stringToDate(String dateString)
    {
        if (Const.isEmpty(dateString)) return null;
        
        try
        {
            synchronized(simpleDateFormat)
            {
                return simpleDateFormat.parse(dateString);
            } 
        }
        catch (ParseException e)
        {
            return null;
        }
    }
    
    public static String date2string(Date date)
    {
        if (date==null) return null;
        synchronized(simpleDateFormat)
        {
            return simpleDateFormat.format(date);
        }
    }
    
    /**
     * Convert a XML encoded binary string back to binary format
     * @param string the (Byte64/GZip) encoded string
     * @return the decoded binary (byte[]) object
     * @throws KettleException In case there is a decoding error
     */
	public static byte[] stringToBinary(String string) throws KettleException {
		try {
	        byte[] bytes;
	        if (string==null)
	        {
	        	bytes = new byte[] {};
	        }
	        else
	        {
	        	bytes = Base64.decodeBase64(string.getBytes());
	        }
	        if (bytes.length>0)
	        {
	            ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
	            GZIPInputStream gzip = new GZIPInputStream(bais);
	            BufferedInputStream bi = new BufferedInputStream(gzip);
	        	byte[] result = new byte[] {};
	            
	            byte[] extra = new byte[1000000];
	            int nrExtra = bi.read(extra);
	            while (nrExtra>=0) {
	            	// add it to bytes...
	            	//
	            	int newSize = result.length + nrExtra;
	            	byte[] tmp = new byte[newSize];
	            	for (int i=0;i<result.length;i++) tmp[i]=result[i];
	            	for (int i=0;i<nrExtra;i++) tmp[result.length+i] = extra[i];
	            	
	            	// change the result
	            	result=tmp;
	                nrExtra = bi.read(extra);
	            }
	            bytes = result;
	            gzip.close();
	        }
	
	        return bytes;
		}
		catch(Exception e) {
			throw new KettleException("Error converting string to binary", e);
		}
	}


    public static String buildCDATA(String string)
    {
        StringBuffer cdata = new StringBuffer("<![CDATA[");
        cdata.append(Const.NVL(string, "")).append("]]>");
        return cdata.toString();
    }

    public static final String openTag(String tag)
    {
        return "<"+tag+">";
    }

    public static final String closeTag(String tag)
    {
        return "</"+tag+">";
    }

    
}


/**
 * Handle external references and return an empty dummy document.
 * @author jb
 * @since 2007-12-21
 *
 */
class DTDIgnoringEntityResolver implements EntityResolver{
	public DTDIgnoringEntityResolver(){
		//nothing
	}
	public InputSource resolveEntity(java.lang.String publicID, java.lang.String systemID) throws IOException {
		System.out.println("Public-ID: "+publicID.toString());
		System.out.println("System-ID: "+systemID.toString());
		return new InputSource(new ByteArrayInputStream("<?xml version='1.0' encoding='UTF-8'?>".getBytes()));
	}
	

}

	