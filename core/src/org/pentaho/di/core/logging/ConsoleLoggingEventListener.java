package org.pentaho.di.core.logging;


public class ConsoleLoggingEventListener implements LoggingEventListener {

  private Log4jKettleLayout layout;
  
  public ConsoleLoggingEventListener() {
    this.layout = new Log4jKettleLayout(true);
  }
  
  @Override
  public void eventAdded(LoggingEvent event) {
    
    String logText = layout.format(event);
    
    if (event.getLevel()==LogLevel.ERROR) {
      CentralLogStore.OriginalSystemErr.println(logText);
    } else {
      CentralLogStore.OriginalSystemOut.println(logText);
    }
  }  
}
