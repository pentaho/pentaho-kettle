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
package org.pentaho.di.ui.repository.pur.repositoryexplorer.abs.controller;

import java.util.List;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.ui.repository.pur.services.IAbsSecurityProvider;
import org.pentaho.di.ui.repository.repositoryexplorer.controllers.SlavesController;
import org.pentaho.di.ui.repository.repositoryexplorer.model.UISlave;

public class AbsSlavesController extends SlavesController implements java.io.Serializable {

  private static final long serialVersionUID = -556791670299584990L; /* EESOURCE: UPDATE SERIALVERUID */
  IAbsSecurityProvider service;
  boolean isAllowed = false;

  @Override
  protected boolean doLazyInit() {
    boolean superSucceeded = super.doLazyInit();
    if ( !superSucceeded ) {
      return false;
    }
    try {
      if ( repository.hasService( IAbsSecurityProvider.class ) ) {
        service = (IAbsSecurityProvider) repository.getService( IAbsSecurityProvider.class );
        setAllowed( allowedActionsContains( service, IAbsSecurityProvider.CREATE_CONTENT_ACTION ) );
      }
    } catch ( KettleException e ) {
      throw new RuntimeException( e );
    }
    return true;
  }

  @Override
  public void setEnableButtons( List<UISlave> slaves ) {
    if ( isAllowed ) {
      super.setEnableButtons( slaves );
    } else {
      enableButtons( false, false, false );
    }
  }

  public boolean isAllowed() {
    return isAllowed;
  }

  public void setAllowed( boolean isAllowed ) {
    this.isAllowed = isAllowed;
    this.firePropertyChange( "allowed", null, isAllowed );
  }

  private boolean allowedActionsContains( IAbsSecurityProvider service, String action ) throws KettleException {
    List<String> allowedActions = service.getAllowedActions( IAbsSecurityProvider.NAMESPACE );
    for ( String actionName : allowedActions ) {
      if ( action != null && action.equals( actionName ) ) {
        return true;
      }
    }
    return false;
  }

}
