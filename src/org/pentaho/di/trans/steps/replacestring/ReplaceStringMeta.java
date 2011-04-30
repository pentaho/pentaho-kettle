/*
 * Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Samatar HASSAN.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
*/
package org.pentaho.di.trans.steps.replacestring;

import java.util.List;
import java.util.Map;

import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Counter;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.shared.SharedObjectInterface;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.w3c.dom.Node;


public class ReplaceStringMeta extends BaseStepMeta implements StepMetaInterface {

	private static Class<?> PKG = ReplaceStringMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private String fieldInStream[];
	
	private String fieldOutStream[];

	private int[] useRegEx;
	
	private String replaceString[];
	
	private String replaceByString[];
	
	private String replaceFieldByString[];
	
	private int[] wholeWord;
	
	private int[] caseSensitive;


	public final static String caseSensitiveCode[] = { "no", "yes"};
	
	public static final String[] caseSensitiveDesc = new String[] { 
			BaseMessages.getString(PKG, "System.Combo.No"), 
			BaseMessages.getString(PKG, "System.Combo.Yes") };
	
	public final static int CASE_SENSITIVE_NO = 0;

	public final static int CASE_SENSITIVE_YES = 1;
	
	public static final String[] wholeWordDesc = new String[] { 
		BaseMessages.getString(PKG, "System.Combo.No"), 
		BaseMessages.getString(PKG, "System.Combo.Yes") };

	public final static String wholeWordCode[] = { "no", "yes"};

	public final static int WHOLE_WORD_NO = 0;

	public final static int WHOLE_WORD_YES = 1;

	
	public static final String[] useRegExDesc = new String[] { 
		BaseMessages.getString(PKG, "System.Combo.No"), 
		BaseMessages.getString(PKG, "System.Combo.Yes") };

	public final static String useRegExCode[] = { "no", "yes"};

	public final static int USE_REGEX_NO = 0;

	public final static int USE_REGEX_YES = 1;
	

	public ReplaceStringMeta() {
		super(); // allocate BaseStepMeta
	}

	/**
	 * @return Returns the fieldInStream.
	 */
	public String[] getFieldInStream() {
		return fieldInStream;
	}

	/**
	 * @param fieldInStream
	 *            The fieldInStream to set.
	 */
	public void setFieldInStream(String[] keyStream) {
		this.fieldInStream = keyStream;
	}
	public int[] getCaseSensitive() {
		return caseSensitive;
	}
	public int[] getWholeWord() {
		return wholeWord;
	}
	
	public int[] getUseRegEx() {
		return useRegEx;
	}
	

	/**
	 * @return Returns the fieldOutStream.
	 */
	public String[] getFieldOutStream() {
		return fieldOutStream;
	}
	/**
	 * @param keyStream  The fieldOutStream to set.
	 */
	public void setFieldOutStream(String[] keyStream) {
		this.fieldOutStream = keyStream;
	}
	
	public String[] getReplaceString() {
		return replaceString;
	}
	
	public void setReplaceString(String[] replaceString) {
		this.replaceString = replaceString;
	}
	public String[] getReplaceByString() {
		return replaceByString;
	}
	
	public String[] getFieldReplaceByString() {
		return replaceFieldByString;
	}

	
	public void setCaseSensitive(int[] caseSensitive) {
		this.caseSensitive = caseSensitive;
	}
	
    public void loadXML(Node stepnode, List<DatabaseMeta> databases, Map<String, Counter> counters)
    throws KettleXMLException
    {
    	readData(stepnode, databases);
	}
	public void allocate(int nrkeys) {
		fieldInStream = new String[nrkeys];
		fieldOutStream = new String[nrkeys];
		useRegEx= new int[nrkeys];
		replaceString = new String[nrkeys];
		replaceByString = new String[nrkeys];
		replaceFieldByString = new String[nrkeys];
		wholeWord= new int[nrkeys];
		caseSensitive= new int[nrkeys];
	}

	public Object clone() {
		ReplaceStringMeta retval = (ReplaceStringMeta) super.clone();
		int nrkeys = fieldInStream.length;

		retval.allocate(nrkeys);

		for (int i = 0; i < nrkeys; i++) {
			retval.fieldInStream[i] = fieldInStream[i];
			retval.fieldOutStream[i] = fieldOutStream[i];
			retval.useRegEx[i] = useRegEx[i];
			retval.replaceString[i] = replaceString[i];
			retval.replaceByString[i] = replaceByString[i];
			retval.replaceFieldByString[i] = replaceFieldByString[i];
			retval.wholeWord[i] = wholeWord[i];
			retval.caseSensitive[i] = caseSensitive[i];
		}

		return retval;
	}

	 private void readData(Node stepnode, List<? extends SharedObjectInterface> databases) throws KettleXMLException
	 {
		try
		{
			int nrkeys;

			Node lookup = XMLHandler.getSubNode(stepnode, "fields"); //$NON-NLS-1$
			nrkeys = XMLHandler.countNodes(lookup, "field"); //$NON-NLS-1$

			allocate(nrkeys);

			for (int i = 0; i < nrkeys; i++) {
				Node fnode = XMLHandler.getSubNodeByNr(lookup, "field", i); //$NON-NLS-1$

				fieldInStream[i] = Const.NVL(XMLHandler.getTagValue(fnode,"in_stream_name"), ""); //$NON-NLS-1$
				fieldOutStream[i] = Const.NVL(XMLHandler.getTagValue(fnode,"out_stream_name"), ""); //$NON-NLS-1$
				useRegEx[i] = getCaseSensitiveByCode(Const.NVL(XMLHandler.getTagValue(fnode,"use_regex"), ""));
				replaceString[i] = Const.NVL(XMLHandler.getTagValue(fnode, "replace_string"), ""); //$NON-NLS-1$
				replaceByString[i] = Const.NVL(XMLHandler.getTagValue(fnode, "replace_by_string"), ""); //$NON-NLS-1$
				replaceFieldByString[i] = Const.NVL(XMLHandler.getTagValue(fnode, "replace_field_by_string"), ""); //$NON-NLS-1$
				wholeWord[i] = getWholeWordByCode(Const.NVL(XMLHandler.getTagValue(fnode,"whole_word"), ""));
				caseSensitive[i] = getCaseSensitiveByCode(Const.NVL(XMLHandler.getTagValue(fnode,"case_sensitive"), ""));
				
			}
		} catch (Exception e) {
			throw new KettleXMLException(
					BaseMessages.getString(PKG, "ReplaceStringMeta.Exception.UnableToReadStepInfoFromXML"), e); //$NON-NLS-1$
		}
	}

	public void setDefault() {
		fieldInStream = null;
		fieldOutStream = null;
		int nrkeys = 0;

		allocate(nrkeys);
	}

	public String getXML() {
		StringBuffer retval = new StringBuffer(500);

		retval.append("    <fields>").append(Const.CR); //$NON-NLS-1$

		for (int i = 0; i < fieldInStream.length; i++) {
			retval.append("      <field>").append(Const.CR); //$NON-NLS-1$
			retval.append("        ").append(XMLHandler.addTagValue("in_stream_name", fieldInStream[i])); //$NON-NLS-1$ //$NON-NLS-2$
			retval.append("        ").append(XMLHandler.addTagValue("out_stream_name", fieldOutStream[i])); 
			retval.append("        ").append(XMLHandler.addTagValue("use_regex",getUseRegExCode(useRegEx[i])));
			retval.append("        ").append(XMLHandler.addTagValue("replace_string", replaceString[i])); //$NON-NLS-1$ //$NON-NLS-2$
			retval.append("        ").append(XMLHandler.addTagValue("replace_by_string", replaceByString[i])); 
			retval.append("        ").append(XMLHandler.addTagValue("replace_field_by_string", replaceFieldByString[i])); 
			retval.append("        ").append(XMLHandler.addTagValue("whole_word",getWholeWordCode(wholeWord[i])));
			retval.append("        ").append(XMLHandler.addTagValue("case_sensitive",getCaseSensitiveCode(caseSensitive[i])));
			retval.append("      </field>").append(Const.CR); //$NON-NLS-1$
		}

		retval.append("    </fields>").append(Const.CR); //$NON-NLS-1$

		return retval.toString();
	}

	 public void readRep(Repository rep, ObjectId id_step, List<DatabaseMeta> databases, Map<String, Counter> counters)
     throws KettleException
     {
		try {
			int nrkeys = rep.countNrStepAttributes(id_step, "in_stream_name"); //$NON-NLS-1$

			allocate(nrkeys);
			for (int i = 0; i < nrkeys; i++) {
				fieldInStream[i] = Const.NVL(rep.getStepAttributeString(id_step, i,	"in_stream_name"), ""); //$NON-NLS-1$
				fieldOutStream[i] = Const.NVL(rep.getStepAttributeString(id_step, i,	"out_stream_name"), ""); //$NON-NLS-1$
				useRegEx[i] = getCaseSensitiveByCode(Const.NVL(rep.getStepAttributeString(id_step, i, "use_regex"), ""));
				replaceString[i] = Const.NVL(rep.getStepAttributeString(id_step, i,	"replace_string"), "");
				replaceByString[i] = Const.NVL(rep.getStepAttributeString(id_step, i,	"replace_by_string"), "");
				replaceFieldByString[i] = Const.NVL(rep.getStepAttributeString(id_step, i,	"replace_field_by_string"), "");
				wholeWord[i] = getWholeWordByCode(Const.NVL(rep.getStepAttributeString(id_step, i, "whole_world"), ""));
				caseSensitive[i] = getCaseSensitiveByCode(Const.NVL(rep.getStepAttributeString(id_step, i, "case_sensitive"), ""));
				
			}
		} catch (Exception e) {
			throw new KettleException(
					BaseMessages.getString(PKG, "ReplaceStringMeta.Exception.UnexpectedErrorInReadingStepInfo"), e); //$NON-NLS-1$
		}
	}

	public void saveRep(Repository rep, ObjectId id_transformation, ObjectId id_step)
			throws KettleException {
		try {
			for (int i = 0; i < fieldInStream.length; i++) {
				rep.saveStepAttribute(id_transformation, id_step, i,"in_stream_name", fieldInStream[i]); //$NON-NLS-1$
				rep.saveStepAttribute(id_transformation, id_step, i,"out_stream_name", fieldOutStream[i]);
				rep.saveStepAttribute(id_transformation, id_step, i,"use_regex", getUseRegExCode(useRegEx[i]));
				rep.saveStepAttribute(id_transformation, id_step, i,"replace_string", replaceString[i]); //$NON-NLS-1$
				rep.saveStepAttribute(id_transformation, id_step, i,"replace_by_string", replaceByString[i]); 
				rep.saveStepAttribute(id_transformation, id_step, i,"replace_field_by_string", replaceFieldByString[i]); 
				rep.saveStepAttribute(id_transformation, id_step, i,"whole_world", getWholeWordCode(wholeWord[i]));
				rep.saveStepAttribute(id_transformation, id_step, i,"case_sensitive", getCaseSensitiveCode(caseSensitive[i]));
				
			}
		} catch (Exception e) {
			throw new KettleException(BaseMessages.getString(PKG, "ReplaceStringMeta.Exception.UnableToSaveStepInfo") + id_step, e); //$NON-NLS-1$
		}
	}
	 public void getFields(RowMetaInterface inputRowMeta, String name, RowMetaInterface info[], StepMeta nextStep,
	            VariableSpace space) throws KettleStepException
	  {
		for(int i=0;i<fieldOutStream.length;i++) {
			ValueMetaInterface valueMeta = new ValueMeta(space.environmentSubstitute(fieldOutStream[i]), ValueMeta.TYPE_STRING);
			valueMeta.setLength(100, -1);
			valueMeta.setOrigin(name);
			
			if (!Const.isEmpty(fieldOutStream[i])){
				inputRowMeta.addValueMeta(valueMeta);
			} else {
				int index = inputRowMeta.indexOfValue(fieldInStream[i]);
				if (index>=0) {
					valueMeta.setName(fieldInStream[i]);
					inputRowMeta.setValueMeta(index, valueMeta);
				}
			}
		}
	}
	 public void check(List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepinfo,
	            RowMetaInterface prev, String input[], String output[], RowMetaInterface info)
	  {

		CheckResult cr;
		String error_message = ""; //$NON-NLS-1$
		boolean first = true;
		boolean error_found = false;

		if (prev == null) {
			error_message += BaseMessages.getString(PKG, "ReplaceStringMeta.CheckResult.NoInputReceived") + Const.CR; //$NON-NLS-1$
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR,
					error_message, stepinfo);
			remarks.add(cr);
		} else {

			for (int i = 0; i < fieldInStream.length; i++) {
				String field = fieldInStream[i];

				ValueMetaInterface v = prev.searchValueMeta(field);
				if (v == null) {
					if (first) {
						first = false;
						error_message += BaseMessages.getString(PKG, "ReplaceStringMeta.CheckResult.MissingInStreamFields") + Const.CR; //$NON-NLS-1$
					}
					error_found = true;
					error_message += "\t\t" + field + Const.CR; //$NON-NLS-1$
				}
			}
			if (error_found) {
				cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR,
						error_message, stepinfo);
			} else {
				cr = new CheckResult(
						CheckResult.TYPE_RESULT_OK,
						BaseMessages.getString(PKG, "ReplaceStringMeta.CheckResult.FoundInStreamFields"), stepinfo); //$NON-NLS-1$
			}
			remarks.add(cr);

			// Check whether all are strings
			first = true;
			error_found = false;
			for (int i = 0; i < fieldInStream.length; i++) {
				String field = fieldInStream[i];

				ValueMetaInterface v = prev.searchValueMeta(field);
				if (v != null) {
					if (v.getType() != ValueMeta.TYPE_STRING) {
						if (first) {
							first = false;
							error_message += BaseMessages.getString(PKG, "ReplaceStringMeta.CheckResult.OperationOnNonStringFields") + Const.CR; //$NON-NLS-1$
						}
						error_found = true;
						error_message += "\t\t" + field + Const.CR; //$NON-NLS-1$
					}
				}
			}
			if (error_found) {
				cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR,
						error_message, stepinfo);
			} else {
				cr = new CheckResult(
						CheckResult.TYPE_RESULT_OK,BaseMessages.getString(PKG, "ReplaceStringMeta.CheckResult.AllOperationsOnStringFields"), stepinfo); //$NON-NLS-1$
			}
			remarks.add(cr);

			if (fieldInStream.length>0) {
				for (int idx = 0; idx < fieldInStream.length; idx++) {
					if (Const.isEmpty(fieldInStream[idx])) {
						cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR,BaseMessages.getString(PKG, "ReplaceStringMeta.CheckResult.InStreamFieldMissing", new Integer(idx + 1).toString()), stepinfo); //$NON-NLS-1$
						remarks.add(cr);
					
					}
				}
			}

			// Check if all input fields are distinct.
			for (int idx = 0; idx < fieldInStream.length; idx++) {
				for (int jdx = 0; jdx < fieldInStream.length; jdx++) {
					if (fieldInStream[idx].equals(fieldInStream[jdx])
							&& idx != jdx && idx < jdx) {
						error_message = BaseMessages.getString(PKG, "ReplaceStringMeta.CheckResult.FieldInputError", fieldInStream[idx]); //$NON-NLS-1$
						cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR,	error_message, stepinfo);
						remarks.add(cr);
					}
				}
			}

		}
	}

	
	public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, 
			int cnr, TransMeta transMeta, Trans trans)
	{
		return new ReplaceString(stepMeta, stepDataInterface, cnr, transMeta, trans);
	}

	public StepDataInterface getStepData() {
		return new ReplaceStringData();
	}

	public boolean supportsErrorHandling() {
		return true;
	}

	private static String getCaseSensitiveCode(int i) {
		if (i < 0 || i >= caseSensitiveCode.length)
			return caseSensitiveCode[0];
		return caseSensitiveCode[i];
	}
	private static String getWholeWordCode(int i) {
		if (i < 0 || i >= wholeWordCode.length)
			return wholeWordCode[0];
		return wholeWordCode[i];
	}
	private static String getUseRegExCode(int i) {
		if (i < 0 || i >= useRegExCode.length)
			return useRegExCode[0];
		return useRegExCode[i];
	}
	public static String getCaseSensitiveDesc(int i) {
		if (i < 0 || i >= caseSensitiveDesc.length)
			return caseSensitiveDesc[0];
		return caseSensitiveDesc[i];
	}
	public static String getWholeWordDesc(int i) {
		if (i < 0 || i >= wholeWordDesc.length)
			return wholeWordDesc[0];
		return wholeWordDesc[i];
	}
	
	public static String getUseRegExDesc(int i) {
		if (i < 0 || i >= useRegExDesc.length)
			return useRegExDesc[0];
		return useRegExDesc[i];
	}
	
	
	private static int getCaseSensitiveByCode(String tt) {
		if (tt == null)
			return 0;

		for (int i = 0; i < caseSensitiveCode.length; i++) {
			if (caseSensitiveCode[i].equalsIgnoreCase(tt))
				return i;
		}
		return 0;
	}
	private static int getWholeWordByCode(String tt) {
		if (tt == null)
			return 0;

		for (int i = 0; i < wholeWordCode.length; i++) {
			if (wholeWordCode[i].equalsIgnoreCase(tt))
				return i;
		}
		return 0;
	}
	
	private static int getRegExByCode(String tt) {
		if (tt == null)
			return 0;

		for (int i = 0; i < useRegExCode.length; i++) {
			if (useRegExCode[i].equalsIgnoreCase(tt))
				return i;
		}
		return 0;
	}
	
	
	public static int getCaseSensitiveByDesc(String tt) {
		if (tt == null)
			return 0;

		for (int i = 0; i < caseSensitiveDesc.length; i++) {
			if (caseSensitiveDesc[i].equalsIgnoreCase(tt))
				return i;
		}

		// If this fails, try to match using the code.
		return getCaseSensitiveByCode(tt);
	}
	
	public static int getWholeWordByDesc(String tt) {
		if (tt == null)
			return 0;

		for (int i = 0; i < wholeWordDesc.length; i++) {
			if (wholeWordDesc[i].equalsIgnoreCase(tt))
				return i;
		}

		// If this fails, try to match using the code.
		return getWholeWordByCode(tt);
	}
	public static int getUseRegExByDesc(String tt) {
		if (tt == null)
			return 0;

		for (int i = 0; i < useRegExDesc.length; i++) {
			if (useRegExDesc[i].equalsIgnoreCase(tt))
				return i;
		}

		// If this fails, try to match using the code.
		return getRegExByCode(tt);
	}
	
}