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

package org.pentaho.di.core.logging;

/**
 * A listener to detect that content was added to a string buffer.
 *
 * @author matt
 *
 */
public interface BufferChangedListener {
  // Left as StringBuffer as this is used across threads.
  public void contentWasAdded( StringBuffer content, String extra, int nrLines );
}
