/*
 * $Header: TeraFastAboutDialog.java
 * $Revision:
 * $Date: 24.06.2009 15:02:38
 *
 * ==========================================================
 * COPYRIGHTS
 * ==========================================================
 * Copyright (c) 2009 Aschauer EDV.  All rights reserved. 
 * This software was developed by Aschauer EDV and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Terafast 
 * PDI Plugin. The Initial Developer is Aschauer EDV.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
*/

package org.pentaho.di.ui.trans.steps.terafast;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.steps.terafast.TeraFastMeta;

/**
 * @author <a href="mailto:michael.gugerell@aschauer-edv.at">Michael Gugerell(asc145)</a>
 * 
 */
public class TeraFastAboutDialog {
    
	private static Class<?> PKG = TeraFastMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$
    
    private static final int DEFAULT_INDENT = 20;
    
    private Shell dialog;
    private Link ascLink;
    private Label buildInfo;
    private Label iconLabel;
    
    /**
     * 
     * @param shell the shell.
     */
    public TeraFastAboutDialog(final Shell shell) {
        this.dialog = new Shell(shell, SWT.BORDER | SWT.CLOSE | SWT.APPLICATION_MODAL);
        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 2;
        
        this.dialog.setLayout(gridLayout);
        this.dialog.setText(BaseMessages.getString(PKG, "TeraFastDialog.About.Shell.Title"));
        this.dialog.setImage(shell.getImage());
        
        this.buildIconCell();
        this.buildPluginInfoCell();
        this.buildOkButton();
        
        this.dialog.pack();
        Rectangle shellBounds = shell.getBounds();
        Point dialogSize = this.dialog.getSize();

        this.dialog.setLocation(
          shellBounds.x + (shellBounds.width - dialogSize.x) / 2,
          shellBounds.y + (shellBounds.height - dialogSize.y) / 2);
    }
    
    /**
     * open the dialog.
     */
    public void open() {
        this.dialog.open();
    }
    
    /**
     * build Ok Button.
     */
    protected void buildOkButton() {
        Button ok = new Button(this.dialog, SWT.PUSH);
        ok.setText(BaseMessages.getString(PKG, "TeraFastDialog.About.Plugin.Close"));
        GridData grdData = new GridData(GridData.HORIZONTAL_ALIGN_CENTER);
        grdData.horizontalSpan = 2;
        grdData.verticalIndent = DEFAULT_INDENT;
        grdData.horizontalIndent = DEFAULT_INDENT;
        ok.setLayoutData(grdData);
        
        ok.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event arg0) {
                dialog.dispose();
            }
        });
    }
    
    /**
     * build icon cell
     */
    protected void buildIconCell() {
        Image icon = Display.getCurrent().getSystemImage(SWT.ICON_INFORMATION);
        this.iconLabel = new Label(this.dialog, SWT.NONE);
        this.iconLabel.setImage(icon);
        GridData grdData = new GridData();
        grdData.horizontalIndent = DEFAULT_INDENT;
        grdData.verticalIndent = DEFAULT_INDENT;
        this.iconLabel.setLayoutData(grdData);
    }
    
    /**
     * build plugin info cell.
     */
    protected void buildPluginInfoCell() {
        this.ascLink = new Link(this.dialog, SWT.NONE);
        this.ascLink.setText(BaseMessages.getString(PKG, "TeraFastDialog.About.Plugin.Info"));
        GridData grdData = new GridData();
        grdData.horizontalIndent = DEFAULT_INDENT;
        grdData.verticalIndent = DEFAULT_INDENT;
        this.ascLink.setLayoutData(grdData);

        
        this.ascLink.addListener(SWT.Selection, new Listener() {
            public void handleEvent(final Event event) {
                Program.launch(event.text);
            }
        });
    }
}
