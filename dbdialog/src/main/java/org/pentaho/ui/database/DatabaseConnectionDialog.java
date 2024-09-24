/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package org.pentaho.ui.database;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.widgets.Shell;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.swt.SwtXulLoader;

public class DatabaseConnectionDialog {

  public static final String DIALOG_DEFINITION_FILE = "org/pentaho/ui/database/databasedialog.xul";

  protected Map<String, String> extendedClasses = new HashMap<>();

  public DatabaseConnectionDialog() {
  }

  public void registerClass( String key, String className ) {
    extendedClasses.put( key, className );
  }

  public XulDomContainer getSwtInstance( Shell shell ) throws XulException {
    SwtXulLoader loader = new SwtXulLoader();
    return getSwtInstance( loader, shell );
  }

  public XulDomContainer getSwtInstance( SwtXulLoader loader, Shell shell ) throws XulException {

    XulDomContainer container = null;

    Iterable<String> keyIterable = extendedClasses.keySet();
    for ( Object key : keyIterable ) {
      loader.register( (String) key, extendedClasses.get( key ) );
    }
    loader.setOuterContext( shell );
    container = loader.loadXul( DIALOG_DEFINITION_FILE, Messages.getBundle() );
    container.initialize();
    return container;
  }
}
