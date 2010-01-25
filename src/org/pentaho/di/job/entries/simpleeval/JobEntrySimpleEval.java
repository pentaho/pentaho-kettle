
/*
 * Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Samatar Hassan.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
*/

package org.pentaho.di.job.entries.simpleeval;

import org.w3c.dom.Node;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Date;
import java.util.regex.Pattern;

import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobEntryType;
import org.pentaho.di.job.entry.JobEntryBase;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.Repository;



/**
 * This defines a 'simple evaluation' job entry.
 * 
 * @author Samatar Hassan
 * @since 01-01-2009
 */

public class JobEntrySimpleEval extends JobEntryBase implements Cloneable, JobEntryInterface
{
	
	public static final String[] valueTypeDesc = new String[] { 
		Messages.getString("JobSimpleEval.EvalPreviousField.Label"), 
		Messages.getString("JobSimpleEval.EvalVariable.Label"),
	
	};
	public static final String[] valueTypeCode = new String[] { 
		"field", 
		"variable"
	};
	public static final int VALUE_TYPE_FIELD=0;
	public static final int VALUE_TYPE_VARIABLE=1;
	public int valuetype;
	
	
	public static final String[] successConditionDesc = new String[] { 
		Messages.getString("JobSimpleEval.SuccessWhenEqual.Label"), 
		Messages.getString("JobSimpleEval.SuccessWhenDifferent.Label"),
		Messages.getString("JobSimpleEval.SuccessWhenContains.Label"),
		Messages.getString("JobSimpleEval.SuccessWhenNotContains.Label"),
		Messages.getString("JobSimpleEval.SuccessWhenStartWith.Label"),
		Messages.getString("JobSimpleEval.SuccessWhenNotStartWith.Label"),
		Messages.getString("JobSimpleEval.SuccessWhenEndWith.Label"),
		Messages.getString("JobSimpleEval.SuccessWhenNotEndWith.Label"),
		Messages.getString("JobSimpleEval.SuccessWhenRegExp.Label")

		
	};
	public static final String[] successConditionCode = new String[] { 
		"equal", 
		"different",
		"contains",
		"notcontains",
		"startswith",
		"notstatwith",
		"endswith",
		"notendwith",
		"regexp"
	};
	public static final int SUCCESS_CONDITION_EQUAL=0;
	public static final int SUCCESS_CONDITION_DIFFERENT=1;
	public static final int SUCCESS_CONDITION_CONTAINS=2;
	public static final int SUCCESS_CONDITION_NOT_CONTAINS=3;
	public static final int SUCCESS_CONDITION_START_WITH=4;
	public static final int SUCCESS_CONDITION_NOT_START_WITH=5;
	public static final int SUCCESS_CONDITION_END_WITH=6;
	public static final int SUCCESS_CONDITION_NOT_END_WITH=7;
	public static final int SUCCESS_CONDITION_REGEX=8;

	public int successcondition;


	public static final String[] fieldTypeDesc = new String[] { 
		Messages.getString("JobSimpleEval.FieldTypeString.Label"), 
		Messages.getString("JobSimpleEval.FieldTypeNumber.Label"),
		Messages.getString("JobSimpleEval.FieldTypeDateTime.Label"),
	
	};
	public static final String[] fieldTypeCode = new String[] { 
		"string", 
		"number",
		"datetime"
	};
	public static final int FIELD_TYPE_STRING=0;
	public static final int FIELD_TYPE_NUMBER=1;
	public static final int FIELD_TYPE_DATE_TIME=7;
	
	public int fieldtype;


	public static final String[] successNumberConditionDesc = new String[] { 
		Messages.getString("JobSimpleEval.SuccessWhenEqual.Label"), 
		Messages.getString("JobSimpleEval.SuccessWhenDifferent.Label"),
		Messages.getString("JobSimpleEval.SuccessWhenSmallThan.Label"),
		Messages.getString("JobSimpleEval.SuccessWhenSmallOrEqualThan.Label"),
		Messages.getString("JobSimpleEval.SuccessWhenGreaterThan.Label"),
		Messages.getString("JobSimpleEval.SuccessWhenGreaterOrEqualThan.Label"),
		Messages.getString("JobSimpleEval.SuccessBetween.Label"),
	
	};
	public static final String[] successNumberConditionCode = new String[] { 
		"equal", 
		"different",
		"smaller",
		"smallequal",
		"greater",
		"greaterequal",
		"between",
	};
	public static final int SUCCESS_NUMBER_CONDITION_EQUAL=0;
	public static final int SUCCESS_NUMBER_CONDITIONDIFFERENT=1;
	public static final int SUCCESS_NUMBER_CONDITION_SMALLER=2;
	public static final int SUCCESS_NUMBER_CONDITION_SMALLEREQUAL=3;
	public static final int SUCCESS_NUMBER_CONDITION_GREATER=4;
	public static final int SUCCESS_NUMBER_CONDITION_GREATEREQUAL=5;
	public static final int SUCCESS_NUMBER_CONDITION_BETWEEN=6;
	
	public int successnumbercondition;
	
	
	
	private String fieldname;
	private String variablename;
	private String mask;
	private String comparevalue;
	private String minvalue;
	private String maxvalue;

	public JobEntrySimpleEval(String n)
	{
		super(n, "");
		valuetype=VALUE_TYPE_FIELD;
		successcondition=SUCCESS_CONDITION_EQUAL;
		successnumbercondition=SUCCESS_CONDITION_EQUAL;
		minvalue=null;
		maxvalue=null;
		comparevalue=null;
		fieldname=null;
		variablename=null;
		fieldtype=FIELD_TYPE_STRING;
		mask=null;
		
		setID(-1L);
		setJobEntryType(JobEntryType.SIMPLE_EVAL);
	}

	public JobEntrySimpleEval()
	{
		this("");
	}

	public JobEntrySimpleEval(JobEntryBase jeb)
	{
		super(jeb);
	}

	public Object clone()
	{
		JobEntrySimpleEval je = (JobEntrySimpleEval) super.clone();
		return je;
	}
	private static String getValueTypeCode(int i) {
		if (i < 0 || i >= valueTypeCode.length)
			return valueTypeCode[0];
		return valueTypeCode[i];
	}
	private static String getFieldTypeCode(int i) {
		if (i < 0 || i >= fieldTypeCode.length)
			return fieldTypeCode[0];
		return fieldTypeCode[i];
	}
	
	private static String getSuccessConditionCode(int i) {
		if (i < 0 || i >= successConditionCode.length)
			return successConditionCode[0];
		return successConditionCode[i];
	}
	private static String getSuccessNumberConditionCode(int i) {
		if (i < 0 || i >= successNumberConditionCode.length)
			return successNumberConditionCode[0];
		return successNumberConditionCode[i];
	}
	public String getXML()
	{
		StringBuffer retval = new StringBuffer(300);
		
		retval.append(super.getXML());				
		retval.append("      ").append(XMLHandler.addTagValue("valuetype",getValueTypeCode(valuetype)));
		retval.append("      ").append(XMLHandler.addTagValue("fieldname", fieldname));
		retval.append("      ").append(XMLHandler.addTagValue("variablename", variablename));
		retval.append("      ").append(XMLHandler.addTagValue("fieldtype",getFieldTypeCode(fieldtype)));
		retval.append("      ").append(XMLHandler.addTagValue("mask", mask));
		retval.append("      ").append(XMLHandler.addTagValue("comparevalue", comparevalue));
		retval.append("      ").append(XMLHandler.addTagValue("minvalue", minvalue));
		retval.append("      ").append(XMLHandler.addTagValue("maxvalue", maxvalue));
		retval.append("      ").append(XMLHandler.addTagValue("successcondition",getSuccessConditionCode(successcondition)));
		retval.append("      ").append(XMLHandler.addTagValue("successnumbercondition",getSuccessNumberConditionCode(successnumbercondition)));
		
		return retval.toString();
	}
	
	private static int getValueTypeByCode(String tt) {
		if (tt == null)
			return 0;

		for (int i = 0; i < valueTypeCode.length; i++) {
			if (valueTypeCode[i].equalsIgnoreCase(tt))
				return i;
		}
		return 0;
	}
	private static int getSuccessNumberByCode(String tt) {
		if (tt == null)
			return 0;

		for (int i = 0; i < successNumberConditionCode.length; i++) {
			if (successNumberConditionCode[i].equalsIgnoreCase(tt))
				return i;
		}
		return 0;
	}

	private static int getFieldTypeByCode(String tt) {
		if (tt == null)
			return 0;

		for (int i = 0; i < fieldTypeCode.length; i++) {
			if (fieldTypeCode[i].equalsIgnoreCase(tt))
				return i;
		}
		return 0;
	}
	
	private static int getSuccessConditionByCode(String tt) {
		if (tt == null)
			return 0;

		for (int i = 0; i < successConditionCode.length; i++) {
			if (successConditionCode[i].equalsIgnoreCase(tt))
				return i;
		}
		return 0;
	}

	private static int getSuccessNumberConditionByCode(String tt) {
		if (tt == null)
			return 0;

		for (int i = 0; i < successNumberConditionCode.length; i++) {
			if (successNumberConditionCode[i].equalsIgnoreCase(tt))
				return i;
		}
		return 0;
	}
	public void loadXML(Node entrynode, List<DatabaseMeta> databases, List<SlaveServer> slaveServers, Repository rep) throws KettleXMLException
	{
	 try
	 {
		  super.loadXML(entrynode, databases, slaveServers);

			valuetype = getValueTypeByCode(Const.NVL(XMLHandler.getTagValue(entrynode,	"valuetype"), ""));
			fieldname          = XMLHandler.getTagValue(entrynode, "fieldname");
			fieldtype = getFieldTypeByCode(Const.NVL(XMLHandler.getTagValue(entrynode,	"fieldtype"), ""));
			variablename          = XMLHandler.getTagValue(entrynode, "variablename");
			mask          = XMLHandler.getTagValue(entrynode, "mask");
			comparevalue          = XMLHandler.getTagValue(entrynode, "comparevalue");
			minvalue          = XMLHandler.getTagValue(entrynode, "minvalue");
			maxvalue          = XMLHandler.getTagValue(entrynode, "maxvalue");
			successcondition = getSuccessConditionByCode(Const.NVL(XMLHandler.getTagValue(entrynode,	"successcondition"), ""));
			successnumbercondition = getSuccessNumberConditionByCode(Const.NVL(XMLHandler.getTagValue(entrynode,	"successnumbercondition"), ""));

		}	
		catch(KettleXMLException xe)
		{	
			throw new KettleXMLException(Messages.getString("JobEntrySimple.Error.Exception.UnableLoadXML"), xe);
		}
	}

	public void loadRep(Repository rep, long id_jobentry, List<DatabaseMeta> databases, List<SlaveServer> slaveServers)
	throws KettleException
  { 
	try
	{
		super.loadRep(rep, id_jobentry, databases, slaveServers);
			valuetype = getValueTypeByCode(Const.NVL(rep.getJobEntryAttributeString(id_jobentry,"valuetype"), ""));
			fieldname  = rep.getJobEntryAttributeString(id_jobentry, "fieldname");
			variablename  = rep.getJobEntryAttributeString(id_jobentry, "variablename");
			fieldtype = getFieldTypeByCode(Const.NVL(rep.getJobEntryAttributeString(id_jobentry,"fieldtype"), ""));
			mask  = rep.getJobEntryAttributeString(id_jobentry, "mask");
			comparevalue  = rep.getJobEntryAttributeString(id_jobentry, "comparevalue");
			minvalue  = rep.getJobEntryAttributeString(id_jobentry, "minvalue");
			maxvalue  = rep.getJobEntryAttributeString(id_jobentry, "maxvalue");
			successcondition = getSuccessConditionByCode(Const.NVL(rep.getJobEntryAttributeString(id_jobentry,"successcondition"), ""));
			successnumbercondition = getSuccessNumberConditionByCode(Const.NVL(rep.getJobEntryAttributeString(id_jobentry,"successnumbercondition"), ""));
		}
		catch(KettleException dbe)
		{
			throw new KettleException(Messages.getString("JobEntrySimple.Error.Exception.UnableLoadRep")+id_jobentry, dbe);
		}
	}
	
	public void saveRep(Repository rep, long id_job)
		throws KettleException
	{
		try
		{
			super.saveRep(rep, id_job);
			
			rep.saveJobEntryAttribute(id_job, getID(),"valuetype", getValueTypeCode(valuetype));
			rep.saveJobEntryAttribute(id_job, getID(), "fieldname",  fieldname);
			rep.saveJobEntryAttribute(id_job, getID(), "variablename",  variablename);
			rep.saveJobEntryAttribute(id_job, getID(),"fieldtype", getFieldTypeCode(fieldtype));
			rep.saveJobEntryAttribute(id_job, getID(), "fieldtype",  fieldtype);
			rep.saveJobEntryAttribute(id_job, getID(), "mask",  mask);
			rep.saveJobEntryAttribute(id_job, getID(), "comparevalue",  comparevalue);
			rep.saveJobEntryAttribute(id_job, getID(), "minvalue",  minvalue);
			rep.saveJobEntryAttribute(id_job, getID(), "maxvalue",  maxvalue);
			rep.saveJobEntryAttribute(id_job, getID(),"successcondition", getSuccessConditionCode(successcondition));
			rep.saveJobEntryAttribute(id_job, getID(),"successnumbercondition", getSuccessNumberConditionCode(successnumbercondition));
		}
		catch(KettleDatabaseException dbe)
		{
			
			throw new KettleException(Messages.getString("JobEntrySimple.Error.Exception.UnableSaveRep")+id_job, dbe);
		}
	}

	public Result execute(Result previousResult, int nr, Repository rep, Job parentJob) throws KettleException 
	{
		LogWriter log = LogWriter.getInstance();
		Result result = previousResult;
		
		result.setNrErrors(1);
		result.setResult(false);

		String sourcevalue=null;
		switch (valuetype)
		{
			case VALUE_TYPE_FIELD: 
				List<RowMetaAndData> rows = result.getRows();
				RowMetaAndData resultRow = null; 
				if(log.isDetailed())	
					log.logDetailed(toString(), Messages.getString("JobEntrySimpleEval.Log.ArgFromPrevious.Found",(rows!=null?rows.size():0)+ ""));
			
				if(rows.size()==0) 
				{
					rows=null;
					log.logError(toString(),Messages.getString("JobEntrySimpleEval.Error.NoRows"));
					return result;
				}
				// get first row
				resultRow = rows.get(0);
				String realfieldname=environmentSubstitute(fieldname);
				int indexOfField=-1;
				indexOfField=resultRow.getRowMeta().indexOfValue(realfieldname);
				if(indexOfField==-1)
				{
					log.logError(toString(),Messages.getString("JobEntrySimpleEval.Error.FieldNotExist",realfieldname));
					resultRow=null;
					rows=null;
					return result;
				}
				sourcevalue=resultRow.getString(indexOfField,null);
				resultRow=null;
				rows=null;
			break;
			case VALUE_TYPE_VARIABLE: 
				if(Const.isEmpty(variablename))
				{
					log.logError(toString(),Messages.getString("JobEntrySimpleEval.Error.VariableMissing"));
					return result;
				}
				sourcevalue=environmentSubstitute(variablename);
			break;
			default:
			break;
		}

		if(log.isDetailed()) log.logDetailed(toString(), Messages.getString("JobSimpleEval.Log.ValueToevaluate",sourcevalue));
		
		boolean success=false;
		String realCompareValue=environmentSubstitute(comparevalue);
		String realMinValue=environmentSubstitute(minvalue);
		String realMaxValue=environmentSubstitute(maxvalue);
		
		switch (fieldtype)
		{
			case FIELD_TYPE_STRING: 
				switch (successcondition)
				{
					case SUCCESS_CONDITION_EQUAL: // equal
						if(log.isDebug()) log.logDebug(toString(), Messages.getString("JobSimpleEval.Log.CompareWithValue",sourcevalue,realCompareValue));
						success=(sourcevalue.equals(realCompareValue));
					break;
					case SUCCESS_CONDITION_DIFFERENT: // different
						if(log.isDebug()) log.logDebug(toString(), Messages.getString("JobSimpleEval.Log.CompareWithValue",sourcevalue,realCompareValue));
						success=(!sourcevalue.equals(realCompareValue));
					break;
					case SUCCESS_CONDITION_CONTAINS: // contains
						if(log.isDebug()) log.logDebug(toString(), Messages.getString("JobSimpleEval.Log.CompareWithValue",sourcevalue,realCompareValue));
						success=(sourcevalue.contains(realCompareValue));
					break;
					case SUCCESS_CONDITION_NOT_CONTAINS: // not contains
						if(log.isDebug()) log.logDebug(toString(), Messages.getString("JobSimpleEval.Log.CompareWithValue",sourcevalue,realCompareValue));
						success=(!sourcevalue.contains(realCompareValue));
					break;
					case SUCCESS_CONDITION_START_WITH: // starts with
						if(log.isDebug()) log.logDebug(toString(), Messages.getString("JobSimpleEval.Log.CompareWithValue",sourcevalue,realCompareValue));
						success=(sourcevalue.startsWith(realCompareValue));
					break;
					case SUCCESS_CONDITION_NOT_START_WITH: // not start with
						if(log.isDebug()) log.logDebug(toString(), Messages.getString("JobSimpleEval.Log.CompareWithValue",sourcevalue,realCompareValue));
						success=(!sourcevalue.startsWith(realCompareValue));
					break;
					case SUCCESS_CONDITION_END_WITH: // ends with
						if(log.isDebug()) log.logDebug(toString(), Messages.getString("JobSimpleEval.Log.CompareWithValue",sourcevalue,realCompareValue));
						success=(sourcevalue.endsWith(realCompareValue));
					break;
					case SUCCESS_CONDITION_NOT_END_WITH: // not ends with
						if(log.isDebug()) log.logDebug(toString(), Messages.getString("JobSimpleEval.Log.CompareWithValue",sourcevalue,realCompareValue));
						success=(!sourcevalue.endsWith(realCompareValue));
					break;
					case SUCCESS_CONDITION_REGEX: // regexp
						if(log.isDebug()) log.logDebug(toString(), Messages.getString("JobSimpleEval.Log.CompareWithValue",sourcevalue,realCompareValue));
						success=(Pattern.compile(realCompareValue).matcher(sourcevalue).matches());
					break;
					default:
					break;
				}
			break;
			case FIELD_TYPE_NUMBER: 
				double valuenumber;
				try{valuenumber=Double.parseDouble(sourcevalue);
				}catch(Exception e)	{log.logError(toString(),Messages.getString("JobEntrySimpleEval.Error.UnparsableNumber",sourcevalue,e.getMessage()));return result;}
				
				double valuecompare;
				switch (successnumbercondition)
				{
					case 0: // equal
						if(log.isDebug()) log.logDebug(toString(), Messages.getString("JobSimpleEval.Log.CompareWithValue",sourcevalue,realCompareValue));					
						try{valuecompare=Double.parseDouble(realCompareValue);
						}catch(Exception e)	{log.logError(toString(),Messages.getString("JobEntrySimpleEval.Error.UnparsableNumber",realCompareValue,e.getMessage()));return result;}
						success=(valuenumber==valuecompare);
					break;
					case 1: // different
						if(log.isDebug()) log.logDebug(toString(), Messages.getString("JobSimpleEval.Log.CompareWithValue",sourcevalue,realCompareValue));
						try{valuecompare=Double.parseDouble(realCompareValue);
						}catch(Exception e)	{log.logError(toString(),Messages.getString("JobEntrySimpleEval.Error.UnparsableNumber",realCompareValue,e.getMessage()));return result;}
						success=(valuenumber!=valuecompare);
					break;
					case 2: // smaller
						if(log.isDebug()) log.logDebug(toString(), Messages.getString("JobSimpleEval.Log.CompareWithValue",sourcevalue,realCompareValue));
						try{valuecompare=Double.parseDouble(realCompareValue);
						}catch(Exception e)	{log.logError(toString(),Messages.getString("JobEntrySimpleEval.Error.UnparsableNumber",realCompareValue,e.getMessage()));return result;}
						success=(valuenumber<valuecompare);
					break;
					case 3: // smaller or equal
						if(log.isDebug()) log.logDebug(toString(), Messages.getString("JobSimpleEval.Log.CompareWithValue",sourcevalue,realCompareValue));
						try{valuecompare=Double.parseDouble(realCompareValue);
						}catch(Exception e)	{log.logError(toString(),Messages.getString("JobEntrySimpleEval.Error.UnparsableNumber",realCompareValue,e.getMessage()));return result;}
						success=(valuenumber<=valuecompare);
					break;
					case 4: // greater
						try{valuecompare=Double.parseDouble(realCompareValue);
						}catch(Exception e)	{log.logError(toString(),Messages.getString("JobEntrySimpleEval.Error.UnparsableNumber",realCompareValue,e.getMessage()));return result;}
						success=(valuenumber>valuecompare);
					break;
					case 5: // greater or equal
						if(log.isDebug()) log.logDebug(toString(), Messages.getString("JobSimpleEval.Log.CompareWithValue",sourcevalue,realCompareValue));
						try{valuecompare=Double.parseDouble(realCompareValue);
						}catch(Exception e)	{log.logError(toString(),Messages.getString("JobEntrySimpleEval.Error.UnparsableNumber",realCompareValue,e.getMessage()));return result;}
						success=(valuenumber>=valuecompare);
					break;
					case 6: // between min and max
						if(log.isDebug()) log.logDebug(toString(), Messages.getString("JobSimpleEval.Log.CompareWithValues",realMinValue,realMaxValue));
						double valuemin;
						try{valuemin=Double.parseDouble(realMinValue);
						}catch(Exception e)	{log.logError(toString(),Messages.getString("JobEntrySimpleEval.Error.UnparsableNumber",realMinValue,e.getMessage()));return result;}
						double valuemax;
						try{valuemax=Double.parseDouble(realMaxValue);
						}catch(Exception e)	{log.logError(toString(),Messages.getString("JobEntrySimpleEval.Error.UnparsableNumber",realMaxValue,e.getMessage()));return result;}
						
						if(valuemin>=valuemax)
						{
							log.logError(toString(),Messages.getString("JobEntrySimpleEval.Error.IncorrectNumbers",realMinValue,realMaxValue));
							return result;
						}
						success=(valuenumber>=valuemin && valuenumber<=valuemax);
					break;
					default:
					break;
				}
				
			break;
			case FIELD_TYPE_DATE_TIME: 
				String realMask=environmentSubstitute(mask);
				SimpleDateFormat df  = new SimpleDateFormat();
				if (!Const.isEmpty(realMask)) df.applyPattern(realMask);
				
				Date datevalue=null;
				try{datevalue=convertToDate(sourcevalue, realMask, df);
				}catch(Exception e)	{log.logError(toString(),e.getMessage());return result;}
				
				Date datecompare;
				switch (successnumbercondition)
				{
					case 0: // equal
						if(log.isDebug()) log.logDebug(toString(), Messages.getString("JobSimpleEval.Log.CompareWithValue",sourcevalue,realCompareValue));
						try{datecompare=convertToDate(realCompareValue, realMask, df);
						}catch(Exception e)	{log.logError(toString(),e.getMessage());return result;}
						success=(datevalue.equals(datecompare));
					break;
					case 1: // different
						if(log.isDebug()) log.logDebug(toString(), Messages.getString("JobSimpleEval.Log.CompareWithValue",sourcevalue,realCompareValue));
						try{datecompare=convertToDate(realCompareValue, realMask, df);
						}catch(Exception e)	{log.logError(toString(),e.getMessage());return result;}
						success=(!datevalue.equals(datecompare));
					break;
					case 2: // smaller
						if(log.isDebug()) log.logDebug(toString(), Messages.getString("JobSimpleEval.Log.CompareWithValue",sourcevalue,realCompareValue));
						try{datecompare=convertToDate(realCompareValue, realMask, df);
						}catch(Exception e)	{log.logError(toString(),e.getMessage());return result;}
						success=(datevalue.before(datecompare));
					break;
					case 3: // smaller or equal
						if(log.isDebug()) log.logDebug(toString(), Messages.getString("JobSimpleEval.Log.CompareWithValue",sourcevalue,realCompareValue));
						try{datecompare=convertToDate(realCompareValue, realMask, df);
						}catch(Exception e)	{log.logError(toString(),e.getMessage());return result;}
						success=(datevalue.before(datecompare) || datevalue.equals(datecompare));
					break;
					case 4: // greater
						if(log.isDebug()) log.logDebug(toString(), Messages.getString("JobSimpleEval.Log.CompareWithValue",sourcevalue,realCompareValue));
						try{datecompare=convertToDate(realCompareValue, realMask, df);
						}catch(Exception e)	{log.logError(toString(),e.getMessage());return result;}
						success=(datevalue.after(datecompare));
					break;
					case 5: // greater or equal
						if(log.isDebug()) log.logDebug(toString(), Messages.getString("JobSimpleEval.Log.CompareWithValue",sourcevalue,realCompareValue));
						try{datecompare=convertToDate
							(realCompareValue, realMask, df);
						}catch(Exception e)	{log.logError(toString(),e.getMessage());return result;}
						success=(datevalue.after(datecompare)  || datevalue.equals(datecompare)) ;
					break;
					case 6: // between min and max
						if(log.isDebug()) log.logDebug(toString(), Messages.getString("JobSimpleEval.Log.CompareWithValues",realMinValue,realMaxValue));
						Date datemin;
						try{datemin=convertToDate(realMinValue, realMask, df);
						}catch(Exception e)	{log.logError(toString(),e.getMessage());return result;}
						
						Date datemax;
						try{datemax=convertToDate(realMaxValue, realMask, df);
						}catch(Exception e)	{log.logError(toString(),e.getMessage());return result;}
						
						if(datemin.after(datemax) || datemin.equals(datemax))
						{
							log.logError(toString(),Messages.getString("JobEntrySimpleEval.Error.IncorrectDates",realMinValue,realMaxValue));
							return result;
						}
						
						success=((datevalue.after(datemin)|| datevalue.equals(datemin))
							&&  (datevalue.before(datemax)|| datevalue.equals(datemax)));
					break;
					default:
					break;
				}
				df=null;
			default:
			break;
		}
		
		if(success)
		{
			result.setResult(true);
			result.setNrErrors(0);
		}
		return result;
	}
	private Date convertToDate(String valueString,String mask,SimpleDateFormat df) throws KettleException
	{
		Date datevalue=null;
		try{
			datevalue=df.parse(valueString);
		}catch(Exception e)
		{
			throw new KettleException(Messages.getString("JobEntrySimpleEval.Error.UnparsableDate",valueString));
		}
		return datevalue;
	}

	public static String getValueTypeDesc(int i) {
		if (i < 0 || i >= valueTypeDesc.length)
			return valueTypeDesc[0];
		return valueTypeDesc[i];
	}
	public static String getFieldTypeDesc(int i) {
		if (i < 0 || i >= fieldTypeDesc.length)
			return fieldTypeDesc[0];
		return fieldTypeDesc[i];
	}
	public static String getSuccessConditionDesc(int i) {
		if (i < 0 || i >= successConditionDesc.length)
			return successConditionDesc[0];
		return successConditionDesc[i];
	}
	public static String getSuccessNumberConditionDesc(int i) {
		if (i < 0 || i >= successNumberConditionDesc.length)
			return successNumberConditionDesc[0];
		return successNumberConditionDesc[i];
	}
	public static int getValueTypeByDesc(String tt) {
		if (tt == null)
			return 0;

		for (int i = 0; i < valueTypeDesc.length; i++) {
			if (valueTypeDesc[i].equalsIgnoreCase(tt))
				return i;
		}

		// If this fails, try to match using the code.
		return getValueTypeByCode(tt);
	}
	public static int getFieldTypeByDesc(String tt) {
		if (tt == null)
			return 0;

		for (int i = 0; i < fieldTypeDesc.length; i++) {
			if (fieldTypeDesc[i].equalsIgnoreCase(tt))
				return i;
		}

		// If this fails, try to match using the code.
		return getFieldTypeByCode(tt);
	}
	public static int getSuccessConditionByDesc(String tt) {
		if (tt == null)
			return 0;

		for (int i = 0; i < successConditionDesc.length; i++) {
			if (successConditionDesc[i].equalsIgnoreCase(tt))
				return i;
		}

		// If this fails, try to match using the code.
		return getSuccessConditionByCode(tt);
	}
	public static int getSuccessNumberConditionByDesc(String tt) {
		if (tt == null)
			return 0;

		for (int i = 0; i < successNumberConditionDesc.length; i++) {
			if (successNumberConditionDesc[i].equalsIgnoreCase(tt))
				return i;
		}

		// If this fails, try to match using the code.
		return getSuccessNumberByCode(tt);
	}
	
	public void setMinValue(String minvalue)
	{
		this.minvalue=minvalue;
	}
	
	public String getMinValue()
	{
		return minvalue;
	}
	public void setCompareValue(String comparevalue)
	{
		this.comparevalue=comparevalue;
	}
	public String getMask()
	{
		return mask;
	}
	public void setMask(String mask)
	{
		this.mask=mask;
	}

	public String getFieldName()
	{
		return fieldname;
	}
	public void setFieldName(String fieldname)
	{
		this.fieldname=fieldname;
	}
	
	public String getVariableName()
	{
		return variablename;
	}
	public void setVariableName(String variablename)
	{
		this.variablename=variablename;
	}
	
	public String getCompareValue()
	{
		return comparevalue;
	}
	
	
	public void setMaxValue(String maxvalue)
	{
		this.maxvalue=maxvalue;
	}
	
	public String getMaxValue()
	{
		return maxvalue;
	}
	
   public boolean evaluates() {
		return true;
   }

}