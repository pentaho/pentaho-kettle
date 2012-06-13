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
import org.pentaho.ui.xul.XulDomException;
import org.pentaho.ui.xul.binding.BindingProvider;
import org.pentaho.ui.xul.containers.XulDeck;
import org.pentaho.ui.xul.dom.Attribute;
import org.pentaho.ui.xul.dom.Document;
import org.pentaho.ui.xul.dom.Element;
import org.pentaho.ui.xul.dom.Namespace;
import org.pentaho.ui.xul.util.Orient;

import java.beans.PropertyChangeListener;
import java.util.List;

/**
 * Mock {@link XulDeck} to be used in unit tests.
 */
public class MockXulDeck extends MockXulContainer implements XulDeck {
  private int selectedIndex = -1;
  @Override
  public void setSelectedIndex(int i) {
    selectedIndex = i;
  }

  @Override
  public int getSelectedIndex() {
    return selectedIndex;
  }
}
