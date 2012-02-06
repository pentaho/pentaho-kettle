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

package org.pentaho.ui.database;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.widgets.Shell;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.swt.SwtXulLoader;

public class DatabaseConnectionDialog {

  public static final String DIALOG_DEFINITION_FILE = "org/pentaho/ui/database/databasedialog.xul"; //$NON-NLS-1$

  private Map<String, String> extendedClasses = new HashMap<String, String>();

  public DatabaseConnectionDialog() {
  }

  public void registerClass(String key, String className) {
    extendedClasses.put(key, className);
  }

  public XulDomContainer getSwtInstance(Shell shell) throws XulException {

    XulDomContainer container = null;
    SwtXulLoader loader = new SwtXulLoader();

    Iterable<String> keyIterable = extendedClasses.keySet();
    for (Object key : keyIterable) {
      loader.register((String) key, extendedClasses.get(key));
    }
    loader.setOuterContext(shell);
    container = loader.loadXul(DIALOG_DEFINITION_FILE, Messages.getBundle());
    container.initialize();
    return container;
  }

}
