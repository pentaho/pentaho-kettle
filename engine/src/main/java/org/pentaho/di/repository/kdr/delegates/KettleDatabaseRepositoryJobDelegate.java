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

package org.pentaho.di.repository.kdr.delegates;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.NotePadMeta;
import org.pentaho.di.core.ProgressMonitorListener;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogTableInterface;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaBoolean;
import org.pentaho.di.core.row.value.ValueMetaDate;
import org.pentaho.di.core.row.value.ValueMetaInteger;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.JobHopMeta;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entries.missing.MissingEntry;
import org.pentaho.di.job.entry.JobEntryCopy;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.LongObjectId;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.RepositoryAttributeInterface;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.repository.RepositoryObjectType;
import org.pentaho.di.repository.kdr.KettleDatabaseRepository;
import org.pentaho.di.shared.SharedObjects;

public class KettleDatabaseRepositoryJobDelegate extends KettleDatabaseRepositoryBaseDelegate {

  private static Class<?> PKG = JobMeta.class; // for i18n purposes, needed by Translator2!!

  public static final String JOB_ATTRIBUTE_PREFIX = "_ATTR_" + '\t';

  public KettleDatabaseRepositoryJobDelegate( KettleDatabaseRepository repository ) {
    super( repository );
  }

  public RowMetaAndData getJob( ObjectId id_job ) throws KettleException {
    return repository.connectionDelegate.getOneRow(
      quoteTable( KettleDatabaseRepository.TABLE_R_JOB ), quote( KettleDatabaseRepository.FIELD_JOB_ID_JOB ),
      id_job );
  }

  public RowMetaAndData getJobHop( ObjectId id_job_hop ) throws KettleException {
    return repository.connectionDelegate.getOneRow(
      quoteTable( KettleDatabaseRepository.TABLE_R_JOB_HOP ),
      quote( KettleDatabaseRepository.FIELD_JOB_HOP_ID_JOB_HOP ), id_job_hop );
  }

  public synchronized ObjectId getJobHopID( ObjectId id_job, ObjectId id_jobentry_copy_from,
    ObjectId id_jobentry_copy_to ) throws KettleException {
    String[] lookupkey =
      new String[] {
        quote( KettleDatabaseRepository.FIELD_JOB_HOP_ID_JOB ),
        quote( KettleDatabaseRepository.FIELD_JOB_HOP_ID_JOBENTRY_COPY_FROM ),
        quote( KettleDatabaseRepository.FIELD_JOB_HOP_ID_JOBENTRY_COPY_TO ), };
    ObjectId[] key = new ObjectId[] { id_job, id_jobentry_copy_from, id_jobentry_copy_to };

    return repository.connectionDelegate.getIDWithValue(
      quoteTable( KettleDatabaseRepository.TABLE_R_JOB_HOP ),
      quote( KettleDatabaseRepository.FIELD_JOB_HOP_ID_JOB_HOP ), lookupkey, key );
  }

  /**
   * Stored a job in the repository
   *
   * @param jobMeta
   *          The job to store
   * @param monitor
   *          the (optional) UI progress monitor
   * @param overwrite
   *          Overwrite existing object(s)?
   * @throws KettleException
   *           in case some IO error occurs.
   */
  public void saveJob( JobMeta jobMeta, String versionComment, ProgressMonitorListener monitor, boolean overwrite ) throws KettleException {
    try {

      // Before saving the job, see if it's not locked by someone else...
      //
      int nrWorks = 2 + jobMeta.nrDatabases() + jobMeta.nrNotes() + jobMeta.nrJobEntries() + jobMeta.nrJobHops();
      if ( monitor != null ) {
        monitor.beginTask( BaseMessages.getString( PKG, "JobMeta.Monitor.SavingTransformation" )
          + jobMeta.getRepositoryDirectory() + Const.FILE_SEPARATOR + jobMeta.getName(), nrWorks );
      }

      repository.insertLogEntry( "save job '" + jobMeta.getName() + "'" );

      // Before we start, make sure we have a valid job ID!
      // Two possibilities:
      // 1) We have a ID: keep it
      // 2) We don't have an ID: look it up.
      // If we find a transformation with the same name: ask!
      //
      if ( monitor != null ) {
        monitor.subTask( BaseMessages.getString( PKG, "JobMeta.Monitor.HandlingPreviousVersionOfJob" ) );
      }
      jobMeta.setObjectId( getJobID( jobMeta.getName(), jobMeta.getRepositoryDirectory().getObjectId() ) );

      // If no valid id is available in the database, assign one...
      if ( jobMeta.getObjectId() == null ) {
        jobMeta.setObjectId( repository.connectionDelegate.getNextJobID() );
      } else {
        // If we have a valid ID, we need to make sure everything is
        // cleared out
        // of the database for this id_job, before we put it back in...
        repository.deleteJob( jobMeta.getObjectId() );
      }
      if ( monitor != null ) {
        monitor.worked( 1 );
      }

      // First of all we need to verify that all database connections are
      // saved.
      //
      if ( log.isDebug() ) {
        log.logDebug( BaseMessages.getString( PKG, "JobMeta.Log.SavingDatabaseConnections" ) );
      }
      for ( int i = 0; i < jobMeta.nrDatabases(); i++ ) {
        if ( monitor != null ) {
          monitor.subTask( BaseMessages.getString( PKG, "JobMeta.Monitor.SavingDatabaseTask.Title" )
            + ( i + 1 ) + "/" + jobMeta.nrDatabases() );
        }
        DatabaseMeta databaseMeta = jobMeta.getDatabase( i );
        // Save the database connection if we're overwriting objects or (it has changed and
        // nothing was saved in the repository)
        if ( overwrite || databaseMeta.hasChanged() || databaseMeta.getObjectId() == null ) {
          repository.save( databaseMeta, versionComment, monitor, overwrite );
        }
        if ( monitor != null ) {
          monitor.worked( 1 );
        }
      }

      // Now, save the job entry in R_JOB
      // Note, we save this first so that we have an ID in the database.
      // Everything else depends on this ID, including recursive job
      // entries to the save job. (retry)
      if ( monitor != null ) {
        monitor.subTask( BaseMessages.getString( PKG, "JobMeta.Monitor.SavingJobDetails" ) );
      }
      if ( log.isDetailed() ) {
        log.logDetailed( "Saving job info to repository..." );
      }

      insertJob( jobMeta );

      if ( monitor != null ) {
        monitor.worked( 1 );
      }

      // Save the group attributes map
      //
      saveJobAttributesMap( jobMeta.getObjectId(), jobMeta.getAttributesMap() );

      // Save the slaves
      //
      for ( int i = 0; i < jobMeta.getSlaveServers().size(); i++ ) {
        SlaveServer slaveServer = jobMeta.getSlaveServers().get( i );
        repository.save( slaveServer, versionComment, null, jobMeta.getObjectId(), false, overwrite );
      }

      //
      // Save the notes
      //
      if ( log.isDetailed() ) {
        log.logDetailed( "Saving notes to repository..." );
      }
      for ( int i = 0; i < jobMeta.nrNotes(); i++ ) {
        if ( monitor != null ) {
          monitor.subTask( BaseMessages.getString( PKG, "JobMeta.Monitor.SavingNoteNr" )
            + ( i + 1 ) + "/" + jobMeta.nrNotes() );
        }
        NotePadMeta ni = jobMeta.getNote( i );
        repository.saveNotePadMeta( ni, jobMeta.getObjectId() );
        if ( ni.getObjectId() != null ) {
          repository.insertJobNote( jobMeta.getObjectId(), ni.getObjectId() );
        }
        if ( monitor != null ) {
          monitor.worked( 1 );
        }
      }

      //
      // Save the job entries
      //
      if ( log.isDetailed() ) {
        log.logDetailed( "Saving " + jobMeta.nrJobEntries() + " Job enty copies to repository..." );
      }
      repository.updateJobEntryTypes();
      for ( int i = 0; i < jobMeta.nrJobEntries(); i++ ) {
        if ( monitor != null ) {
          monitor.subTask( BaseMessages.getString( PKG, "JobMeta.Monitor.SavingJobEntryNr" )
            + ( i + 1 ) + "/" + jobMeta.nrJobEntries() );
        }
        JobEntryCopy cge = jobMeta.getJobEntry( i );
        repository.jobEntryDelegate.saveJobEntryCopy( cge, jobMeta.getObjectId(), repository.metaStore );
        if ( monitor != null ) {
          monitor.worked( 1 );
        }
      }

      if ( log.isDetailed() ) {
        log.logDetailed( "Saving job hops to repository..." );
      }
      for ( int i = 0; i < jobMeta.nrJobHops(); i++ ) {
        if ( monitor != null ) {
          monitor.subTask( "Saving job hop #" + ( i + 1 ) + "/" + jobMeta.nrJobHops() );
        }
        JobHopMeta hi = jobMeta.getJobHop( i );
        saveJobHopMeta( hi, jobMeta.getObjectId() );
        if ( monitor != null ) {
          monitor.worked( 1 );
        }
      }

      saveJobParameters( jobMeta );

      // Commit this transaction!!
      repository.commit();

      jobMeta.clearChanged();
      if ( monitor != null ) {
        monitor.done();
      }
    } catch ( KettleDatabaseException dbe ) {
      repository.rollback();
      throw new KettleException( BaseMessages.getString(
        PKG, "JobMeta.Exception.UnableToSaveJobInRepositoryRollbackPerformed" ), dbe );
    }
  }

  /**
   * Save the parameters of this job to the repository.
   *
   * @param rep
   *          The repository to save to.
   *
   * @throws KettleException
   *           Upon any error.
   */
  private void saveJobParameters( JobMeta jobMeta ) throws KettleException {
    String[] paramKeys = jobMeta.listParameters();
    for ( int idx = 0; idx < paramKeys.length; idx++ ) {
      String desc = jobMeta.getParameterDescription( paramKeys[idx] );
      String defValue = jobMeta.getParameterDefault( paramKeys[idx] );
      insertJobParameter( jobMeta.getObjectId(), idx, paramKeys[idx], defValue, desc );
    }
  }

  public boolean existsJobMeta( String name, RepositoryDirectoryInterface repositoryDirectory,
    RepositoryObjectType objectType ) throws KettleException {
    try {
      return ( getJobID( name, repositoryDirectory.getObjectId() ) != null );
    } catch ( KettleException e ) {
      throw new KettleException( "Unable to verify if the job with name ["
        + name + "] in directory [" + repositoryDirectory + "] exists", e );
    }
  }

  /**
   * Load a job from the repository
   *
   * @param jobname
   *          The name of the job
   * @param repdir
   *          The directory in which the job resides.
   * @throws KettleException
   */
  public JobMeta loadJobMeta( String jobname, RepositoryDirectoryInterface repdir ) throws KettleException {
    return loadJobMeta( jobname, repdir, null );
  }

  /**
   * Load a job in a directory
   *
   * @param log
   *          the logging channel
   * @param rep
   *          The Repository
   * @param jobname
   *          The name of the job
   * @param repdir
   *          The directory in which the job resides.
   * @throws KettleException
   */
  public JobMeta loadJobMeta( String jobname, RepositoryDirectoryInterface repdir, ProgressMonitorListener monitor ) throws KettleException {

    JobMeta jobMeta = new JobMeta();
    synchronized ( repository ) {
      try {
        // Clear everything...
        jobMeta.clear();

        jobMeta.setRepositoryDirectory( repdir );

        // Get the transformation id
        jobMeta.setObjectId( getJobID( jobname, repdir.getObjectId() ) );

        // If no valid id is available in the database, then give error...
        if ( jobMeta.getObjectId() != null ) {
          // Load the notes...
          ObjectId[] noteids = repository.getJobNoteIDs( jobMeta.getObjectId() );
          ObjectId[] jecids = repository.getJobEntryCopyIDs( jobMeta.getObjectId() );
          ObjectId[] hopid = repository.getJobHopIDs( jobMeta.getObjectId() );

          int nrWork = 2 + noteids.length + jecids.length + hopid.length;
          if ( monitor != null ) {
            monitor.beginTask( BaseMessages.getString( PKG, "JobMeta.Monitor.LoadingJob" )
              + repdir + Const.FILE_SEPARATOR + jobname, nrWork );
          }

          //
          // get job info:
          //
          if ( monitor != null ) {
            monitor.subTask( BaseMessages.getString( PKG, "JobMeta.Monitor.ReadingJobInformation" ) );
          }
          RowMetaAndData jobRow = getJob( jobMeta.getObjectId() );

          jobMeta.setName( jobRow.getString( KettleDatabaseRepository.FIELD_JOB_NAME, null ) );
          jobMeta.setDescription( jobRow.getString( KettleDatabaseRepository.FIELD_JOB_DESCRIPTION, null ) );
          jobMeta.setExtendedDescription( jobRow.getString(
            KettleDatabaseRepository.FIELD_JOB_EXTENDED_DESCRIPTION, null ) );
          jobMeta.setJobversion( jobRow.getString( KettleDatabaseRepository.FIELD_JOB_JOB_VERSION, null ) );
          jobMeta.setJobstatus( Const.toInt( jobRow
            .getString( KettleDatabaseRepository.FIELD_JOB_JOB_STATUS, null ), -1 ) );

          jobMeta.setCreatedUser( jobRow.getString( KettleDatabaseRepository.FIELD_JOB_CREATED_USER, null ) );
          jobMeta.setCreatedDate( jobRow.getDate( KettleDatabaseRepository.FIELD_JOB_CREATED_DATE, new Date() ) );

          jobMeta.setModifiedUser( jobRow.getString( KettleDatabaseRepository.FIELD_JOB_MODIFIED_USER, null ) );
          jobMeta.setModifiedDate( jobRow.getDate( KettleDatabaseRepository.FIELD_JOB_MODIFIED_DATE, new Date() ) );

          long id_logdb = jobRow.getInteger( KettleDatabaseRepository.FIELD_JOB_ID_DATABASE_LOG, 0 );
          if ( id_logdb > 0 ) {
            // Get the logconnection
            //
            DatabaseMeta logDb = repository.loadDatabaseMeta( new LongObjectId( id_logdb ), null );
            jobMeta.getJobLogTable().setConnectionName( logDb.getName() );
            // jobMeta.getJobLogTable().getDatabaseMeta().shareVariablesWith(jobMeta);
          }

          jobMeta.getJobLogTable().setTableName(
            jobRow.getString( KettleDatabaseRepository.FIELD_JOB_TABLE_NAME_LOG, null ) );
          jobMeta.getJobLogTable().setBatchIdUsed(
            jobRow.getBoolean( KettleDatabaseRepository.FIELD_JOB_USE_BATCH_ID, false ) );
          jobMeta.getJobLogTable().setLogFieldUsed(
            jobRow.getBoolean( KettleDatabaseRepository.FIELD_JOB_USE_LOGFIELD, false ) );
          jobMeta.getJobLogTable().setLogSizeLimit(
            getJobAttributeString(
              jobMeta.getObjectId(), 0, KettleDatabaseRepository.JOB_ATTRIBUTE_LOG_SIZE_LIMIT ) );

          jobMeta.setBatchIdPassed( jobRow.getBoolean( KettleDatabaseRepository.FIELD_JOB_PASS_BATCH_ID, false ) );

          // Load all the log tables for the job...
          //
          RepositoryAttributeInterface attributeInterface =
            new KettleDatabaseRepositoryJobAttribute( repository.connectionDelegate, jobMeta.getObjectId() );
          for ( LogTableInterface logTable : jobMeta.getLogTables() ) {
            logTable.loadFromRepository( attributeInterface );
          }

          if ( monitor != null ) {
            monitor.worked( 1 );
          }
          //
          // Load the common database connections
          //
          if ( monitor != null ) {
            monitor.subTask( BaseMessages.getString(
              PKG, "JobMeta.Monitor.ReadingAvailableDatabasesFromRepository" ) );
          }
          // Read objects from the shared XML file & the repository
          try {
            jobMeta
              .setSharedObjectsFile( jobRow.getString( KettleDatabaseRepository.FIELD_JOB_SHARED_FILE, null ) );
            jobMeta.setSharedObjects( repository != null
              ? repository.readJobMetaSharedObjects( jobMeta ) : jobMeta.readSharedObjects() );
          } catch ( Exception e ) {
            log
              .logError( BaseMessages.getString( PKG, "JobMeta.ErrorReadingSharedObjects.Message", e.toString() ) );
            //
            log.logError( Const.getStackTracker( e ) );
          }
          if ( monitor != null ) {
            monitor.worked( 1 );
          }

          if ( log.isDetailed() ) {
            log.logDetailed( "Loading " + noteids.length + " notes" );
          }
          for ( int i = 0; i < noteids.length; i++ ) {
            if ( monitor != null ) {
              monitor.subTask( BaseMessages.getString( PKG, "JobMeta.Monitor.ReadingNoteNr" )
                + ( i + 1 ) + "/" + noteids.length );
            }
            NotePadMeta ni = repository.notePadDelegate.loadNotePadMeta( noteids[i] );
            if ( jobMeta.indexOfNote( ni ) < 0 ) {
              jobMeta.addNote( ni );
            }
            if ( monitor != null ) {
              monitor.worked( 1 );
            }
          }

          // Load the group attributes map
          //
          jobMeta.setAttributesMap( loadJobAttributesMap( jobMeta.getObjectId() ) );

          // Load the job entries...
          //

          // Keep a unique list of job entries to facilitate in the loading.
          //
          List<JobEntryInterface> jobentries = new ArrayList<JobEntryInterface>();
          if ( log.isDetailed() ) {
            log.logDetailed( "Loading " + jecids.length + " job entries" );
          }
          for ( int i = 0; i < jecids.length; i++ ) {
            if ( monitor != null ) {
              monitor.subTask( BaseMessages.getString( PKG, "JobMeta.Monitor.ReadingJobEntryNr" )
                + ( i + 1 ) + "/" + ( jecids.length ) );
            }

            JobEntryCopy jec =
              repository.jobEntryDelegate.loadJobEntryCopy(
                jobMeta.getObjectId(), jecids[i], jobentries, jobMeta.getDatabases(), jobMeta
                  .getSlaveServers(), jobname );

            if ( jec.isMissing() ) {
              jobMeta.addMissingEntry( (MissingEntry) jec.getEntry() );
            }

            // Also set the copy number...
            // We count the number of job entry copies that use the job
            // entry
            //
            int copyNr = 0;
            for ( JobEntryCopy copy : jobMeta.getJobCopies() ) {
              if ( jec.getEntry() == copy.getEntry() ) {
                copyNr++;
              }
            }
            jec.setNr( copyNr );

            int idx = jobMeta.indexOfJobEntry( jec );
            if ( idx < 0 ) {
              if ( jec.getName() != null && jec.getName().length() > 0 ) {
                jobMeta.addJobEntry( jec );
              }
            } else {
              jobMeta.setJobEntry( idx, jec ); // replace it!
            }
            if ( monitor != null ) {
              monitor.worked( 1 );
            }
          }

          // Load the hops...
          if ( log.isDetailed() ) {
            log.logDetailed( "Loading " + hopid.length + " job hops" );
          }
          for ( int i = 0; i < hopid.length; i++ ) {
            if ( monitor != null ) {
              monitor.subTask( BaseMessages.getString( PKG, "JobMeta.Monitor.ReadingJobHopNr" )
                + ( i + 1 ) + "/" + ( jecids.length ) );
            }
            JobHopMeta hi = loadJobHopMeta( hopid[i], jobMeta.getJobCopies() );
            jobMeta.getJobhops().add( hi );
            if ( monitor != null ) {
              monitor.worked( 1 );
            }
          }

          loadRepParameters( jobMeta );

          // Finally, clear the changed flags...
          jobMeta.clearChanged();
          if ( monitor != null ) {
            monitor.subTask( BaseMessages.getString( PKG, "JobMeta.Monitor.FinishedLoadOfJob" ) );
          }
          if ( monitor != null ) {
            monitor.done();
          }

          // close prepared statements, minimize locking etc.
          //
          repository.connectionDelegate.closeAttributeLookupPreparedStatements();

          return jobMeta;
        } else {
          throw new KettleException( BaseMessages.getString( PKG, "JobMeta.Exception.CanNotFindJob" ) + jobname );
        }
      } catch ( KettleException dbe ) {
        throw new KettleException( BaseMessages.getString(
          PKG, "JobMeta.Exception.AnErrorOccuredReadingJob", jobname ), dbe );
      } finally {
        jobMeta.initializeVariablesFrom( jobMeta.getParentVariableSpace() );
        jobMeta.setInternalKettleVariables();
      }
    }
  }

  /**
   * Load the parameters of this job from the repository. The current ones already loaded will be erased.
   *
   * @param jobMeta
   *          The target job for the parameters
   *
   * @throws KettleException
   *           Upon any error.
   *
   */
  private void loadRepParameters( JobMeta jobMeta ) throws KettleException {
    jobMeta.eraseParameters();

    int count = countJobParameter( jobMeta.getObjectId() );
    for ( int idx = 0; idx < count; idx++ ) {
      String key = getJobParameterKey( jobMeta.getObjectId(), idx );
      String defValue = getJobParameterDefault( jobMeta.getObjectId(), idx );
      String desc = getJobParameterDescription( jobMeta.getObjectId(), idx );
      jobMeta.addParameterDefinition( key, defValue, desc );
    }
  }

  /**
   * Get a job parameter key. You can count the number of parameters up front.
   *
   * @param id_job
   *          job id
   * @param nr
   *          number of the parameter
   * @return they key/name of specified parameter
   *
   * @throws KettleException
   *           Upon any error.
   */
  public String getJobParameterKey( ObjectId id_job, int nr ) throws KettleException {
    return repository.connectionDelegate.getJobAttributeString(
      id_job, nr, KettleDatabaseRepository.JOB_ATTRIBUTE_PARAM_KEY );
  }

  /**
   * Get a job parameter default. You can count the number of parameters up front.
   *
   * @param id_job
   *          job id
   * @param nr
   *          number of the parameter
   * @return the default value of the parameter
   *
   * @throws KettleException
   *           Upon any error.
   */
  public String getJobParameterDefault( ObjectId id_job, int nr ) throws KettleException {
    return repository.connectionDelegate.getJobAttributeString(
      id_job, nr, KettleDatabaseRepository.JOB_ATTRIBUTE_PARAM_DEFAULT );
  }

  /**
   * Get a job parameter description. You can count the number of parameters up front.
   *
   * @param id_job
   *          job id
   * @param nr
   *          number of the parameter
   * @return the description of the parameter
   *
   * @throws KettleException
   *           Upon any error.
   */
  public String getJobParameterDescription( ObjectId id_job, int nr ) throws KettleException {
    return repository.connectionDelegate.getJobAttributeString(
      id_job, nr, KettleDatabaseRepository.JOB_ATTRIBUTE_PARAM_DESCRIPTION );
  }

  /**
   * Insert a parameter for a job in the repository.
   *
   * @param id_job
   *          job id
   * @param nr
   *          number of the parameter to insert
   * @param key
   *          key to insert
   * @param defValue
   *          default value for key
   * @param description
   *          description to insert
   *
   * @throws KettleException
   *           Upon any error.
   */
  public void insertJobParameter( ObjectId id_job, long nr, String key, String defValue, String description ) throws KettleException {
    repository.connectionDelegate.insertJobAttribute(
      id_job, nr, KettleDatabaseRepository.JOB_ATTRIBUTE_PARAM_KEY, 0, key != null ? key : "" );
    repository.connectionDelegate.insertJobAttribute(
      id_job, nr, KettleDatabaseRepository.JOB_ATTRIBUTE_PARAM_DEFAULT, 0, defValue != null ? defValue : "" );
    repository.connectionDelegate.insertJobAttribute(
      id_job, nr, KettleDatabaseRepository.JOB_ATTRIBUTE_PARAM_DESCRIPTION, 0, description != null
        ? description : "" );
  }

  /**
   * Count the number of parameters of a job.
   *
   * @param id_job
   *          job id
   * @return the number of of parameters of the job
   *
   * @throws KettleException
   *           Upon any error.
   */
  public int countJobParameter( ObjectId id_job ) throws KettleException {
    return repository.connectionDelegate.countNrJobAttributes(
      id_job, KettleDatabaseRepository.JOB_ATTRIBUTE_PARAM_KEY );
  }

  public JobHopMeta loadJobHopMeta( ObjectId id_job_hop, List<JobEntryCopy> jobcopies ) throws KettleException {
    JobHopMeta jobHopMeta = new JobHopMeta();
    try {
      RowMetaAndData r = getJobHop( id_job_hop );
      if ( r != null ) {
        long id_jobentry_copy_from = r.getInteger( "ID_JOBENTRY_COPY_FROM", -1L );
        long id_jobentry_copy_to = r.getInteger( "ID_JOBENTRY_COPY_TO", -1L );

        jobHopMeta.setEnabled( r.getBoolean( "ENABLED", true ) );
        jobHopMeta.setEvaluation( r.getBoolean( "EVALUATION", true ) );
        jobHopMeta.setConditional();
        if ( r.getBoolean( "UNCONDITIONAL", !jobHopMeta.getEvaluation() ) ) {
          jobHopMeta.setUnconditional();
        }

        jobHopMeta.setFromEntry( JobMeta.findJobEntryCopy( jobcopies, new LongObjectId( id_jobentry_copy_from ) ) );
        jobHopMeta.setToEntry( JobMeta.findJobEntryCopy( jobcopies, new LongObjectId( id_jobentry_copy_to ) ) );

        return jobHopMeta;
      } else {
        throw new KettleException( "Unable to find job hop with ID : " + id_job_hop );
      }
    } catch ( KettleDatabaseException dbe ) {
      throw new KettleException( BaseMessages.getString( PKG, "JobHopMeta.Exception.UnableToLoadHopInfoRep", ""
        + id_job_hop ), dbe );

    }
  }

  public void saveJobHopMeta( JobHopMeta hop, ObjectId id_job ) throws KettleException {
    try {
      ObjectId id_jobentry_from = null;
      ObjectId id_jobentry_to = null;

      id_jobentry_from = hop.getFromEntry() == null ? null : hop.getFromEntry().getObjectId();
      id_jobentry_to = hop.getToEntry() == null ? null : hop.getToEntry().getObjectId();

      // Insert new job hop in repository
      //
      hop.setObjectId( insertJobHop( id_job, id_jobentry_from, id_jobentry_to, hop.isEnabled(), hop
        .getEvaluation(), hop.isUnconditional() ) );
    } catch ( KettleDatabaseException dbe ) {
      throw new KettleException( BaseMessages.getString( PKG, "JobHopMeta.Exception.UnableToSaveHopInfoRep", ""
        + id_job ), dbe );

    }
  }

  /**
   * Read the database connections in the repository and add them to this job if they are not yet present.
   *
   * @param jobMeta
   *          the job to put the database connections in
   * @throws KettleException
   */
  public void readDatabases( JobMeta jobMeta ) throws KettleException {
    readDatabases( jobMeta, true );
  }

  /**
   * Read the database connections in the repository and add them to this job if they are not yet present.
   *
   * @param jobMeta
   *          the job to put the database connections in
   * @param overWriteShared
   *          set to true if you want to overwrite shared connections while loading.
   * @throws KettleException
   */
  public void readDatabases( JobMeta jobMeta, boolean overWriteShared ) throws KettleException {
    try {
      ObjectId[] dbids = repository.getDatabaseIDs( false );
      for ( int i = 0; i < dbids.length; i++ ) {
        DatabaseMeta databaseMeta = repository.loadDatabaseMeta( dbids[i], null ); // reads last version
        databaseMeta.shareVariablesWith( jobMeta );

        // See if there already is one in the transformation
        //
        DatabaseMeta check = jobMeta.findDatabase( databaseMeta.getName() );

        // We only add, never overwrite database connections.
        //
        if ( check == null || overWriteShared ) {
          if ( databaseMeta.getName() != null ) {
            jobMeta.addOrReplaceDatabase( databaseMeta );
            if ( !overWriteShared ) {
              databaseMeta.setChanged( false );
            }
          }
        }
      }
      jobMeta.setChanged( false );
    } catch ( KettleDatabaseException dbe ) {
      throw new KettleException(
        BaseMessages.getString( PKG, "JobMeta.Log.UnableToReadDatabaseIDSFromRepository" ), dbe );
    } catch ( KettleException ke ) {
      throw new KettleException(
        BaseMessages.getString( PKG, "JobMeta.Log.UnableToReadDatabasesFromRepository" ), ke );
    }
  }

  /**
   * Read the slave servers in the repository and add them to this transformation if they are not yet present.
   *
   * @param jobMeta
   *          The job to put the slave servers in
   * @param overWriteShared
   *          if an object with the same name exists, overwrite
   * @throws KettleException
   */
  public void readSlaves( JobMeta jobMeta, boolean overWriteShared ) throws KettleException {
    try {
      ObjectId[] dbids = repository.getSlaveIDs( false );
      for ( int i = 0; i < dbids.length; i++ ) {
        SlaveServer slaveServer = repository.loadSlaveServer( dbids[i], null ); // Load last version
        slaveServer.shareVariablesWith( jobMeta );

        SlaveServer check = jobMeta.findSlaveServer( slaveServer.getName() ); // Check if there already is one in the
                                                                              // transformation
        if ( check == null || overWriteShared ) {
          if ( !Utils.isEmpty( slaveServer.getName() ) ) {
            jobMeta.addOrReplaceSlaveServer( slaveServer );
            if ( !overWriteShared ) {
              slaveServer.setChanged( false );
            }
          }
        }
      }
    } catch ( KettleDatabaseException dbe ) {
      throw new KettleException( BaseMessages
        .getString( PKG, "JobMeta.Log.UnableToReadSlaveServersFromRepository" ), dbe );
    }
  }

  public SharedObjects readSharedObjects( JobMeta jobMeta ) throws KettleException {
    jobMeta.setSharedObjects( jobMeta.readSharedObjects() );

    readDatabases( jobMeta, true );
    readSlaves( jobMeta, true );

    return jobMeta.getSharedObjects();
  }

  public synchronized ObjectId getJobID( String name, ObjectId id_directory ) throws KettleException {
    return repository.connectionDelegate.getIDWithValue(
      quoteTable( KettleDatabaseRepository.TABLE_R_JOB ), quote( KettleDatabaseRepository.FIELD_JOB_ID_JOB ),
      quote( KettleDatabaseRepository.FIELD_JOB_NAME ), name,
      quote( KettleDatabaseRepository.FIELD_JOB_ID_DIRECTORY ), id_directory );
  }

  public synchronized int getNrJobs() throws KettleException {
    int retval = 0;

    String sql = "SELECT COUNT(*) FROM " + quoteTable( KettleDatabaseRepository.TABLE_R_JOB );
    RowMetaAndData r = repository.connectionDelegate.getOneRow( sql );
    if ( r != null ) {
      retval = (int) r.getInteger( 0, 0L );
    }

    return retval;
  }

  public synchronized int getNrJobs( ObjectId id_directory ) throws KettleException {
    int retval = 0;

    RowMetaAndData par = repository.connectionDelegate.getParameterMetaData( id_directory );
    String sql =
      "SELECT COUNT(*) FROM "
        + quoteTable( KettleDatabaseRepository.TABLE_R_JOB ) + " WHERE "
        + quote( KettleDatabaseRepository.FIELD_JOB_ID_DIRECTORY ) + " = ? ";
    RowMetaAndData r = repository.connectionDelegate.getOneRow( sql, par.getRowMeta(), par.getData() );
    if ( r != null ) {
      retval = (int) r.getInteger( 0, 0L );
    }

    return retval;
  }

  public synchronized int getNrJobHops( ObjectId id_job ) throws KettleException {
    int retval = 0;

    RowMetaAndData par = repository.connectionDelegate.getParameterMetaData( id_job );
    String sql =
      "SELECT COUNT(*) FROM "
        + quoteTable( KettleDatabaseRepository.TABLE_R_JOB_HOP ) + " WHERE "
        + quote( KettleDatabaseRepository.FIELD_JOB_HOP_ID_JOB ) + " = ? ";
    RowMetaAndData r = repository.connectionDelegate.getOneRow( sql, par.getRowMeta(), par.getData() );
    if ( r != null ) {
      retval = (int) r.getInteger( 0, 0L );
    }

    return retval;
  }

  public String[] getJobsWithIDList( List<Object[]> list, RowMetaInterface rowMeta ) throws KettleException {
    String[] jobList = new String[list.size()];
    for ( int i = 0; i < list.size(); i++ ) {
      long id_job = rowMeta.getInteger( list.get( i ), quote( KettleDatabaseRepository.FIELD_JOB_ID_JOB ), -1L );
      if ( id_job > 0 ) {
        RowMetaAndData jobRow = getJob( new LongObjectId( id_job ) );
        if ( jobRow != null ) {
          String jobName = jobRow.getString( KettleDatabaseRepository.FIELD_JOB_NAME, "<name not found>" );
          long id_directory = jobRow.getInteger( KettleDatabaseRepository.FIELD_JOB_ID_DIRECTORY, -1L );
          RepositoryDirectoryInterface dir =
            repository.loadRepositoryDirectoryTree().findDirectory( new LongObjectId( id_directory ) ); // always
                                                                                                        // reload the
                                                                                                        // directory
                                                                                                        // tree!

          jobList[i] = dir.getPathObjectCombination( jobName );
        }
      }
    }

    return jobList;
  }

  public String[] getJobsWithIDList( ObjectId[] ids ) throws KettleException {
    String[] jobsList = new String[ids.length];
    for ( int i = 0; i < ids.length; i++ ) {
      ObjectId id_job = ids[i];
      if ( id_job != null ) {
        RowMetaAndData transRow = getJob( id_job );
        if ( transRow != null ) {
          String jobName = transRow.getString( KettleDatabaseRepository.FIELD_JOB_NAME, "<name not found>" );
          long id_directory = transRow.getInteger( KettleDatabaseRepository.FIELD_JOB_ID_DIRECTORY, -1L );
          RepositoryDirectoryInterface dir =
            repository.loadRepositoryDirectoryTree().findDirectory( new LongObjectId( id_directory ) );

          jobsList[i] = dir.getPathObjectCombination( jobName );
        }
      }
    }

    return jobsList;
  }

  private synchronized void insertJob( JobMeta jobMeta ) throws KettleException {
    RowMetaAndData table = new RowMetaAndData();

    table.addValue(
      new ValueMetaInteger( KettleDatabaseRepository.FIELD_JOB_ID_JOB ), jobMeta
        .getObjectId() );
    table.addValue( new ValueMetaInteger(
      KettleDatabaseRepository.FIELD_JOB_ID_DIRECTORY ), jobMeta
      .getRepositoryDirectory().getObjectId() );
    table.addValue(
      new ValueMetaString( KettleDatabaseRepository.FIELD_JOB_NAME ), jobMeta
        .getName() );
    table.addValue(
      new ValueMetaString( KettleDatabaseRepository.FIELD_JOB_DESCRIPTION ), jobMeta
        .getDescription() );
    table.addValue( new ValueMetaString(
      KettleDatabaseRepository.FIELD_JOB_EXTENDED_DESCRIPTION ), jobMeta
      .getExtendedDescription() );
    table.addValue(
      new ValueMetaString( KettleDatabaseRepository.FIELD_JOB_JOB_VERSION ), jobMeta
        .getJobversion() );
    table.addValue(
      new ValueMetaInteger( KettleDatabaseRepository.FIELD_JOB_JOB_STATUS ), new Long(
        jobMeta.getJobstatus() < 0 ? -1L : jobMeta.getJobstatus() ) );

    table.addValue( new ValueMetaInteger(
      KettleDatabaseRepository.FIELD_JOB_ID_DATABASE_LOG ), jobMeta
      .getJobLogTable().getDatabaseMeta() != null
      ? jobMeta.getJobLogTable().getDatabaseMeta().getObjectId() : -1L );
    table.addValue( new ValueMetaString(
      KettleDatabaseRepository.FIELD_JOB_TABLE_NAME_LOG ), jobMeta
      .getJobLogTable().getTableName() );
    table.addValue( new ValueMetaBoolean(
      KettleDatabaseRepository.FIELD_JOB_USE_BATCH_ID ), jobMeta
      .getJobLogTable().isBatchIdUsed() );
    table.addValue( new ValueMetaBoolean(
      KettleDatabaseRepository.FIELD_JOB_USE_LOGFIELD ), jobMeta
      .getJobLogTable().isLogFieldUsed() );
    repository.connectionDelegate.insertJobAttribute(
      jobMeta.getObjectId(), 0, KettleDatabaseRepository.JOB_ATTRIBUTE_LOG_SIZE_LIMIT, 0, jobMeta
        .getJobLogTable().getLogSizeLimit() );

    table.addValue(
      new ValueMetaString( KettleDatabaseRepository.FIELD_JOB_CREATED_USER ), jobMeta
        .getCreatedUser() );
    table.addValue(
      new ValueMetaDate( KettleDatabaseRepository.FIELD_JOB_CREATED_DATE ), jobMeta
        .getCreatedDate() );
    table.addValue( new ValueMetaString(
      KettleDatabaseRepository.FIELD_JOB_MODIFIED_USER ), jobMeta
      .getModifiedUser() );
    table.addValue(
      new ValueMetaDate( KettleDatabaseRepository.FIELD_JOB_MODIFIED_DATE ), jobMeta
        .getModifiedDate() );
    table.addValue( new ValueMetaBoolean(
      KettleDatabaseRepository.FIELD_JOB_PASS_BATCH_ID ), jobMeta
      .isBatchIdPassed() );
    table.addValue(
      new ValueMetaString( KettleDatabaseRepository.FIELD_JOB_SHARED_FILE ), jobMeta
        .getSharedObjectsFile() );

    repository.connectionDelegate.getDatabase().prepareInsert(
      table.getRowMeta(), KettleDatabaseRepository.TABLE_R_JOB );
    repository.connectionDelegate.getDatabase().setValuesInsert( table );
    repository.connectionDelegate.getDatabase().insertRow();
    if ( log.isDebug() ) {
      log.logDebug( "Inserted new record into table "
        + quoteTable( KettleDatabaseRepository.TABLE_R_JOB ) + " with data : " + table );
    }
    repository.connectionDelegate.getDatabase().closeInsert();

    // Save the logging connection link...
    if ( jobMeta.getJobLogTable().getDatabaseMeta() != null ) {
      repository.insertJobEntryDatabase( jobMeta.getObjectId(), null, jobMeta
        .getJobLogTable().getDatabaseMeta().getObjectId() );
    }

    // Save the logging tables too..
    //
    RepositoryAttributeInterface attributeInterface =
      new KettleDatabaseRepositoryJobAttribute( repository.connectionDelegate, jobMeta.getObjectId() );
    for ( LogTableInterface logTable : jobMeta.getLogTables() ) {
      logTable.saveToRepository( attributeInterface );
    }
  }

  public synchronized ObjectId insertJobHop( ObjectId id_job, ObjectId id_jobentry_copy_from,
    ObjectId id_jobentry_copy_to, boolean enabled, boolean evaluation, boolean unconditional ) throws KettleException {
    ObjectId id = repository.connectionDelegate.getNextJobHopID();

    RowMetaAndData table = new RowMetaAndData();

    table.addValue( new ValueMetaInteger(
      KettleDatabaseRepository.FIELD_JOB_HOP_ID_JOB_HOP ), id );
    table.addValue(
      new ValueMetaInteger( KettleDatabaseRepository.FIELD_JOB_HOP_ID_JOB ), id_job );
    table.addValue(
      new ValueMetaInteger(
        KettleDatabaseRepository.FIELD_JOB_HOP_ID_JOBENTRY_COPY_FROM ),
      id_jobentry_copy_from );
    table
      .addValue(
        new ValueMetaInteger(
          KettleDatabaseRepository.FIELD_JOB_HOP_ID_JOBENTRY_COPY_TO ),
        id_jobentry_copy_to );
    table.addValue(
      new ValueMetaBoolean( KettleDatabaseRepository.FIELD_JOB_HOP_ENABLED ), Boolean
        .valueOf( enabled ) );
    table.addValue( new ValueMetaBoolean(
      KettleDatabaseRepository.FIELD_JOB_HOP_EVALUATION ), Boolean
      .valueOf( evaluation ) );
    table.addValue( new ValueMetaBoolean(
      KettleDatabaseRepository.FIELD_JOB_HOP_UNCONDITIONAL ), Boolean
      .valueOf( unconditional ) );

    repository.connectionDelegate.getDatabase().prepareInsert(
      table.getRowMeta(), KettleDatabaseRepository.TABLE_R_JOB_HOP );
    repository.connectionDelegate.getDatabase().setValuesInsert( table );
    repository.connectionDelegate.getDatabase().insertRow();
    repository.connectionDelegate.getDatabase().closeInsert();

    return id;
  }

  public String getJobAttributeString( ObjectId id_job, int nr, String code ) throws KettleException {
    return repository.connectionDelegate.getJobAttributeString( id_job, nr, code );
  }

  public long getJobAttributeInteger( ObjectId id_job, int nr, String code ) throws KettleException {
    return repository.connectionDelegate.getJobAttributeInteger( id_job, nr, code );
  }

  public boolean getJobAttributeBoolean( ObjectId id_job, int nr, String code ) throws KettleException {
    return repository.connectionDelegate.getJobAttributeBoolean( id_job, nr, code );
  }

  public synchronized void moveJob( String jobname, ObjectId id_directory_from, ObjectId id_directory_to ) throws KettleException {
    String sql =
      "UPDATE "
        + quoteTable( KettleDatabaseRepository.TABLE_R_JOB ) + " SET "
        + quote( KettleDatabaseRepository.FIELD_JOB_ID_DIRECTORY ) + " = ? WHERE "
        + quote( KettleDatabaseRepository.FIELD_JOB_NAME ) + " = ? AND "
        + quote( KettleDatabaseRepository.FIELD_JOB_ID_DIRECTORY ) + " = ?";

    RowMetaAndData par = new RowMetaAndData();
    par.addValue(
      new ValueMetaInteger( KettleDatabaseRepository.FIELD_JOB_ID_DIRECTORY ),
      id_directory_to );
    par.addValue(
      new ValueMetaString( KettleDatabaseRepository.FIELD_JOB_NAME ), jobname );
    par.addValue(
      new ValueMetaInteger( KettleDatabaseRepository.FIELD_JOB_ID_DIRECTORY ),
      id_directory_from );

    repository.connectionDelegate.getDatabase().execStatement( sql, par.getRowMeta(), par.getData() );
  }

  public synchronized void renameJob( ObjectId id_job, RepositoryDirectoryInterface newParentDir, String newname ) throws KettleException {
    if ( newParentDir != null || newname != null ) {
      RowMetaAndData table = new RowMetaAndData();

      String sql = "UPDATE " + quoteTable( KettleDatabaseRepository.TABLE_R_JOB ) + " SET ";

      boolean additionalParameter = false;

      if ( newname != null ) {
        additionalParameter = true;
        sql += quote( KettleDatabaseRepository.FIELD_JOB_NAME ) + " = ? ";
        table.addValue(
          new ValueMetaString( KettleDatabaseRepository.FIELD_JOB_NAME ), newname );
      }
      if ( newParentDir != null ) {
        if ( additionalParameter ) {
          sql += ", ";
        }
        sql += quote( KettleDatabaseRepository.FIELD_JOB_ID_DIRECTORY ) + " = ? ";
        table.addValue( new ValueMetaInteger(
          KettleDatabaseRepository.FIELD_JOB_ID_DIRECTORY ), newParentDir
          .getObjectId() );
      }

      sql += "WHERE " + quote( KettleDatabaseRepository.FIELD_JOB_ID_JOB ) + " = ?";
      table.addValue(
        new ValueMetaInteger( KettleDatabaseRepository.FIELD_JOB_ID_JOB ), id_job );

      log.logBasic( "sql = [" + sql + "]" );
      log.logBasic( "row = [" + table + "]" );

      repository.connectionDelegate.getDatabase().execStatement( sql, table.getRowMeta(), table.getData() );
    }
  }

  private void saveJobAttributesMap( ObjectId jobId, Map<String, Map<String, String>> attributesMap ) throws KettleException {
    for ( final String groupName : attributesMap.keySet() ) {
      Map<String, String> attributes = attributesMap.get( groupName );
      for ( final String key : attributes.keySet() ) {
        final String value = attributes.get( key );
        if ( key != null && value != null ) {
          repository.connectionDelegate.insertJobAttribute( jobId, 0, JOB_ATTRIBUTE_PREFIX
            + groupName + '\t' + key, 0, value );
        }
      }
    }
  }

  private Map<String, Map<String, String>> loadJobAttributesMap( ObjectId jobId ) throws KettleException {
    Map<String, Map<String, String>> attributesMap = new HashMap<String, Map<String, String>>();

    List<Object[]> attributeRows =
      repository.connectionDelegate.getJobAttributesWithPrefix( jobId, JOB_ATTRIBUTE_PREFIX );
    RowMetaInterface rowMeta = repository.connectionDelegate.getReturnRowMeta();
    for ( Object[] attributeRow : attributeRows ) {
      String code = rowMeta.getString( attributeRow, KettleDatabaseRepository.FIELD_JOB_ATTRIBUTE_CODE, null );
      String value =
        rowMeta.getString( attributeRow, KettleDatabaseRepository.FIELD_JOB_ATTRIBUTE_VALUE_STR, null );
      if ( code != null && value != null ) {
        code = code.substring( JOB_ATTRIBUTE_PREFIX.length() );
        int tabIndex = code.indexOf( '\t' );
        if ( tabIndex > 0 ) {
          String groupName = code.substring( 0, tabIndex );
          String key = code.substring( tabIndex + 1 );
          Map<String, String> attributes = attributesMap.get( groupName );
          if ( attributes == null ) {
            attributes = new HashMap<String, String>();
            attributesMap.put( groupName, attributes );
          }
          attributes.put( key, value );
        }
      }
    }

    return attributesMap;
  }
}
