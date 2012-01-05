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

import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.gui.GUIResource;


/**
 * A Widget that combines a Check Box widget with a Variable button that will insert an Environment variable.
 * 
 * @author Matt
 * @since 9-august-2006
 */
public class CheckBoxVar extends Composite
{
	private static Class<?> PKG = CheckBoxVar.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

    private static final PropsUI props = PropsUI.getInstance();

    private ControlDecoration controlDecoration;
    
    private Button wBox;
    
    private TextVar wText;
    
    public CheckBoxVar(VariableSpace space, Composite composite, int flags)
    {
        this(space, composite, flags, null);
    }
    
    public CheckBoxVar(final VariableSpace space, final Composite composite, int flags, String variable)
    {
        super(composite, SWT.NONE);
        
        props.setLook(this);
        
        FormLayout formLayout = new FormLayout();
        formLayout.marginWidth  = 0;
        formLayout.marginHeight = 0;
        formLayout.marginTop = 0;
        formLayout.marginBottom = 0;

        this.setLayout(formLayout);

        // add a text field on it...
        wBox = new Button(this, flags);
        props.setLook(wBox);
        wText = new TextVar(space, this, flags | SWT.NO_BACKGROUND);
        wText.getTextWidget().setForeground(GUIResource.getInstance().getColorRed()); // Put it in a red color to make it shine...
        wText.getTextWidget().setBackground(composite.getBackground()); // make it blend in with the rest...

        setVariableOnCheckBox(variable);

        controlDecoration = new ControlDecoration(wBox, SWT.TOP | SWT.LEFT);
        Image image = GUIResource.getInstance().getImageVariable();
        controlDecoration.setImage( image );
        controlDecoration.setDescriptionText(BaseMessages.getString(PKG, "CheckBoxVar.tooltip.InsertVariable"));
        controlDecoration.addSelectionListener(new SelectionAdapter() {
			
				@Override
				public void widgetSelected(SelectionEvent arg0) {
                    String variableName = VariableButtonListenerFactory.getVariableName(composite.getShell(), space);
                    if (variableName!=null) {
                    	setVariableOnCheckBox("${"+variableName+"}");
                    }
				}
			}
        );
        
        FormData fdBox = new FormData();
        fdBox.top   = new FormAttachment(0, 0);
        fdBox.left  = new FormAttachment(0 , image.getBounds().width);
        wBox.setLayoutData(fdBox);
        
        FormData fdText = new FormData();
        fdText.top   = new FormAttachment(0, 0);
        fdText.left  = new FormAttachment(wBox , Const.MARGIN);
        fdText.right = new FormAttachment(100, 0);
        wText.setLayoutData(fdText);
    }
    
    private void setVariableOnCheckBox(String variableName) {
    	if (!Const.isEmpty(variableName)) {
    		wText.setText(variableName);   
    	}
    	else {
    		wText.setText("");// $NON-NLS-1$
    	}
	}

    /**
     * @return the text in the Text widget   
     */
    public String getText()
    {
        return wBox.getText();
    }
    
    /**
     * @param text the text in the Text widget to set.
     */
    public void setText(String text)
    {
    	wBox.setText(text);
    }

    public void addSelectionListener(SelectionAdapter lsDef)
    {
    	wBox.addSelectionListener(lsDef);
    }
    
    public void addKeyListener(KeyListener lsKey)
    {
    	wBox.addKeyListener(lsKey);
    }
    
    public void addFocusListener(FocusListener lsFocus)
    {
    	wBox.addFocusListener(lsFocus);
    }

    public void setEnabled(boolean flag)
    {
    	wBox.setEnabled(flag);
    }

    public void setSelection(boolean selection)
    {
    	wBox.setSelection(selection);
    }

    public boolean getSelection()
    {
    	return wBox.getSelection();
    }
    
    public boolean setFocus()
    {
        return wBox.setFocus();
    }
    
    public void addTraverseListener(TraverseListener tl)
    {
    	wBox.addTraverseListener(tl);
    }

	public String getVariableName() {
		return wText.getText();
	}

	public void setVariableName(String variableName) {
	    if (variableName!=null) 
	    	wText.setText(variableName);
	    else 
	    	wText.setText("");
	}
}