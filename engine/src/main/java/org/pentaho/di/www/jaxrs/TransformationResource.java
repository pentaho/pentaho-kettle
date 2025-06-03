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


package org.pentaho.di.www.jaxrs;

import java.util.Map;
import java.util.UUID;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.logging.LoggingObjectType;
import org.pentaho.di.core.logging.SimpleLoggingObject;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransAdapter;
import org.pentaho.di.trans.TransConfiguration;
import org.pentaho.di.trans.TransExecutionConfiguration;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepData.StepExecutionStatus;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepStatus;
import org.pentaho.di.www.CarteObjectEntry;
import org.pentaho.di.www.CarteSingleton;

@Path( "/carte/trans" )
public class TransformationResource {

  public TransformationResource() {
  }

  @GET
  @Path( "/log/{id : .+}" )
  @Produces( { MediaType.TEXT_PLAIN } )
  public String getTransformationLog( @PathParam( "id" ) String id ) {
    return getTransformationLog( id, 0 );
  }

  @GET
  @Path( "/log/{id : .+}/{logStart : .+}" )
  @Produces( { MediaType.TEXT_PLAIN } )
  public String getTransformationLog( @PathParam( "id" ) String id, @PathParam( "logStart" ) int startLineNr ) {
    int lastLineNr = KettleLogStore.getLastBufferLineNr();
    Trans trans = CarteResource.getTransformation( id );
    String logText =
      KettleLogStore.getAppender().getBuffer(
        trans.getLogChannel().getLogChannelId(), false, startLineNr, lastLineNr ).toString();
    return logText;
  }

  @GET
  @Path( "/status/{id : .+}" )
  @Produces( { MediaType.APPLICATION_JSON } )
  public TransformationStatus getTransformationStatus( @PathParam( "id" ) String id ) {
    TransformationStatus status = new TransformationStatus();
    // find trans
    Trans trans = CarteResource.getTransformation( id );
    CarteObjectEntry entry = CarteResource.getCarteObjectEntry( id );

    status.setId( entry.getId() );
    status.setName( entry.getName() );
    status.setStatus( trans.getStatus() );

    for ( int i = 0; i < trans.nrSteps(); i++ ) {
      StepInterface step = trans.getRunThread( i );
      if ( ( step.isRunning() ) || step.getStatus() != StepExecutionStatus.STATUS_EMPTY ) {
        StepStatus stepStatus = new StepStatus( step );
        status.addStepStatus( stepStatus );
      }
    }
    return status;
  }

  // change from GET to UPDATE/POST for proper REST method
  @GET
  @Path( "/start/{id : .+}" )
  @Produces( { MediaType.APPLICATION_JSON } )
  public TransformationStatus startTransformation( @PathParam( "id" ) String id ) {
    Trans trans = CarteResource.getTransformation( id );
    try {
      // Discard old log lines from old transformation runs
      //
      KettleLogStore.discardLines( trans.getLogChannelId(), true );

      String carteObjectId = UUID.randomUUID().toString();
      SimpleLoggingObject servletLoggingObject =
        new SimpleLoggingObject( getClass().getName(), LoggingObjectType.CARTE, null );
      servletLoggingObject.setContainerObjectId( carteObjectId );
      servletLoggingObject.setLogLevel( trans.getLogLevel() );
      trans.setParent( servletLoggingObject );
      trans.execute( null );
    } catch ( KettleException e ) {
      e.printStackTrace();
    }
    return getTransformationStatus( id );
  }

  // change from GET to UPDATE/POST for proper REST method
  @GET
  @Path( "/prepare/{id : .+}" )
  @Produces( { MediaType.APPLICATION_JSON } )
  public TransformationStatus prepareTransformation( @PathParam( "id" ) String id ) {
    Trans trans = CarteResource.getTransformation( id );
    try {

      CarteObjectEntry entry = CarteResource.getCarteObjectEntry( id );
      TransConfiguration transConfiguration =
        CarteSingleton.getInstance().getTransformationMap().getConfiguration( entry );
      TransExecutionConfiguration executionConfiguration = transConfiguration.getTransExecutionConfiguration();
      // Set the appropriate logging, variables, arguments, replay date, ...
      // etc.
      trans.setArguments( executionConfiguration.getArgumentStrings() );
      trans.setReplayDate( executionConfiguration.getReplayDate() );
      trans.setSafeModeEnabled( executionConfiguration.isSafeModeEnabled() );
      trans.setGatheringMetrics( executionConfiguration.isGatheringMetrics() );
      trans.injectVariables( executionConfiguration.getVariables() );
      trans.setPreviousResult( executionConfiguration.getPreviousResult() );

      trans.prepareExecution( null );
    } catch ( KettleException e ) {
      e.printStackTrace();
    }
    return getTransformationStatus( id );
  }

  // change from GET to UPDATE/POST for proper REST method
  @GET
  @Path( "/pause/{id : .+}" )
  @Produces( { MediaType.APPLICATION_JSON } )
  public TransformationStatus pauseTransformation( @PathParam( "id" ) String id ) {
    CarteResource.getTransformation( id ).pauseRunning();
    return getTransformationStatus( id );
  }

  // change from GET to UPDATE/POST for proper REST method
  @GET
  @Path( "/resume/{id : .+}" )
  @Produces( { MediaType.APPLICATION_JSON } )
  public TransformationStatus resumeTransformation( @PathParam( "id" ) String id ) {
    CarteResource.getTransformation( id ).resumeRunning();
    return getTransformationStatus( id );
  }

  // change from GET to UPDATE/POST for proper REST method
  @GET
  @Path( "/stop/{id : .+}" )
  @Produces( { MediaType.APPLICATION_JSON } )
  public TransformationStatus stopTransformation( @PathParam( "id" ) String id ) {
    CarteResource.getTransformation( id ).stopAll();
    return getTransformationStatus( id );
  }

  // change from GET to UPDATE/POST for proper REST method
  @GET
  @Path( "/safeStop/{id : .+}" )
  @Produces( { MediaType.APPLICATION_JSON } )
  public TransformationStatus safeStopTransformation( @PathParam( "id" ) String id ) {
    CarteResource.getTransformation( id ).safeStop();
    return getTransformationStatus( id );
  }

  // change from GET to UPDATE/POST for proper REST method
  @GET
  @Path( "/remove/{id : .+}" )
  public Response removeTransformation( @PathParam( "id" ) String id ) {
    Trans trans = CarteResource.getTransformation( id );
    CarteObjectEntry entry = CarteResource.getCarteObjectEntry( id );
    KettleLogStore.discardLines( trans.getLogChannelId(), true );
    CarteSingleton.getInstance().getTransformationMap().removeTransformation( entry );
    return Response.ok().build();
  }

  // change from GET to UPDATE/POST for proper REST method
  @GET
  @Path( "/cleanup/{id : .+}" )
  @Produces( { MediaType.APPLICATION_JSON } )
  public TransformationStatus cleanupTransformation( @PathParam( "id" ) String id ) {
    CarteResource.getTransformation( id ).cleanup();
    return getTransformationStatus( id );
  }

  @PUT
  @Path( "/add" )
  @Produces( { MediaType.APPLICATION_JSON } )
  public TransformationStatus addTransformation( String xml ) {
    TransConfiguration transConfiguration;
    try {
      transConfiguration = TransConfiguration.fromXML( xml.toString() );
      TransMeta transMeta = transConfiguration.getTransMeta();
      TransExecutionConfiguration transExecutionConfiguration =
        transConfiguration.getTransExecutionConfiguration();
      transMeta.setLogLevel( transExecutionConfiguration.getLogLevel() );
      LogChannelInterface log = CarteSingleton.getInstance().getLog();
      if ( log.isDetailed() ) {
        log.logDetailed( "Logging level set to " + log.getLogLevel().getDescription() );
      }
      transMeta.injectVariables( transExecutionConfiguration.getVariables() );

      // Also copy the parameters over...
      //
      Map<String, String> params = transExecutionConfiguration.getParams();
      for ( Map.Entry<String, String> entry : params.entrySet() ) {
        transMeta.setParameterValue( entry.getKey(), entry.getValue() );
      }

      // If there was a repository, we know about it at this point in time.
      //
      TransExecutionConfiguration executionConfiguration = transConfiguration.getTransExecutionConfiguration();
      final Repository repository = transConfiguration.getTransExecutionConfiguration().getRepository();

      String carteObjectId = UUID.randomUUID().toString();
      SimpleLoggingObject servletLoggingObject =
        new SimpleLoggingObject( getClass().getName(), LoggingObjectType.CARTE, null );
      servletLoggingObject.setContainerObjectId( carteObjectId );
      servletLoggingObject.setLogLevel( executionConfiguration.getLogLevel() );

      // Create the transformation and store in the list...
      //
      final Trans trans = new Trans( transMeta, servletLoggingObject );

      trans.setRepository( repository );
      trans.setSocketRepository( CarteSingleton.getInstance().getSocketRepository() );

      CarteSingleton.getInstance().getTransformationMap().addTransformation(
        transMeta.getName(), carteObjectId, trans, transConfiguration );
      trans.setContainerObjectId( carteObjectId );

      if ( repository != null ) {
        // The repository connection is open: make sure we disconnect from the repository once we
        // are done with this transformation.
        //
        trans.addTransListener( new TransAdapter() {
          @Override public void transFinished( Trans trans ) {
            repository.disconnect();
          }
        } );
      }

      return getTransformationStatus( carteObjectId );
    } catch ( KettleException e ) {
      e.printStackTrace();
    }
    return null;
  }

}
