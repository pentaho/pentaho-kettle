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

package org.pentaho.di.ui.spoon.delegates;

import org.pentaho.di.base.AbstractMeta;
import org.pentaho.di.ui.spoon.TreeSelection;

import java.util.List;

public class SpoonTreeDelegateExtension {

  private AbstractMeta transMeta = null;
  private String[] path = null;
  private int caseNumber = -1;
  private List<TreeSelection> objects = null;

  public SpoonTreeDelegateExtension( AbstractMeta transMeta, String[] path, int caseNumber,
      List<TreeSelection> objects ) {
    this.transMeta = transMeta;
    this.path = path;
    this.caseNumber = caseNumber;
    this.objects = objects;
  }

  public AbstractMeta getTransMeta() {
    return transMeta;
  }

  public String[] getPath() {
    return path;
  }

  public int getCaseNumber() {
    return caseNumber;
  }

  public List<TreeSelection> getObjects() {
    return objects;
  }
}
