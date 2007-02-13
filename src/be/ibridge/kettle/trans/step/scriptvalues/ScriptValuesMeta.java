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

package be.ibridge.kettle.trans.step.scriptvalues;

import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;

import org.eclipse.swt.widgets.Shell;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.JavaScriptException;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.w3c.dom.Node;

import be.ibridge.kettle.core.CheckResult;
import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.XMLHandler;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.exception.KettleXMLException;
import be.ibridge.kettle.core.value.Value;
import be.ibridge.kettle.repository.Repository;
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

public class ScriptValuesMeta extends BaseStepMeta implements StepMetaInterface
{
	private String  script;
	
	private String  name[];
	private String  rename[];
	private int     type[];
	private int     length[];
	private int     precision[];
	
	public ScriptValuesMeta()
	{
		super(); // allocate BaseStepMeta
	}
	
    /**
     * @return Returns the length.
     */
    public int[] getLength()
    {
        return length;
    }
    
    /**
     * @param length The length to set.
     */
    public void setLength(int[] length)
    {
        this.length = length;
    }
    
    /**
     * @return Returns the name.
     */
    public String[] getName()
    {
        return name;
    }
    
    /**
     * @param name The name to set.
     */
    public void setName(String[] name)
    {
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
    
    
    
    
	public void loadXML(Node stepnode, ArrayList databases, Hashtable counters)
		throws KettleXMLException
	{
		readData(stepnode);
	}

	public void allocate(int nrfields)
	{
		name      = new String[nrfields];
		rename    = new String[nrfields];
		type      = new int   [nrfields];
		length    = new int   [nrfields];
		precision = new int   [nrfields];
	}

	public Object clone()
	{
		ScriptValuesMeta retval = (ScriptValuesMeta)super.clone();
		
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
	
	private void readData(Node stepnode)
		throws KettleXMLException
	{
		try
		{
			String slen, sprc;
			int i, nrfields;
			
			script     = XMLHandler.getTagValue(stepnode, "script"); //$NON-NLS-1$
			
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
			throw new KettleXMLException(Messages.getString("ScriptValuesMeta.Exception.UnableToLoadStepInfoFromXML"), e); //$NON-NLS-1$
		}
	}

	public void setDefault()
	{
		script = ""; //$NON-NLS-1$
		
		int nrfields=0;
		
		allocate(nrfields);

		for (int i=0;i<nrfields;i++)
		{
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

	public String getXML()
	{
        StringBuffer retval = new StringBuffer();
		
		retval.append("    "+XMLHandler.addTagValue("script", script)); //$NON-NLS-1$ //$NON-NLS-2$
        
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

	public void readRep(Repository rep, long id_step, ArrayList databases, Hashtable counters)
		throws KettleException
	{
		try
		{
			script     = rep.getStepAttributeString(id_step, "script"); //$NON-NLS-1$
			
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
			throw new KettleException(Messages.getString("ScriptValuesMeta.Exception.UnexpectedErrorInReadingStepInfo"), e); //$NON-NLS-1$
		}
	}

	public void saveRep(Repository rep, long id_transformation, long id_step)
		throws KettleException
	{
		try
		{
			rep.saveStepAttribute(id_transformation, id_step, "script", script); //$NON-NLS-1$
	
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
			throw new KettleException(Messages.getString("ScriptValuesMeta.Exception.UnableToSaveStepInfo")+id_step, e); //$NON-NLS-1$
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
			
		// Scriptable jsvalue = Context.toObject(new ScriptValues(), jsscope);
		// jsscope.put("_step_", jsscope, jsvalue);
		//StringReader in = new StringReader(script);
		
		if (prev!=null && prev.size()>0)
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("ScriptValuesMeta.CheckResult.ConnectedStepOK",String.valueOf(prev.size())), stepinfo); //$NON-NLS-1$ //$NON-NLS-2$
			remarks.add(cr);
			
			Scriptable jsrow = Context.toObject(prev, jsscope);
			jsscope.put("row", jsscope, jsrow); //$NON-NLS-1$
			for (int i=0;i<prev.size();i++)
			{
				Value val = prev.getValue(i); 
				// Set date and string values to something to simulate real thing
				if (val.isDate()) val.setValue(new Date());
				if (val.isString()) val.setValue("test value test value test value test value test value test value test value test value test value test value"); //$NON-NLS-1$
				Scriptable jsarg = Context.toObject(val, jsscope);
				jsscope.put(val.getName(), jsscope, jsarg);
			}
			// Add support for Value class (new Value())
			Scriptable jsval = Context.toObject(Value.class, jsscope);
			jsscope.put("Value", jsscope, jsval); //$NON-NLS-1$
			
			try
			{
				//ScriptableObject.defineClass(jsscope, Value.class);

				jsscript=jscx.compileString(script, "script", 1, null); //$NON-NLS-1$
				
				cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("ScriptValuesMeta.CheckResult.ScriptCompiledOK"), stepinfo); //$NON-NLS-1$
				remarks.add(cr);

				try
				{
					jsscript.exec(jscx, jsscope);

					cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("ScriptValuesMeta.CheckResult.ScriptCompiledOK2"), stepinfo); //$NON-NLS-1$
					remarks.add(cr);
					
					if (name.length>0)
					{
						StringBuffer message = new StringBuffer(Messages.getString("ScriptValuesMeta.CheckResult.FailedToGetValues",String.valueOf(name.length))+Const.CR+Const.CR); //$NON-NLS-1$ //$NON-NLS-2$
						for (int i=0;i<name.length;i++)
						{
							Value res = new Value();
							message.append("   "); //$NON-NLS-1$
							if (getValue(jsscope, i, res, message)) 
							{
								error_found = true;
							}
							message.append(Const.CR);
						}
						
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
				}
				catch(JavaScriptException jse)
				{
					Context.exit();
					error_message=Messages.getString("ScriptValuesMeta.CheckResult.CouldNotExecuteScript")+Const.CR+jse.toString(); //$NON-NLS-1$
					cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, error_message, stepinfo);
					remarks.add(cr);
				}
				catch(Exception e)
				{
					Context.exit();
					error_message=Messages.getString("ScriptValuesMeta.CheckResult.CouldNotExecuteScript2")+Const.CR+e.toString(); //$NON-NLS-1$
					cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, error_message, stepinfo);
					remarks.add(cr);
				}
			}
			catch(Exception e)
			{
				Context.exit();
				error_message = Messages.getString("ScriptValuesMeta.CheckResult.CouldNotCompileScript")+Const.CR+e.toString(); //$NON-NLS-1$
				cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, error_message, stepinfo);
				remarks.add(cr);
			}
		}
		else
		{
			Context.exit();
			error_message = Messages.getString("ScriptValuesMeta.CheckResult.CouldNotGetFieldsFromPreviousStep"); //$NON-NLS-1$
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, error_message, stepinfo);
			remarks.add(cr);
		}

		// See if we have input streams leading to this step!
		if (input.length>0)
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("ScriptValuesMeta.CheckResult.ConnectedStepOK2"), stepinfo); //$NON-NLS-1$
			remarks.add(cr);
		}
		else
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages.getString("ScriptValuesMeta.CheckResult.NoInputReceived"), stepinfo); //$NON-NLS-1$
			remarks.add(cr);
		}
	}

	public boolean getValue(Scriptable scope, int i, Value res, StringBuffer message)
	{
		boolean error_found = false;
		
		if (name[i]!=null && name[i].length()>0)
		{
			res.setName(rename[i]);
			res.setType(type[i]);
			
			try
			{
				Object result = scope.get(name[i], scope);
				if (result!=null)
				{
					String classname = result.getClass().getName();
						
					switch(type[i])
					{
					case Value.VALUE_TYPE_NUMBER:
						if (classname.equalsIgnoreCase("org.mozilla.javascript.Undefined")) //$NON-NLS-1$
						{
							res.setNull();
						}
						else
						if (classname.equalsIgnoreCase("org.mozilla.javascript.NativeJavaObject")) //$NON-NLS-1$
						{
							// Is it a java Value class ?
							Value v = (Value)Context.toType(result, Value.class);
							res.setValue( v.getNumber() );
						}
						else
						{
							res.setValue( ((Double)result).doubleValue() ); 
						}
						break;
					case Value.VALUE_TYPE_INTEGER:
						if (classname.equalsIgnoreCase("java.lang.Byte")) //$NON-NLS-1$
						{
							res.setValue( ((java.lang.Byte)result).longValue() );
						}
						else
						if (classname.equalsIgnoreCase("java.lang.Short")) //$NON-NLS-1$
						{
							res.setValue( ((Short)result).longValue() );
						}
						else
						if (classname.equalsIgnoreCase("java.lang.Integer")) //$NON-NLS-1$
						{
							res.setValue( ((Integer)result).longValue() );
						}
						else
						if (classname.equalsIgnoreCase("java.lang.Long")) //$NON-NLS-1$
						{
							res.setValue( ((Long)result).longValue() );
						}
						else
						if (classname.equalsIgnoreCase("org.mozilla.javascript.Undefined")) //$NON-NLS-1$
						{
							res.setNull();
						}
						else
						if (classname.equalsIgnoreCase("org.mozilla.javascript.NativeJavaObject")) //$NON-NLS-1$
						{
							// Is it a java Value class ?
							Value v = (Value)Context.toType(result, Value.class);
							res.setValue( v.getInteger() );
						}
						else
						{
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
							res.setValue( result.toString() ); 
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
                    case Value.VALUE_TYPE_BINARY:
                        byte[] bytes = new byte[1];
                        byte[] content = (byte[])Context.toType(result, bytes.getClass());
                        res.setValue(content);
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
				message.append(Messages.getString("ScriptValuesMeta.CheckResult.ErrorRetrievingValue", "["+name[i]+"]")+" : "+e.toString()+Const.CR+Const.getStackTracker(e)); //$NON-NLS-1$ //$NON-NLS-2$
				error_found=true;
			}
			res.setLength(length[i], precision[i]);
				
			message.append(Messages.getString("ScriptValuesMeta.CheckResult.RetrievedValue",name[i],res.toStringMeta())); //$NON-NLS-1$ //$NON-NLS-2$
		}
		else
		{
			message.append(Messages.getString("ScriptValuesMeta.CheckResult.ValueIsEmpty",String.valueOf(i))); //$NON-NLS-1$ //$NON-NLS-2$
			error_found=true;
		}
		
		return error_found;
	}

	public StepDialogInterface getDialog(Shell shell, StepMetaInterface info, TransMeta transMeta, String name)
	{
		return new ScriptValuesDialog(shell, info, transMeta, name);
	}

	public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta, Trans trans)
	{
		return new ScriptValues(stepMeta, stepDataInterface, cnr, transMeta, trans);
        
	}

	public StepDataInterface getStepData()
	{
		return new ScriptValuesData();
	}
    
    /**
     * @return the libraries that this step or plugin uses.
     */
    public String[] getUsedLibraries()
    {
        return new String[] { "js.jar" };
    }

    public boolean supportsErrorHandling()
    {
        return true;
    }
}
