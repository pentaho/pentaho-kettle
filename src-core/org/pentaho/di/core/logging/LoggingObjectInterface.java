package org.pentaho.di.core.logging;

import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.ObjectRevision;
import org.pentaho.di.repository.RepositoryDirectory;

public interface LoggingObjectInterface {
	/**
	 * @return the name
	 */
	public String getObjectName();

	/**
	 * @return the repositoryDirectory
	 */
	public RepositoryDirectory getRepositoryDirectory();

	/**
	 * @return the filename
	 */
	public String getFilename();

	/**
	 * @return the objectId in a repository
	 */
	public ObjectId getObjectId();

	/**
	 * @return the object revision in a repository
	 */
	public ObjectRevision getObjectRevision();

	/**
	 * @return the log channel id
	 */
	public String getLogChannelId();

	/**
	 * @return the parent
	 */
	public LoggingObjectInterface getParent();

	/**
	 * @return the objectType
	 */
	public LoggingObjectType getObjectType();
	
	/**
	 * @return A string identifying a copy in a series of steps...
	 */
	public String getObjectCopy();
	
	/**
	 * @return The logging level of the log channel of this logging object.
	 */
	public LogLevel getLogLevel();
	
	/**
	 * @return The execution container (Carte/DI server/BI Server) object id.
	 * We use this to see to which copy of the job/trans hierarchy this object belongs.
	 * If it is null, we assume that we are running a single copy in Spoon/Pan/Kitchen.
	 * 
	 */
	public String getContainerObjectId();
}
