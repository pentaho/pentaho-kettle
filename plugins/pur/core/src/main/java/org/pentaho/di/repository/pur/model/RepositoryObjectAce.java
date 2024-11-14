/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/

package org.pentaho.di.repository.pur.model;

import java.util.EnumSet;

import org.pentaho.di.repository.ObjectRecipient;
import org.pentaho.platform.api.repository2.unified.RepositoryFilePermission;

public class RepositoryObjectAce implements ObjectAce, java.io.Serializable {

  private static final long serialVersionUID = 765743714498377456L; /* EESOURCE: UPDATE SERIALVERUID */

  private ObjectRecipient recipient;

  private EnumSet<RepositoryFilePermission> permissions;

  @Override
  public boolean equals( Object obj ) {
    if ( obj != null ) {
      RepositoryObjectAce ace = (RepositoryObjectAce) obj;

      if ( recipient == null && permissions == null && ace.getRecipient() == null && ace.getPermissions() == null ) {
        return true;
      } else if ( recipient != null && permissions != null ) {
        return recipient.equals( ace.getRecipient() ) && permissions.equals( ace.getPermissions() );
      } else if ( ace.getRecipient() != null && ace.getPermissions() != null ) {
        return ace.getRecipient().equals( recipient ) && ace.getPermissions().equals( permissions );
      } else if ( ace.getPermissions() == null && permissions == null ) {
        return recipient.equals( ace.getRecipient() );
      } else if ( ace.getRecipient() == null && recipient == null ) {
        return permissions.equals( ace.getPermissions() );
      } else {
        return false;
      }
    } else {
      return false;
    }
  }

  public RepositoryObjectAce( ObjectRecipient recipient ) {
    this.recipient = recipient;
  }

  public RepositoryObjectAce( ObjectRecipient recipient, RepositoryFilePermission first,
      RepositoryFilePermission... rest ) {
    this( recipient, EnumSet.of( first, rest ) );
  }

  public RepositoryObjectAce( ObjectRecipient recipient, EnumSet<RepositoryFilePermission> permissions ) {
    this( recipient );
    this.permissions = permissions;
  }

  public ObjectRecipient getRecipient() {
    return recipient;
  }

  public EnumSet<RepositoryFilePermission> getPermissions() {
    return permissions;
  }

  public void setRecipient( ObjectRecipient recipient ) {
    this.recipient = recipient;
  }

  public void setPermissions( EnumSet<RepositoryFilePermission> permissions ) {
    this.permissions = permissions;
  }

  public void setPermissions( RepositoryFilePermission first, RepositoryFilePermission... rest ) {
    this.permissions = EnumSet.of( first, rest );
  }

  @Override
  public String toString() {
    return recipient.toString();
  }
}
