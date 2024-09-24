/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package org.pentaho.di.job.entries.abort;

import org.pentaho.di.core.annotations.JobEntry;
import org.pentaho.di.job.entry.validator.JobEntryValidatorUtils;

import java.util.List;

import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.CheckResultInterface;
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
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Node;

/**
 * Job entry type to abort a job.
 *
 * @author Samatar
 * @since 12-02-2007
 */
@JobEntry( id = "ABORT",
  i18nPackageName = "org.pentaho.di.job.entries.abort",
  name = "JobEntryAbort.Name",
  description = "JobEntryAbort.Description",
  categoryDescription = "i18n:org.pentaho.di.job:JobCategory.Category.Utility" )
public class JobEntryAbort extends JobEntryBase implements Cloneable, JobEntryInterface {
  private static Class<?> PKG = JobEntryAbort.class; // for i18n purposes, needed by Translator2!!

  private String messageAbort;

  public JobEntryAbort( String n, String scr ) {
    super( n, "" );
    messageAbort = null;
  }

  public JobEntryAbort() {
    this( "", "" );
  }

  public Object clone() {
    JobEntryAbort je = (JobEntryAbort) super.clone();
    return je;
  }

  public String getXML() {
    StringBuilder retval = new StringBuilder();

    retval.append( super.getXML() );
    retval.append( "      " ).append( XMLHandler.addTagValue( "message", messageAbort ) );

    return retval.toString();
  }

  public void loadXML( Node entrynode, List<DatabaseMeta> databases, List<SlaveServer> slaveServers,
    Repository rep, IMetaStore metaStore ) throws KettleXMLException {
    try {
      super.loadXML( entrynode, databases, slaveServers );
      messageAbort = XMLHandler.getTagValue( entrynode, "message" );
    } catch ( Exception e ) {
      throw new KettleXMLException( BaseMessages.getString( PKG, "JobEntryAbort.UnableToLoadFromXml.Label" ), e );
    }
  }

  public void loadRep( Repository rep, IMetaStore metaStore, ObjectId id_jobentry, List<DatabaseMeta> databases,
    List<SlaveServer> slaveServers ) throws KettleException {
    try {
      messageAbort = rep.getJobEntryAttributeString( id_jobentry, "message" );
    } catch ( KettleDatabaseException dbe ) {
      throw new KettleException( BaseMessages.getString( PKG, "JobEntryAbort.UnableToLoadFromRepo.Label", String
        .valueOf( id_jobentry ) ), dbe );
    }
  }

  // Save the attributes of this job entry
  //
  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_job ) throws KettleException {
    try {
      rep.saveJobEntryAttribute( id_job, getObjectId(), "message", messageAbort );

    } catch ( KettleDatabaseException dbe ) {
      throw new KettleException( BaseMessages.getString( PKG, "JobEntryAbort.UnableToSaveToRepo.Label", String
        .valueOf( id_job ) ), dbe );
    }
  }

  public boolean evaluate( Result result ) {
    String Returnmessage = null;
    String RealMessageabort = environmentSubstitute( getMessageabort() );

    try {
      // Return False
      if ( RealMessageabort == null ) {
        Returnmessage = BaseMessages.getString( PKG, "JobEntryAbort.Meta.CheckResult.Label" );
      } else {
        Returnmessage = RealMessageabort;

      }
      logError( Returnmessage );
      result.setNrErrors( 1 );
      return false;
    } catch ( Exception e ) {
      result.setNrErrors( 1 );
      logError( BaseMessages.getString( PKG, "JobEntryAbort.Meta.CheckResult.CouldNotExecute" ) + e.toString() );
      return false;
    }
  }

  /**
   * Execute this job entry and return the result. In this case it means, just set the result boolean in the Result
   * class.
   *
   * @param previousResult
   *          The result of the previous execution
   * @return The Result of the execution.
   */
  public Result execute( Result previousResult, int nr ) {
    previousResult.setResult( evaluate( previousResult ) );
    // we fail so stop
    // job execution
    parentJob.stopAll();
    return previousResult;
  }

  public boolean resetErrorsBeforeExecution() {
    // we should be able to evaluate the errors in
    // the previous jobentry.
    return false;
  }

  public boolean evaluates() {
    return true;
  }

  public boolean isUnconditional() {
    return false;
  }

  public void setMessageabort( String messageabort ) {
    this.messageAbort = messageabort;
  }

  public String getMessageabort() {
    return messageAbort;
  }

  public void check( List<CheckResultInterface> remarks, JobMeta jobMeta, VariableSpace space,
    Repository repository, IMetaStore metaStore ) {
    JobEntryValidatorUtils.addOkRemark( this, "messageabort", remarks );
  }
}
