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

package org.pentaho.di.ui.core.dialog;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.Condition;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.gui.WindowProperty;
import org.pentaho.di.ui.core.widget.ConditionEditor;
import org.pentaho.di.ui.trans.step.BaseStepDialog;



/**
 * This dialog allows you to enter a condition in a graphical way.
 * 
 * @author Matt
 * @since 29-07-2004
 */
public class EnterConditionDialog extends Dialog 
{
	private static Class<?> PKG = EnterConditionDialog.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private PropsUI props;
	
	private Shell     shell;
	private ConditionEditor wCond;
	 
	private Button    wOK;
	private Button    wCancel;
	
	private Condition condition;
	private RowMetaInterface fields;				

	public EnterConditionDialog(Shell parent, int style, RowMetaInterface fields, Condition condition)
	{
		super(parent, style);
		this.props     = PropsUI.getInstance();
		this.fields    = fields;
		this.condition = condition;
	}

	public Condition open()
	{
		Shell parent = getParent();
		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN);
 		props.setLook(shell);
		shell.setText(BaseMessages.getString(PKG, "EnterConditionDialog.Title"));
		shell.setImage(GUIResource.getInstance().getImageLogoSmall());
		
		FormLayout formLayout = new FormLayout ();
		formLayout.marginWidth  = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;
		
		shell.setLayout (formLayout);
 		
 		// Condition widget
		wCond = new ConditionEditor(shell, SWT.NONE, condition, fields);
 		props.setLook(wCond, Props.WIDGET_STYLE_FIXED);
 		
 		if (!getData()) return null;
 		
 		// Buttons
		wOK = new Button(shell, SWT.PUSH); 
		wOK.setText(BaseMessages.getString(PKG, "System.Button.OK"));
		
		wCancel = new Button(shell, SWT.PUSH);
		wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel"));
	
		FormData fdCond      = new FormData(); 
		
		int margin =  Const.MARGIN*2;

		fdCond.left   = new FormAttachment(0, 0); // To the right of the label
		fdCond.top    = new FormAttachment(0, 0);
		fdCond.right  = new FormAttachment(100, 0);
		fdCond.bottom = new FormAttachment(100, -50);
		wCond.setLayoutData(fdCond);
        
        BaseStepDialog.positionBottomButtons(shell, new Button[] { wOK, wCancel }, margin, null);
	
		// Add listeners
		wCancel.addListener(SWT.Selection, new Listener ()
			{
				public void handleEvent (Event e) 
				{
					condition=null;
					dispose();
				}
			}
		);

		wOK.addListener(SWT.Selection, new Listener ()
			{
				public void handleEvent (Event e) 
				{
					handleOK();
				}
			}
		);
				

		BaseStepDialog.setSize(shell);

		shell.open();
		Display display = parent.getDisplay();
		while (!shell.isDisposed()) 
		{
			if (!display.readAndDispatch()) display.sleep();
		}
		return condition;
	}
	
	private boolean getData()
	{
		return true;
	}

	public void dispose()
	{
		props.setScreen(new WindowProperty(shell));
		shell.dispose();
	}
	
	public void handleOK()
	{
		if (wCond.getLevel()>0) wCond.goUp();
		else dispose();
	}
}
