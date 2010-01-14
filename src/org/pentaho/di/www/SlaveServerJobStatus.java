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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

import org.apache.commons.codec.binary.Base64;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.exception.KettleException;
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
    private int firstLoggingLineNr;
    private int lastLoggingLineNr;

    private Result result;
    
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
    
    public String getXML() throws KettleException
    {
        StringBuffer xml = new StringBuffer();
        
        xml.append("<"+XML_TAG+">").append(Const.CR);
        xml.append(XMLHandler.addTagValue("jobname", jobName));                
        xml.append(XMLHandler.addTagValue("status_desc", statusDescription));                
        xml.append(XMLHandler.addTagValue("error_desc", errorDescription));          
        
        xml.append(XMLHandler.addTagValue("logging_string", XMLHandler.buildCDATA(loggingString)));
        xml.append(XMLHandler.addTagValue("first_log_line_nr", firstLoggingLineNr));          
        xml.append(XMLHandler.addTagValue("last_log_line_nr", lastLoggingLineNr));          

        if (result!=null)
        {
        	try {
				String resultXML = result.getXML();
				xml.append(resultXML);
			} catch (IOException e) {
				throw new KettleException("Unable to serialize result object as XML", e);
			}
        }

        xml.append("</"+XML_TAG+">");
        
        return xml.toString();
    }
    
    public SlaveServerJobStatus(Node jobStatusNode) throws KettleException 
    {
        this();
        jobName = XMLHandler.getTagValue(jobStatusNode, "jobname");
        statusDescription = XMLHandler.getTagValue(jobStatusNode, "status_desc");
        errorDescription = XMLHandler.getTagValue(jobStatusNode, "error_desc");
        
        firstLoggingLineNr = Const.toInt(XMLHandler.getTagValue(jobStatusNode, "first_log_line_nr"), 0);
        lastLoggingLineNr = Const.toInt(XMLHandler.getTagValue(jobStatusNode, "last_log_line_nr"), 0);

        String loggingString64 = XMLHandler.getTagValue(jobStatusNode, "logging_string");
        
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
            loggingString = "Unable to decode logging from remote server : "+e.toString()+Const.CR+Const.getStackTracker(e)+Const.CR;
        }
        
        // get the result object, if there is any...
        //
        Node resultNode = XMLHandler.getSubNode(jobStatusNode, Result.XML_TAG);
        if (resultNode!=null)
        {
        	try {
				result = new Result(resultNode);
			} catch (KettleException e) {
				loggingString+="Unable to serialize result object as XML"+Const.CR+Const.getStackTracker(e)+Const.CR;
			}
        }
    }
    
    public static SlaveServerJobStatus fromXML(String xml) throws KettleException
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
    
    public boolean isWaiting()
    {
        return getStatusDescription().equalsIgnoreCase(Trans.STRING_WAITING);
    }

	/**
	 * @return the result
	 */
	public Result getResult() {
		return result;
	}

	/**
	 * @param result the result to set
	 */
	public void setResult(Result result) {
		this.result = result;
	}

	/**
	 * @return the firstLoggingLineNr
	 */
	public int getFirstLoggingLineNr() {
		return firstLoggingLineNr;
	}

	/**
	 * @param firstLoggingLineNr the firstLoggingLineNr to set
	 */
	public void setFirstLoggingLineNr(int firstLoggingLineNr) {
		this.firstLoggingLineNr = firstLoggingLineNr;
	}

	/**
	 * @return the lastLoggingLineNr
	 */
	public int getLastLoggingLineNr() {
		return lastLoggingLineNr;
	}

	/**
	 * @param lastLoggingLineNr the lastLoggingLineNr to set
	 */
	public void setLastLoggingLineNr(int lastLoggingLineNr) {
		this.lastLoggingLineNr = lastLoggingLineNr;
	}
}
