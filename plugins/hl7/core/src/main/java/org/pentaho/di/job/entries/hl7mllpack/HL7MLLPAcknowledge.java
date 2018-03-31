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

package org.pentaho.di.job.entries.hl7mllpack;

import java.util.List;

import org.pentaho.di.cluster.SlaveServer;
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
import ca.uhn.hl7v2.parser.GenericParser;
import ca.uhn.hl7v2.parser.Parser;
import ca.uhn.hl7v2.protocol.Transportable;
import ca.uhn.hl7v2.protocol.impl.MLLPTransport;
import ca.uhn.hl7v2.protocol.impl.TransportableImpl;
import ca.uhn.hl7v2.validation.ValidationContext;
import ca.uhn.hl7v2.validation.impl.NoValidation;

/**
 * HL7 MLLP Acknowledge
 *
 * @since 24-03-2011
 * @author matt
 */

@org.pentaho.di.core.annotations.JobEntry( id = "HL7MLLPAcknowledge",
    categoryDescription = "i18n:org.pentaho.di.job:JobCategory.Category.Utility",
    i18nPackageName = "org.pentaho.di.job.entries.hl7mllpack", image = "mllp-ack.svg", name = "HL7MLLPAcknowledge.Name",
    documentationUrl = "http://wiki.pentaho.com/display/EAI/HL7+MLLP+Acknowledge",
    description = "HL7MLLPAcknowledge.TooltipDesc" )
public class HL7MLLPAcknowledge extends JobEntryBase implements Cloneable, JobEntryInterface {
  private static Class<?> PKG = HL7MLLPAcknowledge.class; // for i18n purposes, needed by Translator2!!

  private String server;
  private String port;
  private String variableName;

  public HL7MLLPAcknowledge( String name ) {
    super( name, "" );

    setID( -1L );

    variableName = "MESSAGE";
  }

  public HL7MLLPAcknowledge() {
    this( "" );
  }

  public Object clone() {
    HL7MLLPAcknowledge je = (HL7MLLPAcknowledge) super.clone();
    return je;
  }

  public String getXML() {
    StringBuffer retval = new StringBuffer( 128 );

    retval.append( super.getXML() );
    retval.append( "      " ).append( XMLHandler.addTagValue( "server", server ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "port", port ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "variable", variableName ) );

    return retval.toString();
  }

  public void loadXML( Node entrynode, List<DatabaseMeta> databases, List<SlaveServer> slaveServers, Repository rep,
      IMetaStore metaStore ) throws KettleXMLException {
    try {
      super.loadXML( entrynode, databases, slaveServers );

      server = XMLHandler.getTagValue( entrynode, "server" );
      port = XMLHandler.getTagValue( entrynode, "port" );
      variableName = XMLHandler.getTagValue( entrynode, "variable" );
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
      variableName = rep.getJobEntryAttributeString( idJobentry, "variable" );
    } catch ( KettleException dbe ) {
      throw new KettleException( "Unable to load job entry from the repository for id_jobentry=" + idJobentry, dbe );
    }
  }

  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_job ) throws KettleException {
    try {
      rep.saveJobEntryAttribute( id_job, getObjectId(), "server", server );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "port", port );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "variable", variableName );
    } catch ( KettleDatabaseException dbe ) {
      throw new KettleException( "Unable to save job entry of type 'ftp' to the repository for id_job=" + id_job, dbe );
    }
  }

  public Result execute( Result previousResult, int nr ) {
    Result result = previousResult;

    try {

      String serverName = environmentSubstitute( server );
      int portNumber = Integer.parseInt( environmentSubstitute( port ) );
      String variable = environmentSubstitute( variableName );

      MLLPSocketCacheEntry entry = MLLPSocketCache.getInstance().getServerSocketStreamSource( serverName, portNumber );
      MLLPTransport transport = entry.getTransport();

      // Get the next value...
      //
      synchronized ( transport ) {
        String message = getVariable( variable );

        // Parse the message and extract the acknowledge message.
        //
        Parser parser = new GenericParser();
        ValidationContext validationContext = new NoValidation();
        parser.setValidationContext( validationContext );
        Message msg = parser.parse( message );
        Message ack = msg.generateACK();

        String ackMessage = ack.encode();

        String APPNAME = "PDI4";

        if ( ack instanceof ca.uhn.hl7v2.model.v21.message.ACK ) {
          ca.uhn.hl7v2.model.v21.message.ACK mod = (ca.uhn.hl7v2.model.v21.message.ACK) ack;
          mod.getMSH().getSENDINGAPPLICATION().setValue( APPNAME );
          mod.getMSH().getSENDINGFACILITY().setValue( APPNAME );
          ackMessage = mod.encode();
        } else if ( ack instanceof ca.uhn.hl7v2.model.v22.message.ACK ) {
          ca.uhn.hl7v2.model.v22.message.ACK mod = (ca.uhn.hl7v2.model.v22.message.ACK) ack;
          mod.getMSH().getSendingApplication().setValue( APPNAME );
          mod.getMSH().getSendingFacility().setValue( APPNAME );
          ackMessage = mod.encode();
        } else if ( ack instanceof ca.uhn.hl7v2.model.v23.message.ACK ) {
          ca.uhn.hl7v2.model.v23.message.ACK mod = (ca.uhn.hl7v2.model.v23.message.ACK) ack;
          mod.getMSH().getSendingApplication().getNamespaceID().setValue( APPNAME );
          mod.getMSH().getSendingFacility().getNamespaceID().setValue( APPNAME );
          ackMessage = mod.encode();
        } else if ( ack instanceof ca.uhn.hl7v2.model.v231.message.ACK ) {
          ca.uhn.hl7v2.model.v231.message.ACK mod = (ca.uhn.hl7v2.model.v231.message.ACK) ack;
          mod.getMSH().getSendingApplication().getNamespaceID().setValue( APPNAME );
          mod.getMSH().getSendingFacility().getNamespaceID().setValue( APPNAME );
          ackMessage = mod.encode();
        } else if ( ack instanceof ca.uhn.hl7v2.model.v24.message.ACK ) {
          ca.uhn.hl7v2.model.v24.message.ACK mod = (ca.uhn.hl7v2.model.v24.message.ACK) ack;
          mod.getMSH().getSendingApplication().getNamespaceID().setValue( APPNAME );
          mod.getMSH().getSendingFacility().getNamespaceID().setValue( APPNAME );
          ackMessage = mod.encode();
        } else if ( ack instanceof ca.uhn.hl7v2.model.v25.message.ACK ) {
          ca.uhn.hl7v2.model.v25.message.ACK mod = (ca.uhn.hl7v2.model.v25.message.ACK) ack;
          mod.getMSH().getSendingApplication().getNamespaceID().setValue( APPNAME );
          mod.getMSH().getSendingFacility().getNamespaceID().setValue( APPNAME );
          ackMessage = mod.encode();
        } else if ( ack instanceof ca.uhn.hl7v2.model.v251.message.ACK ) {
          ca.uhn.hl7v2.model.v251.message.ACK mod = (ca.uhn.hl7v2.model.v251.message.ACK) ack;
          mod.getMSH().getSendingApplication().getNamespaceID().setValue( APPNAME );
          mod.getMSH().getSendingFacility().getNamespaceID().setValue( APPNAME );
          ackMessage = mod.encode();
        } else if ( ack instanceof ca.uhn.hl7v2.model.v26.message.ACK ) {
          ca.uhn.hl7v2.model.v26.message.ACK mod = (ca.uhn.hl7v2.model.v26.message.ACK) ack;
          mod.getMSH().getSendingApplication().getNamespaceID().setValue( APPNAME );
          mod.getMSH().getSendingFacility().getNamespaceID().setValue( APPNAME );
          ackMessage = mod.encode();
        } else {
          logError( "This job entry does not support the HL7 dialect used. Found ACK class: "
              + ack.getClass().getName() );
        }

        Transportable transportable = new TransportableImpl( ackMessage );
        transport.doSend( transportable );
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
   * @return the variableName
   */
  public String getVariableName() {
    return variableName;
  }

  /**
   * @param variableName
   *          the variableName to set
   */
  public void setVariableName( String variableName ) {
    this.variableName = variableName;
  }

}
