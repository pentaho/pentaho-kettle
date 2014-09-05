package org.pentaho.di.monitor.step;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.monitor.base.BaseEvent;
import org.pentaho.di.monitor.base.EventType;
import org.pentaho.di.trans.step.StepMetaDataCombi;
import org.pentaho.platform.api.monitoring.snmp.SnmpTrapEvent;

import java.io.Serializable;

@SnmpTrapEvent( oid="1.1.1.1.3.1.2.5" )
public class StepEvent extends BaseEvent {

  private static final long serialVersionUID = 3171013674678295934L;

  private EventType.Step eventType;
  private String name;
  private String xmlContent;
  private boolean clustered;
  private boolean distributed;
  private boolean doesErrorHandling;
  private boolean usesThreadPriorityManagement;
  private long linesRead;
  private long linesWritten;
  private long linesUpdated;
  private long linesRejected;
  private long linesInput;
  private long linesOutput;

  public StepEvent( EventType.Step eventType ) {
    this.eventType = eventType;
  }

  @Override
  public Serializable getId() {
    return getName();
  }

  public EventType.Step getEventType() {
    return eventType;
  }

  public void setEventType( EventType.Step eventType ) {
    this.eventType = eventType;
  }

  public String getName() {
    return name;
  }

  public void setName( String name ) {
    this.name = name;
  }

  public String getXmlContent() {
    return xmlContent;
  }

  public void setXmlContent( String xmlContent ) {
    this.xmlContent = xmlContent;
  }

  public boolean isClustered() {
    return clustered;
  }

  public void setClustered( boolean clustered ) {
    this.clustered = clustered;
  }

  public boolean isDistributed() {
    return distributed;
  }

  public void setDistributed( boolean distributed ) {
    this.distributed = distributed;
  }

  public boolean isDoesErrorHandling() {
    return doesErrorHandling;
  }

  public void setDoesErrorHandling( boolean doesErrorHandling ) {
    this.doesErrorHandling = doesErrorHandling;
  }

  public boolean isUsesThreadPriorityManagement() {
    return usesThreadPriorityManagement;
  }

  public void setUsesThreadPriorityManagement( boolean usesThreadPriorityManagement ) {
    this.usesThreadPriorityManagement = usesThreadPriorityManagement;
  }

  public long getLinesRead() {
    return linesRead;
  }

  public void setLinesRead( long linesRead ) {
    this.linesRead = linesRead;
  }

  public long getLinesWritten() {
    return linesWritten;
  }

  public void setLinesWritten( long linesWritten ) {
    this.linesWritten = linesWritten;
  }

  public long getLinesUpdated() {
    return linesUpdated;
  }

  public void setLinesUpdated( long linesUpdated ) {
    this.linesUpdated = linesUpdated;
  }

  public long getLinesRejected() {
    return linesRejected;
  }

  public void setLinesRejected( long linesRejected ) {
    this.linesRejected = linesRejected;
  }

  public long getLinesInput() {
    return linesInput;
  }

  public void setLinesInput( long linesInput ) {
    this.linesInput = linesInput;
  }

  public long getLinesOutput() {
    return linesOutput;
  }

  public void setLinesOutput( long linesOutput ) {
    this.linesOutput = linesOutput;
  }

  public StepEvent build( StepMetaDataCombi combi ) throws KettleException {

    if ( combi == null ) {
      return this;
    }

    setName( combi.stepname );

    if ( combi.stepMeta != null ) {

      setClustered( combi.stepMeta.isClustered() );
      setDistributed( combi.stepMeta.isDistributes() );
      setDoesErrorHandling( combi.stepMeta.isDoingErrorHandling() );
      setXmlContent( combi.stepMeta.getXML() );
    }

    if ( combi.step != null ) {

      setLogChannelId( combi.step.getLogChannel() != null ? combi.step.getLogChannel().getLogChannelId() : null );
      setEventLogs( filterEventLogging( getLogChannelId() ) );
      setLinesInput( combi.step.getLinesInput() );
      setLinesOutput( combi.step.getLinesOutput() );
      setLinesRead( combi.step.getLinesRead() );
      setLinesWritten( combi.step.getLinesWritten() );
      setLinesUpdated( combi.step.getLinesUpdated() );
      setLinesRejected( combi.step.getLinesRejected() );
      setUsesThreadPriorityManagement( combi.step.isUsingThreadPriorityManagment() );
    }

    return this;
  }

  @Override
  public String toString() {

    StringBuffer sb = new StringBuffer( "[" + getClass().getSimpleName() + "]" );
    sb.append( "[" + this.eventType.toString() + "]" );
    sb.append( " Name: '" + getName() + "' " );
    sb.append( " ( " );
    sb.append( " I= " ).append( getLinesInput() ).append( ", " );
    sb.append( " O= " ).append( getLinesOutput() ).append( ", " );
    sb.append( " R= " ).append( getLinesRead() ).append( ", " );
    sb.append( " W= " ).append( getLinesWritten() ).append( ", " );
    sb.append( " U= " ).append( getLinesUpdated() ).append( ", " );
    sb.append( " R= " ).append( getLinesRejected() ).append( ", " );
    sb.append( " ) " );

    return sb.toString();
  }
}
