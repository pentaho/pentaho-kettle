package org.pentaho.di.core.logging;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.ObjectRevision;
import org.pentaho.di.repository.RepositoryDirectory;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.www.CarteServletInterface;

public class LoggingObject implements LoggingObjectInterface {
	
	private String logChannelId;
	private LoggingObjectType objectType;
	private String name;
	private String copy;
	private RepositoryDirectory repositoryDirectory;
	private String filename;
	private ObjectId objectId;
	private ObjectRevision objectRevision;
	
	private LoggingObjectInterface parent;
	
	public LoggingObject(Object object) {
		if (object instanceof Trans) grabTransInformation((Trans)object);
		else if (object instanceof StepInterface) grabStepInformation((StepInterface)object);
		else if (object instanceof Job) grabJobInformation((Job)object);
		else if (object instanceof Database) grabDatabaseInformation((Database)object);
		
		else if (object instanceof TransMeta) grabTransMetaInformation((TransMeta)object);
		else if (object instanceof JobMeta) grabJobMetaInformation((JobMeta)object);
		else if (object instanceof StepMetaInterface) grabStepMetaInformation((StepMetaInterface)object);
		else if (object instanceof JobEntryInterface) grabJobEntryInformation((JobEntryInterface)object);
		
		else if (object instanceof CarteServletInterface) grabCarteServletInformation((CarteServletInterface)object);
		
		else grabObjectInformation(object);
	}
	
	public boolean equals(Object obj) {
		if (!(obj instanceof LoggingObject)) return false;
		if (obj == this) return true;
		
		try {
			LoggingObject loggingObject = (LoggingObject) obj;
	
			// See if we recognize the repository ID, this is an absolute match
			//
			if (loggingObject.getObjectId()!=null && loggingObject.getObjectId().equals(getObjectId())) {
				return true;
			}
			
			// If the filename is the same, it's the same object...
			//
			if (!Const.isEmpty(loggingObject.getFilename()) && loggingObject.getFilename().equals(getFilename())) {
				return true;
			}
			
			// See if the name & type and parent name & type is the same.
			// This will catch most matches except for the most exceptional use-case.
			//
			if (loggingObject.getName().equals(getName()) && loggingObject.getObjectType().equals(getObjectType())) {
				
				// If there are multiple copies of this object, they both need their own channel
				//
				if (!Const.isEmpty(getCopy()) && !getCopy().equals(loggingObject.getCopy())) {
					return false;
				}
				
				LoggingObjectInterface parent1 = loggingObject.getParent();
				LoggingObjectInterface parent2 = getParent();
				
				if ((parent1!=null && parent2==null) || (parent1==null && parent2!=null)) return false;
				if (parent1==null && parent2==null) return true;
				
				// This goes to the parent recursively...
				//
				if (parent1.equals(parent2)) {
					return true;
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
		}

		return false;
	}
	
	private void grabTransInformation(Trans trans) {
		TransMeta transMeta = trans.getTransMeta();
		
		objectType = LoggingObjectType.TRANS;
		if (transMeta!=null) {
			name = transMeta.getName();
			repositoryDirectory = transMeta.getRepositoryDirectory();
			filename = transMeta.getFilename();
			objectId = transMeta.getObjectId();
			objectRevision = transMeta.getObjectRevision();
		} else {
			name = "<Anonymous transformation>";
		}
		
		if (trans.getParentTrans()!=null) {
			getParentLoggingObject(trans.getParentTrans());
		} else if (trans.getParentJob()!=null) {
			getParentLoggingObject(trans.getParentJob());
		}
	}

	private void grabTransMetaInformation(TransMeta transMeta) {
		
		objectType = LoggingObjectType.TRANSMETA;
		name = transMeta.getName();
		repositoryDirectory = transMeta.getRepositoryDirectory();
		filename = transMeta.getFilename();
		objectId = transMeta.getObjectId();
		objectRevision = transMeta.getObjectRevision();
		
		parent = null; // We don't know this from the metadata.  It's only known at runtime!
	}

	private void grabStepInformation(StepInterface step) {
		StepMeta stepMeta = step.getStepMeta();
		
		objectType = LoggingObjectType.STEP;
		name = stepMeta.getName();
		objectId = stepMeta.getObjectId();
		copy = Integer.toString(step.getCopy());
		
		getParentLoggingObject(step.getTrans());
	}

	private void grabStepMetaInformation(StepMetaInterface step) {
		StepMeta stepMeta = step.getParentStepMeta();
		
		objectType = LoggingObjectType.STEPMETA;
		if (step.getParentStepMeta()==null) {
			name = step.getClass().getName();
			objectId = null;
		} else {
			name = stepMeta.getName();
			objectId = stepMeta.getObjectId();
		}
		
		getParentLoggingObject(stepMeta.getParentTransMeta());
	}

	private void grabJobInformation(Job job) {
		JobMeta jobMeta = job.getJobMeta();
		
		objectType = LoggingObjectType.JOB;
		if (jobMeta!=null) {
			name = jobMeta.getName();
			repositoryDirectory = jobMeta.getRepositoryDirectory();
			filename = jobMeta.getFilename();
			objectId = jobMeta.getObjectId();
			objectRevision = jobMeta.getObjectRevision();
		}

		if (job.getParentJob()!=null) {
			getParentLoggingObject(job.getParentJob());
		}
	}

	private void grabJobMetaInformation(JobMeta jobMeta) {

		objectType = LoggingObjectType.JOB;
		name = jobMeta.getName();
		repositoryDirectory = jobMeta.getRepositoryDirectory();
		filename = jobMeta.getFilename();
		objectId = jobMeta.getObjectId();
		objectRevision = jobMeta.getObjectRevision();

		parent = null; // We don't know this from the metadata.  It's only known at runtime!
	}

	private void grabJobEntryInformation(JobEntryInterface jobEntry) {
		
		objectType = LoggingObjectType.JOBENTRY;
		name = jobEntry.getName();
		objectId = jobEntry.getObjectId();
		
		getParentLoggingObject(jobEntry.getParentJob());
	}

	private void grabDatabaseInformation(Database database) {
		DatabaseMeta databaseMeta = database.getDatabaseMeta();
		
		objectType = LoggingObjectType.DATABASE;
		name = databaseMeta.getName();
		objectId = databaseMeta.getObjectId();
		objectRevision = databaseMeta.getObjectRevision();
		
		getParentLoggingObject(database.getParentObject());
	}
	
	private void grabCarteServletInformation(CarteServletInterface servlet) {
		
		objectType = LoggingObjectType.SERVLET;
		name = servlet.getService();
		objectId = null;
		
		parent = null; 
	}


	private void grabObjectInformation(Object object) {
		objectType = LoggingObjectType.GENERAL;
		name = object.toString(); // name of class or name of object..
		
		parent = null;
	}

	private void getParentLoggingObject(Object parentObject) {
		
		if (parentObject==null) {
			return;
		}
		
		LoggingRegistry registry = LoggingRegistry.getInstance();
		
		// Extract the hierarchy information from the parentObject...
		//
		LoggingObjectInterface check = new LoggingObject(parentObject);
		LoggingObjectInterface loggingObject = registry.findExistingLoggingSource(check);
		if (loggingObject==null) {
			String logChannelId = registry.registerLoggingSource(loggingObject)+"";
			loggingObject = check;
			check.setLogChannelId(logChannelId);
		}
		
		parent = loggingObject;
	}
	
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * @return the repositoryDirectory
	 */
	public RepositoryDirectory getRepositoryDirectory() {
		return repositoryDirectory;
	}
	/**
	 * @param repositoryDirectory the repositoryDirectory to set
	 */
	public void setRepositoryDirectory(RepositoryDirectory repositoryDirectory) {
		this.repositoryDirectory = repositoryDirectory;
	}
	/**
	 * @return the filename
	 */
	public String getFilename() {
		return filename;
	}
	/**
	 * @param filename the filename to set
	 */
	public void setFilename(String filename) {
		this.filename = filename;
	}
	/**
	 * @return the objectId
	 */
	public ObjectId getObjectId() {
		return objectId;
	}
	/**
	 * @param objectId the objectId to set
	 */
	public void setObjectId(ObjectId objectId) {
		this.objectId = objectId;
	}

	/**
	 * @return the objectRevision
	 */
	public ObjectRevision getObjectRevision() {
		return objectRevision;
	}

	/**
	 * @param objectRevision the objectRevision to set
	 */
	public void setObjectRevision(ObjectRevision objectRevision) {
		this.objectRevision = objectRevision;
	}

	/**
	 * @return the id
	 */
	public String getLogChannelId() {
		return logChannelId;
	}

	/**
	 * @param id the id to set
	 */
	public void setLogChannelId(String logChannelId) {
		this.logChannelId = logChannelId;
	}

	/**
	 * @return the parent
	 */
	public LoggingObjectInterface getParent() {
		return parent;
	}

	/**
	 * @param parent the parent to set
	 */
	public void setParent(LoggingObjectInterface parent) {
		this.parent = parent;
	}

	/**
	 * @return the objectType
	 */
	public LoggingObjectType getObjectType() {
		return objectType;
	}

	/**
	 * @param objectType the objectType to set
	 */
	public void setObjectType(LoggingObjectType objectType) {
		this.objectType = objectType;
	}

	/**
	 * @return the copy
	 */
	public String getCopy() {
		return copy;
	}

	/**
	 * @param copy the copy to set
	 */
	public void setCopy(String copy) {
		this.copy = copy;
	}
}
