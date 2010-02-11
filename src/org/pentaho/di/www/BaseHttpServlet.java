package org.pentaho.di.www;

import java.util.List;

import javax.servlet.http.HttpServlet;

import org.pentaho.di.core.logging.LogMessage;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.core.logging.LoggingRegistry;

public class BaseHttpServlet extends HttpServlet {

  protected static final long serialVersionUID = -1348342810327662788L;
  protected static LogWriter log = LogWriter.getInstance();
  private static LoggingRegistry registry = LoggingRegistry.getInstance();

  private String loggingId;

  private TransformationMap transformationMap;
  private JobMap jobMap;
  private SocketRepository socketRepository;
  private List<SlaveServerDetection> detections;

  private boolean jettyMode = false;

  public String convertContextPath(String contextPath) {
    if (jettyMode) {
      return contextPath;
    }
    return contextPath.substring(contextPath.lastIndexOf("/") + 1);
  }

  public BaseHttpServlet() {
  }

  public BaseHttpServlet(TransformationMap transformationMap) {
    this.transformationMap = transformationMap;
    this.jettyMode = true;
  }

  public BaseHttpServlet(JobMap jobMap) {
    this.jobMap = jobMap;
    this.jettyMode = true;
  }

  public BaseHttpServlet(TransformationMap transformationMap, JobMap jobMap) {
    this.transformationMap = transformationMap;
    this.jobMap = jobMap;
    this.jettyMode = true;
  }

  public BaseHttpServlet(TransformationMap transformationMap,
      SocketRepository socketRepository) {
    this.transformationMap = transformationMap;
    this.socketRepository = socketRepository;
    this.jettyMode = true;
  }

  public BaseHttpServlet(JobMap jobMap, SocketRepository socketRepository) {
    this.jobMap = jobMap;
    this.socketRepository = socketRepository;
    this.jettyMode = true;
  }

  public BaseHttpServlet(List<SlaveServerDetection> detections) {
    this.detections = detections;
    this.jettyMode = true;
  }

  public TransformationMap getTransformationMap() {
    if (transformationMap == null) {
      return CarteSingleton.getInstance().getTransformationMap();
    }
    return transformationMap;
  }

  public JobMap getJobMap() {
    if (jobMap == null) {
      return CarteSingleton.getInstance().getJobMap();
    }
    return jobMap;
  }

  public SocketRepository getSocketRepository() {
    if (socketRepository == null) {
      return CarteSingleton.getInstance().getSocketRepository();
    }
    return socketRepository;
  }

  public List<SlaveServerDetection> getDetections() {
    return detections;
  }

  public boolean isJettyMode() {
    return jettyMode;
  }

  public void setJettyMode(boolean jettyMode) {
    this.jettyMode = jettyMode;
  }
  
  private void checkLoggingId() {
    if (loggingId == null) {
      loggingId = registry.registerLoggingSource(this);

      try {
        throw new Exception();
      } catch (Exception e) {
        StackTraceElement[] stackTrace = e.getStackTrace();
        for (StackTraceElement element : stackTrace) {
          System.out.println(element.getClass().toString());
        }
      }
    }
  }

  public void logMinimal(String s) {
    checkLoggingId();
    log.println(new LogMessage(s, loggingId, LogWriter.LOG_LEVEL_MINIMAL)); //$NON-NLS-1$
  }

  public void logBasic(String s) {
    checkLoggingId();
    log.println(new LogMessage(s, loggingId, LogWriter.LOG_LEVEL_BASIC)); //$NON-NLS-1$
  }

  public void logError(String s) {
    checkLoggingId();
    log.println(new LogMessage(s, loggingId, LogWriter.LOG_LEVEL_ERROR)); //$NON-NLS-1$
  }

  public void logError(String s, Throwable e) {
    checkLoggingId();
    log.println(new LogMessage(s, loggingId, LogWriter.LOG_LEVEL_ERROR), e); //$NON-NLS-1$
  }

  public void logBasic(String s, Object... arguments) {
    checkLoggingId();
    log.println(new LogMessage(s, loggingId, arguments,
        LogWriter.LOG_LEVEL_BASIC));
  }

  public void logDetailed(String s, Object... arguments) {
    checkLoggingId();
    log.println(new LogMessage(s, loggingId, arguments,
        LogWriter.LOG_LEVEL_DETAILED));
  }

  public void logError(String s, Object... arguments) {
    checkLoggingId();
    log.println(new LogMessage(s, loggingId, arguments,
        LogWriter.LOG_LEVEL_ERROR));
  }

  public void logDetailed(String s) {
    checkLoggingId();
    log.println(new LogMessage(s, loggingId, LogWriter.LOG_LEVEL_DETAILED));
  }

  public void logDebug(String s) {
    checkLoggingId();
    log.println(new LogMessage(s, loggingId, LogWriter.LOG_LEVEL_DEBUG));
  }

  public void logRowlevel(String s) {
    checkLoggingId();
    log.println(new LogMessage(s, loggingId, LogWriter.LOG_LEVEL_ROWLEVEL));
  }

}
