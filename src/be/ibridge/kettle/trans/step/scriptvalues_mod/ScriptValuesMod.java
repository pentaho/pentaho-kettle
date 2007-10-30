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

import java.math.BigDecimal;
import java.util.Date;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.JavaScriptException;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.value.Value;
import be.ibridge.kettle.trans.Trans;
import be.ibridge.kettle.trans.TransMeta;
import be.ibridge.kettle.trans.step.BaseStep;
import be.ibridge.kettle.trans.step.StepDataInterface;
import be.ibridge.kettle.trans.step.StepInterface;
import be.ibridge.kettle.trans.step.StepMeta;
import be.ibridge.kettle.trans.step.StepMetaInterface;

/**
 * Executes a javascript on the values in the input stream. 
 * Selected calculated values can then be put on the output stream.
 * 
 * @author Matt
 * @since 5-apr-2003
 *
 */
public class ScriptValuesMod extends BaseStep implements StepInterface
{
	private ScriptValuesMetaMod meta;
	private ScriptValuesDataMod data;
		
	public final static int SKIP_TRANSFORMATION = 1;
	public final static int ABORT_TRANSFORMATION = -1;
	public final static int ERROR_TRANSFORMATION = -2;
	public final static int CONTINUE_TRANSFORMATION = 0;
	
	private static boolean bWithTransStat = false;
	private static boolean bRC = false;
	private static int iTranStat = CONTINUE_TRANSFORMATION;
	
	private boolean bFirstRun = false;
	
	private ScriptValuesScript[] jsScripts;
	private String strTransformScript="";
	private String strStartScript="";
	private String strEndScript="";
	
	public static Row insertRow;
	
	public static Script script;

	
	public ScriptValuesMod(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans){
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}
	
	private void determineUsedFields(Row row){
		int nr=0;
		// Count the occurences of the values.
		// Perhaps we find values in comments, but we take no risk!
		for (int i=0;i<row.size();i++){
			String valname = row.getValue(i).getName().toUpperCase();
			if (strTransformScript.toUpperCase().indexOf(valname)>=0){
				nr++;
			}
		}
		
		// Allocate fields_used
		data.fields_used = new int[nr];
		nr = 0;
		// Count the occurences of the values.
		// Perhaps we find values in comments, but we take no risk!
		for (int i=0;i<row.size();i++){
			// Values are case-insensitive in JavaScript.
			//
			String valname = row.getValue(i).getName();
			if (strTransformScript.indexOf(valname)>=0){
				if (log.isDetailed()) logDetailed(Messages.getString("ScriptValuesMod.Log.UsedValueName",String.valueOf(i),valname)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				data.fields_used[nr]=i;
				nr++;
			}
		}
		
		if (log.isDetailed()) logDetailed(Messages.getString("ScriptValuesMod.Log.UsingValuesFromInputStream",String.valueOf(data.fields_used.length))); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	private synchronized int addValues(Row row){
		if (first){
			// Determine the indexes of the fields used!
			determineUsedFields(row);
			
			data.cx = Context.enter();
			data.cx.setOptimizationLevel(9);
			data.scope = data.cx.initStandardObjects(null,true);
			
			first = false;
			bFirstRun = true;

			Scriptable jsvalue = Context.toObject(this, data.scope);
			data.scope.put("_step_", data.scope, jsvalue); //$NON-NLS-1$
    		
			// Adding the existing Scripts to the Context
			for(int i=0;i<meta.getNumberOfJSScripts();i++){
				Scriptable jsR = Context.toObject(jsScripts[i].getScript(), data.scope);
				data.scope.put(jsScripts[i].getScriptName(), data.scope, jsR);
			}
			
			// Adding the Name of the Transformation to the Context
			data.scope.put("_TransformationName_", data.scope, new String(this.getName()));
			
			try{
				// add these now (they will be readded later) to make compilation succeed
				Scriptable jsrow = Context.toObject(row, data.scope);
				data.scope.put("row", data.scope, jsrow); //$NON-NLS-1$
				
				for (int i=0;i<data.fields_used.length;i++)
				{
					Value val = row.getValue(data.fields_used[i]); 
					Scriptable jsarg = Context.toObject(val, data.scope);
					data.scope.put(val.getName(), data.scope, jsarg);
				}
				
				// Modification for Additional Script parsing
				try{
                    if (meta.getAddClasses()!=null)
                    {
    					for(int i=0;i<meta.getAddClasses().length;i++){
    						Object jsOut = Context.javaToJS(meta.getAddClasses()[i].getAddObject(), data.scope);
    						ScriptableObject.putProperty(data.scope, meta.getAddClasses()[i].getJSName(), jsOut);
    					}
                    }
				}catch(Exception e){
					logError(Messages.getString("ScriptValuesMod.Log.CouldNotAttachAdditionalScripts")+e.toString()); //$NON-NLS-1$
                    logError(Const.getStackTracker(e));
					setErrors(1);
					stopAll();
					return ERROR_TRANSFORMATION;
				}
				
				// Adding some default JavaScriptFunctions to the System
				try {
					Context.javaToJS(ScriptValuesAddedFunctions.class, data.scope);
					((ScriptableObject)data.scope).defineFunctionProperties(ScriptValuesAddedFunctions.jsFunctionList, ScriptValuesAddedFunctions.class, ScriptableObject.DONTENUM);
				} catch (Exception ex) {
					//System.out.println(ex.toString());
					logError(Messages.getString("ScriptValuesMod.Log.CouldNotAddDefaultFunctions")+ex.toString()); //$NON-NLS-1$
					setErrors(1);
					stopAll();
					return ERROR_TRANSFORMATION;
				};

				// Adding some Constants to the JavaScript
				try {
					
					data.scope.put("SKIP_TRANSFORMATION", data.scope, new Integer(SKIP_TRANSFORMATION));
					data.scope.put("ABORT_TRANSFORMATION", data.scope, new Integer(ABORT_TRANSFORMATION));
					data.scope.put("ERROR_TRANSFORMATION", data.scope, new Integer(ERROR_TRANSFORMATION));
					data.scope.put("CONTINUE_TRANSFORMATION", data.scope, new Integer(CONTINUE_TRANSFORMATION));

				} catch (Exception ex) {
					//System.out.println("Exception Adding the Constants " + ex.toString());
					logError(Messages.getString("ScriptValuesMod.Log.CouldNotAddDefaultConstants")+ex.toString());
					setErrors(1);
					stopAll();
					return ERROR_TRANSFORMATION;
				};

				try{
					// Checking for StartScript
					if(strStartScript != null && strStartScript.length()>0){
						Script startScript = data.cx.compileString(strStartScript, "trans_Start", 1, null);
						startScript.exec(data.cx, data.scope);
						if (log.isDetailed()) logDetailed(("Start Script found!"));
					}else{
						if (log.isDetailed()) logDetailed(("No starting Script found!"));
					}
				}catch(Exception es){
					//System.out.println("Exception processing StartScript " + es.toString());
					logError(Messages.getString("ScriptValuesMod.Log.ErrorProcessingStartScript")+es.toString());
					setErrors(1);
					stopAll();
					return ERROR_TRANSFORMATION;
					
				}
				// Now Compile our Script
				data.script=data.cx.compileString(strTransformScript, "script", 1, null);
			}
			catch(Exception e){
				logError(Messages.getString("ScriptValuesMod.Log.CouldNotCompileJavascript")+e.toString()); //$NON-NLS-1$
				setErrors(1);
				stopAll();
				return ERROR_TRANSFORMATION;
			}
		}
		
		// Filling the defined TranVars with the Values from the Row
		//for (int i=0;i<data.fields_used.length;i++){
		//	Value val = row.getValue(data.fields_used[i]);
		//	tranVars[i].setValue(val);
		//}
		
		try{
			
			try{
				Scriptable jsrow = Context.toObject(row, data.scope);
				data.scope.put("row", data.scope, jsrow); //$NON-NLS-1$
				
				for (int i=0;i<data.fields_used.length;i++)
				{
					Value val = row.getValue(data.fields_used[i]); 
					Scriptable jsarg = Context.toObject(val, data.scope);
					data.scope.put(val.getName(), data.scope, jsarg);
				}
			}
			catch(Exception e){
				logError(Messages.getString("ScriptValuesMod.Log.UnexpectedeError")+" : "+e.toString()); //$NON-NLS-1$ //$NON-NLS-2$				
				logError(Messages.getString("ScriptValuesMod.Log.ErrorStackTrace")+Const.CR+Const.getStackTracker(e)); //$NON-NLS-1$
				setErrors(1);
				stopAll();
				return ERROR_TRANSFORMATION;
			}				

			// Executing our Script
			data.script.exec(data.cx, data.scope);

			if(bFirstRun){
				bFirstRun=false;
				// Check if we had a Transformation Status
				Object tran_stat =  data.scope.get("trans_Status", data.scope);
				if(tran_stat!= ScriptableObject.NOT_FOUND){
					bWithTransStat = true;
					if (log.isDetailed()) logDetailed(("tran_Status found. Checking transformation status while script execution.")); //$NON-NLS-1$ //$NON-NLS-2$
				}else{
					if (log.isDetailed()) logDetailed(("No tran_Status found. Transformation status checking not available.")); //$NON-NLS-1$ //$NON-NLS-2$
					bWithTransStat = false;
				}
			}
			
			if(bWithTransStat){
				iTranStat = (int)Context.toNumber(data.scope.get("trans_Status", data.scope));
			}else{
				iTranStat = CONTINUE_TRANSFORMATION;
			}
			
			if(iTranStat==CONTINUE_TRANSFORMATION){
				StringBuffer message = new StringBuffer();
				for (int i=0;i<meta.getName().length;i++){
						
					//System.out.println("MetaName" + meta.getName()[i]);
					Object result = data.scope.get(meta.getName()[i], data.scope);
					Value res = new Value();
					if (!getValueFromJScript(result, i, res)){
						logError(message.toString());
						setErrors(1);
						stopAll();
						return ERROR_TRANSFORMATION;
					}
					row.addValue(res);  // This means the row in rowset gets an extra field!
				}
			}
		}catch(JavaScriptException jse){
			if (getStepMeta().isDoingErrorHandling())
	        {
	             putError(row, 1, jse.getMessage(), null, "JSMOD001");
	             row.setIgnore(); 
	        }
			else
			{
		        logError(Messages.getString("ScriptValuesMod.Log.ErrorStackTrace")+Const.CR+Const.getStackTracker(jse)); //$NON-NLS-1$
	            setErrors(1);
				stopAll();
				return ERROR_TRANSFORMATION;
			}
			 return SKIP_TRANSFORMATION;
 
			
		}catch(Exception e)	{
			if (getStepMeta().isDoingErrorHandling())
	        {
	             putError(row, 1, e.getMessage(), null, "JSMOD001");
	             row.setIgnore();
	        }
			else
			{
				logError(Messages.getString("ScriptValuesMod.Log.JavascriptError")+e.toString()); //$NON-NLS-1$
	            logError(Messages.getString("ScriptValuesMod.Log.ErrorStackTrace")+Const.CR+Const.getStackTracker(e)); //$NON-NLS-1$
				setErrors(1);
				stopAll();
				return ERROR_TRANSFORMATION;
			}
			return SKIP_TRANSFORMATION;
		}
		return iTranStat;
	}
	
	
	public boolean getValueFromJScript(Object result, int i, Value res){
		
		if (meta.getName()[i]!=null && meta.getName()[i].length()>0){
			res.setName(meta.getRename()[i]);
			res.setType(meta.getType()[i]);
			
			try{
				if(result!=null){
					String classType = result.getClass().getName();
						switch(meta.getType()[i]){
							case Value.VALUE_TYPE_NUMBER:
								if (classType.equalsIgnoreCase("org.mozilla.javascript.Undefined")){
									res.setNull();
								}else if (classType.equalsIgnoreCase("org.mozilla.javascript.NativeJavaObject")){
									// Is it a java Value class ?
									Value v = (Value)Context.toType(result, Value.class);
									res.setValue( v.getNumber() );
								}else{
									Number nb = (Number)result;
									res.setValue( nb.doubleValue() ); 
								}
								break;
							case Value.VALUE_TYPE_INTEGER:
								if (classType.equalsIgnoreCase("java.lang.Byte")){
									res.setValue( ((java.lang.Byte)result).longValue() );
								}else if (classType.equalsIgnoreCase("java.lang.Short")){
									res.setValue( ((Short)result).longValue() );
								}else if (classType.equalsIgnoreCase("java.lang.Integer")){
									res.setValue( ((Integer)result).longValue() );
								}else if (classType.equalsIgnoreCase("java.lang.Long")){
									res.setValue( ((Long)result).longValue() );
                                }else if (classType.equalsIgnoreCase("java.lang.Double")){
                                    res.setValue( ((Double)result).longValue() );
								}else if (classType.equalsIgnoreCase("java.lang.String")){
									res.setValue( (new Long((String)result)).longValue() );									
								}else if (classType.equalsIgnoreCase("org.mozilla.javascript.Undefined")){
									res.setNull();
								}else if (classType.equalsIgnoreCase("org.mozilla.javascript.NativeJavaObject")){
									// Is it a java Value class ?
									Value v = (Value)Context.toType(result, Value.class);
									res.setValue( v.getInteger() );
								}else{
									res.setValue( (long)((Long)result).longValue()); 
								}
								break;
							case Value.VALUE_TYPE_STRING:  
								if (classType.equalsIgnoreCase("org.mozilla.javascript.NativeJavaObject") ||  //$NON-NLS-1$
										classType.equalsIgnoreCase("org.mozilla.javascript.Undefined")){
										// Is it a java Value class ?
									try{
										Value v = (Value)Context.toType(result, Value.class);
										res.setValue( v.getString() );
									}catch(Exception ev){
										// A String perhaps?
										String s = (String)Context.toType(result, String.class);
										res.setValue( s );
									}
								}else{
									res.setValue( ((String)result) ); 
								}
								break;
							case Value.VALUE_TYPE_DATE:
								double dbl=0;
								if (classType.equalsIgnoreCase("org.mozilla.javascript.Undefined"))	{
									res.setNull();
								}else{
									if (classType.equalsIgnoreCase("org.mozilla.javascript.NativeDate")){
										dbl = Context.toNumber(result);
									}else if (classType.equalsIgnoreCase("org.mozilla.javascript.NativeJavaObject") || classType.equalsIgnoreCase("java.util.Date")){
										// Is it a java Date() class ?
										try	{
											Date dat = (Date)Context.toType(result, java.util.Date.class);
											dbl = dat.getTime();
										}catch(Exception e){
											Value v = (Value)Context.toType(result, Value.class);
											Date dat = v.getDate();
											if (dat!=null) dbl = dat.getTime();
											else res.setNull();
										}
									}else{
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
							case Value.VALUE_TYPE_BIGNUMBER:
								if (classType.equalsIgnoreCase("org.mozilla.javascript.Undefined")){
									res.setNull();
								}else if (classType.equalsIgnoreCase("org.mozilla.javascript.NativeJavaObject")){
									// Is it a BigDecimal class ?
									try {
										BigDecimal bd = (BigDecimal)Context.toType(result, BigDecimal.class);
										res.setValue( bd );
									}catch(Exception e){
										Value v = (Value)Context.toType(result, Value.class);
										if (!v.isNull()) res.setValue( v.getBigNumber() );
										else res.setNull();
									}
								}else if (classType.equalsIgnoreCase("java.lang.Byte")){
									BigDecimal bd = new BigDecimal( ((java.lang.Byte)result).longValue() );
									res.setValue( bd );
								}else if (classType.equalsIgnoreCase("java.lang.Short")){
									BigDecimal bd = new BigDecimal( ((Short)result).longValue() );
									res.setValue( bd );
								}else if (classType.equalsIgnoreCase("java.lang.Integer")){
									BigDecimal bd = new BigDecimal( ((Integer)result).longValue() );
									res.setValue( bd );
								}else if (classType.equalsIgnoreCase("java.lang.Long")){
									BigDecimal bd = new BigDecimal( ((Long)result).longValue() );
									res.setValue( bd );
                                }else if (classType.equalsIgnoreCase("java.lang.Double")){
                                	BigDecimal bd = new BigDecimal( ((Double)result).longValue() );
                                	res.setValue( bd );
								}else if (classType.equalsIgnoreCase("java.lang.String")){
									BigDecimal bd = new BigDecimal( (new Long((String)result)).longValue() );
									res.setValue( bd );
								}else {
									throw new RuntimeException("JavaScript conversion to BigNumber not implemented for " + classType.toString());
								}
								break;
								
							default: throw new RuntimeException("JavaScript conversion not implemented for " + res.getTypeDesc());

					}
				}else{
					res.setNull();
				}
			}catch(Exception e){
				logError(Messages.getString("ScriptValuesMod.Log.JavascriptError")+e.toString()); //$NON-NLS-1$
	            logError(Messages.getString("ScriptValuesMod.Log.ErrorStackTrace")+Const.CR+Const.getStackTracker(e)); //$NON-NLS-1$
	            setErrors(getErrors()+1);				
				return false;
			}
			return true;
		}
		return false;
	}
	
	
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException{
		
		meta=(ScriptValuesMetaMod)smi;
		data=(ScriptValuesDataMod)sdi;
		
		boolean sendToErrorRow=false;
		String errorMessage = null;
		
		Row r=getRow();       // Get row from input rowset & set row busy!

		if (r==null){
			
			//Modification for Additional End Function
			try{
				// Checking for EndScript
				if(strEndScript != null && strEndScript.length()>0){
					Script endScript = data.cx.compileString(strEndScript, "trans_End", 1, null);
					endScript.exec(data.cx, data.scope);
					if (log.isDetailed()) logDetailed(("End Script found!"));
				}else{
					if (log.isDetailed()) logDetailed(("No end Script found!"));
				}
			}catch(Exception e){
				

					logError(Messages.getString("ScriptValuesMod.Log.UnexpectedeError")+" : "+e.toString()); //$NON-NLS-1$ //$NON-NLS-2$				
					logError(Messages.getString("ScriptValuesMod.Log.ErrorStackTrace")+Const.CR+Const.getStackTracker(e)); //$NON-NLS-1$
					setErrors(1);
					stopAll();

			}

			if (data.cx!=null) Context.exit();
			setOutputDone();
			return false;
		}
		
		// Getting the Row, with the Transformation Status
        switch (addValues(r)) {
    		case CONTINUE_TRANSFORMATION:
    			bRC = true;
    			putRow(r);
            break;
    		case SKIP_TRANSFORMATION:
    			bRC = true;
        	break;
        	case ABORT_TRANSFORMATION:
        		if (data.cx!=null) Context.exit();
        		stopAll();
    			setOutputDone();
    			bRC = false;
    		break;
        	case ERROR_TRANSFORMATION:
        		if (data.cx!=null) Context.exit();
    			setErrors(1);
    			stopAll();
    			bRC = false;
            break;
        }

		if (checkFeedback(linesRead)) logBasic(Messages.getString("ScriptValuesMod.Log.LineNumber")+linesRead); //$NON-NLS-1$
		return bRC;
	}

	
	public boolean init(StepMetaInterface smi, StepDataInterface sdi){
		meta=(ScriptValuesMetaMod)smi;
		data=(ScriptValuesDataMod)sdi;

		if (super.init(smi, sdi)){
		    
			// Add init code here.
			// Get the actual Scripts from our MetaData
			jsScripts = meta.getJSScripts();
			for(int j=0;j<jsScripts.length;j++){
				switch(jsScripts[j].getScriptType()){
				case ScriptValuesScript.TRANSFORM_SCRIPT:
						strTransformScript =jsScripts[j].getScript();
						break;
				case ScriptValuesScript.START_SCRIPT:
						strStartScript =jsScripts[j].getScript();
						break;
				case ScriptValuesScript.END_SCRIPT:
						strEndScript = jsScripts[j].getScript();
						break;
				}
			}
			return true;
		}
		return false;
	}


	//
	// Run is were the action happens!
	public void run()
	{
		try{
			logBasic(Messages.getString("ScriptValuesMod.Log.StartingToRun")); //$NON-NLS-1$
			while (processRow(meta, data) && !isStopped());
		}catch(Exception e){
			try{
				if (data.cx!=null) Context.exit();
			}catch(Exception er){};
			logError(Messages.getString("ScriptValuesMod.Log.UnexpectedeError")+" : "+e.toString()); //$NON-NLS-1$ //$NON-NLS-2$
            logError(Messages.getString("ScriptValuesMod.Log.ErrorStackTrace")+Const.CR+Const.getStackTracker(e)); //$NON-NLS-1$
			setErrors(1);
			stopAll();
		}finally{
			dispose(meta, data);
			logSummary();
			markStop();
		}
	}
}