package org.pentaho.di.www;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface CartePluginInterface extends CarteServletInterface {
  
  public void setup(TransformationMap transformationMap, JobMap jobMap, SocketRepository socketRepository, List<SlaveServerDetection> detections);
  
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws Exception;
  
  public String getContextPath();
  
  public void setJettyMode(boolean jettyMode);
  
  public boolean isJettyMode();
}
