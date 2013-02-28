package org.pentaho.di.core.logging.log4j;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.pentaho.di.core.logging.CentralLogStore;
import org.pentaho.di.core.logging.KettleLoggingEvent;
import org.pentaho.di.core.logging.LoggingPluginInterface;

public class Log4jLogging implements LoggingPluginInterface {
  
  public static final String STRING_PENTAHO_DI_LOGGER_NAME = "org.pentaho.di";

  public static final String STRING_PENTAHO_DI_CONSOLE_APPENDER = "ConsoleAppender:" + STRING_PENTAHO_DI_LOGGER_NAME;

  private Logger pentahoLogger;
  
  public Log4jLogging() {
    pentahoLogger = Logger.getLogger(STRING_PENTAHO_DI_LOGGER_NAME);
    pentahoLogger.setAdditivity(false);
  }

  @Override
  public void eventAdded(KettleLoggingEvent event) {
    switch(event.getLevel()) {
      case ERROR: 
        pentahoLogger.log(Level.ERROR, event.getMessage()); 
        break; 
      case DEBUG: 
      case ROWLEVEL: 
        pentahoLogger.log(Level.DEBUG, event.getMessage()); 
        break; 
      default: 
        pentahoLogger.log(Level.INFO, event.getMessage()); 
        break; 
    }
  }
  
  @Override
  public void init() {
    CentralLogStore.getAppender().addLoggingEventListener(this);
  }
  
  public void dispose() {
    CentralLogStore.getAppender().removeLoggingEventListener(this);
  };

}
