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

package org.pentaho.xul.swt.tab;

public interface TabListener {

  public void tabSelected( TabItem item );

  public void tabDeselected( TabItem item );

  public boolean tabClose( TabItem item );

}
