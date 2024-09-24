/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2022 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.job.entries.ping;

import org.pentaho.di.job.entry.validator.AndValidator;
import org.pentaho.di.job.entry.validator.JobEntryValidatorUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
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
 * This defines a ping job entry.
 *
 * @author Samatar Hassan
 * @since Mar-2007
 *
 */
public class JobEntryPing extends JobEntryBase implements Cloneable, JobEntryInterface {
  private static Class<?> PKG = JobEntryPing.class; // for i18n purposes, needed by Translator2!!

  private String hostname;
  private String timeout;
  private static final String DEFAULT_TIMEOUT_MS = "3000";
  private static final String DEFAULT_PACKETS = "2";
  private String nbrPackets;
  private String Windows_CHAR = "-n";
  private String NIX_CHAR = "-c";

  public String classicPing = "classicPing";
  public int iclassicPing = 0;
  public String systemPing = "systemPing";
  public int isystemPing = 1;
  public String bothPings = "bothPings";
  public int ibothPings = 2;

  public String pingtype;
  public int ipingtype;

  public JobEntryPing( String n ) {
    super( n, "" );
    pingtype = classicPing;
    hostname = null;
    nbrPackets = DEFAULT_PACKETS;
    timeout = DEFAULT_TIMEOUT_MS;
  }

  public JobEntryPing() {
    this( "" );
  }

  public Object clone() {
    JobEntryPing je = (JobEntryPing) super.clone();
    return je;
  }

  public String getXML() {
    StringBuilder retval = new StringBuilder( 100 );

    retval.append( super.getXML() );
    retval.append( "      " ).append( XMLHandler.addTagValue( "hostname", hostname ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "nbr_packets", nbrPackets ) );

    // TODO: The following line may be removed 3 versions after 2.5.0
    retval.append( "      " ).append( XMLHandler.addTagValue( "nbrpaquets", nbrPackets ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "timeout", timeout ) );

    retval.append( "      " ).append( XMLHandler.addTagValue( "pingtype", pingtype ) );

    return retval.toString();
  }

  public void loadXML( Node entrynode, List<DatabaseMeta> databases, List<SlaveServer> slaveServers,
    Repository rep, IMetaStore metaStore ) throws KettleXMLException {
    try {
      String nbrPaquets;
      super.loadXML( entrynode, databases, slaveServers );
      hostname = XMLHandler.getTagValue( entrynode, "hostname" );
      nbrPackets = XMLHandler.getTagValue( entrynode, "nbr_packets" );

      // TODO: The following lines may be removed 3 versions after 2.5.0
      nbrPaquets = XMLHandler.getTagValue( entrynode, "nbrpaquets" );
      if ( nbrPackets == null && nbrPaquets != null ) {
        // if only nbrpaquets exists this means that the file was
        // save by a version 2.5.0 ping job entry
        nbrPackets = nbrPaquets;
      }
      timeout = XMLHandler.getTagValue( entrynode, "timeout" );
      pingtype = XMLHandler.getTagValue( entrynode, "pingtype" );
      if ( Utils.isEmpty( pingtype ) ) {
        pingtype = classicPing;
        ipingtype = iclassicPing;
      } else {
        if ( pingtype.equals( systemPing ) ) {
          ipingtype = isystemPing;
        } else if ( pingtype.equals( bothPings ) ) {
          ipingtype = ibothPings;
        } else {
          ipingtype = iclassicPing;
        }

      }
    } catch ( KettleXMLException xe ) {
      throw new KettleXMLException( "Unable to load job entry of type 'ping' from XML node", xe );
    }
  }

  public void loadRep( Repository rep, IMetaStore metaStore, ObjectId id_jobentry, List<DatabaseMeta> databases,
    List<SlaveServer> slaveServers ) throws KettleException {
    try {
      hostname = rep.getJobEntryAttributeString( id_jobentry, "hostname" );
      nbrPackets = rep.getJobEntryAttributeString( id_jobentry, "nbr_packets" );

      // TODO: The following lines may be removed 3 versions after 2.5.0
      String nbrPaquets = rep.getJobEntryAttributeString( id_jobentry, "nbrpaquets" );
      if ( nbrPackets == null && nbrPaquets != null ) {
        // if only nbrpaquets exists this means that the file was
        // save by a version 2.5.0 ping job entry
        nbrPackets = nbrPaquets;
      }
      timeout = rep.getJobEntryAttributeString( id_jobentry, "timeout" );

      pingtype = rep.getJobEntryAttributeString( id_jobentry, "pingtype" );
      if ( Utils.isEmpty( pingtype ) ) {
        pingtype = classicPing;
        ipingtype = iclassicPing;
      } else {
        if ( pingtype.equals( systemPing ) ) {
          ipingtype = isystemPing;
        } else if ( pingtype.equals( bothPings ) ) {
          ipingtype = ibothPings;
        } else {
          ipingtype = iclassicPing;
        }
      }
    } catch ( KettleException dbe ) {
      throw new KettleException(
        "Unable to load job entry of type 'ping' exists from the repository for id_jobentry=" + id_jobentry, dbe );
    }
  }

  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_job ) throws KettleException {
    try {
      rep.saveJobEntryAttribute( id_job, getObjectId(), "hostname", hostname );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "nbr_packets", nbrPackets );

      // TODO: The following line may be removed 3 versions after 2.5.0
      rep.saveJobEntryAttribute( id_job, getObjectId(), "nbrpaquets", nbrPackets );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "timeout", timeout );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "pingtype", pingtype );
    } catch ( KettleDatabaseException dbe ) {
      throw new KettleException(
        "Unable to save job entry of type 'ping' to the repository for id_job=" + id_job, dbe );
    }
  }

  public static boolean isPositiveInteger( String strNum ) {
    boolean result = true;
    if ( strNum == null ) {
      result = false;
    } else {
      try {
        int value = Integer.parseInt( strNum.trim() );
        if ( value <= 0 ) {
          result = false;
        }
      } catch ( NumberFormatException nfe ) {
        result = false;
      }
    }
    return result;
  }

  /**
   * Incase, if a kjb/ktr already has non-numeric value, 0 & negative integers ,
   * value will be replaced with {@link #DEFAULT_PACKETS } during runtime and user
   * will be notified about this replacement in logs
   * @return nbrPackets
   */
  public String getNbrPackets() {
    if ( !isPositiveInteger( nbrPackets ) ) {
      logMinimal( BaseMessages.getString( PKG, "JobPing.WarningOnlyPositiveInteger",
              BaseMessages.getString( PKG, "JobPing.NrPackets.Label" ), DEFAULT_PACKETS ) );
      nbrPackets = DEFAULT_PACKETS;
    }
    return nbrPackets.trim();
  }

  public String getRealNbrPackets() {
    return environmentSubstitute( getNbrPackets() );
  }

  public void setNbrPackets( String nbrPackets ) {
    this.nbrPackets = nbrPackets;
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

  /**
   * Incase, if a kjb/ktr already has non-numeric value, 0 & negative integers ,
   * value will be replaced with {@link #DEFAULT_TIMEOUT_MS } during runtime and user
   * will be notified about this replacement in logs
   * @return timeout
   */
  public String getTimeOut() {
    if ( !isPositiveInteger( timeout ) ) {
      logMinimal( BaseMessages.getString( PKG, "JobPing.WarningOnlyPositiveInteger",
              BaseMessages.getString( PKG, "JobPing.TimeOut.Label" ), DEFAULT_TIMEOUT_MS ) );
      timeout = DEFAULT_TIMEOUT_MS;
    }
    return timeout.trim();
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
    int timeoutInt = Integer.parseInt( getRealTimeOut() );
    int packets = Integer.parseInt( getRealNbrPackets() );
    boolean status = false;

    if ( Utils.isEmpty( hostname ) ) {
      // No Host was specified
      logError( BaseMessages.getString( PKG, "JobPing.SpecifyHost.Label" ) );
      return result;
    }

    try {
      if ( ipingtype == isystemPing || ipingtype == ibothPings ) {
        // Perform a system (Java) ping ...
        status = systemPing( hostname, timeoutInt );
        if ( status ) {
          if ( log.isDetailed() ) {
            log.logDetailed( BaseMessages.getString( PKG, "JobPing.SystemPing" ), BaseMessages.getString(
              PKG, "JobPing.OK.Label", hostname ) );
          }
        } else {
          log.logError( BaseMessages.getString( PKG, "JobPing.SystemPing" ), BaseMessages.getString(
            PKG, "JobPing.NOK.Label", hostname ) );
        }
      }
      if ( ( ipingtype == iclassicPing ) || ( ipingtype == ibothPings && !status ) ) {
        // Perform a classic ping ..
        status = classicPing( hostname, packets );
        if ( status ) {
          if ( log.isDetailed() ) {
            log.logDetailed( BaseMessages.getString( PKG, "JobPing.ClassicPing" ), BaseMessages.getString(
              PKG, "JobPing.OK.Label", hostname ) );
          }
        } else {
          log.logError( BaseMessages.getString( PKG, "JobPing.ClassicPing" ), BaseMessages.getString(
            PKG, "JobPing.NOK.Label", hostname ) );
        }
      }
    } catch ( Exception ex ) {
      logError( BaseMessages.getString( PKG, "JobPing.Error.Label" ) + ex.getMessage() );
    }
    if ( status ) {
      if ( log.isDetailed() ) {
        logDetailed( BaseMessages.getString( PKG, "JobPing.OK.Label", hostname ) );
      }
      result.setNrErrors( 0 );
      result.setResult( true );
    } else {
      logError( BaseMessages.getString( PKG, "JobPing.NOK.Label", hostname ) );
    }
    return result;
  }

  public boolean evaluates() {
    return true;
  }

  private boolean systemPing( String hostname, int timeout ) {
    boolean retval = false;

    InetAddress address = null;
    try {
      address = InetAddress.getByName( hostname );
      if ( address == null ) {
        logError( BaseMessages.getString( PKG, "JobPing.CanNotGetAddress", hostname ) );
        return retval;
      }

      if ( log.isDetailed() ) {
        logDetailed( BaseMessages.getString( PKG, "JobPing.HostName", address.getHostName() ) );
        logDetailed( BaseMessages.getString( PKG, "JobPing.HostAddress", address.getHostAddress() ) );
      }

      retval = address.isReachable( timeout );
    } catch ( Exception e ) {
      logError( BaseMessages.getString( PKG, "JobPing.ErrorSystemPing", hostname, e.getMessage() ) );
    }
    return retval;
  }

  private boolean classicPing( String hostname, int nrpackets ) {
    boolean retval = false;
    try {
      String lignePing = "";
      String CmdPing = "ping ";
      if ( Const.isWindows() ) {
        CmdPing += hostname + " " + Windows_CHAR + " " + nrpackets;
      } else {
        CmdPing += hostname + " " + NIX_CHAR + " " + nrpackets;
      }

      if ( log.isDetailed() ) {
        logDetailed( BaseMessages.getString( PKG, "JobPing.NbrPackets.Label", "" + nrpackets ) );
        logDetailed( BaseMessages.getString( PKG, "JobPing.ExecClassicPing.Label", CmdPing ) );
      }
      Process processPing = Runtime.getRuntime().exec( CmdPing );
      try {
        processPing.waitFor();
      } catch ( InterruptedException e ) {
        logDetailed( BaseMessages.getString( PKG, "JobPing.ClassicPingInterrupted" ) );
      }
      if ( log.isDetailed() ) {
        logDetailed( BaseMessages.getString( PKG, "JobPing.Gettingresponse.Label", hostname ) );
      }
      // Get ping response
      BufferedReader br = new BufferedReader( new InputStreamReader( processPing.getInputStream() ) );

      // Read response lines
      while ( ( lignePing = br.readLine() ) != null ) {
        if ( log.isDetailed() ) {
          logDetailed( lignePing );
        }
      }
      // We succeed only when 0% lost of data
      if ( processPing.exitValue() == 0 ) {
        retval = true;
      }
    } catch ( IOException ex ) {
      logError( BaseMessages.getString( PKG, "JobPing.Error.Label" ) + ex.getMessage() );
    }
    return retval;
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
