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

package org.pentaho.di.ui.spoon;

import org.pentaho.di.core.EngineMetaInterface;
import org.pentaho.di.core.exception.KettleException;

public interface TabItemInterface {
  /**
   * Closes the content behind the tab, de-allocates resources.
   *
   * @return true if the tab was closed, false if it was prevented by the user. (are you sure dialog)
   */
  public boolean canBeClosed();

  public boolean canHandleSave();

  public Object getManagedObject();

  public boolean hasContentChanged();

  public ChangedWarningInterface getChangedWarning();

  public int showChangedWarning() throws KettleException;

  public boolean applyChanges() throws KettleException;

  public EngineMetaInterface getMeta();

  public void setControlStates();

  public boolean setFocus();

}
