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
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.Props;
import be.ibridge.kettle.core.widget.TextVar;



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
        Label             wlTWV;
        TextVar  wTWV;
        FormData          fdlTWV, fdTWV;

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
        
        int middle = Const.MIDDLE_PCT;
        int margin = Const.MARGIN;
        
        wlTWV=new Label(shell, SWT.RIGHT);
        wlTWV.setText("Text With Variable: ");
        props.setLook(wlTWV);
        fdlTWV=new FormData();
        fdlTWV.left = new FormAttachment(0, 0);
        fdlTWV.right= new FormAttachment(middle, -margin);
        fdlTWV.top  = new FormAttachment(0, 0);
        wlTWV.setLayoutData(fdlTWV);
        wTWV=new TextVar(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(wTWV);
        fdTWV=new FormData();
        fdTWV.left = new FormAttachment(middle, 0);
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
