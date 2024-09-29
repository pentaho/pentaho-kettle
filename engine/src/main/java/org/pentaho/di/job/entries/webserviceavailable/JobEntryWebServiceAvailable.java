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

package org.pentaho.di.job.entries.webserviceavailable;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.Const;
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
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Node;

/**
 * This defines a webservice available job entry.
 *
 * @author Samatar
 * @since 05-11-2009
 *
 */

public class JobEntryWebServiceAvailable extends JobEntryBase implements Cloneable, JobEntryInterface {
  private static Class<?> PKG = JobEntryWebServiceAvailable.class; // for i18n purposes, needed by Translator2!!

  private String url;
  private String connectTimeOut;
  private String readTimeOut;

  public JobEntryWebServiceAvailable( String n ) {
    super( n, "" );
    url = null;
    connectTimeOut = "0";
    readTimeOut = "0";
  }

  public JobEntryWebServiceAvailable() {
    this( "" );
  }

  public Object clone() {
    JobEntryWebServiceAvailable je = (JobEntryWebServiceAvailable) super.clone();
    return je;
  }

  public String getXML() {
    StringBuilder retval = new StringBuilder( 50 );

    retval.append( super.getXML() );
    retval.append( "      " ).append( XMLHandler.addTagValue( "url", url ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "connectTimeOut", connectTimeOut ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "readTimeOut", readTimeOut ) );
    return retval.toString();
  }

  public void loadXML( Node entrynode, List<DatabaseMeta> databases, List<SlaveServer> slaveServers,
    Repository rep, IMetaStore metaStore ) throws KettleXMLException {
    try {
      super.loadXML( entrynode, databases, slaveServers );
      url = XMLHandler.getTagValue( entrynode, "url" );
      connectTimeOut = XMLHandler.getTagValue( entrynode, "connectTimeOut" );
      readTimeOut = XMLHandler.getTagValue( entrynode, "readTimeOut" );
    } catch ( KettleXMLException xe ) {
      throw new KettleXMLException( BaseMessages.getString(
        PKG, "JobEntryWebServiceAvailable.ERROR_0001_Cannot_Load_Job_Entry_From_Xml_Node" ), xe );
    }
  }

  public void loadRep( Repository rep, IMetaStore metaStore, ObjectId id_jobentry, List<DatabaseMeta> databases,
    List<SlaveServer> slaveServers ) throws KettleException {
    try {
      url = rep.getJobEntryAttributeString( id_jobentry, "url" );
      connectTimeOut = rep.getJobEntryAttributeString( id_jobentry, "connectTimeOut" );
      readTimeOut = rep.getJobEntryAttributeString( id_jobentry, "readTimeOut" );
    } catch ( KettleException dbe ) {
      throw new KettleException( BaseMessages.getString(
        PKG, "JobEntryWebServiceAvailable.ERROR_0002_Cannot_Load_Job_From_Repository", "" + id_jobentry ), dbe );
    }
  }

  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_job ) throws KettleException {
    try {
      rep.saveJobEntryAttribute( id_job, getObjectId(), "url", url );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "connectTimeOut", connectTimeOut );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "readTimeOut", readTimeOut );
    } catch ( KettleDatabaseException dbe ) {
      throw new KettleException( BaseMessages.getString(
        PKG, "JobEntryWebServiceAvailable.ERROR_0003_Cannot_Save_Job_Entry", "" + id_job ), dbe );
    }
  }

  public void setURL( String url ) {
    this.url = url;
  }

  public String getURL() {
    return url;
  }

  public void setConnectTimeOut( String timeout ) {
    this.connectTimeOut = timeout;
  }

  public String getConnectTimeOut() {
    return connectTimeOut;
  }

  public void setReadTimeOut( String timeout ) {
    this.readTimeOut = timeout;
  }

  public String getReadTimeOut() {
    return readTimeOut;
  }

  public Result execute( Result previousResult, int nr ) {
    Result result = previousResult;
    result.setResult( false );

    String realURL = environmentSubstitute( getURL() );

    if ( !Utils.isEmpty( realURL ) ) {
      int connectTimeOut = Const.toInt( environmentSubstitute( getConnectTimeOut() ), 0 );
      int readTimeOut = Const.toInt( environmentSubstitute( getReadTimeOut() ), 0 );
      InputStream in = null;
      try {

        URLConnection conn = new URL( realURL ).openConnection();
        conn.setConnectTimeout( connectTimeOut );
        conn.setReadTimeout( readTimeOut );
        in = conn.getInputStream();
        // Web service is available
        result.setResult( true );
      } catch ( Exception e ) {
        result.setNrErrors( 1 );
        String message =
          BaseMessages
            .getString( PKG, "JobEntryWebServiceAvailable.ERROR_0004_Exception", realURL, e.toString() );
        logError( message );
        result.setLogText( message );
      } finally {
        if ( in != null ) {
          try {
            in.close();
          } catch ( Exception e ) { /* Ignore */
          }
        }
      }
    } else {
      result.setNrErrors( 1 );
      String message = BaseMessages.getString( PKG, "JobEntryWebServiceAvailable.ERROR_0005_No_URL_Defined" );
      logError( message );
      result.setLogText( message );
    }

    return result;
  }

  public boolean evaluates() {
    return true;
  }

}
