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

package org.pentaho.di.ui.repository.repositoryexplorer.controllers;

import org.pentaho.di.ui.repository.repositoryexplorer.ContextChangeVetoer;

/**
 * Common functionality expected of all browse controller implementations.
 *
 * @author mlowery
 */
public interface IBrowseController {

  void addContextChangeVetoer( ContextChangeVetoer listener );

  void removeContextChangeVetoer( ContextChangeVetoer listener );

}
