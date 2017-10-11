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

package org.pentaho.di.ui.repository.repositoryexplorer.model;

public enum UIPermission {
  READ( "UIPermission.READ_DESC" ), CREATE( "UIPermission.CREATE_DESC" ), UPDATE( "UIPermission.UPDATE_DESC" ),
    MODIFY_PERMISSION( "UIPermission.MODIFY_PERMISSION_DESC" ), DELETE( "UIPermission.DELETE_DESC" );

  private String description;

  private UIPermission( String description ) {
    this.description = description;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription( String description ) {
    this.description = description;
  }

}
