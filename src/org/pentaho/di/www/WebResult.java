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
package org.pentaho.di.www;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.xml.XMLHandler;
import org.w3c.dom.Document;
import org.w3c.dom.Node;



public class WebResult
{
    public static final String XML_TAG = "webresult";
    
    public static final String STRING_OK = "OK";
    public static final String STRING_ERROR = "ERROR";

    public static final WebResult OK = new WebResult(STRING_OK);
    
    private String result;
    private String message;
    
    public WebResult(String result)
    {
        this.result = result;
    }    
    
    public WebResult(String result, String message)
    {
        this.result = result;
        this.message = message;
    }

    public String getXML()
    {
        StringBuffer xml = new StringBuffer();
        
        xml.append("<"+XML_TAG+">").append(Const.CR);
        
        xml.append("  ").append(XMLHandler.addTagValue("result", result));
        xml.append("  ").append(XMLHandler.addTagValue("message", message));

        xml.append("</"+XML_TAG+">").append(Const.CR);

        return xml.toString();
    }
    
    public String toString()
    {
        return getXML();
    }
    
    public WebResult(Node webResultNode)
    {
        result = XMLHandler.getTagValue(webResultNode, "result");
        message = XMLHandler.getTagValue(webResultNode, "message");
    }
    
    public String getResult()
    {
        return result;
    }
    
    public void setResult(String result)
    {
        this.result = result;
    }
    
    public String getMessage()
    {
        return message;
    }
    
    public void setMessage(String message)
    {
        this.message = message;
    }

    public static WebResult fromXMLString(String xml) throws KettleXMLException
    {
        try
        {
            Document doc = XMLHandler.loadXMLString(xml);
            Node node = XMLHandler.getSubNode(doc, XML_TAG);
            
            return new WebResult(node);
        }
        catch(Exception e)
        {
            throw new KettleXMLException("Unable to create webresult from XML", e);
        }
    }
}
