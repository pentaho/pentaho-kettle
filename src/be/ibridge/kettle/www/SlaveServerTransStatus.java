package be.ibridge.kettle.www;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.XMLHandler;
import be.ibridge.kettle.core.exception.KettleXMLException;
import be.ibridge.kettle.trans.step.StepStatus;

public class SlaveServerTransStatus
{
    public static final String XML_TAG = "transstatus";
    
    private String transName;
    private String statusDescription;
    private String errorDescription;
    private String loggingString;
    private List   stepStatusList;
    
    public SlaveServerTransStatus()
    {
        stepStatusList = new ArrayList();
    }
    
    /**
     * @param transName
     * @param statusDescription
     */
    public SlaveServerTransStatus(String transName, String statusDescription)
    {
        this();
        this.transName = transName;
        this.statusDescription = statusDescription;
    }
    
    public String getXML()
    {
        StringBuffer xml = new StringBuffer();
        
        xml.append("<"+XML_TAG+">").append(Const.CR);
        xml.append(XMLHandler.addTagValue("transname", transName));                
        xml.append(XMLHandler.addTagValue("status_desc", statusDescription));                
        xml.append(XMLHandler.addTagValue("error_desc", errorDescription));          
        
        xml.append("  <stepstatuslist>").append(Const.CR);
        for (int i = 0; i < stepStatusList.size(); i++)
        {
            StepStatus stepStatus = (StepStatus) stepStatusList.get(i);
            xml.append("    ").append(stepStatus.getXML()).append(Const.CR);
        }
        xml.append("  </stepstatuslist>").append(Const.CR);

        xml.append(XMLHandler.addTagValue("logging_string", loggingString));          

        xml.append("</"+XML_TAG+">");
        
        return xml.toString();
    }
    
    public SlaveServerTransStatus(Node transStatusNode)
    {
        this();
        transName = XMLHandler.getTagValue(transStatusNode, "transname");
        statusDescription = XMLHandler.getTagValue(transStatusNode, "status_desc");
        errorDescription = XMLHandler.getTagValue(transStatusNode, "error_desc");
        
        Node statusListNode = XMLHandler.getSubNode(transStatusNode, "stepstatusnode");
        int nr = XMLHandler.countNodes(statusListNode, StepStatus.XML_TAG);
        for (int i=0;i<nr;i++)
        {
            Node stepStatusNode = XMLHandler.getSubNodeByNr(statusListNode, StepStatus.XML_TAG, i);
            stepStatusList.add(new StepStatus(stepStatusNode));
        }
        
        loggingString = XMLHandler.getTagValue(transStatusNode, "logging_string");
    }
    
    public static SlaveServerTransStatus fromXML(String xml) throws KettleXMLException
    {
        Document document = XMLHandler.loadXMLString(xml);
        return new SlaveServerTransStatus(XMLHandler.getSubNode(document, XML_TAG));
    }

    /**
     * @return the statusDescription
     */
    public String getStatusDescription()
    {
        return statusDescription;
    }
    /**
     * @param statusDescription the statusDescription to set
     */
    public void setStatusDescription(String statusDescription)
    {
        this.statusDescription = statusDescription;
    }
    /**
     * @return the transName
     */
    public String getTransName()
    {
        return transName;
    }
    /**
     * @param transName the transName to set
     */
    public void setTransName(String transName)
    {
        this.transName = transName;
    }

    /**
     * @return the errorDescription
     */
    public String getErrorDescription()
    {
        return errorDescription;
    }

    /**
     * @param errorDescription the errorDescription to set
     */
    public void setErrorDescription(String errorDescription)
    {
        this.errorDescription = errorDescription;
    }

    /**
     * @return the stepStatusList
     */
    public List getStepStatusList()
    {
        return stepStatusList;
    }

    /**
     * @param stepStatusList the stepStatusList to set
     */
    public void setStepStatusList(List stepStatusList)
    {
        this.stepStatusList = stepStatusList;
    }

    /**
     * @return the loggingString
     */
    public String getLoggingString()
    {
        return loggingString;
    }

    /**
     * @param loggingString the loggingString to set
     */
    public void setLoggingString(String loggingString)
    {
        this.loggingString = loggingString;
    }

}
