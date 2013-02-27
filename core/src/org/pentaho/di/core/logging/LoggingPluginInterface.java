package org.pentaho.di.core.logging;

public interface LoggingPluginInterface extends LoggingEventListener {
  
  // @Override
  // public void eventAdded(LoggingEvent event);
  //
  
  public void init();
  
  public void dispose();
  
}
