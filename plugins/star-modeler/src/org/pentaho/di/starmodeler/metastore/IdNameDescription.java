package org.pentaho.di.starmodeler.metastore;

public class IdNameDescription {
  protected String id;
  
  protected String name;

  protected String description;

  public IdNameDescription(String id, String name, String description) {
    this.id = id;
    this.name = name;
    this.description = description;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }
}