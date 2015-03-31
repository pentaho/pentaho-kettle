package org.pentaho.di.core.logging;

public interface LogChannelInterfaceFactory {
  public LogChannelInterface create( Object subject );

  public LogChannelInterface create( Object subject, boolean gatheringMetrics );

  public LogChannelInterface create( Object subject, LoggingObjectInterface parentObject );

  public LogChannelInterface create( Object subject, LoggingObjectInterface parentObject, boolean gatheringMetrics );
}
