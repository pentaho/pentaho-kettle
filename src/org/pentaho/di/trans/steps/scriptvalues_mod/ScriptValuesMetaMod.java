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
 /**********************************************************************
 **                                                                   **
 ** This Script has been modified for higher performance              **
 ** and more functionality in December-2006,                          **
 ** by proconis GmbH / Germany                                        **
 **                                                                   ** 
 ** http://www.proconis.de                                            **
 ** info@proconis.de                                                  **
 **                                                                   **
 **********************************************************************/

package org.pentaho.di.trans.steps.scriptvalues_mod;

import java.math.BigDecimal;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.JavaScriptException;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.pentaho.di.compatibility.Row;
import org.pentaho.di.compatibility.Value;
import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Counter;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.plugins.KettleURLClassLoader;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.w3c.dom.Document;
import org.w3c.dom.Node;




/*
 * Created on 2-jun-2003
 *
 */
public class ScriptValuesMetaMod extends BaseStepMeta implements StepMetaInterface
{	
	private static Class<?> PKG = ScriptValuesMetaMod.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private static final String JSSCRIPT_TAG_TYPE = "jsScript_type";
	private static final String JSSCRIPT_TAG_NAME = "jsScript_name";
	private static final String JSSCRIPT_TAG_SCRIPT= "jsScript_script";
	
	private ScriptValuesAddClasses[] additionalClasses;
	private ScriptValuesScript[]	jsScripts;
	
	private String  fieldname[];
	private String  rename[];
	private int     type[];
	private int     length[];
	private int     precision[];
	private boolean replace[]; // Replace the specified field.
	
	private boolean compatible;
	
	public ScriptValuesMetaMod(){
		super(); // allocate BaseStepMeta
		compatible=true;
		try{
			parseXmlForAdditionalClasses();
		}catch(Exception e){};
	}
	
    /**
     * @return Returns the length.
     */
    public int[] getLength(){
        return length;
    }
    
    /**
     * @param length The length to set.
     */
    public void setLength(int[] length){
        this.length = length;
    }
    
    /**
     * @return Returns the name.
     */
    public String[] getFieldname(){
        return fieldname;
    }
    
    /**
     * @param fieldname The name to set.
     */
    public void setFieldname(String[] fieldname){
        this.fieldname = fieldname;
    }
    
    /**
     * @return Returns the precision.
     */
    public int[] getPrecision()
    {
        return precision;
    }
    
    /**
     * @param precision The precision to set.
     */
    public void setPrecision(int[] precision)
    {
        this.precision = precision;
    }
    
    /**
     * @return Returns the rename.
     */
    public String[] getRename()
    {
        return rename;
    }
    
    /**
     * @param rename The rename to set.
     */
    public void setRename(String[] rename)
    {
        this.rename = rename;
    }
    
    /**
     * @return Returns the type.
     */
    public int[] getType()
    {
        return type;
    }
    
    /**
     * @param type The type to set.
     */
    public void setType(int[] type)
    {
        this.type = type;
    }
    
    public int getNumberOfJSScripts(){
    	return jsScripts.length;
    }
    
    
    public String[] getJSScriptNames(){
    	String strJSNames[] = new String[jsScripts.length];
    	for(int i=0;i<jsScripts.length;i++) strJSNames[i] = jsScripts[i].getScriptName();
    	return strJSNames;
    }
    
    public ScriptValuesScript[] getJSScripts(){
    	return jsScripts; 
    }
    
    public void setJSScripts(ScriptValuesScript[] jsScripts){
    	this.jsScripts = jsScripts;
    }
    
    
	public void loadXML(Node stepnode, List<DatabaseMeta> databases, Map<String, Counter> counters)
		throws KettleXMLException
	{
		readData(stepnode);
	}

	public void allocate(int nrfields){
		fieldname      = new String [nrfields];
		rename    = new String [nrfields];
		type      = new int    [nrfields];
		length    = new int    [nrfields];
		precision = new int    [nrfields];
		replace   = new boolean[nrfields];
	}

	public Object clone()
	{
		ScriptValuesMetaMod retval = (ScriptValuesMetaMod)super.clone();
		
		int nrfields = fieldname.length;
		
		retval.allocate(nrfields);
		
		for (int i=0;i<nrfields;i++)
		{
			retval.fieldname     [i] = fieldname[i];
			retval.rename   [i] = rename[i];
			retval.type     [i] = type[i];
			retval.length   [i] = length[i];
			retval.precision[i] = precision[i];
			retval.replace  [i] = replace[i];
		}

		return retval;
	}
	
	private void readData(Node stepnode) throws KettleXMLException{
		try	{
			String script = XMLHandler.getTagValue(stepnode, "script"); 
			String strCompatible = XMLHandler.getTagValue(stepnode, "compatible");
			if (strCompatible==null) {
				compatible=true;
			}
			else {
				compatible = "Y".equalsIgnoreCase(strCompatible);
			}
			
			// When in compatibility mode, we load the script, not the other tabs...
			//
			if (!Const.isEmpty(script)) {
				jsScripts = new ScriptValuesScript[1];
				jsScripts[0] = new ScriptValuesScript(
						ScriptValuesScript.TRANSFORM_SCRIPT,
						"ScriptValue", 
						script
						);
			}
			else {
				Node scripts = XMLHandler.getSubNode(stepnode, "jsScripts");
				int nrscripts = XMLHandler.countNodes(scripts, "jsScript");
				jsScripts = new ScriptValuesScript[nrscripts];
				for (int i=0;i<nrscripts;i++){
					Node fnode = XMLHandler.getSubNodeByNr(scripts, "jsScript", i); //$NON-NLS-1$
					
					jsScripts[i] = new ScriptValuesScript(
							Integer.parseInt(XMLHandler.getTagValue(fnode, JSSCRIPT_TAG_TYPE)),
							XMLHandler.getTagValue(fnode, JSSCRIPT_TAG_NAME), 
							XMLHandler.getTagValue(fnode, JSSCRIPT_TAG_SCRIPT) 
					);
				}
			}
			
			Node fields = XMLHandler.getSubNode(stepnode, "fields"); //$NON-NLS-1$
			int nrfields = XMLHandler.countNodes(fields, "field"); //$NON-NLS-1$
				
			allocate(nrfields);
			
			for (int i=0;i<nrfields;i++)
			{
				Node fnode = XMLHandler.getSubNodeByNr(fields, "field", i); //$NON-NLS-1$
				
				fieldname     [i] = XMLHandler.getTagValue(fnode, "name"); //$NON-NLS-1$
				rename   [i] = XMLHandler.getTagValue(fnode, "rename"); //$NON-NLS-1$
				type     [i] = ValueMeta.getType(XMLHandler.getTagValue(fnode, "type")); //$NON-NLS-1$
	
				String slen = XMLHandler.getTagValue(fnode, "length"); //$NON-NLS-1$
				String sprc = XMLHandler.getTagValue(fnode, "precision"); //$NON-NLS-1$
				length   [i]=Const.toInt(slen, -1);
				precision[i]=Const.toInt(sprc, -1);
				replace  [i] = "Y".equalsIgnoreCase(XMLHandler.getTagValue(fnode, "replace")); //$NON-NLS-1$
			}
		}
		catch(Exception e)
		{
			throw new KettleXMLException(BaseMessages.getString(PKG, "ScriptValuesMetaMod.Exception.UnableToLoadStepInfoFromXML"), e); //$NON-NLS-1$
		}
	}

	public void setDefault(){
		jsScripts = new ScriptValuesScript[1];
		jsScripts[0] = new ScriptValuesScript(
				ScriptValuesScript.TRANSFORM_SCRIPT,
				BaseMessages.getString(PKG, "ScriptValuesMod.Script1"),
				"//"+ BaseMessages.getString(PKG, "ScriptValuesMod.ScriptHere") +Const.CR+Const.CR
		);
		
		int nrfields=0;
		allocate(nrfields);

		for (int i=0;i<nrfields;i++){
			fieldname     [i] = "newvalue"; //$NON-NLS-1$
			rename   [i] = "newvalue"; //$NON-NLS-1$
			type     [i] = ValueMetaInterface.TYPE_NUMBER;
			length   [i] = -1;
			precision[i] = -1;
			replace  [i] = false;
		}
		
		compatible=false;
	}
	
	public void getFields(RowMetaInterface row, String originStepname, RowMetaInterface[] info, StepMeta nextStep, VariableSpace space) throws KettleStepException
	{
		for (int i=0;i<fieldname.length;i++)
		{
			if (!Const.isEmpty(fieldname[i]))
			{
				ValueMetaInterface v;
				if (replace[i]) {
					// Look up the field to replace...
					v = row.searchValueMeta(fieldname[i]);
					if (v==null && Const.isEmpty(rename[i])) {
						throw new KettleStepException(BaseMessages.getString(PKG, "ScriptValuesMetaMod.Exception.FieldToReplaceNotFound", fieldname[i]));
					}
					v= row.searchValueMeta(rename[i]);
					
					// Change the data type to match what's specified...
					//
					v.setType(type[i]);
				} else {
					if (rename[i]!=null && rename[i].length()!=0) 
					{
						v = new ValueMeta(rename[i], type[i]);
					} 
					else
					{
						v = new ValueMeta(this.fieldname[i], type[i]); 
					} 
				}
				v.setLength(length[i]);
                v.setPrecision(precision[i]);
				v.setOrigin(originStepname);
				if (!replace[i]) {
					row.addValueMeta( v );
				}
			}
		}
	}

	public String getXML()
    {
        StringBuffer retval = new StringBuffer(300);
		
		retval.append("    ").append(XMLHandler.addTagValue("compatible", compatible)); //$NON-NLS-1$ //$NON-NLS-2$

		retval.append("    <jsScripts>"); 
		for (int i=0;i<jsScripts.length;i++){
			retval.append("      <jsScript>"); //$NON-NLS-1$
			retval.append("        ").append(XMLHandler.addTagValue(JSSCRIPT_TAG_TYPE,  jsScripts[i].getScriptType()));
			retval.append("        ").append(XMLHandler.addTagValue(JSSCRIPT_TAG_NAME,  jsScripts[i].getScriptName()));
			retval.append("        ").append(XMLHandler.addTagValue(JSSCRIPT_TAG_SCRIPT, jsScripts[i].getScript()));
			retval.append("      </jsScript>"); //$NON-NLS-1$
		}
		retval.append("    </jsScripts>"); 
		
		retval.append("    <fields>"); //$NON-NLS-1$
		for (int i=0;i<fieldname.length;i++)
		{
			retval.append("      <field>"); //$NON-NLS-1$
			retval.append("        ").append(XMLHandler.addTagValue("name",      fieldname[i])); //$NON-NLS-1$ //$NON-NLS-2$
			retval.append("        ").append(XMLHandler.addTagValue("rename",    rename[i])); //$NON-NLS-1$ //$NON-NLS-2$
			retval.append("        ").append(XMLHandler.addTagValue("type",      ValueMeta.getTypeDesc(type[i]))); //$NON-NLS-1$ //$NON-NLS-2$
			retval.append("        ").append(XMLHandler.addTagValue("length",    length[i])); //$NON-NLS-1$ //$NON-NLS-2$
			retval.append("        ").append(XMLHandler.addTagValue("precision", precision[i])); //$NON-NLS-1$ //$NON-NLS-2$
			retval.append("        ").append(XMLHandler.addTagValue("replace",   replace[i])); //$NON-NLS-1$ //$NON-NLS-2$
			retval.append("      </field>"); //$NON-NLS-1$
		}
		retval.append("    </fields>"); //$NON-NLS-1$
		
		return retval.toString();
	}

	public void readRep(Repository rep, ObjectId id_step, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleException
    {
		try
		{
			String script = rep.getStepAttributeString(id_step, "script"); //$NON-NLS-1$
			compatible = rep.getStepAttributeBoolean(id_step, 0, "compatible", true); //$NON-NLS-1$

			// When in compatibility mode, we load the script, not the other tabs...
			//
			if (!Const.isEmpty(script)) {
				jsScripts = new ScriptValuesScript[1];
				jsScripts[0] = new ScriptValuesScript(
						ScriptValuesScript.TRANSFORM_SCRIPT,
						"ScriptValue", 
						script
						);
			}
			else {
	            int nrScripts = rep.countNrStepAttributes(id_step, JSSCRIPT_TAG_NAME); //$NON-NLS-1$
	            jsScripts = new ScriptValuesScript[nrScripts];
	            for (int i = 0; i < nrScripts; i++)
	            {
	                jsScripts[i] = new ScriptValuesScript((int) rep.getStepAttributeInteger(id_step, i, JSSCRIPT_TAG_TYPE), rep.getStepAttributeString(
	                        id_step, i, JSSCRIPT_TAG_NAME), rep.getStepAttributeString(id_step, i, JSSCRIPT_TAG_SCRIPT));
	
	            }
			}
			
			int nrfields = rep.countNrStepAttributes(id_step, "field_name"); //$NON-NLS-1$
			allocate(nrfields);
	
			for (int i=0;i<nrfields;i++)
			{
				fieldname[i]        =       rep.getStepAttributeString (id_step, i, "field_name"); //$NON-NLS-1$
				rename[i]      =       rep.getStepAttributeString (id_step, i, "field_rename"); //$NON-NLS-1$
				type[i]        =  ValueMeta.getType( rep.getStepAttributeString (id_step, i, "field_type") ); //$NON-NLS-1$
				length[i]      =  (int)rep.getStepAttributeInteger(id_step, i, "field_length"); //$NON-NLS-1$
				precision[i]   =  (int)rep.getStepAttributeInteger(id_step, i, "field_precision"); //$NON-NLS-1$
				replace[i]     =       rep.getStepAttributeBoolean(id_step, i, "field_replace"); //$NON-NLS-1$
			}
		}
		catch(Exception e)
		{
			throw new KettleException(BaseMessages.getString(PKG, "ScriptValuesMetaMod.Exception.UnexpectedErrorInReadingStepInfo"), e); //$NON-NLS-1$
		}
	}

	public void saveRep(Repository rep, ObjectId id_transformation, ObjectId id_step) throws KettleException
    {
        try
        {
            rep.saveStepAttribute(id_transformation, id_step, 0, "compatible", compatible); //$NON-NLS-1$

            for (int i = 0; i < jsScripts.length; i++)
            {
                rep.saveStepAttribute(id_transformation, id_step, i, JSSCRIPT_TAG_NAME, jsScripts[i].getScriptName());
                rep.saveStepAttribute(id_transformation, id_step, i, JSSCRIPT_TAG_SCRIPT, jsScripts[i].getScript());
                rep.saveStepAttribute(id_transformation, id_step, i, JSSCRIPT_TAG_TYPE, jsScripts[i].getScriptType());
            }

            for (int i = 0; i < fieldname.length; i++)
            {
                rep.saveStepAttribute(id_transformation, id_step, i, "field_name", fieldname[i]); //$NON-NLS-1$
                rep.saveStepAttribute(id_transformation, id_step, i, "field_rename", rename[i]); //$NON-NLS-1$
                rep.saveStepAttribute(id_transformation, id_step, i, "field_type", ValueMeta.getTypeDesc(type[i])); //$NON-NLS-1$
                rep.saveStepAttribute(id_transformation, id_step, i, "field_length", length[i]); //$NON-NLS-1$
                rep.saveStepAttribute(id_transformation, id_step, i, "field_precision", precision[i]); //$NON-NLS-1$
                rep.saveStepAttribute(id_transformation, id_step, i, "field_replace", replace[i]); //$NON-NLS-1$
            }
        }
        catch (Exception e)
        {
            throw new KettleException(BaseMessages.getString(PKG, "ScriptValuesMetaMod.Exception.UnableToSaveStepInfo") + id_step, e); //$NON-NLS-1$
        }
    }


	public void check(List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepinfo, RowMetaInterface prev, String input[], String output[], RowMetaInterface info)
	{
		boolean error_found=false;
		String error_message = ""; //$NON-NLS-1$
		CheckResult cr;
		
		Context jscx;
		Scriptable jsscope;
		Script jsscript;

		jscx = ContextFactory.getGlobal().enterContext();
		jsscope = jscx.initStandardObjects(null, false);
		jscx.setOptimizationLevel(-1);
			
		// String strActiveScriptName="";
		String strActiveStartScriptName="";
		String strActiveEndScriptName="";
		
		String strActiveScript="";
		String strActiveStartScript="";
		String strActiveEndScript="";

		
		// Building the Scripts
		if(jsScripts.length>0){
			for(int i=0;i<jsScripts.length;i++){
				if(jsScripts[i].isTransformScript()){
					// strActiveScriptName =jsScripts[i].getScriptName();
					strActiveScript =jsScripts[i].getScript();
				}else if(jsScripts[i].isStartScript()) {
					strActiveStartScriptName =jsScripts[i].getScriptName(); 
					strActiveStartScript =jsScripts[i].getScript();
				}else if(jsScripts[i].isEndScript()){
					strActiveEndScriptName =jsScripts[i].getScriptName();
					strActiveEndScript =jsScripts[i].getScript();
				}
			}
		}
		
		if (prev!=null && strActiveScript.length()>0)		{
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(PKG, "ScriptValuesMetaMod.CheckResult.ConnectedStepOK",String.valueOf(prev.size())), stepinfo); //$NON-NLS-1$ //$NON-NLS-2$
			remarks.add(cr);
			
			// Adding the existing Scripts to the Context
			for(int i=0;i<getNumberOfJSScripts();i++){
				Scriptable jsR = Context.toObject(jsScripts[i].getScript(), jsscope);
				jsscope.put(jsScripts[i].getScriptName(), jsscope, jsR);
			}
 
			// Modification for Additional Script parsing
			try{
				if (getAddClasses()!=null)
                {
    				for(int i=0;i<getAddClasses().length;i++){
    					Object jsOut = Context.javaToJS(getAddClasses()[i].getAddObject(), jsscope);
    					ScriptableObject.putProperty(jsscope, getAddClasses()[i].getJSName(), jsOut);
    				}
                }
			}catch(Exception e){
				error_message = ("Couldn't add JavaClasses to Context! Error:");
				cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepinfo);
				remarks.add(cr);
			}
			
			// Adding some default JavaScriptFunctions to the System
			try {
				Context.javaToJS(ScriptValuesAddedFunctions.class, jsscope);
				((ScriptableObject)jsscope).defineFunctionProperties(ScriptValuesAddedFunctions.jsFunctionList, ScriptValuesAddedFunctions.class, ScriptableObject.DONTENUM);
			} catch (Exception ex) {
				error_message="Couldn't add Default Functions! Error:"+Const.CR+ex.toString();
				cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepinfo);
				remarks.add(cr);
			};

			// Adding some Constants to the JavaScript
			try {
				jsscope.put("SKIP_TRANSFORMATION", jsscope, Integer.valueOf(ScriptValuesMod.SKIP_TRANSFORMATION));
				jsscope.put("ABORT_TRANSFORMATION", jsscope, Integer.valueOf(ScriptValuesMod.ABORT_TRANSFORMATION));
				jsscope.put("ERROR_TRANSFORMATION", jsscope, Integer.valueOf(ScriptValuesMod.ERROR_TRANSFORMATION));
				jsscope.put("CONTINUE_TRANSFORMATION", jsscope, Integer.valueOf(ScriptValuesMod.CONTINUE_TRANSFORMATION));
			} catch (Exception ex) {
				error_message="Couldn't add Transformation Constants! Error:"+Const.CR+ex.toString(); //$NON-NLS-1$
				cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepinfo);
				remarks.add(cr);
			};

			try{
				ScriptValuesModDummy dummyStep = new ScriptValuesModDummy(prev, transMeta.getStepFields(stepinfo));
				Scriptable jsvalue = Context.toObject(dummyStep, jsscope);
				jsscope.put("_step_", jsscope, jsvalue); //$NON-NLS-1$

				Object[] row=new Object[prev.size()];
   			    Scriptable jsRowMeta = Context.toObject(prev, jsscope);
			    jsscope.put("rowMeta", jsscope, jsRowMeta); //$NON-NLS-1$
			    for (int i=0;i<prev.size();i++)
			    {
                    ValueMetaInterface valueMeta = prev.getValueMeta(i);
  				    Object valueData = null;
                    
				    // Set date and string values to something to simulate real thing
                    //
				    if (valueMeta.isDate()) valueData = new Date();
				    if (valueMeta.isString()) valueData = "test value test value test value test value test value test value test value test value test value test value"; //$NON-NLS-1$
                    if (valueMeta.isInteger()) valueData = Long.valueOf(0L);
                    if (valueMeta.isNumber()) valueData = new Double(0.0);
                    if (valueMeta.isBigNumber()) valueData = BigDecimal.ZERO;
                    if (valueMeta.isBoolean()) valueData = Boolean.TRUE;
                    if (valueMeta.isBinary()) valueData = new byte[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, };
                    
                    row[i]=valueData;
                    
                    if (isCompatible()) {
                    	Value value = valueMeta.createOriginalValue(valueData);
                    	Scriptable jsarg = Context.toObject(value, jsscope);
					    jsscope.put(valueMeta.getName(), jsscope, jsarg);
                    }
                    else {
                    	Scriptable jsarg = Context.toObject(valueData, jsscope);
					    jsscope.put(valueMeta.getName(), jsscope, jsarg);
                    }
			    }
			    // Add support for Value class (new Value())
			    Scriptable jsval = Context.toObject(Value.class, jsscope);
			    jsscope.put("Value", jsscope, jsval); //$NON-NLS-1$

                // Add the old style row object for compatibility reasons...
                //
                if (isCompatible()) {
                	Row v2Row = RowMeta.createOriginalRow(prev, row);
                	Scriptable jsV2Row = Context.toObject(v2Row, jsscope);
                    jsscope.put("row", jsscope, jsV2Row); //$NON-NLS-1$
                }
                else {
	                Scriptable jsRow = Context.toObject(row, jsscope);
	                jsscope.put("row", jsscope, jsRow); //$NON-NLS-1$
                }
            } catch(Exception ev){
				error_message="Couldn't add Input fields to Script! Error:"+Const.CR+ev.toString(); //$NON-NLS-1$
				cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepinfo);
				remarks.add(cr);
			}
			
			try{
				// Checking for StartScript
				if(strActiveStartScript != null && strActiveStartScript.length()>0){
					/* Object startScript =*/ jscx.evaluateString(jsscope, strActiveStartScript, "trans_Start", 1, null);
					error_message = "Found Start Script. "+ strActiveStartScriptName+" Processing OK";
					cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, error_message, stepinfo); //$NON-NLS-1$
					remarks.add(cr);
				}
			}catch(Exception e){
				error_message="Couldn't process Start Script! Error:"+Const.CR+e.toString(); //$NON-NLS-1$
				cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepinfo);
				remarks.add(cr);				
			};
			
			try{
				jsscript=jscx.compileString(strActiveScript, "script", 1, null); //$NON-NLS-1$
				
				cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(PKG, "ScriptValuesMetaMod.CheckResult.ScriptCompiledOK"), stepinfo); //$NON-NLS-1$
				remarks.add(cr);

				try{
					
					jsscript.exec(jscx, jsscope);

					cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(PKG, "ScriptValuesMetaMod.CheckResult.ScriptCompiledOK2"), stepinfo); //$NON-NLS-1$
					remarks.add(cr);
					
					if (fieldname.length>0){
						StringBuffer message = new StringBuffer(BaseMessages.getString(PKG, "ScriptValuesMetaMod.CheckResult.FailedToGetValues",String.valueOf(fieldname.length))+Const.CR+Const.CR); //$NON-NLS-1$ //$NON-NLS-2$
												
						if (error_found)
						{
							cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, message.toString(), stepinfo);
						}
						else
						{
							cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, message.toString(), stepinfo);
						}
						remarks.add(cr);
					}
				}catch(JavaScriptException jse){
					Context.exit();
					error_message=BaseMessages.getString(PKG, "ScriptValuesMetaMod.CheckResult.CouldNotExecuteScript")+Const.CR+jse.toString(); //$NON-NLS-1$
					cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepinfo);
					remarks.add(cr);
				}catch(Exception e){
					Context.exit();
					error_message=BaseMessages.getString(PKG, "ScriptValuesMetaMod.CheckResult.CouldNotExecuteScript2")+Const.CR+e.toString(); //$NON-NLS-1$
					cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepinfo);
					remarks.add(cr);
				}
				
				// Checking End Script
				try{
					if(strActiveEndScript != null && strActiveEndScript.length()>0){
						/* Object endScript = */ jscx.evaluateString(jsscope, strActiveEndScript, "trans_End", 1, null);
						error_message = "Found End Script. "+ strActiveEndScriptName+" Processing OK";
						cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, error_message, stepinfo); //$NON-NLS-1$
						remarks.add(cr);
					}
				}catch(Exception e){
					error_message="Couldn't process End Script! Error:"+Const.CR+e.toString(); //$NON-NLS-1$
					cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepinfo);
					remarks.add(cr);				
				};
			}catch(Exception e){
				Context.exit();
				error_message = BaseMessages.getString(PKG, "ScriptValuesMetaMod.CheckResult.CouldNotCompileScript")+Const.CR+e.toString(); //$NON-NLS-1$
				cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepinfo);
				remarks.add(cr);
			}
		}else{
			Context.exit();
			error_message = BaseMessages.getString(PKG, "ScriptValuesMetaMod.CheckResult.CouldNotGetFieldsFromPreviousStep"); //$NON-NLS-1$
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepinfo);
			remarks.add(cr);
		}

		// See if we have input streams leading to this step!
		if (input.length>0){
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(PKG, "ScriptValuesMetaMod.CheckResult.ConnectedStepOK2"), stepinfo); //$NON-NLS-1$
			remarks.add(cr);
		}else{
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(PKG, "ScriptValuesMetaMod.CheckResult.NoInputReceived"), stepinfo); //$NON-NLS-1$
			remarks.add(cr);
		}
	}
	
	public String getFunctionFromScript(String strFunction, String strScript){
		String sRC = "";
		int iStartPos=strScript.indexOf(strFunction);
		if(iStartPos>0){
			iStartPos = strScript.indexOf('{', iStartPos); 
			int iCounter = 1;
			while(iCounter!=0){
				if(strScript.charAt(iStartPos++)=='{') iCounter++;
				else if(strScript.charAt(iStartPos++)=='}') iCounter--;
				sRC = sRC + strScript.charAt(iStartPos);
			}
		}
		return sRC;
	}
	

	public boolean getValue(Scriptable scope, int i, Value res, StringBuffer message)
	{
		boolean error_found = false;
		
		if (fieldname[i]!=null && fieldname[i].length()>0)
		{
			res.setName(rename[i]);
			res.setType(type[i]);
			
			try{
				
				Object result = scope.get(fieldname[i], scope);
				if (result!=null){
					
					String classname = result.getClass().getName();
						
					switch(type[i]){
						case ValueMetaInterface.TYPE_NUMBER:
							if (classname.equalsIgnoreCase("org.mozilla.javascript.Undefined")){
								res.setNull();
							}else if (classname.equalsIgnoreCase("org.mozilla.javascript.NativeJavaObject")){
								// Is it a java Value class ?
								Value v = (Value)Context.jsToJava(result, Value.class);
								res.setValue( v.getNumber() );
							}else{
								res.setValue( ((Double)result).doubleValue() ); 
							}
							break;
						case ValueMetaInterface.TYPE_INTEGER:
							if (classname.equalsIgnoreCase("java.lang.Byte")){
								res.setValue( ((java.lang.Byte)result).longValue() );
							}else if (classname.equalsIgnoreCase("java.lang.Short")){
								res.setValue( ((Short)result).longValue() );
							}else if (classname.equalsIgnoreCase("java.lang.Integer")){
								res.setValue( ((Integer)result).longValue() );
							}else if (classname.equalsIgnoreCase("java.lang.Long")){
								res.setValue( ((Long)result).longValue() );
							}else if (classname.equalsIgnoreCase("org.mozilla.javascript.Undefined")){
								res.setNull();
							}else if (classname.equalsIgnoreCase("org.mozilla.javascript.NativeJavaObject")){
								// Is it a java Value class ?
								Value v = (Value)Context.jsToJava(result, Value.class);
								res.setValue( v.getInteger() );
							}else{
								res.setValue( Math.round( ((Double)result).doubleValue() ) ); 
							}
							break;
					case ValueMetaInterface.TYPE_STRING:  
						if (classname.equalsIgnoreCase("org.mozilla.javascript.NativeJavaObject") ||  //$NON-NLS-1$
							classname.equalsIgnoreCase("org.mozilla.javascript.Undefined")) //$NON-NLS-1$
						{
							// Is it a java Value class ?
							try
							{
								Value v = (Value)Context.jsToJava(result, Value.class);
								res.setValue( v.getString() );
							}
							catch(Exception ev)
							{
								// A String perhaps?
								String s = (String)Context.jsToJava(result, String.class);
								res.setValue( s );
							}
						}
						else
						{
							res.setValue( ((String)result) ); 
						}
						break;
					case ValueMetaInterface.TYPE_DATE:
						double dbl=0;
						if (classname.equalsIgnoreCase("org.mozilla.javascript.Undefined")) //$NON-NLS-1$
						{
							res.setNull();
						}
						else
						{
							if (classname.equalsIgnoreCase("org.mozilla.javascript.NativeDate")) //$NON-NLS-1$
							{
								dbl = Context.toNumber(result);
							}
							else
							if (classname.equalsIgnoreCase("org.mozilla.javascript.NativeJavaObject")) //$NON-NLS-1$
							{
								// Is it a java Date() class ?
								try
								{
									Date dat = (Date)Context.jsToJava(result, java.util.Date.class);
									dbl = dat.getTime();
								}
								catch(Exception e) // Nope, try a Value
								{
									Value v = (Value)Context.jsToJava(result, Value.class);
									Date dat = v.getDate();
									if (dat!=null) dbl = dat.getTime();
									else res.setNull();
								}
							}
							else  // Finally, try a number conversion to time
							{
								dbl = ((Double)result).doubleValue();
							}
							long   lng = Math.round(dbl);
							Date dat = new Date(lng);
							res.setValue( dat );
						}
						break;
					case ValueMetaInterface.TYPE_BOOLEAN: 
						res.setValue( ((Boolean)result).booleanValue()); 
						break;
					default: res.setNull();
					}
				}
				else
				{
					res.setNull();
				}
			}
			catch(Exception e)
			{
				message.append(BaseMessages.getString(PKG, "ScriptValuesMetaMod.CheckResult.ErrorRetrievingValue",fieldname[i])+" : "+e.toString()); //$NON-NLS-1$ //$NON-NLS-2$
				error_found=true;
			}
			res.setLength(length[i], precision[i]);
				
			message.append(BaseMessages.getString(PKG, "ScriptValuesMetaMod.CheckResult.RetrievedValue",fieldname[i],res.toStringMeta())); //$NON-NLS-1$ //$NON-NLS-2$
		}
		else
		{
			message.append(BaseMessages.getString(PKG, "ScriptValuesMetaMod.CheckResult.ValueIsEmpty",String.valueOf(i))); //$NON-NLS-1$ //$NON-NLS-2$
			error_found=true;
		}
		
		return error_found;
	}

	public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta, Trans trans)
	{
		return new ScriptValuesMod(stepMeta, stepDataInterface, cnr, transMeta, trans);
	}

	public StepDataInterface getStepData()
	{
		return new ScriptValuesModData();
	}
	
	
	
	// This is for Additional Classloading
	public void parseXmlForAdditionalClasses() throws KettleException{
		try {
			Properties sysprops = System.getProperties();
			String strActPath = sysprops.getProperty("user.dir");
			Document dom = XMLHandler.loadXMLFile(strActPath + "/plugins/steps/ScriptValues_mod/plugin.xml");
			Node stepnode = dom.getDocumentElement();
			Node libraries = XMLHandler.getSubNode(stepnode, "js_libraries");
			int nbOfLibs = XMLHandler.countNodes(libraries, "js_lib");
			additionalClasses = new ScriptValuesAddClasses[nbOfLibs];
			for(int i=0;i<nbOfLibs;i++){
				Node fnode = XMLHandler.getSubNodeByNr(libraries, "js_lib", i);
				String strJarName = XMLHandler.getTagAttribute(fnode, "name");
				String strClassName =XMLHandler.getTagAttribute(fnode, "classname");
				String strJSName = XMLHandler.getTagAttribute(fnode, "js_name");
				
				Class<?> addClass = LoadAdditionalClass(strActPath + "/plugins/steps/ScriptValues_mod/"+ strJarName,strClassName);
				Object addObject = addClass.newInstance();
				additionalClasses[i] = new ScriptValuesAddClasses(addClass, addObject, strJSName);
			}
		}catch(Exception e) {
			throw new KettleException(BaseMessages.getString(PKG, "ScriptValuesMetaMod.Exception.UnableToParseXMLforAdditionalClasses"), e); 
		}
	}
	
	private static Class<?> LoadAdditionalClass(String strJar, String strClassName) throws KettleException{
		try{
			Thread t = Thread.currentThread();
			ClassLoader cl = t.getContextClassLoader();
			URL u = new URL("jar:file:"+strJar+"!/");
			KettleURLClassLoader kl = new KettleURLClassLoader(new URL[]{u}, cl);
			Class<?> toRun = kl.loadClass(strClassName);
			return toRun;
		}catch(Exception e){
			throw new KettleException(BaseMessages.getString(PKG, "ScriptValuesMetaMod.Exception.UnableToLoadAdditionalClass"), e); //$NON-NLS-1$
		}
	}
	
	public ScriptValuesAddClasses[] getAddClasses(){
		return additionalClasses;
	}

	/**
	 * @return the compatible
	 */
	public boolean isCompatible() {
		return compatible;
	}

	/**
	 * @param compatible the compatible to set
	 */
	public void setCompatible(boolean compatible) {
		this.compatible = compatible;
	}
	
	public boolean supportsErrorHandling() {
		return true;
	}
	
    public String getDialogClassName() 
    {
    	return "org.pentaho.di.ui.trans.steps.scriptvalues_mod.ScriptValuesModDialog";
    }

	/**
	 * @return the replace
	 */
	public boolean[] getReplace() {
		return replace;
	}

	/**
	 * @param replace the replace to set
	 */
	public void setReplace(boolean[] replace) {
		this.replace = replace;
	}
    
}