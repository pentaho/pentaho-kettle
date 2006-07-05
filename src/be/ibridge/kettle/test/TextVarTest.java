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
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.Props;
import be.ibridge.kettle.core.util.EnvUtil;
import be.ibridge.kettle.core.widget.LabelTextVar;



/*
 * Created on 17-may-2006
 *
 * @author Matt
 *
 */

public class TextVarTest 
{

	public static void main(String[] args) 
	{
        LabelTextVar  wTWV;
        FormData      fdTWV;

        ///////////////////////////////////////////////////////////////////////////////////////////////
        
        EnvUtil.environmentInit();
        
        final Display display = new Display();
        final Shell shell = new Shell(display);
        Props.init(display, Props.TYPE_PROPERTIES_SPOON);
        Props props = Props.getInstance();
        
        FormLayout formLayout = new FormLayout();
        formLayout.marginWidth  = Const.FORM_MARGIN;
        formLayout.marginHeight = Const.FORM_MARGIN;

        shell.setLayout(formLayout);
        shell.setText("Text With Variable test");
        
        ///////////////////////////////////////////////////////////////////////////////////////////////
        
        wTWV=new LabelTextVar(shell, "Text With Variable", "Click on this button to insert a variable");
        props.setLook(wTWV);
        fdTWV=new FormData();
        fdTWV.left = new FormAttachment(0, 0);
        fdTWV.top  = new FormAttachment(0, 0);
        fdTWV.right= new FormAttachment(100, 0);
        wTWV.setLayoutData(fdTWV);
        
        shell.layout();

        
        // Detect X or ALT-F4 or something that kills this window...
        shell.addShellListener( new ShellAdapter() { public void shellClosed(ShellEvent e) { shell.dispose(); } } );

        shell.open();
        while (!shell.isDisposed())
        {
                if (!display.readAndDispatch()) display.sleep();
        }
	}
}
