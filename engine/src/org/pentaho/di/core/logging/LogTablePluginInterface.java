package org.pentaho.di.core.logging;

import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.trans.HasDatabasesInterface;

public interface LogTablePluginInterface extends LogTableInterface {

  public enum TableType {
    JOB, TRANS,
    ;
  }
  
  public TableType getType();
  
  public String getLogTablePluginUIClassname();

  public void setContext(VariableSpace space, HasDatabasesInterface jobMeta);
  
  // Otherwise identical to the log table interface.
  
}
