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
