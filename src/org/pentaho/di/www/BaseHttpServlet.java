package org.pentaho.di.www;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogChannelInterface;

public class BaseHttpServlet extends HttpServlet {

  protected static final long serialVersionUID = -1348342810327662788L;

  private TransformationMap transformationMap;
  private JobMap jobMap;
  private SocketRepository socketRepository;
  private List<SlaveServerDetection> detections;

  private boolean jettyMode = false;
  
  protected LogChannelInterface log = new LogChannel("Servlet");

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

  protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    doGet(request, response);
  }

  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    doGet(request, response);
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
  
  public void logMinimal(String s) {
    log.logMinimal(s);
  }

  public void logBasic(String s) {
    log.logBasic(s);
  }

  public void logError(String s) {
    log.logError(s);
  }

  public void logError(String s, Throwable e) {
    log.logError(s, e);
  }

  public void logBasic(String s, Object... arguments) {
    log.logBasic(s, arguments);
  }

  public void logDetailed(String s, Object... arguments) {
    log.logDetailed(s, arguments);
  }

  public void logError(String s, Object... arguments) {
    log.logError(s, arguments);
  }

  public void logDetailed(String s) {
    log.logDetailed(s);
  }

  public void logDebug(String s) {
    log.logDebug(s);
  }

  public void logRowlevel(String s) {
    log.logRowlevel(s);
  }

}
