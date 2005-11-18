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
 

package be.ibridge.kettle.test;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import be.ibridge.kettle.core.Condition;
import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.Props;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.dialog.EnterConditionDialog;
import be.ibridge.kettle.core.value.Value;



/*
 * Created on 8-jul-2004
 *
 * @author Matt
 *
 */

public class ConditionTest 
{

	public static void main(String[] args) 
	{
		Row r = new Row();
		r.addValue(new Value("A", "aaaa"));
		r.addValue(new Value("B", false));
		r.addValue(new Value("C", 12.34));
		r.addValue(new Value("D", 77L));
		
		Condition cb1 = new Condition(Condition.OPERATOR_NONE, "D", Condition.FUNC_EQUAL, null, new Value("other", 77L));
		System.out.println("ConditionBasic cb1 : "+cb1);
		System.out.println("evaluates to : "+cb1.evaluate(r));

		Condition cb2 = new Condition("A", Condition.FUNC_SMALLER, null, new Value("other", "bbb"));
		System.out.println("ConditionBasic cb2 : "+cb2);
		System.out.println("evaluates to : "+cb2.evaluate(r));
		
		Condition two = new Condition();
		two.addCondition(cb1);
		two.addCondition(cb2);
		cb2.setOperator(Condition.OPERATOR_XOR);
		
		System.out.println("two : \n"+two);
		System.out.println("two result = "+two.evaluate(r));

		Condition cb3 = new Condition("B", Condition.FUNC_EQUAL, null, new Value("other", false));
		System.out.println("ConditionBasic cb3 : "+cb3);
		System.out.println("evaluates to : "+cb3.evaluate(r));

		Condition cb4 = new Condition("C", Condition.FUNC_EQUAL, null, new Value("other", 12.34));
		System.out.println("ConditionBasic cb4 : "+cb4);
		System.out.println("evaluates to : "+cb4.evaluate(r));

		Condition two2 = new Condition();
		two2.addCondition(cb3);
		two2.addCondition(cb4);
		cb4.setOperator(Condition.OPERATOR_AND);
		
		System.out.println("two2 : \n"+two2);
		System.out.println("two2 result = "+two2.evaluate(r));
		
		Condition three = new Condition();
		
		three.addCondition(two);
		three.addCondition(two2);
		two2.setOperator(Condition.OPERATOR_XOR);
		three.setOperator(Condition.OPERATOR_NOT);

		System.out.println("three : \n"+three);
		System.out.println("three result = "+three.evaluate(r));
		
		Display display = new Display();
		Shell shell = new Shell(display, SWT.RESIZE | SWT.MAX | SWT.MIN);
		
		if (!Props.isInitialized()) Props.init(display, Props.TYPE_PROPERTIES_SPOON);
		Props props = Props.getInstance();
		
		LogWriter log = LogWriter.getInstance(LogWriter.LOG_LEVEL_BASIC);
		
		EnterConditionDialog ecd = new EnterConditionDialog(shell, props, SWT.NONE, r, cb1);
		ecd.open();
		
		System.out.println("XML="+Const.CR+cb1.getXML());
		props.saveProps();
		
		log.close();
	}
}
