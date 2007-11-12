/*
 * Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
*/
package org.pentaho.di.core.xml;

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

