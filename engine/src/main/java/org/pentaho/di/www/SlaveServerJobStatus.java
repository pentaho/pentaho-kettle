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

package org.pentaho.di.www;

import java.io.IOException;
import java.util.Date;

import org.pentaho.di.cluster.HttpUtil;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.EnvUtil;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.trans.Trans;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class SlaveServerJobStatus {
  public static final String XML_TAG = "jobstatus";

  private String jobName;
  private String id;
  private String statusDescription;
  private String errorDescription;
  private String loggingString;
  private int firstLoggingLineNr;
  private int lastLoggingLineNr;
  private Date logDate;

  private Result result;

  public SlaveServerJobStatus() {
  }

  /**
   * @param transName
   * @param statusDescription
   */
  public SlaveServerJobStatus( String transName, String id, String statusDescription ) {
    this();
    this.jobName = transName;
    this.id = id;
    this.statusDescription = statusDescription;
  }

  public String getXML() throws KettleException {
    // See PDI-15781
    boolean sendResultXmlWithStatus = EnvUtil.getSystemProperty( "KETTLE_COMPATIBILITY_SEND_RESULT_XML_WITH_FULL_STATUS", "N" ).equalsIgnoreCase( "Y" );
    StringBuilder xml = new StringBuilder();

    xml.append( XMLHandler.openTag( XML_TAG ) ).append( Const.CR );
    xml.append( "  " ).append( XMLHandler.addTagValue( "jobname", jobName ) );
    xml.append( "  " ).append( XMLHandler.addTagValue( "id", id ) );
    xml.append( "  " ).append( XMLHandler.addTagValue( "status_desc", statusDescription ) );
    xml.append( "  " ).append( XMLHandler.addTagValue( "error_desc", errorDescription ) );
    xml.append( "  " ).append( XMLHandler.addTagValue( "log_date", XMLHandler.date2string( logDate ) ) );
    xml.append( "  " ).append( XMLHandler.addTagValue( "logging_string", XMLHandler.buildCDATA( loggingString ) ) );
    xml.append( "  " ).append( XMLHandler.addTagValue( "first_log_line_nr", firstLoggingLineNr ) );
    xml.append( "  " ).append( XMLHandler.addTagValue( "last_log_line_nr", lastLoggingLineNr ) );

    if ( result != null ) {
      String resultXML = sendResultXmlWithStatus ? result.getXML() : result.getBasicXml();
      xml.append( resultXML );
    }

    xml.append( XMLHandler.closeTag( XML_TAG ) );

    return xml.toString();
  }

  public SlaveServerJobStatus( Node jobStatusNode ) throws KettleException {
    this();
    jobName = XMLHandler.getTagValue( jobStatusNode, "jobname" );
    id = XMLHandler.getTagValue( jobStatusNode, "id" );
    statusDescription = XMLHandler.getTagValue( jobStatusNode, "status_desc" );
    errorDescription = XMLHandler.getTagValue( jobStatusNode, "error_desc" );
    logDate = XMLHandler.stringToDate( XMLHandler.getTagValue( jobStatusNode, "log_date" ) );
    firstLoggingLineNr = Const.toInt( XMLHandler.getTagValue( jobStatusNode, "first_log_line_nr" ), 0 );
    lastLoggingLineNr = Const.toInt( XMLHandler.getTagValue( jobStatusNode, "last_log_line_nr" ), 0 );

    String loggingString64 = XMLHandler.getTagValue( jobStatusNode, "logging_string" );

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
    Node resultNode = XMLHandler.getSubNode( jobStatusNode, Result.XML_TAG );
    if ( resultNode != null ) {
      try {
        result = new Result( resultNode );
      } catch ( KettleException e ) {
        loggingString +=
          "Unable to serialize result object as XML" + Const.CR + Const.getStackTracker( e ) + Const.CR;
      }
    }
  }

  public static SlaveServerJobStatus fromXML( String xml ) throws KettleException {
    Document document = XMLHandler.loadXMLString( xml );
    SlaveServerJobStatus status = new SlaveServerJobStatus( XMLHandler.getSubNode( document, XML_TAG ) );
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
   * @return the job name
   */
  public String getJobName() {
    return jobName;
  }

  /**
   * @param jobName
   *          the job name to set
   */
  public void setJobName( String jobName ) {
    this.jobName = jobName;
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

  public boolean isWaiting() {
    return getStatusDescription().equalsIgnoreCase( Trans.STRING_WAITING );
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
