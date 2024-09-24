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

package org.pentaho.di.core.listeners;

/**
 * Listens for changes in {@code Internal.Entry.Current.Directory}
 */
public interface CurrentDirectoryChangedListener {

  public void directoryChanged( Object origin, String previous, String current );

}
