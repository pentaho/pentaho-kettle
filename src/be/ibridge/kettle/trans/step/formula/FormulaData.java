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
 

package be.ibridge.kettle.trans.step.formula;

import java.util.Hashtable;
import java.util.Map;

import org.jfree.formula.lvalues.LValue;
import org.jfree.formula.parser.FormulaParser;

import be.ibridge.kettle.core.formula.RowForumulaContext;
import be.ibridge.kettle.trans.step.BaseStepData;
import be.ibridge.kettle.trans.step.StepDataInterface;

/**
 * @author Matt
 * @since 8-sep-2005
 *
 */
public class FormulaData extends BaseStepData implements StepDataInterface
{
    public Map indexCache;
    public RowForumulaContext context;
    public LValue[] lValue;
    public FormulaParser parser;

	/**
	 * 
	 */
	public FormulaData()
	{
		super();
        
        indexCache = new Hashtable();
	}

}
