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

package be.ibridge.kettle.trans.step.combinationlookup;

import java.util.ArrayList;
import java.util.Hashtable;

import org.eclipse.swt.widgets.Shell;
import org.w3c.dom.Node;

import be.ibridge.kettle.core.CheckResult;
import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.SQLStatement;
import be.ibridge.kettle.core.XMLHandler;
import be.ibridge.kettle.core.database.Database;
import be.ibridge.kettle.core.database.DatabaseMeta;
import be.ibridge.kettle.core.exception.KettleException;
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

/*
 * Created on 14-may-2003
 *
 * TODO: In the distant future the use_autoinc flag should be removed since its
 *       functionality is now taken over by techKeyCreation (which is cleaner).
 */
public class CombinationLookupMeta extends BaseStepMeta implements StepMetaInterface
{
	/** Default cache size: 0 will cache everything */
	public final static int DEFAULT_CACHE_SIZE  = 9999;

    /** what's the lookup schema? */
    private String  schemaName;

	/** what's the lookup table? */
	private String  tablename;

	/** database connection */
	private DatabaseMeta  database;

	/**	replace fields with technical key? */
	private boolean replaceFields;

	/** which fields do we use to look up a value? */
	private String  keyField[];

	/** With which fields in dimension do we look up? */
	private String  keyLookup[];

	/** Use checksum algorithm to limit index size? */
	private boolean useHash;

	/** Name of the CRC field in the dimension */
	private String  hashField;

	/** Technical Key field to return */
	private String  technicalKeyField;

	/** Where to get the sequence from... */
	private String  sequenceFrom;

	/** Commit size for insert / update */
	private int     commitSize;

	/** Limit the cache size to this! */
	private int                 cacheSize;      
	
	/** Use the auto-increment feature of the database to generate keys. */
	private boolean useAutoinc;

	/** Which method to use for the creation of the tech key */
	private String techKeyCreation = null;

	public static String CREATION_METHOD_AUTOINC  = "autoinc";
	public static String CREATION_METHOD_SEQUENCE = "sequence";
	public static String CREATION_METHOD_TABLEMAX = "tablemax";

	public CombinationLookupMeta()
	{
		super(); // allocate BaseStepMeta
	}

	/**
	 * @return Returns the database.
	 */
	public DatabaseMeta getDatabase()
	{
		return database;
	}

	/**
	 * @param database The database to set.
	 */
	public void setDatabase(DatabaseMeta database)
	{
		this.database = database;
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
	 * @return Returns the cacheSize.
	 */
	public int getCacheSize()
	{
		return cacheSize;
	}
	
	/**
	 * @param cacheSize The cacheSize to set.
	 */
	public void setCacheSize(int cacheSize)
	{
		this.cacheSize = cacheSize;
	}    
		
	/**
	 * @return Returns the hashField.
	 */
	public String getHashField()
	{
		return hashField;
	}

	/**
	 * @param hashField The hashField to set.
	 */
	public void setHashField(String hashField)
	{
		this.hashField = hashField;
	}

	/**
	 * @return Returns the keyField (names in the stream).
	 */
	public String[] getKeyField()
	{
		return keyField;
	}

	/**
	 * @param keyField The keyField to set.
	 */
	public void setKeyField(String[] keyField)
	{
		this.keyField = keyField;
	}

	/**
	 * @return Returns the keyLookup (names in the dimension table)
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
	 * @return Returns the replaceFields.
	 */
	public boolean replaceFields()
	{
		return replaceFields;
	}

	/**
	 * @param replaceFields The replaceFields to set.
	 */
	public void setReplaceFields(boolean replaceFields)
	{
		this.replaceFields = replaceFields;
	}

	/**
	 * @return Returns the sequenceFrom.
	 */
	public String getSequenceFrom()
	{
		return sequenceFrom;
	}

	/**
	 * @param sequenceFrom The sequenceFrom to set.
	 */
	public void setSequenceFrom(String sequenceFrom)
	{
		this.sequenceFrom = sequenceFrom;
	}

	/**
	 * @return Returns the tablename.
	 */
	public String getTablename()
	{
		return tablename;
	}

	/**
	 * @param tablename The tablename to set.
	 */
	public void setTablename(String tablename)
	{
		this.tablename = tablename;
	}

	/**
	 * @return Returns the technicalKeyField.
	 */
	public String getTechnicalKeyField()
	{
		return technicalKeyField;
	}

	/**
	 * @param technicalKeyField The technicalKeyField to set.
	 */
	public void setTechnicalKeyField(String technicalKeyField)
	{
		this.technicalKeyField = technicalKeyField;
	}

	/**
	 * @return Returns the useAutoinc.
	 */
	public boolean isUseAutoinc()
	{
		return useAutoinc;
	}

	/**
	 * @param useAutoinc The useAutoinc to set.
	 */
	public void setUseAutoinc(boolean useAutoinc)
	{
		this.useAutoinc = useAutoinc;
	}

	/**
	 * @return Returns the useHash.
	 */
	public boolean useHash()
	{
		return useHash;
	}

	/**
	 * @param useHash The useHash to set.
	 */
	public void setUseHash(boolean useHash)
	{
		this.useHash = useHash;
	}

	public void loadXML(Node stepnode, ArrayList databases, Hashtable counters)
		throws KettleXMLException
	{
		readData(stepnode, databases);
	}

	public void allocate(int nrkeys)
	{
		keyField  = new String[nrkeys];
		keyLookup = new String[nrkeys];
	}

	public Object clone()
	{
		CombinationLookupMeta retval = (CombinationLookupMeta)super.clone();

		int nrkeys    = keyField.length;

		retval.allocate(nrkeys);

		// Read keys to dimension
		for (int i=0;i<nrkeys;i++)
		{
			retval.keyField[i] = keyField[i];
			retval.keyLookup[i]= keyLookup[i];
		}

		return retval;
	}

	private void readData(Node stepnode, ArrayList databases)
		throws KettleXMLException
	{
		try
		{
			String commit, csize;

            schemaName  = XMLHandler.getTagValue(stepnode, "schema"); //$NON-NLS-1$
			tablename  = XMLHandler.getTagValue(stepnode, "table"); //$NON-NLS-1$
			String con = XMLHandler.getTagValue(stepnode, "connection"); //$NON-NLS-1$
			database = DatabaseMeta.findDatabase(databases, con);
			commit     = XMLHandler.getTagValue(stepnode, "commit"); //$NON-NLS-1$
			commitSize = Const.toInt(commit, 0);
			csize      = XMLHandler.getTagValue(stepnode, "cache_size"); //$NON-NLS-1$
			cacheSize  = Const.toInt(csize, 0);

			replaceFields ="Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "replace")); //$NON-NLS-1$ //$NON-NLS-2$
			useHash    ="Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "crc")); //$NON-NLS-1$ //$NON-NLS-2$

			hashField  = XMLHandler.getTagValue(stepnode, "crcfield"); //$NON-NLS-1$

			Node keys = XMLHandler.getSubNode(stepnode, "fields"); //$NON-NLS-1$
			int nrkeys    = XMLHandler.countNodes(keys, "key"); //$NON-NLS-1$

			allocate(nrkeys);

			// Read keys to dimension
			for (int i=0;i<nrkeys;i++)
			{
				Node knode = XMLHandler.getSubNodeByNr(keys, "key", i); //$NON-NLS-1$
				keyField[i]      = XMLHandler.getTagValue(knode, "name"); //$NON-NLS-1$
				keyLookup[i]= XMLHandler.getTagValue(knode, "lookup"); //$NON-NLS-1$
			}

			// If this is empty: use auto-increment field!
			sequenceFrom = XMLHandler.getTagValue(stepnode, "sequence"); //$NON-NLS-1$

			Node fields = XMLHandler.getSubNode(stepnode, "fields"); //$NON-NLS-1$
			Node retkey = XMLHandler.getSubNode(fields, "return"); //$NON-NLS-1$
			technicalKeyField = XMLHandler.getTagValue(retkey, "name"); //$NON-NLS-1$
			useAutoinc = !"N".equalsIgnoreCase(XMLHandler.getTagValue(retkey, "use_autoinc")); //$NON-NLS-1$ //$NON-NLS-2$

			setTechKeyCreation(XMLHandler.getTagValue(retkey, "creation_method")); //$NON-NLS-1$
		}
		catch(Exception e)
		{
			throw new KettleXMLException(Messages.getString("CombinationLookupMeta.Exception.UnableToLoadStepInfo"), e); //$NON-NLS-1$
		}
	}

	public void setDefault()
	{
        schemaName    = ""; //$NON-NLS-1$
		tablename     = Messages.getString("CombinationLookupMeta.DimensionTableName.Label"); //$NON-NLS-1$
		database      = null;
		commitSize    = 100;
		cacheSize     = DEFAULT_CACHE_SIZE;
		replaceFields = false;
		useHash       = false;
		hashField     = "hashcode"; //$NON-NLS-1$
		int nrkeys    = 0;

		allocate(nrkeys);

		// Read keys to dimension
		for (int i=0;i<nrkeys;i++)
		{
			keyField[i] ="key"+i; //$NON-NLS-1$
			keyLookup[i]="keylookup"+i; //$NON-NLS-1$
		}

		technicalKeyField = "technical/surrogate key field"; //$NON-NLS-1$
		useAutoinc  = false;
	}

	public Row getFields(Row r, String name, Row info)
	{
		Row row;
		if (r==null) row=new Row(); // give back values
		else         row=r;         // add to the existing row of values...

		Value v=new Value(technicalKeyField, Value.VALUE_TYPE_INTEGER);
		v.setLength(10,0);
		v.setOrigin(name);
		row.addValue(v);

		if (replaceFields)
		{
			for (int i=0;i<keyField.length;i++)
			{
				int idx = r.searchValueIndex(keyField[i]);
				if (idx>=0)
				{
					row.removeValue(idx);
				}
			}
		}

		return row;
	}

	public String getXML()
	{
        StringBuffer retval = new StringBuffer(512);

        retval.append("      ").append(XMLHandler.addTagValue("schema", schemaName)); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("      ").append(XMLHandler.addTagValue("table", tablename)); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("      ").append(XMLHandler.addTagValue("connection", database==null?"":database.getName())); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		retval.append("      ").append(XMLHandler.addTagValue("commit", commitSize)); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("      ").append(XMLHandler.addTagValue("cache_size", cacheSize)); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("      ").append(XMLHandler.addTagValue("replace", replaceFields)); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("      ").append(XMLHandler.addTagValue("crc", useHash)); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("      ").append(XMLHandler.addTagValue("crcfield", hashField)); //$NON-NLS-1$ //$NON-NLS-2$

		retval.append("      <fields>").append(Const.CR); //$NON-NLS-1$
		for (int i=0;i<keyField.length;i++)
		{
			retval.append("        <key>").append(Const.CR); //$NON-NLS-1$
			retval.append("          ").append(XMLHandler.addTagValue("name",   keyField[i])); //$NON-NLS-1$ //$NON-NLS-2$
			retval.append("          ").append(XMLHandler.addTagValue("lookup", keyLookup[i])); //$NON-NLS-1$ //$NON-NLS-2$
			retval.append("        </key>").append(Const.CR); //$NON-NLS-1$
		}

		retval.append("        <return>").append(Const.CR); //$NON-NLS-1$
		retval.append("          ").append(XMLHandler.addTagValue("name", technicalKeyField)); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("          ").append(XMLHandler.addTagValue("creation_method", techKeyCreation)); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("          ").append(XMLHandler.addTagValue("use_autoinc", useAutoinc)); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("        </return>").append(Const.CR); //$NON-NLS-1$

		retval.append("      </fields>").append(Const.CR); //$NON-NLS-1$

		// If sequence is empty: use auto-increment field!
		retval.append("      ").append(XMLHandler.addTagValue("sequence", sequenceFrom)); //$NON-NLS-1$ //$NON-NLS-2$

		return retval.toString();
	}

	public void readRep(Repository rep, long id_step, ArrayList databases, Hashtable counters)
		throws KettleException
	{
		try
		{
			long id_connection =   rep.getStepAttributeInteger(id_step, "id_connection");  //$NON-NLS-1$
			database = DatabaseMeta.findDatabase( databases, id_connection);

            schemaName       =      rep.getStepAttributeString (id_step, "schema"); //$NON-NLS-1$
			tablename        =      rep.getStepAttributeString (id_step, "table"); //$NON-NLS-1$
			commitSize       = (int)rep.getStepAttributeInteger(id_step, "commit"); //$NON-NLS-1$
			cacheSize        = (int)rep.getStepAttributeInteger(id_step, "cache_size"); //$NON-NLS-1$
			replaceFields    =      rep.getStepAttributeBoolean(id_step, "replace"); //$NON-NLS-1$
			useHash          =      rep.getStepAttributeBoolean(id_step, "crc"); //$NON-NLS-1$
			hashField        =      rep.getStepAttributeString (id_step, "crcfield"); //$NON-NLS-1$

			int nrkeys   = rep.countNrStepAttributes(id_step, "lookup_key_name"); //$NON-NLS-1$

			allocate(nrkeys);

			for (int i=0;i<nrkeys;i++)
			{
				keyField[i]  = rep.getStepAttributeString(id_step, i, "lookup_key_name"); //$NON-NLS-1$
				keyLookup[i] = rep.getStepAttributeString(id_step, i, "lookup_key_field"); //$NON-NLS-1$
			}

			technicalKeyField  = rep.getStepAttributeString (id_step, "return_name"); //$NON-NLS-1$
			useAutoinc         = rep.getStepAttributeBoolean(id_step, "use_autoinc"); //$NON-NLS-1$
			sequenceFrom       = rep.getStepAttributeString (id_step, "sequence"); //$NON-NLS-1$
			techKeyCreation    = rep.getStepAttributeString (id_step, "creation_method"); //$NON-NLS-1$
		}
		catch(Exception e)
		{
			throw new KettleException(Messages.getString("CombinationLookupMeta.Exception.UnexpectedErrorWhileReadingStepInfo"), e); //$NON-NLS-1$
		}
	}


	public void saveRep(Repository rep, long id_transformation, long id_step)
		throws KettleException
	{
		try
		{
            rep.saveStepAttribute(id_transformation, id_step, "schema",         schemaName); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "table",          tablename); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "id_connection",  database==null?-1:database.getID()); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "commit",         commitSize); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "cache_size",     cacheSize); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "replace",        replaceFields); //$NON-NLS-1$

			rep.saveStepAttribute(id_transformation, id_step, "crc",            useHash); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "crcfield",       hashField); //$NON-NLS-1$

			for (int i=0;i<keyField.length;i++)
			{
				rep.saveStepAttribute(id_transformation, id_step, i, "lookup_key_name",      keyField[i]); //$NON-NLS-1$
				rep.saveStepAttribute(id_transformation, id_step, i, "lookup_key_field",     keyLookup[i]); //$NON-NLS-1$
			}

			rep.saveStepAttribute(id_transformation, id_step, "return_name",         technicalKeyField); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "sequence",            sequenceFrom); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "creation_method",     techKeyCreation); //$NON-NLS-1$

			// For the moment still save 'use_autoinc' for backwards compatibility (Sven Boden).
			rep.saveStepAttribute(id_transformation, id_step, "use_autoinc",         useAutoinc); //$NON-NLS-1$

			// Also, save the step-database relationship!
			if (database!=null) rep.insertStepDatabase(id_transformation, id_step, database.getID());
		}
		catch(Exception e)
		{
			throw new KettleException(Messages.getString("CombinationLookupMeta.Exception.UnableToSaveStepInfo")+id_step, e); //$NON-NLS-1$
		}
	}

	public void check(ArrayList remarks, StepMeta stepMeta, Row prev, String input[], String output[], Row info)
	{
		CheckResult cr;
		String error_message = ""; //$NON-NLS-1$

		if (database!=null)
		{
			Database db = new Database(database);
			try
			{
				db.connect();

				if (!Const.isEmpty(tablename))
				{
					boolean first=true;
					boolean error_found=false;
					error_message = ""; //$NON-NLS-1$

                    String schemaTable = database.getQuotedSchemaTableCombination(schemaName, tablename);
					Row r = db.getTableFields(schemaTable);
					if (r!=null)
					{
						for (int i=0;i<keyLookup.length;i++)
						{
							String lufield = keyLookup[i];

							Value v = r.searchValue(lufield);
							if (v==null)
							{
								if (first)
								{
									first=false;
									error_message+=Messages.getString("CombinationLookupMeta.CheckResult.MissingCompareFields")+Const.CR; //$NON-NLS-1$
								}
								error_found=true;
								error_message+="\t\t"+lufield+Const.CR;  //$NON-NLS-1$
							}
						}
						if (error_found)
						{
							cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, error_message, stepMeta);
						}
						else
						{
							cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("CombinationLookupMeta.CheckResult.AllFieldsFound"), stepMeta); //$NON-NLS-1$
						}
						remarks.add(cr);

						/* Also, check the fields: tk, version, from-to, ... */
						if ( r.searchValueIndex(technicalKeyField)<0)
						{
							error_message=Messages.getString("CombinationLookupMeta.CheckResult.TechnicalKeyNotFound",technicalKeyField)+Const.CR; //$NON-NLS-1$ //$NON-NLS-2$
							cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, error_message, stepMeta);
						}
						else
						{
							error_message=Messages.getString("CombinationLookupMeta.CheckResult.TechnicalKeyFound",technicalKeyField)+Const.CR; //$NON-NLS-1$ //$NON-NLS-2$
							cr = new CheckResult(CheckResult.TYPE_RESULT_OK, error_message, stepMeta);
						}
						remarks.add(cr);
					}
					else
					{
						error_message=Messages.getString("CombinationLookupMeta.CheckResult.CouldNotReadTableInfo"); //$NON-NLS-1$
						cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, error_message, stepMeta);
						remarks.add(cr);
					}
				}

				// Look up fields in the input stream <prev>
				if (prev!=null && prev.size()>0)
				{
					boolean first=true;
					error_message = ""; //$NON-NLS-1$
					boolean error_found = false;

					for (int i=0;i<keyField.length;i++)
					{
						Value v = prev.searchValue(keyField[i]);
						if (v==null)
						{
							if (first)
							{
								first=false;
								error_message+=Messages.getString("CombinationLookupMeta.CheckResult.MissingFields")+Const.CR; //$NON-NLS-1$
							}
							error_found=true;
							error_message+="\t\t"+keyField[i]+Const.CR;  //$NON-NLS-1$
						}
					}
					if (error_found)
					{
						cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, error_message, stepMeta);
					}
					else
					{
						cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("CombinationLookupMeta.CheckResult.AllFieldsFoundInInputStream"), stepMeta); //$NON-NLS-1$
					}
					remarks.add(cr);
				}
				else
				{
					error_message=Messages.getString("CombinationLookupMeta.CheckResult.CouldNotReadFields")+Const.CR; //$NON-NLS-1$
					cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, error_message, stepMeta);
					remarks.add(cr);
				}

				// Check sequence
    			if (database.supportsSequences() && CREATION_METHOD_SEQUENCE.equals(getTechKeyCreation()) )
				{
    				if ( Const.isEmpty(sequenceFrom) )
    				{
						error_message+=Messages.getString("CombinationLookupMeta.CheckResult.ErrorNoSequenceName") + "!"; //$NON-NLS-1$ //$NON-NLS-2$
						cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, error_message, stepMeta);
						remarks.add(cr);
    				}
    				else
    				{
    				    // It doesn't make sense to check the sequence name
    					// if it's not filled in.
					    if (db.checkSequenceExists(sequenceFrom))
					    {
						    error_message = Messages.getString("CombinationLookupMeta.CheckResult.ReadingSequenceOK",sequenceFrom); //$NON-NLS-1$ //$NON-NLS-2$
					 	    cr = new CheckResult(CheckResult.TYPE_RESULT_OK, error_message, stepMeta);
						    remarks.add(cr);
					    }
					    else
					    {
						    error_message+=Messages.getString("CombinationLookupMeta.CheckResult.ErrorReadingSequence")+sequenceFrom+"!"; //$NON-NLS-1$ //$NON-NLS-2$
						    cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, error_message, stepMeta);
						    remarks.add(cr);
					    }
    				}
				}

				if ( techKeyCreation != null )
				{
				    // post 2.2 version
					if ( !(CREATION_METHOD_AUTOINC.equals(techKeyCreation) ||
					       CREATION_METHOD_SEQUENCE.equals(techKeyCreation) ||
					       CREATION_METHOD_TABLEMAX.equals(techKeyCreation)) )
					{
						error_message+=Messages.getString("CombinationLookupMeta.CheckResult.ErrorTechKeyCreation")+ ": " + techKeyCreation +"!"; //$NON-NLS-1$ //$NON-NLS-2$
						cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, error_message, stepMeta);
						remarks.add(cr);
					}

				}
			}
			catch(KettleException e)
			{
				error_message = Messages.getString("CombinationLookupMeta.CheckResult.ErrorOccurred")+e.getMessage(); //$NON-NLS-1$
				cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, error_message, stepMeta);
				remarks.add(cr);
			}
			finally
			{
				db.disconnect();
			}
		}
		else
		{
			error_message = Messages.getString("CombinationLookupMeta.CheckResult.InvalidConnection"); //$NON-NLS-1$
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, error_message, stepMeta);
			remarks.add(cr);
		}

		// See if we have input streams leading to this step!
		if (input.length>0)
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("CombinationLookupMeta.CheckResult.ReceivingInfoFromOtherSteps"), stepMeta); //$NON-NLS-1$
			remarks.add(cr);
		}
		else
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages.getString("CombinationLookupMeta.CheckResult.NoInputReceived"), stepMeta); //$NON-NLS-1$
			remarks.add(cr);
		}
	}

	public SQLStatement getSQLStatements(TransMeta transMeta, StepMeta stepMeta, Row prev)
	{
		SQLStatement retval = new SQLStatement(stepMeta.getName(), database, null); // default: nothing to do!

		int i;

		if (database!=null)
		{
			if (prev!=null && prev.size()>0)
			{
				if (!Const.isEmpty(tablename))
				{
                    String schemaTable = database.getQuotedSchemaTableCombination(schemaName, tablename);
					Database db = new Database(database);
					try
					{
						boolean doHash = false;
						String cr_table = null;

						db.connect();

						// OK, what do we put in the new table??
						Row fields = new Row();

						// First, the new technical key...
						Value vkeyfield = new Value(technicalKeyField, Value.VALUE_TYPE_INTEGER);
						vkeyfield.setLength(10,0);

						// Then the hashcode (optional)
						Value vhashfield = null;
						if (useHash && !Const.isEmpty(hashField))
						{
							vhashfield = new Value(hashField, Value.VALUE_TYPE_INTEGER);
							vhashfield.setLength(15,0);
							doHash = true;
						}

						if ( ! db.checkTableExists(schemaTable) )
						{
							// Add technical key field.
							fields.addValue(vkeyfield);				
							
							// Add the keys only to the table
							if ( keyField != null && keyLookup != null )
							{
								int cnt = keyField.length;
								for ( i=0;i<cnt;i++ )
								{
									String error_field=""; //$NON-NLS-1$

									// Find the value in the stream
									Value v = prev.searchValue(keyField[i]);
									if ( v != null )
									{
										String name = keyLookup[i];
									    Value newValue = new Value(v);
									    newValue.setName(name);

        								if ( name.equals(vkeyfield.getName()) ||
		 		  							 (doHash == true && name.equals(vhashfield.getName())) )
  									    {
										    error_field+=name;
									    }
									    if (error_field.length()>0)
									    {
										    retval.setError(Messages.getString("CombinationLookupMeta.ReturnValue.NameCollision", error_field)); //$NON-NLS-1$
									    }
									    else
									    {
									        fields.addValue(newValue);
									    }
									}
								}
							}

							if ( doHash == true )
							{
								fields.addValue(vhashfield);
							}
						}
						else
						{
							// Table already exists

							// Get the fields that are in the table now:
							Row tabFields = db.getTableFields(schemaTable);

							// Don't forget to quote these as well...
							database.quoteReservedWords(tabFields);

							if (tabFields.searchValue( vkeyfield.getName() ) == null )
							{
								// Add technical key field if it didn't exist yet
								fields.addValue(vkeyfield);
							}

							// Add the already existing fields
							int cnt = tabFields.size();
							for ( i=0;i<cnt;i++ )
							{
								Value v = tabFields.getValue(i);

								fields.addValue(v);
							}

							// Find the missing fields in the real table
							String keyLookup[] = getKeyLookup();
							String keyField[] = getKeyField();							
							if ( keyField != null && keyLookup != null )
							{
								cnt = keyField.length;
								for ( i=0;i<cnt;i++ )
								{
									// Find the value in the stream
									Value v = prev.searchValue(keyField[i]);
									if ( v != null )
									{
									    Value newValue = (Value)v.clone();
									    newValue.setName(keyLookup[i]);

									    // Does the corresponding name exist in the table
									    if ( tabFields.searchValue( newValue.getName() )==null )
									    {
										    fields.addValue(newValue); // nope --> add
									    }
									}
								}
							}

							if (doHash == true && tabFields.searchValue( vhashfield.getName() ) == null )
							{
								// Add hash field
								fields.addValue(vhashfield);
							}
						}

						cr_table = db.getDDL(schemaTable,
								             fields,
								             (CREATION_METHOD_SEQUENCE.equals(getTechKeyCreation()) &&
										     sequenceFrom!=null && sequenceFrom.length()!=0)?null:technicalKeyField,
											 CREATION_METHOD_AUTOINC.equals(getTechKeyCreation()),
											 null,
											 true);

						//
						// OK, now let's build the index
						//

						// What fields do we put int the index?
						// Only the hashcode or all fields?
						String cr_index = ""; //$NON-NLS-1$
						String cr_uniq_index = ""; //$NON-NLS-1$
						String idx_fields[] = null;
						if (useHash)
						{
							if (hashField!=null && hashField.length()>0)
							{
								idx_fields = new String[] { hashField };
							}
							else
							{
								retval.setError(Messages.getString("CombinationLookupMeta.ReturnValue.NotHashFieldSpecified")); //$NON-NLS-1$
							}
						}
						else  // index on all key fields...
						{
							if (!Const.isEmpty(keyLookup))
							{
								int nrfields = keyLookup.length;
								if (nrfields>32 && database.getDatabaseType()==DatabaseMeta.TYPE_DATABASE_ORACLE)
								{
									nrfields=32;  // Oracle indexes are limited to 32 fields...
								}
								idx_fields = new String[nrfields];
								for (i=0;i<nrfields;i++) idx_fields[i] = keyLookup[i];
							}
							else
							{
								retval.setError(Messages.getString("CombinationLookupMeta.ReturnValue.NotFieldsSpecified")); //$NON-NLS-1$
							}
						}

						// OK, now get the create index statement...

						if ( !Const.isEmpty(technicalKeyField))
						{
							String techKeyArr[] = new String [] { technicalKeyField };
							if (!db.checkIndexExists(schemaName, tablename, techKeyArr))
							{
								String indexname = "idx_"+tablename+"_pk"; //$NON-NLS-1$ //$NON-NLS-2$
								cr_uniq_index = db.getCreateIndexStatement(schemaName, tablename, indexname, techKeyArr, true, true, false, true);
								cr_uniq_index+=Const.CR;
							}
						}


						// OK, now get the create lookup index statement...
						if (!Const.isEmpty(idx_fields) && !db.checkIndexExists(schemaName, tablename, idx_fields)
						)
						{
							String indexname = "idx_"+tablename+"_lookup"; //$NON-NLS-1$ //$NON-NLS-2$
							cr_index = db.getCreateIndexStatement(schemaName, tablename, indexname, idx_fields, false, false, false, true);
							cr_index+=Const.CR;
						}

						//
						// Don't forget the sequence (optional)
						//
						String cr_seq=""; //$NON-NLS-1$
						if ( database.supportsSequences() && !Const.isEmpty(sequenceFrom) )
						{
							if (!db.checkSequenceExists(schemaName, sequenceFrom))
							{
								cr_seq+=db.getCreateSequenceStatement(schemaName, sequenceFrom, 1L, 1L, -1L, true);
								cr_seq+=Const.CR;
							}
						}
						retval.setSQL(cr_table+cr_uniq_index+cr_index+cr_seq);
					}
					catch(KettleException e)
					{
						retval.setError(Messages.getString("CombinationLookupMeta.ReturnValue.ErrorOccurred")+Const.CR+e.getMessage()); //$NON-NLS-1$
					}
				}
				else
				{
					retval.setError(Messages.getString("CombinationLookupMeta.ReturnValue.NotTableDefined")); //$NON-NLS-1$
				}
			}
			else
			{
				retval.setError(Messages.getString("CombinationLookupMeta.ReturnValue.NotReceivingField")); //$NON-NLS-1$
			}
		}
		else
		{
			retval.setError(Messages.getString("CombinationLookupMeta.ReturnValue.NotConnectionDefined")); //$NON-NLS-1$
		}

		return retval;
	}

	public StepDialogInterface getDialog(Shell shell, StepMetaInterface info, TransMeta transMeta, String name)
	{
		return new CombinationLookupDialog(shell, info, transMeta, name);
	}

	public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta, Trans trans)
	{
		return new CombinationLookup(stepMeta, stepDataInterface, cnr, transMeta, trans);
	}

	public StepDataInterface getStepData()
	{
		return new CombinationLookupData();
	}

	public void analyseImpact(ArrayList impact, TransMeta transMeta, StepMeta stepMeta, Row prev, String input[], String output[], Row info)
	{
		// The keys are read-only...
		for (int i=0;i<keyField.length;i++)
		{
			Value v = prev.searchValue(keyField[i]);
			DatabaseImpact ii = new DatabaseImpact( DatabaseImpact.TYPE_IMPACT_READ_WRITE,
											transMeta.getName(),
											stepMeta.getName(),
											database.getDatabaseName(),
											tablename,
											keyLookup[i],
											keyField[i],
											v!=null?v.getOrigin():"?", //$NON-NLS-1$
											"", //$NON-NLS-1$
											useHash?Messages.getString("CombinationLookupMeta.ReadAndInsert.Label"):Messages.getString("CombinationLookupMeta.LookupAndInsert.Label") //$NON-NLS-1$ //$NON-NLS-2$
											);
			impact.add(ii);
		}

		// Do we lookup-on the hash-field?
		if (useHash)
		{
			DatabaseImpact ii = new DatabaseImpact( DatabaseImpact.TYPE_IMPACT_READ_WRITE,
											transMeta.getName(),
											stepMeta.getName(),
											database.getDatabaseName(),
											tablename,
											hashField,
											"", //$NON-NLS-1$
											"", //$NON-NLS-1$
											"", //$NON-NLS-1$
											Messages.getString("CombinationLookupMeta.KeyLookup.Label") //$NON-NLS-1$
											);
			impact.add(ii);
		}
	}

    public DatabaseMeta[] getUsedDatabaseConnections()
    {
        if (database!=null)
        {
            return new DatabaseMeta[] { database };
        }
        else
        {
            return super.getUsedDatabaseConnections();
        }
    }

    public boolean equals(Object other)
    {
        if (other == this) return true;
        if (other == null) return false;
        if (getClass() != other.getClass()) return false;
        CombinationLookupMeta o = (CombinationLookupMeta)other;

        if ( getCommitSize() != o.getCommitSize() ) return false;
        if ( getCacheSize() != o.getCacheSize() ) return false;
        if ( ! getTechKeyCreation().equals(o.getTechKeyCreation()) ) return false;
        if ( replaceFields() != o.replaceFields() ) return false;
        if ( useHash() != o.useHash() )	return false;
        if ( replaceFields() != o.replaceFields() )	return false;
        if ( (getSequenceFrom() == null && o.getSequenceFrom() != null) ||
        	 (getSequenceFrom() != null && o.getSequenceFrom() == null) ||
        	 (getSequenceFrom() != null && o.getSequenceFrom() != null &&
        	  ! getSequenceFrom().equals(o.getSequenceFrom())) )
        	 return false;

        if ( (getSchemaName() == null && o.getSchemaName() != null) ||
             (getSchemaName() != null && o.getSchemaName() == null) ||
             (getSchemaName() != null && o.getSchemaName() != null &&
              ! getSchemaName().equals(o.getSchemaName())) )
             return false;

        if ( (getTablename() == null && o.getTablename() != null) ||
           	 (getTablename() != null && o.getTablename() == null) ||
           	 (getTablename() != null && o.getTablename() != null &&
           	  ! getTablename().equals(o.getTablename())) )
           	 return false;

        if ( (getHashField() == null && o.getHashField() != null) ||
             (getHashField() != null && o.getHashField() == null) ||
             (getHashField() != null && o.getHashField() != null &&
              ! getHashField().equals(o.getHashField())) )
          	 return false;

        if ( (getTechnicalKeyField() == null && o.getTechnicalKeyField() != null) ||
             (getTechnicalKeyField() != null && o.getTechnicalKeyField() == null) ||
             (getTechnicalKeyField() != null && o.getTechnicalKeyField() != null &&
             ! getTechnicalKeyField().equals(o.getTechnicalKeyField())) )
   	       return false;

        // comparison missing for the following, but can be added later
        // if required.
     	  //   getKeyField()
    	  //   getKeyLookup()

        return true;
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
    public boolean supportsErrorHandling()
    {
        return true;
    }

}