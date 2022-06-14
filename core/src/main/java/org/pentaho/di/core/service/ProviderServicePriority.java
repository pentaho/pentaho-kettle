/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2022 by Hitachi Vantara : http://www.pentaho.com
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
