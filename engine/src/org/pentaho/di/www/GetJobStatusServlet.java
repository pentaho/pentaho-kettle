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

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.owasp.esapi.ESAPI;
import org.owasp.esapi.Encoder;
import org.pentaho.di.cluster.HttpUtil;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.gui.Point;
import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.Job;

public class GetJobStatusServlet extends BaseHttpServlet implements CartePluginInterface {
  private static Class<?> PKG = GetJobStatusServlet.class; // for i18n purposes, needed by Translator2!!

  private static final long serialVersionUID = 3634806745372015720L;
  public static final String CONTEXT_PATH = "/kettle/jobStatus";

  public GetJobStatusServlet() {
  }

  public GetJobStatusServlet( JobMap jobMap ) {
    super( jobMap );
  }

  public void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException,
    IOException {
    if ( isJettyMode() && !request.getContextPath().startsWith( CONTEXT_PATH ) ) {
      return;
    }

    if ( log.isDebug() ) {
      logDebug( BaseMessages.getString( PKG, "GetJobStatusServlet.Log.JobStatusRequested" ) );
    }

    String jobName = request.getParameter( "name" );
    String id = request.getParameter( "id" );
    boolean useXML = "Y".equalsIgnoreCase( request.getParameter( "xml" ) );
    int startLineNr = Const.toInt( request.getParameter( "from" ), 0 );

    response.setStatus( HttpServletResponse.SC_OK );

    if ( useXML ) {
      response.setContentType( "text/xml" );
      response.setCharacterEncoding( Const.XML_ENCODING );
    } else {
      response.setContentType( "text/html;charset=UTF-8" );
    }

    PrintWriter out = response.getWriter();

    // ID is optional...
    //
    Job job;
    CarteObjectEntry entry;
    if ( Const.isEmpty( id ) ) {
      // get the first job that matches...
      //
      entry = getJobMap().getFirstCarteObjectEntry( jobName );
      if ( entry == null ) {
        job = null;
      } else {
        id = entry.getId();
        job = getJobMap().getJob( entry );
      }
    } else {
      // Actually, just providing the ID should be enough to identify the job
      //
      if ( Const.isEmpty( jobName ) ) {
        // Take the ID into account!
        //
        job = getJobMap().findJob( id );
      } else {
        entry = new CarteObjectEntry( jobName, id );
        job = getJobMap().getJob( entry );
        if ( job != null ) {
          jobName = job.getJobname();
        }
      }
    }

    Encoder encoder = ESAPI.encoder();

    if ( job != null ) {
      String status = job.getStatus();
      int lastLineNr = KettleLogStore.getLastBufferLineNr();
      String logText =
        KettleLogStore.getAppender().getBuffer(
          job.getLogChannel().getLogChannelId(), false, startLineNr, lastLineNr ).toString();

      if ( useXML ) {
        response.setContentType( "text/xml" );
        response.setCharacterEncoding( Const.XML_ENCODING );
        out.print( XMLHandler.getXMLHeader( Const.XML_ENCODING ) );

        SlaveServerJobStatus jobStatus = new SlaveServerJobStatus( jobName, id, status );
        jobStatus.setFirstLoggingLineNr( startLineNr );
        jobStatus.setLastLoggingLineNr( lastLineNr );

        // The log can be quite large at times, we are going to put a base64 encoding around a compressed stream
        // of bytes to handle this one.
        String loggingString = HttpUtil.encodeBase64ZippedString( logText );
        jobStatus.setLoggingString( loggingString );

        // Also set the result object...
        //
        jobStatus.setResult( job.getResult() ); // might be null

        try {
          out.println( jobStatus.getXML() );
        } catch ( KettleException e ) {
          throw new ServletException( "Unable to get the job status in XML format", e );
        }
      } else {
        response.setContentType( "text/html" );

        out.println( "<HTML>" );
        out.println( "<HEAD>" );
        out
          .println( "<TITLE>"
            + BaseMessages.getString( PKG, "GetJobStatusServlet.KettleJobStatus" ) + "</TITLE>" );
        out.println( "<META http-equiv=\"Refresh\" content=\"10;url="
          + convertContextPath( GetJobStatusServlet.CONTEXT_PATH ) + "?name="
          + URLEncoder.encode( Const.NVL( jobName, "" ), "UTF-8" ) + "&id=" + URLEncoder.encode( id, "UTF-8" )
          + "\">" );
        out.println( "<META http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">" );
        out.println( "</HEAD>" );
        out.println( "<BODY>" );
        out.println( "<H1>" + BaseMessages.getString( PKG, "GetJobStatusServlet.JobStatus" ) + "</H1>" );

        try {
          out.println( "<table border=\"1\">" );
          out.print( "<tr> <th>"
            + BaseMessages.getString( PKG, "GetJobStatusServlet.Jobname" ) + "</th> <th>"
            + BaseMessages.getString( PKG, "TransStatusServlet.TransStatus" ) + "</th> </tr>" );

          out.print( "<tr>" );
          out.print( "<td>" + Const.NVL( encoder.encodeForHTML( jobName ), "" ) + "</td>" );
          out.print( "<td>" + status + "</td>" );
          out.print( "</tr>" );
          out.print( "</table>" );

          out.print( "<p>" );

          // Show job image?
          //
          Point max = job.getJobMeta().getMaximum();
          max.x += 20;
          max.y += 20;
          out
            .print( "<iframe height=\""
              + max.y + "\" width=\"" + max.x + "\" seamless src=\""
              + convertContextPath( GetJobImageServlet.CONTEXT_PATH ) + "?name="
              + URLEncoder.encode( jobName, "UTF-8" ) + "&id=" + URLEncoder.encode( id, "UTF-8" )
              + "\"></iframe>" );
          out.print( "<p>" );

          // out.print("<a href=\"" + convertContextPath(GetJobImageServlet.CONTEXT_PATH) + "?name=" +
          // URLEncoder.encode(Const.NVL(jobName, ""), "UTF-8") + "&id="+id+"\">"
          // + BaseMessages.getString(PKG, "GetJobImageServlet.GetJobImage") + "</a>");
          // out.print("<p>");

          if ( job.isFinished() ) {
            out.print( "<a href=\""
              + convertContextPath( StartJobServlet.CONTEXT_PATH ) + "?name="
              + URLEncoder.encode( Const.NVL( jobName, "" ), "UTF-8" ) + "&id="
              + URLEncoder.encode( id, "UTF-8" ) + "\">"
              + BaseMessages.getString( PKG, "GetJobStatusServlet.StartJob" ) + "</a>" );
            out.print( "<p>" );
          } else {
            out.print( "<a href=\""
              + convertContextPath( StopJobServlet.CONTEXT_PATH ) + "?name="
              + URLEncoder.encode( Const.NVL( jobName, "" ), "UTF-8" ) + "&id="
              + URLEncoder.encode( id, "UTF-8" ) + "\">"
              + BaseMessages.getString( PKG, "GetJobStatusServlet.StopJob" ) + "</a>" );
            out.print( "<p>" );
          }

          out.println( "<p>" );

          out.print( "<a href=\""
            + convertContextPath( GetJobStatusServlet.CONTEXT_PATH ) + "?name="
            + URLEncoder.encode( Const.NVL( jobName, "" ), "UTF-8" ) + "&xml=y&id="
            + URLEncoder.encode( id, "UTF-8" ) + "\">"
            + BaseMessages.getString( PKG, "TransStatusServlet.ShowAsXml" ) + "</a><br>" );
          out.print( "<a href=\""
            + convertContextPath( GetStatusServlet.CONTEXT_PATH ) + "\">"
            + BaseMessages.getString( PKG, "TransStatusServlet.BackToStatusPage" ) + "</a><br>" );
          out.print( "<p><a href=\""
            + convertContextPath( GetJobStatusServlet.CONTEXT_PATH ) + "?name="
            + URLEncoder.encode( Const.NVL( jobName, "" ), "UTF-8" ) + "&id=" + URLEncoder.encode( id, "UTF-8" )
            + "\">" + BaseMessages.getString( PKG, "TransStatusServlet.Refresh" ) + "</a>" );

          // Put the logging below that.

          out.println( "<p>" );
          out.println( "<textarea id=\"joblog\" cols=\"120\" rows=\"20\" wrap=\"off\" "
            + "name=\"Job log\" readonly=\"readonly\">"
            + encoder.encodeForHTML( logText ) + "</textarea>" );

          out.println( "<script type=\"text/javascript\"> " );
          out.println( "  joblog.scrollTop=joblog.scrollHeight; " );
          out.println( "</script> " );
          out.println( "<p>" );
        } catch ( Exception ex ) {
          out.println( "<p>" );
          out.println( "<pre>" );
          out.println( encoder.encodeForHTML( Const.getStackTracker( ex ) ) );
          out.println( "</pre>" );
        }

        out.println( "<p>" );
        out.println( "</BODY>" );
        out.println( "</HTML>" );
      }
    } else {
      if ( useXML ) {
        out.println( new WebResult( WebResult.STRING_ERROR, BaseMessages.getString(
          PKG, "StartJobServlet.Log.SpecifiedJobNotFound", jobName, id ) ) );
      } else {
        out.println( "<H1>Job '" + encoder.encodeForHTML( jobName ) + "' could not be found.</H1>" );
        out.println( "<a href=\""
          + convertContextPath( GetStatusServlet.CONTEXT_PATH ) + "\">"
          + BaseMessages.getString( PKG, "TransStatusServlet.BackToStatusPage" ) + "</a><p>" );
      }
    }
  }

  public String toString() {
    return "Job Status Handler";
  }

  public String getService() {
    return CONTEXT_PATH + " (" + toString() + ")";
  }

  public String getContextPath() {
    return CONTEXT_PATH;
  }

}
