/*!
 * HITACHI VANTARA PROPRIETARY AND CONFIDENTIAL
 *
 * Copyright 2002 - 2023 Hitachi Vantara. All rights reserved.
 *
 * NOTICE: All information including source code contained herein is, and
 * remains the sole property of Hitachi Vantara and its licensors. The intellectual
 * and technical concepts contained herein are proprietary and confidential
 * to, and are trade secrets of Hitachi Vantara and may be covered by U.S. and foreign
 * patents, or patents in process, and are protected by trade secret and
 * copyright laws. The receipt or possession of this source code and/or related
 * information does not convey or imply any rights to reproduce, disclose or
 * distribute its contents, or to manufacture, use, or sell anything that it
 * may describe, in whole or in part. Any reproduction, modification, distribution,
 * or public display of this information without the express written authorization
 * from Hitachi Vantara is strictly prohibited and in violation of applicable laws and
 * international treaties. Access to the source code contained herein is strictly
 * prohibited to anyone except those individuals and entities who have executed
 * confidentiality and non-disclosure agreements or other agreements with Hitachi Vantara,
 * explicitly covering such access.
 */

package org.pentaho.di.www;

import java.io.IOException;
import java.io.PrintStream;
import java.lang.management.ManagementFactory;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.annotations.CarteServlet;
import org.pentaho.di.core.util.ExecutorUtil;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.Job;

@CarteServlet( id = "StopCarteServlet", name = "StopCarteServlet" )
public class StopCarteServlet extends BaseHttpServlet implements CartePluginInterface {

  private static Class<?> PKG = StopCarteServlet.class;

  private static final long serialVersionUID = -5459379367791045161L;
  public static final String CONTEXT_PATH = "/kettle/stopCarte";
  public static final String REQUEST_ACCEPTED = "request_accepted";
  private final DelayedExecutor delayedExecutor;
  private static boolean acceptJobs = true;
  public StopCarteServlet() {
    this( new DelayedExecutor() );
  }

  public StopCarteServlet( DelayedExecutor delayedExecutor ) {
    this.delayedExecutor = delayedExecutor;
  }

  @Override
  public String getService() {
    return CONTEXT_PATH + " (" + this + ")";
  }
  public static void setAcceptJobs( boolean acceptJobs ) {
    StopCarteServlet.acceptJobs = acceptJobs;
  }
  public static boolean isAcceptJobs() {
    return acceptJobs;
  }

  @Override
  public void doGet( HttpServletRequest request, HttpServletResponse response ) throws IOException {
    if ( isJettyMode() && !request.getContextPath().startsWith( CONTEXT_PATH ) ) {
      return;
    }

    if ( log.isDebug() ) {
      logDebug( BaseMessages.getString( PKG, "StopCarteServlet.shutdownRequest" ) );
    }

    response.setStatus( HttpServletResponse.SC_OK );
    String shutdownType = request.getParameter( "shutdowntype" );
    if ( shutdownType == null ) {
      shutdownType = "Graceful";
    }
    boolean useXML = "Y".equalsIgnoreCase( request.getParameter( "xml" ) );


    PrintStream out = new PrintStream( response.getOutputStream() );
    final Carte carte = CarteSingleton.getCarte();
    Set<String> activeSet = new HashSet<>();
    if ( useXML ) {
      response.setContentType( "text/xml" );
      response.setCharacterEncoding( Const.XML_ENCODING );
      out.print( XMLHandler.getXMLHeader( Const.XML_ENCODING ) );
      out.print( XMLHandler.addTagValue( REQUEST_ACCEPTED, carte != null ) );
      out.flush();
    } else {
      response.setContentType( "text/html" );
      out.println( "<HTML>" );
      out.println(
          "<HEAD><TITLE>" + BaseMessages.getString( PKG, "StopCarteServlet.shutdownRequest" ) + "</TITLE></HEAD>" );
      out.println( "<BODY>" );
      out.println( "<H1>" + BaseMessages.getString( PKG, "StopCarteServlet.status.label" ) +  "</H1>" );
      out.println( "<p>" );
      if ( carte != null ) {
        out.println( BaseMessages.getString( PKG, "StopCarteServlet.shutdownRequest.status.ok" ) );
      } else {
        out.println( BaseMessages.getString( PKG, "StopCarteServlet.shutdownRequest.status.notFound" ) );
      }
      out.println( "</p>" );
      out.println( "</BODY>" );
      out.println( "</HTML>" );
      out.flush();
    }
    if ( carte != null ) {
      switch ( shutdownType ) {
        case "Forceful":
          shutdown( " Proceeding to Forceful Shutdown ", carte );
          break;
        case "Graceful":
          gracefulShutdown( activeSet, carte );
          break;
        default:
          logMinimal( "Specify Shutdown Type" );
          break;
      }
      out.printf( "<a href=\"%s\" target=\"_blank\">Cancel</a><br>%n",
              convertContextPath( CancelGracefulShutdownServlet.CONTEXT_PATH ) );
      out.println( "<br>" );
    }
  }

  @Override
  public String toString() {
    return BaseMessages.getString( PKG, "StopCarteServlet.description" );
  }

  @Override
  public String getContextPath() {
    return CONTEXT_PATH;
  }

  public static class DelayedExecutor {
    public void execute( final Runnable runnable, final long delay ) {
      ExecutorUtil.getExecutor().execute( () -> {
        try {
          Thread.sleep( delay );
        } catch ( InterruptedException e ) {
          // Ignore
        }
        runnable.run();
      } );
    }
  }

  private void shutdown( String logMessage, Carte carte ) {
    delayedExecutor.execute( () -> {
      carte.getWebServer().stopServer();
      logMinimal( logMessage );
      exitJVM( 0 );
    }, 10 );
  }

  private void gracefulShutdown( Set<String> dummySet, Carte carte ) {
    setAcceptJobs( false );
    logMinimal( "acceptJobs" + isAcceptJobs() );
    getSystemLogs();
    ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    executor.scheduleWithFixedDelay( () -> {
      for ( CarteObjectEntry value : getJobMap().getJobObjects() ) {
        String jobID = value.getId();
        Job job = jobMap.findJob( jobID );
        if ( job.isActive() ) {
          dummySet.add( jobID );
        }
      }
      if ( dummySet.isEmpty() ) {
        getSystemLogs();
        shutdown( " Proceeding to Graceful Shutdown ", carte );
      }
      for ( Iterator<String> iterator = dummySet.iterator(); iterator.hasNext();) {
        String id =  iterator.next();
        Job job = jobMap.findJob( id );
        if ( job.isActive() ) {
          log.logMinimal( "JOB ID : " + id + " running / isActive (from thread) , so wait" );
        } else if ( job.isFinished() ) {
          log.logMinimal( "JOB ID : " + id + " removed " );
          iterator.remove();
        }
      } }, 500, 100, TimeUnit.MILLISECONDS );
  }
  private static final void exitJVM( int status ) {
    System.exit( status );
  }
  private String bytesToMb( long bytes ) {
    if ( bytes < 1024 ) {
      return bytes + " B";
    }
    int z = ( 63 - Long.numberOfLeadingZeros( bytes ) ) / 10;
    return String.format( "%.1f %sB", (double) bytes / ( 1L << ( z * 10 ) ), " KMGTPE".charAt( z ) );
  }
  private void getSystemLogs() {
    int numThreads = ManagementFactory.getThreadMXBean().getThreadCount();
    int numProcessors = Runtime.getRuntime().availableProcessors();
    String freeMemoryMb = bytesToMb( Runtime.getRuntime().freeMemory() );
    String maxMemoryMb = bytesToMb( Runtime.getRuntime().maxMemory() );
    String totalMemoryMb = bytesToMb( Runtime.getRuntime().totalMemory() );

    logMinimal( String.format( " num_threads %s", numThreads ) );
    logMinimal( String.format( " num_processors %s", numProcessors ) );
    logMinimal( String.format( " free_memory_mb %s", freeMemoryMb ) );
    logMinimal( String.format( " max_memory_mb %s", maxMemoryMb ) );
    logMinimal( String.format( " total_memory_mb %s", totalMemoryMb ) );
  }
}

