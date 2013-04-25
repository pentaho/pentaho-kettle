package org.pentaho.di.core.logging;

import org.pentaho.di.core.logging.KettleLogLayout;


public class ConsoleLoggingEventListener implements KettleLoggingEventListener {

  private KettleLogLayout layout;
  
  public ConsoleLoggingEventListener() {
    this.layout = new KettleLogLayout(true);
  }
  
  @Override
  public void eventAdded(KettleLoggingEvent event) {
    
    String logText = layout.format(event);
    
    if (event.getLevel()==LogLevel.ERROR) {
      KettleLogStore.OriginalSystemErr.println(logText);
    } else {
      KettleLogStore.OriginalSystemOut.println(logText);
    }
  }  
}
