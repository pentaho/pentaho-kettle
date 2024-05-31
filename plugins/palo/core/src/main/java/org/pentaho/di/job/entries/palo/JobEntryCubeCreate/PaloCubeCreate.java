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

package org.pentaho.di.job.entries.palo.JobEntryCubeCreate;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.Const;
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
 * Job to create a cube in palo
 *
 * @author Pieter van der Merwe
 * @since 03-08-2011
 */

// BACKLOG-38582 Remove specific deprecated steps and job entries from PDI, Please uncomment to enable plugin if needed.
//@org.pentaho.di.core.annotations.JobEntry( id = "PALO_CUBE_CREATE",
//    i18nPackageName = "org.pentaho.di.job.entries.palo.JobEntryCubeCreate", image = "ui/images/deprecated.svg",
//    name = "PaloCubeCreate.JobName", description = "PaloCubeCreate.JobDescription",
//    documentationUrl = "http://wiki.pentaho.com/display/EAI/Palo+Cube+Create",
//    categoryDescription = "i18n:org.pentaho.di.job:JobCategory.Category.Deprecated" )
public class PaloCubeCreate extends JobEntryBase implements Cloneable, JobEntryInterface {

  private DatabaseMeta databaseMeta;
  private String cubeName = "";
  private List<String> dimensionNames = new ArrayList<String>();

  public PaloCubeCreate( String n ) {
    super( n, "" );
    setID( -1L );
  }

  public PaloCubeCreate() {
    this( "" );
  }

  public Object clone() {
    PaloCubeCreate je = (PaloCubeCreate) super.clone();
    return je;
  }

  public String getXML() {
    StringBuffer retval = new StringBuffer();

    retval.append( super.getXML() );

    retval.append( "      " ).append(
        XMLHandler.addTagValue( "connection", getDatabaseMeta() == null ? "" : getDatabaseMeta().getName() ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "cubeName", getCubeName() ) );

    retval.append( "      <dimensions>" ).append( Const.CR );
    for ( String dimensionName : this.dimensionNames ) {
      retval.append( "        " ).append( XMLHandler.addTagValue( "dimensionname", dimensionName ) );
    }
    retval.append( "      </dimensions>" ).append( Const.CR );

    return retval.toString();
  }

  public void loadXML( Node entrynode, List<DatabaseMeta> databases, List<SlaveServer> slaveServers, Repository rep,
      IMetaStore metaStore ) throws KettleXMLException {
    try {
      super.loadXML( entrynode, databases, slaveServers );

      this.setDatabaseMeta( DatabaseMeta.findDatabase( databases, XMLHandler.getTagValue( entrynode, "connection" ) ) );
      this.setCubeName( XMLHandler.getTagValue( entrynode, "cubeName" ) );

      Node dimensionNode = XMLHandler.getSubNode( entrynode, "dimensions" );
      int nrDimensions = XMLHandler.countNodes( dimensionNode, "dimensionname" );
      for ( int i = 0; i < nrDimensions; i++ ) {
        String dimensionName =
            XMLHandler.getNodeValue( XMLHandler.getSubNodeByNr( dimensionNode, "dimensionname", i ) );
        this.dimensionNames.add( dimensionName );
      }

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

      int nrFields = rep.countNrStepAttributes( id_jobentry, "dimensionname" );

      for ( int i = 0; i < nrFields; i++ ) {
        String dimensionName = rep.getStepAttributeString( id_jobentry, i, "dimensionname" );
        this.dimensionNames.add( dimensionName );
      }
    } catch ( KettleException dbe ) {
      throw new KettleException( "Unable to load job entry for type file exists from the repository for id_jobentry="
          + id_jobentry, dbe );
    }
  }

  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_job ) throws KettleException {
    try {
      rep.saveDatabaseMetaJobEntryAttribute( id_job, getObjectId(), "connection", "id_database", databaseMeta );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "cubeName", this.getCubeName() );

      for ( int i = 0; i < this.dimensionNames.size(); i++ ) {
        rep.saveJobEntryAttribute( id_job, getObjectId(), i, "dimensionname", this.dimensionNames.get( i ) );
      }
    } catch ( KettleDatabaseException dbe ) {
      throw new KettleException(
          "unable to save jobentry of type 'file exists' to the repository for id_job=" + id_job, dbe );
    }
  }

  @Override
  public Result execute( Result prevResult, int nr ) throws KettleException {

    Result result = new Result( nr );
    result.setResult( false );

    logDetailed( toString(), "Start of processing" );

    // String substitution..
    String realCubeName = environmentSubstitute( getCubeName() );

    PaloHelper database = new PaloHelper( this.getDatabaseMeta(), getLogLevel() );
    try {
      database.connect();
      database.createCube( realCubeName, dimensionNames.toArray( new String[dimensionNames.size()] ) );
      result.setResult( true );
      result.setNrLinesOutput( 1 );
    } catch ( Exception e ) {
      result.setNrErrors( 1 );
      e.printStackTrace();
      logError( toString(), "Error processing Palo Cube Create : " + e.getMessage() );
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

  public void setDimensionNames( List<String> dimensionNames ) {
    this.dimensionNames = dimensionNames;
  }

  public List<String> getDimensionNames() {
    return dimensionNames;
  }
}
