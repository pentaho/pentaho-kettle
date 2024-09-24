/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package org.pentaho.di.engine.configuration.impl.pentaho.scheduler;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@XmlRootElement( name = "jobScheduleRequest" )
@XmlAccessorType( XmlAccessType.FIELD )
@XmlType( propOrder = { "inputFile", "jobParameters", "pdiParameters" } )
public class JobScheduleRequest implements Serializable {

  private static final long serialVersionUID = -485489832281790257L;

  private String inputFile;

  private List<JobScheduleParam> jobParameters = new ArrayList<>();

  private Map<String, String> pdiParameters = new HashMap<>();

  public String getInputFile() {
    return inputFile;
  }

  public void setInputFile( String file ) {
    this.inputFile = file;
  }

  public List<JobScheduleParam> getJobParameters() {
    return jobParameters;
  }

  public void setJobParameters( List<JobScheduleParam> jobParameters ) {
    this.jobParameters = jobParameters;
  }

  public Map<String, String> getPdiParameters() {
    return pdiParameters;
  }

  public void setPdiParameters( Map<String, String> pdiParameters ) {
    this.pdiParameters = pdiParameters;
  }

}
