package org.pentaho.di.core.logging;

public interface LoggingPluginInterface extends KettleLoggingEventListener {
  
  // @Override
  // public void eventAdded(LoggingEvent event);
  //
  
  public void init();
  
  public void dispose();
  
}
