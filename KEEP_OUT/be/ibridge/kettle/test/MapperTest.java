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
import be.ibridge.kettle.core.SourceToTargetMapping;
import be.ibridge.kettle.core.dialog.EnterMappingDialog;
import be.ibridge.kettle.core.exception.KettleException;



/*
 * Created on 23-3-2006
 *
 * @author Matt
 *
 */

public class MapperTest 
{

	public static void main(String[] args) throws KettleException
	{
		String source[] = { "src1", "src2", "src3", "src4", "src5", "src6", "src7" }; 
        String target[] = { "tgt1", "tgt2", "tgt3", "tgt4", "tgt5", "tgt6", "tgt7" }; 

        Display display = new Display();

        Shell shell = new Shell(display, SWT.RESIZE | SWT.MAX | SWT.MIN);

        LogWriter.getInstance("C:\\Temp\\mapper-test.log", true, LogWriter.LOG_LEVEL_BASIC);
		if (!Props.isInitialized()) Props.init(display, Props.TYPE_PROPERTIES_SPOON);
		
        EnterMappingDialog dialog = new EnterMappingDialog(shell, source, target );
        ArrayList mappings = dialog.open();
        if (mappings!=null)
        {
            for (int i=0;i<mappings.size();i++)
            {
                SourceToTargetMapping mapping = (SourceToTargetMapping) mappings.get(i);
                System.out.println("#"+(i+1)+"  "+source[mapping.getSourcePosition()]+" --> "+target[mapping.getTargetPosition()]);
            }  
        }
	}
}
