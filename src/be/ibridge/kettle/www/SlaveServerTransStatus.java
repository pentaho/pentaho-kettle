package be.ibridge.kettle.www;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.apache.commons.codec.binary.Base64;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.XMLHandler;
import be.ibridge.kettle.core.exception.KettleXMLException;
import be.ibridge.kettle.trans.Trans;
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
        
        Node statusListNode = XMLHandler.getSubNode(transStatusNode, "stepstatuslist");
        int nr = XMLHandler.countNodes(statusListNode, StepStatus.XML_TAG);
        for (int i=0;i<nr;i++)
        {
            Node stepStatusNode = XMLHandler.getSubNodeByNr(statusListNode, StepStatus.XML_TAG, i);
            stepStatusList.add(new StepStatus(stepStatusNode));
            LogWriter.getInstance().logBasic("SlaveServerTransStatus", "7-"+i);
        }
        
        LogWriter.getInstance().logBasic("SlaveServerTransStatus", "8");
        String loggingString64 = XMLHandler.getTagValue(transStatusNode, "logging_string");
        // This is a Base64 encoded GZIP compressed stream of data.
        try
        {
            byte[] bytes = Base64.decodeBase64(loggingString64.getBytes());
            ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
            GZIPInputStream gzip = new GZIPInputStream(bais);
            int c;
            StringBuffer buffer = new StringBuffer();
            while ( (c=gzip.read())!=-1) buffer.append((char)c);
            gzip.close();
            loggingString = buffer.toString();
            System.out.println("Found logging: "+Const.CR+loggingString);
        }
        catch(IOException e)
        {
            loggingString = "Unable to decode logging from remote server : "+e.toString()+Const.CR+Const.getStackTracker(e);
        }
    }
    
    public static SlaveServerTransStatus fromXML(String xml) throws KettleXMLException
    {
        Document document = XMLHandler.loadXMLString(xml);
        LogWriter.getInstance().logBasic("SlaveServerTransStatus", "Loaded XML document into DOM");
        SlaveServerTransStatus status = new SlaveServerTransStatus(XMLHandler.getSubNode(document, XML_TAG));
        LogWriter.getInstance().logBasic("SlaveServerTransStatus", "Constructed new object");
        return status;
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

    public boolean isRunning()
    {
        return getStatusDescription().equalsIgnoreCase(Trans.STRING_RUNNING) || getStatusDescription().equalsIgnoreCase(Trans.STRING_INITIALIZING);
    }

}
