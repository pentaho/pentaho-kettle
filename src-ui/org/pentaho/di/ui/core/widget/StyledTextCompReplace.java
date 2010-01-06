/*
 * Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
*/
 /**********************************************************************
 **                                                                   **
 ** This Script has been developed for more StyledText Enrichment     **
 ** December-2006 by proconis GmbH / Germany                          **
 **                                                                   ** 
 ** http://www.proconis.de                                            **
 ** info@proconis.de                                                  **
 **                                                                   **
 **********************************************************************/

package org.pentaho.di.ui.core.widget;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.ui.core.gui.GUIResource;


public class StyledTextCompReplace extends org.eclipse.swt.widgets.Dialog {


	private Shell sShell = null;
	private Text searchText;
	private Text replaceText;
	private StyledText text;
	
	private Button btnNext;
	private Button btnCancel;
	private Button btnReplace;
	private Button btnReplaceAll;
	
	private Button btnIgnoreCase;

	
	public StyledTextCompReplace(Shell parent, StyledText text){
		super(parent);
		this.text = text;
	}
	
	/**
	 * This method initializes sShell
	 */
public void open() {
		
		Shell parent = getParent();
		sShell = new Shell(parent, SWT.DIALOG_TRIM | SWT.NO_REDRAW_RESIZE | SWT.APPLICATION_MODAL  );
		sShell.setImage(GUIResource.getInstance().getImageSpoon());
		sShell.setText(Messages.getString("Widget.Styled.CompReplace.Replace"));
		sShell.setSize(new Point(351, 178));
		FormLayout formLayout = new FormLayout ();
		sShell.setLayout(formLayout);
		
		searchText = new Text(sShell,  SWT.SINGLE | SWT.BORDER);
		FormData frmData = new FormData();
		frmData.left = new FormAttachment(0, 70);
		frmData.top  = new FormAttachment(12, 0);
		frmData.width = 168;
		searchText.setLayoutData(frmData);
		
		replaceText = new Text(sShell,  SWT.SINGLE | SWT.BORDER);
		frmData = new FormData();
		frmData.left = new FormAttachment(0, 70);
		frmData.top  = new FormAttachment(searchText, 8);
		frmData.width = 168;
		replaceText.setLayoutData(frmData);

		Label lblFind = new Label(sShell,SWT.LEFT);
		lblFind.setText(Messages.getString("Widget.Styled.CompReplace.Find"));
		frmData = new FormData();
		frmData.right = new FormAttachment(searchText,  -8);
		frmData.top  = new FormAttachment(12, 0);
		lblFind.setLayoutData(frmData);

		Label lblReplace = new Label(sShell,SWT.LEFT);
		lblReplace.setText(Messages.getString("Widget.Styled.CompReplace.Replace"));
		frmData = new FormData();
		frmData.right = new FormAttachment(replaceText,  -8);
		frmData.top  = new FormAttachment(replaceText, -15);
		lblReplace.setLayoutData(frmData);
		
		btnNext = new Button(sShell,SWT.PUSH);
		btnNext.setText(Messages.getString("Widget.Styled.CompReplace.FindNext"));
		btnNext.setEnabled(false);
		frmData = new FormData();
		frmData.left = new FormAttachment(searchText,  7);
		frmData.width = 81;
		frmData.height = 23;
		frmData.top  = new FormAttachment(9, 0);
		btnNext.setLayoutData(frmData);
		
		btnReplace = new Button(sShell,SWT.PUSH);
		btnReplace.setText(Messages.getString("Widget.Styled.CompReplace.Replace"));
		btnReplace.setEnabled(false);
		frmData = new FormData();
		frmData.left = new FormAttachment(searchText,  7);
		frmData.width = 81;
		frmData.height = 23;
		frmData.top  = new FormAttachment(btnNext, 6);
		btnReplace.setLayoutData(frmData);
		
		btnReplaceAll = new Button(sShell,SWT.PUSH);
		btnReplaceAll.setText(Messages.getString("Widget.Styled.CompReplace.ReplaceAll"));
		btnReplaceAll.setEnabled(false);
		frmData = new FormData();
		frmData.left = new FormAttachment(searchText,  7);
		frmData.width = 81;
		frmData.height = 23;
		frmData.top  = new FormAttachment(btnReplace, 6);
		btnReplaceAll.setLayoutData(frmData);
		
		btnCancel = new Button(sShell,SWT.PUSH);
		btnCancel.setText(Messages.getString("Widget.Styled.CompReplace.Close"));
		frmData = new FormData();
		frmData.left = new FormAttachment(searchText,  7);
		frmData.width = 81;
		frmData.height = 23;
		frmData.top  = new FormAttachment(btnReplaceAll, 6);
		btnCancel.setLayoutData(frmData);
		
		btnIgnoreCase = new Button(sShell, SWT.CHECK);
		btnIgnoreCase.setText(Messages.getString("Widget.Styled.CompReplace.CaseSensitive"));
		frmData = new FormData();
		frmData.left = new FormAttachment(5,0);
		frmData.top  = new FormAttachment(72, 0);
		btnIgnoreCase.setLayoutData(frmData);
		
		
		btnNext.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				if(!findText()){
					MessageBox messageBox = new MessageBox(sShell, SWT.ICON_INFORMATION | SWT.OK);
	        		messageBox.setText(Messages.getString("Widget.Styled.CompReplace.FindItem"));
	        		messageBox.setMessage(Messages.getString("Widget.Styled.CompReplace.ItemNotFound",searchText.getText()));
	        		messageBox.open();
				}
			}
		});

		btnReplace.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				if(text.getSelectionCount()<1){
					if(!findText()){
						MessageBox messageBox = new MessageBox(sShell, SWT.ICON_INFORMATION | SWT.OK);
						messageBox.setText(Messages.getString("Widget.Styled.CompReplace.FindItem"));
						messageBox.setMessage(Messages.getString("Widget.Styled.CompReplace.ItemNotFound",searchText.getText()));
						messageBox.open();
					}
				}else{
					replaceText();
				}
			}
		});

		btnReplaceAll.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
		        int counter;
		        text.setCaretOffset(-1);
		        for (counter = 0; findText(); counter++) {
		           replaceText();
		        }
			}
		});

		
		
		btnCancel.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				sShell.dispose();
			}
		});
		
		searchText.addModifyListener(new ModifyListener(){
			public void modifyText(ModifyEvent e){
				if(searchText.getText()!=null && searchText.getText().length()>0) btnNext.setEnabled(true);
				else{
					btnNext.setEnabled(false);
					btnReplace.setEnabled(false);
					btnReplaceAll.setEnabled(false);
				}
			}
		});
		
		replaceText.addModifyListener(new ModifyListener(){
			public void modifyText(ModifyEvent e){
				if(replaceText.getText()!=null && replaceText.getText().length()>0 && btnNext.isEnabled()){
					btnReplace.setEnabled(true);
					btnReplaceAll.setEnabled(true);
					
				}else {
					btnReplace.setEnabled(false);
					btnReplaceAll.setEnabled(false);
				}
			}
		});
		
		if (text.getSelectionText() != null) {
			searchText.setText(text.getSelectionText());
			replaceText.setFocus();
		}
		
		sShell.open();
		Display display = parent.getDisplay();
		while (!sShell.isDisposed()){
			if (!display.readAndDispatch()) display.sleep();
		}
		
	}
		   
	private boolean findText() {
		String searchString = searchText.getText();
		String textString = text.getText();
		int offset = text.getCaretOffset();	
		int start = -1;
		
		if (!btnIgnoreCase.getSelection()) {
			searchString = searchString.toLowerCase();
			textString = textString.toLowerCase();
		}
		
		// if (1==1) {
			start = textString.indexOf(searchString, offset);
		/*}else if (text.getSelectionRange().y > searchString.length()) {
			start = textString.lastIndexOf(searchString, offset - 1);
		}else {
			start = textString.lastIndexOf(searchString, offset - text.getSelectionRange().y - 1);
		}*/ 
		
		if (start > -1) {
			text.setSelection(start, start + searchString.length());
			return true;
		}
		return false;
	}
	
	private void replaceText() {
		int start = text.getSelectionRange().x;
		text.replaceTextRange(start, text.getSelectionCount(), replaceText.getText());
		text.setSelection(start, start + replaceText.getText().length());
	}
}
