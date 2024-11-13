/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.di.www;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface CartePluginInterface extends CarteServletInterface {

  public void setup( TransformationMap transformationMap, JobMap jobMap, SocketRepository socketRepository,
    List<SlaveServerDetection> detections );

  public void doGet( HttpServletRequest request, HttpServletResponse response ) throws Exception;

  public String getContextPath();

  public void setJettyMode( boolean jettyMode );

  public boolean isJettyMode();
}
