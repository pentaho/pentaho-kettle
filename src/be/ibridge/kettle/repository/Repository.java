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

package be.ibridge.kettle.repository;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;

import be.ibridge.kettle.core.Condition;
import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.Counter;
import be.ibridge.kettle.core.Counters;
import be.ibridge.kettle.core.Encr;
import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.XMLHandler;
import be.ibridge.kettle.core.database.Database;
import be.ibridge.kettle.core.database.DatabaseMeta;
import be.ibridge.kettle.core.exception.KettleDatabaseException;
import be.ibridge.kettle.core.exception.KettleDependencyException;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.value.Value;
import be.ibridge.kettle.job.JobMeta;
import be.ibridge.kettle.job.entry.JobEntryInterface;
import be.ibridge.kettle.trans.StepLoader;
import be.ibridge.kettle.trans.StepPlugin;
import be.ibridge.kettle.trans.TransMeta;


/**
 * 
 * This class handles interactions with a Kettle repository.
 * 
 * @author Matt
 * Created on 31-mrt-2004
 *
 */
public class Repository
{
	private RepositoryMeta		repinfo;
	public  UserInfo			userinfo;
	private RepositoryDirectory	directoryTree;
	private Database			database;

	public  LogWriter			log;

	private String				locksource;

	private PreparedStatement	psStepAttributesLookup;
	private PreparedStatement	psStepAttributesInsert;
	
	private ArrayList           stepAttributesBuffer;
	
	private PreparedStatement	pstmt_entry_attributes;

	private StepLoader			steploader;

	private int					majorVersion;
	private int					minorVersion;

    /** The maximum length of a text field in a Kettle repository : 2.000.000 is enough for everyone ;-) */ 
    private static final int REP_STRING_LENGTH      = 2000000;
    private static final int REP_STRING_CODE_LENGTH =     255;

	public Repository(LogWriter log, RepositoryMeta repinfo, UserInfo userinfo)
	{
		this.repinfo = repinfo;
		this.log = log;
		this.userinfo = userinfo;

		steploader = StepLoader.getInstance();
		
		database = new Database(repinfo.getConnection());

		psStepAttributesLookup = null;
		psStepAttributesInsert = null;
		pstmt_entry_attributes = null;

		this.majorVersion = 2;
		this.minorVersion = 1;

		directoryTree = null;
	}

	public RepositoryMeta getRepositoryInfo()
	{
		return repinfo;
	}

	public UserInfo getUserInfo()
	{
		return userinfo;
	}

	public String getName()
	{
		if (repinfo == null)
			return null;
		return repinfo.getName();
	}

	/**
	 * Return the major repository version.
	 * @return the major repository version.
	 */
	public int getMajorVersion()
	{
		return majorVersion;
	}

	/**
	 * Return the minor repository version.
	 * @return the minor repository version.
	 */
	public int getMinorVersion()
	{
		return minorVersion;
	}

	/**
	 * Get the repository version.
	 * @return The repository version as major version + "." + minor version
	 */
	public String getVersion()
	{
		return majorVersion + "." + minorVersion;
	}

    /**
     * @return The source specified at connect() time.
     */
    public String getLocksource()
    {
        return locksource;
    }
    
	/**
	 * Connect to the repository 
	 * @param locksource
	 * @return true if the connection went well, false if we couldn't connect.
	 */
	public boolean connect(String locksource)
	{
		return connect(false, true, locksource);
	}

	public boolean connect(boolean no_lookup, boolean readDirectory, String locksource)
	{
		if (repinfo.isLocked())
		{
			log.logError(toString(), "Repository is locked by class " + locksource);
			return false;
		}
		boolean retval = true;
		try
		{
			database.connect();
			setAutoCommit(false);
			repinfo.setLock(true);
			this.locksource = locksource;
			if (!no_lookup)
			{
				try
				{
					setLookupStepAttribute();
					setLookupJobEntryAttribute();
				}
				catch (KettleDatabaseException dbe)
				{
					log.logError(toString(), "Error setting lookup prep.statements: " + dbe.getMessage());
				}
			}

			// Load the directory tree.
            if (readDirectory)
            {
    			try
    			{
    				directoryTree = new RepositoryDirectory(this);
    			}
    			catch (KettleException e)
    			{
    				log.logError(toString(), "Unable to read the directory tree from the repository!" + Const.CR
    											+ e.getMessage());
    				directoryTree = new RepositoryDirectory();
    			}
            }
            else
            {
                directoryTree = new RepositoryDirectory();
            }
		}
		catch (KettleException e)
		{
			retval = false;
			log.logError(toString(), "Error connecting to the repository!" + e.getMessage());
		}

		return retval;
	}

	public void disconnect()
	{
		try
		{
			closeStepAttributeLookupPreparedStatement();
			if (!database.isAutoCommit()) commit();
			repinfo.setLock(false);
			database.disconnect();
		}
		catch (KettleException dbe)
		{
			log.logError(toString(), "Error disconnecting from database : " + dbe.getMessage());
			//return false;
		}
	}

	public void setAutoCommit(boolean autocommit)
	{
		if (!autocommit)
			database.setCommit(99999999);
		else
			database.setCommit(0);
	}

	public void commit() throws KettleException
	{
		try
		{
			if (!database.isAutoCommit()) database.commit();
			
			// Also, clear the counters, reducing the risc of collisions!
			Counters.getInstance().clear();
		}
		catch (KettleDatabaseException dbe)
		{
			throw new KettleException("Unable to commit repository connection", dbe);
		}
	}

	public void rollback()
	{
		try
		{
			database.rollback();
			
			// Also, clear the counters, reducing the risc of collisions!
			Counters.getInstance().clear();
		}
		catch (KettleDatabaseException dbe)
		{
			log.logError(toString(), "Error rolling back repository.");
		}
	}
	
	/**
     * @return Returns the stepAttributesBuffer.
     */
    public ArrayList getStepAttributesBuffer()
    {
        return stepAttributesBuffer;
    }
    
    /**
     * @param stepAttributesBuffer The stepAttributesBuffer to set.
     */
    public void setStepAttributesBuffer(ArrayList stepAttributesBuffer)
    {
        this.stepAttributesBuffer = stepAttributesBuffer;
    }
	
	public void fillStepAttributesBuffer(long id_transformation) throws KettleDatabaseException
	{
	    String sql = "SELECT ID_STEP, CODE, NR, VALUE_NUM, VALUE_STR "+
	                 "FROM R_STEP_ATTRIBUTE "+
	                 "WHERE ID_TRANSFORMATION = "+id_transformation+" "+
	                 "ORDER BY ID_STEP, CODE, NR"
	                 ;
	    
	    stepAttributesBuffer = database.getRows(sql, -1);
        
        Collections.sort(stepAttributesBuffer);  // just to make sure...
	}
	
	private Row searchStepAttributeInBuffer(long id_step, String code, long nr)
	{
	    int idx = searchStepAttributeIndexInBuffer(id_step, code, nr);
	    if (idx<0) return null;
	    
	    // Get the row and remote it from the list...
	    Row r = (Row)stepAttributesBuffer.get(idx);
	    // stepAttributesBuffer.remove(idx);
	    
	    return r;
	}
	
	
	private int searchStepAttributeIndexInBuffer(long id_step, String code, long nr)
	{
	    Row compare = new Row();
	    compare.addValue(new Value("ID_STEP", id_step));
	    compare.addValue(new Value("CODE", code));
	    compare.addValue(new Value("NR", nr));

        int index = Collections.binarySearch(stepAttributesBuffer, compare);
        if (index>=stepAttributesBuffer.size() || index<0) return -1;
        // 
        // Check this...  If it is not, we didn't find it!
        Row look = (Row)stepAttributesBuffer.get(index);
        
        if (look.compare(compare, new int[] {0,1,2}, new boolean[] { true, true, true })==0)
        {
            return index;
        }
        
        return -1;
	}

	private int searchNrStepAttributes(long id_step, String code)
	{
	    // Search the index of the first step attribute with the specified code...
	    int idx = searchStepAttributeIndexInBuffer(id_step, code, 0L);
	    if (idx<0) return 0;
	    
	    int nr = 1;
	    int offset = 1;
        
        if (idx+offset>=stepAttributesBuffer.size())
        {
            return 1; // Only 1, the last of the attributes buffer.
        }
	    Row look = (Row)stepAttributesBuffer.get(idx+offset);
	    long lookID = look.getValue(0).getInteger();
	    String lookCode = look.getValue(1).getString();
	    
	    while (lookID==id_step && code.equalsIgnoreCase( lookCode ) )
	    {
	        nr = (int)look.getValue(2).getInteger() + 1; // Find the maximum
	        offset++;
            if (idx+offset<stepAttributesBuffer.size())
            {
                look = (Row)stepAttributesBuffer.get(idx+offset);
                
                lookID = look.getValue(0).getInteger();
                lookCode = look.getValue(1).getString();
            }
            else
            {
                return nr;
            }
	    }
	    return nr;
	}

	/////////////////////////////////////////////////////////////////////////////////////
	// LOOKUP ID
	/////////////////////////////////////////////////////////////////////////////////////

	public long getJobID(String name, long id_directory) throws KettleDatabaseException
	{
		return getIDWithValue("R_JOB", "ID_JOB", "NAME", name, "ID_DIRECTORY", id_directory);
	}

	public long getTransformationID(String name, long id_directory) throws KettleDatabaseException
	{
		return getIDWithValue("R_TRANSFORMATION", "ID_TRANSFORMATION", "NAME", name, "ID_DIRECTORY", id_directory);
	}

	public long getNoteID(String note) throws KettleDatabaseException
	{
		return getIDWithValue("R_NOTE", "ID_NOTE", "VALUE_STR", note);
	}

	public long getDatabaseID(String name) throws KettleDatabaseException
	{
		return getIDWithValue("R_DATABASE", "ID_DATABASE", "NAME", name);
	}

	public long getDatabaseTypeID(String code) throws KettleDatabaseException
	{
		return getIDWithValue("R_DATABASE_TYPE", "ID_DATABASE_TYPE", "CODE", code);
	}

	public long getDatabaseConTypeID(String code) throws KettleDatabaseException
	{
		return getIDWithValue("R_DATABASE_CONTYPE", "ID_DATABASE_CONTYPE", "CODE", code);
	}

	public long getStepTypeID(String code) throws KettleDatabaseException
	{
		return getIDWithValue("R_STEP_TYPE", "ID_STEP_TYPE", "CODE", code);
	}

	public long getJobEntryID(String name, long id_job) throws KettleDatabaseException
	{
		return getIDWithValue("R_JOBENTRY", "ID_JOBENTRY", "NAME", name, "ID_JOB", id_job);
	}

	public long getJobEntryTypeID(String code) throws KettleDatabaseException
	{
		return getIDWithValue("R_JOBENTRY_TYPE", "ID_JOBENTRY_TYPE", "CODE", code);
	}

	public long getStepID(String name, long id_transformation) throws KettleDatabaseException
	{
		return getIDWithValue("R_STEP", "ID_STEP", "NAME", name, "ID_TRANSFORMATION", id_transformation);
	}

	public long getUserID(String login) throws KettleDatabaseException
	{
		return getIDWithValue("R_USER", "ID_USER", "LOGIN", login);
	}

	public long getProfileID(String profilename) throws KettleDatabaseException
	{
		return getIDWithValue("R_PROFILE", "ID_PROFILE", "NAME", profilename);
	}

	public long getPermissionID(String code) throws KettleDatabaseException
	{
		return getIDWithValue("R_PERMISSION", "ID_PERMISSION", "CODE", code);
	}

	public long getTransHopID(long id_transformation, long id_step_from, long id_step_to)
			throws KettleDatabaseException
	{
		String lookupkey[] = new String[] { "ID_TRANSFORMATION", "ID_STEP_FROM", "ID_STEP_TO" };
		long key[] = new long[] { id_transformation, id_step_from, id_step_to };

		return getIDWithValue("R_TRANS_HOP", "ID_TRANS_HOP", lookupkey, key);
	}

	public long getJobHopID(long id_job, long id_jobentry_copy_from, long id_jobentry_copy_to)
			throws KettleDatabaseException
	{
		String lookupkey[] = new String[] { "ID_JOB", "ID_JOBENTRY_COPY_FROM", "ID_JOBENTRY_COPY_TO" };
		long key[] = new long[] { id_job, id_jobentry_copy_from, id_jobentry_copy_to };

		return getIDWithValue("R_JOB_HOP", "ID_JOB_HOP", lookupkey, key);
	}

	public long getDependencyID(long id_transformation, long id_database, String tablename)
			throws KettleDatabaseException
	{
		String lookupkey[] = new String[] { "ID_TRANSFORMATION", "ID_DATABASE" };
		long key[] = new long[] { id_transformation, id_database };

		return getIDWithValue("R_DEPENDENCY", "ID_DEPENDENCY", "TABLE_NAME", tablename, lookupkey, key);
	}

	public long getRootDirectoryID() throws KettleDatabaseException
	{
		Row result = database.getOneRow("SELECT ID_DIRECTORY FROM R_DIRECTORY WHERE ID_DIRECTORY_PARENT = 0");
		if (result != null && result.getValue(0).isNumeric())
			return result.getValue(0).getInteger();
		return -1;
	}

	public int getNrSubDirectories(long id_directory) throws KettleDatabaseException
	{
		int retval = 0;

		String sql = "SELECT COUNT(*) FROM R_DIRECTORY WHERE ID_DIRECTORY_PARENT = " + id_directory;
		Row r = database.getOneRow(sql);
		if (r != null)
		{
			retval = (int) r.getValue(0).getInteger();
		}

		return retval;
	}

	public long[] getSubDirectoryIDs(long id_directory) throws KettleDatabaseException
	{
		int nr = getNrSubDirectories(id_directory);
		long retval[] = new long[nr];

		String sql = "SELECT ID_DIRECTORY FROM R_DIRECTORY WHERE ID_DIRECTORY_PARENT = " + id_directory+" ORDER BY DIRECTORY_NAME";

		ResultSet rs = database.openQuery(sql);
		Row r = database.getRow(rs);
		int i = 0;
		while (r != null)
		{
			retval[i] = r.getValue(0).getInteger();
			r = database.getRow(rs);
			i++;
		}
		database.closeQuery(rs);

		return retval;
	}

	private long getIDWithValue(String tablename, String idfield, String lookupfield, String value)
			throws KettleDatabaseException
	{
		Row par = new Row();
		par.addValue(new Value("value", value));
		Row result = database.getOneRow("SELECT " + idfield + " FROM " + tablename + " WHERE " + lookupfield + " = ?", par);
		if (result != null && result.getValue(0).isNumeric())
			return result.getValue(0).getInteger();
		return -1;
	}

	private long getIDWithValue(String tablename, String idfield, String lookupfield, String value, String lookupkey,
			long key) throws KettleDatabaseException
	{
		Row par = new Row();
		par.addValue(new Value("value", value));
		par.addValue(new Value("key", key));
		Row result = database.getOneRow("SELECT " + idfield + " FROM " + tablename + " WHERE " + lookupfield + " = ? AND "
									+ lookupkey + " = ?", par);
		if (result != null && result.getValue(0).isNumeric())
			return result.getValue(0).getInteger();
		return -1;
	}

	private long getIDWithValue(String tablename, String idfield, String lookupkey[], long key[])
			throws KettleDatabaseException
	{
		Row par = new Row();
		String sql = "SELECT " + idfield + " FROM " + tablename + " ";
		for (int i = 0; i < lookupkey.length; i++)
		{
			if (i == 0)
				sql += "WHERE ";
			else
				sql += "AND   ";
			par.addValue(new Value(lookupkey[i], key[i]));
			sql += lookupkey[i] + " = ? ";
		}
		Row result = database.getOneRow(sql, par);
		if (result != null && result.getValue(0).isNumeric())
			return result.getValue(0).getInteger();
		return -1;
	}

	private long getIDWithValue(String tablename, String idfield, String lookupfield, String value, String lookupkey[],
			long key[]) throws KettleDatabaseException
	{
		Row par = new Row();
		par.addValue(new Value(lookupfield, value));
		String sql = "SELECT " + idfield + " FROM " + tablename + " WHERE " + lookupfield + " = ? ";
		for (int i = 0; i < lookupkey.length; i++)
		{
			par.addValue(new Value(lookupkey[i], key[i]));
			sql += "AND " + lookupkey[i] + " = ? ";
		}

		Row result = database.getOneRow(sql, par);
		if (result != null && result.getValue(0).isNumeric())
			return result.getValue(0).getInteger();
		return -1;
	}

	public String getDatabaseTypeCode(long id_database_type) throws KettleDatabaseException
	{
		return getStringWithID("R_DATABASE_TYPE", "ID_DATABASE_TYPE", id_database_type, "CODE");
	}

	public String getDatabaseConTypeCode(long id_database_contype) throws KettleDatabaseException
	{
		return getStringWithID("R_DATABASE_CONTYPE", "ID_DATABASE_CONTYPE", id_database_contype, "CODE");
	}

	public String getStepTypeCode(long id_database_type) throws KettleDatabaseException
	{
		return getStringWithID("R_STEP_TYPE", "ID_STEP_TYPE", id_database_type, "CODE");
	}

	private String getStringWithID(String tablename, String keyfield, long id, String fieldname)
			throws KettleDatabaseException
	{
		String sql = "SELECT " + fieldname + " FROM " + tablename + " WHERE " + keyfield + " = ?";
		Row par = new Row();
		par.addValue(new Value(keyfield, id));
		Row result = database.getOneRow(sql, par);
		if (result != null)
		{
			return result.getValue(0).getString();
		}
		return null;
	}

	/////////////////////////////////////////////////////////////////////////////////////
	// DIRECTORIES
	/////////////////////////////////////////////////////////////////////////////////////

	public void moveTransformation(String transname, long id_directory_from, long id_directory_to)
			throws KettleDatabaseException
	{
		String sql = "UPDATE R_TRANSFORMATION SET ID_DIRECTORY = ? WHERE NAME = ? AND ID_DIRECTORY = ?";

		Row par = new Row();
		par.addValue(new Value("ID_DIRECTORY", id_directory_to));
		par.addValue(new Value("NAME", transname));
		par.addValue(new Value("ID_DIRECTORY", id_directory_from));

		database.execStatement(sql, par);
	}

	public void moveJob(String jobname, long id_directory_from, long id_directory_to) throws KettleDatabaseException
	{
		String sql = "UPDATE R_JOB SET ID_DIRECTORY = ? WHERE NAME = ? AND ID_DIRECTORY = ?";

		Row par = new Row();
		par.addValue(new Value("ID_DIRECTORY", id_directory_to));
		par.addValue(new Value("NAME", jobname));
		par.addValue(new Value("ID_DIRECTORY", id_directory_from));

		database.execStatement(sql, par);
	}

	/////////////////////////////////////////////////////////////////////////////////////
	// GET NEW IDS
	/////////////////////////////////////////////////////////////////////////////////////

	public long getNextTransformationID() throws KettleDatabaseException
	{
		return getNextID("R_TRANSFORMATION", "ID_TRANSFORMATION");
	}

	public long getNextJobID() throws KettleDatabaseException
	{
		return getNextID("R_JOB", "ID_JOB");
	}

	public long getNextNoteID() throws KettleDatabaseException
	{
		return getNextID("R_NOTE", "ID_NOTE");
	}

	public long getNextDatabaseID() throws KettleDatabaseException
	{
		return getNextID("R_DATABASE", "ID_DATABASE");
	}

	public long getNextDatabaseTypeID() throws KettleDatabaseException
	{
		return getNextID("R_DATABASE_TYPE", "ID_DATABASE_TYPE");
	}

	public long getNextDatabaseConnectionTypeID() throws KettleDatabaseException
	{
		return getNextID("R_DATABASE_CONTYPE", "ID_DATABASE_CONTYPE");
	}

	public long getNextLoglevelID() throws KettleDatabaseException
	{
		return getNextID("R_LOGLEVEL", "ID_LOGLEVEL");
	}

	public long getNextStepTypeID() throws KettleDatabaseException
	{
		return getNextID("R_STEP_TYPE", "ID_STEP_TYPE");
	}

	public long getNextStepID() throws KettleDatabaseException
	{
		return getNextID("R_STEP", "ID_STEP");
	}

	public long getNextJobEntryID() throws KettleDatabaseException
	{
		return getNextID("R_JOBENTRY", "ID_JOBENTRY");
	}

	public long getNextJobEntryTypeID() throws KettleDatabaseException
	{
		return getNextID("R_JOBENTRY_TYPE", "ID_JOBENTRY_TYPE");
	}

	public long getNextJobEntryCopyID() throws KettleDatabaseException
	{
		return getNextID("R_JOBENTRY_COPY", "ID_JOBENTRY_COPY");
	}

	public long getNextStepAttributeID() throws KettleDatabaseException
	{
		return getNextID("R_STEP_ATTRIBUTE", "ID_STEP_ATTRIBUTE");
	}
    
    public long getNextDatabaseAttributeID() throws KettleDatabaseException
    {
        return getNextID("R_DATABASE_ATTRIBUTE", "ID_DATABASE_ATTRIBUTE");
    }

	public long getNextTransHopID() throws KettleDatabaseException
	{
		return getNextID("R_TRANS_HOP", "ID_TRANS_HOP");
	}

	public long getNextJobHopID() throws KettleDatabaseException
	{
		return getNextID("R_JOB_HOP", "ID_JOB_HOP");
	}

	public long getNextDepencencyID() throws KettleDatabaseException
	{
		return getNextID("R_DEPENDENCY", "ID_DEPENDENCY");
	}

	public long getNextConditionID() throws KettleDatabaseException
	{
		return getNextID("R_CONDITION", "ID_CONDITION");
	}

	public long getNextValueID() throws KettleDatabaseException
	{
		return getNextID("R_VALUE", "ID_VALUE");
	}

	public long getNextUserID() throws KettleDatabaseException
	{
		return getNextID("R_USER", "ID_USER");
	}

	public long getNextProfileID() throws KettleDatabaseException
	{
		return getNextID("R_PROFILE", "ID_PROFILE");
	}

	public long getNextPermissionID() throws KettleDatabaseException
	{
		return getNextID("R_PERMISSION", "ID_PERMISSION");
	}

	public long getNextJobEntryAttributeID() throws KettleDatabaseException
	{
	    return getNextID("R_JOBENTRY_ATTRIBUTE", "ID_JOBENTRY_ATTRIBUTE");
	}
	
	public long getNextID(String tableName, String fieldName) throws KettleDatabaseException
	{
	    String counterName = tableName+"."+fieldName;
	    Counter counter = Counters.getInstance().getCounter(counterName);
	    if (counter==null)
	    {
	        long id = getNextTableID(tableName, fieldName);
	        counter = new Counter(id);
	        Counters.getInstance().setCounter(counterName, counter);
	        return counter.next();
	    }
	    else
	    {
	        return counter.next();
	    }
	}
    
    public void clearNextIDCounters()
    {
        Counters.getInstance().clear();
    }

	public long getNextDirectoryID() throws KettleDatabaseException
	{
		return getNextID("R_DIRECTORY", "ID_DIRECTORY");
	}

	private long getNextTableID(String tablename, String idfield) throws KettleDatabaseException
	{
		long retval = -1;
		Row r = database.getOneRow("SELECT max(" + idfield + ") FROM " + tablename);
		if (r != null)
		{
			Value id = r.getValue(0);
			
			// log.logBasic(toString(), "result row for "+idfield+" is : "+r.toString()+", id = "+id.toString()+" int="+id.getInteger()+" num="+id.getNumber());
			if (id.isNull())
			{
				log.logDebug(toString(), "no max(" + idfield + ") found in table " + tablename);
				retval = 1;
			}
			else
			{
				log.logDebug(toString(), "max(" + idfield + ") found in table " + tablename + " --> " + id.getInteger()
											+ " number: " + id.getNumber());
				retval = id.getInteger() + 10000L;
				
				// log.logBasic(toString(), "Got next id for "+tablename+"."+idfield+" from the database: "+retval);
			}
		}
		return retval;
	}

	/////////////////////////////////////////////////////////////////////////////////////
	// INSERT VALUES
	/////////////////////////////////////////////////////////////////////////////////////

	public void insertTransformation(long id_transformation, String name, long id_step_read, long id_step_write,
			long id_step_input, long id_step_output, long id_step_update, long id_database_log, String table_name_log,
			boolean use_batchid, boolean use_logfield, long id_database_maxdate, String table_name_maxdate,
			String field_name_maxdate, double offset_maxdate, double diff_maxdate, String modified_user,
			Value modified_date, int size_rowset, long id_directory) throws KettleDatabaseException
	{
		Row table = new Row();

		table.addValue(new Value("ID_TRANSFORMATION", id_transformation));
		table.addValue(new Value("NAME", name));
		table.addValue(new Value("ID_STEP_READ", id_step_read));
		table.addValue(new Value("ID_STEP_WRITE", id_step_write));
		table.addValue(new Value("ID_STEP_INPUT", id_step_input));
		table.addValue(new Value("ID_STEP_OUTPUT", id_step_output));
		table.addValue(new Value("ID_STEP_UPDATE", id_step_update));
		table.addValue(new Value("ID_DATABASE_LOG", id_database_log));
		table.addValue(new Value("TABLE_NAME_LOG", table_name_log));
		table.addValue(new Value("USE_BATCHID", use_batchid));
		table.addValue(new Value("USE_LOGFIELD", use_logfield));
		table.addValue(new Value("ID_DATABASE_MAXDATE", id_database_maxdate));
		table.addValue(new Value("TABLE_NAME_MAXDATE", table_name_maxdate));
		table.addValue(new Value("FIELD_NAME_MAXDATE", field_name_maxdate));
		table.addValue(new Value("OFFSET_MAXDATE", offset_maxdate));
		table.addValue(new Value("DIFF_MAXDATE", diff_maxdate));
		table.addValue(new Value("MODIFIED_USER", modified_user));
		table.addValue(new Value("MODIFIED_DATE", modified_date));
		table.addValue(new Value("SIZE_ROWSET", (long) size_rowset));
		table.addValue(new Value("ID_DIRECTORY", id_directory));

		database.prepareInsert(table, "R_TRANSFORMATION");
		database.setValuesInsert(table);
		database.insertRow();
		database.closeInsert();
		
		// Save the logging connection link...
		if (id_database_log>0) insertStepDatabase(id_transformation, -1L, id_database_log);

		// Save the maxdate connection link...
		if (id_database_maxdate>0) insertStepDatabase(id_transformation, -1L, id_database_maxdate);
	}

	public void insertJob(long id_job, long id_directory, String name, long id_database_log, String table_name_log,
			String modified_user, Value modified_date) throws KettleDatabaseException
	{
		Row table = new Row();

		table.addValue(new Value("ID_JOB", id_job));
		table.addValue(new Value("ID_DIRECTORY", id_directory));
		table.addValue(new Value("NAME", name));
		table.addValue(new Value("ID_DATABASE_LOG", id_database_log));
		table.addValue(new Value("TABLE_NAME_LOG", table_name_log));
		table.addValue(new Value("MODIFIED_USER", modified_user));
		table.addValue(new Value("MODIFIED_DATE", modified_date));

		database.prepareInsert(table, "R_JOB");
		database.setValuesInsert(table);
		database.insertRow();
		log.logDebug(toString(), "Inserted new record into table R_JOB with data : " + table);
		database.closeInsert();
	}

	public long insertNote(String note, long gui_location_x, long gui_location_y, long gui_location_width,
			long gui_location_height) throws KettleDatabaseException
	{
		long id = getNextNoteID();

		Row table = new Row();

		table.addValue(new Value("ID_NOTE", id));
		table.addValue(new Value("VALUE_STR", note));
		table.addValue(new Value("GUI_LOCATION_X", gui_location_x));
		table.addValue(new Value("GUI_LOCATION_Y", gui_location_y));
		table.addValue(new Value("GUI_LOCATION_WIDTH", gui_location_width));
		table.addValue(new Value("GUI_LOCATION_HEIGHT", gui_location_height));

		database.prepareInsert(table, "R_NOTE");
		database.setValuesInsert(table);
		database.insertRow();
		database.closeInsert();

		return id;
	}

	public void insertTransNote(long id_transformation, long id_note) throws KettleDatabaseException
	{
		Row table = new Row();

		table.addValue(new Value("ID_TRANSFORMATION", id_transformation));
		table.addValue(new Value("ID_NOTE", id_note));

		database.prepareInsert(table, "R_TRANS_NOTE");
		database.setValuesInsert(table);
		database.insertRow();
		database.closeInsert();
	}

	public void insertJobNote(long id_job, long id_note) throws KettleDatabaseException
	{
		Row table = new Row();

		table.addValue(new Value("ID_JOB", id_job));
		table.addValue(new Value("ID_NOTE", id_note));

		database.prepareInsert(table, "R_JOB_NOTE");
		database.setValuesInsert(table);
		database.insertRow();
		database.closeInsert();
	}

	public long insertDatabase(String name, String type, String access, String host, String dbname, long port,
			String user, String pass, String servername, String data_tablespace, String index_tablespace)
			throws KettleDatabaseException
	{

		long id = getNextDatabaseID();

		long id_database_type = getDatabaseTypeID(type);
		if (id_database_type < 0) // New support database type: add it!
		{
			id_database_type = getNextDatabaseTypeID();

			String tablename = "R_DATABASE_TYPE";
			Row table = new Row();
			table.addValue(new Value("ID_DATABASE_TYPE", Value.VALUE_TYPE_INTEGER, 5, 0));
			table.addValue(new Value("CODE", Value.VALUE_TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
			table.addValue(new Value("DESCRIPTION", Value.VALUE_TYPE_STRING, REP_STRING_LENGTH, 0));

			database.prepareInsert(table, tablename);

			table = new Row();
			table.addValue(new Value("ID_DATABASE_TYPE", id_database_type));
			table.addValue(new Value("CODE", type));
			table.addValue(new Value("DESCRIPTION", type));

			database.setValuesInsert(table);
			database.insertRow();
			database.closeInsert();
		}

		long id_database_contype = getDatabaseConTypeID(access);

		Row table = new Row();
		table.addValue(new Value("ID_DATABASE", id));
		table.addValue(new Value("NAME", name));
		table.addValue(new Value("ID_DATABASE_TYPE", id_database_type));
		table.addValue(new Value("ID_DATABASE_CONTYPE", id_database_contype));
		table.addValue(new Value("HOST_NAME", host));
		table.addValue(new Value("DATABASE_NAME", dbname));
		table.addValue(new Value("PORT", port));
		table.addValue(new Value("USERNAME", user));
		table.addValue(new Value("PASSWORD", "Encrypted " + Encr.encryptPassword(pass)));
		table.addValue(new Value("SERVERNAME", servername));
		table.addValue(new Value("DATA_TBS", data_tablespace));
		table.addValue(new Value("INDEX_TBS", index_tablespace));

		database.prepareInsert(table, "R_DATABASE");
		database.setValuesInsert(table);
		database.insertRow();
		database.closeInsert();

		return id;
	}

	public long insertStep(long id_transformation, String name, String description, String steptype,
			boolean distribute, long copies, long gui_location_x, long gui_location_y, boolean gui_draw)
			throws KettleDatabaseException
	{
		long id = getNextStepID();

		long id_step_type = getStepTypeID(steptype);

		Row table = new Row();

		table.addValue(new Value("ID_STEP", id));
		table.addValue(new Value("ID_TRANSFORMATION", id_transformation));
		table.addValue(new Value("NAME", name));
		table.addValue(new Value("DESCRIPTION", description));
		table.addValue(new Value("ID_STEP_TYPE", id_step_type));
		table.addValue(new Value("DISTRIBUTE", distribute));
		table.addValue(new Value("COPIES", copies));
		table.addValue(new Value("GUI_LOCATION_X", gui_location_x));
		table.addValue(new Value("GUI_LOCATION_Y", gui_location_y));
		table.addValue(new Value("GUI_DRAW", gui_draw));

		database.prepareInsert(table, "R_STEP");
		database.setValuesInsert(table);
		database.insertRow();
		database.closeInsert();

		return id;
	}

	public long insertStepAttribute(long id_transformation, long id_step, long nr, String code, double value_num,
			String value_str) throws KettleDatabaseException
	{
		long id = getNextStepAttributeID();

		Row table = new Row();

		table.addValue(new Value("ID_STEP_ATTRIBUTE", id));
		table.addValue(new Value("ID_TRANSFORMATION", id_transformation));
		table.addValue(new Value("ID_STEP", id_step));
		table.addValue(new Value("NR", nr));
		table.addValue(new Value("CODE", code));
		table.addValue(new Value("VALUE_NUM", value_num));
		table.addValue(new Value("VALUE_STR", value_str));

		/* If we have prepared the insert, we don't do it again.
		 * We asume that all the step insert statements come one after the other.
		 */
		
		if (psStepAttributesInsert == null)
		{
		    String sql = database.getInsertStatement("R_STEP_ATTRIBUTE", table);
		    psStepAttributesInsert = database.prepareSQL(sql);
		}
		database.setValues(table, psStepAttributesInsert);
		database.insertRow(psStepAttributesInsert, true);
		
		/*
		database.prepareInsert(table, "R_STEP_ATTRIBUTE");
		database.setValuesInsert(table);
		database.insertRow();
		database.closeInsert();
		*/
		
		log.logDebug(toString(), "saved attribute ["+code+"]");
		
		return id;
	}

	public void insertStepDatabase(long id_transformation, long id_step, long id_database)
			throws KettleDatabaseException
	{
		// First check if the relationship is already there.
		// There is no need to store it twice!
		Row check = getStepDatabase(id_step);
		if (check == null)
		{
			Row table = new Row();

			table.addValue(new Value("ID_TRANSFORMATION", id_transformation));
			table.addValue(new Value("ID_STEP", id_step));
			table.addValue(new Value("ID_DATABASE", id_database));

			database.insertRow("R_STEP_DATABASE", table);
		}
	}
	
    public long insertDatabaseAttribute(long id_database, String code, String value_str) throws KettleDatabaseException
    {
        long id = getNextDatabaseAttributeID();

        Row table = new Row();

        table.addValue(new Value("ID_DATABASE_ATTRIBUTE", id));
        table.addValue(new Value("ID_DATABASE", id_database));
        table.addValue(new Value("CODE", code));
        table.addValue(new Value("VALUE_STR", value_str));

        /* If we have prepared the insert, we don't do it again.
         * We asume that all the step insert statements come one after the other.
         */
        database.prepareInsert(table, "R_DATABASE_ATTRIBUTE");
        database.setValuesInsert(table);
        database.insertRow();
        database.closeInsert();
        
        log.logDebug(toString(), "saved database attribute ["+code+"]");
        
        return id;
    }

	
	public long insertJobEntryAttribute(long id_job, long id_jobentry, long nr, String code, double value_num,
			String value_str) throws KettleDatabaseException
	{
		long id = getNextJobEntryAttributeID();

		Row table = new Row();

		table.addValue(new Value("ID_JOBENTRY_ATTRIBUTE", id));
		table.addValue(new Value("ID_JOB", id_job));
		table.addValue(new Value("ID_JOBENTRY", id_jobentry));
		table.addValue(new Value("NR", nr));
		table.addValue(new Value("CODE", code));
		table.addValue(new Value("VALUE_NUM", value_num));
		table.addValue(new Value("VALUE_STR", value_str));

		database.prepareInsert(table, "R_JOBENTRY_ATTRIBUTE");
		database.setValuesInsert(table);
		database.insertRow();
		database.closeInsert();

		return id;
	}

	public long insertTransHop(long id_transformation, long id_step_from, long id_step_to, boolean enabled)
			throws KettleDatabaseException
	{
		long id = getNextTransHopID();

		Row table = new Row();

		table.addValue(new Value("ID_TRANS_HOP", id));
		table.addValue(new Value("ID_TRANSFORMATION", id_transformation));
		table.addValue(new Value("ID_STEP_FROM", id_step_from));
		table.addValue(new Value("ID_STEP_TO", id_step_to));
		table.addValue(new Value("ENABLED", enabled));

		database.prepareInsert(table, "R_TRANS_HOP");
		database.setValuesInsert(table);
		database.insertRow();
		database.closeInsert();

		return id;
	}

	public long insertJobHop(long id_job, long id_jobentry_copy_from, long id_jobentry_copy_to, boolean enabled,
			boolean evaluation, boolean unconditional) throws KettleDatabaseException
	{
		long id = getNextJobHopID();

		Row table = new Row();

		table.addValue(new Value("ID_JOB_HOP", id));
		table.addValue(new Value("ID_JOB", id_job));
		table.addValue(new Value("ID_JOBENTRY_COPY_FROM", id_jobentry_copy_from));
		table.addValue(new Value("ID_JOBENTRY_COPY_TO", id_jobentry_copy_to));
		table.addValue(new Value("ENABLED", enabled));
		table.addValue(new Value("EVALUATION", evaluation));
		table.addValue(new Value("UNCONDITIONAL", unconditional));

		database.prepareInsert(table, "R_JOB_HOP");
		database.setValuesInsert(table);
		database.insertRow();
		database.closeInsert();

		return id;
	}

	public long insertDependency(long id_transformation, long id_database, String tablename, String fieldname)
			throws KettleDatabaseException
	{
		long id = getNextDepencencyID();

		Row table = new Row();

		table.addValue(new Value("ID_DEPENDENCY", id));
		table.addValue(new Value("ID_TRANSFORMATION", id_transformation));
		table.addValue(new Value("ID_DATABASE", id_database));
		table.addValue(new Value("TABLE_NAME", tablename));
		table.addValue(new Value("FIELD_NAME", fieldname));

		database.prepareInsert(table, "R_DEPENDENCY");
		database.setValuesInsert(table);
		database.insertRow();
		database.closeInsert();

		return id;
	}

	public long insertCondition(long id_condition_parent, Condition condition) throws KettleDatabaseException
	{
		long id = getNextConditionID();

		String tablename = "R_CONDITION";
		Row table = new Row();
		table.addValue(new Value("ID_CONDITION", id));
		table.addValue(new Value("ID_CONDITION_PARENT", id_condition_parent));
		table.addValue(new Value("NEGATED", condition.isNegated()));
		table.addValue(new Value("OPERATOR", condition.getOperatorDesc()));
		table.addValue(new Value("LEFT_NAME", condition.getLeftValuename()));
		table.addValue(new Value("CONDITION_FUNCTION", condition.getFunctionDesc()));
		table.addValue(new Value("RIGHT_NAME", condition.getRightValuename()));

		long id_value = -1L;
		Value v = condition.getRightExact();

		if (v != null)
		{
			id_value = insertValue(v.getName(), v.getTypeDesc(), v.getString(), v.isNull(), condition.getRightExactID());
			condition.setRightExactID(id_value);
		}
		table.addValue(new Value("ID_VALUE_RIGHT", id_value));

		database.prepareInsert(table, tablename);

		database.setValuesInsert(table);
		database.insertRow();
		database.closeInsert();

		return id;
	}

	public void insertTransStepCondition(long id_transformation, long id_step, long id_condition)
			throws KettleDatabaseException
	{
		String tablename = "R_TRANS_STEP_CONDITION";
		Row table = new Row();
		table.addValue(new Value("ID_TRANSFORMATION", id_transformation));
		table.addValue(new Value("ID_STEP", id_step));
		table.addValue(new Value("ID_CONDITION", id_condition));

		database.prepareInsert(table, tablename);
		database.setValuesInsert(table);
		database.insertRow();
		database.closeInsert();
	}

	public long insertDirectory(long id_directory_parent, RepositoryDirectory dir) throws KettleDatabaseException
	{
		long id = getNextDirectoryID();

		String tablename = "R_DIRECTORY";
		Row table = new Row();
		table.addValue(new Value("ID_DIRECTORY", id));
		table.addValue(new Value("ID_DIRECTORY_PARENT", id_directory_parent));
		table.addValue(new Value("DIRECTORY_NAME", dir.getDirectoryName()));

		database.prepareInsert(table, tablename);

		database.setValuesInsert(table);
		database.insertRow();
		database.closeInsert();

		return id;
	}

	public void deleteDirectory(long id_directory) throws KettleDatabaseException
	{
		String sql = "DELETE FROM R_DIRECTORY WHERE ID_DIRECTORY = " + id_directory;
		database.execStatement(sql);
	}

	public void renameDirectory(long id_directory, String name) throws KettleDatabaseException
	{
		Row r = new Row();
		r.addValue(new Value("DIRECTORY_NAME", name));

		String sql = "UPDATE R_DIRECTORY SET DIRECTORY_NAME = ? WHERE ID_DIRECTORY = " + id_directory;

		log.logBasic(toString(), "sql = [" + sql + "]");
		log.logBasic(toString(), "row = [" + r + "]");

		database.execStatement(sql, r);
	}

	public long lookupValue(String name, String type, String value_str, boolean isnull) throws KettleDatabaseException
	{
		String tablename = "R_VALUE";
		Row table = new Row();
		table.addValue(new Value("NAME", name));
		table.addValue(new Value("VALUE_TYPE", type));
		table.addValue(new Value("VALUE_STR", value_str));
		table.addValue(new Value("IS_NULL", isnull));

		String sql = "SELECT ID_VALUE FROM " + tablename + " ";
		sql += "WHERE NAME       = ? ";
		sql += "AND   VALUE_TYPE = ? ";
		sql += "AND   VALUE_STR  = ? ";
		sql += "AND   IS_NULL    = ? ";

		Row result = database.getOneRow(sql, table);
		if (result != null && result.getValue(0).isNumeric())
			return result.getValue(0).getInteger();
		else
			return -1;
	}

	public long insertValue(String name, String type, String value_str, boolean isnull, long id_value_prev)
			throws KettleDatabaseException
	{
		long id_value = lookupValue(name, type, value_str, isnull);
		// if it didn't exist yet: insert it!!

		if (id_value < 0)
		{
			id_value = getNextValueID();

			// Let's see if the same value is not yet available?
			String tablename = "R_VALUE";
			Row table = new Row();
			table.addValue(new Value("ID_VALUE", id_value));
			table.addValue(new Value("NAME", name));
			table.addValue(new Value("VALUE_TYPE", type));
			table.addValue(new Value("VALUE_STR", value_str));
			table.addValue(new Value("IS_NULL", isnull));

			database.prepareInsert(table, tablename);
			database.setValuesInsert(table);
			database.insertRow();
			database.closeInsert();
		}

		return id_value;
	}

	public long insertJobEntry(long id_job, String name, String description, String jobentrytype)
			throws KettleDatabaseException
	{
		long id = getNextJobEntryID();

		long id_jobentry_type = getJobEntryTypeID(jobentrytype);

		log.logDebug(toString(), "ID_JobEntry_type = " + id_jobentry_type + " for type = [" + jobentrytype + "]");

		Row table = new Row();

		table.addValue(new Value("ID_JOBENTRY", id));
		table.addValue(new Value("ID_JOB", id_job));
		table.addValue(new Value("ID_JOBENTRY_TYPE", id_jobentry_type));
		table.addValue(new Value("NAME", name));
		table.addValue(new Value("DESCRIPTION", description));

		database.prepareInsert(table, "R_JOBENTRY");
		database.setValuesInsert(table);
		database.insertRow();
		database.closeInsert();

		return id;
	}

	public long insertJobEntryCopy(long id_job, long id_jobentry, long id_jobentry_type, int nr, long gui_location_x,
			long gui_location_y, boolean gui_draw, boolean parallel) throws KettleDatabaseException
	{
		long id = getNextJobEntryCopyID();

		Row table = new Row();

		table.addValue(new Value("ID_JOBENTRY_COPY", id));
		table.addValue(new Value("ID_JOBENTRY", id_jobentry));
		table.addValue(new Value("ID_JOB", id_job));
		table.addValue(new Value("ID_JOBENTRY_TYPE", id_jobentry_type));
		table.addValue(new Value("NR", (long) nr));
		table.addValue(new Value("GUI_LOCATION_X", gui_location_x));
		table.addValue(new Value("GUI_LOCATION_Y", gui_location_y));
		table.addValue(new Value("GUI_DRAW", gui_draw));
		table.addValue(new Value("PARALLEL", parallel));

		database.prepareInsert(table, "R_JOBENTRY_COPY");
		database.setValuesInsert(table);
		database.insertRow();
		database.closeInsert();

		return id;
	}

	public void insertTableRow(String tablename, Row values) throws KettleDatabaseException
	{
		database.prepareInsert(values, tablename);
		database.setValuesInsert(values);
		database.insertRow();
		database.closeInsert();
	}

	/////////////////////////////////////////////////////////////////////////////////////
	// UPDATE VALUES
	/////////////////////////////////////////////////////////////////////////////////////

	public void updateDatabase(long id_database, String name, String type, String access, String host, String dbname,
			long port, String user, String pass, String servername, String data_tablespace, String index_tablespace)
			throws KettleDatabaseException
	{
		long id_database_type = getDatabaseTypeID(type);
		long id_database_contype = getDatabaseConTypeID(access);

		Row table = new Row();
		table.addValue(new Value("NAME", name));
		table.addValue(new Value("ID_DATABASE_TYPE", id_database_type));
		table.addValue(new Value("ID_DATABASE_CONTYPE", id_database_contype));
		table.addValue(new Value("HOST_NAME", host));
		table.addValue(new Value("DATABASE_NAME", dbname));
		table.addValue(new Value("PORT", port));
		table.addValue(new Value("USERNAME", user));
		table.addValue(new Value("PASSWORD", "Encrypted " + Encr.encryptPassword(pass)));
		table.addValue(new Value("SERVERNAME", servername));
		table.addValue(new Value("DATA_TBS", data_tablespace));
		table.addValue(new Value("INDEX_TBS", index_tablespace));

		updateTableRow("R_DATABASE", "ID_DATABASE", table, id_database);
	}

	public void updateTableRow(String tablename, String idfield, Row values, long id) throws KettleDatabaseException
	{
		String sets[] = new String[values.size()];
		for (int i = 0; i < values.size(); i++)
			sets[i] = values.getValue(i).getName();
		String codes[] = new String[] { idfield };
		String condition[] = new String[] { "=" };

		database.prepareUpdate(tablename, codes, condition, sets);

		values.addValue(new Value(idfield, id));

		database.setValuesUpdate(values);
		database.updateRow();
		database.closeUpdate();
	}

	public void updateTableRow(String tablename, String idfield, Row values) throws KettleDatabaseException
	{
		long id = values.searchValue(idfield).getInteger();
		values.removeValue(idfield);
		String sets[] = new String[values.size()];
		for (int i = 0; i < values.size(); i++)
			sets[i] = values.getValue(i).getName();
		String codes[] = new String[] { idfield };
		String condition[] = new String[] { "=" };

		database.prepareUpdate(tablename, codes, condition, sets);

		values.addValue(new Value(idfield, id));

		database.setValuesUpdate(values);
		database.updateRow();
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	// READ DATA FROM REPOSITORY
	//////////////////////////////////////////////////////////////////////////////////////////

	public int getNrJobs() throws KettleDatabaseException
	{
		int retval = 0;

		String sql = "SELECT COUNT(*) FROM R_JOB";
		Row r = database.getOneRow(sql);
		if (r != null)
		{
			retval = (int) r.getValue(0).getInteger();
		}

		return retval;
	}

	public int getNrTransformations(long id_directory) throws KettleDatabaseException
	{
		int retval = 0;

		String sql = "SELECT COUNT(*) FROM R_TRANSFORMATION WHERE ID_DIRECTORY = " + id_directory;
		Row r = database.getOneRow(sql);
		if (r != null)
		{
			retval = (int) r.getValue(0).getInteger();
		}

		return retval;
	}

	public int getNrJobs(long id_directory) throws KettleDatabaseException
	{
		int retval = 0;

		String sql = "SELECT COUNT(*) FROM R_JOB WHERE ID_DIRECTORY = " + id_directory;
		Row r = database.getOneRow(sql);
		if (r != null)
		{
			retval = (int) r.getValue(0).getInteger();
		}

		return retval;
	}

	public int getNrSchemas(long id_directory) throws KettleDatabaseException
	{
		int retval = 0;

		/*
		 String sql = "SELECT COUNT(*) FROM R_SCHEMA WHERE ID_DIRECTORY = "+id_directory;
		 Row r = db.getOneRow(sql);
		 if (r!=null) { retval = (int)r.getValue(0).getInteger(); }
		 */
		return retval;
	}

	public int getNrDirectories(long id_directory) throws KettleDatabaseException
	{
		int retval = 0;

		String sql = "SELECT COUNT(*) FROM R_DIRECTORY WHERE ID_DIRECTORY_PARENT = " + id_directory;
		Row r = database.getOneRow(sql);
		if (r != null)
		{
			retval = (int) r.getValue(0).getInteger();
		}

		return retval;
	}

	public int getNrConditions(long id_transforamtion) throws KettleDatabaseException
	{
		int retval = 0;

		String sql = "SELECT COUNT(*) FROM R_TRANS_STEP_CONDITION WHERE ID_TRANSFORMATION = " + id_transforamtion;
		Row r = database.getOneRow(sql);
		if (r != null)
		{
			retval = (int) r.getValue(0).getInteger();
		}

		return retval;
	}

	public int getNrDatabases(long id_transforamtion) throws KettleDatabaseException
	{
		int retval = 0;

		String sql = "SELECT COUNT(*) FROM R_STEP_DATABASE WHERE ID_TRANSFORMATION = " + id_transforamtion;
		Row r = database.getOneRow(sql);
		if (r != null)
		{
			retval = (int) r.getValue(0).getInteger();
		}

		return retval;
	}

	public int getNrSubConditions(long id_condition) throws KettleDatabaseException
	{
		int retval = 0;

		String sql = "SELECT COUNT(*) FROM R_CONDITION WHERE ID_CONDITION_PARENT = " + id_condition;
		Row r = database.getOneRow(sql);
		if (r != null)
		{
			retval = (int) r.getValue(0).getInteger();
		}

		return retval;
	}

	public int getNrTransNotes(long id_transformation) throws KettleDatabaseException
	{
		int retval = 0;

		String sql = "SELECT COUNT(*) FROM R_TRANS_NOTE WHERE ID_TRANSFORMATION = " + id_transformation;
		Row r = database.getOneRow(sql);
		if (r != null)
		{
			retval = (int) r.getValue(0).getInteger();
		}

		return retval;
	}

	public int getNrJobNotes(long id_job) throws KettleDatabaseException
	{
		int retval = 0;

		String sql = "SELECT COUNT(*) FROM R_JOB_NOTE WHERE ID_JOB = " + id_job;
		Row r = database.getOneRow(sql);
		if (r != null)
		{
			retval = (int) r.getValue(0).getInteger();
		}

		return retval;
	}

	public int getNrDatabases() throws KettleDatabaseException
	{
		int retval = 0;

		String sql = "SELECT COUNT(*) FROM R_DATABASE";
		Row r = database.getOneRow(sql);
		if (r != null)
		{
			retval = (int) r.getValue(0).getInteger();
		}

		return retval;
	}

    public int getNrDatabaseAttributes(long id_database) throws KettleDatabaseException
    {
        int retval = 0;

        String sql = "SELECT COUNT(*) FROM R_DATABASE_ATTRIBUTE WHERE ID_DATABASE = "+id_database;
        Row r = database.getOneRow(sql);
        if (r != null)
        {
            retval = (int) r.getValue(0).getInteger();
        }

        return retval;
    }

	public int getNrSteps(long id_transformation) throws KettleDatabaseException
	{
		int retval = 0;

		String sql = "SELECT COUNT(*) FROM R_STEP WHERE ID_TRANSFORMATION = " + id_transformation;
		Row r = database.getOneRow(sql);
		if (r != null)
		{
			retval = (int) r.getValue(0).getInteger();
		}

		return retval;
	}

	public int getNrStepDatabases(long id_database) throws KettleDatabaseException
	{
		int retval = 0;

		String sql = "SELECT COUNT(*) FROM R_STEP_DATABASE WHERE ID_DATABASE = " + id_database;
		Row r = database.getOneRow(sql);
		if (r != null)
		{
			retval = (int) r.getValue(0).getInteger();
		}

		return retval;
	}

	public int getNrStepAttributes(long id_step) throws KettleDatabaseException
	{
		int retval = 0;

		String sql = "SELECT COUNT(*) FROM R_STEP_ATTRIBUTE WHERE ID_STEP = " + id_step;
		Row r = database.getOneRow(sql);
		if (r != null)
		{
			retval = (int) r.getValue(0).getInteger();
		}

		return retval;
	}

	public int getNrTransHops(long id_transformation) throws KettleDatabaseException
	{
		int retval = 0;

		String sql = "SELECT COUNT(*) FROM R_TRANS_HOP WHERE ID_TRANSFORMATION = " + id_transformation;
		Row r = database.getOneRow(sql);
		if (r != null)
		{
			retval = (int) r.getValue(0).getInteger();
		}

		return retval;
	}

	public int getNrJobHops(long id_job) throws KettleDatabaseException
	{
		int retval = 0;

		String sql = "SELECT COUNT(*) FROM R_JOB_HOP WHERE ID_JOB = " + id_job;
		Row r = database.getOneRow(sql);
		if (r != null)
		{
			retval = (int) r.getValue(0).getInteger();
		}

		return retval;
	}

	public int getNrTransDependencies(long id_transformation) throws KettleDatabaseException
	{
		int retval = 0;

		String sql = "SELECT COUNT(*) FROM R_DEPENDENCY WHERE ID_TRANSFORMATION = " + id_transformation;
		Row r = database.getOneRow(sql);
		if (r != null)
		{
			retval = (int) r.getValue(0).getInteger();
		}

		return retval;
	}

	public int getNrJobEntries(long id_job) throws KettleDatabaseException
	{
		int retval = 0;

		String sql = "SELECT COUNT(*) FROM R_JOBENTRY WHERE ID_JOB = " + id_job;
		Row r = database.getOneRow(sql);
		if (r != null)
		{
			retval = (int) r.getValue(0).getInteger();
		}

		return retval;
	}

	public int getNrJobEntryCopies(long id_job, long id_jobentry) throws KettleDatabaseException
	{
		int retval = 0;

		String sql = "SELECT COUNT(*) FROM R_JOBENTRY_COPY WHERE ID_JOB = " + id_job + " AND ID_JOBENTRY = "
						+ id_jobentry;
		Row r = database.getOneRow(sql);
		if (r != null)
		{
			retval = (int) r.getValue(0).getInteger();
		}

		return retval;
	}

	public int getNrJobEntryCopies(long id_job) throws KettleDatabaseException
	{
		int retval = 0;

		String sql = "SELECT COUNT(*) FROM R_JOBENTRY_COPY WHERE ID_JOB = " + id_job;
		Row r = database.getOneRow(sql);
		if (r != null)
		{
			retval = (int) r.getValue(0).getInteger();
		}

		return retval;
	}

	public int getNrUsers() throws KettleDatabaseException
	{
		int retval = 0;

		String sql = "SELECT COUNT(*) FROM R_USER";
		Row r = database.getOneRow(sql);
		if (r != null)
		{
			retval = (int) r.getValue(0).getInteger();
		}

		return retval;
	}

	public int getNrPermissions(long id_profile) throws KettleDatabaseException
	{
		int retval = 0;

		String sql = "SELECT COUNT(*) FROM R_PROFILE_PERMISSION WHERE ID_PROFILE = " + id_profile;
		Row r = database.getOneRow(sql);
		if (r != null)
		{
			retval = (int) r.getValue(0).getInteger();
		}

		return retval;
	}

	public int getNrProfiles() throws KettleDatabaseException
	{
		int retval = 0;

		String sql = "SELECT COUNT(*) FROM R_PROFILE";
		Row r = database.getOneRow(sql);
		if (r != null)
		{
			retval = (int) r.getValue(0).getInteger();
		}

		return retval;
	}

	public String[] getTransformationNames(long id_directory) throws KettleDatabaseException
	{
		int nr = getNrTransformations(id_directory);

		String retval[] = new String[nr];
		String sql = "SELECT NAME FROM R_TRANSFORMATION WHERE ID_DIRECTORY = " + id_directory + " ORDER BY NAME";

		ResultSet rs = database.openQuery(sql);
		if (rs != null)
		{
			Row r = database.getRow(rs);
			int i = 0;
			while (r != null)
			{
				retval[i] = r.getValue(0).getString();
				r = database.getRow(rs);
				i++;
			}
			database.closeQuery(rs);
		}

		return Const.sortStrings(retval);
	}

	public String[] getJobNames(long id_directory) throws KettleDatabaseException
	{
		int nr = getNrJobs(id_directory);
		String retval[] = new String[nr];
		String sql = "SELECT NAME FROM R_JOB WHERE ID_DIRECTORY = " + id_directory + " ORDER BY NAME";

		ResultSet rs = database.openQuery(sql);
		if (rs != null)
		{
			Row r = database.getRow(rs);
			int i = 0;
			while (r != null)
			{
				retval[i] = r.getValue(0).getString();
				r = database.getRow(rs);
				i++;
			}
			database.closeQuery(rs);
		}

		return Const.sortStrings(retval);
	}

	public String[] getSchemaNames(long id_directory) throws KettleDatabaseException
	{
		int nr = getNrSchemas(id_directory);
		String retval[] = new String[nr];
		/*
		 String sql = "SELECT NAME FROM R_SCHEMA WHERE ID_DIRECTORY = "+id_directory+" ORDER BY NAME";
		 
		 ResultSet rs = db.openQuery(sql);
		 if (rs!=null)
		 {
		 Row r = db.getRow(rs);
		 int i = 0;
		 while (r!=null)
		 {
		 retval[i] = r.getValue(0).getString(); 
		 r = db.getRow(rs);
		 i++;
		 }
		 db.closeQuery(rs);
		 }
		 */
		return Const.sortStrings(retval);
	}

	public String[] getDirectoryNames(long id_directory) throws KettleDatabaseException
	{
		int nr = getNrDirectories(id_directory);
		String retval[] = new String[nr];
		String sql = "SELECT   DIRECTORY_NAME " + "FROM     R_DIRECTORY " + "WHERE    ID_DIRECTORY_PARENT = "
						+ id_directory + " " + "ORDER BY DIRECTORY_NAME";

		ResultSet rs = database.openQuery(sql);
		if (rs != null)
		{
			Row r = database.getRow(rs);
			int i = 0;
			while (r != null)
			{
				retval[i] = r.getValue(0).getString();
				r = database.getRow(rs);
				i++;
			}
			database.closeQuery(rs);
		}

		return Const.sortStrings(retval);
	}

	public String[] getJobNames() throws KettleDatabaseException
	{
		int nr = getNrJobs();
		String retval[] = new String[nr];
		String sql = "SELECT NAME FROM R_JOB ORDER BY NAME";

		ResultSet rs = database.openQuery(sql);
		if (rs != null)
		{
			Row r = database.getRow(rs);
			int i = 0;
			while (r != null)
			{
				retval[i] = r.getValue(0).getString();
				r = database.getRow(rs);
				i++;
			}
			database.closeQuery(rs);
		}

		return retval;
	}

	public long[] getSubConditionIDs(long id_condition) throws KettleDatabaseException
	{
		int nr = getNrSubConditions(id_condition);
		long retval[] = new long[nr];

		String sql = "SELECT ID_CONDITION FROM R_CONDITION WHERE ID_CONDITION_PARENT = " + id_condition;

		ResultSet rs = database.openQuery(sql);
		Row r = database.getRow(rs);
		int i = 0;
		while (r != null)
		{
			retval[i] = r.getValue(0).getInteger();
			r = database.getRow(rs);
			i++;
		}
		database.closeQuery(rs);

		return retval;
	}

	public long[] getTransNoteIDs(long id_transformation) throws KettleDatabaseException
	{
		int nr = getNrTransNotes(id_transformation);
		long retval[] = new long[nr];

		String sql = "SELECT ID_NOTE FROM R_TRANS_NOTE WHERE ID_TRANSFORMATION = " + id_transformation;

		ResultSet rs = database.openQuery(sql);
		Row r = database.getRow(rs);
		int i = 0;
		while (r != null)
		{
			retval[i] = r.getValue(0).getInteger();
			r = database.getRow(rs);
			i++;
		}
		database.closeQuery(rs);

		return retval;
	}

	public long[] getConditionIDs(long id_transformation) throws KettleDatabaseException
	{
		int nr = getNrConditions(id_transformation);
		long retval[] = new long[nr];

		String sql = "SELECT ID_CONDITION FROM R_TRANS_STEP_CONDITION WHERE ID_TRANSFORMATION = " + id_transformation;

		ResultSet rs = database.openQuery(sql);
		Row r = database.getRow(rs);
		int i = 0;
		while (r != null)
		{
			retval[i] = r.getValue(0).getInteger();
			r = database.getRow(rs);
			i++;
		}
		database.closeQuery(rs);

		return retval;
	}

	public long[] getDatabaseIDs(long id_transformation) throws KettleDatabaseException
	{
		int nr = getNrDatabases(id_transformation);
		long retval[] = new long[nr];

		String sql = "SELECT ID_DATABASE FROM R_STEP_DATABASE WHERE ID_TRANSFORMATION = " + id_transformation;

		ResultSet rs = database.openQuery(sql);
		Row r = database.getRow(rs);
		int i = 0;
		while (r != null)
		{
			retval[i] = r.getValue(0).getInteger();
			r = database.getRow(rs);
			i++;
		}
		database.closeQuery(rs);

		return retval;
	}

	public long[] getJobNoteIDs(long id_job) throws KettleDatabaseException
	{
		int nr = getNrJobNotes(id_job);
		long retval[] = new long[nr];

		String sql = "SELECT ID_NOTE FROM R_JOB_NOTE WHERE ID_JOB = " + id_job;

		ResultSet rs = database.openQuery(sql);
		Row r = database.getRow(rs);
		int i = 0;
		while (r != null)
		{
			retval[i] = r.getValue(0).getInteger();
			r = database.getRow(rs);
			i++;
		}
		database.closeQuery(rs);

		return retval;
	}

	public long[] getDatabaseIDs() throws KettleDatabaseException
	{
		int nr = getNrDatabases();
		long retval[] = new long[nr];

        String sql = "SELECT ID_DATABASE FROM R_DATABASE";

		ResultSet rs = database.openQuery(sql);
		if (rs != null)
		{
			Row r = database.getRow(rs);
			int i = 0;
			while (r != null)
			{
				retval[i] = r.getValue(0).getInteger();
				r = database.getRow(rs);
				i++;
			}
			database.closeQuery(rs);
		}

		return retval;
	}
    
    public long[] getDatabaseAttributeIDs(long id_database) throws KettleDatabaseException
    {
        int nr = getNrDatabaseAttributes(id_database);
        long retval[] = new long[nr];

        String sql = "SELECT ID_DATABASE_ATTRIBUTE FROM R_DATABASE_ATTRIBUTE WHERE ID_DATABASE = "+id_database;

        ResultSet rs = database.openQuery(sql);
        if (rs != null)
        {
            Row r = database.getRow(rs);
            int i = 0;
            while (r != null)
            {
                retval[i] = r.getValue(0).getInteger();
                r = database.getRow(rs);
                i++;
            }
            database.closeQuery(rs);
        }

        return retval;
    }


	public String[] getDatabaseNames() throws KettleDatabaseException
	{
		int nr = getNrDatabases();
		String retval[] = new String[nr];

		String sql = "SELECT NAME FROM R_DATABASE ORDER BY NAME";

		ResultSet rs = database.openQuery(sql);
		if (rs != null)
		{
			Row r = database.getRow(rs);
			int i = 0;
			while (r != null)
			{
				retval[i] = r.getValue(0).getString();
				r = database.getRow(rs);
				i++;
			}
			database.closeQuery(rs);
		}

		return retval;
	}

	public long[] getStepIDs(long id_transformation) throws KettleDatabaseException
	{
		int nr = getNrSteps(id_transformation);
		long retval[] = new long[nr];

		String sql = "SELECT ID_STEP FROM R_STEP WHERE ID_TRANSFORMATION = " + id_transformation;

		ResultSet rs = database.openQuery(sql);
		Row r = database.getRow(rs);
		int i = 0;
		while (r != null)
		{
			retval[i] = r.getValue(0).getInteger();
			r = database.getRow(rs);
			i++;
		}
		database.closeQuery(rs);

		return retval;
	}

	public String[] getTransformationsUsingDatabase(long id_database) throws KettleDatabaseException
	{
		String sql = "SELECT DISTINCT ID_TRANSFORMATION FROM R_STEP_DATABASE WHERE ID_DATABASE = " + id_database;

		ArrayList list = database.getRows(sql, 100);
		String[] transList = new String[list.size()];
		for (int i=0;i<list.size();i++)
		{
			long id_transformation = ((Row)list.get(i)).getInteger("ID_TRANSFORMATION", -1L); 
			if (id_transformation > 0)
			{
				Row transRow =  getTransformation(id_transformation);
				if (transRow!=null)
				{
					String transName = transRow.getString("NAME", "<name not found>");
					long id_directory = transRow.getInteger("ID_DIRECTORY", -1L);
					RepositoryDirectory dir = directoryTree.findDirectory(id_directory);
					
					transList[i]=dir.getPath()+Const.FILE_SEPARATOR+transName;
				}
			}
			
		}

		return transList;
	}

	public long[] getTransHopIDs(long id_transformation) throws KettleDatabaseException
	{
		int nr = getNrTransHops(id_transformation);
		long retval[] = new long[nr];

		String sql = "SELECT ID_TRANS_HOP FROM R_TRANS_HOP WHERE ID_TRANSFORMATION = " + id_transformation;

		ResultSet rs = database.openQuery(sql);
		Row r = database.getRow(rs);
		int i = 0;
		while (r != null)
		{
			retval[i] = r.getValue(0).getInteger();
			r = database.getRow(rs);
			i++;
		}
		database.closeQuery(rs);

		return retval;
	}

	public long[] getJobHopIDs(long id_job) throws KettleDatabaseException
	{
		int nr = getNrJobHops(id_job);
		long retval[] = new long[nr];

		String sql = "SELECT ID_JOB_HOP FROM R_JOB_HOP WHERE ID_JOB = " + id_job;

		ResultSet rs = database.openQuery(sql);
		Row r = database.getRow(rs);
		int i = 0;
		while (r != null)
		{
			retval[i] = r.getValue(0).getInteger();
			r = database.getRow(rs);
			i++;
		}
		database.closeQuery(rs);

		return retval;
	}

	public long[] getTransDependencyIDs(long id_transformation) throws KettleDatabaseException
	{
		int nr = getNrTransDependencies(id_transformation);
		long retval[] = new long[nr];

		String sql = "SELECT ID_DEPENDENCY FROM R_DEPENDENCY WHERE ID_TRANSFORMATION = " + id_transformation;

		ResultSet rs = database.openQuery(sql);
		Row r = database.getRow(rs);
		int i = 0;
		while (r != null)
		{
			retval[i] = r.getValue(0).getInteger();
			r = database.getRow(rs);
			i++;
		}
		database.closeQuery(rs);

		return retval;
	}

	public long[] getUserIDs() throws KettleDatabaseException
	{
		int nr = getNrUsers();
		long retval[] = new long[nr];

		String sql = "SELECT ID_USER FROM R_USER";

		ResultSet rs = database.openQuery(sql);
		if (rs != null)
		{
			Row r = database.getRow(rs);
			int i = 0;
			while (r != null)
			{
				retval[i] = r.getValue(0).getInteger();
				r = database.getRow(rs);
				i++;
			}
			database.closeQuery(rs);
		}

		return retval;
	}

	public String[] getUserLogins() throws KettleDatabaseException
	{
		int nr = getNrUsers();
		String retval[] = new String[nr];

		String sql = "SELECT LOGIN FROM R_USER ORDER BY LOGIN";

		ResultSet rs = database.openQuery(sql);
		if (rs != null)
		{
			Row r = database.getRow(rs);
			int i = 0;
			while (r != null)
			{
				retval[i] = r.getValue(0).getString();
				r = database.getRow(rs);
				i++;
			}
			database.closeQuery(rs);
		}

		return retval;
	}

	public long[] getPermissionIDs(long id_profile) throws KettleDatabaseException
	{
		int nr = getNrPermissions(id_profile);
		long retval[] = new long[nr];

		String sql = "SELECT ID_PERMISSION FROM R_PROFILE_PERMISSION WHERE ID_PROFILE = " + id_profile;

		ResultSet rs = database.openQuery(sql);
		if (rs != null)
		{
			Row r = database.getRow(rs);
			int i = 0;
			while (r != null)
			{
				retval[i] = r.getValue(0).getInteger();
				r = database.getRow(rs);
				i++;
			}
			database.closeQuery(rs);
		}

		return retval;
	}

	public long[] getJobEntryIDs(long id_job) throws KettleDatabaseException
	{
		int nr = getNrJobEntries(id_job);
		long retval[] = new long[nr];

		String sql = "SELECT ID_JOBENTRY FROM R_JOBENTRY WHERE ID_JOB = " + id_job;

		ResultSet rs = database.openQuery(sql);
		Row r = database.getRow(rs);
		int i = 0;
		while (r != null)
		{
			retval[i] = r.getValue(0).getInteger();
			r = database.getRow(rs);
			i++;
		}
		database.closeQuery(rs);

		return retval;
	}

	public long[] getJobEntryCopyIDs(long id_job) throws KettleDatabaseException
	{
		int nr = getNrJobEntryCopies(id_job);
		long retval[] = new long[nr];

		String sql = "SELECT ID_JOBENTRY_COPY FROM R_JOBENTRY_COPY WHERE ID_JOB = " + id_job;

		ResultSet rs = database.openQuery(sql);
		Row r = database.getRow(rs);
		int i = 0;
		while (r != null)
		{
			retval[i] = r.getValue(0).getInteger();
			r = database.getRow(rs);
			i++;
		}
		database.closeQuery(rs);

		return retval;
	}

	public long[] getJobEntryCopyIDs(long id_job, long id_jobentry) throws KettleDatabaseException
	{
		int nr = getNrJobEntryCopies(id_job, id_jobentry);
		long retval[] = new long[nr];

		String sql = "SELECT ID_JOBENTRY_COPY FROM R_JOBENTRY_COPY WHERE ID_JOB = " + id_job + " AND ID_JOBENTRY = "
						+ id_jobentry;

		ResultSet rs = database.openQuery(sql);
		Row r = database.getRow(rs);
		int i = 0;
		while (r != null)
		{
			retval[i] = r.getValue(0).getInteger();
			r = database.getRow(rs);
			i++;
		}
		database.closeQuery(rs);

		return retval;
	}

	public String[] getProfiles() throws KettleDatabaseException
	{
		int nr = getNrProfiles();
		String retval[] = new String[nr];

		String sql = "SELECT NAME FROM R_PROFILE ORDER BY NAME";

		ResultSet rs = database.openQuery(sql);
		if (rs != null)
		{
			Row r = database.getRow(rs);
			int i = 0;
			while (r != null)
			{
				retval[i] = r.getValue(0).getString();
				r = database.getRow(rs);
				i++;
			}
			database.closeQuery(rs);
		}

		return retval;
	}

	public Row getNote(long id_note) throws KettleDatabaseException
	{
		return getOneRow("R_NOTE", "ID_NOTE", id_note);
	}

	public Row getDatabase(long id_database) throws KettleDatabaseException
	{
		return getOneRow("R_DATABASE", "ID_DATABASE", id_database);
	}

    public Row getDatabaseAttribute(long id_database_attribute) throws KettleDatabaseException
    {
        return getOneRow("R_DATABASE_ATTRIBUTE", "ID_DATABASE_ATTRIBUTE", id_database_attribute);
    }

	public Row getCondition(long id_condition) throws KettleDatabaseException
	{
		return getOneRow("R_CONDITION", "ID_CONDITION", id_condition);
	}

	public Row getValue(long id_value) throws KettleDatabaseException
	{
		return getOneRow("R_VALUE", "ID_VALUE", id_value);
	}

	public Row getStep(long id_step) throws KettleDatabaseException
	{
		return getOneRow("R_STEP", "ID_STEP", id_step);
	}

	public Row getStepType(long id_step_type) throws KettleDatabaseException
	{
		return getOneRow("R_STEP_TYPE", "ID_STEP_TYPE", id_step_type);
	}

	public Row getStepAttribute(long id_step_attribute) throws KettleDatabaseException
	{
		return getOneRow("R_STEP_ATTRIBUTE", "ID_STEP_ATTRIBUTE", id_step_attribute);
	}

	public Row getStepDatabase(long id_step) throws KettleDatabaseException
	{
		return getOneRow("R_STEP_DATABASE", "ID_STEP", id_step);
	}

	public Row getTransHop(long id_trans_hop) throws KettleDatabaseException
	{
		return getOneRow("R_TRANS_HOP", "ID_TRANS_HOP", id_trans_hop);
	}

	public Row getJobHop(long id_job_hop) throws KettleDatabaseException
	{
		return getOneRow("R_JOB_HOP", "ID_JOB_HOP", id_job_hop);
	}

	public Row getTransDependency(long id_dependency) throws KettleDatabaseException
	{
		return getOneRow("R_DEPENDENCY", "ID_DEPENDENCY", id_dependency);
	}

	public Row getTransformation(long id_transformation) throws KettleDatabaseException
	{
		return getOneRow("R_TRANSFORMATION", "ID_TRANSFORMATION", id_transformation);
	}

	public Row getUser(long id_user) throws KettleDatabaseException
	{
		return getOneRow("R_USER", "ID_USER", id_user);
	}

	public Row getProfile(long id_profile) throws KettleDatabaseException
	{
		return getOneRow("R_PROFILE", "ID_PROFILE", id_profile);
	}

	public Row getPermission(long id_permission) throws KettleDatabaseException
	{
		return getOneRow("R_PERMISSION", "ID_PERMISSION", id_permission);
	}

	public Row getJob(long id_job) throws KettleDatabaseException
	{
		return getOneRow("R_JOB", "ID_JOB", id_job);
	}

	public Row getJobEntry(long id_jobentry) throws KettleDatabaseException
	{
		return getOneRow("R_JOBENTRY", "ID_JOBENTRY", id_jobentry);
	}

	public Row getJobEntryCopy(long id_jobentry_copy) throws KettleDatabaseException
	{
		return getOneRow("R_JOBENTRY_COPY", "ID_JOBENTRY_COPY", id_jobentry_copy);
	}

	public Row getJobEntryType(long id_jobentry_type) throws KettleDatabaseException
	{
		return getOneRow("R_JOBENTRY_TYPE", "ID_JOBENTRY_TYPE", id_jobentry_type);
	}

	public Row getDirectory(long id_directory) throws KettleDatabaseException
	{
		return getOneRow("R_DIRECTORY", "ID_DIRECTORY", id_directory);
	}
	
	private Row getOneRow(String tablename, String keyfield, long id) throws KettleDatabaseException
	{
		String sql = "SELECT * FROM " + tablename + " WHERE " + keyfield + " = " + id;

		return database.getOneRow(sql);
	}

	// STEP ATTRIBUTES: SAVE

	public long saveStepAttribute(long id_transformation, long id_step, String code, String value)
			throws KettleDatabaseException
	{
		return saveStepAttribute(code, 0, id_transformation, id_step, 0.0, value);
	}

	public long saveStepAttribute(long id_transformation, long id_step, String code, double value)
			throws KettleDatabaseException
	{
		return saveStepAttribute(code, 0, id_transformation, id_step, value, null);
	}

	public long saveStepAttribute(long id_transformation, long id_step, String code, boolean value)
			throws KettleDatabaseException
	{
		return saveStepAttribute(code, 0, id_transformation, id_step, 0.0, value ? "Y" : "N");
	}

	public long saveStepAttribute(long id_transformation, long id_step, long nr, String code, String value)
			throws KettleDatabaseException
	{
	    if (value==null || value.length()==0) return -1L;
		return saveStepAttribute(code, nr, id_transformation, id_step, 0.0, value);
	}

	public long saveStepAttribute(long id_transformation, long id_step, long nr, String code, double value)
			throws KettleDatabaseException
	{
		return saveStepAttribute(code, nr, id_transformation, id_step, value, null);
	}

	public long saveStepAttribute(long id_transformation, long id_step, long nr, String code, boolean value)
			throws KettleDatabaseException
	{
		return saveStepAttribute(code, nr, id_transformation, id_step, 0.0, value ? "Y" : "N");
	}

	private long saveStepAttribute(String code, long nr, long id_transformation, long id_step, double value_num,
			String value_str) throws KettleDatabaseException
	{
		return insertStepAttribute(id_transformation, id_step, nr, code, value_num, value_str);
	}

	// STEP ATTRIBUTES: GET

	public void setLookupStepAttribute() throws KettleDatabaseException
	{
		String sql = "SELECT VALUE_STR, VALUE_NUM FROM R_STEP_ATTRIBUTE WHERE ID_STEP = ?  AND CODE = ?  AND NR = ? ";

		psStepAttributesLookup = database.prepareSQL(sql);
	}

	public void closeStepAttributeLookupPreparedStatement() throws KettleDatabaseException
	{
		database.closePreparedStatement(psStepAttributesLookup);
		psStepAttributesLookup = null;
	}
	
	public void closeStepAttributeInsertPreparedStatement() throws KettleDatabaseException
	{
	    if (psStepAttributesInsert!=null)
	    {
		    database.insertFinished(psStepAttributesInsert, true); // batch mode!
			database.closePreparedStatement(psStepAttributesInsert);
			psStepAttributesInsert = null;
	    }
	}


	private Row getStepAttributeRow(long id_step, int nr, String code) throws KettleDatabaseException
	{
		Row par = new Row();
		par.addValue(new Value("ID_STEP", id_step));
		par.addValue(new Value("CODE", code));
		par.addValue(new Value("NR", (long) nr));

		database.setValues(par, psStepAttributesLookup);

		return database.getLookup(psStepAttributesLookup);
	}

	public long getStepAttributeInteger(long id_step, int nr, String code) throws KettleDatabaseException
	{
		Row r = null;
		if (stepAttributesBuffer!=null) r = searchStepAttributeInBuffer(id_step, code, (long)nr);
		else                            r = getStepAttributeRow(id_step, nr, code);
		if (r == null)
			return 0;
		return r.searchValue("VALUE_NUM").getInteger();
	}

	public double getStepAttributeNumber(long id_step, int nr, String code) throws KettleDatabaseException
	{
		Row r = null;
		if (stepAttributesBuffer!=null) r = searchStepAttributeInBuffer(id_step, code, (long)nr);
		else                            r = getStepAttributeRow(id_step, nr, code);
		if (r == null)
			return 0.0;
		return r.searchValue("VALUE_NUM").getNumber();
	}

	public String getStepAttributeString(long id_step, int nr, String code) throws KettleDatabaseException
	{
		Row r = null;
		if (stepAttributesBuffer!=null) r = searchStepAttributeInBuffer(id_step, code, (long)nr);
		else                            r = getStepAttributeRow(id_step, nr, code);
		if (r == null)
			return null;
		return r.searchValue("VALUE_STR").getString();
	}

	public boolean getStepAttributeBoolean(long id_step, int nr, String code, boolean def) throws KettleDatabaseException
	{
		Row r = null;
		if (stepAttributesBuffer!=null) r = searchStepAttributeInBuffer(id_step, code, (long)nr);
		else                            r = getStepAttributeRow(id_step, nr, code);
		if (r == null)
			return def;
        Value v = r.searchValue("VALUE_STR");
        if (v==null) return def;
		return v.getBoolean();
	}

    public boolean getStepAttributeBoolean(long id_step, int nr, String code) throws KettleDatabaseException
    {
        Row r = null;
        if (stepAttributesBuffer!=null) r = searchStepAttributeInBuffer(id_step, code, (long)nr);
        else                            r = getStepAttributeRow(id_step, nr, code);
        if (r == null)
            return false;
        return r.searchValue("VALUE_STR").getBoolean();
    }

	public double getStepAttributeNumber(long id_step, String code) throws KettleDatabaseException
	{
		return getStepAttributeNumber(id_step, 0, code);
	}

	public long getStepAttributeInteger(long id_step, String code) throws KettleDatabaseException
	{
		return getStepAttributeInteger(id_step, 0, code);
	}

	public String getStepAttributeString(long id_step, String code) throws KettleDatabaseException
	{
		return getStepAttributeString(id_step, 0, code);
	}

	public boolean getStepAttributeBoolean(long id_step, String code) throws KettleDatabaseException
	{
		return getStepAttributeBoolean(id_step, 0, code);
	}

	public int countNrStepAttributes(long id_step, String code) throws KettleDatabaseException
	{
	    if (stepAttributesBuffer!=null) // see if we can do this in memory...
	    {
	        int nr = searchNrStepAttributes(id_step, code);
            return nr;
	    }
	    else
	    {
			String sql = "SELECT COUNT(*) FROM R_STEP_ATTRIBUTE WHERE ID_STEP = ? AND CODE = ?";
			Row table = new Row();
			table.addValue(new Value("ID_STEP", id_step));
			table.addValue(new Value("CODE", code));
			Row r = database.getOneRow(sql, table);
			if (r == null)
				return 0;
            
			return (int) r.getValue(0).getInteger();
	    }
	}

	// JOBENTRY ATTRIBUTES: SAVE

	// WANTED: throw extra exceptions to locate storage problems (strings too long etc)
	//
	public long saveJobEntryAttribute(long id_job, long id_jobentry, String code, String value)
			throws KettleDatabaseException
	{
		return saveJobEntryAttribute(code, 0, id_job, id_jobentry, 0.0, value);
	}

	public long saveJobEntryAttribute(long id_job, long id_jobentry, String code, double value)
			throws KettleDatabaseException
	{
		return saveJobEntryAttribute(code, 0, id_job, id_jobentry, value, null);
	}

	public long saveJobEntryAttribute(long id_job, long id_jobentry, String code, boolean value)
			throws KettleDatabaseException
	{
		return saveJobEntryAttribute(code, 0, id_job, id_jobentry, 0.0, value ? "Y" : "N");
	}

	public long saveJobEntryAttribute(long id_job, long id_jobentry, long nr, String code, String value)
			throws KettleDatabaseException
	{
		return saveJobEntryAttribute(code, nr, id_job, id_jobentry, 0.0, value);
	}

	public long saveJobEntryAttribute(long id_job, long id_jobentry, long nr, String code, double value)
			throws KettleDatabaseException
	{
		return saveJobEntryAttribute(code, nr, id_job, id_jobentry, value, null);
	}

	public long saveJobEntryAttribute(long id_job, long id_jobentry, long nr, String code, boolean value)
			throws KettleDatabaseException
	{
		return saveJobEntryAttribute(code, nr, id_job, id_jobentry, 0.0, value ? "Y" : "N");
	}

	private long saveJobEntryAttribute(String code, long nr, long id_job, long id_jobentry, double value_num,
			String value_str) throws KettleDatabaseException
	{
		return insertJobEntryAttribute(id_job, id_jobentry, nr, code, value_num, value_str);
	}

	// JOBENTRY ATTRIBUTES: GET

	public void setLookupJobEntryAttribute() throws KettleDatabaseException
	{
		String sql = "SELECT VALUE_STR, VALUE_NUM FROM R_JOBENTRY_ATTRIBUTE WHERE ID_JOBENTRY = ?  AND CODE = ?  AND NR = ? ";

		pstmt_entry_attributes = database.prepareSQL(sql);
	}

	public void closeLookupJobEntryAttribute() throws KettleDatabaseException
	{
		database.closePreparedStatement(pstmt_entry_attributes);
	}

	private Row getJobEntryAttributeRow(long id_jobentry, int nr, String code) throws KettleDatabaseException
	{
		Row par = new Row();
		par.addValue(new Value("ID_JOBENTRY", id_jobentry));
		par.addValue(new Value("CODE", code));
		par.addValue(new Value("NR", (long) nr));

		database.setValues(par, pstmt_entry_attributes);
		return database.getLookup(pstmt_entry_attributes);
	}

	public long getJobEntryAttributeInteger(long id_jobentry, int nr, String code) throws KettleDatabaseException
	{
		Row r = getJobEntryAttributeRow(id_jobentry, nr, code);
		if (r == null)
			return 0;
		return r.searchValue("VALUE_NUM").getInteger();
	}

	public double getJobEntryAttributeNumber(long id_jobentry, int nr, String code) throws KettleDatabaseException
	{
		Row r = getJobEntryAttributeRow(id_jobentry, nr, code);
		if (r == null)
			return 0.0;
		return r.searchValue("VALUE_NUM").getNumber();
	}

	public String getJobEntryAttributeString(long id_jobentry, int nr, String code) throws KettleDatabaseException
	{
		Row r = getJobEntryAttributeRow(id_jobentry, nr, code);
		if (r == null)
			return null;
		return r.searchValue("VALUE_STR").getString();
	}

	public boolean getJobEntryAttributeBoolean(long id_jobentry, int nr, String code) throws KettleDatabaseException
	{
		Row r = getJobEntryAttributeRow(id_jobentry, nr, code);
		if (r == null)
			return false;
		return r.searchValue("VALUE_STR").getBoolean();
	}

	public double getJobEntryAttributeNumber(long id_jobentry, String code) throws KettleDatabaseException
	{
		return getJobEntryAttributeNumber(id_jobentry, 0, code);
	}

	public long getJobEntryAttributeInteger(long id_jobentry, String code) throws KettleDatabaseException
	{
		return getJobEntryAttributeInteger(id_jobentry, 0, code);
	}

	public String getJobEntryAttributeString(long id_jobentry, String code) throws KettleDatabaseException
	{
		return getJobEntryAttributeString(id_jobentry, 0, code);
	}

	public boolean getJobEntryAttributeBoolean(long id_jobentry, String code) throws KettleDatabaseException
	{
		return getJobEntryAttributeBoolean(id_jobentry, 0, code);
	}

	public int countNrJobEntryAttributes(long id_jobentry, String code) throws KettleDatabaseException
	{
		String sql = "SELECT COUNT(*) FROM R_JOBENTRY_ATTRIBUTE WHERE ID_JOBENTRY = ? AND CODE = ?";
		Row table = new Row();
		table.addValue(new Value("ID_JOBENTRY", id_jobentry));
		table.addValue(new Value("CODE", code));
		Row r = database.getOneRow(sql, table);
		if (r == null)
			return 0;
		return (int) r.getValue(0).getInteger();
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	// DELETE DATA IN REPOSITORY
	//////////////////////////////////////////////////////////////////////////////////////////

	public void delSteps(long id_transformation) throws KettleDatabaseException
	{
		String sql = "DELETE FROM R_STEP WHERE ID_TRANSFORMATION = " + id_transformation;
		database.execStatement(sql);
	}

	public void delCondition(long id_condition) throws KettleDatabaseException
	{
		boolean ok = true;
		long ids[] = getSubConditionIDs(id_condition);
		if (ids.length > 0)
		{
			// Delete the sub-conditions...
			for (int i = 0; i < ids.length && ok; i++)
			{
				delCondition(ids[i]);
			}

			// Then delete the main condition
			delCondition(id_condition);
		}
		else
		{
			String sql = "DELETE FROM R_CONDITION WHERE ID_CONDITION = " + id_condition;
			database.execStatement(sql);
		}
	}

	public void delStepConditions(long id_transformation) throws KettleDatabaseException
	{
		long ids[] = getConditionIDs(id_transformation);
		for (int i = 0; i < ids.length; i++)
		{
			delCondition(ids[i]);
		}
		String sql = "DELETE FROM R_TRANS_STEP_CONDITION WHERE ID_TRANSFORMATION = " + id_transformation;
		database.execStatement(sql);
	}

	/**
	 * Delete the relationships between the transformation/steps and the databases.
	 * @param id_transformation the transformation for which we want to delete the databases.
	 * @throws KettleDatabaseException in case something unexpected happens.
	 */
	public void delStepDatabases(long id_transformation) throws KettleDatabaseException
	{
		String sql = "DELETE FROM R_STEP_DATABASE WHERE ID_TRANSFORMATION = " + id_transformation;
		database.execStatement(sql);
	}

	public void delJobEntries(long id_job) throws KettleDatabaseException
	{
		String sql = "DELETE FROM R_JOBENTRY WHERE ID_JOB = " + id_job;
		database.execStatement(sql);
	}

	public void delJobEntryCopies(long id_job) throws KettleDatabaseException
	{
		String sql = "DELETE FROM R_JOBENTRY_COPY WHERE ID_JOB = " + id_job;
		database.execStatement(sql);
	}

	public void delDependencies(long id_transformation) throws KettleDatabaseException
	{
		String sql = "DELETE FROM R_DEPENDENCY WHERE ID_TRANSFORMATION = " + id_transformation;
		database.execStatement(sql);
	}

	public void delStepAttributes(long id_transformation) throws KettleDatabaseException
	{
		String sql = "DELETE FROM R_STEP_ATTRIBUTE WHERE ID_TRANSFORMATION = " + id_transformation;
		database.execStatement(sql);
	}

	public void delJobEntryAttributes(long id_job) throws KettleDatabaseException
	{
		String sql = "DELETE FROM R_JOBENTRY_ATTRIBUTE WHERE ID_JOB = " + id_job;
		database.execStatement(sql);
	}

	public void delTransHops(long id_transformation) throws KettleDatabaseException
	{
		String sql = "DELETE FROM R_TRANS_HOP WHERE ID_TRANSFORMATION = " + id_transformation;
		database.execStatement(sql);
	}

	public void delJobHops(long id_job) throws KettleDatabaseException
	{
		String sql = "DELETE FROM R_JOB_HOP WHERE ID_JOB = " + id_job;
		database.execStatement(sql);
	}

	public void delTransNotes(long id_transformation) throws KettleDatabaseException
	{
		long ids[] = getTransNoteIDs(id_transformation);

		for (int i = 0; i < ids.length; i++)
		{
			String sql = "DELETE FROM R_NOTE WHERE ID_NOTE = " + ids[i];
			database.execStatement(sql);
		}

		String sql = "DELETE FROM R_TRANS_NOTE WHERE ID_TRANSFORMATION = " + id_transformation;
		database.execStatement(sql);
	}

	public void delJobNotes(long id_job) throws KettleDatabaseException
	{
		long ids[] = getJobNoteIDs(id_job);

		for (int i = 0; i < ids.length; i++)
		{
			String sql = "DELETE FROM R_NOTE WHERE ID_NOTE = " + ids[i];
			database.execStatement(sql);
		}

		String sql = "DELETE FROM R_JOB_NOTE WHERE ID_JOB = " + id_job;
		database.execStatement(sql);
	}

	public void delTrans(long id_transformation) throws KettleDatabaseException
	{
		String sql = "DELETE FROM R_TRANSFORMATION WHERE ID_TRANSFORMATION = " + id_transformation;
		database.execStatement(sql);
	}

	public void delJob(long id_job) throws KettleDatabaseException
	{
		String sql = "DELETE FROM R_JOB WHERE ID_JOB = " + id_job;
		database.execStatement(sql);
	}

	public void delDatabase(long id_database) throws KettleDatabaseException
	{
		// First, see if the database connection is still used by other connections...
		// If so, generate an error!!
		// We look in table R_STEP_DATABASE to see if there are any steps using this database.
		String[] transList = getTransformationsUsingDatabase(id_database);
		// TODO: add check for jobs too.
		// TODO: add R_JOBENTRY_DATABASE table & lookups.
		
		if (transList.length==0)
		{
			String sql = "DELETE FROM R_DATABASE WHERE ID_DATABASE = " + id_database;
			database.execStatement(sql);
		}
		else
		{
			
			String message = "Database used by the following transformations:"+Const.CR;
			for (int i = 0; i < transList.length; i++)
			{
				message+="	"+transList[i]+Const.CR;
			}
			KettleDependencyException e = new KettleDependencyException(message);
			throw new KettleDependencyException("This database is still in use by one or more transformations ("+transList.length+" references)", e);
		}
	}
    
    public void delDatabaseAttributes(long id_database) throws KettleDatabaseException
    {
        String sql = "DELETE FROM R_DATABASE_ATTRIBUTE WHERE ID_DATABASE = " + id_database;
        database.execStatement(sql);
    }

	public void delTransStepCondition(long id_transformation) throws KettleDatabaseException
	{
		String sql = "DELETE FROM R_TRANS_STEP_CONDITION WHERE ID_TRANSFORMATION = " + id_transformation;
		database.execStatement(sql);
	}

	public void delValue(long id_value) throws KettleDatabaseException
	{
		String sql = "DELETE FROM R_VALUE WHERE ID_VALUE = " + id_value;
		database.execStatement(sql);
	}

	public void delUser(long id_user) throws KettleDatabaseException
	{
		String sql = "DELETE FROM R_USER WHERE ID_USER = " + id_user;
		database.execStatement(sql);
	}

	public void delProfile(long id_profile) throws KettleDatabaseException
	{
		String sql = "DELETE FROM R_PROFILE WHERE ID_PROFILE = " + id_profile;
		database.execStatement(sql);
	}

	public void delProfilePermissions(long id_profile) throws KettleDatabaseException
	{
		String sql = "DELETE FROM R_PROFILE_PERMISSION WHERE ID_PROFILE = " + id_profile;
		database.execStatement(sql);
	}

	public void delAllFromTrans(long id_transformation) throws KettleDatabaseException
	{
		delTransNotes(id_transformation);
		delStepAttributes(id_transformation);
		delSteps(id_transformation);
		delStepConditions(id_transformation);
		delStepDatabases(id_transformation);
		delTransHops(id_transformation);
		delDependencies(id_transformation);
		delTrans(id_transformation);
	}

	public void renameTransformation(long id_transformation, String newname) throws KettleDatabaseException
	{
		String sql = "UPDATE R_TRANSFORMATION SET NAME = ? WHERE ID_TRANSFORMATION = ?";

		Row table = new Row();
		table.addValue(new Value("NAME", newname));
		table.addValue(new Value("ID_TRANSFORMATION", id_transformation));

		database.execStatement(sql, table);
	}

	public void renameUser(long id_user, String newname) throws KettleDatabaseException
	{
		String sql = "UPDATE R_USER SET NAME = ? WHERE ID_USER = ?";

		Row table = new Row();
		table.addValue(new Value("NAME", newname));
		table.addValue(new Value("ID_USER", id_user));

		database.execStatement(sql, table);
	}

	public void renameProfile(long id_profile, String newname) throws KettleDatabaseException
	{
		String sql = "UPDATE R_PROFILE SET NAME = ? WHERE ID_PROFILE = ?";

		Row table = new Row();
		table.addValue(new Value("NAME", newname));
		table.addValue(new Value("ID_PROFILE", id_profile));

		database.execStatement(sql, table);
	}

	public void renameDatabase(long id_database, String newname) throws KettleDatabaseException
	{
		String sql = "UPDATE R_DATABASE SET NAME = ? WHERE ID_DATABASE = ?";

		Row table = new Row();
		table.addValue(new Value("NAME", newname));
		table.addValue(new Value("ID_DATABASE", id_database));

		database.execStatement(sql, table);
	}

	public void delAllFromJob(long id_job) throws KettleDatabaseException
	{
		// log.logBasic(toString(), "Deleting info in repository on ID_JOB: "+id_job);

		delJobNotes(id_job);
		delJobEntryAttributes(id_job);
		delJobEntries(id_job);
		delJobEntryCopies(id_job);
		delJobHops(id_job);
		delJob(id_job);

		// log.logBasic(toString(), "All deleted on job with ID_JOB: "+id_job);
	}

	public void renameJob(long id_job, String newname) throws KettleDatabaseException
	{
		String sql = "UPDATE R_JOB SET NAME = ? WHERE ID_JOB = ?";

		Row table = new Row();
		table.addValue(new Value("NAME", newname));
		table.addValue(new Value("ID_JOB", id_job));

		database.execStatement(sql, table);
	}

    /**
     * Create or upgrade repository tables & fields, populate lookup tables, ...
     * 
     * @param monitor The progress monitor to use, or null if no monitor is present.
     * @param upgrade True if you want to upgrade the repository, false if you want to create it.
     * @throws KettleDatabaseException in case something goes wrong!
     */
	public void createRepositorySchema(IProgressMonitor monitor, boolean upgrade) throws KettleDatabaseException
	{
		Row table;
		String sql;
		String tablename;
		String indexname;
		String keyfield[];
		String user[], pass[], code[], desc[], prof[];

		int KEY = 9; // integer, no need for bigint!

		log.logBasic(toString(), "Starting to create or modify the repository tables...");
		if (monitor!=null) monitor.beginTask((upgrade?"Upgrading ":"Creating")+" the Kettle repository...", 31);
        
        setAutoCommit(true);
		
		//////////////////////////////////////////////////////////////////////////////////
		// R_DATABASE_TYPE
		//
		// Create table...
		//
		boolean ok_database_type = true;
		table = new Row();
		tablename = "R_DATABASE_TYPE";
		if (monitor!=null) monitor.subTask("Checking table "+tablename);
		table.addValue(new Value("ID_DATABASE_TYPE", Value.VALUE_TYPE_INTEGER, KEY, 0));
		table.addValue(new Value("CODE",             Value.VALUE_TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
		table.addValue(new Value("DESCRIPTION",      Value.VALUE_TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
		sql = database.getDDL(tablename, table, null, false, "ID_DATABASE_TYPE", false);

		if (sql != null && sql.length() > 0)
		{
			try
			{
                log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
				database.execStatements(sql);
                log.logDetailed(toString(), "Created/altered table " + tablename);
			}
			catch (KettleDatabaseException dbe)
			{
				throw new KettleDatabaseException("Unable to create or modify table " + tablename, dbe);
			}
		}
		else
		{
            log.logDetailed(toString(), "Table " + tablename + " is OK.");
		}

		if (ok_database_type)
		{
			//
			// Populate...
			//
			code = DatabaseMeta.getDBTypeDescList();
			desc = DatabaseMeta.getDBTypeDescLongList();

			database.prepareInsert(table, tablename);

			for (int i = 1; i < code.length; i++)
			{
				Row lookup = null;
                if (upgrade) lookup = database.getOneRow("SELECT ID_DATABASE_TYPE FROM " + tablename + " WHERE CODE = '" + code[i] + "'");
				if (lookup == null)
				{
					long nextid = getNextDatabaseTypeID();

					table = new Row();
					table.addValue(new Value("ID_DATABASE_TYPE", nextid));
					table.addValue(new Value("CODE", code[i]));
					table.addValue(new Value("DESCRIPTION", desc[i]));

					database.setValuesInsert(table);
					database.insertRow();
				}
			}

			try
			{
				database.closeInsert();
				log.logDetailed(toString(), "Populated table " + tablename);
			}
			catch (KettleDatabaseException dbe)
			{
                throw new KettleDatabaseException("Unable to close insert after populating table " + tablename, dbe);
			}
		}
		if (monitor!=null) monitor.worked(1);

		//////////////////////////////////////////////////////////////////////////////////
		//
		// R_DATABASE_CONTYPE
		//
		// Create table...
		// 
		boolean ok_database_contype = true;
		table = new Row();
		tablename = "R_DATABASE_CONTYPE";
		if (monitor!=null) monitor.subTask("Checking table "+tablename);
		table.addValue(new Value("ID_DATABASE_CONTYPE", Value.VALUE_TYPE_INTEGER, KEY, 0));
		table.addValue(new Value("CODE", Value.VALUE_TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
		table.addValue(new Value("DESCRIPTION", Value.VALUE_TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
		sql = database.getDDL(tablename, table, null, false, "ID_DATABASE_CONTYPE", false);

		if (sql != null && sql.length() > 0)
		{
            log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
			database.execStatements(sql);
			log.logDetailed(toString(), "Created or altered table " + tablename);
		}
		else
		{
			log.logDetailed(toString(), "Table " + tablename + " is OK.");
		}

		if (ok_database_contype)
		{
			//
			// Populate with data...
			//
			code = DatabaseMeta.access_type_desc;
			desc = DatabaseMeta.db_access_desc_long;

			database.prepareInsert(table, tablename);

			for (int i = 0; i < code.length; i++)
			{
                Row lookup = null;
                if (upgrade) lookup = database.getOneRow("SELECT ID_DATABASE_CONTYPE FROM " + tablename + " WHERE CODE = '" + code[i] + "'");
				if (lookup == null)
				{
					long nextid = getNextDatabaseConnectionTypeID();

					table = new Row();
					table.addValue(new Value("ID_DATABASE_CONTYPE", nextid));
					table.addValue(new Value("CODE", code[i]));
					table.addValue(new Value("DESCRIPTION", desc[i]));

					database.setValuesInsert(table);
					database.insertRow();
				}
			}

            try
            {
                database.closeInsert();
                log.logDetailed(toString(), "Populated table " + tablename);
            }
            catch(KettleDatabaseException dbe)
            {
                throw new KettleDatabaseException("Unable to close insert after populating table " + tablename, dbe);
            }
		}
		if (monitor!=null) monitor.worked(1);

		//////////////////////////////////////////////////////////////////////////////////
		//
		// R_NOTE
		//
		// Create table...
		table = new Row();
		tablename = "R_NOTE";
		if (monitor!=null) monitor.subTask("Checking table "+tablename);
		table.addValue(new Value("ID_NOTE", Value.VALUE_TYPE_INTEGER, KEY, 0));
		table.addValue(new Value("VALUE_STR", Value.VALUE_TYPE_STRING, REP_STRING_LENGTH, 0));
		table.addValue(new Value("GUI_LOCATION_X", Value.VALUE_TYPE_INTEGER, 6, 0));
		table.addValue(new Value("GUI_LOCATION_Y", Value.VALUE_TYPE_INTEGER, 6, 0));
		table.addValue(new Value("GUI_LOCATION_WIDTH", Value.VALUE_TYPE_INTEGER, 6, 0));
		table.addValue(new Value("GUI_LOCATION_HEIGHT", Value.VALUE_TYPE_INTEGER, 6, 0));
		sql = database.getDDL(tablename, table, null, false, "ID_NOTE", false);
		if (sql != null && sql.length() > 0)
		{
            log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
			database.execStatements(sql);
			log.logDetailed(toString(), "Created or altered table " + tablename);
		}
		else
		{
			log.logDetailed(toString(), "Table " + tablename + " is OK.");
		}
		if (monitor!=null) monitor.worked(1);

		//////////////////////////////////////////////////////////////////////////////////
		//
		// R_DATABASE
		//
		// Create table...
		table = new Row();
		tablename = "R_DATABASE";
		if (monitor!=null) monitor.subTask("Checking table "+tablename);
		table.addValue(new Value("ID_DATABASE", Value.VALUE_TYPE_INTEGER, KEY, 0));
		table.addValue(new Value("NAME", Value.VALUE_TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
		table.addValue(new Value("ID_DATABASE_TYPE", Value.VALUE_TYPE_INTEGER, KEY, 0));
		table.addValue(new Value("ID_DATABASE_CONTYPE", Value.VALUE_TYPE_INTEGER, KEY, 0));
		table.addValue(new Value("HOST_NAME", Value.VALUE_TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
		table.addValue(new Value("DATABASE_NAME", Value.VALUE_TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
		table.addValue(new Value("PORT", Value.VALUE_TYPE_INTEGER, 7, 0));
		table.addValue(new Value("USERNAME", Value.VALUE_TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
		table.addValue(new Value("PASSWORD", Value.VALUE_TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
		table.addValue(new Value("SERVERNAME", Value.VALUE_TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
		table.addValue(new Value("DATA_TBS", Value.VALUE_TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
		table.addValue(new Value("INDEX_TBS", Value.VALUE_TYPE_STRING, REP_STRING_CODE_LENGTH, 0));

		sql = database.getDDL(tablename, table, null, false, "ID_DATABASE", false);
		if (sql != null && sql.length() > 0)
		{
            log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
			database.execStatements(sql);
			log.logDetailed(toString(), "Created or altered table " + tablename);
		}
		else
		{
			log.logDetailed(toString(), "Table " + tablename + " is OK.");
		}
		if (monitor!=null) monitor.worked(1);

        //////////////////////////////////////////////////////////////////////////////////
        //
        // R_DATABASE_ATTRIBUTE
        //
        // Create table...
        table = new Row();
        tablename = "R_DATABASE_ATTRIBUTE";
        if (monitor!=null) monitor.subTask("Checking table "+tablename);
        table.addValue(new Value("ID_DATABASE_ATTRIBUTE", Value.VALUE_TYPE_INTEGER, KEY, 0));
        table.addValue(new Value("ID_DATABASE", Value.VALUE_TYPE_INTEGER, KEY, 0));
        table.addValue(new Value("CODE", Value.VALUE_TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
        table.addValue(new Value("VALUE_STR", Value.VALUE_TYPE_STRING, REP_STRING_LENGTH, 0));

        sql = database.getDDL(tablename, table, null, false, "ID_DATABASE_ATTRIBUTE", false);
        if (sql != null && sql.length() > 0)
        {
            log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
            database.execStatements(sql);
            log.logDetailed(toString(), "Created or altered table " + tablename);
            
            try
            {
                indexname = "IDX_" + tablename.substring(2) + "_AK";
                keyfield = new String[] { "ID_DIRECTORY_PARENT", "DIRECTORY_NAME" };
                if (!database.checkIndexExists(tablename, keyfield))
                {
                    sql = database.getCreateIndexStatement(tablename, indexname, keyfield, false, true, false, false);
                    log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
                    database.execStatements(sql);
                    log.logDetailed(toString(), "Created lookup index " + indexname + " on " + tablename);
                }
            }
            catch(KettleDatabaseException kdbe)
            {
                // Ignore this one: index is not properly detected, it already exists...
            }

        }
        else
        {
            log.logDetailed(toString(), "Table " + tablename + " is OK.");
        }
        if (monitor!=null) monitor.worked(1);

		//////////////////////////////////////////////////////////////////////////////////
		//
		// R_DIRECTORY
		//
		// Create table...
		table = new Row();
		tablename = "R_DIRECTORY";
		if (monitor!=null) monitor.subTask("Checking table "+tablename);
		table.addValue(new Value("ID_DIRECTORY",        Value.VALUE_TYPE_INTEGER, KEY, 0));
		table.addValue(new Value("ID_DIRECTORY_PARENT", Value.VALUE_TYPE_INTEGER, KEY, 0));
		table.addValue(new Value("DIRECTORY_NAME",      Value.VALUE_TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
		sql = database.getDDL(tablename, table, null, false, "ID_DIRECTORY", false);

		if (sql != null && sql.length() > 0)
		{
            log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
			database.execStatements(sql);
			log.logDetailed(toString(), "Created or altered table " + tablename);
			
			try
			{
				indexname = "IDX_" + tablename.substring(2) + "_AK";
				keyfield = new String[] { "ID_DIRECTORY_PARENT", "DIRECTORY_NAME" };
				if (!database.checkIndexExists(tablename, keyfield))
				{
					sql = database.getCreateIndexStatement(tablename, indexname, keyfield, false, true, false, false);
                    log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
					database.execStatements(sql);
					log.logDetailed(toString(), "Created lookup index " + indexname + " on " + tablename);
				}
			}
			catch(KettleDatabaseException kdbe)
			{
				// Ignore this one: index is not properly detected, it already exists...
			}

		}
		else
		{
			log.logDetailed(toString(), "Table " + tablename + " is OK.");
		}
		if (monitor!=null) monitor.worked(1);

		//////////////////////////////////////////////////////////////////////////////////
		//
		// R_TRANSFORMATION
		//
		// Create table...
		table = new Row();
		tablename = "R_TRANSFORMATION";
		if (monitor!=null) monitor.subTask("Checking table "+tablename);
		table.addValue(new Value("ID_TRANSFORMATION", Value.VALUE_TYPE_INTEGER, KEY, 0));
		table.addValue(new Value("ID_DIRECTORY", Value.VALUE_TYPE_INTEGER, KEY, 0));
		table.addValue(new Value("NAME", Value.VALUE_TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
		table.addValue(new Value("ID_STEP_READ", Value.VALUE_TYPE_INTEGER, KEY, 0));
		table.addValue(new Value("ID_STEP_WRITE", Value.VALUE_TYPE_INTEGER, KEY, 0));
		table.addValue(new Value("ID_STEP_INPUT", Value.VALUE_TYPE_INTEGER, KEY, 0));
		table.addValue(new Value("ID_STEP_OUTPUT", Value.VALUE_TYPE_INTEGER, KEY, 0));
		table.addValue(new Value("ID_STEP_UPDATE", Value.VALUE_TYPE_INTEGER, KEY, 0));
		table.addValue(new Value("ID_DATABASE_LOG", Value.VALUE_TYPE_INTEGER, KEY, 0));
		table.addValue(new Value("TABLE_NAME_LOG", Value.VALUE_TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
		table.addValue(new Value("USE_BATCHID", Value.VALUE_TYPE_BOOLEAN, 1, 0));
		table.addValue(new Value("USE_LOGFIELD", Value.VALUE_TYPE_BOOLEAN, 1, 0));
		table.addValue(new Value("ID_DATABASE_MAXDATE", Value.VALUE_TYPE_INTEGER, KEY, 0));
		table.addValue(new Value("TABLE_NAME_MAXDATE", Value.VALUE_TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
		table.addValue(new Value("FIELD_NAME_MAXDATE", Value.VALUE_TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
		table.addValue(new Value("OFFSET_MAXDATE", Value.VALUE_TYPE_NUMBER, 12, 2));
		table.addValue(new Value("DIFF_MAXDATE", Value.VALUE_TYPE_NUMBER, 12, 2));
		table.addValue(new Value("MODIFIED_USER", Value.VALUE_TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
		table.addValue(new Value("MODIFIED_DATE", Value.VALUE_TYPE_DATE, 20, 0));
		table.addValue(new Value("SIZE_ROWSET", Value.VALUE_TYPE_INTEGER, KEY, 0));
		sql = database.getDDL(tablename, table, null, false, "ID_TRANSFORMATION", false);

		if (sql != null && sql.length() > 0)
		{
            log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
			database.execStatements(sql);
			log.logDetailed(toString(), "Created or altered table " + tablename);
		}
		else
		{
			log.logDetailed(toString(), "Table " + tablename + " is OK.");
		}

		// In case of an update, the added column R_TRANSFORMATION.ID_DIRECTORY == NULL!!!
		database.execStatement("UPDATE " + tablename + " SET ID_DIRECTORY=0 WHERE ID_DIRECTORY IS NULL");

		if (monitor!=null) monitor.worked(1);

		//////////////////////////////////////////////////////////////////////////////////
		//
		// R_TRANS_ATTRIBUTE
		//
		// Create table...
		table = new Row();
		tablename = "R_TRANS_ATTRIBUTE";
		if (monitor!=null) monitor.subTask("Checking table "+tablename);
		table.addValue(new Value("ID_TRANS_ATTRIBUTE", Value.VALUE_TYPE_INTEGER, KEY, 0));
		table.addValue(new Value("ID_TRANSFORMATION", Value.VALUE_TYPE_INTEGER, KEY, 0));
		table.addValue(new Value("NR", Value.VALUE_TYPE_INTEGER, 6, 0));
		table.addValue(new Value("CODE", Value.VALUE_TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
		table.addValue(new Value("VALUE_NUM", Value.VALUE_TYPE_NUMBER, 13, 2));
		table.addValue(new Value("VALUE_STR", Value.VALUE_TYPE_STRING, REP_STRING_LENGTH, 0));
		sql = database.getDDL(tablename, table, null, false, "ID_TRANS_ATTRIBUTE", false);

		if (sql != null && sql.length() > 0)
		{
            log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
			database.execStatements(sql);
			log.logDetailed(toString(), "Created or altered table " + tablename);

			try
			{
				indexname = "IDX_TRANS_ATTRIBUTE_LOOKUP";
				keyfield = new String[] { "ID_TRANSFORMATION", "CODE", "NR" };

				if (!database.checkIndexExists(tablename, keyfield))
				{
					sql = database.getCreateIndexStatement(tablename, indexname, keyfield, false, true, false, false);
	
                    log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
					database.execStatements(sql);
					log.logDetailed(toString(), "Created lookup index " + indexname + " on " + tablename);
				}
			}
			catch(KettleDatabaseException kdbe)
			{
				// Ignore this one: index is not properly detected, it already exists...
			}
		}
		else
		{
			log.logDetailed(toString(), "Table " + tablename + " is OK.");
		}
		if (monitor!=null) monitor.worked(1);

		//////////////////////////////////////////////////////////////////////////////////
		//
		// R_DEPENDENCY
		//
		// Create table...
		table = new Row();
		tablename = "R_DEPENDENCY";
		if (monitor!=null) monitor.subTask("Checking table "+tablename);
		table.addValue(new Value("ID_DEPENDENCY", Value.VALUE_TYPE_INTEGER, KEY, 0));
		table.addValue(new Value("ID_TRANSFORMATION", Value.VALUE_TYPE_INTEGER, KEY, 0));
		table.addValue(new Value("ID_DATABASE", Value.VALUE_TYPE_INTEGER, KEY, 0));
		table.addValue(new Value("TABLE_NAME", Value.VALUE_TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
		table.addValue(new Value("FIELD_NAME", Value.VALUE_TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
		sql = database.getDDL(tablename, table, null, false, "ID_DEPENDENCY", false);

		if (sql != null && sql.length() > 0)
		{
            log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
			database.execStatements(sql);
			log.logDetailed(toString(), "Created or altered table " + tablename);
		}
		else
		{
			log.logDetailed(toString(), "Table " + tablename + " is OK.");
		}
		if (monitor!=null) monitor.worked(1);

		//
		// R_TRANS_HOP
		//
		// Create table...
		table = new Row();
		tablename = "R_TRANS_HOP";
		if (monitor!=null) monitor.subTask("Checking table "+tablename);
		table.addValue(new Value("ID_TRANS_HOP", Value.VALUE_TYPE_INTEGER, KEY, 0));
		table.addValue(new Value("ID_TRANSFORMATION", Value.VALUE_TYPE_INTEGER, KEY, 0));
		table.addValue(new Value("ID_STEP_FROM", Value.VALUE_TYPE_INTEGER, KEY, 0));
		table.addValue(new Value("ID_STEP_TO", Value.VALUE_TYPE_INTEGER, KEY, 0));
		table.addValue(new Value("ENABLED", Value.VALUE_TYPE_BOOLEAN, 1, 0));
		sql = database.getDDL(tablename, table, null, false, "ID_TRANS_HOP", false);

		if (sql != null && sql.length() > 0)
		{
            log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
			database.execStatements(sql);
			log.logDetailed(toString(), "Created or altered table " + tablename);
		}
		else
		{
			log.logDetailed(toString(), "Table " + tablename + " is OK.");
		}
		if (monitor!=null) monitor.worked(1);

		///////////////////////////////////////////////////////////////////////////////
		// R_TRANS_STEP_CONDITION
		//
		table = new Row();
		tablename = "R_TRANS_STEP_CONDITION";
		if (monitor!=null) monitor.subTask("Checking table "+tablename);
		table.addValue(new Value("ID_TRANSFORMATION", Value.VALUE_TYPE_INTEGER, KEY, 0));
		table.addValue(new Value("ID_STEP", Value.VALUE_TYPE_INTEGER, KEY, 0));
		table.addValue(new Value("ID_CONDITION", Value.VALUE_TYPE_INTEGER, KEY, 0));
		sql = database.getDDL(tablename, table, null, false, null, false);

		if (sql != null && sql.length() > 0) // Doesn't exists: create the table...
		{
            log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
			database.execStatements(sql);
			log.logDetailed(toString(), "Created or altered table " + tablename);
		}
		else
		{
			log.logDetailed(toString(), "Table " + tablename + " is OK.");
		}
		if (monitor!=null) monitor.worked(1);

		///////////////////////////////////////////////////////////////////////////////
		// R_CONDITION
		//
		table = new Row();
		tablename = "R_CONDITION";
		if (monitor!=null) monitor.subTask("Checking table "+tablename);
		table.addValue(new Value("ID_CONDITION", Value.VALUE_TYPE_INTEGER, KEY, 0));
		table.addValue(new Value("ID_CONDITION_PARENT", Value.VALUE_TYPE_INTEGER, KEY, 0));
		table.addValue(new Value("NEGATED", Value.VALUE_TYPE_BOOLEAN, 1, 0));
		table.addValue(new Value("OPERATOR", Value.VALUE_TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
		table.addValue(new Value("LEFT_NAME", Value.VALUE_TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
		table.addValue(new Value("CONDITION_FUNCTION", Value.VALUE_TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
		table.addValue(new Value("RIGHT_NAME", Value.VALUE_TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
		table.addValue(new Value("ID_VALUE_RIGHT", Value.VALUE_TYPE_INTEGER, KEY, 0));
		sql = database.getDDL(tablename, table, null, false, "ID_CONDITION", false);

		if (sql != null && sql.length() > 0) // Doesn't exist: create the table...
		{
            log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
			database.execStatements(sql);
			log.logDetailed(toString(), "Created or altered table " + tablename);
		}
		else
		{
			log.logDetailed(toString(), "Table " + tablename + " is OK.");
		}
		if (monitor!=null) monitor.worked(1);

		///////////////////////////////////////////////////////////////////////////////
		// R_VALUE
		//
		tablename = "R_VALUE";
		if (monitor!=null) monitor.subTask("Checking table "+tablename);
		table = new Row();
		table.addValue(new Value("ID_VALUE", Value.VALUE_TYPE_INTEGER, KEY, 0));
		table.addValue(new Value("NAME", Value.VALUE_TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
		table.addValue(new Value("VALUE_TYPE", Value.VALUE_TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
		table.addValue(new Value("VALUE_STR", Value.VALUE_TYPE_STRING, REP_STRING_LENGTH, 0));
		table.addValue(new Value("IS_NULL", Value.VALUE_TYPE_BOOLEAN, 1, 0));
		sql = database.getDDL(tablename, table, null, false, "ID_VALUE", false);

		if (sql != null && sql.length() > 0) // Doesn't exists: create the table...
		{
            log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
			database.execStatements(sql);
			log.logDetailed(toString(), "Created or altered table " + tablename);
		}
		else
		{
			log.logDetailed(toString(), "Table " + tablename + " is OK.");
		}
		if (monitor!=null) monitor.worked(1);

		//////////////////////////////////////////////////////////////////////////////////
		//
		// R_STEP_TYPE
		//
		// Create table...
		boolean ok_step_type = true;
		table = new Row();
		tablename = "R_STEP_TYPE";
		if (monitor!=null) monitor.subTask("Checking table "+tablename);
		table.addValue(new Value("ID_STEP_TYPE", Value.VALUE_TYPE_INTEGER, KEY, 0));
		table.addValue(new Value("CODE", Value.VALUE_TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
		table.addValue(new Value("DESCRIPTION", Value.VALUE_TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
		table.addValue(new Value("HELPTEXT", Value.VALUE_TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
		sql = database.getDDL(tablename, table, null, false, "ID_STEP_TYPE", false);

		if (sql != null && sql.length() > 0) // Doesn't exists: create the table...
		{
            log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
			database.execStatements(sql);
			log.logDetailed(toString(), "Created or altered table " + tablename);
		}
		else
		{
			log.logDetailed(toString(), "Table " + tablename + " is OK.");
		}

		if (ok_step_type)
		{
			updateStepTypes();
			log.logDetailed(toString(), "Populated table " + tablename);
		}
		if (monitor!=null) monitor.worked(1);

		//////////////////////////////////////////////////////////////////////////////////
		//
		// R_STEP
		//
		// Create table
		table = new Row();
		tablename = "R_STEP";
		if (monitor!=null) monitor.subTask("Checking table "+tablename);
		table.addValue(new Value("ID_STEP", Value.VALUE_TYPE_INTEGER, KEY, 0));
		table.addValue(new Value("ID_TRANSFORMATION", Value.VALUE_TYPE_INTEGER, KEY, 0));
		table.addValue(new Value("NAME", Value.VALUE_TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
		table.addValue(new Value("DESCRIPTION", Value.VALUE_TYPE_STRING, REP_STRING_LENGTH, 0));
		table.addValue(new Value("ID_STEP_TYPE", Value.VALUE_TYPE_INTEGER, KEY, 0));
		table.addValue(new Value("DISTRIBUTE", Value.VALUE_TYPE_BOOLEAN, 1, 0));
		table.addValue(new Value("COPIES", Value.VALUE_TYPE_INTEGER, 3, 0));
		table.addValue(new Value("GUI_LOCATION_X", Value.VALUE_TYPE_INTEGER, 6, 0));
		table.addValue(new Value("GUI_LOCATION_Y", Value.VALUE_TYPE_INTEGER, 6, 0));
		table.addValue(new Value("GUI_DRAW", Value.VALUE_TYPE_BOOLEAN, 1, 0));
		sql = database.getDDL(tablename, table, null, false, "ID_STEP", false);

		if (sql != null && sql.length() > 0) // Doesn't exists: create the table...
		{
            log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
			database.execStatements(sql);
			log.logDetailed(toString(), "Created or altered table " + tablename);
		}
		else
		{
			log.logDetailed(toString(), "Table " + tablename + " is OK.");
		}
		if (monitor!=null) monitor.worked(1);

		//////////////////////////////////////////////////////////////////////////////////
		//
		// R_STEP_ATTRIBUTE
		//
		// Create table...
		tablename = "R_STEP_ATTRIBUTE";
		if (monitor!=null) monitor.subTask("Checking table "+tablename);
		table = new Row();
		table.addValue(new Value("ID_STEP_ATTRIBUTE", Value.VALUE_TYPE_INTEGER, KEY, 0));
		table.addValue(new Value("ID_TRANSFORMATION", Value.VALUE_TYPE_INTEGER, KEY, 0));
		table.addValue(new Value("ID_STEP", Value.VALUE_TYPE_INTEGER, KEY, 0));
		table.addValue(new Value("NR", Value.VALUE_TYPE_INTEGER, 6, 0));
		table.addValue(new Value("CODE", Value.VALUE_TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
		table.addValue(new Value("VALUE_NUM", Value.VALUE_TYPE_NUMBER, 13, 2));
		table.addValue(new Value("VALUE_STR", Value.VALUE_TYPE_STRING, REP_STRING_LENGTH, 0));
		sql = database.getDDL(tablename, table, null, false, "ID_STEP_ATTRIBUTE", false);

		if (sql != null && sql.length() > 0) // Doesn't exist: create the table...
		{
            log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
			database.execStatements(sql);
			log.logDetailed(toString(), "Created or altered table " + tablename);

			try
			{
				indexname = "IDX_" + tablename.substring(2) + "_LOOKUP";
				keyfield = new String[] { "ID_STEP", "CODE", "NR" };
				if (!database.checkIndexExists(tablename, keyfield))
				{
					sql = database.getCreateIndexStatement(tablename, indexname, keyfield, false, true, false, false);
                    log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
					database.execStatements(sql);
					log.logDetailed(toString(), "Created lookup index " + indexname + " on " + tablename);
				}
			}
			catch(KettleDatabaseException kdbe)
			{
				// Ignore this one: index is not properly detected, it already exists...
			}
		}
		else
		{
			log.logDetailed(toString(), "Table " + tablename + " is OK.");
		}
		if (monitor!=null) monitor.worked(1);

		//////////////////////////////////////////////////////////////////////////////////
		//
		// R_STEP_DATABASE
		//
		// Keeps the links between transformation steps and databases.
		// That way investigating dependencies becomes easier to program.
		//
		// Create table...
		tablename = "R_STEP_DATABASE";
		if (monitor!=null) monitor.subTask("Checking table "+tablename);
		table = new Row();
		table.addValue(new Value("ID_TRANSFORMATION", Value.VALUE_TYPE_INTEGER, KEY, 0));
		table.addValue(new Value("ID_STEP", Value.VALUE_TYPE_INTEGER, KEY, 0));
		table.addValue(new Value("ID_DATABASE", Value.VALUE_TYPE_INTEGER, KEY, 0));
		sql = database.getDDL(tablename, table, null, false, null, false);
		if (sql != null && sql.length() > 0) // Doesn't exist: create the table...
		{
            log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
			database.execStatements(sql);
			log.logDetailed(toString(), "Created or altered table " + tablename);

			try
			{
				indexname = "IDX_" + tablename.substring(2) + "_LU1";
				keyfield = new String[] { "ID_TRANSFORMATION" };
				if (!database.checkIndexExists(tablename, keyfield))
				{
					sql = database.getCreateIndexStatement(tablename, indexname, keyfield, false, false, false, false);
                    log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
					database.execStatements(sql);
					log.logDetailed(toString(), "Created lookup index " + indexname + " on " + tablename);
				}
			}
			catch(KettleDatabaseException kdbe)
			{
				// Ignore this one: index is not properly detected, it already exists...
			}

			try
			{
				indexname = "IDX_" + tablename.substring(2) + "_LU2";
				keyfield = new String[] { "ID_DATABASE" };
				if (!database.checkIndexExists(tablename, keyfield))
				{
					sql = database.getCreateIndexStatement(tablename, indexname, keyfield, false, false, false, false);
                    log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
					database.execStatements(sql);
					log.logDetailed(toString(), "Created lookup index " + indexname + " on " + tablename);
				}
			}
			catch(KettleDatabaseException kdbe)
			{
				// Ignore this one: index is not properly detected, it already exists...
			}
		}
		else
		{
			log.logDetailed(toString(), "Table " + tablename + " is OK.");
		}
		if (monitor!=null) monitor.worked(1);

		//////////////////////////////////////////////////////////////////////////////////
		//
		// R_TRANS_NOTE
		//
		// Create table...
		table = new Row();
		tablename = "R_TRANS_NOTE";
		if (monitor!=null) monitor.subTask("Checking table "+tablename);
		table.addValue(new Value("ID_TRANSFORMATION", Value.VALUE_TYPE_INTEGER, KEY, 0));
		table.addValue(new Value("ID_NOTE", Value.VALUE_TYPE_INTEGER, KEY, 0));
		sql = database.getDDL(tablename, table, null, false, null, false);

		if (sql != null && sql.length() > 0) // Doesn't exist: create the table...
		{
            log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
			database.execStatements(sql);
			log.logDetailed(toString(), "Created or altered table " + tablename);
		}
		else
		{
			log.logDetailed(toString(), "Table " + tablename + " is OK.");
		}
		if (monitor!=null) monitor.worked(1);

		//////////////////////////////////////////////////////////////////////////////////
		//
		// R_LOGLEVEL
		//
		// Create table...
		boolean ok_loglevel = true;
		tablename = "R_LOGLEVEL";
		if (monitor!=null) monitor.subTask("Checking table "+tablename);
		table = new Row();
		table.addValue(new Value("ID_LOGLEVEL", Value.VALUE_TYPE_INTEGER, KEY, 0));
		table.addValue(new Value("CODE", Value.VALUE_TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
		table.addValue(new Value("DESCRIPTION", Value.VALUE_TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
		sql = database.getDDL(tablename, table, null, false, "ID_LOGLEVEL", false);

		if (sql != null && sql.length() > 0) // Doesn't exist: create the table...
		{
            log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
			database.execStatements(sql);
			log.logDetailed(toString(), "Created or altered table " + tablename);
		}
		else
		{
			log.logDetailed(toString(), "Table " + tablename + " is OK.");
		}

		if (ok_loglevel)
		{
			//
			// Populate with data...
			//
			code = LogWriter.logLevelDescription;
			desc = LogWriter.log_level_desc_long;

			database.prepareInsert(table, tablename);

			for (int i = 1; i < code.length; i++)
			{
                Row lookup = null;
                if (upgrade) lookup = database.getOneRow("SELECT ID_LOGLEVEL FROM " + tablename + " WHERE CODE = '" + code[i] + "'");
				if (lookup == null)
				{
					long nextid = getNextLoglevelID();

					table = new Row();
					table.addValue(new Value("ID_LOGLEVEL", nextid));
					table.addValue(new Value("CODE", code[i]));
					table.addValue(new Value("DESCRIPTION", desc[i]));

					database.setValuesInsert(table);
					database.insertRow();
				}
			}
            
            try
            {
                database.closeInsert();
                log.logDetailed(toString(), "Populated table " + tablename);
            }
            catch(KettleDatabaseException dbe)
            {
                throw new KettleDatabaseException("Unable to close insert after populating table " + tablename, dbe);
            }
		}
		if (monitor!=null) monitor.worked(1);

		//////////////////////////////////////////////////////////////////////////////////
		//
		// R_LOG
		//
		// Create table...
		table = new Row();
		tablename = "R_LOG";
		if (monitor!=null) monitor.subTask("Checking table "+tablename);
		table.addValue(new Value("ID_LOG", Value.VALUE_TYPE_INTEGER, KEY, 0));
		table.addValue(new Value("NAME", Value.VALUE_TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
		table.addValue(new Value("ID_LOGLEVEL", Value.VALUE_TYPE_INTEGER, KEY, 0));
		table.addValue(new Value("LOGTYPE", Value.VALUE_TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
		table.addValue(new Value("FILENAME", Value.VALUE_TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
		table.addValue(new Value("FILEEXTENTION", Value.VALUE_TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
		table.addValue(new Value("ADD_DATE", Value.VALUE_TYPE_BOOLEAN, 1, 0));
		table.addValue(new Value("ADD_TIME", Value.VALUE_TYPE_BOOLEAN, 1, 0));
		table.addValue(new Value("ID_DATABASE_LOG", Value.VALUE_TYPE_INTEGER, KEY, 0));
		table.addValue(new Value("TABLE_NAME_LOG", Value.VALUE_TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
		sql = database.getDDL(tablename, table, null, false, "ID_LOG", false);

		if (sql != null && sql.length() > 0) // Doesn't exist: create the table...
		{
            log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
			database.execStatements(sql);
			log.logDetailed(toString(), "Created or altered table " + tablename);
		}
		else
		{
			log.logDetailed(toString(), "Table " + tablename + " is OK.");
		}
		if (monitor!=null) monitor.worked(1);

		//////////////////////////////////////////////////////////////////////////////////
		//
		// R_JOB
		//
		// Create table...
		table = new Row();
		tablename = "R_JOB";
		if (monitor!=null) monitor.subTask("Checking table "+tablename);
		table.addValue(new Value("ID_JOB", Value.VALUE_TYPE_INTEGER, KEY, 0));
		table.addValue(new Value("ID_DIRECTORY", Value.VALUE_TYPE_INTEGER, KEY, 0));
		table.addValue(new Value("NAME", Value.VALUE_TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
		table.addValue(new Value("ID_DATABASE_LOG", Value.VALUE_TYPE_INTEGER, KEY, 0));
		table.addValue(new Value("TABLE_NAME_LOG", Value.VALUE_TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
		table.addValue(new Value("MODIFIED_USER", Value.VALUE_TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
		table.addValue(new Value("MODIFIED_DATE", Value.VALUE_TYPE_DATE, 20, 0));
        table.addValue(new Value("USE_BATCH_ID", Value.VALUE_TYPE_BOOLEAN, 0, 0));
        table.addValue(new Value("PASS_BATCH_ID", Value.VALUE_TYPE_BOOLEAN, 0, 0));
        table.addValue(new Value("USE_LOGFIELD", Value.VALUE_TYPE_BOOLEAN, 0, 0));
		sql = database.getDDL(tablename, table, null, false, "ID_JOB", false);

		if (sql != null && sql.length() > 0) // Doesn't exist: create the table...
		{
            log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
			database.execStatements(sql);
			log.logDetailed(toString(), "Created or altered table " + tablename);
		}
		else
		{
			log.logDetailed(toString(), "Table " + tablename + " is OK.");
		}
		if (monitor!=null) monitor.worked(1);

		//////////////////////////////////////////////////////////////////////////////////
		//
		// R_JOBENTRY_TYPE
		//
		// Create table...
		boolean ok_jobentry_type = true;
		table = new Row();
		tablename = "R_JOBENTRY_TYPE";
		if (monitor!=null) monitor.subTask("Checking table "+tablename);
		table.addValue(new Value("ID_JOBENTRY_TYPE", Value.VALUE_TYPE_INTEGER, KEY, 0));
		table.addValue(new Value("CODE", Value.VALUE_TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
		table.addValue(new Value("DESCRIPTION", Value.VALUE_TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
		sql = database.getDDL(tablename, table, null, false, "ID_JOBENTRY_TYPE", false);

		if (sql != null && sql.length() > 0) // Doesn't exist: create the table...
		{
            log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
			database.execStatements(sql);
			log.logDetailed(toString(), "Created or altered table " + tablename);
		}
		else
		{
			log.logDetailed(toString(), "Table " + tablename + " is OK.");
		}

		if (ok_jobentry_type)
		{
			//
			// Populate with data...
			//
			updateJobEntryTypes();
			log.logDetailed(toString(), "Populated table " + tablename);
		}
		if (monitor!=null) monitor.worked(1);

		//////////////////////////////////////////////////////////////////////////////////
		//
		// R_JOBENTRY
		//
		// Create table...
		table = new Row();
		tablename = "R_JOBENTRY";
		if (monitor!=null) monitor.subTask("Checking table "+tablename);
		table.addValue(new Value("ID_JOBENTRY", Value.VALUE_TYPE_INTEGER, KEY, 0));
		table.addValue(new Value("ID_JOB", Value.VALUE_TYPE_INTEGER, KEY, 0));
		table.addValue(new Value("ID_JOBENTRY_TYPE", Value.VALUE_TYPE_INTEGER, KEY, 0));
		table.addValue(new Value("NAME", Value.VALUE_TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
		table.addValue(new Value("DESCRIPTION", Value.VALUE_TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
		sql = database.getDDL(tablename, table, null, false, "ID_JOBENTRY", false);

		if (sql != null && sql.length() > 0) // Doesn't exist: create the table...
		{
            log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
			database.execStatements(sql);
			log.logDetailed(toString(), "Created or altered table " + tablename);
		}
		else
		{
			log.logDetailed(toString(), "Table " + tablename + " is OK.");
		}
		if (monitor!=null) monitor.worked(1);

		//////////////////////////////////////////////////////////////////////////////////
		//
		// R_JOBENTRY_COPY
		//
		// Create table...
		table = new Row();
		tablename = "R_JOBENTRY_COPY";
		if (monitor!=null) monitor.subTask("Checking table "+tablename);
		table.addValue(new Value("ID_JOBENTRY_COPY", Value.VALUE_TYPE_INTEGER, KEY, 0));
		table.addValue(new Value("ID_JOBENTRY", Value.VALUE_TYPE_INTEGER, KEY, 0));
		table.addValue(new Value("ID_JOB", Value.VALUE_TYPE_INTEGER, KEY, 0));
		table.addValue(new Value("ID_JOBENTRY_TYPE", Value.VALUE_TYPE_INTEGER, KEY, 0));
		table.addValue(new Value("NR", Value.VALUE_TYPE_INTEGER, 4, 0));
		table.addValue(new Value("GUI_LOCATION_X", Value.VALUE_TYPE_INTEGER, 6, 0));
		table.addValue(new Value("GUI_LOCATION_Y", Value.VALUE_TYPE_INTEGER, 6, 0));
		table.addValue(new Value("GUI_DRAW", Value.VALUE_TYPE_BOOLEAN, 1, 0));
		table.addValue(new Value("PARALLEL", Value.VALUE_TYPE_BOOLEAN, 1, 0));
		sql = database.getDDL(tablename, table, null, false, "ID_JOBENTRY_COPY", false);

		if (sql != null && sql.length() > 0) // Doesn't exist: create the table...
		{
            log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
			database.execStatements(sql);
			log.logDetailed(toString(), "Created or altered table " + tablename);
		}
		else
		{
			log.logDetailed(toString(), "Table " + tablename + " is OK.");
		}
		if (monitor!=null) monitor.worked(1);

		//////////////////////////////////////////////////////////////////////////////////
		//
		// R_JOBENTRY_ATTRIBUTE
		//
		// Create table...
		table = new Row();
		tablename = "R_JOBENTRY_ATTRIBUTE";
		if (monitor!=null) monitor.subTask("Checking table "+tablename);
		table.addValue(new Value("ID_JOBENTRY_ATTRIBUTE", Value.VALUE_TYPE_INTEGER, KEY, 0));
		table.addValue(new Value("ID_JOB", Value.VALUE_TYPE_INTEGER, KEY, 0));
		table.addValue(new Value("ID_JOBENTRY", Value.VALUE_TYPE_INTEGER, KEY, 0));
		table.addValue(new Value("NR", Value.VALUE_TYPE_INTEGER, 6, 0));
		table.addValue(new Value("CODE", Value.VALUE_TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
		table.addValue(new Value("VALUE_NUM", Value.VALUE_TYPE_NUMBER, 13, 2));
		table.addValue(new Value("VALUE_STR", Value.VALUE_TYPE_STRING, REP_STRING_LENGTH, 0));
		sql = database.getDDL(tablename, table, null, false, "ID_JOBENTRY_ATTRIBUTE", false);

		if (sql != null && sql.length() > 0) // Doesn't exist: create the table...
		{
            log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
			database.execStatements(sql);
			log.logDetailed(toString(), "Created or altered table " + tablename);

			try
			{
				indexname = "IDX_" + tablename.substring(2) + "_LOOKUP";
				keyfield = new String[] { "ID_JOBENTRY_ATTRIBUTE", "CODE", "NR" };
	
				if (!database.checkIndexExists(tablename, keyfield))
				{
					sql = database.getCreateIndexStatement(tablename, indexname, keyfield, false, true, false, false);
	
                    log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
					database.execStatements(sql);
					log.logDetailed(toString(), "Created lookup index " + indexname + " on " + tablename);
				}
			}
			catch(KettleDatabaseException kdbe)
			{
				// Ignore this one: index is not properly detected, it already exists...
			}
		}
		else
		{
			log.logDetailed(toString(), "Table " + tablename + " is OK.");
		}
		if (monitor!=null) monitor.worked(1);

		//////////////////////////////////////////////////////////////////////////////////
		//
		// R_JOB_HOP
		//
		// Create table...
		table = new Row();
		tablename = "R_JOB_HOP";
		if (monitor!=null) monitor.subTask("Checking table "+tablename);
		table.addValue(new Value("ID_JOB_HOP", Value.VALUE_TYPE_INTEGER, KEY, 0));
		table.addValue(new Value("ID_JOB", Value.VALUE_TYPE_INTEGER, KEY, 0));
		table.addValue(new Value("ID_JOBENTRY_COPY_FROM", Value.VALUE_TYPE_INTEGER, KEY, 0));
		table.addValue(new Value("ID_JOBENTRY_COPY_TO", Value.VALUE_TYPE_INTEGER, KEY, 0));
		table.addValue(new Value("ENABLED", Value.VALUE_TYPE_BOOLEAN, 1, 0));
		table.addValue(new Value("EVALUATION", Value.VALUE_TYPE_BOOLEAN, 1, 0));
		table.addValue(new Value("UNCONDITIONAL", Value.VALUE_TYPE_BOOLEAN, 1, 0));

		sql = database.getDDL(tablename, table, null, false, "ID_JOB_HOP", false);

		if (sql != null && sql.length() > 0) // Doesn't exist: create the table...
		{
            log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
			database.execStatements(sql);
			log.logDetailed(toString(), "Created or altered table " + tablename);
		}
		else
		{
			log.logDetailed(toString(), "Table " + tablename + " is OK.");
		}
		if (monitor!=null) monitor.worked(1);

		//////////////////////////////////////////////////////////////////////////////////
		//
		// R_JOB_NOTE
		//
		// Create table...
		table = new Row();
		tablename = "R_JOB_NOTE";
		if (monitor!=null) monitor.subTask("Checking table "+tablename);
		table.addValue(new Value("ID_JOB", Value.VALUE_TYPE_INTEGER, KEY, 0));
		table.addValue(new Value("ID_NOTE", Value.VALUE_TYPE_INTEGER, KEY, 0));
		sql = database.getDDL(tablename, table, null, false, null, false);

		if (sql != null && sql.length() > 0) // Doesn't exist: create the table...
		{
            log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
			database.execStatements(sql);
			log.logDetailed(toString(), "Created or altered table " + tablename);
		}
		else
		{
			log.logDetailed(toString(), "Table " + tablename + " is OK.");
		}
		if (monitor!=null) monitor.worked(1);

		///////////////////////////////////////////////////////////////////////////////////
		//
		//  User tables...
		//
		///////////////////////////////////////////////////////////////////////////////////

		//////////////////////////////////////////////////////////////////////////////////
		//
		// R_PROFILE
		//
		// Create table...
        Map profiles = new Hashtable();
        
		boolean ok_profile = true;
		tablename = "R_PROFILE";
		if (monitor!=null) monitor.subTask("Checking table "+tablename);
		table = new Row();
		table.addValue(new Value("ID_PROFILE", Value.VALUE_TYPE_INTEGER, KEY, 0));
		table.addValue(new Value("NAME", Value.VALUE_TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
		table.addValue(new Value("DESCRIPTION", Value.VALUE_TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
		sql = database.getDDL(tablename, table, null, false, "ID_PROFILE", false);

		if (sql != null && sql.length() > 0) // Doesn't exist: create the table...
		{
            log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
			database.execStatements(sql);
			log.logDetailed(toString(), "Created or altered table " + tablename);
		}
		else
		{
			log.logDetailed(toString(), "Table " + tablename + " is OK.");
		}

		if (ok_profile)
		{
			//
			// Populate with data...
			//
			code = new String[] { "Administrator", "User", "Read-only" };
			desc = new String[] { "Administrator profile, manage users", "Normal user, all tools", "Read-only users" };

			database.prepareInsert(table, tablename);

			for (int i = 0; i < code.length; i++)
			{
                Row lookup = null;
                if (upgrade) lookup = database.getOneRow("SELECT ID_PROFILE FROM " + tablename + " WHERE NAME = '" + code[i] + "'");
				if (lookup == null)
				{
					long nextid = getNextProfileID();

					table = new Row();
					table.addValue(new Value("ID_PROFILE", nextid));
					table.addValue(new Value("NAME", code[i]));
					table.addValue(new Value("DESCRIPTION", desc[i]));

					database.setValuesInsert(table);
					database.insertRow();
                    log.logDetailed(toString(), "Inserted new row into table "+tablename+" : "+table);
                    profiles.put(code[i], new Long(nextid));
				}
			}

            try
            {
                database.closeInsert();
                log.logDetailed(toString(), "Populated table " + tablename);
            }
            catch(KettleDatabaseException dbe)
            {
                throw new KettleDatabaseException("Unable to close insert after populating table " + tablename, dbe);
            }
		}
		if (monitor!=null) monitor.worked(1);

		//////////////////////////////////////////////////////////////////////////////////
		//
		// R_USER
		//
		// Create table...
        Map users = new Hashtable();
		boolean ok_user = true;
		table = new Row();
		tablename = "R_USER";
		if (monitor!=null) monitor.subTask("Checking table "+tablename);
		table.addValue(new Value("ID_USER", Value.VALUE_TYPE_INTEGER, KEY, 0));
		table.addValue(new Value("ID_PROFILE", Value.VALUE_TYPE_INTEGER, KEY, 0));
		table.addValue(new Value("LOGIN", Value.VALUE_TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
		table.addValue(new Value("PASSWORD", Value.VALUE_TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
		table.addValue(new Value("NAME", Value.VALUE_TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
		table.addValue(new Value("DESCRIPTION", Value.VALUE_TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
		table.addValue(new Value("ENABLED", Value.VALUE_TYPE_BOOLEAN, 1, 0));
		sql = database.getDDL(tablename, table, null, false, "ID_USER", false);

		if (sql != null && sql.length() > 0) // Doesn't exist: create the table...
		{
            log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
			database.execStatements(sql);
			log.logDetailed(toString(), "Created or altered table " + tablename);
		}
		else
		{
			log.logDetailed(toString(), "Table " + tablename + " is OK.");
		}

		if (ok_user)
		{
			//
			// Populate with data...
			//
			user = new String[] { "admin", "guest" };
			pass = new String[] { "admin", "guest" };
			code = new String[] { "Administrator", "Guest account" };
			desc = new String[] { "User manager", "Read-only guest account" };
			prof = new String[] { "Administrator", "Read-only" };

			database.prepareInsert(table, tablename);

			for (int i = 0; i < user.length; i++)
			{
                Row lookup = null;
                if (upgrade) lookup = database.getOneRow("SELECT ID_USER FROM " + tablename + " WHERE LOGIN = '" + user[i] + "'");
				if (lookup == null)
				{
					long nextid = getNextUserID();
					String password = Encr.encryptPassword(pass[i]);
                    
                    Long profileID = (Long)profiles.get( prof[i] );
                    long id_profile = -1L;
                    if (profileID!=null) id_profile = profileID.longValue();
                    
					table = new Row();
					table.addValue(new Value("ID_USER", nextid));
					table.addValue(new Value("ID_PROFILE", id_profile));
					table.addValue(new Value("LOGIN", user[i]));
					table.addValue(new Value("PASSWORD", password));
					table.addValue(new Value("NAME", code[i]));
					table.addValue(new Value("DESCRIPTION", desc[i]));
					table.addValue(new Value("ENABLED", true));

					database.setValuesInsert(table);
					database.insertRow();
                    users.put(user[i], new Long(nextid));
				}
			}
            
            try
            {
                database.closeInsert();
                log.logDetailed(toString(), "Populated table " + tablename);
            }
            catch(KettleDatabaseException dbe)
            {
                throw new KettleDatabaseException("Unable to close insert after populating table " + tablename, dbe);
            }
		}
		if (monitor!=null) monitor.worked(1);

		//////////////////////////////////////////////////////////////////////////////////
		//
		// R_PERMISSION
		//
		// Create table...
        Map permissions = new Hashtable();
		boolean ok_permission = true;
		table = new Row();
		tablename = "R_PERMISSION";
		if (monitor!=null) monitor.subTask("Checking table "+tablename);
		table.addValue(new Value("ID_PERMISSION", Value.VALUE_TYPE_INTEGER, KEY, 0));
		table.addValue(new Value("CODE", Value.VALUE_TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
		table.addValue(new Value("DESCRIPTION", Value.VALUE_TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
		sql = database.getDDL(tablename, table, null, false, "ID_PERMISSION", false);

		if (sql != null && sql.length() > 0) // Doesn't exist: create the table...
		{
            log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
			database.execStatements(sql);
			log.logDetailed(toString(), "Created or altered table " + tablename);
		}
		else
		{
			log.logDetailed(toString(), "Table " + tablename + " is OK.");
		}

		if (ok_permission)
		{
			//
			// Populate with data...
			//
			code = PermissionMeta.permission_type_desc;
			desc = PermissionMeta.permissionTypeLongDesc;

			database.prepareInsert(table, tablename);

			for (int i = 1; i < code.length; i++)
			{
                Row lookup = null;
                if (upgrade) lookup = database.getOneRow("SELECT ID_PERMISSION FROM " + tablename + " WHERE CODE = '" + code[i] + "'");
				if (lookup == null)
				{
					long nextid = getNextPermissionID();

					table = new Row();
					table.addValue(new Value("ID_PERMISSION", nextid));
					table.addValue(new Value("CODE", code[i]));
					table.addValue(new Value("DESCRIPTION", desc[i]));

					database.setValuesInsert(table);
					database.insertRow();
                    log.logDetailed(toString(), "Inserted new row into table "+tablename+" : "+table);
                    permissions.put(code[i], new Long(nextid));
				}
			}

            try
            {
                database.closeInsert();
                log.logDetailed(toString(), "Populated table " + tablename);
            }
            catch(KettleDatabaseException dbe)
            {
                throw new KettleDatabaseException("Unable to close insert after populating table " + tablename, dbe);
            }
		}
		if (monitor!=null) monitor.worked(1);

		//////////////////////////////////////////////////////////////////////////////////
		//
		// R_PROFILE_PERMISSION
		//
		// Create table...
		boolean ok_profile_permission = true;
		table = new Row();
		tablename = "R_PROFILE_PERMISSION";
		if (monitor!=null) monitor.subTask("Checking table "+tablename);
		table.addValue(new Value("ID_PROFILE", Value.VALUE_TYPE_INTEGER, KEY, 0));
		table.addValue(new Value("ID_PERMISSION", Value.VALUE_TYPE_INTEGER, KEY, 0));
		sql = database.getDDL(tablename, table, null, false, null, false);

		if (sql != null && sql.length() > 0) // Doesn't exist: create the table...
		{
            log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
			database.execStatements(sql);
			log.logDetailed(toString(), "Created or altered table " + tablename);

			try
			{
				indexname = "IDX_" + tablename.substring(2) + "_PK";
				keyfield = new String[] { "ID_PROFILE", "ID_PERMISSION" };
				if (!database.checkIndexExists(tablename, keyfield))
				{
					sql = database.getCreateIndexStatement(tablename, indexname, keyfield, false, true, false, false);
	
                    log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
					database.execStatements(sql);
					log.logDetailed(toString(), "Created lookup index " + indexname + " on " + tablename);
				}
			}
			catch(KettleDatabaseException kdbe)
			{
				// Ignore this one: index is not properly detected, it already exists...
			}
		}
		else
		{
			log.logDetailed(toString(), "Table " + tablename + " is OK.");
		}

		if (ok_profile_permission)
		{
			database.prepareInsert(table, tablename);

			// Administrator default:
            Long profileID = (Long)profiles.get( "Administrator");
            long id_profile = -1L;
            if (profileID!=null) id_profile = profileID.longValue();
			
            log.logDetailed(toString(), "Administrator profile id = "+id_profile);
            String perms[] = new String[]
				{ 
                    PermissionMeta.permission_type_desc[PermissionMeta.TYPE_PERMISSION_ADMIN],
                    PermissionMeta.permission_type_desc[PermissionMeta.TYPE_PERMISSION_TRANSFORMATION],
                    PermissionMeta.permission_type_desc[PermissionMeta.TYPE_PERMISSION_JOB],
                    PermissionMeta.permission_type_desc[PermissionMeta.TYPE_PERMISSION_SCHEMA] 
				};
			
			for (int i=0;i < perms.length ; i++)
			{
                Long permissionID = (Long) permissions.get(perms[i]);
                long id_permission = -1L;
                if (permissionID!=null) id_permission = permissionID.longValue();
                
                log.logDetailed(toString(), "Permission id for '"+perms[i]+"' = "+id_permission);

				Row lookup = null;
                if (upgrade) 
                {
                    String lookupSQL = "SELECT ID_PROFILE FROM " + tablename + " WHERE ID_PROFILE=" + id_profile + " AND ID_PERMISSION=" + id_permission;
                    log.logDetailed(toString(), "Executing SQL: "+lookupSQL);
                    lookup = database.getOneRow(lookupSQL);
                }
				if (lookup == null) // if the combination is not yet there, insert...
				{
                    String insertSQL="INSERT INTO "+tablename+"(ID_PROFILE, ID_PERMISSION) VALUES("+id_profile+","+id_permission+")";
					database.execStatement(insertSQL);
					log.logDetailed(toString(), "insertSQL = ["+insertSQL+"]");
				}
				else
				{
					log.logDetailed(toString(), "Found id_profile="+id_profile+", id_permission="+id_permission);
				}
			}

			// User profile
            profileID = (Long)profiles.get( "User" );
            id_profile = -1L;
            if (profileID!=null) id_profile = profileID.longValue();
            
            log.logDetailed(toString(), "User profile id = "+id_profile);
            perms = new String[]
                { 
                      PermissionMeta.permission_type_desc[PermissionMeta.TYPE_PERMISSION_TRANSFORMATION],
                      PermissionMeta.permission_type_desc[PermissionMeta.TYPE_PERMISSION_JOB],
                      PermissionMeta.permission_type_desc[PermissionMeta.TYPE_PERMISSION_SCHEMA] 
                };

            for (int i = 0; i < perms.length; i++)
			{
                Long permissionID = (Long) permissions.get(perms[i]);
                long id_permission = -1L;
                if (permissionID!=null) id_permission = permissionID.longValue();

                Row lookup = null;
                if (upgrade) lookup = database.getOneRow("SELECT ID_PROFILE FROM " + tablename + " WHERE ID_PROFILE=" + id_profile + " AND ID_PERMISSION=" + id_permission);
				if (lookup == null) // if the combination is not yet there, insert...
				{
					table = new Row();
					table.addValue(new Value("ID_PROFILE", id_profile));
					table.addValue(new Value("ID_PERMISSION", id_permission));

					database.setValuesInsert(table);
					database.insertRow();
				}
			}

            try
            {
                database.closeInsert();
                log.logDetailed(toString(), "Populated table " + tablename);
            }
            catch(KettleDatabaseException dbe)
            {
                throw new KettleDatabaseException("Unable to close insert after populating table " + tablename, dbe);
            }
		}
        
		if (monitor!=null) monitor.worked(1);
		if (monitor!=null) monitor.done();
        
        log.logBasic(toString(), (upgrade?"Upgraded":"Created")+ " "+repositoryTableNames.length+" repository tables.");

	}

    private final String repositoryTableNames[] = new String[] 
                                      { 
                                        "R_DATABASE_TYPE", "R_DATABASE_CONTYPE", "R_DATABASE", "R_DATABASE_ATTRIBUTE", "R_NOTE",
                                        "R_TRANSFORMATION", "R_DIRECTORY", "R_TRANS_ATTRIBUTE", "R_DEPENDENCY", "R_TRANS_STEP_CONDITION",
                                        "R_CONDITION", "R_VALUE", "R_TRANS_HOP", "R_STEP_TYPE", "R_STEP", "R_STEP_ATTRIBUTE", "R_TRANS_NOTE",
                                        "R_JOB", "R_LOGLEVEL", "R_LOG", "R_JOBENTRY", "R_JOBENTRY_COPY", "R_JOBENTRY_TYPE",
                                        "R_JOBENTRY_ATTRIBUTE", "R_JOB_HOP", "R_JOB_NOTE", "R_PROFILE", "R_USER",
                                        "R_PERMISSION", "R_PROFILE_PERMISSION", "R_STEP_DATABASE" 
                                      };

	public boolean dropRepositorySchema() throws KettleDatabaseException
	{

		for (int i = 0; i < repositoryTableNames.length; i++)
		{
			try
			{
				database.execStatement("DROP TABLE " + repositoryTableNames[i]);
                log.logDetailed(toString(), "dropped table "+repositoryTableNames[i]);
			}
			catch (KettleDatabaseException dbe)
			{
				log.logDetailed(toString(), "Unable to drop table: " + repositoryTableNames[i]);
			}
		}
        log.logBasic(toString(), "Dropped all "+repositoryTableNames.length+" repository tables.");
		return true;
	}

	/**
	 * Update the list in R_STEP_TYPE using the steploader StepPlugin entries
	 * 
	 * @throws KettleDatabaseException if the update didn't go as planned.
	 */
	public void updateStepTypes() throws KettleDatabaseException
	{
		// We should only do an update if something has changed...
		for (int i = 0; i < steploader.nrStepsWithType(StepPlugin.TYPE_ALL); i++)
		{
			StepPlugin sp = steploader.getStepWithType(StepPlugin.TYPE_ALL, i);
			long id = getStepTypeID(sp.getID());
			if (id < 0) // Not found, we need to add this one...
			{
				// We need to add this one ...
				id = getNextStepTypeID();

				Row table = new Row();
				table.addValue(new Value("ID_STEP_TYPE", id));
				table.addValue(new Value("CODE", sp.getID()));
				table.addValue(new Value("DESCRIPTION", sp.getDescription()));
				table.addValue(new Value("HELPTEXT", sp.getTooltip()));

				database.prepareInsert(table, "R_STEP_TYPE");

				database.setValuesInsert(table);
				database.insertRow();
				database.closeInsert();
			}
		}
	}
	
	
	/**
	 * Update the list in R_JOBENTRY_TYPE 
	 * 
	 * @exception KettleDatabaseException if something went wrong during the update.
	 */
	public void updateJobEntryTypes() throws KettleDatabaseException
	{
		// We should only do an update if something has changed...
		for (int i = 1; i < JobEntryInterface.type_desc.length; i++)
		{
			long id = getJobEntryTypeID(JobEntryInterface.type_desc[i]);
			if (id < 0) // Not found, we need to add this one...
			{
				// We need to add this one ...
				id = getNextJobEntryTypeID();

				Row table = new Row();
				table.addValue(new Value("ID_JOBENTRY_TYPE", id));
				table.addValue(new Value("CODE", JobEntryInterface.type_desc[i]));
				table.addValue(new Value("DESCRIPTION", JobEntryInterface.type_desc_long[i]));

				database.prepareInsert(table, "R_JOBENTRY_TYPE");

				database.setValuesInsert(table);
				database.insertRow();
				database.closeInsert();
			}
		}
	}


	public String toString()
	{
		if (repinfo == null)
			return getClass().getName();
		return repinfo.getName();
	}

	/**
	 * @return Returns the database.
	 */
	public Database getDatabase()
	{
		return database;
	}
	
	/**
	 * @param database The database to set.
	 */
	public void setDatabase(Database database)
	{
		this.database = database;
	}

    /**
     * @return Returns the directoryTree.
     */
    public RepositoryDirectory getDirectoryTree()
    {
        return directoryTree;
    }

    /**
     * @param directoryTree The directoryTree to set.
     */
    public void setDirectoryTree(RepositoryDirectory directoryTree)
    {
        this.directoryTree = directoryTree;
    }
    
    public void lockRepository() throws KettleDatabaseException
    {
        database.lockTables(repositoryTableNames);
    }
    
    public void unlockRepository() throws KettleDatabaseException
    {
        database.unlockTables(repositoryTableNames);
    }
    
    public void exportAllObjects(IProgressMonitor monitor, String xmlFilename) throws KettleException
    {
            if (monitor!=null) monitor.beginTask("Exporting the repository to XML...", 3);
            
            String xml = XMLHandler.getXMLHeader();
            xml+="<repository>"+Const.CR+Const.CR;

            // Dump the transformations...
            xml+="<transformations>"+Const.CR;
            if (monitor!=null) monitor.subTask("Exporting the transformations...");
            
            // Loop over all the directory id's
            long dirids[] = getDirectoryTree().getDirectoryIDs();
            System.out.println("Going through "+dirids.length+" directories.");
            
            for (int d=0;d<dirids.length && (monitor==null || (monitor!=null && !monitor.isCanceled()) );d++)
            {
                RepositoryDirectory repdir = getDirectoryTree().findDirectory(dirids[d]);

                String trans[] = getTransformationNames(dirids[d]);
                for (int i=0;i<trans.length && (monitor==null || (monitor!=null && !monitor.isCanceled()));i++)
                {
                    try
                    {
                        TransMeta ti = new TransMeta(this, trans[i], repdir);
                        System.out.println("Loading/Exporting transformation ["+trans[i]+"]");
                        if (monitor!=null) monitor.subTask("Exporting transformation ["+trans[i]+"]");
                        
                        xml+= ti.getXML()+Const.CR;
                    }
                    catch(KettleException ke)
                    {
                        log.logError(toString(), "An error occurred reading transformation ["+trans[i]+"] from directory ["+repdir+"] : "+ke.getMessage());
                        log.logError(toString(), "Transformation ["+trans[i]+"] from directory ["+repdir+"] was not exported because of a loading error!");
                    }
                }
            }
            xml+="</transformations>"+Const.CR;
            if (monitor!=null) monitor.worked(1);

            // Now dump the jobs...
            xml+="<jobs>"+Const.CR;
            
            if (monitor!=null) monitor.subTask("Exporting the jobs...");
            
            for (int d=0;d<dirids.length && (monitor==null || (monitor!=null && !monitor.isCanceled()));d++)
            {
                RepositoryDirectory repdir = getDirectoryTree().findDirectory(dirids[d]);

                String jobs[]  = getJobNames(dirids[d]);
                for (int i=0;i<jobs.length && (monitor==null || (monitor!=null && !monitor.isCanceled()));i++)
                {
                    try
                    {
                        JobMeta ji = new JobMeta(log, this, jobs[i], repdir);
                        System.out.println("Loading/Exporting job ["+jobs[i]+"]");
                        if (monitor!=null) monitor.subTask("Exporting job ["+jobs[i]+"]");
                        
                        xml+=ji.getXML()+Const.CR;
                    }
                    catch(KettleException ke)
                    {
                        log.logError(toString(), "An error occurred reading job ["+jobs[i]+"] from directory ["+repdir+"] : "+ke.getMessage());
                        log.logError(toString(), "Job ["+jobs[i]+"] from directory ["+repdir+"] was not exported because of a loading error!");
                    }
                }
            }
            xml+="</jobs>"+Const.CR;

            xml+="</repository>"+Const.CR+Const.CR;
            
            if (monitor!=null) monitor.worked(1);

            if (monitor==null || (monitor!=null && !monitor.isCanceled()))
            {
                if (monitor!=null) monitor.subTask("Saving XML to file ["+xmlFilename+"]");

                File f = new File(xmlFilename);
                try
                {
                    FileOutputStream fos = new FileOutputStream(f);
                    fos.write(xml.getBytes(Const.XML_ENCODING));
                    fos.close();
                }
                catch(IOException e)
                {
                    System.out.println("Couldn't create file ["+xmlFilename+"]");
                }
                if (monitor!=null) monitor.worked(1);
            }
            
            if (monitor!=null) monitor.done();
    }

}