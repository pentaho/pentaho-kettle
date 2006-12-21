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

package be.ibridge.kettle.trans.step.scriptvalues_mod;

import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.Properties;

import org.eclipse.swt.widgets.Shell;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.JavaScriptException;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import be.ibridge.kettle.core.CheckResult;
import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.XMLHandler;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.exception.KettleXMLException;
import be.ibridge.kettle.core.value.Value;
import be.ibridge.kettle.repository.Repository;
import be.ibridge.kettle.trans.KettleURLClassLoader;
import be.ibridge.kettle.trans.Trans;
import be.ibridge.kettle.trans.TransMeta;
import be.ibridge.kettle.trans.step.BaseStepMeta;
import be.ibridge.kettle.trans.step.StepDataInterface;
import be.ibridge.kettle.trans.step.StepDialogInterface;
import be.ibridge.kettle.trans.step.StepInterface;
import be.ibridge.kettle.trans.step.StepMeta;
import be.ibridge.kettle.trans.step.StepMetaInterface;

/*
 * Created on 2-jun-2003
 *
 */

public class ScriptValuesMetaMod extends BaseStepMeta implements StepMetaInterface
{
	
	private ScriptValuesAddClasses[] additionaClasses;
	private ScriptValuesScript[]	jsScripts;
	
	private String  script;
	
	private String  name[];
	private String  rename[];
	private int     type[];
	private int     length[];
	private int     precision[];
	
	public ScriptValuesMetaMod(){
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
    public String[] getName(){
        return name;
    }
    
    /**
     * @param name The name to set.
     */
    public void setName(String[] name){
        this.name = name;
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
     * @return Returns the script.
     */
    public String getScript()
    {
        return script;
    }
    
    /**
     * @param script The script to set.
     */
    public void setScript(String script)
    {
        this.script = script;
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
    
    
	public void loadXML(Node stepnode, ArrayList databases, Hashtable counters)
		throws KettleXMLException
	{
		readData(stepnode);
	}

	public void allocate(int nrfields){
		name      = new String[nrfields];
		rename    = new String[nrfields];
		type      = new int   [nrfields];
		length    = new int   [nrfields];
		precision = new int   [nrfields];
	}

	public Object clone()
	{
		ScriptValuesMetaMod retval = (ScriptValuesMetaMod)super.clone();
		
		int nrfields = name.length;
		
		retval.allocate(nrfields);
		
		for (int i=0;i<nrfields;i++)
		{
			retval.name     [i] = name[i];
			retval.rename   [i] = rename[i];
			retval.type     [i] = type[i];
			retval.length   [i] = length[i];
			retval.precision[i] = precision[i];
		}

		return retval;
	}
	
	private void readData(Node stepnode) throws KettleXMLException{
		try	{
			String slen, sprc;
			int i, nrfields, nrscripts;
			
			script     = XMLHandler.getTagValue(stepnode, "script"); 
			
			Node scripts = XMLHandler.getSubNode(stepnode, "jsScripts");
			nrscripts = XMLHandler.countNodes(scripts, "jsScript");
			jsScripts = new ScriptValuesScript[nrscripts];
			for (i=0;i<nrscripts;i++){
				Node fnode = XMLHandler.getSubNodeByNr(scripts, "jsScript", i); //$NON-NLS-1$
				
				jsScripts[i] = new ScriptValuesScript(
						Integer.parseInt(XMLHandler.getTagValue(fnode, "jsScript_type")),
						XMLHandler.getTagValue(fnode, "jsScript_name"), 
						XMLHandler.getTagValue(fnode, "jsScript_script") 
				);
				
			}	
			
			Node fields = XMLHandler.getSubNode(stepnode, "fields"); //$NON-NLS-1$
			nrfields = XMLHandler.countNodes(fields, "field"); //$NON-NLS-1$
				
			allocate(nrfields);
			
			for (i=0;i<nrfields;i++)
			{
				Node fnode = XMLHandler.getSubNodeByNr(fields, "field", i); //$NON-NLS-1$
				
				name     [i] = XMLHandler.getTagValue(fnode, "name"); //$NON-NLS-1$
				rename   [i] = XMLHandler.getTagValue(fnode, "rename"); //$NON-NLS-1$
				type     [i] = Value.getType(XMLHandler.getTagValue(fnode, "type")); //$NON-NLS-1$
	
				slen = XMLHandler.getTagValue(fnode, "length"); //$NON-NLS-1$
				sprc = XMLHandler.getTagValue(fnode, "precision"); //$NON-NLS-1$
				length   [i]=Const.toInt(slen, -1);
				precision[i]=Const.toInt(sprc, -1);
			}
		}
		catch(Exception e)
		{
			//throw new KettleXMLException(Messages.getString("ScriptValuesMeta.Exception.UnableToLoadStepInfoFromXML"), e); //$NON-NLS-1$
			throw new KettleXMLException("ScriptValuesMeta.Exception.UnableToLoadStepInfoFromXML", e); //$NON-NLS-1$
		}
	}

	public void setDefault(){
		script = ""; //$NON-NLS-1$
		jsScripts = new ScriptValuesScript[1];
		jsScripts[0] = new ScriptValuesScript(
				ScriptValuesScript.TRANSFORM_SCRIPT,
				"Script 1",
				"//Script here"
		);
		
		int nrfields=0;
		allocate(nrfields);

		for (int i=0;i<nrfields;i++){
			name     [i] = "newvalue"; //$NON-NLS-1$
			rename   [i] = "newvalue"; //$NON-NLS-1$
			type     [i] = Value.VALUE_TYPE_NUMBER;
			length   [i] = -1;
			precision[i] = -1;
		}
	}

	public Row getFields(Row r, String name, Row info)
	{
		Row row;
		if (r==null) row=new Row(); // give back values
		else         row=r;         // add to the existing row of values...
		
		for (int i=0;i<this.name.length;i++)
		{
			if (this.name[i]!=null || rename[i]!=null)
			{
				Value v;
				if (rename[i]!=null && rename[i].length()!=0) 
				{
					v = new Value(rename[i], type[i]);
				} 
				else
				{
					v = new Value(this.name[i], type[i]); 
				} 
				v.setLength(length[i], precision[i]);
				v.setOrigin(name);
				row.addValue( v );
			}
		}
		
		return row;
	}

	public String getXML(){
        StringBuffer retval = new StringBuffer();
		
		retval.append("    "+XMLHandler.addTagValue("script", script)); //$NON-NLS-1$ //$NON-NLS-2$
        

		retval.append("    <jsScripts>"); 
		for (int i=0;i<jsScripts.length;i++){
			retval.append("      <jsScript>"); //$NON-NLS-1$
			retval.append("        "+XMLHandler.addTagValue("jsScript_type",  jsScripts[i].getScriptType()));
			retval.append("        "+XMLHandler.addTagValue("jsScript_name",  jsScripts[i].getScriptName()));
			retval.append("        "+XMLHandler.addTagValue("jsScript_script", jsScripts[i].getScript()));
			retval.append("        </jsScript>"); //$NON-NLS-1$
		}
		retval.append("     </jsScripts>"); 
		
		retval.append("    <fields>"); //$NON-NLS-1$
		for (int i=0;i<name.length;i++)
		{
			retval.append("      <field>"); //$NON-NLS-1$
			retval.append("        "+XMLHandler.addTagValue("name",      name[i])); //$NON-NLS-1$ //$NON-NLS-2$
			retval.append("        "+XMLHandler.addTagValue("rename",    rename[i])); //$NON-NLS-1$ //$NON-NLS-2$
			retval.append("        "+XMLHandler.addTagValue("type",      Value.getTypeDesc(type[i]))); //$NON-NLS-1$ //$NON-NLS-2$
			retval.append("        "+XMLHandler.addTagValue("length",    length[i])); //$NON-NLS-1$ //$NON-NLS-2$
			retval.append("        "+XMLHandler.addTagValue("precision", precision[i])); //$NON-NLS-1$ //$NON-NLS-2$
			retval.append("        </field>"); //$NON-NLS-1$
		}
		retval.append("      </fields>"); //$NON-NLS-1$
		
		return retval.toString();
	}

	public void readRep(Repository rep, long id_step, ArrayList databases, Hashtable counters)throws KettleException{
		try
		{
			script     = rep.getStepAttributeString(id_step, "script"); //$NON-NLS-1$
			
			int nrScripts = rep.countNrStepAttributes(id_step, "jsScript_name"); //$NON-NLS-1$
			jsScripts = new ScriptValuesScript[nrScripts];
			for(int i=0;i<nrScripts;i++){
				jsScripts[i] = new ScriptValuesScript(
						(int)rep.getStepAttributeInteger(id_step, i, "jsScript_type"),
						rep.getStepAttributeString (id_step, i, "jsScript_name"),
						rep.getStepAttributeString (id_step, i, "jsScript_Script")
					);
				
			}
			
			
			int nrfields = rep.countNrStepAttributes(id_step, "field_name"); //$NON-NLS-1$
			allocate(nrfields);
	
			for (int i=0;i<nrfields;i++)
			{
				name[i]        =       rep.getStepAttributeString (id_step, i, "field_name"); //$NON-NLS-1$
				rename[i]      =       rep.getStepAttributeString (id_step, i, "field_rename"); //$NON-NLS-1$
				type[i]        =  Value.getType( rep.getStepAttributeString (id_step, i, "field_type") ); //$NON-NLS-1$
				length[i]      =  (int)rep.getStepAttributeInteger(id_step, i, "field_length"); //$NON-NLS-1$
				precision[i]   =  (int)rep.getStepAttributeInteger(id_step, i, "field_precision"); //$NON-NLS-1$
			}
		}
		catch(Exception e)
		{
			//throw new KettleException(Messages.getString("ScriptValuesMeta.Exception.UnexpectedErrorInReadingStepInfo"), e); //$NON-NLS-1$
			throw new KettleException("ScriptValuesMeta.Exception.UnexpectedErrorInReadingStepInfo", e); //$NON-NLS-1$
		}
	}

	public void saveRep(Repository rep, long id_transformation, long id_step) throws KettleException{
		try{
			rep.saveStepAttribute(id_transformation, id_step, "script", script); //$NON-NLS-1$
	
			for(int i=0;i<jsScripts.length;i++){
				rep.saveStepAttribute(id_transformation, id_step, i, "jsScript_name", jsScripts[i].getScriptName());
				rep.saveStepAttribute(id_transformation, id_step, i, "jsScript_script",jsScripts[i].getScript());
				rep.saveStepAttribute(id_transformation, id_step, i, "jsScript_type",jsScripts[i].getScriptType());
			}
			
			
			for (int i=0;i<name.length;i++)
			{
				rep.saveStepAttribute(id_transformation, id_step, i, "field_name",      name[i]); //$NON-NLS-1$
				rep.saveStepAttribute(id_transformation, id_step, i, "field_rename",    rename[i]); //$NON-NLS-1$
				rep.saveStepAttribute(id_transformation, id_step, i, "field_type",      Value.getTypeDesc(type[i])); //$NON-NLS-1$
				rep.saveStepAttribute(id_transformation, id_step, i, "field_length",    length[i]); //$NON-NLS-1$
				rep.saveStepAttribute(id_transformation, id_step, i, "field_precision", precision[i]); //$NON-NLS-1$
			}
		}
		catch(Exception e)
		{
			//throw new KettleException(Messages.getString("ScriptValuesMeta.Exception.UnableToSaveStepInfo")+id_step, e); //$NON-NLS-1$
			throw new KettleException(("ScriptValuesMeta.Exception.UnableToSaveStepInfo")+id_step, e); //$NON-NLS-1$
		}
	}

	//private boolean test(boolean getvars, boolean popup)
	public void check(ArrayList remarks, StepMeta stepinfo, Row prev, String input[], String output[], Row info)
	{
		
		boolean error_found=false;
		String error_message = ""; //$NON-NLS-1$
		CheckResult cr;
		
		Context jscx;
		Scriptable jsscope;
		Script jsscript;

		jscx = Context.enter();
		jsscope = jscx.initStandardObjects(null);
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
		
		if (prev!=null && prev.size()>0 && strActiveScript.length()>0)		{
			//cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("ScriptValuesMeta.CheckResult.ConnectedStepOK",String.valueOf(prev.size())), stepinfo); //$NON-NLS-1$ //$NON-NLS-2$
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, ("ScriptValuesMeta.CheckResult.ConnectedStepOK"), stepinfo); //$NON-NLS-1$ //$NON-NLS-2$
			remarks.add(cr);

			// Adding the existing Scripts to the Context
			for(int i=0;i<getNumberOfJSScripts();i++){
				Scriptable jsR = Context.toObject(jsScripts[i].getScript(), jsscope);
				jsscope.put(jsScripts[i].getScriptName(), jsscope, jsR);
			}
 
			// Modification for Additional Script parsing
			try{
				for(int i=0;i<getAddClasses().length;i++){
					Object jsOut = Context.javaToJS(getAddClasses()[i].getAddObject(), jsscope);
					ScriptableObject.putProperty(jsscope, getAddClasses()[i].getJSName(), jsOut);
				}
			}catch(Exception e){
				error_message = ("Coundln't not add JavaClasses to Context! Error:");
				cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, error_message, stepinfo);
				remarks.add(cr);
			}
			
			// Adding some default JavaScriptFunctions to the System
			try {
				Context.javaToJS(ScriptValuesAddedFunctions.class, jsscope);
				((ScriptableObject)jsscope).defineFunctionProperties(ScriptValuesAddedFunctions.jsFunctionList, ScriptValuesAddedFunctions.class, ScriptableObject.DONTENUM);
			} catch (Exception ex) {
				error_message="Coundln't not add Default Functions! Error:"+Const.CR+ex.toString();
				cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, error_message, stepinfo);
				remarks.add(cr);
			};

			// Adding some Constants to the JavaScript
			try {
				jsscope.put("SKIP_TRANSFORMATION", jsscope, new Integer(ScriptValuesMod.SKIP_TRANSFORMATION));
				jsscope.put("ABORT_TRANSFORMATION", jsscope, new Integer(ScriptValuesMod.ABORT_TRANSFORMATION));
				jsscope.put("ERROR_TRANSFORMATION", jsscope, new Integer(ScriptValuesMod.ERROR_TRANSFORMATION));
				jsscope.put("CONTINUE_TRANSFORMATION", jsscope, new Integer(ScriptValuesMod.CONTINUE_TRANSFORMATION));
			} catch (Exception ex) {
				error_message="Coundln't not add Transformation Constants! Error:"+Const.CR+ex.toString(); //$NON-NLS-1$
				cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, error_message, stepinfo);
				remarks.add(cr);
			};
			
			try{
				ScriptableObject.defineClass(jsscope, tranVar.class);
				for (int i=0;i<prev.size();i++){
					Value val = prev.getValue(i); 
					// Set date and string values to something to simulate real thing
					if (val.isDate()) val.setValue(new Date());
					if (val.isString()) val.setValue("000000000"); //$NON-NLS-1$
					Object[] arg = {new String(val.getName())};
					tranVar objTV = (tranVar)jscx.newObject(jsscope, "tranVar", arg);
					objTV.setValue(val);
					jsscope.put(val.getName(), jsscope, objTV);
				}
			}catch(Exception ev){
				error_message="Coundln't not add Input fields to Script! Error:"+Const.CR+ev.toString(); //$NON-NLS-1$
				cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, error_message, stepinfo);
				remarks.add(cr);
			}
			
			try{
				// Checking for StartScript
				if(strActiveStartScript != null && strActiveStartScript.length()>0){
					/* Object startScript =*/ jscx.evaluateString(jsscope, strActiveStartScript, "trans_Start", 1, null);
					error_message = "Found Start Script. "+ strActiveStartScriptName+" Processing OK";
					cr = new CheckResult(CheckResult.TYPE_RESULT_OK, error_message, stepinfo); //$NON-NLS-1$
					remarks.add(cr);
				}
			}catch(Exception e){
				error_message="Coundln't not processing Start Script! Error:"+Const.CR+e.toString(); //$NON-NLS-1$
				cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, error_message, stepinfo);
				remarks.add(cr);				
			};
			
			try{
				jsscript=jscx.compileString(strActiveScript, "script", 1, null); //$NON-NLS-1$
				
				//cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("ScriptValuesMeta.CheckResult.ScriptCompiledOK"), stepinfo); //$NON-NLS-1$
				cr = new CheckResult(CheckResult.TYPE_RESULT_OK, "ScriptValuesMeta.CheckResult.ScriptCompiledOK", stepinfo); //$NON-NLS-1$
				remarks.add(cr);

				try{
					
					jsscript.exec(jscx, jsscope);

					//cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("ScriptValuesMeta.CheckResult.ScriptCompiledOK2"), stepinfo); //$NON-NLS-1$
					cr = new CheckResult(CheckResult.TYPE_RESULT_OK, "ScriptValuesMeta.CheckResult.ScriptCompiledOK2", stepinfo); //$NON-NLS-1$
					remarks.add(cr);
					
					if (name.length>0){
						//StringBuffer message = new StringBuffer(Messages.getString("ScriptValuesMeta.CheckResult.FailedToGetValues",String.valueOf(name.length))+Const.CR+Const.CR); //$NON-NLS-1$ //$NON-NLS-2$
						StringBuffer message = new StringBuffer("ScriptValuesMeta.CheckResult.FailedToGetValues"); //$NON-NLS-1$ //$NON-NLS-2$
						
						/*for (int i=0;i<name.length;i++){
							Value res = new Value();
							message.append("   "); //$NON-NLS-1$
							if (getValue(jsscope, i, res, message)){
								error_found = true;
							}
							message.append(Const.CR);
						}*/
						
						if (error_found)
						{
							cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, message.toString(), stepinfo);
						}
						else
						{
							cr = new CheckResult(CheckResult.TYPE_RESULT_OK, message.toString(), stepinfo);
						}
						remarks.add(cr);
					}
				}catch(JavaScriptException jse){
					Context.exit();
					//error_message=Messages.getString("ScriptValuesMeta.CheckResult.CouldNotExecuteScript")+Const.CR+jse.toString(); //$NON-NLS-1$
					error_message=("ScriptValuesMeta.CheckResult.CouldNotExecuteScript")+Const.CR+jse.toString(); //$NON-NLS-1$
					cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, error_message, stepinfo);
					remarks.add(cr);
				}catch(Exception e){
					Context.exit();
					//error_message=Messages.getString("ScriptValuesMeta.CheckResult.CouldNotExecuteScript2")+Const.CR+e.toString(); //$NON-NLS-1$
					error_message=("ScriptValuesMeta.CheckResult.CouldNotExecuteScript2")+Const.CR+e.toString(); //$NON-NLS-1$
					cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, error_message, stepinfo);
					remarks.add(cr);
				}
				
				// Checking End Script
				try{
					if(strActiveEndScript != null && strActiveEndScript.length()>0){
						/* Object endScript = */ jscx.evaluateString(jsscope, strActiveEndScript, "trans_End", 1, null);
						error_message = "Found End Script. "+ strActiveEndScriptName+" Processing OK";
						cr = new CheckResult(CheckResult.TYPE_RESULT_OK, error_message, stepinfo); //$NON-NLS-1$
						remarks.add(cr);
					}
				}catch(Exception e){
					error_message="Coundln't not processing End Script! Error:"+Const.CR+e.toString(); //$NON-NLS-1$
					cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, error_message, stepinfo);
					remarks.add(cr);				
				};
			}catch(Exception e){
				Context.exit();
				//error_message = Messages.getString("ScriptValuesMeta.CheckResult.CouldNotCompileScript")+Const.CR+e.toString(); //$NON-NLS-1$
				error_message = ("ScriptValuesMeta.CheckResult.CouldNotCompileScript")+Const.CR+e.toString(); //$NON-NLS-1$
				cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, error_message, stepinfo);
				remarks.add(cr);
			}
		}else{
			Context.exit();
			//error_message = Messages.getString("ScriptValuesMeta.CheckResult.CouldNotGetFieldsFromPreviousStep"); //$NON-NLS-1$
			error_message = ("ScriptValuesMeta.CheckResult.CouldNotGetFieldsFromPreviousStep"); //$NON-NLS-1$
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, error_message, stepinfo);
			remarks.add(cr);
		}

		// See if we have input streams leading to this step!
		if (input.length>0){
			//cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("ScriptValuesMeta.CheckResult.ConnectedStepOK2"), stepinfo); //$NON-NLS-1$
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, ("ScriptValuesMeta.CheckResult.ConnectedStepOK2"), stepinfo); //$NON-NLS-1$
			remarks.add(cr);
		}else{
			//cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages.getString("ScriptValuesMeta.CheckResult.NoInputReceived"), stepinfo); //$NON-NLS-1$
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, ("ScriptValuesMeta.CheckResult.NoInputReceived"), stepinfo); //$NON-NLS-1$
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
		
		if (name[i]!=null && name[i].length()>0)
		{
			res.setName(rename[i]);
			res.setType(type[i]);
			
			try{
				
				Object result = scope.get(name[i], scope);
				if (result!=null){
					
					String classname = result.getClass().getName();
						
					switch(type[i]){
						case Value.VALUE_TYPE_NUMBER:
							if (classname.equalsIgnoreCase("org.mozilla.javascript.Undefined")){
								res.setNull();
							}else if (classname.equalsIgnoreCase("org.mozilla.javascript.NativeJavaObject")){
								// Is it a java Value class ?
								Value v = (Value)Context.toType(result, Value.class);
								res.setValue( v.getNumber() );
							}else{
								res.setValue( ((Double)result).doubleValue() ); 
							}
							break;
						case Value.VALUE_TYPE_INTEGER:
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
								Value v = (Value)Context.toType(result, Value.class);
								res.setValue( v.getInteger() );
							}else{
								res.setValue( Math.round( ((Double)result).doubleValue() ) ); 
							}
							break;
					case Value.VALUE_TYPE_STRING:  
						if (classname.equalsIgnoreCase("org.mozilla.javascript.NativeJavaObject") ||  //$NON-NLS-1$
							classname.equalsIgnoreCase("org.mozilla.javascript.Undefined")) //$NON-NLS-1$
						{
							// Is it a java Value class ?
							try
							{
								Value v = (Value)Context.toType(result, Value.class);
								res.setValue( v.getString() );
							}
							catch(Exception ev)
							{
								// A String perhaps?
								String s = (String)Context.toType(result, String.class);
								res.setValue( s );
							}
						}
						else
						{
							res.setValue( ((String)result) ); 
						}
						break;
					case Value.VALUE_TYPE_DATE:
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
									Date dat = (Date)Context.toType(result, java.util.Date.class);
									dbl = dat.getTime();
								}
								catch(Exception e) // Nope, try a Value
								{
									Value v = (Value)Context.toType(result, Value.class);
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
					case Value.VALUE_TYPE_BOOLEAN: 
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
				//message.append(Messages.getString("ScriptValuesMeta.CheckResult.ErrorRetrievingValue",name[i])+" : "+e.toString()); //$NON-NLS-1$ //$NON-NLS-2$
				message.append("ScriptValuesMeta.CheckResult.ErrorRetrievingValue : "+e.toString()); //$NON-NLS-1$ //$NON-NLS-2$
				error_found=true;
			}
			res.setLength(length[i], precision[i]);
				
			//message.append(Messages.getString("ScriptValuesMeta.CheckResult.RetrievedValue",name[i],res.toStringMeta())); //$NON-NLS-1$ //$NON-NLS-2$
			message.append("ScriptValuesMeta.CheckResult.RetrievedValue"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		else
		{
			//message.append(Messages.getString("ScriptValuesMeta.CheckResult.ValueIsEmpty",String.valueOf(i))); //$NON-NLS-1$ //$NON-NLS-2$
			message.append(("ScriptValuesMeta.CheckResult.ValueIsEmpty")); //$NON-NLS-1$ //$NON-NLS-2$
			error_found=true;
		}
		
		return error_found;
	}

	public StepDialogInterface getDialog(Shell shell, StepMetaInterface info, TransMeta transMeta, String name)
	{
		return new ScriptValuesDialogMod(shell, info, transMeta, name);
	}

	public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta, Trans trans)
	{
		return new ScriptValuesMod(stepMeta, stepDataInterface, cnr, transMeta, trans);
	}

	public StepDataInterface getStepData()
	{
		return new ScriptValuesDataMod();
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
			additionaClasses = new ScriptValuesAddClasses[nbOfLibs];
			for(int i=0;i<nbOfLibs;i++){
				Node fnode = XMLHandler.getSubNodeByNr(libraries, "js_lib", i);
				String strJarName = XMLHandler.getTagAttribute(fnode, "name");
				String strClassName =XMLHandler.getTagAttribute(fnode, "classname");
				String strJSName = XMLHandler.getTagAttribute(fnode, "js_name");
				
				Class addClass = LoadAdditionalClass(strActPath + "/plugins/steps/ScriptValues_mod/"+ strJarName,strClassName);
				Object addObject = addClass.newInstance();
				additionaClasses[i] = new ScriptValuesAddClasses(addClass, addObject, strJSName);
			}
		}catch(Exception e) {
			throw new KettleException("ScriptValuesMeta.Exception.UnableToParseXMLforAdditionalClasses", e); 
		}
	}
	
	private static Class LoadAdditionalClass(String strJar, String strClassName) throws KettleException{
		try{
			Thread t = Thread.currentThread();
			ClassLoader cl = t.getContextClassLoader();
			URL u = new URL("jar:file:"+strJar+"!/");
			KettleURLClassLoader kl = new KettleURLClassLoader(new URL[]{u}, cl);
			Class toRun = kl.loadClass(strClassName);
			return toRun;
		}catch(Exception e){
			System.out.println(e.toString());
			throw new KettleException("ScriptValuesMeta.Exception.UnableToLoadAdditionalClass", e); //$NON-NLS-1$
		}
	}
	
	public ScriptValuesAddClasses[] getAddClasses(){
		return additionaClasses;
	}
}
