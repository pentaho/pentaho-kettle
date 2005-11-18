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
			
			script     = XMLHandler.getTagValue(stepnode, "script");
			
			Node fields = XMLHandler.getSubNode(stepnode, "fields");
			nrfields = XMLHandler.countNodes(fields, "field");
				
			allocate(nrfields);
			
			for (i=0;i<nrfields;i++)
			{
				Node fnode = XMLHandler.getSubNodeByNr(fields, "field", i);
				
				name     [i] = XMLHandler.getTagValue(fnode, "name");
				rename   [i] = XMLHandler.getTagValue(fnode, "rename");
				type     [i] = Value.getType(XMLHandler.getTagValue(fnode, "type"));
	
				slen = XMLHandler.getTagValue(fnode, "length");
				sprc = XMLHandler.getTagValue(fnode, "precision");
				length   [i]=Const.toInt(slen, -1);
				precision[i]=Const.toInt(sprc, -1);
			}
		}
		catch(Exception e)
		{
			throw new KettleXMLException("Unable to load step info from XML", e);
		}
	}

	public void setDefault()
	{
		script = "";
		
		int nrfields=0;
		
		allocate(nrfields);

		for (int i=0;i<nrfields;i++)
		{
			name     [i] = "newvalue";
			rename   [i] = "newvalue";
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
		String retval="";
		int i;
		
		retval+="    "+XMLHandler.addTagValue("script", script);
		retval+="    <fields>";
		for (i=0;i<name.length;i++)
		{
			retval+="      <field>";
			retval+="        "+XMLHandler.addTagValue("name",      name[i]);
			retval+="        "+XMLHandler.addTagValue("rename",    rename[i]);
			retval+="        "+XMLHandler.addTagValue("type",      Value.getTypeDesc(type[i]));
			retval+="        "+XMLHandler.addTagValue("length",    length[i]);
			retval+="        "+XMLHandler.addTagValue("precision", precision[i]);
			retval+="        </field>";
		}
		retval+="      </fields>";
		
		return retval;
	}

	public void readRep(Repository rep, long id_step, ArrayList databases, Hashtable counters)
		throws KettleException
	{
		try
		{
			script     = rep.getStepAttributeString(id_step, "script");
			
			int nrfields = rep.countNrStepAttributes(id_step, "field_name");
			
			allocate(nrfields);
	
			for (int i=0;i<nrfields;i++)
			{
				name[i]        =       rep.getStepAttributeString (id_step, i, "field_name");
				rename[i]      =       rep.getStepAttributeString (id_step, i, "field_rename");
				type[i]        =  Value.getType( rep.getStepAttributeString (id_step, i, "field_type") );
				length[i]      =  (int)rep.getStepAttributeInteger(id_step, i, "field_length");
				precision[i]   =  (int)rep.getStepAttributeInteger(id_step, i, "field_precision");
			}
		}
		catch(Exception e)
		{
			throw new KettleException("Unexpected error reading step information from the repository", e);
		}
	}

	public void saveRep(Repository rep, long id_transformation, long id_step)
		throws KettleException
	{
		try
		{
			rep.saveStepAttribute(id_transformation, id_step, "script", script);
	
			for (int i=0;i<name.length;i++)
			{
				rep.saveStepAttribute(id_transformation, id_step, i, "field_name",      name[i]);
				rep.saveStepAttribute(id_transformation, id_step, i, "field_rename",    rename[i]);
				rep.saveStepAttribute(id_transformation, id_step, i, "field_type",      Value.getTypeDesc(type[i]));
				rep.saveStepAttribute(id_transformation, id_step, i, "field_length",    length[i]);
				rep.saveStepAttribute(id_transformation, id_step, i, "field_precision", precision[i]);
			}
		}
		catch(Exception e)
		{
			throw new KettleException("Unable to save step information to the repository for id_step="+id_step, e);
		}
	}

	//private boolean test(boolean getvars, boolean popup)
	public void check(ArrayList remarks, StepMeta stepinfo, Row prev, String input[], String output[], Row info)
	{
		boolean error_found=false;
		String error_message = "";
		CheckResult cr;
		
		Context jscx;
		Scriptable jsscope;
		Script jsscript;

		jscx = Context.enter();
		jsscope = jscx.initStandardObjects(null);
			
		// Scriptable jsvalue = Context.toObject(ScriptValues.class, jsscope);
		// jsscope.put("_step_", jsscope, jsvalue);
		//StringReader in = new StringReader(script);
		
		if (prev!=null && prev.size()>0)
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, "Step is connected to previous one, receiving "+prev.size()+" fields", stepinfo);
			remarks.add(cr);
			
			Scriptable jsrow = Context.toObject(prev, jsscope);
			jsscope.put("row", jsscope, jsrow);
			for (int i=0;i<prev.size();i++)
			{
				Value val = prev.getValue(i); 
				// Set date and string values to something to simulate real thing
				if (val.isDate()) val.setValue(new Date());
				if (val.isString()) val.setValue("test value test value test value test value test value test value test value test value test value test value");
				Scriptable jsarg = Context.toObject(val, jsscope);
				jsscope.put(val.getName(), jsscope, jsarg);
			}
			// Add support for Value class (new Value())
			Scriptable jsval = Context.toObject(Value.class, jsscope);
			jsscope.put("Value", jsscope, jsval);
			
			try
			{
				//ScriptableObject.defineClass(jsscope, Value.class);

				jsscript=jscx.compileString(script, "script", 1, null);
				
				cr = new CheckResult(CheckResult.TYPE_RESULT_OK, "Script compiled without a problem", stepinfo);
				remarks.add(cr);

				try
				{
					jsscript.exec(jscx, jsscope);

					cr = new CheckResult(CheckResult.TYPE_RESULT_OK, "Script executed without a problem", stepinfo);
					remarks.add(cr);
					
					if (name.length>0)
					{
						StringBuffer message = new StringBuffer("Trying to retrieve "+name.length+" fields: "+Const.CR+Const.CR);
						for (int i=0;i<name.length;i++)
						{
							Value res = new Value();
							message.append("   ");
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
					error_message="Couldn't execute this script! Error:"+Const.CR+jse.toString();
					cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, error_message, stepinfo);
					remarks.add(cr);
				}
				catch(Exception e)
				{
					Context.exit();
					error_message="General error executing script:"+Const.CR+e.toString();
					cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, error_message, stepinfo);
					remarks.add(cr);
				}
			}
			catch(Exception e)
			{
				Context.exit();
				error_message = "Couldn't compile this script! Error:"+Const.CR+e.toString();
				cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, error_message, stepinfo);
				remarks.add(cr);
			}
		}
		else
		{
			Context.exit();
			error_message = "Couldn't get fields from previous steps, please connect all needed hops!";
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, error_message, stepinfo);
			remarks.add(cr);
		}

		// See if we have input streams leading to this step!
		if (input.length>0)
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, "Step is receiving info from other steps.", stepinfo);
			remarks.add(cr);
		}
		else
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, "No input received from other steps!", stepinfo);
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
						if (classname.equalsIgnoreCase("org.mozilla.javascript.Undefined"))
						{
							res.setNull();
						}
						else
						if (classname.equalsIgnoreCase("org.mozilla.javascript.NativeJavaObject"))
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
						if (classname.equalsIgnoreCase("java.lang.Byte"))
						{
							res.setValue( ((java.lang.Byte)result).longValue() );
						}
						else
						if (classname.equalsIgnoreCase("java.lang.Short"))
						{
							res.setValue( ((Short)result).longValue() );
						}
						else
						if (classname.equalsIgnoreCase("java.lang.Integer"))
						{
							res.setValue( ((Integer)result).longValue() );
						}
						else
						if (classname.equalsIgnoreCase("java.lang.Long"))
						{
							res.setValue( ((Long)result).longValue() );
						}
						else
						if (classname.equalsIgnoreCase("org.mozilla.javascript.Undefined"))
						{
							res.setNull();
						}
						else
						if (classname.equalsIgnoreCase("org.mozilla.javascript.NativeJavaObject"))
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
						if (classname.equalsIgnoreCase("org.mozilla.javascript.NativeJavaObject") || 
							classname.equalsIgnoreCase("org.mozilla.javascript.Undefined"))
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
						if (classname.equalsIgnoreCase("org.mozilla.javascript.Undefined"))
						{
							res.setNull();
						}
						else
						{
							if (classname.equalsIgnoreCase("org.mozilla.javascript.NativeDate"))
							{
								dbl = Context.toNumber(result);
							}
							else
							if (classname.equalsIgnoreCase("org.mozilla.javascript.NativeJavaObject"))
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
				message.append("Error retrieving value "+name[i]+" : "+e.toString());
				error_found=true;
			}
			res.setLength(length[i], precision[i]);
				
			message.append(" - Retrieved value "+name[i]+" of type "+res.toStringMeta());
		}
		else
		{
			message.append("Error: value #"+i+" is empty!");
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

}
