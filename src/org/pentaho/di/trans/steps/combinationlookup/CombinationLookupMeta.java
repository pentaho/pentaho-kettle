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

package org.pentaho.di.trans.steps.combinationlookup;

import java.util.List;
import java.util.Map;

import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Counter;
import org.pentaho.di.core.SQLStatement;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.ObjectId;
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



/*
 * Created on 14-may-2003
 *
 * TODO: In the distant future the use_autoinc flag should be removed since its
 *       functionality is now taken over by techKeyCreation (which is cleaner).
 */
public class CombinationLookupMeta extends BaseStepMeta implements StepMetaInterface
{
	private static Class<?> PKG = CombinationLookupMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	/** Default cache size: 0 will cache everything */
	public final static int DEFAULT_CACHE_SIZE  = 9999;

    /** what's the lookup schema? */
    private String  schemaName;

	/** what's the lookup table? */
	private String  tablename;

	/** database connection */
	private DatabaseMeta  databaseMeta;

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

	private String lastUpdateField;
	
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
	public DatabaseMeta getDatabaseMeta()
	{
		return databaseMeta;
	}

	/**
	 * @param database The database to set.
	 */
	public void setDatabaseMeta(DatabaseMeta database)
	{
		this.databaseMeta = database;
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

	public void loadXML(Node stepnode, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleXMLException {
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

	private void readData(Node stepnode, List<? extends SharedObjectInterface> databases)
		throws KettleXMLException
	{
		try
		{
			String commit, csize;

            schemaName  = XMLHandler.getTagValue(stepnode, "schema"); //$NON-NLS-1$
			tablename  = XMLHandler.getTagValue(stepnode, "table"); //$NON-NLS-1$
			String con = XMLHandler.getTagValue(stepnode, "connection"); //$NON-NLS-1$
			databaseMeta = DatabaseMeta.findDatabase(databases, con);
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
            lastUpdateField = XMLHandler.getTagValue(stepnode, "last_update_field"); //$NON-NLS-1$

			setTechKeyCreation(XMLHandler.getTagValue(retkey, "creation_method")); //$NON-NLS-1$
		}
		catch(Exception e)
		{
			throw new KettleXMLException(BaseMessages.getString(PKG, "CombinationLookupMeta.Exception.UnableToLoadStepInfo"), e); //$NON-NLS-1$
		}
	}

	public void setDefault()
	{
        schemaName    = ""; //$NON-NLS-1$
		tablename     = BaseMessages.getString(PKG, "CombinationLookupMeta.DimensionTableName.Label"); //$NON-NLS-1$
		databaseMeta      = null;
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

	public void getFields(RowMetaInterface row, String origin, RowMetaInterface[] info, StepMeta nextStep, VariableSpace space) throws KettleStepException
	{
		ValueMetaInterface v=new ValueMeta(technicalKeyField, ValueMetaInterface.TYPE_INTEGER);
		v.setLength(10);
        v.setPrecision(0);
		v.setOrigin(origin);
		row.addValueMeta(v);

		if (replaceFields)
		{
			for (int i=0;i<keyField.length;i++)
			{
				int idx = row.indexOfValue(keyField[i]);
				if (idx>=0)
				{
					row.removeValueMeta(idx);
				}
			}
		}
	}

	public String getXML()
	{
        StringBuffer retval = new StringBuffer(512);

        retval.append("      ").append(XMLHandler.addTagValue("schema", schemaName)); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("      ").append(XMLHandler.addTagValue("table", tablename)); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("      ").append(XMLHandler.addTagValue("connection", databaseMeta==null?"":databaseMeta.getName())); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
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
		retval.append("      ").append(XMLHandler.addTagValue("last_update_field", lastUpdateField)); //$NON-NLS-1$ //$NON-NLS-2$

		return retval.toString();
	}

	public void readRep(Repository rep, ObjectId id_step, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleException
	{
		try
		{
			databaseMeta = rep.loadDatabaseMetaFromStepAttribute(id_step, "id_connection", databases);

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
			lastUpdateField    = rep.getStepAttributeString (id_step, "last_update_field"); //$NON-NLS-1$
		}
		catch(Exception e)
		{
			throw new KettleException(BaseMessages.getString(PKG, "CombinationLookupMeta.Exception.UnexpectedErrorWhileReadingStepInfo"), e); //$NON-NLS-1$
		}
	}


	public void saveRep(Repository rep, ObjectId id_transformation, ObjectId id_step) throws KettleException
	{
		try
		{
            rep.saveStepAttribute(id_transformation, id_step, "schema",         schemaName); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "table",          tablename); //$NON-NLS-1$
			rep.saveDatabaseMetaStepAttribute(id_transformation, id_step, "id_connection", databaseMeta);
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

			rep.saveStepAttribute(id_transformation, id_step, "last_update_field",         lastUpdateField); //$NON-NLS-1$

			// Also, save the step-database relationship!
			if (databaseMeta!=null) rep.insertStepDatabase(id_transformation, id_step, databaseMeta.getObjectId());
		}
		catch(Exception e)
		{
			throw new KettleException(BaseMessages.getString(PKG, "CombinationLookupMeta.Exception.UnableToSaveStepInfo")+id_step, e); //$NON-NLS-1$
		}
	}

	public void check(List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta, RowMetaInterface prev, String input[], String output[], RowMetaInterface info)
	{
		CheckResult cr;
		String error_message = ""; //$NON-NLS-1$

		if (databaseMeta!=null)
		{
			Database db = new Database(loggingObject, databaseMeta);
			try
			{
				db.connect();

				if (!Const.isEmpty(tablename))
				{
					boolean first=true;
					boolean error_found=false;
					error_message = ""; //$NON-NLS-1$

                    String schemaTable = databaseMeta.getQuotedSchemaTableCombination(schemaName, tablename);
					RowMetaInterface r = db.getTableFields(schemaTable);
					if (r!=null)
					{
						for (int i=0;i<keyLookup.length;i++)
						{
							String lufield = keyLookup[i];

							ValueMetaInterface v = r.searchValueMeta(lufield);
							if (v==null)
							{
								if (first)
								{
									first=false;
									error_message+=BaseMessages.getString(PKG, "CombinationLookupMeta.CheckResult.MissingCompareFields")+Const.CR; //$NON-NLS-1$
								}
								error_found=true;
								error_message+="\t\t"+lufield+Const.CR;  //$NON-NLS-1$
							}
						}
						if (error_found)
						{
							cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepMeta);
						}
						else
						{
							cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(PKG, "CombinationLookupMeta.CheckResult.AllFieldsFound"), stepMeta); //$NON-NLS-1$
						}
						remarks.add(cr);

						/* Also, check the fields: tk, version, from-to, ... */
						if ( r.indexOfValue(technicalKeyField)<0)
						{
							error_message=BaseMessages.getString(PKG, "CombinationLookupMeta.CheckResult.TechnicalKeyNotFound",technicalKeyField)+Const.CR; //$NON-NLS-1$ //$NON-NLS-2$
							cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepMeta);
						}
						else
						{
							error_message=BaseMessages.getString(PKG, "CombinationLookupMeta.CheckResult.TechnicalKeyFound",technicalKeyField)+Const.CR; //$NON-NLS-1$ //$NON-NLS-2$
							cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, error_message, stepMeta);
						}
						remarks.add(cr);
					}
					else
					{
						error_message=BaseMessages.getString(PKG, "CombinationLookupMeta.CheckResult.CouldNotReadTableInfo"); //$NON-NLS-1$
						cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepMeta);
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
						ValueMetaInterface v = prev.searchValueMeta(keyField[i]);
						if (v==null)
						{
							if (first)
							{
								first=false;
								error_message+=BaseMessages.getString(PKG, "CombinationLookupMeta.CheckResult.MissingFields")+Const.CR; //$NON-NLS-1$
							}
							error_found=true;
							error_message+="\t\t"+keyField[i]+Const.CR;  //$NON-NLS-1$
						}
					}
					if (error_found)
					{
						cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepMeta);
					}
					else
					{
						cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(PKG, "CombinationLookupMeta.CheckResult.AllFieldsFoundInInputStream"), stepMeta); //$NON-NLS-1$
					}
					remarks.add(cr);
				}
				else
				{
					error_message=BaseMessages.getString(PKG, "CombinationLookupMeta.CheckResult.CouldNotReadFields")+Const.CR; //$NON-NLS-1$
					cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepMeta);
					remarks.add(cr);
				}

				// Check sequence
    			if (databaseMeta.supportsSequences() && CREATION_METHOD_SEQUENCE.equals(getTechKeyCreation()) )
				{
    				if ( Const.isEmpty(sequenceFrom) )
    				{
						error_message+=BaseMessages.getString(PKG, "CombinationLookupMeta.CheckResult.ErrorNoSequenceName") + "!"; //$NON-NLS-1$ //$NON-NLS-2$
						cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepMeta);
						remarks.add(cr);
    				}
    				else
    				{
    				    // It doesn't make sense to check the sequence name
    					// if it's not filled in.
					    if (db.checkSequenceExists(sequenceFrom))
					    {
						    error_message = BaseMessages.getString(PKG, "CombinationLookupMeta.CheckResult.ReadingSequenceOK",sequenceFrom); //$NON-NLS-1$ //$NON-NLS-2$
					 	    cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, error_message, stepMeta);
						    remarks.add(cr);
					    }
					    else
					    {
						    error_message+=BaseMessages.getString(PKG, "CombinationLookupMeta.CheckResult.ErrorReadingSequence")+sequenceFrom+"!"; //$NON-NLS-1$ //$NON-NLS-2$
						    cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepMeta);
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
						error_message+=BaseMessages.getString(PKG, "CombinationLookupMeta.CheckResult.ErrorTechKeyCreation")+ ": " + techKeyCreation +"!"; //$NON-NLS-1$ //$NON-NLS-2$
						cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepMeta);
						remarks.add(cr);
					}

				}
			}
			catch(KettleException e)
			{
				error_message = BaseMessages.getString(PKG, "CombinationLookupMeta.CheckResult.ErrorOccurred")+e.getMessage(); //$NON-NLS-1$
				cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepMeta);
				remarks.add(cr);
			}
			finally
			{
				db.disconnect();
			}
		}
		else
		{
			error_message = BaseMessages.getString(PKG, "CombinationLookupMeta.CheckResult.InvalidConnection"); //$NON-NLS-1$
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepMeta);
			remarks.add(cr);
		}

		// See if we have input streams leading to this step!
		if (input.length>0)
		{
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(PKG, "CombinationLookupMeta.CheckResult.ReceivingInfoFromOtherSteps"), stepMeta); //$NON-NLS-1$
			remarks.add(cr);
		}
		else
		{
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(PKG, "CombinationLookupMeta.CheckResult.NoInputReceived"), stepMeta); //$NON-NLS-1$
			remarks.add(cr);
		}
	}

	public SQLStatement getSQLStatements(TransMeta transMeta, StepMeta stepMeta, RowMetaInterface prev)
	{
		SQLStatement retval = new SQLStatement(stepMeta.getName(), databaseMeta, null); // default: nothing to do!

		int i;

		if (databaseMeta!=null)
		{
			if (prev!=null && prev.size()>0)
			{
				if (!Const.isEmpty(tablename))
				{
                    String schemaTable = databaseMeta.getQuotedSchemaTableCombination(schemaName, tablename);
					Database db = new Database(loggingObject, databaseMeta);
					try
					{
						boolean doHash = false;
						String cr_table = null;

						db.connect();

						// OK, what do we put in the new table??
                        RowMetaInterface fields = new RowMeta();

						// First, the new technical key...
						ValueMetaInterface vkeyfield = new ValueMeta(technicalKeyField, ValueMetaInterface.TYPE_INTEGER);
						vkeyfield.setLength(10);
                        vkeyfield.setPrecision(0);

						// Then the hashcode (optional)
                        ValueMetaInterface vhashfield = null;
						if (useHash && !Const.isEmpty(hashField))
						{
							vhashfield = new ValueMeta(hashField, ValueMetaInterface.TYPE_INTEGER);
							vhashfield.setLength(15);
                            vhashfield.setPrecision(0);
							doHash = true;
						}
						
						// Then the last update field (optional)
                        ValueMetaInterface vLastUpdateField = null;
						if (!Const.isEmpty(lastUpdateField))
						{
							vLastUpdateField = new ValueMeta(lastUpdateField, ValueMetaInterface.TYPE_DATE);
						}

						if ( ! db.checkTableExists(schemaTable) )
						{
							// Add technical key field.
							fields.addValueMeta(vkeyfield);				
							
							// Add the keys only to the table
							if ( keyField != null && keyLookup != null )
							{
								int cnt = keyField.length;
								for ( i=0;i<cnt;i++ )
								{
									String error_field=""; //$NON-NLS-1$

									// Find the value in the stream
									ValueMetaInterface v = prev.searchValueMeta(keyField[i]);
									if ( v != null )
									{
										String name = keyLookup[i];
									    ValueMetaInterface newValue = v.clone();
									    newValue.setName(name);

        								if ( name.equals(vkeyfield.getName()) ||
		 		  							 (doHash == true && name.equals(vhashfield.getName())) )
  									    {
										    error_field+=name;
									    }
									    if (error_field.length()>0)
									    {
										    retval.setError(BaseMessages.getString(PKG, "CombinationLookupMeta.ReturnValue.NameCollision", error_field)); //$NON-NLS-1$
									    }
									    else
									    {
									        fields.addValueMeta(newValue);
									    }
									}
								}
							}

							if ( doHash == true )
							{
								fields.addValueMeta(vhashfield);
							}
							
							if ( vLastUpdateField!=null ) 
							{
								fields.addValueMeta(vLastUpdateField);
							}
						}
						else
						{
							// Table already exists

							// Get the fields that are in the table now:
							RowMetaInterface tabFields = db.getTableFields(schemaTable);

							// Don't forget to quote these as well...
							databaseMeta.quoteReservedWords(tabFields);

							if (tabFields.searchValueMeta( vkeyfield.getName() ) == null )
							{
								// Add technical key field if it didn't exist yet
								fields.addValueMeta(vkeyfield);
							}

							// Add the already existing fields
							int cnt = tabFields.size();
							for ( i=0;i<cnt;i++ )
							{
								ValueMetaInterface v = tabFields.getValueMeta(i);

								fields.addValueMeta(v);
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
                                    ValueMetaInterface v = prev.searchValueMeta(keyField[i]);
									if ( v != null )
									{
                                        ValueMetaInterface newValue = v.clone();
									    newValue.setName(keyLookup[i]);

									    // Does the corresponding name exist in the table
									    if ( tabFields.searchValueMeta( newValue.getName() )==null )
									    {
										    fields.addValueMeta(newValue); // nope --> add
									    }
									}
								}
							}

							if (doHash == true && tabFields.searchValueMeta( vhashfield.getName() ) == null )
							{
								// Add hash field
								fields.addValueMeta(vhashfield);
							}
							
							if (vLastUpdateField!=null && tabFields.searchValueMeta( vLastUpdateField.getName()) == null)
							{
								fields.addValueMeta(vLastUpdateField);
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
								retval.setError(BaseMessages.getString(PKG, "CombinationLookupMeta.ReturnValue.NotHashFieldSpecified")); //$NON-NLS-1$
							}
						}
						else  // index on all key fields...
						{
							if (!Const.isEmpty(keyLookup))
							{
								int nrfields = keyLookup.length;
								if (nrfields>32 && databaseMeta.getDatabaseType() == DatabaseMeta.TYPE_DATABASE_ORACLE)
								{
									nrfields=32;  // Oracle indexes are limited to 32 fields...
								}
								idx_fields = new String[nrfields];
								for (i=0;i<nrfields;i++) idx_fields[i] = keyLookup[i];
							}
							else
							{
								retval.setError(BaseMessages.getString(PKG, "CombinationLookupMeta.ReturnValue.NotFieldsSpecified")); //$NON-NLS-1$
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
						if ( databaseMeta.supportsSequences() && !Const.isEmpty(sequenceFrom) )
						{
							if (!db.checkSequenceExists(schemaName, sequenceFrom))
							{
								cr_seq+=db.getCreateSequenceStatement(schemaName, sequenceFrom, 1L, 1L, -1L, true);
								cr_seq+=Const.CR;
							}
						}
						retval.setSQL(transMeta.environmentSubstitute(cr_table+cr_uniq_index+cr_index+cr_seq));
					}
					catch(KettleException e)
					{
						retval.setError(BaseMessages.getString(PKG, "CombinationLookupMeta.ReturnValue.ErrorOccurred")+Const.CR+e.getMessage()); //$NON-NLS-1$
					}
				}
				else
				{
					retval.setError(BaseMessages.getString(PKG, "CombinationLookupMeta.ReturnValue.NotTableDefined")); //$NON-NLS-1$
				}
			}
			else
			{
				retval.setError(BaseMessages.getString(PKG, "CombinationLookupMeta.ReturnValue.NotReceivingField")); //$NON-NLS-1$
			}
		}
		else
		{
			retval.setError(BaseMessages.getString(PKG, "CombinationLookupMeta.ReturnValue.NotConnectionDefined")); //$NON-NLS-1$
		}

		return retval;
	}

	public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta, Trans trans)
	{
		return new CombinationLookup(stepMeta, stepDataInterface, cnr, transMeta, trans);
	}

	public StepDataInterface getStepData()
	{
		return new CombinationLookupData();
	}

	public void analyseImpact(List<DatabaseImpact> impact, TransMeta transMeta, StepMeta stepMeta, RowMetaInterface prev, String input[], String output[], RowMetaInterface info)
	{
		// The keys are read-only...
		for (int i=0;i<keyField.length;i++)
		{
			ValueMetaInterface v = prev.searchValueMeta(keyField[i]);
			DatabaseImpact ii = new DatabaseImpact( DatabaseImpact.TYPE_IMPACT_READ_WRITE,
											transMeta.getName(),
											stepMeta.getName(),
											databaseMeta.getDatabaseName(),
											tablename,
											keyLookup[i],
											keyField[i],
											v!=null?v.getOrigin():"?", //$NON-NLS-1$
											"", //$NON-NLS-1$
											useHash?BaseMessages.getString(PKG, "CombinationLookupMeta.ReadAndInsert.Label"):BaseMessages.getString(PKG, "CombinationLookupMeta.LookupAndInsert.Label") //$NON-NLS-1$ //$NON-NLS-2$
											);
			impact.add(ii);
		}

		// Do we lookup-on the hash-field?
		if (useHash)
		{
			DatabaseImpact ii = new DatabaseImpact( DatabaseImpact.TYPE_IMPACT_READ_WRITE,
											transMeta.getName(),
											stepMeta.getName(),
											databaseMeta.getDatabaseName(),
											tablename,
											hashField,
											"", //$NON-NLS-1$
											"", //$NON-NLS-1$
											"", //$NON-NLS-1$
											BaseMessages.getString(PKG, "CombinationLookupMeta.KeyLookup.Label") //$NON-NLS-1$
											);
			impact.add(ii);
		}
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

	/**
	 * @return the lastUpdateField
	 */
	public String getLastUpdateField() {
		return lastUpdateField;
	}

	/**
	 * @param lastUpdateField the lastUpdateField to set
	 */
	public void setLastUpdateField(String lastUpdateField) {
		this.lastUpdateField = lastUpdateField;
	}
}