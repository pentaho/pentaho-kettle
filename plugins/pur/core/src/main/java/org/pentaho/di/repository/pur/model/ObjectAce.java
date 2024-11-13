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

public interface ObjectAce {

  public ObjectRecipient getRecipient();

  public EnumSet<RepositoryFilePermission> getPermissions();

  public void setRecipient( ObjectRecipient recipient );

  public void setPermissions( RepositoryFilePermission first, RepositoryFilePermission... rest );

  public void setPermissions( EnumSet<RepositoryFilePermission> permissions );
}
