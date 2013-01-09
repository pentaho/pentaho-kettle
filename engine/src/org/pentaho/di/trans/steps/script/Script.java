/*******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.trans.steps.script;

import java.math.BigDecimal;
import java.util.Date;

import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptException;

import org.pentaho.di.compatibility.Value;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

/**
 * Executes a JavaScript on the values in the input stream. Selected calculated
 * values can then be put on the output stream.
 * 
 * @author Matt
 * @since 5-April-2003
 */
public class Script extends BaseStep implements StepInterface {
	private static Class<?>			PKG						= ScriptMeta.class;		// for i18n purposes, needed by Translator2!! 

	private ScriptMeta				meta;

	private ScriptData				data;

	public final static int			SKIP_TRANSFORMATION		= 1;

	public final static int			ABORT_TRANSFORMATION	= -1;

	public final static int			ERROR_TRANSFORMATION	= -2;

	public final static int			CONTINUE_TRANSFORMATION	= 0;

	private boolean					bWithTransStat			= false;

	private boolean					bRC						= false;

	private int						iTranStat				= CONTINUE_TRANSFORMATION;

	private boolean					bFirstRun				= false;

	private ScriptValuesScript[]	jsScripts;

	private String					strTransformScript		= "";

	private String					strStartScript			= "";

	private String					strEndScript			= "";

	// public static Row insertRow;

	// public String script;//TODO AKRETION should be compiled script actually

	public Script(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans) {
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}

	private void determineUsedFields(RowMetaInterface row) {
		int nr = 0;
		// Count the occurrences of the values.
		// Perhaps we find values in comments, but we take no risk!
		//
		for (int i = 0; i < row.size(); i++) {
			String valname = row.getValueMeta(i).getName().toUpperCase();
			if (strTransformScript.toUpperCase().indexOf(valname) >= 0) {
				nr++;
			}
		}

		// Allocate fields_used
		data.fields_used = new int[nr];
		data.values_used = new Value[nr];

		nr = 0;
		// Count the occurrences of the values.
		// Perhaps we find values in comments, but we take no risk!
		//
		for (int i = 0; i < row.size(); i++) {
			// Values are case-insensitive in JavaScript.
			//
			String valname = row.getValueMeta(i).getName();
			if (strTransformScript.indexOf(valname) >= 0) {
				if (log.isDetailed())
					logDetailed(BaseMessages.getString(PKG, "Script.Log.UsedValueName", String.valueOf(i), valname)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				data.fields_used[nr] = i;
				nr++;
			}
		}

		if (log.isDetailed())
			logDetailed(BaseMessages.getString(PKG, "Script.Log.UsingValuesFromInputStream", String.valueOf(data.fields_used.length))); //$NON-NLS-1$ //$NON-NLS-2$
	}

	private boolean addValues(RowMetaInterface rowMeta, Object[] row) throws KettleException {
		if (first) {
			first = false;

			// What is the output row looking like?
			//
			data.outputRowMeta = getInputRowMeta().clone();
			meta.getFields(data.outputRowMeta, getStepname(), null, null, this);

			// Determine the indexes of the fields used!
			//
			determineUsedFields(rowMeta);

			// Get the indexes of the replaced fields...
			//
			data.replaceIndex = new int[meta.getFieldname().length];
			for (int i = 0; i < meta.getFieldname().length; i++) {
				if (meta.getReplace()[i]) {
					data.replaceIndex[i] = rowMeta.indexOfValue(meta.getFieldname()[i]);
					if (data.replaceIndex[i] < 0) {
						if (Const.isEmpty(meta.getFieldname()[i])) {
							throw new KettleStepException(BaseMessages.getString(PKG, "ScriptValuesMetaMod.Exception.FieldToReplaceNotFound", meta.getFieldname()[i]));
						}
						data.replaceIndex[i] = rowMeta.indexOfValue(meta.getRename()[i]);
						if (data.replaceIndex[i] < 0) {
							throw new KettleStepException(BaseMessages.getString(PKG, "ScriptValuesMetaMod.Exception.FieldToReplaceNotFound", meta.getRename()[i]));
						}
					}
				} else {
					data.replaceIndex[i] = -1;
				}
			}

			data.cx = ScriptMeta.createNewScriptEngine(getStepname());
			data.scope = data.cx.getBindings(ScriptContext.ENGINE_SCOPE);

			bFirstRun = true;

			data.scope.put("_step_", this); //$NON-NLS-1$

			// Adding the existing Scripts to the Context
			//
			for (int i = 0; i < meta.getNumberOfJSScripts(); i++) {
				data.scope.put(jsScripts[i].getScriptName(), jsScripts[i].getScript());
			}

			// Adding the Name of the Transformation to the Context
			//
			data.scope.put("_TransformationName_", this.getStepname());

			try {
				// add these now (they will be re-added later) to make
				// compilation succeed
				//

				// Add the old style row object for compatibility reasons...
				//
				data.scope.put("row", row); //$NON-NLS-1$

				// Add the used fields...
				//
				for (int i = 0; i < data.fields_used.length; i++) {
					ValueMetaInterface valueMeta = rowMeta.getValueMeta(data.fields_used[i]);
					Object valueData = row[data.fields_used[i]];

					Object normalStorageValueData = valueMeta.convertToNormalStorageType(valueData);
					data.scope.put(valueMeta.getName(), normalStorageValueData);
				}

				// also add the meta information for the whole row
				//
				data.scope.put("rowMeta", rowMeta); //$NON-NLS-1$

				// Modification for Additional Script parsing
				//
				try {
					if (meta.getAddClasses() != null) {
						for (int i = 0; i < meta.getAddClasses().length; i++) {
							// TODO AKRETION ensure it works
							data.scope.put(meta.getAddClasses()[i].getJSName(), meta.getAddClasses()[i].getAddObject());
							// Object jsOut =
							// Context.javaToJS(meta.getAddClasses()[i].getAddObject(),
							// data.scope);
							// ScriptableObject.putProperty(data.scope,
							// meta.getAddClasses()[i].getJSName(), jsOut);
						}
					}
				} catch (Exception e) {
					throw new KettleValueException(BaseMessages.getString(PKG, "Script.Log.CouldNotAttachAdditionalScripts"), e); //$NON-NLS-1$
				}

				// Adding some default JavaScriptFunctions to the System
				// TODO AKRETION not implemented yet
				// try {
				// Context.javaToJS(ScriptValuesAddedFunctions.class,
				// data.scope);
				// ((ScriptableObject)
				// data.scope).defineFunctionProperties(ScriptValuesAddedFunctions.jsFunctionList,
				// ScriptValuesAddedFunctions.class, ScriptableObject.DONTENUM);
				// } catch (Exception ex) {
				// // System.out.println(ex.toString());
				// throw new KettleValueException(BaseMessages.getString(PKG, "Script.Log.CouldNotAddDefaultFunctions"), ex); //$NON-NLS-1$
				// }
				// ;

				// Adding some Constants to the JavaScript
				try {

					data.scope.put("SKIP_TRANSFORMATION", Integer.valueOf(SKIP_TRANSFORMATION));
					data.scope.put("ABORT_TRANSFORMATION", Integer.valueOf(ABORT_TRANSFORMATION));
					data.scope.put("ERROR_TRANSFORMATION", Integer.valueOf(ERROR_TRANSFORMATION));
					data.scope.put("CONTINUE_TRANSFORMATION", Integer.valueOf(CONTINUE_TRANSFORMATION));

				} catch (Exception ex) {
					// System.out.println("Exception Adding the Constants " +
					// ex.toString());
					throw new KettleValueException(BaseMessages.getString(PKG, "Script.Log.CouldNotAddDefaultConstants"), ex);
				}
				;

				try {
					// Checking for StartScript
					if (strStartScript != null && strStartScript.length() > 0) {
						CompiledScript startScript = ((Compilable) data.cx).compile(strStartScript);
						startScript.eval(data.scope);
						if (log.isDetailed())
							logDetailed(("Start Script found!"));
					} else {
						if (log.isDetailed())
							logDetailed(("No starting Script found!"));
					}
				} catch (Exception es) {
					// System.out.println("Exception processing StartScript " +
					// es.toString());
					throw new KettleValueException(BaseMessages.getString(PKG, "Script.Log.ErrorProcessingStartScript"), es);

				}
				// Now Compile our Script
				// alternatively you could also support non compilable JSR223
				// languages, see how we were doing before:
				// http://github.com/rvalyi/jripple/blob/e6190fd89014a49b0faffae68c75762be124d899/src/org/pentaho/di/trans/steps/scriptvalues_mod/ScriptValuesMod.java
				
				data.script = ((Compilable) data.cx).compile(strTransformScript);
			} catch (Exception e) {
				throw new KettleValueException(BaseMessages.getString(PKG, "Script.Log.CouldNotCompileJavascript"), e);
			}
		}

		// Filling the defined TranVars with the Values from the Row
		//
		Object[] outputRow = RowDataUtil.resizeArray(row, data.outputRowMeta.size());

		// Keep an index...
		int outputIndex = rowMeta.size();

		try {
			try {
				data.scope.put("row", row); //$NON-NLS-1$

				for (int i = 0; i < data.fields_used.length; i++) {
					ValueMetaInterface valueMeta = rowMeta.getValueMeta(data.fields_used[i]);
					Object valueData = row[data.fields_used[i]];

					Object normalStorageValueData = valueMeta.convertToNormalStorageType(valueData);
					data.scope.put(valueMeta.getName(), normalStorageValueData);
				}

				// also add the meta information for the hole row
				//
				data.scope.put("rowMeta", rowMeta); //$NON-NLS-1$
			} catch (Exception e) {
				throw new KettleValueException(BaseMessages.getString(PKG, "Script.Log.UnexpectedeError"), e); //$NON-NLS-1$ //$NON-NLS-2$
			}

			data.script.eval(data.scope);

			if (bFirstRun) {
				bFirstRun = false;
				// Check if we had a Transformation Status
				Object tran_stat = data.scope.get("trans_Status");
				if (tran_stat != null) {// TODO AKRETION not sure: !=
										// ScriptableObject.NOT_FOUND
					bWithTransStat = true;
					if (log.isDetailed())
						logDetailed(("tran_Status found. Checking transformation status while script execution.")); //$NON-NLS-1$ //$NON-NLS-2$
				} else {
					if (log.isDetailed())
						logDetailed(("No tran_Status found. Transformation status checking not available.")); //$NON-NLS-1$ //$NON-NLS-2$
					bWithTransStat = false;
				}
			}

			if (bWithTransStat) {
				iTranStat = (Integer) data.scope.get("trans_Status");// TODO
																		// ARETION
																		// not
																		// sure
																		// the
																		// casting
																		// is
																		// correct
			} else {
				iTranStat = CONTINUE_TRANSFORMATION;
			}

			if (iTranStat == CONTINUE_TRANSFORMATION) {
				bRC = true;
				for (int i = 0; i < meta.getFieldname().length; i++) {
					Object result = data.scope.get(meta.getFieldname()[i]);
					Object valueData = getValueFromJScript(result, i);
					if (data.replaceIndex[i] < 0) {
						outputRow[outputIndex++] = valueData;
					} else {
						outputRow[data.replaceIndex[i]] = valueData;
					}
				}

				putRow(data.outputRowMeta, outputRow);
			} else {
				switch (iTranStat) {
				case SKIP_TRANSFORMATION:
					// eat this row.
					bRC = true;
					break;
				case ABORT_TRANSFORMATION:
					if (data.cx != null)
						// Context.exit(); TODO AKRETION not sure
						stopAll();
					setOutputDone();
					bRC = false;
					break;
				case ERROR_TRANSFORMATION:
					if (data.cx != null)
						// Context.exit(); TODO AKRETION not sure
						setErrors(1);
					stopAll();
					bRC = false;
					break;
				}

				// TODO: kick this "ERROR handling" junk out now that we have
				// solid error handling in place.
				//
			}
		} catch (ScriptException e) {
			throw new KettleValueException(BaseMessages.getString(PKG, "Script.Log.JavascriptError"), e); //$NON-NLS-1$
		}
		return bRC;
	}

	public Object getValueFromJScript(Object result, int i) throws KettleValueException {
		if (meta.getFieldname()[i] != null && meta.getFieldname()[i].length() > 0) {
			// res.setName(meta.getRename()[i]);
			// res.setType(meta.getType()[i]);

			try {
				if (result != null) {
					String classType = result.getClass().getName();
					switch (meta.getType()[i]) {
					case ValueMetaInterface.TYPE_NUMBER:
						if (classType.equalsIgnoreCase("org.mozilla.javascript.Undefined")) {
							return null;
						} else if (classType.equalsIgnoreCase("org.mozilla.javascript.NativeJavaObject")) {
							try {
								// Is it a java Value class ?
								Value v = (Value) result;
								return v.getNumber();
							} catch (Exception e) {
								String string = (String) result;
								return new Double(Double.parseDouble(Const.trim(string)));
							}
						} else if (classType.equalsIgnoreCase("org.mozilla.javascript.NativeNumber")) {
							Number nb = (Number) result;
							return new Double(nb.doubleValue());// TODO AKRETION
																// not sure
						} else {
							Number nb = (Number) result;
							return new Double(nb.doubleValue());
						}

					case ValueMetaInterface.TYPE_INTEGER:
						if (classType.equalsIgnoreCase("java.lang.Byte")) {
							return new Long(((java.lang.Byte) result).longValue());
						} else if (classType.equalsIgnoreCase("java.lang.Short")) {
							return new Long(((Short) result).longValue());
						} else if (classType.equalsIgnoreCase("java.lang.Integer")) {
							return new Long(((Integer) result).longValue());
						} else if (classType.equalsIgnoreCase("java.lang.Long")) {
							return new Long(((Long) result).longValue());
						} else if (classType.equalsIgnoreCase("java.lang.Double")) {
							return new Long(((Double) result).longValue());
						} else if (classType.equalsIgnoreCase("java.lang.String")) {
							return new Long((new Long((String) result)).longValue());
						} else if (classType.equalsIgnoreCase("org.mozilla.javascript.Undefined")) {
							return null;
						} else if (classType.equalsIgnoreCase("org.mozilla.javascript.NativeNumber")) {
							Number nb = (Number) result;// TODO AKRETION not
														// sure
							return new Long(nb.longValue());
						} else if (classType.equalsIgnoreCase("org.mozilla.javascript.NativeJavaObject")) {
							// Is it a Value?
							//
							try {
								Value value = (Value) result;
								return value.getInteger();
							} catch (Exception e2) {
								String string = (String) result;
								return new Long(Long.parseLong(Const.trim(string)));
							}
						} /*
						 * else if(classType.equalsIgnoreCase(
						 * "org.mozilla.javascript.UniqueTag")) { //TODO
						 * AKRETION NOT implemented return
						 * Long.valueOf(Long.parseLong(((UniqueTag)
						 * result).toString())); }
						 */else {
							return Long.valueOf(Long.parseLong(result.toString()));
						}

					case ValueMetaInterface.TYPE_STRING:
						if (classType.equalsIgnoreCase("org.mozilla.javascript.NativeJavaObject") || //$NON-NLS-1$
								classType.equalsIgnoreCase("org.mozilla.javascript.Undefined")) {
							// Is it a java Value class ?
							try {
								Value v = (Value) result;
								return v.toString();
							} catch (Exception ev) {
								// convert to a string should work in most
								// cases...
								//
								String string = (String) result;
								return string;
							}
						} else {
							// A String perhaps?
							String string = (String) result;
							return string;
						}

					case ValueMetaInterface.TYPE_DATE:
						double dbl = 0;
						if (classType.equalsIgnoreCase("org.mozilla.javascript.Undefined")) {
							return null;
						} else {
							if (classType.equalsIgnoreCase("org.mozilla.javascript.NativeDate")) {
								dbl = (Double) result;// TODO AKRETION not sure
							} else if (classType.equalsIgnoreCase("org.mozilla.javascript.NativeJavaObject") || classType.equalsIgnoreCase("java.util.Date")) {
								// Is it a java Date() class ?
								try {
									Date dat = (Date) result;
									dbl = dat.getTime();
								} catch (Exception e) {
									// Is it a Value?
									//
									try {
										Value value = (Value) result;
										return value.getDate();
									} catch (Exception e2) {
										try {
											String string = (String) result;
											return XMLHandler.stringToDate(string);
										} catch (Exception e3) {
											throw new KettleValueException("Can't convert a string to a date");
										}
									}
								}
							} else if (classType.equalsIgnoreCase("java.lang.Double")) {
								dbl = ((Double) result).doubleValue();
							} else {
								String string = (String) result;
								dbl = Double.parseDouble(string);
							}
							long lng = Math.round(dbl);
							Date dat = new Date(lng);
							return dat;
						}

					case ValueMetaInterface.TYPE_BOOLEAN:
						return (Boolean) result;

					case ValueMetaInterface.TYPE_BIGNUMBER:
						if (classType.equalsIgnoreCase("org.mozilla.javascript.Undefined")) {
							return null;
						} else if (classType.equalsIgnoreCase("org.mozilla.javascript.NativeNumber")) {
							Number nb = (Number) result;// TODO AKRETION not
														// sure
							return new BigDecimal(nb.longValue());
						} else if (classType.equalsIgnoreCase("org.mozilla.javascript.NativeJavaObject")) {
							// Is it a BigDecimal class ?
							try {
								BigDecimal bd = (BigDecimal) result;
								return bd;
							} catch (Exception e) {
								try {
									Value v = (Value) result;
									if (!v.isNull())
										return v.getBigNumber();
									else
										return null;
								} catch (Exception e2) {
									String string = (String) result;
									return new BigDecimal(string);
								}
							}
						} else if (classType.equalsIgnoreCase("java.lang.Byte")) {
							return new BigDecimal(((java.lang.Byte) result).longValue());
						} else if (classType.equalsIgnoreCase("java.lang.Short")) {
							return new BigDecimal(((Short) result).longValue());
						} else if (classType.equalsIgnoreCase("java.lang.Integer")) {
							return new BigDecimal(((Integer) result).longValue());
						} else if (classType.equalsIgnoreCase("java.lang.Long")) {
							return new BigDecimal(((Long) result).longValue());
						} else if (classType.equalsIgnoreCase("java.lang.Double")) {
							return new BigDecimal(((Double) result).longValue());
						} else if (classType.equalsIgnoreCase("java.lang.String")) {
							return new BigDecimal((new Long((String) result)).longValue());
						} else {
							throw new RuntimeException("JavaScript conversion to BigNumber not implemented for " + classType);
						}

					case ValueMetaInterface.TYPE_BINARY: {
						return result;// TODO AKRETION not sure
										// //Context.jsToJava(result,
										// byte[].class);
					}
					case ValueMetaInterface.TYPE_NONE: {
						throw new RuntimeException("No data output data type was specified for new field [" + meta.getFieldname()[i] + "]");
					}
					default: {
						throw new RuntimeException("JavaScript conversion not implemented for type " + meta.getType()[i] + " (" + ValueMeta.getTypeDesc(meta.getType()[i]) + ")");
					}
					}
				} else {
					return null;
				}
			} catch (Exception e) {
				throw new KettleValueException(BaseMessages.getString(PKG, "Script.Log.JavascriptError"), e); //$NON-NLS-1$
			}
		} else {
			throw new KettleValueException("No name was specified for result value #" + (i + 1));
		}
	}

	public RowMetaInterface getOutputRowMeta() {
		return data.outputRowMeta;
	}

	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException {

		meta = (ScriptMeta) smi;
		data = (ScriptData) sdi;

		Object[] r = getRow(); // Get row from input rowset & set row busy!
		if (r == null) {
			// Modification for Additional End Function
			try {
				if (data.cx != null) {
					// Checking for EndScript
					if (strEndScript != null && strEndScript.length() > 0) {
						// Script endScript =
						// data.cx.compileString(strEndScript, "trans_End", 1,
						// null);
						// endScript.exec(data.cx, data.scope);
						data.cx.eval(strEndScript, data.scope);
						if (log.isDetailed())
							logDetailed(("End Script found!"));
					} else {
						if (log.isDetailed())
							logDetailed(("No end Script found!"));
					}
				}
			} catch (Exception e) {
				logError(BaseMessages.getString(PKG, "Script.Log.UnexpectedeError") + " : " + e.toString()); //$NON-NLS-1$ //$NON-NLS-2$
				logError(BaseMessages.getString(PKG, "Script.Log.ErrorStackTrace") + Const.CR + Const.getStackTracker(e)); //$NON-NLS-1$
				setErrors(1);
				stopAll();
			}

			if (data.cx != null)
				// Context.exit(); TODO AKRETION not sure
				setOutputDone();
			return false;
		}

		// Getting the Row, with the Transformation Status
		try {
			addValues(getInputRowMeta(), r);
		} catch (KettleValueException e) {
			String location = null;
			if (e.getCause() instanceof ScriptException) {
				ScriptException ee = (ScriptException) e.getCause();
				location = "--> " + ee.getLineNumber() + ":" + ee.getColumnNumber(); // $NON-NLS-1$
																						// $NON-NLS-2$
			}

			if (getStepMeta().isDoingErrorHandling()) {
				putError(getInputRowMeta(), r, 1, e.getMessage() + Const.CR + location, null, "SCR-001");
				bRC = true; // continue by all means, even on the first row and
							// out of this ugly design
			} else {
				throw (e);
			}
		}

		if (checkFeedback(getLinesRead()))
			logBasic(BaseMessages.getString(PKG, "Script.Log.LineNumber") + getLinesRead()); //$NON-NLS-1$
		return bRC;
	}

	public boolean init(StepMetaInterface smi, StepDataInterface sdi) {
		meta = (ScriptMeta) smi;
		data = (ScriptData) sdi;

		if (super.init(smi, sdi)) {

			// Add init code here.
			// Get the actual Scripts from our MetaData
			jsScripts = meta.getJSScripts();
			for (int j = 0; j < jsScripts.length; j++) {
				switch (jsScripts[j].getScriptType()) {
				case ScriptValuesScript.TRANSFORM_SCRIPT:
					strTransformScript = jsScripts[j].getScript();
					break;
				case ScriptValuesScript.START_SCRIPT:
					strStartScript = jsScripts[j].getScript();
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

	public void dispose(StepMetaInterface smi, StepDataInterface sdi) {
		try {
			if (data.cx != null)
				return;
			// Context.exit(); TODO AKRETION not sure
		} catch (Exception er) {
			// Eat this error, it's typically :
			// "Calling Context.exit without previous Context.enter"
			// logError(BaseMessages.getString(PKG, "System.Log.UnexpectedError"), er);
		}
		;

		super.dispose(smi, sdi);
	}

}