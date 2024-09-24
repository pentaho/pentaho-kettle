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

import java.util.List;

import org.pentaho.di.repository.RepositorySecurityManager;
import org.pentaho.di.repository.RepositorySecurityProvider;
import org.pentaho.ui.xul.util.AbstractModelNode;

public class UIRepositoryUsers extends AbstractModelNode<IUIUser> {

  private static final long serialVersionUID = -8505587648560630174L;

  public UIRepositoryUsers() {
  }

  public UIRepositoryUsers( List<IUIUser> users ) {
    super( users );
  }

  public UIRepositoryUsers( RepositorySecurityProvider rsp, RepositorySecurityManager rsm ) {

    String[] logins;
    try {
      logins = rsp.getUserLogins();
      for ( String login : logins ) {
        this.add( UIObjectRegistry.getInstance().constructUIRepositoryUser( rsm.loadUserInfo( login ) ) );
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
