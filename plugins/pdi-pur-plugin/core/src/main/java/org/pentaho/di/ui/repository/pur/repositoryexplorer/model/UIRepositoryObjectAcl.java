/*!
 * Copyright 2010 - 2015 Pentaho Corporation.  All rights reserved.
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

import java.util.EnumSet;

import org.pentaho.di.repository.ObjectRecipient;
import org.pentaho.di.repository.pur.model.ObjectAce;
import org.pentaho.platform.api.repository2.unified.RepositoryFilePermission;
import org.pentaho.ui.xul.XulEventSourceAdapter;

/**
 * TODO mlowery This class represents an ACE, not an ACL.
 */
public class UIRepositoryObjectAcl extends XulEventSourceAdapter implements java.io.Serializable {

  private static final long serialVersionUID = 8320176731576605496L; /* EESOURCE: UPDATE SERIALVERUID */

  @Override
  public boolean equals( Object obj ) {
    if ( obj == null ) {
      return false;
    }
    UIRepositoryObjectAcl acl = (UIRepositoryObjectAcl) obj;
    return ace.equals( acl.getAce() );
  }

  protected ObjectAce ace;

  public ObjectAce getAce() {
    return ace;
  }

  public UIRepositoryObjectAcl( ObjectAce ace ) {
    this.ace = ace;
  }

  public String getRecipientName() {
    return ace.getRecipient().getName();
  }

  public void setRecipientName( String recipientName ) {
    ace.getRecipient().setName( recipientName );
    this.firePropertyChange( "recipientName", null, recipientName ); //$NON-NLS-1$
  }

  public ObjectRecipient.Type getRecipientType() {
    return ace.getRecipient().getType();
  }

  public void setRecipientType( ObjectRecipient.Type recipientType ) {
    ace.getRecipient().setType( recipientType );
    this.firePropertyChange( "recipientType", null, recipientType ); //$NON-NLS-1$
  }

  public EnumSet<RepositoryFilePermission> getPermissionSet() {
    return ace.getPermissions();
  }

  public void setPermissionSet( RepositoryFilePermission first, RepositoryFilePermission... rest ) {
    ace.setPermissions( first, rest );
    this.firePropertyChange( "permissions", null, ace.getPermissions() ); //$NON-NLS-1$
  }

  public void setPermissionSet( EnumSet<RepositoryFilePermission> permissionSet ) {
    EnumSet<RepositoryFilePermission> previousVal = ace.getPermissions();
    ace.setPermissions( permissionSet );
    this.firePropertyChange( "permissions", previousVal, ace.getPermissions() ); //$NON-NLS-1$
  }

  public void addPermission( RepositoryFilePermission permissionToAdd ) {
    ace.getPermissions().add( permissionToAdd );
  }

  public void removePermission( RepositoryFilePermission permissionToRemove ) {
    ace.getPermissions().remove( permissionToRemove );
  }

  @Override
  public String toString() {
    return ace.getRecipient().toString();
  }
}
