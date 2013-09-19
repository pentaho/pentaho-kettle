package org.pentaho.di.www.jaxrs;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class NVPair {

  private String name;
  private String value;

  public NVPair() {
  }

  public NVPair(String name, String value) {
    this.name = name;
    this.value = value;
  }
  
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

}
