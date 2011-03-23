/*
 * Copyright (c) 2010 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
 */
package org.pentaho.di.core.logging;

import java.util.Date;

import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.ObjectRevision;
import org.pentaho.di.repository.RepositoryDirectoryInterface;

public interface LoggingObjectInterface {
	/**
	 * @return the name
	 */
	public String getObjectName();

	/**
	 * @return the repositoryDirectory
	 */
	public RepositoryDirectoryInterface getRepositoryDirectory();

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
	
	/**
	 * @return The registration date of this logging object.  Null if it's not registered.
	 */
	public Date getRegistrationDate();
}
