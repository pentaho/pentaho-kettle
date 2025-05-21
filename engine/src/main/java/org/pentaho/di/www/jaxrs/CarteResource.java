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

import java.util.ArrayList;
import java.util.List;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.pentaho.di.job.Job;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.www.CarteObjectEntry;
import org.pentaho.di.www.CarteSingleton;
import org.pentaho.di.www.SlaveServerConfig;

@Path( "/carte" )
public class CarteResource {

  public CarteResource() {
  }

  public static Trans getTransformation( String id ) {
    return CarteSingleton.getInstance().getTransformationMap().getTransformation( getCarteObjectEntry( id ) );
  }

  public static Job getJob( String id ) {
    return CarteSingleton.getInstance().getJobMap().getJob( getCarteObjectEntry( id ) );
  }

  public static CarteObjectEntry getCarteObjectEntry( String id ) {
    List<CarteObjectEntry> transList =
      CarteSingleton.getInstance().getTransformationMap().getTransformationObjects();
    for ( CarteObjectEntry entry : transList ) {
      if ( entry.getId().equals( id ) ) {
        return entry;
      }
    }
    List<CarteObjectEntry> jobList = CarteSingleton.getInstance().getJobMap().getJobObjects();
    for ( CarteObjectEntry entry : jobList ) {
      if ( entry.getId().equals( id ) ) {
        return entry;
      }
    }
    return null;
  }

  @GET
  @Path( "/systemInfo" )
  @Produces( { MediaType.APPLICATION_JSON } )
  public ServerStatus getSystemInfo() {
    ServerStatus serverStatus = new ServerStatus();
    serverStatus.setStatusDescription( "Online" );
    return serverStatus;
  }

  @GET
  @Path( "/configDetails" )
  @Produces( { MediaType.APPLICATION_JSON } )
  public List<NVPair> getConfigDetails() {
    SlaveServerConfig serverConfig = CarteSingleton.getInstance().getTransformationMap().getSlaveServerConfig();
    List<NVPair> list = new ArrayList<NVPair>();
    list.add( new NVPair( "maxLogLines", "" + serverConfig.getMaxLogLines() ) );
    list.add( new NVPair( "maxLogLinesAge", "" + serverConfig.getMaxLogTimeoutMinutes() ) );
    list.add( new NVPair( "maxObjectsAge", "" + serverConfig.getObjectTimeoutMinutes() ) );
    list.add( new NVPair( "configFile", "" + serverConfig.getFilename() ) );
    return list;
  }

  @GET
  @Path( "/transformations" )
  @Produces( { MediaType.APPLICATION_JSON } )
  public List<CarteObjectEntry> getTransformations() {
    List<CarteObjectEntry> transEntries =
      CarteSingleton.getInstance().getTransformationMap().getTransformationObjects();
    return transEntries;
  }

  @GET
  @Path( "/transformations/detailed" )
  @Produces( { MediaType.APPLICATION_JSON } )
  public List<TransformationStatus> getTransformationsDetails() {
    List<CarteObjectEntry> transEntries =
      CarteSingleton.getInstance().getTransformationMap().getTransformationObjects();

    List<TransformationStatus> details = new ArrayList<TransformationStatus>();

    TransformationResource transRes = new TransformationResource();
    for ( CarteObjectEntry entry : transEntries ) {
      details.add( transRes.getTransformationStatus( entry.getId() ) );
    }
    return details;
  }

  @GET
  @Path( "/jobs" )
  @Produces( { MediaType.APPLICATION_JSON } )
  public List<CarteObjectEntry> getJobs() {
    List<CarteObjectEntry> jobEntries = CarteSingleton.getInstance().getJobMap().getJobObjects();
    return jobEntries;
  }

  @GET
  @Path( "/jobs/detailed" )
  @Produces( { MediaType.APPLICATION_JSON } )
  public List<JobStatus> getJobsDetails() {
    List<CarteObjectEntry> jobEntries = CarteSingleton.getInstance().getJobMap().getJobObjects();

    List<JobStatus> details = new ArrayList<JobStatus>();

    JobResource jobRes = new JobResource();
    for ( CarteObjectEntry entry : jobEntries ) {
      details.add( jobRes.getJobStatus( entry.getId() ) );
    }
    return details;
  }

}
