package org.pentaho.di.core.logging;

public enum LogTableType {

  JOB_LOG_TABLE("KETTLE_LOG_JOB_EXCEPTION_FAILOVER"),
  JOB_ENTRY_LOG_TABLE("KETTLE_LOG_JOB_ENTRY_EXCEPTION_FAILOVER"),
  TRANS_LOG_TABLE("KETTLE_LOG_TRANS_EXCEPTION_FAILOVER");

  private String exception_prop;

  private LogTableType( String exception_prop ){
    this.exception_prop = exception_prop;
  }

  public String getExceptionParamName(){
    return exception_prop;
  }
}
