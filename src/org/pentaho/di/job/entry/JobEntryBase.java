 /**********************************************************************
 **                                                                   **
 **               This code belongs to the KETTLE project.            **
 **                                                                   **
 ** Kettle, from version 2.2 on, is released into the public domain   **
 ** under the Lesser GNU Public License (LGPL).                       **
 **                                                                   **
 ** For more details, please read the document LICENSE.txt, included  **
 ** in this project                                                   **
 **                                                                   **
 ** http://www.kettle.be                                              **
 ** info@kettle.be                                                    **
 **                                                                   **
 **********************************************************************/

package org.pentaho.di.job.entry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.CheckResultSourceInterface;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.SQLStatement;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.job.JobEntryType;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.resource.ResourceDefinition;
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
public class JobEntryBase implements Cloneable, VariableSpace, CheckResultSourceInterface
{
	private String  name;
	private String  description;
    private String  pluginID = null;
    
    /**
	 * Id as defined in the xml or annotation.
	 */
	private String configId;
	
	private boolean changed;
	private JobEntryType type;
	private long    id;

	private VariableSpace variables = new Variables();

	public JobEntryBase()
	{
		name=null;
		description=null;
	}

	public JobEntryBase(String name, String description)
	{
		setName(name);
		setDescription(description);
		setID(-1L);
	}

	public JobEntryBase(JobEntryBase jeb)
	{
		setName(jeb.getName());
		setDescription(jeb.getDescription());
		setJobEntryType(jeb.getJobEntryType());
		setID(jeb.getID());
	}

	public void clear()
	{
		name = null;
		description = null;
		changed = false;
	}

	public void setID(long id)
	{
		this.id=id;
	}

	public long getID()
	{
		return id;
	}

	public void setJobEntryType(JobEntryType type)
	{
		this.type = type;
	}

    public String getPluginID()
    {
        return this.pluginID;
    }

  /**
   * Support for CheckResultSourceInterface
   */
  public String getTypeId() {
    return getTypeCode();
  }

  public JobEntryType getJobEntryType()
	{
		return type;
	}

  public String getTypeCode()
	{
		if (this.pluginID != null)
			return this.pluginID;
		return type.toString();// JobEntryInterface.typeCode[type];
	}


	public static final String getTypeCode(JobEntryType type)
	{
		return type.toString();
	}

	public String getTypeDesc()
	{
		return type.getDescription();
	}

	public static final String getTypeDesc(JobEntryType type)
	{
		return type.getDescription();
	}

	public void setName(String name)
	{
		this.name = name;
	}

    public void setPluginID(String pid)
    {
        this.pluginID = pid;
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
		changed=ch;
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
		return getJobEntryType() == JobEntryType.EVALUATION;
	}

	public boolean isJob()
	{
		return getJobEntryType() == JobEntryType.JOB;
	}

	public boolean isMail()
	{
		return getJobEntryType() == JobEntryType.MAIL;
	}

	public boolean isShell()
	{
		return getJobEntryType() == JobEntryType.MAIL;
	}

	public boolean isSpecial()
	{
		return getJobEntryType() == JobEntryType.SPECIAL;
	}

	public boolean isTransformation()
	{
		return getJobEntryType() == JobEntryType.TRANSFORMATION;
	}

	public boolean isFTP()
	{
		return getJobEntryType() == JobEntryType.FTP;
	}

	public boolean isSFTP()
	{
		return getJobEntryType() == JobEntryType.SFTP;
	}

	public boolean isHTTP()
	{
		return getJobEntryType() == JobEntryType.HTTP;
	}


    // Add here for the new types?

	public String getXML()
	{
        StringBuffer retval = new StringBuffer();
		retval.append("      ").append(XMLHandler.addTagValue("name",         getName()));
		retval.append("      ").append(XMLHandler.addTagValue("description",  getDescription()));
		if (type != JobEntryType.NONE)
            retval.append("      ").append(XMLHandler.addTagValue("type",     getTypeCode()));
        if (pluginID != null)
          retval.append("      ").append(XMLHandler.addTagValue("type",     pluginID));


		return retval.toString();
	}

	public void loadXML(Node entrynode, List<DatabaseMeta> databases)
		throws KettleXMLException
	{
		try
		{
			setName( XMLHandler.getTagValue(entrynode, "name") );
			setDescription( XMLHandler.getTagValue(entrynode, "description") );
			String stype = XMLHandler.getTagValue(entrynode, "type");
			setJobEntryType(JobEntryCopy.getType(stype));
		}
		catch(Exception e)
		{
			throw new KettleXMLException("Unable to load base info for job entry", e);
		}
	}

    public void parseRepositoryObjects(Repository rep) throws KettleException
    {
    }

	public void saveRep(Repository rep, long id_job)
		throws KettleException
	{
		try
		{
			setID( rep.insertJobEntry(id_job, getName(), getDescription(), getTypeCode()) );
		}
		catch(KettleDatabaseException dbe)
		{
			throw new KettleException("Unable to save job entry base information to the repository for id_job="+id_job, dbe);
		}
	}

	public void loadRep(Repository rep, long id_jobentry, List<DatabaseMeta> databases)
		throws KettleException
	{
		try
		{
			RowMetaAndData r = rep.getJobEntry(id_jobentry);
			if( r!=null)
			{
				setName( r.getString("NAME", null) );

				setDescription( r.getString("DESCRIPTION", null) );
				int id_jobentry_type = (int) r.getInteger("ID_JOBENTRY_TYPE", 0);
				RowMetaAndData jetrow = rep.getJobEntryType(id_jobentry_type);
				if (jetrow!=null)
				{
					type = JobEntryCopy.getType( jetrow.getString("CODE", null) );
				}
			}
		}
		catch(KettleDatabaseException dbe)
		{
			throw new KettleException("Unable to load base job entry information from the repository for id_jobentry="+id_jobentry, dbe);
		}
	}

	public Object clone()
	{
		JobEntryBase je;
		try
		{
			je = (JobEntryBase)super.clone();
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

	public String getVariable(String variableName, String defaultValue)
	{
		return variables.getVariable(variableName, defaultValue);
	}

	public String getVariable(String variableName)
	{
		return variables.getVariable(variableName);
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

	public void injectVariables(Map<String,String> prop)
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
     * Get a list of all the resource dependencies that the step is depending on.
     *
     * @return a list of all the resource dependencies that the step is depending on
     */
    public List<ResourceReference> getResourceDependencies() 
    {
    	return new ArrayList<ResourceReference>(); // default: return an empty resource dependency list.
    }
    
    public String exportResources(VariableSpace space, Map<String, ResourceDefinition> definitions, ResourceNamingInterface namingInterface) throws KettleException {
  	  return null;
    }

	public String getConfigId()
	{
		return configId;
	}

	public void setConfigId(String configId)
	{
		this.configId = configId;
	}
}