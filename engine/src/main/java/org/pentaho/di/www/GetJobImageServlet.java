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

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.google.common.annotations.VisibleForTesting;
import org.owasp.encoder.Encode;
import org.pentaho.di.core.gui.AreaOwner;
import org.pentaho.di.core.gui.Point;
import org.pentaho.di.core.gui.SwingGC;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.JobPainter;
import org.pentaho.di.job.entry.JobEntryCopy;
import org.pentaho.di.www.exception.DuplicateKeyException;

public class GetJobImageServlet extends BaseHttpServlet implements CartePluginInterface {

  private static final long serialVersionUID = -4365372274638005929L;

  private static Class<?> PKG = GetTransStatusServlet.class; // for i18n purposes, needed by Translator2!!

  public static final String CONTEXT_PATH = "/kettle/jobImage";

  public GetJobImageServlet() {
  }

  public GetJobImageServlet( JobMap jobMap ) {
    super( jobMap );
  }

  /**
<div id="mindtouch">
    <h1>/kettle/jobImage</h1>
    <a name="GET"></a>
    <h2>GET</h2>
    <p>Generates and returns image of the specified job.
  Generates PNG image of the specified job currently present on Carte server. Job name and Carte job ID (optional)
  is used for specifying job to get information for. Response is binary of the PNG image.</p>

    <p><b>Example Request:</b><br />
    <pre function="syntax.xml">
    GET /kettle/jobImage?name=dummy_job
    </pre>

    </p>
    <h3>Parameters</h3>
    <table class="pentaho-table">
    <tbody>
    <tr>
      <th>name</th>
      <th>description</th>
      <th>type</th>
    </tr>
    <tr>
    <td>name</td>
    <td>Name of the job to be used for image generation.</td>
    <td>query</td>
    </tr>
    <tr>
    <td>id</td>
    <td>Carte id of the job to be used for image generation.</td>
    <td>query, optional</td>
    </tr>
    </tbody>
    </table>

  <h3>Response Body</h3>

  <table class="pentaho-table">
    <tbody>
      <tr>
        <td align="right">binary streak:</td>
        <td>image</td>
      </tr>
      <tr>
        <td align="right">media types:</td>
        <td>image/png</td>
      </tr>
    </tbody>
  </table>
    <p>A binary PNG image or empty response if no job is found.</p>


    <h3>Status Codes</h3>
    <table class="pentaho-table">
  <tbody>
    <tr>
      <th>code</th>
      <th>description</th>
    </tr>
    <tr>
      <td>200</td>
      <td>Request was processed.</td>
    </tr>
    <tr>
      <td>500</td>
      <td>Internal server error occurs during request processing.</td>
    </tr>
  </tbody>
</table>
</div>
  */
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

    boolean useXML = "Y".equalsIgnoreCase( request.getParameter( "xml" ) );

    // ID is optional...
    //
    Job job;
    CarteObjectEntry entry;
    if ( Utils.isEmpty( id ) ) {

      try {
        entry = getJobMap().getUniqueCarteObjectEntry( jobName );
      } catch ( DuplicateKeyException e ) {
        buildDuplicateResponse( response, jobName, useXML );
        return;
      }

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
        buildOkResponse( response, job );
      } else {
        buildNotFoundResponse( response, jobName, id, useXML );
      }
    } catch ( Exception e ) {
      e.printStackTrace();
    }
  }

  private void buildDuplicateResponse( HttpServletResponse response, String jobName, boolean useXML ) {
    String message = BaseMessages.getString( PKG, "GetJobImageServlet.Error.DuplicateJobName", jobName );

    PrintWriter out;
    try {
      out = response.getWriter();
      if ( useXML ) {
        out.println( new WebResult( WebResult.STRING_ERROR, message ) );
      } else {
        out.println( "<H1>" + Encode.forHtml( message ) + "</H1>" );
        out.println( "<a href=\""
          + convertContextPath( GetStatusServlet.CONTEXT_PATH ) + "\">"
          + BaseMessages.getString( PKG, "GetJobImageServlet.BackToStatusPage" ) + "</a><p>" );
        response.setStatus( HttpServletResponse.SC_CONFLICT );
      }

    } catch ( IOException e ) {
      e.printStackTrace();
    }
  }

  private void buildOkResponse( HttpServletResponse response, Job job ) throws Exception {
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

  private void buildNotFoundResponse( HttpServletResponse response, String jobName, String id, boolean useXML ) throws Exception {
    String message = BaseMessages.getString( PKG, "GetJobImageServlet.Error.CoundNotFindJob", jobName, id );

    PrintWriter out;
    try {
      out = response.getWriter();
      if ( useXML ) {
        out.println( new WebResult( WebResult.STRING_ERROR, message ) );
      } else {
        out.println( "<H1>" + Encode.forHtml( message ) + "</H1>" );
        out.println( "<a href=\""
          + convertContextPath( GetStatusServlet.CONTEXT_PATH ) + "\">"
          + BaseMessages.getString( PKG, "GetJobImageServlet.BackToStatusPage" ) + "</a><p>" );
        response.setStatus( HttpServletResponse.SC_NOT_FOUND );
      }

    } catch ( IOException e ) {
      e.printStackTrace();
    }
  }

  @VisibleForTesting
  BufferedImage generateJobImage( JobMeta jobMeta ) throws Exception {
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
