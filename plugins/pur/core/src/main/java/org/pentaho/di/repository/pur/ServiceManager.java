/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/

package org.pentaho.di.repository.pur;

import java.net.MalformedURLException;

public interface ServiceManager {
  public <T> T createService( final String username, final String password, final Class<T> clazz )
    throws MalformedURLException;

  public void close();
}
