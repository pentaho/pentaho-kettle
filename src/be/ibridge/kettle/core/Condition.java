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

 
package be.ibridge.kettle.core;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.regex.Pattern;

import org.w3c.dom.Node;

import be.ibridge.kettle.core.exception.KettleDatabaseException;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.exception.KettleXMLException;
import be.ibridge.kettle.core.value.Value;
import be.ibridge.kettle.repository.Repository;


/**
   This class describes a condition in a general meaning.
   
	A condition can either be<p>
	<p>
	1) Atomic  (a=10, B='aa')<p>
	2) Composite ( NOT Condition1 AND Condition2 OR Condition3 )<p>
<p>	
	If the nr of atomic conditions is 0, the condition is atomic, otherwise it's Composit.<p>
	Precedence doesn't exist.  Conditions are evaluated in the order in which they are found.<p>
	A condition can be negated or not.<p> 
<p>

 @author Matt
 @since 8-06-2004
   
*/

public class Condition implements Cloneable, XMLInterface
{
	public static final String[] operators = new String[] { "-", "OR", "AND", "NOT", "OR NOT", "AND NOT", "XOR" };
	
	public static final int OPERATOR_NONE       = 0;
	public static final int OPERATOR_OR         = 1;
	public static final int OPERATOR_AND        = 2;
	public static final int OPERATOR_NOT        = 3;
	public static final int OPERATOR_OR_NOT     = 4;
	public static final int OPERATOR_AND_NOT    = 5;
	public static final int OPERATOR_XOR        = 6;

	public static final String[] functions = new String[] { "=", "<>", "<", "<=", ">", ">=", "REGEXP", "IS NULL", "IS NOT NULL", "IN LIST", "CONTAINS", "STARTS WITH", "ENDS WITH" };
	
	public static final int FUNC_EQUAL         = 0;
	public static final int FUNC_NOT_EQUAL     = 1;
	public static final int FUNC_SMALLER       = 2;
	public static final int FUNC_SMALLER_EQUAL = 3;
	public static final int FUNC_LARGER        = 4;
	public static final int FUNC_LARGER_EQUAL  = 5;
	public static final int FUNC_REGEXP        = 6;
	public static final int FUNC_NULL          = 7;
	public static final int FUNC_NOT_NULL      = 8;
	public static final int FUNC_IN_LIST       = 9;
	public static final int FUNC_CONTAINS      = 10;
	public static final int FUNC_STARTS_WITH   = 11;
	public static final int FUNC_ENDS_WITH     = 12;

	//
	// These parameters allow for:
	// value = othervalue
	// value = 'A'
	// NOT value = othervalue
	//
	
	private long id;
	
	private boolean negate;
	private int operator;
	private String left_valuename;
	private int function;
	private String right_valuename;
	private Value right_exact;
	private long  id_right_exact;
	
	private int left_fieldnr;
	private int right_fieldnr;
	
	private ArrayList list;
	
	public Condition()
	{
		list = new ArrayList();
		this.operator        = OPERATOR_NONE;
		this.negate          = false;
		
		left_fieldnr = -1;
		right_fieldnr = -1;
		
		id=-1L;
	}
	
	public Condition(String valuename, int function, String valuename2, Value exact)
	{
		this();
		this.left_valuename  = valuename;
		this.function        = function;
		this.right_valuename = valuename2;
		this.right_exact     = exact;
		
		clearFieldPositions();
	}

	public Condition(int operator, String valuename, int function, String valuename2, Value exact)
	{
		this();
		this.operator        = operator;
		this.left_valuename  = valuename;
		this.function        = function;
		this.right_valuename = valuename2;
		this.right_exact     = exact;
		
		clearFieldPositions();
	}

	public Condition(boolean negate, String valuename, int function, String valuename2, Value exact)
	{
		this(valuename, function, valuename2, exact);
		this.negate = negate; 
	}
	
	/**
	 * Returns the database ID of this Condition if a repository was used before.
	 * 
	 * @return the ID of the db connection.
	 */
	public long getID()
	{
		return id;
	}

	/**
	 * Set the database ID for this Condition in the repository.
	 * @param id The ID to set on this condition.
	 * 
	 */
	public void setID(long id)
	{
		this.id = id;
	}


	public Object clone()
	{
		Condition retval = null;

		retval = new Condition();
		retval.negate = negate;
		retval.operator = operator;

		if (isComposite())
		{
			for (int i=0;i<nrConditions();i++)
			{
				Condition c = getCondition(i);
				Condition cCopy = (Condition)c.clone();
				retval.addCondition(cCopy);	
			}
		}
		else
		{
            retval.negate          = negate;
			retval.left_valuename  = left_valuename;
			retval.operator        = operator;
			retval.right_valuename = right_valuename;
			retval.function        = function;
			if (right_exact!=null)
			{
				retval.right_exact = (Value)right_exact.clone();
			}
			else
			{
				retval.right_exact = null;
			}
 		}
		
		return retval;
	}
	
	public void setOperator(int operator)
	{
		this.operator = operator;
	}
	
	public int getOperator()
	{
		return operator;
	}
	
	public String getOperatorDesc()
	{
		return Const.rightPad(operators[operator], 7);
	}
	
	public static final int getOperator(String description)
	{
		if (description==null) return OPERATOR_NONE;
		
		for (int i=1;i<operators.length;i++)
		{
			if (operators[i].equalsIgnoreCase(Const.trim(description))) return i;
		}
		return OPERATOR_NONE;
	}
	
	public static final String[] getOperators()
	{
		String retval[] = new String[operators.length-1];
		for (int i=1;i<operators.length;i++)
		{
			retval[i-1] = operators[i]; 
		}
		return retval;
	}
	
	public static final String[] getRealOperators()
	{
		return new String[] { "OR", "AND", "OR NOT", "AND NOT", "XOR" };
	}

	
	public void setLeftValuename(String left_valuename)
	{
		this.left_valuename = left_valuename;
	}

	public String getLeftValuename()
	{
		return left_valuename;
	}

	public int getFunction()
	{
		return function;
	}
	
	public void setFunction( int function )
	{
		this.function = function;
	}
	
	public String getFunctionDesc()
	{
		return functions[function];
	}
	
	public static final int getFunction(String description)
	{
		for (int i=1;i<functions.length;i++)
		{
			if (functions[i].equalsIgnoreCase(Const.trim(description))) return i;
		}
		return FUNC_EQUAL;
	}
	
	public void setRightValuename(String right_valuename)
	{
		this.right_valuename = right_valuename;
	}

	public String getRightValuename()
	{
		return right_valuename;
	}

	public void setRightExact(Value right_exact)
	{
		this.right_exact = right_exact;
	}
	
	public Value getRightExact()
	{
		return right_exact;
	}
	
	public String getRightExactString()
	{
		if (right_exact == null) return null;
		return right_exact.toString();
	}

	/**
	 * Get the id of the RightExact Value in the repository
	 * @return The id of the RightExact Value in the repository
	 */
	public long getRightExactID()
	{
		return id_right_exact;
	}

	/**
	 * Set the database ID for the RightExact Value in the repository.
	 * @param id_right_exact The ID to set on this Value.
	 * 
	 */
	public void setRightExactID(long id_right_exact)
	{
		this.id_right_exact = id_right_exact;
	}

	public boolean isAtomic()
	{
		return list.size()==0;
	}
	
	public boolean isComposite()
	{
		return list.size()!=0;
	}
	
	public boolean isNegated()
	{
		return negate;
	}
	
	public void setNegated(boolean negate)
	{
		this.negate = negate;
	}
	
	public void negate()
	{
		setNegated(!isNegated());
	}
	
	/**
	 * A condition is empty when the condition is atomic and no left field is specified.
	 */
	public boolean isEmpty()
	{
	   return (isAtomic() && left_valuename==null); 
	}

	/** 
	 * We cache the position of a value in a row.
	 * If ever we want to change the rowtype, we need to
	 * clear these cached field positions...
	 */
	public void clearFieldPositions()
	{
		left_fieldnr = -1;
		right_fieldnr = -1;
	}
	
	//
	// Evaluate the condition...
	//
	public boolean evaluate(Row r)
	{
	    String debug="Start of evaluate";
		boolean retval = false;

		// If we have 0 items in the list, evaluate the current condition
		// Otherwise, evaluate all sub-conditions
		//
	    try
	    {
			if (isAtomic())
			{
			    debug="Atomic : get fieldnrs left value";
				// Check out the fieldnrs if we don't have them...
				if (left_valuename!=null  && left_valuename.length()>0  && left_fieldnr<0)  left_fieldnr = r.searchValueIndex(left_valuename);
			    debug="Atomic : get fieldnrs right value";
				if (right_valuename!=null && right_valuename.length()>0 && right_fieldnr<0) right_fieldnr = r.searchValueIndex(right_valuename);
				
			    debug="Atomic : get fieldnrs left field";
				Value field=null;
				if (left_fieldnr>=0) field= r.getValue(left_fieldnr);
				
			    debug="Atomic : get fieldnrs right exact";
				Value field2 = right_exact;
				if (field2==null && right_fieldnr>=0) field2 = r.getValue(right_fieldnr);
				
			    debug="Atomic : evaluate (function="+Condition.functions[function]+")";
				switch(function)
				{
					case FUNC_EQUAL         : retval = (field.compare(field2)==0); break;
					case FUNC_NOT_EQUAL     : retval = (field.compare(field2)!=0); break;
					case FUNC_SMALLER       : retval = (field.compare(field2)< 0); break;
					case FUNC_SMALLER_EQUAL : retval = (field.compare(field2)<=0); break;
					case FUNC_LARGER        : retval = (field.compare(field2)> 0); break;
					case FUNC_LARGER_EQUAL  : retval = (field.compare(field2)>=0); break;
					case FUNC_REGEXP        : retval = Pattern.matches(field2.getString(), field.getString()); break;
					case FUNC_NULL          : retval = (field.isNull());           break;
					case FUNC_NOT_NULL      : retval = (!field.isNull());          break;
					case FUNC_IN_LIST		: 
					    	String list[] = Const.splitString(field2.getString(), ";");
					    	retval = Const.indexOfString(field.getString(), list)>=0;
					    	break;
					case FUNC_CONTAINS      : retval = field.getString()!=null?field.getString().indexOf(field2.getString())>=0:false; break;
					case FUNC_STARTS_WITH   : retval = field.getString()!=null?field.getString().startsWith(field2.getString()):false; break;
					case FUNC_ENDS_WITH     : retval = field.getString()!=null?field.getString().endsWith(field2.getString()):false;   break;
					default: break;
				}
				
				// Only NOT makes sense, the rest doesn't, so ignore!!!!
				if (isNegated()) retval=!retval;
			}
			else
			{
			    debug="Composite : get first";
				Condition cb0 = (Condition)list.get(0);
				retval = cb0.evaluate(r);
				
				// Loop over the conditions listed below.
				// 
				for (int i=1;i<list.size();i++)
				{
				    debug="Composite : evaluate #"+i;
					Condition cb = (Condition)list.get(i);
					boolean cmp = cb.evaluate(r);
					switch (cb.getOperator()) 
					{
					case Condition.OPERATOR_OR      : retval = retval || cmp; break;
					case Condition.OPERATOR_AND     : retval = retval && cmp; break;
					case Condition.OPERATOR_OR_NOT  : retval = retval || ( !cmp ); break;
					case Condition.OPERATOR_AND_NOT : retval = retval && ( !cmp ); break;
					case Condition.OPERATOR_XOR     : retval = retval ^ cmp; break;
					default: break;
					}
				}
				if (isNegated()) retval=!retval;
			}
	    }
	    catch(Exception e)
	    {
	        throw new RuntimeException("Unexpected error evaluation condition ["+toString()+"] in part ["+debug+"] for row: "+r+Const.CR+e.toString());
	    }
		
		return retval;
	}
	
	public void addCondition(Condition cb)
	{
		if (isAtomic() && getLeftValuename()!=null)
		{
			/* Copy current atomic setup...
			 * 
			 */
			Condition current = new Condition(getLeftValuename(), 
											  getFunction(),
											  getRightValuename(),
											  getRightExact());
			current.setNegated(isNegated());
			setNegated(false);
			list.add(current);
		}
		else
		// Set default operator if not on first position...
		if (isComposite() && list.size()>0 && cb.getOperator()==OPERATOR_NONE)
		{
		    cb.setOperator(OPERATOR_AND);
		}
		list.add( cb );
	}
	
	public void addCondition(int idx, Condition cb)
	{
		if (isAtomic() && getLeftValuename()!=null)
		{
			/* Copy current atomic setup...
			 * 
			 */
			Condition current = new Condition(getLeftValuename(), 
											  getFunction(),
											  getRightValuename(),
											  getRightExact());
			current.setNegated(isNegated());
			setNegated(false);
			list.add(current);
		}
		else
		// Set default operator if not on first position...
		if (isComposite() && idx>0 && cb.getOperator()==OPERATOR_NONE)
		{
		    cb.setOperator(OPERATOR_AND);
		}
		list.add(idx, cb );
	}

	
	public void removeCondition(int nr)
	{
		if (isComposite())
		{
			Condition c = (Condition)list.get(nr);
			list.remove(nr);

		    // Nothing left or only one condition left: move it to the parent: make it atomic.

			boolean moveUp = isAtomic() || nrConditions()==1;
			if (nrConditions()==1) c=getCondition(0);
			
			if (moveUp)
			{
				setLeftValuename(c.getLeftValuename());
				setFunction(c.getFunction());
				setRightValuename(c.getRightValuename());
				setRightExact(c.getRightExact());
				setNegated(c.isNegated());
			}
		}
	}
	
	public int nrConditions()
	{
		return list.size();
	}
	
	public Condition getCondition(int i)
	{
		return (Condition)list.get(i);
	}
	
	public void setCondition(int i, Condition subCondition)
	{
	    list.set(i, subCondition);
	}

	public String toString()
	{
		return toString(0, true, true);
	}
	
	public String toString(int level, boolean show_negate, boolean show_operator)
	{
		String retval="";
		
		if (isAtomic())
		{
			//retval+="<ATOMIC "+level+", "+show_negate+", "+show_operator+">";

			for (int i=0;i<level;i++) retval+="  ";
			
			if (show_operator && getOperator()!=OPERATOR_NONE)
			{
				retval += getOperatorDesc()+" ";
			}
			else 
			{
				retval+="        ";
			}

			// Atomic is negated?
			if (isNegated() && ( show_negate || level>0 ))
			{
				retval+="NOT ( ";
			}
			else
			{
				retval+="      ";
			}
			retval+=left_valuename+" "+getFunctionDesc();
			if (function != FUNC_NULL && function != FUNC_NOT_NULL)
			{
				if ( right_valuename != null )
				{
					retval+=" "+right_valuename;
				}
				else
				{
					retval+=" ["+( getRightExactString()==null?"":getRightExactString() )+"]";
				}
			}

			if (isNegated() && ( show_negate || level>0 )) retval+=" )";

			retval+=Const.CR;
		}
		else
		{
			//retval+="<COMP "+level+", "+show_negate+", "+show_operator+">";

			// Group is negated?
			if (isNegated() && (show_negate || level>0))
			{
				for (int i=0;i<level;i++) retval+="  ";
				retval+="NOT"+Const.CR;
			}
			// Group is preceded by an operator:
			if (getOperator()!=OPERATOR_NONE && (show_operator || level>0)) 
			{
				for (int i=0;i<level;i++) retval+="  ";
				retval+=getOperatorDesc()+Const.CR;
			}
			for (int i=0;i<level;i++) retval+="  "; retval+="("+Const.CR;
			for (int i=0;i<list.size();i++)
			{
				Condition cb = (Condition)list.get(i);
				retval+=cb.toString(level+1, true, true && i>0);
			}
			for (int i=0;i<level;i++) retval+="  "; retval+=")"+Const.CR;
		}
		
		return retval;
	}
	
	public String getXML()
	{
		return getXML(0);
	}
	
	public String getXML(int level)
	{
		String retval="";
		String indent1 = Const.rightPad(" ", level);
		String indent2 = Const.rightPad(" ", level+1);
		String indent3 = Const.rightPad(" ", level+2);

		retval+= indent1+"<condition>"+Const.CR;

		retval+=indent2+XMLHandler.addTagValue("negated",    isNegated());

		if (getOperator()!=OPERATOR_NONE)
		{
			retval+=indent2+XMLHandler.addTagValue("operator",  Const.rtrim(getOperatorDesc()));
		}

		if (isAtomic())
		{
			retval+=indent2+XMLHandler.addTagValue("leftvalue",  getLeftValuename());
			retval+=indent2+XMLHandler.addTagValue("function",   getFunctionDesc());
			retval+=indent2+XMLHandler.addTagValue("rightvalue", getRightValuename());
			if (getRightExact()!=null) 
			{
				retval+="<value>  ";
				retval+=indent2+getRightExact().getXML();
				retval+="  </value>";
			}
		}
		else
		{
			retval+=indent2+"<conditions>"+Const.CR;
			for (int i=0;i<nrConditions();i++)
			{
				Condition c = getCondition(i);
				retval+=c.getXML(level+2);
			}
			retval+=indent3+"</conditions>"+Const.CR;
		}

		retval+=indent2+"</condition>"+Const.CR;

		return retval;
	}

	/**
	 * Build a new condition using an XML Document Node
	 * @param condnode
	 * @throws KettleXMLException
	 */
	public Condition(Node condnode) throws KettleXMLException
	{
		this();
		
		list = new ArrayList();
	    try
	    {
			String str_negated = XMLHandler.getTagValue(condnode, "negated");
			setNegated( "Y".equalsIgnoreCase(str_negated) );
			
			String str_operator = XMLHandler.getTagValue(condnode, "operator");
			setOperator( getOperator( str_operator ) );
			
			Node conditions = XMLHandler.getSubNode(condnode, "conditions");
			int nrconditions = XMLHandler.countNodes(conditions, "condition");
			if (nrconditions==0) // ATOMIC!
			{
				setLeftValuename( XMLHandler.getTagValue(condnode, "leftvalue") );
				setFunction( getFunction(XMLHandler.getTagValue(condnode, "function") ) );
				setRightValuename( XMLHandler.getTagValue(condnode, "rightvalue") );
				Node exactnode = XMLHandler.getSubNode(condnode, "value");
				if (exactnode!=null)
				{
					Value exact = new Value(exactnode);
					setRightExact(exact);
				}
			}
			else
			{
				for (int i=0;i<nrconditions;i++)
				{
					Node subcondnode = XMLHandler.getSubNodeByNr(conditions, "condition", i);
					Condition c = new Condition(subcondnode);
					addCondition(c);
				}
			}
	    }
	    catch(Exception e)
	    {
	        throw new KettleXMLException("Unable to create condition using xml: "+Const.CR+condnode, e);
	    }
	}

	/**
	 * Read a condition from the repository.
	 * @param rep The repository to read from
	 * @param id_condition The condition id
	 * @throws KettleException if something goes wrong.
	 */
	public Condition(Repository rep, long id_condition) throws KettleException
	{
		this();
		
		list = new ArrayList();
		try
		{
			Row r = rep.getCondition(id_condition);
			negate          = r.getBoolean("NEGATED", false);
			operator        = getOperator( r.getString("OPERATOR", null) );
			
			id = r.getInteger("ID_CONDITION", -1L);
			
			long subids[] = rep.getSubConditionIDs(id);
			if (subids.length==0)
			{
				left_valuename  = r.getString("LEFT_NAME", null);
				function        = getFunction( r.getString("CONDITION_FUNCTION", null) );
				right_valuename = r.getString("RIGHT_NAME", null);
				
				long id_value = r.getInteger("ID_VALUE_RIGHT", -1L);
				if (id_value>0)
				{
					Value v = new Value(rep, id_value);
					right_exact = v;
				}
			}
			else
			{
				for (int i=0;i<subids.length;i++)
				{
					addCondition( new Condition(rep, subids[i]) );
				}
			}
		}
		catch(KettleDatabaseException dbe)
		{
			throw new KettleException("Error loading condition from the repository (id_condition="+id_condition+")", dbe);
		}
	}

	public long saveRep(Repository rep)
		throws KettleException
	{
		return saveRep(0L, rep);
	}
	
	public long saveRep(long id_condition_parent, Repository rep)
		throws KettleException
	{
		try
		{
			id = rep.insertCondition( id_condition_parent, this );
			for (int i=0;i<nrConditions();i++)
			{
				Condition subc = getCondition(i);
				subc.saveRep(getID(), rep);
			}
			
			return getID();
		}
		catch(KettleDatabaseException dbe)
		{
			throw new KettleException("Error saving condition to the repository.", dbe);
		}
	}
	
	public String[] getUsedFields()
	{
		Hashtable fields = new Hashtable();
		getUsedFields(fields);
		
		String retval[] = new String[fields.size()];
		Enumeration keys = fields.keys();
		int i=0;
		while (keys.hasMoreElements())
		{
			retval[i] = (String)keys.nextElement();
			i++;
		}
		
		return retval;
	}
	
	public void getUsedFields(Hashtable fields)
	{
		if (isAtomic())
		{
			if (getLeftValuename()!=null) fields.put(getLeftValuename(), "-");
			if (getRightValuename()!=null) fields.put(getRightValuename(), "-");
		}
		else
		{
			for (int i=0;i<nrConditions();i++)
			{
				Condition subc = getCondition(i);
				subc.getUsedFields(fields);
			}
		}
	}
}



