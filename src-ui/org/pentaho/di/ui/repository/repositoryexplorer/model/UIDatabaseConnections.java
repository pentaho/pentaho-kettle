/* Copyright (c) 2009 Pentaho Corporation.  All rights reserved. 
* This software was developed by Pentaho Corporation and is provided under the terms 
* of the GNU Lesser General Public License, Version 2.1. You may not use 
* this file except in compliance with the license. If you need a copy of the license, 
* please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
* Data Integration.  The Initial Developer is Pentaho Corporation.
*
* Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
* basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
* the license for the specific language governing your rights and limitations.*/

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
