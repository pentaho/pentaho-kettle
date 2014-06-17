/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2013 by Pentaho : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.www;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.gui.AreaOwner;
import org.pentaho.di.core.gui.Point;
import org.pentaho.di.core.gui.SwingGC;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.JobPainter;
import org.pentaho.di.job.entry.JobEntryCopy;

public class GetJobImageServlet extends BaseHttpServlet implements CartePluginInterface {

  private static final long serialVersionUID = -4365372274638005929L;

  private static Class<?> PKG = GetTransStatusServlet.class; // for i18n purposes, needed by Translator2!!

  public static final String CONTEXT_PATH = "/kettle/jobImage";

  public void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException,
    IOException {
    if ( isJettyMode() && !request.getContextPath().startsWith( CONTEXT_PATH ) ) {
      return;
    }

    if ( log.isDebug() ) {
      logDebug( BaseMessages.getString( PKG, "GetJobImageServlet.Log.JobImageRequested" ) );
    }

    String jobName = request.getParameter( "name" );
    String id = request.getParameter( "id" );

    // ID is optional...
    //
    Job job;
    CarteObjectEntry entry;
    if ( Const.isEmpty( id ) ) {
      // get the first transformation that matches...
      //
      entry = getJobMap().getFirstCarteObjectEntry( jobName );
      if ( entry == null ) {
        job = null;
      } else {
        id = entry.getId();
        job = getJobMap().getJob( entry );
      }
    } else {
      // Take the ID into account!
      //
      entry = new CarteObjectEntry( jobName, id );
      job = getJobMap().getJob( entry );
    }

    try {
      if ( job != null ) {

        response.setStatus( HttpServletResponse.SC_OK );

        response.setCharacterEncoding( "UTF-8" );
        response.setContentType( "image/png" );

        // Generate xform image
        //
        BufferedImage image = generateJobImage( job.getJobMeta() );
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
          ImageIO.write( image, "png", os );
        } finally {
          os.flush();
        }
        response.setContentLength( os.size() );

        OutputStream out = response.getOutputStream();
        out.write( os.toByteArray() );

      }
    } catch ( Exception e ) {
      e.printStackTrace();
    }
  }

  private BufferedImage generateJobImage( JobMeta jobMeta ) throws Exception {
    float magnification = 1.0f;
    Point maximum = jobMeta.getMaximum();
    maximum.multiply( magnification );

    SwingGC gc = new SwingGC( null, maximum, 32, 0, 0 );
    JobPainter jobPainter =
      new JobPainter(
        gc, jobMeta, maximum, null, null, null, null, null, new ArrayList<AreaOwner>(),
        new ArrayList<JobEntryCopy>(), 32, 1, 0, 0, true, "Arial", 10 );
    jobPainter.setMagnification( magnification );
    jobPainter.drawJob();

    BufferedImage image = (BufferedImage) gc.getImage();

    return image;
  }

  public String toString() {
    return "Job Image Handler";
  }

  public String getService() {
    return CONTEXT_PATH + " (" + toString() + ")";
  }

  public String getContextPath() {
    return CONTEXT_PATH;
  }
}
