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
    managedObject = extText;
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
