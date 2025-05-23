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

package org.pentaho.di.core.bowl;

import org.pentaho.di.core.exception.KettleException;

@FunctionalInterface
public interface ManagerFactory<T> {
  /**
     * Should not return null
     *
     *
     * @param bowl Bowl this manager should belong to.
     *
     * @return T Manager for this bowl.
     * @throw KettleException if a manager cannot be created
     */
  T apply( Bowl bowl ) throws KettleException;
}

