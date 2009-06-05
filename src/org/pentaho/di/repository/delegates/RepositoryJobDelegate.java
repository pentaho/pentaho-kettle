package org.pentaho.di.repository.delegates;

import java.util.Date;
import java.util.List;

import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.NotePadMeta;
import org.pentaho.di.core.ProgressMonitorListener;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.JobHopMeta;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entry.JobEntryCopy;
import org.pentaho.di.repository.KettleDatabaseRepository;
import org.pentaho.di.repository.RepositoryElementInterface;
import org.pentaho.di.repository.directory.RepositoryDirectory;
import org.pentaho.di.shared.SharedObjects;

public class RepositoryJobDelegate extends BaseRepositoryDelegate {

	private static Class<?> PKG = JobMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	public RepositoryJobDelegate(KettleDatabaseRepository repository) {
		super(repository);
	}
	
	public RowMetaAndData getJob(long id_job) throws KettleException
	{
		return repository.connectionDelegate.getOneRow(quoteTable(KettleDatabaseRepository.TABLE_R_JOB), quote(KettleDatabaseRepository.FIELD_JOB_ID_JOB), id_job);
	}

	public RowMetaAndData getJobHop(long id_job_hop) throws KettleException
	{
		return repository.connectionDelegate.getOneRow(quoteTable(KettleDatabaseRepository.TABLE_R_JOB_HOP), quote(KettleDatabaseRepository.FIELD_JOB_HOP_ID_JOB_HOP), id_job_hop);
	}

	public synchronized long getJobHopID(long id_job, long id_jobentry_copy_from, long id_jobentry_copy_to) throws KettleException {
		String lookupkey[] = new String[] { quote(KettleDatabaseRepository.FIELD_JOB_HOP_ID_JOB), quote(KettleDatabaseRepository.FIELD_JOB_HOP_ID_JOBENTRY_COPY_FROM), quote(KettleDatabaseRepository.FIELD_JOB_HOP_ID_JOBENTRY_COPY_TO), };
		long key[] = new long[] { id_job, id_jobentry_copy_from, id_jobentry_copy_to };

		return repository.connectionDelegate.getIDWithValue(quoteTable(KettleDatabaseRepository.TABLE_R_JOB_HOP), quote(KettleDatabaseRepository.FIELD_JOB_HOP_ID_JOB_HOP), lookupkey, key);
	}


    /**
     * Stored a job in the repository
     * @param jobMeta The job to store
     * @param monitor the (optional) UI progress monitor
     * @throws KettleException in case some IO error occurs.
     */
    public void saveJob(JobMeta jobMeta, ProgressMonitorListener monitor) throws KettleException {
		try {
			int nrWorks = 2 + jobMeta.nrDatabases() + jobMeta.nrNotes() + jobMeta.nrJobEntries() + jobMeta.nrJobHops();
			if (monitor != null)
				monitor.beginTask(BaseMessages.getString(PKG, "JobMeta.Monitor.SavingTransformation") + jobMeta.getRepositoryDirectory() + Const.FILE_SEPARATOR + jobMeta.getName(), nrWorks); //$NON-NLS-1$

			repository.lockRepository();

			repository.insertLogEntry("save job '" + jobMeta.getName() + "'"); //$NON-NLS-1$ //$NON-NLS-2$

			// Before we start, make sure we have a valid job ID!
			// Two possibilities:
			// 1) We have a ID: keep it
			// 2) We don't have an ID: look it up.
			// If we find a transformation with the same name: ask!
			//
			if (monitor != null)
				monitor.subTask(BaseMessages.getString(PKG, "JobMeta.Monitor.HandlingPreviousVersionOfJob")); //$NON-NLS-1$
			jobMeta.setID(getJobID(jobMeta.getName(), jobMeta.getRepositoryDirectory().getID()));

			// If no valid id is available in the database, assign one...
			if (jobMeta.getID() <= 0) {
				jobMeta.setID(repository.connectionDelegate.getNextJobID());
			} else {
				// If we have a valid ID, we need to make sure everything is
				// cleared out
				// of the database for this id_job, before we put it back in...
				repository.delAllFromJob(jobMeta.getID());
			}
			if (monitor != null)
				monitor.worked(1);

			// First of all we need to verify that all database connections are
			// saved.
			//
			if(log.isDebug()) log.logDebug(toString(), BaseMessages.getString(PKG, "JobMeta.Log.SavingDatabaseConnections")); //$NON-NLS-1$
			for (int i = 0; i < jobMeta.nrDatabases(); i++) {
				if (monitor != null)
					monitor.subTask(BaseMessages.getString(PKG, "JobMeta.Monitor.SavingDatabaseTask.Title") + (i + 1) + "/" + jobMeta.nrDatabases()); //$NON-NLS-1$ //$NON-NLS-2$
				DatabaseMeta databaseMeta = jobMeta.getDatabase(i);
				// ONLY save the database connection if it has changed and
				// nothing was saved in the repository
				if (databaseMeta.hasChanged() || databaseMeta.getID() <= 0) {
					repository.save(databaseMeta);
				}
				if (monitor != null)
					monitor.worked(1);
			}

			// Now, save the job entry in R_JOB
			// Note, we save this first so that we have an ID in the database.
			// Everything else depends on this ID, including recursive job
			// entries to the save job. (retry)
			if (monitor != null)
				monitor.subTask(BaseMessages.getString(PKG, "JobMeta.Monitor.SavingJobDetails")); //$NON-NLS-1$
			if(log.isDetailed()) log.logDetailed(toString(), "Saving job info to repository..."); //$NON-NLS-1$
			
			insertJob(jobMeta);
			
			if (monitor != null)
				monitor.worked(1);

			// Save the slaves
			//
			for (int i = 0; i < jobMeta.getSlaveServers().size(); i++) {
				SlaveServer slaveServer = jobMeta.getSlaveServers().get(i);
				repository.saveSlaveServer(slaveServer, jobMeta.getID(), false);
			}

			//
			// Save the notes
			//
			if(log.isDetailed()) log.logDetailed(toString(), "Saving notes to repository..."); //$NON-NLS-1$
			for (int i = 0; i < jobMeta.nrNotes(); i++) {
				if (monitor != null)
					monitor.subTask(BaseMessages.getString(PKG, "JobMeta.Monitor.SavingNoteNr") + (i + 1) + "/" + jobMeta.nrNotes()); //$NON-NLS-1$ //$NON-NLS-2$
				NotePadMeta ni = jobMeta.getNote(i);
				repository.saveNotePadMeta(ni, jobMeta.getID());
				if (ni.getID() > 0) {
					repository.insertJobNote(jobMeta.getID(), ni.getID());
				}
				if (monitor != null)
					monitor.worked(1);
			}

			//
			// Save the job entries
			//
			if(log.isDetailed()) log.logDetailed(toString(), "Saving " + jobMeta.nrJobEntries() + " Job enty copies to repository..."); //$NON-NLS-1$ //$NON-NLS-2$
			repository.updateJobEntryTypes();
			for (int i = 0; i < jobMeta.nrJobEntries(); i++) {
				if (monitor != null)
					monitor.subTask(BaseMessages.getString(PKG, "JobMeta.Monitor.SavingJobEntryNr") + (i + 1) + "/" + jobMeta.nrJobEntries()); //$NON-NLS-1$ //$NON-NLS-2$
				JobEntryCopy cge = jobMeta.getJobEntry(i);
				repository.jobEntryDelegate.saveJobEntryCopy(cge, jobMeta.getID());
				if (monitor != null)
					monitor.worked(1);
			}

			if(log.isDetailed()) log.logDetailed(toString(), "Saving job hops to repository..."); //$NON-NLS-1$
			for (int i = 0; i < jobMeta.nrJobHops(); i++) {
				if (monitor != null)
					monitor.subTask("Saving job hop #" + (i + 1) + "/" + jobMeta.nrJobHops()); //$NON-NLS-1$ //$NON-NLS-2$
				JobHopMeta hi = jobMeta.getJobHop(i);
				saveJobHopMeta(hi, jobMeta.getID());
				if (monitor != null)
					monitor.worked(1);
			}

			saveJobParameters(jobMeta);
			
			// Commit this transaction!!
			repository.commit();

			jobMeta.clearChanged();
			if (monitor != null)
				monitor.done();
		} catch (KettleDatabaseException dbe) {
			repository.rollback();
			throw new KettleException(BaseMessages.getString(PKG, "JobMeta.Exception.UnableToSaveJobInRepositoryRollbackPerformed"), dbe); //$NON-NLS-1$
		} finally {
			// don't forget to unlock the repository.
			// Normally this is done by the commit / rollback statement, but hey
			// there are some freaky database out
			// there...
			repository.unlockRepository();
		}

	}

    
    /**
     * Save the parameters of this job to the repository.
     * 
     * @param rep The repository to save to.
     * 
     * @throws KettleException Upon any error.
     */
    private void saveJobParameters(JobMeta jobMeta) throws KettleException
    {
    	String[] paramKeys = jobMeta.listParameters();
    	for (int idx = 0; idx < paramKeys.length; idx++)  {
    		String desc = jobMeta.getParameterDescription(paramKeys[idx]);
    		String defValue = jobMeta.getParameterDefault(paramKeys[idx]);
    		insertJobParameter(jobMeta.getID(), idx, paramKeys[idx], defValue, desc);
    	}
    }

	public boolean existsJobMeta(RepositoryElementInterface repositoryElement) {
		if (repositoryElement.getID() < 0) {
			try {
				if (getJobID(repositoryElement.getName(), repositoryElement.getRepositoryDirectory().getID()) > 0)
					return true;
			} catch (KettleException dbe) {
				return true;
			}
		}
		return false;
	}
    
	/**
	 * Load a job from the repository
	 * 
	 * @param jobname The name of the job
	 * @param repdir The directory in which the job resides.
	 * @throws KettleException
	 */
	public JobMeta loadJobMeta(String jobname, RepositoryDirectory repdir) throws KettleException {
		return loadJobMeta(jobname, repdir, null);
	}

	
	/** Load a job in a directory
	 * 
	 * @param log the logging channel
	 * @param rep The Repository
	 * @param jobname The name of the job
	 * @param repdir The directory in which the job resides.
	 * @throws KettleException
	 */
	public JobMeta loadJobMeta(String jobname, RepositoryDirectory repdir, ProgressMonitorListener monitor) throws KettleException {
		
		JobMeta jobMeta = new JobMeta();
		
		synchronized(repository)
		{
			try {
				// Clear everything...
				jobMeta.clear();
	
				jobMeta.setRepositoryDirectory(repdir );
	
				// Get the transformation id
				jobMeta.setID(getJobID(jobname, repdir.getID()));
	
				// If no valid id is available in the database, then give error...
				if (jobMeta.getID() > 0) {
					// Load the notes...
					long noteids[] = repository.getJobNoteIDs(jobMeta.getID());
					long jecids[] = repository.getJobEntryCopyIDs(jobMeta.getID());
					long hopid[] = repository.getJobHopIDs(jobMeta.getID());
	
					int nrWork = 2 + noteids.length + jecids.length + hopid.length;
					if (monitor != null)
						monitor.beginTask(BaseMessages.getString(PKG, "JobMeta.Monitor.LoadingJob") + repdir + Const.FILE_SEPARATOR + jobname, nrWork); //$NON-NLS-1$
	
					//
					// get job info:
					//
					if (monitor != null)
						monitor.subTask(BaseMessages.getString(PKG, "JobMeta.Monitor.ReadingJobInformation")); //$NON-NLS-1$
					RowMetaAndData jobRow = getJob(jobMeta.getID());
	
					jobMeta.setName( jobRow.getString(KettleDatabaseRepository.FIELD_JOB_NAME, null) ); //$NON-NLS-1$
					jobMeta.setDescription( jobRow.getString(KettleDatabaseRepository.FIELD_JOB_DESCRIPTION, null) ); //$NON-NLS-1$
					jobMeta.setExtendedDescription(jobRow.getString(KettleDatabaseRepository.FIELD_JOB_EXTENDED_DESCRIPTION, null) ); //$NON-NLS-1$
					jobMeta.setJobversion( jobRow.getString(KettleDatabaseRepository.FIELD_JOB_JOB_VERSION, null) ); //$NON-NLS-1$
					jobMeta.setJobstatus( Const.toInt(jobRow.getString(KettleDatabaseRepository.FIELD_JOB_JOB_STATUS, null), -1) ); //$NON-NLS-1$
					jobMeta.setLogTable( jobRow.getString(KettleDatabaseRepository.FIELD_JOB_TABLE_NAME_LOG, null) ); //$NON-NLS-1$
	
					jobMeta.setCreatedUser( jobRow.getString(KettleDatabaseRepository.FIELD_JOB_CREATED_USER, null) ); //$NON-NLS-1$
					jobMeta.setCreatedDate( jobRow.getDate(KettleDatabaseRepository.FIELD_JOB_CREATED_DATE, new Date()) ); //$NON-NLS-1$
	
					jobMeta.setModifiedUser( jobRow.getString(KettleDatabaseRepository.FIELD_JOB_MODIFIED_USER, null) ); //$NON-NLS-1$
					jobMeta.setModifiedDate( jobRow.getDate(KettleDatabaseRepository.FIELD_JOB_MODIFIED_DATE, new Date()) ); //$NON-NLS-1$
	
					long id_logdb = jobRow.getInteger(KettleDatabaseRepository.FIELD_JOB_ID_DATABASE_LOG, 0); //$NON-NLS-1$
					if (id_logdb > 0) {
						// Get the logconnection
						//
						jobMeta.setLogConnection( repository.loadDatabaseMeta(id_logdb) );
						jobMeta.getLogConnection().shareVariablesWith(jobMeta);
					}
					jobMeta.setUseBatchId( jobRow.getBoolean(KettleDatabaseRepository.FIELD_JOB_USE_BATCH_ID, false) ); //$NON-NLS-1$
					jobMeta.setBatchIdPassed(  jobRow.getBoolean(KettleDatabaseRepository.FIELD_JOB_PASS_BATCH_ID, false) ); //$NON-NLS-1$
					jobMeta.setLogfieldUsed( jobRow.getBoolean(KettleDatabaseRepository.FIELD_JOB_USE_LOGFIELD, false) ); //$NON-NLS-1$
					
					// The log size limit is an attribute
					//
					jobMeta.setLogSizeLimit( getJobAttributeString(jobMeta.getID(), 0, KettleDatabaseRepository.JOB_ATTRIBUTE_LOG_SIZE_LIMIT) );
	
					if (monitor != null)
						monitor.worked(1);
					// 
					// Load the common database connections
					//
					if (monitor != null)
						monitor.subTask(BaseMessages.getString(PKG, "JobMeta.Monitor.ReadingAvailableDatabasesFromRepository")); //$NON-NLS-1$
					// Read objects from the shared XML file & the repository
					try {
						jobMeta.setSharedObjectsFile( jobRow.getString(KettleDatabaseRepository.FIELD_JOB_SHARED_FILE, null) );
						jobMeta.setSharedObjects( repository!=null ? repository.readJobMetaSharedObjects(jobMeta) : jobMeta.readSharedObjects() );
					} catch (Exception e) {
						LogWriter.getInstance().logError(toString(),
								BaseMessages.getString(PKG, "JobMeta.ErrorReadingSharedObjects.Message", e.toString())); // $NON-NLS-1$
																												// //$NON-NLS-1$
						LogWriter.getInstance().logError(toString(), Const.getStackTracker(e));
					}
					if (monitor != null)
						monitor.worked(1);
	
					if(log.isDetailed()) log.logDetailed(toString(), "Loading " + noteids.length + " notes"); //$NON-NLS-1$ //$NON-NLS-2$
					for (int i = 0; i < noteids.length; i++) {
						if (monitor != null)
							monitor.subTask(BaseMessages.getString(PKG, "JobMeta.Monitor.ReadingNoteNr") + (i + 1) + "/" + noteids.length); //$NON-NLS-1$ //$NON-NLS-2$
						NotePadMeta ni = repository.loadNotePadMeta(noteids[i]);
						if (jobMeta.indexOfNote(ni) < 0)
							jobMeta.addNote(ni);
						if (monitor != null)
							monitor.worked(1);
					}
	
					// Load the job entries...
					if(log.isDetailed()) log.logDetailed(toString(), "Loading " + jecids.length + " job entries"); //$NON-NLS-1$ //$NON-NLS-2$
					for (int i = 0; i < jecids.length; i++) {
						if (monitor != null)
							monitor.subTask(BaseMessages.getString(PKG, "JobMeta.Monitor.ReadingJobEntryNr") + (i + 1) + "/" + (jecids.length)); //$NON-NLS-1$ //$NON-NLS-2$
	
						JobEntryCopy jec = repository.jobEntryDelegate.loadJobEntryCopy(jobMeta.getID(), jecids[i], jobMeta.getJobentries(), jobMeta.getDatabases(), jobMeta.getSlaveServers());

						// Also set the copy number...
						// We count the number of job entry copies that use the job
						// entry
						//
						int copyNr = 0;
						for (JobEntryCopy copy : jobMeta.getJobCopies()) {
							if (jec.getEntry() == copy.getEntry()) {
								copyNr++;
							}
						}
						jec.setNr(copyNr);
	
						int idx = jobMeta.indexOfJobEntry(jec);
						if (idx < 0) {
							if (jec.getName() != null && jec.getName().length() > 0)
								jobMeta.addJobEntry(jec);
						} else {
							jobMeta.setJobEntry(idx, jec); // replace it!
						}
						if (monitor != null)
							monitor.worked(1);
					}
	
					// Load the hops...
					if(log.isDetailed()) log.logDetailed(toString(), "Loading " + hopid.length + " job hops"); //$NON-NLS-1$ //$NON-NLS-2$
					for (int i = 0; i < hopid.length; i++) {
						if (monitor != null)
							monitor.subTask(BaseMessages.getString(PKG, "JobMeta.Monitor.ReadingJobHopNr") + (i + 1) + "/" + (jecids.length)); //$NON-NLS-1$ //$NON-NLS-2$
						JobHopMeta hi = loadJobHopMeta(hopid[i], jobMeta.getJobCopies());
						jobMeta.getJobhops().add(hi);
						if (monitor != null)
							monitor.worked(1);
					}

					loadRepParameters(jobMeta);
					
					// Finally, clear the changed flags...
					jobMeta.clearChanged();
					if (monitor != null)
						monitor.subTask(BaseMessages.getString(PKG, "JobMeta.Monitor.FinishedLoadOfJob")); //$NON-NLS-1$
					if (monitor != null)
						monitor.done();
					
					return jobMeta;
				} else {
					throw new KettleException(BaseMessages.getString(PKG, "JobMeta.Exception.CanNotFindJob") + jobname); //$NON-NLS-1$
				}
			} catch (KettleException dbe) {
				throw new KettleException(BaseMessages.getString(PKG, "JobMeta.Exception.AnErrorOccuredReadingJob", jobname), dbe);
			} finally {
				jobMeta.initializeVariablesFrom(jobMeta.getParentVariableSpace());
				jobMeta.setInternalKettleVariables();
			}
		}
	}
	
    /**
     * Load the parameters of this job from the repository. The current 
     * ones already loaded will be erased.
     * 
     * @param jobMeta The target job for the parameters
     * 
     * @throws KettleException Upon any error.
     * 
     */
    private void loadRepParameters(JobMeta jobMeta) throws KettleException
    {
    	jobMeta.eraseParameters();

    	int count = countJobParameter(jobMeta.getID());
    	for (int idx = 0; idx < count; idx++)  {
    		String key  = getJobParameterKey(jobMeta.getID(), idx);
    		String defValue = getJobParameterDefault(jobMeta.getID(), idx);
    		String desc = getJobParameterDescription(jobMeta.getID(), idx);
    		jobMeta.addParameterDefinition(key, defValue, desc);
    	}
    }        

	/**
	 * Get a job parameter key. You can count the number of parameters up front.
	 * 
	 * @param id_job job id
	 * @param nr number of the parameter 
	 * @return they key/name of specified parameter
	 * 
	 * @throws KettleException Upon any error.
	 */
	public String getJobParameterKey(long id_job, int nr) throws KettleException  {
 		return repository.connectionDelegate.getJobAttributeString(id_job, nr, KettleDatabaseRepository.JOB_ATTRIBUTE_PARAM_KEY);		
	}

	/**
	 * Get a job parameter default. You can count the number of parameters up front. 
	 * 
	 * @param id_job job id
	 * @param nr number of the parameter
	 * @return
	 * 
	 * @throws KettleException Upon any error.
	 */
	public String getJobParameterDefault(long id_job, int nr) throws KettleException  {
 		return repository.connectionDelegate.getTransAttributeString(id_job, nr, KettleDatabaseRepository.JOB_ATTRIBUTE_PARAM_DEFAULT);		
	}	
	
	/**
	 * Get a job parameter description. You can count the number of parameters up front. 
	 * 
	 * @param id_job job id
	 * @param nr number of the parameter
	 * @return
	 * 
	 * @throws KettleException Upon any error.
	 */
	public String getJobParameterDescription(long id_job, int nr) throws KettleException  {
 		return repository.connectionDelegate.getTransAttributeString(id_job, nr, KettleDatabaseRepository.JOB_ATTRIBUTE_PARAM_DESCRIPTION);		
	}
	
	/**
	 * Insert a parameter for a job in the repository.
	 * 
	 * @param id_job job id
	 * @param nr number of the parameter to insert
	 * @param key key to insert
	 * @param defValue default value for key
	 * @param description description to insert
	 * 
	 * @throws KettleException Upon any error.
	 */
	public void insertJobParameter(long id_job, long nr, String key, String defValue, String description) throws KettleException {
		repository.connectionDelegate.insertJobAttribute(id_job, nr, KettleDatabaseRepository.JOB_ATTRIBUTE_PARAM_KEY, 0, key != null ? key : "");
		repository.connectionDelegate.insertJobAttribute(id_job, nr, KettleDatabaseRepository.JOB_ATTRIBUTE_PARAM_DEFAULT, 0, defValue != null ? defValue : "");
		repository.connectionDelegate.insertJobAttribute(id_job, nr, KettleDatabaseRepository.JOB_ATTRIBUTE_PARAM_DESCRIPTION, 0, description != null ? description : "");
	}	
	

	/**
	 * Count the number of parameters of a job.
	 * 
	 * @param id_job job id
	 * @return the number of transactions
	 * 
	 * @throws KettleException Upon any error.
	 */
	public int countJobParameter(long id_job) throws KettleException  {
		return repository.connectionDelegate.countNrTransAttributes(id_job, KettleDatabaseRepository.JOB_ATTRIBUTE_PARAM_KEY);
	}
	
    
	public JobHopMeta loadJobHopMeta(long id_job_hop, List<JobEntryCopy> jobcopies) throws KettleException
	{
		JobHopMeta jobHopMeta = new JobHopMeta();
		try
		{
			RowMetaAndData r = getJobHop(id_job_hop);
			if (r!=null)
			{
				long id_jobentry_copy_from  =  r.getInteger("ID_JOBENTRY_COPY_FROM", -1L);
				long id_jobentry_copy_to    =  r.getInteger("ID_JOBENTRY_COPY_TO", -1L);
				
				jobHopMeta.setEnabled( r.getBoolean("ENABLED", true) );
				jobHopMeta.setEvaluation( r.getBoolean("EVALUATION", true) );
				jobHopMeta.setConditional();
				if (r.getBoolean("UNCONDITIONAL", !jobHopMeta.getEvaluation())) {
					jobHopMeta.setUnconditional();
				}
				
				jobHopMeta.setFromEntry( JobMeta.findJobEntryCopy(jobcopies, id_jobentry_copy_from) );
				jobHopMeta.setToEntry( JobMeta.findJobEntryCopy(jobcopies, id_jobentry_copy_to) );
				
				return jobHopMeta;
			} else {
				throw new KettleException("Unable to find job hop with ID : "+id_job_hop);
			}
		}
		catch(KettleDatabaseException dbe)
		{
			throw new KettleException(BaseMessages.getString(PKG, "JobHopMeta.Exception.UnableToLoadHopInfoRep",""+id_job_hop) , dbe);
			
		}
	}

	public void saveJobHopMeta(JobHopMeta hop, long id_job) throws KettleException
	{
		try
		{
			long id_jobentry_from=-1, id_jobentry_to=-1;
			
			id_jobentry_from = hop.getFromEntry()==null ? -1 : hop.getFromEntry().getID();
			id_jobentry_to = hop.getToEntry()==null ? -1 : hop.getToEntry().getID();
			
			// Insert new job hop in repository
			//
			hop.setID( insertJobHop(id_job, id_jobentry_from, id_jobentry_to, hop.isEnabled(), hop.getEvaluation(), hop.isUnconditional()) );
		}
		catch(KettleDatabaseException dbe)
		{
			throw new KettleException(BaseMessages.getString(PKG, "JobHopMeta.Exception.UnableToSaveHopInfoRep",""+id_job), dbe);
			
		}
	}

	
	
	
	
	/**
	 * Read the database connections in the repository and add them to this job
	 * if they are not yet present.
	 * 
	 * @param jobMeta the job to put the database connections in
	 * @throws KettleException
	 */
	public void readDatabases(JobMeta jobMeta) throws KettleException {
		readDatabases(jobMeta, true);
	}

	/**
	 * Read the database connections in the repository and add them to this job
	 * if they are not yet present.
	 * 
	 * @param jobMeta the job to put the database connections in
	 * @param overWriteShared set to true if you want to overwrite shared connections while loading.
	 * @throws KettleException
	 */
	public void readDatabases(JobMeta jobMeta, boolean overWriteShared) throws KettleException {
		try {
			long dbids[] = repository.getDatabaseIDs();
			for (int i = 0; i < dbids.length; i++) {
				DatabaseMeta databaseMeta = repository.loadDatabaseMeta(dbids[i]);
				databaseMeta.shareVariablesWith(jobMeta);

				// See if there already is one in the transformation
				//
				DatabaseMeta check = jobMeta.findDatabase(databaseMeta.getName());
				
				// We only add, never overwrite database connections.
				//
				if (check == null || overWriteShared) 
				{
					if (databaseMeta.getName() != null) {
						jobMeta.addOrReplaceDatabase(databaseMeta);
						if (!overWriteShared)
							databaseMeta.setChanged(false);
					}
				}
			}
			jobMeta.setChanged(false);
		} catch (KettleDatabaseException dbe) {
			throw new KettleException(BaseMessages.getString(PKG, "JobMeta.Log.UnableToReadDatabaseIDSFromRepository"), dbe); //$NON-NLS-1$
		} catch (KettleException ke) {
			throw new KettleException(BaseMessages.getString(PKG, "JobMeta.Log.UnableToReadDatabasesFromRepository"), ke); //$NON-NLS-1$
		}
	}
	
    /**
     * Read the slave servers in the repository and add them to this transformation if they are not yet present.
     * @param jobMeta The job to put the slave servers in
     * @param overWriteShared if an object with the same name exists, overwrite
     * @throws KettleException 
     */
    public void readSlaves(JobMeta jobMeta, boolean overWriteShared) throws KettleException
    {
        try
        {
            long dbids[] = repository.getSlaveIDs();
            for (int i = 0; i < dbids.length; i++)
            {
                SlaveServer slaveServer = repository.loadSlaveServer(dbids[i]);
                slaveServer.shareVariablesWith(jobMeta);

                SlaveServer check = jobMeta.findSlaveServer(slaveServer.getName()); // Check if there already is one in the transformation
                if (check==null || overWriteShared) 
                {
                    if (!Const.isEmpty(slaveServer.getName()))
                    {
                    	jobMeta.addOrReplaceSlaveServer(slaveServer);
                        if (!overWriteShared) slaveServer.setChanged(false);
                    }
                }
            }
        }
        catch (KettleDatabaseException dbe)
        {
            throw new KettleException(BaseMessages.getString(PKG, "JobMeta.Log.UnableToReadSlaveServersFromRepository"), dbe); //$NON-NLS-1$
        }
    }

	public SharedObjects readSharedObjects(JobMeta jobMeta) throws KettleException {
		jobMeta.readSharedObjects();

		readDatabases(jobMeta, true);
        readSlaves(jobMeta, true);
		
		return jobMeta.getSharedObjects();
	}

	public synchronized long getJobID(String name, long id_directory) throws KettleException
	{
		return repository.connectionDelegate.getIDWithValue(quoteTable(KettleDatabaseRepository.TABLE_R_JOB), quote(KettleDatabaseRepository.FIELD_JOB_ID_JOB), quote(KettleDatabaseRepository.FIELD_JOB_NAME), name, quote(KettleDatabaseRepository.FIELD_JOB_ID_DIRECTORY), id_directory);
	}

	public synchronized int getNrJobs() throws KettleException
	{
		int retval = 0;

		String sql = "SELECT COUNT(*) FROM "+quoteTable(KettleDatabaseRepository.TABLE_R_JOB);
		RowMetaAndData r = repository.connectionDelegate.getOneRow(sql);
		if (r != null)
		{
			retval = (int) r.getInteger(0, 0L);
		}

		return retval;
	}


	public synchronized int getNrJobs(long id_directory) throws KettleException
	{
		int retval = 0;

		String sql = "SELECT COUNT(*) FROM "+quoteTable(KettleDatabaseRepository.TABLE_R_JOB)+" WHERE "+quote(KettleDatabaseRepository.FIELD_JOB_ID_DIRECTORY)+" = " + id_directory;
		RowMetaAndData r = repository.connectionDelegate.getOneRow(sql);
		if (r != null)
		{
			retval = (int) r.getInteger(0, 0L);
		}

		return retval;
	}


	public synchronized int getNrJobHops(long id_job) throws KettleException
	{
		int retval = 0;

		String sql = "SELECT COUNT(*) FROM "+quoteTable(KettleDatabaseRepository.TABLE_R_JOB_HOP)+" WHERE "+quote(KettleDatabaseRepository.FIELD_JOB_HOP_ID_JOB)+" = " + id_job;
		RowMetaAndData r = repository.connectionDelegate.getOneRow(sql);
		if (r != null)
		{
			retval = (int) r.getInteger(0, 0L);
		}

		return retval;
	}

	public String[] getJobsWithIDList(List<Object[]> list, RowMetaInterface rowMeta) throws KettleException
    {
        String[] jobList = new String[list.size()];
        for (int i=0;i<list.size();i++)
        {
            long id_job = rowMeta.getInteger( list.get(i), quote(KettleDatabaseRepository.FIELD_JOB_ID_JOB), -1L); 
            if (id_job > 0)
            {
            	 RowMetaAndData jobRow =  getJob(id_job);
                 if (jobRow!=null)
                 {
                     String jobName = jobRow.getString(quote(KettleDatabaseRepository.FIELD_JOB_NAME), "<name not found>");
                     long id_directory = jobRow.getInteger(quote(KettleDatabaseRepository.FIELD_JOB_ID_DIRECTORY), -1L);
                     RepositoryDirectory dir = repository.getDirectoryTree().findDirectory(id_directory);
                     
                     jobList[i]=dir.getPathObjectCombination(jobName);
                 }
            }            
        }

        return jobList;
    }

	private synchronized void insertJob(JobMeta jobMeta) throws KettleException
	{
		RowMetaAndData table = new RowMetaAndData();
		
		table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_JOB_ID_JOB, ValueMetaInterface.TYPE_INTEGER), jobMeta.getID());
		table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_JOB_ID_DIRECTORY, ValueMetaInterface.TYPE_INTEGER), jobMeta.getRepositoryDirectory().getID());
		table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_JOB_NAME, ValueMetaInterface.TYPE_STRING), jobMeta.getName());
		table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_JOB_DESCRIPTION, ValueMetaInterface.TYPE_STRING), jobMeta.getDescription());
		table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_JOB_EXTENDED_DESCRIPTION, ValueMetaInterface.TYPE_STRING), jobMeta.getExtendedDescription());
		table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_JOB_JOB_VERSION, ValueMetaInterface.TYPE_STRING), jobMeta.getJobversion());
		table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_JOB_JOB_STATUS, ValueMetaInterface.TYPE_INTEGER), new Long(jobMeta.getJobstatus()  <0 ? -1L : jobMeta.getJobstatus()));

		table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_JOB_ID_DATABASE_LOG, ValueMetaInterface.TYPE_INTEGER), jobMeta.getLogConnection()!=null ? jobMeta.getLogConnection().getID() : -1L);
		table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_JOB_TABLE_NAME_LOG, ValueMetaInterface.TYPE_STRING), jobMeta.getLogTable());

		table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_JOB_CREATED_USER, ValueMetaInterface.TYPE_STRING), jobMeta.getCreatedUser());
		table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_JOB_CREATED_DATE, ValueMetaInterface.TYPE_DATE), jobMeta.getCreatedDate());
		table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_JOB_MODIFIED_USER, ValueMetaInterface.TYPE_STRING), jobMeta.getModifiedUser());
		table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_JOB_MODIFIED_DATE, ValueMetaInterface.TYPE_DATE), jobMeta.getModifiedDate());
        table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_JOB_USE_BATCH_ID, ValueMetaInterface.TYPE_BOOLEAN), jobMeta.isBatchIdUsed());
        table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_JOB_PASS_BATCH_ID, ValueMetaInterface.TYPE_BOOLEAN), jobMeta.isBatchIdPassed());
        table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_JOB_USE_LOGFIELD, ValueMetaInterface.TYPE_BOOLEAN), jobMeta.isLogfieldUsed());
        table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_JOB_SHARED_FILE, ValueMetaInterface.TYPE_STRING), jobMeta.getSharedObjectsFile());
        
        repository.connectionDelegate.getDatabase().prepareInsert(table.getRowMeta(), KettleDatabaseRepository.TABLE_R_JOB);
		repository.connectionDelegate.getDatabase().setValuesInsert(table);
		repository.connectionDelegate.getDatabase().insertRow();
        if (log.isDebug()) log.logDebug(toString(), "Inserted new record into table "+quoteTable(KettleDatabaseRepository.TABLE_R_JOB)+" with data : " + table);
        repository.connectionDelegate.getDatabase().closeInsert();
		
        repository.connectionDelegate.insertJobAttribute(jobMeta.getID(), 0, KettleDatabaseRepository.JOB_ATTRIBUTE_LOG_SIZE_LIMIT, 0, jobMeta.getLogSizeLimit());
        
        
		// Save the logging connection link...
		if (jobMeta.getLogConnection()!=null) {
			repository.insertJobEntryDatabase(jobMeta.getID(), -1L, jobMeta.getLogConnection().getID());
		}
	}

	public synchronized long insertJobHop(long id_job, long id_jobentry_copy_from, long id_jobentry_copy_to, boolean enabled, boolean evaluation, boolean unconditional) throws KettleException {
		long id = repository.connectionDelegate.getNextJobHopID();

		RowMetaAndData table = new RowMetaAndData();

		table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_JOB_HOP_ID_JOB_HOP, ValueMetaInterface.TYPE_INTEGER), Long.valueOf(id));
		table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_JOB_HOP_ID_JOB, ValueMetaInterface.TYPE_INTEGER), Long.valueOf(id_job));
		table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_JOB_HOP_ID_JOBENTRY_COPY_FROM, ValueMetaInterface.TYPE_INTEGER), Long.valueOf(id_jobentry_copy_from));
		table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_JOB_HOP_ID_JOBENTRY_COPY_TO, ValueMetaInterface.TYPE_INTEGER), Long.valueOf(id_jobentry_copy_to));
		table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_JOB_HOP_ENABLED, ValueMetaInterface.TYPE_BOOLEAN), Boolean.valueOf(enabled));
		table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_JOB_HOP_EVALUATION, ValueMetaInterface.TYPE_BOOLEAN), Boolean.valueOf(evaluation));
		table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_JOB_HOP_UNCONDITIONAL, ValueMetaInterface.TYPE_BOOLEAN), Boolean.valueOf(unconditional));

		repository.connectionDelegate.getDatabase().prepareInsert(table.getRowMeta(), KettleDatabaseRepository.TABLE_R_JOB_HOP);
		repository.connectionDelegate.getDatabase().setValuesInsert(table);
		repository.connectionDelegate.getDatabase().insertRow();
		repository.connectionDelegate.getDatabase().closeInsert();

		return id;
	}

	public String getJobAttributeString(long id_job, int nr, String code) throws KettleException {
		return repository.connectionDelegate.getJobAttributeString(id_job, nr, code);
	}
	
	public long getJobAttributeInteger(long id_job, int nr, String code) throws KettleException {
		return repository.connectionDelegate.getJobAttributeInteger(id_job, nr, code);
	}
	
	public boolean getJobAttributeBoolean(long id_job, int nr, String code) throws KettleException {
		return repository.connectionDelegate.getJobAttributeBoolean(id_job, nr, code);
	}
	
	public synchronized void moveJob(String jobname, long id_directory_from, long id_directory_to) throws KettleException
	{
		String sql = "UPDATE "+quoteTable(KettleDatabaseRepository.TABLE_R_JOB)+" SET "+quote(KettleDatabaseRepository.FIELD_JOB_ID_DIRECTORY)+" = ? WHERE "+quote(KettleDatabaseRepository.FIELD_JOB_NAME)+" = ? AND "+quote(KettleDatabaseRepository.FIELD_JOB_ID_DIRECTORY)+" = ?";

		RowMetaAndData par = new RowMetaAndData();
		par.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_JOB_ID_DIRECTORY, ValueMetaInterface.TYPE_INTEGER), new Long(id_directory_to));
		par.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_JOB_NAME,  ValueMetaInterface.TYPE_STRING), jobname);
		par.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_JOB_ID_DIRECTORY, ValueMetaInterface.TYPE_INTEGER), new Long(id_directory_from));

		repository.connectionDelegate.getDatabase().execStatement(sql, par.getRowMeta(), par.getData());
	}

	public synchronized void renameJob(long id_job, String newname) throws KettleException
	{
		String sql = "UPDATE "+quoteTable(KettleDatabaseRepository.TABLE_R_JOB)+" SET "+quote(KettleDatabaseRepository.FIELD_JOB_NAME)+" = ? WHERE "+quote(KettleDatabaseRepository.FIELD_JOB_ID_JOB)+" = ?";

		RowMetaAndData table = new RowMetaAndData();
		table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_JOB_NAME, ValueMetaInterface.TYPE_STRING), newname);
		table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_JOB_ID_JOB, ValueMetaInterface.TYPE_INTEGER), new Long(id_job));

		repository.connectionDelegate.getDatabase().execStatement(sql, table.getRowMeta(), table.getData());
	}
}
