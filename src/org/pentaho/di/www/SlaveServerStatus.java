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

import java.util.ArrayList;
import java.util.List;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.xml.XMLHandler;
import org.w3c.dom.Document;
import org.w3c.dom.Node;



public class SlaveServerStatus
{
    public static final String XML_TAG = "serverstatus";

    private String             statusDescription;
    private String             errorDescription;
    
    private List<SlaveServerTransStatus> transStatusList;
    private List<SlaveServerJobStatus>   jobStatusList;
    

    public SlaveServerStatus()
    {
        transStatusList = new ArrayList<SlaveServerTransStatus>();
        jobStatusList = new ArrayList<SlaveServerJobStatus>();
    }
    
    public SlaveServerStatus(String statusDescription)
    {
        this();
        this.statusDescription = statusDescription;
    }


    /**
     * @param statusDescription
     * @param transStatusList
     * @param jobStatusList
     */
    public SlaveServerStatus(String statusDescription, List<SlaveServerTransStatus> transStatusList, List<SlaveServerJobStatus> jobStatusList)
    {
        this.statusDescription = statusDescription;
        this.transStatusList = transStatusList;
        this.jobStatusList = jobStatusList;
    }

    public String getXML() throws KettleException
    {
        StringBuffer xml = new StringBuffer();

        xml.append("<" + XML_TAG + ">").append(Const.CR);
        xml.append(XMLHandler.addTagValue("statusdesc", statusDescription));

        xml.append("  <transstatuslist>").append(Const.CR);
        for (int i = 0; i < transStatusList.size(); i++)
        {
            SlaveServerTransStatus transStatus = transStatusList.get(i);
            xml.append("    ").append(transStatus.getXML()).append(Const.CR);
        }
        xml.append("  </transstatuslist>").append(Const.CR);

        xml.append("  <jobstatuslist>").append(Const.CR);
        for (int i = 0; i < jobStatusList.size(); i++)
        {
            SlaveServerJobStatus jobStatus = jobStatusList.get(i);
            xml.append("    ").append(jobStatus.getXML()).append(Const.CR);
        }
        xml.append("  </jobstatuslist>").append(Const.CR);
        
        xml.append("</" + XML_TAG + ">").append(Const.CR);

        return xml.toString();
    }

    public SlaveServerStatus(Node statusNode) throws KettleException
    {
        this();
        statusDescription = XMLHandler.getTagValue(statusNode, "statusdesc");
        Node listTransNode = XMLHandler.getSubNode(statusNode, "transstatuslist");
        Node listJobsNode = XMLHandler.getSubNode(statusNode, "jobstatuslist");
        
        int nrTrans = XMLHandler.countNodes(listTransNode, SlaveServerTransStatus.XML_TAG);
        int nrJobs  = XMLHandler.countNodes(listJobsNode, SlaveServerJobStatus.XML_TAG);
        
        for (int i = 0; i < nrTrans; i++)
        {
            Node transStatusNode = XMLHandler.getSubNodeByNr(listTransNode, SlaveServerTransStatus.XML_TAG, i);
            transStatusList.add( new SlaveServerTransStatus(transStatusNode) );
        }

        for (int i = 0; i < nrJobs; i++)
        {
            Node jobStatusNode = XMLHandler.getSubNodeByNr(listJobsNode, SlaveServerJobStatus.XML_TAG, i);
            jobStatusList.add( new SlaveServerJobStatus(jobStatusNode) );
        }
    }
    
    public static SlaveServerStatus fromXML(String xml) throws KettleException
    {
        Document document = XMLHandler.loadXMLString(xml);
        return new SlaveServerStatus(XMLHandler.getSubNode(document, XML_TAG));
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
     * @return the transStatusList
     */
    public List<SlaveServerTransStatus> getTransStatusList()
    {
        return transStatusList;
    }

    /**
     * @param transStatusList the transStatusList to set
     */
    public void setTransStatusList(List<SlaveServerTransStatus> transStatusList)
    {
        this.transStatusList = transStatusList;
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

    public SlaveServerTransStatus findTransStatus(String transName, String id)
    {
        for (int i=0;i<transStatusList.size();i++)
        {
            SlaveServerTransStatus transStatus = (SlaveServerTransStatus) transStatusList.get(i);
            if (transStatus.getTransName().equalsIgnoreCase(transName) && 
            	(Const.isEmpty(id) || transStatus.getId().equals(id))
            ) return transStatus;
        }
        return null;
    }

    public SlaveServerJobStatus findJobStatus(String jobName)
    {
        for (int i=0;i<jobStatusList.size();i++)
        {
            SlaveServerJobStatus jobStatus = (SlaveServerJobStatus) jobStatusList.get(i);
            if (jobStatus.getJobName().equalsIgnoreCase(jobName)) return jobStatus;
        }
        return null;
    }

	/**
	 * @return the jobStatusList
	 */
	public List<SlaveServerJobStatus> getJobStatusList() {
		return jobStatusList;
	}

	/**
	 * @param jobStatusList the jobStatusList to set
	 */
	public void setJobStatusList(List<SlaveServerJobStatus> jobStatusList) {
		this.jobStatusList = jobStatusList;
	}

}
