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
import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.Props;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.dialog.SelectRowDialog;
import be.ibridge.kettle.core.value.Value;



/*
 * Created on 8-jul-2004
 *
 * @author Matt
 *
 */

public class SelectRowTest 
{
	public static void main(String[] args) 
	{
		Row r1 = new Row();
		r1.addValue(new Value("A", "aaaa"));
		r1.addValue(new Value("B", false));
		r1.addValue(new Value("C", 12.34));
		r1.addValue(new Value("D", 77L));

        Row r2 = new Row();
        r2.addValue(new Value("A", "bbbb"));
        r2.addValue(new Value("B", false));
        r2.addValue(new Value("C", 23.45));
        r2.addValue(new Value("D", 88L));

        Row r3 = new Row();
        r3.addValue(new Value("A", "cccc"));
        r3.addValue(new Value("B", true));
        r3.addValue(new Value("C", 34.56));
        r3.addValue(new Value("D", 99L));
        
        ArrayList rows = new ArrayList();
        rows.add(r1);
        rows.add(r2);
        rows.add(r3);

        // Get sane logging
        LogWriter log = LogWriter.getInstance(LogWriter.LOG_LEVEL_BASIC);
        
        // A new display/shell
        Display display = new Display();
        Shell shell = new Shell(display);
        
        // Load Spoon properties
        Props.init(display, Props.TYPE_PROPERTIES_SPOON);

        SelectRowDialog d = new SelectRowDialog(shell, SWT.NONE, "Select a row", rows);
        Row selected = d.open();
        if (selected!=null)
        {
            log.logBasic("Selected row", selected.toString());
        }
	}
}
