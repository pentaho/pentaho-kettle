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

package org.pentaho.di.job.entries.snmptrap;

import java.net.InetAddress;
import java.util.List;

import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.encryption.Encr;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.entry.JobEntryBase;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.metastore.api.IMetaStore;
import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.PDUv1;
import org.snmp4j.ScopedPDU;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.UserTarget;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.MPv3;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.security.AuthMD5;
import org.snmp4j.security.PrivDES;
import org.snmp4j.security.SecurityLevel;
import org.snmp4j.security.SecurityProtocols;
import org.snmp4j.security.USM;
import org.snmp4j.security.UsmUser;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.w3c.dom.Node;

/**
 * This defines an SNMPTrap job entry.
 *
 * @author Matt
 * @since 05-11-2003
 *
 */

public class JobEntrySNMPTrap extends JobEntryBase implements Cloneable, JobEntryInterface {
  private static Class<?> PKG = JobEntrySNMPTrap.class; // for i18n purposes, needed by Translator2!!

  private String serverName;
  private String port;
  private String timeout;
  private String nrretry;
  private String comString;
  private String message;
  private String oid;
  private String targettype;
  private String user;
  private String passphrase;
  private String engineid;

  /**
   * Default retries
   */
  private static int DEFAULT_RETRIES = 1;

  /**
   * Default timeout to 500 milliseconds
   */
  private static int DEFAULT_TIME_OUT = 5000;

  /**
   * Default port
   */
  public static int DEFAULT_PORT = 162;

  public static final String[] target_type_Desc = new String[] {
    BaseMessages.getString( PKG, "JobSNMPTrap.TargetType.Community" ),
    BaseMessages.getString( PKG, "JobSNMPTrap.TargetType.User" ) };
  public static final String[] target_type_Code = new String[] { "community", "user" };

  public JobEntrySNMPTrap( String n ) {
    super( n, "" );
    port = "" + DEFAULT_PORT;
    serverName = null;
    comString = "public";
    nrretry = "" + DEFAULT_RETRIES;
    timeout = "" + DEFAULT_TIME_OUT;
    message = null;
    oid = null;
    targettype = target_type_Code[0];
    user = null;
    passphrase = null;
    engineid = null;
  }

  public JobEntrySNMPTrap() {
    this( "" );
  }

  public Object clone() {
    JobEntrySNMPTrap je = (JobEntrySNMPTrap) super.clone();
    return je;
  }

  public String getTargetTypeDesc( String tt ) {
    if ( Utils.isEmpty( tt ) ) {
      return target_type_Desc[0];
    }
    if ( tt.equalsIgnoreCase( target_type_Code[1] ) ) {
      return target_type_Desc[1];
    } else {
      return target_type_Desc[0];
    }
  }

  public String getTargetTypeCode( String tt ) {
    if ( tt == null ) {
      return target_type_Code[0];
    }
    if ( tt.equals( target_type_Desc[1] ) ) {
      return target_type_Code[1];
    } else {
      return target_type_Code[0];
    }
  }

  public String getXML() {
    StringBuilder retval = new StringBuilder( 200 );

    retval.append( super.getXML() );
    retval.append( "      " ).append( XMLHandler.addTagValue( "port", port ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "servername", serverName ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "oid", oid ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "comstring", comString ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "message", message ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "timeout", timeout ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "nrretry", nrretry ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "targettype", targettype ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "user", user ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "passphrase", Encr.encryptPasswordIfNotUsingVariables( passphrase ) ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "engineid", engineid ) );
    return retval.toString();
  }

  public void loadXML( Node entrynode, List<DatabaseMeta> databases, List<SlaveServer> slaveServers,
    Repository rep, IMetaStore metaStore ) throws KettleXMLException {
    try {
      super.loadXML( entrynode, databases, slaveServers );
      port = XMLHandler.getTagValue( entrynode, "port" );
      serverName = XMLHandler.getTagValue( entrynode, "servername" );
      oid = XMLHandler.getTagValue( entrynode, "oid" );
      message = XMLHandler.getTagValue( entrynode, "message" );
      comString = XMLHandler.getTagValue( entrynode, "comstring" );
      timeout = XMLHandler.getTagValue( entrynode, "timeout" );
      nrretry = XMLHandler.getTagValue( entrynode, "nrretry" );
      targettype = XMLHandler.getTagValue( entrynode, "targettype" );
      user = XMLHandler.getTagValue( entrynode, "user" );
      passphrase = Encr.decryptPasswordOptionallyEncrypted( XMLHandler.getTagValue( entrynode, "passphrase" ) );
      engineid = XMLHandler.getTagValue( entrynode, "engineid" );

    } catch ( KettleXMLException xe ) {
      throw new KettleXMLException( "Unable to load job entry of type 'SNMPTrap' from XML node", xe );
    }
  }

  public void loadRep( Repository rep, IMetaStore metaStore, ObjectId id_jobentry, List<DatabaseMeta> databases,
    List<SlaveServer> slaveServers ) throws KettleException {
    try {
      port = rep.getJobEntryAttributeString( id_jobentry, "port" );
      serverName = rep.getJobEntryAttributeString( id_jobentry, "servername" );
      oid = rep.getJobEntryAttributeString( id_jobentry, "oid" );
      message = rep.getJobEntryAttributeString( id_jobentry, "message" );
      comString = rep.getJobEntryAttributeString( id_jobentry, "comstring" );
      timeout = rep.getJobEntryAttributeString( id_jobentry, "timeout" );
      nrretry = rep.getJobEntryAttributeString( id_jobentry, "nrretry" );
      targettype = rep.getJobEntryAttributeString( id_jobentry, "targettype" );
      user = rep.getJobEntryAttributeString( id_jobentry, "user" );
      passphrase = Encr.decryptPasswordOptionallyEncrypted( rep.getJobEntryAttributeString( id_jobentry, "passphrase" ) );
      engineid = rep.getJobEntryAttributeString( id_jobentry, "engineid" );

    } catch ( KettleException dbe ) {
      throw new KettleException(
        "Unable to load job entry of type 'SNMPTrap' from the repository for id_jobentry=" + id_jobentry, dbe );
    }
  }

  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_job ) throws KettleException {
    try {
      rep.saveJobEntryAttribute( id_job, getObjectId(), "port", port );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "servername", serverName );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "oid", oid );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "message", message );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "comstring", comString );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "timeout", timeout );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "nrretry", nrretry );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "targettype", targettype );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "user", user );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "passphrase", Encr.encryptPasswordIfNotUsingVariables( passphrase ) );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "engineid", engineid );

    } catch ( KettleDatabaseException dbe ) {
      throw new KettleException( "Unable to save job entry of type 'SNMPTrap' to the repository for id_job="
        + id_job, dbe );
    }
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
   * @return Returns the OID.
   */
  public String getOID() {
    return oid;
  }

  /**
   * @param serverName
   *          The oid to set.
   */
  public void setOID( String oid ) {
    this.oid = oid;
  }

  /**
   * @return Returns the comString.
   */
  public String getComString() {
    return comString;
  }

  /**
   * @param comString
   *          The comString to set.
   */
  public void setComString( String comString ) {
    this.comString = comString;
  }

  /**
   * @param user
   *          The user to set.
   */
  public void setUser( String user ) {
    this.user = user;
  }

  /**
   * @return Returns the user.
   */
  public String getUser() {
    return user;
  }

  /**
   * @param user
   *          The passphrase to set.
   */
  public void setPassPhrase( String passphrase ) {
    this.passphrase = passphrase;
  }

  /**
   * @return Returns the passphrase.
   */
  public String getPassPhrase() {
    return passphrase;
  }

  /**
   * @param user
   *          The engineid to set.
   */
  public void setEngineID( String engineid ) {
    this.engineid = engineid;
  }

  /**
   * @return Returns the engineid.
   */
  public String getEngineID() {
    return engineid;
  }

  public String getTargetType() {
    return targettype;
  }

  public void setTargetType( String targettypein ) {
    this.targettype = getTargetTypeCode( targettypein );
  }

  /**
   * @param message
   *          The message to set.
   */
  public void setMessage( String message ) {
    this.message = message;
  }

  /**
   * @return Returns the comString.
   */
  public String getMessage() {
    return message;
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
   * @param timeout
   *          The timeout to set.
   */
  public void setTimeout( String timeout ) {
    this.timeout = timeout;
  }

  /**
   * @return Returns the timeout.
   */
  public String getTimeout() {
    return timeout;
  }

  /**
   * @param nrretry
   *          The nrretry to set.
   */
  public void setRetry( String nrretry ) {
    this.nrretry = nrretry;
  }

  /**
   * @return Returns the nrretry.
   */
  public String getRetry() {
    return nrretry;
  }

  public Result execute( Result previousResult, int nr ) {
    Result result = previousResult;
    result.setNrErrors( 1 );
    result.setResult( false );

    String servername = environmentSubstitute( serverName );
    int nrPort = Const.toInt( environmentSubstitute( "" + port ), DEFAULT_PORT );
    String Oid = environmentSubstitute( oid );
    int timeOut = Const.toInt( environmentSubstitute( "" + timeout ), DEFAULT_TIME_OUT );
    int retry = Const.toInt( environmentSubstitute( "" + nrretry ), 1 );
    String messageString = environmentSubstitute( message );

    Snmp snmp = null;

    try {
      TransportMapping transMap = new DefaultUdpTransportMapping();
      snmp = new Snmp( transMap );

      UdpAddress udpAddress = new UdpAddress( InetAddress.getByName( servername ), nrPort );
      ResponseEvent response = null;
      if ( targettype.equals( target_type_Code[0] ) ) {
        // Community target
        String community = environmentSubstitute( comString );

        CommunityTarget target = new CommunityTarget();
        PDUv1 pdu1 = new PDUv1();
        transMap.listen();

        target.setCommunity( new OctetString( community ) );
        target.setVersion( SnmpConstants.version1 );
        target.setAddress( udpAddress );
        if ( target.getAddress().isValid() ) {
          if ( log.isDebug() ) {
            logDebug( "Valid IP address" );
          }
        } else {
          throw new KettleException( "Invalid IP address" );
        }
        target.setRetries( retry );
        target.setTimeout( timeOut );

        // create the PDU
        pdu1.setGenericTrap( 6 );
        pdu1.setSpecificTrap( PDUv1.ENTERPRISE_SPECIFIC );
        pdu1.setEnterprise( new OID( Oid ) );
        pdu1.add( new VariableBinding( new OID( Oid ), new OctetString( messageString ) ) );

        response = snmp.send( pdu1, target );

      } else {
        // User target
        String userName = environmentSubstitute( user );
        String passPhrase = environmentSubstitute( passphrase );
        String engineID = environmentSubstitute( engineid );

        UserTarget usertarget = new UserTarget();
        transMap.listen();
        usertarget.setAddress( udpAddress );
        if ( usertarget.getAddress().isValid() ) {
          if ( log.isDebug() ) {
            logDebug( "Valid IP address" );
          }
        } else {
          throw new KettleException( "Invalid IP address" );
        }

        usertarget.setRetries( retry );
        usertarget.setTimeout( timeOut );
        usertarget.setVersion( SnmpConstants.version3 );
        usertarget.setSecurityLevel( SecurityLevel.AUTH_PRIV );
        usertarget.setSecurityName( new OctetString( "MD5DES" ) );

        // Since we are using SNMPv3 we use authenticated users
        // this is handled by the UsmUser and USM class

        UsmUser uu =
          new UsmUser(
            new OctetString( userName ), AuthMD5.ID, new OctetString( passPhrase ), PrivDES.ID,
            new OctetString( passPhrase ) );

        USM usm = snmp.getUSM();

        if ( usm == null ) {
          throw new KettleException( "Null Usm" );
        } else {
          usm = new USM( SecurityProtocols.getInstance(), new OctetString( MPv3.createLocalEngineID() ), 0 );
          usm.addUser( new OctetString( userName ), uu );
          if ( log.isDebug() ) {
            logDebug( "Valid Usm" );
          }
        }

        // create the PDU
        ScopedPDU pdu = new ScopedPDU();
        pdu.add( new VariableBinding( new OID( Oid ), new OctetString( messageString ) ) );
        pdu.setType( PDU.TRAP );
        if ( !Utils.isEmpty( engineID ) ) {
          pdu.setContextEngineID( new OctetString( engineID ) );
        }

        // send the PDU
        response = snmp.send( pdu, usertarget );
      }

      if ( response != null ) {
        if ( log.isDebug() ) {
          logDebug( "Received response from: " + response.getPeerAddress() + response.toString() );
        }
      }

      result.setNrErrors( 0 );
      result.setResult( true );
    } catch ( Exception e ) {
      logError( BaseMessages.getString( PKG, "JobEntrySNMPTrap.ErrorGetting", e.getMessage() ) );
    } finally {
      try {
        if ( snmp != null ) {
          snmp.close();
        }
      } catch ( Exception e ) { /* Ignore */
      }
    }

    return result;
  }

  public boolean evaluates() {
    return true;
  }

}
