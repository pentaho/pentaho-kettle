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

package org.pentaho.di.ui.core.database.dialog.tags;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.ui.xul.XulComponent;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.containers.XulTree;
import org.pentaho.ui.xul.dom.Element;
import org.pentaho.ui.xul.impl.AbstractXulComponent;
import org.pentaho.ui.xul.swt.tags.SwtTextbox;

public class ExtTextbox extends SwtTextbox {

  public TextVar extText = null;
  private VariableSpace variableSpace;
  private XulComponent xulParent;

  private int style = SWT.NONE;
  
  public ExtTextbox(Element self, XulComponent parent, XulDomContainer container, String tagName) {
    super(self, parent, container, tagName);
    xulParent = parent;

    if ((xulParent != null) && (xulParent instanceof XulTree)){
      variableSpace = (DatabaseMeta)((XulTree)xulParent).getData();
      
    }else{
      variableSpace = new DatabaseMeta();
      style = SWT.BORDER;
    }

    extText = new TextVar(variableSpace, parentComposite, style);
    textBox = extText.getTextWidget();
    setManagedObject(extText);
  }

  @Override
  public Text createNewText() {
    org.eclipse.swt.widgets.Text box; 
    if (extText != null){
      box =  extText.getTextWidget();
      addKeyListener(box);
    }else{
      box = null;
    }
    return box;
  }
  
  @Override
  public Object getTextControl() {
    getManagedObject();
    return extText.getTextWidget();
  }


  public Object getManagedObject() {
    if (textBox.isDisposed()){
      int thisStyle = isMultiline()? SWT.MULTI|SWT.BORDER|SWT.WRAP|SWT.V_SCROLL : style;
      extText = new TextVar(variableSpace, parentComposite, thisStyle);
      setDisabled(isDisabled());
      setMaxlength(getMaxlength());
      setValue(getValue());
      setReadonly(isReadonly());
      setType(getType());
      textBox = extText.getTextWidget();
      setManagedObject(extText);
      layout();
    }
    return super.getManagedObject();
  }

  @Override
  public void layout() {
    ((AbstractXulComponent)xulParent).layout();
  }
  
  public void setVariableSpace(VariableSpace space){
    variableSpace=space;
    extText.setVariables(variableSpace);
  }



}
