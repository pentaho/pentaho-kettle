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


package org.pentaho.di.trans.step;

import org.pentaho.di.repository.Repository;
import org.pentaho.metastore.api.IMetaStore;

/**
 * This interface is used to launch Step Dialogs. All dialogs that implement this simple interface can be opened by
 * Spoon.
 *
 * @author Matt
 * @since 4-aug-2004
 */
public interface StepDialogInterface {

  /**
   * Opens a step dialog window.
   *
   * @return the (potentially new) name of the step
   */
  String open();

  /**
   * Sets the repository.
   *
   * @param repository
   *          the new repository
   */
  void setRepository( Repository repository );

  /**
   * @param metaStore
   *          The MetaStore to pass
   */
  void setMetaStore( IMetaStore metaStore );
}
