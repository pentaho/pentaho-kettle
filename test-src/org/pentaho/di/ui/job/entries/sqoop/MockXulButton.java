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

import org.pentaho.ui.xul.components.XulButton;

/**
 * Mock {@link XulButton} to be used in unit tests.
 */
public class MockXulButton extends MockXulContainer implements XulButton {

  private String label;
  private String image;
  private String onclick;
  private String dir;
  private String type;
  private String group;
  private boolean selected;

  @Override
  public void setLabel(String s) {
    label = s;
  }

  @Override
  public String getLabel() {
    return label;
  }

  @Override
  public void setImage(String s) {
    image = s;
  }

  @Override
  public String getImage() {
    return image;
  }

  @Override
  public void setOnclick(String s) {
    onclick = s;
  }

  @Override
  public String getOnclick() {
    return onclick;
  }

  @Override
  public void setDir(String s) {
    dir = s;
  }

  @Override
  public String getDir() {
    return dir;
  }

  @Override
  public void setType(String s) {
    type = s;
  }

  @Override
  public String getType() {
    return type;
  }

  @Override
  public void setGroup(String s) {
    group = s;
  }

  @Override
  public String getGroup() {
    return group;
  }

  @Override
  public void setSelected(String s) {
    selected = Boolean.parseBoolean(s);
  }

  @Override
  public void setSelected(boolean b) {
    selected = b;
  }

  @Override
  public boolean isSelected() {
    return selected;
  }

  @Override
  public void doClick() {
  }
}
