/*******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.ui.repository.repositoryexplorer.model;

import java.util.List;

import org.pentaho.ui.xul.util.AbstractModelList;

public class UIDatabaseConnections extends AbstractModelList<UIDatabaseConnection> {
  
  private static final long serialVersionUID = -5835985536358581894L;

  public UIDatabaseConnections(){
  }
  
  public UIDatabaseConnections(List<UIDatabaseConnection> dbConns){
    super(dbConns);
  }
  
  @Override
  protected void fireCollectionChanged() {
    this.changeSupport.firePropertyChange("children", null, this.getChildren()); //$NON-NLS-1$
  }

}
