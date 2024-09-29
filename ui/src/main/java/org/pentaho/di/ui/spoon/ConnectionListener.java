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


package org.pentaho.di.ui.spoon;

import org.pentaho.di.core.exception.KettleMissingPluginsException;
import org.w3c.dom.Node;

public interface ConnectionListener {
  boolean open( Node transNode, String fname, String connection, boolean importfile )
    throws KettleMissingPluginsException;
}
