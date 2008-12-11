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
 

package org.pentaho.di.trans.steps.formula;

import org.jfree.formula.lvalues.LValue;
import org.jfree.formula.parser.FormulaParser;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * @author Matt
 * @since 8-sep-2005
 *
 */
public class FormulaData extends BaseStepData implements StepDataInterface
{
    public RowForumulaContext context;
    public LValue[] lValue;
    public FormulaParser parser;
	public RowMetaInterface outputRowMeta;
	public int nrRemoved;
	public RowMetaInterface tempRowMeta;

	/**
	 * 
	 */
	public FormulaData()
	{
		super();
	}

}
