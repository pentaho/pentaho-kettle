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

package org.pentaho.di.trans.steps.dimensionlookup;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Counter;
import org.pentaho.di.core.SQLStatement;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.shared.SharedObjectInterface;
import org.pentaho.di.trans.DatabaseImpact;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.w3c.dom.Node;


/**
 * @author Matt
 * @since on 14-may-2003
 *  
 * WANTED: Interconnected Dynamic Lookups --> http://www.datawarehouse.com/article/?articleId=5354
 * 
 *    The idea is here to create a central 'dimension' cache process, seperated from the other Kettle processes.
 *    Kettle then connects over a socket to this daemon-like process to check wether a certain dimension entry is present.
 *    Perhaps a more general caching service should be considered.
 *  
 */

public class DimensionLookupMeta extends BaseStepMeta implements StepMetaInterface
{
	public final static int		TYPE_UPDATE_DIM_INSERT			= 0;
	public final static int		TYPE_UPDATE_DIM_UPDATE			= 1;
	public final static int		TYPE_UPDATE_DIM_PUNCHTHROUGH	= 2;
	public final static int		TYPE_UPDATE_DATE_INSUP		    = 3;
	public final static int		TYPE_UPDATE_DATE_INSERTED		= 4;
	public final static int		TYPE_UPDATE_DATE_UPDATED		= 5;
	public final static int		TYPE_UPDATE_LAST_VERSION		= 6;

	public final static String	typeDesc[]						= { 
		Messages.getString("DimensionLookupMeta.TypeDesc.Insert"),               //$NON-NLS-1$ 
		Messages.getString("DimensionLookupMeta.TypeDesc.Update"),               //$NON-NLS-1$
		Messages.getString("DimensionLookupMeta.TypeDesc.PunchThrough"),         //$NON-NLS-1$
		Messages.getString("DimensionLookupMeta.TypeDesc.DateInsertedOrUpdated"),         //$NON-NLS-1$
		Messages.getString("DimensionLookupMeta.TypeDesc.DateInserted"),         //$NON-NLS-1$
		Messages.getString("DimensionLookupMeta.TypeDesc.DateUpdated"),          //$NON-NLS-1$
		Messages.getString("DimensionLookupMeta.TypeDesc.LastVersion"),          //$NON-NLS-1$
		};
	
	public final static String	typeCodes[]						= { // for saving to the repository
		"Insert",               //$NON-NLS-1$ 
		"Update",               //$NON-NLS-1$
		"Punch through",         //$NON-NLS-1$
		"DateInsertedOrUpdated",         //$NON-NLS-1$
		"DateInserted",         //$NON-NLS-1$
		"DateUpdated",          //$NON-NLS-1$
		"LastVersion",          //$NON-NLS-1$
		};

	public final static String	typeDescLookup[]				= ValueMeta.getTypes();

    public static final int     START_DATE_ALTERNATIVE_NONE           = 0;
    public static final int     START_DATE_ALTERNATIVE_SYSDATE        = 1;
    public static final int     START_DATE_ALTERNATIVE_START_OF_TRANS = 2;
    public static final int     START_DATE_ALTERNATIVE_NULL           = 3;
    public static final int     START_DATE_ALTERNATIVE_COLUMN_VALUE   = 4;
    
    private static final String[] startDateAlternativeCodes = { 
    		"none",           // $NON-NLS-1$ 
    		"sysdate",        // $NON-NLS-1$ 
    		"trans_start",    // $NON-NLS-1$ 
    		"null",           // $NON-NLS-1$ 
    		"column_value",   // $NON-NLS-1$
    	};
    
    private static final String[] startDateAlternativeDescs = { 
    		Messages.getString("DimensionLookupMeta.StartDateAlternative.None.Label"),
    		Messages.getString("DimensionLookupMeta.StartDateAlternative.Sysdate.Label"),
    		Messages.getString("DimensionLookupMeta.StartDateAlternative.TransStart.Label"),
    		Messages.getString("DimensionLookupMeta.StartDateAlternative.Null.Label"),
    		Messages.getString("DimensionLookupMeta.StartDateAlternative.ColumnValue.Label"),
    	};
    
    /** The lookup schema name*/
    private String             schemaName;

	/** The lookup table*/
	private String             tableName;

	/** The database connection */
	private DatabaseMeta       databaseMeta;

	/** Update the dimension or just lookup? */
    private boolean             update;
    
    /** Fields used to look up a value in the dimension */
    private String              keyStream[];

    /** Fields in the dimension to use for lookup */
    private String              keyLookup[];

    /** The field to use for date range lookup in the dimension */
    private String              dateField;

    /** The 'from' field of the date range in the dimension */
    private String              dateFrom;

    /** The 'to' field of the date range in the dimension */
    private String              dateTo;

    /** Fields containing the values in the input stream to update the dimension with */
    private String              fieldStream[];

    /** Fields in the dimension to update or retrieve */
    private String              fieldLookup[];

    /** The type of update to perform on the fields: insert, update, punch-through */
    private int                 fieldUpdate[];

    /** Name of the technical key (surrogate key) field to return from the dimension */
    private String              keyField;

    /** New name of the technical key field */
    private String              keyRename;
    
    /** Use auto increment field as TK */
    private boolean             autoIncrement;

    /** The name of the version field */
    private String              versionField;

    /** Sequence name to get the sequence from */
    private String              sequenceName;

    /** The number of rows between commits */
    private int                 commitSize;

    /** The year to use as minus infinity in the dimensions date range */
    private int                 minYear; 
    
    /** The year to use as plus infinity in the dimensions date range */
    private int                 maxYear;
    
	/** Which method to use for the creation of the tech key */
	private String techKeyCreation = null;
	
	public static String CREATION_METHOD_AUTOINC  = "autoinc";
	public static String CREATION_METHOD_SEQUENCE = "sequence";
	public static String CREATION_METHOD_TABLEMAX = "tablemax";    
    
    /** The size of the cache in ROWS : -1 means: not set, 0 means: cache all */
    private int                 cacheSize;
    
    /** Flag to indicate we're going to use an alternative start date */
    private boolean             usingStartDateAlternative;
    
    /** The type of alternative */
    private int                 startDateAlternative;
    
    /** The field name in case we select the column value option as an alternative start date */
    private String              startDateFieldName;
    
    private boolean             preloadingCache;
    
	public DimensionLookupMeta()
	{
		super(); // allocate BaseStepMeta
	}

	/**
     * @return Returns the tablename.
     */
	public String getTableName()
	{
		return tableName;
	}

	/**
     * @param tablename The tablename to set.
     */
	public void setTableName(String tablename)
	{
		this.tableName = tablename;
	}

	/**
     * @return Returns the database.
     */
	public DatabaseMeta getDatabaseMeta()
	{
		return databaseMeta;
	}

	/**
     * @param database
     *            The database to set.
     */
	public void setDatabaseMeta(DatabaseMeta database)
	{
		this.databaseMeta = database;
	}

	/**
     * @return Returns the update.
     */
	public boolean isUpdate()
	{
		return update;
	}

	/**
	 * @param update
	 *            The update to set.
	 */
	public void setUpdate(boolean update)
	{
		this.update = update;
	}

    /**
     * @return Returns the autoIncrement.
     */
    public boolean isAutoIncrement()
    {
        return autoIncrement;
    }
    
    /**
     * @param autoIncrement The autoIncrement to set.
     */
    public void setAutoIncrement(boolean autoIncrement)
    {
        this.autoIncrement = autoIncrement;
    }

    
    
	/**
	 * Set the way how the technical key field should be created.
	 *
	 * @param techKeyCreation which method to use for the creation
	 *                        of the technical key.
	 */
    public void setTechKeyCreation(String techKeyCreation)
    {
   		this.techKeyCreation = techKeyCreation;
    }

	/**
	 * Get the way how the technical key field should be created.
	 *
	 * @return creation way for the technical key.
	 */
    public String getTechKeyCreation()
    {
        return this.techKeyCreation;
    }
        
    /**
     * @return Returns the commitSize.
     */
    public int getCommitSize()
    {
        return commitSize;
    }
    
    /**
     * @param commitSize The commitSize to set.
     */
    public void setCommitSize(int commitSize)
    {
        this.commitSize = commitSize;
    }
    
    /**
     * @return Returns the dateField.
     */
    public String getDateField()
    {
        return dateField;
    }
    
    /**
     * @param dateField The dateField to set.
     */
    public void setDateField(String dateField)
    {
        this.dateField = dateField;
    }
    
    /**
     * @return Returns the dateFrom.
     */
    public String getDateFrom()
    {
        return dateFrom;
    }
    
    /**
     * @param dateFrom The dateFrom to set.
     */
    public void setDateFrom(String dateFrom)
    {
        this.dateFrom = dateFrom;
    }
    
    /**
     * @return Returns the dateTo.
     */
    public String getDateTo()
    {
        return dateTo;
    }
    
    /**
     * @param dateTo The dateTo to set.
     */
    public void setDateTo(String dateTo)
    {
        this.dateTo = dateTo;
    }
    
    /**
     * @return Fields in the dimension to update or retrieve.
     */
    public String[] getFieldLookup()
    {
        return fieldLookup;
    }
    
    /**
     * @param fieldLookup sets the fields in the dimension to update or retrieve.
     */
    public void setFieldLookup(String[] fieldLookup)
    {
        this.fieldLookup = fieldLookup;
    }
    
    /**
     * @return Fields containing the values in the input stream to update the dimension with.
     */
    public String[] getFieldStream()
    {
        return fieldStream;
    }
    
    /**
     * @param fieldStream The fields containing the values in the input stream to update the dimension with.
     */
    public void setFieldStream(String[] fieldStream)
    {
        this.fieldStream = fieldStream;
    }
    
    /**
     * @return Returns the fieldUpdate.
     */
    public int[] getFieldUpdate()
    {
        return fieldUpdate;
    }
    
    /**
     * @param fieldUpdate The fieldUpdate to set.
     */
    public void setFieldUpdate(int[] fieldUpdate)
    {
        this.fieldUpdate = fieldUpdate;
    }
    
    /**
     * @return Returns the keyField.
     */
    public String getKeyField()
    {
        return keyField;
    }
    
    /**
     * @param keyField The keyField to set.
     */
    public void setKeyField(String keyField)
    {
        this.keyField = keyField;
    }
    
    /**
     * @return Returns the keyLookup.
     */
    public String[] getKeyLookup()
    {
        return keyLookup;
    }
    
    /**
     * @param keyLookup The keyLookup to set.
     */
    public void setKeyLookup(String[] keyLookup)
    {
        this.keyLookup = keyLookup;
    }
    
    /**
     * @return Returns the keyRename.
     */
    public String getKeyRename()
    {
        return keyRename;
    }
    
    /**
     * @param keyRename The keyRename to set.
     */
    public void setKeyRename(String keyRename)
    {
        this.keyRename = keyRename;
    }
    
    /**
     * @return Returns the keyStream.
     */
    public String[] getKeyStream()
    {
        return keyStream;
    }
    
    /**
     * @param keyStream The keyStream to set.
     */
    public void setKeyStream(String[] keyStream)
    {
        this.keyStream = keyStream;
    }
    
    /**
     * @return Returns the maxYear.
     */
    public int getMaxYear()
    {
        return maxYear;
    }
    
    /**
     * @param maxYear The maxYear to set.
     */
    public void setMaxYear(int maxYear)
    {
        this.maxYear = maxYear;
    }
    
    /**
     * @return Returns the minYear.
     */
    public int getMinYear()
    {
        return minYear;
    }
    
    /**
     * @param minYear The minYear to set.
     */
    public void setMinYear(int minYear)
    {
        this.minYear = minYear;
    }
        
    /**
     * @return Returns the sequenceName.
     */
    public String getSequenceName()
    {
        return sequenceName;
    }
    
    /**
     * @param sequenceName The sequenceName to set.
     */
    public void setSequenceName(String sequenceName)
    {
        this.sequenceName = sequenceName;
    }
    
    /**
     * @return Returns the versionField.
     */
    public String getVersionField()
    {
        return versionField;
    }
    
    /**
     * @param versionField The versionField to set.
     */
    public void setVersionField(String versionField)
    {
        this.versionField = versionField;
    }
    
    
    
	public void loadXML(Node stepnode, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleXMLException
	{
		readData(stepnode, databases);
	}

	public void allocate(int nrkeys, int nrfields)
	{
		keyStream = new String[nrkeys];
		keyLookup = new String[nrkeys];

		fieldStream = new String[nrfields];
		fieldLookup = new String[nrfields];
		fieldUpdate = new int[nrfields];
	}

	public Object clone()
	{
		DimensionLookupMeta retval = (DimensionLookupMeta) super.clone();

		int nrkeys = keyStream.length;
		int nrfields = fieldStream.length;

		retval.allocate(nrkeys, nrfields);

		for (int i = 0; i < nrkeys; i++)
		{
			retval.keyStream[i] = keyStream[i];
			retval.keyLookup[i] = keyLookup[i];
		}

		for (int i = 0; i < nrfields; i++)
		{
			retval.fieldStream[i] = fieldStream[i];
			retval.fieldLookup[i] = fieldLookup[i];
			retval.fieldUpdate[i] = fieldUpdate[i];
		}

		return retval;
	}

	public final static int getUpdateType(boolean upd, String ty)
	{
		if (upd)
		{
			for (int i = 0; i < typeCodes.length; i++)
			{
				if (typeCodes[i].equalsIgnoreCase(ty))
					return i;
			}
			// for compatibility:
			for (int i = 0; i < typeDesc.length; i++)
			{
				if (typeDesc[i].equalsIgnoreCase(ty))
					return i;
			}
			if ("Y".equalsIgnoreCase(ty)) //$NON-NLS-1$
				return TYPE_UPDATE_DIM_PUNCHTHROUGH;
			return TYPE_UPDATE_DIM_INSERT; // INSERT is the default: don't lose information.
		}
		else
		{
			int retval = ValueMeta.getType(ty);
			if (retval == ValueMetaInterface.TYPE_NONE)
				retval = ValueMetaInterface.TYPE_STRING;
			return retval;
		}
	}

	public final static String getUpdateType(boolean upd, int t)
	{
		if (!upd)
			return ValueMeta.getTypeDesc(t);
		else
			return typeDesc[t];
	}

	public final static String getUpdateTypeCode(boolean upd, int t)
	{
		if (!upd)
			return ValueMeta.getTypeDesc(t);
		else
			return typeCodes[t];
	}

	public final static int getStartDateAlternative(String string)
	{
		for (int i = 0; i < startDateAlternativeCodes.length; i++)
		{
			if (startDateAlternativeCodes[i].equalsIgnoreCase(string)) return i;
		}
		for (int i = 0; i < startDateAlternativeDescs.length; i++)
		{
			if (startDateAlternativeDescs[i].equalsIgnoreCase(string)) return i;
		}
		return START_DATE_ALTERNATIVE_NONE;
	}

	public final static String getStartDateAlternativeCode(int alternative)
	{
		return startDateAlternativeCodes[alternative];
	}
	
	public final static String getStartDateAlternativeDesc(int alternative)
	{
		return startDateAlternativeDescs[alternative];
	}
	
	public static final String[] getStartDateAlternativeCodes() {
		return startDateAlternativeCodes;
	}
	
	public static final String[] getStartDateAlternativeDescriptions() {
		return startDateAlternativeDescs;
	}
	
	public final static boolean isUpdateTypeWithoutArgument(boolean update, int type) {
		if (!update) return false; // doesn't apply
		
		switch(type) {
		case TYPE_UPDATE_DATE_INSUP	 :
		case TYPE_UPDATE_DATE_INSERTED :
		case TYPE_UPDATE_DATE_UPDATED :
		case TYPE_UPDATE_LAST_VERSION : return true;
		default: return false;
		}
	}

	private void readData(Node stepnode, List<? extends SharedObjectInterface> databases) throws KettleXMLException
	{
		try
		{
			String upd;
			int nrkeys, nrfields;
			String commit;

            schemaName = XMLHandler.getTagValue(stepnode, "schema"); //$NON-NLS-1$
			tableName = XMLHandler.getTagValue(stepnode, "table"); //$NON-NLS-1$
			String con = XMLHandler.getTagValue(stepnode, "connection"); //$NON-NLS-1$
			databaseMeta = DatabaseMeta.findDatabase(databases, con);
			commit = XMLHandler.getTagValue(stepnode, "commit"); //$NON-NLS-1$
			commitSize = Const.toInt(commit, 0);

			upd = XMLHandler.getTagValue(stepnode, "update"); //$NON-NLS-1$
			if (upd.equalsIgnoreCase("Y")) //$NON-NLS-1$
				update = true;
			else
				update = false;

			Node fields = XMLHandler.getSubNode(stepnode, "fields"); //$NON-NLS-1$

			nrkeys = XMLHandler.countNodes(fields, "key"); //$NON-NLS-1$
			nrfields = XMLHandler.countNodes(fields, "field"); //$NON-NLS-1$

			allocate(nrkeys, nrfields);

			// Read keys to dimension
			for (int i = 0; i < nrkeys; i++)
			{
				Node knode = XMLHandler.getSubNodeByNr(fields, "key", i); //$NON-NLS-1$

				keyStream[i] = XMLHandler.getTagValue(knode, "name"); //$NON-NLS-1$
				keyLookup[i] = XMLHandler.getTagValue(knode, "lookup"); //$NON-NLS-1$
			}

			// Only one date is supported
			// No datefield: use system date...
			Node dnode = XMLHandler.getSubNode(fields, "date"); //$NON-NLS-1$
			dateField = XMLHandler.getTagValue(dnode, "name"); //$NON-NLS-1$
			dateFrom = XMLHandler.getTagValue(dnode, "from"); //$NON-NLS-1$
			dateTo = XMLHandler.getTagValue(dnode, "to"); //$NON-NLS-1$

			for (int i = 0; i < nrfields; i++)
			{
				Node fnode = XMLHandler.getSubNodeByNr(fields, "field", i); //$NON-NLS-1$

				fieldStream[i] = XMLHandler.getTagValue(fnode, "name"); //$NON-NLS-1$
				fieldLookup[i] = XMLHandler.getTagValue(fnode, "lookup"); //$NON-NLS-1$
				upd = XMLHandler.getTagValue(fnode, "update"); //$NON-NLS-1$
				fieldUpdate[i] = getUpdateType(update, upd);
			}

			if (update)
			{
				// If this is empty: use auto-increment field!
				sequenceName = XMLHandler.getTagValue(stepnode, "sequence"); //$NON-NLS-1$
			}

			maxYear = Const.toInt(XMLHandler.getTagValue(stepnode, "max_year"), Const.MAX_YEAR); //$NON-NLS-1$
			minYear = Const.toInt(XMLHandler.getTagValue(stepnode, "min_year"), Const.MIN_YEAR); //$NON-NLS-1$

			keyField = XMLHandler.getTagValue(fields, "return", "name"); //$NON-NLS-1$ //$NON-NLS-2$
			keyRename = XMLHandler.getTagValue(fields, "return", "rename"); //$NON-NLS-1$ //$NON-NLS-2$
			autoIncrement = !"N".equalsIgnoreCase(XMLHandler.getTagValue(fields, "return", "use_autoinc")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			versionField = XMLHandler.getTagValue(fields, "return", "version"); //$NON-NLS-1$ //$NON-NLS-2$
			
			setTechKeyCreation(XMLHandler.getTagValue(fields, "return", "creation_method")); //$NON-NLS-1$
            
            cacheSize = Const.toInt(XMLHandler.getTagValue(stepnode, "cache_size"), -1); //$NON-NLS-1$
            preloadingCache = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "preload_cache")); //$NON-NLS-1$
            
            usingStartDateAlternative = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "use_start_date_alternative")); //$NON-NLS-1$
            startDateAlternative = getStartDateAlternative(XMLHandler.getTagValue(stepnode, "start_date_alternative")); //$NON-NLS-1$
            startDateFieldName = XMLHandler.getTagValue(stepnode, "start_date_field_name"); //$NON-NLS-1$ 
		}
		catch (Exception e)
		{
			throw new KettleXMLException(Messages.getString("DimensionLookupMeta.Exception.UnableToLoadStepInfoFromXML"), e); //$NON-NLS-1$
		}
	}

	public void setDefault()
	{
		int nrkeys, nrfields;

        schemaName = ""; //$NON-NLS-1$
		tableName = Messages.getString("DimensionLookupMeta.DefualtTableName"); //$NON-NLS-1$
		databaseMeta = null;
		commitSize = 100;
		update = true;

		nrkeys = 0;
		nrfields = 0;

		allocate(nrkeys, nrfields);

		// Read keys to dimension
		for (int i = 0; i < nrkeys; i++)
		{
			keyStream[i] = "key" + i; //$NON-NLS-1$
			keyLookup[i] = "keylookup" + i; //$NON-NLS-1$
		}

		for (int i = 0; i < nrfields; i++)
		{
			fieldStream[i] = "field" + i; //$NON-NLS-1$
			fieldLookup[i] = "lookup" + i; //$NON-NLS-1$
			fieldUpdate[i] = DimensionLookupMeta.TYPE_UPDATE_DIM_INSERT;
		}

		// Only one date is supported
		// No datefield: use system date...
		dateField = ""; //$NON-NLS-1$
		dateFrom = "date_from"; //$NON-NLS-1$
		dateTo = "date_to"; //$NON-NLS-1$

		minYear = Const.MIN_YEAR;
		maxYear = Const.MAX_YEAR;

		keyField = ""; //$NON-NLS-1$
		keyRename = ""; //$NON-NLS-1$
		autoIncrement = false;
		versionField = "version"; //$NON-NLS-1$
        
        cacheSize = 5000;
        preloadingCache = false;
	}

	public void getFields(RowMetaInterface row, String name, RowMetaInterface[] info, StepMeta nextStep, VariableSpace space) throws KettleStepException 
	{
        LogWriter log = LogWriter.getInstance();

		ValueMetaInterface v = new ValueMeta(keyField, ValueMetaInterface.TYPE_INTEGER);
		if (keyRename != null && keyRename.length() > 0)
			v.setName(keyRename);

		v.setLength(9);
        v.setPrecision(0);
		v.setOrigin(name);
		row.addValueMeta(v);

		// retrieve extra fields on lookup?
        // Don't bother if there are no return values specified.
		if (!update && fieldLookup.length>0)
		{
			Database db = null;
            try
            {
                // Get the rows from the table...
                if (databaseMeta!=null)
                {
                    db = new Database(databaseMeta);
                    db.shareVariablesWith(space);
                    // First try without connecting to the database... (can be  S L O W)
                    String schemaTable = databaseMeta.getQuotedSchemaTableCombination(schemaName, tableName);
                    RowMetaInterface extraFields = db.getTableFields(schemaTable);
                    if (extraFields==null) // now we need to connect
                    {
                    	db.connect();
                    	extraFields = db.getTableFields(schemaTable);
                    }
                    
                    for (int i = 0; i < fieldLookup.length; i++)
                    {
                        v = extraFields.searchValueMeta(fieldLookup[i]);
                        if (v==null)
                        {
                            String message = Messages.getString("DimensionLookupMeta.Exception.UnableToFindReturnField",fieldLookup[i]); //$NON-NLS-1$ //$NON-NLS-2$
                            log.logError(toString(), message);
                            throw new KettleStepException(message);
                        }
                        
                        // If the field needs to be renamed, rename
                        if (fieldStream[i] != null && fieldStream[i].length() > 0)
                        {
                            v.setName(fieldStream[i]);
                        }
                        v.setOrigin(name);
                        row.addValueMeta(v);
                    }
                }
                else
                {
                    String message = Messages.getString("DimensionLookupMeta.Exception.UnableToRetrieveDataTypeOfReturnField"); //$NON-NLS-1$
                    log.logError(toString(), message);
                    throw new KettleStepException(message);
                }
            }
            catch(Exception e)
            {
                String message = Messages.getString("DimensionLookupMeta.Exception.UnableToRetrieveDataTypeOfReturnField2"); //$NON-NLS-1$
                log.logError(toString(), message);
                throw new KettleStepException(message, e);
            }
            finally
            {
            	if (db!=null) db.disconnect();
            }
   		}
	}

	public String getXML()
	{
        StringBuffer retval = new StringBuffer(512);
		
        retval.append("      ").append(XMLHandler.addTagValue("schema", schemaName)); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("      ").append(XMLHandler.addTagValue("table", tableName)); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("      ").append(XMLHandler.addTagValue("connection", databaseMeta == null ? "" : databaseMeta.getName())); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		retval.append("      ").append(XMLHandler.addTagValue("commit", commitSize)); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("      ").append(XMLHandler.addTagValue("update", update)); //$NON-NLS-1$ //$NON-NLS-2$

		retval.append("      <fields>").append(Const.CR); //$NON-NLS-1$
		for (int i = 0; i < keyStream.length; i++)
		{
			retval.append("        <key>").append(Const.CR); //$NON-NLS-1$
			retval.append("          ").append(XMLHandler.addTagValue("name", keyStream[i])); //$NON-NLS-1$ //$NON-NLS-2$
			retval.append("          ").append(XMLHandler.addTagValue("lookup", keyLookup[i])); //$NON-NLS-1$ //$NON-NLS-2$
			retval.append("        </key>").append(Const.CR); //$NON-NLS-1$
		}

		retval.append("        <date>").append(Const.CR); //$NON-NLS-1$
		retval.append("          ").append(XMLHandler.addTagValue("name", dateField)); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("          ").append(XMLHandler.addTagValue("from", dateFrom)); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("          ").append(XMLHandler.addTagValue("to", dateTo)); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("        </date>").append(Const.CR); //$NON-NLS-1$

		if (fieldLookup != null)
        {
			for (int i = 0; i < fieldLookup.length; i++)
			{
				if (fieldLookup[i] != null)
				{
					retval.append("        <field>").append(Const.CR); //$NON-NLS-1$
					retval.append("          ").append(XMLHandler.addTagValue("name", fieldStream[i])); //$NON-NLS-1$ //$NON-NLS-2$
					retval.append("          ").append(XMLHandler.addTagValue("lookup", fieldLookup[i])); //$NON-NLS-1$ //$NON-NLS-2$
					retval.append("          ").append(XMLHandler.addTagValue("update", getUpdateTypeCode(update, fieldUpdate[i]))); //$NON-NLS-1$ //$NON-NLS-2$
					retval.append("        </field>").append(Const.CR); //$NON-NLS-1$
				}
			}
        }
		retval.append("        <return>").append(Const.CR); //$NON-NLS-1$
		retval.append("          ").append(XMLHandler.addTagValue("name", keyField)); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("          ").append(XMLHandler.addTagValue("rename", keyRename)); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("          ").append(XMLHandler.addTagValue("creation_method", techKeyCreation)); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("          ").append(XMLHandler.addTagValue("use_autoinc", autoIncrement)); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("          ").append(XMLHandler.addTagValue("version", versionField)); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("        </return>").append(Const.CR); //$NON-NLS-1$

		retval.append("      </fields>").append(Const.CR); //$NON-NLS-1$

		// If sequence is empty: use auto-increment field!
		retval.append("      ").append(XMLHandler.addTagValue("sequence", sequenceName)); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("      ").append(XMLHandler.addTagValue("min_year", minYear)); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("      ").append(XMLHandler.addTagValue("max_year", maxYear)); //$NON-NLS-1$ //$NON-NLS-2$

        retval.append("      ").append(XMLHandler.addTagValue("cache_size", cacheSize)); //$NON-NLS-1$ //$NON-NLS-2$
        retval.append("      ").append(XMLHandler.addTagValue("preload_cache", preloadingCache)); //$NON-NLS-1$ //$NON-NLS-2$

        retval.append("      ").append(XMLHandler.addTagValue("use_start_date_alternative", usingStartDateAlternative)); //$NON-NLS-1$ //$NON-NLS-2$
        retval.append("      ").append(XMLHandler.addTagValue("start_date_alternative", getStartDateAlternativeCode(startDateAlternative))); //$NON-NLS-1$ //$NON-NLS-2$
        retval.append("      ").append(XMLHandler.addTagValue("start_date_field_name", startDateFieldName)); //$NON-NLS-1$ //$NON-NLS-2$

        return retval.toString();
	}

	public void readRep(Repository rep, long id_step, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleException
	{
		try
		{
			long id_connection = rep.getStepAttributeInteger(id_step, "id_connection"); //$NON-NLS-1$
			databaseMeta = DatabaseMeta.findDatabase(databases, id_connection);

            schemaName = rep.getStepAttributeString(id_step, "schema"); //$NON-NLS-1$
			tableName = rep.getStepAttributeString(id_step, "table"); //$NON-NLS-1$
			commitSize = (int) rep.getStepAttributeInteger(id_step, "commit"); //$NON-NLS-1$
			update = rep.getStepAttributeBoolean(id_step, "update"); //$NON-NLS-1$

			int nrkeys = rep.countNrStepAttributes(id_step, "lookup_key_name"); //$NON-NLS-1$
			int nrfields = rep.countNrStepAttributes(id_step, "field_name"); //$NON-NLS-1$

			allocate(nrkeys, nrfields);

			for (int i = 0; i < nrkeys; i++)
			{
				keyStream[i] = rep.getStepAttributeString(id_step, i, "lookup_key_name"); //$NON-NLS-1$
				keyLookup[i] = rep.getStepAttributeString(id_step, i, "lookup_key_field"); //$NON-NLS-1$
			}

			dateField = rep.getStepAttributeString(id_step, "date_name"); //$NON-NLS-1$
			dateFrom = rep.getStepAttributeString(id_step, "date_from"); //$NON-NLS-1$
			dateTo = rep.getStepAttributeString(id_step, "date_to"); //$NON-NLS-1$

			for (int i = 0; i < nrfields; i++)
			{
				fieldStream[i] = rep.getStepAttributeString(id_step, i, "field_name"); //$NON-NLS-1$
				fieldLookup[i] = rep.getStepAttributeString(id_step, i, "field_lookup"); //$NON-NLS-1$
				fieldUpdate[i] = getUpdateType(update, rep.getStepAttributeString(id_step, i, "field_update")); //$NON-NLS-1$
			}

			keyField = rep.getStepAttributeString(id_step, "return_name"); //$NON-NLS-1$
			keyRename = rep.getStepAttributeString(id_step, "return_rename"); //$NON-NLS-1$
			autoIncrement = rep.getStepAttributeBoolean(id_step, "use_autoinc"); //$NON-NLS-1$
			versionField = rep.getStepAttributeString(id_step, "version_field"); //$NON-NLS-1$
			techKeyCreation = rep.getStepAttributeString(id_step, "creation_method"); //$NON-NLS-1$

			sequenceName = rep.getStepAttributeString(id_step, "sequence"); //$NON-NLS-1$
			minYear = (int) rep.getStepAttributeInteger(id_step, "min_year"); //$NON-NLS-1$
			maxYear = (int) rep.getStepAttributeInteger(id_step, "max_year"); //$NON-NLS-1$

            cacheSize = (int) rep.getStepAttributeInteger(id_step, "cache_size"); //$NON-NLS-1$
            preloadingCache = rep.getStepAttributeBoolean(id_step, "preload_cache"); //$NON-NLS-1$
            
            usingStartDateAlternative = rep.getStepAttributeBoolean(id_step, "use_start_date_alternative"); //$NON-NLS-1$
            startDateAlternative = getStartDateAlternative(rep.getStepAttributeString(id_step, "start_date_alternative")); //$NON-NLS-1$
            startDateFieldName = rep.getStepAttributeString(id_step, "start_date_field_name"); //$NON-NLS-1$ 
		}
		catch (Exception e)
		{
			throw new KettleException(Messages.getString("DimensionLookupMeta.Exception.UnexpectedErrorReadingStepInfoFromRepository"), e); //$NON-NLS-1$
		}
	}

	public void saveRep(Repository rep, long id_transformation, long id_step) throws KettleException
    {
        try
        {
            rep.saveStepAttribute(id_transformation, id_step, "schema", schemaName); //$NON-NLS-1$
            rep.saveStepAttribute(id_transformation, id_step, "table", tableName); //$NON-NLS-1$
            rep.saveStepAttribute(id_transformation, id_step, "id_connection", databaseMeta == null ? -1 : databaseMeta.getID()); //$NON-NLS-1$
            rep.saveStepAttribute(id_transformation, id_step, "commit", commitSize); //$NON-NLS-1$
            rep.saveStepAttribute(id_transformation, id_step, "update", update); //$NON-NLS-1$

            for (int i = 0; i < keyStream.length; i++)
            {
                rep.saveStepAttribute(id_transformation, id_step, i, "lookup_key_name", keyStream[i]); //$NON-NLS-1$
                rep.saveStepAttribute(id_transformation, id_step, i, "lookup_key_field", keyLookup[i]); //$NON-NLS-1$
            }

            rep.saveStepAttribute(id_transformation, id_step, "date_name", dateField); //$NON-NLS-1$
            rep.saveStepAttribute(id_transformation, id_step, "date_from", dateFrom); //$NON-NLS-1$
            rep.saveStepAttribute(id_transformation, id_step, "date_to", dateTo); //$NON-NLS-1$

            if (fieldLookup != null) for (int i = 0; i < fieldLookup.length; i++)
            {
                if (fieldLookup[i] != null)
                {
                    rep.saveStepAttribute(id_transformation, id_step, i, "field_name", fieldStream[i]); //$NON-NLS-1$
                    rep.saveStepAttribute(id_transformation, id_step, i, "field_lookup", fieldLookup[i]); //$NON-NLS-1$
                    rep.saveStepAttribute(id_transformation, id_step, i, "field_update", getUpdateTypeCode(update, fieldUpdate[i])); //$NON-NLS-1$
                }
            }

            rep.saveStepAttribute(id_transformation, id_step, "return_name", keyField); //$NON-NLS-1$
            rep.saveStepAttribute(id_transformation, id_step, "return_rename", keyRename); //$NON-NLS-1$
            rep.saveStepAttribute(id_transformation, id_step, "creation_method", techKeyCreation); //$NON-NLS-1$
            // For the moment still save 'use_autoinc' for backwards compatibility (Sven Boden).
            rep.saveStepAttribute(id_transformation, id_step, "use_autoinc", autoIncrement); //$NON-NLS-1$
            rep.saveStepAttribute(id_transformation, id_step, "version_field", versionField); //$NON-NLS-1$

            rep.saveStepAttribute(id_transformation, id_step, "sequence", sequenceName); //$NON-NLS-1$
            rep.saveStepAttribute(id_transformation, id_step, "min_year", minYear); //$NON-NLS-1$
            rep.saveStepAttribute(id_transformation, id_step, "max_year", maxYear); //$NON-NLS-1$

            rep.saveStepAttribute(id_transformation, id_step, "cache_size", cacheSize); //$NON-NLS-1$
            rep.saveStepAttribute(id_transformation, id_step, "preload_cache", preloadingCache); //$NON-NLS-1$

            rep.saveStepAttribute(id_transformation, id_step, "use_start_date_alternative", usingStartDateAlternative); //$NON-NLS-1$
            rep.saveStepAttribute(id_transformation, id_step, "start_date_alternative", getStartDateAlternativeCode(startDateAlternative)); //$NON-NLS-1$
            rep.saveStepAttribute(id_transformation, id_step, "start_date_field_name", startDateFieldName); //$NON-NLS-1$ 

            // Also, save the step-database relationship!
            if (databaseMeta != null) rep.insertStepDatabase(id_transformation, id_step, databaseMeta.getID());
            
            
        }
        catch (KettleDatabaseException dbe)
        {
            throw new KettleException(Messages.getString("DimensionLookupMeta.Exception.UnableToLoadDimensionLookupInfoFromRepository"), dbe); //$NON-NLS-1$
        }
    }

	public Date getMinDate()
	{
		Calendar mincal = Calendar.getInstance();
		mincal.set(Calendar.YEAR, minYear);
		mincal.set(Calendar.MONTH, 0);
		mincal.set(Calendar.DAY_OF_MONTH, 1);
		mincal.set(Calendar.HOUR_OF_DAY, 0);
		mincal.set(Calendar.MINUTE, 0);
		mincal.set(Calendar.SECOND, 0);
		mincal.set(Calendar.MILLISECOND, 0);

		return mincal.getTime();
	}

	public Date getMaxDate()
	{
		Calendar mincal = Calendar.getInstance();
		mincal.set(Calendar.YEAR, maxYear);
		mincal.set(Calendar.MONTH, 11);
		mincal.set(Calendar.DAY_OF_MONTH, 31);
		mincal.set(Calendar.HOUR_OF_DAY, 23);
		mincal.set(Calendar.MINUTE, 59);
		mincal.set(Calendar.SECOND, 59);
		mincal.set(Calendar.MILLISECOND, 999);

		return mincal.getTime();
	}

	public void check(List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepinfo, RowMetaInterface prev, String input[], String output[], RowMetaInterface info)
	{
		if (update)
			checkUpdate(remarks, stepinfo, prev, transMeta);
		else
			checkLookup(remarks, stepinfo, prev, transMeta);

		if ( techKeyCreation != null )
		{
		    // post 2.2 version
			if ( !(CREATION_METHOD_AUTOINC.equals(techKeyCreation) ||
			       CREATION_METHOD_SEQUENCE.equals(techKeyCreation) ||
			       CREATION_METHOD_TABLEMAX.equals(techKeyCreation)) )
			{
				String error_message = Messages.getString("DimensionLookupMeta.CheckResult.ErrorTechKeyCreation")+ ": " + techKeyCreation +"!"; //$NON-NLS-1$ //$NON-NLS-2$
				CheckResult cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepinfo);
				remarks.add(cr);
			}
		}		
		
		// See if we have input streams leading to this step!
		if (input.length > 0)
		{
			CheckResult cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, Messages.getString("DimensionLookupMeta.CheckResult.StepReceiveInfoOK"), //$NON-NLS-1$
					stepinfo);
			remarks.add(cr);
		}
		else
		{
			CheckResult cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, Messages.getString("DimensionLookupMeta.CheckResult.NoInputReceiveFromOtherSteps"), //$NON-NLS-1$
					stepinfo);
			remarks.add(cr);
		}
	}

	private void checkUpdate(List<CheckResultInterface> remarks, StepMeta stepinfo, RowMetaInterface prev, VariableSpace space)
	{
		LogWriter log = LogWriter.getInstance();
		
		CheckResult cr;
		String error_message = ""; //$NON-NLS-1$

		if (databaseMeta != null)
		{
			Database db = new Database(databaseMeta);
	        db.shareVariablesWith(space);
			try
			{
				db.connect();
				if (!Const.isEmpty(tableName))
				{
					boolean first = true;
					boolean error_found = false;
					error_message = ""; //$NON-NLS-1$
					
                    String schemaTable = databaseMeta.getQuotedSchemaTableCombination(schemaName, tableName);
					RowMetaInterface r = db.getTableFields(schemaTable);
					if (r != null)
					{
						for (int i = 0; i < fieldLookup.length; i++)
						{
							String lufield = fieldLookup[i];
							log.logDebug(toString(), Messages.getString("DimensionLookupMeta.Log.CheckLookupField") + i + " --> " + lufield //$NON-NLS-1$ //$NON-NLS-2$
														+ " in lookup table..."); //$NON-NLS-1$
							ValueMetaInterface v = r.searchValueMeta(lufield);
							if (v == null)
							{
								if (first)
								{
									first = false;
									error_message += Messages.getString("DimensionLookupMeta.CheckResult.MissingCompareFieldsInTargetTable") + Const.CR; //$NON-NLS-1$
								}
								error_found = true;
								error_message += "\t\t" + lufield + Const.CR; //$NON-NLS-1$
							}
						}
						if (error_found)
						{
							cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepinfo);
						}
						else
						{
							cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK,
									Messages.getString("DimensionLookupMeta.CheckResult.AllLookupFieldFound"), stepinfo); //$NON-NLS-1$
						}
						remarks.add(cr);

						/* Also, check the fields: tk, version, from-to, ... */
                        if (keyField!=null && keyField.length()>0)
                        {
    						if (r.indexOfValue(keyField) < 0)
    						{
    							error_message = Messages.getString("DimensionLookupMeta.CheckResult.TechnicalKeyNotFound",keyField ) //$NON-NLS-1$ //$NON-NLS-2$
    											+ Const.CR;
    							cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepinfo);
    						}
    						else
    						{
    							error_message = Messages.getString("DimensionLookupMeta.CheckResult.TechnicalKeyFound",keyField ) //$NON-NLS-1$ //$NON-NLS-2$
    											+ Const.CR;
    							cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, error_message, stepinfo);
    						}
    						remarks.add(cr);
                        }
                        else
                        {
                            error_message = Messages.getString("DimensionLookupMeta.CheckResult.TechnicalKeyRequired") + Const.CR; //$NON-NLS-1$
                            remarks.add( new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepinfo) );
                        }

                        if (versionField != null && versionField.length() > 0)
						{
							if (r.indexOfValue(versionField) < 0)
							{
								error_message = Messages.getString("DimensionLookupMeta.CheckResult.VersionFieldNotFound", versionField //$NON-NLS-1$
												) + Const.CR; //$NON-NLS-1$
								cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepinfo);
							}
							else
							{
								error_message = Messages.getString("DimensionLookupMeta.CheckResult.VersionFieldFound", versionField ) + Const.CR; //$NON-NLS-1$ //$NON-NLS-2$
								cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, error_message, stepinfo);
							}
							remarks.add(cr);
						}
                        else
                        {
                            error_message = Messages.getString("DimensionLookupMeta.CheckResult.VersionKeyRequired") + Const.CR; //$NON-NLS-1$
                            remarks.add( new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepinfo) );
                        }

						if (dateFrom != null && dateFrom.length() > 0)
						{
							if (r.indexOfValue(dateFrom) < 0)
							{
								error_message = Messages.getString("DimensionLookupMeta.CheckResult.StartPointOfDaterangeNotFound", dateFrom //$NON-NLS-1$
												) + Const.CR; //$NON-NLS-1$
								cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepinfo);
							}
							else
							{
								error_message = Messages.getString("DimensionLookupMeta.CheckResult.StartPointOfDaterangeFound", dateFrom //$NON-NLS-1$
												) + Const.CR; //$NON-NLS-1$
								cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, error_message, stepinfo);
							}
							remarks.add(cr);
						}
                        else
                        {
                            error_message = Messages.getString("DimensionLookupMeta.CheckResult.StartKeyRequired") + Const.CR; //$NON-NLS-1$
                            remarks.add( new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepinfo) );
                        }

						if (dateTo != null && dateTo.length() > 0)
						{
							if (r.indexOfValue(dateTo) < 0)
							{
								error_message = Messages.getString("DimensionLookupMeta.CheckResult.EndPointOfDaterangeNotFound", dateTo //$NON-NLS-1$
												) + Const.CR; //$NON-NLS-1$
								cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepinfo);
							}
							else
							{
								error_message = Messages.getString("DimensionLookupMeta.CheckResult.EndPointOfDaterangeFound", dateTo //$NON-NLS-1$
												) + Const.CR; //$NON-NLS-1$
								cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, error_message, stepinfo);
							}
							remarks.add(cr);
						}
                        else
                        {
                            error_message = Messages.getString("DimensionLookupMeta.CheckResult.EndKeyRequired") + Const.CR; //$NON-NLS-1$
                            remarks.add( new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepinfo) );
                        }
					}
					else
					{
						error_message = Messages.getString("DimensionLookupMeta.CheckResult.CouldNotReadTableInfo"); //$NON-NLS-1$
						cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepinfo);
						remarks.add(cr);
					}
				}

				// Look up fields in the input stream <prev>
				if (prev != null && prev.size() > 0)
				{
					boolean first = true;
					error_message = ""; //$NON-NLS-1$
					boolean error_found = false;

					for (int i = 0; i < fieldStream.length; i++)
					{
						log.logDebug(toString(), Messages.getString("DimensionLookupMeta.Log.CheckField" ,i + " --> " + fieldStream[i])); //$NON-NLS-1$
						ValueMetaInterface v = prev.searchValueMeta(fieldStream[i]);
						if (v == null)
						{
							if (first)
							{
								first = false;
								error_message += Messages.getString("DimensionLookupMeta.CheckResult.MissongFields") + Const.CR; //$NON-NLS-1$
							}
							error_found = true;
							error_message += "\t\t" + fieldStream[i] + Const.CR; //$NON-NLS-1$
						}
					}
					if (error_found)
					{
						cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepinfo);
					}
					else
					{
						cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, Messages.getString("DimensionLookupMeta.CheckResult.AllFieldsFound"), //$NON-NLS-1$
								stepinfo);
					}
					remarks.add(cr);
				}
				else
				{
					error_message = Messages.getString("DimensionLookupMeta.CheckResult.CouldNotReadFieldsFromPreviousStep") + Const.CR; //$NON-NLS-1$
					cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepinfo);
					remarks.add(cr);
				}

				// Check sequence
				if (databaseMeta.supportsSequences() && CREATION_METHOD_SEQUENCE.equals(getTechKeyCreation()) &&
						 sequenceName != null && sequenceName.length() != 0)
				{
					if (db.checkSequenceExists(sequenceName))
					{
						error_message = Messages.getString("DimensionLookupMeta.CheckResult.SequenceExists", sequenceName ); //$NON-NLS-1$ //$NON-NLS-2$
						cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, error_message, stepinfo);
						remarks.add(cr);
					}
					else
					{
						error_message += Messages.getString("DimensionLookupMeta.CheckResult.SequenceCouldNotFound", sequenceName ); //$NON-NLS-1$ //$NON-NLS-2$
						cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepinfo);
						remarks.add(cr);
					}
				}
			}
			catch (KettleException e)
			{
				error_message = Messages.getString("DimensionLookupMeta.CheckResult.CouldNotConectToDB") + e.getMessage(); //$NON-NLS-1$
				cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepinfo);
				remarks.add(cr);
			}
		}
		else
		{
			error_message = Messages.getString("DimensionLookupMeta.CheckResult.InvalidConnectionName"); //$NON-NLS-1$
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepinfo);
			remarks.add(cr);
		}
	}

	private void checkLookup(List<CheckResultInterface> remarks, StepMeta stepinfo, RowMetaInterface prev, VariableSpace space)
	{
		int i;
		boolean error_found = false;
		String error_message = ""; //$NON-NLS-1$
		boolean first;
		CheckResult cr;

		if (databaseMeta != null)
		{
			Database db = new Database(databaseMeta);
	        db.shareVariablesWith(space);
			try
			{
				db.connect();

				if (!Const.isEmpty(tableName))
				{
                    String schemaTable = databaseMeta.getQuotedSchemaTableCombination(schemaName, tableName);
					RowMetaInterface tableFields = db.getTableFields(schemaTable);
					if (tableFields != null)
					{
						if (prev != null && prev.size() > 0)
						{
							// Start at the top, see if the key fields exist:
							first = true;
							boolean warning_found = false;
							for (i = 0; i < keyStream.length; i++)
							{
								// Does the field exist in the input stream?
								String strfield = keyStream[i];
								ValueMetaInterface strvalue = prev.searchValueMeta(strfield); // 
								if (strvalue == null)
								{
									if (first)
									{
										first = false;
										error_message += Messages.getString("DimensionLookupMeta.CheckResult.KeyhasProblem") + Const.CR; //$NON-NLS-1$
									}
									error_found = true;
									error_message += "\t\t" + keyField +Messages.getString("DimensionLookupMeta.CheckResult.KeyNotPresentInStream") + Const.CR; //$NON-NLS-1$ //$NON-NLS-2$
								}
								else
								{
									// does the field exist in the dimension table?
									String dimfield = keyLookup[i];
                                    ValueMetaInterface dimvalue = tableFields.searchValueMeta(dimfield);
									if (dimvalue == null)
									{
										if (first)
										{
											first = false;
											error_message += Messages.getString("DimensionLookupMeta.CheckResult.KeyhasProblem2") + Const.CR; //$NON-NLS-1$
										}
										error_found = true;
										error_message += "\t\t" + dimfield +Messages.getString("DimensionLookupMeta.CheckResult.KeyNotPresentInDimensiontable") //$NON-NLS-1$ //$NON-NLS-2$
															+ schemaTable + ")" + Const.CR; //$NON-NLS-1$
									}
									else
									{
										// Is the streamvalue of the same type as the dimension value?
										if (strvalue.getType() != dimvalue.getType())
										{
											if (first)
											{
												first = false;
												error_message += Messages.getString("DimensionLookupMeta.CheckResult.KeyhasProblem3") + Const.CR; //$NON-NLS-1$
											}
											warning_found = true;
											error_message += "\t\t" + strfield + " (" + strvalue.getOrigin() //$NON-NLS-1$ //$NON-NLS-2$
																+Messages.getString("DimensionLookupMeta.CheckResult.KeyNotTheSameTypeAs") + dimfield + " (" //$NON-NLS-1$ //$NON-NLS-2$
																+ schemaTable + ")" + Const.CR; //$NON-NLS-1$
											error_message += Messages.getString("DimensionLookupMeta.CheckResult.WarningInfoInDBConversion"); //$NON-NLS-1$
										}
									}
								}
							}
							if (error_found)
							{
								cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepinfo);
							}
							else
								if (warning_found)
								{
									cr = new CheckResult(CheckResultInterface.TYPE_RESULT_WARNING, error_message, stepinfo);
								}
								else
								{
									cr = new CheckResult(
											CheckResultInterface.TYPE_RESULT_OK,
											Messages.getString("DimensionLookupMeta.CheckResult.AllKeysFieldsFound"), //$NON-NLS-1$
											stepinfo);
								}
							remarks.add(cr);

							// In case of lookup, the first column of the UpIns dialog table contains the table field
							error_found = false;
							for (i = 0; i < fieldLookup.length; i++)
							{
								String lufield = fieldLookup[i];
								if (lufield != null && lufield.length() > 0)
								{
									// Checking compare field: lufield
                                    ValueMetaInterface v = tableFields.searchValueMeta(lufield);
									if (v == null)
									{
										if (first)
										{
											first = false;
											error_message += Messages.getString("DimensionLookupMeta.CheckResult.FieldsToRetrieveNotExistInDimension") //$NON-NLS-1$
																+ Const.CR;
										}
										error_found = true;
										error_message += "\t\t" + lufield + Const.CR; //$NON-NLS-1$
									}
								}
							}
							if (error_found)
							{
								cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepinfo);
							}
							else
							{
								cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK,
										Messages.getString("DimensionLookupMeta.CheckResult.AllFieldsToRetrieveFound"), stepinfo); //$NON-NLS-1$
							}
							remarks.add(cr);

							/* Also, check the fields: tk, version, from-to, ... */
							if (tableFields.indexOfValue(keyField) < 0)
							{
								error_message = Messages.getString("DimensionLookupMeta.CheckResult.TechnicalKeyNotFound", keyField ) //$NON-NLS-1$ //$NON-NLS-2$
												+ Const.CR;
								cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepinfo);
							}
							else
							{
								error_message = Messages.getString("DimensionLookupMeta.CheckResult.TechnicalKeyFound",keyField ) //$NON-NLS-1$ //$NON-NLS-2$
												+ Const.CR;
								cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, error_message, stepinfo);
							}
							remarks.add(cr);

							if (tableFields.indexOfValue(versionField) < 0)
							{
								error_message = Messages.getString("DimensionLookupMeta.CheckResult.VersionFieldNotFound",versionField //$NON-NLS-1$
												) + Const.CR; //$NON-NLS-1$
								cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepinfo);
							}
							else
							{
								error_message = Messages.getString("DimensionLookupMeta.CheckResult.VersionFieldFound", versionField ) //$NON-NLS-1$ //$NON-NLS-2$
												+ Const.CR;
								cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, error_message, stepinfo);
							}
							remarks.add(cr);

							if (tableFields.indexOfValue(dateFrom) < 0)
							{
								error_message = Messages.getString("DimensionLookupMeta.CheckResult.StartOfDaterangeFieldNotFound", dateFrom //$NON-NLS-1$
												) + Const.CR; //$NON-NLS-1$
								cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepinfo);
							}
							else
							{
								error_message = Messages.getString("DimensionLookupMeta.CheckResult.StartOfDaterangeFieldFound", dateFrom //$NON-NLS-1$
												) + Const.CR; //$NON-NLS-1$
								cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, error_message, stepinfo);
							}
							remarks.add(cr);

							if (tableFields.indexOfValue(dateTo) < 0)
							{
								error_message = Messages.getString("DimensionLookupMeta.CheckResult.EndOfDaterangeFieldNotFound", dateTo //$NON-NLS-1$
												) + Const.CR; //$NON-NLS-1$
								cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepinfo);
							}
							else
							{
								error_message = Messages.getString("DimensionLookupMeta.CheckResult.EndOfDaterangeFieldFound", dateTo //$NON-NLS-1$
												); //$NON-NLS-1$
								cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, error_message, stepinfo);
							}
							remarks.add(cr);
						}
						else
						{
							error_message = Messages.getString("DimensionLookupMeta.CheckResult.CouldNotReadFieldsFromPreviousStep") + Const.CR; //$NON-NLS-1$
							cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepinfo);
							remarks.add(cr);
						}
					}
					else
					{
						error_message = Messages.getString("DimensionLookupMeta.CheckResult.CouldNotReadTableInfo"); //$NON-NLS-1$
						cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepinfo);
						remarks.add(cr);
					}
				}
			}
			catch (KettleException e)
			{
				error_message = Messages.getString("DimensionLookupMeta.CheckResult.CouldNotConnectDB") + e.getMessage(); //$NON-NLS-1$
				cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepinfo);
				remarks.add(cr);
			}
		}
		else
		{
			error_message = Messages.getString("DimensionLookupMeta.CheckResult.InvalidConnection"); //$NON-NLS-1$
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepinfo);
			remarks.add(cr);
		}
	}

	public RowMetaInterface getTableFields()
	{
		LogWriter log = LogWriter.getInstance();
        
		RowMetaInterface fields = null;
		if (databaseMeta != null)
		{
			Database db = new Database(databaseMeta);
			try
			{
				db.connect();
				fields = db.getTableFields(databaseMeta.getQuotedSchemaTableCombination(schemaName, tableName));
			}
			catch (KettleDatabaseException dbe)
			{
				log.logError(toString(), Messages.getString("DimensionLookupMeta.Log.DatabaseErrorOccurred") + dbe.getMessage()); //$NON-NLS-1$
			}
			finally
			{
				db.disconnect();
			}
		}
		return fields;
	}

	public SQLStatement getSQLStatements(TransMeta transMeta, StepMeta stepMeta, RowMetaInterface prev)
	{
		LogWriter log = LogWriter.getInstance();
		SQLStatement retval = new SQLStatement(stepMeta.getName(), databaseMeta, null); // default: nothing to do!

		if (update) // Only bother in case of update, not lookup!
		{
			log.logDebug(toString(), Messages.getString("DimensionLookupMeta.Log.Update")); //$NON-NLS-1$
			if (databaseMeta != null)
			{
				if (prev != null && prev.size() > 0)
				{
					String schemaTable = databaseMeta.getQuotedSchemaTableCombination(schemaName, tableName);
					if (!Const.isEmpty(schemaTable))
					{                       
						Database db = new Database(databaseMeta);
						db.shareVariablesWith(transMeta);
						try
						{
							db.connect();

							String sql = ""; //$NON-NLS-1$

							// How does the table look like?
							//
                            RowMetaInterface fields = new RowMeta();

							// First the technical key
							//
							ValueMetaInterface vkeyfield = new ValueMeta(keyField, ValueMetaInterface.TYPE_INTEGER);
							vkeyfield.setLength(10);
							fields.addValueMeta(vkeyfield);

							// The the version
							//
                            ValueMetaInterface vversion = new ValueMeta(versionField, ValueMetaInterface.TYPE_INTEGER);
							vversion.setLength(5);
							fields.addValueMeta(vversion);

							// The date from
							//
                            ValueMetaInterface vdatefrom = new ValueMeta(dateFrom, ValueMetaInterface.TYPE_DATE);
							fields.addValueMeta(vdatefrom);

							// The date to
							//
                            ValueMetaInterface vdateto = new ValueMeta(dateTo, ValueMetaInterface.TYPE_DATE);
							fields.addValueMeta(vdateto);
							
							String errors = ""; //$NON-NLS-1$

							// Then the keys
							//
							for (int i = 0; i < keyLookup.length; i++)
							{
                                ValueMetaInterface vprev = prev.searchValueMeta(keyStream[i]);
								if (vprev != null)
								{
                                    ValueMetaInterface field = vprev.clone();
									field.setName(keyLookup[i]);
									fields.addValueMeta(field);
								}
								else
								{
									if (errors.length() > 0)
										errors += ", "; //$NON-NLS-1$
									errors += keyStream[i];
								}
							}

							//
							// Then the fields to update...
							//
							for (int i = 0; i < fieldLookup.length; i++)
							{
                                ValueMetaInterface vprev = prev.searchValueMeta(fieldStream[i]);
								if (vprev != null)
								{
									ValueMetaInterface field = vprev.clone();
									field.setName(fieldLookup[i]);
									fields.addValueMeta(field);
								}
								else
								{
									if (errors.length() > 0)
										errors += ", "; //$NON-NLS-1$
									errors += fieldStream[i];
								}
							}
							
							// Finally, the special update fields...
							//
							for (int i=0;i<fieldUpdate.length;i++) {
								ValueMetaInterface valueMeta = null;
								switch(fieldUpdate[i]) {
								case TYPE_UPDATE_DATE_INSUP    :
								case TYPE_UPDATE_DATE_INSERTED :
								case TYPE_UPDATE_DATE_UPDATED  : valueMeta = new ValueMeta(fieldLookup[i], ValueMetaInterface.TYPE_DATE); break;
								case TYPE_UPDATE_LAST_VERSION  : valueMeta = new ValueMeta(fieldLookup[i], ValueMetaInterface.TYPE_BOOLEAN); break;
								}
								if (valueMeta!=null) {
									fields.addValueMeta(valueMeta);
								}
							}

							if (errors.length() > 0)
							{
								retval.setError(Messages.getString("DimensionLookupMeta.ReturnValue.UnableToFindFields") + errors); //$NON-NLS-1$
							}

							log.logDebug(toString(), Messages.getString("DimensionLookupMeta.Log.GetDDLForTable") + schemaTable + "] : " //$NON-NLS-1$ //$NON-NLS-2$
															+ fields.toStringMeta());

							sql += db.getDDL(schemaTable, fields, (sequenceName != null && sequenceName.length() != 0) ? null : keyField, autoIncrement, null, true);

							log.logDebug(toString(), "sql =" + sql); //$NON-NLS-1$

							String idx_fields[] = null;

							// Key lookup dimensions...
							if (!Const.isEmpty(keyLookup))
							{
								idx_fields = new String[keyLookup.length];
								for (int i = 0; i < keyLookup.length; i++)
                                {
									idx_fields[i] = keyLookup[i];
                                }
							}
							else
							{
								retval.setError(Messages.getString("DimensionLookupMeta.ReturnValue.NoKeyFieldsSpecified")); //$NON-NLS-1$
							}

							if (!Const.isEmpty(idx_fields) && !db.checkIndexExists(schemaName, tableName, idx_fields))
							{
								String indexname = "idx_" + tableName + "_lookup"; //$NON-NLS-1$ //$NON-NLS-2$
								sql += db.getCreateIndexStatement(schemaName, tableName, indexname, idx_fields, false, false, false, true);
							}

							// (Bitmap) index on technical key
							idx_fields = new String[] { keyField };
							if (!Const.isEmpty(keyField))
							{
								if (!db.checkIndexExists(schemaName, tableName, idx_fields))
								{
									String indexname = "idx_" + tableName + "_tk"; //$NON-NLS-1$ //$NON-NLS-2$
									sql += db.getCreateIndexStatement(schemaName, tableName, indexname, idx_fields, true, false, true, true);
								}
							}
							else
							{
								retval.setError(Messages.getString("DimensionLookupMeta.ReturnValue.TechnicalKeyFieldRequired")); //$NON-NLS-1$
							}

							// The optional Oracle sequence
							if ( CREATION_METHOD_SEQUENCE.equals(getTechKeyCreation()) && !Const.isEmpty(sequenceName))
							{
							    if (!db.checkSequenceExists(schemaName, sequenceName))
							    {
								    sql += db.getCreateSequenceStatement(schemaName, sequenceName, 1L, 1L, -1L, true);
							    }
							}

							if (sql.length() == 0)
								retval.setSQL(null);
							else
								retval.setSQL(transMeta.environmentSubstitute(sql));
						}
						catch (KettleDatabaseException dbe)
						{
							retval.setError(Messages.getString("DimensionLookupMeta.ReturnValue.ErrorOccurred") + dbe.getMessage()); //$NON-NLS-1$
						}
						finally
						{
							db.disconnect();
						}
					}
					else
					{
						retval.setError(Messages.getString("DimensionLookupMeta.ReturnValue.NoTableDefinedOnConnection")); //$NON-NLS-1$
					}
				}
				else
				{
					retval
							.setError(Messages.getString("DimensionLookupMeta.ReturnValue.NotReceivingAnyFields")); //$NON-NLS-1$
				}
			}
			else
			{
				retval.setError(Messages.getString("DimensionLookupMeta.ReturnValue.NoConnectionDefiendInStep")); //$NON-NLS-1$
			}
		}

		return retval;
	}
    
    public void analyseImpact(List<DatabaseImpact> impact, TransMeta transMeta, StepMeta stepMeta, RowMetaInterface prev, String input[], String output[], RowMetaInterface info)
    {
        if (prev!=null)
        {
            if (!update)
            {
                // Lookup: we do a lookup on the natural keys + the return fields!
                for (int i=0;i<keyLookup.length;i++)
                {
                    ValueMetaInterface v = prev.searchValueMeta(keyStream[i]);
                    
                    DatabaseImpact ii = new DatabaseImpact( DatabaseImpact.TYPE_IMPACT_READ, 
                            transMeta.getName(),
                            stepMeta.getName(),
                            databaseMeta.getDatabaseName(),
                            tableName, 
                            keyLookup[i],
                            keyStream[i],
                            v!=null?v.getOrigin():"?", //$NON-NLS-1$
                            "", //$NON-NLS-1$
                            "Type = "+v.toStringMeta() //$NON-NLS-1$
                            );
                    impact.add(ii);
                }
                
                // Return fields...
                for (int i=0;i<fieldLookup.length ;i++)
                {
                    ValueMetaInterface v = prev.searchValueMeta(fieldStream[i]);
                    
                    DatabaseImpact ii = new DatabaseImpact( DatabaseImpact.TYPE_IMPACT_READ, 
                            transMeta.getName(),
                            stepMeta.getName(),
                            databaseMeta.getDatabaseName(),
                            tableName,
                            fieldLookup[i],
                            fieldLookup[i],
                            v!=null?v.getOrigin():"?", //$NON-NLS-1$
                            "", //$NON-NLS-1$
                            "Type = "+v.toStringMeta() //$NON-NLS-1$
                            );
                    impact.add(ii);
                }
            }
            else
            {
               // Update: insert/update on all specified fields...
                // Lookup: we do a lookup on the natural keys + the return fields!
                for (int i=0;i<keyLookup.length;i++)
                {
                    ValueMetaInterface v = prev.searchValueMeta(keyStream[i]);
                    
                    DatabaseImpact ii = new DatabaseImpact( DatabaseImpact.TYPE_IMPACT_READ_WRITE, 
                            transMeta.getName(),
                            stepMeta.getName(),
                            databaseMeta.getDatabaseName(),
                            tableName, 
                            keyLookup[i],
                            keyStream[i],
                            v.getOrigin(),
                            "", //$NON-NLS-1$
                            "Type = "+v.toStringMeta() //$NON-NLS-1$
                            );
                    impact.add(ii);
                }
                
                // Return fields...
                for (int i=0;i<fieldLookup.length ;i++)
                {
                    ValueMetaInterface v = prev.searchValueMeta(fieldStream[i]);
                    
                    DatabaseImpact ii = new DatabaseImpact( DatabaseImpact.TYPE_IMPACT_READ_WRITE, 
                            transMeta.getName(),
                            stepMeta.getName(),
                            databaseMeta.getDatabaseName(),
                            tableName,
                            fieldLookup[i],
                            fieldLookup[i],
                            v.getOrigin(),
                            "", //$NON-NLS-1$
                            "Type = "+v.toStringMeta() //$NON-NLS-1$
                            );
                    impact.add(ii);
                }
            }
        }
    }

	public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta tr, Trans trans)
	{
		return new DimensionLookup(stepMeta, stepDataInterface, cnr, tr, trans);
	}

	public StepDataInterface getStepData()
	{
		return new DimensionLookupData();
	}
    
    public DatabaseMeta[] getUsedDatabaseConnections()
    {
        if (databaseMeta!=null) 
        {
            return new DatabaseMeta[] { databaseMeta };
        }
        else
        {
            return super.getUsedDatabaseConnections();
        }
    }

    /**
     * @return the schemaName
     */
    public String getSchemaName()
    {
        return schemaName;
    }

    /**
     * @param schemaName the schemaName to set
     */
    public void setSchemaName(String schemaName)
    {
        this.schemaName = schemaName;
    }

    /**
     * @return the cacheSize
     */
    public int getCacheSize()
    {
        return cacheSize;
    }

    /**
     * @param cacheSize the cacheSize to set
     */
    public void setCacheSize(int cacheSize)
    {
        this.cacheSize = cacheSize;
    }

	/**
	 * @return the usingStartDateAlternative
	 */
	public boolean isUsingStartDateAlternative() {
		return usingStartDateAlternative;
	}

	/**
	 * @param usingStartDateAlternative the usingStartDateAlternative to set
	 */
	public void setUsingStartDateAlternative(boolean usingStartDateAlternative) {
		this.usingStartDateAlternative = usingStartDateAlternative;
	}

	/**
	 * @return the startDateAlternative
	 */
	public int getStartDateAlternative() {
		return startDateAlternative;
	}

	/**
	 * @param startDateAlternative the startDateAlternative to set
	 */
	public void setStartDateAlternative(int startDateAlternative) {
		this.startDateAlternative = startDateAlternative;
	}

	/**
	 * @return the startDateFieldName
	 */
	public String getStartDateFieldName() {
		return startDateFieldName;
	}

	/**
	 * @param startDateFieldName the startDateFieldName to set
	 */
	public void setStartDateFieldName(String startDateFieldName) {
		this.startDateFieldName = startDateFieldName;
	}

	/**
	 * @return the preloadingCache
	 */
	public boolean isPreloadingCache() {
		return preloadingCache;
	}

	/**
	 * @param preloadingCache the preloadingCache to set
	 */
	public void setPreloadingCache(boolean preloadingCache) {
		this.preloadingCache = preloadingCache;
	}
}
