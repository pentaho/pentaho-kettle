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

/**
 * Interface to be notified by a Manager when changes are made.
 *
 */
@FunctionalInterface
public interface UpdateSubscriber {

  /**
   * Notify that any write operation was performed (create/update/delete)
   *
   */
  void notifyChanged();

}
