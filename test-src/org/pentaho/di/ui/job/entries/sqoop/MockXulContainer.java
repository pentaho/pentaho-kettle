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

package org.pentaho.di.ui.job.entries.sqoop;

import org.pentaho.ui.xul.XulComponent;
import org.pentaho.ui.xul.XulContainer;
import org.pentaho.ui.xul.XulDomException;
import org.pentaho.ui.xul.binding.BindingProvider;
import org.pentaho.ui.xul.dom.Attribute;
import org.pentaho.ui.xul.dom.Document;
import org.pentaho.ui.xul.dom.Element;
import org.pentaho.ui.xul.dom.Namespace;
import org.pentaho.ui.xul.util.Orient;

import java.beans.PropertyChangeListener;
import java.util.List;

/**
 * Mock {@link XulContainer} to be used in unit tests.
 */
public class MockXulContainer implements XulContainer {

  @Override
  public void addComponent(XulComponent xulComponent) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void addComponentAt(XulComponent xulComponent, int i) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void removeComponent(XulComponent xulComponent) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Orient getOrientation() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void suppressLayout(boolean b) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Object getManagedObject() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setManagedObject(Object o) {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getName() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setId(String s) {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getId() {
    throw new UnsupportedOperationException();
  }

  @Override
  public int getFlex() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setFlex(int i) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setOnblur(String s) {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getOnblur() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setWidth(int i) {
    throw new UnsupportedOperationException();
  }

  @Override
  public int getWidth() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setHeight(int i) {
    throw new UnsupportedOperationException();
  }

  @Override
  public int getHeight() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void addPropertyChangeListener(PropertyChangeListener propertyChangeListener) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void removePropertyChangeListener(PropertyChangeListener propertyChangeListener) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setDisabled(boolean b) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isDisabled() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setTooltiptext(String s) {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getTooltiptext() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setBgcolor(String s) {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getBgcolor() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setPadding(int i) {
    throw new UnsupportedOperationException();
  }

  @Override
  public int getPadding() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setSpacing(int i) {
    throw new UnsupportedOperationException();
  }

  @Override
  public int getSpacing() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void adoptAttributes(XulComponent xulComponent) {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getInsertbefore() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setInsertbefore(String s) {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getInsertafter() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setInsertafter(String s) {
    throw new UnsupportedOperationException();
  }

  @Override
  public int getPosition() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setPosition(int i) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean getRemoveelement() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setRemoveelement(boolean b) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isVisible() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setVisible(boolean b) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void onDomReady() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setAlign(String s) {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getAlign() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setContext(String s) {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getContext() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setPopup(String s) {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getPopup() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setMenu(String s) {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getMenu() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setOndrag(String s) {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getOndrag() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setDrageffect(String s) {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getDrageffect() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setOndrop(String s) {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getOndrop() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setDropvetoer(String s) {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getDropvetoer() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setBindingProvider(BindingProvider bindingProvider) {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getText() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Document getDocument() {
    throw new UnsupportedOperationException();
  }

  @Override
  public XulComponent getParent() {
    throw new UnsupportedOperationException();
  }

  @Override
  public XulComponent getFirstChild() {
    throw new UnsupportedOperationException();
  }

  @Override
  public List<XulComponent> getChildNodes() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setNamespace(String s, String s1) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Namespace getNamespace() {
    throw new UnsupportedOperationException();
  }

  @Override
  public XulComponent getElementById(String s) {
    throw new UnsupportedOperationException();
  }

  @Override
  public XulComponent getElementByXPath(String s) {
    throw new UnsupportedOperationException();
  }

  @Override
  public List<XulComponent> getElementsByTagName(String s) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void addChild(Element element) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void addChildAt(Element element, int i) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void removeChild(Element element) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Object getElementObject() {
    throw new UnsupportedOperationException();
  }

  @Override
  public List<Attribute> getAttributes() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setAttributes(List<Attribute> attributes) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setAttribute(Attribute attribute) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setAttribute(String s, String s1) {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getAttributeValue(String s) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void replaceChild(XulComponent xulComponent, XulComponent xulComponent1) throws XulDomException {
    throw new UnsupportedOperationException();
  }
}
