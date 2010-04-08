package org.pentaho.di.core.logging;

public interface LogChannelInterface {

  /**
   * @return the id of the logging channel
   */
  public String getLogChannelId();
  
  public LogLevel getLogLevel();
  
  public void setLogLevel(LogLevel logLevel);

  public boolean isBasic();

  public boolean isDetailed();

  public boolean isDebug();

  public boolean isRowLevel();

  public boolean isError();

  public void logMinimal(String message);

  public void logMinimal(String message, Object... arguments);

  public void logBasic(String message);

  public void logBasic(String message, Object... arguments);

  public void logDetailed(String message);

  public void logDetailed(String message, Object... arguments);

  public void logDebug(String message);

  public void logDebug(String message, Object... arguments);

  public void logRowlevel(String message);

  public void logRowlevel(String message, Object... arguments);

  public void logError(String message);

  public void logError(String message, Throwable e);

  public void logError(String message, Object... arguments);


}
