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

import java.io.IOException;
import java.io.OutputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;

import be.ibridge.kettle.cluster.ClusterSchema;
import be.ibridge.kettle.cluster.SlaveServer;
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
import be.ibridge.kettle.core.vfs.KettleVFS;
import be.ibridge.kettle.job.JobEntryLoader;
import be.ibridge.kettle.job.JobMeta;
import be.ibridge.kettle.job.JobPlugin;
import be.ibridge.kettle.partition.PartitionSchema;
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
    private final String repositoryTableNames[] = new String[] 
         { 
            "R_REPOSITORY_LOG", "R_VERSION",
            "R_DATABASE_TYPE", "R_DATABASE_CONTYPE", "R_DATABASE", "R_DATABASE_ATTRIBUTE", "R_NOTE",
            "R_TRANSFORMATION", "R_DIRECTORY", "R_TRANS_ATTRIBUTE", "R_DEPENDENCY", "R_TRANS_STEP_CONDITION",
            "R_CONDITION", "R_VALUE", "R_TRANS_HOP", "R_STEP_TYPE", "R_STEP", "R_STEP_ATTRIBUTE", "R_TRANS_NOTE",
            "R_JOB", "R_LOGLEVEL", "R_LOG", "R_JOBENTRY", "R_JOBENTRY_COPY", "R_JOBENTRY_TYPE",
            "R_JOBENTRY_ATTRIBUTE", "R_JOB_HOP", "R_JOB_NOTE", "R_PROFILE", "R_USER",
            "R_PERMISSION", "R_PROFILE_PERMISSION", "R_STEP_DATABASE",
            "R_PARTITION_SCHEMA", "R_PARTITION", "R_TRANS_PARTITION_SCHEMA", 
            "R_CLUSTER", "R_SLAVE", "R_CLUSTER_SLAVE", "R_TRANS_CLUSTER", "R_TRANS_SLAVE",
         };
    
    public static final int REQUIRED_MAJOR_VERSION = 2;
    public static final int REQUIRED_MINOR_VERSION = 5;
    
	private RepositoryMeta		repinfo;
	public  UserInfo			userinfo;
	private RepositoryDirectory	directoryTree;
	private Database			database;

	public  LogWriter			log;

	private String				locksource;

	private PreparedStatement	psStepAttributesLookup;
	private PreparedStatement	psStepAttributesInsert;
    private PreparedStatement   psTransAttributesLookup;
    private PreparedStatement   psTransAttributesInsert;
	
	private ArrayList           stepAttributesBuffer;
	
	private PreparedStatement	pstmt_entry_attributes;

	private StepLoader			steploader;

	private int					majorVersion;
	private int					minorVersion;
    private DatabaseMeta        databaseMeta;

    /** The maximum length of a text field in a Kettle repository : 2.000.000 is enough for everyone ;-) */ 
    private static final int REP_STRING_LENGTH      = 2000000;
    private static final int REP_STRING_CODE_LENGTH =     255;
    
    private static Repository currentRepository;

	public Repository(LogWriter log, RepositoryMeta repinfo, UserInfo userinfo)
	{
		this.repinfo = repinfo;
		this.log = log;
		this.userinfo = userinfo;

		steploader = StepLoader.getInstance();
		
		database = new Database(repinfo.getConnection());
		databaseMeta = database.getDatabaseMeta();
            
		psStepAttributesLookup = null;
		psStepAttributesInsert = null;
        psTransAttributesLookup = null;
		pstmt_entry_attributes = null;

		this.majorVersion = REQUIRED_MAJOR_VERSION;
		this.minorVersion = REQUIRED_MINOR_VERSION;

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
     * Get the required repository version for this version of Kettle.
     * @return the required repository version for this version of Kettle.
     */
    public static final String getRequiredVersion()
    {
        return REQUIRED_MAJOR_VERSION + "." + REQUIRED_MINOR_VERSION;
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
	public synchronized boolean connect(String locksource) throws KettleException
	{
		return connect(false, true, locksource, false);
	}

    public synchronized boolean connect(boolean no_lookup, boolean readDirectory, String locksource) throws KettleException
    {
        return connect(no_lookup, readDirectory, locksource, false);
    }

	public synchronized boolean connect(boolean no_lookup, boolean readDirectory, String locksource, boolean ignoreVersion) throws KettleException
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
            if (!ignoreVersion) verifyVersion();
			setAutoCommit(false);
			repinfo.setLock(true);
			this.locksource = locksource;
			if (!no_lookup)
			{
				try
				{
					setLookupStepAttribute();
                    setLookupTransAttribute();
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
    				refreshRepositoryDirectoryTree();
    			}
    			catch (KettleException e)
    			{
    				log.logError(toString(), e.toString());
    			}
            }
            else
            {
                directoryTree = new RepositoryDirectory();
            }
            
            // OK, the repository is available
            currentRepository = this;
		}
		catch (KettleException e)
		{
			retval = false;
			log.logError(toString(), "Error connecting to the repository!" + e.getMessage());
            throw new KettleException(e);
		}

		return retval;
	}
    
    private void verifyVersion() throws KettleException
    {
        Row lastUpgrade = null;
        try
        {
            lastUpgrade = database.getOneRow("SELECT * FROM R_VERSION ORDER BY UPGRADE_DATE DESC");
        }
        catch(Exception e)
        {
            // If we can't retrieve the last available upgrade date:
            // this means the R_VERSION table doesn't exist.
            // This table was introduced in version 2.3.0
            //
            log.logBasic(toString(), "There was an error getting information from the version table R_VERSION.");
            log.logBasic(toString(), "This table was introduced in version 2.3.0. so we assume the version is 2.2.2");
            log.logBasic(toString(), "Stack trace: "+Const.getStackTracker(e));

            majorVersion = 2;
            minorVersion = 2;

            lastUpgrade = null;
        }

        if (lastUpgrade != null)
        {
            majorVersion = (int)lastUpgrade.getInteger("MAJOR_VERSION", -1);
            minorVersion = (int)lastUpgrade.getInteger("MINOR_VERSION", -1);
        }
            
        if (majorVersion < REQUIRED_MAJOR_VERSION || ( majorVersion==REQUIRED_MAJOR_VERSION && minorVersion<REQUIRED_MINOR_VERSION))
        {
            throw new KettleException(Const.CR+
                    "The version of the repository is "+getVersion()+Const.CR+
                    "This Kettle edition requires it to be at least version "+getRequiredVersion()+Const.CR+
                    "Please upgrade the repository using the repository dialog (edit)"+Const.CR+
                    "Also see the Repository Upgrade Guide (in docs/English) for more information."
            );
        }
    }

    public synchronized void refreshRepositoryDirectoryTree() throws KettleException
    {
        try
        {
            directoryTree = new RepositoryDirectory(this);
        }
        catch (KettleException e)
        {
            directoryTree = new RepositoryDirectory();
            throw new KettleException("Unable to read the directory tree from the repository!", e);
        }

    }

	public synchronized void disconnect()
	{
		try
		{
            currentRepository=null;
            
			closeStepAttributeLookupPreparedStatement();
            closeTransAttributeLookupPreparedStatement();
            closeLookupJobEntryAttribute();
            
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

	public synchronized void setAutoCommit(boolean autocommit)
	{
		if (!autocommit)
			database.setCommit(99999999);
		else
			database.setCommit(0);
	}

	public synchronized void commit() throws KettleException
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

	public synchronized void rollback()
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
	
	public synchronized void fillStepAttributesBuffer(long id_transformation) throws KettleDatabaseException
	{
	    String sql = "SELECT ID_STEP, CODE, NR, VALUE_NUM, VALUE_STR "+
	                 "FROM R_STEP_ATTRIBUTE "+
	                 "WHERE ID_TRANSFORMATION = "+id_transformation+" "+
	                 "ORDER BY ID_STEP, CODE, NR"
	                 ;
	    
	    stepAttributesBuffer = database.getRows(sql, -1);
        
        Collections.sort(stepAttributesBuffer);  // just to make sure...
	}
	
	private synchronized Row searchStepAttributeInBuffer(long id_step, String code, long nr)
	{
	    int idx = searchStepAttributeIndexInBuffer(id_step, code, nr);
	    if (idx<0) return null;
	    
	    // Get the row and remote it from the list...
	    Row r = (Row)stepAttributesBuffer.get(idx);
	    // stepAttributesBuffer.remove(idx);
	    
	    return r;
	}
	
	
	private synchronized int searchStepAttributeIndexInBuffer(long id_step, String code, long nr)
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

	private synchronized int searchNrStepAttributes(long id_step, String code)
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

	public synchronized long getJobID(String name, long id_directory) throws KettleDatabaseException
	{
		return getIDWithValue("R_JOB", "ID_JOB", "NAME", name, "ID_DIRECTORY", id_directory);
	}

	public synchronized long getTransformationID(String name, long id_directory) throws KettleDatabaseException
	{
		return getIDWithValue("R_TRANSFORMATION", "ID_TRANSFORMATION", "NAME", name, "ID_DIRECTORY", id_directory);
	}

	public synchronized long getNoteID(String note) throws KettleDatabaseException
	{
		return getIDWithValue("R_NOTE", "ID_NOTE", "VALUE_STR", note);
	}

	public synchronized long getDatabaseID(String name) throws KettleDatabaseException
	{
		return getIDWithValue("R_DATABASE", "ID_DATABASE", "NAME", name);
	}
    
    public synchronized long getPartitionSchemaID(String name) throws KettleDatabaseException
    {
        return getIDWithValue("R_PARTITION_SCHEMA", "ID_PARTITION_SCHEMA", "NAME", name);
    }

    public synchronized long getSlaveID(String name) throws KettleDatabaseException
    {
        return getIDWithValue("R_SLAVE", "ID_SLAVE", "NAME", name);
    }

    public synchronized long getClusterID(String name) throws KettleDatabaseException
    {
        return getIDWithValue("R_CLUSTER", "ID_CLUSTER", "NAME", name);
    }

	public synchronized long getDatabaseTypeID(String code) throws KettleDatabaseException
	{
		return getIDWithValue("R_DATABASE_TYPE", "ID_DATABASE_TYPE", "CODE", code);
	}

	public synchronized long getDatabaseConTypeID(String code) throws KettleDatabaseException
	{
		return getIDWithValue("R_DATABASE_CONTYPE", "ID_DATABASE_CONTYPE", "CODE", code);
	}

	public synchronized long getStepTypeID(String code) throws KettleDatabaseException
	{
		return getIDWithValue("R_STEP_TYPE", "ID_STEP_TYPE", "CODE", code);
	}

	public synchronized long getJobEntryID(String name, long id_job) throws KettleDatabaseException
	{
		return getIDWithValue("R_JOBENTRY", "ID_JOBENTRY", "NAME", name, "ID_JOB", id_job);
	}

	public synchronized long getJobEntryTypeID(String code) throws KettleDatabaseException
	{
		return getIDWithValue("R_JOBENTRY_TYPE", "ID_JOBENTRY_TYPE", "CODE", code);
	}

	public synchronized long getStepID(String name, long id_transformation) throws KettleDatabaseException
	{
		return getIDWithValue("R_STEP", "ID_STEP", "NAME", name, "ID_TRANSFORMATION", id_transformation);
	}

	public synchronized long getUserID(String login) throws KettleDatabaseException
	{
		return getIDWithValue("R_USER", "ID_USER", "LOGIN", login);
	}

	public synchronized long getProfileID(String profilename) throws KettleDatabaseException
	{
		return getIDWithValue("R_PROFILE", "ID_PROFILE", "NAME", profilename);
	}

	public synchronized long getPermissionID(String code) throws KettleDatabaseException
	{
		return getIDWithValue("R_PERMISSION", "ID_PERMISSION", "CODE", code);
	}

	public synchronized long getTransHopID(long id_transformation, long id_step_from, long id_step_to)
			throws KettleDatabaseException
	{
		String lookupkey[] = new String[] { "ID_TRANSFORMATION", "ID_STEP_FROM", "ID_STEP_TO" };
		long key[] = new long[] { id_transformation, id_step_from, id_step_to };

		return getIDWithValue("R_TRANS_HOP", "ID_TRANS_HOP", lookupkey, key);
	}

	public synchronized long getJobHopID(long id_job, long id_jobentry_copy_from, long id_jobentry_copy_to)
			throws KettleDatabaseException
	{
		String lookupkey[] = new String[] { "ID_JOB", "ID_JOBENTRY_COPY_FROM", "ID_JOBENTRY_COPY_TO" };
		long key[] = new long[] { id_job, id_jobentry_copy_from, id_jobentry_copy_to };

		return getIDWithValue("R_JOB_HOP", "ID_JOB_HOP", lookupkey, key);
	}

	public synchronized long getDependencyID(long id_transformation, long id_database, String tablename) throws KettleDatabaseException
	{
		String lookupkey[] = new String[] { "ID_TRANSFORMATION", "ID_DATABASE" };
		long key[] = new long[] { id_transformation, id_database };

		return getIDWithValue("R_DEPENDENCY", "ID_DEPENDENCY", "TABLE_NAME", tablename, lookupkey, key);
	}

	public synchronized long getRootDirectoryID() throws KettleDatabaseException
	{
		Row result = database.getOneRow("SELECT ID_DIRECTORY FROM R_DIRECTORY WHERE ID_DIRECTORY_PARENT = 0");
		if (result != null && result.getValue(0).isNumeric())
			return result.getValue(0).getInteger();
		return -1;
	}

	public synchronized int getNrSubDirectories(long id_directory) throws KettleDatabaseException
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

	public synchronized long[] getSubDirectoryIDs(long id_directory) throws KettleDatabaseException
	{
		return getIDs("SELECT ID_DIRECTORY FROM R_DIRECTORY WHERE ID_DIRECTORY_PARENT = " + id_directory+" ORDER BY DIRECTORY_NAME");
	}

	private synchronized long getIDWithValue(String tablename, String idfield, String lookupfield, String value) throws KettleDatabaseException
	{
		Row par = new Row();
		par.addValue(new Value("value", value));
		Row result = database.getOneRow("SELECT " + databaseMeta.quoteField(idfield) + " FROM " + databaseMeta.quoteField(tablename)+ " WHERE " + databaseMeta.quoteField(lookupfield) + " = ?", par);

		if (result != null && result.getValue(0).isNumeric())
			return result.getValue(0).getInteger();
		return -1;
	}

	private synchronized long getIDWithValue(String tablename, String idfield, String lookupfield, String value, String lookupkey, long key) throws KettleDatabaseException
	{
		Row par = new Row();
		par.addValue(new Value("value", value));
		par.addValue(new Value("key", key));
		Row result = database.getOneRow("SELECT " + databaseMeta.quoteField(idfield) + " FROM " + databaseMeta.quoteField(tablename) + " WHERE " + databaseMeta.quoteField( lookupfield ) + " = ? AND "
									+ databaseMeta.quoteField(lookupkey) + " = ?", par);

        if (result != null && result.getValue(0).isNumeric())
			return result.getValue(0).getInteger();
		return -1;
	}

	private synchronized long getIDWithValue(String tablename, String idfield, String lookupkey[], long key[]) throws KettleDatabaseException
	{
		Row par = new Row();
		String sql = "SELECT " + databaseMeta.quoteField(idfield) + " FROM " + databaseMeta.quoteField(tablename) + " ";

		for (int i = 0; i < lookupkey.length; i++)
		{
			if (i == 0)
				sql += "WHERE ";
			else
				sql += "AND   ";
			par.addValue(new Value(lookupkey[i], key[i]));
			sql += databaseMeta.quoteField(lookupkey[i]) + " = ? ";
		}
		Row result = database.getOneRow(sql, par);
		if (result != null && result.getValue(0).isNumeric())
			return result.getValue(0).getInteger();
		return -1;
	}

	private synchronized long getIDWithValue(String tablename, String idfield, String lookupfield, String value, String lookupkey[], long key[]) throws KettleDatabaseException
	{
		Row par = new Row();
		par.addValue(new Value(lookupfield, value));

		String sql = "SELECT " + databaseMeta.quoteField(idfield) + " FROM " + databaseMeta.quoteField(tablename) + " WHERE " + databaseMeta.quoteField(lookupfield) + " = ? ";

		for (int i = 0; i < lookupkey.length; i++)
		{
			par.addValue(new Value(lookupkey[i], key[i]));
			sql += "AND " + databaseMeta.quoteField(lookupkey[i]) + " = ? ";
		}

		Row result = database.getOneRow(sql, par);
		if (result != null && result.getValue(0).isNumeric())
			return result.getValue(0).getInteger();
		return -1;
	}

	public synchronized String getDatabaseTypeCode(long id_database_type) throws KettleDatabaseException
	{
		return getStringWithID("R_DATABASE_TYPE", "ID_DATABASE_TYPE", id_database_type, "CODE");
	}

	public synchronized String getDatabaseConTypeCode(long id_database_contype) throws KettleDatabaseException
	{
		return getStringWithID("R_DATABASE_CONTYPE", "ID_DATABASE_CONTYPE", id_database_contype, "CODE");
	}

	public synchronized String getStepTypeCode(long id_database_type) throws KettleDatabaseException
	{
		return getStringWithID("R_STEP_TYPE", "ID_STEP_TYPE", id_database_type, "CODE");
	}

	private synchronized String getStringWithID(String tablename, String keyfield, long id, String fieldname) throws KettleDatabaseException
	{
		String sql = "SELECT " + databaseMeta.quoteField(fieldname) + " FROM " + databaseMeta.quoteField(tablename) + " WHERE " + databaseMeta.quoteField(keyfield) + " = ?";
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

	public synchronized void moveTransformation(String transname, long id_directory_from, long id_directory_to) throws KettleDatabaseException
	{
        String nameField = databaseMeta.quoteField("NAME");
		String sql = "UPDATE R_TRANSFORMATION SET ID_DIRECTORY = ? WHERE "+nameField+" = ? AND ID_DIRECTORY = ?";

		Row par = new Row();
		par.addValue(new Value("ID_DIRECTORY", id_directory_to));
		par.addValue(new Value("NAME", transname));
		par.addValue(new Value("ID_DIRECTORY", id_directory_from));

		database.execStatement(sql, par);
	}

	public synchronized void moveJob(String jobname, long id_directory_from, long id_directory_to) throws KettleDatabaseException
	{
        String nameField = databaseMeta.quoteField("NAME");
		String sql = "UPDATE R_JOB SET ID_DIRECTORY = ? WHERE "+nameField+" = ? AND ID_DIRECTORY = ?";

		Row par = new Row();
		par.addValue(new Value("ID_DIRECTORY", id_directory_to));
		par.addValue(new Value("NAME", jobname));
		par.addValue(new Value("ID_DIRECTORY", id_directory_from));

		database.execStatement(sql, par);
	}

	/////////////////////////////////////////////////////////////////////////////////////
	// GET NEW IDS
	/////////////////////////////////////////////////////////////////////////////////////

	public synchronized long getNextTransformationID() throws KettleDatabaseException
	{
		return getNextID("R_TRANSFORMATION", "ID_TRANSFORMATION");
	}

	public synchronized long getNextJobID() throws KettleDatabaseException
	{
		return getNextID("R_JOB", "ID_JOB");
	}

	public synchronized long getNextNoteID() throws KettleDatabaseException
	{
		return getNextID("R_NOTE", "ID_NOTE");
	}
    
    public synchronized long getNextLogID() throws KettleDatabaseException
    {
        return getNextID("R_REPOSITORY_LOG", "ID_REPOSITORY_LOG");
    }

	public synchronized long getNextDatabaseID() throws KettleDatabaseException
	{
		return getNextID("R_DATABASE", "ID_DATABASE");
	}

	public synchronized long getNextDatabaseTypeID() throws KettleDatabaseException
	{
		return getNextID("R_DATABASE_TYPE", "ID_DATABASE_TYPE");
	}

	public synchronized long getNextDatabaseConnectionTypeID() throws KettleDatabaseException
	{
		return getNextID("R_DATABASE_CONTYPE", "ID_DATABASE_CONTYPE");
	}

	public synchronized long getNextLoglevelID() throws KettleDatabaseException
	{
		return getNextID("R_LOGLEVEL", "ID_LOGLEVEL");
	}

	public synchronized long getNextStepTypeID() throws KettleDatabaseException
	{
		return getNextID("R_STEP_TYPE", "ID_STEP_TYPE");
	}

	public synchronized long getNextStepID() throws KettleDatabaseException
	{
		return getNextID("R_STEP", "ID_STEP");
	}

	public synchronized long getNextJobEntryID() throws KettleDatabaseException
	{
		return getNextID("R_JOBENTRY", "ID_JOBENTRY");
	}

	public synchronized long getNextJobEntryTypeID() throws KettleDatabaseException
	{
		return getNextID("R_JOBENTRY_TYPE", "ID_JOBENTRY_TYPE");
	}

	public synchronized long getNextJobEntryCopyID() throws KettleDatabaseException
	{
		return getNextID("R_JOBENTRY_COPY", "ID_JOBENTRY_COPY");
	}

	public synchronized long getNextStepAttributeID() throws KettleDatabaseException
	{
		return getNextID("R_STEP_ATTRIBUTE", "ID_STEP_ATTRIBUTE");
	}

    public synchronized long getNextTransAttributeID() throws KettleDatabaseException
    {
        return getNextID("R_TRANS_ATTRIBUTE", "ID_TRANS_ATTRIBUTE");
    }
    
    public synchronized long getNextDatabaseAttributeID() throws KettleDatabaseException
    {
        return getNextID("R_DATABASE_ATTRIBUTE", "ID_DATABASE_ATTRIBUTE");
    }

	public synchronized long getNextTransHopID() throws KettleDatabaseException
	{
		return getNextID("R_TRANS_HOP", "ID_TRANS_HOP");
	}

	public synchronized long getNextJobHopID() throws KettleDatabaseException
	{
		return getNextID("R_JOB_HOP", "ID_JOB_HOP");
	}

	public synchronized long getNextDepencencyID() throws KettleDatabaseException
	{
		return getNextID("R_DEPENDENCY", "ID_DEPENDENCY");
	}
    
    public synchronized long getNextPartitionSchemaID() throws KettleDatabaseException
    {
        return getNextID("R_PARTITION_SCHEMA", "ID_PARTITION_SCHEMA");
    }

    public synchronized long getNextPartitionID() throws KettleDatabaseException
    {
        return getNextID("R_PARTITION", "ID_PARTITION");
    }

    public synchronized long getNextTransformationPartitionSchemaID() throws KettleDatabaseException
    {
        return getNextID("R_TRANS_PARTITION_SCHEMA", "ID_TRANS_PARTITION_SCHEMA");
    }
    
    public synchronized long getNextClusterID() throws KettleDatabaseException
    {
        return getNextID("R_CLUSTER", "ID_CLUSTER");
    }

    public synchronized long getNextSlaveServerID() throws KettleDatabaseException
    {
        return getNextID("R_SLAVE", "ID_SLAVE");
    }
    
    public synchronized long getNextClusterSlaveID() throws KettleDatabaseException
    {
        return getNextID("R_CLUSTER_SLAVE", "ID_CLUSTER_SLAVE");
    }
    
    public synchronized long getNextTransformationSlaveID() throws KettleDatabaseException
    {
        return getNextID("R_TRANS_SLAVE", "ID_TRANS_SLAVE");
    }
    
    public synchronized long getNextTransformationClusterID() throws KettleDatabaseException
    {
        return getNextID("R_TRANS_CLUSTER", "ID_TRANS_CLUSTER");
    }
    
	public synchronized long getNextConditionID() throws KettleDatabaseException
	{
		return getNextID("R_CONDITION", "ID_CONDITION");
	}

	public synchronized long getNextValueID() throws KettleDatabaseException
	{
		return getNextID("R_VALUE", "ID_VALUE");
	}

	public synchronized long getNextUserID() throws KettleDatabaseException
	{
		return getNextID("R_USER", "ID_USER");
	}

	public synchronized long getNextProfileID() throws KettleDatabaseException
	{
		return getNextID("R_PROFILE", "ID_PROFILE");
	}

	public synchronized long getNextPermissionID() throws KettleDatabaseException
	{
		return getNextID("R_PERMISSION", "ID_PERMISSION");
	}

	public synchronized long getNextJobEntryAttributeID() throws KettleDatabaseException
	{
	    return getNextID("R_JOBENTRY_ATTRIBUTE", "ID_JOBENTRY_ATTRIBUTE");
	}
	
	public synchronized long getNextID(String tableName, String fieldName) throws KettleDatabaseException
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
    
    public synchronized void clearNextIDCounters()
    {
        Counters.getInstance().clear();
    }

	public synchronized long getNextDirectoryID() throws KettleDatabaseException
	{
		return getNextID("R_DIRECTORY", "ID_DIRECTORY");
	}

	private synchronized long getNextTableID(String tablename, String idfield) throws KettleDatabaseException
	{
		long retval = -1;

		Row r = database.getOneRow("SELECT MAX(" + databaseMeta.quoteField(idfield) + ") FROM " + databaseMeta.quoteField(tablename));
		if (r != null)
		{
			Value id = r.getValue(0);
			
			// log.logBasic(toString(), "result row for "+idfield+" is : "+r.toString()+", id = "+id.toString()+" int="+id.getInteger()+" num="+id.getNumber());
			if (id.isNull())
			{
				if (log.isDebug()) log.logDebug(toString(), "no max(" + idfield + ") found in table " + tablename);
				retval = 1;
			}
			else
			{
                if (log.isDebug()) log.logDebug(toString(), "max(" + idfield + ") found in table " + tablename + " --> " + id.getInteger()
											+ " number: " + id.getNumber());
				retval = id.getInteger() + 1L;
				
				// log.logBasic(toString(), "Got next id for "+tablename+"."+idfield+" from the database: "+retval);
			}
		}
		return retval;
	}

	/////////////////////////////////////////////////////////////////////////////////////
	// INSERT VALUES
	/////////////////////////////////////////////////////////////////////////////////////

	public synchronized void insertTransformation(TransMeta transMeta) throws KettleDatabaseException
    {
		Row table = new Row();

		table.addValue(new Value("ID_TRANSFORMATION",   transMeta.getId()));
		table.addValue(new Value("NAME",                transMeta.getName()));
		table.addValue(new Value("DESCRIPTION",         transMeta.getDescription()));
		table.addValue(new Value("EXTENDED_DESCRIPTION",   transMeta.getExtendedDescription()));
		table.addValue(new Value("TRANS_VERSION",       transMeta.getTransversion()));
		table.addValue(new Value("TRANS_STATUS",        transMeta.getTransstatus()  <0 ? -1L : transMeta.getTransstatus()));
		table.addValue(new Value("ID_STEP_READ",        transMeta.getReadStep()  ==null ? -1L : transMeta.getReadStep().getID()));
		table.addValue(new Value("ID_STEP_WRITE",       transMeta.getWriteStep() ==null ? -1L : transMeta.getWriteStep().getID()));
		table.addValue(new Value("ID_STEP_INPUT",       transMeta.getInputStep() ==null ? -1L : transMeta.getInputStep().getID()));
		table.addValue(new Value("ID_STEP_OUTPUT",      transMeta.getOutputStep()==null ? -1L : transMeta.getOutputStep().getID()));
		table.addValue(new Value("ID_STEP_UPDATE",      transMeta.getUpdateStep()==null ? -1L : transMeta.getUpdateStep().getID()));
		table.addValue(new Value("ID_DATABASE_LOG",     transMeta.getLogConnection()==null ? -1L : transMeta.getLogConnection().getID()));
		table.addValue(new Value("TABLE_NAME_LOG",      transMeta.getLogTable()));
		table.addValue(new Value("USE_BATCHID",         transMeta.isBatchIdUsed()));
		table.addValue(new Value("USE_LOGFIELD",        transMeta.isLogfieldUsed()));
		table.addValue(new Value("ID_DATABASE_MAXDATE", transMeta.getMaxDateConnection()==null ? -1L : transMeta.getMaxDateConnection().getID()));
		table.addValue(new Value("TABLE_NAME_MAXDATE",  transMeta.getMaxDateTable()));
		table.addValue(new Value("FIELD_NAME_MAXDATE",  transMeta.getMaxDateField()));
		table.addValue(new Value("OFFSET_MAXDATE",      transMeta.getMaxDateOffset()));
		table.addValue(new Value("DIFF_MAXDATE",        transMeta.getMaxDateDifference()));

		table.addValue(new Value("CREATED_USER",        transMeta.getCreatedUser()));
		table.addValue(new Value("CREATED_DATE",        transMeta.getCreatedDate()));
		
		table.addValue(new Value("MODIFIED_USER",       transMeta.getModifiedUser()));
		table.addValue(new Value("MODIFIED_DATE",       transMeta.getModifiedDate()));
		table.addValue(new Value("SIZE_ROWSET",  (long) transMeta.getSizeRowset()));
		table.addValue(new Value("ID_DIRECTORY",        transMeta.getDirectory().getID()));

		database.prepareInsert(table, "R_TRANSFORMATION");
		database.setValuesInsert(table);
		database.insertRow();
		database.closeInsert();

        if (transMeta.getRejectedStep()!=null)
        {
            insertTransAttribute(transMeta.getId(), 0, "ID_STEP_REJECTED", transMeta.getRejectedStep().getID(), null);
        }

        insertTransAttribute(transMeta.getId(), 0, "UNIQUE_CONNECTIONS", 0, transMeta.isUsingUniqueConnections()?"Y":"N");
        insertTransAttribute(transMeta.getId(), 0, "FEEDBACK_SHOWN", 0, transMeta.isFeedbackShown()?"Y":"N");
        insertTransAttribute(transMeta.getId(), 0, "FEEDBACK_SIZE", transMeta.getFeedbackSize(), "");
		insertTransAttribute(transMeta.getId(), 0, "USING_THREAD_PRIORITIES", 0, transMeta.isUsingThreadPriorityManagment()?"Y":"N");
        insertTransAttribute(transMeta.getId(), 0, "SHARED_FILE", 0, transMeta.getSharedObjectsFile());
        
		// Save the logging connection link...
		if (transMeta.getLogConnection()!=null) insertStepDatabase(transMeta.getId(), -1L, transMeta.getLogConnection().getID());

		// Save the maxdate connection link...
		if (transMeta.getMaxDateConnection()!=null) insertStepDatabase(transMeta.getId(), -1L, transMeta.getMaxDateConnection().getID());
	}

	public synchronized void insertJob(long id_job, long id_directory, String name, long id_database_log, String table_name_log,
			String modified_user, Value modified_date, boolean useBatchId, boolean batchIdPassed, boolean logfieldUsed, 
            String sharedObjectsFile, String description, String extended_description, String version, int status,
			String created_user, Value created_date) throws KettleDatabaseException
	{
		Row table = new Row();

		table.addValue(new Value("ID_JOB", id_job));
		table.addValue(new Value("ID_DIRECTORY", id_directory));
		table.addValue(new Value("NAME", name));
		table.addValue(new Value("DESCRIPTION", description));
		table.addValue(new Value("EXTENDED_DESCRIPTION", extended_description));
		table.addValue(new Value("JOB_VERSION", version));
		table.addValue(new Value("JOB_STATUS", status  <0 ? -1L : status));

		table.addValue(new Value("ID_DATABASE_LOG", id_database_log));
		table.addValue(new Value("TABLE_NAME_LOG", table_name_log));

		table.addValue(new Value("CREATED_USER", created_user));
		table.addValue(new Value("CREATED_DATE", created_date));
		table.addValue(new Value("MODIFIED_USER", modified_user));
		table.addValue(new Value("MODIFIED_DATE", modified_date));
        table.addValue(new Value("USE_BATCH_ID", useBatchId));
        table.addValue(new Value("PASS_BATCH_ID", batchIdPassed));
        table.addValue(new Value("USE_LOGFIELD", logfieldUsed));
        table.addValue(new Value("SHARED_FILE", sharedObjectsFile));

		database.prepareInsert(table, "R_JOB");
		database.setValuesInsert(table);
		database.insertRow();
        if (log.isDebug()) log.logDebug(toString(), "Inserted new record into table R_JOB with data : " + table);
		database.closeInsert();
	}

	public synchronized long insertNote(String note, long gui_location_x, long gui_location_y, long gui_location_width, long gui_location_height) throws KettleDatabaseException
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
    
    public synchronized long insertLogEntry(String description) throws KettleDatabaseException
    {
        long id = getNextLogID();

        Row table = new Row();
        table.addValue(new Value("ID_REPOSITORY_LOG", id));
        table.addValue(new Value("REP_VERSION",    getVersion()));
        table.addValue(new Value("LOG_DATE",       new Date()));
        table.addValue(new Value("LOG_USER",       userinfo!=null?userinfo.getLogin():"admin"));
        table.addValue(new Value("OPERATION_DESC", description));

        database.prepareInsert(table, "R_REPOSITORY_LOG");
        database.setValuesInsert(table);
        database.insertRow();
        database.closeInsert();

        return id;
    }

	public synchronized void insertTransNote(long id_transformation, long id_note) throws KettleDatabaseException
	{
		Row table = new Row();

		table.addValue(new Value("ID_TRANSFORMATION", id_transformation));
		table.addValue(new Value("ID_NOTE", id_note));

		database.prepareInsert(table, "R_TRANS_NOTE");
		database.setValuesInsert(table);
		database.insertRow();
		database.closeInsert();
	}

	public synchronized void insertJobNote(long id_job, long id_note) throws KettleDatabaseException
	{
		Row table = new Row();

		table.addValue(new Value("ID_JOB", id_job));
		table.addValue(new Value("ID_NOTE", id_note));

		database.prepareInsert(table, "R_JOB_NOTE");
		database.setValuesInsert(table);
		database.insertRow();
		database.closeInsert();
	}

	public synchronized long insertDatabase(String name, String type, String access, String host, String dbname, String port,
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
		table.addValue(new Value("PORT", (long)Const.toInt(port, -1)));
		table.addValue(new Value("USERNAME", user));
		table.addValue(new Value("PASSWORD", Encr.encryptPasswordIfNotUsingVariables(pass)));
		table.addValue(new Value("SERVERNAME", servername));
		table.addValue(new Value("DATA_TBS", data_tablespace));
		table.addValue(new Value("INDEX_TBS", index_tablespace));

		database.prepareInsert(table, "R_DATABASE");
		database.setValuesInsert(table);
		database.insertRow();
		database.closeInsert();

		return id;
	}

	public synchronized long insertStep(long id_transformation, String name, String description, String steptype,
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

	public synchronized long insertStepAttribute(long id_transformation, long id_step, long nr, String code, double value_num,
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
		
        if (log.isDebug()) log.logDebug(toString(), "saved attribute ["+code+"]");
		
		return id;
	}
    
    public synchronized long insertTransAttribute(long id_transformation, long nr, String code, long value_num, String value_str) throws KettleDatabaseException
    {
        long id = getNextTransAttributeID();

        Row table = new Row();

        table.addValue(new Value("ID_TRANS_ATTRIBUTE", id));
        table.addValue(new Value("ID_TRANSFORMATION", id_transformation));
        table.addValue(new Value("NR", nr));
        table.addValue(new Value("CODE", code));
        table.addValue(new Value("VALUE_NUM", value_num));
        table.addValue(new Value("VALUE_STR", value_str));

        /* If we have prepared the insert, we don't do it again.
         * We asume that all the step insert statements come one after the other.
         */
        
        if (psTransAttributesInsert == null)
        {
            String sql = database.getInsertStatement("R_TRANS_ATTRIBUTE", table);
            psTransAttributesInsert = database.prepareSQL(sql);
        }
        database.setValues(table, psTransAttributesInsert);
        database.insertRow(psTransAttributesInsert, true);
        
        if (log.isDebug()) log.logDebug(toString(), "saved transformation attribute ["+code+"]");
        
        return id;
    }


	public synchronized void insertStepDatabase(long id_transformation, long id_step, long id_database)
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
	
    public synchronized long insertDatabaseAttribute(long id_database, String code, String value_str) throws KettleDatabaseException
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
        
        if (log.isDebug()) log.logDebug(toString(), "saved database attribute ["+code+"]");
        
        return id;
    }

	
	public synchronized long insertJobEntryAttribute(long id_job, long id_jobentry, long nr, String code, double value_num,
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

	public synchronized long insertTransHop(long id_transformation, long id_step_from, long id_step_to, boolean enabled)
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

	public synchronized long insertJobHop(long id_job, long id_jobentry_copy_from, long id_jobentry_copy_to, boolean enabled,
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

	public synchronized long insertDependency(long id_transformation, long id_database, String tablename, String fieldname)
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

    public synchronized long insertPartitionSchema(PartitionSchema partitionSchema) throws KettleDatabaseException
    {
        long id = getNextPartitionSchemaID();

        Row table = new Row();

        table.addValue(new Value("ID_PARTITION_SCHEMA", id));
        table.addValue(new Value("NAME", partitionSchema.getName()));

        database.prepareInsert(table, "R_PARTITION_SCHEMA");
        database.setValuesInsert(table);
        database.insertRow();
        database.closeInsert();

        return id;
    }
    
    public synchronized void updatePartitionSchema(PartitionSchema partitionSchema) throws KettleDatabaseException
    {
        Row table = new Row();
        table.addValue(new Value("NAME", partitionSchema.getName()));
        updateTableRow("R_PARTITION_SCHEMA", "ID_PARTITION_SCHEMA", table, partitionSchema.getId());
    }

    public synchronized long insertPartition(long id_partition_schema, String partition_id) throws KettleDatabaseException
    {
        long id = getNextPartitionID();

        Row table = new Row();

        table.addValue(new Value("ID_PARTITION", id));
        table.addValue(new Value("ID_PARTITION_SCHEMA", id_partition_schema));
        table.addValue(new Value("PARTITION_ID", partition_id));

        database.prepareInsert(table, "R_PARTITION");
        database.setValuesInsert(table);
        database.insertRow();
        database.closeInsert();

        return id;
    }

    public synchronized long insertTransformationPartitionSchema(long id_transformation, long id_partition_schema) throws KettleDatabaseException
    {
        long id = getNextTransformationPartitionSchemaID();

        Row table = new Row();

        table.addValue(new Value("ID_TRANS_PARTITION_SCHEMA", id));
        table.addValue(new Value("ID_TRANSFORMATION", id_transformation));
        table.addValue(new Value("ID_PARTITION_SCHEMA", id_partition_schema));

        database.prepareInsert(table, "R_TRANS_PARTITION_SCHEMA");
        database.setValuesInsert(table);
        database.insertRow();
        database.closeInsert();

        return id;
    }
    
    public synchronized long insertCluster(ClusterSchema clusterSchema) throws KettleDatabaseException
    {
        long id = getNextClusterID();

        Row table = new Row();

        table.addValue(new Value("ID_CLUSTER", id));
        table.addValue(new Value("NAME", clusterSchema.getName()));
        table.addValue(new Value("BASE_PORT", clusterSchema.getBasePort()));
        table.addValue(new Value("SOCKETS_BUFFER_SIZE", clusterSchema.getSocketsBufferSize()));
        table.addValue(new Value("SOCKETS_FLUSH_INTERVAL", clusterSchema.getSocketsFlushInterval()));
        table.addValue(new Value("SOCKETS_COMPRESSED", clusterSchema.isSocketsCompressed()));

        database.prepareInsert(table, "R_CLUSTER");
        database.setValuesInsert(table);
        database.insertRow();
        database.closeInsert();

        return id;
    }

    public synchronized long insertSlave(SlaveServer slaveServer) throws KettleDatabaseException
    {
        long id = getNextSlaveServerID();

        Row table = new Row();

        table.addValue(new Value("ID_SLAVE", id));
        table.addValue(new Value("NAME", slaveServer.getName()));
        table.addValue(new Value("HOST_NAME", slaveServer.getHostname()));
        table.addValue(new Value("PORT", slaveServer.getPort()));
        table.addValue(new Value("USERNAME", slaveServer.getUsername()));
        table.addValue(new Value("PASSWORD", slaveServer.getPassword()));
        table.addValue(new Value("PROXY_HOST_NAME", slaveServer.getProxyHostname()));
        table.addValue(new Value("PROXY_PORT", slaveServer.getProxyPort()));
        table.addValue(new Value("NON_PROXY_HOSTS", slaveServer.getNonProxyHosts()));
        table.addValue(new Value("MASTER", slaveServer.isMaster()));

        database.prepareInsert(table, "R_SLAVE");
        database.setValuesInsert(table);
        database.insertRow();
        database.closeInsert();

        return id;
    }
    
    public synchronized void updateSlave(SlaveServer slaveServer) throws KettleDatabaseException
    {
        Row table = new Row();
        table.addValue(new Value("NAME", slaveServer.getName()));
        table.addValue(new Value("HOST_NAME", slaveServer.getHostname()));
        table.addValue(new Value("PORT", slaveServer.getPort()));
        table.addValue(new Value("USERNAME", slaveServer.getUsername()));
        table.addValue(new Value("PASSWORD", slaveServer.getPassword()));
        table.addValue(new Value("PROXY_HOST_NAME", slaveServer.getProxyHostname()));
        table.addValue(new Value("PROXY_PORT", slaveServer.getProxyPort()));
        table.addValue(new Value("NON_PROXY_HOSTS", slaveServer.getNonProxyHosts()));
        table.addValue(new Value("MASTER", slaveServer.isMaster()));

        updateTableRow("R_SLAVE", "ID_SLAVE", table, slaveServer.getId());
    }
    
    public synchronized long insertClusterSlave(ClusterSchema clusterSchema, SlaveServer slaveServer) throws KettleDatabaseException
    {
        long id = getNextClusterSlaveID();

        Row table = new Row();

        table.addValue(new Value("ID_CLUSTER_SLAVE", id));
        table.addValue(new Value("ID_CLUSTER", clusterSchema.getId()));
        table.addValue(new Value("ID_SLAVE", slaveServer.getId()));

        database.prepareInsert(table, "R_CLUSTER_SLAVE");
        database.setValuesInsert(table);
        database.insertRow();
        database.closeInsert();

        return id;
    }

    public synchronized long insertTransformationCluster(long id_transformation, long id_cluster) throws KettleDatabaseException
    {
        long id = getNextTransformationClusterID();

        Row table = new Row();

        table.addValue(new Value("ID_TRANS_CLUSTER", id));
        table.addValue(new Value("ID_TRANSFORMATION", id_transformation));
        table.addValue(new Value("ID_CLUSTER", id_cluster));

        database.prepareInsert(table, "R_TRANS_CLUSTER");
        database.setValuesInsert(table);
        database.insertRow();
        database.closeInsert();

        return id;
    }

    public synchronized long insertTransformationSlave(long id_transformation, long id_slave) throws KettleDatabaseException
    {
        long id = getNextTransformationSlaveID();

        Row table = new Row();

        table.addValue(new Value("ID_TRANS_SLAVE", id));
        table.addValue(new Value("ID_TRANSFORMATION", id_transformation));
        table.addValue(new Value("ID_SLAVE", id_slave));

        database.prepareInsert(table, "R_TRANS_SLAVE");
        database.setValuesInsert(table);
        database.insertRow();
        database.closeInsert();

        return id;
    }
    
    
	public synchronized long insertCondition(long id_condition_parent, Condition condition) throws KettleDatabaseException
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

	public synchronized void insertTransStepCondition(long id_transformation, long id_step, long id_condition)
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

	public synchronized long insertDirectory(long id_directory_parent, RepositoryDirectory dir) throws KettleDatabaseException
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

	public synchronized void deleteDirectory(long id_directory) throws KettleDatabaseException
	{
		String sql = "DELETE FROM R_DIRECTORY WHERE ID_DIRECTORY = " + id_directory;
		database.execStatement(sql);
	}

	public synchronized void renameDirectory(long id_directory, String name) throws KettleDatabaseException
	{
		Row r = new Row();
		r.addValue(new Value("DIRECTORY_NAME", name));

		String sql = "UPDATE R_DIRECTORY SET DIRECTORY_NAME = ? WHERE ID_DIRECTORY = " + id_directory;

		log.logBasic(toString(), "sql = [" + sql + "]");
		log.logBasic(toString(), "row = [" + r + "]");

		database.execStatement(sql, r);
	}

	public synchronized long lookupValue(String name, String type, String value_str, boolean isnull) throws KettleDatabaseException
	{
		String tablename = "R_VALUE";
		Row table = new Row();
		table.addValue(new Value("NAME", name));
		table.addValue(new Value("VALUE_TYPE", type));
		table.addValue(new Value("VALUE_STR", value_str));
		table.addValue(new Value("IS_NULL", isnull));

		String sql = "SELECT " + database.getDatabaseMeta().quoteField("ID_VALUE") + " FROM " + tablename + " ";
		sql += "WHERE " + database.getDatabaseMeta().quoteField("NAME") + "       = ? ";
		sql += "AND   " + database.getDatabaseMeta().quoteField("VALUE_TYPE") + " = ? ";
		sql += "AND   " + database.getDatabaseMeta().quoteField("VALUE_STR") + "  = ? ";
		sql += "AND   " + database.getDatabaseMeta().quoteField("IS_NULL") + "    = ? ";

		Row result = database.getOneRow(sql, table);
		if (result != null && result.getValue(0).isNumeric())
			return result.getValue(0).getInteger();
		else
			return -1;
	}

	public synchronized long insertValue(String name, String type, String value_str, boolean isnull, long id_value_prev)
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

	public synchronized long insertJobEntry(long id_job, String name, String description, String jobentrytype)
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

	public synchronized long insertJobEntryCopy(long id_job, long id_jobentry, long id_jobentry_type, int nr, long gui_location_x,
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

	public synchronized void insertTableRow(String tablename, Row values) throws KettleDatabaseException
	{
		database.prepareInsert(values, tablename);
		database.setValuesInsert(values);
		database.insertRow();
		database.closeInsert();
	}

	/////////////////////////////////////////////////////////////////////////////////////
	// UPDATE VALUES
	/////////////////////////////////////////////////////////////////////////////////////

	public synchronized void updateDatabase(long id_database, String name, String type, String access, String host, String dbname,
			String port, String user, String pass, String servername, String data_tablespace, String index_tablespace)
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
		table.addValue(new Value("PORT", (long)Const.toInt(port, -1)));
		table.addValue(new Value("USERNAME", user));
		table.addValue(new Value("PASSWORD", Encr.encryptPasswordIfNotUsingVariables(pass)));
		table.addValue(new Value("SERVERNAME", servername));
		table.addValue(new Value("DATA_TBS", data_tablespace));
		table.addValue(new Value("INDEX_TBS", index_tablespace));

		updateTableRow("R_DATABASE", "ID_DATABASE", table, id_database);
	}

	public synchronized void updateTableRow(String tablename, String idfield, Row values, long id) throws KettleDatabaseException
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

	public synchronized void updateTableRow(String tablename, String idfield, Row values) throws KettleDatabaseException
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

	public synchronized int getNrJobs() throws KettleDatabaseException
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

	public synchronized int getNrTransformations(long id_directory) throws KettleDatabaseException
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

	public synchronized int getNrJobs(long id_directory) throws KettleDatabaseException
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

    public synchronized int getNrDirectories(long id_directory) throws KettleDatabaseException
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

	public synchronized int getNrConditions(long id_transforamtion) throws KettleDatabaseException
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

	public synchronized int getNrDatabases(long id_transforamtion) throws KettleDatabaseException
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

	public synchronized int getNrSubConditions(long id_condition) throws KettleDatabaseException
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

	public synchronized int getNrTransNotes(long id_transformation) throws KettleDatabaseException
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

	public synchronized int getNrJobNotes(long id_job) throws KettleDatabaseException
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

	public synchronized int getNrDatabases() throws KettleDatabaseException
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

    public synchronized int getNrDatabaseAttributes(long id_database) throws KettleDatabaseException
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

	public synchronized int getNrSteps(long id_transformation) throws KettleDatabaseException
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

	public synchronized int getNrStepDatabases(long id_database) throws KettleDatabaseException
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

	public synchronized int getNrStepAttributes(long id_step) throws KettleDatabaseException
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

	public synchronized int getNrTransHops(long id_transformation) throws KettleDatabaseException
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

	public synchronized int getNrJobHops(long id_job) throws KettleDatabaseException
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

	public synchronized int getNrTransDependencies(long id_transformation) throws KettleDatabaseException
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

	public synchronized int getNrJobEntries(long id_job) throws KettleDatabaseException
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

	public synchronized int getNrJobEntryCopies(long id_job, long id_jobentry) throws KettleDatabaseException
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

	public synchronized int getNrJobEntryCopies(long id_job) throws KettleDatabaseException
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

	public synchronized int getNrUsers() throws KettleDatabaseException
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

	public synchronized int getNrPermissions(long id_profile) throws KettleDatabaseException
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

	public synchronized int getNrProfiles() throws KettleDatabaseException
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

	public synchronized String[] getTransformationNames(long id_directory) throws KettleDatabaseException
	{
		String nameField = databaseMeta.quoteField("NAME");
		return getStrings("SELECT "+nameField+" FROM R_TRANSFORMATION WHERE ID_DIRECTORY = " + id_directory + " ORDER BY "+nameField);
	}
    
    public List getJobObjects(long id_directory) throws KettleDatabaseException
    {
        return getRepositoryObjects("R_JOB", RepositoryObject.STRING_OBJECT_TYPE_JOB, id_directory);
    }

    public List getTransformationObjects(long id_directory) throws KettleDatabaseException
    {
        return getRepositoryObjects("R_TRANSFORMATION", RepositoryObject.STRING_OBJECT_TYPE_TRANSFORMATION, id_directory);
    }

    /**
     * @param id_directory
     * @return A list of RepositoryObjects
     * 
     * @throws KettleDatabaseException
     */
    private synchronized List getRepositoryObjects(String tableName, String objectType, long id_directory) throws KettleDatabaseException
    {
        String nameField = databaseMeta.quoteField("NAME");
        
        String sql = "SELECT "+nameField+", MODIFIED_USER, MODIFIED_DATE, DESCRIPTION " +
                "FROM "+tableName+" " +
                "WHERE ID_DIRECTORY = " + id_directory + " "
                ;

        List repositoryObjects = new ArrayList();
        
        ResultSet rs = database.openQuery(sql);
        if (rs != null)
        {
        	try
        	{
                Row r = database.getRow(rs);
                while (r != null)
                {
                    repositoryObjects.add(new RepositoryObject( r.getValue(0).getString(), r.getValue(1).getString(), r.getValue(2).getDate(), objectType, r.getValue(3).getString()));
                    r = database.getRow(rs);
                }
        	}
        	finally 
        	{
        		if ( rs != null )
        		{
        			database.closeQuery(rs);
        		}
        	}                
        }

        return repositoryObjects;
    }
    

	public synchronized String[] getJobNames(long id_directory) throws KettleDatabaseException
	{
		String nameField = databaseMeta.quoteField("NAME");
        return getStrings("SELECT "+nameField+" FROM R_JOB WHERE ID_DIRECTORY = " + id_directory + " ORDER BY "+nameField);
	}

	public synchronized String[] getDirectoryNames(long id_directory) throws KettleDatabaseException
	{
        return getStrings("SELECT DIRECTORY_NAME FROM R_DIRECTORY WHERE ID_DIRECTORY_PARENT = " + id_directory + " ORDER BY DIRECTORY_NAME");
	}

	public synchronized String[] getJobNames() throws KettleDatabaseException
	{
        String nameField = databaseMeta.quoteField("NAME");
        return getStrings("SELECT "+nameField+" FROM R_JOB ORDER BY "+nameField);
	}

	public long[] getSubConditionIDs(long id_condition) throws KettleDatabaseException
	{
        return getIDs("SELECT ID_CONDITION FROM R_CONDITION WHERE ID_CONDITION_PARENT = " + id_condition);
	}

	public long[] getTransNoteIDs(long id_transformation) throws KettleDatabaseException
	{
        return getIDs("SELECT ID_NOTE FROM R_TRANS_NOTE WHERE ID_TRANSFORMATION = " + id_transformation);
	}

	public long[] getConditionIDs(long id_transformation) throws KettleDatabaseException
	{
        return getIDs("SELECT ID_CONDITION FROM R_TRANS_STEP_CONDITION WHERE ID_TRANSFORMATION = " + id_transformation);
	}

	public long[] getDatabaseIDs(long id_transformation) throws KettleDatabaseException
	{
        return getIDs("SELECT ID_DATABASE FROM R_STEP_DATABASE WHERE ID_TRANSFORMATION = " + id_transformation);
	}

	public long[] getJobNoteIDs(long id_job) throws KettleDatabaseException
	{
        return getIDs("SELECT ID_NOTE FROM R_JOB_NOTE WHERE ID_JOB = " + id_job);
	}

	public long[] getDatabaseIDs() throws KettleDatabaseException
	{
        String nameField = databaseMeta.quoteField("NAME");
        return getIDs("SELECT ID_DATABASE FROM R_DATABASE ORDER BY "+nameField);
	}
    
    public long[] getDatabaseAttributeIDs(long id_database) throws KettleDatabaseException
    {
        return getIDs("SELECT ID_DATABASE_ATTRIBUTE FROM R_DATABASE_ATTRIBUTE WHERE ID_DATABASE = "+id_database);
    }
    
    public long[] getPartitionSchemaIDs() throws KettleDatabaseException
    {
        String nameField = databaseMeta.quoteField("NAME");
        return getIDs("SELECT ID_PARTITION_SCHEMA FROM R_PARTITION_SCHEMA ORDER BY "+nameField);
    }
    
    public long[] getPartitionIDs(long id_partition_schema) throws KettleDatabaseException
    {
        return getIDs("SELECT ID_PARTITION FROM R_PARTITION WHERE ID_PARTITION_SCHEMA = " + id_partition_schema);
    }

    public long[] getTransformationPartitionSchemaIDs(long id_transformation) throws KettleDatabaseException
    {
        return getIDs("SELECT ID_TRANS_PARTITION_SCHEMA FROM R_TRANS_PARTITION_SCHEMA WHERE ID_TRANSFORMATION = "+id_transformation);
    }
    
    public long[] getTransformationClusterSchemaIDs(long id_transformation) throws KettleDatabaseException
    {
        return getIDs("SELECT ID_TRANS_CLUSTER FROM R_TRANS_CLUSTER WHERE ID_TRANSFORMATION = " + id_transformation);
    }
    
    public long[] getClusterIDs() throws KettleDatabaseException
    {
        String nameField = databaseMeta.quoteField("NAME");
        return getIDs("SELECT ID_CLUSTER FROM R_CLUSTER ORDER BY "+nameField); 
    }

    public long[] getSlaveIDs() throws KettleDatabaseException
    {
        return getIDs("SELECT ID_SLAVE FROM R_SLAVE");
    }

    public long[] getSlaveIDs(long id_cluster_schema) throws KettleDatabaseException
    {
        return getIDs("SELECT ID_SLAVE FROM R_CLUSTER_SLAVE WHERE ID_CLUSTER = " + id_cluster_schema);
    }
    
    private long[] getIDs(String sql) throws KettleDatabaseException
    {
        List ids = new ArrayList();
        
        ResultSet rs = database.openQuery(sql);
        try 
        {
            Row r = database.getRow(rs);
            while (r != null)
            {
                ids.add(new Long(r.getValue(0).getInteger()));
                r = database.getRow(rs);
            }
        }
        finally
        {
        	if ( rs != null )
        	{
        		database.closeQuery(rs);        		
        	}
        }
        return convertLongList(ids);
    }
    
    private String[] getStrings(String sql) throws KettleDatabaseException
    {
        List ids = new ArrayList();
        
        ResultSet rs = database.openQuery(sql);
        try 
        {
            Row r = database.getRow(rs);
            while (r != null)
            {
                ids.add( r.getValue(0).getString() );
                r = database.getRow(rs);
            }
        }
        finally 
        {
        	if ( rs != null )
        	{
        		database.closeQuery(rs);        		
        	}
        }            

        return (String[]) ids.toArray(new String[ids.size()]);

    }
    
    private long[] convertLongList(List list)
    {
        long[] ids = new long[list.size()];
        for (int i=0;i<ids.length;i++) ids[i] = ((Long)list.get(i)).longValue();
        return ids;
    }


	public synchronized String[] getDatabaseNames() throws KettleDatabaseException
	{
		String nameField = databaseMeta.quoteField("NAME");
		return getStrings("SELECT "+nameField+" FROM R_DATABASE ORDER BY "+nameField);
	}
    
    public synchronized String[] getPartitionSchemaNames() throws KettleDatabaseException
    {
        String nameField = databaseMeta.quoteField("NAME");
        return getStrings("SELECT "+nameField+" FROM R_PARTITION_SCHEMA ORDER BY "+nameField);
    }
    
    public synchronized String[] getSlaveNames() throws KettleDatabaseException
    {
        String nameField = databaseMeta.quoteField("NAME");
        return getStrings("SELECT "+nameField+" FROM R_SLAVE ORDER BY "+nameField);
    }
    
    public synchronized String[] getClusterNames() throws KettleDatabaseException
    {
        String nameField = databaseMeta.quoteField("NAME");
        return getStrings("SELECT "+nameField+" FROM R_CLUSTER ORDER BY "+nameField);
    }

	public long[] getStepIDs(long id_transformation) throws KettleDatabaseException
	{
		return getIDs("SELECT ID_STEP FROM R_STEP WHERE ID_TRANSFORMATION = " + id_transformation);
	}

	public synchronized String[] getTransformationsUsingDatabase(long id_database) throws KettleDatabaseException
	{
		String sql = "SELECT DISTINCT ID_TRANSFORMATION FROM R_STEP_DATABASE WHERE ID_DATABASE = " + id_database;
        return getTransformationsWithIDList( database.getRows(sql, 100) );
	}
    
    public synchronized String[] getClustersUsingSlave(long id_slave) throws KettleDatabaseException
    {
        String sql = "SELECT DISTINCT ID_CLUSTER FROM R_CLUSTER_SLAVE WHERE ID_SLAVE = " + id_slave;

        ArrayList list = database.getRows(sql, 100);
        ArrayList clusterList = new ArrayList();

        for (int i=0;i<list.size();i++)
        {
            long id_cluster_schema = ((Row)list.get(i)).getInteger("ID_CLUSTER", -1L); 
            if (id_cluster_schema > 0)
            {
                Row transRow =  getClusterSchema(id_cluster_schema);
                if (transRow!=null)
                {
                    String clusterName = transRow.getString("NAME", "<name not found>");
                    if (clusterName!=null) clusterList.add(clusterName);
                }
            }
            
        }

        return (String[]) clusterList.toArray(new String[clusterList.size()]);
    }

    public synchronized String[] getTransformationsUsingSlave(long id_slave) throws KettleDatabaseException
    {
        String sql = "SELECT DISTINCT ID_TRANSFORMATION FROM R_TRANS_SLAVE WHERE ID_SLAVE = " + id_slave;
        return getTransformationsWithIDList( database.getRows(sql, 100) );
    }
    
    public synchronized String[] getTransformationsUsingPartitionSchema(long id_partition_schema) throws KettleDatabaseException
    {
        String sql = "SELECT DISTINCT ID_TRANSFORMATION FROM R_TRANS_PARTITION_SCHEMA WHERE ID_PARTITION_SCHEMA = " + id_partition_schema;
        return getTransformationsWithIDList( database.getRows(sql, 100) );
    }
    
    public synchronized String[] getTransformationsUsingCluster(long id_cluster) throws KettleDatabaseException
    {
        String sql = "SELECT DISTINCT ID_TRANSFORMATION FROM R_TRANS_CLUSTER WHERE ID_CLUSTER = " + id_cluster;
        return getTransformationsWithIDList( database.getRows(sql, 100) );
    }

	private String[] getTransformationsWithIDList(ArrayList list) throws KettleDatabaseException
    {
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
                    
                    transList[i]=dir.getPathObjectCombination(transName);
                }
            }            
        }

        return transList;
    }

    public long[] getTransHopIDs(long id_transformation) throws KettleDatabaseException
	{
		return getIDs("SELECT ID_TRANS_HOP FROM R_TRANS_HOP WHERE ID_TRANSFORMATION = " + id_transformation);
	}

	public long[] getJobHopIDs(long id_job) throws KettleDatabaseException
	{
		return getIDs("SELECT ID_JOB_HOP FROM R_JOB_HOP WHERE ID_JOB = " + id_job);
	}

	public long[] getTransDependencyIDs(long id_transformation) throws KettleDatabaseException
	{
		return getIDs("SELECT ID_DEPENDENCY FROM R_DEPENDENCY WHERE ID_TRANSFORMATION = " + id_transformation);
	}

	public long[] getUserIDs() throws KettleDatabaseException
	{
		return getIDs("SELECT ID_USER FROM R_USER");
	}

	public synchronized String[] getUserLogins() throws KettleDatabaseException
	{
		String loginField = databaseMeta.quoteField("LOGIN");
		return getStrings("SELECT "+loginField+" FROM R_USER ORDER BY "+loginField);
	}

	public long[] getPermissionIDs(long id_profile) throws KettleDatabaseException
	{
		return getIDs("SELECT ID_PERMISSION FROM R_PROFILE_PERMISSION WHERE ID_PROFILE = " + id_profile);
	}

	public long[] getJobEntryIDs(long id_job) throws KettleDatabaseException
	{
		return getIDs("SELECT ID_JOBENTRY FROM R_JOBENTRY WHERE ID_JOB = " + id_job);
	}

	public long[] getJobEntryCopyIDs(long id_job) throws KettleDatabaseException
	{
		return getIDs("SELECT ID_JOBENTRY_COPY FROM R_JOBENTRY_COPY WHERE ID_JOB = " + id_job);
	}

	public long[] getJobEntryCopyIDs(long id_job, long id_jobentry) throws KettleDatabaseException
	{
		return getIDs("SELECT ID_JOBENTRY_COPY FROM R_JOBENTRY_COPY WHERE ID_JOB = " + id_job + " AND ID_JOBENTRY = " + id_jobentry);
	}

	public synchronized String[] getProfiles() throws KettleDatabaseException
	{
		String nameField = databaseMeta.quoteField("NAME");
		return getStrings("SELECT "+nameField+" FROM R_PROFILE ORDER BY "+nameField);
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
	
    public Row getPartitionSchema(long id_partition_schema) throws KettleDatabaseException
    {
        return getOneRow("R_PARTITION_SCHEMA", "ID_PARTITION_SCHEMA", id_partition_schema);
    }
    
    public Row getPartition(long id_partition) throws KettleDatabaseException
    {
        return getOneRow("R_PARTITION", "ID_PARTITION", id_partition);
    }

    public Row getClusterSchema(long id_cluster_schema) throws KettleDatabaseException
    {
        return getOneRow("R_CLUSTER", "ID_CLUSTER", id_cluster_schema);
    }

    public Row getSlaveServer(long id_slave) throws KettleDatabaseException
    {
        return getOneRow("R_SLAVE", "ID_SLAVE", id_slave);
    }

	private Row getOneRow(String tablename, String keyfield, long id) throws KettleDatabaseException
	{
		String sql = "SELECT * FROM " + tablename + " WHERE " 
			+ database.getDatabaseMeta().quoteField(keyfield) + " = " + id;

		return database.getOneRow(sql);
	}

	// STEP ATTRIBUTES: SAVE

	public synchronized long saveStepAttribute(long id_transformation, long id_step, String code, String value)
			throws KettleDatabaseException
	{
		return saveStepAttribute(code, 0, id_transformation, id_step, 0.0, value);
	}

	public synchronized long saveStepAttribute(long id_transformation, long id_step, String code, double value)
			throws KettleDatabaseException
	{
		return saveStepAttribute(code, 0, id_transformation, id_step, value, null);
	}

	public synchronized long saveStepAttribute(long id_transformation, long id_step, String code, boolean value) throws KettleDatabaseException
	{
		return saveStepAttribute(code, 0, id_transformation, id_step, 0.0, value ? "Y" : "N");
	}

	public synchronized long saveStepAttribute(long id_transformation, long id_step, long nr, String code, String value) throws KettleDatabaseException
	{
		return saveStepAttribute(code, nr, id_transformation, id_step, 0.0, value);
	}

	public synchronized long saveStepAttribute(long id_transformation, long id_step, long nr, String code, double value) throws KettleDatabaseException
	{
		return saveStepAttribute(code, nr, id_transformation, id_step, value, null);
	}

	public synchronized long saveStepAttribute(long id_transformation, long id_step, long nr, String code, boolean value) throws KettleDatabaseException
	{
		return saveStepAttribute(code, nr, id_transformation, id_step, 0.0, value ? "Y" : "N");
	}

	private long saveStepAttribute(String code, long nr, long id_transformation, long id_step, double value_num, String value_str) throws KettleDatabaseException
	{
		return insertStepAttribute(id_transformation, id_step, nr, code, value_num, value_str);
	}

	// STEP ATTRIBUTES: GET

	public synchronized void setLookupStepAttribute() throws KettleDatabaseException
	{
		String sql = "SELECT VALUE_STR, VALUE_NUM FROM R_STEP_ATTRIBUTE WHERE ID_STEP = ?  AND CODE = ?  AND NR = ? ";

		psStepAttributesLookup = database.prepareSQL(sql);
	}
    
    public synchronized void setLookupTransAttribute() throws KettleDatabaseException
    {
        String sql = "SELECT VALUE_STR, VALUE_NUM FROM R_TRANS_ATTRIBUTE WHERE ID_TRANSFORMATION = ?  AND CODE = ?  AND NR = ? ";

        psTransAttributesLookup = database.prepareSQL(sql);
    }
    
    public synchronized void closeTransAttributeLookupPreparedStatement() throws KettleDatabaseException
    {
        database.closePreparedStatement(psTransAttributesLookup);
        psTransAttributesLookup = null;
    }


	public synchronized void closeStepAttributeLookupPreparedStatement() throws KettleDatabaseException
	{
		database.closePreparedStatement(psStepAttributesLookup);
		psStepAttributesLookup = null;
	}
	
	public synchronized void closeStepAttributeInsertPreparedStatement() throws KettleDatabaseException
	{
	    if (psStepAttributesInsert!=null)
	    {
		    database.insertFinished(psStepAttributesInsert, true); // batch mode!
			psStepAttributesInsert = null;
	    }
	}

    public synchronized void closeTransAttributeInsertPreparedStatement() throws KettleDatabaseException
    {
        if (psTransAttributesInsert!=null)
        {
            database.insertFinished(psTransAttributesInsert, true); // batch mode!
            psTransAttributesInsert = null;
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

    public Row getTransAttributeRow(long id_transformation, int nr, String code) throws KettleDatabaseException
    {
        Row par = new Row();
        par.addValue(new Value("ID_TRANSFORMATION", id_transformation));
        par.addValue(new Value("CODE", code));
        par.addValue(new Value("NR", (long) nr));

        database.setValues(par, psTransAttributesLookup);

        return database.getLookup(psTransAttributesLookup);
    }

	public synchronized long getStepAttributeInteger(long id_step, int nr, String code) throws KettleDatabaseException
	{
		Row r = null;
		if (stepAttributesBuffer!=null) r = searchStepAttributeInBuffer(id_step, code, (long)nr);
		else                            r = getStepAttributeRow(id_step, nr, code);
		if (r == null)
			return 0;
		return r.searchValue("VALUE_NUM").getInteger();
	}

	public synchronized String getStepAttributeString(long id_step, int nr, String code) throws KettleDatabaseException
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
		
		if (r == null) return def;
        Value v = r.searchValue("VALUE_STR");
        if (v==null || Const.isEmpty(v.getString())) return def;
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

	public synchronized long getStepAttributeInteger(long id_step, String code) throws KettleDatabaseException
	{
		return getStepAttributeInteger(id_step, 0, code);
	}

	public synchronized String getStepAttributeString(long id_step, String code) throws KettleDatabaseException
	{
		return getStepAttributeString(id_step, 0, code);
	}

	public boolean getStepAttributeBoolean(long id_step, String code) throws KettleDatabaseException
	{
		return getStepAttributeBoolean(id_step, 0, code);
	}

	public synchronized int countNrStepAttributes(long id_step, String code) throws KettleDatabaseException
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
    
    // TRANS ATTRIBUTES: get
    
    public synchronized String getTransAttributeString(long id_transformation, int nr, String code) throws KettleDatabaseException
    {
        Row r = null;
        r = getTransAttributeRow(id_transformation, nr, code);
        if (r == null)
            return null;
        return r.searchValue("VALUE_STR").getString();
    }

    public synchronized boolean getTransAttributeBoolean(long id_transformation, int nr, String code) throws KettleDatabaseException
    {
        Row r = null;
        r = getTransAttributeRow(id_transformation, nr, code);
        if (r == null)
            return false;
        return r.searchValue("VALUE_STR").getBoolean();
    }

    public synchronized double getTransAttributeNumber(long id_transformation, int nr, String code) throws KettleDatabaseException
    {
        Row r = null;
        r = getTransAttributeRow(id_transformation, nr, code);
        if (r == null)
            return 0.0;
        return r.searchValue("VALUE_NUM").getNumber();
    }

    public synchronized long getTransAttributeInteger(long id_transformation, int nr, String code) throws KettleDatabaseException
    {
        Row r = null;
        r = getTransAttributeRow(id_transformation, nr, code);
        if (r == null)
            return 0;
        return r.searchValue("VALUE_NUM").getInteger();
    }
    
    public synchronized int countNrTransAttributes(long id_transformation, String code) throws KettleDatabaseException
    {
        String sql = "SELECT COUNT(*) FROM R_TRANS_ATTRIBUTE WHERE ID_TRANSFORMATION = ? AND CODE = ?";
        Row table = new Row();
        table.addValue(new Value("ID_STEP", id_transformation));
        table.addValue(new Value("CODE", code));
        Row r = database.getOneRow(sql, table);
        if (r == null)
            return 0;
        
        return (int) r.getValue(0).getInteger();
    }

    public synchronized List getTransAttributes(long id_transformation, String code, long nr) throws KettleDatabaseException
    {
        String sql = "SELECT * FROM R_TRANS_ATTRIBUTE WHERE ID_TRANSFORMATION = ? AND CODE = ? AND NR = ? ORDER BY VALUE_NUM";
        Row table = new Row();
        table.addValue(new Value("ID_STEP", id_transformation));
        table.addValue(new Value("CODE", code));
        table.addValue(new Value("NR", nr));
        
        return database.getRows(sql, 0);
    }

	// JOBENTRY ATTRIBUTES: SAVE

	// WANTED: throw extra exceptions to locate storage problems (strings too long etc)
	//
	public synchronized long saveJobEntryAttribute(long id_job, long id_jobentry, String code, String value)
			throws KettleDatabaseException
	{
		return saveJobEntryAttribute(code, 0, id_job, id_jobentry, 0.0, value);
	}

	public synchronized long saveJobEntryAttribute(long id_job, long id_jobentry, String code, double value)
			throws KettleDatabaseException
	{
		return saveJobEntryAttribute(code, 0, id_job, id_jobentry, value, null);
	}

	public synchronized long saveJobEntryAttribute(long id_job, long id_jobentry, String code, boolean value)
			throws KettleDatabaseException
	{
		return saveJobEntryAttribute(code, 0, id_job, id_jobentry, 0.0, value ? "Y" : "N");
	}

	public synchronized long saveJobEntryAttribute(long id_job, long id_jobentry, long nr, String code, String value)
			throws KettleDatabaseException
	{
		return saveJobEntryAttribute(code, nr, id_job, id_jobentry, 0.0, value);
	}

	public synchronized long saveJobEntryAttribute(long id_job, long id_jobentry, long nr, String code, double value)
			throws KettleDatabaseException
	{
		return saveJobEntryAttribute(code, nr, id_job, id_jobentry, value, null);
	}

	public synchronized long saveJobEntryAttribute(long id_job, long id_jobentry, long nr, String code, boolean value)
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

	public synchronized void setLookupJobEntryAttribute() throws KettleDatabaseException
	{
		String sql = "SELECT VALUE_STR, VALUE_NUM FROM R_JOBENTRY_ATTRIBUTE WHERE ID_JOBENTRY = ?  AND CODE = ?  AND NR = ? ";

		pstmt_entry_attributes = database.prepareSQL(sql);
	}

	public synchronized void closeLookupJobEntryAttribute() throws KettleDatabaseException
	{
		database.closePreparedStatement(pstmt_entry_attributes);
        pstmt_entry_attributes = null;
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

	public synchronized long getJobEntryAttributeInteger(long id_jobentry, int nr, String code) throws KettleDatabaseException
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

	public synchronized String getJobEntryAttributeString(long id_jobentry, int nr, String code) throws KettleDatabaseException
	{
		Row r = getJobEntryAttributeRow(id_jobentry, nr, code);
		if (r == null)
			return null;
		return r.searchValue("VALUE_STR").getString();
	}

	public boolean getJobEntryAttributeBoolean(long id_jobentry, int nr, String code) throws KettleDatabaseException
	{
		return getJobEntryAttributeBoolean(id_jobentry, nr, code, false);
	}

	public boolean getJobEntryAttributeBoolean(long id_jobentry, int nr, String code, boolean def) throws KettleDatabaseException
	{
		Row r = getJobEntryAttributeRow(id_jobentry, nr, code);
		if (r == null) return def;
        Value v = r.searchValue("VALUE_STR");
        if (v==null || Const.isEmpty(v.getString())) return def;
        return v.getBoolean();
	}

	public double getJobEntryAttributeNumber(long id_jobentry, String code) throws KettleDatabaseException
	{
		return getJobEntryAttributeNumber(id_jobentry, 0, code);
	}

	public synchronized long getJobEntryAttributeInteger(long id_jobentry, String code) throws KettleDatabaseException
	{
		return getJobEntryAttributeInteger(id_jobentry, 0, code);
	}

	public synchronized String getJobEntryAttributeString(long id_jobentry, String code) throws KettleDatabaseException
	{
		return getJobEntryAttributeString(id_jobentry, 0, code);
	}

	public boolean getJobEntryAttributeBoolean(long id_jobentry, String code) throws KettleDatabaseException
	{
		return getJobEntryAttributeBoolean(id_jobentry, 0, code, false);
	}

	public boolean getJobEntryAttributeBoolean(long id_jobentry, String code, boolean def) throws KettleDatabaseException
	{
		return getJobEntryAttributeBoolean(id_jobentry, 0, code, def);
	}

	public synchronized int countNrJobEntryAttributes(long id_jobentry, String code) throws KettleDatabaseException
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

	public synchronized void delSteps(long id_transformation) throws KettleDatabaseException
	{
		String sql = "DELETE FROM R_STEP WHERE ID_TRANSFORMATION = " + id_transformation;
		database.execStatement(sql);
	}

	public synchronized void delCondition(long id_condition) throws KettleDatabaseException
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

	public synchronized void delStepConditions(long id_transformation) throws KettleDatabaseException
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
	public synchronized void delStepDatabases(long id_transformation) throws KettleDatabaseException
	{
		String sql = "DELETE FROM R_STEP_DATABASE WHERE ID_TRANSFORMATION = " + id_transformation;
		database.execStatement(sql);
	}

	public synchronized void delJobEntries(long id_job) throws KettleDatabaseException
	{
		String sql = "DELETE FROM R_JOBENTRY WHERE ID_JOB = " + id_job;
		database.execStatement(sql);
	}

	public synchronized void delJobEntryCopies(long id_job) throws KettleDatabaseException
	{
		String sql = "DELETE FROM R_JOBENTRY_COPY WHERE ID_JOB = " + id_job;
		database.execStatement(sql);
	}

	public synchronized void delDependencies(long id_transformation) throws KettleDatabaseException
	{
		String sql = "DELETE FROM R_DEPENDENCY WHERE ID_TRANSFORMATION = " + id_transformation;
		database.execStatement(sql);
	}

	public synchronized void delStepAttributes(long id_transformation) throws KettleDatabaseException
	{
		String sql = "DELETE FROM R_STEP_ATTRIBUTE WHERE ID_TRANSFORMATION = " + id_transformation;
		database.execStatement(sql);
	}

    public synchronized void delTransAttributes(long id_transformation) throws KettleDatabaseException
    {
        String sql = "DELETE FROM R_TRANS_ATTRIBUTE WHERE ID_TRANSFORMATION = " + id_transformation;
        database.execStatement(sql);
    }
    
    public synchronized void delPartitionSchemas(long id_transformation) throws KettleDatabaseException
    {
        String sql = "DELETE FROM R_TRANS_PARTITION_SCHEMA WHERE ID_TRANSFORMATION = " + id_transformation;
        database.execStatement(sql);
    }

    public synchronized void delPartitions(long id_partition_schema) throws KettleDatabaseException
    {
        // First see if the partition is used by a step, transformation etc.
        // 
        database.execStatement("DELETE FROM R_PARTITION WHERE ID_PARTITION_SCHEMA = " + id_partition_schema);
    }
    
    public synchronized void delClusterSlaves(long id_cluster) throws KettleDatabaseException
    {
        String sql = "DELETE FROM R_CLUSTER_SLAVE WHERE ID_CLUSTER = " + id_cluster;
        database.execStatement(sql);
    }
    
    public synchronized void delTransformationClusters(long id_transformation) throws KettleDatabaseException
    {
        String sql = "DELETE FROM R_TRANS_CLUSTER WHERE ID_TRANSFORMATION = " + id_transformation;
        database.execStatement(sql);
    }

    public synchronized void delTransformationSlaves(long id_transformation) throws KettleDatabaseException
    {
        String sql = "DELETE FROM R_TRANS_SLAVE WHERE ID_TRANSFORMATION = " + id_transformation;
        database.execStatement(sql);
    }


	public synchronized void delJobEntryAttributes(long id_job) throws KettleDatabaseException
	{
		String sql = "DELETE FROM R_JOBENTRY_ATTRIBUTE WHERE ID_JOB = " + id_job;
		database.execStatement(sql);
	}

	public synchronized void delTransHops(long id_transformation) throws KettleDatabaseException
	{
		String sql = "DELETE FROM R_TRANS_HOP WHERE ID_TRANSFORMATION = " + id_transformation;
		database.execStatement(sql);
	}

	public synchronized void delJobHops(long id_job) throws KettleDatabaseException
	{
		String sql = "DELETE FROM R_JOB_HOP WHERE ID_JOB = " + id_job;
		database.execStatement(sql);
	}

	public synchronized void delTransNotes(long id_transformation) throws KettleDatabaseException
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

	public synchronized void delJobNotes(long id_job) throws KettleDatabaseException
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

	public synchronized void delTrans(long id_transformation) throws KettleDatabaseException
	{
		String sql = "DELETE FROM R_TRANSFORMATION WHERE ID_TRANSFORMATION = " + id_transformation;
		database.execStatement(sql);
	}

	public synchronized void delJob(long id_job) throws KettleDatabaseException
	{
		String sql = "DELETE FROM R_JOB WHERE ID_JOB = " + id_job;
		database.execStatement(sql);
	}

	public synchronized void delDatabase(long id_database) throws KettleDatabaseException
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
    
    public synchronized void delDatabaseAttributes(long id_database) throws KettleDatabaseException
    {
        String sql = "DELETE FROM R_DATABASE_ATTRIBUTE WHERE ID_DATABASE = " + id_database;
        database.execStatement(sql);
    }

	public synchronized void delTransStepCondition(long id_transformation) throws KettleDatabaseException
	{
		String sql = "DELETE FROM R_TRANS_STEP_CONDITION WHERE ID_TRANSFORMATION = " + id_transformation;
		database.execStatement(sql);
	}

	public synchronized void delValue(long id_value) throws KettleDatabaseException
	{
		String sql = "DELETE FROM R_VALUE WHERE ID_VALUE = " + id_value;
		database.execStatement(sql);
	}

	public synchronized void delUser(long id_user) throws KettleDatabaseException
	{
		String sql = "DELETE FROM R_USER WHERE ID_USER = " + id_user;
		database.execStatement(sql);
	}

	public synchronized void delProfile(long id_profile) throws KettleDatabaseException
	{
		String sql = "DELETE FROM R_PROFILE WHERE ID_PROFILE = " + id_profile;
		database.execStatement(sql);
	}

	public synchronized void delProfilePermissions(long id_profile) throws KettleDatabaseException
	{
		String sql = "DELETE FROM R_PROFILE_PERMISSION WHERE ID_PROFILE = " + id_profile;
		database.execStatement(sql);
	}
    
    public synchronized void delSlave(long id_slave) throws KettleDatabaseException
    {
        // First, see if the slave is still used by other objects...
        // If so, generate an error!!
        // We look in table R_TRANS_SLAVE to see if there are any transformations using this slave.
        // We obviously also look in table R_CLUSTER_SLAVE to see if there are any clusters that use this slave.
        String[] transList = getTransformationsUsingSlave(id_slave);
        String[] clustList = getClustersUsingSlave(id_slave);

        if (transList.length==0 && clustList.length==0)
        {
            database.execStatement("DELETE FROM R_SLAVE WHERE ID_SLAVE = " + id_slave);
            database.execStatement("DELETE FROM R_TRANS_SLAVE WHERE ID_SLAVE = " + id_slave);
        }
        else
        {
            StringBuffer message = new StringBuffer();
            
            if (transList.length>0)
            {
                message.append("Slave used by the following transformations:").append(Const.CR);
                for (int i = 0; i < transList.length; i++)
                {
                    message.append("  ").append(transList[i]).append(Const.CR);
                }
                message.append(Const.CR);
            }
            if (clustList.length>0)
            {
                message.append("Slave used by the following cluster schemas:").append(Const.CR);
                for (int i = 0; i < clustList.length; i++)
                {
                    message.append("  ").append(clustList[i]).append(Const.CR);
                }
            }
            
            KettleDependencyException e = new KettleDependencyException(message.toString());
            throw new KettleDependencyException("This slave server is still in use by one or more transformations ("+transList.length+") or cluster schemas ("+clustList.length+") :", e);
        }
    }
   
    public synchronized void delPartitionSchema(long id_partition_schema) throws KettleDatabaseException
    {
        // First, see if the schema is still used by other objects...
        // If so, generate an error!!
        //
        // We look in table R_TRANS_PARTITION_SCHEMA to see if there are any transformations using this schema.
        String[] transList = getTransformationsUsingPartitionSchema(id_partition_schema);

        if (transList.length==0)
        {
            database.execStatement("DELETE FROM R_PARTITION WHERE ID_PARTITION_SCHEMA = " + id_partition_schema);
            database.execStatement("DELETE FROM R_PARTITION_SCHEMA WHERE ID_PARTITION_SCHEMA = " + id_partition_schema);
        }
        else
        {
            StringBuffer message = new StringBuffer();
            
            message.append("The partition schema is used by the following transformations:").append(Const.CR);
            for (int i = 0; i < transList.length; i++)
            {
                message.append("  ").append(transList[i]).append(Const.CR);
            }
            message.append(Const.CR);
            
            KettleDependencyException e = new KettleDependencyException(message.toString());
            throw new KettleDependencyException("This partition schema is still in use by one or more transformations ("+transList.length+") :", e);
        }
    }
    
    public synchronized void delClusterSchema(long id_cluster) throws KettleDatabaseException
    {
        // First, see if the schema is still used by other objects...
        // If so, generate an error!!
        //
        // We look in table R_TRANS_CLUSTER to see if there are any transformations using this schema.
        String[] transList = getTransformationsUsingCluster(id_cluster);

        if (transList.length==0)
        {
            database.execStatement("DELETE FROM R_CLUSTER WHERE ID_CLUSTER = " + id_cluster);
        }
        else
        {
            StringBuffer message = new StringBuffer();
            
            message.append("The cluster schema is used by the following transformations:").append(Const.CR);
            for (int i = 0; i < transList.length; i++)
            {
                message.append("  ").append(transList[i]).append(Const.CR);
            }
            message.append(Const.CR);
            
            KettleDependencyException e = new KettleDependencyException(message.toString());
            throw new KettleDependencyException("This cluster schema is still in use by one or more transformations ("+transList.length+") :", e);
        }
    }


	public synchronized void delAllFromTrans(long id_transformation) throws KettleDatabaseException
	{
		delTransNotes(id_transformation);
		delStepAttributes(id_transformation);
		delSteps(id_transformation);
		delStepConditions(id_transformation);
		delStepDatabases(id_transformation);
		delTransHops(id_transformation);
		delDependencies(id_transformation);
        delTransAttributes(id_transformation);
        delPartitionSchemas(id_transformation);
        delTransformationClusters(id_transformation);
        delTransformationSlaves(id_transformation);
		delTrans(id_transformation);
	}

	public synchronized void renameTransformation(long id_transformation, String newname) throws KettleDatabaseException
	{
        String nameField = databaseMeta.quoteField("NAME");
		String sql = "UPDATE R_TRANSFORMATION SET "+nameField+" = ? WHERE ID_TRANSFORMATION = ?";

		Row table = new Row();
		table.addValue(new Value("NAME", newname));
		table.addValue(new Value("ID_TRANSFORMATION", id_transformation));

		database.execStatement(sql, table);
	}

	public synchronized void renameUser(long id_user, String newname) throws KettleDatabaseException
	{
        String nameField = databaseMeta.quoteField("NAME");
		String sql = "UPDATE R_USER SET "+nameField+" = ? WHERE ID_USER = ?";

		Row table = new Row();
		table.addValue(new Value("NAME", newname));
		table.addValue(new Value("ID_USER", id_user));

		database.execStatement(sql, table);
	}

	public synchronized void renameProfile(long id_profile, String newname) throws KettleDatabaseException
	{
        String nameField = databaseMeta.quoteField("NAME");
		String sql = "UPDATE R_PROFILE SET "+nameField+" = ? WHERE ID_PROFILE = ?";

		Row table = new Row();
		table.addValue(new Value("NAME", newname));
		table.addValue(new Value("ID_PROFILE", id_profile));

		database.execStatement(sql, table);
	}

	public synchronized void renameDatabase(long id_database, String newname) throws KettleDatabaseException
	{
        String nameField = databaseMeta.quoteField("NAME");
		String sql = "UPDATE R_DATABASE SET "+nameField+" = ? WHERE ID_DATABASE = ?";

		Row table = new Row();
		table.addValue(new Value("NAME", newname));
		table.addValue(new Value("ID_DATABASE", id_database));

		database.execStatement(sql, table);
	}

	public synchronized void delAllFromJob(long id_job) throws KettleDatabaseException
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

	public synchronized void renameJob(long id_job, String newname) throws KettleDatabaseException
	{
        String nameField = databaseMeta.quoteField("NAME");
		String sql = "UPDATE R_JOB SET "+nameField+" = ? WHERE ID_JOB = ?";

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
	public synchronized void createRepositorySchema(IProgressMonitor monitor, boolean upgrade) throws KettleDatabaseException
	{
		Row table;
		String sql;
		String tablename;
		String indexname;
		String keyfield[];
		String user[], pass[], code[], desc[], prof[];

		int KEY = 9; // integer, no need for bigint!

		log.logBasic(toString(), "Starting to create or modify the repository tables...");
        String message = (upgrade?"Upgrading ":"Creating")+" the Kettle repository...";
		if (monitor!=null) monitor.beginTask(message, 31);
        
        setAutoCommit(true);
        
        //////////////////////////////////////////////////////////////////////////////////
        // R_LOG
        //
        // Log the operations we do in the repository.
        //
        table = new Row();
        tablename = "R_REPOSITORY_LOG";
        if (monitor!=null) monitor.subTask("Checking table "+tablename);
        table.addValue(new Value("ID_REPOSITORY_LOG", Value.VALUE_TYPE_INTEGER, KEY, 0));
        table.addValue(new Value("REP_VERSION",    Value.VALUE_TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
        table.addValue(new Value("LOG_DATE",       Value.VALUE_TYPE_DATE));
        table.addValue(new Value("LOG_USER",       Value.VALUE_TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
        table.addValue(new Value("OPERATION_DESC", Value.VALUE_TYPE_STRING, REP_STRING_LENGTH, 0));
        sql = database.getDDL(tablename, table, null, false, "ID_REPOSITORY_LOG", false);
        
        if (sql != null && sql.length() > 0)
        {
            try
            {
                if (log.isDetailed()) log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
                database.execStatements(sql);
                if (log.isDetailed()) log.logDetailed(toString(), "Created/altered table " + tablename);
            }
            catch (KettleDatabaseException dbe)
            {
                throw new KettleDatabaseException("Unable to create or modify table " + tablename, dbe);
            }
        }
        else
        {
            if (log.isDetailed()) log.logDetailed(toString(), "Table " + tablename + " is OK.");
        }

        
        insertLogEntry((upgrade?"Upgrade":"Creation")+" of the Kettle repository");

        //////////////////////////////////////////////////////////////////////////////////
        // R_VERSION
        //
        // Let's start with the version table
        //
        table = new Row();
        tablename = "R_VERSION";
        if (monitor!=null) monitor.subTask("Checking table "+tablename);
        table.addValue(new Value("ID_VERSION",       Value.VALUE_TYPE_INTEGER, KEY, 0));
        table.addValue(new Value("MAJOR_VERSION",    Value.VALUE_TYPE_INTEGER, 3, 0));
        table.addValue(new Value("MINOR_VERSION",    Value.VALUE_TYPE_INTEGER, 3, 0));
        table.addValue(new Value("UPGRADE_DATE",     Value.VALUE_TYPE_DATE, 0, 0));
        table.addValue(new Value("IS_UPGRADE",       Value.VALUE_TYPE_BOOLEAN, 1, 0));
        sql = database.getDDL(tablename, table, null, false, "ID_VERSION", false);

        if (sql != null && sql.length() > 0)
        {
            try
            {
                if (log.isDetailed()) log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
                database.execStatements(sql);
                if (log.isDetailed()) log.logDetailed(toString(), "Created/altered table " + tablename);
            }
            catch (KettleDatabaseException dbe)
            {
                throw new KettleDatabaseException("Unable to create or modify table " + tablename, dbe);
            }
        }
        else
        {
            if (log.isDetailed()) log.logDetailed(toString(), "Table " + tablename + " is OK.");
        }

        // Insert an extra record in R_VERSION every time we pass here...
        //
        try
        {
            table.getValue(0).setValue(getNextID(tablename, "ID_VERSION"));
            table.getValue(1).setValue(REQUIRED_MAJOR_VERSION);
            table.getValue(2).setValue(REQUIRED_MINOR_VERSION);
            table.getValue(3).setValue(new Date());
            table.getValue(4).setValue(upgrade);
            database.execStatement("INSERT INTO R_VERSION VALUES(?, ?, ?, ?, ?)", table);
        }
        catch(KettleDatabaseException e)
        {
            throw new KettleDatabaseException("Unable to insert new version log record into "+tablename, e);
        }
        
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
                if (log.isDetailed()) log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
				database.execStatements(sql);
                if (log.isDetailed()) log.logDetailed(toString(), "Created/altered table " + tablename);
			}
			catch (KettleDatabaseException dbe)
			{
				throw new KettleDatabaseException("Unable to create or modify table " + tablename, dbe);
			}
		}
		else
		{
            if (log.isDetailed()) log.logDetailed(toString(), "Table " + tablename + " is OK.");
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
                if (upgrade) lookup = database.getOneRow("SELECT ID_DATABASE_TYPE FROM " + tablename + " WHERE " 
                		+ database.getDatabaseMeta().quoteField("CODE") +" = '" + code[i] + "'");
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
                if (log.isDetailed()) log.logDetailed(toString(), "Populated table " + tablename);
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
            if (log.isDetailed()) log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
			database.execStatements(sql);
            if (log.isDetailed()) log.logDetailed(toString(), "Created or altered table " + tablename);
		}
		else
		{
            if (log.isDetailed()) log.logDetailed(toString(), "Table " + tablename + " is OK.");
		}

		if (ok_database_contype)
		{
			//
			// Populate with data...
			//
			code = DatabaseMeta.dbAccessTypeCode;
			desc = DatabaseMeta.dbAccessTypeDesc;

			database.prepareInsert(table, tablename);

			for (int i = 0; i < code.length; i++)
			{
                Row lookup = null;
                if (upgrade) lookup = database.getOneRow("SELECT ID_DATABASE_CONTYPE FROM " + tablename + " WHERE " 
                		+ database.getDatabaseMeta().quoteField("CODE") + " = '" + code[i] + "'");
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
                if (log.isDetailed()) log.logDetailed(toString(), "Populated table " + tablename);
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
            if (log.isDetailed()) log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
			database.execStatements(sql);
            if (log.isDetailed()) log.logDetailed(toString(), "Created or altered table " + tablename);
		}
		else
		{
            if (log.isDetailed()) log.logDetailed(toString(), "Table " + tablename + " is OK.");
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
            if (log.isDetailed()) log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
			database.execStatements(sql);
            if (log.isDetailed()) log.logDetailed(toString(), "Created or altered table " + tablename);
		}
		else
		{
            if (log.isDetailed()) log.logDetailed(toString(), "Table " + tablename + " is OK.");
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
            if (log.isDetailed()) log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
            database.execStatements(sql);
            if (log.isDetailed()) log.logDetailed(toString(), "Created or altered table " + tablename);
            
            try
            {
                indexname = "IDX_" + tablename.substring(2) + "_AK";
                keyfield = new String[] { "ID_DIRECTORY_PARENT", "DIRECTORY_NAME" };
                if (!database.checkIndexExists(tablename, keyfield))
                {
                    sql = database.getCreateIndexStatement(tablename, indexname, keyfield, false, true, false, false);
                    if (log.isDetailed()) log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
                    database.execStatements(sql);
                    if (log.isDetailed()) log.logDetailed(toString(), "Created lookup index " + indexname + " on " + tablename);
                }
            }
            catch(KettleDatabaseException kdbe)
            {
                // Ignore this one: index is not properly detected, it already exists...
            }

        }
        else
        {
            if (log.isDetailed()) log.logDetailed(toString(), "Table " + tablename + " is OK.");
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
            if (log.isDetailed()) log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
			database.execStatements(sql);
            if (log.isDetailed()) log.logDetailed(toString(), "Created or altered table " + tablename);
			
			try
			{
				indexname = "IDX_" + tablename.substring(2) + "_AK";
				keyfield = new String[] { "ID_DIRECTORY_PARENT", "DIRECTORY_NAME" };
				if (!database.checkIndexExists(tablename, keyfield))
				{
					sql = database.getCreateIndexStatement(tablename, indexname, keyfield, false, true, false, false);
                    if (log.isDetailed()) log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
					database.execStatements(sql);
                    if (log.isDetailed()) log.logDetailed(toString(), "Created lookup index " + indexname + " on " + tablename);
				}
			}
			catch(KettleDatabaseException kdbe)
			{
				// Ignore this one: index is not properly detected, it already exists...
			}

		}
		else
		{
            if (log.isDetailed()) log.logDetailed(toString(), "Table " + tablename + " is OK.");
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
		table.addValue(new Value("DESCRIPTION", Value.VALUE_TYPE_STRING, REP_STRING_LENGTH, 0));
		table.addValue(new Value("EXTENDED_DESCRIPTION", Value.VALUE_TYPE_STRING, REP_STRING_LENGTH, 0));
		table.addValue(new Value("TRANS_VERSION", Value.VALUE_TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
		table.addValue(new Value("TRANS_STATUS", Value.VALUE_TYPE_INTEGER, KEY, 0));
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
		table.addValue(new Value("CREATED_USER", Value.VALUE_TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
		table.addValue(new Value("CREATED_DATE", Value.VALUE_TYPE_DATE, 20, 0));
		table.addValue(new Value("MODIFIED_USER", Value.VALUE_TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
		table.addValue(new Value("MODIFIED_DATE", Value.VALUE_TYPE_DATE, 20, 0));
		table.addValue(new Value("SIZE_ROWSET", Value.VALUE_TYPE_INTEGER, KEY, 0));
		sql = database.getDDL(tablename, table, null, false, "ID_TRANSFORMATION", false);

        if (sql != null && sql.length() > 0)
		{
            if (log.isDetailed()) log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
			database.execStatements(sql);
            if (log.isDetailed()) log.logDetailed(toString(), "Created or altered table " + tablename);
		}
		else
		{
            if (log.isDetailed()) log.logDetailed(toString(), "Table " + tablename + " is OK.");
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
		table.addValue(new Value("VALUE_NUM", Value.VALUE_TYPE_INTEGER, 18, 0));
		table.addValue(new Value("VALUE_STR", Value.VALUE_TYPE_STRING, REP_STRING_LENGTH, 0));
		sql = database.getDDL(tablename, table, null, false, "ID_TRANS_ATTRIBUTE", false);

		if (sql != null && sql.length() > 0)
		{
            if (log.isDetailed()) log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
			database.execStatements(sql);
            if (log.isDetailed()) log.logDetailed(toString(), "Created or altered table " + tablename);

			try
			{
				indexname = "IDX_TRANS_ATTRIBUTE_LOOKUP";
				keyfield = new String[] { "ID_TRANSFORMATION", "CODE", "NR" };

				if (!database.checkIndexExists(tablename, keyfield))
				{
					sql = database.getCreateIndexStatement(tablename, indexname, keyfield, false, true, false, false);
	
                    if (log.isDetailed()) log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
					database.execStatements(sql);
                    if (log.isDetailed()) log.logDetailed(toString(), "Created lookup index " + indexname + " on " + tablename);
				}
			}
			catch(KettleDatabaseException kdbe)
			{
				// Ignore this one: index is not properly detected, it already exists...
			}
		}
		else
		{
            if (log.isDetailed()) log.logDetailed(toString(), "Table " + tablename + " is OK.");
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
            if (log.isDetailed()) log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
			database.execStatements(sql);
            if (log.isDetailed()) log.logDetailed(toString(), "Created or altered table " + tablename);
		}
		else
		{
            if (log.isDetailed()) log.logDetailed(toString(), "Table " + tablename + " is OK.");
		}
		if (monitor!=null) monitor.worked(1);

        //////////////////////////////////////////////////////////////////////////////////
        //
        // R_PARTITION_SCHEMA
        //
        // Create table...
        table = new Row();
        tablename = "R_PARTITION_SCHEMA";
        if (monitor!=null) monitor.subTask("Checking table "+tablename);
        table.addValue(new Value("ID_PARTITION_SCHEMA", Value.VALUE_TYPE_INTEGER, KEY, 0));
        table.addValue(new Value("NAME", Value.VALUE_TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
        sql = database.getDDL(tablename, table, null, false, "ID_PARTITION_SCHEMA", false);

        if (sql != null && sql.length() > 0)
        {
            if (log.isDetailed()) log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
            database.execStatements(sql);
            if (log.isDetailed()) log.logDetailed(toString(), "Created or altered table " + tablename);
        }
        else
        {
            if (log.isDetailed()) log.logDetailed(toString(), "Table " + tablename + " is OK.");
        }
        if (monitor!=null) monitor.worked(1);
        
        //////////////////////////////////////////////////////////////////////////////////
        //
        // R_PARTITION
        //
        // Create table...
        table = new Row();
        tablename = "R_PARTITION";
        if (monitor!=null) monitor.subTask("Checking table "+tablename);
        table.addValue(new Value("ID_PARTITION", Value.VALUE_TYPE_INTEGER, KEY, 0));
        table.addValue(new Value("ID_PARTITION_SCHEMA", Value.VALUE_TYPE_INTEGER, KEY, 0));
        table.addValue(new Value("PARTITION_ID", Value.VALUE_TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
        sql = database.getDDL(tablename, table, null, false, "ID_PARTITION", false);

        if (sql != null && sql.length() > 0)
        {
            if (log.isDetailed()) log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
            database.execStatements(sql);
            if (log.isDetailed()) log.logDetailed(toString(), "Created or altered table " + tablename);
        }
        else
        {
            if (log.isDetailed()) log.logDetailed(toString(), "Table " + tablename + " is OK.");
        }
        if (monitor!=null) monitor.worked(1);

        //////////////////////////////////////////////////////////////////////////////////
        //
        // R_TRANS_PARTITION_SCHEMA
        //
        // Create table...
        table = new Row();
        tablename = "R_TRANS_PARTITION_SCHEMA";
        if (monitor!=null) monitor.subTask("Checking table "+tablename);
        table.addValue(new Value("ID_TRANS_PARTITION_SCHEMA", Value.VALUE_TYPE_INTEGER, KEY, 0));
        table.addValue(new Value("ID_TRANSFORMATION", Value.VALUE_TYPE_INTEGER, KEY, 0));
        table.addValue(new Value("ID_PARTITION_SCHEMA", Value.VALUE_TYPE_INTEGER, KEY, 0));
        sql = database.getDDL(tablename, table, null, false, "ID_TRANS_PARTITION_SCHEMA", false);

        if (sql != null && sql.length() > 0)
        {
            if (log.isDetailed()) log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
            database.execStatements(sql);
            if (log.isDetailed()) log.logDetailed(toString(), "Created or altered table " + tablename);
        }
        else
        {
            if (log.isDetailed()) log.logDetailed(toString(), "Table " + tablename + " is OK.");
        }
        if (monitor!=null) monitor.worked(1);


        //////////////////////////////////////////////////////////////////////////////////
        //
        // R_CLUSTER
        //
        // Create table...
        table = new Row();
        tablename = "R_CLUSTER";
        if (monitor!=null) monitor.subTask("Checking table "+tablename);
        table.addValue(new Value("ID_CLUSTER", Value.VALUE_TYPE_INTEGER, KEY, 0));
        table.addValue(new Value("NAME", Value.VALUE_TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
        table.addValue(new Value("BASE_PORT", Value.VALUE_TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
        table.addValue(new Value("SOCKETS_BUFFER_SIZE", Value.VALUE_TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
        table.addValue(new Value("SOCKETS_FLUSH_INTERVAL", Value.VALUE_TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
        table.addValue(new Value("SOCKETS_COMPRESSED", Value.VALUE_TYPE_BOOLEAN, 0, 0));
        sql = database.getDDL(tablename, table, null, false, "ID_CLUSTER", false);

        if (sql != null && sql.length() > 0)
        {
            if (log.isDetailed()) log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
            database.execStatements(sql);
            if (log.isDetailed()) log.logDetailed(toString(), "Created or altered table " + tablename);
        }
        else
        {
            if (log.isDetailed()) log.logDetailed(toString(), "Table " + tablename + " is OK.");
        }
        if (monitor!=null) monitor.worked(1);
        
        //////////////////////////////////////////////////////////////////////////////////
        //
        // R_SLAVE
        //
        // Create table...
        table = new Row();
        tablename = "R_SLAVE";
        if (monitor!=null) monitor.subTask("Checking table "+tablename);
        table.addValue(new Value("ID_SLAVE", Value.VALUE_TYPE_INTEGER, KEY, 0));
        table.addValue(new Value("NAME", Value.VALUE_TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
        table.addValue(new Value("HOST_NAME", Value.VALUE_TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
        table.addValue(new Value("PORT", Value.VALUE_TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
        table.addValue(new Value("USERNAME", Value.VALUE_TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
        table.addValue(new Value("PASSWORD", Value.VALUE_TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
        table.addValue(new Value("PROXY_HOST_NAME", Value.VALUE_TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
        table.addValue(new Value("PROXY_PORT", Value.VALUE_TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
        table.addValue(new Value("NON_PROXY_HOSTS", Value.VALUE_TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
        table.addValue(new Value("MASTER", Value.VALUE_TYPE_BOOLEAN));
        sql = database.getDDL(tablename, table, null, false, "ID_SLAVE", false);

        if (sql != null && sql.length() > 0)
        {
            if (log.isDetailed()) log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
            database.execStatements(sql);
            if (log.isDetailed()) log.logDetailed(toString(), "Created or altered table " + tablename);
        }
        else
        {
            if (log.isDetailed()) log.logDetailed(toString(), "Table " + tablename + " is OK.");
        }
        if (monitor!=null) monitor.worked(1);

        //////////////////////////////////////////////////////////////////////////////////
        //
        // R_CLUSTER_SLAVE
        //
        // Create table...
        table = new Row();
        tablename = "R_CLUSTER_SLAVE";
        if (monitor!=null) monitor.subTask("Checking table "+tablename);
        table.addValue(new Value("ID_CLUSTER_SLAVE", Value.VALUE_TYPE_INTEGER, KEY, 0));
        table.addValue(new Value("ID_CLUSTER", Value.VALUE_TYPE_INTEGER, KEY, 0));
        table.addValue(new Value("ID_SLAVE", Value.VALUE_TYPE_INTEGER, KEY, 0));
        sql = database.getDDL(tablename, table, null, false, "ID_CLUSTER_SLAVE", false);

        if (sql != null && sql.length() > 0)
        {
            if (log.isDetailed()) log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
            database.execStatements(sql);
            if (log.isDetailed()) log.logDetailed(toString(), "Created or altered table " + tablename);
        }
        else
        {
            if (log.isDetailed()) log.logDetailed(toString(), "Table " + tablename + " is OK.");
        }
        if (monitor!=null) monitor.worked(1);

        //////////////////////////////////////////////////////////////////////////////////
        //
        // R_TRANS_SLAVE
        //
        // Create table...
        table = new Row();
        tablename = "R_TRANS_SLAVE";
        if (monitor!=null) monitor.subTask("Checking table "+tablename);
        table.addValue(new Value("ID_TRANS_SLAVE", Value.VALUE_TYPE_INTEGER, KEY, 0));
        table.addValue(new Value("ID_TRANSFORMATION", Value.VALUE_TYPE_INTEGER, KEY, 0));
        table.addValue(new Value("ID_SLAVE", Value.VALUE_TYPE_INTEGER, KEY, 0));
        sql = database.getDDL(tablename, table, null, false, "ID_TRANS_SLAVE", false);

        if (sql != null && sql.length() > 0)
        {
            if (log.isDetailed()) log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
            database.execStatements(sql);
            if (log.isDetailed()) log.logDetailed(toString(), "Created or altered table " + tablename);
        }
        else
        {
            if (log.isDetailed()) log.logDetailed(toString(), "Table " + tablename + " is OK.");
        }
        if (monitor!=null) monitor.worked(1);


        //////////////////////////////////////////////////////////////////////////////////
        //
        // R_TRANS_CLUSTER
        //
        // Create table...
        table = new Row();
        tablename = "R_TRANS_CLUSTER";
        if (monitor!=null) monitor.subTask("Checking table "+tablename);
        table.addValue(new Value("ID_TRANS_CLUSTER", Value.VALUE_TYPE_INTEGER, KEY, 0));
        table.addValue(new Value("ID_TRANSFORMATION", Value.VALUE_TYPE_INTEGER, KEY, 0));
        table.addValue(new Value("ID_CLUSTER", Value.VALUE_TYPE_INTEGER, KEY, 0));
        sql = database.getDDL(tablename, table, null, false, "ID_TRANS_CLUSTER", false);

        if (sql != null && sql.length() > 0)
        {
            if (log.isDetailed()) log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
            database.execStatements(sql);
            if (log.isDetailed()) log.logDetailed(toString(), "Created or altered table " + tablename);
        }
        else
        {
            if (log.isDetailed()) log.logDetailed(toString(), "Table " + tablename + " is OK.");
        }
        if (monitor!=null) monitor.worked(1);
        
        //////////////////////////////////////////////////////////////////////////////////
        //
        // R_TRANS_SLAVE
        //
        // Create table...
        table = new Row();
        tablename = "R_TRANS_SLAVE";
        if (monitor!=null) monitor.subTask("Checking table "+tablename);
        table.addValue(new Value("ID_TRANS_SLAVE", Value.VALUE_TYPE_INTEGER, KEY, 0));
        table.addValue(new Value("ID_TRANSFORMATION", Value.VALUE_TYPE_INTEGER, KEY, 0));
        table.addValue(new Value("ID_SLAVE", Value.VALUE_TYPE_INTEGER, KEY, 0));
        sql = database.getDDL(tablename, table, null, false, "ID_TRANS_SLAVE", false);

        if (sql != null && sql.length() > 0)
        {
            if (log.isDetailed()) log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
            database.execStatements(sql);
            if (log.isDetailed()) log.logDetailed(toString(), "Created or altered table " + tablename);
        }
        else
        {
            if (log.isDetailed()) log.logDetailed(toString(), "Table " + tablename + " is OK.");
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
            if (log.isDetailed()) log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
			database.execStatements(sql);
            if (log.isDetailed()) log.logDetailed(toString(), "Created or altered table " + tablename);
		}
		else
		{
            if (log.isDetailed()) log.logDetailed(toString(), "Table " + tablename + " is OK.");
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
            if (log.isDetailed()) log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
			database.execStatements(sql);
            if (log.isDetailed()) log.logDetailed(toString(), "Created or altered table " + tablename);
		}
		else
		{
            if (log.isDetailed()) log.logDetailed(toString(), "Table " + tablename + " is OK.");
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
            if (log.isDetailed()) log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
			database.execStatements(sql);
            if (log.isDetailed()) log.logDetailed(toString(), "Created or altered table " + tablename);
		}
		else
		{
            if (log.isDetailed()) log.logDetailed(toString(), "Table " + tablename + " is OK.");
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
		table.addValue(new Value("VALUE_STR", Value.VALUE_TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
		table.addValue(new Value("IS_NULL", Value.VALUE_TYPE_BOOLEAN, 1, 0));
		sql = database.getDDL(tablename, table, null, false, "ID_VALUE", false);

		if (sql != null && sql.length() > 0) // Doesn't exists: create the table...
		{
            if (log.isDetailed()) log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
			database.execStatements(sql);
            if (log.isDetailed()) log.logDetailed(toString(), "Created or altered table " + tablename);
		}
		else
		{
            if (log.isDetailed()) log.logDetailed(toString(), "Table " + tablename + " is OK.");
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
            if (log.isDetailed()) log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
			database.execStatements(sql);
            if (log.isDetailed()) log.logDetailed(toString(), "Created or altered table " + tablename);
		}
		else
		{
            if (log.isDetailed()) log.logDetailed(toString(), "Table " + tablename + " is OK.");
		}

		if (ok_step_type)
		{
			updateStepTypes();
            if (log.isDetailed()) log.logDetailed(toString(), "Populated table " + tablename);
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
            if (log.isDetailed()) log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
			database.execStatements(sql);
            if (log.isDetailed()) log.logDetailed(toString(), "Created or altered table " + tablename);
		}
		else
		{
            if (log.isDetailed()) log.logDetailed(toString(), "Table " + tablename + " is OK.");
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
		table.addValue(new Value("VALUE_NUM", Value.VALUE_TYPE_INTEGER, 18, 0));
		table.addValue(new Value("VALUE_STR", Value.VALUE_TYPE_STRING, REP_STRING_LENGTH, 0));
        sql = database.getDDL(tablename, table, null, false, "ID_STEP_ATTRIBUTE", false);
        
		if (sql != null && sql.length() > 0) // Doesn't exist: create the table...
		{
            if (log.isDetailed()) log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
			database.execStatements(sql);
            if (log.isDetailed()) log.logDetailed(toString(), "Created or altered table " + tablename);

			try
			{
				indexname = "IDX_" + tablename.substring(2) + "_LOOKUP";
				keyfield = new String[] { "ID_STEP", "CODE", "NR" };
				if (!database.checkIndexExists(tablename, keyfield))
				{
					sql = database.getCreateIndexStatement(tablename, indexname, keyfield, false, true, false, false);
                    if (log.isDetailed()) log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
					database.execStatements(sql);
                    if (log.isDetailed()) log.logDetailed(toString(), "Created lookup index " + indexname + " on " + tablename);
				}
			}
			catch(KettleDatabaseException kdbe)
			{
				// Ignore this one: index is not properly detected, it already exists...
			}
		}
		else
		{
            if (log.isDetailed()) log.logDetailed(toString(), "Table " + tablename + " is OK.");
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
            if (log.isDetailed()) log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
			database.execStatements(sql);
            if (log.isDetailed()) log.logDetailed(toString(), "Created or altered table " + tablename);

			try
			{
				indexname = "IDX_" + tablename.substring(2) + "_LU1";
				keyfield = new String[] { "ID_TRANSFORMATION" };
				if (!database.checkIndexExists(tablename, keyfield))
				{
					sql = database.getCreateIndexStatement(tablename, indexname, keyfield, false, false, false, false);
                    if (log.isDetailed()) log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
					database.execStatements(sql);
                    if (log.isDetailed()) log.logDetailed(toString(), "Created lookup index " + indexname + " on " + tablename);
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
                    if (log.isDetailed()) log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
					database.execStatements(sql);
                    if (log.isDetailed()) log.logDetailed(toString(), "Created lookup index " + indexname + " on " + tablename);
				}
			}
			catch(KettleDatabaseException kdbe)
			{
				// Ignore this one: index is not properly detected, it already exists...
			}
		}
		else
		{
            if (log.isDetailed()) log.logDetailed(toString(), "Table " + tablename + " is OK.");
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
            if (log.isDetailed()) log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
			database.execStatements(sql);
            if (log.isDetailed()) log.logDetailed(toString(), "Created or altered table " + tablename);
		}
		else
		{
            if (log.isDetailed()) log.logDetailed(toString(), "Table " + tablename + " is OK.");
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
            if (log.isDetailed()) log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
			database.execStatements(sql);
            if (log.isDetailed()) log.logDetailed(toString(), "Created or altered table " + tablename);
		}
		else
		{
            if (log.isDetailed()) log.logDetailed(toString(), "Table " + tablename + " is OK.");
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
                if (upgrade) lookup = database.getOneRow("SELECT ID_LOGLEVEL FROM " + tablename + " WHERE " 
                		+ database.getDatabaseMeta().quoteField("CODE") + " = '" + code[i] + "'");
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
                if (log.isDetailed()) log.logDetailed(toString(), "Populated table " + tablename);
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
            if (log.isDetailed()) log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
			database.execStatements(sql);
            if (log.isDetailed()) log.logDetailed(toString(), "Created or altered table " + tablename);
		}
		else
		{
            if (log.isDetailed()) log.logDetailed(toString(), "Table " + tablename + " is OK.");
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
        table.addValue(new Value("DESCRIPTION", Value.VALUE_TYPE_STRING, REP_STRING_LENGTH, 0));
        table.addValue(new Value("EXTENDED_DESCRIPTION", Value.VALUE_TYPE_STRING, REP_STRING_LENGTH, 0));
        table.addValue(new Value("JOB_VERSION", Value.VALUE_TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
        table.addValue(new Value("JOB_STATUS", Value.VALUE_TYPE_INTEGER, KEY, 0));
		table.addValue(new Value("ID_DATABASE_LOG", Value.VALUE_TYPE_INTEGER, KEY, 0));
		table.addValue(new Value("TABLE_NAME_LOG", Value.VALUE_TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
        table.addValue(new Value("CREATED_USER", Value.VALUE_TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
        table.addValue(new Value("CREATED_DATE", Value.VALUE_TYPE_DATE, 20, 0));
		table.addValue(new Value("MODIFIED_USER", Value.VALUE_TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
		table.addValue(new Value("MODIFIED_DATE", Value.VALUE_TYPE_DATE, 20, 0));
        table.addValue(new Value("USE_BATCH_ID", Value.VALUE_TYPE_BOOLEAN, 0, 0));
        table.addValue(new Value("PASS_BATCH_ID", Value.VALUE_TYPE_BOOLEAN, 0, 0));
        table.addValue(new Value("USE_LOGFIELD", Value.VALUE_TYPE_BOOLEAN, 0, 0));
        table.addValue(new Value("SHARED_FILE", Value.VALUE_TYPE_STRING, REP_STRING_CODE_LENGTH, 0)); // 255 max length for now.
		sql = database.getDDL(tablename, table, null, false, "ID_JOB", false);

		if (sql != null && sql.length() > 0) // Doesn't exist: create the table...
		{
            if (log.isDetailed()) log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
			database.execStatements(sql);
            if (log.isDetailed()) log.logDetailed(toString(), "Created or altered table " + tablename);
		}
		else
		{
            if (log.isDetailed()) log.logDetailed(toString(), "Table " + tablename + " is OK.");
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
            if (log.isDetailed()) log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
			database.execStatements(sql);
            if (log.isDetailed()) log.logDetailed(toString(), "Created or altered table " + tablename);
		}
		else
		{
            if (log.isDetailed()) log.logDetailed(toString(), "Table " + tablename + " is OK.");
		}

		if (ok_jobentry_type)
		{
			//
			// Populate with data...
			//
			updateJobEntryTypes();
            if (log.isDetailed()) log.logDetailed(toString(), "Populated table " + tablename);
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
		table.addValue(new Value("DESCRIPTION", Value.VALUE_TYPE_STRING, REP_STRING_LENGTH, 0));
		sql = database.getDDL(tablename, table, null, false, "ID_JOBENTRY", false);

		if (sql != null && sql.length() > 0) // Doesn't exist: create the table...
		{
            if (log.isDetailed()) log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
			database.execStatements(sql);
            if (log.isDetailed()) log.logDetailed(toString(), "Created or altered table " + tablename);
		}
		else
		{
            if (log.isDetailed()) log.logDetailed(toString(), "Table " + tablename + " is OK.");
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
            if (log.isDetailed()) log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
			database.execStatements(sql);
            if (log.isDetailed()) log.logDetailed(toString(), "Created or altered table " + tablename);
		}
		else
		{
            if (log.isDetailed()) log.logDetailed(toString(), "Table " + tablename + " is OK.");
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
            if (log.isDetailed()) log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
			database.execStatements(sql);
            if (log.isDetailed()) log.logDetailed(toString(), "Created or altered table " + tablename);

			try
			{
				indexname = "IDX_" + tablename.substring(2) + "_LOOKUP";
				keyfield = new String[] { "ID_JOBENTRY_ATTRIBUTE", "CODE", "NR" };
	
				if (!database.checkIndexExists(tablename, keyfield))
				{
					sql = database.getCreateIndexStatement(tablename, indexname, keyfield, false, true, false, false);
	
                    if (log.isDetailed()) log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
					database.execStatements(sql);
                    if (log.isDetailed()) log.logDetailed(toString(), "Created lookup index " + indexname + " on " + tablename);
				}
			}
			catch(KettleDatabaseException kdbe)
			{
				// Ignore this one: index is not properly detected, it already exists...
			}
		}
		else
		{
            if (log.isDetailed()) log.logDetailed(toString(), "Table " + tablename + " is OK.");
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
            if (log.isDetailed()) log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
			database.execStatements(sql);
            if (log.isDetailed()) log.logDetailed(toString(), "Created or altered table " + tablename);
		}
		else
		{
            if (log.isDetailed()) log.logDetailed(toString(), "Table " + tablename + " is OK.");
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
            if (log.isDetailed()) log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
			database.execStatements(sql);
            if (log.isDetailed()) log.logDetailed(toString(), "Created or altered table " + tablename);
		}
		else
		{
            if (log.isDetailed()) log.logDetailed(toString(), "Table " + tablename + " is OK.");
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
            if (log.isDetailed()) log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
			database.execStatements(sql);
            if (log.isDetailed()) log.logDetailed(toString(), "Created or altered table " + tablename);
		}
		else
		{
            if (log.isDetailed()) log.logDetailed(toString(), "Table " + tablename + " is OK.");
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
                if (upgrade) lookup = database.getOneRow("SELECT ID_PROFILE FROM " + tablename + " WHERE "
                		+ database.getDatabaseMeta().quoteField("NAME") + " = '" + code[i] + "'");
				if (lookup == null)
				{
					long nextid = getNextProfileID();

					table = new Row();
					table.addValue(new Value("ID_PROFILE", nextid));
					table.addValue(new Value("NAME", code[i]));
					table.addValue(new Value("DESCRIPTION", desc[i]));

					database.setValuesInsert(table);
					database.insertRow();
                    if (log.isDetailed()) log.logDetailed(toString(), "Inserted new row into table "+tablename+" : "+table);
                    profiles.put(code[i], new Long(nextid));
				}
			}

            try
            {
                database.closeInsert();
                if (log.isDetailed()) log.logDetailed(toString(), "Populated table " + tablename);
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
            if (log.isDetailed()) log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
			database.execStatements(sql);
            if (log.isDetailed()) log.logDetailed(toString(), "Created or altered table " + tablename);
		}
		else
		{
            if (log.isDetailed()) log.logDetailed(toString(), "Table " + tablename + " is OK.");
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
                if (upgrade) lookup = database.getOneRow("SELECT ID_USER FROM " + tablename + " WHERE "
                		+ database.getDatabaseMeta().quoteField("LOGIN") + " = '" + user[i] + "'");
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
                if (log.isDetailed()) log.logDetailed(toString(), "Populated table " + tablename);
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
            if (log.isDetailed()) log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
			database.execStatements(sql);
            if (log.isDetailed()) log.logDetailed(toString(), "Created or altered table " + tablename);
		}
		else
		{
            if (log.isDetailed()) log.logDetailed(toString(), "Table " + tablename + " is OK.");
		}

		if (ok_permission)
		{
			//
			// Populate with data...
			//
			code = PermissionMeta.permissionTypeCode;
			desc = PermissionMeta.permissionTypeDesc;

			database.prepareInsert(table, tablename);

			for (int i = 1; i < code.length; i++)
			{
                Row lookup = null;
                if (upgrade) lookup = database.getOneRow("SELECT ID_PERMISSION FROM " + tablename + " WHERE " 
                		+ database.getDatabaseMeta().quoteField("CODE") + " = '" + code[i] + "'");
				if (lookup == null)
				{
					long nextid = getNextPermissionID();

					table = new Row();
					table.addValue(new Value("ID_PERMISSION", nextid));
					table.addValue(new Value("CODE", code[i]));
					table.addValue(new Value("DESCRIPTION", desc[i]));

					database.setValuesInsert(table);
					database.insertRow();
                    if (log.isDetailed()) log.logDetailed(toString(), "Inserted new row into table "+tablename+" : "+table);
                    permissions.put(code[i], new Long(nextid));
				}
			}

            try
            {
                database.closeInsert();
                if (log.isDetailed()) log.logDetailed(toString(), "Populated table " + tablename);
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
            if (log.isDetailed()) log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
			database.execStatements(sql);
            if (log.isDetailed()) log.logDetailed(toString(), "Created or altered table " + tablename);

			try
			{
				indexname = "IDX_" + tablename.substring(2) + "_PK";
				keyfield = new String[] { "ID_PROFILE", "ID_PERMISSION" };
				if (!database.checkIndexExists(tablename, keyfield))
				{
					sql = database.getCreateIndexStatement(tablename, indexname, keyfield, false, true, false, false);
	
                    if (log.isDetailed()) log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
					database.execStatements(sql);
                    if (log.isDetailed()) log.logDetailed(toString(), "Created lookup index " + indexname + " on " + tablename);
				}
			}
			catch(KettleDatabaseException kdbe)
			{
				// Ignore this one: index is not properly detected, it already exists...
			}
		}
		else
		{
            if (log.isDetailed()) log.logDetailed(toString(), "Table " + tablename + " is OK.");
		}

		if (ok_profile_permission)
		{
			database.prepareInsert(table, tablename);

			// Administrator default:
            Long profileID = (Long)profiles.get( "Administrator");
            long id_profile = -1L;
            if (profileID!=null) id_profile = profileID.longValue();
			
            if (log.isDetailed()) log.logDetailed(toString(), "Administrator profile id = "+id_profile);
            String perms[] = new String[]
				{ 
                    PermissionMeta.permissionTypeCode[PermissionMeta.TYPE_PERMISSION_ADMIN],
                    PermissionMeta.permissionTypeCode[PermissionMeta.TYPE_PERMISSION_TRANSFORMATION],
                    PermissionMeta.permissionTypeCode[PermissionMeta.TYPE_PERMISSION_JOB],
                    PermissionMeta.permissionTypeCode[PermissionMeta.TYPE_PERMISSION_SCHEMA] 
				};
			
			for (int i=0;i < perms.length ; i++)
			{
                Long permissionID = (Long) permissions.get(perms[i]);
                long id_permission = -1L;
                if (permissionID!=null) id_permission = permissionID.longValue();
                
                if (log.isDetailed()) log.logDetailed(toString(), "Permission id for '"+perms[i]+"' = "+id_permission);

				Row lookup = null;
                if (upgrade) 
                {
                    String lookupSQL = "SELECT ID_PROFILE FROM " + tablename + " WHERE ID_PROFILE=" + id_profile + " AND ID_PERMISSION=" + id_permission;
                    if (log.isDetailed()) log.logDetailed(toString(), "Executing SQL: "+lookupSQL);
                    lookup = database.getOneRow(lookupSQL);
                }
				if (lookup == null) // if the combination is not yet there, insert...
				{
                    String insertSQL="INSERT INTO "+tablename+"(ID_PROFILE, ID_PERMISSION) VALUES("+id_profile+","+id_permission+")";
					database.execStatement(insertSQL);
                    if (log.isDetailed()) log.logDetailed(toString(), "insertSQL = ["+insertSQL+"]");
				}
				else
				{
                    if (log.isDetailed()) log.logDetailed(toString(), "Found id_profile="+id_profile+", id_permission="+id_permission);
				}
			}

			// User profile
            profileID = (Long)profiles.get( "User" );
            id_profile = -1L;
            if (profileID!=null) id_profile = profileID.longValue();
            
            if (log.isDetailed()) log.logDetailed(toString(), "User profile id = "+id_profile);
            perms = new String[]
                { 
                      PermissionMeta.permissionTypeCode[PermissionMeta.TYPE_PERMISSION_TRANSFORMATION],
                      PermissionMeta.permissionTypeCode[PermissionMeta.TYPE_PERMISSION_JOB],
                      PermissionMeta.permissionTypeCode[PermissionMeta.TYPE_PERMISSION_SCHEMA] 
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
                if (log.isDetailed()) log.logDetailed(toString(), "Populated table " + tablename);
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

	public boolean dropRepositorySchema() throws KettleDatabaseException
	{
		// Make sure we close shop before dropping everything. 
		// Some DB's can't handle the drop otherwise.
		//
		closeStepAttributeInsertPreparedStatement();
		closeLookupJobEntryAttribute();
		
		for (int i = 0; i < repositoryTableNames.length; i++)
		{
			try
			{
				database.execStatement("DROP TABLE " + repositoryTableNames[i]);
                if (log.isDetailed()) log.logDetailed(toString(), "dropped table "+repositoryTableNames[i]);
			}
			catch (KettleDatabaseException dbe)
			{
                if (log.isDetailed()) log.logDetailed(toString(), "Unable to drop table: " + repositoryTableNames[i]);
			}
		}
        log.logBasic(toString(), "Dropped all "+repositoryTableNames.length+" repository tables.");
        
        // perform commit, for some DB's drop is not autocommit.
        if (!database.isAutoCommit()) database.commit(); 
        
		return true;
	}

	/**
	 * Update the list in R_STEP_TYPE using the steploader StepPlugin entries
	 * 
	 * @throws KettleDatabaseException if the update didn't go as planned.
	 */
	public synchronized void updateStepTypes() throws KettleDatabaseException
	{
		// We should only do an update if something has changed...
		for (int i = 0; i < steploader.nrStepsWithType(StepPlugin.TYPE_ALL); i++)
		{
			StepPlugin sp = steploader.getStepWithType(StepPlugin.TYPE_ALL, i);
			long id = getStepTypeID(sp.getID()[0]);
			if (id < 0) // Not found, we need to add this one...
			{
				// We need to add this one ...
				id = getNextStepTypeID();

				Row table = new Row();
				table.addValue(new Value("ID_STEP_TYPE", id));
				table.addValue(new Value("CODE", sp.getID()[0]));
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
	public synchronized void updateJobEntryTypes() throws KettleDatabaseException
	{
        // We should only do an update if something has changed...
        JobEntryLoader jobEntryLoader = JobEntryLoader.getInstance();
        JobPlugin[] jobPlugins = jobEntryLoader.getJobEntriesWithType(JobPlugin.TYPE_ALL);
        
        for (int i = 0; i < jobPlugins.length; i++)
        {
            String type_desc = jobPlugins[i].getID();
            String type_desc_long = jobPlugins[i].getDescription();
            long id = getJobEntryTypeID(type_desc);
            if (id < 0) // Not found, we need to add this one...
            {
                // We need to add this one ...
                id = getNextJobEntryTypeID();

                Row table = new Row();
                table.addValue(new Value("ID_JOBENTRY_TYPE", id));
                table.addValue(new Value("CODE", type_desc));
                table.addValue(new Value("DESCRIPTION", type_desc_long));

                database.prepareInsert(table, "R_JOBENTRY_TYPE");

                database.setValuesInsert(table);
                database.insertRow();
                database.closeInsert();
            }
        }
	}


	public synchronized String toString()
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
        this.databaseMeta = database.getDatabaseMeta();
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
    public synchronized void setDirectoryTree(RepositoryDirectory directoryTree)
    {
        this.directoryTree = directoryTree;
    }
    
    public synchronized void lockRepository() throws KettleDatabaseException
    {
        if (database.getDatabaseMeta().needsToLockAllTables())
        {
            database.lockTables(repositoryTableNames);
        }
        else
        {
            database.lockTables( new String[] { "R_REPOSITORY_LOG" } );
        }
    }
    
    public synchronized void unlockRepository() throws KettleDatabaseException
    {
        if (database.getDatabaseMeta().needsToLockAllTables())
        {
            database.unlockTables(repositoryTableNames);
        }
        else
        {
            database.unlockTables(new String[] { "R_REPOSITORY_LOG" });
        }
    }
    
    public synchronized void exportAllObjects(IProgressMonitor monitor, String xmlFilename) throws KettleException
    {
        if (monitor!=null) monitor.beginTask("Exporting the repository to XML...", 3);
        
        StringBuffer xml = new StringBuffer(XMLHandler.getXMLHeader()); 
        xml.append("<repository>"+Const.CR+Const.CR);

        // Dump the transformations...
        xml.append("<transformations>"+Const.CR);
        xml.append(exportTransformations(monitor, getDirectoryTree()));
        xml.append("</transformations>"+Const.CR);

        // Now dump the jobs...
        xml.append("<jobs>"+Const.CR);
        xml.append(exportJobs(monitor, getDirectoryTree()));
        xml.append("</jobs>"+Const.CR);

        xml.append("</repository>"+Const.CR+Const.CR);
        
        if (monitor!=null) monitor.worked(1);

        if (monitor==null || (monitor!=null && !monitor.isCanceled()))
        {
            if (monitor!=null) monitor.subTask("Saving XML to file ["+xmlFilename+"]");

            try
            {
                OutputStream os = KettleVFS.getOutputStream(xmlFilename, false);
                os.write(xml.toString().getBytes(Const.XML_ENCODING));
                os.close();
            }
            catch(IOException e)
            {
                System.out.println("Couldn't create file ["+xmlFilename+"]");
            }
            if (monitor!=null) monitor.worked(1);
        }
        
        if (monitor!=null) monitor.done();
    }

    private String exportJobs(IProgressMonitor monitor, RepositoryDirectory dirTree) throws KettleDatabaseException
    {
        StringBuffer xml = new StringBuffer();
        
        // Loop over all the directory id's
        long dirids[] = dirTree.getDirectoryIDs();
        System.out.println("Going through "+dirids.length+" directories in directory ["+dirTree.getPath()+"]");
 
        if (monitor!=null) monitor.subTask("Exporting the jobs...");
        
        for (int d=0;d<dirids.length && (monitor==null || (monitor!=null && !monitor.isCanceled()));d++)
        {
            RepositoryDirectory repdir = dirTree.findDirectory(dirids[d]);

            String jobs[]  = getJobNames(dirids[d]);
            for (int i=0;i<jobs.length && (monitor==null || (monitor!=null && !monitor.isCanceled()));i++)
            {
                try
                {
                    JobMeta ji = new JobMeta(log, this, jobs[i], repdir);
                    System.out.println("Loading/Exporting job ["+repdir.getPath()+" : "+jobs[i]+"]");
                    if (monitor!=null) monitor.subTask("Exporting job ["+jobs[i]+"]");
                    
                    xml.append(ji.getXML()+Const.CR);
                }
                catch(KettleException ke)
                {
                    log.logError(toString(), "An error occurred reading job ["+jobs[i]+"] from directory ["+repdir+"] : "+ke.getMessage());
                    log.logError(toString(), "Job ["+jobs[i]+"] from directory ["+repdir+"] was not exported because of a loading error!");
                }
            }
            
            // OK, then export the jobs in the sub-directories as well!
            if (repdir.getID()!=dirTree.getID()) exportJobs(null, repdir);
        }

        return xml.toString();
    }

    private String exportTransformations(IProgressMonitor monitor, RepositoryDirectory dirTree) throws KettleDatabaseException
    {
        StringBuffer xml = new StringBuffer();
        
        if (monitor!=null) monitor.subTask("Exporting the transformations...");

        // Loop over all the directory id's
        long dirids[] = dirTree.getDirectoryIDs();
        System.out.println("Going through "+dirids.length+" directories in directory ["+dirTree.getPath()+"]");
        
        for (int d=0;d<dirids.length && (monitor==null || (monitor!=null && !monitor.isCanceled()) );d++)
        {
            RepositoryDirectory repdir = dirTree.findDirectory(dirids[d]);

            System.out.println("Directory ID #"+d+" : "+dirids[d]+" : "+repdir);

            String trans[] = getTransformationNames(dirids[d]);
            for (int i=0;i<trans.length && (monitor==null || (monitor!=null && !monitor.isCanceled()));i++)
            {
                try
                {
                    TransMeta ti = new TransMeta(this, trans[i], repdir);
                    System.out.println("Loading/Exporting transformation ["+repdir.getPath()+" : "+trans[i]+"]  ("+ti.getDirectory().getPath()+")");
                    if (monitor!=null) monitor.subTask("Exporting transformation ["+trans[i]+"]");
                    
                    xml.append(ti.getXML()+Const.CR);
                }
                catch(KettleException ke)
                {
                    log.logError(toString(), "An error occurred reading transformation ["+trans[i]+"] from directory ["+repdir+"] : "+ke.getMessage());
                    log.logError(toString(), "Transformation ["+trans[i]+"] from directory ["+repdir+"] was not exported because of a loading error!");
                }
            }
            
            // OK, then export the transformations in the sub-directories as well!
            if (repdir.getID()!=dirTree.getID()) exportTransformations(null, repdir);
        }
        if (monitor!=null) monitor.worked(1);
        
        return xml.toString();
    }

    public synchronized static Repository getCurrentRepository()
    {
        return currentRepository;
    }

    public synchronized static void setCurrentRepository(Repository currentRepository)
    {
        Repository.currentRepository = currentRepository;
    }

    /**
     * @return a list of all the databases in the repository.
     * @throws KettleException
     */
    public List getDatabases() throws KettleException
    {
        List list = new ArrayList();
        long[] databaseIDs = getDatabaseIDs();
        for (int i=0;i<databaseIDs.length;i++)
        {
            DatabaseMeta databaseMeta = new DatabaseMeta(this, databaseIDs[i]);
            list.add(databaseMeta);
        }
            
        return list;
    }
    
    /**
     * @return a list of all the slave servers in the repository.
     * @throws KettleException
     */
    public List getSlaveServers() throws KettleException
    {
        List list = new ArrayList();
        long[] slaveIDs = getSlaveIDs();
        for (int i=0;i<slaveIDs.length;i++)
        {
            SlaveServer slaveServer = new SlaveServer(this, slaveIDs[i]);
            list.add(slaveServer);
        }
            
        return list;
    }
}