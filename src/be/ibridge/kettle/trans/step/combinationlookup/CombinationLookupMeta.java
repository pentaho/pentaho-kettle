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
 */

public class CombinationLookupMeta extends BaseStepMeta implements StepMetaInterface
{
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
	
	/** Use the auto-increment feature of the database to generate keys. */
	private boolean useAutoinc;
	

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
	 * @return Returns the keyField.
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
		keyField       = new String[nrkeys];
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
			retval.keyField[i]      = keyField[i];
			retval.keyLookup[i]= keyLookup[i];
		}
		
		return retval;
	}
	
	private void readData(Node stepnode, ArrayList databases)
		throws KettleXMLException
	{
		try
		{
			String commit;
			
			tablename      = XMLHandler.getTagValue(stepnode, "table");
			String con = XMLHandler.getTagValue(stepnode, "connection");
			database = Const.findDatabase(databases, con);
			commit     = XMLHandler.getTagValue(stepnode, "commit");
			commitSize = Const.toInt(commit, 0);
			
			replaceFields ="Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "replace"));
			useHash    ="Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "crc"));
			
			hashField  = XMLHandler.getTagValue(stepnode, "crcfield");
	
			Node keys = XMLHandler.getSubNode(stepnode, "fields");
			int nrkeys    = XMLHandler.countNodes(keys, "key");
			
			allocate(nrkeys);
			
			// Read keys to dimension 
			for (int i=0;i<nrkeys;i++)
			{
				Node knode = XMLHandler.getSubNodeByNr(keys, "key", i);
				keyField[i]      = XMLHandler.getTagValue(knode, "name");
				keyLookup[i]= XMLHandler.getTagValue(knode, "lookup");
			}
	
			// If this is empty: use auto-increment field!
			sequenceFrom = XMLHandler.getTagValue(stepnode, "sequence");
			
			Node fields = XMLHandler.getSubNode(stepnode, "fields");
			Node retkey = XMLHandler.getSubNode(fields, "return");
			technicalKeyField     = XMLHandler.getTagValue(retkey, "name");
			useAutoinc  = !"N".equalsIgnoreCase(XMLHandler.getTagValue(retkey, "use_autoinc"));
		}
		catch(Exception e)
		{
			throw new KettleXMLException("Unable to load step info from XML", e);
		}
	}

	public void setDefault()
	{		
		tablename      = "dim table name";
		database = null;
		commitSize = 0;
		replaceFields    = true;
		useHash        = true;
		hashField   = "hashcode";

		int nrkeys    = 0;
		
		allocate(nrkeys);
		
		// Read keys to dimension 
		for (int i=0;i<nrkeys;i++)
		{
			keyField[i]      ="key"+i;
			keyLookup[i]="keylookup"+i;
		}

		technicalKeyField     = "technical /surrogate key field";
		useAutoinc  = true;
	}

	public Row getFields(Row r, String name, Row info)
	{
		Row row;
		if (r==null) row=new Row(); // give back values
		else         row=r;         // add to the existing row of values...

		Value v=new Value(technicalKeyField,     Value.VALUE_TYPE_INTEGER);
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
		String retval="";
		int i;
		
		retval+="      "+XMLHandler.addTagValue("table", tablename);
		retval+="      "+XMLHandler.addTagValue("connection", database==null?"":database.getName());
		retval+="      "+XMLHandler.addTagValue("commit", commitSize);
		retval+="      "+XMLHandler.addTagValue("replace", replaceFields);
		retval+="      "+XMLHandler.addTagValue("crc", useHash);
		retval+="      "+XMLHandler.addTagValue("crcfield", hashField);
		
		retval+="      <fields>"+Const.CR;
		for (i=0;i<keyField.length;i++)
		{
			retval+="        <key>"+Const.CR;
			retval+="          "+XMLHandler.addTagValue("name",   keyField[i]);
			retval+="          "+XMLHandler.addTagValue("lookup", keyLookup[i]);
			retval+="          </key>"+Const.CR;
		}

		retval+="        <return>"+Const.CR;
		retval+="          "+XMLHandler.addTagValue("name", technicalKeyField);
		retval+="          "+XMLHandler.addTagValue("use_autoinc", useAutoinc);
		retval+="        </return>"+Const.CR;

		retval+="      </fields>"+Const.CR;

		// If sequence is empty: use auto-increment field!
		retval+="      "+XMLHandler.addTagValue("sequence", sequenceFrom);
		
		return retval;
	}
	
	public void readRep(Repository rep, long id_step, ArrayList databases, Hashtable counters)
		throws KettleException
	{
		try
		{
			long id_connection =   rep.getStepAttributeInteger(id_step, "id_connection"); 
			database = Const.findDatabase( databases, id_connection);
			
			tablename            =      rep.getStepAttributeString (id_step, "table");
			commitSize       = (int)rep.getStepAttributeInteger(id_step, "commit");
			replaceFields          =      rep.getStepAttributeBoolean(id_step, "replace");
			useHash             =      rep.getStepAttributeBoolean(id_step, "crc");
			hashField        =      rep.getStepAttributeString (id_step, "crcfield");
	
			int nrkeys   = rep.countNrStepAttributes(id_step, "lookup_key_name");
			
			allocate(nrkeys);
			
			for (int i=0;i<nrkeys;i++)
			{
				keyField[i]       = rep.getStepAttributeString(id_step, i, "lookup_key_name");
				keyLookup[i] = rep.getStepAttributeString(id_step, i, "lookup_key_field");
			}
	
			technicalKeyField     =      rep.getStepAttributeString (id_step, "return_name");
			useAutoinc  =      rep.getStepAttributeBoolean(id_step, "use_autoinc");
			sequenceFrom     =      rep.getStepAttributeString (id_step, "sequence");
		}
		catch(Exception e)
		{
			throw new KettleException("Unexpected error reading step information from the repository", e);
		}
	}


	public void saveRep(Repository rep, long id_transformation, long id_step)
		throws KettleException
	{
		try
		{
			long id      = rep.saveStepAttribute(id_transformation, id_step, "table",          tablename);
			if (id>0) id = rep.saveStepAttribute(id_transformation, id_step, "id_connection",  database==null?-1:database.getID());
			if (id>0) id = rep.saveStepAttribute(id_transformation, id_step, "commit",         commitSize);
			if (id>0) id = rep.saveStepAttribute(id_transformation, id_step, "replace",        replaceFields);
			
			if (id>0) id = rep.saveStepAttribute(id_transformation, id_step, "crc",            useHash);
			if (id>0) id = rep.saveStepAttribute(id_transformation, id_step, "crcfield",       hashField);
			
			for (int i=0;i<keyField.length;i++)
			{
				if (id>0) id = rep.saveStepAttribute(id_transformation, id_step, i, "lookup_key_name",      keyField[i]);
				if (id>0) id = rep.saveStepAttribute(id_transformation, id_step, i, "lookup_key_field",     keyLookup[i]);
			}
	
			if (id>0) id = rep.saveStepAttribute(id_transformation, id_step, "return_name",         technicalKeyField);
			if (id>0) id = rep.saveStepAttribute(id_transformation, id_step, "use_autoinc",         useAutoinc);
			if (id>0) id = rep.saveStepAttribute(id_transformation, id_step, "sequence",            sequenceFrom);
			
			// Also, save the step-database relationship!
			if (database!=null) rep.insertStepDatabase(id_transformation, id_step, database.getID());
		}
		catch(Exception e)
		{
			throw new KettleException("Unable to save step information to the repository for id_step="+id_step, e);
		}
	}

	

	public void check(ArrayList remarks, StepMeta stepMeta, Row prev, String input[], String output[], Row info)
	{
		CheckResult cr;
		String error_message = "";
		
		if (database!=null)
		{
			Database db = new Database(database);
			try
			{
				db.connect();
				
				if (tablename!=null && tablename.length()!=0)
				{
					boolean first=true;
					boolean error_found=false;
					error_message = "";
					
					Row r = db.getTableFields(tablename);
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
									error_message+="Missing compare fields in target table:"+Const.CR;
								}
								error_found=true;
								error_message+="\t\t"+lufield+Const.CR; 
							}
						}
						if (error_found)
						{
							cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, error_message, stepMeta);
						}
						else
						{
							cr = new CheckResult(CheckResult.TYPE_RESULT_OK, "All lookup fields found in the table.", stepMeta);
						}
						remarks.add(cr);
						
						/* Also, check the fields: tk, version, from-to, ... */
						if ( r.searchValueIndex(technicalKeyField)<0)
						{
							error_message="Technical key ["+technicalKeyField+"] not found in target table."+Const.CR;
							cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, error_message, stepMeta);
						}
						else
						{
							error_message="Technical key ["+technicalKeyField+"] found in target table."+Const.CR;
							cr = new CheckResult(CheckResult.TYPE_RESULT_OK, error_message, stepMeta);
						}
						remarks.add(cr);
					}
					else
					{
						error_message="Couldn't read the table info, please check the table-name & permissions.";
						cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, error_message, stepMeta);
						remarks.add(cr);
					}
				}
				
				// Look up fields in the input stream <prev>
				if (prev!=null && prev.size()>0)
				{
					boolean first=true;
					error_message = "";
					boolean error_found = false;
					
					for (int i=0;i<keyField.length;i++)
					{
						Value v = prev.searchValue(keyField[i]);
						if (v==null)
						{
							if (first)
							{
								first=false;
								error_message+="Missing fields, not found in input from previous steps:"+Const.CR;
							}
							error_found=true;
							error_message+="\t\t"+keyField[i]+Const.CR; 
						}
					}
					if (error_found)
					{
						cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, error_message, stepMeta);
					}
					else
					{
						cr = new CheckResult(CheckResult.TYPE_RESULT_OK, "All fields found in the input stream.", stepMeta);
					}
					remarks.add(cr);
				}
				else
				{
					error_message="Couldn't read fields from the previous step."+Const.CR;
					cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, error_message, stepMeta);
					remarks.add(cr);
				}
				
				// Check sequence
				if (database.supportsSequences() && sequenceFrom!=null && sequenceFrom.length()!=0)
				{
					Value last = db.checkSequence(sequenceFrom);
					if (last!=null)
					{
						error_message = "No problem reading sequence "+sequenceFrom+", it's at value "+last;
						cr = new CheckResult(CheckResult.TYPE_RESULT_OK, error_message, stepMeta);
						remarks.add(cr);
					}
					else
					{
						error_message+="Error reading sequence "+sequenceFrom+"!";
						cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, error_message, stepMeta);
						remarks.add(cr);
					}
				}
			}
			catch(KettleException e)
			{
				error_message = "An error occurred: "+e.getMessage();
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
			error_message = "Please select or create a connection!";
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, error_message, stepMeta);
			remarks.add(cr);
		}
		
		// See if we have input streams leading to this step!
		if (input.length>0)
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, "Step is receiving info from other steps.", stepMeta);
			remarks.add(cr);
		}
		else
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, "No input received from other steps!", stepMeta);
			remarks.add(cr);
		}

	}
	
	public SQLStatement getSQLStatements(TransMeta transMeta, StepMeta stepMeta, Row prev)
	{
		SQLStatement retval = new SQLStatement(stepMeta.getName(), database, null); // default: nothing to do!
	
		if (database!=null)
		{
			if (prev!=null && prev.size()>0)
			{
				if (tablename!=null && tablename.length()>0)
				{
					Database db = new Database(database);
					try
					{
						db.connect();
						
						// OK, what do we put in the new table??
						Row fields = new Row();
						
						// First, the new technical key...
						Value vkeyfield = new Value(technicalKeyField, Value.VALUE_TYPE_INTEGER);
						vkeyfield.setLength(10,0);
						fields.addValue(vkeyfield);
						
						// Then the hashcode (optional)
						if (useHash && hashField!=null && hashField.length()>0)
						{
							Value vhashfield = new Value(hashField, Value.VALUE_TYPE_INTEGER);
							vhashfield.setLength(15,0);
							fields.addValue(vhashfield);
						}
						
						// Then the rest of the fields in the lookup-section
						for (int i=0;i<keyLookup.length;i++) 
						{
							String error_fields="";
							Value vkey = prev.searchValue(keyLookup[i]);
							if (vkey!=null)
							{
								fields.addValue(vkey);
							}
							else
							{
								if (error_fields.length()>0) error_fields+=", ";
								error_fields+=keyLookup[i];
							}
							if (error_fields.length()>0)
							{
								retval.setError("Can't find fields : "+error_fields);
							}
						}
						
						String cr_table = db.getDDL(tablename, 
													fields, 
													(sequenceFrom!=null && sequenceFrom.length()==0)?technicalKeyField:null, 
													useAutoinc, 
													null,
													true
													);
						
						//
						// OK, now let's build the index
						//
						
						// What fields do we put int the index?
						// Only the hashcode or all fields?
						String cr_index = "";
						String idx_fields[] = null;
						if (useHash)
						{
							if (hashField!=null && hashField.length()>0)
							{
								idx_fields = new String[] { hashField };
							}
							else
							{
								retval.setError("No hashfield is specified.  Please enter a name for the hashfield.");
							}
						}
						else  // index on all key fields...
						{
							if (keyLookup!=null && keyLookup.length>0)
							{
								int nrfields = keyLookup.length;
								if (nrfields>32 && database.getDatabaseType()==DatabaseMeta.TYPE_DATABASE_ORACLE)
								{
									nrfields=32;  // Oracle indexes are limited to 32 fields...
								}
								idx_fields = new String[nrfields];
								for (int i=0;i<nrfields;i++) idx_fields[i] = keyLookup[i];
							}
							else
							{
								retval.setError("No fields are specified.  Please specify the fields to combine in this table.");
							}
						}
						
						// OK, now get the create index statement... 
						if (idx_fields!=null && idx_fields.length>0 && 
							!db.checkIndexExists(tablename, idx_fields)
						   )
						{
							String indexname = "idx_"+tablename+"_lookup";
							cr_index = db.getCreateIndexStatement(tablename, indexname, idx_fields, false, false, false, true);
							cr_index+=Const.CR;
						}
						
						//
						// Don't forget the sequence (optional)
						//
						String cr_seq="";
						if ((database.getDatabaseType()==DatabaseMeta.TYPE_DATABASE_ORACLE) &&
						     sequenceFrom!=null && sequenceFrom.length()>0
						  )
						{
							if (!db.checkSequenceExists(sequenceFrom))
							{
								cr_seq+=db.getCreateSequenceStatement(sequenceFrom, 1L, 1L, -1L, true);
								cr_seq+=Const.CR;
							}
						}
						
						
						retval.setSQL(cr_table+cr_index+cr_seq);
					}
					catch(KettleException e)
					{
						retval.setError("An error occurred: "+Const.CR+e.getMessage());
					}
				}
				else
				{
					retval.setError("No table is defined on this connection.");
				}
			}
			else
			{
				retval.setError("Not receiving any fields from previous steps. Check the previous steps for errors & the connecting hops.");
			}
		}
		else
		{
			retval.setError("There is no connection defined in this step.");
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
											v!=null?v.getOrigin():"?",
											"",
											useHash?"read and insert":"key lookup and insert"
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
											"",
											"",
											"",
											"key lookup on hash-field"
											);
			impact.add(ii);
		}
	}
}

