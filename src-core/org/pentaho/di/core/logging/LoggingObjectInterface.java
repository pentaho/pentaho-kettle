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
	 * @return The log level for this logging object
	 */
	public int getLogLevel();
	
	/**
	 * @Param The LogWriter log level for this logging object
	 */
	public void setLogLevel(int logLevel);
}
