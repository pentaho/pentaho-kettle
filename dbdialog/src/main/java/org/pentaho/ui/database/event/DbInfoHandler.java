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
package org.pentaho.ui.database.event;

import org.pentaho.di.core.database.DatabaseMeta;

/**
 * A complement to {@link DataHandler} that handles
 * database meta save/load without having to keep
 * adding subtypes to a single class.
 */
public interface DbInfoHandler {
  /** ui -> meta */
  void saveConnectionSpecificInfo( DatabaseMeta meta );
  /** meta -> ui */
  void loadConnectionSpecificInfo( DatabaseMeta meta );
}
