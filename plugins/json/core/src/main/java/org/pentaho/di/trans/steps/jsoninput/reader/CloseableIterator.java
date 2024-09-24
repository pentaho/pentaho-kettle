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

package org.pentaho.di.trans.steps.jsoninput.reader;

import java.io.Closeable;
import java.io.IOException;
import java.util.Iterator;

/**
 * <p>An Iterator that may hold some resources and, as so, should be given the opportunity to release them when it's no
 * longer needed.</p>
 */
public interface CloseableIterator<T> extends Iterator<T>, Closeable {
  @Override
  default void close() throws IOException {
  }
}
