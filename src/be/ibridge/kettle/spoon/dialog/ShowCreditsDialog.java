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

 
/*
 * Created on 19-jun-2003
 *
 */

package be.ibridge.kettle.spoon.dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.Props;
import be.ibridge.kettle.trans.step.BaseStepDialog;

public class ShowCreditsDialog extends Dialog
{
	private Image image;
		
	private Canvas       wCanvas;
    private FormData     fdCanvas;

	private Text         wCredits;
	private FormData     fdCredits;    
		
	private Button wOK;
	private Listener lsOK;

	private Shell  shell;
	private Props props;
	
	public ShowCreditsDialog(Shell parent, Props pr, Image img)
	{
		super(parent, SWT.NONE);
		props=pr;
		image  = img;
	}

	public void open()
	{
		Shell parent = getParent();
		Display display = parent.getDisplay();
		
		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.RESIZE | SWT.MAX | SWT.MIN);
 		props.setLook(shell);

		FormLayout formLayout = new FormLayout ();
		formLayout.marginWidth  = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText("Credits");
		
		int margin = Const.MARGIN;

		// Close...
		wOK=new Button(shell, SWT.PUSH);
		wOK.setText("  &Close  ");
		BaseStepDialog.positionBottomButtons(shell, new Button[] { wOK }, margin, null);
		
		wCredits = new Text(shell, SWT.MULTI | SWT.READ_ONLY | SWT.CENTER);
		wCredits.setText(
            "Kettle would have been a lot harder to build if it wasn't for the help of these fine libraries :" +Const.CR +
            Const.CR+
			"SWT: The Standard Windowing Toolkit from IBM's Eclipse project." + Const.CR +
			"     You can find more information on SWT and the Eclipse project at http://www.eclipse.org" +Const.CR+ 
			Const.CR+			
			"Rhino: Rhino is part of the Mozilla project and implements the complete JavaScript 1.5 specifications (Standard ECMA-262 ECMAScript, Edition 3)."+Const.CR+
			"       This library can be found on the mozilla website: http://www.mozilla.org"+Const.CR+
			Const.CR+			
			"jDTS:  Because it is illegal to include the Microsoft JDBC driver for SQL Server, we switched to jTDS with great success."+Const.CR+
			"       jTDS is released under the GNU LGPL and can therefor be included in this distribution."+Const.CR+
			"       jTDS can be found on the Sourceforge website: http://jtds.sourceforge.net/"+Const.CR+
			Const.CR+
			"Java:  You're running this software using the Java 2 Standard Edition (J2SE) Runtime Environment from Sun." +Const.CR+
			"       For more info on Java, visit http://www.javasoft.com"+Const.CR
			);
 		props.setLook(wCredits);
		fdCredits=new FormData();
		fdCredits.left  = new FormAttachment(0, 0);
		fdCredits.right = new FormAttachment(100, 0);
		fdCredits.bottom= new FormAttachment(wOK, -margin);
		wCredits.setLayoutData(fdCredits);

		// Canvas
		wCanvas=new Canvas(shell, SWT.BORDER | SWT.NO_BACKGROUND);
 		props.setLook(wCanvas);
		wCanvas.addPaintListener(new PaintListener() 
			{
				public void paintControl(PaintEvent pe) 
				{
					repaint(pe.gc, pe.width, pe.height);   
				}
			}
		)
		;
		fdCanvas=new FormData();
		fdCanvas.left  = new FormAttachment(0, 0);
		fdCanvas.top   = new FormAttachment(0, margin);
		fdCanvas.right = new FormAttachment(100, 0);
		fdCanvas.bottom= new FormAttachment(wCredits, -margin);
		wCanvas.setLayoutData(fdCanvas);

		// Add listeners
		lsOK       = new Listener() { public void handleEvent(Event e) { close();     } };
		wOK.addListener    (SWT.Selection, lsOK     );
	
		// Detect [X] or ALT-F4 or something that kills this window...
		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { close(); } } );

		shell.layout();
		shell.setSize(640, 480);

		getData();
		
		shell.open();
		while (!shell.isDisposed())
		{
				if (!display.readAndDispatch()) display.sleep();
		}
		return;
	}

	public void dispose()
	{
		shell.dispose();
	}
	
	public void getData()
	{
	}
	
	private void close()
	{
		dispose();
	}
	
	private void repaint(GC gc, int width, int height)
	{
		Rectangle irect = image.getBounds();
		Rectangle crect = wCanvas.getBounds();
		
		gc.drawImage(image, 0, 0, irect.width, irect.height, 0, 0, crect.width, crect.height);
	}

}
