package org.pentaho.di.www.jaxrs;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class JobStatus {

  private String id;
  private String name;
  private String status;

  public JobStatus() {
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

}
