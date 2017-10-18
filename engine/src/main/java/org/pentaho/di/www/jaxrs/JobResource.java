/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.www.jaxrs;

import java.util.UUID;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.core.logging.LoggingObjectType;
import org.pentaho.di.core.logging.SimpleLoggingObject;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobAdapter;
import org.pentaho.di.job.JobConfiguration;
import org.pentaho.di.job.JobExecutionConfiguration;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.www.CarteObjectEntry;
import org.pentaho.di.www.CarteSingleton;

@Path( "/carte/job" )
public class JobResource {

  public JobResource() {
  }

  @GET
  @Path( "/log/{id : .+}" )
  @Produces( { MediaType.TEXT_PLAIN } )
  public String getJobLog( @PathParam( "id" ) String id ) {
    return getJobLog( id, 0 );
  }

  @GET
  @Path( "/log/{id : .+}/{logStart : .+}" )
  @Produces( { MediaType.TEXT_PLAIN } )
  public String getJobLog( @PathParam( "id" ) String id, @PathParam( "logStart" ) int startLineNr ) {
    int lastLineNr = KettleLogStore.getLastBufferLineNr();
    Job job = CarteResource.getJob( id );
    String logText =
      KettleLogStore.getAppender().getBuffer(
        job.getLogChannel().getLogChannelId(), false, startLineNr, lastLineNr ).toString();
    return logText;
  }

  @GET
  @Path( "/status/{id : .+}" )
  @Produces( { MediaType.APPLICATION_JSON } )
  public JobStatus getJobStatus( @PathParam( "id" ) String id ) {
    JobStatus status = new JobStatus();
    // find job
    Job job = CarteResource.getJob( id );
    CarteObjectEntry entry = CarteResource.getCarteObjectEntry( id );

    status.setId( entry.getId() );
    status.setName( entry.getName() );
    status.setStatus( job.getStatus() );

    return status;
  }

  // change from GET to UPDATE/POST for proper REST method
  @GET
  @Path( "/start/{id : .+}" )
  @Produces( { MediaType.APPLICATION_JSON } )
  public JobStatus startJob( @PathParam( "id" ) String id ) {
    Job job = CarteResource.getJob( id );
    CarteObjectEntry entry = CarteResource.getCarteObjectEntry( id );
    try {
      if ( job.isInitialized() && !job.isActive() ) {
        // Re-create the job from the jobMeta
        //
        // We might need to re-connect to the repository
        //
        if ( job.getRep() != null && !job.getRep().isConnected() ) {
          if ( job.getRep().getUserInfo() != null ) {
            job.getRep().connect( job.getRep().getUserInfo().getLogin(), job.getRep().getUserInfo().getPassword() );
          } else {
            job.getRep().connect( null, null );
          }
        }

        // Create a new job object to start from a sane state. Then replace
        // the new job in the job map
        //
        synchronized ( this ) {
          JobConfiguration jobConfiguration = CarteSingleton.getInstance().getJobMap().getConfiguration( entry );

          String carteObjectId = UUID.randomUUID().toString();
          SimpleLoggingObject servletLoggingObject =
            new SimpleLoggingObject( getClass().getName(), LoggingObjectType.CARTE, null );
          servletLoggingObject.setContainerObjectId( carteObjectId );

          Job newJob = new Job( job.getRep(), job.getJobMeta(), servletLoggingObject );
          newJob.setLogLevel( job.getLogLevel() );

          // Discard old log lines from the old job
          //
          KettleLogStore.discardLines( job.getLogChannelId(), true );

          CarteSingleton.getInstance().getJobMap().replaceJob( entry, newJob, jobConfiguration );
          job = newJob;
        }
      }
      job.start();
    } catch ( KettleException e ) {
      e.printStackTrace();
    }
    return getJobStatus( id );
  }

  @GET
  @Path( "/stop/{id : .+}" )
  @Produces( { MediaType.APPLICATION_JSON } )
  public JobStatus stopJob( @PathParam( "id" ) String id ) {
    Job job = CarteResource.getJob( id );
    job.stopAll();
    return getJobStatus( id );
  }

  @GET
  @Path( "/remove/{id : .+}" )
  public Response removeJob( @PathParam( "id" ) String id ) {
    Job job = CarteResource.getJob( id );
    CarteObjectEntry entry = CarteResource.getCarteObjectEntry( id );
    KettleLogStore.discardLines( job.getLogChannelId(), true );
    CarteSingleton.getInstance().getJobMap().removeJob( entry );
    return Response.ok().build();
  }

  @PUT
  @Path( "/add" )
  @Produces( { MediaType.APPLICATION_JSON } )
  public JobStatus addJob( String xml ) {

    // Parse the XML, create a job configuration
    //
    // System.out.println(xml);
    //
    JobConfiguration jobConfiguration;
    try {
      jobConfiguration = JobConfiguration.fromXML( xml.toString() );
      JobMeta jobMeta = jobConfiguration.getJobMeta();
      JobExecutionConfiguration jobExecutionConfiguration = jobConfiguration.getJobExecutionConfiguration();
      jobMeta.setLogLevel( jobExecutionConfiguration.getLogLevel() );
      jobMeta.injectVariables( jobExecutionConfiguration.getVariables() );

      // If there was a repository, we know about it at this point in time.
      //
      final Repository repository = jobConfiguration.getJobExecutionConfiguration().getRepository();

      String carteObjectId = UUID.randomUUID().toString();
      SimpleLoggingObject servletLoggingObject =
        new SimpleLoggingObject( getClass().getName(), LoggingObjectType.CARTE, null );
      servletLoggingObject.setContainerObjectId( carteObjectId );
      servletLoggingObject.setLogLevel( jobExecutionConfiguration.getLogLevel() );

      // Create the transformation and store in the list...
      //
      final Job job = new Job( repository, jobMeta, servletLoggingObject );

      // Setting variables
      //
      job.initializeVariablesFrom( null );
      job.getJobMeta().setInternalKettleVariables( job );
      job.injectVariables( jobConfiguration.getJobExecutionConfiguration().getVariables() );
      job.setArguments( jobExecutionConfiguration.getArgumentStrings() );

      // Also copy the parameters over...
      //
      job.copyParametersFrom( jobMeta );
      job.clearParameters();
      String[] parameterNames = job.listParameters();
      for ( int idx = 0; idx < parameterNames.length; idx++ ) {
        // Grab the parameter value set in the job entry
        //
        String thisValue = jobExecutionConfiguration.getParams().get( parameterNames[idx] );
        if ( !Utils.isEmpty( thisValue ) ) {
          // Set the value as specified by the user in the job entry
          //
          jobMeta.setParameterValue( parameterNames[idx], thisValue );
        }
      }
      jobMeta.activateParameters();

      job.setSocketRepository( CarteSingleton.getInstance().getSocketRepository() );

      CarteSingleton.getInstance().getJobMap().addJob( job.getJobname(), carteObjectId, job, jobConfiguration );

      // Make sure to disconnect from the repository when the job finishes.
      //
      if ( repository != null ) {
        job.addJobListener( new JobAdapter() {
          public void jobFinished( Job job ) {
            repository.disconnect();
          }
        } );
      }
      return getJobStatus( carteObjectId );
    } catch ( KettleException e ) {
      e.printStackTrace();
    }
    return null;
  }
}
