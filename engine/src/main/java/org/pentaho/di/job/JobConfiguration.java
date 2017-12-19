/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.job;

import java.io.IOException;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.xml.XMLHandler;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class JobConfiguration {
  public static final String XML_TAG = "job_configuration";

  private JobMeta jobMeta;
  private JobExecutionConfiguration jobExecutionConfiguration;

  /**
   * @param jobMeta
   * @param jobExecutionConfiguration
   */
  public JobConfiguration( JobMeta jobMeta, JobExecutionConfiguration jobExecutionConfiguration ) {
    this.jobMeta = jobMeta;
    this.jobExecutionConfiguration = jobExecutionConfiguration;
  }

  public String getXML() throws IOException {
    StringBuilder xml = new StringBuilder( 100 );

    xml.append( "<" + XML_TAG + ">" ).append( Const.CR );

    xml.append( jobMeta.getXML() );
    xml.append( jobExecutionConfiguration.getXML() );

    xml.append( "</" + XML_TAG + ">" ).append( Const.CR );

    return xml.toString();
  }

  public JobConfiguration( Node configNode ) throws KettleException {
    Node jobNode = XMLHandler.getSubNode( configNode, JobMeta.XML_TAG );
    Node trecNode = XMLHandler.getSubNode( configNode, JobExecutionConfiguration.XML_TAG );
    jobExecutionConfiguration = new JobExecutionConfiguration( trecNode );
    jobMeta = new JobMeta( jobNode, jobExecutionConfiguration.getRepository(), null );
  }

  public static final JobConfiguration fromXML( String xml ) throws KettleException {
    Document document = XMLHandler.loadXMLString( xml );
    Node configNode = XMLHandler.getSubNode( document, XML_TAG );
    return new JobConfiguration( configNode );
  }

  /**
   * @return the jobExecutionConfiguration
   */
  public JobExecutionConfiguration getJobExecutionConfiguration() {
    return jobExecutionConfiguration;
  }

  /**
   * @param jobExecutionConfiguration
   *          the jobExecutionConfiguration to set
   */
  public void setJobExecutionConfiguration( JobExecutionConfiguration jobExecutionConfiguration ) {
    this.jobExecutionConfiguration = jobExecutionConfiguration;
  }

  /**
   * @return the job metadata
   */
  public JobMeta getJobMeta() {
    return jobMeta;
  }

  /**
   * @param jobMeta
   *          the job meta data to set
   */
  public void setJobMeta( JobMeta jobMeta ) {
    this.jobMeta = jobMeta;
  }
}
