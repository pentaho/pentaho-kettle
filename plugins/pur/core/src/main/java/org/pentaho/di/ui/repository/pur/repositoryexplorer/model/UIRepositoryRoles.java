/*!
 * Copyright 2010 - 2017 Hitachi Vantara.  All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.pentaho.di.ui.repository.pur.repositoryexplorer.model;

import java.util.List;

import org.pentaho.di.repository.pur.model.IRole;
import org.pentaho.di.ui.repository.pur.repositoryexplorer.IUIRole;
import org.pentaho.di.ui.repository.pur.repositoryexplorer.UIEEObjectRegistery;
import org.pentaho.di.ui.repository.pur.services.IRoleSupportSecurityManager;
import org.pentaho.ui.xul.util.AbstractModelNode;

public class UIRepositoryRoles extends AbstractModelNode<IUIRole> implements java.io.Serializable {

  private static final long serialVersionUID = -3386655857939500874L; /* EESOURCE: UPDATE SERIALVERUID */

  public UIRepositoryRoles() {
  }

  public UIRepositoryRoles( List<IUIRole> roles ) {
    super( roles );
  }

  public UIRepositoryRoles( IRoleSupportSecurityManager rsm ) {

    List<IRole> roleList;
    try {
      roleList = rsm.getRoles();
      for ( IRole role : roleList ) {
        this.add( UIEEObjectRegistery.getInstance().constructUIRepositoryRole( role ) );
      }
    } catch ( Exception e ) {
      // TODO: handle exception; can't get users???
    }
  }

  @Override
  protected void fireCollectionChanged() {
    this.changeSupport.firePropertyChange( "children", null, this );
  }

}
