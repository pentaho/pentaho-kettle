package org.pentaho.di.www;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

import org.apache.commons.codec.binary.Base64;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.trans.Trans;
import org.w3c.dom.Document;
import org.w3c.dom.Node;


public class SlaveServerJobStatus
{
    public static final String XML_TAG = "jobstatus";
    
    private String jobName;
    private String statusDescription;
    private String errorDescription;
    private String loggingString;
    
    public SlaveServerJobStatus()
    {
    }
    
    /**
     * @param transName
     * @param statusDescription
     */
    public SlaveServerJobStatus(String transName, String statusDescription)
    {
        this();
        this.jobName = transName;
        this.statusDescription = statusDescription;
    }
    
    public String getXML()
    {
        StringBuffer xml = new StringBuffer();
        
        xml.append("<"+XML_TAG+">").append(Const.CR);
        xml.append(XMLHandler.addTagValue("jobname", jobName));                
        xml.append(XMLHandler.addTagValue("status_desc", statusDescription));                
        xml.append(XMLHandler.addTagValue("error_desc", errorDescription));          
        
        xml.append(XMLHandler.addTagValue("logging_string", XMLHandler.buildCDATA(loggingString)));          

        xml.append("</"+XML_TAG+">");
        
        return xml.toString();
    }
    
    public SlaveServerJobStatus(Node transStatusNode)
    {
        this();
        jobName = XMLHandler.getTagValue(transStatusNode, "jobname");
        statusDescription = XMLHandler.getTagValue(transStatusNode, "status_desc");
        errorDescription = XMLHandler.getTagValue(transStatusNode, "error_desc");
        
        String loggingString64 = XMLHandler.getTagValue(transStatusNode, "logging_string");
        
        // This is a Base64 encoded GZIP compressed stream of data.
        //
        try
        {
            byte[] bytes = new byte[] {};
            if (loggingString64!=null) bytes = Base64.decodeBase64(loggingString64.getBytes());
            if (bytes.length>0)
            {
                ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
                GZIPInputStream gzip = new GZIPInputStream(bais);
                int c;
                StringBuffer buffer = new StringBuffer();
                while ( (c=gzip.read())!=-1) buffer.append((char)c);
                gzip.close();
                loggingString = buffer.toString();
            }
            else
            {
                loggingString="";
            }
        }
        catch(IOException e)
        {
            loggingString = "Unable to decode logging from remote server : "+e.toString()+Const.CR+Const.getStackTracker(e);
        }
    }
    
    public static SlaveServerJobStatus fromXML(String xml) throws KettleXMLException
    {
        Document document = XMLHandler.loadXMLString(xml);
        SlaveServerJobStatus status = new SlaveServerJobStatus(XMLHandler.getSubNode(document, XML_TAG));
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
     * @return the job name
     */
    public String getJobName()
    {
        return jobName;
    }
    /**
     * @param jobName the job name to set
     */
    public void setJobName(String jobName)
    {
        this.jobName = jobName;
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
