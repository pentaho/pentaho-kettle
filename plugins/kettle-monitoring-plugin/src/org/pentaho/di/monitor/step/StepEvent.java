package org.pentaho.di.monitor.step;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.monitor.IKettleMonitoringEvent;
import org.pentaho.di.trans.step.StepMetaDataCombi;

import java.io.Serializable;

public class StepEvent implements IKettleMonitoringEvent {

  private static final long serialVersionUID = 3171013674678295934L;

  private static final String ID = "1.1.1.1.1.1.1.1"; // TODO replace with an actual oid

  public static enum EventType {BEFORE_INIT, AFTER_INIT, BEFORE_START, FINIHED}

  private Serializable id = ID;
  private EventType eventType;
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

  public StepEvent( EventType eventType ) {
    this.eventType = eventType;
  }

  @Override
  public Serializable getId() {
    return null;
  }

  public void setId( Serializable id ) {
    this.id = id;
  }

  public EventType getEventType() {
    return eventType;
  }

  public void setEventType( EventType eventType ) {
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
    sb.append( ", lines read: " + getLinesRead() + " " );
    sb.append( ", lines written: " + getLinesWritten() + " " );
    sb.append( ", lines updated: " + getLinesUpdated() + " " );
    sb.append( ", lines rejected: " + getLinesRejected() + " " );
    sb.append( ", is clustered: '" + isClustered() + "' " );
    sb.append( ", is distributed: '" + isDistributed() + "' " );

    return sb.toString();
  }
}
