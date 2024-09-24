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

package org.pentaho.di.laf;

/**
 * Specific Handler interface from which all LAF/pluggable handlers should derive to enable the factory to manage them
 * consistently.
 *
 * @author dhushon
 *
 */
public interface Handler {
  // public <E extends Handler>LAFDelegate<E> getLAFDelegate();
}
