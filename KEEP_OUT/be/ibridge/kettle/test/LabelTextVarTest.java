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
import be.ibridge.kettle.core.widget.LabelTextVar;



/*
 * Created on 17-may-2006
 *
 * @author Matt
 *
 */

public class LabelTextVarTest 
{

	public static void main(String[] args) 
	{
        LabelTextVar  wLTV;
        FormData      fdLTV;

        ///////////////////////////////////////////////////////////////////////////////////////////////
        
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
        
        wLTV=new LabelTextVar(shell, "This is a sample label", "This is the tooltip of that sample label, a bit longer than the label");
        props.setLook(wLTV);
        fdLTV=new FormData();
        fdLTV.left = new FormAttachment(0, 0);
        fdLTV.top  = new FormAttachment(0, 0);
        fdLTV.right= new FormAttachment(100, 0);
        wLTV.setLayoutData(fdLTV);
        
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
