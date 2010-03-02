 /* Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.*/
 
package org.pentaho.di.job.entry;
import java.util.List;
import java.util.Map;

import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.SQLStatement;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.resource.ResourceDefinition;
import org.pentaho.di.resource.ResourceNamingInterface;
import org.pentaho.di.resource.ResourceReference;
import org.w3c.dom.Node;




/**
 * Interface for the different JobEntry classes.
 * 
 * @author Matt
 * @since 18-06-04
 * 
 */

public interface JobEntryInterface
{
	public Result execute(Result prev_result, int nr) throws KettleException;
	
	public void setParentJob(Job job);
	public Job getParentJob();

	public LogChannelInterface getLogChannel();
	
	public void setRepository(Repository repository);
	
	public void    clear();
	public ObjectId getObjectId();
	public void     setObjectId(ObjectId id);
	public String  getName();
	public void    setName(String name);

	// TypeId are the same, should be refactored out later.
	//
	public String  getPluginId();
	public void    setPluginId(String pluginId);
    
	public String  getDescription();
	public void    setDescription(String description);
	public void    setChanged();
	public void    setChanged(boolean ch);
	public boolean hasChanged();

	public void    loadXML(Node entrynode, List<DatabaseMeta> databases, List<SlaveServer> slaveServers, Repository rep) throws KettleXMLException;
	public String  getXML();
	public void    saveRep(Repository rep, ObjectId id_job) throws KettleException;
	public void    loadRep(Repository rep, ObjectId id_jobentry, List<DatabaseMeta> databases, List<SlaveServer> slaveServers) throws KettleException;

	public boolean isStart();
	public boolean isDummy();
	public Object  clone();
	
	public boolean resetErrorsBeforeExecution();
	public boolean evaluates();
	public boolean isUnconditional();
	
	public boolean isEvaluation();
	public boolean isTransformation();
	public boolean isJob();
	public boolean isShell();
	public boolean isMail();
	public boolean isSpecial();
    
    public List<SQLStatement> getSQLStatements(Repository repository) throws KettleException;
    public List<SQLStatement> getSQLStatements(Repository repository, VariableSpace space) throws KettleException;
    
    /**
     * Get the name of the class that implements the dialog for this job entry
     * JobEntryBase provides a default
     */
    public String getDialogClassName();
    
    public String getFilename();
    public String getRealFilename();
    
    /**
     * This method returns all the database connections that are used by the job entry.
     * @return an array of database connections meta-data.
     *         Return an empty array if no connections are used.
     */
    public DatabaseMeta[] getUsedDatabaseConnections();
    
    /**
     * Allows JobEntry objects to check themselves for consistency
     * @param remarks List of CheckResult objects indicating check status
     * @param jobMeta JobMeta
     */
    public void check(List<CheckResultInterface> remarks, JobMeta jobMeta);
    
    /**
     * Get a list of all the resource dependencies that the step is depending on.
     * 
     * @return a list of all the resource dependencies that the step is depending on
     */
    public List<ResourceReference> getResourceDependencies(JobMeta jobMeta);

	/**
	 * Exports the object to a flat-file system, adding content with filename keys to a set of definitions.
	 * The supplied resource naming interface allows the object to name appropriately without worrying about those parts of the implementation specific details.
	 *  
	 * @param space The variable space to resolve (environment) variables with.
	 * @param definitions The map containing the filenames and content
	 * @param namingInterface The resource naming interface allows the object to name appropriately
	 * @param repository The repository to load resources from
	 * 
	 * @return The filename for this object. (also contained in the definitions map)
	 * @throws KettleException in case something goes wrong during the export
	 */
	public String exportResources(VariableSpace space, Map<String, ResourceDefinition> definitions, ResourceNamingInterface namingInterface, Repository repository) throws KettleException;

}
