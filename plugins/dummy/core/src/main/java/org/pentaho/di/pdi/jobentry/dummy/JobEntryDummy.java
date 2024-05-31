/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2019 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.pdi.jobentry.dummy;

import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.job.entry.JobEntryBase;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.w3c.dom.Node;
import java.util.List;


/**
 * This defines an FTP job entry.
 *
 * @author Matt
 * @since 05-11-2003
 */
// BACKLOG-38582 Remove specific deprecated steps and job entries from PDI, Please uncomment to enable plugin if needed.
//@org.pentaho.di.core.annotations.JobEntry( id = "DummyJob", suggestion = "DummyPlugin.Job.SuggestedEntry",
//      i18nPackageName = "pdi.jobentry.dummy", image = "ui/images/deprecated.svg",
//      name = "DummyPlugin.Job.Name", description = "DummyPlugin.Job.Description",
//      categoryDescription = "i18n:org.pentaho.di.job:JobCategory.Category.Deprecated" )
public class JobEntryDummy extends JobEntryBase implements Cloneable, JobEntryInterface {

  private static final String WILDCARD = "wildcard";

  private static final String TARGETDIRECTORY = "targetdirectory";

  private static final String SOURCEDIRECTORY = "sourcedirectory";

  private String sourceDirectory;

  private String targetDirectory;

  private String wildcard;

  public final String getSourceDirectory() {
    return sourceDirectory;
  }

  public final void setSourceDirectory( String sourceDirectory ) {
    this.sourceDirectory = sourceDirectory;
  }

  public final String getWildcard() {
    return wildcard;
  }

  public final void setWildcard( String wildcard ) {
    this.wildcard = wildcard;
  }

  public JobEntryDummy( String n ) {
    super( n, "" );
    setID( -1L );
  }

  public JobEntryDummy() {
    this( "" );
  }

  @Override
  public Object clone() {
    JobEntryDummy je = (JobEntryDummy) super.clone();
    return je;
  }

  @Override
  public String getXML() {
    StringBuffer retval = new StringBuffer();

    retval.append( super.getXML() );

    retval.append( "      " + XMLHandler.addTagValue( SOURCEDIRECTORY, sourceDirectory ) );
    retval.append( "      " + XMLHandler.addTagValue( TARGETDIRECTORY, targetDirectory ) );
    retval.append( "      " + XMLHandler.addTagValue( WILDCARD, wildcard ) );

    return retval.toString();
  }

  @Override
  public void loadXML( Node entrynode, List<DatabaseMeta> databases, List<SlaveServer> slaveServers, Repository rep ) throws KettleXMLException {
    try {
      super.loadXML( entrynode, databases, slaveServers );
      sourceDirectory = XMLHandler.getTagValue( entrynode, SOURCEDIRECTORY );
      targetDirectory = XMLHandler.getTagValue( entrynode, TARGETDIRECTORY );
      wildcard = XMLHandler.getTagValue( entrynode, WILDCARD );
    } catch ( KettleXMLException xe ) {
      throw new KettleXMLException( "Unable to load file exists job entry from XML node",
          xe );
    }
  }

  @Override
  public void loadRep( Repository rep, ObjectId id_jobentry, List<DatabaseMeta> databases, List<SlaveServer> slaveServers ) throws KettleException {
    try {
      super.loadRep( rep, id_jobentry, databases, slaveServers );
      sourceDirectory = rep.getJobEntryAttributeString( id_jobentry, SOURCEDIRECTORY );
      targetDirectory = rep.getJobEntryAttributeString( id_jobentry, TARGETDIRECTORY );
      wildcard = rep.getJobEntryAttributeString( id_jobentry, WILDCARD );
    } catch ( KettleException dbe ) {
      throw new KettleException(
          "Unable to load job entry for type file exists from the repository for id_jobentry="
              + id_jobentry, dbe );
    }
  }

  @Override
  public void saveRep( Repository rep, ObjectId id_job ) throws KettleException {
    try {
      super.saveRep( rep, id_job );

      rep.saveJobEntryAttribute( id_job, getObjectId(), SOURCEDIRECTORY, sourceDirectory );
      rep.saveJobEntryAttribute( id_job, getObjectId(), TARGETDIRECTORY, targetDirectory );
      rep.saveJobEntryAttribute( id_job, getObjectId(), WILDCARD, wildcard );
    } catch ( KettleDatabaseException dbe ) {
      throw new KettleException(
          "unable to save jobentry of type 'file exists' to the repository for id_job="
              + id_job, dbe );
    }
  }

  /**
   * @return Returns the targetDirectory.
   */
  public String getTargetDirectory() {
    return targetDirectory;
  }

  /**
   * @param targetDirectory
   *          The targetDirectory to set.
   */
  public void setTargetDirectory( String targetDirectory ) {
    this.targetDirectory = targetDirectory;
  }

  @Override
  public String getDialogClassName() {
    return JobEntryDummyDialog.class.getName();
  }

  @Override
  public Result execute( Result prev_result, int nr ) {
    Result result = new Result( nr );
    result.setResult( false );
    long filesRetrieved = 0;

    logDetailed( toString(), "Start of processing" );

    // String substitution..
    String realWildcard = environmentSubstitute( wildcard );
    String realTargetDirectory = environmentSubstitute( targetDirectory );
    String realSourceDirectory = environmentSubstitute( sourceDirectory );
    DummyJob proc = new DummyJob( realSourceDirectory, realTargetDirectory,
        realWildcard );

    try {
      filesRetrieved = proc.process();
      result.setResult( true );
      result.setNrFilesRetrieved( filesRetrieved );
    } catch ( Exception e ) {
      result.setNrErrors( 1 );
      e.printStackTrace();
      logError( toString(), "Error processing DummyJob : " + e.getMessage() );
    }

    return result;
  }

  @Override
  public boolean evaluates() {
    return true;
  }
}
