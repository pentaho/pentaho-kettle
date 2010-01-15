/*
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2009 Pentaho Corporation.  All rights reserved.
 */
package org.pentaho.di.ui.repository.repositoryexplorer.model;

import java.util.Collection;



public class UIRepositoryDirectories extends AbstractModelNode<UIRepositoryObject>{

  @Override
  public void add(int index, UIRepositoryObject element) {
    if(isAccepted(element)) {
      super.add(index, element);
    }
    return;
  }

  @Override
  public boolean add(UIRepositoryObject child) {
    if(isAccepted(child)) {
      return super.add(child);
    }
    return false;
    }

  @Override
  public boolean addAll(Collection<? extends UIRepositoryObject> c) {
    for( Object o : c) {
      if(!isAccepted(o)) {
        return false;
      }
    }
    
    return super.addAll(c);
  }

  @Override
  public boolean addAll(int index, Collection<? extends UIRepositoryObject> c) {
    boolean acceptable = true;
    
    for( Object o : c) {
      if(!isAccepted(o)) {
        acceptable = false;
      }
    }
    
    if(acceptable) {
      return super.addAll(index, c);
    }
    
    return false;
  }

  @Override
  public UIRepositoryObject set(int index, UIRepositoryObject element) {
    if(isAccepted(element)) {
      return super.set(index, element);
    }
    return null;
  }

  public UIRepositoryDirectories(){
  }
  
  @Override
  protected void fireCollectionChanged() {
    this.changeSupport.firePropertyChange("children", null, this.getChildren());
  }

  /**
   * Tests if the storage of this object's type is permissible
   * 
   * @param o Object to test
   * @return true if the type is acceptable to store in this List
   */
  protected boolean isAccepted(Object o) {
    boolean result = true;
    if(o instanceof UIRepositoryDirectory) {
      // Check for duplication in name
      for(UIRepositoryObject child : getChildren()) {
        if(child.getName().equalsIgnoreCase(((UIRepositoryDirectory)o).getName())) {
          result = false;
        }
      }
    } else {
      result = false;
    }
     return result;
  }
  
}
