package org.pentaho.di.www.jaxrs;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

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

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public List<StepStatus> getStepStatus() {
    return stepStatus;
  }

  public void setStepStatus(List<StepStatus> stepStatus) {
    this.stepStatus = stepStatus;
  }

  public void addStepStatus(StepStatus status) {
    stepStatus.add(status);
  }

}
