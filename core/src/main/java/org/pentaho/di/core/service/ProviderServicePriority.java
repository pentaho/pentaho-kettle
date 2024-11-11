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

package org.pentaho.di.core.service;

class ProviderServicePriority<S> {
  private int priority;
  private S service;
  private Object provider;

  public ProviderServicePriority( Object provider, S service, int priority ) {
    this.provider = provider;
    this.service = service;
    this.priority = priority;
  }

  public S getService() {
    return service;
  }

  public void setService( S service ) {
    this.service = service;
  }

  public Object getProvider() {
    return provider;
  }

  public void setProvider( Object provider ) {
    this.provider = provider;
  }

  public int getPriority() {
    return priority;
  }

  public void setPriority( int priority ) {
    this.priority = priority;
  }
}
