package org.pentaho.di.core.trans;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.XMLHandler;
import be.ibridge.kettle.core.exception.KettleXMLException;

public class TransConfiguration
{
    public static final String XML_TAG = "transformation_configuration";
    
    private TransMeta transMeta;
    private TransExecutionConfiguration transExecutionConfiguration;
    
    /**
     * @param transMeta
     * @param transExecutionConfiguration
     */
    public TransConfiguration(TransMeta transMeta, TransExecutionConfiguration transExecutionConfiguration)
    {
        this.transMeta = transMeta;
        this.transExecutionConfiguration = transExecutionConfiguration;
    }
    
    public String getXML()
    {
        StringBuffer xml = new StringBuffer();
        
        xml.append("<"+XML_TAG+">").append(Const.CR);
        
        xml.append(transMeta.getXML());
        xml.append(transExecutionConfiguration.getXML());
        
        xml.append("</"+XML_TAG+">").append(Const.CR);
        
        return xml.toString();
    }
    
    public TransConfiguration(Node configNode) throws KettleXMLException
    {
        Node transNode = XMLHandler.getSubNode(configNode, TransMeta.XML_TAG);
        transMeta = new TransMeta(transNode);
        Node trecNode = XMLHandler.getSubNode(configNode, TransExecutionConfiguration.XML_TAG);
        transExecutionConfiguration = new TransExecutionConfiguration(trecNode);
    }
    
    public static final TransConfiguration fromXML(String xml) throws KettleXMLException
    {
        Document document = XMLHandler.loadXMLString(xml);
        Node configNode = XMLHandler.getSubNode(document, XML_TAG);
        return new TransConfiguration(configNode);
    }
    
    /**
     * @return the transExecutionConfiguration
     */
    public TransExecutionConfiguration getTransExecutionConfiguration()
    {
        return transExecutionConfiguration;
    }
    /**
     * @param transExecutionConfiguration the transExecutionConfiguration to set
     */
    public void setTransExecutionConfiguration(TransExecutionConfiguration transExecutionConfiguration)
    {
        this.transExecutionConfiguration = transExecutionConfiguration;
    }
    /**
     * @return the transMeta
     */
    public TransMeta getTransMeta()
    {
        return transMeta;
    }
    /**
     * @param transMeta the transMeta to set
     */
    public void setTransMeta(TransMeta transMeta)
    {
        this.transMeta = transMeta;
    }
    
    
}
