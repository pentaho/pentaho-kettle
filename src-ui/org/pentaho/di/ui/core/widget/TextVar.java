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
package org.pentaho.di.ui.core.widget;

import java.util.Arrays;

import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.window.DefaultToolTip;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.gui.GUIResource;


/**
 * A Widget that combines a Text widget with a Variable button that will insert an Environment variable.
 * The tool tip of the text widget shows the content of the Text widget with expanded variables.
 * 
 * @author Matt
 * @since 17-may-2006
 */
public class TextVar extends Composite
{
    private String toolTipText;
    
    private static final PropsUI props = PropsUI.getInstance();

    private ControlDecoration controlDecoration;

    private GetCaretPositionInterface getCaretPositionInterface;

    private InsertTextInterface insertTextInterface;
    
    private VariableSpace variables;
    
    private Text wText;
    
    
    public TextVar(VariableSpace space, Composite composite, int flags)
    {
        this(space, composite, flags, null, null, null);
    }
    
    public TextVar(VariableSpace space, Composite composite, int flags, String toolTipText)
    {
        this(space, composite, flags, toolTipText, null, null);
    }
    

    public TextVar(VariableSpace space, Composite composite, int flags, GetCaretPositionInterface getCaretPositionInterface, InsertTextInterface insertTextInterface)
    {
        this(space, composite, flags, null, getCaretPositionInterface, insertTextInterface);
    }
    
    public TextVar(VariableSpace space, Composite composite, int flags, String toolTipText, GetCaretPositionInterface getCaretPositionInterface, InsertTextInterface insertTextInterface)
    {
        super(composite, SWT.NONE);
        this.toolTipText = toolTipText;
        this.getCaretPositionInterface = getCaretPositionInterface;
        this.insertTextInterface = insertTextInterface;
        this.variables = space;
        
        props.setLook(this);
        
        // int margin = Const.MARGIN;
        FormLayout formLayout = new FormLayout();
        formLayout.marginWidth  = 0;
        formLayout.marginHeight = 0;
        formLayout.marginTop = 0;
        formLayout.marginBottom = 0;

        this.setLayout(formLayout);

        // add a text field on it...
        wText = new Text(this, flags);
        
        controlDecoration = new ControlDecoration(wText, SWT.TOP | SWT.RIGHT);
        Image image = GUIResource.getInstance().getImageVariable();
        controlDecoration.setImage( image );
        controlDecoration.setDescriptionText(Messages.getString("TextVar.tooltip.InsertVariable"));
        
        props.setLook(wText);
        wText.addModifyListener(getModifyListenerTooltipText(wText));
        SelectionAdapter lsVar = VariableButtonListenerFactory.getSelectionAdapter(this, wText, getCaretPositionInterface, insertTextInterface, space);
        wText.addKeyListener(getControlSpaceKeyListener(space, wText, lsVar, getCaretPositionInterface, insertTextInterface));
        
        FormData fdText = new FormData();
        fdText.top   = new FormAttachment(0, 0);
        fdText.left  = new FormAttachment(0 ,0);
        fdText.right = new FormAttachment(100, -image.getBounds().width);
        wText.setLayoutData(fdText);
    }
    
    /**
     * @return the getCaretPositionInterface
     */
    public GetCaretPositionInterface getGetCaretPositionInterface()
    {
        return getCaretPositionInterface;
    }

    /**
     * @param getCaretPositionInterface the getCaretPositionInterface to set
     */
    public void setGetCaretPositionInterface(GetCaretPositionInterface getCaretPositionInterface)
    {
        this.getCaretPositionInterface = getCaretPositionInterface;
    }

    /**
     * @return the insertTextInterface
     */
    public InsertTextInterface getInsertTextInterface()
    {
        return insertTextInterface;
    }

    /**
     * @param insertTextInterface the insertTextInterface to set
     */
    public void setInsertTextInterface(InsertTextInterface insertTextInterface)
    {
        this.insertTextInterface = insertTextInterface;
    }    
    
    private ModifyListener getModifyListenerTooltipText(final Text textField)
    {
        return new ModifyListener()
        {
            public void modifyText(ModifyEvent e)
            {
                if (textField.getEchoChar()=='\0') // Can't show passwords ;-)
                {
                    String tip = textField.getText();
                    if (!Const.isEmpty(tip) && !Const.isEmpty(toolTipText))
                    {
                        tip+=Const.CR+Const.CR+toolTipText;
                    }
                    
                    if (Const.isEmpty(tip))
                    {
                        tip=toolTipText;
                    }
                    textField.setToolTipText(variables.environmentSubstitute( tip ) );
                }
            }
        };
    }

    public static final KeyListener getControlSpaceKeyListener(final VariableSpace space, final Text textField, final SelectionListener lsVar)
    {
    	return getControlSpaceKeyListener(space, textField, lsVar, null, null);
    }
    
    public static final KeyListener getControlSpaceKeyListener(final VariableSpace space, final Text textField, final SelectionListener lsVar, final GetCaretPositionInterface getCaretPositionInterface, final InsertTextInterface insertTextInterface)
    {
        return new KeyAdapter()
        {
            public void keyPressed(KeyEvent e)
            {
                // CTRL-<SPACE> --> Insert a variable
                if (e.character == ' ' && (( e.stateMask&SWT.CONTROL)!=0) && (( e.stateMask&SWT.ALT)==0) ) 
                { 
                	e.doit = false;
                	
                	final int position;
                	if (getCaretPositionInterface!=null) position=getCaretPositionInterface.getCaretPosition();
                	else position = -1;
                	
                	// Drop down a list of variables...
                	//
            		Rectangle bounds = textField.getBounds();
            		Point location = GUIResource.calculateControlPosition(textField);
            		
            		final Shell shell = new Shell(textField.getShell(), SWT.NONE);
            		shell.setSize(bounds.width, 200);
            		shell.setLocation(location.x, location.y+bounds.height);
            		shell.setLayout(new FillLayout());
            		final List list = new List(shell, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL);
            		props.setLook(list);
            		list.setItems(getVariableNames(space));
					final DefaultToolTip toolTip = new DefaultToolTip(list, ToolTip.RECREATE, true);
					toolTip.setImage(GUIResource.getInstance().getImageSpoon());
			        toolTip.setHideOnMouseDown(true);
			        toolTip.setRespectMonitorBounds(true);
			        toolTip.setRespectDisplayBounds(true);
			        toolTip.setPopupDelay(350);

            		list.addSelectionListener(new SelectionAdapter() {
            			// Enter or double-click: picks the variable
            			//
						public synchronized void widgetDefaultSelected(SelectionEvent e) {
							applyChanges(shell, list, textField, position, insertTextInterface);
						}
						
						// Select a variable name: display the value in a tool tip
						//
						public void widgetSelected(SelectionEvent event) {
							if (list.getSelectionCount()<=0) return;
							String name = list.getSelection()[0];
							String value = space.getVariable(name);
							Rectangle shellBounds = shell.getBounds();
							String message = Messages.getString("TextVar.VariableValue.Message", name, value);
							if (name.startsWith(Const.INTERNAL_VARIABLE_PREFIX)) message+=Messages.getString("TextVar.InternalVariable.Message");
							toolTip.setText(message);
							toolTip.hide();
							toolTip.show(new Point(shellBounds.width, 0));
						}
					});
            		
            		list.addKeyListener(new KeyAdapter() {
					
						public synchronized void keyPressed(KeyEvent e) {
							if (e.keyCode==SWT.CR && ((e.keyCode&SWT.CONTROL)==0) && ((e.keyCode&SWT.SHIFT)==0) ) { 
								applyChanges(shell, list, textField, position, insertTextInterface);
							}
						}
					
					});
            		list.addFocusListener(new FocusAdapter() { public void focusLost(FocusEvent event) { shell.dispose(); } });
            		shell.open();
                };
            }
        };
    }
    
    private static final void applyChanges(Shell shell, List list, Text textField, int position, InsertTextInterface insertTextInterface) {
    	String extra = "${"+list.getSelection()[0]+"}";
    	if (insertTextInterface!=null) {
    		insertTextInterface.insertText(extra, position);
    	}
    	else {
			if (textField.isDisposed()) return;
			
			if (list.getSelectionCount()<=0) return;
			textField.insert(extra);
    	}
		if (!shell.isDisposed()) shell.dispose();
    }
    
    public static final String[] getVariableNames(VariableSpace space) {
    	String variableNames[] = space.listVariables();
        Arrays.sort(variableNames);
        return variableNames;
    }

    /**
     * @return the text in the Text widget   
     */
    public String getText()
    {
        return wText.getText();
    }
    
    /**
     * @param text the text in the Text widget to set.
     */
    public void setText(String text)
    {
    	wText.setText(text);
    }
    
    public Text getTextWidget()
    {
        return wText;
    }
 
    /**
     * Add a modify listener to the text widget
     * @param modifyListener
     */
    public void addModifyListener(ModifyListener modifyListener)
    {
    	wText.addModifyListener(modifyListener);
    }

    public void addSelectionListener(SelectionAdapter lsDef)
    {
    	wText.addSelectionListener(lsDef);
    }
    
    public void addKeyListener(KeyListener lsKey)
    {
    	wText.addKeyListener(lsKey);
    }
    
    public void addFocusListener(FocusListener lsFocus)
    {
    	wText.addFocusListener(lsFocus);
    }

    public void setEchoChar(char c)
    {
    	wText.setEchoChar(c);
    }
 
    public void setEnabled(boolean flag)
    {
    	wText.setEnabled(flag);
    }
    
    public boolean setFocus()
    {
        return wText.setFocus();
    }
    
    public void addTraverseListener(TraverseListener tl)
    {
    	wText.addTraverseListener(tl);
    }
    
    public void setToolTipText(String toolTipText)
    {
        this.toolTipText = toolTipText;
        wText.setToolTipText(toolTipText);
    }

    public void setEditable(boolean editable)
    {
    	wText.setEditable(editable);
    }

    public void setSelection(int i)
    {
    	wText.setSelection(i);
    }

    public void selectAll()
    {
    	wText.selectAll();
    }

    public void showSelection()
    {
    	wText.showSelection();
    }
}