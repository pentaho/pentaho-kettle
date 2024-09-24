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

package org.pentaho.di.ui.repository.repositoryexplorer.model;

import org.pentaho.di.repository.IUser;

public interface IUIUser extends Comparable<IUIUser> {

  public void setName( String name );

  public String getName();

  public String getDescription();

  public void setDescription( String desc );

  public void setPassword( String pass );

  public String getPassword();

  public IUser getUserInfo();
}
