/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
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
