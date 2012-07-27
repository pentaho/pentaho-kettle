/*******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
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
	
	/**
	 * @return true if this logging object is gathering kettle metrics during execution
	 */
	public boolean isGatheringMetrics();
	
	/**
	 * Enable of disable kettle metrics gathering during execution
	 * @param gatheringMetrics set to true to enable metrics gathering during execution. 
	 */
	public void setGatheringMetrics(boolean gatheringMetrics);
}
