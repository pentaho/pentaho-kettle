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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.CheckResultSourceInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.SQLStatement;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.core.logging.LoggingObjectType;
import org.pentaho.di.core.plugins.JobEntryPluginType;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.repository.LongObjectId;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.ObjectRevision;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectory;
import org.pentaho.di.resource.ResourceDefinition;
import org.pentaho.di.resource.ResourceHolderInterface;
import org.pentaho.di.resource.ResourceNamingInterface;
import org.pentaho.di.resource.ResourceReference;
import org.w3c.dom.Node;

/**
 * Base class for the different types of job-entries...
 *
 * @author Matt
 * Created on 18-jun-04
 *
 */
public class JobEntryBase implements Cloneable, VariableSpace, CheckResultSourceInterface, ResourceHolderInterface, LoggingObjectInterface
{ 
  private String name;

  private String description;

  /**
   * Id as defined in the xml or annotation.
   */
  private String configId;

  private boolean changed;

  private ObjectId id;
  
  protected VariableSpace variables = new Variables();

  protected Repository	rep;
  
  protected Job parentJob;
  
  protected LogChannelInterface log;
 
  public JobEntryBase()
  {
    name = null;
    description = null;
    log = LogChannel.GENERAL;
  }

  public JobEntryBase(String name, String description)
  {
    setName(name);
    setDescription(description);
    setObjectId(null);

    log = LogChannel.GENERAL;
  }
  
  @Override
  public boolean equals(Object obj) {
	  return name.equalsIgnoreCase(((JobEntryBase)obj).getName());
  }

  public void clear()
  {
    name = null;
    description = null;
    changed = false;
  }

  public void setObjectId(ObjectId id)
  {
    this.id = id;
  }
  
  public void setID(long id) {
	  this.id = new LongObjectId(id);
  }

  public ObjectId getObjectId()
  {
    return id;
  }

  public String getTypeDesc()
  {
	  PluginInterface plugin = PluginRegistry.getInstance().findPluginWithId(JobEntryPluginType.getInstance(), configId);
	  return plugin.getDescription();
  }

  public void setName(String name)
  {
    this.name = name;
  }

  public String getName()
  {
    return name;
  }

  public void setDescription(String Description)
  {
    this.description = Description;
  }

  public String getDescription()
  {
    return description;
  }

  public void setChanged()
  {
    setChanged(true);
  }

  public void setChanged(boolean ch)
  {
    changed = ch;
  }

  public boolean hasChanged()
  {
    return changed;
  }

  public boolean isStart()
  {
    return false;
  }

  public boolean isDummy()
  {
    return false;
  }

  public boolean isEvaluation()
  {
		return true;
  }

  public boolean isJob()
  {
    return "JOB".equals(configId);
  }

  public boolean isMail()
  {
    return "MAIL".equals(configId);
  }

  public boolean isShell()
  {
    return "SHELL".equals(configId);
  }

  public boolean isSpecial()
  {
    return "SPECIAL".equals(configId);
  }

  public boolean isTransformation()
  {
	return "TRANS".equals(configId);
  }

  public boolean isFTP()
  {
	return "FTP".equals(configId);
  }

  public boolean isSFTP()
  {
	return "SFTP".equals(configId);
  }

  public boolean isHTTP()
  {
	return "HTTP".equals(configId);
  }

  // Add here for the new types?

  public String getXML()
  {
    StringBuffer retval = new StringBuffer();
    retval.append("      ").append(XMLHandler.addTagValue("name", getName()));
    retval.append("      ").append(XMLHandler.addTagValue("description", getDescription()));
    retval.append("      ").append(XMLHandler.addTagValue("type", configId));

    return retval.toString();
  }

  public void loadXML(Node entrynode, List<DatabaseMeta> databases, List<SlaveServer> slaveServers) throws KettleXMLException
  {
    try
    {
      setName(XMLHandler.getTagValue(entrynode, "name"));
      setDescription(XMLHandler.getTagValue(entrynode, "description"));
	} catch(Exception e)
    {
      throw new KettleXMLException("Unable to load base info for job entry", e);
    }
  }

  public void parseRepositoryObjects(Repository rep) throws KettleException
  {
  }

  public void loadRep(Repository rep, ObjectId id_jobentry, List<DatabaseMeta> databases, List<SlaveServer> slaveServers) throws KettleException {
  }

  public void saveRep(Repository rep, ObjectId id_job) throws KettleException {
  }
	
  public Object clone()
  {
    JobEntryBase je;
    try
    {
      je = (JobEntryBase) super.clone();
		}
		catch(CloneNotSupportedException cnse)
    {
      return null;
    }
    return je;
  }

  public String toString()
  {
    return name;
  }

  /**
   * @return true if a reset of the number of errors is required before execution.
   */
  public boolean resetErrorsBeforeExecution()
  {
    return true;
  }

  /**
   * check whether or not this job entry evaluates.
   * @return true if the job entry evaluates
   */
  public boolean evaluates()
  {
    return false;
  }

  public boolean isUnconditional()
  {
    return true;
  }

  public List<SQLStatement> getSQLStatements(Repository repository) throws KettleException
  {
	return getSQLStatements(repository, null);
  }

  public List<SQLStatement> getSQLStatements(Repository repository, VariableSpace space) throws KettleException
  {
    return new ArrayList<SQLStatement>();
  }  
  
  public String getFilename()
  {
    return null;
  }

  public String getRealFilename()
  {
    return null;
  }

  /**
   * This method returns all the database connections that are used by the job entry.
   * @return an array of database connections meta-data.
   *         Return an empty array if no connections are used.
   */
  public DatabaseMeta[] getUsedDatabaseConnections()
  {
        return new DatabaseMeta[] {};
  }

  public void copyVariablesFrom(VariableSpace space)
  {
    variables.copyVariablesFrom(space);
  }

  public String environmentSubstitute(String aString)
  {
    return variables.environmentSubstitute(aString);
  }

  public String[] environmentSubstitute(String aString[])
  {
    return variables.environmentSubstitute(aString);
  }

  public VariableSpace getParentVariableSpace()
  {
    return variables.getParentVariableSpace();
  }
  
  public void setParentVariableSpace(VariableSpace parent) 
  {
    variables.setParentVariableSpace(parent);
  }

  public String getVariable(String variableName, String defaultValue)
  {
    return variables.getVariable(variableName, defaultValue);
  }

  public String getVariable(String variableName)
  {
    return variables.getVariable(variableName);
  }
  
  public boolean getBooleanValueOfVariable(String variableName, boolean defaultValue) {
	if (!Const.isEmpty(variableName)) {
	  String value = environmentSubstitute(variableName);
	  if (!Const.isEmpty(value)) {
	    return ValueMeta.convertStringToBoolean(value);
	  }
	}
	return defaultValue;
  }

  public void initializeVariablesFrom(VariableSpace parent)
  {
    variables.initializeVariablesFrom(parent);
  }

  public String[] listVariables()
  {
    return variables.listVariables();
  }

  public void setVariable(String variableName, String variableValue)
  {
    variables.setVariable(variableName, variableValue);
  }

  public void shareVariablesWith(VariableSpace space)
  {
    variables = space;
  }

  public void injectVariables(Map<String, String> prop)
  {
    variables.injectVariables(prop);
  }

  /**
   * Support for overrides not having to put in a check method
   * @param remarks CheckResults from checking the job entry
   * @param jobMeta JobMeta information letting threading back to the JobMeta possible
   */
    public void check(List<CheckResultInterface> remarks, JobMeta jobMeta) {

	}

  /**
	 * Get a list of all the resource dependencies that the step is depending
	 * on.
	 * 
	 * @return a list of all the resource dependencies that the step is
	 *         depending on
	 */
	public List<ResourceReference> getResourceDependencies(JobMeta jobMeta) {
		return new ArrayList<ResourceReference>(5); // default: return an empty resource dependency list. Lower the initial capacity
	}

	public String exportResources(VariableSpace space, Map<String, ResourceDefinition> definitions, ResourceNamingInterface namingInterface, Repository repository) throws KettleException {
		return null;
	}

  public String getPluginId()
  {
    return configId;
  }

  public void setPluginId(String configId)
  {
    this.configId = configId;
  }
  
  /**
   * @deprecated in favor of getPluginId()
   */
  public String getTypeId() {
	  return getPluginId();
  }
  
  /**
   * @deprecated in favor of setPluginId()
   */
  public void setTypeId(String typeId) {
	  setPluginId(typeId);
  }

  /**
   * This returns the expected name for the dialog that edits a job entry.
   * The expected name is in the org.pentaho.di.ui tree and has a class name
   * that is the name of the job entry with 'Dialog' added to the end.
   *
   * e.g. if the job entry is org.pentaho.di.job.entries.zipfile.JobEntryZipFile
   * the dialog would be org.pentaho.di.ui.job.entries.zipfile.JobEntryZipFileDialog
   *
   * If the dialog class for a job entry does not match this pattern it should
   * override this method and return the appropriate class name
   *
   * @return full class name of the dialog
   */
  public String getDialogClassName()
  {
    String className = getClass().getCanonicalName();
    	className = className.replaceFirst("\\.di\\.", ".di.ui.");
    className += "Dialog";
    return className;
  }

    public String getHolderType() {
      return "JOBENTRY"; //$NON-NLS-1$
    }

    protected VariableSpace getVariables()
    {
      return variables;
    }
    
    public void setRepository(Repository repository) {
    	this.rep = repository;
    }
    
    public Repository getRepository() {
    	return rep;
    }
    
    public void setParentJob(Job parentJob) {
    	this.parentJob = parentJob;
    	this.log = new LogChannel(this);
    }
    
    public Job getParentJob() {
    	return parentJob;
    }

    public boolean isBasic() { return log.isBasic(); }
    public boolean isDetailed() { return log.isDetailed(); }
    public boolean isDebug() { return log.isDebug(); }
    public boolean isRowlevel() { return log.isRowLevel(); }
    public void logMinimal(String message) { log.logMinimal(message); }
    public void logMinimal(String message, Object...arguments) { log.logMinimal(message, arguments); }
    public void logBasic(String message) { log.logBasic(message); }
    public void logBasic(String message, Object...arguments) { log.logBasic(message, arguments); }
    public void logDetailed(String message) { log.logDetailed(message); }
    public void logDetailed(String message, Object...arguments) { log.logDetailed(message, arguments); }
    public void logDebug(String message) { log.logDebug(message); }
    public void logDebug(String message, Object...arguments) { log.logDebug(message, arguments); }
    public void logRowlevel(String message) { log.logRowlevel(message); }
    public void logRowlevel(String message, Object...arguments) { log.logRowlevel(message, arguments); }
    public void logError(String message) { log.logError(message); } 
    public void logError(String message, Throwable e) { log.logError(message, e); }
    public void logError(String message, Object...arguments) { log.logError(message, arguments); }
 
    public LogChannelInterface getLogChannel() {
		return log;
	}
    
	public String getLogChannelId() {
		return log.getLogChannelId();
	}

	public String getObjectName() {
		return getName();
	}
	
	public String getObjectCopy() {
		return null;
	}

	public ObjectRevision getObjectRevision() {
		return null;
	}

	public LoggingObjectType getObjectType() {
		return LoggingObjectType.JOBENTRY;
	}

	public LoggingObjectInterface getParent() {
		return parentJob;
	}

	public RepositoryDirectory getRepositoryDirectory() {
		return null;
	}
}