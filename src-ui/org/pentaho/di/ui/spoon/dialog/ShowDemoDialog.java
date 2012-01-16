/*******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.ui.spoon.dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
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
import org.pentaho.di.core.Const;
import org.pentaho.di.ui.core.PropsUI;

/**
 * A dialog box showing the explanation of the demo mode, license, etc.
 *  
 * @author Matt
 *
 */
public class ShowDemoDialog extends Dialog
{
	private Image image;
		
	private Canvas       wCanvas;
    private FormData     fdCanvas;

	private StyledText   wLicence;
	private FormData     fdLicence;    
		
	private Button wOK, wCancel;
	private FormData fdOK, fdCancel;
	private Listener lsOK, lsCancel;

	private Shell  shell;
	private PropsUI props;
	
	private boolean retval;
	
	public ShowDemoDialog(Shell parent, PropsUI pr, Image img)
	{
		super(parent, SWT.NONE);
		props=pr;
		image  = img;
		retval=false;
	}

	public boolean open()
	{
		Shell parent = getParent();
		Display display = parent.getDisplay();
		
		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.RESIZE | SWT.MAX | SWT.MIN);
 		props.setLook(shell);

		FormLayout formLayout = new FormLayout ();
		formLayout.marginWidth  = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText("License");
		
		int margin = Const.MARGIN;

		// Some buttons, in random order?
		if (Math.random()*10 > 5.0)
		{
			wOK=new Button(shell, SWT.PUSH);
			wOK.setText("  I Agree  ");
			wCancel=new Button(shell, SWT.PUSH);
			wCancel.setText("  No thanks  ");
		
			fdOK=new FormData();
			fdOK.left       = new FormAttachment(40, 0);
			fdOK.bottom     = new FormAttachment(100, -margin);
			wOK.setLayoutData(fdOK);

			fdCancel=new FormData();
			fdCancel.left       = new FormAttachment(wOK, 10);
			fdCancel.bottom     = new FormAttachment(100, -margin);
			wCancel.setLayoutData(fdCancel);
		}
		else
		{
			wCancel=new Button(shell, SWT.PUSH);
			wCancel.setText("  No thanks  ");
			wOK=new Button(shell, SWT.PUSH);
			wOK.setText("  I Aggree  ");
		
			fdCancel=new FormData();
			fdCancel.left       = new FormAttachment(40, 0);
			fdCancel.bottom     = new FormAttachment(100, -margin);
			wCancel.setLayoutData(fdCancel);

			fdOK=new FormData();
			fdOK.left       = new FormAttachment(wCancel, 10);
			fdOK.bottom     = new FormAttachment(100, -margin);
			wOK.setLayoutData(fdOK);
		}
		
		wLicence = new StyledText(shell, SWT.MULTI | SWT.READ_ONLY | SWT.WRAP );
        String message = 
            "Thank you for your time trying out Kettle." +Const.CR +
            Const.CR+
			"If you find that this software is useful, licenses can be obtained at very low prices." + Const.CR +
			"Kettle is free for students, schools and charities." + Const.CR + Const.CR+
			"Without a licence, you can use this software for 30 days."+Const.CR+
			"After 30 days, you will need to obtain a license."+Const.CR+
			Const.CR+
			"For more information, support and/or advice, send an e-mail to info@kettle.be " +Const.CR+
			"or visit our website at http://www.kettle.be"+Const.CR+
			Const.CR+
			"This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE."+Const.CR+
            Const.CR+
            "Kettle is (c) 2001-2005 by i-Bridge bvba : http://www.kettle.be"
            ;
        
        wLicence.setText( message );
        
        String boldPiece = "Without a licence, you can use this software for 30 days."+Const.CR+"After 30 days, you will need to obtain a license."+Const.CR;
        int fromBold     = message.indexOf(boldPiece);
        int boldLength   = boldPiece.length();
        StyleRange boldStyleRange = new StyleRange();
        boldStyleRange.start = fromBold;
        boldStyleRange.length = boldLength;
        boldStyleRange.fontStyle = SWT.BOLD;
        wLicence.setStyleRange(boldStyleRange);

        props.setLook(wLicence);        

        // Make font larger
        FontData fontData = display.getSystemFont().getFontData()[0];
        fontData.setHeight(fontData.getHeight()+4);
        final Font font = new Font(display, fontData);
        wLicence.setFont(font);
        shell.addDisposeListener(new DisposeListener() { public void widgetDisposed(DisposeEvent e) { font.dispose(); } } );
        
		fdLicence=new FormData();
		fdLicence.left  = new FormAttachment(0, 0);
		fdLicence.right = new FormAttachment(100, 0);
		fdLicence.bottom= new FormAttachment(wOK, -margin);
		wLicence.setLayoutData(fdLicence);

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
		fdCanvas.bottom= new FormAttachment(wLicence, -margin);
		wCanvas.setLayoutData(fdCanvas);

		// Add listeners
		lsOK       = new Listener() { public void handleEvent(Event e) { ok();      } };
		lsCancel   = new Listener() { public void handleEvent(Event e) { cancel();  } };
		
		wOK.addListener    (SWT.Selection, lsOK     );
		wCancel.addListener(SWT.Selection, lsCancel );
		
		// Detect [X] or ALT-F4 or something that kills this window...
		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { cancel(); } } );

		shell.setSize(640, 700);

        getData();
		
        shell.layout();
        
        shell.open();
		while (!shell.isDisposed())
		{
				if (!display.readAndDispatch()) display.sleep();
		}
		return retval;
	}

	public void dispose()
	{
		shell.dispose();
	}
	
	public void getData()
	{
	}
	
	private void ok()
	{
		retval=true;
		dispose();
	}
	
	private void cancel()
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
