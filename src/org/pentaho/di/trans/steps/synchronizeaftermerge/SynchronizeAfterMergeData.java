 /* Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Samatar HASSAN.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
*/
package org.pentaho.di.trans.steps.synchronizeaftermerge;

import java.sql.PreparedStatement;
import java.sql.Savepoint;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * Performs an insert/update/delete depending on the value of a field.
 * 
 * @author Samatar
 * @since 13-10-2008
 */
public class SynchronizeAfterMergeData extends BaseStepData implements StepDataInterface
{
	public Database db;

	public int    keynrs[];         // nr of keylookup -value in row...
	public int    keynrs2[];        // nr of keylookup2-value in row...
	public int    valuenrs[];       // Stream valuename nrs to prevent searches.
	public int indexOfTableNameField;
	
	public int indexOfOperationOrderField;

	//List<String> updateColumns = new ArrayList<String>();
	   /**
     * Mapping between the SQL and the actual prepared statement.
     * Normally this is only one, but in case we have more then one, it's convenient to have this.
     */
    public  Map<String, PreparedStatement>      preparedStatements;
    public String realTableName;
    public String realSchemaName;
    public String realSchemaTable;
    
    /** Use batch mode or not? */
    public boolean batchMode;
    
	PreparedStatement	insertStatement;
	PreparedStatement	lookupStatement;
	PreparedStatement	updateStatement;
	PreparedStatement	deleteStatement;
    
    public String insertValue;
    public String updateValue;
    public String deleteValue;
    
    public String stringErrorKeyNotFound;
    
    public String stringFieldnames;
    
    public boolean lookupFailure;
    
    public RowMetaInterface outputRowMeta;
    public RowMetaInterface inputRowMeta;
    
    public RowMetaInterface deleteParameterRowMeta;
    public RowMetaInterface  updateParameterRowMeta;
    public RowMetaInterface  lookupParameterRowMeta;
    public RowMetaInterface  lookupReturnRowMeta;
    public RowMetaInterface  insertRowMeta;
    
	public Map<String, Integer> commitCounterMap;
	public int commitSize;
	public DatabaseMeta databaseMeta;
	public boolean specialErrorHandling;
	public Savepoint savepoint;
	public boolean releaseSavepoint;
	
    public List<Object[]> batchBuffer;
	
	
	/**
	 *  Default constructor.
	 */
	public SynchronizeAfterMergeData()
	{
		super();
		insertStatement=null;
		lookupStatement=null;
		updateStatement=null;
		deleteStatement=null;
		
		indexOfTableNameField=-1;
		
		db=null;
        preparedStatements = new Hashtable<String, PreparedStatement>(); 
        realTableName=null;
		realSchemaName=null;
		batchMode=false;
		insertValue=null;
		updateValue=null;
		deleteValue=null;
		indexOfOperationOrderField=-1;
		lookupFailure=false;
		realSchemaTable=null;
        commitCounterMap = new HashMap<String, Integer>();
        batchBuffer = new ArrayList<Object[]>();
        releaseSavepoint = true;

	}
}