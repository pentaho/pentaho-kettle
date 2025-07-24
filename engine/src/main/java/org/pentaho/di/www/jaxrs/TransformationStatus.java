/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.di.www.jaxrs;

import java.util.ArrayList;
import java.util.List;

import jakarta.xml.bind.annotation.XmlRootElement;

import org.pentaho.di.trans.step.StepStatus;

@XmlRootElement
public class TransformationStatus {

  private String id;
  private String name;
  private String status;
  private List<StepStatus> stepStatus = new ArrayList<StepStatus>();

  public TransformationStatus() {
  }

  public String getId() {
    return id;
  }

  public void setId( String id ) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName( String name ) {
    this.name = name;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus( String status ) {
    this.status = status;
  }

  public List<StepStatus> getStepStatus() {
    return stepStatus;
  }

  public void setStepStatus( List<StepStatus> stepStatus ) {
    this.stepStatus = stepStatus;
  }

  public void addStepStatus( StepStatus status ) {
    stepStatus.add( status );
  }

}
