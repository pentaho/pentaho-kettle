package org.pentaho.di.core.logging;

import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.ObjectRevision;
import org.pentaho.di.repository.RepositoryDirectory;

public interface LoggingObjectInterface {
	/**
	 * @return the name
	 */
	public String getName();

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
	 * Sets the log channel id of this logging object representation
	 * @param logChannelId the log channel id to set
	 */
	public void setLogChannelId(String logChannelId);

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
	public String getCopy();
}
