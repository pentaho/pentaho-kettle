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
 package org.pentaho.di.trans.steps.script;

import java.math.BigDecimal;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.pentaho.di.compatibility.Value;
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
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.KettleURLClassLoader;
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
public class ScriptMeta extends BaseStepMeta implements StepMetaInterface
{	
	private static Class<?> PKG = ScriptMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private static final String JSSCRIPT_TAG_TYPE = "jsScript_type";
	private static final String JSSCRIPT_TAG_NAME = "jsScript_name";
	private static final String JSSCRIPT_TAG_SCRIPT= "jsScript_script";
	
	private ScriptAddClasses[] additionalClasses;
	private ScriptValuesScript[]	jsScripts;
	
	private String  fieldname[];
	private String  rename[];
	private int     type[];
	private int     length[];
	private int     precision[];
	private boolean replace[]; // Replace the specified field.
	
	public ScriptMeta(){
		super(); // allocate BaseStepMeta
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
		ScriptMeta retval = (ScriptMeta)super.clone();
		
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
			throw new KettleXMLException(BaseMessages.getString(PKG, "ScriptMeta.Exception.UnableToLoadStepInfoFromXML"), e); //$NON-NLS-1$
		}
	}

	public void setDefault(){
		jsScripts = new ScriptValuesScript[1];
		jsScripts[0] = new ScriptValuesScript(
				ScriptValuesScript.TRANSFORM_SCRIPT,
				BaseMessages.getString(PKG, "Script.Script1"),
				"//"+ BaseMessages.getString(PKG, "Script.ScriptHere") +Const.CR+Const.CR
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
						throw new KettleStepException(BaseMessages.getString(PKG, "ScriptMeta.Exception.FieldToReplaceNotFound", fieldname[i]));
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
			throw new KettleException(BaseMessages.getString(PKG, "ScriptMeta.Exception.UnexpectedErrorInReadingStepInfo"), e); //$NON-NLS-1$
		}
	}

	public void saveRep(Repository rep, ObjectId id_transformation, ObjectId id_step) throws KettleException
    {
        try
        {
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
            throw new KettleException(BaseMessages.getString(PKG, "ScriptMeta.Exception.UnableToSaveStepInfo") + id_step, e); //$NON-NLS-1$
        }
    }


	public void check(List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepinfo, RowMetaInterface prev, String input[], String output[], RowMetaInterface info)
	{
		boolean error_found=false;
		String error_message = ""; //$NON-NLS-1$
		CheckResult cr;
		
		ScriptEngine jscx;
		Bindings jsscope;
		CompiledScript jsscript;

		jscx = createNewScriptEngine(stepinfo.getName());
		jsscope = jscx.getBindings(ScriptContext.ENGINE_SCOPE);
			
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
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(PKG, "ScriptMeta.CheckResult.ConnectedStepOK",String.valueOf(prev.size())), stepinfo); //$NON-NLS-1$ //$NON-NLS-2$
			remarks.add(cr);
			
			// Adding the existing Scripts to the Context
			for(int i=0;i<getNumberOfJSScripts();i++){
				jsscope.put(jsScripts[i].getScriptName(), jsScripts[i].getScript());
			}
 
			// Modification for Additional Script parsing
			try{
				if (getAddClasses()!=null)
                {
    				for(int i=0;i<getAddClasses().length;i++){
    					//TODO AKRETION ensure it works
    					jsscope.put(getAddClasses()[i].getJSName(), getAddClasses()[i].getAddObject());
    					//Object jsOut = Context.javaToJS(getAddClasses()[i].getAddObject(), jsscope);
    					//ScriptableObject.putProperty(jsscope, getAddClasses()[i].getJSName(), jsOut);
    					//ScriptableObject.putProperty(jsscope, getAddClasses()[i].getJSName(), jsOut);
    				}
                }
			}catch(Exception e){
				error_message = ("Couldn't add JavaClasses to Context! Error:");
				cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepinfo);
				remarks.add(cr);
			}
			
			// Adding some default JavaScriptFunctions to the System
			//TODO AKRETION not implemented yet
//			try {
//				Context.javaToJS(ScriptValuesAddedFunctions.class, jsscope);
//				((ScriptableObject)jsscope).defineFunctionProperties(ScriptValuesAddedFunctions.jsFunctionList, ScriptValuesAddedFunctions.class, ScriptableObject.DONTENUM);
//			} catch (Exception ex) {
//				error_message="Couldn't add Default Functions! Error:"+Const.CR+ex.toString();
//				cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepinfo);
//				remarks.add(cr);
//			};

			// Adding some Constants to the JavaScript
			try {
				jsscope.put("SKIP_TRANSFORMATION", Integer.valueOf(Script.SKIP_TRANSFORMATION));
				jsscope.put("ABORT_TRANSFORMATION", Integer.valueOf(Script.ABORT_TRANSFORMATION));
				jsscope.put("ERROR_TRANSFORMATION", Integer.valueOf(Script.ERROR_TRANSFORMATION));
				jsscope.put("CONTINUE_TRANSFORMATION", Integer.valueOf(Script.CONTINUE_TRANSFORMATION));
			} catch (Exception ex) {
				error_message="Couldn't add Transformation Constants! Error:"+Const.CR+ex.toString(); //$NON-NLS-1$
				cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepinfo);
				remarks.add(cr);
			};

			try{
				ScriptDummy dummyStep = new ScriptDummy(prev, transMeta.getStepFields(stepinfo));
				jsscope.put("_step_", dummyStep); //$NON-NLS-1$

				Object[] row=new Object[prev.size()];
   			    jsscope.put("rowMeta", prev); //$NON-NLS-1$
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
                    
				    jsscope.put(valueMeta.getName(), valueData);
			    }
			    // Add support for Value class (new Value())
			    jsscope.put("Value", Value.class); //$NON-NLS-1$

                // Add the old style row object for compatibility reasons...
                //
                jsscope.put("row", row); //$NON-NLS-1$
            } catch(Exception ev){
				error_message="Couldn't add Input fields to Script! Error:"+Const.CR+ev.toString(); //$NON-NLS-1$
				cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepinfo);
				remarks.add(cr);
			}
			
			try{
				// Checking for StartScript
				if(strActiveStartScript != null && strActiveStartScript.length()>0){
					jscx.eval(strActiveStartScript, jsscope);
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
				jsscript = ((Compilable) jscx).compile(strActiveScript);
				
				//cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(PKG, "ScriptMeta.CheckResult.ScriptCompiledOK"), stepinfo); //$NON-NLS-1$
				//remarks.add(cr);

				try{
					
					jsscript.eval(jsscope);

					cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(PKG, "ScriptMeta.CheckResult.ScriptCompiledOK2"), stepinfo); //$NON-NLS-1$
					remarks.add(cr);
					
					if (fieldname.length>0){
						StringBuffer message = new StringBuffer(BaseMessages.getString(PKG, "ScriptMeta.CheckResult.FailedToGetValues",String.valueOf(fieldname.length))+Const.CR+Const.CR); //$NON-NLS-1$ //$NON-NLS-2$
												
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
				}catch(ScriptException jse){
					//Context.exit(); TODO AKRETION NOT SURE
					error_message=BaseMessages.getString(PKG, "ScriptMeta.CheckResult.CouldNotExecuteScript")+Const.CR+jse.toString(); //$NON-NLS-1$
					cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepinfo);
					remarks.add(cr);
				}catch(Exception e){
					//Context.exit(); TODO AKRETION NOT SURE
					error_message=BaseMessages.getString(PKG, "ScriptMeta.CheckResult.CouldNotExecuteScript2")+Const.CR+e.toString(); //$NON-NLS-1$
					cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepinfo);
					remarks.add(cr);
				}
				
				// Checking End Script
				try{
					if(strActiveEndScript != null && strActiveEndScript.length()>0){
						/* Object endScript = */ jscx.eval(strActiveEndScript, jsscope);
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
				//Context.exit(); TODO AKRETION NOT SURE
				error_message = BaseMessages.getString(PKG, "ScriptMeta.CheckResult.CouldNotCompileScript")+Const.CR+e.toString(); //$NON-NLS-1$
				cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepinfo);
				remarks.add(cr);
			}
		}else{
			//Context.exit(); TODO AKRETION NOT SURE
			error_message = BaseMessages.getString(PKG, "ScriptMeta.CheckResult.CouldNotGetFieldsFromPreviousStep"); //$NON-NLS-1$
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepinfo);
			remarks.add(cr);
		}

		// See if we have input streams leading to this step!
		if (input.length>0){
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(PKG, "ScriptMeta.CheckResult.ConnectedStepOK2"), stepinfo); //$NON-NLS-1$
			remarks.add(cr);
		}else{
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(PKG, "ScriptMeta.CheckResult.NoInputReceived"), stepinfo); //$NON-NLS-1$
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
	

	public boolean getValue(Bindings scope, int i, Value res, StringBuffer message)
	{
		boolean error_found = false;
		
		if (fieldname[i]!=null && fieldname[i].length()>0)
		{
			res.setName(rename[i]);
			res.setType(type[i]);
			
			try{
				
				Object result = scope.get(fieldname[i]);
				if (result!=null){
					
					String classname = result.getClass().getName();
						
					switch(type[i]){
						case ValueMetaInterface.TYPE_NUMBER:
							if (classname.equalsIgnoreCase("org.mozilla.javascript.Undefined")){
								res.setNull();
							}else if (classname.equalsIgnoreCase("org.mozilla.javascript.NativeJavaObject")){
								// Is it a java Value class ?
								Value v = (Value) result;
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
								Value v = (Value) result;
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
								Value v = (Value) result;
								res.setValue( v.getString() );
							}
							catch(Exception ev)
							{
								// A String perhaps?
								String s = (String) result;
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
								dbl = (Double) result;//TODO AKRETION not sure!
							}
							else
							if (classname.equalsIgnoreCase("org.mozilla.javascript.NativeJavaObject")) //$NON-NLS-1$
							{
								// Is it a java Date() class ?
								try
								{
									Date dat = (Date) result;
									dbl = dat.getTime();
								}
								catch(Exception e) // Nope, try a Value
								{
									Value v = (Value) result;
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
				message.append(BaseMessages.getString(PKG, "ScriptMeta.CheckResult.ErrorRetrievingValue",fieldname[i])+" : "+e.toString()); //$NON-NLS-1$ //$NON-NLS-2$
				error_found=true;
			}
			res.setLength(length[i], precision[i]);
				
			message.append(BaseMessages.getString(PKG, "ScriptMeta.CheckResult.RetrievedValue",fieldname[i],res.toStringMeta())); //$NON-NLS-1$ //$NON-NLS-2$
		}
		else
		{
			message.append(BaseMessages.getString(PKG, "ScriptMeta.CheckResult.ValueIsEmpty",String.valueOf(i))); //$NON-NLS-1$ //$NON-NLS-2$
			error_found=true;
		}
		
		return error_found;
	}

	public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta, Trans trans)
	{
		return new Script(stepMeta, stepDataInterface, cnr, transMeta, trans);
	}

	public StepDataInterface getStepData()
	{
		return new ScriptData();
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
			additionalClasses = new ScriptAddClasses[nbOfLibs];
			for(int i=0;i<nbOfLibs;i++){
				Node fnode = XMLHandler.getSubNodeByNr(libraries, "js_lib", i);
				String strJarName = XMLHandler.getTagAttribute(fnode, "name");
				String strClassName =XMLHandler.getTagAttribute(fnode, "classname");
				String strJSName = XMLHandler.getTagAttribute(fnode, "js_name");
				
				Class<?> addClass = LoadAdditionalClass(strActPath + "/plugins/steps/ScriptValues_mod/"+ strJarName,strClassName);
				Object addObject = addClass.newInstance();
				additionalClasses[i] = new ScriptAddClasses(addClass, addObject, strJSName);
			}
		}catch(Exception e) {
			throw new KettleException(BaseMessages.getString(PKG, "ScriptMeta.Exception.UnableToParseXMLforAdditionalClasses"), e); 
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
			throw new KettleException(BaseMessages.getString(PKG, "ScriptMeta.Exception.UnableToLoadAdditionalClass"), e); //$NON-NLS-1$
		}
	}
	
	public ScriptAddClasses[] getAddClasses(){
		return additionalClasses;
	}
	
	public boolean supportsErrorHandling() {
		return true;
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
	
	/**
	 * Instanciates the right scripting language interpreter, falling back to Javascript for backward compat.
	 * Because Kettle GUI sucks for extensibility, we use the script name extension
	 * to determine the language rather than add a Combo box. Complain to Pentaho please.
	 * @param stepName
	 * @return
	 */
	public static ScriptEngine createNewScriptEngine(String stepName) {
		System.setProperty("org.jruby.embed.localvariable.behavior", "persistent");//required for JRuby, transparent for others
		ScriptEngineManager manager = new ScriptEngineManager();
		String[] strings = stepName.split("\\.");
		String extension = strings[strings.length > 0 ? 1 : 0];//skip the script number extension
		ScriptEngine scriptEngine = manager.getEngineByName(extension);
		if (scriptEngine == null) {//falls back to Javascript
			scriptEngine = manager.getEngineByName("javascript");
		}
		return scriptEngine;
	}
    
}