package org.pentaho.di.ui.job.entries.hadoopjobexecutor;

import java.beans.PropertyChangeListener;

import org.pentaho.ui.xul.XulEventSource;

public class UserDefinedItem implements XulEventSource {
  private String name;
  private String value;

  public UserDefinedItem() {
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

  public void addPropertyChangeListener(PropertyChangeListener listener) {
  }

  public void removePropertyChangeListener(PropertyChangeListener listener) {
  }

}
