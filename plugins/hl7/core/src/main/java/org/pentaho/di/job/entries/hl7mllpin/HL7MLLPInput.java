/*!
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2002-2017 Hitachi Vantara..  All rights reserved.
 */

package org.pentaho.di.job.entries.hl7mllpin;

import java.util.List;

import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.entry.JobEntryBase;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.steps.hl7input.common.MLLPSocketCache;
import org.pentaho.di.trans.steps.hl7input.common.MLLPSocketCacheEntry;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Node;

import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.Structure;
import ca.uhn.hl7v2.parser.GenericParser;
import ca.uhn.hl7v2.parser.Parser;
import ca.uhn.hl7v2.protocol.Transportable;
import ca.uhn.hl7v2.protocol.impl.MLLPTransport;
import ca.uhn.hl7v2.validation.ValidationContext;
import ca.uhn.hl7v2.validation.impl.NoValidation;

/**
 * HL7 MLLP Input
 *
 * @since 24-03-2011
 * @author matt
 */

@org.pentaho.di.core.annotations.JobEntry( id = "HL7MLLPInput",
    categoryDescription = "i18n:org.pentaho.di.job:JobCategory.Category.Utility",
    i18nPackageName = "org.pentaho.di.job.entries.hl7mllpin", image = "mllp-in.svg", name = "HL7MLLPInput.Name",
    documentationUrl = "http://wiki.pentaho.com/display/EAI/HL7+MLLP+Input",
    description = "HL7MLLPInput.TooltipDesc" )
public class HL7MLLPInput extends JobEntryBase implements Cloneable, JobEntryInterface {
  private static Class<?> PKG = HL7MLLPInput.class; // for i18n purposes, needed by Translator2!!

  private String server;
  private String port;
  private String messageVariableName;
  private String messageTypeVariableName;
  private String versionVariableName;

  public HL7MLLPInput( String name ) {
    super( name, "" );

    setID( -1L );

    messageVariableName = "MESSAGE";
    messageTypeVariableName = "MESSAGE_TYPE";
    versionVariableName = "MESSAGE_VERSION";
  }

  public HL7MLLPInput() {
    this( "" );
  }

  public Object clone() {
    HL7MLLPInput je = (HL7MLLPInput) super.clone();
    return je;
  }

  public String getXML() {
    StringBuffer retval = new StringBuffer( 128 );

    retval.append( super.getXML() );
    retval.append( "      " ).append( XMLHandler.addTagValue( "server", server ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "port", port ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "message_variable", messageVariableName ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "type_variable", messageTypeVariableName ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "version_variable", versionVariableName ) );

    return retval.toString();
  }

  public void loadXML( Node entrynode, List<DatabaseMeta> databases, List<SlaveServer> slaveServers, Repository rep,
      IMetaStore metaStore ) throws KettleXMLException {
    try {
      super.loadXML( entrynode, databases, slaveServers );

      server = XMLHandler.getTagValue( entrynode, "server" );
      port = XMLHandler.getTagValue( entrynode, "port" );
      messageVariableName = XMLHandler.getTagValue( entrynode, "message_variable" );
      messageTypeVariableName = XMLHandler.getTagValue( entrynode, "type_variable" );
      versionVariableName = XMLHandler.getTagValue( entrynode, "version_variable" );
    } catch ( KettleXMLException xe ) {
      throw new KettleXMLException( "Unable to load job entry from XML node", xe );
    }
  }

  @Override
  public void loadRep( Repository rep, IMetaStore metaStore, ObjectId idJobentry, List<DatabaseMeta> databases,
      List<SlaveServer> slaveServers ) throws KettleException {
    try {
      server = rep.getJobEntryAttributeString( idJobentry, "server" );
      port = rep.getJobEntryAttributeString( idJobentry, "port" );
      messageVariableName = rep.getJobEntryAttributeString( idJobentry, "message_variable" );
      messageTypeVariableName = rep.getJobEntryAttributeString( idJobentry, "type_variable" );
      versionVariableName = rep.getJobEntryAttributeString( idJobentry, "version_variable" );
    } catch ( KettleException dbe ) {
      throw new KettleException( "Unable to load job entry from the repository for id_jobentry=" + idJobentry, dbe );
    }
  }

  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_job ) throws KettleException {
    try {
      rep.saveJobEntryAttribute( id_job, getObjectId(), "server", server );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "port", port );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "message_variable", messageVariableName );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "type_variable", messageTypeVariableName );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "version_variable", versionVariableName );
    } catch ( KettleDatabaseException dbe ) {
      throw new KettleException( "Unable to save job entry of type 'ftp' to the repository for id_job=" + id_job, dbe );
    }
  }

  public Result execute( Result previousResult, int nr ) {
    Result result = previousResult;

    try {

      String serverName = environmentSubstitute( server );
      int portNumber = Integer.parseInt( environmentSubstitute( port ) );
      String messageVariable = environmentSubstitute( messageVariableName );
      String messageTypeVariable = environmentSubstitute( messageTypeVariableName );
      String versionVariable = environmentSubstitute( versionVariableName );

      MLLPSocketCacheEntry entry = MLLPSocketCache.getInstance().getServerSocketStreamSource( serverName, portNumber );
      if ( entry.getJobListener() != null ) {
        parentJob.addJobListener( entry.getJobListener() );
      }
      MLLPTransport transport = entry.getTransport();

      // Get the next value...
      //
      synchronized ( transport ) {
        Transportable transportable = transport.doReceive();
        String message = transportable.getMessage();

        logDetailed( "Received message: " + message );

        parentJob.setVariable( messageVariable, message );

        // Parse the message and extract the control ID.
        //
        Parser parser = new GenericParser();
        ValidationContext validationContext = new NoValidation();
        parser.setValidationContext( validationContext );
        Message msg = parser.parse( message );
        Structure structure = msg.get( "MSH" );
        String messageType = null;
        String version = msg.getVersion();

        if ( structure instanceof ca.uhn.hl7v2.model.v21.segment.MSH ) {
          messageType = ( (ca.uhn.hl7v2.model.v21.segment.MSH) structure ).getMESSAGETYPE().encode();
        } else if ( structure instanceof ca.uhn.hl7v2.model.v22.segment.MSH ) {
          messageType = ( (ca.uhn.hl7v2.model.v22.segment.MSH) structure ).getMessageType().encode();
        } else if ( structure instanceof ca.uhn.hl7v2.model.v23.segment.MSH ) {
          messageType = ( (ca.uhn.hl7v2.model.v23.segment.MSH) structure ).getMessageType().encode();
        } else if ( structure instanceof ca.uhn.hl7v2.model.v231.segment.MSH ) {
          messageType =
              ( (ca.uhn.hl7v2.model.v231.segment.MSH) structure ).getMessageType().getMessageStructure().getValue();
        } else if ( structure instanceof ca.uhn.hl7v2.model.v24.segment.MSH ) {
          messageType =
              ( (ca.uhn.hl7v2.model.v24.segment.MSH) structure ).getMessageType().getMessageStructure().getValue();
        } else if ( structure instanceof ca.uhn.hl7v2.model.v25.segment.MSH ) {
          messageType =
              ( (ca.uhn.hl7v2.model.v25.segment.MSH) structure ).getMessageType().getMessageStructure().getValue();
        } else if ( structure instanceof ca.uhn.hl7v2.model.v251.segment.MSH ) {
          messageType =
              ( (ca.uhn.hl7v2.model.v251.segment.MSH) structure ).getMessageType().getMessageStructure().getValue();
        } else if ( structure instanceof ca.uhn.hl7v2.model.v26.segment.MSH ) {
          messageType =
              ( (ca.uhn.hl7v2.model.v26.segment.MSH) structure ).getMessageType().getMessageStructure().getValue();
        } else {
          logError( "This job entry does not support the HL7 dialect used. Found MSH class: "
              + structure.getClass().getName() );
        }

        if ( !Utils.isEmpty( messageTypeVariable ) ) {
          parentJob.setVariable( messageTypeVariable, messageType );
        }
        if ( !Utils.isEmpty( versionVariable ) ) {
          parentJob.setVariable( versionVariable, version );
        }
      }

      // All went well..
      //
      result.setNrErrors( 0 );
      result.setResult( true );

    } catch ( Exception e ) {
      log.logError( BaseMessages.getString( PKG, "HL7MLLPInput.Exception.UnexpectedError" ), e );
      result.setNrErrors( 1 );
      result.setResult( false );
    }

    return result;
  }

  public boolean evaluates() {
    return true;
  }

  public boolean isUnconditional() {
    return false;
  }

  /**
   * @return the server
   */
  public String getServer() {
    return server;
  }

  /**
   * @param server
   *          the server to set
   */
  public void setServer( String server ) {
    this.server = server;
  }

  /**
   * @return the port
   */
  public String getPort() {
    return port;
  }

  /**
   * @param port
   *          the port to set
   */
  public void setPort( String port ) {
    this.port = port;
  }

  /**
   * @return the message variable name
   */
  public String getMessageVariableName() {
    return messageVariableName;
  }

  /**
   * @param messageVariableName
   *          the variable name for the message to set
   */
  public void setMessageVariableName( String messageVariableName ) {
    this.messageVariableName = messageVariableName;
  }

  /**
   * @return the message the variable name
   */
  public String getMessageTypeVariableName() {
    return messageTypeVariableName;
  }

  /**
   * @param messageTypeVariableName
   *          the message type variable name to set
   */
  public void setMessageTypeVariableName( String messageTypeVariableName ) {
    this.messageTypeVariableName = messageTypeVariableName;
  }

  /**
   * @return the versionVariableName
   */
  public String getVersionVariableName() {
    return versionVariableName;
  }

  /**
   * @param versionVariableName
   *          the versionVariableName to set
   */
  public void setVersionVariableName( String versionVariableName ) {
    this.versionVariableName = versionVariableName;
  }

}
