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

package org.pentaho.di.ui.trans.dialog;

import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.trans.TransMeta;

public interface TransDialogPluginInterface {
  public void addTab( TransMeta transMeta, Shell shell, CTabFolder tabFolder );

  public void getData( TransMeta transMeta ) throws KettleException;

  public void ok( TransMeta transMeta ) throws KettleException;

  public CTabItem getTab();
}
