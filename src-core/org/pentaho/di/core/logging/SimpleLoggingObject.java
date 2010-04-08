package org.pentaho.di.core.logging;

import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.ObjectRevision;
import org.pentaho.di.repository.RepositoryDirectory;

public class SimpleLoggingObject implements LoggingObjectInterface {

	private String					objectName;
	private LoggingObjectType		objectType;
	private LoggingObjectInterface	parent;
	private LogLevel logLevel = DefaultLogLevel.getLogLevel();
	private String containerObjectId;

	/**
	 * @param objectName
	 * @param loggingObjectType
	 * @param parent
	 */
	public SimpleLoggingObject(String objectName, LoggingObjectType loggingObjectType, LoggingObjectInterface parent) {
		this.objectName = objectName;
		this.objectType = loggingObjectType;
		this.parent = parent;
		if (parent!=null) {
		  this.logLevel = parent.getLogLevel();
		  this.containerObjectId = parent.getContainerObjectId();
		}
	}

	/**
	 * @return the name
	 */
	public String getObjectName() {
		return objectName;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setObjectName(String name) {
		this.objectName = name;
	}

	/**
	 * @return the objectType
	 */
	public LoggingObjectType getObjectType() {
		return objectType;
	}

	/**
	 * @param objectType
	 *            the objectType to set
	 */
	public void setObjectType(LoggingObjectType objectType) {
		this.objectType = objectType;
	}

	/**
	 * @return the parent
	 */
	public LoggingObjectInterface getParent() {
		return parent;
	}

	/**
	 * @param parent
	 *            the parent to set
	 */
	public void setParent(LoggingObjectInterface parent) {
		this.parent = parent;
	}

	public String getFilename() {
		return null;
	}

	public String getLogChannelId() {
		return null;
	}

	public String getObjectCopy() {
		return null;
	}

	public ObjectId getObjectId() {
		return null;
	}

	public ObjectRevision getObjectRevision() {
		return null;
	}

	public RepositoryDirectory getRepositoryDirectory() {
		return null;
	}

  public LogLevel getLogLevel() {
    return logLevel;
  }

  public void setLogLevel(LogLevel logLevel) {
    this.logLevel = logLevel;
  }
  
  public String getContainerObjectId() {
    return containerObjectId;
  }
  
  public void setContainerObjectId(String containerObjectId) {
    this.containerObjectId = containerObjectId;
  }
}
