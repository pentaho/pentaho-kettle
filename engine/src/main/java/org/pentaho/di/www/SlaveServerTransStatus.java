/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.pentaho.di.cluster.HttpUtil;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.EnvUtil;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepStatus;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class SlaveServerTransStatus {
  public static final String XML_TAG = "transstatus";

  private String id;

  private String transName;

  private String statusDescription;

  private String errorDescription;

  private String loggingString;

  private int firstLoggingLineNr;

  private int lastLoggingLineNr;

  private Date logDate;

  private List<StepStatus> stepStatusList;

  private Result result;

  private boolean paused;

  public SlaveServerTransStatus() {
    stepStatusList = new ArrayList<StepStatus>();
  }

  /**
   * @param transName
   * @param statusDescription
   */
  public SlaveServerTransStatus( String transName, String id, String statusDescription ) {
    this();
    this.transName = transName;
    this.id = id;
    this.statusDescription = statusDescription;
  }

  public String getXML() throws KettleException {
    // See PDI-15781
    boolean sendResultXmlWithStatus = EnvUtil.getSystemProperty( "KETTLE_COMPATIBILITY_SEND_RESULT_XML_WITH_FULL_STATUS", "N" ).equalsIgnoreCase( "Y" );
    return getXML( sendResultXmlWithStatus );
  }

  public String getXML( boolean sendResultXmlWithStatus ) throws KettleException {
    StringBuilder xml = new StringBuilder();

    xml.append( XMLHandler.openTag( XML_TAG ) ).append( Const.CR );
    xml.append( "  " ).append( XMLHandler.addTagValue( "transname", transName ) );
    xml.append( "  " ).append( XMLHandler.addTagValue( "id", id ) );
    xml.append( "  " ).append( XMLHandler.addTagValue( "status_desc", statusDescription ) );
    xml.append( "  " ).append( XMLHandler.addTagValue( "error_desc", errorDescription ) );
    xml.append( "  " ).append( XMLHandler.addTagValue( "log_date", XMLHandler.date2string( logDate ) ) );
    xml.append( "  " ).append( XMLHandler.addTagValue( "paused", paused ) );

    xml.append( "  " ).append( XMLHandler.openTag( "stepstatuslist" ) ).append( Const.CR );
    for ( int i = 0; i < stepStatusList.size(); i++ ) {
      StepStatus stepStatus = stepStatusList.get( i );
      xml.append( "    " ).append( stepStatus.getXML() ).append( Const.CR );
    }
    xml.append( "  " ).append( XMLHandler.closeTag( "stepstatuslist" ) ).append( Const.CR );

    xml.append( "  " ).append( XMLHandler.addTagValue( "first_log_line_nr", firstLoggingLineNr ) );
    xml.append( "  " ).append( XMLHandler.addTagValue( "last_log_line_nr", lastLoggingLineNr ) );

    if ( result != null ) {
      String resultXML = sendResultXmlWithStatus ? result.getXML() : result.getBasicXml();
      xml.append( resultXML );
    }

    xml.append( "  " ).append( XMLHandler.addTagValue( "logging_string", XMLHandler.buildCDATA( loggingString ) ) );

    xml.append( XMLHandler.closeTag( XML_TAG ) );

    return xml.toString();
  }

  public SlaveServerTransStatus( Node transStatusNode ) throws KettleException {
    this();
    id = XMLHandler.getTagValue( transStatusNode, "id" );
    transName = XMLHandler.getTagValue( transStatusNode, "transname" );
    statusDescription = XMLHandler.getTagValue( transStatusNode, "status_desc" );
    errorDescription = XMLHandler.getTagValue( transStatusNode, "error_desc" );
    logDate = XMLHandler.stringToDate( XMLHandler.getTagValue( transStatusNode, "log_date" ) );
    paused = "Y".equalsIgnoreCase( XMLHandler.getTagValue( transStatusNode, "paused" ) );

    Node statusListNode = XMLHandler.getSubNode( transStatusNode, "stepstatuslist" );
    int nr = XMLHandler.countNodes( statusListNode, StepStatus.XML_TAG );
    for ( int i = 0; i < nr; i++ ) {
      Node stepStatusNode = XMLHandler.getSubNodeByNr( statusListNode, StepStatus.XML_TAG, i );
      StepStatus stepStatus = new StepStatus( stepStatusNode );
      stepStatusList.add( stepStatus );
    }

    firstLoggingLineNr = Const.toInt( XMLHandler.getTagValue( transStatusNode, "first_log_line_nr" ), 0 );
    lastLoggingLineNr = Const.toInt( XMLHandler.getTagValue( transStatusNode, "last_log_line_nr" ), 0 );

    String loggingString64 = XMLHandler.getTagValue( transStatusNode, "logging_string" );

    if ( !Utils.isEmpty( loggingString64 ) ) {
      // This is a CDATA block with a Base64 encoded GZIP compressed stream of data.
      //
      String dataString64 =
        loggingString64.substring( "<![CDATA[".length(), loggingString64.length() - "]]>".length() );
      try {
        loggingString = HttpUtil.decodeBase64ZippedString( dataString64 );
      } catch ( IOException e ) {
        loggingString =
          "Unable to decode logging from remote server : " + e.toString() + Const.CR + Const.getStackTracker( e );
      }
    } else {
      loggingString = "";
    }

    // get the result object, if there is any...
    //
    Node resultNode = XMLHandler.getSubNode( transStatusNode, Result.XML_TAG );
    if ( resultNode != null ) {
      try {
        result = new Result( resultNode );
      } catch ( KettleException e ) {
        loggingString +=
          "Unable to serialize result object as XML" + Const.CR + Const.getStackTracker( e ) + Const.CR;
      }
      result.setLogText( loggingString );
    }
  }

  public static SlaveServerTransStatus fromXML( String xml ) throws KettleException {
    Document document = XMLHandler.loadXMLString( xml );
    SlaveServerTransStatus status = new SlaveServerTransStatus( XMLHandler.getSubNode( document, XML_TAG ) );
    return status;
  }

  /**
   * @return the statusDescription
   */
  public String getStatusDescription() {
    return statusDescription;
  }

  /**
   * @param statusDescription
   *          the statusDescription to set
   */
  public void setStatusDescription( String statusDescription ) {
    this.statusDescription = statusDescription;
  }

  /**
   * @return the transName
   */
  public String getTransName() {
    return transName;
  }

  /**
   * @param transName
   *          the transName to set
   */
  public void setTransName( String transName ) {
    this.transName = transName;
  }

  /**
   * @return the errorDescription
   */
  public String getErrorDescription() {
    return errorDescription;
  }

  /**
   * @param errorDescription
   *          the errorDescription to set
   */
  public void setErrorDescription( String errorDescription ) {
    this.errorDescription = errorDescription;
  }

  /**
   * @return the stepStatusList
   */
  public List<StepStatus> getStepStatusList() {
    return stepStatusList;
  }

  /**
   * @param stepStatusList
   *          the stepStatusList to set
   */
  public void setStepStatusList( List<StepStatus> stepStatusList ) {
    this.stepStatusList = stepStatusList;
  }

  /**
   * @return the loggingString
   */
  public String getLoggingString() {
    return loggingString;
  }

  /**
   * @param loggingString
   *          the loggingString to set
   */
  public void setLoggingString( String loggingString ) {
    this.loggingString = loggingString;
  }

  public boolean isRunning() {
    return getStatusDescription().equalsIgnoreCase( Trans.STRING_RUNNING )
      || getStatusDescription().equalsIgnoreCase( Trans.STRING_INITIALIZING );
  }

  public boolean isStopped() {
    return getStatusDescription().equalsIgnoreCase( Trans.STRING_STOPPED );
  }

  public boolean isWaiting() {
    return getStatusDescription().equalsIgnoreCase( Trans.STRING_WAITING );
  }

  public long getNrStepErrors() {
    long errors = 0L;
    for ( int i = 0; i < stepStatusList.size(); i++ ) {
      StepStatus stepStatus = stepStatusList.get( i );
      errors += stepStatus.getErrors();
    }
    return errors;
  }

  public Result getResult( TransMeta transMeta ) {
    Result result = new Result();

    for ( StepStatus stepStatus : stepStatusList ) {

      result.setNrErrors( result.getNrErrors() + stepStatus.getErrors() + ( result.isStopped() ? 1 : 0 ) ); // If the
                                                                                                            // remote
                                                                                                            // trans is
                                                                                                            // stopped,
                                                                                                            // count as
                                                                                                            // an error

      if ( stepStatus.getStepname().equals( transMeta.getTransLogTable().getStepnameRead() ) ) {
        result.increaseLinesRead( stepStatus.getLinesRead() );
      }
      if ( stepStatus.getStepname().equals( transMeta.getTransLogTable().getStepnameInput() ) ) {
        result.increaseLinesInput( stepStatus.getLinesInput() );
      }
      if ( stepStatus.getStepname().equals( transMeta.getTransLogTable().getStepnameWritten() ) ) {
        result.increaseLinesWritten( stepStatus.getLinesWritten() );
      }
      if ( stepStatus.getStepname().equals( transMeta.getTransLogTable().getStepnameOutput() ) ) {
        result.increaseLinesOutput( stepStatus.getLinesOutput() );
      }
      if ( stepStatus.getStepname().equals( transMeta.getTransLogTable().getStepnameUpdated() ) ) {
        result.increaseLinesUpdated( stepStatus.getLinesUpdated() );
      }
      if ( stepStatus.getStepname().equals( transMeta.getTransLogTable().getStepnameRejected() ) ) {
        result.increaseLinesRejected( stepStatus.getLinesRejected() );
      }

      if ( stepStatus.isStopped() ) {
        result.setStopped( true );
        result.setResult( false );
      }
    }

    return result;
  }

  /**
   * @return the result
   */
  public Result getResult() {
    return result;
  }

  /**
   * @param result
   *          the result to set
   */
  public void setResult( Result result ) {
    this.result = result;
  }

  /**
   * @return the paused
   */
  public boolean isPaused() {
    return paused;
  }

  /**
   * @param paused
   *          the paused to set
   */
  public void setPaused( boolean paused ) {
    this.paused = paused;
  }

  /**
   * @return the lastLoggingLineNr
   */
  public int getLastLoggingLineNr() {
    return lastLoggingLineNr;
  }

  /**
   * @param lastLoggingLineNr
   *          the lastLoggingLineNr to set
   */
  public void setLastLoggingLineNr( int lastLoggingLineNr ) {
    this.lastLoggingLineNr = lastLoggingLineNr;
  }

  /**
   * @return the firstLoggingLineNr
   */
  public int getFirstLoggingLineNr() {
    return firstLoggingLineNr;
  }

  /**
   * @param firstLoggingLineNr
   *          the firstLoggingLineNr to set
   */
  public void setFirstLoggingLineNr( int firstLoggingLineNr ) {
    this.firstLoggingLineNr = firstLoggingLineNr;
  }

  /**
   * @return the logDate
   */
  public Date getLogDate() {
    return logDate;
  }

  /**
   * @param the logDate
   */
  public void setLogDate( Date logDate ) {
    this.logDate = logDate;
  }

  /**
   * @return the id
   */
  public String getId() {
    return id;
  }

  /**
   * @param id
   *          the id to set
   */
  public void setId( String id ) {
    this.id = id;
  }
}
