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

package org.pentaho.di.repository;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.pentaho.di.cluster.ClusterSchema;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.Condition;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Counter;
import org.pentaho.di.core.Counters;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.encryption.Encr;
import org.pentaho.di.core.exception.KettleDependencyException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaAndData;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.job.JobEntryLoader;
import org.pentaho.di.job.JobPlugin;
import org.pentaho.di.partition.PartitionSchema;
import org.pentaho.di.trans.StepLoader;
import org.pentaho.di.trans.StepPlugin;
import org.pentaho.di.trans.TransMeta;


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
	
	private List<Object[]>           stepAttributesBuffer;
	private RowMetaInterface         stepAttributesRowMeta;
	
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
				catch (KettleException dbe)
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
        RowMetaAndData lastUpgrade = null;
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
		catch (KettleException dbe)
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
		catch (KettleException dbe)
		{
			log.logError(toString(), "Error rolling back repository.");
		}
	}
	
	/**
     * @return Returns the stepAttributesBuffer.
     */
    public List<Object[]> getStepAttributesBuffer()
    {
        return stepAttributesBuffer;
    }
    
    /**
     * @param stepAttributesBuffer The stepAttributesBuffer to set.
     */
    public void setStepAttributesBuffer(List<Object[]> stepAttributesBuffer)
    {
        this.stepAttributesBuffer = stepAttributesBuffer;
    }
	
	public synchronized void fillStepAttributesBuffer(long id_transformation) throws KettleException
	{
	    String sql = "SELECT ID_STEP, CODE, NR, VALUE_NUM, VALUE_STR "+
	                 "FROM R_STEP_ATTRIBUTE "+
	                 "WHERE ID_TRANSFORMATION = "+id_transformation+" "+
	                 "ORDER BY ID_STEP, CODE, NR"
	                 ;
	    
	    stepAttributesBuffer = database.getRows(sql, -1);
	    stepAttributesRowMeta = database.getReturnRowMeta();
        
        // Collections.sort(stepAttributesBuffer);  // just to make sure...
	}
	
	private synchronized RowMetaAndData searchStepAttributeInBuffer(long id_step, String code, long nr) throws KettleValueException
	{
	    int idx = searchStepAttributeIndexInBuffer(id_step, code, nr);
	    if (idx<0) return null;
	    
	    // Get the row and remote it from the list...
        Object[] r = stepAttributesBuffer.get(idx);
        // stepAttributesBuffer.remove(idx);
	    
	    return new RowMetaAndData(stepAttributesRowMeta, r);
	}
	
	
	private synchronized int searchStepAttributeIndexInBuffer(long id_step, String code, long nr) throws KettleValueException
	{
        Object[] key = new Object[] {
        		new Long(id_step), // ID_STEP
        		code, // CODE
        		new Long(nr), // NR
        };
        
        final int[] keyPositions = new int[] {0, 1, 2};

        int index = Collections.binarySearch(stepAttributesBuffer, key, 
        		new Comparator<Object[]>() 
        		{
					public int compare(Object[] r1, Object[] r2) 
					{
						try {
							return stepAttributesRowMeta.compare(r1, r2, keyPositions);
						} catch (KettleValueException e) {
							return 0; // conversion errors
						}
					}
				}
        	);
        if (index>=stepAttributesBuffer.size() || index<0) return -1;
        // 
        // Check this...  If it is not, we didn't find it!
        Object[] look = stepAttributesBuffer.get(index);
        
        if (stepAttributesRowMeta.compare(look, key, keyPositions)==0)
        {
            return index;
        }
        
        return -1;
	}

	private synchronized int searchNrStepAttributes(long id_step, String code) throws KettleValueException
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
        Object[] look = (Object[])stepAttributesBuffer.get(idx+offset);
        RowMetaInterface rowMeta = stepAttributesRowMeta;
        
	    long lookID = rowMeta.getInteger(look, 0);
	    String lookCode = rowMeta.getString(look, 1);
	    
	    while (lookID==id_step && code.equalsIgnoreCase( lookCode ) )
	    {
	        nr = rowMeta.getInteger(look, 2).intValue() + 1; // Find the maximum
	        offset++;
            if (idx+offset<stepAttributesBuffer.size())
            {
                look = (Object[])stepAttributesBuffer.get(idx+offset);
                
                lookID = rowMeta.getInteger(look, 0);
                lookCode = rowMeta.getString(look, 1);
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

	public synchronized long getJobID(String name, long id_directory) throws KettleException
	{
		return getIDWithValue("R_JOB", "ID_JOB", "NAME", name, "ID_DIRECTORY", id_directory);
	}

	public synchronized long getTransformationID(String name, long id_directory) throws KettleException
	{
		return getIDWithValue("R_TRANSFORMATION", "ID_TRANSFORMATION", "NAME", name, "ID_DIRECTORY", id_directory);
	}

	public synchronized long getNoteID(String note) throws KettleException
	{
		return getIDWithValue("R_NOTE", "ID_NOTE", "VALUE_STR", note);
	}

	public synchronized long getDatabaseID(String name) throws KettleException
	{
		return getIDWithValue("R_DATABASE", "ID_DATABASE", "NAME", name);
	}
    
    public synchronized long getPartitionSchemaID(String name) throws KettleException
    {
        return getIDWithValue("R_PARTITION_SCHEMA", "ID_PARTITION_SCHEMA", "NAME", name);
    }

    public synchronized long getSlaveID(String name) throws KettleException
    {
        return getIDWithValue("R_SLAVE", "ID_SLAVE", "NAME", name);
    }

    public synchronized long getClusterID(String name) throws KettleException
    {
        return getIDWithValue("R_CLUSTER", "ID_CLUSTER", "NAME", name);
    }

	public synchronized long getDatabaseTypeID(String code) throws KettleException
	{
		return getIDWithValue("R_DATABASE_TYPE", "ID_DATABASE_TYPE", "CODE", code);
	}

	public synchronized long getDatabaseConTypeID(String code) throws KettleException
	{
		return getIDWithValue("R_DATABASE_CONTYPE", "ID_DATABASE_CONTYPE", "CODE", code);
	}

	public synchronized long getStepTypeID(String code) throws KettleException
	{
		return getIDWithValue("R_STEP_TYPE", "ID_STEP_TYPE", "CODE", code);
	}

	public synchronized long getJobEntryID(String name, long id_job) throws KettleException
	{
		return getIDWithValue("R_JOBENTRY", "ID_JOBENTRY", "NAME", name, "ID_JOB", id_job);
	}

	public synchronized long getJobEntryTypeID(String code) throws KettleException
	{
		return getIDWithValue("R_JOBENTRY_TYPE", "ID_JOBENTRY_TYPE", "CODE", code);
	}

	public synchronized long getStepID(String name, long id_transformation) throws KettleException
	{
		return getIDWithValue("R_STEP", "ID_STEP", "NAME", name, "ID_TRANSFORMATION", id_transformation);
	}

	public synchronized long getUserID(String login) throws KettleException
	{
		return getIDWithValue("R_USER", "ID_USER", "LOGIN", login);
	}

	public synchronized long getProfileID(String profilename) throws KettleException
	{
		return getIDWithValue("R_PROFILE", "ID_PROFILE", "NAME", profilename);
	}

	public synchronized long getPermissionID(String code) throws KettleException
	{
		return getIDWithValue("R_PERMISSION", "ID_PERMISSION", "CODE", code);
	}

	public synchronized long getTransHopID(long id_transformation, long id_step_from, long id_step_to)
			throws KettleException
	{
		String lookupkey[] = new String[] { "ID_TRANSFORMATION", "ID_STEP_FROM", "ID_STEP_TO" };
		long key[] = new long[] { id_transformation, id_step_from, id_step_to };

		return getIDWithValue("R_TRANS_HOP", "ID_TRANS_HOP", lookupkey, key);
	}

	public synchronized long getJobHopID(long id_job, long id_jobentry_copy_from, long id_jobentry_copy_to)
			throws KettleException
	{
		String lookupkey[] = new String[] { "ID_JOB", "ID_JOBENTRY_COPY_FROM", "ID_JOBENTRY_COPY_TO" };
		long key[] = new long[] { id_job, id_jobentry_copy_from, id_jobentry_copy_to };

		return getIDWithValue("R_JOB_HOP", "ID_JOB_HOP", lookupkey, key);
	}

	public synchronized long getDependencyID(long id_transformation, long id_database, String tablename) throws KettleException
	{
		String lookupkey[] = new String[] { "ID_TRANSFORMATION", "ID_DATABASE" };
		long key[] = new long[] { id_transformation, id_database };

		return getIDWithValue("R_DEPENDENCY", "ID_DEPENDENCY", "TABLE_NAME", tablename, lookupkey, key);
	}

	public synchronized long getRootDirectoryID() throws KettleException
	{
		RowMetaAndData result = database.getOneRow("SELECT ID_DIRECTORY FROM R_DIRECTORY WHERE ID_DIRECTORY_PARENT = 0");
		if (result != null && result.isNumeric(0))
			return result.getInteger(0, -1);
		return -1;
	}

	public synchronized int getNrSubDirectories(long id_directory) throws KettleException
	{
		int retval = 0;

		String sql = "SELECT COUNT(*) FROM R_DIRECTORY WHERE ID_DIRECTORY_PARENT = " + id_directory;
		RowMetaAndData r = database.getOneRow(sql);
		if (r != null)
		{
			retval = (int) r.getInteger(0, 0);
		}

		return retval;
	}

	public synchronized long[] getSubDirectoryIDs(long id_directory) throws KettleException
	{
		return getIDs("SELECT ID_DIRECTORY FROM R_DIRECTORY WHERE ID_DIRECTORY_PARENT = " + id_directory+" ORDER BY DIRECTORY_NAME");
	}

	private synchronized long getIDWithValue(String tablename, String idfield, String lookupfield, String value) throws KettleException
	{
		RowMetaAndData par = new RowMetaAndData();
		par.addValue(new ValueMeta("value", ValueMetaInterface.TYPE_STRING), value);
		RowMetaAndData result = database.getOneRow("SELECT " + databaseMeta.quoteField(idfield) + " FROM " + databaseMeta.quoteField(tablename)+ " WHERE " + databaseMeta.quoteField(lookupfield) + " = ?", par.getRowMeta(), par.getData());

		if (result != null && result.getRowMeta() != null && result.getData() != null && result.isNumeric(0))
			return result.getInteger(0, 0);
		return -1;
	}

	private synchronized long getIDWithValue(String tablename, String idfield, String lookupfield, String value, String lookupkey, long key) throws KettleException
	{
		RowMetaAndData par = new RowMetaAndData();
        par.addValue(new ValueMeta("value", ValueMetaInterface.TYPE_STRING), value);
        par.addValue(new ValueMeta("key", ValueMetaInterface.TYPE_INTEGER), new Long(key));
		RowMetaAndData result = database.getOneRow("SELECT " + databaseMeta.quoteField(idfield) + " FROM " + databaseMeta.quoteField(tablename) + " WHERE " + databaseMeta.quoteField( lookupfield ) + " = ? AND "
									+ databaseMeta.quoteField(lookupkey) + " = ?", par.getRowMeta(), par.getData());

		if (result != null && result.getRowMeta() != null && result.getData() != null && result.isNumeric(0))
			return result.getInteger(0, 0);
		return -1;
	}

	private synchronized long getIDWithValue(String tablename, String idfield, String lookupkey[], long key[]) throws KettleException
	{
		RowMetaAndData par = new RowMetaAndData();
		String sql = "SELECT " + databaseMeta.quoteField(idfield) + " FROM " + databaseMeta.quoteField(tablename) + " ";

		for (int i = 0; i < lookupkey.length; i++)
		{
			if (i == 0)
				sql += "WHERE ";
			else
				sql += "AND   ";
			par.addValue(new ValueMeta(lookupkey[i], ValueMetaInterface.TYPE_INTEGER), new Long(key[i]));
			sql += databaseMeta.quoteField(lookupkey[i]) + " = ? ";
		}
		RowMetaAndData result = database.getOneRow(sql, par.getRowMeta(), par.getData());
		if (result != null && result.getRowMeta() != null && result.getData() != null && result.isNumeric(0))
			return result.getInteger(0, 0);
		return -1;
	}

	private synchronized long getIDWithValue(String tablename, String idfield, String lookupfield, String value, String lookupkey[], long key[]) throws KettleException
	{
		RowMetaAndData par = new RowMetaAndData();
        par.addValue(new ValueMeta(lookupfield, ValueMetaInterface.TYPE_STRING), value);
        
		String sql = "SELECT " + databaseMeta.quoteField(idfield) + " FROM " + databaseMeta.quoteField(tablename) + " WHERE " + databaseMeta.quoteField(lookupfield) + " = ? ";

		for (int i = 0; i < lookupkey.length; i++)
		{
			par.addValue( new ValueMeta(lookupkey[i], ValueMetaInterface.TYPE_STRING), new Long(key[i]) );
			sql += "AND " + databaseMeta.quoteField(lookupkey[i]) + " = ? ";
		}

		RowMetaAndData result = database.getOneRow(sql, par.getRowMeta(), par.getData());
		if (result != null && result.getRowMeta() != null && result.getData() != null && result.isNumeric(0))
			return result.getInteger(0, 0);
		return -1;
	}

	public synchronized String getDatabaseTypeCode(long id_database_type) throws KettleException
	{
		return getStringWithID("R_DATABASE_TYPE", "ID_DATABASE_TYPE", id_database_type, "CODE");
	}

	public synchronized String getDatabaseConTypeCode(long id_database_contype) throws KettleException
	{
		return getStringWithID("R_DATABASE_CONTYPE", "ID_DATABASE_CONTYPE", id_database_contype, "CODE");
	}

	public synchronized String getStepTypeCode(long id_database_type) throws KettleException
	{
		return getStringWithID("R_STEP_TYPE", "ID_STEP_TYPE", id_database_type, "CODE");
	}

	private synchronized String getStringWithID(String tablename, String keyfield, long id, String fieldname) throws KettleException
	{
		String sql = "SELECT " + databaseMeta.quoteField(fieldname) + " FROM " + databaseMeta.quoteField(tablename) + " WHERE " + databaseMeta.quoteField(keyfield) + " = ?";
		RowMetaAndData par = new RowMetaAndData();
		par.addValue(new ValueMeta(keyfield, ValueMetaInterface.TYPE_INTEGER), new Long(id));
		RowMetaAndData result = database.getOneRow(sql, par.getRowMeta(), par.getData());
		if (result != null)
		{
			return result.getString(0, null);
		}
		return null;
	}

	/////////////////////////////////////////////////////////////////////////////////////
	// DIRECTORIES
	/////////////////////////////////////////////////////////////////////////////////////

	public synchronized void moveTransformation(String transname, long id_directory_from, long id_directory_to) throws KettleException
	{
        String nameField = databaseMeta.quoteField("NAME");
		String sql = "UPDATE R_TRANSFORMATION SET ID_DIRECTORY = ? WHERE "+nameField+" = ? AND ID_DIRECTORY = ?";

		RowMetaAndData par = new RowMetaAndData();
		par.addValue(new ValueMeta("ID_DIRECTORY", ValueMetaInterface.TYPE_INTEGER), new Long(id_directory_to));
		par.addValue(new ValueMeta("NAME",  ValueMetaInterface.TYPE_STRING), transname);
		par.addValue(new ValueMeta("ID_DIRECTORY", ValueMetaInterface.TYPE_INTEGER), new Long(id_directory_from));

		database.execStatement(sql, par.getRowMeta(), par.getData());
	}

	public synchronized void moveJob(String jobname, long id_directory_from, long id_directory_to) throws KettleException
	{
        String nameField = databaseMeta.quoteField("NAME");
		String sql = "UPDATE R_JOB SET ID_DIRECTORY = ? WHERE "+nameField+" = ? AND ID_DIRECTORY = ?";

		RowMetaAndData par = new RowMetaAndData();
		par.addValue(new ValueMeta("ID_DIRECTORY", ValueMetaInterface.TYPE_INTEGER), new Long(id_directory_to));
		par.addValue(new ValueMeta("NAME",  ValueMetaInterface.TYPE_STRING), jobname);
		par.addValue(new ValueMeta("ID_DIRECTORY", ValueMetaInterface.TYPE_INTEGER), new Long(id_directory_from));

		database.execStatement(sql, par.getRowMeta(), par.getData());
	}

	/////////////////////////////////////////////////////////////////////////////////////
	// GET NEW IDS
	/////////////////////////////////////////////////////////////////////////////////////

	public synchronized long getNextTransformationID() throws KettleException
	{
		return getNextID("R_TRANSFORMATION", "ID_TRANSFORMATION");
	}

	public synchronized long getNextJobID() throws KettleException
	{
		return getNextID("R_JOB", "ID_JOB");
	}

	public synchronized long getNextNoteID() throws KettleException
	{
		return getNextID("R_NOTE", "ID_NOTE");
	}
    
    public synchronized long getNextLogID() throws KettleException
    {
        return getNextID("R_REPOSITORY_LOG", "ID_REPOSITORY_LOG");
    }

	public synchronized long getNextDatabaseID() throws KettleException
	{
		return getNextID("R_DATABASE", "ID_DATABASE");
	}

	public synchronized long getNextDatabaseTypeID() throws KettleException
	{
		return getNextID("R_DATABASE_TYPE", "ID_DATABASE_TYPE");
	}

	public synchronized long getNextDatabaseConnectionTypeID() throws KettleException
	{
		return getNextID("R_DATABASE_CONTYPE", "ID_DATABASE_CONTYPE");
	}

	public synchronized long getNextLoglevelID() throws KettleException
	{
		return getNextID("R_LOGLEVEL", "ID_LOGLEVEL");
	}

	public synchronized long getNextStepTypeID() throws KettleException
	{
		return getNextID("R_STEP_TYPE", "ID_STEP_TYPE");
	}

	public synchronized long getNextStepID() throws KettleException
	{
		return getNextID("R_STEP", "ID_STEP");
	}

	public synchronized long getNextJobEntryID() throws KettleException
	{
		return getNextID("R_JOBENTRY", "ID_JOBENTRY");
	}

	public synchronized long getNextJobEntryTypeID() throws KettleException
	{
		return getNextID("R_JOBENTRY_TYPE", "ID_JOBENTRY_TYPE");
	}

	public synchronized long getNextJobEntryCopyID() throws KettleException
	{
		return getNextID("R_JOBENTRY_COPY", "ID_JOBENTRY_COPY");
	}

	public synchronized long getNextStepAttributeID() throws KettleException
	{
		return getNextID("R_STEP_ATTRIBUTE", "ID_STEP_ATTRIBUTE");
	}

    public synchronized long getNextTransAttributeID() throws KettleException
    {
        return getNextID("R_TRANS_ATTRIBUTE", "ID_TRANS_ATTRIBUTE");
    }
    
    public synchronized long getNextDatabaseAttributeID() throws KettleException
    {
        return getNextID("R_DATABASE_ATTRIBUTE", "ID_DATABASE_ATTRIBUTE");
    }

	public synchronized long getNextTransHopID() throws KettleException
	{
		return getNextID("R_TRANS_HOP", "ID_TRANS_HOP");
	}

	public synchronized long getNextJobHopID() throws KettleException
	{
		return getNextID("R_JOB_HOP", "ID_JOB_HOP");
	}

	public synchronized long getNextDepencencyID() throws KettleException
	{
		return getNextID("R_DEPENDENCY", "ID_DEPENDENCY");
	}
    
    public synchronized long getNextPartitionSchemaID() throws KettleException
    {
        return getNextID("R_PARTITION_SCHEMA", "ID_PARTITION_SCHEMA");
    }

    public synchronized long getNextPartitionID() throws KettleException
    {
        return getNextID("R_PARTITION", "ID_PARTITION");
    }

    public synchronized long getNextTransformationPartitionSchemaID() throws KettleException
    {
        return getNextID("R_TRANS_PARTITION_SCHEMA", "ID_TRANS_PARTITION_SCHEMA");
    }
    
    public synchronized long getNextClusterID() throws KettleException
    {
        return getNextID("R_CLUSTER", "ID_CLUSTER");
    }

    public synchronized long getNextSlaveServerID() throws KettleException
    {
        return getNextID("R_SLAVE", "ID_SLAVE");
    }
    
    public synchronized long getNextClusterSlaveID() throws KettleException
    {
        return getNextID("R_CLUSTER_SLAVE", "ID_CLUSTER_SLAVE");
    }
    
    public synchronized long getNextTransformationSlaveID() throws KettleException
    {
        return getNextID("R_TRANS_SLAVE", "ID_TRANS_SLAVE");
    }
    
    public synchronized long getNextTransformationClusterID() throws KettleException
    {
        return getNextID("R_TRANS_CLUSTER", "ID_TRANS_CLUSTER");
    }
    
	public synchronized long getNextConditionID() throws KettleException
	{
		return getNextID("R_CONDITION", "ID_CONDITION");
	}

	public synchronized long getNextValueID() throws KettleException
	{
		return getNextID("R_VALUE", "ID_VALUE");
	}

	public synchronized long getNextUserID() throws KettleException
	{
		return getNextID("R_USER", "ID_USER");
	}

	public synchronized long getNextProfileID() throws KettleException
	{
		return getNextID("R_PROFILE", "ID_PROFILE");
	}

	public synchronized long getNextPermissionID() throws KettleException
	{
		return getNextID("R_PERMISSION", "ID_PERMISSION");
	}

	public synchronized long getNextJobEntryAttributeID() throws KettleException
	{
	    return getNextID("R_JOBENTRY_ATTRIBUTE", "ID_JOBENTRY_ATTRIBUTE");
	}
	
	public synchronized long getNextID(String tableName, String fieldName) throws KettleException
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

	public synchronized long getNextDirectoryID() throws KettleException
	{
		return getNextID("R_DIRECTORY", "ID_DIRECTORY");
	}

	private synchronized long getNextTableID(String tablename, String idfield) throws KettleException
	{
		long retval = -1;

		RowMetaAndData r = database.getOneRow("SELECT MAX(" + databaseMeta.quoteField(idfield) + ") FROM " + databaseMeta.quoteField(tablename));
		if (r != null)
		{
			Long id = r.getInteger(0);
			
			// log.logBasic(toString(), "result row for "+idfield+" is : "+r.toString()+", id = "+id.toString()+" int="+id.getInteger()+" num="+id.getNumber());
			if (id == null)
			{
				if (log.isDebug()) log.logDebug(toString(), "no max(" + idfield + ") found in table " + tablename);
				retval = 1;
			}
			else
			{
                if (log.isDebug()) log.logDebug(toString(), "max(" + idfield + ") found in table " + tablename + " --> " + idfield + " number: " + id);
				retval = id.longValue() + 1L;
				
				// log.logBasic(toString(), "Got next id for "+tablename+"."+idfield+" from the database: "+retval);
			}
		}
		return retval;
	}

	/////////////////////////////////////////////////////////////////////////////////////
	// INSERT VALUES
	/////////////////////////////////////////////////////////////////////////////////////

	public synchronized void insertTransformation(TransMeta transMeta) throws KettleException
    {
		RowMetaAndData table = new RowMetaAndData();

		table.addValue(new ValueMeta("ID_TRANSFORMATION", ValueMetaInterface.TYPE_INTEGER), new Long(transMeta.getId()));
		table.addValue(new ValueMeta("NAME", ValueMetaInterface.TYPE_STRING), transMeta.getName());
		table.addValue(new ValueMeta("DESCRIPTION", ValueMetaInterface.TYPE_STRING), transMeta.getDescription());
		table.addValue(new ValueMeta("EXTENDED_DESCRIPTION", ValueMetaInterface.TYPE_STRING), transMeta.getExtendedDescription());
		table.addValue(new ValueMeta("TRANS_VERSION", ValueMetaInterface.TYPE_STRING), transMeta.getTransversion());
		table.addValue(new ValueMeta("TRANS_STATUS", ValueMetaInterface.TYPE_INTEGER), new Long(transMeta.getTransstatus()  <0 ? -1L : transMeta.getTransstatus()));
		table.addValue(new ValueMeta("ID_STEP_READ", ValueMetaInterface.TYPE_INTEGER), new Long(transMeta.getReadStep()  ==null ? -1L : transMeta.getReadStep().getID()));
		table.addValue(new ValueMeta("ID_STEP_WRITE", ValueMetaInterface.TYPE_INTEGER), new Long(transMeta.getWriteStep() ==null ? -1L : transMeta.getWriteStep().getID()));
		table.addValue(new ValueMeta("ID_STEP_INPUT", ValueMetaInterface.TYPE_INTEGER), new Long(transMeta.getInputStep() ==null ? -1L : transMeta.getInputStep().getID()));
		table.addValue(new ValueMeta("ID_STEP_OUTPUT", ValueMetaInterface.TYPE_INTEGER), new Long(transMeta.getOutputStep()==null ? -1L : transMeta.getOutputStep().getID()));
		table.addValue(new ValueMeta("ID_STEP_UPDATE", ValueMetaInterface.TYPE_INTEGER), new Long(transMeta.getUpdateStep()==null ? -1L : transMeta.getUpdateStep().getID()));
		table.addValue(new ValueMeta("ID_DATABASE_LOG", ValueMetaInterface.TYPE_INTEGER), new Long(transMeta.getLogConnection()==null ? -1L : transMeta.getLogConnection().getID()));
		table.addValue(new ValueMeta("TABLE_NAME_LOG", ValueMetaInterface.TYPE_STRING), transMeta.getLogTable());
		table.addValue(new ValueMeta("USE_BATCHID", ValueMetaInterface.TYPE_BOOLEAN), new Boolean(transMeta.isBatchIdUsed()));
		table.addValue(new ValueMeta("USE_LOGFIELD", ValueMetaInterface.TYPE_BOOLEAN), new Boolean(transMeta.isLogfieldUsed()));
		table.addValue(new ValueMeta("ID_DATABASE_MAXDATE", ValueMetaInterface.TYPE_INTEGER), new Long(transMeta.getMaxDateConnection()==null ? -1L : transMeta.getMaxDateConnection().getID()));
		table.addValue(new ValueMeta("TABLE_NAME_MAXDATE", ValueMetaInterface.TYPE_STRING), transMeta.getMaxDateTable());
		table.addValue(new ValueMeta("FIELD_NAME_MAXDATE", ValueMetaInterface.TYPE_STRING), transMeta.getMaxDateField());
		table.addValue(new ValueMeta("OFFSET_MAXDATE", ValueMetaInterface.TYPE_NUMBER), new Double(transMeta.getMaxDateOffset()));
		table.addValue(new ValueMeta("DIFF_MAXDATE", ValueMetaInterface.TYPE_NUMBER), new Double(transMeta.getMaxDateDifference()));

		table.addValue(new ValueMeta("CREATED_USER", ValueMetaInterface.TYPE_STRING),        transMeta.getCreatedUser());
		table.addValue(new ValueMeta("CREATED_DATE", ValueMetaInterface.TYPE_DATE), transMeta.getCreatedDate());
		
		table.addValue(new ValueMeta("MODIFIED_USER", ValueMetaInterface.TYPE_STRING), transMeta.getModifiedUser());
		table.addValue(new ValueMeta("MODIFIED_DATE", ValueMetaInterface.TYPE_DATE), transMeta.getModifiedDate());
		table.addValue(new ValueMeta("SIZE_ROWSET", ValueMetaInterface.TYPE_INTEGER), new Long(transMeta.getSizeRowset()));
		table.addValue(new ValueMeta("ID_DIRECTORY", ValueMetaInterface.TYPE_INTEGER), new Long(transMeta.getDirectory().getID()));

		database.prepareInsert(table.getRowMeta(), "R_TRANSFORMATION");
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
			String modified_user, Date modified_date, boolean useBatchId, boolean batchIdPassed, boolean logfieldUsed, 
            String sharedObjectsFile, String description, String extended_description, String version, int status,
			String created_user, Date created_date) throws KettleException
	{
		RowMetaAndData table = new RowMetaAndData();

		table.addValue(new ValueMeta("ID_JOB", ValueMetaInterface.TYPE_INTEGER), new Long(id_job));
		table.addValue(new ValueMeta("ID_DIRECTORY", ValueMetaInterface.TYPE_INTEGER), new Long(id_directory));
		table.addValue(new ValueMeta("NAME", ValueMetaInterface.TYPE_STRING), name);
		table.addValue(new ValueMeta("DESCRIPTION", ValueMetaInterface.TYPE_STRING), description);
		table.addValue(new ValueMeta("EXTENDED_DESCRIPTION", ValueMetaInterface.TYPE_STRING), extended_description);
		table.addValue(new ValueMeta("JOB_VERSION", ValueMetaInterface.TYPE_STRING), version);
		table.addValue(new ValueMeta("JOB_STATUS", ValueMetaInterface.TYPE_INTEGER), new Long(status  <0 ? -1L : status));

		table.addValue(new ValueMeta("ID_DATABASE_LOG", ValueMetaInterface.TYPE_INTEGER), new Long(id_database_log));
		table.addValue(new ValueMeta("TABLE_NAME_LOG", ValueMetaInterface.TYPE_STRING), table_name_log);

		table.addValue(new ValueMeta("CREATED_USER", ValueMetaInterface.TYPE_STRING), created_user);
		table.addValue(new ValueMeta("CREATED_DATE", ValueMetaInterface.TYPE_DATE), created_date);
		table.addValue(new ValueMeta("MODIFIED_USER", ValueMetaInterface.TYPE_STRING), modified_user);
		table.addValue(new ValueMeta("MODIFIED_DATE", ValueMetaInterface.TYPE_DATE), modified_date);
        table.addValue(new ValueMeta("USE_BATCH_ID", ValueMetaInterface.TYPE_BOOLEAN), new Boolean(useBatchId));
        table.addValue(new ValueMeta("PASS_BATCH_ID", ValueMetaInterface.TYPE_BOOLEAN), new Boolean(batchIdPassed));
        table.addValue(new ValueMeta("USE_LOGFIELD", ValueMetaInterface.TYPE_BOOLEAN), new Boolean(logfieldUsed));
        table.addValue(new ValueMeta("SHARED_FILE", ValueMetaInterface.TYPE_STRING), sharedObjectsFile);

		database.prepareInsert(table.getRowMeta(), "R_JOB");
		database.setValuesInsert(table);
		database.insertRow();
        if (log.isDebug()) log.logDebug(toString(), "Inserted new record into table R_JOB with data : " + table);
		database.closeInsert();
	}

	public synchronized long insertNote(String note, long gui_location_x, long gui_location_y, long gui_location_width, long gui_location_height) throws KettleException
	{
		long id = getNextNoteID();

		RowMetaAndData table = new RowMetaAndData();

		table.addValue(new ValueMeta("ID_NOTE", ValueMetaInterface.TYPE_INTEGER), new Long(id));
		table.addValue(new ValueMeta("VALUE_STR", ValueMetaInterface.TYPE_STRING), note);
		table.addValue(new ValueMeta("GUI_LOCATION_X", ValueMetaInterface.TYPE_INTEGER), new Long(gui_location_x));
		table.addValue(new ValueMeta("GUI_LOCATION_Y", ValueMetaInterface.TYPE_INTEGER), new Long(gui_location_y));
		table.addValue(new ValueMeta("GUI_LOCATION_WIDTH", ValueMetaInterface.TYPE_INTEGER), new Long(gui_location_width));
		table.addValue(new ValueMeta("GUI_LOCATION_HEIGHT", ValueMetaInterface.TYPE_INTEGER), new Long(gui_location_height));

		database.prepareInsert(table.getRowMeta(), "R_NOTE");
		database.setValuesInsert(table);
		database.insertRow();
		database.closeInsert();

		return id;
	}
    
    public synchronized long insertLogEntry(String description) throws KettleException
    {
        long id = getNextLogID();

        RowMetaAndData table = new RowMetaAndData();
        table.addValue(new ValueMeta("ID_REPOSITORY_LOG", ValueMetaInterface.TYPE_INTEGER), new Long(id));
        table.addValue(new ValueMeta("REP_VERSION", ValueMetaInterface.TYPE_STRING), getVersion());
        table.addValue(new ValueMeta("LOG_DATE", ValueMetaInterface.TYPE_DATE), new Date());
        table.addValue(new ValueMeta("LOG_USER", ValueMetaInterface.TYPE_STRING), userinfo!=null?userinfo.getLogin():"admin");
        table.addValue(new ValueMeta("OPERATION_DESC", ValueMetaInterface.TYPE_STRING), description);

        database.prepareInsert(table.getRowMeta(), "R_REPOSITORY_LOG");
        database.setValuesInsert(table);
        database.insertRow();
        database.closeInsert();

        return id;
    }

	public synchronized void insertTransNote(long id_transformation, long id_note) throws KettleException
	{
		RowMetaAndData table = new RowMetaAndData();

		table.addValue(new ValueMeta("ID_TRANSFORMATION", ValueMetaInterface.TYPE_INTEGER), new Long(id_transformation));
		table.addValue(new ValueMeta("ID_NOTE", ValueMetaInterface.TYPE_INTEGER), new Long(id_note));

		database.prepareInsert(table.getRowMeta(), "R_TRANS_NOTE");
		database.setValuesInsert(table);
		database.insertRow();
		database.closeInsert();
	}

	public synchronized void insertJobNote(long id_job, long id_note) throws KettleException
	{
		RowMetaAndData table = new RowMetaAndData();

		table.addValue(new ValueMeta("ID_JOB", ValueMetaInterface.TYPE_INTEGER), new Long(id_job));
		table.addValue(new ValueMeta("ID_NOTE", ValueMetaInterface.TYPE_INTEGER), new Long(id_note));

		database.prepareInsert(table.getRowMeta(), "R_JOB_NOTE");
		database.setValuesInsert(table);
		database.insertRow();
		database.closeInsert();
	}

	public synchronized long insertDatabase(String name, String type, String access, String host, String dbname, String port,
			String user, String pass, String servername, String data_tablespace, String index_tablespace)
			throws KettleException
	{

		long id = getNextDatabaseID();

		long id_database_type = getDatabaseTypeID(type);
		if (id_database_type < 0) // New support database type: add it!
		{
			id_database_type = getNextDatabaseTypeID();

			String tablename = "R_DATABASE_TYPE";
			RowMetaInterface tableMeta = new RowMeta();
            
            tableMeta.addValueMeta(new ValueMeta("ID_DATABASE_TYPE", ValueMetaInterface.TYPE_INTEGER, 5, 0));
            tableMeta.addValueMeta(new ValueMeta("CODE", ValueMetaInterface.TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
            tableMeta.addValueMeta(new ValueMeta("DESCRIPTION", ValueMetaInterface.TYPE_STRING, REP_STRING_LENGTH, 0));

			database.prepareInsert(tableMeta, tablename);

			Object[] tableData = new Object[3];
            int tableIndex = 0;
            
			tableData[tableIndex++] = new Long(id_database_type);
            tableData[tableIndex++] = type;
            tableData[tableIndex++] = type;

			database.setValuesInsert(tableMeta, tableData);
			database.insertRow();
			database.closeInsert();
		}

		long id_database_contype = getDatabaseConTypeID(access);

		RowMetaAndData table = new RowMetaAndData();
		table.addValue(new ValueMeta("ID_DATABASE", ValueMetaInterface.TYPE_INTEGER), new Long(id));
		table.addValue(new ValueMeta("NAME", ValueMetaInterface.TYPE_STRING), name);
		table.addValue(new ValueMeta("ID_DATABASE_TYPE", ValueMetaInterface.TYPE_INTEGER), new Long(id_database_type));
		table.addValue(new ValueMeta("ID_DATABASE_CONTYPE", ValueMetaInterface.TYPE_INTEGER), new Long(id_database_contype));
		table.addValue(new ValueMeta("HOST_NAME", ValueMetaInterface.TYPE_STRING), host);
		table.addValue(new ValueMeta("DATABASE_NAME", ValueMetaInterface.TYPE_STRING), dbname);
		table.addValue(new ValueMeta("PORT", ValueMetaInterface.TYPE_INTEGER), new Long(Const.toInt(port, -1)));
		table.addValue(new ValueMeta("USERNAME", ValueMetaInterface.TYPE_STRING), user);
		table.addValue(new ValueMeta("PASSWORD", ValueMetaInterface.TYPE_STRING), Encr.encryptPasswordIfNotUsingVariables(pass));
		table.addValue(new ValueMeta("SERVERNAME", ValueMetaInterface.TYPE_STRING), servername);
		table.addValue(new ValueMeta("DATA_TBS", ValueMetaInterface.TYPE_STRING), data_tablespace);
		table.addValue(new ValueMeta("INDEX_TBS", ValueMetaInterface.TYPE_STRING), index_tablespace);

		database.prepareInsert(table.getRowMeta(), "R_DATABASE");
		database.setValuesInsert(table);
		database.insertRow();
		database.closeInsert();

		return id;
	}

	public synchronized long insertStep(long id_transformation, String name, String description, String steptype,
			boolean distribute, long copies, long gui_location_x, long gui_location_y, boolean gui_draw)
			throws KettleException
	{
		long id = getNextStepID();

		long id_step_type = getStepTypeID(steptype);

		RowMetaAndData table = new RowMetaAndData();

		table.addValue(new ValueMeta("ID_STEP", ValueMetaInterface.TYPE_INTEGER), new Long(id));
		table.addValue(new ValueMeta("ID_TRANSFORMATION", ValueMetaInterface.TYPE_INTEGER), new Long(id_transformation));
		table.addValue(new ValueMeta("NAME", ValueMetaInterface.TYPE_STRING), name);
		table.addValue(new ValueMeta("DESCRIPTION", ValueMetaInterface.TYPE_STRING), description);
		table.addValue(new ValueMeta("ID_STEP_TYPE", ValueMetaInterface.TYPE_INTEGER), new Long(id_step_type));
		table.addValue(new ValueMeta("DISTRIBUTE", ValueMetaInterface.TYPE_BOOLEAN), new Boolean(distribute));
		table.addValue(new ValueMeta("COPIES", ValueMetaInterface.TYPE_INTEGER), new Long(copies));
		table.addValue(new ValueMeta("GUI_LOCATION_X", ValueMetaInterface.TYPE_INTEGER), new Long(gui_location_x));
		table.addValue(new ValueMeta("GUI_LOCATION_Y", ValueMetaInterface.TYPE_INTEGER), new Long(gui_location_y));
		table.addValue(new ValueMeta("GUI_DRAW", ValueMetaInterface.TYPE_BOOLEAN), new Boolean(gui_draw));

		database.prepareInsert(table.getRowMeta(), "R_STEP");
		database.setValuesInsert(table);
		database.insertRow();
		database.closeInsert();

		return id;
	}

	public synchronized long insertStepAttribute(long id_transformation, long id_step, long nr, String code, double value_num,
			String value_str) throws KettleException
	{
		long id = getNextStepAttributeID();

		RowMetaAndData table = new RowMetaAndData();

		table.addValue(new ValueMeta("ID_STEP_ATTRIBUTE", ValueMetaInterface.TYPE_INTEGER), new Long(id));
		table.addValue(new ValueMeta("ID_TRANSFORMATION", ValueMetaInterface.TYPE_INTEGER), new Long(id_transformation));
		table.addValue(new ValueMeta("ID_STEP", ValueMetaInterface.TYPE_INTEGER), new Long(id_step));
		table.addValue(new ValueMeta("NR", ValueMetaInterface.TYPE_INTEGER), new Long(nr));
		table.addValue(new ValueMeta("CODE", ValueMetaInterface.TYPE_STRING), code);
		table.addValue(new ValueMeta("VALUE_NUM", ValueMetaInterface.TYPE_NUMBER), new Double(value_num));
		table.addValue(new ValueMeta("VALUE_STR", ValueMetaInterface.TYPE_STRING), value_str);

		/* If we have prepared the insert, we don't do it again.
		 * We asume that all the step insert statements come one after the other.
		 */
		
		if (psStepAttributesInsert == null)
		{
		    String sql = database.getInsertStatement("R_STEP_ATTRIBUTE", table.getRowMeta());
		    psStepAttributesInsert = database.prepareSQL(sql);
		}
		database.setValues(table, psStepAttributesInsert);
		database.insertRow(psStepAttributesInsert, true);
		
		/*
		database.prepareInsert(table.getRowMeta(), "R_STEP_ATTRIBUTE");
		database.setValuesInsert(table);
		database.insertRow();
		database.closeInsert();
		*/
		
        if (log.isDebug()) log.logDebug(toString(), "saved attribute ["+code+"]");
		
		return id;
	}
    
    public synchronized long insertTransAttribute(long id_transformation, long nr, String code, long value_num, String value_str) throws KettleException
    {
        long id = getNextTransAttributeID();

        RowMetaAndData table = new RowMetaAndData();

        table.addValue(new ValueMeta("ID_TRANS_ATTRIBUTE", ValueMetaInterface.TYPE_INTEGER), new Long(id));
        table.addValue(new ValueMeta("ID_TRANSFORMATION", ValueMetaInterface.TYPE_INTEGER), new Long(id_transformation));
        table.addValue(new ValueMeta("NR", ValueMetaInterface.TYPE_INTEGER), new Long(nr));
        table.addValue(new ValueMeta("CODE", ValueMetaInterface.TYPE_STRING), code);
        table.addValue(new ValueMeta("VALUE_NUM", ValueMetaInterface.TYPE_INTEGER), new Long(value_num));
        table.addValue(new ValueMeta("VALUE_STR", ValueMetaInterface.TYPE_STRING), value_str);

        /* If we have prepared the insert, we don't do it again.
         * We asume that all the step insert statements come one after the other.
         */
        
        if (psTransAttributesInsert == null)
        {
            String sql = database.getInsertStatement("R_TRANS_ATTRIBUTE", table.getRowMeta());
            psTransAttributesInsert = database.prepareSQL(sql);
        }
        database.setValues(table, psTransAttributesInsert);
        database.insertRow(psTransAttributesInsert, true);
        
        if (log.isDebug()) log.logDebug(toString(), "saved transformation attribute ["+code+"]");
        
        return id;
    }


	public synchronized void insertStepDatabase(long id_transformation, long id_step, long id_database)
			throws KettleException
	{
		// First check if the relationship is already there.
		// There is no need to store it twice!
		RowMetaAndData check = getStepDatabase(id_step);
		if (check == null)
		{
			RowMetaAndData table = new RowMetaAndData();

			table.addValue(new ValueMeta("ID_TRANSFORMATION", ValueMetaInterface.TYPE_INTEGER), new Long(id_transformation));
			table.addValue(new ValueMeta("ID_STEP", ValueMetaInterface.TYPE_INTEGER), new Long(id_step));
			table.addValue(new ValueMeta("ID_DATABASE", ValueMetaInterface.TYPE_INTEGER), new Long(id_database));

			database.insertRow("R_STEP_DATABASE", table.getRowMeta(), table.getData());
		}
	}
	
    public synchronized long insertDatabaseAttribute(long id_database, String code, String value_str) throws KettleException
    {
        long id = getNextDatabaseAttributeID();

        RowMetaAndData table = new RowMetaAndData();

        table.addValue(new ValueMeta("ID_DATABASE_ATTRIBUTE", ValueMetaInterface.TYPE_INTEGER), new Long(id));
        table.addValue(new ValueMeta("ID_DATABASE", ValueMetaInterface.TYPE_INTEGER), new Long(id_database));
        table.addValue(new ValueMeta("CODE", ValueMetaInterface.TYPE_STRING), code);
        table.addValue(new ValueMeta("VALUE_STR", ValueMetaInterface.TYPE_STRING), value_str);

        /* If we have prepared the insert, we don't do it again.
         * We asume that all the step insert statements come one after the other.
         */
        database.prepareInsert(table.getRowMeta(), "R_DATABASE_ATTRIBUTE");
        database.setValuesInsert(table);
        database.insertRow();
        database.closeInsert();
        
        if (log.isDebug()) log.logDebug(toString(), "saved database attribute ["+code+"]");
        
        return id;
    }

	
	public synchronized long insertJobEntryAttribute(long id_job, long id_jobentry, long nr, String code, double value_num,
			String value_str) throws KettleException
	{
		long id = getNextJobEntryAttributeID();

		RowMetaAndData table = new RowMetaAndData();

		table.addValue(new ValueMeta("ID_JOBENTRY_ATTRIBUTE", ValueMetaInterface.TYPE_INTEGER), new Long(id));
		table.addValue(new ValueMeta("ID_JOB", ValueMetaInterface.TYPE_INTEGER), new Long(id_job));
		table.addValue(new ValueMeta("ID_JOBENTRY", ValueMetaInterface.TYPE_INTEGER), new Long(id_jobentry));
		table.addValue(new ValueMeta("NR", ValueMetaInterface.TYPE_INTEGER), new Long(nr));
		table.addValue(new ValueMeta("CODE", ValueMetaInterface.TYPE_STRING), code);
		table.addValue(new ValueMeta("VALUE_NUM", ValueMetaInterface.TYPE_NUMBER), new Double(value_num));
		table.addValue(new ValueMeta("VALUE_STR", ValueMetaInterface.TYPE_STRING), value_str);

		database.prepareInsert(table.getRowMeta(), "R_JOBENTRY_ATTRIBUTE");
		database.setValuesInsert(table);
		database.insertRow();
		database.closeInsert();

		return id;
	}

	public synchronized long insertTransHop(long id_transformation, long id_step_from, long id_step_to, boolean enabled)
			throws KettleException
	{
		long id = getNextTransHopID();

		RowMetaAndData table = new RowMetaAndData();

		table.addValue(new ValueMeta("ID_TRANS_HOP", ValueMetaInterface.TYPE_INTEGER), new Long(id));
		table.addValue(new ValueMeta("ID_TRANSFORMATION", ValueMetaInterface.TYPE_INTEGER), new Long(id_transformation));
		table.addValue(new ValueMeta("ID_STEP_FROM", ValueMetaInterface.TYPE_INTEGER), new Long(id_step_from));
		table.addValue(new ValueMeta("ID_STEP_TO", ValueMetaInterface.TYPE_INTEGER), new Long(id_step_to));
		table.addValue(new ValueMeta("ENABLED", ValueMetaInterface.TYPE_BOOLEAN), new Boolean(enabled));

		database.prepareInsert(table.getRowMeta(), "R_TRANS_HOP");
		database.setValuesInsert(table);
		database.insertRow();
		database.closeInsert();

		return id;
	}

	public synchronized long insertJobHop(long id_job, long id_jobentry_copy_from, long id_jobentry_copy_to, boolean enabled,
			boolean evaluation, boolean unconditional) throws KettleException
	{
		long id = getNextJobHopID();

		RowMetaAndData table = new RowMetaAndData();

		table.addValue(new ValueMeta("ID_JOB_HOP", ValueMetaInterface.TYPE_INTEGER), new Long(id));
		table.addValue(new ValueMeta("ID_JOB", ValueMetaInterface.TYPE_INTEGER), new Long(id_job));
		table.addValue(new ValueMeta("ID_JOBENTRY_COPY_FROM", ValueMetaInterface.TYPE_INTEGER), new Long(id_jobentry_copy_from));
		table.addValue(new ValueMeta("ID_JOBENTRY_COPY_TO", ValueMetaInterface.TYPE_INTEGER), new Long(id_jobentry_copy_to));
		table.addValue(new ValueMeta("ENABLED", ValueMetaInterface.TYPE_BOOLEAN), new Boolean(enabled));
		table.addValue(new ValueMeta("EVALUATION", ValueMetaInterface.TYPE_BOOLEAN), new Boolean(evaluation));
		table.addValue(new ValueMeta("UNCONDITIONAL", ValueMetaInterface.TYPE_BOOLEAN), new Boolean(unconditional));

		database.prepareInsert(table.getRowMeta(), "R_JOB_HOP");
		database.setValuesInsert(table);
		database.insertRow();
		database.closeInsert();

		return id;
	}

	public synchronized long insertDependency(long id_transformation, long id_database, String tablename, String fieldname)
			throws KettleException
	{
		long id = getNextDepencencyID();

		RowMetaAndData table = new RowMetaAndData();

		table.addValue(new ValueMeta("ID_DEPENDENCY", ValueMetaInterface.TYPE_INTEGER), new Long(id));
		table.addValue(new ValueMeta("ID_TRANSFORMATION", ValueMetaInterface.TYPE_INTEGER), new Long(id_transformation));
		table.addValue(new ValueMeta("ID_DATABASE", ValueMetaInterface.TYPE_INTEGER), new Long(id_database));
		table.addValue(new ValueMeta("TABLE_NAME", ValueMetaInterface.TYPE_STRING), tablename);
		table.addValue(new ValueMeta("FIELD_NAME", ValueMetaInterface.TYPE_STRING), fieldname);

		database.prepareInsert(table.getRowMeta(), "R_DEPENDENCY");
		database.setValuesInsert(table);
		database.insertRow();
		database.closeInsert();

		return id;
	}

    public synchronized long insertPartitionSchema(PartitionSchema partitionSchema) throws KettleException
    {
        long id = getNextPartitionSchemaID();

        RowMetaAndData table = new RowMetaAndData();

        table.addValue(new ValueMeta("ID_PARTITION_SCHEMA", ValueMetaInterface.TYPE_INTEGER), new Long(id));
        table.addValue(new ValueMeta("NAME", ValueMetaInterface.TYPE_STRING), partitionSchema.getName());

        database.prepareInsert(table.getRowMeta(), "R_PARTITION_SCHEMA");
        database.setValuesInsert(table);
        database.insertRow();
        database.closeInsert();

        return id;
    }
    
    public synchronized void updatePartitionSchema(PartitionSchema partitionSchema) throws KettleException
    {
        RowMetaAndData table = new RowMetaAndData();
        table.addValue(new ValueMeta("NAME", ValueMetaInterface.TYPE_STRING), partitionSchema.getName());
        updateTableRow("R_PARTITION_SCHEMA", "ID_PARTITION_SCHEMA", table, partitionSchema.getId());
    }

    public synchronized long insertPartition(long id_partition_schema, String partition_id) throws KettleException
    {
        long id = getNextPartitionID();

        RowMetaAndData table = new RowMetaAndData();

        table.addValue(new ValueMeta("ID_PARTITION", ValueMetaInterface.TYPE_INTEGER), new Long(id));
        table.addValue(new ValueMeta("ID_PARTITION_SCHEMA", ValueMetaInterface.TYPE_INTEGER), new Long(id_partition_schema));
        table.addValue(new ValueMeta("PARTITION_ID", ValueMetaInterface.TYPE_INTEGER), new Long(partition_id));

        database.prepareInsert(table.getRowMeta(), "R_PARTITION");
        database.setValuesInsert(table);
        database.insertRow();
        database.closeInsert();

        return id;
    }

    public synchronized long insertTransformationPartitionSchema(long id_transformation, long id_partition_schema) throws KettleException
    {
        long id = getNextTransformationPartitionSchemaID();

        RowMetaAndData table = new RowMetaAndData();

        table.addValue(new ValueMeta("ID_TRANS_PARTITION_SCHEMA", ValueMetaInterface.TYPE_INTEGER), new Long(id));
        table.addValue(new ValueMeta("ID_TRANSFORMATION", ValueMetaInterface.TYPE_INTEGER), new Long(id_transformation));
        table.addValue(new ValueMeta("ID_PARTITION_SCHEMA", ValueMetaInterface.TYPE_INTEGER), new Long(id_partition_schema));

        database.prepareInsert(table.getRowMeta(), "R_TRANS_PARTITION_SCHEMA");
        database.setValuesInsert(table);
        database.insertRow();
        database.closeInsert();

        return id;
    }
    
    public synchronized long insertCluster(ClusterSchema clusterSchema) throws KettleException
    {
        long id = getNextClusterID();

        RowMetaAndData table = new RowMetaAndData();

        table.addValue(new ValueMeta("ID_CLUSTER", ValueMetaInterface.TYPE_INTEGER), new Long(id));
        table.addValue(new ValueMeta("NAME", ValueMetaInterface.TYPE_STRING), clusterSchema.getName());
        table.addValue(new ValueMeta("BASE_PORT", ValueMetaInterface.TYPE_STRING), clusterSchema.getBasePort());
        table.addValue(new ValueMeta("SOCKETS_BUFFER_SIZE", ValueMetaInterface.TYPE_STRING), clusterSchema.getSocketsBufferSize());
        table.addValue(new ValueMeta("SOCKETS_FLUSH_INTERVAL", ValueMetaInterface.TYPE_STRING), clusterSchema.getSocketsFlushInterval());
        table.addValue(new ValueMeta("SOCKETS_COMPRESSED", ValueMetaInterface.TYPE_BOOLEAN), new Boolean(clusterSchema.isSocketsCompressed()));

        database.prepareInsert(table.getRowMeta(), "R_CLUSTER");
        database.setValuesInsert(table);
        database.insertRow();
        database.closeInsert();

        return id;
    }

    public synchronized long insertSlave(SlaveServer slaveServer) throws KettleException
    {
        long id = getNextSlaveServerID();

        RowMetaAndData table = new RowMetaAndData();

        table.addValue(new ValueMeta("ID_SLAVE", ValueMetaInterface.TYPE_INTEGER), new Long(id));
        table.addValue(new ValueMeta("NAME", ValueMetaInterface.TYPE_STRING), slaveServer.getName());
        table.addValue(new ValueMeta("HOST_NAME", ValueMetaInterface.TYPE_STRING), slaveServer.getHostname());
        table.addValue(new ValueMeta("PORT", ValueMetaInterface.TYPE_STRING), slaveServer.getPort());
        table.addValue(new ValueMeta("USERNAME", ValueMetaInterface.TYPE_STRING), slaveServer.getUsername());
        table.addValue(new ValueMeta("PASSWORD", ValueMetaInterface.TYPE_STRING), slaveServer.getPassword());
        table.addValue(new ValueMeta("PROXY_HOST_NAME", ValueMetaInterface.TYPE_STRING), slaveServer.getProxyHostname());
        table.addValue(new ValueMeta("PROXY_PORT", ValueMetaInterface.TYPE_STRING), slaveServer.getProxyPort());
        table.addValue(new ValueMeta("NON_PROXY_HOSTS", ValueMetaInterface.TYPE_STRING), slaveServer.getNonProxyHosts());
        table.addValue(new ValueMeta("MASTER", ValueMetaInterface.TYPE_BOOLEAN), new Boolean(slaveServer.isMaster()));

        database.prepareInsert(table.getRowMeta(), "R_SLAVE");
        database.setValuesInsert(table);
        database.insertRow();
        database.closeInsert();

        return id;
    }
    
    public synchronized void updateSlave(SlaveServer slaveServer) throws KettleException
    {
        RowMetaAndData table = new RowMetaAndData();
        table.addValue(new ValueMeta("NAME", ValueMetaInterface.TYPE_STRING), slaveServer.getName());
        table.addValue(new ValueMeta("HOST_NAME", ValueMetaInterface.TYPE_STRING), slaveServer.getHostname());
        table.addValue(new ValueMeta("PORT", ValueMetaInterface.TYPE_STRING), slaveServer.getPort());
        table.addValue(new ValueMeta("USERNAME", ValueMetaInterface.TYPE_STRING), slaveServer.getUsername());
        table.addValue(new ValueMeta("PASSWORD", ValueMetaInterface.TYPE_STRING), slaveServer.getPassword());
        table.addValue(new ValueMeta("PROXY_HOST_NAME", ValueMetaInterface.TYPE_STRING), slaveServer.getProxyHostname());
        table.addValue(new ValueMeta("PROXY_PORT", ValueMetaInterface.TYPE_STRING), slaveServer.getProxyPort());
        table.addValue(new ValueMeta("NON_PROXY_HOSTS", ValueMetaInterface.TYPE_STRING), slaveServer.getNonProxyHosts());
        table.addValue(new ValueMeta("MASTER", ValueMetaInterface.TYPE_BOOLEAN), new Boolean(slaveServer.isMaster()));

        updateTableRow("R_SLAVE", "ID_SLAVE", table, slaveServer.getId());
    }
    
    public synchronized long insertClusterSlave(ClusterSchema clusterSchema, SlaveServer slaveServer) throws KettleException
    {
        long id = getNextClusterSlaveID();

        RowMetaAndData table = new RowMetaAndData();

        table.addValue(new ValueMeta("ID_CLUSTER_SLAVE", ValueMetaInterface.TYPE_INTEGER), new Long(id));
        table.addValue(new ValueMeta("ID_CLUSTER", ValueMetaInterface.TYPE_INTEGER), new Long(clusterSchema.getId()));
        table.addValue(new ValueMeta("ID_SLAVE", ValueMetaInterface.TYPE_INTEGER), new Long(slaveServer.getId()));

        database.prepareInsert(table.getRowMeta(), "R_CLUSTER_SLAVE");
        database.setValuesInsert(table);
        database.insertRow();
        database.closeInsert();

        return id;
    }

    public synchronized long insertTransformationCluster(long id_transformation, long id_cluster) throws KettleException
    {
        long id = getNextTransformationClusterID();

        RowMetaAndData table = new RowMetaAndData();

        table.addValue(new ValueMeta("ID_TRANS_CLUSTER", ValueMetaInterface.TYPE_INTEGER), new Long(id));
        table.addValue(new ValueMeta("ID_TRANSFORMATION", ValueMetaInterface.TYPE_INTEGER), new Long(id_transformation));
        table.addValue(new ValueMeta("ID_CLUSTER", ValueMetaInterface.TYPE_INTEGER), new Long(id_cluster));

        database.prepareInsert(table.getRowMeta(), "R_TRANS_CLUSTER");
        database.setValuesInsert(table);
        database.insertRow();
        database.closeInsert();

        return id;
    }

    public synchronized long insertTransformationSlave(long id_transformation, long id_slave) throws KettleException
    {
        long id = getNextTransformationSlaveID();

        RowMetaAndData table = new RowMetaAndData();

        table.addValue(new ValueMeta("ID_TRANS_SLAVE", ValueMetaInterface.TYPE_INTEGER), new Long(id));
        table.addValue(new ValueMeta("ID_TRANSFORMATION", ValueMetaInterface.TYPE_INTEGER), new Long(id_transformation));
        table.addValue(new ValueMeta("ID_SLAVE", ValueMetaInterface.TYPE_INTEGER), new Long(id_slave));

        database.prepareInsert(table.getRowMeta(), "R_TRANS_SLAVE");
        database.setValuesInsert(table);
        database.insertRow();
        database.closeInsert();

        return id;
    }
    
    
	public synchronized long insertCondition(long id_condition_parent, Condition condition) throws KettleException
	{
		long id = getNextConditionID();

		String tablename = "R_CONDITION";
		RowMetaAndData table = new RowMetaAndData();
		table.addValue(new ValueMeta("ID_CONDITION", ValueMetaInterface.TYPE_INTEGER), new Long(id));
		table.addValue(new ValueMeta("ID_CONDITION_PARENT", ValueMetaInterface.TYPE_INTEGER), new Long(id_condition_parent));
		table.addValue(new ValueMeta("NEGATED", ValueMetaInterface.TYPE_BOOLEAN), new Boolean(condition.isNegated()));
		table.addValue(new ValueMeta("OPERATOR", ValueMetaInterface.TYPE_STRING), condition.getOperatorDesc());
		table.addValue(new ValueMeta("LEFT_NAME", ValueMetaInterface.TYPE_STRING), condition.getLeftValuename());
		table.addValue(new ValueMeta("CONDITION_FUNCTION", ValueMetaInterface.TYPE_STRING), condition.getFunctionDesc());
		table.addValue(new ValueMeta("RIGHT_NAME", ValueMetaInterface.TYPE_STRING), condition.getRightValuename());

		long id_value = -1L;
		ValueMetaAndData v = condition.getRightExact();

		if (v != null)
		{
			id_value = insertValue(v.getValueMeta().getName(), v.getValueMeta().getTypeDesc(), v.getValueMeta().getString(v.getValueData()), v.getValueMeta().isNull(v.getValueData()), condition.getRightExactID());
			condition.setRightExactID(id_value);
		}
		table.addValue(new ValueMeta("ID_VALUE_RIGHT", ValueMetaInterface.TYPE_INTEGER), new Long(id_value));

		database.prepareInsert(table.getRowMeta(), tablename);

		database.setValuesInsert(table);
		database.insertRow();
		database.closeInsert();

		return id;
	}

	public synchronized void insertTransStepCondition(long id_transformation, long id_step, long id_condition)
			throws KettleException
	{
		String tablename = "R_TRANS_STEP_CONDITION";
		RowMetaAndData table = new RowMetaAndData();
		table.addValue(new ValueMeta("ID_TRANSFORMATION", ValueMetaInterface.TYPE_INTEGER), new Long(id_transformation));
		table.addValue(new ValueMeta("ID_STEP", ValueMetaInterface.TYPE_INTEGER), new Long(id_step));
		table.addValue(new ValueMeta("ID_CONDITION", ValueMetaInterface.TYPE_INTEGER), new Long(id_condition));

		database.prepareInsert(table.getRowMeta(), tablename);
		database.setValuesInsert(table);
		database.insertRow();
		database.closeInsert();
	}

	public synchronized long insertDirectory(long id_directory_parent, RepositoryDirectory dir) throws KettleException
	{
		long id = getNextDirectoryID();

		String tablename = "R_DIRECTORY";
		RowMetaAndData table = new RowMetaAndData();
		table.addValue(new ValueMeta("ID_DIRECTORY", ValueMetaInterface.TYPE_INTEGER), new Long(id));
		table.addValue(new ValueMeta("ID_DIRECTORY_PARENT", ValueMetaInterface.TYPE_INTEGER), new Long(id_directory_parent));
		table.addValue(new ValueMeta("DIRECTORY_NAME", ValueMetaInterface.TYPE_STRING), dir.getDirectoryName());

		database.prepareInsert(table.getRowMeta(), tablename);

		database.setValuesInsert(table);
		database.insertRow();
		database.closeInsert();

		return id;
	}

	public synchronized void deleteDirectory(long id_directory) throws KettleException
	{
		String sql = "DELETE FROM R_DIRECTORY WHERE ID_DIRECTORY = " + id_directory;
		database.execStatement(sql);
	}

	public synchronized void renameDirectory(long id_directory, String name) throws KettleException
	{
		RowMetaAndData r = new RowMetaAndData();
		r.addValue(new ValueMeta("DIRECTORY_NAME", ValueMetaInterface.TYPE_STRING), name);

		String sql = "UPDATE R_DIRECTORY SET DIRECTORY_NAME = ? WHERE ID_DIRECTORY = " + id_directory;

		log.logBasic(toString(), "sql = [" + sql + "]");
		log.logBasic(toString(), "row = [" + r + "]");

		database.execStatement(sql, r.getRowMeta(), r.getData());
	}

	public synchronized long lookupValue(String name, String type, String value_str, boolean isnull) throws KettleException
	{
		String tablename = "R_VALUE";
		RowMetaAndData table = new RowMetaAndData();
		table.addValue(new ValueMeta("NAME", ValueMetaInterface.TYPE_STRING), name);
		table.addValue(new ValueMeta("VALUE_TYPE", ValueMetaInterface.TYPE_STRING), type);
		table.addValue(new ValueMeta("VALUE_STR", ValueMetaInterface.TYPE_STRING), value_str);
		table.addValue(new ValueMeta("IS_NULL", ValueMetaInterface.TYPE_BOOLEAN), new Boolean(isnull));

		String sql = "SELECT " + database.getDatabaseMeta().quoteField("ID_VALUE") + " FROM " + tablename + " ";
		sql += "WHERE " + database.getDatabaseMeta().quoteField("NAME") + "       = ? ";
		sql += "AND   " + database.getDatabaseMeta().quoteField("VALUE_TYPE") + " = ? ";
		sql += "AND   " + database.getDatabaseMeta().quoteField("VALUE_STR") + "  = ? ";
		sql += "AND   " + database.getDatabaseMeta().quoteField("IS_NULL") + "    = ? ";

		RowMetaAndData result = database.getOneRow(sql, table.getRowMeta(), table.getData());
		if (result != null && result.isNumeric(0))
			return result.getInteger(0, 0L);
		else
			return -1;
	}

	public synchronized long insertValue(String name, String type, String value_str, boolean isnull, long id_value_prev) throws KettleException
	{
		long id_value = lookupValue(name, type, value_str, isnull);
		// if it didn't exist yet: insert it!!

		if (id_value < 0)
		{
			id_value = getNextValueID();

			// Let's see if the same value is not yet available?
			String tablename = "R_VALUE";
			RowMetaAndData table = new RowMetaAndData();
			table.addValue(new ValueMeta("ID_VALUE", ValueMetaInterface.TYPE_INTEGER), new Long(id_value));
			table.addValue(new ValueMeta("NAME", ValueMetaInterface.TYPE_STRING), name);
			table.addValue(new ValueMeta("VALUE_TYPE", ValueMetaInterface.TYPE_STRING), type);
			table.addValue(new ValueMeta("VALUE_STR", ValueMetaInterface.TYPE_STRING), value_str);
			table.addValue(new ValueMeta("IS_NULL", ValueMetaInterface.TYPE_BOOLEAN), new Boolean(isnull));

			database.prepareInsert(table.getRowMeta(), tablename);
			database.setValuesInsert(table);
			database.insertRow();
			database.closeInsert();
		}

		return id_value;
	}

	public synchronized long insertJobEntry(long id_job, String name, String description, String jobentrytype)
			throws KettleException
	{
		long id = getNextJobEntryID();

		long id_jobentry_type = getJobEntryTypeID(jobentrytype);

		log.logDebug(toString(), "ID_JobEntry_type = " + id_jobentry_type + " for type = [" + jobentrytype + "]");

		RowMetaAndData table = new RowMetaAndData();

		table.addValue(new ValueMeta("ID_JOBENTRY", ValueMetaInterface.TYPE_INTEGER), new Long(id));
		table.addValue(new ValueMeta("ID_JOB", ValueMetaInterface.TYPE_INTEGER), new Long(id_job));
		table.addValue(new ValueMeta("ID_JOBENTRY_TYPE", ValueMetaInterface.TYPE_INTEGER), new Long(id_jobentry_type));
		table.addValue(new ValueMeta("NAME", ValueMetaInterface.TYPE_STRING), name);
		table.addValue(new ValueMeta("DESCRIPTION", ValueMetaInterface.TYPE_STRING), description);

		database.prepareInsert(table.getRowMeta(), "R_JOBENTRY");
		database.setValuesInsert(table);
		database.insertRow();
		database.closeInsert();

		return id;
	}

	public synchronized long insertJobEntryCopy(long id_job, long id_jobentry, long id_jobentry_type, int nr, long gui_location_x,
			long gui_location_y, boolean gui_draw, boolean parallel) throws KettleException
	{
		long id = getNextJobEntryCopyID();

		RowMetaAndData table = new RowMetaAndData();

		table.addValue(new ValueMeta("ID_JOBENTRY_COPY", ValueMetaInterface.TYPE_INTEGER), new Long(id));
		table.addValue(new ValueMeta("ID_JOBENTRY", ValueMetaInterface.TYPE_INTEGER), new Long(id_jobentry));
		table.addValue(new ValueMeta("ID_JOB", ValueMetaInterface.TYPE_INTEGER), new Long(id_job));
		table.addValue(new ValueMeta("ID_JOBENTRY_TYPE", ValueMetaInterface.TYPE_INTEGER), new Long(id_jobentry_type));
		table.addValue(new ValueMeta("NR", ValueMetaInterface.TYPE_INTEGER), new Long(nr));
		table.addValue(new ValueMeta("GUI_LOCATION_X", ValueMetaInterface.TYPE_INTEGER), new Long(gui_location_x));
		table.addValue(new ValueMeta("GUI_LOCATION_Y", ValueMetaInterface.TYPE_INTEGER), new Long(gui_location_y));
		table.addValue(new ValueMeta("GUI_DRAW", ValueMetaInterface.TYPE_BOOLEAN), new Boolean(gui_draw));
		table.addValue(new ValueMeta("PARALLEL", ValueMetaInterface.TYPE_BOOLEAN), new Boolean(parallel));

		database.prepareInsert(table.getRowMeta(), "R_JOBENTRY_COPY");
		database.setValuesInsert(table);
		database.insertRow();
		database.closeInsert();

		return id;
	}

	public synchronized void insertTableRow(String tablename, RowMetaAndData values) throws KettleException
	{
		database.prepareInsert(values.getRowMeta(), tablename);
		database.setValuesInsert(values);
		database.insertRow();
		database.closeInsert();
	}

	/////////////////////////////////////////////////////////////////////////////////////
	// UPDATE VALUES
	/////////////////////////////////////////////////////////////////////////////////////

	public synchronized void updateDatabase(long id_database, String name, String type, String access, String host, String dbname,
			String port, String user, String pass, String servername, String data_tablespace, String index_tablespace)
			throws KettleException
	{
		long id_database_type = getDatabaseTypeID(type);
		long id_database_contype = getDatabaseConTypeID(access);

		RowMetaAndData table = new RowMetaAndData();
		table.addValue(new ValueMeta("NAME", ValueMetaInterface.TYPE_STRING), name);
		table.addValue(new ValueMeta("ID_DATABASE_TYPE", ValueMetaInterface.TYPE_INTEGER), new Long(id_database_type));
		table.addValue(new ValueMeta("ID_DATABASE_CONTYPE", ValueMetaInterface.TYPE_INTEGER), new Long(id_database_contype));
		table.addValue(new ValueMeta("HOST_NAME", ValueMetaInterface.TYPE_STRING), host);
		table.addValue(new ValueMeta("DATABASE_NAME", ValueMetaInterface.TYPE_STRING), dbname);
		table.addValue(new ValueMeta("PORT", ValueMetaInterface.TYPE_INTEGER), new Long(Const.toInt(port, -1)));
		table.addValue(new ValueMeta("USERNAME", ValueMetaInterface.TYPE_STRING), user);
		table.addValue(new ValueMeta("PASSWORD", ValueMetaInterface.TYPE_STRING), Encr.encryptPasswordIfNotUsingVariables(pass));
		table.addValue(new ValueMeta("SERVERNAME", ValueMetaInterface.TYPE_STRING), servername);
		table.addValue(new ValueMeta("DATA_TBS", ValueMetaInterface.TYPE_STRING), data_tablespace);
		table.addValue(new ValueMeta("INDEX_TBS", ValueMetaInterface.TYPE_STRING), index_tablespace);

		updateTableRow("R_DATABASE", "ID_DATABASE", table, id_database);
	}

	public synchronized void updateTableRow(String tablename, String idfield, RowMetaAndData values, long id) throws KettleException
	{
		String sets[] = new String[values.size()];
		for (int i = 0; i < values.size(); i++)
			sets[i] = values.getValueMeta(i).getName();
		String codes[] = new String[] { idfield };
		String condition[] = new String[] { "=" };

		database.prepareUpdate(tablename, codes, condition, sets);

		values.addValue(new ValueMeta(idfield, ValueMetaInterface.TYPE_INTEGER), new Long(id));

		database.setValuesUpdate(values.getRowMeta(), values.getData());
		database.updateRow();
		database.closeUpdate();
	}

	public synchronized void updateTableRow(String tablename, String idfield, RowMetaAndData values) throws KettleException
	{
		long id = values.getInteger(idfield, 0L);
		values.removeValue(idfield);
		String sets[] = new String[values.size()];
		for (int i = 0; i < values.size(); i++)
			sets[i] = values.getValueMeta(i).getName();
		String codes[] = new String[] { idfield };
		String condition[] = new String[] { "=" };

		database.prepareUpdate(tablename, codes, condition, sets);

		values.addValue(new ValueMeta(idfield, ValueMetaInterface.TYPE_INTEGER), new Long(id));

		database.setValuesUpdate(values.getRowMeta(), values.getData());
		database.updateRow();
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	// READ DATA FROM REPOSITORY
	//////////////////////////////////////////////////////////////////////////////////////////

	public synchronized int getNrJobs() throws KettleException
	{
		int retval = 0;

		String sql = "SELECT COUNT(*) FROM R_JOB";
		RowMetaAndData r = database.getOneRow(sql);
		if (r != null)
		{
			retval = (int) r.getInteger(0, 0L);
		}

		return retval;
	}

	public synchronized int getNrTransformations(long id_directory) throws KettleException
	{
		int retval = 0;

		String sql = "SELECT COUNT(*) FROM R_TRANSFORMATION WHERE ID_DIRECTORY = " + id_directory;
		RowMetaAndData r = database.getOneRow(sql);
		if (r != null)
		{
			retval = (int) r.getInteger(0, 0L);
		}

		return retval;
	}

	public synchronized int getNrJobs(long id_directory) throws KettleException
	{
		int retval = 0;

		String sql = "SELECT COUNT(*) FROM R_JOB WHERE ID_DIRECTORY = " + id_directory;
		RowMetaAndData r = database.getOneRow(sql);
		if (r != null)
		{
			retval = (int) r.getInteger(0, 0L);
		}

		return retval;
	}

    public synchronized int getNrDirectories(long id_directory) throws KettleException
	{
		int retval = 0;

		String sql = "SELECT COUNT(*) FROM R_DIRECTORY WHERE ID_DIRECTORY_PARENT = " + id_directory;
		RowMetaAndData r = database.getOneRow(sql);
		if (r != null)
		{
			retval = (int) r.getInteger(0, 0L);
		}

		return retval;
	}

	public synchronized int getNrConditions(long id_transforamtion) throws KettleException
	{
		int retval = 0;

		String sql = "SELECT COUNT(*) FROM R_TRANS_STEP_CONDITION WHERE ID_TRANSFORMATION = " + id_transforamtion;
		RowMetaAndData r = database.getOneRow(sql);
		if (r != null)
		{
			retval = (int) r.getInteger(0, 0L);
		}

		return retval;
	}

	public synchronized int getNrDatabases(long id_transforamtion) throws KettleException
	{
		int retval = 0;

		String sql = "SELECT COUNT(*) FROM R_STEP_DATABASE WHERE ID_TRANSFORMATION = " + id_transforamtion;
		RowMetaAndData r = database.getOneRow(sql);
		if (r != null)
		{
			retval = (int) r.getInteger(0, 0L);
		}

		return retval;
	}

	public synchronized int getNrSubConditions(long id_condition) throws KettleException
	{
		int retval = 0;

		String sql = "SELECT COUNT(*) FROM R_CONDITION WHERE ID_CONDITION_PARENT = " + id_condition;
		RowMetaAndData r = database.getOneRow(sql);
		if (r != null)
		{
			retval = (int) r.getInteger(0, 0L);
		}

		return retval;
	}

	public synchronized int getNrTransNotes(long id_transformation) throws KettleException
	{
		int retval = 0;

		String sql = "SELECT COUNT(*) FROM R_TRANS_NOTE WHERE ID_TRANSFORMATION = " + id_transformation;
		RowMetaAndData r = database.getOneRow(sql);
		if (r != null)
		{
			retval = (int) r.getInteger(0, 0L);
		}

		return retval;
	}

	public synchronized int getNrJobNotes(long id_job) throws KettleException
	{
		int retval = 0;

		String sql = "SELECT COUNT(*) FROM R_JOB_NOTE WHERE ID_JOB = " + id_job;
		RowMetaAndData r = database.getOneRow(sql);
		if (r != null)
		{
			retval = (int) r.getInteger(0, 0L);
		}

		return retval;
	}

	public synchronized int getNrDatabases() throws KettleException
	{
		int retval = 0;

		String sql = "SELECT COUNT(*) FROM R_DATABASE";
		RowMetaAndData r = database.getOneRow(sql);
		if (r != null)
		{
			retval = (int) r.getInteger(0, 0L);
		}

		return retval;
	}

    public synchronized int getNrDatabaseAttributes(long id_database) throws KettleException
    {
        int retval = 0;

        String sql = "SELECT COUNT(*) FROM R_DATABASE_ATTRIBUTE WHERE ID_DATABASE = "+id_database;
        RowMetaAndData r = database.getOneRow(sql);
        if (r != null)
        {
            retval = (int) r.getInteger(0, 0L);
        }

        return retval;
    }

	public synchronized int getNrSteps(long id_transformation) throws KettleException
	{
		int retval = 0;

		String sql = "SELECT COUNT(*) FROM R_STEP WHERE ID_TRANSFORMATION = " + id_transformation;
		RowMetaAndData r = database.getOneRow(sql);
		if (r != null)
		{
			retval = (int) r.getInteger(0, 0L);
		}

		return retval;
	}

	public synchronized int getNrStepDatabases(long id_database) throws KettleException
	{
		int retval = 0;

		String sql = "SELECT COUNT(*) FROM R_STEP_DATABASE WHERE ID_DATABASE = " + id_database;
		RowMetaAndData r = database.getOneRow(sql);
		if (r != null)
		{
			retval = (int) r.getInteger(0, 0L);
		}

		return retval;
	}

	public synchronized int getNrStepAttributes(long id_step) throws KettleException
	{
		int retval = 0;

		String sql = "SELECT COUNT(*) FROM R_STEP_ATTRIBUTE WHERE ID_STEP = " + id_step;
		RowMetaAndData r = database.getOneRow(sql);
		if (r != null)
		{
			retval = (int) r.getInteger(0, 0L);
		}

		return retval;
	}

	public synchronized int getNrTransHops(long id_transformation) throws KettleException
	{
		int retval = 0;

		String sql = "SELECT COUNT(*) FROM R_TRANS_HOP WHERE ID_TRANSFORMATION = " + id_transformation;
		RowMetaAndData r = database.getOneRow(sql);
		if (r != null)
		{
			retval = (int) r.getInteger(0, 0L);
		}

		return retval;
	}

	public synchronized int getNrJobHops(long id_job) throws KettleException
	{
		int retval = 0;

		String sql = "SELECT COUNT(*) FROM R_JOB_HOP WHERE ID_JOB = " + id_job;
		RowMetaAndData r = database.getOneRow(sql);
		if (r != null)
		{
			retval = (int) r.getInteger(0, 0L);
		}

		return retval;
	}

	public synchronized int getNrTransDependencies(long id_transformation) throws KettleException
	{
		int retval = 0;

		String sql = "SELECT COUNT(*) FROM R_DEPENDENCY WHERE ID_TRANSFORMATION = " + id_transformation;
		RowMetaAndData r = database.getOneRow(sql);
		if (r != null)
		{
			retval = (int) r.getInteger(0, 0L);
		}

		return retval;
	}

	public synchronized int getNrJobEntries(long id_job) throws KettleException
	{
		int retval = 0;

		String sql = "SELECT COUNT(*) FROM R_JOBENTRY WHERE ID_JOB = " + id_job;
		RowMetaAndData r = database.getOneRow(sql);
		if (r != null)
		{
			retval = (int) r.getInteger(0, 0L);
		}

		return retval;
	}

	public synchronized int getNrJobEntryCopies(long id_job, long id_jobentry) throws KettleException
	{
		int retval = 0;

		String sql = "SELECT COUNT(*) FROM R_JOBENTRY_COPY WHERE ID_JOB = " + id_job + " AND ID_JOBENTRY = "
						+ id_jobentry;
		RowMetaAndData r = database.getOneRow(sql);
		if (r != null)
		{
			retval = (int) r.getInteger(0, 0L);
		}

		return retval;
	}

	public synchronized int getNrJobEntryCopies(long id_job) throws KettleException
	{
		int retval = 0;

		String sql = "SELECT COUNT(*) FROM R_JOBENTRY_COPY WHERE ID_JOB = " + id_job;
		RowMetaAndData r = database.getOneRow(sql);
		if (r != null)
		{
			retval = (int) r.getInteger(0, 0L);
		}

		return retval;
	}

	public synchronized int getNrUsers() throws KettleException
	{
		int retval = 0;

		String sql = "SELECT COUNT(*) FROM R_USER";
		RowMetaAndData r = database.getOneRow(sql);
		if (r != null)
		{
			retval = (int) r.getInteger(0, 0L);
		}

		return retval;
	}

	public synchronized int getNrPermissions(long id_profile) throws KettleException
	{
		int retval = 0;

		String sql = "SELECT COUNT(*) FROM R_PROFILE_PERMISSION WHERE ID_PROFILE = " + id_profile;
		RowMetaAndData r = database.getOneRow(sql);
		if (r != null)
		{
			retval = (int) r.getInteger(0, 0L);
		}

		return retval;
	}

	public synchronized int getNrProfiles() throws KettleException
	{
		int retval = 0;

		String sql = "SELECT COUNT(*) FROM R_PROFILE";
		RowMetaAndData r = database.getOneRow(sql);
		if (r != null)
		{
			retval = (int) r.getInteger(0, 0L);
		}

		return retval;
	}

	public synchronized String[] getTransformationNames(long id_directory) throws KettleException
	{
		String nameField = databaseMeta.quoteField("NAME");
		return getStrings("SELECT "+nameField+" FROM R_TRANSFORMATION WHERE ID_DIRECTORY = " + id_directory + " ORDER BY "+nameField);
	}
    
    public List<RepositoryObject> getJobObjects(long id_directory) throws KettleException
    {
        return getRepositoryObjects("R_JOB", RepositoryObject.STRING_OBJECT_TYPE_JOB, id_directory);
    }

    public List<RepositoryObject> getTransformationObjects(long id_directory) throws KettleException
    {
        return getRepositoryObjects("R_TRANSFORMATION", RepositoryObject.STRING_OBJECT_TYPE_TRANSFORMATION, id_directory);
    }

    /**
     * @param id_directory
     * @return A list of RepositoryObjects
     * 
     * @throws KettleException
     */
    private synchronized List<RepositoryObject> getRepositoryObjects(String tableName, String objectType, long id_directory) throws KettleException
    {
        String nameField = databaseMeta.quoteField("NAME");
        
        String sql = "SELECT "+nameField+", MODIFIED_USER, MODIFIED_DATE, DESCRIPTION " +
                "FROM "+tableName+" " +
                "WHERE ID_DIRECTORY = " + id_directory + " "
                ;

        List<RepositoryObject> repositoryObjects = new ArrayList<RepositoryObject>();
        
        ResultSet rs = database.openQuery(sql);
        if (rs != null)
        {
        	try
        	{
                Object[] r = database.getRow(rs);
                while (r != null)
                {
                    RowMetaInterface rowMeta = database.getReturnRowMeta();
                    
                    repositoryObjects.add(new RepositoryObject( rowMeta.getString(r, 0), rowMeta.getString(r, 1), rowMeta.getDate(r, 2), objectType, rowMeta.getString(r, 3)));
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
    

	public synchronized String[] getJobNames(long id_directory) throws KettleException
	{
		String nameField = databaseMeta.quoteField("NAME");
        return getStrings("SELECT "+nameField+" FROM R_JOB WHERE ID_DIRECTORY = " + id_directory + " ORDER BY "+nameField);
	}

	public synchronized String[] getDirectoryNames(long id_directory) throws KettleException
	{
        return getStrings("SELECT DIRECTORY_NAME FROM R_DIRECTORY WHERE ID_DIRECTORY_PARENT = " + id_directory + " ORDER BY DIRECTORY_NAME");
	}

	public synchronized String[] getJobNames() throws KettleException
	{
        String nameField = databaseMeta.quoteField("NAME");
        return getStrings("SELECT "+nameField+" FROM R_JOB ORDER BY "+nameField);
	}

	public long[] getSubConditionIDs(long id_condition) throws KettleException
	{
        return getIDs("SELECT ID_CONDITION FROM R_CONDITION WHERE ID_CONDITION_PARENT = " + id_condition);
	}

	public long[] getTransNoteIDs(long id_transformation) throws KettleException
	{
        return getIDs("SELECT ID_NOTE FROM R_TRANS_NOTE WHERE ID_TRANSFORMATION = " + id_transformation);
	}

	public long[] getConditionIDs(long id_transformation) throws KettleException
	{
        return getIDs("SELECT ID_CONDITION FROM R_TRANS_STEP_CONDITION WHERE ID_TRANSFORMATION = " + id_transformation);
	}

	public long[] getDatabaseIDs(long id_transformation) throws KettleException
	{
        return getIDs("SELECT ID_DATABASE FROM R_STEP_DATABASE WHERE ID_TRANSFORMATION = " + id_transformation);
	}

	public long[] getJobNoteIDs(long id_job) throws KettleException
	{
        return getIDs("SELECT ID_NOTE FROM R_JOB_NOTE WHERE ID_JOB = " + id_job);
	}

	public long[] getDatabaseIDs() throws KettleException
	{
        String nameField = databaseMeta.quoteField("NAME");
        return getIDs("SELECT ID_DATABASE FROM R_DATABASE ORDER BY "+nameField);
	}
    
    public long[] getDatabaseAttributeIDs(long id_database) throws KettleException
    {
        return getIDs("SELECT ID_DATABASE_ATTRIBUTE FROM R_DATABASE_ATTRIBUTE WHERE ID_DATABASE = "+id_database);
    }
    
    public long[] getPartitionSchemaIDs() throws KettleException
    {
        String nameField = databaseMeta.quoteField("NAME");
        return getIDs("SELECT ID_PARTITION_SCHEMA FROM R_PARTITION_SCHEMA ORDER BY "+nameField);
    }
    
    public long[] getPartitionIDs(long id_partition_schema) throws KettleException
    {
        return getIDs("SELECT ID_PARTITION FROM R_PARTITION WHERE ID_PARTITION_SCHEMA = " + id_partition_schema);
    }

    public long[] getTransformationPartitionSchemaIDs(long id_transformation) throws KettleException
    {
        return getIDs("SELECT ID_TRANS_PARTITION_SCHEMA FROM R_TRANS_PARTITION_SCHEMA WHERE ID_TRANSFORMATION = "+id_transformation);
    }
    
    public long[] getTransformationClusterSchemaIDs(long id_transformation) throws KettleException
    {
        return getIDs("SELECT ID_TRANS_CLUSTER FROM R_TRANS_CLUSTER WHERE ID_TRANSFORMATION = " + id_transformation);
    }
    
    public long[] getClusterIDs() throws KettleException
    {
        String nameField = databaseMeta.quoteField("NAME");
        return getIDs("SELECT ID_CLUSTER FROM R_CLUSTER ORDER BY "+nameField); 
    }

    public long[] getSlaveIDs() throws KettleException
    {
        return getIDs("SELECT ID_SLAVE FROM R_SLAVE");
    }

    public long[] getSlaveIDs(long id_cluster_schema) throws KettleException
    {
        return getIDs("SELECT ID_SLAVE FROM R_CLUSTER_SLAVE WHERE ID_CLUSTER = " + id_cluster_schema);
    }
    
    private long[] getIDs(String sql) throws KettleException
    {
        List<Long> ids = new ArrayList<Long>();
        
        ResultSet rs = database.openQuery(sql);
        try 
        {
            Object[] r = database.getRow(rs);
            while (r != null)
            {
                RowMetaInterface rowMeta = database.getReturnRowMeta();
                Long id = rowMeta.getInteger(r, 0);
                if (id==null) id=new Long(0);
                
                ids.add(id);
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
    
    private String[] getStrings(String sql) throws KettleException
    {
        List<String> ids = new ArrayList<String>();
        
        ResultSet rs = database.openQuery(sql);
        try 
        {
            Object[] r = database.getRow(rs);
            while (r != null)
            {
                RowMetaInterface rowMeta = database.getReturnRowMeta();
                ids.add( rowMeta.getString(r, 0) );
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


	public synchronized String[] getDatabaseNames() throws KettleException
	{
		String nameField = databaseMeta.quoteField("NAME");
		return getStrings("SELECT "+nameField+" FROM R_DATABASE ORDER BY "+nameField);
	}
    
    public synchronized String[] getPartitionSchemaNames() throws KettleException
    {
        String nameField = databaseMeta.quoteField("NAME");
        return getStrings("SELECT "+nameField+" FROM R_PARTITION_SCHEMA ORDER BY "+nameField);
    }
    
    public synchronized String[] getSlaveNames() throws KettleException
    {
        String nameField = databaseMeta.quoteField("NAME");
        return getStrings("SELECT "+nameField+" FROM R_SLAVE ORDER BY "+nameField);
    }
    
    public synchronized String[] getClusterNames() throws KettleException
    {
        String nameField = databaseMeta.quoteField("NAME");
        return getStrings("SELECT "+nameField+" FROM R_CLUSTER ORDER BY "+nameField);
    }

	public long[] getStepIDs(long id_transformation) throws KettleException
	{
		return getIDs("SELECT ID_STEP FROM R_STEP WHERE ID_TRANSFORMATION = " + id_transformation);
	}

	public synchronized String[] getTransformationsUsingDatabase(long id_database) throws KettleException
	{
		String sql = "SELECT DISTINCT ID_TRANSFORMATION FROM R_STEP_DATABASE WHERE ID_DATABASE = " + id_database;
        return getTransformationsWithIDList( database.getRows(sql, 100), database.getReturnRowMeta() );
	}
    
    public synchronized String[] getClustersUsingSlave(long id_slave) throws KettleException
    {
        String sql = "SELECT DISTINCT ID_CLUSTER FROM R_CLUSTER_SLAVE WHERE ID_SLAVE = " + id_slave;

        List<Object[]> list = database.getRows(sql, 100);
        RowMetaInterface rowMeta = database.getReturnRowMeta();
        List<String> clusterList = new ArrayList<String>();

        for (int i=0;i<list.size();i++)
        {
            long id_cluster_schema = rowMeta.getInteger(list.get(i), "ID_CLUSTER", -1L); 
            if (id_cluster_schema > 0)
            {
                RowMetaAndData transRow =  getClusterSchema(id_cluster_schema);
                if (transRow!=null)
                {
                    String clusterName = transRow.getString("NAME", "<name not found>");
                    if (clusterName!=null) clusterList.add(clusterName);
                }
            }
        }

        return (String[]) clusterList.toArray(new String[clusterList.size()]);
    }

    public synchronized String[] getTransformationsUsingSlave(long id_slave) throws KettleException
    {
        String sql = "SELECT DISTINCT ID_TRANSFORMATION FROM R_TRANS_SLAVE WHERE ID_SLAVE = " + id_slave;
        return getTransformationsWithIDList( database.getRows(sql, 100), database.getReturnRowMeta() );
    }
    
    public synchronized String[] getTransformationsUsingPartitionSchema(long id_partition_schema) throws KettleException
    {
        String sql = "SELECT DISTINCT ID_TRANSFORMATION FROM R_TRANS_PARTITION_SCHEMA WHERE ID_PARTITION_SCHEMA = " + id_partition_schema;
        return getTransformationsWithIDList( database.getRows(sql, 100), database.getReturnRowMeta() );
    }
    
    public synchronized String[] getTransformationsUsingCluster(long id_cluster) throws KettleException
    {
        String sql = "SELECT DISTINCT ID_TRANSFORMATION FROM R_TRANS_CLUSTER WHERE ID_CLUSTER = " + id_cluster;
        return getTransformationsWithIDList( database.getRows(sql, 100), database.getReturnRowMeta() );
    }

	private String[] getTransformationsWithIDList(List<Object[]> list, RowMetaInterface rowMeta) throws KettleException
    {
        String[] transList = new String[list.size()];
        for (int i=0;i<list.size();i++)
        {
            long id_transformation = rowMeta.getInteger( list.get(i), "ID_TRANSFORMATION", -1L); 
            if (id_transformation > 0)
            {
                RowMetaAndData transRow =  getTransformation(id_transformation);
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

    public long[] getTransHopIDs(long id_transformation) throws KettleException
	{
		return getIDs("SELECT ID_TRANS_HOP FROM R_TRANS_HOP WHERE ID_TRANSFORMATION = " + id_transformation);
	}

	public long[] getJobHopIDs(long id_job) throws KettleException
	{
		return getIDs("SELECT ID_JOB_HOP FROM R_JOB_HOP WHERE ID_JOB = " + id_job);
	}

	public long[] getTransDependencyIDs(long id_transformation) throws KettleException
	{
		return getIDs("SELECT ID_DEPENDENCY FROM R_DEPENDENCY WHERE ID_TRANSFORMATION = " + id_transformation);
	}

	public long[] getUserIDs() throws KettleException
	{
		return getIDs("SELECT ID_USER FROM R_USER");
	}

	public synchronized String[] getUserLogins() throws KettleException
	{
		String loginField = databaseMeta.quoteField("LOGIN");
		return getStrings("SELECT "+loginField+" FROM R_USER ORDER BY "+loginField);
	}

	public long[] getPermissionIDs(long id_profile) throws KettleException
	{
		return getIDs("SELECT ID_PERMISSION FROM R_PROFILE_PERMISSION WHERE ID_PROFILE = " + id_profile);
	}

	public long[] getJobEntryIDs(long id_job) throws KettleException
	{
		return getIDs("SELECT ID_JOBENTRY FROM R_JOBENTRY WHERE ID_JOB = " + id_job);
	}

	public long[] getJobEntryCopyIDs(long id_job) throws KettleException
	{
		return getIDs("SELECT ID_JOBENTRY_COPY FROM R_JOBENTRY_COPY WHERE ID_JOB = " + id_job);
	}

	public long[] getJobEntryCopyIDs(long id_job, long id_jobentry) throws KettleException
	{
		return getIDs("SELECT ID_JOBENTRY_COPY FROM R_JOBENTRY_COPY WHERE ID_JOB = " + id_job + " AND ID_JOBENTRY = " + id_jobentry);
	}

	public synchronized String[] getProfiles() throws KettleException
	{
		String nameField = databaseMeta.quoteField("NAME");
		return getStrings("SELECT "+nameField+" FROM R_PROFILE ORDER BY "+nameField);
	}

	public RowMetaAndData getNote(long id_note) throws KettleException
	{
		return getOneRow("R_NOTE", "ID_NOTE", id_note);
	}

	public RowMetaAndData getDatabase(long id_database) throws KettleException
	{
		return getOneRow("R_DATABASE", "ID_DATABASE", id_database);
	}

    public RowMetaAndData getDatabaseAttribute(long id_database_attribute) throws KettleException
    {
        return getOneRow("R_DATABASE_ATTRIBUTE", "ID_DATABASE_ATTRIBUTE", id_database_attribute);
    }

	public RowMetaAndData getCondition(long id_condition) throws KettleException
	{
		return getOneRow("R_CONDITION", "ID_CONDITION", id_condition);
	}

	public RowMetaAndData getValue(long id_value) throws KettleException
	{
		return getOneRow("R_VALUE", "ID_VALUE", id_value);
	}

	public RowMetaAndData getStep(long id_step) throws KettleException
	{
		return getOneRow("R_STEP", "ID_STEP", id_step);
	}

	public RowMetaAndData getStepType(long id_step_type) throws KettleException
	{
		return getOneRow("R_STEP_TYPE", "ID_STEP_TYPE", id_step_type);
	}

	public RowMetaAndData getStepAttribute(long id_step_attribute) throws KettleException
	{
		return getOneRow("R_STEP_ATTRIBUTE", "ID_STEP_ATTRIBUTE", id_step_attribute);
	}

	public RowMetaAndData getStepDatabase(long id_step) throws KettleException
	{
		return getOneRow("R_STEP_DATABASE", "ID_STEP", id_step);
	}

	public RowMetaAndData getTransHop(long id_trans_hop) throws KettleException
	{
		return getOneRow("R_TRANS_HOP", "ID_TRANS_HOP", id_trans_hop);
	}

	public RowMetaAndData getJobHop(long id_job_hop) throws KettleException
	{
		return getOneRow("R_JOB_HOP", "ID_JOB_HOP", id_job_hop);
	}

	public RowMetaAndData getTransDependency(long id_dependency) throws KettleException
	{
		return getOneRow("R_DEPENDENCY", "ID_DEPENDENCY", id_dependency);
	}

	public RowMetaAndData getTransformation(long id_transformation) throws KettleException
	{
		return getOneRow("R_TRANSFORMATION", "ID_TRANSFORMATION", id_transformation);
	}

	public RowMetaAndData getUser(long id_user) throws KettleException
	{
		return getOneRow("R_USER", "ID_USER", id_user);
	}

	public RowMetaAndData getProfile(long id_profile) throws KettleException
	{
		return getOneRow("R_PROFILE", "ID_PROFILE", id_profile);
	}

	public RowMetaAndData getPermission(long id_permission) throws KettleException
	{
		return getOneRow("R_PERMISSION", "ID_PERMISSION", id_permission);
	}

	public RowMetaAndData getJob(long id_job) throws KettleException
	{
		return getOneRow("R_JOB", "ID_JOB", id_job);
	}

	public RowMetaAndData getJobEntry(long id_jobentry) throws KettleException
	{
		return getOneRow("R_JOBENTRY", "ID_JOBENTRY", id_jobentry);
	}

	public RowMetaAndData getJobEntryCopy(long id_jobentry_copy) throws KettleException
	{
		return getOneRow("R_JOBENTRY_COPY", "ID_JOBENTRY_COPY", id_jobentry_copy);
	}

	public RowMetaAndData getJobEntryType(long id_jobentry_type) throws KettleException
	{
		return getOneRow("R_JOBENTRY_TYPE", "ID_JOBENTRY_TYPE", id_jobentry_type);
	}

	public RowMetaAndData getDirectory(long id_directory) throws KettleException
	{
		return getOneRow("R_DIRECTORY", "ID_DIRECTORY", id_directory);
	}
	
    public RowMetaAndData getPartitionSchema(long id_partition_schema) throws KettleException
    {
        return getOneRow("R_PARTITION_SCHEMA", "ID_PARTITION_SCHEMA", id_partition_schema);
    }
    
    public RowMetaAndData getPartition(long id_partition) throws KettleException
    {
        return getOneRow("R_PARTITION", "ID_PARTITION", id_partition);
    }

    public RowMetaAndData getClusterSchema(long id_cluster_schema) throws KettleException
    {
        return getOneRow("R_CLUSTER", "ID_CLUSTER", id_cluster_schema);
    }

    public RowMetaAndData getSlaveServer(long id_slave) throws KettleException
    {
        return getOneRow("R_SLAVE", "ID_SLAVE", id_slave);
    }

	private RowMetaAndData getOneRow(String tablename, String keyfield, long id) throws KettleException
	{
		String sql = "SELECT * FROM " + tablename + " WHERE " 
			+ database.getDatabaseMeta().quoteField(keyfield) + " = " + id;

		return database.getOneRow(sql);
	}

	// STEP ATTRIBUTES: SAVE

	public synchronized long saveStepAttribute(long id_transformation, long id_step, String code, String value)
			throws KettleException
	{
		return saveStepAttribute(code, 0, id_transformation, id_step, 0.0, value);
	}

	public synchronized long saveStepAttribute(long id_transformation, long id_step, String code, double value)
			throws KettleException
	{
		return saveStepAttribute(code, 0, id_transformation, id_step, value, null);
	}

	public synchronized long saveStepAttribute(long id_transformation, long id_step, String code, boolean value) throws KettleException
	{
		return saveStepAttribute(code, 0, id_transformation, id_step, 0.0, value ? "Y" : "N");
	}

	public synchronized long saveStepAttribute(long id_transformation, long id_step, long nr, String code, String value) throws KettleException
	{
		return saveStepAttribute(code, nr, id_transformation, id_step, 0.0, value);
	}

	public synchronized long saveStepAttribute(long id_transformation, long id_step, long nr, String code, double value) throws KettleException
	{
		return saveStepAttribute(code, nr, id_transformation, id_step, value, null);
	}

	public synchronized long saveStepAttribute(long id_transformation, long id_step, long nr, String code, boolean value) throws KettleException
	{
		return saveStepAttribute(code, nr, id_transformation, id_step, 0.0, value ? "Y" : "N");
	}

	private long saveStepAttribute(String code, long nr, long id_transformation, long id_step, double value_num, String value_str) throws KettleException
	{
		return insertStepAttribute(id_transformation, id_step, nr, code, value_num, value_str);
	}

	// STEP ATTRIBUTES: GET

	public synchronized void setLookupStepAttribute() throws KettleException
	{
		String sql = "SELECT VALUE_STR, VALUE_NUM FROM R_STEP_ATTRIBUTE WHERE ID_STEP = ?  AND CODE = ?  AND NR = ? ";

		psStepAttributesLookup = database.prepareSQL(sql);
	}
    
    public synchronized void setLookupTransAttribute() throws KettleException
    {
        String sql = "SELECT VALUE_STR, VALUE_NUM FROM R_TRANS_ATTRIBUTE WHERE ID_TRANSFORMATION = ?  AND CODE = ?  AND NR = ? ";

        psTransAttributesLookup = database.prepareSQL(sql);
    }
    
    public synchronized void closeTransAttributeLookupPreparedStatement() throws KettleException
    {
        database.closePreparedStatement(psTransAttributesLookup);
        psTransAttributesLookup = null;
    }


	public synchronized void closeStepAttributeLookupPreparedStatement() throws KettleException
	{
		database.closePreparedStatement(psStepAttributesLookup);
		psStepAttributesLookup = null;
	}
	
	public synchronized void closeStepAttributeInsertPreparedStatement() throws KettleException
	{
	    if (psStepAttributesInsert!=null)
	    {
		    database.insertFinished(psStepAttributesInsert, true); // batch mode!
			psStepAttributesInsert = null;
	    }
	}

    public synchronized void closeTransAttributeInsertPreparedStatement() throws KettleException
    {
        if (psTransAttributesInsert!=null)
        {
            database.insertFinished(psTransAttributesInsert, true); // batch mode!
            psTransAttributesInsert = null;
        }
    }


	private RowMetaAndData getStepAttributeRow(long id_step, int nr, String code) throws KettleException
	{
		RowMetaAndData par = new RowMetaAndData();
		par.addValue(new ValueMeta("ID_STEP", ValueMetaInterface.TYPE_INTEGER), new Long(id_step));
		par.addValue(new ValueMeta("CODE", ValueMetaInterface.TYPE_STRING), code);
		par.addValue(new ValueMeta("NR", ValueMetaInterface.TYPE_INTEGER), new Long(nr));

		database.setValues(par.getRowMeta(), par.getData(), psStepAttributesLookup);

		Object[] rowData =  database.getLookup(psStepAttributesLookup);
        return new RowMetaAndData(database.getReturnRowMeta(), rowData);
	}

    public RowMetaAndData getTransAttributeRow(long id_transformation, int nr, String code) throws KettleException
    {
        RowMetaAndData par = new RowMetaAndData();
        par.addValue(new ValueMeta("ID_TRANSFORMATION", ValueMetaInterface.TYPE_INTEGER), new Long(id_transformation));
        par.addValue(new ValueMeta("CODE", ValueMetaInterface.TYPE_STRING), code);
        par.addValue(new ValueMeta("NR", ValueMetaInterface.TYPE_INTEGER), new Long(nr));

        database.setValues(par, psTransAttributesLookup);
        Object[] r = database.getLookup(psTransAttributesLookup);
        if (r==null) return null;
        return new RowMetaAndData(database.getReturnRowMeta(), r);
    }

	public synchronized long getStepAttributeInteger(long id_step, int nr, String code) throws KettleException
	{
		RowMetaAndData r = null;
		if (stepAttributesBuffer!=null) r = searchStepAttributeInBuffer(id_step, code, (long)nr);
		else                            r = getStepAttributeRow(id_step, nr, code);
		if (r == null)
			return 0;
		return r.getInteger("VALUE_NUM", 0L);
	}

	public synchronized String getStepAttributeString(long id_step, int nr, String code) throws KettleException
	{
		RowMetaAndData r = null;
		if (stepAttributesBuffer!=null) r = searchStepAttributeInBuffer(id_step, code, (long)nr);
		else                            r = getStepAttributeRow(id_step, nr, code);
		if (r == null)
			return null;
		return r.getString("VALUE_STR", null);
	}

	public boolean getStepAttributeBoolean(long id_step, int nr, String code, boolean def) throws KettleException
	{
		RowMetaAndData r = null;
		if (stepAttributesBuffer!=null) r = searchStepAttributeInBuffer(id_step, code, (long)nr);
		else                            r = getStepAttributeRow(id_step, nr, code);
		
		if (r == null) return def;
        String v = r.getString("VALUE_STR", null);
        if (v==null || Const.isEmpty(v)) return def;
		return ValueMeta.convertStringToBoolean(v).booleanValue();
	}

    public boolean getStepAttributeBoolean(long id_step, int nr, String code) throws KettleException
    {
        RowMetaAndData r = null;
        if (stepAttributesBuffer!=null) r = searchStepAttributeInBuffer(id_step, code, (long)nr);
        else                            r = getStepAttributeRow(id_step, nr, code);
        if (r == null)
            return false;
        return ValueMeta.convertStringToBoolean(r.getString("VALUE_STR", null)).booleanValue();
    }

	public synchronized long getStepAttributeInteger(long id_step, String code) throws KettleException
	{
		return getStepAttributeInteger(id_step, 0, code);
	}

	public synchronized String getStepAttributeString(long id_step, String code) throws KettleException
	{
		return getStepAttributeString(id_step, 0, code);
	}

	public boolean getStepAttributeBoolean(long id_step, String code) throws KettleException
	{
		return getStepAttributeBoolean(id_step, 0, code);
	}

	public synchronized int countNrStepAttributes(long id_step, String code) throws KettleException
	{
	    if (stepAttributesBuffer!=null) // see if we can do this in memory...
	    {
	        int nr = searchNrStepAttributes(id_step, code);
            return nr;
	    }
	    else
	    {
			String sql = "SELECT COUNT(*) FROM R_STEP_ATTRIBUTE WHERE ID_STEP = ? AND CODE = ?";
			RowMetaAndData table = new RowMetaAndData();
			table.addValue(new ValueMeta("ID_STEP", ValueMetaInterface.TYPE_INTEGER), new Long(id_step));
			table.addValue(new ValueMeta("CODE", ValueMetaInterface.TYPE_STRING), code);
			RowMetaAndData r = database.getOneRow(sql, table.getRowMeta(), table.getData());
			if (r == null) return 0;
            return (int) r.getInteger(0, 0L);
	    }
	}
    
    // TRANS ATTRIBUTES: get
    
    public synchronized String getTransAttributeString(long id_transformation, int nr, String code) throws KettleException
    {
        RowMetaAndData r = null;
        r = getTransAttributeRow(id_transformation, nr, code);
        if (r == null)
            return null;
        return r.getString("VALUE_STR", null);
    }

    public synchronized boolean getTransAttributeBoolean(long id_transformation, int nr, String code) throws KettleException
    {
        RowMetaAndData r = null;
        r = getTransAttributeRow(id_transformation, nr, code);
        if (r == null)
            return false;
        return r.getBoolean("VALUE_STR", false);
    }

    public synchronized double getTransAttributeNumber(long id_transformation, int nr, String code) throws KettleException
    {
        RowMetaAndData r = null;
        r = getTransAttributeRow(id_transformation, nr, code);
        if (r == null)
            return 0.0;
        return r.getNumber("VALUE_NUM", 0.0);
    }

    public synchronized long getTransAttributeInteger(long id_transformation, int nr, String code) throws KettleException
    {
        RowMetaAndData r = null;
        r = getTransAttributeRow(id_transformation, nr, code);
        if (r == null)
            return 0L;
        return r.getInteger("VALUE_NUM", 0L);
    }
    
    public synchronized int countNrTransAttributes(long id_transformation, String code) throws KettleException
    {
        String sql = "SELECT COUNT(*) FROM R_TRANS_ATTRIBUTE WHERE ID_TRANSFORMATION = ? AND CODE = ?";
        RowMetaAndData table = new RowMetaAndData();
        table.addValue(new ValueMeta("ID_STEP", ValueMetaInterface.TYPE_INTEGER), new Long(id_transformation));
        table.addValue(new ValueMeta("CODE", ValueMetaInterface.TYPE_STRING), code);
        RowMetaAndData r = database.getOneRow(sql, table.getRowMeta(), table.getData());
        if (r == null)
            return 0;
        
        return (int) r.getInteger(0, 0L);
    }

    public synchronized List getTransAttributes(long id_transformation, String code, long nr) throws KettleException
    {
        String sql = "SELECT * FROM R_TRANS_ATTRIBUTE WHERE ID_TRANSFORMATION = ? AND CODE = ? AND NR = ? ORDER BY VALUE_NUM";
        RowMetaAndData table = new RowMetaAndData();
        table.addValue(new ValueMeta("ID_STEP", ValueMetaInterface.TYPE_INTEGER), new Long(id_transformation));
        table.addValue(new ValueMeta("CODE", ValueMetaInterface.TYPE_STRING), code);
        table.addValue(new ValueMeta("NR", ValueMetaInterface.TYPE_INTEGER), new Long(nr));
        
        return database.getRows(sql, 0);
    }

	// JOBENTRY ATTRIBUTES: SAVE

	// WANTED: throw extra exceptions to locate storage problems (strings too long etc)
	//
	public synchronized long saveJobEntryAttribute(long id_job, long id_jobentry, String code, String value)
			throws KettleException
	{
		return saveJobEntryAttribute(code, 0, id_job, id_jobentry, 0.0, value);
	}

	public synchronized long saveJobEntryAttribute(long id_job, long id_jobentry, String code, double value)
			throws KettleException
	{
		return saveJobEntryAttribute(code, 0, id_job, id_jobentry, value, null);
	}

	public synchronized long saveJobEntryAttribute(long id_job, long id_jobentry, String code, boolean value)
			throws KettleException
	{
		return saveJobEntryAttribute(code, 0, id_job, id_jobentry, 0.0, value ? "Y" : "N");
	}

	public synchronized long saveJobEntryAttribute(long id_job, long id_jobentry, long nr, String code, String value)
			throws KettleException
	{
		return saveJobEntryAttribute(code, nr, id_job, id_jobentry, 0.0, value);
	}

	public synchronized long saveJobEntryAttribute(long id_job, long id_jobentry, long nr, String code, double value)
			throws KettleException
	{
		return saveJobEntryAttribute(code, nr, id_job, id_jobentry, value, null);
	}

	public synchronized long saveJobEntryAttribute(long id_job, long id_jobentry, long nr, String code, boolean value)
			throws KettleException
	{
		return saveJobEntryAttribute(code, nr, id_job, id_jobentry, 0.0, value ? "Y" : "N");
	}

	private long saveJobEntryAttribute(String code, long nr, long id_job, long id_jobentry, double value_num,
			String value_str) throws KettleException
	{
		return insertJobEntryAttribute(id_job, id_jobentry, nr, code, value_num, value_str);
	}

	// JOBENTRY ATTRIBUTES: GET

	public synchronized void setLookupJobEntryAttribute() throws KettleException
	{
		String sql = "SELECT VALUE_STR, VALUE_NUM FROM R_JOBENTRY_ATTRIBUTE WHERE ID_JOBENTRY = ?  AND CODE = ?  AND NR = ? ";

		pstmt_entry_attributes = database.prepareSQL(sql);
	}

	public synchronized void closeLookupJobEntryAttribute() throws KettleException
	{
		database.closePreparedStatement(pstmt_entry_attributes);
        pstmt_entry_attributes = null;
	}

	private RowMetaAndData getJobEntryAttributeRow(long id_jobentry, int nr, String code) throws KettleException
	{
		RowMetaAndData par = new RowMetaAndData();
		par.addValue(new ValueMeta("ID_JOBENTRY", ValueMetaInterface.TYPE_INTEGER), new Long(id_jobentry));
		par.addValue(new ValueMeta("CODE", ValueMetaInterface.TYPE_STRING), code);
		par.addValue(new ValueMeta("NR", ValueMetaInterface.TYPE_INTEGER), new Long(nr));

		database.setValues(par.getRowMeta(), par.getData(), pstmt_entry_attributes);
		Object[] rowData = database.getLookup(pstmt_entry_attributes);
        return new RowMetaAndData(database.getReturnRowMeta(), rowData);
	}

	public synchronized long getJobEntryAttributeInteger(long id_jobentry, int nr, String code) throws KettleException
	{
		RowMetaAndData r = getJobEntryAttributeRow(id_jobentry, nr, code);
		if (r == null)
			return 0;
		return r.getInteger("VALUE_NUM", 0L);
	}

	public double getJobEntryAttributeNumber(long id_jobentry, int nr, String code) throws KettleException
	{
		RowMetaAndData r = getJobEntryAttributeRow(id_jobentry, nr, code);
		if (r == null)
			return 0.0;
		return r.getNumber("VALUE_NUM", 0.0);
	}

	public synchronized String getJobEntryAttributeString(long id_jobentry, int nr, String code) throws KettleException
	{
		RowMetaAndData r = getJobEntryAttributeRow(id_jobentry, nr, code);
		if (r == null)
			return null;
		return r.getString("VALUE_STR", null);
	}

	public boolean getJobEntryAttributeBoolean(long id_jobentry, int nr, String code) throws KettleException
	{
		return getJobEntryAttributeBoolean(id_jobentry, nr, code, false);
	}

	public boolean getJobEntryAttributeBoolean(long id_jobentry, int nr, String code, boolean def) throws KettleException
	{
		RowMetaAndData r = getJobEntryAttributeRow(id_jobentry, nr, code);
		if (r == null) return def;
        String v = r.getString("VALUE_STR", null);
        if (v==null || Const.isEmpty(v)) return def;
        return ValueMeta.convertStringToBoolean(v).booleanValue();
	}

	public double getJobEntryAttributeNumber(long id_jobentry, String code) throws KettleException
	{
		return getJobEntryAttributeNumber(id_jobentry, 0, code);
	}

	public synchronized long getJobEntryAttributeInteger(long id_jobentry, String code) throws KettleException
	{
		return getJobEntryAttributeInteger(id_jobentry, 0, code);
	}

	public synchronized String getJobEntryAttributeString(long id_jobentry, String code) throws KettleException
	{
		return getJobEntryAttributeString(id_jobentry, 0, code);
	}

	public boolean getJobEntryAttributeBoolean(long id_jobentry, String code) throws KettleException
	{
		return getJobEntryAttributeBoolean(id_jobentry, 0, code, false);
	}

	public boolean getJobEntryAttributeBoolean(long id_jobentry, String code, boolean def) throws KettleException
	{
		return getJobEntryAttributeBoolean(id_jobentry, 0, code, def);
	}

	public synchronized int countNrJobEntryAttributes(long id_jobentry, String code) throws KettleException
	{
		String sql = "SELECT COUNT(*) FROM R_JOBENTRY_ATTRIBUTE WHERE ID_JOBENTRY = ? AND CODE = ?";
		RowMetaAndData table = new RowMetaAndData();
		table.addValue(new ValueMeta("ID_JOBENTRY", ValueMetaInterface.TYPE_INTEGER), new Long(id_jobentry));
		table.addValue(new ValueMeta("CODE", ValueMetaInterface.TYPE_STRING), code);
		RowMetaAndData r = database.getOneRow(sql, table.getRowMeta(), table.getData());
		if (r == null) return 0;
		return (int) r.getInteger(0, 0L);
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	// DELETE DATA IN REPOSITORY
	//////////////////////////////////////////////////////////////////////////////////////////

	public synchronized void delSteps(long id_transformation) throws KettleException
	{
		String sql = "DELETE FROM R_STEP WHERE ID_TRANSFORMATION = " + id_transformation;
		database.execStatement(sql);
	}

	public synchronized void delCondition(long id_condition) throws KettleException
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

	public synchronized void delStepConditions(long id_transformation) throws KettleException
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
	 * @throws KettleException in case something unexpected happens.
	 */
	public synchronized void delStepDatabases(long id_transformation) throws KettleException
	{
		String sql = "DELETE FROM R_STEP_DATABASE WHERE ID_TRANSFORMATION = " + id_transformation;
		database.execStatement(sql);
	}

	public synchronized void delJobEntries(long id_job) throws KettleException
	{
		String sql = "DELETE FROM R_JOBENTRY WHERE ID_JOB = " + id_job;
		database.execStatement(sql);
	}

	public synchronized void delJobEntryCopies(long id_job) throws KettleException
	{
		String sql = "DELETE FROM R_JOBENTRY_COPY WHERE ID_JOB = " + id_job;
		database.execStatement(sql);
	}

	public synchronized void delDependencies(long id_transformation) throws KettleException
	{
		String sql = "DELETE FROM R_DEPENDENCY WHERE ID_TRANSFORMATION = " + id_transformation;
		database.execStatement(sql);
	}

	public synchronized void delStepAttributes(long id_transformation) throws KettleException
	{
		String sql = "DELETE FROM R_STEP_ATTRIBUTE WHERE ID_TRANSFORMATION = " + id_transformation;
		database.execStatement(sql);
	}

    public synchronized void delTransAttributes(long id_transformation) throws KettleException
    {
        String sql = "DELETE FROM R_TRANS_ATTRIBUTE WHERE ID_TRANSFORMATION = " + id_transformation;
        database.execStatement(sql);
    }
    
    public synchronized void delPartitionSchemas(long id_transformation) throws KettleException
    {
        String sql = "DELETE FROM R_TRANS_PARTITION_SCHEMA WHERE ID_TRANSFORMATION = " + id_transformation;
        database.execStatement(sql);
    }

    public synchronized void delPartitions(long id_partition_schema) throws KettleException
    {
        // First see if the partition is used by a step, transformation etc.
        // 
        database.execStatement("DELETE FROM R_PARTITION WHERE ID_PARTITION_SCHEMA = " + id_partition_schema);
    }
    
    public synchronized void delClusterSlaves(long id_cluster) throws KettleException
    {
        String sql = "DELETE FROM R_CLUSTER_SLAVE WHERE ID_CLUSTER = " + id_cluster;
        database.execStatement(sql);
    }
    
    public synchronized void delTransformationClusters(long id_transformation) throws KettleException
    {
        String sql = "DELETE FROM R_TRANS_CLUSTER WHERE ID_TRANSFORMATION = " + id_transformation;
        database.execStatement(sql);
    }

    public synchronized void delTransformationSlaves(long id_transformation) throws KettleException
    {
        String sql = "DELETE FROM R_TRANS_SLAVE WHERE ID_TRANSFORMATION = " + id_transformation;
        database.execStatement(sql);
    }


	public synchronized void delJobEntryAttributes(long id_job) throws KettleException
	{
		String sql = "DELETE FROM R_JOBENTRY_ATTRIBUTE WHERE ID_JOB = " + id_job;
		database.execStatement(sql);
	}

	public synchronized void delTransHops(long id_transformation) throws KettleException
	{
		String sql = "DELETE FROM R_TRANS_HOP WHERE ID_TRANSFORMATION = " + id_transformation;
		database.execStatement(sql);
	}

	public synchronized void delJobHops(long id_job) throws KettleException
	{
		String sql = "DELETE FROM R_JOB_HOP WHERE ID_JOB = " + id_job;
		database.execStatement(sql);
	}

	public synchronized void delTransNotes(long id_transformation) throws KettleException
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

	public synchronized void delJobNotes(long id_job) throws KettleException
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

	public synchronized void delTrans(long id_transformation) throws KettleException
	{
		String sql = "DELETE FROM R_TRANSFORMATION WHERE ID_TRANSFORMATION = " + id_transformation;
		database.execStatement(sql);
	}

	public synchronized void delJob(long id_job) throws KettleException
	{
		String sql = "DELETE FROM R_JOB WHERE ID_JOB = " + id_job;
		database.execStatement(sql);
	}

	public synchronized void delDatabase(long id_database) throws KettleException
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
    
    public synchronized void delDatabaseAttributes(long id_database) throws KettleException
    {
        String sql = "DELETE FROM R_DATABASE_ATTRIBUTE WHERE ID_DATABASE = " + id_database;
        database.execStatement(sql);
    }

	public synchronized void delTransStepCondition(long id_transformation) throws KettleException
	{
		String sql = "DELETE FROM R_TRANS_STEP_CONDITION WHERE ID_TRANSFORMATION = " + id_transformation;
		database.execStatement(sql);
	}

	public synchronized void delValue(long id_value) throws KettleException
	{
		String sql = "DELETE FROM R_VALUE WHERE ID_VALUE = " + id_value;
		database.execStatement(sql);
	}

	public synchronized void delUser(long id_user) throws KettleException
	{
		String sql = "DELETE FROM R_USER WHERE ID_USER = " + id_user;
		database.execStatement(sql);
	}

	public synchronized void delProfile(long id_profile) throws KettleException
	{
		String sql = "DELETE FROM R_PROFILE WHERE ID_PROFILE = " + id_profile;
		database.execStatement(sql);
	}

	public synchronized void delProfilePermissions(long id_profile) throws KettleException
	{
		String sql = "DELETE FROM R_PROFILE_PERMISSION WHERE ID_PROFILE = " + id_profile;
		database.execStatement(sql);
	}
    
    public synchronized void delSlave(long id_slave) throws KettleException
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
   
    public synchronized void delPartitionSchema(long id_partition_schema) throws KettleException
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
    
    public synchronized void delClusterSchema(long id_cluster) throws KettleException
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


	public synchronized void delAllFromTrans(long id_transformation) throws KettleException
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

	public synchronized void renameTransformation(long id_transformation, String newname) throws KettleException
	{
        String nameField = databaseMeta.quoteField("NAME");
		String sql = "UPDATE R_TRANSFORMATION SET "+nameField+" = ? WHERE ID_TRANSFORMATION = ?";

		RowMetaAndData table = new RowMetaAndData();
		table.addValue(new ValueMeta("NAME",  ValueMetaInterface.TYPE_STRING), newname);
		table.addValue(new ValueMeta("ID_TRANSFORMATION",  ValueMetaInterface.TYPE_INTEGER), new Long(id_transformation));

		database.execStatement(sql, table.getRowMeta(), table.getData());
	}

	public synchronized void renameUser(long id_user, String newname) throws KettleException
	{
        String nameField = databaseMeta.quoteField("NAME");
		String sql = "UPDATE R_USER SET "+nameField+" = ? WHERE ID_USER = ?";

		RowMetaAndData table = new RowMetaAndData();
		table.addValue(new ValueMeta("NAME", ValueMetaInterface.TYPE_STRING), newname);
		table.addValue(new ValueMeta("ID_USER", ValueMetaInterface.TYPE_INTEGER), new Long(id_user));

		database.execStatement(sql, table.getRowMeta(), table.getData());
	}

	public synchronized void renameProfile(long id_profile, String newname) throws KettleException
	{
        String nameField = databaseMeta.quoteField("NAME");
		String sql = "UPDATE R_PROFILE SET "+nameField+" = ? WHERE ID_PROFILE = ?";

		RowMetaAndData table = new RowMetaAndData();
		table.addValue(new ValueMeta("NAME", ValueMetaInterface.TYPE_STRING), newname);
		table.addValue(new ValueMeta("ID_PROFILE", ValueMetaInterface.TYPE_INTEGER), new Long(id_profile));

		database.execStatement(sql, table.getRowMeta(), table.getData());
	}

	public synchronized void renameDatabase(long id_database, String newname) throws KettleException
	{
        String nameField = databaseMeta.quoteField("NAME");
		String sql = "UPDATE R_DATABASE SET "+nameField+" = ? WHERE ID_DATABASE = ?";

		RowMetaAndData table = new RowMetaAndData();
		table.addValue(new ValueMeta("NAME", ValueMetaInterface.TYPE_STRING), newname);
		table.addValue(new ValueMeta("ID_DATABASE", ValueMetaInterface.TYPE_INTEGER), new Long(id_database));

		database.execStatement(sql, table.getRowMeta(), table.getData());
	}

	public synchronized void delAllFromJob(long id_job) throws KettleException
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

	public synchronized void renameJob(long id_job, String newname) throws KettleException
	{
        String nameField = databaseMeta.quoteField("NAME");
		String sql = "UPDATE R_JOB SET "+nameField+" = ? WHERE ID_JOB = ?";

		RowMetaAndData table = new RowMetaAndData();
		table.addValue(new ValueMeta("NAME", ValueMetaInterface.TYPE_STRING), newname);
		table.addValue(new ValueMeta("ID_JOB", ValueMetaInterface.TYPE_INTEGER), new Long(id_job));

		database.execStatement(sql, table.getRowMeta(), table.getData());
	}

    /**
     * Create or upgrade repository tables & fields, populate lookup tables, ...
     * 
     * @param monitor The progress monitor to use, or null if no monitor is present.
     * @param upgrade True if you want to upgrade the repository, false if you want to create it.
     * @throws KettleException in case something goes wrong!
     */
	public synchronized void createRepositorySchema(IProgressMonitor monitor, boolean upgrade) throws KettleException
	{
		RowMetaInterface table;
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
        table = new RowMeta();
        tablename = "R_REPOSITORY_LOG";
        if (monitor!=null) monitor.subTask("Checking table "+tablename);
        table.addValueMeta(new ValueMeta("ID_REPOSITORY_LOG", ValueMetaInterface.TYPE_INTEGER, KEY, 0));
        table.addValueMeta(new ValueMeta("REP_VERSION",    ValueMetaInterface.TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
        table.addValueMeta(new ValueMeta("LOG_DATE",       ValueMetaInterface.TYPE_DATE));
        table.addValueMeta(new ValueMeta("LOG_USER",       ValueMetaInterface.TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
        table.addValueMeta(new ValueMeta("OPERATION_DESC", ValueMetaInterface.TYPE_STRING, REP_STRING_LENGTH, 0));
        sql = database.getDDL(tablename, table, null, false, "ID_REPOSITORY_LOG", false);
        
        if (sql != null && sql.length() > 0)
        {
            try
            {
                if (log.isDetailed()) log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
                database.execStatements(sql);
                if (log.isDetailed()) log.logDetailed(toString(), "Created/altered table " + tablename);
            }
            catch (KettleException dbe)
            {
                throw new KettleException("Unable to create or modify table " + tablename, dbe);
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
        table = new RowMeta();
        tablename = "R_VERSION";
        if (monitor!=null) monitor.subTask("Checking table "+tablename);
        table.addValueMeta(new ValueMeta("ID_VERSION",       ValueMetaInterface.TYPE_INTEGER, KEY, 0));
        table.addValueMeta(new ValueMeta("MAJOR_VERSION",    ValueMetaInterface.TYPE_INTEGER, 3, 0));
        table.addValueMeta(new ValueMeta("MINOR_VERSION",    ValueMetaInterface.TYPE_INTEGER, 3, 0));
        table.addValueMeta(new ValueMeta("UPGRADE_DATE",     ValueMetaInterface.TYPE_DATE, 0, 0));
        table.addValueMeta(new ValueMeta("IS_UPGRADE",       ValueMetaInterface.TYPE_BOOLEAN, 1, 0));
        sql = database.getDDL(tablename, table, null, false, "ID_VERSION", false);

        if (sql != null && sql.length() > 0)
        {
            try
            {
                if (log.isDetailed()) log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
                database.execStatements(sql);
                if (log.isDetailed()) log.logDetailed(toString(), "Created/altered table " + tablename);
            }
            catch (KettleException dbe)
            {
                throw new KettleException("Unable to create or modify table " + tablename, dbe);
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
            Object[] data = new Object[] {
                    new Long(getNextID(tablename, "ID_VERSION")),
                    new Long(REQUIRED_MAJOR_VERSION),
                    new Long(REQUIRED_MINOR_VERSION),
                    new Date(),
                    new Boolean(upgrade),
                };
            database.execStatement("INSERT INTO R_VERSION VALUES(?, ?, ?, ?, ?)", table, data);
        }
        catch(KettleException e)
        {
            throw new KettleException("Unable to insert new version log record into "+tablename, e);
        }
        
		//////////////////////////////////////////////////////////////////////////////////
		// R_DATABASE_TYPE
		//
		// Create table...
		//
		boolean ok_database_type = true;
		table = new RowMeta();
		tablename = "R_DATABASE_TYPE";
		if (monitor!=null) monitor.subTask("Checking table "+tablename);
		table.addValueMeta(new ValueMeta("ID_DATABASE_TYPE", ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta("CODE",             ValueMetaInterface.TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
		table.addValueMeta(new ValueMeta("DESCRIPTION",      ValueMetaInterface.TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
		sql = database.getDDL(tablename, table, null, false, "ID_DATABASE_TYPE", false);

		if (sql != null && sql.length() > 0)
		{
			try
			{
                if (log.isDetailed()) log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
				database.execStatements(sql);
                if (log.isDetailed()) log.logDetailed(toString(), "Created/altered table " + tablename);
			}
			catch (KettleException dbe)
			{
				throw new KettleException("Unable to create or modify table " + tablename, dbe);
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
				RowMetaAndData lookup = null;
                if (upgrade) lookup = database.getOneRow("SELECT ID_DATABASE_TYPE FROM " + tablename + " WHERE " 
                		+ database.getDatabaseMeta().quoteField("CODE") +" = '" + code[i] + "'");
				if (lookup == null)
				{
					long nextid = getNextDatabaseTypeID();

					Object[] tableData = new Object[] { new Long(nextid), code[i], desc[i], };
					database.setValuesInsert(table, tableData);
					database.insertRow();
				}
			}

			try
			{
				database.closeInsert();
                if (log.isDetailed()) log.logDetailed(toString(), "Populated table " + tablename);
			}
			catch (KettleException dbe)
			{
                throw new KettleException("Unable to close insert after populating table " + tablename, dbe);
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
		table = new RowMeta();
		tablename = "R_DATABASE_CONTYPE";
		if (monitor!=null) monitor.subTask("Checking table "+tablename);
		table.addValueMeta(new ValueMeta("ID_DATABASE_CONTYPE", ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta("CODE", ValueMetaInterface.TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
		table.addValueMeta(new ValueMeta("DESCRIPTION", ValueMetaInterface.TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
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
                RowMetaAndData lookup = null;
                if (upgrade) lookup = database.getOneRow("SELECT ID_DATABASE_CONTYPE FROM " + tablename + " WHERE " 
                		+ database.getDatabaseMeta().quoteField("CODE") + " = '" + code[i] + "'");
				if (lookup == null)
				{
					long nextid = getNextDatabaseConnectionTypeID();

                    Object[] tableData = new Object[] { 
                            new Long(nextid),
                            code[i],
                            desc[i],
                    };
					database.setValuesInsert(table, tableData);
					database.insertRow();
				}
			}

            try
            {
                database.closeInsert();
                if (log.isDetailed()) log.logDetailed(toString(), "Populated table " + tablename);
            }
            catch(KettleException dbe)
            {
                throw new KettleException("Unable to close insert after populating table " + tablename, dbe);
            }
		}
		if (monitor!=null) monitor.worked(1);

		//////////////////////////////////////////////////////////////////////////////////
		//
		// R_NOTE
		//
		// Create table...
		table = new RowMeta();
		tablename = "R_NOTE";
		if (monitor!=null) monitor.subTask("Checking table "+tablename);
		table.addValueMeta(new ValueMeta("ID_NOTE", ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta("VALUE_STR", ValueMetaInterface.TYPE_STRING, REP_STRING_LENGTH, 0));
		table.addValueMeta(new ValueMeta("GUI_LOCATION_X", ValueMetaInterface.TYPE_INTEGER, 6, 0));
		table.addValueMeta(new ValueMeta("GUI_LOCATION_Y", ValueMetaInterface.TYPE_INTEGER, 6, 0));
		table.addValueMeta(new ValueMeta("GUI_LOCATION_WIDTH", ValueMetaInterface.TYPE_INTEGER, 6, 0));
		table.addValueMeta(new ValueMeta("GUI_LOCATION_HEIGHT", ValueMetaInterface.TYPE_INTEGER, 6, 0));
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
		table = new RowMeta();
		tablename = "R_DATABASE";
		if (monitor!=null) monitor.subTask("Checking table "+tablename);
		table.addValueMeta(new ValueMeta("ID_DATABASE", ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta("NAME", ValueMetaInterface.TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
		table.addValueMeta(new ValueMeta("ID_DATABASE_TYPE", ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta("ID_DATABASE_CONTYPE", ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta("HOST_NAME", ValueMetaInterface.TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
		table.addValueMeta(new ValueMeta("DATABASE_NAME", ValueMetaInterface.TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
		table.addValueMeta(new ValueMeta("PORT", ValueMetaInterface.TYPE_INTEGER, 7, 0));
		table.addValueMeta(new ValueMeta("USERNAME", ValueMetaInterface.TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
		table.addValueMeta(new ValueMeta("PASSWORD", ValueMetaInterface.TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
		table.addValueMeta(new ValueMeta("SERVERNAME", ValueMetaInterface.TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
		table.addValueMeta(new ValueMeta("DATA_TBS", ValueMetaInterface.TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
		table.addValueMeta(new ValueMeta("INDEX_TBS", ValueMetaInterface.TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
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
        table = new RowMeta();
        tablename = "R_DATABASE_ATTRIBUTE";
        if (monitor!=null) monitor.subTask("Checking table "+tablename);
        table.addValueMeta(new ValueMeta("ID_DATABASE_ATTRIBUTE", ValueMetaInterface.TYPE_INTEGER, KEY, 0));
        table.addValueMeta(new ValueMeta("ID_DATABASE", ValueMetaInterface.TYPE_INTEGER, KEY, 0));
        table.addValueMeta(new ValueMeta("CODE", ValueMetaInterface.TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
        table.addValueMeta(new ValueMeta("VALUE_STR", ValueMetaInterface.TYPE_STRING, REP_STRING_LENGTH, 0));
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
            catch(KettleException kdbe)
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
		table = new RowMeta();
		tablename = "R_DIRECTORY";
		if (monitor!=null) monitor.subTask("Checking table "+tablename);
		table.addValueMeta(new ValueMeta("ID_DIRECTORY",        ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta("ID_DIRECTORY_PARENT", ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta("DIRECTORY_NAME",      ValueMetaInterface.TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
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
			catch(KettleException kdbe)
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
		table = new RowMeta();
		tablename = "R_TRANSFORMATION";
		if (monitor!=null) monitor.subTask("Checking table "+tablename);
		table.addValueMeta(new ValueMeta("ID_TRANSFORMATION", ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta("ID_DIRECTORY", ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta("NAME", ValueMetaInterface.TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
		table.addValueMeta(new ValueMeta("DESCRIPTION", ValueMetaInterface.TYPE_STRING, REP_STRING_LENGTH, 0));
		table.addValueMeta(new ValueMeta("EXTENDED_DESCRIPTION", ValueMetaInterface.TYPE_STRING, REP_STRING_LENGTH, 0));
		table.addValueMeta(new ValueMeta("TRANS_VERSION", ValueMetaInterface.TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
		table.addValueMeta(new ValueMeta("TRANS_STATUS", ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta("ID_STEP_READ", ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta("ID_STEP_WRITE", ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta("ID_STEP_INPUT", ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta("ID_STEP_OUTPUT", ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta("ID_STEP_UPDATE", ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta("ID_DATABASE_LOG", ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta("TABLE_NAME_LOG", ValueMetaInterface.TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
		table.addValueMeta(new ValueMeta("USE_BATCHID", ValueMetaInterface.TYPE_BOOLEAN, 1, 0));
		table.addValueMeta(new ValueMeta("USE_LOGFIELD", ValueMetaInterface.TYPE_BOOLEAN, 1, 0));
		table.addValueMeta(new ValueMeta("ID_DATABASE_MAXDATE", ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta("TABLE_NAME_MAXDATE", ValueMetaInterface.TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
		table.addValueMeta(new ValueMeta("FIELD_NAME_MAXDATE", ValueMetaInterface.TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
		table.addValueMeta(new ValueMeta("OFFSET_MAXDATE", ValueMetaInterface.TYPE_NUMBER, 12, 2));
		table.addValueMeta(new ValueMeta("DIFF_MAXDATE", ValueMetaInterface.TYPE_NUMBER, 12, 2));
		table.addValueMeta(new ValueMeta("CREATED_USER", ValueMetaInterface.TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
		table.addValueMeta(new ValueMeta("CREATED_DATE", ValueMetaInterface.TYPE_DATE, 20, 0));
		table.addValueMeta(new ValueMeta("MODIFIED_USER", ValueMetaInterface.TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
		table.addValueMeta(new ValueMeta("MODIFIED_DATE", ValueMetaInterface.TYPE_DATE, 20, 0));
		table.addValueMeta(new ValueMeta("SIZE_ROWSET", ValueMetaInterface.TYPE_INTEGER, KEY, 0));
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
		table = new RowMeta();
		tablename = "R_TRANS_ATTRIBUTE";
		if (monitor!=null) monitor.subTask("Checking table "+tablename);
		table.addValueMeta(new ValueMeta("ID_TRANS_ATTRIBUTE", ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta("ID_TRANSFORMATION", ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta("NR", ValueMetaInterface.TYPE_INTEGER, 6, 0));
		table.addValueMeta(new ValueMeta("CODE", ValueMetaInterface.TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
		table.addValueMeta(new ValueMeta("VALUE_NUM", ValueMetaInterface.TYPE_INTEGER, 18, 0));
		table.addValueMeta(new ValueMeta("VALUE_STR", ValueMetaInterface.TYPE_STRING, REP_STRING_LENGTH, 0));
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
			catch(KettleException kdbe)
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
		table = new RowMeta();
		tablename = "R_DEPENDENCY";
		if (monitor!=null) monitor.subTask("Checking table "+tablename);
		table.addValueMeta(new ValueMeta("ID_DEPENDENCY", ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta("ID_TRANSFORMATION", ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta("ID_DATABASE", ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta("TABLE_NAME", ValueMetaInterface.TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
		table.addValueMeta(new ValueMeta("FIELD_NAME", ValueMetaInterface.TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
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
        table = new RowMeta();
        tablename = "R_PARTITION_SCHEMA";
        if (monitor!=null) monitor.subTask("Checking table "+tablename);
        table.addValueMeta(new ValueMeta("ID_PARTITION_SCHEMA", ValueMetaInterface.TYPE_INTEGER, KEY, 0));
        table.addValueMeta(new ValueMeta("NAME", ValueMetaInterface.TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
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
        table = new RowMeta();
        tablename = "R_PARTITION";
        if (monitor!=null) monitor.subTask("Checking table "+tablename);
        table.addValueMeta(new ValueMeta("ID_PARTITION", ValueMetaInterface.TYPE_INTEGER, KEY, 0));
        table.addValueMeta(new ValueMeta("ID_PARTITION_SCHEMA", ValueMetaInterface.TYPE_INTEGER, KEY, 0));
        table.addValueMeta(new ValueMeta("PARTITION_ID", ValueMetaInterface.TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
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
        table = new RowMeta();
        tablename = "R_TRANS_PARTITION_SCHEMA";
        if (monitor!=null) monitor.subTask("Checking table "+tablename);
        table.addValueMeta(new ValueMeta("ID_TRANS_PARTITION_SCHEMA", ValueMetaInterface.TYPE_INTEGER, KEY, 0));
        table.addValueMeta(new ValueMeta("ID_TRANSFORMATION", ValueMetaInterface.TYPE_INTEGER, KEY, 0));
        table.addValueMeta(new ValueMeta("ID_PARTITION_SCHEMA", ValueMetaInterface.TYPE_INTEGER, KEY, 0));
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
        table = new RowMeta();
        tablename = "R_CLUSTER";
        if (monitor!=null) monitor.subTask("Checking table "+tablename);
        table.addValueMeta(new ValueMeta("ID_CLUSTER", ValueMetaInterface.TYPE_INTEGER, KEY, 0));
        table.addValueMeta(new ValueMeta("NAME", ValueMetaInterface.TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
        table.addValueMeta(new ValueMeta("BASE_PORT", ValueMetaInterface.TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
        table.addValueMeta(new ValueMeta("SOCKETS_BUFFER_SIZE", ValueMetaInterface.TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
        table.addValueMeta(new ValueMeta("SOCKETS_FLUSH_INTERVAL", ValueMetaInterface.TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
        table.addValueMeta(new ValueMeta("SOCKETS_COMPRESSED", ValueMetaInterface.TYPE_BOOLEAN, 0, 0));
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
        table = new RowMeta();
        tablename = "R_SLAVE";
        if (monitor!=null) monitor.subTask("Checking table "+tablename);
        table.addValueMeta(new ValueMeta("ID_SLAVE", ValueMetaInterface.TYPE_INTEGER, KEY, 0));
        table.addValueMeta(new ValueMeta("NAME", ValueMetaInterface.TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
        table.addValueMeta(new ValueMeta("HOST_NAME", ValueMetaInterface.TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
        table.addValueMeta(new ValueMeta("PORT", ValueMetaInterface.TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
        table.addValueMeta(new ValueMeta("USERNAME", ValueMetaInterface.TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
        table.addValueMeta(new ValueMeta("PASSWORD", ValueMetaInterface.TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
        table.addValueMeta(new ValueMeta("PROXY_HOST_NAME", ValueMetaInterface.TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
        table.addValueMeta(new ValueMeta("PROXY_PORT", ValueMetaInterface.TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
        table.addValueMeta(new ValueMeta("NON_PROXY_HOSTS", ValueMetaInterface.TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
        table.addValueMeta(new ValueMeta("MASTER", ValueMetaInterface.TYPE_BOOLEAN));
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
        table = new RowMeta();
        tablename = "R_CLUSTER_SLAVE";
        if (monitor!=null) monitor.subTask("Checking table "+tablename);
        table.addValueMeta(new ValueMeta("ID_CLUSTER_SLAVE", ValueMetaInterface.TYPE_INTEGER, KEY, 0));
        table.addValueMeta(new ValueMeta("ID_CLUSTER", ValueMetaInterface.TYPE_INTEGER, KEY, 0));
        table.addValueMeta(new ValueMeta("ID_SLAVE", ValueMetaInterface.TYPE_INTEGER, KEY, 0));
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
        table = new RowMeta();
        tablename = "R_TRANS_SLAVE";
        if (monitor!=null) monitor.subTask("Checking table "+tablename);
        table.addValueMeta(new ValueMeta("ID_TRANS_SLAVE", ValueMetaInterface.TYPE_INTEGER, KEY, 0));
        table.addValueMeta(new ValueMeta("ID_TRANSFORMATION", ValueMetaInterface.TYPE_INTEGER, KEY, 0));
        table.addValueMeta(new ValueMeta("ID_SLAVE", ValueMetaInterface.TYPE_INTEGER, KEY, 0));
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
        table = new RowMeta();
        tablename = "R_TRANS_CLUSTER";
        if (monitor!=null) monitor.subTask("Checking table "+tablename);
        table.addValueMeta(new ValueMeta("ID_TRANS_CLUSTER", ValueMetaInterface.TYPE_INTEGER, KEY, 0));
        table.addValueMeta(new ValueMeta("ID_TRANSFORMATION", ValueMetaInterface.TYPE_INTEGER, KEY, 0));
        table.addValueMeta(new ValueMeta("ID_CLUSTER", ValueMetaInterface.TYPE_INTEGER, KEY, 0));
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
        table = new RowMeta();
        tablename = "R_TRANS_SLAVE";
        if (monitor!=null) monitor.subTask("Checking table "+tablename);
        table.addValueMeta(new ValueMeta("ID_TRANS_SLAVE", ValueMetaInterface.TYPE_INTEGER, KEY, 0));
        table.addValueMeta(new ValueMeta("ID_TRANSFORMATION", ValueMetaInterface.TYPE_INTEGER, KEY, 0));
        table.addValueMeta(new ValueMeta("ID_SLAVE", ValueMetaInterface.TYPE_INTEGER, KEY, 0));
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
		table = new RowMeta();
		tablename = "R_TRANS_HOP";
		if (monitor!=null) monitor.subTask("Checking table "+tablename);
		table.addValueMeta(new ValueMeta("ID_TRANS_HOP", ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta("ID_TRANSFORMATION", ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta("ID_STEP_FROM", ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta("ID_STEP_TO", ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta("ENABLED", ValueMetaInterface.TYPE_BOOLEAN, 1, 0));
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
		table = new RowMeta();
		tablename = "R_TRANS_STEP_CONDITION";
		if (monitor!=null) monitor.subTask("Checking table "+tablename);
		table.addValueMeta(new ValueMeta("ID_TRANSFORMATION", ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta("ID_STEP", ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta("ID_CONDITION", ValueMetaInterface.TYPE_INTEGER, KEY, 0));
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
		table = new RowMeta();
		tablename = "R_CONDITION";
		if (monitor!=null) monitor.subTask("Checking table "+tablename);
		table.addValueMeta(new ValueMeta("ID_CONDITION", ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta("ID_CONDITION_PARENT", ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta("NEGATED", ValueMetaInterface.TYPE_BOOLEAN, 1, 0));
		table.addValueMeta(new ValueMeta("OPERATOR", ValueMetaInterface.TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
		table.addValueMeta(new ValueMeta("LEFT_NAME", ValueMetaInterface.TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
		table.addValueMeta(new ValueMeta("CONDITION_FUNCTION", ValueMetaInterface.TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
		table.addValueMeta(new ValueMeta("RIGHT_NAME", ValueMetaInterface.TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
		table.addValueMeta(new ValueMeta("ID_VALUE_RIGHT", ValueMetaInterface.TYPE_INTEGER, KEY, 0));
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
		table = new RowMeta();
		table.addValueMeta(new ValueMeta("ID_VALUE", ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta("NAME", ValueMetaInterface.TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
		table.addValueMeta(new ValueMeta("VALUE_TYPE", ValueMetaInterface.TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
		table.addValueMeta(new ValueMeta("VALUE_STR", ValueMetaInterface.TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
		table.addValueMeta(new ValueMeta("IS_NULL", ValueMetaInterface.TYPE_BOOLEAN, 1, 0));
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
		table = new RowMeta();
		tablename = "R_STEP_TYPE";
		if (monitor!=null) monitor.subTask("Checking table "+tablename);
		table.addValueMeta(new ValueMeta("ID_STEP_TYPE", ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta("CODE", ValueMetaInterface.TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
		table.addValueMeta(new ValueMeta("DESCRIPTION", ValueMetaInterface.TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
		table.addValueMeta(new ValueMeta("HELPTEXT", ValueMetaInterface.TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
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
		table = new RowMeta();
		tablename = "R_STEP";
		if (monitor!=null) monitor.subTask("Checking table "+tablename);
		table.addValueMeta(new ValueMeta("ID_STEP", ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta("ID_TRANSFORMATION", ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta("NAME", ValueMetaInterface.TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
		table.addValueMeta(new ValueMeta("DESCRIPTION", ValueMetaInterface.TYPE_STRING, REP_STRING_LENGTH, 0));
		table.addValueMeta(new ValueMeta("ID_STEP_TYPE", ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta("DISTRIBUTE", ValueMetaInterface.TYPE_BOOLEAN, 1, 0));
		table.addValueMeta(new ValueMeta("COPIES", ValueMetaInterface.TYPE_INTEGER, 3, 0));
		table.addValueMeta(new ValueMeta("GUI_LOCATION_X", ValueMetaInterface.TYPE_INTEGER, 6, 0));
		table.addValueMeta(new ValueMeta("GUI_LOCATION_Y", ValueMetaInterface.TYPE_INTEGER, 6, 0));
		table.addValueMeta(new ValueMeta("GUI_DRAW", ValueMetaInterface.TYPE_BOOLEAN, 1, 0));
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
		table = new RowMeta();
		table.addValueMeta(new ValueMeta("ID_STEP_ATTRIBUTE", ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta("ID_TRANSFORMATION", ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta("ID_STEP", ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta("NR", ValueMetaInterface.TYPE_INTEGER, 6, 0));
		table.addValueMeta(new ValueMeta("CODE", ValueMetaInterface.TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
		table.addValueMeta(new ValueMeta("VALUE_NUM", ValueMetaInterface.TYPE_INTEGER, 18, 0));
		table.addValueMeta(new ValueMeta("VALUE_STR", ValueMetaInterface.TYPE_STRING, REP_STRING_LENGTH, 0));
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
			catch(KettleException kdbe)
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
		table = new RowMeta();
		table.addValueMeta(new ValueMeta("ID_TRANSFORMATION", ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta("ID_STEP", ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta("ID_DATABASE", ValueMetaInterface.TYPE_INTEGER, KEY, 0));
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
			catch(KettleException kdbe)
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
			catch(KettleException kdbe)
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
		table = new RowMeta();
		tablename = "R_TRANS_NOTE";
		if (monitor!=null) monitor.subTask("Checking table "+tablename);
		table.addValueMeta(new ValueMeta("ID_TRANSFORMATION", ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta("ID_NOTE", ValueMetaInterface.TYPE_INTEGER, KEY, 0));
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
		table = new RowMeta();
		table.addValueMeta(new ValueMeta("ID_LOGLEVEL", ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta("CODE", ValueMetaInterface.TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
		table.addValueMeta(new ValueMeta("DESCRIPTION", ValueMetaInterface.TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
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
                RowMetaAndData lookup = null;
                if (upgrade) lookup = database.getOneRow("SELECT ID_LOGLEVEL FROM " + tablename + " WHERE " 
                		+ database.getDatabaseMeta().quoteField("CODE") + " = '" + code[i] + "'");
				if (lookup == null)
				{
					long nextid = getNextLoglevelID();

					RowMetaAndData tableData = new RowMetaAndData();
                    tableData.addValue(new ValueMeta("ID_LOGLEVEL", ValueMetaInterface.TYPE_INTEGER), new Long(nextid));
                    tableData.addValue(new ValueMeta("CODE", ValueMetaInterface.TYPE_STRING), code[i]);
                    tableData.addValue(new ValueMeta("DESCRIPTION", ValueMetaInterface.TYPE_STRING), desc[i]);

					database.setValuesInsert(tableData.getRowMeta(), tableData.getData());
					database.insertRow();
				}
			}
            
            try
            {
                database.closeInsert();
                if (log.isDetailed()) log.logDetailed(toString(), "Populated table " + tablename);
            }
            catch(KettleException dbe)
            {
                throw new KettleException("Unable to close insert after populating table " + tablename, dbe);
            }
		}
		if (monitor!=null) monitor.worked(1);

		//////////////////////////////////////////////////////////////////////////////////
		//
		// R_LOG
		//
		// Create table...
		table = new RowMeta();
		tablename = "R_LOG";
		if (monitor!=null) monitor.subTask("Checking table "+tablename);
		table.addValueMeta(new ValueMeta("ID_LOG", ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta("NAME", ValueMetaInterface.TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
		table.addValueMeta(new ValueMeta("ID_LOGLEVEL", ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta("LOGTYPE", ValueMetaInterface.TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
		table.addValueMeta(new ValueMeta("FILENAME", ValueMetaInterface.TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
		table.addValueMeta(new ValueMeta("FILEEXTENTION", ValueMetaInterface.TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
		table.addValueMeta(new ValueMeta("ADD_DATE", ValueMetaInterface.TYPE_BOOLEAN, 1, 0));
		table.addValueMeta(new ValueMeta("ADD_TIME", ValueMetaInterface.TYPE_BOOLEAN, 1, 0));
		table.addValueMeta(new ValueMeta("ID_DATABASE_LOG", ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta("TABLE_NAME_LOG", ValueMetaInterface.TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
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
		table = new RowMeta();
		tablename = "R_JOB";
		if (monitor!=null) monitor.subTask("Checking table "+tablename);
		table.addValueMeta(new ValueMeta("ID_JOB", ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta("ID_DIRECTORY", ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta("NAME", ValueMetaInterface.TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
        table.addValueMeta(new ValueMeta("DESCRIPTION", ValueMetaInterface.TYPE_STRING, REP_STRING_LENGTH, 0));
        table.addValueMeta(new ValueMeta("EXTENDED_DESCRIPTION", ValueMetaInterface.TYPE_STRING, REP_STRING_LENGTH, 0));
        table.addValueMeta(new ValueMeta("JOB_VERSION", ValueMetaInterface.TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
        table.addValueMeta(new ValueMeta("JOB_STATUS", ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta("ID_DATABASE_LOG", ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta("TABLE_NAME_LOG", ValueMetaInterface.TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
        table.addValueMeta(new ValueMeta("CREATED_USER", ValueMetaInterface.TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
        table.addValueMeta(new ValueMeta("CREATED_DATE", ValueMetaInterface.TYPE_DATE, 20, 0));
		table.addValueMeta(new ValueMeta("MODIFIED_USER", ValueMetaInterface.TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
		table.addValueMeta(new ValueMeta("MODIFIED_DATE", ValueMetaInterface.TYPE_DATE, 20, 0));
        table.addValueMeta(new ValueMeta("USE_BATCH_ID", ValueMetaInterface.TYPE_BOOLEAN, 0, 0));
        table.addValueMeta(new ValueMeta("PASS_BATCH_ID", ValueMetaInterface.TYPE_BOOLEAN, 0, 0));
        table.addValueMeta(new ValueMeta("USE_LOGFIELD", ValueMetaInterface.TYPE_BOOLEAN, 0, 0));
        table.addValueMeta(new ValueMeta("SHARED_FILE", ValueMetaInterface.TYPE_STRING, REP_STRING_CODE_LENGTH, 0)); // 255 max length for now.
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
		table = new RowMeta();
		tablename = "R_JOBENTRY_TYPE";
		if (monitor!=null) monitor.subTask("Checking table "+tablename);
		table.addValueMeta(new ValueMeta("ID_JOBENTRY_TYPE", ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta("CODE", ValueMetaInterface.TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
		table.addValueMeta(new ValueMeta("DESCRIPTION", ValueMetaInterface.TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
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
		table = new RowMeta();
		tablename = "R_JOBENTRY";
		if (monitor!=null) monitor.subTask("Checking table "+tablename);
		table.addValueMeta(new ValueMeta("ID_JOBENTRY", ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta("ID_JOB", ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta("ID_JOBENTRY_TYPE", ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta("NAME", ValueMetaInterface.TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
		table.addValueMeta(new ValueMeta("DESCRIPTION", ValueMetaInterface.TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
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
		table = new RowMeta();
		tablename = "R_JOBENTRY_COPY";
		if (monitor!=null) monitor.subTask("Checking table "+tablename);
		table.addValueMeta(new ValueMeta("ID_JOBENTRY_COPY", ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta("ID_JOBENTRY", ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta("ID_JOB", ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta("ID_JOBENTRY_TYPE", ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta("NR", ValueMetaInterface.TYPE_INTEGER, 4, 0));
		table.addValueMeta(new ValueMeta("GUI_LOCATION_X", ValueMetaInterface.TYPE_INTEGER, 6, 0));
		table.addValueMeta(new ValueMeta("GUI_LOCATION_Y", ValueMetaInterface.TYPE_INTEGER, 6, 0));
		table.addValueMeta(new ValueMeta("GUI_DRAW", ValueMetaInterface.TYPE_BOOLEAN, 1, 0));
		table.addValueMeta(new ValueMeta("PARALLEL", ValueMetaInterface.TYPE_BOOLEAN, 1, 0));
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
		table = new RowMeta();
		tablename = "R_JOBENTRY_ATTRIBUTE";
		if (monitor!=null) monitor.subTask("Checking table "+tablename);
		table.addValueMeta(new ValueMeta("ID_JOBENTRY_ATTRIBUTE", ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta("ID_JOB", ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta("ID_JOBENTRY", ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta("NR", ValueMetaInterface.TYPE_INTEGER, 6, 0));
		table.addValueMeta(new ValueMeta("CODE", ValueMetaInterface.TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
		table.addValueMeta(new ValueMeta("VALUE_NUM", ValueMetaInterface.TYPE_NUMBER, 13, 2));
		table.addValueMeta(new ValueMeta("VALUE_STR", ValueMetaInterface.TYPE_STRING, REP_STRING_LENGTH, 0));
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
			catch(KettleException kdbe)
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
		table = new RowMeta();
		tablename = "R_JOB_HOP";
		if (monitor!=null) monitor.subTask("Checking table "+tablename);
		table.addValueMeta(new ValueMeta("ID_JOB_HOP", ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta("ID_JOB", ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta("ID_JOBENTRY_COPY_FROM", ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta("ID_JOBENTRY_COPY_TO", ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta("ENABLED", ValueMetaInterface.TYPE_BOOLEAN, 1, 0));
		table.addValueMeta(new ValueMeta("EVALUATION", ValueMetaInterface.TYPE_BOOLEAN, 1, 0));
		table.addValueMeta(new ValueMeta("UNCONDITIONAL", ValueMetaInterface.TYPE_BOOLEAN, 1, 0));
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
		table = new RowMeta();
		tablename = "R_JOB_NOTE";
		if (monitor!=null) monitor.subTask("Checking table "+tablename);
		table.addValueMeta(new ValueMeta("ID_JOB", ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta("ID_NOTE", ValueMetaInterface.TYPE_INTEGER, KEY, 0));
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
        Map<String, Long> profiles = new Hashtable<String, Long>();
        
		boolean ok_profile = true;
		tablename = "R_PROFILE";
		if (monitor!=null) monitor.subTask("Checking table "+tablename);
		table = new RowMeta();
		table.addValueMeta(new ValueMeta("ID_PROFILE", ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta("NAME", ValueMetaInterface.TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
		table.addValueMeta(new ValueMeta("DESCRIPTION", ValueMetaInterface.TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
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
                RowMetaAndData lookup = null;
                if (upgrade) lookup = database.getOneRow("SELECT ID_PROFILE FROM " + tablename + " WHERE "
                		+ database.getDatabaseMeta().quoteField("NAME") + " = '" + code[i] + "'");
				if (lookup == null)
				{
					long nextid = getNextProfileID();

					RowMetaAndData tableData = new RowMetaAndData();
                    tableData.addValue(new ValueMeta("ID_PROFILE", ValueMetaInterface.TYPE_INTEGER), new Long(nextid));
                    tableData.addValue(new ValueMeta("NAME", ValueMetaInterface.TYPE_STRING), code[i]);
                    tableData.addValue(new ValueMeta("DESCRIPTION", ValueMetaInterface.TYPE_STRING), desc[i]);

					database.setValuesInsert(tableData);
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
            catch(KettleException dbe)
            {
                throw new KettleException("Unable to close insert after populating table " + tablename, dbe);
            }
		}
		if (monitor!=null) monitor.worked(1);

		//////////////////////////////////////////////////////////////////////////////////
		//
		// R_USER
		//
		// Create table...
        Map<String, Long> users = new Hashtable<String, Long>();
		boolean ok_user = true;
		table = new RowMeta();
		tablename = "R_USER";
		if (monitor!=null) monitor.subTask("Checking table "+tablename);
		table.addValueMeta(new ValueMeta("ID_USER", ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta("ID_PROFILE", ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta("LOGIN", ValueMetaInterface.TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
		table.addValueMeta(new ValueMeta("PASSWORD", ValueMetaInterface.TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
		table.addValueMeta(new ValueMeta("NAME", ValueMetaInterface.TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
		table.addValueMeta(new ValueMeta("DESCRIPTION", ValueMetaInterface.TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
		table.addValueMeta(new ValueMeta("ENABLED", ValueMetaInterface.TYPE_BOOLEAN, 1, 0));
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
                RowMetaAndData lookup = null;
                if (upgrade) lookup = database.getOneRow("SELECT ID_USER FROM " + tablename + " WHERE "
                		+ database.getDatabaseMeta().quoteField("LOGIN") + " = '" + user[i] + "'");
				if (lookup == null)
				{
					long nextid = getNextUserID();
					String password = Encr.encryptPassword(pass[i]);
                    
                    Long profileID = (Long)profiles.get( prof[i] );
                    long id_profile = -1L;
                    if (profileID!=null) id_profile = profileID.longValue();
                    
					RowMetaAndData tableData = new RowMetaAndData();
                    tableData.addValue(new ValueMeta("ID_USER", ValueMetaInterface.TYPE_INTEGER), new Long(nextid));
                    tableData.addValue(new ValueMeta("ID_PROFILE", ValueMetaInterface.TYPE_INTEGER), new Long(id_profile));
                    tableData.addValue(new ValueMeta("LOGIN", ValueMetaInterface.TYPE_STRING), user[i]);
                    tableData.addValue(new ValueMeta("PASSWORD", ValueMetaInterface.TYPE_STRING), password);
                    tableData.addValue(new ValueMeta("NAME", ValueMetaInterface.TYPE_STRING), code[i]);
                    tableData.addValue(new ValueMeta("DESCRIPTION", ValueMetaInterface.TYPE_STRING), desc[i]);
                    tableData.addValue(new ValueMeta("ENABLED", ValueMetaInterface.TYPE_BOOLEAN), new Boolean(true));

					database.setValuesInsert(tableData);
					database.insertRow();
                    users.put(user[i], new Long(nextid));
				}
			}
            
            try
            {
                database.closeInsert();
                if (log.isDetailed()) log.logDetailed(toString(), "Populated table " + tablename);
            }
            catch(KettleException dbe)
            {
                throw new KettleException("Unable to close insert after populating table " + tablename, dbe);
            }
		}
		if (monitor!=null) monitor.worked(1);

		//////////////////////////////////////////////////////////////////////////////////
		//
		// R_PERMISSION
		//
		// Create table...
        Map<String, Long> permissions = new Hashtable<String, Long>();
		boolean ok_permission = true;
		table = new RowMeta();
		tablename = "R_PERMISSION";
		if (monitor!=null) monitor.subTask("Checking table "+tablename);
		table.addValueMeta(new ValueMeta("ID_PERMISSION", ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta("CODE", ValueMetaInterface.TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
		table.addValueMeta(new ValueMeta("DESCRIPTION", ValueMetaInterface.TYPE_STRING, REP_STRING_CODE_LENGTH, 0));
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
                RowMetaAndData lookup = null;
                if (upgrade) lookup = database.getOneRow("SELECT ID_PERMISSION FROM " + tablename + " WHERE " 
                		+ database.getDatabaseMeta().quoteField("CODE") + " = '" + code[i] + "'");
				if (lookup == null)
				{
					long nextid = getNextPermissionID();

                    RowMetaAndData tableData = new RowMetaAndData();
                    tableData.addValue(new ValueMeta("ID_PERMISSION", ValueMetaInterface.TYPE_INTEGER), new Long(nextid));
                    tableData.addValue(new ValueMeta("CODE", ValueMetaInterface.TYPE_STRING), code[i]);
                    tableData.addValue(new ValueMeta("DESCRIPTION", ValueMetaInterface.TYPE_STRING), desc[i]);

					database.setValuesInsert(tableData);
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
            catch(KettleException dbe)
            {
                throw new KettleException("Unable to close insert after populating table " + tablename, dbe);
            }
		}
		if (monitor!=null) monitor.worked(1);

		//////////////////////////////////////////////////////////////////////////////////
		//
		// R_PROFILE_PERMISSION
		//
		// Create table...
		boolean ok_profile_permission = true;
		table = new RowMeta();
		tablename = "R_PROFILE_PERMISSION";
		if (monitor!=null) monitor.subTask("Checking table "+tablename);
		table.addValueMeta(new ValueMeta("ID_PROFILE", ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta("ID_PERMISSION", ValueMetaInterface.TYPE_INTEGER, KEY, 0));
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
			catch(KettleException kdbe)
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

				RowMetaAndData lookup = null;
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

                RowMetaAndData lookup = null;
                if (upgrade) lookup = database.getOneRow("SELECT ID_PROFILE FROM " + tablename + " WHERE ID_PROFILE=" + id_profile + " AND ID_PERMISSION=" + id_permission);
				if (lookup == null) // if the combination is not yet there, insert...
				{
					RowMetaAndData tableData = new RowMetaAndData();
                    tableData.addValue(new ValueMeta("ID_PROFILE", ValueMetaInterface.TYPE_INTEGER), new Long(id_profile));
                    tableData.addValue(new ValueMeta("ID_PERMISSION", ValueMetaInterface.TYPE_INTEGER), new Long(id_permission));

					database.setValuesInsert(tableData);
					database.insertRow();
				}
			}

            try
            {
                database.closeInsert();
                if (log.isDetailed()) log.logDetailed(toString(), "Populated table " + tablename);
            }
            catch(KettleException dbe)
            {
                throw new KettleException("Unable to close insert after populating table " + tablename, dbe);
            }
		}
        
		if (monitor!=null) monitor.worked(1);
		if (monitor!=null) monitor.done();
        
        log.logBasic(toString(), (upgrade?"Upgraded":"Created")+ " "+repositoryTableNames.length+" repository tables.");

	}

	public boolean dropRepositorySchema() throws KettleException
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
			catch (KettleException dbe)
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
	 * @throws KettleException if the update didn't go as planned.
	 */
	public synchronized void updateStepTypes() throws KettleException
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

				RowMetaAndData table = new RowMetaAndData();
				table.addValue(new ValueMeta("ID_STEP_TYPE", ValueMetaInterface.TYPE_INTEGER), new Long(id));
				table.addValue(new ValueMeta("CODE", ValueMetaInterface.TYPE_STRING), sp.getID()[0]);
				table.addValue(new ValueMeta("DESCRIPTION", ValueMetaInterface.TYPE_STRING), sp.getDescription());
				table.addValue(new ValueMeta("HELPTEXT", ValueMetaInterface.TYPE_STRING), sp.getTooltip());

				database.prepareInsert(table.getRowMeta(), "R_STEP_TYPE");

				database.setValuesInsert(table);
				database.insertRow();
				database.closeInsert();
			}
		}
	}
	
	
	/**
	 * Update the list in R_JOBENTRY_TYPE 
	 * 
	 * @exception KettleException if something went wrong during the update.
	 */
	public synchronized void updateJobEntryTypes() throws KettleException
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

                RowMetaAndData table = new RowMetaAndData();
                table.addValue(new ValueMeta("ID_JOBENTRY_TYPE", ValueMetaInterface.TYPE_INTEGER), new Long(id));
                table.addValue(new ValueMeta("CODE", ValueMetaInterface.TYPE_STRING), type_desc);
                table.addValue(new ValueMeta("DESCRIPTION", ValueMetaInterface.TYPE_STRING), type_desc_long);

                database.prepareInsert(table.getRowMeta(), "R_JOBENTRY_TYPE");

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
    
    public synchronized void lockRepository() throws KettleException
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
    
    public synchronized void unlockRepository() throws KettleException
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

    private String exportJobs(IProgressMonitor monitor, RepositoryDirectory dirTree) throws KettleException
    {
        StringBuffer xml = new StringBuffer();
        
        /*
         * TODO: re-enable job export from repository ...
         * 
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
         */

        return xml.toString();
    }

    private String exportTransformations(IProgressMonitor monitor, RepositoryDirectory dirTree) throws KettleException
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
    public List<DatabaseMeta> getDatabases() throws KettleException
    {
        List<DatabaseMeta> list = new ArrayList<DatabaseMeta>();
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
    public List<SlaveServer> getSlaveServers() throws KettleException
    {
        List<SlaveServer> list = new ArrayList<SlaveServer>();
        long[] slaveIDs = getSlaveIDs();
        for (int i=0;i<slaveIDs.length;i++)
        {
            SlaveServer slaveServer = new SlaveServer(this, slaveIDs[i]);
            list.add(slaveServer);
        }
            
        return list;
    }

	/**
	 * @return the stepAttributesRowMeta
	 */
	public RowMetaInterface getStepAttributesRowMeta() {
		return stepAttributesRowMeta;
	}

	/**
	 * @param stepAttributesRowMeta the stepAttributesRowMeta to set
	 */
	public void setStepAttributesRowMeta(RowMetaInterface stepAttributesRowMeta) {
		this.stepAttributesRowMeta = stepAttributesRowMeta;
	}
}