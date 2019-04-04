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

package org.pentaho.di.job.entries.telnet;

import org.pentaho.di.job.entry.validator.AndValidator;
import org.pentaho.di.job.entry.validator.JobEntryValidatorUtils;

import java.util.List;

import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.util.SocketUtil;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entry.JobEntryBase;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.resource.ResourceEntry;
import org.pentaho.di.resource.ResourceEntry.ResourceType;
import org.pentaho.di.resource.ResourceReference;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Node;

/**
 * This defines a Telnet job entry.
 *
 * @author Samatar
 * @since 05-11-2003
 *
 */

public class JobEntryTelnet extends JobEntryBase implements Cloneable, JobEntryInterface {
  private static Class<?> PKG = JobEntryTelnet.class; // for i18n

  private String hostname;
  private String port;
  private String timeout;

  public static final int DEFAULT_TIME_OUT = 3000;
  public static final int DEFAULT_PORT = 23;

  public JobEntryTelnet( String n ) {
    super( n, "" );
    hostname = null;
    port = String.valueOf( DEFAULT_PORT );
    timeout = String.valueOf( DEFAULT_TIME_OUT );
  }

  public JobEntryTelnet() {
    this( "" );
  }

  public Object clone() {
    JobEntryTelnet je = (JobEntryTelnet) super.clone();
    return je;
  }

  public String getXML() {
    StringBuilder retval = new StringBuilder( 100 );

    retval.append( super.getXML() );
    retval.append( "      " ).append( XMLHandler.addTagValue( "hostname", hostname ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "port", port ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "timeout", timeout ) );

    return retval.toString();
  }

  public void loadXML( Node entrynode, List<DatabaseMeta> databases, List<SlaveServer> slaveServers,
    Repository rep, IMetaStore metaStore ) throws KettleXMLException {
    try {
      super.loadXML( entrynode, databases, slaveServers );
      hostname = XMLHandler.getTagValue( entrynode, "hostname" );
      port = XMLHandler.getTagValue( entrynode, "port" );
      timeout = XMLHandler.getTagValue( entrynode, "timeout" );
    } catch ( KettleXMLException xe ) {
      throw new KettleXMLException( "Unable to load job entry of type 'Telnet' from XML node", xe );
    }
  }

  public void loadRep( Repository rep, IMetaStore metaStore, ObjectId id_jobentry, List<DatabaseMeta> databases,
    List<SlaveServer> slaveServers ) throws KettleException {
    try {
      hostname = rep.getJobEntryAttributeString( id_jobentry, "hostname" );
      port = rep.getJobEntryAttributeString( id_jobentry, "port" );
      timeout = rep.getJobEntryAttributeString( id_jobentry, "timeout" );
    } catch ( KettleException dbe ) {
      throw new KettleException(
        "Unable to load job entry of type 'Telnet' exists from the repository for id_jobentry=" + id_jobentry,
        dbe );
    }
  }

  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_job ) throws KettleException {
    try {
      rep.saveJobEntryAttribute( id_job, getObjectId(), "hostname", hostname );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "port", port );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "timeout", timeout );
    } catch ( KettleDatabaseException dbe ) {
      throw new KettleException( "Unable to save job entry of type 'Telnet' to the repository for id_job="
        + id_job, dbe );
    }
  }

  public String getPort() {
    return port;
  }

  public String getRealPort() {
    return environmentSubstitute( getPort() );
  }

  public void setPort( String port ) {
    this.port = port;
  }

  public void setHostname( String hostname ) {
    this.hostname = hostname;
  }

  public String getHostname() {
    return hostname;
  }

  public String getRealHostname() {
    return environmentSubstitute( getHostname() );
  }

  public String getTimeOut() {
    return timeout;
  }

  public String getRealTimeOut() {
    return environmentSubstitute( getTimeOut() );
  }

  public void setTimeOut( String timeout ) {
    this.timeout = timeout;
  }

  public Result execute( Result previousResult, int nr ) {

    Result result = previousResult;

    result.setNrErrors( 1 );
    result.setResult( false );

    String hostname = getRealHostname();
    int port = Const.toInt( getRealPort(), DEFAULT_PORT );
    int timeoutInt = Const.toInt( getRealTimeOut(), -1 );

    if ( Utils.isEmpty( hostname ) ) {
      // No Host was specified
      logError( BaseMessages.getString( PKG, "JobTelnet.SpecifyHost.Label" ) );
      return result;
    }

    try {

      SocketUtil.connectToHost( hostname, port, timeoutInt );

      if ( isDetailed() ) {
        logDetailed( BaseMessages.getString( PKG, "JobTelnet.OK.Label", hostname, port ) );
      }

      result.setNrErrors( 0 );
      result.setResult( true );

    } catch ( Exception ex ) {
      logError( BaseMessages.getString( PKG, "JobTelnet.NOK.Label", hostname, String.valueOf( port ) ) );
      logError( BaseMessages.getString( PKG, "JobTelnet.Error.Label" ) + ex.getMessage() );
    }

    return result;
  }

  public boolean evaluates() {
    return true;
  }

  public List<ResourceReference> getResourceDependencies( JobMeta jobMeta ) {
    List<ResourceReference> references = super.getResourceDependencies( jobMeta );
    if ( !Utils.isEmpty( hostname ) ) {
      String realServername = jobMeta.environmentSubstitute( hostname );
      ResourceReference reference = new ResourceReference( this );
      reference.getEntries().add( new ResourceEntry( realServername, ResourceType.SERVER ) );
      references.add( reference );
    }
    return references;
  }

  @Override
  public void check( List<CheckResultInterface> remarks, JobMeta jobMeta, VariableSpace space,
    Repository repository, IMetaStore metaStore ) {
    JobEntryValidatorUtils.andValidator().validate( this, "hostname", remarks,
        AndValidator.putValidators( JobEntryValidatorUtils.notBlankValidator() ) );
  }
}
