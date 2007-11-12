 /* Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.*/

 
/*
 * Created on 19-jun-2003
 *
 */

package org.pentaho.di.ui.spoon.dialog;
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
import org.pentaho.di.core.Const;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.spoon.dialog.Messages;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.ui.core.PropsUI;

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
	private PropsUI props;
	
	public ShowCreditsDialog(Shell parent, PropsUI pr, Image img)
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
		shell.setImage(GUIResource.getInstance().getImageLogoSmall());

		FormLayout formLayout = new FormLayout ();
		formLayout.marginWidth  = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText(Messages.getString("ShowCreditsDialog.Dialog.Credits.Title"));
		
		int margin = Const.MARGIN;

		// Close...
		wOK=new Button(shell, SWT.PUSH);
		wOK.setText(Messages.getString("System.Button.Close"));
		BaseStepDialog.positionBottomButtons(shell, new Button[] { wOK }, margin, null);
		
		wCredits = new Text(shell, SWT.MULTI | SWT.READ_ONLY | SWT.CENTER);
		wCredits.setText(Messages.getString("ShowCreditsDialog.Dialog.Credits.Message"));
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
