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
 
package org.pentaho.di.trans.steps.scriptvalues_mod;

import java.math.BigDecimal;
import java.util.Date;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.exception.KettleValueException;
import be.ibridge.kettle.core.value.Value;

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
	
	// public static Row insertRow;
	
	public static Script script;

	
	public ScriptValuesMod(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans){
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}
	
	private void determineUsedFields(RowMetaInterface row){
		int nr=0;
		// Count the occurences of the values.
		// Perhaps we find values in comments, but we take no risk!
		for (int i=0;i<row.size();i++){
			String valname = row.getValueMeta(i).getName().toUpperCase();
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
			String valname = row.getValueMeta(i).getName();
			if (strTransformScript.indexOf(valname)>=0){
				if (log.isDetailed()) logDetailed(Messages.getString("ScriptValuesMod.Log.UsedValueName",String.valueOf(i),valname)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				data.fields_used[nr]=i;
				nr++;
			}
		}
		
		if (log.isDetailed()) logDetailed(Messages.getString("ScriptValuesMod.Log.UsingValuesFromInputStream",String.valueOf(data.fields_used.length))); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	private synchronized Object[] addValues(RowMetaInterface rowMeta, Object[] row) throws KettleValueException
    {
        if (first)
        {
            first = false;

            // What is the output row looking like?
            data.outputRowMeta = (RowMetaInterface)getInputRowMeta().clone();
            meta.getFields(data.outputRowMeta, getStepname(), null);

            // Determine the indexes of the fields used!
            determineUsedFields(rowMeta);

            data.cx = Context.enter();
            data.cx.setOptimizationLevel(9);
            data.scope = data.cx.initStandardObjects(null, true);

            bFirstRun = true;

            Scriptable jsvalue = Context.toObject(this, data.scope);
            data.scope.put("_step_", data.scope, jsvalue); //$NON-NLS-1$

            // Adding the existing Scripts to the Context
            for (int i = 0; i < meta.getNumberOfJSScripts(); i++)
            {
                Scriptable jsR = Context.toObject(jsScripts[i].getScript(), data.scope);
                data.scope.put(jsScripts[i].getScriptName(), data.scope, jsR);
            }

            // Adding the Name of the Transformation to the Context
            data.scope.put("_TransformationName_", data.scope, new String(this.getName()));

            try
            {
                // add these now (they will be readded later) to make compilation succeed
                Scriptable jsrow = Context.toObject(row, data.scope);
                data.scope.put("row", data.scope, jsrow); //$NON-NLS-1$

                for (int i = 0; i < data.fields_used.length; i++)
                {
                    /*
                     * INCOMPATIBLE...
                     * 
                    ValueMetaInterface valueMeta = rowMeta.getValueMeta(data.fields_used[i]);
                    Object valueData = row[data.fields_used[i]];
                    // Value val = valueMeta.createOriginalValue(valueData); // TODO: make backward compatibility mode

                    Scriptable jsarg = Context.toObject(valueData, data.scope);
                    data.scope.put(valueMeta.getName(), data.scope, jsarg);
                     */
                    
                    ValueMetaInterface valueMeta = rowMeta.getValueMeta(data.fields_used[i]);
                    Object valueData = row[data.fields_used[i]];
                    Value val = valueMeta.createOriginalValue(valueData);

                    Scriptable jsarg = Context.toObject(val, data.scope);
                    data.scope.put(val.getName(), data.scope, jsarg);
                }

                // Modification for Additional Script parsing
                try
                {
                    if (meta.getAddClasses() != null)
                    {
                        for (int i = 0; i < meta.getAddClasses().length; i++)
                        {
                            Object jsOut = Context.javaToJS(meta.getAddClasses()[i].getAddObject(), data.scope);
                            ScriptableObject.putProperty(data.scope, meta.getAddClasses()[i].getJSName(), jsOut);
                        }
                    }
                }
                catch (Exception e)
                {
                    throw new KettleValueException(Messages.getString("ScriptValuesMod.Log.CouldNotAttachAdditionalScripts"), e); //$NON-NLS-1$
                }

                // Adding some default JavaScriptFunctions to the System
                try
                {
                    Context.javaToJS(ScriptValuesAddedFunctions.class, data.scope);
                    ((ScriptableObject) data.scope).defineFunctionProperties(ScriptValuesAddedFunctions.jsFunctionList,
                            ScriptValuesAddedFunctions.class, ScriptableObject.DONTENUM);
                }
                catch (Exception ex)
                {
                    // System.out.println(ex.toString());
                    throw new KettleValueException(Messages.getString("ScriptValuesMod.Log.CouldNotAddDefaultFunctions"), ex); //$NON-NLS-1$
                }
                ;

                // Adding some Constants to the JavaScript
                try
                {

                    data.scope.put("SKIP_TRANSFORMATION", data.scope, new Integer(SKIP_TRANSFORMATION));
                    data.scope.put("ABORT_TRANSFORMATION", data.scope, new Integer(ABORT_TRANSFORMATION));
                    data.scope.put("ERROR_TRANSFORMATION", data.scope, new Integer(ERROR_TRANSFORMATION));
                    data.scope.put("CONTINUE_TRANSFORMATION", data.scope, new Integer(CONTINUE_TRANSFORMATION));

                }
                catch (Exception ex)
                {
                    // System.out.println("Exception Adding the Constants " + ex.toString());
                    throw new KettleValueException(Messages.getString("ScriptValuesMod.Log.CouldNotAddDefaultConstants"), ex);
                }
                ;

                try
                {
                    // Checking for StartScript
                    if (strStartScript != null && strStartScript.length() > 0)
                    {
                        Script startScript = data.cx.compileString(strStartScript, "trans_Start", 1, null);
                        startScript.exec(data.cx, data.scope);
                        if (log.isDetailed()) logDetailed(("Start Script found!"));
                    }
                    else
                    {
                        if (log.isDetailed()) logDetailed(("No starting Script found!"));
                    }
                }
                catch (Exception es)
                {
                    // System.out.println("Exception processing StartScript " + es.toString());
                    throw new KettleValueException(Messages.getString("ScriptValuesMod.Log.ErrorProcessingStartScript"), es);

                }
                // Now Compile our Script
                data.script = data.cx.compileString(strTransformScript, "script", 1, null);
            }
            catch (Exception e)
            {
                throw new KettleValueException(Messages.getString("ScriptValuesMod.Log.CouldNotCompileJavascript"), e);
            }
        }

        // Filling the defined TranVars with the Values from the Row
        // for (int i=0;i<data.fields_used.length;i++){
        // Value val = row.getValue(data.fields_used[i]);
        // tranVars[i].setValue(val);
        // }
        
        Object[] outputRow = new Object[data.outputRowMeta.size()];
        
        // First copy the previous rows...
        for (int i=0;i<rowMeta.size();i++)
        {
            outputRow[i]=row[i];
        }

        // Keep an index...
        int outputIndex = rowMeta.size();
        
        try
        {

            try
            {
                Scriptable jsrow = Context.toObject(row, data.scope);
                data.scope.put("row", data.scope, jsrow); //$NON-NLS-1$

                for (int i = 0; i < data.fields_used.length; i++)
                {
                    /* 
                     * IMCOMPATIBLE
                     * 
                    ValueMetaInterface valueMeta = rowMeta.getValueMeta(data.fields_used[i]);
                    Object valueData = row[data.fields_used[i]];
                    
                    Scriptable jsarg = Context.toObject(valueData, data.scope);
                    data.scope.put(valueMeta.getName(), data.scope, jsarg);
                    */
                    
                    ValueMetaInterface valueMeta = rowMeta.getValueMeta(data.fields_used[i]);
                    Object valueData = row[data.fields_used[i]];
                    Value val = valueMeta.createOriginalValue(valueData);

                    Scriptable jsarg = Context.toObject(val, data.scope);
                    data.scope.put(val.getName(), data.scope, jsarg);
                }
            }
            catch (Exception e)
            {
                throw new KettleValueException(Messages.getString("ScriptValuesMod.Log.UnexpectedeError"), e); //$NON-NLS-1$ //$NON-NLS-2$				
            }

            // Executing our Script
            data.script.exec(data.cx, data.scope);

            if (bFirstRun)
            {
                bFirstRun = false;
                // Check if we had a Transformation Status
                Object tran_stat = data.scope.get("trans_Status", data.scope);
                if (tran_stat != ScriptableObject.NOT_FOUND)
                {
                    bWithTransStat = true;
                    if (log.isDetailed()) logDetailed(("tran_Status found. Checking transformation status while script execution.")); //$NON-NLS-1$ //$NON-NLS-2$
                }
                else
                {
                    if (log.isDetailed()) logDetailed(("No tran_Status found. Transformation status checking not available.")); //$NON-NLS-1$ //$NON-NLS-2$
                    bWithTransStat = false;
                }
            }

            if (bWithTransStat)
            {
                iTranStat = (int) Context.toNumber(data.scope.get("trans_Status", data.scope));
            }
            else
            {
                iTranStat = CONTINUE_TRANSFORMATION;
            }

            if (iTranStat == CONTINUE_TRANSFORMATION)
            {
                bRC=true;
                for (int i = 0; i < meta.getName().length; i++)
                {
                    Object result = data.scope.get(meta.getName()[i], data.scope);
                    outputRow[outputIndex]= getValueFromJScript(result, i);
                    outputIndex++;
                }
            }
            else
            {
                switch (iTranStat) 
                {
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
                
                // TODO: kick this "ERROR handling" junk out now that we have solid error handling in place.
                //
            }
        }
        catch (Exception e)
        {
            throw new KettleValueException(Messages.getString("ScriptValuesMod.Log.JavascriptError"), e); //$NON-NLS-1$
        }
        return outputRow;
    }
	
	
	public Object getValueFromJScript(Object result, int i) throws KettleValueException
    {
        if (meta.getName()[i] != null && meta.getName()[i].length() > 0)
        {
            // res.setName(meta.getRename()[i]);
            // res.setType(meta.getType()[i]);

            try
            {
                if (result != null)
                {
                    String classType = result.getClass().getName();
                    switch (meta.getType()[i])
                    {
                    case ValueMetaInterface.TYPE_NUMBER:
                        if (classType.equalsIgnoreCase("org.mozilla.javascript.Undefined"))
                        {
                            return null;
                        }
                        else
                            if (classType.equalsIgnoreCase("org.mozilla.javascript.NativeJavaObject"))
                            {
                                // Is it a java Value class ?
                                // Value v = (Value) Context.toType(result, Value.class);
                                // res.setValue(v.getNumber());
                                String string = Context.toString(result);
                                return new Double( Double.parseDouble(string) );
                            }
                            else
                            {
                                Number nb = (Number) result;
                                return new Double( nb.doubleValue() );
                            }

                    case ValueMetaInterface.TYPE_INTEGER:
                        if (classType.equalsIgnoreCase("java.lang.Byte"))
                        {
                            return new Long( ((java.lang.Byte) result).longValue() );
                        }
                        else
                            if (classType.equalsIgnoreCase("java.lang.Short"))
                            {
                                return new Long( ((Short) result).longValue());
                            }
                            else
                                if (classType.equalsIgnoreCase("java.lang.Integer"))
                                {
                                    return new Long( ((Integer) result).longValue());
                                }
                                else
                                    if (classType.equalsIgnoreCase("java.lang.Long"))
                                    {
                                        return new Long( ((Long) result).longValue());
                                    }
                                    else
                                        if (classType.equalsIgnoreCase("java.lang.Double"))
                                        {
                                            return new Long( ((Double) result).longValue());
                                        }
                                        else
                                            if (classType.equalsIgnoreCase("java.lang.String"))
                                            {
                                                return new Long( (new Long((String) result)).longValue());
                                            }
                                            else
                                                if (classType.equalsIgnoreCase("org.mozilla.javascript.Undefined"))
                                                {
                                                    return null;
                                                }
                                                else
                                                    if (classType.equalsIgnoreCase("org.mozilla.javascript.NativeJavaObject"))
                                                    {
                                                        // Is it a java Value class ?
                                                        // Value v = (Value) Context.toType(result, Value.class);
                                                        // res.setValue(v.getInteger());

                                                        String string = Context.toString(result);
                                                        return new Long( Long.parseLong(string) );
                                                    }
                                                    else
                                                    {
                                                        return new Long( (long) ((Long) result).longValue() );
                                                    }

                    case ValueMetaInterface.TYPE_STRING:
                        if (classType.equalsIgnoreCase("org.mozilla.javascript.NativeJavaObject") || //$NON-NLS-1$
                                classType.equalsIgnoreCase("org.mozilla.javascript.Undefined"))
                        {
                            // Is it a java Value class ?
                            try
                            {
                                // Value v = (Value) Context.toType(result, Value.class);
                                // res.setValue(v.getString());
                                
                                return Context.toString(result);
                            }
                            catch (Exception ev)
                            {
                                // A String perhaps?
                                String string = (String) Context.toType(result, String.class);
                                return string;
                            }
                        }
                        else
                        {
                            // A String perhaps?
                            String string = (String) Context.toType(result, String.class);
                            return string;
                        }

                    case ValueMetaInterface.TYPE_DATE:
                        double dbl = 0;
                        if (classType.equalsIgnoreCase("org.mozilla.javascript.Undefined"))
                        {
                            return null;
                        }
                        else
                        {
                            if (classType.equalsIgnoreCase("org.mozilla.javascript.NativeDate"))
                            {
                                dbl = Context.toNumber(result);
                            }
                            else
                                if (classType.equalsIgnoreCase("org.mozilla.javascript.NativeJavaObject")
                                        || classType.equalsIgnoreCase("java.util.Date"))
                                {
                                    // Is it a java Date() class ?
                                    try
                                    {
                                        Date dat = (Date) Context.toType(result, java.util.Date.class);
                                        dbl = dat.getTime();
                                    }
                                    catch (Exception e)
                                    {
                                        throw new KettleValueException("Can't convert a string to a date");
                                        /*
                                         * 
                                         String string = (String) Context.toType(result, String.class);
                                        
                                        // Value v = (Value) Context.toType(result, Value.class);
                                        Date dat = v.getDate();
                                        if (dat != null)
                                            dbl = dat.getTime();
                                        else
                                            res.setNull();
                                        */
                                    }
                                }
                                else if (classType.equalsIgnoreCase("java.lang.Double"))
                                {
                                    dbl = ((Double) result).doubleValue();
                                }
                                else
                                {
                                    String string = (String) Context.toType(result, String.class);
                                    dbl = Double.parseDouble(string);
                                }
                            long lng = Math.round(dbl);
                            Date dat = new Date(lng);
                            return dat;
                        }

                    case ValueMetaInterface.TYPE_BOOLEAN:
                        return (Boolean)result;

                    case ValueMetaInterface.TYPE_BIGNUMBER:
                        if (classType.equalsIgnoreCase("org.mozilla.javascript.Undefined"))
                        {
                            return null;
                        }
                        else
                            if (classType.equalsIgnoreCase("org.mozilla.javascript.NativeJavaObject"))
                            {
                                // Is it a BigDecimal class ?
                                try
                                {
                                    BigDecimal bd = (BigDecimal) Context.toType(result, BigDecimal.class);
                                    return bd;
                                }
                                catch (Exception e)
                                {
                                    String string = (String) Context.toType(result, String.class);
                                    return new BigDecimal(string);
                                    /*
                                        Value v = (Value) Context.toType(result, Value.class);
                                        if (!v.isNull())
                                            res.setValue(v.getBigNumber());
                                        else
                                            res.setNull();
                                    */
                                }
                            }
                            else
                                if (classType.equalsIgnoreCase("java.lang.Byte"))
                                {
                                    return new BigDecimal(((java.lang.Byte) result).longValue());
                                }
                                else
                                    if (classType.equalsIgnoreCase("java.lang.Short"))
                                    {
                                        return new BigDecimal(((Short) result).longValue());
                                    }
                                    else
                                        if (classType.equalsIgnoreCase("java.lang.Integer"))
                                        {
                                            return new BigDecimal(((Integer) result).longValue());
                                        }
                                        else
                                            if (classType.equalsIgnoreCase("java.lang.Long"))
                                            {
                                                return new BigDecimal(((Long) result).longValue());
                                            }
                                            else
                                                if (classType.equalsIgnoreCase("java.lang.Double"))
                                                {
                                                    return new BigDecimal(((Double) result).longValue());
                                                }
                                                else
                                                    if (classType.equalsIgnoreCase("java.lang.String"))
                                                    {
                                                        return new BigDecimal((new Long((String) result)).longValue());
                                                    }
                                                    else
                                                    {
                                                        throw new RuntimeException("JavaScript conversion to BigNumber not implemented for "+classType.toString());
                                                    }

                    default:
                        throw new RuntimeException("JavaScript conversion not implemented for type" + meta.getType()[i]);

                    }
                }
                else
                {
                    return null;
                }
            }
            catch (Exception e)
            {
                throw new KettleValueException(Messages.getString("ScriptValuesMod.Log.JavascriptError"), e); //$NON-NLS-1$
            }
        }
        else
        {
            throw new KettleValueException("No name was specified for result value #"+(i+1));
        }
    }
	
	
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException{
		
		meta=(ScriptValuesMetaMod)smi;
		data=(ScriptValuesDataMod)sdi;
		
		Object[] r=getRow();       // Get row from input rowset & set row busy!
		if (r==null)
        {
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
        try
        {
            Object[] outputRow = addValues(getInputRowMeta(), r);
            putRow(data.outputRowMeta, outputRow);
        }
        catch(KettleValueException e)
        {
            if (getStepMeta().isDoingErrorHandling())
            {
                putError(getInputRowMeta(), r, 1, e.getMessage(), null, "SCR-001");
            }
            else
            {
                throw(e);
            }
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