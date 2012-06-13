/*******************************************************************************
 *
 * Pentaho Big Data
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

package org.pentaho.di.ui.job.entries.sqoop.xul;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.pentaho.ui.xul.XulComponent;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.components.XulLabel;
import org.pentaho.ui.xul.dom.Element;
import org.pentaho.ui.xul.swt.SwtElement;

public class SwtLabelOrLink extends SwtElement implements XulLabel {
  private static final long serialVersionUID = 5202737172518086154L;
  
  private boolean disabled;
  private String onclick;
  private Link link;
  CLabel cLabel;
  Label label;

  public SwtLabelOrLink(Element self, XulComponent parent, XulDomContainer container, String tagName){
    super(tagName);

    if (self.getAttributeValue("onclick") != null) {
      link = new Link((Composite) parent.getManagedObject(), SWT.NONE);
      link.addSelectionListener(new SelectionListener() {
        public void widgetSelected(SelectionEvent selectionEvent) {
          invoke(onclick);
        }
        public void widgetDefaultSelected(SelectionEvent selectionEvent) {
          invoke(onclick);
        }
      });
      setManagedObject(link);
    } else {
      String multi = (self != null ) ? self.getAttributeValue("multiline") : null;
      if(multi != null && multi.equals("true")){
        label = new Label((Composite)parent.getManagedObject(), SWT.WRAP);
        setManagedObject(label);
      } else {
        cLabel = new CLabel((Composite)parent.getManagedObject(), SWT.NONE);
        setManagedObject(cLabel);
      }
    }
  }

  /**
   * True parameter for bean-able attribute "value" (XUL attribute)
   * @param text
   */
  public void setValue( String text ) {
  	if(text == null) {
  		text = "";
  	}
    if (link != null) {
      // Wrap entire text in an anchor tag so it looks like a link. Only do this if an anchor is not provided so we can
      // support something like "Click <a>here</a> for more information."
      if (!text.contains("<a>")) {
        text = "<a>" + text + "</a>";
      }
      link.setText(text);
    } else if(label != null){
      label.setText(text);
      if(getParent() != null){
        label.getShell().layout(true);
      }
  	} else {
  	  cLabel.setText(text);
  	}
  }
  
  public String getValue() {
    return link != null ? link.getText() : (label != null) ? label.getText() : cLabel.getText();
  }
 
  /**
   * XUL's attribute is "disabled", thus this acts
   * exactly the opposite of SWT. If the property is not 
   * available, then the control is enabled. 
   * @return boolean true if the control is disabled.
   */
  public boolean isDisabled() {
    return disabled;
  }

  public void setDisabled(boolean disabled) {
    this.disabled = disabled;
    if (link != null) {
      link.setEnabled( !disabled );
    } else if(label != null){
      if (!label.isDisposed()){
        label.setEnabled( !disabled );
      }
    } else {
      cLabel.setEnabled( ! disabled);
    }
  }

  public String getOnclick() {
    return onclick;
  }

  public void setOnclick(String onclick) {
    this.onclick = onclick;
  }
}
