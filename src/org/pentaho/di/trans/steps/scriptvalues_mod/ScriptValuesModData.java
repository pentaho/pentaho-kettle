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

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.pentaho.di.compatibility.Value;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;


/**
 * @author Matt
 * @since 24-jan-2005
 *
 */
public class ScriptValuesModData extends BaseStepData implements StepDataInterface
{
	public Context cx;
	public Scriptable scope;
	public Script script;
	
	public int fields_used[];
	public Value values_used[];
    
    public RowMetaInterface outputRowMeta;
	
	/**
	 * 
	 */
	public ScriptValuesModData()
	{
		super();
		cx=null;
		fields_used=null;
	}
	
	public void check(int i){
		System.out.println(i);
	}	
}