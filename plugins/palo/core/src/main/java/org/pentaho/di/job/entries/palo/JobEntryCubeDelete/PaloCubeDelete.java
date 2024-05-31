/*
 *   This file is part of PaloKettlePlugin.
 *
 *   PaloKettlePlugin is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Lesser General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   PaloKettlePlugin is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public License
 *   along with PaloKettlePlugin.  If not, see <http://www.gnu.org/licenses/>.
 *
 *   Portions Copyright 2011 De Bortoli Wines Pty Limited (Australia)
 *   Portions Copyright 2011 - 2018 Hitachi Vantara
 */

package org.pentaho.di.job.entries.palo.JobEntryCubeDelete;

import java.util.List;

import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.palo.core.PaloHelper;

import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.job.entry.JobEntryBase;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Node;

/**
 * Job to delete a cube in palo
 *
 * @author Pieter van der Merwe
 * @since 03-08-2011
 */
// BACKLOG-38582 Remove specific deprecated steps and job entries from PDI, Please uncomment to enable plugin if needed.
//@org.pentaho.di.core.annotations.JobEntry( id = "PALO_CUBE_DELETE",
//    i18nPackageName = "org.pentaho.di.job.entries.palo.JobEntryCubeDelete", image = "ui/images/deprecated.svg",
//    name = "PaloCubeDelete.JobName", description = "PaloCubeDelete.JobDescription",
//    documentationUrl = "http://wiki.pentaho.com/display/EAI/Palo+Cube+Delete",
//    categoryDescription = "i18n:org.pentaho.di.job:JobCategory.Category.Deprecated" )
public class PaloCubeDelete extends JobEntryBase implements Cloneable, JobEntryInterface {

  private DatabaseMeta databaseMeta;
  private String cubeName = "";

  public PaloCubeDelete( String n ) {
    super( n, "" );
    setID( -1L );
  }

  public PaloCubeDelete() {
    this( "" );
  }

  public Object clone() {
    PaloCubeDelete je = (PaloCubeDelete) super.clone();
    return je;
  }

  public String getXML() {
    StringBuffer retval = new StringBuffer();

    retval.append( super.getXML() );

    retval.append( "      " ).append(
        XMLHandler.addTagValue( "connection", getDatabaseMeta() == null ? "" : getDatabaseMeta().getName() ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "cubeName", getCubeName() ) );

    return retval.toString();
  }

  public void loadXML( Node entrynode, List<DatabaseMeta> databases, List<SlaveServer> slaveServers, Repository rep,
      IMetaStore metaStore ) throws KettleXMLException {
    try {
      super.loadXML( entrynode, databases, slaveServers );

      this.setDatabaseMeta( DatabaseMeta.findDatabase( databases, XMLHandler.getTagValue( entrynode, "connection" ) ) );
      this.setCubeName( XMLHandler.getTagValue( entrynode, "cubeName" ) );
    } catch ( KettleXMLException xe ) {
      throw new KettleXMLException( "Unable to load file exists job entry from XML node", xe );
    }
  }

  public void loadRep( Repository rep, IMetaStore metaStore, ObjectId id_jobentry, List<DatabaseMeta> databases,
      List<SlaveServer> slaveServers ) throws KettleException {
    try {
      this.databaseMeta =
          rep.loadDatabaseMetaFromJobEntryAttribute( id_jobentry, "connection", "id_database", databases );
      this.setCubeName( rep.getStepAttributeString( id_jobentry, "cubeName" ) );

    } catch ( KettleException dbe ) {
      throw new KettleException( "Unable to load job entry for type file exists from the repository for id_jobentry="
          + id_jobentry, dbe );
    }
  }

  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_job ) throws KettleException {
    try {

      rep.saveDatabaseMetaJobEntryAttribute( id_job, getObjectId(), "connection", "id_database", databaseMeta );
      rep.saveStepAttribute( id_job, getObjectId(), "cubeName", this.getCubeName() );

    } catch ( KettleDatabaseException dbe ) {
      throw new KettleException(
          "unable to save jobentry of type 'file exists' to the repository for id_job=" + id_job, dbe );
    }
  }

  public Result execute( Result prevResult, int nr ) throws KettleException {

    Result result = new Result( nr );
    result.setResult( false );

    logDetailed( toString(), "Start of processing" );

    // String substitution..
    String realCubeName = environmentSubstitute( getCubeName() );

    PaloHelper database = new PaloHelper( this.getDatabaseMeta(), getLogLevel() );
    try {
      database.connect();
      int cubesremoved = database.removeCube( realCubeName );
      result.setResult( true );
      result.setNrLinesOutput( cubesremoved );
    } catch ( Exception e ) {
      result.setNrErrors( 1 );
      e.printStackTrace();
      logError( toString(), "Error processing Palo Cube Delete : " + e.getMessage() );
    } finally {
      database.disconnect();
    }

    return result;
  }

  public boolean evaluates() {
    return true;
  }

  public void setDatabaseMeta( DatabaseMeta databaseMeta ) {
    this.databaseMeta = databaseMeta;
  }

  public DatabaseMeta getDatabaseMeta() {
    return databaseMeta;
  }

  public void setCubeName( String cubeName ) {
    this.cubeName = cubeName;
  }

  public String getCubeName() {
    return cubeName;
  }
}
