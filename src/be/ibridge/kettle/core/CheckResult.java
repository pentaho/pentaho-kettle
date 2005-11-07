 /**********************************************************************
 **                                                                   **
 **               This code belongs to the KETTLE project.            **
 **                                                                   **
 ** It belongs to, is maintained by and is copyright 1999-2005 by     **
 **                                                                   **
 **      i-Bridge bvba                                                **
 **      Fonteinstraat 70                                             **
 **      9400 OKEGEM                                                  **
 **      Belgium                                                      **
 **      http://www.kettle.be                                         **
 **      info@kettle.be                                               **
 **                                                                   **
 **********************************************************************/
 
package be.ibridge.kettle.core;
import be.ibridge.kettle.trans.step.StepMeta;

/**
 * This class is used to store results of transformation and step verifications.
 * 
 * @author Matt
 * @since 11-01-04
 *
 */
public class CheckResult
{
	public static final int TYPE_RESULT_NONE    = 0;
	public static final int TYPE_RESULT_OK      = 1;
	public static final int TYPE_RESULT_COMMENT = 2;
	public static final int TYPE_RESULT_WARNING = 3;
	public static final int TYPE_RESULT_ERROR   = 4;
	
	public static final String type_desc[] =
		{
			"",
			"OK",
			"Remark",
			"Warning",
			"Error"	
		};
	
	private int    type;
	private String text;
	private StepMeta stepMeta;
	
	public CheckResult()
	{
		this(TYPE_RESULT_NONE, "", null);
	}
	
	public CheckResult(int t, String s, StepMeta stepMeta)
	{
		type = t;
		text = s;
		this.stepMeta=stepMeta;
	}
	
	public int getType()
	{
		return type;
	}
	
	public String getTypeDesc()
	{
		return type_desc[type];
	}
	
	public String getText()
	{
		return text;
	}
	
	public StepMeta getStepInfo()
	{
		return stepMeta;
	}
	
	public String toString()
	{
		if (stepMeta!=null) return type_desc[type]+" : "+text+" ("+stepMeta.getName()+")";
		return type_desc[type]+" : "+text;
	}
}
