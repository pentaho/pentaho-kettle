package org.pentaho.di.ui.core.database.dialog.tags;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.ui.xul.XulComponent;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.impl.AbstractXulComponent;
import org.pentaho.ui.xul.swt.tags.SwtTextbox;

public class ExtTextbox extends SwtTextbox {

  public TextVar extText = null;
  private VariableSpace variableSpace;
  private XulComponent xulParent;

  public ExtTextbox(XulComponent parent, XulDomContainer container, String tagName) {
    super(parent, container, tagName);
    xulParent = parent;
    variableSpace = new DatabaseMeta();
    extText = new TextVar(variableSpace, parentComposite, SWT.BORDER);
    textBox = extText.getTextWidget();
    managedObject = extText;
  }

  @Override
  public Text createNewText() {
    if (extText != null){
      return extText.getTextWidget();
    }
    return null;
  }

  public Object getManagedObject() {
    if (textBox.isDisposed()){
      int style = isMultiline()? SWT.MULTI|SWT.BORDER|SWT.WRAP|SWT.V_SCROLL : SWT.BORDER;
      extText = new TextVar(variableSpace, parentComposite, style);
      setDisabled(isDisabled());
      setMaxlength(getMaxlength());
      setValue(getValue());
      setReadonly(isReadonly());
      setType(getType());
      textBox = extText.getTextWidget();
      managedObject = extText;
      layout();
    }
    return managedObject;
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
