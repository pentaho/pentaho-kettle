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

package org.pentaho.kettle.repository.locator.api;

import org.pentaho.di.repository.Repository;

/**
 * Created by bryan on 3/29/16.
 */
public interface KettleRepositoryProvider {
  Repository getRepository();
}
