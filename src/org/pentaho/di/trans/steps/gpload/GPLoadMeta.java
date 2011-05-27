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

package org.pentaho.di.trans.steps.gpload;

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
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.ObjectId;
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
 * Created on 20-feb-2007
 * 
 * @author Sven Boden
 */
public class GPLoadMeta extends BaseStepMeta implements StepMetaInterface
{
	private static Class<?> PKG = GPLoadMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	/** Collection of Local hosts **/
	private String localHosts[];
	
	/** LocalHostPort **/
	private String localhostPort;
	
    /** what's the schema for the target? */
    private String schemaName;

    /** what's the table for the target? */
	private String tableName;
	
	/** what's the target of the error table? */
	private String errorTableName;
	
	/** Path to the gpload utility */
	private String gploadPath;
	
	/** Path to the control file  */
	private String controlFile;
	
	/** Path to the data file */
	private String dataFile;
	
	/** Path to the log file */
	private String logFile;

    /** database connection */
	private DatabaseMeta databaseMeta;

    /** Specified database field */
	private String fieldTable[];

    /** Field name in the stream */
	private String fieldStream[];
	
	/** Database column to match on for an update or merge operation */
	private boolean matchColumn[];
	
	/** Database columns to update */
	private boolean updateColumn[];
	
	/** the date mask to use if the value is a date */
	private String dateMask[];

    /** maximum errors */
	private String    maxErrors;		
	
	/** Load method */
	private String loadMethod;
	
	/** Load action */
	private String loadAction;	
	
	/** Encoding to use */
	private String encoding;

    /** Erase files after use */
	private boolean eraseFiles; 
	
	/** Boolean to indicate that numbers are to be enclosed */
	private boolean encloseNumbers;
	
	/** Data file delimiter */
	private String delimiter;
	
	/** Default number of maximum errors allowed on a load */
	public static String MAX_ERRORS_DEFAULT = "50";
	
	/** Update condition **/
	private String updateCondition;
	
	/*
	 * Encodings supported by GPLoad.
	 * This list was obtained from the GPAAdminGuide.
	 */
	public final static String[] SUPPORTED_ENCODINGS = { 
	   "", "BIG5", 
	   "EUC_CN", "EUC_JP", "EUC_KR", "EUC_TW", 
	   "GB18030", "GBK", 
	   "ISO-8859-1", "ISO_8859_5", "ISO_8859_6", "ISO_8859_7", "ISO_8859_8", 
      "JOHAB", "KOI8", 
      "LATIN1", "LATIN2", "LATIN3", "LATIN4", "LATIN5", 
      "LATIN6", "LATIN7", "LATIN8", "LATIN9", "LATIN10", 
      "MULE_INTERNAL", "SJIS", "SQL_ASCII", "UHC", "UTF8", 
      "WIN866", "WIN874", "WIN1250", "WIN1251", "WIN1252", 
      "WIN1253", "WIN1254", "WIN1255", "WIN1256", "WIN1257", "WIN1258" }; //$NON-NLS-1$

	/*
	 * Do not translate following values!!! They are will end up in the job export.
	 */
	final static public String ACTION_INSERT   = "insert";
	final static public String ACTION_UPDATE   = "update";
	final static public String ACTION_MERGE    = "merge";

	/*
	 * Do not translate following values!!! They are will end up in the job export.
	 */
	// final static public String METHOD_AUTO_CONCURRENT = "AUTO_CONCURRENT";
	final static public String METHOD_AUTO_END        = "AUTO_END";
	final static public String METHOD_MANUAL          = "MANUAL";
	
	/*
	 * Do not translate following values!!! They are will end up in the job export.
	 */
	final static public String DATE_MASK_DATE     = "DATE";
	final static public String DATE_MASK_DATETIME = "DATETIME";
		
	public GPLoadMeta()
	{
		super();
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
     * @return Returns the tableName.
     */
    public String getTableName()
    {
        return tableName;
    }

    /**
     * @param tableName The tableName to set.
     */
    public void setTableName(String tableName)
    {
        this.tableName = tableName;
    }
    
    /**
     * @return Returns the errorTableName.
     */
    public String getErrorTableName()
    {
        return errorTableName;
    }

    /**
     * @param errorTableName The error table name to set.
     */
    public void setErrorTableName(String errorTableName)
    {
        this.errorTableName = errorTableName;
    }

	public String getGploadPath() {
		return gploadPath;
	}

	public void setGploadPath(String gploadPath) {
		this.gploadPath = gploadPath;
	}    
    
    /**
     * @return Returns the fieldTable.
     */
    public String[] getFieldTable()
    {
        return fieldTable;
    }

    /**
     * @param fieldTable The fieldTable to set.
     */
    public void setFieldTable(String[] fieldTable)
    {
        this.fieldTable = fieldTable;
    }

    /**
     * @return Returns the fieldStream.
     */
    public String[] getFieldStream()
    {
        return fieldStream;
    }

    /**
     * @param fieldStream The fieldStream to set.
     */
    public void setFieldStream(String[] fieldStream)
    {
        this.fieldStream = fieldStream;
    }

	public String[] getDateMask() {
		return dateMask;
	}

	public void setDateMask(String[] dateMask) {
		this.dateMask = dateMask;
	}

	public void loadXML(Node stepnode, List<DatabaseMeta> databases, Map<String, Counter> counters)
		throws KettleXMLException
	{
		readData(stepnode, databases);
	}

	public void allocate(int nrvalues)
	{
		fieldTable  = new String[nrvalues];
		fieldStream = new String[nrvalues];
		dateMask    = new String[nrvalues];
		matchColumn = new boolean[nrvalues];
		updateColumn = new boolean[nrvalues];
		
	}
	
	public void allocateLocalHosts(int numberOfLocalHosts) {
	   this.localHosts = new String[numberOfLocalHosts];
	}
	
	public Object clone()
	{
		GPLoadMeta retval = (GPLoadMeta)super.clone();
		int nrvalues  = fieldTable.length;

		retval.allocate(nrvalues);

		for (int i=0;i<nrvalues;i++)
		{
			retval.fieldTable[i]  = fieldTable[i];
			retval.fieldStream[i] = fieldStream[i];
			retval.dateMask[i]    = dateMask[i];
			retval.matchColumn[i] = matchColumn[i];
			retval.updateColumn[i] = updateColumn[i];
		}
		return retval;
	}

	private void readData(Node stepnode, List<? extends SharedObjectInterface> databases)
		throws KettleXMLException
	{
		try
		{
			String con     = XMLHandler.getTagValue(stepnode, "connection");   //$NON-NLS-1$
			databaseMeta   = DatabaseMeta.findDatabase(databases, con);
			maxErrors      = XMLHandler.getTagValue(stepnode, "errors");       //$NON-NLS-1$  
         schemaName     = XMLHandler.getTagValue(stepnode, "schema");       //$NON-NLS-1$
			tableName      = XMLHandler.getTagValue(stepnode, "table");        //$NON-NLS-1$
			errorTableName = XMLHandler.getTagValue(stepnode, "error_table");        //$NON-NLS-1$
			loadMethod     = XMLHandler.getTagValue(stepnode, "load_method");  //$NON-NLS-1$
			loadAction     = XMLHandler.getTagValue(stepnode, "load_action");  //$NON-NLS-1$			
			gploadPath     = XMLHandler.getTagValue(stepnode, "gpload_path");       //$NON-NLS-1$
			controlFile    = XMLHandler.getTagValue(stepnode, "control_file"); //$NON-NLS-1$
			dataFile       = XMLHandler.getTagValue(stepnode, "data_file");    //$NON-NLS-1$
			delimiter      = XMLHandler.getTagValue(stepnode, "delimiter");    //$NON-NLS-1$
			logFile        = XMLHandler.getTagValue(stepnode, "log_file");     //$NON-NLS-1$
			eraseFiles     = "Y".equalsIgnoreCase( XMLHandler.getTagValue(stepnode, "erase_files")); //$NON-NLS-1$
			encoding       = XMLHandler.getTagValue(stepnode, "encoding");         //$NON-NLS-1$
         updateCondition = XMLHandler.getTagValue(stepnode, "update_condition");         //$NON-NLS-1$;
			
			Node localHostsNode = XMLHandler.getSubNode(stepnode, "local_hosts");
			int nLocalHosts = XMLHandler.countNodes(localHostsNode, "local_host");//$NON-NLS-1$
			allocateLocalHosts(nLocalHosts);
			for (int i=0; i<nLocalHosts; i++) {
			   Node localHostNode = XMLHandler.getSubNodeByNr(localHostsNode, "local_host", i);    //$NON-NLS-1$
			   localHosts[i]      = XMLHandler.getNodeValue(localHostNode); //$NON-NLS-1$
			}
			localhostPort = XMLHandler.getTagValue(stepnode, "localhost_port");        //$NON-NLS-1$
			
			int nrvalues = XMLHandler.countNodes(stepnode, "mapping");      //$NON-NLS-1$
			allocate(nrvalues);

			for (int i=0;i<nrvalues;i++)
			{
				Node vnode = XMLHandler.getSubNodeByNr(stepnode, "mapping", i);    //$NON-NLS-1$

				fieldTable[i]      = XMLHandler.getTagValue(vnode, "stream_name"); //$NON-NLS-1$
				fieldStream[i]     = XMLHandler.getTagValue(vnode, "field_name");  //$NON-NLS-1$
				if (fieldStream[i]==null) fieldStream[i]=fieldTable[i];            // default: the same name!
				String locDateMask = XMLHandler.getTagValue(vnode, "date_mask");   //$NON-NLS-1$
				if(locDateMask==null) {
					dateMask[i] = "";
				} 
				else
                {
                    if (GPLoadMeta.DATE_MASK_DATE.equals(locDateMask) ||
                        GPLoadMeta.DATE_MASK_DATETIME.equals(locDateMask) )
                    {
                        dateMask[i] = locDateMask;
                    }
                    else
                    {
                    	dateMask[i] = "";
                    }
				}
				                  
				
				matchColumn[i] = ("Y".equalsIgnoreCase(XMLHandler.getTagValue(vnode, "match_column"))); //$NON-NLS-1$
				updateColumn[i] = ("Y".equalsIgnoreCase(XMLHandler.getTagValue(vnode, "update_column")));  //$NON-NLS-1$
			}
		}
		catch(Exception e)
		{
			throw new KettleXMLException(BaseMessages.getString(PKG, "GPLoadMeta.Exception.UnableToReadStepInfoFromXML"), e); //$NON-NLS-1$
		}
	}

	public void setDefault()
	{
	   
	   //  TODO: Make non empty defaults public static Strings
	   
		fieldTable     = null;
		databaseMeta   = null;
		maxErrors      = GPLoadMeta.MAX_ERRORS_DEFAULT;
      schemaName     = "";                //$NON-NLS-1$
      localhostPort  = "";
		tableName      = BaseMessages.getString(PKG, "GPLoadMeta.DefaultTableName"); //$NON-NLS-1$
		errorTableName = ""; //BaseMessages.getString(PKG, "GPLocal.ErrorTable.Prefix")+tableName;
		loadMethod     = METHOD_AUTO_END;
		loadAction     = ACTION_INSERT;
		gploadPath     = "/usr/local/greenplum-db/bin/gpload";                              //$NON-NLS-1$
		controlFile    = "control${Internal.Step.CopyNr}.cfg";  //$NON-NLS-1$
		dataFile       = "load${Internal.Step.CopyNr}.dat";     //$NON-NLS-1$
		logFile        = "";                                    //$NON-NLS-1$
      encoding       = "";                                    //$NON-NLS-1$
		delimiter      = ",";
		encloseNumbers = false;
      eraseFiles     = true;
      updateCondition = "";

		allocate(0);	
		allocateLocalHosts(0);
	}

	public String getXML()
	{
        StringBuffer retval = new StringBuffer(300);

		retval.append("    ").append(XMLHandler.addTagValue("connection",   databaseMeta==null?"":databaseMeta.getName())); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		retval.append("    ").append(XMLHandler.addTagValue("errors",       maxErrors));     //$NON-NLS-1$ //$NON-NLS-2$
      retval.append("    ").append(XMLHandler.addTagValue("schema",       schemaName));    //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("    ").append(XMLHandler.addTagValue("table",        tableName));     //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("    ").append(XMLHandler.addTagValue("error_table",  errorTableName));     //$NON-NLS-1$ //$NON-NLS-2$
      retval.append("    ").append(XMLHandler.addTagValue("load_method",  loadMethod));    //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("    ").append(XMLHandler.addTagValue("load_action",  loadAction));    //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("    ").append(XMLHandler.addTagValue("gpload_path",  gploadPath));        //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("    ").append(XMLHandler.addTagValue("control_file", controlFile));   //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("    ").append(XMLHandler.addTagValue("data_file",    dataFile));      //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("    ").append(XMLHandler.addTagValue("delimiter",    delimiter));      //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("    ").append(XMLHandler.addTagValue("log_file",     logFile));       //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("    ").append(XMLHandler.addTagValue("erase_files",  eraseFiles));    //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("    ").append(XMLHandler.addTagValue("encoding",     encoding));      //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("    ").append(XMLHandler.addTagValue("enclose_numbers", (encloseNumbers?"Y":"N")));      //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("    ").append(XMLHandler.addTagValue("localhost_port",  localhostPort));       //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("    ").append(XMLHandler.addTagValue("update_condition",  updateCondition));       //$NON-NLS-1$ //$NON-NLS-2$
		
		for (int i=0;i<fieldTable.length;i++)
		{
			retval.append("      <mapping>").append(Const.CR); //$NON-NLS-1$
			retval.append("        ").append(XMLHandler.addTagValue("stream_name", fieldTable[i])); //$NON-NLS-1$ //$NON-NLS-2$
			retval.append("        ").append(XMLHandler.addTagValue("field_name",  fieldStream[i])); //$NON-NLS-1$ //$NON-NLS-2$
			retval.append("        ").append(XMLHandler.addTagValue("date_mask",   dateMask[i])); //$NON-NLS-1$ //$NON-NLS-2$
			retval.append("        ").append(XMLHandler.addTagValue("match_column",   (matchColumn[i]?"Y":"N"))); //$NON-NLS-1$ //$NON-NLS-2$
			retval.append("        ").append(XMLHandler.addTagValue("update_column",   (updateColumn[i]?"Y":"N"))); //$NON-NLS-1$ //$NON-NLS-2$
			retval.append("      </mapping>").append(Const.CR); //$NON-NLS-1$
		}
		
		retval.append("      <local_hosts>").append(Const.CR); //$NON-NLS-1$
		for (String localHost: localHosts) {
		   retval.append("        ").append(XMLHandler.addTagValue("local_host", localHost)); //$NON-NLS-1$ //$NON-NLS-2$
		}
		retval.append("      </local_hosts>").append(Const.CR); //$NON-NLS-1$
		
		return retval.toString();
	}

	public void readRep(Repository rep, ObjectId id_step, List<DatabaseMeta> databases, Map<String, Counter> counters)
		throws KettleException
	{
		try
		{
			databaseMeta = rep.loadDatabaseMetaFromStepAttribute(id_step, "id_connection", databases);
     		maxErrors      =      rep.getStepAttributeString(id_step, "errors");         //$NON-NLS-1$
         schemaName     =      rep.getStepAttributeString(id_step,  "schema");         //$NON-NLS-1$
			tableName      =      rep.getStepAttributeString(id_step,  "table");          //$NON-NLS-1$
			errorTableName =      rep.getStepAttributeString(id_step,  "error_table");    //$NON-NLS-1$
			loadMethod     =      rep.getStepAttributeString(id_step,  "load_method");    //$NON-NLS-1$
			loadAction     =      rep.getStepAttributeString(id_step,  "load_action");    //$NON-NLS-1$
			gploadPath     =      rep.getStepAttributeString(id_step,  "gpload_path");    //$NON-NLS-1$
			controlFile    =      rep.getStepAttributeString(id_step,  "control_file");   //$NON-NLS-1$
			dataFile       =      rep.getStepAttributeString(id_step,  "data_file");      //$NON-NLS-1$
			delimiter       =     rep.getStepAttributeString(id_step,  "delimiter");      //$NON-NLS-1$
         logFile        =      rep.getStepAttributeString(id_step,  "log_file");       //$NON-NLS-1$
			eraseFiles     =      rep.getStepAttributeBoolean(id_step, "erase_files");    //$NON-NLS-1$
			encoding       =      rep.getStepAttributeString(id_step,  "encoding");       //$NON-NLS-1$
			localhostPort  =      rep.getStepAttributeString(id_step, "localhost_port");    //$NON-NLS-1$
			encloseNumbers =      (rep.getStepAttributeString(id_step, "enclose_numbers").equalsIgnoreCase("Y")?true:false); //$NON-NLS-1$  
			updateCondition =   rep.getStepAttributeString(id_step, "update_condition");    //$NON-NLS-1$
	
			int numberOfLocalHosts = rep.countNrStepAttributes(id_step, "local_host");
			allocateLocalHosts(numberOfLocalHosts);
         for (int i=0; i< numberOfLocalHosts; i++) {
            localHosts[i] = rep.getStepAttributeString(id_step, i, "local_host");   //$NON-NLS-1$
         }
			
         int nrvalues = rep.countNrStepAttributes(id_step, "stream_name");             //$NON-NLS-1$
         allocate(nrvalues);

         for (int i=0;i<nrvalues;i++)
         {
            fieldTable[i]  = rep.getStepAttributeString(id_step, i, "stream_name");   //$NON-NLS-1$
            fieldStream[i] = rep.getStepAttributeString(id_step, i, "field_name");    //$NON-NLS-1$
            dateMask[i]    = rep.getStepAttributeString(id_step, i, "date_mask");     //$NON-NLS-1$
            matchColumn[i] = rep.getStepAttributeBoolean(id_step, i, "match_column");     //$NON-NLS-1$
            updateColumn[i] = rep.getStepAttributeBoolean(id_step, i, "update_column");     //$NON-NLS-1$
         }
		}
		catch(Exception e)
		{
			throw new KettleException(BaseMessages.getString(PKG, "GPLoadMeta.Exception.UnexpectedErrorReadingStepInfoFromRepository"), e); //$NON-NLS-1$
		}
	}

	public void saveRep(Repository rep, ObjectId id_transformation, ObjectId id_step)
		throws KettleException
	{
		try
		{
			rep.saveDatabaseMetaStepAttribute(id_transformation, id_step, "id_connection", databaseMeta);
			rep.saveStepAttribute(id_transformation, id_step, "errors",          maxErrors);     //$NON-NLS-1$
         rep.saveStepAttribute(id_transformation, id_step, "schema",          schemaName);    //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "table",           tableName);     //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "error_table",      errorTableName);     //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "load_method",     loadMethod);    //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "load_action",     loadAction);    //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "gpload_path",     gploadPath);        //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "control_file",    controlFile);   //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "data_file",       dataFile);      //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "delimiter",       delimiter);      //$NON-NLS-1$
         rep.saveStepAttribute(id_transformation, id_step, "log_file",        logFile);       //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "erase_files",     eraseFiles);    //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "encoding",        encoding);      //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "enclose_numbers", (encloseNumbers?"Y":"N"));//$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "localhost_port", localhostPort);//$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "update_condition", updateCondition);//$NON-NLS-1$

			for (int i=0;i <localHosts.length; i++) {
			   rep.saveStepAttribute(id_transformation, id_step, i, "local_host", localHosts[i]);  //$NON-NLS-1$
			}
			
			for (int i=0;i<fieldTable.length;i++)
			{
				rep.saveStepAttribute(id_transformation, id_step, i, "stream_name", fieldTable[i]);  //$NON-NLS-1$
				rep.saveStepAttribute(id_transformation, id_step, i, "field_name",  fieldStream[i]); //$NON-NLS-1$
				rep.saveStepAttribute(id_transformation, id_step, i, "date_mask",   dateMask[i]);    //$NON-NLS-1$
				rep.saveStepAttribute(id_transformation, id_step, i, "match_column",   matchColumn[i]);    //$NON-NLS-1$
				rep.saveStepAttribute(id_transformation, id_step, i, "update_column",   updateColumn[i]);    //$NON-NLS-1$
			}

			// Also, save the step-database relationship!
			if (databaseMeta!=null) rep.insertStepDatabase(id_transformation, id_step, databaseMeta.getObjectId());
		}
		catch(Exception e)
		{
			throw new KettleException(BaseMessages.getString(PKG, "GPLoadMeta.Exception.UnableToSaveStepInfoToRepository")+id_step, e); //$NON-NLS-1$
		}
	}
	
	public void getFields(RowMetaInterface rowMeta, String origin, RowMetaInterface[] info, StepMeta nextStep, VariableSpace space) throws KettleStepException
	{
		// Default: nothing changes to rowMeta
	}

	public void check(List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta, RowMetaInterface prev, String input[], String output[], RowMetaInterface info)
	{
		CheckResult cr;
		String error_message = ""; //$NON-NLS-1$

		if (databaseMeta!=null)
		{
			Database db = new Database(loggingObject, databaseMeta);
			db.shareVariablesWith(transMeta);
			try
			{
				db.connect();

				if (!Const.isEmpty(tableName))
				{
					cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(PKG, "GPLoadMeta.CheckResult.TableNameOK"), stepMeta); //$NON-NLS-1$
					remarks.add(cr);

					boolean first=true;
					boolean error_found=false;
					error_message = ""; //$NON-NLS-1$
					
					// Check fields in table
                    String schemaTable = databaseMeta.getQuotedSchemaTableCombination(
                    		                   transMeta.environmentSubstitute(schemaName), 
                    		                   transMeta.environmentSubstitute(tableName));
					RowMetaInterface r = db.getTableFields(schemaTable);
					if (r!=null)
					{
						cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(PKG, "GPLoadMeta.CheckResult.TableExists"), stepMeta); //$NON-NLS-1$
						remarks.add(cr);

						// How about the fields to insert/dateMask in the table?
						first=true;
						error_found=false;
						error_message = ""; //$NON-NLS-1$
						
						for (int i=0;i<fieldTable.length;i++)
						{
							String field = fieldTable[i];

							ValueMetaInterface v = r.searchValueMeta(field);
							if (v==null)
							{
								if (first)
								{
									first=false;
									error_message+=BaseMessages.getString(PKG, "GPLoadMeta.CheckResult.MissingFieldsToLoadInTargetTable")+Const.CR; //$NON-NLS-1$
								}
								error_found=true;
								error_message+="\t\t"+field+Const.CR;  //$NON-NLS-1$
							}
						}
						if (error_found)
						{
							cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepMeta);
						}
						else
						{
							cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(PKG, "GPLoadMeta.CheckResult.AllFieldsFoundInTargetTable"), stepMeta); //$NON-NLS-1$
						}
						remarks.add(cr);
					}
					else
					{
						error_message=BaseMessages.getString(PKG, "GPLoadMeta.CheckResult.CouldNotReadTableInfo"); //$NON-NLS-1$
						cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepMeta);
						remarks.add(cr);
					}
				}

				// Look up fields in the input stream <prev>
				if (prev!=null && prev.size()>0)
				{
					cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(PKG, "GPLoadMeta.CheckResult.StepReceivingDatas",prev.size()+""), stepMeta); //$NON-NLS-1$ //$NON-NLS-2$
					remarks.add(cr);

					boolean first=true;
					error_message = ""; //$NON-NLS-1$
					boolean error_found = false;

					for (int i=0;i<fieldStream.length;i++)
					{
						ValueMetaInterface v = prev.searchValueMeta(fieldStream[i]);
						if (v==null)
						{
							if (first)
							{
								first=false;
								error_message+=BaseMessages.getString(PKG, "GPLoadMeta.CheckResult.MissingFieldsInInput")+Const.CR; //$NON-NLS-1$
							}
							error_found=true;
							error_message+="\t\t"+fieldStream[i]+Const.CR;  //$NON-NLS-1$
						}
					}
					if (error_found)
 					{
						cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepMeta);
					}
					else
					{
						cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(PKG, "GPLoadMeta.CheckResult.AllFieldsFoundInInput"), stepMeta); //$NON-NLS-1$
					}
					remarks.add(cr);
				}
				else
				{
					error_message=BaseMessages.getString(PKG, "GPLoadMeta.CheckResult.MissingFieldsInInput3")+Const.CR; //$NON-NLS-1$
					cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepMeta);
					remarks.add(cr);
				}
			}
			catch(KettleException e)
			{
				error_message = BaseMessages.getString(PKG, "GPLoadMeta.CheckResult.DatabaseErrorOccurred")+e.getMessage(); //$NON-NLS-1$
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
			error_message = BaseMessages.getString(PKG, "GPLoadMeta.CheckResult.InvalidConnection"); //$NON-NLS-1$
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepMeta);
			remarks.add(cr);
		}

		// See if we have input streams leading to this step!
		if (input.length>0)
		{
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(PKG, "GPLoadMeta.CheckResult.StepReceivingInfoFromOtherSteps"), stepMeta); //$NON-NLS-1$
			remarks.add(cr);
		}
		else
		{
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(PKG, "GPLoadMeta.CheckResult.NoInputError"), stepMeta); //$NON-NLS-1$
			remarks.add(cr);
		}
	}

	public SQLStatement getSQLStatements(TransMeta transMeta, StepMeta stepMeta, RowMetaInterface prev) throws KettleStepException
	{
		SQLStatement retval = new SQLStatement(stepMeta.getName(), databaseMeta, null); // default: nothing to do!

		if (databaseMeta!=null)
		{
			if (prev!=null && prev.size()>0)
			{
                // Copy the row
                RowMetaInterface tableFields = new RowMeta();

                // Now change the field names
                for (int i=0;i<fieldTable.length;i++)
                {
                    ValueMetaInterface v = prev.searchValueMeta(fieldStream[i]);
                    if (v!=null)
                    {
                        ValueMetaInterface tableField = v.clone();
                        tableField.setName(fieldTable[i]);
                        tableFields.addValueMeta(tableField);
                    }
                    else
                    {
                        throw new KettleStepException("Unable to find field ["+fieldStream[i]+"] in the input rows");
                    }
                }

				if (!Const.isEmpty(tableName))
				{
                    Database db = new Database(loggingObject, databaseMeta);
                    db.shareVariablesWith(transMeta);
					try
					{
						db.connect();

                        String schemaTable = databaseMeta.getQuotedSchemaTableCombination(transMeta.environmentSubstitute(schemaName), 
                        		                                                          transMeta.environmentSubstitute(tableName));                        
						String sql = db.getDDL(schemaTable,
													tableFields,
													null,
													false,
													null,
													true
													);

						if (sql.length()==0) retval.setSQL(null); else retval.setSQL(sql);
					}
					catch(KettleException e)
					{
						retval.setError(BaseMessages.getString(PKG, "GPLoadMeta.GetSQL.ErrorOccurred")+e.getMessage()); //$NON-NLS-1$
					}
				}
				else
				{
					retval.setError(BaseMessages.getString(PKG, "GPLoadMeta.GetSQL.NoTableDefinedOnConnection")); //$NON-NLS-1$
				}
			}
			else
			{
				retval.setError(BaseMessages.getString(PKG, "GPLoadMeta.GetSQL.NotReceivingAnyFields")); //$NON-NLS-1$
			}
		}
		else
		{
			retval.setError(BaseMessages.getString(PKG, "GPLoadMeta.GetSQL.NoConnectionDefined")); //$NON-NLS-1$
		}

		return retval;
	}

	public void analyseImpact(List<DatabaseImpact> impact, TransMeta transMeta, StepMeta stepMeta, RowMetaInterface prev, String input[], String output[], RowMetaInterface info) throws KettleStepException
    {
        if (prev != null)
        {
            /* DEBUG CHECK THIS */
            // Insert dateMask fields : read/write
            for (int i = 0; i < fieldTable.length; i++)
            {
                ValueMetaInterface v = prev.searchValueMeta(fieldStream[i]);

                DatabaseImpact ii = new DatabaseImpact(DatabaseImpact.TYPE_IMPACT_READ_WRITE, transMeta.getName(), stepMeta.getName(), databaseMeta.getDatabaseName(), 
                		transMeta.environmentSubstitute(tableName), fieldTable[i], fieldStream[i], v!=null?v.getOrigin():"?", "", "Type = " + v.toStringMeta()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                impact.add(ii);
            }
        }
	}

	public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta, Trans trans)
	{
		return new GPLoad(stepMeta, stepDataInterface, cnr, transMeta, trans);
	}

	public StepDataInterface getStepData()
	{
		return new GPLoadData();
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

    public RowMetaInterface getRequiredFields(VariableSpace space) throws KettleException
    {
    	String realTableName = space.environmentSubstitute(tableName);
    	String realSchemaName = space.environmentSubstitute(schemaName);
    	
        if (databaseMeta!=null)
        {
            Database db = new Database(loggingObject, databaseMeta);
            try
            {
                db.connect();

                if (!Const.isEmpty(realTableName))
                {
                    String schemaTable = databaseMeta.getQuotedSchemaTableCombination(realSchemaName, realTableName);

                    // Check if this table exists...
                    if (db.checkTableExists(schemaTable))
                    {
                        return db.getTableFields(schemaTable);
                    }
                    else
                    {
                        throw new KettleException(BaseMessages.getString(PKG, "GPLoadMeta.Exception.TableNotFound"));
                    }
                }
                else
                {
                    throw new KettleException(BaseMessages.getString(PKG, "GPLoadMeta.Exception.TableNotSpecified"));
                }
            }
            catch(Exception e)
            {
                throw new KettleException(BaseMessages.getString(PKG, "GPLoadMeta.Exception.ErrorGettingFields"), e);
            }
            finally
            {
                db.disconnect();
            }
        }
        else
        {
            throw new KettleException(BaseMessages.getString(PKG, "GPLoadMeta.Exception.ConnectionNotDefined"));
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

    public String getControlFile() {
		return controlFile;
	}

	public void setControlFile(String controlFile) {
		this.controlFile = controlFile;
	}

	public String getDataFile() {
		return dataFile;
	}

	public void setDataFile(String dataFile) {
		this.dataFile = dataFile;
	}

    public String getLogFile() {
		return logFile;
	}

	public void setLogFile(String logFile) {
		this.logFile = logFile;
	}
	
	public void setLoadAction(String action)
	{
	    this.loadAction = action;
	}

	public String getLoadAction()
	{
	    return this.loadAction;
	}

	public void setLoadMethod(String method)
	{
	    this.loadMethod = method;
	}

	public String getLoadMethod()
	{
	    return this.loadMethod;
	}

	public String getEncoding() {
		return encoding;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	public void setDelimiter(String delimiter) {
	   this.delimiter = delimiter;
	}

	public String getDelimiter() {
		return delimiter;
	}

	public String getEnclosure() {
		return "";
	}

	public boolean isEraseFiles() {
		return eraseFiles;
	}

	public void setEraseFiles(boolean eraseFiles) {
		this.eraseFiles = eraseFiles;
	}

	public String getMaxErrors() {
		return maxErrors;
	}

	public void setMaxErrors(String maxErrors) {
		this.maxErrors = maxErrors;
	}
	
	public void setEncloseNumbers(boolean encloseNumbers) {
	   this.encloseNumbers = encloseNumbers;
	}
	
	public boolean getEncloseNumbers() {
	   return this.encloseNumbers;
	}
	
	public void setLocalHosts(String[] localHosts) {
	   this.localHosts = localHosts;
	}
	
	public String[] getLocalHosts() {
	   return localHosts;
	}

	public void setLocalhostPort(String localhostPort) {
	  this.localhostPort = localhostPort;
   }
	   
	public String getLocalhostPort() {
	  return localhostPort;
	}
	
	public void setMatchColumns(boolean[] matchColumn) {
	   this.matchColumn = matchColumn;
	}
	
	public boolean[] getMatchColumn() {
	   return matchColumn;
	}
	
	public void setUpdateColumn(boolean[] updateColumn) {
	   this.updateColumn = updateColumn;
	}
	
	public boolean[] getUpdateColumn() {
	   return updateColumn;
	}
	
	public boolean hasMatchColumn() {
	   
	   for (boolean matchColumn: this.matchColumn) {
	      if (matchColumn) {
	         return true;
	      }
	   }
	   return false;
	}
	
	public boolean hasUpdateColumn() {
	   
	   for (boolean updateColumn: this.updateColumn) {
	       if (updateColumn) {
	          return true;
	       }
	   }
	   
	   return false;
	}
	
	public void setUpdateCondition(String updateCondition) {
	   this.updateCondition = updateCondition;
	}
	
	public String getUpdateCondition() {
	   return updateCondition;
	}
}