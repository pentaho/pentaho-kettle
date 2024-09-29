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

package org.pentaho.di.trans.steps.syslog;

import java.util.List;

import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.entries.syslog.SyslogDefs;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.shared.SharedObjectInterface;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Node;

public class SyslogMessageMeta extends BaseStepMeta implements StepMetaInterface {
  private static Class<?> PKG = SyslogMessageMeta.class; // for i18n purposes, needed by Translator2!!

  /** dynamic message fieldname */
  private String messagefieldname;
  private String serverName;
  private String port;
  private String facility;
  private String priority;
  private String datePattern;
  private boolean addTimestamp;
  private boolean addHostName;

  public SyslogMessageMeta() {
    super(); // allocate BaseStepMeta
  }

  public void loadXML( Node stepnode, List<DatabaseMeta> databases, IMetaStore metaStore ) throws KettleXMLException {
    readData( stepnode, databases );
  }

  public Object clone() {
    SyslogMessageMeta retval = (SyslogMessageMeta) super.clone();

    return retval;
  }

  public void setDefault() {
    messagefieldname = null;
    port = String.valueOf( SyslogDefs.DEFAULT_PORT );
    serverName = null;
    facility = SyslogDefs.FACILITYS[0];
    priority = SyslogDefs.PRIORITYS[0];
    datePattern = SyslogDefs.DEFAULT_DATE_FORMAT;
    addTimestamp = true;
    addHostName = true;
  }

  /**
   * @return Returns the serverName.
   */
  public String getServerName() {
    return serverName;
  }

  /**
   * @param serverName
   *          The serverName to set.
   */
  public void setServerName( String serverName ) {
    this.serverName = serverName;
  }

  /**
   * @return Returns the Facility.
   */
  public String getFacility() {
    return facility;
  }

  /**
   * @param facility
   *          The facility to set.
   */
  public void setFacility( String facility ) {
    this.facility = facility;
  }

  /**
   * @param priority
   *          The priority to set.
   */
  public void setPriority( String priority ) {
    this.priority = priority;
  }

  /**
   * @return Returns the priority.
   */
  public String getPriority() {
    return priority;
  }

  /**
   * @param messagefieldname
   *          The messagefieldname to set.
   */
  public void setMessageFieldName( String messagefieldname ) {
    this.messagefieldname = messagefieldname;
  }

  /**
   * @return Returns the messagefieldname.
   */
  public String getMessageFieldName() {
    return messagefieldname;
  }

  /**
   * @return Returns the port.
   */
  public String getPort() {
    return port;
  }

  /**
   * @param port
   *          The port to set.
   */
  public void setPort( String port ) {
    this.port = port;
  }

  /**
   * @deprecated use {@link #setAddTimestamp(boolean)} instead
   * @param value
   */
  @Deprecated
  public void addTimestamp( boolean value ) {
    setAddTimestamp( value );
  }

  public void setAddTimestamp( boolean value ) {
    this.addTimestamp = value;
  }

  /**
   * @return Returns the addTimestamp.
   */
  public boolean isAddTimestamp() {
    return addTimestamp;
  }

  /**
   * @param pattern
   *          The datePattern to set.
   */
  public void setDatePattern( String pattern ) {
    this.datePattern = pattern;
  }

  /**
   * @return Returns the datePattern.
   */
  public String getDatePattern() {
    return datePattern;
  }

  /**
   * @deprecated use {@link #setAddHostName(boolean)} instead
   * @param value
   */
  @Deprecated
  public void addHostName( boolean value ) {
    setAddHostName( value );
  }

  public void setAddHostName( boolean value ) {
    this.addHostName = value;
  }

  /**
   * @return Returns the addHostName.
   */
  public boolean isAddHostName() {
    return addHostName;
  }

  public String getXML() {
    StringBuilder retval = new StringBuilder();

    retval.append( "    " + XMLHandler.addTagValue( "messagefieldname", messagefieldname ) );
    retval.append( "    " + XMLHandler.addTagValue( "port", port ) );
    retval.append( "    " + XMLHandler.addTagValue( "servername", serverName ) );
    retval.append( "    " + XMLHandler.addTagValue( "facility", facility ) );
    retval.append( "    " + XMLHandler.addTagValue( "priority", priority ) );
    retval.append( "    " + XMLHandler.addTagValue( "addTimestamp", addTimestamp ) );
    retval.append( "    " + XMLHandler.addTagValue( "datePattern", datePattern ) );
    retval.append( "    " + XMLHandler.addTagValue( "addHostName", addHostName ) );

    return retval.toString();
  }

  private void readData( Node stepnode, List<? extends SharedObjectInterface> databases ) throws KettleXMLException {
    try {
      messagefieldname = XMLHandler.getTagValue( stepnode, "messagefieldname" );
      port = XMLHandler.getTagValue( stepnode, "port" );
      serverName = XMLHandler.getTagValue( stepnode, "servername" );
      facility = XMLHandler.getTagValue( stepnode, "facility" );
      priority = XMLHandler.getTagValue( stepnode, "priority" );
      datePattern = XMLHandler.getTagValue( stepnode, "datePattern" );
      addTimestamp = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "addTimestamp" ) );
      addHostName = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "addHostName" ) );

    } catch ( Exception e ) {
      throw new KettleXMLException( BaseMessages.getString(
        PKG, "SyslogMessageMeta.Exception.UnableToReadStepInfo" ), e );
    }
  }

  public void readRep( Repository rep, IMetaStore metaStore, ObjectId id_step, List<DatabaseMeta> databases ) throws KettleException {

    try {
      messagefieldname = rep.getStepAttributeString( id_step, "messagefieldname" );
      serverName = rep.getJobEntryAttributeString( id_step, "servername" );
      port = rep.getJobEntryAttributeString( id_step, "port" );
      facility = rep.getJobEntryAttributeString( id_step, "facility" );
      priority = rep.getJobEntryAttributeString( id_step, "priority" );
      datePattern = rep.getJobEntryAttributeString( id_step, "datePattern" );
      addTimestamp = rep.getJobEntryAttributeBoolean( id_step, "addTimestamp" );
      addHostName = rep.getJobEntryAttributeBoolean( id_step, "addHostName" );

    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString(
        PKG, "SyslogMessageMeta.Exception.UnexpectedErrorReadingStepInfo" ), e );
    }
  }

  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_transformation, ObjectId id_step ) throws KettleException {
    try {
      rep.saveStepAttribute( id_transformation, id_step, "messagefieldname", messagefieldname );
      rep.saveJobEntryAttribute( id_transformation, id_step, "port", port );
      rep.saveJobEntryAttribute( id_transformation, id_step, "servername", serverName );
      rep.saveJobEntryAttribute( id_transformation, id_step, "facility", facility );
      rep.saveJobEntryAttribute( id_transformation, id_step, "priority", priority );
      rep.saveJobEntryAttribute( id_transformation, id_step, "datePattern", datePattern );
      rep.saveJobEntryAttribute( id_transformation, id_step, "addTimestamp", addTimestamp );
      rep.saveJobEntryAttribute( id_transformation, id_step, "addHostName", addHostName );

    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString( PKG, "SyslogMessageMeta.Exception.UnableToSaveStepInfo" )
        + id_step, e );
    }
  }

  public void check( List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta,
    RowMetaInterface prev, String[] input, String[] output, RowMetaInterface info, VariableSpace space,
    Repository repository, IMetaStore metaStore ) {
    CheckResult cr;
    String error_message = "";

    // source filename
    if ( Utils.isEmpty( messagefieldname ) ) {
      error_message = BaseMessages.getString( PKG, "SyslogMessageMeta.CheckResult.MessageFieldMissing" );
      cr = new CheckResult( CheckResult.TYPE_RESULT_ERROR, error_message, stepMeta );
      remarks.add( cr );
    } else {
      error_message = BaseMessages.getString( PKG, "SyslogMessageMeta.CheckResult.MessageFieldOK" );
      cr = new CheckResult( CheckResult.TYPE_RESULT_OK, error_message, stepMeta );
      remarks.add( cr );
    }

    // See if we have input streams leading to this step!
    if ( input.length > 0 ) {
      cr =
        new CheckResult( CheckResult.TYPE_RESULT_OK, BaseMessages.getString(
          PKG, "SyslogMessageMeta.CheckResult.ReceivingInfoFromOtherSteps" ), stepMeta );
      remarks.add( cr );
    } else {
      cr =
        new CheckResult( CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(
          PKG, "SyslogMessageMeta.CheckResult.NoInpuReceived" ), stepMeta );
      remarks.add( cr );
    }

  }

  public StepInterface getStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr,
    TransMeta transMeta, Trans trans ) {
    return new SyslogMessage( stepMeta, stepDataInterface, cnr, transMeta, trans );
  }

  public StepDataInterface getStepData() {
    return new SyslogMessageData();
  }

  public boolean supportsErrorHandling() {
    return true;
  }

}
