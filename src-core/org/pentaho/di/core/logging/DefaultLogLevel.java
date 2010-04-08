package org.pentaho.di.core.logging;

/**
 * Retains the default log level during execution
 * 
 * @author matt
 */
public class DefaultLogLevel {
  private static DefaultLogLevel defaultLogLevel;
  
  private LogLevel logLevel;
  
  private DefaultLogLevel() {
    logLevel = LogLevel.BASIC;
  }
  
  private static DefaultLogLevel getInstance() {
    if (defaultLogLevel==null) {
      defaultLogLevel = new DefaultLogLevel(); 
    }
    return defaultLogLevel;
  }
  
  /**
   * @return The default log level for this application
   */
  public static LogLevel getLogLevel() {
    return getInstance().logLevel;
  }
  
  /**
   * @param logLevel Set the default log level for this application
   */
  public static void setLogLevel(LogLevel logLevel) {
    getInstance().logLevel = logLevel;
  }
  
  
}
