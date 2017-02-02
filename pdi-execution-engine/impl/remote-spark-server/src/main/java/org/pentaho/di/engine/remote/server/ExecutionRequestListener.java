package org.pentaho.di.engine.remote.server;

import org.osgi.framework.ServiceReference;
import org.pentaho.di.engine.api.remote.ExecutionRequest;
import org.pentaho.di.engine.api.remote.Notification;
import org.pentaho.di.engine.api.reporting.Status;
import org.pentaho.di.engine.api.reporting.StatusEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by nbaker on 2/1/17.
 */
public class ExecutionRequestListener {

  Logger logger = LoggerFactory.getLogger( getClass() );

  public void requestAdded( ServiceReference requestReference ) {
    // Fire up Spark submit
    logger.info( "Received PDI Execution Request" );
    ExecutionRequest request =
      (ExecutionRequest) requestReference.getBundle().getBundleContext().getService( requestReference );
    request.update( new Notification( "ID: Will get from ServiceProperties in the future", Notification.Type.CLAIM) );
    request.update( request.getTransformation().getId(), Status.FINISHED );
    request.update( new Notification( "ID: Will get from ServiceProperties in the future", Notification.Type.CLOSE) );
  }


  public void requestRemoved( ExecutionRequest request ) {

  }
}