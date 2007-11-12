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
 

package org.pentaho.di.trans.step;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.pentaho.di.core.SQLStatement;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.resource.ResourceDefinition;
import org.pentaho.di.resource.ResourceNamingInterface;
import org.pentaho.di.resource.ResourceReference;
import org.pentaho.di.trans.DatabaseImpact;
import org.pentaho.di.trans.TransMeta;


/*
 * Created on 19-June-2003
 *
 */

public class BaseStepMeta implements Cloneable
{
	private boolean changed;
	private long    id;
    
    /** database connection object to use for searching fields & checking steps */
    protected Database databases[];
    
    /** The repository that is being used for this step */
    protected Repository repository;

	public BaseStepMeta()
	{
		changed    = false;
	}
	
	public long getID()
	{
		return id;
	}
	
	public void setID(long id)
	{
		this.id = id;
	}

	public Object clone()
	{
		try
		{
			Object retval = super.clone();
			return retval;
		}
		catch(CloneNotSupportedException e)
		{
			return null;
		}
	}
	
	public void setChanged(boolean ch)
	{
		changed=ch;
	}

	public void setChanged()
	{
		changed=true;
	}
	
	public boolean hasChanged()
	{
		return changed;
	}
	
	public RowMetaInterface getTableFields()
	{
		return null;
	}
	
	/**
	 * @param steps optionally search the info step in a list of steps
	 */
	public void searchInfoAndTargetSteps(List<StepMeta> steps)
	{
	}

	public boolean chosesTargetSteps()
	{
	    return false;
	}

	public String[] getTargetSteps()
	{
	    return null;
	}

	/**
	 * @return the informational source steps, if any. Null is the default: none.
	 */
	public String[] getInfoSteps()
	{
	    return null;
	}
    
    /**
     * @param infoSteps The info step(s) to set
     */
    public void setInfoSteps(StepMeta[] infoSteps)
    {
    }
    
    /**
     * @param targetSteps The target step(s) to set
     */
    public void setTargetSteps(StepMeta[] targetSteps)
    {
    }

	/**
	 * Produces the XML string that describes this step's information.
	 * 
	 * @return String containing the XML describing this step.
	 */
	public String getXML()
	{
		String retval="";

		return retval;
	}

	
	/*
	    getFields determines which fields are
	      - added to the stream
	      - removed from the stream
	      - renamed
	      - changed
	        
	 * @param inputRowMeta Row containing fields that are used as input for the step.
	 * @param name Name of the step
	 * @param info Fields used as extra lookup information
	 * 
	 * @return The fields that are being put out by this step.
	 */
	public void getFields(RowMetaInterface inputRowMeta, String name, RowMetaInterface[] info, StepMeta nextStep, VariableSpace space) throws KettleStepException
	{
		// Default: no values are added to the row in the step
	}
	

    /**
     * Each step must be able to report on the impact it has on a database, table field, etc.
     * @param impact The list of impacts @see org.pentaho.di.transMeta.DatabaseImpact
     * @param transMeta The transformation information
     * @param stepMeta The step information
     * @param prev The fields entering this step
     * @param input The previous step names
     * @param output The output step names
     * @param info The fields used as information by this step
     */
    public void analyseImpact(List<DatabaseImpact> impact, TransMeta transMeta, StepMeta stepMeta, RowMetaInterface prev, String input[], String output[], RowMetaInterface info) throws KettleStepException
    {
        
    }
	
    
	/**
	 * Standard method to return one or more SQLStatement objects that the step needs in order to work correctly.
	 * This can mean "create table", "create index" statements but also "alter table ... add/drop/modify" statements.
	 *
	 * @return The SQL Statements for this step or null if an error occurred.  If nothing has to be done, the SQLStatement.getSQL() == null. 
	 * @param transMeta TransInfo object containing the complete transformation
	 * @param stepMeta StepMeta object containing the complete step
	 * @param prev Row containing meta-data for the input fields (no data)
	 */
	public SQLStatement getSQLStatements(TransMeta transMeta, StepMeta stepMeta, RowMetaInterface prev)  throws KettleStepException
	{
		// default: this doesn't require any SQL statements to be executed!
		return new SQLStatement(stepMeta.getName(), null, null);
	}
    
    /**
     *  Call this to cancel trailing database queries (too long running, etc)
     */
    public void cancelQueries() throws KettleDatabaseException
    {
        //
        // Cancel all defined queries...
        //
        if (databases!=null)
        {
            for (int i=0;i<databases.length;i++)
            {
                if (databases[i]!=null) databases[i].cancelQuery();
            }
        }
    }
    
    /**
     * Default a step doesn't use any arguments.
     * Implement this to notify the GUI that a window has to be displayed BEFORE launching a transformation.
     * 
     * @return A row of argument values. (name and optionally a default value)
     */
    public Map<String,String> getUsedArguments()
    {
        return null;
    }

    /**
     * The natural way of data flow in a transformation is source-to-target.
     * However, this makes mapping to target tables difficult to do.
     * To help out here, we supply information to the transformation meta-data model about which fields are required for a step.
     * This allows us to automate certain tasks like the mapping to pre-defined tables.
     * The Table Output step in this case will output the fields in the target table using this method. 
     * 
     * This default implementation returns an empty row meaning that no fields are required for this step to operate.
     * @return the required fields for this steps meta data.
     * @throws KettleException in case the required fields can't be determined
     */
    public RowMetaInterface getRequiredFields() throws KettleException
    {
        return new RowMeta();
    }

    /**
     * This method returns all the database connections that are used by the step.
     * @return an array of database connections meta-data.
     *         Return an empty array if no connections are used.
     */
    public DatabaseMeta[] getUsedDatabaseConnections()
    {
        return new DatabaseMeta[] { };
    }
    
    /**
     * @return the libraries that this step or plug-in uses.
     */
    public String[] getUsedLibraries()
    {
        return new String[] {};
    }
        
    /**
     * @return true if this step supports error "reporting" on rows: the ability to send rows to a certain target step.
     */
    public boolean supportsErrorHandling()
    {
        return false;
    }
    
    /**
     * This method is added to exclude certain steps from layout checking.  
     * @since 2.5.0
     */
    public boolean excludeFromRowLayoutVerification()
    {
        return false;
    }
    
    /**
     * Get a list of all the resource dependencies that the step is depending on.
     * 
     * @return a list of all the resource dependencies that the step is depending on
     */
    public List<ResourceReference> getResourceDependencies(TransMeta transMeta, StepMeta stepInfo) {
      List<ResourceReference> references = new ArrayList<ResourceReference>(5); // Lower the initial capacity - unusual to have more than 1 actually
      ResourceReference reference = new ResourceReference(stepInfo);
      references.add(reference);
      return references;
    }
    
    public String exportResources(VariableSpace space, Map<String, ResourceDefinition> definitions, ResourceNamingInterface resourceNamingInterface) throws KettleException {
    	return null;
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
    	if( className.endsWith("Meta") ) {
    		className = className.substring(0, className.length()-4 );
    	}
    	className += "Dialog";
    	return className;
    }
}
