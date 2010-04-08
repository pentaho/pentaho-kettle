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
package org.pentaho.di.job;

import java.io.IOException;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.gui.OverwritePrompter;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.repository.DomainObjectCreationException;
import org.pentaho.di.repository.DomainObjectRegistry;
import org.pentaho.di.repository.Repository;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class JobConfiguration
{
    public static final String XML_TAG = "job_configuration";
    
    private JobMeta jobMeta;
    private JobExecutionConfiguration jobExecutionConfiguration;
    
    /**
     * @param jobMeta
     * @param jobExecutionConfiguration
     */
    public JobConfiguration(JobMeta jobMeta, JobExecutionConfiguration jobExecutionConfiguration)
    {
        this.jobMeta = jobMeta;
        this.jobExecutionConfiguration = jobExecutionConfiguration;
    }
    
    public String getXML() throws IOException
    {
        StringBuffer xml = new StringBuffer();
        
        xml.append("<"+XML_TAG+">").append(Const.CR);
        
        xml.append(jobMeta.getXML());
        xml.append(jobExecutionConfiguration.getXML());
        
        xml.append("</"+XML_TAG+">").append(Const.CR);
        
        return xml.toString();
    }
    
    public JobConfiguration(Node configNode) throws KettleException
    {
        Node jobNode = XMLHandler.getSubNode(configNode, JobMeta.XML_TAG);
        Node trecNode = XMLHandler.getSubNode(configNode, JobExecutionConfiguration.XML_TAG);
        jobExecutionConfiguration = new JobExecutionConfiguration(trecNode);
        try {
          jobMeta = DomainObjectRegistry.getInstance().constructJobMeta(new Class[] {String.class, Repository.class, OverwritePrompter.class}, new Object[]{jobNode, jobExecutionConfiguration.getRepository(), null}); 
        } catch(DomainObjectCreationException doce) {
          jobMeta = new JobMeta(jobNode, jobExecutionConfiguration.getRepository(), null);
        } 
    }
    
    public static final JobConfiguration fromXML(String xml) throws KettleException
    {
        Document document = XMLHandler.loadXMLString(xml);
        Node configNode = XMLHandler.getSubNode(document, XML_TAG);
        return new JobConfiguration(configNode);
    }
    
    /**
     * @return the jobExecutionConfiguration
     */
    public JobExecutionConfiguration getJobExecutionConfiguration()
    {
        return jobExecutionConfiguration;
    }
    /**
     * @param jobExecutionConfiguration the jobExecutionConfiguration to set
     */
    public void setJobExecutionConfiguration(JobExecutionConfiguration jobExecutionConfiguration)
    {
        this.jobExecutionConfiguration = jobExecutionConfiguration;
    }
    /**
     * @return the job metadata
     */
    public JobMeta getJobMeta()
    {
        return jobMeta;
    }
    /**
     * @param jobMeta the job meta data to set
     */
    public void setJobMeta(JobMeta jobMeta)
    {
        this.jobMeta = jobMeta;
    }
}
