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

package be.ibridge.kettle.trans.step.dimensionlookup;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;

import org.eclipse.swt.widgets.Shell;
import org.w3c.dom.Node;

import be.ibridge.kettle.core.CheckResult;
import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.SQLStatement;
import be.ibridge.kettle.core.XMLHandler;
import be.ibridge.kettle.core.database.Database;
import be.ibridge.kettle.core.database.DatabaseMeta;
import be.ibridge.kettle.core.exception.KettleDatabaseException;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.exception.KettleStepException;
import be.ibridge.kettle.core.exception.KettleXMLException;
import be.ibridge.kettle.core.value.Value;
import be.ibridge.kettle.repository.Repository;
import be.ibridge.kettle.trans.DatabaseImpact;
import be.ibridge.kettle.trans.Trans;
import be.ibridge.kettle.trans.TransMeta;
import be.ibridge.kettle.trans.step.BaseStepMeta;
import be.ibridge.kettle.trans.step.StepDataInterface;
import be.ibridge.kettle.trans.step.StepDialogInterface;
import be.ibridge.kettle.trans.step.StepInterface;
import be.ibridge.kettle.trans.step.StepMeta;
import be.ibridge.kettle.trans.step.StepMetaInterface;

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

	public final static String	typeDesc[]						= { "Insert", "Update", "Punch through" };

	public final static String	typeDescLookup[]				= Value.getTypes();

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

    /** Default value in case nothing was found */
    private Value               notFound;

    /** The number of rows between commits */
    private int                 commitSize;

    /** The year to use as minus infinity in the dimensions date range */
    private int                 minYear; 
    
    /** The year to use as plus infinity in the dimensions date range */
    private int                 maxYear;
    
    
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
     * @param tablename
     *            The tablename to set.
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
     * @return Returns the notFound.
     */
    public Value getNotFound()
    {
        return notFound;
    }
    
    /**
     * @param notFound The notFound to set.
     */
    public void setNotFound(Value notFound)
    {
        this.notFound = notFound;
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
    
    
    
	public void loadXML(Node stepnode, ArrayList databases, Hashtable counters) throws KettleXMLException
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
			for (int i = 0; i < typeDesc.length; i++)
			{
				if (typeDesc[i].equalsIgnoreCase(ty))
					return i;
			}
			if ("Y".equalsIgnoreCase(ty))
				return TYPE_UPDATE_DIM_PUNCHTHROUGH;

			return TYPE_UPDATE_DIM_INSERT; // INSERT is the default: don't lose information.
		}
		else
		{
			int retval = Value.getType(ty);
			if (retval == Value.VALUE_TYPE_NONE)
				retval = Value.VALUE_TYPE_STRING;
			return retval;
		}
	}

	public final static String getUpdateType(boolean upd, int t)
	{
		if (!upd)
			return Value.getTypeDesc(t);
		else
			return typeDesc[t];
	}

	private void readData(Node stepnode, ArrayList databases) throws KettleXMLException
	{
		try
		{
			String upd;
			int nrkeys, nrfields;
			String commit;

			tableName = XMLHandler.getTagValue(stepnode, "table");
			String con = XMLHandler.getTagValue(stepnode, "connection");
			databaseMeta = Const.findDatabase(databases, con);
			commit = XMLHandler.getTagValue(stepnode, "commit");
			commitSize = Const.toInt(commit, 0);

			upd = XMLHandler.getTagValue(stepnode, "update");
			if (upd.equalsIgnoreCase("Y"))
				update = true;
			else
				update = false;

			Node fields = XMLHandler.getSubNode(stepnode, "fields");

			nrkeys = XMLHandler.countNodes(fields, "key");
			nrfields = XMLHandler.countNodes(fields, "field");

			allocate(nrkeys, nrfields);

			// Read keys to dimension
			for (int i = 0; i < nrkeys; i++)
			{
				Node knode = XMLHandler.getSubNodeByNr(fields, "key", i);

				keyStream[i] = XMLHandler.getTagValue(knode, "name");
				keyLookup[i] = XMLHandler.getTagValue(knode, "lookup");
			}

			// Only one date is supported
			// No datefield: use system date...
			Node dnode = XMLHandler.getSubNode(fields, "date");
			dateField = XMLHandler.getTagValue(dnode, "name");
			dateFrom = XMLHandler.getTagValue(dnode, "from");
			dateTo = XMLHandler.getTagValue(dnode, "to");

			for (int i = 0; i < nrfields; i++)
			{
				Node fnode = XMLHandler.getSubNodeByNr(fields, "field", i);

				fieldStream[i] = XMLHandler.getTagValue(fnode, "name");
				fieldLookup[i] = XMLHandler.getTagValue(fnode, "lookup");
				upd = XMLHandler.getTagValue(fnode, "update");
				fieldUpdate[i] = getUpdateType(update, upd);
			}

			if (update)
			{
				// If this is empty: use auto-increment field!
				sequenceName = XMLHandler.getTagValue(stepnode, "sequence");
			}

			maxYear = Const.toInt(XMLHandler.getTagValue(stepnode, "max_year"), Const.MAX_YEAR);
			minYear = Const.toInt(XMLHandler.getTagValue(stepnode, "min_year"), Const.MIN_YEAR);

			keyField = XMLHandler.getTagValue(fields, "return", "name");
			keyRename = XMLHandler.getTagValue(fields, "return", "rename");
			autoIncrement = !"N".equalsIgnoreCase(XMLHandler.getTagValue(fields, "return", "use_autoinc"));
			versionField = XMLHandler.getTagValue(fields, "return", "version");
		}
		catch (Exception e)
		{
			throw new KettleXMLException("Unable to load step info from XML", e);
		}
	}

	public void setDefault()
	{
		int nrkeys, nrfields;

		tableName = "dim table name";
		databaseMeta = null;
		commitSize = 0;
		update = true;

		nrkeys = 0;
		nrfields = 0;

		allocate(nrkeys, nrfields);

		// Read keys to dimension
		for (int i = 0; i < nrkeys; i++)
		{
			keyStream[i] = "key" + i;
			keyLookup[i] = "keylookup" + i;
		}

		for (int i = 0; i < nrfields; i++)
		{
			fieldStream[i] = "field" + i;
			fieldLookup[i] = "lookup" + i;
			fieldUpdate[i] = DimensionLookupMeta.TYPE_UPDATE_DIM_INSERT;
		}

		// Only one date is supported
		// No datefield: use system date...
		dateField = "";
		dateFrom = "date_from";
		dateTo = "date_to";

		minYear = Const.MIN_YEAR;
		maxYear = Const.MAX_YEAR;

		keyField = "";
		keyRename = "";
		autoIncrement = true;
		versionField = "version";
	}

	public Row getFields(Row r, String name, Row info) throws KettleStepException
	{
        LogWriter log = LogWriter.getInstance();
		Row row;
		if (r == null)
			row = new Row(); // give back values
		else
			row = r; // add to the existing row of values...

		Value v = new Value(keyField, Value.VALUE_TYPE_INTEGER);
		if (keyRename != null && keyRename.length() > 0)
			v.setName(keyRename);

		v.setLength(9, 0);
		v.setOrigin(name);
		row.addValue(v);

		// retrieve extra fields on lookup?
        // Don't bother if there are no return values specified.
		if (!update && fieldLookup.length>0)
		{
            try
            {
                // Get the rows from the table...
                if (databaseMeta!=null)
                {
                    Database db = new Database(databaseMeta);
                    Row extraFields = db.getTableFields(tableName);

                    for (int i = 0; i < fieldLookup.length; i++)
                    {
                        v = extraFields.searchValue(fieldLookup[i]);
                        if (v==null)
                        {
                            String message = "Unable to find return field ["+fieldLookup[i]+"] in the dimension table.";
                            log.logError(toString(), message);
                            throw new KettleStepException(message);
                        }
                        
                        // If the field needs to be renamed, rename
                        if (fieldStream[i] != null && fieldStream[i].length() > 0)
                        {
                            v.setName(fieldStream[i]);
                        }
                        v.setOrigin(name);
                        row.addValue(v);
                    }
                }
                else
                {
                    String message = "Unable to retrieve data type of return fields because no database connection was specified";
                    log.logError(toString(), message);
                    throw new KettleStepException(message);
                }
            }
            catch(Exception e)
            {
                String message = "Unable to retrieve data type of return fields because of an unexpected error";
                log.logError(toString(), message);
                throw new KettleStepException(message, e);
            }
   		}

		return row;
	}

	public String getXML()
	{
		String retval = "";
		int i;

		retval += "      " + XMLHandler.addTagValue("table", tableName);
		retval += "      " + XMLHandler.addTagValue("connection", databaseMeta == null ? "" : databaseMeta.getName());
		retval += "      " + XMLHandler.addTagValue("commit", commitSize);
		retval += "      " + XMLHandler.addTagValue("update", update);

		retval += "      <fields>" + Const.CR;
		for (i = 0; i < keyStream.length; i++)
		{
			retval += "        <key>" + Const.CR;
			retval += "          " + XMLHandler.addTagValue("name", keyStream[i]);
			retval += "          " + XMLHandler.addTagValue("lookup", keyLookup[i]);
			retval += "          </key>" + Const.CR;
		}

		retval += "        <date>" + Const.CR;
		retval += "          " + XMLHandler.addTagValue("name", dateField);
		retval += "          " + XMLHandler.addTagValue("from", dateFrom);
		retval += "          " + XMLHandler.addTagValue("to", dateTo);
		retval += "          </date>" + Const.CR;

		if (fieldStream != null)
			for (i = 0; i < fieldStream.length; i++)
			{
				if (fieldStream[i] != null)
				{
					retval += "        <field>" + Const.CR;
					retval += "          " + XMLHandler.addTagValue("name", fieldStream[i]);
					retval += "          " + XMLHandler.addTagValue("lookup", fieldLookup[i]);
					retval += "          " + XMLHandler.addTagValue("update", getUpdateType(update, fieldUpdate[i]));
					retval += "          </field>" + Const.CR;
				}
			}
		retval += "        <return>" + Const.CR;
		retval += "          " + XMLHandler.addTagValue("name", keyField);
		retval += "          " + XMLHandler.addTagValue("rename", keyRename);
		retval += "          " + XMLHandler.addTagValue("use_autoinc", autoIncrement);
		retval += "          " + XMLHandler.addTagValue("version", versionField);
		retval += "        </return>" + Const.CR;

		retval += "      </fields>" + Const.CR;

		// If sequence is empty: use auto-increment field!
		retval += "      " + XMLHandler.addTagValue("sequence", sequenceName);
		retval += "      " + XMLHandler.addTagValue("min_year", minYear);
		retval += "      " + XMLHandler.addTagValue("max_year", maxYear);

		return retval;
	}

	public void readRep(Repository rep, long id_step, ArrayList databases, Hashtable counters) throws KettleException
	{
		try
		{
			long id_connection = rep.getStepAttributeInteger(id_step, "id_connection");
			databaseMeta = Const.findDatabase(databases, id_connection);

			tableName = rep.getStepAttributeString(id_step, "table");
			commitSize = (int) rep.getStepAttributeInteger(id_step, "commit");
			update = rep.getStepAttributeBoolean(id_step, "update");

			int nrkeys = rep.countNrStepAttributes(id_step, "lookup_key_name");
			int nrfields = rep.countNrStepAttributes(id_step, "field_name");

			allocate(nrkeys, nrfields);

			for (int i = 0; i < nrkeys; i++)
			{
				keyStream[i] = rep.getStepAttributeString(id_step, i, "lookup_key_name");
				keyLookup[i] = rep.getStepAttributeString(id_step, i, "lookup_key_field");
			}

			dateField = rep.getStepAttributeString(id_step, "date_name");
			dateFrom = rep.getStepAttributeString(id_step, "date_from");
			dateTo = rep.getStepAttributeString(id_step, "date_to");

			for (int i = 0; i < nrfields; i++)
			{
				fieldStream[i] = rep.getStepAttributeString(id_step, i, "field_name");
				fieldLookup[i] = rep.getStepAttributeString(id_step, i, "field_lookup");
				fieldUpdate[i] = getUpdateType(update, rep.getStepAttributeString(id_step, i, "field_update"));
			}

			keyField = rep.getStepAttributeString(id_step, "return_name");
			keyRename = rep.getStepAttributeString(id_step, "return_rename");
			autoIncrement = rep.getStepAttributeBoolean(id_step, "use_autoinc");
			versionField = rep.getStepAttributeString(id_step, "version_field");

			sequenceName = rep.getStepAttributeString(id_step, "sequence");
			minYear = (int) rep.getStepAttributeInteger(id_step, "min_year");
			maxYear = (int) rep.getStepAttributeInteger(id_step, "max_year");
		}
		catch (Exception e)
		{
			throw new KettleException("Unexpected error reading step information from the repository", e);
		}
	}

	public void saveRep(Repository rep, long id_transformation, long id_step) throws KettleException
	{
		try
		{
			rep.saveStepAttribute(id_transformation, id_step, "table", tableName);
			rep
					.saveStepAttribute(id_transformation, id_step, "id_connection", databaseMeta == null ? -1 : databaseMeta
							.getID());
			rep.saveStepAttribute(id_transformation, id_step, "commit", commitSize);
			rep.saveStepAttribute(id_transformation, id_step, "update", update);

			for (int i = 0; i < keyStream.length; i++)
			{
				rep.saveStepAttribute(id_transformation, id_step, i, "lookup_key_name", keyStream[i]);
				rep.saveStepAttribute(id_transformation, id_step, i, "lookup_key_field", keyLookup[i]);
			}

			rep.saveStepAttribute(id_transformation, id_step, "date_name", dateField);
			rep.saveStepAttribute(id_transformation, id_step, "date_from", dateFrom);
			rep.saveStepAttribute(id_transformation, id_step, "date_to", dateTo);

			if (fieldStream != null)
				for (int i = 0; i < fieldStream.length; i++)
				{
					if (fieldStream[i] != null)
					{
						rep.saveStepAttribute(id_transformation, id_step, i, "field_name", fieldStream[i]);
						rep.saveStepAttribute(id_transformation, id_step, i, "field_lookup", fieldLookup[i]);
						rep.saveStepAttribute(id_transformation, id_step, i, "field_update",
												getUpdateType(update, fieldUpdate[i]));
					}
				}

			rep.saveStepAttribute(id_transformation, id_step, "return_name", keyField);
			rep.saveStepAttribute(id_transformation, id_step, "return_rename", keyRename);
			rep.saveStepAttribute(id_transformation, id_step, "use_autoinc", autoIncrement);
			rep.saveStepAttribute(id_transformation, id_step, "version_field", versionField);

			rep.saveStepAttribute(id_transformation, id_step, "sequence", sequenceName);
			rep.saveStepAttribute(id_transformation, id_step, "min_year", minYear);
			rep.saveStepAttribute(id_transformation, id_step, "max_year", maxYear);

			// Also, save the step-database relationship!
			if (databaseMeta != null)
				rep.insertStepDatabase(id_transformation, id_step, databaseMeta.getID());
		}
		catch (KettleDatabaseException dbe)
		{
			throw new KettleException("Unable to load dimension lookup info from the repository", dbe);
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

	public void check(ArrayList remarks, StepMeta stepinfo, Row prev, String input[], String output[], Row info)
	{
		if (update)
			checkUpdate(remarks, stepinfo, prev);
		else
			checkLookup(remarks, stepinfo, prev);

		// See if we have input streams leading to this step!
		if (input.length > 0)
		{
			CheckResult cr = new CheckResult(CheckResult.TYPE_RESULT_OK, "Step is receiving info from other steps.",
					stepinfo);
			remarks.add(cr);
		}
		else
		{
			CheckResult cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, "No input received from other steps!",
					stepinfo);
			remarks.add(cr);
		}
	}

	private void checkUpdate(ArrayList remarks, StepMeta stepinfo, Row prev)
	{
		LogWriter log = LogWriter.getInstance();
		
		CheckResult cr;
		String error_message = "";

		if (databaseMeta != null)
		{
			Database db = new Database(databaseMeta);
			try
			{
				db.connect();
				if (tableName != null && tableName.length() != 0)
				{
					boolean first = true;
					boolean error_found = false;
					error_message = "";

					Row r = db.getTableFields(tableName);
					if (r != null)
					{
						for (int i = 0; i < fieldLookup.length; i++)
						{
							String lufield = fieldLookup[i];
							log.logDebug(toString(), "Check lookupfield #" + i + " --> " + lufield
														+ " in lookup table...");
							Value v = r.searchValue(lufield);
							if (v == null)
							{
								if (first)
								{
									first = false;
									error_message += "Missing compare fields in target table:" + Const.CR;
								}
								error_found = true;
								error_message += "\t\t" + lufield + Const.CR;
							}
						}
						if (error_found)
						{
							cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, error_message, stepinfo);
						}
						else
						{
							cr = new CheckResult(CheckResult.TYPE_RESULT_OK,
									"All lookup fields found in the dimension table.", stepinfo);
						}
						remarks.add(cr);

						/* Also, check the fields: tk, version, from-to, ... */
                        if (keyField!=null && keyField.length()>0)
                        {
    						if (r.searchValueIndex(keyField) < 0)
    						{
    							error_message = "Technical key [" + keyField + "] not found in target dimension table."
    											+ Const.CR;
    							cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, error_message, stepinfo);
    						}
    						else
    						{
    							error_message = "Technical key [" + keyField + "] found in target dimension table."
    											+ Const.CR;
    							cr = new CheckResult(CheckResult.TYPE_RESULT_OK, error_message, stepinfo);
    						}
    						remarks.add(cr);
                        }
                        else
                        {
                            error_message = "Please specify a fieldname to store the technical/surrogate key of the dimension in." + Const.CR;
                            remarks.add( new CheckResult(CheckResult.TYPE_RESULT_ERROR, error_message, stepinfo) );
                        }

                        if (versionField != null && versionField.length() > 0)
						{
							if (r.searchValueIndex(versionField) < 0)
							{
								error_message = "Version field [" + versionField
												+ "] not found in target dimension table." + Const.CR;
								cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, error_message, stepinfo);
							}
							else
							{
								error_message = "Version field [" + versionField + "] found in target dimension table." + Const.CR;
								cr = new CheckResult(CheckResult.TYPE_RESULT_OK, error_message, stepinfo);
							}
							remarks.add(cr);
						}
                        else
                        {
                            error_message = "Please specify a fieldname to store the version of the dimension entry in." + Const.CR;
                            remarks.add( new CheckResult(CheckResult.TYPE_RESULT_ERROR, error_message, stepinfo) );
                        }

						if (dateFrom != null && dateFrom.length() > 0)
						{
							if (r.searchValueIndex(dateFrom) < 0)
							{
								error_message = "Start of daterange field [" + dateFrom
												+ "] not found in target dimension table." + Const.CR;
								cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, error_message, stepinfo);
							}
							else
							{
								error_message = "Start of daterange field [" + dateFrom
												+ "] found in target dimension table." + Const.CR;
								cr = new CheckResult(CheckResult.TYPE_RESULT_OK, error_message, stepinfo);
							}
							remarks.add(cr);
						}
                        else
                        {
                            error_message = "Please specify a fieldname to store the start of the date range of the dimension entry in." + Const.CR;
                            remarks.add( new CheckResult(CheckResult.TYPE_RESULT_ERROR, error_message, stepinfo) );
                        }

						if (dateTo != null && dateTo.length() > 0)
						{
							if (r.searchValueIndex(dateTo) < 0)
							{
								error_message = "End of daterange field [" + dateTo
												+ "] not found in target dimension table." + Const.CR;
								cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, error_message, stepinfo);
							}
							else
							{
								error_message = "End of daterange field [" + dateTo
												+ "] found in target dimension table." + Const.CR;
								cr = new CheckResult(CheckResult.TYPE_RESULT_OK, error_message, stepinfo);
							}
							remarks.add(cr);
						}
                        else
                        {
                            error_message = "Please specify a fieldname to store the end of the date range of the dimension entry in." + Const.CR;
                            remarks.add( new CheckResult(CheckResult.TYPE_RESULT_ERROR, error_message, stepinfo) );
                        }
					}
					else
					{
						error_message = "Couldn't read the table info, please check the table-name & permissions.";
						cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, error_message, stepinfo);
						remarks.add(cr);
					}
				}

				// Look up fields in the input stream <prev>
				if (prev != null && prev.size() > 0)
				{
					boolean first = true;
					error_message = "";
					boolean error_found = false;

					for (int i = 0; i < fieldStream.length; i++)
					{
						log.logDebug(toString(), "Check field #" + i + " --> " + fieldStream[i]
													+ ", in inputstream from previous steps");
						Value v = prev.searchValue(fieldStream[i]);
						if (v == null)
						{
							if (first)
							{
								first = false;
								error_message += "Missing fields, not found in input from previous steps:" + Const.CR;
							}
							error_found = true;
							error_message += "\t\t" + fieldStream[i] + Const.CR;
						}
					}
					if (error_found)
					{
						cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, error_message, stepinfo);
					}
					else
					{
						cr = new CheckResult(CheckResult.TYPE_RESULT_OK, "All fields found in the input stream.",
								stepinfo);
					}
					remarks.add(cr);
				}
				else
				{
					error_message = "Couldn't read fields from the previous step." + Const.CR;
					cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, error_message, stepinfo);
					remarks.add(cr);
				}

				// Check sequence
				if (databaseMeta.supportsSequences() && sequenceName != null && sequenceName.length() != 0)
				{
					if (db.checkSequenceExists(sequenceName))
					{
						error_message = "Sequence " + sequenceName + " exists.";
						cr = new CheckResult(CheckResult.TYPE_RESULT_OK, error_message, stepinfo);
						remarks.add(cr);
					}
					else
					{
						error_message += "Sequence " + sequenceName + " couldn't be found!";
						cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, error_message, stepinfo);
						remarks.add(cr);
					}
				}
			}
			catch (KettleException e)
			{
				error_message = "Couldn't connect to database, please check the connection: " + e.getMessage();
				cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, error_message, stepinfo);
				remarks.add(cr);
			}
		}
		else
		{
			error_message = "Please select a connection name!";
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, error_message, stepinfo);
			remarks.add(cr);
		}
	}

	private void checkLookup(ArrayList remarks, StepMeta stepinfo, Row prev)
	{
		int i;
		boolean error_found = false;
		String error_message = "";
		boolean first;
		CheckResult cr;

		if (databaseMeta != null)
		{
			Database db = new Database(databaseMeta);
			try
			{
				db.connect();

				if (tableName != null && tableName.length() != 0)
				{
					Row tableFields = db.getTableFields(tableName);
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
								Value strvalue = prev.searchValue(strfield); // 
								if (strvalue == null)
								{
									if (first)
									{
										first = false;
										error_message += "Keys with a problem:" + Const.CR;
									}
									error_found = true;
									error_message += "\t\t" + keyField + " (not present in input stream)" + Const.CR;
								}
								else
								{
									// does the field exist in the dimension table?
									String dimfield = keyLookup[i];
									Value dimvalue = tableFields.searchValue(dimfield);
									if (dimvalue == null)
									{
										if (first)
										{
											first = false;
											error_message += "Keys with a problem:" + Const.CR;
										}
										error_found = true;
										error_message += "\t\t" + dimfield + " (not present in dimension table "
															+ tableName + ")" + Const.CR;
									}
									else
									{
										// Is the streamvalue of the same type as the dimension value?
										if (strvalue.getType() != dimvalue.getType())
										{
											if (first)
											{
												first = false;
												error_message += "Keys with a problem:" + Const.CR;
											}
											warning_found = true;
											error_message += "\t\t" + strfield + " (" + strvalue.getOrigin()
																+ ") is not of the same type as " + dimfield + " ("
																+ tableName + ")" + Const.CR;
											error_message += "\t\tThis is a warning and in many cases (for ex. Oracle) the conversion is handled by the database.";
										}
									}
								}
							}
							if (error_found)
							{
								cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, error_message, stepinfo);
							}
							else
								if (warning_found)
								{
									cr = new CheckResult(CheckResult.TYPE_RESULT_WARNING, error_message, stepinfo);
								}
								else
								{
									cr = new CheckResult(
											CheckResult.TYPE_RESULT_OK,
											"All keys fields found in the input stream and dimension table. (with matching types)",
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
									Value v = tableFields.searchValue(lufield);
									if (v == null)
									{
										if (first)
										{
											first = false;
											error_message += "Fields to retrieve that don't exist in the dimension:"
																+ Const.CR;
										}
										error_found = true;
										error_message += "\t\t" + lufield + Const.CR;
									}
								}
							}
							if (error_found)
							{
								cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, error_message, stepinfo);
							}
							else
							{
								cr = new CheckResult(CheckResult.TYPE_RESULT_OK,
										"All fields to retrieve are found in the dimension.", stepinfo);
							}
							remarks.add(cr);

							/* Also, check the fields: tk, version, from-to, ... */
							if (tableFields.searchValueIndex(keyField) < 0)
							{
								error_message = "Technical key [" + keyField + "] not found in dimension lookup table."
												+ Const.CR;
								cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, error_message, stepinfo);
							}
							else
							{
								error_message = "Technical key [" + keyField + "] found in dimension lookup table."
												+ Const.CR;
								cr = new CheckResult(CheckResult.TYPE_RESULT_OK, error_message, stepinfo);
							}
							remarks.add(cr);

							if (tableFields.searchValueIndex(versionField) < 0)
							{
								error_message = "Version field [" + versionField
												+ "] not found in dimension lookup table." + Const.CR;
								cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, error_message, stepinfo);
							}
							else
							{
								error_message = "Version field [" + versionField + "] found in dimension lookup table."
												+ Const.CR;
								cr = new CheckResult(CheckResult.TYPE_RESULT_OK, error_message, stepinfo);
							}
							remarks.add(cr);

							if (tableFields.searchValueIndex(dateFrom) < 0)
							{
								error_message = "Start of daterange field [" + dateFrom
												+ "] not found in dimension lookup table." + Const.CR;
								cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, error_message, stepinfo);
							}
							else
							{
								error_message = "Start of daterange field [" + dateFrom
												+ "] found in dimension lookup table." + Const.CR;
								cr = new CheckResult(CheckResult.TYPE_RESULT_OK, error_message, stepinfo);
							}
							remarks.add(cr);

							if (tableFields.searchValueIndex(dateTo) < 0)
							{
								error_message = "End of daterange field [" + dateTo
												+ "] not found in dimension lookup table." + Const.CR;
								cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, error_message, stepinfo);
							}
							else
							{
								error_message = "End of daterange field [" + dateTo
												+ "] found in dimension lookup table.";
								cr = new CheckResult(CheckResult.TYPE_RESULT_OK, error_message, stepinfo);
							}
							remarks.add(cr);
						}
						else
						{
							error_message = "Couldn't read fields from the previous step." + Const.CR;
							cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, error_message, stepinfo);
							remarks.add(cr);
						}
					}
					else
					{
						error_message = "Couldn't read the table info, please check the table-name & permissions.";
						cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, error_message, stepinfo);
						remarks.add(cr);
					}
				}
			}
			catch (KettleException e)
			{
				error_message = "Couldn't connect to database, please check the connection: " + e.getMessage();
				cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, error_message, stepinfo);
				remarks.add(cr);
			}
		}
		else
		{
			error_message = "Please select or create a connection!";
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, error_message, stepinfo);
			remarks.add(cr);
		}
	}

	public Row getTableFields()
	{
		LogWriter log = LogWriter.getInstance();
		Row fields = null;
		if (databaseMeta != null)
		{
			Database db = new Database(databaseMeta);
			try
			{
				db.connect();
				fields = db.getTableFields(tableName);
			}
			catch (KettleDatabaseException dbe)
			{
				log.logError(toString(), "A database error occurred: " + dbe.getMessage());
			}
			finally
			{
				db.disconnect();
			}
		}
		return fields;
	}

	public SQLStatement getSQLStatements(TransMeta transMeta, StepMeta stepMeta, Row prev)
	{
		LogWriter log = LogWriter.getInstance();
		SQLStatement retval = new SQLStatement(stepMeta.getName(), databaseMeta, null); // default: nothing to do!

		if (update) // Only bother in case of update, not lookup!
		{
			log.logDebug(toString(), "Update!");
			if (databaseMeta != null)
			{
				if (prev != null && prev.size() > 0)
				{
					if (tableName != null && tableName.length() > 0)
					{
						Database db = new Database(databaseMeta);
						try
						{
							db.connect();

							String sql = "";

							// How does the table look like?
							//
							Row fields = new Row();

							// First the technical key
							//
							Value vkeyfield = new Value(keyField, Value.VALUE_TYPE_INTEGER);
							vkeyfield.setLength(10);
							fields.addValue(vkeyfield);

							// The the version
							//
							Value vversion = new Value(versionField, Value.VALUE_TYPE_INTEGER);
							vversion.setLength(5);
							fields.addValue(vversion);

							// The date from
							//
							Value vdatefrom = new Value(dateFrom, Value.VALUE_TYPE_DATE);
							fields.addValue(vdatefrom);

							// The date to
							//
							Value vdateto = new Value(dateTo, Value.VALUE_TYPE_DATE);
							fields.addValue(vdateto);

							String errors = "";

							// Then the keys
							//
							for (int i = 0; i < keyLookup.length; i++)
							{
								Value vprev = prev.searchValue(keyStream[i]);
								if (vprev != null)
								{
									Value field = new Value(vprev);
									field.setName(keyLookup[i]);
									fields.addValue(field);
								}
								else
								{
									if (errors.length() > 0)
										errors += ", ";
									errors += keyStream[i];
								}
							}

							//
							// Then the fields to update...
							//
							for (int i = 0; i < fieldLookup.length; i++)
							{
								Value vprev = prev.searchValue(fieldStream[i]);
								if (vprev != null)
								{
									Value field = new Value(vprev);
									field.setName(fieldLookup[i]);
									fields.addValue(field);
								}
								else
								{
									if (errors.length() > 0)
										errors += ", ";
									errors += fieldStream[i];
								}
							}

							if (errors.length() > 0)
							{
								retval.setError("Unable to find these fields in the input stream: " + errors);
							}

							log
									.logDebug(toString(), "Get DDL for table [" + tableName + "] : "
															+ fields.toStringMeta());

							sql += db.getDDL(tableName, fields, (sequenceName != null && sequenceName.length() == 0) ? keyField
																											: null,
												autoIncrement, null, true);

							log.logDebug(toString(), "sql = " + sql);

							String idx_fields[] = null;

							// Key lookup dimensions...
							if (keyLookup != null && keyLookup.length > 0)
							{
								idx_fields = new String[keyLookup.length];
								for (int i = 0; i < keyLookup.length; i++)
									idx_fields[i] = keyLookup[i];
							}
							else
							{
								retval
										.setError("No key fields are specified.  Please specify the fields use as key for this dimension.");
							}

							if (idx_fields != null && idx_fields.length > 0
								&& !db.checkIndexExists(tableName, idx_fields))
							{
								String indexname = "idx_" + tableName + "_lookup";
								sql += db.getCreateIndexStatement(tableName, indexname, idx_fields, false, false,
																	false, true);
							}

							// (Bitmap) index on technical key
							idx_fields = new String[] { keyField };
							if (keyField != null && keyField.length() > 0)
							{
								if (!db.checkIndexExists(tableName, idx_fields))
								{
									String indexname = "idx_" + tableName + "_tk";
									sql += db.getCreateIndexStatement(tableName, indexname, idx_fields, true, false,
																		true, true);
								}
							}
							else
							{
								retval
										.setError("Please specifiy the name of the technical key field (a.k.a. the surrogate key)");
							}

							// The optional Oracle sequence
							if (!db.checkSequenceExists(sequenceName))
							{
								sql += db.getCreateSequenceStatement(sequenceName, 1L, 1L, -1L, true);
							}

							if (sql.length() == 0)
								retval.setSQL(null);
							else
								retval.setSQL(sql);
						}
						catch (KettleDatabaseException dbe)
						{
							retval.setError("An error occurred: " + dbe.getMessage());
						}
						finally
						{
							db.disconnect();
						}
					}
					else
					{
						retval.setError("No table is defined on this connection.");
					}
				}
				else
				{
					retval
							.setError("Not receiving any fields from previous steps. Check the previous steps for errors & the connecting hops.");
				}
			}
			else
			{
				retval.setError("There is no connection defined in this step.");
			}
		}

		return retval;
	}
    
    public void analyseImpact(ArrayList impact, TransMeta transMeta, StepMeta stepMeta, Row prev, String input[], String output[], Row info)
    {
        if (prev!=null)
        {
            if (!update)
            {
                // Lookup: we do a lookup on the natural keys + the return fields!
                for (int i=0;i<keyLookup.length;i++)
                {
                    Value v = prev.searchValue(keyStream[i]);
                    
                    DatabaseImpact ii = new DatabaseImpact( DatabaseImpact.TYPE_IMPACT_READ, 
                            transMeta.getName(),
                            stepMeta.getName(),
                            databaseMeta.getDatabaseName(),
                            tableName, 
                            keyLookup[i],
                            keyStream[i],
                            v!=null?v.getOrigin():"?",
                            "",
                            "Type = "+v.toStringMeta()
                            );
                    impact.add(ii);
                }
                
                // Return fields...
                for (int i=0;i<fieldLookup.length ;i++)
                {
                    Value v = prev.searchValue(fieldStream[i]);
                    
                    DatabaseImpact ii = new DatabaseImpact( DatabaseImpact.TYPE_IMPACT_READ, 
                            transMeta.getName(),
                            stepMeta.getName(),
                            databaseMeta.getDatabaseName(),
                            tableName,
                            fieldLookup[i],
                            fieldLookup[i],
                            v!=null?v.getOrigin():"?",
                            "",
                            "Type = "+v.toStringMeta()
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
                    Value v = prev.searchValue(keyStream[i]);
                    
                    DatabaseImpact ii = new DatabaseImpact( DatabaseImpact.TYPE_IMPACT_READ_WRITE, 
                            transMeta.getName(),
                            stepMeta.getName(),
                            databaseMeta.getDatabaseName(),
                            tableName, 
                            keyLookup[i],
                            keyStream[i],
                            v.getOrigin(),
                            "",
                            "Type = "+v.toStringMeta()
                            );
                    impact.add(ii);
                }
                
                // Return fields...
                for (int i=0;i<fieldLookup.length ;i++)
                {
                    Value v = prev.searchValue(fieldStream[i]);
                    
                    DatabaseImpact ii = new DatabaseImpact( DatabaseImpact.TYPE_IMPACT_READ_WRITE, 
                            transMeta.getName(),
                            stepMeta.getName(),
                            databaseMeta.getDatabaseName(),
                            tableName,
                            fieldLookup[i],
                            fieldLookup[i],
                            v.getOrigin(),
                            "",
                            "Type = "+v.toStringMeta()
                            );
                    impact.add(ii);
                }
            }
        }
    }


	public StepDialogInterface getDialog(Shell shell, StepMetaInterface info, TransMeta transMeta, String name)
	{
		return new DimensionLookupDialog(shell, info, transMeta, name);
	}

	public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta tr, Trans trans)
	{
		return new DimensionLookup(stepMeta, stepDataInterface, cnr, tr, trans);
	}

	public StepDataInterface getStepData()
	{
		return new DimensionLookupData();
	}

}

