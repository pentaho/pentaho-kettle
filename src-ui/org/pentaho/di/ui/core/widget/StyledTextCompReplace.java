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
import org.pentaho.di.core.Const;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.ui.core.gui.GUIResource;


public class StyledTextCompReplace extends org.eclipse.swt.widgets.Dialog {

	private static Class<?> PKG = StyledTextCompReplace.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

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
		sShell.setText(BaseMessages.getString(PKG, "Widget.Styled.CompReplace.Replace"));
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
		lblFind.setText(BaseMessages.getString(PKG, "Widget.Styled.CompReplace.Find"));
		frmData = new FormData();
		frmData.right = new FormAttachment(searchText,  -8);
		frmData.top  = new FormAttachment(12, 0);
		lblFind.setLayoutData(frmData);

		Label lblReplace = new Label(sShell,SWT.LEFT);
		lblReplace.setText(BaseMessages.getString(PKG, "Widget.Styled.CompReplace.Replace"));
		frmData = new FormData();
		frmData.right = new FormAttachment(replaceText,  -8);
		frmData.top  = new FormAttachment(replaceText, -15);
		lblReplace.setLayoutData(frmData);
		
		btnNext = new Button(sShell,SWT.PUSH);
		btnNext.setText(BaseMessages.getString(PKG, "Widget.Styled.CompReplace.FindNext"));
		btnNext.setEnabled(false);
		frmData = new FormData();
		frmData.left = new FormAttachment(searchText,  7);
		frmData.width = 81;
		frmData.height = 23;
		frmData.top  = new FormAttachment(9, 0);
		btnNext.setLayoutData(frmData);
		
		btnReplace = new Button(sShell,SWT.PUSH);
		btnReplace.setText(BaseMessages.getString(PKG, "Widget.Styled.CompReplace.Replace"));
		btnReplace.setEnabled(false);
		frmData = new FormData();
		frmData.left = new FormAttachment(searchText,  7);
		frmData.width = 81;
		frmData.height = 23;
		frmData.top  = new FormAttachment(btnNext, 6);
		btnReplace.setLayoutData(frmData);
		
		btnReplaceAll = new Button(sShell,SWT.PUSH);
		btnReplaceAll.setText(BaseMessages.getString(PKG, "Widget.Styled.CompReplace.ReplaceAll"));
		btnReplaceAll.setEnabled(false);
		frmData = new FormData();
		frmData.left = new FormAttachment(searchText,  7);
		frmData.width = 81;
		frmData.height = 23;
		frmData.top  = new FormAttachment(btnReplace, 6);
		btnReplaceAll.setLayoutData(frmData);
		
		btnCancel = new Button(sShell,SWT.PUSH);
		btnCancel.setText(BaseMessages.getString(PKG, "Widget.Styled.CompReplace.Close"));
		frmData = new FormData();
		frmData.left = new FormAttachment(searchText,  7);
		frmData.width = 81;
		frmData.height = 23;
		frmData.top  = new FormAttachment(btnReplaceAll, 6);
		btnCancel.setLayoutData(frmData);
		
		btnIgnoreCase = new Button(sShell, SWT.CHECK);
		btnIgnoreCase.setText(BaseMessages.getString(PKG, "Widget.Styled.CompReplace.CaseSensitive"));
		frmData = new FormData();
		frmData.left = new FormAttachment(5,0);
		frmData.top  = new FormAttachment(72, 0);
		btnIgnoreCase.setLayoutData(frmData);
		
		
		btnNext.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				if(!findText()){
					MessageBox messageBox = new MessageBox(sShell, SWT.ICON_INFORMATION | SWT.OK);
	        		messageBox.setText(BaseMessages.getString(PKG, "Widget.Styled.CompReplace.FindItem"));
	        		messageBox.setMessage(BaseMessages.getString(PKG, "Widget.Styled.CompReplace.ItemNotFound",searchText.getText()));
	        		messageBox.open();
				}
			}
		});

		btnReplace.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				if(text.getSelectionCount()<1){
					if(!findText()){
						MessageBox messageBox = new MessageBox(sShell, SWT.ICON_INFORMATION | SWT.OK);
						messageBox.setText(BaseMessages.getString(PKG, "Widget.Styled.CompReplace.FindItem"));
						messageBox.setMessage(BaseMessages.getString(PKG, "Widget.Styled.CompReplace.ItemNotFound",searchText.getText()));
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
		setSearchText();
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
	private void setSearchText() {
		if(this.text!=null && !Const.isEmpty(this.text.getSelectionText()))
			searchText.setText(this.text.getSelectionText());
		searchText.setFocus();
	}  
	private void replaceText() {
		int start = text.getSelectionRange().x;
		text.replaceTextRange(start, text.getSelectionCount(), replaceText.getText());
		text.setSelection(start, start + replaceText.getText().length());
	}
}
