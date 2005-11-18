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
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.Props;
import be.ibridge.kettle.core.WindowProperty;

public class TipsDialog extends Dialog
{
	private String title, message;
		
	private Label        wlDesc;
	private Text         wDesc;
    private FormData     fdlDesc, fdDesc;
		
	private Button wOK, wNext;
	private FormData fdOK, fdNext;
	private Listener lsOK, lsNext;

	private Label        wlShowTips;
	private Button       wShowTips;
	private FormData     fdlShowTips, fdShowTips;

	private boolean showtips;
		

	private Shell  shell;
	private Display display;
	private Props props;
	
	private String description;
	private Font  font;
	private Shell parent;
	
	public TipsDialog(Shell parent, Props pr)
	{
		super(parent, SWT.NONE);
		props=pr;
		title="Spoon tips...";
		message="TIP!";
		this.parent = parent;
		
		description=getTip();

	}

	public String open()
	{
		display = parent.getDisplay();

		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN);
 		props.setLook(shell);

		showtips=props.showTips();

		FormLayout formLayout = new FormLayout ();
		formLayout.marginWidth  = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText(title);
		
		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		// From step line
		wlDesc=new Label(shell, SWT.NONE);
		wlDesc.setText(message);
 		props.setLook(wlDesc);
		wlDesc.setFont(font);
		fdlDesc=new FormData();
		fdlDesc.left = new FormAttachment(0, 0);
		fdlDesc.top  = new FormAttachment(0, margin);
		wlDesc.setLayoutData(fdlDesc);
		//wDesc=new Text(shell, SWT.MULTI | SWT.LEFT | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		wDesc=new Text(shell, SWT.MULTI | SWT.LEFT );
		wDesc.setText(description);
 		props.setLook(wDesc);
		wDesc.setFont(font);
		fdDesc=new FormData();
		fdDesc.left  = new FormAttachment(0, 0);
		fdDesc.top   = new FormAttachment(wlDesc, margin);
		fdDesc.right = new FormAttachment(100, 0);
		fdDesc.bottom= new FormAttachment(100, -75);
		wDesc.setLayoutData(fdDesc);

		wlShowTips=new Label(shell, SWT.RIGHT);
		wlShowTips.setText("Show tips at startup? ");
 		props.setLook(wlShowTips);
		wlShowTips.setFont(font);
		fdlShowTips=new FormData();
		fdlShowTips.left = new FormAttachment(0, 0);
		fdlShowTips.top  = new FormAttachment(wDesc, margin*2);
		fdlShowTips.right= new FormAttachment(middle, -margin);
		wlShowTips.setLayoutData(fdlShowTips);
		wShowTips=new Button(shell, SWT.CHECK);
 		props.setLook(wShowTips);
		wShowTips.setFont(font);
		wShowTips.setSelection(showtips);
		fdShowTips=new FormData();
		fdShowTips.left = new FormAttachment(middle, 0);
		fdShowTips.top  = new FormAttachment(wDesc, margin*2);
		fdShowTips.right= new FormAttachment(100, 0);
		wShowTips.setLayoutData(fdShowTips);
		wShowTips.addSelectionListener(new SelectionAdapter() 
			{
				public void widgetSelected(SelectionEvent e) 
				{
					showtips=!showtips;
				}
			}
		);

		// Some buttons
		wOK=new Button(shell, SWT.PUSH);
		wOK.setText("  &Close  ");
		wNext=new Button(shell, SWT.PUSH);
		wNext.setText("  &Next tip");
		fdOK=new FormData();
		fdOK.left       = new FormAttachment(33, 0);
		fdOK.bottom     = new FormAttachment(100, 0);
		wOK.setLayoutData(fdOK);
		fdNext=new FormData();
		fdNext.left   = new FormAttachment(66, 0);
		fdNext.bottom = new FormAttachment(100, 0);
		wNext.setLayoutData(fdNext);

		// Add listeners
		lsNext   = new Listener() { public void handleEvent(Event e) { next(); } };
		lsOK       = new Listener() { public void handleEvent(Event e) { ok();     } };
		
		wOK.addListener    (SWT.Selection, lsOK     );
		wNext.addListener(SWT.Selection, lsNext );
		
		// Detect [X] or ALT-F4 or something that kills this window...
		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { next(); } } );
		
		getData();
		
		WindowProperty winprop = props.getScreen(shell.getText());
		if (winprop!=null) winprop.setShell(shell); else
		{
			Point p = getMax(wDesc.getText());
			shell.setSize(p.x+100, p.y+150);
		} 

		
		shell.open();
		while (!shell.isDisposed())
		{
				if (!display.readAndDispatch()) display.sleep();
		}
		return description;
	}

	public void dispose()
	{
		props.setShowTips(showtips);
		shell.dispose();
	}
	
	public void getData()
	{
		if (description!=null) wDesc.setText(description);
	}
	
	private void next()
	{
		wDesc.setText(getTip());
	}
	
	private String getTip()
	{
		int tipnr=props.getTipNr();		
		String retval=Const.tips[tipnr];

		tipnr++;		
		if (tipnr>Const.tips.length-1) tipnr=0;
		props.setTipNr(tipnr);

		return retval;
	}
	
	private Point getMax(String str)
	{
		Image img = new Image(display, 1, 1);
		GC gc = new GC(img);
		Point p = gc.textExtent(str, SWT.DRAW_DELIMITER | SWT.DRAW_TAB);
		
		gc.dispose();
		img.dispose();
		
		return p;
	}
	
	private void ok()
	{
		dispose();
	}
}
