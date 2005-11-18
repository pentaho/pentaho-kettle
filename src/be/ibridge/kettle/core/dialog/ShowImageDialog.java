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

 
package be.ibridge.kettle.core.dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
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

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.Props;

/**
 * Displays an image.
 * 
 * @author Matt
 * @since 19-06-2003
 */
public class ShowImageDialog extends Dialog
{
	private Image image;
		
    private Button       wOK;
    private FormData     fdOK;
    private Listener     lsOK;
    
	private Canvas       wCanvas;
    private FormData     fdCanvas;
    
	private Shell  shell;
	private Props props;
	
	private int prefWidth = -1;
	private int prefHeight = -1;
	
	private int buttonHeight = 30;
	
    /**
     * @deprecated
     * @param parent
     * @param pr
     * @param img
     */
	public ShowImageDialog(Shell parent, Props pr, Image img)
	{
		super(parent, SWT.NONE);
		props=pr;
		image  = img;
		prefWidth = -1;
		prefHeight = -1;
	}
    
    public ShowImageDialog(Shell parent, Image img)
    {
        super(parent, SWT.NONE);
        props=Props.getInstance();
        image  = img;
        prefWidth = -1;
        prefHeight = -1;
    }

    /**
     * @deprecated
     * @param parent
     * @param pr
     * @param img
     * @param w
     * @param h
     */
	public ShowImageDialog(Shell parent, Props pr, Image img, int w, int h)
	{
		this(parent, pr, img);
		prefWidth = w + 20;
		prefHeight = h + buttonHeight + 20; // OK Button
	}
    
    public ShowImageDialog(Shell parent, Image img, int w, int h)
    {
        this(parent, img);
        prefWidth = w + 20;
        prefHeight = h + buttonHeight + 20; // OK Button
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
		shell.setText("Image display");
		
		int margin = Const.MARGIN;
		
		// Canvas
		wCanvas=new Canvas(shell, SWT.BORDER);
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
		fdCanvas.bottom= new FormAttachment(100, -buttonHeight);
		wCanvas.setLayoutData(fdCanvas);

		// Some buttons
		wOK=new Button(shell, SWT.PUSH);
		wOK.setText("  &OK  ");
		fdOK=new FormData();
		fdOK.left       = new FormAttachment(50, 0);
		fdOK.bottom     = new FormAttachment(100, 0);
		wOK.setLayoutData(fdOK);

		// Add listeners
		lsOK       = new Listener() { public void handleEvent(Event e) { ok();     } };
		
		wOK.addListener    (SWT.Selection, lsOK     );
				
		// Detect [X] or ALT-F4 or something that kills this window...
		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { ok(); } } );


		//shell.pack();
		if (prefWidth>0 && prefHeight>0)
		{
			shell.setSize(prefWidth, prefHeight);
			Rectangle r = shell.getClientArea();
			int diffx = prefWidth - r.width;
			int diffy = prefHeight - r.height;
			shell.setSize(prefWidth+diffx, prefHeight+diffy);
		}
		else
		{
			shell.setSize(400, 400);
		}

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
	
	private void ok()
	{
		dispose();
	}
	
	private void repaint(GC gc, int width, int height)
	{
		ImageData imd = image.getImageData();
		
		if (prefHeight<0 || prefWidth<0)
		{
			gc.drawImage(image, 0, 0, imd.width, imd.height,
							  0, 0, width, height
						);
		}
		else
		{
			gc.drawImage(image, 0, 0);
		}
	}

}
