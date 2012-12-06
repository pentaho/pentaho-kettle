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
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
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
import org.pentaho.di.core.Const;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.gui.WindowProperty;

public class TipsDialog extends Dialog
{
	private static Class<?> PKG = TipsDialog.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

    private String title, message;

    private Label wlDesc;
    private Text wDesc;
    private FormData fdlDesc, fdDesc;

    private Label wlShowTips;
    private Button wShowTips;
    private FormData fdlShowTips, fdShowTips;

    private boolean showtips;

    private Shell shell;
    private Display display;
    private PropsUI props;

    private String description;
    private Font font;
    private Shell parent;

    public TipsDialog(Shell parent)
    {
        super(parent, SWT.NONE);
        props = PropsUI.getInstance();
        title = BaseMessages.getString(PKG, "TipsDialog.Dialog.Tips.Title"); // Spoon tips...
        message = BaseMessages.getString(PKG, "TipsDialog.Label.Tips"); // TIP\!
        this.parent = parent;

        description = getTip();

    }

    public String open()
    {
        display = parent.getDisplay();

        shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN);
        props.setLook(shell);
		shell.setImage(GUIResource.getInstance().getImageLogoSmall());

        showtips = props.showTips();

        FormLayout formLayout = new FormLayout();
        formLayout.marginWidth = Const.FORM_MARGIN;
        formLayout.marginHeight = Const.FORM_MARGIN;

        shell.setLayout(formLayout);
        shell.setText(title);

        int middle = props.getMiddlePct();
        int margin = Const.MARGIN;

        // From step line
        wlDesc = new Label(shell, SWT.NONE);
        wlDesc.setText(message);
        props.setLook(wlDesc);
        wlDesc.setFont(font);
        fdlDesc = new FormData();
        fdlDesc.left = new FormAttachment(0, 0);
        fdlDesc.top = new FormAttachment(0, margin);
        wlDesc.setLayoutData(fdlDesc);
        // wDesc=new Text(shell, SWT.MULTI | SWT.LEFT | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
        wDesc = new Text(shell, SWT.MULTI | SWT.LEFT | SWT.READ_ONLY | SWT.V_SCROLL | SWT.WRAP);
        wDesc.setText(description);
        props.setLook(wDesc);
        wDesc.setFont(font);
        fdDesc = new FormData();
        fdDesc.left = new FormAttachment(0, 0);
        fdDesc.top = new FormAttachment(wlDesc, margin);
        fdDesc.right = new FormAttachment(100, 0);
        fdDesc.bottom = new FormAttachment(100, -75);
        wDesc.setLayoutData(fdDesc);

        wlShowTips = new Label(shell, SWT.RIGHT);
        wlShowTips.setText(BaseMessages.getString(PKG, "TipsDialog.OptionShowTips.Label"));
        props.setLook(wlShowTips);
        wlShowTips.setFont(font);
        fdlShowTips = new FormData();
        fdlShowTips.left = new FormAttachment(0, 0);
        fdlShowTips.top = new FormAttachment(wDesc, margin * 2);
        fdlShowTips.right = new FormAttachment(middle, -margin);
        wlShowTips.setLayoutData(fdlShowTips);
        wShowTips = new Button(shell, SWT.CHECK);
        props.setLook(wShowTips);
        wShowTips.setFont(font);
        wShowTips.setSelection(showtips);
        fdShowTips = new FormData();
        fdShowTips.left = new FormAttachment(middle, 0);
        fdShowTips.top = new FormAttachment(wDesc, margin * 2);
        fdShowTips.right = new FormAttachment(100, 0);
        wShowTips.setLayoutData(fdShowTips);
        wShowTips.addSelectionListener(new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent e)
            {
                showtips = !showtips;
            }
        });

        populateButtons(shell);

        // Detect [X] or ALT-F4 or something that kills this window...
        shell.addShellListener( new ShellAdapter() { public void shellClosed(ShellEvent e) { next(); } } );

        getData();

        WindowProperty winprop = props.getScreen(shell.getText());
        if (winprop != null)
            winprop.setShell(shell);
        else
        {
            Point p = getMax(wDesc.getText());
            shell.setSize(p.x + 100, p.y + 150);
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
        if (description != null)
            wDesc.setText(description);
    }

    private void next()
    {
        wDesc.setText(getTip());
    }

    private String getTip()
    {
        int tipnr = props.getTipNr();
        String retval = Const.getTips()[tipnr];

        tipnr++;
        if (tipnr > Const.getTips().length - 1)
            tipnr = 0;
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

    /**
     * Create Close and Next button on the dialog.
     * 
     * @param parent shell on which to put the buttons.
     */
    private void populateButtons(Shell parent)
    {
        Button wOK, wNext;
        FormData fdOK, fdNext;
        Listener lsOK, lsNext;
        int width = 0;
        int margin = Const.MARGIN;

        // Some buttons
        wOK = new Button(parent, SWT.PUSH);
        wOK.setText(BaseMessages.getString(PKG, "System.Button.Close"));
        wOK.pack(true);
        Rectangle rOK = wOK.getBounds();

        wNext = new Button(parent, SWT.PUSH);
        wNext.setText(BaseMessages.getString(PKG, "TipsDialog.Button.Tips")); // &Next tip
        wNext.pack(true);
        Rectangle rNext = wNext.getBounds();

        width = (rOK.width > rNext.width ? rOK.width : rNext.width);
        width += margin;

        fdOK = new FormData();
        fdOK.left = new FormAttachment(50, -width);
        fdOK.right = new FormAttachment(50, -(margin / 2));
        fdOK.bottom = new FormAttachment(100, 0);
        wOK.setLayoutData(fdOK);
        fdNext = new FormData();
        fdNext.left = new FormAttachment(50, margin / 2);
        fdNext.right = new FormAttachment(50, width);
        fdNext.bottom = new FormAttachment(100, 0);
        wNext.setLayoutData(fdNext);

        // Add listeners
        lsNext   = new Listener() { public void handleEvent(Event e) { next(); } };
        lsOK       = new Listener() { public void handleEvent(Event e) { ok();     } };

        wOK.addListener(SWT.Selection, lsOK);
        wNext.addListener(SWT.Selection, lsNext);
    }
}